package com.incomrecycle.common.task;

import com.incomrecycle.common.SysGlobal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DelayTask extends Thread {
    private static final DelayTask delayTask = new DelayTask();
    private HashMap<TaskAction, Long> hsmpTaskAction = new HashMap();

    private DelayTask() {
    }

    public static DelayTask getDelayTask() {
        return delayTask;
    }

    public void run() {
        while (true) {
            List<TaskAction> listTaskAction = new ArrayList();
            synchronized (this.hsmpTaskAction) {
                try {
                    long lTime = System.currentTimeMillis();
                    long lNextTime = 0;
                    for (TaskAction taskAction : this.hsmpTaskAction.keySet()) {
                        Long atTime = (Long) this.hsmpTaskAction.get(taskAction);
                        if (atTime.longValue() <= lTime) {
                            listTaskAction.add(taskAction);
                        } else if (lNextTime == 0) {
                            lNextTime = atTime.longValue();
                        } else if (lNextTime > atTime.longValue()) {
                            lNextTime = atTime.longValue();
                        }
                    }
                    if (listTaskAction.size() == 0) {
                        if (lNextTime == 0) {
                            this.hsmpTaskAction.wait();
                        } else {
                            this.hsmpTaskAction.wait(lNextTime - lTime);
                        }
                    } else {
                        int i;
                        for (i = 0; i < listTaskAction.size(); i++) {
                            this.hsmpTaskAction.remove((TaskAction) listTaskAction.get(i));
                        }
                        for (i = 0; i < listTaskAction.size(); i++) {
                            SysGlobal.execute(new Runnable() {
                                TaskAction taskAction;

                                public Runnable setTaskAction(TaskAction taskAction) {
                                    this.taskAction = taskAction;
                                    return this;
                                }

                                public void run() {
                                    this.taskAction.execute();
                                }
                            }.setTaskAction((TaskAction) listTaskAction.get(i)));
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addDelayTask(TaskAction taskAction, long milliSeconds) {
        if (milliSeconds <= 0) {
            taskAction.execute();
            return;
        }
        synchronized (this.hsmpTaskAction) {
            this.hsmpTaskAction.put(taskAction, Long.valueOf(System.currentTimeMillis() + milliSeconds));
            this.hsmpTaskAction.notify();
        }
    }

    public void removeDelayTask(TaskAction taskAction) {
        synchronized (this.hsmpTaskAction) {
            if (this.hsmpTaskAction.get(taskAction) != null) {
                this.hsmpTaskAction.remove(taskAction);
                this.hsmpTaskAction.notify();
            }
        }
    }
}
