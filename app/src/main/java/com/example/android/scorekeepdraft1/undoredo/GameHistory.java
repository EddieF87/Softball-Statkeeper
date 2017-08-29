package com.example.android.scorekeepdraft1.undoredo;

import java.util.ArrayList;

/**
 * Created by Eddie on 29/08/2017.
 */

public class GameHistory {
    private ArrayList<GameLog> gameLogs;

    public GameHistory() {
        this.gameLogs = new ArrayList<>();
    }

    public ArrayList<GameLog> getAllGameLogs() {
        return gameLogs;
    }

    public GameLog getGameLog(int index) {
        return gameLogs.get(index);
    }

    public void addGameLog(GameLog gameLog) {
        gameLogs.add(gameLog);
    }
}
