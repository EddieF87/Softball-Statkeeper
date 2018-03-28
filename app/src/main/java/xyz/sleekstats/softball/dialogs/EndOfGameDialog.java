package xyz.sleekstats.softball.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import xyz.sleekstats.softball.R;

public class EndOfGameDialog extends DialogFragment {

    private int mHomeScore;
    private int mAwayScore;
    private String mHomeTeam;
    private String mAwayTeam;

    private OnFragmentInteractionListener mListener;

    public EndOfGameDialog() {
        // Required empty public constructor
    }

    public static EndOfGameDialog newInstance(String homeTeam, String awayTeam, int homeScore, int awayScore) {
        Bundle args = new Bundle();
        EndOfGameDialog fragment = new EndOfGameDialog();
        args.putString("homeTeam", homeTeam);
        args.putString("awayTeam", awayTeam);
        args.putInt("homeScore", homeScore);
        args.putInt("awayScore", awayScore);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mHomeTeam = args.getString("homeTeam");
            mAwayTeam = args.getString("awayTeam");
            mHomeScore = args.getInt("homeScore");
            mAwayScore = args.getInt("awayScore");
        }
    }

    private void onButtonPressed(boolean isOver) {
        if (mListener != null) {
            mListener.finishGame(isOver);
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
        void finishGame(boolean isOver);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String titleMessage;
        if(mHomeScore > mAwayScore) {
            titleMessage = mHomeTeam + " defeat " + mAwayTeam + " " + mHomeScore + " to " + mAwayScore + "!";
        } else if (mAwayScore > mHomeScore) {
            titleMessage = mAwayTeam + " defeat " + mHomeTeam + " " + mAwayScore + " to " + mHomeScore + "!";
        } else {
            titleMessage = mAwayTeam + " and " + mHomeTeam + " tie!  " + mAwayScore + " - " + mHomeScore;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(titleMessage)
                .setPositiveButton(R.string.end_msg, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(true);
                    }
                })
                .setNegativeButton(R.string.undo, new DialogInterface.OnClickListener() {
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
