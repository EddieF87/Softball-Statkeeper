package xyz.sleekstats.softball.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.TimeStampUpdater;
import xyz.sleekstats.softball.views.CustomViewPager;
import xyz.sleekstats.softball.adapters.PlayerStatsAdapter;
import xyz.sleekstats.softball.data.FirestoreHelper;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.dialogs.AddNewPlayersDialog;
import xyz.sleekstats.softball.dialogs.DeleteVsWaiversDialog;
import xyz.sleekstats.softball.dialogs.EditNameDialog;
import xyz.sleekstats.softball.dialogs.GameSettingsDialog;
import xyz.sleekstats.softball.dialogs.RemoveAllPlayersDialog;
import xyz.sleekstats.softball.fragments.LineupFragment;
import xyz.sleekstats.softball.fragments.TeamFragment;
import xyz.sleekstats.softball.objects.MainPageSelection;
import xyz.sleekstats.softball.objects.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamManagerActivity extends ExportActivity
        implements AddNewPlayersDialog.OnListFragmentInteractionListener,
        GameSettingsDialog.OnFragmentInteractionListener,
        RemoveAllPlayersDialog.OnFragmentInteractionListener,
        EditNameDialog.OnFragmentInteractionListener {

    private LineupFragment lineupFragment;
    private TeamFragment teamFragment;
    private String mTeamID;
    private int mLevel;
    private int mSelectionType;
    private String mTeamName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_viewpager);

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mTeamName = mainPageSelection.getName();
            mTeamID = mainPageSelection.getId();
            mSelectionType = mainPageSelection.getType();
            mLevel = mainPageSelection.getLevel();
            setTitle(mTeamName + " (Team)");
        } catch (Exception e) {
            Intent intent = new Intent(TeamManagerActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        CustomViewPager mViewPager = findViewById(R.id.team_view_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new TeamManagerPagerAdapter(fragmentManager));

        TabLayout tabLayout = findViewById(R.id.team_tab_layout);
        tabLayout.setupWithViewPager(mViewPager);


        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, null, null, null, null);
        if (cursor.moveToFirst()) {
            sendRetryGameLoadIntent();
            cursor.close();
            return;
        }
        cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_TEAMS, null, null, null, null);
        if (cursor.moveToFirst()) {
            sendRetryGameLoadIntent();
        }
        cursor.close();
    }

    private void sendRetryGameLoadIntent(){
        Intent intent = new Intent(TeamManagerActivity.this, FirestoreHelper.class);
        intent.putExtra(FirestoreHelper.STATKEEPER_ID, mTeamID);
        intent.setAction(FirestoreHelper.INTENT_RETRY_GAME_LOAD);
        startService(intent);
    }

    @Override
    public void onSubmitPlayersListener(List<String> names, List<Integer> genders, String teamName, String teamID) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < names.size() - 1; i++) {
            ContentValues values = new ContentValues();
            String playerName = names.get(i);
            if (playerName.isEmpty()) {
                continue;
            }
            int gender = genders.get(i);
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_GENDER, gender);
            values.put(StatsEntry.COLUMN_TEAM, teamName);
            values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamID);
            values.put(StatsEntry.COLUMN_ORDER, 99);
            values.put(StatsEntry.ADD, true);
            Uri uri = getContentResolver().insert(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values);
            if (uri != null) {
                Cursor cursor = getContentResolver().query(uri, null, null,
                        null, null);
                if (cursor.moveToFirst()) {
                    players.add(new Player(cursor, false));
                }
                cursor.close();
            }
        }

        if (!players.isEmpty()) {
            TimeStampUpdater.updateTimeStamps(TeamManagerActivity.this, mTeamID, System.currentTimeMillis());

            if (lineupFragment != null) {
                lineupFragment.updateBench(players);
            }
            if (teamFragment != null) {
                teamFragment.addPlayers(players);
            }
        }
    }

    @Override
    public void onGameSettingsChanged(int innings, int genderSorter) {
        boolean genderSettingsOn = genderSorter != 0;

        if (lineupFragment != null) {
            lineupFragment.changeColorsRV(genderSettingsOn);
            lineupFragment.setGameSettings(innings, genderSorter);
        }
        if (teamFragment != null) {
            teamFragment.changeColorsRV(genderSettingsOn);
        }
    }

    @Override
    public void onRemoveChoice(int choice) {
        if (choice == DeleteVsWaiversDialog.CHOICE_DELETE) {
            if (teamFragment != null) {
                List<String> firestoreIDsToDelete = teamFragment.deletePlayers();
                if (lineupFragment != null && !firestoreIDsToDelete.isEmpty()) {
                    lineupFragment.removePlayers(firestoreIDsToDelete);
                }
            }
        }
    }

    @Override
    public void onEdit(String enteredText, int type) {
        if (enteredText.isEmpty()) {
            return;
        }
        boolean update = false;

        if (teamFragment != null) {
            update = teamFragment.updateTeamName(enteredText);
        }

        if (update) {
            TimeStampUpdater.updateTimeStamps(TeamManagerActivity.this, mTeamID, System.currentTimeMillis());
        }
    }

    private class TeamManagerPagerAdapter extends FragmentPagerAdapter {

        TeamManagerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TeamFragment.newInstance(mTeamID, mSelectionType, mTeamName, mLevel);
                case 1:
                    if (mLevel < UsersActivity.LEVEL_VIEW_WRITE) {
                        return null;
                    }
                    return LineupFragment.newInstance(mTeamID, mSelectionType, mTeamName, mTeamID, false);
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Stats";
                case 1:
                    return "Game";
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            if (mLevel < UsersActivity.LEVEL_VIEW_WRITE) {
                return 1;
            }
            return 2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);

            switch (position) {
                case 0:
                    teamFragment = (TeamFragment) createdFragment;
                    break;
                case 1:
                    lineupFragment = (LineupFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PlayerStatsAdapter.REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    String deletedPlayer = data.getStringExtra(StatsEntry.DELETE);
                    if (teamFragment != null) {
                        teamFragment.removePlayerFromTeam(deletedPlayer);
                    }
                    if (lineupFragment != null) {
                        List<String> players = new ArrayList<>();
                        players.add(deletedPlayer);
                        lineupFragment.removePlayers(players);
                    }
                } else if (resultCode == 17) {
                    if (teamFragment != null) {
                        String id = data.getStringExtra(StatsEntry.COLUMN_FIRESTORE_ID);
                        int gender = data.getIntExtra(StatsEntry.COLUMN_GENDER, -1);

                        if (teamFragment != null) {
                            teamFragment.updatePlayerGender(gender, id);
                        }

                        if (lineupFragment != null) {
                            lineupFragment.updatePlayerGender(gender, id);
                        }
                    }
                } else if (resultCode == 18) {
                    if (teamFragment != null) {
                        String id = data.getStringExtra(StatsEntry.COLUMN_FIRESTORE_ID);
                        String name = data.getStringExtra(StatsEntry.COLUMN_NAME);

                        if (teamFragment != null) {
                            teamFragment.updatePlayerName(name, id);
                        }

                        if (lineupFragment != null) {
                            lineupFragment.updatePlayerName(name, id);
                        }
                    }
                } else if (resultCode == 19) {
                    if (teamFragment != null) {
                        String id = data.getStringExtra(StatsEntry.COLUMN_FIRESTORE_ID);
                        String name = data.getStringExtra(StatsEntry.COLUMN_NAME);

                        if (teamFragment != null) {
                            teamFragment.updatePlayerName(name, id);
                        }

                        if (lineupFragment != null) {
                            lineupFragment.updatePlayerName(name, id);
                        }
                    }


                }
            }
        } catch (Exception ex) {
            Toast.makeText(TeamManagerActivity.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TeamManagerActivity.this, MainActivity.class);
        startActivity(intent);
    }
}