package xyz.sleekstats.softball.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Created by Eddie on 11/9/2017.
 */

@SuppressWarnings("unused")
public class MainPageSelection implements Parcelable {

    public static final String KEY_SELECTION_ID = "selectionID";
    public static final String KEY_SELECTION_TYPE = "selectionType";
    public static final String KEY_SELECTION_NAME = "selectionName";
    public static final String KEY_SELECTION_LEVEL = "userLevel";

    public static final int TYPE_LEAGUE = 0;
    public static final int TYPE_TEAM = 1;
    public static final int TYPE_PLAYER = 2;

    private String id;
    private String name;
    private int type;
    private int level;

    public MainPageSelection() {
    }

    public MainPageSelection(String id, String name, int type, int level) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    @SuppressWarnings("unused")
    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public void setId(String id) {
        this.id = id;
    }

    @SuppressWarnings("unused")
    public void setType(int type) {
        this.type = type;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    protected MainPageSelection(Parcel in) {
        id = in.readString();
        name = in.readString();
        type = in.readInt();
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
        dest.writeInt(type);
        dest.writeInt(level);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MainPageSelection> CREATOR = new Parcelable.Creator<MainPageSelection>() {
        @Override
        public MainPageSelection createFromParcel(Parcel in) {
            return new MainPageSelection(in);
        }

        @Override
        public MainPageSelection[] newArray(int size) {
            return new MainPageSelection[size];
        }
    };

    public static Comparator<MainPageSelection> nameComparator () {
        return new Comparator<MainPageSelection>() {
            @Override
            public int compare(MainPageSelection mainPageSelection1, MainPageSelection mainPageSelection2) {
                return mainPageSelection1.getName().compareToIgnoreCase(mainPageSelection2.getName());
            }
        };
    }

    public static Comparator<MainPageSelection> typeComparator () {
        return new Comparator<MainPageSelection>() {
            @Override
            public int compare(MainPageSelection mainPageSelection1, MainPageSelection mainPageSelection2) {
                return mainPageSelection2.getType() - mainPageSelection1.getType();
            }
        };
    }

    @SuppressWarnings("unused")
    public static Comparator<MainPageSelection> levelComparator () {
        return new Comparator<MainPageSelection>() {
            @Override
            public int compare(MainPageSelection mainPageSelection1, MainPageSelection mainPageSelection2) {
                return mainPageSelection2.getLevel() - mainPageSelection1.getLevel();
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || getClass() != obj.getClass()) {
            return false;
        }

        MainPageSelection comparedSelection = (MainPageSelection) obj;
        return this.id.equals(comparedSelection.getId());
    }
}
