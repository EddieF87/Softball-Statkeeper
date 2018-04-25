package xyz.sleekstats.softball.activities;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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
    TextView mercyDisplay;
    private TextView inningDisplay;
    private ImageView inningTopArrow;
    private ImageView inningBottomArrow;
    private ImageView undoButton;
    private ImageView redoButton;

    private Button submitPlay;
    private Button resetBasesBtn;

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
    int inningRuns;
    int mercyRuns;

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
    boolean gameHelp;

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
    public static final String KEY_GENDERSORT = "keyGenderSort";
    public static final String KEY_FEMALEORDER = "keyFemaleOrder";
    static final String KEY_INNINGNUMBER = "keyInningNumber";
    public static final String KEY_TOTALINNINGS = "keyTotalInnings";
    static final String KEY_UNDOREDO = "keyUndoRedo";
    static final String KEY_REDOENDSGAME = "redoEndsGame";
    private static final String DIALOG_FINISH = "DialogFinish";
    public static final int RESULT_CODE_GAME_FINISHED = 222;
    public static final int REQUEST_CODE_GAME = 111;
    public static final int RESULT_CODE_EDITED = 444;
    static final int REQUEST_CODE_EDIT = 333;
    public static final String AUTO_OUT = "AUTO-OUT";

    String mSelectionID;
    private Toast mCurrentToast;
    private final MyTouchListener myTouchListener = new MyTouchListener();
    private final MyDragListener myDragListener = new MyDragListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!getSelectionData()) {
            goToMain();
            return;
        }
        setCustomViews();
        setViews();

        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mSelectionID};
        gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                selection, selectionArgs, null);
        if (gameCursor.moveToFirst()) {
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

    protected abstract boolean getSelectionData();

    private void goToMain() {
        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setViews() {

        AdView adView = findViewById(R.id.game_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

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

        mercyDisplay = findViewById(R.id.mercy_display);
        mercyDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGameSettingsDialog();
            }
        });

        submitPlay = findViewById(R.id.submit);
        submitPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });
        disableSubmitButton();

        resetBasesBtn = findViewById(R.id.reset);
        resetBasesBtn.setOnClickListener(new View.OnClickListener() {
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
        if (femaleRequired == 0) {
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

    List<Player> addAutoOuts(List<Player> team, int femaleRequired) {
        if (femaleRequired < 1) {
            return team;
        }
        boolean firstPlayerMale =  team.get(0).getGender() == 0;
        String teamid = team.get(0).getTeamfirestoreid();
        int menInARow = 0;
        int womenInARow = 0;
        int menInARowToStart = 0;
        int womenInARowToStart = 0;
        boolean toStart = true;

        for (int i = 0; i < team.size(); i++) {

            Player player = team.get(i);
            if (player.getGender() == 1) {
                womenInARow++;
                menInARow = 0;
                if (firstPlayerMale) {
                    toStart = false;
                }
            } else {
                menInARow++;
                womenInARow = 0;
                if (!firstPlayerMale) {
                    toStart = false;
                }
            }

            if (womenInARow > 1) {
                team.add(i, new Player(AUTO_OUT, "(AUTO-OUT)", teamid, 0));
                womenInARow = 1;
                i++;
                toStart = false;
            }
            if (menInARow > femaleRequired - 1) {
                team.add(i, new Player(AUTO_OUT, "(AUTO-OUT)", teamid, 1));
                menInARow = 1;
                i++;
                toStart = false;
            }

            if (toStart) {
                if (womenInARow > 0) {
                    womenInARowToStart++;
                }
                if (menInARow > 0) {
                    menInARowToStart++;
                }
            }
        }

        if(menInARow + menInARowToStart > femaleRequired - 1) {
            team.add(new Player(AUTO_OUT, "(AUTO-OUT)", teamid, 1));
        }
        if(womenInARow + womenInARowToStart > 1) {
            team.add(new Player(AUTO_OUT, "(AUTO-OUT)", teamid, 0));
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
        SharedPreferences sharedPreferences = getSharedPreferences(mSelectionID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
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

    @SuppressLint("ShowToast")
    private void showToast(String text)
    {
        if(mCurrentToast == null) {
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
        values.put(StatsEntry.COLUMN_1B, first);
        values.put(StatsEntry.COLUMN_2B, second);
        values.put(StatsEntry.COLUMN_3B, third);
        values.put(StatsEntry.COLUMN_AWAY_RUNS, awayTeamRuns);
        values.put(StatsEntry.COLUMN_HOME_RUNS, homeTeamRuns);
        values.put(StatsEntry.COLUMN_INNING_RUNS, inningRuns);

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
        sendResultToMgr();
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
        newFragment.setCancelable(false);
        newFragment.show(fragmentTransaction, DIALOG_FINISH);
    }

    private Player getPlayerFromCursor(Uri uri, String playerFirestoreID) {
        if(playerFirestoreID.equals(AUTO_OUT)){
            return new Player(AUTO_OUT, "(AUTO-OUT)", "xxxx", 3);
        }
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
        setMercyDisplay(inningRuns);
    }

    private void setMercyDisplay(int runs) {
        String mercyRule;
        if(mercyRuns == 99) {
            mercyRule = "OFF";
        } else {
            mercyRule = runs + "/" + mercyRuns;
        }
        String inningRunString = "Mercy\n" + mercyRule;
        mercyDisplay.setText(inningRunString);
        if(runs >= mercyRuns) {
            mercyDisplay.setTextColor(Color.RED);
        } else {
            mercyDisplay.setTextColor(getResources().getColor(R.color.colorHighlight));
        }
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
//        mercyDisplay.setTextColor(getResources().getColor(R.color.colorHighlight));
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

        if(action == null) {
            return;
        }
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
            inningRuns++;
            if(inningRuns >= mercyRuns) {
                gameOuts = 3;
                if (isTopOfInning()) {
                    awayTeamRuns = mercyRuns;
                } else if (!isTopOfInning()) {
                    homeTeamRuns = mercyRuns;
                }
                return;
            }
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
        resetBasesBtn.setEnabled(true);
        resetBasesBtn.getBackground().setAlpha(255);
    }

    private void disableSubmitButton() {
        submitPlay.setEnabled(false);
        submitPlay.getBackground().setAlpha(64);
    }

    private void disableResetButton() {
        resetBasesBtn.setEnabled(false);
        resetBasesBtn.getBackground().setAlpha(64);
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
        //todo
        inningRuns = StatsContract.getColumnInt(gameCursor, StatsEntry.COLUMN_INNING_RUNS);
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
        //todo
        inningRuns = StatsContract.getColumnInt(gameCursor, StatsEntry.COLUMN_INNING_RUNS);
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
        if(firstDisplay!= null) {
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
        }
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
//                            String inningRunString = "Mercy\n" + (inningRuns + tempRuns) + "/" + mercyRuns;
//                            mercyDisplay.setText(inningRunString);

                            if(inningRuns + tempRuns <= mercyRuns) {
                                if (isTopOfInning()) {
                                    scoreString = String.valueOf(awayTeamRuns + tempRuns);
                                    scoreboardAwayScore.setText(scoreString);
                                } else {
                                    scoreString = String.valueOf(homeTeamRuns + tempRuns);
                                    scoreboardHomeScore.setText(scoreString);
                                }
//                                if(inningRuns + tempRuns == mercyRuns) {
//                                    mercyDisplay.setTextColor(Color.RED);
//                                }
                            }
//                            else {
//                                mercyDisplay.setTextColor(Color.RED);
//                            }
                            setMercyDisplay(inningRuns + tempRuns);
                        }
                    }
                    enableResetButton();
                    mResetListeners = true;
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    View dragView = (View) event.getLocalState();
                    if(dragView != null) {
                        dragView.setAlpha(1f);
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
                    break;
            }
            return true;
        }
    }

    final class MyTouchListener implements View.OnTouchListener {

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
    protected abstract void decreaseLineupIndex();
    protected abstract void checkLineupIndex();

    protected int setLineupIndex(List<Player> team, String playerFirestoreID) {
        for(int i = 0; i < team.size(); i++) {
            Player player = team.get(i);
            if(player.getFirestoreID().equals(playerFirestoreID)) {
                return i;
            }
        }
        return -1;
    }

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
//            case R.id.action_next_inning:
//                if(gameLogIndex == lowestIndex) {
//                    Toast.makeText(GameActivity.this,
//                            "Can't skip to next inning when inning just started", Toast.LENGTH_LONG).show();
//                    return true;
//                }
//                if(undoRedo) {
//                    Toast.makeText(GameActivity.this,
//                            "Can't skip to next inning while undoing/redoing plays", Toast.LENGTH_LONG).show();
//                    return true;
//                }
//                undoPlay();
//                inningJump(result);
//                break;
            case R.id.action_off_lineup_rules:
                actionEndLineupRules();
                break;
            case R.id.action_goto_stats:
                actionViewBoxScore();
                break;
            case R.id.change_game_settings:
                openGameSettingsDialog();
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

    private void openGameSettingsDialog() {
        int genderSorter = 0;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = GameSettingsDialog.newInstance(totalInnings, genderSorter, mercyRuns, mSelectionID, inningNumber, gameHelp);
        newFragment.show(fragmentTransaction, "");
    }

//    protected abstract void inningJump(String playerResult);

    private void actionViewBoxScore() {
        Intent statsIntent = new Intent(GameActivity.this, BoxScoreActivity.class);
        Bundle b = getBoxScoreBundle();
        statsIntent.putExtras(b);
        startActivity(statsIntent);
    }

    protected abstract Bundle getBoxScoreBundle();

    private void actionEndLineupRules() {
        if(currentBatter != null && currentBatter.getFirestoreID().equals(AUTO_OUT)) {
            Toast.makeText(GameActivity.this, "Can't reset lineup rules during Auto-Out", Toast.LENGTH_LONG).show();
            return;
        }
        revertLineups();
        lowestIndex = gameLogIndex;
        highestIndex = gameLogIndex;
        setUndoRedo();
        SharedPreferences gamePreferences = getSharedPreferences(mSelectionID + StatsEntry.GAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = gamePreferences.edit();
        editor.putInt(KEY_GENDERSORT, 0);
        editor.apply();
        invalidateOptionsMenu();
    }

    protected abstract void revertLineups();

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

    private void setUndoButton() {
        boolean undo = gameLogIndex > lowestIndex;
        undoButton.setClickable(undo);
        if (undo) {
            undoButton.setAlpha(1f);
        } else {
            undoButton.setAlpha(.1f);
        }
    }

    private void setRedoButton() {
        boolean redo = gameLogIndex < highestIndex;
        redoButton.setClickable(redo);
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
        MenuItem resetLineupItem = menu.findItem(R.id.action_off_lineup_rules);

        boolean undo = gameLogIndex > lowestIndex;
        boolean redo = gameLogIndex < highestIndex;

        undoItem.setVisible(undo);
        redoItem.setVisible(redo);

        SharedPreferences gamePreferences = getSharedPreferences(mSelectionID + StatsEntry.GAME, MODE_PRIVATE);
        int sortArg = gamePreferences.getInt(KEY_GENDERSORT, 0);
        if(sortArg == 0) {
            resetLineupItem.setVisible(false);
        } else {
            resetLineupItem.setVisible(true);
        }

        return true;
    }

    private void showFinishConfirmationDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = FinishGameConfirmationDialog.newInstance();
        newFragment.setCancelable(false);
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
    public void onGameSettingsChanged(int innings, int genderSorter, int mercy) {
        totalInnings = innings;
        mercyRuns = mercy;
        SharedPreferences gamePreferences = getSharedPreferences(mSelectionID + StatsEntry.GAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = gamePreferences.edit();
        editor.putInt(KEY_TOTALINNINGS, totalInnings);
        editor.putInt(StatsEntry.MERCY, mercyRuns);
        editor.apply();

        if (inningNumber / 2 >= totalInnings) {
            finalInning = true;
        } else {
            finalInning = false;
            redoEndsGame = false;
        }
        setScoreDisplay();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_EDIT) {
            String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
            String[] selectionArgs = new String[]{mSelectionID};
            gameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG, null,
                    selection, selectionArgs, null);
            gameCursor.moveToFirst();
            if (resultCode == RESULT_CODE_EDITED) {
                lowestIndex = gameLogIndex;
                if(!getSelectionData()) {
                    goToMain();
                    return;
                }
                setCustomViews();
                setViews();
                loadGamePreferences();
                checkLineupIndex();
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