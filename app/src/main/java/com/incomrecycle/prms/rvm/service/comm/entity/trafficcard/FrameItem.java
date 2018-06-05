package com.incomrecycle.prms.rvm.service.comm.entity.trafficcard;

public class FrameItem {
    private int len;
    private String name;
    private int pos;
    private FrameItemType type;

    public String getName() {
        return this.name;
    }

    public int getLen() {
        return this.len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getPos() {
        return this.pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public FrameItemType getType() {
        return this.type;
    }

    public FrameItem(String name, FrameItemType type, int len) {
        this.name = name;
        this.type = type;
        this.len = len;
    }
}
