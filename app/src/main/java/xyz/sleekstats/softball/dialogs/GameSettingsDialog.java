package xyz.sleekstats.softball.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.StatsContract;


public class GameSettingsDialog extends DialogFragment {

    private OnFragmentInteractionListener mListener;
    private String mSelectionID;
    private int genderSorter;
    private int innings;
    private int inningNumber;
    private int mercyRuns;
    private TextView mGenderDisplay;
    private TextView mInningDisplay;
    private TextView mMercyDisplay;
    private boolean gameHelp;
    private boolean extraGirlsSorted;

    public GameSettingsDialog() {
        // Required empty public constructor
    }

    public static GameSettingsDialog newInstance(int innings, int genderSortArg, int mercy, String selectionID, int currentInning, boolean helpArg, boolean extraGirlsSortedArg) {

        Bundle args = new Bundle();
        GameSettingsDialog fragment = new GameSettingsDialog();
        args.putInt(StatsContract.StatsEntry.INNINGS, innings);
        args.putInt(StatsContract.StatsEntry.COLUMN_GENDER, genderSortArg);
        args.putBoolean(StatsContract.StatsEntry.HELP, helpArg);
        args.putBoolean(StatsContract.StatsEntry.SORT_GIRLS, extraGirlsSortedArg);
        args.putString("mSelectionID", selectionID);
        args.putInt("inningNumber", currentInning);
        args.putInt(StatsContract.StatsEntry.MERCY, mercy);
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
            inningNumber = args.getInt("inningNumber") / 2;
            mercyRuns = args.getInt(StatsContract.StatsEntry.MERCY, 99);
            gameHelp = args.getBoolean(StatsContract.StatsEntry.HELP);
            extraGirlsSorted = args.getBoolean(StatsContract.StatsEntry.SORT_GIRLS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_game_settings, null);

        final SeekBar inningSeekBar = v.findViewById(R.id.innings_seekbar);
        mInningDisplay = v.findViewById(R.id.innings_textview);
        mInningDisplay.setText(String.format(getString(R.string.Innings), String.valueOf(innings)));
        inningSeekBar.setProgress(innings - 1);

        if(inningNumber > 0) {
            v.findViewById(R.id.gender_sort_seekbar).setVisibility(View.GONE);
            v.findViewById(R.id.gender_sort_title).setVisibility(View.GONE);
            v.findViewById(R.id.sort_girls_button).setVisibility(View.GONE);
            v.findViewById(R.id.toggle_help).setVisibility(View.GONE);
        }


        inningSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mInningDisplay.setText(String.format(getString(R.string.Innings),String.valueOf(i + 1)));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        final SeekBar genderSeekBar = v.findViewById(R.id.gender_sort_seekbar);
        mGenderDisplay = v.findViewById(R.id.gender_sort_title);
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

        ToggleButton helpToggleButton = v.findViewById(R.id.toggle_help);
        helpToggleButton.setChecked(gameHelp);
        helpToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                gameHelp = checked;
            }
        });

        ToggleButton extraGirlsToggleButton = v.findViewById(R.id.sort_girls_button);
        extraGirlsToggleButton.setChecked(extraGirlsSorted);
        extraGirlsToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                extraGirlsSorted = checked;
            }
        });

        final SeekBar mercySeekBar = v.findViewById(R.id.mercy_seekbar);
        mMercyDisplay = v.findViewById(R.id.mercy_textview);
        String runs;
        if(mercyRuns == 99) {
            runs = "Off";
            mercySeekBar.setProgress(0);
        } else {
            runs = mercyRuns + " Runs";
            mercySeekBar.setProgress(mercyRuns - 4);
        }
        String mercyString = String.format(getString(R.string.mercy_rule), runs);
        mMercyDisplay.setText(mercyString);
        mercySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String runs;
                if(i == 0 || i == 12) {
                    runs = "Off";
                } else {
                    runs = String.valueOf(i + 4) + " Runs";
                }
                String mercyString = String.format(getString(R.string.mercy_rule), runs);
                mMercyDisplay.setText(mercyString);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.game_settings)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

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

        Button submitButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                innings = inningSeekBar.getProgress() + 1;
                if(innings < inningNumber) {
                    Toast.makeText(getContext(), "It's already Inning " + inningNumber, Toast.LENGTH_SHORT).show();
                    return;
                }
                genderSorter = genderSeekBar.getProgress();
                mercyRuns = mercySeekBar.getProgress() + 4;
                if(mercyRuns == 4 || mercyRuns == 16) {
                    mercyRuns = 99;
                }
                onButtonPressed();
                alertDialog.dismiss();
            }
        });
        return alertDialog;
    }

    private void setDisplay(int i) {
        String displayString;
        if (i == 0) {
            displayString = getString(R.string.set_gender_lineup) + " OFF";

            mGenderDisplay.setText(displayString);
            mGenderDisplay.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            return;
        }
//        mGenderDisplay.setTextColor(ContextCompat.getColor(getContext(), R.color.male));
        StringBuilder stringBuilder = new StringBuilder();
        String boyColor = Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.colorM) & 0x00ffffff);
        String girlColor = Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.colorF) & 0x00ffffff);
//        stringBuilder.append(getString(R.string.set_gender_lineup));
        stringBuilder.append("<font color='#").append(boyColor).append("'>");
        for (int index = 0; index < i; index++) {
            stringBuilder.append("M");
        }
        stringBuilder.append("</font>");

        String boy = stringBuilder.toString();
        String girl = "<font color='#" + girlColor + "'>F</font>";
        String order = boy + girl;
        order += order;
        displayString = getString(R.string.set_gender_lineup) + " " + order;
        mGenderDisplay.setText(Html.fromHtml(displayString));
    }


    private void onButtonPressed() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(mSelectionID + StatsContract.StatsEntry.SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(StatsContract.StatsEntry.INNINGS, innings);
        editor.putInt(StatsContract.StatsEntry.COLUMN_GENDER, genderSorter);
        editor.putInt(StatsContract.StatsEntry.MERCY, mercyRuns);
        editor.putBoolean(StatsContract.StatsEntry.HELP, gameHelp);
        editor.putBoolean(StatsContract.StatsEntry.SORT_GIRLS, extraGirlsSorted);
        editor.apply();
        if (mListener != null) {
            mListener.onGameSettingsChanged(innings, genderSorter, mercyRuns);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

    public interface OnFragmentInteractionListener {
        void onGameSettingsChanged(int innings, int genderSorter, int mercy);
    }
}
