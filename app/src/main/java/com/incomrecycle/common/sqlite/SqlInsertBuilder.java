package com.incomrecycle.common.sqlite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SqlInsertBuilder implements SqlBuilder {
    private HashMap<String, String> hsmpColumn = new HashMap();
    private List<HashMap<String, String>> listRecord = new ArrayList();
    private String table = null;

    public static class SqlInsertRecord {
        HashMap<String, String> hsmpRecord;
        SqlInsertBuilder sqlInsertBuilder;

        private SqlInsertRecord(SqlInsertBuilder sqlInsertBuilder, HashMap<String, String> hsmpRecord) {
            this.sqlInsertBuilder = sqlInsertBuilder;
            this.hsmpRecord = hsmpRecord;
        }

        public SqlInsertRecord setExpression(String fieldName, String value) {
            if (value != null) {
                value = value.trim();
                if (value.length() > 0) {
                    this.sqlInsertBuilder.hsmpColumn.put(fieldName.toUpperCase(), fieldName);
                    this.hsmpRecord.put(fieldName.toUpperCase(), value);
                }
            }
            return this;
        }

        public SqlInsertRecord setString(String fieldName, Object value) {
            if (value != null) {
                this.sqlInsertBuilder.hsmpColumn.put(fieldName.toUpperCase(), fieldName);
                this.hsmpRecord.put(fieldName.toUpperCase(), "'" + value.toString().replaceAll("'", "''") + "'");
            }
            return this;
        }

        public SqlInsertRecord setNumber(String fieldName, Object value) {
            if (value != null) {
                String v = value.toString().trim();
                if (v.length() > 0) {
                    this.sqlInsertBuilder.hsmpColumn.put(fieldName.toUpperCase(), fieldName);
                    this.hsmpRecord.put(fieldName.toUpperCase(), v);
                }
            }
            return this;
        }

        public SqlInsertRecord setDateTime(String fieldName, Date value) {
            if (value != null) {
                this.sqlInsertBuilder.hsmpColumn.put(fieldName.toUpperCase(), fieldName);
                this.hsmpRecord.put(fieldName.toUpperCase(), "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value) + "'");
            }
            return this;
        }

        public SqlInsertRecord setDate(String fieldName, Date value) {
            if (value != null) {
                this.sqlInsertBuilder.hsmpColumn.put(fieldName.toUpperCase(), fieldName);
                this.hsmpRecord.put(fieldName.toUpperCase(), "'" + new SimpleDateFormat("yyyy-MM-dd").format(value) + "'");
            }
            return this;
        }
    }

    public SqlInsertBuilder(String table) {
        this.table = table;
    }

    public SqlInsertRecord newInsertRecord() {
        HashMap<String, String> hsmpRecord = new HashMap();
        this.listRecord.add(hsmpRecord);
        return new SqlInsertRecord(this, hsmpRecord);
    }

    public String toSql() {
        if (this.table == null || this.listRecord.size() == 0) {
            return null;
        }
        List<String> listColumn = new ArrayList();
        for (String add : this.hsmpColumn.keySet()) {
            listColumn.add(add);
        }
        if (listColumn.size() == 0) {
            return null;
        }
        int i;
        StringBuffer sbField = new StringBuffer();
        sbField.append("(");
        for (i = 0; i < listColumn.size(); i++) {
            if (i > 0) {
                sbField.append(",");
            }
            sbField.append((String) listColumn.get(i));
        }
        sbField.append(")");
        StringBuffer sbSql = new StringBuffer();
        for (i = 0; i < this.listRecord.size(); i++) {
            StringBuffer sbValue = new StringBuffer();
            HashMap<String, String> hsmpBuilder = (HashMap) this.listRecord.get(i);
            sbValue.append("(");
            for (int c = 0; c < listColumn.size(); c++) {
                if (c > 0) {
                    sbValue.append(",");
                }
                if (hsmpBuilder.get(listColumn.get(c)) == null) {
                    sbValue.append("null");
                } else {
                    sbValue.append((String) hsmpBuilder.get(listColumn.get(c)));
                }
            }
            sbValue.append(")");
            sbSql.append("insert into " + this.table + sbField.toString() + " values " + sbValue.toString() + ";");
        }
        return sbSql.toString();
    }

    private String toSql2() {
        if (this.table == null || this.listRecord.size() == 0) {
            return null;
        }
        List<String> listColumn = new ArrayList();
        for (String add : this.hsmpColumn.keySet()) {
            listColumn.add(add);
        }
        if (listColumn.size() == 0) {
            return null;
        }
        int i;
        StringBuffer sbField = new StringBuffer();
        sbField.append("(");
        for (i = 0; i < listColumn.size(); i++) {
            if (i > 0) {
                sbField.append(",");
            }
            sbField.append((String) listColumn.get(i));
        }
        sbField.append(")");
        StringBuffer sbValue = new StringBuffer();
        for (i = 0; i < this.listRecord.size(); i++) {
            HashMap<String, String> hsmpBuilder = (HashMap) this.listRecord.get(i);
            if (i > 0) {
                sbValue.append(",");
            }
            sbValue.append("(");
            for (int c = 0; c < listColumn.size(); c++) {
                if (c > 0) {
                    sbValue.append(",");
                }
                if (hsmpBuilder.get(listColumn.get(c)) == null) {
                    sbValue.append("null");
                } else {
                    sbValue.append((String) hsmpBuilder.get(listColumn.get(c)));
                }
            }
            sbValue.append(")\n");
        }
        return "insert into " + this.table + sbField.toString() + " values " + sbValue.toString();
    }

    public void clearSql() {
        this.listRecord.clear();
    }
}
