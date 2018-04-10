package xyz.sleekstats.softball.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.GameRecapRecyclerViewAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.objects.GameRecap;
import xyz.sleekstats.softball.objects.MainPageSelection;

public class GameRecapListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener{

    private final static int TEAM_NAME_LOADER = 11;
    private final static int GAME_RECAP_LOADER = 12;
    private GameRecapRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Spinner mTeamSpinner;
    private List<GameRecap> mGameRecaps;
    private Map<String, String> mTeamNames;
    private Map<Integer, String> mSpinnerMap;
    private String mSelectionArg;
    private String mStatKeeperID;
    private int mSelectionType;
    private static final String KEY_SELECTION_ARG = "keySelectionArg";
    private static final String KEY_ALL_TEAMS = "All Teams";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_recap_list);

        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mStatKeeperID = mainPageSelection.getId();
            mSelectionType = mainPageSelection.getType();
        } catch (Exception e) {
            Intent intent = new Intent(GameRecapListActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        mRecyclerView = findViewById(R.id.rv_games);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mTeamSpinner = findViewById(R.id.spinner_recap_teams);

        if(mSelectionType == MainPageSelection.TYPE_TEAM) {
            findViewById(R.id.spinner_recap_teams).setVisibility(View.INVISIBLE);
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        if(savedInstanceState != null) {
            mSelectionArg = savedInstanceState.getString(KEY_SELECTION_ARG);
        } else if(action != null && action.equals(StatsEntry.COLUMN_TEAM_ID)) {
            mSelectionArg = intent.getStringExtra(StatsEntry.COLUMN_FIRESTORE_ID);
        }

        getSupportLoaderManager().initLoader(TEAM_NAME_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection;
        Uri uri;
        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String sortOrder;
        String[] selectionArgs;
        switch (id) {
            case TEAM_NAME_LOADER:
                uri = StatsEntry.CONTENT_URI_TEAMS;
                projection = new String[]{StatsEntry.COLUMN_FIRESTORE_ID, StatsEntry.COLUMN_NAME};
                selectionArgs = new String[]{mStatKeeperID};
                sortOrder = StatsEntry.COLUMN_NAME + " COLLATE NOCASE ASC";
                break;
            case GAME_RECAP_LOADER:
                if (mSelectionArg != null) {
                    selection = StatsEntry.COLUMN_AWAY_TEAM + "=? OR " + StatsEntry.COLUMN_HOME_TEAM + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
                    selectionArgs = new String[]{mSelectionArg, mSelectionArg, mStatKeeperID};
                } else {
                    selectionArgs = new String[]{mStatKeeperID};
                }
                sortOrder = StatsEntry.COLUMN_GAME_ID + " DESC";
                uri = StatsEntry.CONTENT_URI_BOXSCORE_OVERVIEWS;
                projection = null;
                break;
            default:
                return null;
        }

        return new CursorLoader(this, uri,
                projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        data.moveToPosition(-1);

        if(id == TEAM_NAME_LOADER) {
            mTeamNames = new HashMap<>();
            mSpinnerMap = new HashMap<>();
            List<String> teamsArray = new ArrayList<>();
            int i = 0;
            mSpinnerMap.put(i, KEY_ALL_TEAMS);
            teamsArray.add(KEY_ALL_TEAMS);
            i++;
            int startPosition = 0;
            while (data.moveToNext()){
                String teamID = StatsContract.getColumnString(data, StatsEntry.COLUMN_FIRESTORE_ID);
                String teamName = StatsContract.getColumnString(data, StatsEntry.COLUMN_NAME);
                mTeamNames.put(teamID, teamName);
                teamsArray.add(teamName);
                mSpinnerMap.put(i, teamID);
                if(teamID.equals(mSelectionArg)) {
                    startPosition = i;
                }
                i++;
            }
            if(mSelectionType != MainPageSelection.TYPE_TEAM) {
                ArrayAdapter<String> mSpinnerAdapter = new ArrayAdapter<>(this,
                        R.layout.spinner_layout, teamsArray);
                mTeamSpinner.setAdapter(mSpinnerAdapter);
                if(mSelectionArg != null) {
                    mTeamSpinner.setSelection(startPosition);
                }
                mTeamSpinner.setOnItemSelectedListener(this);
            }
            getSupportLoaderManager().initLoader(GAME_RECAP_LOADER, null, this);
        } else if(id == GAME_RECAP_LOADER) {
            if(mGameRecaps == null) {
                mGameRecaps = new ArrayList<>();
            } else {
                mGameRecaps.clear();
            }
            while (data.moveToNext()){
                mGameRecaps.add(new GameRecap(data));
            }
            if(mTeamNames == null || mGameRecaps == null) {
                return;
            }
            if(mAdapter == null) {
                mAdapter = new GameRecapRecyclerViewAdapter(mGameRecaps, mTeamNames, this);
                mRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        int position = adapterView.getSelectedItemPosition();
        String teamID = mSpinnerMap.get(position);

        if (view != null) {
            if (mSelectionArg == null || !mSelectionArg.equals(teamID)) {
                if(teamID.equals(KEY_ALL_TEAMS)) {
                    teamID = null;
                }
                mSelectionArg = teamID;
                getSupportLoaderManager().restartLoader(GAME_RECAP_LOADER, null, this);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SELECTION_ARG, mSelectionArg);
    }
}
