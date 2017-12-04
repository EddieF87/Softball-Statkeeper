package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.SettingsActivity;
import com.example.android.scorekeepdraft1.objects.StatKeepUser;
import com.firebase.ui.auth.User;

import java.util.List;

/**
 * Created by Eddie on 12/3/2017.
 */

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    private Context mContext;
    private List<StatKeepUser> mUserList;

    public UserListAdapter(Context context, List<StatKeepUser> list) {
        super();
        mContext = context;
        mUserList = list;
    }

    @Override
    public UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserListViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(UserListViewHolder holder, int position) {
        final StatKeepUser statKeepUser = mUserList.get(position);
        holder.seekBar.setProgress(3 - statKeepUser.getLevel());
        String level = getLevel(holder.seekBar.getProgress());
        holder.nameView.setText(statKeepUser.getName());
        holder.emailView.setText(statKeepUser.getEmail());
        holder.levelView.setText(level);
        final TextView editLevelView = holder.levelView;
        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                statKeepUser.setLevel(3 - i);
                String level = getLevel(i);
                editLevelView.setText(level);
                if (i == 0) {
                    editLevelView.setTextColor(Color.RED);
                } else {
                    editLevelView.setTextColor(Color.BLUE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private String getLevel(int i) {
        String level;
        switch (i) {
            case 0:
                level = "remove user";
                break;
            case 1:
                level = "view only";
                break;
            case 2:
                level = "view/manage";
                break;
            case 3:
                level = "admin";
                break;
            default:
                level = "error";
        }
        return level;
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    static class UserListViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mLinearLayout;
        TextView nameView;
        TextView emailView;
        TextView levelView;
        SeekBar seekBar;

        UserListViewHolder(View itemView) {
            super(itemView);
            mLinearLayout = (LinearLayout) itemView;
            nameView = mLinearLayout.findViewById(R.id.user_name_view);
            emailView = mLinearLayout.findViewById(R.id.user_email_view);
            levelView = mLinearLayout.findViewById(R.id.user_level_view);
            seekBar = mLinearLayout.findViewById(R.id.user_level_seekbar);
        }

    }
}
