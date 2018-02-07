package com.example.android.scorekeepdraft1.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.dialogs.ChangeTeamDialogFragment;
import com.example.android.scorekeepdraft1.fragments.PlayerFragment;
import com.example.android.scorekeepdraft1.fragments.TeamFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

import java.util.ArrayList;
import java.util.List;

public class PlayerPagerActivity extends ObjectPagerActivity
        implements ChangeTeamDialogFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startPager(1, StatsContract.StatsEntry.CONTENT_URI_PLAYERS);
    }

    public void returnDeleteResult(int result, String deletedPlayer) {
        Intent intent = getIntent();
        intent.putExtra("delete", deletedPlayer);
        setResult(result, intent);
        finish();
    }


    @Override
    public void onTeamChosen(String teamName, String teamID) {
        teamChosen(teamName, teamID);
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
    }
}
