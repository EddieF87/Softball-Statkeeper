package xyz.sleekstats.softball.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.EmailInviteRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmailInviteDialog extends DialogFragment {


    private EmailInviteRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private OnListFragmentInteractionListener mListener;
    private static final String KEY_EMAILS = "emails";
    private static final String KEY_LEVELS = "levels";
    private static final String KEY_EDITS = "edits";

    public EmailInviteDialog() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            List<Integer> edits = savedInstanceState.getIntegerArrayList(KEY_EDITS);
            List<Integer> accessLevels = savedInstanceState.getIntegerArrayList(KEY_LEVELS);
            List<String> emails = savedInstanceState.getStringArrayList(KEY_EMAILS);
            mAdapter = new EmailInviteRecyclerViewAdapter(emails, accessLevels, edits);
        } else {
            mAdapter = new EmailInviteRecyclerViewAdapter();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.rv_list, null);
        Context context = view.getContext();
        mRecyclerView = (RecyclerView) view;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(mAdapter);

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(mRecyclerView)
                .setTitle("Add user emails to invite")
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        View view = getActivity().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        }
                        onButtonPressed();
                        disableViewHolderEditTexts();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onCancel();
                        }
                        if (dialog != null) {
                            disableViewHolderEditTexts();
                            dialog.dismiss();
                        }
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return alertDialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mListener != null) {
            mListener.onCancel();
        }
        disableViewHolderEditTexts();
    }

    private void onButtonPressed() {
        if (mListener != null) {
            ArrayList<String> emails = new ArrayList<>(mAdapter.getEmails());
            ArrayList<Integer> levels = new ArrayList<>(mAdapter.getAccessLevels());
            mListener.onSubmitEmails(emails, levels);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> emails = new ArrayList<>(mAdapter.getEmails());
        ArrayList<Integer> levels = new ArrayList<>(mAdapter.getAccessLevels());
        HashSet<Integer> edits = mAdapter.getEdits();
        outState.putStringArrayList(KEY_EMAILS, emails);
        outState.putIntegerArrayList(KEY_LEVELS, new ArrayList<>(levels));
        outState.putIntegerArrayList(KEY_EDITS, new ArrayList<>(edits));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mRecyclerView.setAdapter(null);
        mAdapter = null;
        mRecyclerView = null;
    }

    private void disableViewHolderEditTexts(){
        for (int childCount = mRecyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            final RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(mRecyclerView.getChildAt(i));
            mAdapter.disableEditTextCursor(holder);
        }
    }

    public interface OnListFragmentInteractionListener {
        void onSubmitEmails(List<String> emails, List<Integer> levels);
        void onCancel();
    }

}
