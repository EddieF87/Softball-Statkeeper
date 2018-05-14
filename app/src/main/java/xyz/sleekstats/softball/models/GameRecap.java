package xyz.sleekstats.softball.models;

import android.database.Cursor;

import xyz.sleekstats.softball.data.StatsContract.StatsEntry;

import static xyz.sleekstats.softball.data.StatsContract.getColumnInt;
import static xyz.sleekstats.softball.data.StatsContract.getColumnLong;
import static xyz.sleekstats.softball.data.StatsContract.getColumnString;

@SuppressWarnings("unused")
public class GameRecap {

    private long gameID;
    private String awayID;
    private String homeID;
    private int awayRuns;
    private int homeRuns;
    private int local;

    public GameRecap() {
    }

    public GameRecap(Cursor cursor) {
        this.gameID = getColumnLong(cursor, StatsEntry.COLUMN_GAME_ID);
        this.awayID = getColumnString(cursor, StatsEntry.COLUMN_AWAY_TEAM);
        this.homeID = getColumnString(cursor, StatsEntry.COLUMN_HOME_TEAM);
        this.awayRuns = getColumnInt(cursor, StatsEntry.COLUMN_AWAY_RUNS);
        this.homeRuns = getColumnInt(cursor, StatsEntry.COLUMN_HOME_RUNS);
        this.local = getColumnInt(cursor, StatsEntry.COLUMN_LOCAL);
    }

    public long getGameID() {
        return gameID;
    }

    public void setGameID(long gameID) {
        this.gameID = gameID;
    }

    public String getAwayID() {
        return awayID;
    }

    public void setAwayID(String awayID) {
        this.awayID = awayID;
    }

    public String getHomeID() {
        return homeID;
    }

    public void setHomeID(String homeID) {
        this.homeID = homeID;
    }

    public int getAwayRuns() {
        return awayRuns;
    }

    public void setAwayRuns(int awayRuns) {
        this.awayRuns = awayRuns;
    }

    public int getHomeRuns() {
        return homeRuns;
    }

    public void setHomeRuns(int homeRuns) {
        this.homeRuns = homeRuns;
    }

    public int getLocal() {
        return local;
    }

    public void setLocal(int local) {
        this.local = local;
    }
}
