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

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.SelectTeamRecyclerViewAdapter;
import com.example.android.scorekeepdraft1.objects.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseOrCreateTeamDialogFragment extends DialogFragment
implements SelectTeamRecyclerViewAdapter.OnAdapterInteractionListener{


    private List<Team> mTeams;
    private static final String KEY_TEAMS = "mTeams";
    private TextView teamChoiceText;
    private Team mTeamChoice;
    private EditText newTeamEditText;

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
        teamChoiceText = view.findViewById(R.id.team_chosen_text);

        SelectTeamRecyclerViewAdapter mAdapter = new SelectTeamRecyclerViewAdapter(mTeams, this);
        RecyclerView recyclerView = view.findViewById(R.id.team_list);
//        recyclerView.setHasFixedSize(true);

        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);

//        final CharSequence[] teams_array = mTeams.toArray(new CharSequence[mTeams.size()]);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Choose or Create a Team")
                .create();

        Button submitTeamChosenButton = view.findViewById(R.id.btn_submit_team_chosen);
        submitTeamChosenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTeamChoice == null) {
                    return;
                }
                mListener.onTeamSelected(mTeamChoice.getName(), mTeamChoice.getFirestoreID());
                alertDialog.dismiss();
            }
        });
        Button submitNewTeam = view.findViewById(R.id.btn_submit_new_team);
        submitNewTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(newTeamEditText.getText().length() == 0) {
                    return;
                }
                String newTeam = newTeamEditText.getText().toString();
                mListener.onNewTeam(newTeam);
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

    public void onButtonPressed(String teamName, String teamID) {
        if (mListener != null) {
            mListener.onTeamSelected(teamName, teamID);
        }
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

    @Override
    public void onTeamClicked(Team team) {
        mTeamChoice = team;
        teamChoiceText.setText(mTeamChoice.getName());
    }

    public interface OnFragmentInteractionListener {
        void onTeamSelected(String teamName, String teamID);
        void onNewTeam(String team);
        void onCancel();
    }
}