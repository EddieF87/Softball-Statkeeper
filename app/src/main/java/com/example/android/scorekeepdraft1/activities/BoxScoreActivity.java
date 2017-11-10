package com.example.android.scorekeepdraft1.activities;

import android.content.Loader;
import android.database.Cursor;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.BoxScoreArrayAdapter;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.BoxScorePlayerCursorAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

import java.util.ArrayList;
import java.util.List;

public class BoxScoreActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int AWAY_LOADER = 7;
    private static final int HOME_LOADER = 8;
    private static final int SCORE_LOADER = 9;
    private BoxScorePlayerCursorAdapter awayAdapter;
    private BoxScorePlayerCursorAdapter homeAdapter;
    private String awayTeam;
    private String homeTeam;
    private int totalInnings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_score);
        Bundle b = getIntent().getExtras();
        if (savedInstanceState != null) {
            awayTeam = savedInstanceState.getString("awayTeam");
            homeTeam = savedInstanceState.getString("homeTeam");
            totalInnings = savedInstanceState.getInt("totalInnings", 0);
        } else if (b != null) {
            awayTeam = b.getString("awayTeam");
            homeTeam = b.getString("homeTeam");
            totalInnings = b.getInt("totalInnings", 0);
        }

        View awayTitle = findViewById(R.id.away_players_title);
        View homeTitle = findViewById(R.id.home_players_title);
        TextView awayNameView = awayTitle.findViewById(R.id.name_title);
        TextView homeNameView = homeTitle.findViewById(R.id.name_title);
        awayNameView.setText(awayTeam);
        homeNameView.setText(homeTeam);

        ListView awayListView = findViewById(R.id.away_players_listview);
        ListView homeListView = findViewById(R.id.home_players_listview);
        awayAdapter = new BoxScorePlayerCursorAdapter(this, null);
        homeAdapter = new BoxScorePlayerCursorAdapter(this, null);
        awayListView.setAdapter(awayAdapter);
        homeListView.setAdapter(homeAdapter);
        getLoaderManager().initLoader(AWAY_LOADER, null, this);
        getLoaderManager().initLoader(HOME_LOADER, null, this);
        getLoaderManager().initLoader(SCORE_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection;
        String[] selectionArgs;
        Uri uri;
        switch (id) {
            case AWAY_LOADER:
                uri = StatsEntry.CONTENT_URI_TEMP;
                selection = StatsEntry.COLUMN_TEAM + "=?";
                selectionArgs = new String[] {awayTeam};
                break;
            case HOME_LOADER:
                uri = StatsEntry.CONTENT_URI_TEMP;
                selection = StatsEntry.COLUMN_TEAM + "=?";
                selectionArgs = new String[] {homeTeam};
                break;
            case SCORE_LOADER:
                uri = StatsEntry.CONTENT_URI_GAMELOG;
                selection = null;
                selectionArgs = null;
                break;
            default:
                uri = null;
                selection = null;
                selectionArgs = new String[] {};
        }
        return new CursorLoader(this,
                uri,
                null,
                selection,
                selectionArgs,
                null
        );
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case AWAY_LOADER:
                awayAdapter.swapCursor(data);
                break;
            case HOME_LOADER:
                homeAdapter.swapCursor(data);
                break;
            case SCORE_LOADER:
                List<InningScore> list = new ArrayList<>();
                int runIndex1 = data.getColumnIndex(StatsEntry.COLUMN_RUN1);
                int runIndex2 = data.getColumnIndex(StatsEntry.COLUMN_RUN2);
                int runIndex3 = data.getColumnIndex(StatsEntry.COLUMN_RUN3);
                int runIndex4 = data.getColumnIndex(StatsEntry.COLUMN_RUN4);
                int inningChanged = data.getColumnIndex(StatsEntry.COLUMN_INNING_CHANGED);
                int inningChangedCounter = 0;
                int runs = 0;
                int awayRuns = 0;
                int homeRuns = 0;
                int totalAwayRuns = 0;
                int totalHomeRuns = 0;
                data.moveToPosition(-1);
                while (data.moveToNext()){
                    if(data.getString(runIndex1) != null){runs++;;}
                    if(data.getString(runIndex2) != null){runs++;;}
                    if(data.getString(runIndex3) != null){runs++;;}
                    if(data.getString(runIndex4) != null){runs++;;}

                    if(data.getInt(inningChanged) == 1) {
                        inningChangedCounter++;
                        if(inningChangedCounter % 2 == 0) {
                            homeRuns = runs;
                            totalHomeRuns += homeRuns;
                            list.add(new InningScore(awayRuns, homeRuns));
                        } else {
                            awayRuns = runs;
                            totalAwayRuns += awayRuns;
                        }
                        runs = 0;
                    }
                }
                if(inningChangedCounter % 2 == 1) {
                    list.add(new InningScore(awayRuns, runs));
                    totalHomeRuns += runs;
                } else {
                    list.add(new InningScore(runs, -1));
                    totalAwayRuns += runs;
                }
                for (int i = list.size(); i < totalInnings; i++) {
                    list.add(new InningScore(-1, -1));
                }
                TextView awayTeamView = findViewById(R.id.top_team);
                TextView homeTeamView = findViewById(R.id.bottom_team);
                awayTeamView.setText(awayTeam);
                homeTeamView.setText(homeTeam);
                LinearLayout boxScoreTotal = findViewById(R.id.boxscore_total);
                TextView topTotalView = boxScoreTotal.findViewById(R.id.inning_top_row);
                TextView bottomTotalView = boxScoreTotal.findViewById(R.id.inning_bottom_row);
                TextView titleTotalView = boxScoreTotal.findViewById(R.id.inning_number_row);
                topTotalView.setText(String.valueOf(totalAwayRuns));
                bottomTotalView.setText(String.valueOf(totalHomeRuns));
                topTotalView.setTypeface(null, Typeface.BOLD);
                bottomTotalView.setTypeface(null, Typeface.BOLD);
                titleTotalView.setTypeface(null, Typeface.BOLD);

                BoxScoreArrayAdapter scoreAdapter = new BoxScoreArrayAdapter(list);
                RecyclerView boxScoreGrid = findViewById(R.id.boxscore_grid);
                boxScoreGrid.setLayoutManager(new LinearLayoutManager(
                        this, LinearLayoutManager.HORIZONTAL, false));
                boxScoreGrid.setAdapter(scoreAdapter);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        awayAdapter.swapCursor(null);
        homeAdapter.swapCursor(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("awayTeam", awayTeam);
        outState.putString("homeTeam", homeTeam);
        outState.putInt("totalInnings", totalInnings);
    }
}
