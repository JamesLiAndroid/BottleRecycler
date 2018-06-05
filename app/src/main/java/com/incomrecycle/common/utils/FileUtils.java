package com.incomrecycle.common.utils;

import java.io.File;

public class FileUtils {
    public static boolean mkdir(String path) {
        if (path == null) {
            return false;
        }
        path = path.trim();
        if (path.length() != 0) {
            return mkdir(new File(path));
        }
        return false;
    }

    public static boolean mkdir(File filePath) {
        if (filePath.isFile()) {
            return false;
        }
        if (filePath.isDirectory()) {
            return true;
        }
        return filePath.mkdirs();
    }

    public static boolean mkFile(String path) {
        if (path == null) {
            return false;
        }
        path = path.trim();
        if (path.length() != 0) {
            return mkFile(new File(path));
        }
        return false;
    }

    public static boolean mkFile(File filePath) {
        if (filePath.isDirectory()) {
            return false;
        }
        if (!filePath.exists()) {
            try {
                return filePath.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static final boolean isFile(String path) {
        if (path == null || "".equals(path.trim())) {
            return false;
        }
        try {
            if (new File(path).isFile()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
