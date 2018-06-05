package com.incomrecycle.prms.rvm.gui.activity.starput;

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
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.BroadcastTaskCommonService;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.gui.activity.view.MyGifView;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class Concern_bdj extends BaseActivity {
    private String PRODUCT_TYPE = ((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"));
    private List<HashMap<String, String>> REBATE_PROCESS_CHANGE_LIST = new ArrayList();
    private List<HashMap<String, String>> REBATE_PROCESS_LIST = new ArrayList();
    private Button bdj_concern_back = null;
    private Button bdj_concern_confirm = null;
    private OnClickListener btn_listener = new OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bdj_remind_back:
                    Intent intent;
                    if ("PAPER".equals(Concern_bdj.this.PRODUCT_TYPE)) {
                        intent = new Intent(Concern_bdj.this, SelectRecycleActivity.class);
                        intent.putExtra("RECYCLE", "RECYCLEPAPER");
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        Concern_bdj.this.startActivity(intent);
                        Concern_bdj.this.finish();
                        return;
                    }
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.BDJ_CONCERN_BACK);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    intent = new Intent(Concern_bdj.this, SelectRecycleActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    Concern_bdj.this.startActivity(intent);
                    Concern_bdj.this.finish();
                    return;
                case R.id.bdj_remind_confirm:
                    Concern_bdj.this.gotoNextStep();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean isFinishCalled = false;
    private TextView remindText;
    private MyGifView show_concern_img;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    Concern_bdj.this.timeout_text.setText("" + remainedSeconds);
                    if (remainedSeconds == 0) {
                        Concern_bdj.this.gotoNextStep();
                    }
                }
            };
            Concern_bdj.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TextView timeout_text;

    protected void onStart() {
        super.onStart();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.SHOW.ACTIVITY.WECHAT.TIME")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onStop() {
        super.onStop();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_concern_bdj);
        backgroundToActivity();
        initview();
        this.bdj_concern_back.setOnClickListener(this.btn_listener);
        this.bdj_concern_confirm.setOnClickListener(this.btn_listener);
    }

    private void initview() {
        this.bdj_concern_back = (Button) findViewById(R.id.bdj_remind_back);
        this.bdj_concern_confirm = (Button) findViewById(R.id.bdj_remind_confirm);
        this.timeout_text = (TextView) findViewById(R.id.bdj_remind_time);
        this.remindText = (TextView) findViewById(R.id.bdj_remind_remind_text1);
        this.show_concern_img = (MyGifView) findViewById(R.id.show_concern_img);
        String adconfURL = (String) GUIGlobal.getCurrentSession("AD_CONF_URL");
        this.remindText.setText(R.string.wechat_hint_lv);
        initlvguan(adconfURL);
    }

    private void initlvguan(String adconfURL) {
        this.REBATE_PROCESS_LIST = BroadcastTaskCommonService.getRebateProcessList(adconfURL);
        if (this.REBATE_PROCESS_LIST != null && this.REBATE_PROCESS_LIST.size() > 0) {
            String path = (String) ((HashMap) this.REBATE_PROCESS_LIST.get(0)).get(AllAdvertisement.MAIN_PICTURE_PATH);
            if (!StringUtils.isBlank(path) && new File(path).isFile()) {
                this.show_concern_img.updateResource(-1, path);
                return;
            }
        }
        this.show_concern_img.updateResource(R.drawable.lvguanzhu, null);
    }

    private void initlvchange(String adconfURL) {
        this.REBATE_PROCESS_CHANGE_LIST = BroadcastTaskCommonService.getRebateProcessChangeList(adconfURL);
        if (this.REBATE_PROCESS_CHANGE_LIST != null && this.REBATE_PROCESS_CHANGE_LIST.size() > 0) {
            String path = (String) ((HashMap) this.REBATE_PROCESS_CHANGE_LIST.get(0)).get(AllAdvertisement.MAIN_PICTURE_PATH);
            if (!StringUtils.isBlank(path) && new File(path).isFile()) {
                this.show_concern_img.updateResource(-1, path);
                return;
            }
        }
        this.show_concern_img.updateResource(R.drawable.lvchange, null);
    }

    public void updateLanguage() {
    }

    private void gotoNextStep() {
        executeGUIAction(false, new GUIActionGotoServiceProcess(), new Object[]{getBaseActivity(), Integer.valueOf(2), "BDJ"});
        TimeoutTask.getTimeoutTask().addTimeoutAction(new TimeoutAction() {
            public void apply(int forwardSeconds, int remainedSeconds) {
                if (remainedSeconds == 0) {
                    TimeoutTask.getTimeoutTask().removeTimeoutAction(this);
                    HashMap hsmpGUIEvent = new HashMap();
                    hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                    hsmpGUIEvent.put("EVENT", "CMD");
                    hsmpGUIEvent.put("CMD", "timeoutEnd");
                    hsmpGUIEvent.put("BaseActivity", Concern_bdj.this);
                    GUIGlobal.getEventMgr().addEvent(hsmpGUIEvent);
                }
            }
        }, 5, true);
    }

    public void doEvent(HashMap hsmpEvent) {
        if ("CMD".equalsIgnoreCase((String) hsmpEvent.get("EVENT"))) {
            Intent intent;
            String CMD = (String) hsmpEvent.get("CMD");
            if ("recycleEnd".equalsIgnoreCase(CMD)) {
                if (((BaseActivity) hsmpEvent.get("BaseActivity")) == this && !this.isFinishCalled) {
                    this.isFinishCalled = true;
                    intent = new Intent(this, BdjResultActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("JSON", (String) hsmpEvent.get("JSON"));
                    startActivity(intent);
                    finish();
                } else {
                    return;
                }
            }
            if ("timeoutEnd".equalsIgnoreCase(CMD) && ((BaseActivity) hsmpEvent.get("BaseActivity")) == this && !this.isFinishCalled) {
                this.isFinishCalled = true;
                intent = new Intent(this, BdjResultActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        }
    }
}
