package xyz.sleekstats.softball.activities;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.FirestoreSyncService;
import xyz.sleekstats.softball.data.MySyncResultReceiver;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.dialogs.DeletionCheckDialog;
import xyz.sleekstats.softball.dialogs.NoConnectionDialog;
import xyz.sleekstats.softball.models.ItemMarkedForDeletion;
import xyz.sleekstats.softball.models.MainPageSelection;

import java.util.ArrayList;

public class LoadingActivity extends AppCompatActivity
        implements DeletionCheckDialog.OnListFragmentInteractionListener,
        NoConnectionDialog.OnFragmentInteractionListener,
        MySyncResultReceiver.Receiver {

    private int mStatKeeperType;
    private int mLevel;
    private String mStatKeeperID;
    private String mStatKeeperName;

    private TextView loadTitle;
    private TextView loadDescription;
    private ProgressBar loadProgressBar;
    private MySyncResultReceiver mReceiver;
    private int maxPlayers = 9999;
    private int maxTeams = 9999;
    private int maxBoxscores = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        loadDescription = findViewById(R.id.load_desc);
        loadTitle = findViewById(R.id.load_title);
        loadProgressBar = findViewById(R.id.load_bar);

        getStatKeeperData();
        if(mStatKeeperID == null) {
            goToMain();
            return;
        }

        checkConnection();

    }

    private void getStatKeeperData() {
        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mStatKeeperType = mainPageSelection.getType();
            mStatKeeperID = mainPageSelection.getId();
            mStatKeeperName = mainPageSelection.getName();
            mLevel = mainPageSelection.getLevel();
        } catch (Exception e) {
            goToMain();
        }
    }

    private void checkConnection() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            loadTitle.setText(R.string.load1_prepare_sync);
            loadDescription.setText(R.string.load1_desc);

            mReceiver = new MySyncResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            setAndSendIntent(FirestoreSyncService.INTENT_CHECK_UPDATE);
        } else {
            openNoConnectionDialog();
        }
    }

    private void openNoConnectionDialog(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogFragment newFragment = new NoConnectionDialog();
        newFragment.setCancelable(false);
        fragmentManager.beginTransaction().add(newFragment, null).commitAllowingStateLoss();
    }

    private void setAndSendIntent(String action) {
        Intent serviceIntent = new Intent(LoadingActivity.this, FirestoreSyncService.class);
        serviceIntent.setAction(action);
        serviceIntent.putExtra(StatsEntry.COLUMN_LEAGUE_ID, mStatKeeperID);
        serviceIntent.putExtra(StatsEntry.SYNC, mReceiver);
        if(action.equals(FirestoreSyncService.INTENT_UPDATE_TEAMS)) {
            serviceIntent.putExtra(MainPageSelection.KEY_SELECTION_NAME, mStatKeeperName);
            serviceIntent.putExtra(MainPageSelection.KEY_SELECTION_TYPE, mStatKeeperType);
        }
        startService(serviceIntent);
    }

    private void setAndSendDeletionCheckIntent() {
        Intent serviceIntent = new Intent(LoadingActivity.this, FirestoreSyncService.class);
        serviceIntent.setAction(FirestoreSyncService.INTENT_DELETION_CHECK);
        serviceIntent.putExtra(StatsEntry.COLUMN_LEAGUE_ID, mStatKeeperID);
        serviceIntent.putExtra(StatsEntry.SYNC, mReceiver);
        serviceIntent.putExtra(StatsEntry.LEVEL, mLevel);
        startService(serviceIntent);
    }

    private void setAndSendDeleteListIntent(String action, ArrayList<ItemMarkedForDeletion> list) {
        Intent serviceIntent = new Intent(LoadingActivity.this, FirestoreSyncService.class);
        serviceIntent.setAction(action);
        serviceIntent.putExtra(StatsEntry.COLUMN_LEAGUE_ID, mStatKeeperID);
        serviceIntent.putExtra(StatsEntry.SYNC, mReceiver);
        serviceIntent.putParcelableArrayListExtra(StatsEntry.DELETE, list);
        startService(serviceIntent);
    }

    private void proceedToNext() {
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
        if (mReceiver != null) {
            mReceiver.setReceiver(null);
        }
        startActivity(intent);
        finish();
    }

    private void onDeletionCheck() {
        loadProgressBar.setVisibility(View.INVISIBLE);
        loadTitle.setText(R.string.load3_deletion_check);
        loadDescription.setText(R.string.load3_desc);
        setAndSendDeletionCheckIntent();
    }

    private void startSyncFor(String type, int max) {
        loadProgressBar.setProgress(0);
        loadProgressBar.setMax(max);
        loadProgressBar.setVisibility(View.VISIBLE);
        String syncText = String.format(getString(R.string.load2_desc), type);
        loadTitle.setText(R.string.load2_syncing);
        loadDescription.setText(syncText);
        loadTitle.setVisibility(View.VISIBLE);
        loadDescription.setVisibility(View.VISIBLE);
    }

    private void onSyncError() {
        loadProgressBar.setVisibility(View.INVISIBLE);
        loadDescription.setVisibility(View.INVISIBLE);
        loadTitle.setText(R.string.error);
    }

    private void openDeletionCheckDialog(ArrayList<ItemMarkedForDeletion> itemMarkedForDeletionList) {
        loadProgressBar.setVisibility(View.INVISIBLE);
        loadTitle.setVisibility(View.INVISIBLE);
        loadDescription.setVisibility(View.INVISIBLE);
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        DialogFragment newFragment = DeletionCheckDialog.newInstance(itemMarkedForDeletionList);
//        newFragment.show(fragmentTransaction, "");
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogFragment newFragment = DeletionCheckDialog.newInstance(itemMarkedForDeletionList);
        fragmentManager.beginTransaction().add(newFragment, null).commitAllowingStateLoss();
    }

    @Override
    public void onDeletePlayersListener(ArrayList<ItemMarkedForDeletion> deleteList, ArrayList<ItemMarkedForDeletion> saveList) {
        setAndSendDeleteListIntent(FirestoreSyncService.INTENT_DELETE_ITEMS, deleteList);
        setAndSendDeleteListIntent(FirestoreSyncService.INTENT_SAVE_ITEMS, saveList);
        setAndSendIntent(FirestoreSyncService.INTENT_UPDATE_TIME);
        proceedToNext();
    }

    @Override
    public void onCancel() {
        proceedToNext();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mReceiver != null) {
            mReceiver.setReceiver(null);
        }
        goToMain();
    }

    private void goToMain() {
        if (mReceiver != null) {
            mReceiver.setReceiver(null);
        }
        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        int max;
        switch (resultCode) {

            case FirestoreSyncService.MSG_PLAYER_UPDATED:
                maxPlayers--;
                loadProgressBar.incrementProgressBy(1);
                break;

            case FirestoreSyncService.MSG_TEAM_UPDATED:
                maxTeams--;
                loadProgressBar.incrementProgressBy(1);
                break;

            case FirestoreSyncService.MSG_BOXSCORE_UPDATED:
                maxBoxscores--;
                loadProgressBar.incrementProgressBy(1);
                break;

            case FirestoreSyncService.MSG_GO_TO_STATKEEPER:
                proceedToNext();
                break;

            case FirestoreSyncService.MSG_START_UPDATE:
                loadTitle.setText(R.string.load1_prepare_sync);
                loadDescription.setText(R.string.load1_desc);
                setAndSendIntent(FirestoreSyncService.INTENT_UPDATE_PLAYERS);
                break;

            case FirestoreSyncService.MSG_PLAYER_MAX:
                max = resultData.getInt(FirestoreSyncService.KEY_MAX, 0);
                maxPlayers = max;
                loadProgressBar.setMax(max);
                startSyncFor("player", max);
                break;

            case FirestoreSyncService.MSG_TEAM_MAX:
                max = resultData.getInt(FirestoreSyncService.KEY_MAX, 0);
                maxTeams = max;
                startSyncFor("team", max);
                break;

            case FirestoreSyncService.MSG_BOXSCORE_MAX:
                max = resultData.getInt(FirestoreSyncService.KEY_MAX, 0);
                maxBoxscores += max - 9999;
                loadProgressBar.setMax(max);
                startSyncFor("game", max);
                break;

            case FirestoreSyncService.MSG_OPEN_DELETION_DIALOG:
                ArrayList<ItemMarkedForDeletion> deletions = resultData.getParcelableArrayList(StatsEntry.DELETE);
                openDeletionCheckDialog(deletions);
                break;

            case FirestoreSyncService.MSG_ERROR:
                onSyncError();
                break;
        }
        if (maxPlayers < 1) {
            maxPlayers = 999;
            setAndSendIntent(FirestoreSyncService.INTENT_UPDATE_TEAMS);
        }
        if (maxTeams < 1) {
            maxTeams = 999;
            setAndSendIntent(FirestoreSyncService.INTENT_UPDATE_BOXSCORES);
        }
        if (maxBoxscores < 1) {
            maxBoxscores = 999;
            onDeletionCheck();
        }
    }

    @Override
    public void onNoConnectionChoice(int choice) {
        switch (choice) {
            case NoConnectionDialog.RETRY:
                checkConnection();
                break;

            case NoConnectionDialog.LOAD:
                proceedToNext();
                break;

            case NoConnectionDialog.CANCEL:
                goToMain();
                break;
        }
    }
}
