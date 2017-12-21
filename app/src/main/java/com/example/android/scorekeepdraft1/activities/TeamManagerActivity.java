package com.example.android.scorekeepdraft1.activities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.FirestoreAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.fragments.CreateTeamFragment;
import com.example.android.scorekeepdraft1.fragments.GameSettingsDialogFragment;
import com.example.android.scorekeepdraft1.fragments.LineupFragment;
import com.example.android.scorekeepdraft1.fragments.MatchupFragment;
import com.example.android.scorekeepdraft1.fragments.StandingsFragment;
import com.example.android.scorekeepdraft1.fragments.StatsFragment;
import com.example.android.scorekeepdraft1.fragments.TeamFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamManagerActivity extends AppCompatActivity
        implements CreateTeamFragment.OnListFragmentInteractionListener,
        GameSettingsDialogFragment.OnFragmentInteractionListener {

    private LineupFragment lineupFragment;
    private TeamFragment teamFragment;
    private String teamID;
    private int level;
    private int leagueType;
    private String leagueName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_pager);

        MyApp myApp = (MyApp) getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        if(mainPageSelection == null) {
            Intent intent = new Intent(TeamManagerActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        leagueName = mainPageSelection.getName();
        teamID = mainPageSelection.getId();
        leagueType = mainPageSelection.getType();
        level = mainPageSelection.getLevel();
        setTitle(leagueName);

        Cursor cursor = getContentResolver().query(StatsContract.StatsEntry.CONTENT_URI_TEAMS,
                null, null, null, null);
        if (!cursor.moveToFirst()) {
            Toast.makeText(this, "syncing", Toast.LENGTH_SHORT).show();
            FirestoreAdapter firestoreAdapter = new FirestoreAdapter(this);
            firestoreAdapter.syncStats();
        }
        cursor.close();

        ViewPager viewPager = findViewById(R.id.league_view_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new TeamManagerPagerAdapter(fragmentManager));

        TabLayout tabLayout = findViewById(R.id.league_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onSubmitPlayersListener(List<String> names, List<Integer> genders, String team) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < names.size() - 1; i++) {
            ContentValues values = new ContentValues();
            String playerName = names.get(i);
            if (playerName.isEmpty()) {
                continue;
            }
            int gender = genders.get(i);
            values.put(StatsContract.StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsContract.StatsEntry.COLUMN_GENDER, gender);
            values.put(StatsContract.StatsEntry.COLUMN_TEAM, team);
            Uri uri = getContentResolver().insert(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values);
            if (uri != null) {
                Cursor cursor = getContentResolver().query(uri, null, null,
                        null, null);
                if (cursor.moveToFirst()) {
                    String firestoreID = cursor.getString(cursor
                            .getColumnIndex(StatsContract.StatsEntry.COLUMN_FIRESTORE_ID));
                    long id = ContentUris.parseId(uri);
                    players.add(new Player(playerName, team, gender, id, firestoreID));
                }
            }
        }
        if (!players.isEmpty()) {
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
        }
        if (teamFragment != null) {
            teamFragment.changeColorsRV(genderSettingsOn);
        }
    }

    private class TeamManagerPagerAdapter extends FragmentPagerAdapter {

        public TeamManagerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TeamFragment.newInstance(teamID, leagueType, leagueName, level);
                case 1:
                    if (level < 3) {
                        return null;
                    }
                    return LineupFragment.newInstance(teamID, leagueType, leagueName);
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
            if (level < 3) {
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
}