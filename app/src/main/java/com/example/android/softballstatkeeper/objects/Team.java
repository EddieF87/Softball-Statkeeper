/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.softballstatkeeper.objects;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.softballstatkeeper.data.StatsContract.*;
import java.util.Comparator;

import static com.example.android.softballstatkeeper.data.StatsContract.getColumnInt;
import static com.example.android.softballstatkeeper.data.StatsContract.getColumnString;


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

    public Team(Cursor cursor) {
        this.name = getColumnString(cursor, StatsEntry.COLUMN_NAME);
        this.firestoreID = getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
        this.teamId = getColumnInt(cursor, StatsEntry._ID);
        this.totalRunsScored = getColumnInt(cursor, StatsEntry.COLUMN_RUNSFOR);
        this.totalRunsAllowed = getColumnInt(cursor, StatsEntry.COLUMN_RUNSAGAINST);
        this.wins = getColumnInt(cursor, StatsEntry.COLUMN_WINS);
        this.losses = getColumnInt(cursor, StatsEntry.COLUMN_LOSSES);
        this.ties = getColumnInt(cursor, StatsEntry.COLUMN_TIES);
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
    public int getRunDifferential() {return this.getTotalRunsScored() - this.getTotalRunsAllowed();}
    public double getWinPct() {
        return this.wins / ((double) this.wins + this.losses);
    }

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

    public static Comparator<Team> winComparator () {
        return new Comparator<Team>() {
            @Override
            public int compare(Team team1, Team team2) {
                return team2.getWins() - team1.getWins();
            }
        };
    }

    public static Comparator<Team> lossComparator () {
        return new Comparator<Team>() {
            @Override
            public int compare(Team team1, Team team2) {
                return team2.getLosses() - team1.getLosses();
            }
        };
    }

    public static Comparator<Team> tieComparator () {
        return new Comparator<Team>() {
            @Override
            public int compare(Team team1, Team team2) {
                return team2.getTies() - team1.getTies();
            }
        };
    }

    public static Comparator<Team> runsComparator () {
        return new Comparator<Team>() {
            @Override
            public int compare(Team team1, Team team2) {
                return team2.getTotalRunsScored() - team1.getTotalRunsScored();
            }
        };
    }

    public static Comparator<Team> runsAllowedComparator () {
        return new Comparator<Team>() {
            @Override
            public int compare(Team team1, Team team2) {
                return team2.getTotalRunsAllowed() - team1.getTotalRunsAllowed();
            }
        };
    }

    public static Comparator<Team> runDiffComparator () {
        return new Comparator<Team>() {
            @Override
            public int compare(Team team1, Team team2) {
                return team2.getRunDifferential() - team1.getRunDifferential();
            }
        };
    }

    public static Comparator<Team> winpctComparator () {
        return new Comparator<Team>() {
            @Override
            public int compare(Team team1, Team team2) {
                int team1Games = team1.getLosses() + team1.getWins();
                int team2Games = team2.getLosses() + team2.getWins();

                if(team1Games <= 0 && team2Games <= 0) {
                    return 0;
                } else if (team1Games <= 0) {
                    return 1;
                } else if (team2Games <= 0) {
                    return -1;
                }
                return (int) (1000 * (team2.getWinPct() - team1.getWinPct()));
            }
        };
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