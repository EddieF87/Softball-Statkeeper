package com.example.android.scorekeepdraft1.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.FirestoreHelper;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.dialogs.AddNewPlayersDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.ChooseOrCreateTeamDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.GameSettingsDialogFragment;
import com.example.android.scorekeepdraft1.fragments.MatchupFragment;
import com.example.android.scorekeepdraft1.fragments.StandingsFragment;
import com.example.android.scorekeepdraft1.fragments.StatsFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

import java.util.List;

public class LeagueManagerActivity extends ExportActivity
        implements AddNewPlayersDialogFragment.OnListFragmentInteractionListener,
        GameSettingsDialogFragment.OnFragmentInteractionListener,
        ChooseOrCreateTeamDialogFragment.OnFragmentInteractionListener{

    private StandingsFragment standingsFragment;
    private StatsFragment statsFragment;
    private MatchupFragment matchupFragment;

    private String leagueID;
    private int level;
    private String leagueName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_pager);

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();leagueName = mainPageSelection.getName();
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
    public void onTeamSelected(String teamName, String teamID) {
        if(standingsFragment != null) {
            standingsFragment.addNewPlayersDialog(teamName, teamID);
            standingsFragment.setAdderButtonVisible();
        }
    }

    @Override
    public void onNewTeam(String teamName) {
        if(standingsFragment != null) {
            standingsFragment.addTeam(teamName);
            standingsFragment.setAdderButtonVisible();
        }
    }

    @Override
    public void onCancel() {
        if(standingsFragment != null) {
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
    public void onSubmitPlayersListener(List<String> names, List<Integer> genders, String team, String teamID) {

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
            getContentResolver().insert(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values);
        }

        if (statsFragment != null) {
            String selection = StatsEntry.COLUMN_NAME + "=?";
            String[] selectionArgs = new String[]{team};
            Cursor cursor = getContentResolver().query(StatsContract.StatsEntry.CONTENT_URI_TEAMS,
                    null, selection, selectionArgs, null);
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(StatsEntry._ID);
                int firestoreIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
                int id = cursor.getInt(idIndex);
                String firestoreID = cursor.getString(firestoreIDIndex);
                statsFragment.updateTeams(team, id, firestoreID);
            }
        }
        new FirestoreHelper(this, leagueID).updateTimeStamps();
    }

    @Override
    public void onGameSettingsChanged(int innings, int genderSorter) {
        boolean genderSettingsOn = genderSorter != 0;

        if (statsFragment != null) {
            statsFragment.changeColorsRV(genderSettingsOn);
        }

        if (matchupFragment != null) {
            matchupFragment.setGameSettings(innings, genderSorter);
            matchupFragment.updateBenchColors();
            matchupFragment.changeColorsRV(genderSettingsOn);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            SharedPreferences settingsPreferences = getSharedPreferences(leagueID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
            int innings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);
            int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
            onGameSettingsChanged(innings, genderSorter);
        }
    }
}