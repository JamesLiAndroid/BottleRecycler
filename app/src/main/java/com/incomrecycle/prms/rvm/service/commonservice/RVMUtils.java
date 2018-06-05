package com.incomrecycle.prms.rvm.service.commonservice;

import com.google.code.microlog4android.format.SimpleFormatter;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.commtable.CommTableRecord;
import com.incomrecycle.common.sort.SortEntity.ORDERBY;
import com.incomrecycle.common.sort.SortEntityInt;
import com.incomrecycle.prms.rvm.common.SysDef;
import java.util.HashMap;
import java.util.List;

public class RVMUtils {

    public static class SortEntityIntBarCodePrice extends SortEntityInt {
        public SortEntityIntBarCodePrice(List<CommTableRecord> listCtr, ORDERBY orderby) {
            super(listCtr, orderby);
        }

        protected long getIntVal(int iIndex) {
            return (long) Integer.parseInt(((CommTableRecord) ((List) getObject()).get(iIndex)).get("BAR_CODE_PRICE_PRIORITY"));
        }

        protected int length() {
            return ((List) getObject()).size();
        }

        protected void exchange(int iFirst, int iSecond) {
            List<CommTableRecord> listCtr = (List) getObject();
            CommTableRecord ctr = (CommTableRecord) listCtr.get(iFirst);
            listCtr.set(iFirst, listCtr.get(iSecond));
            listCtr.set(iSecond, ctr);
        }
    }

    public static String generateBarcodePriceKey(HashMap<String, String> hsmpBarCodePrice) {
        return generateBarcodePriceKey((String) hsmpBarCodePrice.get("BAR_CODE_VOL"), (String) hsmpBarCodePrice.get("BAR_CODE_STUFF"), (String) hsmpBarCodePrice.get("BAR_CODE_WEIGH"), (String) hsmpBarCodePrice.get("BAR_CODE_COLOR"));
    }

    public static String generateBarcodePriceKey(String BAR_CODE_VOL, String BAR_CODE_STUFF, String BAR_CODE_WEIGH, String BAR_CODE_COLOR) {
        BAR_CODE_VOL = SysDef.formatBound(BAR_CODE_VOL, 2);
        return BAR_CODE_VOL + "_" + BAR_CODE_STUFF + "_" + SysDef.formatBound(BAR_CODE_WEIGH, 2) + "_" + BAR_CODE_COLOR;
    }

    public static boolean isMatchPrice(CommTableRecord ctrBarcodePrice, CommTableRecord ctrBarcodeAttr) {
        HashMap hsmpBarCodeAttr = new HashMap();
        hsmpBarCodeAttr.put("BAR_CODE_STUFF", ctrBarcodeAttr.get("BAR_CODE_STUFF"));
        hsmpBarCodeAttr.put("BAR_CODE_COLOR", ctrBarcodeAttr.get("BAR_CODE_COLOR"));
        hsmpBarCodeAttr.put("BAR_CODE_VOL", ctrBarcodeAttr.get("BAR_CODE_VOL"));
        hsmpBarCodeAttr.put("BAR_CODE_WEIGH", ctrBarcodeAttr.get("BAR_CODE_WEIGH"));
        return isMatchPrice(ctrBarcodePrice, hsmpBarCodeAttr);
    }

    public static boolean isMatchPrice(CommTableRecord ctrBarcodePrice, HashMap<String, String> hsmpBarCodeAttr) {
        HashMap hsmpBarCodePriceAttr = new HashMap();
        hsmpBarCodePriceAttr.put("BAR_CODE_STUFF", ctrBarcodePrice.get("BAR_CODE_STUFF"));
        hsmpBarCodePriceAttr.put("BAR_CODE_COLOR", ctrBarcodePrice.get("BAR_CODE_COLOR"));
        hsmpBarCodePriceAttr.put("BAR_CODE_VOL", ctrBarcodePrice.get("BAR_CODE_VOL"));
        hsmpBarCodePriceAttr.put("BAR_CODE_WEIGH", ctrBarcodePrice.get("BAR_CODE_WEIGH"));
        return isMatchPrice(hsmpBarCodePriceAttr, (HashMap) hsmpBarCodeAttr);
    }

    public static boolean isMatchPrice(HashMap<String, String> hsmpBarCodePrice, CommTableRecord ctrBarcodeAttr) {
        HashMap hsmpBarCodeAttr = new HashMap();
        hsmpBarCodeAttr.put("BAR_CODE_STUFF", ctrBarcodeAttr.get("BAR_CODE_STUFF"));
        hsmpBarCodeAttr.put("BAR_CODE_COLOR", ctrBarcodeAttr.get("BAR_CODE_COLOR"));
        hsmpBarCodeAttr.put("BAR_CODE_VOL", ctrBarcodeAttr.get("BAR_CODE_VOL"));
        hsmpBarCodeAttr.put("BAR_CODE_WEIGH", ctrBarcodeAttr.get("BAR_CODE_WEIGH"));
        return isMatchPrice((HashMap) hsmpBarCodePrice, hsmpBarCodeAttr);
    }

