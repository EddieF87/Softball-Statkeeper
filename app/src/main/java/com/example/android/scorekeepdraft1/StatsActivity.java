package com.example.android.scorekeepdraft1;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.adapters_listeners_etc.PlayerStatsAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract.PlayerStatsEntry;

import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private PlayerStatsAdapter rvAdapter;
    private Spinner statSpinner;
    private Spinner teamSpinner;
    private TextView titleView;
    private Cursor mCursor;
    private List<Player> players;

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

        statSpinner = (Spinner) findViewById(R.id.spinner_stats_sort);
        ArrayList<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("H");
        spinnerArray.add("HR");
        spinnerArray.add("R");
        spinnerArray.add("RBI");
        spinnerArray.add("AVG");
        spinnerArray.add("OBP");
        spinnerArray.add("SLG");
        spinnerArray.add("OPS");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, spinnerArray);

        statSpinner.setAdapter(spinnerArrayAdapter);
    }

    private void initRecyclerView() {
        rv.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        rvAdapter = new PlayerStatsAdapter(players);
        rv.setAdapter(rvAdapter);
    }
}