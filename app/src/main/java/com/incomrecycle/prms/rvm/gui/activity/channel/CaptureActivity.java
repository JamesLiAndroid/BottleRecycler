package com.incomrecycle.prms.rvm.gui.activity.channel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
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
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.action.GUIActionQRCode;
import com.incomrecycle.prms.rvm.gui.camera.CameraManager;
import com.incomrecycle.prms.rvm.gui.camera.ViewfinderView;

import java.io.IOException;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class CaptureActivity extends BaseActivity implements Callback {
    private CameraManager cameraManager = new CameraManager();
    private Button end;
    private boolean hasSurface;
    private ImageView iv_big_circle;
    private ImageView iv_four_corner;
    private ImageView iv_pg_bg_grey;
    private Button passWordLoginBtn;
    private ProgressBar pg;
    private SurfaceHolder surfaceHolder;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        Intent intent = new Intent();
                        intent.setClass(CaptureActivity.this, ChannelActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        CaptureActivity.this.startActivity(intent);
                        CaptureActivity.this.finish();
                        return;
                    }
                    ((TextView) CaptureActivity.this.findViewById(R.id.showNumText)).setText("" + remainedSeconds);
                }
            };
            CaptureActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private ViewfinderView viewfinderView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.camera_diy);
        this.passWordLoginBtn = (Button) findViewById(R.id.userName_login);
        this.passWordLoginBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.setClass(CaptureActivity.this, ChannelActivity.class);
                CaptureActivity.this.startActivity(intent);
                CaptureActivity.this.finish();
            }
        });
        this.end = (Button) findViewById(R.id.back);
        this.end.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(CaptureActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        CaptureActivity.this.startActivity(intent);
                        CaptureActivity.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.pg = (ProgressBar) findViewById(R.id.pg_camera_diy);
        this.iv_pg_bg_grey = (ImageView) findViewById(R.id.iv_camera_diy);
        this.iv_big_circle = (ImageView) findViewById(R.id.iv_camera_diy_circle);
        this.iv_four_corner = (ImageView) findViewById(R.id.iv_camera_diy_corner);
        this.viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        this.hasSurface = false;
    }

    protected void onStart() {
        super.onStart();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.CAPTURE")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onResume() {
        super.onResume();
        this.surfaceHolder = ((SurfaceView) findViewById(R.id.preview_view)).getHolder();
        if (this.hasSurface) {
            initCamera(this.surfaceHolder);
            return;
        }
        this.surfaceHolder.addCallback(this);
        this.surfaceHolder.setType(3);
    }

    protected void onPause() {
        super.onPause();
        this.cameraManager.closeDriver();
    }

    public void finish() {
        super.finish();
        ((TextView) findViewById(R.id.showNumText)).setText("");
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
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
        executeGUIAction(true, new GUIActionQRCode(), new Object[] {this, resultString});
        if (barcode != null) {
            barcode.recycle();
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
                    while (CaptureActivity.this.cameraManager.isPreviewing()) {
                        Bitmap bitmap = CaptureActivity.this.cameraManager.takePicture();
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
                                        CaptureActivity.this.handleDecode((byte[]) paramObjs[0], (Bitmap) paramObjs[1]);
                                    }
                                };
                                CaptureActivity.this.executeGUIAction(false, guiAction, new Object[]{qrCodeText.getBytes(), bitmap});
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
}
