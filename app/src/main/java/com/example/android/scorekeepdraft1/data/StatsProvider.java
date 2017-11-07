package com.example.android.scorekeepdraft1.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.Player;
import com.example.android.scorekeepdraft1.Team;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eddie on 16/08/2017.
 */

public class StatsProvider extends ContentProvider {

    private FirebaseFirestore mFirestore;

    public static final String LOG_TAG = StatsProvider.class.getSimpleName();

    public static final int PLAYERS = 100;
    public static final int PLAYERS_ID = 101;
    public static final int TEAMS = 200;
    public static final int TEAMS_ID = 201;
    public static final int TEMP = 300;
    public static final int TEMP_ID = 301;
    public static final int GAME = 400;
    public static final int GAME_ID = 401;
    public static final int BACKUP_PLAYERS = 500;
    public static final int BACKUP_PLAYERS_ID = 501;
    public static final int BACKUP_TEAMS = 600;
    public static final int BACKUP_TEAMS_ID = 601;


    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private StatsDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {
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

        return matcher;
    }

//TODO prevent sql injection when player enters info and elsewhere
    @Override
    public boolean onCreate() {
        mOpenHelper = new StatsDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PLAYERS:
                cursor = database.query(StatsEntry.PLAYERS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PLAYERS_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(StatsEntry.PLAYERS_TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case TEAMS:
                cursor = database.query(StatsEntry.TEAMS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TEAMS_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(StatsEntry.TEAMS_TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case TEMP:
                cursor = database.query(StatsEntry.TEMPPLAYERS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TEMP_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(StatsEntry.TEMPPLAYERS_TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case GAME:
                sortOrder = StatsEntry._ID + " ASC";
                cursor = database.query(StatsEntry.GAME_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case GAME_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(StatsEntry.GAME_TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BACKUP_PLAYERS:
                cursor = database.query(StatsEntry.BACKUP_PLAYERS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BACKUP_TEAMS:
                cursor = database.query(StatsEntry.BACKUP_TEAMS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLAYERS:
                return StatsEntry.CONTENT_PLAYERS_TYPE;
            case PLAYERS_ID:
                return StatsEntry.CONTENT_TEAMS_TYPE;
            //TODO: learn/fix getType?
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (sqlSafeguard(values)){
            Toast.makeText(getContext(), "Please only enter letters, numbers, -, and _", Toast.LENGTH_SHORT).show();
            return null;
        }
        boolean sync = false;
        String table;
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PLAYERS:
                if (containsName(StatsEntry.CONTENT_URI_PLAYERS, values, false)){return null;}
                table = StatsEntry.PLAYERS_TABLE_NAME;
                if (values.containsKey("sync")) {
                    sync = true;
                    values.remove("sync");
                }
                break;
            case TEAMS:
                if (containsName(StatsEntry.CONTENT_URI_TEAMS, values, true)){return null;}
                table = StatsEntry.TEAMS_TABLE_NAME;
                if (values.containsKey("sync")) {
                    sync = true;
                    values.remove("sync");
                }
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
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        long id = database.insert(table, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        } else {
            Log.e(LOG_TAG, "Insert row for " + values.getAsString(StatsEntry.COLUMN_NAME) + "  " + uri);
        }
        switch (match) {
            case PLAYERS:
                if(sync) {break;}
                mFirestore = FirebaseFirestore.getInstance();
                CollectionReference players = mFirestore.collection("players");
                String playerName = values.getAsString(StatsEntry.COLUMN_NAME);
                Map<String, Object> player = new HashMap<>();
                if (values.containsKey(StatsEntry.COLUMN_TEAM)) {
                    String teamName = values.getAsString(StatsEntry.COLUMN_TEAM);
                    player.put("team", teamName);
                } else {
                    player.put("team", "Free Agent");
                }
                player.put("name", playerName);
                players.document(playerName).set(player);
                break;
            case TEAMS:
                if(sync) {break;}
                mFirestore = FirebaseFirestore.getInstance();
                CollectionReference teams = mFirestore.collection("teams");
                String teamName = values.getAsString(StatsEntry.COLUMN_NAME);
                Team team = new Team(teamName, id);
                teams.document(teamName).set(team);
                break;
            default:
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        int rowsDeleted;

        switch (match) {
            case PLAYERS:
                rowsDeleted = database.delete(StatsEntry.PLAYERS_TABLE_NAME, selection, selectionArgs);
                break;

            case PLAYERS_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StatsEntry.PLAYERS_TABLE_NAME, selection, selectionArgs);
                break;

            case TEAMS_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StatsEntry.TEAMS_TABLE_NAME, selection, selectionArgs);
                break;

            case TEMP:
                rowsDeleted = database.delete(StatsEntry.TEMPPLAYERS_TABLE_NAME, selection, selectionArgs);
                break;

            case TEMP_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StatsEntry.TEMPPLAYERS_TABLE_NAME, selection, selectionArgs);
                break;

            case GAME:
                rowsDeleted = database.delete(StatsEntry.GAME_TABLE_NAME, selection, selectionArgs);
                break;

            case GAME_ID:
                selection = StatsEntry._ID + ">?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StatsEntry.GAME_TABLE_NAME, selection, selectionArgs);
                break;

            case BACKUP_PLAYERS:
                rowsDeleted = database.delete(StatsEntry.BACKUP_PLAYERS_TABLE_NAME, selection, selectionArgs);
                break;

            case BACKUP_PLAYERS_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StatsEntry.BACKUP_PLAYERS_TABLE_NAME, selection, selectionArgs);
                break;

            case BACKUP_TEAMS:
                rowsDeleted = database.delete(StatsEntry.BACKUP_TEAMS_TABLE_NAME, selection, selectionArgs);
                break;

            case BACKUP_TEAMS_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StatsEntry.BACKUP_TEAMS_TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted < 1) {
            Log.e(LOG_TAG, "Failed to delete row for " + uri);
        } else {
            Log.e(LOG_TAG, "Deleted row for " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.e(LOG_TAG, "Update attempt for " + uri + "  " + values.getAsString(StatsEntry.COLUMN_NAME));
        if (sqlSafeguard(values)){
            Toast.makeText(getContext(), "Please only enter letters, numbers, -, and _", Toast.LENGTH_SHORT).show();
            return -1;
        }
        String table;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLAYERS:
                table = StatsEntry.PLAYERS_TABLE_NAME;
                break;
            case PLAYERS_ID:
                selection = StatsEntry._ID + "=?";
                long id = ContentUris.parseId(uri);
                selectionArgs = new String[]{String.valueOf(id)};
                table = StatsEntry.PLAYERS_TABLE_NAME;
                if (containsName(StatsEntry.CONTENT_URI_PLAYERS, values, false)){return -1;}
                break;
            case TEAMS:
                table = StatsEntry.TEAMS_TABLE_NAME;
                break;
            case TEAMS_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                table = StatsEntry.TEAMS_TABLE_NAME;
                if (containsName(StatsEntry.CONTENT_URI_TEAMS, values, true)){return -1;}
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
        int rowsUpdated = database.update(table, values, selection, selectionArgs);
        if (rowsUpdated != 0) {getContext().getContentResolver().notifyChange(uri, null);}
        return rowsUpdated;
    }


    public boolean containsName(Uri uri, ContentValues values, boolean isTeam) {
        if (values.containsKey(StatsEntry.COLUMN_NAME)) {
            String name = values.getAsString(StatsEntry.COLUMN_NAME);
            if (name == null || name.trim().isEmpty()) {
                Toast.makeText(getContext(), "Please enter a name first!", Toast.LENGTH_SHORT).show();
                return true;
            }
            String[] projection = new String[] {StatsEntry.COLUMN_NAME};
            Cursor cursor = query(uri, projection, null, null, null);
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
                String teamName = cursor.getString(nameIndex);
                if (teamName.equals(name)) {
                    if(isTeam) {
                        Toast.makeText(getContext(), "This team already exists!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "This player already exists!", Toast.LENGTH_SHORT).show();
                    }
                    cursor.close();
                    return true;
                }
            }
            cursor.close();
        }

        return false;
    }

    public boolean sqlSafeguard(ContentValues values) {
        if (!values.containsKey(StatsEntry.COLUMN_NAME)) {
            return false;
        }
        String name = values.getAsString(StatsEntry.COLUMN_NAME);
        if (name.matches("^['\\s\\)\\(a-zA-Z0-9_-]+$")) {
            return false;
        } else {
            return true;
        }
    }
}
