package com.incomrecycle.prms.rvm.gui.action;

import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;

public class GUIActionStartMaintenance extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIStartDonationCommonService", "init", null);
        } catch (Exception e) {
        }
    }
}
