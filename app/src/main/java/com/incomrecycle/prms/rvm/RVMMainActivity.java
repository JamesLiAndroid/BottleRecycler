package com.incomrecycle.prms.rvm;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.common.task.TickTaskThread;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.ShellUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.RVMShell;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.common.SysDef.MediaInfo;
import com.incomrecycle.prms.rvm.common.SysDef.ServiceName;
import com.incomrecycle.prms.rvm.common.SysDef.audioCurrentState;
import com.incomrecycle.prms.rvm.common.SysDef.updateDetection;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.BroadcastTaskCommonService;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.gui.action.GUIActionInit;
import com.incomrecycle.prms.rvm.gui.action.GUIActionSysAction;
import com.incomrecycle.prms.rvm.gui.action.GUIActionTakePhoto;
import com.incomrecycle.prms.rvm.gui.activity.aboutus.AboutUsActivity;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelActivity;
import com.incomrecycle.prms.rvm.gui.activity.convenienceservices.QueryOneCardActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.ActivityPageActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.AdvertisementVideoActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.AnnouncementActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.FaultListActivity;
import com.incomrecycle.prms.rvm.gui.activity.starput.Not_concern_wechat;
import com.incomrecycle.prms.rvm.gui.activity.starput.SelectRecycleActivity;
import com.incomrecycle.prms.rvm.gui.activity.view.MyGifView;
import com.incomrecycle.prms.rvm.gui.activity.view.MyVideoView;
import com.incomrecycle.prms.rvm.gui.activity.view.VerticalMarqueeTextview;
import com.incomrecycle.prms.rvm.gui.entity.TextAdEntity;
import it.sauronsoftware.ftp4j.FTPCodes;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class RVMMainActivity extends BaseActivity {
    private static final Logger logger = LoggerFactory.getLogger("RVMMainActivity");
    private String CURRENT_STATE = null;
    private HashMap<String, String> HOMEPAGE_CENTER_TOP = new HashMap();
    private List<HashMap<String, String>> HOMEPAGE_CENTER_TOP_LIST = new ArrayList();
    private HashMap<String, String> HOMEPAGE_LEFT_BOTTOM = new HashMap();
    private List<HashMap<String, String>> HOMEPAGE_LEFT_BOTTOM_LIST = new ArrayList();
    private HashMap<String, String> HOMEPAGE_LEFT_TOP = new HashMap();
    private List<HashMap<String, String>> HOMEPAGE_LEFT_TOP_LIST = new ArrayList();
    private List<HashMap<String, String>> HOMEPAGE_SCREEN_LIST = new ArrayList();
    private HashMap<String, Object> TRANSMIT_ADV = new HashMap();
    private Button aboutUsBtn = null;
    private MyGifView activityImage = null;
    private String adconfURL;
    private Button btnService = null;
    private Button channel = null;
    private MyGifView contentView = null;
    private int indexOfListActivity = 0;
    private int indexOfListAdText = 0;
    private int indexOfListCenterTop = 0;
    private int indexOfListMedia = 0;
    private boolean isClick = false;
    private boolean isCorrectYear = true;
    private boolean isMaintaining = false;
    private boolean isMaxBottle;
    private boolean isMaxPaper;
    private boolean isOutOfServiceBottle = false;
    private boolean isOutOfServicePaper = false;
    private boolean isPlaySounds = false;
    private MyGifView jiantouImage = null;
    private String jsonDisableReason = null;
    private List languageList = new ArrayList();
    private List<TextAdEntity> listAdText = new ArrayList();
    private List<String> listDisabledService = new ArrayList();

    GUICommonService guiCommonService;

    private Handler mhandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    RVMMainActivity.this.play();
                    return;
                case 3:
                    String picURL = null;
                    if (RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST != null && RVMMainActivity.this.indexOfListMedia < RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.size()) {
                        picURL = (String) ((HashMap) RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.get(RVMMainActivity.this.indexOfListMedia)).get(AllAdvertisement.MAIN_PICTURE_PATH);
                    }
                    if (!StringUtils.isBlank(picURL) && new File(picURL).isFile()) {
                        RVMMainActivity.this.myVideoView.setVisibility(View.GONE);
                        RVMMainActivity.this.contentView.setVisibility(View.VISIBLE);
                        RVMMainActivity.this.contentView.updateResource(-1, picURL);
                    }
                    if (RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST != null && RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.size() > 0) {
                        int picTimeOutTime;
                        try {
                            picTimeOutTime = Integer.valueOf((String) ((HashMap) RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.get(RVMMainActivity.this.indexOfListMedia))
                                    .get(AllAdvertisement.PLAY_SECONDS)).intValue();
                        } catch (Exception e) {
                            picTimeOutTime = Integer.valueOf(SysConfig.get("DEFAULT.AD.TIME")).intValue();
                        }
                        TimeoutTask.getTimeoutTask().addTimeoutAction(RVMMainActivity.this.timeoutActionForNextMedia, picTimeOutTime, false);
                        TimeoutTask.getTimeoutTask().reset(RVMMainActivity.this.timeoutActionForNextMedia, picTimeOutTime);
                        TimeoutTask.getTimeoutTask().setEnabled(RVMMainActivity.this.timeoutActionForNextMedia, true);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private String moviepath;
    private MyVideoView myVideoView = null;
    private boolean myVideoViewIsEnabled = true;
    private boolean recycleBottle = false;
    private boolean recyclepaper = false;
    private VerticalMarqueeTextview sbarView = null;
    private SoundPool soundPool = null;
    private String[] strs = new String[2];
    Object syncOBJ = new Object();
    Object syncObjOfAD = new Object();
    Object syncObjOfActivity = new Object();
    Object syncObjOfCenterTopButton = new Object();
    private TimeoutAction timeoutAction2Video = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    if (((Integer) paramObjs[1]).intValue() == 0) {
                        Intent intent = new Intent();
                        intent.putExtra("recycleBottle", RVMMainActivity.this.recycleBottle);
                        intent.setClass(RVMMainActivity.this, ScreenActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        RVMMainActivity.this.startActivity(intent);
                    }
                }
            };
            RVMMainActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TimeoutAction timeoutActionCheckDB = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    if (((Integer) paramObjs[1]).intValue() == 0) {
                        RVMMainActivity.this.checkDatabase();
                        TimeoutTask.getTimeoutTask().reset(RVMMainActivity.this.timeoutActionCheckDB);
                        TimeoutTask.getTimeoutTask().setEnabled(RVMMainActivity.this.timeoutActionCheckDB, true);
                    }
                }
            };
            RVMMainActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TimeoutAction timeoutActionForAdText = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    if (((Integer) paramObjs[1]).intValue() == 0) {
                        RVMMainActivity.this.loadSubList();
                        TimeoutTask.getTimeoutTask().reset(RVMMainActivity.this.timeoutActionForAdText);
                        TimeoutTask.getTimeoutTask().setEnabled(RVMMainActivity.this.timeoutActionForAdText, true);
                    }
                }
            };
            RVMMainActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TimeoutAction timeoutActionForCheckAudio = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    if (((Integer) paramObjs[1]).intValue() == 0) {
                        Properties externalProp = new Properties();
                        externalProp.put("CURRENT_STATE", audioCurrentState.CHECKING);
                        PropUtils.update(SysConfig.get("AUDIO_CHECK.FILE"), externalProp);
                        SysConfig.set(externalProp);
                        if (RVMMainActivity.this.isPlaySounds && RVMMainActivity.this.soundPool == null) {
                            RVMMainActivity.this.soundPool = new SoundPool(1, 3, 0);
                        }
                        if (RVMMainActivity.this.isPlaySounds && RVMMainActivity.this.soundPool != null) {
                            RVMMainActivity.this.soundPool.load(RVMMainActivity.this, R.raw.blank, 0);
                            RVMMainActivity.this.soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                                    soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                                }
                            });
                        }
                        externalProp.put("CURRENT_STATE", audioCurrentState.CHECKED);
                        PropUtils.update(SysConfig.get("AUDIO_CHECK.FILE"), externalProp);
                        SysConfig.set(externalProp);
                        TimeoutTask.getTimeoutTask().reset(RVMMainActivity.this.timeoutActionForCheckAudio);
                        TimeoutTask.getTimeoutTask().setEnabled(RVMMainActivity.this.timeoutActionForCheckAudio, true);
                    }
                }
            };
            RVMMainActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TimeoutAction timeoutActionForNextActivity = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    if (((Integer) paramObjs[1]).intValue() == 0) {
                        synchronized (RVMMainActivity.this.syncObjOfActivity) {
                            RVMMainActivity.this.indexOfListActivity = RVMMainActivity.this.indexOfListActivity + 1;
                            if (RVMMainActivity.this.HOMEPAGE_LEFT_TOP_LIST == null || RVMMainActivity.this.indexOfListActivity >= RVMMainActivity.this.HOMEPAGE_LEFT_TOP_LIST.size()) {
                                RVMMainActivity.this.indexOfListActivity = 0;
                            }
                        }
                        RVMMainActivity.this.portalToActivity();
                    }
                }
            };
            RVMMainActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TimeoutAction timeoutActionForNextCenterTopButton = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    if (((Integer) paramObjs[1]).intValue() == 0) {
                        synchronized (RVMMainActivity.this.syncObjOfCenterTopButton) {
                            RVMMainActivity.this.indexOfListCenterTop = RVMMainActivity.this.indexOfListCenterTop + 1;
                            if (RVMMainActivity.this.HOMEPAGE_CENTER_TOP_LIST == null || RVMMainActivity.this.indexOfListCenterTop >= RVMMainActivity.this.HOMEPAGE_CENTER_TOP_LIST.size()) {
                                RVMMainActivity.this.indexOfListCenterTop = 0;
                            }
                        }
                        RVMMainActivity.this.initCenterTopButton();
                    }
                }
            };
            RVMMainActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TimeoutAction timeoutActionForNextMedia = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    if (((Integer) paramObjs[1]).intValue() == 0) {
                        synchronized (RVMMainActivity.this.syncOBJ) {
                            RVMMainActivity.this.indexOfListMedia = RVMMainActivity.this.indexOfListMedia + 1;
                            if (RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST == null || RVMMainActivity.this.indexOfListMedia >= RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.size()) {
                                RVMMainActivity.this.indexOfListMedia = 0;
                            }
                        }
                        RVMMainActivity.this.initMediaInfo();
                    }
                }
            };
            RVMMainActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TimeoutAction timeoutActionForSetDisable = new TimeoutAction() {
        private Activity context = RVMMainActivity.this;

        public void apply(int forwardSeconds, int remainedSeconds) {
            if (remainedSeconds == 0) {
                this.context.runOnUiThread(RVMMainActivity.this.updateThread);
            }
        }
    };
    private TimeoutAction timeoutActionForShowNextAdText = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            if (remainedSeconds == 0) {
                String text = RVMMainActivity.this.getResources().getString(R.string.welcome);
                synchronized (RVMMainActivity.this.syncObjOfAD) {
                    RVMMainActivity.this.indexOfListAdText = RVMMainActivity.this.indexOfListAdText + 1;
                    if (RVMMainActivity.this.listAdText == null || RVMMainActivity.this.indexOfListAdText >= RVMMainActivity.this.listAdText.size()) {
                        RVMMainActivity.this.indexOfListAdText = 0;
                    }
                    if (RVMMainActivity.this.listAdText.size() > 0) {
                        text = ((TextAdEntity) RVMMainActivity.this.listAdText.get(RVMMainActivity.this.indexOfListAdText)).getSbarTxt();
                    }
                }
                final String textAD = text;
                RVMMainActivity.this.sbarView.post(new Runnable() {
                    public void run() {
                        RVMMainActivity.this.sbarView.setText(textAD);
                    }
                });
                TimeoutTask.getTimeoutTask().reset(RVMMainActivity.this.timeoutActionForShowNextAdText);
            }
        }
    };
    private TaskAction timeoutActionForShutdwon = new TaskAction() {
        public void execute() {
            RVMMainActivity.this.executeGUIAction(false, new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    RVMMainActivity.this.compareShutdownTime();
                }
            }, new Object[0]);
        }
    };
    Runnable updateThread = new Runnable() {
        public void run() {
            RVMMainActivity.this.aboutUsBtn.setEnabled(true);
            RVMMainActivity.this.myVideoView.setEnabled(true);
            RVMMainActivity.this.btnService.setEnabled(true);
            RVMMainActivity.this.sbarView.setEnabled(true);
            RVMMainActivity.this.weixinButton.setEnabled(true);
            RVMMainActivity.this.activityImage.setEnabled(true);
            RVMMainActivity.this.channel.setEnabled(true);
        }
    };
    private Button weixinButton = null;

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        try {
            compareShutdownTime();
            loadSubList();
            checkDatabase();
            executeGUIAction(false, new GUIActionInit(), new Object[]{this});
            executeGUIAction(false, new GUIActionTakePhoto(), new Object[]{this});
            executeGUIAction(false, new GUIActionSysAction(), new Object[]{this, "sysInit"});
            SysConfig.set("UPDATE_ENABLE", "TRUE");
            executeGUIAction(false, new GUIActionSysAction(), new Object[]{this, "sysUpdate"});
            ((TextView) findViewById(R.id.serial_number)).setText(SysConfig.get("RVM.CODE"));
            TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForShowNextAdText, Integer.valueOf(SysConfig.get("RVM.CHANGE.TEXTAD.TIME")).intValue(), false);
            TimeoutTask.getTimeoutTask().reset(this.timeoutActionForShowNextAdText);
            TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForShowNextAdText, true);
            TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForAdText, Integer.valueOf(SysConfig.get("RVM.QUERY.TEXTAD.TIME")).intValue(), false);
            TimeoutTask.getTimeoutTask().reset(this.timeoutActionForAdText);
            TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForAdText, true);
            TickTaskThread.getTickTaskThread().register(this.timeoutActionForShutdwon, (double) Integer.valueOf(SysConfig.get("RVM.TIMEOUT.CHECK.SHUTDOWN")).intValue(), true);
            TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionCheckDB, Integer.valueOf(SysConfig.get("RVM.CHECK.DB.TIME")).intValue(), false);
            TimeoutTask.getTimeoutTask().reset(this.timeoutActionCheckDB);
            TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionCheckDB, true);
            ScreenIntent();
            TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForSetDisable, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.SET.DISENABLE")).intValue(), false);
            TimeoutTask.getTimeoutTask().reset(this.timeoutActionForSetDisable);
            TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForSetDisable, true);
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
            if (this.isPlaySounds) {
                String fileURL = SysConfig.get("AUDIO_CHECK.FILE");
                if (!StringUtils.isBlank(fileURL)) {
                    Properties myProperties = PropUtils.loadFile(fileURL);
                    this.CURRENT_STATE = myProperties.getProperty("CURRENT_STATE");
                    if (StringUtils.isBlank(this.CURRENT_STATE)) {
                        Properties newProp = new Properties();
                        newProp.put("CURRENT_STATE", audioCurrentState.CHECKED);
                        myProperties.putAll(newProp);
                        if (StringUtils.isBlank(myProperties.getProperty("CURRENT_STATE"))) {
                            myProperties.put("CURRENT_STATE", audioCurrentState.CHECKED);
                        }
                        SysConfig.set(myProperties);
                        PropUtils.update(fileURL, myProperties);
                    }
                }
                if (audioCurrentState.CHECKING.equalsIgnoreCase(this.CURRENT_STATE)) {
                    Properties externalProp = new Properties();
                    externalProp.put("IS_PLAY_SOUNDS", "FALSE");
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                    SysConfig.set(externalProp);
                    RVMShell.backupExternalConfig();
                    audioManager.setStreamVolume(3, 0, 0);
                } else {
                    TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForCheckAudio, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.CHECK.AUDIO")).intValue(), false);
                    TimeoutTask.getTimeoutTask().reset(this.timeoutActionForCheckAudio);
                    TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForCheckAudio, true);
                }
            } else {
                audioManager.setStreamVolume(3, 0, 0);
            }
            this.aboutUsBtn.setEnabled(false);
            this.myVideoView.setEnabled(false);
            this.btnService.setEnabled(false);
            this.sbarView.setEnabled(false);
            this.weixinButton.setEnabled(false);
            this.activityImage.setEnabled(false);
            this.channel.setEnabled(false);
            initView();
            initMediaInfo();
            portalToActivity();
            initCenterTopButton();
            if (this.listAdText.size() != 0) {
                this.sbarView.setMylist(this.listAdText);
            }
        } catch (Exception e) {
            logger.debug("Exception on logger onStart", e);
        }
        List<HashMap<String, String>> homeRightImage = new ArrayList();
        refreshAD();
        homeRightImage = BroadcastTaskCommonService.getHomePageRightTopList(this.adconfURL);
        if (homeRightImage != null && homeRightImage.size() > 0) {
            if (((HashMap) homeRightImage.get(0)).get(AllAdvertisement.MAIN_PICTURE_PATH) != null) {
                this.jiantouImage = (MyGifView) findViewById(R.id.jiantou_text);
                this.jiantouImage.updateResource(-1, (String) ((HashMap) homeRightImage.get(0)).get(AllAdvertisement.MAIN_PICTURE_PATH));
                return;
            }
            this.jiantouImage.setBackgroundResource(R.drawable.pingzitu);
        }
    }

    protected void onResume() {
        super.onResume();
        this.isClick = false;
        this.myVideoViewIsEnabled = true;
        if (!isFinishing() && this.myVideoView != null) {
            this.myVideoView.resume();
        }
    }

    public void finish() {
        executeGUIAction(true, new GUIActionSysAction(), new Object[]{this, "sysExit"});
        super.finish();
    }

    public String sub() {
        String substr = getName();
        return substr.substring(0, substr.length() - 1);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            String[] strs1;
            int i;
            getWindow().getDecorView().setSystemUiVisibility(1);
            setContentView(R.layout.activity_rvm_main);
            backgroundToActivity();
            this.activityImage = (MyGifView) findViewById(R.id.myImageView);
            this.activityImage.updateResource(R.drawable.pic, null);
            ((MyGifView) findViewById(R.id.myQRcodeImage)).updateResource(R.drawable.attention, null);
            this.jiantouImage = (MyGifView) findViewById(R.id.jiantou_text);
            this.weixinButton = (Button) findViewById(R.id.weixinButton);
            this.weixinButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.WEIXIN);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                        GUIGlobal.clearMap();
                        if (RVMMainActivity.this.HOMEPAGE_CENTER_TOP_LIST != null && RVMMainActivity.this.HOMEPAGE_CENTER_TOP_LIST.size() > 0
                                && RVMMainActivity.this.indexOfListCenterTop < RVMMainActivity.this.HOMEPAGE_CENTER_TOP_LIST.size()) {
                            RVMMainActivity.this.HOMEPAGE_CENTER_TOP = (HashMap) RVMMainActivity.this.HOMEPAGE_CENTER_TOP_LIST.get(RVMMainActivity.this.indexOfListCenterTop);
                            String MAIN_CLK_PIC = null;
                            String VENDING_WAY = null;
                            if (RVMMainActivity.this.HOMEPAGE_CENTER_TOP != null) {
                                MAIN_CLK_PIC = (String) RVMMainActivity.this.HOMEPAGE_CENTER_TOP.get(AllAdvertisement.CLICK_PICTURE_PATH);
                                VENDING_WAY = (String) RVMMainActivity.this.HOMEPAGE_CENTER_TOP.get(AllAdvertisement.VENDING_WAY);
                                RVMMainActivity.this.HOMEPAGE_CENTER_TOP.put("RECYCLE_BOTTLE", RVMMainActivity.this.recycleBottle + "");
                            }
                            if (RVMMainActivity.this.TRANSMIT_ADV != null) {
                                RVMMainActivity.this.TRANSMIT_ADV.put("TRANSMIT_ADV", RVMMainActivity.this.HOMEPAGE_CENTER_TOP);
                            }
                            GUIGlobal.setCurrentSession(AllAdvertisement.HOMEPAGE_LEFT, RVMMainActivity.this.TRANSMIT_ADV);
                            if (StringUtils.isBlank(MAIN_CLK_PIC)) {
                                HashMap<String, Object> hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryServiceDisable", null);
                                if (hsmpResult != null) {
                                    String SERVICE_DISABLED = (String) hsmpResult.get("SERVICE_DISABLED");
                                    if (!StringUtils.isBlank(SERVICE_DISABLED)) {
                                        String[] SERVICE_DISABLED_ARRAY = SERVICE_DISABLED.split(",");
                                        for (String add : SERVICE_DISABLED_ARRAY) {
                                            RVMMainActivity.this.listDisabledService.add(add);
                                        }
                                    }
                                }
                                if (!StringUtils.isBlank(VENDING_WAY) && !RVMMainActivity.this.listDisabledService.contains(VENDING_WAY) && RVMMainActivity.this.recycleBottle) {
                                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initRecycle", null);
                                    HashMap<String, Object> hsmpInitProductType = new HashMap();
                                    hsmpInitProductType.put("PRODUCT_TYPE", "BOTTLE");
                                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initProductType", hsmpInitProductType);
                                    RVMMainActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{RVMMainActivity.this.getBaseActivity(), Integer.valueOf(2), VENDING_WAY});
                                }
                            } else if (!RVMMainActivity.this.isClick) {
                                RVMMainActivity.this.isClick = true;
                                Intent intent = new Intent(RVMMainActivity.this, Not_concern_wechat.class);
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                RVMMainActivity.this.startActivity(intent);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            });
            this.activityImage.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    try {
                        HashMap map = new HashMap();
                        map.put("KEY", AllClickContent.ACTIVITY);
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                        GUIGlobal.clearMap();
                        if (RVMMainActivity.this.HOMEPAGE_LEFT_TOP_LIST != null && RVMMainActivity.this.HOMEPAGE_LEFT_TOP_LIST.size() > 0
                                && RVMMainActivity.this.indexOfListActivity < RVMMainActivity.this.HOMEPAGE_LEFT_TOP_LIST.size()) {
                            RVMMainActivity.this.HOMEPAGE_LEFT_TOP = (HashMap) RVMMainActivity.this.HOMEPAGE_LEFT_TOP_LIST.get(RVMMainActivity.this.indexOfListActivity);
                            String MAIN_CLK_PIC = null;
                            String VENDING_WAY = null;
                            if (RVMMainActivity.this.HOMEPAGE_LEFT_TOP != null) {
                                MAIN_CLK_PIC = (String) RVMMainActivity.this.HOMEPAGE_LEFT_TOP.get(AllAdvertisement.CLICK_PICTURE_PATH);
                                VENDING_WAY = (String) RVMMainActivity.this.HOMEPAGE_LEFT_TOP.get(AllAdvertisement.VENDING_WAY);
                                RVMMainActivity.this.HOMEPAGE_LEFT_TOP.put("RECYCLE_BOTTLE", RVMMainActivity.this.recycleBottle + "");
                            }
                            if (RVMMainActivity.this.TRANSMIT_ADV != null) {
                                RVMMainActivity.this.TRANSMIT_ADV.put("TRANSMIT_ADV", RVMMainActivity.this.HOMEPAGE_LEFT_TOP);
                            }
                            GUIGlobal.setCurrentSession(AllAdvertisement.HOMEPAGE_LEFT, RVMMainActivity.this.TRANSMIT_ADV);
                            if (StringUtils.isBlank(MAIN_CLK_PIC)) {
                                HashMap<String, Object> hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryServiceDisable", null);
                                if (hsmpResult != null) {
                                    String SERVICE_DISABLED = (String) hsmpResult.get("SERVICE_DISABLED");
                                    if (!StringUtils.isBlank(SERVICE_DISABLED)) {
                                        String[] SERVICE_DISABLED_ARRAY = SERVICE_DISABLED.split(",");
                                        for (String add : SERVICE_DISABLED_ARRAY) {
                                            RVMMainActivity.this.listDisabledService.add(add);
                                        }
                                    }
                                }
                                if (!StringUtils.isBlank(VENDING_WAY) && !RVMMainActivity.this.listDisabledService.contains(VENDING_WAY) && RVMMainActivity.this.recycleBottle) {
                                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initRecycle", null);
                                    HashMap<String, Object> hsmpInitProductType = new HashMap();
                                    hsmpInitProductType.put("PRODUCT_TYPE", "BOTTLE");
                                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initProductType", hsmpInitProductType);
                                    RVMMainActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(),
                                            new Object[]{RVMMainActivity.this.getBaseActivity(), Integer.valueOf(2), VENDING_WAY});
                                }
                            } else if (!RVMMainActivity.this.isClick) {
                                Intent intent;
                                RVMMainActivity.this.isClick = true;
                                if (MAIN_CLK_PIC.endsWith("mp4")) {
                                    intent = new Intent(RVMMainActivity.this, AdvertisementVideoActivity.class);
                                } else {
                                    intent = new Intent(RVMMainActivity.this, ActivityPageActivity.class);
                                }
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                RVMMainActivity.this.startActivity(intent);
                            }
                        }
                    } catch (Exception e) {
                        RVMMainActivity.logger.debug("Exception on startActivity for activity", e);
                    }
                }
            });
            this.sbarView = (VerticalMarqueeTextview) findViewById(R.id.mySbarView);
            this.sbarView.setMovementMethod(ScrollingMovementMethod.getInstance());
            this.sbarView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.NOTICE);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    if (RVMMainActivity.this.listAdText.size() > 0 && !RVMMainActivity.this.isClick) {
                        RVMMainActivity.this.isClick = true;
                        Intent intent = new Intent();
                        intent.setClass(RVMMainActivity.this, AnnouncementActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        RVMMainActivity.this.startActivity(intent);
                    }
                }
            });
            this.contentView = (MyGifView) findViewById(R.id.fullscreen_content);
            this.contentView.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    GUIGlobal.clearMap();
                    if (RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST != null && RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.size() > 0 && RVMMainActivity.this.indexOfListMedia < RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.size()) {
                        RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM = (HashMap) RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.get(RVMMainActivity.this.indexOfListMedia);
                        String VENDING_WAY = null;
                        String MAIN_CLK_PIC = null;
                        if (RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM != null) {
                            VENDING_WAY = (String) RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM.get(AllAdvertisement.VENDING_WAY);
                            MAIN_CLK_PIC = (String) RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM.get(AllAdvertisement.CLICK_PICTURE_PATH);
                            RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM.put("RECYCLE_BOTTLE", RVMMainActivity.this.recycleBottle + "");
                        }
                        if (RVMMainActivity.this.TRANSMIT_ADV != null) {
                            RVMMainActivity.this.TRANSMIT_ADV.put("TRANSMIT_ADV", RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM);
                        }
                        GUIGlobal.setCurrentSession(AllAdvertisement.HOMEPAGE_LEFT, RVMMainActivity.this.TRANSMIT_ADV);
                        if (StringUtils.isBlank(MAIN_CLK_PIC)) {
                            if (StringUtils.isBlank(MAIN_CLK_PIC) && !StringUtils.isBlank(VENDING_WAY)) {
                                try {
                                    if (RVMMainActivity.this.recycleBottle) {
                                        RVMMainActivity.this.startRecycleBottle();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (!RVMMainActivity.this.isClick) {
                            RVMMainActivity.this.isClick = true;
                            GUIGlobal.setCurrentSession("isYOUKU", "TRUE");
                            Intent intent = new Intent(RVMMainActivity.this, ActivityPageActivity.class);
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            RVMMainActivity.this.startActivity(intent);
                        }
                    }
                }
            });
            this.myVideoView = (MyVideoView) findViewById(R.id.firstVideo);
            this.myVideoView.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    RVMMainActivity.this.myVideoView.start();
                }
            });
            this.myVideoView.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    synchronized (RVMMainActivity.this.syncOBJ) {
                        RVMMainActivity.this.indexOfListMedia = RVMMainActivity.this.indexOfListMedia + 1;
                        if (RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST == null || RVMMainActivity.this.indexOfListMedia >= RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.size()) {
                            RVMMainActivity.this.indexOfListMedia = 0;
                        }
                    }
                    RVMMainActivity.this.initMediaInfo();
                }
            });
            this.myVideoView.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    try {
                        RVMMainActivity.this.myVideoView.pause();
                        RVMMainActivity.this.myVideoView.setVisibility(View.GONE);
                        RVMMainActivity.this.contentView.setVisibility(View.VISIBLE);
                        return true;
                    } catch (Exception e) {
                        RVMMainActivity.this.myVideoView.setVisibility(View.GONE);
                        RVMMainActivity.this.contentView.setVisibility(View.VISIBLE);
                        RVMMainActivity.logger.debug("ErrorListener VideoView Error", e);
                        return false;
                    }
                }
            });
            this.myVideoView.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() != 0 || !RVMMainActivity.this.myVideoViewIsEnabled) {
                        return false;
                    }
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.MOVIE);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    GUIGlobal.clearMap();
                    RVMMainActivity.this.myVideoViewIsEnabled = false;
                    synchronized (RVMMainActivity.this.syncOBJ) {
                        if (RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST != null && RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.size() > 0 && RVMMainActivity.this.indexOfListMedia < RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.size()) {
                            RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM = (HashMap) RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM_LIST.get(RVMMainActivity.this.indexOfListMedia);
                        }
                    }
                    String videopath = null;
                    if (RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM != null) {
                        RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM.put("RECYCLE_BOTTLE", RVMMainActivity.this.recycleBottle + "");
                        videopath = (String) RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM.get(AllAdvertisement.CLICK_MOVIE_PATH);
                    }
                    if (RVMMainActivity.this.TRANSMIT_ADV != null) {
                        RVMMainActivity.this.TRANSMIT_ADV.put("TRANSMIT_ADV", RVMMainActivity.this.HOMEPAGE_LEFT_BOTTOM);
                    }
                    GUIGlobal.setCurrentSession(AllAdvertisement.HOMEPAGE_LEFT, RVMMainActivity.this.TRANSMIT_ADV);
                    File file = new File(videopath);
                    if (!(StringUtils.isBlank(videopath) || !file.exists() || RVMMainActivity.this.isClick)) {
                        RVMMainActivity.this.isClick = true;
                        Intent intent = new Intent();
                        intent.setClass(RVMMainActivity.this, AdvertisementVideoActivity.class);
                        intent.putExtra("PROGRESS", RVMMainActivity.this.myVideoView.getCurrentPosition());
                        RVMMainActivity.this.startActivity(intent);
                        RVMMainActivity.this.overridePendingTransition(R.anim.scale_big_in, R.anim.scale_big_out);
                    }
                    return true;
                }
            });
            this.btnService = (Button) findViewById(R.id.btnservice);
            this.btnService.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!RVMMainActivity.this.isClick) {
                        RVMMainActivity.this.isClick = true;
                        HashMap map = new HashMap();
                        map.put("KEY", AllClickContent.CONVENIENCE_SERVICE);
                        try {
                            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                        } catch (Exception e) {
                        }
                        GUIGlobal.setCurrentSession("USESTATE", Integer.valueOf(2));
                        Intent intent = new Intent();
                        intent.setClass(RVMMainActivity.this, QueryOneCardActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        RVMMainActivity.this.startActivity(intent);
                    }
                }
            });
            String str = SysConfig.get("COUNTRIES_REGIONS");
            if (StringUtils.isBlank(str)) {
                this.strs[0] = Locale.getDefault().getLanguage();
                this.strs[1] = Locale.getDefault().getCountry();
            } else {
                strs1 = str.split(",");
                if (strs1.length < 2) {
                    this.strs[0] = strs1[0];
                    this.strs[1] = "";
                } else {
                    this.strs = strs1;
                }
            }
            GUIGlobal.updateLanguage(getApplication(), new Locale(this.strs[0], this.strs[1]));
            guiCommonService= CommonServiceHelper.getGUICommonService();
            final Button btnLanguageSwitch = (Button) findViewById(R.id.btnLanguageSwitch);
            btnLanguageSwitch.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RVMMainActivity.this.switchLanguage(btnLanguageSwitch, RVMMainActivity.this.languageList, true);
                }
            });
            String VisibleLanguageButton = SysConfig.get("RVM.LANGUAGE.STATE");
            if (!StringUtils.isBlank(VisibleLanguageButton)) {
                btnLanguageSwitch.setVisibility(View.VISIBLE);
                String[] VisibleLanguageButtons = VisibleLanguageButton.split(",");
                if (VisibleLanguageButtons[0] != null) {
                    saveLanguage(VisibleLanguageButtons[0]);
                }
                for (i = 0; i < VisibleLanguageButtons.length; i++) {
                    String countriesRegions = SysConfig.get("RVM.LANGUAGE." + VisibleLanguageButtons[i]);
                    if (!StringUtils.isBlank(countriesRegions)) {
                        String country;
                        HashMap languageMap = new HashMap();
                        if ("CHINESE".equalsIgnoreCase(VisibleLanguageButtons[i])) {
                            languageMap.put("text", getString(R.string.btnChineseText));
                        }
                        if ("ENGLISH".equalsIgnoreCase(VisibleLanguageButtons[i])) {
                            languageMap.put("text", getString(R.string.btnEnglishText));
                        }
                        if ("SPANISH".equalsIgnoreCase(VisibleLanguageButtons[i])) {
                            languageMap.put("text", getString(R.string.btnSpanishText));
                        }
                        if ("TURKY".equalsIgnoreCase(VisibleLanguageButtons[i])) {
                            languageMap.put("text", getString(R.string.btnTurkyText));
                        }
                        if ("PORTUGUESE".equalsIgnoreCase(VisibleLanguageButtons[i])) {
                            languageMap.put("text", getString(R.string.btnPortugueseText));
                        }
                        if ("DUTCH".equalsIgnoreCase(VisibleLanguageButtons[i])) {
                            languageMap.put("text", getString(R.string.btnDutchText));
                        }
                        if ("HINDI".equalsIgnoreCase(VisibleLanguageButtons[i])) {
                            languageMap.put("text", getString(R.string.btnHindiText));
                        }
                        strs1 = countriesRegions.split(",");
                        String language = strs1[0];
                        if (strs1.length < 2) {
                            country = "";
                        } else {
                            country = strs1[1];
                        }
                        languageMap.put("language", language);
                        languageMap.put("country", country);
                        this.languageList.add(languageMap);
                    }
                }
            }
            switchLanguage(btnLanguageSwitch, this.languageList, false);
