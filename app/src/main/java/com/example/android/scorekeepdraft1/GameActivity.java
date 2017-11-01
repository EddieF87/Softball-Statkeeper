/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.scorekeepdraft1;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

import com.example.android.scorekeepdraft1.gamelog.BaseLog;

import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eddie
 */
public class GameActivity extends AppCompatActivity /*implements LoaderManager.LoaderCallbacks<Cursor>*/ {

    private Cursor playerCursor;
    private Cursor gameCursor;
    private FirebaseFirestore mFirestore;

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
    private int nameIndex;
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
    private int totalInnings = 9;

    private static final String KEY_GAMELOGINDEX = "keyGameLogIndex";
    private static final String KEY_HIGHESTINDEX = "keyHighestIndex";
    private static final String KEY_INNINGNUMBER = "keyInningNumber";
    private static final String KEY_AWAYTEAMNDEX = "keyAwayTeamIndex";
    private static final String KEY_HOMETEAMINDEX = "keyHomeTeamIndex";
    private static final String KEY_UNDOREDO = "keyUndoRedo";
    private static final String TAG = "GameActivity: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");

        setContentView(R.layout.activity_game);

        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                null, null, null);
        playerCursor.moveToFirst();
        getPlayerColumnIndexes();
        awayTeamName = playerCursor.getString(teamIndex);
        playerCursor.moveToLast();
        homeTeamName = playerCursor.getString(teamIndex);
        setTitle(awayTeamName + " @ " + homeTeamName);
        awayTeam = setTeam(awayTeamName);
        homeTeam = setTeam(homeTeamName);

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

        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null, null, null, null);
        if (gameCursor.moveToFirst()) {
            SharedPreferences shared = getSharedPreferences("info", MODE_PRIVATE);
            gameLogIndex = shared.getInt(KEY_GAMELOGINDEX, 0);
            highestIndex = shared.getInt(KEY_HIGHESTINDEX, 0);
            inningNumber = shared.getInt(KEY_INNINGNUMBER, 2);
            awayTeamIndex = shared.getInt(KEY_AWAYTEAMNDEX, 0);
            homeTeamIndex = shared.getInt(KEY_HOMETEAMINDEX, 0);
            undoRedo = shared.getBoolean(KEY_UNDOREDO, false);

            resumeGame();
            return;
        }
        setInningDisplay();
        finalInning = false;

        startGame();
    }

    private ArrayList<Player> setTeam(String teamName) {

        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{teamName};
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, sortOrder);

        int nameIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
        int idIndex = playerCursor.getColumnIndex(StatsEntry._ID);

        ArrayList<Player> team = new ArrayList<>();
        while (playerCursor.moveToNext()) {
            int playerId = playerCursor.getInt(idIndex);
            String playerName = playerCursor.getString(nameIndex);
            team.add(new Player(playerName, teamName, playerId));
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
        currentBaseLogStart = new BaseLog(currentTeam, currentBatter, "", "", "", 0, 0, 0);

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

        try {
            startCursor();
            setDisplays();
        } catch (Exception e) {
            Log.v(TAG, "Error with startCursor() or setDisplays()!");
        }
    }

    private void resumeGame() {
        getGameColumnIndexes();

        gameCursor.moveToPosition(gameLogIndex);
        reloadRunsLog();
        reloadBaseLog();
        awayTeamRuns = currentBaseLogStart.getAwayTeamRuns();
        homeTeamRuns = currentBaseLogStart.getHomeTeamRuns();
        gameOuts = currentBaseLogStart.getOutCount();
        currentTeam = currentBaseLogStart.getTeam();
        currentBatter = currentBaseLogStart.getBatter();
        tempBatter = gameCursor.getString(prevBatterIndex);
        if (tempRunsLog == null) {
            tempRunsLog = new ArrayList<>();
        } else {
            tempRunsLog.clear();
        }
        setIndex(currentBatter);
        resetBases(currentBaseLogStart);
        startCursor();
        setDisplays();
        setInningDisplay();
    }


    private void nextBatter() {
        if (currentTeam == homeTeam && finalInning && homeTeamRuns > awayTeamRuns) {
            showEndGameConfirmationDialog();
            return;
        }
        if (gameOuts >= 3) {
            if (currentTeam == homeTeam && finalInning && awayTeamRuns > homeTeamRuns) {
                showEndGameConfirmationDialog();
                return;
            } else {
                nextInning();
            }
        } else {
            increaseLineupIndex();
        }
        updateGameLogs();
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
        SharedPreferences pref = getSharedPreferences("info", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_GAMELOGINDEX, gameLogIndex);
        editor.putInt(KEY_HIGHESTINDEX, highestIndex);
        editor.putInt(KEY_INNINGNUMBER, inningNumber);
        editor.putInt(KEY_AWAYTEAMNDEX, awayTeamIndex);
        editor.putInt(KEY_HOMETEAMINDEX, homeTeamIndex);
        editor.putBoolean(KEY_UNDOREDO, undoRedo);
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
                showEndGameConfirmationDialog();
                return;
            }
            currentTeam = homeTeam;
        } else {
            currentTeam = awayTeam;
        }
        inningNumber++;

        setInningDisplay();
        inningChanged = 1;
    }

    private void endGame() {
        mFirestore = FirebaseFirestore.getInstance();
        addTeamStatsToDB();
        addPlayerStatsToDB();
        getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
        Intent finishGame = new Intent(GameActivity.this, MainActivity.class);
        startActivity(finishGame);
    }

    private void showEndGameConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.end_game_msg);
        builder.setPositiveButton(R.string.end_msg, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                endGame();
            }
        });
        builder.setNegativeButton(R.string.undo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                updateGameLogs();
                undoPlay();
                redoEndsGame = true;
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void addTeamStatsToDB() {
        int valueIndex;
        int newValue;
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgsHome = {homeTeamName};
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS, null,
                selection, selectionArgsHome, null
        );
        playerCursor.moveToFirst();
        ContentValues values = new ContentValues();
        if (homeTeamRuns > awayTeamRuns) {
            valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_WINS);
            newValue = playerCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_WINS, newValue);
        } else if (awayTeamRuns > homeTeamRuns) {
            valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_LOSSES);
            newValue = playerCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_LOSSES, newValue);
        } else {
            valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_TIES);
            newValue = playerCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_TIES, newValue);
        }
        valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RUNSFOR);
        newValue = playerCursor.getInt(valueIndex) + homeTeamRuns;
        values.put(StatsEntry.COLUMN_RUNSFOR, newValue);

        valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RUNSAGAINST);
        newValue = playerCursor.getInt(valueIndex) + awayTeamRuns;
        values.put(StatsEntry.COLUMN_RUNSAGAINST, newValue);

        getContentResolver().update(StatsEntry.CONTENT_URI_TEAMS, values, selection, selectionArgsHome);

        String[] selectionArgsAway = {awayTeamName};
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS, null,
                selection, selectionArgsAway, null
        );
        playerCursor.moveToFirst();
        values = new ContentValues();
        if (awayTeamRuns > homeTeamRuns) {
            valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_WINS);
            newValue = playerCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_WINS, newValue);
        } else if (homeTeamRuns > awayTeamRuns) {
            valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_LOSSES);
            newValue = playerCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_LOSSES, newValue);
        } else {
            valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_TIES);
            newValue = playerCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_TIES, newValue);
        }
        valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RUNSFOR);
        newValue = playerCursor.getInt(valueIndex) + awayTeamRuns;
        values.put(StatsEntry.COLUMN_RUNSFOR, newValue);

        valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RUNSAGAINST);
        newValue = playerCursor.getInt(valueIndex) + homeTeamRuns;
        values.put(StatsEntry.COLUMN_RUNSAGAINST, newValue);

        getContentResolver().update(StatsEntry.CONTENT_URI_TEAMS, values, selection, selectionArgsAway);
    }

    private void addPlayerStatsToDB() {
        ArrayList<String> playerList = new ArrayList<>();
        String selection = StatsEntry.COLUMN_NAME + "=?";

        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                null, null, null);
        playerCursor.moveToPosition(-1);
        while (playerCursor.moveToNext()) {
            String player = playerCursor.getString(nameIndex);
            playerList.add(player);
        }

        for (String playerName : playerList) {
            String[] selectionArgs = new String[]{playerName};

            playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                    selection, selectionArgs, null);
            playerCursor.moveToFirst();
            final int tRBI = playerCursor.getInt(rbiIndex);
            final int tRun = playerCursor.getInt(playerRunIndex);
            final int t1b = playerCursor.getInt(singleIndex);
            final int t2b = playerCursor.getInt(doubleIndex);
            final int t3b = playerCursor.getInt(tripleIndex);
            final int tHR = playerCursor.getInt(hrIndex);
            final int tOuts = playerCursor.getInt(playerOutIndex);
            final int tBB = playerCursor.getInt(bbIndex);
            final int tSF = playerCursor.getInt(sfIndex);


            final DocumentReference docRef = mFirestore.collection("players").document(playerName);
            mFirestore.runTransaction(new Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
//                    docRef = mFirestore.collection("players").document("Purp1");
                    Player player = transaction.get(docRef).toObject(Player.class);
                    int games = player.getGames() + 1;
                    int rbi = player.getRbis() + tRBI;
                    int runs = player.getRuns() + tRun;
                    int singles = player.getSingles() + t1b;
                    int doubles = player.getDoubles() + t2b;
                    int triples = player.getTriples() + t3b;
                    int hrs = player.getHrs() + tHR;
                    int outs = player.getOuts() + tOuts;
                    int walks = player.getWalks() + tBB;
                    int sacfly = player.getSacFlies() + tSF;

                    player.setGames(games);
                    player.setRbis(rbi);
                    player.setRuns(runs);
                    player.setSingles(singles);
                    player.setDoubles(doubles);
                    player.setTriples(triples);
                    player.setHrs(hrs);
                    player.setOuts(outs);
                    player.setWalks(walks);
                    player.setSacFlies(sacfly);

                    transaction.set(docRef, player);

                    return null;
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Transaction success!");
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Transaction failure.", e);
                        }
                    });

