package com.example.android.scorekeepdraft1.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.SetLineupActivity;
import com.example.android.scorekeepdraft1.activities.TeamGameActivity;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.FirestoreAdapter;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.PlayerStatsAdapter;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TeamFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentTeamUri;
    private static final int EXISTING_TEAM_LOADER = 3;

    private TextView teamNameView;
    private TextView teamRecordView;
    private EditText addPlayerText;
    private FloatingActionButton addPlayerButton;
    private Button addPlayerBtn;

    private RecyclerView rv;

    private List<Player> players;
    private String teamSelected;
    private boolean waivers;

    private int selectionType;
    private String selectionID;
    private String selectionName;

    private static final String KEY_TEAM_URI = "teamURI";


    public TeamFragment() {
        // Required empty public constructor
    }

    public static TeamFragment newInstance(String leagueID, int leagueType, String leagueName) {
        Bundle args = new Bundle();
        args.putString(MainPageSelection.KEY_SELECTION_ID, leagueID);
        args.putInt(MainPageSelection.KEY_SELECTION_TYPE, leagueType);
        args.putString(MainPageSelection.KEY_SELECTION_NAME, leagueName);
        TeamFragment fragment = new TeamFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static TeamFragment newInstance(String leagueID, int leagueType, String leagueName, Uri uri) {
        Bundle args = new Bundle();
        args.putString(MainPageSelection.KEY_SELECTION_ID, leagueID);
        args.putInt(MainPageSelection.KEY_SELECTION_TYPE, leagueType);
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
        selectionID = args.getString(MainPageSelection.KEY_SELECTION_ID);
        selectionType = args.getInt(MainPageSelection.KEY_SELECTION_TYPE);
        selectionName = args.getString(MainPageSelection.KEY_SELECTION_NAME);
        if (selectionType == MainPageSelection.TYPE_LEAGUE) {
            String uriString = args.getString(KEY_TEAM_URI);
            mCurrentTeamUri = Uri.parse(uriString);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_team, container, false);

        waivers = false;

//        MyApp myApp = (MyApp) getActivity().getApplicationContext();
//        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
//        if (myApp.getCurrentSelection() == null) {
//            Intent intent = new Intent(getActivity(), MainActivity.class);
//            startActivity(intent);
//            return null;
//        }

//        selectionType = mainPageSelection.getType();
        if (selectionType == MainPageSelection.TYPE_TEAM) {


            //            Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG,
//                    null, null, null, null);
//            Button continueGameButton = findViewById(R.id.btn_continue_game);
//            if (cursor.moveToFirst()) {
//                continueGameButton.setVisibility(View.VISIBLE);
//            } else {
//                continueGameButton.setVisibility(View.INVISIBLE);
//            }
//            cursor.close();
            teamSelected = selectionName;
//            selectionID = mainPageSelection.getId();

        } else {
            mCurrentTeamUri = mCurrentTeamUri;
//            Intent intent = getActivity().getIntent();
//            mCurrentTeamUri = intent.getData();
            if (mCurrentTeamUri == null) {
                waivers = true;
            }
            teamSelected = "Free Agent";
        }

        teamNameView = rootView.findViewById(R.id.teamName);
        teamRecordView = rootView.findViewById(R.id.teamRecord);
        rv = rootView.findViewById(R.id.rv_players);

        View addPlayerView = rootView.findViewById(R.id.item_player_adder);

        addPlayerText = addPlayerView.findViewById(R.id.add_player_text);
        addPlayerBtn = addPlayerView.findViewById(R.id.add_player_submit);
        addPlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPlayer();
            }
        });
        addPlayerButton = addPlayerView.findViewById(R.id.btn_start_adder);
        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPlayerButton.setVisibility(View.GONE);
                addPlayerText.setVisibility(View.VISIBLE);
                addPlayerBtn.setVisibility(View.VISIBLE);
            }
        });
        getLoaderManager();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(EXISTING_TEAM_LOADER, null, this);
    }

    public void addPlayer() {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        String playerName = addPlayerText.getText().toString();

        ContentValues values = new ContentValues();
        values.put(StatsEntry.COLUMN_NAME, playerName);
        values.put(StatsEntry.COLUMN_TEAM, teamSelected);
        getActivity().getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
        addPlayerText.setText("");
        getLoaderManager().restartLoader(EXISTING_TEAM_LOADER, null, this);
    }

    private void initRecyclerView() {
        rv.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));
        PlayerStatsAdapter mAdapter = new PlayerStatsAdapter(players, getActivity());
        rv.setAdapter(mAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = null;
        String[] selectionArgs = null;
        Uri uri;

        if (selectionType == MainPageSelection.TYPE_TEAM) {
            uri = StatsEntry.CONTENT_URI_TEAMS;
        } else if (waivers) {
            selection = StatsEntry.COLUMN_NAME + "=?";
            selectionArgs = new String[]{"Free Agent"};
            uri = StatsEntry.CONTENT_URI_TEAMS;
        } else {
            uri = mCurrentTeamUri;
        }

        return new CursorLoader(
                getActivity(),
                uri,
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

        players = new ArrayList<>();

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
            players.add(new Player(player, teamSelected, sgl, dbl, tpl, hr, bb,
                    run, rbi, out, sf, g, playerId, firestoreID));
        }
        players.add(new Player("Total", teamSelected, sumSgl, sumDbl, sumTpl, sumHr, sumBb,
                sumRun, sumRbi, sumOut, sumSf, sumG, 0, ""));

        initRecyclerView();

        if (selectionType == MainPageSelection.TYPE_TEAM) {
            Button newGameButton = getView().findViewById(R.id.btn_start_game);
            Button continueGameButton = getView().findViewById(R.id.btn_continue_game);

            newGameButton.setVisibility(View.VISIBLE);
            newGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startNewGame();
                }
            });

            continueGameButton.setVisibility(View.VISIBLE);
            continueGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), TeamGameActivity.class);
                    startActivity(intent);

                }
            });
            cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_GAMELOG,
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                continueGameButton.setVisibility(View.VISIBLE);
            } else {
                continueGameButton.setVisibility(View.INVISIBLE);
            }
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    public void startNewGame() {
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_TEMP, null, null);
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_GAMELOG, null, null);
        SharedPreferences savedGamePreferences = getActivity().getSharedPreferences(selectionID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedGamePreferences.edit();
        editor.clear();
        editor.commit();

        Intent intent = new Intent(getActivity(), SetLineupActivity.class);
        Bundle b = new Bundle();
        b.putString("team", teamSelected);
        intent.putExtras(b);
        startActivity(intent);
    }

