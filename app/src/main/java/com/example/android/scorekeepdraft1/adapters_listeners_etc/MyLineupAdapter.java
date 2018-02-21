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
    private boolean genderSettingsOff;
    private boolean isBench;
    private int colorMale;
    private int colorFemale;

    public MyLineupAdapter(ArrayList<Pair<Long, Player>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        //        this.isBench = isBench;
//        genderSettingsOff = genderSorter == 0;
//        if (genderSettingsOff) {
//            colorMale = Color.TRANSPARENT;
//            colorFemale = Color.TRANSPARENT;
//        } else {
//            colorMale = ContextCompat.getColor(context, R.color.male);
//            colorFemale = ContextCompat.getColor(context, R.color.female);
//        }
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
//        int gender = player.getGender();
//
//        if (gender == 0) {
//            holder.mFrameLayout.setBackgroundColor(colorMale);
//        } else {
//            holder.mFrameLayout.setBackgroundColor(colorFemale);
//        }
//
//        if(isBench) {
//            String benchPlayer = "B:   " + name;
//            holder.mTextView.setText(benchPlayer);
//        } else {
//            String positionText = (position + 1) + ". " + name;
//            holder.mTextView.setText(positionText);
//        }
//
    }

    @Override
    public long getUniqueItemId(int position) {
        return mItemList.get(position).first;
    }

    public boolean changeColors(boolean genderSettingsOn){
//        if (genderSettingsOn) {
//            if (!genderSettingsOff) {
//                return false;
//            }
//            colorMale = ContextCompat.getColor(mContext, R.color.male);
//            colorFemale = ContextCompat.getColor(mContext, R.color.female);
//            genderSettingsOff = false;
//        } else {
//            if (genderSettingsOff) {
//                return false;
//            }
//            colorMale = Color.TRANSPARENT;
//            colorFemale = Color.TRANSPARENT;
//            genderSettingsOff = true;
//        }
        return true;
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
