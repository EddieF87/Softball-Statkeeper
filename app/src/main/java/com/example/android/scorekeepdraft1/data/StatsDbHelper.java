package com.example.android.scorekeepdraft1.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

/**
 * Created by Eddie on 16/08/2017.
 */

public class StatsDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "playerstats.db";
    private static final int DATABASE_VERSION = 1;


    public StatsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PLAYERSTATS_TABLE =
                "CREATE TABLE " + StatsEntry.PLAYERS_TABLE_NAME + " (" +
                        StatsEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_TEAM + " TEXT DEFAULT 'FA', " +
                        StatsEntry.COLUMN_ORDER + " INTEGER, " +

                        //StatsEntry.COLUMN_G + " INTEGER, " +
                        StatsEntry.COLUMN_1B + " INTEGER, " +
                        StatsEntry.COLUMN_2B + " INTEGER, " +
                        StatsEntry.COLUMN_3B + " INTEGER, " +
                        StatsEntry.COLUMN_HR + " INTEGER, " +

                        StatsEntry.COLUMN_BB + " INTEGER, " +
                        StatsEntry.COLUMN_SF + " INTEGER, " +
                        StatsEntry.COLUMN_OUT + " INTEGER, " +


                        StatsEntry.COLUMN_RUN + " INTEGER, " +
                        StatsEntry.COLUMN_RBI + " INTEGER" +
                        ");" ;

        final String SQL_CREATE_TEMPPLAYERSTATS_TABLE =
                "CREATE TABLE " + StatsEntry.TEMP_TABLE_NAME + " (" +
                        StatsEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_TEAM + " TEXT DEFAULT 'FA', " +
                        StatsEntry.COLUMN_ORDER + " INTEGER, " +

                        StatsEntry.COLUMN_1B + " INTEGER, " +
                        StatsEntry.COLUMN_2B + " INTEGER, " +
                        StatsEntry.COLUMN_3B + " INTEGER, " +
                        StatsEntry.COLUMN_HR + " INTEGER, " +

                        StatsEntry.COLUMN_BB + " INTEGER, " +
                        StatsEntry.COLUMN_SF + " INTEGER, " +
                        StatsEntry.COLUMN_OUT + " INTEGER, " +

                        StatsEntry.COLUMN_RUN + " INTEGER, " +
                        StatsEntry.COLUMN_RBI + " INTEGER" +
                        ");" ;

        final String SQL_CREATE_TEAMSTATS_TABLE =
                "CREATE TABLE " + StatsEntry.TEAMS_TABLE_NAME + " (" +
                        StatsEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_LEAGUE + " TEXT, " +

                        StatsEntry.COLUMN_WINS + " INTEGER, " +
                        StatsEntry.COLUMN_LOSSES + " INTEGER, " +
                        StatsEntry.COLUMN_TIES + " INTEGER, " +

                        StatsEntry.COLUMN_RUNSFOR + " INTEGER, " +
                        StatsEntry.COLUMN_RUNSAGAINST + " INTEGER" +
                        ");" ;

                db.execSQL(SQL_CREATE_PLAYERSTATS_TABLE);
                db.execSQL(SQL_CREATE_TEAMSTATS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + StatsEntry.PLAYERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + StatsEntry.TEAMS_TABLE_NAME);
        onCreate(db);
    }
}
