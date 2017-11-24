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
import com.example.android.scorekeepdraft1.fragments.PlayerFragment;
import com.example.android.scorekeepdraft1.fragments.TeamFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

import java.util.ArrayList;
import java.util.List;

public class PlayerPagerActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private List<Integer> playerIDs;
    private static final String KEY_PLAYER_IDS = "playerIDs";
    private static final String KEY_PLAYER_URI = "playerURI";
    private Uri playerURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_pager);
        playerIDs = new ArrayList<>();

        Cursor cursor = getContentResolver().query(StatsContract.StatsEntry.CONTENT_URI_PLAYERS,
                null, null, null, null);
        while (cursor.moveToNext()) {
            int playerID =  cursor.getInt(cursor.getColumnIndex(StatsContract.StatsEntry._ID));
            playerIDs.add(playerID);
        }
        cursor.close();

        Intent intent = getIntent();
        playerURI = intent.getData();
        int playerID = (int) ContentUris.parseId(playerURI);

        MyApp myApp = (MyApp) getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        if (mainPageSelection == null) {
            Intent nullIntent = new Intent(PlayerPagerActivity.this, MainActivity.class);
            startActivity(nullIntent);
            finish();
        }
        final String leagueID = mainPageSelection.getId();
        final int selectionType = mainPageSelection.getType();
        final String leagueName = mainPageSelection.getName();
        setTitle(leagueName);

        mViewPager = findViewById(R.id.player_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                int id = playerIDs.get(position);
                Uri currentplayerUri = ContentUris.withAppendedId(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, id);
                return PlayerFragment.newInstance(leagueID, selectionType, leagueName, currentplayerUri);
            }

            @Override
            public int getCount() {
                return playerIDs.size();
            }
        });

        for (int i = 0; i < playerIDs.size(); i++) {
            if (playerIDs.get(i) == playerID) {
                mViewPager.setCurrentItem(i);
            }
        }

    }
}
