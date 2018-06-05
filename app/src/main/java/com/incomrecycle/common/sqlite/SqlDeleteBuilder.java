package com.incomrecycle.common.sqlite;

public class SqlDeleteBuilder implements SqlBuilder {
    private String sqlWhere = null;
    private String table = null;

    public SqlDeleteBuilder(String table) {
        this.table = table;
    }

    public void clearSql() {
        this.sqlWhere = null;
    }

    public String toSql() {
        if (this.table == null) {
            return null;
        }
        if (this.sqlWhere == null || "".equals(this.sqlWhere)) {
            return "delete from " + this.table;
        }
        return "delete from " + this.table + " where " + this.sqlWhere;
    }

    public SqlDeleteBuilder setSqlWhere(String sqlWhere) {
        this.sqlWhere = sqlWhere;
        return this;
    }

    public SqlDeleteBuilder setSqlWhere(SqlWhereBuilder sqlWhereBuilder) {
        this.sqlWhere = sqlWhereBuilder.toSqlWhere();
        return this;
    }
}