//            playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, null, selection, selectionArgs, null);
//            playerCursor.moveToFirst();
//            int pRBIIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RBI);
//            int pRBI = playerCursor.getInt(pRBIIndex);
//            int pRunIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RUN);
//            int pRun = playerCursor.getInt(pRunIndex);
//            int p1bIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_1B);
//            int p1b = playerCursor.getInt(p1bIndex);
//            int p2bIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_2B);
//            int p2b = playerCursor.getInt(p2bIndex);
//            int p3bIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_3B);
//            int p3b = playerCursor.getInt(p3bIndex);
//            int pHRIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_HR);
//            int pHR = playerCursor.getInt(pHRIndex);
//            int pOutsIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_OUT);
//            int pOuts = playerCursor.getInt(pOutsIndex);
//            int pBBIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_BB);
//            int pBB = playerCursor.getInt(pBBIndex);
//            int pSFIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_SF);
//            int pSF = playerCursor.getInt(pSFIndex);
//            int gamesPlayedIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_G);
//            int games = playerCursor.getInt(gamesPlayedIndex);
//
//            ContentValues values = new ContentValues();
//            values.put(StatsEntry.COLUMN_1B, p1b + t1b);
//            values.put(StatsEntry.COLUMN_2B, p2b + t2b);
//            values.put(StatsEntry.COLUMN_3B, p3b + t3b);
//            values.put(StatsEntry.COLUMN_HR, pHR + tHR);
//            values.put(StatsEntry.COLUMN_RUN, pRun + tRun);
//            values.put(StatsEntry.COLUMN_RBI, pRBI + tRBI);
//            values.put(StatsEntry.COLUMN_BB, pBB + tBB);
//            values.put(StatsEntry.COLUMN_OUT, pOuts + tOuts);
//            values.put(StatsEntry.COLUMN_SF, pSF + tSF);
//            values.put(StatsEntry.COLUMN_G, games + 1);
//            getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS, values, selection, selectionArgs);
//        }
        }
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

        String name = playerCursor.getString(nameIndex);

        int tHR = playerCursor.getInt(hrIndex);
        int tRBI = playerCursor.getInt(rbiIndex);
        int tRun = playerCursor.getInt(playerRunIndex);
        int t1b = playerCursor.getInt(singleIndex);
        int t2b = playerCursor.getInt(doubleIndex);
        int t3b = playerCursor.getInt(tripleIndex);
        int tOuts = playerCursor.getInt(playerOutIndex);

        String selection = StatsEntry.COLUMN_NAME + "=?";
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, null, selection, new String[]{name}, null);
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

        String nowBattingString = "Now batting: " + name;
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
//            inningTopArrow.setAlpha(1f);
            inningBottomArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cardview_dark_background));
