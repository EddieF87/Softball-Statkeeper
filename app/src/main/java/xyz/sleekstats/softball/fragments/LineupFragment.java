package xyz.sleekstats.softball.fragments;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.activities.BoxScoreActivity;
import xyz.sleekstats.softball.activities.LeagueGameActivity;
import xyz.sleekstats.softball.activities.TeamGameActivity;
import xyz.sleekstats.softball.activities.TeamManagerActivity;
import xyz.sleekstats.softball.activities.UsersActivity;
import xyz.sleekstats.softball.adapters.MyLineupAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.dialogs.AddNewPlayersDialog;
import xyz.sleekstats.softball.dialogs.GameSettingsDialog;
import xyz.sleekstats.softball.models.MainPageSelection;
import xyz.sleekstats.softball.models.Player;
import com.woxthebox.draglistview.BoardView;

import java.util.ArrayList;
import java.util.List;

public class LineupFragment extends Fragment {

    private BoardView mBoardView;
    private int sCreatedItems;
    private int mColumnWidth;

    private boolean sortLineup;

    private TextView gameSummaryView;
    private TextView inningsView;
    private TextView orderView;

    private List<Player> mLineup;
    private List<Player> mBench;
    private String mTeamName;
    private String mTeamID;
    private int mType;
    private String mSelectionID;
    private boolean inGame;

    private static final String KEY_TEAM_NAME = "team_name";
    private static final String KEY_TEAM_ID = "team_id";
    private static final String KEY_INGAME = "ingame";
    private static final String KEY_GENDERSORT = "keyGenderSort";
    private static final int LINEUP_INDEX = 0;
    private static final int BENCH_INDEX = 1;

    public LineupFragment() {
        // Required empty public constructor
    }

    public static LineupFragment newInstance(String selectionID, int selectionType,
                                             String teamName, String teamID, boolean isInGame) {
        Bundle args = new Bundle();
        args.putString(MainPageSelection.KEY_SELECTION_ID, selectionID);
        args.putInt(MainPageSelection.KEY_SELECTION_TYPE, selectionType);
        args.putString(KEY_TEAM_NAME, teamName);
        args.putString(KEY_TEAM_ID, teamID);
        args.putBoolean(KEY_INGAME, isInGame);
        LineupFragment fragment = new LineupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColumnWidth = 0;
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        if (args != null) {
            mSelectionID = args.getString(MainPageSelection.KEY_SELECTION_ID);
            mType = args.getInt(MainPageSelection.KEY_SELECTION_TYPE);
            mTeamName = args.getString(KEY_TEAM_NAME);
            mTeamID = args.getString(KEY_TEAM_ID);
            inGame = args.getBoolean(KEY_INGAME);
        } else {
            getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_lineup, container, false);

        Button lineupSubmitButton = rootView.findViewById(R.id.lineup_submit);
        lineupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inGame) {
                    onSubmitEdit();
                } else {
                    onSubmitLineup();
                }
            }
        });

        if (inGame) {
            lineupSubmitButton.setText(R.string.save_edit);
        }

        final TextView teamNameTextView = rootView.findViewById(R.id.team_name_display);
        teamNameTextView.setText(mTeamName);

        final FloatingActionButton addPlayersButton = rootView.findViewById(R.id.btn_start_adder);
        addPlayersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTeamFragment(mTeamName, mTeamID);
            }
        });

        mBoardView = rootView.findViewById(R.id.team_bv);
        mBoardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBoardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mColumnWidth = mBoardView.getWidth(); //height is ready
                mBoardView.setColumnWidth(mColumnWidth / 2);

                if (mLineup == null) {
                    mLineup = new ArrayList<>();
                } else {
                    mLineup.clear();
                }

                if (mBench == null) {
                    mBench = new ArrayList<>();
                } else {
                    mBench.clear();
                }

                String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
                String[] selectionArgs = new String[]{mTeamID};
                String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";
                Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS,
                        null, selection, selectionArgs, sortOrder);

                while (cursor.moveToNext()) {
                    Player player = new Player(cursor, false);
                    int playerOrder = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_ORDER);
                    if (playerOrder > 50) {
                        mBench.add(player);
                    } else {
                        mLineup.add(player);
                    }
                }
                cursor.close();
                startBoardView();
            }
        });

        mBoardView.setSnapToColumnsWhenScrolling(true);
        mBoardView.setSnapToColumnWhenDragging(true);
        mBoardView.setSnapDragItemToTouch(true);
