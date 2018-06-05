package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.QRDecodeHelper;
import com.incomrecycle.prms.rvm.common.ResolveCode;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.gui.camera.CameraManager;
import com.incomrecycle.prms.rvm.gui.camera.ViewfinderView;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.io.IOException;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class AlipayActivity extends BaseActivity implements OnClickListener {
    private static final int BLACKLIST = -1;
    private static final int ERROR_NUM = -2;
    private static final int FAIL_NET = -3;
    private String PRODUCT_TYPE = ((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"));
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
    private CameraManager cameraManager = new CameraManager();
    private EditText editTextPhoneNum = null;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case -3:
                    Intent intent = new Intent(AlipayActivity.this.getApplicationContext(), AlipayShowActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    AlipayActivity.this.startActivity(intent);
                    AlipayActivity.this.finish();
                    return;
                case -2:
                    Toast toast1 = Toast.makeText(AlipayActivity.this.getBaseContext(), R.string.input_phonenum_error_warning, 0);
                    toast1.setGravity(17, 0, 0);
                    toast1.show();
                    return;
                case -1:
                    Toast toast = Toast.makeText(AlipayActivity.this.getBaseContext(), R.string.alipay_unuse, 0);
                    toast.setGravity(17, 0, 0);
                    toast.show();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean hasCarmeraOpen = false;
    private boolean hasSurface;
    private LayoutInflater infalter = null;
    private boolean isClick = false;
    private ImageView iv_big_circle;
    private ImageView iv_delete;
    private ImageView iv_four_corner;
    private ImageView iv_pg_bg_grey;
    private LinearLayout linearLayoutAlipay = null;
    TextWatcher mTextWatcher = new TextWatcher() {
        private int editEnd;
        private int editStart;
        private CharSequence temp;

        public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            this.temp = s;
        }

        public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            if (s.length() == 0) {
                AlipayActivity.this.iv_delete.setVisibility(8);
            } else {
                AlipayActivity.this.iv_delete.setVisibility(0);
            }
        }

        public void afterTextChanged(Editable s) {
            this.editStart = AlipayActivity.this.editTextPhoneNum.getSelectionStart();
            this.editEnd = AlipayActivity.this.editTextPhoneNum.getSelectionEnd();
        }
    };
    private Button new_phone_confirm = null;
    private ProgressBar pg;
    private Button phone_confirm = null;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) AlipayActivity.this.findViewById(R.id.alipay_time)).setText("" + remainedSeconds);
                    } else if ("PAPER".equals(AlipayActivity.this.PRODUCT_TYPE)) {
                        Intent intent = new Intent(AlipayActivity.this, SelectRecycleActivity.class);
                        intent.putExtra("RECYCLE", "RECYCLEPAPER");
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        AlipayActivity.this.startActivity(intent);
                        AlipayActivity.this.finish();
                    } else {
                        Intent intent = new Intent(AlipayActivity.this, SelectRecycleActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        AlipayActivity.this.startActivity(intent);
                        AlipayActivity.this.finish();
                    }
                }
            };
            AlipayActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private ViewfinderView viewfinderView;

    class EditChangedListener implements TextWatcher {
        EditChangedListener() {
        }

        public void afterTextChanged(Editable arg0) {
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            TimeoutTask.getTimeoutTask().reset(AlipayActivity.this.timeoutAction);
        }

        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }
    }

    private class SurfaceListener implements Callback {
        private SurfaceListener() {
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        public void surfaceCreated(SurfaceHolder holder) {
            if (!AlipayActivity.this.hasSurface) {
                AlipayActivity.this.hasSurface = true;
                AlipayActivity.this.initCamera(holder);
            }
        }

        public void surfaceDestroyed(SurfaceHolder arg0) {
            AlipayActivity.this.hasSurface = false;
            AlipayActivity.this.surfaceHolder = null;
            AlipayActivity.this.surfaceView = null;
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.hasCarmeraOpen) {
            this.cameraManager.closeDriver();
        }
    }

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        if (this.phone_confirm != null) {
            this.phone_confirm.setEnabled(true);
        }
        if (this.editTextPhoneNum != null) {
            this.editTextPhoneNum.setText("");
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.LOGIN")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onStop() {
        super.onStop();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void finish() {
        super.finish();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alipay);
        this.infalter = LayoutInflater.from(this);
        this.linearLayoutAlipay = (LinearLayout) findViewById(R.id.alipay_change_way);
        View layout = this.infalter.inflate(R.layout.alipay_phone, null);
        this.linearLayoutAlipay.removeAllViews();
        this.linearLayoutAlipay.addView(layout);
        initView();
        setClickListener();
        TextView alipay_result_remind_text = (TextView) findViewById(R.id.alipay_result_remind_text);
        alipay_result_remind_text.setText(R.string.alipay_inform_phone);
        alipay_result_remind_text.setGravity(17);
        this.new_phone_confirm = (Button) findViewById(R.id.alipay_phone_confirm_btn);
        this.new_phone_confirm.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (!AlipayActivity.this.isClick) {
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            if (StringUtils.isBlank(AlipayActivity.this.editTextPhoneNum.getText().toString())) {
                                final TextView remindText = (TextView) AlipayActivity.this.findViewById(R.id.alipay_result_remind_text);
                                remindText.post(new Runnable() {
                                    public void run() {
                                        remindText.setText(R.string.input_phonenum_null_warning);
                                    }
                                });
                                return;
                            }
                            AlipayActivity.this.phoneNumVerify();
                        }
                    });
                }
                TimeoutTask.getTimeoutTask().reset(AlipayActivity.this.timeoutAction);
            }
        });
        ((Button) findViewById(R.id.alipay_return_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if ("PAPER".equals(AlipayActivity.this.PRODUCT_TYPE)) {
                    Intent intent = new Intent(AlipayActivity.this, SelectRecycleActivity.class);
                    intent.putExtra("RECYCLE", "RECYCLEPAPER");
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    AlipayActivity.this.startActivity(intent);
                    AlipayActivity.this.finish();
                    return;
                }
                Intent intent = new Intent(AlipayActivity.this, SelectRecycleActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                AlipayActivity.this.startActivity(intent);
                AlipayActivity.this.finish();
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        return super.onTouchEvent(event);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            this.cameraManager.openDriver(Integer.parseInt(SysConfig.get("CAMERA.INDEX.QRCODE")), surfaceHolder);
            this.cameraManager.startPreview();
            SysGlobal.execute(new Runnable() {
                public void run() {
                    GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                    try {
                        guiCommonService.execute("GUICameraCommonService", "cameraStart", null);
                    } catch (Exception e) {
                    }
                    while (AlipayActivity.this.cameraManager.isPreviewing()) {
                        Bitmap bitmap = AlipayActivity.this.cameraManager.takePicture();
                        if (bitmap != null) {
                            String qrCode = QRDecodeHelper.decode(bitmap);
                            String qrCodeText = null;
                            if (qrCode != null) {
                                try {
                                    qrCodeText = ResolveCode.resolveQRcode(qrCode);
                                } catch (Exception e2) {
                                }
                            }
                            if (StringUtils.isBlank(qrCodeText)) {
                                bitmap.recycle();
                            } else {
                                GUIAction guiAction = new GUIAction() {
                                    protected void doAction(Object[] paramObjs) {
                                        AlipayActivity.this.handleDecode((byte[]) paramObjs[0], (Bitmap) paramObjs[1]);
                                    }
                                };
                                AlipayActivity.this.executeGUIAction(false, guiAction, new Object[]{qrCodeText.getBytes(), bitmap});
                                try {
                                    guiCommonService.execute("GUICameraCommonService", "cameraStop", null);
                                    return;
                                } catch (Exception e3) {
                                    return;
                                }
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e4) {
                            e4.printStackTrace();
                        } catch (Throwable th) {
                            try {
                                guiCommonService.execute("GUICameraCommonService", "cameraStop", null);
                            } catch (Exception e5) {
                            }
                        }
                    }
                    try {
                        guiCommonService.execute("GUICameraCommonService", "cameraStop", null);
                    } catch (Exception e6) {
                    }
                }
            });
        } catch (IOException e) {
        } catch (RuntimeException e2) {
        }
    }

    public void handleDecode(byte[] data, Bitmap barcode) {
        String resultString = new String(data);
        if (resultString.equals("")) {
            Toast.makeText(this, "Scan failed!", Toast.LENGTH_SHORT).show();
        } else if (this.pg != null && this.pg.isShown()) {
            this.pg.setVisibility(View.GONE);
            this.iv_pg_bg_grey.setVisibility(View.VISIBLE);
            this.iv_big_circle.setBackgroundResource(R.drawable.bar_code_center_grey);
            this.iv_four_corner.setBackgroundResource(R.drawable.bar_code_four_corner_grey);
        }
        if (barcode != null) {
            barcode.recycle();
        }
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        HashMap<String, Object> hsmpParam = new HashMap();
        hsmpParam.put("QRCODE_NO", resultString);
        try {
            String str = (String) guiCommonService.execute("GUIAlipayCommonService", "verifyQRCode", hsmpParam).get("RET_QRCODE");
            if (str.equals("error_qrcard_num")) {
                Toast toast = Toast.makeText(getBaseContext(), R.string.scan_fail, 0);
                toast.setGravity(17, 0, 0);
                toast.show();
                Intent intent;
                if ("PAPER".equals(this.PRODUCT_TYPE)) {
                    intent = new Intent();
                    intent.putExtra("RECYCLE", "RECYCLEPAPER");
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.setClass(getBaseContext(), SelectRecycleActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                intent = new Intent();
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.setClass(getBaseContext(), SelectRecycleActivity.class);
                startActivity(intent);
                finish();
            } else if (str.equals("right_qrcard_num")) {
                SysGlobal.execute(new Runnable() {
                    public void run() {
                        AlipayActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{AlipayActivity.this.getBaseActivity(), Integer.valueOf(2), "ALIPAY"});
                        AlipayActivity.this.finish();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ViewfinderView getViewfinderView() {
        return this.viewfinderView;
    }

    public void drawViewfinder() {
        this.viewfinderView.drawViewfinder();
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
        this.btnConfirm.setOnClickListener(this);
        this.btnClear.setOnClickListener(this);
        this.iv_delete.setOnClickListener(this);
        this.editTextPhoneNum.addTextChangedListener(this.mTextWatcher);
    }

    public void initView() {
        this.btnNum1 = (Button) findViewById(R.id.alipay_one);
        this.btnNum2 = (Button) findViewById(R.id.alipay_two);
        this.btnNum3 = (Button) findViewById(R.id.alipay_three);
        this.btnNum4 = (Button) findViewById(R.id.alipay_four);
        this.btnNum5 = (Button) findViewById(R.id.alipay_five);
        this.btnNum6 = (Button) findViewById(R.id.alipay_six);
        this.btnNum7 = (Button) findViewById(R.id.alipay_seven);
        this.btnNum8 = (Button) findViewById(R.id.alipay_eight);
        this.btnNum9 = (Button) findViewById(R.id.alipay_nine);
        this.btnNum0 = (Button) findViewById(R.id.alipay_zero);
        this.btnConfirm = (Button) findViewById(R.id.alipay_phone_confirm);
        this.btnClear = (Button) findViewById(R.id.alipay_phone_clear);
        this.editTextPhoneNum = (EditText) findViewById(R.id.alipay_phone_phonenum_edit);
        this.editTextPhoneNum.setInputType(0);
        this.iv_delete = (ImageView) findViewById(R.id.alipay_delete);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.alipay_delete:
                setKeyClickListener(67);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.alipay_one:
                setKeyClickListener(8);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.alipay_two:
                setKeyClickListener(9);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.alipay_three:
                setKeyClickListener(10);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.alipay_four:
                setKeyClickListener(11);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.alipay_five:
                setKeyClickListener(12);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.alipay_six:
                setKeyClickListener(13);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.alipay_seven:
                setKeyClickListener(14);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.alipay_eight:
                setKeyClickListener(15);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.alipay_nine:
                setKeyClickListener(16);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.alipay_phone_clear:
                if (this.editTextPhoneNum != null) {
                    this.editTextPhoneNum.setText("");
                }
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.alipay_zero:
                setKeyClickListener(7);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            case R.id.alipay_phone_confirm:
                if (StringUtils.isBlank(this.editTextPhoneNum.getText().toString())) {
                    final TextView remindText = (TextView) findViewById(R.id.alipay_result_remind_text);
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
            SysGlobal.execute(new Runnable() {
                public void run() {
                    AlipayActivity.this.isClick = true;
                    GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                    HashMap<String, Object> hashMap = new HashMap();
                    hashMap.put("PHONE_NO", AlipayActivity.this.editTextPhoneNum.getText().toString());
                    GUIGlobal.setCurrentSession("PHONE_NUMBER", hashMap);
                    try {
                        hashMap = guiCommonService.execute("GUIAlipayCommonService", "verifyPhone", hashMap);
                        Intent intent;
                        if (hashMap == null || hashMap.size() <= 0) {
                            AlipayActivity.this.handler.sendMessage(AlipayActivity.this.handler.obtainMessage(-3));
                            if ("PAPER".equals(AlipayActivity.this.PRODUCT_TYPE)) {
                                intent = new Intent(AlipayActivity.this, AlipayShowActivity.class);
                                intent.putExtra("RECYCLE", "RECYCLEPAPER");
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                AlipayActivity.this.startActivity(intent);
                                AlipayActivity.this.finish();
                                return;
                            }
                            intent = new Intent(AlipayActivity.this, AlipayShowActivity.class);
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            AlipayActivity.this.startActivity(intent);
                            AlipayActivity.this.finish();
                        } else if ("error_phone_num".equals(hashMap.get("RET_PHONE_NO"))) {
                            AlipayActivity.this.isClick = false;
                            AlipayActivity.this.handler.sendMessage(AlipayActivity.this.handler.obtainMessage(-2));
                        } else {
                            int cardStatus = -2;
                            try {
                                cardStatus = Integer.parseInt((String) hashMap.get("CARD_STATUS"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (cardStatus == 2 || cardStatus == 1) {
                                HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
                                HashMap<String, Object> VENDING_FLAG = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
                                if (TRANSMIT_ADV != null) {
                                    HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
                                    if (HOMEPAGE_LEFT == null || StringUtils.isBlank((String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_PIC))) {
                                        intent = new Intent(AlipayActivity.this.getBaseContext(), AlipayShowActivity.class);
                                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        AlipayActivity.this.startActivity(intent);
                                        AlipayActivity.this.finish();
                                        return;
                                    }
                                    intent = new Intent(AlipayActivity.this.getBaseContext(), AlipayShowActivity.class);
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    AlipayActivity.this.startActivity(intent);
                                    AlipayActivity.this.finish();
                                } else if (VENDING_FLAG == null || StringUtils.isBlank((String) VENDING_FLAG.get(AllAdvertisement.VENDING_PIC))) {
                                    intent = new Intent(AlipayActivity.this.getBaseContext(), AlipayShowActivity.class);
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    AlipayActivity.this.startActivity(intent);
                                    AlipayActivity.this.finish();
                                } else {
                                    intent = new Intent(AlipayActivity.this.getBaseContext(), AlipayShowActivity.class);
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    AlipayActivity.this.startActivity(intent);
                                    AlipayActivity.this.finish();
                                }
                            } else if (cardStatus == -1) {
                                AlipayActivity.this.handler.sendMessage(AlipayActivity.this.handler.obtainMessage(-1));
                                if ("PAPER".equals(AlipayActivity.this.PRODUCT_TYPE)) {
                                    intent = new Intent(AlipayActivity.this, SelectRecycleActivity.class);
                                    intent.putExtra("RECYCLE", "RECYCLEPAPER");
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    AlipayActivity.this.startActivity(intent);
                                    AlipayActivity.this.finish();
                                    return;
                                }
                                intent = new Intent(AlipayActivity.this, SelectRecycleActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                AlipayActivity.this.startActivity(intent);
                                AlipayActivity.this.finish();
                            } else if (cardStatus == -2) {
                                GUIGlobal.setCurrentSession("cardStatus", Integer.valueOf(cardStatus));
                                intent = new Intent(AlipayActivity.this, AlipayShowActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                AlipayActivity.this.startActivity(intent);
                                AlipayActivity.this.finish();
                            }
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            });
        }
    }
}
