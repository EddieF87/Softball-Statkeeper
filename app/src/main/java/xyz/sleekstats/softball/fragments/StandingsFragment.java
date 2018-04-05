package xyz.sleekstats.softball.fragments;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.activities.GameRecapListActivity;
import xyz.sleekstats.softball.activities.UsersActivity;
import xyz.sleekstats.softball.activities.TeamPagerActivity;
import xyz.sleekstats.softball.adapters.StandingsAdapter;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.objects.MainPageSelection;
import xyz.sleekstats.softball.objects.Team;

import java.util.ArrayList;
import java.util.Collections;

public class StandingsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private static final int STANDINGS_LOADER = 3;
    private int level;
    private StandingsAdapter mAdapter;
    private ArrayList<Team> mTeams;
    private TextView colorView;
    private Button startAdderButton;
    private RecyclerView standingsRV;


    public StandingsFragment() {
        // Required empty public constructor
    }

    public static StandingsFragment newInstance(int level, String name) {
        Bundle args = new Bundle();
        StandingsFragment fragment = new StandingsFragment();
        args.putInt(MainPageSelection.KEY_SELECTION_LEVEL, level);
        args.putString(MainPageSelection.KEY_SELECTION_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            level = args.getInt(MainPageSelection.KEY_SELECTION_LEVEL);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_standings, container, false);

        Button waiversButton = rootView.findViewById(R.id.btn_waivers);
        waiversButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TeamPagerActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        rootView.findViewById(R.id.standings_name_title).setOnClickListener(this);
        rootView.findViewById(R.id.win_title).setOnClickListener(this);
        rootView.findViewById(R.id.loss_title).setOnClickListener(this);
        rootView.findViewById(R.id.tie_title).setOnClickListener(this);
        rootView.findViewById(R.id.winpct_title).setOnClickListener(this);
        rootView.findViewById(R.id.runsfor_title).setOnClickListener(this);
        rootView.findViewById(R.id.runsagainst_title).setOnClickListener(this);
        rootView.findViewById(R.id.rundiff_title).setOnClickListener(this);
        standingsRV = rootView.findViewById(R.id.rv_standings);
        startAdderButton = rootView.findViewById(R.id.item_team_adder);

        if(level < UsersActivity.LEVEL_VIEW_WRITE) {
            startAdderButton.setVisibility(View.INVISIBLE);
        } else {
            startAdderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startAdderButton.setVisibility(View.INVISIBLE);
                    Collections.sort(mTeams, Team.nameComparator());
                    ArrayList<Team> teamsCopy = new ArrayList<>(mTeams);
                    if(mListener != null) {
                        mListener.startAdder(teamsCopy);
                    }
                }
            });
        }
        Button gamesButton = rootView.findViewById(R.id.btn_games);
        gamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GameRecapListActivity.class);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(STANDINGS_LOADER, null, this);
        return rootView;
    }

    private void updateStandingsRV() {
        if (mAdapter == null) {
            standingsRV.setLayoutManager(new LinearLayoutManager(
                    getActivity(), LinearLayoutManager.VERTICAL, false));
            mAdapter = new StandingsAdapter(mTeams, getActivity());
            standingsRV.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setRVVisibility(boolean visible) {
        View emptyView = getView().findViewById(R.id.empty_text);
        if(visible) {
            emptyView.setVisibility(View.GONE);
            standingsRV.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            standingsRV.setVisibility(View.GONE);
        }
    }

    public void setAdderButtonVisible() {
        if (startAdderButton != null) {
            startAdderButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                StatsEntry.CONTENT_URI_TEAMS,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(mTeams == null) {
            mTeams = new ArrayList<>();
        } else {
            mTeams.clear();
        }
        data.moveToPosition(-1);
        while (data.moveToNext()) {
            mTeams.add(new Team(data));
        }
        if(mTeams.isEmpty()) {
            setRVVisibility(false);
        } else {
            setRVVisibility(true);
            updateStandingsRV();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onClick(View view) {
        if (colorView != null) {
            colorView.setTextColor(Color.WHITE);
        }
        colorView = getView().findViewById(view.getId());
        colorView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));

        switch (view.getId()) {
            case R.id.standings_name_title:
                Collections.sort(mTeams, Team.nameComparator());
                break;
            case R.id.win_title:
                Collections.sort(mTeams, Team.winComparator());
                break;
            case R.id.loss_title:
                Collections.sort(mTeams, Team.lossComparator());
                break;
            case R.id.tie_title:
                Collections.sort(mTeams, Team.tieComparator());
                break;
            case R.id.winpct_title:
                Collections.sort(mTeams, Team.winpctComparator());
                break;
            case R.id.runsfor_title:
                Collections.sort(mTeams, Team.runsComparator());
                break;
            case R.id.runsagainst_title:
                Collections.sort(mTeams, Team.runsAllowedComparator());
                break;
            case R.id.rundiff_title:
                Collections.sort(mTeams, Team.runDiffComparator());
                break;
            default:
                Toast.makeText(getActivity(), "SOMETHIGN WRONG WITH onClick", Toast.LENGTH_LONG).show();
        }
        updateStandingsRV();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StandingsFragment.OnFragmentInteractionListener) {
            mListener = (StandingsFragment.OnFragmentInteractionListener) context;
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
        void startAdder(ArrayList<Team> teams);
    }
}
