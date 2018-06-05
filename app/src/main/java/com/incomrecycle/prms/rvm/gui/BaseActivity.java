package com.incomrecycle.prms.rvm.gui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.GuiANRWatchDog;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.camera.ViewfinderView;
import com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameDataFormat.ServerFrameType;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class BaseActivity extends Activity {
    private static Bitmap background;
    private static Drawable drawable;
    protected static final Logger logger = LoggerFactory.getLogger("BaseActivity");
    private List<HashMap<String, String>> HOMEPAGE_BACKGROUND_LIST = new ArrayList();
    protected String adconfURL;
    Object bjOfActivity = new Object();
    private Handler guiHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                GUIActionExecutor guiActionExecutor = (GUIActionExecutor) msg.obj;
                guiActionExecutor.guiAction.doAction(guiActionExecutor.paramObjs);
            }
            if (msg.what == 1) {
                try {
                    if (BaseActivity.this.doEventFilter((HashMap) msg.obj)) {
                        BaseActivity.this.doEvent((HashMap) msg.obj);
                    }
                } catch (Exception e) {
                    BaseActivity.logger.debug(e);
                }
            }
            if (msg.what == 2) {
                BaseActivity.this.updateLanguage();
            }
        }
    };

    private static class GUIActionExecutor {
        private GUIAction guiAction;
        private Object[] paramObjs;

        protected GUIActionExecutor(GUIAction guiAction, Object[] paramObjs) {
            this.guiAction = guiAction;
            this.paramObjs = paramObjs;
        }
    }

    private static final class MsgWhat {
        private static final int GUIAction = 0;
        private static final int GUIEvent = 1;
        private static final int GUILanguage = 2;

        private MsgWhat() {
        }
    }

    public abstract void doEvent(HashMap<String,String> hashMap);

    public abstract void updateLanguage();

    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy:" + this);
    }

    protected void onPause() {
        super.onPause();
        System.out.println("onPause:" + this);
    }

    protected void onPostResume() {
        super.onPostResume();
        System.out.println("onPostResume:" + this);
    }

    protected void onRestart() {
        super.onRestart();
        System.out.println("onRestart:" + this);
    }

    protected void onResume() {
        super.onResume();
        System.out.println("onResume:" + this);
        GUIGlobal.setTopBaseActivity(getName());
    }

    protected void onStart() {
        super.onStart();
        System.out.println("onStart:" + this);
        checkOnStart();
    }

    protected void onStop() {
        super.onStop();
        System.out.println("onStop:" + this);
    }

    public final void executeGUIAction(int delaySeconds, GUIAction guiAction, Object[] paramObjs) {
        if (delaySeconds < 0) {
            executeGUIAction(false, guiAction, paramObjs);
        }
        if (delaySeconds == 0) {
            executeGUIAction(true, guiAction, paramObjs);
        }
        SysGlobal.execute(new Runnable() {
            private int delaySeconds;
            private GUIAction guiAction;
            private Object[] paramObjs;

            public Runnable setDelaySeconds(int delaySeconds, GUIAction guiAction, Object[] paramObjs) {
                this.delaySeconds = delaySeconds;
                this.guiAction = guiAction;
                this.paramObjs = paramObjs;
                return this;
            }

            public void run() {
                try {
                    Thread.sleep((long) (this.delaySeconds * ServerFrameType.DATA_TRANSFER_REQ));
                    BaseActivity.this.executeGUIAction(false, this.guiAction, this.paramObjs);
                } catch (Exception e) {
                }
            }
        }.setDelaySeconds(delaySeconds, guiAction, paramObjs));
    }

    public final void executeGUIAction(boolean isBlocking, GUIAction guiAction, Object[] paramObjs) {
        if (isBlocking) {
            guiAction.doAction(paramObjs);
            return;
        }
        GUIActionExecutor guiActionExecutor = new GUIActionExecutor(guiAction, paramObjs);
        Message msg = new Message();
        msg.what = 0;
        msg.obj = guiActionExecutor;
        this.guiHandler.sendMessage(msg);
    }

    public boolean hasPackage(String packageName) {
        if (StringUtils.isBlank(packageName)) {
            return false;
        }
        try {
            if (getPackageManager().getApplicationInfo(packageName, 0) != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean launchPackage(String packageName) {
        if (StringUtils.isBlank(packageName)) {
            return false;
        }
        try {
            PackageManager packageManager = getPackageManager();
            Intent intent = new Intent();
            startActivity(packageManager.getLaunchIntentForPackage(packageName));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public BaseActivity() {
        GUIGlobal.setBaseActivity(getName(), this);
    }

    public BaseActivity getBaseActivity() {
        return this;
    }

    public void checkOnStart() {
        if (!GUIGlobal.isGUIReady()) {
            SysGlobal.execute(new Runnable() {
                public void run() {
                    GUIGlobal.exit();
                }
            });
        }
    }

    public void finish() {
        super.finish();
        GUIGlobal.setBaseActivity(getName(), null);
        System.out.println("Finish:" + this);
    }

    public void showOrHideKeybordAndResetTime(EditText edtClick, final TimeoutAction timeoutAction) {
        TimeoutTask.getTimeoutTask().reset(timeoutAction);
        edtClick.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TimeoutTask.getTimeoutTask().reset(timeoutAction);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                TimeoutTask.getTimeoutTask().reset(timeoutAction);
            }

            public void afterTextChanged(Editable s) {
                TimeoutTask.getTimeoutTask().reset(timeoutAction);
            }
        });
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public final void postEvent(HashMap hsmpEvent) {
        Message msg = new Message();
        msg.what = 1;
        msg.obj = hsmpEvent;
        this.guiHandler.sendMessage(msg);
    }

    public final void doUpdateLanguage() {
        Message msg = new Message();
        msg.what = 2;
        this.guiHandler.sendMessage(msg);
    }

    public boolean doEventFilter(HashMap hsmpEvent) {
        if ("WATCHDOG".equals((String) hsmpEvent.get("EVENT"))) {
            GuiANRWatchDog.getInstance().setAlive();
        }
        return true;
    }

    public ViewfinderView getViewfinderView() {
        return null;
    }

    public void handleDecode(byte[] data, Bitmap barcode) {
    }

    public void drawViewfinder() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void backgroundToActivity() {
        /*
        r5 = this;
        r3 = r5.bjOfActivity;
        monitor-enter(r3);
        r0 = r5.getnicPath();	 Catch:{ all -> 0x0032 }
        r2 = com.incomrecycle.common.utils.StringUtils.isBlank(r0);	 Catch:{ all -> 0x0032 }
        if (r2 != 0) goto L_0x0030;
    L_0x000d:
        r2 = drawable;	 Catch:{ all -> 0x0032 }
        if (r2 != 0) goto L_0x0017;
    L_0x0011:
        r2 = fitSizeImg(r0);	 Catch:{ all -> 0x0032 }
        drawable = r2;	 Catch:{ all -> 0x0032 }
    L_0x0017:
        r2 = drawable;	 Catch:{ all -> 0x0032 }
        if (r2 == 0) goto L_0x002e;
    L_0x001b:
        r2 = 16908290; // 0x1020002 float:2.3877235E-38 double:8.353805E-317;
        r2 = r5.findViewById(r2);	 Catch:{ all -> 0x0032 }
        r2 = (android.view.ViewGroup) r2;	 Catch:{ all -> 0x0032 }
        r4 = 0;
        r1 = r2.getChildAt(r4);	 Catch:{ all -> 0x0032 }
        r2 = drawable;	 Catch:{ all -> 0x0032 }
        r1.setBackgroundDrawable(r2);	 Catch:{ all -> 0x0032 }
    L_0x002e:
        monitor-exit(r3);	 Catch:{ all -> 0x0032 }
    L_0x002f:
        return;
    L_0x0030:
        monitor-exit(r3);	 Catch:{ all -> 0x0032 }
        goto L_0x002f;
    L_0x0032:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0032 }
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.incomrecycle.prms.rvm.gui.BaseActivity.backgroundToActivity():void");
    }

    public String getnicPath() {
        this.adconfURL = SysConfig.get("AD.URL.PREFIX") + SysConfig.get("RVM.LANGUAGE") + SysConfig.get("AD.URL");
        GUIGlobal.setCurrentSession("AD_CONF_URL", this.adconfURL);
        this.HOMEPAGE_BACKGROUND_LIST = BroadcastTaskCommonService.getBackgroundPicList(this.adconfURL);
        if (this.HOMEPAGE_BACKGROUND_LIST == null || this.HOMEPAGE_BACKGROUND_LIST.size() <= 0) {
            return null;
        }
        String PICTURE_TOP_URL = (String) ((HashMap) this.HOMEPAGE_BACKGROUND_LIST.get(0)).get(AllAdvertisement.BACKGROUND_PIC);
        if (new File(PICTURE_TOP_URL).isFile()) {
            return PICTURE_TOP_URL;
        }
        return null;
    }

    public static Drawable fitSizeImg(String path) {
        Exception e;
        if (path == null || path.length() < 1) {
            return null;
        }
        if (background == null) {
            File file = new File(path);
            Options opts = new Options();
            opts.inTempStorage = new byte[32768];
            if (file.length() < 819200) {
                opts.inSampleSize = 1;
            } else if (file.length() < 1310720) {
                opts.inSampleSize = 2;
            } else if (file.length() < 1843200) {
                opts.inSampleSize = 4;
            } else {
                opts.inSampleSize = 8;
            }
            opts.inDither = false;
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            try {
                FileInputStream fs = new FileInputStream(file);
                if (fs != null) {
                    try {
                        background = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, opts);
                        fs.close();
                        drawable = new BitmapDrawable(background);
                        return drawable;
                    } catch (Exception e2) {
                        e = e2;
                        FileInputStream fileInputStream = fs;
                        e.printStackTrace();
                        return drawable;
                    }
                }
            } catch (Exception e3) {
                e = e3;
                e.printStackTrace();
                return drawable;
            }
        }
        return drawable;
    }
}
