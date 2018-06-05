package com.incomrecycle.prms.rvm.service.commonservice;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.sqlite.DBQuery;
import com.incomrecycle.common.sqlite.DBSequence;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlBuilder;
import com.incomrecycle.common.sqlite.SqlInsertBuilder;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.NumberUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.DoorStatus;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import com.incomrecycle.prms.rvm.service.task.action.RCCInstanceTask;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CommEventYC103PCommonService extends CommEventYC103CommonService {
    public HashMap execute(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        logger.debug(JSONUtils.toJSON(hsmpParam));
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        if (DoorStatus.PAPER_DOOR_OPEN.equalsIgnoreCase(type)) {
            return paperDoorOpen(svcName, subSvcName, hsmpParam);
        }
        if (DoorStatus.PAPER_DOOR_CLOSE.equalsIgnoreCase(type)) {
            return paperDoorClose(subSvcName, subSvcName, hsmpParam);
        }
        if ("PAPER_RECYCLE_FINISH_DOOR_CLOSE".equalsIgnoreCase(type)) {
            return paperRecycleFinishDoorClose(subSvcName, subSvcName, hsmpParam);
        }
        if ("PAPER_WEIGH_REPORT".equalsIgnoreCase(type)) {
            return paperWeighReport(subSvcName, subSvcName, hsmpParam);
        }
        if ("PAPER_RECYCLE_OPT".equalsIgnoreCase(type)) {
            return paperRecycleOPT(subSvcName, subSvcName, hsmpParam);
        }
        if ("CHECK_PAPER_DOOR_STATE_OPEN".equalsIgnoreCase(type)) {
            return paperDoorStateOpen(subSvcName, subSvcName, hsmpParam);
        }
        if ("PAPER_DOOR_OPEN_RECYCLE_FINISH".equalsIgnoreCase(type)) {
            return paperDoorOpenRecycleFinish(subSvcName, subSvcName, hsmpParam);
        }
        return super.execute(svcName, subSvcName, hsmpParam);
    }

    public HashMap paperDoorOpen(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "INFORM");
        hsmpGUIEvent.put("INFORM", "PAPER_RECYCLE_DOOR_OPEN");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap paperDoorStateOpen(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "INFORM");
        hsmpGUIEvent.put("INFORM", "BEFORE_RECYCLE_DOOR_OPEN");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap paperDoorOpenRecycleFinish(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        return null;
    }

    public HashMap paperDoorClose(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        CommService.getCommService().execute("RECYCLE_PAPER_START", null);
        return null;
    }

    public HashMap paperRecycleFinishDoorClose(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String paperWeigh;
        if ("PAPER_RECYCLE_FINISH_DOOR_CLOSE".equalsIgnoreCase((String) hsmpParam.get("data"))) {
            HashMap<String, String> hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
            hsmpGUIEvent.put("EVENT", "INFORM");
            hsmpGUIEvent.put("INFORM", "PAPER_RECYCLE_FINISH_DOOR_CLOSE");
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
            paperWeigh = CommService.getCommService().execute("CHECK_PAPER_WEIGH", null);
            HashMap hsmpResource = (HashMap) ServiceGlobal.getCurrentSession("RESOURCE");
            HashMap hsmpDSParam = new HashMap();
            String SHOW_WEIGHT_FORMAT = null;
            if (hsmpResource != null) {
                SHOW_WEIGHT_FORMAT = (String) hsmpResource.get("SHOW_WEIGHT_FORMAT");
            }
            if (StringUtils.isBlank(SHOW_WEIGHT_FORMAT)) {
                hsmpDSParam.put("MSG", NumberUtils.toScale(paperWeigh, 2));
            } else {
                hsmpDSParam.put("MSG", NumberUtils.toScale(paperWeigh, 2));
            }
            CommService.getCommService().execute("DIGITALSCREEN_SHOWTEXT", JSONUtils.toJSON(hsmpDSParam));
        } else {
            paperWeigh = CommService.getCommService().execute("CHECK_PAPER_WEIGH", "PUT_PAPER_FAIL_OPT");
        }
        CommService.getCommService().execute("RECYCLE_PAPER", null);
        return null;
    }

    public HashMap paperWeighReport(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        double dWeigh = Double.parseDouble((String) hsmpParam.get("data"));
        if (dWeigh > 8.0d) {
            HashMap<String, String> hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
            hsmpGUIEvent.put("EVENT", "INFORM");
            hsmpGUIEvent.put("INFORM", "REACH_MAX_PAPER_PER_OPT");
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
            return null;
        }
        int i;
        List<HashMap<String, String>> listInstanceTask = new ArrayList();
        DBSequence dbSequence = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS"));
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        Date tDate = new Date();
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "STORAGE_CURR_PAPER_WEIGH");
        CommTable commTableRvmSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        Double storageCurrCount;
        if (commTableRvmSysCode.getRecordCount() == 0) {
            storageCurrCount = Double.valueOf(dWeigh);
        } else {
            storageCurrCount = Double.valueOf(Double.valueOf(Double.parseDouble(commTableRvmSysCode.getRecord(0).get("SYS_CODE_VALUE"))).doubleValue() + dWeigh);
        }
        SqlWhereBuilder sqlWhereBuilderRvmAlarm = new SqlWhereBuilder();
        sqlWhereBuilderRvmAlarm.addNumberIn("ALARM_ID", (Object[]) new Object[]{Integer.valueOf(13), Integer.valueOf(14)});
        List<HashMap<String, String>> listRvmAlarm = JSONUtils.toList(dbQuery.getCommTable("select * from RVM_ALARM" + sqlWhereBuilderRvmAlarm.toSqlWhere("where")));
        HashMap<String, String> hsmpMaxAlarm = null;
        HashMap<String, String> hsmpTooMuchAlarm = null;
        if (listRvmAlarm != null) {
            for (i = 0; i < listRvmAlarm.size(); i++) {
                HashMap<String, String> hsmpRvmAlarm = (HashMap) listRvmAlarm.get(i);
                int alarmId = Integer.parseInt((String) hsmpRvmAlarm.get("ALARM_ID"));
                if (alarmId == 13) {
                    hsmpMaxAlarm = hsmpRvmAlarm;
                }
                if (alarmId == 14) {
                    hsmpTooMuchAlarm = hsmpRvmAlarm;
                }
            }
        }
        boolean hasMaxAlarm = false;
        boolean hasTooMuchAlarm = false;
        if ((hsmpTooMuchAlarm != null && Double.parseDouble((String) hsmpTooMuchAlarm.get("TSD_VALUE")) <= storageCurrCount.doubleValue()) || (hsmpMaxAlarm != null && Double.parseDouble((String) hsmpMaxAlarm.get("TSD_VALUE")) <= storageCurrCount.doubleValue())) {
            String ALARM_INST_ID;
            SqlInsertBuilder sqlInsertBuilder;
            HashMap<String, String> hsmpInstanceTask;
            SqlWhereBuilder sqlWhereBuilderRvmAlarmInst = new SqlWhereBuilder();
            Object[] objArr = new Object[]{Integer.valueOf(13), Integer.valueOf(14)};
            sqlWhereBuilderRvmAlarmInst.addNumberIn("ALARM_STATUS", (Object[]) new Object[]{Integer.valueOf(1), Integer.valueOf(2)}).addNumberIn("ALARM_ID", objArr);
            CommTable commTableRvmAlarmInst = dbQuery.getCommTable("select * from RVM_ALARM_INST" + sqlWhereBuilderRvmAlarmInst.toSqlWhere("where"));
            for (i = 0; i < commTableRvmAlarmInst.getRecordCount(); i++) {
                if (commTableRvmAlarmInst.getRecord(i).get("ALARM_ID").equals("13")) {
                    hasMaxAlarm = true;
                }
                if (commTableRvmAlarmInst.getRecord(i).get("ALARM_ID").equals("14")) {
                    hasTooMuchAlarm = true;
                }
            }
            if (!(hasTooMuchAlarm || hsmpTooMuchAlarm == null || Double.parseDouble((String) hsmpTooMuchAlarm.get("TSD_VALUE")) > storageCurrCount.doubleValue())) {
                ALARM_INST_ID = dbSequence.getSeq("ALARM_INST_ID");
                sqlInsertBuilder = new SqlInsertBuilder("RVM_ALARM_INST");
                sqlInsertBuilder.newInsertRecord().setNumber("ALARM_INST_ID", ALARM_INST_ID).setNumber("ALARM_TYPE", "0").setDateTime("ALARM_TIME", tDate).setNumber("ALARM_ID", Integer.valueOf(14)).setNumber("UPLOAD_FLAG", "0").setNumber("ALARM_STATUS", Integer.valueOf(1));
                listSqlBuilder.add(sqlInsertBuilder);
                hsmpInstanceTask = new HashMap();
                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
                hsmpInstanceTask.put("ALARM_INST_ID", ALARM_INST_ID);
                hsmpInstanceTask.put("ALARM_TYPE", "0");
                listInstanceTask.add(hsmpInstanceTask);
            }
            if (!(hasMaxAlarm || hsmpMaxAlarm == null || Double.parseDouble((String) hsmpMaxAlarm.get("TSD_VALUE")) > storageCurrCount.doubleValue())) {
                ALARM_INST_ID = dbSequence.getSeq("ALARM_INST_ID");
                sqlInsertBuilder = new SqlInsertBuilder("RVM_ALARM_INST");
                sqlInsertBuilder.newInsertRecord().setNumber("ALARM_INST_ID", ALARM_INST_ID).setNumber("ALARM_TYPE", "0").setDateTime("ALARM_TIME", tDate).setNumber("ALARM_ID", Integer.valueOf(13)).setNumber("UPLOAD_FLAG", "0").setNumber("ALARM_STATUS", Integer.valueOf(1));
                listSqlBuilder.add(sqlInsertBuilder);
                hsmpInstanceTask = new HashMap();
                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
                hsmpInstanceTask.put("ALARM_INST_ID", ALARM_INST_ID);
                hsmpInstanceTask.put("ALARM_TYPE", "0");
                listInstanceTask.add(hsmpInstanceTask);
            }
        }
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        for (i = 0; i < listInstanceTask.size(); i++) {
            RCCInstanceTask.addTask((HashMap) listInstanceTask.get(i));
        }
        HashMap hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "INFORM");
        if (hasMaxAlarm) {
            hsmpGUIEvent.put("INFORM", "REACH_MAX_PAPER");
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        }
        return null;
    }

    public HashMap paperRecycleOPT(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String VENDING_WAY;
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        CommService.getCommService().execute("RECYCLE_PAPER", null);
        List<HashMap<String, String>> listInstanceTask = new ArrayList();
        if ("PUT_PAPER_FAIL_OPT".equalsIgnoreCase((String) hsmpParam.get("opt"))) {
            VENDING_WAY = "DONATION";
        } else {
            VENDING_WAY = (String) ServiceGlobal.getCurrentSession(AllAdvertisement.VENDING_WAY);
        }
        String OPT_ID = (String) ServiceGlobal.getCurrentSession("OPT_ID");
        DBSequence dbSequence = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS"));
        if (OPT_ID == null) {
            OPT_ID = dbSequence.getSeq("OPT_ID");
            ServiceGlobal.setCurrentSession("OPT_ID", OPT_ID);
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderRvmOpt = new SqlWhereBuilder();
        sqlWhereBuilderRvmOpt.addNumberEqualsTo("OPT_ID", OPT_ID);
        CommTable commTableOpt = dbQuery.getCommTable("select * from RVM_OPT" + sqlWhereBuilderRvmOpt.toSqlWhere("where"));
        SqlWhereBuilder sqlWhereBuilderPaperPrice = new SqlWhereBuilder();
        CommTable commTablePaper = dbQuery.getCommTable("select * from RVM_PAPER_PRICE ");
        String PAPER_WEIGH = null;
        String PAPER_PRICE = null;
        if (commTablePaper.getRecordCount() > 0) {
            PAPER_WEIGH = commTablePaper.getRecord(0).get("PAPER_WEIGH");
            PAPER_PRICE = commTablePaper.getRecord(0).get("PAPER_PRICE");
        }
        String PAPER_PRICE_AMOUNT = null;
        if (!(PAPER_WEIGH == null || PAPER_PRICE == null)) {
            PAPER_PRICE_AMOUNT = new DecimalFormat("########0").format(Double.valueOf((Double.parseDouble(data) / Double.parseDouble(PAPER_WEIGH)) * Double.parseDouble(PAPER_PRICE)));
        }
        if (PAPER_PRICE_AMOUNT == null || "".equals(PAPER_PRICE_AMOUNT)) {
            PAPER_PRICE_AMOUNT = "0";
        }
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        Date tDate = new Date();
        if (commTableOpt.getRecordCount() == 0) {
            SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_OPT");
            sqlInsertBuilder.newInsertRecord().setNumber("OPT_ID", OPT_ID).setDateTime("OPT_TIME", tDate).setString("OPT_TYPE", "RECYCLE").setString("RVM_CODE", SysConfig.get("RVM.CODE")).setString("PRODUCT_TYPE", "PAPER").setNumber("PRODUCT_AMOUNT", data).setString("CARD_TYPE", ServiceGlobal.getCurrentSession("CARD_TYPE")).setString("CARD_NO", ServiceGlobal.getCurrentSession("CARD_NO")).setNumber("PROFIT_AMOUNT", PAPER_PRICE_AMOUNT).setString(AllAdvertisement.VENDING_WAY, VENDING_WAY).setNumber("OPT_STATUS", Integer.valueOf(0)).setNumber("CHARGE_FLAG", "0").setString("UNIQUE_CODE", SysConfig.get("RVM.CODE") + "_" + tDate.getTime() + "_" + OPT_ID);
            listSqlBuilder.add(sqlInsertBuilder);
        }
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "STORAGE_CURR_PAPER_WEIGH");
        CommTable commTableRvmSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        Double storageCurrCount;
        if (commTableRvmSysCode.getRecordCount() == 0) {
            SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_SYS_CODE");
            sqlInsertBuilder.newInsertRecord().setString("SYS_CODE_TYPE", "RVM_INFO").setString("SYS_CODE_KEY", "STORAGE_CURR_PAPER_WEIGH").setString("SYS_CODE_VALUE", data);
            listSqlBuilder.add(sqlInsertBuilder);
            storageCurrCount = Double.valueOf(Double.parseDouble(data));
        } else {
            storageCurrCount = Double.valueOf(Double.valueOf(Double.parseDouble(commTableRvmSysCode.getRecord(0).get("SYS_CODE_VALUE"))).doubleValue() + Double.parseDouble(data));
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_SYS_CODE");
            sqlUpdateBuilder.setString("SYS_CODE_VALUE", Double.toString(storageCurrCount.doubleValue())).setSqlWhere(sqlWhereBuilderRvmSysCode);
            listSqlBuilder.add(sqlUpdateBuilder);
        }
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        HashMap hsmpPaperWeigh = new HashMap();
        hsmpPaperWeigh.put("PAPER_WEIGH", data);
        hsmpPaperWeigh.put("PAPER_PRICE", PAPER_PRICE_AMOUNT);
        ServiceGlobal.setCurrentSession("RECYCLED_PAPER_SUMMARY", hsmpPaperWeigh);
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "INFORM");
        hsmpGUIEvent.put("INFORM", "PAPER_WEIGH_RESULT");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }
}
