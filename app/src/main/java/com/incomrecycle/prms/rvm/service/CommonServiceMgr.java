package com.incomrecycle.prms.rvm.service;

import com.incomrecycle.prms.rvm.service.commonservice.ClockcalibrationTaskCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.CommEventCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.RCCTaskCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.RCCUploadSummaryCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.RVMTrafficTaskCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.WeighReadTaskCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIAlipayCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIBdjCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUICameraCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIDonationCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIEventCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIGreenCardCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIMaintenanceCheckCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIMaintenanceCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIOneCardCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIPhoneCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIQRCodeCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIQueryCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIRecycleCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUITrafficCardCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIVoucherCommonService;
import com.incomrecycle.prms.rvm.service.commonservice.gui.GUIWeChatCommonService;
import java.util.HashMap;

public class CommonServiceMgr {
    private static final CommonServiceMgr mgr = new CommonServiceMgr();
    private HashMap<String, Class> hsmpCommonService = new HashMap<>();
    private HashMap<String, String> hsmpUnlogSvc = new HashMap<>();

    private CommonServiceMgr() {
        if (this.hsmpCommonService.size() == 0) {
            this.hsmpCommonService.put("GUIQueryCommonService", GUIQueryCommonService.class);
            this.hsmpCommonService.put("GUIEventCommonService", GUIEventCommonService.class);
            this.hsmpCommonService.put("GUIRecycleCommonService", GUIRecycleCommonService.class);
            this.hsmpCommonService.put("GUIDonationCommonService", GUIDonationCommonService.class);
            this.hsmpCommonService.put("GUIVoucherCommonService", GUIVoucherCommonService.class);
            this.hsmpCommonService.put("GUIGreenCardCommonService", GUIGreenCardCommonService.class);
            this.hsmpCommonService.put("GUIMaintenanceCommonService", GUIMaintenanceCommonService.class);
            this.hsmpCommonService.put("CommEventCommonService", CommEventCommonService.class);
            this.hsmpCommonService.put("RCCTaskCommonService", RCCTaskCommonService.class);
            this.hsmpCommonService.put("RCCUploadSummaryCommonService", RCCUploadSummaryCommonService.class);
            this.hsmpCommonService.put("GUIOneCardCommonService", GUIOneCardCommonService.class);
            this.hsmpCommonService.put("GUIQRCodeCommonService", GUIQRCodeCommonService.class);
            this.hsmpCommonService.put("GUIPhoneCommonService", GUIPhoneCommonService.class);
            this.hsmpCommonService.put("WeighReadTaskCommonService", WeighReadTaskCommonService.class);
            this.hsmpCommonService.put("ClockcalibrationTaskCommonService", ClockcalibrationTaskCommonService.class);
            this.hsmpCommonService.put("GUIWeChatCommonService", GUIWeChatCommonService.class);
            this.hsmpCommonService.put("GUIMaintenanceCheckCommonService", GUIMaintenanceCheckCommonService.class);
            this.hsmpCommonService.put("GUICameraCommonService", GUICameraCommonService.class);
            this.hsmpCommonService.put("GUITrafficCardCommonService", GUITrafficCardCommonService.class);
            this.hsmpCommonService.put("GUIAlipayCommonService", GUIAlipayCommonService.class);
            this.hsmpCommonService.put("GUIBdjCommonService", GUIBdjCommonService.class);
            this.hsmpCommonService.put("RVMTrafficTaskCommonService", RVMTrafficTaskCommonService.class);
        }
        if (this.hsmpUnlogSvc.size() == 0) {
            String value = "DISABLE";
            this.hsmpUnlogSvc.put("GUIMaintenanceCommonService|checkTcpAlarm", value);
            this.hsmpUnlogSvc.put("InitCommonService|initRVMDaemon", value);
        }
    }

    public static CommonServiceMgr getMgr() {
        return mgr;
    }

    public Class getCommonService(String svcName) {
        return (Class) this.hsmpCommonService.get(svcName);
    }

    public boolean isLogEnable(String svcName, String subSvnName) {
        if (this.hsmpUnlogSvc.get(svcName) == null && this.hsmpUnlogSvc.get(svcName + "|" + subSvnName) == null) {
            return true;
        }
        return false;
    }
}
