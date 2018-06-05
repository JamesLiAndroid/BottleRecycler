package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.incomrecycle.prms.rvm.gui.entity.CardEntity;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import it.sauronsoftware.ftp4j.FTPCodes;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class BdjActivity extends BaseActivity implements OnClickListener {
    private static final int BLACKLIST = -1;
    private static final int ERROR_NUM = -2;
    private static final int NET_ERROR = 2;
    private static final int PHONE_NUMM_NO_BANG = 1;
    private static final int PHONE_NUM_ISNULL = 0;
    private String PRODUCT_TYPE = ((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"));
    private ImageView bdj_iv_lv;
    private TextView bdj_remind_text;
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
    private Dialog dialog;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case -2:
                    BdjActivity.this.showDialog2NoticeUsers(-2, BdjActivity.this.getResources().getString(R.string.input_phonenum_error_warning));
                    return;
                case -1:
                    BdjActivity.this.showDialog2NoticeUsers(-1, BdjActivity.this.getResources().getString(R.string.jywPhoneBlack));
                    return;
                case 0:
                    BdjActivity.this.showDialog2NoticeUsers(0, BdjActivity.this.getResources().getString(R.string.jywPhoneCheckReminder_Null));
                    return;
                case 1:
                    BdjActivity.this.showDialog2NoticeUsers(1, BdjActivity.this.getResources().getString(R.string.jywPhoneNoBang));
                    return;
                case 2:
                    BdjActivity.this.showDialog2NoticeUsers(2, BdjActivity.this.getResources().getString(R.string.jywPhoneNetError));
                    return;
                default:
                    return;
            }
        }
    };
    private LayoutInflater inflater;
    private boolean isClick = false;
    private boolean isFinishCalled = false;
    private ImageView iv_delete = null;
    private View layout;
    private LinearLayout layoutPhone = null;
    TextWatcher mTextWatcher = new TextWatcher() {
        private int editEnd;
        private int editStart;
        private CharSequence temp;

        public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            this.temp = s;
        }

        public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            if (s.length() == 0) {
                BdjActivity.this.iv_delete.setVisibility(View.GONE);
            } else {
                BdjActivity.this.iv_delete.setVisibility(View.VISIBLE);
            }
        }

        public void afterTextChanged(Editable s) {
            this.editStart = BdjActivity.this.showPhoneNum.getSelectionStart();
            this.editEnd = BdjActivity.this.showPhoneNum.getSelectionEnd();
        }
    };
    private EditText showPhoneNum;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    BdjActivity.this.timeout_text.setText("" + remainedSeconds);
                    if (remainedSeconds != 0) {
                        return;
                    }
                    if ("PAPER".equals(BdjActivity.this.PRODUCT_TYPE)) {
                        Intent intent = new Intent(BdjActivity.this, SelectRecycleActivity.class);
                        intent.putExtra("RECYCLE", "RECYCLEPAPER");
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        BdjActivity.this.startActivity(intent);
                        BdjActivity.this.finish();
                        return;
                    }
                   Intent intent = new Intent(BdjActivity.this, SelectRecycleActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    BdjActivity.this.startActivity(intent);
                    BdjActivity.this.finish();
                }
            };
            BdjActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TextView timeout_text;
    private Button wechat_concern_back;
    private Button wechat_confirm;

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMOUT.PHONECHARGE")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onStop() {
        super.onStop();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
        if (this.showPhoneNum != null) {
            this.showPhoneNum.setText("");
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bdj_activity);
        backgroundToActivity();
        this.inflater = LayoutInflater.from(this);
        this.layoutPhone = (LinearLayout) findViewById(R.id.bdj_phone_layout);
        this.layout = this.inflater.inflate(R.layout.wechat_bdj_phone, null);
        this.layoutPhone.setOrientation(LinearLayout.HORIZONTAL);
        this.layoutPhone.removeAllViews();
        this.layoutPhone.addView(this.layout);
        initview();
        setClickListener();
    }

    private void initview() {
        this.timeout_text = (TextView) findViewById(R.id.bdj_time);
        this.wechat_confirm = (Button) findViewById(R.id.bdj_confirm_btn);
        this.wechat_concern_back = (Button) findViewById(R.id.bdj_return_btn);
        this.showPhoneNum = (EditText) this.layout.findViewById(R.id.wechat_phone_phonenum_edit);
        this.showPhoneNum.setInputType(0);
        this.iv_delete = (ImageView) this.layout.findViewById(R.id.wechat_delete);
        this.btnNum1 = (Button) this.layout.findViewById(R.id.wechat_one);
        this.btnNum2 = (Button) this.layout.findViewById(R.id.wechat_two);
        this.btnNum3 = (Button) this.layout.findViewById(R.id.wechat_three);
        this.btnNum4 = (Button) this.layout.findViewById(R.id.wechat_four);
        this.btnNum5 = (Button) this.layout.findViewById(R.id.wechat_five);
        this.btnNum6 = (Button) this.layout.findViewById(R.id.wechat_six);
        this.btnNum7 = (Button) this.layout.findViewById(R.id.wechat_seven);
        this.btnNum8 = (Button) this.layout.findViewById(R.id.wechat_eight);
        this.btnNum9 = (Button) this.layout.findViewById(R.id.wechat_nine);
        this.btnNum0 = (Button) this.layout.findViewById(R.id.wechat_zero);
        this.btnConfirm = (Button) this.layout.findViewById(R.id.wechat_phone_confirm);
        this.btnClear = (Button) this.layout.findViewById(R.id.wechat_phone_clear);
        this.bdj_remind_text = (TextView) findViewById(R.id.bdj_remind_text);
        this.bdj_iv_lv = (ImageView) findViewById(R.id.bdj_iv_lv);
    }

    private void setClickListener() {
        this.wechat_confirm.setOnClickListener(this);
        this.wechat_concern_back.setOnClickListener(this);
        this.showPhoneNum.addTextChangedListener(this.mTextWatcher);
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
        this.btnConfirm.setOnClickListener(this);
        this.btnClear.setOnClickListener(this);
        this.iv_delete.setOnClickListener(this);
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
            case R.id.bdj_return_btn:
                startActivity(new Intent(this, SelectRecycleActivity.class));
                finish();
                return;
            case R.id.bdj_confirm_btn:
                confirmBtnClick();
                return;
            case R.id.btnConfirm:
                confirmBtnClick();
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.wechat_delete:
                setKeyClickListener(67);
                return;
            case R.id.wechat_one:
                setKeyClickListener(8);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.wechat_two:
                setKeyClickListener(9);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.wechat_three:
                setKeyClickListener(10);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.wechat_four:
                setKeyClickListener(11);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.wechat_five:
                setKeyClickListener(12);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.wechat_six:
                setKeyClickListener(13);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.wechat_seven:
                setKeyClickListener(14);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.wechat_eight:
                setKeyClickListener(15);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.wechat_nine:
                setKeyClickListener(16);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.wechat_phone_clear:
                if (this.showPhoneNum != null) {
                    this.showPhoneNum.setText("");
                }
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.wechat_zero:
                setKeyClickListener(7);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.wechat_phone_confirm:
                confirmBtnClick();
                return;
            default:
                return;
        }
    }

    private void confirmBtnClick() {
        if (StringUtils.isBlank(this.showPhoneNum.getText().toString())) {
            Message msg = new Message();
            msg.what = 0;
            this.handler.sendMessage(msg);
        } else {
            phoneNumVerify();
        }
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
    }

    public void phoneNumVerify() {
        if (!this.isClick) {
            this.isClick = true;
            SysGlobal.execute(new Runnable() {
                public void run() {
                    GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                    HashMap<String, Object> hashMap = new HashMap();
                    hashMap.put("PHONE_NO", BdjActivity.this.showPhoneNum.getText().toString());
                    GUIGlobal.setCurrentSession("PHONE_NUMBER", hashMap);
                    try {
                        TimeoutAction timeoutAction = new TimeoutAction() {
                            public void apply(int forwardSeconds, int remainedSeconds) {
                                if (remainedSeconds == 0) {
                                    BdjActivity.this.gotoNextStep();
                                }
                            }
                        };
                        TimeoutTask.getTimeoutTask().addTimeoutAction(timeoutAction, 3, true);
                        HashMap hsmpResult = guiCommonService.execute("GUIBdjCommonService", "verifyPhone", hashMap);
                        TimeoutTask.getTimeoutTask().removeTimeoutAction(timeoutAction);
                        Intent intent;
                        if (hsmpResult == null || hsmpResult.size() <= 0) {
                            intent = new Intent(BdjActivity.this, Concern_bdj.class);
                            intent.putExtra("MARKING", "NOTWO");
                            BdjActivity.this.startActivity(intent);
                            BdjActivity.this.finish();
                        } else if ("error_phone_num".equals(hsmpResult.get("RET_PHONE_NO"))) {
                            BdjActivity.this.handler.sendMessage(BdjActivity.this.handler.obtainMessage(-2));
                        } else if ("no_phone_num".equals(hsmpResult.get("RET_PHONE_NO"))) {
                            BdjActivity.this.handler.sendMessage(BdjActivity.this.handler.obtainMessage(0));
                        } else {
                            int i = -2;
                            try {
                                i = Integer.parseInt((String) hsmpResult.get("CARD_STATUS"));
                                CardEntity.CARD_STATUS = i;
                            } catch (Exception e) {
                            }
                            if (2 == i || 1 == i) {
                                BdjActivity.this.gotoNextStep();
                            }
                            if (-3 == i) {
                                intent = new Intent(BdjActivity.this, Concern_bdj.class);
                                intent.putExtra("MARKING", "NOLV");
                                BdjActivity.this.startActivity(intent);
                                BdjActivity.this.finish();
                            } else if (-2 == i) {
                                intent = new Intent(BdjActivity.this, Concern_bdj.class);
                                intent.putExtra("MARKING", "NOTWO");
                                BdjActivity.this.startActivity(intent);
                                BdjActivity.this.finish();
                            } else if (-1 == i) {
                                BdjActivity.this.handler.sendMessage(BdjActivity.this.handler.obtainMessage(-1));
                            }
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            });
        }
    }

    private void showDialog2NoticeUsers(final int checkStut, String reminder) {
        LayoutInflater factory = LayoutInflater.from(this);
        this.dialog = new Dialog(this, R.style.Custom_dialog);
        View contentView = factory.inflate(R.layout.dialog_phone_check_reminder, null);
        this.dialog.setContentView(contentView);
        this.dialog.setCancelable(false);
        this.dialog.show();
        LayoutParams params0 = this.dialog.getWindow().getAttributes();
        params0.x = 10;
        params0.y = 11;
        params0.width = FTPCodes.FILE_ACTION_NOT_TAKEN;
        params0.height = 320;
        this.dialog.getWindow().setAttributes(params0);
        ((Button) contentView.findViewById(R.id.btnKnown)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BdjActivity.this.isClick = false;
                BdjActivity.this.dialog.dismiss();
                if (checkStut == 1 || checkStut == 2) {
                    BdjActivity.this.startActivity(new Intent(BdjActivity.this, Concern_bdj.class));
                    BdjActivity.this.finish();
                }
            }
        });
        TextView showTextView = (TextView) contentView.findViewById(R.id.phone_check_reminder);
        if (reminder != null) {
            showTextView.setText(reminder);
        }
    }

    public void updateLanguage() {
    }

    private void gotoNextStep() {
        executeGUIAction(false, new GUIActionGotoServiceProcess(), new Object[]{getBaseActivity(), Integer.valueOf(2), "BDJ"});
        TimeoutTask.getTimeoutTask().addTimeoutAction(new TimeoutAction() {
            public void apply(int forwardSeconds, int remainedSeconds) {
                if (remainedSeconds == 0) {
                    TimeoutTask.getTimeoutTask().removeTimeoutAction(this);
                    HashMap hsmpGUIEvent = new HashMap();
                    hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                    hsmpGUIEvent.put("EVENT", "CMD");
                    hsmpGUIEvent.put("CMD", "timeoutEnd");
                    hsmpGUIEvent.put("BaseActivity", BdjActivity.this);
                    GUIGlobal.getEventMgr().addEvent(hsmpGUIEvent);
                }
            }
        }, 5, true);
    }

    public void doEvent(HashMap hsmpEvent) {
        if ("CMD".equalsIgnoreCase((String) hsmpEvent.get("EVENT"))) {
            Intent intent;
            String CMD = (String) hsmpEvent.get("CMD");
            if ("recycleEnd".equalsIgnoreCase(CMD)) {
                if (((BaseActivity) hsmpEvent.get("BaseActivity")) == this && !this.isFinishCalled) {
                    this.isFinishCalled = true;
                    intent = new Intent(this, BdjResultActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("JSON", (String) hsmpEvent.get("JSON"));
                    startActivity(intent);
                    finish();
                } else {
                    return;
                }
            }
            if ("timeoutEnd".equalsIgnoreCase(CMD) && ((BaseActivity) hsmpEvent.get("BaseActivity")) == this && !this.isFinishCalled) {
                this.isFinishCalled = true;
                intent = new Intent(this, Concern_bdj.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        }
    }
}
