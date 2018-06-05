package com.incomrecycle.prms.rvm.service.comm.entity.serialcomm;

import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public abstract class SerialComm {
    private InputStream is = null;
    private OutputStream os = null;
    private boolean recvEnable;
    private SerialPort serialPort = null;

    private class RecvThread extends Thread {

        public void run() {
            byte[] v0 = null;
            try {
                 v0 = new byte[2014];
                if (is.read() == 0) {
                    is.close();
                    return;
                }
                if (is.read(v0) <= 0) {
                    return;
                }
                recvFrom(v0, 0, is.read(v0));
            } catch (Exception e) {
                e.printStackTrace();
                v0 = new byte[0];
                try {
                    recvFrom(v0, 0, is.read(v0));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    is.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    protected abstract boolean checkComm(InputStream inputStream, OutputStream outputStream);

    protected abstract void recvFrom(byte[] bArr, int i, int i2);

    public SerialComm(String comm, String baud, int dataBits, int stopBits, boolean hasCRC, boolean recvEnable) throws IOException {
        if (!StringUtils.isBlank(baud)) {
            String[] listBaud = baud.split(",");
            for (int i = 0; i < listBaud.length; i++) {
                if (!StringUtils.isBlank(listBaud[i])) {
                    close();
                    this.serialPort = new SerialPort(new File(comm), Integer.parseInt(listBaud[i].trim()), 0);
                    this.is = this.serialPort.getInputStream();
                    this.os = this.serialPort.getOutputStream();
                    if (checkComm(this.is, this.os)) {
                        break;
                    }
                }
            }
            if (this.serialPort == null) {
                this.serialPort = new SerialPort(new File(comm), Integer.parseInt(listBaud[0]), 0);
                this.is = this.serialPort.getInputStream();
                this.os = this.serialPort.getOutputStream();
            }
            if (this.serialPort != null) {
                this.recvEnable = recvEnable;
                if (this.recvEnable && this.is != null) {
                    SysGlobal.execute(new RecvThread(), 10);
                }
            }
        }
    }

    public boolean isOpen() {
        return this.serialPort != null;
    }

    public void close() {
        synchronized (this) {
            if (this.serialPort != null) {
                this.serialPort.close();
                this.serialPort = null;
                this.is = null;
                this.os = null;
            }
        }
    }

    protected boolean sendTo(byte[] cmd, int pos, int len) {
        try {
            synchronized (this) {
                if (isOpen()) {
                    this.os.write(cmd, pos, len);
                    this.os.flush();
                    return true;
                }
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            close();
            return false;
        }
    }
}
