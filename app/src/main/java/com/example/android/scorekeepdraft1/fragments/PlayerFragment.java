package com.example.android.scorekeepdraft1.fragments;


import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.LeagueManagerActivity;
import com.example.android.scorekeepdraft1.activities.PlayerManagerActivity;
import com.example.android.scorekeepdraft1.activities.PlayerPagerActivity;
import com.example.android.scorekeepdraft1.activities.TeamPagerActivity;
import com.example.android.scorekeepdraft1.data.FirestoreHelper;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.dialogs.ChangeTeamDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.DeleteConfirmationDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.EditNameDialogFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private NumberFormat formatter = new DecimalFormat("#.000");
    private static final int EXISTING_PLAYER_LOADER = 0;
    private Uri mCurrentPlayerUri;
    private int mLevel;
    private String mSelectionID;
    private String teamString;
    private String playerName;
    private String firestoreID;
    private int gender;
    private static final String KEY_PLAYER_URI = "playerURI";
    private int selectionType;
    private TextView resultCountText;
    private int resultCount;
    private String result;
    private TextView resultText;
    private RadioGroup group1;
    private RadioGroup group2;

    public PlayerFragment() {
        // Required empty public constructor
    }


    public static PlayerFragment newInstance(String leagueID, int leagueType, int level, Uri uri) {
        Bundle args = new Bundle();
        args.putInt(MainPageSelection.KEY_SELECTION_TYPE, leagueType);
        args.putInt(MainPageSelection.KEY_SELECTION_LEVEL, level);
        args.putString(MainPageSelection.KEY_SELECTION_ID, leagueID);
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
            playerName = args.getString(MainPageSelection.KEY_SELECTION_NAME);
            mCurrentPlayerUri = StatsEntry.CONTENT_URI_PLAYERS;
        } else {
            String uriString = args.getString(KEY_PLAYER_URI);
            mCurrentPlayerUri = Uri.parse(uriString);
            mSelectionID = args.getString(MainPageSelection.KEY_SELECTION_ID);
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

            playerName = cursor.getString(nameIndex);
            teamString = cursor.getString(teamIndex);
            gender = cursor.getInt(genderIndex);
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

            Player player = new Player(playerName, teamString, gender, sgl, dbl, tpl, hr, bb,
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

            if (selectionType == MainPageSelection.TYPE_LEAGUE) {
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
            values.put(StatsEntry.COLUMN_NAME, playerName);
            getActivity().getContentResolver().insert(StatsEntry.CONTENT_URI_PLAYERS, values);
            setPlayerManager();
        }
    }

    private void setPlayerManager() {
        View playerManager = getView().findViewById(R.id.player_mgr);
        playerManager.setVisibility(View.VISIBLE);
        setRadioButtons(playerManager);

        resultCount = 0;
        resultCountText = playerManager.findViewById(R.id.textview_result_count);
        resultText = playerManager.findViewById(R.id.textview_result_chosen);
        Button submitBtn = playerManager.findViewById(R.id.submit);
        ImageButton addBtn = playerManager.findViewById(R.id.btn_add_result);
        ImageButton subtractBtn = playerManager.findViewById(R.id.btn_subtract_result);

        subtractBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultCount--;
                resultCountText.setText(String.valueOf(resultCount));
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultCount++;
                resultCountText.setText(String.valueOf(resultCount));
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (result == null) {
                    Toast.makeText(getActivity(), "Please select a result first.", Toast.LENGTH_SHORT).show();
                    return;
                }

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
                        case "Run":
                            statEntry = StatsEntry.COLUMN_RUN;
                            break;
                        case "RBI":
                            statEntry = StatsEntry.COLUMN_RBI;
                            break;
                        default:
                            return;
                    }
                    int currentResultCount = cursor.getInt(cursor.getColumnIndex(statEntry));
                    resultCount += currentResultCount;
                    ContentValues values = new ContentValues();
                    values.put(statEntry, resultCount);
                    getActivity().getContentResolver().update(mCurrentPlayerUri,
                            values, null, null);
                }
                cursor.close();

                resultCount = 0;
                resultCountText.setText(String.valueOf(0));
            }
        });
    }

    public void setRadioButtons(View view) {
        group1 = view.findViewById(R.id.group1);
        group2 = view.findViewById(R.id.group2);
        RadioButton single = view.findViewById(R.id.single);
        RadioButton dbl = view.findViewById(R.id.dbl);
        RadioButton triple = view.findViewById(R.id.triple);
        RadioButton hr = view.findViewById(R.id.hr);
        RadioButton bb = view.findViewById(R.id.bb);
        RadioButton out = view.findViewById(R.id.out);
        RadioButton sf = view.findViewById(R.id.sf);
        RadioButton run = view.findViewById(R.id.run);
        RadioButton rbi = view.findViewById(R.id.rbi);
        single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group2.clearCheck();
                    result = "1B";
                    resultText.setText(result);
                }
            }
        });
        dbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group2.clearCheck();
                    result = "2B";
                    resultText.setText(result);
                }
            }
        });
        triple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group2.clearCheck();
                    result = "3B";
                    resultText.setText(result);
                }
            }
        });
        hr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group2.clearCheck();
                    result = "HR";
                    resultText.setText(result);
                }
            }
        });
        bb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group1.clearCheck();
                    result = "BB";
                    resultText.setText(result);
                }
            }
        });
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group1.clearCheck();
                    result = "Out";
                    resultText.setText(result);
                }
            }
        });
        sf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group1.clearCheck();
                    result = "SF";
                    resultText.setText(result);
                }
            }
        });
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group1.clearCheck();
                    result = "Run";
                    resultText.setText(result);
                }
            }
        });
        rbi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group1.clearCheck();
                    result = "RBI";
                    resultText.setText(result);
                }
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
            case R.id.action_export_stats:
                Activity activity = getActivity();
                if (activity instanceof PlayerManagerActivity) {
                    PlayerManagerActivity playerManagerActivity = (PlayerManagerActivity) activity;
                    playerManagerActivity.startExport(playerName);
                    return true;
                }
                return false;
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
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = EditNameDialogFragment.newInstance(playerName);
        newFragment.show(fragmentTransaction, "");
    }

    private void changeTeamDialog() {
        ArrayList<String> teams = new ArrayList<>();

        String sortOrder = StatsEntry.COLUMN_NAME + " COLLATE NOCASE ASC";
        Cursor mCursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                new String[]{StatsEntry.COLUMN_NAME}, null, null, sortOrder);

        while (mCursor.moveToNext()) {
            int teamNameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            String teamName = mCursor.getString(teamNameIndex);
            teams.add(teamName);
        }
        teams.add(getString(R.string.waivers));

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = ChangeTeamDialogFragment.newInstance(teams, playerName);
        newFragment.show(fragmentTransaction, "");
    }

    public void deletePlayer() {
        FirestoreHelper firestoreHelper = new FirestoreHelper(getActivity(), mSelectionID);
        if (mCurrentPlayerUri != null) {
            String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
            String[] selectionArgs = new String[]{firestoreID};
            int rowsDeleted = getActivity().getContentResolver().delete(mCurrentPlayerUri, selection, selectionArgs);
            if (rowsDeleted > 0) {
                String team;
                if(teamString == null) {
                    team = "Free Agent";
                } else {
                    team = teamString;
                }
                firestoreHelper.addDeletion(firestoreID, 1, playerName, gender, team);
                Toast.makeText(getActivity(), playerName + " " + getString(R.string.editor_delete_player_successful), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.editor_delete_player_failed), Toast.LENGTH_SHORT).show();
            }
        }
        if (getActivity() instanceof PlayerPagerActivity) {
            ((PlayerPagerActivity) getActivity()).returnDeleteResult(Activity.RESULT_OK, playerName);
        }
        ;
    }

    public boolean updatePlayerName(String player) {
        playerName = player;
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_NAME, playerName);
        contentValues.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);

        int rowsUpdated = getActivity().getContentResolver().update(mCurrentPlayerUri, contentValues, null, null);
        getLoaderManager().restartLoader(EXISTING_PLAYER_LOADER, null, this);
        return rowsUpdated > 0;
    }

    public void updatePlayerTeam(String team) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_TEAM, team);
        contentValues.put(StatsEntry.COLUMN_ORDER, 99);
        contentValues.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
        getActivity().getContentResolver().update(mCurrentPlayerUri, contentValues, null, null);

        if(selectionType != MainPageSelection.TYPE_PLAYER) {
            FirestoreHelper firestoreHelper = new FirestoreHelper(getActivity(), mSelectionID);
            firestoreHelper.setUpdate(firestoreID, 1);
            firestoreHelper.updateTimeStamps();
        }
    }

    private boolean levelAuthorized(int level) {
        return mLevel >= level;
    }

}