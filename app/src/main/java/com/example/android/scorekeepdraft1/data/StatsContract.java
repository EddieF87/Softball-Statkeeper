package com.example.android.scorekeepdraft1.data;

/**
 * Created by Eddie on 16/08/2017.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the stats database. This class is not necessary, but keeps
 * the code organized.
 */

public class StatsContract {

    /*
 * The "Content authority" is a name for the entire content provider, similar to the
 * relationship between a domain name and its website. A convenient string to use for the
 * content authority is the package name for the app, which is guaranteed to be unique on the
 * Play Store.
 */
    public static final String CONTENT_AUTHORITY = "com.example.android.scorekeepdraft1";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PLAYERS = "players";
    public static final String PATH_TEAMS = "teams";
    public static final String PATH_TEMP = "temp";
    public static final String PATH_GAME = "game";

    public StatsContract() {
    }

    /* Inner class that defines the table contents of the weather table */
    public static final class StatsEntry implements BaseColumns {

        /* The base CONTENT_URI1 used to query the Weather table from the content provider */
        public static final Uri CONTENT_URI1 = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PLAYERS);
        public static final Uri CONTENT_URI_TEAMS = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TEAMS);
        public static final Uri CONTENT_URI_TEMP = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TEMP);
        public static final Uri CONTENT_URI_GAMELOG = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GAME);


        /* Used internally as the name of our weather table. */
        public static final String PLAYERS_TABLE_NAME = "players";
        public static final String TEAMS_TABLE_NAME = "teams";
        public static final String TEMPPLAYERS_TABLE_NAME = "temp";
        public static final String GAME_TABLE_NAME = "game";

        /*
         * The date column will store the UTC date that correlates to the local date for which
         * each particular weather row represents. For example, if you live in the Eastern
         * Standard Time (EST) time zone and you load weather data at 9:00 PM on September 23, 2016,
         * the UTC time stamp for that particular time would be 1474678800000 in milliseconds.
         * However, due to time zone offsets, it would already be September 24th, 2016 in the GMT
         * time zone when it is 9:00 PM on the 23rd in the EST time zone. In this example, the date
         * column would hold the date representing September 23rd at midnight in GMT time.
         * (1474588800000)
         *
         * The reason we store GMT time and not local time is because it is best practice to have a
         * "normalized", or standard when storing the date and adjust as necessary when
         * displaying the date. Normalizing the date also allows us an easy way to convert to
         * local time at midnight, as all we have to do is add a particular time zone's GMT
         * offset to this date to get local time at midnight on the appropriate date.
         */
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
    }

}
