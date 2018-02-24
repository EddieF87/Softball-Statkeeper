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
import com.example.android.scorekeepdraft1.adapters_listeners_etc.LineupAdapter;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.VerticalTextView;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.dialogs.GameSettingsDialogFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MatchupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {


    private Spinner awayTeamSpinner;
    private Spinner homeTeamSpinner;

    private RecyclerView rvAway;
    private RecyclerView rvHome;
    private LineupAdapter homeLineupAdapter;
    private LineupAdapter awayLineupAdapter;
    private TextView gameSummaryView;
    private TextView inningsView;
    private TextView orderView;

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
    private int innings;
    private int genderSorter;

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
        Log.d("qqq", "onCreate MatchupFragment");

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_matchup, container, false);

        awayTeamSpinner = rootView.findViewById(R.id.awayteam_spinner);
        homeTeamSpinner = rootView.findViewById(R.id.hometeam_spinner);

        rvAway = rootView.findViewById(R.id.rv_left_team);
        rvHome = rootView.findViewById(R.id.rv_right_team);
        gameSummaryView = rootView.findViewById(R.id.current_game_view);

        inningsView = rootView.findViewById(R.id.innings_view);
        orderView = rootView.findViewById(R.id.gender_lineup_view);
        setGameSettings();

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

                SharedPreferences gamePreferences =
                        getActivity().getSharedPreferences(leagueID + StatsEntry.GAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = gamePreferences.edit();
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
        Log.d("qqq", "getLoaderManager restartLoader");

        initialization = true;
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
        if (cursor.moveToLast()) {
            continueGameButton.setVisibility(View.VISIBLE);
            gameSummaryView.setVisibility(View.VISIBLE);
            int awayRuns = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_AWAY_RUNS);
            int homeRuns = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HOME_RUNS);
            setGameSummaryView(awayRuns, homeRuns);
        } else {
            continueGameButton.setVisibility(View.GONE);
            gameSummaryView.setVisibility(View.GONE);
        }
        cursor.close();
        Log.d("qqq", "onResume initRVs");
    }

    private void setGameSummaryView(int awayRuns, int homeRuns){
        SharedPreferences savedGamePreferences = getActivity()
                .getSharedPreferences(leagueID + StatsEntry.GAME, Context.MODE_PRIVATE);
        int inningNumber = savedGamePreferences.getInt("keyInningNumber", 2);
        inningNumber = inningNumber/2;
        String awayID = savedGamePreferences.getString("keyAwayTeam", "");
        String homeID = savedGamePreferences.getString("keyHomeTeam", "");
        String awayTeamName = getTeamName(awayID);
        String homeTeamName = getTeamName(homeID);
        if(awayTeamName == null || homeTeamName == null) {
            return;
        }
        String summary = awayTeamName + ": " + awayRuns + "    "  + homeTeamName + ": " + homeRuns + "\nInning: " + inningNumber;
        gameSummaryView.setText(summary);
    }

    private String getTeamName(String teamID) {
        String team = getTeamNameFromFirestoreID(teamID);
        if(team == null) {
            return null;
        }
        if (team.length() > 2) {
            return  ("" + team.charAt(0) + team.charAt(1) + team.charAt(2)).toUpperCase();
        } else {
            return  ("" + team.charAt(0)).toUpperCase();
        }
    }

