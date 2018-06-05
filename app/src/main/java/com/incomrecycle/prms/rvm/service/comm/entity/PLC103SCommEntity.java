package com.incomrecycle.prms.rvm.service.comm.entity;

public class PLC103SCommEntity extends PLC103CommEntity {
    public PLC103SCommEntity() {
        this.hasLightCmd = true;
        this.isCloseDoorBeforeCheck = true;
    }
}
