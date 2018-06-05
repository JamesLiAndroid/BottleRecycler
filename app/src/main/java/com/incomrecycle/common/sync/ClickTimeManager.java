package com.incomrecycle.common.sync;

import java.util.HashMap;

public class ClickTimeManager {
    private static final HashMap<String, Long> hsmpTime = new HashMap();

    public static boolean clickEnable(String clickId, long Interval) {
        boolean isEnable = false;
        synchronized (hsmpTime) {
            Long lTime = (Long) hsmpTime.get(clickId);
            long lCurrentTime = System.currentTimeMillis();
            if (lTime == null) {
                hsmpTime.put(clickId, Long.valueOf(lCurrentTime));
                isEnable = true;
            } else if (lTime.longValue() + Interval < lCurrentTime) {
                hsmpTime.put(clickId, Long.valueOf(lCurrentTime));
                isEnable = true;
            }
        }
        return isEnable;
    }
}
