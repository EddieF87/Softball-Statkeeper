package com.example.android.softballstatkeeper.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;

import com.example.android.softballstatkeeper.MyApp;
import com.example.android.softballstatkeeper.data.FirestoreHelper;
import com.example.android.softballstatkeeper.data.StatsContract;
import com.example.android.softballstatkeeper.fragments.PlayerFragment;
import com.example.android.softballstatkeeper.fragments.TeamFragment;
import com.example.android.softballstatkeeper.objects.MainPageSelection;
import com.squareup.leakcanary.RefWatcher;

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

    public void returnGenderEdit(int gender, String id) {
        Intent intent = getIntent();
        intent.putExtra(StatsContract.StatsEntry.COLUMN_GENDER, gender);
        intent.putExtra(StatsContract.StatsEntry.COLUMN_FIRESTORE_ID, id);
        setResult(17, intent);
        if(getSelectionType() == MainPageSelection.TYPE_TEAM) {
            finish();
        }
    }

    @Override
    protected void setPagerTitle(String name) {
        super.setPagerTitle(name);
        setTitle(name + ": Players");
    }

    @Override
    public void onEdit(String enteredText) {
        super.onEdit(enteredText);
        if (enteredText.isEmpty()) {
            return;
        }
        boolean update = false;
        PlayerFragment playerFragment = getCurrentPlayerFragment();
        if (playerFragment != null) {
            update = playerFragment.updatePlayerName(enteredText);
        }
        if(update) {
            new FirestoreHelper(this, getSelectionID()).updateTimeStamps();
            String playerFirestoreID = playerFragment.getFirestoreID();
            Intent intent = getIntent();
            intent.putExtra(StatsContract.StatsEntry.COLUMN_FIRESTORE_ID, playerFirestoreID);
            intent.putExtra(StatsContract.StatsEntry.COLUMN_NAME, enteredText);
            setResult(18, intent);
            if(getSelectionType() == MainPageSelection.TYPE_TEAM) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        Log.d("aaa", "onDestroy PlayerPagerActivity");
    }
}
