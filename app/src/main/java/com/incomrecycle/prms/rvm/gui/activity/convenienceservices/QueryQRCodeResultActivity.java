package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.NetworkStateMgr;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.common.SysDef.networkSts;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.text.DecimalFormat;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

@SuppressLint({"HandlerLeak"})
public class QueryQRCodeResultActivity extends BaseActivity {
    private String QRCODE_NO;
    private LinearLayout QRcodeFailInfo = null;
    private LinearLayout QRcodeRebateCountInfo = null;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            DecimalFormat df = new DecimalFormat("0.00");
            QueryQRCodeResultActivity.this.hsmpRecycledAmount = QueryQRCodeResultActivity.this.netWorkTestThread.getHsmpResult();
            if (QueryQRCodeResultActivity.this.hsmpRecycledAmount == null || QueryQRCodeResultActivity.this.hsmpRecycledAmount.size() <= 0) {
                QueryQRCodeResultActivity.this.QRcodeRebateCountInfo.setVisibility(View.GONE);
                QueryQRCodeResultActivity.this.QRcodeFailInfo.setVisibility(View.VISIBLE);
                QueryQRCodeResultActivity.this.qr_select_fail_warningtext.setText(R.string.QRcode_info_fail_text);
                return;
            }
            int CARD_STATUS = Integer.parseInt((String) QueryQRCodeResultActivity.this.hsmpRecycledAmount.get("CARD_STATUS"));
            if (CARD_STATUS == -1 || CARD_STATUS == -2) {
                QueryQRCodeResultActivity.this.QRcodeRebateCountInfo.setVisibility(View.GONE);
                QueryQRCodeResultActivity.this.QRcodeFailInfo.setVisibility(View.VISIBLE);
                QueryQRCodeResultActivity.this.qr_select_fail_warningtext.setText(R.string.qeCode_unuse);
                return;
            }
            double rebateTota = (Double.parseDouble((String) QueryQRCodeResultActivity.this.hsmpRecycledAmount.get("INCOM_AMOUNT")) / 100.0d) * ((double) Integer.parseInt(SysConfig.get("LOCAL_EXCHANGE_RATE")));
            double recharge = (Double.parseDouble((String) QueryQRCodeResultActivity.this.hsmpRecycledAmount.get("RECHARGE")) / 100.0d) * ((double) Integer.parseInt(SysConfig.get("LOCAL_EXCHANGE_RATE")));
            QueryQRCodeResultActivity.this.rebateTotalCon.setText(df.format(rebateTota) + "");
            QueryQRCodeResultActivity.this.recharge_text.setText(df.format(recharge) + "");
            String jiFen = (String) QueryQRCodeResultActivity.this.hsmpRecycledAmount.get("CREDIT");
            if (!StringUtils.isBlank(jiFen)) {
                QueryQRCodeResultActivity.this.findViewById(R.id.chaxun_qrcode_jifen_info).setVisibility(View.VISIBLE);
                ((TextView) QueryQRCodeResultActivity.this.findViewById(R.id.chaxun_qrcode_integration)).setText(jiFen);
            }
            String networkState = networkSts.NETWORK_STS;
            if (!StringUtils.isBlank(networkState) && NetworkStateMgr.NETWORK_SUCCESS.equalsIgnoreCase(networkState) && rebateTota > 0.0d && "TRUE".equalsIgnoreCase(SysConfig.get("DISABLE_LNK_CARD_ENABLED"))) {
                QueryQRCodeResultActivity.this.layout_visibility_btn.setVisibility(View.GONE);
                QueryQRCodeResultActivity.this.layout_gone_btn.setVisibility(View.VISIBLE);
                QueryQRCodeResultActivity.this.sureGone.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(QueryQRCodeResultActivity.this, QRCodeTransferActivity.class);
                        intent.putExtra("QRCODE", QueryQRCodeResultActivity.this.QRCODE_NO);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        QueryQRCodeResultActivity.this.startActivity(intent);
                        QueryQRCodeResultActivity.this.finish();
                    }
                });
            }
        }
    };
    private HashMap<String, Object> hsmpRecycledAmount;
    private HashMap<String, Object> hsmpResult;
    private LinearLayout layout_gone_btn;
    private LinearLayout layout_visibility_btn;
    private NetWorkTestThread netWorkTestThread;
    private TextView qr_select_fail_warningtext = null;
    private TextView rebateTotalCon = null;
    private TextView recharge_text = null;
    private Button sureGone;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) QueryQRCodeResultActivity.this.findViewById(R.id.query_qrcode_result_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(QueryQRCodeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            QueryQRCodeResultActivity.this.startActivity(intent);
                            QueryQRCodeResultActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            QueryQRCodeResultActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    class NetWorkTestThread implements Runnable {
        GUICommonService guiCommonService;
        HashMap<String, Object> hampResult;
        Handler handler;
        HashMap<String, Object> hsmpParam;

        public NetWorkTestThread(GUICommonService guiCommonService, HashMap<String, Object> hsmpParam, Handler handler) {
            this.handler = handler;
            this.guiCommonService = guiCommonService;
            this.hsmpParam = hsmpParam;
        }

        public void run() {
            try {
                QueryQRCodeResultActivity.this.hsmpResult = this.guiCommonService.execute("GUIQRCodeCommonService", "balanceQRCode", this.hsmpParam);
                this.handler.sendMessage(new Message());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public HashMap<String, Object> getHsmpResult() {
            return QueryQRCodeResultActivity.this.hsmpResult;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_query_qrcode_result);
        backgroundToActivity();
        initView();
    }

    public void initView() {
        this.layout_visibility_btn = (LinearLayout) findViewById(R.id.layout_visibility_btn);
        Button endVisibility = (Button) findViewById(R.id.query_visibility_end);
        this.layout_gone_btn = (LinearLayout) findViewById(R.id.layout_gone_btn);
        Button endGone = (Button) findViewById(R.id.query_gone_end);
        this.sureGone = (Button) findViewById(R.id.query_gone_sure);
        endVisibility.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.CONVENIENCESERVICE_QRCODE_END);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(QueryQRCodeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        QueryQRCodeResultActivity.this.startActivity(intent);
                        QueryQRCodeResultActivity.this.finish();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });
        endGone.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(QueryQRCodeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        QueryQRCodeResultActivity.this.startActivity(intent);
                        QueryQRCodeResultActivity.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.QRcodeRebateCountInfo = (LinearLayout) findViewById(R.id.qr_result_count_info);
        this.QRcodeFailInfo = (LinearLayout) findViewById(R.id.qr_result_fail_info);
        this.rebateTotalCon = (TextView) findViewById(R.id.rebate_total_con);
        this.recharge_text = (TextView) findViewById(R.id.recharge_text);
        this.qr_select_fail_warningtext = (TextView) findViewById(R.id.qr_select_fail_warningtext);
    }

    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        try {
            this.netWorkTestThread = new NetWorkTestThread(CommonServiceHelper.getGUICommonService(), JSONUtils.toHashMap(intent.getStringExtra("JSON")), this.handler);
            SysGlobal.execute(this.netWorkTestThread);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
        this.QRCODE_NO = (String) ServiceGlobal.getCurrentSession("CARD_NO");
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
