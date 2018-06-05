package com.incomrecycle.prms.rvm.gui.action;

import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.CardType;
import com.incomrecycle.prms.rvm.common.SysDef.TrafficType;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelAdvancedActivity;
import com.incomrecycle.prms.rvm.util.ActivitySimpleAdapter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GUIActionNetWorkTest extends GUIAction {
    ChannelAdvancedActivity activity;
    Button exitButton;
    Button goBackButton;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            GUIActionNetWorkTest.this.hsmpResult = GUIActionNetWorkTest.this.netWorkTestThread.getHsmpResult();
            if (GUIActionNetWorkTest.this.hsmpResult != null) {
                List<HashMap<String, String>> columnLMap = new ArrayList();
                for (Object hm : (List) GUIActionNetWorkTest.this.hsmpResult.get("LINK_TESTING_LIST")) {
                    HashMap<String, String> hsmpItem = (HashMap) ((HashMap) hm).get(TrafficType.RCC);
                    String RESULT = (String) hsmpItem.get("RESULT");
                    String TIME = (String) hsmpItem.get("TIME");
                    hsmpItem.put("TIME", new DecimalFormat("0.000").format(Double.parseDouble(TIME)));
                    String NEW_RESULT = RESULT;
                    if (!("success".equals(RESULT) || "unknown".equals(RESULT) || !"failed".equals(RESULT))) {
                    }
                    hsmpItem.put("RESULT", NEW_RESULT);
                    columnLMap.add(hsmpItem);
                }
                ((ListView) GUIActionNetWorkTest.this.activity.findViewById(R.id.networkTestLv))
                        .setAdapter(new ActivitySimpleAdapter(GUIActionNetWorkTest.this.activity, columnLMap, R.layout.network_test_listviewlayout,
                                new String[]{"TIME", AllAdvertisement.MEDIA_TYPE, "RESULT"}, new int[]{R.id.networkDateTime, R.id.networkInfo, R.id.networkOper}));
            }
            GUIActionNetWorkTest.this.showNetWorkTestButton.setEnabled(true);
            GUIActionNetWorkTest.this.netCfgButton.setEnabled(true);
            GUIActionNetWorkTest.this.serCfgButton.setEnabled(true);
            GUIActionNetWorkTest.this.operDataButton.setEnabled(true);
            GUIActionNetWorkTest.this.goBackButton.setEnabled(true);
            GUIActionNetWorkTest.this.exitButton.setEnabled(true);
            GUIActionNetWorkTest.this.systemSettingsButton.setEnabled(true);
            GUIActionNetWorkTest.this.testBtn.setText(R.string.startTestBtn);
            GUIActionNetWorkTest.this.testBtn.setEnabled(true);
            GUIActionNetWorkTest.this.testBtn.setBackgroundResource(R.drawable.tongdaoweihu_btn_bg);
        }
    };
    private HashMap<String, Object> hsmpResult;
    private Logger logger = LoggerFactory.getLogger("VIEW");
    Button netCfgButton;
    NetWorkTestThread netWorkTestThread;
    Button operDataButton;
    Button serCfgButton;
    Button showNetWorkTestButton;
    Button systemSettingsButton;
    Button testBtn;

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
                GUIActionNetWorkTest.this.hsmpResult = this.guiCommonService.execute("GUIMaintenanceCommonService", "networkTesting", this.hsmpParam);
                this.handler.sendMessage(new Message());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public HashMap<String, Object> getHsmpResult() {
            return GUIActionNetWorkTest.this.hsmpResult;
        }
    }

    protected void doAction(Object[] paramObjs) {
        this.activity = (ChannelAdvancedActivity) paramObjs[0];
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        EditText testNum = (EditText) this.activity.findViewById(R.id.testNumEdit);
        EditText timeOut = (EditText) this.activity.findViewById(R.id.timeOutEdit);
        this.showNetWorkTestButton = (Button) this.activity.findViewById(R.id.showNetWorkTestButton);
        this.netCfgButton = (Button) this.activity.findViewById(R.id.netCfgButton);
        this.serCfgButton = (Button) this.activity.findViewById(R.id.serCfgButton);
        this.operDataButton = (Button) this.activity.findViewById(R.id.operDataButton);
        this.goBackButton = (Button) this.activity.findViewById(R.id.channel_advance__return_btn);
        this.exitButton = (Button) this.activity.findViewById(R.id.channel_advance_exit_btn);
        this.systemSettingsButton = (Button) this.activity.findViewById(R.id.systemSettingsButton);
        this.testBtn = (Button) this.activity.findViewById(R.id.testBtn);
        this.showNetWorkTestButton.setEnabled(false);
        this.netCfgButton.setEnabled(false);
        this.serCfgButton.setEnabled(false);
        this.operDataButton.setEnabled(false);
        this.goBackButton.setEnabled(false);
        this.exitButton.setEnabled(false);
        this.systemSettingsButton.setEnabled(false);
        this.testBtn.setText(R.string.testing);
        this.testBtn.setEnabled(false);
        this.testBtn.setBackgroundColor(-7829368);
        HashMap<String, Object> hsmpParam = new HashMap();
        if (testNum != null) {
            if (testNum.getText().toString().equals("")) {
                hsmpParam.put("TIMES", "1");
            } else if (Integer.parseInt(testNum.getText().toString()) > 10) {
                Toast.makeText(this.activity, this.activity.getResources().getString(R.string.test_warning), Toast.LENGTH_SHORT).show();
                hsmpParam.put("TIMES", CardType.MSG_BDJ);
            } else {
                hsmpParam.put("TIMES", testNum.getText().toString());
            }
        }
        if (timeOut != null) {
            hsmpParam.put("TIMEOUT", timeOut.getText().toString());
        } else {
            hsmpParam.put("TIMEOUT", "1000");
        }
        try {
            this.netWorkTestThread = new NetWorkTestThread(guiCommonService, hsmpParam, this.handler);
            SysGlobal.execute(this.netWorkTestThread);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
