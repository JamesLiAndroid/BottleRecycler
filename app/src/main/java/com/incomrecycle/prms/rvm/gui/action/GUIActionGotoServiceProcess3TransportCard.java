package com.incomrecycle.prms.rvm.gui.action;

import android.content.Intent;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.starput.CommitRebateInfoFailActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.FirstRechargeWarnActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.NewCommitRebateInfoFailActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.NewFirstRechargeWarnActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.NewOneCardRechargeWarnActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.OneCardRechargeWarnActivity;
import com.incomrecycle.prms.rvm.gui.entity.CardEntity;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class GUIActionGotoServiceProcess3TransportCard extends GUIAction {
    private GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
    private HashMap<String, Object> hsmpStatus = null;
    private double recycleAmount = 0.0d;

    protected void doAction(Object[] paramObjs) {
        BaseActivity baseActivity = (BaseActivity) paramObjs[0];
        try {
            String OneCardVerson = SysConfig.get("RVM.ONECARD.DRV.VERSION");
            int CARD_STATUS;
            Intent intent;
            if (OneCardVerson.equals("0")) {
                this.hsmpStatus = this.guiCommonService.execute("GUIOneCardCommonService", "recycleEnd", null);
                if (this.hsmpStatus != null) {
                    CARD_STATUS = -2;
                    try {
                        CARD_STATUS = Integer.parseInt((String) this.hsmpStatus.get("CARD_STATUS"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        CardEntity.IS_RECHANGE = Integer.parseInt((String) this.hsmpStatus.get("IS_RECHANGE"));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    if (CARD_STATUS == 2 || CARD_STATUS == 1) {
                        intent = new Intent(baseActivity, OneCardRechargeWarnActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("JSON", JSONUtils.toJSON(this.hsmpStatus));
                        baseActivity.startActivity(intent);
                    }
                    if (CARD_STATUS == -2) {
                        intent = new Intent(baseActivity, FirstRechargeWarnActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("JSON", JSONUtils.toJSON(this.hsmpStatus));
                        baseActivity.startActivity(intent);
                    }
                    if (CARD_STATUS == -1) {
                        intent = new Intent(baseActivity, CommitRebateInfoFailActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        baseActivity.startActivity(intent);
                        return;
                    }
                    return;
                }
                intent = new Intent(baseActivity, CommitRebateInfoFailActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                baseActivity.startActivity(intent);
            } else if (OneCardVerson.equals("1")) {
                this.hsmpStatus = this.guiCommonService.execute("GUITrafficCardCommonService", "recycleEnd", null);
                if (this.hsmpStatus != null) {
                    CARD_STATUS = -2;
                    try {
                        CARD_STATUS = Integer.parseInt((String) this.hsmpStatus.get("CARD_STATUS"));
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                    try {
                        CardEntity.IS_RECHANGE = Integer.parseInt((String) this.hsmpStatus.get("IS_RECHANGE"));
                    } catch (Exception e222) {
                        e222.printStackTrace();
                    }
                    if (CARD_STATUS == 2 || CARD_STATUS == 1) {
                        intent = new Intent(baseActivity, NewOneCardRechargeWarnActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("JSON", JSONUtils.toJSON(this.hsmpStatus));
                        baseActivity.startActivity(intent);
                    }
                    if (CARD_STATUS == -2) {
                        intent = new Intent(baseActivity, NewFirstRechargeWarnActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("JSON", JSONUtils.toJSON(this.hsmpStatus));
                        baseActivity.startActivity(intent);
                    }
                    if (CARD_STATUS == -1) {
                        intent = new Intent(baseActivity, NewCommitRebateInfoFailActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        baseActivity.startActivity(intent);
                        return;
                    }
                    return;
                }
                intent = new Intent(baseActivity, NewCommitRebateInfoFailActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                baseActivity.startActivity(intent);
            }
        } catch (Exception e2222) {
            e2222.printStackTrace();
        }
    }
}
