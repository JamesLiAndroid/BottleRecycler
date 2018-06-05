package com.incomrecycle.prms.rvm;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.sync.ClickTimeManager;
import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.common.task.TickTaskThread;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.ShellUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.RVMShell;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.common.SysDef.MediaInfo;
import com.incomrecycle.prms.rvm.common.SysDef.ServiceName;
import com.incomrecycle.prms.rvm.common.SysDef.updateDetection;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.BroadcastTaskCommonService;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.gui.action.GUIActionInit;
import com.incomrecycle.prms.rvm.gui.action.GUIActionSysAction;
import com.incomrecycle.prms.rvm.gui.action.GUIActionTakePhoto;
import com.incomrecycle.prms.rvm.gui.activity.adapter.MianActivityIconAdapter;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelActivity;
import com.incomrecycle.prms.rvm.gui.activity.convenienceservices.QueryOneCardActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.FaultListActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.SelectRecycleActivity;
import com.incomrecycle.prms.rvm.gui.activity.view.MyGifView;
import com.incomrecycle.prms.rvm.gui.activity.view.MyVideoView;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class RVMMainADActivity extends BaseActivity {
    private static final int NEXT_STEP_MSG = 2;
    private static final int PICTURE_MSG = 3;
    private static final int VIDEO_MSG = 1;
    public static int indexOfListMedia = 0;
    private List<HashMap<String, String>> HOMEPAGE_LEFT_BOTTOM_LIST = new ArrayList();
    private LinearLayout adLayout = null;
    private MianActivityIconAdapter adapter = null;
    private String adconfURL = null;
    private List<HashMap<String, String>> beforeList;
    private MyGifView btnService = null;
    private MyGifView btnThrowBottles = null;
    private MyGifView btnThrowPaper = null;
    private MyGifView contentView = null;
    private boolean isClick = false;
    private boolean isCorrectYear = true;
    private boolean isMaintaining = false;
    private boolean isMaxBottle;
    private boolean isMaxPaper;
    private boolean isOutOfServiceBottle = false;
    private boolean isOutOfServicePaper = false;
    private boolean isStopService = false;
    private String jsonDisableReason = null;
    private boolean mListItemClick = false;
    private ListView mListView = null;
    private Handler mhandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    RVMMainADActivity.this.mListItemClick = false;
                    RVMMainADActivity.this.play();
                    return;
                case 2:
                    RVMMainADActivity.this.gotoNextMedia();
                    return;
                case 3:
                    String picURL = null;
                    if (RVMMainADActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST != null && RVMMainADActivity.indexOfListMedia < RVMMainADActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.size()) {
                        picURL = (String) ((HashMap) RVMMainADActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.get(RVMMainADActivity.indexOfListMedia)).get(AllAdvertisement.MAIN_PICTURE_PATH);
                    }
                    if (!StringUtils.isBlank(picURL)) {
                        try {
                            if (new File(picURL).isFile()) {
                                RVMMainADActivity.this.myVideoView.setVisibility(View.GONE);
                                RVMMainADActivity.this.contentView.setVisibility(View.VISIBLE);
                                RVMMainADActivity.this.contentView.updateResource(-1, picURL);
                            }
                        } catch (Exception e) {
                            RVMMainADActivity.this.gotoNextMedia();
                        }
                    }
                    if (RVMMainADActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST != null && RVMMainADActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.size() > 0) {
                        int picTimeOutTime;
                        try {
                            picTimeOutTime = Integer.valueOf((String) ((HashMap) RVMMainADActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.get(RVMMainADActivity.indexOfListMedia)).get(AllAdvertisement.PLAY_SECONDS)).intValue();
                        } catch (Exception e2) {
                            picTimeOutTime = Integer.valueOf(10).intValue();
                        }
                        if (RVMMainADActivity.this.mListItemClick) {
                            picTimeOutTime = 45;
                        }
                        TimeoutTask.getTimeoutTask().addTimeoutAction(RVMMainADActivity.this.timeoutActionForNextMedia, picTimeOutTime, false);
                        TimeoutTask.getTimeoutTask().reset(RVMMainADActivity.this.timeoutActionForNextMedia, picTimeOutTime);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private MyVideoView myVideoView = null;
    private boolean recycleBottle = false;
    private boolean recyclepaper = false;
    private String[] strs = new String[2];
    Object syncOBJ = new Object();
    private TimeoutAction timeoutActionForNextMedia = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    if (((Integer) paramObjs[1]).intValue() == 0) {
                        RVMMainADActivity.this.mhandler.sendEmptyMessage(2);
                        RVMMainADActivity.this.mListItemClick = false;
                    }
                }
            };
            RVMMainADActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TaskAction timeoutActionForQueryAD = new TaskAction() {
        public void execute() {
            RVMMainADActivity.this.executeGUIAction(false, new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    RVMMainADActivity.this.qyeryAD();
                }
            }, new Object[0]);
        }
    };
    private TaskAction timeoutActionForShutdwon = new TaskAction() {
        public void execute() {
            RVMMainADActivity.this.executeGUIAction(false, new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            RVMMainADActivity.this.compareShutdownTime();
                        }
                    });
                }
            }, new Object[0]);
        }
    };
    private TaskAction timeoutActionForSleepTime = new TaskAction() {
        public void execute() {
            RVMMainADActivity.this.executeGUIAction(false, new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    RVMMainADActivity.this.compareSleepTime();
                }
            }, new Object[0]);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_rvm_main_ad);
        backgroundToActivity();
        initView();
    }

    private void initView() {
        this.adLayout = (LinearLayout) findViewById(R.id.ad_icon_list_layout);
        this.mListView = (ListView) findViewById(R.id.ad_icon_list);
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
                if (ClickTimeManager.clickEnable(String.valueOf(R.id.ad_icon_list), 500)) {
                    RVMMainADActivity.indexOfListMedia = arg2;
                    RVMMainADActivity.this.mListItemClick = true;
                    if (RVMMainADActivity.this.adapter != null) {
                        RVMMainADActivity.this.adapter.notifyDataSetChanged();
                    }
                    TimeoutTask.getTimeoutTask().removeTimeoutAction(RVMMainADActivity.this.timeoutActionForNextMedia);
                    RVMMainADActivity.this.initMediaInfo();
                }
            }
        });
        this.myVideoView = (MyVideoView) findViewById(R.id.firstVideo);
        this.myVideoView.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                RVMMainADActivity.this.myVideoView.start();
            }
        });
        this.myVideoView.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                synchronized (RVMMainADActivity.this.syncOBJ) {
                    RVMMainADActivity.this.gotoNextMedia();
                }
            }
        });
        this.myVideoView.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                try {
                    RVMMainADActivity.this.gotoNextMedia();
                } catch (Exception e) {
                }
                return true;
            }
        });
        this.contentView = (MyGifView) findViewById(R.id.fullscreen_content);
        findViewById(R.id.left_top_layout).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!RVMMainADActivity.this.isClick) {
                    RVMMainADActivity.this.isClick = true;
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.CHANNAL);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    Intent intent = new Intent();
                    intent.setClass(RVMMainADActivity.this.getBaseContext(), ChannelActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    RVMMainADActivity.this.startActivity(intent);
                }
            }
        });
        String str = SysConfig.get("COUNTRIES_REGIONS");
        if (StringUtils.isBlank(str)) {
            this.strs[0] = Locale.getDefault().getLanguage();
            this.strs[1] = Locale.getDefault().getCountry();
        } else {
            String[] strs1 = str.split(",");
            if (strs1.length < 2) {
                this.strs[0] = strs1[0];
                this.strs[1] = "";
            } else {
                this.strs = strs1;
            }
        }
        GUIGlobal.updateLanguage(getApplication(), new Locale(this.strs[0], this.strs[1]));
        this.btnThrowBottles = (MyGifView) findViewById(R.id.reminder);
        this.btnThrowBottles.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!RVMMainADActivity.this.isClick) {
                    RVMMainADActivity.this.isClick = true;
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.THROWBOTTLES);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    try {
                        String jsonDisableReason = RVMMainADActivity.this.jsonDisableReason;
                        if (RVMMainADActivity.this.recycleBottle) {
                            RVMMainADActivity.this.startRecycleBottle();
                            return;
                        }
                        List<String> listDisableReason = JSONUtils.toList(jsonDisableReason);
                        Intent intent = new Intent(RVMMainADActivity.this.getBaseContext(), FaultListActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList("STATE", (ArrayList) listDisableReason);
                        intent.putExtras(bundle);
                        RVMMainADActivity.this.startActivity(intent);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });
        this.btnThrowPaper = (MyGifView) findViewById(R.id.throwPaper);
        this.btnThrowPaper.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!RVMMainADActivity.this.isClick) {
                    RVMMainADActivity.this.isClick = true;
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.THROWPAPER);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    RVMMainADActivity.this.startRecyclePaper();
                }
            }
        });
        this.btnService = (MyGifView) findViewById(R.id.btnservice);
        this.btnService.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!RVMMainADActivity.this.isClick) {
                    RVMMainADActivity.this.isClick = true;
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.CONVENIENCE_SERVICE);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    GUIGlobal.setCurrentSession("USESTATE", Integer.valueOf(2));
                    Intent intent = new Intent();
                    intent.setClass(RVMMainADActivity.this.getBaseContext(), QueryOneCardActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    RVMMainADActivity.this.startActivity(intent);
                }
            }
        });
        String[] productTypes = SysConfig.get("RECYCLE.SERVICE.SET").split(",");
        for (Object equals : productTypes) {
            if ("PAPER".equals(equals)) {
                this.btnThrowPaper.setVisibility(0);
            }
        }
        refreshAD();
    }

    private void refreshAD() {
        this.adconfURL = SysConfig.get("BROADCAST.FILE");
        GUIGlobal.setCurrentSession("AD_CONF_URL", this.adconfURL);
        initMediaInfo();
    }

    public void enableRecycleBottle(List<String> listBottleDisableReason) {
        boolean z;
        if (this.isStopService) {
            listBottleDisableReason.add("STOP_SVERVICE_TIME");
        }
        if (listBottleDisableReason != null && listBottleDisableReason.size() > 0) {
            this.isOutOfServiceBottle = true;
        }
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("PRODUCT_TYPE", "BOTTLE");
        try {
            this.isMaxBottle = ((Boolean) CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryIsStorageMax", hashMap).get("IS_MAX_COUNT")).booleanValue();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (!this.isCorrectYear) {
            listBottleDisableReason.add("YEAR_ERROR");
        }
        boolean isUpdated = false;
        if ("TRUE".equalsIgnoreCase(SysConfig.get("DATA_UPDATE_COMPLETED")) || "FALSE".equalsIgnoreCase(SysConfig.get("DATA_UPDATE_CHECK_ENABLE"))) {
            isUpdated = true;
        }
        if (!isUpdated) {
            listBottleDisableReason.add("UPDATING");
        }
        if (this.isMaintaining) {
            listBottleDisableReason.add("MAINTAIN_TIME");
        }
        if (this.isMaxBottle) {
            listBottleDisableReason.add("MAX_BOTTLE");
        }
        if ("TRUE".equalsIgnoreCase(SysConfig.get("STATE:PLC_LIGHT_ERROR"))) {
            listBottleDisableReason.add("PLC_LIGHT_ERROR");
        }
        if ("TRUE".equalsIgnoreCase(SysConfig.get("STATE:PLC_ERROR"))) {
            listBottleDisableReason.add("PLC_ERROR");
        }
        if ("TRUE".equalsIgnoreCase(SysConfig.get("STATE:PLC_FIRST_LIGHT_ERROR"))) {
            listBottleDisableReason.add("PLC_FIRST_LIGHT_ERROR");
        }
        if ("TRUE".equalsIgnoreCase(SysConfig.get("STATE:PLC_SECOND_LIGHT_ERROR"))) {
            listBottleDisableReason.add("PLC_SECOND_LIGHT_ERROR");
        }
        if ("TRUE".equalsIgnoreCase(SysConfig.get("STATE:PLC_THIRD_LIGHT_ERROR"))) {
            listBottleDisableReason.add("PLC_THIRD_LIGHT_ERROR");
        }
        TextView remindText = (TextView) findViewById(R.id.reminder_text);
        if (listBottleDisableReason.size() == 0) {
            z = true;
        } else {
            z = false;
        }
        this.recycleBottle = z;
        if (this.recycleBottle) {
            this.jsonDisableReason = null;
            putBottleBtnBackgrundMonitor();
            remindText.setVisibility(View.GONE);
            try {
                if ("TRUE".equalsIgnoreCase(SysConfig.get("COM.PLC.HAS.DOOR"))) {
                    if ("OPEN".equalsIgnoreCase(SysConfig.get("COM.PLC.DOOR.STATE.INIT"))) {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "openBottleDoor", null);
                        return;
                    }
                    return;
                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        String jsonDisableReason = JSONUtils.toJSON((List) listBottleDisableReason);
        if (!jsonDisableReason.equals(this.jsonDisableReason)) {
            this.jsonDisableReason = jsonDisableReason;
            logger.debug("MAINTAINSTATE:" + jsonDisableReason);
        }
        try {
            if ("TRUE".equalsIgnoreCase(SysConfig.get("COM.PLC.HAS.DOOR"))) {
                if ("OPEN".equalsIgnoreCase(SysConfig.get("COM.PLC.DOOR.STATE.INIT"))) {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "closeBottleDoor", null);
                }
            }
            remindText.setVisibility(View.VISIBLE);
            if (this.isMaxBottle) {
                this.btnThrowBottles.updateResource(R.drawable.btn_mancang, null);
                remindText.setText(R.string.ReachMaxOfBottle);
            } else if (this.isOutOfServiceBottle) {
                this.btnThrowBottles.updateResource(R.drawable.btn_zantingfuwu, null);
                remindText.setText(R.string.outOfService);
            } else {
                this.btnThrowBottles.updateResource(R.drawable.btn_zantingfuwu, null);
                remindText.setText(R.string.btnthrowBottleOutOfService);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void putBottleBtnBackgrundMonitor() {
        List<HashMap<String, String>> homeRightImage = BroadcastTaskCommonService.getHomePageRightTopList(this.adconfURL);
        if (homeRightImage != null && homeRightImage.size() > 0) {
            try {
                String picPath = (String) ((HashMap) homeRightImage.get(0)).get(AllAdvertisement.MAIN_PICTURE_PATH);
                if (!StringUtils.isBlank(picPath) && new File(picPath).isFile()) {
                    this.btnThrowBottles.updateResource(-1, picPath);
                    return;
                }
            } catch (Exception e) {
            }
        }
        this.btnThrowBottles.updateResource(R.drawable.pingzitu, null);
    }

    public void enableRecyclePaper(List<String> listPaperDisableReason) {
        if (listPaperDisableReason != null && listPaperDisableReason.size() > 0) {
            this.isOutOfServicePaper = true;
        }
        try {
            HashMap<String, Object> hashMap = new HashMap();
            hashMap.put("PRODUCT_TYPE", "PAPER");
            this.isMaxPaper = ((Boolean) CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryIsStorageMax", hashMap).get("IS_MAX_PAPER")).booleanValue();
            if (this.isMaxPaper) {
                listPaperDisableReason.add("STORAGE_MAX_PAPER");
            }
            if ("TRUE".equalsIgnoreCase(SysConfig.get("STATE:PLC_ERROR"))) {
                listPaperDisableReason.add("PLC_ERROR");
            }
            this.recyclepaper = listPaperDisableReason.size() == 0;
            this.btnThrowPaper.setEnabled(this.recyclepaper);
            if (this.recyclepaper) {
                this.btnThrowPaper.updateResource(R.drawable.diancitouzhi, null);
            } else if (this.isMaxPaper) {
                this.btnThrowPaper.updateResource(R.drawable.btn_mancang, null);
            } else if (this.isOutOfServicePaper) {
                this.btnThrowPaper.updateResource(R.drawable.btn_zantingfuwu, null);
            } else {
                this.btnThrowPaper.updateResource(R.drawable.btn_zantingfuwu, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRecycleBottle() throws Exception {
        try {
            GUIGlobal.clearMap();
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initRecycle", null);
            HashMap<String, Object> hsmpInitProductType = new HashMap();
            hsmpInitProductType.put("PRODUCT_TYPE", "BOTTLE");
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initProductType", hsmpInitProductType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        GUIGlobal.setCurrentSession("USESTATE", Integer.valueOf(0));
        if (!StringUtils.isBlank(SysConfig.get("PutBottleActivity.class"))) {
            GUIGlobal.setCurrentSession(AllAdvertisement.HOMEPAGE_LEFT, null);
            Intent intent = new Intent(getBaseContext(), Class.forName(SysConfig.get("PutBottleActivity.class")));
            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    private void startRecyclePaper() {
        try {
            GUIGlobal.clearMap();
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initRecycle", null);
            HashMap<String, Object> hsmpInitProductType = new HashMap();
            hsmpInitProductType.put("PRODUCT_TYPE", "PAPER");
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initProductType", hsmpInitProductType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        GUIGlobal.setCurrentSession("USESTATE", Integer.valueOf(0));
        Intent intent = new Intent(getBaseContext(), SelectRecycleActivity.class);
        intent.putExtra("RECYCLE", "RECYCLEPAPER");
        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    protected void onStart() {
        executeGUIAction(false, new GUIActionInit(), new Object[]{this});
        executeGUIAction(false, new GUIActionTakePhoto(), new Object[]{this});
        executeGUIAction(false, new GUIActionSysAction(), new Object[]{this, "sysInit"});
        SysConfig.set("UPDATE_ENABLE", "TRUE");
        executeGUIAction(false, new GUIActionSysAction(), new Object[]{this, "sysUpdate"});
        ((TextView) findViewById(R.id.serial_number)).setText(SysConfig.get("RVM.CODE"));
        TickTaskThread.getTickTaskThread().register(this.timeoutActionForShutdwon, (double) Integer.valueOf(SysConfig.get("RVM.TIMEOUT.CHECK.SHUTDOWN")).intValue(), true);
        TickTaskThread.getTickTaskThread().register(this.timeoutActionForSleepTime, (double) Integer.valueOf(SysConfig.get("RVM.TIMEOUT.CHECK.SHUTDOWN")).intValue(), true);
        TickTaskThread.getTickTaskThread().register(this.timeoutActionForQueryAD, (double) Integer.valueOf(SysConfig.get("QUERY.AD.TIME")).intValue(), true);
        qyeryAD();
        initConvenienceView();
        initMediaInfo();
        super.onStart();
    }

    private void initConvenienceView() {
        this.btnService = (MyGifView) findViewById(R.id.btnservice);
        String str = SysConfig.get("CONVENIENCESERVICE.WAY");
        List<String> listDisabledService = new ArrayList();
        try {
            HashMap<String, Object> hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryServiceDisable", null);
            if (hsmpResult != null) {
                String SERVICE_DISABLED = (String) hsmpResult.get("SERVICE_DISABLED");
                if (!StringUtils.isBlank(SERVICE_DISABLED)) {
                    String[] SERVICE_DISABLED_ARRAY = SERVICE_DISABLED.split(",");
                    for (String add : SERVICE_DISABLED_ARRAY) {
                        listDisabledService.add(add);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (StringUtils.isBlank(str)) {
            this.btnService.setVisibility(View.GONE);
            return;
        }
        if ("TRUE".equalsIgnoreCase(SysConfig.get(MediaInfo.CONVENIENCE_ACTIVITY)) && "CONSERVICE".equalsIgnoreCase(str)) {
            String[] strSet = str.split(";");
            for (String equalsIgnoreCase : strSet) {
                if (equalsIgnoreCase.equalsIgnoreCase("CONSERVICE")) {
                    this.btnService.setVisibility(View.VISIBLE);
                }
            }
            if (listDisabledService.contains("CONSERVICE")) {
                this.btnService.setVisibility(View.GONE);
            } else {
                this.btnService.setVisibility(View.VISIBLE);
            }
        } else {
            this.btnService.setVisibility(View.GONE);
        }
        if ((listDisabledService.contains(ServiceName.PRINTER) || "FALSE".equalsIgnoreCase(SysConfig.get("SET.PRINT.ENABLE"))) && !"FALSE".equalsIgnoreCase(SysConfig.get("SET.PRINT.ENABLE"))) {
            Properties externalProp1 = new Properties();
            externalProp1.put("SET.PRINT.ENABLE", "FALSE");
            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp1);
            SysConfig.set(externalProp1);
            RVMShell.backupExternalConfig();
        }
    }

    protected void onResume() {
        this.isClick = false;
        if (!(isFinishing() || this.myVideoView == null)) {
            this.myVideoView.resume();
        }
        super.onResume();
    }

    protected void onPause() {
        SysConfig.set("UPDATE_ENABLE", "FALSE");
        executeGUIAction(false, new GUIActionSysAction(), new Object[]{this, "sysUpdate"});
        if (!(isFinishing() || this.myVideoView == null)) {
            this.myVideoView.pause();
        }
        super.onPause();
    }

    protected void onStop() {
        this.contentView.updateResource(-1, null);
        TickTaskThread.getTickTaskThread().unregister(this.timeoutActionForShutdwon);
        TickTaskThread.getTickTaskThread().unregister(this.timeoutActionForSleepTime);
        TickTaskThread.getTickTaskThread().unregister(this.timeoutActionForQueryAD);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForNextMedia, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutActionForNextMedia);
        super.onStop();
    }

    public void finish() {
        executeGUIAction(true, new GUIActionSysAction(), new Object[]{this, "sysExit"});
        super.finish();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public String getName() {
        return "RVMMainActivity";
    }

    public void updateLanguage() {
        ((TextView) findViewById(R.id.rvm_code)).setText(R.string.setupCfgTimerNumText);
    }

    public void doEvent(HashMap hsmpEvent) {
        if ("CMD".equalsIgnoreCase((String) hsmpEvent.get("EVENT"))) {
            String CMD = (String) hsmpEvent.get("CMD");
            if ("REQUEST_DONATION_RECYCLE".equalsIgnoreCase(CMD)) {
                GUIGlobal.clearMap();
                if (this.recycleBottle) {
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initRecycle", null);
                        HashMap<String, Object> hsmpInitProductType = new HashMap();
                        hsmpInitProductType.put("PRODUCT_TYPE", "BOTTLE");
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initProductType", hsmpInitProductType);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{getBaseActivity(), Integer.valueOf(2), "DONATION"});
                } else {
                    return;
                }
            }
            if ("PLC_COMM_ERROR".equalsIgnoreCase(CMD)) {
                executeGUIAction(false, new GUIActionInit(), new Object[]{this});
            }
            if ("PLC_COMM_ERROR_RECOVERY".equalsIgnoreCase(CMD)) {
                executeGUIAction(false, new GUIActionInit(), new Object[]{this});
            }
            if ("SERVICE_ENABLE_CHANGED".equalsIgnoreCase(CMD)) {
                executeGUIAction(true, new GUIActionInit(), new Object[]{this});
            }
            if ("PRINTER_ERROR".equalsIgnoreCase(CMD)) {
            }
            if ("PRINTER_ERROR_RECOVERY".equalsIgnoreCase(CMD)) {
            }
            if ("PRINTER_NO_PAPER".equalsIgnoreCase(CMD)) {
            }
            if ("PRINTER_NO_PAPER_RECOVERY".equalsIgnoreCase(CMD)) {
            }
            if ("BARCODE_SCANER_ERROR".equalsIgnoreCase(CMD)) {
            }
            if ("BARCODE_SCANER_ERROR_RECOVERY".equalsIgnoreCase(CMD)) {
            }
            if ("START_OR_STOP_SERVER".equalsIgnoreCase(CMD)) {
                executeGUIAction(false, new GUIActionInit(), new Object[]{this});
            }
            if ("REQUEST_RECYCLE".equalsIgnoreCase(CMD)) {
                try {
                    if (this.recycleBottle) {
                        startRecycleBottle();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            if ("LIGHT_NORMAL".equalsIgnoreCase(CMD)) {
                executeGUIAction(false, new GUIActionInit(), new Object[]{this});
            }
            if ("LIGHT_UNNORMAL_ON".equalsIgnoreCase(CMD)) {
                executeGUIAction(false, new GUIActionInit(), new Object[]{this});
            }
            if (ServiceName.TAKE_PHOTO.equalsIgnoreCase(CMD)) {
                executeGUIAction(false, new GUIActionTakePhoto(), new Object[]{getBaseActivity()});
            }
        }
    }

    private void initMediaInfo() {
        synchronized (this.syncOBJ) {
            if (this.HOMEPAGE_LEFT_BOTTOM_LIST == null || this.HOMEPAGE_LEFT_BOTTOM_LIST.size() <= 0) {
                this.adLayout.setVisibility(View.GONE);
                this.myVideoView.setVisibility(View.GONE);
                this.contentView.setVisibility(View.VISIBLE);
                this.contentView.updateResource(R.drawable.mian_pic, null);
                TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForNextMedia, Integer.valueOf(SysConfig.get("DEFAULT.AD.TIME")).intValue(), false);
                TimeoutTask.getTimeoutTask().reset(this.timeoutActionForNextMedia, Integer.valueOf(SysConfig.get("DEFAULT.AD.TIME")).intValue());
            } else {
                this.adLayout.setVisibility(View.VISIBLE);
                if (indexOfListMedia >= this.HOMEPAGE_LEFT_BOTTOM_LIST.size()) {
                    indexOfListMedia = 0;
                }
                setListSize();
                initIcon();
                String MEDIA_TYPE = (String) ((HashMap) this.HOMEPAGE_LEFT_BOTTOM_LIST.get(indexOfListMedia)).get(AllAdvertisement.MEDIA_TYPE);
                if (AllAdvertisement.MEDIA_TYPE_MOVIE.equalsIgnoreCase(MEDIA_TYPE)) {
                    this.mhandler.sendEmptyMessage(1);
                } else if ("PICTURE".equalsIgnoreCase(MEDIA_TYPE)) {
                    this.mhandler.sendEmptyMessage(3);
                }
            }
        }
    }

    private void setListSize() {
        if (this.HOMEPAGE_LEFT_BOTTOM_LIST != null && !this.HOMEPAGE_LEFT_BOTTOM_LIST.isEmpty()) {
            int itemSize = this.HOMEPAGE_LEFT_BOTTOM_LIST.size();
            if (itemSize < 5) {
                if (itemSize == 1) {
                    this.adLayout.setVisibility(View.GONE);
                } else {
                    this.adLayout.setVisibility(View.VISIBLE);
                }
                LayoutParams params = this.mListView.getLayoutParams();
                if (itemSize == 2) {
                    params.height = 324;
                }
                if (itemSize == 3) {
                    params.height = 466;
                }
                if (itemSize == 4) {
                    params.height = 608;
                }
                if (itemSize == 5) {
                    params.height = 750;
                }
                this.mListView.setLayoutParams(params);
            }
        }
    }

    private void initIcon() {
        if (this.adapter == null) {
            this.adapter = new MianActivityIconAdapter(getBaseContext(), this.HOMEPAGE_LEFT_BOTTOM_LIST);
            this.beforeList = this.HOMEPAGE_LEFT_BOTTOM_LIST;
            this.mListView.setAdapter(this.adapter);
        }
        if (this.beforeList == null || this.beforeList.size() != this.HOMEPAGE_LEFT_BOTTOM_LIST.size()) {
            this.adapter = new MianActivityIconAdapter(getBaseContext(), this.HOMEPAGE_LEFT_BOTTOM_LIST);
            this.mListView.setAdapter(this.adapter);
        }
        if (this.beforeList != null && this.beforeList.size() == this.HOMEPAGE_LEFT_BOTTOM_LIST.size() && !this.beforeList.containsAll(this.HOMEPAGE_LEFT_BOTTOM_LIST)) {
            this.adapter = new MianActivityIconAdapter(getBaseContext(), this.HOMEPAGE_LEFT_BOTTOM_LIST);
            this.beforeList = this.HOMEPAGE_LEFT_BOTTOM_LIST;
            this.mListView.setAdapter(this.adapter);
        }
    }

    private void gotoNextMedia() {
        synchronized (this.syncOBJ) {
            indexOfListMedia++;
            if (this.HOMEPAGE_LEFT_BOTTOM_LIST == null || indexOfListMedia >= this.HOMEPAGE_LEFT_BOTTOM_LIST.size()) {
                indexOfListMedia = 0;
            }
            if (this.adapter != null) {
                this.adapter.notifyDataSetChanged();
            }
            this.mListView.smoothScrollToPosition(indexOfListMedia);
        }
        initMediaInfo();
    }

    private void play() {
        synchronized (this.syncOBJ) {
            if (this.HOMEPAGE_LEFT_BOTTOM_LIST != null && this.HOMEPAGE_LEFT_BOTTOM_LIST.size() > 0 && indexOfListMedia < this.HOMEPAGE_LEFT_BOTTOM_LIST.size()) {
                String path = (String) ((HashMap) this.HOMEPAGE_LEFT_BOTTOM_LIST.get(indexOfListMedia)).get(AllAdvertisement.MAIN_MOVIE_PATH);
                try {
                    if (this.myVideoView == null || StringUtils.isBlank(path) || !new File(path).isFile()) {
                        gotoNextMedia();
                    } else {
                        this.contentView.setVisibility(View.GONE);
                        this.myVideoView.setVisibility(View.VISIBLE);
                        this.myVideoView.setVideoPath(path);
                    }
                } catch (Exception e) {
                    gotoNextMedia();
                }
            }
        }
    }

    private boolean compareShutdownTime() {
        boolean isInMaintainingTime = false;
        try {
            boolean isNowYear;
            String powerOnTime = SysConfig.get("RVM.POWER.ON.TIME");
            String powerOffTime = SysConfig.get("RVM.POWER.OFF.TIME");
            if (!(StringUtils.isBlank(powerOffTime) || StringUtils.isBlank(powerOffTime) || powerOnTime.equals(powerOffTime))) {
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
            if (Integer.parseInt(DateUtils.formatDatetime(new Date(), "yyyy")) < 2015) {
                isNowYear = false;
            } else {
                isNowYear = true;
            }
            if (isNowYear) {
                try {
                    int checkNum = 0;
                    String UPDATED_CHECK_COUNT = ShellUtils.execScript(StringUtils.replace(StringUtils.replace(new String(IOUtils.readResource("get_version.sh")), "$yyyy-MM-dd$", DateUtils.formatDatetime(new Date(), "yyyy-MM-dd")), "$dd$", DateUtils.formatDatetime(new Date(), "dd")));
                    if (!StringUtils.isBlank(UPDATED_CHECK_COUNT)) {
                        int idx = UPDATED_CHECK_COUNT.indexOf(":");
                        if (idx != -1) {
                            String CHECK_COUNT = UPDATED_CHECK_COUNT.substring(idx + 1).trim();
                            int startIdx = CHECK_COUNT.indexOf("[");
                            int endIdx = CHECK_COUNT.indexOf("]");
                            if (startIdx >= 0 && endIdx == CHECK_COUNT.length() - 1) {
                                try {
                                    checkNum = Integer.parseInt(CHECK_COUNT.substring(startIdx + 1, endIdx));
                                } catch (Exception e) {
                                }
                                if (checkNum > 0) {
                                    HashMap hsmp = new HashMap();
                                    hsmp.put("UPDATE_DETECTION", updateDetection.VERSION);
                                    CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "updateDetection", hsmp);
                                }
                            }
                        }
                    }
                } catch (Exception e2) {
                    IOUtils.close((OutputStream) null);
                }
            }
            setMaintaining(isInMaintainingTime, isNowYear);
            return true;
        } catch (Exception e3) {
            return true;
        }
    }

    private void setMaintaining(boolean isInMaintainingTime, boolean isNowYear) {
        if (isInMaintainingTime != this.isMaintaining || isNowYear != this.isCorrectYear) {
            this.isMaintaining = isInMaintainingTime;
            this.isCorrectYear = isNowYear;
            executeGUIAction(false, new GUIActionInit(), new Object[]{getBaseActivity()});
        }
    }

    private void compareSleepTime() {
        try {
            HashMap hsmp = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "querySleepTime", null);
            if (hsmp != null && !hsmp.isEmpty()) {
                if ("TRUE".equalsIgnoreCase((String) hsmp.get("RESULT"))) {
                    GUIGlobal.setCurrentSession("SLEEP_LIST", (List) hsmp.get("SLEEP_LIST"));
                    this.isStopService = true;
                } else {
                    this.isStopService = false;
                }
                executeGUIAction(false, new GUIActionInit(), new Object[]{getBaseActivity()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void qyeryAD() {
        this.HOMEPAGE_LEFT_BOTTOM_LIST = BroadcastTaskCommonService.getHomePageFullAdList(this.adconfURL);
    }
}
