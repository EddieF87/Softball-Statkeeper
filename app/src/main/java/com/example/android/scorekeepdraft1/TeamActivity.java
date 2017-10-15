package com.example.android.scorekeepdraft1;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.app.LoaderManager;
import android.content.Loader;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.adapters_listeners_etc.PlayerStatsAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.scorekeepdraft1.R.id.sf;
import static com.example.android.scorekeepdraft1.R.id.tpl;

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
        if (waivers) {
            String selection = StatsEntry.COLUMN_NAME + "=?";
            String[] selectionArgs = new String[]{"Waivers"};
            return new CursorLoader(
                    this,
                    StatsEntry.CONTENT_URI_TEAMS,
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
        mCursor.moveToPosition(-1);

        int  sumHr = 0;
        int  sumTpl = 0;
        int  sumDbl = 0;
        int  sumSgl = 0;
        int  sumBb = 0;
        int  sumOut = 0;
        int  sumRbi = 0;
        int  sumRun = 0;
        int  sumSf = 0;
        int  sumG = 0;

        while (mCursor.moveToNext()) {

            String player = mCursor.getString(nameIndex);
            String team = teamSelected;
            int hr = mCursor.getInt(hrIndex);
            sumHr += hr;
            int tpl = mCursor.getInt(tripleIndex);
            sumTpl += tpl;
            int dbl = mCursor.getInt(doubleIndex);
            sumDbl += dbl;
            int sgl = mCursor.getInt(singleIndex);
            sumSgl += sgl;
            int bb = mCursor.getInt(bbIndex);
            sumBb += bb;
            int out = mCursor.getInt(outIndex);
            sumOut += out;
            int rbi = mCursor.getInt(rbiIndex);
            sumRbi += rbi;
            int run = mCursor.getInt(runIndex);
            sumRun += run;
            int sf = mCursor.getInt(sfIndex);
            sumSf += sf;
            int g = mCursor.getInt(gameIndex);
            sumG += g;

            players.add(new Player(player, team, sgl, dbl, tpl, hr, bb, run, rbi, out, sf, g));
        }

        LinearLayout totalLayout = findViewById(R.id.team_stats_totals);
        totalLayout.setBackgroundColor(Color.WHITE);
        TextView nameTotalView = totalLayout.findViewById(R.id.name_title);
        TextView gameTotalView = totalLayout.findViewById(R.id.game_title);
        TextView abTotalView = totalLayout.findViewById(R.id.ab_title);
        TextView hitTotalView = totalLayout.findViewById(R.id.hit_title);
        TextView hrTotalView = totalLayout.findViewById(R.id.hr_title);
        TextView runTotalView = totalLayout.findViewById(R.id.run_title);
        TextView rbiTotalView = totalLayout.findViewById(R.id.rbi_title);
        TextView avgTotalView = totalLayout.findViewById(R.id.avg_title);
        TextView obpTotalView = totalLayout.findViewById(R.id.obp_title);
        TextView slgTotalView = totalLayout.findViewById(R.id.slg_title);
        TextView opsTotalView = totalLayout.findViewById(R.id.ops_title);
        TextView sglTotalView = totalLayout.findViewById(R.id.sgl_title);
        TextView dblTotalView = totalLayout.findViewById(R.id.dbl_title);
        TextView tplTotalView = totalLayout.findViewById(R.id.tpl_title);
        TextView bbTotalView = totalLayout.findViewById(R.id.bb_title);

        int sumH = sumSgl + sumDbl + sumTpl + sumHr;
        int sumAb = sumH + sumOut;
        double totalAVG = getTotalAVG(sumAb, sumH);
        double totalOBP = getTotalOBP(sumAb, sumH, sumBb, sumSf);
        double totalSLG = getTotalSLG(sumAb, sumSgl, sumDbl, sumTpl, sumHr);
        double totalOPS = getTotalOPS(totalOBP, totalSLG);
        NumberFormat formatter = new DecimalFormat("#.000");

        nameTotalView.setText(R.string.total);
        gameTotalView.setText(String.valueOf(sumG));
        abTotalView.setText(String.valueOf(sumAb));
        hitTotalView.setText(String.valueOf(sumH));
        hrTotalView.setText(String.valueOf(sumHr));
        runTotalView.setText(String.valueOf(sumRun));
        rbiTotalView.setText(String.valueOf(sumRbi));
        avgTotalView.setText(String.valueOf(formatter.format(totalAVG)));
        obpTotalView.setText(String.valueOf(formatter.format(totalOBP)));
        slgTotalView.setText(String.valueOf(formatter.format(totalSLG)));
        opsTotalView.setText(String.valueOf(formatter.format(totalOPS)));
        sglTotalView.setText(String.valueOf(sumSgl));
        dblTotalView.setText(String.valueOf(sumDbl));
        tplTotalView.setText(String.valueOf(sumTpl));
        bbTotalView.setText(String.valueOf(sumBb));

        initRecyclerView();
    }
    private double getTotalAVG(int sumAb, int hits) {
        if (sumAb == 0) {return .000;}
            return ((double) hits) / sumAb;
    }

    private double getTotalOBP(int sumAb, int hits, int bb, int sf) {
        if (sumAb + bb == 0) {return .000;}
        return ((double) (hits + bb))
                / (sumAb + bb + sf);
    }

    private double getTotalSLG(int sumAb, int sumSgl, int sumDbl, int sumTpl, int sumHr) {
        if (sumAb == 0) {return .000;}
        return (sumSgl + sumDbl * 2 + sumTpl * 3 + sumHr * 4)
                / ((double) sumAb);
    }

    private double getTotalOPS(double totalOBP, double totalSLG) {
        return totalOBP + totalSLG;
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