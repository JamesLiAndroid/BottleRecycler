package com.incomrecycle.prms.rvm.service.commonservice.gui;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.commtable.CommTableRecord;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.sqlite.DBQuery;
import com.incomrecycle.common.sqlite.DBSequence;
import com.incomrecycle.common.sqlite.RowSet;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlBuilder;
import com.incomrecycle.common.sqlite.SqlInsertBuilder;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.CardType;
import com.incomrecycle.prms.rvm.common.SysDef.VendingWay;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import com.incomrecycle.prms.rvm.service.task.action.RCCInstanceTask;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class GUIVoucherCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if ("recycleEnd".equalsIgnoreCase(subSvnName)) {
            return recycleEnd(svcName, subSvnName, hsmpParam);
        }
        if ("queryVoucherList".equalsIgnoreCase(subSvnName)) {
            return queryVoucherList(svcName, subSvnName, hsmpParam);
        }
        if ("queryVoucherDetail".equalsIgnoreCase(subSvnName)) {
            return queryVoucherDetail(svcName, subSvnName, hsmpParam);
        }
        if ("printVoucher".equalsIgnoreCase(subSvnName)) {
            return printVoucher(svcName, subSvnName, hsmpParam);
        }
        if ("previewVoucher".equalsIgnoreCase(subSvnName)) {
            return previewVoucher(svcName, subSvnName, hsmpParam);
        }
        return null;
    }

    private HashMap recycleEnd(String svcName, String subSvnName, HashMap hsmpParam) {
        try {
            String OPT_ID = (String) ServiceGlobal.getCurrentSession("OPT_ID");
            if (OPT_ID == null) {
                return null;
            }
            String SELECT_FLAG = null;
            HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
            if (TRANSMIT_ADV == null || TRANSMIT_ADV.size() <= 0) {
                HashMap mapVendingWayFlag = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
                if (mapVendingWayFlag != null && mapVendingWayFlag.size() > 0) {
                    String VENDING_WAY_SET = (String) mapVendingWayFlag.get(AllAdvertisement.VENDING_WAY_SET);
                    if (VENDING_WAY_SET == null || !VENDING_WAY_SET.contains("COUPON")) {
                        String VENDING_WAY = (String) mapVendingWayFlag.get(AllAdvertisement.VENDING_WAY);
                        if (VENDING_WAY != null && "COUPON".equalsIgnoreCase(VENDING_WAY)) {
                            SELECT_FLAG = (String) mapVendingWayFlag.get(AllAdvertisement.SELECT_FLAG);
                        }
                    } else {
                        SELECT_FLAG = (String) mapVendingWayFlag.get(AllAdvertisement.SELECT_FLAG);
                    }
                }
            } else {
                HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
                if (HOMEPAGE_LEFT != null && HOMEPAGE_LEFT.size() > 0) {
                    SELECT_FLAG = (String) HOMEPAGE_LEFT.get(AllAdvertisement.SELECT_FLAG);
                }
            }
            SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
            SqlWhereBuilder sqlWhereBuilderRvmOpt = new SqlWhereBuilder();
            sqlWhereBuilderRvmOpt.addNumberEqualsTo("OPT_ID", OPT_ID).addNumberEqualsTo("OPT_STATUS", Integer.valueOf(0));
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
            sqlUpdateBuilder.setNumber("PROFIT_AMOUNT", "0").setString(AllAdvertisement.VENDING_WAY, "COUPON").setNumber("OPT_STATUS", Integer.valueOf(1)).setString("CARD_TYPE", CardType.NOCARD).setString("CARD_NO", null).setString(AllAdvertisement.SELECT_FLAG, SELECT_FLAG).setSqlWhere(sqlWhereBuilderRvmOpt);
            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
            HashMap<String, Object> hasmPram = new HashMap();
            hasmPram.put("CARD_TYPE", "COUPON");
            hasmPram.put("OPT_ID", OPT_ID);
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "cutPrice", hasmPram);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap queryVoucherList(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        Date tDate = new Date();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNot(new SqlWhereBuilder().addDatetimeGreaterTo("BEGIN_TIME", tDate)).addNot(new SqlWhereBuilder().addDatetimeLessTo("END_TIME", tDate)).add("(a.ACTIVITY_TYPE=2 or exists(select 1 from RVM_ACTIVITY_VOUCHER b where a.ACTIVITY_ID=b.ACTIVITY_ID and b.USED_TIME is null))");
        CommTable commTable = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase()).getCommTable("select a.*,(select count(*) from RVM_ACTIVITY_VOUCHER b where a.ACTIVITY_ID=b.ACTIVITY_ID and b.USED_TIME is null) as VOUCHER_COUNT from RVM_ACTIVITY a " + sqlWhereBuilder.toSqlWhere("where"));
        HashMap hsmpResult = new HashMap();
        List<HashMap<String, String>> listVoucher = new ArrayList();
        for (int i = 0; i < commTable.getRecordCount(); i++) {
            CommTableRecord ctr = commTable.getRecord(i);
            HashMap<String, String> hsmpVoucher = new HashMap();
            hsmpVoucher.put("ACTIVITY_ID", ctr.get("ACTIVITY_ID"));
            hsmpVoucher.put("ACTIVITY_TYPE", ctr.get("ACTIVITY_TYPE"));
            hsmpVoucher.put("PRINT_RULE", ctr.get("PRINT_RULE"));
            hsmpVoucher.put("PRINT_INFO", ctr.get("PRINT_INFO"));
            hsmpVoucher.put("PIC_INFO", ctr.get("PIC_INFO"));
            hsmpVoucher.put("PIC_PATH", ctr.get("PIC_PATH"));
            hsmpVoucher.put("BEGIN_TIME", ctr.get("BEGIN_TIME"));
            hsmpVoucher.put("END_TIME", ctr.get("END_TIME"));
            hsmpVoucher.put("VOUCHER_COUNT", ctr.get("VOUCHER_COUNT"));
            hsmpVoucher.put("ACTIVITY_NAME", ctr.get("ACTIVITY_NAME"));
            listVoucher.add(hsmpVoucher);
        }
        if (listVoucher.size() > 0) {
            hsmpResult.put("VOUCHER_LIST", listVoucher);
        }
        return hsmpResult;
    }

    public HashMap queryVoucherDetail(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String ACTIVITY_ID = (String) hsmpParam.get("ACTIVITY_ID");
        Date tDate = new Date();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("ACTIVITY_ID", ACTIVITY_ID);
        CommTable commTable = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase()).getCommTable("select a.*,(select count(*) from RVM_ACTIVITY_VOUCHER b where a.ACTIVITY_ID=b.ACTIVITY_ID and b.USED_TIME is null) as VOUCHER_COUNT from RVM_ACTIVITY a " + sqlWhereBuilder.toSqlWhere("where"));
        HashMap hsmpResult = new HashMap();
        if (commTable.getRecordCount() > 0) {
            CommTableRecord ctr = commTable.getRecord(0);
            HashMap<String, String> hsmpVoucher = new HashMap();
            hsmpVoucher.put("ACTIVITY_ID", ctr.get("ACTIVITY_ID"));
            hsmpVoucher.put("ACTIVITY_TYPE", ctr.get("ACTIVITY_TYPE"));
            hsmpVoucher.put("PRINT_RULE", ctr.get("PRINT_RULE"));
            hsmpVoucher.put("PRINT_INFO", ctr.get("PRINT_INFO"));
            hsmpVoucher.put("PIC_INFO", ctr.get("PIC_INFO"));
            hsmpVoucher.put("BEGIN_TIME", ctr.get("BEGIN_TIME"));
            hsmpVoucher.put("END_TIME", ctr.get("END_TIME"));
            hsmpVoucher.put("VOUCHER_COUNT", ctr.get("VOUCHER_COUNT"));
            hsmpResult.put("VOUCHER_INFO", hsmpVoucher);
        }
        return hsmpResult;
    }

    public HashMap previewVoucher(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpPreview = new HashMap();
        String PRINT_INFO = (String) hsmpParam.get("PRINT_INFO");
        HashMap<String, String> hsmpReplace = new HashMap();
        if (PRINT_INFO.indexOf("[FORMAT]") == -1) {
            PRINT_INFO = StringUtils.replace("[FORMAT][RESET][SETTINGS]CHARSET=$CHARSET$;[TEXT]", "$CHARSET$", null) + PRINT_INFO + "[RESET][TEXT]\\n\\n\\n\\n[CUT]HALF[TEXT]\\n";
        }
        hsmpReplace.put("$BOTTLE_COUNT$", "*");
        hsmpReplace.put("$TOTAL_AMOUNT$", "*");
        hsmpReplace.put("$TIME$", DateUtils.formatDatetime(new Date(), "yyyy-MM-dd HH:mm:ss"));
        hsmpReplace.put("$VOUCHER_CODE$", "*");
        hsmpReplace.put("$TERM_CODE$", SysConfig.get("RVM.CODE"));
        hsmpReplace.put("$CHARSET$", null);
        hsmpReplace.put("$n$", "\\n");
        HashMap hsmpCmdParam = new HashMap();
        if (PRINT_INFO.indexOf("$LOGO$") != -1) {
            hsmpReplace.put("$LOGO$", StringUtils.replace(StringUtils.replace(StringUtils.replace(SysConfig.get("COUPON.LOGO.FORMAT"), "$LOGO.FILE$", SysConfig.get("LOGO.FILE")), "$LOGO.FILE.RESOURCE$", SysConfig.get("LOGO.FILE.RESOURCE")), "$CHARSET$", null));
        }
        if (PRINT_INFO.indexOf("$DETAILS$") != -1) {
            hsmpReplace.put("$DETAILS$", StringUtils.replace(StringUtils.replace(StringUtils.replace(SysConfig.get("COUPON.DETAILS.FORMAT"), "$IMAGEPRINT.NUMBER$", "*"), "$IMAGEPRINT.MONEY$", "*"), "$MONEY.CHAR.HEX$", "*"));
        }
        hsmpCmdParam.put("MODEL", PRINT_INFO);
        hsmpCmdParam.put("PARAM", hsmpReplace);
        hsmpPreview.put("PRINT_INFO", CommService.getCommService().execute("PRINTER_PREVIEW", JSONUtils.toJSON(hsmpCmdParam)));
        return hsmpPreview;
    }

    public HashMap printVoucher(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String ACTIVITY_ID = (String) hsmpParam.get("ACTIVITY_ID");
        String PRINT_RULE = (String) hsmpParam.get("PRINT_RULE");
        String LOCALE = (String) hsmpParam.get("LOCALE");
        String OPT_ID = (String) ServiceGlobal.getCurrentSession("OPT_ID");
        if (StringUtils.isBlank(OPT_ID)) {
            return null;
        }
        int i;
        String bottleAmount;
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderRvmOptBottle = new SqlWhereBuilder().addNumberEqualsTo("OPT_ID", OPT_ID);
        if (!"TRUE".equalsIgnoreCase(SysConfig.get("COUPON.ZERO.PRINT.ENABLE"))) {
            sqlWhereBuilderRvmOptBottle.addNumberGreaterTo("BOTTLE_AMOUNT", Integer.valueOf(0));
        }
        CommTable commTableRvmOptBottleCount = dbQuery.getCommTable("select * from RVM_OPT_BOTTLE" + sqlWhereBuilderRvmOptBottle.toSqlWhere("where"));
        int voucherCount = 0;
        double totalAmount = 0.0d;
        List<String> listSingleBottleAmount = new ArrayList();
        if (commTableRvmOptBottleCount.getRecordCount() > 0) {
            for (i = 0; i < commTableRvmOptBottleCount.getRecordCount(); i++) {
                CommTableRecord ctr = commTableRvmOptBottleCount.getRecord(i);
                int bottleCount = Integer.parseInt(ctr.get("BOTTLE_COUNT"));
                voucherCount += bottleCount;
                bottleAmount = ctr.get("BOTTLE_AMOUNT");
                totalAmount += Double.parseDouble(bottleAmount) * ((double) bottleCount);
                for (int m = 0; m < bottleCount; m++) {
                    listSingleBottleAmount.add(bottleAmount);
                }
            }
        }
        if (voucherCount == 0) {
            return null;
        }
        Properties propCharSet = PropUtils.loadResource("charset");
        String charSet = propCharSet.getProperty(LOCALE);
        if (StringUtils.isBlank(charSet)) {
            charSet = propCharSet.getProperty("default");
        }
        Date tDate = new Date();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberEqualsTo("ACTIVITY_ID", ACTIVITY_ID);
        List<HashMap<String, String>> listInstanceTask = new ArrayList();
        CommTable commTable = dbQuery.getCommTable("select a.*,(select count(*) from RVM_ACTIVITY_VOUCHER b where a.ACTIVITY_ID=b.ACTIVITY_ID and b.USED_TIME is null) as VOUCHER_COUNT from RVM_ACTIVITY a " + sqlWhereBuilder.toSqlWhere("where"));
        HashMap hsmpResult = new HashMap();
        if (commTable.getRecordCount() <= 0) {
            return hsmpResult;
        }
        CommTableRecord ctr = commTable.getRecord(0);
        HashMap<String, String> hsmpVoucher = new HashMap();
        hsmpVoucher.put("ACTIVITY_ID", ctr.get("ACTIVITY_ID"));
        hsmpVoucher.put("ACTIVITY_TYPE", ctr.get("ACTIVITY_TYPE"));
        hsmpVoucher.put("PRINT_RULE", ctr.get("PRINT_RULE"));
        hsmpVoucher.put("PRINT_INFO", ctr.get("PRINT_INFO"));
        hsmpVoucher.put("PIC_INFO", ctr.get("PIC_INFO"));
        hsmpVoucher.put("BEGIN_TIME", ctr.get("BEGIN_TIME"));
        hsmpVoucher.put("END_TIME", ctr.get("END_TIME"));
        hsmpVoucher.put("VOUCHER_COUNT", ctr.get("VOUCHER_COUNT"));
        hsmpVoucher.put("ACTIVITY_NAME", ctr.get("ACTIVITY_NAME"));
        List<String> listVoucherCode = new ArrayList();
        if ("2".equals(hsmpVoucher.get("ACTIVITY_TYPE"))) {
            DBSequence dbSequence = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS"));
            if ("3".equals(PRINT_RULE)) {
                listVoucherCode.add(dbSequence.getDateSeq("VOUCHER_SEQ", 13));
            } else {
                for (i = 0; i < voucherCount; i++) {
                    listVoucherCode.add(dbSequence.getDateSeq("VOUCHER_SEQ", 13));
                }
            }
        } else {
            sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addNumberEqualsTo("ACTIVITY_ID", ACTIVITY_ID).addIsNull("USED_TIME");
            commTable = dbQuery.getCommTable(DBQuery.getQuerySQL("select * from RVM_ACTIVITY_VOUCHER " + sqlWhereBuilder.toSqlWhere("where"), RowSet.newRowSet(0, (long) voucherCount)));
            for (i = 0; i < commTable.getRecordCount(); i++) {
                listVoucherCode.add(commTable.getRecord(i).get("VOUCHER_CODE"));
            }
        }
        String MONEY_CHAR = "";
        String currencySymbol = SysConfig.get("COUPON.CURRENCY.SYMBOL");
        if (!StringUtils.isBlank(currencySymbol)) {
            MONEY_CHAR = Currency.getInstance(currencySymbol).getSymbol();
        }
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String PRINT_INFO;
        HashMap<String, String> hsmpReplace;
        HashMap hsmpCmdParam;
        if ("1".equals(PRINT_RULE)) {
            for (i = 0; i < listVoucherCode.size(); i++) {
                bottleAmount = decimalFormat.format(Double.parseDouble((String) listSingleBottleAmount.get(i)) / 100.0d);
                PRINT_INFO = (String) hsmpVoucher.get("PRINT_INFO");
                hsmpReplace = new HashMap();
                if (PRINT_INFO.indexOf("[FORMAT]") == -1) {
                    PRINT_INFO = StringUtils.replace("[FORMAT][RESET][SETTINGS]CHARSET=$CHARSET$;[TEXT]", "$CHARSET$", charSet) + PRINT_INFO + "[RESET][TEXT]\\n\\n\\n\\n[CUT]HALF[TEXT]\\n";
                }
                hsmpReplace.put("$BOTTLE_COUNT$", "1");
                hsmpReplace.put("$TOTAL_AMOUNT$", bottleAmount);
                hsmpReplace.put("$TIME$", DateUtils.formatDatetime(tDate, "yyyy-MM-dd HH:mm:ss"));
                hsmpReplace.put("$VOUCHER_CODE$", listVoucherCode.get(i));
                hsmpReplace.put("$TERM_CODE$", SysConfig.get("RVM.CODE"));
                hsmpReplace.put("$CHARSET$", charSet);
                hsmpReplace.put("$n$", "\\n");
                hsmpCmdParam = new HashMap();
                if (PRINT_INFO.indexOf("$LOGO$") != -1) {
                    hsmpReplace.put("$LOGO$", StringUtils.replace(StringUtils.replace(StringUtils.replace(SysConfig.get("COUPON.LOGO.FORMAT"), "$LOGO.FILE$", SysConfig.get("LOGO.FILE")), "$LOGO.FILE.RESOURCE$", SysConfig.get("LOGO.FILE.RESOURCE")), "$CHARSET$", charSet));
                }
                if (PRINT_INFO.indexOf("$DETAILS$") != -1) {
                    PRINT_INFO = StringUtils.replace(PRINT_INFO, "$DETAILS$", StringUtils.replace(SysConfig.get("COUPON.DETAILS.FORMAT"), "$MONEY.CHAR.HEX$", MONEY_CHAR));
                    hsmpReplace.put("$DETAILS$", bottleAmount);
                }
                hsmpCmdParam.put("MODEL", PRINT_INFO);
                hsmpCmdParam.put("PARAM", hsmpReplace);
                CommService.getCommService().execute("PRINTER_PRINT", JSONUtils.toJSON(hsmpCmdParam));
            }
        } else if ("3".equals(PRINT_RULE)) {
            if (hsmpVoucher.get("PRINT_INFO") != null) {
                for (i = 0; i < listVoucherCode.size(); i++) {
                    PRINT_INFO = (String) hsmpVoucher.get("PRINT_INFO");
                    hsmpReplace = new HashMap();
                    if (PRINT_INFO.indexOf("[FORMAT]") == -1) {
                        PRINT_INFO = StringUtils.replace("[FORMAT][RESET][SETTINGS]CHARSET=$CHARSET$;[TEXT]", "$CHARSET$", charSet) + PRINT_INFO + "[RESET][TEXT]\\n\\n\\n\\n[CUT]HALF[TEXT]\\n";
                    }
                    hsmpReplace.put("$BOTTLE_COUNT$", "" + voucherCount);
                    hsmpReplace.put("$TOTAL_AMOUNT$", decimalFormat.format(totalAmount / 100.0d));
                    hsmpReplace.put("$TIME$", DateUtils.formatDatetime(tDate, "yyyy-MM-dd HH:mm:ss"));
                    hsmpReplace.put("$VOUCHER_CODE$", listVoucherCode.get(i));
                    hsmpReplace.put("$TERM_CODE$", SysConfig.get("RVM.CODE"));
                    hsmpReplace.put("$CHARSET$", charSet);
                    hsmpReplace.put("$n$", "\\n");
                    hsmpCmdParam = new HashMap();
                    if (PRINT_INFO.indexOf("$LOGO$") != -1) {
                        hsmpReplace.put("$LOGO$", StringUtils.replace(StringUtils.replace(StringUtils.replace(SysConfig.get("COUPON.LOGO.FORMAT"), "$LOGO.FILE$", SysConfig.get("LOGO.FILE")), "$LOGO.FILE.RESOURCE$", SysConfig.get("LOGO.FILE.RESOURCE")), "$CHARSET$", charSet));
                    }
                    if (PRINT_INFO.indexOf("$DETAILS$") != -1) {
                        PRINT_INFO = StringUtils.replace(PRINT_INFO, "$DETAILS$", StringUtils.replace(SysConfig.get("COUPON.DETAILS.FORMAT"), "$MONEY.CHAR.HEX$", MONEY_CHAR));
                        hsmpReplace.put("$DETAILS$", decimalFormat.format(totalAmount / 100.0d));
                    }
                    hsmpCmdParam.put("MODEL", PRINT_INFO);
                    hsmpCmdParam.put("PARAM", hsmpReplace);
                    CommService.getCommService().execute("PRINTER_PRINT", JSONUtils.toJSON(hsmpCmdParam));
                }
            }
        } else if ("2".equals(PRINT_RULE)) {
            PRINT_INFO = (String) hsmpVoucher.get("PRINT_INFO");
            hsmpReplace = new HashMap();
            if (PRINT_INFO.indexOf("[FORMAT]") == -1) {
                PRINT_INFO = StringUtils.replace("[FORMAT][RESET][SETTINGS]CHARSET=$CHARSET$;[TEXT]", "$CHARSET$", charSet) + PRINT_INFO + "[RESET][TEXT]\\n\\n\\n\\n[CUT]HALF[TEXT]\\n";
            }
            hsmpReplace.put("$BOTTLE_COUNT$", "" + voucherCount);
            hsmpReplace.put("$TOTAL_AMOUNT$", decimalFormat.format(totalAmount / 100.0d));
            hsmpReplace.put("$TIME$", DateUtils.formatDatetime(tDate, "yyyy-MM-dd HH:mm:ss"));
            hsmpReplace.put("$VOUCHER_CODE$", listVoucherCode.get(0));
            hsmpReplace.put("$TERM_CODE$", SysConfig.get("RVM.CODE"));
            hsmpReplace.put("$CHARSET$", charSet);
            hsmpReplace.put("$n$", "\\n");
            hsmpCmdParam = new HashMap();
            if (PRINT_INFO.indexOf("$LOGO$") != -1) {
                hsmpReplace.put("$LOGO$", StringUtils.replace(StringUtils.replace(StringUtils.replace(SysConfig.get("COUPON.LOGO.FORMAT"), "$LOGO.FILE$", SysConfig.get("LOGO.FILE")), "$LOGO.FILE.RESOURCE$", SysConfig.get("LOGO.FILE.RESOURCE")), "$CHARSET$", charSet));
            }
            if (PRINT_INFO.indexOf("$DETAILS$") != -1) {
                PRINT_INFO = StringUtils.replace(PRINT_INFO, "$DETAILS$", StringUtils.replace(SysConfig.get("COUPON.DETAILS.FORMAT"), "$MONEY.CHAR.HEX$", MONEY_CHAR));
                hsmpReplace.put("$DETAILS$", decimalFormat.format(totalAmount / 100.0d));
            }
            hsmpCmdParam.put("MODEL", PRINT_INFO);
            hsmpCmdParam.put("PARAM", hsmpReplace);
            CommService.getCommService().execute("PRINTER_PRINT", JSONUtils.toJSON(hsmpCmdParam));
        }
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        String UPLOAD_FLAG = "1";
        String[] UPLOAD_ACTIVITY_TYPE_SET = SysConfig.get("COUPON.UPLOAD.ENABLE.ACTIVITY_TYPE.SET").split(";");
        for (String equalsIgnoreCase : UPLOAD_ACTIVITY_TYPE_SET) {
            if (equalsIgnoreCase.equalsIgnoreCase((String) hsmpVoucher.get("ACTIVITY_TYPE"))) {
                UPLOAD_FLAG = "0";
                break;
            }
        }
        if ("1".equals(hsmpVoucher.get("ACTIVITY_TYPE"))) {
            SqlWhereBuilder sqlWhereBuilderRvmActivityVoucher = new SqlWhereBuilder();
            sqlWhereBuilderRvmActivityVoucher.addNumberEqualsTo("ACTIVITY_ID", ACTIVITY_ID).addStringIn("VOUCHER_CODE", (List) listVoucherCode);
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_ACTIVITY_VOUCHER");
            sqlUpdateBuilder.setTime("USED_TIME", tDate).setSqlWhere(sqlWhereBuilderRvmActivityVoucher);
            listSqlBuilder.add(sqlUpdateBuilder);
        }
        SqlWhereBuilder sqlWhereBuilderRvmOpt;
        if ("3".equals(PRINT_RULE)) {
            sqlWhereBuilderRvmOpt = new SqlWhereBuilder();
            sqlWhereBuilderRvmOpt.addNumberEqualsTo("OPT_ID", OPT_ID);
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
            sqlUpdateBuilder.setString(AllAdvertisement.VENDING_WAY, VendingWay.TICKET).setNumber("OPT_STATUS", Integer.valueOf(1)).setNumber("PROFIT_AMOUNT", "0").setString("CARD_NO", listVoucherCode.get(0)).setSqlWhere(sqlWhereBuilderRvmOpt);
            listSqlBuilder.add(sqlUpdateBuilder);
        } else {
            sqlWhereBuilderRvmOpt = new SqlWhereBuilder();
            sqlWhereBuilderRvmOpt.addNumberEqualsTo("OPT_ID", OPT_ID);
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
            sqlUpdateBuilder.setString(AllAdvertisement.VENDING_WAY, "COUPON").setNumber("OPT_STATUS", Integer.valueOf(1)).setNumber("PROFIT_AMOUNT", "0").setString("CARD_NO", hsmpVoucher.get("ACTIVITY_ID")).setSqlWhere(sqlWhereBuilderRvmOpt);
            listSqlBuilder.add(sqlUpdateBuilder);
        }
        SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_OPT_VOUCHER");
        for (i = 0; i < listVoucherCode.size(); i++) {
            sqlInsertBuilder.newInsertRecord().setNumber("OPT_ID", OPT_ID).setString("VOUCHER_CODE", listVoucherCode.get(i)).setNumber("UPLOAD_FLAG", UPLOAD_FLAG).setNumber("ACTIVITY_ID", Integer.valueOf(Integer.parseInt((String) hsmpVoucher.get("ACTIVITY_ID")))).setNumber("ACTIVITY_TYPE", Integer.valueOf(Integer.parseInt((String) hsmpVoucher.get("ACTIVITY_TYPE"))));
        }
        listSqlBuilder.add(sqlInsertBuilder);
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        HashMap<String, String> hsmpInstanceTask = new HashMap();
        hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_OPT");
        hsmpInstanceTask.put("OPT_ID", OPT_ID);
        listInstanceTask.add(hsmpInstanceTask);
        for (i = 0; i < listInstanceTask.size(); i++) {
            RCCInstanceTask.addTask((HashMap) listInstanceTask.get(i));
        }
        return hsmpResult;
    }
}
