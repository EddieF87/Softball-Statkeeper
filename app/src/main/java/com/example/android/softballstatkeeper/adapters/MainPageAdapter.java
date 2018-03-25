package com.example.android.softballstatkeeper.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.softballstatkeeper.MyApp;
import com.example.android.softballstatkeeper.R;
import com.example.android.softballstatkeeper.activities.LoadingActivity;
import com.example.android.softballstatkeeper.activities.MainActivity;
import com.example.android.softballstatkeeper.activities.PlayerManagerActivity;
import com.example.android.softballstatkeeper.dialogs.SelectionInfoDialog;
import com.example.android.softballstatkeeper.models.MainPageSelection;

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
    public void onBindViewHolder(final MainPageViewHolder holder, final int position) {
        final MainPageSelection mainPageSelection = mList.get(position);
        final int selectionType = mainPageSelection.getType();
        final Intent intent;
        String name;

        switch (selectionType) {
            case MainPageSelection.TYPE_LEAGUE:
                intent = new Intent(mContext, LoadingActivity.class);
                name = mainPageSelection.getName() + "  (L)";
                holder.mNameView.setTextColor(Color.rgb(120, 160, 0));
                break;
            case MainPageSelection.TYPE_TEAM:
                intent = new Intent(mContext, LoadingActivity.class);
                name = mainPageSelection.getName() + "  (T)";
                holder.mNameView.setTextColor(Color.rgb(0, 160, 100));
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
//                mList.clear();
//                notifyDataSetChanged();
                startActivity(mContext, intent, null);
            }
        });
        holder.mInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInfoDialog(mainPageSelection);
            }
        });
    }

    private void openInfoDialog(MainPageSelection selection){
        FragmentManager fragmentManager = ((MainActivity)mContext).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = SelectionInfoDialog.newInstance(selection);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class MainPageViewHolder extends RecyclerView.ViewHolder {
        private ImageView mInfoView;
        private TextView mNameView;

        private MainPageViewHolder(View itemView) {
            super(itemView);
            mInfoView = itemView.findViewById(R.id.info_img);
            mNameView = itemView.findViewById(R.id.name_text);
        }
    }
}
