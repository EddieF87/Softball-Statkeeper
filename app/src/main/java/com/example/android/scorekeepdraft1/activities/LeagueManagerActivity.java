package com.example.android.scorekeepdraft1.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.fragments.CreateTeamFragment;
import com.example.android.scorekeepdraft1.fragments.GameSettingsDialogFragment;
import com.example.android.scorekeepdraft1.fragments.MatchupFragment;
import com.example.android.scorekeepdraft1.fragments.StandingsFragment;
import com.example.android.scorekeepdraft1.fragments.StatsFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

import java.util.List;

public class LeagueManagerActivity extends AppCompatActivity
        implements CreateTeamFragment.OnListFragmentInteractionListener,
        GameSettingsDialogFragment.OnFragmentInteractionListener {

    private String leagueID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_pager);

        MyApp myApp = (MyApp) getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        if(mainPageSelection == null) {
            Intent intent = new Intent(LeagueManagerActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        String leagueName = mainPageSelection.getName();
        leagueID = mainPageSelection.getId();
        final int level = mainPageSelection.getLevel();
        setTitle(leagueName);

        ViewPager viewPager = findViewById(R.id.league_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return StandingsFragment.newInstance(leagueID, level);
                    case 1:
                        return StatsFragment.newInstance(leagueID, level);
                    case 2:
                        if(level < 3) {
                            return null;
                        }
                        return MatchupFragment.newInstance(leagueID);
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
        });

        TabLayout tabLayout = findViewById(R.id.league_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
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

            View view = getCurrentFocus();

            if (view != null) {
                Log.d("xxx", "view = " + view.toString());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    Log.d("xxx", "lma hide keyboard!!");
                }
            }
        }
    }

    @Override
    public void onGameSettingsChanged(int innings, int femaleOrder) {
//        SharedPreferences sharedPreferences = getSharedPreferences(leagueID + "settings", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt("innings", innings);
//        editor.putInt("genderSort", femaleOrder);
//        editor.commit();

    }
}