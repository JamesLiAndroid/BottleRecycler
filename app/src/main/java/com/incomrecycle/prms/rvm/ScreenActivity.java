package com.incomrecycle.prms.rvm;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.ServiceName;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.BroadcastTaskCommonService;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionSysAction;
import com.incomrecycle.prms.rvm.gui.action.GUIActionTakePhoto;
import com.incomrecycle.prms.rvm.gui.activity.view.MyGifView;
import com.incomrecycle.prms.rvm.gui.activity.view.MyVideoView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class ScreenActivity extends BaseActivity {
    boolean finishScreenCalled = false;
    private boolean hasFinish = false;
    private int indexOfListMedia = 0;
    private OnTouchListener newViewOnTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == 0 && !ScreenActivity.this.hasFinish) {
                ScreenActivity.this.hasFinish = true;
                ScreenActivity.this.finishScreen();
            }
            return true;
        }
    };
    private boolean recycleBottle = true;
    private MyGifView screen_myGifView;
    private MyVideoView screen_myVideoView;
    Object syncOBJ = new Object();
    private List<String> videoList = new ArrayList();

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
        if ("CMD".equalsIgnoreCase((String) hsmpEvent.get("EVENT"))) {
            String CMD = (String) hsmpEvent.get("CMD");
            if (ServiceName.TAKE_PHOTO.equalsIgnoreCase(CMD)) {
                executeGUIAction(false, new GUIActionTakePhoto(), new Object[]{this});
            }
            if ("REQUEST_RECYCLE".equalsIgnoreCase(CMD)) {
                try {
                    if (this.recycleBottle) {
                        startRecycleBottle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_screen_play);
        initView();
        setNewViewClickListener();
        initMediaInfo();
    }

    private void initView() {
        this.screen_myVideoView = (MyVideoView) findViewById(R.id.screen_myVideoView);
        this.screen_myGifView = (MyGifView) findViewById(R.id.screen_myGifView);
        this.recycleBottle = getIntent().getBooleanExtra("recycleBottle", true);
        if (this.recycleBottle) {
            this.adconfURL = SysConfig.get("AD.URL.PREFIX") + SysConfig.get("RVM.LANGUAGE") + SysConfig.get("AD.URL");
            List<HashMap<String, String>> homeRightImage = new ArrayList();
            homeRightImage = BroadcastTaskCommonService.getHomePageRightTopList(this.adconfURL);
            if (homeRightImage != null && homeRightImage.size() > 0) {
                String icon = (String) ((HashMap) homeRightImage.get(0)).get(AllAdvertisement.MAIN_PICTURE_PATH);
                if (!StringUtils.isBlank(icon) && new File(icon).isFile()) {
                    this.screen_myGifView.updateResource(-1, icon);
                    return;
                }
                return;
            }
            return;
        }
        this.screen_myGifView.setVisibility(View.GONE);
    }

    private void playVideo() {
        if (this.videoList == null || this.videoList.size() <= 0) {
            finishScreen();
        } else if (this.indexOfListMedia < this.videoList.size()) {
            String path = (String) this.videoList.get(this.indexOfListMedia);
            try {
                if (!StringUtils.isBlank(path) && new File(path).isFile() && this.screen_myVideoView != null && !StringUtils.isBlank(path)) {
                    this.screen_myVideoView.setVideoPath(path);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            initMediaInfo();
        }
    }

    private void setNewViewClickListener() {
        this.screen_myVideoView.setOnTouchListener(this.newViewOnTouchListener);
        this.screen_myVideoView.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                ScreenActivity.this.screen_myVideoView.start();
            }
        });
        this.screen_myVideoView.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                synchronized (ScreenActivity.this.syncOBJ) {
                    ScreenActivity.this.indexOfListMedia = ScreenActivity.this.indexOfListMedia + 1;
                    if (ScreenActivity.this.videoList == null || ScreenActivity.this.indexOfListMedia >= ScreenActivity.this.videoList.size()) {
                        ScreenActivity.this.indexOfListMedia = 0;
                    }
                    ScreenActivity.this.initMediaInfo();
                }
            }
        });
        this.screen_myVideoView.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                try {
                    ScreenActivity.this.screen_myVideoView.pause();
                    ScreenActivity.this.finishScreen();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
    }

    private void initMediaInfo() {
        if (!this.hasFinish) {
            synchronized (this.syncOBJ) {
                this.videoList.clear();
                this.adconfURL = SysConfig.get("AD.URL.PREFIX") + SysConfig.get("RVM.LANGUAGE") + SysConfig.get("AD.URL");
                List<HashMap<String, String>> HOMEPAGE_LEFT_BOTTOM_LIST = BroadcastTaskCommonService.getHomePageLeftBottomVideoList(this.adconfURL);
                if (HOMEPAGE_LEFT_BOTTOM_LIST != null && HOMEPAGE_LEFT_BOTTOM_LIST.size() > 0) {
                    for (int i = 0; i < HOMEPAGE_LEFT_BOTTOM_LIST.size(); i++) {
                        String moviepath = (String) ((HashMap) HOMEPAGE_LEFT_BOTTOM_LIST.get(i)).get(AllAdvertisement.MAIN_MOVIE_PATH);
                        if (!StringUtils.isBlank(moviepath)) {
                            File movieFile = new File(moviepath);
                            if (movieFile != null && movieFile.isFile()) {
                                this.videoList.add(moviepath);
                            }
                        }
                    }
                    if (this.indexOfListMedia >= this.videoList.size()) {
                        this.indexOfListMedia = 0;
                    }
                    playVideo();
                }
            }
        }
    }

    protected void onStart() {
        executeGUIAction(false, new GUIActionTakePhoto(), new Object[]{this});
        SysConfig.set("UPDATE_ENABLE", "TRUE");
        executeGUIAction(false, new GUIActionSysAction(), new Object[]{this, "sysUpdate"});
        super.onStart();
    }

    public void finishScreen() {
        this.hasFinish = true;
        if (!this.finishScreenCalled) {
            this.finishScreenCalled = true;
            clearResource();
            finish();
        }
    }

    private void clearResource() {
        if (this.screen_myVideoView != null) {
            this.screen_myVideoView.pause();
            this.screen_myVideoView.stopPlayback();
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
}
