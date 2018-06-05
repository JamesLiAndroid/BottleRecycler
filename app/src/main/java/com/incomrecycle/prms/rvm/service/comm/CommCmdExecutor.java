package com.incomrecycle.prms.rvm.service.comm;

public interface CommCmdExecutor {
    String execute(String str, String str2) throws Exception;

    String[] getCmdSet();
}
