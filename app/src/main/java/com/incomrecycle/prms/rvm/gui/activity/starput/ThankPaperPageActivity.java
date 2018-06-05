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
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import java.text.DecimalFormat;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class ThankPaperPageActivity extends BaseActivity {
    String VENDING_WAY = null;
    private boolean isPlaySounds;
    private int juanZengWanCheng;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        ThankPaperPageActivity.this.stopPhase();
                    } else {
                        ((TextView) ThankPaperPageActivity.this.findViewById(R.id.thank_page_for_paper_time)).setText("" + remainedSeconds);
                    }
                }
            };
            ThankPaperPageActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

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
        findViewById(R.id.thank_page_for_paper_layout).setBackgroundDrawable(null);
    }

    public void onStart() {
        super.onStart();
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
        }
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        DecimalFormat df = new DecimalFormat("0.00");
        HashMap<String, Object> HsmpShowInfo = new HashMap();
        HsmpShowInfo.put("TITLE", "paper");
        HsmpShowInfo.put("COUNT", "0");
        HsmpShowInfo.put("AMOUNT", "0");
        try {
            HashMap<String, Object> hsmpResult = guiCommonService.execute("GUIQueryCommonService", "recycledPaperSummary", null);
            if (hsmpResult != null) {
                HashMap<String, String> HsmpRecyclePaper = (HashMap) hsmpResult.get("RECYCLED_PAPER_SUMMARY");
                if (HsmpRecyclePaper != null && HsmpRecyclePaper.size() > 0) {
                    double paperCount = Double.parseDouble((String) HsmpRecyclePaper.get("PAPER_WEIGH"));
                    double amount = Double.parseDouble((String) HsmpShowInfo.get("AMOUNT")) + Double.parseDouble((String) HsmpRecyclePaper.get("PAPER_PRICE"));
                    HsmpShowInfo.put("COUNT", Double.toString(Double.parseDouble((String) HsmpShowInfo.get("COUNT")) + paperCount));
                    HsmpShowInfo.put("AMOUNT", Double.toString(amount));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        double Count = Double.parseDouble((String) HsmpShowInfo.get("COUNT"));
        getvendingway();
        double Amount = 0.0d;
        if (!"DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
            Amount = (Double.parseDouble((String) HsmpShowInfo.get("AMOUNT")) / 100.0d) * ((double) Integer.parseInt(SysConfig.get("LOCAL_EXCHANGE_RATE")));
        }
        ((TextView) findViewById(R.id.paper_count)).setText("" + df.format(Count));
        ((TextView) findViewById(R.id.paper_total_acount)).setText("" + df.format(Amount));
        int timeout = Integer.valueOf(SysConfig.get("RVM.TIMEOUT.THANKS")).intValue();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, timeout, false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onResume() {
        super.onResume();
        getvendingway();
        if (this.isPlaySounds && "DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
            this.juanZengWanCheng = this.soundPool.load(this, R.raw.juanzengwancheng, 0);
            this.soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    if (sampleId == ThankPaperPageActivity.this.juanZengWanCheng) {
                        soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
            });
        }
    }

    private void getvendingway() {
        try {
            HashMap<String, Object> hsmpResultForVendingWay = CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "queryVendingWay", null);
            if (hsmpResultForVendingWay != null) {
                this.VENDING_WAY = (String) hsmpResultForVendingWay.get(AllAdvertisement.VENDING_WAY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_thank_paper);
        backgroundToActivity();
        ((Button) findViewById(R.id.thank_page_for_paper_end)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.THANK_THROWPAPER_END);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                ThankPaperPageActivity.this.stopPhase();
            }
        });
    }

    public void updateLanguage() {
    }

    private void stopPhase() {
        if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
            try {
                Intent intent = new Intent(getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void doEvent(HashMap hsmpEvent) {
        if ("CMD".equalsIgnoreCase((String) hsmpEvent.get("EVENT"))) {
            if (!"REQUEST_RECYCLE".equalsIgnoreCase((String) hsmpEvent.get("CMD"))) {
            }
        }
    }
}
