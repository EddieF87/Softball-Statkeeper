/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.scorekeepdraft1.undoredo;

import com.example.android.scorekeepdraft1.Game;
import com.example.android.scorekeepdraft1.Player;
import com.example.android.scorekeepdraft1.Team;

/**
 *
 * @author Eddie
 */
public class Play {

    private Player player;
    private Team team;
    private String action;

    public Play(Player player, Team team, String action) {
        this.player = player;
        this.team = team;
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public Player getPlayer() {
        return player;
    }

    public Team getTeam() {
        return team;
    }

    public void undoPlay(Game game) {

        switch (action) {
            case "s":
                player.subtractSingle();
                break;

            case "d":
                player.subtractDouble();
                break;

            case "t":
                player.subtractTriple();
                break;

            case "hr":
                player.subtractHR();
                break;

            case "o":
                player.subtractOut();
                game.subtractOut();
                break;

            case "e":
                player.subtractOut();
                break;

            case "fc":
                player.subtractOut();
                break;

            case "sf":
                player.subtractSacFly();
                game.subtractOut();
                break;

            case "run":
                player.subtractRun();
                team.subtractRun();
                break;

            case "rbi":
                player.subtractRBI();
                break;

            default:
                System.out.print("Something is wrong. Invalid entry. ");
                break;
        }
    }

    public void redoPlay(Game game) {

        switch (action) {
            case "s":
                player.addSingle();
                break;

            case "d":
                player.addDouble();
                break;

            case "t":
                player.addTriple();
                break;

            case "hr":
                player.addHR();
                break;

            case "o":
                player.addOut();
                game.addOut();
                break;

            case "e":
                player.addOut();
                break;

            case "fc":
                player.addOut();
                break;

            case "sf":
                player.addSacFly();
                game.addOut();
                break;

            case "run":
                player.addRun();
                team.addRun();

            case "rbi":
                player.addRBI();

            default:
                System.out.print("Something is wrong. Invalid entry. ");
                break;
        }
    }

}
