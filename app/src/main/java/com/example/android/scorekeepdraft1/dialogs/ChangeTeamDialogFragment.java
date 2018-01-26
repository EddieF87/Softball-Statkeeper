package com.example.android.scorekeepdraft1.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;

import java.util.ArrayList;
import java.util.List;


public class ChangeTeamDialogFragment extends DialogFragment {

    private List<String> teams;
    private String player;
    private static final String KEY_TEAMS = "teams";
    private static final String KEY_PLAYER = "player";
    private OnFragmentInteractionListener mListener;

    public ChangeTeamDialogFragment() {
        // Required empty public constructor
    }

    public static ChangeTeamDialogFragment newInstance(ArrayList<String> teams, String player) {

        Bundle args = new Bundle();
        args.putStringArrayList(KEY_TEAMS, teams);
        args.putString(KEY_PLAYER, player);

        ChangeTeamDialogFragment fragment = new ChangeTeamDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        teams = args.getStringArrayList(KEY_TEAMS);
        player = args.getString(KEY_PLAYER);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_name, null);

        String titleString = getContext().getResources().getString(R.string.edit_player_team);
        String title = String.format(titleString, player);
        final CharSequence[] teams_array = teams.toArray(new CharSequence[teams.size()]);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setItems(teams_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String team = teams_array[item].toString();
                        if (team.equals(getString(R.string.waivers))) {
                            team = "Free Agent";
                        }
                        onButtonPressed(team);
                    }
                })
                .create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    public void onButtonPressed(String team) {
        if (mListener != null) {
            mListener.onTeamChosen(team);
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

    public interface OnFragmentInteractionListener {
        void onTeamChosen(String team);
    }
}
