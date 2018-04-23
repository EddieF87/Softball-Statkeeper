package xyz.sleekstats.softball.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.FirestoreUpdateService;
import xyz.sleekstats.softball.data.GameUpdateIntentMaker;
import xyz.sleekstats.softball.data.MySyncResultReceiver;
import xyz.sleekstats.softball.data.TimeStampUpdater;
import xyz.sleekstats.softball.dialogs.DeleteConfirmationDialog;
import xyz.sleekstats.softball.dialogs.LineupSortDialog;
import xyz.sleekstats.softball.dialogs.PreviewSortDialog;
import xyz.sleekstats.softball.views.CustomViewPager;
import xyz.sleekstats.softball.adapters.PlayerStatsAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.dialogs.AddNewPlayersDialog;
import xyz.sleekstats.softball.dialogs.DeleteVsWaiversDialog;
import xyz.sleekstats.softball.dialogs.EditNameDialog;
import xyz.sleekstats.softball.dialogs.GameSettingsDialog;
import xyz.sleekstats.softball.dialogs.RemoveAllPlayersDialog;
import xyz.sleekstats.softball.fragments.LineupFragment;
import xyz.sleekstats.softball.fragments.TeamFragment;
import xyz.sleekstats.softball.objects.MainPageSelection;
import xyz.sleekstats.softball.objects.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamManagerActivity extends ExportActivity
        implements AddNewPlayersDialog.OnListFragmentInteractionListener,
        GameSettingsDialog.OnFragmentInteractionListener,
        RemoveAllPlayersDialog.OnFragmentInteractionListener,
        EditNameDialog.OnFragmentInteractionListener,
        MySyncResultReceiver.Receiver,
        LineupSortDialog.OnLineupSortListener,
        PreviewSortDialog.OnFragmentInteractionListener {

    private LineupFragment lineupFragment;
    private TeamFragment teamFragment;
    private String mTeamID;
    private int mLevel;
    private int mSelectionType;
    private String mTeamName;
    private MySyncResultReceiver mReceiver;
    private boolean gameUpdating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_viewpager);

        if(savedInstanceState != null){
            gameUpdating = savedInstanceState.getBoolean(StatsEntry.UPDATE, false);
        }

        getStatKeeperData();
        if(mTeamID == null) {
            goToMain();
            return;
        }
        startPager();

        if(!gameUpdating) {
            String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
            String[] selectionArgs = new String[]{mTeamID};
            Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_PLAYERS, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                sendRetryGameLoadIntent();
                cursor.close();
                return;
            }
            cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BACKUP_TEAMS, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                sendRetryGameLoadIntent();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void getStatKeeperData() {
        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mTeamName = mainPageSelection.getName();
            mTeamID = mainPageSelection.getId();
            mSelectionType = mainPageSelection.getType();
            mLevel = mainPageSelection.getLevel();
            setTitle(mTeamName + " (Team)");
        } catch (Exception e) {
            goToMain();
        }
    }

    private void goToMain() {
        Intent intent = new Intent(TeamManagerActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startPager() {
        CustomViewPager mViewPager = findViewById(R.id.team_view_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new TeamManagerPagerAdapter(fragmentManager));

        TabLayout tabLayout = findViewById(R.id.team_tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void sendRetryGameLoadIntent() {
        mReceiver = new MySyncResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        Intent intent = new Intent(TeamManagerActivity.this, FirestoreUpdateService.class);
        intent.putExtra(StatsEntry.UPDATE, mReceiver);
        intent.putExtra(FirestoreUpdateService.STATKEEPER_ID, mTeamID);
        intent.setAction(FirestoreUpdateService.INTENT_RETRY_GAME_LOAD);
        startService(intent);
    }

    @Override
    public void onSubmitPlayersListener(List<String> names, List<Integer> genders, String teamName, String teamID) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < names.size() - 1; i++) {
            ContentValues values = new ContentValues();
            String playerName = names.get(i);
            if (playerName.isEmpty()) {
                continue;
            }
            int gender = genders.get(i);
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_GENDER, gender);
            values.put(StatsEntry.COLUMN_TEAM, teamName);
            values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamID);
            values.put(StatsEntry.COLUMN_LEAGUE_ID, teamID);
            values.put(StatsEntry.COLUMN_ORDER, 99);
            values.put(StatsEntry.ADD, true);
            Uri uri = getContentResolver().insert(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values);
            if (uri != null) {
                String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
                String[] selectionArgs = new String[]{mTeamID};
                Cursor cursor = getContentResolver().query(uri, null, selection,
                        selectionArgs, null);
                if (cursor.moveToFirst()) {
                    players.add(new Player(cursor, false));
                }
                cursor.close();
            }
        }

        if (!players.isEmpty()) {
            TimeStampUpdater.updateTimeStamps(TeamManagerActivity.this, mTeamID, System.currentTimeMillis());

            if (lineupFragment != null) {
                lineupFragment.updateBench(players);
            }
            if (teamFragment != null) {
                teamFragment.addPlayers(players);
            }
        }
    }

    @Override
    public void onGameSettingsChanged(int innings, int genderSorter) {
        boolean genderSettingsOn = genderSorter != 0;

        if (lineupFragment != null) {
            lineupFragment.changeColorsRV(genderSettingsOn);
            lineupFragment.setGameSettings(innings, genderSorter);
        }
        if (teamFragment != null) {
            teamFragment.changeColorsRV(genderSettingsOn);
        }
    }

    @Override
    public void onRemoveChoice(int choice) {
        if (choice == DeleteVsWaiversDialog.CHOICE_DELETE) {
            if (teamFragment != null) {
                List<String> firestoreIDsToDelete = teamFragment.deletePlayers();
                if (lineupFragment != null && !firestoreIDsToDelete.isEmpty()) {
                    lineupFragment.removePlayers(firestoreIDsToDelete);
                }
            }
        }
    }

    @Override
    public void onEdit(String enteredText, int type) {
        if (enteredText.isEmpty()) {
            return;
        }
        boolean update = false;

        if (teamFragment != null) {
            update = teamFragment.updateTeamName(enteredText);
        }

        if (update) {
            TimeStampUpdater.updateTimeStamps(TeamManagerActivity.this, mTeamID, System.currentTimeMillis());
        }
    }

    @Override


    public void onLineupSortChoice(String awayID, String homeID, int inningAmt, int sortArg, int femaleOrder) {

        if (lineupFragment != null) {
            if(sortArg < 0) {
                ContentValues values = new ContentValues();
                values.put(StatsEntry.COLUMN_LEAGUE_ID, mTeamID);
                values.put(StatsEntry.COLUMN_FIRESTORE_ID, GameActivity.AUTO_OUT);
                values.put(StatsEntry.COLUMN_NAME, GameActivity.AUTO_OUT);
                values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, mTeamID);
                values.put(StatsEntry.COLUMN_GENDER, 1);
                values.put(StatsEntry.COLUMN_PLAYERID, -1);
                values.put(StatsEntry.COLUMN_ORDER, 101);
                getContentResolver().insert(StatsEntry.CONTENT_URI_TEMP, values);
            }

//            if(lineupFragment != null) {
//                lineupFragment.setStartButtonClickable();
//            }

            Intent intent = new Intent(TeamManagerActivity.this, TeamGameActivity.class);
            intent.putExtra(GameActivity.KEY_GENDERSORT, sortArg);
            intent.putExtra("isHome", lineupFragment.isHome());
            startActivityForResult(intent, GameActivity.REQUEST_CODE_GAME);
        }
    }

    @Override
    public void onCancelStart() {
        if(lineupFragment != null) {
            lineupFragment.setStartButtonClickable();
        }
    }

    @Override
    public void onShowPreview(int sortArg, int femaleOrder) {
        if (lineupFragment != null) {

            String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
            String[] selectionArgs = new String[]{mTeamID, mTeamID};
            Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEMP,
                    null, selection, selectionArgs, null);

            ArrayList<Player> oldList = new ArrayList<>();
            while (cursor.moveToNext()) {
                oldList.add(new Player(cursor, true));
            }
            cursor.close();

            ArrayList<Player> teamList;
            if(sortArg < 0) {
                teamList = addAutoOuts(oldList, femaleOrder);
            } else if (sortArg > 0) {
                teamList = genderSort(oldList, femaleOrder + 1);
            } else {
                teamList = oldList;
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            DialogFragment newFragment = PreviewSortDialog.newInstance(teamList);
            newFragment.setCancelable(false);
            newFragment.show(fragmentTransaction, "");
        }
    }

    @Override
    public void onReturnToSort() {
        if(lineupFragment != null) {
            lineupFragment.openLineupSortDialog(1);
        }
    }

    private ArrayList<Player> genderSort(ArrayList<Player> team, int femaleRequired) {

        List<Player> females = new ArrayList<>();
        List<Player> males = new ArrayList<>();
        int femaleIndex = 0;
        int maleIndex = 0;
        int firstFemale = 0;
        boolean firstFemaleSet = false;
        for (Player player : team) {
            if (player.getGender() == 1) {
                females.add(player);
                firstFemaleSet = true;
            } else {
                males.add(player);
            }
            if (!firstFemaleSet) {
                firstFemale++;
            }
        }
        if (females.isEmpty() || males.isEmpty()) {
            return team;
        }
        team.clear();
        if (firstFemale >= femaleRequired) {
            firstFemale = femaleRequired - 1;
        }
        for (int i = 0; i < firstFemale; i++) {
            team.add(males.get(maleIndex));
            maleIndex++;
            if (maleIndex >= males.size()) {
                maleIndex = 0;
            }
        }
        for (int i = 0; i < 100; i++) {
            if (i % femaleRequired == 0) {
                team.add(females.get(femaleIndex));
                femaleIndex++;
                if (femaleIndex >= females.size()) {
                    femaleIndex = 0;
                }
            } else {
                team.add(males.get(maleIndex));
                maleIndex++;
                if (maleIndex >= males.size()) {
                    maleIndex = 0;
                }
            }
        }
        return team;
    }


    private ArrayList<Player> addAutoOuts(ArrayList<Player> team, int femaleRequired) {
        boolean firstPlayerMale =  team.get(0).getGender() == 0;

        int menInARow = 0;
        int womenInARow = 0;
        int menInARowToStart = 0;
        int womenInARowToStart = 0;
        boolean toStart = true;

        for (int i = 0; i < team.size(); i++) {

            Player player = team.get(i);
            if (player.getGender() == 1) {
                womenInARow++;
                menInARow = 0;
                if(firstPlayerMale) {
                    toStart = false;
                }
            } else {
                menInARow++;
                womenInARow = 0;
                if(!firstPlayerMale) {
                    toStart = false;
                }
            }

            if(womenInARow > 1) {
                team.add(i, new Player(GameActivity.AUTO_OUT, "(AUTO-OUT)", mTeamID, 0));
                womenInARow = 1;
                i++;
                toStart = false;
            }
            if(menInARow > femaleRequired) {
                team.add(i, new Player(GameActivity.AUTO_OUT, "(AUTO-OUT)", mTeamID, 1));
                menInARow = 1;
                i++;
                toStart = false;
            }

            if(toStart) {
                if(womenInARow > 0) {
                    womenInARowToStart++;
                }
                if(menInARow > 0) {
                    menInARowToStart++;
                }
            }
        }
        if(menInARow + menInARowToStart > femaleRequired) {
            team.add(new Player(GameActivity.AUTO_OUT, "(AUTO-OUT)", mTeamID, 1));
        }
        if(womenInARow + womenInARowToStart > 1) {
            team.add(new Player(GameActivity.AUTO_OUT, "(AUTO-OUT)", mTeamID, 0));
        }

        return team;
    }

    private class TeamManagerPagerAdapter extends FragmentPagerAdapter {

        TeamManagerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TeamFragment.newInstance(mTeamID, mSelectionType, mTeamName, mLevel);
                case 1:
                    if (mLevel < UsersActivity.LEVEL_VIEW_WRITE) {
                        return null;
                    }
                    return LineupFragment.newInstance(mTeamID, mSelectionType, mTeamName, mTeamID, false);
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Stats";
                case 1:
                    return "Game";
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            if (mLevel < UsersActivity.LEVEL_VIEW_WRITE) {
                return 1;
            }
            return 2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);

            switch (position) {
                case 0:
                    teamFragment = (TeamFragment) createdFragment;
                    break;
                case 1:
                    lineupFragment = (LineupFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PlayerStatsAdapter.REQUEST_CODE) {
                switch (resultCode) {
                    case RESULT_OK:
                        String deletedPlayer = data.getStringExtra(StatsEntry.DELETE);
                        if (teamFragment != null) {
                            teamFragment.removePlayerFromTeam(deletedPlayer);
                        }
                        if (lineupFragment != null) {
                            List<String> players = new ArrayList<>();
                            players.add(deletedPlayer);
                            lineupFragment.removePlayers(players);
                        }
                        break;
                    case 17:
                        if (teamFragment != null) {
                            String id = data.getStringExtra(StatsEntry.COLUMN_FIRESTORE_ID);
                            int gender = data.getIntExtra(StatsEntry.COLUMN_GENDER, -1);

                            if (teamFragment != null) {
                                teamFragment.updatePlayerGender(gender, id);
                            }

                            if (lineupFragment != null) {
                                lineupFragment.updatePlayerGender(gender, id);
                            }
                        }
                        break;
                    case 18:
                        if (teamFragment != null) {
                            String id = data.getStringExtra(StatsEntry.COLUMN_FIRESTORE_ID);
                            String name = data.getStringExtra(StatsEntry.COLUMN_NAME);

                            if (teamFragment != null) {
                                teamFragment.updatePlayerName(name, id);
                            }

                            if (lineupFragment != null) {
                                lineupFragment.updatePlayerName(name, id);
                            }
                        }
                        break;
                    case 19:
                        if (teamFragment != null) {
                            String id = data.getStringExtra(StatsEntry.COLUMN_FIRESTORE_ID);
                            String name = data.getStringExtra(StatsEntry.COLUMN_NAME);

                            if (teamFragment != null) {
                                teamFragment.updatePlayerName(name, id);
                            }

                            if (lineupFragment != null) {
                                lineupFragment.updatePlayerName(name, id);
                            }
                        }


                        break;
                }
            } else if (requestCode == GameActivity.REQUEST_CODE_GAME && resultCode == GameActivity.RESULT_CODE_GAME_FINISHED) {
                gameUpdating = true;
                updateStats(data);
            }
        } catch (Exception ex) {
            Toast.makeText(TeamManagerActivity.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void updateStats(Intent data) {

        if (mTeamID == null) {
            getStatKeeperData();
        }
        long updateTime = System.currentTimeMillis();

        String awayID = data.getStringExtra(StatsEntry.COLUMN_AWAY_TEAM);
        String homeID = data.getStringExtra(StatsEntry.COLUMN_HOME_TEAM);

        int myRuns = data.getIntExtra(StatsEntry.COLUMN_RUNSFOR, -1);
        int theirRuns = data.getIntExtra(StatsEntry.COLUMN_RUNSAGAINST, -1);

        int awayTeamRuns;
        int homeTeamRuns;
        if (awayID.equals(StatsEntry.COLUMN_AWAY_TEAM)) {
            awayTeamRuns = theirRuns;
            homeTeamRuns = myRuns;
        } else if (homeID.equals(StatsEntry.COLUMN_HOME_TEAM)) {
            awayTeamRuns = myRuns;
            homeTeamRuns = theirRuns;
        } else {
            Toast.makeText(TeamManagerActivity.this, "Error with saving game. Please try again!", Toast.LENGTH_LONG).show();
            return;
        }

        mReceiver = new MySyncResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        Context context = TeamManagerActivity.this;
        startService(GameUpdateIntentMaker.getTransferIntent(context, updateTime, mTeamID, null, myRuns, theirRuns, mTeamID, mReceiver));
        startService(GameUpdateIntentMaker.getTeamIntent(context, updateTime, mTeamID, myRuns, theirRuns, mTeamID, mReceiver));
        startService(GameUpdateIntentMaker.getPlayersIntent(context, updateTime, mTeamID, mReceiver));
        startService(GameUpdateIntentMaker.getBoxscoreIntent(context, updateTime, awayID, homeID, awayTeamRuns, homeTeamRuns, mTeamID, mReceiver));

//        TimeStampUpdater.updateTimeStamps(this, mTeamID, updateTime);
    }

    private int localUpdate = 4;
    private int firestoreUpdate = 3;

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        switch (resultCode) {
            case FirestoreUpdateService.MSG_UPDATE_SUCCESS:
                localUpdate--;
                break;

            case FirestoreUpdateService.MSG_FIRESTORE_SUCCESS:
                firestoreUpdate--;
                break;

            case FirestoreUpdateService.MSG_TRANSFER_SUCCESS:
                localUpdate--;
                if(lineupFragment != null) {
                    lineupFragment.setPostGameLayout(true);
                }
                break;

            case FirestoreUpdateService.MSG_RETRY_SUCCESS:
                Toast.makeText(TeamManagerActivity.this, R.string.stat_update_success, Toast.LENGTH_LONG).show();
                break;

            case FirestoreUpdateService.MSG_FIRESTORE_FAILURE:
                Toast.makeText(TeamManagerActivity.this, R.string.cloud_fail, Toast.LENGTH_LONG).show();
                break;

            case FirestoreUpdateService.MSG_RETRY_FAILURE:
                Toast.makeText(TeamManagerActivity.this, getString(R.string.cloud_fail) +
                        "\nIf the problem persists, please contact me at sleekstats@gmail.com", Toast.LENGTH_LONG).show();
                break;

            case FirestoreUpdateService.MSG_TRANSFER_FAILURE:
                Toast.makeText(TeamManagerActivity.this, "Error transferring stats.\nPlease try again.", Toast.LENGTH_LONG).show();
                localUpdate = 999;
                if(lineupFragment != null) {
                    lineupFragment.onTransferError();
                }
                break;
        }

        boolean localUpdateFinish = localUpdate < 1;
        boolean firestoreUpdateFinish = firestoreUpdate < 1;
        if (localUpdateFinish) {
            localUpdate = 4;
            Toast.makeText(TeamManagerActivity.this, R.string.stat_update_success, Toast.LENGTH_SHORT).show();
            gameUpdating = false;
            if (teamFragment != null) {
                teamFragment.reloadStats();
            }
        }
        if (firestoreUpdateFinish) {
            firestoreUpdate = 3;
            Toast.makeText(TeamManagerActivity.this, "Stats have been uploaded to cloud!", Toast.LENGTH_SHORT).show();
        }
        if (localUpdateFinish && firestoreUpdateFinish) {
            if(mReceiver != null){
                mReceiver.setReceiver(null);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mReceiver != null){
            mReceiver.setReceiver(null);
        }
        goToMain();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        if(mReceiver != null) {
            mReceiver.setReceiver(null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(gameUpdating) {
//            LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(StatsEntry.UPDATE));
            if(mReceiver == null) {
                mReceiver = new MySyncResultReceiver(new Handler());
            }
            mReceiver.setReceiver(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(StatsEntry.UPDATE, gameUpdating);
    }
}