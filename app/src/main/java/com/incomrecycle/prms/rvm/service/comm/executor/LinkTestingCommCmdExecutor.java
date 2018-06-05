package com.incomrecycle.prms.rvm.service.comm.executor;

import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.TrafficType;
import com.incomrecycle.prms.rvm.service.comm.CheckHardware;
import com.incomrecycle.prms.rvm.service.comm.CommCmdExecutor;
import java.util.HashMap;

public class LinkTestingCommCmdExecutor extends CheckHardware implements CommCmdExecutor {
    private static final String[] cmdSet = new String[]{"LINK_TESTING"};

    public String execute(String cmd, String json) throws Exception {
        HashMap hsmpResult = new HashMap();
        if ("LINK_TESTING".equalsIgnoreCase(cmd)) {
            String retInfo = StringUtils.trimToNull(executeHardware(TrafficType.RCC, "TESTING", json));
            if (retInfo != null) {
                HashMap hsmp = JSONUtils.toHashMap(retInfo);
                hsmp.put(AllAdvertisement.MEDIA_TYPE, TrafficType.RCC);
                hsmpResult.put(TrafficType.RCC, hsmp);
            }
        }
        return JSONUtils.toJSON(hsmpResult);
    }

    public String[] getCmdSet() {
        return cmdSet;
    }
}
