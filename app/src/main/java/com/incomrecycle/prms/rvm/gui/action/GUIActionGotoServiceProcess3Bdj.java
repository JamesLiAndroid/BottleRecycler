package com.incomrecycle.prms.rvm.gui.action;

import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import java.util.HashMap;

public class GUIActionGotoServiceProcess3Bdj extends GUIAction {
    GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
    double recycleAmount = 0.0d;

    protected void doAction(Object[] paramObjs) {
        SysGlobal.execute(new Runnable() {
            private BaseActivity baseActivity;

            public Runnable setParam(BaseActivity baseActivity) {
                this.baseActivity = baseActivity;
                return this;
            }

            public void run() {
                HashMap hsmpStatus = null;
                try {
                    hsmpStatus = GUIActionGotoServiceProcess3Bdj.this.guiCommonService.execute("GUIBdjCommonService", "recycleEnd", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                HashMap hsmpGUIEvent = new HashMap();
                hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                hsmpGUIEvent.put("EVENT", "CMD");
                hsmpGUIEvent.put("CMD", "recycleEnd");
                hsmpGUIEvent.put("JSON", JSONUtils.toJSON(hsmpStatus));
                hsmpGUIEvent.put("BaseActivity", this.baseActivity);
                GUIGlobal.getEventMgr().addEvent(hsmpGUIEvent);
                this.baseActivity = null;
            }
        }.setParam((BaseActivity) paramObjs[0]));
    }
}
