package com.example.android.softballstatkeeper.activities;

import android.content.Intent;
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

import com.example.android.softballstatkeeper.MyApp;
import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.adapters.BoxScoreArrayAdapter;
import com.example.android.softballstatkeeper.adapters.BoxScorePlayerCursorAdapter;
import com.example.android.softballstatkeeper.data.StatsContract;
import com.example.android.softballstatkeeper.data.StatsContract.StatsEntry;
import com.example.android.softballstatkeeper.models.InningScore;
import com.example.android.softballstatkeeper.models.MainPageSelection;

import java.util.ArrayList;
import java.util.List;

public class BoxScoreActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int AWAY_LOADER = 7;
    private static final int HOME_LOADER = 8;
    private static final int SCORE_LOADER = 9;
    private BoxScorePlayerCursorAdapter awayAdapter;
    private BoxScorePlayerCursorAdapter homeAdapter;
    private String awayTeamID;
    private String homeTeamID;
    private String awayTeamName;
    private String homeTeamName;
    private String selectionName;
    private String selectionID;
    private int awayTeamRuns;
    private int homeTeamRuns;
    private int totalInnings;
    private int selectionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_score);

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            selectionID = mainPageSelection.getId();
            selectionType = mainPageSelection.getType();
            selectionName = mainPageSelection.getName();
            setTitle(selectionName + " BoxScore");
        } catch (Exception e) {
            Intent intent = new Intent(BoxScoreActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        Bundle b = getIntent().getExtras();
        if (savedInstanceState != null) {
            awayTeamID = savedInstanceState.getString("awayTeamID", null);
            homeTeamID = savedInstanceState.getString("homeTeamID", null);
            awayTeamName = savedInstanceState.getString("awayTeamName");
            homeTeamName = savedInstanceState.getString("homeTeamName");
            totalInnings = savedInstanceState.getInt("totalInnings", 0);
            awayTeamRuns = savedInstanceState.getInt("awayTeamRuns", 0);
            homeTeamRuns = savedInstanceState.getInt("homeTeamRuns", 0);
        } else if (b != null) {
            awayTeamID = b.getString("awayTeamID", null);
            homeTeamID = b.getString("homeTeamID", null);
            awayTeamName = b.getString("awayTeamName");
            homeTeamName = b.getString("homeTeamName");
            totalInnings = b.getInt("totalInnings", 0);
            awayTeamRuns = b.getInt("awayTeamRuns", 0);
            homeTeamRuns = b.getInt("homeTeamRuns", 0);
        }

        TextView header = findViewById(R.id.boxscore_header);
        View awayTitle = findViewById(R.id.away_players_title);
        View homeTitle = findViewById(R.id.home_players_title);
        TextView awayNameView = awayTitle.findViewById(R.id.name_title);
        TextView homeNameView = homeTitle.findViewById(R.id.name_title);
        ListView awayListView = findViewById(R.id.away_players_listview);
        ListView homeListView = findViewById(R.id.home_players_listview);

        String headerString = awayTeamName + " " + awayTeamRuns + "   " + homeTeamName + " " + homeTeamRuns;
        header.setText(headerString);

        if (selectionType == MainPageSelection.TYPE_TEAM) {

            View boxscore = findViewById(R.id.relativelayout_boxscore);
            boxscore.setVisibility(View.GONE);
            LinearLayout homeLayout = findViewById(R.id.linearLayoutHome);
            homeLayout.setVisibility(View.GONE);

            awayNameView.setText(selectionName);

            awayAdapter = new BoxScorePlayerCursorAdapter(this);
            awayListView.setAdapter(awayAdapter);
            getLoaderManager().initLoader(AWAY_LOADER, null, this);
            return;
        }

        awayNameView.setText(awayTeamName);
        homeNameView.setText(homeTeamName);

        awayAdapter = new BoxScorePlayerCursorAdapter(this);
        awayListView.setAdapter(awayAdapter);
        homeAdapter = new BoxScorePlayerCursorAdapter(this);
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
                selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
                if(selectionType == MainPageSelection.TYPE_TEAM){
                    selectionArgs = new String[]{selectionID};
                } else {
                    selectionArgs = new String[]{awayTeamID};
                }
                break;
            case HOME_LOADER:
                uri = StatsEntry.CONTENT_URI_TEMP;
                selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
                selectionArgs = new String[]{homeTeamID};
                break;
            case SCORE_LOADER:
                uri = StatsEntry.CONTENT_URI_GAMELOG;
                selection = null;
                selectionArgs = null;
                break;
            default:
                uri = null;
                selection = null;
                selectionArgs = new String[]{};
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
                int inningChangedCounter = 0;
                int runs = 0;
                int awayRuns = 0;
                int homeRuns;
                int totalAwayRuns = 0;
                int totalHomeRuns = 0;
                data.moveToPosition(-1);
                while (data.moveToNext()) {
                    if (StatsContract.getColumnString(data, StatsEntry.COLUMN_RUN1) != null) {
                        runs++;
                    }
                    if (StatsContract.getColumnString(data, StatsEntry.COLUMN_RUN2) != null) {
                        runs++;
                    }
                    if (StatsContract.getColumnString(data, StatsEntry.COLUMN_RUN3) != null) {
                        runs++;
                    }
                    if (StatsContract.getColumnString(data, StatsEntry.COLUMN_RUN4) != null) {
                        runs++;
                    }

                    if (StatsContract.getColumnInt(data, StatsEntry.COLUMN_INNING_CHANGED) == 1) {
                        inningChangedCounter++;
                        if (inningChangedCounter % 2 == 0) {
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
                if (inningChangedCounter % 2 == 1) {
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
                awayTeamView.setText(awayTeamName);
                homeTeamView.setText(homeTeamName);
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
        if (selectionType == MainPageSelection.TYPE_TEAM) {
            return;
        }
        homeAdapter.swapCursor(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("awayTeamID", awayTeamID);
        outState.putString("homeTeamID", homeTeamID);
        outState.putString("awayTeamName", awayTeamName);
        outState.putString("homeTeamName", homeTeamName);
        outState.putInt("totalInnings", totalInnings);
        outState.putInt("awayTeamRuns", awayTeamRuns);
        outState.putInt("homeTeamRuns", homeTeamRuns);
    }
}
