package com.example.android.scorekeepdraft1;

import android.database.Cursor;
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
import com.example.android.scorekeepdraft1.data.StatsContract.PlayerStatsEntry;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.scorekeepdraft1.R.string.h;

public class StatsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private RecyclerView rv;
    private PlayerStatsAdapter rvAdapter;
    private Spinner statSpinner;
    private Spinner teamSpinner;
    private String statSort;
    private String teamFilter;
    private String[] projection;
    private TextView titleView;
    private Cursor mCursor;
    private List<Player> players;
    private List<String> teams;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        rv = (RecyclerView) findViewById(R.id.rv_stats);

        String sortOrder = PlayerStatsEntry.COLUMN_HR + " DESC";

        mCursor = getContentResolver().query(PlayerStatsEntry.CONTENT_URI1, null,
                null, null, sortOrder);
        players = new ArrayList<>();

        while (mCursor.moveToNext()) {
            int nameIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_NAME);
            int teamIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_ORDER);
            int hrIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_HR);
            int tripleIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_3B);
            int doubleIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_2B);
            int singleIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_1B);
            int bbIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_BB);
            int outIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_OUT);
            int rbiIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_RBI);
            int runIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_RUN);
            int sfIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_SF);

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

            players.add(new Player(player, team, sgl, dbl, tpl, hr, bb, run, rbi, out, sf));
        }
        initRecyclerView();

        mCursor = getContentResolver().query(PlayerStatsEntry.CONTENT_URI2,
                new String[] {PlayerStatsEntry.COLUMN_NAME}, null, null, null);
        teams = new ArrayList<>();
        teams.add("All");
        while (mCursor.moveToNext()) {
            int teamNameIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_NAME);
            String teamName = mCursor.getString(teamNameIndex);
            teams.add(teamName);
        }
        teamSpinner = (Spinner) findViewById(R.id.spinner_stats_teams);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, teams);

        findViewById(R.id.name_title).setOnClickListener(this);
        findViewById(R.id.team_abv_title).setOnClickListener(this);
        findViewById(R.id.hr_title).setOnClickListener(this);
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

        teamSpinner.setAdapter(spinnerArrayAdapter);
        teamSpinner.setOnItemSelectedListener(this);
    }

    private void initRecyclerView() {
        rv.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        rvAdapter = new PlayerStatsAdapter(players);
        rv.setAdapter(rvAdapter);
    }

    public void sortedFilteredQuery() {
        String sortOrder;
        if (statSort != null) {
            if (statSort.equals(PlayerStatsEntry.COLUMN_NAME) || statSort.equals(PlayerStatsEntry.COLUMN_TEAM)) {
                sortOrder = statSort + " COLLATE NOCASE ASC";
            } else {sortOrder = statSort + " DESC";}
        } else {
            sortOrder = PlayerStatsEntry.COLUMN_HR + " DESC";
        }

        String selection;
        String[] selectionArgs;

        if (teamFilter != null && !teamFilter.equals("All")) {
            selection = PlayerStatsEntry.COLUMN_TEAM + "=?";
            selectionArgs = new String[]{teamFilter};
        } else {
            selection = null;
            selectionArgs = null;
        }

        mCursor = getContentResolver().query(PlayerStatsEntry.CONTENT_URI1, projection,
                selection, selectionArgs, sortOrder);
        players = new ArrayList<>();

        while (mCursor.moveToNext()) {
            int nameIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_NAME);
            int teamIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_ORDER);
            int hrIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_HR);
            int tripleIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_3B);
            int doubleIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_2B);
            int singleIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_1B);
            int bbIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_BB);
            int outIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_OUT);
            int rbiIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_RBI);
            int runIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_RUN);
            int sfIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_SF);

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

            players.add(new Player(player, team, sgl, dbl, tpl, hr, bb, run, rbi, out, sf));
        }
        initRecyclerView();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (view != null) {
            TextView textView = (TextView) view;
            teamFilter = textView.getText().toString();
            sortedFilteredQuery();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.name_title:
                statSort = PlayerStatsEntry.COLUMN_NAME;
                projection = null;
                break;
            case R.id.team_abv_title:
                statSort = PlayerStatsEntry.COLUMN_TEAM;
                projection = null;
                break;
            case R.id.hit_title:
                statSort = "hits";
                projection = new String[]{"*, (" + PlayerStatsEntry.COLUMN_1B + " + " + PlayerStatsEntry.COLUMN_2B + " + " +
                        PlayerStatsEntry.COLUMN_3B + " + " + PlayerStatsEntry.COLUMN_HR + ") AS hits"};
                break;
            case R.id.hr_title:
                statSort = PlayerStatsEntry.COLUMN_HR;
                projection = null;
                break;
            case R.id.run_title:
                statSort = PlayerStatsEntry.COLUMN_RUN;
                projection = null;
                break;
            case R.id.rbi_title:
                statSort = PlayerStatsEntry.COLUMN_RBI;
                projection = null;
                break;
            case R.id.avg_title:
                statSort = "avg";
                projection = new String[]{"*, (CAST ((" + PlayerStatsEntry.COLUMN_1B + " + " + PlayerStatsEntry.COLUMN_2B +
                        " + " + PlayerStatsEntry.COLUMN_3B + " + " + PlayerStatsEntry.COLUMN_HR +
                        ") AS FLOAT) / (" + PlayerStatsEntry.COLUMN_1B + " + " + PlayerStatsEntry.COLUMN_2B +
                        " + " + PlayerStatsEntry.COLUMN_3B + " + " + PlayerStatsEntry.COLUMN_HR +
                        " + " + PlayerStatsEntry.COLUMN_OUT + ")) AS avg"};
                break;
            case R.id.obp_title:
                statSort = "obp";
                projection = new String[]{"*, (CAST ((" + PlayerStatsEntry.COLUMN_1B + " + " + PlayerStatsEntry.COLUMN_2B +
                        " + " + PlayerStatsEntry.COLUMN_3B + " + " + PlayerStatsEntry.COLUMN_HR +
                        " + " + PlayerStatsEntry.COLUMN_BB + ") AS FLOAT) / (" + PlayerStatsEntry.COLUMN_1B +
                        " + " + PlayerStatsEntry.COLUMN_2B + " + " + PlayerStatsEntry.COLUMN_3B + " + " +
                        PlayerStatsEntry.COLUMN_HR + " + " + PlayerStatsEntry.COLUMN_OUT + " + " + PlayerStatsEntry.COLUMN_BB +
                        " + " + PlayerStatsEntry.COLUMN_SF + ")) AS obp"};
                break;
            case R.id.slg_title:
                statSort = "slg";
                projection = new String[]{"*, (CAST ((" + PlayerStatsEntry.COLUMN_1B + " + " + PlayerStatsEntry.COLUMN_2B +
                        " * 2 + " + PlayerStatsEntry.COLUMN_3B + " * 3 + " + PlayerStatsEntry.COLUMN_HR +
                        " * 4) AS FLOAT) / (" + PlayerStatsEntry.COLUMN_1B + " + " + PlayerStatsEntry.COLUMN_2B +
                        " + " + PlayerStatsEntry.COLUMN_3B + " + " + PlayerStatsEntry.COLUMN_HR +
                        " + " + PlayerStatsEntry.COLUMN_OUT + ")) AS slg"};
                break;
            case R.id.ops_title:
                statSort = "ops";
                projection = new String[]{"*, (CAST ((" + PlayerStatsEntry.COLUMN_1B + " + " + PlayerStatsEntry.COLUMN_2B +
                        " * 2 + " + PlayerStatsEntry.COLUMN_3B + " * 3 + " + PlayerStatsEntry.COLUMN_HR +
                        " * 4) AS FLOAT) / (" + PlayerStatsEntry.COLUMN_1B + " + " + PlayerStatsEntry.COLUMN_2B +
                        " + " + PlayerStatsEntry.COLUMN_3B + " + " + PlayerStatsEntry.COLUMN_HR +
                        " + " + PlayerStatsEntry.COLUMN_OUT + ") + " + "CAST ((" + PlayerStatsEntry.COLUMN_1B + " + " + PlayerStatsEntry.COLUMN_2B +
                        " + " + PlayerStatsEntry.COLUMN_3B + " + " + PlayerStatsEntry.COLUMN_HR +
                        " + " + PlayerStatsEntry.COLUMN_BB + ") AS FLOAT) / (" + PlayerStatsEntry.COLUMN_1B +
                        " + " + PlayerStatsEntry.COLUMN_2B + " + " + PlayerStatsEntry.COLUMN_3B + " + " +
                        PlayerStatsEntry.COLUMN_HR + " + " + PlayerStatsEntry.COLUMN_OUT + " + " + PlayerStatsEntry.COLUMN_BB +
                        " + " + PlayerStatsEntry.COLUMN_SF + ")) AS ops"};
                break;
            default:
                Toast.makeText(StatsActivity.this, "SOMETHIGN WRONG WITH SPINNER", Toast.LENGTH_LONG).show();
        }
        sortedFilteredQuery();
    }
}