package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.LeagueActivity;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.TeamPageActivity;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

import java.util.List;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by Eddie on 11/9/2017.
 */

public class MainPageAdapter extends RecyclerView.Adapter<MainPageAdapter.MainPageViewHolder> {


    private List<MainPageSelection> mList;
    private Context mContext;
    private static final int LEAGUE = 0;
    private static final int TEAM = 1;
    private static final int PLAYER = 2;

    public MainPageAdapter(List<MainPageSelection> list, Context context) {
        this.mList = list;
        this.mContext = context;
    }

    @Override
    public MainPageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);

        return new MainPageViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(MainPageViewHolder holder, int position) {
        FrameLayout frameLayout = holder.mFrameLayout;
        TextView textView = frameLayout.findViewById(R.id.team_text);
        MainPageSelection mainPageSelection = mList.get(position);
        String name = mainPageSelection.getName();
        textView.setText(name);
        final String id = mainPageSelection.getId();
        final int type = mainPageSelection.getType();
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent;

                if(type == LEAGUE) {
                    intent = new Intent(mContext, LeagueActivity.class);
                } else if (type == TEAM) {
                    intent = new Intent(mContext, LeagueActivity.class);
                } else if (type == PLAYER) {
                    intent = new Intent(mContext, LeagueActivity.class);
                } else {
                    return;
                }
                intent.putExtra("id", id);
                startActivity(mContext, intent, null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class MainPageViewHolder extends RecyclerView.ViewHolder {
        FrameLayout mFrameLayout;

        MainPageViewHolder(View itemView) {
            super(itemView);
            mFrameLayout = (FrameLayout) itemView;
        }
    }
}
