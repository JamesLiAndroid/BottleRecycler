package com.incomrecycle.prms.rvm.gui.action;

import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.TrafficType;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelAdvancedActivity;
import java.util.HashMap;

public class GUIActionSetupTesting extends GUIAction {
    ChannelAdvancedActivity activity;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            GUIActionSetupTesting.this.hsmpResult = GUIActionSetupTesting.this.netWorkTestThread.getHsmpResult();
            if (GUIActionSetupTesting.this.hsmpResult != null) {
                String RESULT = null;
                if (GUIActionSetupTesting.this.hsmpResult != null) {
                    HashMap hsmpRCC = (HashMap) GUIActionSetupTesting.this.hsmpResult.get(TrafficType.RCC);
                    if (hsmpRCC != null) {
                        if ("success".equalsIgnoreCase((String) hsmpRCC.get("RESULT"))) {
                            RESULT = "success";
                        }
                        if ("failed".equalsIgnoreCase((String) hsmpRCC.get("RESULT"))) {
                            RESULT = "failed";
                        }
                        if ("unknown".equalsIgnoreCase((String) hsmpRCC.get("RESULT"))) {
                            RESULT = "unknown";
                        }
                    }
                }
                if ("success".equalsIgnoreCase(RESULT)) {
                    GUIActionSetupTesting.this.setupCfgDoneBtn.setEnabled(true);
                    GUIActionSetupTesting.this.setupCfgDoneBtn.setBackgroundResource(R.drawable.tongdaoweihu_btn_bg);
                    Toast.makeText(GUIActionSetupTesting.this.activity, R.string.setupCfgTestSuccess, 0).show();
                    GUIActionSetupTesting.this.activity.executeGUIAction(true, new GUIActionSetupPropertyDone(), new Object[]{GUIActionSetupTesting.this.activity});
                } else if ("unknown".equalsIgnoreCase(RESULT)) {
                    Toast.makeText(GUIActionSetupTesting.this.activity, R.string.setupCfgTestUnknown, 0).show();
                } else {
                    Toast.makeText(GUIActionSetupTesting.this.activity, R.string.setupCfgTestFail, 0).show();
                }
            }
        }
    };
    private HashMap hsmpResult;
    private Logger logger = LoggerFactory.getLogger("VIEW");
    NetWorkTestThread netWorkTestThread;
    Button setupCfgDoneBtn;

    class NetWorkTestThread implements Runnable {
        GUICommonService guiCommonService;
        HashMap<String, Object> hampResult;
        Handler handler;
        HashMap<String, Object> hsmpParam;
        String subSvcName;
        String svcName;

        public NetWorkTestThread(GUICommonService guiCommonService, HashMap<String, Object> hsmpParam, Handler handler) {
            this.handler = handler;
            this.guiCommonService = guiCommonService;
            this.hsmpParam = hsmpParam;
        }

        public void run() {
            try {
                GUIActionSetupTesting.this.hsmpResult = this.guiCommonService.execute("GUIMaintenanceCommonService", "linkTest", this.hsmpParam);
                this.handler.sendMessage(new Message());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public HashMap<String, Object> getHsmpResult() {
            return GUIActionSetupTesting.this.hsmpResult;
        }
    }

    protected void doAction(Object[] paramObjs) {
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        this.activity = (ChannelAdvancedActivity) paramObjs[0];
        EditText setupCfgTimerNumEdit = (EditText) this.activity.findViewById(R.id.setupCfgTimerNumEdit);
        EditText setupCfgRccIpEdit = (EditText) this.activity.findViewById(R.id.setupCfgRccIpEdit);
        EditText setupCfgRccPortEdit = (EditText) this.activity.findViewById(R.id.setupCfgRccPortEdit);
        this.setupCfgDoneBtn = (Button) this.activity.findViewById(R.id.setupCfgDoneBtn);
        HashMap<String, Object> hsmpParam = new HashMap();
        hsmpParam.put("RVM_CODE", setupCfgTimerNumEdit.getText().toString());
        hsmpParam.put("RCC_IP", setupCfgRccIpEdit.getText().toString());
        hsmpParam.put("RCC_PORT", setupCfgRccPortEdit.getText().toString());
        hsmpParam.put("RVM_AREA_CODE", SysConfig.get("RVM.AREA.CODE"));
        try {
            this.netWorkTestThread = new NetWorkTestThread(guiCommonService, hsmpParam, this.handler);
            SysGlobal.execute(this.netWorkTestThread);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
