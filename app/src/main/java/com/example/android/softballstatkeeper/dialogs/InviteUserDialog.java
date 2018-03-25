package com.example.android.softballstatkeeper.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.android.softballstatkeeper.R;

public class InviteUserDialog extends DialogFragment {

    private OnFragmentInteractionListener mListener;

    public InviteUserDialog() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Invite Users")
                .setNeutralButton("Message Invite Codes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (mListener != null) {
                            mListener.onInviteUsers();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Grant Access Via emails", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onEmailInvites();
                        }
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onCancel();
                        }
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        try{
            final Button posbutton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            LinearLayout linearLayout = (LinearLayout) posbutton.getParent();
            linearLayout.setOrientation(LinearLayout.VERTICAL);

//            final Button negbutton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//            final Button neutbutton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                posbutton.setBackground(getContext().getDrawable(R.drawable.border));
////                posbutton.setWidth(width);
//                negbutton.setBackground(getContext().getDrawable(R.drawable.border));
////                negbutton.setWidth(width);
//                neutbutton.setBackground(getContext().getDrawable(R.drawable.border));
////                neutbutton.setWidth(width);
//            }
        } catch(Exception ex){
            //ignore it
        }
        return alertDialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mListener != null) {
            mListener.onCancel();
            dialog.dismiss();
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
        void onEmailInvites();
        void onInviteUsers();
        void onCancel();
    }
}
