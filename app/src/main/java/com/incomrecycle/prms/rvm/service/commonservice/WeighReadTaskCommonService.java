package com.incomrecycle.prms.rvm.service.commonservice;

import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import java.util.HashMap;

public class WeighReadTaskCommonService implements AppCommonService {
    private HashMap hsmpretPkg = null;
    private int i = 0;

    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String weighRet = CommService.getCommService().getCommEntity("Weigh").execute("read", null);
        if (weighRet == null) {
            weighRet = "0";
        }
        HashMap<String, String> hsmpEventParam = new HashMap();
        hsmpEventParam.put("type", "PAPER_WEIGH_REPORT");
        hsmpEventParam.put("data", weighRet);
        ServiceGlobal.getCommEventQueye().push(hsmpEventParam);
        return null;
    }
}
