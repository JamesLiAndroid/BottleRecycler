package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.gui.activity.view.MyGifView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class ActivityPageActivity extends BaseActivity {
    private Button btnYes;
    private boolean isClick = false;
    private List<String> listDisabledService = new ArrayList();
    private String recycleBottle = null;
    private TextView textView;
    private TextView textView1;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) ActivityPageActivity.this.findViewById(R.id.activity_page_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(ActivityPageActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            ActivityPageActivity.this.startActivity(intent);
                            ActivityPageActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            ActivityPageActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private String vending_way;

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.SHOW.ACTIVITY.TIME")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_activitypage);
        backgroundToActivity();
        this.textView = (TextView) findViewById(R.id.activity_page_remind);
        this.textView1 = (TextView) findViewById(R.id.activity_page_remind1);
        this.btnYes = (Button) findViewById(R.id.activity_page_confirm);
        this.btnYes.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!ActivityPageActivity.this.isClick) {
                    ActivityPageActivity.this.isClick = true;
                    HashMap<String, String> map = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
                    if (map != null) {
                        ActivityPageActivity.this.vending_way = (String) map.get(AllAdvertisement.VENDING_WAY);
                    }
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initRecycle", null);
                        HashMap<String, Object> hsmpInitProductType = new HashMap();
                        hsmpInitProductType.put("PRODUCT_TYPE", "BOTTLE");
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initProductType", hsmpInitProductType);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ActivityPageActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{ActivityPageActivity.this.getBaseActivity(), Integer.valueOf(2), ActivityPageActivity.this.vending_way});
                    ActivityPageActivity.this.finish();
                }
            }
        });
        ((Button) findViewById(R.id.activity_page_return_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!ActivityPageActivity.this.isClick) {
                    ActivityPageActivity.this.isClick = true;
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.ACTIVITY_RETURN);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(ActivityPageActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            ActivityPageActivity.this.startActivity(intent);
                            ActivityPageActivity.this.finish();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        });
        initview();
    }

    private void initview() {
        try {
            HashMap<String, Object> hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryServiceDisable", null);
            if (hsmpResult != null) {
                String SERVICE_DISABLED = (String) hsmpResult.get("SERVICE_DISABLED");
                if (!StringUtils.isBlank(SERVICE_DISABLED)) {
                    String[] SERVICE_DISABLED_ARRAY = SERVICE_DISABLED.split(",");
                    for (String add : SERVICE_DISABLED_ARRAY) {
                        this.listDisabledService.add(add);
                    }
                }
            }
            String MAIN_CLK_PIC = null;
            String MAIN_CLK_DESC = null;
            HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
            if (TRANSMIT_ADV != null) {
                HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
                if (HOMEPAGE_LEFT != null) {
                    Set<String> keys = HOMEPAGE_LEFT.keySet();
                    if (keys.contains(AllAdvertisement.CLICK_PICTURE_PATH)) {
                        MAIN_CLK_PIC = (String) HOMEPAGE_LEFT.get(AllAdvertisement.CLICK_PICTURE_PATH);
                    }
                    if (keys.contains(AllAdvertisement.MAIN_CLK_DESC)) {
                        MAIN_CLK_DESC = StringUtils.replace((String) HOMEPAGE_LEFT.get(AllAdvertisement.MAIN_CLK_DESC), "\\n", "\n");
                    }
                    if (keys.contains("RECYCLE_BOTTLE")) {
                        this.recycleBottle = (String) HOMEPAGE_LEFT.get("RECYCLE_BOTTLE");
                    }
                    if (keys.contains(AllAdvertisement.VENDING_WAY)) {
                        String VENDING_WAY = (String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_WAY);
                        String vendingWaySet = SysConfig.get("VENDING.WAY.SET");
                        String vendingWay = SysConfig.get("VENDING.WAY");
                        if (!StringUtils.isBlank(VENDING_WAY) && !this.listDisabledService.contains(VENDING_WAY) && "true".equalsIgnoreCase(this.recycleBottle) && vendingWay.contains(VENDING_WAY) && vendingWaySet.contains(VENDING_WAY)) {
                            this.btnYes.setVisibility(View.VISIBLE);
                            this.textView.setVisibility(View.GONE);
                            this.textView1.setVisibility(View.VISIBLE);
                            this.textView1.setText(MAIN_CLK_DESC);
                        } else {
                            this.btnYes.setVisibility(View.GONE);
                            this.textView.setVisibility(View.VISIBLE);
                            this.textView1.setVisibility(View.GONE);
                            this.textView.setText(MAIN_CLK_DESC);
                        }
                    }
                }
            }
            if (!StringUtils.isBlank(MAIN_CLK_PIC) && new File(MAIN_CLK_PIC).exists()) {
                ((TextView) findViewById(R.id.show_page_info)).setVisibility(View.GONE);
                MyGifView myGifView = (MyGifView) findViewById(R.id.show_page_img);
                myGifView.setVisibility(View.VISIBLE);
                myGifView.updateResource(-1, MAIN_CLK_PIC);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
        if ("CMD".equalsIgnoreCase((String) hsmpEvent.get("EVENT"))) {
            String CMD = (String) hsmpEvent.get("CMD");
            if ("REQUEST_DONATION_RECYCLE".equalsIgnoreCase(CMD) && "true".equals(this.recycleBottle)) {
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initRecycle", null);
                    HashMap<String, Object> hsmpInitProductType = new HashMap();
                    hsmpInitProductType.put("PRODUCT_TYPE", "BOTTLE");
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initProductType", hsmpInitProductType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{getBaseActivity(), Integer.valueOf(2), "DONATION"});
            }
            if ("REQUEST_RECYCLE".equalsIgnoreCase(CMD)) {
                try {
                    if ("true".equals(this.recycleBottle)) {
                        startRecycleBottle();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private void startRecycleBottle() throws Exception {
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initRecycle", null);
            HashMap<String, Object> hsmpInitProductType = new HashMap();
            hsmpInitProductType.put("PRODUCT_TYPE", "BOTTLE");
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initProductType", hsmpInitProductType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        GUIGlobal.setCurrentSession("USESTATE", Integer.valueOf(0));
        if (!StringUtils.isBlank(SysConfig.get("PutBottleActivity.class"))) {
            Intent intent = new Intent(this, Class.forName(SysConfig.get("PutBottleActivity.class")));
            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        }
    }
}
