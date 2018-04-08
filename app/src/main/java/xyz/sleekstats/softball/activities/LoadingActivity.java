package xyz.sleekstats.softball.activities;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.FirestoreHelperService;
import xyz.sleekstats.softball.data.FirestoreStatkeeperSync;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.dialogs.DeletionCheckDialog;
import xyz.sleekstats.softball.objects.ItemMarkedForDeletion;
import xyz.sleekstats.softball.objects.MainPageSelection;

import java.util.ArrayList;
import java.util.List;

public class LoadingActivity extends AppCompatActivity
        implements DeletionCheckDialog.OnListFragmentInteractionListener {

    private int mStatKeeperType;
    private int mLevel;
    private String mStatKeeperID;
    private String mStatKeeperName;
    private FirestoreStatkeeperSync firestoreStatkeeperSync;

    private TextView loadTitle;
    private TextView loadDescription;
    private ProgressBar loadProgressBar;
    private LocalBroadcastManager mBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        loadDescription = findViewById(R.id.load_desc);
        loadTitle = findViewById(R.id.load_title);
        loadProgressBar = findViewById(R.id.load_bar);

        loadTitle.setText("MOOGABOOGA");

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mStatKeeperType = mainPageSelection.getType();
            mStatKeeperID = mainPageSelection.getId();
            mStatKeeperName = mainPageSelection.getName();
            mLevel = mainPageSelection.getLevel();
        } catch (Exception e) {
            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        if(savedInstanceState != null) {
            Log.d("openmike1", "savedInstanceState != null");
            return;
        } else {
            Log.d("openmike1", "savedInstanceState == null");
        }
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
//            firestoreStatkeeperSync = new FirestoreStatkeeperSync(this, mStatKeeperID);
//            firestoreStatkeeperSync.checkForUpdate();

            loadTitle.setText(R.string.load1_prepare_sync);
            loadDescription.setText(R.string.load1_desc);
            if(mBroadcastManager == null) {
                Log.d("openmike1", "mBroadcastManager == null");
                mBroadcastManager = LocalBroadcastManager.getInstance(this);
                mBroadcastManager.registerReceiver(mReceiver, new IntentFilter(StatsEntry.SYNC));
            }
            setAndSendIntent(FirestoreStatkeeperSync.INTENT_CHECK_UPDATE);
            Log.d("openmike", "send checkUpdateIntent");
        } else {
//            if(firestoreStatkeeperSync != null) {
//                firestoreStatkeeperSync.detachListener();
//                firestoreStatkeeperSync = null;
//            }
            Log.d("godzilla", "loading");
            proceedToNext();
        }

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private int maxPlayers = 9999;
        private int maxTeams = 9999;
        private int maxBoxscores = 9999;
        private int omaxPlayers = 9999;
        private int omaxTeams = 9999;
        private int omaxBoxscores = 9999;

        @Override
        public void onReceive(Context context, Intent intent) {
            int msg = intent.getIntExtra(StatsEntry.SYNC, 0);
            int max;

            switch (msg) {

                case FirestoreStatkeeperSync.MSG_PLAYER_UPDATED:
                    maxPlayers --;
                    Log.d("openmike", maxPlayers + " players left out of " + omaxPlayers);
                    loadProgressBar.incrementProgressBy(1);
                    break;

                case FirestoreStatkeeperSync.MSG_TEAM_UPDATED:
                    maxTeams --;
                    Log.d("openmike", maxTeams + " TEAMS left out of " + omaxTeams);
                    loadProgressBar.incrementProgressBy(1);
                    break;

                case FirestoreStatkeeperSync.MSG_BOXSCORE_UPDATED:
                    maxBoxscores --;
                    Log.d("openmike", maxBoxscores + " Boxscores left out of " + omaxBoxscores);
                    loadProgressBar.incrementProgressBy(1);
                    break;

                case FirestoreStatkeeperSync.MSG_GO_TO_STATKEEPER:
                    Log.d("openmike", "GO_TO_STATKEEPER");
                    proceedToNext();
                    break;

                case FirestoreStatkeeperSync.MSG_START_UPDATE:
                    Log.d("openmike", "MSG_START_UPDATE");
                    loadTitle.setText(R.string.load1_prepare_sync);
                    loadDescription.setText(R.string.load1_desc);
                    setAndSendIntent(FirestoreStatkeeperSync.INTENT_UPDATE_PLAYERS);
                    break;

                case FirestoreStatkeeperSync.MSG_PLAYER_MAX:
                    max = intent.getIntExtra(FirestoreStatkeeperSync.KEY_MAX, 0);
                    maxPlayers = max;
                    omaxPlayers = max;
                    loadProgressBar.setMax(max);
                    startSyncFor("player", max);
                    break;

                case FirestoreStatkeeperSync.MSG_TEAM_MAX:
                    max = intent.getIntExtra(FirestoreStatkeeperSync.KEY_MAX, 0);
                    maxTeams = max;
                    omaxTeams = max;
                    startSyncFor("team", max);
                    break;

                case FirestoreStatkeeperSync.MSG_BOXSCORE_MAX:
                    max = intent.getIntExtra(FirestoreStatkeeperSync.KEY_MAX, 0);
                    maxBoxscores += max - 9999;
                    omaxBoxscores += max - 9999;
                    loadProgressBar.setMax(max);
                    startSyncFor("game", max);
                    break;

//                case FirestoreStatkeeperSync.MSG_OPEN_DELETION_DIALOG:
////                    openDeletionCheckDialog();
//                    break;

                case FirestoreStatkeeperSync.MSG_ERROR:
                    onSyncError("error");
                    break;
            }
            if(maxPlayers < 1) {
                maxPlayers = 999;
                setAndSendIntent(FirestoreStatkeeperSync.INTENT_UPDATE_TEAMS);
            }
            if(maxTeams < 1) {
                maxTeams = 999;
                setAndSendIntent(FirestoreStatkeeperSync.INTENT_UPDATE_BOXSCORES);
            }
            if(maxBoxscores < 1) {
                maxBoxscores = 999;
                proceedToNext();
            }
        }
    };

    private void setAndSendIntent(String action){
        Intent serviceIntent = new Intent(LoadingActivity.this, FirestoreStatkeeperSync.class);
        serviceIntent.setAction(action);
        serviceIntent.putExtra(StatsEntry.COLUMN_LEAGUE_ID, mStatKeeperID);
        startService(serviceIntent);
    }

    public void proceedToNext() {
        Intent intent;
        switch (mStatKeeperType) {
            case MainPageSelection.TYPE_LEAGUE:
                intent = new Intent(LoadingActivity.this, LeagueManagerActivity.class);
                break;
            case MainPageSelection.TYPE_TEAM:
                intent = new Intent(LoadingActivity.this, TeamManagerActivity.class);
                break;
            default:
                return;
        }
        if(mBroadcastManager != null) {
            mBroadcastManager.unregisterReceiver(mReceiver);
            mBroadcastManager = null;
        }
        startActivity(intent);
        finish();
    }

    private void onDeletionCheck() {
        loadProgressBar.setVisibility(View.INVISIBLE);
        loadTitle.setText(R.string.load3_deletion_check);
        loadDescription.setText(R.string.load3_desc);
        firestoreStatkeeperSync.deletionCheck(mLevel);
    }

    private void startSyncFor(String type, int max){
        Log.d("openmike1", "startSyncFor(String type, int max){ " + type + max);
        loadProgressBar.setProgress(0);
        loadProgressBar.setMax(max);
        loadProgressBar.setVisibility(View.VISIBLE);
        String syncText = String.format(getString(R.string.load2_desc), type);
        loadTitle.setText(R.string.load2_syncing);
        loadDescription.setText(syncText);
        loadTitle.setVisibility(View.VISIBLE);
        loadDescription.setVisibility(View.VISIBLE);
    }

    public void onSyncError(String error) {
        loadProgressBar.setVisibility(View.INVISIBLE);
        loadTitle.setText(R.string.error);
        String errorMsg = "Error with " + error;
        loadDescription.setText(errorMsg);
    }

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
        firestoreStatkeeperSync.deleteItems(deleteList);
        firestoreStatkeeperSync.saveItems(saveList);
        firestoreStatkeeperSync.updateAfterSync();
        proceedToNext();
    }

    @Override
    public void onCancel() {
        proceedToNext();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(mBroadcastManager != null) {
            mBroadcastManager.unregisterReceiver(mReceiver);
            mBroadcastManager = null;
        }

        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
