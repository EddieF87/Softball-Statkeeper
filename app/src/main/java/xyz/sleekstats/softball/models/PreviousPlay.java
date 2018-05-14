package xyz.sleekstats.softball.models;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;

@SuppressWarnings("unused")
public class PreviousPlay {

    private String play;
    private String batter;
    private boolean homeTeam;
    private List<String> runsList;
    private int awayRuns;
    private int homeRuns;
    private int inning;
    private int outs;
    private String first;
    private String second;
    private String third;

//    public PreviousPlay(String play, String batter, boolean homeTeam, List<String> runsList, int awayRuns, int homeRuns, int inning) {
//        this.play = play;
//        this.batter = batter;
//        this.homeTeam = homeTeam;
//        this.runsList = runsList;
//        this.awayRuns = awayRuns;
//        this.homeRuns = homeRuns;
//        this.inning = inning;
//    }


    public PreviousPlay(Cursor cursor) {
        this.play = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_PLAY);
        this.batter = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_BATTER);
        this.homeTeam = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_TEAM) == 1;
        this.runsList = new ArrayList<>();

        String runner = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_RUN1);
        if (runner != null) {
            this.runsList.add(runner);
            runner = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_RUN2);
            if (runner != null) {
                this.runsList.add(runner);
                runner = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_RUN3);
                if (runner != null) {
                    this.runsList.add(runner);
                    runner = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_RUN4);
                    if (runner != null) {
                        this.runsList.add(runner);
                    }
                }
            }
        }
        this.awayRuns = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_AWAY_RUNS);
        this.homeRuns = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HOME_RUNS);
        if (StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_INNING_CHANGED) == 1) {
            this.inning = StatsContract.getColumnInt(cursor, StatsEntry.INNINGS);
        }
        this.outs = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_OUT);
        this.first = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_1B);
        this.second = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_2B);
        this.third = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_3B);
    }

    public String getPlay() {
        return play;
    }

    public void setPlay(String play) {
        this.play = play;
    }

    public String getBatter() {
        return batter;
    }

    public void setBatter(String batter) {
        this.batter = batter;
    }

    public boolean isHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(boolean homeTeam) {
        this.homeTeam = homeTeam;
    }

    public List<String> getRunsList() {
        return runsList;
    }

    public void setRunsList(List<String> runsList) {
        this.runsList = runsList;
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

    public int getInning() {
        return inning;
    }

    public void setInning(int inning) {
        this.inning = inning;
    }

    public int getOuts() {
        return outs;
    }

    public void setOuts(int outs) {
        this.outs = outs;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getThird() {
        return third;
    }

    public void setThird(String third) {
        this.third = third;
    }
}
