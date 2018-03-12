package com.example.android.softballstatkeeper.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

import com.example.android.softballstatkeeper.MyApp;
import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.data.FirestoreHelper;
import com.example.android.softballstatkeeper.dialogs.DeleteConfirmationDialogFragment;
import com.example.android.softballstatkeeper.dialogs.EditNameDialogFragment;
import com.example.android.softballstatkeeper.fragments.PlayerFragment;
import com.example.android.softballstatkeeper.objects.MainPageSelection;
import com.google.firebase.firestore.FirebaseFirestore;

public class PlayerManagerActivity extends ExportActivity
        implements EditNameDialogFragment.OnFragmentInteractionListener,
        PlayerFragment.OnFragmentInteractionListener {

    private PlayerFragment playerFragment;
    private boolean editTeam = false;

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
        } else {
            playerFragment = (PlayerFragment) fragment;
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
        if (playerFragment != null && !enteredText.isEmpty()) {
            if (editTeam) {
                playerFragment.updateTeamName(enteredText);
            } else {
                boolean updated = playerFragment.updatePlayerName(enteredText);
                if (!updated) {
                    return;
                }

                try {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    MyApp myApp = (MyApp) getApplicationContext();
                    MainPageSelection mainPageSelection = myApp.getCurrentSelection();
                    String playerID = mainPageSelection.getId();
                    db.collection(FirestoreHelper.LEAGUE_COLLECTION).document(playerID).update("name", enteredText);
                } catch (Exception e) {
                    Intent intent = new Intent(PlayerManagerActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
        editTeam = false;
    }

    @Override
    protected void onDestroy() {
        Log.d("aaa", "onDestroy() PlayerManagerActivity");
        super.onDestroy();
        playerFragment = null;
    }

    @Override
    public void setTeamEdit() {
        editTeam = true;
    }
}
