package com.incomrecycle.prms.rvm.service.task.action;

import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.prms.rvm.service.MainCommonService;

public class TCPAlarmCheckAction implements TaskAction {
    public void execute() {
        try {
            new MainCommonService().execute("GUIMaintenanceCommonService", "checkTcpAlarm", null);
        } catch (Exception e) {
        }
    }
}
