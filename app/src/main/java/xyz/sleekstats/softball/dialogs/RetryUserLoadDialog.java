package xyz.sleekstats.softball.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import xyz.sleekstats.softball.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RetryUserLoadDialog extends DialogFragment {

    private OnFragmentInteractionListener mListener;
    private AlertDialog myDialog;

    public RetryUserLoadDialog() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        myDialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(true);
                    }
                })
                .setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(false);
                    }
                })
                .setMessage(R.string.check_connection_and_retry)
                .setTitle(R.string.unable_to_connect).create();

        return myDialog;
    }

    private void onButtonPressed(boolean choice) {
        if (mListener != null) {
            mListener.onRetryChoice(choice);
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
        void onRetryChoice(boolean choice);
    }


    public void dismissIfShowing(){
        if (myDialog != null && myDialog.isShowing()){
            myDialog.dismiss();
        }
    }
}
