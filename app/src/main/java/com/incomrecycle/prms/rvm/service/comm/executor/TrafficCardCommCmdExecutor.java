package com.incomrecycle.prms.rvm.service.comm.executor;

import com.incomrecycle.prms.rvm.service.comm.CheckHardware;
import com.incomrecycle.prms.rvm.service.comm.CommCmdExecutor;

public class TrafficCardCommCmdExecutor extends CheckHardware implements CommCmdExecutor {
    private static final String[] cmdSet = new String[]{"ONECARDMODEL_READCARDNO", "ONECARDMODEL_READCARD", "ONECARDMODEL_CHARGE", "ONECARDMODEL_CHARGE_WRITEBACK", "ONECARDMODEL_QUERY_QUICKCARD", "ONECARDMODEL_CHARGE_QUICKCARD", "ONECARDMODEL_CHARGE_SCRASHCARD"};

    public String execute(String cmd, String json) throws Exception {
        if ("ONECARDMODEL_READCARD".equalsIgnoreCase(cmd)) {
            return executeHardware("TrafficCardModel", "ReadCard", json);
        }
        if ("ONECARDMODEL_READCARDNO".equalsIgnoreCase(cmd)) {
            return executeHardware("TrafficCardModel", "ReadCardNo", json);
        }
        if ("ONECARDMODEL_CHARGE".equalsIgnoreCase(cmd)) {
            return executeHardware("TrafficCardModel", "Charge", json);
        }
        if ("ONECARDMODEL_CHARGE_WRITEBACK".equalsIgnoreCase(cmd)) {
            return executeHardware("TrafficCardModel", "ChargeWriteBack", json);
        }
        if ("ONECARDMODEL_QUERY_QUICKCARD".equalsIgnoreCase(cmd)) {
            return executeHardware("TrafficCardModel", "QueryQuickCard", json);
        }
        if ("ONECARDMODEL_CHARGE_QUICKCARD".equalsIgnoreCase(cmd)) {
            return executeHardware("TrafficCardModel", "ChargeQuickCard", json);
        }
        if ("ONECARDMODEL_CHARGE_SCRASHCARD".equalsIgnoreCase(cmd)) {
            return executeHardware("TrafficCardModel", "ChargeScrashCard", json);
        }
        return null;
    }

    public String[] getCmdSet() {
        return cmdSet;
    }
}
