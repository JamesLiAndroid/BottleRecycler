package com.incomrecycle.prms.rvm.gui.event;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import java.util.HashMap;

public class GUIEventThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger("GUIEvent");

    public void run() {
        while (true) {
            try {
                HashMap hsmpEvent = CommonServiceHelper.getGUICommonService().execute("GUIEventCommonService", null, null);
                logger.debug("\n" + JSONUtils.toJSON(hsmpEvent));
                GUIGlobal.getEventMgr().addEvent(hsmpEvent);
            } catch (Exception e) {
            }
        }
    }
}
