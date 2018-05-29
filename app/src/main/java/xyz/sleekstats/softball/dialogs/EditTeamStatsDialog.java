package xyz.sleekstats.softball.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.StatsContract;

public class EditTeamStatsDialog extends DialogFragment implements View.OnClickListener {

    private static final String KEY_STATKEEPER_ID = "skID";
    private static final String KEY_TEAM_ID = "teamID";
    private static final String KEY_W = "win";
    private static final String KEY_L = "loss";
    private static final String KEY_T = "tie";
    private static final String KEY_RS = "runsscored";
    private static final String KEY_RA = "runsallowed";
    private String mStatKeeperID;
    private String mTeamID;
    private OnFragmentInteractionListener mListener;
    private TextView winView;
    private TextView winPlusView;
    private TextView lossView;
    private TextView lossPlusView;
    private TextView tieView;
    private TextView tiePlusView;
    private TextView rsView;
    private TextView rsPlusView;
    private TextView raView;
    private TextView raPlusView;
    private TextView wpctView;
    private TextView wpctPlusView;
    private TextView diffView;
    private TextView diffPlusView;
    private String mTeamName;
    private int totalWins;
    private int totalLosses;
    private int totalTies;
    private int totalRS;
    private int totalRA;
    private int addedWins;
    private int addedLosses;
    private int addedTies;
    private int addedRS;
    private int addedRA;
    private int colorPlus;
    private int colorMinus;
    private static final NumberFormat formatter = new DecimalFormat("#.000");

    public EditTeamStatsDialog() {
        // Required empty public constructor
    }

    public static EditTeamStatsDialog newInstance(String skID, String teamID) {
        EditTeamStatsDialog fragment = new EditTeamStatsDialog();
        Bundle args = new Bundle();
        args.putString(KEY_STATKEEPER_ID, skID);
        args.putString(KEY_TEAM_ID, teamID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStatKeeperID = getArguments().getString(KEY_STATKEEPER_ID);
            mTeamID = getArguments().getString(KEY_TEAM_ID);
        }
        String selection = StatsContract.StatsEntry.COLUMN_LEAGUE_ID + "=? AND " + StatsContract.StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = new String[]{mStatKeeperID, mTeamID};
        Cursor cursor = getActivity().getContentResolver().query(StatsContract.StatsEntry.CONTENT_URI_TEAMS,
                null, selection, selectionArgs, null);
        if(cursor.moveToFirst()) {
            mTeamName = StatsContract.getColumnString(cursor, StatsContract.StatsEntry.COLUMN_NAME);
            totalWins = StatsContract.getColumnInt(cursor, StatsContract.StatsEntry.COLUMN_WINS);
            totalLosses  = StatsContract.getColumnInt(cursor, StatsContract.StatsEntry.COLUMN_LOSSES);
            totalTies = StatsContract.getColumnInt(cursor, StatsContract.StatsEntry.COLUMN_TIES);
            totalRS = StatsContract.getColumnInt(cursor, StatsContract.StatsEntry.COLUMN_RUNSFOR);
            totalRA = StatsContract.getColumnInt(cursor, StatsContract.StatsEntry.COLUMN_RUNSAGAINST);
            if(savedInstanceState != null) {
                addedWins = savedInstanceState.getInt(KEY_W);
                addedLosses = savedInstanceState.getInt(KEY_L);
                addedTies = savedInstanceState.getInt(KEY_T);
                addedRS = savedInstanceState.getInt(KEY_RS);
                addedRA = savedInstanceState.getInt(KEY_RA);
                totalWins += addedWins;
                totalLosses += addedLosses;
                totalTies += addedTies;
                totalRS += addedRS;
                totalRA += addedRA;
            } else {
                addedWins = 0;
                addedLosses = 0;
                addedTies = 0;
                addedRS = 0;
                addedRA = 0;
            }
            colorPlus = ContextCompat.getColor(getContext(), R.color.colorPlus);
            colorMinus = ContextCompat.getColor(getContext(), R.color.colorMinus);
        } else {
            dismiss();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_W, addedWins);
        outState.putInt(KEY_L, addedLosses);
        outState.putInt(KEY_T, addedTies);
        outState.putInt(KEY_RS, addedRS);
        outState.putInt(KEY_RA, addedRA);
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") final  View view=inflater.inflate(R.layout.dialog_edit_teamstats, null);

        Context context = view.getContext();
        view.findViewById(R.id.addW).setOnClickListener(this);
        view.findViewById(R.id.addL).setOnClickListener(this);
        view.findViewById(R.id.addT).setOnClickListener(this);
        view.findViewById(R.id.addRS).setOnClickListener(this);
        view.findViewById(R.id.addRA).setOnClickListener(this);
        view.findViewById(R.id.subW).setOnClickListener(this);
        view.findViewById(R.id.subL).setOnClickListener(this);
        view.findViewById(R.id.subT).setOnClickListener(this);
        view.findViewById(R.id.subRS).setOnClickListener(this);
        view.findViewById(R.id.subRA).setOnClickListener(this);
        winView = view.findViewById(R.id.win_rbtotal);
        winPlusView = view.findViewById(R.id.win_rbplus);
        lossView = view.findViewById(R.id.loss_rbtotal);
        lossPlusView = view.findViewById(R.id.loss_rbplus);
        tieView = view.findViewById(R.id.tie_rbtotal);
        tiePlusView = view.findViewById(R.id.tie_rbplus);
        rsView = view.findViewById(R.id.rs_rbtotal);
        rsPlusView = view.findViewById(R.id.rs_rbplus);
        raView = view.findViewById(R.id.ra_rbtotal);
        raPlusView = view.findViewById(R.id.ra_rbplus);
        diffView = view.findViewById(R.id.rdiff_rbtotal);
        diffPlusView = view.findViewById(R.id.diff_rbplus);
        wpctView = view.findViewById(R.id.wpct_rbtotal);
        wpctPlusView = view.findViewById(R.id.wpct_rbplus);
        setWinText();
        setLossText();
        setTieText();
        setRSText();
        setRAText();

        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.MyAlertDialog)
                .setView(view)
                .setTitle(R.string.edit_team_stats)
                .setMessage(mTeamName)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed();
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

