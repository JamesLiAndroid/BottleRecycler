package com.incomrecycle.common.task;

import com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameDataFormat.ServerFrameType;

public class GuiANRWatchDog {
    private static final GuiANRWatchDog guiANRWatchDog = new GuiANRWatchDog();
    private int interval = 0;
    private TaskAction intervalTaskAction = null;
    private boolean isStart = false;
    private long lAliveTime = 0;
    private int timeout = 0;
    private int timeoutOutOfRange = 0;
    private TaskAction timeoutTaskAction = null;
    private WatchDogThread watchDogThread = null;

    private static class WatchDogThread extends Thread {
        private WatchDogThread() {
        }

        public void run() {
            while (true) {
                synchronized (GuiANRWatchDog.guiANRWatchDog) {
                    if (GuiANRWatchDog.guiANRWatchDog.isStart) {
                        long timeout = (long) (GuiANRWatchDog.guiANRWatchDog.timeout * ServerFrameType.DATA_TRANSFER_REQ);
                        long interval = (long) (GuiANRWatchDog.guiANRWatchDog.interval * ServerFrameType.DATA_TRANSFER_REQ);
                        long timeoutOutOfRange = (long) (GuiANRWatchDog.guiANRWatchDog.timeoutOutOfRange * ServerFrameType.DATA_TRANSFER_REQ);
                        long lAliveTime = GuiANRWatchDog.guiANRWatchDog.lAliveTime;
                        TaskAction intervalTaskAction = GuiANRWatchDog.guiANRWatchDog.intervalTaskAction;
                        TaskAction timeoutTaskAction = GuiANRWatchDog.guiANRWatchDog.timeoutTaskAction;
                        if (lAliveTime > 0) {
                            long lTime = System.currentTimeMillis();
                            long lDeltaTime = lTime - lAliveTime;
                            if ((timeoutOutOfRange <= timeout || lDeltaTime < timeoutOutOfRange) && timeoutTaskAction != null && lAliveTime + timeout < lTime) {
                                timeoutTaskAction.execute();
                            }
                        }
                        if (intervalTaskAction != null) {
                            intervalTaskAction.execute();
                        }
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            GuiANRWatchDog.guiANRWatchDog.wait();
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static GuiANRWatchDog getInstance() {
        return guiANRWatchDog;
    }

    private GuiANRWatchDog() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void start(int r5, int r6, com.incomrecycle.common.task.TaskAction r7, int r8, com.incomrecycle.common.task.TaskAction r9) {
        /*
        r4 = this;
        r1 = guiANRWatchDog;
        monitor-enter(r1);
        r0 = r4.isStart;	 Catch:{ all -> 0x0013 }
        if (r0 == 0) goto L_0x0009;
    L_0x0007:
        monitor-exit(r1);	 Catch:{ all -> 0x0013 }
    L_0x0008:
        return;
    L_0x0009:
        if (r6 <= 0) goto L_0x0011;
    L_0x000b:
        if (r8 <= 0) goto L_0x0011;
    L_0x000d:
        if (r9 == 0) goto L_0x0011;
    L_0x000f:
        if (r7 != 0) goto L_0x0016;
    L_0x0011:
        monitor-exit(r1);	 Catch:{ all -> 0x0013 }
        goto L_0x0008;
    L_0x0013:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0013 }
        throw r0;
    L_0x0016:
        r0 = r8 + r8;
        if (r6 > r0) goto L_0x001c;
    L_0x001a:
        monitor-exit(r1);	 Catch:{ all -> 0x0013 }
        goto L_0x0008;
    L_0x001c:
        r2 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0013 }
        r4.lAliveTime = r2;	 Catch:{ all -> 0x0013 }
        r4.timeout = r6;	 Catch:{ all -> 0x0013 }
        r4.interval = r8;	 Catch:{ all -> 0x0013 }
        r4.timeoutOutOfRange = r5;	 Catch:{ all -> 0x0013 }
        r4.intervalTaskAction = r9;	 Catch:{ all -> 0x0013 }
        r4.timeoutTaskAction = r7;	 Catch:{ all -> 0x0013 }
        r0 = 1;
        r4.isStart = r0;	 Catch:{ all -> 0x0013 }
        r0 = r4.watchDogThread;	 Catch:{ all -> 0x0013 }
        if (r0 != 0) goto L_0x0040;
    L_0x0033:
        r0 = new com.incomrecycle.common.task.GuiANRWatchDog$WatchDogThread;	 Catch:{ all -> 0x0013 }
        r2 = 0;
        r0.<init>();	 Catch:{ all -> 0x0013 }
        r4.watchDogThread = r0;	 Catch:{ all -> 0x0013 }
        r0 = r4.watchDogThread;	 Catch:{ all -> 0x0013 }
        r0.start();	 Catch:{ all -> 0x0013 }
    L_0x0040:
        r0 = guiANRWatchDog;	 Catch:{ all -> 0x0013 }
        r0.notify();	 Catch:{ all -> 0x0013 }
        monitor-exit(r1);	 Catch:{ all -> 0x0013 }
        goto L_0x0008;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.incomrecycle.common.task.GuiANRWatchDog.start(int, int, com.incomrecycle.common.task.TaskAction, int, com.incomrecycle.common.task.TaskAction):void");
    }

    public void setAlive() {
        this.lAliveTime = System.currentTimeMillis();
    }

    public void stop() {
        synchronized (guiANRWatchDog) {
            this.isStart = false;
            guiANRWatchDog.notify();
        }
    }
}
