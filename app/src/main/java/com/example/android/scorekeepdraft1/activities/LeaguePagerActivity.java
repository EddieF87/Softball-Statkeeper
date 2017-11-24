package com.example.android.scorekeepdraft1.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.fragments.MatchupFragment;
import com.example.android.scorekeepdraft1.fragments.StandingsFragment;
import com.example.android.scorekeepdraft1.fragments.StatsFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

public class LeaguePagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_pager);

        MyApp myApp = (MyApp) getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        if(mainPageSelection == null) {
            Intent intent = new Intent(LeaguePagerActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        String leagueName = mainPageSelection.getName();
        final String leagueID = mainPageSelection.getId();
        setTitle(leagueName);

        ViewPager viewPager = findViewById(R.id.league_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new StandingsFragment();
                    case 1:
                        return new StatsFragment();
                    case 2:
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
                return 3;
            }
        });

        TabLayout tabLayout = findViewById(R.id.league_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }
}