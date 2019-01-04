package xyz.sleekstats.softball;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;
import xyz.sleekstats.softball.models.MainPageSelection;

//import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Eddie on 11/10/2017.
 */

public class MyApp extends MultiDexApplication {

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
