package xyz.sleekstats.softball.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import xyz.sleekstats.softball.adapters.PlayerStatsAdapter;
import xyz.sleekstats.softball.data.FirestoreUpdateService;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.TimeStampUpdater;
import xyz.sleekstats.softball.dialogs.EditTeamStatsDialog;
import xyz.sleekstats.softball.fragments.TeamFragment;
import xyz.sleekstats.softball.models.TeamLog;

public class TeamPagerActivity extends ObjectPagerActivity implements EditTeamStatsDialog.OnFragmentInteractionListener {

    @Override
    protected void onStart() {
        super.onStart();
        startPager(0, StatsContract.StatsEntry.CONTENT_URI_TEAMS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PlayerStatsAdapter.REQUEST_CODE) {
                if (resultCode == RESULT_OK || resultCode == 17 || resultCode == 18) {
                    startPager(0, StatsContract.StatsEntry.CONTENT_URI_TEAMS);
                }
            }
        } catch (Exception ex) {
            Toast.makeText(TeamPagerActivity.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void setPagerTitle(String name) {
        setTitle(name + ": Teams");
    }

    @Override
    public void onEdit(String enteredText, int type) {
        super.onEdit(enteredText, type);
        if (enteredText.isEmpty()) {
            return;
        }
        boolean update = false;
        TeamFragment teamFragment = getCurrentTeamFragment();
        if (teamFragment != null) {
            update = teamFragment.updateTeamName(enteredText);
        }
        if(update) {
            TimeStampUpdater.updateTimeStamps(this, getSelectionID(), System.currentTimeMillis());
        }
        setResult(RESULT_OK);
    }

    @Override
    public void onSaveTeamStatsUpdate(String teamID, int wins, int losses, int ties, int runsScored, int runsAllowed) {

        String statKeeperID = getSelectionID();
        Intent intent = new Intent(TeamPagerActivity.this, FirestoreUpdateService.class);
        intent.putExtra(StatsContract.StatsEntry.COLUMN_TEAM, new TeamLog(0, wins, losses, ties, runsScored, runsAllowed));
        intent.putExtra(FirestoreUpdateService.STATKEEPER_ID, statKeeperID);
        intent.putExtra(StatsContract.StatsEntry.COLUMN_FIRESTORE_ID, teamID);
        intent.putExtra(TimeStampUpdater.UPDATE_TIME, System.currentTimeMillis());
        intent.setAction(FirestoreUpdateService.INTENT_UPDATE_TEAM);
        startService(intent);

        String selection = StatsContract.StatsEntry.COLUMN_LEAGUE_ID + "=? AND " + StatsContract.StatsEntry.COLUMN_FIRESTORE_ID + "=?";
        String[] selectionArgs = new String[]{statKeeperID, teamID};

        Cursor cursor = getContentResolver().query(StatsContract.StatsEntry.CONTENT_URI_TEAMS, null, selection, selectionArgs, null);
        if(cursor.moveToFirst()){
            wins += StatsContract.getColumnInt(cursor, StatsContract.StatsEntry.COLUMN_WINS);
            losses += StatsContract.getColumnInt(cursor, StatsContract.StatsEntry.COLUMN_LOSSES);
            ties += StatsContract.getColumnInt(cursor, StatsContract.StatsEntry.COLUMN_TIES);
            runsScored += StatsContract.getColumnInt(cursor, StatsContract.StatsEntry.COLUMN_RUNSFOR);
            runsAllowed += StatsContract.getColumnInt(cursor, StatsContract.StatsEntry.COLUMN_RUNSAGAINST);
        }
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsContract.StatsEntry.COLUMN_WINS, wins);
        contentValues.put(StatsContract.StatsEntry.COLUMN_LOSSES, losses);
        contentValues.put(StatsContract.StatsEntry.COLUMN_TIES, ties);
        contentValues.put(StatsContract.StatsEntry.COLUMN_RUNSFOR, runsScored);
        contentValues.put(StatsContract.StatsEntry.COLUMN_RUNSAGAINST, runsAllowed);
        getContentResolver().update(StatsContract.StatsEntry.CONTENT_URI_TEAMS, contentValues, selection, selectionArgs);
    }
}