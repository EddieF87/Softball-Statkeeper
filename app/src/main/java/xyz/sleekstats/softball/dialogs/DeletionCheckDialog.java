package xyz.sleekstats.softball.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.adapters.DeletionCheckRecyclerViewAdapter;
import xyz.sleekstats.softball.objects.ItemMarkedForDeletion;

import java.util.ArrayList;
import java.util.List;


public class DeletionCheckDialog extends DialogFragment {

    private OnListFragmentInteractionListener mListener;
    private static final String KEY_DELETIONS = "deletions";
    private DeletionCheckRecyclerViewAdapter mAdapter;

    public DeletionCheckDialog() {
        // Required empty public constructor
    }

    public static DeletionCheckDialog newInstance(ArrayList<ItemMarkedForDeletion> items) {
        DeletionCheckDialog fragment = new DeletionCheckDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_DELETIONS, items);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            List<ItemMarkedForDeletion> mDeleteList = args.getParcelableArrayList(KEY_DELETIONS);
            mAdapter = new DeletionCheckRecyclerViewAdapter(mDeleteList);
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_deletion_check, null);
        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.rv_delete);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(mAdapter);

        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.MyAlertDialog)
                .setView(view)
                .setTitle("Permanently delete?")
                .setMessage("The following players/teams have been deleted elsewhere." +
                        " Keep checked to also delete from your device (game in progress may also be deleted).")
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onButtonPressed();
                    }
                })
                .setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onCancel();
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
            List<ItemMarkedForDeletion> deleteList = mAdapter.getDeleteList();
            List<ItemMarkedForDeletion> saveList = mAdapter.getSaveList();
            mListener.onDeletePlayersListener(deleteList, saveList);
        }
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

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mListener != null) {
            mListener.onCancel();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        mAdapter = null;
        super.onDestroy();
    }

    public interface OnListFragmentInteractionListener {
        void onDeletePlayersListener(List<ItemMarkedForDeletion> items, List<ItemMarkedForDeletion> saveList);
        void onCancel();
    }
}
