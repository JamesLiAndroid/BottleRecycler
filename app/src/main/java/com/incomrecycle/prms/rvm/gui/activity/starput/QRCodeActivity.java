package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CalendarView;
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
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
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

public class QRCodeActivity extends BaseActivity implements Callback, OnLoadCompleteListener, OnClickListener {
    private String PRODUCT_TYPE = ((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"));
    private TextView activityQRcodeInputType = null;
    private Button btnClear;
    private Button btnLogin = null;
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
    private boolean hasCameraOpen = false;
    private boolean hasSurface;
    private LayoutInflater inflater = null;
    private TextView inputQRcodeNumRemindText = null;
    private boolean isClick = false;
    private boolean isPlaySounds;
    private ImageView iv_big_circle;
    private ImageView iv_delete = null;
    private ImageView iv_four_corner;
    private ImageView iv_pg_bg_grey;
    private LinearLayout lin = null;
    private ProgressBar pg;
    private TextView remindText = null;
    private Button scanQRcodeConfirmBtn = null;
    private SoundPool soundPool = null;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) QRCodeActivity.this.findViewById(R.id.scan_qrcode_time)).setText("" + remainedSeconds);
                    } else if ("PAPER".equals(QRCodeActivity.this.PRODUCT_TYPE)) {
                        Intent intent = new Intent(QRCodeActivity.this, SelectRecycleActivity.class);
                        intent.putExtra("RECYCLE", "RECYCLEPAPER");
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        QRCodeActivity.this.startActivity(intent);
                        QRCodeActivity.this.finish();
                    } else {
                        Intent intent = new Intent(QRCodeActivity.this, SelectRecycleActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        QRCodeActivity.this.startActivity(intent);
                        QRCodeActivity.this.finish();
                    }
                }
            };
            QRCodeActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private EditText userNameEditText = null;
    private ViewfinderView viewfinderView;

    class EditChangedListener implements TextWatcher {
        EditChangedListener() {
        }

        public void afterTextChanged(Editable arg0) {
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            TimeoutTask.getTimeoutTask().reset(QRCodeActivity.this.timeoutAction);
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
            if (!QRCodeActivity.this.hasSurface) {
                QRCodeActivity.this.hasSurface = true;
                QRCodeActivity.this.initCamera(holder);
            }
        }

        public void surfaceDestroyed(SurfaceHolder arg0) {
            QRCodeActivity.this.hasSurface = false;
            QRCodeActivity.this.surfaceHolder = null;
            QRCodeActivity.this.surfaceView = null;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_qrcode);
        backgroundToActivity();
        this.inflater = LayoutInflater.from(getApplicationContext());
        this.lin = (LinearLayout) findViewById(R.id.login_type_layout);
        this.lin.addView(this.inflater.inflate(R.layout.login_by_qrcard, null));
        this.userNameEditText = (EditText) this.lin.findViewById(R.id.userNameEditText);
        this.userNameEditText.setText("");
        this.userNameEditText.setInputType(0);
        this.userNameEditText.addTextChangedListener(new EditChangedListener());
        final Button pwdLoginButton = (Button) findViewById(R.id.pwd_login_button);
        final Button QRcodeLoginButton = (Button) findViewById(R.id.qrcode_login_button);
        pwdLoginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                QRCodeActivity.this.setClickListener();
                pwdLoginButton.setBackgroundResource(R.drawable.yonghumingdenglu_icon_off);
                QRcodeLoginButton.setBackgroundResource(R.drawable.erweimadenglu_icon_on);
                View layout = QRCodeActivity.this.inflater.inflate(R.layout.login_by_qrcard, null);
                QRCodeActivity.this.lin.removeAllViews();
                QRCodeActivity.this.lin.addView(layout);
                QRCodeActivity.this.activityQRcodeInputType.setText(R.string.cardNum);
                QRCodeActivity.this.userNameEditText = (EditText) QRCodeActivity.this.lin.findViewById(R.id.userNameEditText);
                QRCodeActivity.this.userNameEditText.setText("");
                QRCodeActivity.this.userNameEditText.setInputType(0);
                QRCodeActivity.this.userNameEditText.addTextChangedListener(new EditChangedListener());
                QRCodeActivity.this.remindText.setVisibility(View.GONE);
                QRCodeActivity.this.inputQRcodeNumRemindText.setVisibility(View.VISIBLE);
                QRCodeActivity.this.inputQRcodeNumRemindText.setText(R.string.cardNum);
                QRCodeActivity.this.scanQRcodeConfirmBtn.setVisibility(View.VISIBLE);
                QRCodeActivity.this.initview();
                QRCodeActivity.this.setClickListener();
                QRCodeActivity.this.btnLogin = (Button) QRCodeActivity.this.findViewById(R.id.channelLoginButton);
                QRCodeActivity.this.btnLogin.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        SysGlobal.execute(new Runnable() {
                            public void run() {
                                QRCodeActivity.this.verifyQRCodeNum();
                            }
                        });
                    }
                });
                if (QRCodeActivity.this.hasCameraOpen) {
                    QRCodeActivity.this.cameraManager.closeDriver();
                }
                QRCodeActivity.this.hasCameraOpen = false;
                QRCodeActivity.this.showOrHideKeybordAndResetTime(QRCodeActivity.this.userNameEditText, QRCodeActivity.this.timeoutAction);
            }
        });
        QRcodeLoginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                pwdLoginButton.setBackgroundResource(R.drawable.yonghumingdenglu_icon_on);
                QRcodeLoginButton.setBackgroundResource(R.drawable.erweimadenglu_icon_off);
                View layout = QRCodeActivity.this.inflater.inflate(R.layout.login_by_qrcode, null);
                QRCodeActivity.this.lin.removeAllViews();
                QRCodeActivity.this.lin.addView(layout);
                QRCodeActivity.this.activityQRcodeInputType.setText(R.string.ScannerQRcode);
                QRCodeActivity.this.remindText.setVisibility(View.VISIBLE);
                QRCodeActivity.this.inputQRcodeNumRemindText.setVisibility(View.GONE);
                QRCodeActivity.this.scanQRcodeConfirmBtn.setVisibility(View.GONE);
                QRCodeActivity.this.hasCameraOpen = true;
                QRCodeActivity.this.pg = (ProgressBar) QRCodeActivity.this.lin.findViewById(R.id.pg_camera_diy);
                QRCodeActivity.this.iv_pg_bg_grey = (ImageView) QRCodeActivity.this.lin.findViewById(R.id.iv_camera_diy);
                QRCodeActivity.this.iv_big_circle = (ImageView) QRCodeActivity.this.lin.findViewById(R.id.iv_camera_diy_circle);
                QRCodeActivity.this.iv_four_corner = (ImageView) QRCodeActivity.this.lin.findViewById(R.id.iv_camera_diy_corner);
                QRCodeActivity.this.viewfinderView = (ViewfinderView) QRCodeActivity.this.lin.findViewById(R.id.viewfinder_view);
                QRCodeActivity.this.hasSurface = false;
                QRCodeActivity.this.surfaceView = (SurfaceView) QRCodeActivity.this.lin.findViewById(R.id.preview_view);
                QRCodeActivity.this.surfaceHolder = QRCodeActivity.this.surfaceView.getHolder();
                if (QRCodeActivity.this.hasSurface) {
                    QRCodeActivity.this.initCamera(QRCodeActivity.this.surfaceHolder);
                    return;
                }
                QRCodeActivity.this.surfaceHolder.addCallback(new SurfaceListener());
                QRCodeActivity.this.surfaceHolder.setType(3);
            }
        });
        ((Button) findViewById(R.id.scan_qrcode_return_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.REBATE_QRCODE_RETURN);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                if ("PAPER".equals(QRCodeActivity.this.PRODUCT_TYPE)) {
                    Intent intent = new Intent(QRCodeActivity.this, SelectRecycleActivity.class);
                    intent.putExtra("RECYCLE", "RECYCLEPAPER");
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    QRCodeActivity.this.startActivity(intent);
                    QRCodeActivity.this.finish();
                    return;
                }
                Intent intent = new Intent(QRCodeActivity.this, SelectRecycleActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                QRCodeActivity.this.startActivity(intent);
                QRCodeActivity.this.finish();
            }
        });
        this.btnLogin = (Button) findViewById(R.id.channelLoginButton);
        this.btnLogin.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!QRCodeActivity.this.isClick) {
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            QRCodeActivity.this.verifyQRCodeNum();
                        }
                    });
                }
            }
        });
        this.scanQRcodeConfirmBtn = (Button) findViewById(R.id.scan_qrcode_confirm_btn);
        this.scanQRcodeConfirmBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SysGlobal.execute(new Runnable() {
                    public void run() {
                        QRCodeActivity.this.verifyQRCodeNum();
                    }
                });
            }
        });
        this.remindText = (TextView) findViewById(R.id.scan_qrcode_remind_text);
        this.activityQRcodeInputType = (TextView) findViewById(R.id.activity_qrcode_inputType);
        this.inputQRcodeNumRemindText = (TextView) findViewById(R.id.input_qrcode_num_remind_text);
        this.pg = (ProgressBar) findViewById(R.id.pg_camera_diy);
        this.iv_pg_bg_grey = (ImageView) findViewById(R.id.iv_camera_diy);
        this.iv_big_circle = (ImageView) findViewById(R.id.iv_camera_diy_circle);
        this.iv_four_corner = (ImageView) findViewById(R.id.iv_camera_diy_corner);
        this.viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        this.hasSurface = false;
        initview();
        setClickListener();
    }

    protected void onStart() {
        super.onStart();
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
            this.soundPool.setOnLoadCompleteListener(this);
        }
        if (this.isPlaySounds && this.soundPool != null) {
            this.soundPool.load(this, R.raw.saomiao, 0);
        }
        this.isClick = false;
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.CAPTURE")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onPause() {
        super.onPause();
        if (this.hasCameraOpen) {
            this.cameraManager.closeDriver();
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
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void handleDecode(byte[] data, Bitmap barcode) {
        String resultString = new String(data);
        if (resultString.equals("")) {
            if (this.remindText != null) {
                this.remindText.setText("Scan failed!");
            }
        } else if (this.pg != null && this.pg.isShown()) {
            this.pg.setVisibility(8);
            this.iv_pg_bg_grey.setVisibility(0);
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
            String str = (String) guiCommonService.execute("GUIQRCodeCommonService", "verifyQRCode", hsmpParam).get("RET_QRCODE");
            if (str.equals("error_qrcard_num")) {
                if (this.isPlaySounds && this.soundPool != null) {
                    this.soundPool.load(this, R.raw.saomiaoshibai, 0);
                }
                if (this.remindText != null) {
                    this.remindText.setText(getString(R.string.warnQRError));
                }
                Toast toast = Toast.makeText(getBaseContext(), R.string.warnQRError, 0);
                toast.setGravity(17, 0, 0);
                toast.show();
                Intent intent;
                if ("PAPER".equals(this.PRODUCT_TYPE)) {
                    intent = new Intent();
                    intent.putExtra("RECYCLE", "RECYCLEPAPER");
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.setClass(this, SelectRecycleActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                intent = new Intent();
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.setClass(this, SelectRecycleActivity.class);
                startActivity(intent);
                finish();
            } else if (str.equals("right_qrcard_num")) {
                SysGlobal.execute(new Runnable() {
                    public void run() {
                        HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
                        HashMap<String, Object> VENDING_FLAG = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
                        Intent intent;
                        if (TRANSMIT_ADV != null) {
                            HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
                            if (HOMEPAGE_LEFT == null || StringUtils.isBlank((String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_PIC))) {
                                QRCodeActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{QRCodeActivity.this.getBaseActivity(), Integer.valueOf(2), "QRCODE"});
                                QRCodeActivity.this.finish();
                                return;
                            }
                            intent = new Intent(QRCodeActivity.this, ActivityAdActivity.class);
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            QRCodeActivity.this.startActivity(intent);
                            QRCodeActivity.this.finish();
                        } else if (VENDING_FLAG == null || StringUtils.isBlank((String) VENDING_FLAG.get(AllAdvertisement.VENDING_PIC))) {
                            QRCodeActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{QRCodeActivity.this.getBaseActivity(), Integer.valueOf(2), "QRCODE"});
                            QRCodeActivity.this.finish();
                        } else {
                            intent = new Intent(QRCodeActivity.this, ActivityAdActivity.class);
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            QRCodeActivity.this.startActivity(intent);
                            QRCodeActivity.this.finish();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    while (QRCodeActivity.this.cameraManager.isPreviewing()) {
                        Bitmap bitmap = QRCodeActivity.this.cameraManager.takePicture();
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
                                        QRCodeActivity.this.handleDecode((byte[]) paramObjs[0], (Bitmap) paramObjs[1]);
                                    }
                                };
                                QRCodeActivity.this.executeGUIAction(false, guiAction, new Object[]{qrCodeText.getBytes(), bitmap});
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

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (!this.hasSurface) {
            this.hasSurface = true;
            initCamera(holder);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.hasSurface = false;
        this.surfaceHolder = null;
        this.surfaceView = null;
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

    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        soundPlay(sampleId);
    }

    private void soundPlay(int soundId) {
        if (this.soundPool != null) {
            this.soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void initview() {
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
        this.btnClear = (Button) findViewById(R.id.qrcode_clear);
        this.iv_delete = (ImageView) findViewById(R.id.delete);
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
            case R.id.qrcode_clear:
                if (this.userNameEditText != null) {
                    this.userNameEditText.setText("");
                }
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                return;
            default:
                return;
        }
    }

    private void verifyQRCodeNum() {
        if (this.userNameEditText.getText().toString().trim().length() <= 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(QRCodeActivity.this, R.string.cardNum, 0).show();
                }
            });
        } else if (!this.isClick) {
            String resultString = this.userNameEditText.getText().toString().trim();
            GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
            HashMap<String, Object> hsmpParam = new HashMap();
            hsmpParam.put("QRCODE_NO", resultString);
            try {
                String str = (String) guiCommonService.execute("GUIQRCodeCommonService", "verifyQRCode", hsmpParam).get("RET_QRCODE");
                if (str.equals("error_qrcard_num")) {
                    this.userNameEditText.post(new Runnable() {
                        public void run() {
                            QRCodeActivity.this.userNameEditText.setText("");
                        }
                    });
                    this.inputQRcodeNumRemindText.post(new Runnable() {
                        public void run() {
                            QRCodeActivity.this.inputQRcodeNumRemindText.setText(R.string.scan_fail);
                        }
                    });
                } else if (str.equals("right_qrcard_num")) {
                    this.isClick = true;
                    HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
                    HashMap<String, Object> VENDING_FLAG = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
                    Intent intent;
                    if (TRANSMIT_ADV != null) {
                        HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
                        if (HOMEPAGE_LEFT == null || StringUtils.isBlank((String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_PIC))) {
                            executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{getBaseActivity(), Integer.valueOf(2), "QRCODE"});
                            finish();
                            return;
                        }
                        intent = new Intent(this, ActivityAdActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        finish();
                    } else if (VENDING_FLAG == null || StringUtils.isBlank((String) VENDING_FLAG.get(AllAdvertisement.VENDING_PIC))) {
                        executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{getBaseActivity(), Integer.valueOf(2), "QRCODE"});
                        finish();
                    } else {
                        intent = new Intent(this, ActivityAdActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        finish();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
