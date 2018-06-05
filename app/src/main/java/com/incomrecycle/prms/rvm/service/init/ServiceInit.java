package com.incomrecycle.prms.rvm.service.init;

import android.content.Context;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.init.InitInterface;
import com.incomrecycle.common.task.TickTaskThread;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.router.RouterService;
import com.incomrecycle.prms.rvm.router.RouterService.OnReceiveCallback;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommEventThread;
import com.incomrecycle.prms.rvm.service.commonservice.InitCommonService;
import com.incomrecycle.prms.rvm.service.event.ServiceEventListener;
import com.incomrecycle.prms.rvm.service.task.action.ClockcalibrationTaskAction;
import com.incomrecycle.prms.rvm.service.task.action.RCCTaskAction;
import com.incomrecycle.prms.rvm.service.task.action.RCCUploadSummaryTaskAction;
import com.incomrecycle.prms.rvm.service.task.action.RVMHeartBeatTaskAction;
import com.incomrecycle.prms.rvm.service.task.action.RVMRestartInformTaskAction;
import com.incomrecycle.prms.rvm.service.task.action.RVMTrafficStoreTaskAction;
import com.incomrecycle.prms.rvm.service.task.action.SystemStatusTaskAction;
import com.incomrecycle.prms.rvm.service.task.action.TCPAlarmCheckAction;
import com.incomrecycle.prms.rvm.service.traffic.TrafficEntity;
import java.util.HashMap;

public class ServiceInit implements InitInterface {
    private static final Logger logger = LoggerFactory.getLogger("ServiceInit");
    private RVMRestartInformTaskAction rvmRestartInformTaskAction = new RVMRestartInformTaskAction();

