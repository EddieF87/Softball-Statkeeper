package com.example.android.scorekeepdraft1.objects;

import android.content.Context;
import android.database.Cursor;

import com.example.android.scorekeepdraft1.data.StatsContract;
import com.example.android.scorekeepdraft1.data.StatsContract.StatsEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eddie on 11/25/2017.
 */

public class LineupEditor {

    private Context mContext;

    public LineupEditor(Context context) {
        super();
        this.mContext = context;
    }

    public static List<Player> genderSort(List<Player> team, int femaleRequired) {
        List<Player> females = new ArrayList<>();
        List<Player> males = new ArrayList<>();
        int femaleIndex = 0;
        int maleIndex = 0;
        int firstFemale = 0;
        boolean firstFemaleSet = false;
        for (Player player : team) {
            //TODO complete gendersort, add info to players and db/firestore
//            if (player.getGender == 0) {
//                females.add(player);
//                firstFemaleSet = true;
//            } else {
//                males.add(player);
//            }
            if (!firstFemaleSet) {
                firstFemale++;
            }
        }
        if (firstFemale >= femaleRequired) {
            firstFemale = femaleRequired - 1;
        }
        for (int i = 0; i < firstFemale; i++) {
            team.add(males.get(maleIndex));
            maleIndex++;
            if (maleIndex >= males.size()) {
                maleIndex = 0;
            }
        }
        for (int i = 0; i < 100; i++) {
            if (i % femaleRequired == 0) {
                team.add(females.get(femaleIndex));
                femaleIndex++;
                if (femaleIndex >= females.size()) {
                    femaleIndex = 0;
                }
            } else {
                team.add(males.get(maleIndex));
                maleIndex++;
                if (maleIndex >= males.size()) {
                    maleIndex = 0;
                }
            }
        }
        return team;
    }


    public List<Player> setTeam(String teamName) {

        String selection = StatsContract.StatsEntry.COLUMN_TEAM + "=?";
        String[] selectionArgs = new String[]{teamName};
        String sortOrder = StatsEntry.COLUMN_ORDER + " ASC";
        Cursor cursor = mContext.getContentResolver().query(StatsEntry.CONTENT_URI_TEMP, null,
                selection, selectionArgs, sortOrder);

        int nameIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME);
        int idIndex = cursor.getColumnIndex(StatsEntry._ID);

        ArrayList<Player> team = new ArrayList<>();
        while (cursor.moveToNext()) {
            int playerId = cursor.getInt(idIndex);
            String playerName = cursor.getString(nameIndex);
            int firestoreIDIndex = cursor.getColumnIndex(StatsEntry.COLUMN_FIRESTORE_ID);
            String firestoreID = cursor.getString(firestoreIDIndex);

            team.add(new Player(playerName, teamName, playerId, firestoreID));
        }
        return team;
    }
}
