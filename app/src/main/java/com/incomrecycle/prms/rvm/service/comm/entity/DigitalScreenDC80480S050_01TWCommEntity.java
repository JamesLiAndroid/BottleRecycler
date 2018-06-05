package com.incomrecycle.prms.rvm.service.comm.entity;

import android.support.v4.view.MotionEventCompat;

import com.google.code.microlog4android.appender.SyslogMessage;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.HardwareAlarmState;
import com.incomrecycle.prms.rvm.service.comm.CommEntity;

import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import android_serialport_api.SerialPort;

public class DigitalScreenDC80480S050_01TWCommEntity implements CommEntity {
    private static final byte[] CMD_CLEAR = new byte[]{(byte) -18, (byte) 1, (byte) -1, (byte) -4, (byte) -1, (byte) -1};
    private static final byte[] CMD_TEXT_HEAD = new byte[]{(byte) -18, (byte) 32, (byte) 0, (byte) 100, (byte) 0, (byte) 100, (byte) 1, (byte) 9};
    private static final byte[] CMD_TEXT_TAIL = new byte[]{(byte) -1, (byte) -4, (byte) -1, (byte) -1};
    private static final short COLOR_BLACK = (short) 0;
    private static final short COLOR_GREEN = (short) 1024;
    private static final short COLOR_RED = (short) -2048;
    private static final short COLOR_WIGHT = (short) -1;
    private HardwareAlarmState digitalScreenState = HardwareAlarmState.UNKNOWN;
    private SerialPort screenSerialPort = null;

    public void init() {
        synchronized (this) {
            try {
                if (this.screenSerialPort == null) {
                    this.screenSerialPort = new SerialPort(new File(SysConfig.get("COM.DIGITALSCREEN." + SysConfig.get("PLATFORM"))), Integer.parseInt(SysConfig.get("COM.DIGITALSCREEN.BAND")), 0);
                    this.digitalScreenState = HardwareAlarmState.UNKNOWN;
                }
            } catch (Exception e) {
            }
        }
    }

    private static byte[] setBgcolor(short color) {
        return new byte[]{(byte) -18, (byte) 66, (byte) ((color >> 8) & MotionEventCompat.ACTION_MASK), (byte) (color & MotionEventCompat.ACTION_MASK), (byte) -1, (byte) -4, (byte) -1, (byte) -1};
    }

    private static byte[] changeSurface(int num) {
        return new byte[]{(byte) -18, (byte) -79, (byte) 0, (byte) 0, (byte) num, (byte) -1, (byte) -4, (byte) -1, (byte) -1};
    }

    private byte[] generateShowMsgCmd(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        byte[] strBytes = str.getBytes();
        int dataLen = strBytes.length;
        if (dataLen > 252) {
            dataLen = 252;
        }
        if (dataLen > 4) {
            dataLen = 4;
        }
        if (dataLen < 4) {
            while (strBytes.length < 4) {
                strBytes[strBytes.length] = (byte) 0;
            }
            dataLen = 4;
        }
        byte[] data = new byte[15];
        data[0] = (byte) -18;
        data[1] = (byte) -79;
        data[2] = SyslogMessage.FACILITY_LOCAL_USE_0;
        data[3] = (byte) 0;
        data[4] = (byte) 3;
        data[5] = (byte) 0;
        data[6] = (byte) 2;
        System.arraycopy(strBytes, 0, data, 7, dataLen);
        data[11] = (byte) -1;
        data[12] = (byte) -4;
        data[13] = (byte) -1;
        data[14] = (byte) -1;
        return data;
    }

    private static byte[] clearShow() {
        return new byte[]{(byte) -18, (byte) -79, SyslogMessage.FACILITY_LOCAL_USE_0, (byte) 0, (byte) 3, (byte) 0, (byte) 2, (byte) -1, (byte) -4, (byte) -1, (byte) -1};
    }

    private static byte[] setForecolor(short color) {
        return new byte[]{(byte) -18, (byte) 65, (byte) ((color >> 8) & MotionEventCompat.ACTION_MASK), (byte) (color & MotionEventCompat.ACTION_MASK), (byte) -1, (byte) -4, (byte) -1, (byte) -1};
    }

    private static byte[] setText(String msg, String charset) {
        try {
            byte[] msgData;
            if (StringUtils.isBlank(charset)) {
                msgData = msg.getBytes();
            } else {
                msgData = msg.getBytes(charset);
            }
            byte[] data = new byte[((msgData.length + CMD_TEXT_HEAD.length) + CMD_TEXT_TAIL.length)];
            System.arraycopy(CMD_TEXT_HEAD, 0, data, 0, CMD_TEXT_HEAD.length);
            int dataLen = 0 + CMD_TEXT_HEAD.length;
            System.arraycopy(msgData, 0, data, dataLen, msgData.length);
            dataLen += msgData.length;
            System.arraycopy(CMD_TEXT_TAIL, 0, data, dataLen, CMD_TEXT_TAIL.length);
            int length = CMD_TEXT_TAIL.length + dataLen;
            return data;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String execute(String cmd, String json) throws Exception {
        init();
        OutputStream os = null;
        try {
            if ("enable".equalsIgnoreCase(cmd)) {
                if (this.screenSerialPort == null || this.digitalScreenState == HardwareAlarmState.ALARM) {
                    return "FALSE";
                }
                return "TRUE";
            } else if ("reset".equalsIgnoreCase(cmd)) {
                os = this.screenSerialPort.getOutputStream();
                os.write(clearShow());
                os.write(changeSurface(0));
                return null;
            } else if (!"showMsg".equalsIgnoreCase(cmd)) {
                return null;
            } else {
                HashMap hsmpDSParam = JSONUtils.toHashMap(json);
                String msg = null;
                if (hsmpDSParam != null) {
                    msg = (String) hsmpDSParam.get("MSG");
                    String charset = (String) hsmpDSParam.get(BasePrinter.PrinterOptions.SETTINGS_CHARSET);
                }
                os = this.screenSerialPort.getOutputStream();
                os.write(changeSurface(3));
                if (StringUtils.isBlank(msg)) {
                    return null;
                }
                os.write(generateShowMsgCmd(msg));
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            os.close();
        }
    }
}
