package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.app.Activity;
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
import com.incomrecycle.prms.rvm.common.SysDef.DoorStatus;
import com.incomrecycle.prms.rvm.common.SysDef.ServiceName;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.gui.action.GUIActionRecycleStart;
import com.incomrecycle.prms.rvm.gui.action.GUIActionTakePhoto;
import com.incomrecycle.prms.rvm.gui.activity.view.MyGifView;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class PutPaperActivity extends BaseActivity {
    private String VENDING_WAY = null;
    private Class classs = null;
    private GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
    private MyGifView imageView1 = null;
    private boolean isClick = false;
    private boolean isFinishCalled = false;
    private TimeoutAction timeoutActionForPaperWeigh = new TimeoutAction() {
        private Activity context = PutPaperActivity.this;

        public void apply(int forwardSeconds, int remainedSeconds) {
            final int remainedSecond = remainedSeconds - 1;
            if (remainedSecond == -1) {
                this.context.runOnUiThread(PutPaperActivity.this.updateThread);
                return;
            }
            final TextView textTime = (TextView) PutPaperActivity.this.findViewById(R.id.touzhi_time);
            textTime.post(new Runnable() {
                public void run() {
                    textTime.setText("" + remainedSecond);
                }
            });
        }
    };
    private TimeoutAction timeoutActionForPutPaper = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue() - 1;
                    if (remainedSeconds == -1) {
                        PutPaperActivity.this.stopPutPaper();
                    } else {
                        ((TextView) PutPaperActivity.this.findViewById(R.id.touzhi_time)).setText("" + remainedSeconds);
                    }
                }
            };
            PutPaperActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TimeoutAction timeoutActionForSecondCloseDoor = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            final int remainedSecond = remainedSeconds - 1;
            if (remainedSecond == -1) {
                PutPaperActivity.this.stopPutPaperPhase();
                return;
            }
            final TextView textTime = (TextView) PutPaperActivity.this.findViewById(R.id.touzhi_time);
            textTime.post(new Runnable() {
                public void run() {
                    textTime.setText("" + remainedSecond);
                }
            });
        }
    };
    Runnable updateThread = new Runnable() {
        public void run() {
            ((Button) PutPaperActivity.this.findViewById(R.id.put_paper_close)).setVisibility(View.GONE);
            ((Button) PutPaperActivity.this.findViewById(R.id.colsePaperDoorBtn)).setVisibility(View.GONE);
            ((Button) PutPaperActivity.this.findViewById(R.id.hasPutColsePaperDoorBtn)).setVisibility(View.VISIBLE);
            ((TextView) PutPaperActivity.this.findViewById(R.id.touzhi_text)).setText(R.string.door_cannot_close);
            TimeoutTask.getTimeoutTask().setEnabled(PutPaperActivity.this.timeoutActionForPutPaper, false);
            TimeoutTask.getTimeoutTask().setEnabled(PutPaperActivity.this.timeoutActionForPaperWeigh, false);
            TimeoutTask.getTimeoutTask().reset(PutPaperActivity.this.timeoutActionForSecondCloseDoor);
            TimeoutTask.getTimeoutTask().setEnabled(PutPaperActivity.this.timeoutActionForSecondCloseDoor, true);
        }
    };

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        checkPaperDoorStatus();
        this.imageView1 = (MyGifView) findViewById(R.id.touzhi_imageview);
        this.imageView1.updateResource(R.drawable.throwpaper, null);
        int timeoutOnPutPaper = Integer.valueOf(SysConfig.get("RVM.TIMEOUT.PUTPAPER")).intValue();
        int timeoutOnSecondCloseDoor = Integer.valueOf(SysConfig.get("RVM.TIMEOUT.SECOND.CLOSE.DOOR")).intValue();
        int timeoutOnPaperWeigh = Integer.valueOf(SysConfig.get("RVM.TIMEOUT.PAPERWEIGH")).intValue();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForPutPaper, timeoutOnPutPaper, false);
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForSecondCloseDoor, timeoutOnSecondCloseDoor, false);
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForPaperWeigh, timeoutOnPaperWeigh, false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutActionForPutPaper);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForPutPaper, true);
    }

    protected void onStop() {
        super.onStop();
        if (this.imageView1 != null) {
            this.imageView1.setBackgroundDrawable(null);
            this.imageView1 = null;
        }
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForPutPaper, false);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForPaperWeigh, false);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForSecondCloseDoor, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutActionForPutPaper);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutActionForPaperWeigh);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutActionForSecondCloseDoor);
    }

    private void stopPutPaper() {
        try {
            this.guiCommonService.execute("GUIRecycleCommonService", "recycleEnd", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForPutPaper, false);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForSecondCloseDoor, false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutActionForPaperWeigh);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForPaperWeigh, true);
    }

    private void forceStopPutPaper() {
        try {
            this.guiCommonService.execute("GUIRecycleCommonService", "forceStopPutPaper", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        onStart();
    }

    private void stopPutPaperPhase() {
        try {
            if (!this.isClick) {
                this.isClick = true;
                this.guiCommonService.execute("GUIRecycleCommonService", "informEntityToThanks", null);
                HashMap<String, Object> hsmpResultForVendingWay = this.guiCommonService.execute("GUIRecycleCommonService", "queryVendingWay", null);
                if (hsmpResultForVendingWay != null) {
                    this.VENDING_WAY = (String) hsmpResultForVendingWay.get(AllAdvertisement.VENDING_WAY);
                }
                HashMap<String, Object> hsmpResult = this.guiCommonService.execute("GUIQueryCommonService", "hasRecycled", null);
                boolean hasPut = false;
                if (hsmpResult != null && "TRUE".equalsIgnoreCase((String) hsmpResult.get("RESULT"))) {
                    hasPut = true;
                }
                if (hasPut) {
                    gotoNextStep();
                    return;
                }
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.THROWBOTTLES_THROWPAPER_REBATE);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        finish();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        } catch (Exception e22) {
            e22.printStackTrace();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_put_paper);
        backgroundToActivity();
        Button btnColsePaperDoor = (Button) findViewById(R.id.colsePaperDoorBtn);
        Button btnhasPutColsePaperDoor = (Button) findViewById(R.id.hasPutColsePaperDoorBtn);
        ((Button) findViewById(R.id.put_paper_close)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PutPaperActivity.this.stopPutPaper();
                ((TextView) PutPaperActivity.this.findViewById(R.id.touzhi_text)).setText(R.string.paperDoorTryClose);
                ((Button) PutPaperActivity.this.findViewById(R.id.put_paper_close)).setBackgroundResource(R.drawable.jieshu_btn_hui);
                if (PutPaperActivity.this.imageView1 != null) {
                    PutPaperActivity.this.imageView1.updateResource(R.drawable.weigh, null);
                }
            }
        });
        btnColsePaperDoor.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    PutPaperActivity.this.forceStopPutPaper();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btnhasPutColsePaperDoor.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PutPaperActivity.this.stopPutPaper();
                ((TextView) PutPaperActivity.this.findViewById(R.id.touzhi_text)).setText(R.string.paperDoorTryClose);
                ((Button) PutPaperActivity.this.findViewById(R.id.put_paper_close)).setVisibility(View.GONE);
                ((Button) PutPaperActivity.this.findViewById(R.id.hasPutColsePaperDoorBtn)).setVisibility(View.GONE);
                if (PutPaperActivity.this.imageView1 != null) {
                    PutPaperActivity.this.imageView1.updateResource(R.drawable.weigh, null);
                }
            }
        });
    }

    public void updateLanguage() {
    }

    private void gotoNextStep() {
        executeGUIAction(false, new GUIActionGotoServiceProcess(), new Object[]{getBaseActivity(), Integer.valueOf(3), this.VENDING_WAY});
        TimeoutTask.getTimeoutTask().addTimeoutAction(new TimeoutAction() {
            public void apply(int forwardSeconds, int remainedSeconds) {
                if (remainedSeconds == 0) {
                    TimeoutTask.getTimeoutTask().removeTimeoutAction(this);
                    HashMap hsmpGUIEvent = new HashMap();
                    hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                    hsmpGUIEvent.put("EVENT", "CMD");
                    hsmpGUIEvent.put("CMD", "timeoutEnd");
                    hsmpGUIEvent.put("BaseActivity", PutPaperActivity.this);
                    GUIGlobal.getEventMgr().addEvent(hsmpGUIEvent);
                }
            }
        }, 5, true);
    }

    private void getNextActivity(HashMap hsmpEvent) {
        this.classs = ThankPaperPageActivity.class;
        String CMD = null;
        String JSON = null;
        if (!(hsmpEvent == null || hsmpEvent.isEmpty())) {
            CMD = (String) hsmpEvent.get("CMD");
            JSON = (String) hsmpEvent.get("JSON");
        }
        if ("BDJ".equalsIgnoreCase(this.VENDING_WAY)) {
            if ("recycleEnd".equalsIgnoreCase(CMD)) {
                this.classs = BdjResultActivity.class;
            } else {
                this.classs = Concern_bdj.class;
            }
        }
        if ("CARD".equalsIgnoreCase(this.VENDING_WAY)) {
            this.classs = GreenCardResultActivity.class;
        }
        if ("COUPON".equalsIgnoreCase(this.VENDING_WAY)) {
            this.classs = PrintingVoucherActivity.class;
        }
        if ("DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
            this.classs = ThankPaperPageActivity.class;
        }
        if ("PHONE".equalsIgnoreCase(this.VENDING_WAY)) {
            this.classs = PhoneRechargeResultActivity.class;
        }
        if ("QRCODE".equalsIgnoreCase(this.VENDING_WAY)) {
            this.classs = QRCodeResultActivity.class;
        }
        if ("WECHAT".equalsIgnoreCase(this.VENDING_WAY)) {
            if ("recycleEnd".equalsIgnoreCase(CMD)) {
                this.classs = WechatResultActivity.class;
            } else {
                this.classs = WechatShowActivity.class;
            }
        }
        Intent intent = new Intent(this, this.classs);
        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("JSON", JSON);
        startActivity(intent);
        finish();
    }

    public void doEvent(HashMap hsmpEvent) {
        String EVENT = (String) hsmpEvent.get("EVENT");
        if ("INFORM".equalsIgnoreCase(EVENT)) {
            String INFORM = (String) hsmpEvent.get("INFORM");
            if ("FORCE_RECYCLE".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().reset(this.timeoutActionForPutPaper);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForPutPaper, true);
                if (this.imageView1 != null) {
                    this.imageView1.updateResource(R.drawable.throwpaper, null);
                }
                ((TextView) findViewById(R.id.touzhi_text)).setText(R.string.ForceRecycle);
            }
            if ("REACH_MAX_PAPER".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForPutPaper, false);
                ((TextView) findViewById(R.id.touzhi_text)).setText(R.string.ReachMaxOfStorage);
                executeGUIAction(3, new GUIAction() {
                    protected void doAction(Object[] paramObjs) {
                        PutPaperActivity.this.stopPutPaper();
                    }
                }, null);
            }
            if ("REACH_MAX_PAPER_PER_OPT".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForPutPaper, false);
                ((TextView) findViewById(R.id.touzhi_text)).setText(R.string.ReachMaxPerOpt);
                executeGUIAction(3, new GUIAction() {
                    protected void doAction(Object[] paramObjs) {
                        PutPaperActivity.this.stopPutPaper();
                    }
                }, null);
            }
            if ("PAPER_RECYCLE_FINISH_DOOR_CLOSE".equalsIgnoreCase(INFORM)) {
                ((TextView) findViewById(R.id.touzhi_text)).setText(R.string.door_succeed_close);
                if (this.imageView1 != null) {
                    this.imageView1.updateResource(R.drawable.weigh, null);
                }
            }
            if ("PAPER_WEIGH_RESULT".equalsIgnoreCase(INFORM)) {
                stopPutPaperPhase();
            }
        }
        if ("CMD".equalsIgnoreCase(EVENT)) {
            String CMD = (String) hsmpEvent.get("CMD");
            if ("REQUEST_RECYCLE".equalsIgnoreCase(CMD)) {
            }
            if (ServiceName.TAKE_PHOTO.equalsIgnoreCase(CMD)) {
                executeGUIAction(false, new GUIActionTakePhoto(), new Object[]{this});
            }
            if ("recycleEnd".equalsIgnoreCase(CMD)) {
                if (((BaseActivity) hsmpEvent.get("BaseActivity")) == this && !this.isFinishCalled) {
                    this.isFinishCalled = true;
                    getNextActivity(hsmpEvent);
                } else {
                    return;
                }
            }
            if ("timeoutEnd".equalsIgnoreCase(CMD) && ((BaseActivity) hsmpEvent.get("BaseActivity")) == this && !this.isFinishCalled) {
                this.isFinishCalled = true;
                getNextActivity(hsmpEvent);
            }
        }
    }

    private void checkPaperDoorStatus() {
        HashMap<String, Object> hsmpPaperDoorStatus = new HashMap();
        try {
            hsmpPaperDoorStatus = this.guiCommonService.execute("GUIRecycleCommonService", "queryPaperDoorStatus", null);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (DoorStatus.PAPER_DOOR_CLOSE.equalsIgnoreCase((String) hsmpPaperDoorStatus.get("PaperDoorStatus"))) {
            try {
                HashMap<String, Object> hsmpResource = new HashMap();
                hsmpResource.put("SHOW_WEIGHT_FORMAT", getString(R.string.showWeightFormat));
                HashMap<String, Object> hsmpParam = new HashMap();
                hsmpParam.put("RESOURCE", hsmpResource);
                this.guiCommonService.execute("GUIRecycleCommonService", "initResource", hsmpParam);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ((Button) findViewById(R.id.colsePaperDoorBtn)).setVisibility(View.GONE);
            ((Button) findViewById(R.id.put_paper_close)).setVisibility(View.VISIBLE);
            ((Button) findViewById(R.id.hasPutColsePaperDoorBtn)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.touzhi_text)).setText(R.string.putPaperHint);
            executeGUIAction(false, new GUIActionRecycleStart(), null);
            return;
        }
        ((Button) findViewById(R.id.colsePaperDoorBtn)).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.put_paper_close)).setVisibility(View.GONE);
        ((Button) findViewById(R.id.hasPutColsePaperDoorBtn)).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.touzhi_text)).setText(R.string.paperDoorOpenRemind);
    }
}
