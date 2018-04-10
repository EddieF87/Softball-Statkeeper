package xyz.sleekstats.softball.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.PreviousPlaysAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.objects.MainPageSelection;
import xyz.sleekstats.softball.objects.PreviousPlay;

public class PlayRecapFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private PreviousPlaysAdapter mAdapter;
    private List<PreviousPlay> mPreviousPlays;
    private Map<String, String> mPlayerNames;
    private String mStatKeeperID;
    private String awayTeamID;
    private String homeTeamID;
    private int inningNumber;
    private static final int PLAYER_NAMES_LOADER = 5;
    private static final int PLAYS_LOADER = 6;

    public static PlayRecapFragment newInstance(String awayTeamID, String homeTeamID, int inningNumber, String statKeeperID) {
        Bundle args = new Bundle();
        args.putString(StatsEntry.COLUMN_AWAY_TEAM, awayTeamID);
        args.putString(StatsEntry.COLUMN_HOME_TEAM, homeTeamID);
        args.putInt(StatsEntry.INNINGS, inningNumber);
        args.putString(MainPageSelection.KEY_SELECTION_ID, statKeeperID);
        PlayRecapFragment fragment = new PlayRecapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        this.awayTeamID = args.getString(StatsEntry.COLUMN_AWAY_TEAM, null);
        this.homeTeamID = args.getString(StatsEntry.COLUMN_HOME_TEAM, null);
        this.inningNumber = args.getInt(StatsEntry.INNINGS, 0);
        this.mStatKeeperID = args.getString(MainPageSelection.KEY_SELECTION_ID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_play_recap, container, false);
        mRecyclerView = rootView.findViewById(R.id.rv_plays);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        getLoaderManager().initLoader(PLAYER_NAMES_LOADER, null, this);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(PLAYER_NAMES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection;
        String selection;
        String[] selectionArgs;
        String sortOrder;
        Uri uri;
        switch (id) {
            case PLAYER_NAMES_LOADER:
                uri = StatsEntry.CONTENT_URI_PLAYERS;
                if(homeTeamID == null) {
                    selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
                    selectionArgs = new String[]{awayTeamID};
                } else {
                    selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? OR " + StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
                    selectionArgs = new String[]{awayTeamID, homeTeamID, mStatKeeperID};
                }
                projection = new String[]{StatsEntry.COLUMN_FIRESTORE_ID, StatsEntry.COLUMN_NAME};
                sortOrder = null;
                break;
            case PLAYS_LOADER:
                uri = StatsEntry.CONTENT_URI_GAMELOG;
                selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
                selectionArgs = new String[]{mStatKeeperID};
                projection = null;
                sortOrder = StatsEntry._ID + " DESC";
                break;
            default:
                return null;
        }
        return new CursorLoader(getActivity(), uri,
                projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case PLAYER_NAMES_LOADER:
                data.moveToPosition(-1);
                mPlayerNames = new HashMap<>();
                while (data.moveToNext()) {
                    mPlayerNames.put(
                            StatsContract.getColumnString(data, StatsEntry.COLUMN_FIRESTORE_ID),
                            StatsContract.getColumnString(data, StatsEntry.COLUMN_NAME));
                }
                getLoaderManager().initLoader(PLAYS_LOADER, null, this);
                break;

            case PLAYS_LOADER:
                if (mPreviousPlays == null) {
                    mPreviousPlays = new ArrayList<>();
                } else {
                    mPreviousPlays.clear();
                }
                data.moveToLast();
                do{
                    mPreviousPlays.add(new PreviousPlay(data));
                }
                while (data.moveToPrevious());
                mPreviousPlays.remove(mPreviousPlays.size() - 1);

                if(!mPreviousPlays.isEmpty()) {
                    PreviousPlay previousPlay = mPreviousPlays.get(0);
                    if (previousPlay.getInning() == 0) {
                        previousPlay.setInning(inningNumber + 1);
                    }
                }

                if (mAdapter == null) {
                    mAdapter = new PreviousPlaysAdapter(mPreviousPlays, mPlayerNames);
                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset (Loader < Cursor > loader) {

    }
}
