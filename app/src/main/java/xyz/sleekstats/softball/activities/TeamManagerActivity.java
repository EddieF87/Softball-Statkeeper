package xyz.sleekstats.softball.activities;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.FirestoreHelperService;
import xyz.sleekstats.softball.data.TimeStampUpdater;
import xyz.sleekstats.softball.views.CustomViewPager;
import xyz.sleekstats.softball.adapters.PlayerStatsAdapter;
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
    private FirestoreHelperService mService;
    private ServiceConnection mServiceConnection;

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


        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mTeamID};
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            sendRetryGameLoadIntent();
            cursor.close();
            return;
        }
        cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_TEAMS, null, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            sendRetryGameLoadIntent();
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private void sendRetryGameLoadIntent(){
        Intent intent = new Intent(TeamManagerActivity.this, FirestoreHelperService.class);
        intent.putExtra(FirestoreHelperService.STATKEEPER_ID, mTeamID);
        intent.setAction(FirestoreHelperService.INTENT_RETRY_GAME_LOAD);
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
            values.put(StatsEntry.COLUMN_LEAGUE_ID, teamID);
            values.put(StatsEntry.COLUMN_ORDER, 99);
            values.put(StatsEntry.ADD, true);
            Uri uri = getContentResolver().insert(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values);
            if (uri != null) {
                String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
                String[] selectionArgs = new String[]{mTeamID};
                Cursor cursor = getContentResolver().query(uri, null, selection,
                        selectionArgs, null);
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
            Log.d("megaman", "teamManagerActivity onActivityResult" + resultCode);
            if (requestCode == PlayerStatsAdapter.REQUEST_CODE) {
                switch (resultCode) {
                    case RESULT_OK:
                        String deletedPlayer = data.getStringExtra(StatsEntry.DELETE);
                        if (teamFragment != null) {
                            teamFragment.removePlayerFromTeam(deletedPlayer);
                        }
                        if (lineupFragment != null) {
                            List<String> players = new ArrayList<>();
                            players.add(deletedPlayer);
                            lineupFragment.removePlayers(players);
                        }
                        break;
                    case 17:
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
                        break;
                    case 18:
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
                        break;
                    case 19:
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


                        break;
                }
            } else if(requestCode == GameActivity.REQUEST_CODE_GAME && resultCode == GameActivity.RESULT_CODE_GAME_FINISHED) {
                Toast.makeText(TeamManagerActivity.this, "Saving game results.\nStats should be updated shortly.", Toast.LENGTH_SHORT).show();
                Log.d("megaman", "LeagueManagerActivity onActivityResult  GAME COMPLETE" + requestCode + resultCode);
            } else {
                Log.d("megaman", "LeagueManagerActivity onActivityResult  GAME ONGOING" + requestCode + resultCode +
                "\n requestCode == GameActivity.RESULT_CODE_GAME_FINISHED  = " + (requestCode == GameActivity.RESULT_CODE_GAME_FINISHED) +
                        "\n resultCode == GameActivity.RESULT_CODE_GAME_FINISHED  = " + (resultCode == GameActivity.RESULT_CODE_GAME_FINISHED));
            }
        } catch (Exception ex) {
            Toast.makeText(TeamManagerActivity.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void bindService() {
        if(mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    FirestoreHelperService.MyBinder mBinder = (FirestoreHelperService.MyBinder) iBinder;
                    mService = mBinder.getService();
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {

                }
            };
        }
//        bindService(mService, mServiceConnection, Context.BIND_IMPORTANT);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TeamManagerActivity.this, MainActivity.class);
        startActivity(intent);
    }
}