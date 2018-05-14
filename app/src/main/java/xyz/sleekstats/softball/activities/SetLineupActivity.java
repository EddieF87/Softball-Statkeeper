package xyz.sleekstats.softball.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;


import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.data.TimeStampUpdater;
import xyz.sleekstats.softball.dialogs.AddNewPlayersDialog;
import xyz.sleekstats.softball.dialogs.GameSettingsDialog;
import xyz.sleekstats.softball.fragments.LineupFragment;
import xyz.sleekstats.softball.models.MainPageSelection;
import xyz.sleekstats.softball.models.Player;

import java.util.ArrayList;
import java.util.List;

import static xyz.sleekstats.softball.activities.GameActivity.KEY_GENDERSORT;


public class SetLineupActivity extends SingleFragmentActivity
        implements AddNewPlayersDialog.OnListFragmentInteractionListener,
        GameSettingsDialog.OnFragmentInteractionListener {

    private LineupFragment lineupFragment;
    private String mStatKeeperID;
    private int mType;
    private String mTeamID;
    private boolean mInGame;

    @Override
    protected Fragment createFragment() {
        Bundle args = getIntent().getExtras();
        String teamName = null;

        if (args != null) {
            teamName = args.getString("team_name");
            mTeamID = args.getString("team_id");
            mInGame = args.getBoolean("ingame");
        } else {
            finish();
        }
        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mType = mainPageSelection.getType();
            mStatKeeperID = mainPageSelection.getId();
            if(mStatKeeperID == null) {
                goToMain();
            }
            lineupFragment = LineupFragment.newInstance(mStatKeeperID, mType, teamName, mTeamID, mInGame);
        } catch (Exception e) {
            goToMain();
        }
        return lineupFragment;
    }

    private void goToMain() {
        Intent intent = new Intent(SetLineupActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSubmitPlayersListener(List<String> names, List<Integer> genders, String team, String teamID) {
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < names.size() - 1; i++) {
            ContentValues values = new ContentValues();
            String name = names.get(i);
            if (name.isEmpty()) {
                continue;
            }
            int gender = genders.get(i);
            values.put(StatsEntry.COLUMN_LEAGUE_ID, mStatKeeperID);
            values.put(StatsEntry.COLUMN_NAME, name);
            values.put(StatsEntry.COLUMN_GENDER, gender);
            values.put(StatsEntry.COLUMN_TEAM, team);
            values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamID);
            values.put(StatsEntry.COLUMN_ORDER, 99);
            values.put(StatsEntry.ADD, true);
            Uri uri = getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
            if (uri != null) {
                String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
                String[] selectionArgs = new String[]{mStatKeeperID};
                Cursor cursor = getContentResolver().query(uri, null, selection, selectionArgs, null);
                if(cursor.moveToFirst()) {
                    Player player = new Player(cursor, false);
                    players.add(player);
                }
                cursor.close();
            }
        }
        if (!players.isEmpty()) {
            TimeStampUpdater.updateTimeStamps(this, mStatKeeperID, System.currentTimeMillis());

            if(lineupFragment != null) {
                lineupFragment.updateBench(players);
            }
        }
    }

    @Override
    public void onGameSettingsChanged(int innings, int genderSorter, int mercyRuns) {
        boolean genderSettingsOn = genderSorter != 0;

        if (lineupFragment != null) {
            lineupFragment.changeColorsRV(genderSettingsOn);
        }
        setResult(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(!mInGame){return;}
        Intent intent;
        SharedPreferences gamePreferences = getSharedPreferences(mStatKeeperID + StatsEntry.GAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = gamePreferences.edit();
        if (mType == MainPageSelection.TYPE_LEAGUE) {
            String awayTeam = gamePreferences.getString(StatsEntry.COLUMN_AWAY_TEAM, null);
            String homeTeam = gamePreferences.getString(StatsEntry.COLUMN_HOME_TEAM, null);
            int sortArgument = gamePreferences.getInt(KEY_GENDERSORT, 0);

            switch (sortArgument) {
                case 3:
                    if (mTeamID.equals(awayTeam)) {
                        sortArgument = 2;
                    } else if (mTeamID.equals(homeTeam)) {
                        sortArgument = 1;
                    }
                    break;

                case 2:
                    if (mTeamID.equals(homeTeam)) {
                        sortArgument = 0;
                    }
                    break;

                case 1:
                    if (mTeamID.equals(awayTeam)) {
                        sortArgument = 0;
                    }
                    break;
            }

            intent = new Intent(SetLineupActivity.this, LeagueGameActivity.class);
            editor.putInt(KEY_GENDERSORT, sortArgument);
        } else {
            intent = new Intent(SetLineupActivity.this, TeamGameActivity.class);
            editor.putInt(KEY_GENDERSORT, 0);
        }
        editor.apply();
        startActivity(intent);
    }
}

