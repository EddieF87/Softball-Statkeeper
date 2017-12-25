package com.example.android.scorekeepdraft1.activities;

import android.support.annotation.Nullable;
import android.os.Bundle;

import com.example.android.scorekeepdraft1.data.StatsContract;

public class TeamPagerActivity extends ObjectPagerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startPager(0, StatsContract.StatsEntry.CONTENT_URI_TEAMS);
    }

}