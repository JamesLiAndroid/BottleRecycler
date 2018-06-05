package com.incomrecycle.prms.rvm.gui.action;

import android.widget.Toast;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelMainActivity;
import java.util.HashMap;

public class GUIActionOpenDoor extends GUIAction {
    private HashMap<String, Object> hsmpResult;
    private Logger logger = LoggerFactory.getLogger("VIEW");

    protected void doAction(Object[] paramObjs) {
        ChannelMainActivity activity = (ChannelMainActivity) paramObjs[0];
        try {
            this.hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "openDoor", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.hsmpResult != null) {
            Boolean resultBo = (Boolean) this.hsmpResult.get("RESULT");
            try {
                this.logger.debug("GUIActionOpenDoor////lst    ===" + resultBo);
                if (resultBo.booleanValue()) {
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
