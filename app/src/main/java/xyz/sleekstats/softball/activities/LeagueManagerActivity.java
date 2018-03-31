package xyz.sleekstats.softball.activities;

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
import android.view.ViewGroup;
import android.widget.Toast;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.FirestoreHelper;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.data.TimeStampUpdater;
import xyz.sleekstats.softball.dialogs.AddNewPlayersDialog;
import xyz.sleekstats.softball.dialogs.ChangeTeamDialog;
import xyz.sleekstats.softball.dialogs.ChooseOrCreateTeamDialog;
import xyz.sleekstats.softball.dialogs.GameSettingsDialog;
import xyz.sleekstats.softball.fragments.MatchupFragment;
import xyz.sleekstats.softball.fragments.StandingsFragment;
import xyz.sleekstats.softball.fragments.StatsFragment;
import xyz.sleekstats.softball.objects.MainPageSelection;
import xyz.sleekstats.softball.objects.Team;

import java.util.ArrayList;
import java.util.List;

public class LeagueManagerActivity extends ExportActivity
        implements AddNewPlayersDialog.OnListFragmentInteractionListener,
        GameSettingsDialog.OnFragmentInteractionListener,
        ChooseOrCreateTeamDialog.OnFragmentInteractionListener,
        ChangeTeamDialog.OnFragmentInteractionListener,
        MatchupFragment.OnFragmentInteractionListener,
        StandingsFragment.OnFragmentInteractionListener,
        StatsFragment.OnFragmentInteractionListener {

    private StandingsFragment standingsFragment;
    private StatsFragment statsFragment;
    private MatchupFragment matchupFragment;

    private String mLeagueID;
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
            mLeagueID = mainPageSelection.getId();
            level = mainPageSelection.getLevel();
            setTitle(leagueName + " (League)");
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

        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, null, null, null, null);
        if(cursor.moveToFirst()){
            sendRetryGameLoadIntent();
            cursor.close();
            return;
        }
        cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_TEAMS, null, null, null, null);
        if(cursor.moveToFirst()){
            sendRetryGameLoadIntent();
            cursor.close();
            return;
        }
        cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_BOXSCORES, null, null, null, null);
        if(cursor.moveToFirst()){
            sendRetryGameLoadIntent();
            cursor.close();
            return;
        }
        cursor.close();
    }

    private void sendRetryGameLoadIntent(){
        Intent intent = new Intent(LeagueManagerActivity.this, FirestoreHelper.class);
        intent.putExtra(FirestoreHelper.STATKEEPER_ID, mLeagueID);
        intent.setAction(FirestoreHelper.INTENT_RETRY_GAME_LOAD);
        startService(intent);
    }

    @Override
    public void chooseTeamDialog(ArrayList<Team> teams) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = ChangeTeamDialog.newInstance(teams, null, null);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void onNewTeam(String teamName) {
        if (teamName.isEmpty()) {
            Toast.makeText(LeagueManagerActivity.this, "Please type a team name", Toast.LENGTH_SHORT).show();
        } else {
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_NAME, teamName);
            values.put(StatsEntry.ADD, true);
            Uri teamUri = getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);

            if (teamUri == null) {
                return;
            }
            TimeStampUpdater.updateTimeStamps(LeagueManagerActivity.this, mLeagueID, System.currentTimeMillis());

            Cursor cursor = getContentResolver().query(teamUri, new String[]{StatsEntry._ID, StatsEntry.COLUMN_FIRESTORE_ID}, null, null, null);
            if (cursor.moveToFirst()) {
                int id = StatsContract.getColumnInt(cursor, StatsEntry._ID);
                String firestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
                addNewPlayersDialog(teamName, firestoreID);

                if (standingsFragment != null) {
                    standingsFragment.setAdderButtonVisible();
                }
                if (statsFragment != null) {
                    statsFragment.setAdderButtonVisible();
                    statsFragment.updateTeams(teamName, id, firestoreID);
                }
            }
        }
    }

    private void addNewPlayersDialog(String teamName, String teamID) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = AddNewPlayersDialog.newInstance(teamName, teamID);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void onCancel() {
        if (standingsFragment != null) {
            standingsFragment.setAdderButtonVisible();
        }
        if (statsFragment != null) {
            statsFragment.setAdderButtonVisible();
        }
    }

    @Override
    public void onTeamChosen(String playerFirestoreID, String teamName, String teamFirestoreID) {
        addNewPlayersDialog(teamName, teamFirestoreID);

        if (standingsFragment != null) {
            standingsFragment.setAdderButtonVisible();
        }
        if (statsFragment != null) {
            statsFragment.setAdderButtonVisible();
        }
    }

    @Override
    public void onTeamChoiceCancel() {
        if (standingsFragment != null) {
            standingsFragment.setAdderButtonVisible();
        }
        if (statsFragment != null) {
            statsFragment.setAdderButtonVisible();
        }
    }

    @Override
    public void goToUserSettings() {
        Intent settingsIntent = new Intent(LeagueManagerActivity.this, UsersActivity.class);
        startActivity(settingsIntent);
    }

    @Override
    public void onExport() {
        startExport(leagueName);
    }

    @Override
    public void startAdder(ArrayList<Team> teams) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = ChooseOrCreateTeamDialog.newInstance(teams);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void clearGameDB() {
        getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
        getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        SharedPreferences savedGamePreferences = getSharedPreferences(mLeagueID + StatsEntry.GAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedGamePreferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void goToGameSettings() {
        SharedPreferences settingsPreferences = getSharedPreferences(mLeagueID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
        int innings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);
        int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = GameSettingsDialog.newInstance(innings, genderSorter, mLeagueID, 0);
        newFragment.show(fragmentTransaction, "");
    }

    private class LeagueManagerPagerAdapter extends FragmentStatePagerAdapter {

        LeagueManagerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return StandingsFragment.newInstance(level, leagueName);
                case 1:
                    return StatsFragment.newInstance(mLeagueID, level, leagueName);
                case 2:
                    if (level < UsersActivity.LEVEL_VIEW_WRITE) {
                        return null;
                    }
                    return MatchupFragment.newInstance(mLeagueID, leagueName);
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
            if (level < UsersActivity.LEVEL_VIEW_WRITE) {
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
                    break;
            }
            return createdFragment;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settingsPreferences = getSharedPreferences(mLeagueID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
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
            if (uri != null) {
                update = true;
            }
        }
        if (update) {
            TimeStampUpdater.updateTimeStamps(LeagueManagerActivity.this, mLeagueID, System.currentTimeMillis());
        }

        if (matchupFragment != null) {
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
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LeagueManagerActivity.this, MainActivity.class);
        startActivity(intent);
    }
}