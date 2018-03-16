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
import com.example.android.softballstatkeeper.activities.UsersActivity;
import com.example.android.softballstatkeeper.models.MainPageSelection;


public class DeleteSelectionDialog extends DialogFragment {

    private DeleteSelectionDialog.OnFragmentInteractionListener mListener;
    private MainPageSelection mSelection;
    private int mPosition;

    public DeleteSelectionDialog() {
        // Required empty public constructor
    }

    public static DeleteSelectionDialog newInstance(MainPageSelection selection, int pos) {

        Bundle args = new Bundle();
        DeleteSelectionDialog fragment = new DeleteSelectionDialog();
        args.putParcelable("mSelection", selection);
        args.putInt("mPosition", pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mSelection = args.getParcelable("mSelection");
            mPosition = args.getInt("mPosition");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String name = mSelection.getName();
        int level = mSelection.getLevel();

        String message;
        if (level == UsersActivity.LEVEL_CREATOR) {
            message = getString(R.string.delete_warning_msg);
        } else {
            String type;
            switch (mSelection.getType()) {
                case MainPageSelection.TYPE_LEAGUE:
                    type = "league";
                    break;
                case MainPageSelection.TYPE_TEAM:
                    type = "team";
                    break;
                case MainPageSelection.TYPE_PLAYER:
                    type = "player";
                    break;
                default:
                    type = "error!!!";
            }
            message = String.format(getString(R.string.remove_warning_msg), type);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle("Delete " + name + "?")
                .setMessage(message)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onDeleteConfirmed(mSelection, mPosition);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DeleteSelectionDialog.OnFragmentInteractionListener) {
            mListener = (DeleteSelectionDialog.OnFragmentInteractionListener) context;
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
        void onDeleteConfirmed(MainPageSelection selection, int pos);
    }
}
