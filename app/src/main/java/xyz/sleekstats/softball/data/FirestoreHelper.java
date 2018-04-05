package xyz.sleekstats.softball.data;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.objects.Player;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.objects.PlayerLog;
import xyz.sleekstats.softball.objects.TeamLog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Eddie on 11/7/2017.
 */

public class FirestoreHelper extends IntentService {
    public static final String LEAGUE_COLLECTION = "leagues";
    public static final String PLAYERS_COLLECTION = "players";
    public static final String TEAMS_COLLECTION = "teams";
    public static final String BOXSCORE_COLLECTION = "boxscores";
    public static final String DELETION_COLLECTION = "deletion";
    public static final String PLAYER_LOGS = "playerlogs";
    public static final String TEAM_LOGS = "teamlogs";
    public static final String LAST_UPDATE = "last_update";
    public static final String UPDATE_SETTINGS = "_updateSettings";
    public static final String USERS = "users";
    public static final String REQUESTS = "requests";
    public static final String KEY_DELETE_PLAYERS = "playersToDelete";

    public static final String STATKEEPER_ID = "statkeeperID";
    public static final String INTENT_ADD_PLAYER_STATS = "addPlayerStats";
    public static final String INTENT_ADD_TEAM_STATS = "addTeamStats";
    public static final String INTENT_UPDATE_PLAYER = "updatePlayer";
    public static final String INTENT_DELETE_PLAYER = "delete";
    public static final String INTENT_DELETE_PLAYERS = "deleteList";
    public static final String INTENT_RETRY_GAME_LOAD = "retry";
    public static final String INTENT_ADD_BOXSCORE = "boxScore";

    private String statKeeperID;
    private long mUpdateTime;
    private FirebaseFirestore mFirestore;

    public FirestoreHelper() {
        super("FirestoreHelper");
    }

