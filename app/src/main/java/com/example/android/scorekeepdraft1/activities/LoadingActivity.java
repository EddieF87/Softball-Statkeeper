package com.example.android.scorekeepdraft1.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.FirestoreHelper;
import com.example.android.scorekeepdraft1.dialogs.DeletionCheckDialogFragment;
import com.example.android.scorekeepdraft1.objects.ItemMarkedForDeletion;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

import java.util.ArrayList;
import java.util.List;

public class LoadingActivity extends AppCompatActivity
        implements FirestoreHelper.onFirestoreSyncListener, DeletionCheckDialogFragment.OnListFragmentInteractionListener {

    private int countdown;
    private int numberOfTeams;
    private int numberOfPlayers;
    private int mSelectionType;
    private int mLevel;
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
            mLevel = mainPageSelection.getLevel();
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

    @Override
    public void proceedToNext() {
        Log.d("xxx", "proceedToNext =");

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
        firestoreHelper.deletionCheck(mLevel);
    }

    private void decreaseCountDown() {
        countdown--;
        if (countdown < 1) {
            onCountDownFinished();
        }
    }

    @Override
    public void onFirestoreUpdateSync() {
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
    public void onSyncError(String error) {
        if(error.equals("updating players") || error.equals("updating teams")) {
            countdown = 99;
        }
        Toast.makeText(this, "Error with " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void openDeletionCheckDialog(ArrayList<ItemMarkedForDeletion> itemMarkedForDeletionList) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = DeletionCheckDialogFragment.newInstance(itemMarkedForDeletionList);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void onDeletePlayersListener(List<ItemMarkedForDeletion> deleteList, List<ItemMarkedForDeletion> saveList) {
        for (ItemMarkedForDeletion itemMarkedForDeletion : deleteList) {
            Log.d("xxx", "deleteditem = " + itemMarkedForDeletion.getName());
        }
        for (ItemMarkedForDeletion itemMarkedForDeletion : saveList) {
            Log.d("xxx", "saveditem = " + itemMarkedForDeletion.getName());
        }
        firestoreHelper.deleteItems(deleteList);
        firestoreHelper.saveItems(saveList);
        firestoreHelper.updateAfterSync();
        proceedToNext();
    }

    @Override
    public void onCancel() {
        proceedToNext();
    }
}
