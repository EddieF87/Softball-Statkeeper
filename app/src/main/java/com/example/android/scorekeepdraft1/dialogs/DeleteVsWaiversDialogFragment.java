package com.example.android.scorekeepdraft1.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

import com.example.android.scorekeepdraft1.R;


public class DeleteVsWaiversDialogFragment extends DialogFragment {

    private OnFragmentInteractionListener mListener;
    private String objectToDelete;
    public static final int CHOICE_CANCEL = 0;
    public static final int CHOICE_WAIVERS = 1;
    public static final int CHOICE_DELETE = 2;

    public DeleteVsWaiversDialogFragment() {
        // Required empty public constructor
    }

    public static DeleteVsWaiversDialogFragment newInstance(String objectToDelete) {

        Bundle args = new Bundle();
        DeleteVsWaiversDialogFragment fragment = new DeleteVsWaiversDialogFragment();
        args.putString("objectToDelete", objectToDelete);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        objectToDelete = args.getString("objectToDelete");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog, null);

        AlertDialog choiceDialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.delete_or_freeagency_msg)
                .setPositiveButton(R.string.waivers, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(1);
                    }
                })
                .setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(2);
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onButtonPressed(0);
                    }
                })
                .create();
        return choiceDialog;
    }


    public void onButtonPressed(int choice) {
        if (mListener != null) {
            mListener.onDeleteVsWaiversChoice(choice);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DeleteVsWaiversDialogFragment.OnFragmentInteractionListener) {
            mListener = (DeleteVsWaiversDialogFragment.OnFragmentInteractionListener) context;
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
        void onDeleteVsWaiversChoice(int choice);
    }
}
