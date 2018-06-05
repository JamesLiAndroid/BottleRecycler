package com.incomrecycle.common.task;

import com.google.code.microlog4android.format.SimpleFormatter;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.utils.DateUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static java.util.Calendar.DAY_OF_WEEK;

public class CrontabThread extends Thread {
    private static final CrontabThread crontabThread = new CrontabThread();
    private List<HashMap<String, Object>> listCronTabItem = new ArrayList();

    private static class TaskExecuteThread implements Runnable {
        TaskAction taskAction;

        TaskExecuteThread(TaskAction taskAction) {
            this.taskAction = taskAction;
        }

        public void run() {
            this.taskAction.execute();
        }
    }

    private CrontabThread() {
    }

    public static CrontabThread getCrontabThread() {
        return crontabThread;
    }

    public void run() {
        long lLastSeconds = 0;
        while (true) {
            long lSeconds = System.currentTimeMillis() / 1000;
            while (true) {
                if (lLastSeconds < lSeconds && lSeconds % 60 == 0) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                lSeconds++;
            }
            lLastSeconds = lSeconds;
            Date tDate = new Date();
            Calendar calendar = Calendar.getInstance();
            int minute = Integer.parseInt(DateUtils.formatDatetime(tDate, "mm"));
            int hour = Integer.parseInt(DateUtils.formatDatetime(tDate, "HH"));
            int monthday = Integer.parseInt(DateUtils.formatDatetime(tDate, "dd"));
            int month = Integer.parseInt(DateUtils.formatDatetime(tDate, "MM"));
            int weekday = calendar.get(DAY_OF_WEEK) + 1;
            for (int t = 0; t < this.listCronTabItem.size(); t++) {
                HashMap<String, Object> cronTabItem = (HashMap) this.listCronTabItem.get(t);
                if (isCronTabTime(minute, (int[]) cronTabItem.get("minutes")) && isCronTabTime(hour, (int[]) cronTabItem.get("hours")) && isCronTabTime(monthday, (int[]) cronTabItem.get("monthdays")) && isCronTabTime(month, (int[]) cronTabItem.get("months")) && isCronTabTime(weekday, (int[]) cronTabItem.get("weekdays"))) {
                    SysGlobal.execute(new TaskExecuteThread((TaskAction) cronTabItem.get("action")));
                }
            }
        }
    }

    public static boolean isCronTabTime(int now, int[] times) {
        if (times == null || times.length == 0) {
            return true;
        }
        for (int i : times) {
            if (i == now) {
                return true;
            }
        }
        return false;
    }

    public static int[] parseTime(String s, int min, int max) {
        int[] iArr = null;
        if (!(s == null || s.equals("*"))) {
            int k;
            String[] ms = s.split(",");
            List<String> lms = new ArrayList();
            for (k = 0; k < ms.length; k++) {
                if (ms.length != 0) {
                    int idx = ms[k].indexOf(SimpleFormatter.DEFAULT_DELIMITER);
                    if (idx != -1) {
                        int start = Integer.parseInt(ms[k].substring(0, idx));
                        int end = Integer.parseInt(ms[k].substring(idx + 1));
                        int m = start;
                        while (true) {
                            lms.add("" + m);
                            if (m == end) {
                                break;
                            }
                            m++;
                            if (m > max) {
                                m = min;
                            }
                        }
                    } else {
                        lms.add(ms[k]);
                    }
                }
            }
            if (lms.size() > 0) {
                iArr = new int[lms.size()];
                for (k = 0; k < lms.size(); k++) {
                    iArr[k] = Integer.parseInt((String) lms.get(k));
                }
            }
        }
        return iArr;
    }

    public boolean registerCronTab(String triggerTime, TaskAction taskAction) {
        if (triggerTime == null) {
            return false;
        }
        triggerTime = triggerTime.trim();
        if (triggerTime.length() == 0 || triggerTime.startsWith("#")) {
            return false;
        }
        triggerTime = triggerTime.replaceAll("\t", " ");
        int idx = triggerTime.indexOf(" ");
        if (idx == -1) {
            return false;
        }
        String minutes = triggerTime.substring(0, idx);
        triggerTime = triggerTime.substring(idx + 1).trim();
        idx = triggerTime.indexOf(" ");
        if (idx == -1) {
            return false;
        }
        String hours = triggerTime.substring(0, idx);
        triggerTime = triggerTime.substring(idx + 1).trim();
        idx = triggerTime.indexOf(" ");
        if (idx == -1) {
            return false;
        }
        String monthdays = triggerTime.substring(0, idx);
        triggerTime = triggerTime.substring(idx + 1).trim();
        idx = triggerTime.indexOf(" ");
        if (idx == -1) {
            return false;
        }
        String months = triggerTime.substring(0, idx);
        String weekdays = triggerTime.substring(idx + 1).trim();
        HashMap<String, Object> hsmpCronTabItem = new HashMap();
        try {
            hsmpCronTabItem.put("minutes", parseTime(minutes, 0, 59));
            hsmpCronTabItem.put("hours", parseTime(hours, 0, 23));
            hsmpCronTabItem.put("monthdays", parseTime(monthdays, 1, 31));
            hsmpCronTabItem.put("months", parseTime(months, 1, 12));
            hsmpCronTabItem.put("weekdays", parseTime(weekdays, 0, 6));
            hsmpCronTabItem.put("action", taskAction);
            this.listCronTabItem.add(hsmpCronTabItem);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
