package com.incomrecycle.prms.rvm.service.task.action;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.task.InstanceTask;
import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.prms.rvm.service.commonservice.RCCInstanceTaskCommonService;
import java.util.HashMap;

public class RCCInstanceTask {
    private static final Logger logger = LoggerFactory.getLogger("RCCInstanceTask");

    private static class RCCTaskAction implements TaskAction {
        private HashMap<String, String> hsmpEvent;

        private RCCTaskAction(HashMap<String, String> hsmpEvent) {
            this.hsmpEvent = hsmpEvent;
        }

        public void execute() {
            RCCInstanceTaskCommonService rccInstanceTaskCommonService = new RCCInstanceTaskCommonService();
            try {
                RCCInstanceTask.logger.debug(JSONUtils.toJSON(this.hsmpEvent));
                rccInstanceTaskCommonService.execute("RCCInstanceTaskCommonService", null, this.hsmpEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addTask(HashMap<String, String> hsmpEvent) {
        InstanceTask.getInstanceTask().addTask(new RCCTaskAction(hsmpEvent));
    }
}
