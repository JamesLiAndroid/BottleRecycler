package com.incomrecycle.prms.rvm.service.commonservice.gui;

import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import java.util.HashMap;

public class GUIMaintenanceCheckCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if ("commCmd".equalsIgnoreCase(subSvnName)) {
            return commCmd(svcName, subSvnName, hsmpParam);
        }
        return null;
    }

    public HashMap commCmd(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String result = CommService.getCommService().execute((String) hsmpParam.get("CMD"), (String) hsmpParam.get("JSON"));
        if (StringUtils.isBlank(result)) {
            return null;
        }
        HashMap hsmpResult = new HashMap();
        hsmpResult.put("RESULT", result);
        return hsmpResult;
    }
}
