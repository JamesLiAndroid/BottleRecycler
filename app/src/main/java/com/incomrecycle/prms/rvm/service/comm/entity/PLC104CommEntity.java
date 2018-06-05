package com.incomrecycle.prms.rvm.service.comm.entity;

import com.incomrecycle.common.SysConfig;

public class PLC104CommEntity extends PLC103CommEntity {
    public PLC104CommEntity() {
        this.hasLightCmd = true;
        this.isCloseDoorBeforeCheck = false;
        this.hasMetalLight = "TRUE".equalsIgnoreCase(SysConfig.get("HAS.METAL.LIGHT"));
    }
}
