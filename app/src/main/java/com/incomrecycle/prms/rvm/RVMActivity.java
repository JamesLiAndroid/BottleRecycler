package com.incomrecycle.prms.rvm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.widget.TextView;
import com.google.code.microlog4android.appender.DatagramAppender;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.CrontabThread;
import com.incomrecycle.common.task.DelayTask;
import com.incomrecycle.common.task.InstanceTask;
import com.incomrecycle.common.task.TickTaskThread;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.ShellUtils;
import com.incomrecycle.common.utils.SocketUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.RVMShell;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.init.GUIInit;
import com.incomrecycle.prms.rvm.service.init.ServiceInit;
import com.incomrecycle.prms.rvmdaemon.RVMDaemonClient;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class RVMActivity extends BaseActivity {
    private static boolean hasInited = false;
    boolean enableEvent = true;

    public void checkOnStart() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_rvm);
        SysGlobal.execute(new Thread() {
            public void run() {
                if (RVMActivity.hasInited) {
                    RVMActivity.this.executeGUIAction(true, new GUIAction() {
                        protected void doAction(Object[] paramObjs) {
                            BaseActivity baseActivity = null;
                            int idx = 0;
                            while (true) {
                                String baseActivityName = GUIGlobal.getTopBaseActivity(idx);
                                if (baseActivityName == null) {
                                    break;
                                }
                                if (baseActivity == null && !RVMActivity.this.getName().equals(baseActivityName)) {
                                    baseActivity = GUIGlobal.getBaseActivity(baseActivityName);
                                }
                                if (baseActivity != null) {
                                    break;
                                }
                                idx++;
                            }
                            Intent intent;
                            if (baseActivity != null) {
                                intent = new Intent(RVMActivity.this, baseActivity.getClass());
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                RVMActivity.this.startActivity(intent);
                            } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                                try {
                                    intent = new Intent(RVMActivity.this, Class.forName(SysConfig.get("RVMMActivity.class")));
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    RVMActivity.this.startActivity(intent);
                                    RVMActivity.this.finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            RVMActivity.this.finish();
                        }
                    }, null);
                    return;
                }
                RVMActivity.hasInited = true;
                RVMActivity.this.init();
                RVMActivity.this.enableEvent = false;
                RVMActivity.this.executeGUIAction(true, new GUIAction() {
                    protected void doAction(Object[] paramObjs) {
                        GUIGlobal.setGUIReady(true);
                        if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                            try {
                                Intent intent = new Intent(RVMActivity.this, Class.forName(SysConfig.get("RVMMActivity.class")));
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                RVMActivity.this.startActivity(intent);
                                RVMActivity.this.finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, null);
            }
        });
    }

    private void init() {
        String[] fields;
        int i;
        byte[] data;
        StrictMode.setThreadPolicy(new Builder().permitAll().build());
        SysConfig.init(null);
        String appDir = SysConfig.get("APP.DIR");
        File fileTest = new File(appDir);
        if (!fileTest.exists()) {
            int checkTimes = 0;
            while (true) {
                checkTimes++;
                String dfResult = ShellUtils.shell("df");
                if ((StringUtils.isBlank(dfResult) || dfResult.replaceAll("\t", " ").indexOf("/sdcard ") == -1) && checkTimes < 5) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
            if (!new File(appDir).exists()) {
                ShellUtils.shell("echo \"" + DateUtils.formatDatetime(new Date(), "yyyyMMddHHmmss")
                        + " -- " + appDir + " is not avaliable.\" >> /data/data/com.incomrecycle.prms.rvm/check.log");
                GUIGlobal.exit();
                return;
            }
        }
        RVMShell.restoreExternalConfig();
        String RVM_VERSION = SysConfig.get("RVM.VERSION");
        String RVM_VERSION_ID = SysConfig.get("RVM.VERSION.ID");
        String EXTERNAL_PROP_UPDATE_FIELD_SET = SysConfig.get("EXTERNAL.PROP.UPDATE.FIELD.SET");
        Properties propUpdateFields = new Properties();
        if (!StringUtils.isBlank(EXTERNAL_PROP_UPDATE_FIELD_SET)) {
            fields = EXTERNAL_PROP_UPDATE_FIELD_SET.split(";");
            for (i = 0; i < fields.length; i++) {
                fields[i] = fields[i].trim();
                if (fields[i].length() != 0) {
                    propUpdateFields.put(fields[i], StringUtils.nullToEmpty(SysConfig.get(fields[i])));
                }
            }
        }
        String KEY_SETTINGS_FILE = SysConfig.get("KEY_SETTINGS_FILE");
        String KEY_SETTINGS_ATTR = SysConfig.get("KEY_SETTINGS_ATTR");
        Properties propExternalConfig = PropUtils.loadFile(SysConfig.get("EXTERNAL.FILE"));
        RVMShell.initKeySettings(KEY_SETTINGS_FILE, KEY_SETTINGS_ATTR, propExternalConfig);
        Properties propKeySettings = RVMShell.restoreKeySettings(KEY_SETTINGS_FILE, KEY_SETTINGS_ATTR);
        if (propKeySettings != null && propKeySettings.size() > 0) {
            propExternalConfig.putAll(propKeySettings);
            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), propExternalConfig);
        }
        String PROP_DEFAULT_FIELD_SET = SysConfig.get("PROP.DEFAULT.FIELD.SET");
        if (!StringUtils.isBlank(PROP_DEFAULT_FIELD_SET)) {
            fields = PROP_DEFAULT_FIELD_SET.split(";");
            i = 0;
            while (i < fields.length) {
                fields[i] = fields[i].trim();
                if (fields[i].length() != 0 && StringUtils.isBlank(propExternalConfig.getProperty(fields[i]))) {
                    propUpdateFields.put(fields[i], StringUtils.nullToEmpty(SysConfig.get(fields[i])));
                }
                i++;
            }
        }
        String OLD_RVM_VERSION_ID = propExternalConfig.getProperty("RVM.VERSION.ID");
        propExternalConfig.putAll(propUpdateFields);
        if (!StringUtils.isBlank(RVM_VERSION_ID) && StringUtils.isBlank(propExternalConfig.getProperty("RVM.VERSION.ID"))) {
            propExternalConfig.put("RVM.VERSION.ID", RVM_VERSION_ID);
            propExternalConfig.put("RVM.VERSION", RVM_VERSION);
        }
        SysConfig.set(propExternalConfig);
        PropUtils.update(SysConfig.get("EXTERNAL.FILE"), propExternalConfig);
        try {
            String ipAddress = SocketUtils.getIpAddress();
            if (ipAddress == null) {
                ipAddress = DatagramAppender.DEFAULT_HOST;
            }
            SysConfig.set("LOCAL.IP", ipAddress);
        } catch (Exception e2) {
        }
        if (!StringUtils.isBlank(SysConfig.get("RVM.PROPERTIES." + SysConfig.get("RVM.MODE")))) {
            SysConfig.set(PropUtils.loadResource(SysConfig.get("RVM.PROPERTIES." + SysConfig.get("RVM.MODE"))));
        }
        if (!StringUtils.isBlank(SysConfig.get("SUB.CONFIG.RESOURCE"))) {
            try {
                SysConfig.set(PropUtils.loadResource(SysConfig.get("SUB.CONFIG.RESOURCE")));
            } catch (Exception e3) {
            }
        }
        if (!(StringUtils.isBlank(SysConfig.get("RVM.MODE")) || StringUtils.isBlank(SysConfig.get("HARDWARE.VERSION")))) {
            try {
                SysConfig.set(PropUtils.loadResource(SysConfig.get("HARDWARE.VERSION." + SysConfig.get("HARDWARE.VERSION"))));
                SysConfig.set(propExternalConfig);
                String VENDING_WAY_SET = SysConfig.get("VENDING.WAY.SET." + SysConfig.get("RVM.MODE"));
                if (!StringUtils.isBlank(VENDING_WAY_SET)) {
                    SysConfig.set("VENDING.WAY.SET", VENDING_WAY_SET);
                }
                String VENDING_WAY = SysConfig.get("VENDING.WAY." + SysConfig.get("RVM.MODE"));
                if (!StringUtils.isBlank(VENDING_WAY) && StringUtils.isBlank(SysConfig.get("VENDING.WAY"))) {
                    SysConfig.set("VENDING.WAY", VENDING_WAY);
                }
                String FIXED_VENDING_WAY;
                if ("TRUE".equalsIgnoreCase(SysConfig.get("FORCE.VENDINGWAY"))) {
                    FIXED_VENDING_WAY = SysConfig.get("FIXED.VENDING.WAY");
                    if (!StringUtils.isBlank(FIXED_VENDING_WAY)) {
                        VENDING_WAY = FIXED_VENDING_WAY;
                    }
                } else {
                    SharedPreferences preferences = getSharedPreferences("RVM", 0);
                    String INIT_VENDING_WAY = null;
                    if (preferences.getBoolean("firststart", true)) {
                        data = IOUtils.readResource(SysConfig.get("INIT.VENDINGWAY"));
                        if (data != null && data.length > 0) {
                            INIT_VENDING_WAY = new String(data);
                            if (!StringUtils.isBlank(INIT_VENDING_WAY)) {
                                VENDING_WAY = INIT_VENDING_WAY;
                            }
                            Editor editor = preferences.edit();
                            editor.putBoolean("firststart", false);
                            editor.commit();
                        }
                    }
                    FIXED_VENDING_WAY = SysConfig.get("FIXED.VENDING.WAY");
                    if (!StringUtils.isBlank(FIXED_VENDING_WAY)) {
                        if (StringUtils.isBlank(INIT_VENDING_WAY)) {
                            VENDING_WAY = SysConfig.get("VENDING.WAY");
                        }
                        String[] fixedVendingWayArray = FIXED_VENDING_WAY.split(";");
                        i = 0;
                        while (i < fixedVendingWayArray.length) {
                            if (StringUtils.isBlank(VENDING_WAY) || !(StringUtils.isBlank(VENDING_WAY) || VENDING_WAY.contains(fixedVendingWayArray[i]))) {
                                VENDING_WAY = fixedVendingWayArray[i] + ";" + VENDING_WAY;
                            }
                            i++;
                        }
                    }
                }
                Properties externalProp = new Properties();
                SysConfig.set("VENDING.WAY", VENDING_WAY);
                externalProp.put("VENDING.WAY", VENDING_WAY);
                PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                SysConfig.set(externalProp);
                RVMShell.backupExternalConfig();
            } catch (Exception e4) {
            }
        }
        String VERSION_UPDATE_FIELDS = SysConfig.get("VERSION.UPDATE.FIELDS");
        Properties propVersionUpdateFields = new Properties();
        if (!StringUtils.isBlank(VERSION_UPDATE_FIELDS)) {
            fields = VERSION_UPDATE_FIELDS.split(";");
            for (i = 0; i < fields.length; i++) {
                fields[i] = fields[i].trim();
                if (fields[i].length() != 0) {
                    propVersionUpdateFields.put(fields[i], StringUtils.nullToEmpty(SysConfig.get(fields[i])));
                }
            }
        }
        if (propVersionUpdateFields.size() > 0 && !StringUtils.isBlank(SysConfig.get("VERSION.FILE"))) {
            PropUtils.update(SysConfig.get("VERSION.FILE"), propVersionUpdateFields);
        }
        try {
            String init_shell = SysConfig.get("INIT.SHELL");
            if (!StringUtils.isBlank(init_shell)) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(init_shell);
                DataInputStream dataInputStream = new DataInputStream(is);
                List listCmd = new ArrayList();
                while (true) {
                    String cmd = dataInputStream.readLine();
                    if (cmd == null) {
                        break;
                    }
                    cmd = cmd.trim();
                    if (!(cmd.startsWith("#") || cmd.length() == 0)) {
                        listCmd.add(cmd);
                    }
                }
                is.close();
                if (listCmd.size() > 0) {
                    ShellUtils.shell(listCmd);
                }
            }
        } catch (Exception e5) {
        }
        SysGlobal.execute(new Thread() {
            public void run() {
                try {
                    String init_shell_file = SysConfig.get("INIT.SHELL.FILE");
                    if (!StringUtils.isBlank(init_shell_file)) {
                        File file = new File(init_shell_file);
                        if (file.isFile()) {
                            FileInputStream fis = new FileInputStream(file);
                            DataInputStream dataIS = new DataInputStream(fis);
                            List listCmd = new ArrayList();
                            while (true) {
                                String cmd = dataIS.readLine();
                                if (cmd == null) {
                                    break;
                                }
                                cmd = cmd.trim();
                                if (!(cmd.startsWith("#") || cmd.length() == 0)) {
                                    listCmd.add(cmd);
                                }
                            }
                            fis.close();
                            if (listCmd.size() > 0) {
                                ShellUtils.shell(listCmd);
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        });
        try {
            Process su = Runtime.getRuntime().exec("su");
            su.getOutputStream().write("busybox uname -m\nexit\n".getBytes());
            String str = new String(IOUtils.read(su.getInputStream()));
            String OS_PLATFORM = null;
            if (!StringUtils.isBlank(SysConfig.get("PLATFORM.SET"))) {
                String[] PLATFORM_SET = SysConfig.get("PLATFORM.SET").split(";");
                for (String split : PLATFORM_SET) {
                    String[] PLATFORM_INFO = split.split(":");
                    if (PLATFORM_INFO.length >= 2) {
                        String PLATFORM = PLATFORM_INFO[0];
                        String[] PLATFORM_NAME = PLATFORM_INFO[1].split(",");
                        if (PLATFORM_NAME.length >= 1) {
                            for (String split2 : PLATFORM_NAME) {
                                if (str.indexOf(split2) != -1) {
                                    OS_PLATFORM = PLATFORM;
                                    break;
                                }
                            }
                            if (OS_PLATFORM != null) {
                                break;
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
            if (OS_PLATFORM != null) {
                SysConfig.set("PLATFORM", OS_PLATFORM);
            }
            try {
                IOUtils.close(su.getOutputStream());
            } catch (Exception e6) {
            }
            try {
                IOUtils.close(su.getInputStream());
            } catch (Exception e7) {
            }
        } catch (Exception e8) {
            e8.printStackTrace();
        }
        RVMShell.backupExternalConfig();
        if (!(StringUtils.isBlank(RVM_VERSION_ID) || RVM_VERSION_ID.equals(OLD_RVM_VERSION_ID))) {
            data = IOUtils.readResource("tcp_summary.sh");
            OutputStream outputStream = null;
            try {
                String SOURCE_SHELL_SCRIPT = SysConfig.get("TCP_SUMMARY.SHELL");
                File file = new File(SOURCE_SHELL_SCRIPT);
                if (file.exists()) {
                    file.delete();
                }
                OutputStream fileOutputStream = new FileOutputStream(SOURCE_SHELL_SCRIPT);
                try {
                    fileOutputStream.write(data);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    outputStream = null;
                    ShellUtils.execScript(StringUtils.replace(new String(IOUtils.readResource("tcp_summary_setup.sh")), "$SOURCE_SHELL_SCRIPT$", SOURCE_SHELL_SCRIPT));
                } catch (Exception e9) {
                    outputStream = fileOutputStream;
                    IOUtils.close(outputStream);
                    new GUIInit().Init(null);
                    new ServiceInit().Init(null);
                    RVMDaemonClient.init();
                    SysGlobal.execute(TickTaskThread.getTickTaskThread());
                    SysGlobal.execute(CrontabThread.getCrontabThread());
                    SysGlobal.execute(DelayTask.getDelayTask());
                    SysGlobal.execute(InstanceTask.getInstanceTask());
                    CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "initUpdateCheck", null);
                }
            } catch (Exception e10) {
                IOUtils.close(outputStream);
                new GUIInit().Init(null);
                new ServiceInit().Init(null);
                RVMDaemonClient.init();
                SysGlobal.execute(TickTaskThread.getTickTaskThread());
                SysGlobal.execute(CrontabThread.getCrontabThread());
                SysGlobal.execute(DelayTask.getDelayTask());
                SysGlobal.execute(InstanceTask.getInstanceTask());
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "initUpdateCheck", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new GUIInit().Init(null);
        new ServiceInit().Init(null);
        RVMDaemonClient.init();
        SysGlobal.execute(TickTaskThread.getTickTaskThread());
        SysGlobal.execute(CrontabThread.getCrontabThread());
        SysGlobal.execute(DelayTask.getDelayTask());
        SysGlobal.execute(InstanceTask.getInstanceTask());
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "initUpdateCheck", null);
        } catch (Exception e82) {
            e82.printStackTrace();
        }
    }

    public String getName() {
        return "RVMWelcome";
    }

    public void updateLanguage() {
        TextView contentView = (TextView) findViewById(R.id.fullscreen_content);
        if (contentView != null) {
            contentView.setText(R.string.welcome);
        }
        TextView startRemindTextView = (TextView) findViewById(R.id.starting_remind);
        if (startRemindTextView != null) {
            startRemindTextView.setText(R.string.starting_remind);
        }
    }

    public boolean doEventFilter(HashMap hsmpEvent) {
        if (this.enableEvent) {
            return super.doEventFilter(hsmpEvent);
        }
        return false;
    }

    public void doEvent(HashMap hsmpEvent) {
    }
}
