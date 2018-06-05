package com.incomrecycle.prms.rvm.service.comm.entity;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.os.StrictMode.VmPolicy;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.comm.rvm.RVMRemotePkgUtils;
import com.incomrecycle.common.comm.rvm.RVMRemoteSession;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.utils.SocketUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.MsgType;
import com.incomrecycle.prms.rvm.service.comm.CommEntity;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;

public class RCCCommEntity implements CommEntity {
    private static boolean isDateUpdated = false;
    private static final Logger logger = LoggerFactory.getLogger("RCCCommEntity");
    byte[] des3Key;
    private long lLastTime;

    public static byte[] getEncryptKey() {
        String des3Key = null;
        File file = new File(SysConfig.get("DES3.KEY.FILE"));
        if (file.isFile()) {
            try {
                String line;
                DataInputStream dis = new DataInputStream(new FileInputStream(file));
                do {
                    line = dis.readLine();
                    if (line == null) {
                        break;
                    }
                    line = line.trim();
                } while (line.length() == 0);
                des3Key = line;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (des3Key == null) {
            des3Key = SysConfig.get("DES3.KEY");
        }
        if (des3Key != null) {
            des3Key = des3Key.trim();
            if (des3Key.length() == 0) {
                des3Key = null;
            }
        }
        if (des3Key != null) {
            return des3Key.getBytes();
        }
        return null;
    }

    public RCCCommEntity() {
        this.des3Key = null;
        this.lLastTime = 0;
        this.des3Key = getEncryptKey();
        StrictMode.setThreadPolicy(new Builder().detectDiskReads().detectDiskWrites().detectNetwork().detectAll().penaltyLog().permitAll().build());
        StrictMode.setVmPolicy(new VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
    }

    public void init() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String execute(String r24, String r25) throws Exception {
        /*
        r23 = this;
        r19 = "TESTING";
        r0 = r19;
        r1 = r24;
        r19 = r0.equals(r1);
        if (r19 == 0) goto L_0x0011;
    L_0x000c:
        r14 = r23.testing(r24, r25);
    L_0x0010:
        return r14;
    L_0x0011:
        r16 = 0;
        r19 = "SEND";
        r0 = r19;
        r1 = r24;
        r19 = r0.equals(r1);	 Catch:{ Exception -> 0x0139 }
        if (r19 == 0) goto L_0x0225;
    L_0x001f:
        r11 = java.lang.System.currentTimeMillis();	 Catch:{ Exception -> 0x0139 }
        monitor-enter(r23);	 Catch:{ Exception -> 0x0139 }
        r0 = r23;
        r0 = r0.lLastTime;	 Catch:{ all -> 0x0136 }
        r19 = r0;
        r21 = 0;
        r19 = (r19 > r21 ? 1 : (r19 == r21 ? 0 : -1));
        if (r19 <= 0) goto L_0x0053;
    L_0x0030:
        r19 = "RCC.COMM.INTERVAL";
        r19 = com.incomrecycle.common.SysConfig.get(r19);	 Catch:{ all -> 0x0136 }
        r9 = java.lang.Long.parseLong(r19);	 Catch:{ all -> 0x0136 }
        r0 = r23;
        r0 = r0.lLastTime;	 Catch:{ all -> 0x0136 }
        r19 = r0;
        r19 = r19 + r9;
        r19 = (r19 > r11 ? 1 : (r19 == r11 ? 0 : -1));
        if (r19 <= 0) goto L_0x0053;
    L_0x0046:
        r0 = r23;
        r0 = r0.lLastTime;	 Catch:{ all -> 0x0136 }
        r19 = r0;
        r19 = r19 + r9;
        r19 = r19 - r11;
        java.lang.Thread.sleep(r19);	 Catch:{ all -> 0x0136 }
    L_0x0053:
        r19 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0136 }
        r0 = r19;
        r2 = r23;
        r2.lLastTime = r0;	 Catch:{ all -> 0x0136 }
        monitor-exit(r23);	 Catch:{ all -> 0x0136 }
        r19 = logger;	 Catch:{ Exception -> 0x0139 }
        r20 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0139 }
        r20.<init>();	 Catch:{ Exception -> 0x0139 }
        r21 = "SEND:IP:";
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0139 }
        r21 = "RCC.IP";
        r21 = com.incomrecycle.common.SysConfig.get(r21);	 Catch:{ Exception -> 0x0139 }
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0139 }
        r21 = ";PORT:";
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0139 }
        r21 = "RCC.PORT";
        r21 = com.incomrecycle.common.SysConfig.get(r21);	 Catch:{ Exception -> 0x0139 }
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0139 }
        r21 = ";JSON:";
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0139 }
        r0 = r20;
        r1 = r25;
        r20 = r0.append(r1);	 Catch:{ Exception -> 0x0139 }
        r20 = r20.toString();	 Catch:{ Exception -> 0x0139 }
        r19.debug(r20);	 Catch:{ Exception -> 0x0139 }
        r17 = new com.incomrecycle.common.comm.rvm.RVMRemoteSession;	 Catch:{ Exception -> 0x0139 }
        r19 = "RCC.IP";
        r19 = com.incomrecycle.common.SysConfig.get(r19);	 Catch:{ Exception -> 0x0139 }
        r20 = "RCC.PORT";
        r20 = com.incomrecycle.common.SysConfig.get(r20);	 Catch:{ Exception -> 0x0139 }
        r20 = java.lang.Integer.parseInt(r20);	 Catch:{ Exception -> 0x0139 }
        r21 = "RCC.CONNECT.TIMEOUT";
        r21 = com.incomrecycle.common.SysConfig.get(r21);	 Catch:{ Exception -> 0x0139 }
        r21 = java.lang.Long.parseLong(r21);	 Catch:{ Exception -> 0x0139 }
        r0 = r17;
        r1 = r19;
        r2 = r20;
        r3 = r21;
        r0.<init>(r1, r2, r3);	 Catch:{ Exception -> 0x0139 }
        r19 = "RCC.TIMEOUT";
        r19 = com.incomrecycle.common.SysConfig.get(r19);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r19 = java.lang.Integer.parseInt(r19);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r0 = r17;
        r1 = r19;
        r0.setTimeout(r1);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r0 = r23;
        r0 = r0.des3Key;	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r19 = r0;
        r0 = r17;
        r1 = r19;
        r2 = r25;
        com.incomrecycle.common.comm.rvm.RVMRemotePkgUtils.send(r0, r1, r2);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r0 = r23;
        r0 = r0.des3Key;	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r19 = r0;
        r0 = r17;
        r1 = r19;
        r14 = com.incomrecycle.common.comm.rvm.RVMRemotePkgUtils.read(r0, r1);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        if (r14 != 0) goto L_0x0145;
    L_0x00f1:
        r19 = "NETWORK_FAILED";
        com.incomrecycle.prms.rvm.common.SysDef.networkSts.NETWORK_STS = r19;	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r19 = logger;	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r20 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r20.<init>();	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r21 = "RECV:IP:";
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r21 = "RCC.IP";
        r21 = com.incomrecycle.common.SysConfig.get(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r21 = ";PORT:";
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r21 = "RCC.PORT";
        r21 = com.incomrecycle.common.SysConfig.get(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r21 = ";JSON:";
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r0 = r20;
        r20 = r0.append(r14);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r20 = r20.toString();	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r19.debug(r20);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        if (r17 == 0) goto L_0x0010;
    L_0x0131:
        r17.disconnect();
        goto L_0x0010;
    L_0x0136:
        r19 = move-exception;
        monitor-exit(r23);	 Catch:{ all -> 0x0136 }
        throw r19;	 Catch:{ Exception -> 0x0139 }
    L_0x0139:
        r7 = move-exception;
    L_0x013a:
        r7.printStackTrace();	 Catch:{ all -> 0x022c }
        if (r16 == 0) goto L_0x0142;
    L_0x013f:
        r16.disconnect();
    L_0x0142:
        r14 = 0;
        goto L_0x0010;
    L_0x0145:
        r19 = "NETWORK_SUCCESS";
        com.incomrecycle.prms.rvm.common.SysDef.networkSts.NETWORK_STS = r19;	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r19 = logger;	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r20 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r20.<init>();	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r21 = "RECV:IP:";
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r21 = "RCC.IP";
        r21 = com.incomrecycle.common.SysConfig.get(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r21 = ";PORT:";
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r21 = "RCC.PORT";
        r21 = com.incomrecycle.common.SysConfig.get(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r21 = ";JSON:";
        r20 = r20.append(r21);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r0 = r20;
        r20 = r0.append(r14);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r20 = r20.toString();	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r19.debug(r20);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r19 = isDateUpdated;	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        if (r19 != 0) goto L_0x0212;
    L_0x0187:
        r19 = "RCC_TIME";
        r0 = r19;
        r19 = r14.indexOf(r0);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        if (r19 < 0) goto L_0x0212;
    L_0x0191:
        r19 = 1;
        isDateUpdated = r19;	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r15 = new java.util.Date;	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r15.<init>();	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r19 = "yyyy";
        r0 = r19;
        r19 = com.incomrecycle.common.utils.DateUtils.formatDatetime(r15, r0);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r18 = java.lang.Integer.parseInt(r19);	 Catch:{ Exception -> 0x0237, all -> 0x0233 }
        r8 = com.incomrecycle.common.json.JSONUtils.toHashMap(r14);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r19 = "RCC_TIME";
        r0 = r19;
        r5 = r8.get(r0);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r5 = (java.lang.String) r5;	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r19 = com.incomrecycle.common.utils.StringUtils.isBlank(r5);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        if (r19 != 0) goto L_0x0212;
    L_0x01ba:
        r13 = 0;
        r19 = ":";
        r0 = r19;
        r19 = r5.indexOf(r0);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r20 = -1;
        r0 = r19;
        r1 = r20;
        if (r0 == r1) goto L_0x0219;
    L_0x01cb:
        r13 = com.incomrecycle.common.utils.DateUtils.parseDatetime(r5);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
    L_0x01cf:
        r19 = 2015; // 0x7df float:2.824E-42 double:9.955E-321;
        r0 = r18;
        r1 = r19;
        if (r0 < r1) goto L_0x01ec;
    L_0x01d7:
        r19 = r13.getTime();	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r21 = r15.getTime();	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r19 = r19 - r21;
        r19 = java.lang.Math.abs(r19);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r21 = 600000; // 0x927c0 float:8.40779E-40 double:2.964394E-318;
        r19 = (r19 > r21 ? 1 : (r19 == r21 ? 0 : -1));
        if (r19 <= 0) goto L_0x0212;
    L_0x01ec:
        r19 = "yyyyMMdd.HHmmss";
        r0 = r19;
        r6 = com.incomrecycle.common.utils.DateUtils.formatDatetime(r13, r0);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r19 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r19.<init>();	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r20 = "date -s '";
        r19 = r19.append(r20);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r0 = r19;
        r19 = r0.append(r6);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r20 = "'";
        r19 = r19.append(r20);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r19 = r19.toString();	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        com.incomrecycle.common.utils.ShellUtils.shell(r19);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
    L_0x0212:
        if (r17 == 0) goto L_0x0010;
    L_0x0214:
        r17.disconnect();
        goto L_0x0010;
    L_0x0219:
        r13 = new java.util.Date;	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r19 = java.lang.Long.parseLong(r5);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        r0 = r19;
        r13.<init>(r0);	 Catch:{ Exception -> 0x023c, all -> 0x0233 }
        goto L_0x01cf;
    L_0x0225:
        if (r16 == 0) goto L_0x0142;
    L_0x0227:
        r16.disconnect();
        goto L_0x0142;
    L_0x022c:
        r19 = move-exception;
    L_0x022d:
        if (r16 == 0) goto L_0x0232;
    L_0x022f:
        r16.disconnect();
    L_0x0232:
        throw r19;
    L_0x0233:
        r19 = move-exception;
        r16 = r17;
        goto L_0x022d;
    L_0x0237:
        r7 = move-exception;
        r16 = r17;
        goto L_0x013a;
    L_0x023c:
        r19 = move-exception;
        goto L_0x0212;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.incomrecycle.prms.rvm.service.comm.entity.RCCCommEntity.execute(java.lang.String, java.lang.String):java.lang.String");
    }

    private String testing(String cmd, String json) throws Exception {
        Exception e;
        Double delayTime;
        Throwable th;
        HashMap hashMap = null;
        HashMap hsmpJSON = JSONUtils.toHashMap(json);
        String RCC_IP = (String) hsmpJSON.get("RCC_IP");
        String RCC_PORT = (String) hsmpJSON.get("RCC_PORT");
        String RVM_CODE = (String) hsmpJSON.get("RVM_CODE");
        String RVM_AREA_CODE = (String) hsmpJSON.get("RVM_AREA_CODE");
        String TIMEOUT = (String) hsmpJSON.get("TIMEOUT");
        long StartTime = System.currentTimeMillis();
        try {
            HashMap hsmpRetPkg;
            String ipAddress = SocketUtils.getIpAddress();
            String QuTime = Long.valueOf(new Date().getTime()).toString();
            String opBatchID = RVM_CODE + "_" + QuTime;
            HashMap hsmpPkg = new HashMap();
            hsmpPkg.put("MES_TYPE", MsgType.RVM_PING);
            hsmpPkg.put("TERM_NO", RVM_CODE);
            hsmpPkg.put("LOCAL_AREA_ID", RVM_AREA_CODE);
            hsmpPkg.put("QU_TIME", QuTime);
            hsmpPkg.put("IP_ADDR", ipAddress);
            hsmpPkg.put("OP_BATCH_ID", opBatchID);
            String retJson = JSONUtils.toJSON(hsmpPkg);
            String retPkg = null;
            String CONFIRM = null;
            RVMRemoteSession rVMRemoteSession = null;
            try {
                RVMRemoteSession rVMRemoteSession2 = new RVMRemoteSession(RCC_IP, Integer.parseInt(RCC_PORT), Long.parseLong(SysConfig.get("RCC.TIMEOUT")));
                if (TIMEOUT == null) {
                    try {
                        rVMRemoteSession2.setTimeout(Integer.parseInt(SysConfig.get("RCC.TIMEOUT")));
                    } catch (Exception e2) {
                        e = e2;
                        rVMRemoteSession = rVMRemoteSession2;
                        try {
                            e.printStackTrace();
                            if (rVMRemoteSession != null) {
                                rVMRemoteSession.disconnect();
                            }
                            hsmpRetPkg = new HashMap();
                            if (!StringUtils.isBlank(retPkg)) {
                                hsmpRetPkg.put("RESULT", "failed");
                            } else if ("1".equals(CONFIRM)) {
                                hsmpRetPkg.put("RESULT", "unknown");
                            } else {
                                hsmpRetPkg.put("RESULT", "success");
                            }
                            hashMap = hsmpRetPkg;
                            delayTime = Double.valueOf(((double) (System.currentTimeMillis() - StartTime)) * 0.001d);
                            if (hashMap == null) {
                                hashMap = new HashMap();
                                hashMap.put("RESULT", "failed");
                            }
                            hashMap.put("TIME", delayTime.toString());
                            return JSONUtils.toJSON(hashMap);
                        } catch (Throwable th2) {
                            if (rVMRemoteSession != null) {
                                rVMRemoteSession.disconnect();
                            }
                            throw th2;
                        }
                    } catch (Throwable th3) {
                        rVMRemoteSession = rVMRemoteSession2;
                        if (rVMRemoteSession != null) {
                            rVMRemoteSession.disconnect();
                        }
                        throw th3;
                    }
                }
                rVMRemoteSession2.setTimeout(Integer.parseInt(TIMEOUT));
                RVMRemotePkgUtils.send(rVMRemoteSession2, this.des3Key, retJson);
                retPkg = RVMRemotePkgUtils.read(rVMRemoteSession2, this.des3Key);
                if (retPkg != null) {
                    HashMap hsmpretPkg = JSONUtils.toHashMap(retPkg);
                    if (hsmpretPkg != null) {
                        CONFIRM = (String) hsmpretPkg.get("CONFIRM");
                    }
                }
                if (rVMRemoteSession2 != null) {
                    rVMRemoteSession2.disconnect();
                    rVMRemoteSession = rVMRemoteSession2;
                } else {
                    rVMRemoteSession = rVMRemoteSession2;
                }
            } catch (Exception e3) {
                e = e3;
                e.printStackTrace();
                if (rVMRemoteSession != null) {
                    rVMRemoteSession.disconnect();
                }
                hsmpRetPkg = new HashMap();
                if (!StringUtils.isBlank(retPkg)) {
                    hsmpRetPkg.put("RESULT", "failed");
                } else if ("1".equals(CONFIRM)) {
                    hsmpRetPkg.put("RESULT", "success");
                } else {
                    hsmpRetPkg.put("RESULT", "unknown");
                }
                hashMap = hsmpRetPkg;
                delayTime = Double.valueOf(((double) (System.currentTimeMillis() - StartTime)) * 0.001d);
                if (hashMap == null) {
                    hashMap = new HashMap();
                    hashMap.put("RESULT", "failed");
                }
                hashMap.put("TIME", delayTime.toString());
                return JSONUtils.toJSON(hashMap);
            }
            hsmpRetPkg = new HashMap();
            try {
                if (!StringUtils.isBlank(retPkg)) {
                    hsmpRetPkg.put("RESULT", "failed");
                } else if ("1".equals(CONFIRM)) {
                    hsmpRetPkg.put("RESULT", "success");
                } else {
                    hsmpRetPkg.put("RESULT", "unknown");
                }
                hashMap = hsmpRetPkg;
            } catch (Exception e4) {
                e = e4;
                hashMap = hsmpRetPkg;
                e.printStackTrace();
                delayTime = Double.valueOf(((double) (System.currentTimeMillis() - StartTime)) * 0.001d);
                if (hashMap == null) {
                    hashMap = new HashMap();
                    hashMap.put("RESULT", "failed");
                }
                hashMap.put("TIME", delayTime.toString());
                return JSONUtils.toJSON(hashMap);
            }
        } catch (Exception e5) {
            e = e5;
            e.printStackTrace();
            delayTime = Double.valueOf(((double) (System.currentTimeMillis() - StartTime)) * 0.001d);
            if (hashMap == null) {
                hashMap = new HashMap();
                hashMap.put("RESULT", "failed");
            }
            hashMap.put("TIME", delayTime.toString());
            return JSONUtils.toJSON(hashMap);
        }
        delayTime = Double.valueOf(((double) (System.currentTimeMillis() - StartTime)) * 0.001d);
        if (hashMap == null) {
            hashMap = new HashMap();
            hashMap.put("RESULT", "failed");
        }
        hashMap.put("TIME", delayTime.toString());
        return JSONUtils.toJSON(hashMap);
    }
}
