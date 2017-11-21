package com.example.android.scorekeepdraft1.activities;


import android.support.v4.app.Fragment;
import com.example.android.scorekeepdraft1.fragments.StatsFragment;

public class StatsActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return new StatsFragment();
    }
}