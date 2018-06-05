package com.incomrecycle.prms.rvm.service.comm.executor;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.prms.rvm.service.comm.CheckHardware;
import com.incomrecycle.prms.rvm.service.comm.CommCmdExecutor;

public class PLC103SCommCmdExecutor extends CheckHardware implements CommCmdExecutor {
    private static final String[] cmdSet = new String[]{"RECYCLE_START", "RECYCLE_STOP", "RECYCLE_PAUSE", "BARCODE_RESET", "BARCODE_READ", "BOTTLE_ACCEPT_READY", "BOTTLE_REJECT_READY", "OPEN_FRONT_DOOR", "RVM_POWER_OFF_DISABLE", "RVM_POWER_OFF_ENABLE", "RVM_CLOCK_CALIBRATION", "SET_RVM_ALIVE_TIME", "CLEAR_RVM_ALIVE_TIME", "HEART_BEAT", "QRCODE_LIGHT_ON", "QRCODE_LIGHT_OFF", "WEIGH_READ", "CLEAR_WEIGH", "SET_SECOND_LIGHT_ON", "QUERY_LIGHT_STATE", "CLOSE_BOTTLE_DOOR", "OPEN_BOTTLE_DOOR", "QUERY_BOTTLE_DOOR_STATE"};

    public String execute(String cmd, String json) throws Exception {
        if ("RECYCLE_START".equalsIgnoreCase(cmd)) {
            executeHardware("PLC", "RECYCLE_START", json);
            return null;
        } else if ("RECYCLE_STOP".equalsIgnoreCase(cmd)) {
            executeHardware("PLC", "RECYCLE_END", json);
            return null;
        } else if ("RECYCLE_PAUSE".equalsIgnoreCase(cmd)) {
            executeHardware("PLC", "RECYCLE_PAUSE", json);
            return null;
        } else if ("BARCODE_RESET".equalsIgnoreCase(cmd)) {
            return executeHardware("BarCode", "reset", json);
        } else {
            if ("BARCODE_READ".equalsIgnoreCase(cmd)) {
                return executeHardware("BarCode", "read", json);
            }
            if ("BOTTLE_ACCEPT_READY".equalsIgnoreCase(cmd)) {
                executeHardware("PLC", "RECYCLE_ENABLE", json);
                return null;
            } else if ("BOTTLE_REJECT_READY".equalsIgnoreCase(cmd)) {
                return executeHardware("PLC", "RECYCLE_DISABLE", json);
            } else {
                if ("OPEN_FRONT_DOOR".equalsIgnoreCase(cmd)) {
                    executeHardware("PLC", "STORAGE_DOOR_OPEN", json);
                    return null;
                } else if ("RVM_POWER_OFF_DISABLE".equalsIgnoreCase(cmd)) {
                    executeHardware("PLC", "RVM_POWER_OFF_DISABLE", json);
                    return null;
                } else if ("RVM_POWER_OFF_ENABLE".equalsIgnoreCase(cmd)) {
                    return executeHardware("PLC", "RVM_POWER_OFF_ENABLE", json);
                } else {
                    if ("RVM_CLOCK_CALIBRATION".equalsIgnoreCase(cmd)) {
                        return executeHardware("PLC", "RVM_CLOCK_CALIBRATION", json);
                    }
                    if ("WEIGH_READ".equalsIgnoreCase(cmd)) {
                        return executeHardware("Weigh", "read", json);
                    }
                    if ("QRCODE_LIGHT_ON".equalsIgnoreCase(cmd)) {
                        return executeHardware("PLC", "QRCODE_LIGHT_ON", json);
                    }
                    if ("QRCODE_LIGHT_OFF".equalsIgnoreCase(cmd)) {
                        return executeHardware("PLC", "QRCODE_LIGHT_OFF", json);
                    }
                    if ("SET_RVM_ALIVE_TIME".equalsIgnoreCase(cmd)) {
                        return executeHardware("PLC", "SET_RVM_ALIVE_TIME", json);
                    }
                    if ("CLEAR_RVM_ALIVE_TIME".equalsIgnoreCase(cmd)) {
                        return executeHardware("PLC", "CLEAR_RVM_ALIVE_TIME", json);
                    }
                    if ("HEART_BEAT".equalsIgnoreCase(cmd)) {
                        return executeHardware("PLC", "HEART_BEAT", json);
                    }
                    if ("CLEAR_WEIGH".equalsIgnoreCase(cmd) && "true".equalsIgnoreCase(SysConfig.get("COM.WEIGH.ENABLE"))) {
                        executeHardware("Weigh", "reset", json);
                    }
                    if ("SET_SECOND_LIGHT_ON".equalsIgnoreCase(cmd)) {
                        executeHardware("PLC", "SET_SECOND_LIGHT_ON", json);
                    }
                    if ("QUERY_LIGHT_STATE".equalsIgnoreCase(cmd)) {
                        return executeHardware("PLC", "QUERY_LIGHT_STATE", json);
                    }
                    if ("CLOSE_BOTTLE_DOOR".equalsIgnoreCase(cmd)) {
                        executeHardware("PLC", "DOOR_CLOSE", json);
                    }
                    if ("OPEN_BOTTLE_DOOR".equalsIgnoreCase(cmd)) {
                        executeHardware("PLC", "DOOR_OPEN", json);
                    }
                    if ("QUERY_BOTTLE_DOOR_STATE".equalsIgnoreCase(cmd)) {
                        return executeHardware("PLC", "QUERY_BOTTLE_DOOR_STATE", json);
                    }
                    return null;
                }
            }
        }
    }

    public String[] getCmdSet() {
        return cmdSet;
    }
}
