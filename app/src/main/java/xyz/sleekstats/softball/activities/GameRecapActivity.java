package xyz.sleekstats.softball.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.BoxScorePlayerCursorAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.dialogs.DeleteConfirmationDialog;
import xyz.sleekstats.softball.dialogs.JoinOrCreateDialog;
import xyz.sleekstats.softball.objects.MainPageSelection;

public class GameRecapActivity extends ExportActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
DeleteConfirmationDialog.OnFragmentInteractionListener {

    private final static int PLAYER_NAME_LOADER = 11;
    private final static int AWAY_LOADER = 12;
    private final static int HOME_LOADER = 13;
    private Map<String, String> mPlayerNames;
    private BoxScorePlayerCursorAdapter awayAdapter;
    private BoxScorePlayerCursorAdapter homeAdapter;
    private long mGameID;
    private String awayTeamID;
    private String homeTeamID;
    private String selectionName;
    private String selectionID;
    private int selectionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_boxscore);

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            selectionID = mainPageSelection.getId();
            selectionType = mainPageSelection.getType();
            selectionName = mainPageSelection.getName();
        } catch (Exception e) {
            Intent intent = new Intent(GameRecapActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        View boxscore = findViewById(R.id.relativelayout_boxscore);
        boxscore.setVisibility(View.GONE);

        Intent intent = getIntent();
        mGameID = intent.getLongExtra(StatsEntry.COLUMN_GAME_ID, 0);
        awayTeamID = intent.getStringExtra(StatsEntry.COLUMN_AWAY_TEAM);
        homeTeamID = intent.getStringExtra(StatsEntry.COLUMN_HOME_TEAM);
        String awayTeamName = intent.getStringExtra("awayname");
        String homeTeamName = intent.getStringExtra("homename");
        int awayTeamRuns = intent.getIntExtra(StatsEntry.COLUMN_AWAY_RUNS, 0);
        int homeTeamRuns = intent.getIntExtra(StatsEntry.COLUMN_HOME_RUNS, 0);

        TextView header = findViewById(R.id.boxscore_header);
        View awayTitle = findViewById(R.id.away_players_title);
        View homeTitle = findViewById(R.id.home_players_title);
        TextView awayNameView = awayTitle.findViewById(R.id.bs_name_title);
        TextView homeNameView = homeTitle.findViewById(R.id.bs_name_title);

        String dateString = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(mGameID);
        String titleString = dateString + "  " + awayTeamName + " @ " + homeTeamName;
        setTitle(titleString);

        String headerString = awayTeamName + " " + awayTeamRuns + "   " + homeTeamName + " " + homeTeamRuns;
        header.setText(headerString);


        if (selectionType == MainPageSelection.TYPE_TEAM) {
            awayNameView.setText(selectionName);
        } else {
            awayNameView.setText(awayTeamName);
            homeNameView.setText(homeTeamName);
        }

        getSupportLoaderManager().initLoader(PLAYER_NAME_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection;
        String selection;
        String[] selectionArgs;
        Uri uri;
        switch (id) {
            case AWAY_LOADER:
                String teamID;
                if (selectionType == MainPageSelection.TYPE_TEAM) {
                    teamID = selectionID;
                } else {
                    teamID = awayTeamID;
                }
                uri = StatsEntry.CONTENT_URI_BOXSCORE_PLAYERS;
                selection = StatsEntry.COLUMN_GAME_ID + "=? AND " + StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
                selectionArgs = new String[]{String.valueOf(mGameID), teamID};
                projection = null;
                break;
            case HOME_LOADER:
                uri = StatsEntry.CONTENT_URI_BOXSCORE_PLAYERS;
                selection = StatsEntry.COLUMN_GAME_ID + "=? AND " + StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=?";
                selectionArgs = new String[]{String.valueOf(mGameID), homeTeamID};
                projection = null;
                break;
            case PLAYER_NAME_LOADER:
                uri = StatsEntry.CONTENT_URI_PLAYERS;
                projection = new String[]{StatsEntry.COLUMN_FIRESTORE_ID, StatsEntry.COLUMN_NAME};
                selection = null;
                selectionArgs = null;
                break;
            default:
                return null;
        }
        return new CursorLoader(this, uri,
                projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToPosition(-1);

        switch (loader.getId()) {
            case AWAY_LOADER:
                awayAdapter.swapCursor(data);
                break;
            case HOME_LOADER:
                homeAdapter.swapCursor(data);
                break;
            case PLAYER_NAME_LOADER:
                mPlayerNames = new HashMap<>();
                while (data.moveToNext()) {
                    String teamID = StatsContract.getColumnString(data, StatsEntry.COLUMN_FIRESTORE_ID);
                    String teamName = StatsContract.getColumnString(data, StatsEntry.COLUMN_NAME);
                    mPlayerNames.put(teamID, teamName);
                }

                ListView awayListView = findViewById(R.id.away_players_listview);
                ListView homeListView = findViewById(R.id.home_players_listview);
                if (selectionType == MainPageSelection.TYPE_TEAM) {

                    LinearLayout homeLayout = findViewById(R.id.linearLayoutHome);
                    homeLayout.setVisibility(View.GONE);

                    awayAdapter = new BoxScorePlayerCursorAdapter(this, mPlayerNames);
                    awayListView.setAdapter(awayAdapter);

                    getSupportLoaderManager().initLoader(AWAY_LOADER, null, this);

                } else {
                    awayAdapter = new BoxScorePlayerCursorAdapter(this, mPlayerNames);
                    awayListView.setAdapter(awayAdapter);
                    homeAdapter = new BoxScorePlayerCursorAdapter(this, mPlayerNames);
                    homeListView.setAdapter(homeAdapter);

                    getSupportLoaderManager().initLoader(AWAY_LOADER, null, this);
                    getSupportLoaderManager().initLoader(HOME_LOADER, null, this);
                }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void openDeleteDialog(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = DeleteConfirmationDialog.newInstance("this game recap");
        newFragment.show(fragmentTransaction, "");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gamerecap, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_export_stats:
                startBoxscoreExport(mGameID);
                return true;
            case R.id.action_delete_recap:
                openDeleteDialog();
                return true;
        }
        return false;
    }

    @Override
    public void onDeletionChoice(boolean delete) {
        if(delete) {
            String selection = StatsEntry.COLUMN_GAME_ID + "=?";
            String[] selectionArgs = new String[]{String.valueOf(mGameID)};
            int rowsDeleted = getContentResolver().delete(StatsEntry.CONTENT_URI_BOXSCORE_PLAYERS, selection, selectionArgs);
            if(rowsDeleted >= 0){
                ContentValues values = new ContentValues();
                values.put(StatsEntry.COLUMN_LOCAL, 0);
                getContentResolver().update(StatsEntry.CONTENT_URI_BOXSCORE_OVERVIEWS, values, selection, selectionArgs);
                finish();
            }
        }
    }
}
