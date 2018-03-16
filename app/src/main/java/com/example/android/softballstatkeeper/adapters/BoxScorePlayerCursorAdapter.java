package com.example.android.softballstatkeeper.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.data.StatsContract;
import com.example.android.softballstatkeeper.data.StatsContract.StatsEntry;


/**
 * Created by Eddie on 15/10/2017.
 */

public class BoxScorePlayerCursorAdapter extends CursorAdapter {

    public BoxScorePlayerCursorAdapter(Context context) {
        super(context, null, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
         return LayoutInflater.from(context).inflate(R.layout.item_boxscore_player, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String name = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_NAME);
        int pRBI = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RBI);
        int pRun = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUN);
        int p1b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_1B);
        int p2b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_2B);
        int p3b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_3B);
        int pHR = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HR);
        int pOuts = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_OUT);
        int pBB = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_BB);
        int pH = p1b + p2b + p3b + pHR;
        int pAB = pH + pOuts;

        TextView nameV = view.findViewById(R.id.name);
        TextView abV = view.findViewById(R.id.ab);
        TextView rV = view.findViewById(R.id.run);
        TextView hV = view.findViewById(R.id.hit);
        TextView rbiV = view.findViewById(R.id.rbi);
        TextView hrFV = view.findViewById(R.id.hr);
        TextView bbV = view.findViewById(R.id.bb);

        nameV.setText(name);
        abV.setText(String.valueOf(pAB));
        rV.setText(String.valueOf(pRun));
        hV.setText(String.valueOf(pH));
        rbiV.setText(String.valueOf(pRBI));
        hrFV.setText(String.valueOf(pHR));
        bbV.setText(String.valueOf(pBB));
    }
}
