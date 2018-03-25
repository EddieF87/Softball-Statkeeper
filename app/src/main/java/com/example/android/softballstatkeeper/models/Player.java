/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.softballstatkeeper.models;

import android.database.Cursor;

import com.example.android.softballstatkeeper.data.StatsContract.*;

import java.util.Comparator;

import static com.example.android.softballstatkeeper.data.StatsContract.getColumnInt;
import static com.example.android.softballstatkeeper.data.StatsContract.getColumnString;

/**
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
    private int gender;
    private long playerId;
    private String firestoreID;
    private String teamfirestoreid;

    public Player() {
    }

    public Player(int gender, String firestoreID) {
        this.name = null;
        this.gender = gender;
        this.firestoreID = firestoreID;
    }

    public Player(String firestoreID, String name, String teamfirestoreid, int gender) {
        this.name = name;
        this.gender = gender;
        this.firestoreID = firestoreID;
        this.teamfirestoreid = teamfirestoreid;
    }

    public Player(Cursor cursor, boolean tempData) {
        this.name = getColumnString(cursor, StatsEntry.COLUMN_NAME);
        this.team = getColumnString(cursor, StatsEntry.COLUMN_TEAM);
        this.gender = getColumnInt(cursor, StatsEntry.COLUMN_GENDER);

        this.firestoreID = getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
        this.teamfirestoreid = getColumnString(cursor, StatsEntry.COLUMN_TEAM_FIRESTORE_ID);

        this.singles = getColumnInt(cursor, StatsEntry.COLUMN_1B);
        this.doubles = getColumnInt(cursor, StatsEntry.COLUMN_2B);
        this.triples = getColumnInt(cursor, StatsEntry.COLUMN_3B);
        this.hrs = getColumnInt(cursor, StatsEntry.COLUMN_HR);
        this.walks = getColumnInt(cursor, StatsEntry.COLUMN_BB);
        this.runs = getColumnInt(cursor, StatsEntry.COLUMN_RUN);
        this.rbis = getColumnInt(cursor, StatsEntry.COLUMN_RBI);
        this.outs = getColumnInt(cursor, StatsEntry.COLUMN_OUT);
        this.sacFlies = getColumnInt(cursor, StatsEntry.COLUMN_SF);

        if(tempData) {
            this.playerId = getColumnInt(cursor, StatsEntry.COLUMN_PLAYERID);
            this.games = 0;
        } else {
            this.playerId = getColumnInt(cursor, StatsEntry._ID);
            this.games = getColumnInt(cursor, StatsEntry.COLUMN_G);
        }
    }

    public double getAVG() {
        if (getABs() == 0) {
            return .000;
        }
        return ((double) getHits()) / getABs();
    }

    public double getOBP() {
        if (getABs() + this.walks + this.sacFlies == 0) {
            return .000;
        }
        return ((double) (getHits() + this.walks))
                / (getABs() + this.walks + this.sacFlies);
    }

    public double getSLG() {
        if (getABs() == 0) {
            return .000;
        }
        return (this.singles + this.doubles * 2 + this.triples * 3 + this.hrs * 4)
                / ((double) getABs());
    }

    public double getOPS() {
        return getOBP() + getSLG();
    }

    public int getGames() {
        return games;
    }

    public int getHits() {
        return this.singles + this.doubles + this.triples + this.hrs;
    }

    public int getABs() {
        return getHits() + this.outs;
    }

    private int getPAs() {
        return this.getABs() + this.walks + this.sacFlies;
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

    public String getTeamfirestoreid() {return teamfirestoreid;}

    public long getPlayerId() {
        return playerId;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public void setGames(int games) {
        this.games = games;
    }

    public void setOuts(int outs) {
        this.outs = outs;
    }

    public void setSacFlies(int sacFlies) {
        this.sacFlies = sacFlies;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public void setRbis(int rbis) {
        this.rbis = rbis;
    }

    public void setRuns(int runs) {
        this.runs = runs;
    }

    public void setSingles(int singles) {
        this.singles = singles;
    }

    public void setDoubles(int doubles) {
        this.doubles = doubles;
    }

    public void setTriples(int triples) {
        this.triples = triples;
    }

    public void setHrs(int hrs) {
        this.hrs = hrs;
    }

    public void setWalks(int walks) {
        this.walks = walks;
    }

    public void setTeamfirestoreid(String teamfirestoreid) {this.teamfirestoreid = teamfirestoreid;}

    @Override
    public String toString() {
        return this.name;
    }

    public String getFirestoreID() {
        return firestoreID;
    }

    public void setFirestoreID(String firestoreID) {
        this.firestoreID = firestoreID;
    }

    public static Comparator<Player> nameComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player1.getName().compareToIgnoreCase(player2.getName());
            }
        };
    }

    public static Comparator<Player> teamComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player1.getTeam().compareToIgnoreCase(player2.getTeam());
            }
        };
    }

    public static Comparator<Player> runComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player2.getRuns() - player1.getRuns();
            }
        };
    }

    public static Comparator<Player> rbiComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player2.getRbis() - player1.getRbis();
            }
        };
    }

    public static Comparator<Player> singleComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player2.getSingles() - player1.getSingles();
            }
        };
    }

    public static Comparator<Player> doubleComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player2.getDoubles() - player1.getDoubles();
            }
        };
    }

    public static Comparator<Player> tripleComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player2.getTriples() - player1.getTriples();
            }
        };
    }

    public static Comparator<Player> hrComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player2.getHrs() - player1.getHrs();
            }
        };
    }

    public static Comparator<Player> hitComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player2.getHits() - player1.getHits();
            }
        };
    }

    public static Comparator<Player> walkComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player2.getWalks() - player1.getWalks();
            }
        };
    }

    public static Comparator<Player> atbatComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player2.getABs() - player1.getABs();
            }
        };
    }

    public static Comparator<Player> gamesplayedComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player2.getGames() - player1.getGames();
            }
        };
    }

    public static Comparator<Player> avgComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                if(player1.getABs() == 0 && player2.getABs() == 0) {
                    return 0;
                } else if (player1.getABs() == 0) {
                    return 1;
                } else if (player2.getABs() == 0) {
                    return -1;
                }
                return (int) (1000 * (player2.getAVG() - player1.getAVG()));
            }
        };
    }

    public static Comparator<Player> obpComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                if(player1.getPAs() <= 0 && player2.getPAs() <= 0) {
                    return 0;
                } else if (player1.getPAs() <= 0) {
                    return 1;
                } else if (player2.getPAs() <= 0) {
                    return -1;
                }

                return (int) (1000 * (player2.getOBP() - player1.getOBP()));
            }
        };
    }

    public static Comparator<Player> slgComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                if(player1.getABs() == 0 && player2.getABs() == 0) {
                    return 0;
                } else if (player1.getABs() == 0) {
                    return 1;
                } else if (player2.getABs() == 0) {
                    return -1;
                }

                return (int) (1000 * (player2.getSLG() - player1.getSLG()));
            }
        };
    }

    public static Comparator<Player> opsComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                if(player1.getPAs() <= 0 && player2.getPAs() <= 0) {
                    return 0;
                } else if (player1.getPAs() <= 0) {
                    return 1;
                } else if (player2.getPAs() <= 0) {
                    return -1;
                }

                return (int) (1000* (player2.getOPS() - player1.getOPS()));
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Player comparedPlayer = (Player) obj;
        return this.firestoreID.equals(comparedPlayer.getFirestoreID());
    }
}