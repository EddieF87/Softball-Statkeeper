/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.scorekeepdraft1.undoredo;

import java.util.ArrayList;
import java.util.List;

import com.example.android.scorekeepdraft1.GameActivity;
import com.example.android.scorekeepdraft1.Team;


/**
 *
 * @author Eddie
 */
public class Inning {

    private List<AtBat> atBats = new ArrayList<>();
    private List<AtBat> redoList = new ArrayList<>();
    private Team team;
    private UndoRedoManager manager;

    public Inning(Team team, UndoRedoManager manager) {
        this.team = team;
        this.manager = manager;
    }

    public List<AtBat> getAtBats() {
        return atBats;
    }

    public List<AtBat> getRedoList() {
        return redoList;
    }

    public void addAtBat(AtBat ab) {
        atBats.add(ab);
    }

    public void undoAtBat(GameActivity game) {
        if (atBats.isEmpty()) {
            manager.previousInning();
            return;
        }
        AtBat ab = this.atBats.get(atBats.size() - 1);
        this.redoList.add(ab);
        this.atBats.remove(ab);
        ab.undoPlays(game);
        team.decreaseIndex();
        if (team.getIndex() < 0) {
            team.setIndex(team.getLineup().size() - 1);
        }
        undoOrRedo(game);
    }

    public void redoAtBat(GameActivity game) {
        if (redoList.isEmpty()) {
            if (game.getGameOuts() == 3) {
                manager.nextInning();
                return;
            } else {
                System.out.println("great googly moogly!!");
                return;
            }
        }
        AtBat ab = this.redoList.get(0);
        this.atBats.add(ab);
        this.redoList.remove(ab);
        ab.redoPlays(game);
        team.increaseIndex();
        if (team.getIndex() >= team.getLineup().size()) {
            team.setIndex(0);
        }
        undoOrRedo(game);
    }

    public void undoOrRedo(GameActivity game) {
        System.out.println("To undo an AB, type 'u'\nTo redo an AB, type 'r'"
                + "\nTo continue, type any other key");

        /*String command = GameActivity.reader.nextLine();

        if (command.equals("u")) {
            undoAtBat(game);
        }
        if (command.equals("r")) {
            redoAtBat(game);
        }
        redoList.clear();*/
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
