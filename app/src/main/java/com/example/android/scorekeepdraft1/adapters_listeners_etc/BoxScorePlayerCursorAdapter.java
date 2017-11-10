package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;


/**
 * Created by Eddie on 15/10/2017.
 */

public class BoxScorePlayerCursorAdapter extends CursorAdapter {

    public BoxScorePlayerCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
         return LayoutInflater.from(context).inflate(R.layout.item_boxscore_player, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameV = view.findViewById(R.id.name);
        TextView abV = view.findViewById(R.id.ab);
        TextView rV = view.findViewById(R.id.run);
        TextView hV = view.findViewById(R.id.hit);
        TextView rbiV = view.findViewById(R.id.rbi);
        TextView hrFV = view.findViewById(R.id.hr);
        TextView bbV = view.findViewById(R.id.bb);

        int nameIndex = cursor.getColumnIndexOrThrow(StatsContract.StatsEntry.COLUMN_NAME);
        int rIndex = cursor.getColumnIndexOrThrow(StatsEntry.COLUMN_RUN);
        int rbiIndex = cursor.getColumnIndexOrThrow(StatsEntry.COLUMN_RBI);
        int hrIndex = cursor.getColumnIndexOrThrow(StatsEntry.COLUMN_HR);
        int bbIndex = cursor.getColumnIndexOrThrow(StatsEntry.COLUMN_BB);
        int singleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_1B);
        int doubleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_2B);
        int tripleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_3B);
        int outIndex = cursor.getColumnIndex(StatsEntry.COLUMN_OUT);

        String name = cursor.getString(nameIndex);
        int pRBI = cursor.getInt(rbiIndex);
        int pRun = cursor.getInt(rIndex);
        int p1b = cursor.getInt(singleIndex);
        int p2b = cursor.getInt(doubleIndex);
        int p3b = cursor.getInt(tripleIndex);
        int pHR = cursor.getInt(hrIndex);
        int pOuts = cursor.getInt(outIndex);
        int pBB = cursor.getInt(bbIndex);
        int pH = p1b + p2b + p3b + pHR;
        int pAB = pH + pOuts;

        nameV.setText(name);
        abV.setText(String.valueOf(pAB));
        rV.setText(String.valueOf(pRun));
        hV.setText(String.valueOf(pH));
        rbiV.setText(String.valueOf(pRBI));
        hrFV.setText(String.valueOf(pHR));
        bbV.setText(String.valueOf(pBB));
    }
}
