package com.incomrecycle.prms.rvm.gui.action;

import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import java.util.HashMap;

public class GUIActionClearWeight extends GUIAction {
    private HashMap<String, Object> hsmpResult;
    private Logger logger = LoggerFactory.getLogger("VIEW");

    protected void doAction(Object[] paramObjs) {
        Activity activity = (Activity) paramObjs[0];
        try {
            this.hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "clearWeight", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.hsmpResult != null) {
            Boolean resultBo = (Boolean) this.hsmpResult.get("RESULT");
            try {
                this.logger.debug("GUIActionClearWeight////lst    ===" + resultBo);
                if (resultBo.booleanValue()) {
                    ((TextView) activity.findViewById(R.id.paper_weight_text)).setText("0");
                    Toast.makeText(activity, R.string.clearNumSuccess, 0).show();
                    return;
                }
                Toast.makeText(activity, R.string.clearNumFail, 0).show();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}
