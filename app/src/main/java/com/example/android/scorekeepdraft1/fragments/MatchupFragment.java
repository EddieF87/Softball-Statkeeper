package com.example.android.scorekeepdraft1.fragments;


import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
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

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.GameActivity;
import com.example.android.scorekeepdraft1.activities.SetLineupActivity;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.TeamListAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MatchupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener{

    private TeamListAdapter leftListAdapter;
    private TeamListAdapter rightListAdapter;
    private Spinner awayTeamSpinner;
    private Spinner homeTeamSpinner;

    private RecyclerView rvLeft;
    private RecyclerView rvRight;

    private String awayTeamSelection;
    private String homeTeamSelection;
    private static final int MATCHUP_LOADER = 5;


    public MatchupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_matchup, container, false);

        awayTeamSpinner = rootView.findViewById(R.id.awayteam_spinner);
        homeTeamSpinner = rootView.findViewById(R.id.hometeam_spinner);

        rvLeft = rootView.findViewById(R.id.rv_left_team);
        rvRight = rootView.findViewById(R.id.rv_right_team);

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
                startActivity(intent);
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
                startActivity(intent);
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
                if (getLeftListAdapter().getItemCount() < 4) {
                    Toast.makeText(getActivity(), "Add more players to " + awayTeamSelection + " lineup first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getRightListAdapter().getItemCount() < 4) {
                    Toast.makeText(getActivity(), "Add more players to " + homeTeamSelection + " lineup first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                setLineupsToDB();
                Intent intent = new Intent(getActivity(), GameActivity.class);
                startActivity(intent);
                finish();
            }
        });
        getLoaderManager().initLoader(MATCHUP_LOADER, null, this);
    }




    //TODO add menu options so I can put "Create new team there" and have more space


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
        if (view == null) {
            return;
        }
        TextView textView = (TextView) view;
        String team = textView.getText().toString();
        if (parent.getId() == R.id.awayteam_spinner) {
            awayTeamSelection = team;
        } else if (parent.getId() == R.id.hometeam_spinner) {
            homeTeamSelection = team;
        } else {
            Toast.makeText(getActivity(), "onItemSelected error ", Toast.LENGTH_SHORT).show();
        }
        List<Player> playerList = getLineup(team);

        SharedPreferences.Editor editor;
        SharedPreferences spinnersaving;
        if (parent.getId() == R.id.awayteam_spinner) {
            rvLeft.setLayoutManager(new LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL, false));
            leftListAdapter = new TeamListAdapter(playerList);
            rvLeft.setAdapter(leftListAdapter);
            spinnersaving = getSharedPreferences("awayspinnerstate", 0);

        } else {
            rvRight.setLayoutManager(new LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL, false));
            rightListAdapter = new TeamListAdapter(playerList);
            rvRight.setAdapter(rightListAdapter);
            spinnersaving = getSharedPreferences("homespinnerstate", 0);
        }
        editor = spinnersaving.edit();
        editor.putInt("spinnerPos", position);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        Spinner spinnerAway = findViewById(R.id.awayteam_spinner);
//        Spinner spinnerHome = findViewById(R.id.hometeam_spinner);
        if (awayTeamSpinner.getSelectedItem() == null || homeTeamSpinner.getSelectedItem() == null) {return;}
//        SharedPreferences awaySpinnerSave = getSharedPreferences("awayspinnerstate", 0);
//        SharedPreferences homeSpinnerSave = getSharedPreferences("homespinnerstate", 0);
//
//        onItemSelected(spinnerAway, View view, awayIndex, 0)
//        awayTeamSelection = spinnerAway.getSelectedItem().toString();
//        homeTeamSelection = spinnerHome.getSelectedItem().toString();

        List<Player> awayList = getLineup(awayTeamSelection);
        List<Player> homeList = getLineup(homeTeamSelection);

        rvLeft.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        leftListAdapter = new TeamListAdapter(awayList);
        rvLeft.setAdapter(leftListAdapter);
        rvRight.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        rightListAdapter = new TeamListAdapter(homeList);
        rvRight.setAdapter(rightListAdapter);
    }

    private ArrayList<Player> getLineup(String team){
        ArrayList<Player> lineup = new ArrayList<>();
        List<Player> benchList = new ArrayList<>();
        try {
            String[] projection = new String[]{StatsContract.StatsEntry._ID, StatsContract.StatsEntry.COLUMN_ORDER, StatsEntry.COLUMN_NAME, StatsEntry.COLUMN_FIRESTORE_ID};
            String selection = StatsContract.StatsEntry.COLUMN_TEAM + "=?";
            String[] selectionArgs = new String[]{team};
            String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

            Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, projection,
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
            Toast.makeText(this, "woops  " + e, Toast.LENGTH_SHORT).show();
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
                benchView = findViewById(R.id.bench_away);
                benchView.setText(builder.toString());
            }
            if (team.equals(homeTeamSelection)){
                benchView = findViewById(R.id.bench_home);
                benchView.setText(builder.toString());
            }
        }
    }

    public TeamListAdapter getLeftListAdapter() {
        return leftListAdapter;
    }

    public TeamListAdapter getRightListAdapter() {
        return rightListAdapter;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("awayTeam", awayTeamSelection);
        outState.putString("homeTeam", homeTeamSelection);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        awayTeamSelection = savedInstanceState.getString("awayTeam");
        homeTeamSelection = savedInstanceState.getString("homeTeam");
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = new String[]{StatsContract.StatsEntry._ID, StatsEntry.COLUMN_NAME};
        MyApp myApp = (MyApp) getApplicationContext();
        String leagueID = myApp.getCurrentSelection().getId();
        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String league = leagueID;
        String[] selectionArgs = new String[]{league};
        return new CursorLoader(this, StatsContract.StatsEntry.CONTENT_URI_TEAMS, projection,
                selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.spinner_layout, cursor,
                new String[]{StatsContract.StatsEntry.COLUMN_NAME},
                new int[]{R.id.spinnerTarget}, 0);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        int numberOfTeams = cursor.getCount();
        SharedPreferences awaySpinnerSave = getSharedPreferences("awayspinnerstate", 0);
        SharedPreferences homeSpinnerSave = getSharedPreferences("homespinnerstate", 0);
        awayTeamSpinner.setAdapter(adapter);
        homeTeamSpinner.setAdapter(adapter);
        awayTeamSpinner.setOnItemSelectedListener(this);
        homeTeamSpinner.setOnItemSelectedListener(this);
        int awayIndex = awaySpinnerSave.getInt("spinnerPos", 0);
        int homeIndex = homeSpinnerSave.getInt("spinnerPos", 1);
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
