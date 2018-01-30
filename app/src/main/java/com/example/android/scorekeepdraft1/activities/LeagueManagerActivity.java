package com.example.android.scorekeepdraft1.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.FirestoreHelper;
import com.example.android.scorekeepdraft1.data.StatsContract;
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
        viewPager.setAdapter(new LeagueManagerPagerAdapter(fragmentManager));

        TabLayout tabLayout = findViewById(R.id.league_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onTeamSelected(String team) {
        if(standingsFragment != null) {
            standingsFragment.addNewPlayersDialog(team);
            standingsFragment.setAdderButtonVisible();
        }
    }

    @Override
    public void onNewTeam(String team) {
        if(standingsFragment != null) {
            standingsFragment.addTeam(team);
            standingsFragment.setAdderButtonVisible();
        }
    }

    @Override
    public void onCancel() {
        if(standingsFragment != null) {
            standingsFragment.setAdderButtonVisible();
        }
    }

    private class LeagueManagerPagerAdapter extends FragmentPagerAdapter {

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
    public void onSubmitPlayersListener(List<String> names, List<Integer> genders, String team) {

        for (int i = 0; i < names.size() - 1; i++) {
            ContentValues values = new ContentValues();
            String player = names.get(i);
            if (player.isEmpty()) {
                continue;
            }
            int gender = genders.get(i);
            values.put(StatsContract.StatsEntry.COLUMN_NAME, player);
            values.put(StatsContract.StatsEntry.COLUMN_GENDER, gender);
            values.put(StatsContract.StatsEntry.COLUMN_TEAM, team);
            getContentResolver().insert(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values);

//            View view = getCurrentFocus();
//
//            if (view != null) {
//                Log.d("xxx", "view = " + view.toString());
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                if (imm != null) {
//                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                    Log.d("xxx", "lma hide keyboard!!");
//                }
//            }
        }
        //todo change around when redoing team/player adder for when adding players to already-made team
        if (statsFragment != null) {
            String selection = StatsContract.StatsEntry.COLUMN_NAME + "=?";
            String[] selectionArgs = new String[]{team};
            Cursor cursor = getContentResolver().query(StatsContract.StatsEntry.CONTENT_URI_TEAMS,
                    null, selection, selectionArgs, null);
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(StatsContract.StatsEntry._ID);
                int id = cursor.getInt(idIndex);
                statsFragment.updateTeams(team, id);
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
            matchupFragment.updateBenchColors();
            matchupFragment.changeColorsRV(genderSettingsOn);
        }
    }
}