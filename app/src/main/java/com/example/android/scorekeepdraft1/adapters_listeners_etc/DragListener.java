package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.View;

import com.example.android.scorekeepdraft1.Player;
import com.example.android.scorekeepdraft1.R;

import java.util.List;

/**
 * Created by Eddie on 02/09/2017.
 */

public class DragListener implements View.OnDragListener {

    private boolean isDropped = false;
    private Listener listener;

    DragListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DROP:
                isDropped = true;
                int positionTarget = -1;

                View viewSource = (View) event.getLocalState();
                int viewId = v.getId();
                final int flItem = R.id.lineup_item_layout;
                //final int tvEmptyListTop = R.id.tvEmptyListTop;
                //final int tvEmptyListBottom = R.id.tvEmptyListBottom;
                final int rvLeft = R.id.rvLeft;
                final int rvRight = R.id.rvRight;

                switch (viewId) {
                    case flItem:
//                        case tvEmptyListTop:
//                        case tvEmptyListBottom:
                    case rvLeft:
                    case rvRight:

                        RecyclerView target;
                        switch (viewId) {
                            //case tvEmptyListTop:
                            case rvLeft:
                                target = (RecyclerView) v.getRootView().findViewById(rvLeft);
                                break;
                            //case tvEmptyListBottom:
                            case rvRight:
                                target = (RecyclerView) v.getRootView().findViewById(rvRight);
                                break;
                            default:
                                target = (RecyclerView) v.getParent();
                                positionTarget = (int) v.getTag();
                        }

                        if (viewSource != null) {
                            RecyclerView source = (RecyclerView) viewSource.getParent();

                            LineupListAdapter adapterSource = (LineupListAdapter) source.getAdapter();
                            int positionSource = (int) viewSource.getTag();
                            int sourceId = source.getId();

                            String list = adapterSource.getList().get(positionSource);
                            List<String> listSource = adapterSource.getList();

                            listSource.remove(positionSource);
                            adapterSource.updateList(listSource);
                            adapterSource.notifyDataSetChanged();

                            LineupListAdapter adapterTarget = (LineupListAdapter) target.getAdapter();
                            List<String> customListTarget = adapterTarget.getList();
                            if (positionTarget >= 0) {
                                customListTarget.add(positionTarget, list);
                            } else {
                                customListTarget.add(list);
                            }
                            adapterTarget.updateList(customListTarget);
                            adapterTarget.notifyDataSetChanged();

/*                                if (sourceId == rvRight && adapterSource.getItemCount() < 1) {
                                    listener.setEmptyListBottom(true);
                                }
                                if (viewId == tvEmptyListBottom) {
                                    listener.setEmptyListBottom(false);
                                }
                                if (sourceId == rvLeft && adapterSource.getItemCount() < 1) {
                                    listener.setEmptyListTop(true);
                                }
                                if (viewId == tvEmptyListTop) {
                                    listener.setEmptyListTop(false);
                                }*/
                        }
                        break;
                }
                break;
        }

        if (!isDropped && event.getLocalState() != null) {
            ((View) event.getLocalState()).setVisibility(View.VISIBLE);
        }
        return true;
    }
}