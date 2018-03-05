package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.activities.LoadingActivity;
import com.example.android.scorekeepdraft1.activities.MainActivity;
import com.example.android.scorekeepdraft1.activities.ObjectPagerActivity;
import com.example.android.scorekeepdraft1.activities.PlayerManagerActivity;
import com.example.android.scorekeepdraft1.dialogs.ChangeTeamDialogFragment;
import com.example.android.scorekeepdraft1.dialogs.SelectionInfoDialogFragment;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;

import java.util.List;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by Eddie on 11/9/2017.
 */

public class MainPageAdapter extends RecyclerView.Adapter<MainPageAdapter.MainPageViewHolder> {


    private List<MainPageSelection> mList;
    private Context mContext;

    public MainPageAdapter(List<MainPageSelection> list, Context context) {
        this.mList = list;
        this.mContext = context;
    }

    @Override
    public MainPageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_statkeeper, parent, false);

        return new MainPageViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(MainPageViewHolder holder, int position) {
        final MainPageSelection mainPageSelection = mList.get(position);
        final int selectionType = mainPageSelection.getType();
        final Intent intent;
        String name;

        switch (selectionType) {
            case MainPageSelection.TYPE_LEAGUE:
                intent = new Intent(mContext, LoadingActivity.class);
                name = mainPageSelection.getName() + "  (L)";
                holder.mNameView.setTextColor(Color.rgb(160, 160, 0));
                break;
            case MainPageSelection.TYPE_TEAM:
                intent = new Intent(mContext, LoadingActivity.class);
                name = mainPageSelection.getName() + "  (T)";
                holder.mNameView.setTextColor(Color.rgb(0, 160, 160));
                break;
            case MainPageSelection.TYPE_PLAYER:
                intent = new Intent(mContext, PlayerManagerActivity.class);
                name = mainPageSelection.getName() + "  (P)";
                holder.mNameView.setTextColor(Color.rgb(0, 160, 0));
                break;
            default:
                return;
        }

        holder.mNameView.setText(name);
        holder.mNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp myApp = (MyApp) mContext.getApplicationContext();
                myApp.setCurrentSelection(mainPageSelection);
                startActivity(mContext, intent, null);
                mList.clear();
                notifyDataSetChanged();
            }
        });
        holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                openInfoDialog(mainPageSelection);
                return true;
            }
        });
    }

    private void openInfoDialog(MainPageSelection selection){
        FragmentManager fragmentManager = ((MainActivity)mContext).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = SelectionInfoDialogFragment.newInstance(selection);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class MainPageViewHolder extends RecyclerView.ViewHolder {
        private CardView mCardView;
        private TextView mNameView;

        private MainPageViewHolder(View itemView) {
            super(itemView);
            mCardView = itemView.findViewById(R.id.card);
            mNameView = itemView.findViewById(R.id.name_text);
        }
    }
}
