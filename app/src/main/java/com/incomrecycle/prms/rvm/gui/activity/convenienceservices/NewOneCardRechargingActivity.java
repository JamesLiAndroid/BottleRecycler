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
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.view.MyVideoView;
import com.incomrecycle.prms.rvm.gui.entity.CardEntity;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardCommEntity.ErrorCode;
import com.incomrecycle.prms.rvm.util.QuerySortMedia;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

@SuppressLint({"HandlerLeak"})
public class NewOneCardRechargingActivity extends BaseActivity {
    private static final int READ_CARD_SUEECSS = 1;
    private TextView contentText = null;
    Activity context = this;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            try {
                                HashMap hsmpParam = new HashMap();
                                hsmpParam.put("RECHARGED", Double.valueOf(CardEntity.RECHARGED));
                                HashMap<String, Object> hamp = CommonServiceHelper.getGUICommonService().execute("GUITrafficCardCommonService", "rechargeOneCard", hsmpParam);
                                if (hamp != null) {
                                    String RET_CODE = (String) hamp.get("RET_CODE");
                                    Message msg;
                                    if ("WRITABLE".equalsIgnoreCase(RET_CODE)) {
                                        TimeoutTask.getTimeoutTask().reset(NewOneCardRechargingActivity.this.timeoutAction);
                                        HashMap<String, Object> hsmpTrans = CommonServiceHelper.getGUICommonService().execute("GUITrafficCardCommonService", "writeTrans", hamp);
                                        if (hsmpTrans != null) {
                                            RET_CODE = (String) hsmpTrans.get("RET_CODE");
                                            if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase(RET_CODE)) {
                                                Double BALANCE = Double.valueOf(Double.parseDouble(hsmpTrans.get("BALANCE") + ""));
                                                Double CHARGE_AMOUNT = Double.valueOf(Double.parseDouble(hsmpTrans.get("CHARGE_AMOUNT") + ""));
                                                CardEntity.RECHARGE_STATE = 4;
                                                ServiceGlobal.setCurrentSession("ONECARD_MONEY", String.valueOf(Double.valueOf(CHARGE_AMOUNT.doubleValue() / 100.0d)));
                                                msg = NewOneCardRechargingActivity.this.handler.obtainMessage();
                                                Bundle mData = new Bundle();
                                                mData.putDouble("BALANCE", BALANCE.doubleValue());
                                                mData.putDouble("CHARGE_AMOUNT", CHARGE_AMOUNT.doubleValue());
                                                msg.what = 4;
                                                msg.setData(mData);
                                                NewOneCardRechargingActivity.this.handler.sendMessage(msg);
                                                return;
                                            }
                                            String errorType = ErrorCode.MODEL_RESPONSECODE;
                                            String str = (String) hsmpTrans.get(ErrorCode.MODEL_RESPONSECODE);
                                            if (StringUtils.isBlank(str)) {
                                                errorType = "HOST_RESPONSECODE";
                                                str = (String) hsmpTrans.get("HOST_RESPONSECODE");
                                            }
                                            if (StringUtils.isBlank(str)) {
                                                errorType = "SERVER_RESPONSECODE";
                                                str = (String) hsmpTrans.get("SERVER_RESPONSECODE");
                                            }
                                            if (StringUtils.isBlank(str)) {
                                                errorType = "RVM_RESPONSECODE";
                                                str = (String) hsmpTrans.get("RVM_RESPONSECODE");
                                            }
                                            if (StringUtils.isBlank(str)) {
                                                errorType = "RVM_RESPONSECODE";
                                                str = NetworkUtils.NET_STATE_UNKNOWN;
                                            }
                                            String reason = null;
                                            if ("NOT_ENOUGH".equalsIgnoreCase(RET_CODE)) {
                                                reason = RET_CODE;
                                            }
                                            Intent intent = new Intent(NewOneCardRechargingActivity.this.getBaseContext(), NewOneCardRechargeResultWarnActivity.class);
                                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            intent.putExtra("errorType", errorType);
                                            intent.putExtra("errorCode", str);
                                            if (!StringUtils.isBlank(reason)) {
                                                intent.putExtra("reason", reason);
                                            }
                                            NewOneCardRechargingActivity.this.startActivity(intent);
                                            NewOneCardRechargingActivity.this.finish();
                                            return;
                                        }
                                        msg = NewOneCardRechargingActivity.this.handler.obtainMessage();
                                        msg.what = 5;
                                        msg.obj = "INCOMPLETE";
                                        NewOneCardRechargingActivity.this.handler.sendMessage(msg);
                                    } else if ("LACKMONEY".equalsIgnoreCase(RET_CODE) || "UNBUNDLE".equalsIgnoreCase(RET_CODE) || "BLACKLIST".equalsIgnoreCase(RET_CODE) || NetworkUtils.NET_STATE_FAILED.equalsIgnoreCase(RET_CODE)) {
                                        msg = NewOneCardRechargingActivity.this.handler.obtainMessage();
                                        msg.what = 5;
                                        msg.obj = RET_CODE;
                                        NewOneCardRechargingActivity.this.handler.sendMessage(msg);
                                    }
                                }
                            } catch (Exception e) {
                            }
                        }
                    });
                    return;
                case 4:
                    CardEntity.RECHARGE_STATE = 4;
                    Intent intentSuccess = new Intent(NewOneCardRechargingActivity.this, NewOneCardRechargeResultActivity.class);
                    intentSuccess.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intentSuccess.putExtra("RESULT", NetworkUtils.NET_STATE_SUCCESS);
                    intentSuccess.putExtra("mData", msg.getData());
                    NewOneCardRechargingActivity.this.startActivity(intentSuccess);
                    NewOneCardRechargingActivity.this.finish();
                    return;
                case 5:
                    CardEntity.RECHARGE_STATE = 5;
                    Intent intentFail = new Intent(NewOneCardRechargingActivity.this, NewOneCardRechargeResultWarnActivity.class);
                    intentFail.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intentFail.putExtra("RESULT", "FAIL");
                    NewOneCardRechargingActivity.this.startActivity(intentFail);
                    NewOneCardRechargingActivity.this.finish();
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
                        Intent intent = new Intent(NewOneCardRechargingActivity.this, OneCardRechargeResultActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        NewOneCardRechargingActivity.this.startActivity(intent);
                        NewOneCardRechargingActivity.this.finish();
                        return;
                    }
                    ((TextView) NewOneCardRechargingActivity.this.findViewById(R.id.query_onecard_recharging_time)).setText("" + remainedSeconds);
                }
            };
            NewOneCardRechargingActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
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
                NewOneCardRechargingActivity.this.myVideoView.start();
            }
        });
        this.myVideoView.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                NewOneCardRechargingActivity.this.next();
            }
        });
        this.myVideoView.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                try {
                    NewOneCardRechargingActivity.this.myVideoView.stopPlayback();
                    NewOneCardRechargingActivity.this.myVideoView.setVisibility(View.GONE);
                    NewOneCardRechargingActivity.this.contentText.setVisibility(View.VISIBLE);
                    return true;
                } catch (Exception e) {
                    NewOneCardRechargingActivity.this.myVideoView.setVisibility(View.GONE);
                    NewOneCardRechargingActivity.this.contentText.setVisibility(View.VISIBLE);
                    NewOneCardRechargingActivity.logger.debug("ErrorListener VideoView Error", e);
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
                int i = 0;
                while (i < tryTimes) {
                    if (NewOneCardRechargingActivity.this.onecardReadingEnable) {
                        try {
                            HashMap<String, Object> hashMap = CommonServiceHelper.getGUICommonService().execute("GUITrafficCardCommonService", "readOneCard", null);
                            if (hashMap != null && "success".equalsIgnoreCase((String) hashMap.get("RET_CODE"))) {
                                TimeoutTask.getTimeoutTask().reset(NewOneCardRechargingActivity.this.timeoutAction);
                                try {
                                    NewOneCardRechargingActivity.this.textView.post(new Runnable() {
                                        public void run() {
                                            NewOneCardRechargingActivity.this.textView.setText(R.string.recharging_ing);
                                        }
                                    });
                                } catch (Exception e) {
                                }
                                if (hashMap.get("ONECARD_NUM").toString().equals(CardEntity.CARD_NO)) {
                                    msg = NewOneCardRechargingActivity.this.handler.obtainMessage();
                                    msg.what = 1;
                                    NewOneCardRechargingActivity.this.handler.sendMessage(msg);
                                    return;
                                }
                                i = 0;
                                try {
                                    NewOneCardRechargingActivity.this.textView.post(new Runnable() {
                                        public void run() {
                                            NewOneCardRechargingActivity.this.textView.setText(R.string.validatecardnomatch);
                                        }
                                    });
                                } catch (Exception e2) {
                                }
                            }
                            if (i == 1) {
                                try {
                                    NewOneCardRechargingActivity.this.textView.post(new Runnable() {
                                        public void run() {
                                            NewOneCardRechargingActivity.this.textView.setText(Html.fromHtml(StringUtils.replace(NewOneCardRechargingActivity.this.getString(R.string.validatecardwarn),
                                                    "$ONECARD_FLAG$", "<big>" + NewOneCardRechargingActivity.this.getString(R.string.oneCard_flag) + "</big>")));
                                        }
                                    });
                                } catch (Exception e3) {
                                }
                            }
                            if (i == tryTimes - 1) {
                                try {
                                    NewOneCardRechargingActivity.this.textView.post(new Runnable() {
                                        public void run() {
                                            NewOneCardRechargingActivity.this.textView.setText(Html.fromHtml(StringUtils.replace(NewOneCardRechargingActivity.this.getString(R.string.readCardFail),
                                                    "$ONECARD_FLAG$", "<big>" + NewOneCardRechargingActivity.this.getString(R.string.oneCard_flag) + "</big>")));
                                        }
                                    });
                                    Thread.sleep(3000);
                                } catch (Exception e4) {
                                }
                            }
                        } catch (Exception e5) {
                            e5.printStackTrace();
                            NewOneCardRechargingActivity.logger.debug("OneCard Recharge Exception:" + e5);
                        }
                        synchronized (NewOneCardRechargingActivity.this.objWaitOneCard) {
                            try {
                                NewOneCardRechargingActivity.this.objWaitOneCard.wait(lInterval);
                            } catch (InterruptedException e6) {
                                e6.printStackTrace();
                            }
                        }
                        i++;
                    } else {
                        return;
                    }
                }
                msg = NewOneCardRechargingActivity.this.handler.obtainMessage();
                msg.what = 5;
                msg.obj = "INCOMPLETE";
                NewOneCardRechargingActivity.this.handler.sendMessage(msg);
            }
        });
    }

    protected void onResume() {
        super.onResume();
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
    }

    public void finish() {
        super.finish();
        ((TextView) findViewById(R.id.query_onecard_recharging_time)).setText("");
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
            this.myVideoView.setVisibility(View.VISIBLE);
            this.contentText.setVisibility(View.GONE);
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
