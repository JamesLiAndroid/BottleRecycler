package com.incomrecycle.prms.rvm.service.traffic;

public class TrafficInfo {
    private String appname;
    private String packname;
    private long rx;
    private long tx;
    private int uid;

    public String getPackname() {
        return this.packname;
    }

    public void setPackname(String packname) {
        this.packname = packname;
    }

    public String getAppname() {
        return this.appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public long getTx() {
        return this.tx;
    }

    public void setTx(long tx) {
        this.tx = tx;
    }

    public long getRx() {
        return this.rx;
    }

    public void setRx(long rx) {
        this.rx = rx;
    }

    public int getUID() {
        return this.uid;
    }

    public void setUID(int uid) {
        this.uid = uid;
    }
}
