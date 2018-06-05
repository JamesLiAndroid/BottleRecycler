package com.incomrecycle.prms.rvm.gui.action;

import android.content.Intent;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class GUIActionGotoServiceProcess extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        BaseActivity baseActivity = (BaseActivity) paramObjs[0];
        Integer processId = (Integer) paramObjs[1];
        String wendingWay = (String) paramObjs[2];
        String PRODUCT_TYPE = (String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE");
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        try {
            HashMap hsmpInitVendingWay;
            if (StringUtils.isBlank(wendingWay)) {
                hsmpInitVendingWay = guiCommonService.execute("GUIRecycleCommonService", "queryVendingWay", null);
                if (hsmpInitVendingWay != null) {
                    wendingWay = (String) hsmpInitVendingWay.get(AllAdvertisement.VENDING_WAY);
                }
            }
            HashMap<String, Object> hsmpHasRecycled = guiCommonService.execute("GUIQueryCommonService", "hasRecycled", null);
            boolean hasPut = false;
            if (hsmpHasRecycled != null && "TRUE".equalsIgnoreCase((String) hsmpHasRecycled.get("RESULT"))) {
                hasPut = true;
            }
            if (processId.intValue() == 2) {
                hsmpInitVendingWay = new HashMap();
                hsmpInitVendingWay.put(AllAdvertisement.VENDING_WAY, wendingWay);
                guiCommonService.execute("GUIRecycleCommonService", "initVendingWay", hsmpInitVendingWay);
                if (hasPut) {
                    processId = Integer.valueOf(3);
                }
            }
            if ((processId.intValue() == 1 || processId.intValue() == 3) && !StringUtils.isBlank(wendingWay)) {
                baseActivity.executeGUIAction(true, (GUIAction) Class.forName(SysConfig.get("SERVICE.PROCESS.GUIAction." + processId + "." + wendingWay)).newInstance(), new Object[]{baseActivity});
            }
            if (processId.intValue() == 2) {
                Intent intent;
                if ("BOTTLE".equals(PRODUCT_TYPE) && !StringUtils.isBlank(SysConfig.get("PutBottleActivity.class"))) {
                    intent = new Intent(baseActivity, Class.forName(SysConfig.get("PutBottleActivity.class")));
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    baseActivity.startActivity(intent);
                }
                if ("PAPER".equals(PRODUCT_TYPE) && !StringUtils.isBlank(SysConfig.get("PutPaperActivity.class"))) {
                    intent = new Intent(baseActivity, Class.forName(SysConfig.get("PutPaperActivity.class")));
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    baseActivity.startActivity(intent);
                }
            }
            if (processId.intValue() == 4) {
                baseActivity.executeGUIAction(true, new GUIActionGotoThankPage(), new Object[]{baseActivity});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
