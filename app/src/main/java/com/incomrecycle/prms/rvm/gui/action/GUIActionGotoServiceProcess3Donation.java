package com.incomrecycle.prms.rvm.gui.action;

import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;

public class GUIActionGotoServiceProcess3Donation extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        BaseActivity baseActivity = (BaseActivity) paramObjs[0];
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIDonationCommonService", "recycleEnd", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        baseActivity.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{baseActivity, Integer.valueOf(4), "DONATION"});
    }
}