//            inningBottomArrow.setAlpha(.2f);
            topOrBottom = "Top";
        } else {
            inningTopArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cardview_dark_background));
//            inningTopArrow.setAlpha(.2f);
            inningBottomArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.color_arrow));
//            inningBottomArrow.setAlpha(1f);
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
        //Toast.makeText(GameActivity.this, topOrBottom + " of the " + inningNumber / 2 + indicator, Toast.LENGTH_LONG).show();
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
                Log.v(TAG, "Wrong action entered.");
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
            Log.v(TAG, "Error with updating player runs.");
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
        String outs = gameOuts + "outs";
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
            gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null, null, null, null);
            gameCursor.moveToPosition(gameLogIndex);
            int id = gameCursor.getInt(idIndex);
            Uri toDelete = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_GAMELOG, id);
            getContentResolver().delete(toDelete, null, null);
            undoRedo = false;
            redoEndsGame = false;
            currentRunsLog.clear();
            for (String player : tempRunsLog) {
                currentRunsLog.add(player);
            }
        }
        updatePlayerStats(result, 1);
        gameOuts += tempOuts;
        nextBatter();
        String outs = gameOuts + "outs";
        outsDisplay.setText(outs);
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
        nameIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
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
            gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null, null, null, null);
            gameCursor.moveToPosition(gameLogIndex);
            undoRedo = true;
            tempBatter = gameCursor.getString(prevBatterIndex);
            undoResult = gameCursor.getString(playIndex);
            int inningChanged = gameCursor.getInt(inningChangedIndex);
            if (inningChanged == 1) {
                inningNumber--;
                setInningDisplay();
            }
            gameLogIndex--;
            //redoButton.setVisibility(View.VISIBLE);
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
        setIndex(currentBatter);
        resetBases(currentBaseLogStart);
        updatePlayerStats(undoResult, -1);
    }

    private void redoPlay() {
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null, null, null, null);

        if (gameLogIndex < gameCursor.getCount() - 1) {
            undoRedo = true;
            gameLogIndex++;
        } else {
            //redoButton.setVisibility(View.INVISIBLE);
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
                homeTeamIndex++;
            } else if (currentTeam == homeTeam) {
                awayTeamIndex++;
            } else {
                Log.v(TAG, "inningChanged logic error!");
            }
            setInningDisplay();
            inningChanged = 0;
        }
        setIndex(currentBatter);
        resetBases(currentBaseLogStart);
        updatePlayerStats(redoResult, 1);
        if (redoEndsGame) {
            if (gameCursor.moveToNext()) {
                gameCursor.moveToPrevious();
            } else {
                showEndGameConfirmationDialog();
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
            Log.v(TAG, "Error with reloading BaseLog.");
            return;
        }
        Player batter = findBatterByName(batterName, teamLineup);
        currentBaseLogStart = new BaseLog(teamLineup, batter, first, second, third, outs, awayruns, homeruns);
    }

    private Player findBatterByName(String batterName, List<Player> teamLineup) {
        for (Player player : teamLineup) {
            if (player.getName().equals(batterName)) {
                return player;
            }
        }
        Log.v(TAG, "Error with finding batter.");
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
                            v.setBackground(getDrawable(R.drawable.homeplate2));
                        } else if (v.getId() == R.id.trash) {
                            v.setBackground(getDrawable(R.drawable.base2));
                        } else {
                            v.setBackground(getDrawable(R.drawable.base2));
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
                            v.setBackground(getDrawable(R.drawable.homeplate));
                        } else if (v.getId() == R.id.trash) {
                            v.setBackgroundResource(0);
                        } else {
                            v.setBackground(getDrawable(R.drawable.base));
                        }
                    }
                    //TODO: check later whether can shorten this section
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
                        String sumOuts = gameOuts + tempOuts + "outs";
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
                            scoreString = awayTeamName + " " + (awayTeamRuns + tempRuns) + "    " + homeTeamName + " " + homeTeamRuns;
                        } else {
                            scoreString = awayTeamName + " " + awayTeamRuns + "    " + homeTeamName + " " + (homeTeamRuns + tempRuns);
                        }
                        scoreboard.setText(scoreString);
                    }
                    resetBases.setVisibility(View.VISIBLE);
                    setBaseListeners();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (v.getId() == R.id.home_display) {
                            v.setBackground(getDrawable(R.drawable.homeplate));
                        } else if (v.getId() == R.id.trash) {
                            v.setBackgroundResource(0);
                        } else {
                            v.setBackground(getDrawable(R.drawable.base));
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
                        Log.v(TAG, "SOMETHING WENT WRONG WITH THE SWITCH");
                        break;
                }

                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view.startDragAndDrop(data, shadowBuilder, view, 0);
                } else {
                    view.startDrag(data, shadowBuilder, view, 0);
                }
