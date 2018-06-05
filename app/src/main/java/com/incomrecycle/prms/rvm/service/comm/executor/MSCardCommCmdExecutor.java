package com.incomrecycle.prms.rvm.service.comm.executor;

import com.incomrecycle.prms.rvm.service.comm.CheckHardware;
import com.incomrecycle.prms.rvm.service.comm.CommCmdExecutor;

public class MSCardCommCmdExecutor extends CheckHardware implements CommCmdExecutor {
    private static final String[] cmdSet = new String[]{"MSCARD_RESET", "MSCARD_READ"};

    public String execute(String cmd, String json) throws Exception {
        if ("MSCARD_RESET".equalsIgnoreCase(cmd)) {
            return executeHardware("MagneticCard", "RESET", json);
        }
        if ("MSCARD_READ".equalsIgnoreCase(cmd)) {
            return executeHardware("MagneticCard", "READ", json);
        }
        return null;
    }

    public String[] getCmdSet() {
        return cmdSet;
    }
}
