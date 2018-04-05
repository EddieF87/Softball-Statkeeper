package xyz.sleekstats.softball.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.activities.UsersActivity;
import xyz.sleekstats.softball.adapters.PlayerStatsAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.objects.MainPageSelection;
import xyz.sleekstats.softball.objects.Player;
import xyz.sleekstats.softball.objects.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener, View.OnClickListener {

    private RecyclerView statsRV;
    private PlayerStatsAdapter mAdapter;
    private ArrayAdapter<String> mSpinnerAdapter;
    private TextView emptyView;
    private Button startAdderButton;
    private int statSort;
    private String teamFilter;
    private Integer genderFilter;
    private TextView colorView;
    private Cursor mCursor;
    private List<Player> mPlayers;
    private String selectionID;
    private int level;
    private List<String> teamsArray;
    private static final int STATS_LOADER = 4;
    private static final String KEY_STAT_SORT = "keyStatSort";
    private static final String KEY_TEAM_FILTER = "keyTeamFilter";
    private static final String KEY_GENDER_FILTER = "keyGenderFilter";
    private static final String KEY_ALL_TEAMS = "All Teams";
    private static final String KEY_MALE = "Male";
    private static final String KEY_FEMALE = "Female";

//    private HashMap<String, Integer> teamIDs;
    private OnFragmentInteractionListener mListener;

    public StatsFragment() {
        // Required empty public constructor
    }

    public static StatsFragment newInstance(String leagueID, int level, String name) {
        Bundle args = new Bundle();
        args.putInt(MainPageSelection.KEY_SELECTION_LEVEL, level);
        args.putString(MainPageSelection.KEY_SELECTION_ID, leagueID);
        args.putString(MainPageSelection.KEY_SELECTION_NAME, name);
        StatsFragment fragment = new StatsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            level = args.getInt(MainPageSelection.KEY_SELECTION_LEVEL);
            selectionID = args.getString(MainPageSelection.KEY_SELECTION_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            statSort = savedInstanceState.getInt(KEY_STAT_SORT, -1);
            teamFilter = savedInstanceState.getString(KEY_TEAM_FILTER);
            genderFilter = savedInstanceState.getInt(KEY_GENDER_FILTER);
        } else {
            statSort = -1;
        }

        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        statsRV = rootView.findViewById(R.id.rv_stats);
        emptyView = rootView.findViewById(R.id.empty_stats_view);

        startAdderButton = rootView.findViewById(R.id.item_team_adder);
        if(level < UsersActivity.LEVEL_VIEW_WRITE) {
            startAdderButton.setVisibility(View.INVISIBLE);
        } else {
            startAdderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startAdderButton.setVisibility(View.INVISIBLE);
                    startAdderButton.setVisibility(View.INVISIBLE);

                    if(mListener != null) {
                        Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                                null, null, null, StatsEntry.COLUMN_NAME);
                        ArrayList<Team> teams = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            Team team = new Team(cursor);
                            teams.add(team);
                        }
                        cursor.close();
                        mListener.startAdder(teams);
                    }
                }
            });
        }

        rootView.findViewById(R.id.player_name_title).setOnClickListener(this);
        rootView.findViewById(R.id.team_abv_title).setOnClickListener(this);
        rootView.findViewById(R.id.hr_title).setOnClickListener(this);
        rootView.findViewById(R.id.ab_title).setOnClickListener(this);
        rootView.findViewById(R.id.hit_title).setOnClickListener(this);
        rootView.findViewById(R.id.rbi_title).setOnClickListener(this);
        rootView.findViewById(R.id.run_title).setOnClickListener(this);
        rootView.findViewById(R.id.avg_title).setOnClickListener(this);
        rootView.findViewById(R.id.obp_title).setOnClickListener(this);
        rootView.findViewById(R.id.slg_title).setOnClickListener(this);
        rootView.findViewById(R.id.ops_title).setOnClickListener(this);
        rootView.findViewById(R.id.sgl_title).setOnClickListener(this);
        rootView.findViewById(R.id.dbl_title).setOnClickListener(this);
        rootView.findViewById(R.id.tpl_title).setOnClickListener(this);
        rootView.findViewById(R.id.bb_title).setOnClickListener(this);
        rootView.findViewById(R.id.game_title).setOnClickListener(this);

        mCursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                new String[]{StatsEntry._ID, StatsEntry.COLUMN_NAME, StatsEntry.COLUMN_FIRESTORE_ID},
                null, null, StatsEntry.COLUMN_NAME + " COLLATE NOCASE");

        teamsArray = new ArrayList<>();
        teamsArray.add(KEY_ALL_TEAMS);
