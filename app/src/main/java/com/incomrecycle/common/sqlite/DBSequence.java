package com.incomrecycle.common.sqlite;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.sync.GlobalLock;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DBSequence {
    private DatabaseHelper databaseHelper;

    private DBSequence(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public static DBSequence getInstance(DatabaseHelper databaseHelper) {
        return new DBSequence(databaseHelper);
    }

    public String getSeq(String seqName) {
        String seqValue = "";
        synchronized (this.databaseHelper) {
            SQLiteDatabase sqliteDatabase = this.databaseHelper.getWritableDatabase();
            String sPath = sqliteDatabase.getPath();
            GlobalLock.lock(sPath);
            try {
                sqliteDatabase.beginTransaction();
                sqliteDatabase.execSQL("update t_sequence set seq_value=max((seq_value + seq_step) % (seq_max + 1),seq_min) where seq_name='" + seqName.toUpperCase() + "'");
                seqValue = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from t_sequence where seq_name='" + seqName.toUpperCase() + "'").getRecord(0).get("seq_value");
                sqliteDatabase.setTransactionSuccessful();
                try {
                    sqliteDatabase.endTransaction();
                } catch (Exception e) {
                }
                GlobalLock.unlock(sPath);
            } catch (Throwable th) {
                try {
                    sqliteDatabase.endTransaction();
                } catch (Exception e2) {
                }
                GlobalLock.unlock(sPath);
            }
        }
        return seqValue;
    }

    public String getDateSeq(String seqName) {
        return formatDateSeq(getSeq(seqName), 15);
    }

    public String getDateSeq(String seqName, int iMaxLength) {
        return formatDateSeq(getSeq(seqName), iMaxLength);
    }

    private String formatDateSeq(String seqValue, int iMaxLength) {
        String seq_val = seqValue;
        if (seq_val == null) {
            return null;
        }
        if (iMaxLength <= 8) {
            return seq_val;
        }
        String strPrefix = "00000000000000000000000000000000";
        while (iMaxLength - 8 > strPrefix.length()) {
            strPrefix = strPrefix + strPrefix;
        }
        String strSeqValue = strPrefix + seq_val;
        return new SimpleDateFormat("yyyyMMdd").format(new Date()) + strSeqValue.substring(strSeqValue.length() - (iMaxLength - 8));
    }
}
