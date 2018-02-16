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
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.gamelog.BaseLog;

import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class GameActivity extends AppCompatActivity {

    protected Cursor gameCursor;

    protected TeamListAdapter awayTeamListAdapter;
    protected TeamListAdapter homeTeamListAdapter;
    protected RecyclerView awayLineupRV;
    protected RecyclerView homeLineupRV;

    protected TextView scoreboard;
    protected TextView nowBatting;
    protected TextView outsDisplay;
    protected TextView avgDisplay;
    protected TextView rbiDisplay;
    protected TextView runDisplay;
    protected TextView hrDisplay;
    protected TextView inningDisplay;
    protected ImageView inningTopArrow;
    protected ImageView inningBottomArrow;

    protected Button submitPlay;
    protected Button resetBases;

    protected RadioGroup group1;
    protected RadioGroup group2;
    protected String result;

    protected ImageView batterDisplay;
    protected TextView firstDisplay;
    protected TextView secondDisplay;
    protected TextView thirdDisplay;
    protected TextView homeDisplay;

    protected List<Player> awayTeam;
    protected List<Player> homeTeam;
    protected String awayTeamName;
    protected String homeTeamName;
    protected String awayTeamID;
    protected String homeTeamID;

    protected int awayTeamRuns;
    protected int homeTeamRuns;
    protected int awayTeamIndex;
    protected int homeTeamIndex;

    protected String tempBatter;
    protected int inningChanged = 0;

    protected int inningNumber = 2;
    protected int gameOuts = 0;
    protected int tempOuts;
    protected int tempRuns;

    protected Player currentBatter;
    protected List<Player> currentTeam;

    protected NumberFormat formatter = new DecimalFormat("#.000");
    protected BaseLog currentBaseLogStart;
    protected ArrayList<String> currentRunsLog;
    protected ArrayList<String> tempRunsLog;

    protected int gameLogIndex = 0;
    protected int highestIndex = 0;
    protected boolean undoRedo = false;

    protected boolean finalInning;
    protected boolean redoEndsGame = false;


    protected boolean playEntered = false;
    protected boolean batterMoved = false;
    protected boolean firstOccupied = false;
    protected boolean secondOccupied = false;
    protected boolean thirdOccupied = false;
    protected int totalInnings;

    protected static final String KEY_AWAYTEAM = "keyAwayTeam";
    protected static final String KEY_HOMETEAM = "keyHomeTeam";
    protected static final String KEY_GAMELOGINDEX = "keyGameLogIndex";
    protected static final String KEY_HIGHESTINDEX = "keyHighestIndex";
    protected static final String KEY_GENDERSORT = "keyGenderSort";
    protected static final String KEY_FEMALEORDER = "keyFemaleOrder";
    protected static final String KEY_INNINGNUMBER = "keyInningNumber";
    protected static final String KEY_TOTALINNINGS = "keyTotalInnings";
    protected static final String KEY_AWAYTEAMNDEX = "keyAwayTeamIndex";
    protected static final String KEY_HOMETEAMINDEX = "keyHomeTeamIndex";
    protected static final String KEY_UNDOREDO = "keyUndoRedo";
    protected static final String KEY_REDOENDSGAME = "redoEndsGame";
    protected static final String TAG = "LeagueGameActivity: ";

    protected String leagueID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getSelectionData();

        SharedPreferences gamePreferences = getSharedPreferences(leagueID + "game", MODE_PRIVATE);
        totalInnings = gamePreferences.getInt(KEY_TOTALINNINGS, 7);
        awayTeamID = gamePreferences.getString(KEY_AWAYTEAM, "x");
        awayTeamName = getTeamNameFromFirestoreID(awayTeamID);
        homeTeamID = gamePreferences.getString(KEY_HOMETEAM, "y");
        homeTeamName = getTeamNameFromFirestoreID(homeTeamID);
        int genderSorter = gamePreferences.getInt(KEY_FEMALEORDER, 0);
        int sortArgument = gamePreferences.getInt(KEY_GENDERSORT, 0);

        setTitle(awayTeamName + " @ " + homeTeamName);
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
        batterDisplay.setOnTouchListener(new GameActivity.MyTouchListener());
        firstDisplay.setOnDragListener(new GameActivity.MyDragListener());
        secondDisplay.setOnDragListener(new GameActivity.MyDragListener());
        thirdDisplay.setOnDragListener(new GameActivity.MyDragListener());
        homeDisplay.setOnDragListener(new GameActivity.MyDragListener());
        outTrash.setOnDragListener(new GameActivity.MyDragListener());

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

    private void getSelectionData() {
        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            leagueID = mainPageSelection.getId();
        } catch (Exception e) {
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
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

    private ArrayList<Player> setTeam(String teamID) {

        String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
        String[] selectionArgs = new String[]{teamID};
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, sortOrder);

        ArrayList<Player> team = new ArrayList<>();
        while (cursor.moveToNext()) {
            int order = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_ORDER);
            if (order > 100) {
                continue;
            }
            Player player = new Player(cursor, true);
            team.add(player);
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
                    result = StatsEntry.COLUMN_1B;
                group2.clearCheck();
                break;
            case R.id.dbl:
                if (checked)
                    result = StatsEntry.COLUMN_2B;
                group2.clearCheck();
                break;
            case R.id.triple:
                if (checked)
                    result = StatsEntry.COLUMN_3B;
                group2.clearCheck();
                break;
            case R.id.hr:
                if (checked)
                    result = StatsEntry.COLUMN_HR;
                group2.clearCheck();
                break;
            case R.id.bb:
                if (checked)
                    result = StatsEntry.COLUMN_BB;
                group1.clearCheck();
                break;
            case R.id.out:
                if (checked)
                    result = StatsEntry.COLUMN_OUT;
                group1.clearCheck();
                break;
            case R.id.error:
                if (checked)
                    result = StatsEntry.COLUMN_OUT;
                group1.clearCheck();
                break;
            case R.id.sf:
                if (checked)
                    result = StatsEntry.COLUMN_SF;
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
            firstDisplay.setOnDragListener(new GameActivity.MyDragListener());
            firstOccupied = false;
        } else {
            firstDisplay.setOnTouchListener(new GameActivity.MyTouchListener());
            firstDisplay.setOnDragListener(null);
            firstOccupied = true;
        }

        if (secondDisplay.getText().toString().isEmpty()) {
            secondDisplay.setOnTouchListener(null);
            secondDisplay.setOnDragListener(new GameActivity.MyDragListener());
            secondOccupied = false;
        } else {
            secondDisplay.setOnTouchListener(new GameActivity.MyTouchListener());
            secondDisplay.setOnDragListener(null);
            secondOccupied = true;
        }

        if (thirdDisplay.getText().toString().isEmpty()) {
            thirdDisplay.setOnTouchListener(null);
            thirdDisplay.setOnDragListener(new GameActivity.MyDragListener());
            thirdOccupied = false;
        } else {
            thirdDisplay.setOnTouchListener(new GameActivity.MyTouchListener());
            thirdDisplay.setOnDragListener(null);
            thirdOccupied = true;
        }
        homeDisplay.setOnDragListener(new GameActivity.MyDragListener());
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

    private void resumeGame() {
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
        String first = currentBaseLogEnd.getBasepositions()[0];
        String second = currentBaseLogEnd.getBasepositions()[1];
        String third = currentBaseLogEnd.getBasepositions()[2];

        String onDeck = currentBatter.getFirestoreID();
        ContentValues values = new ContentValues();
        values.put(StatsEntry.COLUMN_PLAY, result);
        values.put(StatsEntry.COLUMN_TEAM, team);
        values.put(StatsEntry.COLUMN_BATTER, previousBatterID);
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

        Intent finishGame = new Intent(GameActivity.this, LeagueManagerActivity.class);
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

    private Cursor getPlayerCursor(Uri uri, String player) {
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = {player};

        return getContentResolver().query(uri, null,
                selection, selectionArgs, null);
    }

    //sets the textview displays with updated player/game data
    private void setDisplays() {

        String playerFirestoreID = currentBatter.getFirestoreID();

        Cursor cursor = getPlayerCursor(StatsEntry.CONTENT_URI_TEMP, playerFirestoreID);
        cursor.moveToFirst();

        Player inGamePlayer = new Player(cursor, true);
        String name = inGamePlayer.getName();
        int tHR = inGamePlayer.getHrs();
        int tRBI = inGamePlayer.getRbis();
        int tRun = inGamePlayer.getRuns();
        int t1b = inGamePlayer.getSingles();
        int t2b = inGamePlayer.getDoubles();
        int t3b = inGamePlayer.getTriples();
        int tOuts = inGamePlayer.getOuts();
        cursor.close();

        cursor = getPlayerCursor(StatsEntry.CONTENT_URI_PLAYERS, playerFirestoreID);
        cursor.moveToFirst();

        Player overallPlayer = new Player(cursor, false);
        int pRBI = overallPlayer.getRbis();
        int pRun = overallPlayer.getRuns();
        int p1b = overallPlayer.getSingles();
        int p2b = overallPlayer.getDoubles();
        int p3b = overallPlayer.getTriples();
        int pHR = overallPlayer.getHrs();
        int pOuts = overallPlayer.getOuts();

        int displayHR = tHR + pHR;
        int displayRBI = tRBI + pRBI;
        int displayRun = tRun + pRun;
        int singles = t1b + p1b;
        int doubles = t2b + p2b;
        int triples = t3b + p3b;
        int playerOuts = tOuts + pOuts;
        double avg = calculateAverage(singles, doubles, triples, displayHR, playerOuts);

        String nowBattingString = getString(R.string.nowbatting) + " " + name;
        String avgDisplayText = "AVG: " + formatter.format(avg);
        String hrDisplayText = "HR: " + displayHR;
        String rbiDisplayText = "RBI: " + displayRBI;
        String runDisplayText = "R: " + displayRun;

        nowBatting.setText(nowBattingString);
        avgDisplay.setText(avgDisplayText);
        hrDisplay.setText(hrDisplayText);
        rbiDisplay.setText(rbiDisplayText);
        runDisplay.setText(runDisplayText);
        batterDisplay.setVisibility(View.VISIBLE);

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
        String playerFirestoreID;
        if (undoRedo) {
            playerFirestoreID = tempBatter;
        } else {
            playerFirestoreID = currentBatter.getFirestoreID();
        }
        Cursor cursor = getPlayerCursor(StatsEntry.CONTENT_URI_TEMP, playerFirestoreID);
        cursor.moveToFirst();
        ContentValues values = new ContentValues();
        int newValue;

        switch (action) {
            case StatsEntry.COLUMN_1B:
                newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_1B) + n;
                values.put(StatsEntry.COLUMN_1B, newValue);
                break;
            case StatsEntry.COLUMN_2B:
                newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_2B) + n;
                values.put(StatsEntry.COLUMN_2B, newValue);
                break;
            case StatsEntry.COLUMN_3B:
                newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_3B) + n;
                values.put(StatsEntry.COLUMN_3B, newValue);
                break;
            case StatsEntry.COLUMN_HR:
                newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HR) + n;
                values.put(StatsEntry.COLUMN_HR, newValue);
                break;
            case StatsEntry.COLUMN_BB:
                newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_BB) + n;
                values.put(StatsEntry.COLUMN_BB, newValue);
                break;
            case StatsEntry.COLUMN_SF:
                newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_SF) + n;
                values.put(StatsEntry.COLUMN_SF, newValue);
                break;
            case StatsEntry.COLUMN_OUT:
                newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_OUT) + n;
                values.put(StatsEntry.COLUMN_OUT, newValue);
                break;
            default:
                Log.e(TAG, "",
                        new Throwable("Wrong action entered!"));
                break;
        }

        int rbiCount = currentRunsLog.size();
        if (rbiCount > 0) {
            newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RBI) + (rbiCount * n);
            values.put(StatsEntry.COLUMN_RBI, newValue);
        }

        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        getContentResolver().update(StatsEntry.CONTENT_URI_TEMP, values, selection, new String[]{playerFirestoreID});

        if (rbiCount > 0) {
            for (String player : currentRunsLog) {
                updatePlayerRuns(player, n);
            }
        }
        cursor.close();
        setDisplays();
    }


    private void updatePlayerRuns(String player, int n) {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {player};
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, null
        );
        if (cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            int newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUN) + n;
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
        int id = StatsContract.getColumnInt(gameCursor, StatsEntry._ID);
        Uri toDelete = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_GAMELOG, id);
        getContentResolver().delete(toDelete, null, null);
        undoRedo = false;
        redoEndsGame = false;
    }

    private void undoPlay() {
        String undoResult;
        if (gameLogIndex > 0) {
            gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                    null, null, null);
            gameCursor.moveToPosition(gameLogIndex);
            undoRedo = true;
            tempBatter = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_BATTER);
            undoResult = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_PLAY);
            inningChanged = StatsContract.getColumnInt(gameCursor, StatsEntry.COLUMN_INNING_CHANGED);
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
        tempBatter = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_BATTER);
        String redoResult = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_PLAY);
        if (!tempRunsLog.isEmpty()) {
            tempRunsLog.clear();
        }
        inningChanged = StatsContract.getColumnInt(gameCursor, StatsEntry.COLUMN_INNING_CHANGED);
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
        String batterID = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_ONDECK);
        int teamChoice = StatsContract.getColumnInt(gameCursor, StatsEntry.COLUMN_TEAM);
        List<Player> teamLineup;
        if (teamChoice == 0) {
            teamLineup = awayTeam;
        } else if (teamChoice == 1) {
            teamLineup = homeTeam;
        } else {
            Log.e(TAG, "BaseLog", new Throwable("Error with reloading BaseLog!"));
            return;
        }
        Player batter = findBatterByID(batterID, teamLineup);
        currentBaseLogStart = new BaseLog(gameCursor, batter, teamLineup);
    }

    private Player findBatterByID(String batterID, List<Player> teamLineup) {
        Log.e(TAG, "findBatterByIDStart " + batterID);
        for (Player player : teamLineup) {
            if (player.getFirestoreID().equals(batterID)) {
                Log.d("xxx", "findBatterByID = " + player.getName());
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
        String run1 = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_RUN1);
        String run2 = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_RUN2);
        String run3 = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_RUN3);
        String run4 = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_RUN4);

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
        Intent editorIntent = new Intent(GameActivity.this, SetLineupActivity.class);
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
                Intent statsIntent = new Intent(GameActivity.this, BoxScoreActivity.class);
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
                Intent exitIntent = new Intent(GameActivity.this, LeagueManagerActivity.class);
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
        String[] selectionArgs = new String[]{firestoreID};
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                null, selection, selectionArgs, null);
        String name = null;
        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            name = cursor.getString(nameIndex);
        }
        cursor.close();
        return name;
    }
}