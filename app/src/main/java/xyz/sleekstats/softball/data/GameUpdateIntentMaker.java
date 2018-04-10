package xyz.sleekstats.softball.data;

import android.content.Context;
import android.content.Intent;

public final class GameUpdateIntentMaker {

    public static Intent getPlayersIntent(Context context, long updateTime,
                                          String statKeeperID, MySyncResultReceiver mReceiver) {

        Intent playersIntent = new Intent(context, FirestoreUpdateService.class);
        playersIntent.setAction(FirestoreUpdateService.INTENT_ADD_PLAYER_STATS);
        playersIntent.putExtra(FirestoreUpdateService.STATKEEPER_ID, statKeeperID);
        playersIntent.putExtra(TimeStampUpdater.UPDATE_TIME, updateTime);
        playersIntent.putExtra(StatsContract.StatsEntry.UPDATE, mReceiver);
        return playersIntent;
    }

    public static Intent getTeamIntent(Context context, long updateTime, String teamID, int runsFor,
                                       int runsAgainst, String statKeeperID, MySyncResultReceiver mReceiver) {

        Intent teamIntent = new Intent(context, FirestoreUpdateService.class);
        teamIntent.setAction(FirestoreUpdateService.INTENT_ADD_TEAM_STATS);
        teamIntent.putExtra(FirestoreUpdateService.STATKEEPER_ID, statKeeperID);
        teamIntent.putExtra(TimeStampUpdater.UPDATE_TIME, updateTime);
        teamIntent.putExtra(StatsContract.StatsEntry.COLUMN_FIRESTORE_ID, teamID);
        teamIntent.putExtra(StatsContract.StatsEntry.COLUMN_RUNSFOR, runsFor);
        teamIntent.putExtra(StatsContract.StatsEntry.COLUMN_RUNSAGAINST, runsAgainst);
        teamIntent.putExtra(StatsContract.StatsEntry.UPDATE, mReceiver);
        return teamIntent;
    }

    public static Intent getBoxscoreIntent(Context context, long updateTime, String awayID, String homeID,
                                           int awayRuns, int homeRuns, String statKeeperID, MySyncResultReceiver mReceiver) {

        Intent boxscoreIntent = new Intent(context, FirestoreUpdateService.class);
        boxscoreIntent.setAction(FirestoreUpdateService.INTENT_ADD_BOXSCORE);
        boxscoreIntent.putExtra(FirestoreUpdateService.STATKEEPER_ID, statKeeperID);
        boxscoreIntent.putExtra(TimeStampUpdater.UPDATE_TIME, updateTime);
        boxscoreIntent.putExtra(StatsContract.StatsEntry.COLUMN_AWAY_TEAM, awayID);
        boxscoreIntent.putExtra(StatsContract.StatsEntry.COLUMN_HOME_TEAM, homeID);
        boxscoreIntent.putExtra(StatsContract.StatsEntry.COLUMN_AWAY_RUNS, awayRuns);
        boxscoreIntent.putExtra(StatsContract.StatsEntry.COLUMN_HOME_RUNS, homeRuns);
        boxscoreIntent.putExtra(StatsContract.StatsEntry.UPDATE, mReceiver);
        return boxscoreIntent;
    }

    public static Intent getTransferIntent(Context context, long updateTime, String awayID, String homeID,
                                           int awayRuns, int homeRuns, String statKeeperID, MySyncResultReceiver mReceiver) {

        Intent transferIntent = new Intent(context, FirestoreUpdateService.class);
        transferIntent.setAction(FirestoreUpdateService.INTENT_TRANSFER_STATS);
        transferIntent.putExtra(FirestoreUpdateService.STATKEEPER_ID, statKeeperID);
        transferIntent.putExtra(TimeStampUpdater.UPDATE_TIME, updateTime);
        transferIntent.putExtra(StatsContract.StatsEntry.COLUMN_AWAY_TEAM, awayID);
        transferIntent.putExtra(StatsContract.StatsEntry.COLUMN_HOME_TEAM, homeID);
        transferIntent.putExtra(StatsContract.StatsEntry.COLUMN_AWAY_RUNS, awayRuns);
        transferIntent.putExtra(StatsContract.StatsEntry.COLUMN_HOME_RUNS, homeRuns);

        transferIntent.putExtra(StatsContract.StatsEntry.UPDATE, mReceiver);
        return transferIntent;
    }

}
