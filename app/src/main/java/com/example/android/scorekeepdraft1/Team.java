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

    private List<Player> team = new ArrayList<>();
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
        team.add(player);
    }

    public Player getPlayer(int index) {
        return team.get(index);
    }

    public String getName() {
        return name;
    }

    public List<Player> getLineup() {
        return team;
    }

    @Override
    public String toString() {
        StringBuilder teamString = new StringBuilder(name + ": ");
        for (Player player : team) {
            teamString.append(player.getName());
            if (team.indexOf(player) == team.size() - 1) {
                break;
            }
            teamString.append(", ");
        }
        return teamString.toString();
    }

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
        if(index >= team.size()) {
            index = 0;
        }
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setIndex(Player player) {
        this.index = team.indexOf(player);
    }

    public int getIndex() {
        return index;
    }
}
