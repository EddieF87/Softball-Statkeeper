package xyz.sleekstats.softball.data;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class GameUpdateIntentMaker {

    public static Intent getPlayersIntent(Context context, long updateTime, String statKeeperID) {
        Log.d("zztop", "getPlayersIntent");
        Intent playersIntent = new Intent(context, FirestoreHelperService.class);
        playersIntent.setAction(FirestoreHelperService.INTENT_ADD_PLAYER_STATS);
        playersIntent.putExtra(FirestoreHelperService.STATKEEPER_ID, statKeeperID);
        playersIntent.putExtra(TimeStampUpdater.UPDATE_TIME, updateTime);
        return playersIntent;
    }

    public static Intent getTeamIntent(Context context, long updateTime, String teamID, int runsFor, int runsAgainst, String statKeeperID) {
        Intent teamIntent = new Intent(context, FirestoreHelperService.class);
        teamIntent.setAction(FirestoreHelperService.INTENT_ADD_TEAM_STATS);
        teamIntent.putExtra(FirestoreHelperService.STATKEEPER_ID, statKeeperID);
        teamIntent.putExtra(TimeStampUpdater.UPDATE_TIME, updateTime);
        teamIntent.putExtra(StatsContract.StatsEntry.COLUMN_FIRESTORE_ID, teamID);
        teamIntent.putExtra(StatsContract.StatsEntry.COLUMN_RUNSFOR, runsFor);
        teamIntent.putExtra(StatsContract.StatsEntry.COLUMN_RUNSAGAINST, runsAgainst);
        return teamIntent;
    }

    public static Intent getBoxscoreIntent(Context context, long updateTime, String awayID, String homeID,  int awayRuns, int homeRuns, String statKeeperID) {
        Intent boxscoreIntent = new Intent(context, FirestoreHelperService.class);
        boxscoreIntent.setAction(FirestoreHelperService.INTENT_ADD_BOXSCORE);
        boxscoreIntent.putExtra(FirestoreHelperService.STATKEEPER_ID, statKeeperID);
        boxscoreIntent.putExtra(TimeStampUpdater.UPDATE_TIME, updateTime);
        boxscoreIntent.putExtra(StatsContract.StatsEntry.COLUMN_AWAY_TEAM, awayID);
        boxscoreIntent.putExtra(StatsContract.StatsEntry.COLUMN_HOME_TEAM, homeID);
        boxscoreIntent.putExtra(StatsContract.StatsEntry.COLUMN_AWAY_RUNS, awayRuns);
        boxscoreIntent.putExtra(StatsContract.StatsEntry.COLUMN_HOME_RUNS, homeRuns);
        return boxscoreIntent;
    }

    public static Intent getTransferIntent(Context context, long updateTime, String statKeeperID) {;
        Intent transferIntent = new Intent(context, FirestoreHelperService.class);
        transferIntent.setAction(FirestoreHelperService.INTENT_TRANSFER_STATS);
        transferIntent.putExtra(FirestoreHelperService.STATKEEPER_ID, statKeeperID);
        transferIntent.putExtra(TimeStampUpdater.UPDATE_TIME, updateTime);
        return transferIntent;
    }

}
