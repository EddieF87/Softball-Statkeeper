package com.example.android.scorekeepdraft1.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.Player;


public class PlayerPageActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String playerString;
    private NumberFormat formatter = new DecimalFormat("#.000");
    private static final int EXISTING_PLAYER_LOADER = 0;
    private Uri mCurrentPlayerUri;
    private String teamString;
    private String firestoreID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_page);

        Intent intent = getIntent();
        mCurrentPlayerUri = intent.getData();

        getLoaderManager().initLoader(EXISTING_PLAYER_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                mCurrentPlayerUri,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        TextView nameView = findViewById(R.id.player_name);

        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            int teamIndex = cursor.getColumnIndex(StatsEntry.COLUMN_TEAM);
            int hrIndex = cursor.getColumnIndex(StatsEntry.COLUMN_HR);
            int tripleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_3B);
            int doubleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_2B);
            int singleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_1B);
            int bbIndex = cursor.getColumnIndex(StatsEntry.COLUMN_BB);
            int outIndex = cursor.getColumnIndex(StatsEntry.COLUMN_OUT);
            int rbiIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RBI);
            int runIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RUN);
            int sfIndex = cursor.getColumnIndex(StatsEntry.COLUMN_SF);
            int gameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_G);
            int firestoreIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);

            playerString = cursor.getString(nameIndex);
            teamString = cursor.getString(teamIndex);
            int hr = cursor.getInt(hrIndex);
            int tpl = cursor.getInt(tripleIndex);
            int dbl = cursor.getInt(doubleIndex);
            int sgl = cursor.getInt(singleIndex);
            int bb = cursor.getInt(bbIndex);
            int out = cursor.getInt(outIndex);
            int rbi = cursor.getInt(rbiIndex);
            int run = cursor.getInt(runIndex);
            int sf = cursor.getInt(sfIndex);
            int g = cursor.getInt(gameIndex);
            firestoreID = cursor.getString(firestoreIDIndex);

            Player player = new Player(playerString, teamString, sgl, dbl, tpl, hr, bb, run, rbi, out, sf, g, 0, firestoreID);
            TextView hitView = findViewById(R.id.playerboard_hit);
            TextView hrView = findViewById(R.id.player_hr);
            TextView rbiView = findViewById(R.id.player_rbi);
            TextView runView = findViewById(R.id.player_runs);
            TextView avgView = findViewById(R.id.player_avg);
            TextView obpView = findViewById(R.id.playerboard_obp);
            TextView slgView = findViewById(R.id.player_slg);
            TextView opsView = findViewById(R.id.player_ops);
            TextView sglView = findViewById(R.id.playerboard_1b);
            TextView dblView = findViewById(R.id.playerboard_2b);
            TextView tplView = findViewById(R.id.playerboard_3b);
            TextView bbView = findViewById(R.id.playerboard_bb);
            TextView teamView = findViewById(R.id.player_team);


            nameView.setText(player.getName());
            teamView.setText(teamString);
            hitView.setText(String.valueOf(player.getHits()));
            hrView.setText(String.valueOf(hr));
            rbiView.setText(String.valueOf(rbi));
            runView.setText(String.valueOf(run));
            sglView.setText(String.valueOf(sgl));
            dblView.setText(String.valueOf(dbl));
            tplView.setText(String.valueOf(tpl));
            bbView.setText(String.valueOf(bb));
            avgView.setText(String.valueOf(formatter.format(player.getAVG())));
            obpView.setText(String.valueOf(formatter.format(player.getOBP())));
            slgView.setText(String.valueOf(formatter.format(player.getSLG())));
            opsView.setText(String.valueOf(formatter.format(player.getOPS())));

            String title = "Player Bio: " + playerString;
            setTitle(title);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_change_name:
                editNameDialog();
                return true;
            case R.id.action_change_team:
                changeTeamDialog();
                return true;
            case R.id.action_edit_photo:

                return true;
            case R.id.action_delete_player:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePlayer();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void editNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.edit_player_name);
        final LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_edit_name, null))
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog dialog1 = (Dialog) dialog;
                        EditText editText = dialog1.findViewById(R.id.username);
                        String enteredPlayer = editText.getText().toString();
                        if (nameAlreadyInDB(enteredPlayer)) {
                            Toast.makeText(PlayerPageActivity.this, enteredPlayer + " already exists!",
                                    Toast.LENGTH_SHORT).show();
                            editNameDialog();
                        } else {
                            updatePlayerName(enteredPlayer);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void changeTeamDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        Cursor mCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                new String[]{StatsEntry.COLUMN_NAME}, null, null, null);
        ArrayList<String> teams = new ArrayList<>();
        while (mCursor.moveToNext()) {
            int teamNameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            String teamName = mCursor.getString(teamNameIndex);
            teams.add(teamName);
        }
        teams.add(getString(R.string.waivers));
        final CharSequence[] teams_array = teams.toArray(new CharSequence[teams.size()]);
        builder.setTitle(R.string.edit_player_name);
        builder.setItems(teams_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                teamString = teams_array[item].toString();
                if (teamString.equals(getString(R.string.waivers))) {
                    teamString = "Free Agent";
                }
                updatePlayerTeam(teamString);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePlayer() {
        if (mCurrentPlayerUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentPlayerUri, null, null);
            if (rowsDeleted == 1) {
                Toast.makeText(this, playerString + " " + getString(R.string.editor_delete_player_successful), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_player_failed), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void updatePlayerName(String player) {
        playerString = player;
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_NAME, playerString);
        contentValues.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);

        getContentResolver().update(mCurrentPlayerUri, contentValues, null, null);
        getLoaderManager().restartLoader(EXISTING_PLAYER_LOADER, null, this);
    }

    private void updatePlayerTeam(String team) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_TEAM, team);
        contentValues.put(StatsEntry.COLUMN_ORDER, 99);
        contentValues.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);

        getContentResolver().update(mCurrentPlayerUri, contentValues, null, null);
    }

    private boolean nameAlreadyInDB(String playerName) {
        String selection = StatsEntry.COLUMN_NAME + " = '" + playerName + "' COLLATE NOCASE";

        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, null, selection, null, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void goToTeamPage(View v) {
        if (teamString != null) {
            Intent intent = new Intent(PlayerPageActivity.this, TeamPageActivity.class);

            String selection = StatsEntry.COLUMN_NAME + "=?";
            String[] selectionArgs = new String[]{teamString};
            Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                    null, selection, selectionArgs, null);
            if (cursor.moveToFirst()) {
                int playerId = cursor.getInt(cursor.getColumnIndex(StatsEntry._ID));
                Uri teamUri = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_TEAMS, playerId);
                intent.setData(teamUri);
            }
            startActivity(intent);
        } else {
            Log.v("PlayerPageActivity", "Error going to team page");
        }
    }
}
