package com.incomrecycle.prms.rvm.service.comm.executor.RecyclePaper;

import com.incomrecycle.common.task.TickTaskThread;
import com.incomrecycle.prms.rvm.common.SysDef.DoorStatus;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CheckHardware;
import com.incomrecycle.prms.rvm.service.comm.CommCmdExecutor;
import com.incomrecycle.prms.rvm.service.task.action.WeighReadTaskAction;
import java.util.HashMap;

public class PLC103PRecyclePaperCommCmdExecutor extends CheckHardware implements CommCmdExecutor {
    private static final String[] cmdSet = new String[]{"RECYCLE_PAPER_START", "RECYCLE_PAPER_STOP", "RECYCLE_PAPER_FINISH", "RECYCLE_PAPER", "CHECK_PAPER_WEIGH", "DIGITALSCREEN_SHOWTEXT", "CHECK_PAPER_DOOR_STATE", "PAPER_BARCODE_LIGHT_ON", "PAPER_BARCODE_LIGHT_OFF", "OPEN_PAPER_FRONT_DOOR", "INFORM_ENTITY_TO_THANKS", "DIGITALSCREEN_SHOWTEXT_RESET"};
    private boolean isWeighReadTaskAction = false;
    private WeighReadTaskAction weighReadTaskAction = new WeighReadTaskAction();

    public String execute(String cmd, String json) throws Exception {
        if ("RECYCLE_PAPER_START".equalsIgnoreCase(cmd)) {
            executeHardware("PLC", "RECYCLE_PAPER_START", json);
            executeHardware("Weigh", "reset", json);
            executeHardware("PLC", "PAPER_WEIGH_RESET", json);
            executeHardware("PLC", DoorStatus.PAPER_DOOR_OPEN, json);
            TickTaskThread.getTickTaskThread().register(this.weighReadTaskAction, 0.5d, true);
            this.isWeighReadTaskAction = true;
            return null;
        } else if ("RECYCLE_PAPER_STOP".equalsIgnoreCase(cmd)) {
            executeHardware("PLC", DoorStatus.PAPER_DOOR_CLOSE, "FORCE_CLOSE_DOOR");
            return null;
        } else if ("RECYCLE_PAPER_FINISH".equalsIgnoreCase(cmd)) {
            if (this.isWeighReadTaskAction) {
                TickTaskThread.getTickTaskThread().unregister(this.weighReadTaskAction);
                this.isWeighReadTaskAction = false;
            }
            executeHardware("PLC", DoorStatus.PAPER_DOOR_CLOSE, "RECYCLE_PAPER_FINISH");
            return null;
        } else if ("RECYCLE_PAPER".equalsIgnoreCase(cmd)) {
            executeHardware("PLC", "RECYCLE_PAPER", json);
            return null;
        } else if ("CHECK_PAPER_WEIGH".equalsIgnoreCase(cmd)) {
            String paperWeigh = executeHardware("Weigh", "read", json);
            if (paperWeigh == null) {
                paperWeigh = "0";
            }
            HashMap<String, String> hsmpParam;
            if ("PUT_PAPER_FAIL_OPT".equalsIgnoreCase(json)) {
                hsmpParam = new HashMap();
                hsmpParam.put("type", "PAPER_RECYCLE_OPT");
                hsmpParam.put("data", paperWeigh);
                hsmpParam.put("opt", "PUT_PAPER_FAIL_OPT");
                ServiceGlobal.getCommEventQueye().push(hsmpParam);
                return paperWeigh;
            }
            hsmpParam = new HashMap();
            hsmpParam.put("type", "PAPER_RECYCLE_OPT");
            hsmpParam.put("data", paperWeigh);
            ServiceGlobal.getCommEventQueye().push(hsmpParam);
            return paperWeigh;
        } else if ("DIGITALSCREEN_SHOWTEXT".equalsIgnoreCase(cmd)) {
            executeHardware("PLC", "RECYCLE_PAPER_END", json);
            executeHardware("DigitalScreen", "showMsg", json);
            return null;
        } else if ("DIGITALSCREEN_SHOWTEXT_RESET".equalsIgnoreCase(cmd)) {
            executeHardware("DigitalScreen", "reset", json);
            return null;
        } else if ("CHECK_PAPER_DOOR_STATE".equalsIgnoreCase(cmd)) {
            return executeHardware("PLC", "CHECK_PAPER_DOOR_STATE", json);
        } else {
            if ("PAPER_BARCODE_LIGHT_ON".equalsIgnoreCase(cmd)) {
                executeHardware("PLC", "PAPER_BARCODE_LIGHT_ON", json);
                return null;
            } else if ("PAPER_BARCODE_LIGHT_OFF".equalsIgnoreCase(cmd)) {
                executeHardware("PLC", "PAPER_BARCODE_LIGHT_OFF", json);
                return null;
            } else if ("OPEN_PAPER_FRONT_DOOR".equalsIgnoreCase(cmd)) {
                executeHardware("PLC", "PAPER_STORAGE_DOOR_OPEN", json);
                return null;
            } else if (!"INFORM_ENTITY_TO_THANKS".equalsIgnoreCase(cmd)) {
                return null;
            } else {
                executeHardware("PLC", "INFORM_ENTITY_TO_THANKS", json);
                return null;
            }
        }
    }

    public String[] getCmdSet() {
        return cmdSet;
    }
}
