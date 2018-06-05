package com.incomrecycle.common.queue;

public interface IQueue {
    public static final long INFINITY = -1;

    int getSize();

    Object pop();

    Object pop(long j);

    void push(long j, Object obj);

    void push(Object obj);

    void reset();

    void terminate();

    Object tryPop();
}
