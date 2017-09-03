package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.Player;
import com.example.android.scorekeepdraft1.R;

import java.util.List;

/**
 * Created by Eddie on 02/09/2017.
 */

public class TeamListAdapter extends RecyclerView.Adapter<TeamListAdapter.ListViewHolder> {

    private List<String> list;

    public TeamListAdapter(List<String> list) {
        this.list = list;
    }

    @Override
    public TeamListAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team, parent, false);
        return new ListViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        FrameLayout frameLayout = holder.mFrameLayout;
        TextView textView = (TextView) frameLayout.findViewById(R.id.team_text);

        textView.setText((position + 1) + ". " + list.get(position));
        frameLayout.setTag(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        FrameLayout mFrameLayout;

        private ListViewHolder(View itemView) {
            super(itemView);
            mFrameLayout = (FrameLayout) itemView;
        }

    }
}
