/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.scorekeepdraft1;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Eddie
 */
public class Player {

    private String name;
    private String team;
    private int singles;
    private int doubles;
    private int triples;
    private int hrs;
    private int walks;
    private int runs;
    private int rbis;
    private int outs;
    private int sacFlies;
    private int games;
    private int teamId;
    private int playerId;
    private final NumberFormat formatter = new DecimalFormat("#.000");

    public Player(String name) {
        this.name = name;
        this.singles = 0;
        this.doubles = 0;
        this.triples = 0;
        this.hrs = 0;
        this.walks = 0;
        this.runs = 0;
        this.rbis = 0;
        this.outs = 0;
        this.sacFlies = 0;
        this.games = 0;
        this.team = "Free Agent";
    }

    public Player(String name, String team, int singles, int doubles, int triples, int hrs, int walks, int runs, int rbis, int outs, int sacFlies, int games, int playerId) {
        this.name = name;
        this.team = team;
        this.singles = singles;
        this.doubles = doubles;
        this.triples = triples;
        this.hrs = hrs;
        this.walks = walks;
        this.runs = runs;
        this.rbis = rbis;
        this.outs = outs;
        this.sacFlies = sacFlies;
        this.games = games;
        this.playerId = playerId;
    }

    public Player(String name, String team, int singles, int doubles, int triples, int hrs, int walks, int runs, int rbis, int outs, int sacFlies, int games, int teamId, int playerId) {
        this.name = name;
        this.team = team;
        this.singles = singles;
        this.doubles = doubles;
        this.triples = triples;
        this.hrs = hrs;
        this.walks = walks;
        this.runs = runs;
        this.rbis = rbis;
        this.outs = outs;
        this.sacFlies = sacFlies;
        this.games = games;
        this.teamId = teamId;
        this.playerId = playerId;
    }

        public double getAVG() {
        if (getABs() == 0) {return .000;}
        return ((double) getHits()) / getABs();
    }

    public double getOBP() {
        if (getABs() + getWalks() == 0) {return .000;}
        return ((double) (getHits() + this.walks))
                / (getABs() + this.walks + this.sacFlies);
    }

    public double getSLG() {
        if (getABs() == 0) {return .000;}
        return (this.singles + this.doubles * 2 + this.triples * 3 + this.hrs * 4)
                / ((double) getABs());
    }

    public double getOPS() {
        return getOBP() + getSLG();
    }

    public int getGames() {return games;}
    public int getHits() {
        return this.singles + this.doubles + this.triples + this.hrs;
    }
    public int getABs() {
        return getHits() + this.outs;
    }
    public String getName() {
        return name;
    }
    public String getTeam() {
        return team;
    }
    public int getSingles() {
        return singles;
    }
    public int getDoubles() {
        return doubles;
    }
    public int getTriples() {
        return triples;
    }
    public int getHrs() {
        return hrs;
    }
    public int getWalks() {
        return walks;
    }
    public int getRbis() {
        return rbis;
    }
    public int getRuns() {
        return runs;
    }
    public int getOuts() {
        return outs;
    }
    public int getSacFlies() {
        return sacFlies;
    }
    public int getTeamId() {return teamId;}

    public void setName(String name) {
        this.name = name;
    }
    public void setTeam(String team) {
        this.team = team;
    }
    public void setOuts(int outs) {
        this.outs = outs;
    }
    public void setSacFlies(int sacFlies) {
        this.sacFlies = sacFlies;
    }
    public void setGames(int games) {this.games = games;}
    public int getPlayerId() {return playerId;}

    @Override
    public String toString() {
        return this.name;
    }
}