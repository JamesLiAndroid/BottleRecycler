package com.incomrecycle.prms.rvm.gui.activity.channel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.channel.PullDownView.OnPullDownListener;
import com.incomrecycle.prms.rvm.util.ActivitySimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class AlarmListActivity extends BaseActivity implements OnPullDownListener {
    private SimpleAdapter adapter;
    private List<HashMap<String, String>> columnLMap = new ArrayList();
    List listOpt = new ArrayList();
    private ListView listView;
    private Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    AlarmListActivity.this.columnLMap.clear();
                    AlarmListActivity.this.columnLMap.addAll(AlarmListActivity.this.getResultListPage(1));
                    AlarmListActivity.this.adapter.notifyDataSetChanged();
                    AlarmListActivity.this.pullDownView.notifyDidLoad();
                    return;
                case 1:
                    AlarmListActivity.this.adapter.notifyDataSetChanged();
                    AlarmListActivity.this.pullDownView.notifyDidRefresh();
                    return;
                case 2:
                    AlarmListActivity.this.pageNo = AlarmListActivity.this.pageNo + 1;
                    AlarmListActivity.this.columnLMap.addAll(AlarmListActivity.this.getResultListPage(AlarmListActivity.this.pageNo));
                    AlarmListActivity.this.adapter.notifyDataSetChanged();
                    AlarmListActivity.this.pullDownView.notifyDidMore();
                    return;
                default:
                    return;
            }
        }
    };
    private int pageNo = 1;
    private PullDownView pullDownView;
    private String staffPermission = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) AlarmListActivity.this.findViewById(R.id.show_alarm_list_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(AlarmListActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            AlarmListActivity.this.startActivity(intent);
                            AlarmListActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            AlarmListActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    public class AlarmSimpleAdapter extends ActivitySimpleAdapter {
        public AlarmSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            HashMap<String, String> m = (HashMap) AlarmListActivity.this.columnLMap.get(position);
            if (convertView == null) {
                convertView = ((LayoutInflater) AlarmListActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.warn_listviewlayout, null);
            }
            LinearLayout warnListview = (LinearLayout) convertView.findViewById(R.id.warn_listview);
            TextView warnDateTime = (TextView) warnListview.findViewById(R.id.warnDateTime);
            warnDateTime.setText((CharSequence) m.get("ALARM_TIME"));
            warnDateTime.setTextSize(18.0f);
            TextView warnInfo = (TextView) warnListview.findViewById(R.id.warnInfo);
            warnInfo.setText((CharSequence) m.get("ALARM_ID_SR"));
            warnInfo.setTextSize(18.0f);
            Button warnOperButton = (Button) warnListview.findViewById(R.id.warnOperButton);
            TextView warnOperText = (TextView) warnListview.findViewById(R.id.warnOperText);
            warnOperButton.setText((CharSequence) m.get("ALARM_STATUS_SR"));
            warnOperButton.setTag(m.get("ALARM_INST_ID"));
            warnOperButton.setVisibility(0);
            warnOperButton.setTextSize(18.0f);
            warnOperText.setVisibility(View.VISIBLE);
            warnOperText.setTextSize(18.0f);
            if ("1".equals(m.get("HANDLE_ENABLE"))) {
                warnOperText.setVisibility(8);
            } else {
                warnOperButton.setVisibility(8);
            }
            warnOperButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Object alarmId = v.findViewById(R.id.warnOperButton).getTag();
                    GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                    HashMap<String, Object> hsmpParam = new HashMap();
                    hsmpParam.put("ALARM_INST_ID", alarmId);
                    try {
                        guiCommonService.execute("GUIMaintenanceCommonService", "alarmManualRecovery", hsmpParam);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    TimeoutTask.getTimeoutTask().reset(AlarmListActivity.this.timeoutAction);
                    AlarmSimpleAdapter.this.notifyDataSetChanged();
                }
            });
            AlarmListActivity.logger.debug("position:" + position + ";status:" + ((String) m.get("ALARM_STATUS")) + ";button:" + ((String) m.get("ALARM_STATUS_SR")) + ";alarmOperText:" + ((String) m.get("ALARM_STATUS_SR")));
            return super.getView(position, convertView, parent);
        }
    }

    public void onStart() {
        super.onStart();
        this.listOpt = getIntent().getStringArrayListExtra("LIST");
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.MAINTAIN")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.show_alarm_list);
        this.staffPermission = getIntent().getStringExtra("STAFF_PERMISSION");
        LinearLayout lin = (LinearLayout) findViewById(R.id.show_alarm_list);
        LinearLayout layout1 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.activity_alarm_list, null);
        this.pullDownView = (PullDownView) layout1.findViewById(R.id.pull_down_alarm_list_view);
        this.pullDownView.setOnPullDownListener(this);
        this.listView = this.pullDownView.getListView();
        this.listView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                TimeoutTask.getTimeoutTask().reset(AlarmListActivity.this.timeoutAction);
                return false;
            }
        });
        lin.removeAllViews();
        lin.addView(layout1);
        this.columnLMap = getResultListPage(1);
        this.adapter = new AlarmSimpleAdapter(this, this.columnLMap, R.layout.warn_listviewlayout, new String[]{"ALARM_TIME", "ALARM_ID_SR", "ALARM_STATUS_SR", "ALARM_STATUS_SR"}, new int[]{R.id.warnDateTime, R.id.warnInfo, R.id.warnOperText, R.id.warnOperButton});
        this.listView.setAdapter(this.adapter);
        this.pullDownView.enableAutoFetchMore(true, 1);
        loadData();
        ((Button) findViewById(R.id.show_alarm_list_return_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AlarmListActivity.this, ChannelMainActivity.class);
                intent.putExtra("STAFF_PERMISSION", AlarmListActivity.this.staffPermission);
                intent.putExtra("LIST_VALUE", "TRUE");
                intent.putStringArrayListExtra("LIST", (ArrayList) AlarmListActivity.this.listOpt);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                AlarmListActivity.this.startActivity(intent);
                AlarmListActivity.this.finish();
            }
        });
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    private void loadData() {
        SysGlobal.execute(new Runnable() {
            public void run() {
                AlarmListActivity.this.mUIHandler.obtainMessage(0).sendToTarget();
            }
        });
    }

    public void onRefresh() {
        SysGlobal.execute(new Runnable() {
            public void run() {
                Message msg = AlarmListActivity.this.mUIHandler.obtainMessage(1);
                msg.obj = "After refresh " + System.currentTimeMillis();
                msg.sendToTarget();
            }
        });
    }

    public void onMore() {
        SysGlobal.execute(new Runnable() {
            public void run() {
                Message msg = AlarmListActivity.this.mUIHandler.obtainMessage(2);
                msg.obj = "After more " + System.currentTimeMillis();
                msg.sendToTarget();
            }
        });
    }

    private List<HashMap<String, String>> getResultListPage(int pageNo) {
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        HashMap<String, Object> hsmpResult = null;
        HashMap<String, Object> hsmpParam = new HashMap();
        hsmpParam.put("PAGE_ROWS", SysConfig.get("RVM.PAGE.ROWS"));
        hsmpParam.put("PAGE_NO", String.valueOf(pageNo));
        try {
            hsmpResult = guiCommonService.execute("GUIMaintenanceCommonService", "getAlarmList", hsmpParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (hsmpResult != null) {
            return listAdapter((List) hsmpResult.get("RET_LIST"));
        }
        return null;
    }

    private List<HashMap<String, String>> listAdapter(List<HashMap<String, String>> listPage) {
        CharSequence[] alarmStatusItems = getResources().getStringArray(R.array.alarm_status);
        CharSequence[] alarmIdItems = getResources().getStringArray(R.array.alarm_info);
        List<HashMap<String, String>> newListPage = new ArrayList();
        for (HashMap<String, String> map : listPage) {
            int a;
            int ALARM_STATUS = -1;
            int alarStu = Integer.valueOf((String) map.get("ALARM_STATUS")).intValue();
            for (a = 0; a < SysDef.AlarmStuts.length; a++) {
                logger.debug("+++++++++++++++ALARM_STATUSss " + SysDef.AlarmStuts[a] + "|||ALARM_STATUS  " + ((String) map.get("ALARM_STATUS")));
                if (SysDef.AlarmStuts[a] == alarStu) {
                    ALARM_STATUS = a;
                    break;
                }
            }
            if (ALARM_STATUS < 0) {
                map.put("ALARM_STATUS_SR", getString(R.string.alarm_status_item_error));
            } else {
                map.put("ALARM_STATUS_SR", alarmStatusItems[ALARM_STATUS].toString());
            }
            int ALARM_ID = -1;
            int alarId = Integer.valueOf((String) map.get("ALARM_ID")).intValue();
            for (a = 0; a < SysDef.AlarmIdStuts.length; a++) {
                if (SysDef.AlarmIdStuts[a] == alarId) {
                    ALARM_ID = a;
                    break;
                }
            }
            if (ALARM_ID < 0) {
                map.put("ALARM_ID_SR", getString(R.string.alarm_info_item_error));
            } else {
                map.put("ALARM_ID_SR", alarmIdItems[ALARM_ID].toString());
            }
            newListPage.add(map);
        }
        return newListPage;
    }
}
