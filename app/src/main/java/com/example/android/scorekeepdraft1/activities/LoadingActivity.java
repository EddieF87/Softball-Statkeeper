package com.example.android.scorekeepdraft1.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.FirestoreAdapter;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

public class LoadingActivity extends AppCompatActivity
        implements FirestoreAdapter.onFirestoreSyncListener {

    private int countdown;
    private int numberOfTeams;
    private int numberOfPlayers;
    private int mSelectionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        MyApp myApp = (MyApp) getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        if(mainPageSelection == null) {
            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            mSelectionType = mainPageSelection.getType();
        }

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            Log.d("xxx", "wifi success");
        } else {
            Log.d("xxx", "wifi fail");
        }
        FirestoreAdapter firestoreAdapter = new FirestoreAdapter(this);
        firestoreAdapter.syncStats();
    }

    private void onCountDownFinished() {
        Intent intent;
        switch (mSelectionType) {
            case MainPageSelection.TYPE_LEAGUE:
                intent = new Intent(LoadingActivity.this, LeagueManagerActivity.class);
                break;
            case MainPageSelection.TYPE_TEAM:
                intent = new Intent(LoadingActivity.this, TeamManagerActivity.class);
                break;
            default:
                return;
        }
        startActivity(intent);
        finish();
    }

    private void decreaseCountDown() {
        countdown--;
        Log.d("xxx", "countdown --  " + countdown);
        if (countdown < 1) {
            Log.d("xxx", "countdown == " + countdown);
            onCountDownFinished();
        }
    }

    @Override
    public void onFirestoreSync() {
        countdown = 2;
        Log.d("xxx", "countdown =  " + countdown);
    }

    @Override
    public void onSyncStart(int numberOf, boolean teams) {
        if(teams) {
            numberOfTeams = numberOf;
            Log.d("xxx", "numberOfTeams = " + numberOfTeams);
        } else {
            numberOfPlayers = numberOf;
            Log.d("xxx", "numberOfPlayers = " + numberOfPlayers);
        }
        if(numberOf < 1) {
            decreaseCountDown();
        }
    }

    @Override
    public void onSyncUpdate(boolean teams) {
        if(teams) {
            numberOfTeams--;
            Log.d("xxx", "numberOfTeams -- " + numberOfTeams);

            if(numberOfTeams < 1) {
                decreaseCountDown();
            }
        } else {
            numberOfPlayers--;
            Log.d("xxx", "numberOfPlayers -- " + numberOfPlayers);

            if(numberOfPlayers < 1) {
                decreaseCountDown();
            }
        }
    }

    @Override
    public void onSyncError() {
        countdown = 99;
    }
}
