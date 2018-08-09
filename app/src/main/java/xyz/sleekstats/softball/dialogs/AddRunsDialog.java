package xyz.sleekstats.softball.dialogs;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import xyz.sleekstats.softball.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddRunsDialog extends DialogFragment implements View.OnClickListener {

    private static final String KEY_AWAY_RUNS = "awayruns";
    private static final String KEY_HOME_RUNS = "homeruns";
    private int awayRuns;
    private int homeRuns;
    private TextView awayRunsText;
    private TextView homeRunsText;
    private AddRunsDialog.OnFragmentInteractionListener mListener;



    public AddRunsDialog() {
        // Required empty public constructor
    }

    public static AddRunsDialog newInstance(int away, int home) {
        AddRunsDialog fragment = new AddRunsDialog();
        Bundle args = new Bundle();
        args.putInt(KEY_AWAY_RUNS, away);
        args.putInt(KEY_HOME_RUNS, home);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            awayRuns = getArguments().getInt(KEY_AWAY_RUNS);
            homeRuns = getArguments().getInt(KEY_HOME_RUNS);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_AWAY_RUNS, awayRuns);
        outState.putInt(KEY_HOME_RUNS, homeRuns);
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") final  View view=inflater.inflate(R.layout.dialog_add_runs, null);

        Context context = view.getContext();
        view.findViewById(R.id.btn_remove_away).setOnClickListener(this);
        view.findViewById(R.id.btn_remove_home).setOnClickListener(this);
        view.findViewById(R.id.btn_add_away).setOnClickListener(this);
        view.findViewById(R.id.btn_add_home).setOnClickListener(this);
        awayRunsText = view.findViewById(R.id.away_runs_text);
        homeRunsText = view.findViewById(R.id.home_runs_text);
        setRunsText(awayRuns, homeRuns);


        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.MyAlertDialog)
                .setView(view)
                .setTitle(R.string.edit_score)
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
            mListener.onChangeRuns(awayRuns, homeRuns);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AddRunsDialog.OnFragmentInteractionListener) {
            mListener = (AddRunsDialog.OnFragmentInteractionListener) context;
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
            case R.id.btn_remove_away:
                awayRuns--;
                if(awayRuns < 0) {
                    awayRuns = 0;
                    return;
                }
                break;
            case R.id.btn_add_away:
                awayRuns++;
                break;
            case R.id.btn_remove_home:
                homeRuns--;
                if(homeRuns < 0) {
                    homeRuns = 0;
                    return;
                }
                break;
            case R.id.btn_add_home:
                homeRuns++;
                break;
            default:
                Toast.makeText(getContext(), "click ERROR", Toast.LENGTH_LONG).show();
        }
        setRunsText(awayRuns, homeRuns);
    }

    private void setRunsText(int awayR, int homeR) {
        awayRunsText.setText(String.valueOf(awayR));
        homeRunsText.setText(String.valueOf(homeR));
    }
    
    public interface OnFragmentInteractionListener {
        void onChangeRuns(int awayR, int homeR);
    }
}
