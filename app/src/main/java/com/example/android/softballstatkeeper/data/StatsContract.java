package com.example.android.softballstatkeeper.data;

/*
 * Created by Eddie on 16/08/2017.
 */

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class StatsContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.softballstatkeeper";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PLAYERS = "players";
    public static final String PATH_TEAMS = "teams";
    public static final String PATH_TEMP = "temps";
    public static final String PATH_GAME = "game";
    public static final String PATH_BACKUP_PLAYERS = "backupplayers";
    public static final String PATH_BACKUP_TEAMS = "backupteams";
    public static final String PATH_SELECTIONS = "selections";

    public StatsContract() {
    }

    public static final class StatsEntry implements BaseColumns {

        public static final Uri CONTENT_URI_PLAYERS = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PLAYERS);
        public static final Uri CONTENT_URI_TEAMS = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TEAMS);
        public static final Uri CONTENT_URI_BACKUP_PLAYERS = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BACKUP_PLAYERS);
        public static final Uri CONTENT_URI_BACKUP_TEAMS = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BACKUP_TEAMS);
        public static final Uri CONTENT_URI_TEMP = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TEMP);
        public static final Uri CONTENT_URI_GAMELOG = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GAME);
        public static final Uri CONTENT_URI_SELECTIONS = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SELECTIONS);


        public static final String PLAYERS_TABLE_NAME = "players";
        public static final String TEAMS_TABLE_NAME = "teams";
        public static final String BACKUP_PLAYERS_TABLE_NAME = "backupplayers";
        public static final String BACKUP_TEAMS_TABLE_NAME = "backupteams";
        public static final String TEMPPLAYERS_TABLE_NAME = "temps";
        public static final String GAME_TABLE_NAME = "game";
        public static final String SELECTIONS_TABLE_NAME = "selections";


        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TEAM = "team";
        public static final String COLUMN_ORDER = "batting";

        public static final String COLUMN_G = "games";
        public static final String COLUMN_1B = "single";
        public static final String COLUMN_2B = "double";
        public static final String COLUMN_3B = "triple";
        public static final String COLUMN_HR = "hr";

        public static final String COLUMN_BB = "bb";
        public static final String COLUMN_OUT = "out";
        public static final String COLUMN_SF = "sf";

        public static final String COLUMN_RBI = "rbi";
        public static final String COLUMN_RUN = "runs";

        public static final String COLUMN_LEAGUE = "league";
        public static final String COLUMN_WINS = "wins";
        public static final String COLUMN_LOSSES= "losses";
        public static final String COLUMN_TIES = "ties";
        public static final String COLUMN_RUNSFOR = "runsfor";
        public static final String COLUMN_RUNSAGAINST = "runsagainst";


        public static final String CONTENT_PLAYERS_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_PLAYERS;

        public static final String CONTENT_TEAMS_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_TEAMS;
        
        public static final String COLUMN_PLAY = "play";
        public static final String COLUMN_BATTER = "batter";
        public static final String COLUMN_ONDECK = "ondeck";

        public static final String COLUMN_AWAY_RUNS = "awayruns";
        public static final String COLUMN_HOME_RUNS = "homeruns";

        public static final String COLUMN_RUN1 = "runa";
        public static final String COLUMN_RUN2 = "runb";
        public static final String COLUMN_RUN3 = "runc";
        public static final String COLUMN_RUN4 = "rund";
        public static final String COLUMN_INNING_CHANGED = "innchange";
        public static final String COLUMN_LOG_INDEX = "logindex";
        public static final String COLUMN_PLAYERID = "playerid";
        public static final String COLUMN_TEAM_ID = "teamid";
        public static final String COLUMN_LOG_ID = "logid";
        public static final String COLUMN_FIRESTORE_ID = "firestoreid";
        public static final String COLUMN_LEAGUE_ID = "leagueid";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_TEAM_FIRESTORE_ID = "teamfirestoreid";


        public static final String SETTINGS = "settings";
        public static final String GAME = "game";
        public static final String INNINGS = "innings";
        public static final String UPDATE = "update";
        public static final String TYPE = "type";
        public static final String TIME = "time";
        public static final String ADD = "add";
        public static final String DELETE = "delete";
        public static final String SYNC = "sync";
        public static final String EMAIL = "email";
        public static final String LEVEL = "level";
        public static final String FREE_AGENT = "Free Agent";
    }

    /* Helpers to retrieve column values */
    public static String getColumnString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName) );
    }

    public static int getColumnInt(Cursor cursor, String columnName) {
        return cursor.getInt( cursor.getColumnIndex(columnName) );
    }

    public static long getColumnLong(Cursor cursor, String columnName) {
        return cursor.getLong( cursor.getColumnIndex(columnName) );
    }

}
