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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.recycler.RecyclerListAdapter;
import com.example.android.scorekeepdraft1.recycler.RecyclerListFragment;

import java.util.List;

/**
 * @author Paul Burke (ipaulpro)
 */
public class LineupActivity extends AppCompatActivity {

    private Button lineupSubmitButton;
    private RecyclerListFragment topFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineup);

        if (savedInstanceState == null) {
            topFragment = new RecyclerListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content1, topFragment)
                    .commit();

            RecyclerListFragment fragment2 = new RecyclerListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content2, fragment2)
                    .commit();
        }

        lineupSubmitButton = (Button) findViewById(R.id.lineup_submit);
        lineupSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Player> cookiecris = topFragment.getAdapter().getList();
                for (Player player : cookiecris) {
                    Toast.makeText(LineupActivity.this, player.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        TextView teamName = (TextView) findViewById(R.id.team_name_display);
        teamName.setText("boogeymen");
    }
}
