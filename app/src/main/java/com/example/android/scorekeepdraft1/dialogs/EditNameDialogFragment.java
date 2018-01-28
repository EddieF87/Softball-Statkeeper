package com.example.android.scorekeepdraft1.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.R;

public class EditNameDialogFragment extends DialogFragment {

    private EditNameDialogFragment.OnFragmentInteractionListener mListener;
    private String objectToEdit;

    public EditNameDialogFragment() {
        // Required empty public constructor
    }

    public static EditNameDialogFragment newInstance(String objectToEdit) {

        Bundle args = new Bundle();
        EditNameDialogFragment fragment = new EditNameDialogFragment();
        args.putString("objectToEdit", objectToEdit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        objectToEdit = args.getString("objectToEdit");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_name, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String titleString = getResources().getString(R.string.edit_object_name);
        String title = String.format(titleString, objectToEdit);
        builder.setTitle(title);
        builder.setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog dialog1 = (Dialog) dialog;
                        EditText editText = dialog1.findViewById(R.id.username);
                        String enteredText = editText.getText().toString();
                        onButtonPressed(enteredText);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed("");
                    }
                });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }


    public void onButtonPressed(String enteredText) {
        if (mListener != null) {
            mListener.onEdit(enteredText);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EditNameDialogFragment.OnFragmentInteractionListener) {
            mListener = (EditNameDialogFragment.OnFragmentInteractionListener) context;
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
        void onEdit(String enteredText);
    }
}
