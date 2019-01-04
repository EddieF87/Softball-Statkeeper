package xyz.sleekstats.softball.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.MatchupAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.dialogs.EditWarningDialog;
import xyz.sleekstats.softball.dialogs.EndOfGameDialog;
import xyz.sleekstats.softball.models.BaseLog;
import xyz.sleekstats.softball.models.MainPageSelection;
import xyz.sleekstats.softball.models.Player;

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
    protected boolean getSelectionData() {
        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mSelectionID = mainPageSelection.getId();
            myTeamName = mainPageSelection.getName();
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
        b.putString("awayTeamID", mSelectionID);
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
                = getSharedPreferences(mSelectionID + StatsEntry.GAME, MODE_PRIVATE);
        gameLogIndex = gamePreferences.getInt(KEY_GAMELOGINDEX, 0);
        lowestIndex = gamePreferences.getInt(KEY_LOWESTINDEX, 0);
        highestIndex = gamePreferences.getInt(KEY_HIGHESTINDEX, 0);
        inningNumber = gamePreferences.getInt(KEY_INNINGNUMBER, 2);
        totalInnings = gamePreferences.getInt(KEY_TOTALINNINGS, 7);
        myTeamIndex = gamePreferences.getInt(KEY_MYTEAMINDEX, 0);
        undoRedo = gamePreferences.getBoolean(KEY_UNDOREDO, false);
        redoEndsGame = gamePreferences.getBoolean(KEY_REDOENDSGAME, false);
        mercyRuns = gamePreferences.getInt(StatsEntry.MERCY, 99);

        int sortArgument = gamePreferences.getInt(KEY_GENDERSORT, 0);
        if (sortArgument > 0) {
            int genderSorter = gamePreferences.getInt(KEY_FEMALEORDER, 0);
            myTeam = genderSort(myTeam, genderSorter);
        } else if (sortArgument <0) {
            int genderSorter = gamePreferences.getInt(KEY_FEMALEORDER, 0);
            myTeam = addAutoOuts(myTeam, genderSorter);
        }
    }

    @Override
    protected void setCustomViews() {
        setContentView(R.layout.activity_game);

        SharedPreferences settingsPreferences =
                getSharedPreferences(mSelectionID + StatsEntry.SETTINGS, MODE_PRIVATE);
        int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0) + 1;
        totalInnings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);
        mercyRuns = settingsPreferences.getInt(StatsEntry.MERCY, 99);


        Bundle args = getIntent().getExtras();

        SharedPreferences gamePreferences = getSharedPreferences(mSelectionID + StatsEntry.GAME, MODE_PRIVATE);

        int sortArgument = 0;

        if (args != null) {
            if (args.containsKey(KEY_ISHOME)) {
                isHome = args.getBoolean(KEY_ISHOME);
            } else {
                isHome = gamePreferences.getBoolean(KEY_ISHOME, false);
            }
            SharedPreferences.Editor editor = gamePreferences.edit();

            sortArgument = args.getInt(KEY_GENDERSORT, 0);

            editor.putBoolean(KEY_ISHOME, isHome);
            editor.putInt(KEY_TOTALINNINGS, totalInnings);
            editor.putInt(KEY_GENDERSORT, sortArgument);
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

        scoreboardAwayName = findViewById(R.id.sb_away_name);
        scoreboardHomeName = findViewById(R.id.sb_home_name);
        scoreboardAwayScore = findViewById(R.id.sb_away_score);
        scoreboardHomeScore = findViewById(R.id.sb_home_score);

        scoreboardAwayName.setText(awayTeamName);
        scoreboardHomeName.setText(homeTeamName);

        myTeam = setTeam(mSelectionID);
        setTitle(awayTeamName + " @ " + homeTeamName);

        if (sortArgument < 0) {
            myTeam = addAutoOuts(myTeam, genderSorter);
        } else if(sortArgument > 0) {
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
                openEditWarningDialog();
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
        inningRuns = 0;

        currentBatter = myTeam.get(0);
        currentRunsLog = new ArrayList<>();
        tempRunsLog = new ArrayList<>();
        currentBaseLogStart = new BaseLog(myTeam, currentBatter, null, null, null,
                0, 0, 0);

        ContentValues values = new ContentValues();
        String onDeck;
        if (isAlternate && gameHelp) {
            myTeamIndex = 0;
            onDeck = null;
            if(step1View != null) {
                step1View.setVisibility(View.GONE);
                step2View.setVisibility(View.GONE);
                step3View.setVisibility(View.GONE);
                step4View.setVisibility(View.GONE);
            }
        } else {
            onDeck = currentBatter.getFirestoreID();
        }
        values.put(StatsEntry.COLUMN_LEAGUE_ID, mSelectionID);
        values.put(StatsEntry.COLUMN_ONDECK, onDeck);
        values.put(StatsEntry.COLUMN_TEAM, 0);
        values.put(StatsEntry.COLUMN_OUT, 0);
        values.put(StatsEntry.COLUMN_AWAY_RUNS, 0);
        values.put(StatsEntry.COLUMN_HOME_RUNS, 0);
        values.put(StatsEntry.COLUMN_PLAY, "start");
        values.put(StatsEntry.COLUMN_INNING_CHANGED, 0);
        values.put(StatsEntry.INNINGS, inningNumber);
        getContentResolver().insert(StatsEntry.CONTENT_URI_GAMELOG, values);

        String outsText = "0 outs";
        outsDisplay.setText(outsText);

        if (isAlternate) {
            setInningDisplay();
            setScoreDisplay();
            return;
        }
        try {
            setDisplays();
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void resumeGame() {
        isTop = (inningNumber % 2 == 0);
        setFinalInning();
        chooseDisplay();
        gameCursor.moveToPosition(gameLogIndex);
        reloadRunsLog();
        reloadBaseLog();
        inningRuns = StatsContract.getColumnInt(gameCursor, StatsEntry.COLUMN_INNING_RUNS);
        awayTeamRuns = currentBaseLogStart.getAwayTeamRuns();
        homeTeamRuns = currentBaseLogStart.getHomeTeamRuns();
        gameOuts = currentBaseLogStart.getOutCount();
        currentBatter = currentBaseLogStart.getBatter();
        if (!isAlternate && currentBatter != myTeam.get(myTeamIndex)) {
            currentBatter = myTeam.get(myTeamIndex);
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_ONDECK, currentBatter.getFirestoreID());
            ContentResolver contentResolver = getContentResolver();
            int gameID = StatsContract.getColumnInt(gameCursor, StatsEntry._ID);
            Uri gameURI = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_GAMELOG, gameID);
            String qSelection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
            String[] qSelectionArgs = new String[]{mSelectionID};
            contentResolver.update(gameURI, values, qSelection, qSelectionArgs);
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
        SharedPreferences gamePreferences = getSharedPreferences(mSelectionID + StatsEntry.GAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = gamePreferences.edit();
        editor.putInt(KEY_GAMELOGINDEX, gameLogIndex);
        editor.putInt(KEY_LOWESTINDEX, lowestIndex);
        editor.putInt(KEY_HIGHESTINDEX, highestIndex);
        editor.putInt(KEY_INNINGNUMBER, inningNumber);
        editor.putInt(KEY_MYTEAMINDEX, myTeamIndex);
        editor.putBoolean(KEY_UNDOREDO, undoRedo);
        editor.putBoolean(KEY_REDOENDSGAME, redoEndsGame);
        editor.putInt(StatsEntry.MERCY, mercyRuns);
        editor.apply();
    }

    @Override
    protected void nextInning() {
        gameOuts = 0;
        inningRuns = 0;
        emptyBases();
        setFinalInning();
        inningNumber++;
        chooseDisplay();
        setInningDisplay();
        inningChanged = 1;
    }

    @Override
    void setDisplays() {
        super.setDisplays();
        setLineupRVPosition();
    }

    private void chooseDisplay() {
        setFinalInning();
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
                    if(isHome) {
                        decreaseLineupIndex();
                    } else {
                        increaseLineupIndex();
                    }
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
        setScoreDisplay();
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

    protected void sendResultToMgr() {
        int myRuns;
        int theirRuns;
        Intent exitIntent = new Intent();

        if(isHome){
            exitIntent.putExtra(StatsEntry.COLUMN_HOME_TEAM, mSelectionID);
            exitIntent.putExtra(StatsEntry.COLUMN_AWAY_TEAM, StatsEntry.COLUMN_AWAY_TEAM);
            myRuns = homeTeamRuns;
            theirRuns = awayTeamRuns;
        } else {
            exitIntent.putExtra(StatsEntry.COLUMN_AWAY_TEAM, mSelectionID);
            exitIntent.putExtra(StatsEntry.COLUMN_HOME_TEAM, StatsEntry.COLUMN_HOME_TEAM);
            myRuns = awayTeamRuns;
            theirRuns = homeTeamRuns;
        }
        exitIntent.putExtra(StatsEntry.COLUMN_RUNSFOR, myRuns);
        exitIntent.putExtra(StatsEntry.COLUMN_RUNSAGAINST, theirRuns);
        setResult(RESULT_CODE_GAME_FINISHED, exitIntent);
        finish();
    }

    @Override
    protected boolean isTeamAlternate() {
        return isAlternate;
    }

    @Override
    protected boolean isLeagueGame() {
        return false;
    }

    @Override
    protected boolean isLeagueGameOrHomeTeam() {
        return isHome;
    }

    @Override
    protected void undoPlay() {
        String undoResult;
        if (gameLogIndex > lowestIndex) {
            undoResult = getUndoPlayResult();

            if (inningChanged == 1) {
                inningNumber--;
                chooseDisplay();
                setInningDisplay();
            }
            isAlternate = (StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_ONDECK) == null);
            gameLogIndex--;
            if(gameLogIndex == 0 && isHome) {
                increaseLineupIndex();
            }
        } else {
            return;
        }
        undoLogs();

        inningChanged = 0;

        resetBases(currentBaseLogStart);
        if (isAlternate) {
            if(!undoResult.equals(StatsEntry.COLUMN_SB)) {
                decreaseLineupIndex();
            }
            chooseDisplay();
            setInningDisplay();
        } else {
            isAlternate = (StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_ONDECK) == null);
            if (isAlternate) {
                chooseDisplay();
                setInningDisplay();
                return;
            }
            if(undoResult == null || !undoResult.equals(StatsEntry.COLUMN_SB)) {
                decreaseLineupIndex();
            }
        }
        updatePlayerStats(undoResult, -1);
        if(gameLogIndex != 0 || !isHome) {
            setDisplays();
        } else {
            setUndoRedo();
        }
    }

    @Override
    protected void redoPlay() {
        String redoResult = getRedoResult();
        if (redoResult == null) {
//            if(isHome && gameLogIndex == lowestIndex + 1) {
//                inningNumber++;
//                chooseDisplay();
//                setInningDisplay();
//                inningChanged = 1;
//                setDisplays();
//            }
            inningNumber++;
            chooseDisplay();
            setInningDisplay();
            inningChanged = 0;
            setDisplays();
            return;
        }

        if (inningChanged == 1) {
            inningNumber++;
            chooseDisplay();
            setInningDisplay();
            inningChanged = 0;
        }
        resetBases(currentBaseLogStart);
        isAlternate = (tempBatter == null);

        if (!isAlternate) {
            if(!redoResult.equals(StatsEntry.COLUMN_SB)) {
                increaseLineupIndex();
            }
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
    }

    @Override
    protected void decreaseLineupIndex() {
        myTeamIndex--;
        if (myTeamIndex < 0) {
            myTeamIndex = myTeam.size() - 1;
        }
    }

    @Override
    protected void checkLineupIndex() {
        if (myTeamIndex >= myTeam.size()) {
            myTeamIndex = myTeam.size() - 1;
        }
    }

    @Override
    protected void revertLineups() {
        String playerFirestoreID;
        if(currentBatter == null) {
            playerFirestoreID = myTeam.get(myTeamIndex).getFirestoreID();
            while (playerFirestoreID.equals(AUTO_OUT)) {
                increaseLineupIndex();
                playerFirestoreID = myTeam.get(myTeamIndex).getFirestoreID();
            }
        } else {
            playerFirestoreID = currentBatter.getFirestoreID();
            while (playerFirestoreID.equals(AUTO_OUT)) {
                increaseLineupIndex();
                playerFirestoreID = myTeam.get(myTeamIndex).getFirestoreID();
            }
        }

        myTeam.clear();

        String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mSelectionID, mSelectionID};
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, sortOrder);

        while (cursor.moveToNext()) {
            int order = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_ORDER);
            if (order > 100) {
                continue;
            }
            Player player = new Player(cursor, true);
            myTeam.add(player);
        }

        myTeamIndex = setLineupIndex(myTeam, playerFirestoreID);

        setLineupRVPosition();
    }

    @Override
    protected void actionEditLineup() {
        gotoLineupEditor(myTeamName, mSelectionID);
    }

    @Override
    protected void openEditWarningDialog() {
        if(isAlternate) {
            Toast.makeText(TeamGameActivity.this, "Can't edit lineup while other team is batting.", Toast.LENGTH_LONG).show();
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = new EditWarningDialog();
        newFragment.show(fragmentTransaction, "");
    }

    protected void gotoLineupEditor(String teamName, String teamID) {
        Intent editorIntent = new Intent(TeamGameActivity.this, SetLineupActivity.class);
        editorIntent.putExtra("ingame", true);
        editorIntent.putExtra("team_name", teamName);
        editorIntent.putExtra("team_id", teamID);
        startActivityForResult(editorIntent, REQUEST_CODE_EDIT);
    }

//    @Override
//    protected void inningJump(String playerResult) {
//        if((isHome && inningNumber % 2 == 0) || (!isHome && inningNumber % 2 == 1)) {
//            redoPlay();
//            return;
//        }
//        deleteGameLogs();
//        updatePlayerStats(playerResult, 1);
//        gameOuts = 3;
//        nextBatter();
//        lowestIndex = gameLogIndex;
//        setUndoRedo();
//        outsDisplay.setText("0 outs");
//        setDisplays();
//        chooseDisplay();
//    }

    private void setLineupRVPosition() {
        mLineupAdapter.setCurrentLineupPosition(myTeamIndex);
        mRecyclerView.scrollToPosition(myTeamIndex);
        mLineupAdapter.notifyDataSetChanged();
    }
}

