package com.example.android.scorekeepdraft1;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private Random randomizer = new Random();
    private Button continueGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button stats = findViewById(R.id.statistics);
        stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, StatsActivity.class);
                startActivity(intent);
            }
        });

        Button standings = findViewById(R.id.standings);
        standings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, StandingsActivity.class);
                startActivity(intent);
            }
        });

        Button newGame = findViewById(R.id.new_game);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MatchupActivity.class);
                getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
                getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
                SharedPreferences savedGamePreferences = getSharedPreferences("info", MODE_PRIVATE);
                SharedPreferences.Editor editor = savedGamePreferences.edit();
                editor.clear();
                editor.commit();
                startActivity(intent);
            }
        });

        continueGame = findViewById(R.id.continue_game);
        continueGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        Button randomize = findViewById(R.id.randomize_team_stats);
        randomize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] listOfTeams = {"Purptopes", "Rigtopes", "Goon Nation", "Boogeymen"};
                for (String team : listOfTeams) {
                    int wins = randomizer.nextInt(10);
                    int losses = randomizer.nextInt(10);
                    int ties = randomizer.nextInt(3);
                    int runsfor = randomizer.nextInt(14) * (wins + losses + ties);
                    int runsagainst = randomizer.nextInt(14) * (wins + losses + ties);

                    ContentValues values = new ContentValues();
                    values.put(StatsEntry.COLUMN_NAME, team);
                    values.put(StatsEntry.COLUMN_LEAGUE, "ISL");
                    values.put(StatsEntry.COLUMN_WINS, wins);
                    values.put(StatsEntry.COLUMN_LOSSES, losses);
                    values.put(StatsEntry.COLUMN_TIES, ties);
                    values.put(StatsEntry.COLUMN_RUNSFOR, runsfor);
                    values.put(StatsEntry.COLUMN_RUNSAGAINST, runsagainst);
                    getContentResolver().update(StatsEntry.CONTENT_URI_TEAMS, values,
                            StatsEntry.COLUMN_NAME + "=?", new String[] {team});
                }
            }
        });

        Button dummyData = findViewById(R.id.dummy_data);
        dummyData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] listOfPlayers = {"Purp1", "Purp2", "Purp3", "Purp4", "Purp5",
                        "Purp6", "Purp7", "Purp8", "Purp9", "Purp10"};

                for (String player : listOfPlayers) {
                    ContentValues values = new ContentValues();
                    values.put(StatsEntry.COLUMN_NAME, player);
                    values.put(StatsEntry.COLUMN_TEAM, "Purptopes");
                    getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
                }

                String[] listOfPlayers2 = {"Goon1", "Goon2", "Goon3", "Goon4", "Goon5",
                        "Goon6", "Goon7", "Goon8", "Goon9", "Goon10"};

                for (String player : listOfPlayers2) {
                    ContentValues values = new ContentValues();
                    values.put(StatsEntry.COLUMN_NAME, player);
                    values.put(StatsEntry.COLUMN_TEAM, "Goon Nation");
                    getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
                }

                String[] listOfTeams = {"Purptopes", "Rigtopes", "Goon Nation", "Boogeymen"};
                for (String team : listOfTeams) {
                    ContentValues values = new ContentValues();
                    values.put(StatsEntry.COLUMN_NAME, team);
                    values.put(StatsEntry.COLUMN_LEAGUE, "ISL");
                    getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);
                }
            }
        });

        getLoaderManager().initLoader(44, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, StatsEntry.CONTENT_URI_GAMELOG, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.moveToFirst()){
            continueGame.setVisibility(View.VISIBLE);
        } else {
            continueGame.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}