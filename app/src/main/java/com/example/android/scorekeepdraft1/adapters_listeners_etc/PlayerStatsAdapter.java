package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.Player;
import com.example.android.scorekeepdraft1.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import static com.example.android.scorekeepdraft1.R.id.linearLayout;
import static com.example.android.scorekeepdraft1.R.string.team;

/**
 * Created by Eddie on 03/09/2017.
 */

public class PlayerStatsAdapter extends RecyclerView.Adapter<PlayerStatsAdapter.ListViewHolder> {

    private List<Player> players;
    private final NumberFormat formatter = new DecimalFormat("#.000");


    public PlayerStatsAdapter(List<Player> players) {
        super();
        this.players = players;
    }

    @Override
    public PlayerStatsAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stats, parent, false);
        return new ListViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(PlayerStatsAdapter.ListViewHolder holder, int position) {
        LinearLayout linearLayout = holder.linearLayout;
        TextView nameView = (TextView) linearLayout.findViewById(R.id.name);
        TextView teamView = (TextView) linearLayout.findViewById(R.id.team_abv);
        TextView hitView = (TextView) linearLayout.findViewById(R.id.hit);
        TextView hrView = (TextView) linearLayout.findViewById(R.id.hr);
        TextView rbiView = (TextView) linearLayout.findViewById(R.id.rbi);
        TextView runView = (TextView) linearLayout.findViewById(R.id.run);
        TextView avgView = (TextView) linearLayout.findViewById(R.id.avg);
        TextView obpView = (TextView) linearLayout.findViewById(R.id.obp);
        TextView slgView = (TextView) linearLayout.findViewById(R.id.slg);
        TextView opsView = (TextView) linearLayout.findViewById(R.id.ops);
        TextView sglView = (TextView) linearLayout.findViewById(R.id.sgl);
        TextView dblView = (TextView) linearLayout.findViewById(R.id.dbl);
        TextView tplView = (TextView) linearLayout.findViewById(R.id.tpl);
        TextView bbView = (TextView) linearLayout.findViewById(R.id.bb);

        if (position % 2 == 1) {
            nameView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            teamView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            hitView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            hrView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            rbiView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            runView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            avgView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            obpView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            slgView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            opsView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            sglView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            dblView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            tplView.setBackgroundColor(Color.parseColor("#dfdfdf"));
            bbView.setBackgroundColor(Color.parseColor("#dfdfdf"));
        }

        //TODO remove temporary abbr setText and import list<string> teams here to do it automagically
        Player player = players.get(position);
        String team = player.getTeam();
        String teamabv;
        if (team.equals("Purptopes")) {
            teamabv = "PTP";
        } else if (team.equals("Goon Nation")) {
            teamabv = "GN";
        } else if (team.equals("Boogeymen")) {
            teamabv = "BGM";
        } else if (team.equals("Rigtopes")) {
            teamabv = "RIG";
        } else {
            teamabv = "FA";
        }

        nameView.setText(player.getName());
        teamView.setText(teamabv);
        hitView.setText(String.valueOf(player.getHits()));
        hrView.setText(String.valueOf(player.getHrs()));
        rbiView.setText(String.valueOf(player.getRbis()));
        runView.setText(String.valueOf(player.getRuns()));
        avgView.setText(String.valueOf(formatter.format(player.getAVG())));
        obpView.setText(String.valueOf(formatter.format(player.getOBP())));
        slgView.setText(String.valueOf(formatter.format(player.getSLG())));
        opsView.setText(String.valueOf(formatter.format(player.getOPS())));
        sglView.setText(String.valueOf(player.getSingles()));
        dblView.setText(String.valueOf(player.getDoubles()));
        tplView.setText(String.valueOf(player.getTriples()));
        bbView.setText(String.valueOf(player.getWalks()));

        linearLayout.setTag(position);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;

        ListViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView;
        }
    }
}