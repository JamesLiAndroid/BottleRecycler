package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardCommEntity.ErrorCode;
import it.sauronsoftware.ftp4j.FTPCodes;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class OneCardToScrapeActivity extends BaseActivity {
    private Button btnend = null;
    private Button btnrechange = null;
    private EditText etmoney = null;
    private EditText etpsd = null;
    private boolean isClick = false;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        Intent intent = new Intent();
                        intent.setClass(OneCardToScrapeActivity.this.getApplicationContext(), OneCardRechargeHintActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        OneCardToScrapeActivity.this.startActivity(intent);
                        OneCardToScrapeActivity.this.finish();
                        return;
                    }
                    ((TextView) OneCardToScrapeActivity.this.findViewById(R.id.wechat_rebate_time)).setText("" + remainedSeconds);
                }
            };
            OneCardToScrapeActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    TextWatcher watcher = new TextWatcher() {
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            TimeoutTask.getTimeoutTask().reset(OneCardToScrapeActivity.this.timeoutAction);
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        public void afterTextChanged(Editable arg0) {
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_onecardscrape_login);
        backgroundToActivity();
        initview();
        this.btnend = (Button) findViewById(R.id.end);
        this.btnrechange = (Button) findViewById(R.id.onecard_rechange);
        this.btnend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(OneCardToScrapeActivity.this.getApplicationContext(), OneCardRechargeHintActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                OneCardToScrapeActivity.this.startActivity(intent);
                OneCardToScrapeActivity.this.finish();
            }
        });
        this.btnrechange.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                LayoutInflater factory;
                final Dialog dialog;
                View contentView;
                LayoutParams params0;
                Button btnKnown;
                if (OneCardToScrapeActivity.this.etmoney.getText().toString().equals("") || OneCardToScrapeActivity.this.etmoney == null || OneCardToScrapeActivity.this.etpsd.getText().toString().equals("") || OneCardToScrapeActivity.this.etpsd == null) {
                    factory = LayoutInflater.from(OneCardToScrapeActivity.this);
                    dialog = new Dialog(OneCardToScrapeActivity.this, R.style.Custom_dialog);
                    contentView = factory.inflate(R.layout.dialog_onecard_toload, null);
                    ((TextView) contentView.findViewById(R.id.reminderToLoad)).setText(R.string.scrape_notnull);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setContentView(contentView);
                    if (!OneCardToScrapeActivity.this.isFinishing()) {
                        dialog.show();
                    }
                    params0 = dialog.getWindow().getAttributes();
                    params0.x = 10;
                    params0.y = 11;
                    params0.width = FTPCodes.FILE_NOT_FOUND;
                    params0.height = 360;
                    dialog.getWindow().setAttributes(params0);
                    btnKnown = (Button) contentView.findViewById(R.id.btnKnown);
                    btnKnown.setVisibility(0);
                    btnKnown.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    OneCardToScrapeActivity.this.etmoney.setText("");
                    OneCardToScrapeActivity.this.etpsd.setText("");
                } else if (OneCardToScrapeActivity.this.etmoney.getText().toString().startsWith("0")) {
                    OneCardToScrapeActivity.this.etmoney.setText("");
                } else {
                    if ((Integer.parseInt((String) ServiceGlobal.getCurrentSession("BALANCE")) / 100) + Integer.parseInt(OneCardToScrapeActivity.this.etmoney.getText().toString()) > Integer.parseInt((String) ServiceGlobal.getCurrentSession("MaxBalance")) / 100) {
                        factory = LayoutInflater.from(OneCardToScrapeActivity.this);
                        dialog = new Dialog(OneCardToScrapeActivity.this, R.style.Custom_dialog);
                        contentView = factory.inflate(R.layout.dialog_onecard_thousand, null);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setContentView(contentView);
                        if (!OneCardToScrapeActivity.this.isFinishing()) {
                            dialog.show();
                        }
                        params0 = dialog.getWindow().getAttributes();
                        params0.x = 10;
                        params0.y = 11;
                        params0.width = FTPCodes.FILE_NOT_FOUND;
                        params0.height = 360;
                        dialog.getWindow().setAttributes(params0);
                        btnKnown = (Button) contentView.findViewById(R.id.btnKnown);
                        btnKnown.setVisibility(View.VISIBLE);
                        btnKnown.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        OneCardToScrapeActivity.this.etmoney.setText("");
                        return;
                    }
                    factory = LayoutInflater.from(OneCardToScrapeActivity.this);
                    dialog = new Dialog(OneCardToScrapeActivity.this, R.style.Custom_dialog);
                    contentView = factory.inflate(R.layout.dialog_onecard_toload, null);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setContentView(contentView);
                    if (!OneCardToScrapeActivity.this.isFinishing()) {
                        dialog.show();
                    }
                    params0 = dialog.getWindow().getAttributes();
                    params0.x = 10;
                    params0.y = 11;
                    params0.width = FTPCodes.FILE_NOT_FOUND;
                    params0.height = 360;
                    dialog.getWindow().setAttributes(params0);
                    OneCardToScrapeActivity.this.onecardNumVerify();
                }
            }
        });
        onClick();
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMOUT.PHONECHARGE")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void initview() {
        this.etpsd = (EditText) findViewById(R.id.scrape_password);
        this.etmoney = (EditText) findViewById(R.id.scrape_money);
    }

    public void onClick() {
        this.etpsd.addTextChangedListener(this.watcher);
        this.etmoney.addTextChangedListener(this.watcher);
    }

    public void onecardNumVerify() {
        SysGlobal.execute(new Runnable() {
            public void run() {
                if (!OneCardToScrapeActivity.this.isClick) {
                    OneCardToScrapeActivity.this.isClick = true;
                    if (OneCardToScrapeActivity.this.etpsd.getText().toString() == null || OneCardToScrapeActivity.this.etpsd.getText().toString().trim().equals("") || OneCardToScrapeActivity.this.etmoney.getText().toString() == null || OneCardToScrapeActivity.this.etmoney.getText().toString().trim().equals("")) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            public void run() {
                                Toast.makeText(OneCardToScrapeActivity.this.getBaseContext(), R.string.scrape_notnull, 0).show();
                            }
                        });
                        return;
                    }
                    GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                    String OneCardNum = (String) ServiceGlobal.getCurrentSession("ONECARD_NUM");
                    HashMap<String, Object> hashMap = new HashMap();
                    hashMap.put("CardNo", OneCardNum);
                    hashMap.put("CardPwd", OneCardToScrapeActivity.this.etpsd.getText().toString().trim());
                    hashMap.put("Balance", String.valueOf(Integer.parseInt(OneCardToScrapeActivity.this.etmoney.getText().toString()) * 100));
                    ServiceGlobal.setCurrentSession("ONECARD_MONEY", OneCardToScrapeActivity.this.etmoney.getText().toString());
                    HashMap hsmp = new HashMap();
                    try {
                        hsmp = guiCommonService.execute("GUITrafficCardCommonService", "ChargeScrashCard", hashMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String RET_CODE = null;
                    if (hsmp != null) {
                        RET_CODE = (String) hsmp.get("RET_CODE");
                    }
                    Intent intent;
                    if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase(RET_CODE)) {
                        intent = new Intent(OneCardToScrapeActivity.this.getApplicationContext(), NewOneCardRechargeResultActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        OneCardToScrapeActivity.this.startActivity(intent);
                        OneCardToScrapeActivity.this.finish();
                        return;
                    }
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
                    String reason = null;
                    if ("NOT_ENOUGH".equalsIgnoreCase(RET_CODE)) {
                        reason = RET_CODE;
                    }
                    intent = new Intent(OneCardToScrapeActivity.this.getApplicationContext(), NewOneCardRechargeResultWarnActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("errorType", errorType);
                    intent.putExtra("errorCode", str);
                    if (!StringUtils.isBlank(reason)) {
                        intent.putExtra("reason", reason);
                    }
                    OneCardToScrapeActivity.this.startActivity(intent);
                    OneCardToScrapeActivity.this.finish();
                }
            }
        });
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }
}
