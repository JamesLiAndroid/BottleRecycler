package com.incomrecycle.prms.rvm.service.dbcallback;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.sqlite.DatabaseInitCallback;
import com.incomrecycle.common.sqlite.SQLiteExecutor;

public class SYSDBInitCallback implements DatabaseInitCallback {
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE T_SEQUENCE(SEQ_NAME VARCHAR(60) PRIMARY KEY, SEQ_VALUE INTEGER, SEQ_STEP INTEGER, SEQ_MIN INTEGER, SEQ_MAX INTEGER)");
        SQLiteExecutor.execSql(db, " INSERT INTO T_SEQUENCE(SEQ_NAME,SEQ_VALUE,SEQ_STEP,SEQ_MIN,SEQ_MAX) VALUES('OPT_ID',0,1,0,99999999); INSERT INTO T_SEQUENCE(SEQ_NAME,SEQ_VALUE,SEQ_STEP,SEQ_MIN,SEQ_MAX) VALUES('USER_ID',0,1,0,99999999); INSERT INTO T_SEQUENCE(SEQ_NAME,SEQ_VALUE,SEQ_STEP,SEQ_MIN,SEQ_MAX) VALUES('VOUCHER_SEQ',0,1,0,99999999); INSERT INTO T_SEQUENCE(SEQ_NAME,SEQ_VALUE,SEQ_STEP,SEQ_MIN,SEQ_MAX) VALUES('LOG_SEQ',0,1,0,99999999); INSERT INTO T_SEQUENCE(SEQ_NAME,SEQ_VALUE,SEQ_STEP,SEQ_MIN,SEQ_MAX) VALUES('ALARM_INST_ID',0,1,0,99999999); INSERT INTO T_SEQUENCE(SEQ_NAME,SEQ_VALUE,SEQ_STEP,SEQ_MIN,SEQ_MAX) VALUES('BAR_CODE_ATTR_ID',0,1,0,99999999);");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
