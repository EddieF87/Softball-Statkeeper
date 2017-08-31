package com.example.android.scorekeepdraft1.undoredo;

/**
 * Created by Eddie on 29/08/2017.
 */

public class GameLog {
    private BaseLog baseLogEnd;
    private RunsLog runsLog;
    private String result;
    private String previousBatter;
    private boolean inningChanged;

    public GameLog(BaseLog baseLogEnd, RunsLog runsLog, String result, String previousBatter, boolean inningChanged) {
        this.baseLogEnd = baseLogEnd;
        this.runsLog = runsLog;
        this.result = result;
        this.previousBatter = previousBatter;
        this.inningChanged = inningChanged;
    }

    public BaseLog getBaseLogEnd() {return baseLogEnd;}
    public RunsLog getRunsLog() {
        return runsLog;
    }
    public String getResult() {
        return result;
    }
    public String getPreviousBatter() {return previousBatter;}
    public boolean isInningChanged() {return inningChanged;    }
}