package xyz.sleekstats.softball.models;

/**
 * Created by Eddie on 15/10/2017.
 */

public class InningScore {

    private final int top;
    private final int bottom;

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
