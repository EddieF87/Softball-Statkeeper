/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.sleekstats.softball.activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.MatchupAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.dialogs.EditWarningDialog;
import xyz.sleekstats.softball.objects.BaseLog;

import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.objects.MainPageSelection;
import xyz.sleekstats.softball.objects.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eddie
 */
public class LeagueGameActivity extends GameActivity {

    private List<Player> awayTeam;
    private List<Player> homeTeam;
    private String awayTeamID;
    private String homeTeamID;
    private int awayTeamIndex;
    private int homeTeamIndex;
    private List<Player> currentTeam;
    private String leagueName;

    private MatchupAdapter awayLineupAdapter;
    private MatchupAdapter homeLineupAdapter;
    private RecyclerView awayLineupRV;
    private RecyclerView homeLineupRV;

    private static final String KEY_AWAYTEAMNDEX = "keyAwayTeamIndex";
    private static final String KEY_HOMETEAMINDEX = "keyHomeTeamIndex";


    @Override
    protected void setCustomViews() {
        setContentView(R.layout.activity_game);

        SharedPreferences gamePreferences = getSharedPreferences(mSelectionID + StatsEntry.GAME, MODE_PRIVATE);
        totalInnings = gamePreferences.getInt(KEY_TOTALINNINGS, 7);
        mercyRuns = gamePreferences.getInt(StatsEntry.MERCY, 99);
        awayTeamID = gamePreferences.getString(StatsEntry.COLUMN_AWAY_TEAM, "x");
        awayTeamName = getTeamNameFromFirestoreID(awayTeamID);
        homeTeamID = gamePreferences.getString(StatsEntry.COLUMN_HOME_TEAM, "y");
        homeTeamName = getTeamNameFromFirestoreID(homeTeamID);
        int genderSorter = gamePreferences.getInt(KEY_FEMALEORDER, 0);
        int sortArgument = gamePreferences.getInt(KEY_GENDERSORT, 0);

        setTitle(leagueName + ": " + awayTeamName + " @ " + homeTeamName);
        awayTeam = setTeam(awayTeamID);
        homeTeam = setTeam(homeTeamID);

        if (sortArgument > 0) {
            setGendersort(sortArgument, genderSorter + 1);
        } else if (sortArgument < 0) {
            setAddAutoOuts(sortArgument, genderSorter + 1);
        }

        awayLineupRV = findViewById(R.id.away_lineup);
        awayLineupRV.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        awayLineupAdapter = new MatchupAdapter(awayTeam, this, genderSorter + 1);
        awayLineupRV.setAdapter(awayLineupAdapter);

        homeLineupRV = findViewById(R.id.home_lineup);
        homeLineupRV.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        homeLineupAdapter = new MatchupAdapter(homeTeam, this, genderSorter + 1);
        homeLineupRV.setAdapter(homeLineupAdapter);

        scoreboardAwayName = findViewById(R.id.sb_away_name);
        scoreboardHomeName = findViewById(R.id.sb_home_name);
        scoreboardAwayScore = findViewById(R.id.sb_away_score);
        scoreboardHomeScore = findViewById(R.id.sb_home_score);

        scoreboardAwayName.setText(awayTeamName);
        scoreboardHomeName.setText(homeTeamName);
        TextView awayText = findViewById(R.id.away_text);
        TextView homeText = findViewById(R.id.home_text);
        awayText.setText(awayTeamName);
        homeText.setText(homeTeamName);
        awayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditWarningDialog();
            }
        });
        homeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditWarningDialog();
            }
        });
    }

    private void setGendersort(int sortArgument, int femaleOrder) {
        switch (sortArgument) {
            case 1:
                genderSort(awayTeam, femaleOrder);
                break;
            case 2:
                genderSort(homeTeam, femaleOrder);
                break;
            case 3:
                genderSort(awayTeam, femaleOrder);
                genderSort(homeTeam, femaleOrder);
                break;
            default:
        }
    }


    @Override
    protected void revertLineups() {

        Player awayPlayer = null;
        while (awayPlayer == null || awayPlayer.getFirestoreID().equals(AUTO_OUT)) {
            awayPlayer = awayTeam.get(awayTeamIndex);
            if(awayPlayer.getFirestoreID().equals(AUTO_OUT)) {
                increaseAwayIndex();
            }
        }
        Player homePlayer = null;
        while (homePlayer == null || homePlayer.getFirestoreID().equals(AUTO_OUT)) {
            homePlayer = homeTeam.get(homeTeamIndex);
            if(homePlayer.getFirestoreID().equals(AUTO_OUT)) {
                increaseHomeIndex();
            }
        }

        awayTeam.clear();
        String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{awayTeamID, mSelectionID};
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, sortOrder);

        while (cursor.moveToNext()) {
            int order = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_ORDER);
            if (order > 100) {
                continue;
            }
            Player player = new Player(cursor, true);
            awayTeam.add(player);
        }

        homeTeam.clear();
        selectionArgs = new String[]{homeTeamID, mSelectionID};
        cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, sortOrder);

        while (cursor.moveToNext()) {
            int order = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_ORDER);
            if (order > 100) {
                continue;
            }
            Player player = new Player(cursor, true);
            homeTeam.add(player);
        }

        awayTeamIndex = setLineupIndex(awayTeam, awayPlayer.getFirestoreID());
        homeTeamIndex = setLineupIndex(homeTeam, homePlayer.getFirestoreID());

        if(currentTeam == awayTeam) {
            awayLineupAdapter.setCurrentLineupPosition(awayTeamIndex);
            awayLineupRV.scrollToPosition(awayTeamIndex);
        } else {
            homeLineupAdapter.setCurrentLineupPosition(homeTeamIndex);
            homeLineupRV.scrollToPosition(homeTeamIndex);
        }

        awayLineupAdapter.notifyDataSetChanged();
        homeLineupAdapter.notifyDataSetChanged();
    }

    private void setAddAutoOuts(int sortArgument, int femaleOrder) {
        switch (sortArgument) {
            case -1:
                addAutoOuts(awayTeam, femaleOrder);
                break;
            case -2:
                addAutoOuts(homeTeam, femaleOrder);
                break;
            case -3:
                addAutoOuts(awayTeam, femaleOrder);
                addAutoOuts(homeTeam, femaleOrder);
                break;
            default:
        }
    }

