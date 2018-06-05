package com.incomrecycle.prms.rvm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import com.google.code.microlog4android.format.SimpleFormatter;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

public class CrashHandler implements UncaughtExceptionHandler {
    private static final String CRASH_REPORTER_EXTENSION = ".cr";
    public static final boolean DEBUG = true;
    private static CrashHandler INSTANCE = null;
    private static final String STACK_TRACE = "STACK_TRACE";
    public static final String TAG = "CrashHandler";
    private static final String VERSION_CODE = "versionCode";
    private static final String VERSION_NAME = "versionName";
    private Context mContext;
    private UncaughtExceptionHandler mDefaultHandler;
    private Properties mDeviceCrashInfo = new Properties();

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    public void init(Context ctx) {
        this.mContext = ctx;
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        if (handleException(ex) || this.mDefaultHandler == null) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error : ", e);
            }
            GUIGlobal.exit();
            return;
        }
        this.mDefaultHandler.uncaughtException(thread, ex);
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            Log.w(TAG, "handleException --- ex==null");
            return true;
        }
        Log.w(TAG, "handleException", ex);
        final String msg = ex.getLocalizedMessage();
        if (msg == null) {
            return false;
        }
        new Thread() {
            public void run() {
                Looper.prepare();
                Toast toast = Toast.makeText(CrashHandler.this.mContext, "Internal Error:\r\n" + msg, Toast.LENGTH_LONG);
                toast.setGravity(17, 0, 0);
                toast.show();
                Looper.loop();
            }
        }.start();
        collectCrashDeviceInfo(this.mContext);
        saveCrashInfoToFile(ex);
        return true;
    }

    public void sendPreviousReportsToServer() {
        sendCrashReportsToServer(this.mContext);
    }

    private void sendCrashReportsToServer(Context ctx) {
        String[] crFiles = getCrashReportFiles(ctx);
        if (crFiles != null && crFiles.length > 0) {
            TreeSet<String> sortedFiles = new TreeSet();
            sortedFiles.addAll(Arrays.asList(crFiles));
            Iterator it = sortedFiles.iterator();
            while (it.hasNext()) {
                File cr = new File(ctx.getFilesDir(), (String) it.next());
                postReport(cr);
                cr.delete();
            }
        }
    }

    private void postReport(File file) {
    }

    private String[] getCrashReportFiles(Context ctx) {
        return ctx.getFilesDir().list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(CrashHandler.CRASH_REPORTER_EXTENSION);
            }
        });
    }

    private String saveCrashInfoToFile(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);
        for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
            cause.printStackTrace(printWriter);
        }
        String result = info.toString();
        printWriter.close();
        this.mDeviceCrashInfo.put("EXEPTION", ex.getLocalizedMessage());
        this.mDeviceCrashInfo.put(STACK_TRACE, result);
        try {
            Time t = new Time("GMT+8");
            int time = ((t.hour * 10000) + (t.minute * 100)) + t.second;
            String fileName = "crash-" + (((t.year * 10000) + (t.month * 100)) + t.monthDay) + SimpleFormatter.DEFAULT_DELIMITER + time + CRASH_REPORTER_EXTENSION;
            FileOutputStream trace = this.mContext.openFileOutput(fileName, 0);
            this.mDeviceCrashInfo.store(trace, "");
            trace.flush();
            trace.close();
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing report file...", e);
            return null;
        }
    }

    public void collectCrashDeviceInfo(Context ctx) {
        try {
            PackageInfo pi = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
            if (pi != null) {
                this.mDeviceCrashInfo.put(VERSION_NAME, pi.versionName == null ? "not set" : pi.versionName);
                this.mDeviceCrashInfo.put(VERSION_CODE, "" + pi.versionCode);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Error while collect package info", e);
        }
        for (Field field : Build.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                this.mDeviceCrashInfo.put(field.getName(), "" + field.get(null));
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e2) {
                Log.e(TAG, "Error while collect crash info", e2);
            }
        }
    }
}
