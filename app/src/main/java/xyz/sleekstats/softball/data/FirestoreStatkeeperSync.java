package xyz.sleekstats.softball.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

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

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.activities.UsersActivity;
import xyz.sleekstats.softball.objects.ItemMarkedForDeletion;
import xyz.sleekstats.softball.objects.Player;
import xyz.sleekstats.softball.objects.PlayerLog;
import xyz.sleekstats.softball.objects.Team;
import xyz.sleekstats.softball.objects.TeamLog;

import static xyz.sleekstats.softball.data.FirestoreHelper.*;
import static xyz.sleekstats.softball.data.StatsContract.StatsEntry;

public class FirestoreStatkeeperSync implements Parcelable {

    private int playersofar;
    private int teamssofar;
//    private int boxscoresofar;
    private String mStatKeeperID;

    private onFirestoreSyncListener mListener;

    private Context mContext;
    private FirebaseFirestore mFirestore;


    public FirestoreStatkeeperSync(Context context, String id) {
        this.mContext = context;
        this.mStatKeeperID = id;
        mFirestore = FirebaseFirestore.getInstance();

        if (context instanceof onFirestoreSyncListener) {
            mListener = (onFirestoreSyncListener) context;
        }
    }

    //SYNC CHECK

    public void checkForUpdate() {

        final long localTimeStamp = TimeStampUpdater.getLocalTimeStamp(mContext, mStatKeeperID);

        mFirestore.collection(LEAGUE_COLLECTION).document(mStatKeeperID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            long cloudTimeStamp = TimeStampUpdater.getCloudTimeStamp(task.getResult(), mStatKeeperID);

                            if (cloudTimeStamp > localTimeStamp) {
                                if (mListener != null) {
                                    mListener.onUpdateCheck(true);
                                }
                            } else {
                                if (mListener != null) {
                                    mListener.onUpdateCheck(false);
                                }
                            }
                        } else {
                            if (mListener != null) {
                                mListener.onUpdateCheck(false);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (mListener != null) {
                            mListener.onUpdateCheck(false);
                        }
                    }
                });
    }


    //SYNCING UPDATES

    public void syncStats() {
        long localTimeStamp = TimeStampUpdater.getLocalTimeStamp(mContext, mStatKeeperID);

        if (mStatKeeperID == null) {
            try {
                MyApp myApp = (MyApp) mContext.getApplicationContext();
                mStatKeeperID = myApp.getCurrentSelection().getId();
            } catch (Exception e) {
                return;
            }
        }
        updateBoxscores(localTimeStamp);
        updatePlayers(localTimeStamp);
        updateTeams(localTimeStamp);
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
                            playersofar = 0;
                            if (mListener != null) {
                                mListener.onSyncStart(numberOfPlayers, false);
                            }
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
                                                    if (mListener != null) {
                                                        mListener.onSyncUpdate(false);
                                                    }
                                                } else {
                                                    if (mListener != null) {
                                                        mListener.onSyncError("updating players");
                                                    }
                                                }
                                            }
                                        });
                            }
                        } else {
                            if (mListener != null) {
                                mListener.onSyncError("updating players");
                            }
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
//                            boxscoresofar = 0;
                            if (mListener != null) {
                                mListener.onSyncStart(numberOfBoxscores, false);
                            }
                            for (DocumentSnapshot document : querySnapshot) {
                                String gameIDString = document.getId();

                                String selection = StatsEntry.COLUMN_GAME_ID + "=?";
                                Cursor cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_BOXSCORE_OVERVIEWS,
                                        null, selection, new String[]{gameIDString}, null);

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
                                    mContext.getContentResolver().insert(StatsEntry.CONTENT_URI_BOXSCORE_OVERVIEWS, values);
                                }
                                cursor.close();

                                if (mListener != null) {
                                    mListener.onSyncUpdate(false);
                                }
                            }
                        } else {
                            if (mListener != null) {
                                mListener.onSyncError("updating boxscores");
                            }
                        }
                    }
                });
    }


    private void updateTeams(long localTimeStamp) {
        mFirestore.collection(LEAGUE_COLLECTION).document(mStatKeeperID).collection(TEAMS_COLLECTION)
                .whereGreaterThan(StatsContract.StatsEntry.UPDATE, localTimeStamp)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            final int numberOfTeams = querySnapshot.size();
                            teamssofar = 0;
                            if (mListener != null) {
                                mListener.onSyncStart(numberOfTeams, true);
                            }
                            //loop through teams
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

                                                    teamssofar++;
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
                                                    values.put(StatsContract.StatsEntry.COLUMN_NAME, team.getName());
                                                    values.put(StatsContract.StatsEntry.COLUMN_WINS, team.getWins());
                                                    values.put(StatsContract.StatsEntry.COLUMN_LOSSES, team.getLosses());
                                                    values.put(StatsContract.StatsEntry.COLUMN_TIES, team.getTies());
                                                    values.put(StatsContract.StatsEntry.COLUMN_RUNSFOR, team.getTotalRunsScored());
                                                    values.put(StatsEntry.COLUMN_RUNSAGAINST, team.getTotalRunsAllowed());
                                                    String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";

                                                    int rowsUpdated = mContext.getContentResolver().update(StatsEntry.CONTENT_URI_TEAMS,
                                                            values, selection, new String[]{teamIdString});

                                                    if (rowsUpdated < 1) {
                                                        values.put(StatsEntry.SYNC, true);
                                                        values.put(StatsEntry.COLUMN_NAME, team.getName());
                                                        values.put(StatsEntry.COLUMN_FIRESTORE_ID, teamIdString);
                                                        mContext.getContentResolver().insert(StatsEntry.CONTENT_URI_TEAMS, values);
                                                    }
                                                    if (mListener != null) {
                                                        mListener.onSyncUpdate(true);
                                                    }
                                                } else {
                                                    if (mListener != null) {
                                                        mListener.onSyncError("updating teams");
                                                    }
                                                }
                                            }
                                        });
                            }
                        } else {
                            if (mListener != null) {
                                mListener.onSyncError("updating teams");
                            }
                        }
                    }
                });
    }

