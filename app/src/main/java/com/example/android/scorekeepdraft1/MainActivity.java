package com.example.android.scorekeepdraft1;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.gamelog.PlayerLog;
import com.example.android.scorekeepdraft1.gamelog.TeamLog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private Random randomizer = new Random();
    private Button continueGame;
    private Button syncStats;
    private FirebaseFirestore mFirestore;
    private static final int TEMP_LOADER = 44;
    private static final int BACKUP_PLAYER_LOADER = 45;
    private static final int BACKUP_TEAM_LOADER = 46;
    private static final String TAG = "MainActivity: ";
    private int transactionCounter = 0;


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

        final Button newGame = findViewById(R.id.new_game);
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

        syncStats = findViewById(R.id.randomize_team_stats);
        syncStats.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                syncStats.setVisibility(View.GONE);
                WriteBatch batch = mFirestore.batch();

                Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, null, null, null, null);
                while (cursor.moveToNext()) {
                    long logId = cursor.getLong(cursor.getColumnIndex(StatsEntry.COLUMN_LOG_ID));
                    long playerId = cursor.getLong(cursor.getColumnIndex(StatsEntry.COLUMN_PLAYERID));
                    int gameRBI = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_RBI));
                    int gameRun = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_RUN));
                    int game1b = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_1B));
                    int game2b = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_2B));
                    int game3b = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_3B));
                    int gameHR = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_HR));
                    int gameOuts = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_OUT));
                    int gameBB = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_BB));
                    int gameSF = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_SF));


                    final DocumentReference docRef = mFirestore.collection("players").document(String.valueOf(playerId))
                            .collection("playerlogs").document(String.valueOf(logId));

                    PlayerLog playerLog = new PlayerLog(playerId, gameRBI, gameRun, game1b, game2b, game3b, gameHR, gameOuts, gameBB, gameSF);
                    batch.set(docRef, playerLog);
                }

                cursor.close();

                cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_TEAMS, null, null, null, null);
                while (cursor.moveToNext()) {
                    long logId = cursor.getLong(cursor.getColumnIndex(StatsEntry.COLUMN_LOG_ID));
                    long teamId = cursor.getLong(cursor.getColumnIndex(StatsEntry.COLUMN_TEAM_ID));
                    int gameWins = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_WINS));
                    int gameLosses = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_LOSSES));
                    int gameTies = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_TIES));
                    int gameRunsScored = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_RUNSFOR));
                    int gameRunsAllowed = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_RUNSAGAINST));

                    final DocumentReference docRef = mFirestore.collection("teams").document(String.valueOf(teamId))
                            .collection("teamlogs").document(String.valueOf(logId));

                    TeamLog teamLog = new TeamLog(teamId, gameWins, gameLosses, gameTies, gameRunsScored, gameRunsAllowed);
                    batch.set(docRef, teamLog);
                }

                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Transaction success!");
                        getContentResolver().delete(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, null, null);
                        getContentResolver().delete(StatsEntry.CONTENT_URI_BACKUP_TEAMS, null, null);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                    }
                });
                cursor.close();
            }
        });


        Button dummyData = findViewById(R.id.dummy_data);
        dummyData.setOnClickListener(new View.OnClickListener()

        {
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

    }


    public void SyncPlayers(final View v) {

        mFirestore.collection("players")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                final Player player = document.toObject(Player.class);
                                final String playerIdString = document.getId();

                                mFirestore.collection("players").document(playerIdString).collection("playerlogs")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "Query successful for " + playerIdString);

                                                    final QuerySnapshot querySnapshot = task.getResult();
                                                    int games = 0;
                                                    int rbi = 0;
                                                    int runs = 0;
                                                    int singles = 0;
                                                    int doubles = 0;
                                                    int triples = 0;
                                                    int hrs = 0;
                                                    int walks = 0;
                                                    int outs = 0;
                                                    int sfs = 0;
                                                    for (DocumentSnapshot document : task.getResult()) {
                                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                                        PlayerLog playerLog = document.toObject(PlayerLog.class);
                                                        games++;
                                                        rbi += playerLog.getRbi();
                                                        runs += playerLog.getRuns();
                                                        singles += playerLog.getSingles();
                                                        doubles += playerLog.getDoubles();
                                                        triples += playerLog.getTriples();
                                                        hrs += playerLog.getHrs();
                                                        walks += playerLog.getWalks();
                                                        outs += playerLog.getOuts();
                                                        sfs += playerLog.getSacfly();
                                                    }
                                                    final PlayerLog finalLog = new PlayerLog(0, rbi, runs, singles, doubles, triples, hrs, outs, walks, sfs);
                                                    final int finalGames = games;

                                                    mFirestore.runTransaction(new Transaction.Function<DocumentReference>() {
                                                        @Nullable
                                                        @Override
                                                        public DocumentReference apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                                            final DocumentReference docRef = mFirestore.collection("players").document(playerIdString);

                                                            int totalGames = player.getGames() + finalGames;
                                                            int totalSingles = player.getSingles() + finalLog.getSingles();
                                                            int totalDoubles = player.getDoubles() + finalLog.getDoubles();
                                                            int totalTriples = player.getTriples() + finalLog.getTriples();
                                                            int totalHrs = player.getHrs() + finalLog.getHrs();
                                                            int totalWalks = player.getWalks() + finalLog.getWalks();
                                                            int totalOuts = player.getOuts() + finalLog.getOuts();
                                                            int totalRbis = player.getRbis() + finalLog.getRbi();
                                                            int totalRuns = player.getRuns() + finalLog.getRuns();
                                                            int totalSFs = player.getSacFlies() + finalLog.getSacfly();

                                                            player.setGames(totalGames);
                                                            player.setSingles(totalSingles);
                                                            player.setDoubles(totalDoubles);
                                                            player.setTriples(totalTriples);
                                                            player.setHrs(totalHrs);
                                                            player.setWalks(totalWalks);
                                                            player.setOuts(totalOuts);
                                                            player.setRbis(totalRbis);
                                                            player.setRuns(totalRuns);
                                                            player.setSacFlies(totalSFs);

                                                            transaction.set(docRef, player);
                                                            return docRef;
                                                        }
                                                    }).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Log.d(TAG, "Update successful for " + playerIdString);
                                                            //delete GameLogs
                                                            WriteBatch batch = mFirestore.batch();
                                                            for (DocumentSnapshot snapshot : querySnapshot) {
                                                                batch.delete(snapshot.getReference());
                                                            }
                                                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Log.d(TAG, "Deletion successful for " + playerIdString);
                                                                }
                                                            });
                                                            ContentValues values = new ContentValues();
                                                            values.put(StatsEntry.COLUMN_1B, player.getSingles());
                                                            values.put(StatsEntry.COLUMN_2B, player.getDoubles());
                                                            values.put(StatsEntry.COLUMN_3B, player.getTriples());
                                                            values.put(StatsEntry.COLUMN_HR, player.getHrs());
                                                            values.put(StatsEntry.COLUMN_RUN,player.getRuns());
                                                            values.put(StatsEntry.COLUMN_RBI,player.getRbis());
                                                            values.put(StatsEntry.COLUMN_BB, player.getWalks());
                                                            values.put(StatsEntry.COLUMN_OUT,player.getOuts());
                                                            values.put(StatsEntry.COLUMN_SF, player.getSacFlies());
                                                            values.put(StatsEntry.COLUMN_G, player.getGames());
                                                            String selection = StatsEntry.COLUMN_NAME + "=?";
                                                            int rowsUpdated = getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS, values, selection, new String[] {playerIdString});
                                                            if(rowsUpdated < 1) {
                                                                values.put("sync", 0);
                                                                values.put(StatsEntry.COLUMN_NAME, player.getName());
                                                                values.put(StatsEntry.COLUMN_TEAM, player.getTeam());
                                                                Log.d(TAG,"Insert attempt for player " + playerIdString + player.getName());
                                                                getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
                                                            }
                                                        }
                                                    });

                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });


                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        mFirestore.collection("teams")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                final Team team = document.toObject(Team.class);
                                final String teamIdString = document.getId();

                                mFirestore.collection("teams").document(teamIdString).collection("teamlogs")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "Query successful for team " + teamIdString);

                                                    final QuerySnapshot querySnapshot = task.getResult();
                                                    int wins = 0;
                                                    int losses = 0;
                                                    int ties = 0;
                                                    int runsScored = 0;
                                                    int runsAllowed = 0;

                                                    for (DocumentSnapshot document : task.getResult()) {
                                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                                        TeamLog teamLog = document.toObject(TeamLog.class);
                                                        wins += teamLog.getWins();
                                                        losses += teamLog.getLosses();
                                                        ties += teamLog.getTies();
                                                        runsScored += teamLog.getRunsScored();
                                                        runsAllowed += teamLog.getRunsAllowed();
                                                    }
                                                    final TeamLog finalLog = new TeamLog(0, wins, losses, ties, runsScored, runsAllowed);

                                                    mFirestore.runTransaction(new Transaction.Function<DocumentReference>() {
                                                        @Nullable
                                                        @Override
                                                        public DocumentReference apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                                            final DocumentReference docRef = mFirestore.collection("teams").document(teamIdString);

                                                            int totalWins = team.getWins() + finalLog.getWins();
                                                            int totalLosses = team.getLosses() + finalLog.getLosses();
                                                            int totalTies = team.getTies() + finalLog.getTies();
                                                            int totalRunsScored = team.getTotalRunsScored() + finalLog.getRunsScored();
                                                            int totalRunsAllowed = team.getTotalRunsAllowed() + finalLog.getRunsAllowed();

                                                            team.setWins(totalWins);
                                                            team.setLosses(totalLosses);
                                                            team.setTies(totalTies);
                                                            team.setTotalRunsScored(totalRunsScored);
                                                            team.setTotalRunsAllowed(totalRunsAllowed);

                                                            transaction.set(docRef, team);
                                                            return docRef;
                                                        }
                                                    }).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Log.d(TAG, "Update successful for team " + teamIdString);
                                                            //delete GameLogs
                                                            WriteBatch batch = mFirestore.batch();
                                                            for (DocumentSnapshot snapshot : querySnapshot) {
                                                                batch.delete(snapshot.getReference());
                                                            }
                                                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Log.d(TAG, "Deletion successful for team " + teamIdString);
                                                                }
                                                            });
                                                            ContentValues values = new ContentValues();
                                                            values.put(StatsEntry.COLUMN_WINS, team.getWins());
                                                            values.put(StatsEntry.COLUMN_LOSSES, team.getLosses());
                                                            values.put(StatsEntry.COLUMN_TIES, team.getTies());
                                                            values.put(StatsEntry.COLUMN_RUNSFOR, team.getTotalRunsScored());
                                                            values.put(StatsEntry.COLUMN_RUNSAGAINST, team.getTotalRunsAllowed());
                                                            String selection = StatsEntry.COLUMN_NAME + "=?";
                                                            int rowsUpdated = getContentResolver().update(StatsEntry.CONTENT_URI_TEAMS, values, selection, new String[] {teamIdString});
                                                            if(rowsUpdated < 1) {
                                                                values.put("sync", 0);
                                                                values.put(StatsEntry.COLUMN_LEAGUE, "ISL");
                                                                values.put(StatsEntry.COLUMN_NAME, team.getName());
                                                                Log.d(TAG,"Insert attempt for team " + teamIdString + team.getName());
                                                                getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);
                                                            }
                                                        }
                                                    });

                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirestore = FirebaseFirestore.getInstance();
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

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}