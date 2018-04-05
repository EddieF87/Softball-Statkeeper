package xyz.sleekstats.softball.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.objects.PreviousPlay;

public class PreviousPlaysAdapter extends RecyclerView.Adapter<PreviousPlaysAdapter.PlayViewHolder> {

    private final List<PreviousPlay> mPreviousPlays;
    private final Map<String, String> mPlayerNames;

    public PreviousPlaysAdapter(List<PreviousPlay> mPreviousPlays, Map<String, String> mPlayerNames) {
        this.setHasStableIds(true);
        this.mPreviousPlays = mPreviousPlays;
        this.mPlayerNames = mPlayerNames;
    }

    @Override
    public PlayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prev_play, parent, false);

        return new PlayViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(PlayViewHolder holder, int position) {
        PreviousPlay currentPlay = mPreviousPlays.get(position);

        String batter = mPlayerNames.get(currentPlay.getBatter());
        int awayRuns = currentPlay.getAwayRuns();
        int homeRuns = currentPlay.getHomeRuns();
        int inningNum = currentPlay.getInning();

        if (position == mPreviousPlays.size() - 1) {
            holder.mScoreTextView.setText("0 - 0");
        } else {
            PreviousPlay lastPlay = mPreviousPlays.get(position + 1);
            if (awayRuns != lastPlay.getAwayRuns() || homeRuns != lastPlay.getHomeRuns()) {
                String scoreText = awayRuns + " - " + homeRuns;
                holder.mScoreTextView.setText(scoreText);
            }
        }

        if (inningNum != 0) {
            StringBuilder sb;
            int newInning = (inningNum - 1) / 2;
            if (inningNum % 2 == 0) {
                sb = new StringBuilder("Bottom of the ");
            } else {
                sb = new StringBuilder("Top of the ");
            }
            String indicator;
            switch (newInning) {
                case 1:
                    indicator = "st";
                    break;
                case 2:
                    indicator = "nd";
                    break;
                case 3:
                    indicator = "rd";
                    break;
                default:
                    indicator = "th";
            }
            sb.append(newInning).append(indicator).append(" Inning");
            holder.mInningTextView.setText(sb.toString());
            holder.mInningTextView.setVisibility(View.VISIBLE);

            if (batter == null) {
                String otherTeamResult;
                if (inningNum % 2 == 0) {
                    otherTeamResult = "Home Team scored " + homeRuns + " runs";
                } else {
                    otherTeamResult = "Away Team scored " + awayRuns + " runs";
                }
                holder.mPlayTextView.setText(otherTeamResult);
                return;
            }
        } else {
            holder.mInningTextView.setVisibility(View.GONE);
        }

        String play = currentPlay.getPlay();
        String action = batter + getPlayText(play);
        holder.mPlayTextView.setText(action);

        List<String> runsList = currentPlay.getRunsList();
        StringBuilder stringBuilder = new StringBuilder();
        int runsScored = runsList.size();
        for (int i = 0; i < runsScored; i++) {
            String runner = runsList.get(i);
            stringBuilder.append(runner);
            if (runsScored == 2) {
                if (i == 0) {
                    stringBuilder.append(" & ");
                    continue;
                }
                continue;
            }
            if (runsScored == 3) {
                if (i == 0) {
                    stringBuilder.append(", ");
                    continue;
                }
                if (i == 1) {
                    stringBuilder.append(", & ");
                }
                continue;
            }
            if (runsScored == 4) {
                if (i < 2) {
                    stringBuilder.append(", ");
                    continue;
                }
                if (i == 2) {
                    stringBuilder.append(", & ");
                }
            }
        }
        if (stringBuilder.length() > 0) {
            String runs = stringBuilder.append(" scored").toString();
            holder.mRunsTextView.setText(runs);
            holder.mRunsTextView.setVisibility(View.VISIBLE);
        } else {
            holder.mRunsTextView.setVisibility(View.GONE);
        }

        String first = currentPlay.getFirst();
        String second = currentPlay.getSecond();
        String third = currentPlay.getThird();
        String baseText;
        if (!first.isEmpty()) {
            baseText = "1B: " + first;
            holder.m1BTextView.setText(baseText);
        }
        if (!second.isEmpty()) {
            baseText = "2B: " + second;
            holder.m2BTextView.setText(baseText);
        }
        if (!third.isEmpty()) {
            baseText = "3B: " + third;
            holder.m3BTextView.setText(baseText);
        }
        int outs = currentPlay.getOuts();
        String outsText;
        if (position == mPreviousPlays.size() - 1) {
            outsText = outs + " outs";
            holder.mOutsTextView.setText(outsText);
        } else {
            PreviousPlay lastPlay = mPreviousPlays.get(position + 1);
            if (outs != lastPlay.getOuts()) {
                outsText = outs + " outs";
                holder.mOutsTextView.setText(outsText);
            }
            boolean threeOuts = (
                    (currentPlay.isHomeTeam() && (inningNum % 2 == 1))
                            || (!currentPlay.isHomeTeam() && (inningNum > 0 && inningNum % 2 == 0)));
            if(position != 0) {
                PreviousPlay nextPlay = mPreviousPlays.get(position - 1);
                if(nextPlay.getBatter() == null) {
                    threeOuts = true;
                }
            }
            if (threeOuts) {
                outsText = 3 + " outs";
                holder.mOutsTextView.setText(outsText);
            }
        }
    }

    private String getPlayText(String play) {
        switch (play) {
            case StatsEntry.COLUMN_1B:
                return " hit a single";

            case StatsContract.StatsEntry.COLUMN_2B:
                return " hit a double";

            case StatsEntry.COLUMN_3B:
                return " hit a triple";

            case StatsEntry.COLUMN_HR:
                return " hit a home run";

            case StatsEntry.COLUMN_OUT:
                return " got out";

            case StatsEntry.COLUMN_ERROR:
                return " reached on error";

            case StatsEntry.COLUMN_BB:
                return " walked";

            case StatsEntry.COLUMN_FC:
                return " hit into a fielder's choice";

            case StatsEntry.COLUMN_SF:
                return " hit a sac fly";

            case StatsEntry.COLUMN_SAC_BUNT:
                return " sac bunted";
        }
        return null;
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

        private final TextView mPlayTextView;
        private final TextView mRunsTextView;
        private final TextView mInningTextView;
        private final TextView m1BTextView;
        private final TextView m2BTextView;
        private final TextView m3BTextView;
        private final TextView mScoreTextView;
        private final TextView mOutsTextView;

        public PlayViewHolder(View itemView) {
            super(itemView);
            mPlayTextView = itemView.findViewById(R.id.prev_play_text);
            mRunsTextView = itemView.findViewById(R.id.prev_runs_text);
            mInningTextView = itemView.findViewById(R.id.new_inning_text);
            m1BTextView = itemView.findViewById(R.id.prev_1b);
            m2BTextView = itemView.findViewById(R.id.prev_2b);
            m3BTextView = itemView.findViewById(R.id.prev_3b);
            mScoreTextView = itemView.findViewById(R.id.prev_score);
            mOutsTextView = itemView.findViewById(R.id.prev_outs);
        }
    }

}


