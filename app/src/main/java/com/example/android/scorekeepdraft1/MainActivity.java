package com.example.android.scorekeepdraft1;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startGame = (Button) findViewById(R.id.start);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        Button test = (Button) findViewById(R.id.testbut);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SetLineupActivity.class);
                startActivity(intent);
            }
        });

        Button testStat = (Button) findViewById(R.id.teststats);
        testStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, StatsActivity.class);
                startActivity(intent);
            }
        });

        Button title = (Button) findViewById(R.id.testtitle);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTitle("Bbbbbbb");
            }
        });

        Button test2 = (Button) findViewById(R.id.testbut2);
        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SetTeamsActivity.class);
                startActivity(intent);
            }
        });

        Button testAdd = (Button) findViewById(R.id.test_add);
        testAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] listOfTeams = {"Purptopes", "Rigtopes", "Goon Nation", "Boogeymen"};
                for (String team : listOfTeams) {
                    ContentValues values = new ContentValues();
                    values.put(StatsEntry.COLUMN_NAME, team);
                    values.put(StatsEntry.COLUMN_LEAGUE, "ISL");
                    values.put(StatsEntry.COLUMN_WINS, 0);
                    values.put(StatsEntry.COLUMN_LOSSES, 0);
                    values.put(StatsEntry.COLUMN_TIES, 0);
                    values.put(StatsEntry.COLUMN_RUNSFOR, 0);
                    values.put(StatsEntry.COLUMN_RUNSAGAINST, 0);
                    getContentResolver().insert(StatsEntry.CONTENT_URI2, values);
                }
            }
        });
        Button testQuery = (Button) findViewById(R.id.testquery);
        testQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor cursor = getContentResolver().query(
                        StatsEntry.CONTENT_URI2, null,
                        null, null, null
                );
                while (cursor.moveToNext()) {
                    int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
                    String name = cursor.getString(nameIndex);
                    Toast.makeText(MainActivity.this, "Team: " + name, Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(MainActivity.this, "all done", Toast.LENGTH_SHORT).show();
            }
        });

        Button testPlayers = (Button) findViewById(R.id.testplayers);
        testPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] listOfPlayers = {"bbaa1", "bgt4w1", "gwbbra2", "byhbbyb2", "a3", "b3", "a4", "bbebb4", "a5", "b5", "bbbba6", "b6"};

                for (String player : listOfPlayers) {
                    ContentValues values = new ContentValues();
                    values.put(StatsEntry.COLUMN_NAME, player);
                    values.put(StatsEntry.COLUMN_TEAM, "Purptopes");
      //              values.put(StatsEntry.COLUMN_ORDER, "null");
                    values.put(StatsEntry.COLUMN_1B, 0);
                    values.put(StatsEntry.COLUMN_2B, 0);
                    values.put(StatsEntry.COLUMN_3B, 0);
                    values.put(StatsEntry.COLUMN_HR, 0);
                    values.put(StatsEntry.COLUMN_BB, 0);
                    values.put(StatsEntry.COLUMN_SF, 0);
                    values.put(StatsEntry.COLUMN_OUT, 0);
                    values.put(StatsEntry.COLUMN_RUN, 0);
                    values.put(StatsEntry.COLUMN_RBI, 0);
                    getContentResolver().insert(StatsEntry.CONTENT_URI1, values);
                }
            }
        });

    }
}