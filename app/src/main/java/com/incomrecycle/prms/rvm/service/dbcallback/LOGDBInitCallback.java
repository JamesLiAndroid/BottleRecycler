package com.incomrecycle.prms.rvm.service.dbcallback;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.sqlite.DatabaseInitCallback;

public class LOGDBInitCallback implements DatabaseInitCallback {
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE RVM_LOG(LOG_SEQ INTEGER NOT NULL, LOG_TIME VARCHAR(20), USER_ID INTEGER, OPT_TYPE VARCHAR(60), OPT_INFO VARCHAR(500), PRIMARY KEY(LOG_SEQ))");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
