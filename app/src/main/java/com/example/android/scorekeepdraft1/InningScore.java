package com.example.android.scorekeepdraft1;

/**
 * Created by Eddie on 15/10/2017.
 */

public class InningScore {
     private int top;
     private int bottom;

    public InningScore(int top, int bottom) {
        this.top = top;
        this.bottom = bottom;
    }

    public int getBottom() {
        return bottom;
    }

    public int getTop() {
        return top;
    }
}
