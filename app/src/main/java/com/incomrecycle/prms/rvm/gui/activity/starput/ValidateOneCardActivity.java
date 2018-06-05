package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Html;
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
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.gui.activity.view.MyGifView;
import com.incomrecycle.prms.rvm.gui.entity.CardEntity;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

@SuppressLint({"HandlerLeak"})
public class ValidateOneCardActivity extends BaseActivity implements OnLoadCompleteListener {
    private static final int NETWORKCONNECTFAIL = -1;
    private static final int ONECARD = 0;
    private static final int ONECARDINFO = 1;
    private String PRODUCT_TYPE = ((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"));
    private String VENDING_HTML;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Intent intent;
            switch (msg.what) {
                case -1:
                    if ("PAPER".equals(ValidateOneCardActivity.this.PRODUCT_TYPE)) {
                        intent = new Intent(ValidateOneCardActivity.this, SelectRecycleActivity.class);
                        intent.putExtra("RECYCLE", "RECYCLEPAPER");
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        ValidateOneCardActivity.this.startActivity(intent);
                        ValidateOneCardActivity.this.finish();
                        return;
                    }
                    intent = new Intent(ValidateOneCardActivity.this, SelectRecycleActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    ValidateOneCardActivity.this.startActivity(intent);
                    ValidateOneCardActivity.this.finish();
                    return;
                case 0:
                    SysGlobal.execute(new ValidateCardInfoThread(ValidateOneCardActivity.this.handler));
                    return;
                case 1:
                    if (!CardEntity.isValid) {
                        SysGlobal.execute(new Runnable() {
                            public void run() {
                                ValidateOneCardActivity.this.executeGUIAction(false, new GUIActionGotoServiceProcess(), new Object[]{ValidateOneCardActivity.this.getBaseActivity(), Integer.valueOf(2), "TRANSPORTCARD"});
                                ValidateOneCardActivity.this.finish();
                            }
                        });
                        return;
                    } else if (CardEntity.CARD_STATUS == 2 || CardEntity.CARD_STATUS == 1) {
                        SysGlobal.execute(new Runnable() {
                            public void run() {
                                HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
                                HashMap<String, Object> VENDING_FLAG = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
                                Intent intent;
                                if (TRANSMIT_ADV != null) {
                                    HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
                                    if (HOMEPAGE_LEFT == null || StringUtils.isBlank((String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_PIC))) {
                                        ValidateOneCardActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{ValidateOneCardActivity.this.getBaseActivity(), Integer.valueOf(2), "TRANSPORTCARD"});
                                        ValidateOneCardActivity.this.finish();
                                        return;
                                    }
                                    intent = new Intent(ValidateOneCardActivity.this, ActivityAdActivity.class);
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    ValidateOneCardActivity.this.startActivity(intent);
                                    ValidateOneCardActivity.this.finish();
                                } else if (VENDING_FLAG == null || StringUtils.isBlank((String) VENDING_FLAG.get(AllAdvertisement.VENDING_PIC))) {
                                    ValidateOneCardActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{ValidateOneCardActivity.this.getBaseActivity(), Integer.valueOf(2), "TRANSPORTCARD"});
                                    ValidateOneCardActivity.this.finish();
                                } else {
                                    intent = new Intent(ValidateOneCardActivity.this, ActivityAdActivity.class);
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    ValidateOneCardActivity.this.startActivity(intent);
                                    ValidateOneCardActivity.this.finish();
                                }
                            }
                        });
                        return;
                    } else if (CardEntity.CARD_STATUS == -1) {
                        if (ValidateOneCardActivity.this.validatecardText != null) {
                            ValidateOneCardActivity.this.validatecardText.setText(ValidateOneCardActivity.this.getString(R.string.toast_card_unuse));
                        }
                        SystemClock.sleep(3000);
                        if ("PAPER".equals(ValidateOneCardActivity.this.PRODUCT_TYPE)) {
                            intent = new Intent(ValidateOneCardActivity.this, SelectRecycleActivity.class);
                            intent.putExtra("RECYCLE", "RECYCLEPAPER");
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            ValidateOneCardActivity.this.startActivity(intent);
                            ValidateOneCardActivity.this.finish();
                            return;
                        }
                        intent = new Intent(ValidateOneCardActivity.this, SelectRecycleActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        ValidateOneCardActivity.this.startActivity(intent);
                        ValidateOneCardActivity.this.finish();
                        return;
                    } else if (CardEntity.CARD_STATUS == -2) {
                        intent = new Intent(ValidateOneCardActivity.this, BindCardActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        ValidateOneCardActivity.this.startActivity(intent);
                        ValidateOneCardActivity.this.finish();
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }
    };
    private MyGifView imageView1 = null;
    private Intent intent;
    private boolean isPlaySounds;
    Object objWaitOneCard = new Object();
    boolean onecardReadingEnable = false;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) ValidateOneCardActivity.this.findViewById(R.id.shua_ka_time)).setText("" + remainedSeconds);
                    } else if ("PAPER".equals(ValidateOneCardActivity.this.PRODUCT_TYPE)) {
                        intent = new Intent(ValidateOneCardActivity.this, SelectRecycleActivity.class);
                        intent.putExtra("RECYCLE", "RECYCLEPAPER");
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        ValidateOneCardActivity.this.startActivity(intent);
                        ValidateOneCardActivity.this.finish();
                    } else {
                        intent = new Intent(ValidateOneCardActivity.this, SelectRecycleActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        ValidateOneCardActivity.this.startActivity(intent);
                        ValidateOneCardActivity.this.finish();
                    }
                }
            };
            ValidateOneCardActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    Runnable updateThread = new Runnable() {
        public void run() {
            if (ValidateOneCardActivity.this.validatecardText != null) {
                ValidateOneCardActivity.this.validatecardText.setText(R.string.validatecardfail);
            }
        }
    };
    private TextView validatecardText;

    public class ValidateCardInfoThread implements Runnable {
        Handler handler = null;

        public ValidateCardInfoThread(Handler handler) {
            this.handler = handler;
        }

        public void run() {
            GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
            HashMap<String, Object> hm = new HashMap();
            hm.put("ONECARD_NUM", CardEntity.CARD_NO);
            Message mg = this.handler.obtainMessage();
            CardEntity.isValid = false;
            try {
                HashMap<String, Object> hsmpResult = guiCommonService.execute("GUIOneCardCommonService", "verifyOneCard", hm);
                if (hsmpResult != null) {
                    mg.what = 1;
                    CardEntity.isValid = true;
                    CardEntity.CARD_STATUS = Integer.parseInt((String) hsmpResult.get("CARD_STATUS"));
                    CardEntity.IS_RECHANGE = Integer.parseInt((String) hsmpResult.get("IS_RECHANGE"));
                    CardEntity.RECHARGED = Double.parseDouble((String) hsmpResult.get("RECHARGE"));
                    CardEntity.INCOM_AMOUNT = Double.parseDouble((String) hsmpResult.get("INCOM_AMOUNT"));
                } else {
                    if (ValidateOneCardActivity.this.isPlaySounds && ValidateOneCardActivity.this.soundPool != null) {
                        ValidateOneCardActivity.this.soundPool.load(ValidateOneCardActivity.this, R.raw.wangluozhongduan, 0);
                    }
                    try {
                        ValidateOneCardActivity.this.runOnUiThread(ValidateOneCardActivity.this.updateThread);
                        Thread.sleep(3000);
                        mg.what = -1;
                    } catch (Exception e) {
                    }
                }
                this.handler.sendMessage(mg);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    protected void onStop() {
        super.onStop();
        if (this.soundPool != null) {
            try {
                this.soundPool.release();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            this.soundPool = null;
        }
        if (this.imageView1 != null) {
            this.imageView1.setBackgroundDrawable(null);
            this.imageView1 = null;
        }
    }

    protected void onStart() {
        super.onStart();
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
            this.soundPool.setOnLoadCompleteListener(this);
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.VALIDATE")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
        this.onecardReadingEnable = true;
        CardEntity.Reset();
        SysGlobal.execute(new Runnable() {
            public void run() {
                GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                int tryTimes = Integer.parseInt(SysConfig.get("TRANSPORTCARD.READ.TRY.TIMES"));
                long lInterval = Long.parseLong(SysConfig.get("TRANSPORTCARD.READ.INTERVAL"));
                int i = 0;
                while (i < tryTimes && ValidateOneCardActivity.this.onecardReadingEnable) {
                    try {
                        HashMap<String, Object> hashMap = guiCommonService.execute("GUIOneCardCommonService", "readOneCard", null);
                        if (hashMap == null || !"success".equalsIgnoreCase((String) hashMap.get("RET_CODE"))) {
                            if (i == 4) {
                                try {
                                    ValidateOneCardActivity.this.validatecardText.post(new Runnable() {
                                        public void run() {
                                            ValidateOneCardActivity.this.validatecardText.setText(Html.fromHtml(StringUtils.replace(ValidateOneCardActivity.this.getString(R.string.validatecardwarn), "$ONECARD_FLAG$", "<big>" + ValidateOneCardActivity.this.getString(R.string.oneCard_flag) + "</big>")));
                                        }
                                    });
                                } catch (Exception e) {
                                }
                            }
                            if (i == tryTimes - 1) {
                                try {
                                    ValidateOneCardActivity.this.validatecardText.post(new Runnable() {
                                        public void run() {
                                            ValidateOneCardActivity.this.validatecardText.setText(Html.fromHtml(StringUtils.replace(ValidateOneCardActivity.this.getString(R.string.readCardFail), "$ONECARD_FLAG$", "<big>" + ValidateOneCardActivity.this.getString(R.string.oneCard_flag) + "</big>")));
                                        }
                                    });
                                } catch (Exception e2) {
                                }
                                if (ValidateOneCardActivity.this.isPlaySounds && ValidateOneCardActivity.this.soundPool != null) {
                                    ValidateOneCardActivity.this.soundPool.load(ValidateOneCardActivity.this, R.raw.dukashibai, 0);
                                }
                            }
                            synchronized (ValidateOneCardActivity.this.objWaitOneCard) {
                                try {
                                    ValidateOneCardActivity.this.objWaitOneCard.wait(lInterval);
                                } catch (InterruptedException e3) {
                                    e3.printStackTrace();
                                }
                            }
                            i++;
                        } else {
                            Message msg = ValidateOneCardActivity.this.handler.obtainMessage();
                            CardEntity.CARD_NO = hashMap.get("ONECARD_NUM").toString();
                            msg.what = 0;
                            ValidateOneCardActivity.this.handler.sendMessage(msg);
                            return;
                        }
                    } catch (Exception e4) {
                        e4.printStackTrace();
                    }
                }
            }
        });
        this.imageView1 = (MyGifView) findViewById(R.id.validatecard_imageview);
        this.imageView1.updateResource(R.drawable.validatecard, null);
    }

    protected void onResume() {
        super.onResume();
        if (this.isPlaySounds && this.soundPool != null) {
            this.soundPool.load(this, R.raw.duka, 0);
        }
    }

    protected void onPause() {
        super.onPause();
        synchronized (this.objWaitOneCard) {
            this.onecardReadingEnable = false;
            this.objWaitOneCard.notify();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_validatecard);
        backgroundToActivity();
        this.intent = getIntent();
        this.VENDING_HTML = this.intent.getStringExtra("advert");
        ((Button) findViewById(R.id.shua_ka_return_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if ("PAPER".equals(ValidateOneCardActivity.this.PRODUCT_TYPE)) {
                    Intent intent = new Intent(ValidateOneCardActivity.this, SelectRecycleActivity.class);
                    intent.putExtra("RECYCLE", "RECYCLEPAPER");
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    ValidateOneCardActivity.this.startActivity(intent);
                    ValidateOneCardActivity.this.finish();
                    return;
                }
                intent = new Intent(ValidateOneCardActivity.this, SelectRecycleActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                ValidateOneCardActivity.this.startActivity(intent);
                ValidateOneCardActivity.this.finish();
            }
        });
        this.validatecardText = (TextView) findViewById(R.id.shua_ka_text);
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
