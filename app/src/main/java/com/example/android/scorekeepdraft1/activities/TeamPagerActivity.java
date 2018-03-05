package com.example.android.scorekeepdraft1.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.adapters_listeners_etc.PlayerStatsAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;

public class TeamPagerActivity extends ObjectPagerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startPager(0, StatsContract.StatsEntry.CONTENT_URI_TEAMS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PlayerStatsAdapter.REQUEST_CODE && resultCode == RESULT_OK) {
                startPager(0, StatsContract.StatsEntry.CONTENT_URI_TEAMS);
            }
        } catch (Exception ex) {
            Toast.makeText(TeamPagerActivity.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void setPagerTitle(String name) {
        super.setPagerTitle(name);
        setTitle(name + ": Teams");
    }
}