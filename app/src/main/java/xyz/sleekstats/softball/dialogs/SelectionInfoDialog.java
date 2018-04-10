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
import android.widget.TextView;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.activities.UsersActivity;
import xyz.sleekstats.softball.objects.MainPageSelection;

public class SelectionInfoDialog extends DialogFragment {

    private MainPageSelection mSelection;
    private SelectionInfoDialog.OnFragmentInteractionListener mListener;

    public static SelectionInfoDialog newInstance(MainPageSelection selection) {

        Bundle args = new Bundle();
        SelectionInfoDialog fragment = new SelectionInfoDialog();
        args.putParcelable("mSelection", selection);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mSelection = args.getParcelable("mSelection");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_selection_info_dialog, null);
        TextView nameView = v.findViewById(R.id.name);
        TextView typeView = v.findViewById(R.id.type);
        TextView levelView = v.findViewById(R.id.level);

        nameView.setText(mSelection.getName());

        switch (mSelection.getType()) {
            case MainPageSelection.TYPE_LEAGUE:
                typeView.setText(R.string.league);
                break;

            case MainPageSelection.TYPE_TEAM:
                typeView.setText(R.string.team);
                break;

            case MainPageSelection.TYPE_PLAYER:
                typeView.setText(R.string.player);
                break;
        }

        switch (mSelection.getLevel()) {
            case UsersActivity.LEVEL_VIEW_ONLY:
                levelView.setText(R.string.view_only);
                break;

            case UsersActivity.LEVEL_VIEW_WRITE:
                levelView.setText(R.string.view_manage);
                break;

            case UsersActivity.LEVEL_ADMIN:
                levelView.setText(R.string.admin);
                break;

            case UsersActivity.LEVEL_CREATOR:
                levelView.setText(R.string.creator);
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onDelete(mSelection);
                        }
                    }
                })
                .setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SelectionInfoDialog.OnFragmentInteractionListener) {
            mListener = (SelectionInfoDialog.OnFragmentInteractionListener) context;
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
        void onDelete(MainPageSelection selection);
    }
}
