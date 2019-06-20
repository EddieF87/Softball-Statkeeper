package xyz.sleekstats.softball.fragments;


import android.content.Context;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.activities.BoxScoreActivity;
import xyz.sleekstats.softball.activities.GameActivity;
import xyz.sleekstats.softball.activities.SetLineupActivity;
import xyz.sleekstats.softball.adapters.MatchupAdapter;
import xyz.sleekstats.softball.dialogs.LineupSortDialog;
import xyz.sleekstats.softball.views.VerticalTextView;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.models.MainPageSelection;
import xyz.sleekstats.softball.models.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MatchupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {


    private OnFragmentInteractionListener mListener;
    private Spinner awayTeamSpinner;
    private Spinner homeTeamSpinner;

    private RecyclerView rvAway;
    private RecyclerView rvHome;
    private MatchupAdapter homeLineupAdapter;
    private MatchupAdapter awayLineupAdapter;
    private SimpleCursorAdapter homeSpinnerAdapter;
    private SimpleCursorAdapter awaySpinnerAdapter;
    private TextView gameSummaryView;
    private TextView inningsView;
    private TextView orderView;
    private Button startGameBtn;

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
    private int innings;
    private int genderSorter;
    private int mercyRuns;
    private boolean postGameUpdate;

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

        Bundle args = getArguments();
        leagueID = args.getString(MainPageSelection.KEY_SELECTION_ID);
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


        final LinearLayout settingsLayout = rootView.findViewById(R.id.layout_settings);
        settingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsLayout.setEnabled(false);
                if(mListener != null) {
                    mListener.goToGameSettings();
                }
                settingsLayout.setEnabled(true);
            }
        });
        setGameSettings();

        final VerticalTextView editAwayLineup = rootView.findViewById(R.id.away_lineup_editor);
        final VerticalTextView editHomeLineup = rootView.findViewById(R.id.home_lineup_editor);
        editAwayLineup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editAwayLineup.setEnabled(false);
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
                editAwayLineup.setEnabled(true);
            }
        });
        editHomeLineup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editHomeLineup.setEnabled(false);
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
                editHomeLineup.setEnabled(true);
            }
        });

        startGameBtn = rootView.findViewById(R.id.start_game);
        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startGameBtn.isEnabled()) {
                    startGameBtn.setEnabled(false);
                } else {
                    return;
                }
                if (awayTeamName == null || homeTeamName == null) {
                    Toast.makeText(getActivity(), R.string.add_teams_text,
                            Toast.LENGTH_SHORT).show();
                    startGameBtn.setEnabled(true);
                    return;
                }
                if (awayTeamName.equals(homeTeamName)) {
                    Toast.makeText(getActivity(), R.string.choose_diff_teams_text, Toast.LENGTH_SHORT).show();
                    startGameBtn.setEnabled(true);
                    return;
                }
                if (awayPlayersCount < 4) {
                    Toast.makeText(getActivity(), "Add more players to " + awayTeamName + " lineup first.", Toast.LENGTH_SHORT).show();
                    startGameBtn.setEnabled(true);
                    return;
                }
                if (homePlayersCount < 4) {
                    Toast.makeText(getActivity(), "Add more players to " + homeTeamName + " lineup first.", Toast.LENGTH_SHORT).show();
                    startGameBtn.setEnabled(true);
                    return;
                }
                if(mListener != null) {
                    mListener.clearGameDB();
                }

                if (setLineupsToDB()) {
                    startGameBtn.setEnabled(true);
                    return;
                }


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

                if(sortArgument == 0) {
                    startGame();
//                    startGameBtn.setEnabled(true);
                } else {
                    openLineupSortDialog(sortArgument);
                }

            }
        });
        getLoaderManager().restartLoader(MATCHUP_LOADER, null, this);
        initialization = true;
        return rootView;
    }

    private void startGame() {
        if(mListener != null) {
            mListener.startGameActivity(awayTeamID, homeTeamID, innings, 0, genderSorter, mercyRuns);
        }
    }

    public void openLineupSortDialog(int sortArg) {
         FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         DialogFragment newFragment = LineupSortDialog.newInstance(awayTeamID, homeTeamID, innings, sortArg, genderSorter);
        newFragment.setCancelable(false);
        newFragment.show(fragmentTransaction, "");
    }

    public void setPostGameLayout(boolean clickable){
        postGameUpdate = !clickable;
        View view = getView();
        if(view == null) {return;}
        view.findViewById(R.id.start_game).setEnabled(clickable);
        view.findViewById(R.id.continue_game).setVisibility(View.GONE);
        if(gameSummaryView == null) {
            gameSummaryView = view.findViewById(R.id.current_game_view);
        }
        gameSummaryView.setVisibility(View.GONE);
    }

    public void onTransferError() {
        postGameUpdate = false;
        View view = getView();
        if(view == null) {return;}
        view.findViewById(R.id.start_game).setEnabled(true);
        view.findViewById(R.id.continue_game).setVisibility(View.VISIBLE);
        if(gameSummaryView == null) {
            gameSummaryView = view.findViewById(R.id.current_game_view);
        }
        gameSummaryView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        setStartButtonClickable();

        final Button continueGameButton = getView().findViewById(R.id.continue_game);
        continueGameButton.setEnabled(true);
        continueGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueGameButton.setEnabled(false);
                if(startGameBtn != null) {
                    startGameBtn.setEnabled(false);
                }
                if(mListener != null) {
                    mListener.goToGameActivity();
                }
            }
        });

        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{leagueID};
        Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG,
                null, selection, selectionArgs, null);
        if (!postGameUpdate && cursor.moveToLast()) {
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
    }

    private void setGameSummaryView(final int awayRuns, final int homeRuns){
        SharedPreferences savedGamePreferences = getActivity()
                .getSharedPreferences(leagueID + StatsEntry.GAME, Context.MODE_PRIVATE);
        final int inningNumber = savedGamePreferences.getInt("keyInningNumber", 2);
        int inningDisplay = inningNumber / 2;
        final int totalInnings = savedGamePreferences.getInt(GameActivity.KEY_TOTALINNINGS, 7);
        final String awayID = savedGamePreferences.getString(StatsEntry.COLUMN_AWAY_TEAM, "");
        final String homeID = savedGamePreferences.getString(StatsEntry.COLUMN_HOME_TEAM, "");
        final String awayTeamName = getTeamNameFromFirestoreID(awayID);
        final String homeTeamName = getTeamNameFromFirestoreID(homeID);
        if(awayTeamName == null || homeTeamName == null) {
            return;
        }
        String awayTeamAbv = getTeamAbv(awayTeamName);
        String homeTeamAbv = getTeamAbv(homeTeamName);
        String summary = awayTeamAbv + ": " + awayRuns + "    "  + homeTeamAbv + ": " + homeRuns + "\nInning: " + inningDisplay;
        gameSummaryView.setText(summary);
        gameSummaryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameSummaryView.setEnabled(false);
                Intent intent = new Intent(getActivity(), BoxScoreActivity.class);
                Bundle b = new Bundle();
                b.putString("awayTeamName", awayTeamName);
                b.putString("homeTeamName", homeTeamName);
                b.putString("awayTeamID", awayID);
                b.putString("homeTeamID", homeID);
                b.putInt("totalInnings", totalInnings);
                b.putInt("inningNumber", inningNumber);
                b.putInt("awayTeamRuns", awayRuns);
                b.putInt("homeTeamRuns", homeRuns);
                b.putInt("homeTeamRuns", homeRuns);
                intent.putExtras(b);
                startActivity(intent);
                gameSummaryView.setEnabled(true);
            }
        });
    }

    private String getTeamAbv(String team) {
        if(team == null) {
            return null;
        }
        if (team.length() > 2) {
            return  ("" + team.charAt(0) + team.charAt(1) + team.charAt(2)).toUpperCase();
        } else if (team.length() > 1) {
            return  ("" + team.charAt(0)  + team.charAt(1)).toUpperCase();
        } else {
            return  ("" + team.charAt(0)).toUpperCase();
        }
    }

    private String getTeamNameFromFirestoreID(String firestoreID) {
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{firestoreID, leagueID};
        String[] projection = new String[]{StatsEntry.COLUMN_NAME};

        Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                projection, selection, selectionArgs, null);
        String name = null;
        if (cursor.moveToFirst()) {
            name = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_NAME);
        }
        cursor.close();
        return name;
    }

    public void setGameSettings() {
        SharedPreferences settingsPreferences = getActivity()
                .getSharedPreferences(leagueID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
        innings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);
        genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
        mercyRuns = settingsPreferences.getInt(StatsEntry.MERCY, 99);
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
            stringBuilder.append("<font color='#6fa2ef'>M</font>");
        }
        stringBuilder.append("<font color='#f99da2'>F</font>");
        String order = stringBuilder.toString();
        orderView.setText(Html.fromHtml(order));
    }

    private int getGenderSorter() {
        SharedPreferences genderPreferences = getActivity()
                .getSharedPreferences(leagueID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
        return genderPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
    }

    private boolean setLineupsToDB() {
        final Button continueGameButton = getView().findViewById(R.id.continue_game);
        continueGameButton.setVisibility(View.GONE);
        gameSummaryView.setVisibility(View.GONE);

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
        int femalesInRow = 0;
        int firstMalesInRow = 0;
        int firstFemalesInRow = 0;
        boolean beforeFirstMale = true;
        boolean beforeFirstFemale = true;
        boolean firstInRowIsMale = true;
        boolean notProperOrder = false;

        for (int i = 0; i < lineup.size(); i++) {
            Player player = lineup.get(i);
            long playerId = player.getPlayerId();
            String playerName = player.getName();
            String firestoreID = player.getFirestoreID();
            int gender = player.getGender();

            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_LEAGUE_ID, leagueID);
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
                femalesInRow = 0;
                beforeFirstMale = false;
                if (beforeFirstFemale) {
                    firstInRowIsMale = true;
                    firstMalesInRow++;
                }
                if (malesInRow > requiredFemale) {
                    notProperOrder = true;
                }
            } else {
                females++;
                femalesInRow++;
                malesInRow = 0;
                beforeFirstFemale = false;
                if (beforeFirstMale) {
                    firstInRowIsMale = false;
                    firstFemalesInRow++;
                }
                if (femalesInRow > 1) {
                    notProperOrder = true;
                }
            }
        }
        if (requiredFemale < 1) {
            return 0;
        }

        if(firstInRowIsMale) {
            if (firstMalesInRow + malesInRow > requiredFemale) {
                notProperOrder = true;
            }
        } else {
            if (firstFemalesInRow + femalesInRow > 1) {
                notProperOrder = true;
            }
        }

        if (notProperOrder) {
            Log.d("POOOO", "notProperOrder");
            if (females * requiredFemale == males) {
                Toast.makeText(getActivity(),
                        "Please set " + teamName + "'s lineup properly or edit gender order settings",
                        Toast.LENGTH_LONG).show();

                Log.d("POOOO", "1");
                return 1;
            } else {
                Log.d("POOOO", "2");
                return 2;
            }
        }
        Log.d("POOOO", "0");
        return 0;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String teamName;
        String teamID;
        if (view == null) {
            switch (parent.getId()) {
                case R.id.awayteam_spinner:
                    if (awayTeamName != null) {
                        List<Player> playerList = getLineup(awayTeamID);
                        updateRVs(rvAway, playerList);
                    } else {
                        return;
                    }
                    break;
                case R.id.hometeam_spinner:
                    if (homeTeamName != null) {
                        List<Player> playerList = getLineup(homeTeamID);
                        updateRVs(rvHome, playerList);
                    } else {
                        return;
                    }
                    break;
                default:
                    Toast.makeText(getActivity(), "Error ", Toast.LENGTH_SHORT).show();
                    break;
            }
            return;
        }
        TextView textView = (TextView) view;
        teamName = textView.getText().toString();
        teamID = mTeamMap.get(teamName);
        switch (parent.getId()) {
            case R.id.awayteam_spinner:
                awayTeamName = teamName;
                awayTeamID = teamID;
                break;
            case R.id.hometeam_spinner:
                homeTeamName = teamName;
                homeTeamID = teamID;
                break;
            default:
                Toast.makeText(getActivity(), "Error ", Toast.LENGTH_SHORT).show();
                break;
        }
        List<Player> playerList = getLineup(teamID);

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
        if (requestCode == LINEUP_REQUEST) {
            getLoaderManager().restartLoader(MATCHUP_LOADER, null, this);
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateMatchup();
    }


    private void updateRVs(RecyclerView rv, List<Player> playerList) {
        if(playerList == null) {return;}
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

    public void updateMatchup() {
        if (awayTeamSpinner == null || awayTeamSpinner.getSelectedItem() == null
                || homeTeamSpinner == null || homeTeamSpinner.getSelectedItem() == null) {
            return;
        }

        List<Player> awayList = getLineup(awayTeamID);
        List<Player> homeList = getLineup(homeTeamID);

        updateRVs(rvAway, awayList);
        updateRVs(rvHome, homeList);
    }

    private void updateAwayRV(List<Player> lineup) {
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

    public ArrayList<Player> getAwayLineupCopy() {
        return new ArrayList<>(awayLineup);
    }

    public ArrayList<Player> getHomeLineupCopy() {
        return new ArrayList<>(homeLineup);
    }

    private void initAwayRV() {
        int genderSorter = getGenderSorter();

        rvAway.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));
        awayLineupAdapter = new MatchupAdapter(awayLineup, getActivity(), genderSorter);
        rvAway.setAdapter(awayLineupAdapter);
        awayPlayersCount = awayLineupAdapter.getItemCount();
    }

    private void updateHomeRV(List<Player> lineup) {
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
        homeLineupAdapter = new MatchupAdapter(homeLineup, getActivity(), genderSorter);
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
            String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
            String[] selectionArgs = new String[]{teamID, leagueID};
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

    public void setStartButtonClickable() {
        if(startGameBtn == null) {
            startGameBtn = getView().findViewById(R.id.start_game);
        }
        startGameBtn.setEnabled(true);
    }

    private void getBench(String teamID) {
        List<Player> benchList = new ArrayList<>();
        try {
            String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_ORDER + ">? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
            String[] selectionArgs = new String[]{teamID, String.valueOf(49), leagueID};

            Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, null,
                    selection, selectionArgs, null);

            while (cursor.moveToNext()) {
                benchList.add(new Player(cursor, false));
            }
            cursor.close();
            addToBench(benchList, teamID);
        } catch (Exception ignored) {}
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
            color = ContextCompat.getColor(getContext(), R.color.colorM);
        } else {
            color = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
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
        outState.putString(StatsEntry.COLUMN_AWAY_TEAM, awayTeamID);
        outState.putString(StatsEntry.COLUMN_HOME_TEAM, homeTeamID);
        outState.putBoolean(StatsEntry.UPDATE, postGameUpdate);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            awayTeamID = savedInstanceState.getString(StatsEntry.COLUMN_AWAY_TEAM);
            homeTeamID = savedInstanceState.getString(StatsEntry.COLUMN_HOME_TEAM);
            postGameUpdate = savedInstanceState.getBoolean(StatsEntry.UPDATE, false);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{leagueID};
        String[] projection = new String[]{StatsContract.StatsEntry._ID,
                StatsEntry.COLUMN_NAME, StatsEntry.COLUMN_FIRESTORE_ID};
        String sortOrder = StatsEntry.COLUMN_NAME + " COLLATE NOCASE";

        return new CursorLoader(getActivity(), StatsContract.StatsEntry.CONTENT_URI_TEAMS,
                projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mTeamMap = new HashMap<>();

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            String firestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
            String name = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_NAME);
            mTeamMap.put(name, firestoreID);
        }

        SharedPreferences spinnerStates = getActivity()
                .getSharedPreferences(SPINNER_STATE, Context.MODE_PRIVATE);
        awaySpinnerAdapter = getSpinnerAdapter(R.layout.spinner_matchup_left, cursor);
        homeSpinnerAdapter = getSpinnerAdapter(R.layout.spinner_matchup, cursor);
        awayTeamSpinner.setAdapter(awaySpinnerAdapter);
        homeTeamSpinner.setAdapter(homeSpinnerAdapter);
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
        if(awaySpinnerAdapter != null) {
            awaySpinnerAdapter.swapCursor(null);
        }
        if(homeSpinnerAdapter != null) {
            homeSpinnerAdapter.swapCursor(null);
        }
    }

    private SimpleCursorAdapter getSpinnerAdapter(int layout, Cursor data) {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), layout, data,
                new String[]{StatsEntry.COLUMN_NAME},
                new int[]{R.id.spinnerTarget}, 0);
        adapter.setDropDownViewResource(R.layout.spinner_matchup_dropdown);
        return adapter;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MatchupFragment.OnFragmentInteractionListener) {
            mListener = (MatchupFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLineupSortListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void clearGameDB();
        void goToGameSettings();
        void startGameActivity(String awayID, String homeID, int inningAmt, int sortArg, int femaleOrder, int mercy);
        void goToGameActivity();
    }
}
