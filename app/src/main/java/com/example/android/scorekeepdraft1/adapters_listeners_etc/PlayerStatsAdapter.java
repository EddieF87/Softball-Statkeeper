package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.activities.PlayerPagerActivity;
import com.example.android.scorekeepdraft1.activities.TeamManagerActivity;
import com.example.android.scorekeepdraft1.activities.TeamPagerActivity;
import com.example.android.scorekeepdraft1.objects.Player;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by Eddie on 03/09/2017.
 */

public class PlayerStatsAdapter extends RecyclerView.Adapter<PlayerStatsAdapter.PlayerStatsListViewHolder> {

    private List<Player> players;
    private final NumberFormat formatter = new DecimalFormat("#.000");
    private int visibility;
    private boolean isTeam = false;
    private Context context;

    public PlayerStatsAdapter(List<Player> players, Context context) {
        super();
        this.players = players;
        this.context = context;
        if (context instanceof TeamManagerActivity || context instanceof TeamPagerActivity) {
            visibility = View.GONE;
            isTeam = true;
        } else {
            visibility = View.VISIBLE;
            isTeam = false;
        }
    }

    @Override
    public PlayerStatsAdapter.PlayerStatsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stats, parent, false);
        return new PlayerStatsListViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(PlayerStatsAdapter.PlayerStatsListViewHolder holder, int position) {
        LinearLayout linearLayout = holder.linearLayout;
        final TextView nameView = linearLayout.findViewById(R.id.name);
        final TextView teamView = linearLayout.findViewById(R.id.team_abv);
        TextView abView = linearLayout.findViewById(R.id.ab);
        TextView hitView = linearLayout.findViewById(R.id.hit);
        TextView hrView = linearLayout.findViewById(R.id.hr);
        TextView rbiView = linearLayout.findViewById(R.id.rbi);
        TextView runView = linearLayout.findViewById(R.id.run);
        TextView avgView = linearLayout.findViewById(R.id.avg);
        TextView obpView = linearLayout.findViewById(R.id.obp);
        TextView slgView = linearLayout.findViewById(R.id.slg);
        TextView opsView = linearLayout.findViewById(R.id.ops);
        TextView sglView = linearLayout.findViewById(R.id.sgl);
        TextView dblView = linearLayout.findViewById(R.id.dbl);
        TextView tplView = linearLayout.findViewById(R.id.tpl);
        TextView bbView = linearLayout.findViewById(R.id.bb);
        TextView gameView = linearLayout.findViewById(R.id.game);

        if (position % 2 == 1) {
            linearLayout.setBackgroundColor(Color.parseColor("#dfdfdf"));
        }

        Player player = players.get(position);
        String team = player.getTeam();
        String teamabv;
        if (team == null || team.equals("Free Agent")) {
            teamabv = "FA";
        } else if (team.length() > 2) {
            teamabv = ("" + team.charAt(0) + team.charAt(1) + team.charAt(2)).toUpperCase();
        } else {
            teamabv = ("" + team.charAt(0)).toUpperCase();
        }

        long playerId = player.getPlayerId();
        nameView.setTag(playerId);

        int teamId = player.getTeamId();
        teamView.setTag(teamId);
        if (!isTeam) {
            teamView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, TeamPagerActivity.class);
                    int teamId = (int) teamView.getTag();
                    Uri currentTeamUri = null;
                    if (teamId != -1) {
                        currentTeamUri = ContentUris.withAppendedId(StatsContract.StatsEntry.CONTENT_URI_TEAMS, teamId);
                    }
                    intent.setData(currentTeamUri);
                    startActivity(context, intent, null);
                }
            });
        }
        nameView.setText(player.getName());
        teamView.setText(teamabv);
        int ab = player.getABs();
        int bb = player.getWalks();
        int sf = player.getSacFlies();
        abView.setText(String.valueOf(ab));
        hitView.setText(String.valueOf(player.getHits()));
        hrView.setText(String.valueOf(player.getHrs()));
        rbiView.setText(String.valueOf(player.getRbis()));
        runView.setText(String.valueOf(player.getRuns()));
        sglView.setText(String.valueOf(player.getSingles()));
        dblView.setText(String.valueOf(player.getDoubles()));
        tplView.setText(String.valueOf(player.getTriples()));
        gameView.setText(String.valueOf(player.getGames()));
        bbView.setText(String.valueOf(bb));
        if (ab == 0) {
            avgView.setText("- - -");
            slgView.setText("- - -");
        } else {
            avgView.setText(String.valueOf(formatter.format(player.getAVG())));
            slgView.setText(String.valueOf(formatter.format(player.getSLG())));
        }
        if (ab == 0 && bb == 0 && sf == 0) {
            obpView.setText("- - -");
            opsView.setText("- - -");
        } else {
            obpView.setText(String.valueOf(formatter.format(player.getOBP())));
            opsView.setText(String.valueOf(formatter.format(player.getOPS())));
        }
        linearLayout.setTag(position);
        teamView.setVisibility(visibility);
        if (isTeam && position == players.size() - 1) {
            abView.setTypeface(Typeface.DEFAULT_BOLD);
            hitView.setTypeface(Typeface.DEFAULT_BOLD);
            hrView.setTypeface(Typeface.DEFAULT_BOLD);
            rbiView.setTypeface(Typeface.DEFAULT_BOLD);
            runView.setTypeface(Typeface.DEFAULT_BOLD);
            sglView.setTypeface(Typeface.DEFAULT_BOLD);
            dblView.setTypeface(Typeface.DEFAULT_BOLD);
            tplView.setTypeface(Typeface.DEFAULT_BOLD);
            gameView.setTypeface(Typeface.DEFAULT_BOLD);
            bbView.setTypeface(Typeface.DEFAULT_BOLD);
            avgView.setTypeface(Typeface.DEFAULT_BOLD);
            obpView.setTypeface(Typeface.DEFAULT_BOLD);
            slgView.setTypeface(Typeface.DEFAULT_BOLD);
            opsView.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            nameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PlayerPagerActivity.class);
                    long playerId = (long) nameView.getTag();
                    Uri playerUri = ContentUris.withAppendedId(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, playerId);
                    intent.setData(playerUri);
                    startActivity(context, intent, null);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class PlayerStatsListViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;

        PlayerStatsListViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView;
        }
    }
}
