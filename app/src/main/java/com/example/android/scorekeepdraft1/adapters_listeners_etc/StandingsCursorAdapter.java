package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by Eddie on 04/09/2017.
 */

public class StandingsCursorAdapter extends CursorAdapter {

    private final NumberFormat formatter = new DecimalFormat("#.000");

    public StandingsCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_standings, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor data) {
        if (data.getPosition() % 2 == 1) {
            view.setBackgroundColor(Color.parseColor("#dfdfdf"));
        } else {
            view.setBackgroundColor(Color.WHITE);
        }
        TextView teamV = view.findViewById(R.id.name);
        TextView winV = view.findViewById(R.id.win);
        TextView lossV = view.findViewById(R.id.loss);
        TextView tieV = view.findViewById(R.id.tie);
        TextView winPctV = view.findViewById(R.id.winpct);
        TextView runsFV = view.findViewById(R.id.runsfor);
        TextView runsAV = view.findViewById(R.id.runsagainst);
        TextView runDV = view.findViewById(R.id.rundiff);

        int teamIndex = data.getColumnIndexOrThrow(StatsEntry.COLUMN_NAME);
        int winIndex = data.getColumnIndexOrThrow(StatsEntry.COLUMN_WINS);
        int lossIndex = data.getColumnIndexOrThrow(StatsEntry.COLUMN_LOSSES);
        int tieIndex = data.getColumnIndexOrThrow(StatsEntry.COLUMN_TIES);
        int runsFIndex = data.getColumnIndexOrThrow(StatsEntry.COLUMN_RUNSFOR);
        int runsAIndex = data.getColumnIndexOrThrow(StatsEntry.COLUMN_RUNSAGAINST);

        String team = data.getString(teamIndex);
        int wins = data.getInt(winIndex);
        int losses = data.getInt(lossIndex);
        int ties = data.getInt(tieIndex);
        double winPercentage = (double) wins / (wins + losses);
        if (Double.isNaN(winPercentage)) {
            winPercentage = .000;
        }
        int runsFor = data.getInt(runsFIndex);
        int runsAgainst = data.getInt(runsAIndex);
        int runDifferential = runsFor - runsAgainst;

        teamV.setText(team);
        winV.setText(String.valueOf(wins));
        lossV.setText(String.valueOf(losses));
        tieV.setText(String.valueOf(ties));
        winPctV.setText(String.valueOf(formatter.format(winPercentage)));
        runsFV.setText(String.valueOf(runsFor));
        runsAV.setText(String.valueOf(runsAgainst));
        runDV.setText(String.valueOf(runDifferential));
    }

}
