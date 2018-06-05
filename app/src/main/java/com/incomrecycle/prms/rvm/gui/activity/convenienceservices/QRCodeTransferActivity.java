package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class QRCodeTransferActivity extends BaseActivity {
    private String AcceptedFinishText;
    private String CODE;
    private String RVM_CODE;
    private String URL_CONFIG;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) QRCodeTransferActivity.this.findViewById(R.id.transfer_number)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(QRCodeTransferActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            QRCodeTransferActivity.this.startActivity(intent);
                            QRCodeTransferActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            QRCodeTransferActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private Button transfer_Btnreturn;
    private WebView web_transfer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        backgroundToActivity();
        initview();
        this.web_transfer.getSettings().setJavaScriptEnabled(true);
        this.web_transfer.getSettings().setLoadsImagesAutomatically(true);
        this.web_transfer.getSettings().setSupportZoom(true);
        this.web_transfer.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        this.AcceptedFinishText = StringUtils.replace(StringUtils.replace(this.URL_CONFIG, "$CODE$", this.CODE), "$RVM_CODE$", this.RVM_CODE);
        this.web_transfer.loadUrl(this.AcceptedFinishText);
        this.transfer_Btnreturn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(QRCodeTransferActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        QRCodeTransferActivity.this.startActivity(intent);
                        QRCodeTransferActivity.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.web_transfer.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                return true;
            }
        });
        this.web_transfer.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
        this.web_transfer.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                TimeoutTask.getTimeoutTask().reset(QRCodeTransferActivity.this.timeoutAction);
                return false;
            }
        });
    }

    private void initview() {
        this.URL_CONFIG = SysConfig.get("TRANSFER.URL");
        this.CODE = getIntent().getStringExtra("QRCODE");
        this.RVM_CODE = SysConfig.get("RVM.CODE");
        this.web_transfer = (WebView) findViewById(R.id.web_transfer);
        this.transfer_Btnreturn = (Button) findViewById(R.id.transfer_Btnreturn);
    }

    protected void onStart() {
        super.onStart();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.MAINTAIN")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    protected void onStop() {
        super.onStop();
        this.web_transfer.onPause();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.web_transfer != null) {
            this.web_transfer.destroy();
            this.web_transfer = null;
        }
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }
}
