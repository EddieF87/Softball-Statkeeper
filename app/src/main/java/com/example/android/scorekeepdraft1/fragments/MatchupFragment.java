package com.example.android.scorekeepdraft1.fragments;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.GameActivity;
import com.example.android.scorekeepdraft1.activities.SetLineupActivity;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.TeamListAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MatchupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener{


    private Spinner awayTeamSpinner;
    private Spinner homeTeamSpinner;

    private RecyclerView rvAway;
    private RecyclerView rvHome;

    private String awayTeamSelection;
    private String homeTeamSelection;

    private int awayPlayers;
    private int homePlayers;

    private static final String SPINNER_STATE = "spinnerstates";
    private static final String KEY_AWAY_STATE = "awaystate";
    private static final String KEY_HOME_STATE = "homestate";
    private static final int LINEUP_REQUEST = 3;
    private static final int MATCHUP_LOADER = 5;

    private String leagueID;

    public MatchupFragment() {
        // Required empty public constructor
    }

    public static MatchupFragment newInstance(String leagueID) {
        Bundle args = new Bundle();
        args.putString(MainPageSelection.KEY_SELECTION_ID, leagueID);
        MatchupFragment fragment = new MatchupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();

        //todo fix this out
        leagueID =  args.getString(MainPageSelection.KEY_SELECTION_ID);
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        SharedPreferences savedGamePreferences = getActivity().getSharedPreferences(leagueID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedGamePreferences.edit();
        editor.clear();
        editor.commit();
    }

    //TODO add menu options so I can put "Create new team there" and have more space

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_matchup, container, false);

        awayTeamSpinner = rootView.findViewById(R.id.awayteam_spinner);
        homeTeamSpinner = rootView.findViewById(R.id.hometeam_spinner);

        rvAway = rootView.findViewById(R.id.rv_left_team);
        rvHome = rootView.findViewById(R.id.rv_right_team);

        Button editAwayLineup = rootView.findViewById(R.id.edit_away_team_button);
        Button editHomeLineup = rootView.findViewById(R.id.edit_home_team_button);
        editAwayLineup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (awayTeamSelection == null) {
                    return;
                }
                Intent intent = new Intent(getActivity(), SetLineupActivity.class);
                Bundle b = new Bundle();
                b.putString("team", awayTeamSelection);
                intent.putExtras(b);
                startActivityForResult(intent, LINEUP_REQUEST);
            }
        });
        editHomeLineup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (homeTeamSelection == null) {
                    return;
                }
                Intent intent = new Intent(getActivity(), SetLineupActivity.class);
                Bundle b = new Bundle();
                b.putString("team", homeTeamSelection);
                intent.putExtras(b);
                startActivityForResult(intent, LINEUP_REQUEST);
            }
        });

        Button startGame = rootView.findViewById(R.id.start_game);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (awayTeamSelection == null || homeTeamSelection == null) {
                    Toast.makeText(getActivity(), "No teams currently in this league.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (awayTeamSelection.equals(homeTeamSelection)) {
                    Toast.makeText(getActivity(), "Please choose different teams.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (awayPlayers < 4) {
                    Toast.makeText(getActivity(), "Add more players to " + awayTeamSelection + " lineup first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (homePlayers < 4) {
                    Toast.makeText(getActivity(), "Add more players to " + homeTeamSelection + " lineup first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                setLineupsToDB();
                Intent intent = new Intent(getActivity(), GameActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        getLoaderManager().restartLoader(MATCHUP_LOADER, null, this);

        return rootView;
    }

    private void setLineupsToDB() {
        addTeamToTempDB(awayTeamSelection);
        addTeamToTempDB(homeTeamSelection);
    }

    private void addTeamToTempDB(String teamSelection){
        List<Player> lineup = getLineup(teamSelection);
        ContentResolver contentResolver = getActivity().getContentResolver();
        for(int i = 0; i < lineup.size(); i++) {
            Player player = lineup.get(i);
            long playerId = player.getPlayerId();
            String playerName = player.getName();
            String firestoreID = player.getFirestoreID();

            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
            values.put(StatsEntry.COLUMN_PLAYERID, playerId);
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_TEAM, teamSelection);
            values.put(StatsEntry.COLUMN_ORDER, i+1);
            contentResolver.insert(StatsEntry.CONTENT_URI_TEMP, values);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String team;
        if (view == null) {
            if (parent.getId() == R.id.awayteam_spinner) {
                if (awayTeamSelection != null) {
                    List<Player> playerList = getLineup(awayTeamSelection);
                    setAdapter(rvAway, playerList);
                } else {
                    return;
                }
            } else if (parent.getId() == R.id.hometeam_spinner) {
                if (homeTeamSelection != null) {
                    List<Player> playerList = getLineup(awayTeamSelection);
                    setAdapter(rvHome, playerList);
                } else {
                    return;
                }
            } else {
                Toast.makeText(getActivity(), "onItemSelected error ", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        TextView textView = (TextView) view;
        team = textView.getText().toString();
        if (parent.getId() == R.id.awayteam_spinner) {
            awayTeamSelection = team;
        } else if (parent.getId() == R.id.hometeam_spinner) {
            homeTeamSelection = team;
        } else {
            Toast.makeText(getActivity(), "onItemSelected error ", Toast.LENGTH_SHORT).show();
        }
        List<Player> playerList = getLineup(team);

        SharedPreferences.Editor editor;
        SharedPreferences spinnerStates = getActivity()
                .getSharedPreferences(SPINNER_STATE, Context.MODE_PRIVATE);
        String key;
        if (parent.getId() == R.id.awayteam_spinner) {
            setAdapter(rvAway, playerList);
            key = KEY_AWAY_STATE;
        } else {
            setAdapter(rvHome, playerList);
            key = KEY_HOME_STATE;
        }
        editor = spinnerStates.edit();
        editor.putInt(key, position);
        editor.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LINEUP_REQUEST && resultCode == getActivity().RESULT_OK) {
            getLoaderManager().restartLoader(MATCHUP_LOADER, null, this);
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (awayTeamSpinner.getSelectedItem() == null || homeTeamSpinner.getSelectedItem() == null) {return;}

        List<Player> awayList = getLineup(awayTeamSelection);
        List<Player> homeList = getLineup(homeTeamSelection);

        setAdapter(rvAway, awayList);
        setAdapter(rvHome, homeList);
    }


    private void setAdapter(RecyclerView rv, List<Player> playerList) {
        rv.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));
        TeamListAdapter teamListAdapter = new TeamListAdapter(playerList);
        rv.setAdapter(teamListAdapter);

        if(rv == rvAway) {
            awayPlayers = teamListAdapter.getItemCount();
        } else if (rv == rvHome) {
            homePlayers = teamListAdapter.getItemCount();
        }
    }

    private ArrayList<Player> getLineup(String team){
        ArrayList<Player> lineup = new ArrayList<>();
        List<Player> benchList = new ArrayList<>();
        try {
            String[] projection = new String[]{StatsContract.StatsEntry._ID, StatsContract.StatsEntry.COLUMN_ORDER, StatsEntry.COLUMN_NAME, StatsEntry.COLUMN_FIRESTORE_ID};
            String selection = StatsContract.StatsEntry.COLUMN_TEAM + "=?";
            String[] selectionArgs = new String[]{team};
            String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

            Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, projection,
                    selection, selectionArgs, sortOrder);
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(StatsContract.StatsEntry.COLUMN_NAME);
                int orderIndex = cursor.getColumnIndex(StatsEntry.COLUMN_ORDER);
                int idIndex = cursor.getColumnIndex(StatsEntry._ID);
                int firestoreIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);

                String playerName = cursor.getString(nameIndex);
                int id = cursor.getInt(idIndex);

                String  firestoreID = cursor.getString(firestoreIDIndex);

                int order = cursor.getInt(orderIndex);
                if (order < 50) {
                    lineup.add(new Player(playerName, team, id, firestoreID));
                } else {
                    benchList.add(new Player(playerName, team, id, firestoreID));
                }
            }
            cursor.close();
            addToBench(benchList, team);
            return lineup;
        } catch (Exception e) {
            return null;
        }
    }

    private void addToBench(List<Player> benchList, String team) {
        TextView benchView;
        if (team.equals(awayTeamSelection) || team.equals(homeTeamSelection)) {
            StringBuilder builder = new StringBuilder();
            for (Player player : benchList) {
                String string = player.getName() + "  ";
                builder.append(string);
            }
            if (team.equals(awayTeamSelection)){
                benchView = getView().findViewById(R.id.bench_away);
                benchView.setText(builder.toString());
            }
            if (team.equals(homeTeamSelection)){
                benchView = getView().findViewById(R.id.bench_home);
                benchView.setText(builder.toString());
            }
        }
    }

//    public TeamListAdapter getLeftListAdapter() {
//        return leftListAdapter;
//    }
//
//    public TeamListAdapter getRightListAdapter() {
//        return rightListAdapter;
//    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("awayTeam", awayTeamSelection);
        outState.putString("homeTeam", homeTeamSelection);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            awayTeamSelection = savedInstanceState.getString("awayTeam");
            homeTeamSelection = savedInstanceState.getString("homeTeam");
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = new String[]{StatsContract.StatsEntry._ID, StatsEntry.COLUMN_NAME};

        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{leagueID};
        return new CursorLoader(getActivity(), StatsContract.StatsEntry.CONTENT_URI_TEAMS, projection,
                selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.spinner_layout, cursor,
                new String[]{StatsContract.StatsEntry.COLUMN_NAME},
                new int[]{R.id.spinnerTarget}, 0);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        int numberOfTeams = cursor.getCount();
        SharedPreferences spinnerStates = getActivity()
                .getSharedPreferences(SPINNER_STATE, Context.MODE_PRIVATE);
        awayTeamSpinner.setAdapter(adapter);
        homeTeamSpinner.setAdapter(adapter);
        awayTeamSpinner.setOnItemSelectedListener(this);
        homeTeamSpinner.setOnItemSelectedListener(this);
        int awayIndex = spinnerStates.getInt(KEY_AWAY_STATE, 0);
        int homeIndex = spinnerStates.getInt(KEY_HOME_STATE, 1);
        if (awayIndex >= numberOfTeams) {
            awayIndex = 0;}
        if (homeIndex >= numberOfTeams) {
            homeIndex = 0;}
        awayTeamSpinner.setSelection(awayIndex);
        homeTeamSpinner.setSelection(homeIndex);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
