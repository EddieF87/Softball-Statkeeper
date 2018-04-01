package xyz.sleekstats.softball.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.List;
import java.util.Map;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.activities.GameRecapActivity;
import xyz.sleekstats.softball.activities.GameRecapListActivity;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.objects.GameRecap;

public class GameRecapRecyclerViewAdapter extends RecyclerView.Adapter<GameRecapRecyclerViewAdapter.RecapViewHolder> {
    private final List<GameRecap> recapList;
    private final Map<String, String> teamNameMap;
    private Context mContext;

    public GameRecapRecyclerViewAdapter(List<GameRecap> list, Map<String, String> map, Context context) {
        this.setHasStableIds(true);
        this.recapList = list;
        this.teamNameMap = map;
        this.mContext = context;
    }

    @Override
    public RecapViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_recap, parent, false);
        return new RecapViewHolder(frameLayout);
    }


    @Override
    public void onBindViewHolder(RecapViewHolder holder, int position) {
        GameRecap gameRecap = recapList.get(position);
        final long gameID = gameRecap.getGameID();
        String dateString = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(gameID);

        final String awayID = gameRecap.getAwayID();
        final String awayTeamName = teamNameMap.get(awayID);
        final String homeID = gameRecap.getHomeID();
        final String homeTeamName = teamNameMap.get(homeID);
        final int awayRuns = gameRecap.getAwayRuns();
        final int homeRuns = gameRecap.getHomeRuns();
        String gameString = dateString + "   "
                + awayTeamName + " " + awayRuns + " @ "
                + homeTeamName + " " + homeRuns;
        holder.mTextView.setText(gameString);
        int local = gameRecap.getLocal();
        if(local == 1) {
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mContext instanceof GameRecapListActivity) {
                        Intent intent = new Intent(mContext, GameRecapActivity.class);
                        intent.putExtra(StatsContract.StatsEntry.COLUMN_GAME_ID, gameID);
                        intent.putExtra(StatsContract.StatsEntry.COLUMN_AWAY_TEAM, awayID);
                        intent.putExtra(StatsContract.StatsEntry.COLUMN_HOME_TEAM, homeID);
                        intent.putExtra("awayname", awayTeamName);
                        intent.putExtra("homename", homeTeamName);
                        intent.putExtra(StatsContract.StatsEntry.COLUMN_AWAY_RUNS, awayRuns);
                        intent.putExtra(StatsContract.StatsEntry.COLUMN_HOME_RUNS, homeRuns);
                        mContext.startActivity(intent);
                    }
                }
            });
            holder.mImageView.setVisibility(View.VISIBLE);
        } else {
            holder.mImageView.setOnClickListener(null);
            holder.mImageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if(recapList == null) {return 0;}
        return recapList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void goToRecap(){

    }

    static class RecapViewHolder extends RecyclerView.ViewHolder {
        final FrameLayout mFrameLayout;
        final TextView mTextView;
        final ImageView mImageView;

        private RecapViewHolder(View itemView) {
            super(itemView);
            mFrameLayout = (FrameLayout) itemView;
            mTextView = mFrameLayout.findViewById(R.id.game_text);
            mImageView = mFrameLayout.findViewById(R.id.img_view_game);
        }
    }
}
