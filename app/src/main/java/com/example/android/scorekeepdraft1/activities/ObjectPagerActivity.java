package com.example.android.scorekeepdraft1.activities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.FirestoreHelper;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.dialogs.AddNewPlayersDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.ChangeTeamDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.DeleteConfirmationDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.DeleteVsWaiversDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.EditNameDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.GameSettingsDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.RemoveAllPlayersDialogFragment;
import com.example.android.scorekeepdraft1.fragments.PlayerFragment;
import com.example.android.scorekeepdraft1.fragments.TeamFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.List;

public class ObjectPagerActivity extends AppCompatActivity
        implements AddNewPlayersDialogFragment.OnListFragmentInteractionListener,
        GameSettingsDialogFragment.OnFragmentInteractionListener,
        DeleteConfirmationDialogFragment.OnFragmentInteractionListener,
        DeleteVsWaiversDialogFragment.OnFragmentInteractionListener,
        RemoveAllPlayersDialogFragment.OnFragmentInteractionListener,
        EditNameDialogFragment.OnFragmentInteractionListener,
        ChangeTeamDialogFragment.OnFragmentInteractionListener{

    private List<Integer> objectIDs;
    private ViewPager mViewPager;
    private int selectionType;
    private int level;
    private String selectionID;
    private String selectionName;
    private int mObjectType;
    private Uri mUri;
    private MyFragmentStatePagerAdapter mAdapter;
    private static final int KEY_TEAM_PAGER = 0;
    private static final int KEY_PLAYER_PAGER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_pager);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    protected void startPager(int objectType,  Uri uri) {
        setLeagueInfo();

        mObjectType = objectType;
        mUri = uri;
        setTitle(selectionName);

        objectIDs = new ArrayList<>();
        String sortOrder;
        if (mObjectType == KEY_PLAYER_PAGER) {
            sortOrder = StatsEntry.COLUMN_TEAM + " COLLATE NOCASE ASC, " + StatsEntry.COLUMN_NAME + " COLLATE NOCASE ASC";
        } else {
            sortOrder = StatsEntry.COLUMN_NAME + " COLLATE NOCASE ASC";
        }

        Cursor cursor = getContentResolver().query(uri, new String[]{StatsEntry._ID},
                null, null, sortOrder);
        while (cursor.moveToNext()) {
            int objectID = cursor.getInt(cursor.getColumnIndex(StatsEntry._ID));
            objectIDs.add(objectID);
        }
        cursor.close();

        if (mObjectType == KEY_TEAM_PAGER) {
            objectIDs.add(-1);
        }

        Intent intent = getIntent();
        Uri objectURI = intent.getData();
        int objectID;
        if (objectURI != null) {
            objectID = (int) ContentUris.parseId(objectURI);
        } else {
            objectID = -1;
        }

        mViewPager = findViewById(R.id.view_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mAdapter = new MyFragmentStatePagerAdapter(fragmentManager);
        mViewPager.setAdapter(mAdapter);

        for (int pagerPosition = 0; pagerPosition < objectIDs.size(); pagerPosition++) {
            if (objectIDs.get(pagerPosition) == objectID) {
                mViewPager.setCurrentItem(pagerPosition);;
            }
        }
    }

    private void setLeagueInfo() {
        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            selectionType = mainPageSelection.getType();
            selectionName = mainPageSelection.getName();
            selectionID = mainPageSelection.getId();
            level = mainPageSelection.getLevel();
        } catch (Exception e) {
            Intent intent = new Intent(ObjectPagerActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onSubmitPlayersListener(List<String> names, List<Integer> genders, String teamName, String teamID) {
        List<Player> players = new ArrayList<>();
        FirestoreHelper firestoreHelper = new FirestoreHelper(this, selectionID);
        for (int i = 0; i < names.size() - 1; i++) {
            ContentValues values = new ContentValues();
            String playerName = names.get(i);
            if (playerName.isEmpty()) {
                continue;
            }
            int gender = genders.get(i);
            values.put(StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsEntry.COLUMN_GENDER, gender);
            values.put(StatsEntry.COLUMN_ORDER, 99);
            values.put(StatsEntry.COLUMN_TEAM, teamName);
            values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamID);
            values.put(StatsEntry.ADD, true);
            Uri uri = getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
            if (uri != null) {
                Cursor cursor = getContentResolver().query(uri, null, null,
                        null, null);
                if (cursor.moveToFirst()) {
                    String firestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
                    players.add(new Player(cursor, false));
                    firestoreHelper.setUpdate(firestoreID, 1);
                }
            }
        }


        if (!players.isEmpty()) {
            firestoreHelper.updateTimeStamps();
            int pos = mViewPager.getCurrentItem();
            TeamFragment teamFragment = (TeamFragment) mAdapter.getRegisteredFragment(pos);
            if (teamFragment != null) {
                teamFragment.addPlayers(players);
            }
        }
    }

    @Override
    public void onGameSettingsChanged(int innings, int genderSorter) {
        boolean genderSettingsOn = genderSorter != 0;

        int pos = mViewPager.getCurrentItem();
        TeamFragment teamFragment = (TeamFragment) mAdapter.getRegisteredFragment(pos);
        if (teamFragment != null) {
            teamFragment.changeColorsRV(genderSettingsOn);
        }
    }

    @Override
    public void onDeletionChoice(boolean delete) {
        if(!delete) {
            return;
        }
        int pos = mViewPager.getCurrentItem();

        if (mObjectType == KEY_PLAYER_PAGER) {
            PlayerFragment playerFragment = (PlayerFragment) mAdapter.getRegisteredFragment(pos);
            if (playerFragment != null) {
                playerFragment.deletePlayer();
            }
        } else if (mObjectType == KEY_TEAM_PAGER){
            TeamFragment teamFragment = (TeamFragment) mAdapter.getRegisteredFragment(pos);
            if (teamFragment != null) {
                teamFragment.showDeleteVsWaiversDialog();
            }
        }
        new FirestoreHelper(this, selectionID).updateTimeStamps();
    }

    @Override
    public void onDeleteVsWaiversChoice(int choice) {
        if (mObjectType != KEY_TEAM_PAGER){
            return;
        }
        int pos = mViewPager.getCurrentItem();
        TeamFragment teamFragment = (TeamFragment) mAdapter.getRegisteredFragment(pos);

        if (choice == DeleteVsWaiversDialogFragment.CHOICE_WAIVERS) {
            teamFragment.updatePlayersTeam(StatsEntry.FREE_AGENT);
            teamFragment.deleteTeam();
        } else if (choice == DeleteVsWaiversDialogFragment.CHOICE_DELETE) {
            teamFragment.deletePlayers();
            teamFragment.deleteTeam();
        }
    }

    @Override
    public void onRemoveChoice(int choice) {
        if (mObjectType != KEY_TEAM_PAGER){
            return;
        }
        int pos = mViewPager.getCurrentItem();
        TeamFragment teamFragment = (TeamFragment) mAdapter.getRegisteredFragment(pos);

        if (choice == DeleteVsWaiversDialogFragment.CHOICE_WAIVERS) {
            teamFragment.updatePlayersTeam(StatsEntry.FREE_AGENT);
            teamFragment.clearPlayers();
            teamFragment.setEmptyViewVisible();

        } else if (choice == DeleteVsWaiversDialogFragment.CHOICE_DELETE) {
            teamFragment.deletePlayers();
        }
    }

    @Override
    public void onEdit(String enteredText) {
        if (enteredText.isEmpty()) {
            return;
        }
        int pos = mViewPager.getCurrentItem();
        boolean update = false;

        if (mObjectType == KEY_PLAYER_PAGER) {
            PlayerFragment playerFragment = (PlayerFragment) mAdapter.getRegisteredFragment(pos);
            if (playerFragment != null) {
                update = playerFragment.updatePlayerName(enteredText);
            }
        } else if (mObjectType == KEY_TEAM_PAGER) {
            TeamFragment teamFragment = (TeamFragment) mAdapter.getRegisteredFragment(pos);
            if (teamFragment != null) {
                update = teamFragment.updateTeamName(enteredText);
            }
        }

        if(update) {
            new FirestoreHelper(this, selectionID).updateTimeStamps();
        }
    }

    @Override
    public void onTeamChosen(String playerFirestoreID, String teamName, String teamFirestoreID) {
        int pos = mViewPager.getCurrentItem();

        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = new String[]{playerFirestoreID};

        ContentValues values = new ContentValues();
        values.put(StatsEntry.COLUMN_TEAM, teamName);
        values.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamFirestoreID);
        values.put(StatsEntry.COLUMN_ORDER, 99);
        values.put(StatsEntry.COLUMN_FIRESTORE_ID, playerFirestoreID);
        getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS, values, selection, selectionArgs);

        if(selectionID == null) {
            setLeagueInfo();
        }
        FirestoreHelper firestoreHelper = new FirestoreHelper(ObjectPagerActivity.this, selectionID);
        firestoreHelper.setUpdate(playerFirestoreID, 1);
        firestoreHelper.updateTimeStamps();

        if (mObjectType == KEY_TEAM_PAGER) {
            TeamFragment teamFragment = (TeamFragment) mAdapter.getRegisteredFragment(pos);
            if (teamFragment != null) {
                teamFragment.removePlayerFromTeam(playerFirestoreID);
            }
            new FirestoreHelper(this, selectionID).updateTimeStamps();
        }
    }

    private class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter{

        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        MyFragmentStatePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            int id = objectIDs.get(position);
            Uri currentObjectUri;
            if (id == -1) {
                currentObjectUri = mUri;
            } else {
                currentObjectUri = ContentUris.withAppendedId(mUri, id);
            }
            switch (mObjectType) {
                case 0:
                    return TeamFragment.newInstance(selectionID, selectionType, selectionName, level, currentObjectUri);
                case 1:
                    return PlayerFragment.newInstance(selectionID, selectionType, level, currentObjectUri);
                default:
                    return null;
            }
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        @Override
        public int getCount() {
            return objectIDs.size();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent;
                if(selectionType == MainPageSelection.TYPE_LEAGUE) {
                    intent = new Intent(ObjectPagerActivity.this, LeagueManagerActivity.class);
                } else if (selectionType == MainPageSelection.TYPE_TEAM) {
                    intent = new Intent(ObjectPagerActivity.this, TeamManagerActivity.class);
                } else {
                    return false;
                }
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

