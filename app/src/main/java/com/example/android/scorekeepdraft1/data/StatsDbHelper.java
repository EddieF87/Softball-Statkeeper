package com.example.android.scorekeepdraft1.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.scorekeepdraft1.data.StatsContract.PlayerStatsEntry;

/**
 * Created by Eddie on 16/08/2017.
 */

public class StatsDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "playerstats.db";
    private static final int DATABASE_VERSION = 3;


    public StatsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PLAYERSTATS_TABLE =
                "CREATE TABLE " + PlayerStatsEntry.PLAYERS_TABLE_NAME + " (" +
                        PlayerStatsEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PlayerStatsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        PlayerStatsEntry.COLUMN_TEAM + " TEXT, " +
                        PlayerStatsEntry.COLUMN_ORDER + " INTEGER, " +

                        PlayerStatsEntry.COLUMN_1B + " INTEGER, " +
                        PlayerStatsEntry.COLUMN_2B + " INTEGER, " +
                        PlayerStatsEntry.COLUMN_3B + " INTEGER, " +
                        PlayerStatsEntry.COLUMN_HR + " INTEGER, " +

                        PlayerStatsEntry.COLUMN_BB + " INTEGER, " +
                        PlayerStatsEntry.COLUMN_SF + " INTEGER, " +
                        PlayerStatsEntry.COLUMN_OUT + " INTEGER, " +

                        PlayerStatsEntry.COLUMN_RUN + " INTEGER, " +
                        PlayerStatsEntry.COLUMN_RBI + " INTEGER" +
                        ");" ;

        final String SQL_CREATE_TEAMSTATS_TABLE =
                "CREATE TABLE " + PlayerStatsEntry.TEAMS_TABLE_NAME + " (" +
                        PlayerStatsEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PlayerStatsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        PlayerStatsEntry.COLUMN_LEAGUE + " TEXT, " +

                        PlayerStatsEntry.COLUMN_WINS + " INTEGER, " +
                        PlayerStatsEntry.COLUMN_LOSSES + " INTEGER, " +
                        PlayerStatsEntry.COLUMN_TIES + " INTEGER, " +

                        PlayerStatsEntry.COLUMN_RUNSFOR + " INTEGER, " +
                        PlayerStatsEntry.COLUMN_RUNSAGAINST + " INTEGER" +

/*                   remember commas     PlayerStatsEntry.B1 + " INTEGER, " +
                        PlayerStatsEntry.B2 + " INTEGER, " +
                        PlayerStatsEntry.B3 + " INTEGER, " +
                        PlayerStatsEntry.B4 + " INTEGER, " +
                        PlayerStatsEntry.B5 + " INTEGER, " +
                        PlayerStatsEntry.B6 + " INTEGER, " +
                        PlayerStatsEntry.B7 + " INTEGER, " +
                        PlayerStatsEntry.B8 + " INTEGER, " +
                        PlayerStatsEntry.B9 + " INTEGER, " +
                        PlayerStatsEntry.B10 + " INTEGER, " +
                        PlayerStatsEntry.B11 + " INTEGER, " +
                        PlayerStatsEntry.B12 + " INTEGER, " +
                        PlayerStatsEntry.B13 + " INTEGER, " +
                        PlayerStatsEntry.B14 + " INTEGER, " +
                        PlayerStatsEntry.B15 + " INTEGER" +*/
                        ");" ;

                db.execSQL(SQL_CREATE_PLAYERSTATS_TABLE);
                db.execSQL(SQL_CREATE_TEAMSTATS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlayerStatsEntry.PLAYERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PlayerStatsEntry.TEAMS_TABLE_NAME);
        onCreate(db);
    }
}