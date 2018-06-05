package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

import java.util.HashMap;

import it.sauronsoftware.ftp4j.FTPCodes;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class OneCardToLoadActivity extends BaseActivity {
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
                        intent.setClass(OneCardToLoadActivity.this.getApplicationContext(), OneCardRechargeHintActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        OneCardToLoadActivity.this.startActivity(intent);
                        OneCardToLoadActivity.this.finish();
                        return;
                    }
                    ((TextView) OneCardToLoadActivity.this.findViewById(R.id.wechat_rebate_time)).setText("" + remainedSeconds);
                }
            };
            OneCardToLoadActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    TextWatcher watcher = new TextWatcher() {
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            TimeoutTask.getTimeoutTask().reset(OneCardToLoadActivity.this.timeoutAction);
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        public void afterTextChanged(Editable arg0) {
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_onecardtoload_login);
        backgroundToActivity();
        initview();
        this.btnend = (Button) findViewById(R.id.end);
        this.btnrechange = (Button) findViewById(R.id.onecard_rechange);
        this.btnend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(OneCardToLoadActivity.this.getApplicationContext(), OneCardRechargeHintActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                OneCardToLoadActivity.this.startActivity(intent);
                OneCardToLoadActivity.this.finish();
            }
        });
        this.btnrechange.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                final Dialog dialog;
                if (OneCardToLoadActivity.this.etpsd.getText().toString() == null ||
                        OneCardToLoadActivity.this.etpsd.getText().toString().trim().equals("") ||
                        OneCardToLoadActivity.this.etmoney.getText().toString() == null ||
                        OneCardToLoadActivity.this.etmoney.getText().toString().trim().equals("")) {

                    LayoutInflater factory = LayoutInflater.from(OneCardToLoadActivity.this);
                    dialog = new Dialog(OneCardToLoadActivity.this, R.style.Custom_dialog);
                    View contentView = factory.inflate(R.layout.dialog_onecard_toload, null);
                    ((TextView) contentView.findViewById(R.id.reminderToLoad)).setText(R.string.toLoad_notnull);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setContentView(contentView);
                    if (!OneCardToLoadActivity.this.isFinishing()) {
                        dialog.show();
                    }
                    LayoutParams params0 = dialog.getWindow().getAttributes();
                    params0.x = 10;
                    params0.y = 11;
                    params0.width = FTPCodes.FILE_NOT_FOUND;
                    params0.height = 360;
                    dialog.getWindow().setAttributes(params0);
                    Button btnKnown = (Button) contentView.findViewById(R.id.btnKnown);
                    btnKnown.setVisibility(View.VISIBLE);
                    btnKnown.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    OneCardToLoadActivity.this.etpsd.setText("");
                    OneCardToLoadActivity.this.etmoney.setText("");
                    return;
                }
                if ((Integer.parseInt((String) ServiceGlobal.getCurrentSession("BALANCE")) / 100) + Integer.parseInt(OneCardToLoadActivity.this.etmoney.getText().toString()) > Integer.parseInt((String) ServiceGlobal.getCurrentSession("MaxBalance")) / 100) {
                    LayoutInflater factory = LayoutInflater.from(OneCardToLoadActivity.this);
                    dialog = new Dialog(OneCardToLoadActivity.this, R.style.Custom_dialog);
                    View contentView = factory.inflate(R.layout.dialog_onecard_thousand, null);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setContentView(contentView);
                    if (!OneCardToLoadActivity.this.isFinishing()) {
                        dialog.show();
                    }
                    LayoutParams params0 = dialog.getWindow().getAttributes();
                    params0.x = 10;
                    params0.y = 11;
                    params0.width = FTPCodes.FILE_NOT_FOUND;
                    params0.height = 360;
                    dialog.getWindow().setAttributes(params0);
                    Button btnKnown = (Button) contentView.findViewById(R.id.btnKnown);
                    btnKnown.setVisibility(View.VISIBLE);
                    btnKnown.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    OneCardToLoadActivity.this.etmoney.setText("");
                    return;
                }
                String money = OneCardToLoadActivity.this.etmoney.getText().toString();
                int mon = Integer.parseInt(money);
                if (money.startsWith("0")) {
                    OneCardToLoadActivity.this.etmoney.setText("");
                } else if (mon % 10 != 0) {
                    LayoutInflater factory = LayoutInflater.from(OneCardToLoadActivity.this);
                    dialog = new Dialog(OneCardToLoadActivity.this, R.style.Custom_dialog);
                    View contentView = factory.inflate(R.layout.dialog_onecard_toload, null);
                    ((TextView) contentView.findViewById(R.id.reminderToLoad)).setText(R.string.money_ten);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setContentView(contentView);
                    if (!OneCardToLoadActivity.this.isFinishing()) {
                        dialog.show();
                    }
                    LayoutParams params0 = dialog.getWindow().getAttributes();
                    params0.x = 10;
                    params0.y = 11;
                    params0.width = FTPCodes.FILE_NOT_FOUND;
                    params0.height = 360;
                    dialog.getWindow().setAttributes(params0);
                    Button btnKnown = (Button) contentView.findViewById(R.id.btnKnown);
                    btnKnown.setVisibility(View.VISIBLE);
                    btnKnown.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    OneCardToLoadActivity.this.etmoney.setText("");
                } else {
                    LayoutInflater factory = LayoutInflater.from(OneCardToLoadActivity.this);
                    dialog = new Dialog(OneCardToLoadActivity.this, R.style.Custom_dialog);
                    View contentView = factory.inflate(R.layout.dialog_onecard_toload, null);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setContentView(contentView);
                    if (!OneCardToLoadActivity.this.isFinishing()) {
                        dialog.show();
                    }
                    LayoutParams params0 = dialog.getWindow().getAttributes();
                    params0.x = 10;
                    params0.y = 11;
                    params0.width = FTPCodes.FILE_NOT_FOUND;
                    params0.height = 360;
                    dialog.getWindow().setAttributes(params0);
                    OneCardToLoadActivity.this.onecardNumVerify();
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
        this.etpsd = (EditText) findViewById(R.id.toload_password);
        this.etmoney = (EditText) findViewById(R.id.toload_money);
    }

    public void onClick() {
        this.etmoney.addTextChangedListener(this.watcher);
        this.etpsd.setMaxEms(15);
        this.etpsd.setFilters(new InputFilter[]{new LengthFilter(15)});
    }

    public void onecardNumVerify() {
        SysGlobal.execute(new Runnable() {
            public void run() {
                if (!OneCardToLoadActivity.this.isClick) {
                    OneCardToLoadActivity.this.isClick = true;
                    GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                    String OneCardNum = (String) ServiceGlobal.getCurrentSession("ONECARD_NUM");
                    HashMap<String, Object> hashMap = new HashMap();
                    hashMap.put("CardNo", OneCardNum);
                    hashMap.put("UserAcctPassword", OneCardToLoadActivity.this.etpsd.getText().toString());
                    hashMap.put("Balance", String.valueOf(Integer.parseInt(OneCardToLoadActivity.this.etmoney.getText().toString()) * 100));
                    ServiceGlobal.setCurrentSession("ONECARD_MONEY", OneCardToLoadActivity.this.etmoney.getText().toString());
                    HashMap hsmp = new HashMap();
                    try {
                        hsmp = guiCommonService.execute("GUITrafficCardCommonService", "chargeWriteBack", hashMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String RET_CODE = null;
                    if (hsmp != null) {
                        RET_CODE = (String) hsmp.get("RET_CODE");
                    }
                    Intent intent;
                    if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase(RET_CODE)) {
                        intent = new Intent(OneCardToLoadActivity.this, NewOneCardRechargeResultActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        OneCardToLoadActivity.this.startActivity(intent);
                        OneCardToLoadActivity.this.finish();
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
                    intent = new Intent(OneCardToLoadActivity.this.getApplicationContext(), NewOneCardRechargeResultWarnActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("errorType", errorType);
                    intent.putExtra("errorCode", str);
                    if (!StringUtils.isBlank(reason)) {
                        intent.putExtra("reason", reason);
                    }
                    OneCardToLoadActivity.this.startActivity(intent);
                    OneCardToLoadActivity.this.finish();
                }
            }
        });
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }
}
