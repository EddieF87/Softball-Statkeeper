package xyz.sleekstats.softball.data;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.sleekstats.softball.activities.UsersActivity;
import xyz.sleekstats.softball.models.ItemMarkedForDeletion;
import xyz.sleekstats.softball.models.MainPageSelection;
import xyz.sleekstats.softball.models.Player;
import xyz.sleekstats.softball.models.PlayerLog;
import xyz.sleekstats.softball.models.Team;
import xyz.sleekstats.softball.models.TeamLog;

import static xyz.sleekstats.softball.data.FirestoreUpdateService.*;
import static xyz.sleekstats.softball.data.StatsContract.StatsEntry;

public class FirestoreSyncService extends IntentService {


    private String mStatKeeperID;

    private FirebaseFirestore mFirestore;

    public static final String INTENT_CHECK_UPDATE = "checkupdate";
    public static final String INTENT_UPDATE_PLAYERS = "playerupdate";
    public static final String INTENT_UPDATE_TEAMS = "teamupdate";
    public static final String INTENT_UPDATE_BOXSCORES = "boxscoreupdate";
    public static final String INTENT_DELETION_CHECK = "deletioncheck";
    public static final String INTENT_DELETE_ITEMS = "deleteitems";
    public static final String INTENT_SAVE_ITEMS = "saveitems";
    public static final String INTENT_UPDATE_TIME = "updatetime";

    public static final String KEY_MAX = "progressmax";

    public static final int MSG_ERROR = -1;
    public static final int MSG_START_UPDATE = 1;
    public static final int MSG_PLAYER_UPDATED = 2;
    public static final int MSG_TEAM_UPDATED = 3;
    public static final int MSG_BOXSCORE_UPDATED = 4;
    public static final int MSG_PLAYER_MAX = 5;
    public static final int MSG_TEAM_MAX = 6;
    public static final int MSG_BOXSCORE_MAX = 7;
    public static final int MSG_OPEN_DELETION_DIALOG = 8;
    public static final int MSG_GO_TO_STATKEEPER = 9;

    private ResultReceiver mReceiver;

