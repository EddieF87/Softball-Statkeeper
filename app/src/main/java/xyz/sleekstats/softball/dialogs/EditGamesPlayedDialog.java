package xyz.sleekstats.softball.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.StatsContract;

public class EditGamesPlayedDialog extends DialogFragment {

    private EditGamesPlayedDialog.OnFragmentInteractionListener mListener;
    private String mPlayerName;
    private String mPlayerFirestoreID;
    private int mPlayerGamesPlayed;

    public EditGamesPlayedDialog() {
        // Required empty public constructor
    }

    public static EditGamesPlayedDialog newInstance(String name, String id, int games) {

        Bundle args = new Bundle();
        EditGamesPlayedDialog fragment = new EditGamesPlayedDialog();
        args.putString(StatsContract.StatsEntry.COLUMN_NAME, name);
        args.putString(StatsContract.StatsEntry.COLUMN_FIRESTORE_ID, id);
        args.putInt(StatsContract.StatsEntry.COLUMN_G, games);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mPlayerName = args.getString(StatsContract.StatsEntry.COLUMN_NAME);
            mPlayerFirestoreID = args.getString(StatsContract.StatsEntry.COLUMN_FIRESTORE_ID);
            if (savedInstanceState != null) {
                mPlayerGamesPlayed = savedInstanceState.getInt(StatsContract.StatsEntry.COLUMN_G);
            } else {
                mPlayerGamesPlayed = args.getInt(StatsContract.StatsEntry.COLUMN_G);
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.dialog_edit_gamesplayed, null);
        Context context = view.getContext();

        ImageView addView = view.findViewById(R.id.btn_add_gp);
        ImageView subView = view.findViewById(R.id.btn_subtract_gp);
        final TextView gamesView = view.findViewById(R.id.textview_gp_count);

        gamesView.setText(String.valueOf(mPlayerGamesPlayed));
        addView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayerGamesPlayed++;
                gamesView.setText(String.valueOf(mPlayerGamesPlayed));
            }
        });
        subView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayerGamesPlayed--;
                gamesView.setText(String.valueOf(mPlayerGamesPlayed));
            }
        });

        String title = "Edit Games Played for " + mPlayerName;

        return new AlertDialog.Builder(context, R.style.MyAlertDialog)
                .setView(view)
                .setTitle(title)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onUpdateGamesPlayed(mPlayerFirestoreID, mPlayerGamesPlayed);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EditGamesPlayedDialog.OnFragmentInteractionListener) {
            mListener = (EditGamesPlayedDialog.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLineupSortListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(StatsContract.StatsEntry.COLUMN_G, mPlayerGamesPlayed);
        super.onSaveInstanceState(outState);
    }

    public interface OnFragmentInteractionListener {
        void onUpdateGamesPlayed(String playerFirestoreID, int games);
    }
}
