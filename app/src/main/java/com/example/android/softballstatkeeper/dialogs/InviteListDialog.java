package com.example.android.softballstatkeeper.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;

import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.adapters.InviteListRecyclerViewAdapter;
import com.example.android.softballstatkeeper.models.MainPageSelection;

import java.util.ArrayList;
import java.util.List;

public class InviteListDialog extends DialogFragment {

    private InviteListRecyclerViewAdapter mAdapter;
    private List<MainPageSelection> inviteList;
    private OnFragmentInteractionListener mListener;
    private static final String ARG_LIST = "list";


    public InviteListDialog() {
        // Required empty public constructor
    }

    public static InviteListDialog newInstance(ArrayList<MainPageSelection> list) {
        InviteListDialog fragment = new InviteListDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_LIST, list);
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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_invite_list, null);
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

    private void onButtonPressed(SparseIntArray changes) {
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
