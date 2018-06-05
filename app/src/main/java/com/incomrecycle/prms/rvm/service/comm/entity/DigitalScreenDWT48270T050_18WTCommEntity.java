package com.incomrecycle.prms.rvm.service.comm.entity;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.HardwareAlarmState;
import com.incomrecycle.prms.rvm.service.comm.CommEntity;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;

import android_serialport_api.SerialPort;

public class DigitalScreenDWT48270T050_18WTCommEntity implements CommEntity {
    private static final String BLANK_DATA = "          ";
    private static final Logger logger = LoggerFactory.getLogger("ScreenCommEntity");
    private static int showLen = 0;
    private HardwareAlarmState digitalScreenState = HardwareAlarmState.UNKNOWN;
    private String prevMsg = null;
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

    private byte[] generateShowMsgCmd(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        byte[] strBytes = str.getBytes();
        int dataLen = strBytes.length;
        if (dataLen > 252) {
            dataLen = 252;
        }
        showLen = dataLen + 3;
        byte[] data = new byte[(dataLen + 6)];
        data[0] = (byte) -91;
        data[1] = (byte) 90;
        data[2] = (byte) (dataLen + 3);
        data[3] = (byte) -126;
        data[4] = (byte) 1;
        data[5] = (byte) 0;
        System.arraycopy(strBytes, 0, data, 6, dataLen);
        return data;
    }

    private static byte[] clearShow() {
        return new byte[]{(byte) -91, (byte) 90, (byte) showLen, (byte) -126, (byte) 1, (byte) 0, (byte) 0};
    }

    public String execute(String cmd, String json) throws Exception {
        init();
        try {
            if ("enable".equalsIgnoreCase(cmd)) {
                if (this.screenSerialPort == null || this.digitalScreenState == HardwareAlarmState.ALARM) {
                    return "FALSE";
                }
                return "TRUE";
            } else if ("reset".equalsIgnoreCase(cmd)) {
                OutputStream os = this.screenSerialPort.getOutputStream();
                os.write(clearShow());
                Thread.sleep(500);
                os.write(clearShow());
                return null;
            } else if (!"showMsg".equalsIgnoreCase(cmd)) {
                return null;
            } else {
                HashMap hsmpDSParam = JSONUtils.toHashMap(json);
                String newMsg = null;
                if (hsmpDSParam != null) {
                    newMsg = (String) hsmpDSParam.get("MSG");
                    if ("0".equals(newMsg)) {
                        newMsg = "0.00";
                    }
                }
                byte[] screenCmd = null;
                if (!(newMsg == null || "".equals(newMsg))) {
                    screenCmd = generateShowMsgCmd(newMsg);
                }
                if (screenCmd == null) {
                    return null;
                }
                this.screenSerialPort.getOutputStream().write(screenCmd);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
