package com.incomrecycle.prms.rvm.service.commonservice;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.traffic.TrafficEntity;
import java.util.HashMap;

public class RVMTrafficTaskCommonService implements AppCommonService {
    private static final Logger logger = LoggerFactory.getLogger("RVMTrafficTaskCommonService");

    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        RVMStoreTrafficRecord(svcName, subSvnName, hsmpParam);
        return null;
    }

    public HashMap RVMStoreTrafficRecord(String svcName, String subSvnName, HashMap hsmpParam) {
        TrafficEntity.saveData();
        return null;
    }
}
