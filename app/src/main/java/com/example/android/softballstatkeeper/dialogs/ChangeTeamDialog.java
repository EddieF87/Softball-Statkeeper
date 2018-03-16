package com.example.android.softballstatkeeper.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.activities.LeagueManagerActivity;
import com.example.android.softballstatkeeper.data.StatsContract;
import com.example.android.softballstatkeeper.models.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChangeTeamDialog extends DialogFragment {

    private List<Team> mTeams;
    private String playerName;
    private String playerFirestoreID;
    private static final String KEY_TEAMS = "teams";
    private static final String KEY_PLAYER_NAME = "playername";
    private static final String KEY_PLAYER_FS_ID = "player_id";
    private OnFragmentInteractionListener mListener;

    public ChangeTeamDialog() {
        // Required empty public constructor
    }

    public static ChangeTeamDialog newInstance(ArrayList<Team> teams,
                                               String name, String firestoreID) {

        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_TEAMS, teams);
        args.putString(KEY_PLAYER_NAME, name);
        args.putString(KEY_PLAYER_FS_ID, firestoreID);

        ChangeTeamDialog fragment = new ChangeTeamDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mTeams = args.getParcelableArrayList(KEY_TEAMS);
            mTeams.add(new Team(StatsContract.StatsEntry.FREE_AGENT));
            playerName = args.getString(KEY_PLAYER_NAME);
            playerFirestoreID = args.getString(KEY_PLAYER_FS_ID);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title;
        if(getContext() instanceof LeagueManagerActivity){
            title = "Choose a team";
        } else {
            String titleString = getContext().getResources().getString(R.string.edit_player_team);
            title = String.format(titleString, playerName);
        }

        final Map<String, String> teamMap = new HashMap<>();
        final List<String> teamNames = new ArrayList<>();
        for (Team team : mTeams) {
            teamNames.add(team.getName());
            teamMap.put(team.getName(), team.getFirestoreID());
        }

        final CharSequence[] teams_array = teamNames.toArray(new CharSequence[mTeams.size()]);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setItems(teams_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String teamName = teams_array[item].toString();
                        String teamID = teamMap.get(teamName);
                        if (teamName.equals(getString(R.string.waivers))) {
                            teamName = StatsContract.StatsEntry.FREE_AGENT;
                        }
                        onButtonPressed(teamName, teamID);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onTeamChoiceCancel();
                    }
                })
                .create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    private void onButtonPressed(String teamName, String teamID) {
        if (mListener != null) {
            mListener.onTeamChosen(playerFirestoreID, teamName, teamID);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mListener.onTeamChoiceCancel();
    }

    public interface OnFragmentInteractionListener {
        void onTeamChosen(String playerID, String teamName, String teamID);
        void onTeamChoiceCancel();
    }
}
