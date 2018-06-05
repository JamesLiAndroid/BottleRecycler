package com.incomrecycle.prms.rvm.service.commonservice.gui;

import android.database.sqlite.SQLiteDatabase;
import com.google.code.microlog4android.format.SimpleFormatter;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.commtable.CommTableRecord;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.sqlite.DBQuery;
import com.incomrecycle.common.sqlite.DBSequence;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlBuilder;
import com.incomrecycle.common.sqlite.SqlDeleteBuilder;
import com.incomrecycle.common.sqlite.SqlInsertBuilder;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.RVMShell;
import com.incomrecycle.prms.rvm.common.SysDef;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.CardType;
import com.incomrecycle.prms.rvm.common.SysDef.MsgType;
import com.incomrecycle.prms.rvm.common.SysDef.ProductType;
import com.incomrecycle.prms.rvm.common.SysDef.VendingWay;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

public class GUIRecycleCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if ("initApp".equalsIgnoreCase(subSvnName)) {
            return initApp(svcName, subSvnName, hsmpParam);
        }
        if ("initVendingWay".equalsIgnoreCase(subSvnName)) {
            return initVendingWay(svcName, subSvnName, hsmpParam);
        }
        if ("initProductType".equalsIgnoreCase(subSvnName)) {
            return initProductType(svcName, subSvnName, hsmpParam);
        }
        if ("queryVendingWay".equalsIgnoreCase(subSvnName)) {
            return queryVendingWay(svcName, subSvnName, hsmpParam);
        }
        if ("queryCardTypeAndNumber".equalsIgnoreCase(subSvnName)) {
            return queryCardTypeAndNumber(svcName, subSvnName, hsmpParam);
        }
        if ("uploadOptDetail".equalsIgnoreCase(subSvnName)) {
            return uploadOptDetail(svcName, subSvnName, hsmpParam);
        }
        if ("queryCardStatus".equalsIgnoreCase(subSvnName)) {
            return queryCardStatus(svcName, subSvnName, hsmpParam);
        }
        if ("initRecycle".equalsIgnoreCase(subSvnName)) {
            return initRecycle(svcName, subSvnName, hsmpParam);
        }
        if ("recycleStart".equalsIgnoreCase(subSvnName)) {
            return recycleStart(svcName, subSvnName, hsmpParam);
        }
        if ("recycleEnd".equalsIgnoreCase(subSvnName)) {
            return recycleEnd(svcName, subSvnName, hsmpParam);
        }
        if ("initResource".equalsIgnoreCase(subSvnName)) {
            return initResource(svcName, subSvnName, hsmpParam);
        }
        if ("showOnDigitalScreen".equalsIgnoreCase(subSvnName)) {
            return showOnDigitalScreen(svcName, subSvnName, hsmpParam);
        }
        if ("queryPaperDoorStatus".equalsIgnoreCase(subSvnName)) {
            return queryPaperDoorStatus(svcName, subSvnName, hsmpParam);
        }
        if ("forceStopPutPaper".equalsIgnoreCase(subSvnName)) {
            return forceStopPutPaper(svcName, subSvnName, hsmpParam);
        }
        if ("informEntityToThanks".equalsIgnoreCase(subSvnName)) {
            return informEntityToThanks(svcName, subSvnName, hsmpParam);
        }
        if ("queryBottleMaxValue".equalsIgnoreCase(subSvnName)) {
            return queryBottleMaxValue(svcName, subSvnName, hsmpParam);
        }
        if ("updateBottleMaxValue".equalsIgnoreCase(subSvnName)) {
            return updateBottleMaxValue(svcName, subSvnName, hsmpParam);
        }
        if ("closeBottleDoor".equalsIgnoreCase(subSvnName)) {
            return closeBottleDoor(svcName, subSvnName, hsmpParam);
        }
        if ("openBottleDoor".equalsIgnoreCase(subSvnName)) {
            return openBottleDoor(svcName, subSvnName, hsmpParam);
        }
        if ("queryBottleDoorState".equalsIgnoreCase(subSvnName)) {
            return queryBottleDoorState(svcName, subSvnName, hsmpParam);
        }
        if ("checkDB".equalsIgnoreCase(subSvnName)) {
            return checkDB(svcName, subSvnName, hsmpParam);
        }
        if ("checkDBbySelect".equalsIgnoreCase(subSvnName)) {
            return checkDBbySelect(svcName, subSvnName, hsmpParam);
        }
        if ("queryPaperMaxValue".equalsIgnoreCase(subSvnName)) {
            return queryPaperMaxValue(svcName, subSvnName, hsmpParam);
        }
        if ("youkuMovieTicket".equalsIgnoreCase(subSvnName)) {
            return youkuMovieTicket(svcName, subSvnName, hsmpParam);
        }
        if ("add_click".equalsIgnoreCase(subSvnName)) {
            return addClickSum(svcName, subSvnName, hsmpParam);
        }
        if ("limitBottleCount".equalsIgnoreCase(subSvnName)) {
            return limitBottleCount(svcName, subSvnName, hsmpParam);
        }
        if ("cutPrice".equalsIgnoreCase(subSvnName)) {
            return cutPrice(svcName, subSvnName, hsmpParam);
        }
        return null;
    }

    private HashMap youkuMovieTicket(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String SELECT_FLAG;
        String CARD_NO = (String) ServiceGlobal.getCurrentSession("CARD_NO");
        String CARD_TYPE = (String) ServiceGlobal.getCurrentSession("CARD_TYPE");
        String OPT_ID = (String) ServiceGlobal.getCurrentSession("OPT_ID");
        Date dDate = new Date();
        Long Time = Long.valueOf(dDate.getTime());
        String QuTime = Time.toString();
        HashMap<String, Object> map = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
        HashMap<String, String> mapVendingWay = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
        if (map != null) {
            SELECT_FLAG = (String) ((HashMap) map.get("TRANSMIT_ADV")).get(AllAdvertisement.SELECT_FLAG);
        } else if (mapVendingWay != null) {
            SELECT_FLAG = (String) mapVendingWay.get(AllAdvertisement.SELECT_FLAG);
        } else {
            SELECT_FLAG = "";
        }
        HashMap hsmpPkg = new HashMap();
        hsmpPkg.put("MES_TYPE", MsgType.RVM_YOUKU);
        hsmpPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
        hsmpPkg.put("QU_TIME", QuTime);
        if ("CARD".equals(CARD_TYPE)) {
            hsmpPkg.put("CARD_TYPE", "1");
        } else if ("TRANSPORTCARD".equals(CARD_TYPE)) {
            hsmpPkg.put("CARD_TYPE", "2");
        } else if ("QRCODE".equals(CARD_TYPE)) {
            hsmpPkg.put("CARD_TYPE", "3");
        } else if ("PHONE".equals(CARD_TYPE)) {
            hsmpPkg.put("CARD_TYPE", CardType.MSG_PHONE);
        } else if ("SQRCODE".equals(CARD_TYPE)) {
            hsmpPkg.put("CARD_TYPE", CardType.MSG_SQRCODE);
        } else if ("WECHAT".equals(CARD_TYPE)) {
            hsmpPkg.put("CARD_TYPE", CardType.MSG_WECHAT);
        } else if ("ALIPAY".equals(CARD_TYPE)) {
            hsmpPkg.put("CARD_TYPE", CardType.MSG_ALIPAY);
        } else if ("BDJ".equals(CARD_TYPE)) {
            hsmpPkg.put("CARD_TYPE", CardType.MSG_BDJ);
        } else {
            hsmpPkg.put("CARD_TYPE", "0");
        }
        hsmpPkg.put(AllAdvertisement.SELECT_FLAG, SELECT_FLAG);
        hsmpPkg.put("CARD_NO", CARD_NO);
        hsmpPkg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + Time + OPT_ID);
        HashMap hsmpRetPkg = new HashMap();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        try {
            HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPkg)));
            SqlInsertBuilder sqlInsertBuilder;
            if (hsmpretPkg == null) {
                sqlInsertBuilder = new SqlInsertBuilder("RVM_YOUKU_MOVIE_TICKET");
                sqlInsertBuilder.newInsertRecord().setNumber("OPT_ID", OPT_ID).setString("CARD_TYPE", CARD_TYPE).setNumber("CARD_NO", CARD_NO).setDateTime("OPT_TIME", dDate).setNumber("UPLOAD_FLAG", Integer.valueOf(1)).setString(AllAdvertisement.SELECT_FLAG, SELECT_FLAG);
                listSqlBuilder.add(sqlInsertBuilder);
            } else if (((String) hsmpretPkg.get("MES_TYPE")).equalsIgnoreCase("RESPONSE")) {
                Integer Confirm = Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM")));
                if (Confirm.intValue() == 1) {
                    hsmpRetPkg.put("RESULT", "success");
                }
                if (Confirm.intValue() == -1) {
                    hsmpRetPkg.put("RESULT", "error");
                }
                if (Confirm.intValue() == 0) {
                    sqlInsertBuilder = new SqlInsertBuilder("RVM_YOUKU_MOVIE_TICKET");
                    sqlInsertBuilder.newInsertRecord().setNumber("OPT_ID", OPT_ID).setString("CARD_TYPE", CARD_TYPE).setNumber("CARD_NO", CARD_NO).setDateTime("OPT_TIME", dDate).setNumber("UPLOAD_FLAG", Integer.valueOf(1)).setString(AllAdvertisement.SELECT_FLAG, SELECT_FLAG);
                    listSqlBuilder.add(sqlInsertBuilder);
                    hsmpRetPkg.put("RESULT", "failed");
                }
            }
            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
            return hsmpRetPkg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private HashMap addClickSum(String svcName, String subSvnName, HashMap hsmpParam) {
        String key = (String) hsmpParam.get("KEY");
        if (StringUtils.isBlank(key)) {
            return null;
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        String SUM_TIME = DateUtils.formatDatetime(new Date(), "yyyyMMdd");
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("SUM_TIME", SUM_TIME);
        CommTable ct = dbQuery.getCommTable("select * from RVM_DATE_COUNT " + sqlWhereBuilder.toSqlWhere("where"));
        if (ct.getRecordCount() == 0) {
            String OP_BATCH_ID = SysConfig.get("RVM.CODE") + "_" + new Date().getTime();
            String CLICK_SUM = key + "=1";
            SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_DATE_COUNT");
            sqlInsertBuilder.newInsertRecord().setString("OP_BATCH_ID", OP_BATCH_ID).setString("SUM_TIME", SUM_TIME).setString("CLICK_SUM", CLICK_SUM).setNumber("UPLOAD_FLAG", Integer.valueOf(1));
            SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
        } else {
            HashMap<String, String> hsmpClickSum = StringUtils.toHashMap(ct.getRecord(0).get("CLICK_SUM"), ";", "=");
            String value = (String) hsmpClickSum.get(key);
            if (StringUtils.isBlank(value)) {
                value = "1";
            } else {
                value = Integer.toString(Integer.parseInt(value) + 1);
            }
            hsmpClickSum.put(key, value);
            StringBuffer sb = new StringBuffer();
            for (Entry entity : hsmpClickSum.entrySet()) {
                String v = (String) entity.getValue();
                sb.append(((String) entity.getKey()) + "=" + v + ";");
            }
            SqlWhereBuilder sqlWhereBuilderUpdate = new SqlWhereBuilder();
            sqlWhereBuilderUpdate.addNumberEqualsTo("SUM_TIME", SUM_TIME);
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_DATE_COUNT");
            sqlUpdateBuilder.setString("CLICK_SUM", sb.toString()).setSqlWhere(sqlWhereBuilderUpdate);
            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
        }
        return null;
    }

    private HashMap limitBottleCount(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if ("PAPER".equals((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"))) {
            return null;
        }
        List<String> listRecycledBottleDetail = (List) ServiceGlobal.getCurrentSession("RECYCLED_BOTTLE_DETAIL");
        if (listRecycledBottleDetail == null) {
            return null;
        }
        String OPT_ID = (String) hsmpParam.get("OPT_ID");
        String CARD_TYPE = (String) hsmpParam.get("CARD_TYPE");
        String CARD_NO = (String) hsmpParam.get("CARD_NO");
        if (StringUtils.isEmpty(CARD_NO) || StringUtils.isEmpty(CARD_TYPE) || StringUtils.isEmpty(OPT_ID)) {
            return null;
        }
        int i;
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("CARD_NO", CARD_NO).addStringEqualsTo("CARD_TYPE", CARD_TYPE).addDateEqualsTo("OPT_TIME", new Date());
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        CommTable commTable = dbQuery.getCommTable("select * from RVM_OPT " + sqlWhereBuilder.toSqlWhere("where"));
        int DAILY_BOTTLE_COUNT = 0;
        int THIS_BOTTLE_COUNT = 0;
        for (i = 0; i < commTable.getRecordCount(); i++) {
            CommTableRecord ctr = commTable.getRecord(i);
            DAILY_BOTTLE_COUNT += new Double(ctr.get("PRODUCT_AMOUNT")).intValue();
            if (ctr.get("OPT_ID").equals("" + OPT_ID)) {
                THIS_BOTTLE_COUNT = new Double(ctr.get("PRODUCT_AMOUNT")).intValue();
            }
        }
        ServiceGlobal.setCurrentSession("DAILY_BOTTLE_COUNT", Integer.valueOf(DAILY_BOTTLE_COUNT));
        ServiceGlobal.setCurrentSession("THIS_BOTTLE_COUNT", Integer.valueOf(THIS_BOTTLE_COUNT));
        int BOTTLES_DAILY_MAX = Integer.parseInt(SysConfig.get("BOTTLES.DAILY.MAX"));
        if (BOTTLES_DAILY_MAX < DAILY_BOTTLE_COUNT) {
            String barcode;
            Integer bottleCount;
            int DONATION_BOTTLE_COUNT = DAILY_BOTTLE_COUNT - BOTTLES_DAILY_MAX;
            if (DONATION_BOTTLE_COUNT > THIS_BOTTLE_COUNT) {
                DONATION_BOTTLE_COUNT = THIS_BOTTLE_COUNT;
            }
            ServiceGlobal.setCurrentSession("DONATION_BOTTLE_COUNT", Integer.valueOf(DONATION_BOTTLE_COUNT));
            HashMap<String, Integer> hsmpDonationBottle = new HashMap();
            for (i = 0; i < DONATION_BOTTLE_COUNT; i++) {
                barcode = (String) listRecycledBottleDetail.get((listRecycledBottleDetail.size() + i) - DONATION_BOTTLE_COUNT);
                bottleCount = (Integer) hsmpDonationBottle.get(barcode);
                if (bottleCount == null) {
                    bottleCount = Integer.valueOf(1);
                } else {
                    bottleCount = Integer.valueOf(bottleCount.intValue() + 1);
                }
                hsmpDonationBottle.put(barcode, bottleCount);
            }
            List<SqlBuilder> listSqlBuilder = new ArrayList();
            SqlWhereBuilder sqlWhereBuilderOptId = new SqlWhereBuilder();
            sqlWhereBuilderOptId.addNumberEqualsTo("OPT_ID", OPT_ID);
            double donationAmount = 0.0d;
            commTable = dbQuery.getCommTable("select * from RVM_OPT_BOTTLE " + sqlWhereBuilderOptId.toSqlWhere("where"));
            for (i = 0; i < commTable.getRecordCount(); i++) {
                CommTableRecord ctr = commTable.getRecord(i);
                barcode = ctr.get("BOTTLE_BAR_CODE");
                bottleCount = (Integer) hsmpDonationBottle.get(barcode);
                if (bottleCount != null) {
                    donationAmount += ((double) bottleCount.intValue()) * Double.parseDouble(ctr.get("BOTTLE_AMOUNT"));
                    listSqlBuilder.add(new SqlUpdateBuilder("RVM_OPT_BOTTLE").addNumber("VENDING_BOTTLE_COUNT", SimpleFormatter.DEFAULT_DELIMITER + bottleCount).setSqlWhere(new SqlWhereBuilder().addNumberEqualsTo("OPT_ID", OPT_ID).addStringEqualsTo("BOTTLE_BAR_CODE", barcode)));
                }
            }
            listSqlBuilder.add(new SqlUpdateBuilder("RVM_OPT").addNumber("PROFIT_AMOUNT", SimpleFormatter.DEFAULT_DELIMITER + donationAmount).setSqlWhere(new SqlWhereBuilder().addNumberEqualsTo("OPT_ID", OPT_ID)));
            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
            List<HashMap<String, String>> listHsmpRecycleBottle = (List) ServiceGlobal.getCurrentSession("RECYCLED_BOTTLE_SUMMARY");
            for (i = 0; i < listHsmpRecycleBottle.size(); i++) {
                HashMap<String, String> hsmpRecycleBottle = (HashMap) listHsmpRecycleBottle.get(i);
                bottleCount = (Integer) hsmpDonationBottle.get((String) hsmpRecycleBottle.get("BOTTLE_BAR_CODE"));
                if (bottleCount != null) {
                    hsmpRecycleBottle.put("VENDING_BOTTLE_COUNT", Integer.toString(Integer.parseInt((String) hsmpRecycleBottle.get("VENDING_BOTTLE_COUNT")) - bottleCount.intValue()));
                }
            }
            ServiceGlobal.setCurrentSession("RECYCLED_BOTTLE_DETAIL", null);
            return null;
        }
        ServiceGlobal.setCurrentSession("DONATION_BOTTLE_COUNT", Integer.valueOf(0));
        return null;
    }

    private HashMap cutPrice(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String OPT_ID = (String) hsmpParam.get("OPT_ID");
        String CARD_TYPE = (String) hsmpParam.get("CARD_TYPE");
        String PRODUCT_TYPE = (String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE");
        String PRE_OPT_ID = (String) ServiceGlobal.getCurrentSession("PRE_OPT_ID");
        if (StringUtils.isEmpty(CARD_TYPE) || StringUtils.isEmpty(OPT_ID) || OPT_ID.equalsIgnoreCase(PRE_OPT_ID)) {
            return null;
        }
        ServiceGlobal.setCurrentSession("PRE_OPT_ID", OPT_ID);
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderVendingWay = new SqlWhereBuilder();
        sqlWhereBuilderVendingWay.addStringEqualsTo(AllAdvertisement.VENDING_WAY, CARD_TYPE).addStringEqualsTo("PRODUCT_TYPE", PRODUCT_TYPE);
        CommTable commTablePriceMap = dbQuery.getCommTable("select * from RVM_PRICE_MAP " + sqlWhereBuilderVendingWay.toSqlWhere("where"));
        String priceMapStr = null;
        if (commTablePriceMap.getRecordCount() != 0) {
            priceMapStr = commTablePriceMap.getRecord(0).get("PRICE_MAP");
        }
        if (StringUtils.isBlank(priceMapStr)) {
            return null;
        }
        int i;
        Double BottleAmount;
        HashMap priceMap = new HashMap();
        double totalAmount = 0.0d;
        if (!StringUtils.isBlank(priceMapStr)) {
            String[] priceMapArray = priceMapStr.trim().split(";");
            for (i = 0; i < priceMapArray.length; i++) {
                int div = priceMapArray[i].indexOf(":");
                if (div > 0) {
                    String bottleOriginal = priceMapArray[i].substring(0, div);
                    String bottleAdjust = priceMapArray[i].substring(div + 1);
                    try {
                        double originalPrice = Double.valueOf(bottleOriginal).doubleValue();
                        double adjustPrice = Double.valueOf(bottleAdjust).doubleValue();
                        priceMap.put(Double.valueOf(originalPrice), Double.valueOf(adjustPrice));
                    } catch (Exception e) {
                    }
                }
            }
        }
        String PAPER_PRICE_AMOUNT = null;
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        SqlWhereBuilder sqlWhereBuilderOPT = new SqlWhereBuilder();
        sqlWhereBuilderOPT.addNumberEqualsTo("OPT_ID", OPT_ID);
        if ("BOTTLE".equals(PRODUCT_TYPE)) {
            CommTable commTableOPTBottle = dbQuery.getCommTable("select * from RVM_OPT_BOTTLE " + sqlWhereBuilderOPT.toSqlWhere("where"));
            if (commTableOPTBottle.getRecordCount() != 0) {
                for (int j = 0; j < commTableOPTBottle.getRecordCount(); j++) {
                    String BottleBarCode = commTableOPTBottle.getRecord(j).get("BOTTLE_BAR_CODE");
                    BottleAmount = Double.valueOf(commTableOPTBottle.getRecord(j).get("BOTTLE_AMOUNT"));
                    Integer BottleCount = Integer.valueOf(commTableOPTBottle.getRecord(j).get("BOTTLE_COUNT"));
                    try {
                        if (priceMap.get(BottleAmount) != null) {
                            BottleAmount = (Double) priceMap.get(BottleAmount);
                        }
                        totalAmount += ((double) BottleCount.intValue()) * BottleAmount.doubleValue();
                        listSqlBuilder.add(new SqlUpdateBuilder("RVM_OPT_BOTTLE").setNumber("BOTTLE_AMOUNT", BottleAmount).setSqlWhere(new SqlWhereBuilder().addNumberEqualsTo("OPT_ID", OPT_ID).addStringEqualsTo("BOTTLE_BAR_CODE", BottleBarCode)));
                    } catch (Exception e2) {
                    }
                }
            }
        } else {
            CommTable commTableOPTPaper = dbQuery.getCommTable("select * from RVM_OPT" + sqlWhereBuilderOPT.toSqlWhere("where"));
            if (commTableOPTPaper.getRecordCount() != 0) {
                Double paperWeight = Double.valueOf(commTableOPTPaper.getRecord(0).get("PRODUCT_AMOUNT"));
                try {
                    CommTable commTablePaper = dbQuery.getCommTable("select * from RVM_PAPER_PRICE ");
                    double PAPER_WEIGH = 0.0d;
                    double PAPER_PRICE = 0.0d;
                    if (commTablePaper.getRecordCount() > 0) {
                        PAPER_WEIGH = Double.valueOf(commTablePaper.getRecord(0).get("PAPER_WEIGH")).doubleValue();
                        PAPER_PRICE = Double.valueOf(commTablePaper.getRecord(0).get("PAPER_PRICE")).doubleValue();
                        if (priceMap.get(Double.valueOf(PAPER_PRICE)) != null) {
                            PAPER_PRICE = ((Double) priceMap.get(Double.valueOf(PAPER_PRICE))).doubleValue();
                        }
                    }
                    PAPER_PRICE_AMOUNT = new DecimalFormat("########0").format(Double.valueOf((paperWeight.doubleValue() / PAPER_WEIGH) * PAPER_PRICE));
                    totalAmount = Double.parseDouble(PAPER_PRICE_AMOUNT);
                } catch (Exception e3) {
                }
            }
        }
        SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
        sqlUpdateBuilder.setNumber("PROFIT_AMOUNT", Double.valueOf(totalAmount)).setSqlWhere(sqlWhereBuilderOPT);
        listSqlBuilder.add(sqlUpdateBuilder);
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        if ("BOTTLE".equals(PRODUCT_TYPE)) {
            List<HashMap<String, String>> listHsmpRecycleBottle = (List) ServiceGlobal.getCurrentSession("RECYCLED_BOTTLE_SUMMARY");
            for (i = 0; i < listHsmpRecycleBottle.size(); i++) {
                HashMap<String, String> hsmpRecycleBottle = (HashMap) listHsmpRecycleBottle.get(i);
                BottleAmount = Double.valueOf((String) hsmpRecycleBottle.get("BOTTLE_AMOUNT"));
                if (!(BottleAmount == null || priceMap.get(BottleAmount) == null)) {
                    hsmpRecycleBottle.put("BOTTLE_AMOUNT", ((Double) priceMap.get(BottleAmount)).toString());
                }
            }
        } else {
            ((HashMap) ServiceGlobal.getCurrentSession("RECYCLED_PAPER_SUMMARY")).put("PAPER_PRICE", PAPER_PRICE_AMOUNT);
        }
        return null;
    }

    private HashMap queryPaperMaxValue(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberIn("ALARM_ID", Integer.valueOf(13), Integer.valueOf(14));
        CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_ALARM " + sqlWhereBuilder.toSqlWhere("where"));
        HashMap hsmpResult = new HashMap();
        String tsdPaperValueMax = null;
        String tsdPaperValueAlarm = null;
        if (commTable.getRecordCount() != 0) {
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                if (commTable.getRecord(i).get("ALARM_ID").equals("13")) {
                    tsdPaperValueMax = commTable.getRecord(i).get("TSD_VALUE");
                }
                if (commTable.getRecord(i).get("ALARM_ID").equals("14")) {
                    tsdPaperValueAlarm = commTable.getRecord(i).get("TSD_VALUE");
                }
            }
        } else {
            tsdPaperValueMax = "0";
            tsdPaperValueAlarm = "0";
        }
        hsmpResult.put("paperMax", tsdPaperValueMax);
        hsmpResult.put("paperAlarm", tsdPaperValueAlarm);
        return hsmpResult;
    }

    private HashMap checkDBbySelect(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hspCheckDBRet = new HashMap();
        String result = (String) hsmpParam.get("RESULT");
        if (result == null) {
            return null;
        }
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_CHECK_DATABASE ");
        if (commTable.getRecordCount() == 0 && "insertSuccess".equalsIgnoreCase(result)) {
            hspCheckDBRet.put("RESULT", NetworkUtils.NET_STATE_FAILED);
            return hspCheckDBRet;
        }
        if (commTable.getRecordCount() != 0) {
            if ("updateSuccess".equalsIgnoreCase(result)) {
                for (int i = 0; i < commTable.getRecordCount(); i++) {
                    if ("updateDB".equalsIgnoreCase(commTable.getRecord(i).get("CHECK_NAME"))) {
                        hspCheckDBRet.put("RESULT", NetworkUtils.NET_STATE_SUCCESS);
                    }
                }
                return hspCheckDBRet;
            } else if ("insertSuccess".equalsIgnoreCase(result)) {
                hspCheckDBRet.put("RESULT", NetworkUtils.NET_STATE_SUCCESS);
                return hspCheckDBRet;
            }
        }
        return null;
    }

    private HashMap checkDB(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpret = new HashMap();
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        String checkOptId = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS")).getSeq("OPT_ID");
        String insertDB = "insertDB";
        String updateDB = "updateDB";
        try {
            CommTable commTable = dbQuery.getCommTable("select * from RVM_CHECK_DATABASE ");
            if (commTable.getRecordCount() == 0) {
                SqlInsertBuilder sqlInsertBuilderRvmCheck = new SqlInsertBuilder("RVM_CHECK_DATABASE");
                sqlInsertBuilderRvmCheck.newInsertRecord().setNumber("CHECK_ID", checkOptId).setString("CHECK_NAME", insertDB).setDateTime("CHECK_TIME", new Date());
                listSqlBuilder.add(sqlInsertBuilderRvmCheck);
                SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                hsmpret.put("RESULT", "insertSuccess");
                return checkDBbySelect(svcName, subSvnName, hsmpret);
            } else if (commTable.getRecordCount() != 1) {
                return null;
            } else {
                SqlUpdateBuilder sqlUpdateBuilderRvmCheck = new SqlUpdateBuilder("RVM_CHECK_DATABASE");
                sqlUpdateBuilderRvmCheck.setString("CHECK_NAME", updateDB).setTime("CHECK_TIME", new Date());
                listSqlBuilder.add(sqlUpdateBuilderRvmCheck);
                SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                hsmpret.put("RESULT", "updateSuccess");
                return checkDBbySelect(svcName, subSvnName, hsmpret);
            }
        } catch (Exception e) {
            hsmpret.put("RESULT", "checkFailed");
            return hsmpret;
        }
    }

    private HashMap queryBottleDoorState(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String str = CommService.getCommService().execute("QUERY_BOTTLE_DOOR_STATE", null);
        HashMap hsmp = new HashMap();
        hsmp.put("BottleDoorStatus", str);
        return hsmp;
    }

    private HashMap openBottleDoor(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if (hsmpParam != null && "TRUE".equalsIgnoreCase((String) hsmpParam.get("SAVE_CONFIG"))) {
            Properties prop = new Properties();
            prop.setProperty("COM.PLC.DOOR.STATE.INIT", "OPEN");
            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), prop);
            SysConfig.set(prop);
            RVMShell.backupExternalConfig();
        }
        CommService.getCommService().execute("OPEN_BOTTLE_DOOR", "");
        return null;
    }

    private HashMap closeBottleDoor(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if (hsmpParam != null && "TRUE".equalsIgnoreCase((String) hsmpParam.get("SAVE_CONFIG"))) {
            Properties prop = new Properties();
            prop.setProperty("COM.PLC.DOOR.STATE.INIT", "CLOSE");
            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), prop);
            SysConfig.set(prop);
            RVMShell.backupExternalConfig();
        }
        CommService.getCommService().execute("CLOSE_BOTTLE_DOOR", "");
        return null;
    }

    private HashMap updateBottleMaxValue(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpResult = new HashMap();
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        String textBottleMax = (String) hsmpParam.get("EditMaxText");
        String textBottleAlarm = (String) hsmpParam.get("EditAlarmText");
        int bottleMaxNum = Integer.valueOf(textBottleMax).intValue();
        int bottleAlarmNum = Integer.valueOf(textBottleAlarm).intValue();
        if (StringUtils.isBlank(textBottleMax) || StringUtils.isBlank(textBottleAlarm)) {
            hsmpResult.put("RET_VALUE", "NO_SET_VALUE");
        } else {
            SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addNumberEqualsTo("ALARM_ID", Integer.valueOf(11));
            if (DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_ALARM " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() != 0) {
                SqlUpdateBuilder sqlUpdateBuilderRvmOpt = new SqlUpdateBuilder(MsgType.RVM_ALARM);
                sqlUpdateBuilderRvmOpt.setNumber("TSD_VALUE", Integer.valueOf(bottleMaxNum)).setSqlWhere(sqlWhereBuilder);
                listSqlBuilder.add(sqlUpdateBuilderRvmOpt);
            } else {
                SqlInsertBuilder sqlInsertBuilderRvmOpt = new SqlInsertBuilder(MsgType.RVM_ALARM);
                sqlInsertBuilderRvmOpt.newInsertRecord().setNumber("ALARM_ID", Integer.valueOf(11)).setNumber("TSD_TYPE", Integer.valueOf(1)).setNumber("TSD_VALUE", Integer.valueOf(bottleMaxNum)).setNumber("AM_LEVEL", Integer.valueOf(3));
                listSqlBuilder.add(sqlInsertBuilderRvmOpt);
            }
            SqlWhereBuilder sqlWhereBuilder1 = new SqlWhereBuilder();
            sqlWhereBuilder1.addNumberEqualsTo("ALARM_ID", Integer.valueOf(12));
            if (DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_ALARM " + sqlWhereBuilder1.toSqlWhere("where")).getRecordCount() != 0) {
                SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder(MsgType.RVM_ALARM);
                sqlUpdateBuilder.setNumber("TSD_VALUE", Integer.valueOf(bottleAlarmNum)).setSqlWhere(sqlWhereBuilder1);
                listSqlBuilder.add(sqlUpdateBuilder);
            } else {
                SqlInsertBuilder sqlInsertBuilderRvmOpt1 = new SqlInsertBuilder(MsgType.RVM_ALARM);
                sqlInsertBuilderRvmOpt1.newInsertRecord().setNumber("ALARM_ID", Integer.valueOf(12)).setNumber("TSD_TYPE", Integer.valueOf(1)).setNumber("TSD_VALUE", Integer.valueOf(bottleAlarmNum)).setNumber("AM_LEVEL", Integer.valueOf(3));
                listSqlBuilder.add(sqlInsertBuilderRvmOpt1);
            }
            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
            hsmpResult.put("RET_VALUE", "SUCCESS_SET_VALUE");
        }
        return hsmpResult;
    }

    private HashMap queryBottleMaxValue(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberIn("ALARM_ID", Integer.valueOf(11), Integer.valueOf(12));
        CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_ALARM " + sqlWhereBuilder.toSqlWhere("where"));
        HashMap hsmpResult = new HashMap();
        String tsdValueMax = null;
        String tsdValueAlarm = null;
        if (commTable.getRecordCount() != 0) {
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                if (commTable.getRecord(i).get("ALARM_ID").equals("11")) {
                    tsdValueMax = commTable.getRecord(i).get("TSD_VALUE");
                }
                if (commTable.getRecord(i).get("ALARM_ID").equals("12")) {
                    tsdValueAlarm = commTable.getRecord(i).get("TSD_VALUE");
                }
            }
        } else {
            tsdValueMax = "0";
            tsdValueAlarm = "0";
        }
        hsmpResult.put("bottleMax", tsdValueMax);
        hsmpResult.put("bottleAlarm", tsdValueAlarm);
        return hsmpResult;
    }

    public HashMap recycleStart(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        try {
            String PRODUCT_TYPE = (String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE");
            String command = null;
            if ("BOTTLE".equalsIgnoreCase(PRODUCT_TYPE)) {
                command = "RECYCLE_START";
            } else if ("PAPER".equalsIgnoreCase(PRODUCT_TYPE)) {
                command = "RECYCLE_PAPER_START";
            }
            CommService.getCommService().execute(command, "");
        } catch (Exception e) {
        }
        return null;
    }

    private HashMap recycleEnd(String svcName, String subSvnName, HashMap hsmpParam) {
        try {
            String PRODUCT_TYPE = (String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE");
            String command = null;
            if ("BOTTLE".equalsIgnoreCase(PRODUCT_TYPE)) {
                command = "RECYCLE_STOP";
            } else if ("PAPER".equalsIgnoreCase(PRODUCT_TYPE)) {
                command = "RECYCLE_PAPER_FINISH";
            }
            CommService.getCommService().execute(command, "");
        } catch (Exception e) {
        }
        return null;
    }

    private HashMap queryCardStatus(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String CARD_TYPE = (String) hsmpParam.get("CARD_TYPE");
        String CARD_NO = (String) hsmpParam.get("CARD_NO");
        Long Time = Long.valueOf(new Date().getTime());
        String QuTime = Time.toString();
        HashMap hsmpPkg = new HashMap();
        hsmpPkg.put("MES_TYPE", MsgType.RVM_CARD_STATUS);
        hsmpPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
        hsmpPkg.put("QU_TIME", QuTime);
        hsmpPkg.put("CARD_TYPE", CardType.getMsgCardType(CARD_TYPE));
        hsmpPkg.put("CARD_NO", CARD_NO);
        hsmpPkg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + Time);
        try {
            HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPkg)));
            if (hsmpretPkg == null) {
                return null;
            }
            String termNo = (String) hsmpretPkg.get("TERM_NO");
            String areaId = (String) hsmpretPkg.get("LOCAL_AREA_ID");
            if (MsgType.RCC_CARD_STATUS.equalsIgnoreCase((String) hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                HashMap hsmpResult = new HashMap();
                hsmpResult.put("INCOM_AMOUNT", hsmpretPkg.get("INCOM_AMOUNT"));
                hsmpResult.put("CARD_STATUS", hsmpretPkg.get("CARD_STATUS"));
                hsmpResult.put("RECHARGE", hsmpretPkg.get("RECHARGE"));
                hsmpResult.put("IS_RECHANGE", hsmpretPkg.get("IS_RECHANGE"));
                hsmpResult.put("CARD_NAME", hsmpretPkg.get("CARD_NAME"));
                hsmpResult.put("CREDIT", hsmpretPkg.get("CREDIT"));
                hsmpResult.put("TODAY_BOTTLE", hsmpretPkg.get("TODAY_BOTTLE"));
                return hsmpResult;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap uploadOptDetail(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String optId = (String) hsmpParam.get("OPT_ID");
        String putdownCount = "0";
        String putdownCountDelta = "0";
        String putdownCountReal = "0";
        double recycleAmout = 0.0d;
        if (hsmpParam.get("RECYCLED_AMOUNT") != null) {
            recycleAmout = ((Double) hsmpParam.get("RECYCLED_AMOUNT")).doubleValue();
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("OPT_TYPE", "RECYCLE").addNumberEqualsTo("OPT_STATUS", Integer.valueOf(1)).addNumberEqualsTo("OPT_ID", optId);
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        CommTable commTable = dbQuery.getCommTable("select * from RVM_OPT " + sqlWhereBuilder.toSqlWhere("where"));
        HashMap hsmpResult = new HashMap();
        if (commTable.getRecordCount() == 0) {
            return hsmpResult;
        }
        try {
            HashMap<String, Object> hsmpStorageCount = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryStorageCount", null);
            putdownCount = (String) hsmpStorageCount.get("STORAGE_CURR_COUNT");
            putdownCountDelta = (String) hsmpStorageCount.get("STORAGE_CURR_COUNT_DELTA");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (StringUtils.isBlank(putdownCount)) {
            putdownCount = "0";
        }
        if (StringUtils.isBlank(putdownCountDelta)) {
            putdownCountDelta = "0";
        }
        if (Integer.parseInt(putdownCount) > Integer.parseInt(putdownCountDelta)) {
            putdownCountReal = Integer.toString(Integer.parseInt(putdownCount) - Integer.parseInt(putdownCountDelta));
        }
        for (int i = 0; i < commTable.getRecordCount(); i++) {
            String CardNO = commTable.getRecord(i).get("CARD_NO");
            String recycleQuantity = commTable.getRecord(i).get("PRODUCT_AMOUNT");
            String SELECT_FLAG = commTable.getRecord(i).get(AllAdvertisement.SELECT_FLAG);
            String vendingWay = commTable.getRecord(i).get(AllAdvertisement.VENDING_WAY);
            if (StringUtils.isBlank(vendingWay)) {
                vendingWay = "DONATION";
            }
            String productType = commTable.getRecord(i).get("PRODUCT_TYPE");
            if (StringUtils.isBlank(productType)) {
                productType = "BOTTLE";
            }
            String vendingMoney = commTable.getRecord(i).get("PROFIT_AMOUNT");
            if ("DONATION".equalsIgnoreCase(vendingWay)) {
                vendingMoney = "0";
            }
            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(commTable.getRecord(i).get("OPT_TIME"));
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            Long vendTime = Long.valueOf(date.getTime());
            String OPBatchId = SysConfig.get("RVM.CODE") + "_" + vendTime + optId;
            String CardType = commTable.getRecord(i).get("CARD_TYPE");
            HashMap hsmpSend = new HashMap();
            hsmpSend.put("MES_TYPE", MsgType.RVM_RECYCLE_DETAIL);
            hsmpSend.put("TERM_NO", SysConfig.get("RVM.CODE"));
            hsmpSend.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
            hsmpSend.put("CARD_NO", CardNO);
            hsmpSend.put(AllAdvertisement.SELECT_FLAG, SELECT_FLAG);
            hsmpSend.put("PRODUCT_TYPE", ProductType.getMsgProductType(productType));
            hsmpSend.put("RECYCLE_QUANTITY", recycleQuantity);
            hsmpSend.put(AllAdvertisement.VENDING_WAY, VendingWay.getMsgVendingRebateFlag(vendingWay));
            if (recycleAmout > 0.0d) {
                hsmpSend.put("VENDING_MONEY", Double.valueOf(recycleAmout));
            } else {
                hsmpSend.put("VENDING_MONEY", vendingMoney);
            }
            hsmpSend.put("VENDING_TIME", vendTime.toString());
            hsmpSend.put("OP_BATCH_ID", OPBatchId);
            if (SysDef.CardType.NOCARD.equals(CardType)) {
                hsmpSend.put("CARD_TYPE", "0");
            } else if ("CARD".equals(CardType)) {
                hsmpSend.put("CARD_TYPE", "1");
            } else if ("TRANSPORTCARD".equals(CardType)) {
                hsmpSend.put("CARD_TYPE", "2");
            } else if ("QRCODE".equals(CardType)) {
                hsmpSend.put("CARD_TYPE", "3");
            } else if ("PHONE".equals(CardType)) {
                hsmpSend.put("CARD_TYPE", SysDef.CardType.MSG_PHONE);
            } else if ("SQRCODE".equals(CardType)) {
                hsmpSend.put("CARD_TYPE", SysDef.CardType.MSG_SQRCODE);
            } else if ("WECHAT".equals(CardType)) {
                hsmpSend.put("CARD_TYPE", SysDef.CardType.MSG_WECHAT);
            } else if ("ALIPAY".equals(CardType)) {
                hsmpSend.put("CARD_TYPE", SysDef.CardType.MSG_ALIPAY);
            } else if ("BDJ".equals(CardType)) {
                hsmpSend.put("CARD_TYPE", SysDef.CardType.MSG_BDJ);
            } else {
                hsmpSend.put("CARD_TYPE", "0");
            }
            SqlWhereBuilder sqlWhereBuilderOPTBottle = new SqlWhereBuilder();
            sqlWhereBuilderOPTBottle.addNumberEqualsTo("OPT_ID", optId);
            CommTable commTableOPTBottle = dbQuery.getCommTable("select * from RVM_OPT_BOTTLE " + sqlWhereBuilderOPTBottle.toSqlWhere("where"));
            List BarValuesList = new ArrayList();
            if (commTableOPTBottle.getRecordCount() != 0) {
                for (int j = 0; j < commTableOPTBottle.getRecordCount(); j++) {
                    String BottleBarCode = commTableOPTBottle.getRecord(j).get("BOTTLE_BAR_CODE");
                    Double BottleAmount = Double.valueOf(commTableOPTBottle.getRecord(j).get("BOTTLE_AMOUNT"));
                    int BottleCount = Integer.parseInt(commTableOPTBottle.getRecord(j).get("BOTTLE_COUNT"));
                    int VendingBottleCount = Integer.parseInt(commTableOPTBottle.getRecord(j).get("VENDING_BOTTLE_COUNT"));
                    for (int k = 0; k < BottleCount; k++) {
                        if (k < VendingBottleCount) {
                            BarValuesList.add(BottleBarCode + "=" + BottleAmount.toString());
                        } else {
                            BarValuesList.add(BottleBarCode + "=0");
                        }
                    }
                }
            }
            hsmpSend.put("BAR_VALUES", BarValuesList);
            hsmpSend.put("REAL_BOTTLE_NUM", putdownCountReal);
            hsmpSend.put("TOTAL_BOTTLE_NUM", putdownCount);
            HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpSend)));
            if (hsmpretPkg == null) {
                return null;
            }
            String msgType = (String) hsmpretPkg.get("MES_TYPE");
            String termNo = (String) hsmpretPkg.get("TERM_NO");
            if ("RESPONSE".equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                try {
                    Integer Confirm = Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM")));
                    if (Confirm.intValue() == 1) {
                        hsmpResult.put("RESULT", "REPEAT");
                    }
                    if (Confirm.intValue() == 0) {
                        SqlWhereBuilder sqlWhereBuilderOptId = new SqlWhereBuilder();
                        sqlWhereBuilderOptId.addNumberEqualsTo("OPT_ID", optId);
                        SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
                        sqlUpdateBuilder.setNumber("OPT_STATUS", Integer.valueOf(-1)).setSqlWhere(sqlWhereBuilderOptId);
                        SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                        hsmpResult.put("RESULT", NetworkUtils.NET_STATE_FAILED);
                    }
                    if (Confirm.intValue() == -1) {
                        return null;
                    }
                } catch (Exception e3) {
                    return null;
                }
            }
            if (MsgType.RCC_CARD_STATUS.equalsIgnoreCase(msgType) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                SqlWhereBuilder sqlWhereBuilderOptId = new SqlWhereBuilder();
                sqlWhereBuilderOptId.addNumberEqualsTo("OPT_ID", optId);
                SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
                sqlUpdateBuilder.setNumber("OPT_STATUS", Integer.valueOf(2)).setSqlWhere(sqlWhereBuilderOptId);
                SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
                hsmpResult.put("INCOM_AMOUNT", hsmpretPkg.get("INCOM_AMOUNT"));
                hsmpResult.put("CARD_STATUS", hsmpretPkg.get("CARD_STATUS"));
                hsmpResult.put("RECHARGE", hsmpretPkg.get("RECHARGE"));
                hsmpResult.put("IS_RECHANGE", hsmpretPkg.get("IS_RECHANGE"));
                hsmpResult.put("CARD_NAME", hsmpretPkg.get("CARD_NAME"));
                hsmpResult.put("BOTTLE_NUM", hsmpretPkg.get("BOTTLE_NUM"));
                hsmpResult.put("CREDIT", hsmpretPkg.get("CREDIT"));
                hsmpResult.put("TODAY_BOTTLE", hsmpretPkg.get("TODAY_BOTTLE"));
                hsmpResult.put("RESULT", NetworkUtils.NET_STATE_SUCCESS);
            }
        }
        return hsmpResult;
    }

    public HashMap initRecycle(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        ServiceGlobal.clearCurrentSession();
        return null;
    }

    public HashMap initVendingWay(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        ServiceGlobal.setCurrentSession(AllAdvertisement.VENDING_WAY, (String) hsmpParam.get(AllAdvertisement.VENDING_WAY));
        return null;
    }

    public HashMap initProductType(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        ServiceGlobal.setCurrentSession("PRODUCT_TYPE", (String) hsmpParam.get("PRODUCT_TYPE"));
        return null;
    }

    private HashMap queryVendingWay(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap<String, Object> hsmpState = new HashMap();
        hsmpState.put(AllAdvertisement.VENDING_WAY, ServiceGlobal.getCurrentSession(AllAdvertisement.VENDING_WAY));
        return hsmpState;
    }

    private HashMap queryCardTypeAndNumber(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap<String, Object> hsmpState = new HashMap();
        hsmpState.put("CARD_TYPE", ServiceGlobal.getCurrentSession("CARD_TYPE"));
        hsmpState.put("CARD_NO", ServiceGlobal.getCurrentSession("CARD_NO"));
        return hsmpState;
    }

    public HashMap initApp(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("OPT_STATUS", Integer.valueOf(0)).addNumberGreaterTo("PRODUCT_AMOUNT", "0");
        SqlUpdateBuilder sqlUpdateBuilderRvmOpt = new SqlUpdateBuilder("RVM_OPT");
        sqlUpdateBuilderRvmOpt.addNumber("OPT_STATUS", Integer.valueOf(1)).setNumber("PROFIT_AMOUNT", "0").setString(AllAdvertisement.VENDING_WAY, "DONATION").setString("CARD_TYPE", CardType.NOCARD).setString("CARD_NO", null).setSqlWhere(sqlWhereBuilder);
        listSqlBuilder.add(sqlUpdateBuilderRvmOpt);
        Date tPrevious7Date = new Date(System.currentTimeMillis() - (86400000 * Long.parseLong(SysConfig.get("RVM.OPT.KEEP.DAYS"))));
        sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("OPT_STATUS", Integer.valueOf(2)).addDateLessTo("OPT_TIME", tPrevious7Date);
        listSqlBuilder.add(new SqlDeleteBuilder("RVM_OPT").setSqlWhere(sqlWhereBuilder));
        listSqlBuilder.add(new SqlDeleteBuilder("rvm_opt_bottle").setSqlWhere("opt_id not in (select opt_id from rvm_opt)"));
        sqlWhereBuilder = new SqlWhereBuilder();
        Object[] objArr = new Object[]{Integer.valueOf(4), Integer.valueOf(3)};
        sqlWhereBuilder.addNumberIn("UPLOAD_FLAG", "1", "2").addNumberIn("ALARM_STATUS", objArr).addDateLessTo("ALARM_TIME", tPrevious7Date);
        listSqlBuilder.add(new SqlDeleteBuilder("RVM_ALARM_INST").setSqlWhere(sqlWhereBuilder));
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        return null;
    }

    public HashMap showOnDigitalScreen(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String text = null;
        if (hsmpParam != null) {
            try {
                text = (String) hsmpParam.get("TEXT");
            } catch (Exception e) {
            }
        }
        HashMap hsmpDSParam = new HashMap();
        hsmpDSParam.put("MSG", text);
        CommService.getCommService().execute("DIGITALSCREEN_SHOWTEXT_RESET", JSONUtils.toJSON(hsmpDSParam));
        return null;
    }

    public HashMap queryPaperDoorStatus(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String str = CommService.getCommService().execute("CHECK_PAPER_DOOR_STATE", null);
        HashMap hsmp = new HashMap();
        hsmp.put("PaperDoorStatus", str);
        return hsmp;
    }

    public HashMap forceStopPutPaper(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        CommService.getCommService().execute("RECYCLE_PAPER_STOP", null);
        return null;
    }

    public HashMap informEntityToThanks(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        CommService.getCommService().execute("INFORM_ENTITY_TO_THANKS", null);
        return null;
    }

    public HashMap initResource(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if (hsmpParam != null) {
            try {
                ServiceGlobal.setCurrentSession("RESOURCE", (HashMap) hsmpParam.get("RESOURCE"));
            } catch (Exception e) {
            }
        }
        return null;
    }
}
