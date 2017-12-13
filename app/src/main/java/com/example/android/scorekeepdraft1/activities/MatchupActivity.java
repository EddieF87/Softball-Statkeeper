package com.example.android.scorekeepdraft1.activities;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.fragments.MatchupFragment;

public class MatchupActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new MatchupFragment();
    }

    public void testclick(View v) {
        Toast.makeText(MatchupActivity.this, "testtttttttt", Toast.LENGTH_SHORT).show();
    }
}