package com.incomrecycle.prms.rvm.router;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class RouterService {
    private static final int PORT_CLI = 50601;
    private static final int PORT_SER = 50600;
    private static final byte[] REQUEST_ROUTER_INFO = new byte[]{(byte) 1, (byte) 0, (byte) 0, (byte) 0};
    private static final byte[] SEND_ROUTER_INFO = new byte[]{(byte) 2, (byte) 0, (byte) 0, (byte) 0};
    private long interval = 600000;
    private OnReceiveCallback onChangeCallback;
    private SendThread sendThread = null;

    public interface OnReceiveCallback {
        void onReceive(HashMap<String, String> hashMap);
    }

    public static class RecvThread extends Thread {
        private DatagramSocket ds = null;
        private boolean isStopped = false;
        private OnReceiveCallback onReceiveCallback = null;

        public RecvThread(DatagramSocket ds, OnReceiveCallback onReceiveCallback) {
            this.ds = ds;
            this.onReceiveCallback = onReceiveCallback;
        }

        public void stopRunning() {
            this.isStopped = true;
        }

        public void run() {
            byte[] data = new byte[196];
            System.arraycopy(RouterService.REQUEST_ROUTER_INFO, 0, data, 0, RouterService.REQUEST_ROUTER_INFO.length);
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getByName("255.255.255.255");
            } catch (Exception e) {
            }
            DatagramPacket dp = new DatagramPacket(data, data.length, inetAddress, RouterService.PORT_CLI);
            while (!this.isStopped) {
                try {
                    this.ds.receive(dp);
                    byte[] recvData = dp.getData();
                    if (recvData != null) {
                        HashMap<String, String> hsmpRouterInfo = RouterService.parseRouterInfo(recvData);
                        if (!(hsmpRouterInfo == null || this.onReceiveCallback == null)) {
                            this.onReceiveCallback.onReceive(hsmpRouterInfo);
                        }
                    }
                } catch (Exception e2) {
                    try {
                        if (!this.isStopped) {
                            sleep(1000);
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    public static class SendThread extends Thread {
        private DatagramSocket ds = null;
        private long interval = 600000;
        private boolean isStopped = false;
        private OnReceiveCallback onReceiveCallback = null;

        public SendThread(OnReceiveCallback onReceiveCallback, long interval) {
            this.onReceiveCallback = onReceiveCallback;
            if (interval < 30000) {
                interval = 30000;
            }
            this.interval = interval;
        }

        public void stopRunning() {
            this.isStopped = true;
        }

        public void run() {
            RecvThread recvThread = null;
            try {
                byte[] data = new byte[196];
                System.arraycopy(RouterService.REQUEST_ROUTER_INFO, 0, data, 0, RouterService.REQUEST_ROUTER_INFO.length);
                this.ds = new DatagramSocket(RouterService.PORT_SER);
                this.ds.setSoTimeout(2);
                DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), RouterService.PORT_CLI);
                RecvThread recvThread2 = null;
                while (!this.isStopped) {
                    try {
                        this.ds.send(dp);
                        if (!this.isStopped) {
                            if (recvThread2 == null) {
                                recvThread = new RecvThread(this.ds, this.onReceiveCallback);
                                recvThread.start();
                            } else {
                                recvThread = recvThread2;
                            }
                            Thread.sleep(this.interval);
                            recvThread2 = recvThread;
                        }
                    } catch (Exception e) {
                        recvThread = recvThread2;
                    }
                }
                recvThread = recvThread2;
            } catch (Exception e2) {
            }
            if (recvThread != null) {
                recvThread.stopRunning();
            }
            try {
                if (this.ds != null) {
                    this.ds.close();
                    this.ds = null;
                }
            } catch (Exception e3) {
            }
        }
    }

    public RouterService(OnReceiveCallback onChangeCallback, long interval) {
        this.onChangeCallback = onChangeCallback;
        this.interval = interval;
    }

    public void start() {
        if (this.onChangeCallback != null && this.sendThread == null) {
            this.sendThread = new SendThread(this.onChangeCallback, this.interval);
            this.sendThread.start();
        }
    }

    public void stop() {
        if (this.sendThread != null) {
            this.sendThread.stopRunning();
            this.sendThread = null;
        }
    }

    public static HashMap<String, String> parseRouterInfo(byte[] data) {
        HashMap<String, String> hsmpResult = new HashMap();
        String s = getString(data, 4, 64).trim();
        int div = s.indexOf("=");
        if (div >= 0) {
            s = s.substring(div + 1).trim();
        }
        hsmpResult.put("IMEI", s);
        s = getString(data, 68, 64).trim();
        div = s.lastIndexOf(" ");
        if (div >= 0) {
            s = s.substring(div + 1);
            div = s.indexOf("%");
            if (div > 0) {
                s = s.substring(0, div).trim();
            }
        }
        hsmpResult.put("CSQ", s);
        s = getString(data, 132, 64).trim();
        div = s.indexOf(" ");
        if (div > 0) {
            s = s.substring(0, div);
        }
        hsmpResult.put("FLOW", s);
        return hsmpResult;
    }

    private static String getString(byte[] data, int pos, int length) {
        int rlen = 0;
        int maxLen = length;
        if (data.length > pos + length) {
            maxLen = data.length - pos;
        }
        int i = 0;
        while (i < maxLen && data[i + pos] != (byte) 0) {
            rlen++;
            i++;
        }
        return new String(data, pos, rlen);
    }
}
