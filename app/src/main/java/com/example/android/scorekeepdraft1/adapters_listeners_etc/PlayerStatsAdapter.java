package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.scorekeepdraft1.activities.LeagueManagerActivity;
import com.example.android.scorekeepdraft1.activities.ObjectPagerActivity;
import com.example.android.scorekeepdraft1.activities.PlayerPagerActivity;
import com.example.android.scorekeepdraft1.activities.TeamManagerActivity;
import com.example.android.scorekeepdraft1.activities.TeamPagerActivity;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.dialogs.ChangeTeamDialogFragment;
import com.example.android.scorekeepdraft1.objects.Player;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.Team;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by Eddie on 03/09/2017.
 */

public class PlayerStatsAdapter extends RecyclerView.Adapter<PlayerStatsAdapter.PlayerStatsListViewHolder> {

    private List<Player> players;
    private final NumberFormat formatter = new DecimalFormat("#.000");
    private int visibility;
    private boolean isTeam = false;
    private boolean genderSettingsOff;
    private Context mContext;
    private int colorMale;
    private int colorFemale;
    public static final int REQUEST_CODE = 1;


    public PlayerStatsAdapter(List<Player> players, Context context, int genderSorter) {
        super();
        this.players = players;
        this.mContext = context;
        if (context instanceof TeamManagerActivity || context instanceof TeamPagerActivity) {
            visibility = View.GONE;
            isTeam = true;
        } else {
            visibility = View.VISIBLE;
            isTeam = false;
        }
        this.genderSettingsOff = genderSorter == 0;
        if (genderSettingsOff) {
            colorMale = Color.parseColor("#666666");
            colorFemale = Color.parseColor("#666666");
        } else {
            colorMale = ContextCompat.getColor(context, R.color.male);
            colorFemale = ContextCompat.getColor(context, R.color.female);
        }
    }