    public FirestoreSyncService() {
        super("FirestoreSyncService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mFirestore = FirebaseFirestore.getInstance();
        this.mStatKeeperID = intent.getStringExtra(StatsEntry.COLUMN_LEAGUE_ID);
        mReceiver = intent.getParcelableExtra(StatsEntry.SYNC);

        String action = intent.getAction();
        if (action == null) {
            return;
        }
        long localTimeStamp = TimeStampUpdater.getLocalTimeStamp(this, mStatKeeperID);
        ArrayList<ItemMarkedForDeletion> items;

        switch (action) {
            case INTENT_CHECK_UPDATE:
                checkForUpdate();
                return;

            case INTENT_UPDATE_PLAYERS:
                updatePlayers(localTimeStamp);
                return;

            case INTENT_UPDATE_TEAMS:
                String name = intent.getStringExtra(MainPageSelection.KEY_SELECTION_NAME);
                int type = intent.getIntExtra(MainPageSelection.KEY_SELECTION_TYPE, 0);
                updateTeams(localTimeStamp, name, type);
                return;

            case INTENT_UPDATE_BOXSCORES:
                updateBoxscores(localTimeStamp);
                return;

            case INTENT_UPDATE_TIME:
                updateAfterSync();
                break;

            case INTENT_DELETION_CHECK:
                int level = intent.getIntExtra(StatsEntry.LEVEL, 0);
                deletionCheck(level);
                return;

            case INTENT_DELETE_ITEMS:
                items = intent.getParcelableArrayListExtra(StatsEntry.DELETE);
                deleteItems(items);
                return;

            case INTENT_SAVE_ITEMS:
                items = intent.getParcelableArrayListExtra(StatsEntry.DELETE);
                saveItems(items);
                break;
        }
    }


    private void sndMsg(int msg) {
        mReceiver.send(msg, null);
    }

    private void sndMax(int msg, int max) {
        Bundle b = new Bundle();
        b.putInt(FirestoreSyncService.KEY_MAX, max);
        mReceiver.send(msg, b);
    }

    private void sndDeletions(ArrayList<ItemMarkedForDeletion> list) {
        Bundle b = new Bundle();
        b.putParcelableArrayList(StatsEntry.DELETE, list);
        mReceiver.send(MSG_OPEN_DELETION_DIALOG, b);
    }
    //SYNC CHECK

    private void checkForUpdate() {
        final long localTimeStamp = TimeStampUpdater.getLocalTimeStamp(this, mStatKeeperID);

        mFirestore.collection(LEAGUE_COLLECTION).document(mStatKeeperID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            long cloudTimeStamp = TimeStampUpdater.getCloudTimeStamp(task.getResult(), mStatKeeperID);

                            if (cloudTimeStamp > localTimeStamp) {
                                sndMsg(MSG_START_UPDATE);
                            } else {
                                sndMsg(MSG_GO_TO_STATKEEPER);
                            }
                        } else {
                            sndMsg(MSG_GO_TO_STATKEEPER);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        sndMsg(MSG_GO_TO_STATKEEPER);
                    }
                });
    }

    private void updatePlayers(long localTimeStamp) {
        mFirestore.collection(LEAGUE_COLLECTION).document(mStatKeeperID).collection(PLAYERS_COLLECTION)
                .whereGreaterThan(StatsEntry.UPDATE, localTimeStamp)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            QuerySnapshot querySnapshot = task.getResult();
                            final int numberOfPlayers = querySnapshot.size();
                            sndMax(MSG_PLAYER_MAX, numberOfPlayers);
                            for (DocumentSnapshot document : querySnapshot) {
                                final Player player = document.toObject(Player.class);
                                final String playerIdString = document.getId();

                                mFirestore.collection(LEAGUE_COLLECTION).document(mStatKeeperID).collection(PLAYERS_COLLECTION)
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
                                                    int sbs = 0;

                                                    for (DocumentSnapshot document : querySnapshot) {
                                                        String id = document.getId().substring(0, 5);
                                                        if(id.equals("ERASE")) {
                                                            games--;
                                                        } else {
                                                            games++;
                                                        }

                                                        PlayerLog playerLog = document.toObject(PlayerLog.class);
                                                        rbi += playerLog.getRbi();
                                                        runs += playerLog.getRuns();
                                                        singles += playerLog.getSingles();
                                                        doubles += playerLog.getDoubles();
                                                        triples += playerLog.getTriples();
                                                        hrs += playerLog.getHrs();
                                                        walks += playerLog.getWalks();
                                                        outs += playerLog.getOuts();
                                                        sfs += playerLog.getSacfly();
                                                        sbs += playerLog.getStolenbases();
                                                    }

                                                    final DocumentReference docRef = mFirestore.collection(LEAGUE_COLLECTION)
                                                            .document(mStatKeeperID).collection(PLAYERS_COLLECTION).document(playerIdString);

                                                    int totalGames = player.getGames() + games;
                                                    int totalSingles = player.getSingles() + singles;
                                                    int totalDoubles = player.getDoubles() + doubles;
                                                    int totalTriples = player.getTriples() + triples;
                                                    int totalHrs = player.getHrs() + hrs;
                                                    int totalWalks = player.getWalks() + walks;
                                                    int totalOuts = player.getOuts() + outs;
                                                    int totalRbis = player.getRbis() + rbi;
                                                    int totalRuns = player.getRuns() + runs;
                                                    int totalSFs = player.getSacFlies() + sfs;
                                                    int totalSBs = player.getStolenBases() + sbs;

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
                                                    player.setStolenBases(totalSBs);

                                                    if (querySnapshot.size() > 0) {
                                                        WriteBatch batch = mFirestore.batch();
                                                        batch.set(docRef, player, SetOptions.merge());

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
                                                    values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, player.getTeamfirestoreid());
                                                    values.put(StatsEntry.COLUMN_LEAGUE_ID, mStatKeeperID);
                                                    values.put(StatsEntry.COLUMN_1B, player.getSingles());
                                                    values.put(StatsEntry.COLUMN_2B, player.getDoubles());
                                                    values.put(StatsEntry.COLUMN_3B, player.getTriples());
                                                    values.put(StatsEntry.COLUMN_HR, player.getHrs());
                                                    values.put(StatsEntry.COLUMN_RUN, player.getRuns());
                                                    values.put(StatsEntry.COLUMN_RBI, player.getRbis());
                                                    values.put(StatsEntry.COLUMN_BB, player.getWalks());
                                                    values.put(StatsEntry.COLUMN_OUT, player.getOuts());
                                                    values.put(StatsEntry.COLUMN_SF, player.getSacFlies());
                                                    values.put(StatsEntry.COLUMN_SB, player.getStolenBases());
                                                    values.put(StatsEntry.COLUMN_G, player.getGames());
                                                    String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";

                                                    int rowsUpdated = getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS,
                                                            values, selection, new String[]{playerIdString, mStatKeeperID});

                                                    if (rowsUpdated < 1) {
                                                        values.put(StatsEntry.SYNC, true);
                                                        values.put(StatsEntry.COLUMN_NAME, player.getName());
                                                        values.put(StatsEntry.COLUMN_TEAM, player.getTeam());
                                                        values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, player.getTeamfirestoreid());
                                                        values.put(StatsEntry.COLUMN_GENDER, player.getGender());
                                                        values.put(StatsEntry.COLUMN_FIRESTORE_ID, playerIdString);
                                                        values.put(StatsEntry.COLUMN_LEAGUE_ID, mStatKeeperID);
                                                        getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
                                                    }
                                                    sndMsg(MSG_PLAYER_UPDATED);
                                                } else {
                                                    sndMsg(MSG_ERROR);
                                                }
                                            }
                                        });
                            }
                        } else {
                            sndMsg(MSG_ERROR);
                        }
                    }
                });
    }

    private void updateBoxscores(long localTimeStamp) {
        mFirestore.collection(LEAGUE_COLLECTION).document(mStatKeeperID).collection(BOXSCORE_COLLECTION)
                .whereGreaterThan(StatsEntry.COLUMN_GAME_ID, localTimeStamp)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            QuerySnapshot querySnapshot = task.getResult();
                            final int numberOfBoxscores = querySnapshot.size();

                            sndMax(MSG_BOXSCORE_MAX, numberOfBoxscores);
                            for (DocumentSnapshot document : querySnapshot) {
                                String gameIDString = document.getId();

                                String selection = StatsEntry.COLUMN_GAME_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
                                Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BOXSCORE_OVERVIEWS,
                                        null, selection, new String[]{gameIDString, mStatKeeperID}, null);

                                if (!cursor.moveToFirst()) {
                                    long gameID = Long.parseLong(gameIDString);
                                    int awayTeamRuns = document.getLong(StatsEntry.COLUMN_AWAY_RUNS).intValue();
                                    int homeTeamRuns = document.getLong(StatsEntry.COLUMN_HOME_RUNS).intValue();
                                    String awayTeamID = document.getString(StatsEntry.COLUMN_AWAY_TEAM);
                                    String homeTeamID = document.getString(StatsEntry.COLUMN_HOME_TEAM);

                                    ContentValues values = new ContentValues();
                                    values.put(StatsEntry.COLUMN_GAME_ID, gameID);
                                    values.put(StatsEntry.COLUMN_AWAY_TEAM, awayTeamID);
                                    values.put(StatsEntry.COLUMN_HOME_TEAM, homeTeamID);
                                    values.put(StatsEntry.COLUMN_AWAY_RUNS, awayTeamRuns);
                                    values.put(StatsEntry.COLUMN_HOME_RUNS, homeTeamRuns);
                                    values.put(StatsEntry.COLUMN_LOCAL, 0);
                                    values.put(StatsEntry.COLUMN_LEAGUE_ID, mStatKeeperID);
                                    getContentResolver().insert(StatsEntry.CONTENT_URI_BOXSCORE_OVERVIEWS, values);
                                }
                                cursor.close();
                                sndMsg(MSG_BOXSCORE_UPDATED);
                            }
                        } else {
                            sndMsg(MSG_ERROR);
                        }
                    }
                });
    }


    private void updateTeams(long localTimeStamp, final String statKeeperName, final int statKeeperType) {
        mFirestore.collection(LEAGUE_COLLECTION).document(mStatKeeperID).collection(TEAMS_COLLECTION)
                .whereGreaterThan(StatsContract.StatsEntry.UPDATE, localTimeStamp)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            final int numberOfTeams = querySnapshot.size();
                            sndMax(MSG_TEAM_MAX, numberOfTeams);


                            for (DocumentSnapshot document : querySnapshot) {
                                //Get the document data and ID of a team
                                final Team team = document.toObject(Team.class);
                                final String teamIdString = document.getId();

                                //Get the logs for a team
                                mFirestore.collection(LEAGUE_COLLECTION).document(mStatKeeperID).collection(TEAMS_COLLECTION)
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
                                                        TeamLog teamLog = document.toObject(TeamLog.class);
                                                        wins += teamLog.getWins();
                                                        losses += teamLog.getLosses();
                                                        ties += teamLog.getTies();
                                                        runsScored += teamLog.getRunsScored();
                                                        runsAllowed += teamLog.getRunsAllowed();
                                                    }

                                                    DocumentReference docRef = mFirestore.collection(LEAGUE_COLLECTION)
                                                            .document(mStatKeeperID).collection(TEAMS_COLLECTION).document(teamIdString);

                                                    int totalWins = team.getWins() + wins;
                                                    int totalLosses = team.getLosses() + losses;
                                                    int totalTies = team.getTies() + ties;
                                                    int totalRunsScored = team.getTotalRunsScored() + runsScored;
                                                    int totalRunsAllowed = team.getTotalRunsAllowed() + runsAllowed;

                                                    team.setWins(totalWins);
                                                    team.setLosses(totalLosses);
                                                    team.setTies(totalTies);
                                                    team.setTotalRunsScored(totalRunsScored);
                                                    team.setTotalRunsAllowed(totalRunsAllowed);

                                                    if (querySnapshot.size() > 0) {
                                                        WriteBatch batch = mFirestore.batch();
                                                        batch.set(docRef, team, SetOptions.merge());

                                                        for (DocumentSnapshot snapshot : querySnapshot) {
                                                            batch.delete(snapshot.getReference());
                                                        }
                                                        batch.commit();
                                                    }

                                                    ContentValues values = new ContentValues();
                                                    values.put(StatsEntry.COLUMN_LEAGUE_ID, mStatKeeperID);
                                                    values.put(StatsContract.StatsEntry.COLUMN_NAME, team.getName());
                                                    values.put(StatsContract.StatsEntry.COLUMN_WINS, team.getWins());
                                                    values.put(StatsContract.StatsEntry.COLUMN_LOSSES, team.getLosses());
                                                    values.put(StatsContract.StatsEntry.COLUMN_TIES, team.getTies());
                                                    values.put(StatsContract.StatsEntry.COLUMN_RUNSFOR, team.getTotalRunsScored());
                                                    values.put(StatsEntry.COLUMN_RUNSAGAINST, team.getTotalRunsAllowed());
                                                    String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";

                                                    int rowsUpdated = getContentResolver().update(StatsEntry.CONTENT_URI_TEAMS,
                                                            values, selection, new String[]{teamIdString, mStatKeeperID});

                                                    if (rowsUpdated < 1) {
                                                        values.put(StatsEntry.SYNC, true);
                                                        values.put(StatsEntry.COLUMN_FIRESTORE_ID, teamIdString);
                                                        values.put(StatsEntry.TYPE, statKeeperType);
                                                        values.put(StatsEntry.COLUMN_LEAGUE, statKeeperName);
                                                        getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);
                                                    }
                                                    sndMsg(MSG_TEAM_UPDATED);
                                                } else {
                                                    sndMsg(MSG_ERROR);
                                                }
                                            }
                                        });
                            }
                        } else {
                            sndMsg(MSG_ERROR);
                        }
                    }
                });
    }

