package com.incomrecycle.prms.rvm.service.comm.entity;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;

import android_serialport_api.SerialPort;

public class MagneticStripeCardCommEntity implements CommEntity {
    private String finalCardNum = null;
    private SerialPort msCardSerialPort = null;
    private RecvThread recvThread = null;

    private class RecvThread extends Thread {
        private RecvThread() {
        }

        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(MagneticStripeCardCommEntity.this.msCardSerialPort.getInputStream()));
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    String cardNum = MagneticStripeCardCommEntity.retriveCardNum(line);
                    if (!StringUtils.isBlank(cardNum)) {
                        MagneticStripeCardCommEntity.this.finalCardNum = cardNum;
                        HashMap<String, String> hsmpParam = new HashMap();
                        hsmpParam.put("type", "MAGNETIC_CARD_NUM");
                        hsmpParam.put("data", cardNum);
                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                MagneticStripeCardCommEntity.this.msCardSerialPort.close();
                MagneticStripeCardCommEntity.this.msCardSerialPort = null;
            }
        }
    }

    public void init() {
        if (!StringUtils.isBlank(SysConfig.get("COM.MAGNETICCARD." + SysConfig.get("PLATFORM"))) && !StringUtils.isBlank(SysConfig.get("COM.MAGNETICCARD.BAND"))) {
            synchronized (this) {
                try {
                    if (this.msCardSerialPort == null) {
                        this.msCardSerialPort = new SerialPort(new File(SysConfig.get("COM.MAGNETICCARD." + SysConfig.get("PLATFORM"))), Integer.parseInt(SysConfig.get("COM.MAGNETICCARD.BAND")), 0);
                        this.recvThread = new RecvThread();
                        SysGlobal.execute(this.recvThread);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    public String execute(String cmd, String json) throws Exception {
        init();
        try {
            if ("RESET".equalsIgnoreCase(cmd)) {
                this.finalCardNum = null;
                return null;
            }
            if ("READ".equalsIgnoreCase(cmd)) {
                String cardNum = this.finalCardNum;
                this.finalCardNum = null;
                return cardNum;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String retriveNum(String str) {
        if (str == null) {
            return null;
        }
        int iStart = -1;
        int iEnd = -1;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= '0' && c <= '9') {
                iEnd = i;
                if (iStart == -1) {
                    iStart = i;
                }
            } else if (iStart != -1) {
                break;
            }
        }
        if (iStart != -1) {
            return str.substring(iStart, iEnd + 1);
        }
        return null;
    }

    private static String retriveCardNum(String str) {
        int idx = str.indexOf(";");
        if (idx != -1) {
            return str.substring(idx + 1, str.indexOf("=", idx));
        }
        idx = str.indexOf("%");
        if (idx != -1) {
            return retriveNum(str.substring(idx + 1));
        }
        idx = str.indexOf("+");
        if (idx != -1) {
            return retriveNum(str.substring(idx + 3));
        }
        return str;
    }
}
