package com.example.android.softballstatkeeper.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;


import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.adapters_listeners_etc.AddPlayersRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AddNewPlayersDialogFragment extends DialogFragment {

    private AddPlayersRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private OnListFragmentInteractionListener mListener;
    private static final String KEY_NAMES = "names";
    private static final String KEY_GENDERS = "genders";
    private static final String KEY_EDITS = "edits";
    private static final String KEY_TEAM_NAME = "team_name";
    private static final String KEY_TEAM_ID = "team_id";
    private String mTeamName;
    private String mTeamID;

    public AddNewPlayersDialogFragment() {
    }

    public static AddNewPlayersDialogFragment newInstance(String teamName, String teamID) {
        AddNewPlayersDialogFragment fragment = new AddNewPlayersDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TEAM_NAME, teamName);
        args.putString(KEY_TEAM_ID, teamID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle == null) {
            return;
        }
        mTeamName = bundle.getString(KEY_TEAM_NAME);
        mTeamID = bundle.getString(KEY_TEAM_ID);

        if (savedInstanceState != null) {
            List<Integer> edits = savedInstanceState.getIntegerArrayList(KEY_EDITS);
            List<Integer> genderEntries = savedInstanceState.getIntegerArrayList(KEY_GENDERS);
            List<String> nameEntries = savedInstanceState.getStringArrayList(KEY_NAMES);
            mAdapter = new AddPlayersRecyclerViewAdapter(nameEntries, genderEntries, edits);
        } else {
            mAdapter = new AddPlayersRecyclerViewAdapter();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_createteam_list, null);

        Context context = view.getContext();
        mRecyclerView = (RecyclerView) view;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(mAdapter);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(mRecyclerView)
                .setTitle("Add new players to " + mTeamName)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        View view = getActivity().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        }
                        onButtonPressed();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return alertDialog;
    }

    public void onButtonPressed() {
        if (mListener != null) {
            ArrayList<String> names = new ArrayList<>(mAdapter.getmNameEntries());
            ArrayList<Integer> genders = new ArrayList<>(mAdapter.getmGenderEntries());
            mListener.onSubmitPlayersListener(names, genders, mTeamName, mTeamID);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> names = new ArrayList<>(mAdapter.getmNameEntries());
        ArrayList<Integer> genders = new ArrayList<>(mAdapter.getmGenderEntries());
        HashSet<Integer> edits = mAdapter.getmEdits();
        outState.putStringArrayList(KEY_NAMES, names);
        outState.putIntegerArrayList(KEY_GENDERS, new ArrayList<>(genders));
        outState.putIntegerArrayList(KEY_EDITS, new ArrayList<>(edits));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mRecyclerView.setAdapter(null);
        mAdapter = null;
        mRecyclerView = null;
    }

    public interface OnListFragmentInteractionListener {
        void onSubmitPlayersListener(List<String> names, List<Integer> genders, String team, String teamID);
    }
}
