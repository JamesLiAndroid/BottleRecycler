package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.content.Intent;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.view.MyGifView;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.text.DecimalFormat;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class RechargeResultActivity extends BaseActivity implements OnLoadCompleteListener {
    private String JSON;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        Intent intent = new Intent();
                        if (!StringUtils.isBlank(RechargeResultActivity.this.JSON) && "BOTTLE".equalsIgnoreCase(ServiceGlobal.getCurrentSession("PRODUCT_TYPE").toString())) {
                            intent.setClass(RechargeResultActivity.this, EnvironmentalPromotionalActivity.class);
                            intent.putExtra("JSON", RechargeResultActivity.this.JSON);
                        } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                            try {
                                intent.setClass(RechargeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        RechargeResultActivity.this.startActivity(intent);
                        RechargeResultActivity.this.finish();
                        return;
                    }
                    ((TextView) RechargeResultActivity.this.findViewById(R.id.onecard_recharge_result_time)).setText("" + remainedSeconds);
                }
            };
            RechargeResultActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_recharge_result);
        backgroundToActivity();
        ((Button) findViewById(R.id.onecard_recharge_result_end)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.REBATE_ONECARD_RECHARGENOW_END);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                Intent intent = new Intent();
                if (!StringUtils.isBlank(RechargeResultActivity.this.JSON) && "BOTTLE".equalsIgnoreCase(ServiceGlobal.getCurrentSession("PRODUCT_TYPE").toString())) {
                    intent.setClass(RechargeResultActivity.this, EnvironmentalPromotionalActivity.class);
                    intent.putExtra("JSON", RechargeResultActivity.this.JSON);
                } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        intent.setClass(RechargeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                RechargeResultActivity.this.startActivity(intent);
                RechargeResultActivity.this.finish();
            }
        });
    }

    protected void onStart() {
        super.onStart();
        TextView recharged = (TextView) findViewById(R.id.recharged);
        TextView moneyInsideCard = (TextView) findViewById(R.id.money_insidecard);
        TextView card_number = (TextView) findViewById(R.id.card_number);
        View recharge_info_view = findViewById(R.id.onecard_recharge_result_success_layout);
        TextView recharge_fail_text = (TextView) findViewById(R.id.onecard_recharge_result_fail_text);
        MyGifView rechargeResultImg = (MyGifView) findViewById(R.id.onecard_recharge_result_title);
        TextView rechargeResultText = (TextView) findViewById(R.id.onecard_recharge_result_text);
        boolean isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
            this.soundPool.setOnLoadCompleteListener(this);
        }
        DecimalFormat df = new DecimalFormat("0.00");
        Intent intent = getIntent();
        this.JSON = intent.getStringExtra("JSON");
        if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase(intent.getStringExtra("RESULT"))) {
            if (isPlaySounds && this.soundPool != null) {
                this.soundPool.load(this, R.raw.chongzhichenggong, 0);
            }
            Bundle data = intent.getBundleExtra("mData");
            double PREV_BALANCE = data.getDouble("PREV_BALANCE");
            double FINAL_BALANCE = data.getDouble("FINAL_BALANCE");
            String CARD_NUMBER = data.getString("CARD_NUMBER");
            recharged.setText(df.format(FINAL_BALANCE - PREV_BALANCE) + "");
            moneyInsideCard.setText(df.format(FINAL_BALANCE) + "");
            card_number.setText(CARD_NUMBER);
        } else {
            if (isPlaySounds && this.soundPool != null) {
                this.soundPool.load(this, R.raw.chongzhishibai, 0);
            }
            recharge_info_view.setVisibility(View.GONE);
            recharge_fail_text.setVisibility(View.VISIBLE);
            rechargeResultImg.updateResource(R.drawable.recharge_failed, null);
            rechargeResultText.setText(R.string.recharge_fail);
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
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
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        try {
            HashMap<String, Object> hsmpParam = new HashMap();
            hsmpParam.put("TEXT", getString(R.string.welcome));
            guiCommonService.execute("GUIRecycleCommonService", "showOnDigitalScreen", hsmpParam);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
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
