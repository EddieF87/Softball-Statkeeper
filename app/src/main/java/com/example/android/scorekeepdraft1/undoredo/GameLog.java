package com.example.android.scorekeepdraft1.undoredo;

import java.util.ArrayList;

/**
 * Created by Eddie on 29/08/2017.
 */

public class GameLog {
    private BaseLog baseLogEnd;
    private ArrayList<String> runsLog;
    private String result;
    private String previousBatter;
    private int inningChanged;
    private int inningNumber;

    public GameLog(BaseLog baseLogEnd, ArrayList<String>  runsLog, String result, String previousBatter, int inning, int inningChanged) {
        this.baseLogEnd = baseLogEnd;
        this.runsLog = runsLog;
        this.result = result;
        this.previousBatter = previousBatter;
        this.inningChanged = inningChanged;
        this.inningNumber = inning;
    }

    public BaseLog getBaseLogEnd() {return baseLogEnd;}
    public ArrayList<String>  getRunsLog() {
        return runsLog;
    }
    public String getResult() {
        return result;
    }
    public String getPreviousBatter() {return previousBatter;}
    public boolean isInningChanged() {
        if (inningChanged == 1){
            return true;
        }
        return false;
    }
    public int getInningNumber() {return inningNumber;}
}