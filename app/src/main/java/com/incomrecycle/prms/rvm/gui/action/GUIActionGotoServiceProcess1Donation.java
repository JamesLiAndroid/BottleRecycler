package com.incomrecycle.prms.rvm.gui.action;

import android.content.Intent;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.activity.starput.ActivityAdActivity;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class GUIActionGotoServiceProcess1Donation extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        BaseActivity baseActivity = (BaseActivity) paramObjs[0];
        try {
            HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
            HashMap<String, Object> VENDING_FLAG = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
            Intent intent;
            if (TRANSMIT_ADV != null) {
                HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
                if (HOMEPAGE_LEFT == null || StringUtils.isBlank((String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_PIC))) {
                    baseActivity.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{baseActivity, Integer.valueOf(2), "DONATION"});
                    return;
                }
                intent = new Intent(baseActivity, ActivityAdActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                baseActivity.startActivity(intent);
                return;
            }
            if (VENDING_FLAG != null) {
                if (!StringUtils.isBlank((String) VENDING_FLAG.get(AllAdvertisement.VENDING_PIC))) {
                    intent = new Intent(baseActivity, ActivityAdActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    baseActivity.startActivity(intent);
                    return;
                }
            }
            baseActivity.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{baseActivity, Integer.valueOf(2), "DONATION"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
