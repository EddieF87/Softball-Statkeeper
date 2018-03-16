package com.example.android.softballstatkeeper.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.softballstatkeeper.models.InningScore;
import com.example.android.softballstatkeeper.R;

import java.util.List;

/**
 * Created by Eddie on 15/10/2017.
 */

public class BoxScoreArrayAdapter extends
        RecyclerView.Adapter<BoxScoreArrayAdapter.BoxScoreListViewHolder> {
    private final List<InningScore> data;

    public BoxScoreArrayAdapter(List<InningScore> list) {
        this.data = list;
    }

    @Override
    public BoxScoreListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_boxscore_score, parent, false);
        return new BoxScoreListViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(BoxScoreListViewHolder holder, int position) {

        holder.numberView.setText(String.valueOf(position + 1));

        int topScore = data.get(position).getTop();
        String topScoreString;
        if(topScore == -1) {
            topScoreString = "-";
        } else {
            topScoreString = String.valueOf(topScore);
        }
        holder.topView.setText(topScoreString);

        int bottomScore = data.get(position).getBottom();
        String bottomScoreString;
        if(bottomScore == -1) {
            bottomScoreString = "-";
        } else {
            bottomScoreString = String.valueOf(bottomScore);
        }
        holder.bottomView.setText(bottomScoreString);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class BoxScoreListViewHolder extends RecyclerView.ViewHolder{
        final LinearLayout linearLayout;
        final TextView numberView;
        final TextView topView;
        final TextView bottomView;

        BoxScoreListViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView;
            numberView = linearLayout.findViewById(R.id.inning_number_row);
            topView = linearLayout.findViewById(R.id.inning_top_row);
            bottomView = linearLayout.findViewById(R.id.inning_bottom_row);}
    }
}
