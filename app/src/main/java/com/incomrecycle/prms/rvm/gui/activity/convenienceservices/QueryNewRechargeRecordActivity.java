package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardCommEntity.ErrorCode;
import com.incomrecycle.prms.rvm.util.ActivitySimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class QueryNewRechargeRecordActivity extends BaseActivity {
    private SimpleAdapter adapter;
    private List<HashMap<String, String>> columnLMap = new ArrayList();
    private ListView listView;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) QueryNewRechargeRecordActivity.this.findViewById(R.id.query_onecard_rebate_record_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(QueryNewRechargeRecordActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            QueryNewRechargeRecordActivity.this.startActivity(intent);
                            QueryNewRechargeRecordActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            QueryNewRechargeRecordActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_query_recharge_record);
        backgroundToActivity();
        this.listView = (ListView) findViewById(R.id.query_recharge_record_list_view);
        this.listView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                TimeoutTask.getTimeoutTask().reset(QueryNewRechargeRecordActivity.this.timeoutAction);
                return false;
            }
        });
        ((Button) findViewById(R.id.query_onecard_rebate_record_end)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.QUERY_RECHARGE_RECORD_END);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                Intent intent = new Intent(QueryNewRechargeRecordActivity.this, OneCardRechargeHintActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                QueryNewRechargeRecordActivity.this.startActivity(intent);
                QueryNewRechargeRecordActivity.this.finish();
            }
        });
    }

    private List<HashMap<String, String>> loadData() {
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        List<HashMap<String, String>> listHsmpOneRecord = new ArrayList();
        try {
            HashMap<String, Object> hashMapQueryTransactionRecord = guiCommonService.execute("GUITrafficCardCommonService", "transactionRecord", null);
            if (hashMapQueryTransactionRecord != null) {
                if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hashMapQueryTransactionRecord.get("RET_CODE"))) {
                    List<HashMap> listHsmpTransactionRecord = (List) hashMapQueryTransactionRecord.get("chargeRecord");
                    if (listHsmpTransactionRecord != null && listHsmpTransactionRecord.size() > 0) {
                        for (int j = 0; j < listHsmpTransactionRecord.size(); j++) {
                            HashMap hashMap = new HashMap();
                            HashMap<String, String> hsmpResult = (HashMap) listHsmpTransactionRecord.get(j);
                            HashMap<String, String> hsmp = new HashMap();
                            hsmp.put("AMOUNT", String.valueOf(Float.parseFloat((String) hsmpResult.get("TransAmount")) / 100.0f));
                            hsmp.put("DATA", hsmpResult.get("TransDate"));
                            listHsmpOneRecord.add(hsmp);
                        }
                        return listHsmpOneRecord;
                    }
                }
                String errorType = ErrorCode.MODEL_RESPONSECODE;
                String str = (String) hashMapQueryTransactionRecord.get(ErrorCode.MODEL_RESPONSECODE);
                if (StringUtils.isBlank(str)) {
                    errorType = "HOST_RESPONSECODE";
                    str = (String) hashMapQueryTransactionRecord.get("HOST_RESPONSECODE");
                }
                if (StringUtils.isBlank(str)) {
                    errorType = "SERVER_RESPONSECODE";
                    str = (String) hashMapQueryTransactionRecord.get("SERVER_RESPONSECODE");
                }
                if (StringUtils.isBlank(str)) {
                    errorType = "RVM_RESPONSECODE";
                    str = (String) hashMapQueryTransactionRecord.get("RVM_RESPONSECODE");
                }
                if (StringUtils.isBlank(str)) {
                    errorType = "RVM_RESPONSECODE";
                    str = NetworkUtils.NET_STATE_UNKNOWN;
                }
                Intent intent = new Intent(getApplicationContext(), NewOneCardRechargeResultWarnActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("errorType", errorType);
                intent.putExtra("errorCode", str);
                startActivity(intent);
                finish();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onStart() {
        super.onStart();
        this.columnLMap = loadData();
        if (this.columnLMap != null && this.columnLMap.size() > 0) {
            this.adapter = new ActivitySimpleAdapter(this, this.columnLMap, R.layout.recharge_records_item, new String[]{"AMOUNT", "DATA"}, new int[]{R.id.recharge_num, R.id.recharge_time});
            this.listView.setAdapter(this.adapter);
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void finish() {
        super.finish();
        ((TextView) findViewById(R.id.query_onecard_rebate_record_time)).setText("");
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }
}
