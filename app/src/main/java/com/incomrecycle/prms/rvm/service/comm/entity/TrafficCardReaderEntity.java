package com.incomrecycle.prms.rvm.service.comm.entity;

import android_serialport_api.SerialPort;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.queue.FIFOQueue;
import com.incomrecycle.common.utils.EncryptUtils;
import com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameItemType;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class TrafficCardReaderEntity {
    private static final byte ETX = (byte) 3;
    private static final byte STX = (byte) 2;
    private FIFOQueue fifoQueue = new FIFOQueue();
    private SerialPort serialPort = null;

    private class RecvThread extends Thread {
        private RecvThread() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
            r21 = this;
            r5 = 4096; // 0x1000 float:5.74E-42 double:2.0237E-320;
            r0 = r21;
            r0 = com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardReaderEntity.this;	 Catch:{ Exception -> 0x0119 }
            r18 = r0;
            r18 = r18.serialPort;	 Catch:{ Exception -> 0x0119 }
            r9 = r18.getInputStream();	 Catch:{ Exception -> 0x0119 }
            r3 = new byte[r5];	 Catch:{ Exception -> 0x0119 }
            r4 = 0;
            r15 = 0;
        L_0x0014:
            r0 = r3.length;	 Catch:{ Exception -> 0x0119 }
            r18 = r0;
            r18 = r18 - r4;
            r0 = r18;
            r15 = r9.read(r3, r4, r0);	 Catch:{ Exception -> 0x0119 }
            if (r15 <= 0) goto L_0x0100;
        L_0x0021:
            r4 = r4 + r15;
        L_0x0022:
            r17 = -1;
            r12 = -1;
            r8 = 0;
        L_0x0026:
            if (r8 >= r4) goto L_0x0048;
        L_0x0028:
            r18 = r3[r8];	 Catch:{ Exception -> 0x0119 }
            r19 = 2;
            r0 = r18;
            r1 = r19;
            if (r0 != r1) goto L_0x0034;
        L_0x0032:
            r17 = r8;
        L_0x0034:
            r18 = r3[r8];	 Catch:{ Exception -> 0x0119 }
            r19 = 3;
            r0 = r18;
            r1 = r19;
            if (r0 != r1) goto L_0x0064;
        L_0x003e:
            r18 = -1;
            r0 = r17;
            r1 = r18;
            if (r0 == r1) goto L_0x0064;
        L_0x0046:
            r12 = r8 + 1;
        L_0x0048:
            r18 = -1;
            r0 = r17;
            r1 = r18;
            if (r0 != r1) goto L_0x0067;
        L_0x0050:
            r4 = 0;
        L_0x0051:
            if (r4 != r5) goto L_0x0014;
        L_0x0053:
            r5 = r5 + 4096;
            r11 = new byte[r5];	 Catch:{ Exception -> 0x0119 }
            r18 = 0;
            r19 = 0;
            r0 = r18;
            r1 = r19;
            java.lang.System.arraycopy(r3, r0, r11, r1, r4);	 Catch:{ Exception -> 0x0119 }
            r3 = r11;
            goto L_0x0014;
        L_0x0064:
            r8 = r8 + 1;
            goto L_0x0026;
        L_0x0067:
            r18 = -1;
            r0 = r18;
            if (r12 == r0) goto L_0x0051;
        L_0x006d:
            r18 = r17 + 1;
            r19 = 4;
            r0 = r18;
            r1 = r19;
            r18 = com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameItemType.getHString2Long(r3, r0, r1);	 Catch:{ Exception -> 0x0119 }
            r0 = r18;
            r0 = (int) r0;	 Catch:{ Exception -> 0x0119 }
            r18 = r0;
            r14 = r18 + 10;
            r18 = r17 + r14;
            r0 = r18;
            if (r4 < r0) goto L_0x00ed;
        L_0x0086:
            r10 = 0;
            r18 = r17 + r14;
            r18 = r18 + -5;
            r19 = 4;
            r0 = r18;
            r1 = r19;
            r18 = com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameItemType.getHString2Long(r3, r0, r1);	 Catch:{ Exception -> 0x0119 }
            r0 = r18;
            r6 = (int) r0;	 Catch:{ Exception -> 0x0119 }
            r18 = r17 + 5;
            r19 = r14 + -10;
            r0 = r18;
            r1 = r19;
            r16 = com.incomrecycle.common.utils.EncryptUtils.CRC16(r3, r0, r1);	 Catch:{ Exception -> 0x0119 }
            r0 = r16;
            if (r6 != r0) goto L_0x00a9;
        L_0x00a8:
            r10 = 1;
        L_0x00a9:
            if (r10 == 0) goto L_0x00ed;
        L_0x00ab:
            r18 = r14 + -10;
            r0 = r18;
            r13 = new byte[r0];	 Catch:{ Exception -> 0x0119 }
            r18 = r17 + 5;
            r19 = 0;
            r20 = r14 + -10;
            r0 = r18;
            r1 = r19;
            r2 = r20;
            java.lang.System.arraycopy(r3, r0, r13, r1, r2);	 Catch:{ Exception -> 0x0119 }
            r18 = java.lang.System.out;	 Catch:{ Exception -> 0x0119 }
            r19 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0119 }
            r19.<init>();	 Catch:{ Exception -> 0x0119 }
            r20 = "RECV FROM MODULE:";
            r19 = r19.append(r20);	 Catch:{ Exception -> 0x0119 }
            r0 = r17;
            r20 = com.incomrecycle.common.utils.EncryptUtils.byte2hex(r3, r0, r14);	 Catch:{ Exception -> 0x0119 }
            r19 = r19.append(r20);	 Catch:{ Exception -> 0x0119 }
            r19 = r19.toString();	 Catch:{ Exception -> 0x0119 }
            r18.println(r19);	 Catch:{ Exception -> 0x0119 }
            r0 = r21;
            r0 = com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardReaderEntity.this;	 Catch:{ Exception -> 0x0119 }
            r18 = r0;
            r18 = r18.fifoQueue;	 Catch:{ Exception -> 0x0119 }
            r0 = r18;
            r0.push(r13);	 Catch:{ Exception -> 0x0119 }
        L_0x00ed:
            if (r4 <= r12) goto L_0x00fd;
        L_0x00ef:
            r18 = 0;
            r19 = r4 - r12;
            r0 = r18;
            r1 = r19;
            java.lang.System.arraycopy(r3, r12, r3, r0, r1);	 Catch:{ Exception -> 0x0119 }
            r4 = r4 - r12;
            goto L_0x0022;
        L_0x00fd:
            r4 = 0;
            goto L_0x0022;
        L_0x0100:
            r0 = r21;
            r0 = com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardReaderEntity.this;
            r18 = r0;
            r18 = r18.serialPort;
            r18.close();
            r0 = r21;
            r0 = com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardReaderEntity.this;
            r18 = r0;
            r19 = 0;
            r18.serialPort = r19;
        L_0x0118:
            return;
        L_0x0119:
            r7 = move-exception;
            r7.printStackTrace();	 Catch:{ all -> 0x0136 }
            r0 = r21;
            r0 = com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardReaderEntity.this;
            r18 = r0;
            r18 = r18.serialPort;
            r18.close();
            r0 = r21;
            r0 = com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardReaderEntity.this;
            r18 = r0;
            r19 = 0;
            r18.serialPort = r19;
            goto L_0x0118;
        L_0x0136:
            r18 = move-exception;
            r0 = r21;
            r0 = com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardReaderEntity.this;
            r19 = r0;
            r19 = r19.serialPort;
            r19.close();
            r0 = r21;
            r0 = com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardReaderEntity.this;
            r19 = r0;
            r20 = 0;
            r19.serialPort = r20;
            throw r18;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardReaderEntity.RecvThread.run():void");
        }
    }

    public void init(String commFile, int baud) {
        if (this.serialPort == null) {
            synchronized (this) {
                try {
                    this.serialPort = new SerialPort(new File(commFile), baud, 0);
                    SysGlobal.execute(new RecvThread());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void done() {
        if (this.serialPort != null) {
            this.serialPort.close();
            this.serialPort = null;
        }
    }

    public boolean isOpen() {
        return this.serialPort != null;
    }

    public void send(byte[] pkgBody, int pos, int len) {
        byte[] pkgData = new byte[(len + 10)];
        FrameItemType.setByte(pkgData, 0, (byte) 2);
        FrameItemType.setLong2HString(pkgData, 1, 4, (long) ((short) len));
        System.arraycopy(pkgBody, pos, pkgData, 5, len);
        FrameItemType.setLong2HString(pkgData, pkgData.length - 5, 4, (long) EncryptUtils.CRC16(pkgBody, pos, len));
        FrameItemType.setByte(pkgData, pkgData.length - 1, (byte) 3);
        System.out.println("SEND TO MODULE:" + EncryptUtils.byte2hex(pkgData));
        try {
            OutputStream os = this.serialPort.getOutputStream();
            synchronized (os) {
                os.write(pkgData);
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] recv(long milliSeconds) {
        return (byte[]) this.fifoQueue.pop(milliSeconds);
    }
}
