package com.incomrecycle.prms.rvm.service;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.prms.rvm.interfaces.CommonService;

public class MainCommonService implements CommonService {
    private static final Logger logger = LoggerFactory.getLogger("MAINSVC");

    public String execute(String svcName, String subSvnName, String json) throws Exception {
        boolean isLogEnable = CommonServiceMgr.getMgr().isLogEnable(svcName, subSvnName);
        if (isLogEnable) {
            logger.debug("svcName:" + svcName + ";subSvnName:" + subSvnName + ";\njson:" + json);
        }
        Class c = CommonServiceMgr.getMgr().getCommonService(svcName);
        if (c == null) {
            throw new Exception("Service " + svcName + " is not found!");
        }
        try {
            String result = JSONUtils.toJSON(((AppCommonService) c.newInstance()).execute(svcName, subSvnName, JSONUtils.toHashMap(json)));
            if (isLogEnable) {
                logger.debug("svcName:" + svcName + ";subSvnName:" + subSvnName + ";\njson:" + json + ";\nresult:" + result);
            }
            return result;
        } catch (Exception e) {
            logger.debug("svcName:" + svcName + ";subSvnName:" + subSvnName + ";\njson:" + json + ";", e);
            throw e;
        }
    }
}
