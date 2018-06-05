package com.incomrecycle.prms.rvm.service.commonservice.gui;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.CardType;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.task.action.RCCInstanceTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GUIDonationCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if ("recycleEnd".equalsIgnoreCase(subSvnName)) {
            return recycleEnd(svcName, subSvnName, hsmpParam);
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
                    if (VENDING_WAY_SET == null || !VENDING_WAY_SET.contains("DONATION")) {
                        String VENDING_WAY = (String) mapVendingWayFlag.get(AllAdvertisement.VENDING_WAY);
                        if (VENDING_WAY != null && "DONATION".equalsIgnoreCase(VENDING_WAY)) {
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
            List<HashMap<String, String>> listInstanceTask = new ArrayList();
            SqlWhereBuilder sqlWhereBuilderRvmOpt = new SqlWhereBuilder();
            sqlWhereBuilderRvmOpt.addNumberEqualsTo("OPT_ID", OPT_ID).addNumberEqualsTo("OPT_STATUS", Integer.valueOf(0));
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
            sqlUpdateBuilder.setNumber("PROFIT_AMOUNT", "0").setString(AllAdvertisement.VENDING_WAY, "DONATION").setNumber("OPT_STATUS", Integer.valueOf(1)).setString("CARD_TYPE", CardType.NOCARD).setString("CARD_NO", null).setString(AllAdvertisement.SELECT_FLAG, SELECT_FLAG).setSqlWhere(sqlWhereBuilderRvmOpt);
            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
            HashMap<String, String> hsmpInstanceTask = new HashMap();
            hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_OPT");
            hsmpInstanceTask.put("OPT_ID", OPT_ID);
            listInstanceTask.add(hsmpInstanceTask);
            for (int i = 0; i < listInstanceTask.size(); i++) {
                RCCInstanceTask.addTask((HashMap) listInstanceTask.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
