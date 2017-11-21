package com.example.android.scorekeepdraft1.activities;

import android.app.Activity;
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
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.Toast;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.FirestoreAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.fragments.FinishGameFragment;
import com.example.android.scorekeepdraft1.gamelog.BaseLog;
import com.example.android.scorekeepdraft1.gamelog.PlayerLog;
import com.example.android.scorekeepdraft1.gamelog.TeamLog;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TeamGameActivity extends AppCompatActivity implements FinishGameFragment.OnFragmentInteractionListener{

    private Cursor playerCursor;
    private Cursor gameCursor;
    private FirebaseFirestore mFirestore;
    private static final String DIALOG_FINISH = "DialogFinish";
    private static final int REQUEST_FINISH = 0;

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

    private TextView otherTeamOutsView;
    private TextView otherTeamRunsView;
    private int otherTeamRuns;

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

    private List<Player> myTeam;
    private int myTeamIndex;

    private String awayTeamName;
    private String homeTeamName;
    private int awayTeamRuns;
    private int homeTeamRuns;

    private String tempBatter;
    private int inningChanged = 0;

    private int inningNumber = 2;
    private int gameOuts = 0;
    private int tempOuts;
    private int tempRuns;

    private Player currentBatter;

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
    private int totalInnings = 1;

    private boolean isHome;
    private boolean isTop;
    private boolean isAlternate;

    private static final String KEY_GAMELOGINDEX = "keyGameLogIndex";
    private static final String KEY_HIGHESTINDEX = "keyHighestIndex";
    private static final String KEY_INNINGNUMBER = "keyInningNumber";
    private static final String KEY_MYTEAMINDEX = "keyMyTeamIndex";
    private static final String KEY_UNDOREDO = "keyUndoRedo";
    private static final String KEY_ISHOME = "isHome";
    private static final String TAG = "TeamGameActivity: ";
    private static final String KEY_REDOENDSGAME = "keyRedoEndsGame";

    private String leagueID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_game);

        //todo if getCurrentSelection == null return to mainActivity
        MyApp myApp = (MyApp) getApplicationContext();

        if (myApp.getCurrentSelection() == null) {
            Intent intent = new Intent(TeamGameActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        leagueID = mainPageSelection.getId();
        String myTeamName = mainPageSelection.getName();

        Bundle b = getIntent().getExtras();
        SharedPreferences pref = getSharedPreferences(leagueID, MODE_PRIVATE);
        if (b != null) {
            isHome = b.getBoolean("isHome");
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(KEY_ISHOME, isHome);
            editor.commit();
        } else {
            isHome = pref.getBoolean(KEY_ISHOME, false);
        }

        playerCursor = getContentResolver().query(StatsContract.StatsEntry.CONTENT_URI_TEMP, null,
                null, null, null);
        playerCursor.moveToFirst();
        getPlayerColumnIndexes();

        if(isHome) {
            homeTeamName = myTeamName;
            awayTeamName = "Away Team";
            myTeam = setTeam(homeTeamName);
        } else {
            awayTeamName = myTeamName;
            homeTeamName = "Home Team";
            myTeam = setTeam(awayTeamName);
        }
        setTitle(awayTeamName + " @ " + homeTeamName);

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

        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                null, null, null);
        if (gameCursor.moveToFirst()) {
            gameLogIndex = pref.getInt(KEY_GAMELOGINDEX, 0);
            highestIndex = pref.getInt(KEY_HIGHESTINDEX, 0);
            inningNumber = pref.getInt(KEY_INNINGNUMBER, 2);
            myTeamIndex = pref.getInt(KEY_MYTEAMINDEX, 0);
            undoRedo = pref.getBoolean(KEY_UNDOREDO, false);
            redoEndsGame = pref.getBoolean(KEY_REDOENDSGAME, false);

            Log.d(TAG, "after getSharedPrefs  =  " + myTeamIndex);
            resumeGame();
            Log.d(TAG, "after resumeGame =  " + myTeamIndex);
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
            int firestoreIDIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
            String firestoreID = playerCursor.getString(firestoreIDIndex);

            team.add(new Player(playerName, teamName, playerId, firestoreID));
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
        isTop = true;
        chooseDisplay();

        awayTeamRuns = 0;
        homeTeamRuns = 0;

        currentBatter = myTeam.get(0);
        currentRunsLog = new ArrayList<>();
        tempRunsLog = new ArrayList<>();
        currentBaseLogStart = new BaseLog(myTeam, currentBatter, "", "", "",
                0, 0, 0);

        ContentValues values = new ContentValues();
        String onDeck;
        if(isAlternate) {
            myTeamIndex = 0;
            onDeck = null;
        } else {
            onDeck = currentBatter.getName();
        }
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

        String outsText = "0 outs";
        outsDisplay.setText(outsText);

        if(isAlternate) {
            setInningDisplay();
            setScoreDisplay();
            return;
        }
        try {
            startCursor();
            setDisplays();
        } catch (Exception e) {
            Log.v(TAG, "Error with startCursor() or setDisplays()!");
        }
    }

    private void resumeGame() {
        isTop = (inningNumber % 2 == 0);
        if (inningNumber / 2 >= totalInnings) {
            finalInning = true;
        }
        getGameColumnIndexes();
        chooseDisplay();
        gameCursor.moveToPosition(gameLogIndex);
        reloadRunsLog();
        reloadBaseLog();
        awayTeamRuns = currentBaseLogStart.getAwayTeamRuns();
        homeTeamRuns = currentBaseLogStart.getHomeTeamRuns();
        gameOuts = currentBaseLogStart.getOutCount();
        currentBatter = currentBaseLogStart.getBatter();
        tempBatter = gameCursor.getString(prevBatterIndex);
        if (tempRunsLog == null) {
            tempRunsLog = new ArrayList<>();
        } else {
            tempRunsLog.clear();
        }
        resetBases(currentBaseLogStart);
        setInningDisplay();
        if(isAlternate) {
            setScoreDisplay();
            return;
        }
        myTeamIndex = myTeam.indexOf(currentBatter);
        startCursor();
        setDisplays();
    }


    private void nextBatter() {
        if(redoEndsGame) {
            return;
        }
        if (!isTop && finalInning && homeTeamRuns > awayTeamRuns) {
            if (isHome) {
                increaseLineupIndex();
            }
            showFinishGameDialog();
            return;
        }
        if (gameOuts >= 3) {
            if (!isTop && finalInning && awayTeamRuns > homeTeamRuns) {
                showFinishGameDialog();
                return;
            } else {
                nextInning();
                if(isAlternate) {
                    increaseLineupIndex();
                    Log.d(TAG, "myteamindex increased to  " + myTeamIndex);
                }
            }
        } else {
            increaseLineupIndex();
            Log.d(TAG, "myteamindex increased to  " + myTeamIndex);
        }
        updateGameLogs();
    }

    private void updateGameLogs() {
        String previousBatterName;
        if(currentBatter != null) {
            previousBatterName = currentBatter.getName();
            currentBatter = myTeam.get(myTeamIndex);
        } else {
            currentBatter = myTeam.get(myTeamIndex);
            previousBatterName = null;
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

        String first = currentBaseLogEnd.getBasepositions()[0];
        String second = currentBaseLogEnd.getBasepositions()[1];
        String third = currentBaseLogEnd.getBasepositions()[2];

        String onDeck = currentBatter.getName();

        if (isAlternate && previousBatterName != null) {
            onDeck = null;
        };

        ContentValues values = new ContentValues();
        values.put(StatsEntry.COLUMN_PLAY, result);
        values.put(StatsEntry.COLUMN_TEAM, 0);
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

        Log.d(TAG, "   gamelog:" + gameLogIndex + "    batter=" + previousBatterName +
                " .  ondeck=" + onDeck + " .  gamelogindex=" +  gameLogIndex);
        saveGameState();

        startCursor();
        if(!isAlternate) {
            setDisplays();
        }
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
        SharedPreferences pref = getSharedPreferences(leagueID, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_GAMELOGINDEX, gameLogIndex);
        editor.putInt(KEY_HIGHESTINDEX, highestIndex);
        editor.putInt(KEY_INNINGNUMBER, inningNumber);
        editor.putInt(KEY_MYTEAMINDEX, myTeamIndex);
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

        inningNumber++;
        chooseDisplay();
        setInningDisplay();
        inningChanged = 1;
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

    private void chooseDisplay() {
        isTop = (inningNumber % 2 == 0);
        if (isTop) {
            if (isHome) {
                setAlternateDisplay(true);
//                increaseLineupIndex();
                isAlternate = true;
            } else {
                setAlternateDisplay(false);
                isAlternate = false;
            }
        } else {
            if (finalInning && homeTeamRuns > awayTeamRuns) {
                if(!redoEndsGame) {
                    showFinishGameDialog();
                }
                return;
            }
            if (isHome) {
                setAlternateDisplay(false);;
                isAlternate = false;
            } else {
                setAlternateDisplay(true);
//                increaseLineupIndex();;
                isAlternate = true;
            }
        }
    }

    private void setAlternateDisplay(boolean alternateDisplay) {
        View radioGroup = findViewById(R.id.radio_group_results);
        View diamond = findViewById(R.id.diamond);
        View alternateTeamDisplay = findViewById(R.id.alternate_team_display);
        if (alternateDisplay) {
            radioGroup.setVisibility(View.GONE);
            diamond.setVisibility(View.GONE);
            alternateTeamDisplay.setVisibility(View.VISIBLE);
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
        if(isHome) {
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
//            if (finalInning && homeTeamRuns > awayTeamRuns) {
//                setScoreDisplay();
//                showFinishGameDialog();
//            }
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

    public void teamAddOut(View v) {
        gameOuts++;
        otherTeamOutsView.setText(String.valueOf(gameOuts));
        if (gameOuts >= 3) {
            currentBatter = null;
            if (undoRedo) {
                deleteGameLogs();
                Log.d(TAG, " after deletegamelogs =  " + gameLogIndex);
                Log.d(TAG, " myteamindex is  =  " + myTeamIndex);
//                increaseLineupIndex();
//                Log.d(TAG, " myteamindex increased to  =  " + myTeamIndex);
            }
            nextBatter();
            Log.d(TAG, " after nextbatter =  " + myTeamIndex);
        }
    }

    public void teamRemoveOut(View v) {
        if (gameOuts <= 0) {
            return;
        }
        gameOuts--;
        Log.d(TAG, " teamRmv  gameouts = " + gameOuts);
        otherTeamOutsView.setText(String.valueOf(gameOuts));
    }

    private void endGame() {
        mFirestore = FirebaseFirestore.getInstance();
        if (isHome) {
            addTeamStatsToDB(homeTeamName, homeTeamRuns, awayTeamRuns);
        } else {
            addTeamStatsToDB(awayTeamName, awayTeamRuns, homeTeamRuns);
        }
        addPlayerStatsToDB();
        getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
        Intent finishGame = new Intent(TeamGameActivity.this, TeamPageActivity.class);
        startActivity(finishGame);
    }

    private void showFinishGameDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FinishGameFragment dialog = FinishGameFragment.newInstance();
        dialog.show(fragmentManager, DIALOG_FINISH);

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(R.string.end_game_msg);
//        builder.setPositiveButton(R.string.end_msg, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                endGame();
//            }
//        });
//        builder.setNegativeButton(R.string.undo, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                updateGameLogs();
//                undoPlay();
//                redoEndsGame = true;
//                if (dialog != null) {
//                    dialog.dismiss();
//                }
//            }
//        });
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
    }

    private void addTeamStatsToDB(String teamName, int teamRuns, int otherTeamRuns) {
        WriteBatch teamBatch = mFirestore.batch();

        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {teamName};
        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS, null,
                selection, selectionArgs, null
        );
        playerCursor.moveToFirst();
        ContentValues values = new ContentValues();
        final ContentValues backupValues = new ContentValues();

        long logId;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            logId = new Date().getTime();
        } else {
            logId = System.currentTimeMillis();
        }

        long teamId = playerCursor.getLong(playerCursor.getColumnIndex(StatsEntry._ID));
        TeamLog teamLog = new TeamLog(teamId, teamRuns, otherTeamRuns);
        backupValues.put(StatsEntry.COLUMN_TEAM_ID, teamId);

        int firestoreIDIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
        String firestoreID = playerCursor.getString(firestoreIDIndex);

        final DocumentReference docRef = mFirestore.collection(FirestoreAdapter.LEAGUE_COLLECTION)
                .document(leagueID).collection(FirestoreAdapter.TEAMS_COLLECTION).document(firestoreID)
                .collection(FirestoreAdapter.TEAM_LOGS).document(String.valueOf(logId));

        if (teamRuns > otherTeamRuns) {
            int valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_WINS);
            int newValue = playerCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_WINS, newValue);
            backupValues.put(StatsEntry.COLUMN_WINS, 1);
            teamLog.setWins(1);
        } else if (otherTeamRuns > teamRuns) {
            int valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_LOSSES);
            int newValue = playerCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_LOSSES, newValue);
            backupValues.put(StatsEntry.COLUMN_LOSSES, 1);
            teamLog.setLosses(1);
        } else {
            int valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_TIES);
            int newValue = playerCursor.getInt(valueIndex) + 1;
            values.put(StatsEntry.COLUMN_TIES, newValue);
            backupValues.put(StatsEntry.COLUMN_TIES, 1);
            teamLog.setTies(1);
        }

        int valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RUNSFOR);
        int newValue = playerCursor.getInt(valueIndex) + teamRuns;
        values.put(StatsEntry.COLUMN_RUNSFOR, newValue);
        backupValues.put(StatsEntry.COLUMN_RUNSFOR, teamRuns);

        valueIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RUNSAGAINST);
        newValue = playerCursor.getInt(valueIndex) + otherTeamRuns;
        values.put(StatsEntry.COLUMN_RUNSAGAINST, newValue);
        backupValues.put(StatsEntry.COLUMN_RUNSAGAINST, otherTeamRuns);

        values.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);

        getContentResolver().update(StatsEntry.CONTENT_URI_TEAMS, values, selection, selectionArgs);

        teamBatch.set(docRef, teamLog);
        teamBatch.commit().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("teamBatch failure", e);
                getContentResolver().insert(StatsEntry.CONTENT_URI_BACKUP_TEAMS, backupValues);
            }
        });
    }

    private void addPlayerStatsToDB() {
        ArrayList<Long> playerList = new ArrayList<>();
        String selection = StatsEntry.COLUMN_PLAYERID + "=?";

        playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                null, null, null);
        while (playerCursor.moveToNext()) {
            long playerId = playerCursor.getLong(playerIdIndex);
            playerList.add(playerId);
        }

        WriteBatch playerBatch = mFirestore.batch();

        for (long playerId : playerList) {
            String[] selectionArgs = new String[]{String.valueOf(playerId)};

            playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                    selection, selectionArgs, null);
            playerCursor.moveToFirst();
            int gameRBI = playerCursor.getInt(rbiIndex);
            int gameRun = playerCursor.getInt(playerRunIndex);
            int game1b = playerCursor.getInt(singleIndex);
            int game2b = playerCursor.getInt(doubleIndex);
            int game3b = playerCursor.getInt(tripleIndex);
            int gameHR = playerCursor.getInt(hrIndex);
            int gameOuts = playerCursor.getInt(playerOutIndex);
            int gameBB = playerCursor.getInt(bbIndex);
            int gameSF = playerCursor.getInt(sfIndex);

            int firestoreIDIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
            String firestoreID = playerCursor.getString(firestoreIDIndex);

            long logId;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                logId = new Date().getTime();
            } else {
                logId = System.currentTimeMillis();
            }
            final DocumentReference docRef = mFirestore.collection(FirestoreAdapter.LEAGUE_COLLECTION)
                    .document(leagueID).collection(FirestoreAdapter.PLAYERS_COLLECTION).document(firestoreID)
                    .collection(FirestoreAdapter.PLAYER_LOGS).document(String.valueOf(logId));

            PlayerLog playerLog = new PlayerLog(playerId, gameRBI, gameRun, game1b, game2b, game3b,
                    gameHR, gameOuts, gameBB, gameSF);
            playerBatch.set(docRef, playerLog);

            Uri playerUri = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_PLAYERS, playerId);
            playerCursor = getContentResolver().query(playerUri, null, null, null, null);
            playerCursor.moveToFirst();

            int pRBIIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RBI);
            int pRBI = playerCursor.getInt(pRBIIndex);
            int pRunIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RUN);
            int pRun = playerCursor.getInt(pRunIndex);
            int p1bIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_1B);
            int p1b = playerCursor.getInt(p1bIndex);
            int p2bIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_2B);
            int p2b = playerCursor.getInt(p2bIndex);
            int p3bIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_3B);
            int p3b = playerCursor.getInt(p3bIndex);
            int pHRIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_HR);
            int pHR = playerCursor.getInt(pHRIndex);
            int pOutsIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_OUT);
            int pOuts = playerCursor.getInt(pOutsIndex);
            int pBBIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_BB);
            int pBB = playerCursor.getInt(pBBIndex);
            int pSFIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_SF);
            int pSF = playerCursor.getInt(pSFIndex);
            int gamesPlayedIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_G);
            int games = playerCursor.getInt(gamesPlayedIndex);
            firestoreIDIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
            firestoreID = playerCursor.getString(firestoreIDIndex);

            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_1B, p1b + game1b);
            values.put(StatsEntry.COLUMN_2B, p2b + game2b);
            values.put(StatsEntry.COLUMN_3B, p3b + game3b);
            values.put(StatsEntry.COLUMN_HR, pHR + gameHR);
            values.put(StatsEntry.COLUMN_RUN, pRun + gameRun);
            values.put(StatsEntry.COLUMN_RBI, pRBI + gameRBI);
            values.put(StatsEntry.COLUMN_BB, pBB + gameBB);
            values.put(StatsEntry.COLUMN_OUT, pOuts + gameOuts);
            values.put(StatsEntry.COLUMN_SF, pSF + gameSF);
            values.put(StatsEntry.COLUMN_G, games + 1);
            values.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
            getContentResolver().update(playerUri, values, null, null);
        }
        playerBatch.commit().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("playerBatch failure", e);
                playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                        null, null, null);
                while (playerCursor.moveToNext()) {
                    long logId;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        logId = new Date().getTime();
                    } else {
                        logId = System.currentTimeMillis();
                    }
                    int playerId = playerCursor.getInt(playerCursor.getColumnIndex(StatsEntry.COLUMN_PLAYERID));
                    int gameRBI = playerCursor.getInt(rbiIndex);
                    int gameRun = playerCursor.getInt(playerRunIndex);
                    int game1b = playerCursor.getInt(singleIndex);
                    int game2b = playerCursor.getInt(doubleIndex);
                    int game3b = playerCursor.getInt(tripleIndex);
                    int gameHR = playerCursor.getInt(hrIndex);
                    int gameOuts = playerCursor.getInt(playerOutIndex);
                    int gameBB = playerCursor.getInt(bbIndex);
                    int gameSF = playerCursor.getInt(sfIndex);

                    ContentValues backupValues = new ContentValues();
                    backupValues.put(StatsEntry.COLUMN_PLAYERID, playerId);
                    backupValues.put(StatsEntry.COLUMN_LOG_ID, logId);
                    backupValues.put(StatsEntry.COLUMN_1B, game1b);
                    backupValues.put(StatsEntry.COLUMN_2B, game2b);
                    backupValues.put(StatsEntry.COLUMN_3B, game3b);
                    backupValues.put(StatsEntry.COLUMN_HR, gameHR);
                    backupValues.put(StatsEntry.COLUMN_RUN, gameRun);
                    backupValues.put(StatsEntry.COLUMN_RBI, gameRBI);
                    backupValues.put(StatsEntry.COLUMN_BB, gameBB);
                    backupValues.put(StatsEntry.COLUMN_OUT, gameOuts);
                    backupValues.put(StatsEntry.COLUMN_SF, gameSF);

                    getContentResolver().insert(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, backupValues);
                }
            }
        });
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
        Toast.makeText(TeamGameActivity.this, topOrBottom + " of the " + inningNumber / 2 + indicator, Toast.LENGTH_LONG).show();
    }

    private void updatePlayerStats(String action, int n) {
//        Log.d(TAG, "updatePlayerStats   " + myTeamIndex);

        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs;
        if (undoRedo) {
            selectionArgs = new String[]{tempBatter};
            if(tempBatter == null) {
                Log.d(TAG, "updatePlayerStats failed: temp batter  = null " + myTeamIndex);
                return;
            }
        } else {
            if(currentBatter == null) {
                Log.d(TAG, "updatePlayerStats failed:  currentbatter == null!" + myTeamIndex);
                return;
            }
            String currentBatterString = currentBatter.getName();
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
        if (isAlternate) {
//            Log.d(TAG, "updatePlayerStats isalt return  " + myTeamIndex);
            return;
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
            Log.e(TAG, "Error with updating player runs.1");
        }
        if (!undoRedo) {
            if (isTop && !isHome) {
                awayTeamRuns++;
            } else if (!isTop && isHome) {
                homeTeamRuns++;
            } else {
                Log.e(TAG, "Error with updating player runs.2");
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
        }
        updatePlayerStats(result, 1);
        gameOuts += tempOuts;
        nextBatter();
        String outs = gameOuts + " outs";
        outsDisplay.setText(outs);
//        Log.d(TAG, " onSubmit  gameouts = " + gameOuts);
    }

    private void deleteGameLogs(){
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                null, null, null);
        gameCursor.moveToPosition(gameLogIndex);
        int id = gameCursor.getInt(idIndex);
        Uri toDelete = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_GAMELOG, id);
        getContentResolver().delete(toDelete, null, null);
        undoRedo = false;
        redoEndsGame = false;
        currentRunsLog.clear();
        currentRunsLog.addAll(tempRunsLog);
    }

    public void getGameColumnIndexes() {
        idIndex = gameCursor.getColumnIndex(StatsEntry._ID);
        playIndex = gameCursor.getColumnIndex(StatsEntry.COLUMN_PLAY);
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
            int inningChanged = gameCursor.getInt(inningChangedIndex);
            if (inningChanged == 1) {
                inningNumber--;
                setInningDisplay();
            }
            isAlternate = (gameCursor.getString(currentBatterIndex) == null);
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
        currentBatter = currentBaseLogStart.getBatter();
        if(currentBatter != null) {
            decreaseLineupIndex();
                        myTeamIndex = myTeam.indexOf(currentBatter);

            Log.d(TAG, "myteamindex changed to " + myTeamIndex);
//            myTeamIndex = myTeam.indexOf(currentBatter);
        } else {
//            decreaseLineupIndex();
//            Log.d(TAG, "myteamindex index decreased to " + myTeamIndex);
        }
        resetBases(currentBaseLogStart);
//        Log.d(TAG, " undoplay  gameouts = " + gameOuts);
        if(isAlternate) {
            chooseDisplay();
            setInningDisplay();
        } else {
            isAlternate = (gameCursor.getString(currentBatterIndex) == null);
            if (isAlternate) {
                chooseDisplay();
                setInningDisplay();
                Log.d(TAG, "undo, current == null   " + myTeamIndex);
                return;
            }
        }
        updatePlayerStats(undoResult, -1);
    }

    private void redoPlay() {
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                null, null, null);

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
        currentBatter = currentBaseLogStart.getBatter();
        tempBatter = gameCursor.getString(prevBatterIndex);
        String redoResult = gameCursor.getString(playIndex);
        if (!tempRunsLog.isEmpty()) {
            tempRunsLog.clear();
        }
        inningChanged = gameCursor.getInt(inningChangedIndex);
        if (inningChanged == 1) {
            inningNumber++;
            if ((isTop && isHome) || (!isTop && !isHome)) {
                myTeamIndex++;
                Log.d(TAG, " redoplay inningchanged increase = " + myTeamIndex);
            }
            setInningDisplay();
            inningChanged = 0;
        }
        if(currentBatter != null) {
            myTeamIndex = myTeam.indexOf(currentBatter);
        } else {
            increaseLineupIndex();
        }
        resetBases(currentBaseLogStart);
        isAlternate = (tempBatter == null);

        if (!isAlternate){
            isAlternate = (currentBatter == null);
            updatePlayerStats(redoResult, 1);
        }
        if (redoEndsGame) {
            if (gameCursor.moveToNext()) {
                gameCursor.moveToPrevious();
            } else {
                showFinishGameDialog();
            }
        }
        if (isAlternate){
            chooseDisplay();
            if (tempBatter == null) {
                startCursor();
                setDisplays();
            }
        }
        Log.d(TAG, " redoplay end = " + myTeamIndex);
    }

    private void reloadBaseLog() {
        int outs = gameCursor.getInt(gameOutIndex);
        int awayruns = gameCursor.getInt(awayRunsIndex);
        int homeruns = gameCursor.getInt(homeRunsIndex);
        String batterName = gameCursor.getString(currentBatterIndex);
        String first = gameCursor.getString(firstIndex);
        String second = gameCursor.getString(secondIndex);
        String third = gameCursor.getString(thirdIndex);
        Player batter = findBatterByName(batterName, myTeam);
        currentBaseLogStart = new BaseLog(myTeam, batter, first, second, third, outs, awayruns, homeruns);
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

    @Override
    public void onFragmentInteraction(boolean isOver) {
        if (isOver) {
            endGame();
        } else {
            if (!redoEndsGame) {
                updateGameLogs();
                redoEndsGame = true;
            }
//            else {
//                redoPlay();
//            }
            undoPlay();
        }
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
                        if (isTop) {
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
        myTeamIndex++;
        if (myTeamIndex >= myTeam.size()) {
            myTeamIndex = 0;
        } else {
            Log.v(TAG, "SOMETHING WENT WRONG WITH THE INDEXES!!!");
        }
    }

    private void decreaseLineupIndex() {
        myTeamIndex--;
        if (myTeamIndex <= 0) {
            myTeamIndex = myTeam.size() - 1;
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
                Intent intent = new Intent(TeamGameActivity.this, BoxScoreActivity.class);
                Bundle b = new Bundle();
                b.putString("awayTeam", awayTeamName);
                b.putString("homeTeam", homeTeamName);
                b.putInt("totalInnings", totalInnings);
                b.putInt("awayTeamRuns", awayTeamRuns);
                b.putInt("homeTeamRuns", homeTeamRuns);
                intent.putExtras(b);
                startActivity(intent);
                break;
            case R.id.action_exit_game:
                intent = new Intent(TeamGameActivity.this, TeamPageActivity.class);
                startActivity(intent);
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
}