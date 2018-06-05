package com.incomrecycle.prms.rvm.common;

import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.ShellUtils;
import com.incomrecycle.common.utils.StringUtils;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

public class RVMShell {
    public static void backupExternalConfig() {
        try {
            Properties propSource = PropUtils.loadFile("/sdcard/rvm/config.properties");
            Properties propBackup = PropUtils.loadFile("/data/data/com.incomrecycle.prms.rvm/config.properties");
            boolean needBackup = false;
            if (propSource.size() == propBackup.size()) {
                Enumeration<String> enumName = (Enumeration<String>) propSource.propertyNames();
                while (enumName.hasMoreElements()) {
                    String name = (String) enumName.nextElement();
                    if (!StringUtils.equals(propSource.getProperty(name), propBackup.getProperty(name))) {
                        needBackup = true;
                        break;
                    }
                }
            }
            needBackup = true;
            if (StringUtils.isBlank(propSource.getProperty("RVM.CODE")) || "0".equals(propSource.getProperty("RVM.CODE"))) {
                needBackup = false;
            }
            if (needBackup) {
                byte[] data = IOUtils.readResource("shell_backup.sh");
                if (data != null && data.length > 0) {
                    ShellUtils.execScript(new String(data));
                }
            }
        } catch (Exception e) {
        }
    }

    public static void restoreExternalConfig() {
        try {
            byte[] data = IOUtils.readResource("shell_restore.sh");
            if (data != null && data.length > 0) {
                ShellUtils.execScript(new String(data));
            }
        } catch (Exception e) {
        }
    }

    public static void initKeySettings(String keySettingsFile, String keySet, Properties prop) {
        if (keySet != null) {
            String[] keys = keySet.split(",");
            Properties keyProp = new Properties();
            for (int i = 0; i < keys.length; i++) {
                String value = prop.getProperty(keys[i]);
                if (!StringUtils.isBlank(value)) {
                    keyProp.setProperty(keys[i], value);
                }
            }
            if (keyProp.size() > 0) {
                PropUtils.save(new File(keySettingsFile), keyProp);
                HashMap<String, String> hsmpReplace = new HashMap();
                hsmpReplace.put("$COMMAND$", "init");
                ShellUtils.execResourceScript("rvm_keysettings.sh", hsmpReplace);
            }
        }
    }

    public static void backupKeySettings(String keySettingsFile, String keySet, Properties prop) {
        if (keySet != null) {
            String[] keys = keySet.split(",");
            Properties keyProp = new Properties();
            for (int i = 0; i < keys.length; i++) {
                String value = prop.getProperty(keys[i]);
                if (!StringUtils.isBlank(value)) {
                    keyProp.setProperty(keys[i], value);
                }
            }
            if (keyProp.size() > 0) {
                PropUtils.save(new File(keySettingsFile), keyProp);
                HashMap<String, String> hsmpReplace = new HashMap();
                hsmpReplace.put("$COMMAND$", "backup");
                ShellUtils.execResourceScript("rvm_keysettings.sh", hsmpReplace);
            }
        }
    }

    public static Properties restoreKeySettings(String keySettingsFile, String keySet) {
        if (keySet == null) {
            return null;
        }
        HashMap<String, String> hsmpReplace = new HashMap();
        hsmpReplace.put("$COMMAND$", "restore");
        ShellUtils.execResourceScript("rvm_keysettings.sh", hsmpReplace);
        Properties prop = PropUtils.loadFile(keySettingsFile);
        String[] keys = keySet.split(",");
        Properties properties = new Properties();
        for (int i = 0; i < keys.length; i++) {
            String value = prop.getProperty(keys[i]);
            if (!StringUtils.isBlank(value)) {
                properties.setProperty(keys[i], value);
            }
        }
        return properties;
    }
}
