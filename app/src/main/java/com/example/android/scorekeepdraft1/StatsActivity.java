package com.example.android.scorekeepdraft1;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.data.PlayerStatsContract;
import com.example.android.scorekeepdraft1.data.PlayerStatsContract.PlayerStatsEntry;

public class StatsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private Spinner statSpinner;
    private Spinner teamSpinner;
    private TextView titleView;
    private Cursor mCursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        rv = (RecyclerView) findViewById(R.id.rv_stats);

        String sortOrder = PlayerStatsEntry.COLUMN_HR + " DESC";

        mCursor = getContentResolver().query(PlayerStatsEntry.CONTENT_URI1, null,
                null, null, sortOrder);
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
            int hit = hr + tpl + dbl + sgl;
            int ab = hit + out;
            int pa = hit + out + sf;
            double avg = (double) hit/ab;
            double obp = (double) (hit+bb)/(pa);
            double slg = (double) ((sgl * 1) + (dbl * 2) + (tpl * 3) + (hr * 4)) /ab;
                playerList.add(playerName);
            //List<Player> players;
            }
        }
    }
}
