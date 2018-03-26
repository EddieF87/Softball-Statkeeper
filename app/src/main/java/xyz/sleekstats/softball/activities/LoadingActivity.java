package xyz.sleekstats.softball.activities;

import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.FirestoreHelper;
import xyz.sleekstats.softball.dialogs.DeletionCheckDialog;
import xyz.sleekstats.softball.models.ItemMarkedForDeletion;
import xyz.sleekstats.softball.models.MainPageSelection;

import java.util.ArrayList;
import java.util.List;

public class LoadingActivity extends AppCompatActivity
        implements
        LoaderManager.LoaderCallbacks,
        FirestoreHelper.onFirestoreSyncListener,
        DeletionCheckDialog.OnListFragmentInteractionListener {

    private int countdown;
    private int numberOfTeams;
    private int numberOfPlayers;
    private int totalNumber;
    private int mSelectionType;
    private int mLevel;
    private String mSelectionID;
    private FirestoreHelper firestoreHelper;

    private TextView loadTitle;
    private TextView loadDescription;
    private ProgressBar loadProgressBar;
    private boolean initialize;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        loadDescription = findViewById(R.id.load_desc);
        loadTitle = findViewById(R.id.load_title);
        loadProgressBar = findViewById(R.id.load_bar);
        if(savedInstanceState != null) {
            firestoreHelper = savedInstanceState.getParcelable("fh");
            firestoreHelper.setContext(this);
        }
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialize = true;
        getLoaderManager().initLoader(2452, null, this);
    }

    @Override
    public void onUpdateCheck(boolean update) {
        if (update) {
            loadTitle.setText(R.string.load1_prepare_sync);
            loadDescription.setText(R.string.load1_desc);
            countdown = 2;
            firestoreHelper.syncStats();
        } else {
            proceedToNext();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("fh", firestoreHelper);
    }

    @Override
    public void proceedToNext() {
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
        loadProgressBar.setVisibility(View.INVISIBLE);
        loadTitle.setText(R.string.load3_deletion_check);
        loadDescription.setText(R.string.load3_desc);
        firestoreHelper.deletionCheck(mLevel);
    }

    private void decreaseCountDown() {
        countdown--;
        if (countdown < 1) {
            onCountDownFinished();
        }
    }

    @Override
    public void onSyncStart(int numberOf, boolean teams) {
        if(teams) {
            numberOfTeams = numberOf;
            if(numberOfPlayers > -1 && totalNumber == -1) {
                startSyncProgress();
            }
        } else {
            numberOfPlayers = numberOf;
            if(numberOfTeams > -1 && totalNumber == -1) {
                startSyncProgress();
            }
        }
        if(numberOf < 1) {
            decreaseCountDown();
        }
    }

    private void startSyncProgress(){
        totalNumber = numberOfTeams + numberOfPlayers;
        loadProgressBar.setMax(totalNumber);
        loadProgressBar.setVisibility(View.VISIBLE);
        loadTitle.setText(R.string.load2_syncing);
        loadDescription.setText(R.string.load2_desc);
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
        loadProgressBar.incrementProgressBy(1);
    }

    @Override
    public void onSyncError(String error) {
        if(error.equals("updating players") || error.equals("updating teams")) {
            countdown = 99;
        }
        loadProgressBar.setVisibility(View.INVISIBLE);
        loadTitle.setText(R.string.error);
        String errorMsg = "Error with " + error;
        loadDescription.setText(errorMsg);
    }

    @Override
    public void openDeletionCheckDialog(ArrayList<ItemMarkedForDeletion> itemMarkedForDeletionList) {
        loadProgressBar.setVisibility(View.INVISIBLE);
        loadTitle.setVisibility(View.INVISIBLE);
        loadDescription.setVisibility(View.INVISIBLE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = DeletionCheckDialog.newInstance(itemMarkedForDeletionList);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void onDeletePlayersListener(List<ItemMarkedForDeletion> deleteList, List<ItemMarkedForDeletion> saveList) {
        firestoreHelper.deleteItems(deleteList);
        firestoreHelper.saveItems(saveList);
        firestoreHelper.updateAfterSync();
        proceedToNext();
    }

    @Override
    public void onCancel() {
        proceedToNext();
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        if(initialize) {
            initialize = false;

            numberOfTeams = -1;
            numberOfPlayers = -1;
            totalNumber = -1;
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                firestoreHelper = new FirestoreHelper(this, mSelectionID);
                firestoreHelper.checkForUpdate();
            } else {
                if(firestoreHelper != null) {
                    firestoreHelper.detachListener();
                    firestoreHelper = null;
                }
                finish();
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object o) {

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firestoreHelper != null) {
            firestoreHelper.detachListener();
            firestoreHelper = null;
        }
    }
}
