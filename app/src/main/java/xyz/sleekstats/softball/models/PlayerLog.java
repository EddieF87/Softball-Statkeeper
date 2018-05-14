package xyz.sleekstats.softball.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Eddie on 11/3/2017.
 */

@SuppressWarnings("unused")
public class PlayerLog implements Parcelable {

    private long id;
    private int rbi;
    private int runs;
    private int singles;
    private int doubles;
    private int triples;
    private int hrs;
    private int outs;
    private int walks;
    private int sacfly;
    private int stolenbases;

    public PlayerLog() {
    }

    public PlayerLog(long id, int rbi, int runs, int singles, int doubles, int triples, int hrs, int outs, int walks, int sacfly, int stolenbases) {
        this.id = id;
        this.rbi = rbi;
        this.runs = runs;
        this.singles = singles;
        this.doubles = doubles;
        this.triples = triples;
        this.hrs = hrs;
        this.outs = outs;
        this.walks = walks;
        this.sacfly = sacfly;
        this.stolenbases = stolenbases;
    }

    public int getDoubles() {
        return doubles;
    }

    public int getHrs() {
        return hrs;
    }

    public long getId() {
        return id;
    }

    public int getOuts() {
        return outs;
    }

    public int getRbi() {
        return rbi;
    }

    public int getRuns() {
        return runs;
    }

    public int getSacfly() {
        return sacfly;
    }

    public int getStolenbases() { return stolenbases; }

    public int getSingles() {
        return singles;
    }

    public int getTriples() {
        return triples;
    }

    public int getWalks() {
        return walks;
    }

    public void setWalks(int walks) {
        this.walks = walks;
    }

    public void setTriples(int triples) {
        this.triples = triples;
    }

    public void setSingles(int singles) {
        this.singles = singles;
    }

    public void setRuns(int runs) {
        this.runs = runs;
    }

    public void setHrs(int hrs) {
        this.hrs = hrs;
    }

    public void setDoubles(int doubles) {
        this.doubles = doubles;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOuts(int outs) {
        this.outs = outs;
    }

    public void setRbi(int rbi) {
        this.rbi = rbi;
    }

    public void setSacfly(int sacfly) {
        this.sacfly = sacfly;
    }

    public void setStolenbases(int stolenbases) { this.stolenbases = stolenbases; }

    public void addWalks(int walks) {
        this.walks += walks;
    }

    public void addTriples(int triples) {
        this.triples += triples;
    }

    public void addSingles(int singles) {
        this.singles += singles;
    }

    public void addRuns(int runs) {
        this.runs += runs;
    }

    public void addHrs(int hrs) {
        this.hrs += hrs;
    }

    public void addDoubles(int doubles) {
        this.doubles += doubles;
    }

    public void addId(int id) {
        this.id += id;
    }

    public void addOuts(int outs) {
        this.outs += outs;
    }

    public void addRbi(int rbi) {
        this.rbi += rbi;
    }

    public void addSacfly(int sacfly) {
        this.sacfly += sacfly;
    }

    public void addStolenbases(int stolenbases) {
        this.stolenbases += stolenbases;
    }

    protected PlayerLog(Parcel in) {
        id = in.readLong();
        rbi = in.readInt();
        runs = in.readInt();
        singles = in.readInt();
        doubles = in.readInt();
        triples = in.readInt();
        hrs = in.readInt();
        outs = in.readInt();
        walks = in.readInt();
        sacfly = in.readInt();
        stolenbases = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(rbi);
        dest.writeInt(runs);
        dest.writeInt(singles);
        dest.writeInt(doubles);
        dest.writeInt(triples);
        dest.writeInt(hrs);
        dest.writeInt(outs);
        dest.writeInt(walks);
        dest.writeInt(sacfly);
        dest.writeInt(stolenbases);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PlayerLog> CREATOR = new Parcelable.Creator<PlayerLog>() {
        @Override
        public PlayerLog createFromParcel(Parcel in) {
            return new PlayerLog(in);
        }

        @Override
        public PlayerLog[] newArray(int size) {
            return new PlayerLog[size];
        }
    };
}