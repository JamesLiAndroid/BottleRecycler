package com.incomrecycle.prms.rvm.service.event;

import com.incomrecycle.common.event.EventListener;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;

public class ServiceEventListener implements EventListener {
    public void apply(Object event) {
        ServiceGlobal.getGUIEventQueye().push(event);
    }
}
