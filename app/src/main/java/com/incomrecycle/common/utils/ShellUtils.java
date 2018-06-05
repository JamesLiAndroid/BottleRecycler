package com.incomrecycle.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShellUtils {
    private static String cmd_su = null;
    private static final Object objWait = new Object();

    public static String shell(String cmd) {
        return execScript(cmd);
    }

    public static String shell(List<String> listCmd) {
        if (listCmd == null) {
            return null;
        }
        if (listCmd.size() == 0) {
            return null;
        }
        try {
            synchronized (objWait) {
                if (cmd_su == null) {
                    if (new File("/system/bin/su").isFile()) {
                        cmd_su = "/system/bin/su";
                    } else {
                        cmd_su = "su";
                    }
                }
            }
            Process su = Runtime.getRuntime().exec(cmd_su);
            OutputStream os = su.getOutputStream();
            InputStream in = su.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int totalReaded = 0;
            byte[] bDataBuff = new byte[8192];
            for (int i = 0; i < listCmd.size(); i++) {
                os.write((((String) listCmd.get(i)) + "\n").getBytes());
            }
            os.write("exit\n".getBytes());
            while (true) {
                try {
                    int iReaded = in.read(bDataBuff);
                    if (iReaded <= 0) {
                        break;
                    }
                    out.write(bDataBuff, 0, iReaded);
                    totalReaded += iReaded;
                } catch (Exception e) {
                }
            }
            in.close();
            os.close();
            if (totalReaded == 0) {
                return null;
            }
            return new String(out.toByteArray());
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static String execScript(File file) {
        String str = null;
        if (file != null && file.isFile()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                String script = new String(IOUtils.read(fis));
                fis.close();
                str = execScript(script);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    public static String execScript(String script) {
        if (script == null) {
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(script.getBytes());
        DataInputStream dis = new DataInputStream(bais);
        List listCmd = new ArrayList();
        while (true) {
            try {
                String str = dis.readLine();
                if (str != null) {
                    listCmd.add(str);
                } else {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    bais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return shell(listCmd);
    }

    public static String execResourceScript(String resource, HashMap<String, String> hsmpReplace) {
        try {
            byte[] data = IOUtils.readResource(resource);
            if (data != null && data.length > 0) {
                String script = new String(data);
                if (hsmpReplace != null) {
                    for (String key : hsmpReplace.keySet()) {
                        script = StringUtils.replace(script, key, (String) hsmpReplace.get(key));
                    }
                }
                return execScript(script);
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String execResourceScript(String resource) {
        return execResourceScript(resource, null);
    }

    public static boolean kill(String activity) {
        String pkgname;
        if (activity.indexOf("/") != -1) {
            pkgname = activity.substring(0, activity.indexOf("/"));
        } else {
            pkgname = activity;
        }
        String text = shell("ps | busybox grep ' " + pkgname + "$' | busybox grep -v grep");
        if (text == null) {
            return true;
        }
        String[] lines = text.split("\n");
        int l = 0;
        while (l < lines.length) {
            lines[l] = lines[l].trim();
            if (lines[l].endsWith(pkgname) && lines[l].indexOf(" grep ") == -1) {
                int idx = lines[l].indexOf(" ");
                if (idx == -1) {
                    return true;
                }
                String substr = lines[l].substring(idx).trim();
                shell("kill -9 " + substr.substring(0, substr.indexOf(" ")).trim());
                if (isRunning(activity)) {
                    return false;
                }
                return true;
            }
            l++;
        }
        return true;
    }

    public static void purifyProcess(String activity) {
        String pkgname;
        if (activity.indexOf("/") != -1) {
            pkgname = activity.substring(0, activity.indexOf("/"));
        } else {
            pkgname = activity;
        }
        if (!StringUtils.isBlank(pkgname)) {
            List listCmd = new ArrayList();
            listCmd.add("PRG_PID_COUNT=`ps | busybox grep ' " + pkgname + "$' | busybox grep -v grep | busybox wc -l`");
            listCmd.add("if [ $PRG_PID_COUNT != '0' ] && [ $PRG_PID_COUNT != '1' ];then");
            listCmd.add("PRG_PIDS=`ps | busybox grep ' " + pkgname + "$' | busybox grep -v grep | busybox awk -F' '  '{print $2}'`");
            listCmd.add("kill -9 $PRG_PIDS");
            listCmd.add("fi");
            shell(listCmd);
        }
    }

    public static boolean isRunning(String activity) {
        String pkgname;
        if (activity.indexOf("/") != -1) {
            pkgname = activity.substring(0, activity.indexOf("/"));
        } else {
            pkgname = activity;
        }
        String text = shell("ps | busybox grep ' " + pkgname + "$' | busybox grep -v grep");
        if (text == null) {
            return false;
        }
        String[] lines = text.split("\n");
        int l = 0;
        while (l < lines.length) {
            lines[l] = lines[l].trim();
            if (lines[l].endsWith(pkgname) && lines[l].indexOf(" grep ") == -1) {
                return true;
            }
            l++;
        }
        return false;
    }

    public static void run(String activity) {
        shell("am start " + activity);
    }

    public static boolean install(String apk) {
        File file = new File(apk);
        if (!file.isFile()) {
            return false;
        }
        shell("pm install -r " + file.getAbsolutePath());
        return true;
    }

    public static void delete(String apk) {
        File file = new File(apk);
        if (file.isFile()) {
            shell("rm " + file.getAbsolutePath());
        }
    }
}
