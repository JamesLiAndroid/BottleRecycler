package com.incomrecycle.common.task;

import com.incomrecycle.common.queue.FIFOQueue;

public class InstanceTask extends Thread {
    private static final InstanceTask instanceTask = new InstanceTask();
    private FIFOQueue fifoQueue = new FIFOQueue();

    public static InstanceTask getInstanceTask() {
        return instanceTask;
    }

    public void addTask(TaskAction taskAction) {
        this.fifoQueue.push(taskAction);
    }

    public void run() {
        while (true) {
            TaskAction taskAction = (TaskAction) this.fifoQueue.pop();
            if (taskAction != null) {
                taskAction.execute();
            } else {
                return;
            }
        }
    }
}
