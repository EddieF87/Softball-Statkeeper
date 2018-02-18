package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.LeagueManagerActivity;
import com.example.android.scorekeepdraft1.activities.TeamPagerActivity;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.objects.Player;
import com.example.android.scorekeepdraft1.objects.Team;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * Created by Eddie on 2/15/2018.
 */

public class StandingsAdapter extends RecyclerView.Adapter<StandingsAdapter.StandingsViewHolder> {

    private Context mContext;
    private List<Team> mTeams;
    private final NumberFormat formatter = new DecimalFormat("#.000");

    public StandingsAdapter(List<Team> teams, Context context){
        this.mTeams = teams;
        this.mContext = context;
    }

    @Override
    public StandingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_standings, parent, false);
        return new StandingsViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(StandingsViewHolder holder, int position) {
        if (position % 2 == 1) {
            holder.linearLayout.setBackgroundColor(Color.parseColor("#dfdfdf"));
        } else {
            holder.linearLayout.setBackgroundColor(Color.WHITE);
        }
        Team team = mTeams.get(position);
        holder.bindTeam(team);
        final long id = team.getTeamId();
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TeamPagerActivity.class);
                Uri currentTeamUri = ContentUris.withAppendedId(StatsContract.StatsEntry.CONTENT_URI_TEAMS, id);
                Log.d("xxx", "onItemClick team id = " + id);
                intent.setData(currentTeamUri);
                ((LeagueManagerActivity) mContext).startActivityForResult(intent, 0);
            }
        });
    }



    @Override
    public int getItemCount() {
        return mTeams.size();
    }

    class StandingsViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayout;
        private TextView teamView;
        private TextView winView;
        private TextView lossView;
        private TextView tieView;
        private TextView winPctView;
        private TextView runsView;
        private TextView runsAllowedView;
        private TextView runDiffView;

        public StandingsViewHolder(View view) {
            super(view);
            linearLayout = (LinearLayout) view;
            teamView = view.findViewById(R.id.name);
            winView = view.findViewById(R.id.win);
            lossView = view.findViewById(R.id.loss);
            tieView = view.findViewById(R.id.tie);
            winPctView = view.findViewById(R.id.winpct);
            runsView = view.findViewById(R.id.runsfor);
            runsAllowedView = view.findViewById(R.id.runsagainst);
            runDiffView = view.findViewById(R.id.rundiff);
        }

        public void bindTeam(Team team) {
            String teamName = team.getName();
            int wins = team.getWins();
            int losses = team.getLosses();
            int ties = team.getTies();
            double winPercentage = team.getWinPct();
            if (Double.isNaN(winPercentage)) {
                winPercentage = .000;
            }
            int runsFor = team.getTotalRunsScored();
            int runsAgainst = team.getTotalRunsAllowed();
            int runDifferential = runsFor - runsAgainst;

            teamView.setText(teamName);
            winView.setText(String.valueOf(wins));
            lossView.setText(String.valueOf(losses));
            tieView.setText(String.valueOf(ties));
            winPctView.setText(String.valueOf(formatter.format(winPercentage)));
            runsView.setText(String.valueOf(runsFor));
            runsAllowedView.setText(String.valueOf(runsAgainst));
            runDiffView.setText(String.valueOf(runDifferential));
        }
    }
}
