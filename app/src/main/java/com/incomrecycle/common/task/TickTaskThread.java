package com.incomrecycle.common.task;

import com.incomrecycle.common.SysGlobal;
import java.util.ArrayList;
import java.util.List;

public class TickTaskThread extends Thread {
    private static final TickTaskThread tickTaskThread = new TickTaskThread();
    private List<TickTask> listTickTask = new ArrayList();

    private static class TickTask {
        boolean isBlocking;
        boolean isExecuting = false;
        long lastTime = 0;
        double seconds;
        TaskAction taskAction;

        TickTask(TaskAction taskAction, double seconds, boolean isBlocking) {
            this.taskAction = taskAction;
            this.seconds = seconds;
            this.isBlocking = isBlocking;
        }

        void run() {
            this.isExecuting = true;
            this.lastTime = System.currentTimeMillis();
            this.taskAction.execute();
            this.lastTime = System.currentTimeMillis();
            this.isExecuting = false;
        }
    }

    private static class TickTaskExecuteTask implements Runnable {
        TickTask tickTask;

        TickTaskExecuteTask(TickTask tickTask) {
            this.tickTask = tickTask;
        }

        public void run() {
            this.tickTask.run();
        }
    }

    private TickTaskThread() {
    }

    public static TickTaskThread getTickTaskThread() {
        return tickTaskThread;
    }

    public void register(TaskAction taskAction, double seconds, boolean isBlocking) {
        synchronized (this) {
            int i = 0;
            while (i < this.listTickTask.size()) {
                try {
                    if (((TickTask) this.listTickTask.get(i)).taskAction.equals(taskAction)) {
                        return;
                    }
                    i++;
                } catch (Exception e) {
                }
            }
            this.listTickTask.add(new TickTask(taskAction, seconds, isBlocking));
        }
    }

    public void unregister(TaskAction taskAction) {
        synchronized (this) {
            int i = 0;
            while (i < this.listTickTask.size()) {
                try {
                    if (((TickTask) this.listTickTask.get(i)).taskAction.equals(taskAction)) {
                        this.listTickTask.remove(i);
                        return;
                    }
                    i++;
                } catch (Exception e) {
                }
            }
        }
    }

    public void clearRegister() {
        synchronized (this) {
            this.listTickTask.clear();
        }
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
                synchronized (this) {
                    long lCurrentTime = System.currentTimeMillis();
                    for (int i = 0; i < this.listTickTask.size(); i++) {
                        TickTask tickTask = (TickTask) this.listTickTask.get(i);
                        if (!tickTask.isExecuting && (tickTask.lastTime == 0 || tickTask.lastTime > lCurrentTime || ((double) tickTask.lastTime) + (tickTask.seconds * 1000.0d) < ((double) lCurrentTime))) {
                            SysGlobal.execute(new TickTaskExecuteTask(tickTask));
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}
