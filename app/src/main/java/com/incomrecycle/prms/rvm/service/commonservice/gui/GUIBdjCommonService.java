package com.incomrecycle.prms.rvm.service.commonservice.gui;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.SysConfig;
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

public class GUIBdjCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        if ("recycleEnd".equalsIgnoreCase(subSvcName)) {
            return recycleEnd(svcName, subSvcName, hsmpParam);
        }
        if ("verifyPhone".equalsIgnoreCase(subSvcName)) {
            return verifyPhone(svcName, subSvcName, hsmpParam);
        }
        return null;
    }

    private HashMap recycleEnd(String svcName, String subSvcName, HashMap hsmpParam) {
        try {
            String CARD_NO = (String) ServiceGlobal.getCurrentSession("CARD_NO");
            String CARD_TYPE = "BDJ";
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
                    if (VENDING_WAY_SET == null || !VENDING_WAY_SET.contains("BDJ")) {
                        String VENDING_WAY = (String) mapVendingWayFlag.get(AllAdvertisement.VENDING_WAY);
                        if (VENDING_WAY != null && "BDJ".equalsIgnoreCase(VENDING_WAY)) {
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
            sqlUpdateBuilder.setString(AllAdvertisement.VENDING_WAY, "BDJ").setNumber("OPT_STATUS", Integer.valueOf(1)).setString("CARD_TYPE", CARD_TYPE).setString("CARD_NO", CARD_NO).setString(AllAdvertisement.SELECT_FLAG, SELECT_FLAG).setSqlWhere(sqlWhereBuilderRvmOpt);
            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
            HashMap<String, Object> hasmPram = new HashMap();
            hasmPram.put("CARD_NO", CARD_NO);
            hasmPram.put("CARD_TYPE", CARD_TYPE);
            hasmPram.put("OPT_ID", OPT_ID);
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "cutPrice", hasmPram);
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "limitBottleCount", hasmPram);
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
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private HashMap verifyPhone(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        HashMap hsmpResult = new HashMap();
        String PHONE_NO = (String) hsmpParam.get("PHONE_NO");
        if (StringUtils.isBlank(PHONE_NO)) {
            hsmpResult.put("RET_PHONE_NO", "no_phone_num");
            return hsmpResult;
        } else if (StringUtils.isBlank(PHONE_NO) || !PHONE_NO.matches(SysConfig.get("VERIFY.PHONE.BIND"))) {
            hsmpResult.put("RET_PHONE_NO", "error_phone_num");
            return hsmpResult;
        } else {
            String CARD_NO = PHONE_NO;
            String CARD_TYPE = "BDJ";
            ServiceGlobal.setCurrentSession("CARD_NO", CARD_NO);
            ServiceGlobal.setCurrentSession("CARD_TYPE", "BDJ");
            HashMap hsmpStatusParam = new HashMap();
            hsmpStatusParam.put("CARD_NO", "WX_" + CARD_NO);
            hsmpStatusParam.put("CARD_TYPE", "WECHAT");
            HashMap hsmpretPkg = new GUIRecycleCommonService().execute("GUIRecycleCommonService", "queryCardStatus", hsmpStatusParam);
            if (hsmpretPkg == null) {
                return null;
            }
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
}
