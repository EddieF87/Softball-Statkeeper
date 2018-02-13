/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.scorekeepdraft1.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.FirestoreHelper;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.TeamListAdapter;
import com.example.android.scorekeepdraft1.gamelog.BaseLog;

import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eddie
 */
public class LeagueGameActivity extends AppCompatActivity /*implements LoaderManager.LoaderCallbacks<Cursor>*/ {

    private Cursor playerCursor;
    private Cursor gameCursor;

    private TeamListAdapter awayTeamListAdapter;
    private TeamListAdapter homeTeamListAdapter;
    private RecyclerView awayLineupRV;
    private RecyclerView homeLineupRV;

    private TextView scoreboard;
    private TextView nowBatting;
    private TextView outsDisplay;
    private TextView avgDisplay;
    private TextView rbiDisplay;
    private TextView runDisplay;
    private TextView hrDisplay;
    private TextView inningDisplay;
    private ImageView inningTopArrow;
    private ImageView inningBottomArrow;

    private Button submitPlay;
    private Button resetBases;

    private RadioGroup group1;
    private RadioGroup group2;
    private String result;

    private ImageView batterDisplay;
    private TextView firstDisplay;
    private TextView secondDisplay;
    private TextView thirdDisplay;
    private TextView homeDisplay;

    private List<Player> awayTeam;
    private List<Player> homeTeam;
    private String awayTeamName;
    private String homeTeamName;
    private String awayTeamID;
    private String homeTeamID;

    private int awayTeamRuns;
    private int homeTeamRuns;
    private int awayTeamIndex;
    private int homeTeamIndex;

    private String tempBatter;
    private int inningChanged = 0;

    private int inningNumber = 2;
    private int gameOuts = 0;
    private int tempOuts;
    private int tempRuns;

    private Player currentBatter;
    private List<Player> currentTeam;

    private NumberFormat formatter = new DecimalFormat("#.000");
    private BaseLog currentBaseLogStart;
    private ArrayList<String> currentRunsLog;
    private ArrayList<String> tempRunsLog;

    private int gameLogIndex = 0;
    private int highestIndex = 0;
    private boolean undoRedo = false;

    private boolean finalInning;
    private boolean redoEndsGame = false;


    private boolean playEntered = false;
    private boolean batterMoved = false;
    private boolean firstOccupied = false;
    private boolean secondOccupied = false;
    private boolean thirdOccupied = false;

    private int playIndex;
    private int currentTeamIndex;
    private int prevBatterIndex;
    private int currentBatterIndex;
    private int firstIndex;
    private int secondIndex;
    private int thirdIndex;
    private int gameOutIndex;
    private int awayRunsIndex;
    private int homeRunsIndex;
    private int run1Index;
    private int run2Index;
    private int run3Index;
    private int run4Index;
    private int inningChangedIndex;
    private int idIndex;
    private int playerIdIndex;
    private int playerNameIndex;
    private int teamIndex;
    private int singleIndex;
    private int doubleIndex;
    private int tripleIndex;
    private int hrIndex;
    private int bbIndex;
    private int sfIndex;
    private int playerOutIndex;
    private int playerRunIndex;
    private int rbiIndex;
    private int totalInnings;

    private static final String KEY_AWAYTEAM = "keyAwayTeam";
    private static final String KEY_HOMETEAM = "keyHomeTeam";
    private static final String KEY_GAMELOGINDEX = "keyGameLogIndex";
    private static final String KEY_HIGHESTINDEX = "keyHighestIndex";
    private static final String KEY_GENDERSORT = "keyGenderSort";
    private static final String KEY_FEMALEORDER = "keyFemaleOrder";
    private static final String KEY_INNINGNUMBER = "keyInningNumber";
    private static final String KEY_TOTALINNINGS = "keyTotalInnings";
    private static final String KEY_AWAYTEAMNDEX = "keyAwayTeamIndex";
    private static final String KEY_HOMETEAMINDEX = "keyHomeTeamIndex";
    private static final String KEY_UNDOREDO = "keyUndoRedo";
    private static final String KEY_REDOENDSGAME = "redoEndsGame";
    private static final String TAG = "LeagueGameActivity: ";

