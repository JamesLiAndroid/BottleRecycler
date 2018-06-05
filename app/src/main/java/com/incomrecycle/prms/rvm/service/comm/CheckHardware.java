package com.incomrecycle.prms.rvm.service.comm;

public abstract class CheckHardware {
    public String executeHardware(String hardwareName, String cmd, String json) throws Exception {
        if (CommService.getCommService().getCommEntity(hardwareName) == null) {
            return null;
        }
        return CommService.getCommService().getCommEntity(hardwareName).execute(cmd, json);
    }
}
