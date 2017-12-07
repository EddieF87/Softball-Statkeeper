package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.objects.StatKeepUser;
import com.example.android.scorekeepdraft1.fragments.UserFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * Created by Eddie on 12/3/2017.
 */

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    private List<StatKeepUser> mUserList;
    private final OnListFragmentInteractionListener mListener;
    private static final String TAG = "UserListAdapter";

    public UserListAdapter(List<StatKeepUser> list, OnListFragmentInteractionListener listener) {
        super();
        this.mUserList = list;
        this.mListener = listener;
        Log.d(TAG, "hoppy UserListAdapter created");
    }

    @Override
    public UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserListViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(final UserListViewHolder holder, int position) {
        final StatKeepUser statKeepUser = mUserList.get(position);
        final String id = statKeepUser.getEmail();

        int level = statKeepUser.getLevel();
        String levelString = getUserLevel(level);

        holder.seekBar.setProgress(level);
        holder.emailView.setText(id);
        holder.levelView.setText(levelString);
        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String levelString = getUserLevel(i);
                statKeepUser.setLevel(i);

                holder.levelView.setText(levelString);
                if (i == 0) {
                    holder.levelView.setTextColor(Color.RED);
                } else {
                    holder.levelView.setTextColor(Color.BLUE);
                }
                if (null != mListener) {
                    mListener.onListFragmentInteraction(id, i);
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

    private String getUserLevel(int i) {
        String level;
        switch (i) {
            case 0:
                level = "remove user";
                break;
            case 1:
                level = "request access";
                break;
            case 2:
                level = "view only";
                break;
            case 3:
                level = "view/manage";
                break;
            case 4:
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
