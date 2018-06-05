package com.incomrecycle.prms.rvm.service.commonservice.gui;

import android.database.sqlite.SQLiteDatabase;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.commtable.CommTableRecord;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.sqlite.DBQuery;
import com.incomrecycle.common.sqlite.DBSequence;
import com.incomrecycle.common.sqlite.RowSet;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlBuilder;
import com.incomrecycle.common.sqlite.SqlDeleteBuilder;
import com.incomrecycle.common.sqlite.SqlInsertBuilder;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.EncryptUtils;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.ShellUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.RVMShell;
import com.incomrecycle.prms.rvm.common.SysDef;
import com.incomrecycle.prms.rvm.common.SysDef.AlarmId;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.MsgType;
import com.incomrecycle.prms.rvm.common.SysDef.ServiceName;
import com.incomrecycle.prms.rvm.common.SysDef.TrafficType;
import com.incomrecycle.prms.rvm.common.SysDef.networkSts;
import com.incomrecycle.prms.rvm.common.SysDef.staffPermission;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import com.incomrecycle.prms.rvm.service.commonservice.BaseAppCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.InitCommonService;
import com.incomrecycle.prms.rvm.service.task.action.RCCInstanceTask;
import com.incomrecycle.prms.rvmdaemon.RVMDaemonClient;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

