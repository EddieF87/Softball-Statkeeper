package com.example.android.scorekeepdraft1;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
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

import com.example.android.scorekeepdraft1.adapters_listeners_etc.LineupListAdapter;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.TeamListAdapter;
import com.example.android.scorekeepdraft1.data.PlayerStatsContract;
import com.example.android.scorekeepdraft1.data.PlayerStatsContract.PlayerStatsEntry;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_teams);

        String[] projection = new String[] {PlayerStatsEntry._ID, PlayerStatsEntry.COLUMN_NAME};
        String selection = PlayerStatsEntry.COLUMN_LEAGUE + "=?";
        String league = "ISL";
        String[] selectionArgs = new String[] {league};
        mCursor = getContentResolver().query(PlayerStatsEntry.CONTENT_URI2, projection,
                selection, selectionArgs, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.spinner_layout, mCursor,
                new String[] {PlayerStatsEntry.COLUMN_NAME},
                new int[] {R.id.spinnerTarget}, 0);
        adapter.setDropDownViewResource(R.layout.spinner_layout);

        awayTeamSpinner = (Spinner) findViewById(R.id.awayteam_spinner);
        homeTeamSpinner = (Spinner) findViewById(R.id.hometeam_spinner);
        awayTeamSpinner.setAdapter(adapter);
        homeTeamSpinner.setAdapter(adapter);
        awayTeamSpinner.setOnItemSelectedListener(this);
        homeTeamSpinner.setOnItemSelectedListener(this);
        awayTeamSpinner.setSelection(getPersistedItem());
        homeTeamSpinner.setSelection(getPersistedItem());

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
                finish();
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
                finish();
            }
        });

        startGame = (Button) findViewById(R.id.start_game);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetTeamsActivity.this, GameActivity.class);
                Bundle b = new Bundle();
                if (getLeftListAdapter().getItemCount() > 3 && getRightListAdapter().getItemCount() > 3) {
                    b.putString("awayteam", awayTeamSelection);
                    b.putString("hometeam", homeTeamSelection);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
            String[] projection = new String[]{PlayerStatsEntry._ID, PlayerStatsEntry.COLUMN_ORDER, PlayerStatsEntry.COLUMN_NAME};
            String selection = PlayerStatsEntry.COLUMN_TEAM + "=?";
            String[] selectionArgs = new String[]{team};
            String sortOrder = PlayerStatsEntry.COLUMN_ORDER + " ASC";

            mCursor = getContentResolver().query(PlayerStatsEntry.CONTENT_URI1, projection,
                    selection, selectionArgs, sortOrder);
            while (mCursor.moveToNext()) {
                int nameIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_NAME);
                int orderIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_ORDER);
                String playerName = mCursor.getString(nameIndex);
                int order = mCursor.getInt(orderIndex);
                if( order < 50) {
                    playerList.add(playerName);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "woops  " + e, Toast.LENGTH_SHORT).show();
        }

        if (parent.getId() == R.id.awayteam_spinner) {
            rvLeft.setLayoutManager(new LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL, false));
            leftListAdapter = new TeamListAdapter(playerList);
            rvLeft.setAdapter(leftListAdapter);
        } else {
            rvRight.setLayoutManager(new LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL, false));
            rightListAdapter = new TeamListAdapter(playerList);
            rvRight.setAdapter(rightListAdapter);
        }
        setPersistedItem(position);
    }

    public TeamListAdapter getLeftListAdapter() {return leftListAdapter;}
    public TeamListAdapter getRightListAdapter() {return rightListAdapter;}

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private int getPersistedItem() {
        String keyName = makePersistedItemKeyName();
        return PreferenceManager.getDefaultSharedPreferences(this).getInt(keyName, 0);
    }
    protected void setPersistedItem(int position) {
        String keyName = makePersistedItemKeyName();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(keyName, position).commit();
    }
    private String makePersistedItemKeyName() {
        return "_your_key";
    }
}