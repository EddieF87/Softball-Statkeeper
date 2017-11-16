package com.example.android.scorekeepdraft1.activities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.app.LoaderManager;
import android.content.Loader;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.StandingsCursorAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

public class StandingsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private String[] projection = new String[]{"*, (CAST ((" + StatsEntry.COLUMN_WINS + ") AS FLOAT) / (" + StatsEntry.COLUMN_WINS + " + "
            + StatsEntry.COLUMN_LOSSES + ")) AS winpct"};
    private String statToSortBy = "winpct";
    private String sortOrder = null;
    private static final int STANDINGS_LOADER = 3;
    private EditText addTeamText;

    private StandingsCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standings);

        ListView listView = findViewById(R.id.list);
        mAdapter = new StandingsCursorAdapter(this, null);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(StandingsActivity.this, TeamPageActivity.class);
                Uri currentTeamUri = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_TEAMS, id);
                intent.setData(currentTeamUri);
                startActivity(intent);
            }
        });
        View emptyView = findViewById(R.id.empty_text);
        listView.setEmptyView(emptyView);

        TextView title = findViewById(R.id.standings_header);
        MyApp myApp = (MyApp)getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        String titleString = mainPageSelection.getName() + " Standings";
        title.setText(titleString);
        findViewById(R.id.name_title).setOnClickListener(this);
        findViewById(R.id.win_title).setOnClickListener(this);
        findViewById(R.id.loss_title).setOnClickListener(this);
        findViewById(R.id.tie_title).setOnClickListener(this);
        findViewById(R.id.winpct_title).setOnClickListener(this);
        findViewById(R.id.runsfor_title).setOnClickListener(this);
        findViewById(R.id.runsagainst_title).setOnClickListener(this);
        findViewById(R.id.rundiff_title).setOnClickListener(this);

        View addPlayerView = findViewById(R.id.item_team_adder);
        addTeamText = addPlayerView.findViewById(R.id.add_player_text);
        addTeamText.setHint(R.string.add_team);
        Button addPlayerBtn = addPlayerView.findViewById(R.id.add_player_submit);
        addPlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTeam();
            }
        });

        getLoaderManager().initLoader(STANDINGS_LOADER, null, this);
    }

    public void addTeam() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        String teamName = addTeamText.getText().toString();

        ContentValues values = new ContentValues();
        values.put(StatsEntry.COLUMN_NAME, teamName);
        getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);
        addTeamText.setText("");
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (statToSortBy.equals(StatsContract.StatsEntry.COLUMN_NAME)) {
            sortOrder = statToSortBy + " COLLATE NOCASE ASC";
        } else {
            sortOrder = statToSortBy + " DESC";
        }


        return new CursorLoader(
                this,
                StatsContract.StatsEntry.CONTENT_URI_TEAMS,
                projection,
                null,
                null,
                sortOrder
        );
    }

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
            case R.id.name_title:
                statToSortBy = StatsEntry.COLUMN_NAME;
                projection = null;
                break;
            case R.id.win_title:
                statToSortBy = StatsEntry.COLUMN_WINS;
                projection = null;
                break;
            case R.id.loss_title:
                statToSortBy = StatsEntry.COLUMN_LOSSES;
                projection = null;
                break;
            case R.id.tie_title:
                statToSortBy = StatsEntry.COLUMN_TIES;
                projection = null;
                break;
            case R.id.winpct_title:
                statToSortBy = "winpct";
                projection = new String[]{"*, (CAST ((" + StatsEntry.COLUMN_WINS + ") AS FLOAT) / (" + StatsEntry.COLUMN_WINS + " + "
                        + StatsEntry.COLUMN_LOSSES + ")) AS winpct"};
                break;
            case R.id.runsfor_title:
                statToSortBy = StatsEntry.COLUMN_RUNSFOR;
                projection = null;
                break;
            case R.id.runsagainst_title:
                statToSortBy = StatsEntry.COLUMN_RUNSAGAINST;
                projection = null;
                break;
            case R.id.rundiff_title:
                statToSortBy = "rundiff";
                projection = new String[]{"*, (" + StatsEntry.COLUMN_RUNSFOR + " - " + StatsEntry.COLUMN_RUNSAGAINST + ") AS rundiff"};
                break;
            default:
                Toast.makeText(StandingsActivity.this, "SOMETHIGN WRONG WITH onClick", Toast.LENGTH_LONG).show();
        }
        getLoaderManager().restartLoader(STANDINGS_LOADER, null, this);
    }
}
