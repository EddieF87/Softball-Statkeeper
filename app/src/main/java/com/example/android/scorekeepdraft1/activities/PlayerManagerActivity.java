package com.example.android.scorekeepdraft1.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.fragments.PlayerFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

public class PlayerManagerActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        MyApp myApp = (MyApp) getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        if(mainPageSelection == null) {
            Intent intent = new Intent(PlayerManagerActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        String playerName = mainPageSelection.getName();
        return PlayerFragment.newInstance(MainPageSelection.TYPE_PLAYER, playerName);
    }
}
