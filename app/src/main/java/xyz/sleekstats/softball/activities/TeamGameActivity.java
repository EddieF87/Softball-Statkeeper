package xyz.sleekstats.softball.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.MatchupAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.data.TimeStampUpdater;
import xyz.sleekstats.softball.dialogs.EndOfGameDialog;
import xyz.sleekstats.softball.objects.BaseLog;
import xyz.sleekstats.softball.objects.MainPageSelection;
import xyz.sleekstats.softball.objects.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamGameActivity extends GameActivity implements EndOfGameDialog.OnFragmentInteractionListener {

    private MatchupAdapter mLineupAdapter;
    private RecyclerView mRecyclerView;

    private TextView otherTeamOutsView;
    private TextView otherTeamRunsView;
    private int otherTeamRuns;
    private ImageView addOutButton;

    private List<Player> myTeam;
    private int myTeamIndex;
    private String myTeamName;

    private boolean isHome;
    private boolean isTop;
    private boolean isAlternate;

    private static final String KEY_MYTEAMINDEX = "keyMyTeamIndex";
    private static final String KEY_ISHOME = "isHome";

    @Override
    protected void getSelectionData() {
        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            selectionID = mainPageSelection.getId();
            myTeamName = mainPageSelection.getName();
        } catch (Exception e) {
            Intent intent = new Intent(TeamGameActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected Bundle getBoxScoreBundle() {
        Bundle b = new Bundle();
        b.putString("awayTeamName", awayTeamName);
        b.putString("homeTeamName", homeTeamName);
        b.putString("awayTeamID", selectionID);
        b.putString("homeTeamID", null);
        b.putInt("totalInnings", totalInnings);
        b.putInt("inningNumber", inningNumber);
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
        myTeamIndex = gamePreferences.getInt(KEY_MYTEAMINDEX, 0);
        Log.d(TAG, "myTeamIndex = gamePref  " + myTeamIndex);
        undoRedo = gamePreferences.getBoolean(KEY_UNDOREDO, false);
        redoEndsGame = gamePreferences.getBoolean(KEY_REDOENDSGAME, false);

        boolean sortArgument = gamePreferences.getBoolean(KEY_GENDERSORT, false);
        if (sortArgument) {
            int genderSorter = gamePreferences.getInt(KEY_FEMALEORDER, 0);
            myTeam = genderSort(myTeam, genderSorter);
        }
    }

    @Override
    protected void setCustomViews() {
        setContentView(R.layout.activity_game);

        SharedPreferences settingsPreferences =
                getSharedPreferences(selectionID + StatsEntry.SETTINGS, MODE_PRIVATE);
        int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0) + 1;
        totalInnings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);

        Bundle args = getIntent().getExtras();

        SharedPreferences gamePreferences = getSharedPreferences(selectionID + StatsEntry.GAME, MODE_PRIVATE);

        boolean sortArgument = false;

        if (args != null) {
            if (args.containsKey(KEY_ISHOME)) {
                isHome = args.getBoolean(KEY_ISHOME);
            } else {
                isHome = gamePreferences.getBoolean(KEY_ISHOME, false);
            }
            SharedPreferences.Editor editor = gamePreferences.edit();

            sortArgument = args.getBoolean("sortArgument");

            editor.putBoolean(KEY_ISHOME, isHome);
            editor.putInt(KEY_TOTALINNINGS, totalInnings);
            editor.putBoolean(KEY_GENDERSORT, sortArgument);
            editor.putInt(KEY_FEMALEORDER, genderSorter);
            editor.apply();
        } else {
            isHome = gamePreferences.getBoolean(KEY_ISHOME, false);
        }

        if (isHome) {
            homeTeamName = myTeamName;
            awayTeamName = "Away Team";
        } else {
            awayTeamName = myTeamName;
            homeTeamName = "Home Team";
        }
        myTeam = setTeam(selectionID);
        setTitle(awayTeamName + " @ " + homeTeamName);

        if (sortArgument) {
            myTeam = genderSort(myTeam, genderSorter);
        }

        addOutButton = findViewById(R.id.btn_add_out);
        addOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                teamAddOut();
            }
        });

        findViewById(R.id.home_text).setVisibility(View.GONE);
        findViewById(R.id.home_lineup).setVisibility(View.GONE);
        mRecyclerView = findViewById(R.id.away_lineup);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        mLineupAdapter = new MatchupAdapter(myTeam, this, genderSorter);
        mRecyclerView.setAdapter(mLineupAdapter);

        TextView teamText = findViewById(R.id.away_text);
        teamText.setText(myTeamName);
        teamText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoLineupEditor(myTeamName, selectionID);
            }
        });
    }

    @Override
    protected void startGame() {
        super.startGame();

        isTop = true;
        chooseDisplay();

        awayTeamRuns = 0;
        homeTeamRuns = 0;

        currentBatter = myTeam.get(0);
        currentRunsLog = new ArrayList<>();
        tempRunsLog = new ArrayList<>();
        currentBaseLogStart = new BaseLog(myTeam, currentBatter, null, null, null,
                0, 0, 0);

        ContentValues values = new ContentValues();
        String onDeck;
        if (isAlternate) {
            myTeamIndex = 0;
            onDeck = null;
            step1View.setVisibility(View.GONE);
            step2View.setVisibility(View.GONE);
            step3View.setVisibility(View.GONE);
            step4View.setVisibility(View.GONE);
        } else {
            onDeck = currentBatter.getFirestoreID();
        }
        values.put(StatsEntry.COLUMN_ONDECK, onDeck);
        values.put(StatsEntry.COLUMN_TEAM, 0);
        values.put(StatsEntry.COLUMN_OUT, 0);
        values.put(StatsEntry.COLUMN_AWAY_RUNS, 0);
        values.put(StatsEntry.COLUMN_HOME_RUNS, 0);
        values.put(StatsEntry.COLUMN_PLAY, "start");
        values.put(StatsEntry.COLUMN_INNING_CHANGED, 0);
        values.put(StatsEntry.INNINGS, inningNumber);
        getContentResolver().insert(StatsEntry.CONTENT_URI_GAMELOG, values);
        Log.d(TAG, gameLogIndex + " " + values.toString());

        String outsText = "0 outs";
        outsDisplay.setText(outsText);

        if (isAlternate) {
            setInningDisplay();
            setScoreDisplay();
            return;
        }
        try {
            setDisplays();
        } catch (Exception e) {
        }
    }

    @Override
    protected void resumeGame() {
        isTop = (inningNumber % 2 == 0);
        if (inningNumber / 2 >= totalInnings) {
            finalInning = true;
        }
        chooseDisplay();
        gameCursor.moveToPosition(gameLogIndex);
        reloadRunsLog();
        reloadBaseLog();
        awayTeamRuns = currentBaseLogStart.getAwayTeamRuns();
        homeTeamRuns = currentBaseLogStart.getHomeTeamRuns();
        gameOuts = currentBaseLogStart.getOutCount();
        currentBatter = currentBaseLogStart.getBatter();
        if (isAlternate) {
            int rrri = 0;
        }
        if (!isAlternate && currentBatter != myTeam.get(myTeamIndex)) {
            currentBatter = myTeam.get(myTeamIndex);
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
        setInningDisplay();
        if (isAlternate) {
            setScoreDisplay();
            return;
        }
        setDisplays();
    }

    @Override
    protected void updateGameLogs() {
        String previousBatterID;
        if (currentBatter != null) {
            previousBatterID = currentBatter.getFirestoreID();
            currentBatter = myTeam.get(myTeamIndex);
        } else {
            currentBatter = myTeam.get(myTeamIndex);
            previousBatterID = null;
        }

        BaseLog currentBaseLogEnd = new BaseLog(myTeam, currentBatter, firstDisplay.getText().toString(),
                secondDisplay.getText().toString(), thirdDisplay.getText().toString(),
                gameOuts, awayTeamRuns, homeTeamRuns
        );
        currentBaseLogStart = new BaseLog(myTeam, currentBatter, firstDisplay.getText().toString(),
                secondDisplay.getText().toString(), thirdDisplay.getText().toString(),
                gameOuts, awayTeamRuns, homeTeamRuns
        );
        gameLogIndex++;
        highestIndex = gameLogIndex;

        String onDeck = currentBatter.getFirestoreID();

        if (isAlternate && previousBatterID != null) {
            onDeck = null;
        }

        enterGameValues(currentBaseLogEnd, 0, previousBatterID, onDeck);

        if (!isAlternate) {
            setDisplays();
        } else {
            setUndoRedo();
        }

        clearTempState();
    }

    @Override
    protected void saveGameState() {
        SharedPreferences gamePreferences = getSharedPreferences(selectionID + StatsEntry.GAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = gamePreferences.edit();
        editor.putInt(KEY_GAMELOGINDEX, gameLogIndex);
        editor.putInt(KEY_HIGHESTINDEX, highestIndex);
        editor.putInt(KEY_INNINGNUMBER, inningNumber);
        editor.putInt(KEY_MYTEAMINDEX, myTeamIndex);
        editor.putBoolean(KEY_UNDOREDO, undoRedo);
        editor.putBoolean(KEY_REDOENDSGAME, redoEndsGame);
        editor.apply();
    }

    @Override
    protected void nextInning() {
        Log.d("phil", "nextInning");
        gameOuts = 0;
        emptyBases();

        if (inningNumber / 2 >= totalInnings) {
            finalInning = true;
        }

        inningNumber++;
        chooseDisplay();
        setInningDisplay();
        inningChanged = 1;
    }

    @Override
    protected void setDisplays() {
        super.setDisplays();
        setLineupRVPosition();
    }

    private void chooseDisplay() {
        Log.d("phil", "chooseDisplay");
        isTop = (inningNumber % 2 == 0);
        if (isTop) {
            if (isHome) {
                setAlternateDisplay(true);
                isAlternate = true;
            } else {
                setAlternateDisplay(false);
                isAlternate = false;
            }
        } else {
            if (finalInning && homeTeamRuns > awayTeamRuns) {
                if (!redoEndsGame) {
                    redoEndsGame = true;
                    increaseLineupIndex();
                    showFinishGameDialog();
                }
                return;
            }
            if (isHome) {
                setAlternateDisplay(false);
                isAlternate = false;
            } else {
                setAlternateDisplay(true);
                isAlternate = true;
            }
        }
    }

    private void setAlternateDisplay(boolean alternateDisplay) {
        View radioGroup = findViewById(R.id.radio_group_results);
        View diamond = findViewById(R.id.diamond);
        View alternateTeamDisplay = findViewById(R.id.alternate_team_display);
        if (alternateDisplay) {
            setUndoRedo();
            radioGroup.setVisibility(View.GONE);
            diamond.setVisibility(View.GONE);
            alternateTeamDisplay.setVisibility(View.VISIBLE);
            TextView otherTeamTitle = findViewById(R.id.other_team_title);
            if (isHome) {
                otherTeamTitle.setText(awayTeamName);
            } else {
                otherTeamTitle.setText(homeTeamName);
            }
            addOutButton.setEnabled(true);
            otherTeamOutsView = alternateTeamDisplay.findViewById(R.id.tv_outs);
            otherTeamRunsView = alternateTeamDisplay.findViewById(R.id.tv_runs_scored);
            otherTeamRuns = 0;
            otherTeamOutsView.setText(String.valueOf(gameOuts));
            otherTeamRunsView.setText(String.valueOf(otherTeamRuns));
        } else {
            radioGroup.setVisibility(View.VISIBLE);
            diamond.setVisibility(View.VISIBLE);
            alternateTeamDisplay.setVisibility(View.GONE);
        }
        avgDisplay.setText("");
        hrDisplay.setText("");
        rbiDisplay.setText("");
        runDisplay.setText("");
        if (isHome) {
            nowBatting.setText(awayTeamName);
        } else {
            nowBatting.setText(homeTeamName);
        }
    }

    public void teamAddRun(View v) {
        if (isHome) {
            awayTeamRuns++;
        } else {
            homeTeamRuns++;
        }
        otherTeamRuns++;
        otherTeamRunsView.setText(String.valueOf(otherTeamRuns));
        setScoreDisplay();
    }

    public void teamRemoveRun(View v) {
        if (otherTeamRuns <= 0) {
            return;
        }
        otherTeamRuns--;
        otherTeamRunsView.setText(String.valueOf(otherTeamRuns));
        if (isHome) {
            awayTeamRuns--;
        } else {
            homeTeamRuns--;
        }
        setScoreDisplay();
    }

    private void teamAddOut() {
        gameOuts++;
        otherTeamOutsView.setText(String.valueOf(gameOuts));
        if (gameOuts >= 3) {
            addOutButton.setEnabled(false);
            currentBatter = null;
            if (undoRedo) {
                deleteGameLogs();
                currentRunsLog.clear();
                currentRunsLog.addAll(tempRunsLog);
            }
            nextBatter();
        }
    }

    public void teamRemoveOut(View v) {
        if (gameOuts <= 0) {
            return;
        }
        gameOuts--;
        otherTeamOutsView.setText(String.valueOf(gameOuts));
    }

    @Override
    protected void firestoreUpdate() {
        long updateTime = System.currentTimeMillis();
        transferStats(updateTime);
        if (isHome) {
            sendTeamIntent(updateTime, selectionID, homeTeamRuns, awayTeamRuns);
            sendBoxscoreIntent(updateTime, StatsEntry.COLUMN_AWAY_TEAM, selectionID, awayTeamRuns, homeTeamRuns);
        } else {
            sendTeamIntent(updateTime, selectionID, awayTeamRuns, homeTeamRuns);
            sendBoxscoreIntent(updateTime, selectionID, StatsEntry.COLUMN_HOME_TEAM, awayTeamRuns, homeTeamRuns);
        }
        sendPlayersIntent(updateTime);
        TimeStampUpdater.updateTimeStamps(this, selectionID, updateTime);
    }

    @Override
    protected boolean isTeamAlternate() {
        return isAlternate;
    }

    @Override
    protected boolean isLeagueGameOrHomeTeam() {
        return isHome;
    }

    private static final String TAG = "UNDOREDOFIX";

    @Override
    protected void undoPlay() {
        String undoResult;
        if (gameLogIndex > 0) {
            undoResult = getUndoPlayResult();
            String ondeckbt;
            if (currentBatter == null) {
                ondeckbt = "null";
            } else {
                ondeckbt = currentBatter.getFirestoreID();
            }
            Log.d(TAG, gameLogIndex + " UNDO=" + undoResult + "   " + StatsEntry.COLUMN_BATTER
                    + "= " + tempBatter + "  " + StatsEntry.COLUMN_ONDECK + "  " + ondeckbt +
                    "   innchanged?" + (inningChanged == 1));
            if (inningChanged == 1) {
                inningNumber--;
                chooseDisplay();
                setInningDisplay();
            }
            isAlternate = (StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_ONDECK) == null);
            gameLogIndex--;
        } else {
            return;
        }
        undoLogs();

        resetBases(currentBaseLogStart);
        if (isAlternate) {
            int pos = gameCursor.getPosition();
            Log.d(TAG + "zz", "gameCursor " + pos + "   " + "   gameLogIndex" + gameLogIndex);
            Log.d(TAG + "zz", "decreaseLineupIndex1");
            decreaseLineupIndex();
            chooseDisplay();
            setInningDisplay();
        } else {
            int pos = gameCursor.getPosition();
            Log.d(TAG + "zz", "gameCursor " + pos + "   " + "   gameLogIndex" + gameLogIndex);
            String alternateString = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_ONDECK);
            if (alternateString != null) {
                Log.d(TAG + "zz", "ondeck = " + alternateString);
            } else {
                Log.d(TAG + "zz", "ondeck = null");
            }
            isAlternate = (StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_ONDECK) == null);
            Log.d(TAG + "zz", "isAlternate = " + isAlternate);

            if (isAlternate) {
                chooseDisplay();
                setInningDisplay();
                return;
            }
            Log.d(TAG + "zz", "decreaseLineupIndex2");
            decreaseLineupIndex();
        }
        updatePlayerStats(undoResult, -1);
        setDisplays();
    }

    @Override
    protected void redoPlay() {
        String redoResult = getRedoResult();
        if (redoResult == null && !isAlternate) {
            return;
        }
        String ondeckbt;
        if (currentBatter == null) {
            ondeckbt = "null";
        } else {
            ondeckbt = currentBatter.getFirestoreID();
        }
        Log.d(TAG, gameLogIndex + " REDO=" + redoResult + "   " + StatsEntry.COLUMN_BATTER
                + "= " + tempBatter + "  " + StatsEntry.COLUMN_ONDECK + "  " + ondeckbt +
                "   innchanged?" + (inningChanged == 1));
        if (inningChanged == 1) {
            inningNumber++;
            chooseDisplay();
            setInningDisplay();
            inningChanged = 0;
        }
        resetBases(currentBaseLogStart);
        isAlternate = (tempBatter == null);

        if (!isAlternate) {
            increaseLineupIndex();
            isAlternate = (currentBatter == null);
            updatePlayerStats(redoResult, 1);

            if (!isAlternate && !(undoRedo && tempBatter == null)) {
                setDisplays();
            }
        }
        if (redoEndsGame) {
            if (gameCursor.moveToNext()) {
                gameCursor.moveToPrevious();
            } else {
                showFinishGameDialog();
            }
        }
        if (isAlternate) {
            chooseDisplay();
            if (tempBatter == null) {
                setDisplays();
            }
        }
    }

    @Override
    protected List<Player> getTeamLineup() {
        return myTeam;
    }

    @Override
    protected boolean isTopOfInning() {
        return isTop;
    }

    @Override
    protected void increaseLineupIndex() {
        myTeamIndex++;
        if (myTeamIndex >= myTeam.size()) {
            myTeamIndex = 0;
        }
        Log.d(TAG, "myTeamIndex +   " + myTeamIndex);
    }

    private void decreaseLineupIndex() {
        myTeamIndex--;
        if (myTeamIndex < 0) {
            myTeamIndex = myTeam.size() - 1;
        }
        Log.d(TAG, "myTeamIndex -   " + myTeamIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    protected void exitToManager() {
        Intent exitIntent = new Intent(TeamGameActivity.this, TeamManagerActivity.class);
        startActivity(exitIntent);
        finish();
    }

    @Override
    protected void actionEditLineup() {
        gotoLineupEditor(myTeamName, selectionID);
    }

    private void setLineupRVPosition() {
        mLineupAdapter.setCurrentLineupPosition(myTeamIndex);
        mRecyclerView.scrollToPosition(myTeamIndex);
        mLineupAdapter.notifyDataSetChanged();
    }
}

