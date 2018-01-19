package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.LeagueManagerActivity;
import com.example.android.scorekeepdraft1.activities.LoadingActivity;
import com.example.android.scorekeepdraft1.activities.PlayerManagerActivity;
import com.example.android.scorekeepdraft1.activities.TeamManagerActivity;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

import java.util.List;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by Eddie on 11/9/2017.
 */

public class MainPageAdapter extends RecyclerView.Adapter<MainPageAdapter.MainPageViewHolder> {


    private List<MainPageSelection> mList;
    private Context mContext;

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
        TextView nameView = frameLayout.findViewById(R.id.team_text);
        final MainPageSelection mainPageSelection = mList.get(position);
        final int selectionType = mainPageSelection.getType();
        final Intent intent;
        String name;

        switch (selectionType) {
            case MainPageSelection.TYPE_LEAGUE:
                intent = new Intent(mContext, LoadingActivity.class);
                name = mainPageSelection.getName() + "  (League)";
                nameView.setTextColor(Color.MAGENTA);
                break;
            case MainPageSelection.TYPE_TEAM:
                intent = new Intent(mContext, LoadingActivity.class);
                name = mainPageSelection.getName() + "  (Team)";
                nameView.setTextColor(Color.BLUE);
                break;
            case MainPageSelection.TYPE_PLAYER:
                intent = new Intent(mContext, PlayerManagerActivity.class);
                name = mainPageSelection.getName() + "  (Player)";
                nameView.setTextColor(Color.RED);
                break;
            default:
                return;
        }

        nameView.setText(name);
        nameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp myApp = (MyApp) mContext.getApplicationContext();
                myApp.setCurrentSelection(mainPageSelection);
                startActivity(mContext, intent, null);
                mList.clear();
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class MainPageViewHolder extends RecyclerView.ViewHolder {
        FrameLayout mFrameLayout;

        private MainPageViewHolder(View itemView) {
            super(itemView);
            mFrameLayout = (FrameLayout) itemView;
        }
    }
}
