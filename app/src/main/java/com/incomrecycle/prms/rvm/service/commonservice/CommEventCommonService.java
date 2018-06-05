package com.incomrecycle.prms.rvm.service.commonservice;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import java.util.HashMap;

public class CommEventCommonService implements AppCommonService {
    public HashMap execute(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        if (StringUtils.isBlank(SysConfig.get("CommEventCommonService.class"))) {
            return null;
        }
        return ((AppCommonService) Class.forName(SysConfig.get("CommEventCommonService.class")).newInstance()).execute(svcName, subSvcName, hsmpParam);
    }
}
