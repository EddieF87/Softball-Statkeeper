package xyz.sleekstats.softball.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.widget.Toast;

import xyz.sleekstats.softball.adapters.PlayerStatsAdapter;
import xyz.sleekstats.softball.data.FirestoreHelper;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.TimeStampUpdater;
import xyz.sleekstats.softball.fragments.TeamFragment;

public class TeamPagerActivity extends ObjectPagerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            TimeStampUpdater.updateTimeStamps(this, getSelectionID());
        }
        setResult(RESULT_OK);
    }

}