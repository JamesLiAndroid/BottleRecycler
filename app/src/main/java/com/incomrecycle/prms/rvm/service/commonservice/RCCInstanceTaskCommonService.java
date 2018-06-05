package com.incomrecycle.prms.rvm.service.commonservice;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.StatFs;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.sqlite.DBQuery;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlDeleteBuilder;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.NumberUtils;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef;
import com.incomrecycle.prms.rvm.common.SysDef.AlarmId;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.CardType;
import com.incomrecycle.prms.rvm.common.SysDef.MsgType;
import com.incomrecycle.prms.rvm.common.SysDef.ProductType;
import com.incomrecycle.prms.rvm.common.SysDef.maintainOptContent;
import com.incomrecycle.prms.rvm.common.SysDef.networkSts;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import com.incomrecycle.prms.rvm.service.traffic.TrafficEntity;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import it.sauronsoftware.ftp4j.FTPClient;

public class RCCInstanceTaskCommonService implements AppCommonService {
    private static DecimalFormat df = new DecimalFormat("0.00");
    private static final Logger logger = LoggerFactory.getLogger("RCCInstanceTaskCommonService");

    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get(AllAdvertisement.MEDIA_TYPE);
        if ("RVM_OPT".equalsIgnoreCase(type)) {
            RCCInstanceTaskOPT(svcName, subSvnName, hsmpParam);
        }
        if ("RVM_ALARM_INST".equalsIgnoreCase(type)) {
            RCCInstanceTaskAlarm(svcName, subSvnName, hsmpParam);
        }
        if ("RVM_RECHARGE_STS".equalsIgnoreCase(type)) {
            RCCInstanceTaskSts(svcName, subSvnName, hsmpParam);
        }
        if ("MAINTAIN_OPT_CON".equalsIgnoreCase(type)) {
            MaintainOptCon(svcName, subSvnName, hsmpParam);
        }
        if ("RVM_RECHARGE_FEEDBACK_STS".equalsIgnoreCase(type)) {
            RCCInstanceTaskRechargeFeedback(svcName, subSvnName, hsmpParam);
        }
        if ("RVM_BMCHARGE_FEEDBACK_STS".equalsIgnoreCase(type)) {
            RCCInstanceTaskBMchargeFeedback(svcName, subSvnName, hsmpParam);
        }
        if ("RVM_GGK_FEEDBACK_STS".equalsIgnoreCase(type)) {
            RCCInstanceTaskGGKFeedback(svcName, subSvnName, hsmpParam);
        }
        if ("RVM_CODE_BACKUP".equalsIgnoreCase(type)) {
            backupRvmCode(svcName, subSvnName, hsmpParam);
        }
        if ("UPLOAD_PICTURE".equalsIgnoreCase(type)) {
            uploadpicture(svcName, subSvnName, hsmpParam);
        }
        return null;
    }

    public HashMap MaintainOptCon(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        int j;
        HashMap hasResult = new HashMap();
        List<String> allServiceList = new ArrayList();
        List<String> disenableServiceList = new ArrayList();
        List<Integer> audioList = new ArrayList();
        List<Integer> informList = new ArrayList();
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "SERVICE_DISABLED_SET");
        CommTable commTableSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        String SYS_CODE_VALUE = "";
        if (commTableSysCode.getRecordCount() > 0) {
            SYS_CODE_VALUE = commTableSysCode.getRecord(0).get("SYS_CODE_VALUE");
        }
        String[] service_set = SYS_CODE_VALUE.split(",");
        for (int i = 0; i < service_set.length; i++) {
            if (service_set[i].equalsIgnoreCase("CARD")) {
                disenableServiceList.add(maintainOptContent.STOP_CARD);
            }
            if (service_set[i].equalsIgnoreCase("COUPON")) {
                disenableServiceList.add(maintainOptContent.STOP_COUPON);
            }
            if (service_set[i].equalsIgnoreCase("DONATION")) {
                disenableServiceList.add(maintainOptContent.STOP_DONATION);
            }
            if (service_set[i].equalsIgnoreCase("PHONE")) {
                disenableServiceList.add(maintainOptContent.STOP_PHONE);
            }
            if (service_set[i].equalsIgnoreCase("QRCODE")) {
                disenableServiceList.add(maintainOptContent.STOP_LNKCARD);
            }
            if (service_set[i].equalsIgnoreCase("TRANSPORTCARD")) {
                disenableServiceList.add(maintainOptContent.STOP_ONECARD);
            }
            if (service_set[i].equalsIgnoreCase("BOTTLE")) {
                disenableServiceList.add(maintainOptContent.STOP_RECYCLE_BOTTLE);
            }
            if (service_set[i].equalsIgnoreCase("PAPER")) {
                disenableServiceList.add(maintainOptContent.STOP_RECYCLE_PAPER);
            }
            if (service_set[i].equalsIgnoreCase("CONSERVICE")) {
                disenableServiceList.add(maintainOptContent.STOP_CONVENIENCE);
            }
            if (service_set[i].equalsIgnoreCase("WECHAT")) {
                disenableServiceList.add(maintainOptContent.STOP_WECHAT);
            }
            if (service_set[i].equalsIgnoreCase("BDJ")) {
                disenableServiceList.add(maintainOptContent.STOP_BDJ);
            }
        }
        allServiceList.addAll(disenableServiceList);
        CommTable commTable = dbQuery.getCommTable("select * from RVM_MEDIA");
        if (commTable.getRecordCount() > 0) {
            for (j = 0; j < commTable.getRecord().size(); j++) {
                audioList.add(Integer.valueOf(Integer.parseInt(commTable.getRecord(j).get("MEDIA_ID"))));
            }
        }
        Date tDate = new Date();
        SqlWhereBuilder sqlWhereTXTBuilder = new SqlWhereBuilder();
        sqlWhereTXTBuilder.addDatetimeLessEqualsTo("BEGIN_TIME", tDate).addDatetimeGreaterEqualsTo("END_TIME", tDate);
        CommTable commTXTTable = dbQuery.getCommTable("select * from RVM_TXT_AD" + sqlWhereTXTBuilder.toSqlWhere("where"));
        if (commTXTTable.getRecordCount() > 0) {
            for (j = 0; j < commTXTTable.getRecord().size(); j++) {
                informList.add(Integer.valueOf(Integer.parseInt(commTXTTable.getRecord(j).get("SBAR_ID"))));
            }
        }
        Long Time = Long.valueOf(new Date().getTime());
        String QuTime = Time.toString();
        HashMap hsmpSend = new HashMap();
        hsmpSend.put("MES_TYPE", MsgType.RVM_INFO_UPDATE);
        hsmpSend.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpSend.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
        hsmpSend.put("QU_TIME", QuTime);
        hsmpSend.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + Time);
        hsmpSend.put("RVM_TYPE", SysConfig.get("RVM.MODE"));
        hsmpSend.put("SER_LIST", allServiceList);
        hsmpSend.put("RESTART_TIME", SysConfig.get("BOOT.TIME"));
        hsmpSend.put("VIDEO_LIST", audioList);
        hsmpSend.put("INFO_LIST", informList);
        hsmpSend.put("DATADSK", readDataSize());
        hsmpSend.put("SDCARDDSK", readSDcardSize());
        if (networkSts.tcpTotal > 0) {
            hsmpSend.put("TCP_TOTAL", "" + networkSts.tcpTotal);
        }
        hsmpSend.put("RVM_VERSION_ID", SysConfig.get("RVM.VERSION.ID"));
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        String putdownCount = "0";
        String putdownCountDelta = "0";
        String putdownCountReal = "0";
        String putdownWeight = "0.00";
        try {
            HashMap<String, Object> hsmpStorageCount = guiCommonService.execute("GUIQueryCommonService", "queryStorageCount", null);
            putdownCount = (String) hsmpStorageCount.get("STORAGE_CURR_COUNT");
            putdownCountDelta = (String) hsmpStorageCount.get("STORAGE_CURR_COUNT_DELTA");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (StringUtils.isBlank(putdownCount)) {
            putdownCount = "0";
        }
        if (StringUtils.isBlank(putdownCountDelta)) {
            putdownCountDelta = "0";
        }
        if (Integer.parseInt(putdownCount) > Integer.parseInt(putdownCountDelta)) {
            putdownCountReal = Integer.toString(Integer.parseInt(putdownCount) - Integer.parseInt(putdownCountDelta));
        }
        putdownWeight = df.format(Double.parseDouble((String) guiCommonService.execute("GUIQueryCommonService", "queryStorageWeight", null).get("STORAGE_CURR_PAPER_WEIGH")));
        hsmpSend.put("BOTTLE_NOW", putdownCountReal);
        hsmpSend.put("BOTTLE_ACCOUNT", putdownCount);
        hsmpSend.put("PAPER_NOW", putdownWeight);
        HashMap RouterMap = (HashMap) SysConfig.getObj("SIMCard");
        int CSQ = -1;
        String IMSI = null;
        String FLOW = null;
        if (!(RouterMap == null || RouterMap.isEmpty())) {
            try {
                CSQ = Integer.parseInt((String) RouterMap.get("CSQ"));
            } catch (Exception e2) {
            }
            IMSI = (String) RouterMap.get("IMEI");
            try {
                FLOW = NumberUtils.toScale((double) (Integer.parseInt((String) RouterMap.get("FLOW")) / 1024), 2);
            } catch (Exception e3) {
            }
        }
        hsmpSend.put("CSQ", Integer.valueOf(CSQ));
        hsmpSend.put("IMSI", IMSI);
        hsmpSend.put("FLOW", FLOW);
        logger.debug("SIMCard=" + RouterMap);
        logger.debug("Router info: IMEI=" + IMSI + ", CSQ=" + CSQ + ", FLOW=" + FLOW);
        try {
            HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
            if (hsmpretPkg == null) {
                return null;
            }
            String termNo = (String) hsmpretPkg.get("TERM_NO");
            if ("RESPONSE".equalsIgnoreCase((String) hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo) && Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM"))).intValue() == 1) {
                hasResult.put("RESULT", NetworkUtils.NET_STATE_SUCCESS);
                return hasResult;
            }
            return null;
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return null;
    }

    public HashMap RCCInstanceTaskSts(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        CommTable commTable = dbQuery.getCommTable("select * from RVM_RECHARGE " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                String CARD_NUM = commTable.getRecord(i).get("CARD_NO");
                String CARD_TYPE = commTable.getRecord(i).get("CARD_TYPE");
                if ("2".equalsIgnoreCase(CARD_TYPE)) {
                    String RECHARGE_STS = commTable.getRecord(i).get("RECHARGE_STATE");
                    String ORDER_ID = commTable.getRecord(i).get("ORDER_ID");
                    String STATUS_MES_TYPE = MsgType.RVM_RECHANGE_STATUS;
                    String QUTime = Long.valueOf(new Date().getTime()).toString();
                    HashMap hsmpPakg = new HashMap();
                    hsmpPakg.put("MES_TYPE", STATUS_MES_TYPE);
                    hsmpPakg.put("TERM_NO", SysConfig.get("RVM.CODE"));
                    hsmpPakg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
                    hsmpPakg.put("QU_TIME", QUTime);
                    hsmpPakg.put("CARD_TYPE", "2");
                    hsmpPakg.put("CARD_NO", CARD_NUM);
                    hsmpPakg.put("ORDER_NO", ORDER_ID);
                    hsmpPakg.put("ORDER_STATUS", RECHARGE_STS);
                    hsmpPakg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + QUTime);
                    try {
                        HashMap hsmap = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPakg)));
                        String termNo_sts = (String) hsmap.get("TERM_NO");
                        if (((String) hsmap.get("MES_TYPE")).equalsIgnoreCase("RESPONSE") && termNo_sts.equalsIgnoreCase(SysConfig.get("RVM.CODE"))) {
                            HashMap hsmpRet = new HashMap();
                            if (Integer.valueOf(Integer.parseInt((String) hsmap.get("CONFIRM"))).intValue() == 1) {
                                SqlWhereBuilder sqlWhereBuild = new SqlWhereBuilder();
                                sqlWhereBuilder.addStringEqualsTo("CARD_NO", CARD_NUM);
                                sqlWhereBuilder.addStringEqualsTo("CARD_TYPE", CARD_TYPE);
                                SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_RECHARGE");
                                sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
                                SQLiteExecutor.execSql(sqliteDatabase, sqlDeleteBuilder.toSql());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public HashMap RCCInstanceTaskOPT(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String OPT_ID = (String) hsmpParam.get("OPT_ID");
        HashMap hsmpSend = new HashMap();
        String putdownCount = "0";
        String putdownCountDelta = "0";
        String putdownCountReal = "0";
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("OPT_TYPE", "RECYCLE").addNumberEqualsTo("OPT_STATUS", Integer.valueOf(1)).addNumberEqualsTo("OPT_ID", OPT_ID);
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        CommTable commTable = dbQuery.getCommTable("select * from RVM_OPT " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            try {
                HashMap<String, Object> hsmpStorageCount = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryStorageCount", null);
                putdownCount = (String) hsmpStorageCount.get("STORAGE_CURR_COUNT");
                putdownCountDelta = (String) hsmpStorageCount.get("STORAGE_CURR_COUNT_DELTA");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (StringUtils.isBlank(putdownCount)) {
                putdownCount = "0";
            }
            if (StringUtils.isBlank(putdownCountDelta)) {
                putdownCountDelta = "0";
            }
            if (Integer.parseInt(putdownCount) > Integer.parseInt(putdownCountDelta)) {
                putdownCountReal = Integer.toString(Integer.parseInt(putdownCount) - Integer.parseInt(putdownCountDelta));
            }
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                String CardNO = commTable.getRecord(i).get("CARD_NO");
                String RecycleQuantity = commTable.getRecord(i).get("PRODUCT_AMOUNT");
                String VendingWay = commTable.getRecord(i).get(AllAdvertisement.VENDING_WAY);
                if (StringUtils.isBlank(VendingWay)) {
                    VendingWay = "DONATION";
                }
                String productType = commTable.getRecord(i).get("PRODUCT_TYPE");
                if (StringUtils.isBlank(productType)) {
                    productType = "BOTTLE";
                }
                String VendingMoney = commTable.getRecord(i).get("PROFIT_AMOUNT");
                if ("DONATION".equalsIgnoreCase(VendingWay)) {
                    VendingMoney = "0";
                }
                Date date = null;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(commTable.getRecord(i).get("OPT_TIME"));
                } catch (ParseException e2) {
                    e2.printStackTrace();
                }
                Long VendTime = Long.valueOf(date.getTime());
                String SELECT_FLAG = commTable.getRecord(i).get(AllAdvertisement.SELECT_FLAG);
                String OPBatchId = SysConfig.get("RVM.CODE") + "_" + VendTime + OPT_ID;
                String cardType = commTable.getRecord(i).get("CARD_TYPE");
                if (cardType == null) {
                    cardType = CardType.NOCARD;
                }
                hsmpSend.put("MES_TYPE", MsgType.RVM_RECYCLE_DETAIL);
                hsmpSend.put("TERM_NO", SysConfig.get("RVM.CODE"));
                hsmpSend.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
                hsmpSend.put("CARD_NO", CardNO);
                hsmpSend.put(AllAdvertisement.SELECT_FLAG, SELECT_FLAG);
                hsmpSend.put("PRODUCT_TYPE", ProductType.getMsgProductType(productType));
                hsmpSend.put("RECYCLE_QUANTITY", RecycleQuantity);
                hsmpSend.put(AllAdvertisement.VENDING_WAY, SysDef.VendingWay.getMsgVendingRebateFlag(VendingWay));
                hsmpSend.put("VENDING_MONEY", VendingMoney);
                hsmpSend.put("VENDING_TIME", VendTime.toString());
                hsmpSend.put("OP_BATCH_ID", OPBatchId);
                hsmpSend.put("CARD_TYPE", CardType.getMsgCardType(cardType));
                SqlWhereBuilder sqlWhereBuilderOPTBottle = new SqlWhereBuilder();
                sqlWhereBuilderOPTBottle.addNumberEqualsTo("OPT_ID", OPT_ID);
                CommTable commTableOPTBottle = dbQuery.getCommTable("select * from RVM_OPT_BOTTLE " + sqlWhereBuilderOPTBottle.toSqlWhere("where"));
                List BarValuesList = new ArrayList();
                if (commTableOPTBottle.getRecordCount() != 0) {
                    for (int j = 0; j < commTableOPTBottle.getRecordCount(); j++) {
                        String BottleBarCode = commTableOPTBottle.getRecord(j).get("BOTTLE_BAR_CODE");
                        Double BottleAmount = Double.valueOf(commTableOPTBottle.getRecord(j).get("BOTTLE_AMOUNT"));
                        int BottleCount = Integer.parseInt(commTableOPTBottle.getRecord(j).get("BOTTLE_COUNT"));
                        for (int k = 0; k < BottleCount; k++) {
                            BarValuesList.add(BottleBarCode + "=" + BottleAmount.toString());
                        }
                    }
                }
                hsmpSend.put("BAR_VALUES", BarValuesList);
                hsmpSend.put("REAL_BOTTLE_NUM", putdownCountReal);
                hsmpSend.put("TOTAL_BOTTLE_NUM", putdownCount);
                HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
                if (hsmpretPkg == null) {
                    return null;
                }
                SqlWhereBuilder sqlWhereBuilderOptId;
                SqlUpdateBuilder sqlUpdateBuilder;
                String bottleNum;
                HashMap<String, String> hsmpGUIEvent;
                String msgType = (String) hsmpretPkg.get("MES_TYPE");
                String termNo = (String) hsmpretPkg.get("TERM_NO");
                if ("RESPONSE".equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                    try {
                        Integer Confirm = Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM")));
                        if (Confirm.intValue() == 1) {
                            sqlWhereBuilderOptId = new SqlWhereBuilder();
                            sqlWhereBuilderOptId.addNumberEqualsTo("OPT_ID", OPT_ID);
                            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
                            sqlUpdateBuilder.setNumber("OPT_STATUS", Integer.valueOf(2)).setSqlWhere(sqlWhereBuilderOptId);
                            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                            bottleNum = (String) hsmpretPkg.get("BOTTLE_NUM");
                            hsmpGUIEvent = new HashMap();
                            hsmpGUIEvent.put("EVENT", "INFORM");
                            hsmpGUIEvent.put("INFORM", "BOTTLE_NUM");
                            hsmpGUIEvent.put("BOTTLE_NUM", bottleNum);
                            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
                        }
                        if (Confirm.intValue() == 0) {
                            sqlWhereBuilderOptId = new SqlWhereBuilder();
                            sqlWhereBuilderOptId.addNumberEqualsTo("OPT_ID", OPT_ID);
                            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
                            sqlUpdateBuilder.setNumber("OPT_STATUS", Integer.valueOf(-1)).setSqlWhere(sqlWhereBuilderOptId);
                            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                        }
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
                if (MsgType.RCC_CARD_STATUS.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                    sqlWhereBuilderOptId = new SqlWhereBuilder();
                    sqlWhereBuilderOptId.addNumberEqualsTo("OPT_ID", OPT_ID);
                    sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
                    sqlUpdateBuilder.setNumber("OPT_STATUS", Integer.valueOf(2)).setSqlWhere(sqlWhereBuilderOptId);
                    SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                    bottleNum = (String) hsmpretPkg.get("BOTTLE_NUM");
                    hsmpGUIEvent = new HashMap();
                    hsmpGUIEvent.put("EVENT", "INFORM");
                    hsmpGUIEvent.put("INFORM", "BOTTLE_NUM");
                    hsmpGUIEvent.put("BOTTLE_NUM", bottleNum);
                    ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
                }
            }
        }
        return null;
    }

    public HashMap RCCInstanceTaskAlarm(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String alarmInstId = (String) hsmpParam.get("ALARM_INST_ID");
        String putdownCountReal = "0";
        try {
            putdownCountReal = (String) CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryStorageCount", null).get("BOTTLE_NOW");
            if (StringUtils.isBlank(putdownCountReal) || "0".endsWith(putdownCountReal)) {
                putdownCountReal = (String) ServiceGlobal.getCurrentSession("BOTTLE_NOW");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        HashMap hsmpSend = new HashMap();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("ALARM_INST_ID", alarmInstId).addNumberEqualsTo("UPLOAD_FLAG", Integer.valueOf(0));
        CommTable commTable = dbQuery.getCommTable("select * from RVM_ALARM_INST " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                String alarmTime = commTable.getRecord(i).get("ALARM_TIME");
                String startOperTime = commTable.getRecord(i).get("START_OPER_DATE");
                String alarmCode = commTable.getRecord(i).get("ALARM_ID");
                String operSts = commTable.getRecord(i).get("ALARM_STATUS");
                String userStaffId = commTable.getRecord(i).get("USER_STAFF_ID");
                DateFormat dd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                Date date2 = null;
                try {
                    date = dd.parse(alarmTime);
                    if (!(startOperTime == null || "".equalsIgnoreCase(startOperTime))) {
                        date2 = dd.parse(startOperTime);
                    }
                } catch (ParseException e2) {
                    e2.printStackTrace();
                }
                Long AMTime = Long.valueOf(date.getTime());
                String finalAlarmInstId = new SimpleDateFormat("yyyyMMdd").format(date) + "_" + alarmInstId;
                if (AlarmId.isSingleAlarm(alarmCode)) {
                    finalAlarmInstId = alarmCode;
                }
                hsmpSend.put("MES_TYPE", MsgType.RVM_ALARM);
                hsmpSend.put("TERM_NO", SysConfig.get("RVM.CODE"));
                hsmpSend.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
                hsmpSend.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + AMTime);
                hsmpSend.put("AM_ID", finalAlarmInstId);
                hsmpSend.put("AM_BEGIN_TIME", AMTime.toString());
                hsmpSend.put("AM_CODE", alarmCode);
                hsmpSend.put("OPER_STS", operSts);
                if (startOperTime == null || "".equalsIgnoreCase(startOperTime)) {
                    hsmpSend.put("START_OPER_DATE", null);
                } else {
                    hsmpSend.put("START_OPER_DATE", Long.valueOf(date2.getTime()).toString());
                }
                hsmpSend.put("OPSER_STAFF", userStaffId);
                hsmpSend.put("BOTTLE_NOW", putdownCountReal);
                HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
                if (hsmpretPkg == null) {
                    return null;
                }
                String termNo = (String) hsmpretPkg.get("TERM_NO");
                if ("RESPONSE".equalsIgnoreCase((String) hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                    try {
                        SqlUpdateBuilder sqlUpdateBuilder;
                        SqlWhereBuilder sqlWhereBuilderAlarm;
                        Integer Confirm = Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM")));
                        if (Confirm.intValue() == 1) {
                            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_ALARM_INST");
                            sqlWhereBuilderAlarm = new SqlWhereBuilder();
                            sqlWhereBuilderAlarm.addNumberEqualsTo("ALARM_INST_ID", Integer.valueOf(Integer.parseInt(alarmInstId)));
                            sqlUpdateBuilder.setNumber("UPLOAD_FLAG", Integer.valueOf(1)).setSqlWhere(sqlWhereBuilderAlarm);
                            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                        }
                        if (Confirm.intValue() == 0) {
                            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_ALARM_INST");
                            sqlWhereBuilderAlarm = new SqlWhereBuilder();
                            sqlWhereBuilderAlarm.addNumberEqualsTo("ALARM_INST_ID", Integer.valueOf(Integer.parseInt(alarmInstId)));
                            sqlUpdateBuilder.setNumber("UPLOAD_FLAG", Integer.valueOf(-1)).setSqlWhere(sqlWhereBuilderAlarm);
                            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                        }
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public HashMap RCCInstanceTaskRechargeFeedback(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("UPLOAD_FLAG", Integer.valueOf(1));
        CommTable commTable = dbQuery.getCommTable("select * from RVM_RECHARGE_FEEDBACK " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            int i = 0;
            while (i < commTable.getRecordCount()) {
                String POS_COMM_SEQ = commTable.getRecord(i).get("POS_COMM_SEQ");
                String UID_REC = commTable.getRecord(i).get("UID_REC");
                String ISAM = commTable.getRecord(i).get("ISAM");
                String CARD_NO = commTable.getRecord(i).get("CARD_NO");
                String CHARGE_AMOUNT = commTable.getRecord(i).get("CHARGE_AMOUNT");
                String BEGIN_MONEY = commTable.getRecord(i).get("BEGIN_MONEY");
                String END_MONEY = commTable.getRecord(i).get("END_MONEY");
                String CHARGE_STATUS = commTable.getRecord(i).get("CHARGE_STATUS");
                String QUTime = Long.valueOf(new Date().getTime()).toString();
                HashMap hsmpPakg = new HashMap();
                hsmpPakg.put("MES_TYPE", MsgType.RVM_CHANGE_RESULT);
                hsmpPakg.put("TERM_NO", SysConfig.get("RVM.CODE"));
                hsmpPakg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
                hsmpPakg.put("QU_TIME", QUTime);
                hsmpPakg.put("POS_COMM_SEQ", POS_COMM_SEQ);
                hsmpPakg.put("UID_REC", UID_REC);
                hsmpPakg.put("ISAM", ISAM);
                hsmpPakg.put("CARD_NO", CARD_NO);
                hsmpPakg.put("CHARGE_AMOUNT", CHARGE_AMOUNT);
                hsmpPakg.put("BEGIN_MONEY", BEGIN_MONEY);
                hsmpPakg.put("END_MONEY", END_MONEY);
                hsmpPakg.put("CHARGE_STATUS", CHARGE_STATUS);
                hsmpPakg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + QUTime);
                try {
                    HashMap hsmap = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPakg)));
                    if (hsmap == null || hsmap.size() == 0) {
                        return null;
                    }
                    String termNo_sts = (String) hsmap.get("TERM_NO");
                    if ("RESPONSE".equalsIgnoreCase((String) hsmap.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo_sts)) {
                        HashMap hsmpRet = new HashMap();
                        if (Integer.valueOf(Integer.parseInt((String) hsmap.get("CONFIRM"))).intValue() == 1) {
                            SqlWhereBuilder sqlWhereBuild = new SqlWhereBuilder();
                            sqlWhereBuilder.addStringEqualsTo("POS_COMM_SEQ", POS_COMM_SEQ).addStringEqualsTo("ISAM", ISAM).addStringEqualsTo("UID_REC", UID_REC);
                            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_RECHARGE_FEEDBACK");
                            sqlUpdateBuilder.setString("UPLOAD_FLAG", Integer.valueOf(2)).setSqlWhere(sqlWhereBuild);
                            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                        }
                    }
                    i++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public HashMap RCCInstanceTaskBMchargeFeedback(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("UPLOAD_FLAG", Integer.valueOf(1));
        CommTable commTable = dbQuery.getCommTable("select * from RVM_BMCHARGE_FEEDBACK " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            int i = 0;
            while (i < commTable.getRecordCount()) {
                String POS_COMM_SEQ = commTable.getRecord(i).get("POS_COMM_SEQ");
                String UID_REC = commTable.getRecord(i).get("UID_REC");
                String ISAM = commTable.getRecord(i).get("ISAM");
                String CARD_NO = commTable.getRecord(i).get("CARD_NO");
                String BUSINESS_TYPE = commTable.getRecord(i).get("BUSINESS_TYPE");
                String BUSINESS_NO = commTable.getRecord(i).get("BUSINESS_NO");
                String CHARGE_AMOUNT = commTable.getRecord(i).get("CHARGE_AMOUNT");
                String CHARGE_TIME = commTable.getRecord(i).get("CHARGE_TIME");
                String BEGIN_MONEY = commTable.getRecord(i).get("BEGIN_MONEY");
                String END_MONEY = commTable.getRecord(i).get("END_MONEY");
                String QUTime = Long.valueOf(new Date().getTime()).toString();
                HashMap hsmpPakg = new HashMap();
                hsmpPakg.put("MES_TYPE", MsgType.RVM_BMCHANGE_RESULT);
                hsmpPakg.put("TERM_NO", SysConfig.get("RVM.CODE"));
                hsmpPakg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
                hsmpPakg.put("QU_TIME", QUTime);
                hsmpPakg.put("POS_COMM_SEQ", POS_COMM_SEQ);
                hsmpPakg.put("BUSINESS_TYPE", BUSINESS_TYPE);
                hsmpPakg.put("BUSINESS_NO", BUSINESS_NO);
                hsmpPakg.put("ISAM", ISAM);
                hsmpPakg.put("CARD_NO", CARD_NO);
                hsmpPakg.put("CHARGE_AMOUNT", CHARGE_AMOUNT);
                hsmpPakg.put("CHARGE_TIME", CHARGE_TIME);
                hsmpPakg.put("BEGIN_MONEY", BEGIN_MONEY);
                hsmpPakg.put("END_MONEY", END_MONEY);
                hsmpPakg.put("OP_BATCH_ID", UID_REC);
                try {
                    HashMap hsmap = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPakg)));
                    if (hsmap == null || hsmap.size() == 0) {
                        return null;
                    }
                    String termNo_sts = (String) hsmap.get("TERM_NO");
                    if ("RESPONSE".equalsIgnoreCase((String) hsmap.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo_sts)) {
                        HashMap hsmpRet = new HashMap();
                        if (Integer.valueOf(Integer.parseInt((String) hsmap.get("CONFIRM"))).intValue() == 1) {
                            SqlWhereBuilder sqlWhereBuild = new SqlWhereBuilder();
                            sqlWhereBuilder.addStringEqualsTo("POS_COMM_SEQ", POS_COMM_SEQ).addStringEqualsTo("ISAM", ISAM).addStringEqualsTo("UID_REC", UID_REC);
                            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_BMCHARGE_FEEDBACK");
                            sqlUpdateBuilder.setString("UPLOAD_FLAG", Integer.valueOf(2)).setSqlWhere(sqlWhereBuild);
                            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                        }
                    }
                    i++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public HashMap RCCInstanceTaskGGKFeedback(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("UPLOAD_FLAG", Integer.valueOf(1)).addNumberEqualsTo("BUSINESS_TYPE", Integer.valueOf(4));
        CommTable commTable = dbQuery.getCommTable("select * from RVM_BMCHARGE_FEEDBACK " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            int i = 0;
            while (i < commTable.getRecordCount()) {
                String POS_COMM_SEQ = commTable.getRecord(i).get("POS_COMM_SEQ");
                String UID_REC = commTable.getRecord(i).get("UID_REC");
                String ISAM = commTable.getRecord(i).get("ISAM");
                String CARD_NO = commTable.getRecord(i).get("CARD_NO");
                String BUSINESS_NO = commTable.getRecord(i).get("BUSINESS_NO");
                List businessList = new ArrayList();
                businessList.add(BUSINESS_NO);
                String QUTime = Long.valueOf(new Date().getTime()).toString();
                HashMap hsmpPakg = new HashMap();
                hsmpPakg.put("MES_TYPE", MsgType.RVM_YKT_RESULT);
                hsmpPakg.put("TERM_NO", SysConfig.get("RVM.CODE"));
                hsmpPakg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
                hsmpPakg.put("QU_TIME", QUTime);
                hsmpPakg.put("GGK_OK_LIST", businessList);
                hsmpPakg.put("YKT_CARD", CARD_NO);
                hsmpPakg.put("OP_BATCH_ID", UID_REC);
                try {
                    HashMap hsmap = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPakg)));
                    if (hsmap == null || hsmap.size() == 0) {
                        return null;
                    }
                    String termNo_sts = (String) hsmap.get("TERM_NO");
                    if ("RESPONSE".equalsIgnoreCase((String) hsmap.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo_sts)) {
                        HashMap hsmpRet = new HashMap();
                        if (Integer.valueOf(Integer.parseInt((String) hsmap.get("CONFIRM"))).intValue() == 1) {
                            SqlWhereBuilder sqlWhereBuild = new SqlWhereBuilder();
                            sqlWhereBuilder.addStringEqualsTo("POS_COMM_SEQ", POS_COMM_SEQ).addStringEqualsTo("ISAM", ISAM).addStringEqualsTo("UID_REC", UID_REC);
                            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_BMCHARGE_FEEDBACK");
                            sqlUpdateBuilder.setString("UPLOAD_FLAG", Integer.valueOf(2)).setSqlWhere(sqlWhereBuild);
                            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                        }
                    }
                    i++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public HashMap backupRvmCode(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        new InitCommonService().execute("InitCommonService", "rvmCodeBackup", null);
        return null;
    }

    private String readDataSize() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long blockSize = (long) stat.getBlockSize();
        double blockCount = (double) stat.getBlockCount();
        return NumberUtils.toScale(((blockCount - ((double) stat.getAvailableBlocks())) / blockCount) * 100.0d, 0);
    }

    private String readSDcardSize() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long blockSize = (long) stat.getBlockSize();
        double blockCount = (double) stat.getBlockCount();
        return NumberUtils.toScale(((blockCount - ((double) stat.getAvailableBlocks())) / blockCount) * 100.0d, 0);
    }

    private HashMap uploadpicture(String svcName, String subSvnName, HashMap hsmpParam) {
        Properties prop;
        if (!PropUtils.hasEncryptFlag(PropUtils.loadEncryptFile(new File(SysConfig.get("RVM.RESPARAM.FILE"))))) {
            PropUtils.transferEncryptFile(new File(SysConfig.get("RVM.RESPARAM.FILE")));
        }
        File rvmResParamFile = new File(SysConfig.get("RVM.RESPARAM.FILE"));
        boolean updateResParamFile = false;
        if (!rvmResParamFile.exists() || rvmResParamFile.length() == 0) {
            updateResParamFile = true;
        } else {
            prop = PropUtils.loadEncryptFile(new File(SysConfig.get("RVM.RESPARAM.FILE")));
            if (StringUtils.isBlank(prop.getProperty("FTP_IP")) || StringUtils.isBlank(prop.getProperty("FTP_USER"))) {
                updateResParamFile = true;
            }
        }
        if (updateResParamFile) {
            prop = PropUtils.loadResource(SysConfig.get("RVM.RESPARAM.RES"));
            PropUtils.saveEncryptFile(rvmResParamFile, prop);
        } else {
            prop = PropUtils.loadEncryptFile(new File(SysConfig.get("RVM.RESPARAM.FILE")));
        }
        String FTP_IP = prop.getProperty("FTP_IP");
        String FTP_CMD_PORT = prop.getProperty("FTP_CMD_PORT");
        String FTP_DATA_PORT = prop.getProperty("FTP_DATA_PORT");
        String FTP_USER = prop.getProperty("FTP_USER");
        String FTP_PASSWORD = prop.getProperty("FTP_PASSWORD");
        String FTP_PICTURE_PATH = prop.getProperty("FTP_PICTURE_PATH");
        if (StringUtils.isBlank(FTP_CMD_PORT)) {
            FTP_CMD_PORT = "21";
        }
        if (StringUtils.isBlank(FTP_DATA_PORT)) {
            FTP_DATA_PORT = "20";
        }
        if (StringUtils.isBlank(FTP_IP) || StringUtils.isBlank(FTP_USER) || StringUtils.isBlank(FTP_PASSWORD)) {
            return null;
        }
        File mSavedDir = new File(SysConfig.get("SCREENCAP.SAVE.PATH"));
        boolean isDirectory = mSavedDir.isDirectory();
        if (!isDirectory) {
            return null;
        }
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_IP, Integer.parseInt(FTP_CMD_PORT));
            ftpClient.login(FTP_USER, FTP_PASSWORD);
            isDirectory = ftpClient.isAuthenticated();
            if (isDirectory) {
                ftpClient.changeDirectory(FTP_PICTURE_PATH);
                File[] fileList = mSavedDir.listFiles();
                if (fileList != null) {
                    for (File newestFile : fileList) {
                        if (newestFile != null) {
                            ftpClient.upload(newestFile);
                            TrafficEntity.addData("PICTURE", new Long(newestFile.length()).intValue());
                            newestFile.delete();
                        }
                    }
                }
                ftpClient.logout();
            }
            try {
                ftpClient.disconnect(true);
            } catch (Exception e) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            try {
                ftpClient.disconnect(isDirectory);
            } catch (Exception e3) {
            }
            return null;
        } finally {
            try {
                ftpClient.disconnect(true);
            } catch (Exception e4) {
            }
        }
        return null;
    }
}