//    private String getKeyFromMap(String firestoreID){
//        if(mTeamMap == null) {
//            return null;
//        }
//        for(Map.Entry entry: mTeamMap.entrySet()){
//            if(firestoreID.equals(entry.getValue())) {
//                return entry.getKey().toString();
//            }
//        }
//        return null;
//    }

    private String getTeamNameFromFirestoreID(String firestoreID) {
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = new String[]{firestoreID};
        String[] projection = new String[]{StatsEntry.COLUMN_NAME};

        Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                projection, selection, selectionArgs, null);
        String name = null;
        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            name = cursor.getString(nameIndex);
        }
        cursor.close();
        return name;
    }

    public void setGameSettings() {
        SharedPreferences settingsPreferences = getActivity()
                .getSharedPreferences(leagueID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
        innings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);
        genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
        if(inningsView == null) {
            return;
        }
        String inningsText = "Innings: " +  innings;
        inningsView.setText(inningsText);
        setGenderSettingDisplay(genderSorter);
    }

    private void setGenderSettingDisplay(int i) {
        if(orderView == null) {
            return;
        }
        if (i == 0) {
            orderView.setVisibility(View.INVISIBLE);
            return;
        }
        orderView.setVisibility(View.VISIBLE);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Order: ");
        for (int index = 0; index < i; index++) {
            stringBuilder.append("<font color='#6fa2ef'>B</font>");
        }
        stringBuilder.append("<font color='#f99da2'>G</font>");
        String order = stringBuilder.toString();
        orderView.setText(Html.fromHtml(order));
    }

    private void clearGameDB() {
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        SharedPreferences savedGamePreferences = getActivity()
                .getSharedPreferences(leagueID + StatsEntry.GAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedGamePreferences.edit();
        editor.clear();
        editor.commit();
    }

    private int getGenderSorter() {
        SharedPreferences genderPreferences = getActivity()
                .getSharedPreferences(leagueID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
        return genderPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
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
        List<Player> lineup = getLineup(teamSelection);
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
        Log.d("qqq", "onItemSelected");
        String teamName;
        String teamID;
        if (view == null) {
            Log.d("qqq", "view == null");

            if (parent.getId() == R.id.awayteam_spinner) {
                if (awayTeamName != null) {
                    List<Player> playerList = getLineup(awayTeamID);
                    updateRVs(rvAway, playerList);
                } else {
                    return;
                }
            } else if (parent.getId() == R.id.hometeam_spinner) {
                if (homeTeamName != null) {
                    List<Player> playerList = getLineup(homeTeamID);
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
            Log.d("qqq", "parent.getId() == R.id.awayteam_spinner" + awayTeamName + awayTeamID);
        } else if (parent.getId() == R.id.hometeam_spinner) {
            homeTeamName = teamName;
            homeTeamID = teamID;
            Log.d("qqq", "parent.getId() == R.id.hometeam_spinner" + homeTeamName + homeTeamID);
        } else {
            Toast.makeText(getActivity(), "onItemSelected error ", Toast.LENGTH_SHORT).show();
        }
        List<Player> playerList = getLineup(teamID);

        SharedPreferences.Editor editor;
        SharedPreferences spinnerStates = getActivity()
                .getSharedPreferences(SPINNER_STATE, Context.MODE_PRIVATE);
        String key;
        if (parent.getId() == R.id.awayteam_spinner) {
            updateRVs(rvAway, playerList);
            key = KEY_AWAY_STATE;
            Log.d("qqq", "update Away RV");
        } else {
            updateRVs(rvHome, playerList);
            key = KEY_HOME_STATE;
            Log.d("qqq", "update Home RV");
        }
        editor = spinnerStates.edit();
        editor.putInt(key, position);
        editor.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LINEUP_REQUEST) {
            getLoaderManager().restartLoader(MATCHUP_LOADER, null, this);
            Log.d("qqq", "getLoaderManager restartLoader");
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (awayTeamSpinner.getSelectedItem() == null || homeTeamSpinner.getSelectedItem() == null) {
            Log.d("qqq", "awayTeamSpinner.getSelectedItem() == null || homeTeamSpinner.getSelectedItem() == nul");
            return;
        }

        List<Player> awayList = getLineup(awayTeamID);
        Log.d("qqq", "List<Player> awayList = getLineup(" + awayTeamName + "  " + awayTeamID);
        List<Player> homeList = getLineup(homeTeamID);
        Log.d("qqq", "List<Player> homeList = getLineup(" + homeTeamName + "  " + homeTeamID);

        updateRVs(rvAway, awayList);
        updateRVs(rvHome, homeList);
    }


    private void updateRVs(RecyclerView rv, List<Player> playerList) {
        if (initialization) {
            Log.d("qqq", "initialization");
            updateAwayRV(playerList);
            updateHomeRV(playerList);
            initialization = false;
            return;
        }
        if (rv == rvAway) {
            Log.d("qqq", "rv == rvAway");
            updateAwayRV(playerList);
        } else if (rv == rvHome) {
            Log.d("qqq", "rv == rvHome");
            updateHomeRV(playerList);
        }
    }

    public void updateAwayRV(List<Player> lineup) {
        Log.d("qqq", "updateAwayRV");
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
        Log.d("qqq", "initAwayRV");
        int genderSorter = getGenderSorter();

        rvAway.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));
        awayLineupAdapter = new LineupAdapter(awayLineup, getActivity(), genderSorter);
        rvAway.setAdapter(awayLineupAdapter);
        awayPlayersCount = awayLineupAdapter.getItemCount();
    }

    public void updateHomeRV(List<Player> lineup) {
        Log.d("qqq", "updateHomeRV");
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
        Log.d("qqq", "initHomeRV");
        int genderSorter = getGenderSorter();

        rvHome.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));
        homeLineupAdapter = new LineupAdapter(homeLineup, getActivity(), genderSorter);
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

    private ArrayList<Player> getLineup(String teamID) {
        ArrayList<Player> lineupList = new ArrayList<>();
        List<Player> benchList = new ArrayList<>();
        try {
            String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
            String[] selectionArgs = new String[]{teamID};
            String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

            Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, null,
                    selection, selectionArgs, sortOrder);

            while (cursor.moveToNext()) {
                int order = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_ORDER);
                if (order < 50) {
                    lineupList.add(new Player(cursor, false));
                } else {
                    benchList.add(new Player(cursor, false));
                }
            }
            cursor.close();
            addToBench(benchList, teamID);
            return lineupList;
        } catch (Exception e) {
            return null;
        }
    }

    private void getBench(String teamID) {
        List<Player> benchList = new ArrayList<>();
        try {
            String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_ORDER + ">?";
            String[] selectionArgs = new String[]{teamID, String.valueOf(49)};

            Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, null,
                    selection, selectionArgs, null);

            while (cursor.moveToNext()) {
                benchList.add(new Player(cursor, false));
            }
            cursor.close();
            addToBench(benchList, teamID);
        } catch (Exception e) {}
    }


    public void updateBenchColors() {
        getBench(awayTeamID);
        getBench(homeTeamID);
    }

    private void addToBench(List<Player> benchList, String teamID) {
        TextView benchView;
        boolean genderSortOn = getGenderSorter() != 0;
        int color;
        if (genderSortOn) {
            color = ContextCompat.getColor(getContext(), R.color.male);
        } else {
            color = Color.parseColor("#666666");
        }

        if (teamID.equals(awayTeamID) || teamID.equals(homeTeamID)) {
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
            if (teamID.equals(awayTeamID)) {
                benchView = getView().findViewById(R.id.bench_away);
                benchView.setTextColor(color);
                benchView.setText(Html.fromHtml(builder.toString()));
            }
            if (teamID.equals(homeTeamID)) {
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
        String sortOrder = StatsEntry.COLUMN_NAME + " COLLATE NOCASE";

        return new CursorLoader(getActivity(), StatsContract.StatsEntry.CONTENT_URI_TEAMS,
                projection, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
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
        Log.d("qqq", "homeTeamSpinner.setOnItemSelectedListener(this);");
        awayTeamSpinner.setOnItemSelectedListener(this);
        Log.d("qqq", "awayTeamSpinner.setOnItemSelectedListener(this);");

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
}
