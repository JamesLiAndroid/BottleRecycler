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

public class YC104CActivity extends BaseActivity {
    private int TAKE_MAX_TIMES;
    String backward;
    String barcodeLightOff;
    String barcodeLightOn;
    String barcodeRead;
    String cameraCheck;
    CameraManager cameraManager = new CameraManager();
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
    String openStorageDoor;
    String optr = null;
    String recycleCheck;
    String rvmCode = null;
    String secondLight;
    String sortToCan;
    String sortToPet;
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
        static final String FIRST_LIGHT = "FIRST_LIGHT";
        static final String FORWARD = "FORWARD";
        static final String LIGHT_FLASH = "LIGHT_FLASH";
        static final String LIGHT_OFF = "LIGHT_OFF";
        static final String LIGHT_ON = "LIGHT_ON";
        static final String OPEN_STORAGE_DOOR = "OPEN_STORAGE_DOOR";
        static final String PRINTER1_CHECK = "PRINTER1_CHECK";
        static final String PRINTER2_CHECK = "PRINTER2_CHECK";
        static final String QRCODE_LIGHT_OFF = "QRCODE_LIGHT_OFF";
        static final String QRCODE_LIGHT_ON = "QRCODE_LIGHT_ON";
        static final String RECYCLE_CHECK = "RECYCLE_CHECK";
        static final String SECOND_LIGHT = "SECOND_LIGHT";
        static final String SORT_TO_CAN = "SORT_TO_CAN";
        static final String SORT_TO_PET = "SORT_TO_PET";
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