    @Override
    public PlayerStatsAdapter.PlayerStatsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stats, parent, false);
        return new PlayerStatsListViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(final PlayerStatsAdapter.PlayerStatsListViewHolder holder, int position) {

        if (position % 2 == 1) {
            holder.linearLayout.setBackgroundColor(Color.parseColor("#dfdfdf"));
        }
        Player player = players.get(position);
        holder.bindPlayer(player);
    }

    private void changeTeamDialog(final Player player) {
        if(!(mContext instanceof ObjectPagerActivity)) {
            return;
        }
        ArrayList<Team> teams = new ArrayList<>();
        String sortOrder = StatsEntry.COLUMN_NAME + " COLLATE NOCASE ASC";
        Cursor cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                null, null, null, sortOrder);
        while (cursor.moveToNext()) {
            teams.add(new Team(cursor));
        }
        teams.add(new Team(mContext.getString(R.string.waivers), StatsEntry.FREE_AGENT));

        FragmentManager fragmentManager = ((ObjectPagerActivity)mContext).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = ChangeTeamDialogFragment.newInstance(teams, player.getName(), player.getFirestoreID());
        newFragment.show(fragmentTransaction, "");
    }

    public boolean changeColors(boolean genderSettingsOn) {
        if (genderSettingsOn) {
            if (!genderSettingsOff) {
                return false;
            }
            colorMale = ContextCompat.getColor(mContext, R.color.male);
            colorFemale = ContextCompat.getColor(mContext, R.color.female);
            genderSettingsOff = false;
        } else {
            if (genderSettingsOff) {
                return false;
            }
            colorMale = Color.parseColor("#666666");
            colorFemale = Color.parseColor("#666666");
            genderSettingsOff = true;
        }
        return true;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    class PlayerStatsListViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayout;
        private TextView abView;
        private TextView hitView;
        private TextView hrView;
        private TextView rbiView;
        private TextView runView;
        private TextView avgView;
        private TextView obpView;
        private TextView slgView;
        private TextView opsView;
        private TextView sglView;
        private TextView dblView;
        private TextView tplView;
        private TextView bbView;
        private TextView gameView;
        private TextView nameView;
        private TextView teamView;
        private Player mPlayer;

        PlayerStatsListViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView;
            nameView = linearLayout.findViewById(R.id.name);
            teamView = linearLayout.findViewById(R.id.team_abv);
            abView = linearLayout.findViewById(R.id.ab);
            hitView = linearLayout.findViewById(R.id.hit);
            hrView = linearLayout.findViewById(R.id.hr);
            rbiView = linearLayout.findViewById(R.id.rbi);
            runView = linearLayout.findViewById(R.id.run);
            avgView = linearLayout.findViewById(R.id.avg);
            obpView = linearLayout.findViewById(R.id.obp);
            slgView = linearLayout.findViewById(R.id.slg);
            opsView = linearLayout.findViewById(R.id.ops);
            sglView = linearLayout.findViewById(R.id.sgl);
            dblView = linearLayout.findViewById(R.id.dbl);
            tplView = linearLayout.findViewById(R.id.tpl);
            bbView = linearLayout.findViewById(R.id.bb);
            gameView = linearLayout.findViewById(R.id.game);
        }

        private void bindPlayer(Player player) {
            this.mPlayer = player;
            String teamfirestoreid = player.getTeamfirestoreid();
            String team = player.getTeam();
            String teamabv;
            boolean FA = false;

            if (teamfirestoreid == null || teamfirestoreid.equals(StatsEntry.FREE_AGENT)) {
                teamabv = "FA";
                FA = true;
            } else if (team.length() > 2) {
                teamabv = ("" + team.charAt(0) + team.charAt(1) + team.charAt(2)).toUpperCase();
            } else if (team.length() > 1) {
                teamabv = ("" + team.charAt(0) + team.charAt(1)).toUpperCase();
            } else if (team.length() > 0) {
                teamabv = ("" + team.charAt(0)).toUpperCase();
            } else {
                teamabv = "   ";
            }

            long playerId = player.getPlayerId();
            nameView.setTag(playerId);

            teamView.setTag(teamfirestoreid);

            if (!isTeam) {
                teamView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String teamfirestoreid = (String) teamView.getTag();

                        Intent intent = new Intent(mContext, TeamPagerActivity.class);
                        Log.d("xxx", "teamclick " + teamfirestoreid);

                        Uri currentTeamUri = null;
                        if (!teamfirestoreid.equals(StatsEntry.FREE_AGENT)) {
                            String selection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
                            String[] selectionArgs = new String[] {teamfirestoreid};
                            String[] projection = new String[] {StatsEntry._ID};

                            Cursor cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                                    projection, selection, selectionArgs, null);
                            if(cursor.moveToFirst()) {
                                int teamID = StatsContract.getColumnInt(cursor, StatsEntry._ID);
                                currentTeamUri = ContentUris.withAppendedId(StatsContract.StatsEntry.CONTENT_URI_TEAMS, teamID);
                            }
                        }
                        intent.setData(currentTeamUri);
                        if (mContext instanceof LeagueManagerActivity) {
                            ((LeagueManagerActivity) mContext).startActivityForResult(intent, REQUEST_CODE);
                        } else {
                            startActivity(mContext, intent, null);
                        }
                    }
                });
            }
            nameView.setText(player.getName());
            teamView.setText(teamabv);
            int ab = player.getABs();
            int bb = player.getWalks();
            int sf = player.getSacFlies();
            abView.setText(String.valueOf(ab));
            hitView.setText(String.valueOf(player.getHits()));
            hrView.setText(String.valueOf(player.getHrs()));
            rbiView.setText(String.valueOf(player.getRbis()));
            runView.setText(String.valueOf(player.getRuns()));
            sglView.setText(String.valueOf(player.getSingles()));
            dblView.setText(String.valueOf(player.getDoubles()));
            tplView.setText(String.valueOf(player.getTriples()));
            gameView.setText(String.valueOf(player.getGames()));
            bbView.setText(String.valueOf(bb));
            if (ab == 0) {
                avgView.setText("- - -");
                slgView.setText("- - -");
            } else {
                avgView.setText(String.valueOf(formatter.format(player.getAVG())));
                slgView.setText(String.valueOf(formatter.format(player.getSLG())));
            }
            if (ab == 0 && bb == 0 && sf == 0) {
                obpView.setText("- - -");
                opsView.setText("- - -");
            } else {
                obpView.setText(String.valueOf(formatter.format(player.getOBP())));
                opsView.setText(String.valueOf(formatter.format(player.getOPS())));
            }
            teamView.setVisibility(visibility);
            if (FA && isTeam) {
                teamView.setVisibility(View.VISIBLE);
                teamView.setText("+");
                int color = ContextCompat.getColor(mContext, R.color.colorPrimary);
                teamView.setTextColor(color);
                teamView.setTypeface(Typeface.DEFAULT_BOLD);
                teamView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeTeamDialog(mPlayer);
                    }
                });
            }

            if (isTeam && player.getName().equals("Total")) {
                setViewBold();
            } else {
                if (abView.getTypeface() == Typeface.DEFAULT_BOLD) {
                    setViewDefault();
                }
                nameView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, PlayerPagerActivity.class);
                        long playerId = (long) nameView.getTag();
                        Uri playerUri = ContentUris.withAppendedId(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, playerId);
                        intent.setData(playerUri);
                        Log.d("zzz", "startActivity");
                        if (mContext instanceof TeamPagerActivity) {
                            ((TeamPagerActivity) mContext).startActivityForResult(intent, REQUEST_CODE);
                        } else if (mContext instanceof TeamManagerActivity) {
                            ((TeamManagerActivity) mContext).startActivityForResult(intent, REQUEST_CODE);
                        } else if (mContext instanceof LeagueManagerActivity) {
                            ((LeagueManagerActivity) mContext).startActivityForResult(intent, REQUEST_CODE);
                        } else {
                                startActivity(mContext, intent, null);
                        }
                    }
                });
                int gender = player.getGender();
                if (gender == 0) {
                    nameView.setTextColor(colorMale);
                } else {
                    nameView.setTextColor(colorFemale);
                }
            }
        }

        private void setViewBold(){
            abView.setTypeface(Typeface.DEFAULT_BOLD);
            hitView.setTypeface(Typeface.DEFAULT_BOLD);
            hrView.setTypeface(Typeface.DEFAULT_BOLD);
            rbiView.setTypeface(Typeface.DEFAULT_BOLD);
            runView.setTypeface(Typeface.DEFAULT_BOLD);
            sglView.setTypeface(Typeface.DEFAULT_BOLD);
            dblView.setTypeface(Typeface.DEFAULT_BOLD);
            tplView.setTypeface(Typeface.DEFAULT_BOLD);
            gameView.setTypeface(Typeface.DEFAULT_BOLD);
            bbView.setTypeface(Typeface.DEFAULT_BOLD);
            avgView.setTypeface(Typeface.DEFAULT_BOLD);
            obpView.setTypeface(Typeface.DEFAULT_BOLD);
            slgView.setTypeface(Typeface.DEFAULT_BOLD);
            opsView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        private void setViewDefault(){
            abView.setTypeface(Typeface.DEFAULT);
            hitView.setTypeface(Typeface.DEFAULT);
            hrView.setTypeface(Typeface.DEFAULT);
            rbiView.setTypeface(Typeface.DEFAULT);
            runView.setTypeface(Typeface.DEFAULT);
            sglView.setTypeface(Typeface.DEFAULT);
            dblView.setTypeface(Typeface.DEFAULT);
            tplView.setTypeface(Typeface.DEFAULT);
            gameView.setTypeface(Typeface.DEFAULT);
            bbView.setTypeface(Typeface.DEFAULT);
            avgView.setTypeface(Typeface.DEFAULT);
            obpView.setTypeface(Typeface.DEFAULT);
            slgView.setTypeface(Typeface.DEFAULT);
            opsView.setTypeface(Typeface.DEFAULT);
        }
    }
}
