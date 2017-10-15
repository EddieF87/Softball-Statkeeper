package com.example.android.scorekeepdraft1;

import android.content.Loader;
import android.database.Cursor;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.adapters_listeners_etc.BoxScorePlayerCursorAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

public class BoxScoreActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int AWAY_LOADER = 7;
    private static final int HOME_LOADER = 8;
    private BoxScorePlayerCursorAdapter awayAdapter;
    private BoxScorePlayerCursorAdapter homeAdapter;
    private String awayTeam;
    private String homeTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_score);
        Bundle b = getIntent().getExtras();
        if (savedInstanceState != null) {
            awayTeam = savedInstanceState.getString("awayTeam");
            homeTeam = savedInstanceState.getString("homeTeam");
        } else if (b != null) {
            awayTeam = b.getString("awayTeam");
            homeTeam = b.getString("homeTeam");
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
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs;
        switch (id) {
            case AWAY_LOADER:
                selectionArgs = new String[] {awayTeam};
                break;
            case HOME_LOADER:
                selectionArgs = new String[] {homeTeam};
                break;
            default:
                selectionArgs = new String[] {};
        }
        return new CursorLoader(this,
                StatsEntry.CONTENT_URI_TEMP,
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
                //TODO team totals
                break;
            case HOME_LOADER:
                homeAdapter.swapCursor(data);
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
        outState.putString("homeTeam", homeTeam);    }
}
