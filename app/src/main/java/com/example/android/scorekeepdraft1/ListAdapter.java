package com.example.android.scorekeepdraft1;

import android.content.ClipData;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Eddie on 02/09/2017.
 */

public class ListAdapter  extends RecyclerView.Adapter<ListAdapter.ListViewHolder>
        implements View.OnTouchListener {

/*    private List<Player> lineupList;
    private List<Player> benchList;*/
    private List<Player> list;
    private Listener listener;
    private boolean isBench;


    public ListAdapter(List<Player> list, Listener listener, boolean isBench) {
        this.list = list;
        this.listener = listener;
        this.isBench = isBench;
    }

    @Override
    public ListAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);

        return new ListViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(ListAdapter.ListViewHolder holder, int position) {
        FrameLayout frameLayout = holder.mFrameLayout;
        TextView textView = (TextView) frameLayout.findViewById(R.id.text);

        if(isBench) {
            textView.setText("B:   " + list.get(position).getName());
        } else {
            textView.setText((position + 1) + ". " + list.get(position).getName());
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

    public List<Player> getList() {return list;    }
    public void updateList(List<Player> list) {
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
            Log.e("ListAdapter", "Listener wasn't initialized!");
            return null;
        }
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        FrameLayout mFrameLayout;

        public ListViewHolder(View itemView) {
            super(itemView);
            mFrameLayout = (FrameLayout) itemView;
        }

    }
}
