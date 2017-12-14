package com.example.android.scorekeepdraft1.activities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.fragments.CreateTeamFragment;
import com.example.android.scorekeepdraft1.fragments.TeamFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

import java.util.ArrayList;
import java.util.List;

public class TeamPagerActivity extends ObjectPagerActivity
        implements CreateTeamFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startPager(0, StatsContract.StatsEntry.CONTENT_URI_TEAMS);
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
        }
    }
}