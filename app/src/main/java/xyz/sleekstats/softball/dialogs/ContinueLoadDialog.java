package xyz.sleekstats.softball.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import xyz.sleekstats.softball.R;


public class ContinueLoadDialog extends DialogFragment {

    private ContinueLoadDialog.OnFragmentInteractionListener mListener;
    private AlertDialog myDialog;

    public ContinueLoadDialog() {
        // Required empty public constructor
    }

    private void onButtonPressed(boolean choice) {
        if (mListener != null) {
            mListener.loadChoice(choice);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ContinueLoadDialog.OnFragmentInteractionListener) {
            mListener = (ContinueLoadDialog.OnFragmentInteractionListener) context;
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

        myDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Accessing database: Please Wait!")
                .setMessage(R.string.load_from_cache)
                .setPositiveButton(R.string.load, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(true);
                    }
                })
                .setNegativeButton(R.string.wait, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(false);
                    }
                })
                .setCancelable(false)
                .create();
        myDialog.setCanceledOnTouchOutside(false);
        return myDialog;
    }

    public void dismissIfShowing(){
        if (myDialog != null && myDialog.isShowing()){
            myDialog.dismiss();
        }
    }
}
