package com.example.android.softballstatkeeper.adapters_listeners_etc;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.objects.ItemMarkedForDeletion;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eddie on 2/2/2018.
 */

public class DeletionCheckRecyclerViewAdapter  extends RecyclerView.Adapter<DeletionCheckRecyclerViewAdapter.DeletionViewHolder> {

    private List<ItemMarkedForDeletion> mItems;
    private List<ItemMarkedForDeletion> mDeleteList;
    private List<ItemMarkedForDeletion> mSaveList;

    public DeletionCheckRecyclerViewAdapter(List<ItemMarkedForDeletion> items) {
        super();
        mItems = items;
        mDeleteList = new ArrayList<>();
        mDeleteList.addAll(mItems);
        mSaveList = new ArrayList<>();
    }

    @Override
    public DeletionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_marked_for_deletion, parent, false);
        return new DeletionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeletionViewHolder holder, int position) {
        final ItemMarkedForDeletion itemMarkedForDeletion = mItems.get(position);
        String name = itemMarkedForDeletion.getName();
        holder.nameView.setText(name);
        holder.deleteCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    if (!mDeleteList.contains(itemMarkedForDeletion)) {
                        mDeleteList.add(itemMarkedForDeletion);
                    }
                    if (mSaveList.contains(itemMarkedForDeletion)) {
                        mSaveList.remove(itemMarkedForDeletion);
                    }
                } else {
                    if (mDeleteList.contains(itemMarkedForDeletion)) {
                        mDeleteList.remove(itemMarkedForDeletion);
                    }
                    if (!mSaveList.contains(itemMarkedForDeletion)) {
                        mSaveList.add(itemMarkedForDeletion);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class DeletionViewHolder extends RecyclerView.ViewHolder {
        private TextView nameView;
        private CheckBox deleteCheck;

        DeletionViewHolder(View itemView) {
            super(itemView);
            this.nameView = itemView.findViewById(R.id.name);
            this.deleteCheck = itemView.findViewById(R.id.checkbox_delete);
        }
    }

    public List<ItemMarkedForDeletion> getDeleteList() {
        return mDeleteList;
    }

    public List<ItemMarkedForDeletion> getSaveList() {
        return mSaveList;
    }
}