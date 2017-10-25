package com.example.android.scorekeepdraft1;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.adapters_listeners_etc.PlayerStatsAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.id;

public class StatsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener, View.OnClickListener {

    private RecyclerView rv;
    private TextView emptyView;
    private PlayerStatsAdapter rvAdapter;
    private Spinner teamSpinner;
    private String statSort;
    private String teamFilter;
    private String[] projection;
    private TextView colorView;
    private Cursor mCursor;
    private List<Player> players;
    private List<String> teams;
    private static final int STATS_LOADER = 4;
    private HashMap<String, Integer> teamIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        rv = findViewById(R.id.rv_stats);
        emptyView = findViewById(R.id.empty_stats_view);

        findViewById(R.id.name_title).setOnClickListener(this);
        findViewById(R.id.team_abv_title).setOnClickListener(this);
        findViewById(R.id.hr_title).setOnClickListener(this);
        findViewById(R.id.ab_title).setOnClickListener(this);
        findViewById(R.id.hit_title).setOnClickListener(this);
        findViewById(R.id.rbi_title).setOnClickListener(this);
        findViewById(R.id.run_title).setOnClickListener(this);
        findViewById(R.id.avg_title).setOnClickListener(this);
        findViewById(R.id.obp_title).setOnClickListener(this);
        findViewById(R.id.slg_title).setOnClickListener(this);
        findViewById(R.id.ops_title).setOnClickListener(this);
        findViewById(R.id.sgl_title).setOnClickListener(this);
        findViewById(R.id.dbl_title).setOnClickListener(this);
        findViewById(R.id.tpl_title).setOnClickListener(this);
        findViewById(R.id.bb_title).setOnClickListener(this);

        mCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                new String[]{StatsEntry._ID, StatsEntry.COLUMN_NAME}, null, null, null);

        teams = new ArrayList<>();
        teams.add("All Teams");
        teamIDs = new HashMap<>();
        while (mCursor.moveToNext()) {
            int teamNameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            String teamName = mCursor.getString(teamNameIndex);
            int idIndex = mCursor.getColumnIndex(StatsEntry._ID);
            int id = mCursor.getInt(idIndex);
            teamIDs.put(teamName, id);
            teams.add(teamName);
        }
        teams.add("Free Agent");
        teamSpinner = findViewById(R.id.spinner_stats_teams);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_layout, teams);

        teamSpinner.setAdapter(spinnerArrayAdapter);
        teamSpinner.setOnItemSelectedListener(this);
        if (colorView != null) {
            onClick(colorView);
        }
        getLoaderManager().initLoader(STATS_LOADER, null, this);
    }

    private void initRecyclerView() {
        rv.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        rvAdapter = new PlayerStatsAdapter(players, this);
        rv.setAdapter(rvAdapter);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (view != null) {
            TextView textView = (TextView) view;
            teamFilter = textView.getText().toString();
        }
        getLoaderManager().restartLoader(STATS_LOADER, null, this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onClick(View v) {
        if (colorView != null) {
            colorView.setBackgroundColor(Color.parseColor("#7fdfff"));
        }
        TextView textView = (TextView) v;
        textView.setBackgroundColor(Color.parseColor("#0098cc"));
        colorView = textView;

        switch (v.getId()) {
            case R.id.name_title:
                statSort = StatsEntry.COLUMN_NAME;
                projection = null;
                break;
            case R.id.team_abv_title:
                statSort = StatsEntry.COLUMN_TEAM;
                projection = null;
                break;
            case R.id.ab_title:
                statSort = "atbats";
                projection = new String[]{"*, (" + StatsEntry.COLUMN_1B + " + " + StatsEntry.COLUMN_2B + " + " +
                        StatsEntry.COLUMN_3B + " + " + StatsEntry.COLUMN_HR + " + " + StatsEntry.COLUMN_OUT + ") AS atbats"};
                break;
            case R.id.hit_title:
                statSort = "hits";
                projection = new String[]{"*, (" + StatsEntry.COLUMN_1B + " + " + StatsEntry.COLUMN_2B + " + " +
                        StatsEntry.COLUMN_3B + " + " + StatsEntry.COLUMN_HR + ") AS hits"};
                break;
            case R.id.hr_title:
                statSort = StatsEntry.COLUMN_HR;
                projection = null;
                break;
            case R.id.run_title:
                statSort = StatsEntry.COLUMN_RUN;
                projection = null;
                break;
            case R.id.rbi_title:
                statSort = StatsEntry.COLUMN_RBI;
                projection = null;
                break;
            case R.id.avg_title:
                statSort = "avg";
                projection = new String[]{"*, (CAST ((" + StatsEntry.COLUMN_1B + " + " + StatsEntry.COLUMN_2B +
                        " + " + StatsEntry.COLUMN_3B + " + " + StatsEntry.COLUMN_HR +
                        ") AS FLOAT) / (" + StatsEntry.COLUMN_1B + " + " + StatsEntry.COLUMN_2B +
                        " + " + StatsEntry.COLUMN_3B + " + " + StatsEntry.COLUMN_HR +
                        " + " + StatsEntry.COLUMN_OUT + ")) AS avg"};
                break;
            case R.id.obp_title:
                statSort = "obp";
                projection = new String[]{"*, (CAST ((" + StatsEntry.COLUMN_1B + " + " + StatsEntry.COLUMN_2B +
                        " + " + StatsEntry.COLUMN_3B + " + " + StatsEntry.COLUMN_HR +
                        " + " + StatsEntry.COLUMN_BB + ") AS FLOAT) / (" + StatsEntry.COLUMN_1B +
                        " + " + StatsEntry.COLUMN_2B + " + " + StatsEntry.COLUMN_3B + " + " +
                        StatsEntry.COLUMN_HR + " + " + StatsEntry.COLUMN_OUT + " + " + StatsEntry.COLUMN_BB +
                        " + " + StatsEntry.COLUMN_SF + ")) AS obp"};
                break;
            case R.id.slg_title:
                statSort = "slg";
                projection = new String[]{"*, (CAST ((" + StatsEntry.COLUMN_1B + " + " + StatsEntry.COLUMN_2B +
                        " * 2 + " + StatsEntry.COLUMN_3B + " * 3 + " + StatsEntry.COLUMN_HR +
                        " * 4) AS FLOAT) / (" + StatsEntry.COLUMN_1B + " + " + StatsEntry.COLUMN_2B +
                        " + " + StatsEntry.COLUMN_3B + " + " + StatsEntry.COLUMN_HR +
                        " + " + StatsEntry.COLUMN_OUT + ")) AS slg"};
                break;
            case R.id.ops_title:
                statSort = "ops";
                projection = new String[]{"*, (CAST ((" + StatsEntry.COLUMN_1B + " + " + StatsEntry.COLUMN_2B +
                        " * 2 + " + StatsEntry.COLUMN_3B + " * 3 + " + StatsEntry.COLUMN_HR +
                        " * 4) AS FLOAT) / (" + StatsEntry.COLUMN_1B + " + " + StatsEntry.COLUMN_2B +
                        " + " + StatsEntry.COLUMN_3B + " + " + StatsEntry.COLUMN_HR +
                        " + " + StatsEntry.COLUMN_OUT + ") + " + "CAST ((" + StatsEntry.COLUMN_1B + " + " + StatsEntry.COLUMN_2B +
                        " + " + StatsEntry.COLUMN_3B + " + " + StatsEntry.COLUMN_HR +
                        " + " + StatsEntry.COLUMN_BB + ") AS FLOAT) / (" + StatsEntry.COLUMN_1B +
                        " + " + StatsEntry.COLUMN_2B + " + " + StatsEntry.COLUMN_3B + " + " +
                        StatsEntry.COLUMN_HR + " + " + StatsEntry.COLUMN_OUT + " + " + StatsEntry.COLUMN_BB +
                        " + " + StatsEntry.COLUMN_SF + ")) AS ops"};
                break;
            case R.id.sgl_title:
                statSort = StatsEntry.COLUMN_1B;
                projection = null;
                break;
            case R.id.dbl_title:
                statSort = StatsEntry.COLUMN_2B;
                projection = null;
                break;
            case R.id.tpl_title:
                statSort = StatsEntry.COLUMN_3B;
                projection = null;
                break;
            case R.id.bb_title:
                statSort = StatsEntry.COLUMN_BB;
                projection = null;
                break;
            default:
                Toast.makeText(StatsActivity.this, "SOMETHING WRONG WITH onClick", Toast.LENGTH_LONG).show();
        }
        getLoaderManager().restartLoader(STATS_LOADER, null, this);
    }

//    public void goToPlayerPage(View v) {
//        Intent intent = new Intent(StatsActivity.this, PlayerPageActivity.class);
//        TextView textView = (TextView) v;
//        String player = textView.getText().toString();
//        Bundle b = new Bundle();
//        b.putString("player", player);
//        intent.putExtras(b);
//        startActivity(intent);
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder;
        if (statSort != null) {
            if (statSort.equals(StatsEntry.COLUMN_NAME) || statSort.equals(StatsEntry.COLUMN_TEAM)) {
                sortOrder = statSort + " COLLATE NOCASE ASC";
            } else {
                sortOrder = statSort + " DESC";
            }
        } else {
            sortOrder = StatsEntry.COLUMN_NAME + " COLLATE NOCASE ASC";
        }

        String selection;
        String[] selectionArgs;

        if (teamFilter != null && !teamFilter.equals("All Teams")) {
            selection = StatsEntry.COLUMN_TEAM + "=?";
            selectionArgs = new String[]{teamFilter};
        } else {
            selection = null;
            selectionArgs = null;
        }

        return new CursorLoader(
                this,
                StatsContract.StatsEntry.CONTENT_URI_PLAYERS,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        players = new ArrayList<>();
        mCursor = data;
        mCursor.moveToPosition(-1);
        while (mCursor.moveToNext()) {
            int nameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            int teamIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_TEAM);
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
            int idIndex = mCursor.getColumnIndex(StatsEntry._ID);

            String player = mCursor.getString(nameIndex);
            String team = mCursor.getString(teamIndex);
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
            int teamId;
            if(team.equals("Free Agent")) {
                teamId = -1;
            } else {
                teamId = teamIDs.get(team);
            }
            int playerId = mCursor.getInt(idIndex);
            players.add(new Player(player, team, sgl, dbl, tpl, hr, bb, run, rbi, out, sf, g, teamId, playerId));
        }
        if (players.isEmpty()) {
                rv.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
            else {
                rv.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        initRecyclerView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                new String[]{StatsEntry._ID, StatsEntry.COLUMN_NAME}, null, null, null);

        teams = new ArrayList<>();
        teams.add("All Teams");
        teamIDs = new HashMap<>();
        while (mCursor.moveToNext()) {
            int teamNameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            String teamName = mCursor.getString(teamNameIndex);
            int idIndex = mCursor.getColumnIndex(StatsEntry._ID);
            int teamId = mCursor.getInt(idIndex);
            teamIDs.put(teamName, teamId);
            teams.add(teamName);
        }
        teams.add("Free Agent");
        teamSpinner = findViewById(R.id.spinner_stats_teams);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_layout, teams);

        teamSpinner.setAdapter(spinnerArrayAdapter);
        teamSpinner.setOnItemSelectedListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String tempStatSort = statSort;
        if (projection != null) {
            String tempProjection = projection[0];
            outState.putString("tP", tempProjection);
        }
        outState.putString("tSS", tempStatSort);
        if (colorView != null) {
            outState.putInt("tV", colorView.getId());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        statSort = savedInstanceState.getString("tSS");
        String savedProjection = savedInstanceState.getString("tP");
        if (savedProjection == null) {
            projection = null;
        } else {
            projection = new String[]{savedProjection};
        }
        int savedId = savedInstanceState.getInt("tV");
        if (savedId != 0) {
            colorView = findViewById(savedId);
            colorView.setBackgroundColor(Color.parseColor("#0098cc"));
        }
    }
}