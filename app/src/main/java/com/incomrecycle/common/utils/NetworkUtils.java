package com.incomrecycle.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkUtils {
    private static final String[][] NET_STATE;
    public static final String NET_STATE_FAILED = "FAILED";
    public static final String NET_STATE_SUCCESS = "SUCCESS";
    public static final String NET_STATE_UNKNOWN = "UNKNOWN";

    static {
        NET_STATE = new String[3][];
        NET_STATE[0] = new String[]{NET_STATE_SUCCESS, "1 packets transmitted, 1 received, 0% packet loss,"};
        NET_STATE[1] = new String[]{NET_STATE_FAILED, "1 packets transmitted, 0 received, 100% packet loss,"};
        NET_STATE[2] = new String[]{NET_STATE_UNKNOWN, "unknown host"};
    }

    public static String ipState(String host) {
        String result = ShellUtils.shell("ping -c 1 " + host);
        if (result != null) {
            for (int i = 0; i < NET_STATE.length; i++) {
                if (result.indexOf(NET_STATE[i][1]) != -1) {
                    return NET_STATE[i][0];
                }
            }
        }
        return null;
    }

    public static List<String> getGatewayIp(String getGatewayIpShell) {
        String result = ShellUtils.execScript(new String(IOUtils.readResource(getGatewayIpShell)));
        List<String> listGatewayIp = new ArrayList();
        if (!StringUtils.isBlank(result)) {
            Matcher m = Pattern.compile("\\[\\d+\\.\\d+\\.\\d+\\.\\d+\\]").matcher(result);
            while (m.find()) {
                String ip = result.substring(m.start() + 1, m.end() - 1);
                if (!StringUtils.isBlank(ip)) {
                    listGatewayIp.add(ip);
                }
            }
        }
        return listGatewayIp;
    }

    public static List<String> getMacAddr() {
        List listCmd = new ArrayList();
        listCmd.add("MAC_ADDR=`netcfg | busybox grep -v '0.0.0.0' | busybox grep -v '127.0.0.' | busybox awk -F' ' '{print $5}'`");
        listCmd.add("if [ -n \"${MAC_ADDR}\" ];then");
        listCmd.add("for ii in ${MAC_ADDR};do");
        listCmd.add("echo MAC_ADDR:\"[${MAC_ADDR}]\"");
        listCmd.add("done");
        listCmd.add("fi");
        List<String> listMacAddr = new ArrayList();
        String result = ShellUtils.shell(listCmd);
        if (!StringUtils.isBlank(result)) {
            String markHead = "MAC_ADDR:";
            Matcher m = Pattern.compile(markHead + "\\[..:..:..:..:..:..\\]").matcher(result);
            while (m.find()) {
                String addr = result.substring((m.start() + 1) + markHead.length(), m.end() - 1).toLowerCase();
                if (!(StringUtils.isBlank(addr) || listMacAddr.contains(addr))) {
                    listMacAddr.add(addr);
                }
            }
        }
        return listMacAddr;
    }
}
