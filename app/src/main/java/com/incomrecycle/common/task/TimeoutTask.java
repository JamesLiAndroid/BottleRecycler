package com.incomrecycle.common.task;

import com.incomrecycle.common.SysGlobal;
import java.util.ArrayList;
import java.util.List;

public class TimeoutTask implements TaskAction {
    private static final TimeoutTask timeoutTask = new TimeoutTask();
    private List<TimeoutTaskItem> listTimeoutTaskItem = new ArrayList();

    private static class TimeoutTaskExecuteThread implements Runnable {
        TimeoutTaskItem timeoutTaskItem;

        TimeoutTaskExecuteThread(TimeoutTaskItem timeoutTaskItem) {
            this.timeoutTaskItem = timeoutTaskItem;
        }

        public void run() {
            this.timeoutTaskItem.run();
        }
    }

    private static class TimeoutTaskItem {
        int forwardSeconds = -1;
        boolean isEnabled;
        boolean isRunning;
        TimeoutAction timeoutAction;
        int timeoutSeconds;

        TimeoutTaskItem(TimeoutAction timeoutAction, int timeoutSeconds, boolean isEnable) {
            this.timeoutAction = timeoutAction;
            this.timeoutSeconds = timeoutSeconds;
            this.isEnabled = isEnable;
            this.isRunning = false;
        }

        void run() {
            int remainedSeconds = this.timeoutSeconds - this.forwardSeconds;
            if (!this.isRunning || remainedSeconds <= 0) {
                this.isRunning = true;
                this.timeoutAction.apply(this.forwardSeconds, remainedSeconds);
                this.isRunning = false;
            }
        }
    }

    public static TimeoutTask getTimeoutTask() {
        return timeoutTask;
    }

    public void reset(TimeoutAction timeoutAction) {
        synchronized (this) {
            int idx = getIndex(timeoutAction);
            if (idx != -1) {
                ((TimeoutTaskItem) this.listTimeoutTaskItem.get(idx)).isEnabled = true;
                ((TimeoutTaskItem) this.listTimeoutTaskItem.get(idx)).forwardSeconds = -1;
            }
        }
    }

    public void reset(TimeoutAction timeoutAction, int timeoutSeconds) {
        synchronized (this) {
            int idx = getIndex(timeoutAction);
            if (idx != -1) {
                if (timeoutSeconds != -1) {
                    ((TimeoutTaskItem) this.listTimeoutTaskItem.get(idx)).timeoutSeconds = timeoutSeconds;
                }
                ((TimeoutTaskItem) this.listTimeoutTaskItem.get(idx)).isEnabled = true;
                ((TimeoutTaskItem) this.listTimeoutTaskItem.get(idx)).forwardSeconds = -1;
            }
        }
    }

    public void setEnabled(TimeoutAction timeoutAction, boolean isEnabled) {
        synchronized (this) {
            int idx = getIndex(timeoutAction);
            if (idx != -1) {
                ((TimeoutTaskItem) this.listTimeoutTaskItem.get(idx)).isEnabled = isEnabled;
            }
        }
    }

    public boolean isEnabled(TimeoutAction timeoutAction) {
        boolean z;
        synchronized (this) {
            int idx = getIndex(timeoutAction);
            if (idx != -1) {
                z = ((TimeoutTaskItem) this.listTimeoutTaskItem.get(idx)).isEnabled;
            } else {
                z = false;
            }
        }
        return z;
    }

    public void addTimeoutAction(TimeoutAction timeoutAction, int timeoutSeconds) {
        addTimeoutAction(timeoutAction, timeoutSeconds, true);
    }

    public void addTimeoutAction(TimeoutAction timeoutAction, int timeoutSeconds, boolean isEnable) {
        if (timeoutAction != null) {
            synchronized (this) {
                if (getIndex(timeoutAction) == -1) {
                    this.listTimeoutTaskItem.add(new TimeoutTaskItem(timeoutAction, timeoutSeconds, isEnable));
                }
            }
        }
    }

    public void removeTimeoutAction(TimeoutAction timeoutAction) {
        if (timeoutAction != null) {
            synchronized (this) {
                int idx = getIndex(timeoutAction);
                if (idx != -1) {
                    this.listTimeoutTaskItem.remove(idx);
                }
            }
        }
    }

    private int getIndex(TimeoutAction timeoutAction) {
        if (timeoutAction == null) {
            return -1;
        }
        for (int i = 0; i < this.listTimeoutTaskItem.size(); i++) {
            if (((TimeoutTaskItem) this.listTimeoutTaskItem.get(i)).timeoutAction == timeoutAction) {
                return i;
            }
        }
        return -1;
    }

    public void execute() {
        synchronized (this) {
            for (int i = 0; i < this.listTimeoutTaskItem.size(); i++) {
                TimeoutTaskItem timeoutTaskItem = (TimeoutTaskItem) this.listTimeoutTaskItem.get(i);
                if (timeoutTaskItem.isEnabled && timeoutTaskItem.timeoutSeconds > 0) {
                    timeoutTaskItem.forwardSeconds++;
                    if (timeoutTaskItem.forwardSeconds >= timeoutTaskItem.timeoutSeconds) {
                        timeoutTaskItem.isEnabled = false;
                    }
                    SysGlobal.execute(new TimeoutTaskExecuteThread(timeoutTaskItem));
                }
            }
        }
    }
}
