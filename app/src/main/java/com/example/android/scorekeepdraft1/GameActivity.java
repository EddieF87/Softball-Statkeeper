/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.scorekeepdraft1;

import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.undoredo.BaseLog;
import com.example.android.scorekeepdraft1.undoredo.GameHistory;

import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.undoredo.RunsLog;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static android.media.CamcorderProfile.get;
import static com.example.android.scorekeepdraft1.R.id.reset;

/**
 * @author Eddie
 */
public class GameActivity extends AppCompatActivity /*implements LoaderManager.LoaderCallbacks<Cursor>*/ {

    private Cursor playerCursor;
    private Cursor gameCursor;
    private Uri gameUri;

    private TextView scoreboard;
    private TextView nowBatting;
    private TextView outsDisplay;
    private TextView inningDisplay;
    private TextView avgDisplay;
    private TextView rbiDisplay;
    private TextView runDisplay;
    private TextView hrDisplay;

    private Button submitPlay;
    private Button resetBases;

    private RadioGroup group1;
    private RadioGroup group2;
    private String result;

    private ImageView batterDisplay;
    private ImageView outTrash;
    private TextView firstDisplay;
    private TextView secondDisplay;
    private TextView thirdDisplay;
    private TextView homeDisplay;

    private List<String> awayTeam;
    private List<String> homeTeam;
    private String awayTeamName;
    private String homeTeamName;
    private int awayTeamRuns;
    private int homeTeamRuns;
    private int awayTeamIndex;
    private int homeTeamIndex;

    //temporary buttonw?
    private Button undoButton;
    private Button redoButton;

    private String tempBatter;
    private int inningChanged = 0;

    private int inningNumber = 2;
    private int gameOuts = 0;
    private int tempOuts;
    private int tempRuns;

    private String currentBatter;
    private List<String> currentTeam;

    private NumberFormat formatter = new DecimalFormat("#.000");
    private BaseLog currentBaseLogStart;
    private BaseLog currentBaseLogEnd;
    private ArrayList<String> currentRunsLog;
    private ArrayList<String> tempRunsLog;

    private GameHistory gameHistory;
    private int gameLogIndex = 0;
    private boolean undoRedo = false;

