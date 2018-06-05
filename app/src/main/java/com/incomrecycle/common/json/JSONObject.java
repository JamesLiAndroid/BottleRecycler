package com.incomrecycle.common.json;

import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import it.sauronsoftware.ftp4j.FTPCodes;

public class JSONObject {
    public static final Object NULL = new Null();
    private HashMap myHashMap;

    private static final class Null {
        private Null() {
        }

        protected final Object clone() {
            return this;
        }

        public boolean equals(Object object) {
            return object == null || object == this;
        }

        public String toString() {
            return "null";
        }
    }

    public JSONObject() {
        this.myHashMap = new HashMap();
    }

    public JSONObject(JSONObject jo, String[] names) throws JSONException {
        this();
        for (int i = 0; i < names.length; i++) {
            putOpt(names[i], jo.opt(names[i]));
        }
    }

    public JSONObject(JSONTokener x) throws JSONException {
        this();
        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        while (true) {
            switch (x.nextClean()) {
                case '\u0000':
                    throw x.syntaxError("A JSONObject text must end with '}'");
                case FTPCodes.DATA_CONNECTION_ALREADY_OPEN /*125*/:
                    return;
                default:
                    x.back();
                    String key = x.nextValue().toString();
                    char c = x.nextClean();
                    if (c == '=') {
                        if (x.next() != '>') {
                            x.back();
                        }
                    } else if (c != ':') {
                        throw x.syntaxError("Expected a ':' after a key");
                    }
                    put(key, x.nextValue());
                    switch (x.nextClean()) {
                        case ',':
                        case ';':
                            if (x.nextClean() != '}') {
                                x.back();
                            } else {
                                return;
                            }
                        case FTPCodes.DATA_CONNECTION_ALREADY_OPEN /*125*/:
                            return;
                        default:
                            throw x.syntaxError("Expected a ',' or '}'");
                    }
            }
        }
    }

    public JSONObject(Map map) {
        this.myHashMap = map == null ? new HashMap() : new HashMap(map);
    }

    public JSONObject(Map<String, String> map, boolean includeSuperClass) {
        this.myHashMap = new HashMap<String, String>();
        if (map != null) {
            for (Entry e : map.entrySet()) {
                this.myHashMap.put(e.getKey(), new JSONObject(e.getValue(), includeSuperClass));
            }
        }
    }

