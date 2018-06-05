package com.incomrecycle.prms.rvm.gui.entity;

public class TextAdEntity {
    private String beginTime;
    private String end_Time;
    private String frontColor;
    private int sbarId;
    private String sbarTxt;

    public int getSbarId() {
        return this.sbarId;
    }

    public void setSbarId(int sbarId) {
        this.sbarId = sbarId;
    }

    public String getSbarTxt() {
        return this.sbarTxt;
    }

    public void setSbarTxt(String sbarTxt) {
        this.sbarTxt = sbarTxt;
    }

    public String getBeginTime() {
        return this.beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEnd_Time() {
        return this.end_Time;
    }

    public void setEnd_Time(String end_Time) {
        this.end_Time = end_Time;
    }

    public String getFrontColor() {
        return this.frontColor;
    }

    public void setFrontColor(String frontColor) {
        this.frontColor = frontColor;
    }

    public TextAdEntity(int sbarId, String subTxt, String beginTime, String end_Time, String frontColor) {
        this.sbarId = sbarId;
        this.sbarTxt = subTxt;
        this.beginTime = beginTime;
        this.end_Time = end_Time;
        this.frontColor = frontColor;
    }
}
