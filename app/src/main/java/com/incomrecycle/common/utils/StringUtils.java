package com.incomrecycle.common.utils;

import java.util.HashMap;

public class StringUtils {
    private static final String EMPTY = "";

    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isBlank(String str) {
        if (str == null || str.trim().length() == 0) {
            return true;
        }
        return false;
    }

    public static String nullToEmpty(String str) {
        if (str == null) {
            return EMPTY;
        }
        return str;
    }

    public static String emptyToNull(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return null;
        }
        return str;
    }

    public static String trimToEmpty(String str) {
        if (str == null) {
            return EMPTY;
        }
        return str.trim();
    }

    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        str = str.trim();
        if (str.length() == 0) {
            return null;
        }
        return str;
    }

    public static boolean equals(String str1, String str2) {
        if (str1 == null) {
            str1 = EMPTY;
        }
        if (str2 == null) {
            str2 = EMPTY;
        }
        return str1.equals(str2);
    }

    public static String replace(String src_string, String sub_string, String replace_string) {
        if (src_string == null || src_string.length() == 0 || sub_string == null || sub_string.length() == 0) {
            return src_string;
        }
        if ((replace_string != null && replace_string.equals(sub_string)) || src_string.indexOf(sub_string) == -1) {
            return src_string;
        }
        int src_string_len = src_string.length();
        int sub_string_len = sub_string.length();
        int replace_string_len = 0;
        if (replace_string != null) {
            replace_string_len = replace_string.length();
        }
        StringBuffer sb = new StringBuffer();
        int iStart = 0;
        while (iStart < src_string_len) {
            int iEnd = src_string.indexOf(sub_string, iStart);
            if (iEnd == -1) {
                iEnd = src_string_len;
            }
            sb.append(src_string.substring(iStart, iEnd));
            if (replace_string_len > 0 && iEnd < src_string_len) {
                sb.append(replace_string);
            }
            iStart = iEnd + sub_string_len;
        }
        return sb.toString();
    }

    public static HashMap<String, String> toHashMap(String str, String attrDiv, String keyDiv) {
        HashMap<String, String> hsmpVal = new HashMap();
        String[] items = str.split(attrDiv);
        for (int i = 0; i < items.length; i++) {
            int div = items[i].indexOf(keyDiv);
            if (div != -1) {
                hsmpVal.put(items[i].substring(0, div).trim(), items[i].substring(div + 1).trim());
            }
        }
        return hsmpVal;
    }

    public static String convert(String src, String[][] strConvert) {
        if (src == null || strConvert == null || strConvert.length == 0) {
            return src;
        }
        int[] indexs = new int[strConvert.length];
        int i = 0;
        while (i < indexs.length) {
            if (strConvert[i] == null || strConvert[i].length != 2) {
                indexs[i] = -1;
            } else if (strConvert[i][0] == null || strConvert[i][0].length() == 0) {
                indexs[i] = -1;
            } else {
                indexs[i] = 0;
            }
            i++;
        }
        int iStart = 0;
        StringBuffer sb = new StringBuffer();
        while (true) {
            int iMin = -1;
            int iMinIndex = -1;
            i = 0;
            while (i < strConvert.length) {
                if (indexs[i] != -1) {
                    if (iStart >= indexs[i]) {
                        indexs[i] = src.indexOf(strConvert[i][0], iStart);
                    }
                    if (indexs[i] != -1 && (iMin == -1 || iMin > indexs[i])) {
                        iMin = indexs[i];
                        iMinIndex = i;
                    }
                }
                i++;
            }
            if (iMinIndex == -1) {
                sb.append(src.substring(iStart));
                return sb.toString();
            }
            sb.append(src.substring(iStart, indexs[iMinIndex]));
            if (strConvert[iMinIndex][1] != null) {
                sb.append(strConvert[iMinIndex][1]);
            }
            iStart = iMin + strConvert[iMinIndex][0].length();
            indexs[iMinIndex] = iStart;
        }
    }
}
