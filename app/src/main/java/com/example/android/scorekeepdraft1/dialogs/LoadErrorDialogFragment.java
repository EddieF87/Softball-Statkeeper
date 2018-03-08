package com.example.android.scorekeepdraft1.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import com.example.android.scorekeepdraft1.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoadErrorDialogFragment extends DialogFragment {
    
    private LoadErrorDialogFragment.OnFragmentInteractionListener mListener;

    public LoadErrorDialogFragment() {
        // Required empty public constructor
    }

    public static LoadErrorDialogFragment newInstance() {
        return new LoadErrorDialogFragment();
    }

    public void onButtonPressed(boolean choice) {
        if (mListener != null) {
            mListener.loadChoice(choice);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoadErrorDialogFragment.OnFragmentInteractionListener) {
            mListener = (LoadErrorDialogFragment.OnFragmentInteractionListener) context;
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
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog, null);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
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
