package com.incomrecycle.prms.rvm.gui.action;

import android.app.Activity;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelMainActivity;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

public class GUIActionMaintenanceLogin extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        Activity activity = (Activity) paramObjs[0];
        String code = (String) paramObjs[1];
        String step = (String) paramObjs[2];
        HashMap hsmpResult = (HashMap) paramObjs[3];
        if ("1".equals(step)) {
            ((Button) activity.findViewById(R.id.channelLoginButton)).setEnabled(false);
            HashMap<String, Object> hsmpParam = new HashMap();
            if (code == null) {
                EditText userName = (EditText) activity.findViewById(R.id.userNameEditText);
                EditText userPwd = (EditText) activity.findViewById(R.id.passWordEditText);
                hsmpParam.put("LOGIN_TYPE", "USER_STAFF_ID");
                hsmpParam.put("USER", userName.getText().toString());
                hsmpParam.put("PASSWORD", userPwd.getText().toString());
            } else {
                hsmpParam.put("LOGIN_TYPE", "QR_CODE");
                hsmpParam.put("USER_EXT_CODE", code);
            }
            SysGlobal.execute(new Thread() {
                private HashMap hsmpParam;

                public Thread setParam(HashMap hsmpParam) {
                    this.hsmpParam = hsmpParam;
                    return this;
                }

                public void run() {
                    HashMap hsmpEvent;
                    try {
                        HashMap<String, Object> hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "login", this.hsmpParam);
                        hsmpEvent = new HashMap();
                        hsmpEvent.put(AllAdvertisement.MEDIA_TYPE, "Activity");
                        hsmpEvent.put("EVENT", "LOGIN");
                        hsmpEvent.put("RESULT", hsmpResult);
                        GUIGlobal.getEventMgr().addEvent(hsmpEvent);
                    } catch (Exception e) {
                        hsmpEvent = new HashMap();
                        hsmpEvent.put(AllAdvertisement.MEDIA_TYPE, "Activity");
                        hsmpEvent.put("EVENT", "LOGIN");
                        GUIGlobal.getEventMgr().addEvent(hsmpEvent);
                    }
                }
            }.setParam(hsmpParam));
        }
        if ("2".equals(step)) {
            ((Button) activity.findViewById(R.id.channelLoginButton)).setEnabled(true);
            if (hsmpResult != null) {
                try {
                    if ("success".equals((String) hsmpResult.get("RET_CODE"))) {
                        CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "maintainUpdate", null);
                        String staffPermission = (String) hsmpResult.get("STAFF_PERMISSION");
                        Intent intent = new Intent();
                        intent.setClass(activity, ChannelMainActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("STAFF_PERMISSION", staffPermission);
                        activity.startActivity(intent);
                        activity.finish();
                        return;
                    }
                    Toast.makeText(activity, R.string.channelLoginFail, 0).show();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            Toast.makeText(activity, R.string.channelLoginNetFail, 0).show();
        }
    }
}
