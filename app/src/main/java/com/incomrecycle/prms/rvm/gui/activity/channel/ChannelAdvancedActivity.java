package com.incomrecycle.prms.rvm.gui.activity.channel;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.RVMShell;
import com.incomrecycle.prms.rvm.common.SysDef;
import com.incomrecycle.prms.rvm.common.SysDef.maintainOptContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionNetWorkTest;
import com.incomrecycle.prms.rvm.gui.action.GUIActionOperData;
import com.incomrecycle.prms.rvm.gui.action.GUIActionServiceCfg;
import com.incomrecycle.prms.rvm.gui.action.GUIActionSetupPropertyDone;
import com.incomrecycle.prms.rvm.gui.action.GUIActionSetupTesting;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import com.incomrecycle.prms.rvmdaemon.RVMDaemonClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class ChannelAdvancedActivity extends BaseActivity {
    private String CONFIG_SAVE_Sts = null;
    private String SET_ALARM_VALUE_Sts = null;
    private String SET_HAS_DOOR_Sts = null;
    private String SET_NO_DOOR_Sts = null;
    private String SET_RVM_OFF_TIME_Sts = null;
    private String SET_RVM_ON_TIME_Sts = null;
    private int add_click_account = 1;
    private AudioManager audioManager = null;
    private int cur_Volume = 0;
    boolean enableHeartToneDetection = false;
    private Properties externalProp = null;
    private HashMap<String, String> hash1;
    private HashMap<String, String> hash2;
    private HashMap<String, String> hash3;
    private String key1 = "0000";
    private String key2 = "0000";
    private String key3 = "0000";
    private List<HashMap<String, String>> list;
    List<String> listOpt = new ArrayList();
    List<String> listStaffSet = new ArrayList();
    private int max_Volume = 0;
    private TimePicker mytp1;
    private TimePicker mytp2;
    private HashMap result;
    public String selectHDVersion;
    public String selectRvmMode;
    private String staffPermission = null;
    private int step_Volume = 0;
    private int time1_off = 0;
    private int time1_on = 0;
    private int time2_off = 0;
    private int time2_on = 0;
    private int time3_off = 0;
    private int time3_on = 0;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        if (ChannelAdvancedActivity.this.listOpt.size() > 0) {
                            SysGlobal.execute(new Runnable() {
                                public void run() {
                                    HashMap haspParam = new HashMap();
                                    haspParam.put("OPT_OPTIONS", ChannelAdvancedActivity.this.listOpt);
                                    try {
                                        CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "maintainAddOptCon", haspParam);
                                        CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "maintainToRCC", null);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                        if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                            try {
                                Intent intent = new Intent(ChannelAdvancedActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                ChannelAdvancedActivity.this.startActivity(intent);
                                ChannelAdvancedActivity.this.finish();
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                        return;
                    }
                    ((TextView) ChannelAdvancedActivity.this.findViewById(R.id.channel_advance__time)).setText("" + remainedSeconds);
                }
            };
            ChannelAdvancedActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    Runnable updateThread = new Runnable() {
        public void run() {
            Button startUsingButton = (Button) ChannelAdvancedActivity.this.findViewById(R.id.startUsingButton);
            startUsingButton.setBackgroundColor(-7829368);
            startUsingButton.setEnabled(false);
        }
    };
    private String value1 = "0000";
    private String value2 = "0000";
    private String value3 = "0000";

    protected void onPause() {
        super.onPause();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
    }

    protected void onPostResume() {
        super.onPostResume();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void onStop() {
        super.onStop();
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void onStart() {
        super.onStart();
        ((LinearLayout) findViewById(R.id.channel_advance_linnearlayout)).removeAllViews();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.MAINTAIN")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_channel_advanced);
        decideStaffPermission();
        ((TextView) findViewById(R.id.software_version)).setText(SysConfig.get("RVM.VERSION.ID"));
        final LayoutInflater inflater = LayoutInflater.from(this);
        final LinearLayout lin = (LinearLayout) findViewById(R.id.channel_advance_linnearlayout);
        Button showNetWorkTestButton = (Button) findViewById(R.id.showNetWorkTestButton);
        Button netCfgButton = (Button) findViewById(R.id.netCfgButton);
        Button serCfgButton = (Button) findViewById(R.id.serCfgButton);
        Button operDataButton = (Button) findViewById(R.id.operDataButton);
        Button goBackButton = (Button) findViewById(R.id.channel_advance__return_btn);
        Button exitButton = (Button) findViewById(R.id.channel_advance_exit_btn);
        Button systemSettingsButton = (Button) findViewById(R.id.systemSettingsButton);
        String advanceStaff = SysConfig.get("ADVANCE_STAFF.PERMISSION.SET");
        String generalStaff = SysConfig.get("GENERAL_STAFF.PERMISSION.SET");
        if (!StringUtils.isBlank(this.staffPermission)) {
            if (this.staffPermission.equalsIgnoreCase(SysDef.staffPermission.ADVANCE_STAFF) && !StringUtils.isBlank(advanceStaff)) {
                String[] advanceStaffSetArray = advanceStaff.split(";");
                for (String add : advanceStaffSetArray) {
                    this.listStaffSet.add(add);
                }
            }
            if (this.staffPermission.equalsIgnoreCase(SysDef.staffPermission.GENERAL_STAFF) && !StringUtils.isBlank(generalStaff)) {
                String[] generalStaffSetArray = generalStaff.split(";");
                for (String add2 : generalStaffSetArray) {
                    this.listStaffSet.add(add2);
                }
            }
        }
        if (!this.listStaffSet.contains(SysDef.staffPermission.systemSeting)) {
            systemSettingsButton.setVisibility(View.GONE);
        }
        if (!this.listStaffSet.contains(SysDef.staffPermission.networkTest)) {
            showNetWorkTestButton.setVisibility(View.GONE);
        }
        if (!this.listStaffSet.contains(SysDef.staffPermission.recycleBottleList)) {
            operDataButton.setVisibility(View.GONE);
        }
        if (!this.listStaffSet.contains(SysDef.staffPermission.cfgSeting)) {
            netCfgButton.setVisibility(View.GONE);
        }
        if (!this.listStaffSet.contains(SysDef.staffPermission.serSeting)) {
            serCfgButton.setVisibility(View.GONE);
        }
        exitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ChannelAdvancedActivity.this.listOpt.size() > 0) {
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            HashMap haspParam = new HashMap();
                            haspParam.put("OPT_OPTIONS", ChannelAdvancedActivity.this.listOpt);
                            try {
                                CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "maintainAddOptCon", haspParam);
                                CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "maintainToRCC", null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                RVMDaemonClient.exit();
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        GUIGlobal.exit();
                    }
                }).start();
            }
        });
        goBackButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ChannelAdvancedActivity.this, ChannelMainActivity.class);
                intent.putExtra("STAFF_PERMISSION", ChannelAdvancedActivity.this.staffPermission);
                intent.putExtra("LIST_VALUE", "TRUE");
                intent.putStringArrayListExtra("LIST", (ArrayList) ChannelAdvancedActivity.this.listOpt);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                ChannelAdvancedActivity.this.startActivity(intent);
                ChannelAdvancedActivity.this.finish();
            }
        });
        operDataButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ChannelAdvancedActivity.this.executeGUIAction(true, new GUIActionOperData(), new Object[]{ChannelAdvancedActivity.this});
                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
            }
        });
        netCfgButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.activity_setup_config, null).findViewById(R.id.setupcfglayout);
                lin.removeAllViews();
                lin.addView(layout);
                ChannelAdvancedActivity.this.showCheckBox();
                EditText setupCfgTimerNumEdit = (EditText) lin.findViewById(R.id.setupCfgTimerNumEdit);
                EditText setupCfgRccIpEdit = (EditText) lin.findViewById(R.id.setupCfgRccIpEdit);
                EditText setupCfgRccPortEdit = (EditText) lin.findViewById(R.id.setupCfgRccPortEdit);
                final EditText importPhoneNumber = (EditText) lin.findViewById(R.id.importPhoneNumber);
                TextView setupCfgOperatorCodeText = (TextView) lin.findViewById(R.id.setupCfgOperatorCode);
                TextView setupCfgLocalIpText = (TextView) lin.findViewById(R.id.setupCfgLocalIpText);
                TextView localModeText = (TextView) lin.findViewById(R.id.localModeText);
                TextView localHardVersionText = (TextView) lin.findViewById(R.id.localHDVersionText);
                ChannelAdvancedActivity.this.showOrHideKeybordAndResetTime(setupCfgTimerNumEdit, ChannelAdvancedActivity.this.timeoutAction);
                ChannelAdvancedActivity.this.showOrHideKeybordAndResetTime(setupCfgRccIpEdit, ChannelAdvancedActivity.this.timeoutAction);
                ChannelAdvancedActivity.this.showOrHideKeybordAndResetTime(setupCfgRccPortEdit, ChannelAdvancedActivity.this.timeoutAction);
                ChannelAdvancedActivity.this.showOrHideKeybordAndResetTime(importPhoneNumber, ChannelAdvancedActivity.this.timeoutAction);
                setupCfgTimerNumEdit.setText(SysConfig.get("RVM.CODE"));
                setupCfgRccIpEdit.setText(SysConfig.get("RCC.IP"));
                setupCfgRccPortEdit.setText(SysConfig.get("RCC.PORT"));
                importPhoneNumber.setText(SysConfig.get("CHANNEL.PHONE.NUMER"));
                setupCfgOperatorCodeText.setText(SysConfig.get("RVM.AREA.CODE"));
                setupCfgLocalIpText.setText(SysConfig.get("LOCAL.IP"));
                localModeText.setText(SysConfig.get("RVM.MODE"));
                localHardVersionText.setText(SysConfig.get("HARDWARE.VERSION"));
                Button btnSetupCfgLink = (Button) ChannelAdvancedActivity.this.findViewById(R.id.setupCfgLinkBtn);
                final Button btnsetupCfgDone = (Button) ChannelAdvancedActivity.this.findViewById(R.id.setupCfgDoneBtn);
                final Button btnInputPhone = (Button) ChannelAdvancedActivity.this.findViewById(R.id.inputPhone);
                final Button btnInputWifi = (Button) ChannelAdvancedActivity.this.findViewById(R.id.inputWifi);
                btnsetupCfgDone.setEnabled(false);
                btnInputPhone.setBackgroundResource(R.color.wechat_red);
                btnInputPhone.setOnClickListener(new OnClickListener() {
                    public void onClick(View arg0) {
                        importPhoneNumber.setText(SysConfig.get("CHANNEL.PHONE.NUMER"));
                        importPhoneNumber.setFocusableInTouchMode(true);
                        importPhoneNumber.setFocusable(true);
                        importPhoneNumber.requestFocus();
                        importPhoneNumber.setBackgroundResource(R.color.wechat_new_white);
                        btnInputPhone.setBackgroundResource(R.color.wechat_red);
                        btnInputWifi.setBackgroundResource(R.color.wechat_new_white);
                        TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                    }
                });
                btnInputWifi.setOnClickListener(new OnClickListener() {
                    public void onClick(View arg0) {
                        ((InputMethodManager) ChannelAdvancedActivity.this.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(arg0.getWindowToken(), 0);
                        importPhoneNumber.setText("0");
                        importPhoneNumber.setFocusable(false);
                        importPhoneNumber.setFocusableInTouchMode(false);
                        importPhoneNumber.setBackgroundResource(R.color.grgray);
                        btnInputWifi.setBackgroundResource(R.color.wechat_red);
                        btnInputPhone.setBackgroundResource(R.color.wechat_new_white);
                        TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                    }
                });
                btnSetupCfgLink.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ChannelAdvancedActivity.this.executeGUIAction(true, new GUIActionSetupTesting(), new Object[]{ChannelAdvancedActivity.this});
                        TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                    }
                });
                btnsetupCfgDone.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (ChannelAdvancedActivity.this.CONFIG_SAVE_Sts == null) {
                            ChannelAdvancedActivity.this.CONFIG_SAVE_Sts = maintainOptContent.CONFIG_SAVE;
                            ChannelAdvancedActivity.this.listOpt.add(ChannelAdvancedActivity.this.CONFIG_SAVE_Sts);
                        }
                        ChannelAdvancedActivity.this.executeGUIAction(true, new GUIActionSetupPropertyDone(), new Object[]{ChannelAdvancedActivity.this});
                        TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                        btnsetupCfgDone.setEnabled(false);
                        btnsetupCfgDone.setBackgroundColor(Color.parseColor("#202924"));
                    }
                });
                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
            }
        });
        serCfgButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ChannelAdvancedActivity.this.executeGUIAction(true, new GUIActionServiceCfg(), new Object[]{ChannelAdvancedActivity.this, ChannelAdvancedActivity.this.listOpt});
                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
            }
        });
        showNetWorkTestButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.activity_network_test, null).findViewById(R.id.networkLayout);
                lin.removeAllViews();
                lin.addView(layout);
                ChannelAdvancedActivity.this.showOrHideKeybordAndResetTime((EditText) ChannelAdvancedActivity.this.findViewById(R.id.testNumEdit), ChannelAdvancedActivity.this.timeoutAction);
                ChannelAdvancedActivity.this.showOrHideKeybordAndResetTime((EditText) ChannelAdvancedActivity.this.findViewById(R.id.timeOutEdit), ChannelAdvancedActivity.this.timeoutAction);
                ((Button) ChannelAdvancedActivity.this.findViewById(R.id.testBtn)).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ChannelAdvancedActivity.this.executeGUIAction(true, new GUIActionNetWorkTest(), new Object[]{ChannelAdvancedActivity.this});
                        TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                    }
                });
                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
            }
        });
        systemSettingsButton.setOnClickListener(new OnClickListener() {
            private LinearLayout inflate;

            public void onClick(View v) {
                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.activity_system_settings, null).findViewById(R.id.activity_system_settings_layout);
                lin.removeAllViews();
                lin.addView(layout);
                Button setBottleMaxNum = (Button) lin.findViewById(R.id.setMaxBottleNum);
                Button faultDiagnosisButton = (Button) lin.findViewById(R.id.faultDiagnosisButton);
                Button cancelTimingButton = (Button) lin.findViewById(R.id.cancelTimingButton);
                Button setOnOff_Door = (Button) lin.findViewById(R.id.setOnOff_Door);
                Button startUsingButton = (Button) lin.findViewById(R.id.startUsingButton);
                Button dubuggingButton = (Button) lin.findViewById(R.id.dubuggingButton);
                ((Button) lin.findViewById(R.id.duociqiting)).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ChannelAdvancedActivity.this.add_click_account = 1;
                        TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                        if (inflate != null) {
                            inflate.removeAllViews();
                        }
                        inflate = (LinearLayout) LayoutInflater.from(ChannelAdvancedActivity.this).inflate(R.layout.duocidianji, null, false);
                        TimePicker time1_start = (TimePicker) inflate.findViewById(R.id.time1_start);
                        TimePicker time1_end = (TimePicker) inflate.findViewById(R.id.time1_end);
                        TimePicker time2_start = (TimePicker) inflate.findViewById(R.id.time2_start);
                        TimePicker time2_end = (TimePicker) inflate.findViewById(R.id.time2_end);
                        TimePicker time3_start = (TimePicker) inflate.findViewById(R.id.time3_start);
                        final TimePicker time3_end = (TimePicker) inflate.findViewById(R.id.time3_end);
                        final Button add1 = (Button) inflate.findViewById(R.id.add1);
                        Button delete2 = (Button) inflate.findViewById(R.id.delete2);
                        Button delete3 = (Button) inflate.findViewById(R.id.delete3);
                        Button confirm = (Button) inflate.findViewById(R.id.confirm);
                        LinearLayout time2_layout = (LinearLayout) inflate.findViewById(R.id.time2_layout);
                        LinearLayout time3_layout = (LinearLayout) inflate.findViewById(R.id.time3_layout);
                        ChannelAdvancedActivity.this.list = new ArrayList();
                        ChannelAdvancedActivity.this.hash1 = new HashMap();
                        ChannelAdvancedActivity.this.hash2 = new HashMap();
                        ChannelAdvancedActivity.this.hash3 = new HashMap();
                        time1_start.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                        time1_end.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                        time2_start.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                        time2_end.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                        time3_start.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                        time3_end.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                        time1_start.setIs24HourView(Boolean.valueOf(true));
                        time1_end.setIs24HourView(Boolean.valueOf(true));
                        time2_start.setIs24HourView(Boolean.valueOf(true));
                        time2_end.setIs24HourView(Boolean.valueOf(true));
                        time3_start.setIs24HourView(Boolean.valueOf(true));
                        time3_end.setIs24HourView(Boolean.valueOf(true));
                        try {
                            ChannelAdvancedActivity.this.result = CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "getStopRecycleTime", null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!(ChannelAdvancedActivity.this.result == null || ChannelAdvancedActivity.this.result.size() <= 0 || "failed".equalsIgnoreCase((String) ChannelAdvancedActivity.this.result.get("result")))) {
                            try {
                                ChannelAdvancedActivity.this.list = JSONUtils.toList((String) ChannelAdvancedActivity.this.result.get("result"));
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                            if (ChannelAdvancedActivity.this.list != null && ChannelAdvancedActivity.this.list.size() > 0) {
                                if (ChannelAdvancedActivity.this.list.size() == 1) {
                                    ChannelAdvancedActivity.this.add_click_account = 1;
                                    ChannelAdvancedActivity.this.startTime(time1_start, time1_end, ChannelAdvancedActivity.this.hash1, ChannelAdvancedActivity.this.key1, ChannelAdvancedActivity.this.value1, 0);
                                }
                                if (ChannelAdvancedActivity.this.list.size() == 2) {
                                    time2_layout.setVisibility(View.VISIBLE);
                                    ChannelAdvancedActivity.this.add_click_account = 2;
                                    ChannelAdvancedActivity.this.startTime(time1_start, time1_end, ChannelAdvancedActivity.this.hash1, ChannelAdvancedActivity.this.key1, ChannelAdvancedActivity.this.value1, 0);
                                    ChannelAdvancedActivity.this.startTime(time2_start, time2_end, ChannelAdvancedActivity.this.hash2, ChannelAdvancedActivity.this.key2, ChannelAdvancedActivity.this.value2, 1);
                                }
                                if (ChannelAdvancedActivity.this.list.size() == 3) {
                                    add1.setEnabled(false);
                                    delete2.setEnabled(false);
                                    delete3.setEnabled(true);
                                    time2_layout.setVisibility(View.VISIBLE);
                                    time3_layout.setVisibility(View.VISIBLE);
                                    ChannelAdvancedActivity.this.add_click_account = 3;
                                    ChannelAdvancedActivity.this.startTime(time1_start, time1_end, ChannelAdvancedActivity.this.hash1, ChannelAdvancedActivity.this.key1, ChannelAdvancedActivity.this.value1, 0);
                                    ChannelAdvancedActivity.this.startTime(time2_start, time2_end, ChannelAdvancedActivity.this.hash2, ChannelAdvancedActivity.this.key2, ChannelAdvancedActivity.this.value2, 1);
                                    ChannelAdvancedActivity.this.startTime(time3_start, time3_end, ChannelAdvancedActivity.this.hash3, ChannelAdvancedActivity.this.key3, ChannelAdvancedActivity.this.value3, 2);
                                }
                            }
                        }
                        final LinearLayout linearLayout = time2_layout;
                        final LinearLayout linearLayout2 = time3_layout;
                        final Button button = add1;
                        final Button button2 = delete2;
                        add1.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                linearLayout.setVisibility(View.GONE);
                                if (ChannelAdvancedActivity.this.add_click_account == 2) {
                                    linearLayout2.setVisibility(View.GONE);
                                    button.setEnabled(false);
                                    button2.setEnabled(false);
                                }
                                ChannelAdvancedActivity.this.add_click_account = ChannelAdvancedActivity.this.add_click_account + 1;
                            }
                        });
                        final LinearLayout linearLayout3 = time2_layout;
                        delete2.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                linearLayout3.setVisibility(View.GONE);
                                ChannelAdvancedActivity.this.add_click_account = 1;
                            }
                        });
                        final LinearLayout linearLayout4 = time3_layout;
                        final Button button3 = delete2;
                        delete3.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                linearLayout4.setVisibility(View.GONE);
                                add1.setEnabled(true);
                                button3.setEnabled(true);
                                ChannelAdvancedActivity.this.add_click_account = 2;
                            }
                        });
                        time1_start.setOnTimeChangedListener(new OnTimeChangedListener() {
                            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                            }
                        });
                        time1_end.setOnTimeChangedListener(new OnTimeChangedListener() {
                            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                            }
                        });
                        time2_start.setOnTimeChangedListener(new OnTimeChangedListener() {
                            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                            }
                        });
                        time2_end.setOnTimeChangedListener(new OnTimeChangedListener() {
                            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                            }
                        });
                        time3_start.setOnTimeChangedListener(new OnTimeChangedListener() {
                            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                            }
                        });
                        time3_end.setOnTimeChangedListener(new OnTimeChangedListener() {
                            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                            }
                        });
                        final Dialog dialog = new Dialog(ChannelAdvancedActivity.this, R.style.Custom_dialog);
                        dialog.setContentView(inflate);
                        dialog.show();
                        LayoutParams params0 = dialog.getWindow().getAttributes();
                        params0.width = 900;
                        params0.height = 900;
                        dialog.getWindow().setAttributes(params0);
                        dialog.setCanceledOnTouchOutside(true);
                        final TimePicker timePicker = time1_start;
                        final TimePicker timePicker2 = time2_start;
                        final TimePicker timePicker3 = time3_start;
                        final TimePicker timePicker4 = time1_end;
                        final TimePicker timePicker5 = time2_end;
                        confirm.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                ChannelAdvancedActivity.this.list.clear();
                                ChannelAdvancedActivity.this.key1 = ChannelAdvancedActivity.this.appendTime(timePicker.getCurrentHour().intValue(), timePicker.getCurrentMinute().intValue());
                                ChannelAdvancedActivity.this.key2 = ChannelAdvancedActivity.this.appendTime(timePicker2.getCurrentHour().intValue(), timePicker2.getCurrentMinute().intValue());
                                ChannelAdvancedActivity.this.key3 = ChannelAdvancedActivity.this.appendTime(timePicker3.getCurrentHour().intValue(), timePicker3.getCurrentMinute().intValue());
                                ChannelAdvancedActivity.this.value1 = ChannelAdvancedActivity.this.appendTime(timePicker4.getCurrentHour().intValue(), timePicker4.getCurrentMinute().intValue());
                                ChannelAdvancedActivity.this.value2 = ChannelAdvancedActivity.this.appendTime(timePicker5.getCurrentHour().intValue(), timePicker5.getCurrentMinute().intValue());
                                ChannelAdvancedActivity.this.value3 = ChannelAdvancedActivity.this.appendTime(time3_end.getCurrentHour().intValue(), time3_end.getCurrentMinute().intValue());
                                ChannelAdvancedActivity.this.hash1.put(ChannelAdvancedActivity.this.key1, ChannelAdvancedActivity.this.value1);
                                ChannelAdvancedActivity.this.hash2.put(ChannelAdvancedActivity.this.key2, ChannelAdvancedActivity.this.value2);
                                ChannelAdvancedActivity.this.hash3.put(ChannelAdvancedActivity.this.key3, ChannelAdvancedActivity.this.value3);
                                if (ChannelAdvancedActivity.this.add_click_account == 1 && Integer.parseInt(ChannelAdvancedActivity.this.value1) > Integer.parseInt(ChannelAdvancedActivity.this.key1)) {
                                    ChannelAdvancedActivity.this.list.add(ChannelAdvancedActivity.this.hash1);
                                }
                                if (ChannelAdvancedActivity.this.add_click_account == 2 && Integer.parseInt(ChannelAdvancedActivity.this.key2) > Integer.parseInt(ChannelAdvancedActivity.this.value1) && Integer.parseInt(ChannelAdvancedActivity.this.value2) > Integer.parseInt(ChannelAdvancedActivity.this.key2)) {
                                    ChannelAdvancedActivity.this.list.add(ChannelAdvancedActivity.this.hash1);
                                    ChannelAdvancedActivity.this.list.add(ChannelAdvancedActivity.this.hash2);
                                }
                                if (ChannelAdvancedActivity.this.add_click_account == 3 && Integer.parseInt(ChannelAdvancedActivity.this.key3) > Integer.parseInt(ChannelAdvancedActivity.this.value2) && Integer.parseInt(ChannelAdvancedActivity.this.value3) > Integer.parseInt(ChannelAdvancedActivity.this.key3)) {
                                    ChannelAdvancedActivity.this.list.add(ChannelAdvancedActivity.this.hash1);
                                    ChannelAdvancedActivity.this.list.add(ChannelAdvancedActivity.this.hash2);
                                    ChannelAdvancedActivity.this.list.add(ChannelAdvancedActivity.this.hash3);
                                }
                                if (ChannelAdvancedActivity.this.list != null && ChannelAdvancedActivity.this.list.size() > 0) {
                                    HashMap hashmap = new HashMap();
                                    hashmap.put("list", ChannelAdvancedActivity.this.list);
                                    try {
                                        ChannelAdvancedActivity.this.result = CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "saveStopRecycleTime", hashmap);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                }
                                if (ChannelAdvancedActivity.this.result != null && ChannelAdvancedActivity.this.result.size() > 0) {
                                    if ("success".equalsIgnoreCase((String) ChannelAdvancedActivity.this.result.get("result"))) {
                                        Toast.makeText(ChannelAdvancedActivity.this.getBaseActivity(), ChannelAdvancedActivity.this.getString(R.string.setupCfgDoneSuccess), Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(ChannelAdvancedActivity.this.getBaseActivity(), ChannelAdvancedActivity.this.getString(R.string.setupCfgDoneFail), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }
                });
                setOnOff_Door.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                        if (inflate != null) {
                            inflate.removeAllViews();
                        }
                        inflate = (LinearLayout) LayoutInflater.from(ChannelAdvancedActivity.this).inflate(R.layout.dialog_setting_openclose_door, null, false);
                        final LinearLayout layout_toggle_OnOffDoors = (LinearLayout) inflate.findViewById(R.id.layout_toggle_OnOffDoors);
                        final ToggleButton toggleButton_OnOffDoors = (ToggleButton) inflate.findViewById(R.id.toggleButton_OnOffDoors);
                        final Button setHasDoor = (Button) inflate.findViewById(R.id.setHasDoor);
                        final Button setWithoutDoor = (Button) inflate.findViewById(R.id.setWithoutDoor);
                        final Drawable drawable = ChannelAdvancedActivity.this.getResources().getDrawable(R.drawable.tongdaoweihu_blue_bg);
                        if ("FALSE".equalsIgnoreCase(SysConfig.get("COM.PLC.HAS.DOOR"))) {
                            ChannelAdvancedActivity.this.SET_NO_DOOR_Sts = maintainOptContent.SET_NO_DOOR;
                            setWithoutDoor.setBackgroundColor(Color.parseColor("#202924"));
                            setWithoutDoor.setEnabled(false);
                        } else {
                            ChannelAdvancedActivity.this.SET_HAS_DOOR_Sts = maintainOptContent.SET_HAS_DOOR;
                            setHasDoor.setBackgroundColor(Color.parseColor("#202924"));
                            setHasDoor.setEnabled(false);
                            layout_toggle_OnOffDoors.setVisibility(View.VISIBLE);
                            GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                            HashMap bottleDoorStatus = new HashMap();
                            try {
                                Object object = guiCommonService.execute("GUIRecycleCommonService", "queryBottleDoorState", null).get("BottleDoorStatus");
                                if (object == null || !object.toString().equals("BOTTLE_DOOR_OPEN")) {
                                    toggleButton_OnOffDoors.setChecked(false);
                                    toggleButton_OnOffDoors.setGravity(21);
                                    toggleButton_OnOffDoors.setBackgroundResource(R.drawable.off);
                                } else {
                                    toggleButton_OnOffDoors.setChecked(true);
                                    toggleButton_OnOffDoors.setGravity(19);
                                    toggleButton_OnOffDoors.setBackgroundResource(R.drawable.on);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        toggleButton_OnOffDoors.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                Properties externalProp = new Properties();
                                if (isChecked) {
                                    externalProp.put("COM.PLC.DOOR.STATE.INIT", "OPEN");
                                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                                    SysConfig.set(externalProp);
                                    TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                                    ChannelAdvancedActivity.this.openDoor();
                                    toggleButton_OnOffDoors.setGravity(19);
                                    toggleButton_OnOffDoors.setBackgroundResource(R.drawable.on);
                                    return;
                                }
                                externalProp.put("COM.PLC.DOOR.STATE.INIT", "CLOSE");
                                PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                                SysConfig.set(externalProp);
                                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                                ChannelAdvancedActivity.this.closeDoor();
                                toggleButton_OnOffDoors.setGravity(21);
                                toggleButton_OnOffDoors.setBackgroundResource(R.drawable.off);
                            }
                        });
                        setHasDoor.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                if (ChannelAdvancedActivity.this.SET_NO_DOOR_Sts.equalsIgnoreCase(maintainOptContent.SET_NO_DOOR)) {
                                    if (ChannelAdvancedActivity.this.listOpt.contains(ChannelAdvancedActivity.this.SET_NO_DOOR_Sts)) {
                                        ChannelAdvancedActivity.this.listOpt.remove(ChannelAdvancedActivity.this.SET_NO_DOOR_Sts);
                                    }
                                    ChannelAdvancedActivity.this.SET_HAS_DOOR_Sts = maintainOptContent.SET_HAS_DOOR;
                                    ChannelAdvancedActivity.this.listOpt.add(ChannelAdvancedActivity.this.SET_HAS_DOOR_Sts);
                                }
                                GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                                if ("FALSE".equalsIgnoreCase(SysConfig.get("COM.PLC.HAS.DOOR"))) {
                                    Properties externalProp = new Properties();
                                    externalProp.put("COM.PLC.HAS.DOOR", "TRUE");
                                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                                    SysConfig.set(externalProp);
                                    setHasDoor.setEnabled(false);
                                    setHasDoor.setBackgroundColor(Color.parseColor("#202924"));
                                    setWithoutDoor.setEnabled(true);
                                    setWithoutDoor.setBackgroundDrawable(drawable);
                                    try {
                                        guiCommonService.execute("GUIMaintenanceCommonService", "backupRvmCode", null);
                                    } catch (Exception e) {
                                    }
                                }
                                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                                HashMap bottleDoorStatus = new HashMap();
                                try {
                                    Object object = guiCommonService.execute("GUIRecycleCommonService", "queryBottleDoorState", null).get("BottleDoorStatus");
                                    if (object == null || !object.toString().equals("BOTTLE_DOOR_OPEN")) {
                                        toggleButton_OnOffDoors.setChecked(false);
                                        toggleButton_OnOffDoors.setGravity(21);
                                        toggleButton_OnOffDoors.setBackgroundResource(R.drawable.off);
                                        layout_toggle_OnOffDoors.setVisibility(View.VISIBLE);
                                        Toast.makeText(ChannelAdvancedActivity.this, ((Button) v).getText(), Toast.LENGTH_SHORT).show();
                                    }
                                    toggleButton_OnOffDoors.setChecked(true);
                                    toggleButton_OnOffDoors.setGravity(19);
                                    toggleButton_OnOffDoors.setBackgroundResource(R.drawable.on);
                                    layout_toggle_OnOffDoors.setVisibility(View.VISIBLE);
                                    Toast.makeText(ChannelAdvancedActivity.this, ((Button) v).getText(), Toast.LENGTH_SHORT).show();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                        });
                        final Button button = setWithoutDoor;
                        final Button button2 = setHasDoor;
                        final Drawable drawable2 = drawable;
                        final LinearLayout linearLayout = layout_toggle_OnOffDoors;
                        setWithoutDoor.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                ChannelAdvancedActivity.this.openDoor();
                                if (ChannelAdvancedActivity.this.SET_HAS_DOOR_Sts.equalsIgnoreCase(maintainOptContent.SET_HAS_DOOR)) {
                                    if (ChannelAdvancedActivity.this.listOpt.contains(ChannelAdvancedActivity.this.SET_HAS_DOOR_Sts)) {
                                        ChannelAdvancedActivity.this.listOpt.remove(ChannelAdvancedActivity.this.SET_HAS_DOOR_Sts);
                                    }
                                    ChannelAdvancedActivity.this.SET_NO_DOOR_Sts = maintainOptContent.SET_NO_DOOR;
                                    ChannelAdvancedActivity.this.listOpt.add(ChannelAdvancedActivity.this.SET_NO_DOOR_Sts);
                                }
                                GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                                if ("TRUE".equalsIgnoreCase(SysConfig.get("COM.PLC.HAS.DOOR"))) {
                                    Properties externalProp = new Properties();
                                    externalProp.put("COM.PLC.HAS.DOOR", "FALSE");
                                    externalProp.put("COM.PLC.DOOR.STATE.INIT", "OPEN");
                                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                                    SysConfig.set(externalProp);
                                    button.setEnabled(false);
                                    button.setBackgroundColor(Color.parseColor("#202924"));
                                    button2.setEnabled(true);
                                    button2.setBackgroundDrawable(drawable2);
                                    try {
                                        guiCommonService.execute("GUIMaintenanceCommonService", "backupRvmCode", null);
                                    } catch (Exception e) {
                                    }
                                }
                                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                                if (linearLayout != null) {
                                    linearLayout.setVisibility(View.INVISIBLE);
                                }
                                Toast.makeText(ChannelAdvancedActivity.this, ((Button) v).getText(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        Dialog dialog = new Dialog(ChannelAdvancedActivity.this, R.style.Custom_dialog);
                        dialog.setContentView(inflate);
                        dialog.setTitle(" " + ChannelAdvancedActivity.this.getResources().getString(R.string.setOnOff_Door) + ":");
                        dialog.show();
                        LayoutParams params0 = dialog.getWindow().getAttributes();
                        params0.width = 900;
                        params0.height = 420;
                        dialog.getWindow().setAttributes(params0);
                        dialog.setCanceledOnTouchOutside(true);
                    }
                });
                final TextView heartToneDetection_status = (TextView) ChannelAdvancedActivity.this.findViewById(R.id.heartToneDetection_status);
                final Button heartToneDetectionButton = (Button) ChannelAdvancedActivity.this.findViewById(R.id.heartToneDetectionButton);
                Button openCloseVoice = (Button) lin.findViewById(R.id.openCloseVoiceButton);
                if (!ChannelAdvancedActivity.this.listStaffSet.contains(SysDef.staffPermission.setBottleMaxNum)) {
                    setBottleMaxNum.setVisibility(View.GONE);
                }
                if (!ChannelAdvancedActivity.this.listStaffSet.contains(SysDef.staffPermission.faultDiagnosis)) {
                    faultDiagnosisButton.setVisibility(View.GONE);
                }
                if (!ChannelAdvancedActivity.this.listStaffSet.contains(SysDef.staffPermission.TimingBoot)) {
                    cancelTimingButton.setVisibility(View.GONE);
                }
                if (!ChannelAdvancedActivity.this.listStaffSet.contains(SysDef.staffPermission.heartToneDetection)) {
                    ChannelAdvancedActivity.this.findViewById(R.id.layout_heartToneDetection).setVisibility(View.GONE);
                }
                if (!ChannelAdvancedActivity.this.listStaffSet.contains(SysDef.staffPermission.setOnOffDoor)) {
                    setOnOff_Door.setVisibility(View.GONE);
                }
                if (!ChannelAdvancedActivity.this.listStaffSet.contains(SysDef.staffPermission.start_using)) {
                    startUsingButton.setVisibility(View.GONE);
                }
                if (!ChannelAdvancedActivity.this.listStaffSet.contains(SysDef.staffPermission.openCloseVoice)) {
                    openCloseVoice.setVisibility(View.GONE);
                }
                setBottleMaxNum.setOnClickListener(new OnClickListener() {
                    public void onClick(View arg0) {
                        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                        HashMap retPkg = new HashMap();
                        try {
                            retPkg = guiCommonService.execute("GUIRecycleCommonService", "queryBottleMaxValue", null);
                            if (retPkg != null) {
                                String bottleNumMax = (String) retPkg.get("bottleMax");
                                String bottleNumAlarm = (String) retPkg.get("bottleAlarm");
                                View bottleEntryView = LayoutInflater.from(ChannelAdvancedActivity.this).inflate(R.layout.dialog_setting_setbottlemax, null);
                                final EditText bottleEditMax = (EditText) bottleEntryView.findViewById(R.id.etBottleNumMax);
                                final EditText bottleEditAlarm = (EditText) bottleEntryView.findViewById(R.id.etBottleNumAlarm);
                                ChannelAdvancedActivity.this.showOrHideKeybordAndResetTime(bottleEditMax, ChannelAdvancedActivity.this.timeoutAction);
                                ChannelAdvancedActivity.this.showOrHideKeybordAndResetTime(bottleEditAlarm, ChannelAdvancedActivity.this.timeoutAction);
                                bottleEditMax.setText(bottleNumMax);
                                bottleEditAlarm.setText(bottleNumAlarm);
                                final Dialog dialog = new Dialog(ChannelAdvancedActivity.this, R.style.Custom_dialog);
                                dialog.setContentView(bottleEntryView);
                                dialog.setTitle(" " + ChannelAdvancedActivity.this.getResources().getString(R.string.setAlarmValue) + ": ");
                                dialog.show();
                                LayoutParams params0 = dialog.getWindow().getAttributes();
                                params0.width = 900;
                                params0.height = 420;
                                dialog.getWindow().setAttributes(params0);
                                ((Button) bottleEntryView.findViewById(R.id.btnConfirm)).setOnClickListener(new OnClickListener() {
                                    public void onClick(View v) {
                                        if (ChannelAdvancedActivity.this.SET_ALARM_VALUE_Sts == null) {
                                            ChannelAdvancedActivity.this.SET_ALARM_VALUE_Sts = maintainOptContent.SET_ALARM_VALUE;
                                            ChannelAdvancedActivity.this.listOpt.add(ChannelAdvancedActivity.this.SET_ALARM_VALUE_Sts);
                                        }
                                        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                                        HashMap ret = new HashMap();
                                        HashMap<String, Object> hsmpPkg = new HashMap();
                                        if (bottleEditMax.getText().toString().trim().length() <= 0 || bottleEditAlarm.getText().toString().trim().length() <= 0) {
                                            Toast.makeText(ChannelAdvancedActivity.this, R.string.setAlarmHint, Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        String EditMaxText = bottleEditMax.getText().toString();
                                        String EditAlarmText = bottleEditAlarm.getText().toString();
                                        hsmpPkg.put("EditMaxText", EditMaxText);
                                        hsmpPkg.put("EditAlarmText", EditAlarmText);
                                        try {
                                            ret = guiCommonService.execute("GUIRecycleCommonService", "updateBottleMaxValue", hsmpPkg);
                                            if (ret != null) {
                                                String retPkg = (String) ret.get("RET_VALUE");
                                                if ("NO_SET_VALUE".equalsIgnoreCase(retPkg)) {
                                                    Toast.makeText(ChannelAdvancedActivity.this, R.string.setAlarmHint, Toast.LENGTH_SHORT).show();
                                                }
                                                if ("SUCCESS_SET_VALUE".equalsIgnoreCase(retPkg)) {
                                                    Toast.makeText(ChannelAdvancedActivity.this, R.string.setAlarmSuccess, Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                    return;
                                                }
                                                return;
                                            }
                                            Toast.makeText(ChannelAdvancedActivity.this, R.string.setAlarmFailed, Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                ((Button) bottleEntryView.findViewById(R.id.btnClose)).setOnClickListener(new OnClickListener() {
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                faultDiagnosisButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        String DiagnoseClass = SysConfig.get("Diagnose.class");
                        if (!StringUtils.isBlank(DiagnoseClass)) {
                            try {
                                Intent intent = new Intent(ChannelAdvancedActivity.this, Class.forName(DiagnoseClass));
                                intent.putExtra("STAFF_PERMISSION", ChannelAdvancedActivity.this.staffPermission);
                                intent.putStringArrayListExtra("LIST", (ArrayList) ChannelAdvancedActivity.this.listOpt);
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                ChannelAdvancedActivity.this.startActivity(intent);
                                ChannelAdvancedActivity.this.finish();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                    }
                });
                cancelTimingButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                        View timing_onofff = LayoutInflater.from(ChannelAdvancedActivity.this).inflate(R.layout.dialog_setting_timing_onoff, null, false);
                        ChannelAdvancedActivity.this.mytp1 = (TimePicker) timing_onofff.findViewById(R.id.setStartTimeTP1);
                        ChannelAdvancedActivity.this.mytp1.setIs24HourView(Boolean.valueOf(true));
                        ChannelAdvancedActivity.this.mytp2 = (TimePicker) timing_onofff.findViewById(R.id.setShutDownTimeTP2);
                        ChannelAdvancedActivity.this.mytp2.setIs24HourView(Boolean.valueOf(true));
                        ChannelAdvancedActivity.this.setCurrentTime();
                        Button bn_temp = (Button) timing_onofff.findViewById(R.id.bn_temp);
                        Button bn_suspended = (Button) timing_onofff.findViewById(R.id.bn_suspended);
                        ((Button) timing_onofff.findViewById(R.id.bn_setPowerOnAndPowerOffTime)).setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                if (ChannelAdvancedActivity.this.SET_RVM_ON_TIME_Sts == null) {
                                    ChannelAdvancedActivity.this.SET_RVM_ON_TIME_Sts = maintainOptContent.SET_RVM_ON_TIME;
                                    ChannelAdvancedActivity.this.listOpt.add(ChannelAdvancedActivity.this.SET_RVM_ON_TIME_Sts);
                                }
                                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                                String startHour = String.valueOf(ChannelAdvancedActivity.this.mytp1.getCurrentHour());
                                if (!StringUtils.isBlank(startHour) && startHour.length() == 1) {
                                    startHour = "0" + startHour;
                                }
                                String startMinute = String.valueOf(ChannelAdvancedActivity.this.mytp1.getCurrentMinute());
                                if (!StringUtils.isBlank(startMinute) && startMinute.length() == 1) {
                                    startMinute = "0" + startMinute;
                                }
                                String shutHour = String.valueOf(ChannelAdvancedActivity.this.mytp2.getCurrentHour());
                                if (!StringUtils.isBlank(shutHour) && shutHour.length() == 1) {
                                    shutHour = "0" + shutHour;
                                }
                                String shutMinute = String.valueOf(ChannelAdvancedActivity.this.mytp2.getCurrentMinute());
                                if (!StringUtils.isBlank(shutMinute) && shutMinute.length() == 1) {
                                    shutMinute = "0" + shutMinute;
                                }
                                String startTime = startHour + ":" + startMinute;
                                String shutdownTime = shutHour + ":" + shutMinute;
                                Properties prop = new Properties();
                                prop.setProperty("RVM.POWER.ON.TIME", startTime);
                                prop.setProperty("RVM.POWER.OFF.TIME", shutdownTime);
                                HashMap hsmpParam = new HashMap();
                                hsmpParam.put("POWER_ON_TIME", startTime);
                                hsmpParam.put("POWER_OFF_TIME", shutdownTime);
                                try {
                                    CommService.getCommService().execute("RVM_POWER_OFF_ENABLE", JSONUtils.toJSON(hsmpParam));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                PropUtils.update(SysConfig.get("EXTERNAL.FILE"), prop);
                                SysConfig.set(prop);
                                RVMShell.backupExternalConfig();
                                Toast.makeText(ChannelAdvancedActivity.this, ChannelAdvancedActivity.this.getString(R.string.onTiming_success), Toast.LENGTH_SHORT).show();
                                try {
                                    CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "backupRvmCode", null);
                                } catch (Exception e2) {
                                }
                            }
                        });
                        bn_temp.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                                try {
                                    CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "UnPowerOff", null);
                                    Toast.makeText(ChannelAdvancedActivity.this, ChannelAdvancedActivity.this.getString(R.string.cancelTimingRemind), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        bn_suspended.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                if (ChannelAdvancedActivity.this.SET_RVM_OFF_TIME_Sts == null) {
                                    ChannelAdvancedActivity.this.SET_RVM_OFF_TIME_Sts = maintainOptContent.SET_RVM_OFF_TIME;
                                    ChannelAdvancedActivity.this.listOpt.add(ChannelAdvancedActivity.this.SET_RVM_OFF_TIME_Sts);
                                }
                                TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                                Properties prop = new Properties();
                                prop.setProperty("RVM.POWER.ON.TIME", "");
                                prop.setProperty("RVM.POWER.OFF.TIME", "");
                                try {
                                    CommService.getCommService().execute("RVM_POWER_OFF_DISABLE", null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                PropUtils.update(SysConfig.get("EXTERNAL.FILE"), prop);
                                SysConfig.set(prop);
                                RVMShell.backupExternalConfig();
                                Toast.makeText(ChannelAdvancedActivity.this, ChannelAdvancedActivity.this.getString(R.string.cancelTiming_success), Toast.LENGTH_SHORT).show();
                                try {
                                    CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "backupRvmCode", null);
                                } catch (Exception e2) {
                                }
                            }
                        });
                        Dialog dialog = new Dialog(ChannelAdvancedActivity.this, R.style.Custom_dialog);
                        dialog.setContentView(timing_onofff);
                        dialog.setTitle(" " + ChannelAdvancedActivity.this.getResources().getString(R.string.timingSet) + ": ");
                        dialog.show();
                        LayoutParams params0 = dialog.getWindow().getAttributes();
                        params0.width = 900;
                        params0.height = 420;
                        dialog.getWindow().setAttributes(params0);
                    }
                });
                heartToneDetectionButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                        HashMap<String, Object> hsmpParam = new HashMap();
                        if (ChannelAdvancedActivity.this.enableHeartToneDetection) {
                            heartToneDetectionButton.setText(R.string.startHeartToneDetection);
                            heartToneDetection_status.setText(R.string.HeartToneDetectionStopped);
                            ChannelAdvancedActivity.this.enableHeartToneDetection = false;
                            hsmpParam.put("ENABLE", "FALSE");
                        } else {
                            heartToneDetectionButton.setText(R.string.stopHeartToneDetection);
                            heartToneDetection_status.setText(R.string.HeartToneDetectionStarted);
                            ChannelAdvancedActivity.this.enableHeartToneDetection = true;
                            hsmpParam.put("ENABLE", "TRUE");
                        }
                        try {
                            guiCommonService.execute("GUIMaintenanceCommonService", "setHeartBeat", hsmpParam);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                    }
                });
                if ("TRUE".equalsIgnoreCase(SysConfig.get("START_USING"))) {
                    startUsingButton.setBackgroundColor(-7829368);
                    startUsingButton.setEnabled(false);
                }
                startUsingButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        String rvmId = SysConfig.get("RVM.CODE");
                        if (StringUtils.isBlank(rvmId) || rvmId.equals("0")) {
                            String rvmValue;
                            String rvmNull = ChannelAdvancedActivity.this.getResources().getString(R.string.rvmNull);
                            String rvmZero = ChannelAdvancedActivity.this.getResources().getString(R.string.rvmZero);
                            if (StringUtils.isBlank(rvmId)) {
                                rvmValue = rvmNull;
                            } else {
                                rvmValue = rvmZero;
                            }
                            Toast toast = Toast.makeText(ChannelAdvancedActivity.this.getBaseContext(), rvmValue, Toast.LENGTH_LONG);
                            toast.setGravity(17, 0, 0);
                            toast.show();
                            return;
                        }
                        try {
                            SysGlobal.execute(new Runnable() {
                                public void run() {
                                    if (!StringUtils.isBlank(SysConfig.get("CHANNEL.PHONE.NUMER"))) {
                                        HashMap<String, Object> hashResult = null;
                                        try {
                                            hashResult = CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "rvmStartUsing", null);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        String result = null;
                                        if (hashResult != null && hashResult.size() > 0) {
                                            result = (String) hashResult.get("RESULT");
                                        }
                                        Looper.prepare();
                                        if (StringUtils.isBlank(result) || !"success".equalsIgnoreCase(result)) {
                                            Toast.makeText(ChannelAdvancedActivity.this, R.string.start_using_failed, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ChannelAdvancedActivity.this, R.string.start_using_success, Toast.LENGTH_SHORT).show();
                                            ChannelAdvancedActivity.this.runOnUiThread(ChannelAdvancedActivity.this.updateThread);
                                            Long Time = Long.valueOf(new Date().getTime());
                                            Properties externalProp = new Properties();
                                            externalProp.put("START_USING", "TRUE");
                                            externalProp.setProperty("START_USING_TIME", Time.toString());
                                            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                                            SysConfig.set(externalProp);
                                            RVMShell.backupExternalConfig();
                                        }
                                        Looper.loop();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                    }
                });
                dubuggingButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        try {
                            CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "startDubugg", null);
                            Toast toast = Toast.makeText(ChannelAdvancedActivity.this.getBaseContext(), R.string.maintain_update_warn, Toast.LENGTH_SHORT);
                            toast.setGravity(17, 0, 0);
                            toast.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                openCloseVoice.setOnClickListener(new OnClickListener() {
                    public void onClick(View arg0) {
                        View voiceAdjust = LayoutInflater.from(ChannelAdvancedActivity.this).inflate(R.layout.voice_set_dialog, null, false);
                        ImageButton btn_up = (ImageButton) voiceAdjust.findViewById(R.id.up);
                        ImageButton btn_down = (ImageButton) voiceAdjust.findViewById(R.id.down);
                        final ImageButton btn_mute = (ImageButton) voiceAdjust.findViewById(R.id.mute);
                        final SeekBar sb_voice = (SeekBar) voiceAdjust.findViewById(R.id.sb_voice);
                        ChannelAdvancedActivity.this.audioManager = (AudioManager) ChannelAdvancedActivity.this.getSystemService(AUDIO_SERVICE);
                        ChannelAdvancedActivity.this.max_Volume = ChannelAdvancedActivity.this.audioManager.getStreamMaxVolume(3);
                        if (StringUtils.isBlank(SysConfig.get("VOICEVOLUME"))) {
                            ChannelAdvancedActivity.this.cur_Volume = 0;
                        } else {
                            ChannelAdvancedActivity.this.cur_Volume = Integer.parseInt(SysConfig.get("VOICEVOLUME"));
                        }
                        ChannelAdvancedActivity.this.step_Volume = ChannelAdvancedActivity.this.max_Volume / 10;
                        sb_voice.setMax(ChannelAdvancedActivity.this.max_Volume);
                        sb_voice.setProgress(ChannelAdvancedActivity.this.cur_Volume);
                        btn_up.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                ChannelAdvancedActivity.this.cur_Volume = ChannelAdvancedActivity.this.cur_Volume + ChannelAdvancedActivity.this.step_Volume;
                                if (ChannelAdvancedActivity.this.cur_Volume >= ChannelAdvancedActivity.this.max_Volume) {
                                    ChannelAdvancedActivity.this.cur_Volume = ChannelAdvancedActivity.this.max_Volume;
                                }
                                sb_voice.setProgress(ChannelAdvancedActivity.this.cur_Volume);
                                ChannelAdvancedActivity.this.putPlayVoice(ChannelAdvancedActivity.this.cur_Volume);
                            }
                        });
                        btn_down.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                ChannelAdvancedActivity.this.cur_Volume = ChannelAdvancedActivity.this.cur_Volume - ChannelAdvancedActivity.this.step_Volume;
                                if (ChannelAdvancedActivity.this.cur_Volume <= 0) {
                                    ChannelAdvancedActivity.this.cur_Volume = 0;
                                }
                                if (ChannelAdvancedActivity.this.cur_Volume == 0) {
                                    btn_mute.setBackgroundResource(R.drawable.voice_mute_press);
                                }
                                sb_voice.setProgress(ChannelAdvancedActivity.this.cur_Volume);
                                ChannelAdvancedActivity.this.putPlayVoice(ChannelAdvancedActivity.this.cur_Volume);
                            }
                        });
                        btn_mute.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                sb_voice.setProgress(0);
                                ChannelAdvancedActivity.this.putPlayVoice(0);
                            }
                        });
                        sb_voice.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                            public void onStopTrackingTouch(SeekBar seekBar) {
                            }

                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                ChannelAdvancedActivity.this.cur_Volume = progress;
                                ChannelAdvancedActivity.this.putPlayVoice(ChannelAdvancedActivity.this.cur_Volume);
                            }
                        });
                        Dialog dialog = new Dialog(ChannelAdvancedActivity.this, R.style.Custom_dialog);
                        dialog.setContentView(voiceAdjust);
                        dialog.setTitle(" " + ChannelAdvancedActivity.this.getResources().getString(R.string.adjust_volume) + ": ");
                        dialog.show();
                        LayoutParams params0 = dialog.getWindow().getAttributes();
                        params0.width = 900;
                        params0.height = 420;
                        dialog.getWindow().setAttributes(params0);
                        TimeoutTask.getTimeoutTask().reset(ChannelAdvancedActivity.this.timeoutAction);
                        try {
                            CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "backupRvmCode", null);
                        } catch (Exception e) {
                        }
                    }
                });
            }
        });
    }

    private void putPlayVoice(int num) {
        this.externalProp = new Properties();
        if (num == 0) {
            this.externalProp.put("IS_PLAY_SOUNDS", "FALSE");
        } else {
            this.externalProp.put("IS_PLAY_SOUNDS", "TRUE");
        }
        this.externalProp.put("VOICEVOLUME", String.valueOf(num));
        PropUtils.update(SysConfig.get("EXTERNAL.FILE"), this.externalProp);
        SysConfig.set(this.externalProp);
        RVMShell.backupExternalConfig();
        this.audioManager.setStreamVolume(3, num, 0);
    }

    public boolean onTouchEvent(MotionEvent event) {
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        return super.onTouchEvent(event);
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    public TimeoutAction getTimeoutAction() {
        return this.timeoutAction;
    }

    private void showCheckBox() {
        int i;
        CheckBox cbDonation = (CheckBox) findViewById(R.id.cbDonation);
        CheckBox cbGreenCard = (CheckBox) findViewById(R.id.cbGreenCard);
        CheckBox cbCoupon = (CheckBox) findViewById(R.id.cbCoupon);
        CheckBox cbTrafficCard = (CheckBox) findViewById(R.id.cbTrafficCard);
        CheckBox cbMobilePhone = (CheckBox) findViewById(R.id.cbMobilePhone);
        CheckBox cbD2Code = (CheckBox) findViewById(R.id.cbD2Code);
        CheckBox cbWechat = (CheckBox) findViewById(R.id.cbWechat);
        CheckBox cbAlipay = (CheckBox) findViewById(R.id.cbAlipay);
        CheckBox cbBDJ = (CheckBox) findViewById(R.id.cbBDJ);
        String str = SysConfig.get("VENDING.WAY.SET");
        if (str.length() > 0) {
            String[] strSet = str.split(";");
            for (i = 0; i < strSet.length; i++) {
                if ("DONATION".equalsIgnoreCase(strSet[i])) {
                    cbDonation.setVisibility(View.VISIBLE);
                }
                if ("CARD".equalsIgnoreCase(strSet[i])) {
                    cbGreenCard.setVisibility(View.VISIBLE);
                }
                if ("COUPON".equalsIgnoreCase(strSet[i])) {
                    cbCoupon.setVisibility(View.VISIBLE);
                }
                if ("TRANSPORTCARD".equalsIgnoreCase(strSet[i])) {
                    cbTrafficCard.setVisibility(View.VISIBLE);
                }
                if ("PHONE".equalsIgnoreCase(strSet[i])) {
                    cbMobilePhone.setVisibility(View.VISIBLE);
                }
                if ("QRCODE".equalsIgnoreCase(strSet[i])) {
                    cbD2Code.setVisibility(View.VISIBLE);
                }
                if ("WECHAT".equalsIgnoreCase(strSet[i])) {
                    cbWechat.setVisibility(View.VISIBLE);
                }
                if ("ALIPAY".equalsIgnoreCase(strSet[i])) {
                    cbAlipay.setVisibility(View.VISIBLE);
                }
                if ("BDJ".equalsIgnoreCase(strSet[i])) {
                    cbBDJ.setVisibility(View.VISIBLE);
                }
            }
        }
        List<String> listVendingWay = new ArrayList();
        String VENDING_WAY = SysConfig.get("VENDING.WAY");
        if (!StringUtils.isBlank(VENDING_WAY)) {
            String[] VENDING_WAY_ARRAY = VENDING_WAY.split(";");
            for (String add : VENDING_WAY_ARRAY) {
                listVendingWay.add(add);
            }
        }
        if (listVendingWay.contains("DONATION")) {
            cbDonation.setChecked(true);
        }
        if (listVendingWay.contains("TRANSPORTCARD")) {
            cbTrafficCard.setChecked(true);
        }
        if (listVendingWay.contains("PHONE")) {
            cbMobilePhone.setChecked(true);
        }
        if (listVendingWay.contains("COUPON")) {
            cbCoupon.setChecked(true);
        }
        if (listVendingWay.contains("QRCODE")) {
            cbD2Code.setChecked(true);
        }
        if (listVendingWay.contains("CARD")) {
            cbGreenCard.setChecked(true);
        }
        if (listVendingWay.contains("WECHAT")) {
            cbWechat.setChecked(true);
        }
        if (listVendingWay.contains("ALIPAY")) {
            cbAlipay.setChecked(true);
        }
        if (listVendingWay.contains("BDJ")) {
            cbBDJ.setChecked(true);
        }
    }

    public void decideStaffPermission() {
        Intent intent = getIntent();
        this.staffPermission = intent.getStringExtra("STAFF_PERMISSION");
        this.listOpt = intent.getStringArrayListExtra("LIST");
    }

    public void closeDoor() {
        HashMap hsmpJSON = new HashMap();
        hsmpJSON.put("SAVE_CONFIG", "TRUE");
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "closeBottleDoor", hsmpJSON);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openDoor() {
        HashMap hsmpJSON = new HashMap();
        hsmpJSON.put("SAVE_CONFIG", "TRUE");
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "openBottleDoor", hsmpJSON);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCurrentTime() {
        String startTime = SysConfig.get("RVM.POWER.ON.TIME");
        if (!StringUtils.isBlank(startTime)) {
            String[] startTimes = startTime.split(":");
            this.mytp1.setCurrentHour(Integer.valueOf(Integer.parseInt(startTimes[0])));
            this.mytp1.setCurrentMinute(Integer.valueOf(Integer.parseInt(startTimes[1])));
        }
        String endTime = SysConfig.get("RVM.POWER.OFF.TIME");
        if (!StringUtils.isBlank(endTime)) {
            String[] endTimes = endTime.split(":");
            this.mytp2.setCurrentHour(Integer.valueOf(Integer.parseInt(endTimes[0])));
            this.mytp2.setCurrentMinute(Integer.valueOf(Integer.parseInt(endTimes[1])));
        }
    }

    private String appendTime(int hourOfDay, int minute) {
        String appendTime = "";
        String hour = "";
        String min = "";
        if (hourOfDay < 0 || hourOfDay >= 10) {
            hour = "" + hourOfDay;
        } else {
            hour = "0" + hourOfDay;
        }
        if (minute < 0 || minute >= 10) {
            min = "" + minute;
        } else {
            min = "0" + minute;
        }
        return hour + min + "00";
    }

    private void startTime(TimePicker start, TimePicker end, HashMap<String, String> hashmap, String key, String value, int i) {
        hashmap = (HashMap) this.list.get(i);
        for (String key2 : hashmap.keySet()) {
            value = hashmap.get(key2);
        }
        start.setCurrentHour(Integer.valueOf(Integer.parseInt(key2.substring(0, 2))));
        start.setCurrentMinute(Integer.valueOf(Integer.parseInt(key2.substring(2, 4))));
        end.setCurrentHour(Integer.valueOf(Integer.parseInt(value.substring(0, 2))));
        end.setCurrentMinute(Integer.valueOf(Integer.parseInt(value.substring(2, 4))));
    }
}
