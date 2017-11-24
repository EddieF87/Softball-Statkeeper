package com.example.android.scorekeepdraft1.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.fragments.TeamFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;
import com.example.android.scorekeepdraft1.objects.Team;

import java.util.ArrayList;
import java.util.List;

public class TeamPagerActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private List<Integer> teamIDs;
    private static final String KEY_TEAM_IDS = "teamIDs";
    private static final String KEY_TEAM_URI = "teamURI";
    private Uri teamURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_pager);

        teamIDs = new ArrayList<>();

        Cursor cursor = getContentResolver().query(StatsContract.StatsEntry.CONTENT_URI_TEAMS,
                null, null, null, null);
        while (cursor.moveToNext()) {
            int teamID =  cursor.getInt(cursor.getColumnIndex(StatsContract.StatsEntry._ID));
            teamIDs.add(teamID);
        }
        cursor.close();

        Intent intent = getIntent();
        teamURI = intent.getData();
        int teamID = (int) ContentUris.parseId(teamURI);

        MyApp myApp = (MyApp) getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        if (mainPageSelection == null) {
            Intent nullIntent = new Intent(TeamPagerActivity.this, MainActivity.class);
            startActivity(nullIntent);
            finish();
        }
        final String leagueID = mainPageSelection.getId();
        final int selectionType = mainPageSelection.getType();
        final String leagueName = mainPageSelection.getName();
        setTitle(leagueName);

        mViewPager = findViewById(R.id.team_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                int id = teamIDs.get(position);
                Uri currentTeamUri = ContentUris.withAppendedId(StatsContract.StatsEntry.CONTENT_URI_TEAMS, id);
                return TeamFragment.newInstance(leagueID, selectionType, leagueName, currentTeamUri);
            }

            @Override
            public int getCount() {
                return teamIDs.size();
            }
        });

        for (int i = 0; i < teamIDs.size(); i++) {
            if (teamIDs.get(i) == teamID) {
                mViewPager.setCurrentItem(i);
            }
        }

    }
}
