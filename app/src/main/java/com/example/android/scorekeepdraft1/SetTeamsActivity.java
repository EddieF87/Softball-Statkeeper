package com.example.android.scorekeepdraft1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;

public class SetTeamsActivity extends AppCompatActivity {


    private Button startGame;
    private Button editAwayLineup;
    private Button editHomeLineup;
    private Spinner setAwayTeam;
    private Spinner setHomeTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_teams);


    }
}
