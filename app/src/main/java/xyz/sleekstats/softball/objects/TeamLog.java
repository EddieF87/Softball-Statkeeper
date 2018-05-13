package xyz.sleekstats.softball.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Eddie on 11/3/2017.
 */

@SuppressWarnings("unused")
public class TeamLog implements Parcelable {

    private long id;
    private int wins;
    private int losses;
    private int ties;
    private int runsScored;
    private int runsAllowed;

    public TeamLog() {
    }

    public TeamLog(long id, int wins, int losses, int ties, int runsScored, int runsAllowed) {
        this.id = id;
        this.wins = wins;
        this.losses = losses;
        this.ties = ties;
        this.runsScored = runsScored;
        this.runsAllowed = runsAllowed;
    }

    public TeamLog(long id, int runsScored, int runsAllowed) {
        this.id = id;
        this.wins = 0;
        this.losses = 0;
        this.ties = 0;
        this.runsScored = runsScored;
        this.runsAllowed = runsAllowed;
    }

    public int getLosses() {
        return losses;
    }

    public int getWins() {
        return wins;
    }

    public int getRunsAllowed() {
        return runsAllowed;
    }

    public int getRunsScored() {
        return runsScored;
    }

    public int getTies() {
        return ties;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setTies(int ties) {
        this.ties = ties;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void setRunsAllowed(int runsAllowed) {
        this.runsAllowed = runsAllowed;
    }

    public void setRunsScored(int runsScored) {
        this.runsScored = runsScored;
    }

    protected TeamLog(Parcel in) {
        id = in.readLong();
        wins = in.readInt();
        losses = in.readInt();
        ties = in.readInt();
        runsScored = in.readInt();
        runsAllowed = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(wins);
        dest.writeInt(losses);
        dest.writeInt(ties);
        dest.writeInt(runsScored);
        dest.writeInt(runsAllowed);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TeamLog> CREATOR = new Parcelable.Creator<TeamLog>() {
        @Override
        public TeamLog createFromParcel(Parcel in) {
            return new TeamLog(in);
        }

        @Override
        public TeamLog[] newArray(int size) {
            return new TeamLog[size];
        }
    };
}