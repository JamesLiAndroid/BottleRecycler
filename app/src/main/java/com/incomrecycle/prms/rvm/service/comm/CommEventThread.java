package com.incomrecycle.prms.rvm.service.comm;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.commonservice.CommEventCommonService;
import java.util.HashMap;

public class CommEventThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger("COMMEVENT");

    public void run() {
        CommEventCommonService commonService = new CommEventCommonService();
        while (true) {
            HashMap<String, String> event = (HashMap<String, String>) ServiceGlobal.getCommEventQueye().pop();
            if (event != null) {
                try {
                    commonService.execute("CommEventCommonService", null, event);
                } catch (Exception e) {
                    logger.error(e);
                }
            } else {
                return;
            }
        }
    }
}