//        mBoardView.setCustomDragItem(new MyDragItem(getActivity(), R.layout.column_item));
        mBoardView.setSnapToColumnInLandscape(false);
        mBoardView.setColumnSnapPosition(BoardView.ColumnSnapPosition.CENTER);
        mBoardView.setBoardListener(new BoardView.BoardListener() {
            @Override
            public void onItemDragStarted(int column, int row) {
            }

            @Override
            public void onItemChangedPosition(int oldColumn, int oldRow, int newColumn, int newRow) {
            }

            @Override
            public void onItemChangedColumn(int oldColumn, int newColumn) {
            }

            @Override
            public void onItemDragEnded(int fromColumn, int fromRow, int toColumn, int toRow) {
            }
        });
        mBoardView.setBoardCallback(new BoardView.BoardCallback() {
            @Override
            public boolean canDragItemAtPosition(int column, int dragPosition) {
                return true;
            }

            @Override
            public boolean canDropItemAtPosition(int oldColumn, int oldRow, int newColumn, int newRow) {
                return true;
            }
        });

        return rootView;
    }

    private void resetBoardView(){
        if(mBoardView == null) {return;}
        mBoardView.clearBoard();
        sCreatedItems = 0;
        startBoardView();
    }

    private void startBoardView(){
        addColumnList(mLineup, false);
        addColumnList(mBench, true);
    }

    private void addColumnList(List<Player> playerList, boolean isBench) {
        final ArrayList<Pair<Long, Player>> mItemArray = new ArrayList<>();
        for (Player player : playerList) {
            long id = sCreatedItems++;
            mItemArray.add(new Pair<>(id, player));
        }
        final MyLineupAdapter listAdapter = new MyLineupAdapter(mItemArray, getActivity(), isBench, getGenderSorter());
        mBoardView.addColumnList(listAdapter, null, false);
    }

    private void onSubmitEdit() {
        if (isLineupOK()) {
            setNewLineupToTempDB(getPreviousLineup(mTeamID));
            Intent intent;
            SharedPreferences gamePreferences = getActivity().getSharedPreferences(mSelectionID + StatsEntry.GAME, Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = gamePreferences.edit();
            if (mType == MainPageSelection.TYPE_LEAGUE) {
                String awayTeam = gamePreferences.getString("keyAwayTeam", null);
                String homeTeam = gamePreferences.getString("keyHomeTeam", null);
                int sortArgument = gamePreferences.getInt(KEY_GENDERSORT, 0);

                switch (sortArgument) {
                    case 3:
                        if (mTeamID.equals(awayTeam)) {
                            sortArgument = 2;
                        } else if (mTeamID.equals(homeTeam)) {
                            sortArgument = 1;
                        }
                        break;

                    case 2:
                        if (mTeamID.equals(homeTeam)) {
                            sortArgument = 0;
                        }
                        break;

                    case 1:
                        if (mTeamID.equals(awayTeam)) {
                            sortArgument = 0;
                        }
                        break;
                }

                intent = new Intent(getActivity(), LeagueGameActivity.class);
                editor.putInt(KEY_GENDERSORT, sortArgument);
            } else {
                intent = new Intent(getActivity(), TeamGameActivity.class);
                editor.putBoolean(KEY_GENDERSORT, false);
            }
            editor.apply();
            intent.putExtra("edited", true);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void onSubmitLineup() {
        if (mType == MainPageSelection.TYPE_TEAM) {
            int genderSorter = getGenderSorter();

            if (isLineupOK()) {
                clearGameDB();
                boolean lineupCheck = addTeamToTempDB(genderSorter);
                if (lineupCheck) {
                    startGame(isHome());
                }
            } else {
                Toast.makeText(getActivity(), "Add more players to lineup first.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            updateAndSubmitLineup();
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Button continueGameButton = getView().findViewById(R.id.continue_game);
        gameSummaryView = getView().findViewById(R.id.current_game_view);
        inningsView = getView().findViewById(R.id.innings_view);
        View radioButtonGroup = getView().findViewById(R.id.radiobtns_away_or_home_team);
        orderView = getView().findViewById(R.id.gender_lineup_view);
        LinearLayout settingsLayout = getView().findViewById(R.id.layout_settings);
        settingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGameSettingsDialog();
            }
        });

        if (mType == MainPageSelection.TYPE_TEAM && !inGame) {
            SharedPreferences settingsPreferences = getActivity()
                    .getSharedPreferences(mSelectionID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
            final int innings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);
            final int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
            settingsLayout.setVisibility(View.VISIBLE);
            inningsView.setVisibility(View.VISIBLE);
            setGameSettings(innings, genderSorter);

            Button lineupSubmitButton = getView().findViewById(R.id.lineup_submit);
            lineupSubmitButton.setText(R.string.start);
            radioButtonGroup.setVisibility(View.VISIBLE);

            continueGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), TeamGameActivity.class);
                    startActivity(intent);
                }
            });
            continueGameButton.setVisibility(View.VISIBLE);

            Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG,
                    null, null, null, null);
            if (cursor.moveToLast()) {
                int awayRuns = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_AWAY_RUNS);
                int homeRuns = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HOME_RUNS);
                setGameSummaryView(awayRuns, homeRuns);
                continueGameButton.setVisibility(View.VISIBLE);
                gameSummaryView.setVisibility(View.VISIBLE);
            } else {
                continueGameButton.setVisibility(View.GONE);
                gameSummaryView.setVisibility(View.INVISIBLE);
            }
            cursor.close();
        } else {
            settingsLayout.setVisibility(View.GONE);
            continueGameButton.setVisibility(View.GONE);
            gameSummaryView.setVisibility(View.GONE);
            radioButtonGroup.setVisibility(View.GONE);
        }
    }

    public void updateBench(List<Player> players) {
        for (Player player : players) {
            long id = sCreatedItems++;
            Pair<Long, Player> pair = new Pair<>(id, player);
            mBench.add(player);
            mBoardView.addItem(BENCH_INDEX, 0, pair, true);
        }
    }

    private void createTeamFragment(String teamName, String teamID) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = AddNewPlayersDialog.newInstance(teamName, teamID);
        newFragment.show(fragmentTransaction, "");
    }

    public void setGameSettings(int innings, int gendersorter) {
        if (inningsView == null) {
            return;
        }
        String inningsText = "Innings: " + innings;
        inningsView.setText(inningsText);
        setGenderSettingDisplay(gendersorter);
    }

    private void setGenderSettingDisplay(int i) {
        if (orderView == null) {
            return;
        }
        if (i == 0) {
            orderView.setVisibility(View.INVISIBLE);
            return;
        }
        orderView.setVisibility(View.VISIBLE);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Order: ");
        for (int index = 0; index < i; index++) {
            stringBuilder.append("<font color='#6fa2ef'>M</font>");
        }
        stringBuilder.append("<font color='#f99da2'>F</font>");
        String order = stringBuilder.toString();
        orderView.setText(Html.fromHtml(order));
    }

    private void setGameSummaryView(final int awayRuns, final int homeRuns) {
        SharedPreferences savedGamePreferences = getActivity()
                .getSharedPreferences(mSelectionID + StatsEntry.GAME, Context.MODE_PRIVATE);
        int inningNumber = savedGamePreferences.getInt("keyInningNumber", 2);
        inningNumber = inningNumber / 2;
        boolean isHome = savedGamePreferences.getBoolean("isHome", false);
        final int totalInnings = savedGamePreferences.getInt("keyTotalInnings", 7);
        String awayTeamName = "Away";
        String homeTeamName = "Home";
        if (isHome) {
            homeTeamName = mTeamName;
        } else {
            awayTeamName = mTeamName;
        }
        String summary = awayTeamName + ": " + awayRuns + "    " + homeTeamName + ": " + homeRuns + "\nInning: " + inningNumber;
        gameSummaryView.setText(summary);
        final String finalAwayTeamName = awayTeamName;
        final String finalHomeTeamName = homeTeamName;
        gameSummaryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BoxScoreActivity.class);
                Bundle b = new Bundle();
                b.putString("awayTeamName", finalAwayTeamName);
                b.putString("homeTeamName", finalHomeTeamName);
                b.putString("awayTeamID", mSelectionID);
                b.putString("homeTeamID", null);
                b.putInt("totalInnings", totalInnings);
                b.putInt("awayTeamRuns", awayRuns);
                b.putInt("homeTeamRuns", homeRuns);
                intent.putExtras(b);
                startActivity(intent);

            }
        });
    }


    private void startGame(boolean isHome) {
        Intent intent = new Intent(getActivity(), TeamGameActivity.class);
        intent.putExtra("isHome", isHome);
        intent.putExtra("sortArgument", sortLineup);
        startActivity(intent);
    }


    private boolean isLineupOK() {
        return updateAndSubmitLineup() > 3;
    }

    private int getGenderSorter() {
        SharedPreferences genderPreferences = getActivity()
                .getSharedPreferences(mSelectionID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
        return genderPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
    }

    private boolean isHome() {
        RadioGroup radioGroup = getView().findViewById(R.id.radiobtns_away_or_home_team);
        int id = radioGroup.getCheckedRadioButtonId();
        switch (id) {
            case R.id.radio_away:
                return false;
            case R.id.radio_home:
                return true;
        }
        return false;
    }

    private void clearGameDB() {
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        SharedPreferences savedGamePreferences = getActivity().getSharedPreferences(mSelectionID + StatsEntry.GAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedGamePreferences.edit();
        editor.clear();
        editor.apply();
    }

    private List<Player> getPreviousLineup(String teamID) {

        String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
        String[] selectionArgs = new String[]{teamID};
        Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_TEMP,
                null, selection, selectionArgs, null);

        List<Player> previousLineup = new ArrayList<>();

        while (cursor.moveToNext()) {
            previousLineup.add(new Player(cursor, true));
        }
        cursor.close();
        return previousLineup;
    }

    private void setNewLineupToTempDB(List<Player> previousLineup) {

        List<Player> lineup = getLineup();

        String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
        String[] selectionArgs = new String[]{mTeamID};

        ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.delete(StatsEntry.CONTENT_URI_TEMP, selection, selectionArgs);

        for (int i = 0; i < lineup.size(); i++) {
            Player player = lineup.get(i);
            long playerId = player.getPlayerId();
            String playerName = player.getName();
            int gender = player.getGender();
            String firestoreID = player.getFirestoreID();

            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
            values.put(StatsEntry.COLUMN_PLAYERID, playerId);
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_TEAM, mTeamName);
            values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, mTeamID);
            values.put(StatsEntry.COLUMN_ORDER, i + 1);
            values.put(StatsEntry.COLUMN_GENDER, gender);

            Player existingPlayer = checkIfPlayerExists(playerId, previousLineup);
            if (existingPlayer != null) {
                values.put(StatsEntry.COLUMN_HR, existingPlayer.getHrs());
                values.put(StatsEntry.COLUMN_3B, existingPlayer.getTriples());
                values.put(StatsEntry.COLUMN_2B, existingPlayer.getDoubles());
                values.put(StatsEntry.COLUMN_1B, existingPlayer.getSingles());
                values.put(StatsEntry.COLUMN_BB, existingPlayer.getWalks());
                values.put(StatsEntry.COLUMN_OUT, existingPlayer.getOuts());
                values.put(StatsEntry.COLUMN_SF, existingPlayer.getSacFlies());
                values.put(StatsEntry.COLUMN_RUN, existingPlayer.getRuns());
                values.put(StatsEntry.COLUMN_RBI, existingPlayer.getRbis());
                previousLineup.remove(existingPlayer);
            }

            contentResolver.insert(StatsEntry.CONTENT_URI_TEMP, values);
        }

        if (!previousLineup.isEmpty()) {
            for (int i = 0; i < previousLineup.size(); i++) {
                Player existingPlayer = previousLineup.get(i);
                ContentValues values = new ContentValues();

                values.put(StatsEntry.COLUMN_FIRESTORE_ID, existingPlayer.getFirestoreID());
                values.put(StatsEntry.COLUMN_PLAYERID, existingPlayer.getPlayerId());
                values.put(StatsEntry.COLUMN_NAME, existingPlayer.getName());
                values.put(StatsEntry.COLUMN_TEAM, mTeamName);
                values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, existingPlayer.getTeamfirestoreid());
                values.put(StatsEntry.COLUMN_ORDER, 999);
                values.put(StatsEntry.COLUMN_GENDER, existingPlayer.getGender());
                values.put(StatsEntry.COLUMN_HR, existingPlayer.getHrs());
                values.put(StatsEntry.COLUMN_3B, existingPlayer.getTriples());
                values.put(StatsEntry.COLUMN_2B, existingPlayer.getDoubles());
                values.put(StatsEntry.COLUMN_1B, existingPlayer.getSingles());
                values.put(StatsEntry.COLUMN_BB, existingPlayer.getWalks());
                values.put(StatsEntry.COLUMN_OUT, existingPlayer.getOuts());
                values.put(StatsEntry.COLUMN_SF, existingPlayer.getSacFlies());
                values.put(StatsEntry.COLUMN_RUN, existingPlayer.getRuns());
                values.put(StatsEntry.COLUMN_RBI, existingPlayer.getRbis());

                contentResolver.insert(StatsEntry.CONTENT_URI_TEMP, values);
            }
            previousLineup.clear();
        }
    }

    private Player checkIfPlayerExists(long playerID, List<Player> players) {
        for (Player player : players) {
            if (playerID == player.getPlayerId()) {
                return player;
            }
        }
        return null;
    }


    private boolean addTeamToTempDB(int requiredFemale) {
        List<Player> lineup = getLineup();
        ContentResolver contentResolver = getActivity().getContentResolver();
        int females = 0;
        int males = 0;
        int malesInRow = 0;
        int firstMalesInRow = 0;
        boolean beforeFirstFemale = true;
        boolean notProperOrder = false;
        sortLineup = false;

        for (int i = 0; i < lineup.size(); i++) {
            Player player = lineup.get(i);
            long playerId = player.getPlayerId();
            String playerName = player.getName();
            int gender = player.getGender();
            String firestoreID = player.getFirestoreID();

            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
            values.put(StatsEntry.COLUMN_PLAYERID, playerId);
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_TEAM, mTeamName);
            values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, mTeamID);
            values.put(StatsEntry.COLUMN_ORDER, i + 1);
            values.put(StatsEntry.COLUMN_GENDER, gender);
            contentResolver.insert(StatsEntry.CONTENT_URI_TEMP, values);

            if (gender == 0) {
                males++;
                malesInRow++;
                if (beforeFirstFemale) {
                    firstMalesInRow++;
                }
                if (malesInRow > requiredFemale) {
                    notProperOrder = true;
                }
            } else {
                females++;
                malesInRow = 0;
                beforeFirstFemale = false;
            }
        }

        if (requiredFemale < 1) {
            return true;
        }

        int lastMalesInRow = malesInRow;
        if (firstMalesInRow + lastMalesInRow > requiredFemale) {
            notProperOrder = true;
        }
        if (notProperOrder) {
            if (females * requiredFemale >= males) {
                Toast.makeText(getActivity(),
                        "Please set " + mTeamName + "'s lineup properly or change gender rules",
                        Toast.LENGTH_LONG).show();
                return false;
            }
            sortLineup = true;
        }
        return true;
    }

    private ArrayList<Player> getLineup() {
        ArrayList<Player> lineup = new ArrayList<>();
        try {
            String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
            String[] selectionArgs = new String[]{mTeamID};
            String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

            Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, null,
                    selection, selectionArgs, sortOrder);
            while (cursor.moveToNext()) {
                int order = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_ORDER);
                if (order < 50) {
                    lineup.add(new Player(cursor, false));
                }
            }
            cursor.close();
            return lineup;
        } catch (Exception e) {
            Toast.makeText(getActivity(), "woops  " + e, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void changeColorsRV(boolean genderSettingsOn) {
        boolean update = true;
        if (mBoardView.getAdapter(LINEUP_INDEX) != null && mBoardView.getAdapter(BENCH_INDEX) != null) {
            update = ((MyLineupAdapter) mBoardView.getAdapter(LINEUP_INDEX)).changeColors(genderSettingsOn);
            ((MyLineupAdapter) mBoardView.getAdapter(BENCH_INDEX)).changeColors(genderSettingsOn);
        }
        if (update) {
            mBoardView.getAdapter(LINEUP_INDEX).notifyDataSetChanged();
            mBoardView.getAdapter(BENCH_INDEX).notifyDataSetChanged();
        }
    }

    private int updateAndSubmitLineup() {
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs;

        int i = 1;
        List lineupList = mBoardView.getAdapter(LINEUP_INDEX).getItemList();
        for (Object playerObject : lineupList) {
            Pair<Long, Player> pair = (Pair<Long, Player>) playerObject;
            Player player = pair.second;
            String playerfireID = player.getFirestoreID();
            selectionArgs = new String[]{playerfireID};
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_ORDER, i);
            getActivity().getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS, values,
                    selection, selectionArgs);
            i++;
        }

        i = 99;
        List benchList = mBoardView.getAdapter(BENCH_INDEX).getItemList();
        for (Object playerObject : benchList) {
            Pair<Long, Player> pair = (Pair<Long, Player>) playerObject;
            Player player = pair.second;
            String playerfireID = player.getFirestoreID();
            selectionArgs = new String[]{playerfireID};
            ContentValues values = new ContentValues();
            values.put(StatsContract.StatsEntry.COLUMN_ORDER, i);
            getActivity().getContentResolver().update(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values,
                    selection, selectionArgs);
        }

        return lineupList.size();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_league, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Activity activity = getActivity();
        if (!(activity instanceof TeamManagerActivity)) {
            menu.findItem(R.id.action_export_stats).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_user_settings:
                Intent settingsIntent = new Intent(getActivity(), UsersActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.change_game_settings:
                openGameSettingsDialog();
                return true;
            case R.id.action_export_stats:
                Activity activity = getActivity();
                if (activity instanceof TeamManagerActivity) {
                    TeamManagerActivity teamManagerActivity = (TeamManagerActivity) activity;
                    teamManagerActivity.startExport(mTeamName);
                    return true;
                }
                return false;
        }
        return false;
    }

    private void openGameSettingsDialog() {
        SharedPreferences settingsPreferences = getActivity()
                .getSharedPreferences(mSelectionID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
        int innings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);
        int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = GameSettingsDialog.newInstance(innings, genderSorter, mSelectionID);
        newFragment.show(fragmentTransaction, "");
    }

    private int removePlayerFromTeam(String playerFirestoreID) {
        Player player = new Player(-1, playerFirestoreID);
        if (mLineup.contains(player)) {
            mLineup.remove(player);
            return LINEUP_INDEX;
        } else if (mBench.contains(player)) {
            mBench.remove(player);
            return BENCH_INDEX;
        }
        return 2;
    }

    public void removePlayers(List<String> firestoreIDsToDelete) {
        boolean lineupUpdate = false;
        boolean benchUpdate = false;
        for (String firestoreID : firestoreIDsToDelete) {
            int i = removePlayerFromTeam(firestoreID);
            if (i == LINEUP_INDEX) {
                lineupUpdate = true;
            } else if (i == BENCH_INDEX) {
                benchUpdate = true;
            }
        }
        if (lineupUpdate || benchUpdate) {
            resetBoardView();
        }
    }

    public void updatePlayerName(String name, String playerFirestoreID) {
        for(Player player : mLineup) {
            if(player.getFirestoreID().equals(playerFirestoreID)) {
                player.setName(name);
                resetBoardView();
                return;
            }
        }
        for(Player player : mBench) {
            if(player.getFirestoreID().equals(playerFirestoreID)) {
                player.setName(name);
                resetBoardView();
                return;
            }
        }
    }

    public void updatePlayerGender(int gender, String playerFirestoreID) {
        for(Player player : mLineup) {
            if(player.getFirestoreID().equals(playerFirestoreID)) {
                player.setGender(gender);
                resetBoardView();
                return;
            }
        }
        for(Player player : mBench) {
            if(player.getFirestoreID().equals(playerFirestoreID)) {
                player.setGender(gender);
                resetBoardView();
                return;
            }
        }
    }

    @Override
    public void onDestroyView() {
        mBoardView = null;
        super.onDestroyView();
    }
}