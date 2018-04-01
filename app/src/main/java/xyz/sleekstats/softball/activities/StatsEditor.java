package xyz.sleekstats.softball.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import xyz.sleekstats.softball.R;
import xyz.sleekstats.softball.data.StatsContract;
import xyz.sleekstats.softball.data.StatsContract.StatsEntry;

public class StatsEditor extends AppCompatActivity {

    private RadioGroup group1;
    private RadioGroup group2;
    private String result;
    private int resultCount;
    private TextView resultText;
    private TextView resultCountText;

    private String mPlayerID;
    private String mGameID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_editor);

        View playerManager = findViewById(R.id.player_mgr);
        playerManager.setVisibility(View.VISIBLE);
        setRadioButtons(playerManager);

        resultCount = 0;
        resultCountText = playerManager.findViewById(R.id.textview_result_count);
        resultText = playerManager.findViewById(R.id.textview_result_chosen);
        Button submitBtn = playerManager.findViewById(R.id.submit);
        ImageView addBtn = playerManager.findViewById(R.id.btn_add_result);
        ImageView subtractBtn = playerManager.findViewById(R.id.btn_subtract_result);

        subtractBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultCount--;
                resultCountText.setText(String.valueOf(resultCount));
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultCount++;
                resultCountText.setText(String.valueOf(resultCount));
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (result == null) {
                    Toast.makeText(StatsEditor.this, "Please select a result first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String statEntry;
                switch (result) {
                    case "1B":
                        statEntry = StatsEntry.COLUMN_1B;
                        break;
                    case "2B":
                        statEntry = StatsEntry.COLUMN_2B;
                        break;
                    case "3B":
                        statEntry = StatsEntry.COLUMN_3B;
                        break;
                    case "HR":
                        statEntry = StatsEntry.COLUMN_HR;
                        break;
                    case "BB":
                        statEntry = StatsEntry.COLUMN_BB;
                        break;
                    case "Out":
                        statEntry = StatsEntry.COLUMN_OUT;
                        break;
                    case "SF":
                        statEntry = StatsEntry.COLUMN_SF;
                        break;
                    case "Run":
                        statEntry = StatsEntry.COLUMN_RUN;
                        break;
                    case "RBI":
                        statEntry = StatsEntry.COLUMN_RBI;
                        break;
                    default:
                        return;
                }

                String playerSelection = StatsEntry.COLUMN_FIRESTORE_ID + "=?";
                String[] playerSelectionArgs = new String[]{mPlayerID};

                String gameSelection = StatsEntry.COLUMN_FIRESTORE_ID + "=? AND " + StatsEntry.COLUMN_GAME_ID + "=?";
                String[] gameSelectionArgs = new String[]{mPlayerID, mGameID};

                Cursor cursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS,
                        null, null, null, null);
                if (cursor.moveToFirst()) {
                    int currentResultCount = StatsContract.getColumnInt(cursor, statEntry);
                    resultCount += currentResultCount;
                    ContentValues values = new ContentValues();
                    values.put(statEntry, resultCount);
                    getContentResolver().update(StatsEntry.CONTENT_URI_PLAYERS,
                            values, playerSelection,  playerSelectionArgs);

                }

                cursor = getContentResolver().query(StatsEntry.CONTENT_URI_PLAYERS,
                        null, null, null, null);
                if (cursor.moveToFirst()) {
                    int currentResultCount = StatsContract.getColumnInt(cursor, statEntry);
                    resultCount += currentResultCount;
                    ContentValues values = new ContentValues();
                    values.put(statEntry, resultCount);
                    getContentResolver().update(StatsEntry.CONTENT_URI_BOXSCORE_PLAYERS,
                            values, playerSelection,  playerSelectionArgs);

                }
                resultCount = 0;
                resultCountText.setText(String.valueOf(0));
            }
        });
    }



    private void setRadioButtons(View view) {
        group1 = view.findViewById(R.id.group1);
        group2 = view.findViewById(R.id.group2);
        RadioButton single = view.findViewById(R.id.single_rb);
        RadioButton dbl = view.findViewById(R.id.dbl_rb);
        RadioButton triple = view.findViewById(R.id.triple_rb);
        RadioButton hr = view.findViewById(R.id.hr_rb);
        RadioButton bb = view.findViewById(R.id.bb_rb);
        RadioButton out = view.findViewById(R.id.out_rb);
        RadioButton sf = view.findViewById(R.id.sf_rb);
        RadioButton run = view.findViewById(R.id.run_rb);
        RadioButton rbi = view.findViewById(R.id.rbi_rb);
        single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group2.clearCheck();
                    result = "1B";
                    resultText.setText(result);
                }
            }
        });
        dbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group2.clearCheck();
                    result = "2B";
                    resultText.setText(result);
                }
            }
        });
        triple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group2.clearCheck();
                    result = "3B";
                    resultText.setText(result);
                }
            }
        });
        hr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group2.clearCheck();
                    result = "HR";
                    resultText.setText(result);
                }
            }
        });
        bb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group1.clearCheck();
                    result = "BB";
                    resultText.setText(result);
                }
            }
        });
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group1.clearCheck();
                    result = "Out";
                    resultText.setText(result);
                }
            }
        });
        sf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group1.clearCheck();
                    result = "SF";
                    resultText.setText(result);
                }
            }
        });
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group1.clearCheck();
                    result = "Run";
                    resultText.setText(result);
                }
            }
        });
        rbi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((RadioButton) view).isChecked();
                if (checked) {
                    group1.clearCheck();
                    result = "RBI";
                    resultText.setText(result);
                }
            }
        });
    }


}
