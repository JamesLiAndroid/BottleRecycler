package com.incomrecycle.prms.rvm.service.commonservice;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.commtable.CommTableRecord;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.sqlite.DBQuery;
import com.incomrecycle.common.sqlite.DBSequence;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlBuilder;
import com.incomrecycle.common.sqlite.SqlDeleteBuilder;
import com.incomrecycle.common.sqlite.SqlInsertBuilder;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.ShellUtils;
import com.incomrecycle.common.utils.SocketUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.RVMShell;
import com.incomrecycle.prms.rvm.common.SysDef;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.CardType;
import com.incomrecycle.prms.rvm.common.SysDef.ConvenienceService;
import com.incomrecycle.prms.rvm.common.SysDef.MediaInfo;
import com.incomrecycle.prms.rvm.common.SysDef.MsgType;
import com.incomrecycle.prms.rvm.common.SysDef.ProductType;
import com.incomrecycle.prms.rvm.common.SysDef.ServiceName;
import com.incomrecycle.prms.rvm.common.SysDef.networkSts;
import com.incomrecycle.prms.rvm.common.SysDef.updateDetection;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import com.incomrecycle.prms.rvm.service.task.action.RCCInstanceTask;
import com.incomrecycle.prms.rvm.service.traffic.TrafficEntity;
import java.io.File;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