//            final GUICommonService gUICommonService = guiCommonService;
            ((Button) findViewById(R.id.btnChinese)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.CHINESE);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    GUIGlobal.updateLanguage(RVMMainActivity.this.getApplication(), Locale.SIMPLIFIED_CHINESE);
                    try {
                        HashMap<String, Object> hashmap = new HashMap();
                        hashmap.put("RVM.LANGUAGE", "CHINESE");
                        guiCommonService.execute("GUIMaintenanceCommonService", "saveLanguage", hashmap);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    RVMMainActivity.this.refreshAD();
                }
            });
//            gUICommonService = guiCommonService;
            ((Button) findViewById(R.id.btnEnglish)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.ENGLISH);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    GUIGlobal.updateLanguage(RVMMainActivity.this.getApplication(), new Locale("en", RVMMainActivity.this.strs[1]));
                    try {
                        HashMap<String, Object> hashmap = new HashMap();
                        hashmap.put("RVM.LANGUAGE", "ENGLISH");
                        guiCommonService.execute("GUIMaintenanceCommonService", "saveLanguage", hashmap);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    RVMMainActivity.this.refreshAD();
                }
            });
//            gUICommonService = guiCommonService;
            ((Button) findViewById(R.id.btnSpanish)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.SPANISH);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    GUIGlobal.updateLanguage(RVMMainActivity.this.getApplication(), new Locale("es", RVMMainActivity.this.strs[1]));
                    try {
                        HashMap<String, Object> hashmap = new HashMap();
                        hashmap.put("RVM.LANGUAGE", "SPANISH");
                        guiCommonService.execute("GUIMaintenanceCommonService", "saveLanguage", hashmap);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    RVMMainActivity.this.refreshAD();
                }
            });
          //  gUICommonService = guiCommonService;
            ((Button) findViewById(R.id.btnTurky)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.TURKY);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    GUIGlobal.updateLanguage(RVMMainActivity.this.getApplication(), new Locale("tr", RVMMainActivity.this.strs[1]));
                    try {
                        HashMap<String, Object> hashmap = new HashMap();
                        hashmap.put("RVM.LANGUAGE", "TURKY");
                        guiCommonService.execute("GUIMaintenanceCommonService", "saveLanguage", hashmap);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    RVMMainActivity.this.refreshAD();
                }
            });
            //gUICommonService = guiCommonService;
            ((Button) findViewById(R.id.btnPortuguese)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.PORTUGUESE);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    GUIGlobal.updateLanguage(RVMMainActivity.this.getApplication(), new Locale("pt", RVMMainActivity.this.strs[1]));
                    try {
                        HashMap<String, Object> hashmap = new HashMap();
                        hashmap.put("RVM.LANGUAGE", "PORTUGUESE");
                        guiCommonService.execute("GUIMaintenanceCommonService", "saveLanguage", hashmap);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    RVMMainActivity.this.refreshAD();
                }
            });
            //gUICommonService = guiCommonService;
            ((Button) findViewById(R.id.btnDutch)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.DUTCH);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    GUIGlobal.updateLanguage(RVMMainActivity.this.getApplication(), new Locale("nl", RVMMainActivity.this.strs[1]));
                    try {
                        HashMap<String, Object> hashmap = new HashMap();
                        hashmap.put("RVM.LANGUAGE", "DUTCH");
                        guiCommonService.execute("GUIMaintenanceCommonService", "saveLanguage", hashmap);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    RVMMainActivity.this.refreshAD();
                }
            });
           // gUICommonService = guiCommonService;
            ((Button) findViewById(R.id.btnHindi)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.HINDI);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    GUIGlobal.updateLanguage(RVMMainActivity.this.getApplication(), new Locale("hi", RVMMainActivity.this.strs[1]));
                    try {
                        HashMap<String, Object> hashmap = new HashMap();
                        hashmap.put("RVM.LANGUAGE", "HINDI");
                        guiCommonService.execute("GUIMaintenanceCommonService", "saveLanguage", hashmap);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    RVMMainActivity.this.refreshAD();
                }
            });
            this.channel = (Button) findViewById(R.id.maintainGard);
            this.channel.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (!RVMMainActivity.this.isClick) {
                        RVMMainActivity.this.isClick = true;
                        HashMap map = new HashMap();
                        map.put("KEY", AllClickContent.CHANNAL);
                        try {
                            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                        } catch (Exception e) {
                        }
                        Intent intent = new Intent();
                        intent.setClass(RVMMainActivity.this, ChannelActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        RVMMainActivity.this.startActivity(intent);
                    }
                }
            });
            ((Button) findViewById(R.id.maintainGardBtn)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (!RVMMainActivity.this.isClick) {
                        RVMMainActivity.this.isClick = true;
                        Intent intent = new Intent();
                        intent.setClass(RVMMainActivity.this, ChannelActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        RVMMainActivity.this.startActivity(intent);
                    }
                }
            });
            ((Button) findViewById(R.id.reminder)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!RVMMainActivity.this.isClick) {
                        RVMMainActivity.this.isClick = true;
                        HashMap map = new HashMap();
                        map.put("KEY", AllClickContent.THROWBOTTLES);
                        try {
                            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                        } catch (Exception e) {
                        }
                        try {
                            String jsonDisableReason = RVMMainActivity.this.jsonDisableReason;
                            if (RVMMainActivity.this.recycleBottle) {
                                RVMMainActivity.this.startRecycleBottle();
                                return;
                            }
                            List<String> listDisableReason = JSONUtils.toList(jsonDisableReason);
                            Intent intent = new Intent(RVMMainActivity.this, FaultListActivity.class);
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("STATE", (ArrayList) listDisableReason);
                            intent.putExtras(bundle);
                            RVMMainActivity.this.startActivity(intent);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            });
            Button btnThrowPaper = (Button) findViewById(R.id.throwPaper);
            btnThrowPaper.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!RVMMainActivity.this.isClick) {
                        RVMMainActivity.this.isClick = true;
                        HashMap map = new HashMap();
                        map.put("KEY", AllClickContent.THROWPAPER);
                        try {
                            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                        } catch (Exception e) {
                        }
                        RVMMainActivity.this.startRecyclePaper();
                    }
                }
            });
            this.aboutUsBtn = (Button) findViewById(R.id.aboutUs);
            this.aboutUsBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (!RVMMainActivity.this.isClick) {
                        RVMMainActivity.this.isClick = true;
                        HashMap map = new HashMap();
                        map.put("KEY", AllClickContent.ABOUTUS);
                        try {
                            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                        } catch (Exception e) {
                        }
                        Intent intent = new Intent();
                        intent.setClass(RVMMainActivity.this, AboutUsActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        RVMMainActivity.this.startActivity(intent);
                    }
                }
            });
            String[] productTypes = SysConfig.get("RECYCLE.SERVICE.SET").split(",");
            for (Object equals : productTypes) {
                if ("PAPER".equals(equals)) {
                    btnThrowPaper.setVisibility(View.VISIBLE);
                }
            }
            refreshAD();
        } catch (Throwable e) {
            logger.debug("Exception on RVMMainActivity onCreate", e);
        }
    }

    private void refreshAD() {
        this.adconfURL = SysConfig.get("AD.URL.PREFIX") + SysConfig.get("RVM.LANGUAGE") + SysConfig.get("AD.URL");
        GUIGlobal.setCurrentSession("AD_CONF_URL", this.adconfURL);
        initMediaInfo();
        portalToActivity();
        initCenterTopButton();
    }

    public void enableRecycleBottle(List<String> listBottleDisableReason) {
        if (listBottleDisableReason != null && listBottleDisableReason.size() > 0) {
            this.isOutOfServiceBottle = true;
        }
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("PRODUCT_TYPE", "BOTTLE");
        try {
            this.isMaxBottle = ((Boolean) CommonServiceHelper.getGUICommonService().
                    execute("GUIQueryCommonService", "queryIsStorageMax", hashMap).get("IS_MAX_COUNT")).booleanValue();
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
        this.recycleBottle = listBottleDisableReason.size() == 0;
        Button btnReminderBottles = (Button) findViewById(R.id.reminder);
        if (this.recycleBottle) {
            this.jsonDisableReason = null;
            btnReminderBottles.setText("");
            btnReminderBottles.setBackgroundResource(R.drawable.touping_btn);
            this.jiantouImage = (MyGifView) findViewById(R.id.jiantou_text);
            this.jiantouImage.setVisibility(View.VISIBLE);
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
            if (this.isMaxBottle) {
                btnReminderBottles.setText(Html.fromHtml("<font color=\"#ff0000\">" + getString(R.string.ReachMaxOfBottle) + "</font>"));
                btnReminderBottles.setBackgroundResource(R.drawable.btn_yikatongfuwu_unable_03);
                this.jiantouImage = (MyGifView) findViewById(R.id.jiantou_text);
                this.jiantouImage.setVisibility(View.GONE);
            } else if (isUpdated) {
                String OutOfService;
                if (this.isOutOfServiceBottle) {
                    OutOfService = "<font color=\"#ff0000\">" + getString(R.string.outOfService) + "</font>";
                } else {
                    OutOfService = "<font color=\"#ff0000\">" + getString(R.string.btnthrowBottleOutOfService) + "</font>";
                }
                btnReminderBottles.setText(Html.fromHtml(OutOfService));
                btnReminderBottles.setBackgroundResource(R.drawable.btn_yikatongfuwu_unable_03);
                this.jiantouImage = (MyGifView) findViewById(R.id.jiantou_text);
                this.jiantouImage.setVisibility(View.GONE);
            } else {
                btnReminderBottles.setText(Html.fromHtml("<font color=\"#ff0000\">" + getString(R.string.mian_activity_updating) + "</font>"));
                btnReminderBottles.setBackgroundResource(R.drawable.btn_yikatongfuwu_unable_03);
                this.jiantouImage = (MyGifView) findViewById(R.id.jiantou_text);
                this.jiantouImage.setVisibility(View.GONE);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void enableRecyclePaper(List<String> listPaperDisableReason) {
        if (listPaperDisableReason != null && listPaperDisableReason.size() > 0) {
            this.isOutOfServicePaper = true;
        }
        Button btnThrowPaper = (Button) findViewById(R.id.throwPaper);
        try {
            boolean z;
            HashMap<String, Object> hashMap = new HashMap();
            hashMap.put("PRODUCT_TYPE", "PAPER");
            HashMap<String, Object> isStorageMax = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryIsStorageMax", hashMap);
            btnThrowPaper.setBackgroundResource(R.drawable.touzhi_btn_hui);
            this.isMaxPaper = ((Boolean) isStorageMax.get("IS_MAX_PAPER")).booleanValue();
            if (this.isMaxPaper) {
                listPaperDisableReason.add("STORAGE_MAX_PAPER");
            }
            if ("TRUE".equalsIgnoreCase(SysConfig.get("STATE:PLC_ERROR"))) {
                listPaperDisableReason.add("PLC_ERROR");
            }
            if (listPaperDisableReason.size() == 0) {
                z = true;
            } else {
                z = false;
            }
            this.recyclepaper = z;
            btnThrowPaper.setEnabled(this.recyclepaper);
            if (this.recyclepaper) {
                btnThrowPaper.setText(R.string.btnthrowPaper);
                btnThrowPaper.setBackgroundResource(R.drawable.touzhi_btn_big);
            } else if (this.isMaxPaper) {
                btnThrowPaper.setText(Html.fromHtml("<font color=\"#ff0000\">" + getString(R.string.ReachMaxOfPaper) + "</font>"));
                btnThrowPaper.setBackgroundResource(R.drawable.btn_touzhi_un);
                btnThrowPaper.setClickable(false);
            } else {
                String OutOfService;
                if (this.isOutOfServicePaper) {
                    OutOfService = "<font color=\"#ff0000\">" + getString(R.string.outOfService) + "</font>";
                } else {
                    OutOfService = "<font color=\"#ff0000\">" + getString(R.string.btnthrowBottleOutOfService) + "</font>";
                }
                btnThrowPaper.setText(Html.fromHtml(OutOfService));
                btnThrowPaper.setBackgroundResource(R.drawable.btn_touzhi_un);
                btnThrowPaper.setClickable(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLanguage() {
        String OutOfService;
        Button btn = (Button) findViewById(R.id.reminder);
        if (this.recycleBottle) {
            btn.setText("");
            btn.setBackgroundResource(R.drawable.touping_btn);
            this.jiantouImage = (MyGifView) findViewById(R.id.jiantou_text);
            this.jiantouImage.setVisibility(View.VISIBLE);
        } else if (this.isMaxBottle) {
            OutOfService = "<font color=\"#ff0000\">" + getString(R.string.ReachMaxOfBottle) + "</font>";
            this.jiantouImage = (MyGifView) findViewById(R.id.jiantou_text);
            this.jiantouImage.setVisibility(View.GONE);
            btn.setBackgroundResource(R.drawable.btn_yikatongfuwu_unable_03);
            btn.setText(Html.fromHtml(OutOfService));
        } else {
            if (this.isOutOfServiceBottle) {
                OutOfService = "<font color=\"#ff0000\">" + getString(R.string.outOfService) + "</font>";
            } else {
                OutOfService = "<font color=\"#ff0000\">" + getString(R.string.btnthrowBottleOutOfService) + "</font>";
            }
            this.jiantouImage = (MyGifView) findViewById(R.id.jiantou_text);
            this.jiantouImage.setVisibility(View.GONE);
            btn.setBackgroundResource(R.drawable.btn_yikatongfuwu_unable_03);
            btn.setText(Html.fromHtml(OutOfService));
        }
        Button btnThrowPaper = (Button) findViewById(R.id.throwPaper);
        if (this.recyclepaper) {
            btnThrowPaper.setText(R.string.btnthrowPaper);
            btnThrowPaper.setBackgroundResource(R.drawable.touzhi_btn_big);
        } else if (this.isMaxPaper) {
            OutOfService = "<font color=\"#ff0000\">" + getString(R.string.ReachMaxOfPaper) + "</font>";
            btnThrowPaper.setBackgroundResource(R.drawable.touzhi_btn_hui);
            btnThrowPaper.setText(Html.fromHtml(OutOfService));
        } else {
            if (this.isOutOfServicePaper) {
                OutOfService = "<font color=\"#ff0000\">" + getString(R.string.outOfService) + "</font>";
            } else {
                OutOfService = "<font color=\"#ff0000\">" + getString(R.string.btnthrowBottleOutOfService) + "</font>";
            }
            btnThrowPaper.setBackgroundResource(R.drawable.touzhi_btn_hui);
            btnThrowPaper.setText(Html.fromHtml(OutOfService));
        }
        ((TextView) findViewById(R.id.rvm_code)).setText(R.string.setupCfgTimerNumText);
        Button channel = (Button) findViewById(R.id.maintainGard);
        if (channel != null) {
            channel.setText(R.string.maintainGard);
        }
        Button mainTainGardBtn = (Button) findViewById(R.id.maintainGardBtn);
        if (mainTainGardBtn != null) {
            mainTainGardBtn.setText(R.string.maintainGard);
        }
        ((Button) findViewById(R.id.aboutUs)).setText(R.string.aboutUs);
        Button btnservice = (Button) findViewById(R.id.btnservice);
        if (btnservice != null) {
            btnservice.setText(R.string.btnConvenienceServices);
        }
        Button weixinButton = (Button) findViewById(R.id.weixinButton);
        if (weixinButton != null) {
            weixinButton.setText(R.string.follow_us);
        }
        ((TextView) findViewById(R.id.sbar_view_title)).setText(R.string.notice);
        if (this.sbarView != null && this.listAdText.size() < 1) {
            this.sbarView.setText(R.string.welcome);
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
            Intent intent = new Intent(this, Class.forName(SysConfig.get("PutBottleActivity.class")));
            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    private void startRecyclePaper() {
        Button btnThrowPaper = (Button) findViewById(R.id.throwPaper);
        if (btnThrowPaper != null && btnThrowPaper.isEnabled()) {
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
            Intent intent = new Intent(this, SelectRecycleActivity.class);
            intent.putExtra("RECYCLE", "RECYCLEPAPER");
            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
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
                initView();
            }
            if ("SCROLL_BAR_ISSUED".equalsIgnoreCase(CMD)) {
                loadSubList();
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
                executeGUIAction(false, new GUIActionTakePhoto(), new Object[]{this});
            }
        }
    }

    private void initMediaInfo() {
        synchronized (this.syncOBJ) {
            this.HOMEPAGE_LEFT_BOTTOM_LIST = BroadcastTaskCommonService.getHomePageLeftBottomList(this.adconfURL);
            if (this.HOMEPAGE_LEFT_BOTTOM_LIST == null || this.HOMEPAGE_LEFT_BOTTOM_LIST.size() <= 0) {
                this.contentView.updateResource(R.drawable.home, null);
                TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForNextMedia, Integer.valueOf(SysConfig.get("DEFAULT.AD.TIME")).intValue(), false);
                TimeoutTask.getTimeoutTask().reset(this.timeoutActionForNextMedia, Integer.valueOf(SysConfig.get("DEFAULT.AD.TIME")).intValue());
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForNextMedia, true);
            } else {
                if (this.indexOfListMedia >= this.HOMEPAGE_LEFT_BOTTOM_LIST.size()) {
                    this.indexOfListMedia = 0;
                }
                String MEDIA_TYPE = (String) ((HashMap) this.HOMEPAGE_LEFT_BOTTOM_LIST.get(this.indexOfListMedia)).get(AllAdvertisement.MEDIA_TYPE);
                Message message;
                if (AllAdvertisement.MEDIA_TYPE_MOVIE.equalsIgnoreCase(MEDIA_TYPE)) {
                    message = new Message();
                    message.what = 1;
                    this.mhandler.sendMessage(message);
                } else if ("PICTURE".equalsIgnoreCase(MEDIA_TYPE)) {
                    message = new Message();
                    message.what = 3;
                    this.mhandler.sendMessage(message);
                }
            }
        }
    }

    private void play() {
        synchronized (this.syncOBJ) {
            if (this.HOMEPAGE_LEFT_BOTTOM_LIST != null && this.HOMEPAGE_LEFT_BOTTOM_LIST.size() > 0 && this.indexOfListMedia < this.HOMEPAGE_LEFT_BOTTOM_LIST.size()) {
                String path = (String) ((HashMap) this.HOMEPAGE_LEFT_BOTTOM_LIST.get(this.indexOfListMedia)).get(AllAdvertisement.MAIN_MOVIE_PATH);
                try {
                    if (this.myVideoView == null || StringUtils.isBlank(path) || !new File(path).isFile()) {
                        this.indexOfListMedia++;
                        initMediaInfo();
                    } else {
                        this.contentView.setVisibility(View.GONE);
                        this.myVideoView.setVisibility(View.VISIBLE);
                        this.myVideoView.setVideoPath(path);
                    }
                } catch (Exception e) {
                    logger.debug("play() VideoView Error", e);
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadSubList() {
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        boolean isChanged = false;
        try {
            List<TextAdEntity> list = new ArrayList();
            synchronized (this.syncObjOfAD) {
                int i;
                HashMap<Integer, TextAdEntity> hsmpTextAdEntity = new HashMap();
                HashMap<String, Object> hsmpResult = guiCommonService.execute("GUIQueryCommonService", "queryScrollText", null);
                if (hsmpResult != null) {
                    List<HashMap<String, String>> listTextAdInfo = new ArrayList();
                    listTextAdInfo = (List) hsmpResult.get("RVM_TEXT_AD_LIST");
                    int num = listTextAdInfo.size();
                    if (listTextAdInfo != null && num > 0) {
                        for (i = 0; i < num; i++) {
                            HashMap<String, String> hsmpTextAd = (HashMap) listTextAdInfo.get(i);
                            int SBAR_ID = Integer.parseInt((String) hsmpTextAd.get("SBAR_ID"));
                            hsmpTextAdEntity.put(Integer.valueOf(SBAR_ID), new TextAdEntity(SBAR_ID, (String) hsmpTextAd.get("SBAR_TEXT"), (String) hsmpTextAd.get("BEGIN_TIME"), (String) hsmpTextAd.get("END_TIME"), SysConfig.get("RVM.SCROLLTEXT.COLOR")));
                        }
                    }
                }
                if (this.listAdText != null && this.listAdText.size() > 0) {
                    for (i = 0; i < this.listAdText.size(); i++) {
                        TextAdEntity textAdEntity = (TextAdEntity) this.listAdText.get(i);
                        if (hsmpTextAdEntity.get(Integer.valueOf(textAdEntity.getSbarId())) != null) {
                            list.add(textAdEntity);
                            hsmpTextAdEntity.remove(Integer.valueOf(textAdEntity.getSbarId()));
                        } else {
                            isChanged = true;
                        }
                    }
                }
                for (Integer intValue : hsmpTextAdEntity.keySet()) {
                    list.add(hsmpTextAdEntity.get(Integer.valueOf(intValue.intValue())));
                    isChanged = true;
                }
            }
            if (isChanged) {
                String text = "";
                synchronized (this.listAdText) {
                    this.listAdText.clear();
                    this.listAdText.addAll(list);
                    if (this.listAdText.size() > 0) {
                        text = ((TextAdEntity) this.listAdText.get(0)).getSbarTxt();
                    }
                }
                this.sbarView.setText(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onPause() {
        super.onPause();
        SysConfig.set("UPDATE_ENABLE", "FALSE");
        executeGUIAction(false, new GUIActionSysAction(), new Object[]{this, "sysUpdate"});
        if (!isFinishing() && this.myVideoView != null) {
            this.myVideoView.pause();
        }
    }

    protected void onStop() {
        super.onStop();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForAdText, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutActionForAdText);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForShowNextAdText, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutActionForShowNextAdText);
        TickTaskThread.getTickTaskThread().unregister(this.timeoutActionForShutdwon);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionCheckDB, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutActionCheckDB);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForSetDisable, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutActionForSetDisable);
        if (this.isPlaySounds && audioCurrentState.CHECKED.equalsIgnoreCase(this.CURRENT_STATE)) {
            TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForCheckAudio, false);
            TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutActionForCheckAudio);
        }
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForNextActivity, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutActionForNextActivity);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForNextMedia, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutActionForNextMedia);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction2Video, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction2Video);
        this.activityImage.updateResource(-1, null);
    }

    private void initView() {
        Button btnService = (Button) findViewById(R.id.btnservice);
        Button maintainGardBtn = (Button) findViewById(R.id.maintainGardBtn);
        Button channel = (Button) findViewById(R.id.maintainGard);
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
            btnService.setVisibility(View.GONE);
            LayoutParams imgParams = new LayoutParams(160, 162);
            imgParams.setMargins(10, 0, 0, 0);
            this.aboutUsBtn.setLayoutParams(imgParams);
            maintainGardBtn.setVisibility(View.VISIBLE);
            channel.setVisibility(View.GONE);
            return;
        }
        if (SysConfig.get(MediaInfo.CONVENIENCE_ACTIVITY).equalsIgnoreCase("TRUE") && str.equalsIgnoreCase("CONSERVICE")) {
            String[] strSet = str.split(";");
            for (String add2 : strSet) {
                if (add2.equalsIgnoreCase("CONSERVICE")) {
                    btnService.setVisibility(View.VISIBLE);
                }
                maintainGardBtn.setVisibility(View.GONE);
                channel.setVisibility(View.VISIBLE);
            }
            if (listDisabledService.contains("CONSERVICE")) {
                btnService.setVisibility(View.GONE);
                LayoutParams imgParams = new LayoutParams(160, 162);
                imgParams.setMargins(10, 0, 0, 0);
                this.aboutUsBtn.setLayoutParams(imgParams);
                maintainGardBtn.setVisibility(View.VISIBLE);
                channel.setVisibility(View.GONE);
            } else {
                btnService.setVisibility(View.VISIBLE);
                maintainGardBtn.setVisibility(View.GONE);
                channel.setVisibility(View.VISIBLE);
            }
        } else {
            btnService.setVisibility(View.GONE);
            LayoutParams imgParams = new LayoutParams(160, 162);
            imgParams.setMargins(10, 0, 0, 0);
            this.aboutUsBtn.setLayoutParams(imgParams);
            maintainGardBtn.setVisibility(View.VISIBLE);
            channel.setVisibility(View.GONE);
        }
        if ((listDisabledService.contains(ServiceName.PRINTER) || "FALSE".equalsIgnoreCase(SysConfig.get("SET.PRINT.ENABLE"))) && !"FALSE".equalsIgnoreCase(SysConfig.get("SET.PRINT.ENABLE"))) {
            Properties externalProp1 = new Properties();
            externalProp1.put("SET.PRINT.ENABLE", "FALSE");
            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp1);
            SysConfig.set(externalProp1);
            RVMShell.backupExternalConfig();
        }
    }

    private void portalToActivity() {
        synchronized (this.syncObjOfActivity) {
            this.HOMEPAGE_LEFT_TOP_LIST = BroadcastTaskCommonService.getHomePageLeftTopList(this.adconfURL);
            if (this.HOMEPAGE_LEFT_TOP_LIST == null || this.HOMEPAGE_LEFT_TOP_LIST.size() <= 0) {
                this.activityImage.updateResource(R.drawable.pic, null);
                TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForNextActivity, Integer.valueOf(SysConfig.get("DEFAULT.AD.TIME")).intValue(), false);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForNextActivity, true);
                TimeoutTask.getTimeoutTask().reset(this.timeoutActionForNextActivity, Integer.valueOf(SysConfig.get("DEFAULT.AD.TIME")).intValue());
            } else {
                int play_time;
                if (this.indexOfListActivity >= this.HOMEPAGE_LEFT_TOP_LIST.size()) {
                    this.indexOfListActivity = 0;
                }
                String PICTURE_TOP_URL = (String) ((HashMap) this.HOMEPAGE_LEFT_TOP_LIST.get(this.indexOfListActivity)).get(AllAdvertisement.MAIN_PICTURE_PATH);
                if (!StringUtils.isBlank(PICTURE_TOP_URL) && new File(PICTURE_TOP_URL).isFile()) {
                    this.activityImage.updateResource(-1, PICTURE_TOP_URL);
                }
                try {
                    play_time = Integer.valueOf((String) ((HashMap) this.HOMEPAGE_LEFT_TOP_LIST.get(this.indexOfListActivity)).get(AllAdvertisement.PLAY_SECONDS)).intValue();
                } catch (Exception e) {
                    play_time = Integer.valueOf(SysConfig.get("DEFAULT.AD.TIME")).intValue();
                }
                TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForNextActivity, play_time, false);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForNextActivity, true);
                TimeoutTask.getTimeoutTask().reset(this.timeoutActionForNextActivity, play_time);
            }
        }
    }

    private void initCenterTopButton() {
        synchronized (this.syncObjOfCenterTopButton) {
            MyGifView myGifView = (MyGifView) findViewById(R.id.myQRcodeImage);
            this.HOMEPAGE_CENTER_TOP_LIST = BroadcastTaskCommonService.getHomePageCenterTopList(this.adconfURL);
            if (this.HOMEPAGE_CENTER_TOP_LIST == null || this.HOMEPAGE_CENTER_TOP_LIST.size() <= 0) {
                myGifView.updateResource(R.drawable.attention, null);
                TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForNextCenterTopButton, Integer.valueOf(SysConfig.get("DEFAULT.AD.TIME")).intValue(), false);
                TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForNextCenterTopButton, true);
                TimeoutTask.getTimeoutTask().reset(this.timeoutActionForNextCenterTopButton, Integer.valueOf(SysConfig.get("DEFAULT.AD.TIME")).intValue());
            } else {
                if (this.indexOfListCenterTop >= this.HOMEPAGE_CENTER_TOP_LIST.size()) {
                    this.indexOfListCenterTop = 0;
                }
                String PICTURE_CENTER_TOP_URL = (String) ((HashMap) this.HOMEPAGE_CENTER_TOP_LIST.get(this.indexOfListCenterTop)).get(AllAdvertisement.MAIN_PICTURE_PATH);
                if (!StringUtils.isBlank(PICTURE_CENTER_TOP_URL) && new File(PICTURE_CENTER_TOP_URL).isFile()) {
                    myGifView.updateResource(-1, PICTURE_CENTER_TOP_URL);
                }
                final String PICTURE_CENTER_TOP_TEXT = (String) ((HashMap) this.HOMEPAGE_CENTER_TOP_LIST.get(this.indexOfListCenterTop)).get(AllAdvertisement.MAIN_CLK_DESC);
                final TextView followUsMoreText = (TextView) findViewById(R.id.follow_us_more_text);
                followUsMoreText.post(new Runnable() {
                    public void run() {
                        followUsMoreText.setText(PICTURE_CENTER_TOP_TEXT);
                    }
                });
                if (this.HOMEPAGE_CENTER_TOP_LIST != null && this.HOMEPAGE_CENTER_TOP_LIST.size() >= 1) {
                    int play_time;
                    try {
                        play_time = Integer.valueOf((String) ((HashMap) this.HOMEPAGE_CENTER_TOP_LIST.get(this.indexOfListCenterTop)).get(AllAdvertisement.PLAY_SECONDS)).intValue();
                    } catch (Exception e) {
                        play_time = Integer.valueOf(SysConfig.get("DEFAULT.AD.TIME")).intValue();
                    }
                    TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutActionForNextCenterTopButton, play_time, false);
                    TimeoutTask.getTimeoutTask().setEnabled(this.timeoutActionForNextCenterTopButton, true);
                    TimeoutTask.getTimeoutTask().reset(this.timeoutActionForNextCenterTopButton, play_time);
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
            if (this.isCorrectYear) {
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
            executeGUIAction(false, new GUIActionInit(), new Object[]{this});
        }
    }

    public void saveLanguage(String language) {
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        HashMap<String, Object> hashmap = new HashMap();
        hashmap.put("RVM.LANGUAGE", language);
        try {
            guiCommonService.execute("GUIMaintenanceCommonService", "saveLanguage", hashmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkDatabase() {
        logger.debug("Starting check:DBException...");
        try {
            HashMap<String, Object> hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "checkDB", null);
            if (hsmpResult != null) {
                String checkStatus = (String) hsmpResult.get("RESULT");
                if (checkStatus == null) {
                    return;
                }
                if (NetworkUtils.NET_STATE_FAILED.equalsIgnoreCase(checkStatus) || "checkFailed".equalsIgnoreCase(checkStatus)) {
                    logger.debug("First:DBException,chmod /database");
                    try {
                        List listCmd;
                        String cmd;
                        String chmodDB_shell = SysConfig.get("CHMODDB.SHELL");
                        if (!StringUtils.isBlank(chmodDB_shell)) {
                            InputStream is = getClass().getClassLoader().getResourceAsStream(chmodDB_shell);
                            listCmd = new ArrayList();
                            DataInputStream dataIS = new DataInputStream(is);
                            while (true) {
                                cmd = dataIS.readLine();
                                if (cmd == null) {
                                    break;
                                }
                                cmd = cmd.trim();
                                if (!(cmd.startsWith("#") || cmd.length() == 0)) {
                                    listCmd.add(cmd);
                                }
                            }
                            is.close();
                            if (listCmd.size() > 0) {
                                ShellUtils.shell(listCmd);
                            }
                        }
                        hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "checkDB", null);
                        if (hsmpResult != null) {
                            checkStatus = (String) hsmpResult.get("RESULT");
                            if (NetworkUtils.NET_STATE_FAILED.equalsIgnoreCase(checkStatus) || "checkFailed".equalsIgnoreCase(checkStatus)) {
                                logger.debug("Second:DBException,delete /database");
                                try {
                                    String deleteDB_shell = SysConfig.get("DELETEDB.SHELL");
                                    if (!StringUtils.isBlank(deleteDB_shell)) {
                                        InputStream delis = getClass().getClassLoader().getResourceAsStream(deleteDB_shell);
                                        listCmd = new ArrayList();
                                        DataInputStream delIS = new DataInputStream(delis);
                                        while (true) {
                                            cmd = delIS.readLine();
                                            if (cmd == null) {
                                                break;
                                            }
                                            cmd = cmd.trim();
                                            if (!(cmd.startsWith("#") || cmd.length() == 0)) {
                                                listCmd.add(cmd);
                                            }
                                        }
                                        delis.close();
                                        if (listCmd.size() > 0) {
                                            ShellUtils.shell(listCmd);
                                        }
                                        GUIGlobal.exit();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        } catch (Exception e22) {
            e22.printStackTrace();
        }
    }

    public void ScreenIntent() {
        this.HOMEPAGE_SCREEN_LIST = BroadcastTaskCommonService.getHomePageLeftBottomVideoList(this.adconfURL);
        if (this.HOMEPAGE_SCREEN_LIST != null && this.HOMEPAGE_SCREEN_LIST.size() > 0) {
            List listpath = new ArrayList();
            for (int i = 0; i < this.HOMEPAGE_SCREEN_LIST.size(); i++) {
                this.moviepath = (String) ((HashMap) this.HOMEPAGE_SCREEN_LIST.get(i)).get(AllAdvertisement.MAIN_MOVIE_PATH);
                if (!StringUtils.isBlank(this.moviepath)) {
                    File movieFile = new File(this.moviepath);
                    if (movieFile != null && movieFile.isFile()) {
                        listpath.add(this.moviepath);
                    }
                }
            }
            if (listpath != null && listpath.size() > 0) {
                if (new File((String) listpath.get(0)).isFile()) {
                    int screenSaveTime;
                    String screenSaveTimeStr = SysConfig.get("SCREENSAVETIME");
                    if (StringUtils.isBlank(screenSaveTimeStr)) {
                        screenSaveTime = FTPCodes.SERVICE_NOT_READY;
                    } else {
                        screenSaveTime = Integer.parseInt(screenSaveTimeStr);
                    }
                    TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction2Video, screenSaveTime, false);
                    TimeoutTask.getTimeoutTask().reset(this.timeoutAction2Video);
                    TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction2Video, true);
                    return;
                }
                Log.v("TAG", "meitiao");
            }
        }
    }

    private void switchLanguage(Button btn, List<HashMap<String, String>> list, boolean updateLanguage) {
        String localLanguage = Locale.getDefault().getLanguage();
        if (list != null && list.size() >= 1) {
            int i = 0;
            while (i < list.size()) {
                HashMap map = (HashMap) list.get(i);
                if (map == null || map.size() <= 0 || !localLanguage.equalsIgnoreCase((String) map.get("language"))) {
                    i++;
                } else {
                    i++;
                    if (i >= list.size()) {
                        i = 0;
                    }
                    HashMap<String, String> nextMap = (HashMap) list.get(i);
                    if (nextMap != null && nextMap.size() > 0) {
                        if (updateLanguage) {
                            GUIGlobal.updateLanguage(getApplication(), new Locale((String) nextMap.get("language"), (String) nextMap.get("country")));
                            i++;
                            if (i >= list.size()) {
                                i = 0;
                            }
                            btn.setText((CharSequence) ((HashMap) list.get(i)).get("text"));
                            return;
                        }
                        btn.setText((CharSequence) nextMap.get("text"));
                        return;
                    }
                    return;
                }
            }
        }
    }
}
