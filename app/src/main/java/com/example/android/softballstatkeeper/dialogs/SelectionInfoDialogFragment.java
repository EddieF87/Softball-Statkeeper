package com.example.android.softballstatkeeper.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.activities.UserSettingsActivity;
import com.example.android.softballstatkeeper.objects.MainPageSelection;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectionInfoDialogFragment extends DialogFragment {

    private MainPageSelection mSelection;
    private int position;
    private SelectionInfoDialogFragment.OnFragmentInteractionListener mListener;

    public static SelectionInfoDialogFragment newInstance(MainPageSelection selection, int pos) {

        Bundle args = new Bundle();
        SelectionInfoDialogFragment fragment = new SelectionInfoDialogFragment();
        args.putParcelable("mSelection", selection);
        args.putInt("position", pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        position = args.getInt("position");
        mSelection = args.getParcelable("mSelection");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_selection_info_dialog, null);
        TextView nameView = v.findViewById(R.id.name);
        TextView typeView = v.findViewById(R.id.type);
        TextView levelView = v.findViewById(R.id.level);

        nameView.setText(mSelection.getName());

        switch (mSelection.getType()) {
            case MainPageSelection.TYPE_LEAGUE:
                typeView.setText("League");
                break;

            case MainPageSelection.TYPE_TEAM:
                typeView.setText("Team");
                break;

            case MainPageSelection.TYPE_PLAYER:
                typeView.setText("Player");
                break;
        }

        switch (mSelection.getLevel()) {
            case UserSettingsActivity.LEVEL_VIEW_ONLY:
                levelView.setText("View Only");
                break;

            case UserSettingsActivity.LEVEL_VIEW_WRITE:
                levelView.setText("View/Manage");
                break;

            case UserSettingsActivity.LEVEL_ADMIN:
                levelView.setText("Admin");
                break;

            case UserSettingsActivity.LEVEL_CREATOR:
                levelView.setText("Creator");
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onDelete(mSelection, position);
                        }
                    }
                })
                .setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SelectionInfoDialogFragment.OnFragmentInteractionListener) {
            mListener = (SelectionInfoDialogFragment.OnFragmentInteractionListener) context;
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
        void onDelete(MainPageSelection selection, int pos);
    }
}
