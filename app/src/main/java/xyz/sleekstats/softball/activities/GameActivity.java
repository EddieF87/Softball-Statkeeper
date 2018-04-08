package xyz.sleekstats.softball.activities;

import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.dialogs.EndOfGameDialog;
import xyz.sleekstats.softball.dialogs.FinishGameConfirmationDialog;
import xyz.sleekstats.softball.dialogs.GameSettingsDialog;
import xyz.sleekstats.softball.dialogs.SaveDeleteGameDialog;
import xyz.sleekstats.softball.objects.BaseLog;

import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.objects.Player;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class GameActivity extends AppCompatActivity
        implements EndOfGameDialog.OnFragmentInteractionListener,
        SaveDeleteGameDialog.OnFragmentInteractionListener,
        FinishGameConfirmationDialog.OnFragmentInteractionListener,
        GameSettingsDialog.OnFragmentInteractionListener {

    Cursor gameCursor;
    private static final String TAG = "UNDOREDOFIX";

    TextView scoreboardAwayName;
    TextView scoreboardHomeName;
    TextView scoreboardAwayScore;
    TextView scoreboardHomeScore;
    TextView nowBatting;
    TextView outsDisplay;
    TextView avgDisplay;
    TextView rbiDisplay;
    TextView runDisplay;
    TextView hrDisplay;
    private TextView inningDisplay;
    private ImageView inningTopArrow;
    private ImageView inningBottomArrow;
    private ImageView undoButton;
    private ImageView redoButton;

    private Button submitPlay;
    private Button resetBases;

    private RadioGroup group1;
    private RadioGroup group2;
    private String result;

    private RelativeLayout batterDisplay;
    private TextView batterText;
    TextView firstDisplay;
    TextView secondDisplay;
    TextView thirdDisplay;
    private TextView homeDisplay;
    private ImageView outTrash;

    TextView step1View;
    TextView step2View;
    TextView step3View;
    TextView step4View;


    String awayTeamName;
    String homeTeamName;
    int awayTeamRuns;
    int homeTeamRuns;

    String tempBatter;
    int inningChanged = 0;
    int inningNumber = 2;
    int gameOuts = 0;
    private int tempOuts;
    private int tempRuns;

    Player currentBatter;
    private Drawable mRunner;

    private final NumberFormat formatter = new DecimalFormat("#.000");
    BaseLog currentBaseLogStart;
    ArrayList<String> currentRunsLog;
    ArrayList<String> tempRunsLog;

    int gameLogIndex = 0;
    int lowestIndex = 0;
    int highestIndex = 0;
    boolean undoRedo = false;

    boolean finalInning;
    boolean redoEndsGame = false;

    private boolean playEntered = false;
    private boolean batterMoved = false;
    private boolean firstOccupied = false;
    private boolean secondOccupied = false;
    private boolean thirdOccupied = false;
    private boolean mResetListeners = false;
    int totalInnings;

    static final String KEY_GAMELOGINDEX = "keyGameLogIndex";
    static final String KEY_LOWESTINDEX = "keyLowestIndex";
    static final String KEY_HIGHESTINDEX = "keyHighestIndex";
    static final String KEY_GENDERSORT = "keyGenderSort";
    static final String KEY_FEMALEORDER = "keyFemaleOrder";
    static final String KEY_INNINGNUMBER = "keyInningNumber";
    static final String KEY_TOTALINNINGS = "keyTotalInnings";
    static final String KEY_UNDOREDO = "keyUndoRedo";
    static final String KEY_REDOENDSGAME = "redoEndsGame";
    private static final String DIALOG_FINISH = "DialogFinish";
    public static final int RESULT_CODE_GAME_FINISHED = 222;
    public static final int REQUEST_CODE_GAME = 111;
    public static final int RESULT_CODE_EDITED = 444;
    public static final int REQUEST_CODE_EDIT = 333;

    String mSelectionID;
    Toast mCurrentToast;
    MyTouchListener myTouchListener = new MyTouchListener();
    MyDragListener myDragListener = new MyDragListener();
    private boolean gameHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSelectionData();
        setCustomViews();
        setViews();
        Log.d("megaman", "game onCreate");

        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mSelectionID};
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                selection, selectionArgs, null);
        if (gameCursor.moveToFirst()) {
            Log.d("megaman", "ameCursor.moveToFirst");
            loadGamePreferences();
            Bundle args = getIntent().getExtras();
            if (args != null) {
                if (args.getBoolean("edited") && undoRedo) {
                    deleteGameLogs();
                    highestIndex = gameLogIndex;
                    invalidateOptionsMenu();
                    setUndoRedo();
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

    private void setViews() {
        //todo
//        AdView adView = findViewById(R.id.game_ad);
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                .build();
//        adView.loadAd(adRequest);

        nowBatting = findViewById(R.id.nowbatting);
        outsDisplay = findViewById(R.id.num_of_outs);
        avgDisplay = findViewById(R.id.avgdisplay);
        rbiDisplay = findViewById(R.id.rbidisplay);
        runDisplay = findViewById(R.id.rundisplay);
        hrDisplay = findViewById(R.id.hrdisplay);
        inningDisplay = findViewById(R.id.inning);
        inningTopArrow = findViewById(R.id.inning_top_arrow);
        inningBottomArrow = findViewById(R.id.inning_bottom_arrow);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mRunner = getDrawable(R.drawable.ic_directions_run_black_18dp);
            mRunner.setAlpha(25);
        }

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
        outTrash = findViewById(R.id.trash);
        batterDisplay.setOnTouchListener(myTouchListener);
        firstDisplay.setOnDragListener(myDragListener);
        secondDisplay.setOnDragListener(myDragListener);
        thirdDisplay.setOnDragListener(myDragListener);
        homeDisplay.setOnDragListener(myDragListener);
        outTrash.setOnDragListener(myDragListener);
    }

    protected abstract void loadGamePreferences();

    protected abstract void setCustomViews();

    ArrayList<Player> setTeam(String teamID) {
        String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{teamID, mSelectionID};
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

    List<Player> genderSort(List<Player> team, int femaleRequired) {
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
            case R.id.single_rb:
                if (checked)
                    result = StatsEntry.COLUMN_1B;
                group2.clearCheck();
                break;
            case R.id.dbl_rb:
                if (checked)
                    result = StatsEntry.COLUMN_2B;
                group2.clearCheck();
                break;
            case R.id.triple_rb:
                if (checked)
                    result = StatsEntry.COLUMN_3B;
                group2.clearCheck();
                break;
            case R.id.hr_rb:
                if (checked)
                    result = StatsEntry.COLUMN_HR;
                group2.clearCheck();
                break;
            case R.id.bb_rb:
                if (checked)
                    result = StatsEntry.COLUMN_BB;
                group2.clearCheck();
                break;
            case R.id.out_rb:
                if (checked)
                    result = StatsEntry.COLUMN_OUT;
                group1.clearCheck();
                break;
            case R.id.error_rb:
                if (checked)
                    result = StatsEntry.COLUMN_ERROR;
                group1.clearCheck();
                break;
            case R.id.fc_rb:
                if (checked)
                    result = StatsEntry.COLUMN_FC;
                group1.clearCheck();
                break;
            case R.id.sf_rb:
                if (checked)
                    result = StatsEntry.COLUMN_SF;
                group1.clearCheck();
                break;
            case R.id.sacbunt_rb:
                if (checked)
                    result = StatsEntry.COLUMN_SAC_BUNT;
                group1.clearCheck();
                break;
        }
        if (batterMoved) {
            enableSubmitButton();
        }
    }

    private void setBaseListeners() {
        Log.d("zztop", "setBaseListeners");
        if (firstDisplay.getText().toString().isEmpty()) {
            firstDisplay.setOnTouchListener(null);
            firstDisplay.setOnDragListener(myDragListener);
            firstOccupied = false;
        } else {
            firstDisplay.setOnTouchListener(myTouchListener);
            firstDisplay.setOnDragListener(null);
            firstOccupied = true;
        }

        if (secondDisplay.getText().toString().isEmpty()) {
            secondDisplay.setOnTouchListener(null);
            secondDisplay.setOnDragListener(myDragListener);
            secondOccupied = false;
        } else {
            secondDisplay.setOnTouchListener(myTouchListener);
            secondDisplay.setOnDragListener(null);
            secondOccupied = true;
        }

        if (thirdDisplay.getText().toString().isEmpty()) {
            thirdDisplay.setOnTouchListener(null);
            thirdDisplay.setOnDragListener(myDragListener);
            thirdOccupied = false;
        } else {
            thirdDisplay.setOnTouchListener(myTouchListener);
            thirdDisplay.setOnDragListener(null);
            thirdOccupied = true;
        }
        batterDisplay.setOnTouchListener(myTouchListener);
        homeDisplay.setOnDragListener(myDragListener);
    }

    void startGame() {
        SharedPreferences sharedPreferences = getSharedPreferences(mSelectionID + StatsContract.StatsEntry.SETTINGS, Context.MODE_PRIVATE);
        gameHelp = sharedPreferences.getBoolean(StatsEntry.HELP, true);
        if (gameHelp) {
            step1View = findViewById(R.id.step1text);
            step2View = findViewById(R.id.step2text);
            step3View = findViewById(R.id.step3text);
            step4View = findViewById(R.id.step4text);
            step1View.setVisibility(View.VISIBLE);
            step2View.setVisibility(View.VISIBLE);
            step3View.setVisibility(View.VISIBLE);
            step4View.setVisibility(View.VISIBLE);
            submitPlay.setOnClickListener(null);

            submitPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    step1View.setVisibility(View.GONE);
                    step2View.setVisibility(View.GONE);
                    step3View.setVisibility(View.GONE);
                    step4View.setVisibility(View.GONE);
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
    }

    protected abstract void resumeGame();

    void nextBatter() {
        if (!isTopOfInning() && finalInning && homeTeamRuns > awayTeamRuns) {
            if (isLeagueGameOrHomeTeam()) {
                increaseLineupIndex();
            }
            showFinishGameDialog();
            return;
        }
        if (gameOuts >= 3) {
            if (!isTopOfInning() && finalInning && awayTeamRuns > homeTeamRuns) {
                increaseLineupIndex();
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

    void clearTempState() {
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

    void showToast(String text)
    {
        if(mCurrentToast == null)
        {
            mCurrentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        }
        mCurrentToast.setText(text);
        mCurrentToast.setDuration(Toast.LENGTH_SHORT);
        mCurrentToast.show();
    }

    void enterGameValues(BaseLog currentBaseLogEnd, int team,
                         String previousBatterID, String onDeckID) {

        String first = currentBaseLogEnd.getBasepositions()[0];
        String second = currentBaseLogEnd.getBasepositions()[1];
        String third = currentBaseLogEnd.getBasepositions()[2];

        ContentValues values = new ContentValues();
        values.put(StatsEntry.COLUMN_LEAGUE_ID, mSelectionID);
        values.put(StatsEntry.COLUMN_PLAY, result);
        values.put(StatsEntry.COLUMN_TEAM, team);
        values.put(StatsEntry.COLUMN_BATTER, previousBatterID);
        values.put(StatsEntry.COLUMN_ONDECK, onDeckID);
        values.put(StatsEntry.COLUMN_INNING_CHANGED, inningChanged);
        values.put(StatsEntry.INNINGS, inningNumber);
        values.put(StatsEntry.COLUMN_OUT, gameOuts);
        Log.d(TAG, gameLogIndex +  " " + values.toString());
        values.put(StatsEntry.COLUMN_1B, first);
        values.put(StatsEntry.COLUMN_2B, second);
        values.put(StatsEntry.COLUMN_3B, third);
        values.put(StatsEntry.COLUMN_AWAY_RUNS, awayTeamRuns);
        values.put(StatsEntry.COLUMN_HOME_RUNS, homeTeamRuns);

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


    private void endGame() {
        Log.d("megaman", "endGame");
//        firestoreUpdate();

//        deleteTempData();

        sendResultToMgr();
    }

    protected void transferStats(long gameID){
        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mSelectionID};
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, null);
        while (cursor.moveToNext()) {
            Log.d("zztop", "player backup");

            String playerFirestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
            String teamFirestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_TEAM_FIRESTORE_ID);
            int playerId = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_PLAYERID);
            int game1b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_1B);
            int game2b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_2B);
            int game3b = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_3B);
            int gameHR = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HR);
            int gameRBI = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RBI);
            int gameRun = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUN);
            int gameBB = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_BB);
            int gameOuts = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_OUT);
            int gameSF = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_SF);

            ContentValues backupValues = new ContentValues();
            backupValues.put(StatsEntry.COLUMN_LEAGUE_ID, mSelectionID);
            backupValues.put(StatsEntry.COLUMN_GAME_ID, gameID);
            backupValues.put(StatsEntry.COLUMN_FIRESTORE_ID, playerFirestoreID);
            backupValues.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamFirestoreID);
            backupValues.put(StatsEntry.COLUMN_PLAYERID, playerId);
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

    void showFinishGameDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag(DIALOG_FINISH);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);

        DialogFragment newFragment = EndOfGameDialog.newInstance(homeTeamName, awayTeamName, homeTeamRuns, awayTeamRuns);
        newFragment.show(fragmentTransaction, DIALOG_FINISH);
    }

    private Player getPlayerFromCursor(Uri uri, String playerFirestoreID) {
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = {playerFirestoreID, mSelectionID};
        Cursor cursor = getContentResolver().query(uri, null,
                selection, selectionArgs, null);
        cursor.moveToFirst();
        boolean tempData = uri.equals(StatsEntry.CONTENT_URI_TEMP);
        Player player = new Player(cursor, tempData);
        cursor.close();
        return player;
    }

    //sets the textview displays with updated player/game data
    void setDisplays() {
        Log.d("pinkfloyd", "setDisplays");

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

        setUndoRedo();
        setScoreDisplay();
    }


    void setScoreDisplay() {
        scoreboardAwayScore.setText(String.valueOf(awayTeamRuns));
        scoreboardHomeScore.setText(String.valueOf(homeTeamRuns));
    }

    void setInningDisplay() {
        String topOrBottom;
        int scoreboardColor = ContextCompat.getColor(this, R.color.colorScoreboard);
        int atBatColor = ContextCompat.getColor(this, R.color.colorHighlight);
        if (inningNumber % 2 == 0) {
            inningTopArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorScoreboard));
            inningBottomArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cardview_dark_background));
            topOrBottom = "Top";
            scoreboardAwayName.setTextColor(atBatColor);
            scoreboardAwayScore.setTextColor(atBatColor);
            scoreboardHomeName.setTextColor(scoreboardColor);
            scoreboardHomeScore.setTextColor(scoreboardColor);
        } else {
            inningTopArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cardview_dark_background));
            inningBottomArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorScoreboard));
            topOrBottom = "Bottom";
            scoreboardAwayName.setTextColor(scoreboardColor);
            scoreboardAwayScore.setTextColor(scoreboardColor);
            scoreboardHomeName.setTextColor(atBatColor);
            scoreboardHomeScore.setTextColor(atBatColor);
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
        String inningString = topOrBottom + " of the " + inningNumber / 2 + indicator;
        showToast(inningString);
    }

    void updatePlayerStats(String action, int n) {
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
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = {playerFirestoreID, mSelectionID};
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
            case StatsEntry.COLUMN_OUT:
            case StatsEntry.COLUMN_ERROR:
            case StatsEntry.COLUMN_FC:
                newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_OUT) + n;
                values.put(StatsEntry.COLUMN_OUT, newValue);
                break;
            case StatsEntry.COLUMN_BB:
                newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_BB) + n;
                values.put(StatsEntry.COLUMN_BB, newValue);
                break;
            case StatsEntry.COLUMN_SF:
                newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_SF) + n;
                values.put(StatsEntry.COLUMN_SF, newValue);
                break;
            case StatsEntry.COLUMN_SAC_BUNT:
                break;
            default:
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
    }

    protected abstract boolean isTeamAlternate();


    private void updatePlayerRuns(String player, int n) {
        String selection = StatsEntry.COLUMN_NAME + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = {player, mSelectionID};
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, null
        );
        if (cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            int newValue = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_RUN) + n;
            values.put(StatsEntry.COLUMN_RUN, newValue);
            getContentResolver().update(StatsEntry.CONTENT_URI_TEMP, values, selection, selectionArgs);
        }
        if (!undoRedo) {
            if (isTopOfInning()) {
                awayTeamRuns++;
            } else if (!isTopOfInning()) {
                homeTeamRuns++;
            }
        }
    }

    private void enableSubmitButton() {
        submitPlay.setEnabled(true);
        submitPlay.getBackground().setAlpha(255);
    }

    private void enableResetButton() {
        resetBases.setEnabled(true);
        resetBases.getBackground().setAlpha(255);
    }

    private void disableSubmitButton() {
        submitPlay.setEnabled(false);
        submitPlay.getBackground().setAlpha(64);
    }

    private void disableResetButton() {
        resetBases.setEnabled(false);
        resetBases.getBackground().setAlpha(64);
    }

    void resetBases(BaseLog baseLog) {
        String[] bases = baseLog.getBasepositions();
        String first = bases[0];
        String second = bases[1];
        String third = bases[2];
        firstDisplay.setText(first);
        secondDisplay.setText(second);
        thirdDisplay.setText(third);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(first != null && !first.isEmpty()) {
                firstDisplay.setForeground(mRunner);
            } else {
                firstDisplay.setForeground(null);
            }
            if(second != null && !second.isEmpty()) {
                secondDisplay.setForeground(mRunner);
            } else {
                secondDisplay.setForeground(null);
            }
            if(third != null && !third.isEmpty()) {
                thirdDisplay.setForeground(mRunner);
            } else {
                thirdDisplay.setForeground(null);
            }
        }
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

    void emptyBases() {
        firstDisplay.setText(null);
        secondDisplay.setText(null);
        thirdDisplay.setText(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            firstDisplay.setForeground(null);
            secondDisplay.setForeground(null);
            thirdDisplay.setForeground(null);
        }
    }

    private double calculateAverage(int singles, int doubles, int triples, int hrs, int outs) {
        double hits = (double) (singles + doubles + triples + hrs);
        return (hits / (outs + hits));
    }

    private void onSubmit() {
        disableSubmitButton();
        if (undoRedo) {
            deleteGameLogs();
            currentRunsLog.clear();
            currentRunsLog.addAll(tempRunsLog);
        }
        if(result == null) {
            Toast.makeText(GameActivity.this, "Please choose a result!", Toast.LENGTH_SHORT).show();
            return;
        }
        updatePlayerStats(result, 1);
        gameOuts += tempOuts;
        nextBatter();
        String outs = gameOuts + " outs";
        outsDisplay.setText(outs);
    }

    void deleteGameLogs() {
        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mSelectionID};
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                selection, selectionArgs, null);
        gameCursor.moveToPosition(gameLogIndex);
        int id = StatsContract.getColumnInt(gameCursor, StatsEntry._ID);
        Uri toDelete = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_GAMELOG, id);
        getContentResolver().delete(toDelete, selection, selectionArgs);
        undoRedo = false;
        redoEndsGame = false;
    }

    protected abstract void undoPlay();

    String getUndoPlayResult() {
        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mSelectionID};
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                selection, selectionArgs, null);
        gameCursor.moveToPosition(gameLogIndex);
        undoRedo = true;
        tempBatter = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_BATTER);
        inningChanged = StatsContract.getColumnInt(gameCursor, StatsEntry.COLUMN_INNING_CHANGED);
        return StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_PLAY);
    }

    void undoLogs() {
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

    String getRedoResult() {
        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mSelectionID};
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                selection, selectionArgs, null);

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

    void reloadBaseLog() {
        String batterID = StatsContract.getColumnString(gameCursor, StatsEntry.COLUMN_ONDECK);
        List<Player> teamLineup = getTeamLineup();
        Player batter = findBatterByID(batterID, teamLineup);
        currentBaseLogStart = new BaseLog(gameCursor, batter, teamLineup);
    }

    protected abstract List<Player> getTeamLineup();

    private Player findBatterByID(String batterID, List<Player> teamLineup) {
        for (Player player : teamLineup) {
            if (player.getFirestoreID().equals(batterID)) {
                return player;
            }
        }
        return null;
    }

    void reloadRunsLog() {
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

    @Override
    protected void onDestroy() {
        firstDisplay.setOnDragListener(null);
        secondDisplay.setOnDragListener(null);
        thirdDisplay.setOnDragListener(null);
        homeDisplay.setOnDragListener(null);
        outTrash.setOnDragListener(null);
        batterDisplay.setOnTouchListener(null);
        firstDisplay.setOnTouchListener(null);
        secondDisplay.setOnTouchListener(null);
        thirdDisplay.setOnTouchListener(null);
        homeDisplay.setOnTouchListener(null);
        super.onDestroy();
    }

    protected abstract boolean isTopOfInning();

    class MyDragListener implements View.OnDragListener {


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
                    Log.d("zztop", "ACTION_DRAG_STARTED");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        switch (v.getId()) {
                            case R.id.home_display:
                                v.setBackground(getDrawable(R.drawable.img_home2));
                                break;
                            case R.id.trash:
                                v.setBackground(getDrawable(R.drawable.img_base2));
                                break;
                            default:
                                v.setBackground(getDrawable(R.drawable.img_base2));
                                break;
                        }
                    }
                    break;
                case DragEvent.ACTION_DROP:
                    batterDisplay.setOnTouchListener(null);
                    firstDisplay.setOnTouchListener(null);
                    secondDisplay.setOnTouchListener(null);
                    thirdDisplay.setOnTouchListener(null);
                    homeDisplay.setOnTouchListener(null);
                    Log.d("zztop", "ACTION_DROP");
                    String movedPlayer = "";
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
                                break;
                        }
                    }
                    if (v.getId() == R.id.trash) {
                        if (eventView instanceof TextView) {
                            TextView draggedView = (TextView) eventView;
                            draggedView.setText(null);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                draggedView.setForeground(null);
                            }

                        } else {
                            batterDisplay.setVisibility(View.INVISIBLE);
                            batterMoved = true;
                            if (playEntered) {
                                enableSubmitButton();
//                                Log.d("zztop", "enableSubmitButton");
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                dropPoint.setForeground(mRunner);
                            }
                            draggedView.setText(null);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                draggedView.setForeground(null);
                            }
                            draggedView.setAlpha(1);
                        } else {
                            String currentBatterString = currentBatter.getName();
                            dropPoint.setText(currentBatterString);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                dropPoint.setForeground(mRunner);
                            }
                            batterDisplay.setVisibility(View.INVISIBLE);
                            batterMoved = true;
                            if (playEntered) {
                                enableSubmitButton();
                            }
                        }
                        dropPoint.setAlpha(1);

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
                            homeDisplay.setText(null);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                homeDisplay.setForeground(null);
                            }
                            tempRuns++;
                            String scoreString;
                            if (isTopOfInning()) {
                                scoreString = String.valueOf(awayTeamRuns + tempRuns);
                                scoreboardAwayScore.setText(scoreString);
                            } else {
                                scoreString = String.valueOf(homeTeamRuns + tempRuns);
                                scoreboardHomeScore.setText(scoreString);
                            }
                        }
                    }
                    enableResetButton();
                    mResetListeners = true;
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    Log.d("zztop", "ACTION_DRAG_ENDED1");
                    View dragView = (View) event.getLocalState();
                    if(dragView != null) {
                        dragView.setAlpha(1f);
                        Log.d("zztop", "dragView != null");
                    } else {
                        Log.d("zztop", "dragView == NULLLLLLLLLLLLLL");
                    }
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
                    if(mResetListeners) {
                        mResetListeners = false;
                        setBaseListeners();
                    }
                    Log.d("zztop", "ACTION_DRAG_ENDED2");
                    break;
            }
            return true;
        }
    }

    final class MyTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            Log.d("zztop", "onTouch");
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d("zztop", "onTouchDOWN");
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

    protected abstract void gotoLineupEditor(String teamName, String teamID);

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
            case R.id.change_game_settings:
                int innings = totalInnings;
                int genderSorter = 0;
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DialogFragment newFragment = GameSettingsDialog.newInstance(innings, genderSorter, mSelectionID, inningNumber, gameHelp);
                newFragment.show(fragmentTransaction, "");
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

    private void actionViewBoxScore() {
        Intent statsIntent = new Intent(GameActivity.this, BoxScoreActivity.class);
        Bundle b = getBoxScoreBundle();
        statsIntent.putExtras(b);
        startActivity(statsIntent);
    }

    protected abstract Bundle getBoxScoreBundle();

    private void showExitDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = SaveDeleteGameDialog.newInstance();
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void exitGameChoice(boolean save) {
        if (!save) {
            deleteTempData();
            SharedPreferences savedGamePreferences = getSharedPreferences(mSelectionID + StatsEntry.GAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = savedGamePreferences.edit();
            editor.clear();
            editor.apply();
        }
        finish();
    }

    private void deleteTempData(){
        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mSelectionID};
        getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, selection, selectionArgs);
        getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, selection, selectionArgs);
    }

    protected abstract void sendResultToMgr();

    protected abstract void actionEditLineup();

    void setUndoRedo(){
        setUndoButton();
        setRedoButton();
    }

    void setUndoButton() {
        boolean undo = gameLogIndex > lowestIndex;
        undoButton.setClickable(undo);
        if (undo) {
            undoButton.setAlpha(1f);
        } else {
            undoButton.setAlpha(.1f);
        }
    }

    void setRedoButton() {
        boolean redo = gameLogIndex < highestIndex;
        redoButton.setClickable(redo);
        Log.d("phil", "redoButton.setClickable(" + redo + ")   gameLogIndex=" + gameLogIndex + "   highestIndex=" + highestIndex);
        if (redo) {
            redoButton.setAlpha(1f);
        } else {
            redoButton.setAlpha(.1f);
        }
    }

    //warning dialog

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem undoItem = menu.findItem(R.id.action_undo_play);
        MenuItem redoItem = menu.findItem(R.id.action_redo_play);

        boolean undo = gameLogIndex > lowestIndex;
        boolean redo = gameLogIndex < highestIndex;

        undoItem.setVisible(undo);
        redoItem.setVisible(redo);

        return true;
    }

    private void showFinishConfirmationDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = FinishGameConfirmationDialog.newInstance();
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void finishEarly() {
        if (undoRedo) {
            deleteGameLogs();
        }
        endGame();
    }


    String getTeamNameFromFirestoreID(String firestoreID) {
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{firestoreID, mSelectionID};
        String[] projection = new String[]{StatsEntry.COLUMN_NAME};

        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                projection, selection, selectionArgs, null);
        String name = null;
        if (cursor.moveToFirst()) {
            name = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_NAME);
        }
        cursor.close();
        return name;
    }

    @Override
    public void onGameSettingsChanged(int innings, int genderSorter) {
        totalInnings = innings;
        SharedPreferences gamePreferences = getSharedPreferences(mSelectionID + StatsEntry.GAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = gamePreferences.edit();
        editor.putInt(KEY_TOTALINNINGS, totalInnings);
        editor.apply();

        if (inningNumber / 2 >= totalInnings) {
            finalInning = true;
        } else {
            finalInning = false;
            redoEndsGame = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("megaman", "game onActivityResult");
        if(requestCode == REQUEST_CODE_EDIT) {
            Log.d("megaman", "requestCode == REQUEST_CODE_EDIT");
            String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
            String[] selectionArgs = new String[]{mSelectionID};
            gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                    selection, selectionArgs, null);
            gameCursor.moveToFirst();
            if (resultCode == RESULT_CODE_EDITED) {
                Log.d("megaman", "resultCode == RESULT_CODE_EDITED");
                lowestIndex = gameLogIndex;
                getSelectionData();
                setCustomViews();
                setViews();
                if(undoRedo) {
                    deleteGameLogs();
                    highestIndex = gameLogIndex;
                    invalidateOptionsMenu();
                    setUndoRedo();
                }
                resumeGame();
                Toast.makeText(GameActivity.this, "Lineups have been edited.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}