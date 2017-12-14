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
import android.support.v4.app.Fragment;
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
import com.example.android.scorekeepdraft1.fragments.CreateTeamFragment;
import com.example.android.scorekeepdraft1.fragments.LineupFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Paul Burke (ipaulpro)
 */
public class SetLineupActivity extends SingleFragmentActivity
        implements CreateTeamFragment.OnListFragmentInteractionListener{

    private LineupFragment lineupFragment;

    @Override
    protected Fragment createFragment() {
        Bundle args = getIntent().getExtras();
        String team = null;
        if (args != null) {
            team = args.getString("team");
        } else {
            finish();
        }
        MyApp myApp = (MyApp) getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        if(mainPageSelection == null) {
            Intent intent = new Intent(SetLineupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        int type= mainPageSelection.getType();
        String leagueId = mainPageSelection.getId();
        lineupFragment = LineupFragment.newInstance(leagueId, type, team);
        return lineupFragment;
    }

    @Override
    public void onSubmitPlayersListener(List<String> names, List<Integer> genders, String team) {
        List<String> players = new ArrayList<>();
        for (int i = 0; i < names.size() - 1; i++) {
            ContentValues values = new ContentValues();
            String player = names.get(i);
            if (player.isEmpty()) {
                continue;
            }
            int gender = genders.get(i);
            values.put(StatsEntry.COLUMN_NAME, player);
            values.put(StatsEntry.COLUMN_GENDER, gender);
            values.put(StatsEntry.COLUMN_TEAM, team);
            values.put(StatsEntry.COLUMN_ORDER, 99);
            Uri uri = getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
            if (uri != null) {
                players.add(player);
            }
        }
        if (!players.isEmpty() && lineupFragment != null) {
            lineupFragment.updateBench(players);
        }
    }
}

