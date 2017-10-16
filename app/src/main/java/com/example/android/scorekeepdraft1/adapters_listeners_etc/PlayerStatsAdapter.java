package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.Player;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.TeamActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import static com.example.android.scorekeepdraft1.R.id.linearLayout;
import static com.example.android.scorekeepdraft1.R.string.team;

/**
 * Created by Eddie on 03/09/2017.
 */

public class PlayerStatsAdapter extends RecyclerView.Adapter<PlayerStatsAdapter.PlayerStatsListViewHolder> {

    private List<Player> players;
    private final NumberFormat formatter = new DecimalFormat("#.000");
    private int visibility;

    public PlayerStatsAdapter(List<Player> players, Context context) {
        super();
        this.players = players;
        if (context instanceof TeamActivity){
            visibility = View.GONE;
        } else {
            visibility = View.VISIBLE;
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
        TextView nameView = linearLayout.findViewById(R.id.name);
        TextView teamView = linearLayout.findViewById(R.id.team_abv);
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

        if (position % 2 == 1) {linearLayout.setBackgroundColor(Color.parseColor("#dfdfdf"));}

        Player player = players.get(position);
        String team = player.getTeam();
        String teamabv;
        if (team == null || team.equals("Free Agent")) {
            teamabv = "FA";
        } else if (team.length()>2){
            teamabv = ("" + team.charAt(0) + team.charAt(1) + team.charAt(2)).toUpperCase();
        } else {
            teamabv = ("" + team.charAt(0)).toUpperCase();
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
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class PlayerStatsListViewHolder extends RecyclerView.ViewHolder{
        LinearLayout linearLayout;

        PlayerStatsListViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView;
        }
    }
}