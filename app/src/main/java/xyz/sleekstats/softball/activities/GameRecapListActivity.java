package xyz.sleekstats.softball.activities;

import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.GameRecapRecyclerViewAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.objects.GameRecap;

public class GameRecapListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private final static int TEAM_NAME_LOADER = 11;
    private final static int GAME_RECAP_LOADER = 12;
    private GameRecapRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private List<GameRecap> mGameRecaps;
    private Map<String, String> mTeamNames;

    private int loadsFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_recap_list);
        Log.d("zztop", "        super.onCreate(savedInstanceState);\n");

        loadsFinished = 0;
        getSupportLoaderManager().initLoader(TEAM_NAME_LOADER, null, this);
        getSupportLoaderManager().initLoader(GAME_RECAP_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection;
        Uri uri;
        if(id == TEAM_NAME_LOADER) {
            uri = StatsEntry.CONTENT_URI_TEAMS;
            projection = new String[] {StatsEntry.COLUMN_FIRESTORE_ID, StatsEntry.COLUMN_NAME};
        } else if(id == GAME_RECAP_LOADER) {
            uri = StatsEntry.CONTENT_URI_BOXSCORE_OVERVIEWS;
            projection = null;
        } else {
            return null;
        }

        return new CursorLoader(this, uri,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        data.moveToPosition(-1);
        Log.d("zztop", "onLoadFinished");
        if(id == TEAM_NAME_LOADER) {
            Log.d("zztop", "TEAM_NAME_LOADER");
            mTeamNames = new HashMap<>();
            while (data.moveToNext()){
                String teamID = StatsContract.getColumnString(data, StatsEntry.COLUMN_FIRESTORE_ID);
                String teamName = StatsContract.getColumnString(data, StatsEntry.COLUMN_NAME);
                mTeamNames.put(teamID, teamName);
                Log.d("zztop", "mTeamNames + " + teamName);
            }
            loadsFinished++;
        } else if(id == GAME_RECAP_LOADER) {
            Log.d("zztop", "GAME_RECAP_LOADER");
            mGameRecaps = new ArrayList<>();
            while (data.moveToNext()){
                mGameRecaps.add(new GameRecap(data));
                Log.d("zztop", "mGameRecaps + ");
            }
            loadsFinished++;
        }
        Log.d("zztop", "loadsFinished " + loadsFinished);
        if(loadsFinished > 1) {
            Log.d("zztop", "loadsFinished > 1");
            if(mTeamNames == null || mGameRecaps == null) {
                Log.d("zztop", "ERROR:  mTeamNames == null || mGameRecaps == null)");
                return;
            }
            mAdapter = new GameRecapRecyclerViewAdapter(mGameRecaps, mTeamNames, this);
            mRecyclerView = findViewById(R.id.rv_games);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        loadsFinished = 0;
        Log.d("zztop", "onLoaderReset");
    }

}
