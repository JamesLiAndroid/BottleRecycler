package com.incomrecycle.prms.rvm.service.comm.executor;

import com.incomrecycle.prms.rvm.common.SysDef.TrafficType;
import com.incomrecycle.prms.rvm.service.comm.CheckHardware;
import com.incomrecycle.prms.rvm.service.comm.CommCmdExecutor;

public class RCCSendCommCmdExecutor extends CheckHardware implements CommCmdExecutor {
    private static final String[] cmdSet = new String[]{"RCC_SEND"};

    public String execute(String cmd, String json) throws Exception {
        if ("RCC_SEND".equalsIgnoreCase(cmd)) {
            return executeHardware(TrafficType.RCC, "SEND", json);
        }
        return null;
    }

    public String[] getCmdSet() {
        return cmdSet;
    }
}
