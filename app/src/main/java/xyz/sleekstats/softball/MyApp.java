package xyz.sleekstats.softball;

import android.app.Application;

//import com.squareup.leakcanary.LeakCanary;

import xyz.sleekstats.softball.objects.MainPageSelection;

/**
 * Created by Eddie on 11/10/2017.
 */

public class MyApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        // Normal app init code...
    }

    private MainPageSelection currentSelection;

    public MainPageSelection getCurrentSelection(){
        return currentSelection;
    }

    public void setCurrentSelection(MainPageSelection s){
        currentSelection = s;
    }

}
