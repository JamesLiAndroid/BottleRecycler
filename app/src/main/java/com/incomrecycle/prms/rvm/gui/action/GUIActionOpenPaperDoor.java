package com.incomrecycle.prms.rvm.gui.action;

import android.widget.Toast;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelMainActivity;
import java.util.HashMap;

public class GUIActionOpenPaperDoor extends GUIAction {
    private HashMap<String, Object> hsmpResult;

    protected void doAction(Object[] paramObjs) {
        ChannelMainActivity activity = (ChannelMainActivity) paramObjs[0];
        try {
            this.hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "openPaperDoor", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.hsmpResult != null) {
            try {
                if (((Boolean) this.hsmpResult.get("RESULT")).booleanValue()) {
                    Toast.makeText(activity, R.string.channlOpenDoorSuccess, 0).show();
                } else {
                    Toast.makeText(activity, R.string.channlOpenDoorFail, 0).show();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}