    private String leagueID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            leagueID = mainPageSelection.getId();
        } catch (Exception e) {
            Intent intent = new Intent(LeagueGameActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        SharedPreferences gamePreferences = getSharedPreferences(leagueID + "game", MODE_PRIVATE);
        totalInnings = gamePreferences.getInt(KEY_TOTALINNINGS, 7);
        awayTeamID = gamePreferences.getString(KEY_AWAYTEAM, "x");
        awayTeamName = getTeamNameFromFirestoreID(awayTeamID);
        homeTeamID = gamePreferences.getString(KEY_HOMETEAM, "y");
        homeTeamName = getTeamNameFromFirestoreID(homeTeamID);
        int genderSorter = gamePreferences.getInt(KEY_FEMALEORDER, 0);
        int sortArgument = gamePreferences.getInt(KEY_GENDERSORT, 0);

        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                null, null, null);
        playerCursor.moveToFirst();
        getPlayerColumnIndexes();
        playerCursor.close();

        setTitle(awayTeamName + " @ " + homeTeamName);
//        awayTeam = setTeam(awayTeamName);
//        homeTeam = setTeam(homeTeamName);
        awayTeam = setTeam(awayTeamID);
        homeTeam = setTeam(homeTeamID);

        if (sortArgument != 0) {
            setGendersort(sortArgument, genderSorter + 1);
        }

        scoreboard = findViewById(R.id.scoreboard);
        nowBatting = findViewById(R.id.nowbatting);
        outsDisplay = findViewById(R.id.num_of_outs);
        avgDisplay = findViewById(R.id.avgdisplay);
        rbiDisplay = findViewById(R.id.rbidisplay);
        runDisplay = findViewById(R.id.rundisplay);
        hrDisplay = findViewById(R.id.hrdisplay);
        inningDisplay = findViewById(R.id.inning);
        inningTopArrow = findViewById(R.id.inning_top_arrow);
        inningBottomArrow = findViewById(R.id.inning_bottom_arrow);

        group1 = findViewById(R.id.group1);
        group2 = findViewById(R.id.group2);

        submitPlay = findViewById(R.id.submit);
        submitPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });
        submitPlay.setVisibility(View.INVISIBLE);

        resetBases = findViewById(R.id.reset);
        resetBases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetBases(currentBaseLogStart);
            }
        });
        resetBases.setVisibility(View.INVISIBLE);

        batterDisplay = findViewById(R.id.batter);
        firstDisplay = findViewById(R.id.first_display);
        secondDisplay = findViewById(R.id.second_display);
        thirdDisplay = findViewById(R.id.third_display);
        homeDisplay = findViewById(R.id.home_display);
        homeDisplay.bringToFront();
        ImageView outTrash = findViewById(R.id.trash);
        batterDisplay.setOnTouchListener(new MyTouchListener());
        firstDisplay.setOnDragListener(new MyDragListener());
        secondDisplay.setOnDragListener(new MyDragListener());
        thirdDisplay.setOnDragListener(new MyDragListener());
        homeDisplay.setOnDragListener(new MyDragListener());
        outTrash.setOnDragListener(new MyDragListener());

        awayLineupRV = findViewById(R.id.away_lineup);
        awayLineupRV.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        awayTeamListAdapter = new TeamListAdapter(awayTeam, this, genderSorter + 1);
        awayLineupRV.setAdapter(awayTeamListAdapter);

        homeLineupRV = findViewById(R.id.home_lineup);
        homeLineupRV.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        homeTeamListAdapter = new TeamListAdapter(homeTeam, this, genderSorter + 1);
        homeLineupRV.setAdapter(homeTeamListAdapter);

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

        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                null, null, null);
        if (gameCursor.moveToFirst()) {
            gamePreferences = getSharedPreferences(leagueID + "game", MODE_PRIVATE);
            gameLogIndex = gamePreferences.getInt(KEY_GAMELOGINDEX, 0);
            highestIndex = gamePreferences.getInt(KEY_HIGHESTINDEX, 0);
            inningNumber = gamePreferences.getInt(KEY_INNINGNUMBER, 2);
            totalInnings = gamePreferences.getInt(KEY_TOTALINNINGS, 7);
            awayTeamIndex = gamePreferences.getInt(KEY_AWAYTEAMNDEX, 0);
            homeTeamIndex = gamePreferences.getInt(KEY_HOMETEAMINDEX, 0);
            undoRedo = gamePreferences.getBoolean(KEY_UNDOREDO, false);
            redoEndsGame = gamePreferences.getBoolean(KEY_REDOENDSGAME, redoEndsGame);

            Bundle args = getIntent().getExtras();
            if (args != null) {
                if (args.getBoolean("edited") && undoRedo) {
                    deleteGameLogs();
                    highestIndex = gameLogIndex;
                    invalidateOptionsMenu();
                }
            }

            resumeGame();
            return;
        }
        setInningDisplay();
        finalInning = false;

        startGame();
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
                Log.e(TAG, "error with gendersort sortArgument",
                        new Throwable("error with gendersort"));
        }
    }

