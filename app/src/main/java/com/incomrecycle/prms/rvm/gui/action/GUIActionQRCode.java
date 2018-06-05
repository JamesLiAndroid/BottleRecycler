package com.incomrecycle.prms.rvm.gui.action;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelActivity;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelMainActivity;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class GUIActionQRCode extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        Activity activity = (Activity) paramObjs[0];
        String resultString = (String) paramObjs[1];
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        HashMap<String, Object> hsmpParam = new HashMap();
        hsmpParam.put("LOGIN_TYPE", "QR_CODE");
        hsmpParam.put("USER_EXT_CODE", resultString);
        try {
            HashMap<String, Object> hsmpResult = guiCommonService.execute("GUIMaintenanceCommonService", "login", hsmpParam);
            if (hsmpResult != null) {
                Intent intent;
                if ("success".equals((String) hsmpResult.get("RET_CODE"))) {
                    String staffPermission = (String) hsmpResult.get("STAFF_PERMISSION");
                    intent = new Intent();
                    intent.setClass(activity, ChannelMainActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("STAFF_PERMISSION", staffPermission);
                    activity.startActivity(intent);
                    activity.finish();
                    return;
                }
                Toast.makeText(activity, R.string.scan_fail, 0).show();
                intent = new Intent();
                intent.setClass(activity, ChannelActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
