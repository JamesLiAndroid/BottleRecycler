package com.incomrecycle.prms.rvm.service.comm;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.common.task.TickTaskThread;
import java.util.HashMap;

public abstract class AbstractCommService {
    private static final Logger logger = LoggerFactory.getLogger("COMMSERVICE");
    private HashMap<String, CommCmdExecutor> hsmpCommCmdExecutor = new HashMap();
    private HashMap<String, CommEntity> hsmpCommEntity = new HashMap();

    public void setCommonEntity(String name, CommEntity commEntity) {
        this.hsmpCommEntity.put(name, commEntity);
    }

    public void addCommonCmdExecutor(CommCmdExecutor commCmdExecutor) {
        String[] cmdSet = commCmdExecutor.getCmdSet();
        for (String  put : cmdSet) {
            this.hsmpCommCmdExecutor.put(put, commCmdExecutor);
        }
    }

    public CommEntity getCommEntity(String name) {
        return (CommEntity) this.hsmpCommEntity.get(name);
    }

    public void init() {
        initCommEntity();
        TickTaskThread.getTickTaskThread().register(new TaskAction() {
            public void execute() {
                AbstractCommService.this.initCommEntity();
            }
        }, 10.0d, false);
    }

    public void initCommEntity() {
        for (String svcName : this.hsmpCommEntity.keySet()) {
            ((CommEntity) this.hsmpCommEntity.get(svcName)).init();
        }
    }

    public String execute(String cmd, String json) throws Exception {
        logger.debug("cmd:" + cmd + ";json:" + json);
        CommCmdExecutor commCmdExecutor = (CommCmdExecutor) this.hsmpCommCmdExecutor.get(cmd);
        if (commCmdExecutor == null) {
            throw new Exception("ERROR_NOTFOUND_COMM_CMD");
        }
        String res = commCmdExecutor.execute(cmd, json);
        logger.debug("cmd:" + cmd + ";json:" + json + ";result:" + res);
        return res;
    }
}
