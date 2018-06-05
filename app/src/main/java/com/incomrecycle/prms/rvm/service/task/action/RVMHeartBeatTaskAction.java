package com.incomrecycle.prms.rvm.service.task.action;

import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.prms.rvm.service.MainCommonService;

public class RVMHeartBeatTaskAction implements TaskAction {
    private boolean hasInit = false;

    public void execute() {
        MainCommonService commonService = new MainCommonService();
        try {
            if (this.hasInit) {
                commonService.execute("GUIMaintenanceCommonService", "sendHeartBeat", null);
                return;
            }
            commonService.execute("GUIMaintenanceCommonService", "setRvmAliveTime", null);
            this.hasInit = true;
        } catch (Exception e) {
        }
    }
}
