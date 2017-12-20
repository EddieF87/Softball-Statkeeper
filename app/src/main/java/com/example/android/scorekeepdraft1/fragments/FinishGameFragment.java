package com.example.android.scorekeepdraft1.fragments;

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

import com.example.android.scorekeepdraft1.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FinishGameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FinishGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FinishGameFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;


    private OnFragmentInteractionListener mListener;

    public FinishGameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
     * @return A new instance of fragment FinishGameFragment.
     */
    // TODO: Rename and change types and number of parameters
    //String param1, String param2
    public static FinishGameFragment newInstance() {
        //        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return new FinishGameFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(boolean isOver) {
        if (mListener != null) {
            mListener.onFragmentInteraction(isOver);
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
        void onFragmentInteraction(boolean isOver);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog, null);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.end_game_msg)
                .setPositiveButton(R.string.end_msg, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(true);
                    }
                })
                .setNegativeButton(R.string.undo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed(false);
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
}