//todo


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_team, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (waivers) {
            menu.findItem(R.id.action_change_name).setVisible(false);
//            menu.findItem(R.id.action_edit_photo).setVisible(false);
            menu.findItem(R.id.action_delete_team).setVisible(false);
            menu.findItem(R.id.action_edit_lineup).setVisible(false);
        } else if (selectionType == MainPageSelection.TYPE_TEAM) {
            menu.findItem(R.id.action_edit_lineup).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_name:
                editNameDialog();
                return true;
            case R.id.action_edit_photo:
                FirestoreAdapter statsTransfer = new FirestoreAdapter(getActivity());
                statsTransfer.syncStats();
                return true;
            case R.id.action_edit_lineup:
                Intent intent = new Intent(getActivity(), SetLineupActivity.class);
                Bundle b = new Bundle();
                b.putString("team", teamSelected);
                intent.putExtras(b);
                startActivity(intent);
                return true;
            case R.id.action_remove_players:
                showRemoveAllPlayersDialog();
                return true;
            case R.id.action_delete_team:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_add_players:
                View itemPlayerAdder = getView().findViewById(R.id.item_player_adder);
                itemPlayerAdder.setVisibility(View.VISIBLE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //todo
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    //todo
    private void showRemoveAllPlayersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                }
            }
        });
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

    //todo
    private void editNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.edit_team_name);
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

    private void deleteTeam() {
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
        //todo
        getActivity().finish();
    }

    private void deletePlayers() {
        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{teamSelected};
        getActivity().getContentResolver().delete(StatsEntry.CONTENT_URI_PLAYERS, selection, selectionArgs);
        getLoaderManager().restartLoader(EXISTING_TEAM_LOADER, null, this);
    }

    private void updateTeamName(String team) {

        String selection = StatsEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = new String[]{team};
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
        }
        teamSelected = team;
        getLoaderManager().restartLoader(EXISTING_TEAM_LOADER, null, this);
    }

    public void updatePlayersTeam(String team) {
        String selection = StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{teamSelected};
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_TEAM, team);
        getActivity().getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS, contentValues, selection, selectionArgs);
        getLoaderManager().restartLoader(EXISTING_TEAM_LOADER, null, this);
    }
}
