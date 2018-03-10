package com.example.android.softballstatkeeper.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.softballstatkeeper.MyApp;
import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.data.FirestoreHelper;
import com.example.android.softballstatkeeper.data.StatsContract;
import com.example.android.softballstatkeeper.data.StatsContract.StatsEntry;
import com.example.android.softballstatkeeper.dialogs.AddNewPlayersDialogFragment;
import com.example.android.softballstatkeeper.dialogs.ChangeTeamDialogFragment;
import com.example.android.softballstatkeeper.dialogs.ChooseOrCreateTeamDialogFragment;
import com.example.android.softballstatkeeper.dialogs.GameSettingsDialogFragment;
import com.example.android.softballstatkeeper.fragments.MatchupFragment;
import com.example.android.softballstatkeeper.fragments.StandingsFragment;
import com.example.android.softballstatkeeper.fragments.StatsFragment;
import com.example.android.softballstatkeeper.objects.MainPageSelection;
import com.example.android.softballstatkeeper.objects.Team;

import java.util.ArrayList;
import java.util.List;

public class LeagueManagerActivity extends ExportActivity
        implements AddNewPlayersDialogFragment.OnListFragmentInteractionListener,
        GameSettingsDialogFragment.OnFragmentInteractionListener,
        ChooseOrCreateTeamDialogFragment.OnFragmentInteractionListener,
        ChangeTeamDialogFragment.OnFragmentInteractionListener {

    private StandingsFragment standingsFragment;
    private StatsFragment statsFragment;
    private MatchupFragment matchupFragment;

    private String leagueID;
    private int level;
    private String leagueName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lg_mgr_pager);

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            leagueName = mainPageSelection.getName();
            leagueID = mainPageSelection.getId();
            level = mainPageSelection.getLevel();
            setTitle(leagueName);
        } catch (Exception e) {
            Intent intent = new Intent(LeagueManagerActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        ViewPager viewPager = findViewById(R.id.league_view_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(new LeagueManagerPagerAdapter(fragmentManager));

        TabLayout tabLayout = findViewById(R.id.league_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void chooseTeamDialog(ArrayList<Team> teams) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = ChangeTeamDialogFragment.newInstance(teams, null, null);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void onNewTeam(String teamName) {
        if (teamName.isEmpty()) {
            Toast.makeText(LeagueManagerActivity.this, "Please type a team name", Toast.LENGTH_SHORT).show();
        } else {
            if (standingsFragment != null) {
                standingsFragment.addTeam(teamName);
            }

            if (statsFragment != null) {
                String selection = StatsEntry.COLUMN_NAME + "=?";
                String[] selectionArgs = new String[]{teamName};
                Cursor cursor = getContentResolver().query(StatsContract.StatsEntry.CONTENT_URI_TEAMS,
                        null, selection, selectionArgs, null);
                if (cursor.moveToFirst()) {
                    int id = StatsContract.getColumnInt(cursor, StatsEntry._ID);
                    String firestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
                    statsFragment.updateTeams(teamName, id, firestoreID);
                }
            }
        }
    }

    @Override
    public void onCancel() {
        if (standingsFragment != null) {
            standingsFragment.setAdderButtonVisible();
        }
    }

    @Override
    public void onTeamChosen(String playerID, String teamName, String teamID) {
        if (standingsFragment != null) {
            standingsFragment.addNewPlayersDialog(teamName, teamID);
            standingsFragment.setAdderButtonVisible();
        }
    }

    @Override
    public void onTeamChoiceCancel() {
        if (standingsFragment != null) {
            standingsFragment.setAdderButtonVisible();
        }
    }

    private class LeagueManagerPagerAdapter extends FragmentStatePagerAdapter {

        LeagueManagerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return StandingsFragment.newInstance(leagueID, level, leagueName);
                case 1:
                    return StatsFragment.newInstance(leagueID, level, leagueName);
                case 2:
                    if (level < 3) {
                        return null;
                    }
                    return MatchupFragment.newInstance(leagueID, leagueName);
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Standings";
                case 1:
                    return "Stats";
                case 2:
                    return "Game";
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            if (level < 3) {
                return 2;
            }
            return 3;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);

            switch (position) {
                case 0:
                    standingsFragment = (StandingsFragment) createdFragment;
                    break;
                case 1:
                    statsFragment = (StatsFragment) createdFragment;
                    break;
                case 2:
                    matchupFragment = (MatchupFragment) createdFragment;
            }
            return createdFragment;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settingsPreferences = getSharedPreferences(leagueID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
        int innings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);
        int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
        onGameSettingsChanged(innings, genderSorter);
        boolean genderSettingsOn = genderSorter != 0;

        if (statsFragment != null) {
            statsFragment.changeColorsRV(genderSettingsOn);
        }

        if (matchupFragment != null) {
            matchupFragment.setGameSettings();
            matchupFragment.updateBenchColors();
            matchupFragment.changeColorsRV(genderSettingsOn);
            matchupFragment.updateMatchup();
        }
    }

    @Override
    public void onSubmitPlayersListener(List<String> names, List<Integer> genders, String team, String teamID) {
        boolean update = false;

        for (int i = 0; i < names.size() - 1; i++) {
            ContentValues values = new ContentValues();
            String player = names.get(i);
            if (player.isEmpty()) {
                continue;
            }
            int gender = genders.get(i);
            values.put(StatsEntry.COLUMN_NAME, player);
            values.put(StatsEntry.COLUMN_GENDER, gender);
            values.put(StatsEntry.COLUMN_ORDER, i + 1);
            values.put(StatsEntry.COLUMN_TEAM, team);
            values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamID);
            values.put(StatsEntry.ADD, true);
            Uri uri = getContentResolver().insert(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values);
            if(uri != null) {
                update = true;
            }
        }
        if(update) {
            new FirestoreHelper(this, leagueID).updateTimeStamps();
            Log.d("hhh", "updatetimestamps");
        }

        if(matchupFragment != null) {
            matchupFragment.updateMatchup();
        }
    }

    @Override
    public void onGameSettingsChanged(int innings, int genderSorter) {
        boolean genderSettingsOn = genderSorter != 0;

        if (statsFragment != null) {
            statsFragment.changeColorsRV(genderSettingsOn);
        }

        if (matchupFragment != null) {
            matchupFragment.setGameSettings();
            matchupFragment.updateBenchColors();
            matchupFragment.changeColorsRV(genderSettingsOn);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            SharedPreferences settingsPreferences = getSharedPreferences(leagueID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
//            int innings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);
//            int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
//            onGameSettingsChanged(innings, genderSorter);
//
//            if (matchupFragment != null) {
//                matchupFragment.updateMatchup();
//            }
//        }
    }
}