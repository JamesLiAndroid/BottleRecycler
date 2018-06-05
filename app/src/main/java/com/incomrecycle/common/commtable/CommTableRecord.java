package com.incomrecycle.common.commtable;

public class CommTableRecord {
    private CommTableField commTableField;
    private String[] record = null;

    public CommTableRecord(CommTableField commTableField) {
        this.commTableField = commTableField;
    }

    public CommTableRecord(CommTableField commTableField, String[] data) {
        this.commTableField = commTableField;
        set(data);
    }

    public CommTableRecord set(String[] data) {
        this.record = new String[this.commTableField.getCount()];
        for (int i = 0; i < this.record.length; i++) {
            this.record[i] = data[i];
        }
        return this;
    }

    public CommTableRecord set(String field, String val) {
        int iIndex = this.commTableField.getFieldIndex(field);
        if (this.record == null) {
            this.record = new String[this.commTableField.getCount()];
        }
        this.record[iIndex] = val;
        return this;
    }

    public CommTableRecord set(int fieldIndex, String val) {
        if (this.record == null) {
            this.record = new String[this.commTableField.getCount()];
        }
        this.record[fieldIndex] = val;
        return this;
    }

    public CommTableField getField() {
        return this.commTableField;
    }

    public String get(String field) {
        return get(this.commTableField.getFieldIndex(field));
    }

    public String get(int index) {
        return this.record[index];
    }

    public String[] get() {
        return this.record;
    }
}
