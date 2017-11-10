package com.example.android.scorekeepdraft1.activities;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.TeamListAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.List;

public class MatchupActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {

    private TeamListAdapter leftListAdapter;
    private TeamListAdapter rightListAdapter;
    private Spinner awayTeamSpinner;
    private Spinner homeTeamSpinner;

    private RecyclerView rvLeft;
    private RecyclerView rvRight;

    private String awayTeamSelection;
    private String homeTeamSelection;
    private static final int MATCHUP_LOADER = 5;


    //TODO add menu options so I can put "Create new team there" and have more space
    //TODO fix bench/lineup text overlap

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_teams);
        awayTeamSpinner = findViewById(R.id.awayteam_spinner);
        homeTeamSpinner = findViewById(R.id.hometeam_spinner);

        rvLeft = findViewById(R.id.rv_left_team);
        rvRight = findViewById(R.id.rv_right_team);

        Button editAwayLineup = findViewById(R.id.edit_away_team_button);
        Button editHomeLineup = findViewById(R.id.edit_home_team_button);
        editAwayLineup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (awayTeamSelection == null) {
                    return;
                }
                Intent intent = new Intent(MatchupActivity.this, SetLineupActivity.class);
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
                Intent intent = new Intent(MatchupActivity.this, SetLineupActivity.class);
                Bundle b = new Bundle();
                b.putString("team", homeTeamSelection);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        Button startGame = findViewById(R.id.start_game);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (awayTeamSelection == null || homeTeamSelection == null) {
                    Toast.makeText(MatchupActivity.this, "No teams currently in this league.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (awayTeamSelection.equals(homeTeamSelection)) {
                    Toast.makeText(MatchupActivity.this, "Please choose different teams.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getLeftListAdapter().getItemCount() < 4) {
                    Toast.makeText(MatchupActivity.this, "Add more players to " + awayTeamSelection + " lineup first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getRightListAdapter().getItemCount() < 4) {
                    Toast.makeText(MatchupActivity.this, "Add more players to " + homeTeamSelection + " lineup first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                setLineupsToDB();
                Intent intent = new Intent(MatchupActivity.this, GameActivity.class);
                startActivity(intent);
                finish();
            }
        });
        getLoaderManager().initLoader(MATCHUP_LOADER, null, this);
    }

    private void setLineupsToDB() {
        addTeamToTempDB(awayTeamSelection);
        addTeamToTempDB(homeTeamSelection);
    }

    private void addTeamToTempDB(String teamSelection){
        List<Player> lineup = getLineup(teamSelection);
        ContentResolver contentResolver = getContentResolver();
        for(int i = 0; i < lineup.size(); i++) {
            Player player = lineup.get(i);
            long playerId = player.getPlayerId();
            String playerName = player.getName();
            ContentValues values = new ContentValues();
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
            Toast.makeText(MatchupActivity.this, "onItemSelected error ", Toast.LENGTH_SHORT).show();
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
            String[] projection = new String[]{StatsContract.StatsEntry._ID, StatsContract.StatsEntry.COLUMN_ORDER, StatsEntry.COLUMN_NAME};
            String selection = StatsContract.StatsEntry.COLUMN_TEAM + "=?";
            String[] selectionArgs = new String[]{team};
            String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

            Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, projection,
                    selection, selectionArgs, sortOrder);
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(StatsContract.StatsEntry.COLUMN_NAME);
                int orderIndex = cursor.getColumnIndex(StatsEntry.COLUMN_ORDER);
                int idIndex = cursor.getColumnIndex(StatsEntry._ID);
                String playerName = cursor.getString(nameIndex);
                int id = cursor.getInt(idIndex);
                int order = cursor.getInt(orderIndex);
                if (order < 50) {
                    lineup.add(new Player(playerName, team, id));
                } else {
                    benchList.add(new Player(playerName, team, id));
                }
            }
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
        String selection = StatsContract.StatsEntry.COLUMN_LEAGUE + "=?";
        String league = "ISL";
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