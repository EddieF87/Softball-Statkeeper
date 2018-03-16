package com.example.android.softballstatkeeper.models;

/**
 * Created by Eddie on 11/3/2017.
 */

public class TeamLog {

    private long id;
    private int wins;
    private int losses;
    private int ties;
    private int runsScored;
    private int runsAllowed;

    public TeamLog() {
    }

    public TeamLog(long id, int wins, int losses, int ties, int runsScored, int runsAllowed) {
        this.id = id;
        this.wins = wins;
        this.losses = losses;
        this.ties = ties;
        this.runsScored = runsScored;
        this.runsAllowed = runsAllowed;
    }

    public TeamLog(long id, int runsScored, int runsAllowed) {
        this.id = id;
        this.wins = 0;
        this.losses = 0;
        this.ties = 0;
        this.runsScored = runsScored;
        this.runsAllowed = runsAllowed;
    }

    public int getLosses() {
        return losses;
    }

    public int getWins() {
        return wins;
    }

    public int getRunsAllowed() {
        return runsAllowed;
    }

    public int getRunsScored() {
        return runsScored;
    }

    public int getTies() {
        return ties;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setTies(int ties) {
        this.ties = ties;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void setRunsAllowed(int runsAllowed) {
        this.runsAllowed = runsAllowed;
    }

    public void setRunsScored(int runsScored) {
        this.runsScored = runsScored;
    }
}
