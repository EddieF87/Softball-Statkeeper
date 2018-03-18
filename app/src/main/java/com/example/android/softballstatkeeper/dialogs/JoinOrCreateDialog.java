package com.example.android.softballstatkeeper.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.models.MainPageSelection;


public class JoinOrCreateDialog extends DialogFragment {

    private OnFragmentInteractionListener mListener;
    private int mType;

    public JoinOrCreateDialog() {
        // Required empty public constructor
    }

    public static android.support.v4.app.DialogFragment newInstance(int type) {

        Bundle args = new Bundle();
        JoinOrCreateDialog fragment = new JoinOrCreateDialog();
        args.putInt("mType", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mType = args.getInt("mType");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(true);
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        String title;
        switch (mType) {
            case MainPageSelection.TYPE_PLAYER:
                title = "Create a New Player StatKeeper";
                break;

            case MainPageSelection.TYPE_TEAM:
                title = "Join or Create a New Team StatKeeper";
                builder.setNegativeButton(R.string.join, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(false);
                    }
                });
                break;

            case MainPageSelection.TYPE_LEAGUE:
                title = "Join or Create a New League StatKeeper";
                builder.setNegativeButton(R.string.join, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(false);
                    }
                });
                break;
            default:
                return null;
        }
        builder.setTitle(title);

        return builder.create();
    }


    private void onButtonPressed(boolean create) {
        if (mListener != null) {
            mListener.onJoinOrCreate(create, mType);
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
        void onJoinOrCreate(boolean create, int type);
    }
}
