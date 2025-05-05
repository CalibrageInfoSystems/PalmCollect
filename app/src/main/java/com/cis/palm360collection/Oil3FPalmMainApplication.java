package com.cis.palm360collection;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import com.cis.palm360collection.cloudhelper.ApplicationThread;
import com.cis.palm360collection.cloudhelper.Config;

public class Oil3FPalmMainApplication extends Application {

    private static Oil3FPalmMainApplication thisInstance;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressWarnings("unused")
    @Override
    public void onCreate() {

        if (Config.DEVELOPER_MODE   && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
        }
        super.onCreate();
        thisInstance=this;
       // Fabric.with(this, new Crashlytics());
        ApplicationThread.start();
        Config.initialize();

//        Picasso.Builder builder = new Picasso.Builder(this);
//        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
//        Picasso built = builder.build();
//        built.setIndicatorsEnabled(true);
//        built.setLoggingEnabled(true);
//        Picasso.setSingletonInstance(built);

    }

    public void onTerminate() {
        ApplicationThread.stop();
        super.onTerminate();
    }

    public static synchronized Oil3FPalmMainApplication getInstance() {
        return thisInstance;
    }

}

