package com.example.android.softballstatkeeper.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.adapters.UserListAdapter;
import com.example.android.softballstatkeeper.data.StatsContract;
import com.example.android.softballstatkeeper.models.StatKeepUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class UserFragment extends Fragment {

    private static final String ARG_LIST = "list";
    private List<StatKeepUser> mUserList;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private int mLevel;

    public UserFragment() {
    }

    public static UserFragment newInstance(List<StatKeepUser> users, int level) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_LIST, (ArrayList<? extends Parcelable>) users);
        args.putInt(StatsContract.StatsEntry.LEVEL, level);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle args = getArguments();
        if (args != null) {
            mLevel = args.getInt(StatsContract.StatsEntry.LEVEL);
            List<StatKeepUser> users = args.getParcelableArrayList(ARG_LIST);
            mUserList = cloneList(users);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        mRecyclerView = view.findViewById(R.id.list);
        mRecyclerView.setAdapter(new UserListAdapter(mUserList, getActivity(), mListener, mLevel));

        return view;
    }

    public void swapList(List<StatKeepUser> list) {
        mUserList = cloneList(list);
        mRecyclerView.setAdapter(new UserListAdapter(mUserList, getActivity(), mListener, mLevel));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    private List<StatKeepUser> cloneList(List<StatKeepUser> list) {
        List<StatKeepUser> clonedList =  new ArrayList<>();
        for (StatKeepUser user: list) {
            String id = user.getId();
            String name = user.getName();
            String email = user.getEmail();
            int level = user.getLevel();

            clonedList.add(new StatKeepUser(id, name, email, level));
        }
        return clonedList;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnListFragmentInteractionListener {
        void onUserLevelChanged(String name, int level);
    }
}
