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
import com.incomrecycle.common.sqlite.SqlInsertBuilder;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.NumberUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.MsgType;
import com.incomrecycle.prms.rvm.common.SysDef.ProductType;
import com.incomrecycle.prms.rvm.common.SysDef.maintainOptContent;
import com.incomrecycle.prms.rvm.common.SysDef.networkSts;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RCCUploadSummaryCommonService implements AppCommonService {
    private static DecimalFormat df = new DecimalFormat("0.00");
    private static final Logger logger = LoggerFactory.getLogger("RCCUploadSummaryCommonService");

    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        RCCUploadRequest(svcName, subSvnName, hsmpParam);
        MaintainOptCon(svcName, subSvnName, hsmpParam);
        RCCInstanceTaskSts(svcName, subSvnName, hsmpParam);
        RCCInstanceTaskOPT(svcName, subSvnName, hsmpParam);
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
        try {
            if (Integer.parseInt(putdownCount) > Integer.parseInt(putdownCountDelta)) {
                putdownCountReal = Integer.toString(Integer.parseInt(putdownCount) - Integer.parseInt(putdownCountDelta));
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        try {
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
            HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
            if (hsmpretPkg == null) {
                return null;
            }
            String termNo = (String) hsmpretPkg.get("TERM_NO");
            if (((String) hsmpretPkg.get("MES_TYPE")).equalsIgnoreCase("RESPONSE") && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                try {
                    if (Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM"))).intValue() == 1) {
                        hasResult.put("RESULT", NetworkUtils.NET_STATE_SUCCESS);
                        return hasResult;
                    }
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
            return null;
        } catch (Exception e42) {
            e42.printStackTrace();
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
                        if ("RESPONSE".equalsIgnoreCase((String) hsmap.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo_sts)) {
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
        HashMap hsmpSend = new HashMap();
        String putdownCount = "0";
        String putdownCountDelta = "0";
        String putdownCountReal = "0";
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("OPT_TYPE", "RECYCLE").addNumberEqualsTo("OPT_STATUS", Integer.valueOf(1));
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
            try {
                if (Integer.parseInt(putdownCount) > Integer.parseInt(putdownCountDelta)) {
                    putdownCountReal = Integer.toString(Integer.parseInt(putdownCount) - Integer.parseInt(putdownCountDelta));
                }
            } catch (Exception e2) {
            }
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                String CardNO = commTable.getRecord(i).get("CARD_NO");
                String RecycleQuantity = commTable.getRecord(i).get("PRODUCT_AMOUNT");
                String VendingWay = commTable.getRecord(i).get(AllAdvertisement.VENDING_WAY);
                String productType = commTable.getRecord(i).get("PRODUCT_TYPE");
                String VendingMoney = commTable.getRecord(i).get("PROFIT_AMOUNT");
                Date date = null;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(commTable.getRecord(i).get("OPT_TIME"));
                } catch (ParseException e3) {
                    e3.printStackTrace();
                }
                Long VendTime = Long.valueOf(date.getTime());
                String OptId = commTable.getRecord(i).get("OPT_ID");
                String SELECT_FLAG = commTable.getRecord(i).get(AllAdvertisement.SELECT_FLAG);
                String OPBatchId = SysConfig.get("RVM.CODE") + "_" + VendTime + OptId;
                String CardType = commTable.getRecord(i).get("CARD_TYPE");
                if (CardType == null) {
                    CardType = SysDef.CardType.NOCARD;
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
                hsmpSend.put("CARD_TYPE", SysDef.CardType.getMsgCardType(CardType));
                SqlWhereBuilder sqlWhereBuilderOPTBottle = new SqlWhereBuilder();
                sqlWhereBuilderOPTBottle.addNumberEqualsTo("OPT_ID", OptId);
                CommTable commTableOPTBottle = dbQuery.getCommTable("select * from RVM_OPT_BOTTLE " + sqlWhereBuilderOPTBottle.toSqlWhere("where"));
                List BarValuesList = new ArrayList();
                if (commTableOPTBottle.getRecordCount() != 0) {
                    for (int j = 0; j < commTableOPTBottle.getRecordCount(); j++) {
                        String BottleBarCode = commTableOPTBottle.getRecord(j).get("BOTTLE_BAR_CODE");
                        try {
                            Double BottleAmount = Double.valueOf(commTableOPTBottle.getRecord(j).get("BOTTLE_AMOUNT"));
                            int BottleCount = Integer.parseInt(commTableOPTBottle.getRecord(j).get("BOTTLE_COUNT"));
                            int VendingBottleCount = Integer.parseInt(commTableOPTBottle.getRecord(j).get("VENDING_BOTTLE_COUNT"));
                            for (int k = 0; k < BottleCount; k++) {
                                if (k < VendingBottleCount) {
                                    BarValuesList.add(BottleBarCode + "=" + BottleAmount.toString());
                                } else {
                                    BarValuesList.add(BottleBarCode + "=0");
                                }
                            }
                        } catch (Exception e4) {
                            e4.printStackTrace();
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
                            sqlWhereBuilderOptId.addNumberEqualsTo("OPT_ID", OptId);
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
                            sqlWhereBuilderOptId.addNumberEqualsTo("OPT_ID", OptId);
                            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
                            sqlUpdateBuilder.setNumber("OPT_STATUS", Integer.valueOf(-1)).setSqlWhere(sqlWhereBuilderOptId);
                            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                        }
                    } catch (Exception e42) {
                        e42.printStackTrace();
                    }
                }
                if (MsgType.RCC_CARD_STATUS.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                    sqlWhereBuilderOptId = new SqlWhereBuilder();
                    sqlWhereBuilderOptId.addNumberEqualsTo("OPT_ID", OptId);
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

    private HashMap RCCUploadRequest(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        SqlWhereBuilder sqlWhereBuilder2;
        int i;
        double bottleAmount = 0.0d;
        double SumMoney = 0.0d;
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        long lTime = new Date().getTime() - 86400000;
        Date dateTime = new Date(lTime);
        String currentDate = simpleDateFormat.format(dateTime);
        String OptSummaryId = simpleDateFormat.format(Long.valueOf(lTime));
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addDateEqualsTo("OPT_TIME", dateTime).addStringEqualsTo("PRODUCT_TYPE", "BOTTLE").addStringEqualsTo("OPT_TYPE", "RECYCLE");
        SqlWhereBuilder sqlWhereBuilder1 = new SqlWhereBuilder();
        sqlWhereBuilder1.addDateEqualsTo("OPT_TIME", dateTime).addStringEqualsTo("PRODUCT_TYPE", "PAPER").addStringEqualsTo("OPT_TYPE", "RECYCLE");
        SqlWhereBuilder sqlWhereBuilderCoupon = new SqlWhereBuilder();
        sqlWhereBuilderCoupon.addDateEqualsTo("USED_TIME", dateTime);
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        CommTable commTable = dbQuery.getCommTable("select sum(PRODUCT_AMOUNT) AS num, sum(PROFIT_AMOUNT) AS sum from RVM_OPT " + sqlWhereBuilder.toSqlWhere("where"));
        CommTable commTable1 = dbQuery.getCommTable("select sum(PRODUCT_AMOUNT) AS num, sum(PROFIT_AMOUNT) AS sum from RVM_OPT " + sqlWhereBuilder1.toSqlWhere("where"));
        CommTable commTableCoupon = dbQuery.getCommTable("select count(ACTIVITY_ID) AS num from RVM_ACTIVITY_VOUCHER " + sqlWhereBuilderCoupon.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            try {
                sqlWhereBuilder2 = new SqlWhereBuilder();
                sqlWhereBuilder2.addNumberEqualsTo("OPT_SUMMARY_ID", Integer.valueOf(Integer.parseInt(OptSummaryId))).addStringEqualsTo("PRODUCT_TYPE", "BOTTLE");
                if (dbQuery.getCommTable("select * from RVM_OPT_SUMMARY " + sqlWhereBuilder2.toSqlWhere("where")).getRecordCount() == 0) {
                    i = 0;
                    while (i < commTable.getRecordCount()) {
                        if (commTable.getRecord(i).get("num") == null || commTable.getRecord(i).get("sum") == null) {
                            bottleAmount = 0.0d;
                            SumMoney = 0.0d;
                        } else {
                            bottleAmount = Double.parseDouble(commTable.getRecord(i).get("num"));
                            SumMoney = Double.parseDouble(commTable.getRecord(i).get("sum"));
                        }
                        i++;
                    }
                    SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_OPT_SUMMARY");
                    sqlInsertBuilder.newInsertRecord().setNumber("OPT_SUMMARY_ID", Integer.valueOf(Integer.parseInt(OptSummaryId))).setString("VENDING_TIME", currentDate).setString("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + lTime + "_1").setString("PRODUCT_TYPE", "BOTTLE").setNumber("RECYCLE_QUANTITY", Double.valueOf(bottleAmount)).setNumber("SUM_MONEY", Double.valueOf(SumMoney)).setString("SEND_TIME", null).setNumber("UPLOAD_FLAG", Integer.valueOf(0));
                    SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (("YC103P_OLD".equalsIgnoreCase(SysConfig.get("RVM.MODE")) || "YC103P_NEW".equalsIgnoreCase(SysConfig.get("RVM.MODE"))) && commTable1.getRecordCount() != 0) {
            try {
                sqlWhereBuilder2 = new SqlWhereBuilder();
                sqlWhereBuilder2.addNumberEqualsTo("OPT_SUMMARY_ID", Integer.valueOf(Integer.parseInt(OptSummaryId))).addStringEqualsTo("PRODUCT_TYPE", "PAPER");
                if (dbQuery.getCommTable("select * from RVM_OPT_SUMMARY " + sqlWhereBuilder2.toSqlWhere("where")).getRecordCount() == 0) {
                    i = 0;
                    while (i < commTable1.getRecordCount()) {
                        if (commTable1.getRecord(i).get("num") == null || commTable1.getRecord(i).get("sum") == null) {
                            bottleAmount = 0.0d;
                            SumMoney = 0.0d;
                        } else {
                            bottleAmount = Double.parseDouble(new DecimalFormat("#####0.00").format(Double.parseDouble(commTable1.getRecord(i).get("num"))));
                            SumMoney = Double.parseDouble(commTable1.getRecord(i).get("sum"));
                        }
                        i++;
                    }
                    if (!(bottleAmount == 0.0d || SumMoney == 0.0d)) {
                        SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_OPT_SUMMARY");
                        sqlInsertBuilder.newInsertRecord().setNumber("OPT_SUMMARY_ID", Integer.valueOf(Integer.parseInt(OptSummaryId))).setString("VENDING_TIME", currentDate).setString("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + lTime + "_2").setString("PRODUCT_TYPE", "PAPER").setNumber("RECYCLE_QUANTITY", Double.valueOf(bottleAmount)).setNumber("SUM_MONEY", Double.valueOf(SumMoney)).setString("SEND_TIME", null).setNumber("UPLOAD_FLAG", Integer.valueOf(0));
                        SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        if (commTableCoupon.getRecordCount() != 0) {
            try {
                sqlWhereBuilder2 = new SqlWhereBuilder();
                sqlWhereBuilder2.addNumberEqualsTo("OPT_SUMMARY_ID", Integer.valueOf(Integer.parseInt(OptSummaryId))).addStringEqualsTo("PRODUCT_TYPE", "COUPON");
                if (dbQuery.getCommTable("select * from RVM_OPT_SUMMARY " + sqlWhereBuilder2.toSqlWhere("where")).getRecordCount() == 0) {
                    for (i = 0; i < commTableCoupon.getRecordCount(); i++) {
                        bottleAmount = Double.parseDouble(commTableCoupon.getRecord(i).get("num"));
                    }
                    if (bottleAmount > 0.0d) {
                        SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_OPT_SUMMARY");
                        sqlInsertBuilder.newInsertRecord().setNumber("OPT_SUMMARY_ID", Integer.valueOf(Integer.parseInt(OptSummaryId))).setString("VENDING_TIME", currentDate).setString("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + lTime + "_3").setString("PRODUCT_TYPE", "COUPON").setNumber("RECYCLE_QUANTITY", Double.valueOf(bottleAmount)).setNumber("SUM_MONEY", Integer.valueOf(0)).setString("SEND_TIME", null).setNumber("UPLOAD_FLAG", Integer.valueOf(0));
                        SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
                    }
                }
            } catch (Exception e22) {
                e22.printStackTrace();
            }
        }
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
}
