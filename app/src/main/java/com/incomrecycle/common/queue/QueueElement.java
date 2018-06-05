package com.incomrecycle.common.queue;

public class QueueElement {
    private static QueueElement m_root = null;
    public QueueElement m_next = null;
    public Object m_obj = null;
    public QueueElement m_prev = null;

    private QueueElement() {
    }

    public static final synchronized QueueElement create() {
        QueueElement queueElement;
        synchronized (QueueElement.class) {
            if (m_root == null) {
                queueElement = new QueueElement();
            } else {
                queueElement = m_root;
                m_root = m_root.m_next;
                queueElement.m_next = null;
            }
        }
        return queueElement;
    }

    public static final synchronized void release(QueueElement tQueueElement) {
        synchronized (QueueElement.class) {
            tQueueElement.m_obj = null;
            tQueueElement.m_prev = null;
            tQueueElement.m_next = m_root;
            m_root = tQueueElement;
        }
    }
}