//        teamIDs = new HashMap<>();
//        teamIDs.put(StatsEntry.FREE_AGENT, -1);
        while (mCursor.moveToNext()) {
            String teamName = StatsContract.getColumnString(mCursor, StatsEntry.COLUMN_NAME);
//            String firestoreID = StatsContract.getColumnString(mCursor, StatsEntry.COLUMN_FIRESTORE_ID);
//            int id = StatsContract.getColumnInt(mCursor, StatsEntry._ID);
//            teamIDs.put(firestoreID, id);
            teamsArray.add(teamName);
        }
        teamsArray.add(StatsEntry.FREE_AGENT);
        teamsArray.add(KEY_MALE);
        teamsArray.add(KEY_FEMALE);

        Spinner teamSpinner = rootView.findViewById(R.id.spinner_stats_teams);
        mSpinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_layout, teamsArray);
        teamSpinner.setAdapter(mSpinnerAdapter);
        teamSpinner.setOnItemSelectedListener(this);

        getLoaderManager().initLoader(STATS_LOADER, null, this);

        return rootView;
    }

    private void updateStatsRV() {
        if (mAdapter == null) {
            SharedPreferences settingsPreferences = getActivity()
                    .getSharedPreferences(selectionID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
            int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);

            statsRV.setLayoutManager(new LinearLayoutManager(
                    getActivity(), LinearLayoutManager.VERTICAL, false));
            mAdapter = new PlayerStatsAdapter(mPlayers, getActivity(), genderSorter);
            statsRV.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void changeColorsRV(boolean genderSettingsOn) {
        boolean update = true;
        if (mAdapter != null) {
            update = mAdapter.changeColors(genderSettingsOn);
        }
        if (update) {
            updateStatsRV();
        }
    }

    public void setAdderButtonVisible() {
        if (startAdderButton != null) {
            startAdderButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (view != null) {
            TextView textView = (TextView) view;
            String newFilter = textView.getText().toString();
            if (newFilter.equals(KEY_FEMALE) || newFilter.equals(KEY_MALE)) {
                teamFilter = null;
                if(newFilter.equals(KEY_MALE)) {
                    genderFilter = 0;
                } else {
                    genderFilter = 1;
                }
                getLoaderManager().restartLoader(STATS_LOADER, null, this);
            } else if (teamFilter == null || !teamFilter.equals(newFilter)) {
                genderFilter = null;
                teamFilter = newFilter;
                getLoaderManager().restartLoader(STATS_LOADER, null, this);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onClick(View v) {
        statSort = v.getId();
        sortStats(statSort);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = StatsEntry.COLUMN_G + " DESC";
        String selection;
        String[] selectionArgs;

        if (teamFilter != null && !teamFilter.equals(KEY_ALL_TEAMS)) {
            selection = StatsEntry.COLUMN_TEAM + "=?";
            selectionArgs = new String[]{teamFilter};
        } else if (genderFilter != null) {
            selection = StatsEntry.COLUMN_GENDER + "=?";
            selectionArgs = new String[]{String.valueOf(genderFilter)};
        } else {
            selection = null;
            selectionArgs = null;
        }

        return new CursorLoader(
                getActivity(),
                StatsContract.StatsEntry.CONTENT_URI_PLAYERS,
                null,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mPlayers == null) {
            mPlayers = new ArrayList<>();
        } else {
            mPlayers.clear();
        }
        mCursor = data;
        mCursor.moveToPosition(-1);
        while (mCursor.moveToNext()) {
            mPlayers.add(new Player(mCursor, false));
        }
        if (mPlayers.isEmpty()) {
            statsRV.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            statsRV.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        if (statSort != -1) {
            sortStats(statSort);
        } else {
            updateStatsRV();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayers == null || mAdapter == null) {
            return;
        }

        SharedPreferences settingsPreferences = getActivity()
                .getSharedPreferences(selectionID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
        int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
        boolean genderSettingsOn = genderSorter != 0;
        mAdapter.changeColors(genderSettingsOn);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_STAT_SORT, statSort);
        if(teamFilter != null) {
            outState.putString(KEY_TEAM_FILTER, teamFilter);
        }
        if(genderFilter != null) {
            outState.putInt(KEY_GENDER_FILTER, genderFilter);
        }
    }

    private void sortStats(int statSorter) {
        if (colorView != null) {
            colorView.setTextColor(Color.WHITE);
        }
        colorView = getView().findViewById(statSorter);
        colorView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));

        switch (statSorter) {
            case R.id.player_name_title:
                Collections.sort(mPlayers, Player.nameComparator());
                break;

            case R.id.team_abv_title:
                Collections.sort(mPlayers, Player.teamComparator());
                break;

            case R.id.ab_title:
                Collections.sort(mPlayers, Player.atbatComparator());
                break;

            case R.id.hit_title:
                Collections.sort(mPlayers, Player.hitComparator());
                break;

            case R.id.hr_title:
                Collections.sort(mPlayers, Player.hrComparator());

                break;
            case R.id.run_title:
                Collections.sort(mPlayers, Player.runComparator());

                break;
            case R.id.rbi_title:
                Collections.sort(mPlayers, Player.rbiComparator());

                break;
            case R.id.avg_title:
                Collections.sort(mPlayers, Player.avgComparator());
                break;

            case R.id.obp_title:
                Collections.sort(mPlayers, Player.obpComparator());
                break;

            case R.id.slg_title:
                Collections.sort(mPlayers, Player.slgComparator());
                break;

            case R.id.ops_title:
                Collections.sort(mPlayers, Player.opsComparator());
                break;

            case R.id.sgl_title:
                Collections.sort(mPlayers, Player.singleComparator());
                break;

            case R.id.dbl_title:
                Collections.sort(mPlayers, Player.doubleComparator());
                break;

            case R.id.tpl_title:
                Collections.sort(mPlayers, Player.tripleComparator());
                break;

            case R.id.bb_title:
                Collections.sort(mPlayers, Player.walkComparator());
                break;

            case R.id.game_title:
                Collections.sort(mPlayers, Player.gamesplayedComparator());
                break;

            default:
                Toast.makeText(getActivity(), "SOMETHING WRONG WITH onClick", Toast.LENGTH_LONG).show();
        }
        updateStatsRV();
    }

    public void updateTeams(String team, int id, String firestoreID) {
//        if (teamIDs == null) {
//            teamIDs = new HashMap<>();
//            teamIDs.put(StatsEntry.FREE_AGENT, -1);
//        }
//        teamIDs.put(firestoreID, id);

        if (mSpinnerAdapter == null || teamsArray == null) {
            return;
        }
        teamsArray.add(team);
        Collections.sort(teamsArray, String.CASE_INSENSITIVE_ORDER);
        teamsArray.remove(StatsEntry.FREE_AGENT);
        teamsArray.remove(KEY_ALL_TEAMS);
        teamsArray.remove(KEY_MALE);
        teamsArray.remove(KEY_FEMALE);
        teamsArray.add(teamsArray.size(), StatsEntry.FREE_AGENT);
        teamsArray.add(0, KEY_ALL_TEAMS);
        teamsArray.add(KEY_MALE);
        teamsArray.add(KEY_FEMALE);
        mSpinnerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StandingsFragment.OnFragmentInteractionListener) {
            mListener = (StatsFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void startAdder(ArrayList<Team> teams);
    }
}
