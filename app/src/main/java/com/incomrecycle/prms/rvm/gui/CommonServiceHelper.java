package com.incomrecycle.prms.rvm.gui;

import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.prms.rvm.interfaces.CommonService;
import com.incomrecycle.prms.rvm.service.MainCommonService;
import java.util.HashMap;

public class CommonServiceHelper {

    public static class GUICommonService {
        private CommonService commonService;

        public GUICommonService(CommonService commonService) {
            this.commonService = commonService;
        }

        public HashMap<String, Object> execute(String svcName, String subSvcName, HashMap<String, Object> hsmpParam) throws Exception {
            String jsonResult = this.commonService.execute(svcName, subSvcName, JSONUtils.toJSON((HashMap) hsmpParam));
            if (jsonResult == null) {
                return null;
            }
            return JSONUtils.toHashMap(jsonResult);
        }
    }

    private static CommonService getCommonService() {
        return new MainCommonService();
    }

    public static GUICommonService getGUICommonService() {
        return new GUICommonService(getCommonService());
    }
}
