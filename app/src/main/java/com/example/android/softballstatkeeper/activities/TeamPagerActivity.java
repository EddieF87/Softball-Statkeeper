package com.example.android.softballstatkeeper.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.android.softballstatkeeper.adapters.PlayerStatsAdapter;
import com.example.android.softballstatkeeper.data.FirestoreHelper;
import com.example.android.softballstatkeeper.data.StatsContract;
import com.example.android.softballstatkeeper.fragments.TeamFragment;

public class TeamPagerActivity extends ObjectPagerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("xyz", "onCreate TeamPagerActivity");
    }

    @Override
    protected void onStart() {
        super.onStart();
        startPager(0, StatsContract.StatsEntry.CONTENT_URI_TEAMS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PlayerStatsAdapter.REQUEST_CODE) {
                if (resultCode == RESULT_OK || resultCode == 17 || resultCode == 18) {
                    startPager(0, StatsContract.StatsEntry.CONTENT_URI_TEAMS);
                }
            }
        } catch (Exception ex) {
            Toast.makeText(TeamPagerActivity.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void setPagerTitle(String name) {
        setTitle(name + ": Teams");
    }

    @Override
    public void onEdit(String enteredText, int type) {
        super.onEdit(enteredText, type);
        if (enteredText.isEmpty()) {
            return;
        }
        boolean update = false;
        TeamFragment teamFragment = getCurrentTeamFragment();
        if (teamFragment != null) {
            update = teamFragment.updateTeamName(enteredText);
        }
        if(update) {
            new FirestoreHelper(this, getSelectionID()).updateTimeStamps();
        }
        setResult(RESULT_OK);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("aaa", "onDestroy TeamPagerActivity");
    }
}