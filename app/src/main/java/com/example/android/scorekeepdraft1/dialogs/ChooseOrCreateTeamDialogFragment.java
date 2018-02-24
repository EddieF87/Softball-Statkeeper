package com.example.android.scorekeepdraft1.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.SelectTeamRecyclerViewAdapter;
import com.example.android.scorekeepdraft1.objects.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseOrCreateTeamDialogFragment extends DialogFragment {


    private ArrayList<Team> mTeams;
    private static final String KEY_TEAMS = "mTeams";
    private EditText newTeamEditText;
    private Button mButton;

    private ChooseOrCreateTeamDialogFragment.OnFragmentInteractionListener mListener;

    public ChooseOrCreateTeamDialogFragment() {
        // Required empty public constructor
    }

    public static ChooseOrCreateTeamDialogFragment newInstance(ArrayList<Team> teams) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_TEAMS, teams);
        ChooseOrCreateTeamDialogFragment fragment = new ChooseOrCreateTeamDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mTeams = args.getParcelableArrayList(KEY_TEAMS);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog_choose_or_create_team, null);
        newTeamEditText = view.findViewById(R.id.edit_text_new_team);
        mButton = view.findViewById(R.id.select_current_team_btn);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Choose or Create a Team")
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onCancel();
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
                        String newTeam = newTeamEditText.getText().toString();
                        mListener.onNewTeam(newTeam);
                        if (!newTeam.isEmpty()) {
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
                alertDialog.dismiss();
            }
        });

        return alertDialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mListener.onCancel();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ChooseOrCreateTeamDialogFragment.OnFragmentInteractionListener) {
            mListener = (ChooseOrCreateTeamDialogFragment.OnFragmentInteractionListener) context;
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