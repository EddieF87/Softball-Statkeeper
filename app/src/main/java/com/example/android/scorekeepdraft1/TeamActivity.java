package com.example.android.scorekeepdraft1;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.app.LoaderManager;
import android.content.Loader;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.adapters_listeners_etc.PlayerStatsAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static android.drm.DrmStore.DrmObjectType.CONTENT;
import static com.example.android.scorekeepdraft1.R.string.g;

public class TeamActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentTeamUri;
    private static final int EXISTING_TEAM_LOADER = 3;

    private TextView teamNameView;
    private TextView teamRecordView;
    private RecyclerView rv;

    private Cursor mCursor;
    private List<Player> players;
    private String teamSelected;
    private PlayerStatsAdapter rvAdapter;
    private boolean waivers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);
        waivers = false;
        Intent intent = getIntent();
        mCurrentTeamUri = intent.getData();
        if (mCurrentTeamUri == null) {
            waivers = true;
        }

        teamNameView = (TextView) findViewById(R.id.teamName);
        teamRecordView = (TextView) findViewById(R.id.teamRecord);
        rv = (RecyclerView) findViewById(R.id.rv_players);

        getLoaderManager().initLoader(EXISTING_TEAM_LOADER, null, this);
    }

    private void initRecyclerView() {
        rv.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        rvAdapter = new PlayerStatsAdapter(players);
        rv.setAdapter(rvAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (waivers) {
            String selection = StatsEntry.COLUMN_NAME + "=?";
            String[] selectionArgs = new String[]{"Waivers"};
            return new CursorLoader(
                    this,
                    StatsEntry.CONTENT_URI2,
                    null,
                    selection,
                    selectionArgs,
                    null
            );
        } else {
            return new CursorLoader(
                    this,
                    mCurrentTeamUri,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            int winColumnIndex = cursor.getColumnIndex(StatsEntry.COLUMN_WINS);
            int lossColumnIndex = cursor.getColumnIndex(StatsEntry.COLUMN_LOSSES);
            int tieColumnIndex = cursor.getColumnIndex(StatsEntry.COLUMN_TIES);

            // Extract out the value from the Cursor for the given column index
            teamSelected = cursor.getString(nameColumnIndex);
            int wins = cursor.getInt(winColumnIndex);
            int losses = cursor.getInt(lossColumnIndex);
            int ties = cursor.getInt(tieColumnIndex);

            teamRecordView.setText(wins + "-" + losses + "-" + ties);
        }

        String sortOrder;
        if (waivers) {
            teamSelected = "Free Agent";
            sortOrder = StatsEntry.COLUMN_ORDER + " ASC";
            teamNameView.setText("Waivers");
        } else {
            sortOrder = StatsEntry.COLUMN_ORDER + " ASC";
            teamNameView.setText(teamSelected);
        }
        setTitle(teamSelected);

        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{teamSelected};

        mCursor = getContentResolver().query(StatsEntry.CONTENT_URI1, null,
                selection, selectionArgs, sortOrder);
        players = new ArrayList<>();
        mCursor.moveToPosition(-1);
        while (mCursor.moveToNext()) {
            int nameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            int hrIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_HR);
            int tripleIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_3B);
            int doubleIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_2B);
            int singleIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_1B);
            int bbIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_BB);
            int outIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_OUT);
            int rbiIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_RBI);
            int runIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_RUN);
            int sfIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_SF);
            int gameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_G);

            String player = mCursor.getString(nameIndex);
            String team = teamSelected;
            int hr = mCursor.getInt(hrIndex);
            int tpl = mCursor.getInt(tripleIndex);
            int dbl = mCursor.getInt(doubleIndex);
            int sgl = mCursor.getInt(singleIndex);
            int bb = mCursor.getInt(bbIndex);
            int out = mCursor.getInt(outIndex);
            int rbi = mCursor.getInt(rbiIndex);
            int run = mCursor.getInt(runIndex);
            int sf = mCursor.getInt(sfIndex);
            int g = mCursor.getInt(gameIndex);

            players.add(new Player(player, team, sgl, dbl, tpl, hr, bb, run, rbi, out, sf, g));
        }

        initRecyclerView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void goToPlayerPage(View v) {
        Intent intent = new Intent(TeamActivity.this, PlayerPageActivity.class);
        TextView textView = (TextView) v;
        String player = textView.getText().toString();
        Bundle b = new Bundle();
        b.putString("player", player);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_team, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_name:
                //TODO
                return true;
            case R.id.action_edit_photo:

                return true;
            case R.id.action_delete_team:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_team_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                AlertDialog.Builder choice = new AlertDialog.Builder(TeamActivity.this);
                choice.setMessage(R.string.delete_or_freeagency_msg);
                choice.setPositiveButton(R.string.waivers, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updatePlayers();
                        deleteTeam();
                    }
                });
                choice.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deletePlayers();
                        deleteTeam();
                    }
                });
                AlertDialog alertDialog2 = choice.create();
                alertDialog2.show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteTeam() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentTeamUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentTeamUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 1) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, teamSelected + " " + getString(R.string.editor_delete_player_successful),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_team_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void updatePlayers() {
        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{teamSelected};
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_TEAM, "Free Agent");
        getContentResolver().update(StatsEntry.CONTENT_URI1, contentValues, selection, selectionArgs);
    }

    private void deletePlayers() {
        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{teamSelected};
        getContentResolver().delete(StatsEntry.CONTENT_URI1, selection, selectionArgs);
    }
}