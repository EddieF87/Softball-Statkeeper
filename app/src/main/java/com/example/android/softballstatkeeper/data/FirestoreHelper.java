package com.example.android.softballstatkeeper.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.android.softballstatkeeper.MyApp;
import com.example.android.softballstatkeeper.models.ItemMarkedForDeletion;
import com.example.android.softballstatkeeper.models.Player;
import com.example.android.softballstatkeeper.models.Team;
import com.example.android.softballstatkeeper.data.StatsContract.StatsEntry;
import com.example.android.softballstatkeeper.models.PlayerLog;
import com.example.android.softballstatkeeper.models.TeamLog;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Eddie on 11/7/2017.
 */

public class FirestoreHelper implements Parcelable {
    private static final String TAG = "FirestoreHelper: ";
    public static final String LEAGUE_COLLECTION = "leagues";
    public static final String PLAYERS_COLLECTION = "players";
    public static final String TEAMS_COLLECTION = "teams";
    public static final String DELETION_COLLECTION = "deletion";
    public static final String PLAYER_LOGS = "playerlogs";
    public static final String TEAM_LOGS = "teamlogs";
    private static final String LAST_UPDATE = "last_update";
    private static final String UPDATE_SETTINGS = "_updateSettings";
    public static final String USERS = "users";
    private int playersofar;
    private int teamssofar;
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

    public void setContext(Context context) {
        mContext = context;
        mListener = (onFirestoreSyncListener) context;
    }

    public void detachListener() {
        mListener = null;

    }

    //SYNC CHECK

