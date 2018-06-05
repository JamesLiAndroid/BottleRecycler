package com.incomrecycle.prms.rvm.service.comm.entity.serialcomm;

import android.support.v4.view.MotionEventCompat;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.queue.FIFOQueue;
import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.common.task.TickTaskThread;
import com.incomrecycle.common.utils.EncryptUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MCUSerialComm extends MachineComm {
    private static final byte HERR_HEAD = (byte) -14;
    private static final byte HTIME_HEAD = (byte) -16;
    private static final byte RECV_HEAD = (byte) -91;
    private static final byte SEND_HEAD = (byte) -86;
    private static final Logger logger = LoggerFactory.getLogger("MCUSerialComm");
    private byte[] buff;
    private byte[] checkedCmd = null;
    private byte checkedSeq = (byte) 0;
    private FIFOQueue cmdResultFIFOQueue = null;
    private CommRecvCallBack commRecvCallBack = null;
    private CommStateCallBack commStateCallBack = null;
    private int dataLen = 0;
    private long errorTimeout;
    private int errorTimes;
    private boolean hasRecv = false;
    private long lCheckedSendTime = 0;
    private long lLastRecvTime = 0;
    private byte[] mcuState = new byte[4];
    private byte recvSeqVal = (byte) 0;
    private FIFOQueue respFIFOQueue = new FIFOQueue();
    private FIFOQueue sendFIFOQueue = new FIFOQueue();
    private byte sendSeqVal = (byte) 0;

    private class SendThread extends Thread {
        private byte[] recvData;

        private SendThread() {
        }

        public void run() {
            while (MCUSerialComm.this.isOpen()) {
                byte[] cmd = (byte[])MCUSerialComm.this.sendFIFOQueue.pop();
                if (cmd != null) {
                    byte seqVal = MCUSerialComm.this.sendSeqVal;
                    MCUSerialComm.this.sendSeqVal = (byte) (MCUSerialComm.this.sendSeqVal + 1);
                    byte[] sendData = new byte[]{MCUSerialComm.SEND_HEAD, seqVal,
                            cmd[0], cmd[1], cmd[2], cmd[3], (byte) (((( seqVal ^cmd[0] ^ cmd[1]) ^ cmd[2]) ^ cmd[3]) ^ cmd[4])};
                    boolean hasResponse = false;
                    int i = 0;
                    while (i <= MCUSerialComm.this.errorTimes) {
                        if (MCUSerialComm.this.lCheckedSendTime == 0 || (MCUSerialComm.this.checkedSeq != seqVal && MCUSerialComm.this.lCheckedSendTime < MCUSerialComm.this.lLastRecvTime)) {
                            MCUSerialComm.this.lCheckedSendTime = System.currentTimeMillis();
                            MCUSerialComm.this.checkedSeq = seqVal;
                            MCUSerialComm.this.checkedCmd = sendData;
                        }
                        if (MCUSerialComm.this.sendTo(sendData, 0, 7)) {
                            do {
                                byte[] bArr = (byte[]) MCUSerialComm.this.respFIFOQueue.pop(MCUSerialComm.this.errorTimeout);
                                this.recvData = bArr;
                                if (bArr == null) {
                                    break;
                                }
                            } while (this.recvData[1] != sendData[1]);
                            if (this.recvData[2] == TimeControl.TIME_POWER_ON && MCUSerialComm.this.cmdResultFIFOQueue != null) {
                                MCUSerialComm.this.cmdResultFIFOQueue.push("POWER_ON_TIME_OK");
                            }
                            if (this.recvData[2] == TimeControl.TIME_POWER_OFF && MCUSerialComm.this.cmdResultFIFOQueue != null) {
                                MCUSerialComm.this.cmdResultFIFOQueue.push("POWER_OFF_TIME_OK");
                            }
                            if (this.recvData[2] == TimeControl.TIME_DISABLE && MCUSerialComm.this.cmdResultFIFOQueue != null) {
                                MCUSerialComm.this.cmdResultFIFOQueue.push("POWER_OFF_DISABLE_OK");
                            }
                            if (this.recvData[2] == TimeControl.TIME_ENABLE && MCUSerialComm.this.cmdResultFIFOQueue != null) {
                                MCUSerialComm.this.cmdResultFIFOQueue.push("POWER_OFF_ENABLE_OK");
                            }
                            if (this.recvData[2] == TimeControl.ALIVE_TIME && MCUSerialComm.this.cmdResultFIFOQueue != null) {
                                MCUSerialComm.this.cmdResultFIFOQueue.push("RVM_ALIVE_TIME_OK");
                            }
                            if (this.recvData[2] == TimeControl.ALIVE_TIME_MAXTIMES && MCUSerialComm.this.cmdResultFIFOQueue != null) {
                                MCUSerialComm.this.cmdResultFIFOQueue.push("RVM_ALIVE_TIME_MAXTIMES_OK");
                            }
                            hasResponse = true;
                            if (hasResponse) {
                                break;
                            }
                            i++;
                        } else {
                            return;
                        }
                    }
                    continue;
                }
            }
        }
    }

    public MCUSerialComm(String comm, String baud, CommRecvCallBack commRecvCallBack, CommStateCallBack commStateCallBack, int errorTimes, long errorTimeout, FIFOQueue cmdResultFIFOQueue) throws IOException {
        super(comm, baud);
        this.commRecvCallBack = commRecvCallBack;
        this.commStateCallBack = commStateCallBack;
        if (errorTimes < 0) {
            errorTimes = 0;
        }
        if (errorTimeout < 20) {
            errorTimeout = 20;
        }
        this.errorTimes = errorTimes;
        this.errorTimeout = errorTimeout;
        this.cmdResultFIFOQueue = cmdResultFIFOQueue;
        SysGlobal.execute(new SendThread());
        for (int i = 0; i < 4; i++) {
            this.mcuState[i] = (byte) 0;
        }
        sendCmd("QQSS".getBytes());
        TickTaskThread.getTickTaskThread().register(new TaskAction() {
            public void execute() {
                MCUSerialComm.this.triggerState();
            }
        }, 1.0d, false);
    }

    public boolean sendCmd(byte[] cmd) {
        if (!isOpen()) {
            return false;
        }
        this.sendFIFOQueue.push(cmd);
        return true;
    }

    public void triggerState() {
        synchronized (this.mcuState) {
            if (!(this.commStateCallBack == null || this.mcuState == null)) {
                this.commStateCallBack.apply(this.mcuState);
            }
        }
    }

    public void setState(byte[] state) {
        boolean isChanged = false;
        for (int i = 0; i < state.length; i++) {
            if (state[i] != this.mcuState[i]) {
                isChanged = true;
                break;
            }
        }
        System.arraycopy(state, 0, this.mcuState, 0, this.mcuState.length);
        if (this.commStateCallBack != null) {
            this.commStateCallBack.apply(state);
        }
        if (isChanged) {
            logger.debug("MCU State:" + EncryptUtils.byte2hex(state));
        }
    }

    public byte[] getState() {
        return this.mcuState;
    }

    public void serviceEnable(boolean serviceEnable) {
    }

    public void recvData(byte[] recv) {
        byte[] event = new byte[4];
        boolean hasEvent = false;
        synchronized (this.mcuState) {
            for (int i = 0; i < 4; i++) {
                event[i] = (byte) (((this.mcuState[i] ^ MotionEventCompat.ACTION_MASK) & recv[i]) & MotionEventCompat.ACTION_MASK);
                if (event[i] != (byte) 0) {
                    hasEvent = true;
                }
            }
            setState(recv);
        }
        if (hasEvent && this.commRecvCallBack != null) {
            this.commRecvCallBack.apply(event);
        }
    }

    public void resetRecvData() {
        this.dataLen = 0;
    }

    protected void recvFrom(byte[] data, int pos, int len) {
        if (data != null) {
            int i = 0;
            while (i < len) {
                if (this.dataLen != 0 || data[pos + i] == SEND_HEAD || data[pos + i] == RECV_HEAD) {
                    if (this.dataLen == 0 && (data[pos + i] == SEND_HEAD || data[pos + i] == RECV_HEAD)) {
                        this.buff = new byte[7];
                    }
                    this.buff[this.dataLen] = data[pos + i];
                    this.dataLen++;
                    if (this.dataLen == 7) {
                        if (this.buff[6] == ((byte) ((((this.buff[1] ^ this.buff[2]) ^ this.buff[3]) ^ this.buff[4]) ^ this.buff[5]))) {
                            this.lLastRecvTime = System.currentTimeMillis();
                            if (this.buff[0] == SEND_HEAD) {
                                this.buff[0] = RECV_HEAD;
                                sendTo(this.buff, 0, 7);
                                if (!(this.hasRecv && this.recvSeqVal == this.buff[1])) {
                                    this.recvSeqVal = this.buff[1];
                                    recvData(new byte[]{this.buff[2], this.buff[3], this.buff[4], this.buff[5]});
                                }
                                this.hasRecv = true;
                            } else {
                                this.respFIFOQueue.push(this.buff);
                            }
                            this.dataLen = 0;
                        } else {
                            this.dataLen--;
                            int p = 1;
                            while (p < 7 && this.buff[p] != SEND_HEAD && this.buff[p] != RECV_HEAD) {
                                this.dataLen--;
                                p++;
                            }
                            if (this.dataLen > 0) {
                                for (p = 0; p < this.dataLen; p++) {
                                    this.buff[p] = this.buff[(7 - this.dataLen) + p];
                                }
                            }
                        }
                    }
                }
                i++;
            }
        }
    }

    public boolean isCommOK() {
        if (this.lCheckedSendTime <= 0 || this.lCheckedSendTime <= this.lLastRecvTime || System.currentTimeMillis() - this.lCheckedSendTime <= 30000) {
            return true;
        }
        logger.debug("MCU CMD RES not received:" + EncryptUtils.byte2hex(this.checkedCmd));
        return false;
    }

    protected boolean checkComm(InputStream is, OutputStream os) {
        return true;
    }
}
