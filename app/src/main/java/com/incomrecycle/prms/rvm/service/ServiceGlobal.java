package com.incomrecycle.prms.rvm.service;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.event.EventMgr;
import com.incomrecycle.common.queue.FIFOQueue;
import com.incomrecycle.common.sqlite.DatabaseHelper;
import com.incomrecycle.common.sqlite.DatabaseInitCallback;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.RVMApplication;
import com.incomrecycle.prms.rvm.service.dbcallback.LOGDBInitCallback;
import com.incomrecycle.prms.rvm.service.dbcallback.RVMDBInitCallback;
import com.incomrecycle.prms.rvm.service.dbcallback.SYSDBInitCallback;
import java.io.File;
import java.util.HashMap;

public class ServiceGlobal {
    private static final FIFOQueue commEventFIFOQueue = new FIFOQueue();
    private static DatabaseHelper configDatabaseHelper = null;
    private static final FIFOQueue guiEventFIFOQueue = new FIFOQueue();
    private static final EventMgr guiEventMgr = new EventMgr();
    private static final HashMap hsmpCurrentSession = new HashMap();
    private static final HashMap<String, HashMap<String, DatabaseHelper>> hsmpDatabaseHelper = new HashMap();

    public static DatabaseHelper getDatabaseHelper(String schema) {
        DatabaseHelper databaseHelper;
        synchronized (hsmpDatabaseHelper) {
            String str = SysConfig.get("RVM.CODE");
            str = "INCOM";
            HashMap<String, DatabaseHelper> hsmp = (HashMap) hsmpDatabaseHelper.get(str);
            if (hsmp == null) {
                hsmp = new HashMap();
                hsmpDatabaseHelper.put(str, hsmp);
            }
            schema = schema.toUpperCase();
            databaseHelper = (DatabaseHelper) hsmp.get(schema);
            if (databaseHelper == null) {
                DatabaseInitCallback databaseInitCallback = null;
                if (schema.equalsIgnoreCase("SYS")) {
                    databaseInitCallback = new SYSDBInitCallback();
                }
                if (schema.equalsIgnoreCase("LOG")) {
                    databaseInitCallback = new LOGDBInitCallback();
                }
                if (schema.equalsIgnoreCase("RVM")) {
                    databaseInitCallback = new RVMDBInitCallback();
                }
                String databasePath = SysConfig.get("DB.PATH");
                if (StringUtils.isBlank(databasePath)) {
                    databasePath = "";
                } else {
                    if (!databasePath.endsWith("/")) {
                        databasePath = databasePath + "/";
                    }
                    new File(databasePath).mkdirs();
                }
                databaseHelper = new DatabaseHelper(RVMApplication.getApplication().getApplicationContext(), databasePath + str + "." + schema, Integer.parseInt(SysConfig.get("DB.VERSION")), databaseInitCallback);
                hsmp.put(schema, databaseHelper);
            }
        }
        return databaseHelper;
    }

    public static EventMgr getGUIEventMgr() {
        return guiEventMgr;
    }

    public static FIFOQueue getGUIEventQueye() {
        return guiEventFIFOQueue;
    }

    public static FIFOQueue getCommEventQueye() {
        return commEventFIFOQueue;
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

    public static boolean hasPackage(String packageName) {
        if (StringUtils.isBlank(packageName)) {
            return false;
        }
        try {
            if (RVMApplication.getApplication().getPackageManager().getApplicationInfo(packageName, 0) != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
