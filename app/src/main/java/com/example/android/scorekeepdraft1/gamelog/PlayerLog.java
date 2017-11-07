package com.example.android.scorekeepdraft1.gamelog;

/**
 * Created by Eddie on 11/3/2017.
 */

public class PlayerLog {

    private long id;
    private int rbi;
    private int runs;
    private int singles;
    private int doubles;
    private int triples;
    private int hrs;
    private int outs;
    private int walks;
    private int sacfly;

    public PlayerLog() {
    }

    public PlayerLog(long id, int rbi, int runs, int singles, int doubles, int triples, int hrs, int outs, int walks, int sacfly) {
        this.id = id;
        this.rbi = rbi;
        this.runs = runs;
        this.singles = singles;
        this.doubles = doubles;
        this.triples = triples;
        this.hrs = hrs;
        this.outs = outs;
        this.walks = walks;
        this.sacfly = sacfly;
    }

    public int getDoubles() {
        return doubles;
    }

    public int getHrs() {
        return hrs;
    }

    public long getId() {
        return id;
    }

    public int getOuts() {
        return outs;
    }

    public int getRbi() {
        return rbi;
    }

    public int getRuns() {
        return runs;
    }

    public int getSacfly() {
        return sacfly;
    }

    public int getSingles() {
        return singles;
    }

    public int getTriples() {
        return triples;
    }

    public int getWalks() {
        return walks;
    }

    public void setWalks(int walks) {
        this.walks = walks;
    }

    public void setTriples(int triples) {
        this.triples = triples;
    }

    public void setSingles(int singles) {
        this.singles = singles;
    }

    public void setRuns(int runs) {
        this.runs = runs;
    }

    public void setHrs(int hrs) {
        this.hrs = hrs;
    }

    public void setDoubles(int doubles) {
        this.doubles = doubles;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOuts(int outs) {
        this.outs = outs;
    }

    public void setRbi(int rbi) {
        this.rbi = rbi;
    }

    public void setSacfly(int sacfly) {
        this.sacfly = sacfly;
    }
}
