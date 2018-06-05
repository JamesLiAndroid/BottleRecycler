package com.incomrecycle.prms.rvm;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import com.incomrecycle.common.init.LoggerInit;

public class RVMApplication extends Application {
    private static Application gApplication = null;

    public RVMApplication() {
        gApplication = this;
    }

    public void onCreate() {
        super.onCreate();
        init(this);
        CrashHandler.getInstance().init(getApplicationContext());
    }

    private void init(Context context) {
        StrictMode.setThreadPolicy(new Builder().permitAll().build());
        new LoggerInit().Init(context, null);
    }

    public static Application getApplication() {
        return gApplication;
    }

    public void onLowMemory() {
        System.gc();
        super.onLowMemory();
    }
}
