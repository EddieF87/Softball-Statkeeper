package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ChangeTeamDialogFragment extends DialogFragment {
//    private ArrayList<String> teams;
//
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Cursor mCursor = getActivity().getContentResolver().query(StatsEntry.CONTENT_URI2,
//                new String[]{StatsEntry.COLUMN_NAME}, null, null, null);
//        teams = new ArrayList<>();
//        teams.add("All Teams");
//        while (mCursor.moveToNext()) {
//            int teamNameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
//            String teamName = mCursor.getString(teamNameIndex);
//            teams.add(teamName);
//        }
//        Object[] teams_array = teams.toArray();
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setAdapter()
//        builder.setTitle(R.string.choose_team_title)
//                .setItems((CharSequence[]) teams_array, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // The 'which' argument contains the index position
//                        // of the selected item
//                    }
//                });
//        return builder.create();
//    }

}
