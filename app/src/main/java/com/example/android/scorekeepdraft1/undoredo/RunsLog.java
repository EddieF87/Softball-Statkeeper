package com.example.android.scorekeepdraft1.undoredo;

import java.util.ArrayList;

/**
 * Created by Eddie on 29/08/2017.
 */

public class RunsLog {
    private ArrayList<String> playersScored;

    public RunsLog() {
        this.playersScored = new ArrayList<>();
    }

    public ArrayList<String> getPlayersScored() {
        return playersScored;
    }

    public void addPlayerScored(String player) {
        playersScored.add(player);
    }

    public void resetPlayersScored() {playersScored.clear();}

    public int getRBICount(){
        return playersScored.size();
    }
}