    public void checkForUpdate() {
        Log.d("xxx", "checkForUpdate");

        final long localTimeStamp = getLocalTimeStamp();
        Log.d("xxx", "localTimeStamp = " + localTimeStamp);

        mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            long cloudTimeStamp = getCloudTimeStamp(task.getResult());
                            Log.d("xxx", "cloudTimeStamp = " + cloudTimeStamp);

                            if (cloudTimeStamp > localTimeStamp) {
                                Log.d("xxx", "onUpdateCheck(true)");
                                if(mListener != null) {
                                    mListener.onUpdateCheck(true);
                                }
                            } else {
                                Log.d("xxx", "onUpdateCheck(false)");
                                if(mListener != null) {
                                    mListener.onUpdateCheck(false);
                                }
                            }
                        } else {
                            Log.d("xxx", "onUpdateCheck(false)");
                            if(mListener != null) {
                                mListener.onUpdateCheck(false);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(mListener != null) {
                            mListener.onUpdateCheck(false);
                        }
                    }
                });
    }

    //TIMESTAMP MAINTENANCE

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

    private long getLocalTimeStamp() {
//        return 0;
        SharedPreferences updatePreferences = mContext.getSharedPreferences(leagueID + UPDATE_SETTINGS, Context.MODE_PRIVATE);
        return updatePreferences.getLong(LAST_UPDATE, 0);
    }

    public void setLocalTimeStamp(long time) {
        SharedPreferences updatePreferences = mContext.getSharedPreferences(leagueID + UPDATE_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = updatePreferences.edit();
        editor.putLong(LAST_UPDATE, time);
        editor.apply();
    }

    private long getCloudTimeStamp(DocumentSnapshot documentSnapshot) {
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


    //SYNCING UPDATES

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
        updatePlayers(localTimeStamp);
        updateTeams(localTimeStamp);
    }

    private void updatePlayers(long localTimeStamp) {
        mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(PLAYERS_COLLECTION)
                .whereGreaterThanOrEqualTo(StatsEntry.UPDATE, localTimeStamp)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            QuerySnapshot querySnapshot = task.getResult();
                            final int numberOfPlayers = querySnapshot.size();
                            playersofar = 0;
                            if(mListener != null) {
                                mListener.onSyncStart(numberOfPlayers, false);
                            }
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
                                                    playersofar++;

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
                                                        values.put(StatsEntry.SYNC, true);
                                                        values.put(StatsEntry.COLUMN_NAME, player.getName());
                                                        values.put(StatsEntry.COLUMN_TEAM, player.getTeam());
                                                        values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, player.getTeamfirestoreid());
                                                        values.put(StatsEntry.COLUMN_GENDER, player.getGender());
                                                        values.put(StatsEntry.COLUMN_FIRESTORE_ID, playerIdString);
                                                        mContext.getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
                                                    }
                                                    Log.d("xxx", "onSyncUpdate: " + player.getName() + "  " + playersofar + " / " + numberOfPlayers);
                                                    if(mListener != null) {
                                                        mListener.onSyncUpdate(false);
                                                    }
                                                } else {
                                                    if(mListener != null) {
                                                        mListener.onSyncError("updating players");
                                                    }
                                                }
                                            }
                                        });
                            }
                        } else {
                            if(mListener != null) {
                                mListener.onSyncError("updating players");
                            }
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void updateTeams(long localTimeStamp) {
        Log.d("xxx", "updateTeamsAttempt");
        mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(TEAMS_COLLECTION)
                .whereGreaterThanOrEqualTo(StatsEntry.UPDATE, localTimeStamp)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            final int numberOfTeams = querySnapshot.size();
                            teamssofar = 0;
                            Log.d("xxx", "teamsUpdating = " + numberOfTeams);
                            if(mListener != null) {
                                mListener.onSyncStart(numberOfTeams, true);
                            }
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

                                                    teamssofar++;
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
                                                        batch.set(docRef, team, SetOptions.merge());

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
                                                    Log.d("xxx", "rowsUpdated = " + rowsUpdated);

                                                    if (rowsUpdated < 1) {
                                                        Log.d("xxx", "inserting");
                                                        Log.d(TAG, "test:  " + "inserting");
                                                        values.put(StatsEntry.SYNC, true);
                                                        values.put(StatsEntry.COLUMN_NAME, team.getName());
                                                        values.put(StatsEntry.COLUMN_FIRESTORE_ID, teamIdString);
                                                        mContext.getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);
                                                    }
                                                    if(mListener != null) {
                                                        mListener.onSyncUpdate(true);
                                                    }
                                                    Log.d("xxx", "onSyncUpdate: " + team.getName() + "  " + teamssofar + " / " + numberOfTeams);
                                                } else {
                                                    if(mListener != null) {
                                                        mListener.onSyncError("updating teams");
                                                    }
                                                }
                                            }
                                        });
                            }
                        } else {
                            if(mListener != null) {
                                mListener.onSyncError("updating teams");
                            }
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    //SYNCING DELETES

    public void deletionCheck(final int level) {
        final long localTimeStamp = getLocalTimeStamp();
        Log.d("xxx", "FIRESTORE DELETION CHECK");
        Log.d("xxx", "localTimeStamp = " + localTimeStamp);

        mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(DELETION_COLLECTION)
                .whereGreaterThanOrEqualTo(StatsEntry.TIME, localTimeStamp).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                        if(mListener != null) {
                            mListener.proceedToNext();
                        }
                    } else {
                        if (level > 3) {
                            Collections.sort(itemMarkedForDeletionList, ItemMarkedForDeletion.nameComparator());
                            Collections.sort(itemMarkedForDeletionList, ItemMarkedForDeletion.typeComparator());
                            if(mListener != null) {
                                mListener.openDeletionCheckDialog(itemMarkedForDeletionList);
                            }
                        } else {
                            deleteItems(itemMarkedForDeletionList);
                            if(mListener != null) {
                                mListener.proceedToNext();
                            }
                        }
                    }
                } else {
                    Log.d("xxx", "filtered_deletionQueryError");
                    if(mListener != null) {
                        mListener.onSyncError(DELETION_COLLECTION);
                    }
                }
            }
        });
    }

    public void deleteItems (List<ItemMarkedForDeletion> deleteList) {
        if(deleteList.isEmpty()) {
            return;
        }
        final List<String> currentlyPlaying = checkForCurrentGameInterference();
        boolean keepGame = !currentlyPlaying.isEmpty();
        Uri uri;
        for(ItemMarkedForDeletion item : deleteList) {
            if (item.getType() == 0) {
                uri = StatsEntry.CONTENT_URI_TEAMS;
            } else {
                uri = StatsEntry.CONTENT_URI_PLAYERS;
            }
            String firestoreID = item.getFirestoreID();
            String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
            String[] selectionArgs = new String[]{firestoreID};
            mContext.getContentResolver().delete(uri, selection, selectionArgs);
            if (keepGame && currentlyPlaying.contains(firestoreID)) {
                Log.d("xxx", "currentlyPlaying.contains(firestoreID " + firestoreID);
                clearGameDB();
                keepGame = false;
            }
        }
    }

    public void saveItems (List<ItemMarkedForDeletion> saveList) {
        if(saveList.isEmpty()) {
            return;
        }

        for(ItemMarkedForDeletion item : saveList) {
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
            DocumentReference itemDoc = mFirestore.collection(LEAGUE_COLLECTION)
                    .document(leagueID).collection(collection).document(firestoreID);
            itemDoc.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION)
                            .document(leagueID).collection(DELETION_COLLECTION).document(firestoreID).delete();
                    setUpdate(firestoreID, (int) type);
                }
            });
        }
    }

    private void clearGameDB() {
        mContext.getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
        mContext.getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        SharedPreferences savedGamePreferences = mContext.getSharedPreferences(leagueID + StatsEntry.GAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedGamePreferences.edit();
        editor.clear();
        editor.apply();
    }

    //SETTING UPDATES

    public void setUpdate(String firestoreID, int type) {
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }

        long timeStamp = System.currentTimeMillis();
        String collection;

        if (type == 0) {
            collection = FirestoreHelper.TEAMS_COLLECTION;
        } else if (type == 1) {
            collection = FirestoreHelper.PLAYERS_COLLECTION;
        } else {
            return;
        }

        DocumentReference documentReference = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(leagueID)
                .collection(collection).document(firestoreID);
        documentReference.update(StatsEntry.UPDATE, timeStamp).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("xxx", "updateFailure");
                Toast.makeText(mContext, "POKEMON! GOTTA CATCH THEM ALLLLL!!!", Toast.LENGTH_LONG).show();
            }
        });
        updateTimeStamps();
    }

    public void addDeletion(final String firestoreID, final int type, final String name, final int gender, final String team) {
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }
        mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(leagueID)
                .collection(FirestoreHelper.PLAYERS_COLLECTION).document(firestoreID)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setDeletionDoc(leagueID, firestoreID, type, name, gender, team);
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
        if(type == 1) {
            deletion.put(StatsEntry.COLUMN_GENDER, gender);
            deletion.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, team);
        }

        deletionDoc.set(deletion);
        updateTimeStamps();
    }

    public void addPlayerStatsToDB() {

        ArrayList<Long> playerList = new ArrayList<>();
        String selection = StatsEntry.COLUMN_PLAYERID + "=?";

        Cursor cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                null, null, null);

        while (cursor.moveToNext()) {
            long playerId = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_PLAYERID);
            playerList.add(playerId);
        }

        WriteBatch playerBatch = mFirestore.batch();

        for (long playerId : playerList) {
            String[] selectionArgs = new String[]{String.valueOf(playerId)};

            cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                    selection, selectionArgs, null);
            cursor.moveToFirst();
            int gameRBI = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RBI);
            int gameRun = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUN);
            int game1b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_1B);
            int game2b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_2B);
            int game3b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_3B);
            int gameHR = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HR);
            int gameOuts = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_OUT);
            int gameBB = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_BB);
            int gameSF = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_SF);
            String firestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);

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

            int pRBI = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RBI);
            int pRun = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUN);
            int p1b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_1B);
            int p2b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_2B);
            int p3b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_3B);
            int pHR = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HR);
            int pOuts = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_OUT);
            int pBB = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_BB);
            int pSF = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_SF);
            int games = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_G);
            firestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);

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
            setUpdate(firestoreID, 1);
        }
        cursor.close();
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
                    int playerId = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_PLAYERID);
                    int gameRBI = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RBI);
                    int gameRun = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUN);
                    int game1b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_1B);
                    int game2b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_2B);
                    int game3b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_3B);
                    int gameHR = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HR);
                    int gameOuts = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_OUT);
                    int gameBB = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_BB);
                    int gameSF = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_SF);

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
                cursor.close();
            }
        });
    }

    public void addTeamStatsToDB(final String teamFirestoreID, int teamRuns, int otherTeamRuns) {
        WriteBatch teamBatch = mFirestore.batch();

        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = {teamFirestoreID};
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

        long teamId = StatsContract.getColumnLong(cursor, StatsEntry._ID);
        TeamLog teamLog = new TeamLog(teamId, teamRuns, otherTeamRuns);
        backupValues.put(StatsEntry.COLUMN_TEAM_ID, teamId);

        final DocumentReference docRef = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION)
                .document(leagueID).collection(FirestoreHelper.TEAMS_COLLECTION).document(teamFirestoreID)
                .collection(FirestoreHelper.TEAM_LOGS).document(String.valueOf(logId));

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

        mContext.getContentResolver().update(StatsEntry.CONTENT_URI_TEAMS, values, selection, selectionArgs);

        teamBatch.set(docRef, teamLog);
        teamBatch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setUpdate(teamFirestoreID, 0);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("teamBatch failure", e);
                        mContext.getContentResolver().insert(StatsEntry.CONTENT_URI_BACKUP_TEAMS, backupValues);
                    }
                });

    }

    //OTHER
    private List<String> checkForCurrentGameInterference() {
        List<String> currentlyPlaying = new ArrayList<>();
        Cursor cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_TEMP,
                null, null, null, null);
        while (cursor.moveToNext()) {
            String firestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
            currentlyPlaying.add(firestoreID);
        }
        cursor.close();
        if (!currentlyPlaying.isEmpty()) {
            SharedPreferences gamePreferences
                    = mContext.getSharedPreferences(leagueID + StatsEntry.GAME, Context.MODE_PRIVATE);
            String awayID = gamePreferences.getString("keyAwayTeam", null);
            String homeID = gamePreferences.getString("keyHomeTeam", null);
            currentlyPlaying.add(awayID);
            currentlyPlaying.add(homeID);
        }
        return currentlyPlaying;
    }

