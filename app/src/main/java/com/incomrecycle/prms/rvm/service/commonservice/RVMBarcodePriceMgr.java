package com.incomrecycle.prms.rvm.service.commonservice;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.commtable.CommTableRecord;
import com.incomrecycle.common.sort.SortEntity.ORDERBY;
import com.incomrecycle.common.sqlite.DBQuery;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.commonservice.RVMUtils.SortEntityIntBarCodePrice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RVMBarcodePriceMgr {
    public static final int PRICE_TYPE_DEPOSIT = 1;
    public static final int PRICE_TYPE_NORMAL = 0;
    private static final RVMBarcodePriceMgr mgr = new RVMBarcodePriceMgr();
    private String DEPOSIT_DELTA_FLAG = null;
    private List<CommTableRecord> listMarkPrice = null;
    private List<CommTableRecord> listPrice = null;

    private RVMBarcodePriceMgr() {
    }

    public static RVMBarcodePriceMgr getMgr() {
        return mgr;
    }

    public void load(int type, boolean isForce) {
        String table;
        boolean isToLoad = false;
        if (isForce) {
            isToLoad = true;
        }
        if (1 == type) {
            table = "RVM_BAR_CODE_PRICE_DEPOSIT";
            if (this.listMarkPrice == null) {
                isToLoad = true;
            }
        } else {
            table = "RVM_BAR_CODE_PRICE";
            if (this.listPrice == null) {
                isToLoad = true;
            }
        }
        if (isToLoad) {
            DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
            if (1 == type) {
                SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
                sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "DEPOSIT_DELTA_FLAG");
                CommTable commTableSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
                if (commTableSysCode.getRecordCount() > 0) {
                    this.DEPOSIT_DELTA_FLAG = commTableSysCode.getRecord(0).get("SYS_CODE_VALUE");
                }
            }
            CommTable ctBarCodePrice = dbQuery.getCommTable("select * from " + table);
            if (ctBarCodePrice.getRecordCount() > 0) {
                List<CommTableRecord> listCtr = new ArrayList();
                for (int p = 0; p < ctBarCodePrice.getRecordCount(); p++) {
                    CommTableRecord ctr = ctBarCodePrice.getRecord(p);
                    if (Double.parseDouble(ctr.get("BAR_CODE_AMOUNT")) > 0.0d) {
                        listCtr.add(ctr);
                    }
                }
                new SortEntityIntBarCodePrice(listCtr, "HIGHEST".equalsIgnoreCase(SysConfig.get("BAR_CODE_PRICE_PRIORITY_ZERO")) ? ORDERBY.ASC : ORDERBY.DESC).sort();
                if (1 == type) {
                    this.listMarkPrice = listCtr;
                } else {
                    this.listPrice = listCtr;
                }
            }
        }
    }

    public String getStuffPrice(int type, String vol, String stuff, String weigh, String color) {
        List<CommTableRecord> listPrice;
        load(type, false);
        if (1 == type) {
            listPrice = this.listMarkPrice;
        } else {
            listPrice = this.listPrice;
        }
        boolean BAR_CODE_PRICE_PRIORITY_LOWER_PRICE_HIGHEST = "HIGHEST".equalsIgnoreCase(SysConfig.get("BAR_CODE_PRICE_PRIORITY_LOWER_PRICE"));
        HashMap hsmpBarCodeAttr = new HashMap();
        hsmpBarCodeAttr.put("BAR_CODE_VOL", vol);
        hsmpBarCodeAttr.put("BAR_CODE_STUFF", stuff);
        hsmpBarCodeAttr.put("BAR_CODE_WEIGH", weigh);
        hsmpBarCodeAttr.put("BAR_CODE_COLOR", color);
        String BAR_CODE_PRICE = null;
        if (listPrice == null || listPrice.size() <= 0) {
            SysConfig.set("RVM_PRICE_VERSION", "0");
        } else {
            int p = 0;
            while (p < listPrice.size()) {
                CommTableRecord ctrBarCodePrice = (CommTableRecord) listPrice.get(p);
                if (!RVMUtils.isMatchPrice(ctrBarCodePrice, hsmpBarCodeAttr)) {
                    p++;
                } else if (null == null) {
                    BAR_CODE_PRICE = ctrBarCodePrice.get("BAR_CODE_AMOUNT");
                } else if (BAR_CODE_PRICE_PRIORITY_LOWER_PRICE_HIGHEST) {
                    if (Double.parseDouble(null) > Double.parseDouble(ctrBarCodePrice.get("BAR_CODE_AMOUNT"))) {
                        BAR_CODE_PRICE = ctrBarCodePrice.get("BAR_CODE_AMOUNT");
                    }
                } else if (Double.parseDouble(null) < Double.parseDouble(ctrBarCodePrice.get("BAR_CODE_AMOUNT"))) {
                    BAR_CODE_PRICE = ctrBarCodePrice.get("BAR_CODE_AMOUNT");
                }
            }
        }
        if (BAR_CODE_PRICE == null) {
            BAR_CODE_PRICE = "0";
        }
        if (1 != type) {
            return BAR_CODE_PRICE;
        }
        if ("0".equals(BAR_CODE_PRICE)) {
            return getStuffPrice(0, vol, stuff, weigh, color);
        }
        if (!"1".equals(this.DEPOSIT_DELTA_FLAG)) {
            return BAR_CODE_PRICE;
        }
        String NORMAL_BAR_CODE_PRICE = getStuffPrice(0, vol, stuff, weigh, color);
        if ("0".equals(NORMAL_BAR_CODE_PRICE)) {
            return BAR_CODE_PRICE;
        }
        return Double.toString(Double.parseDouble(BAR_CODE_PRICE) + Double.parseDouble(NORMAL_BAR_CODE_PRICE));
    }

    public String getBarcodePrice(int type, String barCode) {
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("BAR_CODE", barCode);
        CommTable commTableBarCode = dbQuery.getCommTable("select * from rvm_bar_code " + sqlWhereBuilder.toSqlWhere("where"));
        String BAR_CODE_VOL = "0";
        String BAR_CODE_STUFF = "0";
        String BAR_CODE_COLOR = "0";
        String BAR_CODE_WEIGH = "0";
        if (commTableBarCode.getRecordCount() > 0) {
            BAR_CODE_VOL = commTableBarCode.getRecord(0).get("BAR_CODE_VOL");
            BAR_CODE_STUFF = commTableBarCode.getRecord(0).get("BAR_CODE_STUFF");
            BAR_CODE_COLOR = commTableBarCode.getRecord(0).get("BAR_CODE_COLOR");
            BAR_CODE_WEIGH = commTableBarCode.getRecord(0).get("BAR_CODE_WEIGH");
        }
        return getStuffPrice(type, BAR_CODE_VOL, BAR_CODE_STUFF, BAR_CODE_WEIGH, BAR_CODE_COLOR);
    }
}
