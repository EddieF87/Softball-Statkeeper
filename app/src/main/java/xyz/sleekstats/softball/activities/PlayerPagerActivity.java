package xyz.sleekstats.softball.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.TimeStampUpdater;
import xyz.sleekstats.softball.dialogs.EditGamesPlayedDialog;
import xyz.sleekstats.softball.fragments.PlayerFragment;
import xyz.sleekstats.softball.models.MainPageSelection;

import static xyz.sleekstats.softball.data.FirestoreUpdateService.LEAGUE_COLLECTION;
import static xyz.sleekstats.softball.data.FirestoreUpdateService.PLAYERS_COLLECTION;

public class PlayerPagerActivity extends ObjectPagerActivity
        implements EditGamesPlayedDialog.OnFragmentInteractionListener{

    @Override
    protected void onStart() {
        super.onStart();
        startPager(1, StatsContract.StatsEntry.CONTENT_URI_PLAYERS);
    }

    public void returnDeleteResult(String deletedPlayer) {
        Intent intent = new Intent();
        intent.putExtra(StatsContract.StatsEntry.DELETE, deletedPlayer);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onTeamChosen(String playerID, String teamName, String teamID) {
        super.onTeamChosen(playerID, teamName, teamID);
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }

    public void returnGenderEdit(int gender, String id) {
        Intent intent = new Intent();
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
            Intent intent = new Intent();
            intent.putExtra(StatsContract.StatsEntry.COLUMN_FIRESTORE_ID, playerFirestoreID);
            intent.putExtra(StatsContract.StatsEntry.COLUMN_NAME, enteredText);
            setResult(18, intent);
            if(getSelectionType() == MainPageSelection.TYPE_TEAM) {
                finish();
            }
        }
    }

    @Override
    public void onUpdateGamesPlayed(String playerFirestoreID, int games) {
        final String selectionID = getSelectionID();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firestore
                .collection(LEAGUE_COLLECTION).document(selectionID)
                .collection(PLAYERS_COLLECTION).document(playerFirestoreID);

        final Long time = System.currentTimeMillis();

        WriteBatch writeBatch = firestore.batch();
        writeBatch.update(documentReference, StatsContract.StatsEntry.COLUMN_G, games);
        writeBatch.update(documentReference, StatsContract.StatsEntry.UPDATE, time);
        writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    TimeStampUpdater.updateTimeStamps(PlayerPagerActivity.this, selectionID, time);
                    Toast.makeText(PlayerPagerActivity.this, "Games Played successfully updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PlayerPagerActivity.this, "Error: Failed to update Games Played", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ContentValues values = new ContentValues();
        values.put(StatsContract.StatsEntry.COLUMN_G, games);

        String selection = StatsContract.StatsEntry.COLUMN_LEAGUE_ID + "=?" + " AND "
                + StatsContract.StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = new String[]{selectionID, playerFirestoreID};

        getContentResolver().update(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values, selection, selectionArgs);
    }
}
