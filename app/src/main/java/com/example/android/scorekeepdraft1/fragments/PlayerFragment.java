package com.example.android.scorekeepdraft1.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.LeagueManagerActivity;
import com.example.android.scorekeepdraft1.activities.PlayerPagerActivity;
import com.example.android.scorekeepdraft1.activities.TeamPagerActivity;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.dialogs.CreateTeamDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.DeleteConfirmationDialogFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private String playerString;
    private NumberFormat formatter = new DecimalFormat("#.000");
    private static final int EXISTING_PLAYER_LOADER = 0;
    private Uri mCurrentPlayerUri;
    private int mLevel;
    private String teamString;
    private String playerName;
    private String firestoreID;
    private static final String KEY_PLAYER_URI = "playerURI";
    private int selectionType;
    private TextView runsText;
    private TextView rbiText;
    private TextView resultText;
    private int runs;
    private int rbi;
    private int resultCount;


    public PlayerFragment() {
        // Required empty public constructor
    }


    public static PlayerFragment newInstance(int leagueType, int level , Uri uri) {
        Bundle args = new Bundle();
        args.putInt(MainPageSelection.KEY_SELECTION_TYPE, leagueType);
        args.putInt(MainPageSelection.KEY_SELECTION_LEVEL, level);
        args.putString(KEY_PLAYER_URI, uri.toString());
        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static PlayerFragment newInstance(int leagueType, String playerName) {
        Bundle args = new Bundle();
        args.putInt(MainPageSelection.KEY_SELECTION_TYPE, leagueType);
        args.putInt(MainPageSelection.KEY_SELECTION_LEVEL, 5);
        args.putString(MainPageSelection.KEY_SELECTION_NAME, playerName);
        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        selectionType = args.getInt(MainPageSelection.KEY_SELECTION_TYPE);
        mLevel = args.getInt(MainPageSelection.KEY_SELECTION_LEVEL);
        if (selectionType == MainPageSelection.TYPE_PLAYER) {
            playerString = args.getString(MainPageSelection.KEY_SELECTION_NAME);
            mCurrentPlayerUri = StatsEntry.CONTENT_URI_PLAYERS;
        } else {
            String uriString = args.getString(KEY_PLAYER_URI);
            mCurrentPlayerUri = Uri.parse(uriString);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(EXISTING_PLAYER_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                mCurrentPlayerUri,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        View rootView = getView();
        TextView nameView = rootView.findViewById(R.id.player_name);

        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(StatsContract.StatsEntry.COLUMN_NAME);
            int teamIndex = cursor.getColumnIndex(StatsEntry.COLUMN_TEAM);
            int genderIndex = cursor.getColumnIndex(StatsEntry.COLUMN_GENDER);
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
            int firestoreIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);

            playerString = cursor.getString(nameIndex);
            teamString = cursor.getString(teamIndex);
            int gender = cursor.getInt(genderIndex);
            int hr = cursor.getInt(hrIndex);
            int tpl = cursor.getInt(tripleIndex);
            int dbl = cursor.getInt(doubleIndex);
            int sgl = cursor.getInt(singleIndex);
            int bb = cursor.getInt(bbIndex);
            int out = cursor.getInt(outIndex);
            int rbi = cursor.getInt(rbiIndex);
            int run = cursor.getInt(runIndex);
            int sf = cursor.getInt(sfIndex);
            int g = cursor.getInt(gameIndex);
            firestoreID = cursor.getString(firestoreIDIndex);

            Player player = new Player(playerString, teamString, gender, sgl, dbl, tpl, hr, bb,
                    run, rbi, out, sf, g, 0, firestoreID);

            TextView abView = rootView.findViewById(R.id.playerboard_ab);
            TextView hitView = rootView.findViewById(R.id.playerboard_hit);
            TextView hrView = rootView.findViewById(R.id.player_hr);
            TextView rbiView = rootView.findViewById(R.id.player_rbi);
            TextView runView = rootView.findViewById(R.id.player_runs);
            TextView avgView = rootView.findViewById(R.id.player_avg);
            TextView obpView = rootView.findViewById(R.id.playerboard_obp);
            TextView slgView = rootView.findViewById(R.id.player_slg);
            TextView opsView = rootView.findViewById(R.id.player_ops);
            TextView sglView = rootView.findViewById(R.id.playerboard_1b);
            TextView dblView = rootView.findViewById(R.id.playerboard_2b);
            TextView tplView = rootView.findViewById(R.id.playerboard_3b);
            TextView bbView = rootView.findViewById(R.id.playerboard_bb);
            TextView teamView = rootView.findViewById(R.id.player_team);

            if(selectionType == MainPageSelection.TYPE_LEAGUE) {
                teamView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (teamString != null) {
                            Intent intent;
                            String selection = StatsEntry.COLUMN_NAME + "=?";
                            String[] selectionArgs = new String[]{teamString};
                            Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                                    null, selection, selectionArgs, null);
                            if (cursor.moveToFirst()) {
                                int playerId = cursor.getInt(cursor.getColumnIndex(StatsEntry._ID));
                                Uri teamUri = ContentUris.withAppendedId(StatsEntry.CONTENT_URI_TEAMS, playerId);
                                intent = new Intent(getActivity(), TeamPagerActivity.class);
                                intent.setData(teamUri);
                            } else {
                                intent = new Intent(getActivity(), LeagueManagerActivity.class);
                            }
                            cursor.close();
                            startActivity(intent);
                        } else {
                            Log.d("PlayerActivity", "Error going to team page");
                        }
                    }
                });
            }


            int color;
            if (gender == 0) {
                color = R.color.male;
            } else {
                color = R.color.female;
            }
            nameView.setTextColor(getResources().getColor(color));
            playerName = player.getName();
            nameView.setText(playerName);
            teamView.setText(teamString);
            abView.setText(String.valueOf(player.getABs()));
            hitView.setText(String.valueOf(player.getHits()));
            hrView.setText(String.valueOf(hr));
            rbiView.setText(String.valueOf(rbi));
            runView.setText(String.valueOf(run));
            sglView.setText(String.valueOf(sgl));
            dblView.setText(String.valueOf(dbl));
            tplView.setText(String.valueOf(tpl));
            bbView.setText(String.valueOf(bb));
            avgView.setText(String.valueOf(formatter.format(player.getAVG())));
            obpView.setText(String.valueOf(formatter.format(player.getOBP())));
            slgView.setText(String.valueOf(formatter.format(player.getSLG())));
            opsView.setText(String.valueOf(formatter.format(player.getOPS())));

            if (selectionType == MainPageSelection.TYPE_PLAYER) {
                setPlayerManager();
            }
        } else if (selectionType == MainPageSelection.TYPE_PLAYER) {
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_NAME, playerString);
            getActivity().getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
            setPlayerManager();
        }
    }

    private void setPlayerManager() {
        View playerManager = getView().findViewById(R.id.player_mgr);
        playerManager.setVisibility(View.VISIBLE);

        runs = 0;
        rbi = 0;
        resultCount = 0;
        rbiText = playerManager.findViewById(R.id.textview_rbi);
        runsText = playerManager.findViewById(R.id.textview_runs);
        resultText = playerManager.findViewById(R.id.textview_results);
        final Spinner resultSpinner = playerManager.findViewById(R.id.spinner_result);
        ImageButton addRuns = playerManager.findViewById(R.id.btn_add_run);
        ImageButton addRBI = playerManager.findViewById(R.id.btn_add_rbi);
        ImageButton addResult = playerManager.findViewById(R.id.btn_add_result);
        ImageButton subtractRuns = playerManager.findViewById(R.id.btn_subtract_run);
        ImageButton subtractRBI = playerManager.findViewById(R.id.btn_subtract_rbi);
        ImageButton subtractResult = playerManager.findViewById(R.id.btn_subtract_result);
        Button submitButton = playerManager.findViewById(R.id.submit);

        addRuns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runs++;
                runsText.setText(String.valueOf(runs));
            }
        });
        addRBI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rbi++;
                rbiText.setText(String.valueOf(rbi));
            }
        });
        addResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultCount++;
                resultText.setText(String.valueOf(resultCount));
            }
        });
        subtractRuns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runs--;
                runsText.setText(String.valueOf(runs));
            }
        });
        subtractRBI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rbi--;
                rbiText.setText(String.valueOf(rbi));
            }
        });
        subtractResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultCount--;
                resultText.setText(String.valueOf(resultCount));
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String result = resultSpinner.getSelectedItem().toString();
                String statEntry;

                Cursor cursor = getActivity().getContentResolver().query(mCurrentPlayerUri,
                        null, null, null, null);
                if (cursor.moveToFirst()) {
                    switch (result) {
                        case "1B":
                            statEntry = StatsEntry.COLUMN_1B;
                            break;
                        case "2B":
                            statEntry = StatsEntry.COLUMN_2B;
                            break;
                        case "3B":
                            statEntry = StatsEntry.COLUMN_3B;
                            break;
                        case "HR":
                            statEntry = StatsEntry.COLUMN_HR;
                            break;
                        case "BB":
                            statEntry = StatsEntry.COLUMN_BB;
                            break;
                        case "Out":
                            statEntry = StatsEntry.COLUMN_OUT;
                            break;
                        case "SF":
                            statEntry = StatsEntry.COLUMN_SF;
                            break;
                        default:
                            return;
                    }
                    int currentResultCount = cursor.getInt(cursor.getColumnIndex(statEntry));
                    resultCount += currentResultCount;
                    int currentRuns = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_RUN));
                    runs += currentRuns;
                    int currentRBI = cursor.getInt(cursor.getColumnIndex(StatsEntry.COLUMN_RBI));
                    rbi += currentRBI;
                    ContentValues values = new ContentValues();
                    values.put(statEntry, resultCount);
                    values.put(StatsEntry.COLUMN_RUN, runs);
                    values.put(StatsEntry.COLUMN_RBI, rbi);
                    getActivity().getContentResolver().update(mCurrentPlayerUri,
                            values, null, null);
                }
                cursor.close();

                runs = 0;
                rbi = 0;
                resultCount = 0;
                runsText.setText(String.valueOf(0));
                rbiText.setText(String.valueOf(0));
                resultText.setText(String.valueOf(0));
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (levelAuthorized(3)) {
            inflater.inflate(R.menu.menu_player, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_name:
                editNameDialog();
                return true;
            case R.id.action_change_team:
                changeTeamDialog();
                return true;
            case R.id.action_edit_photo:
                return true;
            case R.id.action_delete_player:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (levelAuthorized(4)) {
            menu.findItem(R.id.action_delete_player).setVisible(true);
        }
        if (selectionType == MainPageSelection.TYPE_LEAGUE) {
            menu.findItem(R.id.action_change_team).setVisible(true);
        }

    }

    private void showDeleteConfirmationDialog() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = DeleteConfirmationDialogFragment.newInstance(playerName);
        newFragment.show(fragmentTransaction, "");
    }

    private void editNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String titleString = getContext().getResources().getString(R.string.edit_object_name);
        String title = String.format(titleString, playerName);
        builder.setTitle(title);
        final LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_edit_name, null))
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog dialog1 = (Dialog) dialog;
                        EditText editText = dialog1.findViewById(R.id.username);
                        String enteredPlayer = editText.getText().toString();
                        if (nameAlreadyInDB(enteredPlayer)) {
                            Toast.makeText(getActivity(), enteredPlayer + " already exists!",
                                    Toast.LENGTH_SHORT).show();
                            editNameDialog();
                        } else {
                            updatePlayerName(enteredPlayer);
                        }
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

    private void changeTeamDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String sortOrder = StatsEntry.COLUMN_NAME + " COLLATE NOCASE ASC";

        Cursor mCursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                new String[]{StatsEntry.COLUMN_NAME}, null, null, sortOrder);
        ArrayList<String> teams = new ArrayList<>();
        while (mCursor.moveToNext()) {
            int teamNameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            String teamName = mCursor.getString(teamNameIndex);
            teams.add(teamName);
        }
        teams.add(getString(R.string.waivers));
        final CharSequence[] teams_array = teams.toArray(new CharSequence[teams.size()]);
        String titleString = getContext().getResources().getString(R.string.edit_player_team);
        String title = String.format(titleString, playerName);
        builder.setTitle(title);
        builder.setItems(teams_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                teamString = teams_array[item].toString();
                if (teamString.equals(getString(R.string.waivers))) {
                    teamString = "Free Agent";
                }
                updatePlayerTeam(teamString);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void deletePlayer() {
        if (mCurrentPlayerUri != null) {
            String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
            String[] selectionArgs = new String[]{firestoreID};
            int rowsDeleted = getActivity().getContentResolver().delete(mCurrentPlayerUri, selection, selectionArgs);
            if (rowsDeleted == 1) {
                Toast.makeText(getActivity(), playerString + " " + getString(R.string.editor_delete_player_successful), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.editor_delete_player_failed), Toast.LENGTH_SHORT).show();
            }
        }
        if (getActivity() instanceof PlayerPagerActivity) {
            ((PlayerPagerActivity) getActivity()).returnResult(true);
        };
    }

    private void updatePlayerName(String player) {
        playerString = player;
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_NAME, playerString);
        contentValues.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);

        getActivity().getContentResolver().update(mCurrentPlayerUri, contentValues, null, null);
        getLoaderManager().restartLoader(EXISTING_PLAYER_LOADER, null, this);
    }

    private void updatePlayerTeam(String team) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_TEAM, team);
        contentValues.put(StatsEntry.COLUMN_ORDER, 99);
        contentValues.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);

        getActivity().getContentResolver().update(mCurrentPlayerUri, contentValues, null, null);
    }

    private boolean nameAlreadyInDB(String playerName) {
        String selection = StatsEntry.COLUMN_NAME + " = '" + playerName + "' COLLATE NOCASE";

        Cursor cursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS, null, selection, null, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    private boolean levelAuthorized(int level) {
        return mLevel >= level;
    }

}
