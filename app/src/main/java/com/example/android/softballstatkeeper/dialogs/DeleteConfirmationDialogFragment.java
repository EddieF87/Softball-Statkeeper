package com.example.android.softballstatkeeper.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;

import com.example.android.softballstatkeeper.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeleteConfirmationDialogFragment extends DialogFragment {

    private OnFragmentInteractionListener mListener;
    private String objectToDelete;

    public DeleteConfirmationDialogFragment() {
        // Required empty public constructor
    }

    public static DeleteConfirmationDialogFragment newInstance(String objectToDelete) {

        Bundle args = new Bundle();
        DeleteConfirmationDialogFragment fragment = new DeleteConfirmationDialogFragment();
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
        String message = String.format(getString(R.string.delete_dialog_msg), objectToDelete);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Delete " + objectToDelete + "?")
                .setMessage(message)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(true);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(false);
                    }
                })
                .create();
        return alertDialog;
    }


    public void onButtonPressed(boolean delete) {
        if (mListener != null) {
            mListener.onDeletionChoice(delete);
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
        void onDeletionChoice(boolean delete);
    }
}
