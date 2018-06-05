package com.incomrecycle.common.json;

import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.commtable.CommTableField;
import com.incomrecycle.common.commtable.CommTableRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONUtils {
    public static String toJSON(List listParam) {
        if (listParam == null) {
            return null;
        }
        return new JSONArray((Collection) listParam).toString();
    }

    public static String toJSON(HashMap hsmpParam) {
        if (hsmpParam == null) {
            return null;
        }
        return new JSONObject((Map) hsmpParam).toString();
    }

    public static List toList(JSONArray jsonArray) throws Exception {
        if (jsonArray == null) {
            return null;
        }
        List listResult = new ArrayList();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object obj = jsonArray.get(i);
            if (obj == null) {
                listResult.add(null);
            } else if (obj.equals(JSONObject.NULL)) {
                listResult.add(null);
            } else if (obj instanceof JSONObject) {
                listResult.add(toHashMap((JSONObject) obj));
            } else if (obj instanceof JSONArray) {
                listResult.add(toList((JSONArray) obj));
            } else {
                listResult.add(obj);
            }
        }
        return listResult;
    }

    public static HashMap toHashMap(JSONObject jsonObject) throws Exception {
        if (jsonObject == null) {
            return null;
        }
        HashMap hsmpResult = new HashMap();
        Iterator iter = jsonObject.keys();
        while (iter.hasNext()) {
            String key = iter.next().toString();
            Object val = jsonObject.get(key);
            if (val == null) {
                hsmpResult.put(key, null);
            } else if (val.equals(JSONObject.NULL)) {
                hsmpResult.put(key, null);
            } else if (val instanceof JSONArray) {
                hsmpResult.put(key, toList((JSONArray) val));
            } else if (val instanceof JSONObject) {
                hsmpResult.put(key, toHashMap((JSONObject) val));
            } else {
                hsmpResult.put(key, val);
            }
        }
        return hsmpResult;
    }

    public static List toList(String json) throws Exception {
        if (json == null) {
            return null;
        }
        return toList(new JSONArray(json));
    }

    public static HashMap toHashMap(String json) throws Exception {
        if (json == null || json.length() == 0) {
            return null;
        }
        return toHashMap(new JSONObject(json));
    }

    public static List<HashMap<String, String>> toList(CommTable commTable) {
        if (commTable == null) {
            return null;
        }
        List<HashMap<String, String>> list = new ArrayList();
        for (int i = 0; i < commTable.getRecordCount(); i++) {
            CommTableRecord ctr = commTable.getRecord(i);
            CommTableField ctf = ctr.getField();
            HashMap<String, String> hsmp = new HashMap();
            for (int f = 0; f < ctf.getCount(); f++) {
                hsmp.put(ctf.getField(f), ctr.get(ctf.getField(f)));
            }
            list.add(hsmp);
        }
        return list;
    }
}
