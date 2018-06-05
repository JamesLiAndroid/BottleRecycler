package com.incomrecycle.prms.rvm.gui.action;

import android.content.Intent;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.starput.NewValidateOneCardActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.ValidateOneCardActivity;
import com.incomrecycle.prms.rvm.gui.entity.CardEntity;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class GUIActionGotoServiceProcess1TransportCard extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        BaseActivity baseActivity = (BaseActivity) paramObjs[0];
        try {
            String OneCardVerson = SysConfig.get("RVM.ONECARD.DRV.VERSION");
            Intent intent;
            if (OneCardVerson.equals("0")) {
                CardEntity.VERSION = 0;
                intent = new Intent(baseActivity, ValidateOneCardActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                baseActivity.startActivity(intent);
            } else if (OneCardVerson.equals("1")) {
                CardEntity.VERSION = 1;
                intent = new Intent(baseActivity, NewValidateOneCardActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                baseActivity.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
