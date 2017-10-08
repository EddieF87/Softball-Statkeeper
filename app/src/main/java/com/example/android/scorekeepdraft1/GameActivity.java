/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.scorekeepdraft1;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
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
import com.example.android.scorekeepdraft1.undoredo.GameLog;
import com.example.android.scorekeepdraft1.undoredo.RunsLog;

import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.scorekeepdraft1.R.id.reset;

/**
 * @author Eddie
 */
public class GameActivity extends AppCompatActivity /*implements LoaderManager.LoaderCallbacks<Cursor>*/ {

    private Cursor mCursor;

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
    private boolean inningChanged = false;

    private int inningNumber = 2;
    private int gameOuts = 0;
    private int tempOuts;
    private int tempRuns;

    private String currentBatter;
    private List<String> currentTeam;

    private NumberFormat formatter = new DecimalFormat("#.000");
    private BaseLog currentBaseLogStart;
    private BaseLog currentBaseLogEnd;
    private RunsLog currentRunsLog;
    private RunsLog tempRunsLog;

    private GameHistory gameHistory;
    private int gameLogIndex = 0;
    private boolean undoRedo = false;

    private boolean finalInning;

    private boolean playEntered = false;
    private boolean batterMoved = false;
    private boolean firstOccupied = false;
    private boolean secondOccupied = false;
    private boolean thirdOccupied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            awayTeamName = b.getString("awayteam");
            homeTeamName = b.getString("hometeam");
        }
        awayTeam = new ArrayList<>();
        homeTeam = new ArrayList<>();

        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";
        String[] selectionArgs = new String[]{awayTeamName};

        mCursor = getContentResolver().query(StatsEntry.CONTENT_URI1, null,
                selection, selectionArgs, sortOrder);
        while (mCursor.moveToNext()) {
            int nameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            int orderIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_ORDER);
            String playerName = mCursor.getString(nameIndex);
            int order = mCursor.getInt(orderIndex);
            if( order < 50) {
                awayTeam.add(playerName);
            }
        }

        selectionArgs = new String[]{homeTeamName};
        mCursor = getContentResolver().query(StatsEntry.CONTENT_URI1, null,
                selection, selectionArgs, sortOrder);
        while (mCursor.moveToNext()) {
            int nameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            int orderIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_ORDER);
            String playerName = mCursor.getString(nameIndex);
            int order = mCursor.getInt(orderIndex);
            if( order < 50) {
                homeTeam.add(playerName);
            }
        }

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

        finalInning= false;

        startGame();
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        playEntered = true;
        // Check which radio button was clicked
        switch(view.getId()) {
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
        if(batterMoved) {
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
        //TEMPORARY
        /*for (int i = 0; i < listOfPlayers.length; i++) {
            if (i % 2 == 0) {
                awayTeam.addPlayer(new Player(listOfPlayers[i]));
            } else {
                homeTeam.addPlayer(new Player(listOfPlayers[i]));
            }
        }*/
        awayTeamRuns = 0;
        homeTeamRuns = 0;

        gameHistory = new GameHistory();
        currentTeam = awayTeam;
        currentBatter = awayTeam.get(0);
        currentRunsLog = new RunsLog();
        currentBaseLogStart = new BaseLog(currentTeam, currentBatter, "", "", "", 0, 0, 0);
        gameHistory.addGameLog(new GameLog(currentBaseLogStart, currentRunsLog, "start", null, inningNumber, false));
        scoreboard.setText(awayTeamName + " " + awayTeamRuns + "    " + homeTeamName + " " + homeTeamRuns);

        try {
            startCursor();
            setDisplays();
        } catch (Exception e) {
            Toast.makeText(GameActivity.this, "UHOH SPAGETTIOS!", Toast.LENGTH_LONG).show();
        }
    }

    public void nextBatter() {
        if(currentTeam == homeTeam && finalInning && homeTeamRuns>awayTeamRuns) {
            endGame();
        }
        if (gameOuts >= 3) {
            if(currentTeam == homeTeam && finalInning && awayTeamRuns>homeTeamRuns) {
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
                gameOuts, awayTeamRuns, homeTeamRuns);
        currentBaseLogStart = new BaseLog(currentTeam, currentBatter, firstDisplay.getText().toString(),
                secondDisplay.getText().toString(), thirdDisplay.getText().toString(),
                gameOuts, awayTeamRuns, homeTeamRuns);
        gameHistory.addGameLog(new GameLog(currentBaseLogEnd, currentRunsLog, result, previousBatterName, inningNumber, inningChanged));
        gameLogIndex++;

        startCursor();
        setDisplays();
        group1.clearCheck();
        group2.clearCheck();
        tempRunsLog = null;
        currentRunsLog = new RunsLog();
        submitPlay.setVisibility(View.INVISIBLE);
        resetBases.setVisibility(View.INVISIBLE);
        tempOuts = 0;
        tempRuns = 0;
        playEntered = false;
        batterMoved = false;
        inningChanged = false;
    }

    public void nextInning() {
        gameOuts = 0;
        emptyBases();

        if (inningNumber/2 >= 1) {
            finalInning = true;
        }
        increaseIndex();
        if (currentTeam == awayTeam) {
            if(finalInning && homeTeamRuns > awayTeamRuns) {
                endGame();
            }
            currentTeam = homeTeam;
        } else {
            currentTeam = awayTeam;
            inningDisplay.setText(String.valueOf(inningNumber/2));
        }
        inningNumber++;
        Toast.makeText(GameActivity.this, "inning " + inningNumber, Toast.LENGTH_LONG).show();

        inningDisplay.setText(String.valueOf(inningNumber/2));
        inningChanged = true;
    }

    public void startCursor() {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {currentBatter};
        mCursor = getContentResolver().query(
                StatsEntry.CONTENT_URI1, null,
                selection, selectionArgs, null
        );
        mCursor.moveToNext();
    }

    public void endGame() {
        int valueIndex;
        int newValue;
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgsHome = {homeTeamName};
        mCursor = getContentResolver().query(
                StatsEntry.CONTENT_URI2, null,
                selection, selectionArgsHome, null
        );
        mCursor.moveToFirst();
        ContentValues values = new ContentValues();
        if (homeTeamRuns>awayTeamRuns) {
            valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_WINS);
            newValue = mCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_WINS, newValue);
        } else if (awayTeamRuns>homeTeamRuns) {
            valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_LOSSES);
            newValue = mCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_LOSSES, newValue);
        } else {
            valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_TIES);
            newValue = mCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_TIES, newValue);
        }
        valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_RUNSFOR);
        newValue = mCursor.getInt(valueIndex) + homeTeamRuns;
        values.put(StatsEntry.COLUMN_RUNSFOR, newValue);

        valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_RUNSAGAINST);
        newValue = mCursor.getInt(valueIndex) + awayTeamRuns;
        values.put(StatsEntry.COLUMN_RUNSAGAINST, newValue);

        getContentResolver().update(
                StatsEntry.CONTENT_URI2,
                values,
                selection,
                selectionArgsHome
        );


        String[] selectionArgsAway = {awayTeamName};
        mCursor = getContentResolver().query(
                StatsEntry.CONTENT_URI2, null,
                selection, selectionArgsAway, null
        );
        mCursor.moveToFirst();
        values = new ContentValues();
        if (awayTeamRuns>homeTeamRuns) {
            valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_WINS);
            newValue = mCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_WINS, newValue);
        } else if (homeTeamRuns>awayTeamRuns) {
            valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_LOSSES);
            newValue = mCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_LOSSES, newValue);
        } else {
            valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_TIES);
            newValue = mCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_TIES, newValue);
        }
        valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_RUNSFOR);
        newValue = mCursor.getInt(valueIndex) + awayTeamRuns;
        values.put(StatsEntry.COLUMN_RUNSFOR, newValue);

        valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_RUNSAGAINST);
        newValue = mCursor.getInt(valueIndex) + homeTeamRuns;
        values.put(StatsEntry.COLUMN_RUNSAGAINST, newValue);

        getContentResolver().update(
                StatsEntry.CONTENT_URI2,
                values,
                selection,
                selectionArgsAway
        );


        Intent finishGame = new Intent(GameActivity.this, MainActivity.class);
        startActivity(finishGame);
    }


        //sets the textview displays with updated player/game data
    public void setDisplays() {
        batterDisplay.setVisibility(View.VISIBLE);

        int nameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
        int hrIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_HR);
        int rbiIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_RBI);
        int runIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_RUN);
        int singleIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_1B);
        int doubleIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_2B);
        int tripleIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_3B);
        int outIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_OUT);

        String name = mCursor.getString(nameIndex);
        int displayHR = mCursor.getInt(hrIndex);
        int displayRBI = mCursor.getInt(rbiIndex);
        int displayRun = mCursor.getInt(runIndex);
        int singles = mCursor.getInt(singleIndex);
        int doubles = mCursor.getInt(doubleIndex);
        int triples = mCursor.getInt(tripleIndex);
        int playerOuts = mCursor.getInt(outIndex);

        double avg = calculateAverage(singles, doubles, triples, displayHR, playerOuts, 0);

        nowBatting.setText("Now batting: " + name);
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
        String[] selectionArgs;
        if(undoRedo) {
            String selection = StatsEntry.COLUMN_NAME + "=?";
            selectionArgs = new String[]{tempBatter};
            mCursor = getContentResolver().query(
                    StatsEntry.CONTENT_URI1, null,
                    selection, selectionArgs, null
            );
        } else {
            selectionArgs = new String[]{currentBatter};
        }
        mCursor.moveToFirst();
        ContentValues values = new ContentValues();
        int valueIndex;
        int newValue;

        switch (action) {
            case "1b":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_1B);
                newValue = mCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_1B, newValue);
                break;
            case "2b":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_2B);
                newValue = mCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_2B, newValue);
                break;
            case "3b":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_3B);
                newValue = mCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_3B, newValue);
                break;
            case "hr":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_HR);
                newValue = mCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_HR, newValue);
                break;
            case "bb":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_BB);
                newValue = mCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_BB, newValue);
                break;
            case "sf":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_SF);
                newValue = mCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_SF, newValue);
                break;
            case "out":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_OUT);
                newValue = mCursor.getInt(valueIndex) + 1;
                values.put(StatsEntry.COLUMN_OUT, newValue);
                break;
            default:
                Toast.makeText(GameActivity.this, "SOMETHING FUCKED UP BIG TIME!!!", Toast.LENGTH_LONG).show();
                break;
        }

        int rbiCount = currentRunsLog.getRBICount();
        if (rbiCount > 0) {
            valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_RBI);
            newValue = mCursor.getInt(valueIndex) + rbiCount;
            values.put(StatsEntry.COLUMN_RBI, newValue);
        }

        String selection = StatsEntry.COLUMN_NAME + "=?";
        getContentResolver().update(
                StatsEntry.CONTENT_URI1,
                values,
                selection,
                selectionArgs
        );

        if(rbiCount > 0) {
            for(String player : currentRunsLog.getPlayersScored()) {
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
        mCursor = getContentResolver().query(
                StatsEntry.CONTENT_URI1, null,
                selection, selectionArgs, null);
        mCursor.moveToFirst();
        ContentValues values = new ContentValues();
        int valueIndex;
        int newValue;

        switch (action) {
            case "1b":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_1B);
                newValue = mCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_1B, newValue);
                break;
            case "2b":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_2B);
                newValue = mCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_2B, newValue);
                break;
            case "3b":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_3B);
                newValue = mCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_3B, newValue);
                break;
            case "hr":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_HR);
                newValue = mCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_HR, newValue);
                break;
            case "bb":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_BB);
                newValue = mCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_BB, newValue);
                break;
            case "sf":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_SF);
                newValue = mCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_SF, newValue);
                break;
            case "out":
                valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_OUT);
                newValue = mCursor.getInt(valueIndex) - 1;
                values.put(StatsEntry.COLUMN_OUT, newValue);
                break;
            default:
                Toast.makeText(GameActivity.this, "SOMETHING FUCKED UP BIG TIME!!!", Toast.LENGTH_LONG).show();
                break;
        }

        int rbiCount = currentRunsLog.getRBICount();
        if (rbiCount > 0) {
            valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_RBI);
            newValue = mCursor.getInt(valueIndex) - rbiCount;
            values.put(StatsEntry.COLUMN_RBI, newValue);
        }

