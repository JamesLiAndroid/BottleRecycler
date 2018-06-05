package com.incomrecycle.common.sqlite;

import android.database.sqlite.SQLiteDatabase;

public interface DatabaseInitCallback {
    void onCreate(SQLiteDatabase sQLiteDatabase);

    void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2);
}
