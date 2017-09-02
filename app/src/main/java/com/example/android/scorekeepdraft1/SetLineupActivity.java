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

import android.content.Context;
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

import com.woxthebox.draglistview.DragListView;


import java.util.List;

/**
 * @author Paul Burke (ipaulpro)
 */
public class SetLineupActivity extends AppCompatActivity implements Listener {

    private Button lineupSubmitButton;
    private Button addPlayerButton;
    private EditText addPlayerText;

    private ListAdapter leftListAdapter;
    private ListAdapter rightListAdapter;

    private RecyclerView rvLeft;
    private RecyclerView rvRight;

    private List<Player> mLineup;
    private List<Player> mBench;
    private List<Player> mRoster;

    private DragListView mDragListView;

    private Team boogeymen = new Team("boogeymen");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineup);

        rvLeft = (RecyclerView) findViewById(R.id.rvLeft);
        rvRight = (RecyclerView) findViewById(R.id.rvRight);


        boogeymen = new Team("Boogeymen");
        boogeymen.addPlayer(new Player("Kosta"));
        boogeymen.addPlayer(new Player("EDD"));
        boogeymen.addPlayer(new Player("Agy"));
        boogeymen.addPlayer(new Player("Jay"));
        boogeymen.addPlayer(new Player("IVA"));
        boogeymen.addPlayer(new Player("Mike"));
        boogeymen.addPlayer(new Player("Josh"));
        boogeymen.addPlayer(new Player("Isaac"));
        boogeymen.addPlayer(new Player("Adam"));
        boogeymen.addPlayer(new Player("IV222A"));
        boogeymen.addPlayer(new Player("Mi222ke"));
        boogeymen.addPlayer(new Player("Jo222sh"));
        boogeymen.addPlayer(new Player("Is22aac"));
        boogeymen.addPlayer(new Player("Ad222am"));
        boogeymen.addPlayer(new Player("IVA444"));
        boogeymen.addPlayer(new Player("4444"));
        boogeymen.addPlayer(new Player("Jo444sh"));
        boogeymen.addPlayer(new Player("Is444aac"));
        boogeymen.addPlayer(new Player("Ad444am"));
        mRoster = boogeymen.getRoster();
        for (int i = 0; i < mRoster.size(); i++) {
            if (i % 2 == 0) {
                boogeymen.putInLineup(mRoster.get(i));
            } else {
                boogeymen.putOnBench(mRoster.get(i));
            }
        }
        mLineup = boogeymen.getLineup();
        mBench = boogeymen.getBench();

        initLeftRecyclerView();
        initRightRecyclerView();

        lineupSubmitButton = (Button) findViewById(R.id.lineup_submit);
        lineupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Player> cookiecris = getLeftListAdapter().getList();
                for (Player player : cookiecris) {
                    Toast.makeText(SetLineupActivity.this, player.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView teamName = (TextView) findViewById(R.id.team_name_display);
        teamName.setText("boogeymen");

        addPlayerButton = (Button) findViewById(R.id.add_player_submit);
        addPlayerText = (EditText) findViewById(R.id.add_player_text);
        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                String playerName = addPlayerText.getText().toString();
                if (playerName.isEmpty()) {
                    Toast.makeText(SetLineupActivity.this, "Please type a player's name first",
                            Toast.LENGTH_SHORT).show();
                } else if (boogeymen.isOnRoster(playerName)) {
                    Toast.makeText(SetLineupActivity.this, playerName + " is already on" + boogeymen.getName(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Player newPlayer = new Player(playerName);
                    boogeymen.addPlayer(newPlayer);
                    boogeymen.putOnBench(newPlayer);
                }
                addPlayerText.setText("");
            }
        });
    }

    private void initLeftRecyclerView() {
        rvLeft.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));

        leftListAdapter = new ListAdapter(mLineup, this, false);
        rvLeft.setAdapter(leftListAdapter);
        rvLeft.setOnDragListener(leftListAdapter.getDragInstance());
    }

    private void initRightRecyclerView() {
        rvRight.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));

        rightListAdapter = new ListAdapter(mBench, this, true);
        rvRight.setAdapter(rightListAdapter);
        rvRight.setOnDragListener(rightListAdapter.getDragInstance());
    }

    @Override
    public void setEmptyListTop(boolean visibility) {

    }

    @Override
    public void setEmptyListBottom(boolean visibility) {

    }

    public ListAdapter getLeftListAdapter() {
        return leftListAdapter;
    }
}
