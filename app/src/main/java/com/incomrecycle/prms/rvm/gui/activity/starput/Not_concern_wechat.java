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

public class Not_concern_wechat extends BaseActivity {
    private boolean isClick = false;
    private List<String> listDisabledService = new ArrayList();
    private String recycleBottle = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) Not_concern_wechat.this.findViewById(R.id.wechat_phone_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(Not_concern_wechat.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            Not_concern_wechat.this.startActivity(intent);
                            Not_concern_wechat.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            Not_concern_wechat.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private String vending_way;
    private Button wechat_concern_back;
    private Button wechat_concern_confirm;
    private TextView wechat_phone_remind_text;
    private TextView wechat_phone_remind_text1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_concern_wechat);
        backgroundToActivity();
        this.wechat_phone_remind_text = (TextView) findViewById(R.id.wechat_phone_remind_text);
        this.wechat_phone_remind_text.setGravity(17);
        this.wechat_phone_remind_text1 = (TextView) findViewById(R.id.wechat_phone_remind_text1);
        this.wechat_phone_remind_text1.setGravity(17);
        this.wechat_concern_confirm = (Button) findViewById(R.id.wechat_concern_confirm);
        this.wechat_concern_back = (Button) findViewById(R.id.wechat_concern_back);
        this.wechat_concern_confirm.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!Not_concern_wechat.this.isClick) {
                    Not_concern_wechat.this.isClick = true;
                    HashMap<String, String> map = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
                    if (map != null) {
                        Not_concern_wechat.this.vending_way = (String) map.get(AllAdvertisement.VENDING_WAY);
                    }
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initRecycle", null);
                        HashMap<String, Object> hsmpInitProductType = new HashMap();
                        hsmpInitProductType.put("PRODUCT_TYPE", "BOTTLE");
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initProductType", hsmpInitProductType);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Not_concern_wechat.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{Not_concern_wechat.this.getBaseActivity(), Integer.valueOf(2), Not_concern_wechat.this.vending_way});
                    Not_concern_wechat.this.finish();
                }
            }
        });
        this.wechat_concern_back.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!Not_concern_wechat.this.isClick) {
                    Not_concern_wechat.this.isClick = true;
                    if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(Not_concern_wechat.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            Not_concern_wechat.this.startActivity(intent);
                            Not_concern_wechat.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        initview();
    }

    public void finish() {
        super.finish();
    }

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.SHOW.ACTIVITY.WECHAT.TIME")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onStop() {
        super.onStop();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    public void initview() {
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
            HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
            if (TRANSMIT_ADV != null) {
                HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
                if (HOMEPAGE_LEFT != null) {
                    Set<String> keys = HOMEPAGE_LEFT.keySet();
                    if (keys.contains(AllAdvertisement.CLICK_PICTURE_PATH)) {
                        MAIN_CLK_PIC = (String) HOMEPAGE_LEFT.get(AllAdvertisement.CLICK_PICTURE_PATH);
                    }
                    if (keys.contains(AllAdvertisement.MAIN_CLK_DESC)) {
                        String MAIN_CLK_DESC = StringUtils.replace((String) HOMEPAGE_LEFT.get(AllAdvertisement.MAIN_CLK_DESC), "\\n", "\n");
                    }
                    if (keys.contains("RECYCLE_BOTTLE")) {
                        this.recycleBottle = (String) HOMEPAGE_LEFT.get("RECYCLE_BOTTLE");
                    }
                    if (keys.contains(AllAdvertisement.VENDING_WAY)) {
                        String VENDING_WAY = (String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_WAY);
                        String vendingWaySet = SysConfig.get("VENDING.WAY.SET");
                        String vendingWay = SysConfig.get("VENDING.WAY");
                        if (!StringUtils.isBlank(VENDING_WAY) && !this.listDisabledService.contains(VENDING_WAY) && "true".equalsIgnoreCase(this.recycleBottle) && vendingWay.contains(VENDING_WAY) && vendingWaySet.contains(VENDING_WAY)) {
                            this.wechat_concern_confirm.setVisibility(View.VISIBLE);
                            this.wechat_phone_remind_text.setVisibility(View.GONE);
                            this.wechat_phone_remind_text1.setVisibility(View.VISIBLE);
                        } else {
                            this.wechat_concern_confirm.setVisibility(View.GONE);
                            this.wechat_phone_remind_text.setVisibility(View.VISIBLE);
                            this.wechat_phone_remind_text1.setVisibility(View.GONE);
                        }
                    }
                }
            }
            if (!StringUtils.isBlank(MAIN_CLK_PIC) && new File(MAIN_CLK_PIC).exists()) {
                MyGifView myGifView = (MyGifView) findViewById(R.id.show_concern_img);
                myGifView.setVisibility(View.VISIBLE);
                myGifView.updateResource(-1, MAIN_CLK_PIC);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
