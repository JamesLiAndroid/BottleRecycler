package com.incomrecycle.prms.rvm.service.task.action;

import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.prms.rvm.service.MainCommonService;

public class RVMTrafficStoreTaskAction implements TaskAction {
    public void execute() {
        try {
            new MainCommonService().execute("RVMTrafficTaskCommonService", null, null);
        } catch (Exception e) {
        }
    }
}
