package com.incomrecycle.common.monitor;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.monitor.NetMonitor.NetMonitorClient;
import com.incomrecycle.common.queue.FIFOQueue;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MonitorService implements NetMonitorClient {
    private static final MonitorService monitorService = new MonitorService();
    private Thread clientThread;
    private FIFOQueue fifoQueue = new FIFOQueue();
    private boolean isRunning = false;
    private List<Socket> listSocket = new ArrayList();
    private int port;
    private ServerSocket serverSocket = null;

    private MonitorService() {
    }

    public static MonitorService getMonitorService() {
        return monitorService;
    }

    public void start(int port) {
        this.port = port;
        if (this.clientThread == null) {
            this.clientThread = new Thread() {
                public void run() {
                    String msg = "";
                    while (true) {
                        msg = (String) MonitorService.this.fifoQueue.pop();
                        if (msg == null) {
                            return;
                        }
                        if (!msg.isEmpty()) {
                            byte[] data = msg.getBytes();
                            List<Socket> listRunningSocket = new ArrayList();
                            synchronized (MonitorService.this.listSocket) {
                                listRunningSocket.addAll(MonitorService.this.listSocket);
                            }
                            for (int i = 0; i < listRunningSocket.size(); i++) {
                                Socket socket = (Socket) listRunningSocket.get(i);
                                try {
                                    socket.getOutputStream().write(data);
                                } catch (Exception e) {
                                    try {
                                        socket.close();
                                    } catch (Exception e2) {
                                    }
                                    synchronized (MonitorService.this.listSocket) {
                                        MonitorService.this.listSocket.remove(socket);
                                    }
                                }
                            }
                            continue;
                        }
                    }
                }
            };
            SysGlobal.execute(this.clientThread);
        }
        synchronized (this) {
            if (this.isRunning) {
                return;
            }
            this.isRunning = true;
            SysGlobal.execute(new Thread() {
                public void run() {
                    StrictMode.setThreadPolicy(new Builder().permitAll().build());
                    try {
                        MonitorService.this.serverSocket = new ServerSocket(MonitorService.this.port);
                        NetMonitor.addNetMonitorClient(MonitorService.this);
                        while (true) {
                            Socket socket = MonitorService.this.serverSocket.accept();
                            if (socket == null) {
                                break;
                            }
                            synchronized (MonitorService.this.listSocket) {
                                MonitorService.this.listSocket.add(socket);
                            }
                        }
                    } catch (Exception e) {
                        synchronized (MonitorService.this) {
                            e.printStackTrace();
                            try {
                                MonitorService.this.serverSocket.close();
                            } catch (Exception e2) {
                            }
                            MonitorService.this.serverSocket = null;
                        }
                    }
                    synchronized (MonitorService.this) {
                        MonitorService.this.isRunning = false;
                    }
                }
            });
        }
    }

    public void stop() {
        try {
            synchronized (this) {
                if (this.serverSocket != null) {
                    this.serverSocket.close();
                }
            }
        } catch (Exception e) {
        }
    }

    public void output(String str) {
        this.fifoQueue.push(str);
    }
}