    public YC104CActivity() {
        String[][] strArr = new String[17][];
        strArr[0] = new String[]{"SORT_TO_PET", this.sortToPet};
        strArr[1] = new String[]{"SORT_TO_CAN", this.sortToCan};
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
                    ((TextView) YC104CActivity.this.findViewById(R.id.TextView_CurrentResult)).setText((String) msg.obj);
                    ((EditText) YC104CActivity.this.findViewById(R.id.editText_Result)).append(((String) msg.obj) + "\n");
                }
                if (msg.what == MsgWhat.SHOW_RESET) {
                    ((EditText) YC104CActivity.this.findViewById(R.id.editText_Result)).setText("");
                }
                if (msg.what == MsgWhat.ENABLE_CONFIRM) {
                    ((TextView) YC104CActivity.this.findViewById(R.id.textView_Hint)).setText((String) msg.obj);
                    YC104CActivity.this.findViewById(R.id.button_CheckOk).setEnabled(true);
                    YC104CActivity.this.findViewById(R.id.button_CheckOk).setBackgroundColor(-65281);
                    YC104CActivity.this.findViewById(R.id.button_CheckNot).setEnabled(true);
                    YC104CActivity.this.findViewById(R.id.button_CheckNot).setBackgroundColor(-65281);
                }
                if (msg.what == MsgWhat.DISABLE_CONFIRM) {
                    ((TextView) YC104CActivity.this.findViewById(R.id.textView_Hint)).setText("");
                    YC104CActivity.this.findViewById(R.id.button_CheckOk).setEnabled(false);
                    YC104CActivity.this.findViewById(R.id.button_CheckOk).setBackgroundColor(-7829368);
                    YC104CActivity.this.findViewById(R.id.button_CheckNot).setEnabled(false);
                    YC104CActivity.this.findViewById(R.id.button_CheckNot).setBackgroundColor(-7829368);
                }
                if (msg.what == MsgWhat.SHOW_COLOR) {
                    view = YC104CActivity.this.findViewById(((int[]) msg.obj)[0]);
                    if (view instanceof Button) {
                        ((Button) view).setBackgroundColor(((int[]) msg.obj)[1]);
                    }
                }
                if (msg.what == MsgWhat.SHOW_CAMERA) {
                    view = YC104CActivity.this.findViewById(R.id.button_OpenCamera);
                    if (view instanceof Button) {
                        ((Button) view).setText((String) msg.obj);
                    }
                }
            }
        };
    }

    private void initTestItemSet() {
        this.sortToPet = this.context.getString(R.string.Detection_sort_to_pet);
        this.sortToCan = this.context.getString(R.string.Detection_sort_to_can);
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
        strArr[0] = new String[]{"SORT_TO_PET", this.sortToPet};
        strArr[1] = new String[]{"SORT_TO_CAN", this.sortToCan};
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
                YC104CActivity.this.eventQueuePLC.reset();
                YC104CActivity.executeCheck("PLC:RESET");
                YC104CActivity.executeCheck("PLC:LIGHT_OFF");
                YC104CActivity.executeCheck("PLC:QRCODE_LIGHT_OFF");
                if ("TRUE".equalsIgnoreCase(SysConfig.get("COM.PLC.HAS.DOOR"))) {
                    YC104CActivity.executeCheck("PLC:DOOR_CLOSE");
                }
                YC104CActivity.safeSleep(200);
                YC104CActivity.executeCheck("CHECK:END");
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
        setContentView(R.layout.activity_checkyc104c);
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
                Intent intent = new Intent(YC104CActivity.this, ChannelAdvancedActivity.class);
                intent.putExtra("STAFF_PERMISSION", YC104CActivity.this.staffPermission);
                intent.putStringArrayListExtra("LIST", (ArrayList) YC104CActivity.this.listOpt);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                YC104CActivity.this.startActivity(intent);
                YC104CActivity.this.finish();
            }
        });
        ((Button) findViewById(R.id.button_SetSettings)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                EditText editTextOptr = (EditText) YC104CActivity.this.findViewById(R.id.editText_Optr);
                EditText editTextRvmCode = (EditText) YC104CActivity.this.findViewById(R.id.editText_RvmCode);
                YC104CActivity.this.optr = editTextOptr.getText().toString();
                YC104CActivity.this.rvmCode = editTextRvmCode.getText().toString();
                TextView textView = (TextView) YC104CActivity.this.findViewById(R.id.textView_Hint);
                if (StringUtils.isBlank(YC104CActivity.this.rvmCode)) {
                    textView.setText(YC104CActivity.this.getString(R.string.input_terminal_number));
                    YC104CActivity.this.showButtonColorFault(R.id.button_SetSettings);
                } else {
                    YC104CActivity.this.showButtonColorSuccess(R.id.button_SetSettings);
                    textView.setText("");
                }
                Properties prop = new Properties();
                prop.setProperty("RVM.CODE", YC104CActivity.this.rvmCode);
                prop.setProperty("RVM.MODE", "YC104C");
                PropUtils.update("/sdcard/rvm/config.properties", prop);
            }
        });
        ((Button) findViewById(R.id.Button_AutoCheck)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Button buttonAutoCheck = (Button) YC104CActivity.this.findViewById(R.id.Button_AutoCheck);
                if (YC104CActivity.this.isAutoChecking) {
                    YC104CActivity.this.isAutoCheckEnable = false;
                    buttonAutoCheck.setText(YC104CActivity.this.getString(R.string.automatic_detection));
                    return;
                }
                YC104CActivity.this.isAutoCheckEnable = true;
                YC104CActivity.this.isAutoChecking = true;
                buttonAutoCheck.setText(YC104CActivity.this.getString(R.string.stop_testing));
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.start_testing));
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.eventQueuePLC.reset();
                            int runCount = 0;
                            while (YC104CActivity.this.isAutoCheckEnable) {
                                runCount++;
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.wait_open_door));
                                YC104CActivity.this.eventQueuePLC.reset();
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.turn_on_the_door_lamp));
                                YC104CActivity.executeCheck("PLC:LIGHT_ON");
                                YC104CActivity.executeCheck("PLC:QRCODE_LIGHT_ON");
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.conveyor_forward_5s));
                                YC104CActivity.executeCheck("PLC:BELT_FORWARD");
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.door_lights_flashing));
                                YC104CActivity.executeCheck("PLC:LIGHT_FLASH");
                                YC104CActivity.safeSleep(5000);
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.conveyor_stop));
                                YC104CActivity.executeCheck("PLC:BELT_STOP");
                                YC104CActivity.safeSleep(800);
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.conveyor_back_5s));
                                YC104CActivity.executeCheck("PLC:BELT_BACKWARD");
                                YC104CActivity.safeSleep(5000);
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.door_lamp_closed));
                                YC104CActivity.executeCheck("PLC:LIGHT_OFF");
                                YC104CActivity.executeCheck("PLC:QRCODE_LIGHT_OFF");
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.wait_door_close));
                                YC104CActivity.this.eventQueuePLC.reset();
                            }
                            YC104CActivity.executeCheck("PLC:BELT_STOP");
                            YC104CActivity.safeSleep(1000);
                            YC104CActivity.executeCheck("PLC:LIGHT_OFF");
                            YC104CActivity.executeCheck("PLC:QRCODE_LIGHT_OFF");
                            YC104CActivity.safeSleep(1000);
                        } else {
                            YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                        }
                        YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.end_test));
                        YC104CActivity.this.isAutoChecking = false;
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
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6.clearMsg();
                        r6 = "TRUE";
                        r7 = "BARCODE:CHECK_OPEN";
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r7);
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.getExecuteCheckResult(r7);
                        r6 = r6.equalsIgnoreCase(r7);
                        if (r6 != 0) goto L_0x0069;
                    L_0x0019:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = new java.lang.StringBuilder;
                        r7.<init>();
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
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
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r9 = 2131296583; // 0x7f090147 float:1.8211087E38 double:1.053000423E-314;
                        r8 = r8.getString(r9);
                        r7 = r7.append(r8);
                        r7 = r7.toString();
                        r6.showMsg(r7);
                    L_0x0068:
                        return;
                    L_0x0069:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296568; // 0x7f090138 float:1.8211056E38 double:1.0530004153E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = "TRUE";
                        r7 = "PLC:CHECK_OPEN";
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r7);
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.getExecuteCheckResult(r7);
                        r6 = r6.equalsIgnoreCase(r7);
                        if (r6 == 0) goto L_0x03f4;
                    L_0x008d:
                        r6 = "PLC:RESET";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r6);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296586; // 0x7f09014a float:1.8211093E38 double:1.053000424E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.eventQueuePLC;
                        r6.reset();
                        r2 = 0;
                        r5 = 1;
                    L_0x00af:
                        if (r5 == 0) goto L_0x03a5;
                    L_0x00b1:
                        r4 = 0;
                        r3 = 0;
                        switch(r5) {
                            case 1: goto L_0x00b7;
                            case 2: goto L_0x0103;
                            case 3: goto L_0x016c;
                            case 4: goto L_0x026a;
                            case 5: goto L_0x0338;
                            default: goto L_0x00b6;
                        };
                    L_0x00b6:
                        goto L_0x00af;
                    L_0x00b7:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 8000; // 0x1f40 float:1.121E-41 double:3.9525E-320;
                        r4 = r6.pop(r7);
                        r4 = (java.util.HashMap) r4;
                        if (r4 == 0) goto L_0x00d8;
                    L_0x00c7:
                        r6 = "EVENT";
                        r0 = r4.get(r6);
                        r0 = (java.lang.String) r0;
                        r6 = "RVM_BUTTON_PUSH";
                        r6 = r6.equalsIgnoreCase(r0);
                        if (r6 == 0) goto L_0x00b7;
                    L_0x00d7:
                        r3 = 1;
                    L_0x00d8:
                        if (r3 != 0) goto L_0x00ef;
                    L_0x00da:
                        r2 = 1;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296587; // 0x7f09014b float:1.8211095E38 double:1.0530004247E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 0;
                        goto L_0x00af;
                    L_0x00ef:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296588; // 0x7f09014c float:1.8211097E38 double:1.053000425E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 2;
                        goto L_0x00af;
                    L_0x0103:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.eventQueuePLC;
                        r6.reset();
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296589; // 0x7f09014d float:1.8211099E38 double:1.0530004257E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                    L_0x011e:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 8000; // 0x1f40 float:1.121E-41 double:3.9525E-320;
                        r4 = r6.pop(r7);
                        r4 = (java.util.HashMap) r4;
                        if (r4 == 0) goto L_0x013f;
                    L_0x012e:
                        r6 = "EVENT";
                        r0 = r4.get(r6);
                        r0 = (java.lang.String) r0;
                        r6 = "FIRST_LIGHT_ON";
                        r6 = r6.equalsIgnoreCase(r0);
                        if (r6 == 0) goto L_0x011e;
                    L_0x013e:
                        r3 = 1;
                    L_0x013f:
                        if (r3 != 0) goto L_0x0157;
                    L_0x0141:
                        r2 = 1;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296590; // 0x7f09014e float:1.82111E38 double:1.053000426E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 0;
                        goto L_0x00af;
                    L_0x0157:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296591; // 0x7f09014f float:1.8211103E38 double:1.0530004267E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 3;
                        goto L_0x00af;
                    L_0x016c:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296592; // 0x7f090150 float:1.8211105E38 double:1.053000427E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.eventQueuePLC;
                        r6.reset();
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.safeSleep(r6);
                        r6 = "PLC:BELT_FORWARD";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r6);
                        r6 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.safeSleep(r6);
                        r6 = "PLC:BELT_FORWARD";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r6);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296593; // 0x7f090151 float:1.8211107E38 double:1.0530004277E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.safeSleep(r6);
                    L_0x01b2:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
                        r4 = r6.pop(r7);
                        r4 = (java.util.HashMap) r4;
                        if (r4 == 0) goto L_0x01d3;
                    L_0x01c2:
                        r6 = "EVENT";
                        r0 = r4.get(r6);
                        r0 = (java.lang.String) r0;
                        r6 = "SECOND_LIGHT_ON";
                        r6 = r6.equalsIgnoreCase(r0);
                        if (r6 == 0) goto L_0x01b2;
                    L_0x01d2:
                        r3 = 1;
                    L_0x01d3:
                        r6 = "PLC:BELT_STOP";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r6);
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.safeSleep(r6);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296595; // 0x7f090153 float:1.8211111E38 double:1.0530004287E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        if (r3 != 0) goto L_0x0207;
                    L_0x01f1:
                        r2 = 1;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296596; // 0x7f090154 float:1.8211113E38 double:1.053000429E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 0;
                        goto L_0x00af;
                    L_0x0207:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296597; // 0x7f090155 float:1.8211115E38 double:1.0530004297E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = "BARCODE:READ";
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r6);
                        r1 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.getExecuteCheckResult(r6);
                        r6 = com.incomrecycle.common.utils.StringUtils.isBlank(r1);
                        if (r6 == 0) goto L_0x023e;
                    L_0x0229:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296598; // 0x7f090156 float:1.8211117E38 double:1.05300043E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 5;
                        goto L_0x00af;
                    L_0x023e:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = new java.lang.StringBuilder;
                        r7.<init>();
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
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
                    L_0x026a:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296600; // 0x7f090158 float:1.8211121E38 double:1.053000431E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.safeSleep(r6);
                        r6 = "PLC:BELT_FORWARD";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r6);
                        r6 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.safeSleep(r6);
                    L_0x028b:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
                        r4 = r6.pop(r7);
                        r4 = (java.util.HashMap) r4;
                        if (r4 == 0) goto L_0x02ac;
                    L_0x029b:
                        r6 = "EVENT";
                        r0 = r4.get(r6);
                        r0 = (java.lang.String) r0;
                        r6 = "THIRD_LIGHT_ON";
                        r6 = r6.equalsIgnoreCase(r0);
                        if (r6 == 0) goto L_0x028b;
                    L_0x02ab:
                        r3 = 1;
                    L_0x02ac:
                        r6 = "PLC:BELT_STOP";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r6);
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.safeSleep(r6);
                        if (r3 != 0) goto L_0x02cf;
                    L_0x02b8:
                        r2 = 1;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296602; // 0x7f09015a float:1.8211125E38 double:1.053000432E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r5 = 8;
                        goto L_0x00af;
                    L_0x02cf:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296603; // 0x7f09015b float:1.8211127E38 double:1.0530004326E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.eventQueuePLC;
                        r6.reset();
                        r6 = "PLC:CHECK_THIRD_LIGHT_STATE";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r6);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296604; // 0x7f09015c float:1.821113E38 double:1.053000433E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        r6 = r6.pop(r7);
                        if (r6 != 0) goto L_0x0325;
                    L_0x030f:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296605; // 0x7f09015d float:1.8211131E38 double:1.0530004336E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                    L_0x0321:
                        r5 = 8;
                        goto L_0x00af;
                    L_0x0325:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296606; // 0x7f09015e float:1.8211133E38 double:1.053000434E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        goto L_0x0321;
                    L_0x0338:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296601; // 0x7f090159 float:1.8211123E38 double:1.0530004317E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.eventQueuePLC;
                        r6.reset();
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.safeSleep(r6);
                        r6 = "PLC:BELT_BACKWARD";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r6);
                        r6 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.safeSleep(r6);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296594; // 0x7f090152 float:1.821111E38 double:1.053000428E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                    L_0x0374:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.eventQueuePLC;
                        r7 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
                        r4 = r6.pop(r7);
                        r4 = (java.util.HashMap) r4;
                        if (r4 == 0) goto L_0x0395;
                    L_0x0384:
                        r6 = "EVENT";
                        r0 = r4.get(r6);
                        r0 = (java.lang.String) r0;
                        r6 = "FIRST_LIGHT_ON";
                        r6 = r6.equalsIgnoreCase(r0);
                        if (r6 == 0) goto L_0x0374;
                    L_0x0394:
                        r3 = 1;
                    L_0x0395:
                        if (r3 != 0) goto L_0x0398;
                    L_0x0397:
                        r2 = 1;
                    L_0x0398:
                        r6 = "PLC:BELT_STOP";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r6);
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.safeSleep(r6);
                        r5 = 2;
                        goto L_0x00af;
                    L_0x03a5:
                        r6 = "PLC:BELT_STOP";
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.executeCheck(r6);
                        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
                        com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.safeSleep(r6);
                        if (r2 == 0) goto L_0x03dc;
                    L_0x03b1:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.hsmpTestResult;
                        r7 = "RECYCLE_CHECK";
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.TestResult.FAULT;
                        r6.put(r7, r8);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = 2131493061; // 0x7f0c00c5 float:1.8609592E38 double:1.053097496E-314;
                        r6.showButtonColorFault(r7);
                    L_0x03c8:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r7 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r8 = 2131296584; // 0x7f090148 float:1.8211089E38 double:1.0530004233E-314;
                        r7 = r7.getString(r8);
                        r6.showMsg(r7);
                        goto L_0x0068;
                    L_0x03dc:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r6 = r6.hsmpTestResult;
                        r7 = "RECYCLE_CHECK";
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.TestResult.SUCCESSFUL;
                        r6.put(r7, r8);
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r7 = 2131493061; // 0x7f0c00c5 float:1.8609592E38 double:1.053097496E-314;
                        r6.showButtonColorSuccess(r7);
                        goto L_0x03c8;
                    L_0x03f4:
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r6 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
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
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.AnonymousClass5.this;
                        r8 = com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.this;
                        r9 = 2131296583; // 0x7f090147 float:1.8211087E38 double:1.053000423E-314;
                        r8 = r8.getString(r9);
                        r7 = r7.append(r8);
                        r7 = r7.toString();
                        r6.showMsg(r7);
                        goto L_0x03c8;
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.incomrecycle.prms.rvm.gui.activity.channel.check.YC104CActivity.5.1.run():void");
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_sort_to_pet)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.Detection_sort_to_pet));
                            YC104CActivity.this.eventQueuePLC.reset();
                            YC104CActivity.executeCheck("PLC:SORT_TO_PET");
                            boolean hasFound = false;
                            HashMap<String, String> hsmpParam;
                            do {
                                hsmpParam = (HashMap) YC104CActivity.this.eventQueuePLC.pop(8000);
                                if (hsmpParam == null) {
                                    break;
                                }
                            } while (!"SORT_TO_PET".equalsIgnoreCase((String) hsmpParam.get("EVENT")));
                            hasFound = true;
                            if (hasFound) {
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.received_sort_to_pet_signal));
                                YC104CActivity.this.showButtonColorSuccess(R.id.button_sort_to_pet);
                                YC104CActivity.this.hsmpTestResult.put("SORT_TO_PET", TestResult.SUCCESSFUL);
                                return;
                            }
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.not_received_sort_to_pet_signal));
                            YC104CActivity.this.showButtonColorFault(R.id.button_sort_to_pet);
                            YC104CActivity.this.hsmpTestResult.put("SORT_TO_PET", TestResult.FAULT);
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_sort_to_can)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.Detection_sort_to_can));
                            YC104CActivity.this.eventQueuePLC.reset();
                            YC104CActivity.executeCheck("PLC:SORT_TO_CAN");
                            boolean hasFound = false;
                            HashMap<String, String> hsmpParam;
                            do {
                                hsmpParam = (HashMap) YC104CActivity.this.eventQueuePLC.pop(8000);
                                if (hsmpParam == null) {
                                    break;
                                }
                            } while (!"SORT_TO_CAN".equalsIgnoreCase((String) hsmpParam.get("EVENT")));
                            hasFound = true;
                            if (hasFound) {
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.received_sort_to_can_signal));
                                YC104CActivity.this.showButtonColorSuccess(R.id.button_sort_to_can);
                                YC104CActivity.this.hsmpTestResult.put("SORT_TO_CAN", TestResult.SUCCESSFUL);
                                return;
                            }
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.not_received_sort_to_can_signal));
                            YC104CActivity.this.showButtonColorFault(R.id.button_sort_to_can);
                            YC104CActivity.this.hsmpTestResult.put("SORT_TO_CAN", TestResult.FAULT);
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CheckOk)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (YC104CActivity.this.enablePush) {
                    YC104CActivity.this.disableConfirm();
                    YC104CActivity.this.showButtonColorSuccess(YC104CActivity.this.currentId);
                    TestResult testResult = TestResult.SUCCESSFUL;
                    switch (YC104CActivity.this.currentId) {
                        case R.id.button_Forward:
                            YC104CActivity.this.hsmpTestResult.put("FORWARD", testResult);
                            return;
                        case R.id.button_Backward:
                            YC104CActivity.this.hsmpTestResult.put("BACKWARD", testResult);
                            return;
                        case R.id.button_OpenBottleDoor:
                            YC104CActivity.this.hsmpTestResult.put("OPEN_STORAGE_DOOR", testResult);
                            return;
                        case R.id.button_DoorLightOn:
                            YC104CActivity.this.hsmpTestResult.put("LIGHT_ON", testResult);
                            return;
                        case R.id.button_DoorLightFlash:
                            YC104CActivity.this.hsmpTestResult.put("LIGHT_FLASH", testResult);
                            return;
                        case R.id.button_DoorLightOff:
                            YC104CActivity.this.hsmpTestResult.put("LIGHT_OFF", testResult);
                            return;
                        case R.id.button_BarcodeLightOn:
                            YC104CActivity.this.hsmpTestResult.put("QRCODE_LIGHT_ON", testResult);
                            return;
                        case R.id.button_BarcodeLightOff:
                            YC104CActivity.this.hsmpTestResult.put("QRCODE_LIGHT_OFF", testResult);
                            return;
                        default:
                            return;
                    }
                }
            }
        });
        ((Button) findViewById(R.id.button_CheckNot)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (YC104CActivity.this.enablePush) {
                    YC104CActivity.this.disableConfirm();
                    YC104CActivity.this.showButtonColorFault(YC104CActivity.this.currentId);
                    TestResult testResult = TestResult.FAULT;
                    switch (YC104CActivity.this.currentId) {
                        case R.id.button_Forward:
                            YC104CActivity.this.hsmpTestResult.put("FORWARD", testResult);
                            return;
                        case R.id.button_Backward:
                            YC104CActivity.this.hsmpTestResult.put("BACKWARD", testResult);
                            return;
                        case R.id.button_OpenBottleDoor:
                            YC104CActivity.this.hsmpTestResult.put("OPEN_STORAGE_DOOR", testResult);
                            return;
                        case R.id.button_DoorLightOn:
                            YC104CActivity.this.hsmpTestResult.put("LIGHT_ON", testResult);
                            return;
                        case R.id.button_DoorLightFlash:
                            YC104CActivity.this.hsmpTestResult.put("LIGHT_FLASH", testResult);
                            return;
                        case R.id.button_DoorLightOff:
                            YC104CActivity.this.hsmpTestResult.put("LIGHT_OFF", testResult);
                            return;
                        case R.id.button_BarcodeLightOn:
                            YC104CActivity.this.hsmpTestResult.put("QRCODE_LIGHT_ON", testResult);
                            return;
                        case R.id.button_BarcodeLightOff:
                            YC104CActivity.this.hsmpTestResult.put("QRCODE_LIGHT_OFF", testResult);
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
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.wait_conveyor_forward));
                            YC104CActivity.executeCheck("PLC:BELT_FORWARD");
                            YC104CActivity.safeSleep(3000);
                            YC104CActivity.this.enableConfirm(R.id.button_Forward, YC104CActivity.this.getString(R.string.confirm_conveyor_forward));
                            YC104CActivity.executeCheck("PLC:BELT_STOP");
                            YC104CActivity.safeSleep(500);
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_Backward)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.wait_conveyor_back));
                            YC104CActivity.executeCheck("PLC:BELT_BACKWARD");
                            YC104CActivity.safeSleep(3000);
                            YC104CActivity.this.enableConfirm(R.id.button_Backward, YC104CActivity.this.getString(R.string.confirm_conveyor_back));
                            YC104CActivity.executeCheck("PLC:BELT_STOP");
                            YC104CActivity.safeSleep(500);
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_OpenBottleDoor)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.wait_dwon_open_door));
                            YC104CActivity.executeCheck("PLC:STORAGE_DOOR_OPEN");
                            YC104CActivity.safeSleep(200);
                            YC104CActivity.executeCheck("PLC:NONE");
                            YC104CActivity.this.enableConfirm(R.id.button_OpenBottleDoor, YC104CActivity.this.getString(R.string.check_dwon_open_door));
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CheckFirstLight)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.eventQueuePLC.reset();
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.collect_bottles_check_first_photoelectric));
                            boolean hasFound = false;
                            HashMap<String, String> hsmpParam;
                            do {
                                hsmpParam = (HashMap) YC104CActivity.this.eventQueuePLC.pop(8000);
                                if (hsmpParam == null) {
                                    break;
                                }
                            } while (!"FIRST_LIGHT_ON".equalsIgnoreCase((String) hsmpParam.get("EVENT")));
                            hasFound = true;
                            if (hasFound) {
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.received_first_photoelectric));
                                YC104CActivity.this.showButtonColorSuccess(R.id.button_CheckFirstLight);
                                YC104CActivity.this.hsmpTestResult.put("FIRST_LIGHT", TestResult.SUCCESSFUL);
                                return;
                            }
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.not_received_first_photoelectric));
                            YC104CActivity.this.showButtonColorFault(R.id.button_CheckFirstLight);
                            YC104CActivity.this.hsmpTestResult.put("FIRST_LIGHT", TestResult.FAULT);
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CheckSecondLight)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.eventQueuePLC.reset();
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.collect_bottles_check_second_photoelectric));
                            YC104CActivity.executeCheck("PLC:BELT_FORWARD");
                            boolean hasFound = false;
                            HashMap<String, String> hsmpParam;
                            do {
                                hsmpParam = (HashMap) YC104CActivity.this.eventQueuePLC.pop(10000);
                                if (hsmpParam == null) {
                                    break;
                                }
                            } while (!"SECOND_LIGHT_ON".equalsIgnoreCase((String) hsmpParam.get("EVENT")));
                            hasFound = true;
                            YC104CActivity.executeCheck("PLC:BELT_STOP");
                            if (hasFound) {
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.received_second_photoelectric));
                                YC104CActivity.this.showButtonColorSuccess(R.id.button_CheckSecondLight);
                                YC104CActivity.this.hsmpTestResult.put("SECOND_LIGHT", TestResult.SUCCESSFUL);
                                return;
                            }
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.not_received_second_photoelectric));
                            YC104CActivity.this.showButtonColorFault(R.id.button_CheckSecondLight);
                            YC104CActivity.this.hsmpTestResult.put("SECOND_LIGHT", TestResult.FAULT);
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CheckThirdLight)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.eventQueuePLC.reset();
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.collect_bottles_check_third_photoelectric));
                            YC104CActivity.executeCheck("PLC:BELT_FORWARD");
                            boolean hasFound = false;
                            HashMap<String, String> hsmpParam;
                            do {
                                hsmpParam = (HashMap) YC104CActivity.this.eventQueuePLC.pop(12000);
                                if (hsmpParam == null) {
                                    break;
                                }
                            } while (!"THIRD_LIGHT_ON".equalsIgnoreCase((String) hsmpParam.get("EVENT")));
                            hasFound = true;
                            YC104CActivity.executeCheck("PLC:BELT_STOP");
                            if (hasFound) {
                                YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.received_third_photoelectric));
                                YC104CActivity.this.showButtonColorSuccess(R.id.button_CheckThirdLight);
                                YC104CActivity.this.hsmpTestResult.put("THIRD_LIGHT", TestResult.SUCCESSFUL);
                                return;
                            }
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.not_received_third_photoelectric));
                            YC104CActivity.this.showButtonColorFault(R.id.button_CheckThirdLight);
                            YC104CActivity.this.hsmpTestResult.put("THIRD_LIGHT", TestResult.FAULT);
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_DoorLightOn)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.wait_turn_on_the_door_lamp));
                            YC104CActivity.executeCheck("PLC:LIGHT_ON");
                            YC104CActivity.safeSleep(200);
                            YC104CActivity.this.enableConfirm(R.id.button_DoorLightOn, YC104CActivity.this.getString(R.string.check_turn_on_the_door_lamp));
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_DoorLightFlash)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.wait_door_lights_flashing));
                            YC104CActivity.executeCheck("PLC:LIGHT_FLASH");
                            YC104CActivity.safeSleep(200);
                            YC104CActivity.this.enableConfirm(R.id.button_DoorLightFlash, YC104CActivity.this.getString(R.string.check_door_lights_flashing));
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_DoorLightOff)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.wait_door_lamp_closed));
                            YC104CActivity.executeCheck("PLC:LIGHT_OFF");
                            YC104CActivity.safeSleep(200);
                            YC104CActivity.this.enableConfirm(R.id.button_DoorLightOff, YC104CActivity.this.getString(R.string.check_door_lamp_closed));
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_BarcodeLightOn)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.wait_camera_light_open));
                            YC104CActivity.executeCheck("PLC:QRCODE_LIGHT_ON");
                            YC104CActivity.safeSleep(200);
                            YC104CActivity.this.enableConfirm(R.id.button_BarcodeLightOn, YC104CActivity.this.getString(R.string.pleasecheck_camera_light_open));
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_BarcodeLightOff)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.wait_camera_light_closed));
                            YC104CActivity.executeCheck("PLC:QRCODE_LIGHT_OFF");
                            YC104CActivity.safeSleep(200);
                            YC104CActivity.this.enableConfirm(R.id.button_BarcodeLightOff, YC104CActivity.this.getString(R.string.pleasecheck_camera_light_closed));
                            return;
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_ReadBarcode)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PLC:CHECK_OPEN")))) {
                            YC104CActivity.executeCheck("PLC:RESET");
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.wait_scanned_barcode));
                            YC104CActivity.this.eventQueuePLC.reset();
                            YC104CActivity.executeCheck("BARCODE:RESET");
                            String barcode = null;
                            for (int i = 0; i < 5; i++) {
                                YC104CActivity.safeSleep(2000);
                                String result = YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("BARCODE:READ"));
                                if (!StringUtils.isBlank(result)) {
                                    barcode = result;
                                    break;
                                }
                            }
                            if (barcode == null) {
                                YC104CActivity.this.showButtonColorFault(R.id.button_ReadBarcode);
                                YC104CActivity.this.hsmpTestResult.put("BARCODE_READ", TestResult.FAULT);
                                return;
                            } else if (barcode.indexOf("\n") == -1 && barcode.indexOf("\r") == -1) {
                                YC104CActivity.this.showMsg(StringUtils.replace(YC104CActivity.this.getString(R.string.barcode_gun_set_error), "$BARCODE$", "[" + StringUtils.replace(StringUtils.replace(barcode, "\r", "\\r"), "\n", "\\n") + "]"));
                                YC104CActivity.this.showButtonColorFault(R.id.button_ReadBarcode);
                                YC104CActivity.this.hsmpTestResult.put("BARCODE_READ", TestResult.FAULT);
                                return;
                            } else {
                                YC104CActivity.this.enableConfirm(R.id.button_ReadBarcode, YC104CActivity.this.getString(R.string.check_barcode) + barcode.trim() + "]");
                                return;
                            }
                        }
                        YC104CActivity.this.showMsg("PLC[" + SysConfig.get("COM.PLC." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CheckPrinter1)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                EditText editTextOptr = (EditText) YC104CActivity.this.findViewById(R.id.editText_Optr);
                EditText editTextRvmCode = (EditText) YC104CActivity.this.findViewById(R.id.editText_RvmCode);
                YC104CActivity.this.optr = editTextOptr.getText().toString();
                YC104CActivity.this.rvmCode = editTextRvmCode.getText().toString();
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PRINTER1:CHECK_OPEN")))) {
                            StringBuffer sb = new StringBuffer();
                            sb.append(YC104CActivity.this.getString(R.string.tester) + ":" + YC104CActivity.this.optr + "\r\n");
                            sb.append(YC104CActivity.this.getString(R.string.terminal_no) + ":" + YC104CActivity.this.rvmCode + "\r\n");
                            sb.append(YC104CActivity.this.getString(R.string.test_time) + ":" + DateUtils.formatDatetime(new Date(), "yyyy-MM-dd HH:mm:ss") + "\r\n");
                            sb.append(YC104CActivity.this.getString(R.string.printer1_test_result));
                            for (int i = 0; i < YC104CActivity.this.testItemSet.length; i++) {
                                sb.append(YC104CActivity.this.testItemSet[i][1] + ":");
                                TestResult testResult = (TestResult) YC104CActivity.this.hsmpTestResult.get(YC104CActivity.this.testItemSet[i][0]);
                                if (testResult == null) {
                                    testResult = TestResult.UNKNOWN;
                                }
                                if (testResult == TestResult.UNKNOWN) {
                                    sb.append(YC104CActivity.this.getString(R.string.no_test));
                                }
                                if (testResult == TestResult.SUCCESSFUL) {
                                    sb.append(YC104CActivity.this.getString(R.string.pass_test));
                                }
                                if (testResult == TestResult.FAULT) {
                                    sb.append(YC104CActivity.this.getString(R.string.fail_test));
                                }
                                sb.append("\r\n");
                            }
                            try {
                                if (YC104CActivity.executeCheck("PRINTER1:state") == null) {
                                    YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.printer1_noresonpse));
                                    YC104CActivity.this.showButtonColorFault(R.id.button_CheckPrinter1);
                                    YC104CActivity.this.hsmpTestResult.put("PRINTER1_CHECK", TestResult.FAULT);
                                    sb.append(YC104CActivity.this.getString(R.string.printer1_fail));
                                } else {
                                    YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.printer1_resonpse));
                                    YC104CActivity.this.showButtonColorSuccess(R.id.button_CheckPrinter1);
                                    YC104CActivity.this.hsmpTestResult.put("PRINTER1_CHECK", TestResult.SUCCESSFUL);
                                    sb.append(YC104CActivity.this.getString(R.string.printer1_success));
                                }
                                YC104CActivity.executeCheck("PRINTER1:init");
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
                                YC104CActivity.executeCheck(hsmpParam);
                                YC104CActivity.executeCheck("PRINTER1:cut");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.check_printer1_result));
                            return;
                        }
                        YC104CActivity.this.showMsg("Printer1[" + SysConfig.get("COM.PRINTER1." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_CheckPrinter2)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                EditText editTextOptr = (EditText) YC104CActivity.this.findViewById(R.id.editText_Optr);
                EditText editTextRvmCode = (EditText) YC104CActivity.this.findViewById(R.id.editText_RvmCode);
                YC104CActivity.this.optr = editTextOptr.getText().toString();
                YC104CActivity.this.rvmCode = editTextRvmCode.getText().toString();
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        if ("TRUE".equalsIgnoreCase(YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("PRINTER2:CHECK_OPEN")))) {
                            StringBuffer sb = new StringBuffer();
                            sb.append(YC104CActivity.this.getString(R.string.tester) + ":" + YC104CActivity.this.optr + "\r\n");
                            sb.append(YC104CActivity.this.getString(R.string.terminal_no) + ":" + YC104CActivity.this.rvmCode + "\r\n");
                            sb.append(YC104CActivity.this.getString(R.string.test_time) + ":" + DateUtils.formatDatetime(new Date(), "yyyy-MM-dd HH:mm:ss") + "\r\n");
                            sb.append(YC104CActivity.this.getString(R.string.printer2_test_result));
                            for (int i = 0; i < YC104CActivity.this.testItemSet.length; i++) {
                                sb.append(YC104CActivity.this.testItemSet[i][1] + ":");
                                TestResult testResult = (TestResult) YC104CActivity.this.hsmpTestResult.get(YC104CActivity.this.testItemSet[i][0]);
                                if (testResult == null) {
                                    testResult = TestResult.UNKNOWN;
                                }
                                if (testResult == TestResult.UNKNOWN) {
                                    sb.append(YC104CActivity.this.getString(R.string.no_test));
                                }
                                if (testResult == TestResult.SUCCESSFUL) {
                                    sb.append(YC104CActivity.this.getString(R.string.pass_test));
                                }
                                if (testResult == TestResult.FAULT) {
                                    sb.append(YC104CActivity.this.getString(R.string.fail_test));
                                }
                                sb.append("\r\n");
                            }
                            try {
                                if (YC104CActivity.executeCheck("PRINTER2:state") == null) {
                                    YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.printer2_noresonpse));
                                    YC104CActivity.this.showButtonColorFault(R.id.button_CheckPrinter2);
                                    YC104CActivity.this.hsmpTestResult.put("PRINTER2_CHECK", TestResult.FAULT);
                                    sb.append(YC104CActivity.this.getString(R.string.printer2_fail));
                                } else {
                                    YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.printer2_resonpse));
                                    YC104CActivity.this.showButtonColorSuccess(R.id.button_CheckPrinter2);
                                    YC104CActivity.this.hsmpTestResult.put("PRINTER2_CHECK", TestResult.SUCCESSFUL);
                                    sb.append(YC104CActivity.this.getString(R.string.printer2_success));
                                }
                                YC104CActivity.executeCheck("PRINTER2:init");
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
                                YC104CActivity.executeCheck(hsmpParam);
                                YC104CActivity.executeCheck("PRINTER2:cut");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.check_printer2_result));
                            return;
                        }
                        YC104CActivity.this.showMsg("Printer2[" + SysConfig.get("COM.PRINTER2." + SysConfig.get("PLATFORM")) + YC104CActivity.this.getString(R.string.unable_open));
                    }
                });
            }
        });
        ((Button) findViewById(R.id.button_ReadTransferCard)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.prepare_read_card));
                        YC104CActivity.safeSleep(3000);
                        String ONECARD_NUM = null;
                        int i = 0;
                        while (i < 10) {
                            try {
                                String OneCardVerson = SysConfig.get("RVM.ONECARD.DRV.VERSION");
                                String result;
                                if (OneCardVerson.equals("0")) {
                                    result = YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("ONECARD:readOneCardReader"));
                                    if (!StringUtils.isBlank(result)) {
                                        ONECARD_NUM = (String) JSONUtils.toHashMap(result).get("ONECARD_NUM");
                                    }
                                } else if (OneCardVerson.equals("1")) {
                                    result = YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("TRAFFICCARD:ReadCard"));
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
                                YC104CActivity.safeSleep(1000);
                                i++;
                            } catch (Exception e2) {
                                e2.printStackTrace();
                                return;
                            }
                        }
                        if (StringUtils.isBlank(ONECARD_NUM)) {
                            YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.not_read_card));
                            YC104CActivity.this.showButtonColorFault(R.id.button_ReadTransferCard);
                            YC104CActivity.this.hsmpTestResult.put("TRANSFERCARD_READ", TestResult.FAULT);
                            return;
                        }
                        YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.already_read_card) + ONECARD_NUM + "]");
                        YC104CActivity.this.showButtonColorSuccess(R.id.button_ReadTransferCard);
                        YC104CActivity.this.hsmpTestResult.put("TRANSFERCARD_READ", TestResult.SUCCESSFUL);
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
                    if (YC104CActivity.this.takePictureRemainedTimes > 0) {
                        SysGlobal.execute(new Thread() {
                            public void run() {
                                Bitmap bitmap = YC104CActivity.this.cameraManager.takePicture();
                                if (bitmap != null) {
                                    String qrCode = QRDecodeHelper.decode(bitmap);
                                    if (StringUtils.isBlank(qrCode)) {
                                        YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.image_not_qrcode));
                                    } else {
                                        YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.image_is_qrcode) + qrCode + "]");
                                    }
                                    bitmap.recycle();
                                    YC104CActivity.this.takePictureCount = YC104CActivity.this.takePictureCount + 1;
                                }
                                YC104CActivity.this.takePictureRemainedTimes = YC104CActivity.this.takePictureRemainedTimes - 1;
                                if (YC104CActivity.this.TAKE_MAX_TIMES == YC104CActivity.this.takePictureCount) {
                                    YC104CActivity.this.showButtonColorSuccess(R.id.button_OpenCamera);
                                    YC104CActivity.this.hsmpTestResult.put("CAMERA_CHECK", TestResult.SUCCESSFUL);
                                } else {
                                    YC104CActivity.this.showButtonColorFault(R.id.button_OpenCamera);
                                    YC104CActivity.this.hsmpTestResult.put("CAMERA_CHECK", TestResult.FAULT);
                                }
                                Message message = new Message();
                                message.what = MsgWhat.SHOW_CAMERA;
                                if (YC104CActivity.this.takePictureRemainedTimes > 0) {
                                    message.obj = YC104CActivity.this.getString(R.string.has) + YC104CActivity.this.takePictureRemainedTimes + YC104CActivity.this.getString(R.string.ci);
                                } else {
                                    message.obj = YC104CActivity.this.getString(R.string.camera_test);
                                }
                                YC104CActivity.this.handler.sendMessage(message);
                                if (YC104CActivity.this.takePictureRemainedTimes == 0) {
                                    YC104CActivity.this.cameraManager.stopPreview();
                                    YC104CActivity.this.cameraManager.closeDriver();
                                }
                            }
                        });
                        return;
                    }
                    YC104CActivity.this.takePictureRemainedTimes = YC104CActivity.this.TAKE_MAX_TIMES;
                    YC104CActivity.this.takePictureCount = 0;
                    ((Button) YC104CActivity.this.findViewById(R.id.button_OpenCamera)).setText(YC104CActivity.this.getString(R.string.please_take_photo));
                    try {
                        YC104CActivity.this.cameraManager.openDriver(0, ((SurfaceView) YC104CActivity.this.findViewById(R.id.surfaceView_Camera)).getHolder());
                        YC104CActivity.this.cameraManager.startPreview();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Button buttonCheckWeigh = (Button) findViewById(R.id.button_WeighRead);
        if ("true".equalsIgnoreCase(SysConfig.get("COM.WEIGH.ENABLE"))) {
            buttonCheckWeigh.setVisibility(View.VISIBLE);
        }
        buttonCheckWeigh.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                SysGlobal.execute(new Thread() {
                    public void run() {
                        YC104CActivity.this.clearMsg();
                        YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.weigh_read_remind));
                        YC104CActivity.safeSleep(3000);
                        String weighStr = YC104CActivity.getExecuteCheckResult(YC104CActivity.executeCheck("WEIGH_READ"));
                        double weigh = 0.0d;
                        if (weighStr != null) {
                            weigh = Double.parseDouble(weighStr);
                        }
                        YC104CActivity.this.showMsg(YC104CActivity.this.getString(R.string.weigh_read_result) + weigh + "g");
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
