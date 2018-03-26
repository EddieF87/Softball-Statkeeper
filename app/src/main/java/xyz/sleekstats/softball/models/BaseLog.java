package xyz.sleekstats.softball.models;

import android.database.Cursor;

import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;

import java.util.List;

/**
 * Created by Eddie on 26/08/2017.
 */

public class BaseLog {
    private final List<Player> team;
    private final Player batter;
    private final String[] basepositions;
    private final int outCount;
    private final int awayTeamRuns;
    private final int homeTeamRuns;

    public BaseLog(List<Player> team, Player batter, String first, String second, String third, int outs, int awayTeamRuns, int homeTeamRuns) {
        this.team = team;
        this.batter = batter;
        this.basepositions =  new String[]{first, second, third};
        this.outCount = outs;
        this.awayTeamRuns = awayTeamRuns;
        this.homeTeamRuns = homeTeamRuns;
    }

    public BaseLog(Cursor cursor, Player batter, List<Player> team) {
        this.outCount = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_OUT);
        this.awayTeamRuns = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_AWAY_RUNS);
        this.homeTeamRuns = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HOME_RUNS);
        String first = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_1B);
        String second = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_2B);
        String third = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_3B);
        this.basepositions =  new String[]{first, second, third};

        this.batter = batter;
        this.team = team;
    }

    public String[] getBasepositions() {
        return basepositions;
    }

    public int getOutCount() {
        return outCount;
    }

    public int getAwayTeamRuns() {
        return awayTeamRuns;
    }

    public int getHomeTeamRuns() {
        return homeTeamRuns;
    }

    public Player getBatter() {
        return batter;
    }

    public List<Player> getTeam() {
        return team;
    }
}