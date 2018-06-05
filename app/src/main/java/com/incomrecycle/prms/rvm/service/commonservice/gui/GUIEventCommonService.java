package com.incomrecycle.prms.rvm.service.commonservice.gui;

import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.util.HashMap;

public class GUIEventCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        Object event = ServiceGlobal.getGUIEventQueye().pop();
        if (event instanceof HashMap) {
            return (HashMap) event;
        }
        return JSONUtils.toHashMap((String) event);
    }
}
