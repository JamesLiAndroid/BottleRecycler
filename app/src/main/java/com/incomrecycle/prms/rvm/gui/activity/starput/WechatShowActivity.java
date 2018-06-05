package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.NetworkStateMgr;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.util.HashMap;

public class WechatShowActivity extends BaseActivity {
    private String JSON;
    private String NETWORK;
    private TextView balanceOfINCOM = null;
    private int cardStatus;
    private int fanLiWanCheng;
    private boolean isClick = false;
    private boolean isPlaySounds;
    private TextView rebateThis = null;
    private TextView recharge_warningtext = null;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        WechatShowActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{WechatShowActivity.this.getBaseActivity(), Integer.valueOf(2), "WECHAT"});
                        WechatShowActivity.this.finish();
                        return;
                    }
                    ((TextView) WechatShowActivity.this.findViewById(R.id.wechat_rebate_time)).setText("" + remainedSeconds);
                }
            };
            WechatShowActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private View wechatRebateInfoLayout = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_wechat_show);
        backgroundToActivity();
        String PRODUCT_TYPE = (String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE");
        TextView textView = (TextView) findViewById(R.id.wechat_rebate_remind_text1);
        this.NETWORK = NetworkStateMgr.getMgr().getNetworkState();
        if (NetworkStateMgr.NETWORK_ERROR.equals(this.NETWORK)) {
            textView.setText(R.string.commit_rebateinfo_fail_text1);
        }
        if (NetworkStateMgr.NETWORK_FAILED.equals(this.NETWORK)) {
            textView.setText(R.string.commit_rebateinfo_fail_text1);
        }
        ((Button) findViewById(R.id.wechat_rebate_end)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Runnable() {
                    public void run() {
                        if (!WechatShowActivity.this.isClick) {
                            WechatShowActivity.this.isClick = true;
                            WechatShowActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{WechatShowActivity.this.getBaseActivity(), Integer.valueOf(2), "WECHAT"});
                            WechatShowActivity.this.finish();
                        }
                    }
                });
            }
        });
    }

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD1")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void onStop() {
        super.onStop();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void finish() {
        super.finish();
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }
}
