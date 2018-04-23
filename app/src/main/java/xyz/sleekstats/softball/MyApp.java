package xyz.sleekstats.softball;

import android.app.Application;

//import com.squareup.leakcanary.LeakCanary;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;
import xyz.sleekstats.softball.objects.MainPageSelection;

/**
 * Created by Eddie on 11/10/2017.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        configureCrashReporting();
    }

    private void configureCrashReporting() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }

    private MainPageSelection currentSelection;

    public MainPageSelection getCurrentSelection(){
        return currentSelection;
    }

    public void setCurrentSelection(MainPageSelection s){
        currentSelection = s;
    }

}
