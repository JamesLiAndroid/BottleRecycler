package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
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

public class QueryTransactionCard extends BaseActivity {
    private String CODE;
    private String RVM_CODE;
    private String URL_CONFIG;
    private Button query_transaction_card_btnreturn;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) QueryTransactionCard.this.findViewById(R.id.query_transaction_card_number)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(QueryTransactionCard.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            QueryTransactionCard.this.startActivity(intent);
                            QueryTransactionCard.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            QueryTransactionCard.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private WebView web_query_transaction_card;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_transaction_card);
        backgroundToActivity();
        initview();
        WebSettings set = this.web_query_transaction_card.getSettings();
        set.setSavePassword(false);
        set.setSaveFormData(false);
        this.web_query_transaction_card.getSettings().setJavaScriptEnabled(true);
        this.web_query_transaction_card.getSettings().setLoadsImagesAutomatically(true);
        this.web_query_transaction_card.getSettings().setSupportZoom(true);
        this.web_query_transaction_card.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        this.web_query_transaction_card.loadUrl(StringUtils.replace(StringUtils.replace(this.URL_CONFIG, "$CODE$", this.CODE), "$RVM_CODE$", this.RVM_CODE));
        this.query_transaction_card_btnreturn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(QueryTransactionCard.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        QueryTransactionCard.this.startActivity(intent);
                        QueryTransactionCard.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.web_query_transaction_card.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                return true;
            }
        });
        this.web_query_transaction_card.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
        this.web_query_transaction_card.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                TimeoutTask.getTimeoutTask().reset(QueryTransactionCard.this.timeoutAction);
                return false;
            }
        });
    }

    private void initview() {
        this.URL_CONFIG = SysConfig.get("TRANSACTION.CARD.URL");
        this.CODE = getIntent().getStringExtra("QRCODE_NO");
        this.RVM_CODE = SysConfig.get("RVM.CODE");
        this.web_query_transaction_card = (WebView) findViewById(R.id.web_query_transaction_card);
        this.query_transaction_card_btnreturn = (Button) findViewById(R.id.query_transaction_card_btnreturn);
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
        this.web_query_transaction_card.onPause();
        ((TextView) findViewById(R.id.query_transaction_card_number)).setText("");
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.web_query_transaction_card != null) {
            this.web_query_transaction_card.destroy();
            this.web_query_transaction_card = null;
        }
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }
}
