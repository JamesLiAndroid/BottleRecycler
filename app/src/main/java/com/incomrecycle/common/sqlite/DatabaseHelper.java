package com.incomrecycle.common.sqlite;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.utils.FileUtils;
import com.incomrecycle.common.utils.StringUtils;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static HashMap<String, String> hsmp = new HashMap();
    private Context context;
    private DatabaseInitCallback databaseCallback;
    private int finalVersion;
    Logger logger;
    private String name;

    public DatabaseHelper(Context context, String name, DatabaseInitCallback databaseCallback) {
        super(context, name, null, 1);
        this.logger = LoggerFactory.getLogger("DB");
        this.finalVersion = 1;
        this.context = null;
        this.name = null;
        this.finalVersion = 1;
        this.databaseCallback = databaseCallback;
        this.context = context;
        this.name = name;
    }

    public DatabaseHelper(Context context, String name, int version, DatabaseInitCallback databaseCallback) {
        super(context, name, null, version);
        this.logger = LoggerFactory.getLogger("DB");
        this.finalVersion = 1;
        this.context = null;
        this.name = null;
        this.finalVersion = version;
        this.databaseCallback = databaseCallback;
        this.context = context;
        this.name = name;
    }

    public void onCreate(SQLiteDatabase db) {
        if (this.databaseCallback != null) {
            try {
                this.databaseCallback.onCreate(db);
                this.databaseCallback.onUpgrade(db, 0, this.finalVersion);
            } catch (SQLException e) {
                this.logger.debug(e);
                e.printStackTrace();
            }
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (this.databaseCallback != null) {
            this.databaseCallback.onUpgrade(db, oldVersion, newVersion);
        }
    }

    public SQLiteDatabase getWritableDatabase() {
        try {
            SQLiteDatabase sqliteDatabase = super.getWritableDatabase();
            if (!StringUtils.isEmpty((String) hsmp.get(this.name))) {
                return sqliteDatabase;
            }
            String temp_store_directory = "";
            if (this.context != null) {
                temp_store_directory = this.context.getCacheDir().getPath();
            }
            FileUtils.mkdir(temp_store_directory);
            try {
                sqliteDatabase.execSQL("PRAGMA temp_store = FILE");
                sqliteDatabase.execSQL("PRAGMA temp_store_directory = '" + temp_store_directory + "'");
                hsmp.put(this.name, this.name);
                return sqliteDatabase;
            } catch (Exception e) {
                return sqliteDatabase;
            }
        } catch (Exception e2) {
            this.logger.debug(e2);
            return null;
        }
    }
}
