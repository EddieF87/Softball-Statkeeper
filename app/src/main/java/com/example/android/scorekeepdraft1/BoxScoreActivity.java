package com.example.android.scorekeepdraft1;

import android.content.Loader;
import android.database.Cursor;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.adapters_listeners_etc.BoxScorePlayerCursorAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

public class BoxScoreActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOXSCORE_LOADER = 8;
    private BoxScorePlayerCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_score);

        ListView awayListView = (ListView) findViewById(R.id.away_players_listview);
        ListView homeListView = (ListView) findViewById(R.id.home_players_listview);
        mAdapter = new BoxScorePlayerCursorAdapter(this, null);
        awayListView.setAdapter(mAdapter);
        homeListView.setAdapter(mAdapter);

        getLoaderManager().initLoader(BOXSCORE_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                StatsEntry.CONTENT_URI_TEMP,
                null,
                null,
                null,
                null
        );
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        View awayTitle = findViewById(R.id.away_players_title);
        View homeTitle = findViewById(R.id.home_players_title);
        TextView awayNameView = (TextView) awayTitle.findViewById(R.id.name_title);
        TextView homeNameView = (TextView) homeTitle.findViewById(R.id.name_title);

        data.moveToFirst();
        int teamIndex = data.getColumnIndex(StatsEntry.COLUMN_TEAM);
        String awayName = data.getString(teamIndex);
        awayNameView.setText(awayName);
        data.moveToLast();
        String homeName = data.getString(teamIndex);
        homeNameView.setText(homeName);

        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
