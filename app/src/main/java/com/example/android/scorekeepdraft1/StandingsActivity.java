package com.example.android.scorekeepdraft1;

import android.content.CursorLoader;
import android.database.Cursor;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.adapters_listeners_etc.StandingsCursorAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

public class StandingsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private String[] projection = new String[]{"*, (CAST ((" + StatsEntry.COLUMN_WINS + ") AS FLOAT) / (" + StatsEntry.COLUMN_WINS + " + "
            + StatsEntry.COLUMN_LOSSES + ")) AS winpct"};
    private String statToSortBy = "winpct";
    private String sortOrder = null;
    private String[] selectionArgs = new String[] {"ISL"};
    private static final int STANDINGS_LOADER = 3;



    StandingsCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standings);

        ListView listView = (ListView) findViewById(R.id.list);
        mAdapter = new StandingsCursorAdapter(this, null);
        listView.setAdapter(mAdapter);

        TextView title = (TextView) findViewById(R.id.standings_title);
        String titleString = selectionArgs[0] + " Standings";
        title.setText(titleString);
        findViewById(R.id.team_title).setOnClickListener(this);
        findViewById(R.id.wins_title).setOnClickListener(this);
        findViewById(R.id.losses_title).setOnClickListener(this);
        findViewById(R.id.ties_title).setOnClickListener(this);
        findViewById(R.id.winPCT_title).setOnClickListener(this);
        findViewById(R.id.runsFor_title).setOnClickListener(this);
        findViewById(R.id.runsAgainst_title).setOnClickListener(this);
        findViewById(R.id.runDiff_title).setOnClickListener(this);

        getLoaderManager().initLoader(STANDINGS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = StatsEntry.COLUMN_LEAGUE + "=?";

            if (statToSortBy.equals(StatsContract.StatsEntry.COLUMN_NAME)) {
                sortOrder = statToSortBy + " COLLATE NOCASE ASC";
            } else {sortOrder = statToSortBy + " DESC";}

        
        return new CursorLoader(
                this,
                StatsContract.StatsEntry.CONTENT_URI2,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.team_title:
                statToSortBy = StatsEntry.COLUMN_NAME;
                projection = null;
                break;
            case R.id.wins_title:
                statToSortBy = StatsEntry.COLUMN_WINS;
                projection = null;
                break;
            case R.id.losses_title:
                statToSortBy = StatsEntry.COLUMN_LOSSES;
                projection = null;
                break;
            case R.id.ties_title:
                statToSortBy = StatsEntry.COLUMN_TIES;
                projection = null;
                break;
            case R.id.winPCT_title:
                statToSortBy = "winpct";
                projection = new String[]{"*, (CAST ((" + StatsEntry.COLUMN_WINS + ") AS FLOAT) / (" + StatsEntry.COLUMN_WINS + " + "
                        + StatsEntry.COLUMN_LOSSES + ")) AS winpct"};
                break;
            case R.id.runsFor_title:
                statToSortBy = StatsEntry.COLUMN_RUNSFOR;
                projection = null;
                break;
            case R.id.runsAgainst_title:
                statToSortBy = StatsEntry.COLUMN_RUNSAGAINST;
                projection = null;
                break;
            case R.id.runDiff_title:
                statToSortBy = "rundiff";
                projection = new String[]{"*, (" + StatsEntry.COLUMN_RUNSFOR + " - " + StatsEntry.COLUMN_RUNSAGAINST + ") AS rundiff"};
                break;
            default:
                Toast.makeText(StandingsActivity.this, "SOMETHIGN WRONG WITH onClick", Toast.LENGTH_LONG).show();
        }
        getLoaderManager().restartLoader(STANDINGS_LOADER, null, this);
    }
}
