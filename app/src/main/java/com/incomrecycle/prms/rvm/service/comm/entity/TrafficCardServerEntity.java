package com.incomrecycle.prms.rvm.service.comm.entity;

import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.queue.FIFOQueue;
import com.incomrecycle.common.utils.EncryptUtils;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.SocketUtils;
import com.incomrecycle.prms.rvm.common.SysDef.TrafficType;
import com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameItemType;
import com.incomrecycle.prms.rvm.service.traffic.TrafficEntity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TrafficCardServerEntity {
    private FIFOQueue fifoQueueSocket = new FIFOQueue();
    private InputStream isSocket = null;
    private OutputStream osSocket = null;
    private Socket socket = null;

    private class RecvThread extends Thread {
        InputStream is = null;

        public RecvThread(InputStream is) {
            this.is = is;
        }

        public void run() {
            int buffSize = 4096;
            try {
                byte[] buff = new byte[4096];
                int buffLen = 0;
                while (true) {
                    int readLen = this.is.read(buff, buffLen, buff.length - buffLen);
                    if (readLen > 0) {
                        buffLen += readLen;
                        if (buffLen > 2) {
                            int packageLen = ((int) FrameItemType.getHEX2Long(buff, 0, 2)) + 2;
                            if (buffLen >= packageLen) {
                                byte[] packageData = new byte[packageLen];
                                System.arraycopy(buff, 0, packageData, 0, packageLen);
                                for (int i = packageLen; i < buffLen; i++) {
                                    buff[i - packageLen] = buff[i];
                                }
                                buffLen -= packageLen;
                                System.out.println("RECV FROM SRV:" + EncryptUtils.byte2hex(packageData));
                                TrafficCardServerEntity.this.fifoQueueSocket.push(packageData);
                            }
                        }
                        if (buffLen == buffSize) {
                            buffSize += 4096;
                            byte[] newBuff = new byte[buffSize];
                            System.arraycopy(buff, 0, newBuff, 0, buffLen);
                            buff = newBuff;
                        }
                        TrafficEntity.addData(TrafficType.TRAFFICCARD, buffLen);
                    } else {
                        return;
                    }
                }
            } catch (Exception e) {
                if (TrafficCardServerEntity.this.isSocket != null) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean connect(String ip, int port) {
        if (this.socket == null) {
            try {
                this.socket = SocketUtils.createSocket(ip, port, 1000);
                this.isSocket = this.socket.getInputStream();
                this.osSocket = this.socket.getOutputStream();
                this.fifoQueueSocket.reset();
                SysGlobal.execute(new RecvThread(this.isSocket));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.socket != null) {
            return true;
        }
        return false;
    }

    public void disconnect() {
        if (this.isSocket != null) {
            this.isSocket = IOUtils.close(this.isSocket);
            this.osSocket = IOUtils.close(this.osSocket);
            this.socket = SocketUtils.close(this.socket);
        }
    }

    public void clear() {
        this.fifoQueueSocket.reset();
    }

    public void send(short dataSeq, byte[] data, int pos, int len) throws Exception {
        byte[] buff = new byte[(len + 9)];
        FrameItemType.setLong2HEX(buff, 0, 2, (long) ((short) (buff.length - 2)));
        FrameItemType.setLong2HEX(buff, 2, 2, (long) dataSeq);
        FrameItemType.setByte(buff, 4, (byte) 0);
        System.arraycopy(data, pos, buff, 5, len);
        FrameItemType.setCRC2Byte4(buff, len + 5, EncryptUtils.CRC16(data, pos, len));
        System.out.println("SEND TO SRV:" + EncryptUtils.byte2hex(buff));
        if (this.osSocket != null) {
            synchronized (this.osSocket) {
                this.osSocket.write(buff);
                this.osSocket.flush();
                TrafficEntity.addData(TrafficType.TRAFFICCARD, buff.length);
            }
        }
    }

    public byte[] recv(long milliSeconds) {
        byte[] buff = (byte[]) this.fifoQueueSocket.pop(milliSeconds);
        if (buff == null) {
            return null;
        }
        byte[] data = new byte[(buff.length - 9)];
        System.arraycopy(buff, 5, data, 0, buff.length - 9);
        return data;
    }
}
