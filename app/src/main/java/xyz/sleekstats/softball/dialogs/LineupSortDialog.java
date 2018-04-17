package xyz.sleekstats.softball.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.activities.GameActivity;
import xyz.sleekstats.softball.data.StatsContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class LineupSortDialog extends DialogFragment {
    private OnLineupSortListener mListener;
    private int mSortArg;
    private int mGenderSorter;
    private int mInnings;
    private String mAwayID;
    private String mHomeID;
    private int mSelectedItem;

    public LineupSortDialog() {
        // Required empty public constructor
    }

    public static LineupSortDialog newInstance(String awayID, String homeID, int innings, int sortArg, int femaleOrder) {

        Bundle args = new Bundle();
        LineupSortDialog fragment = new LineupSortDialog();
        args.putInt(GameActivity.KEY_GENDERSORT, sortArg);
        args.putString(StatsContract.StatsEntry.COLUMN_AWAY_TEAM, awayID);
        args.putString(StatsContract.StatsEntry.COLUMN_HOME_TEAM, homeID);
        args.putInt(GameActivity.KEY_TOTALINNINGS, innings);
        args.putInt(GameActivity.KEY_FEMALEORDER, femaleOrder);
        fragment.setArguments(args);
        return fragment;
    }

    public static LineupSortDialog newInstance(String awayID, int sortArg, int femaleOrder) {

        Bundle args = new Bundle();
        LineupSortDialog fragment = new LineupSortDialog();
        args.putInt(GameActivity.KEY_GENDERSORT, sortArg);
        args.putString(StatsContract.StatsEntry.COLUMN_AWAY_TEAM, awayID);
        args.putInt(GameActivity.KEY_FEMALEORDER, femaleOrder);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mSortArg = args.getInt(GameActivity.KEY_GENDERSORT, 0);
        mGenderSorter = args.getInt(GameActivity.KEY_FEMALEORDER, 0);
        mInnings = args.getInt(GameActivity.KEY_TOTALINNINGS, 0);
        mAwayID = args.getString(StatsContract.StatsEntry.COLUMN_AWAY_TEAM);
        mHomeID = args.getString(StatsContract.StatsEntry.COLUMN_HOME_TEAM);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        String message = "dddddddddd";
////
////        return new AlertDialog.Builder(getActivity())
////                .setTitle("Choose Lineup Sorting Method")
////                .setMessage(message)
////                .setNegativeButton(R.string.add_auto_outs, new DialogInterface.OnClickListener() {
////                    public void onClick(DialogInterface dialog, int id) {
////                        mSortArg = -mSortArg;
////                        onButtonPressed();
////                        if(dialog != null) {
////                            dialog.dismiss();
////                        }
////                    }
////                })
////                .setPositiveButton(R.string.auto_sort, new DialogInterface.OnClickListener() {
////                    public void onClick(DialogInterface dialog, int id) {
////                        onButtonPressed();
////                        if(dialog != null) {
////                            dialog.dismiss();
////                        }
////                    }
////                })
////                .setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
////                    public void onClick(DialogInterface dialog, int id) {
////                        if(dialog != null) {
////                            dialog.dismiss();
////                        }
////                    }
////                })
////                .create();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title

        final String[] strings = getResources().getStringArray(R.array.sortmethods);
        builder.setTitle("Choose Lineup Sorting Method")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(strings, 0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mSelectedItem = i;
                            }
                        })
                // Set the action buttons
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        switch (strings[mSelectedItem]) {
                            case "Ignore":
                                mSortArg = 0;
                                break;
                            case "Auto-Sort":

                                break;

                            case "Add Auto-Outs":

                                mSortArg = -mSortArg;
                                break;
                        }
                        onButtonPressed();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onCancelStart();
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                })
                .setNeutralButton(R.string.preview, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int prevArg;
                        switch (strings[mSelectedItem]) {
                            case "Ignore":
                                prevArg = 0;
                                break;
                            case "Auto-Sort":
                                prevArg = mSortArg;
                                break;

                            case "Add Auto-Outs":
                                prevArg = -mSortArg;
                                break;
                            default:
                                return;
                        }
                        if (mListener != null) {
                            mListener.onShowPreview(prevArg, mGenderSorter);
                        }
                    }
                })

        ;

        return builder.create();
    }


    private void onButtonPressed() {
        if (mListener != null) {
            mListener.onLineupSortChoice(mAwayID, mHomeID, mInnings, mSortArg, mGenderSorter);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLineupSortListener) {
            mListener = (OnLineupSortListener) context;
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


    public interface OnLineupSortListener {
        void onLineupSortChoice(String awayID, String homeID, int inningAmt, int sortArg, int femaleOrder);
        void onCancelStart();

        void onShowPreview(int sortArg, int femaleOrder);
    }
}
