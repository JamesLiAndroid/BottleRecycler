package com.incomrecycle.common;

import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.StringUtils;

import java.util.HashMap;
import java.util.Properties;

public class SysConfig {
    private static final HashMap hsmp = new HashMap();
    private static final Properties prop = new Properties();
    private static final String HEAD_START_DIR = "assets/";

    public static void init(String resource) {
        synchronized (prop) {
            if (prop.size() == 0) {
                if (resource == null || "".equals(resource)) {
                    resource = "config";
                }
                prop.putAll(PropUtils.loadResource(resource));
                String INCLUDE_CONFIG = prop.getProperty("INCLUDES");
                if (!StringUtils.isBlank(INCLUDE_CONFIG)) {
                    String[] INCLUDES = INCLUDE_CONFIG.split(";");
                    for (int i = 0; i < INCLUDES.length; i++) {
                        if (!StringUtils.isBlank(INCLUDES[i])) {
                            try {
                                Properties propInclude;
                                if (INCLUDES[i].startsWith("/")) {
                                    propInclude = PropUtils.loadFile(INCLUDES[i]);
                                } else {
                                    propInclude = PropUtils.loadResource(INCLUDES[i]);
                                }
                                if (propInclude != null) {
                                    prop.putAll(propInclude);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public static void set(Properties prop) {
        init(null);
        synchronized (prop) {
            prop.putAll(prop);
        }
    }

    public static void set(String name, String value) {
        init(null);
        prop.setProperty(name, value);
    }

    public static String get(String name) {
        init(null);
        return prop.getProperty(name);
    }

    public static void setObj(String name, Object value) {
        hsmp.put(name, value);
    }

    public static Object getObj(String name) {
        return hsmp.get(name);
    }
}
