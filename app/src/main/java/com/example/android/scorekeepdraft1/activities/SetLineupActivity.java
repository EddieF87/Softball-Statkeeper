package com.example.android.scorekeepdraft1.activities;

/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.LineupListAdapter;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.Listener;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Paul Burke (ipaulpro)
 */
public class SetLineupActivity extends AppCompatActivity implements Listener {

    private EditText addPlayerText;

    private LineupListAdapter leftListAdapter;
    private LineupListAdapter rightListAdapter;

    private RecyclerView rvLeftLineup;
    private RecyclerView rvRightLineup;

    private List<String> mLineup;
    private List<String> mBench;
    private String mTeam;


    //TODO add player from free agency/other teams
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineup);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            mTeam = b.getString("team");
        } else {
            finish();
        }
        MyApp myApp = (MyApp) getApplicationContext();
        final String selectionType = myApp.getCurrentSelection().getType();

        rvLeftLineup = findViewById(R.id.rvLeft);
        rvRightLineup = findViewById(R.id.rvRight);
        mLineup = new ArrayList<>();
        mBench = new ArrayList<>();

        String[] projection = new String[]{StatsEntry._ID, StatsEntry.COLUMN_NAME, StatsEntry.COLUMN_ORDER};
        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{mTeam};
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, projection,
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

        initLeftRecyclerView();
        initRightRecyclerView();

        Button lineupSubmitButton = findViewById(R.id.lineup_submit);
        if (selectionType.equals("Team")) {
            lineupSubmitButton.setText(R.string.start);
            View teamNameDisplay = findViewById(R.id.team_name_display);
            teamNameDisplay.setVisibility(View.GONE);
            View radioButtonGroup = findViewById(R.id.radiobtns_away_or_home_team);
            radioButtonGroup.setVisibility(View.VISIBLE);
        }
        lineupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectionType.equals("Team")) {
                    RadioGroup radioGroup = findViewById(R.id.radiobtns_away_or_home_team);
                    int id = radioGroup.getCheckedRadioButtonId();
                    switch (id) {
                        case R.id.radio_away:
                            startGame(false);
                            break;
                        case R.id.radio_home:
                            startGame(true);
                            break;
                        default:
                            Log.e("lineup", "Radiobutton error");
                    }
                } else {
                    updateAndSubmitLineup();
                    finish();
                }
            }
        });

        TextView teamName = findViewById(R.id.team_name_display);
        teamName.setText(mTeam);

        Button addPlayerButton = findViewById(R.id.add_player_submit);
        addPlayerText = findViewById(R.id.add_player_text);
        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayer();
            }
        });
    }

    private void startGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setPositiveButton(R.string.home, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                        startGame(true);
                    }
                })
                .setNeutralButton(R.string.away, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                        startGame(false);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void startGame(boolean isHome) {
        int size = updateAndSubmitLineup();
        if (size < 4) {
            Toast.makeText(SetLineupActivity.this, "Add more players to lineup first.", Toast.LENGTH_SHORT).show();
            return;
        }
        addTeamToTempDB();
        Intent intent = new Intent(SetLineupActivity.this, TeamGameActivity.class);
        intent.putExtra("isHome", isHome);
        startActivity(intent);
        finish();
    }

    private void addTeamToTempDB() {
        List<Player> lineup = getLineup();
        ContentResolver contentResolver = getContentResolver();
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

            Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, projection,
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
            Toast.makeText(this, "woops  " + e, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void initLeftRecyclerView() {
        rvLeftLineup.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));

        leftListAdapter = new LineupListAdapter(mLineup, this, false);
        rvLeftLineup.setAdapter(leftListAdapter);
        rvLeftLineup.setOnDragListener(leftListAdapter.getDragInstance());
    }

    private void initRightRecyclerView() {
        rvRightLineup.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));

        rightListAdapter = new LineupListAdapter(mBench, this, true);
        rvRightLineup.setAdapter(rightListAdapter);
        rvRightLineup.setOnDragListener(rightListAdapter.getDragInstance());
    }

    public void addPlayer() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        String playerName = addPlayerText.getText().toString();

        ContentValues values = new ContentValues();
        values.put(StatsEntry.COLUMN_NAME, playerName);
        values.put(StatsEntry.COLUMN_TEAM, mTeam);
        values.put(StatsEntry.COLUMN_ORDER, 99);
        Uri uri = getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
        if (uri != null) {
            mBench.add(playerName);
            initRightRecyclerView();
        }
        addPlayerText.setText("");
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
            getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS, values,
                    selection, selectionArgs);
            i++;
        }

        i = 99;
        List<String> benchList = getRightListAdapter().getList();
        for (String player : benchList) {
            selectionArgs = new String[]{player};
            ContentValues values = new ContentValues();
            values.put(StatsContract.StatsEntry.COLUMN_ORDER, i);
            getContentResolver().update(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values,
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

