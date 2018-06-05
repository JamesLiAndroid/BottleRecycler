package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
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
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.NetworkStateMgr;
import com.incomrecycle.prms.rvm.common.QRDecodeHelper;
import com.incomrecycle.prms.rvm.common.ResolveCode;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.common.SysDef.networkSts;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.camera.CameraManager;
import com.incomrecycle.prms.rvm.gui.camera.ViewfinderView;
import java.io.IOException;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class QueryQRCodeActivity extends BaseActivity implements Callback, OnLoadCompleteListener {
    private String QRCODE_NO;
    private HashMap<String, Object> QRCodehashmap;
    private CameraManager cameraManager = new CameraManager();
    private boolean hasSurface;
    private HashMap<String, Object> hsmpParam;
    private boolean isPlaySounds;
    private ImageView iv_big_circle;
    private ImageView iv_four_corner;
    private ImageView iv_pg_bg_grey;
    private ProgressBar pg;
    private TextView remindText = null;
    private SoundPool soundPool = null;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) QueryQRCodeActivity.this.findViewById(R.id.query_qrcode_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(QueryQRCodeActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            QueryQRCodeActivity.this.startActivity(intent);
                            QueryQRCodeActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            QueryQRCodeActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private ViewfinderView viewfinderView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_query_qrcode);
        backgroundToActivity();
        ((Button) findViewById(R.id.query_qrcode_return_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.CONVENIENCESERVICE_QRCODE_RETURN);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(QueryQRCodeActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        QueryQRCodeActivity.this.startActivity(intent);
                        QueryQRCodeActivity.this.finish();
                    } catch (Exception e2) {
                        e2.printStackTrace();
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
        this.remindText = (TextView) findViewById(R.id.query_qrcode_remind_text);
    }

    protected void onStart() {
        super.onStart();
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(10, 3, 0);
            this.soundPool.setOnLoadCompleteListener(this);
        }
        if (this.isPlaySounds && this.soundPool != null) {
            this.soundPool.load(this, R.raw.saomiao, 0);
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.CAPTURE")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onResume() {
        super.onResume();
        this.surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        this.surfaceHolder = this.surfaceView.getHolder();
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
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void handleDecode(byte[] data, Bitmap barcode) {
        String resultString = new String(data);
        if (resultString.equals("")) {
            if (this.remindText != null) {
                this.remindText.setText("Scan failed!");
            }
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
        this.hsmpParam = new HashMap();
        this.hsmpParam.put("QRCODE_NO", resultString);
        try {
            String str = (String) guiCommonService.execute("GUIQRCodeCommonService", "verifyQRCode", this.hsmpParam).get("RET_QRCODE");
            if (str.equals("error_qrcard_num")) {
                if (this.remindText != null) {
                    this.remindText.setText(R.string.warnQRError);
                }
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (str.equals("right_qrcard_num")) {
                SysGlobal.execute(new Runnable() {
                    public void run() {
                        GUICommonService guiQRCodeCommonService = CommonServiceHelper.getGUICommonService();
                        QueryQRCodeActivity.this.QRCODE_NO = (String) QueryQRCodeActivity.this.hsmpParam.get("QRCODE_NO");
                        HashMap hsmpStatusParam = new HashMap();
                        hsmpStatusParam.put("QRCODE_NO", QueryQRCodeActivity.this.QRCODE_NO);
                        try {
                            QueryQRCodeActivity.this.QRCodehashmap = guiQRCodeCommonService.execute("GUIQRCodeCommonService", "balanceQRCode", hsmpStatusParam);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (QueryQRCodeActivity.this.QRCodehashmap == null) {
                            Intent intent = new Intent();
                            intent.setClass(QueryQRCodeActivity.this, QueryQRCodeResultActivity.class);
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent.putExtra("JSON", JSONUtils.toJSON(QueryQRCodeActivity.this.hsmpParam));
                            QueryQRCodeActivity.this.startActivity(intent);
                            QueryQRCodeActivity.this.finish();
                            return;
                        }
                        int QRCODE_STATUS = Integer.valueOf((String) QueryQRCodeActivity.this.QRCodehashmap.get("CARD_STATUS")).intValue();
                        if (QRCODE_STATUS == 2) {
                            Intent intent = new Intent();
                            intent.setClass(QueryQRCodeActivity.this, QueryQRCodeResultActivity.class);
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent.putExtra("JSON", JSONUtils.toJSON(QueryQRCodeActivity.this.hsmpParam));
                            QueryQRCodeActivity.this.startActivity(intent);
                            QueryQRCodeActivity.this.finish();
                        }
                        String networkState = networkSts.NETWORK_STS;
                        if (QRCODE_STATUS != -2 || !NetworkStateMgr.NETWORK_SUCCESS.equalsIgnoreCase(networkState)) {
                            Intent intent = new Intent();
                            intent.setClass(QueryQRCodeActivity.this, QueryQRCodeResultActivity.class);
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent.putExtra("JSON", JSONUtils.toJSON(QueryQRCodeActivity.this.hsmpParam));
                            QueryQRCodeActivity.this.startActivity(intent);
                            QueryQRCodeActivity.this.finish();
                        } else if ("TRUE".equalsIgnoreCase(SysConfig.get("DISABLE_LNK_CARD_ENABLED"))) {
                            Intent intent = new Intent(QueryQRCodeActivity.this, QueryTransactionCard.class);
                            intent.putExtra("QRCODE_NO", QueryQRCodeActivity.this.QRCODE_NO);
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            QueryQRCodeActivity.this.startActivity(intent);
                            QueryQRCodeActivity.this.finish();
                        } else if ("FALSE".equalsIgnoreCase(SysConfig.get("DISABLE_LNK_CARD_ENABLED"))) {
                            Intent intent = new Intent();
                            intent.setClass(QueryQRCodeActivity.this, QueryQRCodeResultActivity.class);
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent.putExtra("JSON", JSONUtils.toJSON(QueryQRCodeActivity.this.hsmpParam));
                            QueryQRCodeActivity.this.startActivity(intent);
                            QueryQRCodeActivity.this.finish();
                        }
                        if (QRCODE_STATUS == -1) {
                            Toast.makeText(QueryQRCodeActivity.this, QueryQRCodeActivity.this.getResources().getString(R.string.toast_card_unuse), Toast.LENGTH_SHORT).show();
                            if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                                try {
                                    Intent intent = new Intent(QueryQRCodeActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    QueryQRCodeActivity.this.startActivity(intent);
                                    QueryQRCodeActivity.this.finish();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            this.cameraManager.openDriver(Integer.parseInt(SysConfig.get("CAMERA.INDEX.QRCODE")), surfaceHolder);
            this.cameraManager.startPreview();
            SysGlobal.execute(new Thread() {
                public void run() {
                    GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                    try {
                        guiCommonService.execute("GUICameraCommonService", "cameraStart", null);
                    } catch (Exception e) {
                    }
                    while (QueryQRCodeActivity.this.cameraManager.isPreviewing()) {
                        Bitmap bitmap = QueryQRCodeActivity.this.cameraManager.takePicture();
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
                                        QueryQRCodeActivity.this.handleDecode((byte[]) paramObjs[0], (Bitmap) paramObjs[1]);
                                    }
                                };
                                QueryQRCodeActivity.this.executeGUIAction(false, guiAction, new Object[]{qrCodeText.getBytes(), bitmap});
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
}
