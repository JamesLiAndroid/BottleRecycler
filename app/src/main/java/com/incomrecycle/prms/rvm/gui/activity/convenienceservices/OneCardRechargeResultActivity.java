package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.content.Intent;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.view.MyGifView;
import java.text.DecimalFormat;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class OneCardRechargeResultActivity extends BaseActivity implements OnLoadCompleteListener {
    private boolean isPlaySounds;
    private TextView moneyInsideCard = null;
    private TextView rechargeFailLayout;
    private MyGifView rechargeImage = null;
    private LinearLayout rechargeSuccessLayout = null;
    private TextView recharged = null;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) OneCardRechargeResultActivity.this.findViewById(R.id.query_onecard_recharge_result_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(OneCardRechargeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            OneCardRechargeResultActivity.this.startActivity(intent);
                            OneCardRechargeResultActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            OneCardRechargeResultActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_query_onecard_recharge_result);
        backgroundToActivity();
        initView();
        ((Button) findViewById(R.id.query_onecard_recharge_result_end)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.ONECARD_RECHARGE_END);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(OneCardRechargeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        OneCardRechargeResultActivity.this.startActivity(intent);
                        OneCardRechargeResultActivity.this.finish();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });
    }

    protected void onStart() {
        super.onStart();
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
            this.soundPool.setOnLoadCompleteListener(this);
        }
        DecimalFormat df = new DecimalFormat("0.00");
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
        TextView rechargeResultText = (TextView) findViewById(R.id.query_onecard_recharge_result_text);
        Intent intent = getIntent();
        if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase(intent.getStringExtra("RESULT"))) {
            if (this.isPlaySounds && this.soundPool != null) {
                this.soundPool.load(this, R.raw.chongzhichenggong, 0);
            }
            Bundle data = intent.getBundleExtra("mData");
            double PREV_BALANCE = data.getDouble("PREV_BALANCE");
            double FINAL_BALANCE = data.getDouble("FINAL_BALANCE");
            this.recharged.setText(df.format(FINAL_BALANCE - PREV_BALANCE) + "");
            this.moneyInsideCard.setText(df.format(FINAL_BALANCE) + "");
            return;
        }
        if (this.isPlaySounds && this.soundPool != null) {
            this.soundPool.load(this, R.raw.chongzhishibai, 0);
        }
        this.rechargeSuccessLayout.setVisibility(View.GONE);
        this.rechargeFailLayout.setVisibility(View.VISIBLE);
        if (this.rechargeImage != null) {
            this.rechargeImage.updateResource(R.drawable.recharge_failed, null);
        }
        rechargeResultText.setText(R.string.recharge_fail);
    }

    public void onStop() {
        super.onStop();
        if (this.soundPool != null) {
            try {
                this.soundPool.release();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            this.soundPool = null;
        }
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void initView() {
        this.recharged = (TextView) findViewById(R.id.query_onecard_recharged);
        this.moneyInsideCard = (TextView) findViewById(R.id.query_onecard_money_insidecard);
        this.rechargeSuccessLayout = (LinearLayout) findViewById(R.id.query_onecard_recharge_result_success_layout);
        this.rechargeFailLayout = (TextView) findViewById(R.id.query_onecard_recharge_result_fail_text);
        this.rechargeImage = (MyGifView) findViewById(R.id.query_onecard_recharge_result_title);
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        soundPlay(sampleId);
    }

    private void soundPlay(int soundId) {
        if (this.soundPool != null) {
            this.soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
}
