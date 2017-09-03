package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.content.ClipData;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.Player;
import com.example.android.scorekeepdraft1.R;

import java.util.List;

/**
 * Created by Eddie on 02/09/2017.
 */

public class LineupListAdapter extends RecyclerView.Adapter<LineupListAdapter.ListViewHolder>
        implements View.OnTouchListener {

    private List<String> list;
    private Listener listener;
    private boolean isBench;


    public LineupListAdapter(List<String> list, Listener listener, boolean isBench) {
        this.list = list;
        this.listener = listener;
        this.isBench = isBench;
    }

    @Override
    public LineupListAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lineup, parent, false);

        return new ListViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(LineupListAdapter.ListViewHolder holder, int position) {
        FrameLayout frameLayout = holder.mFrameLayout;
        TextView textView = (TextView) frameLayout.findViewById(R.id.lineup_text);

        if(isBench) {
            textView.setText("B:   " + list.get(position));
        } else {
            textView.setText((position + 1) + ". " + list.get(position));
        }
        frameLayout.setTag(position);
        frameLayout.setOnTouchListener(this);
        frameLayout.setOnDragListener(new DragListener(listener));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    v.startDragAndDrop(data, shadowBuilder, v, 0);
                } else {
                    v.startDrag(data, shadowBuilder, v, 0);
                }
                return true;
        }
        return false;
    }

    public List<String> getList() {return list;    }
    public void updateList(List<String> list) {
        this.list = list;
    }



    /*
    public List<Player> getLineupList(){
        return lineupList;
    }

    public List<Player> getBenchList() {
        return benchList;
    }

    public void updateLineupList(List<Player> list) {
        this.lineupList = list;
    }
    public void updateBenchList(List<Player> list) {
        this.benchList = list;
    }
*/
    public DragListener getDragInstance() {
        if (listener != null) {
            return new DragListener(listener);
        } else {
            Log.e("LineupListAdapter", "Listener wasn't initialized!");
            return null;
        }
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {

        FrameLayout mFrameLayout;

         ListViewHolder(View itemView) {
            super(itemView);
            mFrameLayout = (FrameLayout) itemView;
        }

    }
}