//    private ArrayList<Player> setTeam(String teamName) {
//
//        String selection = StatsEntry.COLUMN_TEAM + "=?";
//        String[] selectionArgs = new String[]{teamName};
//        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";
//        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
//                selection, selectionArgs, sortOrder);
//
//        int nameIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
//        int idIndex = playerCursor.getColumnIndex(StatsEntry._ID);
//        int orderIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_ORDER);
//        int genderIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_GENDER);
//        int firestoreIDIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
//
//        ArrayList<Player> team = new ArrayList<>();
//        while (playerCursor.moveToNext()) {
//            int order = playerCursor.getInt(orderIndex);
//            if (order > 100) {
//                continue;
//            }
//
//            int playerId = playerCursor.getInt(idIndex);
//            int gender = playerCursor.getInt(genderIndex);
//            String playerName = playerCursor.getString(nameIndex);
//            String firestoreID = playerCursor.getString(firestoreIDIndex);
//            team.add(new Player(playerName, teamName, gender, playerId, firestoreID));
//        }
//        return team;
//    }

    private ArrayList<Player> setTeam(String teamID) {

        String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
        String[] selectionArgs = new String[]{teamID};
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, sortOrder);

        int nameIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
        int idIndex = playerCursor.getColumnIndex(StatsEntry._ID);
        int orderIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_ORDER);
        int genderIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_GENDER);
        int firestoreIDIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
        int teamIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_TEAM);

        ArrayList<Player> team = new ArrayList<>();
        while (playerCursor.moveToNext()) {
            int order = playerCursor.getInt(orderIndex);
            if (order > 100) {
                continue;
            }

            int playerId = playerCursor.getInt(idIndex);
            int gender = playerCursor.getInt(genderIndex);
            String playerName = playerCursor.getString(nameIndex);
            String teamName = playerCursor.getString(teamIndex);
            String firestoreID = playerCursor.getString(firestoreIDIndex);
            team.add(new Player(playerName, teamName, gender, playerId, firestoreID, teamID));
        }
        return team;
    }

    private List<Player> genderSort(List<Player> team, int femaleRequired) {
        if (femaleRequired < 1) {
            return team;
        }

        List<Player> females = new ArrayList<>();
        List<Player> males = new ArrayList<>();
        int femaleIndex = 0;
        int maleIndex = 0;
        int firstFemale = 0;
        boolean firstFemaleSet = false;
        for (Player player : team) {
            if (player.getGender() == 1) {
                females.add(player);
                firstFemaleSet = true;
            } else {
                males.add(player);
            }
            if (!firstFemaleSet) {
                firstFemale++;
            }
        }
        if (females.isEmpty() || males.isEmpty()) {
            return team;
        }
        team.clear();
        if (firstFemale >= femaleRequired) {
            firstFemale = femaleRequired - 1;
        }
        for (int i = 0; i < firstFemale; i++) {
            team.add(males.get(maleIndex));
            maleIndex++;
            if (maleIndex >= males.size()) {
                maleIndex = 0;
            }
        }
        for (int i = 0; i < 100; i++) {
            if (i % femaleRequired == 0) {
                team.add(females.get(femaleIndex));
                femaleIndex++;
                if (femaleIndex >= females.size()) {
                    femaleIndex = 0;
                }
            } else {
                team.add(males.get(maleIndex));
                maleIndex++;
                if (maleIndex >= males.size()) {
                    maleIndex = 0;
                }
            }
        }
        return team;
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        playEntered = true;
        switch (view.getId()) {
            case R.id.single:
                if (checked)
                    result = "1b";
                group2.clearCheck();
                break;
            case R.id.dbl:
                if (checked)
                    result = "2b";
                group2.clearCheck();
                break;
            case R.id.triple:
                if (checked)
                    result = "3b";
                group2.clearCheck();
                break;
            case R.id.hr:
                if (checked)
                    result = "hr";
                group2.clearCheck();
                break;
            case R.id.bb:
                if (checked)
                    result = "bb";
                group1.clearCheck();
                break;
            case R.id.out:
                if (checked)
                    result = "out";
                group1.clearCheck();
                break;
            case R.id.error:
                if (checked)
                    result = "out";
                group1.clearCheck();
                break;
            case R.id.fc:
                if (checked)
                    result = "out";
                group1.clearCheck();
                break;
            case R.id.sf:
                if (checked)
                    result = "sf";
                group1.clearCheck();
                break;
        }
        if (batterMoved) {
            submitPlay.setVisibility(View.VISIBLE);
        }
    }

    public void setBaseListeners() {
        if (firstDisplay.getText().toString().isEmpty()) {
            firstDisplay.setOnTouchListener(null);
            firstDisplay.setOnDragListener(new MyDragListener());
            firstOccupied = false;
        } else {
            firstDisplay.setOnTouchListener(new MyTouchListener());
            firstDisplay.setOnDragListener(null);
            firstOccupied = true;
        }

        if (secondDisplay.getText().toString().isEmpty()) {
            secondDisplay.setOnTouchListener(null);
            secondDisplay.setOnDragListener(new MyDragListener());
            secondOccupied = false;
        } else {
            secondDisplay.setOnTouchListener(new MyTouchListener());
            secondDisplay.setOnDragListener(null);
            secondOccupied = true;
        }

        if (thirdDisplay.getText().toString().isEmpty()) {
            thirdDisplay.setOnTouchListener(null);
            thirdDisplay.setOnDragListener(new MyDragListener());
            thirdOccupied = false;
        } else {
            thirdDisplay.setOnTouchListener(new MyTouchListener());
            thirdDisplay.setOnDragListener(null);
            thirdOccupied = true;
        }
        homeDisplay.setOnDragListener(new MyDragListener());
    }

    private void startGame() {

        awayTeamRuns = 0;
        homeTeamRuns = 0;

        currentTeam = awayTeam;
        currentBatter = awayTeam.get(0);
        currentRunsLog = new ArrayList<>();
        tempRunsLog = new ArrayList<>();
        currentBaseLogStart = new BaseLog(currentTeam, currentBatter, "", "", "",
                0, 0, 0);

        ContentValues values = new ContentValues();
        String onDeck = currentBatter.getName();
        values.put(StatsEntry.COLUMN_ONDECK, onDeck);
        values.put(StatsEntry.COLUMN_TEAM, 0);
        values.put(StatsEntry.COLUMN_OUT, 0);
        values.put(StatsEntry.COLUMN_AWAY_RUNS, 0);
        values.put(StatsEntry.COLUMN_HOME_RUNS, 0);
        values.put(StatsEntry.COLUMN_PLAY, "start");
        values.put(StatsEntry.COLUMN_INNING_CHANGED, 0);
        values.put(StatsEntry.COLUMN_LOG_INDEX, gameLogIndex);
        getContentResolver().insert(StatsEntry.CONTENT_URI_GAMELOG, values);
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                null, null, null);
        getGameColumnIndexes();

        setLineupRVPosition(false);

        startCursor();
        setDisplays();
        String outsText = "0 outs";
        outsDisplay.setText(outsText);
    }

    private void resumeGame() {
        getGameColumnIndexes();
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
            awayTeamListAdapter.setCurrentLineupPosition(-1);
            setLineupRVPosition(true);
            if (homeTeamIndex >= currentTeam.size()) {
                homeTeamIndex = 0;
            }
            lineupIndex = homeTeamIndex;
        } else {
            homeTeamListAdapter.setCurrentLineupPosition(-1);
            setLineupRVPosition(false);
            if (awayTeamIndex >= currentTeam.size()) {
                awayTeamIndex = 0;
            }
            lineupIndex = awayTeamIndex;
        }

        if (currentBatter != currentTeam.get(lineupIndex)) {
            currentBatter = currentTeam.get(lineupIndex);
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_ONDECK, currentBatter.getName());
            ContentResolver contentResolver = getContentResolver();
            int gameID = gameCursor.getInt(idIndex);
            Uri gameURI = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_GAMELOG, gameID);
            contentResolver.update(gameURI, values, null, null);
        }
        tempBatter = gameCursor.getString(prevBatterIndex);
        if (tempRunsLog == null) {
            tempRunsLog = new ArrayList<>();
        } else {
            tempRunsLog.clear();
        }
        resetBases(currentBaseLogStart);
        startCursor();
        setDisplays();
        setInningDisplay();
    }


    private void nextBatter() {
        if (currentTeam == homeTeam && finalInning && homeTeamRuns > awayTeamRuns) {
            increaseLineupIndex();
            showFinishGameDialog();
            return;
        }
        if (gameOuts >= 3) {
            if (currentTeam == homeTeam && finalInning && awayTeamRuns > homeTeamRuns) {
                showFinishGameDialog();
                return;
            } else {
                nextInning();
            }
        } else {
            increaseLineupIndex();
        }
        updateGameLogs();
        submitPlay.setEnabled(true);
    }

    private void updateGameLogs() {
        String previousBatterName = currentBatter.getName();
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
        String first = currentBaseLogEnd.getBasepositions()[0];
        String second = currentBaseLogEnd.getBasepositions()[1];
        String third = currentBaseLogEnd.getBasepositions()[2];

        String onDeck = currentBatter.getName();
        ContentValues values = new ContentValues();
        values.put(StatsEntry.COLUMN_PLAY, result);
        values.put(StatsEntry.COLUMN_TEAM, team);
        values.put(StatsEntry.COLUMN_BATTER, previousBatterName);
        values.put(StatsEntry.COLUMN_ONDECK, onDeck);
        values.put(StatsEntry.COLUMN_1B, first);
        values.put(StatsEntry.COLUMN_2B, second);
        values.put(StatsEntry.COLUMN_3B, third);
        values.put(StatsEntry.COLUMN_OUT, gameOuts);
        values.put(StatsEntry.COLUMN_AWAY_RUNS, awayTeamRuns);
        values.put(StatsEntry.COLUMN_HOME_RUNS, homeTeamRuns);
        values.put(StatsEntry.COLUMN_INNING_CHANGED, inningChanged);
        values.put(StatsEntry.COLUMN_LOG_INDEX, gameLogIndex);
        for (int i = 0; i < currentRunsLog.size(); i++) {
            String player = currentRunsLog.get(i);
            switch (i) {
                case 0:
                    values.put(StatsEntry.COLUMN_RUN1, player);
                    break;
                case 1:
                    values.put(StatsEntry.COLUMN_RUN2, player);
                    break;
                case 2:
                    values.put(StatsEntry.COLUMN_RUN3, player);
                    break;
                case 3:
                    values.put(StatsEntry.COLUMN_RUN4, player);
                    break;
                default:
                    break;
            }
        }
        getContentResolver().insert(StatsEntry.CONTENT_URI_GAMELOG, values);

        saveGameState();

        startCursor();
        setDisplays();
        group1.clearCheck();
        group2.clearCheck();
        tempRunsLog.clear();
        currentRunsLog.clear();
        submitPlay.setVisibility(View.INVISIBLE);
        resetBases.setVisibility(View.INVISIBLE);
        tempOuts = 0;
        tempRuns = 0;
        playEntered = false;
        batterMoved = false;
        inningChanged = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveGameState();
    }

    private void saveGameState() {
        SharedPreferences gamePreferences = getSharedPreferences(leagueID + "game", MODE_PRIVATE);
        SharedPreferences.Editor editor = gamePreferences.edit();
        editor.putInt(KEY_GAMELOGINDEX, gameLogIndex);
        editor.putInt(KEY_HIGHESTINDEX, highestIndex);
        editor.putInt(KEY_INNINGNUMBER, inningNumber);
        editor.putInt(KEY_AWAYTEAMNDEX, awayTeamIndex);
        editor.putInt(KEY_HOMETEAMINDEX, homeTeamIndex);
        editor.putBoolean(KEY_UNDOREDO, undoRedo);
        editor.putBoolean(KEY_REDOENDSGAME, redoEndsGame);
        editor.commit();
    }

    private void nextInning() {
        gameOuts = 0;
        emptyBases();

        if (inningNumber / 2 >= totalInnings) {
            finalInning = true;
        }
        increaseLineupIndex();
        if (currentTeam == awayTeam) {
            if (finalInning && homeTeamRuns > awayTeamRuns) {
                showFinishGameDialog();
                return;
            }
            currentTeam = homeTeam;
            awayTeamListAdapter.setCurrentLineupPosition(-1);
            setLineupRVPosition(true);
        } else {
            currentTeam = awayTeam;
            homeTeamListAdapter.setCurrentLineupPosition(-1);
            setLineupRVPosition(false);
        }
        inningNumber++;

        setInningDisplay();
        inningChanged = 1;
    }

    private void endGame() {
        FirestoreHelper firestoreHelper = new FirestoreHelper(this, leagueID);
        firestoreHelper.addTeamStatsToDB(homeTeamID, homeTeamRuns, awayTeamRuns);
        firestoreHelper.addTeamStatsToDB(awayTeamID, awayTeamRuns, homeTeamRuns);
        firestoreHelper.addPlayerStatsToDB();
        firestoreHelper.updateTimeStamps();

        getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);

        Intent finishGame = new Intent(LeagueGameActivity.this, LeagueManagerActivity.class);
        startActivity(finishGame);
        finish();
    }

    private void showFinishGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.end_game_msg);
        builder.setPositiveButton(R.string.end_msg, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                endGame();
            }
        });
        builder.setNegativeButton(R.string.undo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!redoEndsGame) {
                    updateGameLogs();
                    redoEndsGame = true;
                }
                undoPlay();
                submitPlay.setEnabled(true);
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void startCursor() {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String currentBatterString = currentBatter.getName();
        String[] selectionArgs = {currentBatterString};
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, null);
        playerCursor.moveToFirst();
    }

    //sets the textview displays with updated player/game data
    private void setDisplays() {
        batterDisplay.setVisibility(View.VISIBLE);

        String name = playerCursor.getString(playerNameIndex);

        int tHR = playerCursor.getInt(hrIndex);
        int tRBI = playerCursor.getInt(rbiIndex);
        int tRun = playerCursor.getInt(playerRunIndex);
        int t1b = playerCursor.getInt(singleIndex);
        int t2b = playerCursor.getInt(doubleIndex);
        int t3b = playerCursor.getInt(tripleIndex);
        int tOuts = playerCursor.getInt(playerOutIndex);

        String selection = StatsEntry.COLUMN_NAME + "=?";
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, null,
                selection, new String[]{name}, null);
        playerCursor.moveToFirst();

        int pRBIIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RBI);
        int pRunIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RUN);
        int p1bIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_1B);
        int p2bIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_2B);
        int p3bIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_3B);
        int pHRIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_HR);
        int pOutsIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_OUT);

        int pRBI = playerCursor.getInt(pRBIIndex);
        int pRun = playerCursor.getInt(pRunIndex);
        int p1b = playerCursor.getInt(p1bIndex);
        int p2b = playerCursor.getInt(p2bIndex);
        int p3b = playerCursor.getInt(p3bIndex);
        int pHR = playerCursor.getInt(pHRIndex);
        int pOuts = playerCursor.getInt(pOutsIndex);

        int displayHR = tHR + pHR;
        int displayRBI = tRBI + pRBI;
        int displayRun = tRun + pRun;
        int singles = t1b + p1b;
        int doubles = t2b + p2b;
        int triples = t3b + p3b;
        int playerOuts = tOuts + pOuts;
        double avg = calculateAverage(singles, doubles, triples, displayHR, playerOuts);

        String nowBattingString = getString(R.string.nowbatting) + " " + name;
        nowBatting.setText(nowBattingString);
        String avgDisplayText = "AVG: " + formatter.format(avg);
        String hrDisplayText = "HR: " + displayHR;
        String rbiDisplayText = "RBI: " + displayRBI;
        String runDisplayText = "R: " + displayRun;

        avgDisplay.setText(avgDisplayText);
        hrDisplay.setText(hrDisplayText);
        rbiDisplay.setText(rbiDisplayText);
        runDisplay.setText(runDisplayText);

        setScoreDisplay();
    }

    private void setScoreDisplay() {
        String scoreString = awayTeamName + " " + awayTeamRuns + "    " + homeTeamName + " " + homeTeamRuns;
        scoreboard.setText(scoreString);
    }

    private void setInningDisplay() {
        String topOrBottom;
        if (inningNumber % 2 == 0) {
            inningTopArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.color_arrow));
            inningBottomArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cardview_dark_background));
            topOrBottom = "Top";
        } else {
            inningTopArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cardview_dark_background));
            inningBottomArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.color_arrow));
            topOrBottom = "Bottom";
        }
        inningDisplay.setText(String.valueOf(inningNumber / 2));

        String indicator;
        switch (inningNumber / 2) {
            case 1:
                indicator = "st";
                break;
            case 2:
                indicator = "nd";
                break;
            case 3:
                indicator = "rd";
                break;
            default:
                indicator = "th";
        }
        //Toast.makeText(LeagueGameActivity.this, topOrBottom + " of the " + inningNumber / 2 + indicator, Toast.LENGTH_LONG).show();
    }

    private void updatePlayerStats(String action, int n) {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs;
        String currentBatterString = currentBatter.getName();
        if (undoRedo) {
            selectionArgs = new String[]{tempBatter};
        } else {
            selectionArgs = new String[]{currentBatterString};
        }
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, null);
        playerCursor.moveToFirst();
        ContentValues values = new ContentValues();
        int newValue;

        switch (action) {
            case "1b":
                newValue = playerCursor.getInt(singleIndex) + n;
                values.put(StatsEntry.COLUMN_1B, newValue);
                break;
            case "2b":
                newValue = playerCursor.getInt(doubleIndex) + n;
                values.put(StatsEntry.COLUMN_2B, newValue);
                break;
            case "3b":
                newValue = playerCursor.getInt(tripleIndex) + n;
                values.put(StatsEntry.COLUMN_3B, newValue);
                break;
            case "hr":
                newValue = playerCursor.getInt(hrIndex) + n;
                values.put(StatsEntry.COLUMN_HR, newValue);
                break;
            case "bb":
                newValue = playerCursor.getInt(bbIndex) + n;
                values.put(StatsEntry.COLUMN_BB, newValue);
                break;
            case "sf":
                newValue = playerCursor.getInt(sfIndex) + n;
                values.put(StatsEntry.COLUMN_SF, newValue);
                break;
            case "out":
                newValue = playerCursor.getInt(playerOutIndex) + n;
                values.put(StatsEntry.COLUMN_OUT, newValue);
                break;
            default:
                Log.e(TAG, "",
                        new Throwable("Wrong action entered!"));
                break;
        }

        int rbiCount = currentRunsLog.size();
        if (rbiCount > 0) {
            newValue = playerCursor.getInt(rbiIndex) + (rbiCount * n);
            values.put(StatsEntry.COLUMN_RBI, newValue);
        }

        getContentResolver().update(StatsEntry.CONTENT_URI_TEMP, values, selection, selectionArgs);

        if (rbiCount > 0) {
            for (String player : currentRunsLog) {
                updatePlayerRuns(player, n);
            }
        }
        startCursor();
        setDisplays();
    }

    private void updatePlayerRuns(String player, int n) {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {player};
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, null
        );
        if (playerCursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            int newValue = playerCursor.getInt(playerRunIndex) + n;
            values.put(StatsEntry.COLUMN_RUN, newValue);
            getContentResolver().update(StatsEntry.CONTENT_URI_TEMP, values, selection, selectionArgs);
        } else {
            Log.e(TAG, "", new Throwable("Error with updating player runs."));
        }
        if (!undoRedo) {
            if (currentTeam == homeTeam) {
                homeTeamRuns++;
            } else {
                awayTeamRuns++;
            }
        }
    }

    private void resetBases(BaseLog baseLog) {
        String[] bases = baseLog.getBasepositions();
        firstDisplay.setText(bases[0]);
        secondDisplay.setText(bases[1]);
        thirdDisplay.setText(bases[2]);
        batterDisplay.setVisibility(View.VISIBLE);
        batterMoved = false;
        if (!undoRedo) {
            currentRunsLog.clear();
        }
        if (!tempRunsLog.isEmpty()) {
            tempRunsLog.clear();
        }
        submitPlay.setVisibility(View.INVISIBLE);
        resetBases.setVisibility(View.INVISIBLE);
        setBaseListeners();
        tempOuts = 0;
        tempRuns = 0;
        String outs = gameOuts + " outs";
        outsDisplay.setText(outs);
        setScoreDisplay();
    }

    private void emptyBases() {
        firstDisplay.setText("");
        secondDisplay.setText("");
        thirdDisplay.setText("");
    }

    private double calculateAverage(int singles, int doubles, int triples, int hrs, int outs) {
        double hits = (double) (singles + doubles + triples + hrs);
        return (hits / (outs + hits));
    }

    private void onSubmit() {
        if (undoRedo) {
            deleteGameLogs();
            currentRunsLog.clear();
            currentRunsLog.addAll(tempRunsLog);
        }
        submitPlay.setEnabled(false);
        updatePlayerStats(result, 1);
        gameOuts += tempOuts;
        nextBatter();
        String outs = gameOuts + " outs";
        outsDisplay.setText(outs);
    }

    private void deleteGameLogs() {
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                null, null, null);
        gameCursor.moveToPosition(gameLogIndex);
        int id = gameCursor.getInt(idIndex);
        Uri toDelete = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_GAMELOG, id);
        getContentResolver().delete(toDelete, null, null);
        undoRedo = false;
        redoEndsGame = false;
    }

    public void getGameColumnIndexes() {
        idIndex = gameCursor.getColumnIndex(StatsEntry._ID);
        playIndex = gameCursor.getColumnIndex(StatsEntry.COLUMN_PLAY);
        currentTeamIndex = gameCursor.getColumnIndex(StatsEntry.COLUMN_TEAM);
        prevBatterIndex = gameCursor.getColumnIndex(StatsEntry.COLUMN_BATTER);
        currentBatterIndex = gameCursor.getColumnIndex(StatsEntry.COLUMN_ONDECK);
        firstIndex = gameCursor.getColumnIndex(StatsEntry.COLUMN_1B);
        secondIndex = gameCursor.getColumnIndex(StatsEntry.COLUMN_2B);
        thirdIndex = gameCursor.getColumnIndex(StatsEntry.COLUMN_3B);
        gameOutIndex = gameCursor.getColumnIndex(StatsEntry.COLUMN_OUT);
        awayRunsIndex = gameCursor.getColumnIndex(StatsEntry.COLUMN_AWAY_RUNS);
        homeRunsIndex = gameCursor.getColumnIndex(StatsEntry.COLUMN_HOME_RUNS);
        run1Index = gameCursor.getColumnIndex(StatsEntry.COLUMN_RUN1);
        run2Index = gameCursor.getColumnIndex(StatsEntry.COLUMN_RUN2);
        run3Index = gameCursor.getColumnIndex(StatsEntry.COLUMN_RUN3);
        run4Index = gameCursor.getColumnIndex(StatsEntry.COLUMN_RUN4);
        inningChangedIndex = gameCursor.getColumnIndex(StatsEntry.COLUMN_INNING_CHANGED);
    }

    public void getPlayerColumnIndexes() {
        idIndex = playerCursor.getColumnIndex(StatsEntry._ID);
        playerIdIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_PLAYERID);
        playerNameIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
        teamIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_TEAM);
        singleIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_1B);
        doubleIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_2B);
        tripleIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_3B);
        hrIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_HR);
        bbIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_BB);
        sfIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_SF);
        playerOutIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_OUT);
        playerRunIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RUN);
        rbiIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RBI);
    }

    private void undoPlay() {
        String undoResult;
        if (gameLogIndex > 0) {
            gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                    null, null, null);
            gameCursor.moveToPosition(gameLogIndex);
            undoRedo = true;
            tempBatter = gameCursor.getString(prevBatterIndex);
            undoResult = gameCursor.getString(playIndex);
            inningChanged = gameCursor.getInt(inningChangedIndex);
            if (inningChanged == 1) {
                inningNumber--;
                setInningDisplay();
                if (currentTeam == awayTeam) {
                    awayTeamListAdapter.setCurrentLineupPosition(-1);
                    awayTeamListAdapter.notifyDataSetChanged();
                } else if (currentTeam == homeTeam) {

                    homeTeamListAdapter.setCurrentLineupPosition(-1);
                    homeTeamListAdapter.notifyDataSetChanged();
                }
            }
            gameLogIndex--;
        } else {
            Log.v(TAG, "This is the beginning of the game!");
            return;
        }
        reloadRunsLog();
        gameCursor.moveToPrevious();
        reloadBaseLog();
        //undoRuns
        awayTeamRuns = currentBaseLogStart.getAwayTeamRuns();
        homeTeamRuns = currentBaseLogStart.getHomeTeamRuns();
        if (!tempRunsLog.isEmpty()) {
            tempRunsLog.clear();
        }

        gameOuts = currentBaseLogStart.getOutCount();
        currentTeam = currentBaseLogStart.getTeam();
        currentBatter = currentBaseLogStart.getBatter();

        if (currentTeam == awayTeam) {
            decreaseAwayIndex();
        } else if (currentTeam == homeTeam) {
            decreaseHomeIndex();
        }
        inningChanged = 0;

        resetBases(currentBaseLogStart);
        updatePlayerStats(undoResult, -1);
    }

    private void redoPlay() {
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                null, null, null);

        if (gameLogIndex < gameCursor.getCount() - 1) {
            undoRedo = true;
            gameLogIndex++;
        } else {
            return;
        }
        gameCursor.moveToPosition(gameLogIndex);

        reloadRunsLog();
        reloadBaseLog();
        awayTeamRuns = currentBaseLogStart.getAwayTeamRuns();
        homeTeamRuns = currentBaseLogStart.getHomeTeamRuns();
        gameOuts = currentBaseLogStart.getOutCount();
        currentTeam = currentBaseLogStart.getTeam();
        currentBatter = currentBaseLogStart.getBatter();
        tempBatter = gameCursor.getString(prevBatterIndex);
        String redoResult = gameCursor.getString(playIndex);
        if (!tempRunsLog.isEmpty()) {
            tempRunsLog.clear();
        }
        inningChanged = gameCursor.getInt(inningChangedIndex);
        if (inningChanged == 1) {
            inningNumber++;
            if (currentTeam == awayTeam) {
                increaseHomeIndex();
                setLineupRVPosition(false);
                homeTeamListAdapter.setCurrentLineupPosition(-1);
                homeTeamListAdapter.notifyDataSetChanged();
            } else if (currentTeam == homeTeam) {
                increaseAwayIndex();
                setLineupRVPosition(true);
                awayTeamListAdapter.setCurrentLineupPosition(-1);
                awayTeamListAdapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "inningChanged", new Throwable("inningChanged logic error!"));
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

    private void reloadBaseLog() {
        int outs = gameCursor.getInt(gameOutIndex);
        int awayruns = gameCursor.getInt(awayRunsIndex);
        int homeruns = gameCursor.getInt(homeRunsIndex);
        String batterName = gameCursor.getString(currentBatterIndex);
        int team = gameCursor.getInt(currentTeamIndex);
        String first = gameCursor.getString(firstIndex);
        String second = gameCursor.getString(secondIndex);
        String third = gameCursor.getString(thirdIndex);
        List<Player> teamLineup;
        if (team == 0) {
            teamLineup = awayTeam;
        } else if (team == 1) {
            teamLineup = homeTeam;
        } else {
            Log.e(TAG, "BaseLog", new Throwable("Error with reloading BaseLog!"));
            return;
        }
        Player batter = findBatterByName(batterName, teamLineup);
        currentBaseLogStart = new BaseLog(teamLineup, batter, first, second, third, outs, awayruns, homeruns);
    }

    private Player findBatterByName(String batterName, List<Player> teamLineup) {
        Log.e(TAG, "findBatterByNameStart " + batterName);
        for (Player player : teamLineup) {
            if (player.getName().equals(batterName)) {
                Log.d("xxx", "findBatterByName " + player.getName());
                return player;
            }
        }
        Log.e(TAG, "findBatterByName ", new Throwable("Error with finding batter!"));
        return null;
    }

    private void reloadRunsLog() {
        if (currentRunsLog == null) {
            currentRunsLog = new ArrayList<>();
        }
        currentRunsLog.clear();
        String run1 = gameCursor.getString(run1Index);
        String run2 = gameCursor.getString(run2Index);
        String run3 = gameCursor.getString(run3Index);
        String run4 = gameCursor.getString(run4Index);

        if (run1 == null) {
            return;
        }
        currentRunsLog.add(run1);

        if (run2 == null) {
            return;
        }
        currentRunsLog.add(run2);

        if (run3 == null) {
            return;
        }
        currentRunsLog.add(run3);

        if (run4 == null) {
            return;
        }
        currentRunsLog.add(run4);
    }

    private class MyDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, final DragEvent event) {
            int action = event.getAction();
            TextView dropPoint = null;
            if (v.getId() != R.id.trash) {
                dropPoint = (TextView) v;
            }
            final View eventView = (View) event.getLocalState();

            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (v.getId() == R.id.home_display) {
                            v.setBackground(getDrawable(R.drawable.img_home2));
                        } else if (v.getId() == R.id.trash) {
                            //todo
                            v.setBackground(getDrawable(R.drawable.img_base2));
                        } else {
                            v.setBackground(getDrawable(R.drawable.img_base2));
                        }
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
//                    v.setBackgroundColor(Color.TRANSPARENT);
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    break;
                case DragEvent.ACTION_DROP:
                    String movedPlayer = "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (v.getId() == R.id.home_display) {
                            v.setBackground(getDrawable(R.drawable.img_home));
                        } else if (v.getId() == R.id.trash) {
                            v.setBackgroundResource(0);
                        } else {
                            v.setBackground(getDrawable(R.drawable.img_base));
                        }
                    }
                    if (v.getId() == R.id.trash) {
                        if (eventView instanceof TextView) {
                            TextView draggedView = (TextView) eventView;
                            draggedView.setText("");
                        } else {
                            batterDisplay.setVisibility(View.INVISIBLE);
                            batterMoved = true;
                            if (playEntered) {
                                submitPlay.setVisibility(View.VISIBLE);
                            }
                        }
                        tempOuts++;
                        String sumOuts = gameOuts + tempOuts + " outs";
                        outsDisplay.setText(sumOuts);
                    } else {
                        if (eventView instanceof TextView) {
                            TextView draggedView = (TextView) eventView;
                            movedPlayer = draggedView.getText().toString();
                            dropPoint.setText(movedPlayer);
                            draggedView.setText("");
                            draggedView.setAlpha(1);
                        } else {
                            String currentBatterString = currentBatter.getName();
                            dropPoint.setText(currentBatterString);
                            batterDisplay.setVisibility(View.INVISIBLE);
                            batterMoved = true;
                            if (playEntered) {
                                submitPlay.setVisibility(View.VISIBLE);
                            }
                        }
                        dropPoint.setAlpha(1);
                    }
                    if (dropPoint == homeDisplay) {
                        homeDisplay.bringToFront();
                        if (eventView instanceof TextView) {
                            if (undoRedo) {
                                tempRunsLog.add(movedPlayer);
                            } else {
                                currentRunsLog.add(movedPlayer);
                            }
                        } else {
                            String currentBatterString = currentBatter.getName();
                            if (undoRedo) {
                                tempRunsLog.add(currentBatterString);
                            } else {
                                currentRunsLog.add(currentBatterString);
                            }
                        }
                        homeDisplay.setText("");
                        tempRuns++;
                        String scoreString;
                        if (currentTeam == awayTeam) {
                            scoreString = awayTeamName + " " + (awayTeamRuns + tempRuns) + "    "
                                    + homeTeamName + " " + homeTeamRuns;
                        } else {
                            scoreString = awayTeamName + " " + awayTeamRuns + "    "
                                    + homeTeamName + " " + (homeTeamRuns + tempRuns);
                        }
                        scoreboard.setText(scoreString);
                    }
                    resetBases.setVisibility(View.VISIBLE);
                    setBaseListeners();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (v.getId() == R.id.home_display) {
                            v.setBackground(getDrawable(R.drawable.img_home));
                        } else if (v.getId() == R.id.trash) {
                            v.setBackgroundResource(0);
                        } else {
                            v.setBackground(getDrawable(R.drawable.img_base));
                        }
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    private final class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                setBaseListeners();
                switch (view.getId()) {
                    case R.id.batter:
                        if (firstOccupied) {
                            secondDisplay.setOnDragListener(null);
                            thirdDisplay.setOnDragListener(null);
                            homeDisplay.setOnDragListener(null);
                        } else if (secondOccupied) {
                            thirdDisplay.setOnDragListener(null);
                            homeDisplay.setOnDragListener(null);
                        } else if (thirdOccupied) {
                            homeDisplay.setOnDragListener(null);
                        }
                        break;
                    case R.id.first_display:
                        if (secondOccupied) {
                            thirdDisplay.setOnDragListener(null);
                            homeDisplay.setOnDragListener(null);
                        } else if (thirdOccupied) {
                            homeDisplay.setOnDragListener(null);
                        }
                        break;
                    case R.id.second_display:
                        firstDisplay.setOnDragListener(null);
                        if (thirdOccupied) {
                            homeDisplay.setOnDragListener(null);
                        }
                        break;
                    case R.id.third_display:
                        firstDisplay.setOnDragListener(null);
                        secondDisplay.setOnDragListener(null);
                        break;
                    default:
                        Log.e(TAG, "MyTouchListener",
                                new Throwable("SOMETHING WENT WRONG WITH THE SWITCH"));
                        break;
                }

                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view.startDragAndDrop(data, shadowBuilder, view, 0);
                } else {
                    view.startDrag(data, shadowBuilder, view, 0);
                }
            }
            view.performClick();
            return true;
        }
    }

    private void increaseLineupIndex() {
        if (currentTeam == awayTeam) {
            increaseAwayIndex();
        } else if (currentTeam == homeTeam) {
            increaseHomeIndex();
        } else {
            Log.e(TAG, "",
                    new Throwable("SOMETHING WENT WRONG WITH THE INDEXES!"));
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
        } else {
            Log.e(TAG, "",
                    new Throwable("SOMETHING WENT WRONG WITH THE INDEXES!"));
        }
        return 0;
    }

    private void setIndex(Player player) {
        if (currentTeam == awayTeam) {
            awayTeamIndex = awayTeam.indexOf(player);
            setLineupRVPosition(false);
        } else if (currentTeam == homeTeam) {
            homeTeamIndex = homeTeam.indexOf(player);
            setLineupRVPosition(true);
        } else {
            Log.e(TAG, "",
                    new Throwable("SOMETHING WENT WRONG WITH THE INDEXES!"));
        }
    }

    private void setLineupRVPosition(boolean home) {
        if (home) {
            homeTeamListAdapter.setCurrentLineupPosition(homeTeamIndex);
            homeLineupRV.scrollToPosition(homeTeamIndex);
            homeTeamListAdapter.notifyDataSetChanged();
        } else {
            awayTeamListAdapter.setCurrentLineupPosition(awayTeamIndex);
            awayLineupRV.scrollToPosition(awayTeamIndex);
            awayTeamListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    public void gotoLineupEditor(String teamName, String teamID) {
        Intent editorIntent = new Intent(LeagueGameActivity.this, SetLineupActivity.class);
        editorIntent.putExtra("ingame", true);
        editorIntent.putExtra("team_name", teamName);
        editorIntent.putExtra("team_id", teamID);
        startActivity(editorIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_undo_play:
                undoPlay();
                break;
            case R.id.action_redo_play:
                redoPlay();
                break;
            case R.id.action_edit_lineup:
                chooseTeamToEditDialog();
                break;
            case R.id.action_goto_stats:
                Intent statsIntent = new Intent(LeagueGameActivity.this, BoxScoreActivity.class);
                Bundle b = new Bundle();
                //todo convert to teamid
                b.putString("awayTeam", awayTeamName);
                b.putString("homeTeam", homeTeamName);
                b.putInt("totalInnings", totalInnings);
                b.putInt("awayTeamRuns", awayTeamRuns);
                b.putInt("homeTeamRuns", homeTeamRuns);
                statsIntent.putExtras(b);
                startActivity(statsIntent);
                break;
            case R.id.action_exit_game:
                Intent exitIntent = new Intent(LeagueGameActivity.this, LeagueManagerActivity.class);
                startActivity(exitIntent);
                finish();
                break;
            case R.id.action_finish_game:
                showFinishConfirmationDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem undoItem = menu.findItem(R.id.action_undo_play);
        MenuItem finishItem = menu.findItem(R.id.action_finish_game);
        MenuItem redoItem = menu.findItem(R.id.action_redo_play);

        if (gameLogIndex <= 0) {
            undoItem.setVisible(false);
        } else {
            undoItem.setVisible(true);
        }

        if (gameLogIndex >= highestIndex) {
            redoItem.setVisible(false);
            finishItem.setVisible(true);
        } else {
            redoItem.setVisible(true);
            finishItem.setVisible(false);
        }
        return true;
    }

    private void showFinishConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Complete game and update stats?");
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                endGame();
                finish();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

    private String getTeamNameFromFirestoreID(String firestoreID) {
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = new String[] {firestoreID};
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                null, selection, selectionArgs, null);
        String name = null;
        if(cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            name = cursor.getString(nameIndex);
        }
        cursor.close();
        return name;
    }
}