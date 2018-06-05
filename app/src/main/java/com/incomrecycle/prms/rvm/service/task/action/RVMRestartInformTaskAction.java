package com.incomrecycle.prms.rvm.service.task.action;

import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.common.task.TickTaskThread;
import com.incomrecycle.prms.rvm.service.commonservice.RVMRestartInformCommonService;
import java.util.HashMap;

public class RVMRestartInformTaskAction implements TaskAction {
    public void execute() {
        try {
            HashMap hampResult = new RVMRestartInformCommonService().execute("RVMRestartInformCommonService", "initRestart", null);
            if (hampResult != null && "success".equalsIgnoreCase((String) hampResult.get("RESULT"))) {
                TickTaskThread.getTickTaskThread().unregister(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
