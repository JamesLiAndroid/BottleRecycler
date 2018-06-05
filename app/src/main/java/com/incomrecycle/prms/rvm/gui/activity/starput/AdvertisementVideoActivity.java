package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.MotionEvent;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.gui.activity.view.MyVideoView;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class AdvertisementVideoActivity extends BaseActivity {
    private String VIDEOPATH;
    private boolean isClick = false;
    private boolean isFinished;
    private MyVideoView myVideoView_advertisement;
    private String recycleBottle = null;

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        initVideoView();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        setContentView(R.layout.activity_advertisement_video);
        backgroundToActivity();
        this.myVideoView_advertisement = (MyVideoView) findViewById(R.id.myVideoview_advertisement);
        HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
        if (TRANSMIT_ADV != null) {
            HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
            if (HOMEPAGE_LEFT != null) {
                this.VIDEOPATH = (String) HOMEPAGE_LEFT.get(AllAdvertisement.CLICK_MOVIE_PATH);
                if (StringUtils.isBlank(this.VIDEOPATH)) {
                    this.VIDEOPATH = (String) HOMEPAGE_LEFT.get(AllAdvertisement.CLICK_PICTURE_PATH);
                }
                this.recycleBottle = (String) HOMEPAGE_LEFT.get("RECYCLE_BOTTLE");
            }
        }
        this.myVideoView_advertisement.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                if (AdvertisementVideoActivity.this.myVideoView_advertisement != null) {
                    mp.start();
                }
            }
        });
        this.myVideoView_advertisement.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                AdvertisementVideoActivity.this.finish();
                return true;
            }
        });
        this.myVideoView_advertisement.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                if (!AdvertisementVideoActivity.this.isClick) {
                    AdvertisementVideoActivity.this.isClick = true;
                    if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(AdvertisementVideoActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            AdvertisementVideoActivity.this.startActivity(intent);
                            AdvertisementVideoActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void initVideoView() {
        if (this.myVideoView_advertisement == null) {
            this.myVideoView_advertisement = (MyVideoView) findViewById(R.id.myVideoview_advertisement);
        }
        if (this.VIDEOPATH != null) {
            this.myVideoView_advertisement.setVideoPath(this.VIDEOPATH);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (1 != event.getAction() || this.isFinished) {
            return super.onTouchEvent(event);
        }
        HashMap map = new HashMap();
        map.put("KEY", AllClickContent.VIDEO_BACK);
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
        } catch (Exception e) {
        }
        this.isFinished = true;
        finish();
        overridePendingTransition(R.anim.scale_small_in, R.anim.scale_small_out);
        return true;
    }

    protected void onPause() {
        super.onPause();
        if (this.myVideoView_advertisement != null) {
            this.myVideoView_advertisement.pause();
        }
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.myVideoView_advertisement != null) {
            this.myVideoView_advertisement.stopPlayback();
            this.myVideoView_advertisement = null;
        }
    }

    private void startRecycleBottle() throws Exception {
        try {
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initRecycle", null);
            HashMap<String, Object> hsmpInitProductType = new HashMap();
            hsmpInitProductType.put("PRODUCT_TYPE", "BOTTLE");
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initProductType", hsmpInitProductType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        GUIGlobal.setCurrentSession("USESTATE", Integer.valueOf(0));
        if (!StringUtils.isBlank(SysConfig.get("PutBottleActivity.class"))) {
            Intent intent = new Intent(this, Class.forName(SysConfig.get("PutBottleActivity.class")));
            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        }
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
        if ("CMD".equalsIgnoreCase((String) hsmpEvent.get("EVENT"))) {
            String CMD = (String) hsmpEvent.get("CMD");
            if ("REQUEST_DONATION_RECYCLE".equalsIgnoreCase(CMD)) {
                try {
                    if ("true".equals(this.recycleBottle)) {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initRecycle", null);
                        HashMap<String, Object> hsmpInitProductType = new HashMap();
                        hsmpInitProductType.put("PRODUCT_TYPE", "BOTTLE");
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "initProductType", hsmpInitProductType);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{getBaseActivity(), Integer.valueOf(2), "DONATION"});
            }
            if ("REQUEST_RECYCLE".equalsIgnoreCase(CMD)) {
                try {
                    if ("true".equals(this.recycleBottle)) {
                        startRecycleBottle();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
}
