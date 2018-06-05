package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.content.Intent;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.common.SysDef.ServiceName;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.BroadcastTaskCommonService;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.gui.action.GUIActionRecycleStart;
import com.incomrecycle.prms.rvm.gui.action.GUIActionTakePhoto;
import com.incomrecycle.prms.rvm.gui.activity.view.MyGifView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class PutBottleYC103Activity extends BaseActivity implements OnLoadCompleteListener {
    private String CMD = null;
    private HashMap<String, String> HOMEPAGE_LEFT = new HashMap();
    private String PUTBOTTLE_PIC;
    private boolean PUTBOTTLE_PIC_FLAG = true;
    private HashMap<String, Object> TRANSMIT_ADV = new HashMap();
    private String VENDING_WAY = null;
    private MyGifView imageView1 = null;
    private boolean isClick = false;
    private boolean isPlaySounds;
    private List<HashMap<String, String>> putBottlesNoEvent = new ArrayList();
    private MyGifView rightImageView = null;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue() - 1;
                    if (remainedSeconds == -1) {
                        PutBottleYC103Activity.this.stopPutBottlePhase();
                    } else {
                        ((TextView) PutBottleYC103Activity.this.findViewById(R.id.touping_time)).setText("" + remainedSeconds);
                    }
                }
            };
            PutBottleYC103Activity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TimeoutAction timeoutActionForBottleScan = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue() - 1;
                    if (remainedSeconds == -1) {
                        PutBottleYC103Activity.this.stopPutBottlePhase();
                    } else {
                        ((TextView) PutBottleYC103Activity.this.findViewById(R.id.touping_time)).setText("" + remainedSeconds);
                    }
                }
            };
            PutBottleYC103Activity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private LinearLayout toupingLayout = null;

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        executeGUIAction(true, new GUIActionTakePhoto(), new Object[]{this});
        String adconfURL = (String) GUIGlobal.getCurrentSession("AD_CONF_URL");
        this.TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
        this.rightImageView = (MyGifView) findViewById(R.id.rightImageView);
        if (this.TRANSMIT_ADV != null) {
            this.HOMEPAGE_LEFT = (HashMap) this.TRANSMIT_ADV.get("TRANSMIT_ADV");
            if (this.HOMEPAGE_LEFT != null) {
                if (this.HOMEPAGE_LEFT.keySet().contains(AllAdvertisement.PUTBOTTLE_PIC)) {
                    this.PUTBOTTLE_PIC = (String) this.HOMEPAGE_LEFT.get(AllAdvertisement.PUTBOTTLE_PIC);
                    if (this.PUTBOTTLE_PIC != null) {
                        this.PUTBOTTLE_PIC_FLAG = false;
                    } else {
                        this.putBottlesNoEvent = BroadcastTaskCommonService.getPutBottlePicList(adconfURL);
                        this.PUTBOTTLE_PIC_FLAG = true;
                    }
                }
                this.VENDING_WAY = (String) this.HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_WAY);
            }
        }
        if (this.PUTBOTTLE_PIC_FLAG || StringUtils.isBlank(this.PUTBOTTLE_PIC) || !new File(this.PUTBOTTLE_PIC).isFile()) {
            this.putBottlesNoEvent = BroadcastTaskCommonService.getPutBottlePicList(adconfURL);
            if (this.putBottlesNoEvent != null) {
                if (this.putBottlesNoEvent.size() > 0) {
                    String path = (String) ((HashMap) this.putBottlesNoEvent.get((int) (Math.random() * ((double) this.putBottlesNoEvent.size())))).get(AllAdvertisement.PUTBOTTLE_PIC);
                    if (!StringUtils.isBlank(path) && new File(path).isFile()) {
                        this.rightImageView.setVisibility(View.VISIBLE);
                        this.rightImageView.updateResource(-1, path);
                    }
                } else {
                    this.rightImageView.setVisibility(View.GONE);
                }
            }
        } else {
            this.rightImageView.setVisibility(View.VISIBLE);
            this.rightImageView.updateResource(-1, this.PUTBOTTLE_PIC);
        }
        this.CMD = null;
        SysGlobal.execute(new Runnable() {
            public void run() {
                PutBottleYC103Activity.this.executeGUIAction(false, new GUIActionRecycleStart(), null);
            }
        });
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
            this.soundPool.setOnLoadCompleteListener(this);
        }
        if (this.isPlaySounds && this.soundPool != null) {
            this.soundPool.load(this, R.raw.touping, 0);
        }
        showRemindInfo();
        int timeoutOnPutBottle = Integer.valueOf(SysConfig.get("RVM.TIMEOUT.PUTBOTTLE")).intValue();
        int timeoutOnBottleScan = Integer.valueOf(SysConfig.get("RVM.TIMEOUT.BOTTLESCAN")).intValue();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, timeoutOnPutBottle, false);
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForBottleScan, timeoutOnBottleScan, false);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
        this.toupingLayout.setBackgroundResource(R.drawable.blue_bg);
    }

    protected void onStop() {
        super.onStop();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutActionForBottleScan);
        if (this.imageView1 != null) {
            this.imageView1.setBackgroundDrawable(null);
        }
        if (this.rightImageView != null) {
            this.rightImageView.updateResource(-1, null);
            this.rightImageView = null;
        }
        if (this.soundPool != null) {
            try {
                this.soundPool.release();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            this.soundPool = null;
        }
        this.toupingLayout.setBackgroundResource(0);
    }

    public void finish() {
        super.finish();
    }

    private void stopPutBottlePhase() {
        if (!this.isClick) {
            this.isClick = true;
            boolean isEnableRecycleForServiceDisable = false;
            final GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
            int count = 0;
            try {
                SysGlobal.execute(new Runnable() {
                    public void run() {
                        if ("MAX".equalsIgnoreCase(PutBottleYC103Activity.this.CMD)) {
                            try {
                                if ("TRUE".equalsIgnoreCase(SysConfig.get("COM.PLC.HAS.DOOR"))) {
                                    guiCommonService.execute("GUIRecycleCommonService", "closeBottleDoor", null);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            guiCommonService.execute("GUIRecycleCommonService", "recycleEnd", null);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                });
                HashMap<String, Object> hsmpResult = guiCommonService.execute("GUIQueryCommonService", "hasRecycled", null);
                boolean hasPut = false;
                if (hsmpResult != null && "TRUE".equalsIgnoreCase((String) hsmpResult.get("RESULT"))) {
                    hasPut = true;
                }
                if (!hasPut) {
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.THROWBOTTLES_NULL_RETURN);
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
                } else if (StringUtils.isBlank(this.VENDING_WAY)) {
                    int i;
                    String str = SysConfig.get("VENDING.WAY");
                    String vendingWaySet = SysConfig.get("VENDING.WAY.SET");
                    List vendingWaySetList = null;
                    if (!StringUtils.isBlank(vendingWaySet)) {
                        vendingWaySetList = Arrays.asList(vendingWaySet.split(";"));
                    }
                    List<String> listDisabledService = new ArrayList();
                    HashMap<String, Object> hsmpResultServiceDisable = guiCommonService.execute("GUIQueryCommonService", "queryServiceDisable", null);
                    if (hsmpResultServiceDisable != null) {
                        String ServiceDisabled = (String) hsmpResultServiceDisable.get("SERVICE_DISABLED");
                        if (!StringUtils.isBlank(ServiceDisabled) && ServiceDisabled.length() > 0) {
                            String[] strDisabledService = ServiceDisabled.split(",");
                            for (String add : strDisabledService) {
                                listDisabledService.add(add);
                            }
                        }
                    }
                    if (!StringUtils.isBlank(str)) {
                        String[] strSet = str.split(";");
                        String vendingWay = null;
                        i = 0;
                        while (i < strSet.length) {
                            if (!listDisabledService.contains(strSet[i]) && vendingWaySetList.contains(strSet[i])) {
                                isEnableRecycleForServiceDisable = true;
                                count++;
                                vendingWay = strSet[i];
                            }
                            i++;
                        }
                        if (isEnableRecycleForServiceDisable) {
                            if (count > 1) {
                                startActivity(new Intent(this, SelectRecycleActivity.class));
                                finish();
                            }
                            if (count == 1 && !StringUtils.isBlank(vendingWay)) {
                                executeGUIAction(true, new GUIActionGotoServiceProcess(), (Object[]) new Object[]{getBaseActivity(), Integer.valueOf(1), vendingWay});
                                finish();
                            }
                        }
                    }
                } else {
                    executeGUIAction(true, new GUIActionGotoServiceProcess(), (Object[]) new Object[]{getBaseActivity(), Integer.valueOf(1), this.VENDING_WAY});
                    finish();
                }
            } catch (Exception e22) {
                e22.printStackTrace();
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_put_103);
        backgroundToActivity();
        this.imageView1 = (MyGifView) findViewById(R.id.ImageView);
        this.imageView1.updateResource(R.drawable.throwbottle, null);
        ((Button) findViewById(R.id.touping_end_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.THROWBOTTLES_NULL_RETURN);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                PutBottleYC103Activity.this.stopPutBottlePhase();
            }
        });
        this.toupingLayout = (LinearLayout) findViewById(R.id.touping_layout);
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        String EVENT = (String) hsmpEvent.get("EVENT");
        if ("INFORM".equalsIgnoreCase(EVENT)) {
            Button btnClose;
            String INFORM = (String) hsmpEvent.get("INFORM");
            if ("BOTTLE_READY".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
                TimeoutTask.getTimeoutTask().reset(this.timeoutActionForBottleScan);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, true);
                if (this.imageView1 != null) {
                    this.imageView1.updateResource(R.drawable.scanbarcode, null);
                }
                ((TextView) findViewById(R.id.touping_text)).setText(Html.fromHtml(getString(R.string.hintScanBarcode)));
                ((TextView) findViewById(R.id.touping_remind_text)).setText(Html.fromHtml(getString(R.string.puthint)));
                btnClose = (Button) findViewById(R.id.touping_end_btn);
                if ("DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
                    btnClose.setBackgroundResource(R.drawable.jieshu_btn_hui);
                    btnClose.setText(getString(R.string.end));
                    btnClose.setEnabled(false);
                } else {
                    btnClose.setBackgroundResource(R.drawable.chongzhi_btn_hui);
                    btnClose.setText(getString(R.string.pre_paid));
                    btnClose.setEnabled(false);
                }
            }
            if ("BOTTLE_SCAN_END".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, true);
                if (this.imageView1 != null) {
                    this.imageView1.updateResource(R.drawable.throwbottle, null);
                }
                ((TextView) findViewById(R.id.touping_text)).setText(Html.fromHtml(getString(R.string.finishScanBarcode)));
                ((TextView) findViewById(R.id.touping_remind_text)).setText(Html.fromHtml(getString(R.string.puthint)));
                btnClose = (Button) findViewById(R.id.touping_end_btn);
                if ("DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
                    btnClose.setBackgroundResource(R.drawable.jieshu_btn);
                    btnClose.setText(getString(R.string.end));
                    btnClose.setEnabled(false);
                } else {
                    btnClose.setBackgroundResource(R.drawable.chongzhi_btn_hui);
                    btnClose.setText(getString(R.string.pre_paid));
                    btnClose.setEnabled(false);
                }
            }
            if ("BAR_CODE_NOT_FOUND".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, false);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
                if (this.imageView1 != null) {
                    this.imageView1.updateResource(R.drawable.throwbottle, null);
                }
                if (this.isPlaySounds && this.soundPool != null) {
                    this.soundPool.load(this, R.raw.tiaomaweishaodao, 0);
                }
                ((TextView) findViewById(R.id.touping_text)).setText(Html.fromHtml(getString(R.string.NoScanBarcodePer) + "<font color=\"#ff0000\">" + getString(R.string.NoScanBarcode) + "</font>"));
                ((TextView) findViewById(R.id.touping_remind_text)).setText("");
                btnClose = (Button) findViewById(R.id.touping_end_btn);
                btnClose.setBackgroundResource(R.drawable.jieshu_btn);
                btnClose.setText(R.string.end);
                btnClose.setEnabled(true);
                compareShutdownTime();
            }
            if ("BAR_CODE_REJECT".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, false);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
                if (this.imageView1 != null) {
                    this.imageView1.updateResource(R.drawable.throwbottle, null);
                }
                if (this.isPlaySounds && this.soundPool != null) {
                    this.soundPool.load(this, R.raw.tiaomabushibie, 0);
                }
                ((TextView) findViewById(R.id.touping_text)).setText(Html.fromHtml(getString(R.string.RecognizedScanBarcode)));
                ((TextView) findViewById(R.id.touping_remind_text)).setText("");
                btnClose = (Button) findViewById(R.id.touping_end_btn);
                btnClose.setBackgroundResource(R.drawable.jieshu_btn);
                btnClose.setText(getString(R.string.end));
                btnClose.setEnabled(true);
            }
            if ("BAR_CODE_REJECT_ACCEPT".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, false);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
                if (this.imageView1 != null) {
                    this.imageView1.updateResource(R.drawable.throwbottle, null);
                }
                ((TextView) findViewById(R.id.touping_text)).setText(Html.fromHtml(getString(R.string.barcode_reject_accept)));
                ((TextView) findViewById(R.id.touping_remind_text)).setText("");
                btnClose = (Button) findViewById(R.id.touping_end_btn);
                btnClose.setBackgroundResource(R.drawable.jieshu_btn);
                btnClose.setText(getString(R.string.end));
                btnClose.setEnabled(true);
            }
            if ("BOTTLE_ACCEPT_READY".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, true);
                ((TextView) findViewById(R.id.touping_text)).setText(Html.fromHtml(getString(R.string.bottleSureAccept)));
                ((TextView) findViewById(R.id.touping_remind_text)).setText(Html.fromHtml(getString(R.string.puthint)));
                btnClose = (Button) findViewById(R.id.touping_end_btn);
                if ("DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
                    btnClose.setBackgroundResource(R.drawable.jieshu_btn);
                    btnClose.setText(getString(R.string.end));
                    btnClose.setEnabled(false);
                } else {
                    btnClose.setBackgroundResource(R.drawable.chongzhi_btn_hui);
                    btnClose.setText(getString(R.string.pre_paid));
                    btnClose.setEnabled(false);
                }
            }
            if ("FORCE_RECYCLE".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
                TimeoutTask.getTimeoutTask().reset(this.timeoutActionForBottleScan);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, true);
                if (this.imageView1 != null) {
                    this.imageView1.updateResource(R.drawable.scanbarcode, null);
                }
                ((TextView) findViewById(R.id.touping_text)).setText(Html.fromHtml(getString(R.string.ForceRecycle)));
                ((TextView) findViewById(R.id.touping_remind_text)).setText("");
                btnClose = (Button) findViewById(R.id.touping_end_btn);
                if ("DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
                    btnClose.setBackgroundResource(R.drawable.jieshu_btn);
                    btnClose.setText(getString(R.string.end));
                    btnClose.setEnabled(true);
                } else {
                    btnClose.setBackgroundResource(R.drawable.chongzhi_btn);
                    btnClose.setText(getString(R.string.pre_paid));
                    btnClose.setEnabled(true);
                }
                compareShutdownTime();
            }
            if ("REACH_MAX_BOTTLES".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, false);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
                setPutBottleFullWarn();
                ((TextView) findViewById(R.id.touping_remind_text)).setText(R.string.ReachMaxOfStorage);
                ((TextView) findViewById(R.id.right_touping_text)).setVisibility(8);
                btnClose = (Button) findViewById(R.id.touping_end_btn);
                if ("DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
                    btnClose.setBackgroundResource(R.drawable.jieshu_btn);
                    btnClose.setText(getString(R.string.end));
                    btnClose.setEnabled(true);
                } else {
                    btnClose.setBackgroundResource(R.drawable.chongzhi_btn);
                    btnClose.setText(getString(R.string.pre_paid));
                    btnClose.setEnabled(true);
                }
            }
            if ("REACH_MAX_BOTTLES_PER_OPT".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, false);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
                setPutBottleFullWarn();
                ((TextView) findViewById(R.id.touping_remind_text)).setText(R.string.ReachMaxPerOpt);
                ((TextView) findViewById(R.id.right_touping_text)).setVisibility(8);
                btnClose = (Button) findViewById(R.id.touping_end_btn);
                if ("DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
                    btnClose.setBackgroundResource(R.drawable.jieshu_btn);
                    btnClose.setText(getString(R.string.end));
                    btnClose.setEnabled(true);
                } else {
                    btnClose.setBackgroundResource(R.drawable.chongzhi_btn);
                    btnClose.setText(getString(R.string.pre_paid));
                    btnClose.setEnabled(true);
                }
            }
            if ("BOTTLE_REJECT_FINISH".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, false);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
                if (this.imageView1 != null) {
                    this.imageView1.updateResource(R.drawable.throwbottle, null);
                }
                ((TextView) findViewById(R.id.touping_text)).setText(getString(R.string.RejectedFinish));
                ((TextView) findViewById(R.id.touping_remind_text)).setText("");
                btnClose = (Button) findViewById(R.id.touping_end_btn);
                if ("DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
                    btnClose.setBackgroundResource(R.drawable.jieshu_btn);
                    btnClose.setText(getString(R.string.end));
                    btnClose.setEnabled(true);
                } else {
                    btnClose.setBackgroundResource(R.drawable.chongzhi_btn);
                    btnClose.setText(getString(R.string.pre_paid));
                    btnClose.setEnabled(true);
                }
                compareShutdownTime();
            }
            if ("BOTTLE_ACCEPT_FINISH".equalsIgnoreCase(INFORM) || "RESET".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, false);
                TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
                if (this.imageView1 != null) {
                    this.imageView1.updateResource(R.drawable.throwbottle, null);
                }
                if (this.isPlaySounds && this.soundPool != null) {
                    this.soundPool.load(this, R.raw.kacha, 0);
                }
                TextView textView = (TextView) findViewById(R.id.touping_text);
                TextView noPut = (TextView) findViewById(R.id.touping_remind_text);
                noPut.setText("");
                btnClose = (Button) findViewById(R.id.touping_end_btn);
                btnClose.setEnabled(true);
                int bottleNumber = 0;
                try {
                    HashMap<String, Object> hsmpResult = guiCommonService.execute("GUIQueryCommonService", "recycledBottleSummary", null);
                    if (hsmpResult != null) {
                        List<HashMap<String, String>> listHsmpRecycleBottle = (List) hsmpResult.get("RECYCLED_BOTTLE_SUMMARY");
                        if (listHsmpRecycleBottle != null && listHsmpRecycleBottle.size() > 0) {
                            for (int i = 0; i < listHsmpRecycleBottle.size(); i++) {
                                bottleNumber += Integer.parseInt((String) ((HashMap) listHsmpRecycleBottle.get(i)).get("BOTTLE_COUNT"));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                noPut.setText(Html.fromHtml(StringUtils.replace(StringUtils.replace(getString(R.string.AcceptedFinish), "$PUT_BOTTLE_NUM$", "<H1>" + bottleNumber + "</H1>"), "$REMAIN_BOTTLE_NUM$", "<H1>" + (Integer.parseInt(SysConfig.get("MAX.BOTTLES.PER.OPT")) - bottleNumber) + "</H1>")));
                if (bottleNumber <= 0) {
                    btnClose.setBackgroundResource(R.drawable.jieshu_btn);
                    btnClose.setText(getString(R.string.end));
                } else if ("DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
                    btnClose.setBackgroundResource(R.drawable.jieshu_btn);
                    btnClose.setText(getString(R.string.end));
                } else {
                    btnClose.setBackgroundResource(R.drawable.chongzhi_btn);
                    btnClose.setText(getString(R.string.pre_paid));
                }
                textView.setText(R.string.Bar_code_facing_up);
                compareShutdownTime();
            }
            if ("BOTTLE_STATE_EXCEPTION".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, false);
                ((TextView) findViewById(R.id.touping_text)).setText(R.string.bottle_state_excepiton);
                this.CMD = "MAX";
                executeGUIAction(1, new GUIAction() {
                    protected void doAction(Object[] paramObjs) {
                        PutBottleYC103Activity.this.stopPutBottlePhase();
                    }
                }, null);
            }
            if ("OVER_WEIGH".equalsIgnoreCase(INFORM)) {
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
                TimeoutTask.getTimeoutTask().reset(this.timeoutActionForBottleScan);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForBottleScan, true);
                if (this.imageView1 != null) {
                    this.imageView1.updateResource(R.drawable.throwbottle, null);
                }
                if (this.isPlaySounds && this.soundPool != null) {
                    this.soundPool.load(this, R.raw.chaozhong, 0);
                }
                ((TextView) findViewById(R.id.touping_text)).setText(getString(R.string.bottleOverWeight));
                ((TextView) findViewById(R.id.touping_remind_text)).setText("");
                btnClose = (Button) findViewById(R.id.touping_end_btn);
                if ("DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
                    btnClose.setBackgroundResource(R.drawable.jieshu_btn);
                    btnClose.setText(getString(R.string.end));
                    btnClose.setEnabled(true);
                } else {
                    btnClose.setBackgroundResource(R.drawable.chongzhi_btn);
                    btnClose.setText(getString(R.string.pre_paid));
                    btnClose.setEnabled(true);
                }
                compareShutdownTime();
            }
        }
        if ("CMD".equalsIgnoreCase(EVENT)) {
            if (ServiceName.TAKE_PHOTO.equalsIgnoreCase((String) hsmpEvent.get("CMD"))) {
                executeGUIAction(false, new GUIActionTakePhoto(), (Object[]) new Object[]{this});
            }
        }
    }

    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        soundPlay(sampleId);
    }

    private void soundPlay(int soundId) {
        if (this.soundPool != null) {
            this.soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    private boolean compareShutdownTime() {
        boolean isInMaintainingTime = false;
        try {
            String powerOnTime = SysConfig.get("RVM.POWER.ON.TIME");
            String powerOffTime = SysConfig.get("RVM.POWER.OFF.TIME");
            if (!(StringUtils.isBlank(powerOnTime) || StringUtils.isBlank(powerOffTime) || powerOnTime.equals(powerOffTime))) {
                String[] powerOnTimes = powerOnTime.split(":");
                String[] powerOffTimes = powerOffTime.split(":");
                int enableTime = (Integer.parseInt(powerOnTimes[0]) * 60) + Integer.parseInt(powerOnTimes[1]);
                int disableTime = ((Integer.parseInt(powerOffTimes[0]) * 60) + Integer.parseInt(powerOffTimes[1])) - 5;
                if (disableTime < 0) {
                    disableTime += 1440;
                }
                String[] nowTimes = DateUtils.formatDatetime(new Date(), "HH:mm").split(":");
                int nowTime = (Integer.parseInt(nowTimes[0]) * 60) + Integer.parseInt(nowTimes[1]);
                if (enableTime > disableTime && nowTime >= disableTime && nowTime <= enableTime) {
                    isInMaintainingTime = true;
                }
                if (enableTime < disableTime && (nowTime >= disableTime || nowTime <= enableTime)) {
                    isInMaintainingTime = true;
                }
            }
            if (isInMaintainingTime) {
                ((TextView) findViewById(R.id.touping_text)).setText(getString(R.string.Power_off_before));
                executeGUIAction(3, new GUIAction() {
                    protected void doAction(Object[] paramObjs) {
                        PutBottleYC103Activity.this.stopPutBottlePhase();
                    }
                }, null);
            }
        } catch (Exception e) {
        }
        return true;
    }

    private void setPutBottleFullWarn() {
        if (this.imageView1 != null) {
            this.imageView1.setVisibility(View.GONE);
        }
        ((TextView) findViewById(R.id.put_bottle_full_text)).setVisibility(View.VISIBLE);
        MyGifView warnGifView = (MyGifView) findViewById(R.id.rightImageView);
        warnGifView.updateResource(R.drawable.putbottle_full_warn, null);
        warnGifView.setVisibility(View.VISIBLE);
    }

    private void showRemindInfo() {
        if (!"FALSE".equalsIgnoreCase(SysConfig.get("BOTTLES_LIMITED_ENABLE")) && !"FALSE".equalsIgnoreCase(SysConfig.get("BOTTLES.LIMITED.LOCALSWITCHABLE"))) {
            TextView remindTextView = (TextView) findViewById(R.id.right_touping_text);
            remindTextView.setText(StringUtils.replace(getString(R.string.put_bottle_too_mutch_warn), "$MAX_REBATE_BOTTLES_NUM$", SysConfig.get("BOTTLES.DAILY.ALARM")));
            remindTextView.setVisibility(View.VISIBLE);
        }
    }
}
