package com.incomrecycle.prms.rvm.gui.action;

import android.content.Intent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.starput.BdjActivity;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class GUIActionGotoServiceProcess1Bdj extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        BaseActivity baseActivity = (BaseActivity) paramObjs[0];
        try {
            Intent intent = new Intent(baseActivity, BdjActivity.class);
            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            baseActivity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
