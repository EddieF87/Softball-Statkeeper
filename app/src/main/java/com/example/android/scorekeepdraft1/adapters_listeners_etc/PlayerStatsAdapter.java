package com.example.android.scorekeepdraft1.adapters_listeners_etc;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.activities.PlayerPagerActivity;
import com.example.android.scorekeepdraft1.activities.TeamManagerActivity;
import com.example.android.scorekeepdraft1.activities.TeamPagerActivity;
import com.example.android.scorekeepdraft1.data.FirestoreHelper;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
import com.example.android.scorekeepdraft1.objects.Player;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String sortOrder = StatsEntry.COLUMN_NAME + " COLLATE NOCASE ASC";
        Cursor mCursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                new String[]{StatsEntry.COLUMN_NAME}, null, null, sortOrder);
        ArrayList<String> teams = new ArrayList<>();
        while (mCursor.moveToNext()) {
            int teamNameIndex = mCursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            String teamName = mCursor.getString(teamNameIndex);
            teams.add(teamName);
        }
        final CharSequence[] teams_array = teams.toArray(new CharSequence[teams.size()]);
        String titleString = mContext.getResources().getString(R.string.edit_player_team);
        String title = String.format(titleString, player.getName());
        builder.setTitle(title);
        builder.setItems(teams_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String teamString = teams_array[item].toString();
                updatePlayerTeam(player, teamString);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updatePlayerTeam(Player player, String team) {
        String selectionID;
        try {
            MyApp myApp = (MyApp) mContext.getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            selectionID = mainPageSelection.getId();
        } catch (Exception e){
            Toast.makeText(mContext, "Error with updating " + player + "'s team", Toast.LENGTH_SHORT).show();
            return;
        }
        long playerId = player.getPlayerId();
        Uri playerUri = ContentUris.withAppendedId(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, playerId);
        String firestoreID = player.getFirestoreID();
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsEntry.COLUMN_TEAM, team);
        contentValues.put(StatsEntry.COLUMN_ORDER, 99);
        contentValues.put(StatsEntry.COLUMN_FIRESTORE_ID, firestoreID);
        mContext.getContentResolver().update(playerUri, contentValues, null, null);
        players.remove(player);
        notifyDataSetChanged();

        FirestoreHelper firestoreHelper = new FirestoreHelper(mContext, selectionID);
        firestoreHelper.setUpdate(firestoreID, 1);
        firestoreHelper.updateTimeStamps();
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
            String team = player.getTeam();
            String teamabv;
            boolean FA = false;

            if (team == null || team.equals("Free Agent")) {
                teamabv = "FA";
                FA = true;
            } else if (team.length() > 2) {
                teamabv = ("" + team.charAt(0) + team.charAt(1) + team.charAt(2)).toUpperCase();
            } else {
                teamabv = ("" + team.charAt(0)).toUpperCase();
            }

            long playerId = player.getPlayerId();
            nameView.setTag(playerId);

            int teamId = player.getTeamId();
            teamView.setTag(teamId);

            if (!isTeam) {
                teamView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, TeamPagerActivity.class);
                        int teamId = (int) teamView.getTag();
                        Log.d("xxx", "teamclick " + teamId);
                        Uri currentTeamUri = null;
                        if (teamId != -1) {
                            currentTeamUri = ContentUris.withAppendedId(StatsContract.StatsEntry.CONTENT_URI_TEAMS, teamId);
                        }
                        intent.setData(currentTeamUri);
                        startActivity(mContext, intent, null);
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
                int color = ContextCompat.getColor(mContext, R.color.colorPrimaryDark);
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
            } else {
                nameView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, PlayerPagerActivity.class);
                        long playerId = (long) nameView.getTag();
                        Uri playerUri = ContentUris.withAppendedId(StatsContract.StatsEntry.CONTENT_URI_PLAYERS, playerId);
                        intent.setData(playerUri);
                        if (mContext instanceof TeamPagerActivity) {
                            ((TeamPagerActivity) mContext).startActivityForResult(intent, REQUEST_CODE);
                        } else if (mContext instanceof TeamManagerActivity) {
                            ((TeamManagerActivity) mContext).startActivityForResult(intent, REQUEST_CODE);
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
    }
}
