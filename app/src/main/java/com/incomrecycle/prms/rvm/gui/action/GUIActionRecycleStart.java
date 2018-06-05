package com.incomrecycle.prms.rvm.gui.action;

import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;

public class GUIActionRecycleStart extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "recycleStart", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
