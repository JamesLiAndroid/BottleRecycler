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
import com.incomrecycle.common.task.DelayTask;
import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import com.incomrecycle.prms.rvm.service.task.action.RCCInstanceTask;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CommEventYC103SCommonService extends CommEventYC103CommonService {
    public HashMap execute(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        logger.debug(JSONUtils.toJSON(hsmpParam));
        String data = (String) hsmpParam.get("data");
        if ("CLEAR_WEIGH".equalsIgnoreCase((String) hsmpParam.get("type"))) {
            return clearWeigh(svcName, subSvcName, hsmpParam);
        }
        return super.execute(svcName, subSvcName, hsmpParam);
    }

    public HashMap plcRecycleCheck(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        String VENDING_WAY = (String) ServiceGlobal.getCurrentSession(AllAdvertisement.VENDING_WAY);
        String barCode = StringUtils.trimToNull(CommService.getCommService().execute("BARCODE_READ", null));
        String previousBarCode = (String) ServiceGlobal.getCurrentSession("CURRENT_BAR_CODE");
        boolean isForceRecycle = false;
        if ("FORCE_RECYCLE".equalsIgnoreCase((String) ServiceGlobal.getCurrentSession("BAR_CODE_FLAG"))) {
            isForceRecycle = true;
            ServiceGlobal.setCurrentSession("BAR_CODE_FLAG", null);
        }
        if (!StringUtils.equals(barCode, previousBarCode)) {
            isForceRecycle = false;
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        boolean isValidBarCode = false;
        if (barCode != null) {
            DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
            SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addStringEqualsTo("BAR_CODE", barCode);
            CommTable commTable = dbQuery.getCommTable("select * from rvm_bar_code " + sqlWhereBuilder.toSqlWhere("where"));
            if (commTable.getRecordCount() != 0) {
                if (Integer.toString(1).equals(commTable.getRecord(0).get("BAR_CODE_FLAG"))) {
                    isValidBarCode = true;
                }
            } else if (dbQuery.getCommTable("select * from RVM_BAR_CODE_UNKNOWN " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
                SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_BAR_CODE_UNKNOWN");
                sqlInsertBuilder.newInsertRecord().setString("BAR_CODE", barCode).setNumber("UPLOAD_FLAG", "0").setDateTime("CREATE_TIME", new Date());
                try {
                    SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
                } catch (Exception e) {
                }
            } else {
                SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_BAR_CODE_UNKNOWN");
                sqlUpdateBuilder.setNumber("UPLOAD_FLAG", Integer.valueOf(0)).setSqlWhere(sqlWhereBuilder);
                SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
            }
        }
        HashMap<String, String> hsmpGUIEvent;
        if (!"DONATION".equalsIgnoreCase(VENDING_WAY)) {
            String weighStr = null;
            if ("true".equalsIgnoreCase(SysConfig.get("COM.WEIGH.ENABLE"))) {
                weighStr = CommService.getCommService().execute("WEIGH_READ", null);
            }
            double weigh = 0.0d;
            if (weighStr != null) {
                weigh = Double.parseDouble(weighStr);
            }
            if (!isValidBarCode || weigh > Double.parseDouble(SysConfig.get("WEIGH.LIMIT"))) {
                if (isForceRecycle) {
                    CommService.getCommService().execute("BOTTLE_ACCEPT_READY", null);
                } else {
                    CommService.getCommService().execute("BOTTLE_REJECT_READY", null);
                }
                hsmpGUIEvent = new HashMap();
                hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                hsmpGUIEvent.put("EVENT", "INFORM");
                if (barCode == null) {
                    hsmpGUIEvent.put("INFORM", "BAR_CODE_NOT_FOUND");
                } else if (weigh > Double.parseDouble(SysConfig.get("WEIGH.LIMIT"))) {
                    hsmpGUIEvent.put("INFORM", "OVER_WEIGH");
                } else {
                    hsmpGUIEvent.put("INFORM", "BAR_CODE_REJECT");
                }
                ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
            } else {
                hsmpGUIEvent = new HashMap();
                hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                hsmpGUIEvent.put("EVENT", "INFORM");
                hsmpGUIEvent.put("INFORM", "BOTTLE_ACCEPT_READY");
                ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
                CommService.getCommService().execute("BOTTLE_ACCEPT_READY", null);
            }
        } else if (isForceRecycle || isValidBarCode || "false".equalsIgnoreCase(SysConfig.get("DONATION.WITHDRAW.ENABLE"))) {
            hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
            hsmpGUIEvent.put("EVENT", "INFORM");
            hsmpGUIEvent.put("INFORM", "BOTTLE_SCAN_END");
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
            isForceRecycle = true;
            CommService.getCommService().execute("BOTTLE_ACCEPT_READY", null);
        } else {
            CommService.getCommService().execute("BOTTLE_REJECT_READY", null);
            hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
            hsmpGUIEvent.put("EVENT", "INFORM");
            if (barCode == null) {
                hsmpGUIEvent.put("INFORM", "BAR_CODE_NOT_FOUND");
            } else {
                hsmpGUIEvent.put("INFORM", "BAR_CODE_REJECT");
            }
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        }
        if (barCode == null && isForceRecycle) {
            barCode = SysConfig.get("DEFAULT_BAR_CODE");
        }
        ServiceGlobal.setCurrentSession("CURRENT_BAR_CODE", barCode);
        return null;
    }

    public HashMap plcThirdLightOn(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SqlInsertBuilder sqlInsertBuilder;
        SqlUpdateBuilder sqlUpdateBuilder;
        int i;
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        List<HashMap<String, String>> listInstanceTask = new ArrayList();
        String barCode = (String) ServiceGlobal.getCurrentSession("CURRENT_BAR_CODE");
        String VENDING_WAY = (String) ServiceGlobal.getCurrentSession(AllAdvertisement.VENDING_WAY);
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
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("BAR_CODE", barCode);
        CommTable commTableBarCode = dbQuery.getCommTable("select * from rvm_bar_code " + sqlWhereBuilder.toSqlWhere("where"));
        String BAR_CODE_VOL = "0";
        String BAR_CODE_AMOUNT = "0";
        String BAR_CODE_STUFF = "0";
        String TOTAL_BAR_CODE_AMOUNT = (String) ServiceGlobal.getCurrentSession("TOTAL_BAR_CODE_AMOUNT");
        if (StringUtils.isBlank(TOTAL_BAR_CODE_AMOUNT)) {
            TOTAL_BAR_CODE_AMOUNT = "0";
        }
        String TOTAL_BAR_CODE_COUNT = (String) ServiceGlobal.getCurrentSession("TOTAL_BAR_CODE_COUNT");
        if (StringUtils.isBlank(TOTAL_BAR_CODE_COUNT)) {
            TOTAL_BAR_CODE_COUNT = "0";
        }
        String TOTAL_BAR_CODE_VOL = (String) ServiceGlobal.getCurrentSession("TOTAL_BAR_CODE_VOL");
        if (StringUtils.isBlank(TOTAL_BAR_CODE_VOL)) {
            TOTAL_BAR_CODE_VOL = "0";
        }
        if (commTableBarCode.getRecordCount() > 0) {
            BAR_CODE_VOL = commTableBarCode.getRecord(0).get("BAR_CODE_VOL");
            String barCodeAttrId = commTableBarCode.getRecord(0).get("BAR_CODE_ATTR_ID");
            SqlWhereBuilder sqlWhereBuilderPrice = new SqlWhereBuilder();
            sqlWhereBuilderPrice.addNumberEqualsTo("BAR_CODE_ATTR_ID", barCodeAttrId);
            CommTable commTablePrice = dbQuery.getCommTable("select * from RVM_BAR_CODE_ATTR_ID" + sqlWhereBuilderPrice.toSqlWhere("where"));
            if (commTablePrice.getRecordCount() != 0) {
                BAR_CODE_AMOUNT = commTablePrice.getRecord(0).get("BAR_CODE_PRICE");
                BAR_CODE_STUFF = commTablePrice.getRecord(0).get("BAR_CODE_STUFF");
            } else {
                BAR_CODE_AMOUNT = "0";
                BAR_CODE_STUFF = "0";
            }
        }
        TOTAL_BAR_CODE_COUNT = Integer.toString(Integer.parseInt(TOTAL_BAR_CODE_COUNT) + 1);
        TOTAL_BAR_CODE_VOL = Double.toString(Double.parseDouble(TOTAL_BAR_CODE_VOL) + Double.parseDouble(BAR_CODE_VOL));
        TOTAL_BAR_CODE_AMOUNT = Double.toString(Double.parseDouble(TOTAL_BAR_CODE_AMOUNT) + Double.parseDouble(BAR_CODE_AMOUNT));
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        Date tDate = new Date();
        if (commTableOpt.getRecordCount() == 0) {
            sqlInsertBuilder = new SqlInsertBuilder("RVM_OPT");
            sqlInsertBuilder.newInsertRecord().setNumber("OPT_ID", OPT_ID).setDateTime("OPT_TIME", tDate).setString("OPT_TYPE", "RECYCLE").setString("RVM_CODE", SysConfig.get("RVM.CODE")).setString("PRODUCT_TYPE", ServiceGlobal.getCurrentSession("PRODUCT_TYPE")).setNumber("PRODUCT_AMOUNT", "1").setString("CARD_TYPE", ServiceGlobal.getCurrentSession("CARD_TYPE")).setString("CARD_NO", ServiceGlobal.getCurrentSession("CARD_NO")).setNumber("PROFIT_AMOUNT", BAR_CODE_AMOUNT).setString(AllAdvertisement.VENDING_WAY, VENDING_WAY).setNumber("OPT_STATUS", Integer.valueOf(0)).setNumber("CHARGE_FLAG", "0").setString("UNIQUE_CODE", SysConfig.get("RVM.CODE") + "_" + tDate.getTime() + "_" + OPT_ID);
            sqlInsertBuilder = new SqlInsertBuilder("RVM_OPT_BOTTLE");
            sqlInsertBuilder.newInsertRecord().setNumber("OPT_ID", OPT_ID).setString("BOTTLE_BAR_CODE", barCode).setNumber("BOTTLE_VOL", BAR_CODE_VOL).setNumber("BOTTLE_AMOUNT", BAR_CODE_AMOUNT).setNumber("BOTTLE_COUNT", "1").setNumber("VENDING_BOTTLE_COUNT", "1");
            listSqlBuilder.add(sqlInsertBuilder);
            listSqlBuilder.add(sqlInsertBuilder);
        } else {
            SqlWhereBuilder sqlWhereBuilderRvmOptBottle = new SqlWhereBuilder();
            sqlWhereBuilderRvmOptBottle.addNumberEqualsTo("OPT_ID", OPT_ID).addStringEqualsTo("BOTTLE_BAR_CODE", barCode);
            CommTable commTableRvmOptBottle = dbQuery.getCommTable("select * from RVM_OPT_BOTTLE " + sqlWhereBuilderRvmOptBottle.toSqlWhere("where"));
            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
            sqlUpdateBuilder.addNumber("PRODUCT_AMOUNT", "1").addNumber("PROFIT_AMOUNT", BAR_CODE_AMOUNT).setSqlWhere(sqlWhereBuilderRvmOpt);
            listSqlBuilder.add(sqlUpdateBuilder);
            if (commTableRvmOptBottle.getRecordCount() > 0) {
                sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT_BOTTLE");
                sqlUpdateBuilder.addNumber("BOTTLE_COUNT", "1").addNumber("VENDING_BOTTLE_COUNT", "1").setSqlWhere(sqlWhereBuilderRvmOptBottle);
                listSqlBuilder.add(sqlUpdateBuilder);
            } else {
                sqlInsertBuilder = new SqlInsertBuilder("RVM_OPT_BOTTLE");
                sqlInsertBuilder.newInsertRecord().setNumber("OPT_ID", OPT_ID).setString("BOTTLE_BAR_CODE", barCode).setNumber("BOTTLE_VOL", BAR_CODE_VOL).setNumber("BOTTLE_AMOUNT", BAR_CODE_AMOUNT).setNumber("BOTTLE_COUNT", "1").setNumber("VENDING_BOTTLE_COUNT", "1");
                listSqlBuilder.add(sqlInsertBuilder);
            }
        }
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "STORAGE_CURR_COUNT");
        CommTable commTableRvmSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        int storageCurrCount;
        if (commTableRvmSysCode.getRecordCount() == 0) {
            sqlInsertBuilder = new SqlInsertBuilder("RVM_SYS_CODE");
            sqlInsertBuilder.newInsertRecord().setString("SYS_CODE_TYPE", "RVM_INFO").setString("SYS_CODE_KEY", "STORAGE_CURR_COUNT").setString("SYS_CODE_VALUE", "1");
            listSqlBuilder.add(sqlInsertBuilder);
            storageCurrCount = 1;
        } else {
            storageCurrCount = Integer.parseInt(commTableRvmSysCode.getRecord(0).get("SYS_CODE_VALUE")) + 1;
            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_SYS_CODE");
            sqlUpdateBuilder.setString("SYS_CODE_VALUE", Integer.toString(storageCurrCount)).setSqlWhere(sqlWhereBuilderRvmSysCode);
            listSqlBuilder.add(sqlUpdateBuilder);
        }
        SqlWhereBuilder sqlWhereBuilderRvmAlarm = new SqlWhereBuilder();
        sqlWhereBuilderRvmAlarm.addNumberIn("ALARM_ID", (Object[]) new Object[]{Integer.valueOf(11), Integer.valueOf(12)});
        List<HashMap<String, String>> listRvmAlarm = JSONUtils.toList(dbQuery.getCommTable("select * from RVM_ALARM" + sqlWhereBuilderRvmAlarm.toSqlWhere("where")));
        HashMap<String, String> hsmpMaxAlarm = null;
        HashMap<String, String> hsmpTooMuchAlarm = null;
        if (listRvmAlarm != null) {
            for (i = 0; i < listRvmAlarm.size(); i++) {
                HashMap<String, String> hsmpRvmAlarm = (HashMap) listRvmAlarm.get(i);
                int alarmId = Integer.parseInt((String) hsmpRvmAlarm.get("ALARM_ID"));
                if (alarmId == 11) {
                    hsmpMaxAlarm = hsmpRvmAlarm;
                }
                if (alarmId == 12) {
                    hsmpTooMuchAlarm = hsmpRvmAlarm;
                }
            }
        }
        boolean hasMaxAlarm = false;
        boolean hasTooMuchAlarm = false;
        if ((hsmpTooMuchAlarm != null && Double.parseDouble((String) hsmpTooMuchAlarm.get("TSD_VALUE")) == ((double) storageCurrCount)) || (hsmpMaxAlarm != null && Double.parseDouble((String) hsmpMaxAlarm.get("TSD_VALUE")) == ((double) storageCurrCount))) {
            String ALARM_INST_ID;
            HashMap<String, String> hsmpInstanceTask;
            SqlWhereBuilder sqlWhereBuilderRvmAlarmInst = new SqlWhereBuilder();
            Object[] objArr = new Object[]{Integer.valueOf(11), Integer.valueOf(12)};
            sqlWhereBuilderRvmAlarmInst.addNumberIn("ALARM_STATUS", (Object[]) new Object[]{Integer.valueOf(1), Integer.valueOf(2)}).addNumberIn("ALARM_ID", objArr);
            CommTable commTableRvmAlarmInst = dbQuery.getCommTable("select * from RVM_ALARM_INST" + sqlWhereBuilderRvmAlarmInst.toSqlWhere("where"));
            for (i = 0; i < commTableRvmAlarmInst.getRecordCount(); i++) {
                if (commTableRvmAlarmInst.getRecord(i).get("ALARM_ID").equals("11")) {
                    hasMaxAlarm = true;
                }
                if (commTableRvmAlarmInst.getRecord(i).get("ALARM_ID").equals("12")) {
                    hasTooMuchAlarm = true;
                }
            }
            if (!(hasTooMuchAlarm || hsmpTooMuchAlarm == null || Double.parseDouble((String) hsmpTooMuchAlarm.get("TSD_VALUE")) != ((double) storageCurrCount))) {
                ALARM_INST_ID = dbSequence.getSeq("ALARM_INST_ID");
                sqlInsertBuilder = new SqlInsertBuilder("RVM_ALARM_INST");
                sqlInsertBuilder.newInsertRecord().setNumber("ALARM_INST_ID", ALARM_INST_ID).setNumber("ALARM_TYPE", "0").setDateTime("ALARM_TIME", tDate).setNumber("ALARM_ID", Integer.valueOf(12)).setNumber("UPLOAD_FLAG", "0").setNumber("ALARM_STATUS", Integer.valueOf(1));
                listSqlBuilder.add(sqlInsertBuilder);
                hsmpInstanceTask = new HashMap();
                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
                hsmpInstanceTask.put("ALARM_INST_ID", ALARM_INST_ID);
                hsmpInstanceTask.put("ALARM_TYPE", "0");
                listInstanceTask.add(hsmpInstanceTask);
            }
            if (!(hasMaxAlarm || hsmpMaxAlarm == null || Double.parseDouble((String) hsmpMaxAlarm.get("TSD_VALUE")) != ((double) storageCurrCount))) {
                ALARM_INST_ID = dbSequence.getSeq("ALARM_INST_ID");
                sqlInsertBuilder = new SqlInsertBuilder("RVM_ALARM_INST");
                sqlInsertBuilder.newInsertRecord().setNumber("ALARM_INST_ID", ALARM_INST_ID).setNumber("ALARM_TYPE", "0").setDateTime("ALARM_TIME", tDate).setNumber("ALARM_ID", Integer.valueOf(11)).setNumber("UPLOAD_FLAG", "0").setNumber("ALARM_STATUS", Integer.valueOf(1));
                listSqlBuilder.add(sqlInsertBuilder);
                hsmpInstanceTask = new HashMap();
                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
                hsmpInstanceTask.put("ALARM_INST_ID", ALARM_INST_ID);
                hsmpInstanceTask.put("ALARM_TYPE", "0");
                listInstanceTask.add(hsmpInstanceTask);
                hasMaxAlarm = true;
            }
        }
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        ServiceGlobal.setCurrentSession("TOTAL_BAR_CODE_AMOUNT", TOTAL_BAR_CODE_AMOUNT);
        ServiceGlobal.setCurrentSession("TOTAL_BAR_CODE_COUNT", TOTAL_BAR_CODE_COUNT);
        ServiceGlobal.setCurrentSession("TOTAL_BAR_CODE_VOL", TOTAL_BAR_CODE_VOL);
        for (i = 0; i < listInstanceTask.size(); i++) {
            RCCInstanceTask.addTask((HashMap) listInstanceTask.get(i));
        }
        List<String> listRecycledBottleDetail = (List) ServiceGlobal.getCurrentSession("RECYCLED_BOTTLE_DETAIL");
        if (listRecycledBottleDetail == null) {
            listRecycledBottleDetail = new ArrayList();
            ServiceGlobal.setCurrentSession("RECYCLED_BOTTLE_DETAIL", listRecycledBottleDetail);
        }
        listRecycledBottleDetail.add(barCode);
        List<HashMap<String, String>> listHsmpRecycleBottle = (List) ServiceGlobal.getCurrentSession("RECYCLED_BOTTLE_SUMMARY");
        if (listHsmpRecycleBottle == null) {
            listHsmpRecycleBottle = new ArrayList();
            ServiceGlobal.setCurrentSession("RECYCLED_BOTTLE_SUMMARY", listHsmpRecycleBottle);
        }
        int totalCount = 0;
        HashMap<String, String> hsmpRecycleBottle = null;
        for (i = 0; i < listHsmpRecycleBottle.size(); i++) {
            HashMap<String, String> hsmpItem = (HashMap) listHsmpRecycleBottle.get(i);
            totalCount += Integer.parseInt((String) hsmpItem.get("BOTTLE_COUNT"));
            if (((String) hsmpItem.get("BOTTLE_BAR_CODE")).equals(barCode)) {
                hsmpRecycleBottle = hsmpItem;
            } else {
                hsmpRecycleBottle = null;
            }
        }
        totalCount++;
        if (hsmpRecycleBottle != null) {
            hsmpRecycleBottle.put("BOTTLE_COUNT", Integer.toString(Integer.parseInt((String) hsmpRecycleBottle.get("BOTTLE_COUNT")) + 1));
            hsmpRecycleBottle.put("VENDING_BOTTLE_COUNT", Integer.toString(Integer.parseInt((String) hsmpRecycleBottle.get("VENDING_BOTTLE_COUNT")) + 1));
        } else {
            hsmpRecycleBottle = new HashMap();
            hsmpRecycleBottle.put("BOTTLE_BAR_CODE", barCode);
            hsmpRecycleBottle.put("BOTTLE_COUNT", "1");
            hsmpRecycleBottle.put("VENDING_BOTTLE_COUNT", "1");
            hsmpRecycleBottle.put("BOTTLE_AMOUNT", BAR_CODE_AMOUNT);
            hsmpRecycleBottle.put("BOTTLE_VOL", BAR_CODE_VOL);
            hsmpRecycleBottle.put("BOTTLE_STUFF", BAR_CODE_STUFF);
            listHsmpRecycleBottle.add(hsmpRecycleBottle);
        }
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "INFORM");
        if (hasMaxAlarm) {
            CommService.getCommService().execute("RECYCLE_PAUSE", null);
            hsmpGUIEvent.put("INFORM", "REACH_MAX_BOTTLES");
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        } else if (totalCount >= Integer.parseInt(SysConfig.get("MAX.BOTTLES.PER.OPT"))) {
            CommService.getCommService().execute("RECYCLE_PAUSE", null);
            hsmpGUIEvent.put("INFORM", "REACH_MAX_BOTTLES_PER_OPT");
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        } else if ("TRUE".equalsIgnoreCase(SysConfig.get("COM.PLC.HAS.ROLLER"))) {
            DelayTask.getDelayTask().addDelayTask(new TaskAction() {
                public void execute() {
                    try {
                        HashMap<String, String> hsmpGUIEvent = new HashMap();
                        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                        hsmpGUIEvent.put("EVENT", "INFORM");
                        hsmpGUIEvent.put("INFORM", "BOTTLE_ACCEPT_FINISH");
                        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 3000);
        } else {
            hsmpGUIEvent.put("INFORM", "BOTTLE_ACCEPT_FINISH");
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        }
        return null;
    }

    public HashMap clearWeigh(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        CommService.getCommService().execute("CLEAR_WEIGH", null);
        return null;
    }
}
