package com.example.android.scorekeepdraft1.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.FirestoreHelper;
import com.example.android.scorekeepdraft1.dialogs.EditNameDialogFragment;
import com.example.android.scorekeepdraft1.fragments.PlayerFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

public class PlayerManagerActivity extends ExportActivity
        implements EditNameDialogFragment.OnFragmentInteractionListener {

    private PlayerFragment playerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (fragment == null ) {
            fragment = createFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    protected Fragment createFragment() {
        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            String playerName = mainPageSelection.getName();
            playerFragment = PlayerFragment.newInstance(MainPageSelection.TYPE_PLAYER, playerName);
        } catch (Exception e) {
            Intent intent = new Intent(PlayerManagerActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return playerFragment;
    }

    @Override
    public void onEdit(String enteredText) {
        if (enteredText.isEmpty()) {
            return;
        }

        if (playerFragment != null) {
            playerFragment.updatePlayerName(enteredText);
            //todo mainpage
        }
    }
}
