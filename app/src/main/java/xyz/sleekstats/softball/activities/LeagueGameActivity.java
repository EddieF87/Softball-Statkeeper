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
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.MatchupAdapter;
import xyz.sleekstats.softball.data.FirestoreHelper;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsDbHelper;
import xyz.sleekstats.softball.data.TimeStampUpdater;
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

    private static final String KEY_AWAYTEAM = "keyAwayTeam";
    private static final String KEY_HOMETEAM = "keyHomeTeam";
    private static final String KEY_AWAYTEAMNDEX = "keyAwayTeamIndex";
    private static final String KEY_HOMETEAMINDEX = "keyHomeTeamIndex";


    @Override
    protected void setCustomViews() {
        setContentView(R.layout.activity_lg_game);

        SharedPreferences gamePreferences = getSharedPreferences(selectionID + StatsEntry.GAME, MODE_PRIVATE);
        totalInnings = gamePreferences.getInt(KEY_TOTALINNINGS, 7);
        awayTeamID = gamePreferences.getString(KEY_AWAYTEAM, "x");
        awayTeamName = getTeamNameFromFirestoreID(awayTeamID);
        homeTeamID = gamePreferences.getString(KEY_HOMETEAM, "y");
        homeTeamName = getTeamNameFromFirestoreID(homeTeamID);
        int genderSorter = gamePreferences.getInt(KEY_FEMALEORDER, 0);
        int sortArgument = gamePreferences.getInt(KEY_GENDERSORT, 0);

        setTitle(leagueName + ": " + awayTeamName + " @ " + homeTeamName);
        awayTeam = setTeam(awayTeamID);
        homeTeam = setTeam(homeTeamID);

        if (sortArgument != 0) {
            setGendersort(sortArgument, genderSorter + 1);
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

        TextView awayText = findViewById(R.id.away_text);
        TextView homeText = findViewById(R.id.home_text);
        awayText.setText(awayTeamName);
        homeText.setText(homeTeamName);
        awayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoLineupEditor(awayTeamName, awayTeamID);
            }
        });
        homeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoLineupEditor(homeTeamName, homeTeamID);
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

    protected void getSelectionData() {
        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            selectionID = mainPageSelection.getId();
            leagueName = mainPageSelection.getName();
        } catch (Exception e) {
            Intent intent = new Intent(LeagueGameActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
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
        b.putInt("awayTeamRuns", awayTeamRuns);
        b.putInt("homeTeamRuns", homeTeamRuns);
        return b;
    }

    @Override
    protected void loadGamePreferences() {
        SharedPreferences gamePreferences
                = getSharedPreferences(selectionID + StatsEntry.GAME, MODE_PRIVATE);
        gameLogIndex = gamePreferences.getInt(KEY_GAMELOGINDEX, 0);
        highestIndex = gamePreferences.getInt(KEY_HIGHESTINDEX, 0);
        inningNumber = gamePreferences.getInt(KEY_INNINGNUMBER, 2);
        totalInnings = gamePreferences.getInt(KEY_TOTALINNINGS, 7);
        awayTeamIndex = gamePreferences.getInt(KEY_AWAYTEAMNDEX, 0);
        homeTeamIndex = gamePreferences.getInt(KEY_HOMETEAMINDEX, 0);
        undoRedo = gamePreferences.getBoolean(KEY_UNDOREDO, false);
        redoEndsGame = gamePreferences.getBoolean(KEY_REDOENDSGAME, redoEndsGame);
    }

    @Override
    protected void startGame() {
        super.startGame();
        awayTeamRuns = 0;
        homeTeamRuns = 0;

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
        values.put(StatsEntry.COLUMN_LOG_INDEX, gameLogIndex);
        getContentResolver().insert(StatsEntry.CONTENT_URI_GAMELOG, values);

        setLineupRVPosition(false);

        setDisplays();
        String outsText = "0 outs";
        outsDisplay.setText(outsText);
    }

    @Override
    protected void resumeGame() {
        if (inningNumber / 2 >= totalInnings) {
            finalInning = true;
        }
        gameCursor.moveToPosition(gameLogIndex);
        reloadRunsLog();
        reloadBaseLog();
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
            contentResolver.update(gameURI, values, null, null);
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

    @Override
    protected void updateGameLogs() {
        String previousBatterID = currentBatter.getFirestoreID();
        currentBatter = currentTeam.get(getIndex());
        Log.d("xxx", "updateGameLogs: " + currentBatter.getName());

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
            Log.d("xxx", "team = 0;");
        } else {
            team = 1;
            Log.d("xxx", "team = 1;");
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
        SharedPreferences gamePreferences = getSharedPreferences(selectionID + StatsEntry.GAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = gamePreferences.edit();
        editor.putInt(KEY_GAMELOGINDEX, gameLogIndex);
        editor.putInt(KEY_HIGHESTINDEX, highestIndex);
        editor.putInt(KEY_INNINGNUMBER, inningNumber);
        editor.putInt(KEY_AWAYTEAMNDEX, awayTeamIndex);
        editor.putInt(KEY_HOMETEAMINDEX, homeTeamIndex);
        editor.putBoolean(KEY_UNDOREDO, undoRedo);
        editor.putBoolean(KEY_REDOENDSGAME, redoEndsGame);
        editor.apply();
    }

    @Override
    protected void exitToManager() {
        Intent exitIntent = new Intent(LeagueGameActivity.this, LeagueManagerActivity.class);
        startActivity(exitIntent);
        finish();
    }

    @Override
    protected void actionEditLineup() {
        chooseTeamToEditDialog();
    }

    @Override
    protected void nextInning() {
        gameOuts = 0;
        emptyBases();

        if (inningNumber / 2 >= totalInnings) {
            finalInning = true;
        }
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

    @Override
    protected void firestoreUpdate() {
        Log.d("zztop", "firestoreUpdate");
        long updateTime = System.currentTimeMillis();
        transferStats(updateTime);
        sendTeamIntent(updateTime, awayTeamID, awayTeamRuns, homeTeamRuns);
        sendTeamIntent(updateTime, homeTeamID, homeTeamRuns, awayTeamRuns);
        sendPlayersIntent(updateTime);
        sendBoxscoreIntent(updateTime, awayTeamID, homeTeamID, awayTeamRuns, homeTeamRuns);
        TimeStampUpdater.updateTimeStamps(this, selectionID, updateTime);
    }

    @Override
    protected void undoPlay() {
        String undoResult;
        if (gameLogIndex > 0) {
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
    }

    @Override
    protected List<Player> getTeamLineup() {
        int teamChoice = StatsContract.getColumnInt(gameCursor, StatsEntry.COLUMN_TEAM);
        List<Player> teamLineup;
        if (teamChoice == 0) {
            teamLineup = awayTeam;
        } else if (teamChoice == 1) {
            teamLineup = homeTeam;
        } else {
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