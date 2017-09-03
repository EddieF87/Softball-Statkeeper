package com.example.android.scorekeepdraft1;

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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.scorekeepdraft1.adapters_listeners_etc.LineupListAdapter;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.Listener;
import com.example.android.scorekeepdraft1.data.StatsContract.PlayerStatsEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Paul Burke (ipaulpro)
 */
public class SetLineupActivity extends AppCompatActivity implements Listener {

    private Button lineupSubmitButton;
    private Button addPlayerButton;
    private EditText addPlayerText;

    private LineupListAdapter leftListAdapter;
    private LineupListAdapter rightListAdapter;

    private RecyclerView rvLeftLineup;
    private RecyclerView rvRightLineup;

    private List<String> mLineup;
    private List<String> mBench;
    private String mTeam;
    private Cursor mCursor;

    //TODO figure out why both spinners are the same after submitting lineup

    //TODO add player from free agency/other teams
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineup);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            mTeam = b.getString("team");
        }

        rvLeftLineup = (RecyclerView) findViewById(R.id.rvLeft);
        rvRightLineup = (RecyclerView) findViewById(R.id.rvRight);
        mLineup = new ArrayList<>();
        mBench = new ArrayList<>();

        String[] projection = new String[]{PlayerStatsEntry._ID, PlayerStatsEntry.COLUMN_NAME, PlayerStatsEntry.COLUMN_ORDER};
        String selection = PlayerStatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{mTeam};
        String sortOrder = PlayerStatsEntry.COLUMN_ORDER + " ASC";
        mCursor = getContentResolver().query(PlayerStatsEntry.CONTENT_URI1, projection,
                selection, selectionArgs, sortOrder);

        while (mCursor.moveToNext()){
            int nameIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_NAME);
            int orderIndex = mCursor.getColumnIndex(PlayerStatsEntry.COLUMN_ORDER);
            String playerName = mCursor.getString(nameIndex);
            int playerOrder = mCursor.getInt(orderIndex);
            if (playerOrder > 50) {mBench.add(playerName);
            } else {mLineup.add(playerName);}
        }

        initLeftRecyclerView();
        initRightRecyclerView();

        lineupSubmitButton = (Button) findViewById(R.id.lineup_submit);
        lineupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAndSubmitLineup();
            }
        });

        TextView teamName = (TextView) findViewById(R.id.team_name_display);
        teamName.setText(mTeam);

        addPlayerButton = (Button) findViewById(R.id.add_player_submit);
        addPlayerText = (EditText) findViewById(R.id.add_player_text);
        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayer();
            }
        });
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

    public void addPlayer(){
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        String playerName = addPlayerText.getText().toString();
        if (playerName.isEmpty()) {
            Toast.makeText(SetLineupActivity.this, "Please type a player's name first",
                    Toast.LENGTH_SHORT).show();
        } else if (mBench.contains(playerName) || mLineup.contains(playerName)) {
            Toast.makeText(SetLineupActivity.this, playerName + " is already on" + mTeam,
                    Toast.LENGTH_SHORT).show();
        } else {
            ContentValues values = new ContentValues();
            values.put(PlayerStatsEntry.COLUMN_NAME, playerName);
            values.put(PlayerStatsEntry.COLUMN_TEAM, mTeam);
            values.put(PlayerStatsEntry.COLUMN_ORDER, 99);
            values.put(PlayerStatsEntry.COLUMN_1B, 0);
            values.put(PlayerStatsEntry.COLUMN_2B, 0);
            values.put(PlayerStatsEntry.COLUMN_3B, 0);
            values.put(PlayerStatsEntry.COLUMN_HR, 0);
            values.put(PlayerStatsEntry.COLUMN_BB, 0);
            values.put(PlayerStatsEntry.COLUMN_SF, 0);
            values.put(PlayerStatsEntry.COLUMN_OUT, 0);
            values.put(PlayerStatsEntry.COLUMN_RUN, 0);
            values.put(PlayerStatsEntry.COLUMN_RBI, 0);
            getContentResolver().insert(PlayerStatsEntry.CONTENT_URI1, values);
            mBench.add(playerName);
        }
        addPlayerText.setText("");
    }

    public void updateAndSubmitLineup() {
        String selection = PlayerStatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs;

        int i = 1;
        List<String> lineupList = getLeftListAdapter().getList();
        for (String player : lineupList) {
            selectionArgs = new String[]{player};
            ContentValues values = new ContentValues();
            values.put(PlayerStatsEntry.COLUMN_ORDER, i);
            getContentResolver().update(PlayerStatsEntry.CONTENT_URI1, values,
                    selection, selectionArgs);
            i++;
        }

        i = 99;
        List<String> benchList = getRightListAdapter().getList();
        for (String player : benchList) {
            selectionArgs = new String[]{player};
            ContentValues values = new ContentValues();
            values.put(PlayerStatsEntry.COLUMN_ORDER, i);
            getContentResolver().update(PlayerStatsEntry.CONTENT_URI1, values,
                    selection, selectionArgs);
        }

        Intent intent = new Intent(SetLineupActivity.this, SetTeamsActivity.class);
        startActivity(intent);
    }

    @Override
    public void setEmptyListTop(boolean visibility) {}
    @Override
    public void setEmptyListBottom(boolean visibility) {}
    public LineupListAdapter getLeftListAdapter() {
        return leftListAdapter;
    }

    public LineupListAdapter getRightListAdapter() {
        return rightListAdapter;
    }
}

