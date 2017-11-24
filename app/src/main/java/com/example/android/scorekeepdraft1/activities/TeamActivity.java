package com.example.android.scorekeepdraft1.activities;


import android.content.Intent;
import android.support.v4.app.Fragment;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.fragments.TeamFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

public class TeamActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        MyApp myApp = (MyApp) getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        if(mainPageSelection == null) {
            Intent intent = new Intent(TeamActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        String leagueID = mainPageSelection.getId();
        int selectionType = mainPageSelection.getType();
        String leagueName = mainPageSelection.getName();

        if(selectionType == MainPageSelection.TYPE_TEAM) {
            setTitle(leagueName);
        }

        return TeamFragment.newInstance(leagueID, selectionType, leagueName);
    }
}