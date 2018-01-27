package com.example.android.scorekeepdraft1.activities;

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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.scorekeepdraft1.MyApp;
import com.example.android.scorekeepdraft1.R;
import com.example.android.scorekeepdraft1.data.MyFileProvider;
import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;
import com.example.android.scorekeepdraft1.objects.MainPageSelection;
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

public class ExportActivity extends AppCompatActivity {

    private String leagueName;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int KEY_TEAMS = 0;
    private static final int KEY_PLAYERS = 1;
    private final NumberFormat formatter = new DecimalFormat("#.000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        MyApp myApp = (MyApp) getApplicationContext();
        MainPageSelection mainPageSelection = myApp.getCurrentSelection();
        if (mainPageSelection == null) {
            Intent nullIntent = new Intent(this, MainActivity.class);
            startActivity(nullIntent);
            finish();
        }
        leagueName = mainPageSelection.getName();

        Button exportBtn = findViewById(R.id.btn_export);
        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });
    }

    private void tryExport() {
        try {
            export();
        } catch (IOException e) {
            Toast.makeText(ExportActivity.this, "FAILLLL", Toast.LENGTH_LONG).show();
            Log.e("xxx", e.toString());
            e.printStackTrace();
        }
    }

    private List<String[]> gatherData (int key) {


        List<String[]> data = new ArrayList<>();

        if (key == KEY_TEAMS) {

            Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_TEAMS,
                    null, null, null, null);
            int idIndex = cursor.getColumnIndex(StatsEntry._ID);
            int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            int winIndex = cursor.getColumnIndex(StatsEntry.COLUMN_WINS);
            int lossIndex = cursor.getColumnIndex(StatsEntry.COLUMN_LOSSES);
            int tieIndex = cursor.getColumnIndex(StatsEntry.COLUMN_TIES);
            int runsForIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RUNSFOR);
            int runsAgainstIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RUNSAGAINST);

            String[] titleArray = new String[] {
                    "id", "Name",
                    "Wins", "Losses", "Ties",
                    "Runs Scored", "Runs Allowed"
            };
            data.add(titleArray);

            while (cursor.moveToNext()) {

                String id = String.valueOf(cursor.getLong(idIndex));
                String name = cursor.getString(nameIndex);
                String wins = String.valueOf(cursor.getInt(winIndex));
                String losses = String.valueOf(cursor.getInt(lossIndex));
                String ties = String.valueOf(cursor.getInt(tieIndex));
                String runsFor = String.valueOf(cursor.getInt(runsForIndex));
                String runsAgainst = String.valueOf(cursor.getInt(runsAgainstIndex));

                String[] stringArray = new String[] {
                        id, name, wins, losses, ties, runsFor, runsAgainst
                };
                data.add(stringArray);
            }
            cursor.close();

        } else if (key == KEY_PLAYERS) {

            Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS,
                    null, null, null, null);
            int idIndex = cursor.getColumnIndex(StatsEntry._ID);
            int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
            int teamIndex = cursor.getColumnIndex(StatsEntry.COLUMN_TEAM);
            int hrIndex = cursor.getColumnIndex(StatsEntry.COLUMN_HR);
            int tripleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_3B);
            int doubleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_2B);
            int singleIndex = cursor.getColumnIndex(StatsEntry.COLUMN_1B);
            int bbIndex = cursor.getColumnIndex(StatsEntry.COLUMN_BB);
            int outIndex = cursor.getColumnIndex(StatsEntry.COLUMN_OUT);
            int rbiIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RBI);
            int runIndex = cursor.getColumnIndex(StatsEntry.COLUMN_RUN);
            int sfIndex = cursor.getColumnIndex(StatsEntry.COLUMN_SF);
            int gameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_G);
            int genderIndex = cursor.getColumnIndex(StatsEntry.COLUMN_GENDER);

            String[] titleArray = new String[] {
                    "Name", "Team", "G", "AB",
                    "H", "HR", "R", "RBI",
                    "AVG", "OBP", "SLG", "OPS",
                    "3B", "2B", "1B", "BB",
                    "Out", "SF", "Gender"
            };
            data.add(titleArray);

            while (cursor.moveToNext()) {

                String name = cursor.getString(nameIndex);
                String team = cursor.getString(teamIndex);
                int gender = cursor.getInt(genderIndex);

                int hr =   cursor.getInt(hrIndex);
                int tpl =  cursor.getInt(tripleIndex);
                int dbl =  cursor.getInt(doubleIndex);
                int sgl =  cursor.getInt(singleIndex);
                int bb =   cursor.getInt(bbIndex);
                int out =  cursor.getInt(outIndex);
                int rbi =  cursor.getInt(rbiIndex);
                int run =  cursor.getInt(runIndex);
                int sf =   cursor.getInt(sfIndex);
                int g =    cursor.getInt(gameIndex);
                int hit = sgl + dbl + tpl + hr;
                int ab = hit + out;
                String avg = getAVG(ab, hit);
                double obp = getOBP(ab, hit, bb, sf);
                double slg = getSLG(ab, sgl, dbl, tpl, hr);
                String ops = getOPS(obp, slg);


                String hitString = String.valueOf(hit);
                String abString = String.valueOf(ab);
                String hrString =   String.valueOf(hr);
                String tplString =  String.valueOf(tpl);
                String dblString =  String.valueOf(dbl);
                String sglString =  String.valueOf(sgl);
                String bbString =   String.valueOf(bb);
                String outString =  String.valueOf(out);
                String rbiString =  String.valueOf(rbi);
                String runString =  String.valueOf(run);
                String sfString =   String.valueOf(sf);
                String gameString = String.valueOf(g);

                String obpString;
                String slgString;
                if (slg == -1) {
                    slgString = "";
                    if (obp == -1) {
                        obpString = "";
                    } else {
                        obpString = String.valueOf(formatter.format(obp));
                    }
                } else {
                    slgString = String.valueOf(formatter.format(slg));
                    obpString = String.valueOf(formatter.format(obp));
                }

                String genderString;
                if (gender == 0) {
                    genderString = "M";
                } else {
                    genderString = "F";
                }

                String[] stringArray = new String[] {
                        name, team, gameString, abString,
                        hitString, hrString, runString, rbiString,
                        avg, obpString, slgString, ops,
                        tplString, dblString, sglString, bbString,
                        outString, sfString, genderString
                };
                data.add(stringArray);
            }
            cursor.close();
        }

        return data;
    }

    private void export() throws IOException {

        File exportDir = new File(Environment.getExternalStorageDirectory(), "test");
        if(!exportDir.exists()){
            exportDir.mkdir();
        }

        List<String[]> teamData = gatherData(KEY_TEAMS);

        File teamFile = new File(exportDir, "teams.csv");
        teamFile.createNewFile();

        CSVWriter teamWriter = new CSVWriter(new FileWriter(teamFile));
        teamWriter.writeAll(teamData);
        teamWriter.close();

        Uri pathTeams = MyFileProvider.getUriForFile(this,
                this.getApplicationContext().getPackageName() + ".data.fileprovider", teamFile);


        List<String[]> playerData = gatherData(KEY_PLAYERS);

        File playerFile = new File(exportDir, "players.csv");
        playerFile.createNewFile();

        CSVWriter playerWriter = new CSVWriter(new FileWriter(playerFile));
        playerWriter.writeAll(playerData);
        playerWriter.close();

        Uri pathPlayers = MyFileProvider.getUriForFile(this,
                this.getApplicationContext().getPackageName() + ".data.fileprovider", playerFile);

        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(pathTeams);
        uris.add(pathPlayers);

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
// set the type to 'email'
        emailIntent .setType("vnd.android.cursor.dir/email");
// the attachment
        emailIntent .putExtra(Intent.EXTRA_STREAM, uris);
// the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, leagueName + " Stats");
        emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }


    public String getAVG(int ab, int hit) {
        if (ab == 0) {
            return "";
        }
        return String.valueOf(formatter.format(((double) hit) / ab));
    }

    public double getOBP(int ab, int hit, int bb, int sf) {
        if (ab + bb + sf == 0) {
            return -1;
        }
        return ((double) (hit + bb)
                / (ab + bb + sf));
    }

    public double getSLG(int ab, int sgl, int dbl, int tpl, int hr) {
        if (ab == 0) {
            return -1;
        }
        return (sgl + dbl * 2 + tpl * 3 + hr * 4)
                / ((double) ab);
    }

    public String getOPS(double obp, double slg) {
        if (obp == -1) {
            return "";
        }
        if (slg == -1) {
            slg = 0;
        }
        return String.valueOf(formatter.format(obp + slg));
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
                alertBuilder.setMessage("storage_permission_is_encessary_to_wrote_event");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(ExportActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
                Log.e("", "permission denied, show dialog");
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
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
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