//SYNCING DELETES

    private void deletionCheck(final int level) {
        final long localTimeStamp = TimeStampUpdater.getLocalTimeStamp(this, mStatKeeperID);

        mFirestore.collection(LEAGUE_COLLECTION).document(mStatKeeperID).collection(DELETION_COLLECTION)
                .whereGreaterThan(StatsEntry.TIME, localTimeStamp).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<ItemMarkedForDeletion> itemMarkedForDeletionList = new ArrayList<>();
                    QuerySnapshot querySnapshot = task.getResult();
                    for (DocumentSnapshot documentSnapshot : querySnapshot) {
                        Map<String, Object> data = documentSnapshot.getData();
                        long type = (long) data.get(StatsEntry.TYPE);
                        long gender;
                        String team;
                        if (type == 1) {
                            gender = (long) data.get(StatsEntry.COLUMN_GENDER);
                            team = (String) data.get(StatsEntry.COLUMN_TEAM_FIRESTORE_ID);
                        } else {
                            gender = -1;
                            team = null;
                        }
                        String name = (String) data.get(StatsEntry.COLUMN_NAME);
                        String firestoreID = documentSnapshot.getId();
                        itemMarkedForDeletionList.add(new ItemMarkedForDeletion(firestoreID, type, name, gender, team));
                    }
                    if (itemMarkedForDeletionList.isEmpty()) {
                        updateAfterSync();
                        sndMsg(MSG_GO_TO_STATKEEPER);
                    } else {
                        if (level > UsersActivity.LEVEL_VIEW_WRITE) {
                            Collections.sort(itemMarkedForDeletionList, ItemMarkedForDeletion.nameComparator());
                            Collections.sort(itemMarkedForDeletionList, ItemMarkedForDeletion.typeComparator());
                            sndDeletions(itemMarkedForDeletionList);
                        } else {
                            deleteItems(itemMarkedForDeletionList);
                            sndMsg(MSG_GO_TO_STATKEEPER);
                        }
                    }
                } else {
                    sndMsg(MSG_ERROR);
                }
            }
        });
    }

    private void deleteItems(List<ItemMarkedForDeletion> deleteList) {
        if (deleteList.isEmpty()) {
            return;
        }
        final List<String> currentlyPlaying = checkForCurrentGameInterference();
        boolean keepGame = !currentlyPlaying.isEmpty();
        Uri uri;
        for (ItemMarkedForDeletion item : deleteList) {
            if (item.getType() == 0) {
                uri = StatsEntry.CONTENT_URI_TEAMS;
            } else {
                uri = StatsEntry.CONTENT_URI_PLAYERS;
            }
            String firestoreID = item.getFirestoreID();
            String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
            String[] selectionArgs = new String[]{firestoreID, mStatKeeperID};
            getContentResolver().delete(uri, selection, selectionArgs);
            if (keepGame && currentlyPlaying.contains(firestoreID)) {
                clearGameDB();
                keepGame = false;
            }
        }
    }

    private void saveItems(List<ItemMarkedForDeletion> saveList) {
        if (saveList.isEmpty()) {
            return;
        }

        for (ItemMarkedForDeletion item : saveList) {
            Map<String, Object> data = new HashMap<>();
            final String firestoreID = item.getFirestoreID();
            String name = item.getName();
            String collection;
            final long type = item.getType();
            if (type == 0) {
                collection = TEAMS_COLLECTION;
            } else {
                collection = PLAYERS_COLLECTION;
                long gender = item.getGender();
                data.put(StatsEntry.COLUMN_GENDER, gender);
                String team = item.getTeam();
                data.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, team);
            }
            data.put(StatsEntry.COLUMN_NAME, name);

            final Context context = this;

            DocumentReference itemDoc = mFirestore.collection(LEAGUE_COLLECTION)
                    .document(mStatKeeperID).collection(collection).document(firestoreID);
            itemDoc.set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mFirestore.collection(LEAGUE_COLLECTION)
                            .document(mStatKeeperID).collection(DELETION_COLLECTION).document(firestoreID).delete();
                    TimeStampUpdater.setUpdate(firestoreID, (int) type, mStatKeeperID, context, System.currentTimeMillis());
                }
            });
        }
    }


    private void clearGameDB() {
        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mStatKeeperID};
        getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, selection, selectionArgs);
        getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, selection, selectionArgs);
        SharedPreferences savedGamePreferences = getSharedPreferences(mStatKeeperID + StatsEntry.GAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedGamePreferences.edit();
        editor.clear();
        editor.apply();
    }

    //OTHER
    private List<String> checkForCurrentGameInterference() {
        List<String> currentlyPlaying = new ArrayList<>();

        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mStatKeeperID};
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP,
                null, selection, selectionArgs, null);
        while (cursor.moveToNext()) {
            String firestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
            currentlyPlaying.add(firestoreID);
        }
        cursor.close();
        if (!currentlyPlaying.isEmpty()) {
            SharedPreferences gamePreferences
                    = getSharedPreferences(mStatKeeperID + StatsEntry.GAME, Context.MODE_PRIVATE);
            String awayID = gamePreferences.getString(StatsEntry.COLUMN_AWAY_TEAM, null);
            String homeID = gamePreferences.getString(StatsEntry.COLUMN_HOME_TEAM, null);
            currentlyPlaying.add(awayID);
            currentlyPlaying.add(homeID);
        }
        return currentlyPlaying;
    }

    private void updateAfterSync() {
        final Context context = this;
        mFirestore.collection(LEAGUE_COLLECTION).document(mStatKeeperID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            long cloudTimeStamp = TimeStampUpdater.getCloudTimeStamp(task.getResult(), mStatKeeperID);
                            TimeStampUpdater.updateLocalTimeStamp(cloudTimeStamp, context, mStatKeeperID);
                        }
                    }
                });
    }


//    //LISTENER
//    public interface onFirestoreSyncListener {
//        void onUpdateCheck(boolean update);
//
//        void onSyncStart(int numberOf, boolean teams);
//
//        void onSyncUpdate(boolean teams);
//
//        void openDeletionCheckDialog(ArrayList<ItemMarkedForDeletion> deleteList);
//
//        void proceedToNext();
//
//        void onSyncError(String error);
//
//    }
//    public void detachListener() {
//        mListener = null;
//
//    }
}
