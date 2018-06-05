package com.incomrecycle.prms.rvm.gui.event;

import com.incomrecycle.common.event.EventListener;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import java.util.HashMap;

public class GUIActivityEventListener implements EventListener {
    public void apply(Object event) {
        BaseActivity baseActivity = GUIGlobal.getCurrentBaseActivity();
        if (baseActivity != null) {
            HashMap hsmpEvent = (HashMap) event;
            if (!"Application".equalsIgnoreCase((String) hsmpEvent.get(AllAdvertisement.MEDIA_TYPE))) {
                baseActivity.postEvent(hsmpEvent);
            }
        }
    }
}
