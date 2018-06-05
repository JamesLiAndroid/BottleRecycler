package com.incomrecycle.common.comm.rvm;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.monitor.NetMonitor;
import com.incomrecycle.common.utils.DataBuffer;
import com.incomrecycle.common.utils.SocketUtils;
import com.incomrecycle.prms.rvm.common.SysDef.TrafficType;
import com.incomrecycle.prms.rvm.service.traffic.TrafficEntity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class RVMRemoteSession {
    private static final Logger logger = LoggerFactory.getLogger("RVMRemoteSession");
    private DataBuffer dataBuffer;
    private String host;
    private InputStream inputStream;
    private long milliseconds;
    private OutputStream outputStream;
    private int port;
    private Socket socket;

    public RVMRemoteSession(String host, int port) {
        this(host, port, 0);
    }

    public RVMRemoteSession(String host, int port, long timeoutMilliSeconds) {
        this.host = null;
        this.port = 0;
        this.socket = null;
        this.inputStream = null;
        this.outputStream = null;
        this.dataBuffer = new DataBuffer();
        this.host = host;
        this.port = port;
        this.milliseconds = timeoutMilliSeconds;
        connect();
    }

    public RVMRemoteSession(Socket socket) {
        this.host = null;
        this.port = 0;
        this.socket = null;
        this.inputStream = null;
        this.outputStream = null;
        this.dataBuffer = new DataBuffer();
        this.socket = socket;
        try {
            socket.setKeepAlive(true);
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
        } catch (Exception e) {
        }
    }

    public void setTimeout(int milliseconds) {
        if (this.socket != null) {
            try {
                this.socket.setSoTimeout(milliseconds);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean connect() {
        if (isConnect()) {
            return true;
        }
        try {
            this.socket = SocketUtils.createSocket(this.host, this.port, this.milliseconds);
            this.socket.setKeepAlive(true);
            this.inputStream = this.socket.getInputStream();
            this.outputStream = this.socket.getOutputStream();
        } catch (Exception e) {
        }
        if (this.socket == null) {
            return false;
        }
        return true;
    }

    public boolean isConnect() {
        return this.socket != null && this.socket.isConnected();
    }

    public boolean disconnect() {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
            }
            this.socket = null;
            this.inputStream = null;
            this.outputStream = null;
            this.dataBuffer.clear();
        }
        return false;
    }

    public void send(RVMRemotePkg Pkg) throws Exception {
        synchronized (this) {
            if (isConnect()) {
                byte[] data = Pkg.getPkgData();
                logger.debug("SEND_LENGTH:" + data.length);
                this.outputStream.write(data);
                NetMonitor.addUploadSize((long) data.length);
                TrafficEntity.addData(TrafficType.RCC, data.length);
            }
        }
    }

    public RVMRemotePkg read() throws Exception {
        RVMRemotePkg pkg = null;
        synchronized (this) {
            byte[] data = new byte[1024];
            int startIdx = 0;
            loop0:
            while (isConnect()) {
                for (int i = startIdx; i < this.dataBuffer.length(); i++) {
                    if (RVMRemotePkg.isEndFlag(this.dataBuffer.get(i))) {
                        int dataLen = i + 1;
                        pkg = RVMRemotePkg.parse(this.dataBuffer.get(0, dataLen));
                        this.dataBuffer.remove(dataLen);
                        logger.debug("RECV_LENGTH:" + dataLen);
                        break loop0;
                    }
                }
                try {
                    startIdx = this.dataBuffer.length();
                    int readLen = this.inputStream.read(data);
                    if (readLen <= 0) {
                        disconnect();
                        break;
                    }
                    this.dataBuffer.append(data, 0, readLen);
                    NetMonitor.addDownloadSize((long) readLen);
                    TrafficEntity.addData(TrafficType.RCC, readLen);
                } catch (Exception e) {
                    disconnect();
                    throw e;
                }
            }
        }
        return pkg;
    }

    public void reset() {
        this.dataBuffer.clear();
    }
}
