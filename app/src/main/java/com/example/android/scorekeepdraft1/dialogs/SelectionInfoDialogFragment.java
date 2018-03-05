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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectionInfoDialogFragment extends DialogFragment {

    private MainPageSelection mSelection;
    private SelectionInfoDialogFragment.OnFragmentInteractionListener mListener;

    public static SelectionInfoDialogFragment newInstance(MainPageSelection selection) {

        Bundle args = new Bundle();
        SelectionInfoDialogFragment fragment = new SelectionInfoDialogFragment();
        args.putParcelable("mSelection", selection);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mSelection = args.getParcelable("mSelection");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_name, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String titleString = getResources().getString(R.string.edit_object_name);
        String title = String.format(titleString, mSelection);
        builder.setTitle(title);
        builder.setView(v)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onDelete(mSelection);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SelectionInfoDialogFragment.OnFragmentInteractionListener) {
            mListener = (SelectionInfoDialogFragment.OnFragmentInteractionListener) context;
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
        void onDelete(MainPageSelection selection);
    }
}
