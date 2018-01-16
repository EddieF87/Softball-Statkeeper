package com.example.android.scorekeepdraft1.fragments;


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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.TeamGameActivity;
import com.example.android.scorekeepdraft1.activities.UserSettingsActivity;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.LineupListAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.dialogs.CreateTeamDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.GameSettingsDialogFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class LineupFragment extends Fragment {

    private LineupListAdapter leftListAdapter;
    private LineupListAdapter rightListAdapter;

    private RecyclerView rvLeftLineup;
    private RecyclerView rvRightLineup;
    private boolean sortLineup;

    private List<Player> mLineup;
    private List<Player> mBench;
    private String mTeam;
    private int mType;
    private String mSelectionID;

    private static final String KEY_TEAM = "team";

    public LineupFragment() {
        // Required empty public constructor
    }

    public static LineupFragment newInstance(String selectionID, int selectionType, String team) {
        Bundle args = new Bundle();
        args.putString(MainPageSelection.KEY_SELECTION_ID, selectionID);
        args.putInt(MainPageSelection.KEY_SELECTION_TYPE, selectionType);
        args.putString(KEY_TEAM, team);
        LineupFragment fragment = new LineupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //TODO add player from free agency/other teams
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        if (args != null) {
            mSelectionID = args.getString(MainPageSelection.KEY_SELECTION_ID);
            mType = args.getInt(MainPageSelection.KEY_SELECTION_TYPE);
            mTeam = args.getString(KEY_TEAM);
        } else {
            getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_lineup, container, false);

        rvLeftLineup = rootView.findViewById(R.id.rvLeft);
        rvRightLineup = rootView.findViewById(R.id.rvRight);

        Button lineupSubmitButton = rootView.findViewById(R.id.lineup_submit);
        lineupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    getActivity().setResult(RESULT_OK);
                    getActivity().finish();
                }
            }
        });

        final TextView teamNameTextView = rootView.findViewById(R.id.team_name_display);
        teamNameTextView.setText(mTeam);

        Button test = rootView.findViewById(R.id.button2);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLineupOK()) {
                    setNewLineupToTempDB(getPreviousLineup(mTeam));
                    Intent intent = new Intent(getActivity(), TeamGameActivity.class);
                    SharedPreferences gamePreferences = getActivity().getSharedPreferences(mSelectionID + "game", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = gamePreferences.edit();
                    editor.putBoolean("keyGenderSort", false);
                    editor.putInt("keyFemaleOrder", 0);
                    editor.commit();
                    startActivity(intent);
                }
            }
        });

        View addPlayerView = rootView.findViewById(R.id.item_player_adder);
        final FloatingActionButton addPlayersButton = addPlayerView.findViewById(R.id.btn_start_adder);
        addPlayersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTeamFragment(mTeam);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mLineup = new ArrayList<>();
        mBench = new ArrayList<>();

        String[] projection = new String[]{StatsEntry._ID, StatsEntry.COLUMN_NAME,
                StatsEntry.COLUMN_ORDER, StatsEntry.COLUMN_GENDER};
        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{mTeam};
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

        Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, projection,
                selection, selectionArgs, sortOrder);

        while (cursor.moveToNext()) {
            int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            int orderIndex = cursor.getColumnIndex(StatsEntry.COLUMN_ORDER);
            int genderIndex = cursor.getColumnIndex(StatsEntry.COLUMN_GENDER);
            String playerName = cursor.getString(nameIndex);
            int gender = cursor.getInt(genderIndex);
            Player player = new Player(playerName, gender);
            int playerOrder = cursor.getInt(orderIndex);
            if (playerOrder > 50) {
                mBench.add(player);
            } else {
                mLineup.add(player);
            }
        }
        cursor.close();
        updateLineupRV();
        updateBenchRV();

        if (mType == MainPageSelection.TYPE_TEAM) {
            Button lineupSubmitButton = getView().findViewById(R.id.lineup_submit);
            lineupSubmitButton.setText(R.string.start);
            View radioButtonGroup = getView().findViewById(R.id.radiobtns_away_or_home_team);
            radioButtonGroup.setVisibility(View.VISIBLE);

            Button continueGameButton = getView().findViewById(R.id.continue_game);
            continueGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), TeamGameActivity.class);
                    startActivity(intent);
                }
            });
            continueGameButton.setVisibility(View.VISIBLE);

            cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG,
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                continueGameButton.setVisibility(View.VISIBLE);
            } else {
                continueGameButton.setVisibility(View.GONE);
            }
            cursor.close();
        }
    }

    public void updateBench(List<Player> players) {
        mBench.addAll(players);
        updateBenchRV();
    }

    private void createTeamFragment(String team) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = CreateTeamDialogFragment.newInstance(team);
        newFragment.show(fragmentTransaction, "");
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
                .getSharedPreferences(mSelectionID + "settings", Context.MODE_PRIVATE);
        return genderPreferences.getInt("genderSort", 0);
    }

    private boolean isHome() {
        RadioGroup radioGroup = getView().findViewById(R.id.radiobtns_away_or_home_team);
        int id = radioGroup.getCheckedRadioButtonId();
        switch (id) {
            case R.id.radio_away:
                return false;
            case R.id.radio_home:
                return true;
            default:
                Log.e("lineup", "Radiobutton error");
        }
        return false;
    }

    private void clearGameDB() {
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        SharedPreferences savedGamePreferences = getActivity().getSharedPreferences(mSelectionID + "game", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedGamePreferences.edit();
        editor.clear();
        editor.commit();
    }

    private List<Player> getPreviousLineup(String team) {
        Cursor playerCursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_TEMP,
                null, null, null, null);

        int playerIdIndex;
        int playerNameIndex;
        int genderIndex;
        int firestoreIdIndex;
        int singleIndex;
        int doubleIndex;
        int tripleIndex;
        int hrIndex;
        int bbIndex;
        int sfIndex;
        int playerOutIndex;
        int playerRunIndex;
        int rbiIndex;

        if (playerCursor.moveToFirst()) {
            playerIdIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_PLAYERID);
            playerNameIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            genderIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_GENDER);
            firestoreIdIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
            singleIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_1B);
            doubleIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_2B);
            tripleIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_3B);
            hrIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_HR);
            bbIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_BB);
            sfIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_SF);
            playerOutIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_OUT);
            playerRunIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RUN);
            rbiIndex = playerCursor.getColumnIndex(StatsEntry.COLUMN_RBI);
            playerCursor.moveToPrevious();
        } else {
            return null;
        }

        List<Player> previousLineup = new ArrayList<>();

        while (playerCursor.moveToNext()) {
            Long id = playerCursor.getLong(playerIdIndex);
            String name = playerCursor.getString(playerNameIndex);
            int gameRBI = playerCursor.getInt(rbiIndex);
            int gameRun = playerCursor.getInt(playerRunIndex);
            int game1b = playerCursor.getInt(singleIndex);
            int game2b = playerCursor.getInt(doubleIndex);
            int game3b = playerCursor.getInt(tripleIndex);
            int gameHR = playerCursor.getInt(hrIndex);
            int gameOuts = playerCursor.getInt(playerOutIndex);
            int gameBB = playerCursor.getInt(bbIndex);
            int gameSF = playerCursor.getInt(sfIndex);
            int gender = playerCursor.getInt(genderIndex);
            String firestoreID = playerCursor.getString(firestoreIdIndex);

            previousLineup.add(new Player(name, team, gender,
                    game1b, game2b, game3b, gameHR,
                    gameBB, gameRun, gameRBI, gameOuts, gameSF, 0, id, firestoreID));
            Log.d("xxx", "prev: " + name);
        }
        return previousLineup;
    }

    private boolean setNewLineupToTempDB(List<Player> previousLineup) {

        List<Player> lineup = getLineup();
        ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.delete(StatsEntry.CONTENT_URI_TEMP, null, null);

        for (int i = 0; i < lineup.size(); i++) {
            Player player = lineup.get(i);
            long playerId = player.getPlayerId();
            String playerName = player.getName();
            Log.d("xxx", "getLU: " + playerName);
            int gender = player.getGender();
            String firestoreID = player.getFirestoreID();

            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
            values.put(StatsEntry.COLUMN_PLAYERID, playerId);
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_TEAM, mTeam);
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

            Log.d("xxx", "currentLineup: " + playerName);
            contentResolver.insert(StatsEntry.CONTENT_URI_TEMP, values);
        }

        if (!previousLineup.isEmpty()) {
            for (int i = 0; i < previousLineup.size(); i++) {
                Player existingPlayer = previousLineup.get(i);
                ContentValues values = new ContentValues();

                values.put(StatsEntry.COLUMN_FIRESTORE_ID, existingPlayer.getFirestoreID());
                values.put(StatsEntry.COLUMN_PLAYERID, existingPlayer.getPlayerId());
                values.put(StatsEntry.COLUMN_NAME, existingPlayer.getName());
                values.put(StatsEntry.COLUMN_TEAM, mTeam);
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
                Log.d("xxx", "removedPlayer: " + existingPlayer.getName());
            }
            previousLineup.clear();
        }
        return true;
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
            values.put(StatsEntry.COLUMN_TEAM, mTeam);
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
                        "Please set " + mTeam + "'s lineup properly or change gender rules",
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
            String[] projection = new String[]{StatsContract.StatsEntry._ID, StatsContract.StatsEntry.COLUMN_ORDER,
                    StatsEntry.COLUMN_GENDER, StatsEntry.COLUMN_NAME, StatsEntry.COLUMN_FIRESTORE_ID};
            String selection = StatsContract.StatsEntry.COLUMN_TEAM + "=?";
            String[] selectionArgs = new String[]{mTeam};
            String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

            Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, projection,
                    selection, selectionArgs, sortOrder);
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(StatsContract.StatsEntry.COLUMN_NAME);
                int orderIndex = cursor.getColumnIndex(StatsEntry.COLUMN_ORDER);
                int idIndex = cursor.getColumnIndex(StatsEntry._ID);
                int firestoreIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
                int genderIndex = cursor.getColumnIndex(StatsEntry.COLUMN_GENDER);

                String playerName = cursor.getString(nameIndex);
                int id = cursor.getInt(idIndex);
                int gender = cursor.getInt(genderIndex);
                String firestoreID = cursor.getString(firestoreIDIndex);

                int order = cursor.getInt(orderIndex);
                if (order < 50) {
                    lineup.add(new Player(playerName, mTeam, gender, id, firestoreID));
                }
            }
            cursor.close();
            return lineup;
        } catch (Exception e) {
            Toast.makeText(getActivity(), "woops  " + e, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void updateLineupRV() {
        if (leftListAdapter == null) {
            int genderSorter = getGenderSorter();

            rvLeftLineup.setLayoutManager(new LinearLayoutManager(
                    getActivity(), LinearLayoutManager.VERTICAL, false));
            leftListAdapter = new LineupListAdapter(mLineup, getContext(), false, genderSorter);
            rvLeftLineup.setAdapter(leftListAdapter);
            rvLeftLineup.setOnDragListener(leftListAdapter.getDragInstance());
        } else {
            leftListAdapter.notifyDataSetChanged();
        }
    }

    public void updateBenchRV() {
        if (rightListAdapter == null) {
            int genderSorter = getGenderSorter();

            rvRightLineup.setLayoutManager(new LinearLayoutManager(
                    getActivity(), LinearLayoutManager.VERTICAL, false));

            rightListAdapter = new LineupListAdapter(mBench, getContext(), true, genderSorter);
            rvRightLineup.setAdapter(rightListAdapter);
            rvRightLineup.setOnDragListener(rightListAdapter.getDragInstance());
        } else {
            rightListAdapter.notifyDataSetChanged();
        }
    }

    public void changeColorsRV(boolean genderSettingsOn) {
        boolean update = true;
        if (leftListAdapter != null && rightListAdapter != null) {
            update = leftListAdapter.changeColors(genderSettingsOn);
            rightListAdapter.changeColors(genderSettingsOn);
        }
        if (update) {
            updateLineupRV();
            updateBenchRV();
        }
    }

    private int updateAndSubmitLineup() {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs;

        int i = 1;
        List<Player> lineupList = getLeftListAdapter().getPlayerList();
        for (Player player : lineupList) {
            String name = player.getName();
            selectionArgs = new String[]{name};
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_ORDER, i);
            getActivity().getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS, values,
                    selection, selectionArgs);
            i++;
        }

        i = 99;
        List<Player> benchList = getRightListAdapter().getPlayerList();
        for (Player player : benchList) {
            String name = player.getName();
            selectionArgs = new String[]{name};
            ContentValues values = new ContentValues();
            values.put(StatsContract.StatsEntry.COLUMN_ORDER, i);
            getActivity().getContentResolver().update(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values,
                    selection, selectionArgs);
        }

        return lineupList.size();
    }

    public LineupListAdapter getLeftListAdapter() {
        return leftListAdapter;
    }

    public LineupListAdapter getRightListAdapter() {
        return rightListAdapter;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_league, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_user_settings:
                Intent settingsIntent = new Intent(getActivity(), UserSettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.change_game_settings:
                SharedPreferences settingsPreferences = getActivity()
                        .getSharedPreferences(mSelectionID + "settings", Context.MODE_PRIVATE);
                int innings = settingsPreferences.getInt("innings", 7);
                int genderSorter = settingsPreferences.getInt("genderSort", 0);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DialogFragment newFragment = GameSettingsDialogFragment.newInstance(innings, genderSorter, mSelectionID);
                newFragment.show(fragmentTransaction, "");
                return true;
        }
        return false;
    }
}