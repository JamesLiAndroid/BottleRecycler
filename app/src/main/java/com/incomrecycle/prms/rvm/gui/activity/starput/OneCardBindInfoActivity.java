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
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class OneCardBindInfoActivity extends BaseActivity {
    private String JSON = null;
    private String PRODUCT_TYPE = ((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"));
    private int duanXin;
    private GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
    private boolean hasPut;
    private boolean isPlaySounds;
    private double recycleAmount = 0.0d;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        if (OneCardBindInfoActivity.this.hasPut) {
                            OneCardBindInfoActivity.this.uploadData();
                        }
                        Intent intent = new Intent();
                        if ("BOTTLE".equalsIgnoreCase(OneCardBindInfoActivity.this.PRODUCT_TYPE)) {
                            intent.setClass(OneCardBindInfoActivity.this, ThankBottlePageActivity.class);
                            intent.putExtra("JSON", OneCardBindInfoActivity.this.JSON);
                        } else {
                            intent.setClass(OneCardBindInfoActivity.this, ThankPaperPageActivity.class);
                            intent.putExtra("JSON", OneCardBindInfoActivity.this.JSON);
                        }
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        OneCardBindInfoActivity.this.startActivity(intent);
                        OneCardBindInfoActivity.this.finish();
                        return;
                    }
                    ((TextView) OneCardBindInfoActivity.this.findViewById(R.id.bind_info_time)).setText("" + remainedSeconds);
                }
            };
            OneCardBindInfoActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_bindcommit_info);
        backgroundToActivity();
        initView();
    }

    protected void onStart() {
        super.onStart();
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(10, 3, 0);
        }
        this.JSON = getIntent().getStringExtra("JSON");
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onResume() {
        super.onResume();
        if (this.isPlaySounds && this.soundPool != null) {
            this.duanXin = this.soundPool.load(this, R.raw.duanxin, 0);
            this.soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    if (sampleId == OneCardBindInfoActivity.this.duanXin) {
                        soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
            });
        }
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
        HashMap<String, Object> hsmpResult = null;
        try {
            hsmpResult = this.guiCommonService.execute("GUIQueryCommonService", "hasRecycled", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.hasPut = false;
        if (hsmpResult != null && "TRUE".equalsIgnoreCase((String) hsmpResult.get("RESULT"))) {
            this.hasPut = true;
        }
        ((Button) findViewById(R.id.bind_info_confirm)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (OneCardBindInfoActivity.this.hasPut) {
                    OneCardBindInfoActivity.this.uploadData();
                    Intent intent = new Intent();
                    if ("BOTTLE".equalsIgnoreCase(OneCardBindInfoActivity.this.PRODUCT_TYPE)) {
                        intent.setClass(OneCardBindInfoActivity.this, ThankBottlePageActivity.class);
                        intent.putExtra("JSON", OneCardBindInfoActivity.this.JSON);
                    } else {
                        intent.setClass(OneCardBindInfoActivity.this, ThankPaperPageActivity.class);
                        intent.putExtra("JSON", OneCardBindInfoActivity.this.JSON);
                    }
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    OneCardBindInfoActivity.this.startActivity(intent);
                } else {
                    OneCardBindInfoActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{OneCardBindInfoActivity.this.getBaseActivity(), Integer.valueOf(2), "TRANSPORTCARD"});
                }
                OneCardBindInfoActivity.this.finish();
            }
        });
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    private void uploadData() {
        try {
            this.guiCommonService.execute("GUIOneCardCommonService", "recycleEnd", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