//    @Override
//    protected void inningJump(String playerResult) {
//        deleteGameLogs();
//        updatePlayerStats(playerResult, 1);
//        gameOuts = 3;
//        nextBatter();
//        lowestIndex = gameLogIndex;
//        setUndoRedo();
//        outsDisplay.setText("0 outs");
//    }

    protected boolean getSelectionData() {
        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mSelectionID = mainPageSelection.getId();
            leagueName = mainPageSelection.getName();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected Bundle getBoxScoreBundle() {
        Bundle b = new Bundle();
        b.putString("awayTeamName", awayTeamName);
        b.putString("homeTeamName", homeTeamName);
        b.putString("awayTeamID", awayTeamID);
        b.putString("homeTeamID", homeTeamID);
        b.putInt("totalInnings", totalInnings);
        b.putInt("inningNumber", inningNumber);
        b.putInt("awayTeamRuns", awayTeamRuns);
        b.putInt("homeTeamRuns", homeTeamRuns);
        return b;
    }

    @Override
    protected void loadGamePreferences() {
        SharedPreferences gamePreferences
                = getSharedPreferences(mSelectionID + StatsEntry.GAME, MODE_PRIVATE);
        gameLogIndex = gamePreferences.getInt(KEY_GAMELOGINDEX, 0);
        highestIndex = gamePreferences.getInt(KEY_HIGHESTINDEX, 0);
        lowestIndex = gamePreferences.getInt(KEY_LOWESTINDEX, 0);
        inningNumber = gamePreferences.getInt(KEY_INNINGNUMBER, 2);
        totalInnings = gamePreferences.getInt(KEY_TOTALINNINGS, 7);
        awayTeamIndex = gamePreferences.getInt(KEY_AWAYTEAMNDEX, 0);
        homeTeamIndex = gamePreferences.getInt(KEY_HOMETEAMINDEX, 0);
        undoRedo = gamePreferences.getBoolean(KEY_UNDOREDO, false);
        redoEndsGame = gamePreferences.getBoolean(KEY_REDOENDSGAME, redoEndsGame);
        mercyRuns = gamePreferences.getInt(StatsEntry.MERCY, 99);
    }

    @Override
    protected void startGame() {
        super.startGame();
        awayTeamRuns = 0;
        homeTeamRuns = 0;
        inningRuns = 0;

        currentTeam = awayTeam;
        currentBatter = awayTeam.get(0);
        currentRunsLog = new ArrayList<>();
        tempRunsLog = new ArrayList<>();
        currentBaseLogStart = new BaseLog(currentTeam, currentBatter, null, null, null,
                0, 0, 0);

        ContentValues values = new ContentValues();
        String onDeck = currentBatter.getFirestoreID();
        values.put(StatsEntry.COLUMN_ONDECK, onDeck);
        values.put(StatsEntry.COLUMN_TEAM, 0);
        values.put(StatsEntry.COLUMN_OUT, 0);
        values.put(StatsEntry.COLUMN_AWAY_RUNS, 0);
        values.put(StatsEntry.COLUMN_HOME_RUNS, 0);
        values.put(StatsEntry.COLUMN_PLAY, "start");
        values.put(StatsEntry.COLUMN_INNING_CHANGED, 0);
        values.put(StatsEntry.INNINGS, inningNumber);
        values.put(StatsEntry.COLUMN_LEAGUE_ID, mSelectionID);
        getContentResolver().insert(StatsEntry.CONTENT_URI_GAMELOG, values);

        setLineupRVPosition(false);

        setDisplays();
        String outsText = "0 outs";
        outsDisplay.setText(outsText);
    }

    @Override
    protected void resumeGame() {
        setFinalInning();
        gameCursor.moveToPosition(gameLogIndex);
        reloadRunsLog();
        reloadBaseLog();
        inningRuns = StatsContract.getColumnInt(gameCursor, StatsEntry.COLUMN_INNING_RUNS);
        awayTeamRuns = currentBaseLogStart.getAwayTeamRuns();
        homeTeamRuns = currentBaseLogStart.getHomeTeamRuns();
        gameOuts = currentBaseLogStart.getOutCount();
        currentTeam = currentBaseLogStart.getTeam();
        currentBatter = currentBaseLogStart.getBatter();
        int lineupIndex;
        if (currentTeam == homeTeam) {
            awayLineupAdapter.setCurrentLineupPosition(-1);
            setLineupRVPosition(true);
            if (homeTeamIndex >= currentTeam.size()) {
                homeTeamIndex = 0;
            }
            lineupIndex = homeTeamIndex;
        } else {
            homeLineupAdapter.setCurrentLineupPosition(-1);
            setLineupRVPosition(false);
            if (awayTeamIndex >= currentTeam.size()) {
                awayTeamIndex = 0;
            }
            lineupIndex = awayTeamIndex;
        }

        if (currentBatter != currentTeam.get(lineupIndex)) {
            currentBatter = currentTeam.get(lineupIndex);
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_ONDECK, currentBatter.getFirestoreID());
            ContentResolver contentResolver = getContentResolver();
            int gameID = StatsContract.getColumnInt(gameCursor, StatsEntry._ID);
            Uri gameURI = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_GAMELOG, gameID);

            String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
            String[] selectionArgs = new String[]{mSelectionID};
            contentResolver.update(gameURI, values, selection, selectionArgs);
        }
        tempBatter = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_BATTER);
        if (tempRunsLog == null) {
            tempRunsLog = new ArrayList<>();
        } else {
            tempRunsLog.clear();
        }
        resetBases(currentBaseLogStart);
        setDisplays();
        setInningDisplay();
    }

    private void chooseTeamToEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Choose team to edit:");
        builder.setNegativeButton(awayTeamName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                gotoLineupEditor(awayTeamName, awayTeamID);
            }
        });
        builder.setPositiveButton(homeTeamName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                gotoLineupEditor(homeTeamName, homeTeamID);
            }
        });
        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    protected void gotoLineupEditor(String teamName, String teamID) {
        Intent editorIntent = new Intent(LeagueGameActivity.this, SetLineupActivity.class);
        editorIntent.putExtra("ingame", true);
        editorIntent.putExtra("team_name", teamName);
        editorIntent.putExtra("team_id", teamID);
        startActivityForResult(editorIntent, REQUEST_CODE_EDIT);
    }

    @Override
    protected void updateGameLogs() {
        String previousBatterID = currentBatter.getFirestoreID();
        currentBatter = currentTeam.get(getIndex());

        BaseLog currentBaseLogEnd = new BaseLog(currentTeam, currentBatter, firstDisplay.getText().toString(),
                secondDisplay.getText().toString(), thirdDisplay.getText().toString(),
                gameOuts, awayTeamRuns, homeTeamRuns
        );
        currentBaseLogStart = new BaseLog(currentTeam, currentBatter, firstDisplay.getText().toString(),
                secondDisplay.getText().toString(), thirdDisplay.getText().toString(),
                gameOuts, awayTeamRuns, homeTeamRuns
        );
        gameLogIndex++;
        highestIndex = gameLogIndex;

        int team;
        if (currentTeam == awayTeam) {
            team = 0;
        } else {
            team = 1;
        }

        String onDeck = currentBatter.getFirestoreID();

        enterGameValues(currentBaseLogEnd, team, previousBatterID, onDeck);

        setDisplays();
        clearTempState();
    }

    @Override
    protected boolean isTopOfInning() {
        return currentTeam == awayTeam;
    }

    @Override
    protected boolean isLeagueGameOrHomeTeam() {
        return true;
    }

    @Override
    protected void saveGameState() {
        SharedPreferences gamePreferences = getSharedPreferences(mSelectionID + StatsEntry.GAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = gamePreferences.edit();
        editor.putInt(KEY_GAMELOGINDEX, gameLogIndex);
        editor.putInt(KEY_LOWESTINDEX, lowestIndex);
        editor.putInt(KEY_HIGHESTINDEX, highestIndex);
        editor.putInt(KEY_INNINGNUMBER, inningNumber);
        editor.putInt(KEY_AWAYTEAMNDEX, awayTeamIndex);
        editor.putInt(KEY_HOMETEAMINDEX, homeTeamIndex);
        editor.putBoolean(KEY_UNDOREDO, undoRedo);
        editor.putBoolean(KEY_REDOENDSGAME, redoEndsGame);
        editor.putInt(StatsEntry.MERCY, mercyRuns);
        editor.apply();
    }

    @Override
    protected void actionEditLineup() {
        chooseTeamToEditDialog();
    }

    @Override
    protected void openEditWarningDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = new EditWarningDialog();
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    protected void nextInning() {
        gameOuts = 0;
        inningRuns = 0;
        emptyBases();

        setFinalInning();
        increaseLineupIndex();
        if (currentTeam == awayTeam) {
            if (finalInning && homeTeamRuns > awayTeamRuns) {
                redoEndsGame = true;

                showFinishGameDialog();
                return;
            }
            currentTeam = homeTeam;
            awayLineupAdapter.setCurrentLineupPosition(-1);
            setLineupRVPosition(true);
        } else {
            currentTeam = awayTeam;
            homeLineupAdapter.setCurrentLineupPosition(-1);
            setLineupRVPosition(false);
        }
        inningNumber++;

        setInningDisplay();
        inningChanged = 1;
    }

    @Override
    protected boolean isTeamAlternate() {
        return false;
    }

    protected void sendResultToMgr() {

        Intent exitIntent = new Intent();
        exitIntent.putExtra(StatsEntry.COLUMN_AWAY_TEAM, awayTeamID);
        exitIntent.putExtra(StatsEntry.COLUMN_HOME_TEAM, homeTeamID);
        exitIntent.putExtra(StatsEntry.COLUMN_AWAY_RUNS, awayTeamRuns);
        exitIntent.putExtra(StatsEntry.COLUMN_HOME_RUNS, homeTeamRuns);
        setResult(RESULT_CODE_GAME_FINISHED, exitIntent);
        finish();
    }


    @Override
    protected void undoPlay() {
        String undoResult;
        if (gameLogIndex > lowestIndex) {
            undoResult = getUndoPlayResult();
            if (inningChanged == 1) {
                inningNumber--;
                setInningDisplay();
                if (currentTeam == awayTeam) {
                    awayLineupAdapter.setCurrentLineupPosition(-1);
                    awayLineupAdapter.notifyDataSetChanged();
                } else if (currentTeam == homeTeam) {
                    homeLineupAdapter.setCurrentLineupPosition(-1);
                    homeLineupAdapter.notifyDataSetChanged();
                }
            }
            gameLogIndex--;
        } else {
            return;
        }
        undoLogs();
        currentTeam = currentBaseLogStart.getTeam();
        if (currentTeam == awayTeam) {
            decreaseAwayIndex();
        } else if (currentTeam == homeTeam) {
            decreaseHomeIndex();
        }
        inningChanged = 0;

        resetBases(currentBaseLogStart);
        updatePlayerStats(undoResult, -1);
        setDisplays();
    }

    @Override
    protected void redoPlay() {
        String redoResult = getRedoResult();
        if(redoResult == null) {
            return;
        }
        currentTeam = currentBaseLogStart.getTeam();
        if (inningChanged == 1) {
            inningNumber++;
            if (currentTeam == awayTeam) {
                increaseHomeIndex();
                setLineupRVPosition(false);
                homeLineupAdapter.setCurrentLineupPosition(-1);
                homeLineupAdapter.notifyDataSetChanged();
            } else if (currentTeam == homeTeam) {
                increaseAwayIndex();
                setLineupRVPosition(true);
                awayLineupAdapter.setCurrentLineupPosition(-1);
                awayLineupAdapter.notifyDataSetChanged();
            }
            setInningDisplay();
        }
        if (inningChanged == 0) {
            increaseLineupIndex();
        } else {
            inningChanged = 0;
        }

        resetBases(currentBaseLogStart);
        updatePlayerStats(redoResult, 1);
        if (redoEndsGame) {
            if (gameCursor.moveToNext()) {
                gameCursor.moveToPrevious();
            } else {
                showFinishGameDialog();
            }
        }
        setDisplays();
    }

    @Override
    protected List<Player> getTeamLineup() {
        int teamChoice = StatsContract.getColumnInt(gameCursor, StatsEntry.COLUMN_TEAM);
        List<Player> teamLineup;
        switch (teamChoice) {
            case 0:
                teamLineup = awayTeam;
                break;
            case 1:
                teamLineup = homeTeam;
                break;
            default:
                return null;
        }
        return teamLineup;
    }

    @Override
    protected void increaseLineupIndex() {
        if (currentTeam == awayTeam) {
            increaseAwayIndex();
        } else if (currentTeam == homeTeam) {
            increaseHomeIndex();
        } else {
            increaseAwayIndex();
        }
    }

    @Override
    protected void decreaseLineupIndex() {
        if (currentTeam == awayTeam) {
            decreaseAwayIndex();
        } else if (currentTeam == homeTeam) {
            decreaseHomeIndex();
        } else {
            decreaseAwayIndex();
        }
    }

    @Override
    protected void checkLineupIndex() {
        if (awayTeamIndex >= awayTeam.size()) {
            awayTeamIndex = awayTeam.size() - 1;
        }
        if (homeTeamIndex >= homeTeam.size()) {
            homeTeamIndex = homeTeam.size() - 1;
        }
    }


    private void increaseAwayIndex() {
        awayTeamIndex++;
        if (awayTeamIndex >= awayTeam.size()) {
            awayTeamIndex = 0;
        }
        setLineupRVPosition(false);
    }

    private void increaseHomeIndex() {
        homeTeamIndex++;
        if (homeTeamIndex >= homeTeam.size()) {
            homeTeamIndex = 0;
        }
        setLineupRVPosition(true);
    }

    private void decreaseAwayIndex() {
        awayTeamIndex--;
        if (awayTeamIndex < 0) {
            awayTeamIndex = awayTeam.size() - 1;
        }
        setLineupRVPosition(false);
    }

    private void decreaseHomeIndex() {
        homeTeamIndex--;
        if (homeTeamIndex < 0) {
            homeTeamIndex = homeTeam.size() - 1;
        }
        setLineupRVPosition(true);
    }

    private int getIndex() {
        if (currentTeam == awayTeam) {
            return awayTeamIndex;
        } else if (currentTeam == homeTeam) {
            return homeTeamIndex;
        }
        return 0;
    }

    private void setLineupRVPosition(boolean home) {
        if (home) {
            homeLineupAdapter.setCurrentLineupPosition(homeTeamIndex);
            homeLineupRV.scrollToPosition(homeTeamIndex);
            homeLineupAdapter.notifyDataSetChanged();
        } else {
            awayLineupAdapter.setCurrentLineupPosition(awayTeamIndex);
            awayLineupRV.scrollToPosition(awayTeamIndex);
            awayLineupAdapter.notifyDataSetChanged();
        }
    }
}