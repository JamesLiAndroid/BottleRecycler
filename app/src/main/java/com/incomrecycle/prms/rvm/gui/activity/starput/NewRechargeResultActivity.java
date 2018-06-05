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
import com.incomrecycle.common.utils.IOUtils;
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
import com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardCommEntity.ErrorCode;
import java.text.DecimalFormat;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class NewRechargeResultActivity extends BaseActivity implements OnLoadCompleteListener {
    private static String hostResponseCodeText = null;
    private static String modelResponseCodeText = null;
    private static String serverResponseCodeText = null;
    private String JSON;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        Intent intent = new Intent();
                        if (!StringUtils.isBlank(NewRechargeResultActivity.this.JSON) && "BOTTLE".equalsIgnoreCase(ServiceGlobal.getCurrentSession("PRODUCT_TYPE").toString())) {
                            intent.setClass(NewRechargeResultActivity.this, EnvironmentalPromotionalActivity.class);
                            intent.putExtra("JSON", NewRechargeResultActivity.this.JSON);
                        } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                            try {
                                intent.setClass(NewRechargeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        NewRechargeResultActivity.this.startActivity(intent);
                        NewRechargeResultActivity.this.finish();
                        return;
                    }
                    ((TextView) NewRechargeResultActivity.this.findViewById(R.id.onecard_recharge_result_time)).setText("" + remainedSeconds);
                }
            };
            NewRechargeResultActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
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
                if (!StringUtils.isBlank(NewRechargeResultActivity.this.JSON) && "BOTTLE".equalsIgnoreCase(ServiceGlobal.getCurrentSession("PRODUCT_TYPE").toString())) {
                    intent.setClass(NewRechargeResultActivity.this, EnvironmentalPromotionalActivity.class);
                    intent.putExtra("JSON", NewRechargeResultActivity.this.JSON);
                } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        intent.setClass(NewRechargeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                NewRechargeResultActivity.this.startActivity(intent);
                NewRechargeResultActivity.this.finish();
            }
        });
    }

    protected void onStart() {
        super.onStart();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
        TextView recharged = (TextView) findViewById(R.id.recharged);
        TextView moneyInsideCard = (TextView) findViewById(R.id.money_insidecard);
        TextView card_number = (TextView) findViewById(R.id.card_number);
        View recharge_info_view = findViewById(R.id.onecard_recharge_result_success_layout);
        View recharge_fail_view = findViewById(R.id.onecard_recharge_result_fail_layout);
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
            String CARDNO = data.getString("CARDNO");
            double BALANCE = data.getDouble("BALANCE") / 100.0d;
            recharged.setText(df.format(data.getDouble("CHARGE_AMOUNT") / 100.0d) + "");
            moneyInsideCard.setText(df.format(BALANCE) + "");
            card_number.setText(CARDNO);
            return;
        }
        if (isPlaySounds && this.soundPool != null) {
            this.soundPool.load(this, R.raw.chongzhishibai, 0);
        }
        recharge_info_view.setVisibility(View.GONE);
        recharge_fail_view.setVisibility(View.VISIBLE);
        rechargeResultImg.updateResource(R.drawable.recharge_failed, null);
        rechargeResultText.setText(R.string.recharge_fail);
        Bundle data = intent.getBundleExtra("mData");
        String errorType = data.getString("errorType");
        String errorCode = data.getString("errorCode");
        TextView failed = (TextView) findViewById(R.id.failed_textView);
        String failedReason = intent.getStringExtra("reason");
        if (!StringUtils.isBlank(failedReason) && "NOT_ENOUGH".equalsIgnoreCase(failedReason)) {
            failed.setText(R.string.onecard_balance_not_enough);
        } else if (!StringUtils.isBlank(errorType) && !StringUtils.isBlank(errorCode)) {
            failed.setText(getTransCardErrorCode(errorType, errorCode));
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
        ((TextView) findViewById(R.id.onecard_recharge_result_time)).setText("");
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

    public String getTransCardErrorCode(String errorType, String errorCode) {
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
                    idxStart = modelResponseCodeText.indexOf(errorCode + "=");
                    if (idxStart != -1) {
                        idxEnd = modelResponseCodeText.indexOf(10, idxStart);
                        codeDesc = idxEnd == -1 ? modelResponseCodeText.substring(idxStart + 5).trim() : modelResponseCodeText.substring(idxStart + 5, idxEnd).trim();
                    }
                }
            } catch (Exception e) {
            }
            type = getString(R.string.model_responsecode);
            if (StringUtils.isBlank(codeDesc)) {
                return type + " : " + errorCode;
            }
            return type + " : " + errorCode + "(" + codeDesc + ")";
        } else if ("HOST_RESPONSECODE".equalsIgnoreCase(errorType)) {
            if (errorCode.length() == 1) {
                errorCode = "0" + errorCode;
            }
            codeDesc = null;
            try {
                if (StringUtils.isBlank(hostResponseCodeText)) {
                    hostResponseCodeText = new String(IOUtils.readResource("code/hostrescode.txt"), "UTF-8");
                }
                if (!StringUtils.isBlank(hostResponseCodeText)) {
                    idxStart = hostResponseCodeText.indexOf(errorCode + "=");
                    if (idxStart != -1) {
                        idxEnd = hostResponseCodeText.indexOf(10, idxStart);
                        codeDesc = idxEnd == -1 ? hostResponseCodeText.substring(idxStart + 3).trim() : hostResponseCodeText.substring(idxStart + 3, idxEnd).trim();
                    }
                }
            } catch (Exception e2) {
            }
            type = getString(R.string.model_hostresponsecode);
            if (StringUtils.isBlank(codeDesc)) {
                return type + " : " + errorCode;
            }
            return type + " : " + errorCode + "(" + codeDesc + ")";
        } else if ("SERVER_RESPONSECODE".equalsIgnoreCase(errorType)) {
            if (errorCode.length() == 1) {
                errorCode = "0" + errorCode;
            }
            codeDesc = null;
            try {
                if (StringUtils.isBlank(serverResponseCodeText)) {
                    serverResponseCodeText = new String(IOUtils.readResource("code/hostrescode.txt"), "UTF-8");
                }
                if (!StringUtils.isBlank(serverResponseCodeText)) {
                    idxStart = serverResponseCodeText.indexOf(errorCode + "=");
                    if (idxStart != -1) {
                        idxEnd = serverResponseCodeText.indexOf(10, idxStart);
                        codeDesc = idxEnd == -1 ? serverResponseCodeText.substring(idxStart + 3).trim() : serverResponseCodeText.substring(idxStart + 3, idxEnd).trim();
                    }
                }
            } catch (Exception e3) {
            }
            type = getString(R.string.server_response);
            if (StringUtils.isBlank(codeDesc)) {
                return type + " : " + errorCode;
            }
            return type + " : " + errorCode + "(" + codeDesc + ")";
        } else if (!"RVM_RESPONSECODE".equalsIgnoreCase(errorType)) {
            return "";
        } else {
            if ("MODEL_NORESPONSE".equalsIgnoreCase(errorCode) || "MODEL_UNKNOWN".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_model_error);
            }
            if ("SERVER_LOST".equalsIgnoreCase(errorCode) || "SERVER_NORESPONSE".equalsIgnoreCase(errorCode) || "SERVER_COMMERROR".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_server_error);
            }
            if ("CARD_NOTFOUND".equalsIgnoreCase(errorCode)) {
                return StringUtils.replace(getString(R.string.validatecardwarn), "$ONECARD_FLAG$", getString(R.string.oneCard_flag));
            }
            if ("CARD_NOTMATCH".equalsIgnoreCase(errorCode)) {
                return getString(R.string.validatecardnomatch);
            }
            if ("UNSUPPORT".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_error_unsupport);
            }
            if ("CHARGE_CANCELED".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_error_charge_canceled);
            }
            if ("CARD_USED".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_error_card_used);
            }
            if ("CARD_PWDERROR".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_error_pwderror);
            }
            if ("CARD_AMOUNTERROR".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_error_card_amounterror);
            }
            if ("QUICKCARD_STATUSERROR".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_error_quickcard_statuserror);
            }
            return getString(R.string.onecard_error_unkown);
        }
    }
}
