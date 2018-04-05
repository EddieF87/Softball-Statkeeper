package xyz.sleekstats.softball.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import xyz.sleekstats.softball.data.StatsContract.StatsEntry;

/**
 * Created by Eddie on 16/08/2017.
 */

public class StatsDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "playerstats.db";
    private static final int DATABASE_VERSION = 1;


    public StatsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PLAYERSTATS_TABLE =
                "CREATE TABLE " + StatsEntry.PLAYERS_TABLE_NAME + " (" +
                        StatsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_FIRESTORE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_LEAGUE_ID + " TEXT NOT NULL, " +

                        StatsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_TEAM + " TEXT DEFAULT '" + StatsEntry.FREE_AGENT + "', " +
                        StatsEntry.COLUMN_TEAM_FIRESTORE_ID + " TEXT DEFAULT '" + StatsEntry.FREE_AGENT + "', " +
                        StatsEntry.COLUMN_ORDER + " INTEGER, " +
                        StatsEntry.COLUMN_GENDER + " INTEGER DEFAULT 0, " +

                        StatsEntry.COLUMN_1B + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_2B + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_3B + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_HR + " INTEGER DEFAULT 0, " +

                        StatsEntry.COLUMN_BB + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_SF + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_OUT + " INTEGER DEFAULT 0, " +

                        StatsEntry.COLUMN_RUN + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_RBI + " INTEGER DEFAULT 0, " +

                        StatsEntry.COLUMN_G + " INTEGER DEFAULT 0" +
                        ");";

        final String SQL_CREATE_TEMPPLAYERSTATS_TABLE =
                "CREATE TABLE " + StatsEntry.TEMPPLAYERS_TABLE_NAME + " (" +
                        StatsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_FIRESTORE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_LEAGUE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_PLAYERID + " INTEGER NOT NULL, " +
                        StatsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_TEAM + " TEXT DEFAULT '" + StatsEntry.FREE_AGENT + "', " +
                        StatsEntry.COLUMN_TEAM_FIRESTORE_ID + " TEXT DEFAULT '" + StatsEntry.FREE_AGENT + "', " +
                        StatsEntry.COLUMN_ORDER + " INTEGER, " +
                        StatsEntry.COLUMN_GENDER + " INTEGER, " +

                        StatsEntry.COLUMN_1B + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_2B + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_3B + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_HR + " INTEGER DEFAULT 0, " +

                        StatsEntry.COLUMN_BB + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_SF + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_OUT + " INTEGER DEFAULT 0, " +

                        StatsEntry.COLUMN_RUN + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_RBI + " INTEGER DEFAULT 0" +
                        ");";

        final String SQL_CREATE_BACKUP_PLAYERSTATS_TABLE =
                "CREATE TABLE " + StatsEntry.BACKUP_PLAYERS_TABLE_NAME + " (" +
                        StatsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_FIRESTORE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_LEAGUE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_GAME_ID + " INTEGER NOT NULL, " +
                        StatsEntry.COLUMN_PLAYERID + " INTEGER NOT NULL, " +
                        StatsEntry.COLUMN_TEAM_FIRESTORE_ID + " TEXT DEFAULT '" + StatsEntry.FREE_AGENT + "', " +
                        StatsEntry.COLUMN_1B + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_2B + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_3B + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_HR + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_BB + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_SF + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_OUT + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_RUN + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_RBI + " INTEGER DEFAULT 0" +
                        ");";

        final String SQL_CREATE_TEAMSTATS_TABLE =
                "CREATE TABLE " + StatsEntry.TEAMS_TABLE_NAME + " (" +
                        StatsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_FIRESTORE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_LEAGUE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_LEAGUE + " TEXT, " +

                        StatsEntry.COLUMN_WINS + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_LOSSES + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_TIES + " INTEGER DEFAULT 0, " +

                        StatsEntry.COLUMN_RUNSFOR + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_RUNSAGAINST + " INTEGER DEFAULT 0" +
                        ");";

        final String SQL_CREATE_BACKUP_TEAMSTATS_TABLE =
                "CREATE TABLE " + StatsEntry.BACKUP_TEAMS_TABLE_NAME + " (" +
                        StatsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_FIRESTORE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_LEAGUE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_GAME_ID + " INTEGER NOT NULL, " +
                        StatsEntry.COLUMN_TEAM_ID + " INTEGER NOT NULL, " +
                        StatsEntry.COLUMN_WINS + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_LOSSES + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_TIES + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_RUNSFOR + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_RUNSAGAINST + " INTEGER DEFAULT 0" +
                        ");";

        final String SQL_CREATE_GAMESTATS_TABLE =
                "CREATE TABLE " + StatsEntry.GAME_TABLE_NAME + " (" +
                        StatsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_LEAGUE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_PLAY + " TEXT, " +
                        StatsEntry.COLUMN_TEAM + " INTEGER, " +
                        StatsEntry.COLUMN_BATTER + " TEXT, " +
                        StatsEntry.COLUMN_ONDECK + " TEXT, " +

                        StatsEntry.COLUMN_1B + " TEXT, " +
                        StatsEntry.COLUMN_2B + " TEXT, " +
                        StatsEntry.COLUMN_3B + " TEXT, " +
                        StatsEntry.COLUMN_OUT + " INTEGER, " +
                        StatsEntry.COLUMN_AWAY_RUNS + " INTEGER, " +
                        StatsEntry.COLUMN_HOME_RUNS + " INTEGER, " +

                        StatsEntry.COLUMN_RUN1 + " TEXT, " +
                        StatsEntry.COLUMN_RUN2 + " TEXT, " +
                        StatsEntry.COLUMN_RUN3 + " TEXT, " +
                        StatsEntry.COLUMN_RUN4 + " TEXT, " +

                        StatsEntry.COLUMN_INNING_CHANGED + " INTEGER, " +
                        StatsEntry.INNINGS + " INTEGER" +
                        ");";

        final String SQL_CREATE_LEAGUES_TABLE =
                "CREATE TABLE " + StatsEntry.SELECTIONS_TABLE_NAME + " (" +
                        StatsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_FIRESTORE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_LEAGUE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        StatsEntry.TYPE + " INTEGER NOT NULL, " +
                        StatsEntry.LEVEL + " INTEGER NOT NULL" +
                        ");";

        final String SQL_CREATE_BOXSCORE_OVERVIEW_TABLE =
                "CREATE TABLE " + StatsEntry.BOXSCORE_OVERVIEW_TABLE_NAME + " (" +
                        StatsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_LEAGUE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_GAME_ID + " INTEGER NOT NULL, " +
                        StatsEntry.COLUMN_AWAY_TEAM + " TEXT DEFAULT '" + StatsEntry.COLUMN_AWAY_TEAM + "', " +
                        StatsEntry.COLUMN_HOME_TEAM + " TEXT DEFAULT '" + StatsEntry.COLUMN_HOME_TEAM + "', " +
                        StatsEntry.COLUMN_AWAY_RUNS + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_HOME_RUNS + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_LOCAL + " INTEGER DEFAULT 0" +
                        ");";

        final String SQL_CREATE_BOXSCORE_PLAYERS_TABLE =
                "CREATE TABLE " + StatsEntry.BOXSCORE_PLAYERS_TABLE_NAME + " (" +
                        StatsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_LEAGUE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_GAME_ID + " INTEGER NOT NULL, " +
                        StatsEntry.COLUMN_FIRESTORE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_TEAM_FIRESTORE_ID + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_1B + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_2B + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_3B + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_HR + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_BB + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_SF + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_OUT + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_RUN + " INTEGER DEFAULT 0, " +
                        StatsEntry.COLUMN_RBI + " INTEGER DEFAULT 0" +
                        ");";


        db.execSQL(SQL_CREATE_LEAGUES_TABLE);
        db.execSQL(SQL_CREATE_PLAYERSTATS_TABLE);
        db.execSQL(SQL_CREATE_TEAMSTATS_TABLE);
        db.execSQL(SQL_CREATE_TEMPPLAYERSTATS_TABLE);
        db.execSQL(SQL_CREATE_GAMESTATS_TABLE);
        db.execSQL(SQL_CREATE_BACKUP_PLAYERSTATS_TABLE);
        db.execSQL(SQL_CREATE_BACKUP_TEAMSTATS_TABLE);
        db.execSQL(SQL_CREATE_BOXSCORE_OVERVIEW_TABLE);
        db.execSQL(SQL_CREATE_BOXSCORE_PLAYERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + StatsEntry.PLAYERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + StatsEntry.PLAYERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + StatsEntry.TEAMS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + StatsEntry.TEMPPLAYERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + StatsEntry.GAME_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + StatsEntry.BACKUP_PLAYERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + StatsEntry.BACKUP_TEAMS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + StatsEntry.BOXSCORE_OVERVIEW_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + StatsEntry.BOXSCORE_PLAYERS_TABLE_NAME);
        onCreate(db);
    }
}