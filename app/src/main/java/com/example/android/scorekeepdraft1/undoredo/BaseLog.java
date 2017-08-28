package com.example.android.scorekeepdraft1.undoredo;

/**
 * Created by Eddie on 26/08/2017.
 */

public class BaseLog {
    private String[] basepositions;

    public BaseLog(String batter, String first, String second, String third) {
        this.basepositions =  new String[]{batter, first, second, third};
    }

    public String[] getBasepositions() {
        return basepositions;
    }
}