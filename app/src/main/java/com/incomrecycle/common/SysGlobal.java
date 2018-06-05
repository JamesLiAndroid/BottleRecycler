package com.incomrecycle.common;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class SysGlobal {
    private static final HashMap<Integer, ExecutorService> hsmpExecutorService = new HashMap();

    public static void execute(Runnable runnable, int priority) {
        synchronized (hsmpExecutorService) {
            ExecutorService executorService = (ExecutorService) hsmpExecutorService.get(Integer.valueOf(priority));
            if (executorService == null) {
                executorService = Executors.newCachedThreadPool(new ThreadFactory() {
                    private int priority;

                    public ThreadFactory setPriority(int priority) {
                        this.priority = priority;
                        return this;
                    }

                    public Thread newThread(Runnable runnable) {
                        Thread thread = new Thread(runnable);
                        thread.setPriority(this.priority);
                        return thread;
                    }
                }.setPriority(priority));
                hsmpExecutorService.put(Integer.valueOf(priority), executorService);
            }
            executorService.execute(runnable);
        }
    }

    public static void execute(Runnable runnable) {
        execute(runnable, 5);
    }
}
