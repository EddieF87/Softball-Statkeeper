package xyz.sleekstats.softball.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import xyz.sleekstats.softball.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccessGuideDialog extends DialogFragment {

    public AccessGuideDialog() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String accessLevels = "Admin: View/Create/Update/Delete"
                + "\n\nView/Manage: View/Create/Update"
                + "\n\nView Only: View";
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(dialog != null) {
                            dialog.dismiss();
                        }
                    }
                })
                .setTitle("Access Level Privileges")
                .setMessage(accessLevels);

        return builder.create();
    }
}
