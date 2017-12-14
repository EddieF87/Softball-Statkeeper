package com.example.android.scorekeepdraft1.fragments;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.GameActivity;
import com.example.android.scorekeepdraft1.activities.SetLineupActivity;
import com.example.android.scorekeepdraft1.activities.TeamGameActivity;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.LineupListAdapter;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.Listener;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class LineupFragment extends Fragment implements Listener {

    private LineupListAdapter leftListAdapter;
    private LineupListAdapter rightListAdapter;

    private RecyclerView rvLeftLineup;
    private RecyclerView rvRightLineup;

    private List<String> mLineup;
    private List<String> mBench;
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
        mLineup = new ArrayList<>();
        mBench = new ArrayList<>();

        String[] projection = new String[]{StatsEntry._ID, StatsEntry.COLUMN_NAME, StatsEntry.COLUMN_ORDER};
        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{mTeam};
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

        Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, projection,
                selection, selectionArgs, sortOrder);

        while (cursor.moveToNext()) {
            int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            int orderIndex = cursor.getColumnIndex(StatsEntry.COLUMN_ORDER);
            String playerName = cursor.getString(nameIndex);
            int playerOrder = cursor.getInt(orderIndex);
            if (playerOrder > 50) {
                mBench.add(playerName);
            } else {
                mLineup.add(playerName);
            }
        }
        cursor.close();

        Button continueGameButton = rootView.findViewById(R.id.continue_game);
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

        Button lineupSubmitButton = rootView.findViewById(R.id.lineup_submit);
        if (mType == MainPageSelection.TYPE_TEAM) {
            lineupSubmitButton.setText(R.string.start);
            View radioButtonGroup = rootView.findViewById(R.id.radiobtns_away_or_home_team);
            radioButtonGroup.setVisibility(View.VISIBLE);
        }
        lineupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mType == MainPageSelection.TYPE_TEAM) {
                    if(isLineupOK()) {
                        clearGameDB();
                        addTeamToTempDB();
                        startGame(isHome());
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

        View addPlayerView = rootView.findViewById(R.id.item_player_adder);
        final FloatingActionButton addPlayersButton = addPlayerView.findViewById(R.id.btn_start_adder);
        addPlayersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTeamFragment(mTeam);
            }
        });

        initLeftRecyclerView();
        initRightRecyclerView();

        return rootView;
    }

    public void updateBench(List<String> players) {
        mBench.addAll(players);
        if (rightListAdapter != null) {
            rightListAdapter.notifyDataSetChanged();
        }
    }

    private void createTeamFragment(String team) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = CreateTeamFragment.newInstance(team);
        newFragment.show(fragmentTransaction, "");
    }


    private void startGame(boolean isHome) {
        Intent intent = new Intent(getActivity(), TeamGameActivity.class);
        intent.putExtra("isHome", isHome);
        startActivity(intent);
        getActivity().finish();
    }

    private boolean isLineupOK() {
        return updateAndSubmitLineup() > 3;
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
        SharedPreferences savedGamePreferences = getActivity().getSharedPreferences(mSelectionID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedGamePreferences.edit();
        editor.clear();
        editor.commit();
    }

    private void addTeamToTempDB() {
        List<Player> lineup = getLineup();
        ContentResolver contentResolver = getActivity().getContentResolver();
        for (int i = 0; i < lineup.size(); i++) {
            Player player = lineup.get(i);
            long playerId = player.getPlayerId();
            String playerName = player.getName();
            String firestoreID = player.getFirestoreID();

            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
            values.put(StatsEntry.COLUMN_PLAYERID, playerId);
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_TEAM, mTeam);
            values.put(StatsEntry.COLUMN_ORDER, i + 1);
            contentResolver.insert(StatsEntry.CONTENT_URI_TEMP, values);
        }
    }

    private ArrayList<Player> getLineup() {
        ArrayList<Player> lineup = new ArrayList<>();
        try {
            String[] projection = new String[]{StatsContract.StatsEntry._ID, StatsContract.StatsEntry.COLUMN_ORDER, StatsEntry.COLUMN_NAME, StatsEntry.COLUMN_FIRESTORE_ID};
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

                String playerName = cursor.getString(nameIndex);
                int id = cursor.getInt(idIndex);

                String firestoreID = cursor.getString(firestoreIDIndex);

                int order = cursor.getInt(orderIndex);
                if (order < 50) {
                    lineup.add(new Player(playerName, mTeam, id, firestoreID));
                }
            }
            cursor.close();
            return lineup;
        } catch (Exception e) {
            Toast.makeText(getActivity(), "woops  " + e, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void initLeftRecyclerView() {
        rvLeftLineup.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));

        leftListAdapter = new LineupListAdapter(mLineup, this, false);
        rvLeftLineup.setAdapter(leftListAdapter);
        rvLeftLineup.setOnDragListener(leftListAdapter.getDragInstance());
    }

    private void initRightRecyclerView() {
        rvRightLineup.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));

        rightListAdapter = new LineupListAdapter(mBench, this, true);
        rvRightLineup.setAdapter(rightListAdapter);
        rvRightLineup.setOnDragListener(rightListAdapter.getDragInstance());
    }

    private int updateAndSubmitLineup() {
        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs;

        int i = 1;
        List<String> lineupList = getLeftListAdapter().getList();
        for (String player : lineupList) {
            selectionArgs = new String[]{player};
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_ORDER, i);
            getActivity().getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS, values,
                    selection, selectionArgs);
            i++;
        }

        i = 99;
        List<String> benchList = getRightListAdapter().getList();
        for (String player : benchList) {
            selectionArgs = new String[]{player};
            ContentValues values = new ContentValues();
            values.put(StatsContract.StatsEntry.COLUMN_ORDER, i);
            getActivity().getContentResolver().update(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values,
                    selection, selectionArgs);
        }

        return lineupList.size();
    }

    @Override
    public void setEmptyListTop(boolean visibility) {
    }
    @Override
    public void setEmptyListBottom(boolean visibility) {
    }
    public LineupListAdapter getLeftListAdapter() {
        return leftListAdapter;
    }
    public LineupListAdapter getRightListAdapter() {
        return rightListAdapter;
    }
}