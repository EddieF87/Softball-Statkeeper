package com.example.android.scorekeepdraft1.adapters_listeners_etc;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.InningScore;
import com.example.android.scorekeepdraft1.R;

import java.util.List;

/**
 * Created by Eddie on 15/10/2017.
 */

public class BoxScoreArrayAdapter extends
        RecyclerView.Adapter<BoxScoreArrayAdapter.BoxScoreListViewHolder> {
    private List<InningScore> data;

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
        LinearLayout linearLayout = holder.linearLayout;
        TextView numberView = linearLayout.findViewById(R.id.inning_number_row);
        TextView topView = linearLayout.findViewById(R.id.inning_top_row);
        TextView bottomView = linearLayout.findViewById(R.id.inning_bottom_row);
        numberView.setText(String.valueOf(position + 1));

        int topScore = data.get(position).getTop();
        String topScoreString;
        if(topScore == -1) {
            topScoreString = "-";
        } else {
            topScoreString = String.valueOf(topScore);
        }
        topView.setText(topScoreString);

        int bottomScore = data.get(position).getBottom();
        String bottomScoreString;
        if(bottomScore == -1) {
            bottomScoreString = "-";
        } else {
            bottomScoreString = String.valueOf(bottomScore);
        }
        bottomView.setText(bottomScoreString);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class BoxScoreListViewHolder extends RecyclerView.ViewHolder{
        LinearLayout linearLayout;
        BoxScoreListViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView;
        }
    }
}
