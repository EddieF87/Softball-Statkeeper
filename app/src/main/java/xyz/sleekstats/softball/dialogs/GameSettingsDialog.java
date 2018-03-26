package xyz.sleekstats.softball.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.StatsContract;


public class GameSettingsDialog extends DialogFragment {

    private OnFragmentInteractionListener mListener;
    private int genderSorter;
    private int innings;
    private String mSelectionID;
    private TextView mGenderDisplay;
    private TextView mInningDisplay;

    public GameSettingsDialog() {
        // Required empty public constructor
    }

    public static GameSettingsDialog newInstance(int innings, int genderSortArg, String selectionID) {

        Bundle args = new Bundle();
        GameSettingsDialog fragment = new GameSettingsDialog();
        args.putInt(StatsContract.StatsEntry.INNINGS, innings);
        args.putInt(StatsContract.StatsEntry.COLUMN_GENDER, genderSortArg);
        args.putString("mSelectionID", selectionID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            innings = args.getInt(StatsContract.StatsEntry.INNINGS);
            genderSorter = args.getInt(StatsContract.StatsEntry.COLUMN_GENDER);
            mSelectionID = args.getString("mSelectionID");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_game_settings, null);

        final SeekBar inningSeekBar = v.findViewById(R.id.innings_seekbar);
        mInningDisplay = v.findViewById(R.id.innings_textview);
        mInningDisplay.setText(String.valueOf(innings));
        inningSeekBar.setProgress(innings - 1);
        inningSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mInningDisplay.setText(String.valueOf(i + 1));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        final SeekBar genderSeekBar = v.findViewById(R.id.gender_sort_seekbar);
        mGenderDisplay = v.findViewById(R.id.gender_sort_textview);
        genderSeekBar.setProgress(genderSorter);
        setDisplay(genderSorter);
        genderSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setDisplay(i);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.game_settings)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        innings = inningSeekBar.getProgress() + 1;
                        genderSorter = genderSeekBar.getProgress();
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
        return alertDialog;
    }

    private void setDisplay(int i) {
        if (i == 0) {
            mGenderDisplay.setText(R.string.OFF);
            mGenderDisplay.setTextColor(Color.BLACK);
            return;
        }
        mGenderDisplay.setTextColor(ContextCompat.getColor(getContext(), R.color.male));
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < i; index++) {
            stringBuilder.append("M");
        }
        String boy = stringBuilder.toString();
        String girl = "<font color='#f99da2'>F</font>";
        String order = boy + girl;
        order += order + order;
        mGenderDisplay.setText(Html.fromHtml(order));
    }


    private void onButtonPressed() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(mSelectionID + StatsContract.StatsEntry.SETTINGS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(StatsContract.StatsEntry.INNINGS, innings);
        editor.putInt(StatsContract.StatsEntry.COLUMN_GENDER, genderSorter);
        editor.apply();
        if (mListener != null) {
            mListener.onGameSettingsChanged(innings, genderSorter);
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
        void onGameSettingsChanged(int innings, int genderSorter);
    }
}