//    public void retryGameLogLoad() {
//        MyApp myApp = (MyApp) mContext.getApplicationContext();
//        String leagueID = myApp.getCurrentSelection().getId();
//
//        WriteBatch batch = mFirestore.batch();
//
//        Cursor cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_PLAYERS,
//                null, null, null, null);
//        while (cursor.moveToNext()) {
//            long logId = StatsContract.getColumnLong(cursor, StatsEntry.COLUMN_LOG_ID);
//            long playerId = StatsContract.getColumnLong(cursor, StatsEntry.COLUMN_PLAYERID);
//            int gameRBI = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RBI);
//            int gameRun = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUN);
//            int game1b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_1B);
//            int game2b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_2B);
//            int game3b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_3B);
//            int gameHR = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HR);
//            int gameOuts = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_OUT);
//            int gameBB = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_BB);
//            int gameSF = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_SF);
//
//            final DocumentReference docRef = mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(PLAYERS_COLLECTION)
//                    .document(String.valueOf(playerId)).collection(PLAYER_LOGS).document(String.valueOf(logId));
//
//            PlayerLog playerLog = new PlayerLog(playerId, gameRBI, gameRun, game1b, game2b, game3b, gameHR, gameOuts, gameBB, gameSF);
//            batch.set(docRef, playerLog);
//        }
//
//        cursor.close();
//
//        cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_TEAMS, null,
//                null, null, null);
//        while (cursor.moveToNext()) {
//            long logId = StatsContract.getColumnLong(cursor, StatsEntry.COLUMN_LOG_ID);
//            long teamId = StatsContract.getColumnLong(cursor, StatsEntry.COLUMN_TEAM_ID);
//            int gameWins = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_WINS);
//            int gameLosses = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_LOSSES);
//            int gameTies = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_TIES);
//            int gameRunsScored = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUNSFOR);
//            int gameRunsAllowed = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUNSAGAINST);
//
//            final DocumentReference docRef = mFirestore.collection(LEAGUE_COLLECTION).document(leagueID).collection(TEAMS_COLLECTION)
//                    .document(String.valueOf(teamId)).collection(TEAM_LOGS).document(String.valueOf(logId));
//
//            TeamLog teamLog = new TeamLog(teamId, gameWins, gameLosses, gameTies, gameRunsScored, gameRunsAllowed);
//            batch.set(docRef, teamLog);
//        }
//
//        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                Log.d(TAG, "Transaction success!");
//                mContext.getContentResolver().delete(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, null, null);
//                mContext.getContentResolver().delete(StatsEntry.CONTENT_URI_BACKUP_TEAMS, null, null);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.w(TAG, "Transaction failure.", e);
//            }
//        });
//        cursor.close();
//
//    }

    //LISTENER
    public interface onFirestoreSyncListener {
        void onUpdateCheck(boolean update);
        void onSyncStart(int numberOf, boolean teams);
        void onSyncUpdate(boolean teams);
        void openDeletionCheckDialog(ArrayList<ItemMarkedForDeletion> deleteList);
        void proceedToNext();
        void onSyncError(String error);
    }



    protected FirestoreHelper(Parcel in) {
        playersofar = in.readInt();
        teamssofar = in.readInt();
        leagueID = in.readString();
        mListener = (onFirestoreSyncListener) in.readValue(onFirestoreSyncListener.class.getClassLoader());
        mContext = (Context) in.readValue(Context.class.getClassLoader());
        mFirestore = (FirebaseFirestore) in.readValue(FirebaseFirestore.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(playersofar);
        dest.writeInt(teamssofar);
        dest.writeString(leagueID);
        dest.writeValue(mListener);
        dest.writeValue(mContext);
        dest.writeValue(mFirestore);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FirestoreHelper> CREATOR = new Parcelable.Creator<FirestoreHelper>() {
        @Override
        public FirestoreHelper createFromParcel(Parcel in) {
            return new FirestoreHelper(in);
        }

        @Override
        public FirestoreHelper[] newArray(int size) {
            return new FirestoreHelper[size];
        }
    };
}