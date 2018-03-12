package com.example.android.softballstatkeeper;

import android.app.Application;
import android.content.Context;

import com.example.android.softballstatkeeper.objects.MainPageSelection;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

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


    public static RefWatcher getRefWatcher(Context context) {
        MyApp application = (MyApp) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;
    @Override public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
        // Normal app init code...
    }
}
