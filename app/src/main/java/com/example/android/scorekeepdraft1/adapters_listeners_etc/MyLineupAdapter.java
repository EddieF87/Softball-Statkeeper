package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.objects.Player;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

/**
 * Created by Eddie on 2/20/2018.
 */

public class MyLineupAdapter extends DragItemAdapter<Pair<Long, Player>, MyLineupAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    public MyLineupAdapter(ArrayList<Pair<Long, Player>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setItemList(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Player player = mItemList.get(position).second;
        holder.mText.setText(player.getName());
        holder.itemView.setTag(mItemList.get(position));
    }

    @Override
    public long getUniqueItemId(int position) {
        return mItemList.get(position).first;
    }



    class ViewHolder extends DragItemAdapter.ViewHolder  {
        TextView mText;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mText = itemView.findViewById(R.id.lineup_text);
        }

        @Override
        public boolean onItemTouch(View view, MotionEvent event) {
            Log.d("yyy", "onItemTouch");
            return super.onItemTouch(view, event);

        }

        @Override
        public void onItemClicked(View view) {
            Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onItemLongClicked(View view) {
            Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

    }
}
