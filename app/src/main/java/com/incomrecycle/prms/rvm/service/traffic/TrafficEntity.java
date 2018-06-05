package com.incomrecycle.prms.rvm.service.traffic;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class TrafficEntity {
    private static HashMap<String, Long> hsmpFlows = new HashMap();
    private static final Logger logger = LoggerFactory.getLogger("TrafficEntity");
    private static int month = -1;
    private static TrafficInfoProvider trafficInfoProvider = new TrafficInfoProvider();

   private static int geCurrenttMonth() {
        return new Date().getMonth() + 1;
    }

    public static void addData(String type, int len) {
        if (month != geCurrenttMonth()) {
            saveData();
        }
        synchronized (hsmpFlows) {
            Long lLen = (Long) hsmpFlows.get(type);
            if (lLen == null) {
                lLen = Long.valueOf(0);
            }
            hsmpFlows.put(type, Long.valueOf(lLen.longValue() + ((long) len)));
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void saveData() {
        try {
            if (month != -1) {
                // v5 = hsmpFlows;
                try {
                    if (hsmpFlows.size() <= 0) {
                        HashMap v2 = new HashMap();
                        v2.putAll(hsmpFlows);
                        if (month == TrafficEntity.geCurrenttMonth()) {
                            if (v2.size() == 0) {
                                return;
                            }
                            TrafficEntity.clear(month + 1);
                            TrafficEntity.saveData(month, v2);
                        }
                        hsmpFlows.clear();
                        month = TrafficEntity.geCurrenttMonth();
                    }
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {

        }
    }

    private static void saveData(int month, HashMap<String, Long> hsmpTrafficData) {
        HashMap hsmpParam = new HashMap();
        try {
            hsmpParam.put("TRAFFIC_MONTH", Integer.toString(month));
            hsmpParam.put("TRAFFIC_DATA", hsmpTrafficData);
            CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "saveTrafficRecord", hsmpParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<TrafficInfo> list = trafficInfoProvider.getTrafficInfos();
        if (list != null) {
            logger.debug("Today flow details:");
            for (int i = 0; i < list.size(); i++) {
                TrafficInfo trafficInfo = (TrafficInfo) list.get(i);
                logger.debug("packname = '" + trafficInfo.getPackname() + "', flow = {RXD=" + trafficInfo.getRx() + "}{TXD=" + trafficInfo.getTx() + "}{TOTAL_DATA=" + (trafficInfo.getRx() + trafficInfo.getTx()) + "}");
            }
        }
    }

    private static void clear(int month) {
        if (month > 12) {
            month = 1;
        }
        HashMap hsmpParam = new HashMap();
        try {
            hsmpParam.put("TRAFFIC_MONTH", Integer.toString(month));
            CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "clearTrafficRecord", hsmpParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        month = geCurrenttMonth();
        clear(month + 1);
        hsmpFlows.clear();
        HashMap hsmpParam = new HashMap();
        try {
            hsmpParam.put("TRAFFIC_MONTH", Integer.toString(month));
            HashMap resultMap = CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "getTrafficRecord", hsmpParam);
            if (resultMap != null && resultMap.size() > 0) {
                HashMap<String, String> hsmpTrafficData = (HashMap) resultMap.get("TRAFFIC_DATA");
                if (hsmpTrafficData != null && hsmpTrafficData.size() > 0) {
                    for (Entry entry : hsmpTrafficData.entrySet()) {
                        hsmpFlows.put((String) entry.getKey(), Long.valueOf(Long.parseLong(String.valueOf(entry.getValue()))));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HashMap getData() {
        int currentMonth = geCurrenttMonth();
        HashMap hsmpParam = new HashMap();
        try {
            hsmpParam.put("TRAFFIC_MONTH", Integer.toString(currentMonth));
            HashMap resultMap = CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "getTrafficRecord", hsmpParam);
            if (resultMap != null && resultMap.size() > 0) {
                return (HashMap) resultMap.get("TRAFFIC_DATA");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
