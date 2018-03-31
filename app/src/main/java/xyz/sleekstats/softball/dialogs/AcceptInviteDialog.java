package xyz.sleekstats.softball.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.StatsContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class AcceptInviteDialog extends DialogFragment {

    private OnFragmentInteractionListener mListener;
    private int mType;
    private int mLevel;
    private String mID;
    private String mName;

    public AcceptInviteDialog() {
        // Required empty public constructor
    }

    public static AcceptInviteDialog newInstance(String id, String name, int type, int level) {

        Bundle args = new Bundle();
        args.putString(StatsContract.StatsEntry._ID, id);
        args.putString(StatsContract.StatsEntry.COLUMN_NAME, name);
        args.putInt(StatsContract.StatsEntry.TYPE, type);
        args.putInt(StatsContract.StatsEntry.LEVEL, level);
        AcceptInviteDialog fragment = new AcceptInviteDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null) {
            mID = args.getString(StatsContract.StatsEntry._ID);
            mName = args.getString(StatsContract.StatsEntry.COLUMN_NAME);
            mType = args.getInt(StatsContract.StatsEntry.TYPE);
            mLevel = args.getInt(StatsContract.StatsEntry.LEVEL);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Statkeeper Invite!")
                .setMessage("You're invited to view stats for " + mName + "!")
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onAcceptInvite(true, mID, mName, mType, mLevel);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onAcceptInvite(false, mID, mName, mType, mLevel);
                        }
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        return alertDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onAcceptInvite(boolean accepted, String id, String name, int type, int level);
    }
}
