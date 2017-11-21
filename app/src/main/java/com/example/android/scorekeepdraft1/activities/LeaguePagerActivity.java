package com.example.android.scorekeepdraft1.activities;

import android.app.ActionBar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.fragments.StandingsFragment;
import com.example.android.scorekeepdraft1.fragments.StatsFragment;

public class LeaguePagerActivity extends AppCompatActivity {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_pager);

        mViewPager = findViewById(R.id.league_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new StandingsFragment();
                    case 1:
                        return new StatsFragment();
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
                    default:
                        return null;
                }
            }
            @Override
            public int getCount() {
                return 2;
            }
        });

        final ActionBar actionBar = getActionBar();

        TabLayout tabLayout = findViewById(R.id.league_tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
//        // Specify that tabs should be displayed in the action bar.
//        if (actionBar == null) {return;}
//
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        // Create a tab listener that is called when the user changes tabs.
//        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
//            @Override
//            public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
//                mViewPager.setCurrentItem(tab.getPosition());
//            }
//
//            @Override
//            public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
//                // hide the given tab
//
//            }
//
//            @Override
//            public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
//                // probably ignore this event
//
//            }
//        };
//        actionBar.addTab(
//                    actionBar.newTab()
//                            .setText("Standings ")
//                            .setTabListener(tabListener));
//        actionBar.addTab(
//                actionBar.newTab()
//                        .setText("Stats ")
//                        .setTabListener(tabListener));
//
//        mViewPager.addOnPageChangeListener(
//                new ViewPager.SimpleOnPageChangeListener() {
//                    @Override
//                    public void onPageSelected(int position) {
//                        getActionBar().setSelectedNavigationItem(position);
//                    }
//                });
    }
}