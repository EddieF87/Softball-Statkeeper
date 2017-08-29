package com.example.android.scorekeepdraft1.undoredo;

import com.example.android.scorekeepdraft1.Player;
import com.example.android.scorekeepdraft1.Team;

/**
 * Created by Eddie on 26/08/2017.
 */

public class BaseLog {
    private Team team;
    private Player batter;
    private String[] basepositions;
    private int outCount;
    private int awayTeamRuns;
    private int homeTeamRuns;

    public BaseLog(Team team, Player batter, String first, String second, String third, int outs, int awayTeamRuns, int homeTeamRuns) {
        this.team = team;
        this.batter = batter;
        this.basepositions =  new String[]{first, second, third};
        this.outCount = outs;
        this.awayTeamRuns = awayTeamRuns;
        this.homeTeamRuns = homeTeamRuns;
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

    public Team getTeam() {
        return team;
    }
}