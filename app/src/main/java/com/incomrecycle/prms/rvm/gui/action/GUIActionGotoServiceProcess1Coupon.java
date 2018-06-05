package com.incomrecycle.prms.rvm.gui.action;

import android.content.Intent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.starput.SelectVoucherActivity;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class GUIActionGotoServiceProcess1Coupon extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        BaseActivity baseActivity = (BaseActivity) paramObjs[0];
        Intent intentSelectVoucher = new Intent(baseActivity, SelectVoucherActivity.class);
        intentSelectVoucher.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        baseActivity.startActivity(intentSelectVoucher);
    }
}
