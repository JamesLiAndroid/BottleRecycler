package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class ConvenienceServicesActivity extends BaseActivity {
    private TextView hintTextView = null;
    private boolean isSelected = false;
    private String queryWay = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) ConvenienceServicesActivity.this.findViewById(R.id.conve_Time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(ConvenienceServicesActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            ConvenienceServicesActivity.this.startActivity(intent);
                            ConvenienceServicesActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            ConvenienceServicesActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_convenienceservices);
        backgroundToActivity();
        this.hintTextView = (TextView) findViewById(R.id.conve_remind_text);
        setClickListener();
    }

    protected void onStart() {
        super.onStart();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    private void setClickListener() {
        final Button btnQueryQRCode = (Button) findViewById(R.id.conve_btnqueryqrcode);
        final Button btnQueryOneCard = (Button) findViewById(R.id.conve_btnqueryonecard);
        ((Button) findViewById(R.id.conve_btnreturn)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.CONVENIENCESERVICE_RETURN);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                            try {
                                Intent intent = new Intent(ConvenienceServicesActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                ConvenienceServicesActivity.this.startActivity(intent);
                                ConvenienceServicesActivity.this.finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, 350);
            }
        });
        btnQueryOneCard.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.CONVENIENCESERVICE_ONECARD);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                ConvenienceServicesActivity.this.isSelected = true;
                ConvenienceServicesActivity.this.queryWay = "TRANSPORTCARD";
                if (ConvenienceServicesActivity.this.hintTextView != null) {
                    ConvenienceServicesActivity.this.hintTextView.setText(R.string.conve_yikatong_hinitInfo);
                }
                btnQueryOneCard.setBackgroundResource(R.drawable.conve_yikatong_btn_off);
                btnQueryQRCode.setBackgroundResource(R.drawable.conve_erweima_btn_on);
                ConvenienceServicesActivity.this.onClickItem();
            }
        });
        btnQueryQRCode.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.CONVENIENCESERVICE_QRCODE);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                ConvenienceServicesActivity.this.isSelected = true;
                ConvenienceServicesActivity.this.queryWay = "QRCODE";
                if (ConvenienceServicesActivity.this.hintTextView != null) {
                    ConvenienceServicesActivity.this.hintTextView.setText(R.string.conve_qrcode);
                }
                btnQueryOneCard.setBackgroundResource(R.drawable.conve_yikatong_btn_on);
                btnQueryQRCode.setBackgroundResource(R.drawable.conve_erweima_btn_off);
                ConvenienceServicesActivity.this.onClickItem();
            }
        });
    }

    public void onClickItem() {
        if (this.isSelected) {
            Intent intent;
            if (this.queryWay.equals("TRANSPORTCARD")) {
                intent = new Intent();
                intent.setClass(this, QueryOneCardActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
            if (this.queryWay.equals("QRCODE")) {
                intent = new Intent();
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.setClass(this, QueryQRCodeActivity.class);
                startActivity(intent);
                finish();
            }
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
}
