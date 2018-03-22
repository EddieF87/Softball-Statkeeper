package com.example.android.softballstatkeeper.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.android.softballstatkeeper.models.Player;
import com.example.android.softballstatkeeper.R;

import java.util.List;

/**
 * Created by Eddie on 02/09/2017.
 */

public class MatchupAdapter extends RecyclerView.Adapter<MatchupAdapter.ListViewHolder> {

    private List<Player> list;
    private Context mContext;
    private int colorMale;
    private int colorFemale;
    private boolean genderSettingsOff;
    private int currentLineupPosition = -1;

    public MatchupAdapter(List<Player> list, Context context, int genderSorter) {
        this.setHasStableIds(true);
        this.list = list;
        this.mContext = context;
        this.genderSettingsOff = genderSorter == 0;
        if(genderSettingsOff) {
            colorMale = Color.parseColor("#666666");
            colorFemale = Color.parseColor("#666666");
        } else {
            colorMale = ContextCompat.getColor(context, R.color.male);
            colorFemale = ContextCompat.getColor(context, R.color.female);
        }
    }

    public boolean changeColors(boolean genderSettingsOn){
        if (genderSettingsOn) {
            if (!genderSettingsOff) {
                return false;
            }
            colorMale = ContextCompat.getColor(mContext, R.color.male);
            colorFemale = ContextCompat.getColor(mContext, R.color.female);
            genderSettingsOff = false;
        } else {
            if (genderSettingsOff) {
                return false;
            }
            colorMale = Color.parseColor("#666666");
            colorFemale = Color.parseColor("#666666");
            genderSettingsOff = true;
        }
        return true;
    }

    @Override
    public MatchupAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team, parent, false);
        return new ListViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Player player = list.get(position);

        String name = player.getName();
        String newPos = String.valueOf(position + 1);
        String playerName = newPos + ". " + name;
        holder.mTextView.setText(playerName);

        int gender = player.getGender();
        if (position == currentLineupPosition) {
            holder.mTextView.setTypeface(null, Typeface.BOLD);
            if (gender == 0) {
                holder.mTextView.setTextColor(Color.BLUE);
            } else {
                holder.mTextView.setTextColor(Color.MAGENTA);
            }
        } else {
            holder.mTextView.setTypeface(null, Typeface.NORMAL);
            if (gender == 0) {
                holder.mTextView.setTextColor(colorMale);
            } else {
                holder.mTextView.setTextColor(colorFemale);
            }
        }
        holder.mFrameLayout.setTag(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setCurrentLineupPosition(int position) {
        currentLineupPosition = position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        FrameLayout mFrameLayout;
        TextView mTextView;

        private ListViewHolder(View itemView) {
            super(itemView);
            mFrameLayout = (FrameLayout) itemView;
            mTextView = mFrameLayout.findViewById(R.id.team_text);
        }
    }
}
