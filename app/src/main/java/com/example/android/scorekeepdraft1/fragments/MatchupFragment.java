package com.example.android.scorekeepdraft1.fragments;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.LeagueGameActivity;
import com.example.android.scorekeepdraft1.activities.LeagueManagerActivity;
import com.example.android.scorekeepdraft1.activities.SetLineupActivity;
import com.example.android.scorekeepdraft1.activities.UserSettingsActivity;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.TeamListAdapter;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.VerticalTextView;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.dialogs.GameSettingsDialogFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;
import com.example.android.scorekeepdraft1.objects.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MatchupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {


    private Spinner awayTeamSpinner;
    private Spinner homeTeamSpinner;

    private RecyclerView rvAway;
    private RecyclerView rvHome;
    private TeamListAdapter homeLineupAdapter;
    private TeamListAdapter awayLineupAdapter;

    private String awayTeamName;
    private String homeTeamName;
    private String awayTeamID;
    private String homeTeamID;

    private Map<String, String> mTeamMap;
    private List<Player> awayLineup;
    private List<Player> homeLineup;

    private int awayPlayersCount;
    private int homePlayersCount;
    private boolean sortAwayLineup;
    private boolean sortHomeLineup;
    private boolean initialization;

    private static final String SPINNER_STATE = "spinnerstates";
    private static final String KEY_AWAY_STATE = "awaystate";
    private static final String KEY_HOME_STATE = "homestate";
    private static final int LINEUP_REQUEST = 3;
    private static final int MATCHUP_LOADER = 5;

    private String leagueID;
    private String leagueName;

    public MatchupFragment() {
        // Required empty public constructor
    }

    public static MatchupFragment newInstance(String leagueID, String name) {
        Bundle args = new Bundle();
        args.putString(MainPageSelection.KEY_SELECTION_ID, leagueID);
        args.putString(MainPageSelection.KEY_SELECTION_NAME, name);
        MatchupFragment fragment = new MatchupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        leagueID = args.getString(MainPageSelection.KEY_SELECTION_ID);
        leagueName = args.getString(MainPageSelection.KEY_SELECTION_NAME);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_league, menu);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_matchup, container, false);

        awayTeamSpinner = rootView.findViewById(R.id.awayteam_spinner);
        homeTeamSpinner = rootView.findViewById(R.id.hometeam_spinner);

        rvAway = rootView.findViewById(R.id.rv_left_team);
        rvHome = rootView.findViewById(R.id.rv_right_team);

        VerticalTextView editAwayLineup = rootView.findViewById(R.id.away_lineup_editor);
        VerticalTextView editHomeLineup = rootView.findViewById(R.id.home_lineup_editor);
        editAwayLineup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (awayTeamID == null) {
                    return;
                }
                Intent intent = new Intent(getActivity(), SetLineupActivity.class);
                Bundle b = new Bundle();
                b.putString("team_name", awayTeamName);
                b.putString("team_id", awayTeamID);
                b.putBoolean("ingame", false);
                intent.putExtras(b);
                startActivityForResult(intent, LINEUP_REQUEST);
            }
        });
        editHomeLineup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (homeTeamID == null) {
                    return;
                }
                Intent intent = new Intent(getActivity(), SetLineupActivity.class);
                Bundle b = new Bundle();
                b.putString("team_name", homeTeamName);
                b.putString("team_id", homeTeamID);
                b.putBoolean("ingame", false);
                intent.putExtras(b);
                startActivityForResult(intent, LINEUP_REQUEST);
            }
        });

        Button startGame = rootView.findViewById(R.id.start_game);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (awayTeamName == null || homeTeamName == null) {
                    Toast.makeText(getActivity(), "No teams currently in this league.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (awayTeamName.equals(homeTeamName)) {
                    Toast.makeText(getActivity(), "Please choose different teams.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (awayPlayersCount < 4) {
                    Toast.makeText(getActivity(), "Add more players to " + awayTeamName + " lineup first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (homePlayersCount < 4) {
                    Toast.makeText(getActivity(), "Add more players to " + homeTeamName + " lineup first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                clearGameDB();
                if (setLineupsToDB()) {
                    return;
                }
                Intent intent = new Intent(getActivity(), LeagueGameActivity.class);

                int sortArgument;
                if (sortAwayLineup && sortHomeLineup) {
                    sortArgument = 3;
                } else if (sortHomeLineup) {
                    sortArgument = 2;
                } else if (sortAwayLineup) {
                    sortArgument = 1;
                } else {
                    sortArgument = 0;
                }
//                intent.putExtra("sortArgument", sortArgument);

                SharedPreferences settingsPreferences = getActivity()
                        .getSharedPreferences(leagueID + "settings", Context.MODE_PRIVATE);
                int innings = settingsPreferences.getInt("innings", 7);
                int genderSorter = settingsPreferences.getInt("genderSort", 0);

                SharedPreferences gamePreferences =
                        getActivity().getSharedPreferences(leagueID + "game", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = gamePreferences.edit();
//                String awayTeam = getFirestoreIDFromTeamName(awayTeamName);
//                String homeTeam = getFirestoreIDFromTeamName(homeTeamName);
                editor.putString("keyAwayTeam", awayTeamID);
                editor.putString("keyHomeTeam", homeTeamID);
                editor.putInt("keyTotalInnings", innings);
                editor.putInt("keyGenderSort", sortArgument);
                editor.putInt("keyFemaleOrder", genderSorter);
                editor.commit();

                startActivity(intent);
            }
        });
        getLoaderManager().restartLoader(MATCHUP_LOADER, null, this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Button continueGameButton = getView().findViewById(R.id.continue_game);
        continueGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LeagueGameActivity.class);
                startActivity(intent);

            }
        });
        Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG,
                null, null, null, null);
        if (cursor.moveToFirst()) {
            continueGameButton.setVisibility(View.VISIBLE);
        } else {
            continueGameButton.setVisibility(View.GONE);
        }
        cursor.close();

        initialization = true;
    }

    private void clearGameDB() {
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        SharedPreferences savedGamePreferences = getActivity()
                .getSharedPreferences(leagueID + "game", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedGamePreferences.edit();
        editor.clear();
        editor.commit();
    }

    private int getGenderSorter() {
        SharedPreferences genderPreferences = getActivity()
                .getSharedPreferences(leagueID + "settings", Context.MODE_PRIVATE);
        return genderPreferences.getInt("genderSort", 0);
    }

    private boolean setLineupsToDB() {
        sortAwayLineup = false;
        sortHomeLineup = false;
        boolean cancel = false;
        int genderSorter = getGenderSorter();

        if (genderSorter < 1) {
            addTeamToTempDB(awayTeamName, awayTeamID, genderSorter);
            addTeamToTempDB(homeTeamName, homeTeamID, genderSorter);
            return false;
        }

        int lineupCheck = addTeamToTempDB(awayTeamName, awayTeamID, genderSorter);
        if (lineupCheck == 1) {
            cancel = true;
        } else if (lineupCheck == 2) {
            sortAwayLineup = true;
        }

        lineupCheck = addTeamToTempDB(homeTeamName, homeTeamID, genderSorter);
        if (lineupCheck == 1) {
            cancel = true;
        } else if (lineupCheck == 2) {
            sortHomeLineup = true;
        }
        return cancel;
    }

    private int addTeamToTempDB(String teamName, String teamSelection, int requiredFemale) {
        List<Player> lineup = getLineup(teamName, teamSelection);
        ContentResolver contentResolver = getActivity().getContentResolver();

        int females = 0;
        int males = 0;
        int malesInRow = 0;
        int firstMalesInRow = 0;
        boolean beforeFirstFemale = true;
        boolean notProperOrder = false;

        for (int i = 0; i < lineup.size(); i++) {
            Player player = lineup.get(i);
            long playerId = player.getPlayerId();
            String playerName = player.getName();
            String firestoreID = player.getFirestoreID();
            int gender = player.getGender();

            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_PLAYERID, playerId);
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
            values.put(StatsEntry.COLUMN_TEAM, teamName);
            values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamSelection);
            values.put(StatsEntry.COLUMN_GENDER, gender);
            values.put(StatsEntry.COLUMN_ORDER, i + 1);
            contentResolver.insert(StatsEntry.CONTENT_URI_TEMP, values);

            if (gender == 0) {
                males++;
                malesInRow++;
                if (beforeFirstFemale) {
                    firstMalesInRow++;
                }
                if (malesInRow > requiredFemale) {
                    notProperOrder = true;
                }
            } else {
                females++;
                malesInRow = 0;
                beforeFirstFemale = false;
            }
        }
        if (requiredFemale < 1) {
            return 0;
        }

        int lastMalesInRow = malesInRow;
        if (firstMalesInRow + lastMalesInRow > requiredFemale) {
            notProperOrder = true;
        }
        if (notProperOrder) {
            if (females * requiredFemale >= males) {
                Toast.makeText(getActivity(),
                        "Please set " + teamName + "'s lineup properly or change gender rules",
                        Toast.LENGTH_LONG).show();
                return 1;
            } else {
                return 2;
            }
        }
        return 0;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String teamName;
        String teamID;
        if (view == null) {
            if (parent.getId() == R.id.awayteam_spinner) {
                if (awayTeamName != null) {
                    List<Player> playerList = getLineup(awayTeamName, awayTeamID);
                    updateRVs(rvAway, playerList);
                } else {
                    return;
                }
            } else if (parent.getId() == R.id.hometeam_spinner) {
                if (homeTeamName != null) {
                    List<Player> playerList = getLineup(homeTeamName, homeTeamID);
                    updateRVs(rvHome, playerList);
                } else {
                    return;
                }
            } else {
                Toast.makeText(getActivity(), "onItemSelected error ", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        TextView textView = (TextView) view;
        teamName = textView.getText().toString();
        teamID = mTeamMap.get(teamName);
        if (parent.getId() == R.id.awayteam_spinner) {
            awayTeamName = teamName;
            awayTeamID = teamID;
        } else if (parent.getId() == R.id.hometeam_spinner) {
            homeTeamName = teamName;
            homeTeamID = teamID;
        } else {
            Toast.makeText(getActivity(), "onItemSelected error ", Toast.LENGTH_SHORT).show();
        }
        List<Player> playerList = getLineup(teamName, teamID);

        SharedPreferences.Editor editor;
        SharedPreferences spinnerStates = getActivity()
                .getSharedPreferences(SPINNER_STATE, Context.MODE_PRIVATE);
        String key;
        if (parent.getId() == R.id.awayteam_spinner) {
            updateRVs(rvAway, playerList);
            key = KEY_AWAY_STATE;
        } else {
            updateRVs(rvHome, playerList);
            key = KEY_HOME_STATE;
        }
        editor = spinnerStates.edit();
        editor.putInt(key, position);
        editor.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LINEUP_REQUEST && resultCode == getActivity().RESULT_OK) {
            getLoaderManager().restartLoader(MATCHUP_LOADER, null, this);
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (awayTeamSpinner.getSelectedItem() == null || homeTeamSpinner.getSelectedItem() == null) {
            return;
        }

        List<Player> awayList = getLineup(awayTeamName, awayTeamID);
        List<Player> homeList = getLineup(homeTeamName, homeTeamID);

        updateRVs(rvAway, awayList);
        updateRVs(rvHome, homeList);
    }


    private void updateRVs(RecyclerView rv, List<Player> playerList) {
        if (initialization) {
            updateAwayRV(playerList);
            updateHomeRV(playerList);
            initialization = false;
            return;
        }
        if (rv == rvAway) {
            updateAwayRV(playerList);
        } else if (rv == rvHome) {
            updateHomeRV(playerList);
        }
    }

    public void updateAwayRV(List<Player> lineup) {
        if (awayLineup != null) {
            awayLineup.clear();
        } else {
            awayLineup = new ArrayList<>();
        }
        awayLineup.addAll(lineup);
        awayPlayersCount = awayLineup.size();

        if (initialization || awayLineupAdapter == null) {
            initAwayRV();
        } else {
            awayLineupAdapter.notifyDataSetChanged();
        }
    }

    private void initAwayRV() {
        int genderSorter = getGenderSorter();

        rvAway.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));
        awayLineupAdapter = new TeamListAdapter(awayLineup, getActivity(), genderSorter);
        rvAway.setAdapter(awayLineupAdapter);
        awayPlayersCount = awayLineupAdapter.getItemCount();
    }

    public void updateHomeRV(List<Player> lineup) {
        if (homeLineup != null) {
            homeLineup.clear();
        } else {
            homeLineup = new ArrayList<>();
        }
        homeLineup.addAll(lineup);
        homePlayersCount = homeLineup.size();

        if (initialization || homeLineupAdapter == null) {
            initHomeRV();
        } else {
            homeLineupAdapter.notifyDataSetChanged();
        }
    }

    private void initHomeRV() {
        int genderSorter = getGenderSorter();

        rvHome.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));
        homeLineupAdapter = new TeamListAdapter(homeLineup, getActivity(), genderSorter);
        rvHome.setAdapter(homeLineupAdapter);
        homePlayersCount = homeLineupAdapter.getItemCount();
    }

    public void changeColorsRV(boolean genderSettingsOn) {
        if (awayLineupAdapter != null) {
            if (awayLineupAdapter.changeColors(genderSettingsOn)) {
                awayLineupAdapter.notifyDataSetChanged();
            }
        }
        if (homeLineupAdapter != null) {
            if (homeLineupAdapter.changeColors(genderSettingsOn)) {
                homeLineupAdapter.notifyDataSetChanged();
            }
        }
    }

    private ArrayList<Player> getLineup(String teamName, String teamID) {
        ArrayList<Player> lineup = new ArrayList<>();
        List<Player> benchList = new ArrayList<>();
        try {
            String[] projection = new String[]{StatsEntry._ID, StatsEntry.COLUMN_ORDER,
                    StatsEntry.COLUMN_NAME, StatsEntry.COLUMN_FIRESTORE_ID, StatsEntry.COLUMN_GENDER};
            String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
            String[] selectionArgs = new String[]{teamID};
            String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

            Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, projection,
                    selection, selectionArgs, sortOrder);

            int nameIndex = cursor.getColumnIndex(StatsContract.StatsEntry.COLUMN_NAME);
            int orderIndex = cursor.getColumnIndex(StatsEntry.COLUMN_ORDER);
            int idIndex = cursor.getColumnIndex(StatsEntry._ID);
            int firestoreIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
            int genderIndex = cursor.getColumnIndex(StatsEntry.COLUMN_GENDER);

            while (cursor.moveToNext()) {
                String playerName = cursor.getString(nameIndex);
                int id = cursor.getInt(idIndex);
                int gender = cursor.getInt(genderIndex);
                String firestoreID = cursor.getString(firestoreIDIndex);

                int order = cursor.getInt(orderIndex);
                if (order < 50) {
                    lineup.add(new Player(playerName, teamName, gender, id, firestoreID, teamID));
                } else {
                    benchList.add(new Player(playerName, teamName, gender, id, firestoreID, teamID));
                }
            }
            cursor.close();
            addToBench(benchList, teamName);
            return lineup;
        } catch (Exception e) {
            return null;
        }
    }

    public void updateBenchColors() {
        getLineup(awayTeamName, awayTeamID);
        getLineup(homeTeamName, homeTeamID);
    }

    private void addToBench(List<Player> benchList, String teamName) {
        TextView benchView;
        boolean genderSortOn = getGenderSorter() != 0;
        int color;
        if (genderSortOn) {
            color = ContextCompat.getColor(getContext(), R.color.male);
        } else {
            color = Color.parseColor("#666666");
        }

        if (teamName.equals(awayTeamName) || teamName.equals(homeTeamName)) {
            StringBuilder builder = new StringBuilder();
            for (Player player : benchList) {
                String string = player.getName() + "  ";
                int gender = player.getGender();
                if (genderSortOn && gender == 1) {
                    builder.append("<font color='#f99da2'>");
                    builder.append(string);
                    builder.append("</font>");
                } else {
                    builder.append(string);
                }
            }
            if (teamName.equals(awayTeamName)) {
                benchView = getView().findViewById(R.id.bench_away);
                benchView.setTextColor(color);
                benchView.setText(Html.fromHtml(builder.toString()));
            }
            if (teamName.equals(homeTeamName)) {
                benchView = getView().findViewById(R.id.bench_home);
                benchView.setTextColor(color);
                benchView.setText(Html.fromHtml(builder.toString()));
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("awayTeam", awayTeamID);
        outState.putString("homeTeam", homeTeamID);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            awayTeamID = savedInstanceState.getString("awayTeam");
            homeTeamID = savedInstanceState.getString("homeTeam");
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = new String[]{StatsContract.StatsEntry._ID,
                StatsEntry.COLUMN_NAME, StatsEntry.COLUMN_FIRESTORE_ID};
        return new CursorLoader(getActivity(), StatsContract.StatsEntry.CONTENT_URI_TEAMS, projection,
                null, null, StatsEntry.COLUMN_NAME + " COLLATE NOCASE");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.spinner_matchup_left, cursor,
//                new String[]{StatsContract.StatsEntry.COLUMN_NAME},
//                new int[]{R.id.spinnerTarget}, 0);
//        adapter.setDropDownViewResource(R.layout.spinner_matchup_dropdown);
        mTeamMap = new HashMap<>();
        int firestoreIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
        int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);

        while (cursor.moveToNext()) {
            String firestoreID = cursor.getString(firestoreIDIndex);
            String name = cursor.getString(nameIndex);
            mTeamMap.put(name, firestoreID);
        }

        SharedPreferences spinnerStates = getActivity()
                .getSharedPreferences(SPINNER_STATE, Context.MODE_PRIVATE);
        awayTeamSpinner.setAdapter(getSpinnerAdapter(R.layout.spinner_matchup_left, cursor));
        homeTeamSpinner.setAdapter(getSpinnerAdapter(R.layout.spinner_matchup, cursor));
        awayTeamSpinner.setOnItemSelectedListener(this);
        homeTeamSpinner.setOnItemSelectedListener(this);
        int awayIndex = spinnerStates.getInt(KEY_AWAY_STATE, 0);
        int homeIndex = spinnerStates.getInt(KEY_HOME_STATE, 1);
        int numberOfTeams = cursor.getCount();
        if (awayIndex >= numberOfTeams) {
            awayIndex = 0;
        }
        if (homeIndex >= numberOfTeams) {
            homeIndex = 0;
        }
        awayTeamSpinner.setSelection(awayIndex);
        homeTeamSpinner.setSelection(homeIndex);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private SimpleCursorAdapter getSpinnerAdapter(int layout, Cursor data) {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), layout, data,
                new String[]{StatsEntry.COLUMN_NAME},
                new int[]{R.id.spinnerTarget}, 0);
        adapter.setDropDownViewResource(R.layout.spinner_matchup_dropdown);
        return adapter;
    }
//
//    //todo temporary method until replacing working with names with firestoreIDs
//    private String getFirestoreIDFromTeamName(String teamName) {
//        String selection = StatsEntry.COLUMN_NAME + "=?";
//        String[] selectionArgs = new String[] {teamName};
//        Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
//                null, selection, selectionArgs, null);
//        String firestoreID = null;
//        if(cursor.moveToFirst()) {
//            int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
//            firestoreID = cursor.getString(nameIndex);
//        }
//        cursor.close();
//        return firestoreID;
//    }
}
