package com.incomrecycle.common.utils;

public class DataBuffer {
    private static final int BLOCK_SIZE = 1024;
    private static final Object g_ObjLcok = new Object();
    private static DataBlock g_root = null;
    private int m_blocks = 0;
    private DataBlock m_head = null;
    private DataBlock m_tail = null;

    private static class DataBlock {
        byte[] m_buff;
        int m_len;
        DataBlock m_next;
        int m_offset;

        private DataBlock() {
            this.m_next = null;
            this.m_buff = new byte[1024];
            this.m_offset = 0;
            this.m_len = 0;
        }

        public int getFreeSize() {
            return (1024 - this.m_offset) - this.m_len;
        }
    }

    public int length() {
        if (this.m_blocks == 0) {
            return 0;
        }
        return ((this.m_blocks * 1024) - this.m_head.m_offset) - this.m_tail.getFreeSize();
    }

    private void preAppend() {
        if (this.m_tail == null) {
            this.m_head = createDataBlock();
            this.m_tail = this.m_head;
            this.m_blocks++;
        } else if (this.m_tail.m_offset + this.m_tail.m_len == 1024) {
            this.m_tail.m_next = createDataBlock();
            this.m_tail = this.m_tail.m_next;
            this.m_blocks++;
        }
    }

    public void append(byte b) {
        preAppend();
        this.m_tail.m_buff[this.m_tail.m_offset + this.m_tail.m_len] = b;
        DataBlock dataBlock = this.m_tail;
        dataBlock.m_len++;
    }

    public void append(byte[] buff) {
        append(buff, 0, buff.length);
    }

    public void append(byte[] buff, int offset, int len) {
        if (offset < 0 || offset >= buff.length) {
            throw new RuntimeException("Out of index!");
        }
        if (len > buff.length - offset) {
            len = buff.length - offset;
        }
        while (len > 0) {
            preAppend();
            int max = this.m_tail.getFreeSize();
            if (max > len) {
                max = len;
            }
            System.arraycopy(buff, offset, this.m_tail.m_buff, this.m_tail.m_offset + this.m_tail.m_len, max);
            DataBlock dataBlock = this.m_tail;
            dataBlock.m_len += max;
            offset += max;
            len -= max;
        }
    }

    public byte get(int offset) {
        if (offset < 0 || offset >= length()) {
            throw new RuntimeException("Out of index!");
        }
        DataBlock tDataBlock = this.m_head;
        while (offset >= tDataBlock.m_len) {
            offset -= tDataBlock.m_len;
            tDataBlock = tDataBlock.m_next;
        }
        return tDataBlock.m_buff[tDataBlock.m_offset + offset];
    }

    public int copy(byte[] buff, int offset, int len) {
        int i = 0;
        if (offset != length()) {
            if (offset < 0 || offset > length()) {
                throw new RuntimeException("Out of index!");
            }
            int max = length() - offset;
            if (len > max) {
                len = max;
            }
            if (max != 0) {
                i = len;
                DataBlock tDataBlock = this.m_head;
                while (offset >= tDataBlock.m_len) {
                    offset -= tDataBlock.m_len;
                    tDataBlock = tDataBlock.m_next;
                }
                int idx = 0;
                while (len > 0) {
                    max = tDataBlock.m_len - offset;
                    if (max > len) {
                        max = len;
                    }
                    System.arraycopy(tDataBlock.m_buff, tDataBlock.m_offset + offset, buff, idx, max);
                    offset = 0;
                    len -= max;
                    idx += max;
                    tDataBlock = tDataBlock.m_next;
                }
            }
        }
        return i;
    }

    public byte[] get(int offset, int len) {
        if (offset < 0 || offset >= length()) {
            throw new RuntimeException("Out of index!");
        }
        int max = length() - offset;
        if (len > max) {
            len = max;
        }
        if (max == 0) {
            return new byte[0];
        }
        byte[] buff = new byte[len];
        copy(buff, 0, len);
        return buff;
    }

    public void remove(int len) {
        int max = length();
        if (len > max) {
            len = max;
        }
        while (len > 0) {
            if (len >= this.m_head.m_len) {
                len -= this.m_head.m_len;
                DataBlock tDataBlock = this.m_head;
                this.m_head = this.m_head.m_next;
                if (this.m_head == null) {
                    this.m_tail = null;
                }
                releaseDataBlock(tDataBlock);
                this.m_blocks--;
            } else {
                DataBlock dataBlock = this.m_head;
                dataBlock.m_offset += len;
                dataBlock = this.m_head;
                dataBlock.m_len -= len;
                return;
            }
        }
    }

    public void clear() {
        remove(length());
    }

    private static DataBlock createDataBlock() {
        DataBlock tDataBlock;
        synchronized (g_ObjLcok) {
            if (g_root == null) {
                tDataBlock = new DataBlock();
            } else {
                tDataBlock = g_root;
                g_root = g_root.m_next;
                tDataBlock.m_len = 0;
                tDataBlock.m_offset = 0;
                tDataBlock.m_next = null;
            }
        }
        return tDataBlock;
    }

    private static void releaseDataBlock(DataBlock tDataBlock) {
        synchronized (g_ObjLcok) {
            tDataBlock.m_next = g_root;
            g_root = tDataBlock;
        }
    }
}
