package com.incomrecycle.prms.rvm.service.commonservice.gui;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.util.HashMap;

public class GUIQRCodeCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        if ("recycleEnd".equalsIgnoreCase(subSvcName)) {
            return recycleEnd(svcName, subSvcName, hsmpParam);
        }
        if ("verifyQRCode".equalsIgnoreCase(subSvcName)) {
            return verifyQRCode(svcName, subSvcName, hsmpParam);
        }
        if ("balanceQRCode".equalsIgnoreCase(subSvcName)) {
            return balanceQRCode(svcName, subSvcName, hsmpParam);
        }
        return null;
    }

    private HashMap recycleEnd(String svcName, String subSvcName, HashMap hsmpParam) {
        try {
            String CARD_NO = (String) ServiceGlobal.getCurrentSession("CARD_NO");
            String CARD_TYPE = (String) ServiceGlobal.getCurrentSession("CARD_TYPE");
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
                    if (VENDING_WAY_SET == null || !VENDING_WAY_SET.contains("QRCODE")) {
                        String VENDING_WAY = (String) mapVendingWayFlag.get(AllAdvertisement.VENDING_WAY);
                        if (VENDING_WAY != null && "QRCODE".equalsIgnoreCase(VENDING_WAY)) {
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
            sqlUpdateBuilder.setString(AllAdvertisement.VENDING_WAY, "QRCODE").setNumber("OPT_STATUS", Integer.valueOf(1)).setString("CARD_TYPE", CARD_TYPE).setString("CARD_NO", CARD_NO).setString(AllAdvertisement.SELECT_FLAG, SELECT_FLAG).setSqlWhere(sqlWhereBuilderRvmOpt);
            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
            HashMap<String, Object> hasmPram = new HashMap();
            hasmPram.put("CARD_NO", CARD_NO);
            hasmPram.put("CARD_TYPE", CARD_TYPE);
            hasmPram.put("OPT_ID", OPT_ID);
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "cutPrice", hasmPram);
            HashMap hsmpUploadParam = new HashMap();
            hsmpUploadParam.put("OPT_ID", OPT_ID);
            HashMap hsmpUploadResult = CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "uploadOptDetail", hsmpUploadParam);
            if (hsmpUploadResult != null && "success".equalsIgnoreCase((String) hsmpUploadResult.get("RESULT"))) {
                HashMap hsmpResult = new HashMap();
                hsmpResult.put("INCOM_AMOUNT", hsmpUploadResult.get("INCOM_AMOUNT"));
                hsmpResult.put("CARD_STATUS", hsmpUploadResult.get("CARD_STATUS"));
                hsmpResult.put("RECHARGE", hsmpUploadResult.get("RECHARGE"));
                hsmpResult.put("IS_RECHANGE", hsmpUploadResult.get("IS_RECHANGE"));
                hsmpResult.put("CARD_NAME", hsmpUploadResult.get("CARD_NAME"));
                hsmpResult.put("BOTTLE_NUM", hsmpUploadResult.get("BOTTLE_NUM"));
                hsmpResult.put("CREDIT", hsmpUploadResult.get("CREDIT"));
                hsmpResult.put("TODAY_BOTTLE", hsmpUploadResult.get("TODAY_BOTTLE"));
                return hsmpResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private HashMap verifyQRCode(String svcName, String subSvcName, HashMap hsmpParam) {
        HashMap hsmpResult = new HashMap();
        String QRCODE_NO = (String) hsmpParam.get("QRCODE_NO");
        if (StringUtils.isBlank(QRCODE_NO)) {
            hsmpResult.put("RET_QRCODE", "no_qrcard_num");
        } else if (QRCODE_NO.length() == 9) {
            if (QRCODE_NO.startsWith("S-")) {
                ServiceGlobal.setCurrentSession("CARD_NO", QRCODE_NO);
                ServiceGlobal.setCurrentSession("CARD_TYPE", "SQRCODE");
            } else {
                ServiceGlobal.setCurrentSession("CARD_NO", QRCODE_NO);
                ServiceGlobal.setCurrentSession("CARD_TYPE", "QRCODE");
            }
            hsmpResult.put("RET_QRCODE", "right_qrcard_num");
        } else {
            hsmpResult.put("RET_QRCODE", "error_qrcard_num");
        }
        return hsmpResult;
    }

    private HashMap balanceQRCode(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        String QRCODE_NO = (String) hsmpParam.get("QRCODE_NO");
        String CARD_TYPE = (String) ServiceGlobal.getCurrentSession("CARD_TYPE");
        HashMap hsmpStatusParam = new HashMap();
        hsmpStatusParam.put("CARD_NO", QRCODE_NO);
        hsmpStatusParam.put("CARD_TYPE", CARD_TYPE);
        HashMap hsmpretPkg = new GUIRecycleCommonService().execute("GUIRecycleCommonService", "queryCardStatus", hsmpStatusParam);
        if (hsmpretPkg == null) {
            return null;
        }
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
}
