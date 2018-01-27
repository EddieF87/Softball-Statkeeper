package com.example.android.scorekeepdraft1.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.objects.Player;
import com.example.android.scorekeepdraft1.objects.Team;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.gamelog.PlayerLog;
import com.example.android.scorekeepdraft1.gamelog.TeamLog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

/**
 * Created by Eddie on 11/7/2017.
 */

public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper: ";
    public static final String LEAGUE_COLLECTION = "leagues";
    public static final String PLAYERS_COLLECTION = "players";
    public static final String TEAMS_COLLECTION = "teams";
    public static final String PLAYER_LOGS = "playerlogs";
    public static final String TEAM_LOGS = "teamlogs";
    public static final String USER = "id";
    public static final String USERS = "users";
    private onFirestoreSyncListener mListener;

    private Context mContext;
    private FirebaseFirestore mFirestore;

    public FirestoreHelper(Context context) {
        this.mContext = context;
        mFirestore = FirebaseFirestore.getInstance();
        if (context instanceof onFirestoreSyncListener) {
            mListener = (onFirestoreSyncListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void syncStats() {
        MyApp myApp = (MyApp) mContext.getApplicationContext();
        final String leagueID = myApp.getCurrentSelection().getId();
        mListener.onFirestoreSync();

        mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(PLAYERS_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            QuerySnapshot querySnapshot = task.getResult();
                            int numberOfPlayers = querySnapshot.size();
                            mListener.onSyncStart(numberOfPlayers, false);

                            for (DocumentSnapshot document : querySnapshot) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                final Player player = document.toObject(Player.class);
                                final String playerIdString = document.getId();
//                                int id = player.getTeamId();

                                mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(PLAYERS_COLLECTION)
                                        .document(playerIdString).collection(PLAYER_LOGS)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    WriteBatch batch = mFirestore.batch();

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
                                                        Log.d("xxx", "excalibur!!!!");

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
                                                    final PlayerLog finalLog = new PlayerLog(0, rbi, runs, singles,
                                                            doubles, triples, hrs, outs, walks, sfs);
                                                    final int finalGames = games;


                                                    final DocumentReference docRef = mFirestore.collection(LEAGUE_COLLECTION)
                                                            .document(leagueID).collection(PLAYERS_COLLECTION).document(playerIdString);

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

                                                    batch.set(docRef, player);

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
                                                    values.put(StatsEntry.COLUMN_NAME, player.getName());
                                                    values.put(StatsEntry.COLUMN_TEAM, player.getTeam());
                                                    values.put(StatsEntry.COLUMN_1B, player.getSingles());
                                                    values.put(StatsEntry.COLUMN_2B, player.getDoubles());
                                                    values.put(StatsEntry.COLUMN_3B, player.getTriples());
                                                    values.put(StatsEntry.COLUMN_HR, player.getHrs());
                                                    values.put(StatsEntry.COLUMN_RUN, player.getRuns());
                                                    values.put(StatsEntry.COLUMN_RBI, player.getRbis());
                                                    values.put(StatsEntry.COLUMN_BB, player.getWalks());
                                                    values.put(StatsEntry.COLUMN_OUT, player.getOuts());
                                                    values.put(StatsEntry.COLUMN_SF, player.getSacFlies());
                                                    values.put(StatsEntry.COLUMN_G, player.getGames());
                                                    String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";

                                                    int rowsUpdated = mContext.getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS,
                                                            values, selection, new String[]{playerIdString});

                                                    if (rowsUpdated < 1) {
                                                        values.put("sync", 0);
                                                        values.put(StatsEntry.COLUMN_NAME, player.getName());
                                                        values.put(StatsEntry.COLUMN_TEAM, player.getTeam());
                                                        values.put(StatsEntry.COLUMN_FIRESTORE_ID, playerIdString);
                                                        mContext.getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
                                                    }
                                                    mListener.onSyncUpdate(false);
                                                } else {
                                                    mListener.onSyncError();
                                                }
                                            }
                                        });
                            }
                        } else {
                            mListener.onSyncError();
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //Get the collection of all teams in a league
        mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(TEAMS_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            int numberOfTeams = querySnapshot.size();
                            mListener.onSyncStart(numberOfTeams, true);

                            //loop through teams
                            for (DocumentSnapshot document : querySnapshot) {
                                //Get the document data and ID of a team
                                final Team team = document.toObject(Team.class);
                                final String teamIdString = document.getId();

                                //Get the logs for a team
                                mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(TEAMS_COLLECTION)
                                        .document(teamIdString).collection(TEAM_LOGS)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {

                                                    WriteBatch batch = mFirestore.batch();

                                                    final QuerySnapshot querySnapshot = task.getResult();
                                                    int wins = 0;
                                                    int losses = 0;
                                                    int ties = 0;
                                                    int runsScored = 0;
                                                    int runsAllowed = 0;

                                                    //compile logs for team
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


                                                    DocumentReference docRef = mFirestore.collection(LEAGUE_COLLECTION)
                                                            .document(leagueID).collection(TEAMS_COLLECTION).document(teamIdString);

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

                                                    batch.set(docRef, team);

                                                    for (DocumentSnapshot snapshot : querySnapshot) {
                                                        batch.delete(snapshot.getReference());
                                                    }

                                                    batch.commit();

                                                    ContentValues values = new ContentValues();
                                                    values.put(StatsEntry.COLUMN_NAME, team.getName());
                                                    values.put(StatsEntry.COLUMN_WINS, team.getWins());
                                                    values.put(StatsEntry.COLUMN_LOSSES, team.getLosses());
                                                    values.put(StatsEntry.COLUMN_TIES, team.getTies());
                                                    values.put(StatsEntry.COLUMN_RUNSFOR, team.getTotalRunsScored());
                                                    values.put(StatsEntry.COLUMN_RUNSAGAINST, team.getTotalRunsAllowed());
                                                    String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
                                                    Log.d(TAG, "test:  " + teamIdString + "   " + team.getTeamId());

                                                    int rowsUpdated = mContext.getContentResolver().update(StatsEntry.CONTENT_URI_TEAMS,
                                                            values, selection, new String[]{teamIdString});

                                                    if (rowsUpdated < 1) {
                                                        values.put("sync", 0);
                                                        values.put(StatsEntry.COLUMN_NAME, team.getName());
                                                        values.put(StatsEntry.COLUMN_FIRESTORE_ID, teamIdString);
                                                        mContext.getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);
                                                    }
                                                    mListener.onSyncUpdate(true);
                                                } else {
                                                    mListener.onSyncError();
                                                }
                                            }
                                        });
                            }
                        } else {
                            mListener.onSyncError();
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    public void retryGameLogLoad() {
        MyApp myApp = (MyApp) mContext.getApplicationContext();
        String leagueID = myApp.getCurrentSelection().getId();

        WriteBatch batch = mFirestore.batch();

        Cursor cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_PLAYERS,
                null, null, null, null);
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

            final DocumentReference docRef = mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(PLAYERS_COLLECTION)
                    .document(String.valueOf(playerId)).collection(PLAYER_LOGS).document(String.valueOf(logId));

            PlayerLog playerLog = new PlayerLog(playerId, gameRBI, gameRun, game1b, game2b, game3b, gameHR, gameOuts, gameBB, gameSF);
            batch.set(docRef, playerLog);
        }

        cursor.close();

        cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_TEAMS, null,
                null, null, null);
        while (cursor.moveToNext()) {
            long logId = cursor.getLong(cursor.getColumnIndex(StatsEntry.COLUMN_LOG_ID));
            long teamId = cursor.getLong(cursor.getColumnIndex(StatsEntry.COLUMN_TEAM_ID));
            int gameWins = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_WINS));
            int gameLosses = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_LOSSES));
            int gameTies = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_TIES));
            int gameRunsScored = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_RUNSFOR));
            int gameRunsAllowed = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_RUNSAGAINST));

            final DocumentReference docRef = mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(TEAMS_COLLECTION)
                    .document(String.valueOf(teamId)).collection(TEAM_LOGS).document(String.valueOf(logId));

            TeamLog teamLog = new TeamLog(teamId, gameWins, gameLosses, gameTies, gameRunsScored, gameRunsAllowed);
            batch.set(docRef, teamLog);
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Transaction success!");
                mContext.getContentResolver().delete(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, null, null);
                mContext.getContentResolver().delete(StatsEntry.CONTENT_URI_BACKUP_TEAMS, null, null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Transaction failure.", e);
            }
        });
        cursor.close();

    }

    public interface onFirestoreSyncListener {
        void onFirestoreSync();

        void onSyncStart(int numberOf, boolean teams);

        void onSyncUpdate(boolean teams);

        void onSyncError();
    }
}
