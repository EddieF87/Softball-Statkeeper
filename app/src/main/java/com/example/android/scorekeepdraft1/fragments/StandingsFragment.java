package com.example.android.scorekeepdraft1.fragments;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.UserSettingsActivity;
import com.example.android.scorekeepdraft1.activities.TeamPagerActivity;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.StandingsCursorAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

/**
 * A simple {@link Fragment} subclass.
 */
public class StandingsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {


    private String[] projection = new String[]{"*, (CAST ((" + StatsEntry.COLUMN_WINS + ") AS FLOAT) / (" + StatsEntry.COLUMN_WINS + " + "
            + StatsEntry.COLUMN_LOSSES + ")) AS winpct"};
    private String statToSortBy = "winpct";
    private static final int STANDINGS_LOADER = 3;
    private EditText addEditText;
    private Button addSubmitButton;
    private int level;
    private String leagueID;
    private StandingsCursorAdapter mAdapter;

    public StandingsFragment() {
        // Required empty public constructor
    }

    public static StandingsFragment newInstance(String leagueID, int level) {

        Bundle args = new Bundle();
        StandingsFragment fragment = new StandingsFragment();
        args.putInt(MainPageSelection.KEY_SELECTION_LEVEL, level);
        args.putString(MainPageSelection.KEY_SELECTION_ID, leagueID);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_standings, container, false);

        ListView listView = rootView.findViewById(R.id.list);
        mAdapter = new StandingsCursorAdapter(getActivity(), null);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TeamPagerActivity.class);
                Uri currentTeamUri = ContentUris.withAppendedId(StatsContract.StatsEntry.CONTENT_URI_TEAMS, id);
                Log.d("xxx", "onItemClick team id = " + id);
                intent.setData(currentTeamUri);
                startActivity(intent);
            }
        });
        View emptyView = rootView.findViewById(R.id.empty_text);
        listView.setEmptyView(emptyView);

        TextView title = rootView.findViewById(R.id.standings_header);
        title.setVisibility(View.GONE);

        rootView.findViewById(R.id.name_title).setOnClickListener(this);
        rootView.findViewById(R.id.win_title).setOnClickListener(this);
        rootView.findViewById(R.id.loss_title).setOnClickListener(this);
        rootView.findViewById(R.id.tie_title).setOnClickListener(this);
        rootView.findViewById(R.id.winpct_title).setOnClickListener(this);
        rootView.findViewById(R.id.runsfor_title).setOnClickListener(this);
        rootView.findViewById(R.id.runsagainst_title).setOnClickListener(this);
        rootView.findViewById(R.id.rundiff_title).setOnClickListener(this);


        View addPlayerView = rootView.findViewById(R.id.item_team_adder);

        addEditText = addPlayerView.findViewById(R.id.add_player_text);
        addEditText.setHint(R.string.add_team);
        addSubmitButton = addPlayerView.findViewById(R.id.add_player_submit);
        addSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTeam();
            }
        });

        final FloatingActionButton startAdderButton = addPlayerView.findViewById(R.id.btn_start_adder);
        startAdderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAdderButton.setVisibility(View.GONE);
                addEditText.setVisibility(View.VISIBLE);
                addSubmitButton.setVisibility(View.VISIBLE);
            }
        });

        getLoaderManager().initLoader(STANDINGS_LOADER, null, this);

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
                Intent settingsIntent = new Intent(getActivity(), UserSettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.change_game_settings:
                SharedPreferences settingsPreferences = getActivity()
                        .getSharedPreferences(leagueID + "settings", Context.MODE_PRIVATE);
                int innings = settingsPreferences.getInt("innings", 7);
                int genderSorter = settingsPreferences.getInt("genderSort", 0);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DialogFragment newFragment = GameSettingsDialogFragment.newInstance(innings, genderSorter, leagueID);
                newFragment.show(fragmentTransaction, "");
                return true;
        }
        return false;
    }

    private void createTeamFragment(String team) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = CreateTeamFragment.newInstance(team);
        newFragment.show(fragmentTransaction, "");
    }

    public void addTeam() {
        String teamName = addEditText.getText().toString();

        ContentValues values = new ContentValues();
        values.put(StatsEntry.COLUMN_NAME, teamName);
        Uri teamUri = getActivity().getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);
        addEditText.setText("");
        if (teamUri == null) {
            InputMethodManager inputManager = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            return;
        }
        createTeamFragment(teamName);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder;
        if (statToSortBy.equals(StatsContract.StatsEntry.COLUMN_NAME)) {
            sortOrder = statToSortBy + " COLLATE NOCASE ASC";
        } else {
            sortOrder = statToSortBy + " DESC";
        }


        return new CursorLoader(
                getActivity(),
                StatsContract.StatsEntry.CONTENT_URI_TEAMS,
                projection,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.name_title:
                statToSortBy = StatsEntry.COLUMN_NAME;
                projection = null;
                break;
            case R.id.win_title:
                statToSortBy = StatsEntry.COLUMN_WINS;
                projection = null;
                break;
            case R.id.loss_title:
                statToSortBy = StatsEntry.COLUMN_LOSSES;
                projection = null;
                break;
            case R.id.tie_title:
                statToSortBy = StatsEntry.COLUMN_TIES;
                projection = null;
                break;
            case R.id.winpct_title:
                statToSortBy = "winpct";
                projection = new String[]{"*, (CAST ((" + StatsEntry.COLUMN_WINS + ") AS FLOAT) / (" + StatsEntry.COLUMN_WINS + " + "
                        + StatsEntry.COLUMN_LOSSES + ")) AS winpct"};
                break;
            case R.id.runsfor_title:
                statToSortBy = StatsEntry.COLUMN_RUNSFOR;
                projection = null;
                break;
            case R.id.runsagainst_title:
                statToSortBy = StatsEntry.COLUMN_RUNSAGAINST;
                projection = null;
                break;
            case R.id.rundiff_title:
                statToSortBy = "rundiff";
                projection = new String[]{"*, (" + StatsEntry.COLUMN_RUNSFOR + " - " + StatsEntry.COLUMN_RUNSAGAINST + ") AS rundiff"};
                break;
            default:
                Toast.makeText(getActivity(), "SOMETHIGN WRONG WITH onClick", Toast.LENGTH_LONG).show();
        }
        getLoaderManager().restartLoader(STANDINGS_LOADER, null, this);
    }

}