    public void Init(HashMap hsmp) {
        SysConfig.init(null);
        try {
            new InitCommonService().execute("InitCommonService", "initRVM", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SysGlobal.execute(new Thread() {
            public void run() {
                try {
                    new InitCommonService().execute("InitCommonService", "downloadBarcode", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        TrafficEntity.init();
        TickTaskThread.getTickTaskThread().register(this.rvmRestartInformTaskAction, 300.0d, true);
        ServiceGlobal.getGUIEventMgr().add(new ServiceEventListener());
        SysGlobal.execute(new CommEventThread());
        TickTaskThread.getTickTaskThread().register(new RCCTaskAction(), (double) Integer.parseInt(SysConfig.get("RCC.TASK.QUERY.INTERVAL")), true);
        TickTaskThread.getTickTaskThread().register(new RCCUploadSummaryTaskAction(), 60.0d, true);
        TickTaskThread.getTickTaskThread().register(new SystemStatusTaskAction(), 600.0d, true);
        TickTaskThread.getTickTaskThread().register(new RVMHeartBeatTaskAction(), (double) Integer.parseInt(SysConfig.get("RVM.ALIVE.TIME.HEARTBEAT")), true);
        TickTaskThread.getTickTaskThread().register(new TCPAlarmCheckAction(), 30.0d, true);
        TickTaskThread.getTickTaskThread().register(new RVMTrafficStoreTaskAction(), 600.0d, true);
        if ("true".equalsIgnoreCase(SysConfig.get("COM.PLC.CLOCK.CALIBRATION"))) {
            TickTaskThread.getTickTaskThread().register(new ClockcalibrationTaskAction(), 3600.0d, true);
        }
        SysGlobal.execute(new Thread() {
            public void run() {
                try {
                    while (true) {
                        new InitCommonService().execute("InitCommonService", "initRVMDaemon", null);
                        Thread.sleep(600000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        new RouterService(new OnReceiveCallback() {
            private HashMap<String, String> hsmpSIMCardInfo = null;
            private long prevFlow = 0;
            private long totalFlow = 0;

            public void onReceive(HashMap<String, String> hsmpCardInfo) {
                if (this.hsmpSIMCardInfo == null) {
                    this.hsmpSIMCardInfo = new HashMap();
                    SysConfig.setObj("SIMCard", this.hsmpSIMCardInfo);
                }
                String IMEI = (String) hsmpCardInfo.get("IMEI");
                String CSQ = (String) hsmpCardInfo.get("CSQ");
                String FLOW = (String) hsmpCardInfo.get("FLOW");
                this.hsmpSIMCardInfo.put("IMEI", IMEI);
                this.hsmpSIMCardInfo.put("CSQ", CSQ);
                if (!StringUtils.isEmpty(FLOW)) {
                    try {
                        long flow = Long.parseLong(FLOW);
                        if (this.totalFlow == 0) {
                            this.totalFlow = flow;
                        } else if (this.prevFlow != flow) {
                            if (flow < this.prevFlow) {
                                this.totalFlow += flow;
                            } else {
                                this.totalFlow += flow - this.prevFlow;
                            }
                        }
                        this.prevFlow = flow;
                        this.hsmpSIMCardInfo.put("FLOW", Long.toString(this.totalFlow));
                    } catch (Exception e) {
                    }
                }
                ServiceInit.logger.debug("SIMCard=" + this.hsmpSIMCardInfo);
                ServiceInit.logger.debug("Router info: IMEI=" + IMEI + ", CSQ=" + CSQ + ", FLOW=" + FLOW);
            }
        }, 300000).start();
    }

    @Override
    public void Init(Context context, HashMap hashMap) {
        SysConfig.init(null);
        try {
            new InitCommonService().execute("InitCommonService", "initRVM", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SysGlobal.execute(new Thread() {
            public void run() {
                try {
                    new InitCommonService().execute("InitCommonService", "downloadBarcode", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        TrafficEntity.init();
        TickTaskThread.getTickTaskThread().register(this.rvmRestartInformTaskAction, 300.0d, true);
        ServiceGlobal.getGUIEventMgr().add(new ServiceEventListener());
        SysGlobal.execute(new CommEventThread());
        TickTaskThread.getTickTaskThread().register(new RCCTaskAction(), (double) Integer.parseInt(SysConfig.get("RCC.TASK.QUERY.INTERVAL")), true);
        TickTaskThread.getTickTaskThread().register(new RCCUploadSummaryTaskAction(), 60.0d, true);
        TickTaskThread.getTickTaskThread().register(new SystemStatusTaskAction(), 600.0d, true);
        TickTaskThread.getTickTaskThread().register(new RVMHeartBeatTaskAction(), (double) Integer.parseInt(SysConfig.get("RVM.ALIVE.TIME.HEARTBEAT")), true);
        TickTaskThread.getTickTaskThread().register(new TCPAlarmCheckAction(), 30.0d, true);
        TickTaskThread.getTickTaskThread().register(new RVMTrafficStoreTaskAction(), 600.0d, true);
        if ("true".equalsIgnoreCase(SysConfig.get("COM.PLC.CLOCK.CALIBRATION"))) {
            TickTaskThread.getTickTaskThread().register(new ClockcalibrationTaskAction(), 3600.0d, true);
        }
        SysGlobal.execute(new Thread() {
            public void run() {
                try {
                    while (true) {
                        new InitCommonService().execute("InitCommonService", "initRVMDaemon", null);
                        Thread.sleep(600000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        new RouterService(new OnReceiveCallback() {
            private HashMap<String, String> hsmpSIMCardInfo = null;
            private long prevFlow = 0;
            private long totalFlow = 0;

            public void onReceive(HashMap<String, String> hsmpCardInfo) {
                if (this.hsmpSIMCardInfo == null) {
                    this.hsmpSIMCardInfo = new HashMap();
                    SysConfig.setObj("SIMCard", this.hsmpSIMCardInfo);
                }
                String IMEI = (String) hsmpCardInfo.get("IMEI");
                String CSQ = (String) hsmpCardInfo.get("CSQ");
                String FLOW = (String) hsmpCardInfo.get("FLOW");
                this.hsmpSIMCardInfo.put("IMEI", IMEI);
                this.hsmpSIMCardInfo.put("CSQ", CSQ);
                if (!StringUtils.isEmpty(FLOW)) {
                    try {
                        long flow = Long.parseLong(FLOW);
                        if (this.totalFlow == 0) {
                            this.totalFlow = flow;
                        } else if (this.prevFlow != flow) {
                            if (flow < this.prevFlow) {
                                this.totalFlow += flow;
                            } else {
                                this.totalFlow += flow - this.prevFlow;
                            }
                        }
                        this.prevFlow = flow;
                        this.hsmpSIMCardInfo.put("FLOW", Long.toString(this.totalFlow));
                    } catch (Exception e) {
                    }
                }
                ServiceInit.logger.debug("SIMCard=" + this.hsmpSIMCardInfo);
                ServiceInit.logger.debug("Router info: IMEI=" + IMEI + ", CSQ=" + CSQ + ", FLOW=" + FLOW);
            }
        }, 300000).start();
    }
}
