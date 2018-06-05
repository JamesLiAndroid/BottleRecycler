package com.incomrecycle.prms.rvm.service.comm.entity;

import android.support.v4.view.GravityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.EncryptUtils;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.updateDetection;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameDataFormat;
import com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameDataFormat.SerialFrameType;
import com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameDataFormat.SerialFrameType.TransType;
import com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameDataFormat.ServerFrameType;
import com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameFormat;
import com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameItemType;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TrafficCardCommEntity implements CommEntity {
    private static final int CARD_CHARGED_RECORD = 2;
    private static final int CARD_CONSUME_RECORD = 1;
    public static int LOGIN_FLAG_ERRORSEQ = 3;
    public static int LOGIN_FLAG_INIT = 0;
    public static int LOGIN_FLAG_NEEDDOWNLOAD = 1;
    public static int LOGIN_FLAG_NEEDLOGOUT = 2;
    public static int LOGIN_FLAG_NEEDUPDATESEQ = 3;
    private static final String PARAMFLAG = "PARAMFLAG:";
    private static final String RESULT_FAILED = "FAILED";
    private static final String RESULT_SUCCESS = "SUCCESS";
    private static long SERIAL_RES_MAX_TIME = 2000;
    private String ISAM;
    private int PsamTermNo;
    private int PsamTransNo;
    private int agentCode;
    private int batchNo;
    private int baud;
    private String commFile;
    private short dataSeq;
    private int hostSeq;
    private boolean initiated;
    private String ip;
    private boolean isInCheck;
    private List<HashMap> listCardType;
    private String mchntId;
    private String oprId;
    private String paramfile;
    private int port;
    private long posAccSeq;
    private long posCommSeq;
    private long posIcSeq;
    private String posId;
    private byte programName;
    private TrafficCardReaderEntity trafficCardReaderEntity;
    TrafficCardServerEntity trafficCardServerEntity;
    private String unitId;

    public static class ErrorCode {
        public static final String ERRCODE = "ERRCODE";
        public static final String MODEL_HOSTRESPONSECODE = "MODEL_HOSTRESPONSECODE";
        public static final String MODEL_RESPONSECODE = "MODEL_RESPONSECODE";
        public static final String SERVER_RESPONSE = "SERVER_RESPONSE";
    }

    public TrafficCardCommEntity() {
        this.paramfile = null;
        this.posId = "99999070018500000000";
        this.oprId = "000001";
        this.unitId = "75140048";
        this.mchntId = "003280000001";
        this.agentCode = 0;
        this.programName = (byte) 1;
        this.dataSeq = (short) 0;
        this.posIcSeq = 0;
        this.posAccSeq = 0;
        this.posCommSeq = 0;
        this.batchNo = 0;
        this.PsamTransNo = 0;
        this.PsamTermNo = 0;
        this.hostSeq = 0;
        this.ISAM = null;
        this.initiated = false;
        this.trafficCardReaderEntity = new TrafficCardReaderEntity();
        this.trafficCardServerEntity = new TrafficCardServerEntity();
        this.listCardType = new ArrayList();
        this.isInCheck = false;
        this.ip = SysConfig.get("SVR.ONECARDDRV.IP");
        this.port = Integer.parseInt(SysConfig.get("SVR.ONECARDDRV.PORT"));
        this.baud = Integer.parseInt(SysConfig.get("COM.ONECARDDRV.BAND"));
        this.commFile = SysConfig.get("COM.ONECARDDRV." + SysConfig.get("PLATFORM"));
        this.paramfile = SysConfig.get("SVR.ONECARDDEV.PARAM.FILE");
    }

    public void init() {
        this.trafficCardReaderEntity.init(this.commFile, this.baud);
        if (!this.initiated) {
            HashMap hsmpResult = readModel();
            if (hsmpResult == null) {
                SysConfig.set("RVM.ONECARD.DRV.VERSION", "0");
            } else if ("FAILED".equalsIgnoreCase((String) hsmpResult.get("RESULT")) && "MODEL_UNKNOWN".equalsIgnoreCase((String) hsmpResult.get(ErrorCode.ERRCODE))) {
                SysConfig.set("RVM.ONECARD.DRV.VERSION", "0");
            } else {
                SysConfig.set("RVM.ONECARD.DRV.VERSION", "1");
                SysGlobal.execute(new Runnable() {
                    public void run() {
                        TrafficCardCommEntity.this.loadParam(TrafficCardCommEntity.this.paramfile);
                    }
                });
                HashMap<String, String> hsmpParam;
                if ("SUCCESS".equalsIgnoreCase((String) hsmpResult.get("RESULT"))) {
                    hsmpParam = new HashMap();
                    hsmpParam.put("type", "ONECARDDRV_ERROR_RECOVERY");
                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                } else {
                    hsmpParam = new HashMap();
                    hsmpParam.put("type", "ONECARDDRV_ERROR");
                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                }
            }
            this.initiated = true;
        }
    }

    public void done() {
        this.trafficCardReaderEntity.done();
    }

    public String execute(String cmd, String json) throws Exception {
        init();
        if ("CHECK:START".equalsIgnoreCase(cmd)) {
            this.isInCheck = true;
        }
        if ("CHECK:END".equalsIgnoreCase(cmd)) {
            this.isInCheck = false;
        }
        if ("0".equals(SysConfig.get("RVM.ONECARD.DRV.VERSION"))) {
            return null;
        }
        HashMap hsmpModel;
        if (!this.isInCheck) {
            synchronized (this) {
                HashMap hsmpParam;
                String CardNo;
                int Balance;
                String Datetime;
                HashMap hsmpLogin;
                HashMap hsmpResult;
                String toJSON;
                if ("Charge".equalsIgnoreCase(cmd)) {
                    hsmpModel = readModel();
                    if ("SUCCESS".equalsIgnoreCase((String) hsmpModel.get("RESULT"))) {
                        hsmpParam = JSONUtils.toHashMap(json);
                        CardNo = (String) hsmpParam.get("CardNo");
                        Balance = Integer.parseInt((String) hsmpParam.get("Balance"));
                        Datetime = (String) hsmpParam.get("Datetime");
                        hsmpLogin = login(LOGIN_FLAG_INIT);
                        if ("SUCCESS".equalsIgnoreCase((String) hsmpLogin.get("RESULT"))) {
                            hsmpResult = charge(CardNo, Balance, Datetime);
                            logout((String) hsmpModel.get("ISAM"));
                            toJSON = JSONUtils.toJSON(hsmpResult);
                            return toJSON;
                        }
                        toJSON = JSONUtils.toJSON(hsmpLogin);
                        return toJSON;
                    }
                    toJSON = JSONUtils.toJSON(hsmpModel);
                    return toJSON;
                } else if ("ChargeWriteBack".equalsIgnoreCase(cmd)) {
                    hsmpModel = readModel();
                    if ("SUCCESS".equalsIgnoreCase((String) hsmpModel.get("RESULT"))) {
                        hsmpParam = JSONUtils.toHashMap(json);
                        CardNo = (String) hsmpParam.get("CardNo");
                        String UserAcctPassword = (String) hsmpParam.get("UserAcctPassword");
                        Balance = Integer.parseInt((String) hsmpParam.get("Balance"));
                        Datetime = (String) hsmpParam.get("Datetime");
                        hsmpLogin = login(LOGIN_FLAG_INIT);
                        if ("SUCCESS".equalsIgnoreCase((String) hsmpLogin.get("RESULT"))) {
                            hsmpResult = chargeWriteBack(CardNo, Balance, UserAcctPassword, Datetime);
                            logout((String) hsmpModel.get("ISAM"));
                            toJSON = JSONUtils.toJSON(hsmpResult);
                            return toJSON;
                        }
                        toJSON = JSONUtils.toJSON(hsmpLogin);
                        return toJSON;
                    }
                    toJSON = JSONUtils.toJSON(hsmpModel);
                    return toJSON;
                } else if ("QueryQuickCard".equalsIgnoreCase(cmd)) {
                    hsmpModel = readModel();
                    if ("SUCCESS".equalsIgnoreCase((String) hsmpModel.get("RESULT"))) {
                        CardNo = (String) JSONUtils.toHashMap(json).get("CardNo");
                        hsmpLogin = login(LOGIN_FLAG_INIT);
                        if ("SUCCESS".equalsIgnoreCase((String) hsmpLogin.get("RESULT"))) {
                            hsmpResult = queryQuickCard(CardNo);
                            logout((String) hsmpModel.get("ISAM"));
                            toJSON = JSONUtils.toJSON(hsmpResult);
                            return toJSON;
                        }
                        toJSON = JSONUtils.toJSON(hsmpLogin);
                        return toJSON;
                    }
                    toJSON = JSONUtils.toJSON(hsmpModel);
                    return toJSON;
                } else if ("ChargeQuickCard".equalsIgnoreCase(cmd)) {
                    hsmpModel = readModel();
                    if ("SUCCESS".equalsIgnoreCase((String) hsmpModel.get("RESULT"))) {
                        hsmpParam = JSONUtils.toHashMap(json);
                        CardNo = (String) hsmpParam.get("CardNo");
                        String OrderNo = (String) hsmpParam.get("OrderNo");
                        String Balance2 = (String) hsmpParam.get("Balance");
                        Datetime = (String) hsmpParam.get("Datetime");
                        HashMap hsmpOrder = new HashMap();
                        hsmpOrder.put("OrderNo", OrderNo);
                        hsmpOrder.put("OrderSaveMnt", Balance2);
                        hsmpLogin = login(LOGIN_FLAG_INIT);
                        if ("SUCCESS".equalsIgnoreCase((String) hsmpLogin.get("RESULT"))) {
                            hsmpResult = chargeQuickCard(CardNo, hsmpOrder, Datetime);
                            logout((String) hsmpModel.get("ISAM"));
                            toJSON = JSONUtils.toJSON(hsmpResult);
                            return toJSON;
                        }
                        toJSON = JSONUtils.toJSON(hsmpLogin);
                        return toJSON;
                    }
                    toJSON = JSONUtils.toJSON(hsmpModel);
                    return toJSON;
                } else if ("ChargeScrashCard".equalsIgnoreCase(cmd)) {
                    hsmpModel = readModel();
                    if ("SUCCESS".equalsIgnoreCase((String) hsmpModel.get("RESULT"))) {
                        hsmpParam = JSONUtils.toHashMap(json);
                        CardNo = (String) hsmpParam.get("CardNo");
                        String CardPwd = (String) hsmpParam.get("CardPwd");
                        Balance = Integer.parseInt((String) hsmpParam.get("Balance"));
                        Datetime = (String) hsmpParam.get("Datetime");
                        hsmpLogin = login(LOGIN_FLAG_INIT);
                        if ("SUCCESS".equalsIgnoreCase((String) hsmpLogin.get("RESULT"))) {
                            hsmpResult = chargeScrashCard(CardNo, Balance, CardPwd, Datetime);
                            logout((String) hsmpModel.get("ISAM"));
                            toJSON = JSONUtils.toJSON(hsmpResult);
                            return toJSON;
                        }
                        toJSON = JSONUtils.toJSON(hsmpLogin);
                        return toJSON;
                    }
                    toJSON = JSONUtils.toJSON(hsmpModel);
                    return toJSON;
                } else if ("ReadCardNo".equalsIgnoreCase(cmd)) {
                    toJSON = JSONUtils.toJSON(readCard(0));
                    return toJSON;
                } else if ("ReadCard".equalsIgnoreCase(cmd)) {
                    toJSON = JSONUtils.toJSON(readCard(3));
                    return toJSON;
                }
            }
        } else if ("TRAFFICCARD:CHECK_OPEN".equalsIgnoreCase(cmd)) {
            if (!this.trafficCardReaderEntity.isOpen()) {
                return "FALSE";
            }
            hsmpModel = readModel();
            if (hsmpModel == null) {
                return "FALSE";
            }
            if ("SUCCESS".equalsIgnoreCase((String) hsmpModel.get("RESULT"))) {
                return "TRUE";
            }
            return "FALSE";
        } else if ("TRAFFICCARD:Init".equalsIgnoreCase(cmd)) {
            hsmpModel = readModel();
            if (hsmpModel == null) {
                return null;
            }
            return JSONUtils.toJSON(hsmpModel);
        } else if ("TRAFFICCARD:ReadCard".equalsIgnoreCase(cmd)) {
            return JSONUtils.toJSON(readCard(0));
        }
        return null;
    }

    private boolean loadParamFromFile(String paramfile) {
        boolean isLoaded = false;
        File file = new File(paramfile);
        if (file.isFile() && file.length() > 0) {
            InputStream is = null;
            DataInputStream dis = null;
            try {
                InputStream fileInputStream = new FileInputStream(paramfile);
                try {
                    DataInputStream dis2 = new DataInputStream(fileInputStream);
                    try {
                        this.listCardType.clear();
                        int paramFlag = 0;
                        int paramCount = 0;
                        while (true) {
                            String line = dis2.readLine();
                            if (line == null) {
                                break;
                            } else if (!StringUtils.isBlank(line)) {
                                if (line.startsWith(PARAMFLAG)) {
                                    String[] p = line.substring(PARAMFLAG.length()).split(",");
                                    paramFlag = Integer.parseInt(p[0], 16);
                                    paramCount = Integer.parseInt(p[1]);
                                } else if (paramCount > 0) {
                                    int pos;
                                    int i;
                                    isLoaded = true;
                                    byte[] paramData = EncryptUtils.hex2byte(line);
                                    int paramSize = paramData.length / paramCount;
                                    if ((ExploreByTouchHelper.INVALID_ID & paramFlag) != 0) {
                                    }
                                    if ((1073741824 & paramFlag) != 0) {
                                        FrameFormat frameFormatCardType = FrameDataFormat.getFrameFormat("CARD_TYPE");
                                        if (paramSize == frameFormatCardType.getFrameLen()) {
                                            pos = 0;
                                            for (i = 0; i < paramCount; i++) {
                                                String PhyCardType = FrameItemType.getHEX2String(paramData, frameFormatCardType.getPos("PhyCardType") + pos, frameFormatCardType.getLen("PhyCardType"));
                                                String CardType = FrameItemType.getHEX2String(paramData, frameFormatCardType.getPos("CardType") + pos, frameFormatCardType.getLen("CardType"));
                                                String CardTypeName = FrameItemType.getString(paramData, frameFormatCardType.getPos("CardTypeName") + pos, frameFormatCardType.getLen("CardTypeName"));
                                                int MinChargeAmount = (int) FrameItemType.getHEX2Long(paramData, frameFormatCardType.getPos("MinChargeAmount") + pos, frameFormatCardType.getLen("MinChargeAmount"));
                                                int ChargeAmountUnit = (int) FrameItemType.getHEX2Long(paramData, frameFormatCardType.getPos("ChargeAmountUnit") + pos, frameFormatCardType.getLen("ChargeAmountUnit"));
                                                int MaxBalance = (int) FrameItemType.getHEX2Long(paramData, frameFormatCardType.getPos("MaxBalance") + pos, frameFormatCardType.getLen("MaxBalance"));
                                                int MaxChargeAmount = (int) FrameItemType.getHEX2Long(paramData, frameFormatCardType.getPos("MaxChargeAmount") + pos, frameFormatCardType.getLen("MaxChargeAmount"));
                                                int EndDays = (int) FrameItemType.getHEX2Long(paramData, frameFormatCardType.getPos("EndDays") + pos, frameFormatCardType.getLen("EndDays"));
                                                HashMap hsmpCardType = new HashMap();
                                                hsmpCardType.put("PhyCardType", "" + PhyCardType);
                                                hsmpCardType.put("CardType", "" + CardType);
                                                hsmpCardType.put("CardTypeName", "" + CardTypeName);
                                                hsmpCardType.put("MinChargeAmount", "" + MinChargeAmount);
                                                hsmpCardType.put("MaxChargeAmount", "" + MaxChargeAmount);
                                                hsmpCardType.put("ChargeAmountUnit", "" + ChargeAmountUnit);
                                                hsmpCardType.put("MaxBalance", "" + MaxBalance);
                                                hsmpCardType.put("EndDays", "" + EndDays);
                                                this.listCardType.add(hsmpCardType);
                                                pos += paramSize;
                                            }
                                        }
                                    }
                                    if ((134217728 & paramFlag) != 0) {
                                        FrameFormat frameFormatCommSettings = FrameDataFormat.getFrameFormat("CommSettings");
                                        if (paramSize == frameFormatCommSettings.getFrameLen()) {
                                            pos = 0;
                                            for (i = 0; i < paramCount; i++) {
                                                String ServIp1 = FrameItemType.getString(paramData, frameFormatCommSettings.getPos("ServIp1") + pos, frameFormatCommSettings.getLen("ServIp1"));
                                                int ServPort1 = (int) FrameItemType.getHEX2Long(paramData, frameFormatCommSettings.getPos("ServPort1") + pos, frameFormatCommSettings.getLen("ServPort1"));
                                                pos += paramSize;
                                            }
                                        }
                                    }
                                    if ((67108864 & paramFlag) != 0) {
                                        FrameFormat frameFormatBusinessParam = FrameDataFormat.getFrameFormat("BUSINESS_PARAM");
                                        if (paramSize == frameFormatBusinessParam.getFrameLen()) {
                                            pos = 0;
                                            for (i = 0; i < paramCount; i++) {
                                                this.agentCode = (int) FrameItemType.getHEX2Long(paramData, frameFormatBusinessParam.getPos("AGENT_CODE") + pos, frameFormatBusinessParam.getLen("AGENT_CODE"));
                                                pos += paramSize;
                                            }
                                        }
                                    }
                                    if ((16777216 & paramFlag) != 0) {
                                    }
                                    if ((GravityCompat.RELATIVE_LAYOUT_DIRECTION & paramFlag) != 0) {
                                    }
                                    if ((AccessibilityEventCompat.TYPE_TOUCH_INTERACTION_START & paramFlag) != 0) {
                                    }
                                }
                            }
                        }
                        dis = dis2;
                        is = fileInputStream;
                    } catch (Exception e) {
                        dis = dis2;
                        is = fileInputStream;
                    }
                } catch (Exception e2) {
                    is = fileInputStream;
                }
            } catch (Exception e3) {
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (Exception e4) {
                }
            }
            IOUtils.close(is);
        }
        if (this.listCardType.size() == 0) {
            return false;
        }
        return isLoaded;
    }

    public void loadParam(String paramfile) {
        this.paramfile = paramfile;
        if (!loadParamFromFile(paramfile)) {
            downloadParam(paramfile, 0);
            loadParamFromFile(paramfile);
        }
    }

    public HashMap downloadParam(String paramfile, int chkmode) {
        HashMap hsmpModel = readModel();
        if (hsmpModel == null) {
            return null;
        }
        String ISAM = (String) hsmpModel.get("ISAM");
        HashMap hsmpResult = new HashMap();
        OutputStream fos = null;
        try {
            if (this.trafficCardServerEntity.connect(this.ip, this.port)) {
                File file = new File(paramfile);
                if (!(file.exists() || file.getParentFile().exists())) {
                    file.getParentFile().mkdirs();
                }
                if (chkmode == 0) {
                    fos = new FileOutputStream(paramfile, false);
                } else {
                    fos = new FileOutputStream(paramfile, true);
                }
                FrameFormat frameFormatServerParamQueryReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.PARAM_QUERY_REQ, 2);
                byte[] dataServerParamQueryReq = new byte[frameFormatServerParamQueryReq.getFrameLen()];
                FrameItemType.setLong2BCD(dataServerParamQueryReq, frameFormatServerParamQueryReq.getPos("MessageType"), frameFormatServerParamQueryReq.getLen("MessageType"), 5033);
                FrameItemType.setByte(dataServerParamQueryReq, frameFormatServerParamQueryReq.getPos("Ver"), (byte) 2);
                FrameItemType.setString2BCD(dataServerParamQueryReq, frameFormatServerParamQueryReq.getPos("SysDatetime"), frameFormatServerParamQueryReq.getLen("SysDatetime"), DateUtils.formatDatetime(new Date(), "yyyyMMddHHmmss"));
                FrameItemType.setString(dataServerParamQueryReq, frameFormatServerParamQueryReq.getPos("POSID"), frameFormatServerParamQueryReq.getLen("POSID"), this.posId);
                FrameItemType.setString2BCD(dataServerParamQueryReq, frameFormatServerParamQueryReq.getPos("oprId"), frameFormatServerParamQueryReq.getLen("oprId"), this.oprId);
                FrameItemType.setString2BCD(dataServerParamQueryReq, frameFormatServerParamQueryReq.getPos("ISAM"), frameFormatServerParamQueryReq.getLen("ISAM"), ISAM);
                FrameItemType.setString2BCD(dataServerParamQueryReq, frameFormatServerParamQueryReq.getPos("UnitId"), frameFormatServerParamQueryReq.getLen("UnitId"), this.unitId);
                FrameItemType.setString2BCD(dataServerParamQueryReq, frameFormatServerParamQueryReq.getPos("MchntId"), frameFormatServerParamQueryReq.getLen("MchntId"), this.mchntId);
                FrameItemType.setByte(dataServerParamQueryReq, frameFormatServerParamQueryReq.getPos("ChkMode"), (byte) chkmode);
                FrameItemType.setLong2HEX(dataServerParamQueryReq, frameFormatServerParamQueryReq.getPos("Reserved"), frameFormatServerParamQueryReq.getLen("Reserved"), 0);
                System.out.println("SERVER SEND:" + frameFormatServerParamQueryReq.dump(dataServerParamQueryReq));
                this.trafficCardServerEntity.send(this.dataSeq, dataServerParamQueryReq, 0, dataServerParamQueryReq.length);
                byte[] dataServerParamQueryRes = this.trafficCardServerEntity.recv(10000);
                if (dataServerParamQueryRes == null) {
                    hsmpResult.put("RESULT", "SERVER_NO_RESPONSE");
                    fos = IOUtils.close(fos);
                    this.trafficCardServerEntity.disconnect();
                    return hsmpResult;
                }
                FrameFormat frameFormatServerParamQueryRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.PARAM_QUERY_RES, 2);
                System.out.println("SERVER RECV:" + frameFormatServerParamQueryRes.dump(dataServerParamQueryRes, 0, dataServerParamQueryRes.length));
                if (((int) FrameItemType.getBCD2Long(dataServerParamQueryRes, frameFormatServerParamQueryRes.getPos("MessageType"), frameFormatServerParamQueryRes.getLen("MessageType"))) != 5034) {
                    hsmpResult.put("RESULT", "SERVER_RESPONSE_NOTMATCH");
                    fos = IOUtils.close(fos);
                    this.trafficCardServerEntity.disconnect();
                    return hsmpResult;
                }
                byte bResponsecode = FrameItemType.getByte(dataServerParamQueryRes, frameFormatServerParamQueryRes.getPos("ResponseCode"));
                if (bResponsecode != (byte) 0) {
                    hsmpResult.put(ErrorCode.SERVER_RESPONSE, Integer.toHexString(bResponsecode));
                    hsmpResult.put("RESULT", "SERVER_PARAMQUERY_FAIL");
                    fos = IOUtils.close(fos);
                    this.trafficCardServerEntity.disconnect();
                    return hsmpResult;
                }
                int paramFlag = FrameItemType.getBIT(dataServerParamQueryRes, frameFormatServerParamQueryRes.getPos("ParamFlag"), frameFormatServerParamQueryRes.getLen("ParamFlag"));
                int[] paramMask = new int[32];
                for (int i = 0; i < paramMask.length; i++) {
                    paramMask[i] = 1 << i;
                }
                for (int maskIdx = 0; maskIdx < paramMask.length; maskIdx++) {
                    if ((paramMask[maskIdx] & paramFlag) != 0) {
                        FrameFormat frameFormatServerParamDownloadReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.PARAM_DOWNLOAD_REQ, 1);
                        byte[] dataServerParamDownloadReq = new byte[frameFormatServerParamDownloadReq.getFrameLen()];
                        FrameItemType.setLong2BCD(dataServerParamDownloadReq, frameFormatServerParamDownloadReq.getPos("MessageType"), frameFormatServerParamDownloadReq.getLen("MessageType"), 5035);
                        FrameItemType.setByte(dataServerParamDownloadReq, frameFormatServerParamDownloadReq.getPos("Ver"), (byte) 1);
                        FrameItemType.setString2BCD(dataServerParamDownloadReq, frameFormatServerParamDownloadReq.getPos("SysDatetime"), frameFormatServerParamDownloadReq.getLen("SysDatetime"), DateUtils.formatDatetime(new Date(), "yyyyMMddHHmmss"));
                        FrameItemType.setString(dataServerParamDownloadReq, frameFormatServerParamDownloadReq.getPos("POSID"), frameFormatServerParamDownloadReq.getLen("POSID"), this.posId);
                        FrameItemType.setString2BCD(dataServerParamDownloadReq, frameFormatServerParamDownloadReq.getPos("ISAM"), frameFormatServerParamDownloadReq.getLen("ISAM"), ISAM);
                        FrameItemType.setBIT(dataServerParamDownloadReq, frameFormatServerParamDownloadReq.getPos("ParamFlag"), frameFormatServerParamDownloadReq.getLen("ParamFlag"), paramMask[maskIdx]);
                        System.out.println("SERVER SEND" + frameFormatServerParamDownloadReq.dump(dataServerParamDownloadReq));
                        this.trafficCardServerEntity.send(this.dataSeq, dataServerParamDownloadReq, 0, dataServerParamDownloadReq.length);
                        byte[] dataServerParamDownloadRes = this.trafficCardServerEntity.recv(10000);
                        FrameFormat frameFormatServerParamDownloadRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.PARAM_DOWNLOAD_RES, 1);
                        System.out.println("SERVER RECV" + frameFormatServerParamDownloadRes.dump(dataServerParamDownloadRes));
                        int recordNum = (int) FrameItemType.getHEX2Long(dataServerParamDownloadRes, frameFormatServerParamDownloadRes.getPos("RecordNum"), frameFormatServerParamDownloadRes.getLen("RecordNum"));
                        bResponsecode = FrameItemType.getByte(dataServerParamDownloadRes, frameFormatServerParamDownloadRes.getPos("ResponseCode"));
                        String validDate = FrameItemType.getBCD2String(dataServerParamDownloadRes, frameFormatServerParamDownloadRes.getPos("ValidDate"), frameFormatServerParamDownloadRes.getLen("ValidDate"));
                        if (bResponsecode == (byte) 0 && recordNum > 0) {
                            fos.write((PARAMFLAG + Integer.toString(paramMask[maskIdx], 16) + "," + recordNum + "\n").getBytes());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            boolean isEnd = false;
                            while (true) {
                                FrameFormat frameFormatServerDataTransferReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.DATA_TRANSFER_REQ, 1);
                                byte[] dataServerDataTransferReq = new byte[frameFormatServerDataTransferReq.getFrameLen()];
                                FrameItemType.setLong2BCD(dataServerDataTransferReq, frameFormatServerDataTransferReq.getPos("MessageType"), frameFormatServerParamDownloadReq.getLen("MessageType"), 1000);
                                FrameItemType.setByte(dataServerDataTransferReq, frameFormatServerDataTransferReq.getPos("Ver"), (byte) 1);
                                FrameItemType.setByte(dataServerDataTransferReq, frameFormatServerDataTransferReq.getPos("ResponseCode"), bResponsecode);
                                System.out.println("PARAM REQ:" + frameFormatServerDataTransferReq.dump(dataServerDataTransferReq));
                                this.trafficCardServerEntity.send(this.dataSeq, dataServerDataTransferReq, 0, dataServerDataTransferReq.length);
                                if (isEnd) {
                                    break;
                                }
                                byte[] dataServerDataTransferRes = this.trafficCardServerEntity.recv(5000);
                                FrameFormat frameFormatServerDataTransferRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.DATA_TRANSFER_RES, 1);
                                byte PackFlag = FrameItemType.getByte(dataServerDataTransferRes, frameFormatServerDataTransferRes.getPos("PackFlag"));
                                int dataBlockLen = dataServerDataTransferRes.length - frameFormatServerDataTransferRes.getPos("DataBlock");
                                frameFormatServerDataTransferRes.setLen("DataBlock", dataBlockLen);
                                System.out.println("PARAM RES:" + frameFormatServerDataTransferRes.dump(dataServerDataTransferRes));
                                baos.write(dataServerDataTransferRes, frameFormatServerDataTransferRes.getPos("DataBlock"), dataBlockLen);
                                isEnd = PackFlag != (byte) 0;
                            }
                            fos.write(EncryptUtils.byte2hex(baos.toByteArray()).getBytes());
                            fos.write("\n\n".getBytes());
                        }
                    }
                }
                fos = IOUtils.close(IOUtils.close(fos));
                this.trafficCardServerEntity.disconnect();
                hsmpResult.put("RESULT", "SUCCESS");
                return null;
            }
            hsmpResult.put("RESULT", "SERVER_DISCONNECT");
            return hsmpResult;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fos = IOUtils.close(fos);
            this.trafficCardServerEntity.disconnect();
        }
        return null;
    }

    public void resetSeq() {
        this.posIcSeq = 0;
        this.posAccSeq = 0;
        this.posCommSeq = 0;
    }

    public HashMap login(int flag) {
        boolean reload = false;
        HashMap hsmpResult = new HashMap();
        try {
            FrameFormat frameFormatSerialLogin = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.LOGIN);
            byte[] dataSerialLogin = new byte[frameFormatSerialLogin.getFrameLen()];
            FrameItemType.setString(dataSerialLogin, frameFormatSerialLogin.getPos("Messagetype"), SerialFrameType.LOGIN);
            this.trafficCardReaderEntity.send(dataSerialLogin, 0, dataSerialLogin.length);
            byte[] dataSerialLoginRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
            if (dataSerialLoginRes == null) {
                hsmpResult.put(ErrorCode.ERRCODE, "MODEL_NORESPONSE");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "516");
                return hsmpResult;
            }
            FrameFormat frameFormatSerialLoginRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.LOGIN, null);
            String responseCode = FrameItemType.getString(dataSerialLoginRes, frameFormatSerialLoginRes.getPos("responseCode"), frameFormatSerialLoginRes.getLen("responseCode"));
            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                String ISAM = FrameItemType.getString(dataSerialLoginRes, frameFormatSerialLoginRes.getPos("ISAM"), frameFormatSerialLoginRes.getLen("ISAM"));
                String RANDOM = FrameItemType.getString(dataSerialLoginRes, frameFormatSerialLoginRes.getPos("RANDOM"), frameFormatSerialLoginRes.getLen("RANDOM"));
                hsmpResult.put("ISAM", ISAM);
                hsmpResult.put("RANDOM", RANDOM);
                if (this.trafficCardServerEntity.connect(this.ip, this.port)) {
                    FrameFormat frameFormatServerLoginReq = FrameDataFormat.getServerFrameFormat(5001, 2);
                    byte[] dataServerLoginReq = new byte[frameFormatServerLoginReq.getFrameLen()];
                    FrameItemType.setLong2BCD(dataServerLoginReq, frameFormatServerLoginReq.getPos("MessageType"), frameFormatServerLoginReq.getLen("MessageType"), 5001);
                    FrameItemType.setByte(dataServerLoginReq, frameFormatServerLoginReq.getPos("Ver"), (byte) 2);
                    FrameItemType.setString2BCD(dataServerLoginReq, frameFormatServerLoginReq.getPos("SysDatetime"), frameFormatServerLoginReq.getLen("SysDatetime"), DateUtils.formatDatetime(new Date(), "yyyyMMddHHmmss"));
                    FrameItemType.setString2BCD(dataServerLoginReq, frameFormatServerLoginReq.getPos("oprId"), frameFormatServerLoginReq.getLen("oprId"), this.oprId);
                    FrameItemType.setString(dataServerLoginReq, frameFormatServerLoginReq.getPos("PosId"), frameFormatServerLoginReq.getLen("PosId"), this.posId);
                    FrameItemType.setString2BCD(dataServerLoginReq, frameFormatServerLoginReq.getPos("ISAM"), frameFormatServerLoginReq.getLen("ISAM"), ISAM);
                    FrameItemType.setString2BCD(dataServerLoginReq, frameFormatServerLoginReq.getPos("UnitId"), frameFormatServerLoginReq.getLen("UnitId"), this.unitId);
                    FrameItemType.setString2BCD(dataServerLoginReq, frameFormatServerLoginReq.getPos("MchntId"), frameFormatServerLoginReq.getLen("MchntId"), this.mchntId);
                    FrameItemType.setByte(dataServerLoginReq, frameFormatServerLoginReq.getPos("ProgramName"), this.programName);
                    FrameItemType.setString2BCD(dataServerLoginReq, frameFormatServerLoginReq.getPos("RANDOM"), frameFormatServerLoginReq.getLen("RANDOM"), RANDOM);
                    FrameItemType.setLong2HEX(dataServerLoginReq, frameFormatServerLoginReq.getPos("posIcSeq"), frameFormatServerLoginReq.getLen("posIcSeq"), this.posIcSeq);
                    FrameItemType.setLong2HEX(dataServerLoginReq, frameFormatServerLoginReq.getPos("posAccSeq"), frameFormatServerLoginReq.getLen("posAccSeq"), this.posAccSeq);
                    FrameItemType.setLong2HEX(dataServerLoginReq, frameFormatServerLoginReq.getPos("posCommSeq"), frameFormatServerLoginReq.getLen("posCommSeq"), this.posCommSeq);
                    System.out.println("SERVER:\n" + frameFormatServerLoginReq.dump(dataServerLoginReq, 0, dataServerLoginReq.length));
                    this.trafficCardServerEntity.send(this.dataSeq, dataServerLoginReq, 0, dataServerLoginReq.length);
                    byte[] dataServerLoginRes = this.trafficCardServerEntity.recv(10000);
                    if (dataServerLoginRes == null) {
                        hsmpResult.put(ErrorCode.ERRCODE, "SERVER_NORESPONSE");
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "563");
                        this.trafficCardServerEntity.disconnect();
                        return hsmpResult;
                    }
                    FrameFormat frameFormatServerLoginRes = FrameDataFormat.getServerFrameFormat(5002, 2);
                    System.out.println("SERVER:\n" + frameFormatServerLoginRes.dump(dataServerLoginRes, 0, dataServerLoginRes.length));
                    if (((int) FrameItemType.getBCD2Long(dataServerLoginRes, frameFormatServerLoginRes.getPos("MessageType"), frameFormatServerLoginRes.getLen("MessageType"))) != 5002) {
                        hsmpResult.put(ErrorCode.ERRCODE, "SERVER_COMMERROR");
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "572");
                        this.trafficCardServerEntity.disconnect();
                        return hsmpResult;
                    }
                    if (dataServerLoginRes.length != frameFormatServerLoginRes.getFrameLen()) {
                        hsmpResult.put(ErrorCode.ERRCODE, "SERVER_COMMERROR");
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "578");
                        this.trafficCardServerEntity.disconnect();
                        return hsmpResult;
                    }
                    byte bResponsecode = FrameItemType.getByte(dataServerLoginRes, frameFormatServerLoginRes.getPos("ResponseCode"));
                    if (bResponsecode == (byte) 42) {
                        this.trafficCardServerEntity.disconnect();
                        this.trafficCardServerEntity.connect(this.ip, this.port);
                        logout(ISAM);
                        if (flag == LOGIN_FLAG_NEEDLOGOUT) {
                            hsmpResult.put(ErrorCode.SERVER_RESPONSE, Integer.toHexString(bResponsecode));
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "590");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        reload = true;
                        flag = LOGIN_FLAG_NEEDLOGOUT;
                    } else if (bResponsecode == (byte) 16) {
                        this.trafficCardServerEntity.disconnect();
                        byte newProgramName = FrameItemType.getByte(dataServerLoginRes, frameFormatServerLoginRes.getPos("ProgramName"));
                        if (newProgramName == this.programName) {
                            hsmpResult.put(ErrorCode.SERVER_RESPONSE, Integer.toHexString(bResponsecode));
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "602");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        this.programName = newProgramName;
                        reload = true;
                    } else if (bResponsecode == (byte) 4) {
                        hsmpResult.put(ErrorCode.SERVER_RESPONSE, Integer.toHexString(bResponsecode));
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "611");
                        this.trafficCardServerEntity.disconnect();
                        return hsmpResult;
                    } else if (bResponsecode == (byte) 5) {
                        hsmpResult.put(ErrorCode.SERVER_RESPONSE, Integer.toHexString(bResponsecode));
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "617");
                        this.trafficCardServerEntity.disconnect();
                        return hsmpResult;
                    } else if (bResponsecode == (byte) 40) {
                        if ((this.posIcSeq == 0 && this.posAccSeq == 0 && this.posCommSeq == 0) || flag == LOGIN_FLAG_NEEDUPDATESEQ) {
                            hsmpResult.put(ErrorCode.SERVER_RESPONSE, Integer.toHexString(bResponsecode));
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "624");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        long newposIcSeq = FrameItemType.getHEX2Long(dataServerLoginRes, frameFormatServerLoginRes.getPos("posIcSeq"), frameFormatServerLoginRes.getLen("posIcSeq"));
                        long newposAccSeq = FrameItemType.getHEX2Long(dataServerLoginRes, frameFormatServerLoginRes.getPos("PosAccSeq"), frameFormatServerLoginRes.getLen("PosAccSeq"));
                        long newposCommSeq = FrameItemType.getHEX2Long(dataServerLoginRes, frameFormatServerLoginRes.getPos("PosCommSeq"), frameFormatServerLoginRes.getLen("PosCommSeq"));
                        if (this.posIcSeq == newposIcSeq && this.posAccSeq == newposAccSeq && newposCommSeq == this.posCommSeq) {
                            this.posIcSeq = 0;
                            this.posAccSeq = 0;
                            this.posCommSeq = 0;
                            flag = LOGIN_FLAG_INIT;
                        } else {
                            flag = LOGIN_FLAG_NEEDUPDATESEQ;
                        }
                        reload = true;
                    } else if (bResponsecode == (byte) 41) {
                        if (flag == LOGIN_FLAG_NEEDUPDATESEQ) {
                            hsmpResult.put(ErrorCode.SERVER_RESPONSE, Integer.toHexString(bResponsecode));
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "644");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        this.posIcSeq = FrameItemType.getHEX2Long(dataServerLoginRes, frameFormatServerLoginRes.getPos("posIcSeq"), frameFormatServerLoginRes.getLen("posIcSeq"));
                        this.posAccSeq = FrameItemType.getHEX2Long(dataServerLoginRes, frameFormatServerLoginRes.getPos("PosAccSeq"), frameFormatServerLoginRes.getLen("PosAccSeq"));
                        this.posCommSeq = FrameItemType.getHEX2Long(dataServerLoginRes, frameFormatServerLoginRes.getPos("PosCommSeq"), frameFormatServerLoginRes.getLen("PosCommSeq"));
                        reload = true;
                        flag = LOGIN_FLAG_NEEDUPDATESEQ;
                    } else if (bResponsecode == (byte) 6) {
                        hsmpResult.put(ErrorCode.SERVER_RESPONSE, Integer.toHexString(bResponsecode));
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "656");
                        this.trafficCardServerEntity.disconnect();
                        return hsmpResult;
                    } else if (bResponsecode == (byte) 15) {
                        if (flag == LOGIN_FLAG_NEEDDOWNLOAD) {
                            hsmpResult.put(ErrorCode.SERVER_RESPONSE, Integer.toHexString(bResponsecode));
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "663");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        hsmpResult.put("DOWNLOAD_FLAG", "TRUE");
                        this.trafficCardServerEntity.disconnect();
                        this.trafficCardServerEntity.connect(this.ip, this.port);
                        downloadParam(this.paramfile, 1);
                        loadParamFromFile(this.paramfile);
                        reload = true;
                        flag = LOGIN_FLAG_NEEDDOWNLOAD;
                    } else if (bResponsecode == (byte) 0) {
                        this.posIcSeq = (long) ((int) FrameItemType.getHEX2Long(dataServerLoginRes, frameFormatServerLoginRes.getPos("posIcSeq"), frameFormatServerLoginRes.getLen("posIcSeq")));
                        this.posAccSeq = (long) ((int) FrameItemType.getHEX2Long(dataServerLoginRes, frameFormatServerLoginRes.getPos("posAccSeq"), frameFormatServerLoginRes.getLen("posAccSeq")));
                        this.posCommSeq = (long) ((int) FrameItemType.getHEX2Long(dataServerLoginRes, frameFormatServerLoginRes.getPos("posCommSeq"), frameFormatServerLoginRes.getLen("posCommSeq")));
                        this.batchNo = (int) FrameItemType.getHEX2Long(dataServerLoginRes, frameFormatServerLoginRes.getPos("BatchNO"), frameFormatServerLoginRes.getLen("BatchNO"));
                        String EncText = FrameItemType.getHEX2String(dataServerLoginRes, frameFormatServerLoginRes.getPos("EncText"), frameFormatServerLoginRes.getLen("EncText"));
                        String MacBuf = FrameItemType.getHEX2String(dataServerLoginRes, frameFormatServerLoginRes.getPos("MacBuf"), frameFormatServerLoginRes.getLen("MacBuf"));
                        FrameFormat frameFormatSerialLoginAuth = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.LOGIN_AUTH);
                        byte[] dataSerialLoginAuth = new byte[frameFormatSerialLoginAuth.getFrameLen()];
                        FrameItemType.setString(dataSerialLoginAuth, frameFormatSerialLoginAuth.getPos("Messagetype"), SerialFrameType.LOGIN_AUTH);
                        FrameItemType.setString(dataSerialLoginAuth, frameFormatSerialLoginAuth.getPos("EncText"), EncText);
                        FrameItemType.setString(dataSerialLoginAuth, frameFormatSerialLoginAuth.getPos("MacBuf"), MacBuf);
                        this.trafficCardReaderEntity.send(dataSerialLoginAuth, 0, dataSerialLoginAuth.length);
                        byte[] dataSerialLoginAuthRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                        if (dataSerialLoginAuthRes == null) {
                            hsmpResult.put(ErrorCode.ERRCODE, "SERVER_NORESPONSE");
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "691");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        FrameFormat frameFormatSerialLoginAuthRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.LOGIN_AUTH, null);
                        responseCode = FrameItemType.getString(dataSerialLoginAuthRes, frameFormatSerialLoginAuthRes.getPos("responseCode"), frameFormatSerialLoginAuthRes.getLen("responseCode"));
                        if ("AA12".equalsIgnoreCase(responseCode)) {
                            hsmpResult.put("REMAIN_FAIL_COUNT", Integer.toString((int) FrameItemType.getHString2Long(dataSerialLoginAuthRes, frameFormatSerialLoginAuthRes.getPos("RemainFailCount"), frameFormatSerialLoginAuthRes.getLen("RemainFailCount"))));
                            hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "702");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        } else if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                            FrameFormat frameFormatServerLoginConfirm = FrameDataFormat.getServerFrameFormat(ServerFrameType.LOGIN_CONFIRM, 2);
                            byte[] dataServerLoginConfirm = new byte[frameFormatServerLoginConfirm.getFrameLen()];
                            FrameItemType.setLong2BCD(dataServerLoginConfirm, frameFormatServerLoginConfirm.getPos("MessageType"), frameFormatServerLoginConfirm.getLen("MessageType"), 5000);
                            FrameItemType.setByte(dataServerLoginConfirm, frameFormatServerLoginConfirm.getPos("Ver"), (byte) 2);
                            FrameItemType.setString2BCD(dataServerLoginConfirm, frameFormatServerLoginConfirm.getPos("ISAM"), frameFormatServerLoginConfirm.getLen("ISAM"), ISAM);
                            FrameItemType.setByte(dataServerLoginConfirm, frameFormatServerLoginConfirm.getPos("ResponseCode"), (byte) (Integer.parseInt(responseCode, 16) & MotionEventCompat.ACTION_MASK));
                            this.trafficCardServerEntity.send(this.dataSeq, dataServerLoginConfirm, 0, dataServerLoginConfirm.length);
                            Thread.sleep(500);
                            hsmpResult.put("RESULT", "SUCCESS");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        } else {
                            hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "708");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                    } else {
                        hsmpResult.put(ErrorCode.SERVER_RESPONSE, Integer.toHexString(bResponsecode));
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "727");
                        this.trafficCardServerEntity.disconnect();
                        return hsmpResult;
                    }
                    this.trafficCardServerEntity.disconnect();
                    if (reload) {
                        return login(flag);
                    }
                    hsmpResult.put(ErrorCode.ERRCODE, NetworkUtils.NET_STATE_UNKNOWN);
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "740");
                    return hsmpResult;
                }
                hsmpResult.put(ErrorCode.ERRCODE, "SERVER_LOST");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "536");
                this.trafficCardServerEntity.disconnect();
                return hsmpResult;
            }
            hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "524");
            this.trafficCardServerEntity.disconnect();
            return hsmpResult;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.trafficCardServerEntity.disconnect();
        }
        return null;
    }

    public boolean logout(String ISAM) {
        try {
            if (!this.trafficCardServerEntity.connect(this.ip, this.port)) {
                return false;
            }
            int newBatchNo = this.batchNo + 1;
            FrameFormat frameFormatServerDataUploadReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.DATA_UPLOAD_REQ, 1);
            byte[] dataServerDataUploadReq = new byte[frameFormatServerDataUploadReq.getFrameLen()];
            FrameItemType.setLong2BCD(dataServerDataUploadReq, frameFormatServerDataUploadReq.getPos("MessageType"), frameFormatServerDataUploadReq.getLen("MessageType"), 5037);
            FrameItemType.setByte(dataServerDataUploadReq, frameFormatServerDataUploadReq.getPos("Ver"), (byte) 1);
            FrameItemType.setString2BCD(dataServerDataUploadReq, frameFormatServerDataUploadReq.getPos("SysDatetime"), frameFormatServerDataUploadReq.getLen("SysDatetime"), DateUtils.formatDatetime(new Date(), "yyyyMMddHHmmss"));
            FrameItemType.setString(dataServerDataUploadReq, frameFormatServerDataUploadReq.getPos("MessageID"), "0000000000000000000000000000000000");
            FrameItemType.setString(dataServerDataUploadReq, frameFormatServerDataUploadReq.getPos("posId"), frameFormatServerDataUploadReq.getLen("posId"), this.posId);
            FrameItemType.setString2BCD(dataServerDataUploadReq, frameFormatServerDataUploadReq.getPos("UnitId"), frameFormatServerDataUploadReq.getLen("UnitId"), this.unitId);
            FrameItemType.setString2BCD(dataServerDataUploadReq, frameFormatServerDataUploadReq.getPos("MchntId"), frameFormatServerDataUploadReq.getLen("MchntId"), this.mchntId);
            FrameItemType.setLong2HEX(dataServerDataUploadReq, frameFormatServerDataUploadReq.getPos("BatchNo"), frameFormatServerDataUploadReq.getLen("BatchNo"), (long) newBatchNo);
            FrameItemType.setString2BCD(dataServerDataUploadReq, frameFormatServerDataUploadReq.getPos("ISAM"), frameFormatServerDataUploadReq.getLen("ISAM"), ISAM);
            FrameItemType.setLong2HEX(dataServerDataUploadReq, frameFormatServerDataUploadReq.getPos("MessageLen"), frameFormatServerDataUploadReq.getLen("MessageLen"), 0);
            System.out.println("SERVER:" + frameFormatServerDataUploadReq.dump(dataServerDataUploadReq, 0, dataServerDataUploadReq.length));
            this.trafficCardServerEntity.send(this.dataSeq, dataServerDataUploadReq, 0, dataServerDataUploadReq.length);
            if (this.trafficCardServerEntity.recv(10000) == null) {
                this.trafficCardServerEntity.disconnect();
                return false;
            }
            this.batchNo = newBatchNo;
            FrameFormat frameFormatServerLogoutReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.LOGOUT_REQ, 2);
            byte[] dataServerLogoutReq = new byte[frameFormatServerLogoutReq.getFrameLen()];
            FrameItemType.setLong2BCD(dataServerLogoutReq, frameFormatServerLogoutReq.getPos("MessageType"), frameFormatServerLogoutReq.getLen("MessageType"), 6001);
            FrameItemType.setByte(dataServerLogoutReq, frameFormatServerLogoutReq.getPos("Ver"), (byte) 2);
            FrameItemType.setString2BCD(dataServerLogoutReq, frameFormatServerLogoutReq.getPos("SysDatetime"), frameFormatServerLogoutReq.getLen("SysDatetime"), DateUtils.formatDatetime(new Date(), "yyyyMMddHHmmss"));
            FrameItemType.setString2BCD(dataServerLogoutReq, frameFormatServerLogoutReq.getPos("oprId"), frameFormatServerLogoutReq.getLen("oprId"), this.oprId);
            FrameItemType.setString(dataServerLogoutReq, frameFormatServerLogoutReq.getPos("posId"), frameFormatServerLogoutReq.getLen("posId"), this.posId);
            FrameItemType.setString2BCD(dataServerLogoutReq, frameFormatServerLogoutReq.getPos("ISAM"), frameFormatServerLogoutReq.getLen("ISAM"), ISAM);
            FrameItemType.setString2BCD(dataServerLogoutReq, frameFormatServerLogoutReq.getPos("UnitId"), frameFormatServerLogoutReq.getLen("UnitId"), this.unitId);
            FrameItemType.setString2BCD(dataServerLogoutReq, frameFormatServerLogoutReq.getPos("MchntId"), frameFormatServerLogoutReq.getLen("MchntId"), this.mchntId);
            FrameItemType.setLong2HEX(dataServerLogoutReq, frameFormatServerLogoutReq.getPos("BatchNo"), frameFormatServerLogoutReq.getLen("BatchNo"), (long) newBatchNo);
            FrameItemType.setLong2HEX(dataServerLogoutReq, frameFormatServerLogoutReq.getPos("posIcSeq"), frameFormatServerLogoutReq.getLen("posIcSeq"), this.posIcSeq);
            FrameItemType.setLong2HEX(dataServerLogoutReq, frameFormatServerLogoutReq.getPos("posAccSeq"), frameFormatServerLogoutReq.getLen("posAccSeq"), this.posAccSeq);
            FrameItemType.setLong2HEX(dataServerLogoutReq, frameFormatServerLogoutReq.getPos("posCommSeq"), frameFormatServerLogoutReq.getLen("posCommSeq"), this.posCommSeq);
            System.out.println("SERVER SEND:" + frameFormatServerLogoutReq.dump(dataServerLogoutReq, 0, dataServerLogoutReq.length));
            this.trafficCardServerEntity.send(this.dataSeq, dataServerLogoutReq, 0, dataServerLogoutReq.length);
            byte[] dataServerLogoutRes = this.trafficCardServerEntity.recv(10000);
            if (dataServerLogoutRes == null) {
                this.trafficCardServerEntity.disconnect();
                return false;
            }
            FrameFormat frameFormatServerLogoutRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.LOGOUT_RES, 2);
            System.out.println("SERVER RECV:" + frameFormatServerLogoutRes.dump(dataServerLogoutRes, 0, dataServerLogoutRes.length));
            if (((int) FrameItemType.getBCD2Long(dataServerLogoutRes, frameFormatServerLogoutRes.getPos("MessageType"), frameFormatServerLogoutRes.getLen("MessageType"))) != 6002) {
                this.trafficCardServerEntity.disconnect();
                return false;
            }
            byte bResponsecode = FrameItemType.getByte(dataServerLogoutRes, frameFormatServerLogoutRes.getPos("ResponseCode"));
            if (bResponsecode == (byte) 42 || bResponsecode == (byte) 15) {
                this.trafficCardServerEntity.disconnect();
                return false;
            } else if (bResponsecode == (byte) 0) {
                FrameFormat frameFormatSerialLogoutReset = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.LOGOUT_RESET);
                byte[] dataSerialLogoutReset = new byte[frameFormatSerialLogoutReset.getFrameLen()];
                FrameItemType.setString(dataSerialLogoutReset, frameFormatSerialLogoutReset.getPos("Messagetype"), SerialFrameType.LOGOUT_RESET);
                this.trafficCardReaderEntity.send(dataSerialLogoutReset, 0, dataSerialLogoutReset.length);
                byte[] dataSerialLogoutResetRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                if (dataSerialLogoutResetRes == null) {
                    this.trafficCardServerEntity.disconnect();
                    return false;
                }
                FrameFormat frameFormatSerialLogoutResetRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.LOGOUT_RESET, null);
                if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(dataSerialLogoutResetRes, frameFormatSerialLogoutResetRes.getPos("responseCode"), frameFormatSerialLogoutResetRes.getLen("responseCode")))) {
                    this.trafficCardServerEntity.disconnect();
                    return true;
                }
                this.trafficCardServerEntity.disconnect();
                return false;
            } else {
                this.trafficCardServerEntity.disconnect();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.trafficCardServerEntity.disconnect();
        }
        return false;
    }

    public HashMap readModel() {
        HashMap hsmpResult = new HashMap();
        FrameFormat frameFormatSerialReadVersion = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.READ_VERSION);
        byte[] dataSerialReadVersion = new byte[frameFormatSerialReadVersion.getFrameLen()];
        FrameItemType.setString(dataSerialReadVersion, frameFormatSerialReadVersion.getPos("Messagetype"), SerialFrameType.READ_VERSION);
        this.trafficCardReaderEntity.send(dataSerialReadVersion, 0, dataSerialReadVersion.length);
        byte[] dataSerialReadVersionRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
        if (dataSerialReadVersionRes == null) {
            hsmpResult.put(ErrorCode.ERRCODE, "MODEL_UNKNOWN");
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "839");
        } else {
            FrameFormat frameFormatSerialReadVersionRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.READ_VERSION, null);
            String responseCode = FrameItemType.getString(dataSerialReadVersionRes, frameFormatSerialReadVersionRes.getPos("responseCode"), frameFormatSerialReadVersionRes.getLen("responseCode"));
            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                this.ISAM = FrameItemType.getString(dataSerialReadVersionRes, frameFormatSerialReadVersionRes.getPos("ISAM"), frameFormatSerialReadVersionRes.getLen("ISAM"));
                String Version = FrameItemType.getString(dataSerialReadVersionRes, frameFormatSerialReadVersionRes.getPos("Version"), frameFormatSerialReadVersionRes.getLen("Version"));
                this.posId = this.ISAM;
                FrameFormat frameFormatSerialReadLastRecord = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.READ_LAST_RECORD);
                byte[] dataSerialReadLastRecord = new byte[frameFormatSerialReadLastRecord.getFrameLen()];
                FrameItemType.setString(dataSerialReadLastRecord, frameFormatSerialReadLastRecord.getPos("Messagetype"), SerialFrameType.READ_LAST_RECORD);
                this.trafficCardReaderEntity.send(dataSerialReadLastRecord, 0, dataSerialReadLastRecord.length);
                byte[] dataSerialReadLastRecordRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                if (dataSerialReadLastRecordRes == null) {
                    hsmpResult.put(ErrorCode.ERRCODE, "MODEL_NORESPONSE");
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "861");
                } else {
                    FrameFormat frameFormatSerialReadLastRecordRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.READ_LAST_RECORD, "00");
                    responseCode = FrameItemType.getString(dataSerialReadLastRecordRes, frameFormatSerialReadLastRecordRes.getPos("responseCode"), frameFormatSerialReadLastRecordRes.getLen("responseCode"));
                    if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                        this.PsamTransNo = (int) FrameItemType.getHString2Long(dataSerialReadLastRecordRes, frameFormatSerialReadLastRecordRes.getPos("PsamTransNo"), frameFormatSerialReadLastRecordRes.getLen("PsamTransNo"));
                        this.PsamTermNo = (int) FrameItemType.getHString2Long(dataSerialReadLastRecordRes, frameFormatSerialReadLastRecordRes.getPos("PsamTermNo"), frameFormatSerialReadLastRecordRes.getLen("PsamTermNo"));
                        this.hostSeq = (int) FrameItemType.getHString2Long(dataSerialReadLastRecordRes, frameFormatSerialReadLastRecordRes.getPos("hostSeq"), frameFormatSerialReadLastRecordRes.getLen("hostSeq"));
                        hsmpResult.put("ISAM", this.ISAM);
                        hsmpResult.put(updateDetection.VERSION, Version);
                        hsmpResult.put("RESULT", "SUCCESS");
                    } else {
                        hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "869");
                    }
                }
            } else {
                hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "847");
            }
        }
        return hsmpResult;
    }

    public HashMap readCard(int readMode) {
        HashMap hsmpResult = new HashMap();
        FrameFormat frameFormatSerialFindCard = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.FIND_CARD);
        byte[] dataSerialFindCard = new byte[frameFormatSerialFindCard.getFrameLen()];
        FrameItemType.setString(dataSerialFindCard, frameFormatSerialFindCard.getPos("Messagetype"), SerialFrameType.FIND_CARD);
        this.trafficCardReaderEntity.send(dataSerialFindCard, 0, dataSerialFindCard.length);
        byte[] dataSerialFindcardRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
        if (dataSerialFindcardRes == null) {
            hsmpResult.put(ErrorCode.ERRCODE, "MODEL_NORESPONSE");
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "891");
        } else {
            FrameFormat frameFormatSerialFindCardRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.FIND_CARD, null);
            String responseCode = FrameItemType.getString(dataSerialFindcardRes, frameFormatSerialFindCardRes.getPos("responseCode"), frameFormatSerialFindCardRes.getLen("responseCode"));
            if (SerialFrameType.LOCK_CARD.equalsIgnoreCase(responseCode)) {
                hsmpResult.put(ErrorCode.ERRCODE, "CARD_NOTFOUND");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "899");
            } else if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                FrameFormat frameFormatSerialReadCard = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.READ_CARD);
                byte[] dataSerialReadCard = new byte[frameFormatSerialReadCard.getFrameLen()];
                FrameItemType.setString(dataSerialReadCard, frameFormatSerialReadCard.getPos("Messagetype"), SerialFrameType.READ_CARD);
                FrameItemType.setString(dataSerialReadCard, frameFormatSerialReadCard.getPos("TransType"), "02");
                FrameItemType.setString(dataSerialReadCard, frameFormatSerialReadCard.getPos("TransParam"), "00");
                System.out.println("MODEL SEND:" + frameFormatSerialReadCard.dump(dataSerialReadCard));
                this.trafficCardReaderEntity.send(dataSerialReadCard, 0, dataSerialReadCard.length);
                byte[] dataSerialReadcardRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                if (dataSerialReadcardRes == null) {
                    hsmpResult.put(ErrorCode.ERRCODE, "MODEL_NORESPONSE");
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "919");
                } else {
                    FrameFormat frameFormatSerialReadCardRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.READ_CARD, "00");
                    System.out.println("MODEL RECV:" + frameFormatSerialReadCardRes.dump(dataSerialReadcardRes));
                    responseCode = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("responseCode"), frameFormatSerialReadCardRes.getLen("responseCode"));
                    if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                        int remainedLen;
                        int pos;
                        int i;
                        int prevBalance;
                        String CSN = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("CSN"), frameFormatSerialReadCardRes.getLen("CSN"));
                        String CardNo = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("CardNo"), frameFormatSerialReadCardRes.getLen("CardNo"));
                        String CardType = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("CardType"), frameFormatSerialReadCardRes.getLen("CardType"));
                        String PhyCardType = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("PhyCardType"), frameFormatSerialReadCardRes.getLen("PhyCardType"));
                        String CardState = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("CardState"), frameFormatSerialReadCardRes.getLen("CardState"));
                        String StartDate = "" + FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("StartDate"), frameFormatSerialReadCardRes.getLen("StartDate"));
                        String EndDate = "" + FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("EndDate"), frameFormatSerialReadCardRes.getLen("EndDate"));
                        int Balance = (int) FrameItemType.getHString2Balance(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("Balance"), frameFormatSerialReadCardRes.getLen("Balance"));
                        int Deposit = (int) FrameItemType.getHString2Balance(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("Deposit"), frameFormatSerialReadCardRes.getLen("Deposit"));
                        String LastRecord = "";
                        if (dataSerialReadcardRes.length == frameFormatSerialReadCardRes.getPos("LastRecord") + frameFormatSerialReadCardRes.getLen("LastRecord")) {
                            LastRecord = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("LastRecord"), frameFormatSerialReadCardRes.getLen("LastRecord"));
                        }
                        if ((readMode & 1) != 0) {
                            dataSerialReadCard = new byte[frameFormatSerialReadCard.getFrameLen()];
                            FrameItemType.setString(dataSerialReadCard, frameFormatSerialReadCard.getPos("Messagetype"), SerialFrameType.READ_CARD);
                            FrameItemType.setString(dataSerialReadCard, frameFormatSerialReadCard.getPos("TransType"), "CC");
                            FrameItemType.setString(dataSerialReadCard, frameFormatSerialReadCard.getPos("TransParam"), "00");
                            System.out.println("MODEL SEND:" + frameFormatSerialReadCard.dump(dataSerialReadCard));
                            this.trafficCardReaderEntity.send(dataSerialReadCard, 0, dataSerialReadCard.length);
                            dataSerialReadcardRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                            if (dataSerialReadcardRes == null) {
                                hsmpResult.put(ErrorCode.ERRCODE, "MODEL_NORESPONSE");
                                hsmpResult.put("RESULT", "FAILED");
                                hsmpResult.put("ERROR_FLAG", "956");
                            } else {
                                frameFormatSerialReadCardRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.READ_CARD, "CC");
                                System.out.println("MODEL RECV:" + frameFormatSerialReadCardRes.dump(dataSerialReadcardRes));
                                responseCode = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("responseCode"), frameFormatSerialReadCardRes.getLen("responseCode"));
                                if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                                    List<HashMap> listConsumeRecord = new ArrayList();
                                    String ConsumeRecordFormat = "";
                                    String ConsumeRecord = "";
                                    remainedLen = dataSerialReadcardRes.length - frameFormatSerialReadCardRes.getPos("ConsumeRecord");
                                    if (remainedLen >= 0) {
                                        FrameFormat frameFormatSerialConsumeRecord;
                                        int transAmount;
                                        String TransTime;
                                        String thisTime;
                                        String RealTransTime;
                                        String TransType;
                                        HashMap hsmpConsumeRecord;
                                        int count = remainedLen / 32;
                                        frameFormatSerialReadCardRes.setLen("ConsumeRecord", remainedLen);
                                        ConsumeRecordFormat = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("ConsumeRecordFormat"), frameFormatSerialReadCardRes.getLen("ConsumeRecordFormat"));
                                        CSN = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("CSN"), frameFormatSerialReadCardRes.getLen("CSN"));
                                        ConsumeRecord = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("ConsumeRecord"), frameFormatSerialReadCardRes.getLen("ConsumeRecord"));
                                        if ("01".equals(ConsumeRecordFormat)) {
                                            frameFormatSerialConsumeRecord = FrameDataFormat.getFrameFormat("ConsumeRecord1");
                                            pos = 0;
                                            for (i = 0; i < count; i++) {
                                                prevBalance = (int) FrameItemType.getHString2Balance(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ConsumeRecord") + pos) + frameFormatSerialConsumeRecord.getPos("PrevBalance"), frameFormatSerialConsumeRecord.getLen("PrevBalance"));
                                                transAmount = (int) FrameItemType.getHString2Balance(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ConsumeRecord") + pos) + frameFormatSerialConsumeRecord.getPos("TransAmount"), frameFormatSerialConsumeRecord.getLen("TransAmount"));
                                                if (transAmount != 0) {
                                                    TransTime = FrameItemType.getString(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ConsumeRecord") + pos) + frameFormatSerialConsumeRecord.getPos("TransTime"), frameFormatSerialConsumeRecord.getLen("TransTime"));
                                                    thisTime = DateUtils.formatDatetime(new Date(), "yyyyMMddHHmm");
                                                    if (Long.parseLong(thisTime.substring(2)) < Long.parseLong(TransTime)) {
                                                        RealTransTime = Integer.toString(Integer.parseInt(thisTime.substring(0, 2)) - 1) + TransTime + "00";
                                                    } else {
                                                        RealTransTime = thisTime.substring(0, 2) + TransTime + "00";
                                                    }
                                                    TransType = FrameItemType.getString(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ConsumeRecord") + pos) + frameFormatSerialConsumeRecord.getPos("TransType"), frameFormatSerialConsumeRecord.getLen("TransType"));
                                                    hsmpConsumeRecord = new HashMap();
                                                    hsmpConsumeRecord.put("TransTime", RealTransTime);
                                                    hsmpConsumeRecord.put("TransType", "" + TransType);
                                                    hsmpConsumeRecord.put("PrevBalance", "" + prevBalance);
                                                    hsmpConsumeRecord.put("TransAmount", "" + transAmount);
                                                    listConsumeRecord.add(hsmpConsumeRecord);
                                                }
                                                pos += frameFormatSerialConsumeRecord.getFrameLen();
                                            }
                                        }
                                        if ("02".equals(ConsumeRecordFormat)) {
                                            frameFormatSerialConsumeRecord = FrameDataFormat.getFrameFormat("ConsumeRecord2");
                                            pos = 0;
                                            for (i = 0; i < count; i++) {
                                                transAmount = (int) FrameItemType.getHString2Balance(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ConsumeRecord") + pos) + frameFormatSerialConsumeRecord.getPos("TransAmount"), frameFormatSerialConsumeRecord.getLen("TransAmount"));
                                                if (transAmount != 0) {
                                                    TransTime = FrameItemType.getString(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ConsumeRecord") + pos) + frameFormatSerialConsumeRecord.getPos("TransTime"), frameFormatSerialConsumeRecord.getLen("TransTime"));
                                                    thisTime = DateUtils.formatDatetime(new Date(), "yyyyMMddHHmm");
                                                    if (Long.parseLong(thisTime.substring(2)) < Long.parseLong(TransTime)) {
                                                        RealTransTime = Integer.toString(Integer.parseInt(thisTime.substring(0, 2)) - 1) + TransTime + "00";
                                                    } else {
                                                        RealTransTime = thisTime.substring(0, 2) + TransTime + "00";
                                                    }
                                                    TransType = FrameItemType.getString(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ConsumeRecord") + pos) + frameFormatSerialConsumeRecord.getPos("TransType"), frameFormatSerialConsumeRecord.getLen("TransType"));
                                                    hsmpConsumeRecord = new HashMap();
                                                    hsmpConsumeRecord.put("TransTime", RealTransTime);
                                                    hsmpConsumeRecord.put("TransType", "" + TransType);
                                                    hsmpConsumeRecord.put("TransAmount", "" + transAmount);
                                                    listConsumeRecord.add(hsmpConsumeRecord);
                                                }
                                                pos += frameFormatSerialConsumeRecord.getFrameLen();
                                            }
                                        }
                                    }
                                    hsmpResult.put("ConsumeRecordFormat", ConsumeRecordFormat);
                                    hsmpResult.put("ConsumeRecord", ConsumeRecord);
                                    hsmpResult.put("ConsumeRecordList", listConsumeRecord);
                                } else {
                                    hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                                    hsmpResult.put("RESULT", "FAILED");
                                    hsmpResult.put("ERROR_FLAG", "965");
                                }
                            }
                        }
                        if ((readMode & 2) != 0) {
                            dataSerialReadCard = new byte[frameFormatSerialReadCard.getFrameLen()];
                            FrameItemType.setString(dataSerialReadCard, frameFormatSerialReadCard.getPos("Messagetype"), SerialFrameType.READ_CARD);
                            FrameItemType.setString(dataSerialReadCard, frameFormatSerialReadCard.getPos("TransType"), "CD");
                            FrameItemType.setString(dataSerialReadCard, frameFormatSerialReadCard.getPos("TransParam"), "00");
                            System.out.println("MODEL SEND:" + frameFormatSerialReadCard.dump(dataSerialReadCard));
                            this.trafficCardReaderEntity.send(dataSerialReadCard, 0, dataSerialReadCard.length);
                            dataSerialReadcardRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                            if (dataSerialReadcardRes == null) {
                                hsmpResult.put(ErrorCode.ERRCODE, "MODEL_NORESPONSE");
                                hsmpResult.put("RESULT", "FAILED");
                                hsmpResult.put("ERROR_FLAG", "1046");
                            } else {
                                frameFormatSerialReadCardRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.READ_CARD, "CD");
                                frameFormatSerialReadCardRes.setLen("ChargeRecord", dataSerialReadcardRes.length - frameFormatSerialReadCardRes.getPos("ChargeRecord"));
                                System.out.println("MODEL RECV:" + frameFormatSerialReadCardRes.dump(dataSerialReadcardRes));
                                responseCode = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("responseCode"), frameFormatSerialReadCardRes.getLen("responseCode"));
                                if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                                    remainedLen = dataSerialReadcardRes.length - frameFormatSerialReadCardRes.getPos("ChargeRecord");
                                    List<HashMap> listChargeRecord = new ArrayList();
                                    String ChargeRecordFormat = "";
                                    String ChargeRecord = "";
                                    if (remainedLen >= 0) {
                                        FrameFormat frameFormatSerialChargeRecord;
                                        int n;
                                        String TransDate;
                                        int nextBalance;
                                        int chargeAmount;
                                        String thisDate;
                                        String RealTransDate;
                                        HashMap hsmpChargeRecord;
                                        ChargeRecordFormat = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("ChargeRecordFormat"), frameFormatSerialReadCardRes.getLen("ChargeRecordFormat"));
                                        CSN = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("CSN"), frameFormatSerialReadCardRes.getLen("CSN"));
                                        ChargeRecord = FrameItemType.getString(dataSerialReadcardRes, frameFormatSerialReadCardRes.getPos("ChargeRecord"), frameFormatSerialReadCardRes.getLen("ChargeRecord"));
                                        if ("01".equals(ChargeRecordFormat)) {
                                            frameFormatSerialChargeRecord = FrameDataFormat.getFrameFormat("ChargeRecord1");
                                            n = frameFormatSerialReadCardRes.getLen("ChargeRecord") / frameFormatSerialChargeRecord.getFrameLen();
                                            pos = 0;
                                            for (i = 0; i < n; i++) {
                                                TransDate = FrameItemType.getString(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ChargeRecord") + pos) + frameFormatSerialChargeRecord.getPos("TransDate"), frameFormatSerialChargeRecord.getLen("TransDate"));
                                                if ("FFFFFF".equalsIgnoreCase(TransDate)) {
                                                    break;
                                                }
                                                prevBalance = (int) FrameItemType.getHString2Balance(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ChargeRecord") + pos) + frameFormatSerialChargeRecord.getPos("PrevBalance"), frameFormatSerialChargeRecord.getLen("PrevBalance"));
                                                nextBalance = (int) FrameItemType.getHString2Balance(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ChargeRecord") + pos) + frameFormatSerialChargeRecord.getPos("NextBalance"), frameFormatSerialChargeRecord.getLen("NextBalance"));
                                                chargeAmount = nextBalance - prevBalance;
                                                thisDate = DateUtils.formatDatetime(new Date(), "yyyyMMdd");
                                                if (Long.parseLong(thisDate.substring(2)) < Long.parseLong(TransDate)) {
                                                    RealTransDate = Integer.toString(Integer.parseInt(TransDate.substring(0, 2)) - 1) + TransDate;
                                                } else {
                                                    RealTransDate = thisDate.substring(0, 2) + TransDate;
                                                }
                                                hsmpChargeRecord = new HashMap();
                                                hsmpChargeRecord.put("TransDate", RealTransDate);
                                                hsmpChargeRecord.put("PrevBalance", "" + prevBalance);
                                                hsmpChargeRecord.put("NextBalance", "" + nextBalance);
                                                hsmpChargeRecord.put("TransAmount", "" + chargeAmount);
                                                listChargeRecord.add(hsmpChargeRecord);
                                                pos += frameFormatSerialChargeRecord.getFrameLen();
                                            }
                                        }
                                        if ("02".equals(ChargeRecordFormat)) {
                                            frameFormatSerialChargeRecord = FrameDataFormat.getFrameFormat("ChargeRecord2");
                                            n = frameFormatSerialReadCardRes.getLen("ChargeRecord") / frameFormatSerialChargeRecord.getFrameLen();
                                            pos = 0;
                                            for (i = 0; i < n; i++) {
                                                TransDate = FrameItemType.getString(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ChargeRecord") + pos) + frameFormatSerialChargeRecord.getPos("TransDate"), frameFormatSerialChargeRecord.getLen("TransDate"));
                                                if ("FFFFFF".equalsIgnoreCase(TransDate)) {
                                                    break;
                                                }
                                                prevBalance = (int) FrameItemType.getHString2Balance(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ChargeRecord") + pos) + frameFormatSerialChargeRecord.getPos("PrevBalance"), frameFormatSerialChargeRecord.getLen("PrevBalance"));
                                                nextBalance = (int) FrameItemType.getHString2Balance(dataSerialReadcardRes, (frameFormatSerialReadCardRes.getPos("ChargeRecord") + pos) + frameFormatSerialChargeRecord.getPos("NextBalance"), frameFormatSerialChargeRecord.getLen("NextBalance"));
                                                chargeAmount = nextBalance - prevBalance;
                                                thisDate = DateUtils.formatDatetime(new Date(), "yyyyMMdd");
                                                if (Long.parseLong(thisDate.substring(2)) < Long.parseLong(TransDate)) {
                                                    RealTransDate = Integer.toString(Integer.parseInt(TransDate.substring(0, 2)) - 1) + TransDate;
                                                } else {
                                                    RealTransDate = thisDate.substring(0, 2) + TransDate;
                                                }
                                                hsmpChargeRecord = new HashMap();
                                                hsmpChargeRecord.put("TransDate", RealTransDate);
                                                hsmpChargeRecord.put("PrevBalance", "" + prevBalance);
                                                hsmpChargeRecord.put("NextBalance", "" + nextBalance);
                                                hsmpChargeRecord.put("TransAmount", "" + chargeAmount);
                                                listChargeRecord.add(hsmpChargeRecord);
                                                pos += frameFormatSerialChargeRecord.getFrameLen();
                                            }
                                        }
                                    }
                                    hsmpResult.put("ChargeRecordFormat", ChargeRecordFormat);
                                    hsmpResult.put("ChargeRecord", ChargeRecord);
                                    hsmpResult.put("ChargeRecordList", listChargeRecord);
                                } else {
                                    hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                                    hsmpResult.put("RESULT", "FAILED");
                                    hsmpResult.put("ERROR_FLAG", "1056");
                                }
                            }
                        }
                        String MaxBalance = null;
                        HashMap hsmpCardType = getCardType(CardType, PhyCardType);
                        if (hsmpCardType != null) {
                            MaxBalance = (String) hsmpCardType.get("MaxBalance");
                        }
                        if (StringUtils.isBlank(MaxBalance)) {
                            MaxBalance = "100000";
                        }
                        hsmpResult.put("NextPosCommSeq", "" + (this.posCommSeq + 1));
                        hsmpResult.put("CSN", CSN);
                        hsmpResult.put("ISAM", this.ISAM);
                        hsmpResult.put("CardNo", CardNo);
                        hsmpResult.put("CardType", CardType);
                        hsmpResult.put("PhyCardType", PhyCardType);
                        hsmpResult.put("CardState", CardState);
                        hsmpResult.put("StartDate", StartDate);
                        hsmpResult.put("EndDate", EndDate);
                        hsmpResult.put("Balance", Integer.toString(Balance));
                        hsmpResult.put("MaxBalance", MaxBalance);
                        hsmpResult.put("Deposit", Integer.toString(Deposit));
                        hsmpResult.put("LastRecord", LastRecord);
                        hsmpResult.put("RESULT", "SUCCESS");
                    } else {
                        hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "928");
                    }
                }
            } else {
                hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "905");
            }
        }
        return hsmpResult;
    }

    public HashMap charge(String cardNo, int balance, String datetime) {
        Date date;
        try {
            date = DateUtils.parseDatetime(datetime);
        } catch (Exception e) {
            date = new Date();
        }
        if (date == null) {
            date = new Date();
        }
        HashMap hsmpResult = new HashMap();
        HashMap hsmpModel = readModel();
        if (hsmpModel == null) {
            hsmpResult.put(ErrorCode.ERRCODE, "MODEL_UNKNOWN");
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "1167");
            return hsmpResult;
        }
        if (!"SUCCESS".equalsIgnoreCase((String) hsmpModel.get("RESULT"))) {
            return hsmpModel;
        }
        String ISAM = (String) hsmpModel.get("ISAM");
        HashMap hsmpCard = readCard(0);
        if (hsmpCard == null) {
            hsmpResult.put(ErrorCode.ERRCODE, "MODEL_UNKNOWN");
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "1178");
            return hsmpResult;
        }
        if (!"SUCCESS".equalsIgnoreCase((String) hsmpCard.get("RESULT"))) {
            return hsmpCard;
        }
        if (cardNo.equals((String) hsmpCard.get("CardNo"))) {
            String CSN = (String) hsmpCard.get("CSN");
            String EndDate = (String) hsmpCard.get("EndDate");
            String sPrevBalance = (String) hsmpCard.get("Balance");
            String CardType = (String) hsmpCard.get("CardType");
            String PhyCardType = (String) hsmpCard.get("PhyCardType");
            String LastRecord = (String) hsmpCard.get("LastRecord");
            HashMap hsmpCardType = getCardType(CardType, PhyCardType);
            if (hsmpCardType == null) {
                hsmpResult.put(ErrorCode.ERRCODE, "UNSUPPORT");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "1200");
                return hsmpResult;
            }
            String ExpectEndDate;
            int MinChargeAmount = Integer.parseInt((String) hsmpCardType.get("MinChargeAmount"));
            int MaxChargeAmount = Integer.parseInt((String) hsmpCardType.get("MaxChargeAmount"));
            int ChargeAmountUnit = Integer.parseInt((String) hsmpCardType.get("ChargeAmountUnit"));
            int EndDays = Integer.parseInt((String) hsmpCardType.get("EndDays"));
            if (EndDays > 0) {
                ExpectEndDate = DateUtils.formatDatetime(new Date(System.currentTimeMillis() + (86400000 * ((long) EndDays))), "yyyyMMdd");
            } else {
                ExpectEndDate = EndDate;
            }
            int prevBalance = 0;
            if (!StringUtils.isBlank(sPrevBalance)) {
                prevBalance = Integer.parseInt(sPrevBalance);
            }
            if (prevBalance + balance <= 0) {
                hsmpResult.put(ErrorCode.ERRCODE, "NOT_ENOUGH");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "1227");
                return hsmpResult;
            }
            if (StringUtils.isBlank(LastRecord)) {
                LastRecord = "";
            }
            FrameFormat frameFormatSerialChargeReq = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_REQ);
            byte[] dataSerialChargeReq = new byte[frameFormatSerialChargeReq.getFrameLen()];
            FrameItemType.setString(dataSerialChargeReq, 0, SerialFrameType.ONLINE_CHARGE_REQ);
            FrameItemType.setString(dataSerialChargeReq, frameFormatSerialChargeReq.getPos("CSN"), CSN);
            byte[] bArr = dataSerialChargeReq;
            FrameItemType.setString(bArr, frameFormatSerialChargeReq.getPos("TransType"), TransType.TRANSTYPE_CARD_CHARGE);
            FrameItemType.setLong2HString(dataSerialChargeReq, frameFormatSerialChargeReq.getPos("TransBalance"), frameFormatSerialChargeReq.getLen("TransBalance"), (long) balance);
            System.out.println("MODULE SEND:" + frameFormatSerialChargeReq.dump(dataSerialChargeReq));
            this.trafficCardReaderEntity.send(dataSerialChargeReq, 0, dataSerialChargeReq.length);
            byte[] dataSerialChargeReqRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
            if (dataSerialChargeReqRes == null) {
                hsmpResult.put(ErrorCode.ERRCODE, "MODEL_NORESPONSE");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "1238");
                return hsmpResult;
            }
            FrameFormat frameFormatSerialChargeReqRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_REQ, "00");
            System.out.println("MODULE SEND:" + frameFormatSerialChargeReqRes.dump(dataSerialChargeReqRes));
            String responseCode = FrameItemType.getString(dataSerialChargeReqRes, frameFormatSerialChargeReqRes.getPos("responseCode"), frameFormatSerialChargeReqRes.getLen("responseCode"));
            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                String MAC = FrameItemType.getString(dataSerialChargeReqRes, frameFormatSerialChargeReqRes.getPos("MAC"), frameFormatSerialChargeReqRes.getLen("MAC"));
                String EncryptData = FrameItemType.getString(dataSerialChargeReqRes, frameFormatSerialChargeReqRes.getPos("EncryptData"), frameFormatSerialChargeReqRes.getLen("EncryptData"));
                long WalletTransSeq = FrameItemType.getHString2Long(dataSerialChargeReqRes, frameFormatSerialChargeReqRes.getPos("WalletTransSeq"), frameFormatSerialChargeReqRes.getLen("WalletTransSeq"));
                try {
                    if (this.trafficCardServerEntity.connect(this.ip, this.port)) {
                        FrameFormat frameFormatTxnMsg = FrameDataFormat.getFrameFormat("TxnMsg");
                        frameFormatTxnMsg.setLen("TxnMsg", 0);
                        frameFormatTxnMsg.setLen("TxnCryMsg", 0);
                        frameFormatTxnMsg.setLen("DigitalSign", 0);
                        FrameFormat frameFormatServerChargeReq = FrameDataFormat.getServerFrameFormat(5003, 4);
                        frameFormatServerChargeReq.setLen("TxnMsg", frameFormatTxnMsg.getFrameLen());
                        byte[] dataServerChargeReq = new byte[frameFormatServerChargeReq.getFrameLen()];
                        FrameItemType.setLong2BCD(dataServerChargeReq, frameFormatServerChargeReq.getPos("MessageType"), frameFormatServerChargeReq.getLen("MessageType"), 5003);
                        FrameItemType.setByte(dataServerChargeReq, frameFormatServerChargeReq.getPos("Ver"), (byte) 4);
                        FrameItemType.setString2BCD(dataServerChargeReq, frameFormatServerChargeReq.getPos("SysDatetime"), frameFormatServerChargeReq.getLen("SysDatetime"), DateUtils.formatDatetime(date, "yyyyMMddHHmmss"));
                        FrameItemType.setString2BCD(dataServerChargeReq, frameFormatServerChargeReq.getPos("oprId"), frameFormatServerChargeReq.getLen("oprId"), this.oprId);
                        FrameItemType.setString(dataServerChargeReq, frameFormatServerChargeReq.getPos("PosId"), frameFormatServerChargeReq.getLen("PosId"), this.posId);
                        FrameItemType.setString2BCD(dataServerChargeReq, frameFormatServerChargeReq.getPos("ISAM"), frameFormatServerChargeReq.getLen("ISAM"), ISAM);
                        FrameItemType.setString2BCD(dataServerChargeReq, frameFormatServerChargeReq.getPos("UnitId"), frameFormatServerChargeReq.getLen("UnitId"), this.unitId);
                        FrameItemType.setString2BCD(dataServerChargeReq, frameFormatServerChargeReq.getPos("MchntId"), frameFormatServerChargeReq.getLen("MchntId"), this.mchntId);
                        bArr = dataServerChargeReq;
                        FrameItemType.setLong2HEX(bArr, frameFormatServerChargeReq.getPos("BatchNo"), frameFormatServerChargeReq.getLen("BatchNo"), (long) this.batchNo);
                        FrameItemType.setString2HEX(dataServerChargeReq, frameFormatServerChargeReq.getPos("CipherDataMAC"), frameFormatServerChargeReq.getLen("CipherDataMAC"), MAC);
                        bArr = dataServerChargeReq;
                        FrameItemType.setLong2HEX(bArr, frameFormatServerChargeReq.getPos("CipherDataLen"), frameFormatServerChargeReq.getLen("CipherDataLen"), (long) frameFormatServerChargeReq.getLen("MacBuf"));
                        FrameItemType.setString2HEX(dataServerChargeReq, frameFormatServerChargeReq.getPos("MacBuf"), frameFormatServerChargeReq.getLen("MacBuf"), EncryptData);
                        FrameItemType.setString2HEX(dataServerChargeReq, frameFormatServerChargeReq.getPos("LastRecord"), frameFormatServerChargeReq.getLen("LastRecord"), LastRecord);
                        this.posCommSeq++;
                        FrameItemType.setLong2HEX(dataServerChargeReq, frameFormatServerChargeReq.getPos("PosCommSeq"), frameFormatServerChargeReq.getLen("PosCommSeq"), this.posCommSeq);
                        FrameItemType.setLong2HEX(dataServerChargeReq, frameFormatServerChargeReq.getPos("TxnMode"), frameFormatServerChargeReq.getLen("TxnMode"), 1);
                        bArr = dataServerChargeReq;
                        FrameItemType.setLong2HEX(bArr, frameFormatServerChargeReq.getPos("TxnMsg") + frameFormatTxnMsg.getPos("TxnMsgLen"), frameFormatTxnMsg.getLen("TxnMsgLen"), (long) frameFormatTxnMsg.getFrameLen());
                        FrameItemType.setLong2HEX(dataServerChargeReq, frameFormatServerChargeReq.getPos("TxnMsg") + frameFormatTxnMsg.getPos("DSSign1"), frameFormatTxnMsg.getLen("DSSign1"), 0);
                        FrameItemType.setLong2HEX(dataServerChargeReq, frameFormatServerChargeReq.getPos("TxnMsg") + frameFormatTxnMsg.getPos("DSSign2"), frameFormatTxnMsg.getLen("DSSign2"), 0);
                        System.out.println("SERVER SEND:" + frameFormatServerChargeReq.dump(dataServerChargeReq));
                        this.trafficCardServerEntity.send(this.dataSeq, dataServerChargeReq, 0, dataServerChargeReq.length);
                        byte[] dataServerChargeReqRes = this.trafficCardServerEntity.recv(SERIAL_RES_MAX_TIME);
                        if (dataServerChargeReqRes == null) {
                            hsmpResult.put(ErrorCode.ERRCODE, "SERVER_NORESPONSE");
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "1295");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        FrameFormat frameFormatServerChargeReqRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_RES, 4);
                        System.out.println("SERVER RECV:" + frameFormatServerChargeReqRes.dump(dataServerChargeReqRes));
                        if (((int) FrameItemType.getBCD2Long(dataServerChargeReqRes, frameFormatServerChargeReqRes.getPos("MessageType"), frameFormatServerChargeReqRes.getLen("MessageType"))) != 5004) {
                            hsmpResult.put(ErrorCode.ERRCODE, "SERVER_COMMERROR");
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "1304");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        this.batchNo = (int) FrameItemType.getHEX2Long(dataServerChargeReqRes, frameFormatServerChargeReqRes.getPos("BatchNo"), frameFormatServerChargeReqRes.getLen("BatchNo"));
                        String CipherDataMAC = FrameItemType.getHEX2String(dataServerChargeReqRes, frameFormatServerChargeReqRes.getPos("CipherDataMAC"), frameFormatServerChargeReqRes.getLen("CipherDataMAC"));
                        String ServerEncryptData = EncryptUtils.byte2hex(dataServerChargeReqRes, frameFormatServerChargeReqRes.getPos("MacBuf"), frameFormatServerChargeReqRes.getLen("MacBuf")).toUpperCase();
                        FrameFormat frameFormatSerialChargeReqFeedback = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_REQ_FEEDBACK);
                        this.posIcSeq++;
                        byte[] dataSerialChargeReqFeedback = new byte[frameFormatSerialChargeReqFeedback.getFrameLen()];
                        FrameItemType.setString(dataSerialChargeReqFeedback, 0, SerialFrameType.ONLINE_CHARGE_REQ_FEEDBACK);
                        FrameItemType.setLong2HString(dataSerialChargeReqFeedback, frameFormatSerialChargeReqFeedback.getPos("PosIcSeq"), frameFormatSerialChargeReqFeedback.getLen("PosIcSeq"), this.posIcSeq);
                        FrameItemType.setString(dataSerialChargeReqFeedback, frameFormatSerialChargeReqFeedback.getPos("OprId"), this.oprId);
                        bArr = dataSerialChargeReqFeedback;
                        FrameItemType.setLong2BString(bArr, frameFormatSerialChargeReqFeedback.getPos("AgentCode"), frameFormatSerialChargeReqFeedback.getLen("AgentCode"), (long) this.agentCode);
                        FrameItemType.setString(dataSerialChargeReqFeedback, frameFormatSerialChargeReqFeedback.getPos("EndDate"), ExpectEndDate);
                        FrameItemType.setString(dataSerialChargeReqFeedback, frameFormatSerialChargeReqFeedback.getPos("MAC"), frameFormatSerialChargeReqFeedback.getLen("MAC"), CipherDataMAC);
                        FrameItemType.setString(dataSerialChargeReqFeedback, frameFormatSerialChargeReqFeedback.getPos("ServerEncryptData"), ServerEncryptData);
                        System.out.println("MODULE SEND:" + frameFormatSerialChargeReqFeedback.dump(dataSerialChargeReqFeedback));
                        this.trafficCardReaderEntity.send(dataSerialChargeReqFeedback, 0, dataSerialChargeReqFeedback.length);
                        FrameFormat frameFormatSerialChargeReqFeedbackRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_REQ_FEEDBACK, "00");
                        byte[] dataSerialChargeReqFeedbackRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                        System.out.println("MODULE RECV:" + frameFormatSerialChargeReqFeedbackRes.dump(dataSerialChargeReqFeedbackRes));
                        int times = 0;
                        while (times < 3) {
                            if (dataSerialChargeReqFeedbackRes != null) {
                                responseCode = FrameItemType.getString(dataSerialChargeReqFeedbackRes, frameFormatSerialChargeReqFeedbackRes.getPos("responseCode"), frameFormatSerialChargeReqFeedbackRes.getLen("responseCode"));
                            }
                            if (dataSerialChargeReqFeedbackRes != null && !"AA20".equalsIgnoreCase(responseCode)) {
                                break;
                            }
                            Thread.sleep(3000);
                            this.trafficCardReaderEntity.send(dataSerialChargeReqFeedback, 0, dataSerialChargeReqFeedback.length);
                            dataSerialChargeReqFeedbackRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                            times++;
                        }
                        if (times >= 3 && !"AA20".equalsIgnoreCase(responseCode)) {
                            hsmpResult.put("RESULT", "SUCCESS");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        } else if ("AA34".equalsIgnoreCase(responseCode)) {
                            if ("SUCCESS".equalsIgnoreCase((String) chargeCancel(ISAM, cardNo, TransType.TRANSTYPE_CARD_CHARGE, balance, this.posCommSeq, datetime).get("RESULT"))) {
                                hsmpResult.put(ErrorCode.ERRCODE, "CHARGE_CANCELED");
                                hsmpResult.put("RESULT", "FAILED");
                                hsmpResult.put("ERROR_FLAG", "1357");
                                this.trafficCardServerEntity.disconnect();
                                return hsmpResult;
                            }
                            hsmpResult.put("RESULT", "SUCCESS");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        } else if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                            String hostResCode = FrameItemType.getString(dataSerialChargeReqFeedbackRes, frameFormatSerialChargeReqFeedbackRes.getPos("hostResCode"), frameFormatSerialChargeReqFeedbackRes.getLen("hostResCode"));
                            if ("00".equals(hostResCode)) {
                                hsmpResult.put("RESULT", "SUCCESS");
                                int hostSeq = (int) FrameItemType.getHString2Long(dataSerialChargeReqFeedbackRes, frameFormatSerialChargeReqFeedbackRes.getPos("hostSeq"), frameFormatSerialChargeReqFeedbackRes.getLen("hostSeq"));
                                byte[] bChargeDataRecord = EncryptUtils.hex2byte(FrameItemType.getString(dataSerialChargeReqFeedbackRes, frameFormatSerialChargeReqFeedbackRes.getPos("ChargeDataRecord"), frameFormatSerialChargeReqFeedbackRes.getLen("ChargeDataRecord")));
                                FrameFormat frameFormatChargeDataRecord = FrameDataFormat.getFrameFormat("CHARGE_DATA_RECORD");
                                ExpectEndDate = FrameItemType.getBCD2String(bChargeDataRecord, frameFormatChargeDataRecord.getPos("EndDate"), frameFormatChargeDataRecord.getLen("EndDate"));
                                FrameFormat frameFormatSerialChargeConfirm = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_CONFIRM);
                                byte[] dataSerialChargeConfirm = new byte[frameFormatSerialChargeConfirm.getFrameLen()];
                                FrameItemType.setString(dataSerialChargeConfirm, 0, SerialFrameType.ONLINE_CHARGE_CONFIRM);
                                bArr = dataSerialChargeConfirm;
                                FrameItemType.setString(bArr, frameFormatSerialChargeConfirm.getPos("TransType"), TransType.TRANSTYPE_CARD_CHARGE);
                                FrameItemType.setString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("TransResCode"), hostResCode);
                                FrameItemType.setString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("TransDate"), DateUtils.formatDatetime(date, "yyyyMMdd"));
                                FrameItemType.setString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("TransTime"), DateUtils.formatDatetime(date, "HHmmss"));
                                FrameItemType.setString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("CardNo"), cardNo);
                                FrameItemType.setLong2HString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("TransBalance"), frameFormatSerialChargeConfirm.getLen("TransBalance"), (long) balance);
                                FrameItemType.setLong2HString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("HostTransSeq"), frameFormatSerialChargeConfirm.getLen("HostTransSeq"), (long) hostSeq);
                                FrameItemType.setLong2HString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("PosICSeq"), frameFormatSerialChargeConfirm.getLen("HostTransSeq"), this.posIcSeq);
                                System.out.println("MODULE SEND:" + frameFormatSerialChargeConfirm.dump(dataSerialChargeConfirm));
                                this.trafficCardReaderEntity.send(dataSerialChargeConfirm, 0, dataSerialChargeConfirm.length);
                                FrameFormat frameFormatSerialChargeConfirmRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_CONFIRM, "00");
                                byte[] dataSerialChargeConfirmRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                                System.out.println("MODULE RECV:" + frameFormatSerialChargeConfirmRes.dump(dataSerialChargeConfirmRes));
                                if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(dataSerialChargeConfirmRes, frameFormatSerialChargeConfirmRes.getPos("responseCode"), frameFormatSerialChargeConfirmRes.getLen("responseCode")))) {
                                    MAC = FrameItemType.getString(dataSerialChargeConfirmRes, frameFormatSerialChargeConfirmRes.getPos("MAC"), frameFormatSerialChargeConfirmRes.getLen("MAC"));
                                    EncryptData = FrameItemType.getString(dataSerialChargeConfirmRes, frameFormatSerialChargeConfirmRes.getPos("EncryptData"), frameFormatSerialChargeConfirmRes.getLen("EncryptData"));
                                    FrameFormat frameFormatServerChargeConfirmReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_CONFIRM_REQ, 4);
                                    frameFormatServerChargeConfirmReq.setLen("PlivateMsg", 0);
                                    byte[] dataServerChargeConfirmReq = new byte[frameFormatServerChargeConfirmReq.getFrameLen()];
                                    FrameItemType.setLong2BCD(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("MessageType"), frameFormatServerChargeConfirmReq.getLen("MessageType"), 5005);
                                    FrameItemType.setByte(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("Ver"), (byte) 4);
                                    FrameItemType.setString2BCD(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("SysDatetime"), frameFormatServerChargeConfirmReq.getLen("SysDatetime"), DateUtils.formatDatetime(date, "yyyyMMddHHmmss"));
                                    FrameItemType.setString(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("PosId"), frameFormatServerChargeConfirmReq.getLen("PosId"), this.posId);
                                    FrameItemType.setString2BCD(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("oprId"), frameFormatServerChargeConfirmReq.getLen("oprId"), this.oprId);
                                    FrameItemType.setString2BCD(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("ISAM"), frameFormatServerChargeConfirmReq.getLen("ISAM"), ISAM);
                                    FrameItemType.setString2BCD(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("UnitId"), frameFormatServerChargeConfirmReq.getLen("UnitId"), this.unitId);
                                    FrameItemType.setString2BCD(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("MchntId"), frameFormatServerChargeConfirmReq.getLen("MchntId"), this.mchntId);
                                    FrameItemType.setString2HEX(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("CipherDataMAC"), frameFormatServerChargeConfirmReq.getLen("CipherDataMAC"), MAC);
                                    bArr = dataServerChargeConfirmReq;
                                    FrameItemType.setLong2HEX(bArr, frameFormatServerChargeConfirmReq.getPos("CipherDataLen"), frameFormatServerChargeConfirmReq.getLen("CipherDataLen"), (long) frameFormatServerChargeConfirmReq.getLen("MacBuf"));
                                    FrameItemType.setString2HEX(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("MacBuf"), frameFormatServerChargeConfirmReq.getLen("MacBuf"), EncryptData);
                                    FrameItemType.setString2HEX(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("CSN"), frameFormatServerChargeConfirmReq.getLen("CSN"), CSN);
                                    int aftBal = prevBalance + balance;
                                    FrameItemType.setLong2HEX(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("AftBal"), frameFormatServerChargeConfirmReq.getLen("AftBal"), (long) aftBal);
                                    FrameItemType.setLong2HEX(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("CardCount"), frameFormatServerChargeConfirmReq.getLen("CardCount"), FrameItemType.getHEX2Long(bChargeDataRecord, frameFormatChargeDataRecord.getPos("CardCount"), frameFormatChargeDataRecord.getLen("CardCount")));
                                    FrameItemType.setLong2HEX(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("TxnTAC"), frameFormatServerChargeConfirmReq.getLen("TxnTAC"), FrameItemType.getHEX2Long(bChargeDataRecord, frameFormatChargeDataRecord.getPos("TxnTAC"), frameFormatChargeDataRecord.getLen("TxnTAC")));
                                    bArr = dataServerChargeConfirmReq;
                                    FrameItemType.setLong2BCD(bArr, frameFormatServerChargeConfirmReq.getPos("CardExp"), frameFormatServerChargeConfirmReq.getLen("CardExp"), Long.parseLong(ExpectEndDate));
                                    FrameItemType.setLong2HEX(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("TacType"), frameFormatServerChargeConfirmReq.getLen("TacType"), FrameItemType.getHEX2Long(bChargeDataRecord, frameFormatChargeDataRecord.getPos("EncryptType"), frameFormatChargeDataRecord.getLen("EncryptType")));
                                    FrameItemType.setLong2HEX(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("PsamTransNo"), frameFormatServerChargeConfirmReq.getLen("PsamTransNo"), WalletTransSeq);
                                    FrameItemType.setLong2HEX(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("TxnStatus"), frameFormatServerChargeConfirmReq.getLen("TxnStatus"), 0);
                                    FrameItemType.setLong2HEX(dataServerChargeConfirmReq, frameFormatServerChargeConfirmReq.getPos("PlivateType"), frameFormatServerChargeConfirmReq.getLen("PlivateType"), 8192);
                                    System.out.println("SERVER SEND:" + frameFormatServerChargeConfirmReq.dump(dataServerChargeConfirmReq));
                                    this.trafficCardServerEntity.send(this.dataSeq, dataServerChargeConfirmReq, 0, dataServerChargeConfirmReq.length);
                                    byte[] dataServerChargeConfirmRes = this.trafficCardServerEntity.recv(SERIAL_RES_MAX_TIME);
                                    if (dataServerChargeConfirmRes == null) {
                                        this.trafficCardServerEntity.disconnect();
                                        return hsmpResult;
                                    }
                                    FrameFormat frameFormatServerChargeConfirmRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_CONFIRM_RES, 4);
                                    System.out.println("SERVER RECV:" + frameFormatServerChargeConfirmRes.dump(dataServerChargeConfirmRes));
                                    if (((int) FrameItemType.getBCD2Long(dataServerChargeConfirmRes, frameFormatServerChargeConfirmRes.getPos("MessageType"), frameFormatServerChargeConfirmRes.getLen("MessageType"))) != 5006) {
                                        this.trafficCardServerEntity.disconnect();
                                        return hsmpResult;
                                    }
                                    CipherDataMAC = FrameItemType.getHEX2String(dataServerChargeConfirmRes, frameFormatServerChargeConfirmRes.getPos("CipherDataMAC"), frameFormatServerChargeConfirmRes.getLen("CipherDataMAC"));
                                    ServerEncryptData = EncryptUtils.byte2hex(dataServerChargeConfirmRes, frameFormatServerChargeConfirmRes.getPos("MacBuf"), frameFormatServerChargeConfirmRes.getLen("MacBuf"));
                                    FrameFormat frameFormatSerialChargeConfirmFeedback = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_CONFIRM_FEEDBACK);
                                    byte[] dataSerialChargeConfirmFeedback = new byte[frameFormatSerialChargeConfirmFeedback.getFrameLen()];
                                    FrameItemType.setString(dataSerialChargeConfirmFeedback, 0, SerialFrameType.ONLINE_CHARGE_CONFIRM_FEEDBACK);
                                    FrameItemType.setString(dataSerialChargeConfirmFeedback, frameFormatSerialChargeConfirmFeedback.getPos("MAC"), frameFormatSerialChargeConfirmFeedback.getLen("MAC"), CipherDataMAC);
                                    FrameItemType.setString(dataSerialChargeConfirmFeedback, frameFormatSerialChargeConfirmFeedback.getPos("EncryptData"), ServerEncryptData);
                                    this.trafficCardReaderEntity.send(dataSerialChargeConfirmFeedback, 0, dataSerialChargeConfirmFeedback.length);
                                    FrameFormat frameFormatSerialChargeConfirmFeedbackRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_CONFIRM_FEEDBACK, "00");
                                    if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME), frameFormatSerialChargeConfirmFeedbackRes.getPos("responseCode"), frameFormatSerialChargeConfirmFeedbackRes.getLen("responseCode")))) {
                                        hsmpResult.put("RESULT", "SUCCESS");
                                        this.trafficCardServerEntity.disconnect();
                                        return hsmpResult;
                                    }
                                    this.trafficCardServerEntity.disconnect();
                                    return hsmpResult;
                                }
                                this.trafficCardServerEntity.disconnect();
                                return hsmpResult;
                            }
                            hsmpResult.put(ErrorCode.MODEL_HOSTRESPONSECODE, hostResCode);
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "1372");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        } else {
                            hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "1365");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                    }
                    hsmpResult.put(ErrorCode.ERRCODE, "SERVER_LOST");
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "1257");
                    return hsmpResult;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    hsmpResult.put(ErrorCode.ERRCODE, NetworkUtils.NET_STATE_UNKNOWN);
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "1474");
                    return hsmpResult;
                } finally {
                    this.trafficCardServerEntity.disconnect();
                }
            } else {
                hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "1247");
                return hsmpResult;
            }
        }
        hsmpResult.put(ErrorCode.ERRCODE, "CARD_NOTMATCH");
        hsmpResult.put("RESULT", "FAILED");
        hsmpResult.put("ERROR_FLAG", "1187");
        return hsmpResult;
    }

    public HashMap chargeWriteBack(String cardNo, int balance, String userAcctPassword, String datetime) {
        Date date;
        try {
            date = DateUtils.parseDatetime(datetime);
        } catch (Exception e) {
            date = new Date();
        }
        if (date == null) {
            date = new Date();
        }
        if (userAcctPassword == null) {
            userAcctPassword = "";
        }
        if (userAcctPassword.length() > 14) {
            userAcctPassword = userAcctPassword.substring(0, 14);
        }
        String pkgUserAcctPassword = Integer.toHexString(userAcctPassword.length());
        if (pkgUserAcctPassword.length() == 1) {
            pkgUserAcctPassword = "0" + pkgUserAcctPassword;
        }
        pkgUserAcctPassword = (pkgUserAcctPassword + userAcctPassword + "FFFFFFFFFFFFFFFF").substring(0, 16);
        HashMap hsmpResult = new HashMap();
        HashMap hsmpModel = readModel();
        if (hsmpModel == null) {
            hsmpResult.put(ErrorCode.ERRCODE, "MODEL_UNKNOWN");
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "1506");
            return hsmpResult;
        }
        if (!"SUCCESS".equalsIgnoreCase((String) hsmpModel.get("RESULT"))) {
            return hsmpModel;
        }
        String ISAM = (String) hsmpModel.get("ISAM");
        HashMap hsmpCard = readCard(0);
        if (hsmpCard == null) {
            hsmpResult.put(ErrorCode.ERRCODE, "MODEL_UNKNOWN");
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "1517");
            return hsmpResult;
        }
        if (!"SUCCESS".equalsIgnoreCase((String) hsmpCard.get("RESULT"))) {
            return hsmpCard;
        }
        if (cardNo.equals((String) hsmpCard.get("CardNo"))) {
            String CSN = (String) hsmpCard.get("CSN");
            String EndDate = (String) hsmpCard.get("EndDate");
            String sPrevBalance = (String) hsmpCard.get("Balance");
            String CardType = (String) hsmpCard.get("CardType");
            String PhyCardType = (String) hsmpCard.get("PhyCardType");
            String LastRecord = (String) hsmpCard.get("LastRecord");
            HashMap hsmpCardType = getCardType(CardType, PhyCardType);
            if (hsmpCardType == null) {
                hsmpResult.put(ErrorCode.ERRCODE, "UNSUPPORT");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "1539");
                return hsmpResult;
            }
            String ExpectEndDate;
            int MinChargeAmount = Integer.parseInt((String) hsmpCardType.get("MinChargeAmount"));
            int MaxChargeAmount = Integer.parseInt((String) hsmpCardType.get("MaxChargeAmount"));
            int ChargeAmountUnit = Integer.parseInt((String) hsmpCardType.get("ChargeAmountUnit"));
            int EndDays = Integer.parseInt((String) hsmpCardType.get("EndDays"));
            if (EndDays > 0) {
                ExpectEndDate = DateUtils.formatDatetime(new Date(System.currentTimeMillis() + (86400000 * ((long) EndDays))), "yyyyMMdd");
            } else {
                ExpectEndDate = EndDate;
            }
            int prevBalance = 0;
            if (!StringUtils.isBlank(sPrevBalance)) {
                prevBalance = Integer.parseInt(sPrevBalance);
            }
            if (prevBalance + balance <= 0) {
                hsmpResult.put(ErrorCode.ERRCODE, "NOT_ENOUGH");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "1571");
                return hsmpResult;
            }
            if (StringUtils.isBlank(LastRecord)) {
                LastRecord = "";
            }
            FrameFormat frameFormatSerialChargeWriteBackReq = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ);
            byte[] dataSerialChargeWriteBackReq = new byte[frameFormatSerialChargeWriteBackReq.getFrameLen()];
            FrameItemType.setString(dataSerialChargeWriteBackReq, 0, SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ);
            FrameItemType.setString(dataSerialChargeWriteBackReq, frameFormatSerialChargeWriteBackReq.getPos("CSN"), CSN);
            byte[] bArr = dataSerialChargeWriteBackReq;
            FrameItemType.setString(bArr, frameFormatSerialChargeWriteBackReq.getPos("TransType"), TransType.TRANSTYPE_CARD_CHARGE_WRITEBACK);
            FrameItemType.setLong2HString(dataSerialChargeWriteBackReq, frameFormatSerialChargeWriteBackReq.getPos("TransBalance"), frameFormatSerialChargeWriteBackReq.getLen("TransBalance"), (long) balance);
            this.posCommSeq++;
            FrameItemType.setLong2HString(dataSerialChargeWriteBackReq, frameFormatSerialChargeWriteBackReq.getPos("PosCommSeq"), frameFormatSerialChargeWriteBackReq.getLen("PosCommSeq"), this.posCommSeq);
            FrameItemType.setString(dataSerialChargeWriteBackReq, frameFormatSerialChargeWriteBackReq.getPos("Password"), pkgUserAcctPassword);
            System.out.println("MODULE SEND:" + frameFormatSerialChargeWriteBackReq.dump(dataSerialChargeWriteBackReq));
            this.trafficCardReaderEntity.send(dataSerialChargeWriteBackReq, 0, dataSerialChargeWriteBackReq.length);
            byte[] dataSerialChargeWriteBackReqRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
            if (dataSerialChargeWriteBackReqRes == null) {
                hsmpResult.put(ErrorCode.ERRCODE, "MODEL_NORESPONSE");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "1580");
                return hsmpResult;
            }
            FrameFormat frameFormatSerialChargeWriteBackReqRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ, "00");
            System.out.println("MODULE SEND:" + frameFormatSerialChargeWriteBackReqRes.dump(dataSerialChargeWriteBackReqRes));
            String responseCode = FrameItemType.getString(dataSerialChargeWriteBackReqRes, frameFormatSerialChargeWriteBackReqRes.getPos("responseCode"), frameFormatSerialChargeWriteBackReqRes.getLen("responseCode"));
            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                String AcctEncryptData = FrameItemType.getString(dataSerialChargeWriteBackReqRes, frameFormatSerialChargeWriteBackReqRes.getPos("AcctEncryptData"), frameFormatSerialChargeWriteBackReqRes.getLen("AcctEncryptData"));
                String MAC = FrameItemType.getString(dataSerialChargeWriteBackReqRes, frameFormatSerialChargeWriteBackReqRes.getPos("MAC"), frameFormatSerialChargeWriteBackReqRes.getLen("MAC"));
                String EncryptData = FrameItemType.getString(dataSerialChargeWriteBackReqRes, frameFormatSerialChargeWriteBackReqRes.getPos("EncryptData"), frameFormatSerialChargeWriteBackReqRes.getLen("EncryptData"));
                long WalletTransSeq = FrameItemType.getHString2Long(dataSerialChargeWriteBackReqRes, frameFormatSerialChargeWriteBackReqRes.getPos("WalletTransSeq"), frameFormatSerialChargeWriteBackReqRes.getLen("WalletTransSeq"));
                try {
                    if (this.trafficCardServerEntity.connect(this.ip, this.port)) {
                        FrameFormat frameFormatTxnMsg = FrameDataFormat.getFrameFormat("TxnMsg");
                        frameFormatTxnMsg.setLen("TxnMsg", 8);
                        frameFormatTxnMsg.setLen("TxnCryMsg", 0);
                        frameFormatTxnMsg.setLen("DigitalSign", 0);
                        FrameFormat frameFormatServerChargeWriteBackReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_WRITEBACK_REQ, 4);
                        frameFormatServerChargeWriteBackReq.setLen("TxnMsg", frameFormatTxnMsg.getFrameLen());
                        byte[] dataServerChargeWriteBackReq = new byte[frameFormatServerChargeWriteBackReq.getFrameLen()];
                        FrameItemType.setLong2BCD(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("MessageType"), frameFormatServerChargeWriteBackReq.getLen("MessageType"), 5025);
                        FrameItemType.setByte(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("Ver"), (byte) 4);
                        FrameItemType.setString2BCD(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("SysDatetime"), frameFormatServerChargeWriteBackReq.getLen("SysDatetime"), DateUtils.formatDatetime(date, "yyyyMMddHHmmss"));
                        FrameItemType.setString2BCD(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("oprId"), frameFormatServerChargeWriteBackReq.getLen("oprId"), this.oprId);
                        FrameItemType.setString(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("PosId"), frameFormatServerChargeWriteBackReq.getLen("PosId"), this.posId);
                        FrameItemType.setString2BCD(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("ISAM"), frameFormatServerChargeWriteBackReq.getLen("ISAM"), ISAM);
                        FrameItemType.setString2BCD(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("UnitId"), frameFormatServerChargeWriteBackReq.getLen("UnitId"), this.unitId);
                        FrameItemType.setString2BCD(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("MchntId"), frameFormatServerChargeWriteBackReq.getLen("MchntId"), this.mchntId);
                        bArr = dataServerChargeWriteBackReq;
                        FrameItemType.setLong2HEX(bArr, frameFormatServerChargeWriteBackReq.getPos("BatchNo"), frameFormatServerChargeWriteBackReq.getLen("BatchNo"), (long) this.batchNo);
                        FrameItemType.setString2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("CipherDataMAC"), frameFormatServerChargeWriteBackReq.getLen("CipherDataMAC"), MAC);
                        bArr = dataServerChargeWriteBackReq;
                        FrameItemType.setLong2HEX(bArr, frameFormatServerChargeWriteBackReq.getPos("CipherDataLen"), frameFormatServerChargeWriteBackReq.getLen("CipherDataLen"), (long) frameFormatServerChargeWriteBackReq.getLen("MacBuf"));
                        FrameItemType.setString2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("MacBuf"), frameFormatServerChargeWriteBackReq.getLen("MacBuf"), EncryptData);
                        FrameItemType.setString2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("LastRecord"), frameFormatServerChargeWriteBackReq.getLen("LastRecord"), LastRecord);
                        FrameItemType.setLong2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("TxnMode"), frameFormatServerChargeWriteBackReq.getLen("TxnMode"), 4);
                        bArr = dataServerChargeWriteBackReq;
                        FrameItemType.setLong2HEX(bArr, frameFormatServerChargeWriteBackReq.getPos("TxnMsg") + frameFormatTxnMsg.getPos("TxnMsgLen"), frameFormatTxnMsg.getLen("TxnMsgLen"), (long) frameFormatTxnMsg.getFrameLen());
                        FrameItemType.setLong2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("TxnMsg") + frameFormatTxnMsg.getPos("DSSign1"), frameFormatTxnMsg.getLen("DSSign1"), 0);
                        FrameItemType.setLong2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("TxnMsg") + frameFormatTxnMsg.getPos("DSSign2"), frameFormatTxnMsg.getLen("DSSign2"), 0);
                        FrameItemType.setString2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("TxnMsg") + frameFormatTxnMsg.getPos("TxnMsg"), frameFormatTxnMsg.getLen("TxnMsg"), AcctEncryptData);
                        System.out.println("SERVER SEND:" + frameFormatServerChargeWriteBackReq.dump(dataServerChargeWriteBackReq));
                        this.trafficCardServerEntity.send(this.dataSeq, dataServerChargeWriteBackReq, 0, dataServerChargeWriteBackReq.length);
                        byte[] dataServerChargeWriteBackReqRes = this.trafficCardServerEntity.recv(SERIAL_RES_MAX_TIME);
                        if (dataServerChargeWriteBackReqRes == null) {
                            hsmpResult.put(ErrorCode.ERRCODE, "SERVER_NORESPONSE");
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "1637");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        FrameFormat frameFormatServerChargeWriteBackReqRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_WRITEBACK_RES, 4);
                        System.out.println("SERVER RECV:" + frameFormatServerChargeWriteBackReqRes.dump(dataServerChargeWriteBackReqRes));
                        if (((int) FrameItemType.getBCD2Long(dataServerChargeWriteBackReqRes, frameFormatServerChargeWriteBackReqRes.getPos("MessageType"), frameFormatServerChargeWriteBackReqRes.getLen("MessageType"))) != 5026) {
                            hsmpResult.put(ErrorCode.ERRCODE, "SERVER_COMMERROR");
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "1646");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        this.batchNo = (int) FrameItemType.getHEX2Long(dataServerChargeWriteBackReqRes, frameFormatServerChargeWriteBackReqRes.getPos("BatchNo"), frameFormatServerChargeWriteBackReqRes.getLen("BatchNo"));
                        String CipherDataMAC = FrameItemType.getHEX2String(dataServerChargeWriteBackReqRes, frameFormatServerChargeWriteBackReqRes.getPos("CipherDataMAC"), frameFormatServerChargeWriteBackReqRes.getLen("CipherDataMAC"));
                        String ServerEncryptData = EncryptUtils.byte2hex(dataServerChargeWriteBackReqRes, frameFormatServerChargeWriteBackReqRes.getPos("MacBuf"), frameFormatServerChargeWriteBackReqRes.getLen("MacBuf")).toUpperCase();
                        FrameFormat frameFormatSerialChargeWriteBackReqFeedback = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ_FEEDBACK);
                        this.posIcSeq++;
                        byte[] dataSerialChargeWriteBackReqFeedback = new byte[frameFormatSerialChargeWriteBackReqFeedback.getFrameLen()];
                        FrameItemType.setString(dataSerialChargeWriteBackReqFeedback, 0, SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ_FEEDBACK);
                        FrameItemType.setLong2HString(dataSerialChargeWriteBackReqFeedback, frameFormatSerialChargeWriteBackReqFeedback.getPos("PosIcSeq"), frameFormatSerialChargeWriteBackReqFeedback.getLen("PosIcSeq"), this.posIcSeq);
                        FrameItemType.setString(dataSerialChargeWriteBackReqFeedback, frameFormatSerialChargeWriteBackReqFeedback.getPos("OprId"), this.oprId);
                        bArr = dataSerialChargeWriteBackReqFeedback;
                        FrameItemType.setLong2BString(bArr, frameFormatSerialChargeWriteBackReqFeedback.getPos("AgentCode"), frameFormatSerialChargeWriteBackReqFeedback.getLen("AgentCode"), (long) this.agentCode);
                        FrameItemType.setString(dataSerialChargeWriteBackReqFeedback, frameFormatSerialChargeWriteBackReqFeedback.getPos("EndDate"), ExpectEndDate);
                        FrameItemType.setString(dataSerialChargeWriteBackReqFeedback, frameFormatSerialChargeWriteBackReqFeedback.getPos("MAC"), frameFormatSerialChargeWriteBackReqFeedback.getLen("MAC"), CipherDataMAC);
                        FrameItemType.setString(dataSerialChargeWriteBackReqFeedback, frameFormatSerialChargeWriteBackReqFeedback.getPos("ServerEncryptData"), ServerEncryptData);
                        System.out.println("MODULE SEND:" + frameFormatSerialChargeWriteBackReqFeedback.dump(dataSerialChargeWriteBackReqFeedback));
                        this.trafficCardReaderEntity.send(dataSerialChargeWriteBackReqFeedback, 0, dataSerialChargeWriteBackReqFeedback.length);
                        FrameFormat frameFormatSerialChargeWriteBackReqFeedbackRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ_FEEDBACK, "00");
                        byte[] dataSerialChargeWriteBackReqFeedbackRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                        System.out.println("MODULE RECV:" + frameFormatSerialChargeWriteBackReqFeedbackRes.dump(dataSerialChargeWriteBackReqFeedbackRes));
                        int times = 0;
                        while (times < 3) {
                            if (dataSerialChargeWriteBackReqFeedbackRes != null) {
                                responseCode = FrameItemType.getString(dataSerialChargeWriteBackReqFeedbackRes, frameFormatSerialChargeWriteBackReqFeedbackRes.getPos("responseCode"), frameFormatSerialChargeWriteBackReqFeedbackRes.getLen("responseCode"));
                            }
                            if (dataSerialChargeWriteBackReqFeedbackRes != null && !"AA20".equalsIgnoreCase(responseCode)) {
                                break;
                            }
                            Thread.sleep(3000);
                            this.trafficCardReaderEntity.send(dataSerialChargeWriteBackReqFeedback, 0, dataSerialChargeWriteBackReqFeedback.length);
                            dataSerialChargeWriteBackReqFeedbackRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                            times++;
                        }
                        if (times >= 3 && !"AA20".equalsIgnoreCase(responseCode)) {
                            hsmpResult.put("RESULT", "SUCCESS");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        } else if ("AA34".equalsIgnoreCase(responseCode)) {
                            if ("SUCCESS".equalsIgnoreCase((String) chargeCancel(ISAM, cardNo, TransType.TRANSTYPE_CARD_CHARGE_WRITEBACK, balance, this.posCommSeq, datetime).get("RESULT"))) {
                                hsmpResult.put(ErrorCode.ERRCODE, "CHARGE_CANCELED");
                                hsmpResult.put("RESULT", "FAILED");
                                hsmpResult.put("ERROR_FLAG", "1700");
                                this.trafficCardServerEntity.disconnect();
                                return hsmpResult;
                            }
                            hsmpResult.put("RESULT", "SUCCESS");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        } else {
                            responseCode = FrameItemType.getString(dataSerialChargeWriteBackReqFeedbackRes, frameFormatSerialChargeWriteBackReqFeedbackRes.getPos("responseCode"), frameFormatSerialChargeWriteBackReqFeedbackRes.getLen("responseCode"));
                            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                                String hostResCode = FrameItemType.getString(dataSerialChargeWriteBackReqFeedbackRes, frameFormatSerialChargeWriteBackReqFeedbackRes.getPos("hostResCode"), frameFormatSerialChargeWriteBackReqFeedbackRes.getLen("hostResCode"));
                                if ("00".equals(hostResCode)) {
                                    hsmpResult.put("RESULT", "SUCCESS");
                                    int hostSeq = (int) FrameItemType.getHString2Long(dataSerialChargeWriteBackReqFeedbackRes, frameFormatSerialChargeWriteBackReqFeedbackRes.getPos("hostSeq"), frameFormatSerialChargeWriteBackReqFeedbackRes.getLen("hostSeq"));
                                    byte[] bChargeDataRecord = EncryptUtils.hex2byte(FrameItemType.getString(dataSerialChargeWriteBackReqFeedbackRes, frameFormatSerialChargeWriteBackReqFeedbackRes.getPos("ChargeDataRecord"), frameFormatSerialChargeWriteBackReqFeedbackRes.getLen("ChargeDataRecord")));
                                    FrameFormat frameFormatChargeDataRecord = FrameDataFormat.getFrameFormat("CHARGE_DATA_RECORD");
                                    ExpectEndDate = FrameItemType.getBCD2String(bChargeDataRecord, frameFormatChargeDataRecord.getPos("EndDate"), frameFormatChargeDataRecord.getLen("EndDate"));
                                    FrameFormat frameFormatSerialChargeWriteBackConfirm = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM);
                                    byte[] dataSerialChargeWriteBackConfirm = new byte[frameFormatSerialChargeWriteBackConfirm.getFrameLen()];
                                    FrameItemType.setString(dataSerialChargeWriteBackConfirm, 0, SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM);
                                    bArr = dataSerialChargeWriteBackConfirm;
                                    FrameItemType.setString(bArr, frameFormatSerialChargeWriteBackConfirm.getPos("TransType"), TransType.TRANSTYPE_CARD_CHARGE_WRITEBACK);
                                    FrameItemType.setString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("TransResCode"), hostResCode);
                                    FrameItemType.setString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("TransDate"), DateUtils.formatDatetime(date, "yyyyMMdd"));
                                    FrameItemType.setString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("TransTime"), DateUtils.formatDatetime(date, "HHmmss"));
                                    FrameItemType.setString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("CardNo"), cardNo);
                                    FrameItemType.setLong2HString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("TransBalance"), frameFormatSerialChargeWriteBackConfirm.getLen("TransBalance"), (long) balance);
                                    FrameItemType.setLong2HString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("HostTransSeq"), frameFormatSerialChargeWriteBackConfirm.getLen("HostTransSeq"), (long) hostSeq);
                                    FrameItemType.setLong2HString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("PosICSeq"), frameFormatSerialChargeWriteBackConfirm.getLen("HostTransSeq"), this.posIcSeq);
                                    System.out.println("MODULE SEND:" + frameFormatSerialChargeWriteBackConfirm.dump(dataSerialChargeWriteBackConfirm));
                                    this.trafficCardReaderEntity.send(dataSerialChargeWriteBackConfirm, 0, dataSerialChargeWriteBackConfirm.length);
                                    FrameFormat frameFormatSerialChargeWriteBackConfirmRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM, "00");
                                    byte[] dataSerialChargeWriteBackConfirmRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                                    System.out.println("MODULE RECV:" + frameFormatSerialChargeWriteBackConfirmRes.dump(dataSerialChargeWriteBackConfirmRes));
                                    if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(dataSerialChargeWriteBackConfirmRes, frameFormatSerialChargeWriteBackConfirmRes.getPos("responseCode"), frameFormatSerialChargeWriteBackConfirmRes.getLen("responseCode")))) {
                                        MAC = FrameItemType.getString(dataSerialChargeWriteBackConfirmRes, frameFormatSerialChargeWriteBackConfirmRes.getPos("MAC"), frameFormatSerialChargeWriteBackConfirmRes.getLen("MAC"));
                                        EncryptData = FrameItemType.getString(dataSerialChargeWriteBackConfirmRes, frameFormatSerialChargeWriteBackConfirmRes.getPos("EncryptData"), frameFormatSerialChargeWriteBackConfirmRes.getLen("EncryptData"));
                                        FrameFormat frameFormatServerChargeWriteBackConfirmReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_WRITEBACK_CONFIRM_REQ, 4);
                                        frameFormatServerChargeWriteBackConfirmReq.setLen("PlivateMsg", 0);
                                        byte[] dataServerChargeWriteBackConfirmReq = new byte[frameFormatServerChargeWriteBackConfirmReq.getFrameLen()];
                                        FrameItemType.setLong2BCD(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("MessageType"), frameFormatServerChargeWriteBackConfirmReq.getLen("MessageType"), 5027);
                                        FrameItemType.setByte(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("Ver"), (byte) 4);
                                        FrameItemType.setString2BCD(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("SysDatetime"), frameFormatServerChargeWriteBackConfirmReq.getLen("SysDatetime"), DateUtils.formatDatetime(date, "yyyyMMddHHmmss"));
                                        FrameItemType.setString(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("PosId"), frameFormatServerChargeWriteBackConfirmReq.getLen("PosId"), this.posId);
                                        FrameItemType.setString2BCD(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("oprId"), frameFormatServerChargeWriteBackConfirmReq.getLen("oprId"), this.oprId);
                                        FrameItemType.setString2BCD(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("ISAM"), frameFormatServerChargeWriteBackConfirmReq.getLen("ISAM"), ISAM);
                                        FrameItemType.setString2BCD(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("UnitId"), frameFormatServerChargeWriteBackConfirmReq.getLen("UnitId"), this.unitId);
                                        FrameItemType.setString2BCD(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("MchntId"), frameFormatServerChargeWriteBackConfirmReq.getLen("MchntId"), this.mchntId);
                                        FrameItemType.setString2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("CipherDataMAC"), frameFormatServerChargeWriteBackConfirmReq.getLen("CipherDataMAC"), MAC);
                                        bArr = dataServerChargeWriteBackConfirmReq;
                                        FrameItemType.setLong2HEX(bArr, frameFormatServerChargeWriteBackConfirmReq.getPos("CipherDataLen"), frameFormatServerChargeWriteBackConfirmReq.getLen("CipherDataLen"), (long) frameFormatServerChargeWriteBackConfirmReq.getLen("MacBuf"));
                                        FrameItemType.setString2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("MacBuf"), frameFormatServerChargeWriteBackConfirmReq.getLen("MacBuf"), EncryptData);
                                        FrameItemType.setString2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("CSN"), frameFormatServerChargeWriteBackConfirmReq.getLen("CSN"), CSN);
                                        int aftBal = prevBalance + balance;
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("AftBal"), frameFormatServerChargeWriteBackConfirmReq.getLen("AftBal"), (long) aftBal);
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("CardCount"), frameFormatServerChargeWriteBackConfirmReq.getLen("CardCount"), FrameItemType.getHEX2Long(bChargeDataRecord, frameFormatChargeDataRecord.getPos("CardCount"), frameFormatChargeDataRecord.getLen("CardCount")));
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("TxnTAC"), frameFormatServerChargeWriteBackConfirmReq.getLen("TxnTAC"), FrameItemType.getHEX2Long(bChargeDataRecord, frameFormatChargeDataRecord.getPos("TxnTAC"), frameFormatChargeDataRecord.getLen("TxnTAC")));
                                        bArr = dataServerChargeWriteBackConfirmReq;
                                        FrameItemType.setLong2BCD(bArr, frameFormatServerChargeWriteBackConfirmReq.getPos("CardExp"), frameFormatServerChargeWriteBackConfirmReq.getLen("CardExp"), Long.parseLong(ExpectEndDate));
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("TacType"), frameFormatServerChargeWriteBackConfirmReq.getLen("TacType"), FrameItemType.getHEX2Long(bChargeDataRecord, frameFormatChargeDataRecord.getPos("EncryptType"), frameFormatChargeDataRecord.getLen("EncryptType")));
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("PsamTransNo"), frameFormatServerChargeWriteBackConfirmReq.getLen("PsamTransNo"), WalletTransSeq);
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("TxnStatus"), frameFormatServerChargeWriteBackConfirmReq.getLen("TxnStatus"), 0);
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("PlivateType"), frameFormatServerChargeWriteBackConfirmReq.getLen("PlivateType"), 8192);
                                        System.out.println("SERVER SEND:" + frameFormatServerChargeWriteBackConfirmReq.dump(dataServerChargeWriteBackConfirmReq));
                                        this.trafficCardServerEntity.send(this.dataSeq, dataServerChargeWriteBackConfirmReq, 0, dataServerChargeWriteBackConfirmReq.length);
                                        byte[] dataServerChargeWriteBackConfirmRes = this.trafficCardServerEntity.recv(SERIAL_RES_MAX_TIME);
                                        if (dataServerChargeWriteBackConfirmRes == null) {
                                            this.trafficCardServerEntity.disconnect();
                                            return hsmpResult;
                                        }
                                        FrameFormat frameFormatServerChargeWriteBackConfirmRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_WRITEBACK_CONFIRM_RES, 4);
                                        System.out.println("SERVER RECV:" + frameFormatServerChargeWriteBackConfirmRes.dump(dataServerChargeWriteBackConfirmRes));
                                        if (((int) FrameItemType.getBCD2Long(dataServerChargeWriteBackConfirmRes, frameFormatServerChargeWriteBackConfirmRes.getPos("MessageType"), frameFormatServerChargeWriteBackConfirmRes.getLen("MessageType"))) != 5028) {
                                            this.trafficCardServerEntity.disconnect();
                                            return hsmpResult;
                                        }
                                        CipherDataMAC = FrameItemType.getHEX2String(dataServerChargeWriteBackConfirmRes, frameFormatServerChargeWriteBackConfirmRes.getPos("CipherDataMAC"), frameFormatServerChargeWriteBackConfirmRes.getLen("CipherDataMAC"));
                                        ServerEncryptData = EncryptUtils.byte2hex(dataServerChargeWriteBackConfirmRes, frameFormatServerChargeWriteBackConfirmRes.getPos("MacBuf"), frameFormatServerChargeWriteBackConfirmRes.getLen("MacBuf"));
                                        FrameFormat frameFormatSerialChargeWriteBackConfirmFeedback = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM_FEEDBACK);
                                        byte[] dataSerialChargeWriteBackConfirmFeedback = new byte[frameFormatSerialChargeWriteBackConfirmFeedback.getFrameLen()];
                                        FrameItemType.setString(dataSerialChargeWriteBackConfirmFeedback, 0, SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM_FEEDBACK);
                                        FrameItemType.setString(dataSerialChargeWriteBackConfirmFeedback, frameFormatSerialChargeWriteBackConfirmFeedback.getPos("MAC"), frameFormatSerialChargeWriteBackConfirmFeedback.getLen("MAC"), CipherDataMAC);
                                        FrameItemType.setString(dataSerialChargeWriteBackConfirmFeedback, frameFormatSerialChargeWriteBackConfirmFeedback.getPos("EncryptData"), ServerEncryptData);
                                        this.trafficCardReaderEntity.send(dataSerialChargeWriteBackConfirmFeedback, 0, dataSerialChargeWriteBackConfirmFeedback.length);
                                        FrameFormat frameFormatSerialChargeWriteBackConfirmFeedbackRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM_FEEDBACK, "00");
                                        if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME), frameFormatSerialChargeWriteBackConfirmFeedbackRes.getPos("responseCode"), frameFormatSerialChargeWriteBackConfirmFeedbackRes.getLen("responseCode")))) {
                                            hsmpResult.put("RESULT", "SUCCESS");
                                            this.trafficCardServerEntity.disconnect();
                                            return hsmpResult;
                                        }
                                        this.trafficCardServerEntity.disconnect();
                                        return hsmpResult;
                                    }
                                    this.trafficCardServerEntity.disconnect();
                                    return hsmpResult;
                                }
                                hsmpResult.put(ErrorCode.MODEL_HOSTRESPONSECODE, hostResCode);
                                hsmpResult.put("RESULT", "FAILED");
                                hsmpResult.put("ERROR_FLAG", "1715");
                                this.trafficCardServerEntity.disconnect();
                                return hsmpResult;
                            }
                            hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "1708");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                    }
                    hsmpResult.put(ErrorCode.ERRCODE, "SERVER_LOST");
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "1600");
                    return hsmpResult;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    hsmpResult.put(ErrorCode.ERRCODE, NetworkUtils.NET_STATE_UNKNOWN);
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "1816");
                    return hsmpResult;
                } finally {
                    this.trafficCardServerEntity.disconnect();
                }
            } else {
                hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "1589");
                return hsmpResult;
            }
        }
        hsmpResult.put(ErrorCode.ERRCODE, "CARD_NOTMATCH");
        hsmpResult.put("RESULT", "FAILED");
        hsmpResult.put("ERROR_FLAG", "1526");
        return hsmpResult;
    }

    public HashMap chargeScrashCard(String cardNo, int balance, String CardPassword, String datetime) {
        Date date;
        try {
            date = DateUtils.parseDatetime(datetime);
        } catch (Exception e) {
            date = new Date();
        }
        if (date == null) {
            date = new Date();
        }
        String pkgUserAcctPassword = "00FFFFFFFFFFFFFF";
        HashMap hsmpResult = new HashMap();
        HashMap hsmpModel = readModel();
        if (hsmpModel == null) {
            hsmpResult.put(ErrorCode.ERRCODE, "MODEL_UNKNOWN");
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "1836");
            return hsmpResult;
        }
        if (!"SUCCESS".equalsIgnoreCase((String) hsmpModel.get("RESULT"))) {
            return hsmpModel;
        }
        String ISAM = (String) hsmpModel.get("ISAM");
        HashMap hsmpCard = readCard(0);
        if (hsmpCard == null) {
            hsmpResult.put(ErrorCode.ERRCODE, "MODEL_UNKNOWN");
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "1847");
            return hsmpResult;
        }
        if (!"SUCCESS".equalsIgnoreCase((String) hsmpCard.get("RESULT"))) {
            return hsmpCard;
        }
        if (cardNo.equals((String) hsmpCard.get("CardNo"))) {
            String CSN = (String) hsmpCard.get("CSN");
            String EndDate = (String) hsmpCard.get("EndDate");
            String sPrevBalance = (String) hsmpCard.get("Balance");
            String CardType = (String) hsmpCard.get("CardType");
            String PhyCardType = (String) hsmpCard.get("PhyCardType");
            String LastRecord = (String) hsmpCard.get("LastRecord");
            HashMap hsmpCardType = getCardType(CardType, PhyCardType);
            if (hsmpCardType == null) {
                hsmpResult.put(ErrorCode.ERRCODE, "UNSUPPORT");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "1869");
                return hsmpResult;
            }
            String ExpectEndDate;
            int MinChargeAmount = Integer.parseInt((String) hsmpCardType.get("MinChargeAmount"));
            int MaxChargeAmount = Integer.parseInt((String) hsmpCardType.get("MaxChargeAmount"));
            int ChargeAmountUnit = Integer.parseInt((String) hsmpCardType.get("ChargeAmountUnit"));
            int EndDays = Integer.parseInt((String) hsmpCardType.get("EndDays"));
            if (EndDays > 0) {
                ExpectEndDate = DateUtils.formatDatetime(new Date(System.currentTimeMillis() + (86400000 * ((long) EndDays))), "yyyyMMdd");
            } else {
                ExpectEndDate = EndDate;
            }
            int prevBalance = 0;
            if (!StringUtils.isBlank(sPrevBalance)) {
                prevBalance = Integer.parseInt(sPrevBalance);
            }
            if (prevBalance + balance <= 0) {
                hsmpResult.put(ErrorCode.ERRCODE, "NOT_ENOUGH");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "1907");
                return hsmpResult;
            }
            if (StringUtils.isBlank(LastRecord)) {
                LastRecord = "";
            }
            FrameFormat frameFormatSerialChargeWriteBackReq = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ);
            byte[] dataSerialChargeWriteBackReq = new byte[frameFormatSerialChargeWriteBackReq.getFrameLen()];
            FrameItemType.setString(dataSerialChargeWriteBackReq, 0, SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ);
            FrameItemType.setString(dataSerialChargeWriteBackReq, frameFormatSerialChargeWriteBackReq.getPos("CSN"), CSN);
            byte[] bArr = dataSerialChargeWriteBackReq;
            FrameItemType.setString(bArr, frameFormatSerialChargeWriteBackReq.getPos("TransType"), TransType.TRANSTYPE_CARD_CHARGE_SCRASHCARD);
            FrameItemType.setLong2HString(dataSerialChargeWriteBackReq, frameFormatSerialChargeWriteBackReq.getPos("TransBalance"), frameFormatSerialChargeWriteBackReq.getLen("TransBalance"), (long) balance);
            this.posCommSeq++;
            FrameItemType.setLong2HString(dataSerialChargeWriteBackReq, frameFormatSerialChargeWriteBackReq.getPos("PosCommSeq"), frameFormatSerialChargeWriteBackReq.getLen("PosCommSeq"), this.posCommSeq);
            FrameItemType.setString(dataSerialChargeWriteBackReq, frameFormatSerialChargeWriteBackReq.getPos("Password"), pkgUserAcctPassword);
            System.out.println("MODULE SEND:" + frameFormatSerialChargeWriteBackReq.dump(dataSerialChargeWriteBackReq));
            this.trafficCardReaderEntity.send(dataSerialChargeWriteBackReq, 0, dataSerialChargeWriteBackReq.length);
            byte[] dataSerialChargeWriteBackReqRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
            if (dataSerialChargeWriteBackReqRes == null) {
                hsmpResult.put(ErrorCode.ERRCODE, "MODEL_NORESPONSE");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "1910");
                return hsmpResult;
            }
            FrameFormat frameFormatSerialChargeWriteBackReqRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ, "00");
            System.out.println("MODULE SEND:" + frameFormatSerialChargeWriteBackReqRes.dump(dataSerialChargeWriteBackReqRes));
            String responseCode = FrameItemType.getString(dataSerialChargeWriteBackReqRes, frameFormatSerialChargeWriteBackReqRes.getPos("responseCode"), frameFormatSerialChargeWriteBackReqRes.getLen("responseCode"));
            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                String AcctEncryptData = FrameItemType.getString(dataSerialChargeWriteBackReqRes, frameFormatSerialChargeWriteBackReqRes.getPos("AcctEncryptData"), frameFormatSerialChargeWriteBackReqRes.getLen("AcctEncryptData"));
                String MAC = FrameItemType.getString(dataSerialChargeWriteBackReqRes, frameFormatSerialChargeWriteBackReqRes.getPos("MAC"), frameFormatSerialChargeWriteBackReqRes.getLen("MAC"));
                String EncryptData = FrameItemType.getString(dataSerialChargeWriteBackReqRes, frameFormatSerialChargeWriteBackReqRes.getPos("EncryptData"), frameFormatSerialChargeWriteBackReqRes.getLen("EncryptData"));
                long WalletTransSeq = FrameItemType.getHString2Long(dataSerialChargeWriteBackReqRes, frameFormatSerialChargeWriteBackReqRes.getPos("WalletTransSeq"), frameFormatSerialChargeWriteBackReqRes.getLen("WalletTransSeq"));
                try {
                    if (this.trafficCardServerEntity.connect(this.ip, this.port)) {
                        FrameFormat frameFormatTxnMsg = FrameDataFormat.getFrameFormat("TxnMsg");
                        frameFormatTxnMsg.setLen("TxnMsg", 8);
                        frameFormatTxnMsg.setLen("TxnCryMsg", 0);
                        frameFormatTxnMsg.setLen("DigitalSign", 0);
                        FrameFormat frameFormatServerChargeWriteBackReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_WRITEBACK_REQ, 4);
                        frameFormatServerChargeWriteBackReq.setLen("TxnMsg", frameFormatTxnMsg.getFrameLen());
                        byte[] dataServerChargeWriteBackReq = new byte[frameFormatServerChargeWriteBackReq.getFrameLen()];
                        FrameItemType.setLong2BCD(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("MessageType"), frameFormatServerChargeWriteBackReq.getLen("MessageType"), 5025);
                        FrameItemType.setByte(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("Ver"), (byte) 4);
                        FrameItemType.setString2BCD(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("SysDatetime"), frameFormatServerChargeWriteBackReq.getLen("SysDatetime"), DateUtils.formatDatetime(date, "yyyyMMddHHmmss"));
                        FrameItemType.setString2BCD(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("oprId"), frameFormatServerChargeWriteBackReq.getLen("oprId"), this.oprId);
                        FrameItemType.setString(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("PosId"), frameFormatServerChargeWriteBackReq.getLen("PosId"), this.posId);
                        FrameItemType.setString2BCD(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("ISAM"), frameFormatServerChargeWriteBackReq.getLen("ISAM"), ISAM);
                        FrameItemType.setString2BCD(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("UnitId"), frameFormatServerChargeWriteBackReq.getLen("UnitId"), this.unitId);
                        FrameItemType.setString2BCD(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("MchntId"), frameFormatServerChargeWriteBackReq.getLen("MchntId"), this.mchntId);
                        bArr = dataServerChargeWriteBackReq;
                        FrameItemType.setLong2HEX(bArr, frameFormatServerChargeWriteBackReq.getPos("BatchNo"), frameFormatServerChargeWriteBackReq.getLen("BatchNo"), (long) this.batchNo);
                        FrameItemType.setString2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("CipherDataMAC"), frameFormatServerChargeWriteBackReq.getLen("CipherDataMAC"), MAC);
                        bArr = dataServerChargeWriteBackReq;
                        FrameItemType.setLong2HEX(bArr, frameFormatServerChargeWriteBackReq.getPos("CipherDataLen"), frameFormatServerChargeWriteBackReq.getLen("CipherDataLen"), (long) frameFormatServerChargeWriteBackReq.getLen("MacBuf"));
                        FrameItemType.setString2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("MacBuf"), frameFormatServerChargeWriteBackReq.getLen("MacBuf"), EncryptData);
                        FrameItemType.setString2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("LastRecord"), frameFormatServerChargeWriteBackReq.getLen("LastRecord"), LastRecord);
                        FrameItemType.setLong2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("TxnMode"), frameFormatServerChargeWriteBackReq.getLen("TxnMode"), 6);
                        bArr = dataServerChargeWriteBackReq;
                        FrameItemType.setLong2HEX(bArr, frameFormatServerChargeWriteBackReq.getPos("TxnMsg") + frameFormatTxnMsg.getPos("TxnMsgLen"), frameFormatTxnMsg.getLen("TxnMsgLen"), (long) frameFormatTxnMsg.getFrameLen());
                        FrameItemType.setLong2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("TxnMsg") + frameFormatTxnMsg.getPos("DSSign1"), frameFormatTxnMsg.getLen("DSSign1"), 0);
                        FrameItemType.setLong2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("TxnMsg") + frameFormatTxnMsg.getPos("DSSign2"), frameFormatTxnMsg.getLen("DSSign2"), 0);
                        String adjCardPassword = (CardPassword + "FFFFFFFFFFFFFFFF").substring(0, 16);
                        FrameItemType.setString2HEX(dataServerChargeWriteBackReq, frameFormatServerChargeWriteBackReq.getPos("TxnMsg") + frameFormatTxnMsg.getPos("TxnMsg"), frameFormatTxnMsg.getLen("TxnMsg"), adjCardPassword);
                        System.out.println("SERVER SEND:" + frameFormatServerChargeWriteBackReq.dump(dataServerChargeWriteBackReq));
                        this.trafficCardServerEntity.send(this.dataSeq, dataServerChargeWriteBackReq, 0, dataServerChargeWriteBackReq.length);
                        byte[] dataServerChargeWriteBackReqRes = this.trafficCardServerEntity.recv(SERIAL_RES_MAX_TIME);
                        if (dataServerChargeWriteBackReqRes == null) {
                            hsmpResult.put(ErrorCode.ERRCODE, "SERVER_NORESPONSE");
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "1969");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        FrameFormat frameFormatServerChargeWriteBackReqRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_WRITEBACK_RES, 4);
                        System.out.println("SERVER RECV:" + frameFormatServerChargeWriteBackReqRes.dump(dataServerChargeWriteBackReqRes));
                        if (((int) FrameItemType.getBCD2Long(dataServerChargeWriteBackReqRes, frameFormatServerChargeWriteBackReqRes.getPos("MessageType"), frameFormatServerChargeWriteBackReqRes.getLen("MessageType"))) != 5026) {
                            hsmpResult.put(ErrorCode.ERRCODE, "SERVER_COMMERROR");
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "1978");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        this.batchNo = (int) FrameItemType.getHEX2Long(dataServerChargeWriteBackReqRes, frameFormatServerChargeWriteBackReqRes.getPos("BatchNo"), frameFormatServerChargeWriteBackReqRes.getLen("BatchNo"));
                        String CipherDataMAC = FrameItemType.getHEX2String(dataServerChargeWriteBackReqRes, frameFormatServerChargeWriteBackReqRes.getPos("CipherDataMAC"), frameFormatServerChargeWriteBackReqRes.getLen("CipherDataMAC"));
                        String ServerEncryptData = EncryptUtils.byte2hex(dataServerChargeWriteBackReqRes, frameFormatServerChargeWriteBackReqRes.getPos("MacBuf"), frameFormatServerChargeWriteBackReqRes.getLen("MacBuf")).toUpperCase();
                        FrameFormat frameFormatSerialChargeWriteBackReqFeedback = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ_FEEDBACK);
                        this.posIcSeq++;
                        byte[] dataSerialChargeWriteBackReqFeedback = new byte[frameFormatSerialChargeWriteBackReqFeedback.getFrameLen()];
                        FrameItemType.setString(dataSerialChargeWriteBackReqFeedback, 0, SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ_FEEDBACK);
                        FrameItemType.setLong2HString(dataSerialChargeWriteBackReqFeedback, frameFormatSerialChargeWriteBackReqFeedback.getPos("PosIcSeq"), frameFormatSerialChargeWriteBackReqFeedback.getLen("PosIcSeq"), this.posIcSeq);
                        FrameItemType.setString(dataSerialChargeWriteBackReqFeedback, frameFormatSerialChargeWriteBackReqFeedback.getPos("OprId"), this.oprId);
                        bArr = dataSerialChargeWriteBackReqFeedback;
                        FrameItemType.setLong2BString(bArr, frameFormatSerialChargeWriteBackReqFeedback.getPos("AgentCode"), frameFormatSerialChargeWriteBackReqFeedback.getLen("AgentCode"), (long) this.agentCode);
                        FrameItemType.setString(dataSerialChargeWriteBackReqFeedback, frameFormatSerialChargeWriteBackReqFeedback.getPos("EndDate"), ExpectEndDate);
                        FrameItemType.setString(dataSerialChargeWriteBackReqFeedback, frameFormatSerialChargeWriteBackReqFeedback.getPos("MAC"), frameFormatSerialChargeWriteBackReqFeedback.getLen("MAC"), CipherDataMAC);
                        FrameItemType.setString(dataSerialChargeWriteBackReqFeedback, frameFormatSerialChargeWriteBackReqFeedback.getPos("ServerEncryptData"), ServerEncryptData);
                        System.out.println("MODULE SEND:" + frameFormatSerialChargeWriteBackReqFeedback.dump(dataSerialChargeWriteBackReqFeedback));
                        this.trafficCardReaderEntity.send(dataSerialChargeWriteBackReqFeedback, 0, dataSerialChargeWriteBackReqFeedback.length);
                        FrameFormat frameFormatSerialChargeWriteBackReqFeedbackRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ_FEEDBACK, "00");
                        byte[] dataSerialChargeWriteBackReqFeedbackRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                        System.out.println("MODULE RECV:" + frameFormatSerialChargeWriteBackReqFeedbackRes.dump(dataSerialChargeWriteBackReqFeedbackRes));
                        int times = 0;
                        while (times < 3) {
                            if (dataSerialChargeWriteBackReqFeedbackRes != null) {
                                responseCode = FrameItemType.getString(dataSerialChargeWriteBackReqFeedbackRes, frameFormatSerialChargeWriteBackReqFeedbackRes.getPos("responseCode"), frameFormatSerialChargeWriteBackReqFeedbackRes.getLen("responseCode"));
                            }
                            if (dataSerialChargeWriteBackReqFeedbackRes != null && !"AA20".equalsIgnoreCase(responseCode)) {
                                break;
                            }
                            Thread.sleep(3000);
                            this.trafficCardReaderEntity.send(dataSerialChargeWriteBackReqFeedback, 0, dataSerialChargeWriteBackReqFeedback.length);
                            dataSerialChargeWriteBackReqFeedbackRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                            times++;
                        }
                        if (times >= 3 && !"AA20".equalsIgnoreCase(responseCode)) {
                            hsmpResult.put("RESULT", "SUCCESS");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        } else if ("AA34".equalsIgnoreCase(responseCode)) {
                            if ("SUCCESS".equalsIgnoreCase((String) chargeCancel(ISAM, cardNo, TransType.TRANSTYPE_CARD_CHARGE_SCRASHCARD, balance, this.posCommSeq, datetime).get("RESULT"))) {
                                hsmpResult.put(ErrorCode.ERRCODE, "CHARGE_CANCELED");
                                hsmpResult.put("RESULT", "FAILED");
                                hsmpResult.put("ERROR_FLAG", "2032");
                                this.trafficCardServerEntity.disconnect();
                                return hsmpResult;
                            }
                            hsmpResult.put("RESULT", "SUCCESS");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        } else {
                            responseCode = FrameItemType.getString(dataSerialChargeWriteBackReqFeedbackRes, frameFormatSerialChargeWriteBackReqFeedbackRes.getPos("responseCode"), frameFormatSerialChargeWriteBackReqFeedbackRes.getLen("responseCode"));
                            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                                String hostResCode = FrameItemType.getString(dataSerialChargeWriteBackReqFeedbackRes, frameFormatSerialChargeWriteBackReqFeedbackRes.getPos("hostResCode"), frameFormatSerialChargeWriteBackReqFeedbackRes.getLen("hostResCode"));
                                if ("C0".equalsIgnoreCase(hostResCode)) {
                                    hsmpResult.put(ErrorCode.ERRCODE, "CARD_USED");
                                    hsmpResult.put("RESULT", "FAILED");
                                    hsmpResult.put("ERROR_FLAG", "2047");
                                    this.trafficCardServerEntity.disconnect();
                                    return hsmpResult;
                                } else if ("BA".equalsIgnoreCase(hostResCode)) {
                                    hsmpResult.put(ErrorCode.ERRCODE, "CARD_PWDERROR");
                                    hsmpResult.put("RESULT", "FAILED");
                                    hsmpResult.put("ERROR_FLAG", "2053");
                                    this.trafficCardServerEntity.disconnect();
                                    return hsmpResult;
                                } else if ("B8".equalsIgnoreCase(hostResCode)) {
                                    hsmpResult.put(ErrorCode.ERRCODE, "CARD_AMOUNTERROR");
                                    hsmpResult.put("RESULT", "FAILED");
                                    hsmpResult.put("ERROR_FLAG", "2059");
                                    this.trafficCardServerEntity.disconnect();
                                    return hsmpResult;
                                } else if ("00".equals(hostResCode)) {
                                    hsmpResult.put("RESULT", "SUCCESS");
                                    int hostSeq = (int) FrameItemType.getHString2Long(dataSerialChargeWriteBackReqFeedbackRes, frameFormatSerialChargeWriteBackReqFeedbackRes.getPos("hostSeq"), frameFormatSerialChargeWriteBackReqFeedbackRes.getLen("hostSeq"));
                                    byte[] bChargeDataRecord = EncryptUtils.hex2byte(FrameItemType.getString(dataSerialChargeWriteBackReqFeedbackRes, frameFormatSerialChargeWriteBackReqFeedbackRes.getPos("ChargeDataRecord"), frameFormatSerialChargeWriteBackReqFeedbackRes.getLen("ChargeDataRecord")));
                                    FrameFormat frameFormatChargeDataRecord = FrameDataFormat.getFrameFormat("CHARGE_DATA_RECORD");
                                    ExpectEndDate = FrameItemType.getBCD2String(bChargeDataRecord, frameFormatChargeDataRecord.getPos("EndDate"), frameFormatChargeDataRecord.getLen("EndDate"));
                                    FrameFormat frameFormatSerialChargeWriteBackConfirm = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM);
                                    byte[] dataSerialChargeWriteBackConfirm = new byte[frameFormatSerialChargeWriteBackConfirm.getFrameLen()];
                                    FrameItemType.setString(dataSerialChargeWriteBackConfirm, 0, SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM);
                                    bArr = dataSerialChargeWriteBackConfirm;
                                    FrameItemType.setString(bArr, frameFormatSerialChargeWriteBackConfirm.getPos("TransType"), TransType.TRANSTYPE_CARD_CHARGE_SCRASHCARD);
                                    FrameItemType.setString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("TransResCode"), hostResCode);
                                    FrameItemType.setString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("TransDate"), DateUtils.formatDatetime(date, "yyyyMMdd"));
                                    FrameItemType.setString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("TransTime"), DateUtils.formatDatetime(date, "HHmmss"));
                                    FrameItemType.setString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("CardNo"), cardNo);
                                    FrameItemType.setLong2HString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("TransBalance"), frameFormatSerialChargeWriteBackConfirm.getLen("TransBalance"), (long) balance);
                                    FrameItemType.setLong2HString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("HostTransSeq"), frameFormatSerialChargeWriteBackConfirm.getLen("HostTransSeq"), (long) hostSeq);
                                    FrameItemType.setLong2HString(dataSerialChargeWriteBackConfirm, frameFormatSerialChargeWriteBackConfirm.getPos("PosICSeq"), frameFormatSerialChargeWriteBackConfirm.getLen("HostTransSeq"), this.posIcSeq);
                                    System.out.println("MODULE SEND:" + frameFormatSerialChargeWriteBackConfirm.dump(dataSerialChargeWriteBackConfirm));
                                    this.trafficCardReaderEntity.send(dataSerialChargeWriteBackConfirm, 0, dataSerialChargeWriteBackConfirm.length);
                                    FrameFormat frameFormatSerialChargeWriteBackConfirmRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM, "00");
                                    byte[] dataSerialChargeWriteBackConfirmRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                                    System.out.println("MODULE RECV:" + frameFormatSerialChargeWriteBackConfirmRes.dump(dataSerialChargeWriteBackConfirmRes));
                                    if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(dataSerialChargeWriteBackConfirmRes, frameFormatSerialChargeWriteBackConfirmRes.getPos("responseCode"), frameFormatSerialChargeWriteBackConfirmRes.getLen("responseCode")))) {
                                        MAC = FrameItemType.getString(dataSerialChargeWriteBackConfirmRes, frameFormatSerialChargeWriteBackConfirmRes.getPos("MAC"), frameFormatSerialChargeWriteBackConfirmRes.getLen("MAC"));
                                        EncryptData = FrameItemType.getString(dataSerialChargeWriteBackConfirmRes, frameFormatSerialChargeWriteBackConfirmRes.getPos("EncryptData"), frameFormatSerialChargeWriteBackConfirmRes.getLen("EncryptData"));
                                        FrameFormat frameFormatServerChargeWriteBackConfirmReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_WRITEBACK_CONFIRM_REQ, 4);
                                        frameFormatServerChargeWriteBackConfirmReq.setLen("PlivateMsg", 0);
                                        byte[] dataServerChargeWriteBackConfirmReq = new byte[frameFormatServerChargeWriteBackConfirmReq.getFrameLen()];
                                        FrameItemType.setLong2BCD(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("MessageType"), frameFormatServerChargeWriteBackConfirmReq.getLen("MessageType"), 5027);
                                        FrameItemType.setByte(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("Ver"), (byte) 4);
                                        FrameItemType.setString2BCD(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("SysDatetime"), frameFormatServerChargeWriteBackConfirmReq.getLen("SysDatetime"), DateUtils.formatDatetime(date, "yyyyMMddHHmmss"));
                                        FrameItemType.setString(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("PosId"), frameFormatServerChargeWriteBackConfirmReq.getLen("PosId"), this.posId);
                                        FrameItemType.setString2BCD(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("oprId"), frameFormatServerChargeWriteBackConfirmReq.getLen("oprId"), this.oprId);
                                        FrameItemType.setString2BCD(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("ISAM"), frameFormatServerChargeWriteBackConfirmReq.getLen("ISAM"), ISAM);
                                        FrameItemType.setString2BCD(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("UnitId"), frameFormatServerChargeWriteBackConfirmReq.getLen("UnitId"), this.unitId);
                                        FrameItemType.setString2BCD(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("MchntId"), frameFormatServerChargeWriteBackConfirmReq.getLen("MchntId"), this.mchntId);
                                        FrameItemType.setString2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("CipherDataMAC"), frameFormatServerChargeWriteBackConfirmReq.getLen("CipherDataMAC"), MAC);
                                        bArr = dataServerChargeWriteBackConfirmReq;
                                        FrameItemType.setLong2HEX(bArr, frameFormatServerChargeWriteBackConfirmReq.getPos("CipherDataLen"), frameFormatServerChargeWriteBackConfirmReq.getLen("CipherDataLen"), (long) frameFormatServerChargeWriteBackConfirmReq.getLen("MacBuf"));
                                        FrameItemType.setString2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("MacBuf"), frameFormatServerChargeWriteBackConfirmReq.getLen("MacBuf"), EncryptData);
                                        FrameItemType.setString2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("CSN"), frameFormatServerChargeWriteBackConfirmReq.getLen("CSN"), CSN);
                                        int aftBal = prevBalance + balance;
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("AftBal"), frameFormatServerChargeWriteBackConfirmReq.getLen("AftBal"), (long) aftBal);
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("CardCount"), frameFormatServerChargeWriteBackConfirmReq.getLen("CardCount"), FrameItemType.getHEX2Long(bChargeDataRecord, frameFormatChargeDataRecord.getPos("CardCount"), frameFormatChargeDataRecord.getLen("CardCount")));
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("TxnTAC"), frameFormatServerChargeWriteBackConfirmReq.getLen("TxnTAC"), FrameItemType.getHEX2Long(bChargeDataRecord, frameFormatChargeDataRecord.getPos("TxnTAC"), frameFormatChargeDataRecord.getLen("TxnTAC")));
                                        bArr = dataServerChargeWriteBackConfirmReq;
                                        FrameItemType.setLong2BCD(bArr, frameFormatServerChargeWriteBackConfirmReq.getPos("CardExp"), frameFormatServerChargeWriteBackConfirmReq.getLen("CardExp"), Long.parseLong(ExpectEndDate));
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("TacType"), frameFormatServerChargeWriteBackConfirmReq.getLen("TacType"), FrameItemType.getHEX2Long(bChargeDataRecord, frameFormatChargeDataRecord.getPos("EncryptType"), frameFormatChargeDataRecord.getLen("EncryptType")));
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("PsamTransNo"), frameFormatServerChargeWriteBackConfirmReq.getLen("PsamTransNo"), WalletTransSeq);
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("TxnStatus"), frameFormatServerChargeWriteBackConfirmReq.getLen("TxnStatus"), 0);
                                        FrameItemType.setLong2HEX(dataServerChargeWriteBackConfirmReq, frameFormatServerChargeWriteBackConfirmReq.getPos("PlivateType"), frameFormatServerChargeWriteBackConfirmReq.getLen("PlivateType"), 8192);
                                        System.out.println("SERVER SEND:" + frameFormatServerChargeWriteBackConfirmReq.dump(dataServerChargeWriteBackConfirmReq));
                                        this.trafficCardServerEntity.send(this.dataSeq, dataServerChargeWriteBackConfirmReq, 0, dataServerChargeWriteBackConfirmReq.length);
                                        byte[] dataServerChargeWriteBackConfirmRes = this.trafficCardServerEntity.recv(SERIAL_RES_MAX_TIME);
                                        if (dataServerChargeWriteBackConfirmRes == null) {
                                            this.trafficCardServerEntity.disconnect();
                                            return hsmpResult;
                                        }
                                        FrameFormat frameFormatServerChargeWriteBackConfirmRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_WRITEBACK_CONFIRM_RES, 4);
                                        System.out.println("SERVER RECV:" + frameFormatServerChargeWriteBackConfirmRes.dump(dataServerChargeWriteBackConfirmRes));
                                        if (((int) FrameItemType.getBCD2Long(dataServerChargeWriteBackConfirmRes, frameFormatServerChargeWriteBackConfirmRes.getPos("MessageType"), frameFormatServerChargeWriteBackConfirmRes.getLen("MessageType"))) != 5028) {
                                            this.trafficCardServerEntity.disconnect();
                                            return hsmpResult;
                                        }
                                        CipherDataMAC = FrameItemType.getHEX2String(dataServerChargeWriteBackConfirmRes, frameFormatServerChargeWriteBackConfirmRes.getPos("CipherDataMAC"), frameFormatServerChargeWriteBackConfirmRes.getLen("CipherDataMAC"));
                                        ServerEncryptData = EncryptUtils.byte2hex(dataServerChargeWriteBackConfirmRes, frameFormatServerChargeWriteBackConfirmRes.getPos("MacBuf"), frameFormatServerChargeWriteBackConfirmRes.getLen("MacBuf"));
                                        FrameFormat frameFormatSerialChargeWriteBackConfirmFeedback = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM_FEEDBACK);
                                        byte[] dataSerialChargeWriteBackConfirmFeedback = new byte[frameFormatSerialChargeWriteBackConfirmFeedback.getFrameLen()];
                                        FrameItemType.setString(dataSerialChargeWriteBackConfirmFeedback, 0, SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM_FEEDBACK);
                                        FrameItemType.setString(dataSerialChargeWriteBackConfirmFeedback, frameFormatSerialChargeWriteBackConfirmFeedback.getPos("MAC"), frameFormatSerialChargeWriteBackConfirmFeedback.getLen("MAC"), CipherDataMAC);
                                        FrameItemType.setString(dataSerialChargeWriteBackConfirmFeedback, frameFormatSerialChargeWriteBackConfirmFeedback.getPos("EncryptData"), ServerEncryptData);
                                        this.trafficCardReaderEntity.send(dataSerialChargeWriteBackConfirmFeedback, 0, dataSerialChargeWriteBackConfirmFeedback.length);
                                        FrameFormat frameFormatSerialChargeWriteBackConfirmFeedbackRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM_FEEDBACK, "00");
                                        if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME), frameFormatSerialChargeWriteBackConfirmFeedbackRes.getPos("responseCode"), frameFormatSerialChargeWriteBackConfirmFeedbackRes.getLen("responseCode")))) {
                                            hsmpResult.put("RESULT", "SUCCESS");
                                            this.trafficCardServerEntity.disconnect();
                                            return hsmpResult;
                                        }
                                        this.trafficCardServerEntity.disconnect();
                                        return hsmpResult;
                                    }
                                    this.trafficCardServerEntity.disconnect();
                                    return hsmpResult;
                                } else {
                                    hsmpResult.put(ErrorCode.MODEL_HOSTRESPONSECODE, hostResCode);
                                    hsmpResult.put("RESULT", "FAILED");
                                    hsmpResult.put("ERROR_FLAG", "2065");
                                    this.trafficCardServerEntity.disconnect();
                                    return hsmpResult;
                                }
                            }
                            hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "2040");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                    }
                    hsmpResult.put(ErrorCode.ERRCODE, "SERVER_LOST");
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "1930");
                    return hsmpResult;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    hsmpResult.put(ErrorCode.ERRCODE, NetworkUtils.NET_STATE_UNKNOWN);
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "2166");
                    return hsmpResult;
                } finally {
                    this.trafficCardServerEntity.disconnect();
                }
            } else {
                hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "1919");
                return hsmpResult;
            }
        }
        hsmpResult.put(ErrorCode.ERRCODE, "CARD_NOTMATCH");
        hsmpResult.put("RESULT", "FAILED");
        hsmpResult.put("ERROR_FLAG", "1856");
        return hsmpResult;
    }

    public HashMap queryQuickCard(String cardNo) {
        HashMap hsmpResult = new HashMap();
        HashMap hsmpModel = readModel();
        if (hsmpModel == null) {
            hsmpResult.put(ErrorCode.ERRCODE, "MODEL_UNKNOWN");
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "2175");
            return hsmpResult;
        } else if (!"SUCCESS".equalsIgnoreCase((String) hsmpModel.get("RESULT"))) {
            return hsmpModel;
        } else {
            String ISAM = (String) hsmpModel.get("ISAM");
            HashMap hsmpCard = readCard(0);
            if (hsmpCard == null) {
                hsmpResult.put(ErrorCode.ERRCODE, "MODEL_UNKNOWN");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "2186");
                return hsmpResult;
            } else if (!"SUCCESS".equalsIgnoreCase((String) hsmpCard.get("RESULT"))) {
                return hsmpCard;
            } else {
                if (cardNo.equals((String) hsmpCard.get("CardNo"))) {
                    String CSN = (String) hsmpCard.get("CSN");
                    String EndDate = (String) hsmpCard.get("EndDate");
                    String sPrevBalance = (String) hsmpCard.get("Balance");
                    String CardType = (String) hsmpCard.get("CardType");
                    String PhyCardType = (String) hsmpCard.get("PhyCardType");
                    String LastRecord = (String) hsmpCard.get("LastRecord");
                    HashMap hsmpCardType = getCardType(CardType, PhyCardType);
                    if (hsmpCardType == null) {
                        hsmpResult.put(ErrorCode.ERRCODE, "UNSUPPORT");
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "2208");
                        return hsmpResult;
                    }
                    int MinChargeAmount = Integer.parseInt((String) hsmpCardType.get("MinChargeAmount"));
                    int MaxChargeAmount = Integer.parseInt((String) hsmpCardType.get("MaxChargeAmount"));
                    int ChargeAmountUnit = Integer.parseInt((String) hsmpCardType.get("ChargeAmountUnit"));
                    int EndDays = Integer.parseInt((String) hsmpCardType.get("EndDays"));
                    try {
                        if (this.trafficCardServerEntity.connect(this.ip, this.port)) {
                            FrameFormat frameFormatTxnMsg = FrameDataFormat.getFrameFormat("TxnMsg");
                            frameFormatTxnMsg.setLen("TxnMsg", 8);
                            frameFormatTxnMsg.setLen("TxnCryMsg", 0);
                            frameFormatTxnMsg.setLen("DigitalSign", 0);
                            FrameFormat frameFormatServerQuickCardQueryReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.QUICKCARD_QUERY_REQ, 1);
                            byte[] dataServerQuickCardQueryReq = new byte[frameFormatServerQuickCardQueryReq.getFrameLen()];
                            FrameItemType.setLong2BCD(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("MessageType"), frameFormatServerQuickCardQueryReq.getLen("MessageType"), 5141);
                            FrameItemType.setByte(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("Ver"), (byte) 1);
                            FrameItemType.setString2BCD(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("SysDatetime"), frameFormatServerQuickCardQueryReq.getLen("SysDatetime"), DateUtils.formatDatetime(new Date(), "yyyyMMddHHmmss"));
                            FrameItemType.setString2BCD(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("oprId"), frameFormatServerQuickCardQueryReq.getLen("oprId"), this.oprId);
                            FrameItemType.setString(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("PosId"), frameFormatServerQuickCardQueryReq.getLen("PosId"), this.posId);
                            FrameItemType.setString2BCD(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("ISAM"), frameFormatServerQuickCardQueryReq.getLen("ISAM"), ISAM);
                            FrameItemType.setString2BCD(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("UnitId"), frameFormatServerQuickCardQueryReq.getLen("UnitId"), this.unitId);
                            FrameItemType.setString2BCD(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("MchntId"), frameFormatServerQuickCardQueryReq.getLen("MchntId"), this.mchntId);
                            FrameItemType.setLong2HEX(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("BatchNo"), frameFormatServerQuickCardQueryReq.getLen("BatchNo"), (long) this.batchNo);
                            FrameItemType.setString2BCD(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("CardNo"), frameFormatServerQuickCardQueryReq.getLen("CardNo"), cardNo);
                            FrameItemType.setLong2HEX(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("CardType"), frameFormatServerQuickCardQueryReq.getLen("CardType"), (long) Integer.parseInt(CardType, 16));
                            FrameItemType.setLong2HEX(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("CardPhyType"), frameFormatServerQuickCardQueryReq.getLen("CardPhyType"), (long) Integer.parseInt(PhyCardType, 16));
                            FrameItemType.setLong2HEX(dataServerQuickCardQueryReq, frameFormatServerQuickCardQueryReq.getPos("BefBal"), frameFormatServerQuickCardQueryReq.getLen("BefBal"), (long) Integer.parseInt(sPrevBalance));
                            System.out.println("SERVER SEND:" + frameFormatServerQuickCardQueryReq.dump(dataServerQuickCardQueryReq));
                            this.trafficCardServerEntity.send(this.dataSeq, dataServerQuickCardQueryReq, 0, dataServerQuickCardQueryReq.length);
                            byte[] dataServerQuickCardQueryRes = this.trafficCardServerEntity.recv(SERIAL_RES_MAX_TIME);
                            if (dataServerQuickCardQueryRes == null) {
                                hsmpResult.put(ErrorCode.ERRCODE, "SERVER_NORESPONSE");
                                hsmpResult.put("RESULT", "FAILED");
                                hsmpResult.put("ERROR_FLAG", "2250");
                                this.trafficCardServerEntity.disconnect();
                                return hsmpResult;
                            }
                            FrameFormat frameFormatServerQuickCardQueryRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.QUICKCARD_QUERY_RES, 1);
                            frameFormatServerQuickCardQueryRes.setLen("OrderInfo", dataServerQuickCardQueryRes.length - frameFormatServerQuickCardQueryRes.getPos("OrderInfo"));
                            System.out.println("SERVER RECV:" + frameFormatServerQuickCardQueryRes.dump(dataServerQuickCardQueryRes));
                            if (((int) FrameItemType.getBCD2Long(dataServerQuickCardQueryRes, frameFormatServerQuickCardQueryRes.getPos("MessageType"), frameFormatServerQuickCardQueryRes.getLen("MessageType"))) != 5142) {
                                hsmpResult.put(ErrorCode.ERRCODE, "SERVER_COMMERROR");
                                hsmpResult.put("RESULT", "FAILED");
                                hsmpResult.put("ERROR_FLAG", "2260");
                                this.trafficCardServerEntity.disconnect();
                                return hsmpResult;
                            }
                            byte bResponsecode = FrameItemType.getByte(dataServerQuickCardQueryRes, frameFormatServerQuickCardQueryRes.getPos("ResponseCode"));
                            int OrderNum = (int) FrameItemType.getHEX2Long(dataServerQuickCardQueryRes, frameFormatServerQuickCardQueryRes.getPos("OrderNum"), frameFormatServerQuickCardQueryRes.getLen("OrderNum"));
                            if (bResponsecode == (byte) -34) {
                                OrderNum = 0;
                            } else if (bResponsecode != (byte) 0) {
                                hsmpResult.put(ErrorCode.SERVER_RESPONSE, Integer.toHexString(bResponsecode));
                                hsmpResult.put("RESULT", "FAILED");
                                hsmpResult.put("ERROR_FLAG", "2270");
                                this.trafficCardServerEntity.disconnect();
                                return hsmpResult;
                            }
                            this.batchNo = (int) FrameItemType.getHEX2Long(dataServerQuickCardQueryRes, frameFormatServerQuickCardQueryRes.getPos("BatchNo"), frameFormatServerQuickCardQueryRes.getLen("BatchNo"));
                            FrameFormat frameFormatQuickCard = FrameDataFormat.getFrameFormat("QuickCard");
                            int pos = 0;
                            List<HashMap> listOrder = new ArrayList();
                            for (int i = 0; i < OrderNum; i++) {
                                String OrderNo = FrameItemType.getHEX2String(dataServerQuickCardQueryRes, (frameFormatServerQuickCardQueryRes.getPos("OrderInfo") + pos) + frameFormatQuickCard.getPos("OrderNo"), frameFormatQuickCard.getLen("OrderNo"));
                                long OrderSaveMnt = FrameItemType.getHEX2Balance(dataServerQuickCardQueryRes, (frameFormatServerQuickCardQueryRes.getPos("OrderInfo") + pos) + frameFormatQuickCard.getPos("OrderSaveMnt"), frameFormatQuickCard.getLen("OrderSaveMnt"));
                                String OrderDate = FrameItemType.getBCD2String(dataServerQuickCardQueryRes, (frameFormatServerQuickCardQueryRes.getPos("OrderInfo") + pos) + frameFormatQuickCard.getPos("OrderDate"), frameFormatQuickCard.getLen("OrderDate"));
                                String OrderTime = FrameItemType.getBCD2String(dataServerQuickCardQueryRes, (frameFormatServerQuickCardQueryRes.getPos("OrderInfo") + pos) + frameFormatQuickCard.getPos("OrderTime"), frameFormatQuickCard.getLen("OrderTime"));
                                HashMap hsmpOrder = new HashMap();
                                hsmpOrder.put("OrderNo", OrderNo);
                                hsmpOrder.put("OrderSaveMnt", "" + OrderSaveMnt);
                                hsmpOrder.put("OrderDate", OrderDate);
                                hsmpOrder.put("OrderTime", OrderTime);
                                listOrder.add(hsmpOrder);
                                pos += frameFormatQuickCard.getFrameLen();
                            }
                            hsmpResult.put("ORDER", listOrder);
                            hsmpResult.put("RESULT", "SUCCESS");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        hsmpResult.put(ErrorCode.ERRCODE, "SERVER_LOST");
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "2221");
                        return hsmpResult;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return hsmpResult;
                    } finally {
                        this.trafficCardServerEntity.disconnect();
                    }
                } else {
                    hsmpResult.put(ErrorCode.ERRCODE, "CARD_NOTMATCH");
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "2195");
                    return hsmpResult;
                }
            }
        }
    }

    public HashMap chargeQuickCard(String cardNo, HashMap hsmpOrder, String datetime) {
        Date date;
        try {
            date = DateUtils.parseDatetime(datetime);
        } catch (Exception e) {
            date = new Date();
        }
        if (date == null) {
            date = new Date();
        }
        String pkgUserAcctPassword = "00FFFFFFFFFFFFFF";
        String OrderNo = (String) hsmpOrder.get("OrderNo");
        long OrderSaveMnt = Long.parseLong((String) hsmpOrder.get("OrderSaveMnt"));
        HashMap hsmpResult = new HashMap();
        HashMap hsmpModel = readModel();
        if (hsmpModel == null) {
            return null;
        }
        String ISAM = (String) hsmpModel.get("ISAM");
        HashMap hsmpCard = readCard(0);
        if (hsmpCard == null) {
            hsmpResult.put(ErrorCode.ERRCODE, "MODEL_UNKNOWN");
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "2325");
            return hsmpResult;
        }
        if (!"SUCCESS".equalsIgnoreCase((String) hsmpCard.get("RESULT"))) {
            return hsmpCard;
        }
        if (cardNo.equals((String) hsmpCard.get("CardNo"))) {
            String CSN = (String) hsmpCard.get("CSN");
            String EndDate = (String) hsmpCard.get("EndDate");
            String sPrevBalance = (String) hsmpCard.get("Balance");
            String CardType = (String) hsmpCard.get("CardType");
            String PhyCardType = (String) hsmpCard.get("PhyCardType");
            String LastRecord = (String) hsmpCard.get("LastRecord");
            HashMap hsmpCardType = getCardType(CardType, PhyCardType);
            if (hsmpCardType == null) {
                hsmpResult.put(ErrorCode.ERRCODE, "UNSUPPORT");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "2347");
                return hsmpResult;
            }
            String ExpectEndDate;
            int MinChargeAmount = Integer.parseInt((String) hsmpCardType.get("MinChargeAmount"));
            int MaxChargeAmount = Integer.parseInt((String) hsmpCardType.get("MaxChargeAmount"));
            int ChargeAmountUnit = Integer.parseInt((String) hsmpCardType.get("ChargeAmountUnit"));
            int EndDays = Integer.parseInt((String) hsmpCardType.get("EndDays"));
            if (EndDays > 0) {
                ExpectEndDate = DateUtils.formatDatetime(new Date(System.currentTimeMillis() + (86400000 * ((long) EndDays))), "yyyyMMdd");
            } else {
                ExpectEndDate = EndDate;
            }
            int prevBalance = 0;
            if (!StringUtils.isBlank(sPrevBalance)) {
                prevBalance = Integer.parseInt(sPrevBalance);
            }
            if (((int) OrderSaveMnt) + prevBalance <= 0) {
                hsmpResult.put(ErrorCode.ERRCODE, "NOT_ENOUGH");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "2391");
                return hsmpResult;
            }
            if (StringUtils.isBlank(LastRecord)) {
                LastRecord = "";
            }
            FrameFormat frameFormatSerialChargeReq = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_REQ);
            byte[] dataSerialChargeReq = new byte[frameFormatSerialChargeReq.getFrameLen()];
            FrameItemType.setString(dataSerialChargeReq, 0, SerialFrameType.ONLINE_CHARGE_REQ);
            FrameItemType.setString(dataSerialChargeReq, frameFormatSerialChargeReq.getPos("CSN"), CSN);
            byte[] bArr = dataSerialChargeReq;
            FrameItemType.setString(bArr, frameFormatSerialChargeReq.getPos("TransType"), TransType.TRANSTYPE_CARD_CHARGE_QUICKCARD);
            FrameItemType.setLong2HString(dataSerialChargeReq, frameFormatSerialChargeReq.getPos("TransBalance"), frameFormatSerialChargeReq.getLen("TransBalance"), OrderSaveMnt);
            System.out.println("MODULE SEND:" + frameFormatSerialChargeReq.dump(dataSerialChargeReq));
            this.trafficCardReaderEntity.send(dataSerialChargeReq, 0, dataSerialChargeReq.length);
            byte[] dataSerialChargeReqRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
            if (dataSerialChargeReqRes == null) {
                hsmpResult.put(ErrorCode.ERRCODE, "MODEL_NORESPONSE");
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "2385");
                return hsmpResult;
            }
            FrameFormat frameFormatSerialChargeReqRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_REQ, "00");
            System.out.println("MODULE SEND:" + frameFormatSerialChargeReqRes.dump(dataSerialChargeReqRes));
            String responseCode = FrameItemType.getString(dataSerialChargeReqRes, frameFormatSerialChargeReqRes.getPos("responseCode"), frameFormatSerialChargeReqRes.getLen("responseCode"));
            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                String MAC = FrameItemType.getString(dataSerialChargeReqRes, frameFormatSerialChargeReqRes.getPos("MAC"), frameFormatSerialChargeReqRes.getLen("MAC"));
                String EncryptData = FrameItemType.getString(dataSerialChargeReqRes, frameFormatSerialChargeReqRes.getPos("EncryptData"), frameFormatSerialChargeReqRes.getLen("EncryptData"));
                long WalletTransSeq = FrameItemType.getHString2Long(dataSerialChargeReqRes, frameFormatSerialChargeReqRes.getPos("WalletTransSeq"), frameFormatSerialChargeReqRes.getLen("WalletTransSeq"));
                try {
                    if (this.trafficCardServerEntity.connect(this.ip, this.port)) {
                        FrameFormat frameFormatServerQuickCardChargeReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.QUICKCARD_CHARGE_REQ, 1);
                        byte[] dataServerQuickCardChargeReq = new byte[frameFormatServerQuickCardChargeReq.getFrameLen()];
                        FrameItemType.setLong2BCD(dataServerQuickCardChargeReq, frameFormatServerQuickCardChargeReq.getPos("MessageType"), frameFormatServerQuickCardChargeReq.getLen("MessageType"), 5143);
                        FrameItemType.setByte(dataServerQuickCardChargeReq, frameFormatServerQuickCardChargeReq.getPos("Ver"), (byte) 1);
                        FrameItemType.setString2BCD(dataServerQuickCardChargeReq, frameFormatServerQuickCardChargeReq.getPos("SysDatetime"), frameFormatServerQuickCardChargeReq.getLen("SysDatetime"), DateUtils.formatDatetime(date, "yyyyMMddHHmmss"));
                        FrameItemType.setString2BCD(dataServerQuickCardChargeReq, frameFormatServerQuickCardChargeReq.getPos("oprId"), frameFormatServerQuickCardChargeReq.getLen("oprId"), this.oprId);
                        FrameItemType.setString(dataServerQuickCardChargeReq, frameFormatServerQuickCardChargeReq.getPos("PosId"), frameFormatServerQuickCardChargeReq.getLen("PosId"), this.posId);
                        FrameItemType.setString2BCD(dataServerQuickCardChargeReq, frameFormatServerQuickCardChargeReq.getPos("ISAM"), frameFormatServerQuickCardChargeReq.getLen("ISAM"), ISAM);
                        FrameItemType.setString2BCD(dataServerQuickCardChargeReq, frameFormatServerQuickCardChargeReq.getPos("UnitId"), frameFormatServerQuickCardChargeReq.getLen("UnitId"), this.unitId);
                        FrameItemType.setString2BCD(dataServerQuickCardChargeReq, frameFormatServerQuickCardChargeReq.getPos("MchntId"), frameFormatServerQuickCardChargeReq.getLen("MchntId"), this.mchntId);
                        bArr = dataServerQuickCardChargeReq;
                        FrameItemType.setLong2HEX(bArr, frameFormatServerQuickCardChargeReq.getPos("BatchNo"), frameFormatServerQuickCardChargeReq.getLen("BatchNo"), (long) this.batchNo);
                        FrameItemType.setString2HEX(dataServerQuickCardChargeReq, frameFormatServerQuickCardChargeReq.getPos("CipherDataMAC"), frameFormatServerQuickCardChargeReq.getLen("CipherDataMAC"), MAC);
                        bArr = dataServerQuickCardChargeReq;
                        FrameItemType.setLong2HEX(bArr, frameFormatServerQuickCardChargeReq.getPos("CipherDataLen"), frameFormatServerQuickCardChargeReq.getLen("CipherDataLen"), (long) frameFormatServerQuickCardChargeReq.getLen("MacBuf"));
                        FrameItemType.setString2HEX(dataServerQuickCardChargeReq, frameFormatServerQuickCardChargeReq.getPos("MacBuf"), frameFormatServerQuickCardChargeReq.getLen("MacBuf"), EncryptData);
                        FrameItemType.setString2HEX(dataServerQuickCardChargeReq, frameFormatServerQuickCardChargeReq.getPos("OrderNo"), frameFormatServerQuickCardChargeReq.getLen("OrderNo"), OrderNo);
                        FrameItemType.setString2HEX(dataServerQuickCardChargeReq, frameFormatServerQuickCardChargeReq.getPos("LastRecord"), frameFormatServerQuickCardChargeReq.getLen("LastRecord"), LastRecord);
                        System.out.println("SERVER SEND:" + frameFormatServerQuickCardChargeReq.dump(dataServerQuickCardChargeReq));
                        this.trafficCardServerEntity.send(this.dataSeq, dataServerQuickCardChargeReq, 0, dataServerQuickCardChargeReq.length);
                        byte[] dataServerQuickCardChargeRes = this.trafficCardServerEntity.recv(SERIAL_RES_MAX_TIME);
                        if (dataServerQuickCardChargeRes == null) {
                            hsmpResult.put(ErrorCode.ERRCODE, "SERVER_NORESPONSE");
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "2429");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        FrameFormat frameFormatServerQuickCardChargeRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.QUICKCARD_CHARGE_RES, 1);
                        System.out.println("SERVER RECV:" + frameFormatServerQuickCardChargeRes.dump(dataServerQuickCardChargeRes));
                        if (((int) FrameItemType.getBCD2Long(dataServerQuickCardChargeRes, frameFormatServerQuickCardChargeRes.getPos("MessageType"), frameFormatServerQuickCardChargeRes.getLen("MessageType"))) != 5144) {
                            hsmpResult.put(ErrorCode.ERRCODE, "SERVER_COMMERROR");
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "2438");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                        this.batchNo = (int) FrameItemType.getHEX2Long(dataServerQuickCardChargeRes, frameFormatServerQuickCardChargeRes.getPos("BatchNo"), frameFormatServerQuickCardChargeRes.getLen("BatchNo"));
                        String CipherDataMAC = FrameItemType.getHEX2String(dataServerQuickCardChargeRes, frameFormatServerQuickCardChargeRes.getPos("CipherDataMAC"), frameFormatServerQuickCardChargeRes.getLen("CipherDataMAC"));
                        String ServerEncryptData = EncryptUtils.byte2hex(dataServerQuickCardChargeRes, frameFormatServerQuickCardChargeRes.getPos("MacBuf"), frameFormatServerQuickCardChargeRes.getLen("MacBuf")).toUpperCase();
                        FrameFormat frameFormatSerialChargeReqFeedback = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_REQ_FEEDBACK);
                        this.posIcSeq++;
                        byte[] dataSerialChargeReqFeedback = new byte[frameFormatSerialChargeReqFeedback.getFrameLen()];
                        FrameItemType.setString(dataSerialChargeReqFeedback, 0, SerialFrameType.ONLINE_CHARGE_REQ_FEEDBACK);
                        FrameItemType.setLong2HString(dataSerialChargeReqFeedback, frameFormatSerialChargeReqFeedback.getPos("PosIcSeq"), frameFormatSerialChargeReqFeedback.getLen("PosIcSeq"), this.posIcSeq);
                        FrameItemType.setString(dataSerialChargeReqFeedback, frameFormatSerialChargeReqFeedback.getPos("OprId"), this.oprId);
                        bArr = dataSerialChargeReqFeedback;
                        FrameItemType.setLong2BString(bArr, frameFormatSerialChargeReqFeedback.getPos("AgentCode"), frameFormatSerialChargeReqFeedback.getLen("AgentCode"), (long) this.agentCode);
                        FrameItemType.setString(dataSerialChargeReqFeedback, frameFormatSerialChargeReqFeedback.getPos("EndDate"), ExpectEndDate);
                        FrameItemType.setString(dataSerialChargeReqFeedback, frameFormatSerialChargeReqFeedback.getPos("MAC"), frameFormatSerialChargeReqFeedback.getLen("MAC"), CipherDataMAC);
                        FrameItemType.setString(dataSerialChargeReqFeedback, frameFormatSerialChargeReqFeedback.getPos("ServerEncryptData"), ServerEncryptData);
                        System.out.println("MODULE SEND:" + frameFormatSerialChargeReqFeedback.dump(dataSerialChargeReqFeedback));
                        this.trafficCardReaderEntity.send(dataSerialChargeReqFeedback, 0, dataSerialChargeReqFeedback.length);
                        FrameFormat frameFormatSerialChargeReqFeedbackRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_REQ_FEEDBACK, "00");
                        byte[] dataSerialChargeReqFeedbackRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                        System.out.println("MODULE RECV:" + frameFormatSerialChargeReqFeedbackRes.dump(dataSerialChargeReqFeedbackRes));
                        int times = 0;
                        while (times < 3) {
                            if (dataSerialChargeReqFeedbackRes != null) {
                                responseCode = FrameItemType.getString(dataSerialChargeReqFeedbackRes, frameFormatSerialChargeReqFeedbackRes.getPos("responseCode"), frameFormatSerialChargeReqFeedbackRes.getLen("responseCode"));
                            }
                            if (dataSerialChargeReqFeedbackRes != null && !"AA20".equalsIgnoreCase(responseCode)) {
                                break;
                            }
                            Thread.sleep(3000);
                            this.trafficCardReaderEntity.send(dataSerialChargeReqFeedback, 0, dataSerialChargeReqFeedback.length);
                            dataSerialChargeReqFeedbackRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                            times++;
                        }
                        if (times >= 3 && !"AA20".equalsIgnoreCase(responseCode)) {
                            hsmpResult.put("RESULT", "SUCCESS");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        } else if ("AA34".equalsIgnoreCase(responseCode)) {
                            if ("SUCCESS".equalsIgnoreCase((String) chargeCancel(ISAM, cardNo, TransType.TRANSTYPE_CARD_CHARGE_QUICKCARD, (int) OrderSaveMnt, this.posCommSeq, datetime).get("RESULT"))) {
                                hsmpResult.put(ErrorCode.ERRCODE, "CHARGE_CANCELED");
                                hsmpResult.put("RESULT", "FAILED");
                                hsmpResult.put("ERROR_FLAG", "2492");
                                this.trafficCardServerEntity.disconnect();
                                return hsmpResult;
                            }
                            hsmpResult.put("RESULT", "SUCCESS");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        } else {
                            responseCode = FrameItemType.getString(dataSerialChargeReqFeedbackRes, frameFormatSerialChargeReqFeedbackRes.getPos("responseCode"), frameFormatSerialChargeReqFeedbackRes.getLen("responseCode"));
                            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(responseCode)) {
                                String hostResCode = FrameItemType.getString(dataSerialChargeReqFeedbackRes, frameFormatSerialChargeReqFeedbackRes.getPos("hostResCode"), frameFormatSerialChargeReqFeedbackRes.getLen("hostResCode"));
                                if ("16".equalsIgnoreCase(hostResCode)) {
                                    hsmpResult.put(ErrorCode.ERRCODE, "QUICKCARD_STATUSERROR");
                                    hsmpResult.put("RESULT", "FAILED");
                                    hsmpResult.put("ERROR_FLAG", "2508");
                                    this.trafficCardServerEntity.disconnect();
                                    return hsmpResult;
                                } else if ("00".equals(hostResCode)) {
                                    hsmpResult.put("RESULT", "SUCCESS");
                                    int hostSeq = (int) FrameItemType.getHString2Long(dataSerialChargeReqFeedbackRes, frameFormatSerialChargeReqFeedbackRes.getPos("hostSeq"), frameFormatSerialChargeReqFeedbackRes.getLen("hostSeq"));
                                    byte[] bChargeDataRecord = EncryptUtils.hex2byte(FrameItemType.getString(dataSerialChargeReqFeedbackRes, frameFormatSerialChargeReqFeedbackRes.getPos("ChargeDataRecord"), frameFormatSerialChargeReqFeedbackRes.getLen("ChargeDataRecord")));
                                    FrameFormat frameFormatChargeDataRecord = FrameDataFormat.getFrameFormat("CHARGE_DATA_RECORD");
                                    ExpectEndDate = FrameItemType.getBCD2String(bChargeDataRecord, frameFormatChargeDataRecord.getPos("EndDate"), frameFormatChargeDataRecord.getLen("EndDate"));
                                    FrameFormat frameFormatSerialChargeConfirm = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_CONFIRM);
                                    byte[] dataSerialChargeConfirm = new byte[frameFormatSerialChargeConfirm.getFrameLen()];
                                    FrameItemType.setString(dataSerialChargeConfirm, 0, SerialFrameType.ONLINE_CHARGE_CONFIRM);
                                    bArr = dataSerialChargeConfirm;
                                    FrameItemType.setString(bArr, frameFormatSerialChargeConfirm.getPos("TransType"), TransType.TRANSTYPE_CARD_CHARGE_QUICKCARD);
                                    FrameItemType.setString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("TransResCode"), hostResCode);
                                    FrameItemType.setString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("TransDate"), DateUtils.formatDatetime(date, "yyyyMMdd"));
                                    FrameItemType.setString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("TransTime"), DateUtils.formatDatetime(date, "HHmmss"));
                                    FrameItemType.setString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("CardNo"), cardNo);
                                    FrameItemType.setLong2HString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("TransBalance"), frameFormatSerialChargeConfirm.getLen("TransBalance"), OrderSaveMnt);
                                    FrameItemType.setLong2HString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("HostTransSeq"), frameFormatSerialChargeConfirm.getLen("HostTransSeq"), (long) hostSeq);
                                    FrameItemType.setLong2HString(dataSerialChargeConfirm, frameFormatSerialChargeConfirm.getPos("PosICSeq"), frameFormatSerialChargeConfirm.getLen("HostTransSeq"), this.posIcSeq);
                                    System.out.println("MODULE SEND:" + frameFormatSerialChargeConfirm.dump(dataSerialChargeConfirm));
                                    this.trafficCardReaderEntity.send(dataSerialChargeConfirm, 0, dataSerialChargeConfirm.length);
                                    FrameFormat frameFormatSerialChargeConfirmRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_CONFIRM, "00");
                                    byte[] dataSerialChargeConfirmRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                                    System.out.println("MODULE RECV:" + frameFormatSerialChargeConfirmRes.dump(dataSerialChargeConfirmRes));
                                    if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(dataSerialChargeConfirmRes, frameFormatSerialChargeConfirmRes.getPos("responseCode"), frameFormatSerialChargeConfirmRes.getLen("responseCode")))) {
                                        MAC = FrameItemType.getString(dataSerialChargeConfirmRes, frameFormatSerialChargeConfirmRes.getPos("MAC"), frameFormatSerialChargeConfirmRes.getLen("MAC"));
                                        EncryptData = FrameItemType.getString(dataSerialChargeConfirmRes, frameFormatSerialChargeConfirmRes.getPos("EncryptData"), frameFormatSerialChargeConfirmRes.getLen("EncryptData"));
                                        FrameFormat frameFormatServerQuickCardChargeConfirmReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.QUICKCARD_CHARGE_CONFIRM_REQ, 1);
                                        frameFormatServerQuickCardChargeConfirmReq.setLen("PlivateMsg", 0);
                                        byte[] dataServerQuickCardChargeConfirmReq = new byte[frameFormatServerQuickCardChargeConfirmReq.getFrameLen()];
                                        FrameItemType.setLong2BCD(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("MessageType"), frameFormatServerQuickCardChargeConfirmReq.getLen("MessageType"), 5145);
                                        FrameItemType.setByte(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("Ver"), (byte) 1);
                                        FrameItemType.setString2BCD(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("SysDatetime"), frameFormatServerQuickCardChargeConfirmReq.getLen("SysDatetime"), DateUtils.formatDatetime(date, "yyyyMMddHHmmss"));
                                        FrameItemType.setString(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("PosId"), frameFormatServerQuickCardChargeConfirmReq.getLen("PosId"), this.posId);
                                        FrameItemType.setString2BCD(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("oprId"), frameFormatServerQuickCardChargeConfirmReq.getLen("oprId"), this.oprId);
                                        FrameItemType.setString2BCD(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("ISAM"), frameFormatServerQuickCardChargeConfirmReq.getLen("ISAM"), ISAM);
                                        FrameItemType.setString2BCD(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("UnitId"), frameFormatServerQuickCardChargeConfirmReq.getLen("UnitId"), this.unitId);
                                        FrameItemType.setString2BCD(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("MchntId"), frameFormatServerQuickCardChargeConfirmReq.getLen("MchntId"), this.mchntId);
                                        FrameItemType.setString2HEX(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("CipherDataMAC"), frameFormatServerQuickCardChargeConfirmReq.getLen("CipherDataMAC"), MAC);
                                        bArr = dataServerQuickCardChargeConfirmReq;
                                        FrameItemType.setLong2HEX(bArr, frameFormatServerQuickCardChargeConfirmReq.getPos("CipherDataLen"), frameFormatServerQuickCardChargeConfirmReq.getLen("CipherDataLen"), (long) frameFormatServerQuickCardChargeConfirmReq.getLen("MacBuf"));
                                        FrameItemType.setString2HEX(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("MacBuf"), frameFormatServerQuickCardChargeConfirmReq.getLen("MacBuf"), EncryptData);
                                        FrameItemType.setString2HEX(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("OrderNo"), frameFormatServerQuickCardChargeConfirmReq.getLen("OrderNo"), OrderNo);
                                        FrameItemType.setString2HEX(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("CSN"), frameFormatServerQuickCardChargeConfirmReq.getLen("CSN"), CSN);
                                        int aftBal = prevBalance + ((int) OrderSaveMnt);
                                        FrameItemType.setLong2HEX(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("AftBal"), frameFormatServerQuickCardChargeConfirmReq.getLen("AftBal"), (long) aftBal);
                                        FrameItemType.setLong2HEX(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("CardCount"), frameFormatServerQuickCardChargeConfirmReq.getLen("CardCount"), FrameItemType.getHEX2Long(bChargeDataRecord, frameFormatChargeDataRecord.getPos("CardCount"), frameFormatChargeDataRecord.getLen("CardCount")));
                                        FrameItemType.setLong2HEX(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("TxnTAC"), frameFormatServerQuickCardChargeConfirmReq.getLen("TxnTAC"), FrameItemType.getHEX2Long(bChargeDataRecord, frameFormatChargeDataRecord.getPos("TxnTAC"), frameFormatChargeDataRecord.getLen("TxnTAC")));
                                        bArr = dataServerQuickCardChargeConfirmReq;
                                        FrameItemType.setLong2BCD(bArr, frameFormatServerQuickCardChargeConfirmReq.getPos("CardExp"), frameFormatServerQuickCardChargeConfirmReq.getLen("CardExp"), Long.parseLong(ExpectEndDate));
                                        FrameItemType.setLong2HEX(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("TacType"), frameFormatServerQuickCardChargeConfirmReq.getLen("TacType"), FrameItemType.getHEX2Long(bChargeDataRecord, frameFormatChargeDataRecord.getPos("EncryptType"), frameFormatChargeDataRecord.getLen("EncryptType")));
                                        FrameItemType.setLong2HEX(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("TxnStatus"), frameFormatServerQuickCardChargeConfirmReq.getLen("TxnStatus"), 0);
                                        FrameItemType.setLong2HEX(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("PsamTransNo"), frameFormatServerQuickCardChargeConfirmReq.getLen("PsamTransNo"), WalletTransSeq);
                                        FrameItemType.setLong2HEX(dataServerQuickCardChargeConfirmReq, frameFormatServerQuickCardChargeConfirmReq.getPos("PlivateType"), frameFormatServerQuickCardChargeConfirmReq.getLen("PlivateType"), 8192);
                                        System.out.println("SERVER SEND:" + frameFormatServerQuickCardChargeConfirmReq.dump(dataServerQuickCardChargeConfirmReq));
                                        this.trafficCardServerEntity.send(this.dataSeq, dataServerQuickCardChargeConfirmReq, 0, dataServerQuickCardChargeConfirmReq.length);
                                        byte[] dataServerQuickCardChargeConfirmRes = this.trafficCardServerEntity.recv(SERIAL_RES_MAX_TIME);
                                        if (dataServerQuickCardChargeConfirmRes == null) {
                                            this.trafficCardServerEntity.disconnect();
                                            return hsmpResult;
                                        }
                                        FrameFormat frameFormatServerQuickCardChargeConfirmRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.QUICKCARD_CHARGE_CONFIRM_RES, 1);
                                        System.out.println("SERVER RECV:" + frameFormatServerQuickCardChargeConfirmRes.dump(dataServerQuickCardChargeConfirmRes));
                                        if (((int) FrameItemType.getBCD2Long(dataServerQuickCardChargeConfirmRes, frameFormatServerQuickCardChargeConfirmRes.getPos("MessageType"), frameFormatServerQuickCardChargeConfirmRes.getLen("MessageType"))) != 5146) {
                                            this.trafficCardServerEntity.disconnect();
                                            return hsmpResult;
                                        }
                                        CipherDataMAC = FrameItemType.getHEX2String(dataServerQuickCardChargeConfirmRes, frameFormatServerQuickCardChargeConfirmRes.getPos("CipherDataMAC"), frameFormatServerQuickCardChargeConfirmRes.getLen("CipherDataMAC"));
                                        ServerEncryptData = EncryptUtils.byte2hex(dataServerQuickCardChargeConfirmRes, frameFormatServerQuickCardChargeConfirmRes.getPos("MacBuf"), frameFormatServerQuickCardChargeConfirmRes.getLen("MacBuf"));
                                        FrameFormat frameFormatSerialChargeConfirmFeedback = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_CONFIRM_FEEDBACK);
                                        byte[] dataSerialChargeConfirmFeedback = new byte[frameFormatSerialChargeConfirmFeedback.getFrameLen()];
                                        FrameItemType.setString(dataSerialChargeConfirmFeedback, 0, SerialFrameType.ONLINE_CHARGE_CONFIRM_FEEDBACK);
                                        FrameItemType.setString(dataSerialChargeConfirmFeedback, frameFormatSerialChargeConfirmFeedback.getPos("MAC"), frameFormatSerialChargeConfirmFeedback.getLen("MAC"), CipherDataMAC);
                                        FrameItemType.setString(dataSerialChargeConfirmFeedback, frameFormatSerialChargeConfirmFeedback.getPos("EncryptData"), ServerEncryptData);
                                        this.trafficCardReaderEntity.send(dataSerialChargeConfirmFeedback, 0, dataSerialChargeConfirmFeedback.length);
                                        FrameFormat frameFormatSerialChargeConfirmFeedbackRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_CONFIRM_FEEDBACK, "00");
                                        if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME), frameFormatSerialChargeConfirmFeedbackRes.getPos("responseCode"), frameFormatSerialChargeConfirmFeedbackRes.getLen("responseCode")))) {
                                            hsmpResult.put("RESULT", "SUCCESS");
                                            this.trafficCardServerEntity.disconnect();
                                            return hsmpResult;
                                        }
                                        this.trafficCardServerEntity.disconnect();
                                        return hsmpResult;
                                    }
                                    this.trafficCardServerEntity.disconnect();
                                    return hsmpResult;
                                } else {
                                    hsmpResult.put(ErrorCode.MODEL_HOSTRESPONSECODE, hostResCode);
                                    hsmpResult.put("RESULT", "FAILED");
                                    hsmpResult.put("ERROR_FLAG", "2514");
                                    this.trafficCardServerEntity.disconnect();
                                    return hsmpResult;
                                }
                            }
                            hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                            hsmpResult.put("RESULT", "FAILED");
                            hsmpResult.put("ERROR_FLAG", "2500");
                            this.trafficCardServerEntity.disconnect();
                            return hsmpResult;
                        }
                    }
                    hsmpResult.put(ErrorCode.ERRCODE, "SERVER_LOST");
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "2404");
                    return hsmpResult;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    hsmpResult.put(ErrorCode.ERRCODE, NetworkUtils.NET_STATE_UNKNOWN);
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "2616");
                    return hsmpResult;
                } finally {
                    this.trafficCardServerEntity.disconnect();
                }
            } else {
                hsmpResult.put(ErrorCode.MODEL_RESPONSECODE, responseCode);
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "2394");
                return hsmpResult;
            }
        }
        hsmpResult.put(ErrorCode.ERRCODE, "CARD_NOTMATCH");
        hsmpResult.put("RESULT", "FAILED");
        hsmpResult.put("ERROR_FLAG", "2334");
        return hsmpResult;
    }

    public HashMap getCardType(String cardType, String phyCardType) {
        for (int i = 0; i < this.listCardType.size(); i++) {
            HashMap hsmpCardType = (HashMap) this.listCardType.get(i);
            if (cardType.equals(hsmpCardType.get("CardType")) && phyCardType.equals(hsmpCardType.get("PhyCardType"))) {
                return hsmpCardType;
            }
        }
        return null;
    }

    public HashMap chargeCancel(String ISAM, String cardNo, String transType, int balance, long posCommSeq, String datetime) throws Exception {
        Date date;
        try {
            date = DateUtils.parseDatetime(datetime);
        } catch (Exception e) {
            date = new Date();
        }
        if (date == null) {
            date = new Date();
        }
        HashMap hsmpResult = new HashMap();
        FrameFormat frameFormatSerialChargeCancelReq = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_CANCEL_REQ);
        byte[] dataSerialChargeCancelReq = new byte[frameFormatSerialChargeCancelReq.getFrameLen()];
        FrameItemType.setString(dataSerialChargeCancelReq, 0, SerialFrameType.ONLINE_CHARGE_CANCEL_REQ);
        FrameItemType.setString(dataSerialChargeCancelReq, frameFormatSerialChargeCancelReq.getPos("TransType"), transType);
        FrameItemType.setString(dataSerialChargeCancelReq, frameFormatSerialChargeCancelReq.getPos("TransDate"), DateUtils.formatDatetime(date, "yyyyMMdd"));
        FrameItemType.setString(dataSerialChargeCancelReq, frameFormatSerialChargeCancelReq.getPos("CardNo"), cardNo);
        FrameItemType.setLong2HString(dataSerialChargeCancelReq, frameFormatSerialChargeCancelReq.getPos("TransBalance"), frameFormatSerialChargeCancelReq.getLen("TransBalance"), (long) balance);
        FrameItemType.setLong2HString(dataSerialChargeCancelReq, frameFormatSerialChargeCancelReq.getPos("PosCommSeq"), frameFormatSerialChargeCancelReq.getLen("PosCommSeq"), posCommSeq);
        System.out.println("MODULE SEND:" + frameFormatSerialChargeCancelReq.dump(dataSerialChargeCancelReq));
        this.trafficCardReaderEntity.send(dataSerialChargeCancelReq, 0, dataSerialChargeCancelReq.length);
        byte[] dataSerialChargeCancelReqRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
        if (dataSerialChargeCancelReqRes == null) {
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "2654");
        } else {
            FrameFormat frameFormatSerialChargeCancelReqRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_CANCEL_REQ, "00");
            System.out.println("MODULE SEND:" + frameFormatSerialChargeCancelReqRes.dump(dataSerialChargeCancelReqRes));
            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(dataSerialChargeCancelReqRes, frameFormatSerialChargeCancelReqRes.getPos("responseCode"), frameFormatSerialChargeCancelReqRes.getLen("responseCode")))) {
                String MAC = FrameItemType.getString(dataSerialChargeCancelReqRes, frameFormatSerialChargeCancelReqRes.getPos("MAC"), frameFormatSerialChargeCancelReqRes.getLen("MAC"));
                String EncryptData = FrameItemType.getString(dataSerialChargeCancelReqRes, frameFormatSerialChargeCancelReqRes.getPos("EncryptData"), frameFormatSerialChargeCancelReqRes.getLen("EncryptData"));
                FrameFormat frameFormatServerChargeCancelReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_CANCEL_REQ, 1);
                byte[] dataServerChargeCancelReq = new byte[frameFormatServerChargeCancelReq.getFrameLen()];
                FrameItemType.setLong2BCD(dataServerChargeCancelReq, frameFormatServerChargeCancelReq.getPos("MessageType"), frameFormatServerChargeCancelReq.getLen("MessageType"), 5091);
                FrameItemType.setByte(dataServerChargeCancelReq, frameFormatServerChargeCancelReq.getPos("Ver"), (byte) 1);
                FrameItemType.setString2BCD(dataServerChargeCancelReq, frameFormatServerChargeCancelReq.getPos("SysDatetime"), frameFormatServerChargeCancelReq.getLen("SysDatetime"), DateUtils.formatDatetime(date, "yyyyMMddHHmmss"));
                FrameItemType.setString2BCD(dataServerChargeCancelReq, frameFormatServerChargeCancelReq.getPos("oprId"), frameFormatServerChargeCancelReq.getLen("oprId"), this.oprId);
                FrameItemType.setString(dataServerChargeCancelReq, frameFormatServerChargeCancelReq.getPos("PosId"), frameFormatServerChargeCancelReq.getLen("PosId"), this.posId);
                FrameItemType.setString2BCD(dataServerChargeCancelReq, frameFormatServerChargeCancelReq.getPos("ISAM"), frameFormatServerChargeCancelReq.getLen("ISAM"), ISAM);
                FrameItemType.setString2BCD(dataServerChargeCancelReq, frameFormatServerChargeCancelReq.getPos("UnitId"), frameFormatServerChargeCancelReq.getLen("UnitId"), this.unitId);
                FrameItemType.setString2BCD(dataServerChargeCancelReq, frameFormatServerChargeCancelReq.getPos("MchntId"), frameFormatServerChargeCancelReq.getLen("MchntId"), this.mchntId);
                FrameItemType.setString2HEX(dataServerChargeCancelReq, frameFormatServerChargeCancelReq.getPos("CipherDataMAC"), frameFormatServerChargeCancelReq.getLen("CipherDataMAC"), MAC);
                FrameItemType.setLong2HEX(dataServerChargeCancelReq, frameFormatServerChargeCancelReq.getPos("CipherDataLen"), frameFormatServerChargeCancelReq.getLen("CipherDataLen"), (long) frameFormatServerChargeCancelReq.getLen("MacBuf"));
                FrameItemType.setString2HEX(dataServerChargeCancelReq, frameFormatServerChargeCancelReq.getPos("MacBuf"), frameFormatServerChargeCancelReq.getLen("MacBuf"), EncryptData);
                System.out.println("SERVER SEND:" + frameFormatServerChargeCancelReq.dump(dataServerChargeCancelReq));
                this.trafficCardServerEntity.send(this.dataSeq, dataServerChargeCancelReq, 0, dataServerChargeCancelReq.length);
                byte[] dataServerChargeCancelReqRes = this.trafficCardServerEntity.recv(SERIAL_RES_MAX_TIME);
                if (dataServerChargeCancelReqRes == null) {
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "2687");
                } else {
                    FrameFormat frameFormatServerChargeCancelReqRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.ONLINE_CARD_CHARGE_CANCEL_RES, 1);
                    System.out.println("SERVER RECV:" + frameFormatServerChargeCancelReqRes.dump(dataServerChargeCancelReqRes));
                    if (((int) FrameItemType.getBCD2Long(dataServerChargeCancelReqRes, frameFormatServerChargeCancelReqRes.getPos("MessageType"), frameFormatServerChargeCancelReqRes.getLen("MessageType"))) != 5092) {
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "2695");
                    } else {
                        String CipherDataMAC = FrameItemType.getHEX2String(dataServerChargeCancelReqRes, frameFormatServerChargeCancelReqRes.getPos("CipherDataMAC"), frameFormatServerChargeCancelReqRes.getLen("CipherDataMAC"));
                        String ServerEncryptData = EncryptUtils.byte2hex(dataServerChargeCancelReqRes, frameFormatServerChargeCancelReqRes.getPos("MacBuf"), frameFormatServerChargeCancelReqRes.getLen("MacBuf")).toUpperCase();
                        FrameFormat frameFormatSerialChargeCancelFeedback = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ONLINE_CHARGE_CANCEL_FEEDBACK);
                        byte[] dataSerialChargeCancelFeedback = new byte[frameFormatSerialChargeCancelFeedback.getFrameLen()];
                        FrameItemType.setString(dataSerialChargeCancelFeedback, 0, SerialFrameType.ONLINE_CHARGE_CANCEL_FEEDBACK);
                        FrameItemType.setString(dataSerialChargeCancelFeedback, frameFormatSerialChargeCancelFeedback.getPos("MAC"), frameFormatSerialChargeCancelFeedback.getLen("MAC"), CipherDataMAC);
                        FrameItemType.setString(dataSerialChargeCancelFeedback, frameFormatSerialChargeCancelFeedback.getPos("ServerEncryptData"), ServerEncryptData);
                        System.out.println("MODULE SEND:" + frameFormatSerialChargeCancelFeedback.dump(dataSerialChargeCancelFeedback));
                        this.trafficCardReaderEntity.send(dataSerialChargeCancelFeedback, 0, dataSerialChargeCancelFeedback.length);
                        FrameFormat frameFormatSerialChargeCancelFeedbackRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_CANCEL_FEEDBACK, "00");
                        byte[] dataSerialChargeCancelFeedbackRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                        System.out.println("MODULE RECV:" + frameFormatSerialChargeCancelFeedbackRes.dump(dataSerialChargeCancelFeedbackRes));
                        if (dataSerialChargeCancelFeedbackRes == null) {
                            hsmpResult.put("RESULT", "SUCCESS");
                        } else {
                            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(dataSerialChargeCancelFeedbackRes, frameFormatSerialChargeCancelFeedbackRes.getPos("responseCode"), frameFormatSerialChargeCancelFeedbackRes.getLen("responseCode")))) {
                                if ("00".equalsIgnoreCase(FrameItemType.getString(dataSerialChargeCancelFeedbackRes, frameFormatSerialChargeCancelFeedbackRes.getPos("hostResCode"), frameFormatSerialChargeCancelFeedbackRes.getLen("hostResCode")))) {
                                    hsmpResult.put("RESULT", "SUCCESS");
                                } else {
                                    hsmpResult.put("RESULT", "SUCCESS");
                                }
                            } else {
                                hsmpResult.put("RESULT", "SUCCESS");
                            }
                        }
                    }
                }
            } else {
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "2662");
            }
        }
        return hsmpResult;
    }

    public HashMap acctQuery(String ISAM, String cardNo, String userAcctPassword) throws Exception {
        if (userAcctPassword == null) {
            userAcctPassword = "";
        }
        if (userAcctPassword.length() > 14) {
            userAcctPassword = userAcctPassword.substring(0, 14);
        }
        String pkgUserAcctPassword = Integer.toHexString(userAcctPassword.length());
        if (pkgUserAcctPassword.length() == 1) {
            pkgUserAcctPassword = "0" + pkgUserAcctPassword;
        }
        pkgUserAcctPassword = (pkgUserAcctPassword + userAcctPassword + "FFFFFFFFFFFFFFFF").substring(0, 16);
        HashMap hsmpResult = new HashMap();
        FrameFormat frameFormatSerialAcctQueryReq = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ACCT_QUERY_REQ);
        byte[] dataSerialAcctQueryReq = new byte[frameFormatSerialAcctQueryReq.getFrameLen()];
        FrameItemType.setString(dataSerialAcctQueryReq, 0, SerialFrameType.ACCT_QUERY_REQ);
        FrameItemType.setString(dataSerialAcctQueryReq, frameFormatSerialAcctQueryReq.getPos("Password"), pkgUserAcctPassword);
        FrameItemType.setString(dataSerialAcctQueryReq, frameFormatSerialAcctQueryReq.getPos("TransTime"), DateUtils.formatDatetime(new Date(), "yyyyMMddHHmmss"));
        System.out.println("MODULE SEND:" + frameFormatSerialAcctQueryReq.dump(dataSerialAcctQueryReq));
        this.trafficCardReaderEntity.send(dataSerialAcctQueryReq, 0, dataSerialAcctQueryReq.length);
        byte[] dataSerialAcctQueryReqRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
        if (dataSerialAcctQueryReqRes == null) {
            hsmpResult.put("RESULT", "FAILED");
            hsmpResult.put("ERROR_FLAG", "2755");
        } else {
            FrameFormat frameFormatSerialAcctQueryReqRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ACCT_QUERY_REQ, "00");
            System.out.println("MODULE SEND:" + frameFormatSerialAcctQueryReqRes.dump(dataSerialAcctQueryReqRes));
            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(dataSerialAcctQueryReqRes, frameFormatSerialAcctQueryReqRes.getPos("responseCode"), frameFormatSerialAcctQueryReqRes.getLen("responseCode")))) {
                String EncPasswd = FrameItemType.getString(dataSerialAcctQueryReqRes, frameFormatSerialAcctQueryReqRes.getPos("EncPasswd"), frameFormatSerialAcctQueryReqRes.getLen("EncPasswd"));
                String MAC = FrameItemType.getString(dataSerialAcctQueryReqRes, frameFormatSerialAcctQueryReqRes.getPos("MAC"), frameFormatSerialAcctQueryReqRes.getLen("MAC"));
                String EncryptData = FrameItemType.getString(dataSerialAcctQueryReqRes, frameFormatSerialAcctQueryReqRes.getPos("EncryptData"), frameFormatSerialAcctQueryReqRes.getLen("EncryptData"));
                String readCardNo = FrameItemType.getString(dataSerialAcctQueryReqRes, frameFormatSerialAcctQueryReqRes.getPos("CardNo"), frameFormatSerialAcctQueryReqRes.getLen("CardNo"));
                FrameFormat frameFormatServerAcctQueryReq = FrameDataFormat.getServerFrameFormat(ServerFrameType.ACCT_QUERY_REQ, 1);
                byte[] dataServerAcctQueryReq = new byte[frameFormatServerAcctQueryReq.getFrameLen()];
                FrameItemType.setLong2BCD(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("MessageType"), frameFormatServerAcctQueryReq.getLen("MessageType"), 5007);
                FrameItemType.setByte(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("Ver"), (byte) 1);
                FrameItemType.setString2BCD(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("SysDatetime"), frameFormatServerAcctQueryReq.getLen("SysDatetime"), DateUtils.formatDatetime(new Date(), "yyyyMMddHHmmss"));
                FrameItemType.setString2BCD(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("oprId"), frameFormatServerAcctQueryReq.getLen("oprId"), this.oprId);
                FrameItemType.setString(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("PosId"), frameFormatServerAcctQueryReq.getLen("PosId"), this.posId);
                FrameItemType.setString2BCD(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("ISAM"), frameFormatServerAcctQueryReq.getLen("ISAM"), ISAM);
                FrameItemType.setString2BCD(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("UnitId"), frameFormatServerAcctQueryReq.getLen("UnitId"), this.unitId);
                FrameItemType.setString2BCD(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("MchntId"), frameFormatServerAcctQueryReq.getLen("MchntId"), this.mchntId);
                FrameItemType.setLong2HEX(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("BatchNo"), frameFormatServerAcctQueryReq.getLen("BatchNo"), (long) this.batchNo);
                FrameItemType.setString2HEX(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("CipherDataMAC"), frameFormatServerAcctQueryReq.getLen("CipherDataMAC"), MAC);
                FrameItemType.setLong2HEX(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("CipherDataLen"), frameFormatServerAcctQueryReq.getLen("CipherDataLen"), (long) frameFormatServerAcctQueryReq.getLen("MacBuf"));
                FrameItemType.setString2HEX(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("MacBuf"), frameFormatServerAcctQueryReq.getLen("MacBuf"), EncryptData);
                FrameItemType.setString2HEX(dataServerAcctQueryReq, frameFormatServerAcctQueryReq.getPos("EncPasswd"), frameFormatServerAcctQueryReq.getLen("EncPasswd"), EncPasswd);
                System.out.println("SERVER SEND:" + frameFormatServerAcctQueryReq.dump(dataServerAcctQueryReq));
                this.trafficCardServerEntity.send(this.dataSeq, dataServerAcctQueryReq, 0, dataServerAcctQueryReq.length);
                byte[] dataServerAcctQueryReqRes = this.trafficCardServerEntity.recv(SERIAL_RES_MAX_TIME);
                if (dataServerAcctQueryReqRes == null) {
                    hsmpResult.put("RESULT", "FAILED");
                    hsmpResult.put("ERROR_FLAG", "2792");
                } else {
                    FrameFormat frameFormatServerAcctQueryReqRes = FrameDataFormat.getServerFrameFormat(ServerFrameType.ACCT_QUERY_RES, 1);
                    System.out.println("SERVER RECV:" + frameFormatServerAcctQueryReqRes.dump(dataServerAcctQueryReqRes));
                    if (((int) FrameItemType.getBCD2Long(dataServerAcctQueryReqRes, frameFormatServerAcctQueryReqRes.getPos("MessageType"), frameFormatServerAcctQueryReqRes.getLen("MessageType"))) != 5008) {
                        hsmpResult.put("RESULT", "FAILED");
                        hsmpResult.put("ERROR_FLAG", "2800");
                    } else {
                        String CipherDataMAC = FrameItemType.getHEX2String(dataServerAcctQueryReqRes, frameFormatServerAcctQueryReqRes.getPos("CipherDataMAC"), frameFormatServerAcctQueryReqRes.getLen("CipherDataMAC"));
                        String ServerEncryptData = EncryptUtils.byte2hex(dataServerAcctQueryReqRes, frameFormatServerAcctQueryReqRes.getPos("MacBuf"), frameFormatServerAcctQueryReqRes.getLen("MacBuf")).toUpperCase();
                        FrameFormat frameFormatSerialAcctQueryReqFeedback = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.ACCT_QUERY_REQ_FEEDBACK);
                        byte[] dataSerialAcctQueryReqFeedback = new byte[frameFormatSerialAcctQueryReqFeedback.getFrameLen()];
                        FrameItemType.setString(dataSerialAcctQueryReqFeedback, 0, SerialFrameType.ACCT_QUERY_REQ_FEEDBACK);
                        FrameItemType.setString(dataSerialAcctQueryReqFeedback, frameFormatSerialAcctQueryReqFeedback.getPos("MAC"), frameFormatSerialAcctQueryReqFeedback.getLen("MAC"), CipherDataMAC);
                        FrameItemType.setString(dataSerialAcctQueryReqFeedback, frameFormatSerialAcctQueryReqFeedback.getPos("ServerEncryptData"), ServerEncryptData);
                        System.out.println("MODULE SEND:" + frameFormatSerialAcctQueryReqFeedback.dump(dataSerialAcctQueryReqFeedback));
                        this.trafficCardReaderEntity.send(dataSerialAcctQueryReqFeedback, 0, dataSerialAcctQueryReqFeedback.length);
                        FrameFormat frameFormatSerialAcctQueryReqFeedbackRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.ONLINE_CHARGE_CANCEL_FEEDBACK, "00");
                        byte[] dataSerialAcctQueryReqFeedbackRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                        System.out.println("MODULE RECV:" + frameFormatSerialAcctQueryReqFeedbackRes.dump(dataSerialAcctQueryReqFeedbackRes));
                        if (dataSerialAcctQueryReqFeedbackRes == null) {
                            hsmpResult.put("RESULT", "SUCCESS");
                        } else {
                            if (SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(dataSerialAcctQueryReqFeedbackRes, frameFormatSerialAcctQueryReqFeedbackRes.getPos("responseCode"), frameFormatSerialAcctQueryReqFeedbackRes.getLen("responseCode")))) {
                                if ("00".equalsIgnoreCase(FrameItemType.getString(dataSerialAcctQueryReqFeedbackRes, frameFormatSerialAcctQueryReqFeedbackRes.getPos("hostResCode"), frameFormatSerialAcctQueryReqFeedbackRes.getLen("hostResCode")))) {
                                    hsmpResult.put("RESULT", "SUCCESS");
                                } else {
                                    hsmpResult.put("RESULT", "SUCCESS");
                                }
                            } else {
                                hsmpResult.put("RESULT", "SUCCESS");
                            }
                        }
                    }
                }
            } else {
                hsmpResult.put("RESULT", "FAILED");
                hsmpResult.put("ERROR_FLAG", "2763");
            }
        }
        return hsmpResult;
    }

    public HashMap lockCard(String cardNo) {
        HashMap hsmpModel = readModel();
        if (hsmpModel != null) {
            String ISAM = (String) hsmpModel.get("ISAM");
            if (readCard(0) != null) {
                this.PsamTransNo++;
                FrameFormat frameFormatSerialLockReq = FrameDataFormat.getSerialRequestFrameFormat(SerialFrameType.LOCK_CARD);
                byte[] dataSerialLockReq = new byte[frameFormatSerialLockReq.getFrameLen()];
                FrameItemType.setString(dataSerialLockReq, 0, SerialFrameType.LOCK_CARD);
                FrameItemType.setString2BCD(dataSerialLockReq, frameFormatSerialLockReq.getPos("CardNo"), frameFormatSerialLockReq.getLen("CardNo"), cardNo);
                FrameItemType.setLong2HString(dataSerialLockReq, frameFormatSerialLockReq.getPos("TermTransSeq"), frameFormatSerialLockReq.getLen("TermTransSeq"), (long) this.PsamTransNo);
                FrameItemType.setString2BCD(dataSerialLockReq, frameFormatSerialLockReq.getPos("TransTime"), frameFormatSerialLockReq.getLen("TransTime"), DateUtils.formatDatetime(new Date(), "yyyyMMddHHmmss"));
                this.trafficCardReaderEntity.send(dataSerialLockReq, 0, dataSerialLockReq.length);
                byte[] dataSerialLockReqRes = this.trafficCardReaderEntity.recv(SERIAL_RES_MAX_TIME);
                if (dataSerialLockReqRes != null) {
                    FrameFormat frameFormatSerialLockReqRes = FrameDataFormat.getSerialResponseFrameFormat(SerialFrameType.LOCK_CARD, "00");
                    if (!SerialFrameType.FIND_CARD.equalsIgnoreCase(FrameItemType.getString(dataSerialLockReqRes, frameFormatSerialLockReqRes.getPos("responseCode"), frameFormatSerialLockReqRes.getLen("responseCode")))) {
                    }
                }
            }
        }
        return null;
    }
}
