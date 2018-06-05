package com.incomrecycle.prms.rvm.service.comm.executor;

import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.prms.rvm.common.SysDef.TrafficType;
import com.incomrecycle.prms.rvm.service.comm.CheckHardware;
import com.incomrecycle.prms.rvm.service.comm.CommCmdExecutor;
import java.util.HashMap;

public class YC103PCheckCommCmdExecutor extends CheckHardware implements CommCmdExecutor {
    private static final String[] cmdSet = new String[]{"CHECK:START", "CHECK:END", "PLC:CHECK_OPEN", "PLC:RESET", "PLC:DOOR_OPEN", "PLC:DOOR_CLOSE", "PLC:LIGHT_ON", "PLC:LIGHT_FLASH", "PLC:LIGHT_OFF", "PLC:QRCODE_LIGHT_ON", "PLC:QRCODE_LIGHT_OFF", "PLC:BELT_FORWARD", "PLC:BELT_BACKWARD", "PLC:BELT_STOP", "PLC:CHECK_THIRD_LIGHT_STATE", "PLC:STORAGE_DOOR_OPEN", "PLC:NONE", "PLC:PAPER_DOOR_OPEN", "PLC:PAPER_DOOR_CLOSE", "PLC:RECYCLE_PAPER", "PLC:PAPER_WEIGH_RESET", "PLC:PAPER_LIGHT_ON", "PLC:PAPER_LIGHT_OFF", "DIGITALSCREEN:showMsg", "DIGITALSCREEN:reset", "BARCODE:CHECK_OPEN", "BARCODE:RESET", "BARCODE:READ", "PRINTER1:CHECK_OPEN", "PRINTER1:state", "PRINTER1:init", "PRINTER1:printer", "PRINTER1:cut", "PRINTER2:CHECK_OPEN", "PRINTER2:state", "PRINTER2:init", "PRINTER2:printer", "PRINTER2:cut", "ONECARD:readOneCardReader", "TRAFFICCARD:ReadCard"};

    public String execute(String cmd, String json) throws Exception {
        if ("CHECK:START".equalsIgnoreCase(cmd)) {
            executeHardware("PLC", cmd, json);
            executeHardware("BarCode", cmd, json);
            executeHardware("OneCardReader", cmd, json);
            executeHardware("TrafficCardModel", cmd, json);
            executeHardware("Printer1", cmd, json);
            executeHardware("Printer2", cmd, json);
            return null;
        } else if ("CHECK:END".equalsIgnoreCase(cmd)) {
            executeHardware("PLC", cmd, json);
            executeHardware("BarCode", cmd, json);
            executeHardware("OneCardReader", cmd, json);
            executeHardware("TrafficCardModel", cmd, json);
            executeHardware("Printer1", cmd, json);
            executeHardware("Printer2", cmd, json);
            return null;
        } else {
            int idx = cmd.indexOf(":");
            if (idx == -1) {
                return null;
            }
            String TYPE = cmd.substring(0, idx);
            String COMMCMD = cmd.substring(idx + 1);
            if ("PLC".equalsIgnoreCase(TYPE)) {
                return executeHardware("PLC", cmd, json);
            }
            if (TrafficType.BARCODE.equalsIgnoreCase(TYPE)) {
                return executeHardware("BarCode", cmd, json);
            }
            if ("ONECARD".equalsIgnoreCase(TYPE)) {
                return executeHardware("OneCardReader", cmd, json);
            }
            if (TrafficType.TRAFFICCARD.equalsIgnoreCase(TYPE)) {
                return executeHardware("TrafficCardModel", cmd, json);
            }
            if ("PRINTER1".equalsIgnoreCase(TYPE)) {
                return executeHardware("Printer1", cmd, json);
            }
            if ("PRINTER2".equalsIgnoreCase(TYPE)) {
                return executeHardware("Printer2", cmd, json);
            }
            if (!"DIGITALSCREEN".equalsIgnoreCase(TYPE)) {
                return null;
            }
            if ("DIGITALSCREEN:showMsg".equalsIgnoreCase(cmd)) {
                HashMap hsmpDSParam = new HashMap();
                hsmpDSParam.put("MSG", "0");
                json = JSONUtils.toJSON(hsmpDSParam);
            }
            return executeHardware("DigitalScreen", COMMCMD, json);
        }
    }

    public String[] getCmdSet() {
        return cmdSet;
    }
}
