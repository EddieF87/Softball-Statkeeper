package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.View;

import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.objects.Player;

import java.util.List;

/**
 * Created by Eddie on 02/09/2017.
 */

public class DragListener implements View.OnDragListener {

    private boolean isDropped = false;

    DragListener() {}

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DROP:
                isDropped = true;
                int positionTarget = -1;

                View viewSource = (View) event.getLocalState();
                int viewId = v.getId();
                final int flItem = R.id.lineup_item_layout;
                final int rvLeft = R.id.rvLeft;
                final int rvRight = R.id.rvRight;

                switch (viewId) {
                    case flItem:
                    case rvLeft:
                    case rvRight:

                        RecyclerView target;
                        switch (viewId) {
                            case rvLeft:
                                target = v.getRootView().findViewById(rvLeft);
                                break;
                            case rvRight:
                                target = v.getRootView().findViewById(rvRight);
                                break;
                            default:
                                target = (RecyclerView) v.getParent();
                                positionTarget = (int) v.getTag();
                        }

                        if (viewSource != null) {
                            RecyclerView source = (RecyclerView) viewSource.getParent();

                            SetLineupAdapter adapterSource = (SetLineupAdapter) source.getAdapter();
                            int positionSource = (int) viewSource.getTag();
                            int sourceId = source.getId();

                            Player player = adapterSource.getPlayerList().get(positionSource);
                            List<Player> listSource = adapterSource.getPlayerList();

                            listSource.remove(positionSource);
                            adapterSource.updateList(listSource);
                            adapterSource.notifyDataSetChanged();

                            SetLineupAdapter adapterTarget = (SetLineupAdapter) target.getAdapter();
                            List<Player> customListTarget = adapterTarget.getPlayerList();
                            if (positionTarget >= 0) {
                                customListTarget.add(positionTarget, player);
                            } else {
                                customListTarget.add(player);
                            }
                            adapterTarget.updateList(customListTarget);
                            adapterTarget.notifyDataSetChanged();
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