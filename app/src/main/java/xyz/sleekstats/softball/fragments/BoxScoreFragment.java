package xyz.sleekstats.softball.fragments;


import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.BoxScoreArrayAdapter;
import xyz.sleekstats.softball.adapters.BoxScorePlayerCursorAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.objects.InningScore;
import xyz.sleekstats.softball.objects.MainPageSelection;

/**
 * A simple {@link Fragment} subclass.
 */
public class BoxScoreFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    private static final int AWAY_LOADER = 7;
    private static final int HOME_LOADER = 8;
    private static final int SCORE_LOADER = 9;
    private BoxScorePlayerCursorAdapter awayAdapter;
    private BoxScorePlayerCursorAdapter homeAdapter;
    private String awayTeamID;
    private String homeTeamID;
    private String awayTeamName;
    private String homeTeamName;
    private int awayTeamRuns;
    private int homeTeamRuns;
    private int totalInnings;

    private String selectionID;
    private int selectionType;
    private String selectionName;

    public BoxScoreFragment() {
        // Required empty public constructor
    }

    public static BoxScoreFragment newInstance(String selectionID, String selectionName, int selectionType, String awayTeamID, String homeTeamID,
                                               String awayTeamName, String homeTeamName, int totalInnings,
                                               int awayTeamRuns, int homeTeamRuns) {
        Bundle args = new Bundle();
        BoxScoreFragment fragment = new BoxScoreFragment();
        fragment.setArguments(args);
        args.putString("selectionID", selectionID);
        args.putString("selectionName", selectionName);
        args.putString("awayTeamID", awayTeamID);
        args.putString("homeTeamID", homeTeamID);
        args.putString("awayTeamName", awayTeamName);
        args.putString("homeTeamName", homeTeamName);
        args.putInt("totalInnings", totalInnings);
        args.putInt("awayTeamRuns", awayTeamRuns);
        args.putInt("homeTeamRuns", homeTeamRuns);
        args.putInt("selectionType", selectionType);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        awayTeamID = args.getString("awayTeamID", null);
        homeTeamID = args.getString("homeTeamID", null);
        awayTeamName = args.getString("awayTeamName");
        homeTeamName = args.getString("homeTeamName");
        totalInnings = args.getInt("totalInnings", 0);
        awayTeamRuns = args.getInt("awayTeamRuns", 0);
        homeTeamRuns = args.getInt("homeTeamRuns", 0);
        selectionType = args.getInt("selectionType", 0);
        selectionName = args.getString("selectionName");
        selectionID = args.getString("selectionID");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_boxscore, container, false);

        TextView header = rootView.findViewById(R.id.boxscore_header);
        View awayTitle = rootView.findViewById(R.id.away_players_title);
        View homeTitle = rootView.findViewById(R.id.home_players_title);
        TextView awayNameView = awayTitle.findViewById(R.id.bs_name_title);
        TextView homeNameView = homeTitle.findViewById(R.id.bs_name_title);
        ListView awayListView = rootView.findViewById(R.id.away_players_listview);
        ListView homeListView = rootView.findViewById(R.id.home_players_listview);

        String headerString = awayTeamName + " " + awayTeamRuns + "   " + homeTeamName + " " + homeTeamRuns;
        header.setText(headerString);

        if (selectionType == MainPageSelection.TYPE_TEAM) {

            View boxscore = rootView.findViewById(R.id.relativelayout_boxscore);
            boxscore.setVisibility(View.GONE);
            LinearLayout homeLayout = rootView.findViewById(R.id.linearLayoutHome);
            homeLayout.setVisibility(View.GONE);

            awayNameView.setText(selectionName);

            awayAdapter = new BoxScorePlayerCursorAdapter(getActivity(), BoxScorePlayerCursorAdapter.KEY_CURRENT);
            awayListView.setAdapter(awayAdapter);
            getLoaderManager().initLoader(AWAY_LOADER, null, this);
            return rootView;
        }

        awayNameView.setText(awayTeamName);
        homeNameView.setText(homeTeamName);

        awayAdapter = new BoxScorePlayerCursorAdapter(getActivity(), BoxScorePlayerCursorAdapter.KEY_CURRENT);
        awayListView.setAdapter(awayAdapter);
        homeAdapter = new BoxScorePlayerCursorAdapter(getActivity(), BoxScorePlayerCursorAdapter.KEY_CURRENT);
        homeListView.setAdapter(homeAdapter);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(AWAY_LOADER, null, this);
        if (selectionType == MainPageSelection.TYPE_LEAGUE) {
            getLoaderManager().initLoader(HOME_LOADER, null, this);
            getLoaderManager().initLoader(SCORE_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection;
        String[] selectionArgs;
        Uri uri;
        switch (id) {
            case AWAY_LOADER:
                uri = StatsEntry.CONTENT_URI_TEMP;
                selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
                if(selectionType == MainPageSelection.TYPE_TEAM){
                    selectionArgs = new String[]{selectionID, selectionID};
                } else {
                    selectionArgs = new String[]{awayTeamID, selectionID};
                }
                break;
            case HOME_LOADER:
                uri = StatsEntry.CONTENT_URI_TEMP;
                selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
                selectionArgs = new String[]{homeTeamID, selectionID};
                break;
            case SCORE_LOADER:
                uri = StatsEntry.CONTENT_URI_GAMELOG;
                selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
                selectionArgs = new String[]{selectionID};
                break;
            default:
                uri = null;
                selection = null;
                selectionArgs = null;
        }
        return new CursorLoader(getActivity(),
                uri,
                null,
                selection,
                selectionArgs,
                null
        );
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case AWAY_LOADER:
                awayAdapter.swapCursor(data);
                break;
            case HOME_LOADER:
                homeAdapter.swapCursor(data);
                break;
            case SCORE_LOADER:
                List<InningScore> list = new ArrayList<>();
                int inningChangedCounter = 0;
                int runs = 0;
                int awayRuns = 0;
                int homeRuns;
                int totalAwayRuns = 0;
                int totalHomeRuns = 0;
                data.moveToPosition(-1);
                while (data.moveToNext()) {
                    if (StatsContract.getColumnString(data, StatsEntry.COLUMN_RUN1) != null) {
                        runs++;
                    }
                    if (StatsContract.getColumnString(data, StatsEntry.COLUMN_RUN2) != null) {
                        runs++;
                    }
                    if (StatsContract.getColumnString(data, StatsEntry.COLUMN_RUN3) != null) {
                        runs++;
                    }
                    if (StatsContract.getColumnString(data, StatsEntry.COLUMN_RUN4) != null) {
                        runs++;
                    }

                    if (StatsContract.getColumnInt(data, StatsEntry.COLUMN_INNING_CHANGED) == 1) {
                        inningChangedCounter++;
                        if (inningChangedCounter % 2 == 0) {
                            homeRuns = runs;
                            totalHomeRuns += homeRuns;
                            list.add(new InningScore(awayRuns, homeRuns));
                        } else {
                            awayRuns = runs;
                            totalAwayRuns += awayRuns;
                        }
                        runs = 0;
                    }
                }
                if (inningChangedCounter % 2 == 1) {
                    list.add(new InningScore(awayRuns, runs));
                    totalHomeRuns += runs;
                } else {
                    list.add(new InningScore(runs, -1));
                    totalAwayRuns += runs;
                }
                for (int i = list.size(); i < totalInnings; i++) {
                    list.add(new InningScore(-1, -1));
                }
                TextView awayTeamView = getView().findViewById(R.id.top_team);
                TextView homeTeamView = getView().findViewById(R.id.bottom_team);
                awayTeamView.setText(awayTeamName);
                homeTeamView.setText(homeTeamName);
                LinearLayout boxScoreTotal = getView().findViewById(R.id.boxscore_total);
                TextView topTotalView = boxScoreTotal.findViewById(R.id.inning_top_row);
                TextView bottomTotalView = boxScoreTotal.findViewById(R.id.inning_bottom_row);
                TextView titleTotalView = boxScoreTotal.findViewById(R.id.inning_number_row);
                topTotalView.setText(String.valueOf(totalAwayRuns));
                bottomTotalView.setText(String.valueOf(totalHomeRuns));
                topTotalView.setTypeface(null, Typeface.BOLD);
                bottomTotalView.setTypeface(null, Typeface.BOLD);
                titleTotalView.setTypeface(null, Typeface.BOLD);

                BoxScoreArrayAdapter scoreAdapter = new BoxScoreArrayAdapter(list);
                RecyclerView boxScoreGrid = getView().findViewById(R.id.boxscore_grid);
                boxScoreGrid.setLayoutManager(new LinearLayoutManager(
                        getActivity(), LinearLayoutManager.HORIZONTAL, false));
                boxScoreGrid.setAdapter(scoreAdapter);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        awayAdapter.swapCursor(null);
        if (homeAdapter != null) {
            homeAdapter.swapCursor(null);
        }
    }
}
