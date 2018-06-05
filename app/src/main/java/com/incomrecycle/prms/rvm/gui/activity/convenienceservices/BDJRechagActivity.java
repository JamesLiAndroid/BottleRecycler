package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardCommEntity.ErrorCode;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class BDJRechagActivity extends BaseActivity {
    private static final int RECHARGE_FAILED = 1;
    private static final int RECHARGE_SUEEUSS = 2;
    private static String hostResponseCodeText = null;
    private static String modelResponseCodeText = null;
    private static String serverResponseCodeText = null;
    private int GGKNO = 0;
    private int allmoney;
    private LinearLayout bdj_ggk_recharge_layout = null;
    private TextView bi;
    private TextView cardmoney;
    private Button endBtn;
    private List ggkMapList = new ArrayList();
    private LinearLayout ggk_info_show_layout = null;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    HashMap failedMap = (HashMap) msg.obj;
                    String failedInfo = (String) failedMap.get("INFO");
                    String reason = (String) failedMap.get("REASON");
                    if (!StringUtils.isBlank(reason) && "NOT_ENOUGH".equalsIgnoreCase(reason)) {
                        failedInfo = BDJRechagActivity.this.getString(R.string.onecard_balance_not_enough);
                    }
                    Toast filedToast = Toast.makeText(BDJRechagActivity.this.getBaseContext(), failedInfo, 0);
                    filedToast.setGravity(17, 0, 0);
                    filedToast.show();
                    BDJRechagActivity.this.handleRechargeResult(1);
                    return;
                case 2:
                    Toast successToast = Toast.makeText(BDJRechagActivity.this.getBaseContext(), R.string.recharge_success, 0);
                    successToast.setGravity(17, 0, 0);
                    successToast.show();
                    BDJRechagActivity.this.handleRechargeResult(2);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean isclick = false;
    private LinearLayout no_ggk_info_layout = null;
    private ProgressBar progressBar = null;
    private Button recharge_ing;
    private TextView recharge_now;
    private Button returnBtn;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        BDJRechagActivity.this.goBack();
                    } else {
                        ((TextView) BDJRechagActivity.this.findViewById(R.id.bdj_GGK_time)).setText("" + remainedSeconds);
                    }
                }
            };
            BDJRechagActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TextView wait_recharge;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bdjrechage_activity);
        initView();
        backgroundToActivity();
    }

    protected void onStart() {
        super.onStart();
        try {
            this.cardmoney.setText((String) GUIGlobal.getCurrentSession("cardBalance"));
            SysGlobal.execute(new Runnable() {
                public void run() {
                    HashMap<String, Object> hashMap = new HashMap();
                    try {
                        hashMap = CommonServiceHelper.getGUICommonService().execute("GUITrafficCardCommonService", "QueryGGK", null);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    HashMap<String, Object> GGkmap = (HashMap) hashMap.get("GGK_MAP");
                    if (GGkmap != null && GGkmap.size() > 0) {
                        for (Entry<String, Object> entry : GGkmap.entrySet()) {
                            String[][] ggkArray = (String[][]) Array.newInstance(String.class, new int[]{1, 2});
                            ggkArray[0][0] = (String) entry.getKey();
                            ggkArray[0][1] = String.valueOf(entry.getValue());
                            BDJRechagActivity.this.ggkMapList.add(ggkArray);
                            BDJRechagActivity.this.allmoney = BDJRechagActivity.this.allmoney + Integer.parseInt(ggkArray[0][1]);
                        }
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            BDJRechagActivity.this.wait_recharge.setText(String.valueOf(BDJRechagActivity.this.allmoney / 100));
                            BDJRechagActivity.this.ggkMapList = BDJRechagActivity.this.sortByList(BDJRechagActivity.this.ggkMapList);
                            if (BDJRechagActivity.this.ggkMapList == null || BDJRechagActivity.this.ggkMapList.size() <= 0) {
                                BDJRechagActivity.this.ggk_info_show_layout.setVisibility(View.GONE);
                                BDJRechagActivity.this.bdj_ggk_recharge_layout.setVisibility(View.GONE);
                                BDJRechagActivity.this.no_ggk_info_layout.setVisibility(View.VISIBLE);
                                return;
                            }
                            BDJRechagActivity.this.bi.setText(String.valueOf(BDJRechagActivity.this.ggkMapList.size()));
                            try {
                                BDJRechagActivity.this.recharge_now.setText(String.valueOf(Integer.parseInt(((String[][]) BDJRechagActivity.this.ggkMapList.get(0))[0][1]) / 100));
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.ONECARDRECHARGE")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onStop() {
        super.onStop();
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    private void handleRechargeResult(int rechargeStatus) {
        if (2 == rechargeStatus) {
            try {
                CommonServiceHelper.getGUICommonService().execute("GUITrafficCardCommonService", "readOneCard", null);
                HashMap balanceMap = CommonServiceHelper.getGUICommonService().execute("GUITrafficCardCommonService", "queryOneCardBalance", null);
                if (balanceMap != null && balanceMap.size() > 0) {
                    this.cardmoney.setText(new DecimalFormat("0.00").format((double) (Float.parseFloat((String) balanceMap.get("Balance")) / 100.0f)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.GGKNO++;
        if (this.GGKNO >= this.ggkMapList.size()) {
            this.ggk_info_show_layout.setVisibility(View.GONE);
            this.bdj_ggk_recharge_layout.setVisibility(View.GONE);
            this.no_ggk_info_layout.setVisibility(View.VISIBLE);
        } else {
            this.bi.setText(String.valueOf(this.ggkMapList.size() - this.GGKNO));
            try {
                this.recharge_now.setText(String.valueOf(Integer.parseInt(((String[][]) this.ggkMapList.get(this.GGKNO))[0][1]) / 100));
                int waitRechargeMoney = 0;
                for (int i = this.GGKNO; i < this.ggkMapList.size(); i++) {
                    waitRechargeMoney += Integer.parseInt(((String[][]) this.ggkMapList.get(this.GGKNO))[0][1]);
                }
                this.wait_recharge.setText(String.valueOf(waitRechargeMoney / 100));
            } catch (Exception e2) {
            }
        }
        this.endBtn.setEnabled(true);
        this.returnBtn.setEnabled(true);
        this.progressBar.setVisibility(8);
        this.recharge_ing.setBackgroundResource(R.drawable.this_recharge_btn);
    }

    private void initView() {
        this.cardmoney = (TextView) findViewById(R.id.cardmoney);
        this.wait_recharge = (TextView) findViewById(R.id.wait_recharge);
        this.recharge_now = (TextView) findViewById(R.id.recharge_now);
        this.bi = (TextView) findViewById(R.id.bi);
        this.no_ggk_info_layout = (LinearLayout) findViewById(R.id.no_ggk_info_layout);
        this.ggk_info_show_layout = (LinearLayout) findViewById(R.id.ggk_info_show_layout);
        this.bdj_ggk_recharge_layout = (LinearLayout) findViewById(R.id.bdj_ggk_recharge_layout);
        this.recharge_ing = (Button) findViewById(R.id.recharge_ing);
        this.recharge_ing.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!BDJRechagActivity.this.isclick && BDJRechagActivity.this.ggkMapList != null && BDJRechagActivity.this.ggkMapList.size() > 0) {
                    BDJRechagActivity.this.isclick = true;
                    BDJRechagActivity.this.progressBar.setVisibility(View.VISIBLE);
                    BDJRechagActivity.this.recharge_ing.setBackgroundResource(R.drawable.btn_chongzhizhong);
                    BDJRechagActivity.this.endBtn.setEnabled(false);
                    BDJRechagActivity.this.returnBtn.setEnabled(false);
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            String[][] frist = (String[][]) BDJRechagActivity.this.ggkMapList.get(BDJRechagActivity.this.GGKNO);
                            HashMap parmMap = new HashMap();
                            parmMap.put("CardPwd", frist[0][0]);
                            parmMap.put("Balance", frist[0][1]);
                            try {
                                HashMap hsmp = CommonServiceHelper.getGUICommonService().execute("GUITrafficCardCommonService", "ChargeGGK", parmMap);
                                String RET_CODE = null;
                                if (hsmp != null) {
                                    RET_CODE = (String) hsmp.get("RET_CODE");
                                }
                                BDJRechagActivity.this.isclick = false;
                                if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase(RET_CODE)) {
                                    BDJRechagActivity.this.handler.sendMessage(BDJRechagActivity.this.handler.obtainMessage(2));
                                    return;
                                }
                                String errorType = ErrorCode.MODEL_RESPONSECODE;
                                String str = (String) hsmp.get(ErrorCode.MODEL_RESPONSECODE);
                                if (StringUtils.isBlank(str)) {
                                    errorType = "HOST_RESPONSECODE";
                                    str = (String) hsmp.get("HOST_RESPONSECODE");
                                }
                                if (StringUtils.isBlank(str)) {
                                    errorType = "SERVER_RESPONSECODE";
                                    str = (String) hsmp.get("SERVER_RESPONSECODE");
                                }
                                if (StringUtils.isBlank(str)) {
                                    errorType = "RVM_RESPONSECODE";
                                    str = (String) hsmp.get("RVM_RESPONSECODE");
                                }
                                if (StringUtils.isBlank(str)) {
                                    errorType = "RVM_RESPONSECODE";
                                    str = NetworkUtils.NET_STATE_UNKNOWN;
                                }
                                String reason = null;
                                if ("NOT_ENOUGH".equalsIgnoreCase(RET_CODE)) {
                                    reason = RET_CODE;
                                }
                                String failedInfo = BDJRechagActivity.this.getTransCardErrorCode(errorType, str);
                                Message failedMsg = BDJRechagActivity.this.handler.obtainMessage();
                                failedMsg.what = 1;
                                HashMap failedMap = new HashMap();
                                failedMap.put("INFO", failedInfo);
                                failedMap.put("REASON", reason);
                                failedMsg.obj = failedMap;
                                BDJRechagActivity.this.handler.sendMessage(failedMsg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
        this.returnBtn = (Button) findViewById(R.id.bdj_GGK_return_btn);
        this.returnBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BDJRechagActivity.this.goBack();
            }
        });
        this.endBtn = (Button) findViewById(R.id.bdj_GGK_end_btn);
        this.endBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(BDJRechagActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        BDJRechagActivity.this.startActivity(intent);
                        BDJRechagActivity.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.progressBar = (ProgressBar) findViewById(R.id.pb_progressbar);
    }

    private void goBack() {
        Intent intent = new Intent(this, OneCardRechargeHintActivity.class);
        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    public String getTransCardErrorCode(String errorType, String errorCode) {
        String codeDesc;
        int idxStart;
        int idxEnd;
        String type;
        if (ErrorCode.MODEL_RESPONSECODE.equalsIgnoreCase(errorType)) {
            codeDesc = null;
            try {
                if (StringUtils.isBlank(modelResponseCodeText)) {
                    modelResponseCodeText = new String(IOUtils.readResource("code/modelrescode.txt"), "UTF-8").toUpperCase();
                }
                if (!StringUtils.isBlank(modelResponseCodeText)) {
                    idxStart = modelResponseCodeText.indexOf(errorCode + "=");
                    if (idxStart != -1) {
                        idxEnd = modelResponseCodeText.indexOf(10, idxStart);
                        codeDesc = idxEnd == -1 ? modelResponseCodeText.substring(idxStart + 5).trim() : modelResponseCodeText.substring(idxStart + 5, idxEnd).trim();
                    }
                }
            } catch (Exception e) {
            }
            type = getString(R.string.model_responsecode);
            if (StringUtils.isBlank(codeDesc)) {
                return type + " : " + errorCode;
            }
            return type + " : " + errorCode + "(" + codeDesc + ")";
        } else if ("HOST_RESPONSECODE".equalsIgnoreCase(errorType)) {
            if (errorCode.length() == 1) {
                errorCode = "0" + errorCode;
            }
            codeDesc = null;
            try {
                if (StringUtils.isBlank(hostResponseCodeText)) {
                    hostResponseCodeText = new String(IOUtils.readResource("code/hostrescode.txt"), "UTF-8");
                }
                if (!StringUtils.isBlank(hostResponseCodeText)) {
                    idxStart = hostResponseCodeText.indexOf(errorCode + "=");
                    if (idxStart != -1) {
                        idxEnd = hostResponseCodeText.indexOf(10, idxStart);
                        codeDesc = idxEnd == -1 ? hostResponseCodeText.substring(idxStart + 3).trim() : hostResponseCodeText.substring(idxStart + 3, idxEnd).trim();
                    }
                }
            } catch (Exception e2) {
            }
            type = getString(R.string.model_hostresponsecode);
            if (StringUtils.isBlank(codeDesc)) {
                return type + " : " + errorCode;
            }
            return type + " : " + errorCode + "(" + codeDesc + ")";
        } else if ("SERVER_RESPONSECODE".equalsIgnoreCase(errorType)) {
            if (errorCode.length() == 1) {
                errorCode = "0" + errorCode;
            }
            codeDesc = null;
            try {
                if (StringUtils.isBlank(serverResponseCodeText)) {
                    serverResponseCodeText = new String(IOUtils.readResource("code/hostrescode.txt"), "UTF-8");
                }
                if (!StringUtils.isBlank(serverResponseCodeText)) {
                    idxStart = serverResponseCodeText.indexOf(errorCode + "=");
                    if (idxStart != -1) {
                        idxEnd = serverResponseCodeText.indexOf(10, idxStart);
                        codeDesc = idxEnd == -1 ? serverResponseCodeText.substring(idxStart + 3).trim() : serverResponseCodeText.substring(idxStart + 3, idxEnd).trim();
                    }
                }
            } catch (Exception e3) {
            }
            type = getString(R.string.server_response);
            if (StringUtils.isBlank(codeDesc)) {
                return type + " : " + errorCode;
            }
            return type + " : " + errorCode + "(" + codeDesc + ")";
        } else if (!"RVM_RESPONSECODE".equalsIgnoreCase(errorType)) {
            return "";
        } else {
            if ("MODEL_NORESPONSE".equalsIgnoreCase(errorCode) || "MODEL_UNKNOWN".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_model_error);
            }
            if ("SERVER_LOST".equalsIgnoreCase(errorCode) || "SERVER_NORESPONSE".equalsIgnoreCase(errorCode) || "SERVER_COMMERROR".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_server_error);
            }
            if ("CARD_NOTFOUND".equalsIgnoreCase(errorCode)) {
                return StringUtils.replace(getString(R.string.validatecardwarn), "$ONECARD_FLAG$", getString(R.string.oneCard_flag));
            }
            if ("CARD_NOTMATCH".equalsIgnoreCase(errorCode)) {
                return getString(R.string.validatecardnomatch);
            }
            if ("UNSUPPORT".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_error_unsupport);
            }
            if ("CHARGE_CANCELED".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_error_charge_canceled);
            }
            if ("CARD_USED".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_error_card_used);
            }
            if ("CARD_PWDERROR".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_error_pwderror);
            }
            if ("CARD_AMOUNTERROR".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_error_card_amounterror);
            }
            if ("QUICKCARD_STATUSERROR".equalsIgnoreCase(errorCode)) {
                return getString(R.string.onecard_error_quickcard_statuserror);
            }
            return getString(R.string.onecard_error_unkown);
        }
    }

    private List sortByList(List GGKlist) {
        Collections.sort(GGKlist, new Comparator<String[][]>() {
            public int compare(String[][] o1, String[][] o2) {
                int i1 = Integer.parseInt(o1[0][1]);
                int i2 = Integer.parseInt(o2[0][1]);
                if (i1 < i2) {
                    return 1;
                }
                if (i1 == i2) {
                    return 0;
                }
                return -1;
            }
        });
        return GGKlist;
    }
}
