package com.example.android.scorekeepdraft1.objects;

/**
 * Created by Eddie on 11/9/2017.
 */

public class MainPageSelection {

    private String id;
    private String name;
    private String type;

    public MainPageSelection() {
    }

    public MainPageSelection(String id, String name, String type) {
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

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }
}
