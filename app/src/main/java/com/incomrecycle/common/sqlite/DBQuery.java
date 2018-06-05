package com.incomrecycle.common.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.commtable.CommTableField;
import com.incomrecycle.common.commtable.CommTableRecord;
import com.incomrecycle.common.sync.GlobalLock;

public class DBQuery {
    private SQLiteDatabase sqliteDatabase;

    private DBQuery(SQLiteDatabase sqliteDatabase) {
        this.sqliteDatabase = sqliteDatabase;
    }

    public static DBQuery getDBQuery(SQLiteDatabase sqliteDatabase) {
        return new DBQuery(sqliteDatabase);
    }

    public static String getQuerySQL(String selectSql, RowSet rowSet) {
        if (rowSet == null) {
            return selectSql;
        }
        long lOffset = rowSet.getOffset();
        long lRows = rowSet.getRows();
        if (lRows <= 0) {
            return selectSql;
        }
        if (lOffset < 0) {
            lRows += lOffset;
            lOffset = 0;
            if (lRows < 0) {
                lRows = 0;
            }
        }
        if (lOffset == 0) {
            return "SELECT A.* FROM ( " + selectSql + " ) A LIMIT " + lRows;
        }
        return "SELECT A.* FROM ( " + selectSql + " ) A LIMIT " + lOffset + "," + lRows;
    }

    public long getRecordCount(String selectSql) {
        return Long.parseLong(getCommTable(" SELECT COUNT(*) AS TOTALROW FROM (" + selectSql + ") A").getRecord(0).get("TOTALROW"));
    }

    public boolean existRecord(String selectSql) {
        return getCommTable(new StringBuilder().append(" SELECT 1 AS EXISTSMARK FROM (").append(selectSql).append(") A LIMIT 1").toString()).getRecordCount() > 0;
    }

    protected String getForUpdateSQL(String sql) {
        return sql + " for update";
    }

    public CommTable getCommTable(String sql) {
        int i;
        Cursor cursor = null;
        GlobalLock.lock(this.sqliteDatabase.getPath());
        cursor = this.sqliteDatabase.rawQuery(sql, null);
        String[] columns = cursor.getColumnNames();
        CommTableField ctf = new CommTableField();
        for (String addField : columns) {
            ctf.addField(addField);
        }
        CommTable commTable = new CommTable(ctf);
        while (cursor.moveToNext()) {
            CommTableRecord ctr = new CommTableRecord(ctf);
            for (i = 0; i < columns.length; i++) {
                if (!cursor.isNull(i)) {
                    switch (cursor.getType(i)) {
                        case 0:
                            try {
                                ctr.set(i, null);
                                break;
                            } finally {
                                if (cursor != null) {
                                    cursor.close();
                                }
                                GlobalLock.unlock(this.sqliteDatabase.getPath());
                            }
                        case 1:
                            ctr.set(i, Integer.toString(cursor.getInt(i)));
                            break;
                        case 2:
                            ctr.set(i, Double.toString(cursor.getDouble(i)));
                            break;
                        case 4:
                            ctr.set(i, new String(cursor.getBlob(i)));
                            break;
                        default:
                            try {
                                ctr.set(i, cursor.getString(i));
                                break;
                            } catch (Exception e) {
                                break;
                            }
                    }
                }
                ctr.set(i, null);
            }
            commTable.addRecord(ctr);
        }
        return commTable;
    }
}
