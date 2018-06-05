package com.incomrecycle.prms.rvm.service.comm.entity.serialcomm;

import android.support.v4.view.MotionEventCompat;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.queue.FIFOQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class K5V1SerialComm extends MachineComm {
    private byte[] buff = null;
    private FIFOQueue cmdResultFIFOQueue = null;
    private int cmdSendSeq = 15;
    private CommRecvCallBack commRecvCallBack = null;
    private CommStateCallBack commStateCallBack = null;
    private int dataLen = 0;
    private FIFOQueue k5v1FIFOQueueRecv = new FIFOQueue();
    private FIFOQueue k5v1FIFOQueueSend = new FIFOQueue();
    private long lCheckedSendTime = 0;
    private long lLastRecvTime = 0;
    private boolean needQueryState = true;
    private byte[] plcState = null;
    private Thread queryThreadK5V1 = null;
    private byte rebootFailTimes = (byte) 0;
    private byte[] recvSeqData = null;
    private Thread sendThreadK5V1 = null;
    private byte[] sendingCmd = new byte[4];
    private boolean serviceEnable = false;

    private static final class PLCFormatFlag {
        private static final int DATA_LEN = 6;
        private static final byte RECV_CMD_HEAD = (byte) -91;
        private static final byte RECV_QRY_HEAD = (byte) -86;
        private static final byte SEND_HEAD = (byte) 48;
        private static final byte SEND_TAIL = (byte) 49;

        private PLCFormatFlag() {
        }
    }

    public static final class PLC_CMD {
        public static final byte[] QUERY_FIRST = "QQEE".getBytes();
        public static final byte[] QUERY_RETRY = "QQRR".getBytes();
        public static final byte[] QUERY_STATE = "QQSS".getBytes();
    }

    private class QueryThreadK5V1 extends Thread {
        private QueryThreadK5V1() {
        }

        public void run() {
            long comPlcRecvTimeout = Long.parseLong(SysConfig.get("COM.PLC.RECV.TIMEOUT"));
            while (K5V1SerialComm.this.isOpen()) {
                long finalComPlcRecvTimeout = comPlcRecvTimeout;
                if (!K5V1SerialComm.this.serviceEnable) {
                    finalComPlcRecvTimeout = 1000;
                }
                try {
                    if (K5V1SerialComm.this.isOpen() && K5V1SerialComm.this.k5v1FIFOQueueSend.getSize() == 0) {
                        K5V1SerialComm.this.k5v1FIFOQueueSend.push(PLC_CMD.QUERY_FIRST);
                    }
                    Thread.sleep(finalComPlcRecvTimeout);
                } catch (Exception e) {
                }
            }
        }
    }

    private class SendThreadK5V1 extends Thread {
        private SendThreadK5V1() {
        }

        public void run() {
            long comPlcRecvTimeout = Long.parseLong(SysConfig.get("COM.PLC.RECV.TIMEOUT"));
            if (comPlcRecvTimeout < 50) {
                comPlcRecvTimeout = 50;
            }
            while (K5V1SerialComm.this.isOpen()) {
                byte[] newCmd = (byte[]) K5V1SerialComm.this.k5v1FIFOQueueSend.pop(5000);
                if (newCmd != null) {
                    if (newCmd[0] == TimeControl.ALIVE_TIME_MAXTIMES) {
                        K5V1SerialComm.this.rebootFailTimes = newCmd[3];
                    }
                    System.arraycopy(newCmd, 0, K5V1SerialComm.this.sendingCmd, 0, K5V1SerialComm.this.sendingCmd.length);
                    byte[] finalCmd = new byte[6];
                    for (int p = 0; p < 4; p++) {
                        if (p < newCmd.length) {
                            finalCmd[p + 1] = newCmd[p];
                        } else {
                            finalCmd[p + 1] = (byte) 0;
                        }
                    }
                    finalCmd[0] = (byte) 48;
                    finalCmd[5] = (byte) 49;
                    int repeatTimes = 2;
                    if (K5V1SerialComm.this.isEquals(K5V1SerialComm.this.sendingCmd, PLC_CMD.QUERY_FIRST) || K5V1SerialComm.this.isEquals(K5V1SerialComm.this.sendingCmd, PLC_CMD.QUERY_RETRY) || K5V1SerialComm.this.isEquals(K5V1SerialComm.this.sendingCmd, PLC_CMD.QUERY_STATE)) {
                        repeatTimes = 1;
                    }
                    for (int i = 0; i < repeatTimes; i++) {
                        if (K5V1SerialComm.this.isEquals(K5V1SerialComm.this.sendingCmd, PLC_CMD.QUERY_STATE)) {
                            byte[] seqCmd = new byte[6];
                            K5V1SerialComm.this.cmdSendSeq = (K5V1SerialComm.this.cmdSendSeq + 16) & MotionEventCompat.ACTION_MASK;
                            seqCmd[0] = (byte) 48;
                            seqCmd[1] = TimeControl.ALIVE_TIME_MAXTIMES;
                            seqCmd[2] = (byte) 0;
                            seqCmd[3] = (byte) K5V1SerialComm.this.cmdSendSeq;
                            seqCmd[4] = K5V1SerialComm.this.rebootFailTimes;
                            seqCmd[5] = (byte) 49;
                            if (!K5V1SerialComm.this.sendTo(seqCmd, 0, seqCmd.length)) {
                                K5V1SerialComm.this.close();
                                break;
                            }
                            try {
                                Thread.sleep(comPlcRecvTimeout);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (K5V1SerialComm.this.lCheckedSendTime == 0 || K5V1SerialComm.this.lCheckedSendTime < K5V1SerialComm.this.lLastRecvTime) {
                            K5V1SerialComm.this.lCheckedSendTime = System.currentTimeMillis();
                        }
                        if (!K5V1SerialComm.this.sendTo(finalCmd, 0, finalCmd.length)) {
                            K5V1SerialComm.this.close();
                            break;
                        }
                        try {
                            Thread.sleep(comPlcRecvTimeout);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                        if (((byte[]) K5V1SerialComm.this.k5v1FIFOQueueRecv.pop(comPlcRecvTimeout)) != null) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private class StateQueryThread extends Thread {
        private StateQueryThread() {
        }

        public void run() {
            int normalCount = 0;
            while (true) {
                try {
                    Thread.sleep(1000);
                    K5V1SerialComm.this.needQueryState = false;
                    if (K5V1SerialComm.this.needQueryState || normalCount == 0) {
                        K5V1SerialComm.this.needQueryState = false;
                        K5V1SerialComm.this.sendCmd(PLC_CMD.QUERY_STATE);
                        normalCount = 5;
                    }
                    if (normalCount > 0) {
                        normalCount--;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public K5V1SerialComm(String comm, String baud, CommRecvCallBack commRecvCallBack, CommStateCallBack commStateCallBack, FIFOQueue cmdResultFIFOQueue) throws IOException {
        super(comm, baud);
        this.commRecvCallBack = commRecvCallBack;
        this.commStateCallBack = commStateCallBack;
        this.cmdResultFIFOQueue = cmdResultFIFOQueue;
        this.plcState = new byte[4];
        for (int i = 0; i < 4; i++) {
            this.plcState[i] = (byte) 0;
        }
        if (this.queryThreadK5V1 == null) {
            this.queryThreadK5V1 = new QueryThreadK5V1();
            SysGlobal.execute(this.queryThreadK5V1);
        }
        if (this.sendThreadK5V1 == null) {
            this.sendThreadK5V1 = new SendThreadK5V1();
            this.sendThreadK5V1.setPriority(10);
            SysGlobal.execute(this.sendThreadK5V1);
        }
        SysGlobal.execute(new StateQueryThread());
    }

    public boolean sendCmd(byte[] cmd) {
        if (!isOpen()) {
            return false;
        }
        this.k5v1FIFOQueueSend.push(cmd);
        return true;
    }

    public void setRecvCallback(CommRecvCallBack commRecvCallBack) {
        this.commRecvCallBack = commRecvCallBack;
    }

    public void setState(byte[] state) {
        System.arraycopy(state, 0, this.plcState, 0, this.plcState.length);
        if (this.commStateCallBack != null) {
            this.commStateCallBack.apply(state);
        }
    }

    public byte[] getState() {
        return this.plcState;
    }

    public void serviceEnable(boolean serviceEnable) {
        this.serviceEnable = serviceEnable;
    }

    public void recvData(byte[] recv) {
        byte[] event = new byte[4];
        byte[] newState = new byte[4];
        boolean hasEvent = false;
        for (int i = 0; i < 4; i++) {
            event[i] = recv[i];
            if (event[i] != (byte) 0) {
                hasEvent = true;
            }
            newState[i] = (byte) (event[i] | this.plcState[i]);
        }
        if (hasEvent) {
            this.needQueryState = true;
        }
        if (hasEvent && this.commRecvCallBack != null) {
            this.commRecvCallBack.apply(event);
        }
    }

    public void resetRecvData() {
        this.dataLen = 0;
    }

    protected void recvFrom(byte[] recv, int pos, int len) {
        if (recv != null) {
            for (int i = 0; i < len; i++) {
                if (this.dataLen == 0) {
                    if (!isRecvHead(recv[pos + i])) {
                    } else if (this.buff == null) {
                        this.buff = new byte[6];
                    }
                }
                this.buff[this.dataLen] = recv[pos + i];
                this.dataLen++;
                if (this.dataLen == 6) {
                    if (checkRecv(this.buff, 0)) {
                        this.lLastRecvTime = System.currentTimeMillis();
                        if (isCmdResponse(this.buff, 0) && this.buff[1] == TimeControl.ALIVE_TIME_MAXTIMES && (this.buff[3] & 15) == 15) {
                            this.recvSeqData = this.buff;
                        } else {
                            int t;
                            byte[] recvData = new byte[4];
                            for (t = 0; t < 4; t++) {
                                recvData[t] = this.buff[t + 1];
                            }
                            if (isCmdResponse(this.buff, 0)) {
                                if (recvData[0] == TimeControl.TIME_POWER_ON && this.cmdResultFIFOQueue != null) {
                                    this.cmdResultFIFOQueue.push("POWER_ON_TIME_OK");
                                }
                                if (recvData[0] == TimeControl.TIME_POWER_OFF && this.cmdResultFIFOQueue != null) {
                                    this.cmdResultFIFOQueue.push("POWER_OFF_TIME_OK");
                                }
                                if (recvData[0] == TimeControl.TIME_DISABLE && this.cmdResultFIFOQueue != null) {
                                    this.cmdResultFIFOQueue.push("POWER_OFF_DISABLE_OK");
                                }
                                if (recvData[0] == TimeControl.TIME_ENABLE && this.cmdResultFIFOQueue != null) {
                                    this.cmdResultFIFOQueue.push("POWER_OFF_ENABLE_OK");
                                }
                                if (recvData[0] == TimeControl.ALIVE_TIME && this.cmdResultFIFOQueue != null) {
                                    this.cmdResultFIFOQueue.push("RVM_ALIVE_TIME_OK");
                                }
                                if (recvData[0] == TimeControl.ALIVE_TIME_MAXTIMES && this.cmdResultFIFOQueue != null) {
                                    this.cmdResultFIFOQueue.push("RVM_ALIVE_TIME_MAXTIMES_OK");
                                }
                            }
                            if (isQryResponse(this.buff, 0)) {
                                if (this.recvSeqData == null) {
                                    for (t = 0; t < 4; t++) {
                                        if (recvData[t] != (byte) 0) {
                                            recvData(recvData);
                                            break;
                                        }
                                    }
                                } else {
                                    setState(recvData);
                                }
                                if (isEquals(this.sendingCmd, PLC_CMD.QUERY_FIRST) || isEquals(this.sendingCmd, PLC_CMD.QUERY_RETRY) || isEquals(this.sendingCmd, PLC_CMD.QUERY_STATE)) {
                                    this.k5v1FIFOQueueRecv.push(recvData);
                                }
                            } else if (isEquals(this.sendingCmd, recvData)) {
                                this.k5v1FIFOQueueRecv.push(recvData);
                            }
                            this.recvSeqData = null;
                        }
                        this.buff = null;
                        this.dataLen = 0;
                    } else {
                        int newPos = findRecvHead(this.buff, 1, 5);
                        if (newPos == -1) {
                            this.dataLen = 0;
                        } else {
                            for (int p = 0; p < 6 - newPos; p++) {
                                this.buff[p] = this.buff[p + newPos];
                            }
                            this.dataLen = 6 - newPos;
                        }
                    }
                }
            }
        }
    }

    private boolean isEquals(byte[] d1, byte[] d2) {
        if (d1 == null && d2 == null) {
            return true;
        }
        if (d1 == null || d2 == null) {
            return false;
        }
        if (d1.length != d2.length) {
            return false;
        }
        for (int i = 0; i < d1.length; i++) {
            if (d1[i] != d2[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isQryResponse(byte[] data, int pos) {
        return data[pos] == (byte) -86;
    }

    private static boolean isCmdResponse(byte[] data, int pos) {
        return data[pos] == (byte) -91;
    }

    private static boolean isRecvHead(byte b) {
        return b == (byte) -91 || b == (byte) -86;
    }

    private static boolean checkRecv(byte[] data, int pos) {
        if (data == null || pos < 0 || data.length < pos + 6) {
            return false;
        }
        if ((data[pos] == (byte) -91 || data[pos] == (byte) -86) && data[pos + 5] == ((byte) (((data[pos + 1] ^ data[pos + 2]) ^ data[pos + 3]) ^ data[pos + 4]))) {
            return true;
        }
        return false;
    }

    private static int findRecvHead(byte[] data, int pos, int len) {
        int i = 0;
        while (i < len) {
            if (data[pos + i] == (byte) -91 || data[pos + i] == (byte) -86) {
                return pos + i;
            }
            i++;
        }
        return -1;
    }

    private static byte[] generateCmdPkg(byte[] cmd, int seq) {
        return new byte[]{(byte) 48, cmd[0], cmd[1], cmd[2], cmd[3], (byte) 49};
    }

    public boolean isCommOK() {
        if (this.lCheckedSendTime <= 0 || this.lCheckedSendTime <= this.lLastRecvTime || System.currentTimeMillis() - this.lCheckedSendTime <= 30000) {
            return true;
        }
        return false;
    }

    protected boolean checkComm(InputStream is, OutputStream os) {
        try {
            byte[] cmdPkg = generateCmdPkg(PLC_CMD.QUERY_STATE, 0);
            int len = 0;
            for (int i = 0; i < 2; i++) {
                os.write(cmdPkg);
                Thread.sleep(5000);
                len = is.available();
                if (len >= cmdPkg.length) {
                    is.read(cmdPkg);
                    break;
                }
            }
            if (len >= cmdPkg.length) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
