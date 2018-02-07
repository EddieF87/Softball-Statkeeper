/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.scorekeepdraft1.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Eddie
 */
public class Team implements Parcelable {

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

    public Team(String name, String teamFirestoreID) {
        this.name = name;
        this.firestoreID = teamFirestoreID;
        this.teamId = 0;
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

    public static Comparator<Team> nameComparator () {
        return new Comparator<Team>() {
            @Override
            public int compare(Team team1, Team team2) {
                return team1.getName().compareToIgnoreCase(team2.getName());
            }
        };
    }

    protected Team(Parcel in) {
        teamId = in.readLong();
        name = in.readString();
        wins = in.readInt();
        losses = in.readInt();
        ties = in.readInt();
        totalRunsScored = in.readInt();
        totalRunsAllowed = in.readInt();
        firestoreID = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(teamId);
        dest.writeString(name);
        dest.writeInt(wins);
        dest.writeInt(losses);
        dest.writeInt(ties);
        dest.writeInt(totalRunsScored);
        dest.writeInt(totalRunsAllowed);
        dest.writeString(firestoreID);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Team> CREATOR = new Parcelable.Creator<Team>() {
        @Override
        public Team createFromParcel(Parcel in) {
            return new Team(in);
        }

        @Override
        public Team[] newArray(int size) {
            return new Team[size];
        }
    };
}