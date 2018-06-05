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
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.util.ActivitySimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class QueryRechargeRecordActivity extends BaseActivity {
    private SimpleAdapter adapter;
    private List<HashMap<String, String>> columnLMap = new ArrayList();
    private ListView listView;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) QueryRechargeRecordActivity.this.findViewById(R.id.query_onecard_rebate_record_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(QueryRechargeRecordActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            QueryRechargeRecordActivity.this.startActivity(intent);
                            QueryRechargeRecordActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            QueryRechargeRecordActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
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
                TimeoutTask.getTimeoutTask().reset(QueryRechargeRecordActivity.this.timeoutAction);
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
                Intent intent = new Intent(QueryRechargeRecordActivity.this, OneCardRechargeHintActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                QueryRechargeRecordActivity.this.startActivity(intent);
                QueryRechargeRecordActivity.this.finish();
            }
        });
    }

    private List<HashMap<String, String>> loadData() {
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        List<HashMap<String, String>> listHsmpOneRecord = new ArrayList();
        try {
            String chinese_colon = getString(R.string.chinese_colon);
            HashMap<String, Object> hashMapQueryTransactionRecord = null;
            String OneCardVerson = SysConfig.get("RVM.ONECARD.DRV.VERSION");
            if (OneCardVerson.equals("0")) {
                hashMapQueryTransactionRecord = guiCommonService.execute("GUIOneCardCommonService", "transactionRecord", null);
            } else if (OneCardVerson.equals("1")) {
                hashMapQueryTransactionRecord = guiCommonService.execute("GUITrafficCardCommonService", "transactionRecord", null);
            }
            if (hashMapQueryTransactionRecord != null) {
                List<String> listHsmpTransactionRecord = (List) hashMapQueryTransactionRecord.get("TRANSATION_CONTENT");
                if (listHsmpTransactionRecord != null && listHsmpTransactionRecord.size() > 0) {
                    for (int j = 0; j < listHsmpTransactionRecord.size(); j++) {
                        HashMap<String, String> hsmpResult = new HashMap();
                        String[] str = ((String) listHsmpTransactionRecord.get(j)).split(",");
                        String[] strTime = str[0].split(chinese_colon);
                        if (strTime.length > 1) {
                            hsmpResult.put("rechargeMoney", str[3].split(chinese_colon)[1]);
                            hsmpResult.put("rechargeTime", strTime[1]);
                        } else {
                            hsmpResult.put("rechargeMoney", strTime[0]);
                            hsmpResult.put("rechargeTime", strTime[0]);
                        }
                        listHsmpOneRecord.add(hsmpResult);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listHsmpOneRecord;
    }

    protected void onStart() {
        super.onStart();
        this.columnLMap = loadData();
        this.adapter = new ActivitySimpleAdapter(this, this.columnLMap, R.layout.recharge_records_item, new String[]{"rechargeMoney", "rechargeTime"}, new int[]{R.id.recharge_num, R.id.recharge_time});
        this.listView.setAdapter(this.adapter);
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
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
