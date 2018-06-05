package com.incomrecycle.common.sqlite;

public class RowSet {
    private long offset;
    private long rows;

    private RowSet(long offset, long rows) {
        this.offset = offset;
        this.rows = rows;
    }

    public static RowSet newRowSet(long offset, long rows) {
        return new RowSet(offset, rows);
    }

    public static RowSet newPageSet(long no, long rows) {
        return new RowSet((no - 1) * rows, rows);
    }

    public long getOffset() {
        return this.offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getRows() {
        return this.rows;
    }

    public void setRows(long rows) {
        this.rows = rows;
    }
}
