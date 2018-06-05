package com.incomrecycle.prms.rvm.gui.event;

import android.app.Application;
import com.incomrecycle.common.event.EventListener;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import java.util.HashMap;

public class GUIApplicationEventListener implements EventListener {
    private Application application;

    public GUIApplicationEventListener(Application application) {
        this.application = application;
    }

    public void apply(Object event) {
        BaseActivity baseActivity = GUIGlobal.getBaseActivity("RVMMainActivity");
        if (baseActivity != null) {
            HashMap hsmpEvent = (HashMap) event;
            String TYPE = (String) hsmpEvent.get(AllAdvertisement.MEDIA_TYPE);
            String EVENT = (String) hsmpEvent.get("EVENT");
            if (EVENT != null && "Application".equalsIgnoreCase(TYPE)) {
                if (EVENT.equalsIgnoreCase("UPDATE")) {
                    baseActivity.postEvent(hsmpEvent);
                }
                if (EVENT.equalsIgnoreCase("CMD")) {
                    if ("REQUEST_RECYCLE".equalsIgnoreCase((String) hsmpEvent.get("CMD"))) {
                        if (GUIGlobal.getCurrentBaseActivity() == baseActivity) {
                            baseActivity.postEvent(hsmpEvent);
                        } else {
                            return;
                        }
                    }
                    if ("PLC_COMM_ERROR".equalsIgnoreCase((String) hsmpEvent.get("CMD"))) {
                        if (GUIGlobal.getCurrentBaseActivity() == baseActivity) {
                            baseActivity.postEvent(hsmpEvent);
                        } else {
                            return;
                        }
                    }
                    if ("PLC_COMM_ERROR_RECOVERY".equalsIgnoreCase((String) hsmpEvent.get("CMD")) && GUIGlobal.getCurrentBaseActivity() == baseActivity) {
                        baseActivity.postEvent(hsmpEvent);
                    }
                }
            }
        }
    }
}
