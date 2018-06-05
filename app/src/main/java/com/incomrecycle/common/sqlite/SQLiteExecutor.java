package com.incomrecycle.common.sqlite;

import android.database.sqlite.SQLiteDatabase;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.sync.GlobalLock;
import com.incomrecycle.common.utils.StringUtils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SQLiteExecutor {
    private static final Logger logger = LoggerFactory.getLogger("SQLException");

    public static void execSqlBuilder(SQLiteDatabase sqliteDatabase, List<SqlBuilder> listSqlBuilder) {
        if (listSqlBuilder != null && listSqlBuilder.size() != 0) {
            GlobalLock.lock(sqliteDatabase.getPath());
            try {
                sqliteDatabase.beginTransaction();
                for (int i = 0; i < listSqlBuilder.size(); i++) {
                    String sql = ((SqlBuilder) listSqlBuilder.get(i)).toSql();
                    if (!StringUtils.isEmpty(sql)) {
                        dbExecSql(sqliteDatabase, sql);
                    }
                }
                sqliteDatabase.setTransactionSuccessful();
                sqliteDatabase.endTransaction();
                GlobalLock.unlock(sqliteDatabase.getPath());
            } catch (Throwable th) {
                GlobalLock.unlock(sqliteDatabase.getPath());
            }
        }
    }

    public static void execSql(SQLiteDatabase sqliteDatabase, List<String> listSql) {
        if (listSql != null && listSql.size() != 0) {
            GlobalLock.lock(sqliteDatabase.getPath());
            try {
                sqliteDatabase.beginTransaction();
                for (int i = 0; i < listSql.size(); i++) {
                    String sql = (String) listSql.get(i);
                    if (!StringUtils.isEmpty(sql)) {
                        dbExecSql(sqliteDatabase, sql);
                    }
                }
                sqliteDatabase.setTransactionSuccessful();
                sqliteDatabase.endTransaction();
                GlobalLock.unlock(sqliteDatabase.getPath());
            } catch (Throwable th) {
                GlobalLock.unlock(sqliteDatabase.getPath());
            }
        }
    }

    public static void execSql(SQLiteDatabase sqliteDatabase, String sql) {
        if (!StringUtils.isEmpty(sql)) {
            GlobalLock.lock(sqliteDatabase.getPath());
            try {
                sqliteDatabase.beginTransaction();
                dbExecSql(sqliteDatabase, sql);
                sqliteDatabase.setTransactionSuccessful();
                sqliteDatabase.endTransaction();
                GlobalLock.unlock(sqliteDatabase.getPath());
            } catch (Throwable th) {
                GlobalLock.unlock(sqliteDatabase.getPath());
            }
        }
    }

    private static void dbExecSql(SQLiteDatabase sqliteDatabase, String sqlScript) {
        List<String> listSql = sqlParser(sqlScript);
        if (listSql != null) {
            int i = 0;
            while (i < listSql.size()) {
                String sql = null;
                try {
                    sql = (String) listSql.get(i);
                    sqliteDatabase.execSQL(sql);
                    if (!true) {
                        logger.debug("SQL:" + sql);
                    }
                    i++;
                } catch (Throwable th) {
                    if (!false) {
                        logger.debug("SQL:" + sql);
                    }
                }
            }
        }
    }

    private static List<String> sqlParser(String sqlScript) {
        InputStream bais = new ByteArrayInputStream(sqlScript.getBytes());
        try {
            List<String> sqlParser = sqlParser(bais);
            return sqlParser;
        } finally {
            try {
                bais.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<String> sqlParser(InputStream input) {
        List<String> listSql = new ArrayList();
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        StringBuffer sb = new StringBuffer();
        String nextline = null;
        boolean memo = false;
        boolean isClosed = true;
        String tail = ";";
        while (true) {
            try {
                nextline = readerLine(br, nextline);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (nextline == null) {
                nextline = tail;
                tail = null;
            }
            if (nextline == null) {
                return listSql;
            }
            String line = nextline;
            nextline = null;
            int len = line.length();
            int t = 0;
            int s = 0;
            while (t < len) {
                if (memo) {
                    int memoend = line.indexOf("*/", t);
                    if (memoend == -1) {
                        s = len;
                        t = len;
                        break;
                    }
                    memo = false;
                    s = memoend + 2;
                    t = s;
                    sb.append(" ");
                } else {
                    char c = line.charAt(t);
                    if (c == '\'') {
                        isClosed = !isClosed;
                        t++;
                    } else if (!isClosed) {
                        t++;
                    } else if (c == ';') {
                        sb.append(line.substring(s, t));
                        String sql = sb.toString().trim();
                        sb.setLength(0);
                        if (sql.length() > 0) {
                            listSql.add(sql);
                        }
                        t++;
                        s = t;
                    } else if (c == '/') {
                        t++;
                        if (t >= len) {
                            continue;
                        } else if (line.charAt(t) == '*') {
                            sb.append(line.substring(s, t - 1));
                            memo = true;
                            t++;
                        } else if (line.charAt(t) == '/') {
                            sb.append(line.substring(s, t - 1));
                            sb.append(" ");
                            t = len;
                            s = len;
                            break;
                        }
                    } else {
                        t++;
                    }
                }
            }
            if (!(memo || s == t)) {
                sb.append(line.substring(s, t));
                if (sb.length() > 0) {
                    sb.append("\n");
                }
            }
        }
    }

    private static String readerLine(BufferedReader br, String nextline) throws Exception {
        return nextline != null ? nextline : br.readLine();
    }
}
