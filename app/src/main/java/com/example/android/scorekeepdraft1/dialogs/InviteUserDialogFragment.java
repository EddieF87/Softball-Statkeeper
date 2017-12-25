package com.example.android.scorekeepdraft1.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.R;

import org.w3c.dom.Text;

public class InviteUserDialogFragment extends DialogFragment {

    private OnFragmentInteractionListener mListener;
    private EditText mEditText;
    private TextView mLevelDisplay;
    private SeekBar mSeekBar;

    public InviteUserDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog_invite_user, null);
        mEditText = view.findViewById(R.id.new_user_textview);
        mSeekBar = view.findViewById(R.id.new_user_level_seekbar);
        mLevelDisplay = view.findViewById(R.id.new_user_level_view);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String levelDisplay;
                switch (i) {
                    case 0:
                        levelDisplay = getString(R.string.view_only);
                        break;
                    case 1:
                        levelDisplay = getString(R.string.view_manage);
                        break;
                    case 2:
                        levelDisplay = getString(R.string.admin);
                        break;
                    default:
                        levelDisplay = "Error";
                }
                mLevelDisplay.setText(levelDisplay);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Invite Users")
                .setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String email = mEditText.getText().toString();
                        int level = mSeekBar.getProgress() + 2;

                        View view = getActivity().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        }
                        if (email.isEmpty()) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            return;
                        }
                        onButtonPressed(email, level);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return alertDialog;
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

    public void onButtonPressed(String email, int level) {
        if (mListener != null) {
            mListener.onInviteUser(email, level);
        }
    }

    public interface OnFragmentInteractionListener {
        void onInviteUser(String email, int level);
    }
}
