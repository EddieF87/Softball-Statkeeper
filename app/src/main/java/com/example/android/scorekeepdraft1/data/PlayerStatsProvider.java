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

import com.example.android.scorekeepdraft1.data.PlayerStatsContract.PlayerStatsEntry;

/**
 * Created by Eddie on 16/08/2017.
 */

public class PlayerStatsProvider extends ContentProvider {

    public static final String LOG_TAG = PlayerStatsProvider.class.getSimpleName();

    public static final int STATS = 100;
    public static final int STATS_ID = 101;
    public static final int TEAMS = 102;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PlayerDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PlayerStatsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PlayerStatsContract.PATH_STATS, STATS);
        matcher.addURI(authority, PlayerStatsContract.PATH_STATS + "/#", STATS_ID);
        matcher.addURI(authority, PlayerStatsContract.PATH_TEAMS, TEAMS);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new PlayerDbHelper(getContext());
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
            case STATS:
                cursor = database.query(PlayerStatsEntry.PLAYERS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            /*case STATS_ID:
                selection = PlayerStatsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(PlayerStatsEntry.PLAYERS_TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;*/
            case TEAMS:
                cursor = database.query(PlayerStatsEntry.TEAMS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
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
            case STATS:
                return PlayerStatsEntry.CONTENT_LIST_TYPE;
            case STATS_ID:
                return PlayerStatsEntry.CONTENT_ITEM_TYPE;
            //TODO: enter teams type
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STATS:
                return insertPlayer(uri, values);
            case TEAMS:
                //TODO: enter insertTeam method
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }    }

    private Uri insertPlayer (Uri uri, ContentValues values) {
        // Check that the name is not null
        if (values.containsKey(PlayerStatsEntry.COLUMN_NAME)) {
            String name = values.getAsString(PlayerStatsEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Player requires a name");
            }
        }

        SQLiteDatabase database = mOpenHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(PlayerStatsEntry.PLAYERS_TABLE_NAME, null, values);
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
        return 0;
        //TODO: enter delete player and delete team logic
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STATS:
                return updatePlayer(uri, values, selection, selectionArgs);
            case TEAMS:
                //TODO: enter updateTeam method
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    
    private int updatePlayer(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(PlayerStatsEntry.PLAYERS_TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
