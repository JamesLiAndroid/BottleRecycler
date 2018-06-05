package com.incomrecycle.prms.rvm.service.comm.entity;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommEntity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import android_serialport_api.SerialPort;

public class HuiLifeReaderCommEntity implements CommEntity {
    private String finalCardNum = null;
    private SerialPort huiLifeCardSerialPort;

    public class RecvThread extends Thread {
        public void run() {
            InputStream is = HuiLifeReaderCommEntity.this.huiLifeCardSerialPort.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                byte[] data = new byte[1024];
                while (true) {
                    int readLen = is.read(data);
                    if (readLen <= 0) {
                        break;
                    }
                    for (int i = 0; i < readLen; i++) {
                        if (data[i] == (byte) 2) {
                            baos.reset();
                            baos.write(data[i]);
                        } else if (baos.size() > 0) {
                            baos.write(data[i]);
                            if (data[i] == (byte) 3) {
                                byte[] recvData = baos.toByteArray();
                                baos.reset();
                                String cardNum = new String(recvData, 1, recvData.length - 2).trim();
                                HuiLifeReaderCommEntity.this.finalCardNum = cardNum;
                                HashMap<String, String> hsmpParam = new HashMap();
                                hsmpParam.put("type", "HUILIFE_CARD_NUM");
                                hsmpParam.put("data", cardNum);
                                ServiceGlobal.getCommEventQueye().push(hsmpParam);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                HuiLifeReaderCommEntity.this.huiLifeCardSerialPort.close();
                HuiLifeReaderCommEntity.this.huiLifeCardSerialPort = null;
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

    public void init() {
        if (!StringUtils.isBlank(SysConfig.get("COM.HUILIFECARD." + SysConfig.get("PLATFORM"))) && !StringUtils.isBlank(SysConfig.get("COM.HUILIFECARD.BAND"))) {
            synchronized (this) {
                try {
                    if (this.huiLifeCardSerialPort == null) {
                        this.huiLifeCardSerialPort = new SerialPort(new File(SysConfig.get("COM.HUILIFECARD." + SysConfig.get("PLATFORM"))), Integer.parseInt(SysConfig.get("COM.HUILIFECARD.BAND")), 0);
                        this.finalCardNum = null;
                        SysGlobal.execute(new RecvThread());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
