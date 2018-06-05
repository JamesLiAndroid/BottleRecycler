package com.incomrecycle.prms.rvm.gui.action;

import android.content.Intent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.activity.starput.PrintingVoucherActivity;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class GUIActionGotoServiceProcess3Coupon extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        BaseActivity baseActivity = (BaseActivity) paramObjs[0];
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        try {
            guiCommonService.execute("GUIVoucherCommonService", "recycleEnd", null);
            GUIGlobal.setCurrentSession("RECYCLED_AMOUNT", guiCommonService.execute("GUIQueryCommonService", "queryRecycledAmount", null).get("RECYCLED_AMOUNT"));
            Intent intent = new Intent(baseActivity, PrintingVoucherActivity.class);
            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            baseActivity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
