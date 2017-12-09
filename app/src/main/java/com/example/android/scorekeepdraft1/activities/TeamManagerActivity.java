package com.example.android.scorekeepdraft1.activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.FirestoreAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.fragments.LineupFragment;
import com.example.android.scorekeepdraft1.fragments.MatchupFragment;
import com.example.android.scorekeepdraft1.fragments.StandingsFragment;
import com.example.android.scorekeepdraft1.fragments.StatsFragment;
import com.example.android.scorekeepdraft1.fragments.TeamFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

public class TeamManagerActivity extends AppCompatActivity {

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
        final String leagueName = mainPageSelection.getName();
        final String leagueID = mainPageSelection.getId();
        final int leagueType = mainPageSelection.getType();
        final int level = mainPageSelection.getLevel();
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
        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return TeamFragment.newInstance(leagueID, leagueType, leagueName, level);
                    case 1:
                        if (level < 3) {
                            return null;
                        }
                        return LineupFragment.newInstance(leagueType, leagueName);
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
        });

        TabLayout tabLayout = findViewById(R.id.league_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }
}



