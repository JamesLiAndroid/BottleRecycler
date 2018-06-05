package com.incomrecycle.prms.rvm.service.commonservice.gui;

import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import java.util.HashMap;

public class GUICameraCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        if ("cameraStart".equalsIgnoreCase(subSvcName)) {
            return cameraStart(svcName, subSvcName, hsmpParam);
        }
        if ("cameraStop".equalsIgnoreCase(subSvcName)) {
            return cameraStop(svcName, subSvcName, hsmpParam);
        }
        return null;
    }

    private HashMap cameraStart(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        CommService.getCommService().execute("QRCODE_LIGHT_ON", null);
        return null;
    }

    private HashMap cameraStop(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        CommService.getCommService().execute("QRCODE_LIGHT_OFF", null);
        return null;
    }
}
