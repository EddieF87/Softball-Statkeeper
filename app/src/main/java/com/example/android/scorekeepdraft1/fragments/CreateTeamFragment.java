package com.example.android.scorekeepdraft1.fragments;

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


import com.example.android.scorekeepdraft1.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CreateTeamFragment extends DialogFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private CreateTeamRecyclerViewAdapter mAdapter;
    private OnListFragmentInteractionListener mListener;
    private static final String KEY_NAMES = "names";
    private static final String KEY_GENDERS = "genders";
    private static final String KEY_EDITS = "edits";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CreateTeamFragment() {
    }

//    // TODO: Customize parameter initialization
//    @SuppressWarnings("unused")
//    public static CreateTeamFragment newInstance() {
//        CreateTeamFragment fragment = new CreateTeamFragment();
//        Bundle args = new Bundle();
//        Log.d("", "CreateTeamFrag newinstance");
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            List <Integer> edits = savedInstanceState.getIntegerArrayList(KEY_EDITS);
            List <Integer> genderEntries = savedInstanceState.getIntegerArrayList(KEY_GENDERS);
            List<String> nameEntries = savedInstanceState.getStringArrayList(KEY_NAMES);
            mAdapter = new CreateTeamRecyclerViewAdapter(nameEntries, genderEntries, edits);
        } else {
            mAdapter = new CreateTeamRecyclerViewAdapter();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_createteam_list, null);

        RecyclerView recyclerView;

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(mAdapter);
        } else {
            return null;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(recyclerView)
                .setTitle(R.string.end_game_msg)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(true);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(false);
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    public void onButtonPressed(boolean save) {
        if (mListener != null) {
            ArrayList<String> names = new ArrayList<>(mAdapter.getmNameEntries());
            ArrayList<Integer> genders = new ArrayList<>(mAdapter.getmGenderEntries());
            mListener.onListFragmentInteraction(names, genders);
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
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(List<String> names, List<Integer> genders);
    }
}
