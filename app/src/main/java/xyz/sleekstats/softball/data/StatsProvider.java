package xyz.sleekstats.softball.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.activities.MainActivity;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.objects.MainPageSelection;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Eddie on 16/08/2017.
 */

public class StatsProvider extends ContentProvider {

    private FirebaseFirestore mFirestore;

    private static final int PLAYERS = 100;
    private static final int PLAYERS_ID = 101;
    private static final int BACKUP_PLAYERS = 102;
    private static final int BACKUP_PLAYERS_ID = 103;
    private static final int TEAMS = 200;
    private static final int TEAMS_ID = 201;
    private static final int BACKUP_TEAMS = 202;
    private static final int BACKUP_TEAMS_ID = 203;
    private static final int TEMP = 300;
    private static final int TEMP_ID = 301;
    private static final int GAME = 400;
    private static final int GAME_ID = 401;
    private static final int SELECTIONS = 500;
    private static final int BOXSCORES = 600;
    private static final int BOXSCORES_ID = 601;
    private static final int BACKUP_BOXSCORES = 602;
    private static final int BACKUP_BOXSCORES_ID = 603;


    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private StatsDbHelper mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = StatsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, StatsContract.PATH_PLAYERS, PLAYERS);
        matcher.addURI(authority, StatsContract.PATH_PLAYERS + "/#", PLAYERS_ID);
        matcher.addURI(authority, StatsContract.PATH_TEAMS, TEAMS);
        matcher.addURI(authority, StatsContract.PATH_TEAMS + "/#", TEAMS_ID);
        matcher.addURI(authority, StatsContract.PATH_TEMP, TEMP);
        matcher.addURI(authority, StatsContract.PATH_TEMP + "/#", TEMP_ID);
        matcher.addURI(authority, StatsContract.PATH_GAME, GAME);
        matcher.addURI(authority, StatsContract.PATH_GAME + "/#", GAME_ID);
        matcher.addURI(authority, StatsContract.PATH_BACKUP_PLAYERS, BACKUP_PLAYERS);
        matcher.addURI(authority, StatsContract.PATH_BACKUP_PLAYERS + "/#", BACKUP_PLAYERS_ID);
        matcher.addURI(authority, StatsContract.PATH_BACKUP_TEAMS, BACKUP_TEAMS);
        matcher.addURI(authority, StatsContract.PATH_BACKUP_TEAMS + "/#", BACKUP_TEAMS_ID);
        matcher.addURI(authority, StatsContract.PATH_SELECTIONS, SELECTIONS);
        matcher.addURI(authority, StatsContract.PATH_BOXSCORES, BOXSCORES);
        matcher.addURI(authority, StatsContract.PATH_BOXSCORES + "/#", BOXSCORES_ID);
        matcher.addURI(authority, StatsContract.PATH_BACKUP_BOXSCORES, BACKUP_BOXSCORES);
        matcher.addURI(authority, StatsContract.PATH_BACKUP_BOXSCORES + "/#", BACKUP_BOXSCORES_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new StatsDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = sUriMatcher.match(uri);
        if(match == SELECTIONS) {
            return querySelection(projection, selection, selectionArgs, sortOrder);
        }

        try {
            MyApp myApp = (MyApp) getContext().getApplicationContext();
            String leagueID = myApp.getCurrentSelection().getId();
            if (selection == null || selection.isEmpty()) {
                selection = StatsEntry.COLUMN_LEAGUE_ID + "='" + leagueID + "'";
            } else {
                selection = selection + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "='" + leagueID + "'";
            }
        } catch (Exception e) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            getContext().startActivity(intent);
            return null;
        }

        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        Cursor cursor;
        String table;
        switch (match) {
            case PLAYERS:
                table = StatsEntry.PLAYERS_TABLE_NAME;
                break;
            case PLAYERS_ID:
                selection = StatsEntry._ID + "=?";
                String playerID = String.valueOf(ContentUris.parseId(uri));
                selectionArgs = new String[]{playerID};
                sortOrder = null;
                table = StatsEntry.PLAYERS_TABLE_NAME;
                break;
            case TEAMS:
                table = StatsEntry.TEAMS_TABLE_NAME;
                break;
            case TEAMS_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.TEAMS_TABLE_NAME;
                break;
            case TEMP:
                table = StatsEntry.TEMPPLAYERS_TABLE_NAME;
                break;
            case TEMP_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.TEMPPLAYERS_TABLE_NAME;
                break;
            case GAME:
                sortOrder = StatsEntry._ID + " ASC";
                table = StatsEntry.GAME_TABLE_NAME;
                break;
            case GAME_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.GAME_TABLE_NAME;
                break;
            case BACKUP_PLAYERS:
                table = StatsEntry.BACKUP_PLAYERS_TABLE_NAME;
                break;
            case BACKUP_TEAMS:
                table = StatsEntry.BACKUP_TEAMS_TABLE_NAME;
                break;
            case BOXSCORES:
                table = StatsEntry.BOXSCORE_TABLE_NAME;
                break;
            case BACKUP_BOXSCORES:
                table = StatsEntry.BACKUP_BOXSCORE_TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor = database.query(table, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    private Cursor querySelection(String[] projection,  String selection,
                                   String[] selectionArgs,  String sortOrder){
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        return database.query(StatsEntry.SELECTIONS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        //Get current selectionID
        final int match = sUriMatcher.match(uri);
        if(match == SELECTIONS) {
            return insertSelection(uri, values);
        }

        MyApp myApp;
        String leagueID;
        int selectionType;
        try {
            myApp = (MyApp) getContext().getApplicationContext();
            leagueID = myApp.getCurrentSelection().getId();
            selectionType = myApp.getCurrentSelection().getType();
            values.put(StatsEntry.COLUMN_LEAGUE_ID, leagueID);
        } catch (Exception e) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            getContext().startActivity(intent);
            return null;
        }

        if (sqlSafeguard(values)) {
            Toast.makeText(getContext(), "Please only enter letters, numbers, -, and _", Toast.LENGTH_SHORT).show();
            return null;
        }

        String table;
        switch (match) {
            case PLAYERS:
                if (containsName(StatsEntry.CONTENT_URI_PLAYERS, values, false)) {
                    return null;
                }
                table = StatsEntry.PLAYERS_TABLE_NAME;
                if (values.containsKey(StatsEntry.SYNC)) {
                    values.remove(StatsEntry.SYNC);
                    break;
                }
                mFirestore = FirebaseFirestore.getInstance();
                DocumentReference playerDoc = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION)
                        .document(leagueID).collection(FirestoreHelper.PLAYERS_COLLECTION).document();

                Map<String, Object> player = new HashMap<>();

                String playerName = values.getAsString(StatsEntry.COLUMN_NAME);

                String playerTeamID;
                String playerTeam;
                if (values.containsKey(StatsEntry.COLUMN_TEAM_FIRESTORE_ID) && values.containsKey(StatsEntry.COLUMN_TEAM)) {
                    playerTeamID = values.getAsString(StatsEntry.COLUMN_TEAM_FIRESTORE_ID);
                    playerTeam = values.getAsString(StatsEntry.COLUMN_TEAM);
                } else {
                    playerTeamID = StatsEntry.FREE_AGENT;
                    playerTeam = StatsEntry.FREE_AGENT;
                }

                int playerGender;
                if (values.containsKey(StatsEntry.COLUMN_GENDER)) {
                    playerGender = values.getAsInteger(StatsEntry.COLUMN_GENDER);
                } else {
                    playerGender = 0;
                }

                player.put(StatsEntry.COLUMN_NAME, playerName);
                player.put(StatsEntry.COLUMN_TEAM, playerTeam);
                player.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, playerTeamID);
                player.put(StatsEntry.COLUMN_GENDER, playerGender);
                if (values.containsKey(StatsEntry.ADD)) {
                    values.remove(StatsEntry.ADD);
                    player.put(StatsEntry.UPDATE, System.currentTimeMillis());
                }
                playerDoc.set(player, SetOptions.merge());

                values.put(StatsEntry.COLUMN_FIRESTORE_ID, playerDoc.getId());
                break;

            case TEAMS:
                String leagueName = myApp.getCurrentSelection().getName();
                values.put(StatsEntry.COLUMN_LEAGUE, leagueName);
                if (containsName(StatsEntry.CONTENT_URI_TEAMS, values, true)) {
                    return null;
                }
                table = StatsEntry.TEAMS_TABLE_NAME;
                if (values.containsKey(StatsEntry.SYNC)) {
                    values.remove(StatsEntry.SYNC);
                    break;
                }
                mFirestore = FirebaseFirestore.getInstance();
                DocumentReference teamDoc;
                if (selectionType == MainPageSelection.TYPE_TEAM) {
                    teamDoc = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION)
                            .document(leagueID).collection(FirestoreHelper.TEAMS_COLLECTION).document(leagueID);
                } else {
                    teamDoc = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION)
                            .document(leagueID).collection(FirestoreHelper.TEAMS_COLLECTION).document();
                }

                Map<String, Object> team = new HashMap<>();
                String teamName = values.getAsString(StatsEntry.COLUMN_NAME);
                team.put(StatsEntry.COLUMN_NAME, teamName);
                if (values.containsKey(StatsEntry.ADD)) {
                    values.remove(StatsEntry.ADD);
                    team.put(StatsEntry.UPDATE, System.currentTimeMillis());
                }
                teamDoc.set(team, SetOptions.merge());

                values.put(StatsEntry.COLUMN_FIRESTORE_ID, teamDoc.getId());
                break;

            case TEMP:
                table = StatsEntry.TEMPPLAYERS_TABLE_NAME;
                break;
            case BACKUP_PLAYERS:
                table = StatsEntry.BACKUP_PLAYERS_TABLE_NAME;
                break;
            case BACKUP_TEAMS:
                table = StatsEntry.BACKUP_TEAMS_TABLE_NAME;
                break;
            case GAME:
                table = StatsEntry.GAME_TABLE_NAME;
                break;
            case BOXSCORES:
                table = StatsEntry.BOXSCORE_TABLE_NAME;
                break;
            case BACKUP_BOXSCORES:
                table = StatsEntry.BACKUP_BOXSCORE_TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        long id = database.insert(table, null, values);

        if (id == -1) {
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        if(match == SELECTIONS) {
            return deleteSelection(selection, selectionArgs);
        }

        String firestoreID;
        if(match == PLAYERS || match == PLAYERS_ID || match == TEAMS || match == TEAMS_ID) {
            if (selectionArgs != null) {
                firestoreID = selectionArgs[0];
            } else {
                return -1;
            }
        } else {
            firestoreID = null;
        }
        final String leagueID;
        try {
            MyApp myApp = (MyApp) getContext().getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            if (mainPageSelection == null) {
                return -1;
            }
            leagueID = myApp.getCurrentSelection().getId();
            if (selection == null || selection.isEmpty()) {
                selection = StatsEntry.COLUMN_LEAGUE_ID + "='" + leagueID + "'";
            } else {
                selection = selection + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "='" + leagueID + "'";
            }
        } catch (Exception e) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            getContext().startActivity(intent);
            return -1;
        }


        SQLiteDatabase database = mOpenHelper.getWritableDatabase();


        int rowsDeleted;
        String table;

        switch (match) {
            case PLAYERS:
                if(inGamePlayerCheck(firestoreID)) {
                    return -1;
                }
                table = StatsEntry.PLAYERS_TABLE_NAME;
                break;

            case PLAYERS_ID:
                if(inGamePlayerCheck(firestoreID)) {
                    return -1;
                }
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.PLAYERS_TABLE_NAME;
                break;

            case TEAMS:
                if(inGameTeamCheck(leagueID, firestoreID)){
                    return -1;
                }
                table = StatsEntry.TEAMS_TABLE_NAME;
                break;

            case TEAMS_ID:
                if(inGameTeamCheck(leagueID, firestoreID)){
                    return -1;
                }
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.TEAMS_TABLE_NAME;
                break;

            case TEMP:
                table = StatsEntry.TEMPPLAYERS_TABLE_NAME;
                break;

            case TEMP_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.TEMPPLAYERS_TABLE_NAME;
                break;

            case GAME:
                table = StatsEntry.GAME_TABLE_NAME;
                break;

            case GAME_ID:
                selection = StatsEntry._ID + ">?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.GAME_TABLE_NAME;
                break;

            case BACKUP_PLAYERS:
                table = StatsEntry.BACKUP_PLAYERS_TABLE_NAME;
                break;

            case BACKUP_PLAYERS_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.BACKUP_PLAYERS_TABLE_NAME;
                break;

            case BACKUP_TEAMS:
                table = StatsEntry.BACKUP_TEAMS_TABLE_NAME;
                break;

            case BACKUP_TEAMS_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.BACKUP_TEAMS_TABLE_NAME;
                break;
            case BOXSCORES:
                table = StatsEntry.BOXSCORE_TABLE_NAME;
                break;
            case BOXSCORES_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.BOXSCORE_TABLE_NAME;
                break;
            case BACKUP_BOXSCORES:
                table = StatsEntry.BACKUP_BOXSCORE_TABLE_NAME;
                break;
            case BACKUP_BOXSCORES_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.BACKUP_BOXSCORE_TABLE_NAME;
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        rowsDeleted = database.delete(table, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    private int deleteSelection(String selection, String[] selectionArgs){
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        int rowsDeleted = database.delete(StatsEntry.SELECTIONS_TABLE_NAME, selection, selectionArgs);

        selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        rowsDeleted += database.delete(StatsEntry.GAME_TABLE_NAME, selection, selectionArgs);
        rowsDeleted += database.delete(StatsEntry.PLAYERS_TABLE_NAME, selection, selectionArgs);
        rowsDeleted += database.delete(StatsEntry.TEAMS_TABLE_NAME, selection, selectionArgs);
        rowsDeleted += database.delete(StatsEntry.TEMPPLAYERS_TABLE_NAME, selection, selectionArgs);
        rowsDeleted += database.delete(StatsEntry.BACKUP_PLAYERS_TABLE_NAME, selection, selectionArgs);
        rowsDeleted +=  database.delete(StatsEntry.BACKUP_TEAMS_TABLE_NAME, selection, selectionArgs);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        String leagueID;
        try {
            MyApp myApp = (MyApp) getContext().getApplicationContext();
            leagueID = myApp.getCurrentSelection().getId();
            if (selection == null || selection.isEmpty()) {
                selection = StatsEntry.COLUMN_LEAGUE_ID + "='" + leagueID + "'";
            } else {
                selection = selection + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "='" + leagueID + "'";
            }
        } catch (Exception e) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            getContext().startActivity(intent);
            return -1;
        }


        if (sqlSafeguard(values)) {
            Toast.makeText(getContext(), "Please only enter letters, numbers, -, and _", Toast.LENGTH_SHORT).show();
            return -1;
        }
        String table;
        String firestoreID;
        DocumentReference documentReference;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLAYERS:
                table = StatsEntry.PLAYERS_TABLE_NAME;
                if (!values.containsKey(StatsEntry.COLUMN_FIRESTORE_ID)) {
                    break;
                }
                firestoreID = values.getAsString(StatsEntry.COLUMN_FIRESTORE_ID);
                mFirestore = FirebaseFirestore.getInstance();
                documentReference = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(leagueID)
                        .collection(FirestoreHelper.PLAYERS_COLLECTION).document(firestoreID);
                if (values.containsKey(StatsEntry.COLUMN_TEAM_FIRESTORE_ID)) {
                    Map<String, Object> data = new HashMap<>();
                    String teamFirestoreID = values.getAsString(StatsEntry.COLUMN_TEAM_FIRESTORE_ID);
                    data.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamFirestoreID);
                    if (values.containsKey(StatsEntry.COLUMN_TEAM)) {
                        String teamName = values.getAsString(StatsEntry.COLUMN_TEAM);
                        data.put(StatsEntry.COLUMN_TEAM, teamName);
                    }
                    documentReference.update(data);
                }
                if (values.containsKey(StatsEntry.COLUMN_GENDER)) {
                    int gender = values.getAsInteger(StatsEntry.COLUMN_GENDER);
                    documentReference.update(StatsEntry.COLUMN_GENDER, gender);
                }
                break;
            case PLAYERS_ID:

                selection = StatsEntry._ID + "=?";
                long id = ContentUris.parseId(uri);
                selectionArgs = new String[]{String.valueOf(id)};
                table = StatsEntry.PLAYERS_TABLE_NAME;

                if (values.containsKey(StatsEntry.SYNC)) {
                    values.remove(StatsEntry.SYNC);
                } else if (containsName(StatsEntry.CONTENT_URI_PLAYERS, values, false)) {
                    return -1;
                }
                firestoreID = values.getAsString(StatsEntry.COLUMN_FIRESTORE_ID);

                mFirestore = FirebaseFirestore.getInstance();
                documentReference = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(leagueID)
                        .collection(FirestoreHelper.PLAYERS_COLLECTION).document(firestoreID);
                values.remove(StatsEntry.COLUMN_FIRESTORE_ID);
                if (values.containsKey(StatsEntry.COLUMN_NAME)) {
                    String playerName = values.getAsString(StatsEntry.COLUMN_NAME);
                    documentReference.update(StatsEntry.COLUMN_NAME, playerName);
                } else if (values.containsKey(StatsEntry.COLUMN_TEAM_FIRESTORE_ID)) {
                    Map<String, Object> data = new HashMap<>();
                    String teamFirestoreID = values.getAsString(StatsEntry.COLUMN_TEAM_FIRESTORE_ID);
                    data.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamFirestoreID);
                    if (values.containsKey(StatsEntry.COLUMN_TEAM)) {
                        String teamName = values.getAsString(StatsEntry.COLUMN_TEAM);
                        data.put(StatsEntry.COLUMN_TEAM, teamName);
                    }
                    documentReference.update(data);
                }
                break;
            case TEAMS:
                table = StatsEntry.TEAMS_TABLE_NAME;
                if (values.containsKey(StatsEntry.COLUMN_NAME)) {
                    firestoreID = values.getAsString(StatsEntry.COLUMN_FIRESTORE_ID);

                    if (firestoreID == null) {
                        break;
                    }

                    mFirestore = FirebaseFirestore.getInstance();

                    String teamName = values.getAsString(StatsEntry.COLUMN_NAME);
                    if (teamName == null) {
                        break;
                    }

                    documentReference = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(leagueID)
                            .collection(FirestoreHelper.TEAMS_COLLECTION).document(firestoreID);
                    values.remove(StatsEntry.COLUMN_FIRESTORE_ID);
                    documentReference.update(StatsEntry.COLUMN_NAME, teamName);

                    DocumentReference documentReference1 = mFirestore
                            .collection(FirestoreHelper.LEAGUE_COLLECTION).document(leagueID);
                    documentReference1.update(StatsEntry.COLUMN_NAME, teamName);
                }
                break;
            case TEAMS_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.TEAMS_TABLE_NAME;
                if (values.containsKey(StatsEntry.SYNC)) {
                    values.remove(StatsEntry.SYNC);
                } else if (containsName(StatsEntry.CONTENT_URI_TEAMS, values, true)) {
                    return -1;
                }
                if (values.containsKey(StatsEntry.COLUMN_NAME)) {
                    firestoreID = values.getAsString(StatsEntry.COLUMN_FIRESTORE_ID);
                    mFirestore = FirebaseFirestore.getInstance();
                    documentReference = mFirestore.collection(FirestoreHelper.LEAGUE_COLLECTION).document(leagueID)
                            .collection(FirestoreHelper.TEAMS_COLLECTION).document(firestoreID);
                    values.remove(StatsEntry.COLUMN_FIRESTORE_ID);
                    String teamName = values.getAsString(StatsEntry.COLUMN_NAME);
                    documentReference.update(StatsEntry.COLUMN_NAME, teamName);
                }
                break;
            case TEMP:
                table = StatsEntry.TEMPPLAYERS_TABLE_NAME;
                break;
            case TEMP_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.TEMPPLAYERS_TABLE_NAME;
                break;
            case GAME:
                table = StatsEntry.GAME_TABLE_NAME;
                break;
            case GAME_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.GAME_TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        try {
            int rowsUpdated = database.update(table, values, selection, selectionArgs);
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
        } catch (Exception e) {
            return -1;
        }
    }

    private Uri insertSelection(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        long id = database.insert(StatsEntry.SELECTIONS_TABLE_NAME, null, values);
        return ContentUris.withAppendedId(uri, id);
    }

    private boolean containsName(Uri uri, ContentValues values, boolean isTeam) {
        MyApp myApp = (MyApp) getContext().getApplicationContext();
        String leagueID = myApp.getCurrentSelection().getId();
        String selection = StatsEntry.COLUMN_LEAGUE_ID + "='" + leagueID + "'";

        if (values.containsKey(StatsEntry.COLUMN_NAME)) {
            String name = values.getAsString(StatsEntry.COLUMN_NAME).toLowerCase();
            if (name.trim().isEmpty()) {
                Toast.makeText(getContext(), R.string.please_enter_name_first, Toast.LENGTH_LONG).show();
                return true;
            }
            String[] projection = new String[]{StatsEntry.COLUMN_NAME};
            Cursor cursor = query(uri, projection, selection, null, null);
            while (cursor.moveToNext()) {
                String teamName = (StatsContract.getColumnString(cursor, StatsEntry.COLUMN_NAME)).toLowerCase();
                if (teamName.equals(name)) {
                    if (isTeam) {
                        Toast.makeText(getContext(), "This team already exists!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "This player already exists!", Toast.LENGTH_LONG).show();
                    }
                    cursor.close();
                    return true;
                }
            }
            cursor.close();
        }
        return false;
    }

    private boolean inGamePlayerCheck(String firestoreID) {
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selctionArgs = new String[] {firestoreID};

        Cursor cursor = query(StatsEntry.CONTENT_URI_TEMP, null, selection, selctionArgs, null);
        if (cursor.moveToFirst()) {
            String name = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_NAME);
            cursor.close();
            Toast.makeText(getContext(), name + " is in an active game!", Toast.LENGTH_SHORT).show();
            return true;
        }
        cursor.close();
        return false;
    }

    private boolean inGameTeamCheck(String leagueID, String firestoreID) {

        SharedPreferences gamePreferences =
                getContext().getSharedPreferences(leagueID + StatsEntry.GAME, Context.MODE_PRIVATE);
        String team = gamePreferences.getString("keyAwayTeam", null);
        if(firestoreID.equals(team)) {
            Toast.makeText(getContext(), "Team is in an active game!", Toast.LENGTH_SHORT).show();
            return true;
        }
        team = gamePreferences.getString("keyHomeTeam", null);
        if(firestoreID.equals(team)) {
            Toast.makeText(getContext(), "Team is in an active game!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private boolean sqlSafeguard(ContentValues values) {
        if (!values.containsKey(StatsEntry.COLUMN_NAME)) {
            return false;
        }
        String name = values.getAsString(StatsEntry.COLUMN_NAME).toLowerCase();
        List<String> forbiddenNames = new ArrayList<>(Arrays.asList(StatsEntry.DELETE, "total",
                "free agent", "waivers", "all teams"));
        return forbiddenNames.contains(name) || !name.matches("^['\\s\\)\\(a-zA-Z0-9_-]+$");
    }
}
