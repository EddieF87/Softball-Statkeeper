package com.example.android.softballstatkeeper.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.views.MyEditText;
import com.example.android.softballstatkeeper.models.Team;

import java.util.ArrayList;


public class ChooseOrCreateTeamDialog extends DialogFragment {


    private ArrayList<Team> mTeams;
    private static final String KEY_TEAMS = "mTeams";
    private MyEditText newTeamEditText;

    private ChooseOrCreateTeamDialog.OnFragmentInteractionListener mListener;

    public ChooseOrCreateTeamDialog() {
        // Required empty public constructor
    }

    public static ChooseOrCreateTeamDialog newInstance(ArrayList<Team> teams) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_TEAMS, teams);
        ChooseOrCreateTeamDialog fragment = new ChooseOrCreateTeamDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mTeams = args.getParcelableArrayList(KEY_TEAMS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_choose_or_create_team, null);
        newTeamEditText = view.findViewById(R.id.edit_text_new_team);
        Button mButton = view.findViewById(R.id.select_current_team_btn);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Choose or Create a Team")
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onCancel();
                        newTeamEditText.setCursorVisible(false);
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton(R.string.submit, null)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newTeam = newTeamEditText.getText().toString().trim();
                        mListener.onNewTeam(newTeam);
                        if (!newTeam.isEmpty()) {
                            newTeamEditText.setCursorVisible(false);
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.chooseTeamDialog(mTeams);
                newTeamEditText.setCursorVisible(false);
                alertDialog.dismiss();
            }
        });

        return alertDialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mListener.onCancel();
        newTeamEditText.setCursorVisible(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ChooseOrCreateTeamDialog.OnFragmentInteractionListener) {
            mListener = (ChooseOrCreateTeamDialog.OnFragmentInteractionListener) context;
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
        void chooseTeamDialog(ArrayList<Team> teams);

        void onNewTeam(String team);

        void onCancel();
    }
}