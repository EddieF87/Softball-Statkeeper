package xyz.sleekstats.softball.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import xyz.sleekstats.softball.R;
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
    private static final int BOXSCORE_PLAYERS = 600;
    private static final int BOXSCORE_PLAYER_ID = 601;
    private static final int BOXSCORE_OVERVIEWS = 700;
    private static final int BOXSCORE_OVERVIEW_ID = 701;


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
        matcher.addURI(authority, StatsContract.PATH_BOXSCORE_PLAYERS, BOXSCORE_PLAYERS);
        matcher.addURI(authority, StatsContract.PATH_BOXSCORE_PLAYERS + "/#", BOXSCORE_PLAYER_ID);
        matcher.addURI(authority, StatsContract.PATH_BOXSCORE_OVERVIEWS, BOXSCORE_OVERVIEWS);
        matcher.addURI(authority, StatsContract.PATH_BOXSCORE_OVERVIEWS + "/#", BOXSCORE_OVERVIEW_ID);

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

        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        Cursor cursor;
        String table;
        switch (match) {
            case PLAYERS:
                table = StatsEntry.PLAYERS_TABLE_NAME;
                break;
            case PLAYERS_ID:
                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                String playerID = String.valueOf(ContentUris.parseId(uri));
                selectionArgs = new String[]{playerID};
                sortOrder = null;
                table = StatsEntry.PLAYERS_TABLE_NAME;
                break;
            case TEAMS:
                table = StatsEntry.TEAMS_TABLE_NAME;
                break;
            case TEAMS_ID:
                selection = StatsEntry._ID + "=?"
//                         + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.TEAMS_TABLE_NAME;
                break;
            case TEMP:
                table = StatsEntry.TEMPPLAYERS_TABLE_NAME;
                break;
            case TEMP_ID:
                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.TEMPPLAYERS_TABLE_NAME;
                break;
            case GAME:
                sortOrder = StatsEntry._ID + " ASC";
                table = StatsEntry.GAME_TABLE_NAME;
                break;
            case GAME_ID:
                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.GAME_TABLE_NAME;
                break;
            case BACKUP_PLAYERS:
                table = StatsEntry.BACKUP_PLAYERS_TABLE_NAME;
                break;
            case BACKUP_TEAMS:
                table = StatsEntry.BACKUP_TEAMS_TABLE_NAME;
                break;
            case BOXSCORE_PLAYERS:
                table = StatsEntry.BOXSCORE_PLAYERS_TABLE_NAME;
                break;
            case BOXSCORE_OVERVIEWS:
                table = StatsEntry.BOXSCORE_OVERVIEW_TABLE_NAME;
                break;
            case SELECTIONS:
                table = StatsEntry.SELECTIONS_TABLE_NAME;
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

//    private Cursor querySelection(String[] projection, String selection,
//                                  String[] selectionArgs, String sortOrder) {
//        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
//        return database.query(StatsEntry.SELECTIONS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
//    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        if (match == SELECTIONS) {
            long id = insertSelection(uri, values);
            if(id == -1) {
                return null;
            } else {
                return ContentUris.withAppendedId(uri, id);
            }
        }
        String leagueID = values.getAsString(StatsEntry.COLUMN_LEAGUE_ID);

        if (sqlSafeguard(values)) {
            Toast.makeText(getContext(), "Please only enter letters, numbers, -, and _", Toast.LENGTH_SHORT).show();
            return null;
        }

        String table;
        switch (match) {
            case PLAYERS:
                if (containsName(StatsEntry.CONTENT_URI_PLAYERS, values)) {
                    return null;
                }
                table = StatsEntry.PLAYERS_TABLE_NAME;
                if (values.containsKey(StatsEntry.SYNC)) {
                    values.remove(StatsEntry.SYNC);
                    break;
                }
                mFirestore = FirebaseFirestore.getInstance();
                DocumentReference playerDoc = mFirestore.collection(FirestoreUpdateService.LEAGUE_COLLECTION)
                        .document(leagueID).collection(FirestoreUpdateService.PLAYERS_COLLECTION).document();

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
                if (containsName(StatsEntry.CONTENT_URI_TEAMS, values)) {
                    return null;
                }
                table = StatsEntry.TEAMS_TABLE_NAME;
                if (values.containsKey(StatsEntry.SYNC)) {
                    values.remove(StatsEntry.SYNC);
                    values.remove(StatsEntry.TYPE);
                    break;
                }
                mFirestore = FirebaseFirestore.getInstance();
                DocumentReference teamDoc;
                int selectionType = values.getAsInteger(StatsEntry.TYPE);
                if (selectionType == MainPageSelection.TYPE_TEAM) {
                    teamDoc = mFirestore.collection(FirestoreUpdateService.LEAGUE_COLLECTION)
                            .document(leagueID).collection(FirestoreUpdateService.TEAMS_COLLECTION).document(leagueID);
                } else {
                    teamDoc = mFirestore.collection(FirestoreUpdateService.LEAGUE_COLLECTION)
                            .document(leagueID).collection(FirestoreUpdateService.TEAMS_COLLECTION).document();
                }
                values.remove(StatsEntry.TYPE);

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
            case BOXSCORE_PLAYERS:
                table = StatsEntry.BOXSCORE_PLAYERS_TABLE_NAME;
                break;
            case BOXSCORE_OVERVIEWS:
                table = StatsEntry.BOXSCORE_OVERVIEW_TABLE_NAME;
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

        if (match == SELECTIONS) {
            return deleteSelection(selection, selectionArgs);
        }

        String firestoreID;
        String leagueID;
        switch (match) {
            case PLAYERS:
            case TEAMS:
                if (selectionArgs != null) {
                    if (selectionArgs.length == 1) {
                        leagueID = selectionArgs[0];
                        firestoreID = null;
                    } else {
                        firestoreID = selectionArgs[0];
                        leagueID = selectionArgs[1];
                    }
                } else {
                    return -1;
                }
                break;
            case PLAYERS_ID:
            case TEAMS_ID:
                if (selectionArgs != null) {
                    firestoreID = selectionArgs[0];
                    leagueID = selectionArgs[1];
                } else {
                    return -1;
                }
                break;
            default:
                firestoreID = null;
                leagueID = null;
                break;
        }



        SQLiteDatabase database = mOpenHelper.getWritableDatabase();


        int rowsDeleted;
        String table;

        switch (match) {
            case PLAYERS:
                if (firestoreID != null) {
                    if (inGamePlayerCheck(firestoreID, leagueID)) {
                        return -1;
                    }
                }
                table = StatsEntry.PLAYERS_TABLE_NAME;
                break;

            case PLAYERS_ID:
                if (inGamePlayerCheck(firestoreID, leagueID)) {
                    return -1;
                }
                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.PLAYERS_TABLE_NAME;
                break;

            case TEAMS:
                if (firestoreID != null) {
                    if (inGamePlayerCheck(firestoreID, leagueID)) {
                        return -1;
                    }
                }
                table = StatsEntry.TEAMS_TABLE_NAME;
                break;

            case TEAMS_ID:
                if (inGameTeamCheck(leagueID, firestoreID)) {
                    return -1;
                }
                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.TEAMS_TABLE_NAME;
                break;

            case TEMP:
                table = StatsEntry.TEMPPLAYERS_TABLE_NAME;
                break;

            case TEMP_ID:
                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
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
                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.BACKUP_PLAYERS_TABLE_NAME;
                break;

            case BACKUP_TEAMS:
                table = StatsEntry.BACKUP_TEAMS_TABLE_NAME;
                break;

            case BACKUP_TEAMS_ID:
                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.BACKUP_TEAMS_TABLE_NAME;
                break;
            case BOXSCORE_PLAYERS:
                table = StatsEntry.BOXSCORE_PLAYERS_TABLE_NAME;
                break;
            case BOXSCORE_PLAYER_ID:
                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.BOXSCORE_PLAYERS_TABLE_NAME;
                break;
            case BOXSCORE_OVERVIEWS:
                table = StatsEntry.BOXSCORE_OVERVIEW_TABLE_NAME;
                break;
            case BOXSCORE_OVERVIEW_ID:
                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.BOXSCORE_OVERVIEW_TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        rowsDeleted = database.delete(table, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    private int deleteSelection(String selection, String[] selectionArgs) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        int rowsDeleted = database.delete(StatsEntry.SELECTIONS_TABLE_NAME, selection, selectionArgs);

        rowsDeleted += database.delete(StatsEntry.GAME_TABLE_NAME, selection, selectionArgs);
        rowsDeleted += database.delete(StatsEntry.PLAYERS_TABLE_NAME, selection, selectionArgs);
        rowsDeleted += database.delete(StatsEntry.TEAMS_TABLE_NAME, selection, selectionArgs);
        rowsDeleted += database.delete(StatsEntry.TEMPPLAYERS_TABLE_NAME, selection, selectionArgs);
        rowsDeleted += database.delete(StatsEntry.BACKUP_PLAYERS_TABLE_NAME, selection, selectionArgs);
        rowsDeleted += database.delete(StatsEntry.BACKUP_TEAMS_TABLE_NAME, selection, selectionArgs);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        String leagueID;
        if (values.containsKey(StatsEntry.COLUMN_LEAGUE_ID)) {
            leagueID = values.getAsString(StatsEntry.COLUMN_LEAGUE_ID);
        } else {
            leagueID = null;
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
                documentReference = mFirestore.collection(FirestoreUpdateService.LEAGUE_COLLECTION).document(leagueID)
                        .collection(FirestoreUpdateService.PLAYERS_COLLECTION).document(firestoreID);
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

                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                long id = ContentUris.parseId(uri);
                selectionArgs = new String[]{String.valueOf(id)};
                table = StatsEntry.PLAYERS_TABLE_NAME;

                if (values.containsKey(StatsEntry.SYNC)) {
                    values.remove(StatsEntry.SYNC);
                } else if (containsName(StatsEntry.CONTENT_URI_PLAYERS, values)) {
                    return -1;
                }
                firestoreID = values.getAsString(StatsEntry.COLUMN_FIRESTORE_ID);

                mFirestore = FirebaseFirestore.getInstance();
                documentReference = mFirestore.collection(FirestoreUpdateService.LEAGUE_COLLECTION).document(leagueID)
                        .collection(FirestoreUpdateService.PLAYERS_COLLECTION).document(firestoreID);
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

                    documentReference = mFirestore.collection(FirestoreUpdateService.LEAGUE_COLLECTION).document(leagueID)
                            .collection(FirestoreUpdateService.TEAMS_COLLECTION).document(firestoreID);
                    values.remove(StatsEntry.COLUMN_FIRESTORE_ID);
                    documentReference.update(StatsEntry.COLUMN_NAME, teamName);

                    DocumentReference documentReference1 = mFirestore
                            .collection(FirestoreUpdateService.LEAGUE_COLLECTION).document(leagueID);
                    documentReference1.update(StatsEntry.COLUMN_NAME, teamName);
                }
                break;
            case TEAMS_ID:

                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.TEAMS_TABLE_NAME;
                if (values.containsKey(StatsEntry.SYNC)) {
                    values.remove(StatsEntry.SYNC);
                } else if (containsName(StatsEntry.CONTENT_URI_TEAMS, values)) {
                    return -1;
                }
                if (values.containsKey(StatsEntry.COLUMN_NAME)) {
                    firestoreID = values.getAsString(StatsEntry.COLUMN_FIRESTORE_ID);
                    mFirestore = FirebaseFirestore.getInstance();
                    documentReference = mFirestore.collection(FirestoreUpdateService.LEAGUE_COLLECTION).document(leagueID)
                            .collection(FirestoreUpdateService.TEAMS_COLLECTION).document(firestoreID);
                    values.remove(StatsEntry.COLUMN_FIRESTORE_ID);
                    String teamName = values.getAsString(StatsEntry.COLUMN_NAME);
                    documentReference.update(StatsEntry.COLUMN_NAME, teamName);
                }
                break;
            case TEMP:
                table = StatsEntry.TEMPPLAYERS_TABLE_NAME;
                break;
            case TEMP_ID:

                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.TEMPPLAYERS_TABLE_NAME;
                break;
            case GAME:
                table = StatsEntry.GAME_TABLE_NAME;
                break;
            case GAME_ID:

                selection = StatsEntry._ID + "=?"
//                        + " AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?"
                ;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.GAME_TABLE_NAME;
                break;
            case BOXSCORE_OVERVIEWS:
                table = StatsEntry.BOXSCORE_OVERVIEW_TABLE_NAME;
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

    private long insertSelection(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        long id = database.insert(StatsEntry.SELECTIONS_TABLE_NAME, null, values);
        return id;
    }

    private boolean containsName(Uri uri, ContentValues values) {
        String leagueID = values.getAsString(StatsEntry.COLUMN_LEAGUE_ID);
        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{leagueID};
        if (values.containsKey(StatsEntry.COLUMN_NAME)) {
            String name = values.getAsString(StatsEntry.COLUMN_NAME).toLowerCase();
            if (name.trim().isEmpty()) {
                Toast.makeText(getContext(), R.string.please_enter_name_first, Toast.LENGTH_LONG).show();
                return true;
            }
            String[] projection = new String[]{StatsEntry.COLUMN_NAME};
            Cursor cursor = query(uri, projection, selection, selectionArgs, null);
            while (cursor.moveToNext()) {
                String failedName = (StatsContract.getColumnString(cursor, StatsEntry.COLUMN_NAME)).toLowerCase();
                if (failedName.equals(name)) {
                    String toastText = String.format("%1$s already exists!", failedName);
                    Toast.makeText(getContext(), toastText, Toast.LENGTH_SHORT).show();
                    cursor.close();
                    return true;
                }
            }
            cursor.close();
        }
        return false;
    }

    private boolean inGamePlayerCheck(String firestoreID, String statkeeperID) {
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{firestoreID, statkeeperID};

        Cursor cursor = query(StatsEntry.CONTENT_URI_TEMP, null, selection, selectionArgs, null);
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
        String team = gamePreferences.getString(StatsEntry.COLUMN_AWAY_TEAM, null);
        if (firestoreID.equals(team)) {
            Toast.makeText(getContext(), "Team is in an active game!", Toast.LENGTH_SHORT).show();
            return true;
        }
        team = gamePreferences.getString(StatsEntry.COLUMN_HOME_TEAM, null);
        if (firestoreID.equals(team)) {
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
