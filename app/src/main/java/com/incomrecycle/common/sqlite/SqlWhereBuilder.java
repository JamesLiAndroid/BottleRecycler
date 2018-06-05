package com.incomrecycle.common.sqlite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SqlWhereBuilder {
    private String logic = " and ";
    private StringBuffer sb = new StringBuffer();

    public enum LOGIC {
        AND,
        OR
    }

    public SqlWhereBuilder() {

    }

    public SqlWhereBuilder(LOGIC logic) {
        if (logic == LOGIC.OR) {
            this.logic = " or ";
        }
    }

    public void clearSqlWhere() {
        this.sb.setLength(0);
    }

    public boolean isEmpty() {
        return this.sb.length() == 0;
    }

    public String toSqlWhere() {
        return this.sb.toString();
    }

    public String toSqlWhere(String join) {
        String sqlWhere = this.sb.toString();
        return (join == null || sqlWhere.trim().length() == 0) ? sqlWhere : " " + join + " (" + sqlWhere + ")";
    }

    public SqlWhereBuilder add(String sqlWhere) {
        if (!(sqlWhere == null || sqlWhere.trim().length() == 0)) {
            if (this.sb.length() != 0) {
                this.sb.append(this.logic);
            }
            this.sb.append(" ( ");
            this.sb.append(sqlWhere);
            this.sb.append(" ) ");
        }
        return this;
    }

    public SqlWhereBuilder addNot(String sqlWhere) {
        if (!(sqlWhere == null || sqlWhere.trim().length() == 0)) {
            if (this.sb.length() != 0) {
                this.sb.append(this.logic);
            }
            this.sb.append(" not ( ");
            this.sb.append(sqlWhere);
            this.sb.append(" ) ");
        }
        return this;
    }

    public SqlWhereBuilder addIsNull(String fieldName) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        this.sb.append(" is null ");
        return this;
    }

    public SqlWhereBuilder addIsNotNull(String fieldName) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        this.sb.append(" is not null ");
        return this;
    }

    public SqlWhereBuilder addStringEqualsTo(String fieldName, String value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (value == null) {
            this.sb.append(" is null ");
        } else {
            this.sb.append("=");
            this.sb.append("'" + value.replaceAll("'", "''") + "'");
        }
        return this;
    }

    public SqlWhereBuilder addStringNotEqualsTo(String fieldName, String value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        if (value == null) {
            this.sb.append(fieldName);
            this.sb.append(" is not null ");
        } else {
            this.sb.append("(");
            this.sb.append(fieldName);
            this.sb.append("<>");
            this.sb.append("'" + value.replaceAll("'", "''") + "'");
            this.sb.append(" or ");
            this.sb.append(fieldName);
            this.sb.append(" is null");
            this.sb.append(")");
        }
        return this;
    }

    public SqlWhereBuilder addStringIn(String fieldName, String[] array) {
        if (array == null) {
            return addStringEqualsTo(fieldName, null);
        }
        List list = new ArrayList();
        for (Object add : array) {
            list.add(add);
        }
        return addStringIn(fieldName, list);
    }

    public SqlWhereBuilder addStringIn(String fieldName, List<String> strList) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (strList == null) {
            this.sb.append(" is null ");
        } else {
            this.sb.append(" in");
            this.sb.append("(");
            int iCount = strList.size();
            for (int i = 0; i < iCount; i++) {
                if (strList.get(i) == null) {
                    this.sb.append("null");
                } else {
                    this.sb.append("'" + ((String) strList.get(i)).replaceAll("'", "''") + "'");
                }
                if (i < iCount - 1) {
                    this.sb.append(",");
                }
            }
            this.sb.append(")");
        }
        return this;
    }

    public SqlWhereBuilder addStringNotIn(String fieldName, List<String> strList) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (strList == null) {
            this.sb.append(" is null ");
        } else {
            this.sb.append(" not in");
            this.sb.append("(");
            int iCount = strList.size();
            for (int i = 0; i < iCount; i++) {
                if (strList.get(i) == null) {
                    this.sb.append("null");
                } else {
                    this.sb.append("'" + ((String) strList.get(i)).replaceAll("'", "''") + "'");
                }
                if (i < iCount - 1) {
                    this.sb.append(",");
                }
            }
            this.sb.append(")");
        }
        return this;
    }

    public SqlWhereBuilder addStringLike(String fieldName, String value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (value == null) {
            this.sb.append(" is null ");
        } else {
            this.sb.append(" like ");
            this.sb.append("'%" + value.replaceAll("'", "''") + "%'");
        }
        return this;
    }

    public SqlWhereBuilder addStringStartWith(String fieldName, String value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (value == null) {
            this.sb.append(" is null ");
        } else {
            this.sb.append(" like ");
            this.sb.append("'" + value.replaceAll("'", "''") + "%'");
        }
        return this;
    }

    public SqlWhereBuilder addStringEndWith(String fieldName, String value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (value == null) {
            this.sb.append(" is null ");
        } else {
            this.sb.append(" like ");
            this.sb.append("'%" + value.replaceAll("'", "''") + "'");
        }
        return this;
    }

    public SqlWhereBuilder addNumberEqualsTo(String fieldName, Object value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (value == null) {
            this.sb.append(" is null ");
        } else {
            this.sb.append("=");
            this.sb.append(value.toString());
        }
        return this;
    }

    public SqlWhereBuilder addNumberNotEqualsTo(String fieldName, Object value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        if (value == null) {
            this.sb.append(fieldName);
            this.sb.append(" is not null ");
        } else {
            this.sb.append("(");
            this.sb.append(fieldName);
            this.sb.append("<>");
            this.sb.append(value.toString());
            this.sb.append(" or ");
            this.sb.append(fieldName);
            this.sb.append(" is null");
            this.sb.append(")");
        }
        return this;
    }

    public SqlWhereBuilder addNumberGreaterTo(String fieldName, Object value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (value == null) {
            this.sb.append(" is null ");
        } else {
            this.sb.append(">");
            this.sb.append(value.toString());
        }
        return this;
    }

    public SqlWhereBuilder addNumberGreaterEqualsTo(String fieldName, Object value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (value == null) {
            this.sb.append(" is null ");
        } else {
            this.sb.append(">=");
            this.sb.append(value.toString());
        }
        return this;
    }

    public SqlWhereBuilder addNumberLessTo(String fieldName, Object value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (value == null) {
            this.sb.append(" is null ");
        } else {
            this.sb.append("<");
            this.sb.append(value.toString());
        }
        return this;
    }

    public SqlWhereBuilder addNumberLessEqualsTo(String fieldName, Object value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (value == null) {
            this.sb.append(" is null ");
        } else {
            this.sb.append("<=");
            this.sb.append(value.toString());
        }
        return this;
    }

    public SqlWhereBuilder addNumberIn(String fieldName, Object... objs) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (objs.length == 0) {
            this.sb.append(" is null ");
        } else {
            this.sb.append(" in");
            this.sb.append("(");
            int iCount = objs.length;
            for (int i = 0; i < iCount; i++) {
                if (objs[i] == null) {
                    this.sb.append("null");
                } else {
                    this.sb.append(objs[i].toString());
                }
                if (i < iCount - 1) {
                    this.sb.append(",");
                }
            }
            this.sb.append(")");
        }
        return this;
    }

    public SqlWhereBuilder addNumberIn(String fieldName, List longList) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        this.sb.append(fieldName);
        if (longList == null) {
            this.sb.append(" is null ");
        } else {
            this.sb.append(" in");
            this.sb.append("(");
            int iCount = longList.size();
            for (int i = 0; i < iCount; i++) {
                if (longList.get(i) == null) {
                    this.sb.append("null");
                } else {
                    this.sb.append(longList.get(i).toString());
                }
                if (i < iCount - 1) {
                    this.sb.append(",");
                }
            }
            this.sb.append(")");
        }
        return this;
    }

    public SqlWhereBuilder addDateEqualsTo(String fieldName, Date value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        if (value == null) {
            this.sb.append(fieldName);
            this.sb.append(" is null ");
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(value);
            this.sb.append("date(");
            this.sb.append(fieldName);
            this.sb.append(")=");
            this.sb.append("date('" + date.replaceAll("'", "''") + "')");
        }
        return this;
    }

    public SqlWhereBuilder addDateGreaterTo(String fieldName, Date value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        if (value == null) {
            this.sb.append(fieldName);
            this.sb.append(" is null ");
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(value);
            this.sb.append("date(");
            this.sb.append(fieldName);
            this.sb.append(")>");
            this.sb.append("date('" + date.replaceAll("'", "''") + "')");
        }
        return this;
    }

    public SqlWhereBuilder addDateGreaterEqualsTo(String fieldName, Date value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        if (value == null) {
            this.sb.append(fieldName);
            this.sb.append(" is null ");
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(value);
            this.sb.append("date(");
            this.sb.append(fieldName);
            this.sb.append(")>=");
            this.sb.append("date('" + date.replaceAll("'", "''") + "')");
        }
        return this;
    }

    public SqlWhereBuilder addDateLessTo(String fieldName, Date value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        if (value == null) {
            this.sb.append(fieldName);
            this.sb.append(" is null ");
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(value);
            this.sb.append("date(");
            this.sb.append(fieldName);
            this.sb.append(")<");
            this.sb.append("date('" + date.replaceAll("'", "''") + "')");
        }
        return this;
    }

    public SqlWhereBuilder addDateLessEqualsTo(String fieldName, Date value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        if (value == null) {
            this.sb.append(fieldName);
            this.sb.append(" is null ");
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(value);
            this.sb.append("date(");
            this.sb.append(fieldName);
            this.sb.append(")<=");
            this.sb.append("date('" + date.replaceAll("'", "''") + "')");
        }
        return this;
    }

    public SqlWhereBuilder addDatetimeEqualsTo(String fieldName, Date value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        if (value == null) {
            this.sb.append(fieldName);
            this.sb.append(" is null ");
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
            this.sb.append("datetime(");
            this.sb.append(fieldName);
            this.sb.append(")=");
            this.sb.append("datetime('" + date.replaceAll("'", "''") + "')");
        }
        return this;
    }

    public SqlWhereBuilder addDatetimeGreaterTo(String fieldName, Date value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        if (value == null) {
            this.sb.append(fieldName);
            this.sb.append(" is null ");
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
            this.sb.append("datetime(");
            this.sb.append(fieldName);
            this.sb.append(")>");
            this.sb.append("datetime('" + date.replaceAll("'", "''") + "')");
        }
        return this;
    }

    public SqlWhereBuilder addDatetimeGreaterEqualsTo(String fieldName, Date value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        if (value == null) {
            this.sb.append(fieldName);
            this.sb.append(" is null ");
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
            this.sb.append("datetime(");
            this.sb.append(fieldName);
            this.sb.append(")>=");
            this.sb.append("datetime('" + date.replaceAll("'", "''") + "')");
        }
        return this;
    }

    public SqlWhereBuilder addDatetimeLessTo(String fieldName, Date value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        if (value == null) {
            this.sb.append(fieldName);
            this.sb.append(" is null ");
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
            this.sb.append("datetime(");
            this.sb.append(fieldName);
            this.sb.append(")<");
            this.sb.append("datetime('" + date.replaceAll("'", "''") + "')");
        }
        return this;
    }

    public SqlWhereBuilder addDatetimeLessEqualsTo(String fieldName, Date value) {
        if (this.sb.length() != 0) {
            this.sb.append(this.logic);
        }
        if (value == null) {
            this.sb.append(fieldName);
            this.sb.append(" is null ");
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
            this.sb.append("datetime(");
            this.sb.append(fieldName);
            this.sb.append(")<=");
            this.sb.append("datetime('" + date.replaceAll("'", "''") + "')");
        }
        return this;
    }

    public SqlWhereBuilder add(SqlWhereBuilder sqlWhereBuilder) {
        String strSubWhere = sqlWhereBuilder.toSqlWhere();
        if (strSubWhere.length() != 0) {
            if (this.sb.length() != 0) {
                this.sb.append(this.logic);
            }
            this.sb.append(" ( ");
            this.sb.append(strSubWhere);
            this.sb.append(" ) ");
        }
        return this;
    }

    public SqlWhereBuilder addNot(SqlWhereBuilder sqlWhereBuilder) {
        String strSubWhere = sqlWhereBuilder.toSqlWhere();
        if (strSubWhere.length() != 0) {
            if (this.sb.length() != 0) {
                this.sb.append(this.logic);
            }
            this.sb.append(" not ( ");
            this.sb.append(strSubWhere);
            this.sb.append(" ) ");
        }
        return this;
    }
}
