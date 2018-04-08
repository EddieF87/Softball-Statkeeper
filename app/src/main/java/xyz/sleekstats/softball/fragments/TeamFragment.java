package xyz.sleekstats.softball.fragments;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.activities.GameRecapListActivity;
import xyz.sleekstats.softball.activities.MainActivity;
import xyz.sleekstats.softball.activities.SetLineupActivity;
import xyz.sleekstats.softball.activities.TeamManagerActivity;
import xyz.sleekstats.softball.activities.UsersActivity;
import xyz.sleekstats.softball.adapters.PlayerStatsAdapter;
import xyz.sleekstats.softball.data.FirestoreUpdateService;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.data.TimeStampUpdater;
import xyz.sleekstats.softball.dialogs.AddNewPlayersDialog;
import xyz.sleekstats.softball.dialogs.DeleteConfirmationDialog;
import xyz.sleekstats.softball.dialogs.DeleteVsWaiversDialog;
import xyz.sleekstats.softball.dialogs.EditNameDialog;
import xyz.sleekstats.softball.dialogs.GameSettingsDialog;
import xyz.sleekstats.softball.dialogs.RemoveAllPlayersDialog;
import xyz.sleekstats.softball.objects.MainPageSelection;
import xyz.sleekstats.softball.objects.Player;
import xyz.sleekstats.softball.objects.Team;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TeamFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private Uri mCurrentTeamUri;
    private static final int EXISTING_TEAM_LOADER = 3;

    private LinearLayout totalLayout;
    private TextView teamNameView;
    private TextView teamRecordView;
    private RecyclerView rv;
    private PlayerStatsAdapter mAdapter;

    private List<Player> mPlayers;
    private String teamName;
    private String teamFirestoreID;
    private boolean waivers;

    private int statSort;
    private TextView colorView;

    private int mSelectionType;
    private String mSelectionID;
    private String mSelectionName;
    private int mLevel;
    private static final String KEY_TEAM_URI = "teamURI";
    private static final String KEY_STAT_SORT = "keyStatSort";


    public TeamFragment() {
        // Required empty public constructor
    }

    public static TeamFragment newInstance(String leagueID, int leagueType, String leagueName, int level) {
        Bundle args = new Bundle();
        args.putString(MainPageSelection.KEY_SELECTION_ID, leagueID);
        args.putInt(MainPageSelection.KEY_SELECTION_TYPE, leagueType);
        args.putInt(MainPageSelection.KEY_SELECTION_LEVEL, level);
        args.putString(MainPageSelection.KEY_SELECTION_NAME, leagueName);
        TeamFragment fragment = new TeamFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static TeamFragment newInstance(String leagueID, int leagueType, String leagueName, int level, Uri uri) {
        Bundle args = new Bundle();
        args.putString(MainPageSelection.KEY_SELECTION_ID, leagueID);
        args.putInt(MainPageSelection.KEY_SELECTION_TYPE, leagueType);
        args.putInt(MainPageSelection.KEY_SELECTION_LEVEL, level);
        args.putString(MainPageSelection.KEY_SELECTION_NAME, leagueName);
        args.putString(KEY_TEAM_URI, uri.toString());
        TeamFragment fragment = new TeamFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        mSelectionID = args.getString(MainPageSelection.KEY_SELECTION_ID);
        mSelectionType = args.getInt(MainPageSelection.KEY_SELECTION_TYPE);
        mLevel = args.getInt(MainPageSelection.KEY_SELECTION_LEVEL);
        mSelectionName = args.getString(MainPageSelection.KEY_SELECTION_NAME);
        String uriString = args.getString(KEY_TEAM_URI, null);

        if (uriString != null) {
            mCurrentTeamUri = Uri.parse(uriString);
        } else {
            mCurrentTeamUri = StatsEntry.CONTENT_URI_TEAMS;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            statSort = savedInstanceState.getInt(KEY_STAT_SORT, -1);
        } else {
            statSort = -1;
        }

        View rootView = inflater.inflate(R.layout.fragment_team, container, false);

        waivers = false;


        if (mSelectionType == MainPageSelection.TYPE_TEAM) {
            teamName = mSelectionName;
        } else {
            if (mCurrentTeamUri.equals(StatsEntry.CONTENT_URI_TEAMS)) {
                waivers = true;
                View titleLayout = rootView.findViewById(R.id.team_stats_titles);
                View teamAbvView = titleLayout.findViewById(R.id.team_abv_title);
                teamAbvView.setVisibility(View.VISIBLE);
            }
            teamName = StatsEntry.FREE_AGENT;
        }
        totalLayout = rootView.findViewById(R.id.team_stats_totals);


        teamNameView = rootView.findViewById(R.id.teamName);
        teamNameView = rootView.findViewById(R.id.teamName);
        teamRecordView = rootView.findViewById(R.id.teamRecord);
        rv = rootView.findViewById(R.id.rv_players);

        rootView.findViewById(R.id.player_name_title).setOnClickListener(this);
        rootView.findViewById(R.id.hr_title).setOnClickListener(this);
        rootView.findViewById(R.id.ab_title).setOnClickListener(this);
        rootView.findViewById(R.id.hit_title).setOnClickListener(this);
        rootView.findViewById(R.id.rbi_title).setOnClickListener(this);
        rootView.findViewById(R.id.run_title).setOnClickListener(this);
        rootView.findViewById(R.id.avg_title).setOnClickListener(this);
        rootView.findViewById(R.id.obp_title).setOnClickListener(this);
        rootView.findViewById(R.id.slg_title).setOnClickListener(this);
        rootView.findViewById(R.id.ops_title).setOnClickListener(this);
        rootView.findViewById(R.id.sgl_title).setOnClickListener(this);
        rootView.findViewById(R.id.dbl_title).setOnClickListener(this);
        rootView.findViewById(R.id.tpl_title).setOnClickListener(this);
        rootView.findViewById(R.id.bb_title).setOnClickListener(this);
        rootView.findViewById(R.id.game_title).setOnClickListener(this);

        FloatingActionButton startAdderBtn = rootView.findViewById(R.id.btn_start_adder);
        if (levelAuthorized(UsersActivity.LEVEL_VIEW_WRITE)) {
            startAdderBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addPlayersDialog(teamName, teamFirestoreID);
                }
            });
        } else {
            startAdderBtn.setVisibility(View.INVISIBLE);
        }

        rootView.findViewById(R.id.btn_games).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GameRecapListActivity.class);
                if(mSelectionType != MainPageSelection.TYPE_TEAM) {
                    intent.setAction(StatsEntry.COLUMN_TEAM_ID);
                    intent.putExtra(StatsEntry.COLUMN_FIRESTORE_ID, teamFirestoreID);
                }
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(EXISTING_TEAM_LOADER, null, this);
        return rootView;
    }

    private void addPlayersDialog(String teamName, String teamID) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = AddNewPlayersDialog.newInstance(teamName, teamID);
        newFragment.show(fragmentTransaction, "");
    }


    private void updateTeamRV() {
        if (mAdapter == null) {
            setNewAdapter();
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void updatePlayerGender(int gender, String playerFirestoreID) {
        for(Player player : mPlayers) {
            if(player.getFirestoreID().equals(playerFirestoreID)) {
                player.setGender(gender);
                updateTeamRV();
                return;
            }
        }
    }

    public void updatePlayerName(String name, String playerFirestoreID) {
        for(Player player : mPlayers) {
            if(player.getFirestoreID().equals(playerFirestoreID)) {
                player.setName(name);
                updateTeamRV();
                return;
            }
        }
    }

    private void setNewAdapter() {
        SharedPreferences settingsPreferences = getActivity()
                .getSharedPreferences(mSelectionID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
        int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);

        rv.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new PlayerStatsAdapter(mPlayers, getActivity(), genderSorter, mSelectionID);
        rv.setAdapter(mAdapter);
    }

    public void changeColorsRV(boolean genderSettingsOn) {
        boolean update = true;
        if (mAdapter != null) {
            update = mAdapter.changeColors(genderSettingsOn);
        }
        if (update) {
            updateTeamRV();
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection;
        String[] selectionArgs;

        if (waivers) {
            selection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
            selectionArgs = new String[]{StatsEntry.FREE_AGENT, mSelectionID};
        } else {
            selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
            selectionArgs = new String[]{mSelectionID};
        }

        return new CursorLoader(
                getActivity(),
                mCurrentTeamUri,
                null,
                selection,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        int wins = 0;
        int losses = 0;
        int ties = 0;
        if (cursor.moveToFirst()) {
            Team team = new Team(cursor);

            teamFirestoreID = team.getFirestoreID();
            teamName = team.getName();
            wins = team.getWins();
            losses = team.getLosses();
            ties = team.getTies();

            String recordText = wins + "-" + losses + "-" + ties;
            teamRecordView.setText(recordText);
        } else if (!waivers) {
            TimeStampUpdater.setLocalTimeStamp(-1, getActivity(), mSelectionID);
            getActivity().finish();
            return;
        }

        int sumG = wins + losses + ties;

        if (waivers) {
            teamName = StatsEntry.FREE_AGENT;
            teamFirestoreID = StatsEntry.FREE_AGENT;
            teamNameView.setText(R.string.waivers);
        } else {
            teamNameView.setText(teamName);
        }

        String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{teamFirestoreID, mSelectionID};
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

        cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS,
                null, selection, selectionArgs, sortOrder);

        if (mPlayers == null) {
            mPlayers = new ArrayList<>();
        } else {
            mPlayers.clear();
        }

        int sumHr = 0;
        int sumTpl = 0;
        int sumDbl = 0;
        int sumSgl = 0;
        int sumBb = 0;
        int sumOut = 0;
        int sumRbi = 0;
        int sumRun = 0;
        int sumSf = 0;

        while (cursor.moveToNext()) {
            Player player = new Player(cursor, false);

            sumHr += player.getHrs();
            sumTpl += player.getTriples();
            sumDbl += player.getDoubles();
            sumSgl += player.getSingles();
            sumBb += player.getWalks();
            sumOut += player.getOuts();
            sumRbi += player.getRbis();
            sumRun += player.getRuns();
            sumSf += player.getSacFlies();

            mPlayers.add(player);
        }
        cursor.close();
        if (mPlayers.size() > 0 && !waivers) {
            setRecyclerViewVisible();
        } else if (!waivers) {
            setEmptyViewVisible();
        }

        if (statSort != -1) {
            sortStats(statSort);
        } else {
            updateTeamRV();
        }

        if(teamFirestoreID.equals(StatsEntry.FREE_AGENT)){
            totalLayout.setVisibility(View.GONE);
            return;
        }

        TextView totalNameView = totalLayout.findViewById(R.id.player_name_title);
        TextView abView = totalLayout.findViewById(R.id.ab_title);
        TextView hitView = totalLayout.findViewById(R.id.hit_title);
        TextView hrView = totalLayout.findViewById(R.id.hr_title);
        TextView rbiView = totalLayout.findViewById(R.id.rbi_title);
        TextView runView = totalLayout.findViewById(R.id.run_title);
        TextView avgView = totalLayout.findViewById(R.id.avg_title);
        TextView obpView = totalLayout.findViewById(R.id.obp_title);
        TextView slgView = totalLayout.findViewById(R.id.slg_title);
        TextView opsView = totalLayout.findViewById(R.id.ops_title);
        TextView sglView = totalLayout.findViewById(R.id.sgl_title);
        TextView dblView = totalLayout.findViewById(R.id.dbl_title);
        TextView tplView = totalLayout.findViewById(R.id.tpl_title);
        TextView bbView = totalLayout.findViewById(R.id.bb_title);
        TextView gameView = totalLayout.findViewById(R.id.game_title);

        totalNameView.setText(R.string.total);
        hrView.setText(String.valueOf(sumHr));
        tplView.setText(String.valueOf(sumTpl));
        dblView.setText(String.valueOf(sumDbl));
        sglView.setText(String.valueOf(sumSgl));
        bbView.setText(String.valueOf(sumBb));
        rbiView.setText(String.valueOf(sumRbi));
        runView.setText(String.valueOf(sumRun));
        gameView.setText(String.valueOf(sumG));

        int sumHits = sumSgl + sumDbl + sumTpl + sumHr;
        int sumAB = sumOut + sumHits;
        hitView.setText(String.valueOf(sumHits));
        abView.setText(String.valueOf(sumAB));

        double sumAvg = convertAVG(sumHits, sumAB);
        double sumOBP = convertOBP(sumHits, sumAB, sumBb, sumSf);
        double sumSLG = convertSLG(sumAB, sumSgl, sumDbl, sumTpl, sumHr);
        double sumOPS = sumOBP + sumSLG;

        NumberFormat formatter = new DecimalFormat("#.000");
        String avgString;
        String obpString;
        String slgString;
        String opsString;

        if (sumAB <= 0) {
            avgString = "---";
            slgString = "---";
            if (sumBb + sumSf <= 0) {
                obpString = "---";
                opsString = "---";
            } else {
                obpString = String.valueOf(formatter.format(sumOBP));
                opsString = String.valueOf(formatter.format(sumOPS));
            }
        } else {
            avgString = String.valueOf(formatter.format(sumAvg));
            obpString = String.valueOf(formatter.format(sumOBP));
            slgString = String.valueOf(formatter.format(sumSLG));
            opsString = String.valueOf(formatter.format(sumOPS));
        }

        avgView.setText(avgString);
        obpView.setText(obpString);
        slgView.setText(slgString);
        opsView.setText(opsString);
    }

    public void reloadStats() {
        getLoaderManager().restartLoader(EXISTING_TEAM_LOADER, null,this);
    }

    private double convertAVG(int hits, int atbats) {
        if (atbats == 0) {
            return .000;
        }
        return ((double) hits) / atbats;
    }

    private double convertOBP(int hits, int atbats, int walks, int sacFlies) {
        if (atbats + walks + sacFlies == 0) {
            return .000;
        }
        return ((double) (hits + walks))
                / (atbats + walks + sacFlies);
    }

    private double convertSLG(int atbats, int singles, int doubles, int triples, int hrs) {
        if (atbats == 0) {
            return .000;
        }
        return (singles + doubles * 2 + triples * 3 + hrs * 4)
                / ((double) atbats);
    }
    private void setEmptyViewVisible() {
        getView().findViewById(R.id.team_scroll_view).setVisibility(View.GONE);
        getView().findViewById(R.id.empty_team_text).setVisibility(View.VISIBLE);
    }

    private void setRecyclerViewVisible() {
        getView().findViewById(R.id.empty_team_text).setVisibility(View.GONE);
        getView().findViewById(R.id.team_scroll_view).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void addPlayers(List<Player> newPlayers) {
        if (mPlayers.isEmpty()) {
            setRecyclerViewVisible();
        }
        mPlayers.addAll(newPlayers);
        updateTeamRV();
    }

    private boolean levelAuthorized(int level) {
        return mLevel >= level;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (levelAuthorized(UsersActivity.LEVEL_VIEW_WRITE)) {
            inflater.inflate(R.menu.menu_team, menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!levelAuthorized(UsersActivity.LEVEL_VIEW_WRITE)) {
            return;
        }
        if (levelAuthorized(UsersActivity.LEVEL_ADMIN)) {
            menu.setGroupVisible(R.id.group_high_level_team_options, true);
        }
        if (waivers) {
            menu.findItem(R.id.action_change_name).setVisible(false);
            menu.findItem(R.id.action_delete_team).setVisible(false);
            menu.findItem(R.id.action_edit_lineup).setVisible(false);
        } else if (mSelectionType == MainPageSelection.TYPE_TEAM) {
            menu.findItem(R.id.action_edit_lineup).setVisible(false);
            menu.findItem(R.id.action_delete_team).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                if(getActivity() instanceof TeamManagerActivity) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    super.onOptionsItemSelected(item);
                }
                return true;

            case R.id.action_change_name:
                editNameDialog();
                return true;

            case R.id.action_edit_lineup:
                Intent setLineupIntent = new Intent(getActivity(), SetLineupActivity.class);
                Bundle b = new Bundle();
                b.putString("team_name", teamName);
                b.putString("team_id", teamFirestoreID);
                b.putBoolean("ingame", false);
                setLineupIntent.putExtras(b);
                startActivity(setLineupIntent);
                return true;

            case R.id.action_remove_players:
                if (mPlayers.isEmpty()) {
                    Toast.makeText(getActivity(), teamName + " has no players."
                            , Toast.LENGTH_LONG).show();
                } else {
                    showRemoveAllPlayersDialog();
                }
                return true;

            case R.id.action_delete_team:
                showDeleteConfirmationDialog();
                return true;

            case R.id.change_user_settings:
                Intent settingsIntent = new Intent(getActivity(), UsersActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.change_game_settings:
                SharedPreferences settingsPreferences = getActivity()
                        .getSharedPreferences(mSelectionID + StatsEntry.SETTINGS, Context.MODE_PRIVATE);
                int innings = settingsPreferences.getInt(StatsEntry.INNINGS, 7);
                int genderSorter = settingsPreferences.getInt(StatsEntry.COLUMN_GENDER, 0);
                boolean gameHelp = settingsPreferences.getBoolean(StatsEntry.HELP, true);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DialogFragment newFragment = GameSettingsDialog.newInstance(innings, genderSorter, mSelectionID, 0, gameHelp);
                newFragment.show(fragmentTransaction, "");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = DeleteConfirmationDialog.newInstance(teamName);
        newFragment.show(fragmentTransaction, "");
    }

    public void showDeleteVsWaiversDialog() {
        if (mPlayers.isEmpty()) {
            deleteTeam();
        } else {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            DialogFragment newFragment = new DeleteVsWaiversDialog();
            newFragment.show(fragmentTransaction, "");
        }
    }


    private void showRemoveAllPlayersDialog() {
        boolean isLeague = mSelectionType == MainPageSelection.TYPE_LEAGUE;

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = RemoveAllPlayersDialog.newInstance(teamName, isLeague, waivers);
        newFragment.show(fragmentTransaction, "");
    }

    private void editNameDialog() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = EditNameDialog.newInstance(teamName);
        newFragment.show(fragmentTransaction, "");
    }

    public void deleteTeam() {
        if (mCurrentTeamUri != null) {
            String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
            String[] selectionArgs = new String[]{teamFirestoreID, mSelectionID};
            int rowsDeleted = getActivity().getContentResolver().delete(mCurrentTeamUri, selection, selectionArgs);

            if (rowsDeleted > 0) {
                Toast.makeText(getActivity(), teamName + " " + getString(R.string.editor_delete_player_successful),
                        Toast.LENGTH_SHORT).show();
                long updateTime = System.currentTimeMillis();

                Intent intent = new Intent(getActivity(), FirestoreUpdateService.class);
                intent.putExtra(FirestoreUpdateService.STATKEEPER_ID, mSelectionID);
                intent.putExtra(TimeStampUpdater.UPDATE_TIME, updateTime);

                intent.putExtra(StatsEntry.COLUMN_FIRESTORE_ID, teamFirestoreID);
                intent.putExtra(StatsEntry.TYPE, teamFirestoreID);
                intent.putExtra(StatsEntry.COLUMN_NAME, teamFirestoreID);
                intent.putExtra(StatsEntry.COLUMN_GENDER, teamFirestoreID);
                intent.putExtra(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, teamFirestoreID);

                intent.setAction(FirestoreUpdateService.INTENT_DELETE_PLAYER);
                getActivity().startService(intent);
            } else {
                return;
            }
        }
        getActivity().finish();
    }

    public List<String> deletePlayers() {
        List<String> firestoreIDsToDelete = new ArrayList<>();
        ArrayList<Player> firestorePlayersToDelete = new ArrayList<>();
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";

        int amountDeleted = 0;
        for (int i = 0; i < mPlayers.size(); i++) {
            Player player = mPlayers.get(i);
            String firestoreID = player.getFirestoreID();
            String name = player.getName();
            int gender = player.getGender();
            String[] selectionArgs = new String[]{firestoreID, mSelectionID};
            int deleted = getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_PLAYERS, selection, selectionArgs);
            if(deleted > 0) {
                mPlayers.remove(i);
                i--;
                amountDeleted++;
                firestoreIDsToDelete.add(firestoreID);
                firestorePlayersToDelete.add(new Player(firestoreID, name, teamFirestoreID, gender));
            }
        }
        long updateTime = System.currentTimeMillis();

        Intent intent = new Intent(getActivity(), FirestoreUpdateService.class);
        intent.putExtra(FirestoreUpdateService.STATKEEPER_ID, mSelectionID);
        intent.putExtra(TimeStampUpdater.UPDATE_TIME, updateTime);

        intent.putParcelableArrayListExtra(FirestoreUpdateService.KEY_DELETE_PLAYERS, firestorePlayersToDelete);
        intent.setAction(FirestoreUpdateService.INTENT_DELETE_PLAYERS);
        getActivity().startService(intent);
        TimeStampUpdater.updateTimeStamps(getActivity(), mSelectionID, updateTime);

        if (amountDeleted > 0) {
            updateTeamRV();
        }
        if(mPlayers.size() == 0) {
            setEmptyViewVisible();
        }
        return firestoreIDsToDelete;
    }

    public void clearPlayers() {
        mPlayers.clear();
        updateTeamRV();
        setEmptyViewVisible();
    }

    public boolean updateTeamName(String newName) {

        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mSelectionID};
        String[] projection = new String[]{StatsEntry.COLUMN_FIRESTORE_ID};
        Cursor cursor = getActivity().getContentResolver().query(mCurrentTeamUri,
                projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        String firestoreID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_FIRESTORE_ID);
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_NAME, newName);
        contentValues.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
        contentValues.put(StatsEntry.COLUMN_LEAGUE_ID, mSelectionID);

        String qSelection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] qSelectionArgs = new String[]{mSelectionID};
        int rowsUpdated = getActivity().getContentResolver().update(mCurrentTeamUri,
                contentValues, qSelection, qSelectionArgs);
        if (rowsUpdated > 0) {
            teamName = newName;
            updatePlayersTeam(teamName);
            TimeStampUpdater.setUpdate(firestoreID, 0, mSelectionID, getActivity(), System.currentTimeMillis());
            return true;
        }
        return false;
    }

    public void updatePlayersTeam(String team) {
        ContentValues contentValues = new ContentValues();
        long updateTime = System.currentTimeMillis();
        for (int i = 0; i < mPlayers.size() - 1; i++) {
            Player player = mPlayers.get(i);
            long playerID = player.getPlayerId();
            String firestoreID = player.getFirestoreID();

            contentValues.put(StatsEntry.COLUMN_LEAGUE_ID, mSelectionID);
            contentValues.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
            contentValues.put(StatsEntry.COLUMN_TEAM, team);

            if(team.equals(StatsEntry.FREE_AGENT)) {
                contentValues.put(StatsEntry.COLUMN_TEAM_FIRESTORE_ID, StatsEntry.FREE_AGENT);
            }

            Uri playerURI = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_PLAYERS, playerID);
            String qSelection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
            String[] qSelectionArgs = new String[]{mSelectionID};
            getActivity().getContentResolver().update(playerURI, contentValues, qSelection, qSelectionArgs);

            TimeStampUpdater.setUpdate(firestoreID, 1, mSelectionID, getActivity(), updateTime);
        }
        updateTeamRV();
    }

    public void removePlayerFromTeam(String playerFirestoreID) {
        mPlayers.remove(new Player(-1, playerFirestoreID));
        updateTeamRV();
    }

    @Override
    public void onClick(View v) {
        statSort = v.getId();
        sortStats(statSort);
    }

    private void sortStats(int statSorter) {
        if (colorView != null) {
            colorView.setTextColor(Color.WHITE);
        }
        colorView = getView().findViewById(statSorter);
        colorView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));

        switch (statSorter) {

            case R.id.player_name_title:
                Collections.sort(mPlayers, Player.nameComparator());
                break;

            case R.id.team_abv_title:
                Collections.sort(mPlayers, Player.teamComparator());
                break;

            case R.id.ab_title:
                Collections.sort(mPlayers, Player.atbatComparator());
                break;

            case R.id.hit_title:
                Collections.sort(mPlayers, Player.hitComparator());
                break;

            case R.id.hr_title:
                Collections.sort(mPlayers, Player.hrComparator());

                break;
            case R.id.run_title:
                Collections.sort(mPlayers, Player.runComparator());

                break;
            case R.id.rbi_title:
                Collections.sort(mPlayers, Player.rbiComparator());

                break;
            case R.id.avg_title:
                Collections.sort(mPlayers, Player.avgComparator());
                break;

            case R.id.obp_title:
                Collections.sort(mPlayers, Player.obpComparator());
                break;

            case R.id.slg_title:
                Collections.sort(mPlayers, Player.slgComparator());
                break;

            case R.id.ops_title:
                Collections.sort(mPlayers, Player.opsComparator());
                break;

            case R.id.sgl_title:
                Collections.sort(mPlayers, Player.singleComparator());
                break;

            case R.id.dbl_title:
                Collections.sort(mPlayers, Player.doubleComparator());
                break;

            case R.id.tpl_title:
                Collections.sort(mPlayers, Player.tripleComparator());
                break;

            case R.id.game_title:
                Collections.sort(mPlayers, Player.gamesplayedComparator());
                break;

            case R.id.bb_title:
                Collections.sort(mPlayers, Player.walkComparator());
                break;

            default:
                Toast.makeText(getActivity(), "SOMETHING WRONG WITH onClick", Toast.LENGTH_LONG).show();
        }
        updateTeamRV();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_STAT_SORT, statSort);
    }
}
