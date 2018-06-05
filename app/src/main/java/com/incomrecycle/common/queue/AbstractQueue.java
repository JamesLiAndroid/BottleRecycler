package com.incomrecycle.common.queue;

public abstract class AbstractQueue implements IQueue {
    public void terminate() {
        push(null);
    }

    public Object pop() {
        return pop(-1);
    }

    public Object tryPop() {
        return pop(0);
    }
}
