package com.example.android.scorekeepdraft1.fragments;


import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.LeagueManagerActivity;
import com.example.android.scorekeepdraft1.activities.UserSettingsActivity;
import com.example.android.scorekeepdraft1.activities.TeamPagerActivity;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.PlayerStatsAdapter;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.StandingsAdapter;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.StandingsCursorAdapter;
import com.example.android.scorekeepdraft1.data.FirestoreHelper;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.dialogs.AddNewPlayersDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.ChooseOrCreateTeamDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.GameSettingsDialogFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Team;

import java.util.ArrayList;
import java.util.Collections;

public class StandingsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {

    private static final int STANDINGS_LOADER = 3;
    private int level;
    private String leagueID;
    private String leagueName;
    private StandingsAdapter mAdapter;
    private ArrayList<Team> mTeams;
    private TextView colorView;
    private FloatingActionButton startAdderButton;
    private RecyclerView standingsRV;


    public StandingsFragment() {
        // Required empty public constructor
    }

    public static StandingsFragment newInstance(String leagueID, int level, String name) {
        Bundle args = new Bundle();
        StandingsFragment fragment = new StandingsFragment();
        args.putInt(MainPageSelection.KEY_SELECTION_LEVEL, level);
        args.putString(MainPageSelection.KEY_SELECTION_ID, leagueID);
        args.putString(MainPageSelection.KEY_SELECTION_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        level = args.getInt(MainPageSelection.KEY_SELECTION_LEVEL);
        leagueID = args.getString(MainPageSelection.KEY_SELECTION_ID);
        leagueName = args.getString(MainPageSelection.KEY_SELECTION_NAME);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_standings, container, false);

        Button waiversButton = rootView.findViewById(R.id.btn_waivers);
        waiversButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TeamPagerActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        rootView.findViewById(R.id.name_title).setOnClickListener(this);
        rootView.findViewById(R.id.win_title).setOnClickListener(this);
        rootView.findViewById(R.id.loss_title).setOnClickListener(this);
        rootView.findViewById(R.id.tie_title).setOnClickListener(this);
        rootView.findViewById(R.id.winpct_title).setOnClickListener(this);
        rootView.findViewById(R.id.runsfor_title).setOnClickListener(this);
        rootView.findViewById(R.id.runsagainst_title).setOnClickListener(this);
        rootView.findViewById(R.id.rundiff_title).setOnClickListener(this);
        standingsRV = rootView.findViewById(R.id.rv_standings);
        View emptyView = rootView.findViewById(R.id.empty_text);
        startAdderButton = rootView.findViewById(R.id.item_team_adder);

        startAdderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAdderButton.setVisibility(View.INVISIBLE);
                chooseOrCreateTeamDialog();
            }
        });

        getLoaderManager().initLoader(STANDINGS_LOADER, null, this);
        return rootView;
    }

    private void updateStandingsRV() {
        if (mAdapter == null) {
            standingsRV.setLayoutManager(new LinearLayoutManager(
                    getActivity(), LinearLayoutManager.VERTICAL, false));
            mAdapter = new StandingsAdapter(mTeams, getActivity());
            standingsRV.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void chooseOrCreateTeamDialog() {
        Collections.sort(mTeams, Team.nameComparator());
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = ChooseOrCreateTeamDialogFragment.newInstance(mTeams);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (level >= 3) {
            inflater.inflate(R.menu.menu_league, menu);
        }
    }

    public void setAdderButtonVisible() {
        if (startAdderButton != null) {
            startAdderButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_user_settings:
                Intent settingsIntent = new Intent(getActivity(), UserSettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.change_game_settings:
                SharedPreferences settingsPreferences = getActivity()
                        .getSharedPreferences(leagueID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
                int innings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);
                int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DialogFragment newFragment = GameSettingsDialogFragment.newInstance(innings, genderSorter, leagueID);
                newFragment.show(fragmentTransaction, "");
                return true;
            case R.id.action_export_stats:
                Activity activity = getActivity();
                if (activity instanceof LeagueManagerActivity) {
                    LeagueManagerActivity leagueManagerActivity = (LeagueManagerActivity) activity;
                    leagueManagerActivity.startExport(leagueName);
                    return true;
                }
                return false;
        }
        return false;
    }

    public void addNewPlayersDialog(String teamName, String teamID) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = AddNewPlayersDialogFragment.newInstance(teamName, teamID);
        newFragment.show(fragmentTransaction, "");
    }



    public void addTeam(String team) {
        ContentValues values = new ContentValues();
        values.put(StatsEntry.COLUMN_NAME, team);
        values.put(StatsEntry.ADD, true);
        Uri teamUri = getActivity().getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);

        if (teamUri == null) {
            return;
        }
        new FirestoreHelper(getActivity(), leagueID).updateTimeStamps();

        Cursor cursor = getActivity().getContentResolver().query(teamUri, new String[]{StatsEntry.COLUMN_FIRESTORE_ID}, null, null, null);
        if (cursor.moveToFirst()) {
            int teamIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
            String teamID = cursor.getString(teamIDIndex);
            addNewPlayersDialog(team, teamID);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                StatsEntry.CONTENT_URI_TEAMS,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(mTeams == null) {
            mTeams = new ArrayList<>();
        } else {
            mTeams.clear();
        }
        data.moveToPosition(-1);
        while (data.moveToNext()) {
            mTeams.add(new Team(data));
        }
        updateStandingsRV();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onClick(View view) {
        if (colorView != null) {
            colorView.setTextColor(Color.WHITE);
        }
        colorView = getView().findViewById(view.getId());
        colorView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));

        switch (view.getId()) {
            case R.id.name_title:
                Collections.sort(mTeams, Team.nameComparator());
                break;
            case R.id.win_title:
                Collections.sort(mTeams, Team.winComparator());
                break;
            case R.id.loss_title:
                Collections.sort(mTeams, Team.lossComparator());
                break;
            case R.id.tie_title:
                Collections.sort(mTeams, Team.tieComparator());
                break;
            case R.id.winpct_title:
                Collections.sort(mTeams, Team.winpctComparator());
                break;
            case R.id.runsfor_title:
                Collections.sort(mTeams, Team.runsComparator());
                break;
            case R.id.runsagainst_title:
                Collections.sort(mTeams, Team.runsAllowedComparator());
                break;
            case R.id.rundiff_title:
                Collections.sort(mTeams, Team.runDiffComparator());
                break;
            default:
                Toast.makeText(getActivity(), "SOMETHIGN WRONG WITH onClick", Toast.LENGTH_LONG).show();
        }
        updateStandingsRV();
    }
}
