package com.incomrecycle.prms.rvm.service.comm.entity;

import com.google.code.microlog4android.appender.SyslogMessage;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.queue.FIFOQueue;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.HardwareAlarmState;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommEntity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

import android_serialport_api.SerialPort;

public class BarCodeLineCommEntity implements CommEntity {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private SerialPort barCodeSerialPort = null;
    private int errorTimes = 0;
    private FIFOQueue fifoQueue = new FIFOQueue();
    private int id;
    boolean isInCheck = false;
    private int maxErrorTimes = 30;
    private HardwareAlarmState readerState = HardwareAlarmState.UNKNOWN;
    private RecvThread recvThread = null;

    private class RecvThread extends Thread {
        private RecvThread() {
        }

        public void run() {
            InputStream is = BarCodeLineCommEntity.this.barCodeSerialPort.getInputStream();
            BarCodeLineCommEntity.this.baos.reset();
            try {
                byte[] data = new byte[256];
                while (true) {
                    int readLen = is.read(data);
                    if (readLen > 0) {
                        BarCodeLineCommEntity.this.errorTimes = 0;
                        synchronized (BarCodeLineCommEntity.this.baos) {
                            int i = 0;
                            while (i < readLen) {
                                BarCodeLineCommEntity.this.baos.write(data[i]);
                                if (data[i] == (byte) 10 || data[i] == SyslogMessage.FACILITY_LOG_AUDIT) {
                                    String barcode = StringUtils.trimToNull(new String(BarCodeLineCommEntity.this.baos.toByteArray()));
                                    BarCodeLineCommEntity.this.baos.reset();
                                    if (barcode != null) {
                                        BarCodeLineCommEntity.this.monitorError();
                                        BarCodeLineCommEntity.this.fifoQueue.reset();
                                        BarCodeLineCommEntity.this.fifoQueue.push(barcode + "\r\n");
                                        HashMap<String, String> hsmpParam = new HashMap();
                                        hsmpParam.put("type", "BARCODE_DATA");
                                        hsmpParam.put("data", barcode);
                                        hsmpParam.put("id", Integer.toString(BarCodeLineCommEntity.this.id));
                                        hsmpParam.put("time", DateUtils.formatDatetime(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
                                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                                    }
                                }
                                i++;
                            }
                        }
                    } else {
                        BarCodeLineCommEntity.this.barCodeSerialPort.close();
                        BarCodeLineCommEntity.this.barCodeSerialPort = null;
                        return;
                    }
                }
            } catch (Exception e) {
                try {
                    e.printStackTrace();
                } finally {
                    BarCodeLineCommEntity.this.barCodeSerialPort.close();
                    BarCodeLineCommEntity.this.barCodeSerialPort = null;
                }
            }
        }
    }

    public BarCodeLineCommEntity(int id) {
        this.id = id;
    }

    public void init() {
        synchronized (this) {
            try {
                if (this.barCodeSerialPort == null) {
                    this.barCodeSerialPort = new SerialPort(new File(SysConfig.get("COM.BARCODE" + this.id + "." + SysConfig.get("PLATFORM"))), Integer.parseInt(SysConfig.get("COM.BARCODE" + this.id + ".BAND")), 0);
                    this.maxErrorTimes = Integer.parseInt(SysConfig.get("BARCODE.ERROR.MAXTIMES"));
                    this.readerState = HardwareAlarmState.UNKNOWN;
                    this.recvThread = new RecvThread();
                    this.recvThread.start();
                }
            } catch (Exception e) {
            }
        }
    }

    private void monitorError() {
        if (this.errorTimes == this.maxErrorTimes && this.readerState != HardwareAlarmState.ALARM) {
            this.readerState = HardwareAlarmState.ALARM;
            HashMap<String, String> hsmpParam = new HashMap();
            hsmpParam.put("type", "BARCODE_READER_ERROR");
            ServiceGlobal.getCommEventQueye().push(hsmpParam);
        }
        if (this.errorTimes == 0 && this.readerState != HardwareAlarmState.NORMAL) {
            this.readerState = HardwareAlarmState.NORMAL;
            HashMap hsmpParam = new HashMap();
            hsmpParam.put("type", "BARCODE_READER_ERROR_RECOVERY");
            ServiceGlobal.getCommEventQueye().push(hsmpParam);
        }
    }

    public String execute(String cmd, String json) throws Exception {
        Throwable th;
        init();
        try {
            if ("CHECK:START".equalsIgnoreCase(cmd)) {
                this.isInCheck = true;
            }
            if ("CHECK:END".equalsIgnoreCase(cmd)) {
                this.isInCheck = false;
            }
            if ("BARCODE:CHECK_OPEN".equalsIgnoreCase(cmd)) {
                String str;
                if (this.barCodeSerialPort != null) {
                    str = "TRUE";
                } else {
                    str = "FALSE";
                }
                return str;
            }
            String ret;
            if (this.isInCheck) {
                if ("BARCODE:RESET".equalsIgnoreCase(cmd)) {
                    this.fifoQueue.reset();
                }
                if ("BARCODE:READ".equalsIgnoreCase(cmd)) {
                    ret = (String) this.fifoQueue.tryPop();
                    if (ret != null) {
                        return ret;
                    }
                    synchronized (this.baos) {
                        try {
                            String ret2 = new String(this.baos.toByteArray());
                            try {
                                return ret2;
                            } catch (Throwable th2) {
                                th = th2;
                                ret = ret2;
                                throw th;
                            }
                        } catch (Throwable th3) {
//                            th = th3;
//                            throw th3;
                        }
                    }
                } else if ("BARCODE:READ_SOURCE".equalsIgnoreCase(cmd)) {
                    synchronized (this.baos) {
                        try {
                            String recvData = new String(this.baos.toByteArray());
                            try {
                                return StringUtils.trimToNull(recvData);
                            } catch (Throwable th4) {
                                th = th4;
                                String str2 = recvData;
                                throw th;
                            }
                        } catch (Throwable th5) {
//                            th = th5;
//                            throw th;
                        }
                    }
                }
            }
            if ("reset".equalsIgnoreCase(cmd) || "ready_to_read".equalsIgnoreCase(cmd)) {
                this.fifoQueue.reset();
                return null;
            }
            if ("read".equalsIgnoreCase(cmd)) {
                ret = null;
                while (true) {
                    String barcode = (String) this.fifoQueue.tryPop();
                    if (barcode == null) {
                        break;
                    }
                    ret = barcode;
                }
                if (ret == null) {
                    this.errorTimes++;
                } else {
                    this.errorTimes = 0;
                }
                monitorError();
                return ret;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
