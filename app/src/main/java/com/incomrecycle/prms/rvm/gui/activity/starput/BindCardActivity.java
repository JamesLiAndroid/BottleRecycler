package com.incomrecycle.prms.rvm.gui.activity.starput;

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
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.gui.entity.CardEntity;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class BindCardActivity extends BaseActivity implements OnClickListener {
    private String JSON;
    private String PRODUCT_TYPE = ((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"));
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
    private GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
    private boolean hasPut;
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
                BindCardActivity.this.iv_delete.setVisibility(View.GONE);
            } else {
                BindCardActivity.this.iv_delete.setVisibility(View.VISIBLE);
            }
        }

        public void afterTextChanged(Editable s) {
            this.editStart = BindCardActivity.this.phoneNumEdit.getSelectionStart();
            this.editEnd = BindCardActivity.this.phoneNumEdit.getSelectionEnd();
        }
    };
    private EditText phoneNumEdit = null;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        Intent intent = new Intent();
                        if (BindCardActivity.this.hasPut) {
                            BindCardActivity.this.uploadData();
                        }
                        if (StringUtils.isBlank(BindCardActivity.this.JSON)) {
                            if ("PAPER".equals(BindCardActivity.this.PRODUCT_TYPE)) {
                                intent.setClass(BindCardActivity.this, ThankPaperPageActivity.class);
                            } else {
                                intent.setClass(BindCardActivity.this, ThankBottlePageActivity.class);
                            }
                        } else if ("BOTTLE".equalsIgnoreCase(BindCardActivity.this.PRODUCT_TYPE)) {
                            intent.setClass(BindCardActivity.this, ThankBottlePageActivity.class);
                            intent.putExtra("JSON", BindCardActivity.this.JSON);
                        } else {
                            intent.setClass(BindCardActivity.this, ThankPaperPageActivity.class);
                            intent.putExtra("JSON", BindCardActivity.this.JSON);
                        }
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        BindCardActivity.this.startActivity(intent);
                        BindCardActivity.this.finish();
                        return;
                    }
                    ((TextView) BindCardActivity.this.findViewById(R.id.bind_card_time)).setText("" + remainedSeconds);
                }
            };
            BindCardActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_bindcard);
        backgroundToActivity();
        this.JSON = getIntent().getStringExtra("JSON");
        initView();
        setClickListener();
        HashMap<String, Object> hsmpResult = null;
        try {
            hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "hasRecycled", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (hsmpResult != null && "TRUE".equalsIgnoreCase((String) hsmpResult.get("RESULT"))) {
            this.hasPut = true;
        }
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
        this.btnReturn.setOnClickListener(this);
        this.btnConfirm.setOnClickListener(this);
        this.btnBindNext.setOnClickListener(this);
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
        this.btnCancle = (Button) findViewById(R.id.bind_card_cancle);
        this.btnClear = (Button) findViewById(R.id.bind_card_clear);
        this.btnReturn = (Button) findViewById(R.id.bind_card_return_btn);
        this.btnConfirm = (Button) findViewById(R.id.bind_card_confirm);
        this.btnBindNext = (Button) findViewById(R.id.bind_card_next);
        this.phoneNumEdit = (EditText) findViewById(R.id.bind_card_phonenum_edit);
        this.phoneNumEdit.setInputType(0);
        this.cardWarn = (TextView) findViewById(R.id.bind_card_remind_text);
        this.iv_delete = (ImageView) findViewById(R.id.delete);
    }

    protected void onStart() {
        super.onStart();
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
                    if (sampleId == BindCardActivity.this.bangDing) {
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
            case R.id.bind_card_clear:
                if (this.phoneNumEdit != null) {
                    this.phoneNumEdit.setText("");
                    this.iv_delete.setVisibility(8);
                }
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.zero:
                setKeyClickListener(7);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.bind_card_cancle:
                setKeyClickListener(67);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.bind_card_return_btn:
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
            case R.id.bind_card_confirm:
                if (!StringUtils.isBlank(this.phoneNumEdit.getText().toString())) {
                    String OneCardVerson;
                    GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                    HashMap<String, Object> hsmp = new HashMap();
                    hsmp.put("PHONG_NUM", this.phoneNumEdit.getText());
                    String result = "error";
                    try {
                        OneCardVerson = SysConfig.get("RVM.ONECARD.DRV.VERSION");
                        if (OneCardVerson.equals("0")) {
                            hsmp = guiCommonService.execute("GUIOneCardCommonService", "verifyBindPhone", hsmp);
                        } else if (OneCardVerson.equals("1")) {
                            hsmp = guiCommonService.execute("GUITrafficCardCommonService", "verifyBindPhone", hsmp);
                        }
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
                        HashMap<String, Object> hsmpRet = null;
                        try {
                            OneCardVerson = SysConfig.get("RVM.ONECARD.DRV.VERSION");
                            if (OneCardVerson.equals("0")) {
                                hsmpRet = guiCommonService.execute("GUIOneCardCommonService", "bindOneCard", hp);
                            } else if (OneCardVerson.equals("1")) {
                                hsmpRet = guiCommonService.execute("GUITrafficCardCommonService", "bindOneCard", hp);
                            }
                            if (hsmpRet != null) {
                                String retCode = (String) hsmpRet.get("RESULT");
                                if ("success".equalsIgnoreCase(retCode)) {
                                    if (this.cardWarn != null) {
                                        this.cardWarn.setText(R.string.bind_success);
                                    }
                                    Intent intent1 = new Intent(this, OneCardBindInfoActivity.class);
                                    intent1.putExtra("JSON", this.JSON);
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
            case R.id.bind_card_next:
                if (this.hasPut) {
                    uploadData();
                    Intent intent2 = new Intent();
                    if (StringUtils.isBlank(this.JSON)) {
                        if ("PAPER".equals(this.PRODUCT_TYPE)) {
                            intent2.setClass(this, ThankPaperPageActivity.class);
                        } else {
                            intent2.setClass(this, ThankBottlePageActivity.class);
                        }
                    } else if ("BOTTLE".equalsIgnoreCase(this.PRODUCT_TYPE)) {
                        intent2.setClass(this, ThankBottlePageActivity.class);
                        intent2.putExtra("JSON", this.JSON);
                    } else {
                        intent2.setClass(this, ThankPaperPageActivity.class);
                        intent2.putExtra("JSON", this.JSON);
                    }
                    intent2.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent2);
                    finish();
                    return;
                }
                executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{getBaseActivity(), Integer.valueOf(2), "TRANSPORTCARD"});
                finish();
                return;
            default:
                return;
        }
    }

    private void uploadData() {
        try {
            String OneCardVerson = SysConfig.get("RVM.ONECARD.DRV.VERSION");
            if (OneCardVerson.equals("0")) {
                this.guiCommonService.execute("GUIOneCardCommonService", "recycleEnd", null);
            } else if (OneCardVerson.equals("1")) {
                this.guiCommonService.execute("GUITrafficCardCommonService", "recycleEnd", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
