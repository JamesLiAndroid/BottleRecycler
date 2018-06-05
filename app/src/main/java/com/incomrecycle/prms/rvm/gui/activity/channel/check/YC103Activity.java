package com.incomrecycle.prms.rvm.gui.activity.channel.check;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.internal.view.SupportMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.queue.FIFOQueue;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.QRDecodeHelper;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelAdvancedActivity;
import com.incomrecycle.prms.rvm.gui.camera.CameraManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class YC103Activity extends BaseActivity {
    private int TAKE_MAX_TIMES;
    String backward;
    String barcodeLightOff;
    String barcodeLightOn;
    String barcodeRead;
    String cameraCheck;
    CameraManager cameraManager = new CameraManager();
    String closeDoor;
    Activity context = this;
    int currentId = 0;
    int defaultColor = 0;
    boolean enablePush = false;
    FIFOQueue eventQueuePLC = new FIFOQueue();
    FIFOQueue eventQueuePrinter1 = new FIFOQueue();
    FIFOQueue eventQueuePrinter2 = new FIFOQueue();
    String firstLight;
    String forward;
    Handler handler;
    HashMap<String, TestResult> hsmpTestResult = new HashMap();
    boolean isAutoCheckEnable;
    boolean isAutoChecking;
    String lightFlash;
    String lightOff;
    String lightOn;
    List listOpt = new ArrayList();
    String openDoor;
    String openStorageDoor;
    String optr = null;
    String recycleCheck;
    String rvmCode = null;
    String secondLight;
    private String staffPermission = null;
    private int takePictureCount;
    private int takePictureRemainedTimes;
    private String[][] testItemSet;
    String thirdLight;
    String transfercardRead;

    private static class MsgWhat {
        private static int DISABLE_CONFIRM = 3;
        private static int ENABLE_CONFIRM = 2;
        private static int SHOW_CAMERA = 5;
        private static int SHOW_COLOR = 4;
        private static int SHOW_MSG = 0;
        private static int SHOW_RESET = 1;

        private MsgWhat() {
        }
    }

    private class RecycleAction {
        public static final int CLOSE_DOOR = 7;
        public static final int DONE = 8;
        public static final int END = 0;
        public static final int ERROR = -1;
        public static final int OPEN_DOOR = 6;
        public static final int PUSH_BUTTON = 1;
        public static final int PUT_BOTTLE = 2;
        public static final int RECYCLE_BOTTLE = 4;
        public static final int REJECT_BOTTLE = 5;
        public static final int SCAN_BOTTLE = 3;

        private RecycleAction() {
        }
    }

    static final class TestItem {
        static final String BACKWARD = "BACKWARD";
        static final String BARCODE_READ = "BARCODE_READ";
        static final String CAMERA_CHECK = "CAMERA_CHECK";
        static final String CLOSE_DOOR = "CLOSE_DOOR";
        static final String FIRST_LIGHT = "FIRST_LIGHT";
        static final String FORWARD = "FORWARD";
        static final String LIGHT_FLASH = "LIGHT_FLASH";
        static final String LIGHT_OFF = "LIGHT_OFF";
        static final String LIGHT_ON = "LIGHT_ON";
        static final String OPEN_DOOR = "OPEN_DOOR";
        static final String OPEN_STORAGE_DOOR = "OPEN_STORAGE_DOOR";
        static final String PRINTER1_CHECK = "PRINTER1_CHECK";
        static final String PRINTER2_CHECK = "PRINTER2_CHECK";
        static final String QRCODE_LIGHT_OFF = "QRCODE_LIGHT_OFF";
        static final String QRCODE_LIGHT_ON = "QRCODE_LIGHT_ON";
        static final String RECYCLE_CHECK = "RECYCLE_CHECK";
        static final String SECOND_LIGHT = "SECOND_LIGHT";
        static final String THIRD_LIGHT = "THIRD_LIGHT";
        static final String TRANSFERCARD_READ = "TRANSFERCARD_READ";

        TestItem() {
        }
    }

    private enum TestResult {
        UNKNOWN,
        FAULT,
        SUCCESSFUL
    }

    public YC103Activity() {
        String[][] strArr = new String[17][];
        strArr[0] = new String[]{"OPEN_DOOR", this.openDoor};
        strArr[1] = new String[]{"CLOSE_DOOR", this.closeDoor};
        strArr[2] = new String[]{"FORWARD", this.forward};
        strArr[3] = new String[]{"BACKWARD", this.backward};
        strArr[4] = new String[]{"OPEN_STORAGE_DOOR", this.openStorageDoor};
        strArr[5] = new String[]{"FIRST_LIGHT", this.firstLight};
        strArr[6] = new String[]{"SECOND_LIGHT", this.secondLight};
        strArr[7] = new String[]{"THIRD_LIGHT", this.thirdLight};
        strArr[8] = new String[]{"BARCODE_READ", this.barcodeRead};
        strArr[9] = new String[]{"LIGHT_ON", this.lightOn};
        strArr[10] = new String[]{"LIGHT_FLASH", this.lightFlash};
        strArr[11] = new String[]{"LIGHT_OFF", this.lightOff};
        strArr[12] = new String[]{"QRCODE_LIGHT_ON", this.barcodeLightOn};
        strArr[13] = new String[]{"QRCODE_LIGHT_OFF", this.barcodeLightOff};
        strArr[14] = new String[]{"TRANSFERCARD_READ", this.transfercardRead};
        strArr[15] = new String[]{"CAMERA_CHECK", this.cameraCheck};
        strArr[16] = new String[]{"RECYCLE_CHECK", this.recycleCheck};
        this.testItemSet = strArr;
        this.isAutoChecking = false;
        this.isAutoCheckEnable = false;
        this.TAKE_MAX_TIMES = 10;
        this.takePictureRemainedTimes = 0;
        this.takePictureCount = 0;
        this.handler = new Handler() {
            public void handleMessage(Message msg) {
                View view;
                if (msg.what == MsgWhat.SHOW_MSG) {
                    ((TextView) YC103Activity.this.findViewById(R.id.TextView_CurrentResult)).setText((String) msg.obj);
                    ((EditText) YC103Activity.this.findViewById(R.id.editText_Result)).append(((String) msg.obj) + "\n");
                }
                if (msg.what == MsgWhat.SHOW_RESET) {
                    ((EditText) YC103Activity.this.findViewById(R.id.editText_Result)).setText("");
                }
                if (msg.what == MsgWhat.ENABLE_CONFIRM) {
                    ((TextView) YC103Activity.this.findViewById(R.id.textView_Hint)).setText((String) msg.obj);
                    YC103Activity.this.findViewById(R.id.button_CheckOk).setEnabled(true);
                    YC103Activity.this.findViewById(R.id.button_CheckOk).setBackgroundColor(-65281);
                    YC103Activity.this.findViewById(R.id.button_CheckNot).setEnabled(true);
                    YC103Activity.this.findViewById(R.id.button_CheckNot).setBackgroundColor(-65281);
                }
                if (msg.what == MsgWhat.DISABLE_CONFIRM) {
                    ((TextView) YC103Activity.this.findViewById(R.id.textView_Hint)).setText("");
                    YC103Activity.this.findViewById(R.id.button_CheckOk).setEnabled(false);
                    YC103Activity.this.findViewById(R.id.button_CheckOk).setBackgroundColor(-7829368);
                    YC103Activity.this.findViewById(R.id.button_CheckNot).setEnabled(false);
                    YC103Activity.this.findViewById(R.id.button_CheckNot).setBackgroundColor(-7829368);
                }
                if (msg.what == MsgWhat.SHOW_COLOR) {
                    view = YC103Activity.this.findViewById(((int[]) msg.obj)[0]);
                    if (view instanceof Button) {
                        ((Button) view).setBackgroundColor(((int[]) msg.obj)[1]);
                    }
                }
                if (msg.what == MsgWhat.SHOW_CAMERA) {
                    view = YC103Activity.this.findViewById(R.id.button_OpenCamera);
                    if (view instanceof Button) {
                        ((Button) view).setText((String) msg.obj);
                    }
                }
            }
        };
    }

    private void initTestItemSet() {
        this.openDoor = this.context.getString(R.string.Detection_to_open_door);
        this.closeDoor = this.context.getString(R.string.Detection_to_close_door);
        this.forward = this.context.getString(R.string.conveyor_forward);
        this.backward = this.context.getString(R.string.conveyor_back);
        this.openStorageDoor = this.context.getString(R.string.dwonOpenDoorBtn);
        this.firstLight = this.context.getString(R.string.first_photoelectric);
        this.secondLight = this.context.getString(R.string.second_photoelectric);
        this.thirdLight = this.context.getString(R.string.third_photoelectric);
        this.barcodeRead = this.context.getString(R.string.read_barcode_test);
        this.lightOn = this.context.getString(R.string.turn_on_the_door_lamp);
        this.lightFlash = this.context.getString(R.string.door_lights_flashing);
        this.lightOff = this.context.getString(R.string.door_lamp_closed);
        this.barcodeLightOn = this.context.getString(R.string.check_camera_light_open);
        this.barcodeLightOff = this.context.getString(R.string.check_camera_light_closed);
        this.transfercardRead = this.context.getString(R.string.check_ICcard_reader);
        this.cameraCheck = this.context.getString(R.string.check_camera);
        this.recycleCheck = this.context.getString(R.string.throw_bottles_of_test);
        String[][] strArr = new String[17][];
        strArr[0] = new String[]{"OPEN_DOOR", this.openDoor};
        strArr[1] = new String[]{"CLOSE_DOOR", this.closeDoor};
        strArr[2] = new String[]{"FORWARD", this.forward};
        strArr[3] = new String[]{"BACKWARD", this.backward};
        strArr[4] = new String[]{"OPEN_STORAGE_DOOR", this.openStorageDoor};
        strArr[5] = new String[]{"FIRST_LIGHT", this.firstLight};
        strArr[6] = new String[]{"SECOND_LIGHT", this.secondLight};
        strArr[7] = new String[]{"THIRD_LIGHT", this.thirdLight};
        strArr[8] = new String[]{"BARCODE_READ", this.barcodeRead};
        strArr[9] = new String[]{"LIGHT_ON", this.lightOn};
        strArr[10] = new String[]{"LIGHT_FLASH", this.lightFlash};
        strArr[11] = new String[]{"LIGHT_OFF", this.lightOff};
        strArr[12] = new String[]{"QRCODE_LIGHT_ON", this.barcodeLightOn};
        strArr[13] = new String[]{"QRCODE_LIGHT_OFF", this.barcodeLightOff};
        strArr[14] = new String[]{"TRANSFERCARD_READ", this.transfercardRead};
        strArr[15] = new String[]{"CAMERA_CHECK", this.cameraCheck};
        strArr[16] = new String[]{"RECYCLE_CHECK", this.recycleCheck};
        this.testItemSet = strArr;
    }

    protected void onStart() {
        super.onStart();
        ((EditText) findViewById(R.id.editText_RvmCode)).setText(StringUtils.trimToEmpty(SysConfig.get("RVM.CODE")));
        executeCheck("CHECK:START");
        this.eventQueuePLC.reset();
        decideStaffPermission();
    }

    protected void onPause() {
        super.onPause();
        SysGlobal.execute(new Thread() {
            public void run() {
                YC103Activity.this.eventQueuePLC.reset();
                YC103Activity.executeCheck("PLC:RESET");
                YC103Activity.executeCheck("PLC:LIGHT_OFF");
                YC103Activity.executeCheck("PLC:QRCODE_LIGHT_OFF");
                if ("TRUE".equalsIgnoreCase(SysConfig.get("COM.PLC.HAS.DOOR"))) {
                    YC103Activity.executeCheck("PLC:DOOR_CLOSE");
                }
                YC103Activity.safeSleep(200);
                YC103Activity.executeCheck("CHECK:END");
            }
        });
        this.cameraManager.closeDriver();
    }

    private static HashMap executeCheck(String CMD) {
        HashMap hsmpParam = new HashMap();
        hsmpParam.put("CMD", CMD);
        return executeCheck(hsmpParam);
    }

    private static HashMap executeCheck(HashMap hsmpParam) {
        HashMap hsmpResult = null;
        try {
            hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCheckCommonService", "commCmd", hsmpParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hsmpResult;
    }

    private static String getExecuteCheckResult(HashMap hsmpResult) {
        if (hsmpResult == null) {
            return null;
        }
        return (String) hsmpResult.get("RESULT");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_checkyc103);
        initTestItemSet();
        for (String[] objArr : this.testItemSet) {
            this.hsmpTestResult.put(objArr[0], TestResult.UNKNOWN);
        }
        TextView textView = (TextView) findViewById(R.id.textView_DeviceList);
        StringBuffer sb = new StringBuffer();
        sb.append("PLC:" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + "\n");
        sb.append("PRINT1:" + SysConfig.get("COM.PRINTER1." + SysConfig.get("PLATFORM")) + "\n");
        sb.append("PRINT2:" + SysConfig.get("COM.PRINTER2." + SysConfig.get("PLATFORM")) + "\n");
        sb.append("BARCODE:" + SysConfig.get("COM.BARCODE1." + SysConfig.get("PLATFORM")) + "\n");
        sb.append("HUILIFECARD:" + SysConfig.get("COM.HUILIFECARD." + SysConfig.get("PLATFORM")) + "\n");
        sb.append("MAGNETICCARD:" + SysConfig.get("COM.MAGNETICCARD." + SysConfig.get("PLATFORM")) + "\n");
        sb.append("WEIGH:" + SysConfig.get("COM.WEIGH." + SysConfig.get("PLATFORM")) + "\n");
        sb.append("USB:OTG:CAMERA\n");
        sb.append("USB:EXT:TOUCH SCREEN,TRANSFERPORTCARD\n");
        textView.setText(sb.toString());
        ((Button) findViewById(R.id.button_GoBack)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(YC103Activity.this, ChannelAdvancedActivity.class);
                intent.putExtra("STAFF_PERMISSION", YC103Activity.this.staffPermission);
                intent.putStringArrayListExtra("LIST", (ArrayList) YC103Activity.this.listOpt);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                YC103Activity.this.startActivity(intent);
                YC103Activity.this.finish();
            }
        });
        ((Button) findViewById(R.id.button_SetSettings)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                EditText editTextOptr = (EditText) YC103Activity.this.findViewById(R.id.editText_Optr);
                EditText editTextRvmCode = (EditText) YC103Activity.this.findViewById(R.id.editText_RvmCode);
                YC103Activity.this.optr = editTextOptr.getText().toString();
                YC103Activity.this.rvmCode = editTextRvmCode.getText().toString();
                TextView textView = (TextView) YC103Activity.this.findViewById(R.id.textView_Hint);
                if (StringUtils.isBlank(YC103Activity.this.rvmCode)) {
                    textView.setText(YC103Activity.this.getString(R.string.input_terminal_number));
                    YC103Activity.this.showButtonColorFault(R.id.button_SetSettings);
                } else {
                    YC103Activity.this.showButtonColorSuccess(R.id.button_SetSettings);
                    textView.setText("");
                }
                Properties prop = new Properties();
                prop.setProperty("RVM.CODE", YC103Activity.this.rvmCode);
                prop.setProperty("RVM.MODE", "YC103");
                PropUtils.update("/sdcard/rvm/config.properties", prop);
            }
        });
        ((Button) findViewById(R.id.Button_AutoCheck)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Button buttonAutoCheck = (Button) YC103Activity.this.findViewById(R.id.Button_AutoCheck);
                if (YC103Activity.this.isAutoChecking) {
                    YC103Activity.this.isAutoCheckEnable = false;
                    buttonAutoCheck.setText(YC103Activity.this.getString(R.string.automatic_detection));
                    return;
                }
                YC103Activity.this.isAutoCheckEnable = true;
                YC103Activity.this.isAutoChecking = true;
                buttonAutoCheck.setText(YC103Activity.this.getString(R.string.stop_testing));
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.start_testing));
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.eventQueuePLC.reset();
                            int doorOpenSignCount = 0;
                            int doorCloseSignCount = 0;
                            int runCount = 0;
                            while (YC103Activity.this.isAutoCheckEnable) {
                                HashMap<String, String> hsmpParam;
                                runCount++;
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_open_door));
                                boolean hasFound = false;
                                YC103Activity.this.eventQueuePLC.reset();
                                YC103Activity.executeCheck("PLC:DOOR_OPEN");
                                do {
                                    hsmpParam = (HashMap) YC103Activity.this.eventQueuePLC.pop(8000);
                                    if (hsmpParam == null) {
                                        break;
                                    }
                                } while (!"DOOR_OPEN".equalsIgnoreCase((String) hsmpParam.get("EVENT")));
                                hasFound = true;
                                if (hasFound) {
                                    YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.received_open_door_event));
                                    doorOpenSignCount++;
                                } else {
                                    YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.not_received_open_door_event_over_8s));
                                }
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.turn_on_the_door_lamp));
                                YC103Activity.executeCheck("PLC:LIGHT_ON");
                                YC103Activity.executeCheck("PLC:QRCODE_LIGHT_ON");
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.conveyor_forward_5s));
                                YC103Activity.executeCheck("PLC:BELT_FORWARD");
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.door_lights_flashing));
                                YC103Activity.executeCheck("PLC:LIGHT_FLASH");
                                YC103Activity.safeSleep(5000);
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.conveyor_stop));
                                YC103Activity.executeCheck("PLC:BELT_STOP");
                                YC103Activity.safeSleep(800);
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.conveyor_back_5s));
                                YC103Activity.executeCheck("PLC:BELT_BACKWARD");
                                YC103Activity.safeSleep(5000);
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.door_lamp_closed));
                                YC103Activity.executeCheck("PLC:LIGHT_OFF");
                                YC103Activity.executeCheck("PLC:QRCODE_LIGHT_OFF");
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_door_close));
                                YC103Activity.this.eventQueuePLC.reset();
                                YC103Activity.executeCheck("PLC:DOOR_CLOSE");
                                do {
                                    hsmpParam = (HashMap) YC103Activity.this.eventQueuePLC.pop(8000);
                                    if (hsmpParam == null) {
                                        break;
                                    }
                                } while (!"DOOR_CLOSE".equalsIgnoreCase((String) hsmpParam.get("EVENT")));
                                hasFound = true;
                                if (hasFound) {
                                    YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.received_close_door_event));
                                    doorCloseSignCount++;
                                } else {
                                    YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.not_received_close_door_event_over_8s));
                                }
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.run_number) + runCount + YC103Activity.this.getString(R.string.open_door_signal_counting) + doorOpenSignCount + YC103Activity.this.getString(R.string.close_door_signal_counting) + doorCloseSignCount);
                            }
                            YC103Activity.executeCheck("PLC:BELT_STOP");
                            YC103Activity.safeSleep(1000);
                            YC103Activity.executeCheck("PLC:DOOR_CLOSE");
                            YC103Activity.executeCheck("PLC:LIGHT_OFF");
                            YC103Activity.executeCheck("PLC:QRCODE_LIGHT_OFF");
                            YC103Activity.safeSleep(1000);
                        } else {
                            YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                        }
                        YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.end_test));
                        YC103Activity.this.isAutoChecking = false;
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_RecycleBottleCheck)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    /* JADX WARNING: inconsistent code. */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        /*
                        r10 = this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6.clearMsg();
                        r6 = "TRUE";
                        r7 = "BARCODE:CHECK_OPEN";
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r7);
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.getExecuteCheckResult(r7);
                        r6 = r6.equalsIgnoreCase(r7);
                        if (r6 != 0) goto L_0x0069;
                    L_0x0019:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = new java.lang.StringBuilder;
                        r7.<init>();
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r9 = 2131296585; // 0x7f090149 float:1.821109E38 double:1.0530004237E-314;
                        r8 = r8.getString(r9);
                        r7 = r7.append(r8);
                        r8 = new java.lang.StringBuilder;
                        r8.<init>();
                        r9 = "COM.BARCODE.";
                        r8 = r8.append(r9);
                        r9 = "PLATFORM";
                        r9 = com.incomrecycle.common.SysConfig.get(r9);
                        r8 = r8.append(r9);
                        r8 = r8.toString();
                        r8 = com.incomrecycle.common.SysConfig.get(r8);
                        r7 = r7.append(r8);
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r9 = 2131296583; // 0x7f090147 float:1.8211087E38 double:1.053000423E-314;
                        r8 = r8.getString(r9);
                        r7 = r7.append(r8);
                        r7 = r7.toString();
                        r6.showMsg(r7);
                    L_0x0068:
                        return;
                    L_0x0069:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296568; // 0x7f090138 float:1.8211056E38 double:1.0530004153E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = "TRUE";
                        r7 = "PLC:CHECK_OPEN";
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r7);
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.getExecuteCheckResult(r7);
                        r6 = r6.equalsIgnoreCase(r7);
                        if (r6 == 0) goto L_0x04da;
                    L_0x008d:
                        r6 = "PLC:RESET";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296586; // 0x7f09014a float:1.8211093E38 double:1.053000424E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r6.reset();
                        r2 = 0;
                        r5 = 1;
                    L_0x00af:
                        if (r5 == 0) goto L_0x0481;
                    L_0x00b1:
                        r4 = 0;
                        r3 = 0;
                        switch(r5) {
                            case -1: goto L_0x00b7;
                            case 0: goto L_0x00b6;
                            case 1: goto L_0x010f;
                            case 2: goto L_0x01cb;
                            case 3: goto L_0x0234;
                            case 4: goto L_0x0332;
                            case 5: goto L_0x0400;
                            case 6: goto L_0x015d;
                            case 7: goto L_0x00b7;
                            case 8: goto L_0x00b7;
                            default: goto L_0x00b6;
                        };
                    L_0x00b6:
                        goto L_0x00af;
                    L_0x00b7:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296607; // 0x7f09015f float:1.8211135E38 double:1.0530004346E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r6.reset();
                        r6 = "PLC:DOOR_CLOSE";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                    L_0x00d7:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 8000; // 0x1f40 float:1.121E-41 double:3.9525E-320;
                        r4 = r6.pop(r7);
                        r4 = (java.util.HashMap) r4;
                        if (r4 == 0) goto L_0x00f8;
                    L_0x00e7:
                        r6 = "EVENT";
                        r0 = r4.get(r6);
                        r0 = (java.lang.String) r0;
                        r6 = "DOOR_CLOSE";
                        r6 = r6.equalsIgnoreCase(r0);
                        if (r6 == 0) goto L_0x00d7;
                    L_0x00f7:
                        r3 = 1;
                    L_0x00f8:
                        if (r3 != 0) goto L_0x046d;
                    L_0x00fa:
                        r2 = 1;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296608; // 0x7f090160 float:1.8211137E38 double:1.053000435E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                    L_0x010d:
                        r5 = 0;
                        goto L_0x00af;
                    L_0x010f:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 8000; // 0x1f40 float:1.121E-41 double:3.9525E-320;
                        r4 = r6.pop(r7);
                        r4 = (java.util.HashMap) r4;
                        if (r4 == 0) goto L_0x0130;
                    L_0x011f:
                        r6 = "EVENT";
                        r0 = r4.get(r6);
                        r0 = (java.lang.String) r0;
                        r6 = "RVM_BUTTON_PUSH";
                        r6 = r6.equalsIgnoreCase(r0);
                        if (r6 == 0) goto L_0x010f;
                    L_0x012f:
                        r3 = 1;
                    L_0x0130:
                        if (r3 != 0) goto L_0x0148;
                    L_0x0132:
                        r2 = 1;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296587; // 0x7f09014b float:1.8211095E38 double:1.0530004247E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 0;
                        goto L_0x00af;
                    L_0x0148:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296588; // 0x7f09014c float:1.8211097E38 double:1.053000425E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 6;
                        goto L_0x00af;
                    L_0x015d:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296569; // 0x7f090139 float:1.8211058E38 double:1.053000416E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r6.reset();
                        r6 = "PLC:DOOR_OPEN";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                    L_0x017d:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 8000; // 0x1f40 float:1.121E-41 double:3.9525E-320;
                        r4 = r6.pop(r7);
                        r4 = (java.util.HashMap) r4;
                        if (r4 == 0) goto L_0x019e;
                    L_0x018d:
                        r6 = "EVENT";
                        r0 = r4.get(r6);
                        r0 = (java.lang.String) r0;
                        r6 = "DOOR_OPEN";
                        r6 = r6.equalsIgnoreCase(r0);
                        if (r6 == 0) goto L_0x017d;
                    L_0x019d:
                        r3 = 1;
                    L_0x019e:
                        if (r3 != 0) goto L_0x01b6;
                    L_0x01a0:
                        r2 = 1;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296573; // 0x7f09013d float:1.8211066E38 double:1.053000418E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 0;
                        goto L_0x00af;
                    L_0x01b6:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296574; // 0x7f09013e float:1.8211069E38 double:1.0530004183E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 2;
                        goto L_0x00af;
                    L_0x01cb:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r6.reset();
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296589; // 0x7f09014d float:1.8211099E38 double:1.0530004257E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                    L_0x01e6:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 8000; // 0x1f40 float:1.121E-41 double:3.9525E-320;
                        r4 = r6.pop(r7);
                        r4 = (java.util.HashMap) r4;
                        if (r4 == 0) goto L_0x0207;
                    L_0x01f6:
                        r6 = "EVENT";
                        r0 = r4.get(r6);
                        r0 = (java.lang.String) r0;
                        r6 = "FIRST_LIGHT_ON";
                        r6 = r6.equalsIgnoreCase(r0);
                        if (r6 == 0) goto L_0x01e6;
                    L_0x0206:
                        r3 = 1;
                    L_0x0207:
                        if (r3 != 0) goto L_0x021f;
                    L_0x0209:
                        r2 = 1;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296590; // 0x7f09014e float:1.82111E38 double:1.053000426E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 7;
                        goto L_0x00af;
                    L_0x021f:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296591; // 0x7f09014f float:1.8211103E38 double:1.0530004267E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 3;
                        goto L_0x00af;
                    L_0x0234:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296592; // 0x7f090150 float:1.8211105E38 double:1.053000427E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r6.reset();
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.safeSleep(r6);
                        r6 = "PLC:BELT_FORWARD";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                        r6 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.safeSleep(r6);
                        r6 = "PLC:BELT_FORWARD";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296593; // 0x7f090151 float:1.8211107E38 double:1.0530004277E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.safeSleep(r6);
                    L_0x027a:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
                        r4 = r6.pop(r7);
                        r4 = (java.util.HashMap) r4;
                        if (r4 == 0) goto L_0x029b;
                    L_0x028a:
                        r6 = "EVENT";
                        r0 = r4.get(r6);
                        r0 = (java.lang.String) r0;
                        r6 = "SECOND_LIGHT_ON";
                        r6 = r6.equalsIgnoreCase(r0);
                        if (r6 == 0) goto L_0x027a;
                    L_0x029a:
                        r3 = 1;
                    L_0x029b:
                        r6 = "PLC:BELT_STOP";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.safeSleep(r6);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296595; // 0x7f090153 float:1.8211111E38 double:1.0530004287E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        if (r3 != 0) goto L_0x02cf;
                    L_0x02b9:
                        r2 = 1;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296596; // 0x7f090154 float:1.8211113E38 double:1.053000429E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 7;
                        goto L_0x00af;
                    L_0x02cf:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296597; // 0x7f090155 float:1.8211115E38 double:1.0530004297E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = "BARCODE:READ";
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                        r1 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.getExecuteCheckResult(r6);
                        r6 = com.incomrecycle.common.utils.StringUtils.isBlank(r1);
                        if (r6 == 0) goto L_0x0306;
                    L_0x02f1:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296598; // 0x7f090156 float:1.8211117E38 double:1.05300043E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 5;
                        goto L_0x00af;
                    L_0x0306:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = new java.lang.StringBuilder;
                        r7.<init>();
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r9 = 2131296599; // 0x7f090157 float:1.821112E38 double:1.0530004307E-314;
                        r8 = r8.getString(r9);
                        r7 = r7.append(r8);
                        r7 = r7.append(r1);
                        r8 = "]";
                        r7 = r7.append(r8);
                        r7 = r7.toString();
                        r6.showMsg(r7);
                        r5 = 4;
                        goto L_0x00af;
                    L_0x0332:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296600; // 0x7f090158 float:1.8211121E38 double:1.053000431E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.safeSleep(r6);
                        r6 = "PLC:BELT_FORWARD";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                        r6 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.safeSleep(r6);
                    L_0x0353:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
                        r4 = r6.pop(r7);
                        r4 = (java.util.HashMap) r4;
                        if (r4 == 0) goto L_0x0374;
                    L_0x0363:
                        r6 = "EVENT";
                        r0 = r4.get(r6);
                        r0 = (java.lang.String) r0;
                        r6 = "THIRD_LIGHT_ON";
                        r6 = r6.equalsIgnoreCase(r0);
                        if (r6 == 0) goto L_0x0353;
                    L_0x0373:
                        r3 = 1;
                    L_0x0374:
                        r6 = "PLC:BELT_STOP";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.safeSleep(r6);
                        if (r3 != 0) goto L_0x0397;
                    L_0x0380:
                        r2 = 1;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296602; // 0x7f09015a float:1.8211125E38 double:1.053000432E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 8;
                        goto L_0x00af;
                    L_0x0397:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296603; // 0x7f09015b float:1.8211127E38 double:1.0530004326E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r6.reset();
                        r6 = "PLC:CHECK_THIRD_LIGHT_STATE";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296604; // 0x7f09015c float:1.821113E38 double:1.053000433E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        r6 = r6.pop(r7);
                        if (r6 != 0) goto L_0x03ed;
                    L_0x03d7:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296605; // 0x7f09015d float:1.8211131E38 double:1.0530004336E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                    L_0x03e9:
                        r5 = 8;
                        goto L_0x00af;
                    L_0x03ed:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296606; // 0x7f09015e float:1.8211133E38 double:1.053000434E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        goto L_0x03e9;
                    L_0x0400:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296601; // 0x7f090159 float:1.8211123E38 double:1.0530004317E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r6.reset();
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.safeSleep(r6);
                        r6 = "PLC:BELT_BACKWARD";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                        r6 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.safeSleep(r6);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296594; // 0x7f090152 float:1.821111E38 double:1.053000428E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                    L_0x043c:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
                        r4 = r6.pop(r7);
                        r4 = (java.util.HashMap) r4;
                        if (r4 == 0) goto L_0x045d;
                    L_0x044c:
                        r6 = "EVENT";
                        r0 = r4.get(r6);
                        r0 = (java.lang.String) r0;
                        r6 = "FIRST_LIGHT_ON";
                        r6 = r6.equalsIgnoreCase(r0);
                        if (r6 == 0) goto L_0x043c;
                    L_0x045c:
                        r3 = 1;
                    L_0x045d:
                        if (r3 != 0) goto L_0x0460;
                    L_0x045f:
                        r2 = 1;
                    L_0x0460:
                        r6 = "PLC:BELT_STOP";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.safeSleep(r6);
                        r5 = 2;
                        goto L_0x00af;
                    L_0x046d:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296609; // 0x7f090161 float:1.821114E38 double:1.0530004356E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        goto L_0x010d;
                    L_0x0481:
                        r6 = "PLC:BELT_STOP";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.safeSleep(r6);
                        r6 = "PLC:DOOR_CLOSE";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.executeCheck(r6);
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.safeSleep(r6);
                        if (r2 == 0) goto L_0x04c2;
                    L_0x0497:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.hsmpTestResult;
                        r7 = "RECYCLE_CHECK";
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.TestResult.FAULT;
                        r6.put(r7, r8);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = 2131493061; // 0x7f0c00c5 float:1.8609592E38 double:1.053097496E-314;
                        r6.showButtonColorFault(r7);
                    L_0x04ae:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r8 = 2131296584; // 0x7f090148 float:1.8211089E38 double:1.0530004233E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        goto L_0x0068;
                    L_0x04c2:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r6 = r6.hsmpTestResult;
                        r7 = "RECYCLE_CHECK";
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.TestResult.SUCCESSFUL;
                        r6.put(r7, r8);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = 2131493061; // 0x7f0c00c5 float:1.8609592E38 double:1.053097496E-314;
                        r6.showButtonColorSuccess(r7);
                        goto L_0x04ae;
                    L_0x04da:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r7 = new java.lang.StringBuilder;
                        r7.<init>();
                        r8 = "PLC[";
                        r7 = r7.append(r8);
                        r8 = new java.lang.StringBuilder;
                        r8.<init>();
                        r9 = "COM.PLC.";
                        r8 = r8.append(r9);
                        r9 = "PLATFORM";
                        r9 = com.incomrecycle.common.SysConfig.get(r9);
                        r8 = r8.append(r9);
                        r8 = r8.toString();
                        r8 = com.incomrecycle.common.SysConfig.get(r8);
                        r7 = r7.append(r8);
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.AnonymousClass5.this;
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.this;
                        r9 = 2131296583; // 0x7f090147 float:1.8211087E38 double:1.053000423E-314;
                        r8 = r8.getString(r9);
                        r7 = r7.append(r8);
                        r7 = r7.toString();
                        r6.showMsg(r7);
                        goto L_0x04ae;
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.incomrecycle.prms.rvm.gui.activity.channel.check.YC103Activity.5.1.run():void");
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_OpenDoor)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_door_open));
                            YC103Activity.this.eventQueuePLC.reset();
                            YC103Activity.executeCheck("PLC:DOOR_OPEN");
                            boolean hasFound = false;
                            HashMap<String, String> hsmpParam;
                            do {
                                hsmpParam = (HashMap) YC103Activity.this.eventQueuePLC.pop(8000);
                                if (hsmpParam == null) {
                                    break;
                                }
                            } while (!"DOOR_OPEN".equalsIgnoreCase((String) hsmpParam.get("EVENT")));
                            hasFound = true;
                            if (hasFound) {
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.received_open_door_signal));
                                YC103Activity.this.showButtonColorSuccess(R.id.button_OpenDoor);
                                YC103Activity.this.hsmpTestResult.put("OPEN_DOOR", TestResult.SUCCESSFUL);
                                return;
                            }
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.not_received_open_door_signal));
                            YC103Activity.this.showButtonColorFault(R.id.button_OpenDoor);
                            YC103Activity.this.hsmpTestResult.put("OPEN_DOOR", TestResult.FAULT);
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CloseDoor)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_door_close));
                            YC103Activity.this.eventQueuePLC.reset();
                            YC103Activity.executeCheck("PLC:DOOR_CLOSE");
                            boolean hasFound = false;
                            HashMap<String, String> hsmpParam;
                            do {
                                hsmpParam = (HashMap) YC103Activity.this.eventQueuePLC.pop(8000);
                                if (hsmpParam == null) {
                                    break;
                                }
                            } while (!"DOOR_CLOSE".equalsIgnoreCase((String) hsmpParam.get("EVENT")));
                            hasFound = true;
                            if (hasFound) {
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.received_close_door_signal));
                                YC103Activity.this.showButtonColorSuccess(R.id.button_CloseDoor);
                                YC103Activity.this.hsmpTestResult.put("CLOSE_DOOR", TestResult.SUCCESSFUL);
                                return;
                            }
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.not_received_close_door_signal));
                            YC103Activity.this.showButtonColorFault(R.id.button_CloseDoor);
                            YC103Activity.this.hsmpTestResult.put("CLOSE_DOOR", TestResult.FAULT);
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CheckOk)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (YC103Activity.this.enablePush) {
                    YC103Activity.this.disableConfirm();
                    YC103Activity.this.showButtonColorSuccess(YC103Activity.this.currentId);
                    TestResult testResult = TestResult.SUCCESSFUL;
                    switch (YC103Activity.this.currentId) {
                        case R.id.button_Forward:
                            YC103Activity.this.hsmpTestResult.put("FORWARD", testResult);
                            return;
                        case R.id.button_Backward:
                            YC103Activity.this.hsmpTestResult.put("BACKWARD", testResult);
                            return;
                        case R.id.button_OpenBottleDoor:
                            YC103Activity.this.hsmpTestResult.put("OPEN_STORAGE_DOOR", testResult);
                            return;
                        case R.id.button_DoorLightOn:
                            YC103Activity.this.hsmpTestResult.put("LIGHT_ON", testResult);
                            return;
                        case R.id.button_DoorLightFlash:
                            YC103Activity.this.hsmpTestResult.put("LIGHT_FLASH", testResult);
                            return;
                        case R.id.button_DoorLightOff:
                            YC103Activity.this.hsmpTestResult.put("LIGHT_OFF", testResult);
                            return;
                        case R.id.button_BarcodeLightOn:
                            YC103Activity.this.hsmpTestResult.put("QRCODE_LIGHT_ON", testResult);
                            return;
                        case R.id.button_BarcodeLightOff:
                            YC103Activity.this.hsmpTestResult.put("QRCODE_LIGHT_OFF", testResult);
                            return;
                        default:
                            return;
                    }
                }
            }
        });
        ((Button) findViewById(R.id.button_CheckNot)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (YC103Activity.this.enablePush) {
                    YC103Activity.this.disableConfirm();
                    YC103Activity.this.showButtonColorFault(YC103Activity.this.currentId);
                    TestResult testResult = TestResult.FAULT;
                    switch (YC103Activity.this.currentId) {
                        case R.id.button_Forward:
                            YC103Activity.this.hsmpTestResult.put("FORWARD", testResult);
                            return;
                        case R.id.button_Backward:
                            YC103Activity.this.hsmpTestResult.put("BACKWARD", testResult);
                            return;
                        case R.id.button_OpenBottleDoor:
                            YC103Activity.this.hsmpTestResult.put("OPEN_STORAGE_DOOR", testResult);
                            return;
                        case R.id.button_DoorLightOn:
                            YC103Activity.this.hsmpTestResult.put("LIGHT_ON", testResult);
                            return;
                        case R.id.button_DoorLightFlash:
                            YC103Activity.this.hsmpTestResult.put("LIGHT_FLASH", testResult);
                            return;
                        case R.id.button_DoorLightOff:
                            YC103Activity.this.hsmpTestResult.put("LIGHT_OFF", testResult);
                            return;
                        case R.id.button_BarcodeLightOn:
                            YC103Activity.this.hsmpTestResult.put("QRCODE_LIGHT_ON", testResult);
                            return;
                        case R.id.button_BarcodeLightOff:
                            YC103Activity.this.hsmpTestResult.put("QRCODE_LIGHT_OFF", testResult);
                            return;
                        default:
                            return;
                    }
                }
            }
        });
        ((Button) findViewById(R.id.button_Forward)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_conveyor_forward));
                            YC103Activity.executeCheck("PLC:BELT_FORWARD");
                            YC103Activity.safeSleep(3000);
                            YC103Activity.this.enableConfirm(R.id.button_Forward, YC103Activity.this.getString(R.string.confirm_conveyor_forward));
                            YC103Activity.executeCheck("PLC:BELT_STOP");
                            YC103Activity.safeSleep(500);
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_Backward)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_conveyor_back));
                            YC103Activity.executeCheck("PLC:BELT_BACKWARD");
                            YC103Activity.safeSleep(3000);
                            YC103Activity.this.enableConfirm(R.id.button_Backward, YC103Activity.this.getString(R.string.confirm_conveyor_back));
                            YC103Activity.executeCheck("PLC:BELT_STOP");
                            YC103Activity.safeSleep(500);
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_OpenBottleDoor)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_dwon_open_door));
                            YC103Activity.executeCheck("PLC:STORAGE_DOOR_OPEN");
                            YC103Activity.safeSleep(200);
                            YC103Activity.executeCheck("PLC:NONE");
                            YC103Activity.this.enableConfirm(R.id.button_OpenBottleDoor, YC103Activity.this.getString(R.string.check_dwon_open_door));
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CheckFirstLight)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.eventQueuePLC.reset();
                            YC103Activity.executeCheck("PLC:DOOR_OPEN");
                            YC103Activity.safeSleep(2000);
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.collect_bottles_check_first_photoelectric));
                            boolean hasFound = false;
                            HashMap<String, String> hsmpParam;
                            do {
                                hsmpParam = (HashMap) YC103Activity.this.eventQueuePLC.pop(8000);
                                if (hsmpParam == null) {
                                    break;
                                }
                            } while (!"FIRST_LIGHT_ON".equalsIgnoreCase((String) hsmpParam.get("EVENT")));
                            hasFound = true;
                            if (hasFound) {
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.received_first_photoelectric));
                                YC103Activity.this.showButtonColorSuccess(R.id.button_CheckFirstLight);
                                YC103Activity.this.hsmpTestResult.put("FIRST_LIGHT", TestResult.SUCCESSFUL);
                                return;
                            }
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.not_received_first_photoelectric));
                            YC103Activity.this.showButtonColorFault(R.id.button_CheckFirstLight);
                            YC103Activity.this.hsmpTestResult.put("FIRST_LIGHT", TestResult.FAULT);
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CheckSecondLight)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.eventQueuePLC.reset();
                            YC103Activity.executeCheck("PLC:DOOR_OPEN");
                            YC103Activity.safeSleep(2000);
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.collect_bottles_check_second_photoelectric));
                            YC103Activity.executeCheck("PLC:BELT_FORWARD");
                            boolean hasFound = false;
                            HashMap<String, String> hsmpParam;
                            do {
                                hsmpParam = (HashMap) YC103Activity.this.eventQueuePLC.pop(10000);
                                if (hsmpParam == null) {
                                    break;
                                }
                            } while (!"SECOND_LIGHT_ON".equalsIgnoreCase((String) hsmpParam.get("EVENT")));
                            hasFound = true;
                            YC103Activity.executeCheck("PLC:BELT_STOP");
                            if (hasFound) {
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.received_second_photoelectric));
                                YC103Activity.this.showButtonColorSuccess(R.id.button_CheckSecondLight);
                                YC103Activity.this.hsmpTestResult.put("SECOND_LIGHT", TestResult.SUCCESSFUL);
                                return;
                            }
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.not_received_second_photoelectric));
                            YC103Activity.this.showButtonColorFault(R.id.button_CheckSecondLight);
                            YC103Activity.this.hsmpTestResult.put("SECOND_LIGHT", TestResult.FAULT);
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CheckThirdLight)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.eventQueuePLC.reset();
                            YC103Activity.executeCheck("PLC:DOOR_OPEN");
                            YC103Activity.safeSleep(2000);
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.collect_bottles_check_third_photoelectric));
                            YC103Activity.executeCheck("PLC:BELT_FORWARD");
                            boolean hasFound = false;
                            HashMap<String, String> hsmpParam;
                            do {
                                hsmpParam = (HashMap) YC103Activity.this.eventQueuePLC.pop(12000);
                                if (hsmpParam == null) {
                                    break;
                                }
                            } while (!"THIRD_LIGHT_ON".equalsIgnoreCase((String) hsmpParam.get("EVENT")));
                            hasFound = true;
                            YC103Activity.executeCheck("PLC:BELT_STOP");
                            if (hasFound) {
                                YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.received_third_photoelectric));
                                YC103Activity.this.showButtonColorSuccess(R.id.button_CheckThirdLight);
                                YC103Activity.this.hsmpTestResult.put("THIRD_LIGHT", TestResult.SUCCESSFUL);
                                return;
                            }
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.not_received_third_photoelectric));
                            YC103Activity.this.showButtonColorFault(R.id.button_CheckThirdLight);
                            YC103Activity.this.hsmpTestResult.put("THIRD_LIGHT", TestResult.FAULT);
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_DoorLightOn)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_turn_on_the_door_lamp));
                            YC103Activity.executeCheck("PLC:LIGHT_ON");
                            YC103Activity.safeSleep(200);
                            YC103Activity.this.enableConfirm(R.id.button_DoorLightOn, YC103Activity.this.getString(R.string.check_turn_on_the_door_lamp));
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_DoorLightFlash)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_door_lights_flashing));
                            YC103Activity.executeCheck("PLC:LIGHT_FLASH");
                            YC103Activity.safeSleep(200);
                            YC103Activity.this.enableConfirm(R.id.button_DoorLightFlash, YC103Activity.this.getString(R.string.check_door_lights_flashing));
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_DoorLightOff)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_door_lamp_closed));
                            YC103Activity.executeCheck("PLC:LIGHT_OFF");
                            YC103Activity.safeSleep(200);
                            YC103Activity.this.enableConfirm(R.id.button_DoorLightOff, YC103Activity.this.getString(R.string.check_door_lamp_closed));
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_BarcodeLightOn)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_camera_light_open));
                            YC103Activity.executeCheck("PLC:QRCODE_LIGHT_ON");
                            YC103Activity.safeSleep(200);
                            YC103Activity.this.enableConfirm(R.id.button_BarcodeLightOn, YC103Activity.this.getString(R.string.pleasecheck_camera_light_open));
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_BarcodeLightOff)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_camera_light_closed));
                            YC103Activity.executeCheck("PLC:QRCODE_LIGHT_OFF");
                            YC103Activity.safeSleep(200);
                            YC103Activity.this.enableConfirm(R.id.button_BarcodeLightOff, YC103Activity.this.getString(R.string.pleasecheck_camera_light_closed));
                            return;
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_ReadBarcode)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC103Activity.executeCheck("PLC:RESET");
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.wait_scanned_barcode));
                            YC103Activity.this.eventQueuePLC.reset();
                            YC103Activity.executeCheck("PLC:DOOR_OPEN");
                            YC103Activity.executeCheck("BARCODE:RESET");
                            String barcode = null;
                            for (int i = 0; i < 5; i++) {
                                YC103Activity.safeSleep(2000);
                                String result = YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("BARCODE:READ"));
                                if (!StringUtils.isBlank(result)) {
                                    barcode = result;
                                    break;
                                }
                            }
                            if (barcode == null) {
                                YC103Activity.this.showButtonColorFault(R.id.button_ReadBarcode);
                                YC103Activity.this.hsmpTestResult.put("BARCODE_READ", TestResult.FAULT);
                                return;
                            } else if (barcode.indexOf("\n") == -1 && barcode.indexOf("\r") == -1) {
                                YC103Activity.this.showMsg(StringUtils.replace(YC103Activity.this.getString(R.string.barcode_gun_set_error), "$BARCODE$", "[" + StringUtils.replace(StringUtils.replace(barcode, "\r", "\\r"), "\n", "\\n") + "]"));
                                YC103Activity.this.showButtonColorFault(R.id.button_ReadBarcode);
                                YC103Activity.this.hsmpTestResult.put("BARCODE_READ", TestResult.FAULT);
                                return;
                            } else {
                                YC103Activity.this.enableConfirm(R.id.button_ReadBarcode, YC103Activity.this.getString(R.string.check_barcode) + barcode.trim() + "]");
                                return;
                            }
                        }
                        YC103Activity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CheckPrinter1)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                EditText editTextOptr = (EditText) YC103Activity.this.findViewById(R.id.editText_Optr);
                EditText editTextRvmCode = (EditText) YC103Activity.this.findViewById(R.id.editText_RvmCode);
                YC103Activity.this.optr = editTextOptr.getText().toString();
                YC103Activity.this.rvmCode = editTextRvmCode.getText().toString();
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PRINTER1:CHECK_OPEN")))) {
                            StringBuffer sb = new StringBuffer();
                            sb.append(YC103Activity.this.getString(R.string.tester) + ":" + YC103Activity.this.optr + "\r\n");
                            sb.append(YC103Activity.this.getString(R.string.terminal_no) + ":" + YC103Activity.this.rvmCode + "\r\n");
                            sb.append(YC103Activity.this.getString(R.string.test_time) + ":" + DateUtils.formatDatetime(new Date(), "yyyy-MM-dd HH:mm:ss") + "\r\n");
                            sb.append(YC103Activity.this.getString(R.string.printer1_test_result));
                            for (int i = 0; i < YC103Activity.this.testItemSet.length; i++) {
                                sb.append(YC103Activity.this.testItemSet[i][1] + ":");
                                TestResult testResult = (TestResult) YC103Activity.this.hsmpTestResult.get(YC103Activity.this.testItemSet[i][0]);
                                if (testResult == null) {
                                    testResult = TestResult.UNKNOWN;
                                }
                                if (testResult == TestResult.UNKNOWN) {
                                    sb.append(YC103Activity.this.getString(R.string.no_test));
                                }
                                if (testResult == TestResult.SUCCESSFUL) {
                                    sb.append(YC103Activity.this.getString(R.string.pass_test));
                                }
                                if (testResult == TestResult.FAULT) {
                                    sb.append(YC103Activity.this.getString(R.string.fail_test));
                                }
                                sb.append("\r\n");
                            }
                            try {
                                if (YC103Activity.executeCheck("PRINTER1:state") == null) {
                                    YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.printer1_noresonpse));
                                    YC103Activity.this.showButtonColorFault(R.id.button_CheckPrinter1);
                                    YC103Activity.this.hsmpTestResult.put("PRINTER1_CHECK", TestResult.FAULT);
                                    sb.append(YC103Activity.this.getString(R.string.printer1_fail));
                                } else {
                                    YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.printer1_resonpse));
                                    YC103Activity.this.showButtonColorSuccess(R.id.button_CheckPrinter1);
                                    YC103Activity.this.hsmpTestResult.put("PRINTER1_CHECK", TestResult.SUCCESSFUL);
                                    sb.append(YC103Activity.this.getString(R.string.printer1_success));
                                }
                                YC103Activity.executeCheck("PRINTER1:init");
                                String pt = sb.toString();
                                if (pt.indexOf("[FORMAT]") == -1) {
                                    pt = StringUtils.replace("[FORMAT][RESET][SETTINGS]CHARSET=$CHARSET$;[TEXT]", "$CHARSET$", "GBK") + pt + "[RESET][TEXT]\\n\\n\\n\\n[CUT]HALF[TEXT]\\n";
                                }
                                HashMap<String, String> hsmpReplace = new HashMap();
                                hsmpReplace.put("$TERM_CODE$", SysConfig.get("RVM.CODE"));
                                hsmpReplace.put("$CHARSET$", "GBK");
                                hsmpReplace.put("$n$", "\\n");
                                HashMap hsmpCmdParam = new HashMap();
                                hsmpCmdParam.put("MODEL", pt);
                                hsmpCmdParam.put("PARAM", hsmpReplace);
                                HashMap hsmpParam = new HashMap();
                                hsmpParam.put("CMD", "PRINTER1:printer");
                                hsmpParam.put("JSON", JSONUtils.toJSON(hsmpCmdParam));
                                YC103Activity.executeCheck(hsmpParam);
                                YC103Activity.executeCheck("PRINTER1:cut");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.check_printer1_result));
                            return;
                        }
                        YC103Activity.this.showMsg("Printer1[" + SysConfig.get("COM.PRINTER1." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CheckPrinter2)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                EditText editTextOptr = (EditText) YC103Activity.this.findViewById(R.id.editText_Optr);
                EditText editTextRvmCode = (EditText) YC103Activity.this.findViewById(R.id.editText_RvmCode);
                YC103Activity.this.optr = editTextOptr.getText().toString();
                YC103Activity.this.rvmCode = editTextRvmCode.getText().toString();
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("PRINTER2:CHECK_OPEN")))) {
                            StringBuffer sb = new StringBuffer();
                            sb.append(YC103Activity.this.getString(R.string.tester) + ":" + YC103Activity.this.optr + "\r\n");
                            sb.append(YC103Activity.this.getString(R.string.terminal_no) + ":" + YC103Activity.this.rvmCode + "\r\n");
                            sb.append(YC103Activity.this.getString(R.string.test_time) + ":" + DateUtils.formatDatetime(new Date(), "yyyy-MM-dd HH:mm:ss") + "\r\n");
                            sb.append(YC103Activity.this.getString(R.string.printer2_test_result));
                            for (int i = 0; i < YC103Activity.this.testItemSet.length; i++) {
                                sb.append(YC103Activity.this.testItemSet[i][1] + ":");
                                TestResult testResult = (TestResult) YC103Activity.this.hsmpTestResult.get(YC103Activity.this.testItemSet[i][0]);
                                if (testResult == null) {
                                    testResult = TestResult.UNKNOWN;
                                }
                                if (testResult == TestResult.UNKNOWN) {
                                    sb.append(YC103Activity.this.getString(R.string.no_test));
                                }
                                if (testResult == TestResult.SUCCESSFUL) {
                                    sb.append(YC103Activity.this.getString(R.string.pass_test));
                                }
                                if (testResult == TestResult.FAULT) {
                                    sb.append(YC103Activity.this.getString(R.string.fail_test));
                                }
                                sb.append("\r\n");
                            }
                            try {
                                if (YC103Activity.executeCheck("PRINTER2:state") == null) {
                                    YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.printer2_noresonpse));
                                    YC103Activity.this.showButtonColorFault(R.id.button_CheckPrinter2);
                                    YC103Activity.this.hsmpTestResult.put("PRINTER2_CHECK", TestResult.FAULT);
                                    sb.append(YC103Activity.this.getString(R.string.printer2_fail));
                                } else {
                                    YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.printer2_resonpse));
                                    YC103Activity.this.showButtonColorSuccess(R.id.button_CheckPrinter2);
                                    YC103Activity.this.hsmpTestResult.put("PRINTER2_CHECK", TestResult.SUCCESSFUL);
                                    sb.append(YC103Activity.this.getString(R.string.printer2_success));
                                }
                                YC103Activity.executeCheck("PRINTER2:init");
                                String pt = sb.toString();
                                if (pt.indexOf("[FORMAT]") == -1) {
                                    pt = StringUtils.replace("[FORMAT][RESET][SETTINGS]CHARSET=$CHARSET$;[TEXT]", "$CHARSET$", "GBK") + pt + "[RESET][TEXT]\\n\\n\\n\\n[CUT]HALF[TEXT]\\n";
                                }
                                HashMap<String, String> hsmpReplace = new HashMap();
                                hsmpReplace.put("$TERM_CODE$", SysConfig.get("RVM.CODE"));
                                hsmpReplace.put("$CHARSET$", "GBK");
                                hsmpReplace.put("$n$", "\\n");
                                HashMap hsmpCmdParam = new HashMap();
                                hsmpCmdParam.put("MODEL", pt);
                                hsmpCmdParam.put("PARAM", hsmpReplace);
                                HashMap hsmpParam = new HashMap();
                                hsmpParam.put("CMD", "PRINTER2:printer");
                                hsmpParam.put("JSON", JSONUtils.toJSON(hsmpCmdParam));
                                YC103Activity.executeCheck(hsmpParam);
                                YC103Activity.executeCheck("PRINTER2:cut");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.check_printer2_result));
                            return;
                        }
                        YC103Activity.this.showMsg("Printer2[" + SysConfig.get("COM.PRINTER2." + SysConfig.get("PLATFORM")) + YC103Activity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_ReadTransferCard)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.prepare_read_card));
                        YC103Activity.safeSleep(3000);
                        String ONECARD_NUM = null;
                        int i = 0;
                        while (i < 10) {
                            try {
                                String OneCardVerson = SysConfig.get("RVM.ONECARD.DRV.VERSION");
                                String result;
                                if (OneCardVerson.equals("0")) {
                                    result = YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("ONECARD:readOneCardReader"));
                                    if (!StringUtils.isBlank(result)) {
                                        ONECARD_NUM = (String) JSONUtils.toHashMap(result).get("ONECARD_NUM");
                                    }
                                } else if (OneCardVerson.equals("1")) {
                                    result = YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("TRAFFICCARD:ReadCard"));
                                    if (!StringUtils.isBlank(result)) {
                                        ONECARD_NUM = (String) JSONUtils.toHashMap(result).get("CardNo");
                                    }
                                }
                            } catch (Exception e) {
                            }
                            try {
                                if (!StringUtils.isBlank(ONECARD_NUM)) {
                                    break;
                                }
                                YC103Activity.safeSleep(1000);
                                i++;
                            } catch (Exception e2) {
                                e2.printStackTrace();
                                return;
                            }
                        }
                        if (StringUtils.isBlank(ONECARD_NUM)) {
                            YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.not_read_card));
                            YC103Activity.this.showButtonColorFault(R.id.button_ReadTransferCard);
                            YC103Activity.this.hsmpTestResult.put("TRANSFERCARD_READ", TestResult.FAULT);
                            return;
                        }
                        YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.already_read_card) + ONECARD_NUM + "]");
                        YC103Activity.this.showButtonColorSuccess(R.id.button_ReadTransferCard);
                        YC103Activity.this.hsmpTestResult.put("TRANSFERCARD_READ", TestResult.SUCCESSFUL);
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_OpenCamera)).setOnClickListener(new OnClickListener() {
            long lLastTime = 0;

            public void onClick(View arg0) {
                long lTime = System.currentTimeMillis();
                if (this.lLastTime <= 0 || 1000 + this.lLastTime <= lTime) {
                    this.lLastTime = lTime;
                    if (YC103Activity.this.takePictureRemainedTimes > 0) {
                        SysGlobal.execute(new Thread() {
                            public void run() {
                                Bitmap bitmap = YC103Activity.this.cameraManager.takePicture();
                                if (bitmap != null) {
                                    String qrCode = QRDecodeHelper.decode(bitmap);
                                    if (StringUtils.isBlank(qrCode)) {
                                        YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.image_not_qrcode));
                                    } else {
                                        YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.image_is_qrcode) + qrCode + "]");
                                    }
                                    bitmap.recycle();
                                    YC103Activity.this.takePictureCount = YC103Activity.this.takePictureCount + 1;
                                }
                                YC103Activity.this.takePictureRemainedTimes = YC103Activity.this.takePictureRemainedTimes - 1;
                                if (YC103Activity.this.TAKE_MAX_TIMES == YC103Activity.this.takePictureCount) {
                                    YC103Activity.this.showButtonColorSuccess(R.id.button_OpenCamera);
                                    YC103Activity.this.hsmpTestResult.put("CAMERA_CHECK", TestResult.SUCCESSFUL);
                                } else {
                                    YC103Activity.this.showButtonColorFault(R.id.button_OpenCamera);
                                    YC103Activity.this.hsmpTestResult.put("CAMERA_CHECK", TestResult.FAULT);
                                }
                                Message message = new Message();
                                message.what = MsgWhat.SHOW_CAMERA;
                                if (YC103Activity.this.takePictureRemainedTimes > 0) {
                                    message.obj = YC103Activity.this.getString(R.string.has) + YC103Activity.this.takePictureRemainedTimes + YC103Activity.this.getString(R.string.ci);
                                } else {
                                    message.obj = YC103Activity.this.getString(R.string.camera_test);
                                }
                                YC103Activity.this.handler.sendMessage(message);
                                if (YC103Activity.this.takePictureRemainedTimes == 0) {
                                    YC103Activity.this.cameraManager.stopPreview();
                                    YC103Activity.this.cameraManager.closeDriver();
                                }
                            }
                        });
                        return;
                    }
                    YC103Activity.this.takePictureRemainedTimes = YC103Activity.this.TAKE_MAX_TIMES;
                    YC103Activity.this.takePictureCount = 0;
                    ((Button) YC103Activity.this.findViewById(R.id.button_OpenCamera)).setText(YC103Activity.this.getString(R.string.please_take_photo));
                    try {
                        YC103Activity.this.cameraManager.openDriver(0, ((SurfaceView) YC103Activity.this.findViewById(R.id.surfaceView_Camera)).getHolder());
                        YC103Activity.this.cameraManager.startPreview();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Button buttonCheckWeigh = (Button) findViewById(R.id.button_WeighRead);
        if ("true".equalsIgnoreCase(SysConfig.get("COM.WEIGH.ENABLE"))) {
            buttonCheckWeigh.setVisibility(View.GONE);
        }
        buttonCheckWeigh.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC103Activity.this.clearMsg();
                        YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.weigh_read_remind));
                        YC103Activity.safeSleep(3000);
                        String weighStr = YC103Activity.getExecuteCheckResult(YC103Activity.executeCheck("WEIGH_READ"));
                        double weigh = 0.0d;
                        if (weighStr != null) {
                            weigh = Double.parseDouble(weighStr);
                        }
                        YC103Activity.this.showMsg(YC103Activity.this.getString(R.string.weigh_read_result) + weigh + "g");
                    }
                });
            }
        });
    }

    public static void safeSleep(long lMilliseconds) {
        try {
            Thread.sleep(lMilliseconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearMsg() {
        Message message = new Message();
        message.what = MsgWhat.SHOW_RESET;
        this.handler.sendMessage(message);
    }

    public void showMsg(String msg) {
        Message message = new Message();
        message.what = MsgWhat.SHOW_MSG;
        message.obj = msg;
        this.handler.sendMessage(message);
    }

    public void showButtonColor(int resId, int color) {
        Message message = new Message();
        message.what = MsgWhat.SHOW_COLOR;
        message.obj = new int[]{resId, color};
        this.handler.sendMessage(message);
    }

    public void resetButtonColor(int resId) {
        showButtonColor(resId, -7829368);
    }

    public void showButtonColorFault(int resId) {
        showButtonColor(resId, SupportMenu.CATEGORY_MASK);
    }

    public void showButtonColorSuccess(int resId) {
        showButtonColor(resId, -16711936);
    }

    public void enableConfirm(int resId, String msg) {
        this.currentId = resId;
        this.enablePush = true;
        Message message = new Message();
        message.what = MsgWhat.ENABLE_CONFIRM;
        message.obj = msg;
        this.handler.sendMessage(message);
    }

    public void disableConfirm() {
        this.enablePush = false;
        Message message = new Message();
        message.what = MsgWhat.DISABLE_CONFIRM;
        this.handler.sendMessage(message);
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
        if ("CMD".equalsIgnoreCase((String) hsmpEvent.get("EVENT"))) {
            String MSG;
            HashMap hsmpParam;
            String CMD = (String) hsmpEvent.get("CMD");
            if ("PLC".equalsIgnoreCase(CMD)) {
                MSG = (String) hsmpEvent.get("MSG");
                hsmpParam = new HashMap();
                hsmpParam.put("EVENT", MSG);
                this.eventQueuePLC.push(hsmpParam);
            }
            if ("PRINTER1".equalsIgnoreCase(CMD)) {
                MSG = (String) hsmpEvent.get("MSG");
                hsmpParam = new HashMap();
                hsmpParam.put("EVENT", MSG);
                this.eventQueuePrinter1.push(hsmpParam);
            }
            if ("PRINTER2".equalsIgnoreCase(CMD)) {
                MSG = (String) hsmpEvent.get("MSG");
                hsmpParam = new HashMap();
                hsmpParam.put("EVENT", MSG);
                this.eventQueuePrinter2.push(hsmpParam);
            }
        }
    }

    public void decideStaffPermission() {
        Intent intent = getIntent();
        this.staffPermission = intent.getStringExtra("STAFF_PERMISSION");
        this.listOpt = intent.getStringArrayListExtra("LIST");
    }
}
