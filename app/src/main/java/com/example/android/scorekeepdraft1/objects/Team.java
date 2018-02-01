/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.scorekeepdraft1.objects;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eddie
 */
public class Team {

    private long teamId;
    private String name;
    private int wins;
    private int losses;
    private int ties;
    private int totalRunsScored;
    private int totalRunsAllowed;
    private String firestoreID;

    public Team() {
    }

    public Team(String name, long teamId) {
        this.name = name;
        this.teamId = teamId;
        this.totalRunsScored = 0;
        this.totalRunsAllowed = 0;
        this.wins = 0;
        this.losses = 0;
        this.ties = 0;
    }

    public String getName() {
        return name;
    }
    public int getTotalRunsScored() {return totalRunsScored;}
    public int getTotalRunsAllowed() {return totalRunsAllowed;}
    public int getWins() {return wins;}
    public int getLosses() {return losses;}
    public int getTies() {return ties;}
    public long getTeamId() {return teamId;}
    public String getFirestoreID() {return firestoreID;}

    public void setName(String name) {this.name = name;}
    public void setTeamId(long teamId) {this.teamId = teamId;}
    public void setLosses(int losses) {this.losses = losses;}
    public void setTies(int ties) {this.ties = ties;}
    public void setTotalRunsAllowed(int totalRunsAllowed) {this.totalRunsAllowed = totalRunsAllowed;}
    public void setTotalRunsScored(int totalRunsScored) {this.totalRunsScored = totalRunsScored;}
    public void setWins(int wins) {this.wins = wins;}
    public void setFirestoreID(String firestoreID) {this.firestoreID = firestoreID;}

    @Override
    public boolean equals(Object obj) {
        if(obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Team comparedTeam = (Team) obj;
        return this.firestoreID.equals(comparedTeam.getFirestoreID());
    }
}

