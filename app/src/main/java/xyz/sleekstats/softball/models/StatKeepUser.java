package xyz.sleekstats.softball.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Created by Eddie on 12/3/2017.
 */

@SuppressWarnings("unused")
public class StatKeepUser implements Parcelable {

    private String id;
    private String name;
    private String email;
    private int level;

    public StatKeepUser() {
    }

    public StatKeepUser(String id, String name, String email, int level) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj == null || getClass() != obj.getClass()) {
            return false;
        }

        StatKeepUser statKeepUser = (StatKeepUser) obj;
        return this.id.equals(statKeepUser.getId());
    }

    public static Comparator<StatKeepUser> levelComparator () {
        return new Comparator<StatKeepUser>() {
            @Override
            public int compare(StatKeepUser statKeepUser1, StatKeepUser statKeepUser2) {
                return statKeepUser2.getLevel() - statKeepUser1.getLevel();
            }
        };
    }

    protected StatKeepUser(Parcel in) {
        id = in.readString();
        name = in.readString();
        email = in.readString();
        level = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeInt(level);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<StatKeepUser> CREATOR = new Parcelable.Creator<StatKeepUser>() {
        @Override
        public StatKeepUser createFromParcel(Parcel in) {
            return new StatKeepUser(in);
        }

        @Override
        public StatKeepUser[] newArray(int size) {
            return new StatKeepUser[size];
        }
    };
}
