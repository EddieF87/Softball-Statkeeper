package com.example.android.scorekeepdraft1.undoredo;

import com.example.android.scorekeepdraft1.Player;
import com.example.android.scorekeepdraft1.Team;

import java.util.List;

/**
 * Created by Eddie on 26/08/2017.
 */

public class BaseLog {
    private List<String> team;
    private String batter;
    private String[] basepositions;
    private int outCount;
    private int awayTeamRuns;
    private int homeTeamRuns;

    public BaseLog(List<String> team, String batter, String first, String second, String third, int outs, int awayTeamRuns, int homeTeamRuns) {
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

    public String getBatter() {
        return batter;
    }

    public List<String> getTeam() {
        return team;
    }
}