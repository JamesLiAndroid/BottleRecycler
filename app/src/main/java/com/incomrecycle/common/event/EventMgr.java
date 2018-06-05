package com.incomrecycle.common.event;

import java.util.ArrayList;
import java.util.List;

public class EventMgr {
    private List<EventListener> listEventListener = new ArrayList();

    public void add(EventListener eventListener) {
        synchronized (this) {
            this.listEventListener.add(eventListener);
        }
    }

    public void remove(EventListener eventListener) {
        synchronized (this) {
            for (int i = 0; i < this.listEventListener.size(); i++) {
                if (((EventListener) this.listEventListener.get(i)).equals(eventListener)) {
                    this.listEventListener.remove(i);
                    return;
                }
            }
        }
    }

    public void clear() {
        synchronized (this) {
            this.listEventListener.clear();
        }
    }

    public void addEvent(Object event) {
        synchronized (this) {
            for (int i = 0; i < this.listEventListener.size(); i++) {
                ((EventListener) this.listEventListener.get(i)).apply(event);
            }
        }
    }
}
