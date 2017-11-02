package com.example.android.scorekeepdraft1;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private Random randomizer = new Random();
    private Button continueGame;
    private FirebaseFirestore mFirestore;
    private static final int TEMP_LOADER = 44;
    private static final int BACKUP_PLAYER_LOADER = 45;
    private static final int BACKUP_TEAM_LOADER = 46;
    private static final String TAG = "MainActivity: ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirestore = FirebaseFirestore.getInstance();

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
            CollectionReference players = mFirestore.collection("players");

            @Override
            public void onClick(View view) {
                if (players.document("19").get().isSuccessful()) {
                    Toast.makeText(MainActivity.this, "FOUND", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "LOST", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Button dummyData = findViewById(R.id.dummy_data);
        dummyData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String[] listOfPlayers = {"Purp1", "Purp2", "Purp3", "Purp4", "Purp5",
                        "Purp6", "Purp7", "Purp8", "Purp9", "Purp10"};

                for (String playerName : listOfPlayers) {
                    ContentValues values = new ContentValues();
                    values.put(StatsEntry.COLUMN_NAME, playerName);
                    values.put(StatsEntry.COLUMN_TEAM, "Purptopes");
                    getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
//                    long id = ContentUris.parseId(uri);
//                    Player player = new Player(playerName, "Purptopes", id);
//                    players.document(playerName).set(player);
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
        });

        getLoaderManager().initLoader(TEMP_LOADER, null, this);
        getLoaderManager().initLoader(BACKUP_PLAYER_LOADER, null, this);
        getLoaderManager().initLoader(BACKUP_TEAM_LOADER, null, this);

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
        ConnectivityManager connManager;
        NetworkInfo mWifi;
        switch (loader.getId()) {
            case TEMP_LOADER:
                if (cursor.moveToFirst()) {
                    continueGame.setVisibility(View.VISIBLE);
                } else {
                    continueGame.setVisibility(View.GONE);
                }
                break;
            case BACKUP_PLAYER_LOADER:
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    final int playerId = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_PLAYERID));
                    final int tRBI = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_RBI));
                    final int tRun = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_RUN));
                    final int t1b = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_1B));
                    final int t2b = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_2B));
                    final int t3b = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_3B));
                    final int tHR = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_HR));
                    final int tOuts = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_OUT));
                    final int tBB = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_BB));
                    final int tSF = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_SF));


                    final DocumentReference docRef = mFirestore.collection("players").document(String.valueOf(playerId));
                    mFirestore.runTransaction(new Transaction.Function<Void>() {
                        @Nullable
                        @Override
                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                            Player player = transaction.get(docRef).toObject(Player.class);
                            int games = player.getGames() + 1;
                            int rbi = player.getRbis() + tRBI;
                            int runs = player.getRuns() + tRun;
                            int singles = player.getSingles() + t1b;
                            int doubles = player.getDoubles() + t2b;
                            int triples = player.getTriples() + t3b;
                            int hrs = player.getHrs() + tHR;
                            int outs = player.getOuts() + tOuts;
                            int walks = player.getWalks() + tBB;
                            int sacfly = player.getSacFlies() + tSF;

                            player.setGames(games);
                            player.setRbis(rbi);
                            player.setRuns(runs);
                            player.setSingles(singles);
                            player.setDoubles(doubles);
                            player.setTriples(triples);
                            player.setHrs(hrs);
                            player.setOuts(outs);
                            player.setWalks(walks);
                            player.setSacFlies(sacfly);

                            transaction.set(docRef, player);
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Uri uri = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, playerId);
                            getContentResolver().delete(uri, null, null);
                            Log.d(TAG, "Transaction success!");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Transaction failure.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Transaction failure.", e);
                        }
                    });
                }
                break;
            case BACKUP_TEAM_LOADER:
                int i = cursor.getCount();
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    final int teamId = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_TEAM_ID));
                    final int tWins = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_WINS));
                    final int tLosses = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_LOSSES));
                    final int tTies = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_TIES));
                    final int tRunsScored = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_RUNSFOR));
                    final int tRunsAllowed = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_RUNSAGAINST));

                    final DocumentReference docRef = mFirestore.collection("teams").document(String.valueOf(teamId));
                    mFirestore.runTransaction(new Transaction.Function<Void>() {
                        @Nullable
                        @Override
                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                            Team team = transaction.get(docRef).toObject(Team.class);
                            int wins = team.getWins();
                            int losses = team.getLosses();
                            int ties = team.getTies();
                            int runsScored = team.getTotalRunsScored();
                            int runsAllowed = team.getTotalRunsAllowed();

                            team.setWins(wins + tWins);
                            team.setLosses(losses + tLosses);
                            team.setTies(ties + tTies);
                            team.setTotalRunsScored(runsScored + tRunsScored);
                            team.setTotalRunsAllowed(runsAllowed + tRunsAllowed);

                            transaction.set(docRef, team);
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Uri uri = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_BACKUP_TEAMS, teamId);
                            getContentResolver().delete(uri, null, null);
                            Log.d(TAG, "Transaction success!");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Transaction failure.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Transaction failure.", e);
                        }
                    });
                }

                break;
            default:
                Log.v(TAG, "ERROR WITH CURSORLOADER.");
        }
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}