package com.example.android.scorekeepdraft1.objects;

/**
 * Created by Eddie on 12/3/2017.
 */

public class StatKeepUser {

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
}
