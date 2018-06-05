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
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.NetworkUtils;
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
import com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardCommEntity.ErrorCode;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

@SuppressLint({"HandlerLeak"})
public class NewValidateOneCardActivity extends BaseActivity implements OnLoadCompleteListener {
    private static final int NETWORKCONNECTFAIL = -1;
    private static final int ONECARD = 0;
    private static final int ONECARDINFO = 1;
    private static String hostResponseCodeText = null;
    private static String modelResponseCodeText = null;
    private static String serverResponseCodeText = null;
    private String PRODUCT_TYPE = ((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"));
    private String VENDING_HTML;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Intent intent;
            switch (msg.what) {
                case -1:
                    if ("PAPER".equals(NewValidateOneCardActivity.this.PRODUCT_TYPE)) {
                        intent = new Intent(NewValidateOneCardActivity.this, SelectRecycleActivity.class);
                        intent.putExtra("RECYCLE", "RECYCLEPAPER");
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        NewValidateOneCardActivity.this.startActivity(intent);
                        NewValidateOneCardActivity.this.finish();
                        return;
                    }
                    intent = new Intent(NewValidateOneCardActivity.this, SelectRecycleActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    NewValidateOneCardActivity.this.startActivity(intent);
                    NewValidateOneCardActivity.this.finish();
                    return;
                case 0:
                    SysGlobal.execute(new ValidateCardInfoThread(NewValidateOneCardActivity.this.handler));
                    return;
                case 1:
                    if (!CardEntity.isValid) {
                        SysGlobal.execute(new Runnable() {
                            public void run() {
                                NewValidateOneCardActivity.this.executeGUIAction(false, new GUIActionGotoServiceProcess(), new Object[]{NewValidateOneCardActivity.this.getBaseActivity(), Integer.valueOf(2), "TRANSPORTCARD"});
                                NewValidateOneCardActivity.this.finish();
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
                                        NewValidateOneCardActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{NewValidateOneCardActivity.this.getBaseActivity(), Integer.valueOf(2), "TRANSPORTCARD"});
                                        NewValidateOneCardActivity.this.finish();
                                        return;
                                    }
                                    intent = new Intent(NewValidateOneCardActivity.this, ActivityAdActivity.class);
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    NewValidateOneCardActivity.this.startActivity(intent);
                                    NewValidateOneCardActivity.this.finish();
                                } else if (VENDING_FLAG == null || StringUtils.isBlank((String) VENDING_FLAG.get(AllAdvertisement.VENDING_PIC))) {
                                    NewValidateOneCardActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{NewValidateOneCardActivity.this.getBaseActivity(), Integer.valueOf(2), "TRANSPORTCARD"});
                                    NewValidateOneCardActivity.this.finish();
                                } else {
                                    intent = new Intent(NewValidateOneCardActivity.this, ActivityAdActivity.class);
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    NewValidateOneCardActivity.this.startActivity(intent);
                                    NewValidateOneCardActivity.this.finish();
                                }
                            }
                        });
                        return;
                    } else if (CardEntity.CARD_STATUS == -1) {
                        if (NewValidateOneCardActivity.this.validatecardText != null) {
                            NewValidateOneCardActivity.this.validatecardText.setText(NewValidateOneCardActivity.this.getString(R.string.toast_card_unuse));
                        }
                        SystemClock.sleep(3000);
                        if ("PAPER".equals(NewValidateOneCardActivity.this.PRODUCT_TYPE)) {
                            intent = new Intent(NewValidateOneCardActivity.this, SelectRecycleActivity.class);
                            intent.putExtra("RECYCLE", "RECYCLEPAPER");
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            NewValidateOneCardActivity.this.startActivity(intent);
                            NewValidateOneCardActivity.this.finish();
                            return;
                        }
                        intent = new Intent(NewValidateOneCardActivity.this, SelectRecycleActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        NewValidateOneCardActivity.this.startActivity(intent);
                        NewValidateOneCardActivity.this.finish();
                        return;
                    } else if (CardEntity.CARD_STATUS == -2) {
                        intent = new Intent(NewValidateOneCardActivity.this, BindCardActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        NewValidateOneCardActivity.this.startActivity(intent);
                        NewValidateOneCardActivity.this.finish();
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
                        ((TextView) NewValidateOneCardActivity.this.findViewById(R.id.shua_ka_time)).setText("" + remainedSeconds);
                    } else if ("PAPER".equals(NewValidateOneCardActivity.this.PRODUCT_TYPE)) {
                        intent = new Intent(NewValidateOneCardActivity.this, SelectRecycleActivity.class);
                        intent.putExtra("RECYCLE", "RECYCLEPAPER");
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        NewValidateOneCardActivity.this.startActivity(intent);
                        NewValidateOneCardActivity.this.finish();
                    } else {
                        intent = new Intent(NewValidateOneCardActivity.this, SelectRecycleActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        NewValidateOneCardActivity.this.startActivity(intent);
                        NewValidateOneCardActivity.this.finish();
                    }
                }
            };
            NewValidateOneCardActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    Runnable updateThread = new Runnable() {
        public void run() {
            if (NewValidateOneCardActivity.this.validatecardText != null) {
                NewValidateOneCardActivity.this.validatecardText.setText(R.string.validatecardfail);
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
                HashMap<String, Object> hsmpResult = guiCommonService.execute("GUITrafficCardCommonService", "verifyOneCard", hm);
                if (hsmpResult != null) {
                    mg.what = 1;
                    CardEntity.isValid = true;
                    CardEntity.CARD_STATUS = Integer.parseInt((String) hsmpResult.get("CARD_STATUS"));
                    CardEntity.IS_RECHANGE = Integer.parseInt((String) hsmpResult.get("IS_RECHANGE"));
                    CardEntity.RECHARGED = Double.parseDouble((String) hsmpResult.get("RECHARGE"));
                    CardEntity.INCOM_AMOUNT = Double.parseDouble((String) hsmpResult.get("INCOM_AMOUNT"));
                } else {
                    if (NewValidateOneCardActivity.this.isPlaySounds && NewValidateOneCardActivity.this.soundPool != null) {
                        NewValidateOneCardActivity.this.soundPool.load(NewValidateOneCardActivity.this, R.raw.wangluozhongduan, 0);
                    }
                    try {
                        NewValidateOneCardActivity.this.runOnUiThread(NewValidateOneCardActivity.this.updateThread);
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
                while (i < tryTimes && NewValidateOneCardActivity.this.onecardReadingEnable) {
                    try {
                        HashMap<String, Object> hashMap = guiCommonService.execute("GUITrafficCardCommonService", "readOneCard", null);
                        if (hashMap == null || !NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hashMap.get("RET_CODE"))) {
                            if (i == 4) {
                                try {
                                    NewValidateOneCardActivity.this.validatecardText.post(new Runnable() {
                                        public void run() {
                                            NewValidateOneCardActivity.this.validatecardText.setText(Html.fromHtml(StringUtils.replace(NewValidateOneCardActivity.this.getString(R.string.validatecardwarn), "$ONECARD_FLAG$", "<big>" + NewValidateOneCardActivity.this.getString(R.string.oneCard_flag) + "</big>")));
                                        }
                                    });
                                } catch (Exception e) {
                                }
                            }
                            if (i == 6 && hashMap != null) {
                                try {
                                    if (hashMap.size() > 0) {
                                        final String validatecardwarn = NewValidateOneCardActivity.this.showErrorCode(hashMap);
                                        NewValidateOneCardActivity.this.validatecardText.post(new Runnable() {
                                            public void run() {
                                                NewValidateOneCardActivity.this.validatecardText.setText(validatecardwarn);
                                            }
                                        });
                                    }
                                } catch (Exception e2) {
                                }
                            }
                            synchronized (NewValidateOneCardActivity.this.objWaitOneCard) {
                                try {
                                    NewValidateOneCardActivity.this.objWaitOneCard.wait(lInterval);
                                } catch (InterruptedException e3) {
                                    e3.printStackTrace();
                                }
                            }
                            i++;
                        } else {
                            Message msg = NewValidateOneCardActivity.this.handler.obtainMessage();
                            CardEntity.CARD_NO = hashMap.get("ONECARD_NUM").toString();
                            msg.what = 0;
                            NewValidateOneCardActivity.this.handler.sendMessage(msg);
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
        setContentView(R.layout.activity_newvalidatecard);
        backgroundToActivity();
        this.intent = getIntent();
        this.VENDING_HTML = this.intent.getStringExtra("advert");
        ((Button) findViewById(R.id.shua_ka_return_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if ("PAPER".equals(NewValidateOneCardActivity.this.PRODUCT_TYPE)) {
                    Intent intent = new Intent(NewValidateOneCardActivity.this, SelectRecycleActivity.class);
                    intent.putExtra("RECYCLE", "RECYCLEPAPER");
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    NewValidateOneCardActivity.this.startActivity(intent);
                    NewValidateOneCardActivity.this.finish();
                    return;
                }
                intent = new Intent(NewValidateOneCardActivity.this, SelectRecycleActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                NewValidateOneCardActivity.this.startActivity(intent);
                NewValidateOneCardActivity.this.finish();
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

    private String showErrorCode(HashMap hsmp) {
        String errorType = ErrorCode.MODEL_RESPONSECODE;
        String str = (String) hsmp.get(ErrorCode.MODEL_RESPONSECODE);
        if (StringUtils.isBlank(str)) {
            errorType = "HOST_RESPONSECODE";
            str = (String) hsmp.get("HOST_RESPONSECODE");
        }
        if (StringUtils.isBlank(str)) {
            errorType = "SERVER_RESPONSECODE";
            str = (String) hsmp.get("SERVER_RESPONSECODE");
        }
        if (StringUtils.isBlank(str)) {
            errorType = "RVM_RESPONSECODE";
            str = (String) hsmp.get("RVM_RESPONSECODE");
        }
        if (StringUtils.isBlank(str)) {
            errorType = "RVM_RESPONSECODE";
            str = NetworkUtils.NET_STATE_UNKNOWN;
        }
        if (StringUtils.isBlank(errorType) || StringUtils.isBlank(str)) {
            return "";
        }
        String codeDesc;
        int idxStart;
        int idxEnd;
        String type;
        if (ErrorCode.MODEL_RESPONSECODE.equalsIgnoreCase(errorType)) {
            codeDesc = null;
            try {
                if (StringUtils.isBlank(modelResponseCodeText)) {
                    modelResponseCodeText = new String(IOUtils.readResource("code/modelrescode.txt"), "UTF-8").toUpperCase();
                }
                if (!StringUtils.isBlank(modelResponseCodeText)) {
                    idxStart = modelResponseCodeText.indexOf(str + "=");
                    if (idxStart != -1) {
                        idxEnd = modelResponseCodeText.indexOf(10, idxStart);
                        codeDesc = idxEnd == -1 ? modelResponseCodeText.substring(idxStart + 5).trim() : modelResponseCodeText.substring(idxStart + 5, idxEnd).trim();
                    }
                }
            } catch (Exception e) {
            }
            type = getString(R.string.model_responsecode);
            if (StringUtils.isBlank(codeDesc)) {
                return type + " : " + str;
            }
            return type + " : " + str + "(" + codeDesc + ")";
        } else if ("HOST_RESPONSECODE".equalsIgnoreCase(errorType)) {
            if (str.length() == 1) {
                str = "0" + str;
            }
            codeDesc = null;
            try {
                if (StringUtils.isBlank(hostResponseCodeText)) {
                    hostResponseCodeText = new String(IOUtils.readResource("code/hostrescode.txt"), "UTF-8");
                }
                if (!StringUtils.isBlank(hostResponseCodeText)) {
                    idxStart = hostResponseCodeText.indexOf(str + "=");
                    if (idxStart != -1) {
                        idxEnd = hostResponseCodeText.indexOf(10, idxStart);
                        codeDesc = idxEnd == -1 ? hostResponseCodeText.substring(idxStart + 3).trim() : hostResponseCodeText.substring(idxStart + 3, idxEnd).trim();
                    }
                }
            } catch (Exception e2) {
            }
            type = getString(R.string.model_hostresponsecode);
            if (StringUtils.isBlank(codeDesc)) {
                return type + " : " + str;
            }
            return type + " : " + str + "(" + codeDesc + ")";
        } else if ("SERVER_RESPONSECODE".equalsIgnoreCase(errorType)) {
            if (str.length() == 1) {
                str = "0" + str;
            }
            codeDesc = null;
            try {
                if (StringUtils.isBlank(serverResponseCodeText)) {
                    serverResponseCodeText = new String(IOUtils.readResource("code/hostrescode.txt"), "UTF-8");
                }
                if (!StringUtils.isBlank(serverResponseCodeText)) {
                    idxStart = serverResponseCodeText.indexOf(str + "=");
                    if (idxStart != -1) {
                        idxEnd = serverResponseCodeText.indexOf(10, idxStart);
                        codeDesc = idxEnd == -1 ? serverResponseCodeText.substring(idxStart + 3).trim() : serverResponseCodeText.substring(idxStart + 3, idxEnd).trim();
                    }
                }
            } catch (Exception e3) {
            }
            type = getString(R.string.server_response);
            if (StringUtils.isBlank(codeDesc)) {
                return type + " : " + str;
            }
            return type + " : " + str + "(" + codeDesc + ")";
        } else if (!"RVM_RESPONSECODE".equalsIgnoreCase(errorType)) {
            return "";
        } else {
            if ("MODEL_NORESPONSE".equalsIgnoreCase(str) || "MODEL_UNKNOWN".equalsIgnoreCase(str)) {
                return getString(R.string.onecard_model_error);
            }
            if ("SERVER_LOST".equalsIgnoreCase(str) || "SERVER_NORESPONSE".equalsIgnoreCase(str) || "SERVER_COMMERROR".equalsIgnoreCase(str)) {
                return getString(R.string.onecard_server_error);
            }
            if ("CARD_NOTFOUND".equalsIgnoreCase(str)) {
                return StringUtils.replace(getString(R.string.validatecardwarn), "$ONECARD_FLAG$", getString(R.string.oneCard_flag));
            }
            if ("CARD_NOTMATCH".equalsIgnoreCase(str)) {
                return getString(R.string.validatecardnomatch);
            }
            if ("UNSUPPORT".equalsIgnoreCase(str)) {
                return getString(R.string.onecard_error_unsupport);
            }
            if ("CHARGE_CANCELED".equalsIgnoreCase(str)) {
                return getString(R.string.onecard_error_charge_canceled);
            }
            if ("CARD_USED".equalsIgnoreCase(str)) {
                return getString(R.string.onecard_error_card_used);
            }
            if ("CARD_PWDERROR".equalsIgnoreCase(str)) {
                return getString(R.string.onecard_error_pwderror);
            }
            if ("CARD_AMOUNTERROR".equalsIgnoreCase(str)) {
                return getString(R.string.onecard_error_card_amounterror);
            }
            if ("QUICKCARD_STATUSERROR".equalsIgnoreCase(str)) {
                return getString(R.string.onecard_error_quickcard_statuserror);
            }
            return getString(R.string.onecard_error_unkown);
        }
    }
}