//                view.setAlpha(.2f);
            }
            view.performClick();
            return true;
        }
    }

    private void increaseLineupIndex() {
        if (currentTeam == awayTeam) {
            awayTeamIndex++;
            if (awayTeamIndex >= awayTeam.size()) {
                awayTeamIndex = 0;
            }
        } else if (currentTeam == homeTeam) {
            homeTeamIndex++;
            if (homeTeamIndex >= homeTeam.size()) {
                homeTeamIndex = 0;
            }
        } else {
            Log.v(TAG, "SOMETHING WENT WRONG WITH THE INDEXES!!!");
        }
    }

    private int getIndex() {
        if (currentTeam == awayTeam) {
            return awayTeamIndex;
        } else if (currentTeam == homeTeam) {
            return homeTeamIndex;
        } else {
            Log.v(TAG, "SOMETHING WENT WRONG WITH THE INDEXES!!!");
        }
        return 0;
    }

    private void setIndex(Player player) {
        if (currentTeam == awayTeam) {
            awayTeamIndex = awayTeam.indexOf(player);
        } else if (currentTeam == homeTeam) {
            homeTeamIndex = homeTeam.indexOf(player);
        } else {
            Log.v(TAG, "SOMETHING WENT WRONG WITH THE INDEXES!!!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
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
            case R.id.action_goto_stats:
                Intent intent = new Intent(GameActivity.this, BoxScoreActivity.class);
                Bundle b = new Bundle();
                b.putString("awayTeam", awayTeamName);
                b.putString("homeTeam", homeTeamName);
                b.putInt("totalInnings", totalInnings);
                intent.putExtras(b);
                startActivity(intent);
                break;
            case R.id.action_exit_game:
                finish();
                break;
            case R.id.action_finish_game:
                showFinishConfirmationDialog();
                break;
//            case android.R.id.home:
//                showQuitConfirmationDialog();
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
}