    public static boolean isMatchPrice(HashMap<String, String> hsmpBarCodePrice, HashMap<String, String> hsmpBarCodeAttr) {
        if (!"-1".equals(hsmpBarCodePrice.get("BAR_CODE_STUFF")) && !"-1".equals(hsmpBarCodeAttr.get("BAR_CODE_STUFF")) && !((String) hsmpBarCodePrice.get("BAR_CODE_STUFF")).equals(hsmpBarCodeAttr.get("BAR_CODE_STUFF"))) {
            return false;
        }
        if (!"-1".equals(hsmpBarCodePrice.get("BAR_CODE_COLOR")) && !"-1".equals(hsmpBarCodeAttr.get("BAR_CODE_COLOR")) && !((String) hsmpBarCodePrice.get("BAR_CODE_COLOR")).equals(hsmpBarCodeAttr.get("BAR_CODE_COLOR"))) {
            return false;
        }
        int div;
        boolean BAR_CODE_PRICE_BOUND_MIN_INCLUDE = "INCLUDE".equalsIgnoreCase(SysConfig.get("BAR_CODE_PRICE_BOUND_MIN"));
        if (!"-1".equals(hsmpBarCodePrice.get("BAR_CODE_VOL"))) {
            String BAR_CODE_VOL = (String) hsmpBarCodePrice.get("BAR_CODE_VOL");
            double ATTR_BAR_CODE_VOL = Double.parseDouble((String) hsmpBarCodeAttr.get("BAR_CODE_VOL"));
            div = BAR_CODE_VOL.indexOf(SimpleFormatter.DEFAULT_DELIMITER);
            if (div != -1) {
                double BAR_CODE_VOL_MIN = Double.parseDouble(BAR_CODE_VOL.substring(0, div));
                double BAR_CODE_VOL_MAX = Double.parseDouble(BAR_CODE_VOL.substring(div + 1));
                if (BAR_CODE_VOL_MIN == BAR_CODE_VOL_MAX) {
                    if (BAR_CODE_VOL_MIN != ATTR_BAR_CODE_VOL) {
                        return false;
                    }
                } else if (BAR_CODE_PRICE_BOUND_MIN_INCLUDE) {
                    if (BAR_CODE_VOL_MIN > ATTR_BAR_CODE_VOL || ATTR_BAR_CODE_VOL >= BAR_CODE_VOL_MAX) {
                        return false;
                    }
                } else if (BAR_CODE_VOL_MIN >= ATTR_BAR_CODE_VOL || ATTR_BAR_CODE_VOL > BAR_CODE_VOL_MAX) {
                    return false;
                }
            } else if (Double.parseDouble(BAR_CODE_VOL) != ATTR_BAR_CODE_VOL) {
                return false;
            }
        }
        if (!("-1".equals(hsmpBarCodePrice.get("BAR_CODE_WEIGH")) || "-1".equals(hsmpBarCodeAttr.get("BAR_CODE_WEIGH")))) {
            String BAR_CODE_WEIGH = (String) hsmpBarCodePrice.get("BAR_CODE_WEIGH");
            double ATTR_BAR_CODE_WEIGH = Double.parseDouble((String) hsmpBarCodeAttr.get("BAR_CODE_WEIGH"));
            div = BAR_CODE_WEIGH.indexOf(SimpleFormatter.DEFAULT_DELIMITER);
            if (div != -1) {
                double BAR_CODE_WEIGH_MIN = Double.parseDouble(BAR_CODE_WEIGH.substring(0, div));
                double BAR_CODE_WEIGH_MAX = Double.parseDouble(BAR_CODE_WEIGH.substring(div + 1));
                if (BAR_CODE_WEIGH_MIN == BAR_CODE_WEIGH_MAX) {
                    if (BAR_CODE_WEIGH_MIN != ATTR_BAR_CODE_WEIGH) {
                        return false;
                    }
                } else if (BAR_CODE_PRICE_BOUND_MIN_INCLUDE) {
                    if (BAR_CODE_WEIGH_MIN > ATTR_BAR_CODE_WEIGH || ATTR_BAR_CODE_WEIGH >= BAR_CODE_WEIGH_MAX) {
                        return false;
                    }
                } else if (BAR_CODE_WEIGH_MIN >= ATTR_BAR_CODE_WEIGH || ATTR_BAR_CODE_WEIGH > BAR_CODE_WEIGH_MAX) {
                    return false;
                }
            } else if (Double.parseDouble(BAR_CODE_WEIGH) != ATTR_BAR_CODE_WEIGH) {
                return false;
            }
        }
        return true;
    }
}
