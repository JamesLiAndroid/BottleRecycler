package com.incomrecycle.common.queue;

public class FIFOQueue extends AbstractQueue {
    private int m_count = 0;
    private QueueElement m_root = null;

    public void push(Object obj) {
        QueueElement tQueueElement = QueueElement.create();
        synchronized (this) {
            if (this.m_root == null) {
                tQueueElement.m_obj = obj;
                this.m_root = tQueueElement;
                this.m_root.m_next = this.m_root;
            } else {
                tQueueElement.m_obj = this.m_root.m_obj;
                this.m_root.m_obj = obj;
                tQueueElement.m_next = this.m_root.m_next;
                this.m_root.m_next = tQueueElement;
                this.m_root = tQueueElement;
            }
            this.m_count++;
            notifyAll();
        }
    }

    public int getSize() {
        return this.m_count;
    }

    public void reset() {
        synchronized (this) {
            while (this.m_root != null) {
                QueueElement tQueueElement;
                if (this.m_root == this.m_root.m_next) {
                    tQueueElement = this.m_root;
                    tQueueElement.m_obj = null;
                    tQueueElement.m_next = null;
                    this.m_root = null;
                } else {
                    tQueueElement = this.m_root.m_next;
                    this.m_root.m_obj = tQueueElement.m_obj;
                    this.m_root.m_next = tQueueElement.m_next;
                    tQueueElement.m_obj = null;
                    tQueueElement.m_next = null;
                }
                this.m_count--;
                if (tQueueElement != null) {
                    QueueElement.release(tQueueElement);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Object pop(long r13) {
        /*
        r12 = this;
        r10 = 0;
        r7 = 0;
        r6 = 0;
        r5 = 0;
        r8 = java.lang.System.currentTimeMillis();
        r3 = r13 + r8;
        monitor-enter(r12);
        r8 = (r13 > r10 ? 1 : (r13 == r10 ? 0 : -1));
        if (r8 != 0) goto L_0x0017;
    L_0x0010:
        r8 = r12.m_root;	 Catch:{ all -> 0x0038 }
        if (r8 != 0) goto L_0x0017;
    L_0x0014:
        monitor-exit(r12);	 Catch:{ all -> 0x0038 }
        r5 = r7;
    L_0x0016:
        return r5;
    L_0x0017:
        r8 = r12.m_root;	 Catch:{ all -> 0x0038 }
        if (r8 != 0) goto L_0x003b;
    L_0x001b:
        r8 = (r13 > r10 ? 1 : (r13 == r10 ? 0 : -1));
        if (r8 >= 0) goto L_0x0027;
    L_0x001f:
        r12.wait();	 Catch:{ Exception -> 0x0023 }
        goto L_0x0017;
    L_0x0023:
        r0 = move-exception;
        monitor-exit(r12);	 Catch:{ all -> 0x0038 }
        r5 = r7;
        goto L_0x0016;
    L_0x0027:
        r1 = java.lang.System.currentTimeMillis();	 Catch:{ Exception -> 0x0023 }
        r8 = (r3 > r1 ? 1 : (r3 == r1 ? 0 : -1));
        if (r8 > 0) goto L_0x0032;
    L_0x002f:
        monitor-exit(r12);	 Catch:{ all -> 0x0038 }
        r5 = r7;
        goto L_0x0016;
    L_0x0032:
        r8 = r3 - r1;
        r12.wait(r8);	 Catch:{ Exception -> 0x0023 }
        goto L_0x0017;
    L_0x0038:
        r7 = move-exception;
        monitor-exit(r12);	 Catch:{ all -> 0x0038 }
        throw r7;
    L_0x003b:
        r7 = r12.m_root;	 Catch:{ all -> 0x0038 }
        r5 = r7.m_obj;	 Catch:{ all -> 0x0038 }
        r7 = r12.m_root;	 Catch:{ all -> 0x0038 }
        r8 = r12.m_root;	 Catch:{ all -> 0x0038 }
        r8 = r8.m_next;	 Catch:{ all -> 0x0038 }
        if (r7 != r8) goto L_0x005f;
    L_0x0047:
        r6 = r12.m_root;	 Catch:{ all -> 0x0038 }
        r7 = 0;
        r6.m_obj = r7;	 Catch:{ all -> 0x0038 }
        r7 = 0;
        r6.m_next = r7;	 Catch:{ all -> 0x0038 }
        r7 = 0;
        r12.m_root = r7;	 Catch:{ all -> 0x0038 }
    L_0x0052:
        r7 = r12.m_count;	 Catch:{ all -> 0x0038 }
        r7 = r7 + -1;
        r12.m_count = r7;	 Catch:{ all -> 0x0038 }
        monitor-exit(r12);	 Catch:{ all -> 0x0038 }
        if (r6 == 0) goto L_0x0016;
    L_0x005b:
        com.incomrecycle.common.queue.QueueElement.release(r6);
        goto L_0x0016;
    L_0x005f:
        r7 = r12.m_root;	 Catch:{ all -> 0x0038 }
        r6 = r7.m_next;	 Catch:{ all -> 0x0038 }
        r7 = r12.m_root;	 Catch:{ all -> 0x0038 }
        r8 = r6.m_obj;	 Catch:{ all -> 0x0038 }
        r7.m_obj = r8;	 Catch:{ all -> 0x0038 }
        r7 = r12.m_root;	 Catch:{ all -> 0x0038 }
        r8 = r6.m_next;	 Catch:{ all -> 0x0038 }
        r7.m_next = r8;	 Catch:{ all -> 0x0038 }
        r7 = 0;
        r6.m_obj = r7;	 Catch:{ all -> 0x0038 }
        r7 = 0;
        r6.m_next = r7;	 Catch:{ all -> 0x0038 }
        goto L_0x0052;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.incomrecycle.common.queue.FIFOQueue.pop(long):java.lang.Object");
    }

    public void push(long timeout, Object obj) {
        push(obj);
    }
}
