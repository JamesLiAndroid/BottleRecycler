package com.incomrecycle.prms.rvm.service.commonservice;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.sqlite.DBQuery;
import com.incomrecycle.common.sqlite.DBSequence;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlBuilder;
import com.incomrecycle.common.sqlite.SqlDeleteBuilder;
import com.incomrecycle.common.sqlite.SqlInsertBuilder;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AlarmId;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.task.action.RCCInstanceTask;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public abstract class BaseAppCommonService implements AppCommonService {
    private void updateSingleAlarm(String alarmId, int alarmStatus, String guiEventCmd) throws Exception {
        String ALARM_INST_ID;
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderRvmAlarmInst = new SqlWhereBuilder();
        sqlWhereBuilderRvmAlarmInst.addNumberEqualsTo("ALARM_ID", alarmId);
        CommTable commTableRvmAlarmInst = dbQuery.getCommTable("select * from RVM_ALARM_INST" + sqlWhereBuilderRvmAlarmInst.toSqlWhere("where"));
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        List<HashMap<String, String>> listInstanceTask = new ArrayList();
        if (commTableRvmAlarmInst.getRecordCount() == 0) {
            ALARM_INST_ID = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS")).getSeq("ALARM_INST_ID");
        } else if (commTableRvmAlarmInst.getRecordCount() != 1 || alarmStatus != Integer.parseInt(commTableRvmAlarmInst.getRecord(0).get("ALARM_STATUS"))) {
            ALARM_INST_ID = commTableRvmAlarmInst.getRecord(commTableRvmAlarmInst.getRecordCount() - 1).get("ALARM_INST_ID");
            listSqlBuilder.add(new SqlDeleteBuilder("RVM_ALARM_INST").setSqlWhere(sqlWhereBuilderRvmAlarmInst));
        } else {
            return;
        }
        SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_ALARM_INST");
        sqlInsertBuilder.newInsertRecord().setNumber("ALARM_INST_ID", ALARM_INST_ID).setNumber("ALARM_TYPE", "0").setDateTime("ALARM_TIME", new Date()).setNumber("ALARM_ID", alarmId).setNumber("UPLOAD_FLAG", "0").setNumber("ALARM_STATUS", Integer.valueOf(alarmStatus));
        listSqlBuilder.add(sqlInsertBuilder);
        HashMap<String, String> hsmpInstanceTask = new HashMap();
        hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
        hsmpInstanceTask.put("ALARM_INST_ID", ALARM_INST_ID);
        hsmpInstanceTask.put("ALARM_TYPE", "0");
        listInstanceTask.add(hsmpInstanceTask);
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        for (int i = 0; i < listInstanceTask.size(); i++) {
            RCCInstanceTask.addTask((HashMap) listInstanceTask.get(i));
        }
        if (!StringUtils.isBlank(guiEventCmd)) {
            HashMap<String, String> hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "Application");
            hsmpGUIEvent.put("EVENT", "CMD");
            hsmpGUIEvent.put("CMD", guiEventCmd);
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        }
    }

    public void raiseAlarm(String alarmId, String guiEventCmd) throws Exception {
        if (AlarmId.isSingleAlarm(alarmId)) {
            updateSingleAlarm(alarmId, 1, guiEventCmd);
            return;
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderRvmAlarmInst = new SqlWhereBuilder();
        sqlWhereBuilderRvmAlarmInst.addNumberIn("ALARM_STATUS", (Object[]) new Object[]{Integer.valueOf(1), Integer.valueOf(2)}).addNumberEqualsTo("ALARM_ID", alarmId);
        CommTable commTableRvmAlarmInst = dbQuery.getCommTable("select * from RVM_ALARM_INST" + sqlWhereBuilderRvmAlarmInst.toSqlWhere("where"));
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        List<HashMap<String, String>> listInstanceTask = new ArrayList();
        if (commTableRvmAlarmInst.getRecordCount() == 0) {
            String ALARM_INST_ID = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS")).getSeq("ALARM_INST_ID");
            SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_ALARM_INST");
            sqlInsertBuilder.newInsertRecord().setNumber("ALARM_INST_ID", ALARM_INST_ID).setNumber("ALARM_TYPE", "0").setDateTime("ALARM_TIME", new Date()).setNumber("ALARM_ID", alarmId).setNumber("UPLOAD_FLAG", "0").setNumber("ALARM_STATUS", Integer.valueOf(1));
            listSqlBuilder.add(sqlInsertBuilder);
            HashMap<String, String> hsmpInstanceTask = new HashMap();
            hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
            hsmpInstanceTask.put("ALARM_INST_ID", ALARM_INST_ID);
            hsmpInstanceTask.put("ALARM_TYPE", "0");
            listInstanceTask.add(hsmpInstanceTask);
        }
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        for (int i = 0; i < listInstanceTask.size(); i++) {
            RCCInstanceTask.addTask((HashMap) listInstanceTask.get(i));
        }
        if (!StringUtils.isBlank(guiEventCmd)) {
            HashMap<String, String> hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "Application");
            hsmpGUIEvent.put("EVENT", "CMD");
            hsmpGUIEvent.put("CMD", guiEventCmd);
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        }
    }

    public void recoverAlarm(String alarmId, String guiEventCmd) throws Exception {
        if (AlarmId.isSingleAlarm(alarmId)) {
            updateSingleAlarm(alarmId, 4, guiEventCmd);
            return;
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderRvmAlarmInst = new SqlWhereBuilder();
        sqlWhereBuilderRvmAlarmInst.addNumberIn("ALARM_STATUS", Integer.valueOf(1), Integer.valueOf(2)).addNumberEqualsTo("ALARM_ID", alarmId).addNumberEqualsTo("ALARM_TYPE", "0");
        CommTable commTableRvmAlarmInst = dbQuery.getCommTable("select * from RVM_ALARM_INST" + sqlWhereBuilderRvmAlarmInst.toSqlWhere("where"));
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        List<HashMap<String, String>> listInstanceTask = new ArrayList();
        if (commTableRvmAlarmInst.getRecordCount() != 0) {
            String ALARM_INST_ID = commTableRvmAlarmInst.getRecord(0).get("ALARM_INST_ID");
            sqlWhereBuilderRvmAlarmInst = new SqlWhereBuilder();
            sqlWhereBuilderRvmAlarmInst.addNumberIn("ALARM_STATUS", Integer.valueOf(1), Integer.valueOf(2)).addNumberEqualsTo("ALARM_ID", alarmId).addNumberEqualsTo("ALARM_INST_ID", ALARM_INST_ID).addNumberEqualsTo("ALARM_TYPE", "0");
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_ALARM_INST");
            sqlUpdateBuilder.setNumber("ALARM_STATUS", Integer.valueOf(4)).setNumber("UPLOAD_FLAG", "0").setSqlWhere(sqlWhereBuilderRvmAlarmInst);
            listSqlBuilder.add(sqlUpdateBuilder);
            HashMap<String, String> hsmpInstanceTask = new HashMap();
            hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_ALARM_INST");
            hsmpInstanceTask.put("ALARM_INST_ID", ALARM_INST_ID);
            hsmpInstanceTask.put("ALARM_TYPE", "0");
            listInstanceTask.add(hsmpInstanceTask);
        }
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        for (int i = 0; i < listInstanceTask.size(); i++) {
            RCCInstanceTask.addTask((HashMap) listInstanceTask.get(i));
        }
        if (!StringUtils.isBlank(guiEventCmd)) {
            HashMap<String, String> hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "Application");
            hsmpGUIEvent.put("EVENT", "CMD");
            hsmpGUIEvent.put("CMD", guiEventCmd);
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        }
    }
}
