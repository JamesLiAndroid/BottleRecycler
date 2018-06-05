package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
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
import java.util.HashMap;
import java.util.Set;

public class ActivityAdActivity extends BaseActivity {
    private boolean isClick = false;
    private MyGifView myGif = null;
    private TextView remindTextView = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        SysGlobal.execute(new Runnable() {
                            public void run() {
                                ActivityAdActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{ActivityAdActivity.this.getBaseActivity(), Integer.valueOf(2), ActivityAdActivity.this.vendingWay});
                                ActivityAdActivity.this.finish();
                            }
                        });
                    } else {
                        ((TextView) ActivityAdActivity.this.findViewById(R.id.activity_youku_time)).setText("" + remainedSeconds);
                    }
                }
            };
            ActivityAdActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private String vendingWay = null;

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.SHOW.ACTIVITY.TIME")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void finish() {
        super.finish();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_ad_activity);
        backgroundToActivity();
        this.remindTextView = (TextView) findViewById(R.id.activity_youku_remind);
        initview();
        Button btnYes = (Button) findViewById(R.id.activity_youku_confirm);
        btnYes.setText(R.string.confirm);
        btnYes.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!ActivityAdActivity.this.isClick) {
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            ActivityAdActivity.this.isClick = true;
                            try {
                                CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "phoneMovieTicket", null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ActivityAdActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{ActivityAdActivity.this.getBaseActivity(), Integer.valueOf(2), ActivityAdActivity.this.vendingWay});
                            ActivityAdActivity.this.finish();
                        }
                    });
                }
            }
        });
        Button btnNo = (Button) findViewById(R.id.activity_youku_return_btn);
        btnNo.setText(R.string.backBtn);
        btnNo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SysGlobal.execute(new Runnable() {
                    public void run() {
                        ActivityAdActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{ActivityAdActivity.this.getBaseActivity(), Integer.valueOf(2), ActivityAdActivity.this.vendingWay});
                        ActivityAdActivity.this.finish();
                    }
                });
            }
        });
    }

    protected void onStop() {
        if (this.myGif != null) {
            this.myGif.updateResource(-1, "");
            this.myGif = null;
        }
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
        super.onStop();
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    public void initview() {
        HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
        Set<String> keys;
        String AD_PIC;
        if (TRANSMIT_ADV != null) {
            HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
            if (HOMEPAGE_LEFT != null) {
                keys = HOMEPAGE_LEFT.keySet();
                if (keys.contains(AllAdvertisement.VENDING_PIC)) {
                    AD_PIC = (String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_PIC);
                    if (new File(AD_PIC).exists()) {
                        this.myGif = (MyGifView) findViewById(R.id.show_ad_img);
                        this.myGif.setVisibility(0);
                        this.myGif.updateResource(-1, AD_PIC);
                    }
                }
                if (keys.contains(AllAdvertisement.VENDING_DESC)) {
                    this.remindTextView.setText(StringUtils.replace((String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_DESC), "\\n", "\n"));
                }
                if (keys.contains(AllAdvertisement.VENDING_WAY)) {
                    this.vendingWay = (String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_WAY);
                }
            }
        } else {
            HashMap<String, String> VENDING_SELECT_FLAG = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
            if (VENDING_SELECT_FLAG != null) {
                keys = VENDING_SELECT_FLAG.keySet();
                if (keys.contains(AllAdvertisement.VENDING_PIC)) {
                    AD_PIC = (String) VENDING_SELECT_FLAG.get(AllAdvertisement.VENDING_PIC);
                    if (new File(AD_PIC).exists()) {
                        this.myGif = (MyGifView) findViewById(R.id.show_ad_img);
                        this.myGif.setVisibility(0);
                        this.myGif.updateResource(-1, AD_PIC);
                    }
                }
                if (keys.contains(AllAdvertisement.VENDING_DESC)) {
                    this.remindTextView.setText(StringUtils.replace((String) VENDING_SELECT_FLAG.get(AllAdvertisement.VENDING_DESC), "\\n", "\n"));
                }
                if (keys.contains(AllAdvertisement.VENDING_WAY)) {
                    this.vendingWay = (String) VENDING_SELECT_FLAG.get(AllAdvertisement.VENDING_WAY);
                }
                if (StringUtils.isBlank(this.vendingWay) && keys.contains(AllAdvertisement.VENDING_WAY_SET)) {
                    this.vendingWay = (String) VENDING_SELECT_FLAG.get(AllAdvertisement.VENDING_WAY);
                }
            }
        }
        if (StringUtils.isBlank(this.vendingWay)) {
            this.vendingWay = "DONATION";
        }
    }
}
