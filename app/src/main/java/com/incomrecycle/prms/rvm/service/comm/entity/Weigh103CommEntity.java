package com.incomrecycle.prms.rvm.service.comm.entity;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.SyslogMessage;
import com.google.code.microlog4android.format.SimpleFormatter;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.prms.rvm.common.SysDef.HardwareAlarmState;
import com.incomrecycle.prms.rvm.service.comm.CommEntity;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class Weigh103CommEntity implements CommEntity {
    private static final Logger logger = LoggerFactory.getLogger("WeighCommEntity");
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private RecvThread recvThread = null;
    private SerialPort weighSerialPort = null;
    private HardwareAlarmState weighState = HardwareAlarmState.UNKNOWN;

    private class RecvThread extends Thread {
        private RecvThread() {
        }

        public void run() {
            DataInputStream is = new DataInputStream(Weigh103CommEntity.this.weighSerialPort.getInputStream());
            byte[] buffer = new byte[1];
            try {
                byte[] data = new byte[256];
                while (true) {
                    int readLen = is.read(data);
                    if (readLen <= 0) {
                        break;
                    }
                    synchronized (Weigh103CommEntity.this.baos) {
                        Weigh103CommEntity.this.baos.write(data, 0, readLen);
                    }
                }
            } catch (Exception e) {
            } finally {
                Weigh103CommEntity.this.weighSerialPort.close();
                Weigh103CommEntity.this.weighSerialPort = null;
            }
        }
    }

    public void init() {
        synchronized (this) {
            try {
                if (this.weighSerialPort == null) {
                    this.weighSerialPort = new SerialPort(new File(SysConfig.get("COM.WEIGH." + SysConfig.get("PLATFORM"))), Integer.parseInt(SysConfig.get("COM.WEIGH.BAND")), 0);
                    this.weighState = HardwareAlarmState.UNKNOWN;
                    this.recvThread = new RecvThread();
                    SysGlobal.execute(this.recvThread);
                }
            } catch (Exception e) {
            }
        }
    }

    public String execute(String cmd, String json) throws Exception {
        init();
        try {
            if ("enable".equalsIgnoreCase(cmd)) {
                if (this.weighSerialPort == null || this.weighState == HardwareAlarmState.ALARM) {
                    return "FALSE";
                }
                return "TRUE";
            } else if ("reset".equalsIgnoreCase(cmd)) {
                this.weighSerialPort.getOutputStream().write(new byte[]{(byte) 35, (byte) 48, (byte) 49, (byte) 90, SyslogMessage.FACILITY_LOG_AUDIT});
                return null;
            } else {
                if ("read".equalsIgnoreCase(cmd)) {
                    OutputStream os = this.weighSerialPort.getOutputStream();
                    byte[] bufferRead = new byte[]{(byte) 35, (byte) 48, (byte) 49, SyslogMessage.FACILITY_LOG_AUDIT};
                    this.baos.reset();
                    os.write(bufferRead);
                    String weighret = null;
                    Thread.sleep(500);
                    synchronized (this.baos) {
                        byte[] data = this.baos.toByteArray();
                        if (data != null && data.length > 0) {
                            weighret = new String(data);
                        }
                    }
                    if (weighret == null) {
                        return null;
                    }
                    String flag = weighret.substring(1, 2);
                    if ("+".equals(flag)) {
                        return weighret.substring(2, 7);
                    }
                    if (SimpleFormatter.DEFAULT_DELIMITER.equals(flag)) {
                        return weighret.substring(2, 7);
                    }
                    return "0";
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