public class RCCTaskCommonService implements AppCommonService {
    private static final HashMap TASK_REQUEST = new HashMap();
    private static boolean isTaskResponse = false;
    private HashMap hsmpretPkg = null;

    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        long lInterval = Long.parseLong(SysConfig.get("RCC.TASKREQUEST.INTERVAL"));
        while (TASK_REQUEST == RCCTaskRequest(svcName, subSvnName, hsmpParam)) {
            Thread.sleep(lInterval);
        }
        if (isTaskResponse) {
            HashMap hsmp = new HashMap();
            hsmp.put("UPDATE_DETECTION", "RVM_REQUEST");
            CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "updateDetection", hsmp);
        }
        RCCTaskAlarm(null, null, null);
        RCCTaskSummary(null, null, null);
        RCCTaskRVMRecycleDetail(null, null, null);
        RCCTaskRVMBarCodeUnknown(null, null, null);
        RCCTaskCouponNotEnough(null, null, null);
        RCCTaskCouponPrinter(null, null, null);
        RCCTaskCouponCancel(null, null, null);
        RCCTaskTransportCardSts(null, null, null);
        RCCTaskWorkerSignInUpload(null, null, null);
        RCCTaskYoukuMovieTicketUpload(null, null, null);
        RCCTaskMaintainerOptInfo(null, null, null);
        RCCTaskRechargeFeedback(null, null, null);
        RCCTaskBMchargeFeedback(null, null, null);
        RCCInstanceTaskGGKFeedback(null, null, null);
        return null;
    }

    public HashMap RCCTaskAllCount(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        int today_sum_time = Integer.parseInt(DateUtils.formatDatetime(new Date(), "yyyyMMdd"));
        String UP_TIME = DateUtils.formatDatetime(new Date(), "yyyy-MM-dd HH:mm:ss");
        List<SqlBuilder> list = new ArrayList();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("UPLOAD_FLAG", Integer.valueOf(0)).addNumberLessTo("SUM_TIME", Integer.valueOf(today_sum_time));
        CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_DATE_COUNT " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            HashMap hsmpMsg = new HashMap();
            hsmpMsg.put("MES_TYPE", MsgType.RVM_UICLICK_SUM);
            hsmpMsg.put("TERM_NO", SysConfig.get("RVM.CODE"));
            hsmpMsg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                String SUM_TIME = commTable.getRecord(i).get("SUM_TIME");
                String OP_BATCH_ID = commTable.getRecord(i).get("OP_BATCH_ID");
                HashMap hsmpClickSum = StringUtils.toHashMap(commTable.getRecord(i).get("CLICK_SUM"), ";", "=");
                hsmpMsg.put("OP_BATCH_ID", OP_BATCH_ID);
                hsmpMsg.put("CLICK_SUM", hsmpClickSum);
                hsmpMsg.put("SUM_TIME", SUM_TIME);
                this.hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpMsg)));
                if (this.hsmpretPkg == null) {
                    return null;
                }
                String termNo = (String) this.hsmpretPkg.get("TERM_NO");
                if ("RESPONSE".equalsIgnoreCase((String) this.hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo) && !StringUtils.isBlank((String) this.hsmpretPkg.get("CONFIRM"))) {
                    try {
                        SqlWhereBuilder sqlWhereBuilder2;
                        SqlUpdateBuilder sqlUpdateBuilder;
                        Integer Confirm = Integer.valueOf(Integer.parseInt((String) this.hsmpretPkg.get("CONFIRM")));
                        if (Confirm.intValue() == 1) {
                            sqlWhereBuilder2 = new SqlWhereBuilder();
                            sqlWhereBuilder2.addNumberEqualsTo("SUM_TIME", SUM_TIME);
                            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_DATE_COUNT");
                            sqlUpdateBuilder.setNumber("UPLOAD_FLAG", Integer.valueOf(1)).setString("UP_TIME", UP_TIME).setSqlWhere(sqlWhereBuilder2);
                            list.add(sqlUpdateBuilder);
                            SQLiteExecutor.execSqlBuilder(sqliteDatabase, list);
                        }
                        if (Confirm.intValue() == 0) {
                            sqlWhereBuilder2 = new SqlWhereBuilder();
                            sqlWhereBuilder2.addNumberEqualsTo("SUM_TIME", SUM_TIME);
                            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_DATE_COUNT");
                            sqlUpdateBuilder.setNumber("UPLOAD_FLAG", Integer.valueOf(-1)).setString("UP_TIME", UP_TIME).setSqlWhere(sqlWhereBuilder2);
                            list.add(sqlUpdateBuilder);
                            SQLiteExecutor.execSqlBuilder(sqliteDatabase, list);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private HashMap RCCTaskRequest(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String ipAddress = SocketUtils.getIpAddress();
        String version = SysConfig.get("RVM_PRICE_VERSION");
        String localAreaId = SysConfig.get("RVM.AREA.CODE");
        Long time = Long.valueOf(new Date().getTime());
        String opBatchID = SysConfig.get("RVM.CODE") + "_" + time;
        HashMap hsmpMsg = new HashMap();
        hsmpMsg.put("MES_TYPE", "RVM_REQUEST");
        hsmpMsg.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpMsg.put("QU_TIME", time.toString());
        hsmpMsg.put("IP_ADDR", ipAddress);
        hsmpMsg.put("OP_BATCH_ID", opBatchID);
        hsmpMsg.put("ATTR_VERSION", version);
        hsmpMsg.put("LOCAL_AREA_ID", localAreaId);
        hsmpMsg.put("RVM_VERSION_ID", SysConfig.get("RVM.VERSION.ID"));
        if (networkSts.tcpTotal > 0) {
            hsmpMsg.put("TCP_TOTAL", "" + networkSts.tcpTotal);
        }
        Long flowTotal = Long.valueOf(0);
        HashMap<String, String> trafficMap = TrafficEntity.getData();
        if (trafficMap != null && trafficMap.size() > 0) {
            for (Entry entry : trafficMap.entrySet()) {
                String trafficType = (String) entry.getKey();
                Long flows = Long.valueOf(Long.parseLong(String.valueOf(entry.getValue())));
                flowTotal = Long.valueOf(flowTotal.longValue() + flows.longValue());
                hsmpMsg.put(trafficType, String.valueOf(flows.longValue() / 1024) + "KB");
            }
        }
        hsmpMsg.put("FLOW_TOTAL", String.valueOf(flowTotal.longValue() / 1024) + "KB");
        HashMap<String, String> haredwareMap = PLCAnormalMonitorUtils.getData();
        if (haredwareMap != null && haredwareMap.size() > 0) {
            for (Entry entry2 : haredwareMap.entrySet()) {
                hsmpMsg.put((String) entry2.getKey(), (String) entry2.getValue());
            }
        }
        hsmpMsg.put("LIGHT_LOST_SUM", PLCAnormalMonitorUtils.getDataCount());
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
        putdownWeight = new DecimalFormat("0.00").format(Double.parseDouble((String) guiCommonService.execute("GUIQueryCommonService", "queryStorageWeight", null).get("STORAGE_CURR_PAPER_WEIGH")));
        hsmpMsg.put("BOTTLE_NOW", putdownCountReal);
        hsmpMsg.put("BOTTLE_ACCOUNT", putdownCount);
        hsmpMsg.put("PAPER_NOW", putdownWeight);
        this.hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpMsg)));
        if (this.hsmpretPkg == null) {
            isTaskResponse = false;
            return null;
        }
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e2) {
        }
        if (nextTag != 1) {
            isTaskResponse = true;
        }
        String msgType = (String) this.hsmpretPkg.get("MES_TYPE");
        String termNo = (String) this.hsmpretPkg.get("TERM_NO");
        if (MsgType.BARCODE_ISSUED.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return BarCodeIssued();
        }
        if (MsgType.COUPONS_ISSUED_ACTIVITIES.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return CouponsIssuedActivities();
        }
        if ("TASK_COUPON".equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return CouponsIssued();
        }
        if (MsgType.WARNING_ISSUED.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return WarningIssued();
        }
        if (MsgType.PATROL_BARCODE_ISSUED.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return PatrolBarcodeIssued();
        }
        if (MsgType.TERMINAL_SERVICES_COMMITMENT_ISSUED.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return StartOrStopIssued();
        }
        if (MsgType.SCROLLBAR_ISSUED.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return ScrollbarIssued();
        }
        if (MsgType.PICTURES_AUDIO_ISSUED.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return PicturesAudioIssued();
        }
        if (MsgType.TASK_VOLUME.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return TaskVolMoney();
        }
        if (MsgType.TASK_BLACK_BAR.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return TaskBlackBar();
        }
        if (MsgType.TASK_UD_TIME.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return TaskRVMStartOrShutdown();
        }
        if (MsgType.TASK_ALARM_BOTTEL.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return taskVOLConvertion();
        }
        if (MsgType.RCC_LOCALID.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return updateLocalId();
        }
        if (MsgType.RCC_RES_PARAM.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return TaskRccResParam();
        }
        if (MsgType.TASK_RVM_SLEEP.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
            return TaskRVMSleep();
        }
        return null;
    }

    private HashMap updateLocalId() throws Exception {
        String localAreaId = (String) this.hsmpretPkg.get("LOCAL_AREA_ID");
        if (!(localAreaId == null || "".equals(localAreaId) || localAreaId.equalsIgnoreCase(SysConfig.get("RVM.AREA.CODE")))) {
            Properties prop = new Properties();
            prop.setProperty("RVM.AREA.CODE", localAreaId);
            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), prop);
            SysConfig.set(prop);
            RVMShell.backupExternalConfig();
        }
        return TASK_REQUEST;
    }

    public HashMap BarCodeIssued() throws Exception {
        HashMap<String, String> mapBars = (HashMap) this.hsmpretPkg.get("BAR_CODE-ATTR_ID");
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBSequence dbSequence = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS"));
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        HashMap<String, String> hsmpBarcode = new HashMap();
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        for (String barCode : mapBars.keySet()) {
            CommTableRecord ctr;
            String[] property = ((String) mapBars.get(barCode)).split(";");
            String vol = property[0];
            String stuff = property[1];
            String weigh = property[2];
            String color = property[3];
            SqlWhereBuilder sqlWhereBuilderBarCode = new SqlWhereBuilder();
            sqlWhereBuilderBarCode.addStringEqualsTo("BAR_CODE", barCode);
            CommTable commTableBarCode = dbQuery.getCommTable("select * from RVM_BAR_CODE " + sqlWhereBuilderBarCode.toSqlWhere("where"));
            boolean hasBarcode = false;
            boolean isValidBarcode = false;
            if (commTableBarCode.getRecordCount() > 0) {
                ctr = commTableBarCode.getRecord(0);
                hasBarcode = true;
                if (2 != Integer.parseInt(ctr.get("BAR_CODE_FLAG")) && Double.parseDouble(ctr.get("BAR_CODE_VOL")) == Double.parseDouble(vol) && ctr.get("BAR_CODE_STUFF").equals(stuff) && ctr.get("BAR_CODE_COLOR").equals(color) && Double.parseDouble(ctr.get("BAR_CODE_WEIGH")) == Double.parseDouble(weigh)) {
                    isValidBarcode = true;
                }
            }
            if (commTableBarCode.getRecordCount() > 0) {
                ctr = commTableBarCode.getRecord(0);
                hsmpBarcode.put(ctr.get("BAR_CODE"), ctr.get("BAR_CODE"));
            }
            if (!StringUtils.isBlank((String) hsmpBarcode.get(barCode))) {
                hasBarcode = true;
            }
            if (!isValidBarcode) {
                if (hasBarcode) {
                    SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_BAR_CODE");
                    sqlUpdateBuilder.setNumber("BAR_CODE_VOL", vol).setNumber("BAR_CODE_FLAG", Integer.valueOf(1)).setNumber("BAR_CODE_WEIGH", weigh).setString("BAR_CODE_COLOR", color).setString("BAR_CODE_STUFF", stuff).setSqlWhere(sqlWhereBuilderBarCode);
                    listSqlBuilder.add(sqlUpdateBuilder);
                } else {
                    SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_BAR_CODE");
                    sqlInsertBuilder.newInsertRecord().setString("BAR_CODE", barCode).setNumber("BAR_CODE_VOL", vol).setNumber("BAR_CODE_FLAG", Integer.valueOf(1)).setNumber("BAR_CODE_WEIGH", weigh).setString("BAR_CODE_COLOR", color).setString("BAR_CODE_STUFF", stuff).setNumber("BAR_CODE_ATTR_ID", Integer.valueOf(0));
                    listSqlBuilder.add(sqlInsertBuilder);
                }
                hsmpBarcode.put(barCode, barCode);
                SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_BAR_CODE_UNKNOWN");
                sqlDeleteBuilder.setSqlWhere(sqlWhereBuilderBarCode);
                listSqlBuilder.add(sqlDeleteBuilder);
            }
        }
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    public HashMap CouponsIssuedActivities() throws Exception {
        String actID = (String) this.hsmpretPkg.get("ACTIVITY_ID");
        Integer activityId = null;
        Integer delActivityId = null;
        if (actID != null) {
            try {
                if (!"".equals(actID)) {
                    activityId = Integer.valueOf(Integer.parseInt(actID));
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        String str = (String) this.hsmpretPkg.get("DEL_ACTIVITY_ID");
        delActivityId = null;
        if (!(str == null || "".equals(str))) {
            delActivityId = Integer.valueOf(Integer.parseInt(str));
        }
        String activityType = (String) this.hsmpretPkg.get("ACTIVITY_TYPE");
        String printRule = (String) this.hsmpretPkg.get("PRINT_RULE");
        String notEnough = (String) this.hsmpretPkg.get("NOT_ENOUGH");
        String printInfo = (String) this.hsmpretPkg.get("PRINT_INFO");
        String beginTime = (String) this.hsmpretPkg.get("BEGIN_TIME");
        String endTime = (String) this.hsmpretPkg.get("END_TIME");
        String picInfo = (String) this.hsmpretPkg.get("PIC_INFO");
        String activityName = (String) this.hsmpretPkg.get("ACTIVITY_NAME");
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder;
        SqlInsertBuilder sqlInsertBuilder;
        SqlUpdateBuilder sqlUpdateBuilder;
        if (delActivityId != null) {
            sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addNumberEqualsTo("ACTIVITY_ID", delActivityId);
            SqlWhereBuilder sqlWhereBuilder2;
            if (dbQuery.getCommTable("select * from RVM_ACTIVITY " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() != 0) {
                List<SqlBuilder> listSqlBuilder = new ArrayList();
                SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_ACTIVITY");
                sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
                listSqlBuilder.add(sqlDeleteBuilder);
                if (activityId != null) {
                    sqlWhereBuilder2 = new SqlWhereBuilder();
                    sqlWhereBuilder2.addNumberEqualsTo("ACTIVITY_ID", activityId);
                    if (dbQuery.getCommTable("select * from RVM_ACTIVITY " + sqlWhereBuilder2.toSqlWhere("where")).getRecordCount() == 0) {
                        sqlInsertBuilder = new SqlInsertBuilder("RVM_ACTIVITY");
                        sqlInsertBuilder.newInsertRecord().setNumber("ACTIVITY_ID", activityId).setNumber("ACTIVITY_TYPE", activityType).setNumber("PRINT_RULE", printRule).setNumber("MIN_BOTTLE_COUNT", Integer.valueOf(1)).setNumber("NOT_ENOUGH", notEnough).setNumber("APPLY_COUNT", Integer.valueOf(10)).setString("APPLY_TIME", null).setString("PRINT_INFO", printInfo).setString("BEGIN_TIME", beginTime).setString("END_TIME", endTime).setString("PIC_INFO", picInfo).setString("PIC_PATH", picInfo).setString("ACTIVITY_NAME", activityName).setNumber("ACTIVITY_STATUS", Integer.valueOf(1));
                        listSqlBuilder.add(sqlInsertBuilder);
                    } else {
                        sqlUpdateBuilder = new SqlUpdateBuilder("RVM_ACTIVITY");
                        sqlUpdateBuilder.setNumber("ACTIVITY_TYPE", activityType).setNumber("PRINT_RULE", printRule).setNumber("MIN_BOTTLE_COUNT", Integer.valueOf(1)).setNumber("NOT_ENOUGH", notEnough).setNumber("APPLY_COUNT", Integer.valueOf(10)).setString("APPLY_TIME", null).setString("PRINT_INFO", printInfo).setString("BEGIN_TIME", beginTime).setString("END_TIME", endTime).setString("PIC_INFO", picInfo).setString("PIC_PATH", picInfo).setString("ACTIVITY_NAME", activityName).setNumber("ACTIVITY_STATUS", Integer.valueOf(1)).setSqlWhere(sqlWhereBuilder2);
                        listSqlBuilder.add(sqlUpdateBuilder);
                    }
                }
                SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
            } else if (activityId != null) {
                sqlWhereBuilder2 = new SqlWhereBuilder();
                sqlWhereBuilder2.addNumberEqualsTo("ACTIVITY_ID", activityId);
                if (dbQuery.getCommTable("select * from RVM_ACTIVITY " + sqlWhereBuilder2.toSqlWhere("where")).getRecordCount() == 0) {
                    sqlInsertBuilder = new SqlInsertBuilder("RVM_ACTIVITY");
                    sqlInsertBuilder.newInsertRecord().setNumber("ACTIVITY_ID", activityId).setNumber("ACTIVITY_TYPE", activityType).setNumber("PRINT_RULE", printRule).setNumber("MIN_BOTTLE_COUNT", Integer.valueOf(1)).setNumber("NOT_ENOUGH", notEnough).setNumber("APPLY_COUNT", Integer.valueOf(10)).setString("APPLY_TIME", null).setString("PRINT_INFO", printInfo).setString("BEGIN_TIME", beginTime).setString("END_TIME", endTime).setString("PIC_INFO", picInfo).setString("PIC_PATH", picInfo).setString("ACTIVITY_NAME", activityName).setNumber("ACTIVITY_STATUS", Integer.valueOf(1));
                    SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
                } else {
                    sqlUpdateBuilder = new SqlUpdateBuilder("RVM_ACTIVITY");
                    sqlUpdateBuilder.setNumber("ACTIVITY_TYPE", activityType).setNumber("PRINT_RULE", printRule).setNumber("MIN_BOTTLE_COUNT", Integer.valueOf(1)).setNumber("NOT_ENOUGH", notEnough).setNumber("APPLY_COUNT", Integer.valueOf(10)).setString("APPLY_TIME", null).setString("PRINT_INFO", printInfo).setString("BEGIN_TIME", beginTime).setString("END_TIME", endTime).setString("PIC_INFO", picInfo).setString("PIC_PATH", picInfo).setString("ACTIVITY_NAME", activityName).setNumber("ACTIVITY_STATUS", Integer.valueOf(1)).setSqlWhere(sqlWhereBuilder2);
                    SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                }
            }
        } else if (activityId != null) {
            sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addNumberEqualsTo("ACTIVITY_ID", activityId);
            if (dbQuery.getCommTable("select * from RVM_ACTIVITY " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
                sqlInsertBuilder = new SqlInsertBuilder("RVM_ACTIVITY");
                sqlInsertBuilder.newInsertRecord().setNumber("ACTIVITY_ID", activityId).setNumber("ACTIVITY_TYPE", activityType).setNumber("PRINT_RULE", printRule).setNumber("MIN_BOTTLE_COUNT", Integer.valueOf(1)).setNumber("NOT_ENOUGH", notEnough).setNumber("APPLY_COUNT", Integer.valueOf(10)).setString("APPLY_TIME", null).setString("PRINT_INFO", printInfo).setString("BEGIN_TIME", beginTime).setString("END_TIME", endTime).setString("PIC_INFO", picInfo).setString("PIC_PATH", picInfo).setString("ACTIVITY_NAME", activityName).setNumber("ACTIVITY_STATUS", Integer.valueOf(1));
                SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
            } else {
                sqlUpdateBuilder = new SqlUpdateBuilder("RVM_ACTIVITY");
                sqlUpdateBuilder.setNumber("ACTIVITY_TYPE", activityType).setNumber("PRINT_RULE", printRule).setNumber("MIN_BOTTLE_COUNT", Integer.valueOf(1)).setNumber("NOT_ENOUGH", notEnough).setNumber("APPLY_COUNT", Integer.valueOf(10)).setString("APPLY_TIME", null).setString("PRINT_INFO", printInfo).setString("BEGIN_TIME", beginTime).setString("END_TIME", endTime).setString("PIC_INFO", picInfo).setString("PIC_PATH", picInfo).setString("ACTIVITY_NAME", activityName).setNumber("ACTIVITY_STATUS", Integer.valueOf(1)).setSqlWhere(sqlWhereBuilder);
                SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
            }
        }
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    public HashMap CouponsIssued() throws Exception {
        int activityId = Integer.parseInt((String) this.hsmpretPkg.get("ACTIVITY_ID"));
        List listCouponNo = (List) this.hsmpretPkg.get("COUPON_NO");
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        for (int i = 0; i < listCouponNo.size(); i++) {
            SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addStringEqualsTo("VOUCHER_CODE", (String) listCouponNo.get(i));
            if (dbQuery.getCommTable("select * from RVM_ACTIVITY_VOUCHER " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
                SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_ACTIVITY_VOUCHER");
                sqlInsertBuilder.newInsertRecord().setNumber("ACTIVITY_ID", Integer.valueOf(activityId)).setString("VOUCHER_CODE", listCouponNo.get(i)).setString("USED_TIME", null);
                listSqlBuilder.add(sqlInsertBuilder);
            }
        }
        if (listSqlBuilder.size() > 0) {
            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        }
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    public HashMap WarningIssued() throws Exception {
        int AttrId = 0;
        int TSDType = 0;
        double TSDValue = 0.0d;
        int AMLevel = 0;
        int nextTag = 0;
        try {
            AttrId = Integer.parseInt((String) this.hsmpretPkg.get("ATTR_ID"));
            TSDType = Integer.parseInt((String) this.hsmpretPkg.get("TSD_TYPE"));
            TSDValue = Double.parseDouble((String) this.hsmpretPkg.get("TSD_VALUE"));
            AMLevel = Integer.parseInt((String) this.hsmpretPkg.get("AM_LEVEL"));
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("ALARM_ID", Integer.valueOf(AttrId));
        if (dbQuery.getCommTable("select * from RVM_ALARM " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
            SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder(MsgType.RVM_ALARM);
            sqlInsertBuilder.newInsertRecord().setNumber("ALARM_ID", Integer.valueOf(AttrId)).setNumber("TSD_TYPE", Integer.valueOf(TSDType)).setNumber("TSD_VALUE", Double.valueOf(TSDValue)).setNumber("AM_LEVEL", Integer.valueOf(AMLevel));
            SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
        } else {
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder(MsgType.RVM_ALARM);
            sqlUpdateBuilder.setNumber("TSD_TYPE", Integer.valueOf(TSDType)).setNumber("TSD_VALUE", Double.valueOf(TSDValue)).setNumber("AM_LEVEL", Integer.valueOf(AMLevel)).setSqlWhere(sqlWhereBuilder);
            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
        }
        HashMap hsmpEvent = new HashMap();
        hsmpEvent.put(AllAdvertisement.MEDIA_TYPE, "RVM_CODE_BACKUP");
        RCCInstanceTask.addTask(hsmpEvent);
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    public HashMap PatrolBarcodeIssued() throws Exception {
        int nextTag = 0;
        int sts = 2;
        int staffPermission = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
            sts = Integer.parseInt((String) this.hsmpretPkg.get("STS"));
            staffPermission = Integer.parseInt((String) this.hsmpretPkg.get("STAFF_PERMISSION"));
        } catch (Exception e) {
        }
        HashMap RVMStaff = (HashMap) this.hsmpretPkg.get("2_DIMEN_SERIAL-STAFF_ID");
        Iterator iter = RVMStaff.keySet().iterator();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        String userExtCode;
        String userStaffId;
        DBQuery dbQuery;
        String userId;
        SqlWhereBuilder sqlWhereBuilder;
        SqlInsertBuilder sqlInsertBuilder;
        if (sts == 1) {
            while (iter.hasNext()) {
                userExtCode = (String) iter.next();
                userStaffId = (String) RVMStaff.get(userExtCode);
                dbQuery = DBQuery.getDBQuery(sqliteDatabase);
                userId = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS")).getSeq("USER_ID");
                sqlWhereBuilder = new SqlWhereBuilder();
                sqlWhereBuilder.addStringEqualsTo("USER_EXT_CODE", userExtCode).addNumberEqualsTo("USER_STATUS", Integer.valueOf(1));
                if (dbQuery.getCommTable("select * from RVM_STAFF " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
                    sqlInsertBuilder = new SqlInsertBuilder("RVM_STAFF");
                    sqlInsertBuilder.newInsertRecord().setNumber("USER_ID", userId).setString("USER_EXT_CODE", userExtCode).setString("USER_LOGIN_NAME", null).setNumber("USER_STAFF_ID", Integer.valueOf(Integer.parseInt(userStaffId))).setNumber("USER_STATUS", Integer.valueOf(1)).setNumber("USER_PERMISSION", Integer.valueOf(staffPermission));
                    listSqlBuilder.add(sqlInsertBuilder);
                } else {
                    SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_STAFF");
                    sqlUpdateBuilder.setNumber("USER_STAFF_ID", Integer.valueOf(Integer.parseInt(userStaffId))).setNumber("USER_STATUS", Integer.valueOf(1)).setNumber("USER_PERMISSION", Integer.valueOf(staffPermission)).setSqlWhere(sqlWhereBuilder);
                    listSqlBuilder.add(sqlUpdateBuilder);
                }
            }
        } else if (sts == 0) {
            while (iter.hasNext()) {
                userExtCode = (String) iter.next();
                dbQuery = DBQuery.getDBQuery(sqliteDatabase);
                sqlWhereBuilder = new SqlWhereBuilder();
                sqlWhereBuilder.addStringEqualsTo("USER_EXT_CODE", userExtCode).addNumberEqualsTo("USER_STATUS", Integer.valueOf(1));
                if (dbQuery.getCommTable("select * from RVM_STAFF " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() != 0) {
                    SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_STAFF");
                    sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
                    listSqlBuilder.add(sqlDeleteBuilder);
                }
            }
        } else if (sts == -1) {
            SqlWhereBuilder sqlWhereBuilderUserId = new SqlWhereBuilder();
            sqlWhereBuilderUserId.addNumberNotEqualsTo("USER_ID", Integer.valueOf(0));
            SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_STAFF");
            sqlDeleteBuilder.setSqlWhere(sqlWhereBuilderUserId);
            listSqlBuilder.add(sqlDeleteBuilder);
            while (iter.hasNext()) {
                userExtCode = (String) iter.next();
                userStaffId = (String) RVMStaff.get(userExtCode);
                userId = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS")).getSeq("USER_ID");
                sqlInsertBuilder = new SqlInsertBuilder("RVM_STAFF");
                sqlInsertBuilder.newInsertRecord().setNumber("USER_ID", userId).setString("USER_EXT_CODE", userExtCode).setString("USER_LOGIN_NAME", null).setNumber("USER_STAFF_ID", Integer.valueOf(Integer.parseInt(userStaffId))).setNumber("USER_STATUS", Integer.valueOf(1)).setNumber("USER_PERMISSION", Integer.valueOf(staffPermission));
                listSqlBuilder.add(sqlInsertBuilder);
            }
        }
        if (listSqlBuilder != null) {
            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
            HashMap hsmpEvent = new HashMap();
            hsmpEvent.put(AllAdvertisement.MEDIA_TYPE, "RVM_CODE_BACKUP");
            RCCInstanceTask.addTask(hsmpEvent);
        }
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    public HashMap StartOrStopIssued() throws Exception {
        int i;
        String RUN_OPT = (String) this.hsmpretPkg.get("RUN_OPT");
        String OPT_TYPE = (String) this.hsmpretPkg.get("OPT_TYPE");
        String serviceName = null;
        Properties externalProp = new Properties();
        for (i = 0; i < SysDef.SERVICE_OF_OPT_TYPE.length; i++) {
            if (SysDef.SERVICE_OF_OPT_TYPE[i][0].equals(OPT_TYPE)) {
                serviceName = SysDef.SERVICE_OF_OPT_TYPE[i][1];
            }
        }
        if (serviceName != null) {
            Properties externalProp2;
            String RECYCLE_MATERIAL_SET;
            String service_cfg_disable;
            String[] str_service_cfg_disable;
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
                if (ConvenienceService.LNKCARDSTATUS.equalsIgnoreCase(serviceName)) {
                    externalProp.put("DISABLE_LNK_CARD_ENABLED", "FALSE");
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                    SysConfig.set(externalProp);
                    RVMShell.backupExternalConfig();
                }
                if (ServiceName.PRINTER.equalsIgnoreCase(serviceName)) {
                    externalProp.put("SET.PRINT.ENABLE", "FALSE");
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                    SysConfig.set(externalProp);
                    RVMShell.backupExternalConfig();
                }
                if (serviceName.equals("FENXUANQI")) {
                    externalProp2 = new Properties();
                    if ("FALSE".equalsIgnoreCase(SysConfig.get("RECYCLE.STATUS.STOP"))) {
                        externalProp2.put("RECYCLE.STATUS.STOP", "TRUE");
                    }
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp2);
                    SysConfig.set(externalProp2);
                    RVMShell.backupExternalConfig();
                }
                if (ServiceName.PET.equalsIgnoreCase(serviceName) || ServiceName.METAL.equalsIgnoreCase(serviceName)) {
                    RECYCLE_MATERIAL_SET = SysConfig.get("RECYCLE.MATERIAL.SET");
                    if (!StringUtils.isBlank(RECYCLE_MATERIAL_SET) && RECYCLE_MATERIAL_SET.contains(serviceName)) {
                        RECYCLE_MATERIAL_SET = StringUtils.replace(RECYCLE_MATERIAL_SET, serviceName, "");
                    }
                    externalProp.put("RECYCLE.MATERIAL.SET", RECYCLE_MATERIAL_SET);
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                    SysConfig.set(externalProp);
                    RVMShell.backupExternalConfig();
                }
                if (!listDisabledService.contains(serviceName)) {
                    i = 0;
                    while (i < listDisabledService.size() && !serviceName.equalsIgnoreCase((String) listDisabledService.get(i))) {
                        i++;
                    }
                    if (i == listDisabledService.size()) {
                        listDisabledService.add(serviceName);
                    }
                }
                service_cfg_disable = SysConfig.get("SERVICE_CFG_DISABLE");
                if (service_cfg_disable == null || "".equals(service_cfg_disable)) {
                    externalProp.put("SERVICE_CFG_DISABLE", serviceName + ";");
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                    SysConfig.set(externalProp);
                } else {
                    str_service_cfg_disable = service_cfg_disable.split(";");
                    i = 0;
                    while (i < str_service_cfg_disable.length && !serviceName.equalsIgnoreCase(str_service_cfg_disable[i])) {
                        i++;
                    }
                    if (i == str_service_cfg_disable.length) {
                        externalProp.put("SERVICE_CFG_DISABLE", service_cfg_disable + serviceName + ";");
                        PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                        SysConfig.set(externalProp);
                    }
                }
                if ("BOTTLELIMITED".equalsIgnoreCase(serviceName)) {
                    BOTTLES_LIMITED_ENABLE = "FALSE";
                }
            }
            if ("1".equals(RUN_OPT)) {
                if (ConvenienceService.LNKCARDSTATUS.equalsIgnoreCase(serviceName)) {
                    externalProp.put("DISABLE_LNK_CARD_ENABLED", "TRUE");
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                    SysConfig.set(externalProp);
                    RVMShell.backupExternalConfig();
                }
                if (ServiceName.PRINTER.equalsIgnoreCase(serviceName)) {
                    externalProp.put("SET.PRINT.ENABLE", "TRUE");
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                    SysConfig.set(externalProp);
                    RVMShell.backupExternalConfig();
                }
                if (serviceName.equals("FENXUANQI")) {
                    externalProp2 = new Properties();
                    if ("TRUE".equalsIgnoreCase(SysConfig.get("RECYCLE.STATUS.STOP"))) {
                        externalProp2.put("RECYCLE.STATUS.STOP", "FALSE");
                    }
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp2);
                    SysConfig.set(externalProp2);
                    RVMShell.backupExternalConfig();
                }
                if (ServiceName.PET.equalsIgnoreCase(serviceName) || ServiceName.METAL.equalsIgnoreCase(serviceName)) {
                    RECYCLE_MATERIAL_SET = SysConfig.get("RECYCLE.MATERIAL.SET");
                    if (StringUtils.isBlank(RECYCLE_MATERIAL_SET)) {
                        RECYCLE_MATERIAL_SET = serviceName;
                    } else if (!RECYCLE_MATERIAL_SET.contains(serviceName)) {
                        RECYCLE_MATERIAL_SET = RECYCLE_MATERIAL_SET + ";" + serviceName;
                    }
                    externalProp.put("RECYCLE.MATERIAL.SET", RECYCLE_MATERIAL_SET);
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                    SysConfig.set(externalProp);
                    RVMShell.backupExternalConfig();
                }
                if (ServiceName.SCREEN_CAPTURE.equalsIgnoreCase(serviceName)) {
                    screenCap();
                }
                if (ServiceName.TAKE_PHOTO.equalsIgnoreCase(serviceName)) {
                    takePhotoTask();
                }
                if (serviceName.equals("CARD") || serviceName.equals("COUPON") || serviceName.equals("DONATION") || serviceName.equals("QRCODE") || serviceName.equals("TRANSPORTCARD") || serviceName.equals("PHONE") || serviceName.equals("WECHAT") || serviceName.equals("BDJ")) {
                    String strVengdingWay = SysConfig.get("VENDING.WAY");
                    if (!strVengdingWay.contains(serviceName)) {
                        StringBuffer sbVengdingWay = new StringBuffer();
                        sbVengdingWay.append(strVengdingWay);
                        sbVengdingWay.append(";" + serviceName);
                        externalProp.put("VENDING.WAY", sbVengdingWay.toString());
                        PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                        SysConfig.set(externalProp);
                        RVMShell.backupExternalConfig();
                    }
                }
                if (listDisabledService.contains(serviceName)) {
                    i = 0;
                    while (i < listDisabledService.size() && !serviceName.equalsIgnoreCase((String) listDisabledService.get(i))) {
                        i++;
                    }
                    if (i < listDisabledService.size()) {
                        listDisabledService.remove(serviceName);
                    }
                }
                service_cfg_disable = SysConfig.get("SERVICE_CFG_DISABLE");
                if (!(service_cfg_disable == null || "".equals(service_cfg_disable))) {
                    StringBuffer sbSerCfg = new StringBuffer();
                    List<String> listServiceCfgDisable = new ArrayList();
                    str_service_cfg_disable = service_cfg_disable.split(";");
                    for (String add2 : str_service_cfg_disable) {
                        listServiceCfgDisable.add(add2);
                    }
                    i = 0;
                    while (i < str_service_cfg_disable.length && !serviceName.equalsIgnoreCase(str_service_cfg_disable[i])) {
                        i++;
                    }
                    if (i < str_service_cfg_disable.length) {
                        listServiceCfgDisable.remove(serviceName);
                    }
                    for (int j = 0; j < listServiceCfgDisable.size(); j++) {
                        sbSerCfg.append((String) listServiceCfgDisable.get(j));
                        sbSerCfg.append(";");
                    }
                    externalProp.put("SERVICE_CFG_DISABLE", sbSerCfg.toString());
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                    SysConfig.set(externalProp);
                }
                if ("BOTTLELIMITED".equalsIgnoreCase(serviceName)) {
                    BOTTLES_LIMITED_ENABLE = "TRUE";
                }
            }
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
            if (!StringUtils.isBlank(serviceName)) {
                if (serviceName.equalsIgnoreCase(ServiceName.BOTTLE_NUMBER_CLEAR)) {
                    CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "clearNum", null);
                }
                if (serviceName.equalsIgnoreCase(ServiceName.PAPER_WEIGHT_CLEAR)) {
                    CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "clearWeight", null);
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
            HashMap<String, String> hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put("EVENT", "CMD");
            hsmpGUIEvent.put("CMD", "START_OR_STOP_SERVER");
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        }
        HashMap hsmpEvent = new HashMap();
        hsmpEvent.put(AllAdvertisement.MEDIA_TYPE, "RVM_CODE_BACKUP");
        RCCInstanceTask.addTask(hsmpEvent);
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    public HashMap ScrollbarIssued() throws Exception {
        String Str = (String) this.hsmpretPkg.get("SBAR_ID");
        Integer sbarId = null;
        Integer delSbarId = null;
        if (Str != null) {
            try {
                if (!"".equals(Str)) {
                    sbarId = Integer.valueOf(Integer.parseInt(Str));
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        String Str2 = (String) this.hsmpretPkg.get("DEL_SBAR_ID");
        if (!(Str2 == null || "".equals(Str2))) {
            delSbarId = Integer.valueOf(Integer.parseInt(Str2));
        }
        String barText = (String) this.hsmpretPkg.get("TEXT");
        String backText = (String) this.hsmpretPkg.get("BACK_TEXT");
        String begingTime = (String) this.hsmpretPkg.get("BEGIN_TIME");
        String endTime = (String) this.hsmpretPkg.get("END_TIME");
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("SBAR_ID", sbarId);
        CommTable commTable = dbQuery.getCommTable("select * from RVM_TXT_AD " + sqlWhereBuilder.toSqlWhere("where"));
        SqlInsertBuilder sqlInsertBuilder;
        SqlUpdateBuilder sqlUpdateBuilder;
        if (delSbarId != null) {
            SqlWhereBuilder sqlWhereBuilder1 = new SqlWhereBuilder();
            sqlWhereBuilder1.addNumberEqualsTo("SBAR_ID", delSbarId);
            if (dbQuery.getCommTable("select * from RVM_TXT_AD " + sqlWhereBuilder1.toSqlWhere("where")).getRecordCount() != 0) {
                List<SqlBuilder> listSqlBuilder = new ArrayList();
                SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_TXT_AD");
                sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder1);
                listSqlBuilder.add(sqlDeleteBuilder);
                if (sbarId != null) {
                    if (dbQuery.getCommTable("select * from RVM_TXT_AD " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
                        sqlInsertBuilder = new SqlInsertBuilder("RVM_TXT_AD");
                        sqlInsertBuilder.newInsertRecord().setNumber("SBAR_ID", sbarId).setString("SBAR_TEXT", barText).setString("BEGIN_TIME", begingTime).setString("END_TIME", endTime).setString("BACK_TEXT", backText);
                        listSqlBuilder.add(sqlInsertBuilder);
                    } else {
                        sqlUpdateBuilder = new SqlUpdateBuilder("RVM_TXT_AD");
                        sqlUpdateBuilder.setString("SBAR_TEXT", barText).setString("BEGIN_TIME", begingTime).setString("END_TIME", endTime).setString("BACK_TEXT", backText).setSqlWhere(sqlWhereBuilder);
                        listSqlBuilder.add(sqlUpdateBuilder);
                    }
                }
                if (listSqlBuilder != null) {
                    SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                }
            } else if (sbarId != null) {
                if (dbQuery.getCommTable("select * from RVM_TXT_AD " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
                    sqlInsertBuilder = new SqlInsertBuilder("RVM_TXT_AD");
                    sqlInsertBuilder.newInsertRecord().setNumber("SBAR_ID", sbarId).setString("SBAR_TEXT", barText).setString("BEGIN_TIME", begingTime).setString("END_TIME", endTime).setString("BACK_TEXT", backText);
                    SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
                } else {
                    sqlUpdateBuilder = new SqlUpdateBuilder("RVM_TXT_AD");
                    sqlUpdateBuilder.setString("SBAR_TEXT", barText).setString("BEGIN_TIME", begingTime).setString("END_TIME", endTime).setString("BACK_TEXT", backText).setSqlWhere(sqlWhereBuilder);
                    SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                }
            }
        } else if (commTable.getRecordCount() == 0 && sbarId != null) {
            sqlInsertBuilder = new SqlInsertBuilder("RVM_TXT_AD");
            sqlInsertBuilder.newInsertRecord().setNumber("SBAR_ID", sbarId).setString("SBAR_TEXT", barText).setString("BEGIN_TIME", begingTime).setString("END_TIME", endTime).setString("BACK_TEXT", backText);
            SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
        } else if (!(sbarId == null || commTable.getRecordCount() == 0)) {
            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_TXT_AD");
            sqlUpdateBuilder.setString("SBAR_TEXT", barText).setString("BEGIN_TIME", begingTime).setString("END_TIME", endTime).setString("BACK_TEXT", backText).setSqlWhere(sqlWhereBuilder);
            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
        }
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put("EVENT", "CMD");
        hsmpGUIEvent.put("CMD", "SCROLL_BAR_ISSUED");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    private String getFileName(String url) {
        return url.substring(url.lastIndexOf("=") + 1);
    }

    public HashMap PicturesAudioIssued() throws Exception {
        Integer mediaId = null;
        String mId = (String) this.hsmpretPkg.get("MEDIA_ID");
        if (!(mId == null || "".equals(mId))) {
            mediaId = Integer.valueOf(Integer.parseInt(mId));
        }
        Integer delMediaId = null;
        String dmId = (String) this.hsmpretPkg.get("DEL_MEDIA_ID");
        if (!(dmId == null || "".equals(dmId))) {
            delMediaId = Integer.valueOf(Integer.parseInt(dmId));
        }
        int mediaType = Integer.parseInt((String) this.hsmpretPkg.get("MEDIA_TYPE"));
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        String mediaUrl = (String) this.hsmpretPkg.get("MEDIA_PATH");
        String playLocal = (String) this.hsmpretPkg.get("PAGE_NUM");
        if ("A".equalsIgnoreCase(playLocal)) {
            playLocal = "welcome";
        } else if ("B".equalsIgnoreCase(playLocal)) {
            playLocal = "select";
        } else if ("C".equalsIgnoreCase(playLocal)) {
            playLocal = "bottle_in";
        } else if ("D".equalsIgnoreCase(playLocal)) {
            playLocal = MediaInfo.RECHARING_ACTIVITY;
        } else if ("E".equalsIgnoreCase(playLocal)) {
            playLocal = MediaInfo.CONVENIENCE_ACTIVITY;
        }
        int priority = Integer.parseInt((String) this.hsmpretPkg.get("PLAY_SQE"));
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("MEDIA_ID", mediaId);
        SqlWhereBuilder sqlWhereBuilder1 = new SqlWhereBuilder();
        sqlWhereBuilder1.addNumberEqualsTo("MEDIA_ID", delMediaId);
        String mediaRoot = SysConfig.get("MEDIA_ROOT");
        String mr = mediaRoot.substring(mediaRoot.lastIndexOf("/") + 1);
        if (!(mr == null || "".equals(mr))) {
            mediaRoot = mediaRoot + "/";
        }
        SqlInsertBuilder sqlInsertBuilder;
        SqlUpdateBuilder sqlUpdateBuilder;
        if (delMediaId != null || mediaId == null) {
            SqlDeleteBuilder sqlDeleteBuilder;
            if (delMediaId != null && mediaId != null) {
                CommTable commTable1 = dbQuery.getCommTable("select * from RVM_MEDIA " + sqlWhereBuilder.toSqlWhere("where"));
                if (dbQuery.getCommTable("select * from RVM_MEDIA " + sqlWhereBuilder1.toSqlWhere("where")).getRecordCount() != 0) {
                    sqlDeleteBuilder = new SqlDeleteBuilder("RVM_MEDIA");
                    sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder1);
                    SQLiteExecutor.execSql(sqliteDatabase, sqlDeleteBuilder.toSql());
                }
                if (commTable1.getRecordCount() == 0) {
                    sqlInsertBuilder = new SqlInsertBuilder("RVM_MEDIA");
                    sqlInsertBuilder.newInsertRecord().setNumber("MEDIA_ID", mediaId).setString("MEDIA_URL", mediaUrl).setNumber("MEDIA_TYPE", Integer.valueOf(mediaType)).setNumber("DOWNLOAD_FLAG", Integer.valueOf(0)).setString("FILE_PATH", mediaRoot + getFileName(mediaUrl)).setString("MEDIA_PLAY_LOCAL", playLocal).setNumber("MEDIA_PRIORITY", Integer.valueOf(priority));
                    SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
                } else {
                    sqlUpdateBuilder = new SqlUpdateBuilder("RVM_MEDIA");
                    sqlUpdateBuilder.setString("MEDIA_URL", mediaUrl).setNumber("MEDIA_TYPE", Integer.valueOf(mediaType)).setNumber("DOWNLOAD_FLAG", Integer.valueOf(0)).setString("FILE_PATH", mediaRoot + getFileName(mediaUrl)).setString("MEDIA_PLAY_LOCAL", playLocal).setNumber("MEDIA_PRIORITY", Integer.valueOf(priority)).setSqlWhere(sqlWhereBuilder);
                    SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                }
            } else if (!(delMediaId == null || mediaId != null || dbQuery.getCommTable("select * from RVM_MEDIA " + sqlWhereBuilder1.toSqlWhere("where")).getRecordCount() == 0)) {
                sqlDeleteBuilder = new SqlDeleteBuilder("RVM_MEDIA");
                sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder1);
                SQLiteExecutor.execSql(sqliteDatabase, sqlDeleteBuilder.toSql());
            }
        } else if (dbQuery.getCommTable("select * from RVM_MEDIA " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
            sqlInsertBuilder = new SqlInsertBuilder("RVM_MEDIA");
            sqlInsertBuilder.newInsertRecord().setNumber("MEDIA_ID", mediaId).setString("MEDIA_URL", mediaUrl).setNumber("MEDIA_TYPE", Integer.valueOf(mediaType)).setNumber("DOWNLOAD_FLAG", Integer.valueOf(0)).setString("FILE_PATH", mediaRoot + getFileName(mediaUrl)).setString("MEDIA_PLAY_LOCAL", playLocal).setNumber("MEDIA_PRIORITY", Integer.valueOf(priority));
            SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
        } else {
            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_MEDIA");
            sqlUpdateBuilder.setString("MEDIA_URL", mediaUrl).setNumber("MEDIA_TYPE", Integer.valueOf(mediaType)).setNumber("DOWNLOAD_FLAG", Integer.valueOf(0)).setString("FILE_PATH", mediaRoot + getFileName(mediaUrl)).setString("MEDIA_PLAY_LOCAL", playLocal).setNumber("MEDIA_PRIORITY", Integer.valueOf(priority)).setSqlWhere(sqlWhereBuilder);
            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
        }
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    public HashMap TaskVolMoney() throws Exception {
        HashMap<String, String> attrIdPrice;
        String localAreaId = (String) this.hsmpretPkg.get("LOCAL_AREA_ID");
        String priceVersion = (String) this.hsmpretPkg.get("ATTR_VERSION");
        String productType = (String) this.hsmpretPkg.get("PRODUCT_TYPE");
        String PRODUCT_TYPE = ProductType.getProductType(productType);
        if (productType.equalsIgnoreCase("1")) {
            Properties prop = new Properties();
            if (!localAreaId.equalsIgnoreCase(SysConfig.get("RVM.AREA.CODE"))) {
                prop.setProperty("RVM.AREA.CODE", localAreaId);
            }
            if (!priceVersion.equalsIgnoreCase(SysConfig.get("RVM_PRICE_VERSION"))) {
                prop.setProperty("RVM_PRICE_VERSION", priceVersion);
            }
            if (prop.size() > 0) {
                PropUtils.update(new File(SysConfig.get("EXTERNAL.FILE")), prop);
                SysConfig.set(prop);
                RVMShell.backupExternalConfig();
            }
        }
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        HashMap<String, String> priceMap = (HashMap) this.hsmpretPkg.get("PAY_EQ");
        if (priceMap == null || priceMap.size() <= 0) {
            listSqlBuilder.add(new SqlDeleteBuilder("RVM_PRICE_MAP"));
        } else {
            for (String keyVengdWay : priceMap.keySet()) {
                String priceMapDetail = (String) priceMap.get(keyVengdWay);
                String keyVengdWay2 = CardType.getCardType(keyVengdWay);
                SqlWhereBuilder sqlWhereBuilderPriceMap = new SqlWhereBuilder();
                sqlWhereBuilderPriceMap.addStringEqualsTo("PRODUCT_TYPE", PRODUCT_TYPE);
                if (dbQuery.getCommTable("select * from RVM_PRICE_MAP " + sqlWhereBuilderPriceMap.toSqlWhere("where")).getRecordCount() != 0) {
                    SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_PRICE_MAP");
                    sqlDeleteBuilder.setSqlWhere(sqlWhereBuilderPriceMap);
                    SQLiteExecutor.execSql(sqliteDatabase, sqlDeleteBuilder.toSql());
                }
                SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_PRICE_MAP");
                sqlInsertBuilder.newInsertRecord().setString(AllAdvertisement.VENDING_WAY, keyVengdWay2).setString("PRICE_MAP", priceMapDetail).setString("PRODUCT_TYPE", PRODUCT_TYPE);
                listSqlBuilder.add(sqlInsertBuilder);
            }
        }
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        listSqlBuilder.clear();
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        HashMap<String, HashMap<String, String>> hsmpNewBarCodePrice = new HashMap();
        if (productType.equalsIgnoreCase("1")) {
            HashMap<String, String> hsmpNewBarCodePriceItem;
            listSqlBuilder.clear();
            attrIdPrice = (HashMap) this.hsmpretPkg.get("ATTR_ID-PRICE");
            for (String keyAttrId : attrIdPrice.keySet()) {
                String[] VolandMaterials = ((String) attrIdPrice.get(keyAttrId)).split(";");
                String price = VolandMaterials[0];
                String vol = SysDef.formatBound(VolandMaterials[1], 2);
                String stuff = VolandMaterials[2];
                String weigh = SysDef.formatBound(VolandMaterials[3], 2);
                String color = VolandMaterials[4];
                String priority = "100";
                if (VolandMaterials.length >= 6) {
                    priority = VolandMaterials[5];
                }
                hsmpNewBarCodePriceItem = new HashMap();
                hsmpNewBarCodePriceItem.put("BAR_CODE_VOL", vol);
                hsmpNewBarCodePriceItem.put("BAR_CODE_AMOUNT", price);
                hsmpNewBarCodePriceItem.put("BAR_CODE_STUFF", stuff);
                hsmpNewBarCodePriceItem.put("BAR_CODE_WEIGH", weigh);
                hsmpNewBarCodePriceItem.put("BAR_CODE_COLOR", color);
                hsmpNewBarCodePriceItem.put("BAR_CODE_PRICE_PRIORITY", priority);
                hsmpNewBarCodePrice.put(RVMUtils.generateBarcodePriceKey(vol, stuff, weigh, color), hsmpNewBarCodePriceItem);
            }
            for (String BAR_CODE_PRICE_KEY : hsmpNewBarCodePrice.keySet()) {
                hsmpNewBarCodePriceItem = (HashMap) hsmpNewBarCodePrice.get(BAR_CODE_PRICE_KEY);
                SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_BAR_CODE_PRICE");
                sqlInsertBuilder.newInsertRecord().setString("BAR_CODE_PRICE_KEY", BAR_CODE_PRICE_KEY).setString("BAR_CODE_VOL", hsmpNewBarCodePriceItem.get("BAR_CODE_VOL")).setNumber("BAR_CODE_AMOUNT", hsmpNewBarCodePriceItem.get("BAR_CODE_AMOUNT")).setNumber("BAR_CODE_STUFF", hsmpNewBarCodePriceItem.get("BAR_CODE_STUFF")).setString("BAR_CODE_WEIGH", hsmpNewBarCodePriceItem.get("BAR_CODE_WEIGH")).setString("BAR_CODE_COLOR", hsmpNewBarCodePriceItem.get("BAR_CODE_COLOR")).setNumber("BAR_CODE_PRICE_PRIORITY", hsmpNewBarCodePriceItem.get("BAR_CODE_PRICE_PRIORITY")).setString("AREA_ID", localAreaId).setString(updateDetection.VERSION, priceVersion);
                listSqlBuilder.add(sqlInsertBuilder);
            }
            if (listSqlBuilder.size() > 0) {
                SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_BAR_CODE_PRICE");
                List<SqlBuilder> listSqlBuilderAll = new ArrayList();
                listSqlBuilderAll.add(sqlDeleteBuilder);
                listSqlBuilderAll.addAll(listSqlBuilder);
                SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilderAll);
                RVMBarcodePriceMgr.getMgr().load(0, true);
            }
        }
        if (productType.equalsIgnoreCase("2")) {
            listSqlBuilder.clear();
            attrIdPrice = (HashMap) this.hsmpretPkg.get("ATTR_ID-PRICE");
            double paperPrice = 0.0d;
            for (String keyAttrId2 : attrIdPrice.keySet()) {
                String paperPriceStr = ((String) attrIdPrice.get(keyAttrId2)).split(";")[0];
                if (!(paperPriceStr == null || "".equals(paperPriceStr))) {
                    paperPrice = Double.parseDouble(paperPriceStr);
                }
            }
            SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_PAPER_PRICE");
            SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_PAPER_PRICE");
            sqlInsertBuilder.newInsertRecord().setNumber("PAPER_WEIGH", Integer.valueOf(1)).setNumber("PAPER_PRICE", Double.valueOf(paperPrice)).setString("AREA_ID", localAreaId);
            listSqlBuilder.add(sqlDeleteBuilder);
            listSqlBuilder.add(sqlInsertBuilder);
            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        }
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    public HashMap TaskBlackBar() throws Exception {
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        List listBarCode = (List) this.hsmpretPkg.get("BLACK_BAR");
        if (listBarCode != null) {
            SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
            List<SqlBuilder> listSqlBuilder = new ArrayList();
            for (int i = 0; i < listBarCode.size(); i++) {
                String barCode = (String) listBarCode.get(i);
                DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
                SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
                sqlWhereBuilder.addStringEqualsTo("BAR_CODE", barCode);
                if (dbQuery.getCommTable("select * from RVM_BAR_CODE " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() != 0) {
                    SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_BAR_CODE");
                    sqlUpdateBuilder.setNumber("BAR_CODE_FLAG", Integer.valueOf(2)).setSqlWhere(sqlWhereBuilder);
                    listSqlBuilder.add(sqlUpdateBuilder);
                }
            }
            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        }
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    public HashMap taskVOLConvertion() throws Exception {
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        String localAreaId = (String) this.hsmpretPkg.get("LOCAL_AREA_ID");
        String VOL_CONVERSION = (String) this.hsmpretPkg.get("VOL_CONVERSION");
        if (!StringUtils.isBlank(VOL_CONVERSION)) {
            Properties prop = new Properties();
            prop.setProperty("VOL_CONVERSION", VOL_CONVERSION);
            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), prop);
            SysConfig.set(prop);
            RVMShell.backupExternalConfig();
        }
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    public HashMap TaskRVMStartOrShutdown() throws Exception {
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        String localAreaId = (String) this.hsmpretPkg.get("LOCAL_AREA_ID");
        String startTime = (String) this.hsmpretPkg.get("SVC_START_TIME");
        String shutdownTime = (String) this.hsmpretPkg.get("SVC_END_TIME");
        String sts = (String) this.hsmpretPkg.get("STS");
        Properties prop = new Properties();
        if ("1".equals(sts)) {
            if (startTime.equals(shutdownTime)) {
                prop.setProperty("RVM.POWER.ON.TIME", "");
                prop.setProperty("RVM.POWER.OFF.TIME", "");
                CommService.getCommService().execute("RVM_POWER_OFF_DISABLE", null);
            } else {
                prop.setProperty("RVM.POWER.ON.TIME", startTime);
                prop.setProperty("RVM.POWER.OFF.TIME", shutdownTime);
                HashMap hsmpParam = new HashMap();
                hsmpParam.put("POWER_ON_TIME", startTime);
                hsmpParam.put("POWER_OFF_TIME", shutdownTime);
                CommService.getCommService().execute("RVM_POWER_OFF_ENABLE", JSONUtils.toJSON(hsmpParam));
            }
        } else if ("0".equals(sts)) {
            prop.setProperty("RVM.POWER.ON.TIME", "");
            prop.setProperty("RVM.POWER.OFF.TIME", "");
            CommService.getCommService().execute("RVM_POWER_OFF_DISABLE", null);
        }
        PropUtils.update(SysConfig.get("EXTERNAL.FILE"), prop);
        SysConfig.set(prop);
        RVMShell.backupExternalConfig();
        HashMap hsmpEvent = new HashMap();
        hsmpEvent.put(AllAdvertisement.MEDIA_TYPE, "RVM_CODE_BACKUP");
        RCCInstanceTask.addTask(hsmpEvent);
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    private HashMap RCCTaskSummary(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("UPLOAD_FLAG", Integer.valueOf(0));
        CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_OPT_SUMMARY " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            HashMap hsmpMsg = new HashMap();
            hsmpMsg.put("MES_TYPE", MsgType.RVM_DAYSUM);
            hsmpMsg.put("TERM_NO", SysConfig.get("RVM.CODE"));
            hsmpMsg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                Integer OptSummaryId = Integer.valueOf(Integer.parseInt(commTable.getRecord(i).get("OPT_SUMMARY_ID")));
                String VendingTime = commTable.getRecord(i).get("VENDING_TIME");
                String OpBatchId = commTable.getRecord(i).get("OP_BATCH_ID");
                String ProductType = commTable.getRecord(i).get("PRODUCT_TYPE");
                String RecycleQuantity = commTable.getRecord(i).get("RECYCLE_QUANTITY");
                String SumMoney = commTable.getRecord(i).get("SUM_MONEY");
                String SendT = commTable.getRecord(i).get("SEND_TIME");
                Date date = null;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(VendingTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Long VendTime = Long.valueOf(date.getTime());
                Long SendTime = Long.valueOf(new Date().getTime());
                if ("BOTTLE".equalsIgnoreCase(ProductType)) {
                    hsmpMsg.put("PRODUCT_TYPE", "1");
                }
                if ("PAPER".equalsIgnoreCase(ProductType)) {
                    hsmpMsg.put("PRODUCT_TYPE", "2");
                }
                if ("COUPON".equalsIgnoreCase(ProductType)) {
                    hsmpMsg.put("PRODUCT_TYPE", "3");
                }
                hsmpMsg.put("VENDING_TIME", VendTime.toString());
                hsmpMsg.put("OP_BATCH_ID", OpBatchId);
                hsmpMsg.put("RECYCLE_QUANTITY", RecycleQuantity);
                hsmpMsg.put("SUM_MONEY", SumMoney);
                hsmpMsg.put("SEND_TIME", SendTime.toString());
                this.hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpMsg)));
                if (this.hsmpretPkg == null) {
                    return null;
                }
                String termNo = (String) this.hsmpretPkg.get("TERM_NO");
                if ("RESPONSE".equalsIgnoreCase((String) this.hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                    String sendTime;
                    SqlWhereBuilder sqlWhereBuilder2;
                    SqlUpdateBuilder sqlUpdateBuilder;
                    Integer Confirm = Integer.valueOf(Integer.parseInt((String) this.hsmpretPkg.get("CONFIRM")));
                    if (Confirm.intValue() == 1) {
                        sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        sqlWhereBuilder2 = new SqlWhereBuilder();
                        sqlWhereBuilder2.addNumberEqualsTo("OPT_SUMMARY_ID", OptSummaryId);
                        sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT_SUMMARY");
                        sqlUpdateBuilder.setNumber("UPLOAD_FLAG", Integer.valueOf(1)).setString("SEND_TIME", sendTime).setSqlWhere(sqlWhereBuilder2);
                        SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                    }
                    if (Confirm.intValue() == 0) {
                        sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        sqlWhereBuilder2 = new SqlWhereBuilder();
                        sqlWhereBuilder2.addNumberEqualsTo("OPT_SUMMARY_ID", OptSummaryId);
                        sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT_SUMMARY");
                        sqlUpdateBuilder.setNumber("UPLOAD_FLAG", Integer.valueOf(-1)).setString("SEND_TIME", sendTime).setSqlWhere(sqlWhereBuilder2);
                        SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                    }
                }
            }
        }
        return null;
    }

    public HashMap RCCTaskRVMRecycleDetail(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("OPT_TYPE", "RECYCLE").addNumberEqualsTo("OPT_STATUS", Integer.valueOf(1));
        CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_OPT " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                String OPT_ID = commTable.getRecord(i).get("OPT_ID");
                HashMap<String, String> hsmpInstanceTask = new HashMap();
                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_OPT");
                hsmpInstanceTask.put("OPT_ID", OPT_ID);
                RCCInstanceTask.addTask(hsmpInstanceTask);
            }
        }
        return null;
    }

    public HashMap RCCTaskRVMBarCodeUnknown(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpSend = new HashMap();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("UPLOAD_FLAG", Integer.valueOf(0));
        CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_BAR_CODE_UNKNOWN " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            List listCarCode = new ArrayList();
            HashMap barCodeUnknown = new HashMap();
            hsmpSend.put("MES_TYPE", MsgType.RVM_BAR);
            hsmpSend.put("TERM_NO", SysConfig.get("RVM.CODE"));
            hsmpSend.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
            Long time = Long.valueOf(new Date().getTime());
            hsmpSend.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + time);
            hsmpSend.put("UP_DATE", time.toString());
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                String BarCode = commTable.getRecord(i).get("BAR_CODE");
                Date date = null;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(commTable.getRecord(i).get("CREATE_TIME"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                barCodeUnknown.put(BarCode, Long.valueOf(date.getTime()).toString());
                listCarCode.add(BarCode);
            }
            hsmpSend.put("BAR_CODE", barCodeUnknown);
            this.hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
            if (this.hsmpretPkg == null) {
                return null;
            }
            String termNo = (String) this.hsmpretPkg.get("TERM_NO");
            if ("RESPONSE".equalsIgnoreCase((String) this.hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                try {
                    List<SqlBuilder> listSqlBuilder;
                    int j;
                    SqlWhereBuilder sqlWhereBuilderOptId;
                    SqlUpdateBuilder sqlUpdateBuilder;
                    Integer Confirm = Integer.valueOf(Integer.parseInt((String) this.hsmpretPkg.get("CONFIRM")));
                    if (Confirm.intValue() == 1) {
                        listSqlBuilder = new ArrayList();
                        for (j = 0; j < listCarCode.size(); j++) {
                            sqlWhereBuilderOptId = new SqlWhereBuilder();
                            sqlWhereBuilderOptId.addStringEqualsTo("BAR_CODE", (String) listCarCode.get(j));
                            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_BAR_CODE_UNKNOWN");
                            sqlUpdateBuilder.setNumber("UPLOAD_FLAG", Integer.valueOf(1)).setSqlWhere(sqlWhereBuilderOptId);
                            listSqlBuilder.add(sqlUpdateBuilder);
                        }
                        if (listSqlBuilder != null) {
                            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                        }
                    }
                    if (Confirm.intValue() == 0) {
                        listSqlBuilder = new ArrayList();
                        for (j = 0; j < listCarCode.size(); j++) {
                            sqlWhereBuilderOptId = new SqlWhereBuilder();
                            sqlWhereBuilderOptId.addStringEqualsTo("BAR_CODE", (String) listCarCode.get(j));
                            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_BAR_CODE_UNKNOWN");
                            sqlUpdateBuilder.setNumber("UPLOAD_FLAG", Integer.valueOf(-1)).setSqlWhere(sqlWhereBuilderOptId);
                            listSqlBuilder.add(sqlUpdateBuilder);
                        }
                        if (listSqlBuilder != null) {
                            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    public HashMap RCCTaskCouponNotEnough(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpSend = new HashMap();
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilder1 = new SqlWhereBuilder();
        sqlWhereBuilder1.addNumberEqualsTo("ACTIVITY_TYPE", Integer.valueOf(1)).addNumberEqualsTo("ACTIVITY_STATUS", Integer.valueOf(1));
        CommTable commTable = dbQuery.getCommTable("select * from RVM_ACTIVITY " + sqlWhereBuilder1.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                Integer activityId = Integer.valueOf(Integer.parseInt(commTable.getRecord(i).get("ACTIVITY_ID")));
                SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
                sqlWhereBuilder.addStringEqualsTo("USED_TIME", null).addNumberEqualsTo("ACTIVITY_ID", activityId);
                CommTable commTable2 = dbQuery.getCommTable("select count(VOUCHER_CODE) as num from RVM_ACTIVITY_VOUCHER " + sqlWhereBuilder.toSqlWhere("where"));
                if (commTable2.getRecordCount() != 0) {
                    for (int j = 0; j < commTable2.getRecordCount(); j++) {
                        Integer num = Integer.valueOf(Integer.parseInt(commTable2.getRecord(j).get("num")));
                        if (num.intValue() < 100) {
                            Long time = Long.valueOf(new Date().getTime());
                            hsmpSend.put("MES_TYPE", MsgType.RVM_COUPON_NE);
                            hsmpSend.put("TERM_NO", SysConfig.get("RVM.CODE"));
                            hsmpSend.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
                            hsmpSend.put("COUPON_TOTAL", num.toString());
                            hsmpSend.put("ACTIVITY_ID", activityId.toString());
                            hsmpSend.put("QU_TIME", time.toString());
                            this.hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
                            if (this.hsmpretPkg == null) {
                                return null;
                            }
                            String termNo = (String) this.hsmpretPkg.get("TERM_NO");
                            if ("RESPONSE".equalsIgnoreCase((String) this.hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                                Integer.valueOf(Integer.parseInt((String) this.hsmpretPkg.get("CONFIRM")));
                            }
                        }
                    }
                    continue;
                }
            }
        }
        return null;
    }

    public HashMap RCCTaskCouponPrinter(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpSend = new HashMap();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("UPLOAD_FLAG", Integer.valueOf(0));
        CommTable commTable = dbQuery.getCommTable("select * from RVM_OPT_VOUCHER " + sqlWhereBuilder.toSqlWhere("where"));
        hsmpSend.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + new Date().getTime() + "v");
        if (commTable.getRecordCount() != 0) {
            HashMap<String, String> hsmpActivityId = new HashMap();
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                hsmpActivityId.put(commTable.getRecord(i).get("ACTIVITY_ID"), commTable.getRecord(i).get("ACTIVITY_ID"));
            }
            for (String key : hsmpActivityId.keySet()) {
                String activityId = (String) hsmpActivityId.get(key);
                SqlWhereBuilder sqlWhereBuilderActivityId = new SqlWhereBuilder();
                sqlWhereBuilderActivityId.addNumberEqualsTo("UPLOAD_FLAG", Integer.valueOf(0)).addNumberEqualsTo("ACTIVITY_ID", Integer.valueOf(Integer.parseInt(activityId)));
                CommTable commTableActivityId = dbQuery.getCommTable("select * from RVM_OPT_VOUCHER " + sqlWhereBuilderActivityId.toSqlWhere("where"));
                hsmpSend.put("MES_TYPE", MsgType.RVM_COUPON_PRINT);
                hsmpSend.put("TERM_NO", SysConfig.get("RVM.CODE"));
                hsmpSend.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
                HashMap CouponPrint = new HashMap();
                List optList = new ArrayList();
                for (int k = 0; k < commTableActivityId.getRecordCount(); k++) {
                    Integer optId = Integer.valueOf(Integer.parseInt(commTableActivityId.getRecord(k).get("OPT_ID")));
                    String VoucherCode = commTableActivityId.getRecord(k).get("VOUCHER_CODE");
                    SqlWhereBuilder sqlWhereBuilder1 = new SqlWhereBuilder();
                    sqlWhereBuilder1.addNumberEqualsTo("OPT_ID", optId);
                    CommTable commTable2 = dbQuery.getCommTable("select * from RVM_OPT " + sqlWhereBuilder1.toSqlWhere("where"));
                    if (commTable2.getRecordCount() != 0) {
                        Date date = null;
                        try {
                            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(commTable2.getRecord(0).get("OPT_TIME"));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Long time = Long.valueOf(date.getTime());
                        CouponPrint.put(VoucherCode, time.toString() + ":" + SysConfig.get("RVM.CODE") + "_" + time + optId);
                        optList.add(optId);
                    }
                }
                Long dtime = Long.valueOf(new Date().getTime());
                hsmpSend.put("COUPON_PRINT", CouponPrint);
                hsmpSend.put("ACTIVITY_ID", activityId);
                hsmpSend.put("ACTIVITY_TYPE", commTableActivityId.getRecord(0).get("ACTIVITY_TYPE"));
                hsmpSend.put("UP_TIME", dtime.toString());
                this.hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
                if (this.hsmpretPkg == null) {
                    return null;
                }
                String termNo = (String) this.hsmpretPkg.get("TERM_NO");
                if ("RESPONSE".equalsIgnoreCase((String) this.hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                    try {
                        List<SqlBuilder> listSqlBuilder;
                        int j;
                        SqlWhereBuilder sqlWhereBuilder2;
                        SqlUpdateBuilder sqlUpdateBuilder;
                        Integer Confirm = Integer.valueOf(Integer.parseInt((String) this.hsmpretPkg.get("CONFIRM")));
                        if (Confirm.intValue() == 1) {
                            listSqlBuilder = new ArrayList();
                            for (j = 0; j < optList.size(); j++) {
                                sqlWhereBuilder2 = new SqlWhereBuilder();
                                sqlWhereBuilder2.addNumberEqualsTo("OPT_ID", optList.get(j));
                                sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT_VOUCHER");
                                sqlUpdateBuilder.setNumber("UPLOAD_FLAG", Integer.valueOf(1)).setSqlWhere(sqlWhereBuilder2);
                                listSqlBuilder.add(sqlUpdateBuilder);
                            }
                            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                        }
                        if (Confirm.intValue() == 0) {
                            listSqlBuilder = new ArrayList();
                            for (j = 0; j < optList.size(); j++) {
                                sqlWhereBuilder2 = new SqlWhereBuilder();
                                sqlWhereBuilder2.addNumberEqualsTo("OPT_ID", optList.get(j));
                                sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT_VOUCHER");
                                sqlUpdateBuilder.setNumber("UPLOAD_FLAG", Integer.valueOf(-1)).setSqlWhere(sqlWhereBuilder2);
                                listSqlBuilder.add(sqlUpdateBuilder);
                            }
                            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public HashMap RCCTaskCouponCancel(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpSend = new HashMap();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("ACTIVITY_STATUS", Integer.valueOf(2));
        CommTable commTable = dbQuery.getCommTable("select * from RVM_ACTIVITY " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            Long optTime = Long.valueOf(new Date().getTime());
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                Integer avtivityId = Integer.valueOf(Integer.parseInt(commTable.getRecord(i).get("ACTIVITY_ID")));
                SqlWhereBuilder sqlWhereBuilder2 = new SqlWhereBuilder();
                sqlWhereBuilder2.addNumberEqualsTo("ACTIVITY_ID", avtivityId);
                CommTable commTable2 = dbQuery.getCommTable("select * from RVM_ACTIVITY_VOUCHER " + sqlWhereBuilder2.toSqlWhere("where"));
                if (commTable2.getRecordCount() != 0) {
                    List listCancel = new ArrayList();
                    for (int j = 0; j < commTable2.getRecordCount(); j++) {
                        listCancel.add(commTable2.getRecord(j).get("VOUCHER_CODE"));
                    }
                    hsmpSend.put("MES_TYPE", MsgType.RVM_COUPON_CAN);
                    hsmpSend.put("TERM_NO", SysConfig.get("RVM.CODE"));
                    hsmpSend.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
                    hsmpSend.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + optTime);
                    hsmpSend.put("CANCEL_NO", listCancel);
                    hsmpSend.put("UP_TIME", optTime.toString());
                    this.hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
                    if (hsmpSend == null) {
                        return null;
                    }
                    String termNo = (String) this.hsmpretPkg.get("TERM_NO");
                    if ("RESPONSE".equalsIgnoreCase((String) this.hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                        try {
                            SqlUpdateBuilder sqlUpdateBuilder;
                            Integer Confirm = Integer.valueOf(Integer.parseInt((String) this.hsmpretPkg.get("CONFIRM")));
                            if (Confirm.intValue() == 1) {
                                sqlUpdateBuilder = new SqlUpdateBuilder("RVM_ACTIVITY");
                                sqlUpdateBuilder.setNumber("ACTIVITY_STATUS", Integer.valueOf(3)).setSqlWhere(sqlWhereBuilder2);
                                SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                            }
                            if (Confirm.intValue() == 0) {
                                sqlUpdateBuilder = new SqlUpdateBuilder("RVM_ACTIVITY");
                                sqlUpdateBuilder.setNumber("ACTIVITY_STATUS", Integer.valueOf(-1)).setSqlWhere(sqlWhereBuilder2);
                                SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return null;
    }

    public HashMap RCCTaskAlarm(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("UPLOAD_FLAG", Integer.valueOf(0));
        CommTable commTable = dbQuery.getCommTable("select * from RVM_ALARM_INST " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                String alarmInstId = commTable.getRecord(i).get("ALARM_INST_ID");
                HashMap<String, String> hsmpInstanceTask = new HashMap();
                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
                hsmpInstanceTask.put("ALARM_INST_ID", alarmInstId);
                hsmpInstanceTask.put("ALARM_TYPE", "0");
                RCCInstanceTask.addTask(hsmpInstanceTask);
            }
        }
        return null;
    }

    public HashMap RCCTaskTransportCardSts(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
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

    public HashMap RCCTaskWorkerSignInUpload(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpSend = new HashMap();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_WORKER_SIGN_IN");
        if (commTable.getRecordCount() != 0) {
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                String userStaffName = commTable.getRecord(i).get("USER_STAFF_NAME");
                if (StringUtils.isBlank(userStaffName)) {
                    userStaffName = "unkown";
                }
                String userStaffId = commTable.getRecord(i).get("USER_STAFF_ID");
                String loginType = commTable.getRecord(i).get("LOGIN_TYPE");
                Date date = null;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(commTable.getRecord(i).get("SIGN_IN_TIME"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Long signTime = Long.valueOf(date.getTime());
                String UpTime = Long.valueOf(new Date().getTime()).toString();
                String UserId = commTable.getRecord(i).get("USER_ID");
                hsmpSend.put("MES_TYPE", MsgType.RVM_WORKER_REGISTER);
                hsmpSend.put("TERM_NO", SysConfig.get("RVM.CODE"));
                hsmpSend.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
                hsmpSend.put("UP_TIME", UpTime);
                hsmpSend.put("RE_TIME", signTime);
                hsmpSend.put("STAFF_NAME", userStaffName);
                hsmpSend.put("LOGIN_TYPE", loginType);
                hsmpSend.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + UpTime + UserId);
                HashMap hsmpRetPkg = new HashMap();
                this.hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
                if (this.hsmpretPkg == null) {
                    return null;
                }
                if ("RESPONSE".equalsIgnoreCase((String) this.hsmpretPkg.get("MES_TYPE"))) {
                    try {
                        if (Integer.valueOf(Integer.parseInt((String) this.hsmpretPkg.get("CONFIRM"))).intValue() == 1) {
                            SqlWhereBuilder sqlWhereBuild = new SqlWhereBuilder();
                            sqlWhereBuild.addStringEndWith("USER_ID", UserId);
                            sqlWhereBuild.addStringEqualsTo("USER_STAFF_NAME", userStaffName);
                            sqlWhereBuild.addStringEqualsTo("USER_STAFF_ID", userStaffId);
                            SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_WORKER_SIGN_IN");
                            sqlDeleteBuilder.setSqlWhere(sqlWhereBuild);
                            SQLiteExecutor.execSql(sqliteDatabase, sqlDeleteBuilder.toSql());
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public HashMap RCCTaskMaintainerOptInfo(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpSend = new HashMap();
        List<String> listOptCon = new ArrayList();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("UPLOAD_FLAG", Integer.valueOf(1));
        CommTable commTable = dbQuery.getCommTable("select * from RVM_MAINTAIN_UPDATE " + sqlWhereBuilder.toSqlWhere("where"));
        if (commTable.getRecordCount() != 0) {
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                String OP_BATCH_ID = commTable.getRecord(i).get("OP_BATCH_ID");
                String STAFF_ID = commTable.getRecord(i).get("STAFF_ID");
                Integer LOGIN_TYPE = Integer.valueOf(Integer.parseInt(commTable.getRecord(i).get("LOGIN_TYPE")));
                String OPT_CONTENT = commTable.getRecord(i).get("OPT_CONTENT");
                SqlDeleteBuilder sqlDeleteBuilder;
                if (OPT_CONTENT == null || OPT_CONTENT == "") {
                    SqlWhereBuilder sqlWhereBuild = new SqlWhereBuilder();
                    sqlWhereBuild.addNumberEqualsTo("OPT_CONTENT", "null");
                    sqlDeleteBuilder = new SqlDeleteBuilder("RVM_MAINTAIN_UPDATE");
                    sqlDeleteBuilder.setSqlWhere(sqlWhereBuild);
                    SQLiteExecutor.execSql(sqliteDatabase, sqlDeleteBuilder.toSql());
                } else {
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
                        try {
                            HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
                            if (hsmpretPkg == null) {
                                return null;
                            }
                            String termNo = (String) hsmpretPkg.get("TERM_NO");
                            if (((String) hsmpretPkg.get("MES_TYPE")).equalsIgnoreCase("RESPONSE") && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo) && Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM"))).intValue() == 1) {
                                SqlWhereBuilder sqlWhereDeleteBuild = new SqlWhereBuilder();
                                sqlWhereDeleteBuild.addStringEndWith("STAFF_ID", STAFF_ID);
                                sqlWhereDeleteBuild.addStringEqualsTo("OP_BATCH_ID", OP_BATCH_ID);
                                sqlDeleteBuilder = new SqlDeleteBuilder("RVM_MAINTAIN_UPDATE");
                                sqlDeleteBuilder.setSqlWhere(sqlWhereDeleteBuild);
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
        return null;
    }

    public HashMap RCCTaskYoukuMovieTicketUpload(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpSend = new HashMap();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_YOUKU_MOVIE_TICKET");
        if (commTable.getRecordCount() != 0) {
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                String CARD_TYPE = commTable.getRecord(i).get("CARD_TYPE");
                String CARD_NO = commTable.getRecord(i).get("CARD_NO");
                Date date = null;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(commTable.getRecord(i).get("OPT_TIME"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Long VendTime = Long.valueOf(date.getTime());
                String UPLOAD_FLAG = commTable.getRecord(i).get("UPLOAD_FLAG");
                String OptId = commTable.getRecord(i).get("OPT_ID");
                hsmpSend.put("MES_TYPE", MsgType.RVM_YOUKU);
                hsmpSend.put("TERM_NO", SysConfig.get("RVM.CODE"));
                hsmpSend.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
                if ("CARD".equals(CARD_TYPE)) {
                    hsmpSend.put("CARD_TYPE", "1");
                } else if ("TRANSPORTCARD".equals(CARD_TYPE)) {
                    hsmpSend.put("CARD_TYPE", "2");
                } else if ("QRCODE".equals(CARD_TYPE)) {
                    hsmpSend.put("CARD_TYPE", "3");
                } else if ("PHONE".equals(CARD_TYPE)) {
                    hsmpSend.put("CARD_TYPE", CardType.MSG_PHONE);
                } else if ("SQRCODE".equals(CARD_TYPE)) {
                    hsmpSend.put("CARD_TYPE", CardType.MSG_SQRCODE);
                } else if ("WECHAT".equals(CARD_TYPE)) {
                    hsmpSend.put("CARD_TYPE", CardType.MSG_WECHAT);
                } else if ("ALIPAY".equals(CARD_TYPE)) {
                    hsmpSend.put("CARD_TYPE", CardType.MSG_ALIPAY);
                } else if ("BDJ".equals(CARD_TYPE)) {
                    hsmpSend.put("CARD_TYPE", CardType.MSG_BDJ);
                } else {
                    hsmpSend.put("CARD_TYPE", "0");
                }
                hsmpSend.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + OptId);
                hsmpSend.put("UPLOAD_FLAG", Integer.valueOf(1));
                HashMap hsmpRetPkg = new HashMap();
                this.hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
                if (this.hsmpretPkg == null) {
                    return null;
                }
                if ("RESPONSE".equalsIgnoreCase((String) this.hsmpretPkg.get("MES_TYPE"))) {
                    try {
                        if (Integer.valueOf(Integer.parseInt((String) this.hsmpretPkg.get("CONFIRM"))).intValue() == 1) {
                            SqlWhereBuilder sqlWhereBuild = new SqlWhereBuilder();
                            sqlWhereBuild.addStringEndWith("OPT_ID", OptId);
                            SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_YOUKU_MOVIE_TICKET");
                            sqlDeleteBuilder.setSqlWhere(sqlWhereBuild);
                            SQLiteExecutor.execSql(sqliteDatabase, sqlDeleteBuilder.toSql());
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public HashMap TaskRccResParam() throws Exception {
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        String localAreaId = (String) this.hsmpretPkg.get("LOCAL_AREA_ID");
        HashMap<String, String> PARAM_INFO = (HashMap) this.hsmpretPkg.get("PARAM_INFO");
        Properties prop = new Properties();
        for (String key : PARAM_INFO.keySet()) {
            prop.setProperty(key, (String) PARAM_INFO.get(key));
        }
        if (!PropUtils.hasEncryptFlag(PropUtils.loadEncryptFile(new File(SysConfig.get("RVM.RESPARAM.FILE"))))) {
            PropUtils.transferEncryptFile(new File(SysConfig.get("RVM.RESPARAM.FILE")));
        }
        PropUtils.updateEncryptFile(new File(SysConfig.get("RVM.RESPARAM.FILE")), prop);
        ShellUtils.shell("cat " + SysConfig.get("RVM.RESPARAM.FILE") + " 2>/dev/null 1>/dev/null");
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }

    public HashMap RCCTaskRechargeFeedback(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
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
                        try {
                            if (Integer.valueOf(Integer.parseInt((String) hsmap.get("CONFIRM"))).intValue() == 1) {
                                SqlWhereBuilder sqlWhereBuild = new SqlWhereBuilder();
                                sqlWhereBuilder.addStringEqualsTo("POS_COMM_SEQ", POS_COMM_SEQ).addStringEqualsTo("ISAM", ISAM).addStringEqualsTo("UID_REC", UID_REC);
                                SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_RECHARGE_FEEDBACK");
                                sqlUpdateBuilder.setString("UPLOAD_FLAG", Integer.valueOf(2)).setSqlWhere(sqlWhereBuild);
                                SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    i++;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    public HashMap RCCTaskBMchargeFeedback(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
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
                        try {
                            if (Integer.valueOf(Integer.parseInt((String) hsmap.get("CONFIRM"))).intValue() == 1) {
                                SqlWhereBuilder sqlWhereBuild = new SqlWhereBuilder();
                                sqlWhereBuilder.addStringEqualsTo("POS_COMM_SEQ", POS_COMM_SEQ).addStringEqualsTo("ISAM", ISAM).addStringEqualsTo("UID_REC", UID_REC);
                                SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_BMCHARGE_FEEDBACK");
                                sqlUpdateBuilder.setString("UPLOAD_FLAG", Integer.valueOf(2)).setSqlWhere(sqlWhereBuild);
                                SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    i++;
                } catch (Exception e2) {
                    e2.printStackTrace();
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
                        try {
                            if (Integer.valueOf(Integer.parseInt((String) hsmap.get("CONFIRM"))).intValue() == 1) {
                                SqlWhereBuilder sqlWhereBuild = new SqlWhereBuilder();
                                sqlWhereBuilder.addStringEqualsTo("POS_COMM_SEQ", POS_COMM_SEQ).addStringEqualsTo("ISAM", ISAM).addStringEqualsTo("UID_REC", UID_REC);
                                SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_BMCHARGE_FEEDBACK");
                                sqlUpdateBuilder.setString("UPLOAD_FLAG", Integer.valueOf(2)).setSqlWhere(sqlWhereBuild);
                                SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    i++;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    private void screenCap() {
        SysGlobal.execute(new Runnable() {
            public void run() {
                try {
                    String mSavedPath = SysConfig.get("SCREENCAP.SAVE.PATH");
                    File mSavedDir = new File(mSavedPath);
                    if (!mSavedDir.isDirectory()) {
                        mSavedDir.mkdirs();
                    }
                    String RVMcode = SysConfig.get("RVM.CODE");
                    if (StringUtils.isBlank(RVMcode)) {
                        RVMcode = "0";
                    }
                    String fileName = mSavedPath + "/" + RVMcode + "_" + DateUtils.formatDatetime(new Date(), "yyyyMMddHHmmss") + "_SCREEN" + ".png";
                    Process process = null;
                    OutputStream outputStream = null;
                    try {
                        process = Runtime.getRuntime().exec("su");
                        String cmd = "screencap -p " + fileName;
                        outputStream = process.getOutputStream();
                        outputStream.write(cmd.getBytes());
                        outputStream.flush();
                        if (outputStream != null) {
                            IOUtils.close(outputStream);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (outputStream != null) {
                            IOUtils.close(outputStream);
                        }
                    } catch (Throwable th) {
                        if (outputStream != null) {
                            IOUtils.close(outputStream);
                        }
                    }
                    process.waitFor();
                    HashMap<String, String> hsmpEvent = new HashMap();
                    hsmpEvent.put(AllAdvertisement.MEDIA_TYPE, "UPLOAD_PICTURE");
                    RCCInstanceTask.addTask(hsmpEvent);
                } catch (Exception e2) {
                }
            }
        });
    }

    private void takePhotoTask() {
        HashMap hsmp = new HashMap();
        hsmp.put("TAKEPHOTO_ENABLE", "TRUE");
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "saveTakePhotoEnable", hsmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put("EVENT", "CMD");
        hsmpGUIEvent.put("CMD", ServiceName.TAKE_PHOTO);
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
    }

    private HashMap TaskRVMSleep() {
        List listsleep = (List) this.hsmpretPkg.get("SLEEP_TIME");
        int nextTag = 0;
        try {
            String tag = (String) this.hsmpretPkg.get("NEXT_TAG");
            if (!StringUtils.isBlank(tag)) {
                nextTag = Integer.parseInt(tag);
            }
        } catch (Exception e) {
        }
        if (listsleep != null && listsleep.size() > 0) {
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
            sqlInsertBuilder.newInsertRecord().setString("SYS_CODE_TYPE", "RVM_INFO").setString("SYS_CODE_KEY", "SLEEP_TIME").setString("SYS_CODE_VALUE", JSONUtils.toJSON(listsleep));
            SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
        }
        if (nextTag == 1) {
            return TASK_REQUEST;
        }
        return null;
    }
}
