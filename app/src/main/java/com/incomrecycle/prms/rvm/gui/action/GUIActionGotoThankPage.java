package com.incomrecycle.prms.rvm.gui.action;

import android.content.Intent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.starput.ThankBottlePageActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.ThankPaperPageActivity;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class GUIActionGotoThankPage extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        BaseActivity baseActivity = (BaseActivity) paramObjs[0];
        String PRODUCT_TYPE = (String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE");
        Intent intent;
        if ("BOTTLE".equalsIgnoreCase(PRODUCT_TYPE)) {
            intent = new Intent(baseActivity, ThankBottlePageActivity.class);
            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            baseActivity.startActivity(intent);
        } else if ("PAPER".equalsIgnoreCase(PRODUCT_TYPE)) {
            intent = new Intent(baseActivity, ThankPaperPageActivity.class);
            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            baseActivity.startActivity(intent);
        }
    }
}