//SYNCING DELETES

    public void deletionCheck(final int level) {
        final long localTimeStamp = TimeStampUpdater.getLocalTimeStamp(mContext, mStatKeeperID);

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
                        if (mListener != null) {
                            mListener.proceedToNext();
                        }
                    } else {
                        if (level > UsersActivity.LEVEL_VIEW_WRITE) {
                            Collections.sort(itemMarkedForDeletionList, ItemMarkedForDeletion.nameComparator());
                            Collections.sort(itemMarkedForDeletionList, ItemMarkedForDeletion.typeComparator());
                            if (mListener != null) {
                                mListener.openDeletionCheckDialog(itemMarkedForDeletionList);
                            }
                        } else {
                            deleteItems(itemMarkedForDeletionList);
                            if (mListener != null) {
                                mListener.proceedToNext();
                            }
                        }
                    }
                } else {
                    if (mListener != null) {
                        mListener.onSyncError(DELETION_COLLECTION);
                    }
                }
            }
        });
    }

    public void deleteItems(List<ItemMarkedForDeletion> deleteList) {
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
            String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
            String[] selectionArgs = new String[]{firestoreID};
            mContext.getContentResolver().delete(uri, selection, selectionArgs);
            if (keepGame && currentlyPlaying.contains(firestoreID)) {
                clearGameDB();
                keepGame = false;
            }
        }
    }

    public void saveItems(List<ItemMarkedForDeletion> saveList) {
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
            DocumentReference itemDoc = mFirestore.collection(LEAGUE_COLLECTION)
                    .document(mStatKeeperID).collection(collection).document(firestoreID);
            itemDoc.set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mFirestore.collection(LEAGUE_COLLECTION)
                            .document(mStatKeeperID).collection(DELETION_COLLECTION).document(firestoreID).delete();
                    TimeStampUpdater.setUpdate(firestoreID, (int) type, mStatKeeperID, mContext, System.currentTimeMillis());
                }
            });
        }
    }



    private void clearGameDB() {
        mContext.getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
        mContext.getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        SharedPreferences savedGamePreferences = mContext.getSharedPreferences(mStatKeeperID + StatsEntry.GAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedGamePreferences.edit();
        editor.clear();
        editor.apply();
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
                    = mContext.getSharedPreferences(mStatKeeperID + StatsEntry.GAME, Context.MODE_PRIVATE);
            String awayID = gamePreferences.getString("keyAwayTeam", null);
            String homeID = gamePreferences.getString("keyHomeTeam", null);
            currentlyPlaying.add(awayID);
            currentlyPlaying.add(homeID);
        }
        return currentlyPlaying;
    }

    public void updateAfterSync() {
        mFirestore.collection(LEAGUE_COLLECTION).document(mStatKeeperID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            long cloudTimeStamp = TimeStampUpdater.getCloudTimeStamp(task.getResult(), mStatKeeperID);
                            TimeStampUpdater.updateLocalTimeStamp(cloudTimeStamp, mContext, mStatKeeperID);
                        }
                    }
                });
    }


    //LISTENER
    public interface onFirestoreSyncListener {
        void onUpdateCheck(boolean update);

        void onSyncStart(int numberOf, boolean teams);

        void onSyncUpdate(boolean teams);

        void openDeletionCheckDialog(ArrayList<ItemMarkedForDeletion> deleteList);

        void proceedToNext();

        void onSyncError(String error);

    }

    public void setContext(Context context) {
        mContext = context;
        mListener = (onFirestoreSyncListener) context;
    }

    public void detachListener() {
        mListener = null;

    }

    protected FirestoreStatkeeperSync(Parcel in) {
        playersofar = in.readInt();
        teamssofar = in.readInt();
//        boxscoresofar = in.readInt();
        mStatKeeperID = in.readString();
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
//        dest.writeInt(boxscoresofar);
        dest.writeString(mStatKeeperID);
        dest.writeValue(mListener);
        dest.writeValue(mContext);
        dest.writeValue(mFirestore);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FirestoreStatkeeperSync> CREATOR = new Parcelable.Creator<FirestoreStatkeeperSync>() {
        @Override
        public FirestoreStatkeeperSync createFromParcel(Parcel in) {
            return new FirestoreStatkeeperSync(in);
        }

        @Override
        public FirestoreStatkeeperSync[] newArray(int size) {
            return new FirestoreStatkeeperSync[size];
        }
    };
}
