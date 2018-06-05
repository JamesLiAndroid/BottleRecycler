package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.google.code.microlog4android.format.SimpleFormatter;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class FaultListActivity extends BaseActivity {
    private String CURRENT_STATE = null;
    private String NotRecycle;
    private FaultAdapter faultAdapter;
    private TextView fault_content_title;
    private ListView fault_list;
    private Button fault_return_btn;
    private List<String> list = new ArrayList();
    private List listerror = new ArrayList();
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int announcementSeconds, int remainedAnnouncements) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedAnnouncements = ((Integer) paramObjs[1]).intValue();
                    if (remainedAnnouncements != 0) {
                        ((TextView) FaultListActivity.this.findViewById(R.id.fault_list_time)).setText("" + remainedAnnouncements);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(FaultListActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            FaultListActivity.this.startActivity(intent);
                            FaultListActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            FaultListActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(announcementSeconds), Integer.valueOf(remainedAnnouncements)});
        }
    };

    public class FaultAdapter extends BaseAdapter {
        public int getCount() {
            return FaultListActivity.this.list.size();
        }

        public Object getItem(int position) {
            return FaultListActivity.this.list.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View contentView, ViewGroup arg2) {
            if (contentView == null) {
                contentView = LayoutInflater.from(FaultListActivity.this).inflate(R.layout.activity_fault_list_item, null);
                LinearLayout second_lisght_layout = (LinearLayout) contentView.findViewById(R.id.second_layout);
                LinearLayout service_layout = (LinearLayout) contentView.findViewById(R.id.service_layout);
                LinearLayout first_light_layout = (LinearLayout) contentView.findViewById(R.id.first_light);
                LinearLayout third_light_layout = (LinearLayout) contentView.findViewById(R.id.third_light);
                LinearLayout power_layout = (LinearLayout) contentView.findViewById(R.id.power_layout);
                LinearLayout nopaper_layout = (LinearLayout) contentView.findViewById(R.id.nopaper_layout);
                LinearLayout print_error_layout = (LinearLayout) contentView.findViewById(R.id.print_error_layout);
                LinearLayout barcode_scaner_error_layout = (LinearLayout) contentView.findViewById(R.id.barcode_scaner_error_layout);
                LinearLayout plc_comm_error_layout = (LinearLayout) contentView.findViewById(R.id.plc_comm_error_layout);
                LinearLayout plc_door_error_layout = (LinearLayout) contentView.findViewById(R.id.plc_door_error_layout);
                LinearLayout rebates_layout = (LinearLayout) contentView.findViewById(R.id.rebates_layout);
                LinearLayout max_bottle_layout = (LinearLayout) contentView.findViewById(R.id.max_bottle_layout);
                LinearLayout play_sounds_layout = (LinearLayout) contentView.findViewById(R.id.play_sounds_layout);
                LinearLayout year_error_layout = (LinearLayout) contentView.findViewById(R.id.year_error);
                LinearLayout update_error_layout = (LinearLayout) contentView.findViewById(R.id.update_error);
                LinearLayout stop_service_layout = (LinearLayout) contentView.findViewById(R.id.stop_service_layout);
                if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("UPDATING")) {
                    update_error_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.fault_updateerror_list_text)).setText("" + FaultListActivity.this.getString(R.string.alarm_info_item_update_state));
                }
                if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("YEAR_ERROR")) {
                    year_error_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.fault_yearerror_list_text)).setText("" + FaultListActivity.this.getString(R.string.alarm_info_item_year_error));
                }
                if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("SERVICE_DISABLE")) {
                    service_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.fault_list_text)).setText(FaultListActivity.this.getString(R.string.service_error));
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("MAX_BOTTLE")) {
                    max_bottle_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.max_bottle_list_text)).setText(FaultListActivity.this.getString(R.string.ReachMaxOfBottle));
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("IS_PLAY_SOUNDS")) {
                    play_sounds_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.play_sounds_list_text)).setText(FaultListActivity.this.getString(R.string.Audio_driver_error));
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("VENDINGWAY_DISABLE")) {
                    rebates_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.rebates_list_text)).setText(FaultListActivity.this.getString(R.string.Rebates_error));
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("PLC_COMM_ERROR")) {
                    plc_comm_error_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.plc_comm_error_list_text)).setText(FaultListActivity.this.getString(R.string.hardware_comm_error));
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("PLC_DOOR_ERROR")) {
                    plc_door_error_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.plc_door_error_list_text)).setText(FaultListActivity.this.getString(R.string.hardware_door_error));
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("PLC_SECOND_LIGHT_ERROR")) {
                    second_lisght_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.fault_second_light_text)).setText(FaultListActivity.this.getString(R.string.alarm_info_item_16));
                    ((LinearLayout) contentView.findViewById(R.id.fault_second_light_solve_linearlayout)).setVisibility(0);
                    Button fault_second_light_solve_button = (Button) contentView.findViewById(R.id.fault_second_light_solve_button);
                    fault_second_light_solve_button.setEnabled(true);
                    fault_second_light_solve_button.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            SysGlobal.execute(new Runnable() {
                                public void run() {
                                    try {
                                        if ("TRUE".equalsIgnoreCase(SysConfig.get("COM.PLC.HAS.DOOR")) && CommService.getCommService().execute("QUERY_BOTTLE_DOOR_STATE", null).equalsIgnoreCase("BOTTLE_DOOR_CLOSE")) {
                                            CommService.getCommService().execute("OPEN_BOTTLE_DOOR", "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("PLC_FIRST_LIGHT_ERROR")) {
                    first_light_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.fault_first_light_list_text)).setText(FaultListActivity.this.getString(R.string.alarm_info_item_15));
                    ((LinearLayout) contentView.findViewById(R.id.fault_first_light_solve_linearlayout)).setVisibility(View.VISIBLE);
                    Button fault_first_light_solve_button = (Button) contentView.findViewById(R.id.fault_first_light_solve_button);
                    fault_first_light_solve_button.setEnabled(true);
                    fault_first_light_solve_button.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            SysGlobal.execute(new Runnable() {
                                public void run() {
                                    try {
                                        if ("TRUE".equalsIgnoreCase(SysConfig.get("COM.PLC.HAS.DOOR")) && CommService.getCommService().execute("QUERY_BOTTLE_DOOR_STATE", null).equalsIgnoreCase("BOTTLE_DOOR_CLOSE")) {
                                            CommService.getCommService().execute("OPEN_BOTTLE_DOOR", "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("PLC_THIRD_LIGHT_ERROR")) {
                    third_light_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.fault_third_light_list_text)).setText(FaultListActivity.this.getString(R.string.alarm_info_item_17));
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("MAINTAIN_TIME")) {
                    power_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.fault_power_list_text)).setText(FaultListActivity.this.getString(R.string.Power_off_before));
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("PRINTER_NO_PAPER")) {
                    nopaper_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.fault_nopaper_list_text)).setText(FaultListActivity.this.getString(R.string.alarm_info_item_6));
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("PRINTER_ERROR")) {
                    print_error_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.fault_print_error_list_text)).setText(FaultListActivity.this.getString(R.string.alarm_info_item_7));
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("BARCODE_SCANER_ERROR")) {
                    barcode_scaner_error_layout.setVisibility(View.VISIBLE);
                    ((TextView) contentView.findViewById(R.id.fault_barcode_scaner_error_list_text)).setText(FaultListActivity.this.getString(R.string.alarm_info_item_8));
                } else if (((String) FaultListActivity.this.list.get(position)).equalsIgnoreCase("STOP_SVERVICE_TIME")) {
                    stop_service_layout.setVisibility(View.VISIBLE);
                    TextView text = (TextView) contentView.findViewById(R.id.fault_stopservice_list_text);
                    List sleepList = (List) GUIGlobal.getCurrentSession("SLEEP_LIST");
                    if (!(sleepList == null || sleepList.isEmpty())) {
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < sleepList.size(); i++) {
                            HashMap tmMap = (HashMap) sleepList.get(i);
                            if (!(tmMap == null || tmMap.isEmpty())) {
                                String beginTime = (String) tmMap.keySet().iterator().next();
                                String endTime = (String) tmMap.get(beginTime);
                                if (!(StringUtils.isBlank(beginTime) || StringUtils.isBlank(endTime))) {
                                    if (beginTime.length() == 6 && beginTime.matches("\\d{6}")) {
                                        String[] bTime = new String[] { beginTime.substring(0, 2), null, null };
                                        sb.append(bTime[0] + ":");
                                        bTime[1] = beginTime.substring(2, 4);
                                        sb.append(bTime[1] + SimpleFormatter.DEFAULT_DELIMITER);
                                    }
                                    String[] eTime = new String[3];
                                    if (endTime.length() == 6 && endTime.matches("\\d{6}")) {
                                        eTime[0] = endTime.substring(0, 2);
                                        sb.append(eTime[0] + ":");
                                        eTime[1] = endTime.substring(2, 4);
                                        sb.append(eTime[1] + " ");
                                    }
                                }
                            }
                        }
                        text.setText(FaultListActivity.this.getString(R.string.stop_service_info).replace("$STOP_SERVICE_TIME$", sb.toString()));
                    }
                }
            }
            return contentView;
        }
    }

    protected void onStart() {
        super.onStart();
        this.fault_list = (ListView) findViewById(R.id.fault_list);
        this.fault_content_title = (TextView) findViewById(R.id.fault_content_title);
        this.fault_content_title.setText(R.string.fault_list);
        this.listerror = getIntent().getExtras().getParcelableArrayList("STATE");
        if (this.listerror != null && this.listerror.size() > 0) {
            for (int i = 0; i < this.listerror.size(); i++) {
                this.NotRecycle = (String) this.listerror.get(i);
                this.list.add(this.NotRecycle);
            }
        }
        if ("TRUE".equalsIgnoreCase(SysConfig.get("STATE:PRINTER_ERROR"))) {
            this.list.add("PRINTER_ERROR");
        }
        if ("TRUE".equalsIgnoreCase(SysConfig.get("STATE:PRINTER_NO_PAPER"))) {
            this.list.add("PRINTER_NO_PAPER");
        }
        if ("TRUE".equalsIgnoreCase(SysConfig.get("STATE:BARCODE_SCANER_ERROR"))) {
            this.list.add("BARCODE_SCANER_ERROR");
        }
        if ("TRUE".equalsIgnoreCase(SysConfig.get("STATE:PLC_COMM_ERROR"))) {
            this.list.add("PLC_COMM_ERROR");
        }
        if ("TRUE".equalsIgnoreCase(SysConfig.get("STATE:PLC_DOOR_ERROR"))) {
            this.list.add("PLC_DOOR_ERROR");
        }
        if (this.list.size() > 0) {
            this.faultAdapter = new FaultAdapter();
            this.fault_list.setAdapter(this.faultAdapter);
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.SELECTVOUCHER")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_fault_list);
        backgroundToActivity();
        this.fault_return_btn = (Button) findViewById(R.id.fault_return_btn);
        this.fault_return_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.FAULT_RETURN);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                FaultListActivity.this.finish();
            }
        });
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    protected void onStop() {
        super.onStop();
        finish();
    }

    public void finish() {
        super.finish();
        SysGlobal.execute(new Runnable() {
            public void run() {
                try {
                    if ("TRUE".equalsIgnoreCase(SysConfig.get("COM.PLC.HAS.DOOR")) && CommService.getCommService().execute("QUERY_BOTTLE_DOOR_STATE", null).equalsIgnoreCase("BOTTLE_DOOR_OPEN")) {
                        CommService.getCommService().execute("CLOSE_BOTTLE_DOOR", "");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }
}
