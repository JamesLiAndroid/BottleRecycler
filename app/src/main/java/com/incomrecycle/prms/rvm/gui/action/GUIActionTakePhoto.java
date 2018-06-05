package com.incomrecycle.prms.rvm.gui.action;

import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.camera.CameraManager;
import com.incomrecycle.prms.rvm.service.task.action.RCCInstanceTask;
import java.io.File;
import java.util.Date;
import java.util.HashMap;

public class GUIActionTakePhoto extends GUIAction {
    protected void doAction(Object[] paramObjs) {
        BaseActivity baseActivity = (BaseActivity) paramObjs[0];
        final CameraManager cameraManager = new CameraManager();
        final GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        try {
            HashMap hsmp = guiCommonService.execute("GUIMaintenanceCommonService", "queryCanTakePhoto", null);
            if (hsmp != null && hsmp.size() > 0) {
                if ("TRUE".equalsIgnoreCase((String) hsmp.get("TAKEPHOTO"))) {
                    LinearLayout parentLayout = (LinearLayout) baseActivity.findViewById(R.id.take_photo_layout);
                    parentLayout.addView((LinearLayout) LayoutInflater.from(baseActivity.getBaseContext()).inflate(R.layout.camera_preview, null));
                    try {
                        cameraManager.openDriver(0, ((SurfaceView) parentLayout.findViewById(R.id.preview_view)).getHolder());
                        cameraManager.startPreview();
                        SysGlobal.execute(new Runnable() {
                            public void run() {
                                for (int i = 0; i < 10; i++) {
                                    try {
                                        Thread.currentThread();
                                        Thread.sleep(1000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    String mSavedPath = SysConfig.get("SCREENCAP.SAVE.PATH");
                                    File mSavedDir = new File(mSavedPath);
                                    if (!mSavedDir.isDirectory()) {
                                        mSavedDir.mkdirs();
                                    }
                                    String RVMcode = SysConfig.get("RVM.CODE");
                                    if (StringUtils.isBlank(RVMcode)) {
                                        RVMcode = "0";
                                    }
                                    if (cameraManager.takePictureAsFile(mSavedPath + "/" + RVMcode + "_" + DateUtils.formatDatetime(new Date(), "yyyyMMddHHmmss") + "_PHOTO" + ".png")) {
                                        HashMap hsmpParam = new HashMap();
                                        hsmpParam.put("TAKEPHOTO_ENABLE", "FALSE");
                                        try {
                                            guiCommonService.execute("GUIMaintenanceCommonService", "saveTakePhotoEnable", hsmpParam);
                                        } catch (Exception e2) {
                                            e2.printStackTrace();
                                        }
                                        HashMap<String, String> hsmpEvent = new HashMap();
                                        hsmpEvent.put(AllAdvertisement.MEDIA_TYPE, "UPLOAD_PICTURE");
                                        RCCInstanceTask.addTask(hsmpEvent);
                                        break;
                                    }
                                }
                                cameraManager.stopPreview();
                                cameraManager.closeDriver();
                            }
                        });
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
