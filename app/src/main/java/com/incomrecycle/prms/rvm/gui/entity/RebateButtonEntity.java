package com.incomrecycle.prms.rvm.gui.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class RebateButtonEntity implements Parcelable {
    public static final Creator<RebateButtonEntity> CREATOR = new Creator<RebateButtonEntity>() {
        public RebateButtonEntity createFromParcel(Parcel source) {
            RebateButtonEntity mRebateButton = new RebateButtonEntity();
            mRebateButton.id = source.readInt();
            mRebateButton.backgroundFromResource = source.readInt();
            mRebateButton.backgroundFromSDCard = source.readString();
            mRebateButton.text = source.readString();
            mRebateButton.Visibility = source.readInt();
            mRebateButton.vendingWay = source.readString();
            mRebateButton.enabled = source.readByte() != (byte) 0;
            mRebateButton.selectFlag = source.readString();
            mRebateButton.buttonType = source.readString();
            return mRebateButton;
        }

        public RebateButtonEntity[] newArray(int size) {
            return new RebateButtonEntity[size];
        }
    };
    private int Visibility;
    private int backgroundFromResource;
    private String backgroundFromSDCard;
    private String buttonType;
    private boolean enabled;
    private int id;
    private String selectFlag;
    private String text;
    private String vendingWay;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBackgroundFromResource() {
        return this.backgroundFromResource;
    }

    public void setBackgroundFromResource(int backgroundFromResource) {
        this.backgroundFromResource = backgroundFromResource;
    }

    public String getBackgroundFromSDCard() {
        return this.backgroundFromSDCard;
    }

    public void setBackgroundFromSDCard(String backgroundFromSDCard) {
        this.backgroundFromSDCard = backgroundFromSDCard;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getVisibility() {
        return this.Visibility;
    }

    public void setVisibility(int visibility) {
        this.Visibility = visibility;
    }

    public String getVendingWay() {
        return this.vendingWay;
    }

    public void setVendingWay(String vendingWay) {
        this.vendingWay = vendingWay;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSelectFlag() {
        return this.selectFlag;
    }

    public void setSelectFlag(String selectFlag) {
        this.selectFlag = selectFlag;
    }

    public String getButtonType() {
        return this.buttonType;
    }

    public void setButtonType(String buttonType) {
        this.buttonType = buttonType;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.backgroundFromResource);
        dest.writeString(this.backgroundFromSDCard);
        dest.writeString(this.text);
        dest.writeInt(this.Visibility);
        dest.writeString(this.vendingWay);
        dest.writeByte((byte) (this.enabled ? 1 : 0));
        dest.writeString(this.selectFlag);
        dest.writeString(this.buttonType);
    }
}
