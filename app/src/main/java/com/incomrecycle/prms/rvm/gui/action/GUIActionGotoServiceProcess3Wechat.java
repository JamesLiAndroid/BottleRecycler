package com.incomrecycle.prms.rvm.gui.action;

import android.content.Intent;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.starput.WechatResultActivity;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class GUIActionGotoServiceProcess3Wechat extends GUIAction {
    GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
    HashMap<String, Object> hsmpStatus = null;
    double recycleAmount = 0.0d;

    protected void doAction(Object[] paramObjs) {
        BaseActivity baseActivity = (BaseActivity) paramObjs[0];
        try {
            this.hsmpStatus = this.guiCommonService.execute("GUIWeChatCommonService", "recycleEnd", null);
            Intent intent = new Intent(baseActivity, WechatResultActivity.class);
            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("JSON", JSONUtils.toJSON(this.hsmpStatus));
            baseActivity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