    public void addDeletion(final String firestoreID, final int type, final String name, final int gender, final String teamFireID) {
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }
        mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(statKeeperID)
                .collection(FirestoreHelper.PLAYERS_COLLECTION).document(firestoreID)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setDeletionDoc(statKeeperID, firestoreID, type, name, gender, teamFireID);
            }
        });
    }

    public void addDeletionList(List<Player> playersToDelete) {
        if (playersToDelete.isEmpty()) {
            return;
        }
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }
        CollectionReference playersCollection = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(statKeeperID)
                .collection(FirestoreHelper.PLAYERS_COLLECTION);
        CollectionReference deletionCollection = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(statKeeperID)
                .collection(FirestoreHelper.DELETION_COLLECTION);

        WriteBatch batch = mFirestore.batch();

        for (Player player : playersToDelete) {
            String fireID = player.getFirestoreID();
            String teamFireID = player.getTeamfirestoreid();
            String name = player.getName();
            int gender = player.getGender();
            batch.delete(playersCollection.document(fireID));

            Map<String, Object> deletion = new HashMap<>();
            deletion.put(StatsEntry.TIME, mUpdateTime);
            deletion.put(StatsEntry.TYPE, 1);
            deletion.put(StatsEntry.COLUMN_NAME, name);
            deletion.put(StatsEntry.COLUMN_GENDER, gender);
            deletion.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamFireID);

            batch.set(deletionCollection.document(fireID), deletion, SetOptions.merge());
        }
    }

    private void setDeletionDoc(String leagueID, String firestoreID, int type, String name, int gender, String team) {
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }

        DocumentReference deletionDoc = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(leagueID)
                .collection(FirestoreHelper.DELETION_COLLECTION).document(firestoreID);

        Map<String, Object> deletion = new HashMap<>();
        long time = System.currentTimeMillis();
        deletion.put(StatsEntry.TIME, time);
        deletion.put(StatsEntry.TYPE, type);
        deletion.put(StatsEntry.COLUMN_NAME, name);
        if (type == 1) {
            deletion.put(StatsEntry.COLUMN_GENDER, gender);
            deletion.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, team);
        }

        deletionDoc.set(deletion, SetOptions.merge());
    }

    public void addPlayerStatsToDB() {
        Log.d("zztop", "addPlayerStatsToDB");

        Cursor backupPlayerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, null,
                null, null, null);

        WriteBatch playerBatch = mFirestore.batch();


        while (backupPlayerCursor.moveToNext()) {

            long playerId = StatsContract.getColumnInt(backupPlayerCursor, StatsEntry.COLUMN_PLAYERID);
            String firestoreID = StatsContract.getColumnString(backupPlayerCursor, StatsEntry.COLUMN_FIRESTORE_ID);
            String teamfirestoreID = StatsContract.getColumnString(backupPlayerCursor, StatsEntry.COLUMN_TEAM_FIRESTORE_ID);
            int game1b = StatsContract.getColumnInt(backupPlayerCursor, StatsEntry.COLUMN_1B);
            int game2b = StatsContract.getColumnInt(backupPlayerCursor, StatsEntry.COLUMN_2B);
            int game3b = StatsContract.getColumnInt(backupPlayerCursor, StatsEntry.COLUMN_3B);
            int gameHR = StatsContract.getColumnInt(backupPlayerCursor, StatsEntry.COLUMN_HR);
            int gameRun = StatsContract.getColumnInt(backupPlayerCursor, StatsEntry.COLUMN_RUN);
            int gameRBI = StatsContract.getColumnInt(backupPlayerCursor, StatsEntry.COLUMN_RBI);
            int gameOuts = StatsContract.getColumnInt(backupPlayerCursor, StatsEntry.COLUMN_OUT);
            int gameBB = StatsContract.getColumnInt(backupPlayerCursor, StatsEntry.COLUMN_BB);
            int gameSF = StatsContract.getColumnInt(backupPlayerCursor, StatsEntry.COLUMN_SF);

            ContentValues boxscoreValues = new ContentValues();
            boxscoreValues.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
            boxscoreValues.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamfirestoreID);
            boxscoreValues.put(StatsEntry.COLUMN_GAME_ID, mUpdateTime);
            boxscoreValues.put(StatsEntry.COLUMN_1B, game1b);
            boxscoreValues.put(StatsEntry.COLUMN_2B, game2b);
            boxscoreValues.put(StatsEntry.COLUMN_3B, game3b);
            boxscoreValues.put(StatsEntry.COLUMN_HR, gameHR);
            boxscoreValues.put(StatsEntry.COLUMN_RUN, gameRun);
            boxscoreValues.put(StatsEntry.COLUMN_RBI, gameRBI);
            boxscoreValues.put(StatsEntry.COLUMN_BB, gameBB);
            boxscoreValues.put(StatsEntry.COLUMN_OUT, gameOuts);
            boxscoreValues.put(StatsEntry.COLUMN_SF, gameSF);
            getContentResolver().insert(StatsEntry.CONTENT_URI_BOXSCORE_PLAYERS, boxscoreValues);

            final DocumentReference playerRef = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION)
                    .document(statKeeperID).collection(FirestoreHelper.PLAYERS_COLLECTION).document(firestoreID);

            final DocumentReference playerLogRef =
                    playerRef.collection(FirestoreHelper.PLAYER_LOGS).document(String.valueOf(mUpdateTime));

            PlayerLog playerLog = new PlayerLog(playerId, gameRBI, gameRun, game1b, game2b, game3b,
                    gameHR, gameOuts, gameBB, gameSF);
            playerBatch.set(playerLogRef, playerLog);

            Uri playerUri = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_PLAYERS, playerId);
            Cursor permanentPlayerCursor = getContentResolver().query(playerUri, null, null, null, null);
            permanentPlayerCursor.moveToFirst();

            firestoreID = StatsContract.getColumnString(permanentPlayerCursor, StatsEntry.COLUMN_FIRESTORE_ID);
            int p1b = StatsContract.getColumnInt(permanentPlayerCursor, StatsEntry.COLUMN_1B);
            int p2b = StatsContract.getColumnInt(permanentPlayerCursor, StatsEntry.COLUMN_2B);
            int p3b = StatsContract.getColumnInt(permanentPlayerCursor, StatsEntry.COLUMN_3B);
            int pHR = StatsContract.getColumnInt(permanentPlayerCursor, StatsEntry.COLUMN_HR);
            int pRun = StatsContract.getColumnInt(permanentPlayerCursor, StatsEntry.COLUMN_RUN);
            int pRBI = StatsContract.getColumnInt(permanentPlayerCursor, StatsEntry.COLUMN_RBI);
            int pBB = StatsContract.getColumnInt(permanentPlayerCursor, StatsEntry.COLUMN_BB);
            int pOuts = StatsContract.getColumnInt(permanentPlayerCursor, StatsEntry.COLUMN_OUT);
            int pSF = StatsContract.getColumnInt(permanentPlayerCursor, StatsEntry.COLUMN_SF);
            int pGames = StatsContract.getColumnInt(permanentPlayerCursor, StatsEntry.COLUMN_G);
            permanentPlayerCursor.close();

            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
            values.put(StatsEntry.COLUMN_1B, p1b + game1b);
            values.put(StatsEntry.COLUMN_2B, p2b + game2b);
            values.put(StatsEntry.COLUMN_3B, p3b + game3b);
            values.put(StatsEntry.COLUMN_HR, pHR + gameHR);
            values.put(StatsEntry.COLUMN_RUN, pRun + gameRun);
            values.put(StatsEntry.COLUMN_RBI, pRBI + gameRBI);
            values.put(StatsEntry.COLUMN_BB, pBB + gameBB);
            values.put(StatsEntry.COLUMN_OUT, pOuts + gameOuts);
            values.put(StatsEntry.COLUMN_SF, pSF + gameSF);
            values.put(StatsEntry.COLUMN_G, pGames + 1);
            Log.d("zztop", firestoreID + "  hr: " + gameHR);
            getContentResolver().update(playerUri, values, null, null);

            playerBatch.update(playerRef, StatsEntry.UPDATE, mUpdateTime);
        }
        backupPlayerCursor.close();

        playerBatch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getContentResolver().delete(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, null, null);
                    }
                });
    }

    public void addTeamStatsToDB(final String teamFirestoreID, int teamRuns, int otherTeamRuns) {

        WriteBatch teamBatch = mFirestore.batch();

        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = {teamFirestoreID};
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS, null,
                selection, selectionArgs, null);
        cursor.moveToFirst();
        ContentValues values = new ContentValues();
        final ContentValues backupValues = new ContentValues();

        long teamId = StatsContract.getColumnLong(cursor, StatsEntry._ID);
        TeamLog teamLog = new TeamLog(teamId, teamRuns, otherTeamRuns);
        backupValues.put(StatsEntry.COLUMN_TEAM_ID, teamId);

        final DocumentReference teamRef = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION)
                .document(statKeeperID).collection(FirestoreHelper.TEAMS_COLLECTION).document(teamFirestoreID);
        final DocumentReference teamLogRef = teamRef.collection(FirestoreHelper.TEAM_LOGS).document(String.valueOf(mUpdateTime));

        if (teamRuns > otherTeamRuns) {
            int newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_WINS) + 1;
            values.put(StatsEntry.COLUMN_WINS, newValue);
            backupValues.put(StatsEntry.COLUMN_WINS, 1);
            teamLog.setWins(1);
        } else if (otherTeamRuns > teamRuns) {
            int newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_LOSSES) + 1;
            values.put(StatsEntry.COLUMN_LOSSES, newValue);
            backupValues.put(StatsEntry.COLUMN_LOSSES, 1);
            teamLog.setLosses(1);
        } else {
            int newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_TIES) + 1;
            values.put(StatsEntry.COLUMN_TIES, newValue);
            backupValues.put(StatsEntry.COLUMN_TIES, 1);
            teamLog.setTies(1);
        }

        int newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUNSFOR) + teamRuns;
        values.put(StatsEntry.COLUMN_RUNSFOR, newValue);
        backupValues.put(StatsEntry.COLUMN_RUNSFOR, teamRuns);

        newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUNSAGAINST) + otherTeamRuns;
        values.put(StatsEntry.COLUMN_RUNSAGAINST, newValue);
        backupValues.put(StatsEntry.COLUMN_RUNSAGAINST, otherTeamRuns);
        cursor.close();

        values.put(StatsEntry.COLUMN_FIRESTORE_ID, teamFirestoreID);

        getContentResolver().update(StatsEntry.CONTENT_URI_TEAMS, values, selection, selectionArgs);

        teamBatch.update(teamRef, StatsEntry.UPDATE, mUpdateTime);
        teamBatch.set(teamLogRef, teamLog);

        teamBatch.commit().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                getContentResolver().insert(StatsEntry.CONTENT_URI_BACKUP_TEAMS, backupValues);
            }
        });
    }

    public void addBoxScoreToDB(final String awayID, String homeID, int awayRuns, int homeRuns) {
        final ContentValues boxscoreValues = new ContentValues();
        boxscoreValues.put(StatsEntry.COLUMN_GAME_ID, mUpdateTime);
        boxscoreValues.put(StatsEntry.COLUMN_LOCAL, 1);
        boxscoreValues.put(StatsEntry.COLUMN_AWAY_TEAM, awayID);
        boxscoreValues.put(StatsEntry.COLUMN_HOME_TEAM, homeID);
        boxscoreValues.put(StatsEntry.COLUMN_AWAY_RUNS, awayRuns);
        boxscoreValues.put(StatsEntry.COLUMN_HOME_RUNS, homeRuns);

        Map<String, Object> boxscoreMap = new HashMap<>();
        boxscoreMap.put(StatsEntry.COLUMN_GAME_ID, mUpdateTime);
        boxscoreMap.put(StatsEntry.COLUMN_AWAY_TEAM, awayID);
        boxscoreMap.put(StatsEntry.COLUMN_HOME_TEAM, homeID);
        boxscoreMap.put(StatsEntry.COLUMN_AWAY_RUNS, awayRuns);
        boxscoreMap.put(StatsEntry.COLUMN_HOME_RUNS, homeRuns);
        final DocumentReference boxscoreRef = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION)
                .document(statKeeperID).collection(FirestoreHelper.BOXSCORE_COLLECTION).document(String.valueOf(mUpdateTime));
        boxscoreRef.set(boxscoreMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    boxscoreValues.put(StatsEntry.COLUMN_LOCAL, 1);
                } else {
                    boxscoreValues.put(StatsEntry.COLUMN_LOCAL, 0);
                }
                getContentResolver().insert(StatsEntry.CONTENT_URI_BOXSCORE_OVERVIEWS, boxscoreValues);
            }
        });
    }


    public void retryGameLogLoad() {
        MyApp myApp = (MyApp) getApplicationContext();
        String leagueID = myApp.getCurrentSelection().getId();
        Log.d("zztop", "retryGameLogLoad");

        WriteBatch batch = mFirestore.batch();

        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_PLAYERS,
                null, null, null, null);
        while (cursor.moveToNext()) {
            String playerFirestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
            long gameID = StatsContract.getColumnLong(cursor, StatsEntry.COLUMN_GAME_ID);
            long playerId = StatsContract.getColumnLong(cursor, StatsEntry.COLUMN_PLAYERID);
            int gameRBI = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RBI);
            int gameRun = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUN);
            int game1b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_1B);
            int game2b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_2B);
            int game3b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_3B);
            int gameHR = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HR);
            int gameOuts = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_OUT);
            int gameBB = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_BB);
            int gameSF = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_SF);

            final DocumentReference docRef = mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(PLAYERS_COLLECTION)
                    .document(playerFirestoreID).collection(PLAYER_LOGS).document(String.valueOf(gameID));

            PlayerLog playerLog = new PlayerLog(playerId, gameRBI, gameRun, game1b, game2b, game3b, gameHR, gameOuts, gameBB, gameSF);
            batch.set(docRef, playerLog);
        }

        cursor.close();

        cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_TEAMS, null,
                null, null, null);
        while (cursor.moveToNext()) {
            long gameID = StatsContract.getColumnLong(cursor, StatsEntry.COLUMN_GAME_ID);
            long teamId = StatsContract.getColumnLong(cursor, StatsEntry.COLUMN_TEAM_ID);
            int gameWins = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_WINS);
            int gameLosses = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_LOSSES);
            int gameTies = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_TIES);
            int gameRunsScored = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUNSFOR);
            int gameRunsAllowed = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUNSAGAINST);
            String teamFirestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);

            final DocumentReference docRef = mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(TEAMS_COLLECTION)
                    .document(teamFirestoreID).collection(TEAM_LOGS).document(String.valueOf(gameID));

            TeamLog teamLog = new TeamLog(teamId, gameWins, gameLosses, gameTies, gameRunsScored, gameRunsAllowed);
            batch.set(docRef, teamLog);
        }

        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                getContentResolver().delete(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, null, null);
                getContentResolver().delete(StatsEntry.CONTENT_URI_BACKUP_TEAMS, null, null);
            }
        });
        cursor.close();
    }

    public void updatePlayer(final String firestoreID, final PlayerLog playerLog){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final DocumentReference keeperRef = firestore.collection(FirestoreHelper.LEAGUE_COLLECTION)
                .document(statKeeperID);
        keeperRef.update(LAST_UPDATE, mUpdateTime);

        final DocumentReference playerRef = keeperRef.collection(FirestoreHelper.PLAYERS_COLLECTION)
                .document(firestoreID);
        playerRef.update(StatsEntry.UPDATE, mUpdateTime);

        final DocumentReference playerLogRef = playerRef.collection(FirestoreHelper.PLAYER_LOGS).document();
        playerLogRef.set(playerLog).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ContentValues values = new ContentValues();
                values.put(StatsEntry.COLUMN_BB, - playerLog.getWalks());
                values.put(StatsEntry.COLUMN_1B, - playerLog.getSingles());
                values.put(StatsEntry.COLUMN_2B, - playerLog.getDoubles());
                values.put(StatsEntry.COLUMN_3B, - playerLog.getTriples());
                values.put(StatsEntry.COLUMN_HR, - playerLog.getHrs());
                values.put(StatsEntry.COLUMN_OUT, - playerLog.getOuts());
                values.put(StatsEntry.COLUMN_SF, - playerLog.getSacfly());
                values.put(StatsEntry.COLUMN_RBI, - playerLog.getRbi());
                values.put(StatsEntry.COLUMN_RUN, - playerLog.getRuns());

                String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
                String[] selectionArgs = new String[]{firestoreID};
                getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS, values, selection, selectionArgs);
            }
        });
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }
        statKeeperID = intent.getStringExtra(STATKEEPER_ID);
        mUpdateTime = intent.getLongExtra(TimeStampUpdater.UPDATE_TIME, 0);

        String firestoreID;
        switch (action) {
            case INTENT_ADD_PLAYER_STATS:
                Log.d("zztop", "addPlayerStatsToDB");
                addPlayerStatsToDB();
                break;

            case INTENT_ADD_TEAM_STATS:
                firestoreID = intent.getStringExtra(StatsEntry.COLUMN_FIRESTORE_ID);
                int runsFor = intent.getIntExtra(StatsEntry.COLUMN_RUNSFOR, 0);
                int runsAgainst = intent.getIntExtra(StatsEntry.COLUMN_RUNSAGAINST, 0);
                addTeamStatsToDB(firestoreID, runsFor, runsAgainst);
                break;

            case INTENT_RETRY_GAME_LOAD:
                retryGameLogLoad();
                break;

            case INTENT_DELETE_PLAYER:
                firestoreID = intent.getStringExtra(StatsEntry.COLUMN_FIRESTORE_ID);
                String teamFirestoreID = intent.getStringExtra(StatsEntry.COLUMN_TEAM_FIRESTORE_ID);
                int type = intent.getIntExtra(StatsEntry.TYPE, -1);
                String name = intent.getStringExtra(StatsEntry.COLUMN_NAME);
                int gender = intent.getIntExtra(StatsEntry.COLUMN_GENDER, -1);
                addDeletion(firestoreID, type, name, gender, teamFirestoreID);
                break;

            case INTENT_DELETE_PLAYERS:
                List<Player> playersToDelete = intent.getParcelableArrayListExtra(KEY_DELETE_PLAYERS);
                addDeletionList(playersToDelete);
                break;

            case INTENT_ADD_BOXSCORE:
                String awayID = intent.getStringExtra(StatsEntry.COLUMN_AWAY_TEAM);
                String homeID = intent.getStringExtra(StatsEntry.COLUMN_HOME_TEAM);
                int awayRuns = intent.getIntExtra(StatsEntry.COLUMN_AWAY_RUNS, 0);
                int homeRuns = intent.getIntExtra(StatsEntry.COLUMN_HOME_RUNS, 0);
                addBoxScoreToDB(awayID, homeID, awayRuns, homeRuns);
                break;

            case INTENT_UPDATE_PLAYER:
                PlayerLog playerLog = intent.getParcelableExtra(StatsEntry.PLAYERS_TABLE_NAME);
                firestoreID = intent.getStringExtra(StatsEntry.COLUMN_FIRESTORE_ID);
                updatePlayer(firestoreID, playerLog);
                break;
        }
    }
}