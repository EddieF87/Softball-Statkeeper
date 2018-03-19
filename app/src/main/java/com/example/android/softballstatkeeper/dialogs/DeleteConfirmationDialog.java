package com.example.android.softballstatkeeper.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.example.android.softballstatkeeper.R;


public class DeleteConfirmationDialog extends DialogFragment {

    private OnFragmentInteractionListener mListener;
    private String objectToDelete;

    public DeleteConfirmationDialog() {
        // Required empty public constructor
    }

    public static DeleteConfirmationDialog newInstance(String objectToDelete) {

        Bundle args = new Bundle();
        DeleteConfirmationDialog fragment = new DeleteConfirmationDialog();
        args.putString("objectToDelete", objectToDelete);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        objectToDelete = args != null ? args.getString("objectToDelete") : null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = String.format(getString(R.string.delete_dialog_msg), objectToDelete);

        return new AlertDialog.Builder(getActivity())
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
    }


    private void onButtonPressed(boolean delete) {
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
