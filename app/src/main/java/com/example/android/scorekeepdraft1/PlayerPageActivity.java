package com.example.android.scorekeepdraft1;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

import static android.R.attr.id;


public class PlayerPageActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String playerString;
    private NumberFormat formatter = new DecimalFormat("#.000");
    private static final int EXISTING_PLAYER_LOADER = 0;
    private Uri mCurrentPlayerUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_page);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            playerString = b.getString("player");
        }
        String title = "Player Bio: " + playerString;
        setTitle(title);
        getLoaderManager().initLoader(EXISTING_PLAYER_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = StatsEntry.COLUMN_NAME + "=?";

        return new CursorLoader(
                this,
                StatsContract.StatsEntry.CONTENT_URI1,
                null,
                selection,
                new String[]{playerString},
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
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

            String playerName = cursor.getString(nameIndex);
            String team = cursor.getString(teamIndex);
            int hr = cursor.getInt(hrIndex);
            int tpl = cursor.getInt(tripleIndex);
            int dbl = cursor.getInt(doubleIndex);
            int sgl = cursor.getInt(singleIndex);
            int bb = cursor.getInt(bbIndex);
            int out = cursor.getInt(outIndex);
            int rbi = cursor.getInt(rbiIndex);
            int run = cursor.getInt(runIndex);
            int sf = cursor.getInt(sfIndex);

            int playerId = cursor.getInt(cursor.getColumnIndex(StatsEntry._ID));
            mCurrentPlayerUri = ContentUris.withAppendedId(StatsEntry.CONTENT_URI1, playerId);

            Player player = new Player(playerName, team, sgl, dbl, tpl, hr, bb, run, rbi, out, sf);
            TextView nameView = (TextView) findViewById(R.id.player_name);
            TextView teamView = (TextView) findViewById(R.id.player_team);
            TextView hitView = (TextView) findViewById(R.id.playerboard_hit);
            TextView hrView = (TextView) findViewById(R.id.player_hr);
            TextView rbiView = (TextView) findViewById(R.id.player_rbi);
            TextView runView = (TextView) findViewById(R.id.player_runs);
            TextView avgView = (TextView) findViewById(R.id.player_avg);
            TextView obpView = (TextView) findViewById(R.id.playerboard_obp);
            TextView slgView = (TextView) findViewById(R.id.player_slg);
            TextView opsView = (TextView) findViewById(R.id.player_ops);
            TextView sglView = (TextView) findViewById(R.id.playerboard_1b);
            TextView dblView = (TextView) findViewById(R.id.playerboard_2b);
            TextView tplView = (TextView) findViewById(R.id.playerboard_3b);
            TextView bbView = (TextView) findViewById(R.id.playerboard_bb);

            nameView.setText(player.getName());
            teamView.setText(team);
            hitView.setText(String.valueOf(player.getHits()));
            hrView.setText(String.valueOf(player.getHrs()));
            rbiView.setText(String.valueOf(player.getRbis()));
            runView.setText(String.valueOf(player.getRuns()));
            avgView.setText(String.valueOf(formatter.format(player.getAVG())));
            obpView.setText(String.valueOf(formatter.format(player.getOBP())));
            slgView.setText(String.valueOf(formatter.format(player.getSLG())));
            opsView.setText(String.valueOf(formatter.format(player.getOPS())));
            sglView.setText(String.valueOf(player.getSingles()));
            dblView.setText(String.valueOf(player.getDoubles()));
            tplView.setText(String.valueOf(player.getTriples()));
            bbView.setText(String.valueOf(player.getWalks()));
        } else {
            finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        String test = "";
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
            // Respond to a click on the "Save" menu option
            case R.id.action_change_name:

                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_change_team:

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
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
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

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePlayer() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentPlayerUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPlayerUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 1) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, playerString + " " + getString(R.string.editor_delete_player_successful),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_player_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
        //finish();
    }
}