    public JSONObject(Object bean) {
        this();
        try {
            populateInternalMap(bean, false);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject(Object bean, boolean includeSuperClass) {
        this();
        try {
            populateInternalMap(bean, includeSuperClass);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void populateInternalMap(Object bean, boolean includeSuperClass) throws InvocationTargetException, IllegalAccessException, JSONException {
        Class klass = bean.getClass();
        if (klass.getClassLoader() == null) {
            includeSuperClass = false;
        }
        Method[] methods = includeSuperClass ? klass.getMethods() : klass.getDeclaredMethods();
        for (Method method : methods) {
            String name = method.getName();
            String key = "";
            if (name.startsWith("get")) {
                key = name.substring(3);
            } else {
                try {
                    if (name.startsWith("is")) {
                        key = name.substring(2);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (key.length() > 0 && Character.isUpperCase(key.charAt(0)) && method.getParameterTypes().length == 0) {
                if (key.length() == 1) {
                    key = key.toLowerCase();
                } else if (!Character.isUpperCase(key.charAt(1))) {
                    key = key.substring(0, 1).toLowerCase() + key.substring(1);
                }
                Object result = method.invoke(bean, (Object[]) null);
                if (result == null) {
                    this.myHashMap.put(key, NULL);
                } else if (result.getClass().isArray()) {
                    this.myHashMap.put(key, new JSONArray(result, includeSuperClass));
                } else if (result instanceof Collection) {
                    this.myHashMap.put(key, new JSONArray((Collection) result, includeSuperClass));
                } else if (result instanceof Map) {
                    this.myHashMap.put(key, new JSONObject((Map) result, includeSuperClass));
                } else if (isStandardProperty(result.getClass())) {
                    this.myHashMap.put(key, result);
                } else if (result.getClass().getPackage().getName().startsWith("java") || result.getClass().getClassLoader() == null) {
                    this.myHashMap.put(key, result.toString());
                } else {
                    this.myHashMap.put(key, new JSONObject(result, includeSuperClass));
                }
            }
        }
    }

    private boolean isStandardProperty(Class clazz) {
        if (clazz.isPrimitive() || clazz.isAssignableFrom(Byte.class) || clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(Character.class) || clazz.isAssignableFrom(String.class) || clazz.isAssignableFrom(Boolean.class)) {
            return true;
        }
        return false;
    }

    public JSONObject(Object object, String[] names) {
        this();
        Class c = object.getClass();
        for (String name : names) {
            try {
                put(name, c.getField(name).get(object));
            } catch (Exception e) {
            }
        }
    }

    public JSONObject(String source) throws JSONException {
        this(new JSONTokener(source));
    }

    public JSONObject accumulate(String key, Object value) throws JSONException {
        testValidity(value);
        Object o = opt(key);
        if (o == null) {
            if (value instanceof JSONArray) {
                value = new JSONArray().put(value);
            }
            put(key, value);
        } else if (o instanceof JSONArray) {
            ((JSONArray) o).put(value);
        } else {
            put(key, new JSONArray().put(o).put(value));
        }
        return this;
    }

    public JSONObject append(String key, Object value) throws JSONException {
        testValidity(value);
        Object o = opt(key);
        if (o == null) {
            put(key, new JSONArray().put(value));
        } else if (o instanceof JSONArray) {
            put(key, ((JSONArray) o).put(value));
        } else {
            throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
        }
        return this;
    }

    public static String doubleToString(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return "null";
        }
        String s = Double.toString(d);
        if (s.indexOf(46) <= 0 || s.indexOf(101) >= 0 || s.indexOf(69) >= 0) {
            return s;
        }
        while (s.endsWith("0")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.endsWith(".")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    public Object get(String key) throws JSONException {
        Object o = opt(key);
        if (o != null) {
            return o;
        }
        throw new JSONException("JSONObject[" + quote(key) + "] not found.");
    }

    public boolean getBoolean(String key) throws JSONException {
        Object o = get(key);
        if (o.equals(Boolean.FALSE) || ((o instanceof String) && ((String) o).equalsIgnoreCase("false"))) {
            return false;
        }
        if (o.equals(Boolean.TRUE) || ((o instanceof String) && ((String) o).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new JSONException("JSONObject[" + quote(key) + "] is not a Boolean.");
    }

    public double getDouble(String key) throws JSONException {
        Object o = get(key);
        try {
            if (o instanceof Number) {
                return ((Number) o).doubleValue();
            }
            return Double.valueOf((String) o).doubleValue();
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + quote(key) + "] is not a number.");
        }
    }

    public int getInt(String key) throws JSONException {
        Object o = get(key);
        return o instanceof Number ? ((Number) o).intValue() : (int) getDouble(key);
    }

    public JSONArray getJSONArray(String key) throws JSONException {
        Object o = get(key);
        if (o instanceof JSONArray) {
            return (JSONArray) o;
        }
        throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONArray.");
    }

    public JSONObject getJSONObject(String key) throws JSONException {
        Object o = get(key);
        if (o instanceof JSONObject) {
            return (JSONObject) o;
        }
        throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONObject.");
    }

    public long getLong(String key) throws JSONException {
        Object o = get(key);
        return o instanceof Number ? ((Number) o).longValue() : (long) getDouble(key);
    }

    public static String[] getNames(JSONObject jo) {
        int length = jo.length();
        if (length == 0) {
            return null;
        }
        Iterator i = jo.keys();
        String[] names = new String[length];
        int j = 0;
        while (i.hasNext()) {
            names[j] = (String) i.next();
            j++;
        }
        return names;
    }

    public static String[] getNames(Object object) {
        String[] strArr = null;
        if (object != null) {
            Field[] fields = object.getClass().getFields();
            int length = fields.length;
            if (length != 0) {
                strArr = new String[length];
                for (int i = 0; i < length; i++) {
                    strArr[i] = fields[i].getName();
                }
            }
        }
        return strArr;
    }

    public String getString(String key) throws JSONException {
        return get(key).toString();
    }

    public boolean has(String key) {
        return this.myHashMap.containsKey(key);
    }

    public boolean isNull(String key) {
        return NULL.equals(opt(key));
    }

    public Iterator keys() {
        return this.myHashMap.keySet().iterator();
    }

    public int length() {
        return this.myHashMap.size();
    }

    public JSONArray names() {
        JSONArray ja = new JSONArray();
        Iterator keys = keys();
        while (keys.hasNext()) {
            ja.put(keys.next());
        }
        return ja.length() == 0 ? null : ja;
    }

    public static String numberToString(Number n) throws JSONException {
        if (n == null) {
            throw new JSONException("Null pointer");
        }
        testValidity(n);
        String s = n.toString();
        if (s.indexOf(46) <= 0 || s.indexOf(101) >= 0 || s.indexOf(69) >= 0) {
            return s;
        }
        while (s.endsWith("0")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.endsWith(".")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    public Object opt(String key) {
        return key == null ? null : this.myHashMap.get(key);
    }

    public boolean optBoolean(String key) {
        return optBoolean(key, false);
    }

    public boolean optBoolean(String key, boolean defaultValue) {
        try {
            defaultValue = getBoolean(key);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public JSONObject put(String key, Collection value) throws JSONException {
        put(key, new JSONArray(value));
        return this;
    }

    public double optDouble(String key) {
        return optDouble(key, Double.NaN);
    }

    public double optDouble(String key, double defaultValue) {
        try {
            Object o = opt(key);
            if (o instanceof Number) {
                return ((Number) o).doubleValue();
            }
            return new Double((String) o).doubleValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public int optInt(String key) {
        return optInt(key, 0);
    }

    public int optInt(String key, int defaultValue) {
        try {
            defaultValue = getInt(key);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public JSONArray optJSONArray(String key) {
        Object o = opt(key);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }

    public JSONObject optJSONObject(String key) {
        Object o = opt(key);
        return o instanceof JSONObject ? (JSONObject) o : null;
    }

    public long optLong(String key) {
        return optLong(key, 0);
    }

    public long optLong(String key, long defaultValue) {
        try {
            defaultValue = getLong(key);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public String optString(String key) {
        return optString(key, "");
    }

    public String optString(String key, String defaultValue) {
        Object o = opt(key);
        return o != null ? o.toString() : defaultValue;
    }

    public JSONObject put(String key, boolean value) throws JSONException {
        put(key, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    public JSONObject put(String key, double value) throws JSONException {
        put(key, new Double(value));
        return this;
    }

    public JSONObject put(String key, int value) throws JSONException {
        put(key, new Integer(value));
        return this;
    }

    public JSONObject put(String key, long value) throws JSONException {
        put(key, new Long(value));
        return this;
    }

    public JSONObject put(String key, Map value) throws JSONException {
        put(key, new JSONObject(value));
        return this;
    }

    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        if (value != null) {
            testValidity(value);
            this.myHashMap.put(key, value);
        } else {
            remove(key);
        }
        return this;
    }

    public JSONObject putOpt(String key, Object value) throws JSONException {
        if (!(key == null || value == null)) {
            put(key, value);
        }
        return this;
    }

    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }
        char c = '\u0000';
        int len = string.length();
        StringBuffer sb = new StringBuffer(len + 4);
        sb.append('\"');
        for (int i = 0; i < len; i++) {
            char b = c;
            c = string.charAt(i);
            switch (c) {
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\"':
                case '\\':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    if (b == '<') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    break;
                default:
                    if (c >= ' ' && ((c < '' || c >= ' ') && (c < ' ' || c >= '℀'))) {
                        sb.append(c);
                        break;
                    }
                    String t = "000" + Integer.toHexString(c);
                    sb.append("\\u" + t.substring(t.length() - 4));
                    break;
            }
        }
        sb.append('\"');
        return sb.toString();
    }

    public Object remove(String key) {
        return this.myHashMap.remove(key);
    }

    public Iterator sortedKeys() {
        return new TreeSet(this.myHashMap.keySet()).iterator();
    }

    static void testValidity(Object o) throws JSONException {
        if (o == null) {
            return;
        }
        if (o instanceof Double) {
            if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
                throw new JSONException("JSON does not allow non-finite numbers.");
            }
        } else if (!(o instanceof Float)) {
        } else {
            if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
                throw new JSONException("JSON does not allow non-finite numbers.");
            }
        }
    }

    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        if (names == null || names.length() == 0) {
            return null;
        }
        JSONArray ja = new JSONArray();
        for (int i = 0; i < names.length(); i++) {
            ja.put(opt(names.getString(i)));
        }
        return ja;
    }

    public String toString() {
        try {
            Iterator keys = keys();
            StringBuffer sb = new StringBuffer("{");
            while (keys.hasNext()) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                Object o = keys.next();
                sb.append(quote(o.toString()));
                sb.append(':');
                sb.append(valueToString(this.myHashMap.get(o)));
            }
            sb.append('}');
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public String toString(int indentFactor) throws JSONException {
        return toString(indentFactor, 0);
    }

    String toString(int indentFactor, int indent) throws JSONException {
        int n = length();
        if (n == 0) {
            return "{}";
        }
        Iterator keys = sortedKeys();
        StringBuffer sb = new StringBuffer("{");
        int newindent = indent + indentFactor;
        Object o;
        if (n == 1) {
            o = keys.next();
            sb.append(quote(o.toString()));
            sb.append(": ");
            sb.append(valueToString(this.myHashMap.get(o), indentFactor, indent));
        } else {
            int j;
            while (keys.hasNext()) {
                o = keys.next();
                if (sb.length() > 1) {
                    sb.append(",\n");
                } else {
                    sb.append('\n');
                }
                for (j = 0; j < newindent; j++) {
                    sb.append(' ');
                }
                sb.append(quote(o.toString()));
                sb.append(": ");
                sb.append(valueToString(this.myHashMap.get(o), indentFactor, newindent));
            }
            if (sb.length() > 1) {
                sb.append('\n');
                for (j = 0; j < indent; j++) {
                    sb.append(' ');
                }
            }
        }
        sb.append('}');
        return sb.toString();
    }

    static String valueToString(Object value) throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        if (value instanceof JSONString) {
            try {
                String o = ((JSONString) value).toJSONString();
                if (o instanceof String) {
                    return o;
                }
                throw new JSONException("Bad value from toJSONString: " + o);
            } catch (Throwable e) {
                throw new JSONException(e);
            }
        } else if (value instanceof Number) {
            return numberToString((Number) value);
        } else {
            if ((value instanceof Boolean) || (value instanceof JSONObject) || (value instanceof JSONArray)) {
                return value.toString();
            }
            if (value instanceof Map) {
                return new JSONObject((Map) value).toString();
            }
            if (value instanceof Collection) {
                return new JSONArray((Collection) value).toString();
            }
            if (value.getClass().isArray()) {
                return new JSONArray(value).toString();
            }
            return quote(value.toString());
        }
    }

    static String valueToString(Object value, int indentFactor, int indent) throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        try {
            if (value instanceof JSONString) {
                String o = ((JSONString) value).toJSONString();
                if (o instanceof String) {
                    return o;
                }
            }
        } catch (Exception e) {
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof JSONObject) {
            return ((JSONObject) value).toString(indentFactor, indent);
        }
        if (value instanceof JSONArray) {
            return ((JSONArray) value).toString(indentFactor, indent);
        }
        if (value instanceof Map) {
            return new JSONObject((Map) value).toString(indentFactor, indent);
        }
        if (value instanceof Collection) {
            return new JSONArray((Collection) value).toString(indentFactor, indent);
        }
        if (value.getClass().isArray()) {
            return new JSONArray(value).toString(indentFactor, indent);
        }
        return quote(value.toString());
    }

    public Writer write(Writer writer) throws JSONException {
        boolean b = false;
        try {
            Iterator keys = keys();
            writer.write(123);
            while (keys.hasNext()) {
                if (b) {
                    writer.write(44);
                }
                Object k = keys.next();
                writer.write(quote(k.toString()));
                writer.write(58);
                Object v = this.myHashMap.get(k);
                if (v instanceof JSONObject) {
                    ((JSONObject) v).write(writer);
                } else if (v instanceof JSONArray) {
                    ((JSONArray) v).write(writer);
                } else {
                    writer.write(valueToString(v));
                }
                b = true;
            }
            writer.write(FTPCodes.DATA_CONNECTION_ALREADY_OPEN);
            return writer;
        } catch (Throwable e) {
            throw new JSONException(e);
        }
    }
}