/*        String selection = PlayervStatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {currentBatter.getName()};*/
        getContentResolver().update(
                StatsEntry.CONTENT_URI1,
                values,
                selection,
                selectionArgs
        );

        if(rbiCount > 0) {
            for(String player : currentRunsLog.getPlayersScored()) {
                subtractRun(player);
            }
        }
        startCursor();
        setDisplays();
    }

    public void addRun(String player) {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {player};
        mCursor = getContentResolver().query(
                StatsEntry.CONTENT_URI1, null,
                selection, selectionArgs, null
        );
        mCursor.moveToNext();

        ContentValues values = new ContentValues();
        int valueIndex;
        int newValue;
        valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_RUN);
        newValue = mCursor.getInt(valueIndex) + 1;
        values.put(StatsEntry.COLUMN_RUN, newValue);

        getContentResolver().update(
                StatsEntry.CONTENT_URI1,
                values,
                selection,
                selectionArgs
        );
        if (!undoRedo) {
            if (currentTeam == homeTeam) {
                homeTeamRuns++;
            } else {
                awayTeamRuns++;
            };
        }
    }

    public void subtractRun(String player) {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {player};
        mCursor = getContentResolver().query(
                StatsEntry.CONTENT_URI1, null,
                selection, selectionArgs, null
        );
        mCursor.moveToNext();

        ContentValues values = new ContentValues();
        int valueIndex;
        int newValue;
        valueIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_RUN);
        newValue = mCursor.getInt(valueIndex) - 1;
        values.put(StatsEntry.COLUMN_RUN, newValue);

        getContentResolver().update(
                StatsEntry.CONTENT_URI1,
                values,
                selection,
                selectionArgs
        );
    }

    public void resetBases(BaseLog baseLog) {
        String[] bases = baseLog.getBasepositions();
        firstDisplay.setText(bases[0]);
        secondDisplay.setText(bases[1]);
        thirdDisplay.setText(bases[2]);
        batterDisplay.setVisibility(View.VISIBLE);
        batterMoved = false;
        if(!undoRedo) {
            currentRunsLog.resetPlayersScored();
        }
        if(tempRunsLog != null) {
            tempRunsLog.resetPlayersScored();
        }
        submitPlay.setVisibility(View.INVISIBLE);
        resetBases.setVisibility(View.INVISIBLE);
        setBaseListeners();
        tempOuts = 0;
        tempRuns = 0;
        outsDisplay.setText(gameOuts + "outs");
        scoreboard.setText(awayTeamName + " " + awayTeamRuns + "    " + homeTeamName + " " + homeTeamRuns);
    }

    public void emptyBases() {
        firstDisplay.setText("");
        secondDisplay.setText("");
        thirdDisplay.setText("");
    }

    public double calculateAverage(int singles, int doubles, int triples, int hrs, int outs, int errors) {
        double hits = (double) (singles + doubles + triples + hrs);
        return (hits / (outs + errors + hits));
    }

    public void onSubmit() {
        if(undoRedo) {
            gameHistory.updateGameLog(gameLogIndex);
            undoRedo = false;
            currentRunsLog = tempRunsLog;
        }
        updatePlayerStats(result);
        gameOuts += tempOuts;
        nextBatter();
        outsDisplay.setText(gameOuts + "outs");
    }

    public void undoPlay() {
        String undoResult;
        if(gameLogIndex > 0) {
            undoRedo = true;
            GameLog gameLog1 = gameHistory.getGameLog(gameLogIndex);
            tempBatter = gameLog1.getPreviousBatter();
            undoResult = gameLog1.getResult();
            currentRunsLog = gameLog1.getRunsLog();
            if (gameLog1.isInningChanged()){
                inningNumber--;
                Toast.makeText(GameActivity.this, "inning subtracted " + inningNumber, Toast.LENGTH_LONG).show();
                inningDisplay.setText(String.valueOf(inningNumber/2));
            }
            gameLogIndex--;
            redoButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(GameActivity.this, "This is the beginning of the game!", Toast.LENGTH_SHORT).show();
            return;
        }
        GameLog gameLog2 = gameHistory.getGameLog(gameLogIndex);
        BaseLog baseLogEnd = gameLog2.getBaseLogEnd();
        currentBaseLogStart = baseLogEnd;
        //undoRuns
        awayTeamRuns = baseLogEnd.getAwayTeamRuns();
        homeTeamRuns = baseLogEnd.getHomeTeamRuns();
        if(tempRunsLog != null) {
            tempRunsLog.resetPlayersScored();
        }

        gameOuts = baseLogEnd.getOutCount();
        currentTeam = baseLogEnd.getTeam();
        currentBatter = baseLogEnd.getBatter();
        setIndex(currentBatter);
        resetBases(baseLogEnd);
        undoPlayerStats(undoResult);
        setDisplays();
    }

    public void redoPlay() {
        if(gameLogIndex < gameHistory.getAllGameLogs().size() -1) {
            undoRedo = true;
            gameLogIndex++;
        }  else {
            redoButton.setVisibility(View.INVISIBLE);
            Toast.makeText(GameActivity.this, "Already caught up! \n gameindex =" + gameLogIndex, Toast.LENGTH_SHORT).show();
            return;
        }
        GameLog gameLog = gameHistory.getGameLog(gameLogIndex);
        currentRunsLog = gameLog.getRunsLog();
        BaseLog baseLogEnd = gameLog.getBaseLogEnd();
        currentBaseLogStart = baseLogEnd;
        awayTeamRuns = baseLogEnd.getAwayTeamRuns();
        homeTeamRuns = baseLogEnd.getHomeTeamRuns();
        gameOuts = baseLogEnd.getOutCount();
        currentTeam = baseLogEnd.getTeam();
        currentBatter = baseLogEnd.getBatter();
        tempBatter = gameLog.getPreviousBatter();
        String redoResult = gameLog.getResult();
        if(tempRunsLog != null) {
            tempRunsLog.resetPlayersScored();
        }
        if(gameLog.isInningChanged()) {
            inningNumber++;
            Toast.makeText(GameActivity.this, "inning added " + inningNumber, Toast.LENGTH_LONG).show();
            if(currentTeam == awayTeam) {
                homeTeamIndex++;
            } else if (currentTeam == homeTeam){
                awayTeamIndex++;
            } else {
                Toast.makeText(GameActivity.this, "inningChanged logic is fucked up!!!", Toast.LENGTH_SHORT).show();
            }
            inningDisplay.setText(String.valueOf(inningNumber/2));
            inningChanged = false;
        }
        setIndex(currentBatter);
        resetBases(baseLogEnd);
        updatePlayerStats(redoResult);
        setDisplays();
    }

    private class MyDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            if(undoRedo) {
                if (tempRunsLog == null || tempRunsLog.getRBICount() == 0) {
                tempRunsLog = new RunsLog();
                }
            }
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
                    //check later whether can shorten this section
                    if (v.getId() == R.id.trash) {
                        if (eventView instanceof TextView) {
                            TextView draggedView = (TextView) eventView;
                            draggedView.setText("");
                        } else {
                            batterDisplay.setVisibility(View.INVISIBLE);
                            batterMoved = true;
                            if(playEntered) {
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
                            if(playEntered) {
                                submitPlay.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    v.setBackgroundColor(Color.TRANSPARENT);
                    if (dropPoint == homeDisplay) {
                        if (eventView instanceof TextView) {
                            if(undoRedo) {
                                tempRunsLog.addPlayerScored(movedPlayer);
                            } else {currentRunsLog.addPlayerScored(movedPlayer);}
                        } else {
                            if(undoRedo) {
                                tempRunsLog.addPlayerScored(currentBatter);
                            } else {currentRunsLog.addPlayerScored(currentBatter);}
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
            if(homeTeamIndex >= homeTeam.size()) {
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

    public void subtractOut() {
        gameOuts--;
    }
    public int getGameOuts() {
        return gameOuts;
    }
    public void setGameOuts(int gameOuts) {
        this.gameOuts = gameOuts;
    }
    public int getInningNumber() {return inningNumber;}
    public void setInningNumber(int inningNumber) {this.inningNumber = inningNumber;}
    public void increaseInningNumber() {
        this.inningNumber++;
    }
    public void decreaseInningNumber() {this.inningNumber--;}
}
