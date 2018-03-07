package com.example.android.scorekeepdraft1.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.dialogs.EndOfGameDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.FinishGameConfirmationDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.SaveDeleteGameFragment;
import com.example.android.scorekeepdraft1.gamelog.BaseLog;

import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.Player;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class GameActivity extends AppCompatActivity
        implements EndOfGameDialogFragment.OnFragmentInteractionListener,
        SaveDeleteGameFragment.OnFragmentInteractionListener,
        FinishGameConfirmationDialogFragment.OnFragmentInteractionListener {

    protected Cursor gameCursor;

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
    protected ImageView undoButton;
    protected ImageView redoButton;

    protected Button submitPlay;
    protected Button resetBases;

    protected RadioGroup group1;
    protected RadioGroup group2;
    protected String result;

    protected RelativeLayout batterDisplay;
    protected TextView batterText;
    protected TextView firstDisplay;
    protected TextView secondDisplay;
    protected TextView thirdDisplay;
    protected TextView homeDisplay;

    protected TextView step1View;
    protected TextView step2View;
    protected TextView step3View;
    protected TextView step4View;
    protected ImageView step1Arrow;
    protected ImageView step2Arrow;


    protected String awayTeamName;
    protected String homeTeamName;
    protected int awayTeamRuns;
    protected int homeTeamRuns;

    protected String tempBatter;
    protected int inningChanged = 0;
    protected int inningNumber = 2;
    protected int gameOuts = 0;
    protected int tempOuts;
    protected int tempRuns;

    protected Player currentBatter;

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

    protected static final String KEY_GAMELOGINDEX = "keyGameLogIndex";
    protected static final String KEY_HIGHESTINDEX = "keyHighestIndex";
    protected static final String KEY_GENDERSORT = "keyGenderSort";
    protected static final String KEY_FEMALEORDER = "keyFemaleOrder";
    protected static final String KEY_INNINGNUMBER = "keyInningNumber";
    protected static final String KEY_TOTALINNINGS = "keyTotalInnings";
    protected static final String KEY_UNDOREDO = "keyUndoRedo";
    protected static final String KEY_REDOENDSGAME = "redoEndsGame";
    protected static final String TAG = "GameActivity: ";
    protected static final String DIALOG_FINISH = "DialogFinish";

    protected String selectionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSelectionData();
        setCustomViews();
        setViews();

        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                null, null, null);
        if (gameCursor.moveToFirst()) {
            loadGamePreferences();
            Bundle args = getIntent().getExtras();
            if (args != null) {
                if (args.getBoolean("edited") && undoRedo) {
                    deleteGameLogs();
                    highestIndex = gameLogIndex;
                    invalidateOptionsMenu();
                    setUndoButton();
                    setRedoButton();
                }
            }
            resumeGame();
            return;
        }
        setInningDisplay();
        finalInning = false;
        startGame();
    }

    protected abstract void getSelectionData();

    protected void setViews() {
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
        disableSubmitButton();

        resetBases = findViewById(R.id.reset);
        resetBases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetBases(currentBaseLogStart);
            }
        });
        disableResetButton();

        undoButton = findViewById(R.id.btn_undo);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undoButton.setClickable(false);
                undoPlay();
            }
        });

        redoButton = findViewById(R.id.btn_redo);
        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redoButton.setClickable(false);
                redoPlay();
            }
        });

        batterDisplay = findViewById(R.id.batter);
        batterText = findViewById(R.id.batter_name_view);
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
    }

    protected abstract void loadGamePreferences();

    protected abstract void setCustomViews();

    protected ArrayList<Player> setTeam(String teamID) {
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

    protected List<Player> genderSort(List<Player> team, int femaleRequired) {
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
            enableSubmitButton();
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

    protected void startGame() {
        step1View = findViewById(R.id.step1text);
        step2View = findViewById(R.id.step2text);
        step3View = findViewById(R.id.step3text);
        step4View = findViewById(R.id.step4text);
        step1Arrow = findViewById(R.id.step1_arrow);
        step2Arrow = findViewById(R.id.step2_arrow);
        step1View.setVisibility(View.VISIBLE);
        step2View.setVisibility(View.VISIBLE);
        step3View.setVisibility(View.VISIBLE);
        step4View.setVisibility(View.VISIBLE);
        step1Arrow.setVisibility(View.VISIBLE);
        step2Arrow.setVisibility(View.VISIBLE);
        submitPlay.setOnClickListener(null);

        submitPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                step1View.setVisibility(View.GONE);
                step2View.setVisibility(View.GONE);
                step3View.setVisibility(View.GONE);
                step4View.setVisibility(View.GONE);
                step1Arrow.setVisibility(View.GONE);
                step2Arrow.setVisibility(View.GONE);
                onSubmit();
                submitPlay.setOnClickListener(null);
                submitPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSubmit();
                    }
                });
            }
        });
    }

    protected abstract void resumeGame();

    protected void nextBatter() {
        if (!isTopOfInning() && finalInning && homeTeamRuns > awayTeamRuns) {
            if (isLeagueGameOrHomeTeam()) {
                increaseLineupIndex();
            }
            showFinishGameDialog();
            return;
        }
        if (gameOuts >= 3) {
            if (!isTopOfInning() && finalInning && awayTeamRuns > homeTeamRuns) {
                showFinishGameDialog();
                return;
            } else {
                nextInning();
                if (isTeamAlternate()) {
                    increaseLineupIndex();
                }
            }
        } else {
            increaseLineupIndex();
        }
        enableSubmitButton();
        updateGameLogs();
    }

    protected abstract boolean isLeagueGameOrHomeTeam();

    protected abstract void updateGameLogs();

    protected void clearTempState() {
        group1.clearCheck();
        group2.clearCheck();
        tempRunsLog.clear();
        currentRunsLog.clear();
        disableSubmitButton();
        disableResetButton();
        tempOuts = 0;
        tempRuns = 0;
        playEntered = false;
        batterMoved = false;
        inningChanged = 0;
    }

    protected void enterGameValues(BaseLog currentBaseLogEnd, int team,
                                   String previousBatterID, String onDeckID) {

        String first = currentBaseLogEnd.getBasepositions()[0];
        String second = currentBaseLogEnd.getBasepositions()[1];
        String third = currentBaseLogEnd.getBasepositions()[2];

        ContentValues values = new ContentValues();
        values.put(StatsEntry.COLUMN_PLAY, result);
        values.put(StatsEntry.COLUMN_TEAM, team);
        values.put(StatsEntry.COLUMN_BATTER, previousBatterID);
        values.put(StatsEntry.COLUMN_ONDECK, onDeckID);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveGameState();
    }

    protected abstract void saveGameState();

    protected abstract void nextInning();


    protected void endGame() {
        firestoreUpdate();

        getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);

        exitToManager();
    }

    protected abstract void firestoreUpdate();

    protected void showFinishGameDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag(DIALOG_FINISH);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);

        DialogFragment newFragment = EndOfGameDialogFragment.newInstance();
        newFragment.show(fragmentTransaction, DIALOG_FINISH);
    }

    protected Player getPlayerFromCursor(Uri uri, String playerFirestoreID) {
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = {playerFirestoreID};
        Cursor cursor = getContentResolver().query(uri, null,
                selection, selectionArgs, null);
        cursor.moveToFirst();
        boolean tempData = uri.equals(StatsEntry.CONTENT_URI_TEMP);
        Player player = new Player(cursor, tempData);
        cursor.close();
        return player;
    }

    //sets the textview displays with updated player/game data
    protected void setDisplays() {

        String playerFirestoreID = currentBatter.getFirestoreID();

        Player inGamePlayer = getPlayerFromCursor(StatsEntry.CONTENT_URI_TEMP, playerFirestoreID);
        String name = inGamePlayer.getName();
        int tHR = inGamePlayer.getHrs();
        int tRBI = inGamePlayer.getRbis();
        int tRun = inGamePlayer.getRuns();
        int t1b = inGamePlayer.getSingles();
        int t2b = inGamePlayer.getDoubles();
        int t3b = inGamePlayer.getTriples();
        int tOuts = inGamePlayer.getOuts();

        Player permanentPlayer = getPlayerFromCursor(StatsEntry.CONTENT_URI_PLAYERS, playerFirestoreID);
        int pRBI = permanentPlayer.getRbis();
        int pRun = permanentPlayer.getRuns();
        int p1b = permanentPlayer.getSingles();
        int p2b = permanentPlayer.getDoubles();
        int p3b = permanentPlayer.getTriples();
        int pHR = permanentPlayer.getHrs();
        int pOuts = permanentPlayer.getOuts();

        int displayHR = tHR + pHR;
        int displayRBI = tRBI + pRBI;
        int displayRun = tRun + pRun;
        int singles = t1b + p1b;
        int doubles = t2b + p2b;
        int triples = t3b + p3b;
        int playerOuts = tOuts + pOuts;
        double avg = calculateAverage(singles, doubles, triples, displayHR, playerOuts);
        String avgString;
        if (Double.isNaN(avg)) {
            avgString = "---";
        } else {
            avgString = formatter.format(avg);
        }

        String nowBattingString = getString(R.string.nowbatting) + " " + name;
        String avgDisplayText = "AVG: " + avgString;
        String hrDisplayText = "HR: " + displayHR;
        String rbiDisplayText = "RBI: " + displayRBI;
        String runDisplayText = "R: " + displayRun;

        batterText.setText(name);
        nowBatting.setText(nowBattingString);
        avgDisplay.setText(avgDisplayText);
        hrDisplay.setText(hrDisplayText);
        rbiDisplay.setText(rbiDisplayText);
        runDisplay.setText(runDisplayText);
        batterDisplay.setVisibility(View.VISIBLE);

        setUndoButton();
        setRedoButton();
        setScoreDisplay();
    }


    protected void setScoreDisplay() {
        String scoreString = awayTeamName + " " + awayTeamRuns + "    " + homeTeamName + " " + homeTeamRuns;
        scoreboard.setText(scoreString);
    }

    protected void setInningDisplay() {
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

    protected void updatePlayerStats(String action, int n) {
        String playerFirestoreID;
        if (undoRedo) {
            if (tempBatter == null) {
                return;
            }
            playerFirestoreID = tempBatter;

        } else {
            if (currentBatter == null) {
                return;
            }
            playerFirestoreID = currentBatter.getFirestoreID();
        }
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = {playerFirestoreID};
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP,
                null, selection, selectionArgs, null);
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

        getContentResolver().update(StatsEntry.CONTENT_URI_TEMP, values, selection, selectionArgs);

        if (rbiCount > 0) {
            for (String player : currentRunsLog) {
                updatePlayerRuns(player, n);
            }
        }
        cursor.close();
        if (isTeamAlternate()) {
            return;
        }
        setDisplays();
    }

    protected abstract boolean isTeamAlternate();


    protected void updatePlayerRuns(String player, int n) {
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
            if (isTopOfInning()) {
                awayTeamRuns++;
            } else if (!isTopOfInning()) {
                homeTeamRuns++;
            }
        }
    }

    protected void enableSubmitButton() {
        submitPlay.setEnabled(true);
        submitPlay.getBackground().setAlpha(255);
    }

    protected void disableSubmitButton() {
        submitPlay.setEnabled(false);
        submitPlay.getBackground().setAlpha(64);
    }

    protected void disableResetButton() {
        resetBases.setEnabled(false);
        resetBases.getBackground().setAlpha(64);
    }

    protected void enableResetButton() {
        resetBases.setEnabled(true);
        resetBases.getBackground().setAlpha(255);
    }

    protected void resetBases(BaseLog baseLog) {
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
        disableSubmitButton();
        disableResetButton();
        setBaseListeners();
        tempOuts = 0;
        tempRuns = 0;
        String outs = gameOuts + " outs";
        outsDisplay.setText(outs);
        setScoreDisplay();
    }

    protected void emptyBases() {
        firstDisplay.setText("");
        secondDisplay.setText("");
        thirdDisplay.setText("");
    }

    protected double calculateAverage(int singles, int doubles, int triples, int hrs, int outs) {
        double hits = (double) (singles + doubles + triples + hrs);
        return (hits / (outs + hits));
    }

    protected void onSubmit() {
        disableSubmitButton();
        if (undoRedo) {
            deleteGameLogs();
            currentRunsLog.clear();
            currentRunsLog.addAll(tempRunsLog);
        }
        updatePlayerStats(result, 1);
        gameOuts += tempOuts;
        nextBatter();
        String outs = gameOuts + " outs";
        outsDisplay.setText(outs);
    }

    protected void deleteGameLogs() {
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                null, null, null);
        gameCursor.moveToPosition(gameLogIndex);
        int id = StatsContract.getColumnInt(gameCursor, StatsEntry._ID);
        Uri toDelete = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_GAMELOG, id);
        getContentResolver().delete(toDelete, null, null);
        undoRedo = false;
        redoEndsGame = false;
    }

    protected abstract void undoPlay();

    protected String getUndoPlayResult() {
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                null, null, null);
        gameCursor.moveToPosition(gameLogIndex);
        undoRedo = true;
        tempBatter = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_BATTER);
        inningChanged = StatsContract.getColumnInt(gameCursor, StatsEntry.COLUMN_INNING_CHANGED);
        return StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_PLAY);
    }

    protected void undoLogs() {
        reloadRunsLog();
        gameCursor.moveToPrevious();
        reloadBaseLog();
        awayTeamRuns = currentBaseLogStart.getAwayTeamRuns();
        homeTeamRuns = currentBaseLogStart.getHomeTeamRuns();
        if (!tempRunsLog.isEmpty()) {
            tempRunsLog.clear();
        }
        gameOuts = currentBaseLogStart.getOutCount();
        currentBatter = currentBaseLogStart.getBatter();
    }

    protected abstract void redoPlay();

    protected String getRedoResult() {
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                null, null, null);

        if (gameLogIndex < gameCursor.getCount() - 1) {
            undoRedo = true;
            gameLogIndex++;
        } else {
            return null;
        }
        gameCursor.moveToPosition(gameLogIndex);

        reloadRunsLog();
        reloadBaseLog();
        awayTeamRuns = currentBaseLogStart.getAwayTeamRuns();
        homeTeamRuns = currentBaseLogStart.getHomeTeamRuns();
        gameOuts = currentBaseLogStart.getOutCount();
        currentBatter = currentBaseLogStart.getBatter();
        tempBatter = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_BATTER);
        if (!tempRunsLog.isEmpty()) {
            tempRunsLog.clear();
        }
        inningChanged = StatsContract.getColumnInt(gameCursor, StatsEntry.COLUMN_INNING_CHANGED);
        return StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_PLAY);
    }

    protected void reloadBaseLog() {
        String batterID = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_ONDECK);
        List<Player> teamLineup = getTeamLineup();
        Player batter = findBatterByID(batterID, teamLineup);
        currentBaseLogStart = new BaseLog(gameCursor, batter, teamLineup);
    }

    protected abstract List<Player> getTeamLineup();

    protected Player findBatterByID(String batterID, List<Player> teamLineup) {
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

    protected void reloadRunsLog() {
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

    @Override
    public void finishGame(boolean isOver) {
        if (isOver) {
            endGame();
        } else {
            if (!redoEndsGame) {
                updateGameLogs();
                redoEndsGame = true;
            }
            undoPlay();
        }
    }

    protected abstract boolean isTopOfInning();

    protected class MyDragListener implements View.OnDragListener {
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
                                enableSubmitButton();
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
                                enableSubmitButton();
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
                        if (isTopOfInning()) {
                            scoreString = awayTeamName + " " + (awayTeamRuns + tempRuns) + "    "
                                    + homeTeamName + " " + homeTeamRuns;
                        } else {
                            scoreString = awayTeamName + " " + awayTeamRuns + "    "
                                    + homeTeamName + " " + (homeTeamRuns + tempRuns);
                        }
                        scoreboard.setText(scoreString);
                    }
                    enableResetButton();
                    setBaseListeners();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    View dragView = (View) event.getLocalState();
                    dragView.setAlpha(1f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        switch (v.getId()) {
                            case R.id.home_display:
                                v.setBackground(getDrawable(R.drawable.img_home));
                                break;
                            case R.id.trash:
                                v.setBackgroundResource(0);
                                break;
                            default:
                                v.setBackground(getDrawable(R.drawable.img_base));
                        }
                    }
                    break;
            }
            return true;
        }
    }

    protected final class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                setBaseListeners();
                view.setAlpha(.1f);
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

    protected abstract void increaseLineupIndex();

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
                actionEditLineup();
                break;
            case R.id.action_goto_stats:
                actionViewBoxScore();
                break;
            case R.id.action_exit_game:
                showExitDialog();
                break;
            case R.id.action_finish_game:
                showFinishConfirmationDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void actionViewBoxScore() {
        Intent statsIntent = new Intent(GameActivity.this, BoxScoreActivity.class);
        Bundle b = getBoxScoreBundle();
        statsIntent.putExtras(b);
        startActivity(statsIntent);
    }

    protected abstract Bundle getBoxScoreBundle();

    protected void showExitDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = SaveDeleteGameFragment.newInstance();
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void exitGameChoice(boolean save) {
        if (!save) {
            getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
            getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
            SharedPreferences savedGamePreferences = getSharedPreferences(selectionID + StatsEntry.GAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = savedGamePreferences.edit();
            editor.clear();
            editor.commit();
        }
        exitToManager();
    }

    protected abstract void exitToManager();

    protected abstract void actionEditLineup();

    protected void setUndoButton() {
        boolean undo = gameLogIndex > 0;
        undoButton.setClickable(undo);
        if (undo) {
            undoButton.setAlpha(1f);
        } else {
            undoButton.setAlpha(.1f);
        }
    }

    protected void setRedoButton() {
        boolean redo = gameLogIndex < highestIndex;
        redoButton.setClickable(redo);
        if (redo) {
            redoButton.setAlpha(1f);
        } else {
            redoButton.setAlpha(.1f);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem undoItem = menu.findItem(R.id.action_undo_play);
        MenuItem redoItem = menu.findItem(R.id.action_redo_play);

        boolean undo = gameLogIndex > 0;
        boolean redo = gameLogIndex < highestIndex;

        undoItem.setVisible(undo);
        redoItem.setVisible(redo);

        return true;
    }

    protected void showFinishConfirmationDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = FinishGameConfirmationDialogFragment.newInstance();
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void finishEarly() {
        if (undoRedo) {
            deleteGameLogs();
        }
        endGame();
    }


    protected String getTeamNameFromFirestoreID(String firestoreID) {
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = new String[]{firestoreID};
        String[] projection = new String[]{StatsEntry.COLUMN_NAME};

        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                projection, selection, selectionArgs, null);
        String name = null;
        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            name = cursor.getString(nameIndex);
        }
        cursor.close();
        return name;
    }
}