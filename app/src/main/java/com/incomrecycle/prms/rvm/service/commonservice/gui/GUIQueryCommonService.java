package com.incomrecycle.prms.rvm.service.commonservice.gui;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.commtable.CommTableRecord;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.sqlite.DBQuery;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class GUIQueryCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if ("hasRecycled".equalsIgnoreCase(subSvnName)) {
            return hasRecycled(svcName, subSvnName, hsmpParam);
        }
        if ("recycledBottleSummary".equalsIgnoreCase(subSvnName)) {
            return recycledBottleSummary(svcName, subSvnName, hsmpParam);
        }
        if ("recycledPaperSummary".equalsIgnoreCase(subSvnName)) {
            return recycledPaperSummary(svcName, subSvnName, hsmpParam);
        }
        if ("queryRecycledAmount".equalsIgnoreCase(subSvnName)) {
            return queryRecycledAmount(svcName, subSvnName, hsmpParam);
        }
        if ("hasPrinterPaper".equalsIgnoreCase(subSvnName)) {
            return hasPrinterPaper(svcName, subSvnName, hsmpParam);
        }
        if ("queryStorageCount".equalsIgnoreCase(subSvnName)) {
            return queryStorageCount(svcName, subSvnName, hsmpParam);
        }
        if ("queryStorageWeight".equalsIgnoreCase(subSvnName)) {
            return queryStorageWeight(svcName, subSvnName, hsmpParam);
        }
        if ("queryMedia".equalsIgnoreCase(subSvnName)) {
            return queryMedia(svcName, subSvnName, hsmpParam);
        }
        if ("queryScrollText".equalsIgnoreCase(subSvnName)) {
            return queryTextAd(svcName, subSvnName, hsmpParam);
        }
        if ("queryStorageMax".equalsIgnoreCase(subSvnName)) {
            return queryStorageMax(svcName, subSvnName, hsmpParam);
        }
        if ("queryStorageWeightMax".equalsIgnoreCase(subSvnName)) {
            return queryStorageWeightMax(svcName, subSvnName, hsmpParam);
        }
        if ("queryServiceEnable".equalsIgnoreCase(subSvnName)) {
            return queryServiceEnable(svcName, subSvnName, hsmpParam);
        }
        if ("queryServiceDisable".equalsIgnoreCase(subSvnName)) {
            return queryServiceDisable(svcName, subSvnName, hsmpParam);
        }
        if ("queryIsStorageMax".equalsIgnoreCase(subSvnName)) {
            return queryIsStorageMax(svcName, subSvnName, hsmpParam);
        }
        if ("queryLightState".equalsIgnoreCase(subSvnName)) {
            return queryLightState(svcName, subSvnName, hsmpParam);
        }
        if ("querySleepTime".equalsIgnoreCase(subSvnName)) {
            return querySleepTime(svcName, subSvnName, hsmpParam);
        }
        return null;
    }

    private HashMap queryLightState(String svcName, String subSvnName, HashMap hsmpParam) {
        try {
            CommService.getCommService().execute("QUERY_LIGHT_STATE", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private HashMap queryRecycledAmount(String svcName, String subSvnName, HashMap hsmpParam) {
        List<HashMap<String, String>> listBottleSummary = (List) ServiceGlobal.getCurrentSession("RECYCLED_BOTTLE_SUMMARY");
        HashMap hsmpPaperSummary = (HashMap) ServiceGlobal.getCurrentSession("RECYCLED_PAPER_SUMMARY");
        double dRecycledAmount = 0.0d;
        if (listBottleSummary != null) {
            for (int i = 0; i < listBottleSummary.size(); i++) {
                HashMap<String, String> hsmpBottleSummary = (HashMap) listBottleSummary.get(i);
                int VENDING_BOTTLE_COUNT = Integer.parseInt((String) hsmpBottleSummary.get("VENDING_BOTTLE_COUNT"));
                dRecycledAmount += ((double) VENDING_BOTTLE_COUNT) * Double.parseDouble((String) hsmpBottleSummary.get("BOTTLE_AMOUNT"));
            }
        }
        if (!(hsmpPaperSummary == null || StringUtils.isBlank((String) hsmpPaperSummary.get("PAPER_PRICE")))) {
            dRecycledAmount += Double.parseDouble((String) hsmpPaperSummary.get("PAPER_PRICE"));
        }
        HashMap hsmpResult = new HashMap();
        hsmpResult.put("RECYCLED_AMOUNT", Double.toString(dRecycledAmount));
        return hsmpResult;
    }

    private HashMap recycledPaperSummary(String svcName, String subSvnName, HashMap hsmpParam) {
        HashMap<String, Object> hsmpState = new HashMap();
        hsmpState.put("RECYCLED_PAPER_SUMMARY", ServiceGlobal.getCurrentSession("RECYCLED_PAPER_SUMMARY"));
        return hsmpState;
    }

    private HashMap recycledBottleSummary(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap<String, Object> hsmpState = new HashMap();
        hsmpState.put("RECYCLED_BOTTLE_SUMMARY", ServiceGlobal.getCurrentSession("RECYCLED_BOTTLE_SUMMARY"));
        return hsmpState;
    }

    private HashMap hasRecycled(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String result = "FALSE";
        HashMap hsmpPaper = (HashMap) ServiceGlobal.getCurrentSession("RECYCLED_PAPER_SUMMARY");
        if (!(hsmpPaper == null || hsmpPaper.get("PAPER_WEIGH") == null || Double.parseDouble((String) hsmpPaper.get("PAPER_WEIGH")) <= 0.0d)) {
            result = "TRUE";
        }
        List<HashMap<String, String>> listHsmpRecycleBottle = (List) ServiceGlobal.getCurrentSession("RECYCLED_BOTTLE_SUMMARY");
        int bottleNumber = 0;
        if (listHsmpRecycleBottle != null && listHsmpRecycleBottle.size() > 0) {
            for (int i = 0; i < listHsmpRecycleBottle.size(); i++) {
                bottleNumber += Integer.parseInt((String) ((HashMap) listHsmpRecycleBottle.get(i)).get("BOTTLE_COUNT"));
            }
            if (bottleNumber > 0) {
                result = "TRUE";
            } else {
                result = "FALSE";
            }
        }
        HashMap hsmpResult = new HashMap();
        hsmpResult.put("RESULT", result);
        return hsmpResult;
    }

    private HashMap hasPrinterPaper(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String status = CommService.getCommService().execute("PRINTER_CHECK", null);
        HashMap hsmpResult = new HashMap();
        if ("havePaper".equalsIgnoreCase(status)) {
            hsmpResult.put("RET_CODE", "HAVE_PAPER");
        } else if ("noPaper".equalsIgnoreCase(status)) {
            hsmpResult.put("RET_CODE", "NO_PAPER");
        } else {
            hsmpResult.put("RET_CODE", "ERROR_UNKNOWN");
        }
        return hsmpResult;
    }

    private HashMap queryStorageCount(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringIn("SYS_CODE_KEY", new String[]{"STORAGE_CURR_COUNT", "STORAGE_CURR_COUNT_DELTA"});
        CommTable commTableRvmSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        HashMap<String, Object> hsmpResult = new HashMap();
        hsmpResult.put("STORAGE_CURR_COUNT", "0");
        hsmpResult.put("STORAGE_CURR_COUNT_DELTA", "0");
        for (int i = 0; i < commTableRvmSysCode.getRecordCount(); i++) {
            CommTableRecord ctr = commTableRvmSysCode.getRecord(i);
            hsmpResult.put(ctr.get("SYS_CODE_KEY"), ctr.get("SYS_CODE_VALUE"));
        }
        String putdownCount = "0";
        String putdownCountDelta = "0";
        String putdownCountReal = "0";
        try {
            putdownCount = (String) hsmpResult.get("STORAGE_CURR_COUNT");
            putdownCountDelta = (String) hsmpResult.get("STORAGE_CURR_COUNT_DELTA");
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
        hsmpResult.put("BOTTLE_NOW", putdownCountReal);
        return hsmpResult;
    }

    private HashMap queryStorageWeight(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "STORAGE_CURR_PAPER_WEIGH");
        CommTable commTableRvmSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        HashMap<String, Object> hsmpResult = new HashMap();
        if (commTableRvmSysCode.getRecordCount() == 0) {
            hsmpResult.put("STORAGE_CURR_PAPER_WEIGH", "0");
        } else {
            hsmpResult.put("STORAGE_CURR_PAPER_WEIGH", commTableRvmSysCode.getRecord(0).get("SYS_CODE_VALUE"));
        }
        return hsmpResult;
    }

    private HashMap queryStorageMax(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilderRvmAlarm = new SqlWhereBuilder();
        sqlWhereBuilderRvmAlarm.addNumberIn("ALARM_ID", Integer.valueOf(11));
        CommTable commTableRvmAlarm = dbQuery.getCommTable("select * from RVM_ALARM" + sqlWhereBuilderRvmAlarm.toSqlWhere("where"));
        HashMap<String, Object> hsmpResult = new HashMap();
        if (commTableRvmAlarm.getRecordCount() > 0) {
            hsmpResult.put("STORAGE_MAX_COUNT", commTableRvmAlarm.getRecord(0).get("TSD_VALUE"));
        }
        return hsmpResult;
    }

    private HashMap queryMedia(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        CommTable commTable = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase()).getCommTable("select * from RVM_MEDIA");
        HashMap<String, Object> hsmpResult = new HashMap();
        hsmpResult.put("RVM_MEDIA_LIST", JSONUtils.toList(commTable));
        return hsmpResult;
    }

    private HashMap queryTextAd(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        Date tDate = new Date();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addDatetimeLessEqualsTo("BEGIN_TIME", tDate).addDatetimeGreaterEqualsTo("END_TIME", tDate);
        CommTable commTable = dbQuery.getCommTable("select * from RVM_TXT_AD" + sqlWhereBuilder.toSqlWhere("where"));
        HashMap<String, Object> hsmpResult = new HashMap();
        hsmpResult.put("RVM_TEXT_AD_LIST", JSONUtils.toList(commTable));
        return hsmpResult;
    }

    private HashMap queryStorageWeightMax(String svcName, String subSvnName, HashMap hsmpParam) {
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilderRvmAlarm = new SqlWhereBuilder();
        sqlWhereBuilderRvmAlarm.addNumberIn("ALARM_ID", Integer.valueOf(13));
        CommTable commTableRvmAlarm = dbQuery.getCommTable("select * from RVM_ALARM" + sqlWhereBuilderRvmAlarm.toSqlWhere("where"));
        HashMap<String, Object> hsmpResult = new HashMap();
        if (commTableRvmAlarm.getRecordCount() > 0) {
            hsmpResult.put("STORAGE_MAX_PAPER", commTableRvmAlarm.getRecord(0).get("TSD_VALUE"));
        }
        return hsmpResult;
    }

    private HashMap queryServiceEnable(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String SERVICE_NAME = (String) hsmpParam.get("SERVICE_NAME");
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "SERVICE_DISABLED_SET");
        CommTable commTableSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        String SYS_CODE_VALUE = "";
        if (commTableSysCode.getRecordCount() > 0) {
            SYS_CODE_VALUE = commTableSysCode.getRecord(0).get("SYS_CODE_VALUE");
        }
        boolean isEnableRecycle = false;
        int i;
        String[] service_set;
        if ("BOTTLE".equalsIgnoreCase(SERVICE_NAME) || "PAPER".equalsIgnoreCase(SERVICE_NAME)) {
            if (!StringUtils.isBlank(SysConfig.get("RECYCLE.SERVICE.SET"))) {
                String[] RECYCLE_SERVICE_SET_LIST = SysConfig.get("RECYCLE.SERVICE.SET").split(",");
                for (String equalsIgnoreCase : RECYCLE_SERVICE_SET_LIST) {
                    if (equalsIgnoreCase.equalsIgnoreCase(SERVICE_NAME)) {
                        isEnableRecycle = true;
                        break;
                    }
                }
            }
            if (!StringUtils.isBlank(SYS_CODE_VALUE)) {
                service_set = SYS_CODE_VALUE.split(",");
                i = 0;
                while (i < service_set.length) {
                    if (isEnableRecycle && service_set[i].equalsIgnoreCase(SERVICE_NAME)) {
                        isEnableRecycle = false;
                    }
                    i++;
                }
            }
            if (isEnableRecycle && "BOTTLE".equalsIgnoreCase(SERVICE_NAME)) {
                HashMap<String, Object> hsmpResultStorageCount = execute("GUIQueryCommonService", "queryStorageCount", null);
                HashMap<String, Object> hsmpResultStorageMax = execute("GUIQueryCommonService", "queryStorageMax", null);
                if (!(hsmpResultStorageMax == null || hsmpResultStorageCount == null || hsmpResultStorageMax.get("STORAGE_MAX_COUNT") == null || hsmpResultStorageCount.get("STORAGE_CURR_COUNT") == null)) {
                    int STORAGE_MAX_COUNT = Double.valueOf((String) hsmpResultStorageMax.get("STORAGE_MAX_COUNT")).intValue();
                    int STORAGE_CURR_COUNT = Integer.parseInt((String) hsmpResultStorageCount.get("STORAGE_CURR_COUNT"));
                    if (STORAGE_MAX_COUNT > 0 && STORAGE_CURR_COUNT > 0 && STORAGE_CURR_COUNT >= STORAGE_MAX_COUNT) {
                        isEnableRecycle = false;
                    }
                }
            }
            if (isEnableRecycle && "PAPER".equalsIgnoreCase(SERVICE_NAME)) {
                HashMap<String, Object> hsmpResultStorageWeight = execute("GUIQueryCommonService", "queryStorageWeight", null);
                HashMap<String, Object> hsmpResultStoragePaperMax = execute("GUIQueryCommonService", "queryStorageWeightMax", null);
                if (!(hsmpResultStoragePaperMax == null || hsmpResultStorageWeight == null || hsmpResultStoragePaperMax.get("STORAGE_MAX_PAPER") == null || hsmpResultStorageWeight.get("STORAGE_CURR_PAPER_WEIGH") == null)) {
                    double STORAGE_MAX_WEIGHT = Double.valueOf((String) hsmpResultStoragePaperMax.get("STORAGE_MAX_PAPER")).doubleValue();
                    double STORAGE_CURR_WEIGHT = Double.valueOf((String) hsmpResultStorageWeight.get("STORAGE_CURR_PAPER_WEIGH")).doubleValue();
                    if (STORAGE_MAX_WEIGHT > 0.0d && STORAGE_CURR_WEIGHT > 0.0d && STORAGE_CURR_WEIGHT >= STORAGE_MAX_WEIGHT) {
                        isEnableRecycle = false;
                    }
                }
            }
        } else {
            String VENDING_WAY = SysConfig.get("VENDING.WAY");
            if (!StringUtils.isBlank(VENDING_WAY)) {
                String[] VENDING_WAY_ARRAY = VENDING_WAY.split(";");
                for (String equalsIgnoreCase2 : VENDING_WAY_ARRAY) {
                    if (equalsIgnoreCase2.equalsIgnoreCase(SERVICE_NAME)) {
                        isEnableRecycle = true;
                        break;
                    }
                }
            }
            service_set = SYS_CODE_VALUE.split(",");
            i = 0;
            while (i < service_set.length) {
                if (isEnableRecycle && service_set[i].equalsIgnoreCase(SERVICE_NAME)) {
                    isEnableRecycle = false;
                }
                i++;
            }
        }
        HashMap hsmpResult = new HashMap();
        hsmpResult.put("SERVICE_ENABLE", isEnableRecycle ? "TRUE" : "FALSE");
        return hsmpResult;
    }

    private HashMap queryServiceDisable(String svcName, String subSvnName, HashMap hsmpParam) {
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "SERVICE_DISABLED_SET");
        CommTable commTableSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        String SYS_CODE_VALUE = "";
        if (commTableSysCode.getRecordCount() > 0) {
            SYS_CODE_VALUE = commTableSysCode.getRecord(0).get("SYS_CODE_VALUE");
        }
        HashMap<String, Object> hsmpResult = new HashMap();
        hsmpResult.put("SERVICE_DISABLED", SYS_CODE_VALUE);
        return hsmpResult;
    }

    private HashMap queryIsStorageMax(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        boolean isStorageMax = false;
        boolean isStorageWeightMax = false;
        String PRODUCT_TYPE = null;
        if (hsmpParam != null) {
            PRODUCT_TYPE = (String) hsmpParam.get("PRODUCT_TYPE");
        }
        if ("BOTTLE".equals(PRODUCT_TYPE)) {
            HashMap<String, Object> hsmpResultStorageCount = execute("GUIQueryCommonService", "queryStorageCount", null);
            HashMap<String, Object> hsmpResultStorageMax = execute("GUIQueryCommonService", "queryStorageMax", null);
            if (!(hsmpResultStorageMax == null || hsmpResultStorageCount == null || hsmpResultStorageMax.get("STORAGE_MAX_COUNT") == null || hsmpResultStorageCount.get("STORAGE_CURR_COUNT") == null)) {
                int STORAGE_MAX_COUNT = Double.valueOf((String) hsmpResultStorageMax.get("STORAGE_MAX_COUNT")).intValue();
                int STORAGE_CURR_COUNT = Integer.parseInt((String) hsmpResultStorageCount.get("STORAGE_CURR_COUNT"));
                if (STORAGE_MAX_COUNT > 0 && STORAGE_CURR_COUNT > 0 && STORAGE_CURR_COUNT >= STORAGE_MAX_COUNT) {
                    isStorageMax = true;
                }
            }
        }
        if ("PAPER".equals(PRODUCT_TYPE)) {
            HashMap<String, Object> hsmpResultStorageWeight = execute("GUIQueryCommonService", "queryStorageWeight", null);
            HashMap<String, Object> hsmpResultStoragePaperMax = execute("GUIQueryCommonService", "queryStorageWeightMax", null);
            if (!(hsmpResultStoragePaperMax == null || hsmpResultStorageWeight == null || hsmpResultStoragePaperMax.get("STORAGE_MAX_PAPER") == null || hsmpResultStorageWeight.get("STORAGE_CURR_PAPER_WEIGH") == null)) {
                double STORAGE_MAX_WEIGHT = Double.valueOf((String) hsmpResultStoragePaperMax.get("STORAGE_MAX_PAPER")).doubleValue();
                double STORAGE_CURR_WEIGHT = Double.valueOf((String) hsmpResultStorageWeight.get("STORAGE_CURR_PAPER_WEIGH")).doubleValue();
                if (STORAGE_MAX_WEIGHT > 0.0d && STORAGE_CURR_WEIGHT > 0.0d && STORAGE_CURR_WEIGHT >= STORAGE_MAX_WEIGHT) {
                    isStorageWeightMax = true;
                }
            }
        }
        HashMap<String, Object> hsmpResult = new HashMap();
        hsmpResult.put("IS_MAX_COUNT", Boolean.valueOf(isStorageMax));
        hsmpResult.put("IS_MAX_PAPER", Boolean.valueOf(isStorageWeightMax));
        return hsmpResult;
    }

    private HashMap querySleepTime(String svcName, String subSvnName, HashMap hsmpParam) {
        HashMap hashmap = new HashMap();
        hashmap.put("RESULT", "FALSE");
        try {
            DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
            SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "SLEEP_TIME");
            CommTable commTable = dbQuery.getCommTable("select * from RVM_SYS_CODE " + sqlWhereBuilder.toSqlWhere("where"));
            if (commTable.getRecordCount() != 0) {
                try {
                    int i;
                    int i2;
                    List sleepList = JSONUtils.toList(commTable.getRecord(0).get("SYS_CODE_VALUE"));
                    if (sleepList != null) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    if (sleepList.size() > 0) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    if ((i2 & i) != 0) {
                        Date nowDate = new Date();
                        int nowTotalTime = (nowDate.getHours() * 60) + nowDate.getMinutes();
                        for (int i3 = 0; i3 < sleepList.size(); i3++) {
                            HashMap tmMap = (HashMap) sleepList.get(i3);
                            if (!(tmMap == null || tmMap.isEmpty())) {
                                String beginTime = (String) tmMap.keySet().iterator().next();
                                String endTime = (String) tmMap.get(beginTime);
                                int totalBeginTime = 0;
                                int totalEndTime = 0;
                                if (!(StringUtils.isBlank(beginTime) || StringUtils.isBlank(endTime))) {
                                    if (beginTime.length() == 6 && beginTime.matches("\\d{6}")) {
                                        String[] bTime = new String[3];
                                        bTime[0] = beginTime.substring(0, 2);
                                        bTime[1] = beginTime.substring(2, 4);
                                        totalBeginTime = (Integer.parseInt(bTime[0]) * 60) + Integer.parseInt(bTime[1]);
                                    }
                                    String[] eTime = new String[3];
                                    if (endTime.length() == 6 && endTime.matches("\\d{6}")) {
                                        eTime[0] = endTime.substring(0, 2);
                                        eTime[1] = endTime.substring(2, 4);
                                        totalEndTime = (Integer.parseInt(eTime[0]) * 60) + Integer.parseInt(eTime[1]);
                                    }
                                    if (nowTotalTime >= totalBeginTime && nowTotalTime < totalEndTime) {
                                        hashmap.put("RESULT", "TRUE");
                                        hashmap.put("END_TIME", eTime[0] + ":" + eTime[1]);
                                    }
                                    hashmap.put("SLEEP_LIST", sleepList);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e2) {
        }
        return hashmap;
    }
}
