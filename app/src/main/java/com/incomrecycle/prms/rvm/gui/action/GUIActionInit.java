package com.incomrecycle.prms.rvm.gui.action;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.RVMMainADActivity;
import com.incomrecycle.prms.rvm.RVMMainActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GUIActionInit extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        boolean isEnableRecycleBottle = false;
        boolean isEnableRecyclePaper = false;
        boolean isEnableRecycleForVendingWay = false;
        boolean isEnableRecycleForServiceEnable = false;
        List<String> listBottleDisableReason = new ArrayList();
        List<String> listPaperDisableReason = new ArrayList();
        try {
            int i;
            HashMap<String, Object> hsmpResult = guiCommonService.execute("GUIRecycleCommonService", "initApp", null);
            HashMap hsmpParam = new HashMap();
            hsmpParam.put("SERVICE_NAME", "PAPER");
            HashMap<String, Object> hsmpResultServiceEnable = guiCommonService.execute("GUIQueryCommonService", "queryServiceEnable", hsmpParam);
            if (hsmpResultServiceEnable != null && "TRUE".equalsIgnoreCase((String) hsmpResultServiceEnable.get("SERVICE_ENABLE"))) {
                isEnableRecyclePaper = true;
            }
            hsmpParam.put("SERVICE_NAME", "BOTTLE");
            hsmpResultServiceEnable = guiCommonService.execute("GUIQueryCommonService", "queryServiceEnable", hsmpParam);
            if (hsmpResultServiceEnable != null && "TRUE".equalsIgnoreCase((String) hsmpResultServiceEnable.get("SERVICE_ENABLE"))) {
                isEnableRecycleBottle = true;
            }
            String str = SysConfig.get("VENDING.WAY");
            List<String> listDisabledService = new ArrayList();
            HashMap<String, Object> hsmpResultServiceDisable = guiCommonService.execute("GUIQueryCommonService", "queryServiceDisable", null);
            if (hsmpResultServiceDisable != null) {
                String ServiceDisabled = (String) hsmpResultServiceDisable.get("SERVICE_DISABLED");
                if (!StringUtils.isBlank(ServiceDisabled) && ServiceDisabled.length() > 0) {
                    String[] strDisabledService = ServiceDisabled.split(",");
                    for (String add : strDisabledService) {
                        listDisabledService.add(add);
                    }
                }
            }
            if (!StringUtils.isBlank(str)) {
                isEnableRecycleForVendingWay = true;
                String[] strSet = str.split(";");
                for (String add2 : strSet) {
                    if (!listDisabledService.contains(add2)) {
                        isEnableRecycleForServiceEnable = true;
                    }
                }
            }
            String[] productTypes = SysConfig.get("RECYCLE.SERVICE.SET").split(",");
            for (i = 0; i < productTypes.length; i++) {
                if ("PAPER".equals(productTypes[i])) {
                    if (!(isEnableRecyclePaper && isEnableRecycleForServiceEnable)) {
                        listPaperDisableReason.add("SERVICE_DISABLE");
                    }
                    if (!isEnableRecycleForVendingWay) {
                        listPaperDisableReason.add("VENDINGWAY_DISABLE");
                    }
                }
                if ("BOTTLE".equals(productTypes[i])) {
                    if (!(isEnableRecycleBottle && isEnableRecycleForServiceEnable)) {
                        listBottleDisableReason.add("SERVICE_DISABLE");
                    }
                    if (!isEnableRecycleForVendingWay) {
                        listBottleDisableReason.add("VENDINGWAY_DISABLE");
                    }
                }
            }
            if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                try {
                    if (RVMMainADActivity.class.getName().equalsIgnoreCase(SysConfig.get("RVMMActivity.class"))) {
                        RVMMainADActivity rvmMainActivity = (RVMMainADActivity) paramObjs[0];
                        rvmMainActivity.enableRecyclePaper(listPaperDisableReason);
                        rvmMainActivity.enableRecycleBottle(listBottleDisableReason);
                    }
                    if (RVMMainActivity.class.getName().equalsIgnoreCase(SysConfig.get("RVMMActivity.class"))) {
                        RVMMainActivity rvmMainActivity2 = (RVMMainActivity) paramObjs[0];
                        rvmMainActivity2.enableRecyclePaper(listPaperDisableReason);
                        rvmMainActivity2.enableRecycleBottle(listBottleDisableReason);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
}
