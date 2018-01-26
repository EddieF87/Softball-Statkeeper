package com.example.android.scorekeepdraft1.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.SetLineupActivity;
import com.example.android.scorekeepdraft1.activities.UserSettingsActivity;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.FirestoreAdapter;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.PlayerStatsAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.dialogs.CreateTeamDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.GameSettingsDialogFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TeamFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private Uri mCurrentTeamUri;
    private static final int EXISTING_TEAM_LOADER = 3;

    private TextView teamNameView;
    private TextView teamRecordView;
    private RecyclerView rv;
    private PlayerStatsAdapter mAdapter;

    private List<Player> mPlayers;
    private String teamSelected;
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

    @Override
    public void onResume() {
        super.onResume();
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
            teamSelected = mSelectionName;
        } else {
            if (mCurrentTeamUri.equals(StatsEntry.CONTENT_URI_TEAMS)) {
                waivers = true;
                View titleLayout = rootView.findViewById(R.id.team_stats_titles);
                View teamAbvView = titleLayout.findViewById(R.id.team_abv_title);
                teamAbvView.setVisibility(View.VISIBLE);
            }
            teamSelected = "Free Agent";
        }



        teamNameView = rootView.findViewById(R.id.teamName);
        teamNameView = rootView.findViewById(R.id.teamName);
        teamRecordView = rootView.findViewById(R.id.teamRecord);
        rv = rootView.findViewById(R.id.rv_players);

        rootView.findViewById(R.id.name_title).setOnClickListener(this);
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

        View addPlayerView = rootView.findViewById(R.id.item_player_adder);
        FloatingActionButton startAdderBtn = addPlayerView.findViewById(R.id.btn_start_adder);
        if (levelAuthorized(3)) {
            startAdderBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createTeamFragment(teamSelected);
                }
            });
        } else {
            addPlayerView.setVisibility(View.INVISIBLE);
        }
        getLoaderManager().initLoader(EXISTING_TEAM_LOADER, null, this);

        return rootView;
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(EXISTING_TEAM_LOADER, null, this);
    }

    private void createTeamFragment(String team) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = CreateTeamDialogFragment.newInstance(team);
        newFragment.show(fragmentTransaction, "");
    }


    public void updateTeamRV() {
        if (mAdapter == null) {
            SharedPreferences settingsPreferences = getActivity()
                    .getSharedPreferences(mSelectionID + "settings", Context.MODE_PRIVATE);
            int genderSorter = settingsPreferences.getInt("genderSort", 0);

            rv.setLayoutManager(new LinearLayoutManager(
                    getActivity(), LinearLayoutManager.VERTICAL, false));
            mAdapter = new PlayerStatsAdapter(mPlayers, getActivity(), genderSorter);
            rv.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
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

        String selection = null;
        String[] selectionArgs = null;

        if (waivers) {
            selection = StatsEntry.COLUMN_NAME + "=?";
            selectionArgs = new String[]{"Free Agent"};
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
            int nameColumnIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            int winColumnIndex = cursor.getColumnIndex(StatsEntry.COLUMN_WINS);
            int lossColumnIndex = cursor.getColumnIndex(StatsEntry.COLUMN_LOSSES);
            int tieColumnIndex = cursor.getColumnIndex(StatsEntry.COLUMN_TIES);

            teamSelected = cursor.getString(nameColumnIndex);
            wins = cursor.getInt(winColumnIndex);
            losses = cursor.getInt(lossColumnIndex);
            ties = cursor.getInt(tieColumnIndex);

            String recordText = wins + "-" + losses + "-" + ties;
            teamRecordView.setText(recordText);
        } else if (!waivers) {
            return;
        }

        int sumG = wins + losses + ties;

        if (waivers) {
            teamSelected = "Free Agent";
            teamNameView.setText(R.string.waivers);
        } else {
            teamNameView.setText(teamSelected);
        }

        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{teamSelected};
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";

        cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS,
                null, selection, selectionArgs, sortOrder);

        if(mPlayers == null) {
            mPlayers = new ArrayList<>();
        } else {
            mPlayers.clear();
        }

        int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
        int hrIndex = cursor.getColumnIndex(StatsEntry.COLUMN_HR);
        int tripleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_3B);
        int doubleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_2B);
        int singleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_1B);
        int bbIndex = cursor.getColumnIndex(StatsEntry.COLUMN_BB);
        int outIndex = cursor.getColumnIndex(StatsEntry.COLUMN_OUT);
        int rbiIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RBI);
        int runIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RUN);
        int sfIndex = cursor.getColumnIndex(StatsEntry.COLUMN_SF);
        int gameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_G);
        int idIndex = cursor.getColumnIndex(StatsEntry._ID);
        int genderIndex = cursor.getColumnIndex(StatsEntry.COLUMN_GENDER);
        int firestoreIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);

        int sumHr = 0;
        int sumTpl = 0;
        int sumDbl = 0;
        int sumSgl = 0;
        int sumBb = 0;
        int sumOut = 0;
        int sumRbi = 0;
        int sumRun = 0;
        int sumSf = 0;

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {

            String firestoreID = cursor.getString(firestoreIDIndex);
            String player = cursor.getString(nameIndex);
            int gender = cursor.getInt(genderIndex);
            int hr = cursor.getInt(hrIndex);
            sumHr += hr;
            int tpl = cursor.getInt(tripleIndex);
            sumTpl += tpl;
            int dbl = cursor.getInt(doubleIndex);
            sumDbl += dbl;
            int sgl = cursor.getInt(singleIndex);
            sumSgl += sgl;
            int bb = cursor.getInt(bbIndex);
            sumBb += bb;
            int out = cursor.getInt(outIndex);
            sumOut += out;
            int rbi = cursor.getInt(rbiIndex);
            sumRbi += rbi;
            int run = cursor.getInt(runIndex);
            sumRun += run;
            int sf = cursor.getInt(sfIndex);
            sumSf += sf;
            int g = cursor.getInt(gameIndex);

            int playerId = cursor.getInt(idIndex);
            mPlayers.add(new Player(player, teamSelected, gender, sgl, dbl, tpl, hr, bb,
                    run, rbi, out, sf, g, playerId, firestoreID));
        }
        if (mPlayers.size() >0 && !waivers) {
            mPlayers.add(new Player("Total", teamSelected, 2, sumSgl, sumDbl, sumTpl, sumHr, sumBb,
                    sumRun, sumRbi, sumOut, sumSf, sumG, -1, ""));
            setRecyclerViewVisible();
        } else if (!waivers) {
            setEmptyViewVisible();
        }

        if (statSort != -1) {
            sortStats(statSort);
        } else {
            updateTeamRV();
        }
    }

    public void setEmptyViewVisible () {
        getView().findViewById(R.id.team_scroll_view).setVisibility(View.GONE);
        getView().findViewById(R.id.empty_team_text).setVisibility(View.VISIBLE);
    }

    public void setRecyclerViewVisible () {
        getView().findViewById(R.id.empty_team_text).setVisibility(View.GONE);
        getView().findViewById(R.id.team_scroll_view).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void addPlayers(List<Player> newPlayers) {
        if (mPlayers.isEmpty()) {
            mPlayers.add(new Player("Total", teamSelected, 2));
            setRecyclerViewVisible();
        }
        int position = mPlayers.size() - 1;
        mPlayers.addAll(position, newPlayers);
        updateTeamRV();
    }

    private boolean levelAuthorized(int level) {
        return mLevel >= level;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (levelAuthorized(3)) {
            inflater.inflate(R.menu.menu_team, menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(levelAuthorized(4)) {
            menu.setGroupVisible(R.id.group_high_level_team_options, true);
        }
        if (waivers) {
            menu.findItem(R.id.action_change_name).setVisible(false);
            menu.findItem(R.id.action_edit_photo).setVisible(false);
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
            case R.id.action_change_name:
                editNameDialog();
                return true;
            case R.id.action_edit_photo:
                return true;
            case R.id.action_edit_lineup:
                Intent setLineupIntent = new Intent(getActivity(), SetLineupActivity.class);
                Bundle b = new Bundle();
                b.putString("team", teamSelected);
                b.putBoolean("ingame", false);
                setLineupIntent.putExtras(b);
                startActivity(setLineupIntent);
                return true;
            case R.id.action_remove_players:
                showRemoveAllPlayersDialog();
                return true;
            case R.id.action_delete_team:
                showDeleteConfirmationDialog();
                return true;
            case R.id.change_user_settings:
                Intent settingsIntent = new Intent(getActivity(), UserSettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.change_game_settings:
                SharedPreferences settingsPreferences = getActivity()
                        .getSharedPreferences(mSelectionID + "settings", Context.MODE_PRIVATE);
                int innings = settingsPreferences.getInt("innings", 7);
                int genderSorter = settingsPreferences.getInt("genderSort", 0);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DialogFragment newFragment = GameSettingsDialogFragment.newInstance(innings, genderSorter, mSelectionID);
                newFragment.show(fragmentTransaction, "");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete " + teamSelected + "?");
        builder.setMessage(R.string.delete_team_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                AlertDialog.Builder choice = new AlertDialog.Builder(getActivity());
                choice.setMessage(R.string.delete_or_freeagency_msg);
                choice.setPositiveButton(R.string.waivers, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updatePlayersTeam("Free Agent");
                        deleteTeam();
                    }
                });
                choice.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deletePlayers();
                        deleteTeam();
                    }
                });
                AlertDialog alertDialog2 = choice.create();
                alertDialog2.show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showRemoveAllPlayersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (mSelectionType == MainPageSelection.TYPE_LEAGUE) {
            if (waivers) {
                builder.setMessage(R.string.remove_all_free_agents);
            } else {
                builder.setMessage(R.string.send_all_to_waivers);
            }
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (waivers) {
                        deletePlayers();
                    } else {
                        updatePlayersTeam("Free Agent");
                        clearPlayers();
                        setEmptyViewVisible();
                    }
                }
            });
        } else {
            builder.setTitle("Delete all players?")
                    .setMessage("Players and their stats will be permanently deleted.")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deletePlayers();
                            setEmptyViewVisible();
                        }
                    });
        }
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void editNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String titleString = getContext().getResources().getString(R.string.edit_object_name);
        String title = String.format(titleString, teamSelected);
        builder.setTitle(title);
        final LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_edit_name, null))
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog dialog1 = (Dialog) dialog;
                        EditText editText = dialog1.findViewById(R.id.username);
                        String enteredTeam = editText.getText().toString();
                        updateTeamName(enteredTeam);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void deleteTeam() {
        if (mCurrentTeamUri != null) {
            int rowsDeleted = getActivity().getContentResolver().delete(mCurrentTeamUri, null, null);

            if (rowsDeleted == 1) {
                Toast.makeText(getActivity(), teamSelected + " " + getString(R.string.editor_delete_player_successful),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.editor_delete_team_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
        getActivity().finish();
    }

    private void deletePlayers() {
        String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        int total = 1;
        if (waivers) {
            total--;
        }

        for (int i = 0; i < mPlayers.size() - total; i++) {
            Player player = mPlayers.get(i);
            String firestoreID = player.getFirestoreID();
            String[] selectionArgs = new String[]{firestoreID};
            getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_PLAYERS, selection, selectionArgs);
        }
        clearPlayers();
    }

    private void clearPlayers() {
        mPlayers.clear();
        updateTeamRV();
    }

    private void updateTeamName(String team) {

        String[] projection = new String[]{StatsEntry.COLUMN_FIRESTORE_ID};
        Cursor cursor = getActivity().getContentResolver().query(mCurrentTeamUri,
                projection, null, null, null);
        cursor.moveToFirst();
        int firestoreIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
        String firestoreID = cursor.getString(firestoreIndex);
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_NAME, team);
        contentValues.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);

        int rowsUpdated = getActivity().getContentResolver().update(mCurrentTeamUri,
                contentValues, null, null);
        if (rowsUpdated > 0) {
            updatePlayersTeam(team);
            teamSelected = team;
        }
    }

    public void updatePlayersTeam(String team) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_TEAM, team);
        for (int i = 0; i < mPlayers.size() - 1; i++) {
            Player player = mPlayers.get(i);
            long playerID = player.getPlayerId();
            String firestoreID = player.getFirestoreID();

            contentValues.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);

            Uri playerURI = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_PLAYERS, playerID);
            getActivity().getContentResolver().update(playerURI, contentValues, null, null);
        }
        updateTeamRV();
    }

    @Override
    public void onClick(View v) {
        statSort = v.getId();
        sortStats(statSort);
    }

    private void sortStats (int statSorter) {
        if (colorView != null) {
            colorView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.stat_title));
        }
        colorView = getView().findViewById(statSorter);
        colorView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryLight));

        Player total = mPlayers.get(mPlayers.size() - 1);
        mPlayers.remove(total);

        switch (statSorter) {

            case R.id.name_title:
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

        mPlayers.add(total);
        updateTeamRV();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_STAT_SORT, statSort);
    }
}
