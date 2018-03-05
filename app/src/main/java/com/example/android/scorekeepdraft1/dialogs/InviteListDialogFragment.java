package com.example.android.scorekeepdraft1.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.InviteListRecyclerViewAdapter;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InviteListDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InviteListDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InviteListDialogFragment extends DialogFragment {

    private InviteListRecyclerViewAdapter mAdapter;
    private List<MainPageSelection> inviteList;
    private OnFragmentInteractionListener mListener;
    private static final String ARG_LIST = "list";


    public InviteListDialogFragment() {
        // Required empty public constructor
    }

    public static InviteListDialogFragment newInstance(List<MainPageSelection> list) {
        InviteListDialogFragment fragment = new InviteListDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_LIST, (ArrayList<? extends Parcelable>) list);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            inviteList = getArguments().getParcelableArrayList(ARG_LIST);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_invite_list_dialog, null);
        RecyclerView recyclerView = view.findViewById(R.id.invite_list_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new InviteListRecyclerViewAdapter(inviteList);
        recyclerView.setAdapter(mAdapter);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Your Invites")
                .setMessage("You've been invited to StatKeepers:")
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SparseIntArray changes = mAdapter.getChanges();
                        onButtonPressed(changes);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        return alertDialog;



    }

    public void onButtonPressed(SparseIntArray changes) {
        if (mListener != null) {
            mListener.onInvitesSorted(inviteList, changes);
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
        void onInvitesSorted(List<MainPageSelection> list, SparseIntArray changes);
    }
}
