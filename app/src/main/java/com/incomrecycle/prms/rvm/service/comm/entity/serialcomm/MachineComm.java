package com.incomrecycle.prms.rvm.service.comm.entity.serialcomm;

import java.io.IOException;

public abstract class MachineComm extends SerialComm {

    public static class TimeControl {
        public static final byte ALIVE_TIME = (byte) -109;
        public static final byte ALIVE_TIME_MAXTIMES = (byte) -108;
        public static final byte TIME_DISABLE = (byte) -111;
        public static final byte TIME_ENABLE = (byte) -110;
        public static final byte TIME_POWER_OFF = (byte) -112;
        public static final byte TIME_POWER_ON = (byte) -119;

        protected TimeControl() {
        }
    }

    public abstract byte[] getState();

    public abstract boolean isCommOK();

    protected abstract void recvData(byte[] bArr);

    public abstract void resetRecvData();

    public abstract boolean sendCmd(byte[] bArr);

    public abstract void serviceEnable(boolean z);

    public abstract void setState(byte[] bArr);

    public MachineComm(String comm, String baud) throws IOException {
        super(comm, baud, 8, 1, false, true);
    }
}
