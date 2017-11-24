package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.LeaguePagerActivity;
import com.example.android.scorekeepdraft1.activities.PlayerActivity;
import com.example.android.scorekeepdraft1.activities.TeamActivity;
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
        TextView textView = frameLayout.findViewById(R.id.team_text);
        final MainPageSelection mainPageSelection = mList.get(position);
        String name = mainPageSelection.getName();
        textView.setText(name);
        final int selectionType = mainPageSelection.getType();
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent;
                switch (selectionType) {
                    case MainPageSelection.TYPE_LEAGUE:
                        intent = new Intent(mContext, LeaguePagerActivity.class);
                        break;
                    case MainPageSelection.TYPE_TEAM:
                        intent = new Intent(mContext, TeamActivity.class);
                        break;
                    case MainPageSelection.TYPE_PLAYER:
                        intent = new Intent(mContext, PlayerActivity.class);
                        break;
                    default:
                        return;
                }
                MyApp myApp = (MyApp) mContext.getApplicationContext();
                myApp.setCurrentSelection(mainPageSelection);
                startActivity(mContext, intent, null);
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
