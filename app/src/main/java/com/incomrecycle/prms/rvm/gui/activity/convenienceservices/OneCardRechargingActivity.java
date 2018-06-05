package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.MediaInfo;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.view.MyVideoView;
import com.incomrecycle.prms.rvm.gui.entity.CardEntity;
import com.incomrecycle.prms.rvm.util.QuerySortMedia;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

@SuppressLint({"HandlerLeak"})
public class OneCardRechargingActivity extends BaseActivity {
    private int chongZhiZhong;
    private TextView contentText = null;
    Activity context = this;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4:
                    CardEntity.RECHARGE_STATE = 4;
                    Intent intentSuccess = new Intent(OneCardRechargingActivity.this, OneCardRechargeResultActivity.class);
                    intentSuccess.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intentSuccess.putExtra("RESULT", NetworkUtils.NET_STATE_SUCCESS);
                    intentSuccess.putExtra("mData", msg.getData());
                    OneCardRechargingActivity.this.startActivity(intentSuccess);
                    OneCardRechargingActivity.this.finish();
                    return;
                case 5:
                    CardEntity.RECHARGE_STATE = 5;
                    Intent intentFail = new Intent(OneCardRechargingActivity.this, OneCardRechargeResultActivity.class);
                    intentFail.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intentFail.putExtra("RESULT", "FAIL");
                    OneCardRechargingActivity.this.startActivity(intentFail);
                    OneCardRechargingActivity.this.finish();
                    return;
                default:
                    return;
            }
        }
    };
    private int index = 0;
    private boolean isPlaySounds;
    private List<HashMap<String, String>> listVideo = new ArrayList();
    private MyVideoView myVideoView = null;
    Object objWaitOneCard = new Object();
    boolean onecardReadingEnable = false;
    private SoundPool soundPool = null;
    private TextView textView = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        Intent intent = new Intent(OneCardRechargingActivity.this, OneCardRechargeResultActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        OneCardRechargingActivity.this.startActivity(intent);
                        OneCardRechargingActivity.this.finish();
                        return;
                    }
                    ((TextView) OneCardRechargingActivity.this.findViewById(R.id.query_onecard_recharging_time)).setText("" + remainedSeconds);
                }
            };
            OneCardRechargingActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_query_onecard_recharging);
        backgroundToActivity();
        this.contentText = (TextView) findViewById(R.id.query_onecard_recharging_text);
        this.textView = (TextView) findViewById(R.id.query_onecard_recharging_remind_text);
        this.myVideoView = (MyVideoView) findViewById(R.id.recgaringVideo);
        this.myVideoView.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                OneCardRechargingActivity.this.myVideoView.start();
            }
        });
        this.myVideoView.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                OneCardRechargingActivity.this.next();
            }
        });
        this.myVideoView.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                try {
                    OneCardRechargingActivity.this.myVideoView.stopPlayback();
                    OneCardRechargingActivity.this.myVideoView.setVisibility(View.GONE);
                    OneCardRechargingActivity.this.contentText.setVisibility(View.VISIBLE);
                    return true;
                } catch (Exception e) {
                    OneCardRechargingActivity.this.myVideoView.setVisibility(View.GONE);
                    OneCardRechargingActivity.this.contentText.setVisibility(View.VISIBLE);
                    OneCardRechargingActivity.logger.debug("ErrorListener VideoView Error", e);
                    return false;
                }
            }
        });
    }

    protected void onStart() {
        super.onStart();
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
        }
        initMediaInfo();
        decideCountry();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.ONECARDRECHARGE")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
        this.onecardReadingEnable = true;
        SysGlobal.execute(new Runnable() {
            public void run() {
                Message msg;
                int tryTimes = Integer.parseInt(SysConfig.get("TRANSPORTCARD.READER.CHARGE.TRY.TIMES"));
                long lInterval = Long.parseLong(SysConfig.get("TRANSPORTCARD.READ.INTERVAL"));
                int times = Integer.parseInt(SysConfig.get("TRANSPORTCARD.READER.CHARGE.REQ.TIMES"));
                long interval = Long.parseLong(SysConfig.get("TRANSPORTCARD.READER.CHARGE.REQ.INTERVAL"));
                int i = 0;
                while (i < tryTimes) {
                    if (OneCardRechargingActivity.this.onecardReadingEnable) {
                        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                        HashMap<String, Object> hashMap = null;
                        try {
                            hashMap = guiCommonService.execute("GUIOneCardCommonService", "readOneCard", null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (hashMap != null && "success".equals(hashMap.get("RET_CODE"))) {
                            if (hashMap.get("ONECARD_NUM").toString().equals(CardEntity.CARD_NO)) {
                                TimeoutTask.getTimeoutTask().reset(OneCardRechargingActivity.this.timeoutAction);
                                int j = 0;
                                while (j < times) {
                                    if (OneCardRechargingActivity.this.onecardReadingEnable) {
                                        try {
                                            OneCardRechargingActivity.this.textView.post(new Runnable() {
                                                public void run() {
                                                    OneCardRechargingActivity.this.textView.setText(R.string.recharging_ing);
                                                }
                                            });
                                        } catch (Exception e) {
                                        }
                                        try {
                                            HashMap<String, Object> hamp = guiCommonService.execute("GUIOneCardCommonService", "rechargeOneCard", null);
                                            if (hamp != null) {
                                                String RET_CODE = (String) hamp.get("RET_CODE");
                                                if ("CHARGABLE".equalsIgnoreCase(RET_CODE)) {
                                                }
                                                if ("WRITABLE".equalsIgnoreCase(RET_CODE)) {
                                                    TimeoutTask.getTimeoutTask().reset(OneCardRechargingActivity.this.timeoutAction);
                                                    HashMap<String, Object> hsmpTrans = guiCommonService.execute("GUIOneCardCommonService", "writeTrans", hamp);
                                                    if (hsmpTrans != null) {
                                                        RET_CODE = (String) hsmpTrans.get("RET_CODE");
                                                        if ("success".equalsIgnoreCase(RET_CODE)) {
                                                            Double PREV_BALANCE = Double.valueOf(Double.parseDouble((String) hsmpTrans.get("PREV_BALANCE")));
                                                            Double FINAL_BALANCE = Double.valueOf(Double.parseDouble((String) hsmpTrans.get("FINAL_BALANCE")));
                                                            CardEntity.RECHARGE_STATE = 4;
                                                            msg = OneCardRechargingActivity.this.handler.obtainMessage();
                                                            Bundle mData = new Bundle();
                                                            mData.putDouble("PREV_BALANCE", PREV_BALANCE.doubleValue());
                                                            mData.putDouble("FINAL_BALANCE", FINAL_BALANCE.doubleValue());
                                                            msg.what = 4;
                                                            msg.setData(mData);
                                                            OneCardRechargingActivity.this.handler.sendMessage(msg);
                                                            return;
                                                        } else if ("fail".equalsIgnoreCase(RET_CODE)) {
                                                            msg = OneCardRechargingActivity.this.handler.obtainMessage();
                                                            msg.what = 5;
                                                            msg.obj = "INCOMPLETE";
                                                            OneCardRechargingActivity.this.handler.sendMessage(msg);
                                                            return;
                                                        } else if ("not_match".equalsIgnoreCase(RET_CODE)) {
                                                            msg = OneCardRechargingActivity.this.handler.obtainMessage();
                                                            msg.what = 5;
                                                            msg.obj = "NOTMATCH";
                                                            OneCardRechargingActivity.this.handler.sendMessage(msg);
                                                            return;
                                                        }
                                                    }
                                                    msg = OneCardRechargingActivity.this.handler.obtainMessage();
                                                    msg.what = 5;
                                                    msg.obj = "INCOMPLETE";
                                                    OneCardRechargingActivity.this.handler.sendMessage(msg);
                                                    return;
                                                }
                                                if ("LACKMONEY".equalsIgnoreCase(RET_CODE) || "UNBUNDLE".equalsIgnoreCase(RET_CODE) || "BLACKLIST".equalsIgnoreCase(RET_CODE)) {
                                                    msg = OneCardRechargingActivity.this.handler.obtainMessage();
                                                    msg.what = 5;
                                                    msg.obj = RET_CODE;
                                                    OneCardRechargingActivity.this.handler.sendMessage(msg);
                                                    return;
                                                }
                                            }
                                            if (j == times - 1) {
                                                msg = OneCardRechargingActivity.this.handler.obtainMessage();
                                                msg.what = 5;
                                                msg.obj = "INCOMPLETE";
                                                OneCardRechargingActivity.this.handler.sendMessage(msg);
                                                return;
                                            }
                                            synchronized (OneCardRechargingActivity.this.objWaitOneCard) {
                                                try {
                                                    OneCardRechargingActivity.this.objWaitOneCard.wait(interval);
                                                } catch (InterruptedException e2) {
                                                    e2.printStackTrace();
                                                }
                                            }
                                            j++;
                                        } catch (Exception e3) {
                                            e3.printStackTrace();
                                            OneCardRechargingActivity.logger.debug("OneCard Recharge Exception:" + e3);
                                        }
                                    } else {
                                        return;
                                    }
                                }
                            }
                            i = 0;
                            try {
                                OneCardRechargingActivity.this.textView.post(new Runnable() {
                                    public void run() {
                                        OneCardRechargingActivity.this.textView.setText(R.string.validatecardnomatch);
                                    }
                                });
                            } catch (Exception e4) {
                            }
                        }
                        if (i == 1) {
                            try {
                                OneCardRechargingActivity.this.textView.post(new Runnable() {
                                    public void run() {
                                        OneCardRechargingActivity.this.textView.setText(Html.fromHtml(StringUtils.replace(OneCardRechargingActivity.this.getString(R.string.validatecardwarn), "$ONECARD_FLAG$", "<big>" + OneCardRechargingActivity.this.getString(R.string.oneCard_flag) + "</big>")));
                                    }
                                });
                            } catch (Exception e5) {
                            }
                        }
                        if (i == tryTimes - 1) {
                            try {
                                OneCardRechargingActivity.this.textView.post(new Runnable() {
                                    public void run() {
                                        OneCardRechargingActivity.this.textView.setText(R.string.readCardFail);
                                    }
                                });
                                Thread.sleep(3000);
                            } catch (Exception e6) {
                            }
                        }
                        synchronized (OneCardRechargingActivity.this.objWaitOneCard) {
                            try {
                                OneCardRechargingActivity.this.objWaitOneCard.wait(lInterval);
                            } catch (InterruptedException e22) {
                                e22.printStackTrace();
                            }
                        }
                        i++;
                    } else {
                        return;
                    }
                }
                msg = OneCardRechargingActivity.this.handler.obtainMessage();
                msg.what = 5;
                msg.obj = "INCOMPLETE";
                OneCardRechargingActivity.this.handler.sendMessage(msg);
            }
        });
    }

    protected void onResume() {
        super.onResume();
        if (this.isPlaySounds && this.soundPool != null) {
            this.chongZhiZhong = this.soundPool.load(this, R.raw.chongzhizhong, 0);
            this.soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    if (sampleId == OneCardRechargingActivity.this.chongZhiZhong) {
                        soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
            });
        }
    }

    protected void onPause() {
        super.onPause();
        synchronized (this.objWaitOneCard) {
            this.onecardReadingEnable = false;
            this.objWaitOneCard.notify();
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

    private void initMediaInfo() {
        this.listVideo.clear();
        this.listVideo = new QuerySortMedia().QueryLocalMedia(MediaInfo.RECHARING_ACTIVITY, "1");
        if (this.listVideo.size() > 0) {
            play();
            this.myVideoView.setVisibility(0);
            this.contentText.setVisibility(8);
        }
    }

    private void next() {
        this.index++;
        if (this.index >= this.listVideo.size()) {
            this.index = 0;
        }
        play();
    }

    private void play() {
        if (this.listVideo.size() > 0 && this.index < this.listVideo.size()) {
            String path = (String) ((HashMap) this.listVideo.get(this.index)).get("FILE_PATH");
            try {
                if (this.myVideoView != null && !StringUtils.isBlank(path)) {
                    this.myVideoView.setVideoPath(path);
                }
            } catch (Exception e) {
                logger.debug("play() VideoView Error", e);
                e.printStackTrace();
            }
        }
    }

    public void decideCountry() {
        String PICTURE_URL = SysConfig.get("INNER_PAGE_URL");
        String HOME_PICTURE = SysConfig.get("HOME_PAGE_PICTURE");
        if (!StringUtils.isBlank(HOME_PICTURE) && "TRUE".equalsIgnoreCase(HOME_PICTURE)) {
            if (StringUtils.isBlank(PICTURE_URL)) {
                this.contentText.setBackgroundResource(R.drawable.home);
                this.contentText.setText("");
            } else if (new File(PICTURE_URL).isFile()) {
                Bitmap bitmap = BitmapFactory.decodeFile(PICTURE_URL);
                if (bitmap != null) {
                    this.contentText.setBackgroundDrawable(new BitmapDrawable(bitmap));
                    this.contentText.setText("");
                    return;
                }
                this.contentText.setBackgroundResource(R.drawable.home);
                this.contentText.setText("");
            } else {
                this.contentText.setBackgroundResource(R.drawable.home);
                this.contentText.setText("");
            }
        }
    }
}
