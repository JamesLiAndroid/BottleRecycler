package com.incomrecycle.prms.rvm.service.commonservice.gui;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlBuilder;
import com.incomrecycle.common.sqlite.SqlDeleteBuilder;
import com.incomrecycle.common.sqlite.SqlInsertBuilder;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.CardType;
import com.incomrecycle.prms.rvm.common.SysDef.MsgType;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import com.incomrecycle.prms.rvm.service.task.action.RCCInstanceTask;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class GUIOneCardCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if ("recycleEnd".equalsIgnoreCase(subSvnName)) {
            return recycleEnd(svcName, subSvnName, hsmpParam);
        }
        if ("initOneCard".equalsIgnoreCase(subSvnName)) {
            return initCard(svcName, subSvnName, hsmpParam);
        }
        if ("readOneCard".equalsIgnoreCase(subSvnName)) {
            return readCard(svcName, subSvnName, hsmpParam);
        }
        if ("verifyOneCard".equalsIgnoreCase(subSvnName)) {
            return verifyOneCard(svcName, subSvnName, hsmpParam);
        }
        if ("rechargeOneCard".equalsIgnoreCase(subSvnName)) {
            return rechargeOneCard(svcName, subSvnName, hsmpParam);
        }
        if ("transactionRecord".equalsIgnoreCase(subSvnName)) {
            return transactionRecord(svcName, subSvnName, hsmpParam);
        }
        if ("consumeRecord".equalsIgnoreCase(subSvnName)) {
            return consumeRecord(svcName, subSvnName, hsmpParam);
        }
        if ("writeTrans".equalsIgnoreCase(subSvnName)) {
            return writeTrans(svcName, subSvnName, hsmpParam);
        }
        if ("queryOneCardBalance".equalsIgnoreCase(subSvnName)) {
            return queryOneCardBalance(svcName, subSvnName, hsmpParam);
        }
        if ("bindOneCard".equalsIgnoreCase(subSvnName)) {
            return bindOneCard(svcName, subSvnName, hsmpParam);
        }
        if ("verifyBindPhone".equalsIgnoreCase(subSvnName)) {
            return verifyBindPhone(svcName, subSvnName, hsmpParam);
        }
        return null;
    }

    private HashMap writeTrans(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hasp = new HashMap();
        hasp.put("ORDER_ID", hsmpParam.get("ORDER_ID"));
        hasp.put("MERCHANT_ID", hsmpParam.get("MERCHANT_ID"));
        hasp.put("AMOUNT", hsmpParam.get("AMOUNT"));
        hasp.put("RELEASE_FLAG", SysConfig.get("TRANSPORTCARD.READER.RELEASE"));
        hasp.put("ONECARD_NUM", hsmpParam.get("ONECARD_NUM"));
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        List<HashMap<String, String>> listInstanceTask = new ArrayList();
        HashMap hsmpCardInfo = JSONUtils.toHashMap(CommService.getCommService().execute("WRITE_TRANS", JSONUtils.toJSON(hasp)));
        HashMap retPkg = null;
        if (hsmpCardInfo != null) {
            List<SqlBuilder> listSqlBuilder;
            SqlWhereBuilder sqlWhereBuilder;
            SqlDeleteBuilder sqlDeleteBuilder;
            SqlInsertBuilder sqlInsertBuilder;
            HashMap<String, String> hsmpInstanceTask;
            retPkg = new HashMap();
            if ("success".equalsIgnoreCase((String) hsmpCardInfo.get("RET_CODE"))) {
                listSqlBuilder = new ArrayList();
                sqlWhereBuilder = new SqlWhereBuilder();
                sqlWhereBuilder.addStringEqualsTo("CARD_NO", (String) hsmpParam.get("ONECARD_NUM")).addStringEqualsTo("CARD_TYPE", "TRANSPORTCARD");
                sqlDeleteBuilder = new SqlDeleteBuilder("RVM_RECHARGE");
                sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
                listSqlBuilder.add(sqlDeleteBuilder);
                sqlInsertBuilder = new SqlInsertBuilder("RVM_RECHARGE");
                sqlInsertBuilder.newInsertRecord().setString("CARD_NO", (String) hsmpParam.get("ONECARD_NUM")).setString("CARD_TYPE", "2").setString("RECHARGE_STATE", "1").setDateTime("RECHARGE_TIME", new Date()).setString("ORDER_ID", hsmpParam.get("ORDER_ID"));
                listSqlBuilder.add(sqlInsertBuilder);
                SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                hsmpInstanceTask = new HashMap();
                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_RECHARGE_STS");
                RCCInstanceTask.addTask(hsmpInstanceTask);
                retPkg.put("RET_CODE", "success");
                retPkg.put("PREV_BALANCE", hsmpCardInfo.get("PREV_BALANCE"));
                retPkg.put("FINAL_BALANCE", hsmpCardInfo.get("FINAL_BALANCE"));
            }
            if ("fail".equalsIgnoreCase((String) hsmpCardInfo.get("RET_CODE"))) {
                retPkg.put("RET_CODE", "fail");
            }
            if ("not_match".equalsIgnoreCase((String) hsmpCardInfo.get("RET_CODE"))) {
                retPkg.put("RET_CODE", "not_match");
            }
            if ("writeException".equalsIgnoreCase((String) hsmpCardInfo.get("RET_CODE"))) {
                listSqlBuilder = new ArrayList();
                sqlWhereBuilder = new SqlWhereBuilder();
                sqlWhereBuilder.addStringEqualsTo("CARD_NO", (String) hsmpParam.get("ONECARD_NUM")).addStringEqualsTo("CARD_TYPE", "TRANSPORTCARD");
                sqlDeleteBuilder = new SqlDeleteBuilder("RVM_RECHARGE");
                sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
                listSqlBuilder.add(sqlDeleteBuilder);
                sqlInsertBuilder = new SqlInsertBuilder("RVM_RECHARGE");
                sqlInsertBuilder.newInsertRecord().setString("CARD_NO", (String) hsmpParam.get("ONECARD_NUM")).setString("CARD_TYPE", "2").setString("RECHARGE_STATE", "-1").setDateTime("RECHARGE_TIME", new Date()).setString("ORDER_ID", hsmpParam.get("ORDER_ID"));
                listSqlBuilder.add(sqlInsertBuilder);
                SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                hsmpInstanceTask = new HashMap();
                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_RECHARGE_STS");
                RCCInstanceTask.addTask(hsmpInstanceTask);
                retPkg.put("RET_CODE", "fail");
            }
        }
        return retPkg;
    }

    private HashMap recycleEnd(String svcName, String subSvnName, HashMap hsmpParam) {
        try {
            String CARD_NO = (String) ServiceGlobal.getCurrentSession("CARD_NO");
            String CARD_TYPE = "TRANSPORTCARD";
            String OPT_ID = (String) ServiceGlobal.getCurrentSession("OPT_ID");
            if (OPT_ID == null) {
                return null;
            }
            String SELECT_FLAG = null;
            HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
            if (TRANSMIT_ADV != null) {
                SELECT_FLAG = (String) ((HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV")).get(AllAdvertisement.SELECT_FLAG);
            } else {
                HashMap mapVendingWayFlag = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
                if (mapVendingWayFlag != null && mapVendingWayFlag.size() > 0) {
                    String VENDING_WAY_SET = (String) mapVendingWayFlag.get(AllAdvertisement.VENDING_WAY_SET);
                    if (VENDING_WAY_SET == null || !VENDING_WAY_SET.contains("TRANSPORTCARD")) {
                        String VENDING_WAY = (String) mapVendingWayFlag.get(AllAdvertisement.VENDING_WAY);
                        if (VENDING_WAY != null && "TRANSPORTCARD".equalsIgnoreCase(VENDING_WAY)) {
                            SELECT_FLAG = (String) mapVendingWayFlag.get(AllAdvertisement.SELECT_FLAG);
                        }
                    } else {
                        SELECT_FLAG = (String) mapVendingWayFlag.get(AllAdvertisement.SELECT_FLAG);
                    }
                }
            }
            SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
            SqlWhereBuilder sqlWhereBuilderRvmOpt = new SqlWhereBuilder();
            sqlWhereBuilderRvmOpt.addNumberEqualsTo("OPT_ID", OPT_ID).addNumberEqualsTo("OPT_STATUS", Integer.valueOf(0));
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
            sqlUpdateBuilder.setString(AllAdvertisement.VENDING_WAY, "TRANSPORTCARD").setNumber("OPT_STATUS", Integer.valueOf(1)).setString("CARD_TYPE", CARD_TYPE).setString("CARD_NO", CARD_NO).setString(AllAdvertisement.SELECT_FLAG, SELECT_FLAG).setSqlWhere(sqlWhereBuilderRvmOpt);
            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
            HashMap<String, Object> hasmPram = new HashMap();
            hasmPram.put("CARD_NO", CARD_NO);
            hasmPram.put("CARD_TYPE", CARD_TYPE);
            hasmPram.put("OPT_ID", OPT_ID);
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "cutPrice", hasmPram);
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "limitBottleCount", hasmPram);
            HashMap hsmpUploadParam = new HashMap();
            hsmpUploadParam.put("OPT_ID", OPT_ID);
            HashMap hsmpUploadResult = new GUIRecycleCommonService().execute("GUIRecycleCommonService", "uploadOptDetail", hsmpUploadParam);
            if (hsmpUploadResult != null) {
                if ("repeat".equalsIgnoreCase((String) hsmpUploadResult.get("RESULT"))) {
                    HashMap hsmpStatusParam = new HashMap();
                    hsmpStatusParam.put("CARD_NO", CARD_NO);
                    hsmpStatusParam.put("CARD_TYPE", CARD_TYPE);
                    return new GUIRecycleCommonService().execute("GUIRecycleCommonService", "queryCardStatus", hsmpStatusParam);
                } else if ("success".equalsIgnoreCase((String) hsmpUploadResult.get("RESULT"))) {
                    HashMap hsmpResult = new HashMap();
                    hsmpResult.put("INCOM_AMOUNT", hsmpUploadResult.get("INCOM_AMOUNT"));
                    hsmpResult.put("CARD_STATUS", hsmpUploadResult.get("CARD_STATUS"));
                    hsmpResult.put("RECHARGE", hsmpUploadResult.get("RECHARGE"));
                    hsmpResult.put("IS_RECHANGE", hsmpUploadResult.get("IS_RECHANGE"));
                    hsmpResult.put("BOTTLE_NUM", hsmpUploadResult.get("BOTTLE_NUM"));
                    hsmpResult.put("CREDIT", hsmpUploadResult.get("CREDIT"));
                    hsmpResult.put("TODAY_BOTTLE", hsmpUploadResult.get("TODAY_BOTTLE"));
                    return hsmpResult;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public HashMap readCard(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpCardInfo = JSONUtils.toHashMap(CommService.getCommService().execute("READ_ONECARD", null));
        HashMap retPkg = null;
        if (hsmpCardInfo != null) {
            retPkg = new HashMap();
            retPkg.put("RET_CODE", hsmpCardInfo.get("RET_CODE"));
            retPkg.put("ONECARD_NUM", hsmpCardInfo.get("ONECARD_NUM"));
            if ("success".equalsIgnoreCase((String) hsmpCardInfo.get("RET_CODE"))) {
                ServiceGlobal.setCurrentSession("ONECARD_NUM", hsmpCardInfo.get("ONECARD_NUM"));
                ServiceGlobal.setCurrentSession("CARD_NO", hsmpCardInfo.get("ONECARD_NUM"));
                ServiceGlobal.setCurrentSession("CARD_TYPE", "TRANSPORTCARD");
            }
        }
        return retPkg;
    }

    private HashMap verifyOneCard(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String ONECARD_NUM = (String) hsmpParam.get("ONECARD_NUM");
        if (ONECARD_NUM != null) {
            try {
                HashMap hsmpStatusParam = new HashMap();
                hsmpStatusParam.put("CARD_NO", ONECARD_NUM);
                hsmpStatusParam.put("CARD_TYPE", "TRANSPORTCARD");
                return new GUIRecycleCommonService().execute("GUIRecycleCommonService", "queryCardStatus", hsmpStatusParam);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private HashMap rechargeOneCard(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String ONECARD_NUM = (String) ServiceGlobal.getCurrentSession("ONECARD_NUM");
        String MES_TYPE = MsgType.RVM_CARD_RECHANGE;
        Long Time = Long.valueOf(new Date().getTime());
        String QuTime = Time.toString();
        HashMap hsmpPkg = new HashMap();
        hsmpPkg.put("MES_TYPE", MES_TYPE);
        hsmpPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
        hsmpPkg.put("QU_TIME", QuTime);
        hsmpPkg.put("CARD_TYPE", CardType.getMsgCardType("TRANSPORTCARD"));
        hsmpPkg.put("CARD_NO", ONECARD_NUM);
        hsmpPkg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + Time);
        try {
            HashMap hsmp = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPkg)));
            if (hsmp == null) {
                return null;
            }
            String termNo = (String) hsmp.get("TERM_NO");
            String areaId = (String) hsmp.get("LOCAL_AREA_ID");
            if (MsgType.RCC_CARD_RECHANGE.equalsIgnoreCase((String) hsmp.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                HashMap hsmpRet = new HashMap();
                int order_sts = Integer.parseInt((String) hsmp.get("ORDER_STS"));
                String recharge_request = (String) hsmp.get("RECHARGE_REQUEST");
                if (1 == order_sts || order_sts == 0) {
                    hsmpRet.put("RET_CODE", "CHARGABLE");
                    return hsmpRet;
                } else if (2 == order_sts) {
                    String[] requestStr = recharge_request.trim().split("&");
                    String mchntid = null;
                    String txnAmt = null;
                    String orderId = null;
                    for (int i = 0; i < requestStr.length; i++) {
                        int div = requestStr[i].indexOf("=");
                        if (div != -1) {
                            String name = requestStr[i].substring(0, div);
                            String value = requestStr[i].substring(div + 1);
                            if ("mchntid".equals(name)) {
                                mchntid = value;
                            }
                            if ("txnAmt".equals(name)) {
                                txnAmt = value;
                            }
                            if ("sysDatetime".equals(name)) {
                                String sysDatetime = value;
                            }
                            if ("orderId".equals(name)) {
                                orderId = value;
                            }
                        }
                    }
                    txnAmt = new DecimalFormat("0.00").format(Double.parseDouble(txnAmt) / 100.0d);
                    hsmpRet.put("ORDER_ID", orderId);
                    hsmpRet.put("MERCHANT_ID", mchntid);
                    hsmpRet.put("AMOUNT", txnAmt);
                    hsmpRet.put("ONECARD_NUM", (String) hsmp.get("CARD_NO"));
                    hsmpRet.put("RET_CODE", "WRITABLE");
                    return hsmpRet;
                } else if (-1 == order_sts) {
                    hsmpRet.put("RET_CODE", "BLACKLIST");
                    return hsmpRet;
                } else if (-2 == order_sts) {
                    hsmpRet.put("RET_CODE", "UNBUNDLE");
                    return hsmpRet;
                } else if (-3 == order_sts) {
                    hsmpRet.put("RET_CODE", "LACKMONEY");
                    return hsmpRet;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private HashMap initCard(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        CommService.getCommService().execute("INIT_ONECARD", null);
        return null;
    }

    private HashMap transactionRecord(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        return JSONUtils.toHashMap(CommService.getCommService().execute("TRANSACTION_ONECARD", null));
    }

    private HashMap consumeRecord(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        return JSONUtils.toHashMap(CommService.getCommService().execute("CONSUME_ONECARD", null));
    }

    private HashMap queryOneCardBalance(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        return JSONUtils.toHashMap(CommService.getCommService().execute("QUERY_BALANCE", null));
    }

    private HashMap bindOneCard(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String ONECARD_NUM = (String) hsmpParam.get("ONECARD_NUM");
        String PHONG_NUM = (String) hsmpParam.get("PHONE_NUM");
        String MES_TYPE = MsgType.BIND_MESTYPE;
        Long Time = Long.valueOf(new Date().getTime());
        String QuTime = Time.toString();
        HashMap hsmpPkg = new HashMap();
        hsmpPkg.put("MES_TYPE", MES_TYPE);
        hsmpPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
        hsmpPkg.put("QU_TIME", QuTime);
        hsmpPkg.put("CARD_TYPE", CardType.getMsgCardType("TRANSPORTCARD"));
        hsmpPkg.put("CARD_NO", ONECARD_NUM);
        hsmpPkg.put("PHONE", PHONG_NUM);
        hsmpPkg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + Time);
        String retJson = JSONUtils.toJSON(hsmpPkg);
        HashMap hsmpRetPkg = new HashMap();
        try {
            HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPkg)));
            if (hsmpretPkg == null) {
                HashMap hsmp = new HashMap();
                hsmp.put("RESULT", "error");
                return hsmp;
            }
            String termNo = (String) hsmpretPkg.get("TERM_NO");
            if ("RESPONSE".equalsIgnoreCase((String) hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                Integer Confirm = Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM")));
                if (Confirm.intValue() == 1) {
                    hsmpRetPkg.put("RESULT", "success");
                }
                if (Confirm.intValue() == -1) {
                    hsmpRetPkg.put("RESULT", "error");
                }
                if (Confirm.intValue() != 0) {
                    return hsmpRetPkg;
                }
                hsmpRetPkg.put("RESULT", "failed");
                return hsmpRetPkg;
            }
            hsmpRetPkg.put("RESULT", "error");
            return hsmpRetPkg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private HashMap verifyBindPhone(String svcName, String subSvnName, HashMap hsmpParam) {
        String PHONE_NUM = (String) hsmpParam.get("PHONG_NUM");
        HashMap hsmpResult = new HashMap();
        if (StringUtils.isBlank(PHONE_NUM)) {
            hsmpResult.put("RET_CODE", "none");
        } else {
            if (!StringUtils.isBlank(PHONE_NUM)) {
                String VERIFY_PHONE = SysConfig.get("VERIFY.PHONE.BIND");
                Pattern p = Pattern.compile(VERIFY_PHONE);
                if (PHONE_NUM.matches(VERIFY_PHONE)) {
                    hsmpResult.put("RET_CODE", "success");
                }
            }
            hsmpResult.put("RET_CODE", "error");
        }
        return hsmpResult;
    }
}
