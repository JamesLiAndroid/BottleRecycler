package com.incomrecycle.prms.rvm.gui.action;

import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;

public class GUIActionSysAction extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        BaseActivity baseActivity = (BaseActivity) paramObjs[0];
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", (String) paramObjs[1], null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
