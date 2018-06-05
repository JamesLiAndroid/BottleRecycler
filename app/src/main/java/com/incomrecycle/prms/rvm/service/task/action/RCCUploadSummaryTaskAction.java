package com.incomrecycle.prms.rvm.service.task.action;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.prms.rvm.service.MainCommonService;
import java.util.Date;

public class RCCUploadSummaryTaskAction implements TaskAction {
    private long lLastTime = 0;

    public void execute() {
        try {
            long lTime = System.currentTimeMillis();
            long lShallExecuteTime = DateUtils.parseDatetime(DateUtils.formatDatetime(new Date(), "yyyyMMdd") + SysConfig.get("SUMMARY.UPLOAD.TIMEOFDAY")).getTime();
            if (this.lLastTime <= lShallExecuteTime && lTime > lShallExecuteTime) {
                new MainCommonService().execute("RCCUploadSummaryCommonService", null, null);
                this.lLastTime = lTime;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
