package com.example.android.scorekeepdraft1.activities;

import android.app.Application;

import com.example.android.scorekeepdraft1.objects.MainPageSelection;

/**
 * Created by Eddie on 11/10/2017.
 */

public class MyApp extends Application {

    private MainPageSelection currentSelection;

    public MainPageSelection getCurrentSelection(){
        return currentSelection;
    }
    public void setCurrentSelection(MainPageSelection s){
        currentSelection = s;
    }
}
