package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardCommEntity.ErrorCode;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class NewOneCardRechargeResultWarnActivity extends BaseActivity {
    private static String hostResponseCodeText = null;
    private static String modelResponseCodeText = null;
    private static String serverResponseCodeText = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        Intent intent = new Intent(NewOneCardRechargeResultWarnActivity.this.getBaseContext(), OneCardRechargeHintActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        NewOneCardRechargeResultWarnActivity.this.startActivity(intent);
                        NewOneCardRechargeResultWarnActivity.this.finish();
                        return;
                    }
                    ((TextView) NewOneCardRechargeResultWarnActivity.this.findViewById(R.id.phone_time)).setText("" + remainedSeconds);
                }
            };
            NewOneCardRechargeResultWarnActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_newonecardrecharge_warnresult);
        backgroundToActivity();
        ((Button) findViewById(R.id.onecard_cancle)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(NewOneCardRechargeResultWarnActivity.this.getBaseContext(), OneCardRechargeHintActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                NewOneCardRechargeResultWarnActivity.this.startActivity(intent);
                NewOneCardRechargeResultWarnActivity.this.finish();
            }
        });
        Intent intent = getIntent();
        String errorType = intent.getStringExtra("errorType");
        String errorCode = intent.getStringExtra("errorCode");
        String failedReason = intent.getStringExtra("reason");
        if (!StringUtils.isBlank(failedReason) && "NOT_ENOUGH".equalsIgnoreCase(failedReason)) {
            ((TextView) findViewById(R.id.failed_textView)).setText(R.string.onecard_balance_not_enough);
        } else if (!StringUtils.isBlank(errorType) && !StringUtils.isBlank(errorType)) {
            ((TextView) findViewById(R.id.failed_textView)).setText(getTransCardErrorCode(errorType, errorCode));
        }
    }

    protected void onStart() {
        super.onStart();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void onStop() {
        super.onStop();
        ((TextView) findViewById(R.id.phone_time)).setText("");
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void finish() {
        super.finish();
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
}
