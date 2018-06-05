package com.incomrecycle.common.commtable;

import java.util.ArrayList;
import java.util.List;

public class CommTable {
    private CommTableField commTableField;
    private List<CommTableRecord> listCommTableRecord = new ArrayList();

    public CommTable(CommTableField commTableField) {
        this.commTableField = commTableField;
    }

    public CommTableField getField() {
        return this.commTableField;
    }

    public String getField(int index) {
        return this.commTableField.getField(index);
    }

    public int getFieldIndex(String field) {
        return this.commTableField.getFieldIndex(field);
    }

    public CommTable addRecord(String[] strRecordData) {
        return addRecord(new CommTableRecord(this.commTableField, strRecordData));
    }

    public CommTable addRecord(CommTableRecord commTableRecord) {
        this.listCommTableRecord.add(commTableRecord);
        return this;
    }

    public List<CommTableRecord> getRecord() {
        return this.listCommTableRecord;
    }

    public CommTableRecord getRecord(int index) {
        return (CommTableRecord) this.listCommTableRecord.get(index);
    }

    public List<CommTableRecord> getRecord(String strFieldName, String strFieldValue) {
        if (strFieldName == null) {
            return null;
        }
        int iFieldIndex = this.commTableField.getFieldIndex(strFieldName);
        if (iFieldIndex == -1) {
            return null;
        }
        List<CommTableRecord> resList = new ArrayList();
        for (int i = 0; i < this.listCommTableRecord.size(); i++) {
            CommTableRecord commTableRecord = (CommTableRecord) this.listCommTableRecord.get(i);
            if (strFieldValue == null) {
                if (commTableRecord.get(iFieldIndex) == null) {
                    resList.add(commTableRecord);
                }
            } else if (strFieldValue.equals(commTableRecord.get(iFieldIndex))) {
                resList.add(commTableRecord);
            }
        }
        if (resList.size() == 0) {
            return null;
        }
        return resList;
    }

    public int getRecordCount() {
        return this.listCommTableRecord.size();
    }
}
