package com.example.android.scorekeepdraft1;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.android.scorekeepdraft1.adapters_listeners_etc.FirestoreAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Arrays;

public class LeagueActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private Button continueGame;
    private Button syncStats;
    private static final int TEMP_LOADER = 44;
    private static final int BACKUP_PLAYER_LOADER = 45;
    private static final int BACKUP_TEAM_LOADER = 46;
    private static final String TAG = "LeagueActivity: ";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league);

        Button stats = findViewById(R.id.statistics);
        stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LeagueActivity.this, StatsActivity.class);
                startActivity(intent);
            }
        });

        Button standings = findViewById(R.id.standings);
        standings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LeagueActivity.this, StandingsActivity.class);
                startActivity(intent);
            }
        });

        final Button newGame = findViewById(R.id.new_game);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewGame();
            }
        });

        continueGame = findViewById(R.id.continue_game);
        continueGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LeagueActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        syncStats = findViewById(R.id.randomize_team_stats);
        syncStats.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                syncStats.setVisibility(View.GONE);
                FirestoreAdapter statsTransfer = new FirestoreAdapter(LeagueActivity.this);
                statsTransfer.retryGameLogLoad();
            }
        });

        Button dummyData = findViewById(R.id.dummy_data);
        dummyData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDummyData();
            }
        });

    }




    public void SyncPlayers(final View v) {
        FirestoreAdapter statsTransfer = new FirestoreAdapter(LeagueActivity.this);
        statsTransfer.syncStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MENU", "onResume");
        getLoaderManager().restartLoader(TEMP_LOADER, null, this);
        getLoaderManager().restartLoader(BACKUP_PLAYER_LOADER, null, this);
        getLoaderManager().restartLoader(BACKUP_TEAM_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Uri uri;
        switch (id) {
            case TEMP_LOADER:
                uri = StatsEntry.CONTENT_URI_GAMELOG;
                break;
            case BACKUP_PLAYER_LOADER:
                uri = StatsEntry.CONTENT_URI_BACKUP_PLAYERS;
                break;
            case BACKUP_TEAM_LOADER:
                uri = StatsEntry.CONTENT_URI_BACKUP_TEAMS;
                break;
            default:
                uri = null;
        }
        return new CursorLoader(this,
                uri,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case TEMP_LOADER:
                continueGame = findViewById(R.id.continue_game);
                if (cursor.moveToFirst()) {
                    continueGame.setVisibility(View.VISIBLE);
                } else {
                    continueGame.setVisibility(View.GONE);
                }
                break;
            case BACKUP_PLAYER_LOADER:
                if (cursor.moveToFirst()) {
                    syncStats.setVisibility(View.VISIBLE);
                } else {
                    syncStats.setVisibility(View.GONE);
                }
                break;
            case BACKUP_TEAM_LOADER:
                if (cursor.moveToFirst()) {
                    syncStats.setVisibility(View.VISIBLE);
                } else {
                    syncStats.setVisibility(View.GONE);
                }
                break;
            default:
                Log.v(TAG, "ERROR WITH CURSORLOADER.");
        }
        cursor.close();
    }

    public void startNewGame(){
        Intent intent = new Intent(LeagueActivity.this, MatchupActivity.class);
        getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
        getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        SharedPreferences savedGamePreferences = getSharedPreferences("info", MODE_PRIVATE);
        SharedPreferences.Editor editor = savedGamePreferences.edit();
        editor.clear();
        editor.commit();
        startActivity(intent);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void getDummyData(){
        String[] listOfPlayers = {"Purp1", "Purp2", "Purp3", "Purp4", "Purp5",
                "Purp6", "Purp7", "Purp8", "Purp9", "Purp10"};

        for (String playerName : listOfPlayers) {
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_TEAM, "Purptopes");
            getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
        }

        String[] listOfPlayers2 = {"Goon1", "Goon2", "Goon3", "Goon4", "Goon5",
                "Goon6", "Goon7", "Goon8", "Goon9", "Goon10"};

        for (String playerName : listOfPlayers2) {
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_TEAM, "Goon Nation");
            getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
        }


        String[] listOfPlayers3 = {"Rig1", "Rig2", "Rig3", "Rig4", "Rig5",
                "Rig6", "Rig7", "Rig8", "Rig9", "Rig10"};

        for (String playerName : listOfPlayers3) {
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_TEAM, "Rigtopes");
            getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
        }

        String[] listOfPlayers4 = {"Boog1", "Boog2", "Boog3", "Boog4", "Boog5",
                "Boog6", "Boog7", "Boog8", "Boog9", "Boog10"};

        for (String playerName : listOfPlayers4) {
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_TEAM, "Boogeymen");
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
}