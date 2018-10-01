package xyz.sleekstats.softball.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;


/**
 * Created by Eddie on 15/10/2017.
 */

public class BoxScorePlayerCursorAdapter extends CursorAdapter {

    private final int cursorKey;
    private Map<String, String> playerNames;
    private SimpleDateFormat mDateFormat;
    public static final int KEY_CURRENT = 0;
    public static final int KEY_PLAYER = 1;
    private static final int KEY_RECAP = 2;

    public BoxScorePlayerCursorAdapter(Context context, int key) {
        super(context, null, 0 /* flags */);
        this.cursorKey = key;
        if(cursorKey == KEY_PLAYER) {
            this.mDateFormat = new SimpleDateFormat("MMM dd\nyyyy", Locale.US);
        }
    }

    public BoxScorePlayerCursorAdapter(Context context, Map<String, String> map) {
        super(context, null, 0 /* flags */);
        this.cursorKey = KEY_RECAP;
        this.playerNames = map;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
         return LayoutInflater.from(context).inflate(R.layout.item_boxscore_player, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        if(cursor.getPosition() % 2 == 0) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
        } else {
            view.setBackground(null);
        }
        String name;
        switch (cursorKey) {
            case KEY_CURRENT:
                name = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_NAME);
                break;
            case KEY_RECAP:
                String nameID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
                if (playerNames.containsKey(nameID)) {
                    name = playerNames.get(nameID);
                } else {
                    name = "(Ringer)";
                }
                break;
            default:
                long gameID = StatsContract.getColumnLong(cursor, StatsEntry.COLUMN_GAME_ID);
                name = mDateFormat.format(gameID);
                break;
        }
        int pRBI = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RBI);
        int pRun = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUN);
        int p1b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_1B);
        int p2b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_2B);
        int p3b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_3B);
        int pHR = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HR);
        int pOuts = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_OUT);
        int pBB = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_BB);
        int pSB = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_SB);
        int pH = p1b + p2b + p3b + pHR;
        int pAB = pH + pOuts;

        TextView nameV = view.findViewById(R.id.bs_name);
        TextView abV = view.findViewById(R.id.bs_ab);
        TextView rV = view.findViewById(R.id.bs_run);
        TextView hV = view.findViewById(R.id.bs_hit);
        TextView rbiV = view.findViewById(R.id.bs_rbi);
        TextView hrFV = view.findViewById(R.id.bs_hr);
        TextView bbV = view.findViewById(R.id.bs_bb);
        TextView sglV = view.findViewById(R.id.bs_sgl);
        TextView dblV = view.findViewById(R.id.bs_dbl);
        TextView tplV = view.findViewById(R.id.bs_tpl);
        TextView sbV = view.findViewById(R.id.bs_sb);
        TextView outV = view.findViewById(R.id.bs_out);

        nameV.setText(name);
        abV.setText(String.valueOf(pAB));
        hV.setText(String.valueOf(pH));
        rV.setText(String.valueOf(pRun));
        rbiV.setText(String.valueOf(pRBI));
        hrFV.setText(String.valueOf(pHR));
        bbV.setText(String.valueOf(pBB));
        sglV.setText(String.valueOf(p1b));
        dblV.setText(String.valueOf(p2b));
        tplV.setText(String.valueOf(p3b));
        sbV.setText(String.valueOf(pSB));
        outV.setText(String.valueOf(pOuts));
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
