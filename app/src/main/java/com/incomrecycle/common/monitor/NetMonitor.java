package com.incomrecycle.common.monitor;

import java.util.ArrayList;
import java.util.List;

public class NetMonitor {
    private static long lDownloadSize = 0;
    private static long lStartTime = 0;
    private static long lUploadSize = 0;
    private static List<NetMonitorClient> listNetMonitorClient = new ArrayList();

    public interface NetMonitorClient {
        void output(String str);
    }

    public static void addNetMonitorClient(NetMonitorClient netMonitorClient) {
        synchronized (listNetMonitorClient) {
            listNetMonitorClient.add(netMonitorClient);
        }
    }

    public static long getUploadSize() {
        return lUploadSize;
    }

    public static long getDownloadSize() {
        return lDownloadSize;
    }

    public static void resetUploadSize() {
        lUploadSize = 0;
        lStartTime = 0;
    }

    public static void resetDownloadSize() {
        lDownloadSize = 0;
        lStartTime = 0;
    }

    public static void addUploadSize(long size) {
        lUploadSize += size;
        if (lStartTime == 0) {
            lStartTime = System.currentTimeMillis();
        }
        output("RVM Network:Up=" + (lUploadSize / 1000) + "kb;Down=" + (lUploadSize / 1000) + "kb;Elapse=" + ((System.currentTimeMillis() - lStartTime) / 1000) + "s\r\n");
    }

    public static void addDownloadSize(long size) {
        lDownloadSize += size;
        if (lStartTime == 0) {
            lStartTime = System.currentTimeMillis();
        }
        output("RVM Network:Up=" + (lUploadSize / 1000) + "kb;Down=" + (lUploadSize / 1000) + "kb;Elapse=" + ((System.currentTimeMillis() - lStartTime) / 1000) + "s\r\n");
    }

    private static void output(String s) {
        for (int i = 0; i < listNetMonitorClient.size(); i++) {
            ((NetMonitorClient) listNetMonitorClient.get(i)).output(s);
        }
    }
}
