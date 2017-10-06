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
    private static final int DATABASE_VERSION = 3;


    public StatsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PLAYERSTATS_TABLE =
                "CREATE TABLE " + StatsEntry.PLAYERS_TABLE_NAME + " (" +
                        StatsEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        StatsContract.StatsEntry.COLUMN_TEAM + " TEXT DEFAULT 'FA', " +
                        StatsEntry.COLUMN_ORDER + " INTEGER, " +

                        StatsEntry.COLUMN_1B + " INTEGER, " +
                        StatsEntry.COLUMN_2B + " INTEGER, " +
                        StatsContract.StatsEntry.COLUMN_3B + " INTEGER, " +
                        StatsContract.StatsEntry.COLUMN_HR + " INTEGER, " +

                        StatsEntry.COLUMN_BB + " INTEGER, " +
                        StatsContract.StatsEntry.COLUMN_SF + " INTEGER, " +
                        StatsContract.StatsEntry.COLUMN_OUT + " INTEGER, " +

                        StatsContract.StatsEntry.COLUMN_RUN + " INTEGER, " +
                        StatsEntry.COLUMN_RBI + " INTEGER" +
                        ");" ;

        final String SQL_CREATE_TEAMSTATS_TABLE =
                "CREATE TABLE " + StatsEntry.TEAMS_TABLE_NAME + " (" +
                        StatsContract.StatsEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        StatsEntry.COLUMN_LEAGUE + " TEXT, " +

                        StatsEntry.COLUMN_WINS + " INTEGER, " +
                        StatsContract.StatsEntry.COLUMN_LOSSES + " INTEGER, " +
                        StatsEntry.COLUMN_TIES + " INTEGER, " +

                        StatsContract.StatsEntry.COLUMN_RUNSFOR + " INTEGER, " +
                        StatsContract.StatsEntry.COLUMN_RUNSAGAINST + " INTEGER" +

/*                   remember commas     StatsEntry.B1 + " INTEGER, " +
                        StatsEntry.B2 + " INTEGER, " +
                        StatsEntry.B3 + " INTEGER, " +
                        StatsEntry.B4 + " INTEGER, " +
                        StatsEntry.B5 + " INTEGER, " +
                        StatsEntry.B6 + " INTEGER, " +
                        StatsEntry.B7 + " INTEGER, " +
                        StatsEntry.B8 + " INTEGER, " +
                        StatsEntry.B9 + " INTEGER, " +
                        StatsEntry.B10 + " INTEGER, " +
                        StatsEntry.B11 + " INTEGER, " +
                        StatsEntry.B12 + " INTEGER, " +
                        StatsEntry.B13 + " INTEGER, " +
                        StatsEntry.B14 + " INTEGER, " +
                        StatsEntry.B15 + " INTEGER" +*/
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
