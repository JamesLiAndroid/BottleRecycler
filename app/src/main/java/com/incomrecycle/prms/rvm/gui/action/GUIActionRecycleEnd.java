package com.incomrecycle.prms.rvm.gui.action;

import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;

public class GUIActionRecycleEnd extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "recycleEnd", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
