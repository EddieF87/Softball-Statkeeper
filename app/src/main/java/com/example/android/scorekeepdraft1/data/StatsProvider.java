package com.example.android.scorekeepdraft1.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

/**
 * Created by Eddie on 16/08/2017.
 */

public class StatsProvider extends ContentProvider {

    public static final String LOG_TAG = StatsProvider.class.getSimpleName();

    public static final int PLAYERS = 100;
    public static final int PLAYERS_ID = 101;
    public static final int TEAMS = 200;
    public static final int TEAMS_ID = 201;
    public static final int TEMP = 300;
    public static final int TEMP_ID = 301;
    public static final int GAME = 400;
    public static final int GAME_ID = 401;

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
// Get readable database
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor;
        // Figure out if the URI matcher can match the URI to a specific code
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
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLAYERS:
                return insertPlayer(uri, values);
            case TEAMS:
                return insertTeam(uri, values);
            case TEMP:
                return insertTempPlayer(uri, values);
            case GAME:
                return insertGamePlay(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }    }

    private Uri insertGamePlay(Uri uri, ContentValues values) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        // Insert the new play with the given values
        long id = database.insert(StatsEntry.GAME_TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertTempPlayer (Uri uri, ContentValues values) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        // Insert the new play with the given values
        long id = database.insert(StatsEntry.TEMPPLAYERS_TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertPlayer (Uri uri, ContentValues values) {
        // Check that the name is not null
        if (values.containsKey(StatsEntry.COLUMN_NAME)) {
            String name = values.getAsString(StatsEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Player requires a name");
            }
        }
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        // Insert the new pet with the given values
        long id = database.insert(StatsEntry.PLAYERS_TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertTeam (Uri uri, ContentValues values) {
        // Check that the name is not null
        if (values.containsKey(StatsEntry.COLUMN_NAME)) {
            String name = values.getAsString(StatsEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Team requires a name");
            }
            String[] projection = new String[] {StatsEntry.COLUMN_NAME};
            Cursor cursor = query(StatsEntry.CONTENT_URI_TEAMS, projection, null, null, null);
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
                String teamName = cursor.getString(nameIndex);
                if (teamName.equals(name)) {
                    Toast.makeText(getContext(), "This team already exists!", Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
        }
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        // Insert the new pet with the given values
        long id = database.insert(StatsEntry.TEAMS_TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
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
                // Delete a single row given by the ID in the URI
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StatsEntry.PLAYERS_TABLE_NAME, selection, selectionArgs);
                break;

            case TEAMS_ID:
                // Delete a single row given by the ID in the URI
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StatsEntry.TEAMS_TABLE_NAME, selection, selectionArgs);
                break;

            case TEMP:
                rowsDeleted = database.delete(StatsEntry.TEMPPLAYERS_TABLE_NAME, selection, selectionArgs);
                break;

            case TEMP_ID:
                // Delete a single row given by the ID in the URI
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StatsEntry.TEMPPLAYERS_TABLE_NAME, selection, selectionArgs);
                break;

            case GAME:
                rowsDeleted = database.delete(StatsEntry.GAME_TABLE_NAME, selection, selectionArgs);
                break;

            case GAME_ID:
                // Delete a single row given by the ID in the URI
                selection = StatsEntry._ID + ">?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                //Cursor cursor = query(StatsEntry.CONTENT_URI_GAMELOG, null, selection, selectionArgs, null);

                rowsDeleted = database.delete(StatsEntry.GAME_TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLAYERS:
                return updatePlayer(uri, values, selection, selectionArgs);
            case PLAYERS_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePlayer(uri, values, selection, selectionArgs);

            case TEAMS:
                return updateTeam(uri, values, selection, selectionArgs);

            case TEMP:
                return updateTempPlayer(uri, values, selection, selectionArgs);
            case TEMP_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateTempPlayer(uri, values, selection, selectionArgs);

            case GAME:
                return updateGame(uri, values, selection, selectionArgs);
            case GAME_ID:
                selection = StatsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateGame(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateTempPlayer(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(StatsEntry.TEMPPLAYERS_TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the given URI has changed
        if (rowsUpdated != 0) {getContext().getContentResolver().notifyChange(uri, null);}
        return rowsUpdated;
    }

    private int updatePlayer(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(StatsEntry.PLAYERS_TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the given URI has changed
        if (rowsUpdated != 0) {getContext().getContentResolver().notifyChange(uri, null);}
        return rowsUpdated;
    }

    private int updateTeam(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(StatsEntry.TEAMS_TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the given URI has changed
        if (rowsUpdated != 0) {getContext().getContentResolver().notifyChange(uri, null);}
        return rowsUpdated;
    }

    private int updateGame(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(StatsEntry.GAME_TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the given URI has changed
        if (rowsUpdated != 0) {getContext().getContentResolver().notifyChange(uri, null);}
        return rowsUpdated;
    }
}
