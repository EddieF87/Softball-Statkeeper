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

/**
 * Created by Eddie on 3/18/2018.
 */

public class EnterCodeDialog extends DialogFragment {

    private EnterCodeDialog.OnFragmentInteractionListener mListener;
    private MyEditText mCodeText;
    private MyEditText mIDText;
    private int mType;

    public EnterCodeDialog() {
        // Required empty public constructor
    }

    public static EnterCodeDialog newInstance(int type) {
        Bundle args = new Bundle();
        EnterCodeDialog fragment = new EnterCodeDialog();
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
            mType = args.getInt("mType");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_enter_code, null);
        mIDText = view.findViewById(R.id.edit_id);
        mCodeText = view.findViewById(R.id.edit_code);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Enter StatKeeper ID and Code");
        builder.setView(view)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String idText = mIDText.getText().toString();
                        String codeText = mCodeText.getText().toString();
                        mIDText.setCursorVisible(false);
                        mCodeText.setCursorVisible(false);
                        dialog.dismiss();
                        if (mListener != null) {
                            mListener.onSubmitCode(idText, codeText, mType);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCodeText.setCursorVisible(false);
                        mIDText.setCursorVisible(false);
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        Log.d("aaa", "onAttach dialog");
        super.onAttach(context);
        if (context instanceof EnterCodeDialog.OnFragmentInteractionListener) {
            mListener = (EnterCodeDialog.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mIDText.setCursorVisible(false);
        mCodeText.setCursorVisible(false);
        dialog.dismiss();
        super.onCancel(dialog);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("aaa", "onDetach dialog");
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onSubmitCode(String idText, String codeText, int type);
    }

    @Override
    public void onDestroy() {
        mIDText = null;
        mCodeText = null;
        Log.d("aaa", "onDestroy dialog");
        super.onDestroy();
        RefWatcher refWatcher = MyApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}
