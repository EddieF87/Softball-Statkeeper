package com.example.android.scorekeepdraft1.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eddie on 11/7/2017.
 */

public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper: ";
    public static final String LEAGUE_COLLECTION = "leagues";
    public static final String PLAYERS_COLLECTION = "players";
    public static final String TEAMS_COLLECTION = "teams";
    public static final String DELETION_COLLECTION = "deletion";
    public static final String PLAYER_LOGS = "playerlogs";
    public static final String TEAM_LOGS = "teamlogs";
    public static final String LAST_UPDATE = "last_update";
    public static final String UPDATE_SETTINGS = "_updateSettings";
    public static final String USERS = "users";
    private String leagueID;

    private onFirestoreSyncListener mListener;

    private Context mContext;
    private FirebaseFirestore mFirestore;

    public FirestoreHelper(Context context, String id) {
        this.mContext = context;
        this.leagueID = id;
        mFirestore = FirebaseFirestore.getInstance();

        if (context instanceof onFirestoreSyncListener) {
            mListener = (onFirestoreSyncListener) context;
        }
    }

    public void checkForUpdate() {
        Log.d("xxx", "checkForUpdate");

        final long localTimeStamp = getLocalTimeStamp();

        mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            long cloudTimeStamp = getCloudTimeStamp(task.getResult());
                            Log.d("xxx", "cloudTimeStamp = " + cloudTimeStamp);

                            if (cloudTimeStamp > localTimeStamp) {
                                Log.d("xxx", "onUpdateCheck(true)");
                                mListener.onUpdateCheck(true);
                            } else {
                                Log.d("xxx", "onUpdateCheck(false)");
                                mListener.onUpdateCheck(false);
                            }
                        } else {
                            Log.d("xxx", "onUpdateCheck(false)");

                            mListener.onUpdateCheck(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mListener.onUpdateCheck(false);
                    }
                });
    }

    private long getNewTimeStamp() {
        return System.currentTimeMillis();
    }

    public void updateAfterSync() {
        Log.d("xxx", "updateAfterSync");

        mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            long cloudTimeStamp = getCloudTimeStamp(task.getResult());
                            updateLocalTimeStamp(cloudTimeStamp);
                        }
                    }
                });
    }

    public long getLocalTimeStamp() {
        SharedPreferences updatePreferences = mContext.getSharedPreferences(leagueID + UPDATE_SETTINGS, Context.MODE_PRIVATE);
        return updatePreferences.getLong(LAST_UPDATE, 0);
    }

    public long getCloudTimeStamp(DocumentSnapshot documentSnapshot) {
        Map<String, Object> data = documentSnapshot.getData();
        long cloudTimeStamp;
        Object object = data.get(LAST_UPDATE);
        if (object == null) {
            cloudTimeStamp = 0;
            updateCloudTimeStamp(cloudTimeStamp);
        } else {
            cloudTimeStamp = (long) object;
        }
        return cloudTimeStamp;
    }

    public void updateTimeStamps() {

        final long newTimeStamp = getNewTimeStamp();
        final long localTimeStamp = getLocalTimeStamp();

        mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            long cloudTimeStamp = getCloudTimeStamp(task.getResult());

                            if (localTimeStamp >= cloudTimeStamp) {
                                updateLocalTimeStamp(newTimeStamp);
                            }
                            updateCloudTimeStamp(newTimeStamp);
                        }
                    }
                });

    }

    private void updateLocalTimeStamp(long timestamp) {
        SharedPreferences updatePreferences = mContext.getSharedPreferences(leagueID + UPDATE_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = updatePreferences.edit();
        editor.putLong(LAST_UPDATE, timestamp);
        editor.apply();
    }

    private void updateCloudTimeStamp(long timestamp) {
        DocumentReference leagueDoc = mFirestore.collection(LEAGUE_COLLECTION).document(leagueID);
        leagueDoc.update(LAST_UPDATE, timestamp);
    }

    public void deletionCheck(long localTimeStamp) {
        Log.d("xxx", "FIRESTORE DELETION CHECK");

        Log.d("xxx", "localTimeStamp = " + localTimeStamp);

        mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(DELETION_COLLECTION)
                .whereGreaterThanOrEqualTo("time", localTimeStamp).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("xxx", "filtered_deletionQuery");
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                Map<String, Object> data = documentSnapshot.getData();
                                long time = (long) data.get("time");
                                long type = (long) data.get("type");
                                String firestoreID = documentSnapshot.getId();
                                Log.d("xxx", "filtered_deletionQuery  =  time: " + time + "   id: " + firestoreID);
                                Uri uri;
                                if (type == 0) {
                                    uri = StatsEntry.CONTENT_URI_TEAMS;
                                } else {
                                    uri = StatsEntry.CONTENT_URI_PLAYERS;
                                }
                                String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
                                String[] selectionArgs = new String[]{firestoreID};
                                mContext.getContentResolver().delete(uri, selection, selectionArgs);
                            }
                        } else {
                            Log.d("xxx", "filtered_deletionQueryError");
                        }
                    }
                });
    }

    public void syncStats() {
        Log.d("xxx", "syncStats");
        long localTimeStamp = getLocalTimeStamp();

        if (leagueID == null) {
            try {
                MyApp myApp = (MyApp) mContext.getApplicationContext();
                leagueID = myApp.getCurrentSelection().getId();
            } catch (Exception e) {
                return;
            }
        }

        deletionCheck(localTimeStamp);

        mListener.onFirestoreSync();

        updatePlayers(localTimeStamp);

        //Get the collection of all teams in a league
        updateTeams(localTimeStamp);
    }

    private void updatePlayers(long localTimeStamp) {
        mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(PLAYERS_COLLECTION)
//                .whereGreaterThanOrEqualTo("update", localTimeStamp)
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

                                mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(PLAYERS_COLLECTION)
                                        .document(playerIdString).collection(PLAYER_LOGS)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {

                                                    QuerySnapshot querySnapshot = task.getResult();
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

                                                    for (DocumentSnapshot document : querySnapshot) {
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

                                                    if (querySnapshot.size() > 0) {
                                                        Log.d("xxx", "writebatch");
                                                        WriteBatch batch = mFirestore.batch();
                                                        batch.set(docRef, player);

                                                        for (DocumentSnapshot snapshot : querySnapshot) {
                                                            batch.delete(snapshot.getReference());
                                                        }
                                                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                            }
                                                        });
                                                    }

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
                                                        values.put(StatsEntry.COLUMN_GENDER, player.getGender());
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

    }

    private void updateTeams(long localTimeStamp) {
        mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(TEAMS_COLLECTION)
                //                .whereGreaterThanOrEqualTo("update", localTimeStamp)
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

                                                    QuerySnapshot querySnapshot = task.getResult();
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

                                                    if (querySnapshot.size() > 0) {
                                                        WriteBatch batch = mFirestore.batch();
                                                        batch.set(docRef, team);

                                                        for (DocumentSnapshot snapshot : querySnapshot) {
                                                            batch.delete(snapshot.getReference());
                                                        }
                                                        batch.commit();
                                                    }

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

    public void setUpdate(String firestoreID, int type) {
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }

        long timeStamp = System.currentTimeMillis();
        String collection;

        if(type == 0) {
            collection = FirestoreHelper.TEAMS_COLLECTION;
        } else if (type == 1) {
            collection = FirestoreHelper.PLAYERS_COLLECTION;
        } else {
            return;
        }

        DocumentReference documentReference = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(leagueID)
                .collection(collection).document(firestoreID);
        documentReference.update("update", timeStamp);
    }

    public void addDeletion(final String firestoreID, final int type) {
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }
        mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(leagueID)
                .collection(FirestoreHelper.PLAYERS_COLLECTION).document(firestoreID)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setDeletionDoc(leagueID, firestoreID, type);
                Log.d("xxx", "DocumentSnapshot successfully deleted!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    private void setDeletionDoc(String leagueID, String firestoreID, int type) {
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }

        DocumentReference deletionDoc = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(leagueID)
                .collection(FirestoreHelper.DELETION_COLLECTION).document(firestoreID);

        Map<String, Object> deletion = new HashMap<>();
        long time = System.currentTimeMillis();
        deletion.put("time", time);
        deletion.put("type", type);

        deletionDoc.set(deletion);
        updateTimeStamps();
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
        void onUpdateCheck(boolean update);

        void onFirestoreSync();

        void onSyncStart(int numberOf, boolean teams);

        void onSyncUpdate(boolean teams);

        void onSyncError();
    }

    public void addPlayerStatsToDB() {

        ArrayList<Long> playerList = new ArrayList<>();
        String selection = StatsEntry.COLUMN_PLAYERID + "=?";

        Cursor cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                null, null, null);

        final int playerIdIndex = cursor.getColumnIndex(StatsEntry.COLUMN_PLAYERID);
        final int singleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_1B);
        final int doubleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_2B);
        final int tripleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_3B);
        final int hrIndex = cursor.getColumnIndex(StatsEntry.COLUMN_HR);
        final int bbIndex = cursor.getColumnIndex(StatsEntry.COLUMN_BB);
        final int sfIndex = cursor.getColumnIndex(StatsEntry.COLUMN_SF);
        final int playerOutIndex = cursor.getColumnIndex(StatsEntry.COLUMN_OUT);
        final int playerRunIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RUN);
        final int rbiIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RBI);

        while (cursor.moveToNext()) {
            long playerId = cursor.getLong(playerIdIndex);
            playerList.add(playerId);
        }

        WriteBatch playerBatch = mFirestore.batch();

        for (long playerId : playerList) {
            String[] selectionArgs = new String[]{String.valueOf(playerId)};

            cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                    selection, selectionArgs, null);
            cursor.moveToFirst();
            int gameRBI = cursor.getInt(rbiIndex);
            int gameRun = cursor.getInt(playerRunIndex);
            int game1b = cursor.getInt(singleIndex);
            int game2b = cursor.getInt(doubleIndex);
            int game3b = cursor.getInt(tripleIndex);
            int gameHR = cursor.getInt(hrIndex);
            int gameOuts = cursor.getInt(playerOutIndex);
            int gameBB = cursor.getInt(bbIndex);
            int gameSF = cursor.getInt(sfIndex);

            int firestoreIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
            String firestoreID = cursor.getString(firestoreIDIndex);

            long logId;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                logId = new Date().getTime();
            } else {
                logId = System.currentTimeMillis();
            }

            final DocumentReference docRef = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION)
                    .document(leagueID).collection(FirestoreHelper.PLAYERS_COLLECTION).document(firestoreID)
                    .collection(FirestoreHelper.PLAYER_LOGS).document(String.valueOf(logId));

            PlayerLog playerLog = new PlayerLog(playerId, gameRBI, gameRun, game1b, game2b, game3b,
                    gameHR, gameOuts, gameBB, gameSF);
            playerBatch.set(docRef, playerLog);

            Uri playerUri = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_PLAYERS, playerId);
            cursor = mContext.getContentResolver().query(playerUri, null, null, null, null);
            cursor.moveToFirst();

            int pRBIIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RBI);
            int pRBI = cursor.getInt(pRBIIndex);
            int pRunIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RUN);
            int pRun = cursor.getInt(pRunIndex);
            int p1bIndex = cursor.getColumnIndex(StatsEntry.COLUMN_1B);
            int p1b = cursor.getInt(p1bIndex);
            int p2bIndex = cursor.getColumnIndex(StatsEntry.COLUMN_2B);
            int p2b = cursor.getInt(p2bIndex);
            int p3bIndex = cursor.getColumnIndex(StatsEntry.COLUMN_3B);
            int p3b = cursor.getInt(p3bIndex);
            int pHRIndex = cursor.getColumnIndex(StatsEntry.COLUMN_HR);
            int pHR = cursor.getInt(pHRIndex);
            int pOutsIndex = cursor.getColumnIndex(StatsEntry.COLUMN_OUT);
            int pOuts = cursor.getInt(pOutsIndex);
            int pBBIndex = cursor.getColumnIndex(StatsEntry.COLUMN_BB);
            int pBB = cursor.getInt(pBBIndex);
            int pSFIndex = cursor.getColumnIndex(StatsEntry.COLUMN_SF);
            int pSF = cursor.getInt(pSFIndex);
            int gamesPlayedIndex = cursor.getColumnIndex(StatsEntry.COLUMN_G);
            int games = cursor.getInt(gamesPlayedIndex);
            firestoreIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
            firestoreID = cursor.getString(firestoreIDIndex);

            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_1B, p1b + game1b);
            values.put(StatsEntry.COLUMN_2B, p2b + game2b);
            values.put(StatsEntry.COLUMN_3B, p3b + game3b);
            values.put(StatsEntry.COLUMN_HR, pHR + gameHR);
            values.put(StatsEntry.COLUMN_RUN, pRun + gameRun);
            values.put(StatsEntry.COLUMN_RBI, pRBI + gameRBI);
            values.put(StatsEntry.COLUMN_BB, pBB + gameBB);
            values.put(StatsEntry.COLUMN_OUT, pOuts + gameOuts);
            values.put(StatsEntry.COLUMN_SF, pSF + gameSF);
            values.put(StatsEntry.COLUMN_G, games + 1);
            values.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
            mContext.getContentResolver().update(playerUri, values, null, null);
        }
        playerBatch.commit().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("playerBatch failure", e);
                Cursor cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                        null, null, null);
                while (cursor.moveToNext()) {
                    long logId;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        logId = new Date().getTime();
                    } else {
                        logId = System.currentTimeMillis();
                    }
                    int playerId = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_PLAYERID));
                    int gameRBI = cursor.getInt(rbiIndex);
                    int gameRun = cursor.getInt(playerRunIndex);
                    int game1b = cursor.getInt(singleIndex);
                    int game2b = cursor.getInt(doubleIndex);
                    int game3b = cursor.getInt(tripleIndex);
                    int gameHR = cursor.getInt(hrIndex);
                    int gameOuts = cursor.getInt(playerOutIndex);
                    int gameBB = cursor.getInt(bbIndex);
                    int gameSF = cursor.getInt(sfIndex);

                    ContentValues backupValues = new ContentValues();
                    backupValues.put(StatsEntry.COLUMN_PLAYERID, playerId);
                    backupValues.put(StatsEntry.COLUMN_LOG_ID, logId);
                    backupValues.put(StatsEntry.COLUMN_1B, game1b);
                    backupValues.put(StatsEntry.COLUMN_2B, game2b);
                    backupValues.put(StatsEntry.COLUMN_3B, game3b);
                    backupValues.put(StatsEntry.COLUMN_HR, gameHR);
                    backupValues.put(StatsEntry.COLUMN_RUN, gameRun);
                    backupValues.put(StatsEntry.COLUMN_RBI, gameRBI);
                    backupValues.put(StatsEntry.COLUMN_BB, gameBB);
                    backupValues.put(StatsEntry.COLUMN_OUT, gameOuts);
                    backupValues.put(StatsEntry.COLUMN_SF, gameSF);

                    mContext.getContentResolver().insert(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, backupValues);
                }
            }
        });
    }

    public void addTeamStatsToDB(String teamName, int teamRuns, int otherTeamRuns) {
        WriteBatch teamBatch = mFirestore.batch();

        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {teamName};
        Cursor cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS, null,
                selection, selectionArgs, null
        );
        cursor.moveToFirst();
        ContentValues values = new ContentValues();
        final ContentValues backupValues = new ContentValues();

        long logId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            logId = new Date().getTime();
        } else {
            logId = System.currentTimeMillis();
        }

        long teamId = cursor.getLong(cursor.getColumnIndex(StatsEntry._ID));
        TeamLog teamLog = new TeamLog(teamId, teamRuns, otherTeamRuns);
        backupValues.put(StatsEntry.COLUMN_TEAM_ID, teamId);

        int firestoreIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
        String firestoreID = cursor.getString(firestoreIDIndex);

        final DocumentReference docRef = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION)
                .document(leagueID).collection(FirestoreHelper.TEAMS_COLLECTION).document(firestoreID)
                .collection(FirestoreHelper.TEAM_LOGS).document(String.valueOf(logId));

        if (teamRuns > otherTeamRuns) {
            int valueIndex = cursor.getColumnIndex(StatsEntry.COLUMN_WINS);
            int newValue = cursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_WINS, newValue);
            backupValues.put(StatsEntry.COLUMN_WINS, 1);
            teamLog.setWins(1);
        } else if (otherTeamRuns > teamRuns) {
            int valueIndex = cursor.getColumnIndex(StatsEntry.COLUMN_LOSSES);
            int newValue = cursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_LOSSES, newValue);
            backupValues.put(StatsEntry.COLUMN_LOSSES, 1);
            teamLog.setLosses(1);
        } else {
            int valueIndex = cursor.getColumnIndex(StatsEntry.COLUMN_TIES);
            int newValue = cursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_TIES, newValue);
            backupValues.put(StatsEntry.COLUMN_TIES, 1);
            teamLog.setTies(1);
        }

        int valueIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RUNSFOR);
        int newValue = cursor.getInt(valueIndex) + teamRuns;
        values.put(StatsEntry.COLUMN_RUNSFOR, newValue);
        backupValues.put(StatsEntry.COLUMN_RUNSFOR, teamRuns);

        valueIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RUNSAGAINST);
        newValue = cursor.getInt(valueIndex) + otherTeamRuns;
        values.put(StatsEntry.COLUMN_RUNSAGAINST, newValue);
        backupValues.put(StatsEntry.COLUMN_RUNSAGAINST, otherTeamRuns);

        values.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);

        mContext.getContentResolver().update(StatsEntry.CONTENT_URI_TEAMS, values, selection, selectionArgs);

        teamBatch.set(docRef, teamLog);
        teamBatch.commit().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("teamBatch failure", e);
                mContext.getContentResolver().insert(StatsEntry.CONTENT_URI_BACKUP_TEAMS, backupValues);
            }
        });
    }

}
