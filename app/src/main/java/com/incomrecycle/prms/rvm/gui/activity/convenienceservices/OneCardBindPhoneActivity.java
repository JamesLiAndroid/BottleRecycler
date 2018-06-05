package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.app.Instrumentation;
import android.content.Intent;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.entity.CardEntity;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class OneCardBindPhoneActivity extends BaseActivity implements OnClickListener {
    private int bangDing;
    private Button btnBindNext = null;
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
    private TextView cardWarn = null;
    private boolean isAlreadyBind;
    private boolean isPlaySounds;
    private ImageView iv_delete = null;
    TextWatcher mTextWatcher = new TextWatcher() {
        private int editEnd;
        private int editStart;
        private CharSequence temp;

        public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            this.temp = s;
        }

        public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            if (this.temp.length() == 0) {
                OneCardBindPhoneActivity.this.iv_delete.setVisibility(8);
            } else {
                OneCardBindPhoneActivity.this.iv_delete.setVisibility(0);
            }
        }

        public void afterTextChanged(Editable s) {
            this.editStart = OneCardBindPhoneActivity.this.phoneNumEdit.getSelectionStart();
            this.editEnd = OneCardBindPhoneActivity.this.phoneNumEdit.getSelectionEnd();
        }
    };
    private EditText phoneNumEdit = null;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) OneCardBindPhoneActivity.this.findViewById(R.id.query_onecard_bind_card_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(OneCardBindPhoneActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            OneCardBindPhoneActivity.this.startActivity(intent);
                            OneCardBindPhoneActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            OneCardBindPhoneActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_query_onecard_bindcard);
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
        this.btnClear.setOnClickListener(this);
        this.btnConfirm.setOnClickListener(this);
        this.btnBindNext.setOnClickListener(this);
        this.btnReturn.setOnClickListener(this);
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
        this.btnConfirm = (Button) findViewById(R.id.query_onecard_bind_card_confirm);
        this.btnReturn = (Button) findViewById(R.id.query_onecard_bind_card_return_btn);
        this.btnCancle = (Button) findViewById(R.id.query_onecard_bind_card_cancle);
        this.btnClear = (Button) findViewById(R.id.query_onecard_bind_card_clear);
        this.btnBindNext = (Button) findViewById(R.id.query_onecard_bind_card_next);
        this.phoneNumEdit = (EditText) findViewById(R.id.query_onecard_bind_card_phonenum_edit);
        this.phoneNumEdit.setInputType(0);
        this.cardWarn = (TextView) findViewById(R.id.query_onecard_bind_card_remind_text);
        this.iv_delete = (ImageView) findViewById(R.id.delete);
    }

    protected void onStart() {
        super.onStart();
        if (CardEntity.CARD_STATUS == -2) {
            this.isAlreadyBind = false;
        } else {
            this.isAlreadyBind = true;
        }
        if (this.isAlreadyBind) {
            ((TextView) findViewById(R.id.isBind_show)).setVisibility(View.GONE);
            if (this.cardWarn != null) {
                this.cardWarn.setText(R.string.rebind_phone);
                this.cardWarn.setGravity(17);
            }
        }
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onResume() {
        super.onResume();
        if (this.isPlaySounds && this.soundPool != null) {
            this.bangDing = this.soundPool.load(this, R.raw.bangdingshoujihao, 0);
            this.soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    if (sampleId == OneCardBindPhoneActivity.this.bangDing) {
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
            case R.id.query_onecard_bind_card_clear:
                if (this.phoneNumEdit != null) {
                    this.phoneNumEdit.setText("");
                }
                this.iv_delete.setVisibility(8);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.query_onecard_bind_card_cancle:
                setKeyClickListener(67);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.query_onecard_bind_card_return_btn:
                Intent intent = new Intent();
                intent.setClass(this, OneCardRechargeHintActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                return;
            case R.id.query_onecard_bind_card_confirm:
                if (!StringUtils.isBlank(this.phoneNumEdit.getText().toString())) {
                    String OneCardVerson;
                    GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                    HashMap<String, Object> hsmp = new HashMap();
                    hsmp.put("PHONG_NUM", this.phoneNumEdit.getText());
                    String result = "error";
                    try {
                        OneCardVerson = SysConfig.get("RVM.ONECARD.DRV.VERSION");
                        hsmp = guiCommonService.execute("GUIOneCardCommonService", "verifyBindPhone", hsmp);
                        if (hsmp != null) {
                            result = (String) hsmp.get("RET_CODE");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if ("none".equalsIgnoreCase(result)) {
                        if (this.cardWarn != null) {
                            this.cardWarn.setText(R.string.input_phonenum_null_warning);
                        }
                    } else if ("error".equalsIgnoreCase(result)) {
                        if (this.cardWarn != null) {
                            this.cardWarn.setText(R.string.input_phonenum_error_warning);
                        }
                    } else if ("success".equalsIgnoreCase(result)) {
                        HashMap<String, Object> hp = new HashMap();
                        hp.put("PHONE_NUM", this.phoneNumEdit.getText());
                        hp.put("ONECARD_NUM", CardEntity.CARD_NO);
                        try {
                            OneCardVerson = SysConfig.get("RVM.ONECARD.DRV.VERSION");
                            HashMap<String, Object> hsmpRet = guiCommonService.execute("GUIOneCardCommonService", "bindOneCard", hp);
                            if (hsmpRet != null) {
                                String retCode = (String) hsmpRet.get("RESULT");
                                if ("success".equalsIgnoreCase(retCode)) {
                                    if (this.cardWarn != null) {
                                        this.cardWarn.setText(R.string.bind_success);
                                    }
                                    Intent intent1 = new Intent(this, OneCardBindPhoneInfoActivity.class);
                                    intent1.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(intent1);
                                    finish();
                                }
                                if ("failed".equalsIgnoreCase(retCode) && this.cardWarn != null) {
                                    this.cardWarn.setText(R.string.bind_failed);
                                }
                                if ("error".equalsIgnoreCase(retCode) && this.cardWarn != null) {
                                    this.cardWarn.setText(R.string.bind_failed);
                                }
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                } else if (this.cardWarn != null) {
                    this.cardWarn.setText(R.string.input_phonenum_null_warning);
                }
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.query_onecard_bind_card_next:
                Intent intent2 = new Intent();
                if (this.isAlreadyBind) {
                    intent2.setClass(this, OneCardRechargeHintActivity.class);
                } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        intent2.setClass(getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                }
                intent2.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent2);
                finish();
                return;
            default:
                return;
        }
    }
}
