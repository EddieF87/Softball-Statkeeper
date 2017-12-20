package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.example.android.scorekeepdraft1.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**

 * TODO: Replace the implementation with code for your data type.
 */
public class CreateTeamRecyclerViewAdapter extends RecyclerView.Adapter<CreateTeamRecyclerViewAdapter.EditViewHolder> {

    private static final String TAG = "CreateTeamRVA";
    private List<String> mNameEntries;
    private List<Integer> mGenderEntries;
    private HashSet<Integer> mEdits;

    public CreateTeamRecyclerViewAdapter() {
        this.setHasStableIds(true);
        mNameEntries = new ArrayList<>();
        mGenderEntries = new ArrayList<>();
        mNameEntries.add(null);
        mGenderEntries.add(0);
        mEdits = new HashSet<>();
    }

    public CreateTeamRecyclerViewAdapter(List<String> nameEntries, List<Integer> genderEntries, List<Integer> edits) {
        this.setHasStableIds(true);
        mNameEntries = nameEntries;
        mGenderEntries = genderEntries;
        if (edits != null) {
            mEdits = new HashSet<>(edits);
        } else {
            mEdits = new HashSet<>();
        }
    }

    @Override
    public EditViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_editor, parent, false);
        return new EditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EditViewHolder holder, int position) {
        holder.mEditText.setTag(position);
        int gender = mGenderEntries.get(position);
        holder.mToggle.setChecked(gender == 1);
        if (mNameEntries.size() <= position + 1) {
            return;
        }
        String textEntry = mNameEntries.get(position);

        if(holder.previouslyEdited(position) && textEntry != null) {
            holder.mEditText.setText(textEntry);
            int length = holder.mEditText.length();
            holder.mEditText.setSelection(length);
        }
    }

    @Override
    public int getItemCount() {
        return mNameEntries.size();
    }

    public HashSet<Integer> getmEdits() {
        return mEdits;
    }

    public List<String> getmNameEntries() {
        return mNameEntries;
    }

    public List<Integer> getmGenderEntries() {
        return mGenderEntries;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class EditViewHolder extends RecyclerView.ViewHolder {
        private EditText mEditText;
        private ToggleButton mToggle;

        EditViewHolder(View view) {
            super(view);
            mEditText = view.findViewById(R.id.text_adder);
            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    int position = (int) mEditText.getTag();

                    String string = mEditText.getText().toString();
                    mNameEntries.set(position, string);
                    if (!previouslyEdited(position)) {
                        mNameEntries.add(null);
                        mGenderEntries.add(0);
                        notifyDataSetChanged();
                    }
                }
            });

            mToggle = view.findViewById(R.id.toggle_gender);
            mToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    int position = (int) mEditText.getTag();
                    int gender;
                    if (isChecked) {
                        gender = 1;
                    } else {
                        gender = 0;
                    }
                    mToggle.setChecked(isChecked);
                    mGenderEntries.set(position, gender);
                }
            });

        }

        private boolean previouslyEdited(int position) {
            if (mEdits.contains(position)) {
                return true;
            } else {
                mEdits.add(position);
                return false;
            }
        }
    }
}
