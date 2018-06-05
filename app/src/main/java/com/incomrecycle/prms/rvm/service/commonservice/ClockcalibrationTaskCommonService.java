package com.incomrecycle.prms.rvm.service.commonservice;

import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import java.util.HashMap;

public class ClockcalibrationTaskCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if (System.currentTimeMillis() >= DateUtils.parseDate("2014-01-01 00:00:00").getTime()) {
            CommService.getCommService().execute("RVM_CLOCK_CALIBRATION", null);
        }
        return null;
    }
}
