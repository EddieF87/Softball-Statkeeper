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
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.fragments.PlayerFragment;
import com.example.android.scorekeepdraft1.fragments.TeamFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

import java.util.ArrayList;
import java.util.List;

public class ObjectPagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_pager);
    }

    protected void startPager(final int objectType, final Uri uri) {
        final List<Integer> objectIDs = new ArrayList<>();

        Cursor cursor = getContentResolver().query(uri, null,
                null, null, null);
        while (cursor.moveToNext()) {
            int objectID = cursor.getInt(cursor.getColumnIndex(StatsContract.StatsEntry._ID));
            objectIDs.add(objectID);
        }
        cursor.close();

        Intent intent = getIntent();
        Uri playerURI = intent.getData();
        int objectID = (int) ContentUris.parseId(playerURI);

        MyApp myApp = (MyApp) getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        if (mainPageSelection == null) {
            Intent nullIntent = new Intent(this, MainActivity.class);
            startActivity(nullIntent);
            finish();
        }
        final int selectionType = mainPageSelection.getType();
        final String leagueName = mainPageSelection.getName();
        final String leagueID = mainPageSelection.getId();
        final int level = mainPageSelection.getLevel();
        setTitle(leagueName);

        ViewPager mViewPager = findViewById(R.id.view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                int id = objectIDs.get(position);
                Uri currentObjectUri = ContentUris.withAppendedId(uri, id);
                switch (objectType) {
                    case 0:
                        return TeamFragment.newInstance(leagueID, selectionType, leagueName, level, currentObjectUri);
                    case 1:
                        return PlayerFragment.newInstance(selectionType, level, currentObjectUri);
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return objectIDs.size();
            }
        });

        for (int i = 0; i < objectIDs.size(); i++) {
            if (objectIDs.get(i) == objectID) {
                mViewPager.setCurrentItem(i);
            }
        }
    }
}
