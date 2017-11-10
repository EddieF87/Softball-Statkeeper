package com.example.android.scorekeepdraft1.activities;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.PlayerStatsAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.List;


public class TeamPageActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

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
        teamSelected = "Free Agent";
        teamNameView = findViewById(R.id.teamName);
        teamRecordView = findViewById(R.id.teamRecord);
        rv = findViewById(R.id.rv_players);
        getLoaderManager().initLoader(EXISTING_TEAM_LOADER, null, this);
    }

    private void initRecyclerView() {
        rv.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        rvAdapter = new PlayerStatsAdapter(players, this);
        rv.setAdapter(rvAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection;
        String[] selectionArgs;
        Uri uri;

        if (waivers) {
            selection = StatsEntry.COLUMN_NAME + "=?";
            selectionArgs = new String[]{"Free Agent"};
            uri = StatsEntry.CONTENT_URI_TEAMS;
        } else {
            selection = null;
            selectionArgs = null;
            uri = mCurrentTeamUri;
        }
        return new CursorLoader(
                this,
                uri,
                null,
                selection,
                selectionArgs,
                null
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int wins = 0;
        int losses = 0;
        int ties = 0;
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            int winColumnIndex = cursor.getColumnIndex(StatsEntry.COLUMN_WINS);
            int lossColumnIndex = cursor.getColumnIndex(StatsEntry.COLUMN_LOSSES);
            int tieColumnIndex = cursor.getColumnIndex(StatsEntry.COLUMN_TIES);

            teamSelected = cursor.getString(nameColumnIndex);
            wins = cursor.getInt(winColumnIndex);
            losses = cursor.getInt(lossColumnIndex);
            ties = cursor.getInt(tieColumnIndex);

            teamRecordView.setText(wins + "-" + losses + "-" + ties);
        }
        int sumG = wins + losses + ties;

        if (waivers) {
            teamSelected = "Free Agent";
            teamNameView.setText(R.string.waivers);
        } else {
            teamNameView.setText(teamSelected);
        }
        setTitle(teamSelected);

        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{teamSelected};
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

        cursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, null, selection, selectionArgs, sortOrder);

        players = new ArrayList<>();

        int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
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
        int idIndex = cursor.getColumnIndex(StatsEntry._ID);

        int sumHr = 0;
        int sumTpl = 0;
        int sumDbl = 0;
        int sumSgl = 0;
        int sumBb = 0;
        int sumOut = 0;
        int sumRbi = 0;
        int sumRun = 0;
        int sumSf = 0;

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {

            String player = cursor.getString(nameIndex);
            int hr = cursor.getInt(hrIndex);
            sumHr += hr;
            int tpl = cursor.getInt(tripleIndex);
            sumTpl += tpl;
            int dbl = cursor.getInt(doubleIndex);
            sumDbl += dbl;
            int sgl = cursor.getInt(singleIndex);
            sumSgl += sgl;
            int bb = cursor.getInt(bbIndex);
            sumBb += bb;
            int out = cursor.getInt(outIndex);
            sumOut += out;
            int rbi = cursor.getInt(rbiIndex);
            sumRbi += rbi;
            int run = cursor.getInt(runIndex);
            sumRun += run;
            int sf = cursor.getInt(sfIndex);
            sumSf += sf;
            int g = cursor.getInt(gameIndex);

            int playerId = cursor.getInt(idIndex);
            players.add(new Player(player, teamSelected, sgl, dbl, tpl, hr, bb, run, rbi, out, sf, g, playerId));
        }
        players.add(new Player("Total", teamSelected, sumSgl, sumDbl, sumTpl, sumHr, sumBb, sumRun, sumRbi, sumOut, sumSf, sumG, 0));

        initRecyclerView();
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

//    public void goToPlayerPage(View v) {
//        Intent intent = new Intent(TeamPageActivity.this, PlayerPageActivity.class);
//        TextView textView = (TextView) v;
//        String player = textView.getText().toString();
//        Bundle b = new Bundle();
//        b.putString("player", player);
//        intent.putExtras(b);
//        startActivity(intent);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_team, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (waivers) {
            menu.findItem(R.id.action_change_name).setVisible(false);
            menu.findItem(R.id.action_edit_photo).setVisible(false);
            menu.findItem(R.id.action_delete_team).setVisible(false);
            menu.findItem(R.id.action_edit_lineup).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_name:
                editNameDialog();
                return true;
            case R.id.action_edit_photo:

                return true;
            case R.id.action_edit_lineup:
                Intent intent = new Intent(TeamPageActivity.this, SetLineupActivity.class);
                Bundle b = new Bundle();
                b.putString("team", teamSelected);
                intent.putExtras(b);
                startActivity(intent);
                return true;
            case R.id.action_remove_players:
                showRemoveAllPlayersDialog();
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
                AlertDialog.Builder choice = new AlertDialog.Builder(TeamPageActivity.this);
                choice.setMessage(R.string.delete_or_freeagency_msg);
                choice.setPositiveButton(R.string.waivers, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updatePlayersTeam("Free Agent");
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

    private void showRemoveAllPlayersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (waivers) {
            builder.setMessage(R.string.remove_all_free_agents);
        } else {
            builder.setMessage(R.string.send_all_to_waivers);
        }
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (waivers) {
                    deletePlayers();
                } else {
                    updatePlayersTeam("Free Agent");
                }
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void editNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.edit_team_name);
        final LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_edit_name, null))
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog dialog1 = (Dialog) dialog;
                        EditText editText = dialog1.findViewById(R.id.username);
                        String enteredTeam = editText.getText().toString();
                        updateTeamName(enteredTeam);
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
    private void deleteTeam() {
        if (mCurrentTeamUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentTeamUri, null, null);

            if (rowsDeleted == 1) {
                Toast.makeText(this, teamSelected + " " + getString(R.string.editor_delete_player_successful),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_team_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void deletePlayers() {
        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{teamSelected};
        getContentResolver().delete(StatsEntry.CONTENT_URI_PLAYERS, selection, selectionArgs);
        getLoaderManager().restartLoader(EXISTING_TEAM_LOADER, null, this);
    }

    private void updateTeamName(String team) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_NAME, team);
        int rowsUpdated = getContentResolver().update(mCurrentTeamUri, contentValues, null, null);
        if (rowsUpdated > 0) {
            updatePlayersTeam(team);
        }
        teamSelected = team;
        getLoaderManager().restartLoader(EXISTING_TEAM_LOADER, null, this);
    }

    public void updatePlayersTeam(String team) {
        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{teamSelected};
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_TEAM, team);
        getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS, contentValues, selection, selectionArgs);
        getLoaderManager().restartLoader(EXISTING_TEAM_LOADER, null, this);
    }
}