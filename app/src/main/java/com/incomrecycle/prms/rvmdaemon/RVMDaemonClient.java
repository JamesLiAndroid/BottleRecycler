package com.incomrecycle.prms.rvmdaemon;

import com.google.code.microlog4android.appender.DatagramAppender;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.queue.FIFOQueue;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.SocketUtils;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class RVMDaemonClient {
    private static String currentState = null;
    private static DaemonClient daemonClient = null;
    private static final FIFOQueue fifoQueue = new FIFOQueue();

    private static class DaemonClient implements Runnable {
        private FIFOQueue fifoQueue;

        public DaemonClient(FIFOQueue fifoQueue) {
            this.fifoQueue = fifoQueue;
        }

        public void run() {
            while (true) {
                connecting();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void connecting() {
            Socket s = null;
            OutputStream os = null;
            byte[] CRLF = "\n".getBytes();
            try {
                s = SocketUtils.createSocket(DatagramAppender.DEFAULT_HOST, 9337, 5000);
                if (s != null) {
                    os = s.getOutputStream();
                    this.fifoQueue.push(RVMDaemonClient.currentState);
                    String activity = SysConfig.get("MainActivity");
                    while (true) {
                        String cmd = (String) this.fifoQueue.pop(10000);
                        if (cmd == null) {
                            os.write(CRLF);
                        } else {
                            HashMap hashMap = null;
                            if ("EXIT".equalsIgnoreCase(cmd)) {
                                hashMap = new HashMap();
                                hashMap.put("ACTIVITY", activity);
                                hashMap.put("CMD", "EXIT");
                            }
                            if ("IDLE".equalsIgnoreCase(cmd)) {
                                hashMap = new HashMap();
                                hashMap.put("ACTIVITY", activity);
                                hashMap.put("CMD", "IDLE");
                            }
                            if ("BUSY".equalsIgnoreCase(cmd)) {
                                hashMap = new HashMap();
                                hashMap.put("ACTIVITY", activity);
                                hashMap.put("CMD", "BUSY");
                            }
                            if (hashMap != null) {
                                os.write((toCmd(hashMap) + "\n").getBytes());
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
            IOUtils.close(os);
            SocketUtils.close(s);
        }

        private String toCmd(HashMap<String, String> hsmpCmd) {
            StringBuffer sb = new StringBuffer();
            for (String key : hsmpCmd.keySet()) {
                String val = (String) hsmpCmd.get(key);
                if (sb.length() == 0) {
                    sb.append(key + "=" + val);
                } else {
                    sb.append(";" + key + "=" + val);
                }
            }
            return sb.toString();
        }
    }

    public static void init() {
        if ("TRUE".equalsIgnoreCase(SysConfig.get("RVMDaemonEnable")) && daemonClient == null) {
            currentState = "IDLE";
            daemonClient = new DaemonClient(fifoQueue);
            SysGlobal.execute(daemonClient);
        }
    }

    public static void exit() {
        if ("TRUE".equalsIgnoreCase(SysConfig.get("RVMDaemonEnable"))) {
            fifoQueue.push("EXIT");
            currentState = "EXIT";
        }
    }

    public static void update(boolean isEnable) {
        if (!"TRUE".equalsIgnoreCase(SysConfig.get("RVMDaemonEnable"))) {
            return;
        }
        if (isEnable) {
            fifoQueue.push("IDLE");
            currentState = "IDLE";
            return;
        }
        fifoQueue.push("BUSY");
        currentState = "BUSY";
    }
}
