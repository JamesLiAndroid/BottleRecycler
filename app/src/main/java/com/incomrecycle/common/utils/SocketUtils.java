package com.incomrecycle.common.utils;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.os.StrictMode.VmPolicy;
import com.incomrecycle.common.SysGlobal;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

public class SocketUtils {
    private static final String LOCAL_IP = "127.0.0.1";

    private static class SocketClientThread implements Runnable {
        private SocketInfo socketInfo;

        public SocketClientThread(SocketInfo socketInfo) {
            this.socketInfo = socketInfo;
        }

        public void run() {
            StrictMode.setThreadPolicy(new Builder().detectDiskReads().detectDiskWrites().detectNetwork().detectAll().penaltyLog().permitAll().build());
            StrictMode.setVmPolicy(new VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
            try {
                InetAddress[] inetAddresses = InetAddress.getAllByName(this.socketInfo.ip);
                if (inetAddresses == null) {
                    this.socketInfo.unknownHostException = new UnknownHostException("Cannot get InetAddress by name");
                } else if (inetAddresses.length == 0) {
                    this.socketInfo.unknownHostException = new UnknownHostException("Cannot get InetAddress by name");
                } else {
                    this.socketInfo.ip = inetAddresses[0].getHostAddress();
                    this.socketInfo.socket = new Socket(this.socketInfo.ip, this.socketInfo.port);
                }
            } catch (UnknownHostException e) {
                this.socketInfo.unknownHostException = e;
            } catch (IOException e2) {
                this.socketInfo.ioException = e2;
            }
            synchronized (this.socketInfo) {
                if (!this.socketInfo.isDone) {
                    this.socketInfo.isFinished = true;
                    this.socketInfo.notify();
                } else if (this.socketInfo.socket != null) {
                    this.socketInfo.socket = SocketUtils.close(this.socketInfo.socket);
                }
            }
        }
    }

    private static class SocketInfo {
        private IOException ioException;
        private String ip;
        private boolean isDone;
        private boolean isFinished;
        private int port;
        private Socket socket;
        private UnknownHostException unknownHostException;

        private SocketInfo() {
            this.socket = null;
            this.isFinished = false;
            this.isDone = false;
            this.unknownHostException = null;
            this.ioException = null;
        }
    }

    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> enumNI = NetworkInterface.getNetworkInterfaces();
            while (enumNI.hasMoreElements()) {
                List<InterfaceAddress> listInterfaceAddress = ((NetworkInterface) enumNI.nextElement()).getInterfaceAddresses();
                for (int i = 0; i < listInterfaceAddress.size(); i++) {
                    InetAddress inetAddress = ((InterfaceAddress) listInterfaceAddress.get(i)).getAddress();
                    if (inetAddress instanceof Inet4Address) {
                        String hostAddress = inetAddress.getHostAddress();
                        if (!hostAddress.startsWith("127.0.0.1")) {
                            return hostAddress;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return "127.0.0.1";
    }

    public static Socket close(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Socket createSocket(String ip, int port, long timeoutMilliseconds) throws IOException, InterruptedException {
        SocketInfo socketInfo = new SocketInfo();
        socketInfo.ip = ip;
        socketInfo.port = port;
        if (timeoutMilliseconds < 0) {
            timeoutMilliseconds = 0;
        }
        long lEndTime = System.currentTimeMillis() + timeoutMilliseconds;
        synchronized (socketInfo) {
            SysGlobal.execute(new SocketClientThread(socketInfo));
            while (!socketInfo.isFinished) {
                if (timeoutMilliseconds <= 0) {
                    try {
                        socketInfo.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    long lTime = System.currentTimeMillis();
                    if (lTime >= lEndTime) {
                        socketInfo.isDone = true;
                        break;
                    }
                    socketInfo.wait(lEndTime - lTime);
                }
            }
        }
        if (socketInfo.ioException != null) {
            throw socketInfo.ioException;
        } else if (socketInfo.unknownHostException != null) {
            throw socketInfo.unknownHostException;
        } else if (socketInfo.isFinished) {
            return socketInfo.socket;
        } else {
            throw new UnknownHostException("TIMEOUT");
        }
    }
}
