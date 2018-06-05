package com.incomrecycle.prms.rvm.gui;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import com.incomrecycle.common.event.EventMgr;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.prms.rvm.RVMApplication;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.camera.CameraManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GUIGlobal {
    private static final String TAG = "GUIGlobal";
    private static final HashMap<String, BaseActivity> hsmpBaseActivity = new HashMap();
    private static final HashMap hsmpCurrentSession = new HashMap();
    private static boolean isGUIReady = false;
    private static final List<String> listTopBaseActivity = new ArrayList();
    private static final EventMgr mgr = new EventMgr();

    public static EventMgr getEventMgr() {
        return mgr;
    }

    public static void setGUIReady(boolean isGUIReady) {
        isGUIReady = isGUIReady;
    }

    public static boolean isGUIReady() {
        return isGUIReady;
    }

    public static BaseActivity getCurrentBaseActivity() {
        List<RunningTaskInfo> RunningActivityList = ((ActivityManager) RVMApplication.getApplication().getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1);
        if (RunningActivityList != null && RunningActivityList.size() > 0) {
            ComponentName cn = ((RunningTaskInfo) RunningActivityList.get(0)).topActivity;
            synchronized (hsmpBaseActivity) {
                for (BaseActivity baseActivity : hsmpBaseActivity.values()) {
                    if (baseActivity != null && baseActivity.getComponentName().equals(cn)) {
                        return baseActivity;
                    }
                }
            }
        }
        return null;
    }

    public static void exit() {
        BaseActivity RVMMain = (BaseActivity) hsmpBaseActivity.get("RVMMainActivity");
        HashMap<String, BaseActivity> hsmpBaseActivity = new HashMap();
        hsmpBaseActivity.putAll(hsmpBaseActivity);
        for (BaseActivity baseActivity : hsmpBaseActivity.values()) {
            if (!(baseActivity == null || baseActivity == RVMMain)) {
                try {
                    baseActivity.finish();
                } catch (Exception e) {
                }
            }
        }
        if (RVMMain != null) {
            try {
                RVMMain.finish();
            } catch (Exception e2) {
            }
        }
        CameraManager.clearCameraManager();
        Log.w(TAG, "RVM Exit on " + DateUtils.formatDatetime(new Date(), "yyyy-MM-dd HH:mm:ss"));
        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    public static BaseActivity getBaseActivity(String name) {
        if (name == null) {
            return null;
        }
        BaseActivity baseActivity;
        synchronized (hsmpBaseActivity) {
            baseActivity = (BaseActivity) hsmpBaseActivity.get(name);
        }
        return baseActivity;
    }

    public static void setBaseActivity(String name, BaseActivity baseActivity) {
        synchronized (hsmpBaseActivity) {
            if (baseActivity == null) {
                hsmpBaseActivity.remove(name);
                synchronized (listTopBaseActivity) {
                    if (listTopBaseActivity.contains(name)) {
                        listTopBaseActivity.remove(name);
                    }
                }
            } else {
                hsmpBaseActivity.put(name, baseActivity);
            }
        }
    }

    public static void setTopBaseActivity(String name) {
        synchronized (listTopBaseActivity) {
            if (listTopBaseActivity.contains(name)) {
                listTopBaseActivity.remove(name);
            }
            listTopBaseActivity.add(name);
        }
    }

    public static String getTopBaseActivity(int n) {
        String str;
        synchronized (listTopBaseActivity) {
            if (n >= listTopBaseActivity.size()) {
                str = null;
            } else {
                str = (String) listTopBaseActivity.get(listTopBaseActivity.size() - (n + 1));
            }
        }
        return str;
    }

    public static void updateLanguage() {
        synchronized (hsmpBaseActivity) {
            for (String name : hsmpBaseActivity.keySet()) {
                BaseActivity baseActivity = (BaseActivity) hsmpBaseActivity.get(name);
                if (baseActivity != null) {
                    baseActivity.doUpdateLanguage();
                }
            }
        }
    }

    public static Locale getLocale(Application application) {
        Configuration config = application.getResources().getConfiguration();
        if (config.locale == null) {
            return Locale.getDefault();
        }
        return config.locale;
    }

    public static void updateLanguage(Application application, Locale locale) {
        Locale.setDefault(locale);
        Resources resource = application.getResources();
        Configuration config = resource.getConfiguration();
        DisplayMetrics metrics = resource.getDisplayMetrics();
        config.locale = locale;
        resource.updateConfiguration(config, metrics);
        updateLanguage();
    }

    public static void setCurrentSession(String name, Object obj) {
        synchronized (hsmpCurrentSession) {
            hsmpCurrentSession.put(name, obj);
        }
    }

    public static Object getCurrentSession(String name) {
        Object obj;
        synchronized (hsmpCurrentSession) {
            obj = hsmpCurrentSession.get(name);
        }
        return obj;
    }

    public static void clearCurrentSession() {
        synchronized (hsmpCurrentSession) {
            hsmpCurrentSession.clear();
        }
    }

    public static void RemoveCurrentSession(String name) {
        synchronized (hsmpCurrentSession) {
            hsmpCurrentSession.remove(name);
        }
    }

    public static void clearMap() {
        synchronized (hsmpCurrentSession) {
            if (((HashMap) getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT)) != null) {
                hsmpCurrentSession.remove(AllAdvertisement.HOMEPAGE_LEFT);
            }
            if (((HashMap) getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG)) != null) {
                hsmpCurrentSession.remove(AllAdvertisement.VENDING_SELECT_FLAG);
            }
        }
    }
}
