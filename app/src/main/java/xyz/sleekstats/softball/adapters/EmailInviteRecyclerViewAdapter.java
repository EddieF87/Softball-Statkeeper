package xyz.sleekstats.softball.adapters;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.views.MyEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Eddie on 3/18/2018.
 */

public class EmailInviteRecyclerViewAdapter extends RecyclerView.Adapter<EmailInviteRecyclerViewAdapter.InviteViewHolder> {

    private final List<String> mEmails;
    private final List<Integer> mAccessLevels;
    private final HashSet<Integer> mEdits;

    public EmailInviteRecyclerViewAdapter() {
        this.setHasStableIds(true);
        mEmails = new ArrayList<>();
        mAccessLevels = new ArrayList<>();
        mEmails.add(null);
        mAccessLevels.add(0);
        mEdits = new HashSet<>();
    }

    public EmailInviteRecyclerViewAdapter(List<String> emails, List<Integer> levels, List<Integer> edits) {
        this.setHasStableIds(true);
        mEmails = emails;
        mAccessLevels = levels;
        if (edits != null) {
            mEdits = new HashSet<>(edits);
        } else {
            mEdits = new HashSet<>();
        }
    }

    @Override
    public InviteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invite_user, parent, false);
        return new InviteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final InviteViewHolder holder, int position) {
        holder.mEditText.setTag(position);
        holder.mSeekBar.setTag(position);

        int level = mAccessLevels.get(position);
        holder.mSeekBar.setProgress(level);

        if (mEmails.size() <= position + 1) {
            return;
        }
        String textEntry = mEmails.get(position);

        if(holder.previouslyEdited(position) && textEntry != null) {
            holder.mEditText.setText(textEntry);
            int length = holder.mEditText.length();
            holder.mEditText.setSelection(length);
        }
    }

    public void disableEditTextCursor(RecyclerView.ViewHolder viewHolder){
        InviteViewHolder holder = (InviteViewHolder) viewHolder;
        holder.mEditText.setCursorVisible(false);
    }

    @Override
    public int getItemCount() {
        return mEmails.size();
    }

    public HashSet<Integer> getEdits() {
        return mEdits;
    }

    public List<String> getEmails() {
        return mEmails;
    }

    public List<Integer> getAccessLevels() {
        return mAccessLevels;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class InviteViewHolder extends RecyclerView.ViewHolder {
        private final EditText mEditText;
        private final TextView levelTextView;
        private final SeekBar mSeekBar;

        InviteViewHolder(View view) {
            super(view);
            mEditText = view.findViewById(R.id.new_user_edit);
            mSeekBar = view.findViewById(R.id.new_user_level_seekbar);
            levelTextView = view.findViewById(R.id.new_user_level_view);
            mSeekBar.setMax(2);

            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int level, boolean b) {
                    int position = (int) mSeekBar.getTag();

                    String levelDisplay;
                    switch (level) {
                        case 0:
                            levelDisplay = "View Only";
                            break;
                        case 1:
                            levelDisplay = "View/Manage";
                            break;
                        case 2:
                            levelDisplay = "Admin";
                            break;
                        default:
                            levelDisplay = "Error";
                    }
                    levelTextView.setText(levelDisplay);
                    mAccessLevels.set(position, level);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });

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

                    String string = mEditText.getText().toString().trim();
                    mEmails.set(position, string);
                    if (!previouslyEdited(position)) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mEmails.add(null);
                                mAccessLevels.add(0);
                                notifyDataSetChanged();
                            }
                        }, 500);
                    }
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