package com.example.android.scorekeepdraft1.activities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.dialogs.CreateTeamDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.DeleteConfirmationDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.GameSettingsDialogFragment;
import com.example.android.scorekeepdraft1.fragments.PlayerFragment;
import com.example.android.scorekeepdraft1.fragments.TeamFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.List;

public class ObjectPagerActivity extends AppCompatActivity
        implements CreateTeamDialogFragment.OnListFragmentInteractionListener,
        GameSettingsDialogFragment.OnFragmentInteractionListener,
        DeleteConfirmationDialogFragment.OnFragmentInteractionListener{

    private List<Integer> objectIDs;
    private ViewPager mViewPager;
    private int selectionType;
    private int level;
    private String leagueID;
    private String leagueName;
    private int mObjectType;
    private Uri mUri;
    private MyFragmentStatePagerAdapter mAdapter;
    private static final int KEY_TEAM_PAGER = 0;
    private static final int KEY_PLAYER_PAGER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_pager);
    }

    protected void startPager(int objectType,  Uri uri) {

        MyApp myApp = (MyApp) getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        if (mainPageSelection == null) {
            Intent nullIntent = new Intent(this, MainActivity.class);
            startActivity(nullIntent);
            finish();
        }
        selectionType = mainPageSelection.getType();
        leagueName = mainPageSelection.getName();
        leagueID = mainPageSelection.getId();
        level = mainPageSelection.getLevel();
        mObjectType = objectType;
        mUri = uri;
        setTitle(leagueName);

        objectIDs = new ArrayList<>();
        String sortOrder;
        if (mObjectType == KEY_PLAYER_PAGER) {
            sortOrder = StatsEntry.COLUMN_TEAM + " COLLATE NOCASE ASC, " + StatsEntry.COLUMN_NAME + " COLLATE NOCASE ASC";
        } else {
            sortOrder = StatsEntry.COLUMN_NAME + " COLLATE NOCASE ASC";
        }
        Cursor cursor = getContentResolver().query(uri, null,
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

    @Override
    public void onSubmitPlayersListener(List<String> names, List<Integer> genders, String team) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < names.size() - 1; i++) {
            ContentValues values = new ContentValues();
            String playerName = names.get(i);
            if (playerName.isEmpty()) {
                continue;
            }
            int gender = genders.get(i);
            values.put(StatsContract.StatsEntry.COLUMN_NAME, playerName);
            values.put(StatsContract.StatsEntry.COLUMN_GENDER, gender);
            values.put(StatsContract.StatsEntry.COLUMN_TEAM, team);
            Uri uri = getContentResolver().insert(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, values);
            if (uri != null) {
                Cursor cursor = getContentResolver().query(uri, null, null,
                        null, null);
                if (cursor.moveToFirst()) {
                    String firestoreID = cursor.getString(cursor
                            .getColumnIndex(StatsContract.StatsEntry.COLUMN_FIRESTORE_ID));
                    long id = ContentUris.parseId(uri);
                    players.add(new Player(playerName, team, gender, id, firestoreID));
                }
            }
        }

        if (!players.isEmpty()) {
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
                teamFragment.deleteTeam();
            }
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
                    return TeamFragment.newInstance(leagueID, selectionType, leagueName, level, currentObjectUri);
                case 1:
                    return PlayerFragment.newInstance(selectionType, level, currentObjectUri);
                default:
                    return null;
            }
        }

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

}

