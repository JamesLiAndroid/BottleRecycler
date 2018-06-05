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
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.entity.CardEntity;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.text.DecimalFormat;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class NewOneCardRechargeResultActivity extends BaseActivity {
    private TextView onecard_balance = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) NewOneCardRechargeResultActivity.this.findViewById(R.id.phone_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(NewOneCardRechargeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            NewOneCardRechargeResultActivity.this.startActivity(intent);
                            NewOneCardRechargeResultActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            NewOneCardRechargeResultActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_newonecardrecharge_result);
        backgroundToActivity();
        this.onecard_balance = (TextView) findViewById(R.id.onecard_balance);
        DecimalFormat df = new DecimalFormat("0.00");
        if (((String) ServiceGlobal.getCurrentSession("ONECARD_MONEY")) != null) {
            this.onecard_balance.setText((String) ServiceGlobal.getCurrentSession("ONECARD_MONEY"));
        } else {
            this.onecard_balance.setText("" + df.format(CardEntity.RECHARGED / 100.0d));
        }
        ((Button) findViewById(R.id.onecard_cancle)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(NewOneCardRechargeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        NewOneCardRechargeResultActivity.this.startActivity(intent);
                        NewOneCardRechargeResultActivity.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
}