        return alertDialog;
    }

    private void onButtonPressed() {
        if (mListener != null) {
            mListener.onSaveTeamStatsUpdate(mTeamID, addedWins, addedLosses, addedTies, addedRS, addedRA);
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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.addW:
                addedWins++;
                totalWins++;
                setWinText();
                break;
            case R.id.subW:
                if(totalWins > 0) {
                    addedWins--;
                    totalWins--;
                    setWinText();
                }
                break;
            case R.id.addL:
                    addedLosses++;
                    totalLosses++;
                    setLossText();
                break;
            case R.id.subL:
                if(totalLosses > 0) {
                    addedLosses--;
                totalLosses--;
                setLossText();
                }
                break;
            case R.id.addT:
                addedTies++;
                totalTies++;
                setTieText();
                break;
            case R.id.subT:
                if(totalTies > 0) {
                    addedTies--;
                    totalTies--;
                    setTieText();
                }
                break;
            case R.id.addRS:
                addedRS++;
                totalRS++;
                setRSText();
                break;
            case R.id.subRS:
                if(totalRS > 0) {
                    addedRS--;
                    totalRS--;
                    setRSText();
                }
                break;
            case R.id.addRA:
                addedRA++;
                totalRA++;
                setRAText();
                break;
            case R.id.subRA:
                if(totalRA > 0) {
                    addedRA--;
                    totalRA--;
                    setRAText();
                }
                break;
            default:
                Toast.makeText(getContext(), "click ERROR", Toast.LENGTH_LONG).show();
        }
    }

    private void setWinText(){
        winView.setText(String.valueOf(totalWins));
        setWPctText();

        if(addedWins == 0) {
            winPlusView.setText("");
            return;
        }
        String added;

        if(addedWins>0) {
            winPlusView.setTextColor(colorPlus);
            added = "+" + addedWins;
        } else {
            winPlusView.setTextColor(colorMinus);
            added = String.valueOf(addedWins);
        }
        winPlusView.setText(added);
    }

    private void setLossText(){
        lossView.setText(String.valueOf(totalLosses));
        setWPctText();

        if(addedLosses == 0) {
            lossPlusView.setText("");
            return;
        }
        String added;
        if(addedLosses>0) {
            lossPlusView.setTextColor(colorPlus);
            added = "+" + addedLosses;
        } else {
            lossPlusView.setTextColor(colorMinus);
            added = String.valueOf(addedLosses);
        }
        lossPlusView.setText(added);
    }

    private void setTieText(){
        tieView.setText(String.valueOf(totalTies));

        if(addedTies == 0) {
            tiePlusView.setText("");
            return;
        }
        String added;
        if(addedTies>0) {
            tiePlusView.setTextColor(colorPlus);
            added = "+" + addedTies;
        } else {
            tiePlusView.setTextColor(colorMinus);
            added = String.valueOf(addedTies);
        }
        tiePlusView.setText(added);
    }

    private void setRSText(){
        rsView.setText(String.valueOf(totalRS));
        setRDiffText();

        if(addedRS == 0) {
            rsPlusView.setText("");
            return;
        }
        String added;
        if(addedRS>0) {
            rsPlusView.setTextColor(colorPlus);
            added = "+" + addedRS;
        } else {
            rsPlusView.setTextColor(colorMinus);
            added = String.valueOf(addedRS);
        }
        rsPlusView.setText(added);
    }

    private void setRAText(){
        raView.setText(String.valueOf(totalRA));
        setRDiffText();

        if(addedRA == 0) {
            raPlusView.setText("");
            return;
        }
        String added;
        if(addedRA>0) {
            rsPlusView.setTextColor(colorPlus);
            added = "+" + addedRA;
        } else {
            raPlusView.setTextColor(colorMinus);
            added = String.valueOf(addedRA);
        }
        raPlusView.setText(added);
    }

    private void setRDiffText(){
        int diff = totalRS - totalRA;
        diffView.setText(String.valueOf(diff));

        int addedDiff = addedRS - addedRA;
        if(addedDiff == 0){
            diffPlusView.setText("");
            return;
        }
        String addedDiffString;
        if(addedDiff > 0){
            addedDiffString = "+" + addedDiff;
            diffPlusView.setTextColor(colorPlus);
        } else {
            addedDiffString = String.valueOf(addedDiff);
            diffPlusView.setTextColor(colorMinus);
        }
        diffPlusView.setText(addedDiffString);
    }
    private void setWPctText(){
        double newWinPct;
        double oldWinPct;

        if(totalWins + totalLosses == 0) {
            newWinPct = .000;
        } else {
            newWinPct = ((double) totalWins) / (totalWins + totalLosses);
        }
        if(totalWins-addedWins + totalLosses-addedLosses == 0) {
            oldWinPct = .000;
        } else {
            oldWinPct = ((double)(totalWins - addedWins)) / (totalWins - addedWins + totalLosses - addedLosses);
        }
        wpctView.setText(String.valueOf(formatter.format(newWinPct)));


        if(newWinPct - oldWinPct == 0) {
            wpctPlusView.setText("");
            return;
        }
        String winPctPlus;
        if(newWinPct - oldWinPct > 0){
            winPctPlus = "+" + (formatter.format(newWinPct - oldWinPct));
            wpctPlusView.setTextColor(colorPlus);
        } else {
            winPctPlus = String.valueOf(formatter.format(newWinPct - oldWinPct));
            wpctPlusView.setTextColor(colorMinus);
        }
        wpctPlusView.setText(winPctPlus);
    }

    public interface OnFragmentInteractionListener {
        void onSaveTeamStatsUpdate(String teamID, int wins, int losses, int ties, int runsScored, int runsAllowed);
    }
}
