package xyz.sleekstats.softball.adapters;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.objects.PreviousPlay;

public class PreviousPlaysAdapter extends RecyclerView.Adapter<PreviousPlaysAdapter.PlayViewHolder> {

    private List<PreviousPlay> mPreviousPlays;
    private Map<String, String> mPlayerNames;

    public PreviousPlaysAdapter(List<PreviousPlay> mPreviousPlays, Map<String, String> mPlayerNames) {
        this.setHasStableIds(true);
        this.mPreviousPlays = mPreviousPlays;
        this.mPlayerNames = mPlayerNames;
    }

    @Override
    public PlayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prev_play, parent, false);

        return new PlayViewHolder(layout);    }

    @Override
    public void onBindViewHolder(PlayViewHolder holder, int position) {
        PreviousPlay previousPlay = mPreviousPlays.get(position);

        String batter = mPlayerNames.get(previousPlay.getBatter());
        String play = previousPlay.getPlay();
        String action = batter + " hit a " + play;
        holder.mPlayTextView.setText(action);

        List<String> runsList = previousPlay.getRunsList();
        StringBuilder stringBuilder = new StringBuilder();
        int runsScored = runsList.size();
        for(int i = 0; i < runsScored; i++){
            String runner = runsList.get(i);
            stringBuilder.append(runner);
            if(runsScored == 2) {
                if(i == 0) {
                    stringBuilder.append(" & ");
                    continue;
                }
                continue;
            }
            if(runsScored == 3) {
                if(i == 0) {
                    stringBuilder.append(", ");
                    continue;
                }
                if(i == 1) {
                    stringBuilder.append(", & ");
                }
                continue;
            }
            if(runsScored == 4) {
                if(i < 2) {
                    stringBuilder.append(", ");
                    continue;
                }
                if(i == 2) {
                    stringBuilder.append(", & ");
                }
            }
        }
        if(stringBuilder.length() > 0) {
            String runs = stringBuilder.append(" scored").toString();
            holder.mRunsTextView.setText(runs);
            holder.mRunsTextView.setVisibility(View.VISIBLE);
        } else{
            holder.mRunsTextView.setVisibility(View.GONE);
        }

        int inningNum = previousPlay.getInning();
        if(inningNum != 0) {
            StringBuilder sb;
            int newInning = (inningNum - 1) / 2;
            if(inningNum % 2 == 0) {
                sb = new StringBuilder("Bottom of the ");
            } else {
                sb = new StringBuilder("Top of the ");
            }
            sb.append(newInning).append(" Inning");
            holder.mInningTextView.setText(sb.toString());
            holder.mInningTextView.setVisibility(View.VISIBLE);
        } else{
            holder.mInningTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mPreviousPlays.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class PlayViewHolder extends RecyclerView.ViewHolder {

        private TextView mPlayTextView;
        private TextView mRunsTextView;
        private TextView mInningTextView;

        public PlayViewHolder(View itemView) {
            super(itemView);
            mPlayTextView = itemView.findViewById(R.id.prev_play_text);
            mRunsTextView = itemView.findViewById(R.id.prev_runs_text);
            mInningTextView = itemView.findViewById(R.id.new_inning_text);
        }
    }

}


