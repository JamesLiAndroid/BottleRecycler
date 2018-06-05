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
import com.google.code.microlog4android.format.command.DateFormatCommand;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.DateUtils;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class QueryNewConsumptionRecordActivity extends BaseActivity {
    private SimpleAdapter adapter;
    private List<HashMap<String, String>> columnLMap = new ArrayList();
    private ListView listView;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) QueryNewConsumptionRecordActivity.this.findViewById(R.id.query_onecard_consumption_record_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(QueryNewConsumptionRecordActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            QueryNewConsumptionRecordActivity.this.startActivity(intent);
                            QueryNewConsumptionRecordActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            QueryNewConsumptionRecordActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_query_consumption_record);
        backgroundToActivity();
        this.listView = (ListView) findViewById(R.id.query_consumption_record_list_view);
        this.listView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                TimeoutTask.getTimeoutTask().reset(QueryNewConsumptionRecordActivity.this.timeoutAction);
                return false;
            }
        });
        ((Button) findViewById(R.id.query_onecard_consumption_record_end)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.QUERY_CONSUMPTION_END);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                Intent intent = new Intent(QueryNewConsumptionRecordActivity.this, OneCardRechargeHintActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                QueryNewConsumptionRecordActivity.this.startActivity(intent);
                QueryNewConsumptionRecordActivity.this.finish();
            }
        });
    }

    private List<HashMap<String, String>> loadData() {
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        List<HashMap<String, String>> listHsmpOneRecord = new ArrayList();
        try {
            HashMap<String, Object> hashMapQueryconsumeRecord = guiCommonService.execute("GUITrafficCardCommonService", "consumeRecord", null);
            if (hashMapQueryconsumeRecord != null) {
                if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hashMapQueryconsumeRecord.get("RET_CODE"))) {
                    List<HashMap> listHsmpConsumeRecord = (List) hashMapQueryconsumeRecord.get("consumeRecord");
                    if (listHsmpConsumeRecord != null && listHsmpConsumeRecord.size() > 0) {
                        for (int j = 0; j < listHsmpConsumeRecord.size(); j++) {
                            HashMap<String, String> hsmpResult = new HashMap();
                            hsmpResult = (HashMap) listHsmpConsumeRecord.get(j);
                            HashMap<String, String> hsmp = new HashMap();
                            hsmp.put("AMOUNT", String.valueOf(Float.parseFloat((String) hsmpResult.get("TransAmount")) / 100.0f));
                            hsmp.put(DateFormatCommand.DATE_FORMAT_STRING, DateUtils.formatDatetime(new SimpleDateFormat("yyyyMMddhhmmss").parse((String) hsmpResult.get("TransTime")), "yyyyMMdd"));
                            listHsmpOneRecord.add(hsmp);
                        }
                        return listHsmpOneRecord;
                    }
                }
                String errorType = ErrorCode.MODEL_RESPONSECODE;
                String str = (String) hashMapQueryconsumeRecord.get(ErrorCode.MODEL_RESPONSECODE);
                if (StringUtils.isBlank(str)) {
                    errorType = "HOST_RESPONSECODE";
                    str = (String) hashMapQueryconsumeRecord.get("HOST_RESPONSECODE");
                }
                if (StringUtils.isBlank(str)) {
                    errorType = "SERVER_RESPONSECODE";
                    str = (String) hashMapQueryconsumeRecord.get("SERVER_RESPONSECODE");
                }
                if (StringUtils.isBlank(str)) {
                    errorType = "RVM_RESPONSECODE";
                    str = (String) hashMapQueryconsumeRecord.get("RVM_RESPONSECODE");
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
            this.adapter = new ActivitySimpleAdapter(this, this.columnLMap, R.layout.consumption_records_item, new String[]{"AMOUNT", DateFormatCommand.DATE_FORMAT_STRING}, new int[]{R.id.consumption_num, R.id.consumption_time});
            this.listView.setAdapter(this.adapter);
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void finish() {
        super.finish();
        ((TextView) findViewById(R.id.query_onecard_consumption_record_time)).setText("");
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }
}
