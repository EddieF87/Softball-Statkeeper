package com.example.android.softballstatkeeper.adapters_listeners_etc;

import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.objects.MainPageSelection;

import java.util.List;

/**
 * Created by Eddie on 12/25/2017.
 */

public class InviteListRecyclerViewAdapter extends RecyclerView.Adapter<InviteListRecyclerViewAdapter.InviteViewHolder> {

    private List<MainPageSelection> inviteList;
    private SparseIntArray changes;

    public InviteListRecyclerViewAdapter(List<MainPageSelection> list) {
        super();
        this.inviteList = list;
        this.changes = new SparseIntArray();
    }

    @Override
    public InviteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new InviteViewHolder(view);    }

    @Override
    public void onBindViewHolder(final InviteViewHolder holder, final int position) {
        MainPageSelection mainPageSelection = inviteList.get(position);
        holder.leagueTextView.setText(mainPageSelection.getName());
        final int level = mainPageSelection.getLevel();

        int progress = holder.mSeekBar.getProgress();
        String initialText;
        switch (progress) {
            case 0:
                initialText = "Decline";
                break;
            case 1:
                initialText = "Decline <---> Accept";
                break;
            case 2:
                initialText = "Accept";
                break;
            default:
                initialText = "Error";
        }
        holder.levelTextView.setText(initialText);

        holder.mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int choice, boolean b) {
                String text;
                int newLevel;
                switch (choice) {
                    case 0:
                        text = "Decline";
                        newLevel = 0;
                        break;
                    case 1:
                        text = "Remind me later";
                        newLevel = level;
                        break;
                    case 2:
                        text = "Accept";
                        newLevel = level * -1;
                        break;
                    default:
                        newLevel = level;
                        text = "Error";
                }
                holder.levelTextView.setText(text);
                changes.put(position, newLevel);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public int getItemCount() {
        return inviteList.size();
    }

    class InviteViewHolder extends RecyclerView.ViewHolder {
        private TextView leagueTextView;
        private TextView levelTextView;
        private SeekBar mSeekBar;

        InviteViewHolder(View view) {
            super(view);
            leagueTextView = view.findViewById(R.id.user_email_view);
            mSeekBar = view.findViewById(R.id.user_level_seekbar);
            levelTextView = view.findViewById(R.id.user_level_view);
            mSeekBar.setMax(2);
        }

    }

    public SparseIntArray getChanges() {
        return changes;
    }

    @Override
    public long getItemId(int position) {return position;}
    @Override
    public int getItemViewType(int position) {return position;}
}