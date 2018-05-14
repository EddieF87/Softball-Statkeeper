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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.MatchupAdapter;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.models.Player;

public class PreviewSortDialog extends DialogFragment {

    private OnFragmentInteractionListener mListener;
    private List<Player> mAwayLineup;
    private List<Player> mHomeLineup;

    public PreviewSortDialog() {
        // Required empty public constructor
    }

    public static PreviewSortDialog newInstance(ArrayList<Player> awayLineup, ArrayList<Player> homeLineup) {

        Bundle args = new Bundle();
        PreviewSortDialog fragment = new PreviewSortDialog();
        args.putParcelableArrayList(StatsContract.StatsEntry.COLUMN_AWAY_TEAM, awayLineup);
        args.putParcelableArrayList(StatsContract.StatsEntry.COLUMN_HOME_TEAM, homeLineup);
        fragment.setArguments(args);
        return fragment;
    }

    public static PreviewSortDialog newInstance(ArrayList<Player> awayLineup) {

        Bundle args = new Bundle();
        PreviewSortDialog fragment = new PreviewSortDialog();
        args.putParcelableArrayList(StatsContract.StatsEntry.COLUMN_AWAY_TEAM, awayLineup);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mAwayLineup = args.getParcelableArrayList(StatsContract.StatsEntry.COLUMN_AWAY_TEAM);
        mHomeLineup = args.getParcelableArrayList(StatsContract.StatsEntry.COLUMN_HOME_TEAM);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_sortpreview, null);
        RecyclerView rvAway = view.findViewById(R.id.away_rv);
        rvAway.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvAway.setAdapter(new MatchupAdapter(mAwayLineup, getActivity(), 1));

        RecyclerView rvHome = view.findViewById(R.id.home_rv);
        if(mHomeLineup != null) {
            rvHome.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            rvHome.setAdapter(new MatchupAdapter(mHomeLineup, getActivity(), 1));
        } else {
            rvHome.setVisibility(View.GONE);
        }


        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.preview)
                .setView(view)
                .setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onReturnToSort();
                        }
                    }
                })
                .create();
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
        void onReturnToSort();
    }
}
