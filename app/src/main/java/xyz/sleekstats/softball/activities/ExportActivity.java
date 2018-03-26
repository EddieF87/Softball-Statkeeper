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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import xyz.sleekstats.softball.data.MyFileProvider;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;
import xyz.sleekstats.softball.models.Player;
import xyz.sleekstats.softball.models.Team;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public abstract class ExportActivity extends AppCompatActivity {

    private String leagueName;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int KEY_TEAMS = 0;
    private static final int KEY_PLAYERS = 1;
    private final NumberFormat formatter = new DecimalFormat("#.000");

    public void startExport(String name) {
        leagueName = name;
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
            export();
        } catch (IOException e) {
            Toast.makeText(ExportActivity.this, "FAILURE", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private List<String[]> gatherTeamData() {

        List<String[]> data = new ArrayList<>();

        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                null, null, null, null);

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


    private List<String[]> gatherPlayerData() {

        List<String[]> data = new ArrayList<>();

        Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS,
                null, null, null, null);

        String[] titleArray = new String[]{
                "Name", "Team", "G", "AB",
                "H", "HR", "R", "RBI",
                "AVG", "OBP", "SLG", "OPS",
                "3B", "2B", "1B", "BB",
                "Out", "SF", "Gender"
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
                    outString, sfString, genderString
            };
            data.add(stringArray);
        }
        cursor.close();
        if(data.size() <= 1) {
            return null;
        }
        return data;
    }


    private void export() throws IOException {

        File exportDir = new File(Environment.getExternalStorageDirectory(), "test");
        if (!exportDir.exists()) {
            exportDir.mkdir();
        }
        ArrayList<Uri> uris = new ArrayList<>();

        writeData(exportDir, KEY_TEAMS, uris);
        writeData(exportDir, KEY_PLAYERS, uris);

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
// set the type to 'email'
        emailIntent.setType("vnd.android.cursor.dir/email");
// the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, uris);
// the mail subject
        if (leagueName == null) {
            return;
        }
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, leagueName + " Stats");
        emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    private void writeData(File exportDir, int key, ArrayList<Uri> uris) throws IOException {
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
