package com.example.android.scorekeepdraft1.objects;

/**
 * Created by Eddie on 11/9/2017.
 */

public class MainPageSelection {

    private String name;
    private String id;
    private int type;

    public MainPageSelection(String name, String id, int type) {
        this.name = name;
        this.id = id;
        this.type = type;
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
}
