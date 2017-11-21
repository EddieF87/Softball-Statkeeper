package com.example.android.scorekeepdraft1.activities;


import android.support.v4.app.Fragment;
import com.example.android.scorekeepdraft1.fragments.StandingsFragment;

public class StandingsActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return new StandingsFragment();
    }
}
