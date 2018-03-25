package com.example.android.softballstatkeeper.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.example.android.softballstatkeeper.MyApp;
import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.views.MyEditText;
import com.squareup.leakcanary.RefWatcher;

public class EditNameDialog extends DialogFragment {

    private EditNameDialog.OnFragmentInteractionListener mListener;
    private String mTitle;
    private MyEditText mEditText;
    private int mType;

    public EditNameDialog() {
        // Required empty public constructor
    }

    public static EditNameDialog newInstance(String title) {

        Bundle args = new Bundle();
        EditNameDialog fragment = new EditNameDialog();
        args.putString("mTitle", title);
        fragment.setArguments(args);
        return fragment;
    }

    public static EditNameDialog newInstance(String title, int type) {

        Bundle args = new Bundle();
        EditNameDialog fragment = new EditNameDialog();
        args.putString("mTitle", title);
        args.putInt("mType", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("aaa", "onJoinOrCreate dialog");
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mTitle = args.getString("mTitle");
            mType = args.getInt("mType");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_name, null);
        mEditText = view.findViewById(R.id.username);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(mTitle);
        builder.setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String enteredText = mEditText.getText().toString().trim();
                        mEditText.setCursorVisible(false);
                        dialog.dismiss();
                        onButtonPressed(enteredText);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mType = -1;
                        mEditText.setCursorVisible(false);
                        dialog.dismiss();
                        onButtonPressed("");
                    }
                });
        return builder.create();
    }


    private void onButtonPressed(String enteredText) {
        if (mListener != null) {
            mListener.onEdit(enteredText, mType);
        }
    }

    @Override
    public void onAttach(Context context) {
        Log.d("aaa", "onAttach dialog");
        super.onAttach(context);
        if (context instanceof EditNameDialog.OnFragmentInteractionListener) {
            mListener = (EditNameDialog.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.d("aaa", "onCancel dialog");
        mEditText.setCursorVisible(false);
        dialog.dismiss();
        onButtonPressed("");
        super.onCancel(dialog);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("aaa", "onDetach dialog");
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onEdit(String enteredText, int type);
    }

    @Override
    public void onDestroy() {
        mTitle = null;
        mEditText = null;
        Log.d("aaa", "onDestroy dialog");
        super.onDestroy();
        RefWatcher refWatcher = MyApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}
