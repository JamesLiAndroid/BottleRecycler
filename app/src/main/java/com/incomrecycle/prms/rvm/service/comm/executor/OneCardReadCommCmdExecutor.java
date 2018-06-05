package com.incomrecycle.prms.rvm.service.comm.executor;

import com.incomrecycle.prms.rvm.service.comm.CheckHardware;
import com.incomrecycle.prms.rvm.service.comm.CommCmdExecutor;

public class OneCardReadCommCmdExecutor extends CheckHardware implements CommCmdExecutor {
    private static final String[] cmdSet = new String[]{"INIT_ONECARD", "READ_ONECARD", "TRANSACTION_ONECARD", "CONSUME_ONECARD", "QUERY_BALANCE", "WRITE_TRANS"};

    public String execute(String cmd, String json) throws Exception {
        if ("INIT_ONECARD".equalsIgnoreCase(cmd)) {
            return executeHardware("OneCardReader", "initOneCard", json);
        }
        if ("READ_ONECARD".equalsIgnoreCase(cmd)) {
            return executeHardware("OneCardReader", "readOneCardReader", json);
        }
        if ("TRANSACTION_ONECARD".equalsIgnoreCase(cmd)) {
            return executeHardware("OneCardReader", "transactionOneCard", json);
        }
        if ("CONSUME_ONECARD".equalsIgnoreCase(cmd)) {
            return executeHardware("OneCardReader", "consumeOneCardReader", json);
        }
        if ("QUERY_BALANCE".equalsIgnoreCase(cmd)) {
            return executeHardware("OneCardReader", "queryBalanceOneCard", json);
        }
        if ("WRITE_TRANS".equalsIgnoreCase(cmd)) {
            return executeHardware("OneCardReader", "writeTransInOneCard", json);
        }
        return null;
    }

    public String[] getCmdSet() {
        return cmdSet;
    }
}
