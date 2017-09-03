package com.example.android.scorekeepdraft1;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.data.PlayerStatsContract;
import com.example.android.scorekeepdraft1.data.PlayerStatsContract.PlayerStatsEntry;

import static android.R.attr.name;

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
                    values.put(PlayerStatsEntry.COLUMN_NAME, team);
                    values.put(PlayerStatsEntry.COLUMN_LEAGUE, "ISL");
                    values.put(PlayerStatsEntry.COLUMN_WINS, 0);
                    values.put(PlayerStatsEntry.COLUMN_LOSSES, 0);
                    values.put(PlayerStatsEntry.COLUMN_TIES, 0);
                    values.put(PlayerStatsEntry.COLUMN_RUNSFOR, 0);
                    values.put(PlayerStatsEntry.COLUMN_RUNSAGAINST, 0);
                    getContentResolver().insert(PlayerStatsEntry.CONTENT_URI2, values);
                }
            }
        });
        Button testQuery = (Button) findViewById(R.id.testquery);
        testQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor cursor = getContentResolver().query(
                        PlayerStatsEntry.CONTENT_URI2, null,
                        null, null, null
                );
                while (cursor.moveToNext()) {
                    int nameIndex = cursor.getColumnIndex(PlayerStatsEntry.COLUMN_NAME);
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
                    values.put(PlayerStatsEntry.COLUMN_NAME, player);
                    values.put(PlayerStatsEntry.COLUMN_TEAM, "Purptopes");
      //              values.put(PlayerStatsEntry.COLUMN_ORDER, "null");
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
                }
            }
        });

    }
}