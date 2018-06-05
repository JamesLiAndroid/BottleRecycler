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

public class GUIGreenCardCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if ("verifyCardNo".equalsIgnoreCase(subSvnName)) {
            return verifyCardNo(svcName, subSvnName, hsmpParam);
        }
        if ("recycleEnd".equalsIgnoreCase(subSvnName)) {
            return recycleEnd(svcName, subSvnName, hsmpParam);
        }
        return null;
    }

    private HashMap verifyCardNo(String svcName, String subSvnName, HashMap hsmpParam) {
        HashMap hsmpResult = new HashMap();
        String CARD_TYPE = "CARD";
        String CARD_NO = (String) hsmpParam.get("CARD_NO");
        if (StringUtils.isBlank(CARD_NO)) {
            hsmpResult.put("RET_CODE", "no_card_no");
        } else if (StringUtils.isBlank(CARD_TYPE) || !CARD_NO.matches(SysConfig.get("VERIFY.GREENCARD.PATTERN"))) {
            hsmpResult.put("RET_CODE", "error");
        } else {
            ServiceGlobal.setCurrentSession("CARD_TYPE", CARD_TYPE);
            ServiceGlobal.setCurrentSession("CARD_NO", CARD_NO);
            hsmpResult.put("RET_CODE", "success");
        }
        return hsmpResult;
    }

    private HashMap recycleEnd(String svcName, String subSvnName, HashMap hsmpParam) {
        try {
            String CARD_NO = (String) ServiceGlobal.getCurrentSession("CARD_NO");
            String CARD_TYPE = "CARD";
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
                    if (VENDING_WAY_SET == null || !VENDING_WAY_SET.contains("CARD")) {
                        String VENDING_WAY = (String) mapVendingWayFlag.get(AllAdvertisement.VENDING_WAY);
                        if (VENDING_WAY != null && "CARD".equalsIgnoreCase(VENDING_WAY)) {
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
            sqlUpdateBuilder.setString(AllAdvertisement.VENDING_WAY, "CARD").setNumber("OPT_STATUS", Integer.valueOf(1)).setString("CARD_TYPE", CARD_TYPE).setString("CARD_NO", CARD_NO).setString(AllAdvertisement.SELECT_FLAG, SELECT_FLAG).setSqlWhere(sqlWhereBuilderRvmOpt);
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
}