    private boolean finalInning;

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
    private int nameIndex;
    private int teamIndex;
    private int orderIndex;
    private int singleIndex;
    private int doubleIndex;
    private int tripleIndex;
    private int hrIndex;
    private int bbIndex;
    private int sfIndex;
    private int playerOutIndex;
    private int playerRunIndex;
    private int rbiIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            awayTeamName = b.getString("awayteam");
            homeTeamName = b.getString("hometeam");
        }
        awayTeam = new ArrayList<>();
        homeTeam = new ArrayList<>();

        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";
        String[] selectionArgs = new String[]{awayTeamName};

        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI3, null,
                selection, selectionArgs, sortOrder);
        int nameIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
        while (playerCursor.moveToNext()) {
            String playerName = playerCursor.getString(nameIndex);
            awayTeam.add(playerName);
        }

        selectionArgs = new String[]{homeTeamName};
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI3, null,
                selection, selectionArgs, sortOrder);
        while (playerCursor.moveToNext()) {
            String playerName = playerCursor.getString(nameIndex);
            homeTeam.add(playerName);
        }
        playerCursor.moveToFirst();
        getPlayerColumnIndeces();

        scoreboard = (TextView) findViewById(R.id.scoreboard);
        nowBatting = (TextView) findViewById(R.id.nowbatting);
        outsDisplay = (TextView) findViewById(R.id.num_of_outs);
        inningDisplay = (TextView) findViewById(R.id.inning);
        avgDisplay = (TextView) findViewById(R.id.avgdisplay);
        rbiDisplay = (TextView) findViewById(R.id.rbidisplay);
        runDisplay = (TextView) findViewById(R.id.rundisplay);
        hrDisplay = (TextView) findViewById(R.id.hrdisplay);

        group1 = (RadioGroup) findViewById(R.id.group1);
        group2 = (RadioGroup) findViewById(R.id.group2);

        submitPlay = (Button) findViewById(R.id.submit);
        submitPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });
        submitPlay.setVisibility(View.INVISIBLE);

        resetBases = (Button) findViewById(reset);
        resetBases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetBases(currentBaseLogStart);
            }
        });
        resetBases.setVisibility(View.INVISIBLE);

        //temporary?
        undoButton = (Button) findViewById(R.id.undobutton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoPlay();
            }
        });

        redoButton = (Button) findViewById(R.id.redobutton);
        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoPlay();
            }
        });
        redoButton.setVisibility(View.INVISIBLE);

        batterDisplay = (ImageView) findViewById(R.id.batter);
        firstDisplay = (TextView) findViewById(R.id.first_display);
        secondDisplay = (TextView) findViewById(R.id.second_display);
        thirdDisplay = (TextView) findViewById(R.id.third_display);
        homeDisplay = (TextView) findViewById(R.id.home_display);
        outTrash = (ImageView) findViewById(R.id.trash);

        batterDisplay.setOnLongClickListener(new MyClickListener());

        firstDisplay.setOnDragListener(new MyDragListener());
        secondDisplay.setOnDragListener(new MyDragListener());
        thirdDisplay.setOnDragListener(new MyDragListener());
        homeDisplay.setOnDragListener(new MyDragListener());
        outTrash.setOnDragListener(new MyDragListener());

        finalInning = false;

        startGame();
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        playEntered = true;
        // Check which radio button was clicked
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
            firstDisplay.setOnLongClickListener(null);
            firstDisplay.setOnDragListener(new MyDragListener());
            firstOccupied = false;
        } else {
            firstDisplay.setOnLongClickListener(new MyClickListener());
            firstDisplay.setOnDragListener(null);
            firstOccupied = true;
        }

        if (secondDisplay.getText().toString().isEmpty()) {
            secondDisplay.setOnLongClickListener(null);
            secondDisplay.setOnDragListener(new MyDragListener());
            secondOccupied = false;
        } else {
            secondDisplay.setOnLongClickListener(new MyClickListener());
            secondDisplay.setOnDragListener(null);
            secondOccupied = true;
        }

        if (thirdDisplay.getText().toString().isEmpty()) {
            thirdDisplay.setOnLongClickListener(null);
            thirdDisplay.setOnDragListener(new MyDragListener());
            thirdOccupied = false;
        } else {
            thirdDisplay.setOnLongClickListener(new MyClickListener());
            thirdDisplay.setOnDragListener(null);
            thirdOccupied = true;
        }
        homeDisplay.setOnDragListener(new MyDragListener());
    }

    public void startGame() {
        awayTeamRuns = 0;
        homeTeamRuns = 0;

        //gameHistory = new GameHistory();
        currentTeam = awayTeam;
        currentBatter = awayTeam.get(0);
        currentRunsLog = new ArrayList<>();
        tempRunsLog = new ArrayList<>();
        currentBaseLogStart = new BaseLog(currentTeam, currentBatter, "", "", "", 0, 0, 0);

        //gameHistory.addGameLog(new GameLog(currentBaseLogStart, currentRunsLog, "start", null, inningNumber, false));
        scoreboard.setText(awayTeamName + " " + awayTeamRuns + "    " + homeTeamName + " " + homeTeamRuns);

        ContentValues values = new ContentValues();
        //currentBaseLogStart
        values.put(StatsEntry.COLUMN_ONDECK, currentBatter);
        values.put(StatsEntry.COLUMN_TEAM, 0);
        values.put(StatsEntry.COLUMN_OUT, 0);
        values.put(StatsEntry.COLUMN_AWAY_RUNS, 0);
        values.put(StatsEntry.COLUMN_HOME_RUNS, 0);
        //currentRunsLog
        //result
        values.put(StatsEntry.COLUMN_PLAY, "start");
        //previousBatter
        //isInningChanged
        values.put(StatsEntry.COLUMN_INNING_CHANGED, 0);
        gameUri = getContentResolver().insert(StatsEntry.CONTENT_URI4, values);
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI4, null,
                null, null, null);
        getGameColumnIndeces();

        try {
            startCursor();
            setDisplays();
        } catch (Exception e) {
            Toast.makeText(GameActivity.this, "UHOH SPAGETTIOS!", Toast.LENGTH_LONG).show();
        }
    }

    public void nextBatter() {
        if (currentTeam == homeTeam && finalInning && homeTeamRuns > awayTeamRuns) {
            endGame();
        }
        if (gameOuts >= 3) {
            if (currentTeam == homeTeam && finalInning && awayTeamRuns > homeTeamRuns) {
                endGame();
            } else {
                nextInning();
            }
        } else {
            increaseIndex();
        }
        String previousBatterName = currentBatter;
        currentBatter = currentTeam.get(getIndex());

        currentBaseLogEnd = new BaseLog(currentTeam, currentBatter, firstDisplay.getText().toString(),
                secondDisplay.getText().toString(), thirdDisplay.getText().toString(),
                gameOuts, awayTeamRuns, homeTeamRuns
        );
        currentBaseLogStart = new BaseLog(currentTeam, currentBatter, firstDisplay.getText().toString(),
                secondDisplay.getText().toString(), thirdDisplay.getText().toString(),
                gameOuts, awayTeamRuns, homeTeamRuns
        );
        //gameHistory.addGameLog(new GameLog(currentBaseLogEnd, currentRunsLog, result, previousBatterName, inningNumber, inningChanged));
        gameLogIndex++;

        int team;
        if (currentTeam == awayTeam) {
            team = 0;
        } else {
            team = 1;
        }
        String first = currentBaseLogEnd.getBasepositions()[0];
        String second = currentBaseLogEnd.getBasepositions()[1];
        String third = currentBaseLogEnd.getBasepositions()[2];

        ContentValues values = new ContentValues();
        values.put(StatsEntry.COLUMN_PLAY, result);
        values.put(StatsEntry.COLUMN_TEAM, team);
        values.put(StatsEntry.COLUMN_BATTER, previousBatterName);
        values.put(StatsEntry.COLUMN_ONDECK, currentBatter);
        values.put(StatsEntry.COLUMN_1B, first);
        values.put(StatsEntry.COLUMN_2B, second);
        values.put(StatsEntry.COLUMN_3B, third);
        values.put(StatsEntry.COLUMN_OUT, gameOuts);
        values.put(StatsEntry.COLUMN_AWAY_RUNS, awayTeamRuns);
        values.put(StatsEntry.COLUMN_HOME_RUNS, homeTeamRuns);
        values.put(StatsEntry.COLUMN_INNING_CHANGED, inningChanged);
        for(int i = 0; i < currentRunsLog.size(); i++) {
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
        gameUri = getContentResolver().insert(StatsEntry.CONTENT_URI4, values);

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

    public void nextInning() {
        gameOuts = 0;
        emptyBases();

        if (inningNumber / 2 >= 1) {
            finalInning = true;
        }
        increaseIndex();
        if (currentTeam == awayTeam) {
            if (finalInning && homeTeamRuns > awayTeamRuns) {
                endGame();
            }
            currentTeam = homeTeam;
        } else {
            currentTeam = awayTeam;
            inningDisplay.setText(String.valueOf(inningNumber / 2));
        }
        inningNumber++;
        Toast.makeText(GameActivity.this, "inning " + inningNumber, Toast.LENGTH_LONG).show();

        inningDisplay.setText(String.valueOf(inningNumber / 2));
        inningChanged = 1;
    }

    public void startCursor() {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {currentBatter};
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI3, null,
                selection, selectionArgs, null);
        playerCursor.moveToNext();
    }

    public void endGame() {
        int valueIndex;
        int newValue;
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgsHome = {homeTeamName};
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI2, null,
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

        getContentResolver().update(StatsEntry.CONTENT_URI2, values, selection, selectionArgsHome);

        String[] selectionArgsAway = {awayTeamName};
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI2, null,
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

        getContentResolver().update(StatsEntry.CONTENT_URI2, values, selection, selectionArgsAway);
        addStatsToPermanentDB();

        Intent finishGame = new Intent(GameActivity.this, MainActivity.class);
        startActivity(finishGame);
    }

    private void addStatsToPermanentDB() {
        ArrayList<String> playerList = new ArrayList<>();
        String selection = StatsEntry.COLUMN_NAME + "=?";

        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI3, null,
                null, null,  null);
        playerCursor.moveToPosition(-1);
        while (playerCursor.moveToNext()) {
            String player = playerCursor.getString(nameIndex);
            playerList.add(player);
        }

        for(String player : playerList) {
            String[] selectionArgs = new String[] {player};

            playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI3, null,
                    selection, selectionArgs,  null);
            playerCursor.moveToFirst();
            int pRBI = playerCursor.getInt(rbiIndex);
            int pRun = playerCursor.getInt(playerRunIndex);
            int p1b = playerCursor.getInt(singleIndex);
            int p2b = playerCursor.getInt(doubleIndex);
            int p3b = playerCursor.getInt(tripleIndex);
            int pHR = playerCursor.getInt(hrIndex);
            int pOuts = playerCursor.getInt(playerOutIndex);
            int pBB = playerCursor.getInt(bbIndex);
            int pSF = playerCursor.getInt(sfIndex);

            playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI1, null, selection, selectionArgs,  null);
            playerCursor.moveToFirst();
            int tRBI = playerCursor.getInt(rbiIndex);
            int tRun = playerCursor.getInt(playerRunIndex);
            int t1b = playerCursor.getInt(singleIndex);
            int t2b = playerCursor.getInt(doubleIndex);
            int t3b = playerCursor.getInt(tripleIndex);
            int tHR = playerCursor.getInt(hrIndex);
            int tOuts = playerCursor.getInt(playerOutIndex);
            int tBB = playerCursor.getInt(bbIndex);
            int tSF = playerCursor.getInt(sfIndex);
            int gamesPlayedIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_G);
            int games = playerCursor.getInt(gamesPlayedIndex);

            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_1B, p1b + t1b);
            values.put(StatsEntry.COLUMN_2B, p2b + t2b);
            values.put(StatsEntry.COLUMN_3B, p3b + t3b);
            values.put(StatsEntry.COLUMN_HR, pHR + tHR);
            values.put(StatsEntry.COLUMN_RUN, pRun + tRun);
            values.put(StatsEntry.COLUMN_RBI, pRBI + tRBI);
            values.put(StatsEntry.COLUMN_BB, pBB + tBB);
            values.put(StatsEntry.COLUMN_OUT, pOuts + tOuts);
            values.put(StatsEntry.COLUMN_SF, pSF + tSF);
            values.put(StatsEntry.COLUMN_G, games + 1);
            getContentResolver().update(StatsEntry.CONTENT_URI1, values, selection, selectionArgs);
        }








    }


    //sets the textview displays with updated player/game data
    public void setDisplays() {
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
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI1, null, selection, new String[]{name}, null);
        playerCursor.moveToFirst();

        int pHR = playerCursor.getInt(hrIndex);
        int pRBI = playerCursor.getInt(rbiIndex);
        int pRun = playerCursor.getInt(playerRunIndex);
        int p1b = playerCursor.getInt(singleIndex);
        int p2b = playerCursor.getInt(doubleIndex);
        int p3b = playerCursor.getInt(tripleIndex);
        int pOuts = playerCursor.getInt(playerOutIndex);

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

        scoreboard.setText(awayTeamName + " " + awayTeamRuns + "    " + homeTeamName + " " + homeTeamRuns);
    }

    public void updatePlayerStats(String action) {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs;
        if (undoRedo) {
            selectionArgs = new String[]{tempBatter};
//            playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI3, null,
//                    selection, selectionArgs, null);
        } else {
            selectionArgs = new String[]{currentBatter};
        }
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI3, null,
                selection, selectionArgs, null);
        playerCursor.moveToFirst();
        ContentValues values = new ContentValues();
        int valueIndex;
        int newValue;

        switch (action) {
            case "1b":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_1B);
                newValue = playerCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_1B, newValue);
                break;
            case "2b":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_2B);
                newValue = playerCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_2B, newValue);
                break;
            case "3b":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_3B);
                newValue = playerCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_3B, newValue);
                break;
            case "hr":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_HR);
                newValue = playerCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_HR, newValue);
                break;
            case "bb":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_BB);
                newValue = playerCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_BB, newValue);
                break;
            case "sf":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_SF);
                newValue = playerCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_SF, newValue);
                break;
            case "out":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_OUT);
                newValue = playerCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_OUT, newValue);
                break;
            default:
                Toast.makeText(GameActivity.this, "SOMETHING FUCKED UP BIG TIME!!!", Toast.LENGTH_LONG).show();
                break;
        }

        int rbiCount = currentRunsLog.size();
        if (rbiCount > 0) {
            valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RBI);
            newValue = playerCursor.getInt(valueIndex) + rbiCount;
            values.put(StatsEntry.COLUMN_RBI, newValue);
        }

        getContentResolver().update(StatsEntry.CONTENT_URI3, values, selection, selectionArgs);

        if (rbiCount > 0) {
            for (String player : currentRunsLog) {
                addRun(player);
            }
        }
        startCursor();
        setDisplays();
    }

    public void undoPlayerStats(String action) {
        if (tempBatter == null) {
            Toast.makeText(GameActivity.this, "TWAS NULL!!!", Toast.LENGTH_SHORT).show();
            return;
        }
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {tempBatter};
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI3, null,
                selection, selectionArgs, null
        );
        playerCursor.moveToFirst();
        ContentValues values = new ContentValues();
        int valueIndex;
        int newValue;

        switch (action) {
            case "1b":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_1B);
                newValue = playerCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_1B, newValue);
                break;
            case "2b":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_2B);
                newValue = playerCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_2B, newValue);
                break;
            case "3b":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_3B);
                newValue = playerCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_3B, newValue);
                break;
            case "hr":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_HR);
                newValue = playerCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_HR, newValue);
                break;
            case "bb":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_BB);
                newValue = playerCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_BB, newValue);
                break;
            case "sf":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_SF);
                newValue = playerCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_SF, newValue);
                break;
            case "out":
                valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_OUT);
                newValue = playerCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_OUT, newValue);
                break;
            default:
                Toast.makeText(GameActivity.this, "SOMETHING FUCKED UP BIG TIME!!!", Toast.LENGTH_LONG).show();
                break;
        }

        int rbiCount = currentRunsLog.size();
        if (rbiCount > 0) {
            valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RBI);
            newValue = playerCursor.getInt(valueIndex) - rbiCount;
            values.put(StatsEntry.COLUMN_RBI, newValue);
        }

        getContentResolver().update(StatsEntry.CONTENT_URI3, values, selection, selectionArgs);

        if (rbiCount > 0) {
            for (String player : currentRunsLog) {
                subtractRun(player);
            }
        }
        startCursor();
        setDisplays();
    }

    public void addRun(String player) {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {player};
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI3, null,
                selection, selectionArgs, null
        );
        playerCursor.moveToNext();

        ContentValues values = new ContentValues();
        int valueIndex;
        int newValue;
        valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RUN);
        newValue = playerCursor.getInt(valueIndex) + 1;
        values.put(StatsEntry.COLUMN_RUN, newValue);

        getContentResolver().update(StatsEntry.CONTENT_URI3, values, selection, selectionArgs);
        if (!undoRedo) {
            if (currentTeam == homeTeam) {
                homeTeamRuns++;
            } else {
                awayTeamRuns++;
            }
        }
    }

    public void subtractRun(String player) {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {player};
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI3, null,
                selection, selectionArgs, null
        );
        if (playerCursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            int newValue;
            newValue = playerCursor.getInt(playerRunIndex) - 1;
            values.put(StatsEntry.COLUMN_RUN, newValue);

            getContentResolver().update(StatsEntry.CONTENT_URI3, values, selection, selectionArgs);
        } else {
            Toast.makeText(GameActivity.this, "subtractRun logic is fucked up!!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void resetBases(BaseLog baseLog) {
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
        scoreboard.setText(awayTeamName + " " + awayTeamRuns + "    " + homeTeamName + " " + homeTeamRuns);
    }

    public void emptyBases() {
        firstDisplay.setText("");
        secondDisplay.setText("");
        thirdDisplay.setText("");
    }

    public double calculateAverage(int singles, int doubles, int triples, int hrs, int outs) {
        double hits = (double) (singles + doubles + triples + hrs);
        return (hits / (outs + hits));
    }

    public void onSubmit() {
        if (undoRedo) {
            gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI4, null, null, null, null);
            gameCursor.moveToPosition(gameLogIndex);
            int id = gameCursor.getInt(idIndex);
            Uri toDelete = ContentUris.withAppendedId(StatsEntry.CONTENT_URI4, id);
            int playsDeleted = getContentResolver().delete(toDelete, null, null);
            String deleteToast = "Deleted " + playsDeleted + " plays!";
            Toast.makeText(GameActivity.this, deleteToast, Toast.LENGTH_SHORT).show();
            undoRedo = false;
            currentRunsLog = tempRunsLog;
        }
        updatePlayerStats(result);
        gameOuts += tempOuts;
        nextBatter();
        String outs = gameOuts + "outs";
        outsDisplay.setText(outs);
    }

    public void getGameColumnIndeces(){
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

    public void getPlayerColumnIndeces(){
        idIndex = playerCursor.getColumnIndex(StatsEntry._ID);
        nameIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
        teamIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_TEAM);
        orderIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_ORDER);
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

    public void undoPlay() {
        String undoResult;
        if (gameLogIndex>0) {
            gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI4, null, null, null, null);
            gameCursor.moveToPosition(gameLogIndex);
            undoRedo = true;
            tempBatter = gameCursor.getString(prevBatterIndex);
            undoResult = gameCursor.getString(playIndex);
            int inningChanged = gameCursor.getInt(inningChangedIndex);
            if (inningChanged == 1) {
                inningNumber--;
                Toast.makeText(GameActivity.this, "inning subtracted " + inningNumber, Toast.LENGTH_LONG).show();
                inningDisplay.setText(String.valueOf(inningNumber / 2));
            }
            gameLogIndex--;
            redoButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(GameActivity.this, "This is the beginning of the game!", Toast.LENGTH_SHORT).show();
            return;
        }
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
        undoPlayerStats(undoResult);
        setDisplays();
    }

    public void redoPlay() {
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI4, null, null, null, null);

        if (gameLogIndex < gameCursor.getCount() - 1) {
            undoRedo = true;
            gameLogIndex++;
        } else {
            redoButton.setVisibility(View.INVISIBLE);
            Toast.makeText(GameActivity.this, "Already caught up! \n gameindex =" + gameLogIndex, Toast.LENGTH_SHORT).show();
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
        tempBatter = gameCursor.getString(currentBatterIndex);
        String redoResult = gameCursor.getString(playIndex);
        if (!tempRunsLog.isEmpty()) {
            tempRunsLog.clear();
        }
        inningChanged = gameCursor.getInt(inningChangedIndex);
        if (inningChanged == 1) {
            inningNumber++;
            Toast.makeText(GameActivity.this, "inning added " + inningNumber, Toast.LENGTH_LONG).show();
            if (currentTeam == awayTeam) {
                homeTeamIndex++;
            } else if (currentTeam == homeTeam) {
                awayTeamIndex++;
            } else {
                Toast.makeText(GameActivity.this, "inningChanged logic is fucked up!!!", Toast.LENGTH_SHORT).show();
            }
            inningDisplay.setText(String.valueOf(inningNumber / 2));
            inningChanged = 0;
        }
        setIndex(currentBatter);
        resetBases(currentBaseLogStart);
        updatePlayerStats(redoResult);
        setDisplays();
    }

    private void reloadBaseLog() {
        int outs = gameCursor.getInt(gameOutIndex);
        int awayruns = gameCursor.getInt(awayRunsIndex);
        int homeruns = gameCursor.getInt(homeRunsIndex);
        String batter = gameCursor.getString(currentBatterIndex);
        int team = gameCursor.getInt(currentTeamIndex);
        String first = gameCursor.getString(firstIndex);
        String second = gameCursor.getString(secondIndex);
        String third = gameCursor.getString(thirdIndex);
        List<String> teamLineup;
        if (team == 0){
            teamLineup = awayTeam;
        } else if (team == 1){
            teamLineup = homeTeam;
        } else {
            Toast.makeText(GameActivity.this, "FUKUP FUKUP with teamLINEUP!", Toast.LENGTH_SHORT).show();
            return;
        }
        currentBaseLogStart = new BaseLog(teamLineup, batter, first, second, third, outs, awayruns, homeruns);
    }

    private void reloadRunsLog() {
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
        public boolean onDrag(View v, DragEvent event) {
            //if (undoRedo) {if (tempRunsLog == null || tempRunsLog.getRBICount() == 0) {tempRunsLog = new RunsLog();}}
            int action = event.getAction();
            TextView dropPoint = null;
            if (v.getId() != R.id.trash) {
                dropPoint = (TextView) v;
            }
            View eventView = (View) event.getLocalState();

            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        v.setBackgroundColor(Color.LTGRAY);
                    }
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(Color.TRANSPARENT);
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    break;
                case DragEvent.ACTION_DROP:
                    String movedPlayer = "";
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
                        } else {
                            dropPoint.setText(currentBatter);
                            batterDisplay.setVisibility(View.INVISIBLE);
                            batterMoved = true;
                            if (playEntered) {
                                submitPlay.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    v.setBackgroundColor(Color.TRANSPARENT);
                    if (dropPoint == homeDisplay) {
                        if (eventView instanceof TextView) {
                            if (undoRedo) {
                                tempRunsLog.add(movedPlayer);
                            } else {
                                currentRunsLog.add(movedPlayer);
                            }
                        } else {
                            if (undoRedo) {
                                tempRunsLog.add(currentBatter);
                            } else {
                                currentRunsLog.add(currentBatter);
                            }
                        }
                        homeDisplay.setText("");
                        tempRuns++;
                        if (currentTeam == awayTeam) {
                            scoreboard.setText(awayTeamName + " " + (awayTeamRuns + tempRuns) + "    " + homeTeamName + " " + homeTeamRuns);
                        } else {
                            scoreboard.setText(awayTeamName + " " + awayTeamRuns + "    " + homeTeamName + " " + (homeTeamRuns + tempRuns));
                        }
                    }
                    resetBases.setVisibility(View.VISIBLE);
                    setBaseListeners();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.TRANSPARENT);
                    if (eventView instanceof TextView) {
                        eventView.setBackgroundColor(Color.TRANSPARENT);
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    //TODO: LOOK into performance issues of making new onclick/ondrag listeners each time as opposed to perhaps recycling the same one?
    // This defines your touch listener
    private final class MyClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
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
                    Toast.makeText(GameActivity.this, "SOMETHING WENT WRONG WITH THE SWITCH", Toast.LENGTH_LONG).show();
                    break;
            }

            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(data, shadowBuilder, view, 0);
            } else {
                view.startDrag(data, shadowBuilder, view, 0);
            }
            return true;
        }
    }

    private void increaseIndex() {
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
            Toast.makeText(GameActivity.this, "SOMETHING WENT WRONG WITH THE INDEXES!!!", Toast.LENGTH_LONG).show();
        }
    }

    private int getIndex() {
        if (currentTeam == awayTeam) {
            return awayTeamIndex;
        } else if (currentTeam == homeTeam) {
            return homeTeamIndex;
        } else {
            Toast.makeText(GameActivity.this, "SOMETHING WENT WRONG WITH THE INDEXES!!!", Toast.LENGTH_LONG).show();
        }
        return 0;
    }

    private void setIndex(String player) {
        if (currentTeam == awayTeam) {
            awayTeamIndex = awayTeam.indexOf(player);
        } else if (currentTeam == homeTeam) {
            homeTeamIndex = homeTeam.indexOf(player);
        } else {
            Toast.makeText(GameActivity.this, "SOMETHING WENT WRONG WITH THE INDEXES!!!", Toast.LENGTH_LONG).show();
        }
    }
}
