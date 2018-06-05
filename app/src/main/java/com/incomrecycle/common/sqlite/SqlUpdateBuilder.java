package com.incomrecycle.common.sqlite;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class SqlUpdateBuilder implements SqlBuilder {
    private HashMap<String, String> hsmpFields = new HashMap();
    private String sqlWhere = null;
    private String table = null;

    public SqlUpdateBuilder(String table) {
        this.table = table;
    }

    public void clearSql() {
        this.hsmpFields.clear();
        this.sqlWhere = null;
    }

    public String toSql() {
        if (this.table == null || this.hsmpFields.size() == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        int count = 0;
        for (String field : this.hsmpFields.keySet()) {
            String expression = (String) this.hsmpFields.get(field);
            if (count > 0) {
                sb.append(",");
            }
            sb.append(field);
            sb.append("=");
            sb.append(expression);
            count++;
        }
        if (this.sqlWhere == null || "".equals(this.sqlWhere)) {
            return "update " + this.table + " set " + sb.toString();
        }
        return "update " + this.table + " set " + sb.toString() + " where " + this.sqlWhere;
    }

    public SqlUpdateBuilder setSqlWhere(String sqlWhere) {
        this.sqlWhere = sqlWhere;
        return this;
    }

    public SqlUpdateBuilder setSqlWhere(SqlWhereBuilder sqlWhereBuilder) {
        this.sqlWhere = sqlWhereBuilder.toSqlWhere();
        return this;
    }

    public SqlUpdateBuilder setExpression(String fieldName, String value) {
        if (value == null) {
            this.hsmpFields.put(fieldName.toUpperCase(), "null");
        } else {
            this.hsmpFields.put(fieldName.toUpperCase(), value);
        }
        return this;
    }

    public SqlUpdateBuilder setString(String fieldName, Object value) {
        if (value == null) {
            this.hsmpFields.put(fieldName.toUpperCase(), "null");
        } else {
            this.hsmpFields.put(fieldName.toUpperCase(), "'" + value.toString().replaceAll("'", "''") + "'");
        }
        return this;
    }

    public SqlUpdateBuilder setNumber(String fieldName, Object value) {
        if (value == null) {
            this.hsmpFields.put(fieldName.toUpperCase(), "null");
        } else {
            this.hsmpFields.put(fieldName.toUpperCase(), value.toString());
        }
        return this;
    }

    public SqlUpdateBuilder setTime(String fieldName, Date value) {
        if (value == null) {
            this.hsmpFields.put(fieldName.toUpperCase(), "null");
        } else {
            this.hsmpFields.put(fieldName.toUpperCase(), "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value) + "'");
        }
        return this;
    }

    public SqlUpdateBuilder setDate(String fieldName, Date value) {
        if (value == null) {
            this.hsmpFields.put(fieldName.toUpperCase(), "null");
        } else {
            this.hsmpFields.put(fieldName.toUpperCase(), "'" + new SimpleDateFormat("yyyy-MM-dd").format(value) + "'");
        }
        return this;
    }

    public SqlUpdateBuilder addNumber(String fieldName, Object value) {
        if (value == null) {
            this.hsmpFields.put(fieldName.toUpperCase(), fieldName);
        } else {
            this.hsmpFields.put(fieldName.toUpperCase(), "case when " + fieldName + " is null then " + value.toString() + " else " + fieldName + "+(" + value.toString() + ") end ");
        }
        return this;
    }

    public SqlUpdateBuilder subNumber(String fieldName, Object value) {
        if (value == null) {
            this.hsmpFields.put(fieldName.toUpperCase(), fieldName);
        } else {
            this.hsmpFields.put(fieldName.toUpperCase(), "case when " + fieldName + " is null then -(" + value.toString() + ") else " + fieldName + "-(" + value.toString() + ") end ");
        }
        return this;
    }

    public SqlUpdateBuilder addString(String fieldName, String value) {
        if (value == null) {
            this.hsmpFields.put(fieldName.toUpperCase(), fieldName);
        } else {
            this.hsmpFields.put(fieldName.toUpperCase(), "(case when " + fieldName + " is null then '' else " + fieldName + " end) || '" + value.replaceAll("'", "''") + "'");
        }
        return this;
    }
}
