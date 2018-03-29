package xyz.sleekstats.softball.dialogs;

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
import android.widget.EditText;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.views.MyEditText;

/**
 * Created by Eddie on 3/18/2018.
 */

public class EnterCodeDialog extends DialogFragment {

    private EnterCodeDialog.OnFragmentInteractionListener mListener;
    private EditText mCodeText;
    private int mType;

    public EnterCodeDialog() {
        // Required empty public constructor
    }

    public static EnterCodeDialog newInstance(int type) {
        Bundle args = new Bundle();
        EnterCodeDialog fragment = new EnterCodeDialog();
        args.putInt("mType", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mType = args.getInt("mType");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_enter_code, null);
        mCodeText = view.findViewById(R.id.edit_code);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.enter_statkeeper_code);
        builder.setView(view)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String codeText = mCodeText.getText().toString().trim();
                        mCodeText.setCursorVisible(false);
                        dialog.dismiss();
                        if (mListener != null) {
                            mListener.onSubmitCode(codeText, mType);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCodeText.setCursorVisible(false);
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EnterCodeDialog.OnFragmentInteractionListener) {
            mListener = (EnterCodeDialog.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mCodeText.setCursorVisible(false);
        dialog.dismiss();
        super.onCancel(dialog);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onSubmitCode(String codeText, int type);
    }

    @Override
    public void onDestroy() {
        mCodeText = null;
        super.onDestroy();
    }
}
