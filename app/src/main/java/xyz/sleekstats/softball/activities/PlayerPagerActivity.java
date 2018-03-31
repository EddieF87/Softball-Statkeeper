package xyz.sleekstats.softball.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;

import xyz.sleekstats.softball.data.FirestoreHelper;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.TimeStampUpdater;
import xyz.sleekstats.softball.fragments.PlayerFragment;
import xyz.sleekstats.softball.objects.MainPageSelection;

public class PlayerPagerActivity extends ObjectPagerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startPager(1, StatsContract.StatsEntry.CONTENT_URI_PLAYERS);
    }

    public void returnDeleteResult(String deletedPlayer) {
        Intent intent = getIntent();
        intent.putExtra(StatsContract.StatsEntry.DELETE, deletedPlayer);
        setResult(android.app.Activity.RESULT_OK, intent);
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
        setTitle(name + ": Players");
    }

    @Override
    public void onEdit(String enteredText, int type) {
        super.onEdit(enteredText, type);
        if (enteredText.isEmpty()) {
            return;
        }
        boolean update = false;
        PlayerFragment playerFragment = getCurrentPlayerFragment();
        if (playerFragment != null) {
            update = playerFragment.updatePlayerName(enteredText);
        }
        if(update) {
            TimeStampUpdater.updateTimeStamps(this, getSelectionID(), System.currentTimeMillis());
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
}
