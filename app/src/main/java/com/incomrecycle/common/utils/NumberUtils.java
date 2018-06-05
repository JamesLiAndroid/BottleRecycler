package com.incomrecycle.common.utils;

import java.math.BigDecimal;

public class NumberUtils {
    public static String toScale(BigDecimal dfVal, int scale) {
        if (dfVal == null) {
            return "0";
        }
        return toScale(dfVal.doubleValue(), scale);
    }

    public static String toScale(String sVal, int scale) {
        if (StringUtils.isBlank(sVal)) {
            return "0";
        }
        return toScale(Double.parseDouble(sVal), scale);
    }

    public static String toScale(double dfVal, int scale) {
        if (dfVal == 0.0d) {
            return "0";
        }
        int oldScale = scale;
        double dfScale = 1.0d;
        while (scale > 0) {
            dfScale *= 10.0d;
            scale--;
        }
        while (scale < 0) {
            dfScale *= 0.1d;
            scale++;
        }
        String sValue = "" + Math.round(dfVal * dfScale);
        if (oldScale == 0) {
            return sValue;
        }
        if (oldScale > 0) {
            while (sValue.length() <= oldScale) {
                sValue = "0" + sValue;
            }
            return sValue.substring(0, sValue.length() - oldScale) + "." + sValue.substring(sValue.length() - oldScale);
        }
        while (oldScale < 0) {
            sValue = sValue + "0";
            oldScale++;
        }
        return sValue;
    }
}
