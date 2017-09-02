/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.scorekeepdraft1;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eddie
 */
public class Team {

    private List<Player> roster = new ArrayList<>();
    private List<Player> lineup = new ArrayList<>();
    private List<Player> bench = new ArrayList<>();
    private String name;
    private int totalRunsScored = 0;
    private int totalRunsAgainst = 0;
    private int currentRuns = 0;
    private int index = 0;

    public Team(String name) {
        this.name = name;
    }

    public void addPlayer(Player player) {
        player.setTeam(getName());
        roster.add(player);
    }

    public Player getPlayer(int index) {
        return roster.get(index);
    }
    public String getName() {
        return name;
    }

    public List<Player> getRoster() {
        return roster;
    }
    public List<Player> getBench() {
        return bench;
    }
    public List<Player> getLineup() {return lineup;}

    public void putOnBench(Player player) {
        if(lineup.contains(player)) {lineup.remove(player);}
        if(bench.contains(player)) {return;}
        bench.add(player);
    }

    public void putInLineup(Player player) {
        if(bench.contains(player)) {bench.remove(player);}
        if(lineup.contains(player)) {return;}
        lineup.add(player);
    }

    public int getRosterSize(){return roster.size();}
    public int getLineupSize(){return lineup.size();}
    public int getBenchSize(){return bench.size();}

    public void addRun() {
        currentRuns++;
    }

    public void subtractRun() {
        currentRuns--;
    }

    public int getCurrentRuns() {
        return currentRuns;
    }

    public void setCurrentRuns(int currentRuns) {
        this.currentRuns = currentRuns;
    }

    public void decreaseIndex() {
        this.index--;
    }

    public void increaseIndex() {
        this.index++;
        if(index >= roster.size()) {
            index = 0;
        }
    }

    public boolean isOnRoster(String playerName) {
        for (Player player : roster) {
            if (player.getName().equals(playerName)) {
                return true;
            }
        }
        return false;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setIndex(Player player) {
        this.index = roster.indexOf(player);
    }

    public int getIndex() {
        return index;
    }
}
