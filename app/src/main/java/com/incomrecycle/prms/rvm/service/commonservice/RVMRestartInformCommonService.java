package com.incomrecycle.prms.rvm.service.commonservice;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.prms.rvm.common.SysDef.MsgType;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import java.util.Date;
import java.util.HashMap;

public class RVMRestartInformCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        if ("initRestart".equalsIgnoreCase(subSvcName)) {
            return initRestart(svcName, subSvcName, hsmpParam);
        }
        return null;
    }

    public HashMap initRestart(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        String MES_TYPE = MsgType.RVM_RESTART;
        Long Time = Long.valueOf(new Date().getTime());
        String QuTime = Time.toString();
        HashMap hsmpPkg = new HashMap();
        hsmpPkg.put("MES_TYPE", MES_TYPE);
        hsmpPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
        hsmpPkg.put("RESTART_TIME", QuTime);
        hsmpPkg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + Time);
        String retJson = JSONUtils.toJSON(hsmpPkg);
        HashMap hsmpRetPkg = new HashMap();
        try {
            HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPkg)));
            if (hsmpretPkg == null) {
                return null;
            }
            if (((String) hsmpretPkg.get("MES_TYPE")).equalsIgnoreCase("RESPONSE")) {
                Integer Confirm = Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM")));
                if (Confirm.intValue() == 1) {
                    hsmpRetPkg.put("RESULT", "success");
                }
                if (Confirm.intValue() == -1) {
                    hsmpRetPkg.put("RESULT", "error");
                }
                if (Confirm.intValue() != 0) {
                    return hsmpRetPkg;
                }
                hsmpRetPkg.put("RESULT", "failed");
                return hsmpRetPkg;
            }
            hsmpRetPkg.put("RESULT", "error");
            return hsmpRetPkg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
