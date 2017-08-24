/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.scorekeepdraft1.undoredo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.android.scorekeepdraft1.Game;
import com.example.android.scorekeepdraft1.Player;

/**
 *
 * @author Eddie
 */
public class AtBat {

    private List<Play> plays;
    private Player[] initialBases = new Player[5];
    private Player[] finalBases = new Player[5];
    private int lineupIndex;

    public AtBat(int index, Player[] basesCopied) {
        this.lineupIndex = index;
        System.arraycopy(basesCopied, 1, this.initialBases, 1, 4);
        this.plays = new ArrayList<>();
    }

    public void addPlay(Play play) {
        plays.add(play);
    }

    public List<Play> getPlays() {
        return plays;
    }

    public void setFinalBases(Player[] basesCopied) {
        System.arraycopy(basesCopied, 1, this.finalBases, 1, 4);
    }

    public int getIndex() {
        return lineupIndex;
    }

    public Player[] getInitialBases() {
        return initialBases;
    }

    public Player[] getFinalBases() {
        return this.finalBases;
    }

    @Override
    public String toString() {
        return "test: initalbase = " + Arrays.toString(initialBases) + "   index = " + lineupIndex + "\n final bases = " + Arrays.toString(finalBases);
    }

    public void undoPlays(Game game) {
        for (Play play : plays) {
            play.undoPlay(game);
        }
        game.editOnBase(initialBases);
    }

    public void redoPlays(Game game) {
        for (Play play : plays) {
            play.redoPlay(game);
        }
        game.editOnBase(finalBases);
    }

}
