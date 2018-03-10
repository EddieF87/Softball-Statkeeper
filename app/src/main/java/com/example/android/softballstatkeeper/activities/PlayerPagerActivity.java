package com.example.android.softballstatkeeper.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;

import com.example.android.softballstatkeeper.data.StatsContract;

public class PlayerPagerActivity extends ObjectPagerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startPager(1, StatsContract.StatsEntry.CONTENT_URI_PLAYERS);
    }

    public void returnDeleteResult(int result, String deletedPlayer) {
        Intent intent = getIntent();
        intent.putExtra(StatsContract.StatsEntry.DELETE, deletedPlayer);
        setResult(result, intent);
        finish();
    }

    @Override
    public void onTeamChosen(String playerID, String teamName, String teamID) {
        super.onTeamChosen(playerID, teamName, teamID);
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
    }

    @Override
    protected void setPagerTitle(String name) {
        super.setPagerTitle(name);
        setTitle(name + ": Players");
    }
}
