package com.example.android.scorekeepdraft1.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.FirestoreHelper;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

public class LoadingActivity extends AppCompatActivity
        implements FirestoreHelper.onFirestoreSyncListener {

    private int countdown;
    private int numberOfTeams;
    private int numberOfPlayers;
    private int mSelectionType;
    private String mSelectionID;
    private FirestoreHelper firestoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mSelectionType = mainPageSelection.getType();
            mSelectionID = mainPageSelection.getId();
        } catch (Exception e) {
            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            Log.d("xxx", "wifi success");
        } else {
            Log.d("xxx", "wifi fail");
        }
        firestoreHelper = new FirestoreHelper(this, mSelectionID);
        firestoreHelper.checkForUpdate();
    }

    @Override
    public void onUpdateCheck(boolean update) {
        if (update) {
            firestoreHelper.syncStats();
        } else {
            proceedToNext();
        }
    }

    private void proceedToNext() {
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

    private void onCountDownFinished() {
        firestoreHelper.updateAfterSync();
        proceedToNext();
    }

    private void decreaseCountDown() {
        countdown--;
        if (countdown < 1) {
            onCountDownFinished();
        }
    }

    @Override
    public void onFirestoreSync() {
        countdown = 2;
    }

    @Override
    public void onSyncStart(int numberOf, boolean teams) {
        if(teams) {
            numberOfTeams = numberOf;
        } else {
            numberOfPlayers = numberOf;
        }
        if(numberOf < 1) {
            decreaseCountDown();
        }
    }

    @Override
    public void onSyncUpdate(boolean teams) {
        if(teams) {
            numberOfTeams--;
            if(numberOfTeams < 1) {
                decreaseCountDown();
            }
        } else {
            numberOfPlayers--;
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
