package com.incomrecycle.common.json;

import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONArray {
    private ArrayList myArrayList;

    public JSONArray() {
        this.myArrayList = new ArrayList();
    }

    public JSONArray(JSONTokener x) throws JSONException {
        char q;
        char c = x.nextClean();
        if (c == '[') {
            q = ']';
        } else if (c == '(') {
            q = ')';
        } else {
            throw x.syntaxError("A JSONArray text must start with '['");
        }
        if (x.nextClean() != ']') {
            x.back();
            while (true) {
                if (x.nextClean() == ',') {
                    x.back();
                    this.myArrayList.add(null);
                } else {
                    x.back();
                    this.myArrayList.add(x.nextValue());
                }
                c = x.nextClean();
                switch (c) {
                    case ')':
                    case ']':
                        if (q != c) {
                            throw x.syntaxError("Expected a '" + new Character(q) + "'");
                        }
                        return;
                    case ',':
                    case ';':
                        if (x.nextClean() != ']') {
                            x.back();
                        } else {
                            return;
                        }
                    default:
                        throw x.syntaxError("Expected a ',' or ']'");
                }
            }
        }
    }

    public JSONArray(String source) throws JSONException {
        this(new JSONTokener(source));
    }

    public JSONArray(Collection collection) {
        this.myArrayList = collection == null ? new ArrayList() : new ArrayList(collection);
    }

    public JSONArray(Collection collection, boolean includeSuperClass) {
        this.myArrayList = new ArrayList();
        if (collection != null) {
            for (Object jSONObject : collection) {
                this.myArrayList.add(new JSONObject(jSONObject, includeSuperClass));
            }
        }
    }

    public JSONArray(Object array) throws JSONException {
        this();
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                put(Array.get(array, i));
            }
            return;
        }
        throw new JSONException("JSONArray initial value should be a string or collection or array.");
    }

    public JSONArray(Object array, boolean includeSuperClass) throws JSONException {
        this();
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                put(new JSONObject(Array.get(array, i), includeSuperClass));
            }
            return;
        }
        throw new JSONException("JSONArray initial value should be a string or collection or array.");
    }

    public Object get(int index) throws JSONException {
        Object o = opt(index);
        if (o != null) {
            return o;
        }
        throw new JSONException("JSONArray[" + index + "] not found.");
    }

    public boolean getBoolean(int index) throws JSONException {
        Object o = get(index);
        if (o.equals(Boolean.FALSE) || ((o instanceof String) && ((String) o).equalsIgnoreCase("false"))) {
            return false;
        }
        if (o.equals(Boolean.TRUE) || ((o instanceof String) && ((String) o).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new JSONException("JSONArray[" + index + "] is not a Boolean.");
    }

    public double getDouble(int index) throws JSONException {
        Object o = get(index);
        try {
            if (o instanceof Number) {
                return ((Number) o).doubleValue();
            }
            return Double.valueOf((String) o).doubleValue();
        } catch (Exception e) {
            throw new JSONException("JSONArray[" + index + "] is not a number.");
        }
    }

    public int getInt(int index) throws JSONException {
        Object o = get(index);
        return o instanceof Number ? ((Number) o).intValue() : (int) getDouble(index);
    }

    public JSONArray getJSONArray(int index) throws JSONException {
        Object o = get(index);
        if (o instanceof JSONArray) {
            return (JSONArray) o;
        }
        throw new JSONException("JSONArray[" + index + "] is not a JSONArray.");
    }

    public JSONObject getJSONObject(int index) throws JSONException {
        Object o = get(index);
        if (o instanceof JSONObject) {
            return (JSONObject) o;
        }
        throw new JSONException("JSONArray[" + index + "] is not a JSONObject.");
    }

    public long getLong(int index) throws JSONException {
        Object o = get(index);
        return o instanceof Number ? ((Number) o).longValue() : (long) getDouble(index);
    }

    public String getString(int index) throws JSONException {
        return get(index).toString();
    }

    public boolean isNull(int index) {
        return JSONObject.NULL.equals(opt(index));
    }

    public String join(String separator) throws JSONException {
        int len = length();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(JSONObject.valueToString(this.myArrayList.get(i)));
        }
        return sb.toString();
    }

    public int length() {
        return this.myArrayList.size();
    }

    public Object opt(int index) {
        if (index < 0 || index >= length()) {
            return null;
        }
        return this.myArrayList.get(index);
    }

    public boolean optBoolean(int index) {
        return optBoolean(index, false);
    }

    public boolean optBoolean(int index, boolean defaultValue) {
        try {
            defaultValue = getBoolean(index);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public double optDouble(int index) {
        return optDouble(index, Double.NaN);
    }

    public double optDouble(int index, double defaultValue) {
        try {
            defaultValue = getDouble(index);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public int optInt(int index) {
        return optInt(index, 0);
    }

    public int optInt(int index, int defaultValue) {
        try {
            defaultValue = getInt(index);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public JSONArray optJSONArray(int index) {
        Object o = opt(index);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }

    public JSONObject optJSONObject(int index) {
        Object o = opt(index);
        return o instanceof JSONObject ? (JSONObject) o : null;
    }

    public long optLong(int index) {
        return optLong(index, 0);
    }

    public long optLong(int index, long defaultValue) {
        try {
            defaultValue = getLong(index);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public String optString(int index) {
        return optString(index, "");
    }

    public String optString(int index, String defaultValue) {
        Object o = opt(index);
        return o != null ? o.toString() : defaultValue;
    }

    public JSONArray put(boolean value) {
        put(value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    public JSONArray put(Collection value) {
        put(new JSONArray(value));
        return this;
    }

    public JSONArray put(double value) throws JSONException {
        Object d = new Double(value);
        JSONObject.testValidity(d);
        put(d);
        return this;
    }

    public JSONArray put(int value) {
        put(new Integer(value));
        return this;
    }

    public JSONArray put(long value) {
        put(new Long(value));
        return this;
    }

    public JSONArray put(Map value) {
        put(new JSONObject(value));
        return this;
    }

    public JSONArray put(Object value) {
        this.myArrayList.add(value);
        return this;
    }

    public JSONArray put(int index, boolean value) throws JSONException {
        put(index, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    public JSONArray put(int index, Collection value) throws JSONException {
        put(index, new JSONArray(value));
        return this;
    }

    public JSONArray put(int index, double value) throws JSONException {
        put(index, new Double(value));
        return this;
    }

    public JSONArray put(int index, int value) throws JSONException {
        put(index, new Integer(value));
        return this;
    }

    public JSONArray put(int index, long value) throws JSONException {
        put(index, new Long(value));
        return this;
    }

    public JSONArray put(int index, Map value) throws JSONException {
        put(index, new JSONObject(value));
        return this;
    }

    public JSONArray put(int index, Object value) throws JSONException {
        JSONObject.testValidity(value);
        if (index < 0) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        if (index < length()) {
            this.myArrayList.set(index, value);
        } else {
            while (index != length()) {
                put(JSONObject.NULL);
            }
            put(value);
        }
        return this;
    }

    public JSONObject toJSONObject(JSONArray names) throws JSONException {
        if (names == null || names.length() == 0 || length() == 0) {
            return null;
        }
        JSONObject jo = new JSONObject();
        for (int i = 0; i < names.length(); i++) {
            jo.put(names.getString(i), opt(i));
        }
        return jo;
    }

    public String toString() {
        try {
            return '[' + join(",") + ']';
        } catch (Exception e) {
            return null;
        }
    }

    public String toString(int indentFactor) throws JSONException {
        return toString(indentFactor, 0);
    }

    String toString(int indentFactor, int indent) throws JSONException {
        int len = length();
        if (len == 0) {
            return "[]";
        }
        StringBuffer sb = new StringBuffer("[");
        if (len == 1) {
            sb.append(JSONObject.valueToString(this.myArrayList.get(0), indentFactor, indent));
        } else {
            int i;
            int newindent = indent + indentFactor;
            sb.append('\n');
            for (i = 0; i < len; i++) {
                if (i > 0) {
                    sb.append(",\n");
                }
                for (int j = 0; j < newindent; j++) {
                    sb.append(' ');
                }
                sb.append(JSONObject.valueToString(this.myArrayList.get(i), indentFactor, newindent));
            }
            sb.append('\n');
            for (i = 0; i < indent; i++) {
                sb.append(' ');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public Writer write(Writer writer) throws JSONException {
        boolean b = false;
        try {
            int len = length();
            writer.write(91);
            for (int i = 0; i < len; i++) {
                if (b) {
                    writer.write(44);
                }
                Object v = this.myArrayList.get(i);
                if (v instanceof JSONObject) {
                    ((JSONObject) v).write(writer);
                } else if (v instanceof JSONArray) {
                    ((JSONArray) v).write(writer);
                } else {
                    writer.write(JSONObject.valueToString(v));
                }
                b = true;
            }
            writer.write(93);
            return writer;
        } catch (Throwable e) {
            throw new JSONException(e);
        }
    }

    public static void main(String[] args) {
        Collection arrayList = new ArrayList();
        Map a = new HashMap();
        a.put("name", "北京");
        a.put("id", "010");
        Map b = new HashMap();
        b.put("name", "上海");
        b.put("id", "012");
        Map c = new HashMap();
        c.put("name", "辽宁");
        c.put("id", "024");
        List lnList = new ArrayList();
        Map d = new HashMap();
        d.put("name", "大连");
        d.put("id", "0411");
        lnList.add(d);
        c.put("list", lnList);
        arrayList.add(a);
        arrayList.add(b);
        arrayList.add(c);
        JSONArray jArray = new JSONArray(arrayList);
    }
}
