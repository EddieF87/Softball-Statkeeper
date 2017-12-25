package com.example.android.scorekeepdraft1.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.adapters_listeners_etc.UserListAdapter;
import com.example.android.scorekeepdraft1.objects.StatKeepUser;

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
    private static final String TAG = "UserFragment";
    private List<StatKeepUser> mUserList;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;

    public UserFragment() {
    }

    public static UserFragment newInstance(List<StatKeepUser> users) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_LIST, (ArrayList<? extends Parcelable>) users);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "hoppy userfragment created");

        setRetainInstance(true);
        if (getArguments() != null) {
            List<StatKeepUser> users = getArguments().getParcelableArrayList(ARG_LIST);
            mUserList = cloneList(users);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        recyclerView = view.findViewById(R.id.list);
        recyclerView.setAdapter(new UserListAdapter(mUserList, getActivity(), mListener));

        return view;
    }

    public void swapList(List<StatKeepUser> list) {
        mUserList = cloneList(list);
        recyclerView.setAdapter(new UserListAdapter(mUserList, getActivity(), mListener));
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
