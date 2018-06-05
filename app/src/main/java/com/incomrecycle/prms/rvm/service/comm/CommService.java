package com.incomrecycle.prms.rvm.service.comm;

import com.incomrecycle.common.SysConfig;

public class CommService {
    private static final CommService commService = new CommService();
    private AbstractCommService abstractCommService = null;

    private CommService() {
    }

    public static CommService getCommService() {
        return commService;
    }

    public CommEntity getCommEntity(String name) {
        AbstractCommService abstractCommService = getCommServiceInstance();
        if (abstractCommService == null) {
            return null;
        }
        return abstractCommService.getCommEntity(name);
    }

    private AbstractCommService getCommServiceInstance() {
        AbstractCommService abstractCommService;
        synchronized (this) {
            if (this.abstractCommService == null) {
                try {
                    this.abstractCommService = (AbstractCommService) Class.forName(SysConfig.get("CommService.class")).newInstance();
                    this.abstractCommService.init();
                } catch (Exception e) {
                    abstractCommService = null;
                }
            }
            abstractCommService = this.abstractCommService;
        }
        return abstractCommService;
    }

    public String execute(String cmd, String json) throws Exception {
        AbstractCommService abstractCommService = getCommServiceInstance();
        if (abstractCommService == null) {
            return null;
        }
        return abstractCommService.execute(cmd, json);
    }
}
