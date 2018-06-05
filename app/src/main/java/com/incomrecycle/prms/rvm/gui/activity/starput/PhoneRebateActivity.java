package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
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
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import it.sauronsoftware.ftp4j.FTPCodes;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class PhoneRebateActivity extends BaseActivity implements OnClickListener {
    private static final int BLACKLIST = -1;
    private static final int ERROR_NUM = -2;
    private static final int FAIL_NET = -3;
    private String PRODUCT_TYPE = ((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"));
    private Button btnCancle;
    private Button btnClear;
    private Button btnConfirm;
    private Button btnNum0;
    private Button btnNum1;
    private Button btnNum2;
    private Button btnNum3;
    private Button btnNum4;
    private Button btnNum5;
    private Button btnNum6;
    private Button btnNum7;
    private Button btnNum8;
    private Button btnNum9;
    private Button btnReturn;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case -3:
                    Toast toast2 = Toast.makeText(PhoneRebateActivity.this.getBaseContext(), R.string.network_fail, 0);
                    toast2.setGravity(17, 0, 0);
                    toast2.show();
                    return;
                case -2:
                    Toast toast1 = Toast.makeText(PhoneRebateActivity.this.getBaseContext(), R.string.input_phonenum_error_warning, 0);
                    toast1.setGravity(17, 0, 0);
                    toast1.show();
                    return;
                case -1:
                    Toast toast = Toast.makeText(PhoneRebateActivity.this.getBaseContext(), R.string.wechat_unuse, 0);
                    toast.setGravity(17, 0, 0);
                    toast.show();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean isClick = false;
    private ImageView iv_delete;
    TextWatcher mTextWatcher = new TextWatcher() {
        private int editEnd;
        private int editStart;
        private CharSequence temp;

        public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            this.temp = s;
        }

        public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            if (s.length() == 0) {
                PhoneRebateActivity.this.iv_delete.setVisibility(View.GONE);
                return;
            }
            PhoneRebateActivity.this.iv_delete.setVisibility(View.VISIBLE);
            if (s.length() == 11) {
                GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                HashMap<String, Object> hashRet = new HashMap();
                hashRet.put("PHONE_NO", PhoneRebateActivity.this.phoneNumEdit.getText().toString());
                try {
                    if (guiCommonService.execute("GUIPhoneCommonService", "verifyPhone", hashRet).get("RET_PHONE_NO").equals("cmcc_phone_num")) {
                        LayoutInflater factory = LayoutInflater.from(PhoneRebateActivity.this);
                        final Dialog dialog = new Dialog(PhoneRebateActivity.this, R.style.Custom_dialog);
                        View contentView = factory.inflate(R.layout.dialog_cmcc_phone_reminder, null);
                        dialog.setTitle(Html.fromHtml(StringUtils.replace(PhoneRebateActivity.this.getResources().getString(R.string.cmccRechargeReminder), PhoneRebateActivity.this.getResources().getString(R.string.cmccRechargeReminder), "<H1><font color=\"#ff0000\">" + PhoneRebateActivity.this.getResources().getString(R.string.cmccRechargeReminder) + "</font></H1>")));
                        dialog.setContentView(contentView);
                        dialog.setCancelable(false);
                        dialog.show();
                        LayoutParams params0 = dialog.getWindow().getAttributes();
                        params0.x = 10;
                        params0.y = 11;
                        params0.width = FTPCodes.FILE_NOT_FOUND;
                        params0.height = 360;
                        dialog.getWindow().setAttributes(params0);
                        ((Button) contentView.findViewById(R.id.btnKnown)).setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                HashMap map = new HashMap();
                                map.put("KEY", AllClickContent.PHONE_REBATE_CONFIRM_CONFIRM);
                                try {
                                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                                } catch (Exception e) {
                                }
                                dialog.dismiss();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void afterTextChanged(Editable s) {
            this.editStart = PhoneRebateActivity.this.phoneNumEdit.getSelectionStart();
            this.editEnd = PhoneRebateActivity.this.phoneNumEdit.getSelectionEnd();
        }
    };
    private EditText phoneNumEdit = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) PhoneRebateActivity.this.findViewById(R.id.phone_time)).setText("" + remainedSeconds);
                    } else if ("PAPER".equals(PhoneRebateActivity.this.PRODUCT_TYPE)) {
                        Intent intent = new Intent(PhoneRebateActivity.this, SelectRecycleActivity.class);
                        intent.putExtra("RECYCLE", "RECYCLEPAPER");
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        PhoneRebateActivity.this.startActivity(intent);
                        PhoneRebateActivity.this.finish();
                    } else {
                        Intent intent = new Intent(PhoneRebateActivity.this, SelectRecycleActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        PhoneRebateActivity.this.startActivity(intent);
                        PhoneRebateActivity.this.finish();
                    }
                }
            };
            PhoneRebateActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_phone);
        backgroundToActivity();
        initView();
        setClickListener();
    }

    public void setClickListener() {
        this.btnNum1.setOnClickListener(this);
        this.btnNum2.setOnClickListener(this);
        this.btnNum3.setOnClickListener(this);
        this.btnNum4.setOnClickListener(this);
        this.btnNum5.setOnClickListener(this);
        this.btnNum6.setOnClickListener(this);
        this.btnNum7.setOnClickListener(this);
        this.btnNum8.setOnClickListener(this);
        this.btnNum9.setOnClickListener(this);
        this.btnNum0.setOnClickListener(this);
        this.btnCancle.setOnClickListener(this);
        this.btnConfirm.setOnClickListener(this);
        this.btnReturn.setOnClickListener(this);
        this.btnClear.setOnClickListener(this);
        this.iv_delete.setOnClickListener(this);
        this.phoneNumEdit.addTextChangedListener(this.mTextWatcher);
    }

    public void initView() {
        this.btnNum1 = (Button) findViewById(R.id.one);
        this.btnNum2 = (Button) findViewById(R.id.two);
        this.btnNum3 = (Button) findViewById(R.id.three);
        this.btnNum4 = (Button) findViewById(R.id.four);
        this.btnNum5 = (Button) findViewById(R.id.five);
        this.btnNum6 = (Button) findViewById(R.id.six);
        this.btnNum7 = (Button) findViewById(R.id.seven);
        this.btnNum8 = (Button) findViewById(R.id.eight);
        this.btnNum9 = (Button) findViewById(R.id.nine);
        this.btnNum0 = (Button) findViewById(R.id.zero);
        this.btnReturn = (Button) findViewById(R.id.phone_return_btn);
        this.btnConfirm = (Button) findViewById(R.id.phone_confirm);
        this.btnCancle = (Button) findViewById(R.id.phone_cancle);
        this.btnClear = (Button) findViewById(R.id.phone_clear);
        this.phoneNumEdit = (EditText) findViewById(R.id.phone_phonenum_edit);
        this.phoneNumEdit.setInputType(0);
        this.iv_delete = (ImageView) findViewById(R.id.delete);
    }

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMOUT.PHONECHARGE")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void onStop() {
        super.onStop();
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

    public void setKeyClickListener(int KeyCode) {
        final int clickKeyCode = KeyCode;
        SysGlobal.execute(new Runnable() {
            public void run() {
                new Instrumentation().sendKeyDownUpSync(clickKeyCode);
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete:
                setKeyClickListener(67);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.one:
                setKeyClickListener(8);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.two:
                setKeyClickListener(9);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.three:
                setKeyClickListener(10);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.four:
                setKeyClickListener(11);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.five:
                setKeyClickListener(12);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.six:
                setKeyClickListener(13);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.seven:
                setKeyClickListener(14);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.eight:
                setKeyClickListener(15);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.nine:
                setKeyClickListener(16);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.zero:
                setKeyClickListener(7);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.phone_clear:
                if (this.phoneNumEdit != null) {
                    this.phoneNumEdit.setText("");
                }
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.phone_cancle:
                setKeyClickListener(67);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.phone_return_btn:
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.PHONE_REBATE_RETURN);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                Intent intent;
                if ("PAPER".equals(this.PRODUCT_TYPE)) {
                    intent = new Intent(this, SelectRecycleActivity.class);
                    intent.putExtra("RECYCLE", "RECYCLEPAPER");
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
                    return;
                }
                intent = new Intent(this, SelectRecycleActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                return;
            case R.id.phone_confirm:
                if (StringUtils.isBlank(this.phoneNumEdit.getText().toString())) {
                    final TextView remindText = (TextView) findViewById(R.id.phone_remind_text);
                    remindText.post(new Runnable() {
                        public void run() {
                            remindText.setText(R.string.input_phonenum_null_warning);
                        }
                    });
                } else {
                    phoneNumVerify();
                }
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            default:
                return;
        }
    }

    public void phoneNumVerify() {
        if (!this.isClick) {
            HashMap map = new HashMap();
            map.put("KEY", "120142");
            try {
                CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
            } catch (Exception e) {
            }
            GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
            HashMap<String, Object> hashMap = new HashMap();
            hashMap.put("PHONE_NO", this.phoneNumEdit.getText().toString());
            GUIGlobal.setCurrentSession("PHONE_NUM", hashMap);
            try {
                hashMap = guiCommonService.execute("GUIPhoneCommonService", "verifyPhone", hashMap);
                if (hashMap.get("RET_PHONE_NO").equals("right_phone_num") || hashMap.get("RET_PHONE_NO").equals("cmcc_phone_num")) {
                    this.isClick = true;
                    GUIGlobal.setCurrentSession("phone_number", this.phoneNumEdit.getText().toString());
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            try {
                                HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
                                HashMap<String, Object> VENDING_FLAG = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
                                Intent intent;
                                if (TRANSMIT_ADV != null) {
                                    HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
                                    if (HOMEPAGE_LEFT == null || StringUtils.isBlank((String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_PIC))) {
                                        PhoneRebateActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{PhoneRebateActivity.this.getBaseActivity(), Integer.valueOf(2), "PHONE"});
                                        PhoneRebateActivity.this.finish();
                                        return;
                                    }
                                    intent = new Intent(PhoneRebateActivity.this.getBaseContext(), ActivityAdActivity.class);
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    PhoneRebateActivity.this.startActivity(intent);
                                    PhoneRebateActivity.this.finish();
                                } else if (VENDING_FLAG == null || StringUtils.isBlank((String) VENDING_FLAG.get(AllAdvertisement.VENDING_PIC))) {
                                    PhoneRebateActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{PhoneRebateActivity.this.getBaseActivity(), Integer.valueOf(2), "PHONE"});
                                    PhoneRebateActivity.this.finish();
                                } else {
                                    intent = new Intent(PhoneRebateActivity.this.getBaseContext(), ActivityAdActivity.class);
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    PhoneRebateActivity.this.startActivity(intent);
                                    PhoneRebateActivity.this.finish();
                                }
                            } catch (Exception e) {
                            }
                        }
                    });
                } else if (hashMap.get("RET_PHONE_NO").equals("error_phone_num")) {
                    final TextView remindText = (TextView) findViewById(R.id.phone_remind_text);
                    remindText.post(new Runnable() {
                        public void run() {
                            remindText.setText(R.string.input_phonenum_error_warning);
                        }
                    });
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}
