package com.example.android.softballstatkeeper.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.softballstatkeeper.MyApp;
import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.activities.LeagueManagerActivity;
import com.example.android.softballstatkeeper.activities.TeamManagerActivity;
import com.example.android.softballstatkeeper.activities.UsersActivity;
import com.example.android.softballstatkeeper.adapters_listeners_etc.PlayerStatsAdapter;
import com.example.android.softballstatkeeper.data.StatsContract;
import com.example.android.softballstatkeeper.data.StatsContract.StatsEntry;
import com.example.android.softballstatkeeper.dialogs.GameSettingsDialogFragment;
import com.example.android.softballstatkeeper.objects.MainPageSelection;
import com.example.android.softballstatkeeper.objects.Player;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener, View.OnClickListener {

    private static final String TAG = "StatActivity: ";
    private RecyclerView statsRV;
    private PlayerStatsAdapter mAdapter;
    private ArrayAdapter<String> mSpinnerAdapter;
    private TextView emptyView;
    private int statSort;
    private String teamFilter;
    private Integer genderFilter;
    private TextView colorView;
    private Cursor mCursor;
    private List<Player> mPlayers;
    private String selectionID;
    private int level;
    private String selectionName;
    private List<String> teamsArray;
    private static final int STATS_LOADER = 4;
    private static final String KEY_STAT_SORT = "keyStatSort";
    private static final String KEY_TEAM_FILTER = "keyTeamFilter";
    private static final String KEY_GENDER_FILTER = "keyGenderFilter";
    private static final String KEY_ALL_TEAMS = "All Teams";
    private static final String KEY_MALE = "Male";
    private static final String KEY_FEMALE = "Female";

    private HashMap<String, Integer> teamIDs;

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
        setHasOptionsMenu(true);
        Log.d("aaa", "onCreate StatsFragment");
        Bundle args = getArguments();
        level = args.getInt(MainPageSelection.KEY_SELECTION_LEVEL);
        selectionID = args.getString(MainPageSelection.KEY_SELECTION_ID);
        selectionName = args.getString(MainPageSelection.KEY_SELECTION_NAME);
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

        rootView.findViewById(R.id.name_title).setOnClickListener(this);
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
        teamIDs = new HashMap<>();
        teamIDs.put(StatsEntry.FREE_AGENT, -1);
        while (mCursor.moveToNext()) {
            int teamNameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            String teamName = mCursor.getString(teamNameIndex);
            int firestoreIDIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
            String firestoreID = mCursor.getString(firestoreIDIndex);
            int idIndex = mCursor.getColumnIndex(StatsEntry._ID);
            int id = mCursor.getInt(idIndex);
            teamIDs.put(firestoreID, id);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (level >= 3) {
            inflater.inflate(R.menu.menu_league, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_user_settings:
                Intent settingsIntent = new Intent(getActivity(), UsersActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.change_game_settings:
                SharedPreferences settingsPreferences = getActivity()
                        .getSharedPreferences(selectionID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
                int innings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);
                int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DialogFragment newFragment = GameSettingsDialogFragment.newInstance(innings, genderSorter, selectionID);
                newFragment.show(fragmentTransaction, "");
                return true;
            case R.id.action_export_stats:
                Activity activity = getActivity();
                if (activity instanceof LeagueManagerActivity) {
                    LeagueManagerActivity leagueManagerActivity = (LeagueManagerActivity) activity;
                    leagueManagerActivity.startExport(selectionName);
                    return true;
                } else if (activity instanceof TeamManagerActivity) {
                    TeamManagerActivity teamManagerActivity = (TeamManagerActivity) activity;
                    teamManagerActivity.startExport(selectionName);
                    return true;
                }
                return false;
        }
        return false;
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
            case R.id.name_title:
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
        if (teamIDs == null) {
            teamIDs = new HashMap<>();
            teamIDs.put(StatsEntry.FREE_AGENT, -1);
        }
        teamIDs.put(firestoreID, id);

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
    public void onDestroy() {
        super.onDestroy();
        Log.d("aaa", "onDestroy StatsFragment");
        RefWatcher refWatcher = MyApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("aaa", "onDestroyView StatsFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("aaa", "onDetach StatsFragment");
    }
}
