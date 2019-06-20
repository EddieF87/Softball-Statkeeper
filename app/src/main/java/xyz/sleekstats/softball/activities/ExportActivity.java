package xyz.sleekstats.softball.activities;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import xyz.sleekstats.softball.MyApp;
import xyz.sleekstats.softball.data.MyFileProvider;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.models.MainPageSelection;
import xyz.sleekstats.softball.models.Player;
import xyz.sleekstats.softball.models.Team;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public abstract class ExportActivity extends AppCompatActivity {

    private String emailSubject;
    private String mStatKeeperID;
    private long mGameID;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int KEY_TEAMS = 0;
    private static final int KEY_PLAYERS = 1;
    private static final NumberFormat formatter = new DecimalFormat("#.000");
    private static final int KEY_LEAGUE = 10;
    private static final int KEY_BOXSCORE = 11;
    private int exportType;


    public void startLeagueExport(String name) {
        emailSubject = name;
        exportType = KEY_LEAGUE;
        if (!checkPermission()) {
            tryExport();
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                tryExport();
            }
        }
    }

    void startBoxscoreExport(long id) {
        mGameID = id;
        exportType = KEY_BOXSCORE;
        if (!checkPermission()) {
            tryExport();
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                tryExport();
            }
        }
    }

    private void tryExport() {
        try {
            MyApp myApp = (MyApp) getApplicationContext();
            MainPageSelection mainPageSelection = myApp.getCurrentSelection();
            mStatKeeperID = mainPageSelection.getId();
            if(exportType == KEY_LEAGUE) {
                exportLeague();
            } else if (exportType == KEY_BOXSCORE){
                exportBoxscore();
            }
        } catch (IOException e) {
            Toast.makeText(ExportActivity.this, "ERROR WITH EXPORT", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private List<String[]> gatherTeamData() {

        List<String[]> data = new ArrayList<>();

        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mStatKeeperID};
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                null, selection, selectionArgs, null);

        String[] titleArray = new String[]{
                "Name", "Win %",
                "Wins", "Losses", "Ties",
                "Runs Scored", "Runs Allowed", "Run Differential"
        };
        data.add(titleArray);

        while (cursor.moveToNext()) {
            Team team = new Team(cursor);

            String name = team.getName();
            String ties = String.valueOf(team.getTies());
            String runDiff = String.valueOf(team.getRunDifferential());
            String runs = String.valueOf(team.getTotalRunsScored());
            String runsAllowed = String.valueOf(team.getTotalRunsAllowed());

            int wins = team.getWins();
            int losses = team.getLosses();
            String winString = String.valueOf(wins);
            String lossString = String.valueOf(losses);

            double winPct = team.getWinPct();
            String winPctString;
            if (wins + losses <= 0) {
                winPctString = "";
            } else {
                winPctString = String.valueOf(formatter.format(winPct));
            }

            String[] stringArray = new String[]{
                    name, winPctString, winString, lossString, ties, runs, runsAllowed, runDiff
            };
            data.add(stringArray);
        }
        cursor.close();
        if(data.size() <= 1) {
            return null;
        }
        return data;
    }

    private List<String[]> gatherBoxscoreData() {

        String dateString = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(mGameID);

        List<String[]> data = new ArrayList<>();

        String selection = StatsEntry.COLUMN_GAME_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[] {String.valueOf(mGameID), mStatKeeperID};
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_BOXSCORE_OVERVIEWS,
                null, selection, selectionArgs, null);

        String awayTeamID;
        String homeTeamID;
        int awayRuns;
        int homeRuns;
        if (cursor.moveToFirst()){
            awayTeamID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_AWAY_TEAM);
            homeTeamID = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_HOME_TEAM);
            awayRuns = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HOME_RUNS);
            homeRuns = StatsContract.getColumnInt(cursor, StatsEntry.COLUMN_HOME_RUNS);
        } else {
            cursor.close();
            return null;
        }
        String awayTeamName = null;
        String homeTeamName = null;
        selection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        selectionArgs = new String[]{awayTeamID, mStatKeeperID};
        cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                null, selection, selectionArgs, null);
        if(cursor.moveToFirst()) {
            awayTeamName = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_NAME);
        }
        selectionArgs = new String[]{homeTeamID, mStatKeeperID};
        cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                null, selection, selectionArgs, null);
        if(cursor.moveToFirst()) {
            homeTeamName = StatsContract.getColumnString(cursor, StatsEntry.COLUMN_NAME);
        }
        emailSubject = dateString + ":  " + awayTeamName + " " + awayRuns + " - " +  homeTeamName + " " + homeRuns;
        data.add(new String[]{emailSubject});
        data.add(new String[]{});
        addToBoxscoreData(data, awayTeamID);
        data.add(new String[]{});
        addToBoxscoreData(data, homeTeamID);
        if(data.size() <= 1) {
            return null;
        }
        return data;

    }

    private void addToBoxscoreData(List<String[]> data, String teamID){

        String[] titleArray = new String[]{
                "Name", "AB", "R",
                "H",  "RBI","HR",
                "3B", "2B", "1B", "BB",
                "SF", "Out", "SB", "K", "HBP", "ROE"
        };
        data.add(titleArray);

        String selection = StatsEntry.COLUMN_TEAM_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[] {teamID, mStatKeeperID};

        Cursor playerCursor = getContentResolver().query(StatsEntry.CONTENT_URI_BOXSCORE_PLAYERS,
                null, selection, selectionArgs, null);

        while (playerCursor.moveToNext()) {

            String playerID = StatsContract.getColumnString(playerCursor, StatsEntry.COLUMN_FIRESTORE_ID);
            int hr =  StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_HR);
            int tpl = StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_3B);
            int dbl = StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_2B);
            int sgl = StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_1B);
            int bb =  StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_BB);
            int out = StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_OUT);
            int rbi = StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_RBI);
            int run = StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_RUN);
            int sf =  StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_SF);
            int sb =  StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_SB);
            int k =  StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_K);
            int hbp =  StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_HBP);
            int roe =  StatsContract.getColumnInt(playerCursor, StatsEntry.COLUMN_ROE);

            int hit = hr + tpl + dbl + sgl;
            int ab =  hit + out;


            String hitString = String.valueOf(hit);
            String abString = String.valueOf(ab);
            String hrString = String.valueOf(hr);
            String tplString = String.valueOf(tpl);
            String dblString = String.valueOf(dbl);
            String sglString = String.valueOf(sgl);
            String bbString = String.valueOf(bb);
            String outString = String.valueOf(out);
            String rbiString = String.valueOf(rbi);
            String runString = String.valueOf(run);
            String sfString = String.valueOf(sf);
            String sbString = String.valueOf(sb);
            String kString = String.valueOf(k);
            String hbpString = String.valueOf(hbp);
            String roeString = String.valueOf(roe);

            String nameSelection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_LEAGUE_ID + "=?";
            String[] nameSelectionArgs = new String[] {playerID, mStatKeeperID};
            String[] nameProjection = new String[] {StatsEntry.COLUMN_NAME};

            String nameString;
            Cursor nameCursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS,
                    nameProjection, nameSelection, nameSelectionArgs, null);
            if(nameCursor.moveToFirst()){
                nameString = StatsContract.getColumnString(nameCursor, StatsEntry.COLUMN_NAME);
            } else {
                nameString = "";
            }

            String[] stringArray = new String[]{
                    nameString, abString, runString,
                    hitString, rbiString, hrString,
                    tplString, dblString, sglString, bbString,
                    sfString, outString, sbString, kString, hbpString, roeString
            };
            data.add(stringArray);
        }
        playerCursor.close();
    }

    private List<String[]> gatherPlayerData() {

        List<String[]> data = new ArrayList<>();

        String selection = StatsEntry.COLUMN_LEAGUE_ID + "=?";
        String[] selectionArgs = new String[]{mStatKeeperID};
        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS,
                null, selection, selectionArgs, null);

        String[] titleArray = new String[]{
                "Name", "Team", "G", "AB",
                "H", "HR", "R", "RBI",
                "AVG", "OBP", "SLG", "OPS",
                "3B", "2B", "1B", "BB",
                "Out", "SF", "SB", "K", "HBP", "ROE", "Gender"
        };
        data.add(titleArray);

        while (cursor.moveToNext()) {
            Player player = new Player(cursor, false);

            String nameString = player.getName();
            String teamString = player.getTeam();
            int gender = player.getGender();
            int hr = player.getHrs();
            int tpl = player.getTriples();
            int dbl = player.getDoubles();
            int sgl = player.getSingles();
            int bb = player.getWalks();
            int out = player.getOuts();
            int rbi = player.getRbis();
            int run = player.getRuns();
            int sf = player.getSacFlies();
            int sb = player.getStolenBases();
            int k = player.getStrikeouts();
            int hbp = player.getHbp();
            int roe = player.getRoe();
            int g = player.getGames();
            int hit = player.getHits();
            int ab = player.getABs();
            double avg = player.getAVG();
            double obp = player.getOBP();
            double slg = player.getSLG();
            double ops = player.getOPS();

            String hitString = String.valueOf(hit);
            String abString = String.valueOf(ab);
            String hrString = String.valueOf(hr);
            String tplString = String.valueOf(tpl);
            String dblString = String.valueOf(dbl);
            String sglString = String.valueOf(sgl);
            String bbString = String.valueOf(bb);
            String outString = String.valueOf(out);
            String rbiString = String.valueOf(rbi);
            String runString = String.valueOf(run);
            String sfString = String.valueOf(sf);
            String sbString = String.valueOf(sb);
            String kString = String.valueOf(k);
            String hbpString = String.valueOf(hbp);
            String roeString = String.valueOf(roe);
            String gameString = String.valueOf(g);

            String avgString;
            String obpString;
            String slgString;
            String opsString;
            if (ab <= 0) {
                avgString = "";
                slgString = "";
                if (bb + sf <= 0) {
                    obpString = "";
                    opsString = "";
                } else {
                    obpString = String.valueOf(formatter.format(obp));
                    opsString = String.valueOf(formatter.format(ops));
                }
            } else {
                avgString = String.valueOf(formatter.format(avg));
                obpString = String.valueOf(formatter.format(obp));
                slgString = String.valueOf(formatter.format(slg));
                opsString = String.valueOf(formatter.format(ops));
            }

            String genderString;
            if (gender == 0) {
                genderString = "M";
            } else {
                genderString = "F";
            }

            String[] stringArray = new String[]{
                    nameString, teamString, gameString, abString,
                    hitString, hrString, runString, rbiString,
                    avgString, obpString, slgString, opsString,
                    tplString, dblString, sglString, bbString,
                    outString, sfString, sbString, kString, hbpString, roeString, genderString
            };
            data.add(stringArray);
        }
        cursor.close();
        if(data.size() <= 1) {
            return null;
        }
        return data;
    }


    private void exportLeague() throws IOException {

        File exportDir = new File(Environment.getExternalStorageDirectory(), "sleekstats");
        if (!exportDir.exists()) {
            exportDir.mkdir();
        }
        ArrayList<Uri> uris = new ArrayList<>();

        writeLeagueData(exportDir, KEY_TEAMS, uris);
        writeLeagueData(exportDir, KEY_PLAYERS, uris);

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
// set the type to 'email'
        emailIntent.setType("vnd.android.cursor.dir/email");
// the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, uris);
// the mail subject
        if (emailSubject == null) {
            return;
        }
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject + " Stats");
        emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    private void exportBoxscore() throws IOException {

        File exportDir = new File(Environment.getExternalStorageDirectory(), "sleekstats");
        if (!exportDir.exists()) {
            exportDir.mkdir();
        }

        File file = new File(exportDir, "boxscore.csv");
        List<String[]> data = gatherBoxscoreData();
        file.createNewFile();

        CSVWriter csvWriter = new CSVWriter(new FileWriter(file));
        csvWriter.writeAll(data);
        csvWriter.close();

        Uri path = MyFileProvider.getUriForFile(this,
                this.getApplicationContext().getPackageName() + ".data.fileprovider", file);

        Intent emailIntent = new Intent(Intent.ACTION_SEND );
// set the type to 'email'
        emailIntent.setType("vnd.android.cursor.dir/email");
// the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
// the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    private void writeLeagueData(File exportDir, int key, ArrayList<Uri> uris) throws IOException {
        File file;
        List<String[]> data;
        if (key == KEY_PLAYERS) {
            file = new File(exportDir, "players.csv");
            data = gatherPlayerData();
        } else {
            file = new File(exportDir, "teams.csv");
            data = gatherTeamData();
        }
        if(data == null) {
            return;
        }
        file.createNewFile();

        CSVWriter csvWriter = new CSVWriter(new FileWriter(file));
        csvWriter.writeAll(data);
        csvWriter.close();

        Uri path = MyFileProvider.getUriForFile(this,
                this.getApplicationContext().getPackageName() + ".data.fileprovider", file);
        uris.add(path);
    }


    private boolean checkPermission() {

        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ;
    }

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("permission_necessary");
                alertBuilder.setMessage("storage_permission_is_necessary_to_write_event");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(ExportActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(ExportActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        } else {
            tryExport();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults.length > 0) {
                boolean flag = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        flag = false;
                    }
                }
                if (flag) {
                    tryExport();
                } else {
                    finish();
                }
            } else {
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
