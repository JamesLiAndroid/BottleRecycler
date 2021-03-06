package com.incomrecycle.prms.rvm.service.comm.entity;

import android.support.v4.view.MotionEventCompat;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.queue.FIFOQueue;
import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.common.task.TickTaskThread;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.EncryptUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.COM_PLC_VERSION;
import com.incomrecycle.prms.rvm.common.SysDef.HardwareAlarmState;
import com.incomrecycle.prms.rvm.common.SysDef.ServiceName;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.serialcomm.CommRecvCallBack;
import com.incomrecycle.prms.rvm.service.comm.entity.serialcomm.CommStateCallBack;
import com.incomrecycle.prms.rvm.service.comm.entity.serialcomm.K5V1SerialComm;
import com.incomrecycle.prms.rvm.service.comm.entity.serialcomm.MCUSerialComm;
import com.incomrecycle.prms.rvm.service.comm.entity.serialcomm.MachineComm;
import com.incomrecycle.prms.rvm.service.commonservice.PLCAnormalMonitorUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class PLC104CCommEntity implements CommEntity {
    private static final long BUTTON_TIME_INTERVAL = 1000;
    private static final Logger logger = LoggerFactory.getLogger("PLC104CCommEntity");
    private String COM_PLC_PROTOCOL;
    private FIFOQueue CmdResultFIFOQueue;
    private int beltState;
    private boolean buttonEnable;
    private long buttonTime;
    protected boolean clearFogEnable;
    private CommRecvCallBack commRecvCallBack;
    private int currMaterialState;
    private FIFOQueue eventFIFOQueue;
    private int forwardFirstLightTime;
    private int forwardSecondLightTime;
    private int forwardThirdLightTime;
    protected boolean hasLightCmd;
    protected boolean hasMetalLight;
    private boolean heartBeatEnable;
    private boolean idleQueryEnable;
    boolean isInCheck;
    private boolean isPlcError;
    private boolean isPutBottle;
    private long lBeltRollingContinueTime;
    private long lBeltStateTime;
    private long lCmdInterval;
    long lFirstLightTime;
    private long lLastCmdTime;
    long lSecondLightTime;
    long lThirdLightTime;
    private long lWaitBeltStopTime;
    private long lWaitSortChangedTime;
    private boolean lightCheck;
    long lightShakeTime;
    private MachineComm machineComm;
    private int materialState;
    private HardwareAlarmState plcCommAlarm;
    private PLCCommStateCallBack plcCommStateCallBack;
    private PLCResetThread plcResetThread;
    private FIFOQueue plcResponseFIFOQueue;
    private HardwareAlarmState plcSortAlarm;
    private int recyclePauseTime;
    private long secondLightDelay;
    private boolean serviceEnable;
    private int sortErrorTimes;
    private TaskAction taskActionMonitor;

    private static final class BeltState {
        private static final int BELT_BACKWARD = 2;
        private static final int BELT_FORWARD = 1;
        private static final int BELT_STOP = 0;

        private BeltState() {
        }
    }

    private class ClearFogThread extends Thread {
        private static final int CLEARFOGFLAG_IDLE = 1;
        private static final int CLEARFOGFLAG_NONE = 0;
        private static final int CLEARFOGFLAG_WORK = 2;
        private static final int COOLING_SECONDS = 10800;
        private static final int HOTING_SECONDS = 600;

        private ClearFogThread() {
        }

        public void run() {
            long lStartTime = 0;
            long lExpectContinueTime = 0;
            int clearFogFlag = 0;
            int processState = 1;
            while (true) {
                long lTime = System.currentTimeMillis();
                switch (processState) {
                    case 0:
                        if (clearFogFlag != 1) {
                            clearFogFlag = 1;
                            lStartTime = System.currentTimeMillis();
                            lExpectContinueTime = 10800000;
                        }
                        if (lTime - lStartTime >= lExpectContinueTime) {
                            processState = 1;
                            clearFogFlag = 0;
                            break;
                        }
                        break;
                    case 1:
                        if (PLC104CCommEntity.this.machineComm.isOpen()) {
                            if (clearFogFlag != 2) {
                                clearFogFlag = 2;
                                lStartTime = System.currentTimeMillis();
                                lExpectContinueTime = 600000;
                            }
                            if (lTime - lStartTime < lExpectContinueTime) {
                                try {
                                    PLC104CCommEntity.this.sendCmd(PLC_CMD.LIGHT_ON);
                                    break;
                                } catch (Exception e) {
                                    break;
                                }
                            }
                            processState = 0;
                            clearFogFlag = 0;
                            try {
                                PLC104CCommEntity.this.sendCmd(PLC_CMD.LIGHT_OFF);
                                break;
                            } catch (Exception e2) {
                                break;
                            }
                        }
                        break;
                }
                try {
                    sleep(1200);
                } catch (Exception e3) {
                }
            }
        }
    }

    private class ExecuteThread extends Thread {
        private ExecuteThread() {
        }

        public void run() {
            while (true) {
                byte[] data = (byte[]) PLC104CCommEntity.this.plcResponseFIFOQueue.pop();
                if (data != null) {
                    try {
                        PLC104CCommEntity.this.execute(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    return;
                }
            }
        }
    }

    private static final class MaterialState {
        private static final int MATERIAL_TO_CAN = 2;
        private static final int MATERIAL_TO_NONE = 0;
        private static final int MATERIAL_TO_PET = 1;

        private MaterialState() {
        }
    }

    private class PLCResetThread extends Thread {
        private PLCResetThread() {
        }

        public void run() {
            try {
                Thread.sleep(200);
                if (PLC104CCommEntity.this.hasLightCmd && !PLC104CCommEntity.this.clearFogEnable) {
                    PLC104CCommEntity.this.sendComm(PLC_CMD.LIGHT_OFF);
                }
                Thread.sleep(200);
                PLC104CCommEntity.this.sendComm(PLC_CMD.BELT_FORWARD);
                Thread.sleep(5000);
                PLC104CCommEntity.this.sendComm(PLC_CMD.BELT_STOP);
                PLC104CCommEntity.this.beltState = 0;
                PLC104CCommEntity.this.execute("RVM_CLOCK_CALIBRATION", null);
                String POWER_ON_TIME = SysConfig.get("RVM.POWER.ON.TIME");
                String POWER_OFF_TIME = SysConfig.get("RVM.POWER.OFF.TIME");
                if (StringUtils.isBlank(POWER_ON_TIME) || StringUtils.isBlank(POWER_OFF_TIME)) {
                    PLC104CCommEntity.this.execute("RVM_POWER_OFF_DISABLE", null);
                    return;
                }
                HashMap hsmpParam = new HashMap();
                hsmpParam.put("POWER_ON_TIME", POWER_ON_TIME);
                hsmpParam.put("POWER_OFF_TIME", POWER_OFF_TIME);
                PLC104CCommEntity.this.execute("RVM_POWER_OFF_ENABLE", JSONUtils.toJSON(hsmpParam));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class PLCStep {
        public static final int CHECK_BARCODE = 4;
        public static final int DONE = -2;
        public static final int IDLE = 1;
        public static final int PAUSE = 8;
        public static final int PUT_BOTTLE_FIRST = 7;
        public static final int PUT_BOTTLE_NEXT = 2;
        public static final int RECYCLE_BOTTLE = 5;
        public static final int REJECT_BOTTLE = 6;
        public static final int RESET = 0;
        public static final int SCAN_BARCODE = 3;
        public static final int STOP = -1;

        private PLCStep() {
        }
    }

    private static final class PLC_CMD {
        private static final byte[] BELT_BACKWARD = "NNNN".getBytes();
        private static final byte[] BELT_FORWARD = "YYYY".getBytes();
        private static final byte[] BELT_STOP = "AAAA".getBytes();
        private static final byte[] CHECK_FIRST_LIGHT_STATE = "JJJJ".getBytes();
        private static final byte[] CHECK_THIRD_LIGHT_STATE = "ZZZZ".getBytes();
        private static final byte[] LIGHT_FLASH = "RRRR".getBytes();
        private static final byte[] LIGHT_OFF = "SSSS".getBytes();
        private static final byte[] LIGHT_ON = "QQQQ".getBytes();
        private static final byte[] MAT_TO_CAN = "GGGG".getBytes();
        private static final byte[] MAT_TO_PET = "OOOO".getBytes();
        private static final byte[] NONE = "KKKK".getBytes();
        private static final byte[] QRCODE_LIGHT_OFF = "WWWW".getBytes();
        private static final byte[] QRCODE_LIGHT_ON = "VVVV".getBytes();
        private static final byte[] STORAGE_DOOR_OPEN = "UUUU".getBytes();

        private PLC_CMD() {
        }
    }

    private static final class StateMask {
        private static final int[] BUTTON_PUSH = new int[]{1, 1};
        private static final int[] FIRST_LIGHT = new int[]{0, 32};
        private static final int[] FIRST_LIGHT_ADDI = new int[]{0, 64};
        private static final int[] MATERIAL_TO_CAN = new int[]{3, 2};
        private static final int[] MATERIAL_TO_PET = new int[]{3, 1};
        private static final int[] METAL_LIGHT = new int[]{0, 4};
        private static final int[] SECOND_LIGHT = new int[]{0, 2};
        private static final int[] SECOND_LIGHT_FIRST = new int[]{3, 128};
        private static final int[] THIRD_LIGHT = new int[]{0, 16};

        private StateMask() {
        }
    }

    private class PLCCommStateCallBack implements CommStateCallBack {
        private boolean doBeltForward;
        private boolean firstLightErrorReset;
        private int firstLightErrorTimes;
        private int firstLightNoErrorTimes;
        private long firstLightStateCheckTime;
        private boolean isFirstCheckLightState;
        private int maxErrorTimes;
        private long maxStateCheckInterval;
        private boolean secondLightErrorReset;
        private int secondLightErrorTimes;
        private int secondLightNoErrorTimes;
        private long secondLightStateCheckTime;
        private boolean thirdLightErrorReset;
        private int thirdLightErrorTimes;
        private int thirdLightNoErrorTimes;
        private long thirdLightStateCheckTime;

        private PLCCommStateCallBack() {
            this.firstLightErrorReset = false;
            this.secondLightErrorReset = false;
            this.thirdLightErrorReset = false;
            this.firstLightStateCheckTime = 0;
            this.secondLightStateCheckTime = 0;
            this.thirdLightStateCheckTime = 0;
            this.maxStateCheckInterval = 5000;
            this.firstLightErrorTimes = 0;
            this.secondLightErrorTimes = 0;
            this.thirdLightErrorTimes = 0;
            this.firstLightNoErrorTimes = 0;
            this.secondLightNoErrorTimes = 0;
            this.thirdLightNoErrorTimes = 0;
            this.maxErrorTimes = 3;
            this.doBeltForward = false;
            this.isFirstCheckLightState = true;
        }

        public void apply(byte[] data) {
            int sortToPetSignal = data[StateMask.MATERIAL_TO_PET[0]] & StateMask.MATERIAL_TO_PET[1];
            int sortToCanSignal = data[StateMask.MATERIAL_TO_CAN[0]] & StateMask.MATERIAL_TO_CAN[1];
            if (sortToPetSignal != 0) {
                PLC104CCommEntity.this.currMaterialState = 1;
            }
            if (sortToCanSignal != 0) {
                PLC104CCommEntity.this.currMaterialState = 2;
            }
            if (sortToPetSignal == 0 && sortToCanSignal == 0) {
                PLC104CCommEntity.this.currMaterialState = 0;
            }
            int firstLightSignal = data[StateMask.FIRST_LIGHT[0]] & StateMask.FIRST_LIGHT[1];
            int secondLightSignal = data[StateMask.SECOND_LIGHT[0]] & StateMask.SECOND_LIGHT[1];
            int thirdLightSignal = data[StateMask.THIRD_LIGHT[0]] & StateMask.THIRD_LIGHT[1];
            long lTime = System.currentTimeMillis();
            if (PLC104CCommEntity.this.lightCheck) {
                if (this.firstLightStateCheckTime == 0) {
                    this.firstLightStateCheckTime = lTime;
                }
                if (this.secondLightStateCheckTime == 0) {
                    this.secondLightStateCheckTime = lTime;
                }
                if (this.thirdLightStateCheckTime == 0) {
                    this.thirdLightStateCheckTime = lTime;
                }
            } else {
                this.firstLightStateCheckTime = 0;
                this.secondLightStateCheckTime = 0;
                this.thirdLightStateCheckTime = 0;
            }
            if ("TRUE".equalsIgnoreCase(SysConfig.get("RVM.QUERY.LIGHT.STATE")) && PLC104CCommEntity.this.lightCheck) {
                boolean firstLightCheckedChanged = false;
                if (firstLightSignal > 0) {
                    this.firstLightNoErrorTimes = 0;
                    if (this.firstLightErrorReset) {
                        this.firstLightErrorTimes = 0;
                        this.firstLightErrorReset = false;
                    }
                    if (lTime - this.firstLightStateCheckTime >= this.maxStateCheckInterval) {
                        this.firstLightStateCheckTime = lTime;
                        if (this.firstLightErrorTimes <= this.maxErrorTimes) {
                            this.firstLightErrorTimes++;
                        }
                        if (this.firstLightErrorTimes == this.maxErrorTimes) {
                            firstLightCheckedChanged = true;
                        }
                    }
                } else if (this.firstLightErrorTimes > 0) {
                    this.firstLightErrorTimes = 0;
                    this.firstLightNoErrorTimes = 0;
                    this.firstLightStateCheckTime = lTime;
                } else if (lTime - this.firstLightStateCheckTime >= this.maxStateCheckInterval) {
                    this.firstLightStateCheckTime = lTime;
                    if (this.firstLightNoErrorTimes <= this.maxErrorTimes) {
                        this.firstLightNoErrorTimes++;
                    }
                    if (this.firstLightNoErrorTimes == this.maxErrorTimes) {
                        firstLightCheckedChanged = true;
                    }
                }
                boolean secondLightCheckedChanged = false;
                if (secondLightSignal > 0) {
                    this.secondLightNoErrorTimes = 0;
                    if (this.secondLightErrorReset) {
                        this.secondLightErrorTimes = 0;
                        this.secondLightErrorReset = false;
                    }
                    if (lTime - this.secondLightStateCheckTime >= this.maxStateCheckInterval) {
                        this.secondLightStateCheckTime = lTime;
                        if (this.secondLightErrorTimes <= this.maxErrorTimes) {
                            this.secondLightErrorTimes++;
                        }
                        if (this.secondLightErrorTimes == this.maxErrorTimes) {
                            if (this.doBeltForward) {
                                secondLightCheckedChanged = true;
                            } else {
                                try {
                                    PLC104CCommEntity.this.sendComm(PLC_CMD.BELT_FORWARD);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                this.doBeltForward = true;
                                this.secondLightErrorTimes = 0;
                            }
                        }
                    }
                } else if (this.secondLightErrorTimes > 0) {
                    this.secondLightErrorTimes = 0;
                    this.doBeltForward = false;
                    this.secondLightNoErrorTimes = 0;
                    this.secondLightStateCheckTime = lTime;
                } else if (lTime - this.secondLightStateCheckTime >= this.maxStateCheckInterval) {
                    this.secondLightStateCheckTime = lTime;
                    if (this.secondLightNoErrorTimes <= this.maxErrorTimes) {
                        this.secondLightNoErrorTimes++;
                    }
                    if (this.secondLightNoErrorTimes == this.maxErrorTimes) {
                        secondLightCheckedChanged = true;
                    }
                }
                boolean thirdLightCheckedChanged = false;
                if (thirdLightSignal > 0) {
                    this.thirdLightNoErrorTimes = 0;
                    if (this.thirdLightErrorReset) {
                        this.thirdLightErrorTimes = 0;
                        this.thirdLightErrorReset = false;
                    }
                    if (lTime - this.thirdLightStateCheckTime >= this.maxStateCheckInterval) {
                        this.thirdLightStateCheckTime = lTime;
                        if (this.thirdLightErrorTimes <= this.maxErrorTimes) {
                            this.thirdLightErrorTimes++;
                        }
                        if (this.thirdLightErrorTimes == this.maxErrorTimes) {
                            thirdLightCheckedChanged = true;
                        }
                    }
                } else if (this.thirdLightErrorTimes > 0) {
                    this.thirdLightErrorTimes = 0;
                    this.thirdLightNoErrorTimes = 0;
                    this.thirdLightStateCheckTime = lTime;
                } else if (lTime - this.thirdLightStateCheckTime >= this.maxStateCheckInterval) {
                    this.thirdLightStateCheckTime = lTime;
                    if (this.thirdLightNoErrorTimes <= this.maxErrorTimes) {
                        this.thirdLightNoErrorTimes++;
                    }
                    if (this.thirdLightNoErrorTimes == this.maxErrorTimes) {
                        thirdLightCheckedChanged = true;
                    }
                }
                if (this.isFirstCheckLightState || firstLightCheckedChanged || secondLightCheckedChanged || thirdLightCheckedChanged) {
                    HashMap hashMap;
                    HashMap<String, String> hsmpParam;
                    if (firstLightCheckedChanged && this.firstLightErrorTimes > 0) {
                        hashMap = new HashMap();
                        hsmpParam = new HashMap();
                        hsmpParam.put("type", "LIGHT_UNNORMAL_ON");
                        hsmpParam.put("msg", "FIRST_LIGHT_UNNORMAL_ON");
                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                    }
                    if (secondLightCheckedChanged && this.secondLightErrorTimes > 0) {
                        hashMap = new HashMap();
                        hsmpParam = new HashMap();
                        hsmpParam.put("type", "LIGHT_UNNORMAL_ON");
                        hsmpParam.put("msg", "SECOND_LIGHT_UNNORMAL_ON");
                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                    }
                    if (thirdLightCheckedChanged && this.thirdLightErrorTimes > 0) {
                        hashMap = new HashMap();
                        hsmpParam = new HashMap();
                        hsmpParam.put("type", "LIGHT_UNNORMAL_ON");
                        hsmpParam.put("msg", "THIRD_LIGHT_UNNORMAL_ON");
                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                    }
                    if (this.firstLightErrorTimes == 0 && this.firstLightNoErrorTimes >= this.maxErrorTimes && this.secondLightErrorTimes == 0 && this.secondLightNoErrorTimes >= this.maxErrorTimes && this.thirdLightErrorTimes == 0 && this.thirdLightNoErrorTimes >= this.maxErrorTimes) {
                        hashMap = new HashMap();
                        hsmpParam = new HashMap();
                        hsmpParam.put("type", "LIGHT_NORMAL");
                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                    }
                }
                this.isFirstCheckLightState = false;
            }
        }
    }

    public void PLCAuto() throws Exception {
        long delayTime = 0;
        int step = 0;
        long cheatTime = 0;
        long lRemovingTimeout = Long.parseLong(SysConfig.get("COM.PLC.TAKEOUT.TIMEOUT"));
        long lMonitorStartTime = System.currentTimeMillis();
        boolean controlCheat = false;
        boolean isMetalLightOn = false;
        while (true) {
            logger.debug("STEP:" + step);
            HashMap<String, String> hsmpParam;
            int flag;
            long lWaitStartTime;
            String event;
            switch (step) {
                case -2:
                    step = 0;
                    this.eventFIFOQueue.reset();
                    break;
                case -1:
                    if (this.hasLightCmd && !this.clearFogEnable) {
                        sendCmd(PLC_CMD.LIGHT_OFF);
                    }
                    if (this.isPutBottle) {
                        sendCmd(PLC_CMD.BELT_FORWARD);
                    }
                    step = -2;
                    break;
                case 0:
                    if (cheatTime > 0 && (cheatTime + 2000) - System.currentTimeMillis() > 0) {
                        hsmpParam = new HashMap();
                        hsmpParam.put("type", "IN_CHEATING");
                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                        Thread.sleep(delayTime);
                    }
                    step = 1;
                    break;
                case 1:
                    this.isPutBottle = false;
                    controlCheat = false;
                    if (this.hasLightCmd && !this.clearFogEnable) {
                        sendCmd(PLC_CMD.LIGHT_OFF);
                    }
                    delayTime = 10000;
                    flag = 0;
                    boolean hasFirstLightOn = false;
                    lWaitStartTime = System.currentTimeMillis();
                    do {
                        event = (String) this.eventFIFOQueue.pop(getRealDelayTime(lWaitStartTime, delayTime));
                        if (event == null) {
                            break;
                        }
                        logger.debug(event);
                        if ("EVENT:FIRST_LIGHT_ON".equals(event)) {
                            hasFirstLightOn = true;
                        }
                        if ((flag & 1) == 0 && "EVENT:FIRST_LIGHT_ON".equals(event)) {
                            hsmpParam = new HashMap();
                            hsmpParam.put("type", "FIRST_LIGHT_ON");
                            ServiceGlobal.getCommEventQueye().push(hsmpParam);
                            hsmpParam = new HashMap();
                            hsmpParam.put("type", "ENTRY_RECYCLE");
                            ServiceGlobal.getCommEventQueye().push(hsmpParam);
                            delayTime = 4000;
                            lWaitStartTime = System.currentTimeMillis();
                            flag |= 1;
                        }
                    } while (!"CMD:START".equals(event));
                    if (!hasFirstLightOn) {
                        step = 2;
                        break;
                    }
                    step = 7;
                    this.eventFIFOQueue.push("EVENT:FIRST_LIGHT_ON");
                    break;
                case 2:
                case 7:
                    isMetalLightOn = false;
                    if (this.hasLightCmd && !this.clearFogEnable) {
                        sendCmd(PLC_CMD.LIGHT_ON);
                    }
                    if (step != 7) {
                        delayTime = 10000;
                        flag = 0;
                        lWaitStartTime = System.currentTimeMillis();
                        while (true) {
                            event = (String) this.eventFIFOQueue.pop(getRealDelayTime(lWaitStartTime, delayTime));
                            if (event != null) {
                                logger.debug(event);
                                if ("CMD:STOP".equals(event)) {
                                    step = -1;
                                } else if ("CMD:PAUSE".equals(event)) {
                                    step = 8;
                                } else {
                                    if ("EVENT:FIRST_LIGHT_ON".equals(event)) {
                                        if ((flag & 1) == 0) {
                                            hsmpParam = new HashMap();
                                            hsmpParam.put("type", "FIRST_LIGHT_ON");
                                            ServiceGlobal.getCommEventQueye().push(hsmpParam);
                                            flag |= 1;
                                        }
                                        step = 3;
                                        delayTime = 0;
                                    }
                                    if ("EVENT:SECOND_LIGHT_ON".equals(event) && controlCheat) {
                                        hsmpParam = new HashMap();
                                        hsmpParam.put("type", "THIRD_LIGHT_ON_BACKWARD");
                                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                                        hsmpParam = new HashMap();
                                        hsmpParam.put("type", "SECOND_LIGHT_ON_BACKWARD");
                                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                                        step = -1;
                                        cheatTime = System.currentTimeMillis();
                                        controlCheat = false;
                                    }
                                }
                            }
                            if (step != 2 && step != 7) {
                                break;
                            }
                            hsmpParam = new HashMap();
                            hsmpParam.put("type", "NO_FIRST_LIGHT");
                            ServiceGlobal.getCommEventQueye().push(hsmpParam);
                            break;
                        }
                    }
                    step = 3;
                    break;
                case 3:
                    hsmpParam = new HashMap();
                    hsmpParam.put("type", "SCAN_BARCODE");
                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                    sendCmd(PLC_CMD.BELT_FORWARD);
                    if (this.hasLightCmd && !this.clearFogEnable && "TRUE".equalsIgnoreCase(SysConfig.get("SET.PLC.LIGHT.FLASH"))) {
                        sendCmd(PLC_CMD.LIGHT_FLASH);
                    }
                    delayTime = (long) this.forwardSecondLightTime;
                    lMonitorStartTime = System.currentTimeMillis();
                    lWaitStartTime = System.currentTimeMillis();
                    do {
                        event = (String) this.eventFIFOQueue.pop(getRealDelayTime(lWaitStartTime, delayTime));
                        if (event != null) {
                            logger.debug(event);
                            if ("CMD:STOP".equals(event)) {
                                step = -1;
                            } else if ("CMD:PAUSE".equals(event)) {
                                step = 8;
                            } else {
                                if ("EVENT:SECOND_LIGHT_ON".equals(event) || "EVENT:BARCODE_READED".equals(event)) {
                                    step = 4;
                                    delayTime = 0;
                                    PLCAnormalMonitorUtils.addData("SECOND_LIGHT_LOST", "NORMAL");
                                }
                                if ("EVENT:METAL_LIGHT_ON".equals(event)) {
                                    isMetalLightOn = true;
                                }
                            }
                        }
                        if (step != 3) {
                            break;
                        }
                        hsmpParam = new HashMap();
                        hsmpParam.put("type", "NO_SECOND_LIGHT");
                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                        step = 6;
                        break;
                    } while (!"EVENT:THIRD_LIGHT_ON".equals(event));
                    PLCAnormalMonitorUtils.addData("SECOND_LIGHT_LOST", "ANORMAL");
                    if (3000 + lMonitorStartTime > System.currentTimeMillis()) {
                        hsmpParam = new HashMap();
                        hsmpParam.put("type", "THIRD_LIGHT_ON_BACKWARD");
                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                        step = -1;
                        cheatTime = System.currentTimeMillis();
                    } else {
                        hsmpParam = new HashMap();
                        hsmpParam.put("type", "NO_SECOND_LIGHT");
                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                        step = 2;
                        controlCheat = false;
                    }
                    if (step != 3) {
                        hsmpParam = new HashMap();
                        hsmpParam.put("type", "NO_SECOND_LIGHT");
                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                        step = 6;
                    }
                case 4:
                    hsmpParam = new HashMap();
                    hsmpParam.put("type", "RECYCLE_CHECK");
                    if (this.hasMetalLight) {
                        hsmpParam.put("HAS_METAL_LIGHT", "TRUE");
                        if (isMetalLightOn) {
                            hsmpParam.put("METAL_LIGHT_ON", "TRUE");
                        } else {
                            hsmpParam.put("METAL_LIGHT_ON", "FALSE");
                        }
                    }
                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                    delayTime = 2000;
                    lWaitStartTime = System.currentTimeMillis();
                    while (true) {
                        event = (String) this.eventFIFOQueue.pop(getRealDelayTime(lWaitStartTime, 2000));
                        if (event == null) {
                            break;
                        }
                        logger.debug(event);
                        if ("CMD:STOP".equals(event)) {
                            step = -1;
                            break;
                        } else if ("CMD:PAUSE".equals(event)) {
                            step = 8;
                            break;
                        } else if ("CMD:RECYCLE".equals(event)) {
                            step = 5;
                            break;
                        } else if ("CMD:REJECT".equals(event)) {
                            step = 6;
                            break;
                        } else if ("EVENT:SECOND_LIGHT_ON".equals(event)) {
                        }
                    }
                case 5:
                    if (!(!"FALSE".equalsIgnoreCase(SysConfig.get("RECYCLE.STATUS.STOP")) || this.currMaterialState == this.materialState || this.plcSortAlarm == HardwareAlarmState.ALARM)) {
                        sendCmd(PLC_CMD.BELT_STOP);
                        Thread.sleep(200);
                        if (this.materialState == 1) {
                            if (this.currMaterialState == 0) {
                                sendCmd(PLC_CMD.MAT_TO_CAN);
                                Thread.sleep(800);
                            }
                            sendCmd(PLC_CMD.MAT_TO_PET);
                        } else {
                            if (this.currMaterialState == 0) {
                                sendCmd(PLC_CMD.MAT_TO_PET);
                                Thread.sleep(800);
                            }
                            sendCmd(PLC_CMD.MAT_TO_CAN);
                        }
                        delayTime = Long.parseLong(SysConfig.get("COM.PLC.SORT.TIMEOUT"));
                        lWaitStartTime = System.currentTimeMillis();
                        while (true) {
                            event = (String) this.eventFIFOQueue.pop(getRealDelayTime(lWaitStartTime, delayTime));
                            if (event != null) {
                                logger.debug(event);
                                if ("CMD:STOP".equals(event)) {
                                    step = -1;
                                } else if ("CMD:PAUSE".equals(event)) {
                                    step = 8;
                                } else if (("EVENT:SORT_TO_PET".equals(event) && this.materialState == 1) || ("EVENT:SORT_TO_CAN".equals(event) && this.materialState == 2)) {
                                    this.sortErrorTimes = 0;
                                }
                            }
                            if (this.currMaterialState != 0) {
                                if (step == -1) {
                                    break;
                                }
                            } else {
                                step = 6;
                                break;
                            }
                        }
                    }
                    flag = 0;
                    delayTime = (long) this.forwardThirdLightTime;
                    sendCmd(PLC_CMD.BELT_FORWARD);
                    lWaitStartTime = System.currentTimeMillis();
                    while (true) {
                        event = (String) this.eventFIFOQueue.pop(getRealDelayTime(lWaitStartTime, delayTime));
                        if (event != null) {
                            logger.debug(event);
                            if ("CMD:STOP".equals(event)) {
                                step = -1;
                            } else if ("CMD:PAUSE".equals(event)) {
                                step = 8;
                            } else if ("EVENT:THIRD_LIGHT_ON".equals(event)) {
                                if ((flag & 4) == 0) {
                                    hsmpParam = new HashMap();
                                    hsmpParam.put("type", "THIRD_LIGHT_ON");
                                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                                    flag |= 4;
                                }
                                step = 2;
                                controlCheat = true;
                                delayTime = 0;
                                PLCAnormalMonitorUtils.addData("THIRD_LIGHT_LOST", "NORMAL");
                            }
                        }
                        if ((flag & 4) == 0) {
                            hsmpParam = new HashMap();
                            hsmpParam.put("type", "NO_THIRD_LIGHT");
                            ServiceGlobal.getCommEventQueye().push(hsmpParam);
                            step = 2;
                            controlCheat = false;
                            PLCAnormalMonitorUtils.addData("THIRD_LIGHT_LOST", "ANORMAL");
                        }
                        sendCmd(PLC_CMD.BELT_STOP);
                        Thread.sleep(200);
                        break;
                    }
                    break;
                case 6:
                    sendCmd(PLC_CMD.BELT_STOP);
                    Thread.sleep(200);
                    sendCmd(PLC_CMD.BELT_BACKWARD);
                    delayTime = (long) this.forwardFirstLightTime;
                    flag = 0;
                    lWaitStartTime = System.currentTimeMillis();
                    while (true) {
                        event = (String) this.eventFIFOQueue.pop(getRealDelayTime(lWaitStartTime, delayTime));
                        if (event != null) {
                            logger.debug(event);
                            if ("CMD:STOP".equals(event)) {
                                step = -1;
                            } else if ("CMD:PAUSE".equals(event)) {
                                step = 8;
                            } else if ("EVENT:FIRST_LIGHT_ON".equals(event)) {
                                if ((flag & 1) == 0) {
                                    hsmpParam = new HashMap();
                                    hsmpParam.put("type", "FIRST_LIGHT_ON_BACKWARD");
                                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                                    flag |= 1;
                                }
                                if (step != 2) {
                                    step = 2;
                                    controlCheat = false;
                                }
                                delayTime = 0;
                            }
                        }
                        if (step == 6) {
                            step = 2;
                            controlCheat = false;
                        }
                        sendCmd(PLC_CMD.BELT_STOP);
                        Thread.sleep(lRemovingTimeout);
                        do {
                            event = (String) this.eventFIFOQueue.pop(0);
                            if (event == null) {
                                break;
                            }
                            logger.debug(event);
                            if ("CMD:STOP".equals(event)) {
                                step = -1;
                                break;
                            }
                        } while (!"CMD:PAUSE".equals(event));
                        step = 8;
                        break;
                    }
                case 8:
                    sendCmd(PLC_CMD.BELT_STOP);
                    delayTime = (long) this.recyclePauseTime;
                    lWaitStartTime = System.currentTimeMillis();
                    do {
                        event = (String) this.eventFIFOQueue.pop(getRealDelayTime(lWaitStartTime, delayTime));
                        if (event != null) {
                            logger.debug(event);
                        }
                        step = -1;
                        break;
                    } while (!"CMD:STOP".equals(event));
                    step = -1;
                default:
                    break;
            }
        }
    }

    public void init() {
        synchronized (this) {
            try {
                if (!(this.machineComm == null || this.machineComm.isOpen())) {
                    this.machineComm = null;
                }
                if (this.machineComm == null) {
                    String comm = SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM"));
                    String band = SysConfig.get("COM.PLC.BAND");
                    long comPlcRecvTimeout = Long.parseLong(SysConfig.get("COM.PLC.RECV.TIMEOUT"));
                    if (COM_PLC_VERSION.K5V1.equalsIgnoreCase(this.COM_PLC_PROTOCOL)) {
                        this.machineComm = new K5V1SerialComm(comm, band, this.commRecvCallBack, this.plcCommStateCallBack, this.CmdResultFIFOQueue);
                    } else if (COM_PLC_VERSION.MCUV1.equalsIgnoreCase(this.COM_PLC_PROTOCOL)) {
                        this.machineComm = new MCUSerialComm(comm, band, this.commRecvCallBack, this.plcCommStateCallBack, Integer.parseInt(SysConfig.get("COM.PLC.RECV.ERRORTIMES")), comPlcRecvTimeout, this.CmdResultFIFOQueue);
                    }
                    if (this.machineComm == null) {
                        return;
                    }
                    this.plcSortAlarm = HardwareAlarmState.UNKNOWN;
                    this.sortErrorTimes = 0;
                    this.beltState = 0;
                    this.plcResetThread = new PLCResetThread();
                    SysGlobal.execute(this.plcResetThread);
                    serviceEnableChange();
                    SysGlobal.execute(new Thread() {
                        public void run() {
                            try {
                                PLC104CCommEntity.this.PLCAuto();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    if (this.clearFogEnable) {
                        SysGlobal.execute(new ClearFogThread());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getCmdName(byte[] cmd) {
        if (cmd == PLC_CMD.MAT_TO_PET) {
            return "MAT_TO_PET";
        }
        if (cmd == PLC_CMD.MAT_TO_CAN) {
            return "MAT_TO_CAN";
        }
        if (cmd == PLC_CMD.BELT_FORWARD) {
            return "BELT_FORWARD";
        }
        if (cmd == PLC_CMD.BELT_BACKWARD) {
            return "BELT_BACKWARD";
        }
        if (cmd == PLC_CMD.BELT_STOP) {
            return "BELT_STOP";
        }
        if (cmd == PLC_CMD.LIGHT_ON) {
            return "LIGHT_ON";
        }
        if (cmd == PLC_CMD.LIGHT_FLASH) {
            return "LIGHT_FLASH";
        }
        if (cmd == PLC_CMD.LIGHT_OFF) {
            return "LIGHT_OFF";
        }
        if (cmd == PLC_CMD.STORAGE_DOOR_OPEN) {
            return "STORAGE_DOOR_OPEN";
        }
        if (cmd == PLC_CMD.QRCODE_LIGHT_ON) {
            return "QRCODE_LIGHT_ON";
        }
        if (cmd == PLC_CMD.QRCODE_LIGHT_OFF) {
            return "QRCODE_LIGHT_OFF";
        }
        if (cmd == PLC_CMD.CHECK_FIRST_LIGHT_STATE) {
            return "CHECK_FIRST_LIGHT_STATE";
        }
        if (cmd == PLC_CMD.CHECK_THIRD_LIGHT_STATE) {
            return "CHECK_THIRD_LIGHT_STATE";
        }
        if (cmd == K5V1SerialComm.PLC_CMD.QUERY_STATE) {
            return "QUERY_LIGHT_STATE";
        }
        return null;
    }

    public PLC104CCommEntity() {
        this.hasLightCmd = false;
        this.hasMetalLight = false;
        this.clearFogEnable = false;
        this.COM_PLC_PROTOCOL = null;
        this.sortErrorTimes = 0;
        this.plcSortAlarm = HardwareAlarmState.UNKNOWN;
        this.plcCommAlarm = HardwareAlarmState.UNKNOWN;
        this.plcResetThread = null;
        this.plcResponseFIFOQueue = new FIFOQueue();
        this.CmdResultFIFOQueue = new FIFOQueue();
        this.beltState = 0;
        this.materialState = 0;
        this.currMaterialState = 0;
        this.lCmdInterval = 0;
        this.isPutBottle = false;
        this.lBeltRollingContinueTime = 0;
        this.lWaitBeltStopTime = 0;
        this.forwardFirstLightTime = 2000;
        this.forwardSecondLightTime = 2000;
        this.forwardThirdLightTime = 6000;
        this.recyclePauseTime = 60000;
        this.secondLightDelay = 0;
        this.lBeltStateTime = 0;
        this.lWaitSortChangedTime = 0;
        this.heartBeatEnable = false;
        this.eventFIFOQueue = new FIFOQueue();
        this.machineComm = null;
        this.buttonEnable = true;
        this.serviceEnable = false;
        this.idleQueryEnable = true;
        this.lightCheck = true;
        this.taskActionMonitor = new TaskAction() {
            public void execute() {
                PLC104CCommEntity.this.checkPlcError();
                if (!PLC104CCommEntity.this.isInCheck && PLC104CCommEntity.this.materialState == 2 && PLC104CCommEntity.this.lWaitSortChangedTime > 0 && PLC104CCommEntity.this.currMaterialState != 2 && PLC104CCommEntity.this.lWaitSortChangedTime + Long.parseLong(SysConfig.get("COM.PLC.SORT.TIMEOUT")) < System.currentTimeMillis()) {
                    PLC104CCommEntity.this.lWaitSortChangedTime = System.currentTimeMillis();
                    PLC104CCommEntity.this.sortErrorTimes = PLC104CCommEntity.this.sortErrorTimes + 1;
                    try {
                        PLC104CCommEntity.this.sendComm(PLC_CMD.MAT_TO_CAN);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!PLC104CCommEntity.this.isInCheck && PLC104CCommEntity.this.materialState == 1 && PLC104CCommEntity.this.lWaitSortChangedTime > 0 && PLC104CCommEntity.this.currMaterialState != 1 && PLC104CCommEntity.this.lWaitSortChangedTime + Long.parseLong(SysConfig.get("COM.PLC.SORT.TIMEOUT")) < System.currentTimeMillis()) {
                    PLC104CCommEntity.this.lWaitSortChangedTime = System.currentTimeMillis();
                    PLC104CCommEntity.this.sortErrorTimes = PLC104CCommEntity.this.sortErrorTimes + 1;
                    try {
                        PLC104CCommEntity.this.sendComm(PLC_CMD.MAT_TO_PET);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                if (PLC104CCommEntity.this.lWaitBeltStopTime > 0) {
                    if (PLC104CCommEntity.this.beltState != 0 && PLC104CCommEntity.this.lWaitBeltStopTime + (Long.parseLong(SysConfig.get("COM.PLC.BELTROLLING.TIMEOUT")) * PLC104CCommEntity.BUTTON_TIME_INTERVAL) < System.currentTimeMillis()) {
                        try {
                            PLC104CCommEntity.this.sendCmd(PLC_CMD.BELT_STOP);
                            PLC104CCommEntity.this.lWaitBeltStopTime = 0;
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (PLC104CCommEntity.this.beltState == 1 && System.currentTimeMillis() - PLC104CCommEntity.this.lBeltRollingContinueTime > PLC104CCommEntity.BUTTON_TIME_INTERVAL && PLC104CCommEntity.this.lWaitBeltStopTime + (Long.parseLong(SysConfig.get("COM.PLC.BELTROLLING.TIMEOUT")) * PLC104CCommEntity.BUTTON_TIME_INTERVAL) > System.currentTimeMillis()) {
                        try {
                            PLC104CCommEntity.this.lBeltRollingContinueTime = System.currentTimeMillis();
                        } catch (Exception e222) {
                            e222.printStackTrace();
                        }
                    }
                }
            }
        };
        this.lLastCmdTime = 0;
        this.isPlcError = false;
        this.buttonTime = 0;
        this.isInCheck = false;
        this.lFirstLightTime = 0;
        this.lSecondLightTime = 0;
        this.lThirdLightTime = 0;
        this.lightShakeTime = 500;
        this.plcCommStateCallBack = new PLCCommStateCallBack();
        this.commRecvCallBack = new CommRecvCallBack() {
            public void apply(byte[] data) {
                PLC104CCommEntity.this.plcResponseFIFOQueue.push(data);
            }
        };
        this.clearFogEnable = "TRUE".equalsIgnoreCase(SysConfig.get("CLEAR.FOG.ENABLE"));
        this.hasLightCmd = true;
        this.machineComm = null;
        this.idleQueryEnable = "TRUE".equalsIgnoreCase(SysConfig.get("COM.PLC.IDLE.QUERY.ENABLE"));
        this.buttonEnable = "TRUE".equalsIgnoreCase(SysConfig.get("COM.PLC.BUTTON.ENABLE"));
        this.COM_PLC_PROTOCOL = SysConfig.get("COM.PLC.PROTOCOL." + SysConfig.get("RVM.MODE"));
        if (!StringUtils.isBlank(SysConfig.get("COM.PLC.CMD.INTERVAL"))) {
            if (COM_PLC_VERSION.K5V1.equalsIgnoreCase(this.COM_PLC_PROTOCOL)) {
                this.lCmdInterval = 0;
            } else if (COM_PLC_VERSION.MCUV1.equalsIgnoreCase(this.COM_PLC_PROTOCOL)) {
                this.lCmdInterval = 0;
            }
        }
        try {
            if (!StringUtils.isBlank(SysConfig.get("COM.PLC.SECOND.LIGHT.DELAY"))) {
                this.secondLightDelay = Long.parseLong(SysConfig.get("COM.PLC.SECOND.LIGHT.DELAY"));
            }
            this.forwardSecondLightTime = Integer.parseInt(SysConfig.get("COM.PLC.FORWARD.SECOND"));
            this.forwardThirdLightTime = Integer.parseInt(SysConfig.get("COM.PLC.FORWARD.THIRD"));
            this.forwardFirstLightTime = Integer.parseInt(SysConfig.get("COM.PLC.FORWARD.FIRST"));
            this.recyclePauseTime = Integer.parseInt(SysConfig.get("COM.PLC.RECYCLE.PAUSE"));
        } catch (Exception e) {
        }
        SysGlobal.execute(new ExecuteThread());
        TickTaskThread.getTickTaskThread().register(this.taskActionMonitor, 0.2d, false);
    }

    private void sendCmd(byte[] cmd) throws Exception {
        long lCurrentTime;
        if (this.isInCheck) {
            lCurrentTime = System.currentTimeMillis();
            if (this.lLastCmdTime > 0 && this.lCmdInterval > 0) {
                try {
                    if (this.lLastCmdTime + this.lCmdInterval > lCurrentTime) {
                        Thread.sleep((this.lLastCmdTime + this.lCmdInterval) - lCurrentTime);
                    }
                } catch (Exception e) {
                    logger.debug(e);
                }
            }
            sendComm(cmd);
            this.lLastCmdTime = System.currentTimeMillis();
            return;
        }
        if (cmd == PLC_CMD.BELT_FORWARD) {
            if (this.beltState == 2) {
                sendCmd(PLC_CMD.BELT_STOP);
            }
            this.lWaitBeltStopTime = System.currentTimeMillis();
            this.beltState = 1;
        }
        if (cmd == PLC_CMD.BELT_BACKWARD) {
            if (this.beltState == 1) {
                sendCmd(PLC_CMD.BELT_STOP);
            }
            this.lWaitBeltStopTime = System.currentTimeMillis();
            this.beltState = 2;
        }
        if (cmd == PLC_CMD.BELT_STOP) {
            this.beltState = 0;
            this.lWaitBeltStopTime = 0;
        }
        lCurrentTime = System.currentTimeMillis();
        if (this.lLastCmdTime > 0 && this.lCmdInterval > 0) {
            try {
                if (this.lLastCmdTime + this.lCmdInterval > lCurrentTime) {
                    Thread.sleep((this.lLastCmdTime + this.lCmdInterval) - lCurrentTime);
                }
            } catch (Exception e2) {
                logger.debug(e2);
            }
        }
        lCurrentTime = System.currentTimeMillis();
        if (cmd == PLC_CMD.BELT_FORWARD || cmd == PLC_CMD.BELT_BACKWARD || cmd == PLC_CMD.BELT_STOP) {
            long lPrevBeltStateTime = this.lBeltStateTime;
            long lBeltCmdInterval = Long.parseLong(SysConfig.get("COM.PLC.CMD.INTERVAL.BELT"));
            if (lPrevBeltStateTime > 0 && lBeltCmdInterval > 0 && lPrevBeltStateTime + lBeltCmdInterval > lCurrentTime) {
                Thread.sleep((lPrevBeltStateTime + lBeltCmdInterval) - lCurrentTime);
            }
            this.lBeltStateTime = System.currentTimeMillis();
        }
        logger.debug("WRITE:" + EncryptUtils.byte2hex(cmd) + "|" + new String(cmd) + "|" + getCmdName(cmd) + " at " + DateUtils.formatDatetime(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        try {
            sendComm(cmd);
            long lBeltRepeatCmdInterval = Long.parseLong(SysConfig.get("COM.PLC.CMD.BELTREPEAT.INTERVAL"));
            if (lBeltRepeatCmdInterval > 0 && (cmd == PLC_CMD.BELT_FORWARD || cmd == PLC_CMD.BELT_BACKWARD || cmd == PLC_CMD.BELT_STOP)) {
                SysGlobal.execute(new Thread() {
                    int beltState;
                    byte[] cmd;
                    long lDelayTime;

                    public Thread setParam(byte[] cmd, int beltState, long lDelayTime) {
                        this.cmd = cmd;
                        this.lDelayTime = lDelayTime;
                        this.beltState = beltState;
                        return this;
                    }

                    public void run() {
                        try {
                            Thread.sleep(this.lDelayTime);
                            if (this.beltState == PLC104CCommEntity.this.beltState) {
                                PLC104CCommEntity.this.sendComm(this.cmd);
                                PLC104CCommEntity.this.lBeltStateTime = System.currentTimeMillis();
                                PLC104CCommEntity.this.lLastCmdTime = System.currentTimeMillis();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.setParam(cmd, this.beltState, lBeltRepeatCmdInterval));
            }
            this.lLastCmdTime = System.currentTimeMillis();
        } catch (Exception e22) {
            logger.debug(e22);
            throw e22;
        }
    }

    public String execute(String cmd, String json) throws Exception {
        logger.debug("CMD:" + cmd);
        init();
        synchronized (this) {
            if (this.machineComm == null || !this.machineComm.isOpen()) {
                return null;
            }
            try {
                if ("CHECK:START".equalsIgnoreCase(cmd)) {
                    this.isInCheck = true;
                    serviceEnableChange();
                }
                if ("CHECK:END".equalsIgnoreCase(cmd)) {
                    this.isInCheck = false;
                    serviceEnableChange();
                }
                if (!this.isInCheck) {
                    if ("RECYCLE_START".equalsIgnoreCase(cmd)) {
                        this.eventFIFOQueue.push("CMD:START");
                        this.serviceEnable = true;
                        this.lightCheck = false;
                        serviceEnableChange();
                    }
                    if ("RECYCLE_END".equalsIgnoreCase(cmd)) {
                        this.eventFIFOQueue.push("CMD:STOP");
                        this.serviceEnable = false;
                        this.lightCheck = true;
                        serviceEnableChange();
                    }
                    if ("RECYCLE_PAUSE".equalsIgnoreCase(cmd)) {
                        this.eventFIFOQueue.push("CMD:PAUSE");
                    }
                    if ("RESET".equalsIgnoreCase(cmd)) {
                        return null;
                    } else if ("RECYCLE_ENABLE".equalsIgnoreCase(cmd)) {
                        HashMap<String, String> hsmpParam = JSONUtils.toHashMap(json);
                        if (hsmpParam != null) {
                            if (ServiceName.PET.equalsIgnoreCase((String) hsmpParam.get("STUFF"))) {
                                this.materialState = 1;
                            } else {
                                this.materialState = 2;
                            }
                        }
                        this.eventFIFOQueue.push("CMD:RECYCLE");
                        return null;
                    } else if ("RECYCLE_DISABLE".equalsIgnoreCase(cmd)) {
                        this.eventFIFOQueue.push("CMD:REJECT");
                        return null;
                    } else if ("STORAGE_DOOR_OPEN".equalsIgnoreCase(cmd)) {
                        sendCmd(PLC_CMD.STORAGE_DOOR_OPEN);
                        return null;
                    } else if ("QRCODE_LIGHT_ON".equalsIgnoreCase(cmd)) {
                        sendCmd(PLC_CMD.QRCODE_LIGHT_ON);
                        return null;
                    } else if ("QRCODE_LIGHT_OFF".equalsIgnoreCase(cmd)) {
                        sendCmd(PLC_CMD.QRCODE_LIGHT_OFF);
                        return null;
                    } else {
                        long comPlcRecvTimeout;
                        byte[] buffer;
                        String event;
                        int errorCount;
                        HashMap hsmpParam2;
                        String[] time;
                        if ("RVM_POWER_OFF_ENABLE".equalsIgnoreCase(cmd)) {
                            comPlcRecvTimeout = 10 * Long.parseLong(SysConfig.get("COM.PLC.RECV.TIMEOUT"));
                            buffer = new byte[]{MachineComm.TimeControl.TIME_DISABLE, (byte) 0, (byte) 0, (byte) 0};
                            event = null;
                            errorCount = 0;
                            while (errorCount < 10) {
                                sendComm(buffer);
                                do {
                                    event = (String) this.CmdResultFIFOQueue.pop(comPlcRecvTimeout);
                                    if (event == null) {
                                        break;
                                    }
                                } while (!event.equals("POWER_OFF_DISABLE_OK"));
                                errorCount++;
                                if (event != null) {
                                    break;
                                }
                            }
                            hsmpParam2 = JSONUtils.toHashMap(json);
                            time = ((String) hsmpParam2.get("POWER_ON_TIME")).split(":");
                            buffer[0] = MachineComm.TimeControl.TIME_POWER_ON;
                            buffer[1] = (byte) (Integer.parseInt(time[0]) & MotionEventCompat.ACTION_MASK);
                            buffer[2] = (byte) (Integer.parseInt(time[1]) & MotionEventCompat.ACTION_MASK);
                            buffer[3] = (byte) 0;
                            errorCount = 0;
                            while (errorCount < 5) {
                                sendComm(buffer);
                                do {
                                    event = (String) this.CmdResultFIFOQueue.pop(comPlcRecvTimeout);
                                    if (event == null) {
                                        break;
                                    }
                                } while (!event.equals("POWER_ON_TIME_OK"));
                                errorCount++;
                                if (event != null) {
                                    break;
                                }
                            }
                            if (event == null) {
                                return null;
                            }
                            time = ((String) hsmpParam2.get("POWER_OFF_TIME")).split(":");
                            buffer[0] = MachineComm.TimeControl.TIME_POWER_OFF;
                            buffer[1] = (byte) (Integer.parseInt(time[0]) & MotionEventCompat.ACTION_MASK);
                            buffer[2] = (byte) (Integer.parseInt(time[1]) & MotionEventCompat.ACTION_MASK);
                            buffer[3] = (byte) 0;
                            errorCount = 0;
                            while (errorCount < 5) {
                                sendComm(buffer);
                                do {
                                    event = (String) this.CmdResultFIFOQueue.pop(comPlcRecvTimeout);
                                    if (event == null) {
                                        break;
                                    }
                                } while (!event.equals("POWER_OFF_TIME_OK"));
                                errorCount++;
                                if (event != null) {
                                    break;
                                }
                            }
                            if (event == null) {
                                return null;
                            }
                            buffer = new byte[]{MachineComm.TimeControl.TIME_ENABLE, (byte) 0, (byte) 0, (byte) 0};
                            errorCount = 0;
                            while (errorCount < 5) {
                                sendComm(buffer);
                                do {
                                    event = (String) this.CmdResultFIFOQueue.pop(comPlcRecvTimeout);
                                    if (event == null) {
                                        break;
                                    }
                                } while (!event.equals("POWER_OFF_ENABLE_OK"));
                                errorCount++;
                                if (event != null) {
                                    break;
                                }
                            }
                            if (event == null) {
                                return null;
                            }
                        }
                        if ("RVM_CLOCK_CALIBRATION".equalsIgnoreCase(cmd) && "true".equalsIgnoreCase(SysConfig.get("COM.PLC.CLOCK.CALIBRATION"))) {
                            byte[] bufferClockCalubration = new byte[4];
                            time = new SimpleDateFormat("HH:mm:ss").format(new Date()).split(":");
                            bufferClockCalubration[0] = (byte) -120;
                            bufferClockCalubration[1] = (byte) (Integer.parseInt(time[0]) & MotionEventCompat.ACTION_MASK);
                            bufferClockCalubration[2] = (byte) (Integer.parseInt(time[1]) & MotionEventCompat.ACTION_MASK);
                            bufferClockCalubration[3] = (byte) (Integer.parseInt(time[2]) & MotionEventCompat.ACTION_MASK);
                            sendComm(bufferClockCalubration);
                        }
                        if ("RVM_POWER_OFF_DISABLE".equalsIgnoreCase(cmd)) {
                            comPlcRecvTimeout = 10 * Long.parseLong(SysConfig.get("COM.PLC.RECV.TIMEOUT"));
                            buffer = new byte[]{MachineComm.TimeControl.TIME_DISABLE, (byte) 0, (byte) 0, (byte) 0};
                            errorCount = 0;
                            while (errorCount < 5) {
                                sendComm(buffer);
                                do {
                                    event = (String) this.CmdResultFIFOQueue.pop(comPlcRecvTimeout);
                                    if (event == null) {
                                        break;
                                    }
                                } while (!event.equals("POWER_OFF_DISABLE_OK"));
                                errorCount++;
                                if (event != null) {
                                    break;
                                }
                            }
                        }
                        int rvm_alive_time;
                        if (this.heartBeatEnable && "HEART_BEAT".equalsIgnoreCase(cmd)) {
                            if ("TRUE".equalsIgnoreCase(SysConfig.get("RVM.ALIVE.TIME.ENABLE"))) {
                                String RVM_ALIVE_TIME = SysConfig.get("RVM.ALIVE.TIME");
                                rvm_alive_time = 0;
                                if (!StringUtils.isBlank(RVM_ALIVE_TIME)) {
                                    rvm_alive_time = Integer.parseInt(RVM_ALIVE_TIME);
                                }
                                hsmpParam2 = new HashMap();
                                hsmpParam2.put("RVM_ALIVE_TIME", Integer.toString(rvm_alive_time));
                                execute("SET_RVM_ALIVE_TIME", JSONUtils.toJSON(hsmpParam2));
                            }
                            return null;
                        } else if ("SET_RVM_ALIVE_TIME".equalsIgnoreCase(cmd)) {
                            comPlcRecvTimeout = 10 * Long.parseLong(SysConfig.get("COM.PLC.RECV.TIMEOUT"));
                            rvm_alive_time = 0;
                            if ("TRUE".equalsIgnoreCase(SysConfig.get("RVM.ALIVE.TIME.ENABLE"))) {
                                hsmpParam2 = JSONUtils.toHashMap(json);
                                if (!(hsmpParam2 == null || StringUtils.isBlank((String) hsmpParam2.get("RVM_ALIVE_TIME")))) {
                                    rvm_alive_time = Integer.parseInt((String) hsmpParam2.get("RVM_ALIVE_TIME"));
                                }
                            }
                            buffer = new byte[]{MachineComm.TimeControl.ALIVE_TIME,
                                    (byte) ((rvm_alive_time >> 16) & MotionEventCompat.ACTION_MASK),
                                    (byte) ((rvm_alive_time >> 8) & MotionEventCompat.ACTION_MASK),
                                    (byte) (rvm_alive_time & MotionEventCompat.ACTION_MASK)};
                            boolean hasResult = false;
                            errorCount = 0;
                            while (!hasResult && errorCount < 5) {
                                sendComm(buffer);
                                do {
                                    event = (String) this.CmdResultFIFOQueue.pop(comPlcRecvTimeout);
                                    if (event == null) {
                                        break;
                                    }
                                } while (!event.equals("RVM_ALIVE_TIME_OK"));
                                hasResult = true;
                                errorCount++;
                            }
                            if (hasResult && rvm_alive_time > 0) {
                                this.heartBeatEnable = true;
                            }
                            if (hasResult && rvm_alive_time > 0) {
                                int heart_beat_times = 0;
                                if (!StringUtils.isBlank(SysConfig.get("RVM.ALIVE.TIME.MAXTIMES"))) {
                                    heart_beat_times = Integer.parseInt(SysConfig.get("RVM.ALIVE.TIME.MAXTIMES"));
                                }
                                buffer = new byte[]{MachineComm.TimeControl.ALIVE_TIME_MAXTIMES, (byte) 0, (byte) 0, (byte) (heart_beat_times & MotionEventCompat.ACTION_MASK)};
                                hasResult = false;
                                errorCount = 0;
                                while (!hasResult && errorCount < 5) {
                                    sendComm(buffer);
                                    do {
                                        event = (String) this.CmdResultFIFOQueue.pop(comPlcRecvTimeout);
                                        if (event == null) {
                                            break;
                                        }
                                    } while (!event.equals("RVM_ALIVE_TIME_MAXTIMES_OK"));
                                    hasResult = true;
                                    errorCount++;
                                }
                            }
                            return null;
                        } else if ("CLEAR_RVM_ALIVE_TIME".equalsIgnoreCase(cmd)) {
                            comPlcRecvTimeout = 10 * Long.parseLong(SysConfig.get("COM.PLC.RECV.TIMEOUT"));
                            buffer = new byte[]{MachineComm.TimeControl.ALIVE_TIME, (byte) 0, (byte) 0, (byte) 0};
                            errorCount = 0;
                            while (errorCount < 5) {
                                sendComm(buffer);
                                do {
                                    event = (String) this.CmdResultFIFOQueue.pop(comPlcRecvTimeout);
                                    if (event == null) {
                                        break;
                                    }
                                } while (!event.equals("RVM_ALIVE_TIME_OK"));
                                errorCount++;
                                if (event != null) {
                                    break;
                                }
                            }
                            this.heartBeatEnable = false;
                            return null;
                        } else {
                            if ("QUERY_LIGHT_STATE".equalsIgnoreCase(cmd)) {
                                sendCmd(K5V1SerialComm.PLC_CMD.QUERY_STATE);
                            }
                            if ("SET_SECOND_LIGHT_ON".equalsIgnoreCase(cmd)) {
                                SysGlobal.execute(new Runnable() {
                                    public void run() {
                                        try {
                                            Thread.sleep(1500);
                                            PLC104CCommEntity.this.eventFIFOQueue.push("EVENT:BARCODE_READED");
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    }
                } else if ("PLC:CHECK_OPEN".equalsIgnoreCase(cmd)) {
                    String str;
                    if (this.machineComm == null || !this.machineComm.isOpen()) {
                        str = "FALSE";
                    } else {
                        str = "TRUE";
                    }
                    return str;
                } else if ("PLC:RESET".equalsIgnoreCase(cmd)) {
                    this.machineComm.resetRecvData();
                    return null;
                } else if ("PLC:BELT_FORWARD".equalsIgnoreCase(cmd)) {
                    sendCmd(PLC_CMD.BELT_FORWARD);
                    return null;
                } else if ("PLC:BELT_BACKWARD".equalsIgnoreCase(cmd)) {
                    sendCmd(PLC_CMD.BELT_BACKWARD);
                    return null;
                } else if ("PLC:BELT_STOP".equalsIgnoreCase(cmd)) {
                    sendCmd(PLC_CMD.BELT_STOP);
                    return null;
                } else if ("PLC:LIGHT_ON".equalsIgnoreCase(cmd)) {
                    sendCmd(PLC_CMD.LIGHT_ON);
                    return null;
                } else if ("PLC:LIGHT_FLASH".equalsIgnoreCase(cmd)) {
                    if ("TRUE".equalsIgnoreCase(SysConfig.get("SET.PLC.LIGHT.FLASH"))) {
                        sendCmd(PLC_CMD.LIGHT_FLASH);
                    }
                    return null;
                } else if ("PLC:LIGHT_OFF".equalsIgnoreCase(cmd)) {
                    sendCmd(PLC_CMD.LIGHT_OFF);
                    return null;
                } else if ("PLC:QRCODE_LIGHT_ON".equalsIgnoreCase(cmd)) {
                    sendCmd(PLC_CMD.QRCODE_LIGHT_ON);
                    return null;
                } else if ("PLC:QRCODE_LIGHT_OFF".equalsIgnoreCase(cmd)) {
                    sendCmd(PLC_CMD.QRCODE_LIGHT_OFF);
                    return null;
                } else if ("PLC:CHECK_THIRD_LIGHT_STATE".equalsIgnoreCase(cmd)) {
                    sendCmd(PLC_CMD.CHECK_THIRD_LIGHT_STATE);
                    return null;
                } else if ("PLC:STORAGE_DOOR_OPEN".equalsIgnoreCase(cmd)) {
                    sendCmd(PLC_CMD.STORAGE_DOOR_OPEN);
                    return null;
                } else if ("PLC:SORT_TO_PET".equalsIgnoreCase(cmd)) {
                    sendCmd(PLC_CMD.MAT_TO_PET);
                    return null;
                } else if ("PLC:SORT_TO_CAN".equalsIgnoreCase(cmd)) {
                    sendCmd(PLC_CMD.MAT_TO_CAN);
                    return null;
                } else if ("PLC:NONE".equalsIgnoreCase(cmd)) {
                    sendCmd(PLC_CMD.NONE);
                    return null;
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void checkPlcError() {
        if (this.machineComm != null) {
            boolean z;
            boolean oldPlcError = this.isPlcError;
            if (this.machineComm.isCommOK()) {
                z = false;
            } else {
                z = true;
            }
            this.isPlcError = z;
            if (!(this.isPlcError == oldPlcError || this.isPlcError)) {
                this.sortErrorTimes = 0;
            }
            HashMap<String, String> hsmpParam;
            if (this.isPlcError) {
                if (this.plcCommAlarm != HardwareAlarmState.ALARM) {
                    this.plcCommAlarm = HardwareAlarmState.ALARM;
                    hsmpParam = new HashMap();
                    hsmpParam.put("type", "PLC_COMM_ERROR");
                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                }
            } else if (this.plcCommAlarm != HardwareAlarmState.NORMAL) {
                this.plcCommAlarm = HardwareAlarmState.NORMAL;
                hsmpParam = new HashMap();
                hsmpParam.put("type", "PLC_COMM_ERROR_RECOVERY");
                ServiceGlobal.getCommEventQueye().push(hsmpParam);
            }
            checkPlcSortError();
        }
    }

    private void checkPlcSortError() {
        if (this.sortErrorTimes >= 5 && this.plcSortAlarm != HardwareAlarmState.ALARM) {
            this.plcSortAlarm = HardwareAlarmState.ALARM;
            HashMap<String, String> hsmpParam = new HashMap();
            hsmpParam.put("type", "PLC_SORT_ERROR");
            ServiceGlobal.getCommEventQueye().push(hsmpParam);
        }
        if (this.sortErrorTimes == 0 && this.plcSortAlarm != HardwareAlarmState.NORMAL) {
            this.plcSortAlarm = HardwareAlarmState.NORMAL;
           HashMap hsmpParam = new HashMap();
            hsmpParam.put("type", "PLC_SORT_ERROR_RECOVERY");
            ServiceGlobal.getCommEventQueye().push(hsmpParam);
        }
    }

    private void execute(byte[] buffer) throws Exception {
        int buttonSignal = buffer[StateMask.BUTTON_PUSH[0]] & StateMask.BUTTON_PUSH[1];
        int metalSignal = buffer[StateMask.METAL_LIGHT[0]] & StateMask.METAL_LIGHT[1];
        int firstLightSignal = buffer[StateMask.FIRST_LIGHT[0]] & StateMask.FIRST_LIGHT[1];
        if ((buffer[StateMask.FIRST_LIGHT[0]] & StateMask.FIRST_LIGHT[1]) == 0 && (buffer[StateMask.FIRST_LIGHT_ADDI[0]] & StateMask.FIRST_LIGHT_ADDI[1]) == 0) {
            firstLightSignal = 0;
        }
        int secondLightSignal = buffer[StateMask.SECOND_LIGHT[0]] & StateMask.SECOND_LIGHT[1];
        int thirdLightSignal = buffer[StateMask.THIRD_LIGHT[0]] & StateMask.THIRD_LIGHT[1];
        int sortToPetSignal = buffer[StateMask.MATERIAL_TO_PET[0]] & StateMask.MATERIAL_TO_PET[1];
        int sortToCanSignal = buffer[StateMask.MATERIAL_TO_CAN[0]] & StateMask.MATERIAL_TO_CAN[1];
        int secondLightFirst = buffer[StateMask.SECOND_LIGHT_FIRST[0]] & StateMask.SECOND_LIGHT_FIRST[1];
        if (secondLightSignal <= 0 || thirdLightSignal <= 0) {
            long lTime = System.currentTimeMillis();
            if (firstLightSignal > 0) {
                this.plcCommStateCallBack.firstLightErrorReset = true;
                if (this.lFirstLightTime + this.lightShakeTime < lTime) {
                    this.lFirstLightTime = lTime;
                } else {
                    firstLightSignal = 0;
                }
            }
            if (secondLightSignal > 0) {
                this.plcCommStateCallBack.secondLightErrorReset = true;
                if (this.lSecondLightTime + this.lightShakeTime < lTime) {
                    this.lSecondLightTime = lTime;
                } else {
                    secondLightSignal = 0;
                }
            }
            if (thirdLightSignal > 0) {
                this.plcCommStateCallBack.thirdLightErrorReset = true;
                if (this.lThirdLightTime + this.lightShakeTime < lTime) {
                    this.lThirdLightTime = lTime;
                } else {
                    thirdLightSignal = 0;
                }
            }
            HashMap<String, String> hsmpParam;
            if (this.isInCheck) {
                hsmpParam = new HashMap();
                hsmpParam.put("type", "CHECK");
                hsmpParam.put("comm", "PLC");
                hsmpParam.put("msg", "HAS_EVENT");
                ServiceGlobal.getCommEventQueye().push(hsmpParam);
                if (buttonSignal > 0) {
                    hsmpParam = new HashMap();
                    hsmpParam.put("type", "CHECK");
                    hsmpParam.put("comm", "PLC");
                    hsmpParam.put("msg", "RVM_BUTTON_PUSH");
                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                }
                if (firstLightSignal > 0) {
                    hsmpParam = new HashMap();
                    hsmpParam.put("type", "CHECK");
                    hsmpParam.put("comm", "PLC");
                    hsmpParam.put("msg", "FIRST_LIGHT_ON");
                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                }
                if (secondLightSignal > 0) {
                    hsmpParam = new HashMap();
                    hsmpParam.put("type", "CHECK");
                    hsmpParam.put("comm", "PLC");
                    hsmpParam.put("msg", "SECOND_LIGHT_ON");
                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                }
                if (thirdLightSignal > 0) {
                    hsmpParam = new HashMap();
                    hsmpParam.put("type", "CHECK");
                    hsmpParam.put("comm", "PLC");
                    hsmpParam.put("msg", "THIRD_LIGHT_ON");
                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                }
                if (sortToPetSignal > 0) {
                    hsmpParam = new HashMap();
                    hsmpParam.put("type", "CHECK");
                    hsmpParam.put("comm", "PLC");
                    hsmpParam.put("msg", "SORT_TO_PET");
                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                }
                if (sortToCanSignal > 0) {
                    hsmpParam = new HashMap();
                    hsmpParam.put("type", "CHECK");
                    hsmpParam.put("comm", "PLC");
                    hsmpParam.put("msg", "SORT_TO_CAN");
                    ServiceGlobal.getCommEventQueye().push(hsmpParam);
                    return;
                }
                return;
            }
            if (buttonSignal == 0 && firstLightSignal == 0 && secondLightSignal == 0 && thirdLightSignal == 0) {
                logger.debug("RECV:NONE");
            } else {
                logger.debug("RECV:" + (buttonSignal != 0 ? "BUTTON" : "") + ";" + (firstLightSignal != 0 ? "FIRST" : "") + ";" + (secondLightSignal != 0 ? "SECOND" : "") + ";" + (thirdLightSignal != 0 ? "THIRD" : "") + ";");
            }
            if (buttonSignal > 0) {
                if (this.buttonTime <= 0 || this.buttonTime + BUTTON_TIME_INTERVAL <= System.currentTimeMillis()) {
                    this.buttonTime = System.currentTimeMillis();
                } else {
                    buttonSignal = 0;
                }
            }
            if (!this.buttonEnable) {
                buttonSignal = 0;
            }
            if (sortToCanSignal > 0) {
                this.currMaterialState = 2;
                this.sortErrorTimes = 0;
                this.eventFIFOQueue.push("EVENT:SORT_TO_CAN");
                serviceEnableChange();
                if (this.materialState == 2) {
                    this.sortErrorTimes = 0;
                    checkPlcSortError();
                }
            }
            if (sortToPetSignal > 0) {
                this.currMaterialState = 1;
                this.sortErrorTimes = 0;
                this.eventFIFOQueue.push("EVENT:SORT_TO_PET");
                serviceEnableChange();
                if (this.materialState == 1) {
                    this.sortErrorTimes = 0;
                    checkPlcSortError();
                }
            }
            if (buttonSignal > 0) {
                hsmpParam = new HashMap();
                hsmpParam.put("type", "RVM_BUTTON_PUSH");
                ServiceGlobal.getCommEventQueye().push(hsmpParam);
                return;
            }
            if (this.hasMetalLight && metalSignal > 0) {
                this.eventFIFOQueue.push("EVENT:METAL_LIGHT_ON");
            }
            if (firstLightSignal > 0) {
                this.isPutBottle = true;
                this.eventFIFOQueue.push("EVENT:FIRST_LIGHT_ON");
            }
            if (secondLightSignal > 0) {
                this.isPutBottle = true;
                if (this.secondLightDelay <= 0) {
                    this.eventFIFOQueue.push("EVENT:SECOND_LIGHT_ON");
                } else {
                    SysGlobal.execute(new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(PLC104CCommEntity.this.secondLightDelay);
                            } catch (Exception e) {
                            }
                            PLC104CCommEntity.this.eventFIFOQueue.push("EVENT:SECOND_LIGHT_ON");
                        }
                    });
                }
            }
            if (thirdLightSignal > 0) {
                this.eventFIFOQueue.push("EVENT:THIRD_LIGHT_ON");
                return;
            }
            return;
        }
        byte[] bufferSecondLight = new byte[4];
        byte[] bufferThirdLight = new byte[4];
        System.arraycopy(buffer, 0, bufferSecondLight, 0, buffer.length);
        System.arraycopy(buffer, 0, bufferThirdLight, 0, buffer.length);
        int i = StateMask.SECOND_LIGHT[0];
        bufferSecondLight[i] = (byte) (bufferSecondLight[i] ^ StateMask.SECOND_LIGHT[1]);
        i = StateMask.THIRD_LIGHT[0];
        bufferThirdLight[i] = (byte) (bufferThirdLight[i] ^ StateMask.THIRD_LIGHT[1]);
        if (secondLightFirst > 0) {
            execute(bufferSecondLight);
            execute(bufferThirdLight);
            return;
        }
        execute(bufferSecondLight);
        execute(bufferThirdLight);
    }

    private void sendComm(byte[] cmd) throws Exception {
        if (cmd[0] == (byte) -120 || cmd[0] == MachineComm.TimeControl.TIME_POWER_ON ||
                cmd[0] == MachineComm.TimeControl.TIME_POWER_OFF || cmd[0] == MachineComm.TimeControl.TIME_DISABLE ||
                cmd[0] == MachineComm.TimeControl.TIME_ENABLE || cmd[0] == MachineComm.TimeControl.ALIVE_TIME || cmd[0] == MachineComm.TimeControl.ALIVE_TIME_MAXTIMES) {
            logger.debug("WRITE:" + EncryptUtils.byte2hex(cmd) + " at " + DateUtils.formatDatetime(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        }
        if (COM_PLC_VERSION.MCUV1.equalsIgnoreCase(this.COM_PLC_PROTOCOL)) {
            sendToMCU(cmd);
        } else if (COM_PLC_VERSION.K5V1.equalsIgnoreCase(this.COM_PLC_PROTOCOL)) {
            sendToK5V1(cmd);
        }
    }

    private void sendToK5V1(byte[] cmd) throws Exception {
        if (cmd == PLC_CMD.CHECK_FIRST_LIGHT_STATE || cmd == PLC_CMD.CHECK_THIRD_LIGHT_STATE) {
            cmd = K5V1SerialComm.PLC_CMD.QUERY_STATE;
        }
        if (cmd != PLC_CMD.NONE) {
            this.machineComm.sendCmd(cmd);
        }
    }

    private void sendToMCU(byte[] cmd) throws Exception {
        if (cmd == PLC_CMD.CHECK_FIRST_LIGHT_STATE) {
            execute(this.machineComm.getState());
        } else if (cmd == PLC_CMD.CHECK_THIRD_LIGHT_STATE) {
            execute(this.machineComm.getState());
        } else {
            if (cmd[0] == (byte) -120 || cmd[0] == MachineComm.TimeControl.TIME_POWER_ON || cmd[0] == MachineComm.TimeControl.TIME_POWER_OFF) {
                byte[] newCmd = new byte[4];
                newCmd[0] = cmd[0];
                for (int i = 1; i < 4; i++) {
                    int v = cmd[i] & MotionEventCompat.ACTION_MASK;
                    newCmd[i] = (byte) (((v / 10) * 16) + (v % 10));
                }
                cmd = newCmd;
            }
            this.machineComm.sendCmd(cmd);
        }
    }

    private void serviceEnableChange() {
        if (this.machineComm != null) {
            MachineComm machineComm = this.machineComm;
            boolean z = this.isInCheck || this.idleQueryEnable || this.serviceEnable;
            machineComm.serviceEnable(z);
        }
    }

    protected static long getRealDelayTime(long lStartWaitTime, long delayTime) {
        if (delayTime == -1 || delayTime == 0) {
            return delayTime;
        }
        long lTime = System.currentTimeMillis();
        if (lStartWaitTime + delayTime < lTime) {
            return 0;
        }
        return (lStartWaitTime + delayTime) - lTime;
    }
}
