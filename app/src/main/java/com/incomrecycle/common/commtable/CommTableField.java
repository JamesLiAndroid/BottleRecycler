package com.incomrecycle.common.commtable;

import java.util.ArrayList;
import java.util.List;

public class CommTableField {
    private List<String> listField = new ArrayList();

    public CommTableField addField(String field) {
        field = field.toUpperCase().trim();
        if (!this.listField.contains(field)) {
            this.listField.add(field);
        }
        return this;
    }

    public CommTableField setFields(String[] fields) {
        for (String addField : fields) {
            addField(addField);
        }
        return this;
    }

    public int getFieldIndex(String field) {
        return this.listField.indexOf(field.toUpperCase().trim());
    }

    public String getField(int index) {
        return (String) this.listField.get(index);
    }

    public String[] getFields() {
        return (String[]) this.listField.toArray(new String[0]);
    }

    public int getCount() {
        return this.listField.size();
    }
}
