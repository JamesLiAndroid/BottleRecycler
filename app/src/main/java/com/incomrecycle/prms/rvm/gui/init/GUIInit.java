package com.incomrecycle.prms.rvm.gui.init;

import android.content.Context;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.init.InitInterface;
import com.incomrecycle.common.task.GuiANRWatchDog;
import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.common.task.TickTaskThread;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.prms.rvm.RVMApplication;
import com.incomrecycle.prms.rvm.common.NetworkStateMgr;
import com.incomrecycle.prms.rvm.common.NetworkStateMgr.OnStateChanged;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.event.GUIActivityEventListener;
import com.incomrecycle.prms.rvm.gui.event.GUIApplicationEventListener;
import com.incomrecycle.prms.rvm.gui.event.GUIEventThread;
import java.util.HashMap;

public class GUIInit implements InitInterface {
    public static final Logger logger = LoggerFactory.getLogger("GUIInit");

    public void Init(HashMap hsmp) {
        GUIGlobal.getEventMgr().add(new GUIApplicationEventListener(RVMApplication.getApplication()));
        GUIGlobal.getEventMgr().add(new GUIActivityEventListener());
        SysGlobal.execute(new GUIEventThread());
        NetworkStateMgr.getMgr().add(new OnStateChanged() {
            public void apply(String state) {
                HashMap<String, String> hsmpGUIEvent = new HashMap();
                hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                hsmpGUIEvent.put("EVENT", "NETWORKSTATE");
                hsmpGUIEvent.put("NETWORKSTATE", state);
                GUIGlobal.getEventMgr().addEvent(hsmpGUIEvent);
            }
        });
        NetworkStateMgr.getMgr().start();
        TickTaskThread.getTickTaskThread().register(TimeoutTask.getTimeoutTask(), 1.0d, false);
        GuiANRWatchDog.getInstance().start(Integer.parseInt(SysConfig.get("RVM.GUI.ANR.TIMEOUT.OUTOFRANGE")), Integer.parseInt(SysConfig.get("RVM.GUI.ANR.TIMEOUT")), new TaskAction() {
            public void execute() {
                GUIInit.logger.debug("system will reboot");
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    su.getOutputStream().write("reboot".getBytes());
                    su.getOutputStream().flush();
                    IOUtils.close(su.getOutputStream());
                } catch (Exception e) {
                }
            }
        }, Integer.parseInt(SysConfig.get("RVM.GUI.ANR.CHECK.INTERVAL")), new TaskAction() {
            public void execute() {
                HashMap<String, String> hsmpGUIEvent = new HashMap();
                hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                hsmpGUIEvent.put("EVENT", "WATCHDOG");
                GUIGlobal.getEventMgr().addEvent(hsmpGUIEvent);
            }
        });
    }

    @Override
    public void Init(Context context, HashMap hsmp) {
        GUIGlobal.getEventMgr().add(new GUIApplicationEventListener(RVMApplication.getApplication()));
        GUIGlobal.getEventMgr().add(new GUIActivityEventListener());
        SysGlobal.execute(new GUIEventThread());
        NetworkStateMgr.getMgr().add(new OnStateChanged() {
            public void apply(String state) {
                HashMap<String, String> hsmpGUIEvent = new HashMap();
                hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                hsmpGUIEvent.put("EVENT", "NETWORKSTATE");
                hsmpGUIEvent.put("NETWORKSTATE", state);
                GUIGlobal.getEventMgr().addEvent(hsmpGUIEvent);
            }
        });
        NetworkStateMgr.getMgr().start();
        TickTaskThread.getTickTaskThread().register(TimeoutTask.getTimeoutTask(), 1.0d, false);
        GuiANRWatchDog.getInstance().start(Integer.parseInt(SysConfig.get("RVM.GUI.ANR.TIMEOUT.OUTOFRANGE")), Integer.parseInt(SysConfig.get("RVM.GUI.ANR.TIMEOUT")), new TaskAction() {
            public void execute() {
                GUIInit.logger.debug("system will reboot");
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    su.getOutputStream().write("reboot".getBytes());
                    su.getOutputStream().flush();
                    IOUtils.close(su.getOutputStream());
                } catch (Exception e) {
                }
            }
        }, Integer.parseInt(SysConfig.get("RVM.GUI.ANR.CHECK.INTERVAL")), new TaskAction() {
            public void execute() {
                HashMap<String, String> hsmpGUIEvent = new HashMap();
                hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                hsmpGUIEvent.put("EVENT", "WATCHDOG");
                GUIGlobal.getEventMgr().addEvent(hsmpGUIEvent);
            }
        });
    }
}
