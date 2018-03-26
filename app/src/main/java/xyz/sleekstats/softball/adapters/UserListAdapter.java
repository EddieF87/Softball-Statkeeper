package xyz.sleekstats.softball.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.activities.UsersActivity;
import xyz.sleekstats.softball.models.StatKeepUser;

import java.util.List;

/**
 * Created by Eddie on 12/3/2017.
 */

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    private final List<StatKeepUser> mUserList;
    private final Context mContext;
    private final int mLevel;
    private final AdapterListener mListener;

    public UserListAdapter(List<StatKeepUser> list, Context context, int level) {
        super();
        this.setHasStableIds(true);
        this.mUserList = list;
        this.mContext = context;
        this.mListener = (AdapterListener) mContext;
        this.mLevel = level;
    }

    @Override
    public UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserListViewHolder(linearLayout, mLevel);
    }

    @Override
    public void onBindViewHolder(final UserListViewHolder holder, int position) {
        final StatKeepUser statKeepUser = mUserList.get(position);
        String email = statKeepUser.getEmail();
        int level = statKeepUser.getLevel();
        String levelString = getUserLevel(level);

        holder.emailView.setText(email);
        holder.levelView.setText(levelString);

        if(mLevel >= UsersActivity.LEVEL_ADMIN) {
            if(mLevel == level) {
                holder.seekBar.setVisibility(View.GONE);
                holder.levelView.setTypeface(Typeface.DEFAULT_BOLD);
                return;
            }
            holder.seekBar.setProgress(level);
            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    String levelString = getUserLevel(i);
                    String id = statKeepUser.getId();

                    statKeepUser.setLevel(i);

                    holder.levelView.setText(levelString);
                    if (i == 0) {
                        holder.levelView.setTextColor(Color.RED);
                    } else {
                        holder.levelView.setTextColor(Color.BLUE);
                    }
                    if (mListener != null) {
                        mListener.onUserLevelChanged(id, i);
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
                level = mContext.getString(R.string.remove_user);
                break;
            case 1:
                level = mContext.getString(R.string.view_only);
                break;
            case 2:
                level = mContext.getString(R.string.view_manage);
                break;
            case 3:
                level = mContext.getString(R.string.admin);
                break;
            default:
                level = mContext.getString(R.string.error);
        }
        return level;
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    static class UserListViewHolder extends RecyclerView.ViewHolder {

        final LinearLayout mLinearLayout;
        final TextView emailView;
        final TextView levelView;
        final SeekBar seekBar;

        UserListViewHolder(View itemView, int level) {
            super(itemView);
            mLinearLayout = (LinearLayout) itemView;
            emailView = mLinearLayout.findViewById(R.id.user_email_view);
            levelView = mLinearLayout.findViewById(R.id.user_level_view);
            seekBar = mLinearLayout.findViewById(R.id.user_level_seekbar);
            if (level < UsersActivity.LEVEL_ADMIN) {
                seekBar.setVisibility(View.GONE);
                levelView.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }
    }

    public interface AdapterListener {
        void onUserLevelChanged(String name, int level);
    }
}
