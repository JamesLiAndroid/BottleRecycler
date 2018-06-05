package com.incomrecycle.prms.rvm.service.commonservice;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class PLCAnormalMonitorUtils {
    private static HashMap<String, HashMap> hsmp = new HashMap();

    public static void addData(String type, String state) {
        synchronized (hsmp) {
            HashMap<String, Integer> hsmpDetail = (HashMap) hsmp.get(type);
            if (hsmpDetail == null) {
                hsmpDetail = new HashMap();
            }
            Integer lLen = (Integer) hsmpDetail.get(state);
            if (lLen == null) {
                lLen = Integer.valueOf(0);
            }
            if (lLen.intValue() < Integer.MAX_VALUE) {
                lLen = Integer.valueOf(lLen.intValue() + 1);
            }
            hsmp.put(type, hsmpDetail);
            hsmpDetail.put(state, lLen);
        }
    }

    public static HashMap getData() {
        HashMap resultMap = new HashMap();
        synchronized (hsmp) {
            if (hsmp.size() > 0) {
                for (Entry entry : hsmp.entrySet()) {
                    String type = (String) entry.getKey();
                    HashMap<String, Integer> hsmpDetail = (HashMap) entry.getValue();
                    Integer NORMAL = (Integer) hsmpDetail.get("NORMAL");
                    if (NORMAL == null) {
                        NORMAL = Integer.valueOf(0);
                    }
                    Integer ANORMAL = (Integer) hsmpDetail.get("ANORMAL");
                    if (ANORMAL == null) {
                        ANORMAL = Integer.valueOf(0);
                    }
                    if (NORMAL.intValue() + ANORMAL.intValue() == 0) {
                        NORMAL = Integer.valueOf(1);
                    }
                    double normalData = Double.parseDouble(String.valueOf(NORMAL));
                    double anormalData = Double.parseDouble(String.valueOf(ANORMAL));
                    resultMap.put(type, String.valueOf((int) ((anormalData / (anormalData + normalData)) * 100.0d)));
                }
            }
        }
        return resultMap;
    }

    public static String getDataCount() {
        int count = 0;
        synchronized (hsmp) {
            if (hsmp.size() > 0) {
                Iterator iter = hsmp.entrySet().iterator();
                if (iter.hasNext()) {
                    Entry entry = (Entry) iter.next();
                    String type = (String) entry.getKey();
                    HashMap<String, Integer> hsmpDetail = (HashMap) entry.getValue();
                    Integer NORMAL = (Integer) hsmpDetail.get("NORMAL");
                    if (NORMAL == null) {
                        NORMAL = Integer.valueOf(0);
                    }
                    Integer ANORMAL = (Integer) hsmpDetail.get("ANORMAL");
                    if (ANORMAL == null) {
                        ANORMAL = Integer.valueOf(0);
                    }
                    count = Integer.parseInt(String.valueOf(ANORMAL)) + Integer.parseInt(String.valueOf(NORMAL));
                }
            }
        }
        return String.valueOf(count);
    }
}
