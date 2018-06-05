package com.incomrecycle.common.sync;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GlobalLock {
    private static final HashMap<String, Lock> hsmpLock = new HashMap();

    public static void lock(String name) {
        synchronized (hsmpLock) {
            Lock lock = (Lock) hsmpLock.get(name);
            if (lock == null) {
                Lock lock2 = new ReentrantLock();
                try {
                    hsmpLock.put(name, lock2);
                    lock = lock2;
                } catch (Throwable th) {
                    lock = lock2;
                    throw th;
                }
            }
            try {
                lock.lock();
            } catch (Throwable th3) {
                throw th3;
            }
        }
    }

    public static void unlock(String name) {
        synchronized (hsmpLock) {
            Lock lock = (Lock) hsmpLock.get(name);
            if (lock == null) {
                return;
            }
            lock.unlock();
        }
    }
}
