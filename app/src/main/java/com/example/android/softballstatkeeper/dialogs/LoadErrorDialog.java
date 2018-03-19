package com.example.android.softballstatkeeper.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.example.android.softballstatkeeper.R;


public class LoadErrorDialog extends DialogFragment {
    
    private LoadErrorDialog.OnFragmentInteractionListener mListener;

    public LoadErrorDialog() {
        // Required empty public constructor
    }

    public static LoadErrorDialog newInstance() {
        return new LoadErrorDialog();
    }

    private void onButtonPressed(boolean choice) {
        if (mListener != null) {
            mListener.loadChoice(choice);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoadErrorDialog.OnFragmentInteractionListener) {
            mListener = (LoadErrorDialog.OnFragmentInteractionListener) context;
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
        void loadChoice(boolean load);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.load_from_cache)
                .setMessage("Some stats may not be up to date.")
                .setPositiveButton(R.string.load, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(true);
                    }
                })
                .setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(false);
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

}
