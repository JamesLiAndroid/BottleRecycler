package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
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

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class AlipayShowActivity extends BaseActivity {
    private String JSON;
    private String NETWORK;
    private View alipayRebateInfoLayout = null;
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
                        Intent intent = new Intent(AlipayShowActivity.this.getApplicationContext(), SelectRecycleActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        AlipayShowActivity.this.startActivity(intent);
                        AlipayShowActivity.this.finish();
                        return;
                    }
                    ((TextView) AlipayShowActivity.this.findViewById(R.id.alipay_rebate_time)).setText("" + remainedSeconds);
                }
            };
            AlipayShowActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_alipay_show);
        String PRODUCT_TYPE = (String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE");
        TextView textView = (TextView) findViewById(R.id.alipay_rebate_remind_text1);
        ImageView imageView = (ImageView) findViewById(R.id.show_img);
        this.NETWORK = NetworkStateMgr.getMgr().getNetworkState();
        if (NetworkStateMgr.NETWORK_ERROR.equals(this.NETWORK)) {
            textView.setText(R.string.commit_rebateinfo_fail_text_bdj);
            imageView.setBackgroundResource(R.drawable.not_mesh);
        }
        if (NetworkStateMgr.NETWORK_FAILED.equals(this.NETWORK)) {
            textView.setText(R.string.commit_rebateinfo_fail_text_bdj);
            imageView.setBackgroundResource(R.drawable.not_mesh);
        }
        ((Button) findViewById(R.id.alipay_rebate_end)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Runnable() {
                    public void run() {
                        if (!AlipayShowActivity.this.isClick) {
                            AlipayShowActivity.this.isClick = true;
                            AlipayShowActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{AlipayShowActivity.this.getBaseActivity(), Integer.valueOf(2), "ALIPAY"});
                            AlipayShowActivity.this.finish();
                        }
                    }
                });
            }
        });
    }

    protected void onStart() {
        super.onStart();
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
