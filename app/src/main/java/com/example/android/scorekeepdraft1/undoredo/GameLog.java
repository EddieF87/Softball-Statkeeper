package com.example.android.scorekeepdraft1.undoredo;

import java.util.ArrayList;

/**
 * Created by Eddie on 29/08/2017.
 */

public class GameLog {
    private BaseLog baseLog;
    private RunsLog runsLog;
    private String result;

    public GameLog(BaseLog baseLog, RunsLog runsLog, String result) {
        this.baseLog = baseLog;
        this.runsLog = runsLog;
        this.result = result;
    }

    public BaseLog getBaseLog() {
        return baseLog;
    }

    public RunsLog getRunsLog() {
        return runsLog;
    }

    public String getResult() {
        return result;
    }
}
