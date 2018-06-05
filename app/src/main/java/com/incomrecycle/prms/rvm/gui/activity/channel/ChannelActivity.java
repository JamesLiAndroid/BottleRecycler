package com.incomrecycle.prms.rvm.gui.activity.channel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
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
import com.incomrecycle.prms.rvm.common.NetworkStateMgr;
import com.incomrecycle.prms.rvm.common.QRDecodeHelper;
import com.incomrecycle.prms.rvm.common.ResolveCode;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.action.GUIActionMaintenanceLogin;
import com.incomrecycle.prms.rvm.gui.action.GUIActionQRCode;
import com.incomrecycle.prms.rvm.gui.camera.CameraManager;
import com.incomrecycle.prms.rvm.gui.camera.ViewfinderView;
import java.io.IOException;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

@SuppressLint({"ResourceAsColor"})
public class ChannelActivity extends BaseActivity {
    private Button btnLogin;
    private CameraManager cameraManager = new CameraManager();
    private boolean hasCameraOpen = false;
    private boolean hasSurface;
    private LayoutInflater infalter = null;
    private boolean isclick = false;
    private ImageView iv_big_circle;
    private ImageView iv_four_corner;
    private ImageView iv_pg_bg_grey;
    private LinearLayout lin = null;
    private ImageView networkStsView = null;
    private EditText passWordEditText = null;
    private ProgressBar pg;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) ChannelActivity.this.findViewById(R.id.login_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(ChannelActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            ChannelActivity.this.startActivity(intent);
                            ChannelActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            ChannelActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
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
            TimeoutTask.getTimeoutTask().reset(ChannelActivity.this.timeoutAction);
        }

        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            ChannelActivity.this.isclick = false;
        }
    }

    private class SurfaceListener implements Callback {
        private SurfaceListener() {
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        public void surfaceCreated(SurfaceHolder holder) {
            if (!ChannelActivity.this.hasSurface) {
                ChannelActivity.this.hasSurface = true;
                ChannelActivity.this.initCamera(holder);
            }
        }

        public void surfaceDestroyed(SurfaceHolder arg0) {
            ChannelActivity.this.hasSurface = false;
            ChannelActivity.this.surfaceHolder = null;
            ChannelActivity.this.surfaceView = null;
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.hasCameraOpen) {
            this.cameraManager.closeDriver();
        }
    }

    public void onStart() {
        super.onStart();
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "init", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.btnLogin != null) {
            this.btnLogin.setEnabled(true);
        }
        this.isclick = false;
        setNetworkState(NetworkStateMgr.getMgr().getNetworkState());
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.LOGIN")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_channel_login);
        this.networkStsView = (ImageView) findViewById(R.id.network_sts);
        this.infalter = LayoutInflater.from(this);
        this.lin = (LinearLayout) findViewById(R.id.login_type_layout);
        this.lin.addView(this.infalter.inflate(R.layout.login_by_password, null));
        this.userNameEditText = (EditText) findViewById(R.id.userNameEditText);
        this.passWordEditText = (EditText) findViewById(R.id.passWordEditText);
        this.userNameEditText.setText("");
        this.passWordEditText.setText("");
        this.userNameEditText.addTextChangedListener(new EditChangedListener());
        this.passWordEditText.addTextChangedListener(new EditChangedListener());
        final Button pwdLoginButton = (Button) findViewById(R.id.pwd_login_button);
        final Button QRcodeLoginButton = (Button) findViewById(R.id.qrcode_login_button);
        pwdLoginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                pwdLoginButton.setBackgroundResource(R.drawable.yonghumingdenglu_icon_off);
                QRcodeLoginButton.setBackgroundResource(R.drawable.erweimadenglu_icon_on);
                View layout = ChannelActivity.this.infalter.inflate(R.layout.login_by_password, null);
                ChannelActivity.this.lin.removeAllViews();
                ChannelActivity.this.lin.addView(layout);
                ChannelActivity.this.userNameEditText = (EditText) ChannelActivity.this.findViewById(R.id.userNameEditText);
                ChannelActivity.this.passWordEditText = (EditText) ChannelActivity.this.findViewById(R.id.passWordEditText);
                ChannelActivity.this.userNameEditText.setText("");
                ChannelActivity.this.passWordEditText.setText("");
                ChannelActivity.this.userNameEditText.addTextChangedListener(new EditChangedListener());
                ChannelActivity.this.passWordEditText.addTextChangedListener(new EditChangedListener());
                ChannelActivity.this.btnLogin = (Button) ChannelActivity.this.findViewById(R.id.channelLoginButton);
                ChannelActivity.this.btnLogin.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (ChannelActivity.this.userNameEditText.getText().toString().trim().length() <= 0 || ChannelActivity.this.passWordEditText.getText().toString().trim().length() <= 0) {
                            Toast.makeText(ChannelActivity.this, R.string.channelLoginNotNull, Toast.LENGTH_SHORT).show();
                        } else if (!ChannelActivity.this.isclick) {
                            ChannelActivity.this.isclick = true;
                            ChannelActivity.this.executeGUIAction(true, new GUIActionMaintenanceLogin(), new Object[]{ChannelActivity.this, null, "1", null});
                        }
                    }
                });
                if (ChannelActivity.this.hasCameraOpen) {
                    ChannelActivity.this.cameraManager.closeDriver();
                }
                ChannelActivity.this.hasCameraOpen = false;
                ChannelActivity.this.showOrHideKeybordAndResetTime(ChannelActivity.this.passWordEditText, ChannelActivity.this.timeoutAction);
                ChannelActivity.this.passWordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
        QRcodeLoginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // ChannelActivity.this.cameraManager;
                CameraManager.clearCameraManager(Integer.parseInt(SysConfig.get("CAMERA.INDEX.QRCODE")));
                pwdLoginButton.setBackgroundResource(R.drawable.yonghumingdenglu_icon_on);
                QRcodeLoginButton.setBackgroundResource(R.drawable.erweimadenglu_icon_off);
                View layout = ChannelActivity.this.infalter.inflate(R.layout.login_by_qrcode, null);
                ChannelActivity.this.lin.removeAllViews();
                ChannelActivity.this.lin.addView(layout);
                ChannelActivity.this.hasCameraOpen = true;
                ChannelActivity.this.pg = (ProgressBar) ChannelActivity.this.findViewById(R.id.pg_camera_diy);
                ChannelActivity.this.iv_pg_bg_grey = (ImageView) ChannelActivity.this.findViewById(R.id.iv_camera_diy);
                ChannelActivity.this.iv_big_circle = (ImageView) ChannelActivity.this.findViewById(R.id.iv_camera_diy_circle);
                ChannelActivity.this.iv_four_corner = (ImageView) ChannelActivity.this.findViewById(R.id.iv_camera_diy_corner);
                ChannelActivity.this.viewfinderView = (ViewfinderView) ChannelActivity.this.findViewById(R.id.viewfinder_view);
                ChannelActivity.this.hasSurface = false;
                ChannelActivity.this.surfaceView = (SurfaceView) ChannelActivity.this.findViewById(R.id.preview_view);
                ChannelActivity.this.surfaceHolder = ChannelActivity.this.surfaceView.getHolder();
                if (ChannelActivity.this.hasSurface) {
                    ChannelActivity.this.initCamera(ChannelActivity.this.surfaceHolder);
                    return;
                }
                ChannelActivity.this.surfaceHolder.addCallback(new SurfaceListener());
                ChannelActivity.this.surfaceHolder.setType(3);
            }
        });
        ((Button) findViewById(R.id.login_return_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!ChannelActivity.this.isclick) {
                    ChannelActivity.this.isclick = true;
                    if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(ChannelActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            ChannelActivity.this.startActivity(intent);
                            ChannelActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        this.btnLogin = (Button) findViewById(R.id.channelLoginButton);
        this.btnLogin.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ChannelActivity.this.userNameEditText.getText().toString().trim().length() <= 0 || ChannelActivity.this.passWordEditText.getText().toString().trim().length() <= 0) {
                    Toast.makeText(ChannelActivity.this, R.string.channelLoginNotNull, Toast.LENGTH_SHORT).show();
                } else if (!ChannelActivity.this.isclick) {
                    ChannelActivity.this.isclick = true;
                    ChannelActivity.this.executeGUIAction(true, new GUIActionMaintenanceLogin(), new Object[]{ChannelActivity.this, null, "1", null});
                }
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        return super.onTouchEvent(event);
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
        String EVENT = (String) hsmpEvent.get("EVENT");
        if ("LOGIN".equalsIgnoreCase(EVENT)) {
            HashMap<String, Object> RESULT = (HashMap) hsmpEvent.get("RESULT");
            if (RESULT == null || !"success".equalsIgnoreCase((String) RESULT.get("RET_CODE"))) {
                this.isclick = false;
            }
            executeGUIAction(true, new GUIActionMaintenanceLogin(), new Object[]{this, null, "2", RESULT});
        }
        if ("NETWORKSTATE".equalsIgnoreCase(EVENT)) {
            setNetworkState((String) hsmpEvent.get("NETWORKSTATE"));
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
                    while (ChannelActivity.this.cameraManager.isPreviewing()) {
                        Bitmap bitmap = ChannelActivity.this.cameraManager.takePicture();
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
                                        ChannelActivity.this.handleDecode((byte[]) paramObjs[0], (Bitmap) paramObjs[1]);
                                    }
                                };
                                ChannelActivity.this.executeGUIAction(false, guiAction, new Object[]{qrCodeText.getBytes(), bitmap});
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
        executeGUIAction(true, new GUIActionQRCode(), new Object[]{this, resultString});
        if (barcode != null) {
            barcode.recycle();
        }
    }

    public ViewfinderView getViewfinderView() {
        return this.viewfinderView;
    }

    public void drawViewfinder() {
        this.viewfinderView.drawViewfinder();
    }

    protected void onResume() {
        super.onResume();
        this.isclick = false;
    }

    private void setNetworkState(String netState) {
        if (NetworkStateMgr.NETWORK_ERROR.equals(netState)) {
            this.networkStsView.setBackgroundResource(R.drawable.network_error);
        } else if (NetworkStateMgr.NETWORK_FAILED.equals(netState)) {
            this.networkStsView.setBackgroundResource(R.drawable.network_failed);
        } else if (NetworkStateMgr.NETWORK_SUCCESS.equals(netState)) {
            this.networkStsView.setBackgroundResource(R.drawable.network_success);
        } else {
            this.networkStsView.setBackgroundResource(R.drawable.network_unknown);
        }
    }
}