public class GUIMaintenanceCommonService extends BaseAppCommonService {
    private static List<String> listRaisedTcpAlarm = new ArrayList();
    private static final Logger logger = LoggerFactory.getLogger("MAINTAIN");

    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if ("queryFullStoreAlarm".equalsIgnoreCase(subSvnName)) {
            return queryFullStoreAlarm(svcName, subSvnName, hsmpParam);
        }
        if ("init".equalsIgnoreCase(subSvnName)) {
            return init(svcName, subSvnName, hsmpParam);
        }
        if ("login".equalsIgnoreCase(subSvnName)) {
            return login(svcName, subSvnName, hsmpParam);
        }
        if ("getOptList".equalsIgnoreCase(subSvnName)) {
            return getOptList(svcName, subSvnName, hsmpParam);
        }
        if ("networkTesting".equalsIgnoreCase(subSvnName)) {
            return networkTesting(svcName, subSvnName, hsmpParam);
        }
        if ("getAlarmList".equalsIgnoreCase(subSvnName)) {
            return getAlarmList(svcName, subSvnName, hsmpParam);
        }
        if ("openDoor".equals(subSvnName)) {
            return openDoor(svcName, subSvnName, hsmpParam);
        }
        if ("openPaperDoor".equals(subSvnName)) {
            return openPaperDoor(svcName, subSvnName, hsmpParam);
        }
        if ("clearNum".equals(subSvnName)) {
            return clearNum(svcName, subSvnName, hsmpParam);
        }
        if ("clearWeight".equals(subSvnName)) {
            return clearWeight(svcName, subSvnName, hsmpParam);
        }
        if ("linkTest".equals(subSvnName)) {
            return linkTest(svcName, subSvnName, hsmpParam);
        }
        if ("saveConfig".equals(subSvnName)) {
            return saveConfig(hsmpParam);
        }
        if ("saveHardwareConfig".equals(subSvnName)) {
            return saveHardwareConfig(hsmpParam);
        }
        if ("alarmManualRecovery".equals(subSvnName)) {
            return alarmManualRecovery(svcName, subSvnName, hsmpParam);
        }
        if ("startOrStopService".equals(subSvnName)) {
            return startOrStopService(svcName, subSvnName, hsmpParam);
        }
        if ("sysUpdate".equals(subSvnName)) {
            return sysUpdate(svcName, subSvnName, hsmpParam);
        }
        if ("sysInit".equals(subSvnName)) {
            return sysInit(svcName, subSvnName, hsmpParam);
        }
        if ("sysExit".equals(subSvnName)) {
            return sysExit(svcName, subSvnName, hsmpParam);
        }
        if ("UnPowerOff".equals(subSvnName)) {
            return unPowerOff(svcName, subSvnName, hsmpParam);
        }
        if ("setRvmAliveTime".equals(subSvnName)) {
            return setRvmAliveTime(svcName, subSvnName, hsmpParam);
        }
        if ("clearRvmAliveTime".equals(subSvnName)) {
            return clearRvmAliveTime(svcName, subSvnName, hsmpParam);
        }
        if ("sendHeartBeat".equals(subSvnName)) {
            return sendHeartBeat(svcName, subSvnName, hsmpParam);
        }
        if ("setHeartBeat".equals(subSvnName)) {
            return setHeartBeat(svcName, subSvnName, hsmpParam);
        }
        if ("checkSystemStatus".equals(subSvnName)) {
            return checkSystemStatus(svcName, subSvnName, hsmpParam);
        }
        if ("saveLanguage".equals(subSvnName)) {
            return saveLanguage(hsmpParam);
        }
        if ("checkTcpAlarm".equals(subSvnName)) {
            return checkTcpAlarm(svcName, subSvnName, hsmpParam);
        }
        if ("workerSignIn".equalsIgnoreCase(subSvnName)) {
            return workerSignIn(svcName, subSvnName, hsmpParam);
        }
        if ("rvmStartUsing".equalsIgnoreCase(subSvnName)) {
            return rvmStartUsing(svcName, subSvnName, hsmpParam);
        }
        if ("maintainUpdate".equalsIgnoreCase(subSvnName)) {
            return maintainUpdate(svcName, subSvnName, hsmpParam);
        }
        if ("maintainAddOptCon".equalsIgnoreCase(subSvnName)) {
            return maintainAddOptCon(svcName, subSvnName, hsmpParam);
        }
        if ("maintainToRCC".equalsIgnoreCase(subSvnName)) {
            return maintainToRCC(svcName, subSvnName, hsmpParam);
        }
        if ("backupRvmCode".equalsIgnoreCase(subSvnName)) {
            return backupRvmCode(svcName, subSvnName, hsmpParam);
        }
        if ("updateDetection".equalsIgnoreCase(subSvnName)) {
            return updateDetection(svcName, subSvnName, hsmpParam);
        }
        if ("startDubugg".equalsIgnoreCase(subSvnName)) {
            return startDubugg(svcName, subSvnName, hsmpParam);
        }
        if ("initUpdateCheck".equalsIgnoreCase(subSvnName)) {
            return initUpdateCheck(svcName, subSvnName, hsmpParam);
        }
        if ("startDetection".equalsIgnoreCase(subSvnName)) {
            return startDetection(svcName, subSvnName, hsmpParam);
        }
        if ("verifyPhone".equalsIgnoreCase(subSvnName)) {
            return verifyPhone(svcName, subSvnName, hsmpParam);
        }
        if ("queryCanTakePhoto".equalsIgnoreCase(subSvnName)) {
            return queryCanTakePhoto(svcName, subSvnName, hsmpParam);
        }
        if ("saveTakePhotoEnable".equalsIgnoreCase(subSvnName)) {
            return saveTakePhotoEnable(svcName, subSvnName, hsmpParam);
        }
        if ("saveStopRecycleTime".equalsIgnoreCase(subSvnName)) {
            return saveStopRecycleTime(svcName, subSvnName, hsmpParam);
        }
        if ("getStopRecycleTime".equalsIgnoreCase(subSvnName)) {
            return getStopRecycleTime(svcName, subSvnName, hsmpParam);
        }
        if ("getTrafficRecord".equalsIgnoreCase(subSvnName)) {
            return getTrafficRecord(svcName, subSvnName, hsmpParam);
        }
        if ("saveTrafficRecord".equalsIgnoreCase(subSvnName)) {
            return saveTrafficRecord(svcName, subSvnName, hsmpParam);
        }
        if ("clearTrafficRecord".equalsIgnoreCase(subSvnName)) {
            return clearTrafficRecord(svcName, subSvnName, hsmpParam);
        }
        return null;
    }

    private HashMap queryFullStoreAlarm(String svcName, String subSvnName, HashMap hsmpParam) {
        HashMap hsmpSend = new HashMap();
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        CommTable commTable;
        if (((Boolean) hsmpParam.get("ISRECOVERY_MANUAL")).booleanValue()) {
            sqlWhereBuilder.addNumberEqualsTo("ALARM_INST_ID", (String) hsmpParam.get("ALARM_INST_ID"));
            commTable = dbQuery.getCommTable("select * from RVM_ALARM_INST " + sqlWhereBuilder.toSqlWhere("where"));
            if (commTable.getRecordCount() == 0) {
                return null;
            }
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                String alarmId = commTable.getRecord(i).get("ALARM_INST_ID");
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
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Long AMTime = Long.valueOf(date.getTime());
                String finalAlarmInstId = new SimpleDateFormat("yyyyMMdd").format(date) + "_" + alarmId;
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
                hsmpSend.put("FROM", "APP");
                String putdownCountReal = "0";
                try {
                    putdownCountReal = (String) CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryStorageCount", null).get("BOTTLE_NOW");
                    if (StringUtils.isBlank(putdownCountReal) || "0".endsWith(putdownCountReal)) {
                        putdownCountReal = (String) ServiceGlobal.getCurrentSession("BOTTLE_NOW");
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                hsmpSend.put("BOTTLE_NOW", putdownCountReal);
                if (startOperTime == null || "".equalsIgnoreCase(startOperTime)) {
                    hsmpSend.put("START_OPER_DATE", null);
                } else {
                    hsmpSend.put("START_OPER_DATE", Long.valueOf(date2.getTime()).toString());
                }
                hsmpSend.put("OPSER_STAFF", userStaffId);
            }
            return hsmpSend;
        }
        sqlWhereBuilder.addNumberIn("ALARM_STATUS", (Object[]) new Object[]{Integer.valueOf(1), Integer.valueOf(2)}).addNumberEqualsTo("ALARM_ID", Integer.valueOf(11));
        commTable = dbQuery.getCommTable("select * from RVM_ALARM_INST " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() <= 0) {
            sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addNumberIn("ALARM_STATUS", (Object[]) new Object[]{Integer.valueOf(1), Integer.valueOf(2)}).addNumberEqualsTo("ALARM_ID", Integer.valueOf(12));
            commTable = dbQuery.getCommTable("select * from RVM_ALARM_INST " + sqlWhereBuilder.toSqlWhere("where"));
        }
        if (commTable.getRecordCount() <= 0) {
            return hsmpSend;
        }
        hsmpSend.put("ALARM_INST_ID", commTable.getRecord(0).get("ALARM_INST_ID"));
        return hsmpSend;
    }

    private HashMap verifyPhone(String svcName, String subSvnName, HashMap hsmpParam) {
        String PhoneNumber = (String) hsmpParam.get("PHONENUM");
        if (StringUtils.isBlank(PhoneNumber)) {
            PhoneNumber = "0";
        }
        Long Time = Long.valueOf(new Date().getTime());
        String QuTime = Time.toString();
        HashMap hsmpretPkg = new HashMap();
        hsmpretPkg.put("MES_TYPE", MsgType.RVM_BIND_SIM);
        hsmpretPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpretPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
        hsmpretPkg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + Time);
        hsmpretPkg.put("UP_DATE", Time);
        hsmpretPkg.put("SIM_NO", PhoneNumber);
        try {
            HashMap hsmpretPkgs = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpretPkg)));
            if (hsmpretPkgs == null) {
                return null;
            }
            if (((String) hsmpretPkgs.get("CONFIRM")).equalsIgnoreCase("1")) {
                HashMap hsmpResult = new HashMap();
                hsmpResult.put("MES_TYPE", hsmpretPkgs.get("MES_TYPE"));
                hsmpResult.put("RCC_NO", hsmpretPkgs.get("RCC_NO"));
                hsmpResult.put("TEAM_NO", hsmpretPkgs.get("TEAM_NO"));
                hsmpResult.put("OP_BATCH_ID", hsmpretPkgs.get("OP_BATCH_ID"));
                hsmpResult.put("CONFIRM", hsmpretPkgs.get("CONFIRM"));
                return hsmpResult;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private HashMap initUpdateCheck(String svcName, String subSvnName, HashMap hsmpParam) {
        SysConfig.set("DATA_UPDATE_CHECK_ENABLE", "FALSE");
        SysConfig.set("DATA_UPDATE_COMPLETED", "TRUE");
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderRvmCheckEnable = new SqlWhereBuilder();
        sqlWhereBuilderRvmCheckEnable.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "DATA_UPDATE_ENABLE");
        CommTable commTableRvmCheckEnable = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmCheckEnable.toSqlWhere("where"));
        if (commTableRvmCheckEnable.getRecordCount() > 0) {
            if ("TRUE".equalsIgnoreCase(commTableRvmCheckEnable.getRecord(0).get("SYS_CODE_VALUE"))) {
                SysConfig.set("DATA_UPDATE_CHECK_ENABLE", "TRUE");
            } else {
                SysConfig.set("DATA_UPDATE_CHECK_ENABLE", "FALSE");
            }
        } else {
            SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_SYS_CODE");
            sqlInsertBuilder.newInsertRecord().setString("SYS_CODE_TYPE", "RVM_INFO").setString("SYS_CODE_KEY", "DATA_UPDATE_ENABLE").setString("SYS_CODE_VALUE", "FALSE");
            sqliteDatabase.execSQL(sqlInsertBuilder.toSql());
            SysConfig.set("DATA_UPDATE_CHECK_ENABLE", "FALSE");
        }
        String UPDATE_DETECTION_SET = SysConfig.get("UPDATE.DETECTION");
        if (StringUtils.isBlank(UPDATE_DETECTION_SET)) {
            SysConfig.set("DATA_UPDATE_COMPLETED", "TRUE");
        } else {
            SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
            sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "DATA_UPDATE_SET");
            CommTable commTable = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
            if (commTable.getRecordCount() > 0) {
                String DATA_UPDATE_SET = commTable.getRecord(0).get("SYS_CODE_VALUE");
                if (StringUtils.isBlank(DATA_UPDATE_SET)) {
                    SysConfig.set("DATA_UPDATE_COMPLETED", "FALSE");
                } else {
                    String[] updateDetectionSets = UPDATE_DETECTION_SET.split(";");
                    for (CharSequence contains : updateDetectionSets) {
                        if (!DATA_UPDATE_SET.contains(contains)) {
                            SysConfig.set("DATA_UPDATE_COMPLETED", "FALSE");
                            break;
                        }
                    }
                }
            } else {
                SysConfig.set("DATA_UPDATE_COMPLETED", "FALSE");
            }
        }
        return null;
    }

    private HashMap startDetection(String svcName, String subSvnName, HashMap hsmpParam) {
        SysConfig.set("DATA_UPDATE_CHECK_ENABLE", "TRUE");
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderRvmCheckEnable = new SqlWhereBuilder();
        sqlWhereBuilderRvmCheckEnable.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "DATA_UPDATE_ENABLE");
        if (dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmCheckEnable.toSqlWhere("where")).getRecordCount() > 0) {
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_SYS_CODE");
            sqlUpdateBuilder.setString("SYS_CODE_VALUE", "TRUE").setSqlWhere(sqlWhereBuilderRvmCheckEnable);
            sqliteDatabase.execSQL(sqlUpdateBuilder.toSql());
        } else {
            SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_SYS_CODE");
            sqlInsertBuilder.newInsertRecord().setString("SYS_CODE_TYPE", "RVM_INFO").setString("SYS_CODE_KEY", "DATA_UPDATE_ENABLE").setString("SYS_CODE_VALUE", "TRUE");
            sqliteDatabase.execSQL(sqlInsertBuilder.toSql());
        }
        return null;
    }

    private HashMap startDubugg(String svcName, String subSvnName, HashMap hsmpParam) {
        SysConfig.set("DATA_UPDATE_CHECK_ENABLE", "FALSE");
        return null;
    }

    private HashMap updateDetection(String svcName, String subSvnName, HashMap hsmpParam) {
        String UPDATE_DETECTION = null;
        if (hsmpParam != null && hsmpParam.size() > 0) {
            UPDATE_DETECTION = (String) hsmpParam.get("UPDATE_DETECTION");
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "DATA_UPDATE_SET");
        CommTable commTable = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        String DATA_UPDATE_SET = null;
        if (commTable.getRecordCount() > 0) {
            DATA_UPDATE_SET = commTable.getRecord(0).get("SYS_CODE_VALUE");
        }
        if (StringUtils.isBlank(DATA_UPDATE_SET) || !DATA_UPDATE_SET.contains(UPDATE_DETECTION)) {
            DATA_UPDATE_SET = DATA_UPDATE_SET + ";" + UPDATE_DETECTION;
        }
        if (commTable.getRecordCount() > 0) {
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_SYS_CODE");
            sqlUpdateBuilder.setString("SYS_CODE_VALUE", DATA_UPDATE_SET).setSqlWhere(sqlWhereBuilderRvmSysCode);
            sqliteDatabase.execSQL(sqlUpdateBuilder.toSql());
        } else {
            SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_SYS_CODE");
            sqlInsertBuilder.newInsertRecord().setString("SYS_CODE_TYPE", "RVM_INFO").setString("SYS_CODE_KEY", "DATA_UPDATE_SET").setString("SYS_CODE_VALUE", DATA_UPDATE_SET);
            sqliteDatabase.execSQL(sqlInsertBuilder.toSql());
        }
        if ("FALSE".equals(SysConfig.get("DATA_UPDATE_CHECK_ENABLE")) || "TRUE".equals(SysConfig.get("DATA_UPDATE_COMPLETED"))) {
            return null;
        }
        String UPDATE_DETECTION_SET = SysConfig.get("UPDATE.DETECTION");
        SysConfig.set("DATA_UPDATE_COMPLETED", "TRUE");
        if (StringUtils.isBlank(UPDATE_DETECTION_SET)) {
            SysConfig.set("DATA_UPDATE_COMPLETED", "TRUE");
        } else if (StringUtils.isBlank(DATA_UPDATE_SET)) {
            SysConfig.set("DATA_UPDATE_COMPLETED", "FALSE");
        } else {
            String[] update_detection_sets = UPDATE_DETECTION_SET.split(";");
            for (CharSequence contains : update_detection_sets) {
                if (!DATA_UPDATE_SET.contains(contains)) {
                    SysConfig.set("DATA_UPDATE_COMPLETED", "FALSE");
                    break;
                }
            }
        }
        return null;
    }

    private HashMap maintainToRCC(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpSend = new HashMap();
        List<String> listOptCon = new ArrayList();
        Integer optId = Integer.valueOf(Integer.parseInt((String) ServiceGlobal.getCurrentSession("STAFF_OPT_ID")));
        Integer staffId = Integer.valueOf(Integer.parseInt((String) ServiceGlobal.getCurrentSession("USER_STAFF_ID")));
        if (!(optId == null || staffId == null)) {
            SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
            DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
            SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addNumberEqualsTo("UPLOAD_FLAG", Integer.valueOf(1)).addNumberEqualsTo("STAFF_ID", staffId).addNumberEqualsTo("OPT_ID", optId);
            CommTable commTable = dbQuery.getCommTable("select * from RVM_MAINTAIN_UPDATE " + sqlWhereBuilder.toSqlWhere("where"));
            if (commTable.getRecordCount() != 0) {
                for (int i = 0; i < commTable.getRecordCount(); i++) {
                    String OP_BATCH_ID = commTable.getRecord(i).get("OP_BATCH_ID");
                    Integer STAFF_ID = Integer.valueOf(Integer.parseInt(commTable.getRecord(i).get("STAFF_ID")));
                    Integer LOGIN_TYPE = Integer.valueOf(Integer.parseInt(commTable.getRecord(i).get("LOGIN_TYPE")));
                    String OPT_CONTENT = commTable.getRecord(i).get("OPT_CONTENT");
                    if (!(OPT_CONTENT == null || OPT_CONTENT == "")) {
                        String[] consumeContent = OPT_CONTENT.trim().split(",");
                        for (String con : consumeContent) {
                            if (!con.equalsIgnoreCase("null")) {
                                listOptCon.add(con);
                            }
                        }
                        if (listOptCon.size() > 0) {
                            Date date = null;
                            try {
                                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(commTable.getRecord(i).get("LOGIN_TIME"));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Long loginTime = Long.valueOf(date.getTime());
                            String QuTime = Long.valueOf(new Date().getTime()).toString();
                            hsmpSend.put("MES_TYPE", MsgType.RVM_MAINTAINER_OPT);
                            hsmpSend.put("TERM_NO", SysConfig.get("RVM.CODE"));
                            hsmpSend.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
                            hsmpSend.put("QU_TIME", QuTime);
                            hsmpSend.put("OP_BATCH_ID", OP_BATCH_ID);
                            hsmpSend.put("STAFF_ID", STAFF_ID);
                            hsmpSend.put("LOGIN_TYPE", LOGIN_TYPE);
                            hsmpSend.put("LOGIN_TIME", loginTime);
                            hsmpSend.put("OPT_CONTENT", listOptCon);
                            logger.debug("--------------------OPT_CONTENT:" + JSONUtils.toJSON(hsmpSend));
                            try {
                                HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
                                HashMap<String, String> hsmpInstanceTask = new HashMap();
                                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "MAINTAIN_OPT_CON");
                                RCCInstanceTask.addTask(hsmpInstanceTask);
                                if (hsmpretPkg == null) {
                                    return null;
                                }
                                String termNo = (String) hsmpretPkg.get("TERM_NO");
                                if ("RESPONSE".equalsIgnoreCase((String) hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo) && Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM"))).intValue() == 1) {
                                    SqlWhereBuilder sqlWhereBuild = new SqlWhereBuilder();
                                    sqlWhereBuild.addNumberEqualsTo("STAFF_ID", staffId);
                                    sqlWhereBuild.addNumberEqualsTo("OPT_ID", optId);
                                    SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_MAINTAIN_UPDATE");
                                    sqlDeleteBuilder.setSqlWhere(sqlWhereBuild);
                                    SQLiteExecutor.execSql(sqliteDatabase, sqlDeleteBuilder.toSql());
                                }
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
        return null;
    }

    private HashMap maintainAddOptCon(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        List optConList = (List) hsmpParam.get("OPT_OPTIONS");
        String subOptions = "";
        for (int i = 0; i < optConList.size(); i++) {
            String options = ((String) optConList.get(i)) + ",";
            if (i == optConList.size() - 1) {
                options = options.substring(0, options.length() - 1);
            }
            subOptions = subOptions + options;
        }
        if (!StringUtils.isBlank(subOptions)) {
            String STAFF_OPT_ID = (String) ServiceGlobal.getCurrentSession("STAFF_OPT_ID");
            Integer optId = null;
            if (!StringUtils.isBlank(STAFF_OPT_ID)) {
                optId = Integer.valueOf(Integer.parseInt(STAFF_OPT_ID));
            }
            if (!(optId == null || subOptions == null || subOptions == "")) {
                SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
                List<SqlBuilder> listSqlBuilder = new ArrayList();
                DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
                SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
                sqlWhereBuilder.addNumberEqualsTo("OPT_ID", optId);
                CommTable commTable = dbQuery.getCommTable("select * from RVM_MAINTAIN_UPDATE " + sqlWhereBuilder.toSqlWhere("where"));
                if (commTable.getRecordCount() != 0) {
                    String OPT_CONTENT = commTable.getRecord(0).get("OPT_CONTENT") + subOptions;
                    SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_MAINTAIN_UPDATE");
                    sqlUpdateBuilder.setString("OPT_CONTENT", OPT_CONTENT);
                    sqlUpdateBuilder.setSqlWhere(sqlWhereBuilder);
                    listSqlBuilder.add(sqlUpdateBuilder);
                    SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                }
            }
        }
        return null;
    }

    private HashMap maintainUpdate(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        Long Time = Long.valueOf(new Date().getTime());
        DBSequence dbSequence = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS"));
        Integer loginType = Integer.valueOf(Integer.parseInt((String) ServiceGlobal.getCurrentSession("LOGIN_TYPE")));
        Integer staffId = Integer.valueOf(Integer.parseInt((String) ServiceGlobal.getCurrentSession("USER_STAFF_ID")));
        String optId = dbSequence.getSeq("OPT_ID");
        ServiceGlobal.setCurrentSession("STAFF_OPT_ID", optId);
        String optBatchId = SysConfig.get("RVM.CODE") + "_" + Time;
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        if (!(loginType == null || staffId == null || optId == null)) {
            SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_MAINTAIN_UPDATE");
            sqlInsertBuilder.newInsertRecord().setNumber("OPT_ID", optId).setString("OP_BATCH_ID", optBatchId).setDateTime("LOGIN_TIME", new Date()).setNumber("STAFF_ID", staffId).setNumber("LOGIN_TYPE", loginType).setString("OPT_CONTENT", "null,").setNumber("UPLOAD_FLAG", Integer.valueOf(1));
            listSqlBuilder.add(sqlInsertBuilder);
            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        }
        return null;
    }

    private HashMap rvmStartUsing(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        Long Time = Long.valueOf(new Date().getTime());
        String QuTime = Time.toString();
        HashMap hsmpPkg = new HashMap();
        hsmpPkg.put("MES_TYPE", MsgType.RVM_STS_UPDATE);
        hsmpPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
        hsmpPkg.put("QU_TIME", QuTime);
        hsmpPkg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + Time);
        hsmpPkg.put("RVM_STATUS", "1");
        String retJson = JSONUtils.toJSON(hsmpPkg);
        HashMap hsmpRetPkg = new HashMap();
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "startDetection", null);
            HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPkg)));
            if (hsmpretPkg == null) {
                return null;
            }
            String termNo = (String) hsmpretPkg.get("TERM_NO");
            if ("RESPONSE".equalsIgnoreCase((String) hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                Integer Confirm = Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM")));
                if (Confirm.intValue() == 1) {
                    String userQRCode = SysConfig.get("DEFAULT.QRCODE");
                    String userStaffId = SysConfig.get("DEFAULT.QRCODE.ID");
                    if (!(StringUtils.isBlank(userQRCode) || StringUtils.isBlank(userStaffId))) {
                        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
                        List<SqlBuilder> listSqlBuilder = new ArrayList();
                        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
                        String userId = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS")).getSeq("USER_ID");
                        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
                        sqlWhereBuilder.addStringEqualsTo("USER_EXT_CODE", userQRCode).addNumberEqualsTo("USER_STATUS", Integer.valueOf(1));
                        if (dbQuery.getCommTable("select * from RVM_STAFF " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
                            SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_STAFF");
                            sqlInsertBuilder.newInsertRecord().setNumber("USER_ID", userId).setString("USER_EXT_CODE", userQRCode).setString("USER_LOGIN_NAME", null).setNumber("USER_STAFF_ID", Integer.valueOf(Integer.parseInt(userStaffId))).setNumber("USER_STATUS", Integer.valueOf(1)).setNumber("USER_PERMISSION", staffPermission.ADVANCE_STAFF);
                            listSqlBuilder.add(sqlInsertBuilder);
                            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                        }
                    }
                    hsmpRetPkg.put("RESULT", "success");
                }
                if (Confirm.intValue() == -1) {
                    hsmpRetPkg.put("RESULT", "error");
                }
                if (Confirm.intValue() != 0) {
                    return hsmpRetPkg;
                }
                hsmpRetPkg.put("RESULT", "failed");
                return hsmpRetPkg;
            }
            hsmpRetPkg.put("RESULT", "error");
            return hsmpRetPkg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private HashMap workerSignIn(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String staffName = (String) ServiceGlobal.getCurrentSession("userStaffName");
        String loginType = (String) ServiceGlobal.getCurrentSession("LOGIN_TYPE");
        if (StringUtils.isBlank(staffName)) {
            staffName = (String) ServiceGlobal.getCurrentSession("userQRCode");
        }
        if (StringUtils.isBlank(staffName)) {
            return null;
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        DBSequence dbSequence = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS"));
        SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_WORKER_SIGN_IN");
        String userId = dbSequence.getSeq("USER_ID");
        Long Time = Long.valueOf(new Date().getTime());
        String UpTime = Time.toString();
        HashMap hsmpPkg = new HashMap();
        hsmpPkg.put("MES_TYPE", MsgType.RVM_WORKER_REGISTER);
        hsmpPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
        hsmpPkg.put("RE_TIME", UpTime);
        hsmpPkg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + Time);
        hsmpPkg.put("STAFF_NAME", staffName);
        hsmpPkg.put("LOGIN_TYPE", loginType);
        hsmpPkg.put("UP_TIME", UpTime);
        String retJson = JSONUtils.toJSON(hsmpPkg);
        HashMap hsmpRetPkg = new HashMap();
        try {
            HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPkg)));
            if (hsmpretPkg == null) {
                sqlInsertBuilder.newInsertRecord().setNumber("USER_ID", userId).setString("USER_STAFF_NAME", staffName).setNumber("USER_STAFF_ID", (String) ServiceGlobal.getCurrentSession("USER_STAFF_ID")).setDateTime("SIGN_IN_TIME", new Date()).setNumber("LOGIN_TYPE", loginType).setNumber("UPLOAD_FLAG", Integer.valueOf(1));
                listSqlBuilder.add(sqlInsertBuilder);
                SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                return null;
            }
            String termNo = (String) hsmpretPkg.get("TERM_NO");
            if ("RESPONSE".equalsIgnoreCase((String) hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                Integer Confirm = Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM")));
                if (Confirm.intValue() == 1) {
                    hsmpRetPkg.put("RESULT", "success");
                }
                if (Confirm.intValue() == -1) {
                    hsmpRetPkg.put("RESULT", "error");
                }
                if (Confirm.intValue() != 0) {
                    return hsmpRetPkg;
                }
                hsmpRetPkg.put("RESULT", "failed");
                sqlInsertBuilder.newInsertRecord().setNumber("USER_ID", userId).setString("USER_STAFF_NAME", staffName).setNumber("USER_STAFF_ID", (String) ServiceGlobal.getCurrentSession("USER_STAFF_ID")).setDateTime("SIGN_IN_TIME", new Date()).setNumber("LOGIN_TYPE", loginType).setNumber("UPLOAD_FLAG", Integer.valueOf(1));
                listSqlBuilder.add(sqlInsertBuilder);
                SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                return hsmpRetPkg;
            }
            hsmpRetPkg.put("RESULT", "error");
            return hsmpRetPkg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap init(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        ServiceGlobal.clearCurrentSession();
        return null;
    }

    public HashMap loginRCCCheck(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpResult = new HashMap();
        String username = (String) hsmpParam.get("USER");
        String md5Password = EncryptUtils.md5((String) hsmpParam.get("PASSWORD"));
        long OPT_TIME = new Date().getTime();
        String OP_BATCH_ID = SysConfig.get("RVM.CODE") + "_" + OPT_TIME + "_" + SysDef.getOptSeq();
        HashMap hsmpLoginPkg = new HashMap();
        hsmpLoginPkg.put("MES_TYPE", "WORKER_LOGIN");
        hsmpLoginPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpLoginPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
        hsmpLoginPkg.put("OPT_TIME", "" + OPT_TIME);
        hsmpLoginPkg.put("OP_BATCH_ID", OP_BATCH_ID);
        hsmpLoginPkg.put("TERM_NAME", username);
        hsmpLoginPkg.put("TERM_PWD", md5Password);
        String retPkg = CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpLoginPkg));
        if (retPkg == null) {
            return null;
        }
        HashMap<String, String> hsmpRetPkg = JSONUtils.toHashMap(retPkg);
        if (!((String) hsmpLoginPkg.get("TERM_NO")).equals(hsmpRetPkg.get("TERM_NO")) || !((String) hsmpLoginPkg.get("OP_BATCH_ID")).equals(hsmpRetPkg.get("OP_BATCH_ID"))) {
            hsmpResult.put("RET_CODE", "failed");
            return hsmpResult;
        } else if ("1".equalsIgnoreCase((String) hsmpRetPkg.get("LOGIN_TAG"))) {
            String Staff_Id = (String) hsmpRetPkg.get("STAFF_ID");
            String Staff_Permission = (String) hsmpRetPkg.get("STAFF_PERMISSION");
            if (StringUtils.isBlank(Staff_Permission)) {
                Staff_Permission = staffPermission.ADVANCE_STAFF;
            }
            ServiceGlobal.setCurrentSession("USER_STAFF_ID", Staff_Id);
            ServiceGlobal.setCurrentSession("LOGIN_TYPE", "1");
            hsmpResult.put("RET_CODE", "success");
            hsmpResult.put("STAFF_PERMISSION", Staff_Permission);
            ServiceGlobal.setCurrentSession("userStaffName", username);
            return hsmpResult;
        } else {
            hsmpResult.put("RET_CODE", "failed");
            return hsmpResult;
        }
    }

    public HashMap loginByUserLoginName(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpResult = new HashMap();
        String userStaffid = (String) hsmpParam.get("USER");
        String password = (String) hsmpParam.get("PASSWORD");
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("USER_LOGIN_NAME", userStaffid);
        CommTable commTable = dbQuery.getCommTable("select * from RVM_STAFF " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() == 0) {
            return loginRCCCheck(svcName, subSvnName, hsmpParam);
        }
        String md5Password = EncryptUtils.md5(password);
        if (!StringUtils.isBlank(commTable.getRecord(0).get("USER_PASSWORD"))) {
            if (md5Password.equalsIgnoreCase(commTable.getRecord(0).get("USER_PASSWORD"))) {
                sqlWhereBuilder = new SqlWhereBuilder();
                sqlWhereBuilder.addStringEqualsTo("USER_STATUS", "1").addStringNotEqualsTo("USER_LOGIN_NAME", userStaffid);
                if (dbQuery.getCommTable("select * from RVM_STAFF " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
                    ServiceGlobal.setCurrentSession("USER_STAFF_ID", ((CommTableRecord) commTable.getRecord().get(0)).get("USER_STAFF_ID"));
                    ServiceGlobal.setCurrentSession("LOGIN_TYPE", "1");
                    String staffPermssion = commTable.getRecord(0).get("USER_PERMISSION");
                    if (StringUtils.isBlank(staffPermssion)) {
                        staffPermssion = staffPermission.ADVANCE_STAFF;
                    }
                    hsmpResult.put("STAFF_PERMISSION", staffPermssion);
                    hsmpResult.put("RET_CODE", "success");
                    ServiceGlobal.setCurrentSession("userStaffName", userStaffid);
                    return hsmpResult;
                }
            }
            hsmpResult.put("RET_CODE", "failed");
            return hsmpResult;
        }
        return loginRCCCheck(svcName, subSvnName, hsmpParam);
    }

    public HashMap loginByD2Code(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpResult = new HashMap();
        String userExtCode = (String) hsmpParam.get("USER_EXT_CODE");
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("USER_EXT_CODE", userExtCode).addNumberEqualsTo("USER_STATUS", Integer.valueOf(1));
        CommTable commTable = dbQuery.getCommTable("select * from RVM_STAFF " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() == 0) {
            hsmpResult.put("RET_CODE", "not_found");
        } else {
            ServiceGlobal.setCurrentSession("USER_STAFF_ID", commTable.getRecord(0).get("USER_STAFF_ID"));
            ServiceGlobal.setCurrentSession("LOGIN_TYPE", "2");
            String staffPermssion = commTable.getRecord(0).get("USER_PERMISSION");
            if (StringUtils.isBlank(staffPermssion)) {
                staffPermssion = staffPermission.ADVANCE_STAFF;
            }
            hsmpResult.put("STAFF_PERMISSION", staffPermssion);
            hsmpResult.put("RET_CODE", "success");
            ServiceGlobal.setCurrentSession("userQRCode", userExtCode);
        }
        return hsmpResult;
    }

    public HashMap login(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpResult = new HashMap();
        String username = (String) hsmpParam.get("USER");
        String password = (String) hsmpParam.get("PASSWORD");
        if ("USER_STAFF_ID".equalsIgnoreCase((String) hsmpParam.get("LOGIN_TYPE"))) {
            return loginByUserLoginName(svcName, subSvnName, hsmpParam);
        }
        return loginByD2Code(svcName, subSvnName, hsmpParam);
    }

    private HashMap saveConfig(HashMap hsmpParam) {
        String OLD_RVM_CODE = SysConfig.get("RVM.CODE");
        Properties externalProp = new Properties();
        externalProp.put("RVM.CODE", hsmpParam.get("TIMER_NO"));
        externalProp.put("RVM.AREA.CODE", SysConfig.get("RVM.AREA.CODE"));
        externalProp.put("RCC.IP", hsmpParam.get("RCC_IP"));
        externalProp.put("RCC.PORT", hsmpParam.get("RCC_PORT"));
        externalProp.put("VENDING.WAY", hsmpParam.get(AllAdvertisement.VENDING_WAY));
        externalProp.put("CHANNEL.PHONE.NUMER", hsmpParam.get("PHONE_NUMBER"));
        PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
        SysConfig.set(externalProp);
        RVMShell.backupExternalConfig();
        RVMShell.backupKeySettings(SysConfig.get("KEY_SETTINGS_FILE"), SysConfig.get("KEY_SETTINGS_ATTR"), PropUtils.loadFile(SysConfig.get("EXTERNAL.FILE")));
        String NEW_RVM_CODE = SysConfig.get("RVM.CODE");
        if (!StringUtils.equals(OLD_RVM_CODE, NEW_RVM_CODE)) {
            try {
                new InitCommonService().execute("InitCommonService", "initRVM", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!(StringUtils.isBlank(NEW_RVM_CODE) || "0".equals(NEW_RVM_CODE))) {
            try {
                CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "startDetection", null);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        HashMap hsmpEvent = new HashMap();
        hsmpEvent.put(AllAdvertisement.MEDIA_TYPE, "RVM_CODE_BACKUP");
        RCCInstanceTask.addTask(hsmpEvent);
        HashMap resultMap = new HashMap();
        resultMap.put("RESULT", Boolean.valueOf(true));
        return resultMap;
    }

    private HashMap saveHardwareConfig(HashMap hsmpParam) {
        Properties externalProp = new Properties();
        externalProp.put("RVM.MODE", hsmpParam.get("RVM.MODE"));
        externalProp.put("HARDWARE.VERSION", hsmpParam.get("HARDWARE.VERSION"));
        PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
        SysConfig.set(externalProp);
        RVMShell.backupExternalConfig();
        HashMap resultMap = new HashMap();
        resultMap.put("RESULT", Boolean.valueOf(true));
        return resultMap;
    }

    private HashMap saveLanguage(HashMap hsmpParam) {
        Properties externalProp = new Properties();
        externalProp.put("RVM.LANGUAGE", hsmpParam.get("RVM.LANGUAGE"));
        PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
        SysConfig.set(externalProp);
        RVMShell.backupExternalConfig();
        HashMap resultMap = new HashMap();
        resultMap.put("RESULT", Boolean.valueOf(true));
        return resultMap;
    }

    private HashMap linkTest(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpJSON = new HashMap();
        hsmpJSON.put("RCC_IP", hsmpParam.get("RCC_IP"));
        hsmpJSON.put("RCC_PORT", hsmpParam.get("RCC_PORT"));
        hsmpJSON.put("RVM_CODE", hsmpParam.get("RVM_CODE"));
        hsmpJSON.put("RVM_AREA_CODE", hsmpParam.get("RVM_AREA_CODE"));
        return JSONUtils.toHashMap(CommService.getCommService().execute("LINK_TESTING", JSONUtils.toJSON(hsmpJSON)));
    }

    private HashMap clearNum(String svcName, String subSvnName, HashMap hsmpParam) {
        int i;
        HashMap hsmpResult = new HashMap();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringIn("SYS_CODE_KEY", new String[]{"STORAGE_CURR_COUNT", "STORAGE_CURR_COUNT_DELTA"});
        HashMap hsmpPrmam = new HashMap();
        CommTable commTableSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        if (commTableSysCode.getRecordCount() > 0) {
            i = 0;
            while (i < commTableSysCode.getRecordCount()) {
                hsmpPrmam.put(commTableSysCode.getRecord(i).get("SYS_CODE_KEY"), commTableSysCode.getRecord(i).get("SYS_CODE_VALUE"));
                if (i == commTableSysCode.getRecordCount() - 1) {
                    String str = "0";
                    String putdownCountDelta = "0";
                    String putdownCountReal = "0";
                    try {
                        str = (String) hsmpPrmam.get("STORAGE_CURR_COUNT");
                        putdownCountDelta = (String) hsmpPrmam.get("STORAGE_CURR_COUNT_DELTA");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (StringUtils.isBlank(str)) {
                        str = "0";
                    }
                    if (StringUtils.isBlank(putdownCountDelta)) {
                        putdownCountDelta = "0";
                    }
                    try {
                        if (Integer.parseInt(str) > Integer.parseInt(putdownCountDelta)) {
                            putdownCountReal = Integer.toString(Integer.parseInt(str) - Integer.parseInt(putdownCountDelta));
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    ServiceGlobal.setCurrentSession("BOTTLE_NOW", putdownCountReal);
                }
                if ("STORAGE_CURR_COUNT".equals(commTableSysCode.getRecord(i).get("SYS_CODE_KEY")) && "0".equals(commTableSysCode.getRecord(i).get("SYS_CODE_VALUE"))) {
                    hsmpResult.put("RESULT", Boolean.valueOf(true));
                    break;
                }
                i++;
            }
        }
        SqlWhereBuilder sqlWhereBuilderStorage = new SqlWhereBuilder();
        sqlWhereBuilderStorage.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringIn("SYS_CODE_KEY", new String[]{"STORAGE_CURR_COUNT", "STORAGE_CURR_COUNT_DELTA"});
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_SYS_CODE");
        sqlUpdateBuilder.setString("SYS_CODE_VALUE", "0").setSqlWhere(sqlWhereBuilderStorage);
        listSqlBuilder.add(sqlUpdateBuilder);
        SqlWhereBuilder sqlWhereBuilderRvmAlarmInst = new SqlWhereBuilder();
        Object[] objArr = new Object[]{Integer.valueOf(11), Integer.valueOf(12)};
        sqlWhereBuilderRvmAlarmInst.addNumberIn("ALARM_STATUS", (Object[]) new Object[]{Integer.valueOf(1), Integer.valueOf(2)}).addNumberIn("ALARM_ID", objArr);
        CommTable commTableRvmAlarmInst = dbQuery.getCommTable("select * from RVM_ALARM_INST" + sqlWhereBuilderRvmAlarmInst.toSqlWhere("where"));
        sqlUpdateBuilder = new SqlUpdateBuilder("RVM_ALARM_INST");
        sqlUpdateBuilder.setNumber("ALARM_STATUS", Integer.valueOf(3)).setNumber("UPLOAD_FLAG", "0").setNumber("USER_STAFF_ID", (String) ServiceGlobal.getCurrentSession("USER_STAFF_ID")).setTime("START_OPER_DATE", new Date()).setSqlWhere(sqlWhereBuilderRvmAlarmInst);
        listSqlBuilder.add(sqlUpdateBuilder);
        boolean hasBottleMaxAlarm = false;
        List<HashMap<String, String>> listInstanceTask = new ArrayList();
        for (i = 0; i < commTableRvmAlarmInst.getRecordCount(); i++) {
            CommTableRecord ctr = commTableRvmAlarmInst.getRecord(i);
            if (Integer.parseInt(ctr.get("ALARM_ID")) == 11) {
                hasBottleMaxAlarm = true;
            }
            HashMap<String, String> hsmpInstanceTask = new HashMap();
            hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
            hsmpInstanceTask.put("ALARM_INST_ID", ctr.get("ALARM_INST_ID"));
            listInstanceTask.add(hsmpInstanceTask);
        }
        if (!hasBottleMaxAlarm) {
            String ALARM_INST_ID = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS")).getSeq("ALARM_INST_ID");
            SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_ALARM_INST");
            sqlInsertBuilder.newInsertRecord().setNumber("ALARM_INST_ID", ALARM_INST_ID).setNumber("ALARM_TYPE", "0").setDateTime("ALARM_TIME", new Date()).setNumber("ALARM_ID", Integer.valueOf(11)).setNumber("UPLOAD_FLAG", "0").setNumber("ALARM_STATUS", Integer.valueOf(3)).setNumber("USER_STAFF_ID", (String) ServiceGlobal.getCurrentSession("USER_STAFF_ID")).setDateTime("START_OPER_DATE", new Date());
            listSqlBuilder.add(sqlInsertBuilder);
            HashMap hsmpInstanceTask = new HashMap();
            hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
            hsmpInstanceTask.put("ALARM_INST_ID", ALARM_INST_ID);
            listInstanceTask.add(hsmpInstanceTask);
        }
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        for (i = 0; i < listInstanceTask.size(); i++) {
            RCCInstanceTask.addTask((HashMap) listInstanceTask.get(i));
        }
        hsmpResult.put("RESULT", Boolean.valueOf(true));
        return hsmpResult;
    }

    private HashMap clearWeight(String svcName, String subSvnName, HashMap hsmpParam) {
        HashMap hsmpResult = new HashMap();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "STORAGE_CURR_PAPER_WEIGH");
        CommTable commTableSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        if (commTableSysCode.getRecordCount() <= 0 || !"0".equals(commTableSysCode.getRecord(0).get("SYS_CODE_VALUE"))) {
            int i;
            HashMap<String, String> hsmpInstanceTask;
            List<SqlBuilder> listSqlBuilder = new ArrayList();
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_SYS_CODE");
            sqlUpdateBuilder.setString("SYS_CODE_VALUE", "0").setSqlWhere(sqlWhereBuilderRvmSysCode);
            listSqlBuilder.add(sqlUpdateBuilder);
            SqlWhereBuilder sqlWhereBuilderRvmAlarmInst = new SqlWhereBuilder();
            Object[] objArr = new Object[]{Integer.valueOf(13), Integer.valueOf(14)};
            sqlWhereBuilderRvmAlarmInst.addNumberIn("ALARM_STATUS", (Object[]) new Object[]{Integer.valueOf(1), Integer.valueOf(2)}).addNumberIn("ALARM_ID", objArr);
            CommTable commTableRvmAlarmInst = dbQuery.getCommTable("select * from RVM_ALARM_INST" + sqlWhereBuilderRvmAlarmInst.toSqlWhere("where"));
            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_ALARM_INST");
            sqlUpdateBuilder.setNumber("ALARM_STATUS", Integer.valueOf(3)).setNumber("UPLOAD_FLAG", "0").setNumber("USER_STAFF_ID", (String) ServiceGlobal.getCurrentSession("USER_STAFF_ID")).setTime("START_OPER_DATE", new Date()).setSqlWhere(sqlWhereBuilderRvmAlarmInst);
            listSqlBuilder.add(sqlUpdateBuilder);
            boolean hasPaperMaxAlarm = false;
            List<HashMap<String, String>> listInstanceTask = new ArrayList();
            for (i = 0; i < commTableRvmAlarmInst.getRecordCount(); i++) {
                CommTableRecord ctr = commTableRvmAlarmInst.getRecord(i);
                if (Integer.parseInt(ctr.get("ALARM_ID")) == 13) {
                    hasPaperMaxAlarm = true;
                }
                hsmpInstanceTask = new HashMap();
                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
                hsmpInstanceTask.put("ALARM_INST_ID", ctr.get("ALARM_INST_ID"));
                listInstanceTask.add(hsmpInstanceTask);
            }
            if (!hasPaperMaxAlarm) {
                String ALARM_INST_ID = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS")).getSeq("ALARM_INST_ID");
                SqlInsertBuilder sqlInsertBuilderRvmAlarmInst = new SqlInsertBuilder("RVM_ALARM_INST");
                sqlInsertBuilderRvmAlarmInst.newInsertRecord().setNumber("ALARM_INST_ID", ALARM_INST_ID).setNumber("ALARM_TYPE", "0").setDateTime("ALARM_TIME", new Date()).setNumber("ALARM_ID", Integer.valueOf(13)).setNumber("UPLOAD_FLAG", "0").setNumber("ALARM_STATUS", Integer.valueOf(3)).setNumber("USER_STAFF_ID", (String) ServiceGlobal.getCurrentSession("USER_STAFF_ID")).setDateTime("START_OPER_DATE", new Date());
                listSqlBuilder.add(sqlInsertBuilderRvmAlarmInst);
                hsmpInstanceTask = new HashMap();
                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
                hsmpInstanceTask.put("ALARM_INST_ID", ALARM_INST_ID);
                listInstanceTask.add(hsmpInstanceTask);
            }
            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
            for (i = 0; i < listInstanceTask.size(); i++) {
                RCCInstanceTask.addTask((HashMap) listInstanceTask.get(i));
            }
            hsmpResult.put("RESULT", Boolean.valueOf(true));
        } else {
            hsmpResult.put("RESULT", Boolean.valueOf(true));
        }
        return hsmpResult;
    }

    private HashMap openDoor(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        CommService.getCommService().execute("OPEN_FRONT_DOOR", null);
        HashMap hsmpResult = new HashMap();
        hsmpResult.put("RESULT", Boolean.valueOf(true));
        return hsmpResult;
    }

    private HashMap openPaperDoor(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        CommService.getCommService().execute("OPEN_PAPER_FRONT_DOOR", null);
        HashMap hsmpResult = new HashMap();
        hsmpResult.put("RESULT", Boolean.valueOf(true));
        return hsmpResult;
    }

    private HashMap getOptList(String svcName, String subSvnName, HashMap hsmpParam) {
        String PAGE_ROWS = (String) hsmpParam.get("PAGE_ROWS");
        String PAGE_NO = (String) hsmpParam.get("PAGE_NO");
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getReadableDatabase());
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("OPT_TYPE", "RECYCLE").addNumberIn("OPT_STATUS", Integer.valueOf(1), Integer.valueOf(2));
        String sql = "select * from RVM_OPT" + sqlWhereBuilder.toSqlWhere("where") + " ORDER BY OPT_TIME DESC";
        if (!StringUtils.isBlank(PAGE_NO)) {
            sql = DBQuery.getQuerySQL(sql, RowSet.newPageSet((long) Integer.parseInt(PAGE_NO), (long) Integer.parseInt(PAGE_ROWS)));
        }
        CommTable commTable = dbQuery.getCommTable(sql);
        HashMap hsmpResult = new HashMap();
        hsmpResult.put("RET_LIST", JSONUtils.toList(commTable));
        return hsmpResult;
    }

    private HashMap<String, Object> networkTesting(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        int times = Integer.parseInt((String) hsmpParam.get("TIMES"));
        HashMap hsmpJSON = new HashMap();
        hsmpJSON.put("RCC_IP", SysConfig.get("RCC.IP"));
        hsmpJSON.put("RCC_PORT", SysConfig.get("RCC.PORT"));
        hsmpJSON.put("RVM_CODE", SysConfig.get("RVM.CODE"));
        hsmpJSON.put("RVM_AREA_CODE", SysConfig.get("RVM.AREA.CODE"));
        hsmpJSON.put("TIMEOUT", hsmpParam.get("TIMEOUT"));
        List<HashMap<String, String>> listResult = new ArrayList();
        HashMap hsmpResult = new HashMap();
        for (int i = 0; i < times; i++) {
            listResult.add(JSONUtils.toHashMap(CommService.getCommService().execute("LINK_TESTING", JSONUtils.toJSON(hsmpJSON))));
        }
        hsmpResult.put("LINK_TESTING_LIST", listResult);
        return hsmpResult;
    }

    private HashMap<String, Object> getAlarmList(String svcName, String subSvnName, HashMap hsmpParam) {
        String PAGE_ROWS = (String) hsmpParam.get("PAGE_ROWS");
        String PAGE_NO = (String) hsmpParam.get("PAGE_NO");
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        String sql = "select a.* from RVM_ALARM_INST a " + new SqlWhereBuilder().toSqlWhere("where") + " order by ALARM_TIME DESC ";
        if (!StringUtils.isBlank(PAGE_NO)) {
            sql = DBQuery.getQuerySQL(sql, RowSet.newPageSet((long) Integer.parseInt(PAGE_NO), (long) Integer.parseInt(PAGE_ROWS)));
        }
        CommTable commTable = dbQuery.getCommTable(sql);
        HashMap<String, Object> hsmpResult = new HashMap();
        List<HashMap<String, String>> listRet = JSONUtils.toList(commTable);
        for (int i = 0; i < listRet.size(); i++) {
            HashMap<String, String> hsmpAlarm = (HashMap) listRet.get(i);
            hsmpAlarm.put("HANDLE_ENABLE", "0");
            int alarmId = Integer.parseInt((String) hsmpAlarm.get("ALARM_ID"));
            int a = 0;
            while (a < SysDef.handlingAlarmId.length) {
                if (SysDef.handlingAlarmId[a] == alarmId) {
                    if ("1".equals(hsmpAlarm.get("ALARM_STATUS"))) {
                        hsmpAlarm.put("HANDLE_ENABLE", "1");
                    }
                } else {
                    a++;
                }
            }
        }
        hsmpResult.put("RET_LIST", listRet);
        return hsmpResult;
    }

    private HashMap<String, Object> alarmManualRecovery(String svcName, String subSvnName, HashMap hsmpParam) {
        String ALARM_INST_ID = (String) hsmpParam.get("ALARM_INST_ID");
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderRvmAlarmInst = new SqlWhereBuilder();
        sqlWhereBuilderRvmAlarmInst.addNumberEqualsTo("ALARM_INST_ID", ALARM_INST_ID);
        CommTable commTableRvmAlarmInst = dbQuery.getCommTable("select * from RVM_ALARM_INST" + sqlWhereBuilderRvmAlarmInst.toSqlWhere("where"));
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        List<HashMap<String, String>> listInstanceTask = new ArrayList();
        if (commTableRvmAlarmInst.getRecordCount() != 0) {
            sqlWhereBuilderRvmAlarmInst = new SqlWhereBuilder();
            sqlWhereBuilderRvmAlarmInst.addNumberIn("ALARM_STATUS", Integer.valueOf(1)).addNumberEqualsTo("ALARM_INST_ID", ALARM_INST_ID);
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_ALARM_INST");
            sqlUpdateBuilder.setNumber("ALARM_STATUS", Integer.valueOf(2)).setNumber("UPLOAD_FLAG", "0").setNumber("USER_STAFF_ID", (String) ServiceGlobal.getCurrentSession("USER_STAFF_ID")).setTime("START_OPER_DATE", new Date()).setSqlWhere(sqlWhereBuilderRvmAlarmInst);
            listSqlBuilder.add(sqlUpdateBuilder);
            HashMap<String, String> hsmpInstanceTask = new HashMap();
            hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
            hsmpInstanceTask.put("ALARM_INST_ID", ALARM_INST_ID);
            listInstanceTask.add(hsmpInstanceTask);
        }
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        for (int i = 0; i < listInstanceTask.size(); i++) {
            RCCInstanceTask.addTask((HashMap) listInstanceTask.get(i));
        }
        return null;
    }

    private HashMap startOrStopService(String svcName, String subSvnName, HashMap hsmpParam) {
        int i;
        HashMap hsmpretPkg = hsmpParam;
        String OPT_TYPE = (String) hsmpretPkg.get("OPT_TYPE");
        String RUN_OPT = (String) hsmpretPkg.get("RUN_OPT");
        String serviceName = null;
        for (i = 0; i < SysDef.SERVICE_OF_OPT_TYPE.length; i++) {
            if (SysDef.SERVICE_OF_OPT_TYPE[i][0].equals(OPT_TYPE)) {
                serviceName = SysDef.SERVICE_OF_OPT_TYPE[i][1];
            }
        }
        if (serviceName != null) {
            SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
            DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
            SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
            sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "SERVICE_DISABLED_SET");
            CommTable commTableSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
            String[] SERVICE_DISABLE_SET = null;
            List<String> listDisabledService = new ArrayList();
            if (commTableSysCode.getRecordCount() == 0) {
                SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_SYS_CODE");
                sqlInsertBuilder.newInsertRecord().setString("SYS_CODE_TYPE", "RVM_INFO").setString("SYS_CODE_KEY", "SERVICE_DISABLED_SET").setString("SYS_CODE_VALUE", null);
                SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
            } else {
                String serviceSet = commTableSysCode.getRecord(0).get("SYS_CODE_VALUE");
                if (!StringUtils.isBlank(serviceSet)) {
                    SERVICE_DISABLE_SET = serviceSet.split(",");
                }
            }
            if (SERVICE_DISABLE_SET != null) {
                for (String add : SERVICE_DISABLE_SET) {
                    listDisabledService.add(add);
                }
            }
            String BOTTLES_LIMITED_ENABLE = null;
            if ("0".equals(RUN_OPT)) {
                if (!listDisabledService.contains(serviceName)) {
                    listDisabledService.add(serviceName);
                }
                if ("BOTTLELIMITED".equalsIgnoreCase(serviceName)) {
                    BOTTLES_LIMITED_ENABLE = "FALSE";
                }
            }
            if ("1".equals(RUN_OPT)) {
                if (listDisabledService.contains(serviceName)) {
                    listDisabledService.remove(serviceName);
                }
                if ("BOTTLELIMITED".equalsIgnoreCase(serviceName)) {
                    BOTTLES_LIMITED_ENABLE = "TRUE";
                }
            }
            StringBuffer sb = new StringBuffer();
            for (i = 0; i < listDisabledService.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append((String) listDisabledService.get(i));
            }
            String newServiceSet = sb.toString();
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_SYS_CODE");
            sqlUpdateBuilder.setString("SYS_CODE_VALUE", newServiceSet).setSqlWhere(sqlWhereBuilderRvmSysCode);
            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
            if (!StringUtils.isBlank(BOTTLES_LIMITED_ENABLE)) {
                String BOTTLES_LIMITED = SysConfig.get("BOTTLES.LIMITED");
                String BOTTLES_UNLIMITED = SysConfig.get("BOTTLES.UNLIMITED");
                String BOTTLES_LIMITED_UPDATE = BOTTLES_LIMITED;
                if (!StringUtils.isBlank(BOTTLES_LIMITED_ENABLE)) {
                    SysConfig.set("BOTTLES_LIMITED_ENABLE", BOTTLES_LIMITED_ENABLE);
                    if ("FALSE".equalsIgnoreCase(BOTTLES_LIMITED_ENABLE) && !StringUtils.isBlank(BOTTLES_UNLIMITED)) {
                        BOTTLES_LIMITED_UPDATE = BOTTLES_UNLIMITED;
                    }
                }
                if (!StringUtils.isBlank(BOTTLES_LIMITED_UPDATE)) {
                    HashMap<String, String> updateValues = StringUtils.toHashMap(BOTTLES_LIMITED_UPDATE, ";", "=");
                    for (String KEY : updateValues.keySet()) {
                        SysConfig.set(KEY, (String) updateValues.get(KEY));
                    }
                }
            }
            HashMap hsmpEvent = new HashMap();
            hsmpEvent.put(AllAdvertisement.MEDIA_TYPE, "RVM_CODE_BACKUP");
            RCCInstanceTask.addTask(hsmpEvent);
        }
        return null;
    }

    public HashMap sysInit(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        RVMDaemonClient.init();
        return null;
    }

    public HashMap sysUpdate(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String UPDATE_ENABLE = SysConfig.get("UPDATE_ENABLE");
        if ("TRUE".equalsIgnoreCase(UPDATE_ENABLE)) {
            RVMDaemonClient.update(true);
        }
        if ("FALSE".equalsIgnoreCase(UPDATE_ENABLE)) {
            RVMDaemonClient.update(false);
        }
        return null;
    }

    public HashMap sysExit(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        clearRvmAliveTime(svcName, "clearRvmAliveTime", null);
        return null;
    }

    public HashMap unPowerOff(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        CommService.getCommService().execute("RVM_POWER_OFF_DISABLE", null);
        HashMap hsmpResult = new HashMap();
        hsmpResult.put("RESULT", Boolean.valueOf(true));
        return hsmpResult;
    }

    public HashMap setRvmAliveTime(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if (!StringUtils.isBlank(SysConfig.get("RVM.ALIVE.TIME"))) {
            HashMap hsmpJSON = new HashMap();
            hsmpJSON.put("RVM_ALIVE_TIME", SysConfig.get("RVM.ALIVE.TIME"));
            CommService.getCommService().execute("SET_RVM_ALIVE_TIME", JSONUtils.toJSON(hsmpJSON));
        }
        return null;
    }

    public HashMap clearRvmAliveTime(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if (!StringUtils.isBlank(SysConfig.get("RVM.ALIVE.TIME"))) {
            HashMap hsmpJSON = new HashMap();
            hsmpJSON.put("RVM_ALIVE_TIME", "0");
            CommService.getCommService().execute("CLEAR_RVM_ALIVE_TIME", JSONUtils.toJSON(hsmpJSON));
        }
        return null;
    }

    public HashMap sendHeartBeat(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if (!StringUtils.isBlank(SysConfig.get("RVM.ALIVE.TIME"))) {
            CommService.getCommService().execute("HEART_BEAT", null);
        }
        return null;
    }

    public HashMap setHeartBeat(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if (hsmpParam != null) {
            String ENABLE = (String) hsmpParam.get("ENABLE");
            Properties prop = new Properties();
            if ("TRUE".equals(ENABLE)) {
                prop.put("RVM.ALIVE.TIME.ENABLE", "TRUE");
                PropUtils.update(SysConfig.get("EXTERNAL.FILE"), prop);
                SysConfig.set(prop);
                RVMShell.backupExternalConfig();
                setRvmAliveTime(svcName, "setRvmAliveTime", null);
            }
            if ("FALSE".equals(ENABLE)) {
                prop.put("RVM.ALIVE.TIME.ENABLE", "FALSE");
                PropUtils.update(SysConfig.get("EXTERNAL.FILE"), prop);
                SysConfig.set(prop);
                RVMShell.backupExternalConfig();
                setRvmAliveTime(svcName, "clearRvmAliveTime", null);
            }
        }
        return null;
    }

    public HashMap checkSystemStatus(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String SYSTEM_STATUS_SHELL = SysConfig.get("SYSTEM_STATUS.SHELL");
        if (!StringUtils.isBlank(SYSTEM_STATUS_SHELL)) {
            String syslog = ShellUtils.execScript(new File(SYSTEM_STATUS_SHELL));
            if (syslog != null) {
                logger.debug("Sysem Status:\n" + syslog);
            }
        }
        return null;
    }

    public HashMap checkTcpAlarm(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String TCP_ALARM_LIMIT_SET = SysConfig.get("TCP.ALARM.LIMIT.SET");
        String TCP_SUMMARY_FILE = SysConfig.get("TCP_SUMMARY.FILE");
        String TCP_TOTAL = null;
        if (StringUtils.isBlank(TCP_SUMMARY_FILE) || StringUtils.isBlank(TCP_ALARM_LIMIT_SET)) {
            return null;
        }
        byte[] data = IOUtils.readFile(TCP_SUMMARY_FILE + "." + DateUtils.formatDatetime(new Date(), "yyyyMM"));
        if (data != null) {
            String text = new String(data).trim();
            String TCP_FLOW_SIZE_HEAD = "TCP_FLOW_SIZE:";
            if (text.length() <= TCP_FLOW_SIZE_HEAD.length()) {
                return null;
            }
            TCP_TOTAL = text.substring(TCP_FLOW_SIZE_HEAD.length());
        }
        if (!StringUtils.isBlank(TCP_TOTAL)) {
            String THE_MONTH = DateUtils.formatDatetime(new Date(), "yyyyMM");
            boolean isClearSummaryEnable = false;
            String TCP_SUMMARY_MONTH = SysConfig.get("TCP_SUMMARY_MONTH");
            SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
            DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
            SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
            sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "TCP_SUMMARY_MONTH");
            if (StringUtils.isBlank(TCP_SUMMARY_MONTH)) {
                CommTable commTableSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
                List<String> listDisabledService = new ArrayList();
                if (commTableSysCode.getRecordCount() == 0) {
                    TCP_SUMMARY_MONTH = THE_MONTH;
                    SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_SYS_CODE");
                    sqlInsertBuilder.newInsertRecord().setString("SYS_CODE_TYPE", "RVM_INFO").setString("SYS_CODE_KEY", "TCP_SUMMARY_MONTH").setString("SYS_CODE_VALUE", TCP_SUMMARY_MONTH);
                    SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
                    isClearSummaryEnable = true;
                } else {
                    TCP_SUMMARY_MONTH = commTableSysCode.getRecord(0).get("SYS_CODE_VALUE");
                }
                SysConfig.set("TCP_SUMMARY_MONTH", TCP_SUMMARY_MONTH);
            }
            if (Integer.parseInt(THE_MONTH) > Integer.parseInt(TCP_SUMMARY_MONTH)) {
                isClearSummaryEnable = true;
                SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_SYS_CODE");
                sqlUpdateBuilder.setString("SYS_CODE_VALUE", THE_MONTH).setSqlWhere(sqlWhereBuilderRvmSysCode);
                SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                SysConfig.set("TCP_SUMMARY_MONTH", THE_MONTH);
            }
            networkSts.tcpTotal = Long.parseLong(TCP_TOTAL);
            String[] TCP_BYTES_LIMIT_LIST;
            int i;
            String[] TCP_BYTES_LIMIT;
            if (isClearSummaryEnable) {
                listRaisedTcpAlarm.clear();
                TCP_BYTES_LIMIT_LIST = TCP_ALARM_LIMIT_SET.split(";");
                for (i = 0; i < TCP_BYTES_LIMIT_LIST.length; i++) {
                    TCP_BYTES_LIMIT_LIST[i] = TCP_BYTES_LIMIT_LIST[i].trim();
                    if (TCP_BYTES_LIMIT_LIST[i].length() != 0) {
                        TCP_BYTES_LIMIT = TCP_BYTES_LIMIT_LIST[i].split(":");
                        if (TCP_BYTES_LIMIT.length >= 2) {
                            TCP_BYTES_LIMIT[0] = TCP_BYTES_LIMIT[0].trim();
                            TCP_BYTES_LIMIT[1] = TCP_BYTES_LIMIT[1].trim();
                            if (!(TCP_BYTES_LIMIT[0].length() == 0 || TCP_BYTES_LIMIT[1].length() == 0)) {
                                recoverAlarm(TCP_BYTES_LIMIT[0], null);
                            }
                        }
                    }
                }
                return null;
            }
            SqlWhereBuilder sqlWhereBuilderRvmAlarm = new SqlWhereBuilder();
            sqlWhereBuilderRvmAlarm.addNumberIn("ALARM_ID", (Object[]) new Object[]{Integer.valueOf(AlarmId.TCP_ALARM_YELLOW), Integer.valueOf(AlarmId.TCP_ALARM_RED)});
            CommTable commTableRvmAlarm = dbQuery.getCommTable("select * from RVM_ALARM" + sqlWhereBuilderRvmAlarm.toSqlWhere("where"));
            String TCP_ALARM_LIMIT_SET_IN_DB = "";
            for (i = 0; i < commTableRvmAlarm.getRecordCount(); i++) {
                CommTableRecord ctr = commTableRvmAlarm.getRecord(i);
                String ALARM_ID = ctr.get("ALARM_ID");
                String TSD_VALUE = ctr.get("TSD_VALUE");
                if (!(StringUtils.isBlank(ALARM_ID) || StringUtils.isBlank(TSD_VALUE))) {
                    TCP_ALARM_LIMIT_SET_IN_DB = TCP_ALARM_LIMIT_SET_IN_DB + ALARM_ID + ":" + Long.toString(Double.valueOf(Double.parseDouble(TSD_VALUE)).longValue()) + ";";
                }
            }
            if (!StringUtils.isBlank(TCP_ALARM_LIMIT_SET_IN_DB)) {
                TCP_ALARM_LIMIT_SET = TCP_ALARM_LIMIT_SET_IN_DB;
            }
            TCP_BYTES_LIMIT_LIST = TCP_ALARM_LIMIT_SET.split(";");
            for (i = 0; i < TCP_BYTES_LIMIT_LIST.length; i++) {
                TCP_BYTES_LIMIT_LIST[i] = TCP_BYTES_LIMIT_LIST[i].trim();
                if (TCP_BYTES_LIMIT_LIST[i].length() != 0) {
                    TCP_BYTES_LIMIT = TCP_BYTES_LIMIT_LIST[i].split(":");
                    if (TCP_BYTES_LIMIT.length >= 2) {
                        TCP_BYTES_LIMIT[0] = TCP_BYTES_LIMIT[0].trim();
                        TCP_BYTES_LIMIT[1] = TCP_BYTES_LIMIT[1].trim();
                        if (!(TCP_BYTES_LIMIT[0].length() == 0 || TCP_BYTES_LIMIT[1].length() == 0 || Long.parseLong(TCP_BYTES_LIMIT[1]) > networkSts.tcpTotal || listRaisedTcpAlarm.contains(TCP_BYTES_LIMIT[0]))) {
                            listRaisedTcpAlarm.add(TCP_BYTES_LIMIT[0]);
                            raiseAlarm(TCP_BYTES_LIMIT[0], null);
                        }
                    }
                }
            }
        }
        return null;
    }

    public HashMap backupRvmCode(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpEvent = new HashMap();
        hsmpEvent.put(AllAdvertisement.MEDIA_TYPE, "RVM_CODE_BACKUP");
        RCCInstanceTask.addTask(hsmpEvent);
        return null;
    }

    public HashMap queryCanTakePhoto(String svcName, String subSvnName, HashMap hsmpParam) {
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", ServiceName.TAKE_PHOTO);
        String canTakePhoto = "FALSE";
        CommTable commTable = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() > 0) {
            if ("TRUE".equalsIgnoreCase(commTable.getRecord(0).get("SYS_CODE_VALUE"))) {
                canTakePhoto = "TRUE";
            }
        }
        HashMap hsmp = new HashMap();
        hsmp.put("TAKEPHOTO", canTakePhoto);
        return hsmp;
    }

    public HashMap saveTakePhotoEnable(String svcName, String subSvnName, HashMap hsmpParam) {
        if (!(hsmpParam == null || hsmpParam.size() == 0)) {
            SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
            DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
            SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", ServiceName.TAKE_PHOTO);
            String canTakePhoto = (String) hsmpParam.get("TAKEPHOTO_ENABLE");
            if ("TRUE".equalsIgnoreCase(canTakePhoto) || "FALSE".equalsIgnoreCase(canTakePhoto)) {
                if (dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() > 0) {
                    SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_SYS_CODE");
                    sqlUpdateBuilder.setString("SYS_CODE_VALUE", canTakePhoto).setSqlWhere(sqlWhereBuilder);
                    sqliteDatabase.execSQL(sqlUpdateBuilder.toSql());
                } else {
                    SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_SYS_CODE");
                    sqlInsertBuilder.newInsertRecord().setString("SYS_CODE_TYPE", "RVM_INFO").setString("SYS_CODE_KEY", ServiceName.TAKE_PHOTO).setString("SYS_CODE_VALUE", canTakePhoto);
                    sqliteDatabase.execSQL(sqlInsertBuilder.toSql());
                }
            }
        }
        return null;
    }

    public HashMap saveStopRecycleTime(String svcName, String subSvnName, HashMap hsmpParam) {
        HashMap hashmap = new HashMap();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "SLEEP_TIME");
        if (dbQuery.getCommTable("select * from RVM_SYS_CODE " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() != 0) {
            SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_SYS_CODE");
            sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
            SQLiteExecutor.execSql(sqliteDatabase, sqlDeleteBuilder.toSql());
        }
        SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_SYS_CODE");
        sqlInsertBuilder.newInsertRecord().setString("SYS_CODE_TYPE", "RVM_INFO").setString("SYS_CODE_KEY", "SLEEP_TIME").setString("SYS_CODE_VALUE", JSONUtils.toJSON((List) hsmpParam.get("list")));
        SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
        if (dbQuery.getCommTable("select * from RVM_SYS_CODE " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() > 0) {
            hashmap.put("result", "success");
        } else {
            hashmap.put("result", "failed");
        }
        return hashmap;
    }

    public HashMap getStopRecycleTime(String svcName, String subSvnName, HashMap hsmpParam) {
        HashMap hashmap = new HashMap();
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "SLEEP_TIME");
        CommTable commTable1 = dbQuery.getCommTable("select * from RVM_SYS_CODE " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable1.getRecordCount() > 0) {
            try {
                hashmap.put("result", commTable1.getRecord(0).get("SYS_CODE_VALUE"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            hashmap.put("result", "failed");
        }
        return hashmap;
    }

    public HashMap getTrafficRecord(String svcName, String subSvnName, HashMap hsmpParam) {
        if (hsmpParam != null) {
            String trafficMonth = (String) hsmpParam.get("TRAFFIC_MONTH");
            HashMap hashmap = new HashMap();
            DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
            SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addStringIn("SYS_CODE_TYPE", new String[]{TrafficType.RCC, TrafficType.TRAFFICCARD, TrafficType.BARCODE, "PICTURE"}).addStringEqualsTo("SYS_CODE_KEY", trafficMonth);
            CommTable commTable1 = dbQuery.getCommTable("select * from RVM_SYS_CODE " + sqlWhereBuilder.toSqlWhere("where"));
            if (commTable1.getRecordCount() > 0) {
                HashMap dataMap = new HashMap();
                for (int i = 0; i < commTable1.getRecordCount(); i++) {
                    dataMap.put(commTable1.getRecord(i).get("SYS_CODE_TYPE"), commTable1.getRecord(i).get("SYS_CODE_VALUE"));
                }
                hashmap.put("TRAFFIC_DATA", dataMap);
                return hashmap;
            }
        }
        return null;
    }

    public HashMap saveTrafficRecord(String svcName, String subSvnName, HashMap<String, Object> hsmpParam) {
        if (hsmpParam != null) {
            SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
            DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
            String trafficMonth = (String) hsmpParam.get("TRAFFIC_MONTH");
            for (Entry entry : ((HashMap<String, String>) hsmpParam.get("TRAFFIC_DATA")).entrySet()) {
                String trafficType = (String) entry.getKey();
                String trafficVal = String.valueOf(entry.getValue());
                SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
                sqlWhereBuilder.addStringEqualsTo("SYS_CODE_TYPE", trafficType).addStringEqualsTo("SYS_CODE_KEY", trafficMonth);
                if (dbQuery.getCommTable("select * from RVM_SYS_CODE " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() > 0) {
                    SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_SYS_CODE");
                    sqlUpdateBuilder.setString("SYS_CODE_VALUE", trafficVal).setSqlWhere(sqlWhereBuilder);
                    SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                } else {
                    SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_SYS_CODE");
                    sqlInsertBuilder.newInsertRecord().setString("SYS_CODE_TYPE", trafficType).setString("SYS_CODE_KEY", trafficMonth).setString("SYS_CODE_VALUE", trafficVal);
                    SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
                }
            }
        }
        return null;
    }

    public HashMap clearTrafficRecord(String svcName, String subSvnName, HashMap hsmpParam) {
        if (hsmpParam != null) {
            String trafficMonth = (String) hsmpParam.get("TRAFFIC_MONTH");
            HashMap hashmap = new HashMap();
            SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
            DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
            SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addStringIn("SYS_CODE_TYPE", new String[]{TrafficType.RCC, TrafficType.TRAFFICCARD, TrafficType.BARCODE, "PICTURE"}).addStringEqualsTo("SYS_CODE_KEY", trafficMonth);
            if (dbQuery.getCommTable("select * from RVM_SYS_CODE " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() > 0) {
                SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_SYS_CODE");
                sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
                SQLiteExecutor.execSql(sqliteDatabase, sqlDeleteBuilder.toSql());
            }
        }
        return null;
    }
}
