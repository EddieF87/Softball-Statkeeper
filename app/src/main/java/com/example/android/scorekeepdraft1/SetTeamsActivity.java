package com.example.android.scorekeepdraft1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.adapters_listeners_etc.TeamListAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

import java.util.ArrayList;
import java.util.List;

public class SetTeamsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TeamListAdapter leftListAdapter;
    private TeamListAdapter rightListAdapter;
    private Button startGame;
    private Button editAwayLineup;
    private Button editHomeLineup;
    private Spinner awayTeamSpinner;
    private Spinner homeTeamSpinner;
    private Cursor mCursor;

    private RecyclerView rvLeft;
    private RecyclerView rvRight;

    private List<String> playerList;
    private String awayTeamSelection;
    private String homeTeamSelection;

    //TODO add menu options so I can put "Create new team there" and have more space
    //TODO add bench spots (gridview?)
    //TODO figure out how to disable option on one spinner if selected on other

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_teams);
        awayTeamSpinner = (Spinner) findViewById(R.id.awayteam_spinner);
        homeTeamSpinner = (Spinner) findViewById(R.id.hometeam_spinner);

        SharedPreferences awaySpinnerSave = getSharedPreferences("awayspinnerstate", 0);
        SharedPreferences homeSpinnerSave = getSharedPreferences("homespinnerstate", 0);


        String[] projection = new String[]{StatsContract.StatsEntry._ID, StatsEntry.COLUMN_NAME};
        String selection = StatsContract.StatsEntry.COLUMN_LEAGUE + "=?";
        String league = "ISL";
        String[] selectionArgs = new String[]{league};
        mCursor = getContentResolver().query(StatsContract.StatsEntry.CONTENT_URI2, projection,
                selection, selectionArgs, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.spinner_layout, mCursor,
                new String[]{StatsContract.StatsEntry.COLUMN_NAME},
                new int[]{R.id.spinnerTarget}, 0);
        adapter.setDropDownViewResource(R.layout.spinner_layout);


        awayTeamSpinner.setAdapter(adapter);
        homeTeamSpinner.setAdapter(adapter);
        awayTeamSpinner.setOnItemSelectedListener(this);
        homeTeamSpinner.setOnItemSelectedListener(this);
        awayTeamSpinner.setSelection(awaySpinnerSave.getInt("spinnerPos", 0));
        homeTeamSpinner.setSelection(homeSpinnerSave.getInt("spinnerPos", 0));


        rvLeft = (RecyclerView) findViewById(R.id.rv_left_team);
        rvRight = (RecyclerView) findViewById(R.id.rv_right_team);

        editAwayLineup = (Button) findViewById(R.id.edit_away_team_button);
        editHomeLineup = (Button) findViewById(R.id.edit_home_team_button);
        editAwayLineup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetTeamsActivity.this, SetLineupActivity.class);
                Bundle b = new Bundle();
                b.putString("team", awayTeamSelection);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
        editHomeLineup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetTeamsActivity.this, SetLineupActivity.class);
                Bundle b = new Bundle();
                b.putString("team", homeTeamSelection);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        startGame = (Button) findViewById(R.id.start_game);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (awayTeamSelection.equals(homeTeamSelection)) {
                    Toast.makeText(SetTeamsActivity.this, "Please choose different teams.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getLeftListAdapter().getItemCount() < 4) {
                    Toast.makeText(SetTeamsActivity.this, "Add more players to " + awayTeamSelection + " lineup first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getRightListAdapter().getItemCount() < 4) {
                    Toast.makeText(SetTeamsActivity.this, "Add more players to " + homeTeamSelection + " lineup first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(SetTeamsActivity.this, GameActivity.class);
                Bundle b = new Bundle();
                b.putString("awayteam", awayTeamSelection);
                b.putString("hometeam", homeTeamSelection);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });
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
            Toast.makeText(this, "woops I DID SOMETHIGN WRONG ", Toast.LENGTH_SHORT).show();
        }
        playerList = new ArrayList<>();

        try {
            String[] projection = new String[]{StatsContract.StatsEntry._ID, StatsContract.StatsEntry.COLUMN_ORDER, StatsEntry.COLUMN_NAME};
            String selection = StatsContract.StatsEntry.COLUMN_TEAM + "=?";
            String[] selectionArgs = new String[]{team};
            String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

            mCursor = getContentResolver().query(StatsEntry.CONTENT_URI1, projection,
                    selection, selectionArgs, sortOrder);
            while (mCursor.moveToNext()) {
                int nameIndex = mCursor.getColumnIndex(StatsContract.StatsEntry.COLUMN_NAME);
                int orderIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_ORDER);
                String playerName = mCursor.getString(nameIndex);
                int order = mCursor.getInt(orderIndex);
                if (order < 50) {
                    playerList.add(playerName);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "woops  " + e, Toast.LENGTH_SHORT).show();
        }

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
        editor.commit();
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
        outState.putInt("awaySpinner", awayTeamSpinner.getSelectedItemPosition());
        outState.putInt("homeSpinner", homeTeamSpinner.getSelectedItemPosition());
    }
}