/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.sleekstats.softball.models;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import xyz.sleekstats.softball.data.StatsContract.*;

import java.util.Comparator;

import static xyz.sleekstats.softball.data.StatsContract.getColumnInt;
import static xyz.sleekstats.softball.data.StatsContract.getColumnString;

/**
 * @author Eddie
 */

@SuppressWarnings("unused")
public class Player implements Parcelable {

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
    private int stolenBases;
    private int strikeouts;
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
        this.stolenBases = getColumnInt(cursor, StatsEntry.COLUMN_SB);
        this.strikeouts = getColumnInt(cursor, StatsEntry.COLUMN_K);

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

    public int getStolenBases() { return stolenBases; }

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

    public void setStolenBases(int stolenBases) { this.stolenBases = stolenBases; }

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

    public int getStrikeouts() {
        return strikeouts;
    }

    public void setStrikeouts(int strikeouts) {
        this.strikeouts = strikeouts;
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

    public static Comparator<Player> kComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player2.getStrikeouts() - player1.getStrikeouts();
            }
        };
    }

    public static Comparator<Player> sbComparator () {
        return new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player2.getStolenBases() - player1.getStolenBases();
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

    protected Player(Parcel in) {
        name = in.readString();
        team = in.readString();
        singles = in.readInt();
        doubles = in.readInt();
        triples = in.readInt();
        hrs = in.readInt();
        walks = in.readInt();
        runs = in.readInt();
        rbis = in.readInt();
        outs = in.readInt();
        sacFlies = in.readInt();
        stolenBases = in.readInt();
        strikeouts = in.readInt();
        games = in.readInt();
        gender = in.readInt();
        playerId = in.readLong();
        firestoreID = in.readString();
        teamfirestoreid = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(team);
        dest.writeInt(singles);
        dest.writeInt(doubles);
        dest.writeInt(triples);
        dest.writeInt(hrs);
        dest.writeInt(walks);
        dest.writeInt(runs);
        dest.writeInt(rbis);
        dest.writeInt(outs);
        dest.writeInt(sacFlies);
        dest.writeInt(stolenBases);
        dest.writeInt(strikeouts);
        dest.writeInt(games);
        dest.writeInt(gender);
        dest.writeLong(playerId);
        dest.writeString(firestoreID);
        dest.writeString(teamfirestoreid);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };
}