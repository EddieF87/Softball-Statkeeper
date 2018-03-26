package xyz.sleekstats.softball.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Created by Eddie on 2/2/2018.
 */

public class ItemMarkedForDeletion implements Parcelable {

    private String firestoreID;
    private long type;
    private String name;
    private long gender;
    private String team;

    public ItemMarkedForDeletion(String id, long type, String name, long gender, String team) {
        this.firestoreID = id;
        this.type = type;
        this.gender = gender;
        this.name = name;
        this.team = team;
    }

    public String getFirestoreID() {        return firestoreID;}
    public long getType() {        return type;}
    public String getName() {        return name;}
    public long getGender() {        return gender;}
    public String getTeam() {        return team;}

    public void setFirestoreID(String firestoreID) {        this.firestoreID = firestoreID;}
    public void setType(long type) {        this.type = type;}
    public void setName(String name) {        this.name = name;}
    public void setGender(long gender) {        this.gender = gender;}
    public void setTeam(String team) {        this.team = team;}

    public static Comparator<ItemMarkedForDeletion> typeComparator () {
        return new Comparator<ItemMarkedForDeletion>() {
            @Override
            public int compare(ItemMarkedForDeletion itemMarkedForDeletion1, ItemMarkedForDeletion itemMarkedForDeletion2) {
                return (int) (itemMarkedForDeletion1.getType() - itemMarkedForDeletion2.getType());
            }
        };
    }

    public static Comparator<ItemMarkedForDeletion> nameComparator () {
        return new Comparator<ItemMarkedForDeletion>() {
            @Override
            public int compare(ItemMarkedForDeletion itemMarkedForDeletion1, ItemMarkedForDeletion itemMarkedForDeletion2) {
                return itemMarkedForDeletion1.getName().compareToIgnoreCase(itemMarkedForDeletion2.getName());
            }
        };
    }


    protected ItemMarkedForDeletion(Parcel in) {
        firestoreID = in.readString();
        type = in.readLong();
        name = in.readString();
        gender = in.readLong();
        team = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firestoreID);
        dest.writeLong(type);
        dest.writeString(name);
        dest.writeLong(gender);
        dest.writeString(team);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ItemMarkedForDeletion> CREATOR = new Parcelable.Creator<ItemMarkedForDeletion>() {
        @Override
        public ItemMarkedForDeletion createFromParcel(Parcel in) {
            return new ItemMarkedForDeletion(in);
        }

        @Override
        public ItemMarkedForDeletion[] newArray(int size) {
            return new ItemMarkedForDeletion[size];
        }
    };
}