package xyz.sleekstats.softball.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.models.Player;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

/**
 * Created by Eddie on 2/20/2018.
 */

public class MyLineupAdapter extends DragItemAdapter<Pair<Long, Player>, MyLineupAdapter.ViewHolder> {

    private final int mLayoutId;
    private final int mGrabHandleId;
    private final boolean mDragOnLongPress;
    private boolean genderSettingsOff;
    private final boolean isBench;
    private int colorMale;
    private int colorFemale;
    private final Context mContext;

    public MyLineupAdapter(ArrayList<Pair<Long, Player>> list, Context context, boolean isBench, int genderSorter) {
        this.setHasStableIds(true);
        mLayoutId = R.layout.item_lineup;
        mGrabHandleId = R.id.lineup_mover;
        mDragOnLongPress = false;
        this.mContext = context;
        this.isBench = isBench;
        genderSettingsOff = genderSorter == 0;
        if (genderSettingsOff) {
            setNormalColors();
        } else {
            setGenderColors();
        }
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
        String name = player.getName();
        holder.itemView.setTag(mItemList.get(position));
        int gender = player.getGender();

        if (gender == 0) {
            holder.mGrabView.setBackgroundColor(colorMale);
        } else {
            holder.mGrabView.setBackgroundColor(colorFemale);
        }

        if(isBench) {
            holder.mText.setText(name);
        } else {
            String positionText = (position + 1) + ". " + name;
            holder.mText.setText(positionText);
        }
    }

    @Override
    public long getUniqueItemId(int position) {
        return mItemList.get(position).first;
    }

//    public void removePlayers() {
//        mItemList.clear();
//        notifyDataSetChanged();
//    }

    public boolean changeColors(boolean genderSettingsOn){
        if (genderSettingsOn) {
            if (!genderSettingsOff) {
                return false;
            }
            setGenderColors();
            genderSettingsOff = false;
        } else {
            if (genderSettingsOff) {
                return false;
            }
            setNormalColors();
            genderSettingsOff = true;
        }
        return true;
    }

    private void setNormalColors(){
        colorMale = ContextCompat.getColor(mContext, R.color.colorPrimary);
        colorFemale = ContextCompat.getColor(mContext, R.color.colorPrimary);
    }

    private void setGenderColors(){
        colorMale = ContextCompat.getColor(mContext, R.color.male);
        colorFemale = ContextCompat.getColor(mContext, R.color.female);
    }


    class ViewHolder extends DragItemAdapter.ViewHolder  {
        final TextView mText;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mText = itemView.findViewById(R.id.lineup_text);
        }
    }
}
