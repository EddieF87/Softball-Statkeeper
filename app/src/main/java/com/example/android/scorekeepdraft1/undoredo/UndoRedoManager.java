/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.scorekeepdraft1.undoredo;

import java.util.ArrayList;
import java.util.List;
import com.example.android.scorekeepdraft1.GameActivity;

/**
 *
 * @author Eddie
 */
public class UndoRedoManager {

    private List<Inning> innings = new ArrayList<>();
    private List<Inning> redoInningList = new ArrayList<>();
    private GameActivity game;

    public UndoRedoManager(GameActivity game) {
        this.game = game;
    }

    public void addInning(Inning inning) {
        innings.add(inning);
    }

    public void undoAtBats() {
        innings.get(game.getInningNumber()).undoAtBat(game);
    }

    public void redoAtBats() {
        try {
            innings.get(game.getInningNumber()).redoAtBat(game);
        } catch (Exception e) {
            game.decreaseInningNumber();
            System.out.println("You're all caught up!");
        }
    }

    public void previousInning() {
        if (game.getInningNumber() == 0) {
            System.out.println("This is the beginning of the game!");
            return;
        }
        game.decreaseInningNumber();
        game.setGameOuts(3);
        switchTeams();
        undoAtBats();
    }

    public void switchTeams() {
        int newInningNumber = game.getInningNumber();
        Inning inning = innings.get(newInningNumber);
        if (newInningNumber % 2 == 0) {
            inning.setTeam(game.getAwayTeam());
        } else {
            inning.setTeam(game.getHomeTeam());
        }
    }

    public void nextInning() {
        game.increaseInningNumber();
        game.setGameOuts(0);
        switchTeams();
        redoAtBats();
    }

}
