package xyz.sleekstats.softball.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.objects.ItemMarkedForDeletion;

import java.util.ArrayList;

/**
 * Created by Eddie on 2/2/2018.
 */

public class DeletionCheckRecyclerViewAdapter  extends RecyclerView.Adapter<DeletionCheckRecyclerViewAdapter.DeletionViewHolder> {

    private final ArrayList<ItemMarkedForDeletion> mItems;
    private final ArrayList<ItemMarkedForDeletion> mDeleteList;
    private final ArrayList<ItemMarkedForDeletion> mSaveList;

    public DeletionCheckRecyclerViewAdapter(ArrayList<ItemMarkedForDeletion> items) {
        super();
        this.setHasStableIds(true);
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
        if(mItems == null) {return 0;}
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    class DeletionViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final CheckBox deleteCheck;

        DeletionViewHolder(View itemView) {
            super(itemView);
            this.nameView = itemView.findViewById(R.id.name);
            this.deleteCheck = itemView.findViewById(R.id.checkbox_delete);
        }
    }

    public ArrayList<ItemMarkedForDeletion> getDeleteList() {
        return mDeleteList;
    }

    public ArrayList<ItemMarkedForDeletion> getSaveList() {
        return mSaveList;
    }
}