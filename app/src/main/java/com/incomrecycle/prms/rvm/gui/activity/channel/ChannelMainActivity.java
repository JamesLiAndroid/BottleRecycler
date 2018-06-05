package com.incomrecycle.prms.rvm.gui.activity.channel;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.json.JSONObject;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef;
import com.incomrecycle.prms.rvm.common.SysDef.maintainOptContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.action.GUIActionClearCount;
import com.incomrecycle.prms.rvm.gui.action.GUIActionClearWeight;
import com.incomrecycle.prms.rvm.gui.action.GUIActionOpenDoor;
import com.incomrecycle.prms.rvm.gui.action.GUIActionOpenPaperDoor;
import com.incomrecycle.prms.rvm.gui.activity.view.MyDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

@SuppressLint({"ResourceAsColor"})
public class ChannelMainActivity extends BaseActivity {
    protected static final String DATABASE = "spData";
    protected Bitmap bitmap;
    private String cleanNumBtn_Sts = null;
    private String cleanWeightBtn_Sts = null;
    private String dwonOpenDoorBtn_Sts = null;
    private String dwonOpenPaperDoorBtn_Sts = null;
    List<HashMap<String, String>> listShowInfo = null;
    List<String> optList = new ArrayList();
    private boolean requestSts = false;
    private String staffPermission = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        if (ChannelMainActivity.this.optList.size() > 0 && !ChannelMainActivity.this.requestSts) {
                            SysGlobal.execute(new Runnable() {
                                public void run() {
                                    ChannelMainActivity.this.requestSts = true;
                                    HashMap haspParam = new HashMap();
                                    haspParam.put("OPT_OPTIONS", ChannelMainActivity.this.optList);
                                    try {
                                        CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "maintainAddOptCon", haspParam);
                                        CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "maintainToRCC", null);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                        if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                            try {
                                Intent intent = new Intent(ChannelMainActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                ChannelMainActivity.this.startActivity(intent);
                                ChannelMainActivity.this.finish();
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                        return;
                    }
                    ((TextView) ChannelMainActivity.this.findViewById(R.id.channel_main_time)).setText("" + remainedSeconds);
                }
            };
            ChannelMainActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    public void onStart() {
        super.onStart();
        if ("TRUE".equalsIgnoreCase(getIntent().getStringExtra("LIST_VALUE"))) {
            this.optList = getIntent().getStringArrayListExtra("LIST");
        }
        DecimalFormat df = new DecimalFormat("0.00");
        String putdownCount = "0";
        String putdownCountDelta = "0";
        String putdownWeight = "0";
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        try {
            HashMap<String, Object> hsmpStorageCount = guiCommonService.execute("GUIQueryCommonService", "queryStorageCount", null);
            putdownCount = (String) hsmpStorageCount.get("STORAGE_CURR_COUNT");
            putdownCountDelta = (String) hsmpStorageCount.get("STORAGE_CURR_COUNT_DELTA");
            putdownWeight = "" + df.format(Double.parseDouble((String) guiCommonService.execute("GUIQueryCommonService", "queryStorageWeight", null).get("STORAGE_CURR_PAPER_WEIGH")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (StringUtils.isBlank(putdownCount)) {
            putdownCount = "0";
        }
        if (StringUtils.isBlank(putdownCountDelta)) {
            putdownCountDelta = "0";
        }
        if (StringUtils.isBlank(putdownWeight)) {
            putdownWeight = "0";
        }
        String putdownCountReal = "0";
        if (Integer.parseInt(putdownCount) > Integer.parseInt(putdownCountDelta)) {
            putdownCountReal = Integer.toString(Integer.parseInt(putdownCount) - Integer.parseInt(putdownCountDelta));
        }
        TextView clearBottleNumText = (TextView) findViewById(R.id.bottle_num_text);
        if ("0".equals(putdownCountDelta)) {
            clearBottleNumText.setText(putdownCountReal);
        } else {
            clearBottleNumText.setText(putdownCountReal + "(" + putdownCount + ")");
        }
        ((TextView) findViewById(R.id.paper_weight_text)).setText(putdownWeight);
        int timeout = Integer.valueOf(SysConfig.get("RVM.TIMEOUT.MAINTAIN")).intValue();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, timeout, false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
        if (this.bitmap != null) {
            this.bitmap.recycle();
            this.bitmap = null;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        HashMap<String, String> hsmpItem;
        super.onCreate(savedInstanceState);
        decideStaffPermission();
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_channel_main);
        View showPaperWeightLayout = findViewById(R.id.paper_weight_layout);
        View showBottleNumLayout = findViewById(R.id.bottle_num_layout);
        String recycleServiceSet = SysConfig.get("RECYCLE.SERVICE.SET");
        List<String> listRecycleServiceSet = new ArrayList();
        if (!StringUtils.isBlank(recycleServiceSet)) {
            String[] recycleServiceSetArray = recycleServiceSet.split(",");
            for (String add : recycleServiceSetArray) {
                listRecycleServiceSet.add(add);
            }
        }
        String advanceStaff = SysConfig.get("ADVANCE_STAFF.PERMISSION.SET");
        String generalStaff = SysConfig.get("GENERAL_STAFF.PERMISSION.SET");
        List<String> listStaffSet = new ArrayList();
        if (!StringUtils.isBlank(staffPermission)) {
            if (this.staffPermission.equalsIgnoreCase(SysDef.staffPermission.ADVANCE_STAFF) && !StringUtils.isBlank(advanceStaff)) {
                String[] advanceStaffSetArray = advanceStaff.split(";");
                for (String add2 : advanceStaffSetArray) {
                    listStaffSet.add(add2);
                }
            }
            if (this.staffPermission.equalsIgnoreCase(SysDef.staffPermission.GENERAL_STAFF) && !StringUtils.isBlank(generalStaff)) {
                String[] generalStaffSetArray = generalStaff.split(";");
                for (String add22 : generalStaffSetArray) {
                    listStaffSet.add(add22);
                }
            }
        }
        this.listShowInfo = new ArrayList();
        if (listRecycleServiceSet.contains("PAPER")) {
            if (listStaffSet.contains(SysDef.staffPermission.cleanWeight)) {
                hsmpItem = new HashMap();
                hsmpItem.put("ItemInfo", getString(R.string.cleanWeightBtn));
                this.listShowInfo.add(hsmpItem);
            }
            if (listStaffSet.contains(SysDef.staffPermission.dwonOpenDoor)) {
                hsmpItem = new HashMap();
                hsmpItem.put("ItemInfo", getString(R.string.dwonOpenPaperDoorBtn));
                this.listShowInfo.add(hsmpItem);
            }
            showPaperWeightLayout.setVisibility(View.GONE);
        }
        if (listRecycleServiceSet.contains("BOTTLE")) {
            if (listStaffSet.contains(SysDef.staffPermission.cleanNum)) {
                hsmpItem = new HashMap();
                hsmpItem.put("ItemInfo", getString(R.string.cleanNumBtn));
                this.listShowInfo.add(hsmpItem);
            }
            if (listStaffSet.contains(SysDef.staffPermission.dwonOpenDoor)) {
                hsmpItem = new HashMap();
                hsmpItem.put("ItemInfo", getString(R.string.dwonOpenDoorBtn));
                this.listShowInfo.add(hsmpItem);
            }
            showBottleNumLayout.setVisibility(View.VISIBLE);
        }
        if (listStaffSet.contains(SysDef.staffPermission.wringList)) {
            hsmpItem = new HashMap();
            hsmpItem.put("ItemInfo", getString(R.string.wringListBtn));
            this.listShowInfo.add(hsmpItem);
        }
        if (listStaffSet.contains(SysDef.staffPermission.advance)) {
            hsmpItem = new HashMap();
            hsmpItem.put("ItemInfo", getString(R.string.advanceBtn));
            this.listShowInfo.add(hsmpItem);
        }
        if (listStaffSet.contains(SysDef.staffPermission.checkIn)) {
            hsmpItem = new HashMap();
            hsmpItem.put("ItemInfo", getString(R.string.check_in));
            this.listShowInfo.add(hsmpItem);
        }
        GridView gridview = (GridView) findViewById(R.id.gridview_show_button);
        gridview.setAdapter(new SimpleAdapter(this, this.listShowInfo, R.layout.activity_show_channel_text, new String[]{"ItemInfo"}, new int[]{R.id.channel_text_item}));
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String clikItemText = (String) ((HashMap) ChannelMainActivity.this.listShowInfo.get(position)).get("ItemInfo");
                final Object[] objs = new Object[]{ChannelMainActivity.this};
                TimeoutTask.getTimeoutTask().reset(ChannelMainActivity.this.timeoutAction);
                if (ChannelMainActivity.this.getString(R.string.cleanWeightBtn).equals(clikItemText)) {
                    new Builder(ChannelMainActivity.this).setTitle(ChannelMainActivity.this.getResources().getString(R.string.Hint)).setMessage(ChannelMainActivity.this.getResources().getString(R.string.clear_paperWeight)).setPositiveButton(ChannelMainActivity.this.getResources().getString(R.string.confirm), new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (ChannelMainActivity.this.cleanWeightBtn_Sts == null) {
                                ChannelMainActivity.this.cleanWeightBtn_Sts = maintainOptContent.PAPER_CLEAR;
                                ChannelMainActivity.this.optList.add(ChannelMainActivity.this.cleanWeightBtn_Sts);
                            }
                            ChannelMainActivity.this.executeGUIAction(true, new GUIActionClearWeight(), objs);
                        }
                    }).setNegativeButton(ChannelMainActivity.this.getResources().getString(R.string.alarmCancel), null).show();
                }
                if (ChannelMainActivity.this.getString(R.string.dwonOpenPaperDoorBtn).equals(clikItemText)) {
                    if (ChannelMainActivity.this.dwonOpenPaperDoorBtn_Sts == null) {
                        ChannelMainActivity.this.dwonOpenPaperDoorBtn_Sts = maintainOptContent.OPEN_PAPER_DOOR;
                        ChannelMainActivity.this.optList.add(ChannelMainActivity.this.dwonOpenPaperDoorBtn_Sts);
                    }
                    ChannelMainActivity.this.executeGUIAction(true, new GUIActionOpenPaperDoor(), objs);
                }
                if (ChannelMainActivity.this.getString(R.string.cleanNumBtn).equals(clikItemText)) {
                    showMyDialog(objs);
                }
                if (ChannelMainActivity.this.getString(R.string.dwonOpenDoorBtn).equals(clikItemText)) {
                    if (ChannelMainActivity.this.dwonOpenDoorBtn_Sts == null) {
                        ChannelMainActivity.this.dwonOpenDoorBtn_Sts = maintainOptContent.OPEN_BOTTLE_DOOR;
                        ChannelMainActivity.this.optList.add(ChannelMainActivity.this.dwonOpenDoorBtn_Sts);
                    }
                    ChannelMainActivity.this.executeGUIAction(true, new GUIActionOpenDoor(), objs);
                }
                if (ChannelMainActivity.this.getString(R.string.wringListBtn).equals(clikItemText)) {
                    Intent intent = new Intent();
                    intent.setClass(ChannelMainActivity.this, AlarmListActivity.class);
                    intent.putExtra("STAFF_PERMISSION", ChannelMainActivity.this.staffPermission);
                    intent.putStringArrayListExtra("LIST", (ArrayList) ChannelMainActivity.this.optList);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    ChannelMainActivity.this.startActivity(intent);
                    ChannelMainActivity.this.finish();
                }
                if (ChannelMainActivity.this.getString(R.string.advanceBtn).equals(clikItemText)) {
                    Intent intent = new Intent(ChannelMainActivity.this, ChannelAdvancedActivity.class);
                    intent.putExtra("STAFF_PERMISSION", ChannelMainActivity.this.staffPermission);
                    intent.putStringArrayListExtra("LIST", (ArrayList) ChannelMainActivity.this.optList);
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    ChannelMainActivity.this.startActivity(intent);
                    ChannelMainActivity.this.finish();
                }
                if (ChannelMainActivity.this.getString(R.string.check_in).equals(clikItemText)) {
                    try {
                        SysGlobal.execute(new Runnable() {
                            public void run() {
                                HashMap<String, Object> hashResult = null;
                                try {
                                    hashResult = CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "workerSignIn", null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String result = null;
                                if (hashResult != null && hashResult.size() > 0) {
                                    result = (String) hashResult.get("RESULT");
                                }
                                Looper.prepare();
                                if (StringUtils.isBlank(result) || !"success".equalsIgnoreCase(result)) {
                                    Toast.makeText(ChannelMainActivity.this, R.string.check_in_failed, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ChannelMainActivity.this, R.string.check_in_success, Toast.LENGTH_SHORT).show();
                                }
                                Looper.loop();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    TimeoutTask.getTimeoutTask().reset(ChannelMainActivity.this.timeoutAction);
                }
            }

            private Bitmap generateBitmap(String content, int width, int height) {
                MultiFormatWriter qrCodeWriter = new MultiFormatWriter();
                Hashtable<EncodeHintType, String> hints = new Hashtable();
                hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
                try {
                    BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
                    int[] pixels = new int[(width * height)];
                    for (int i = 0; i < height; i++) {
                        for (int j = 0; j < width; j++) {
                            if (encode.get(j, i)) {
                                pixels[(i * width) + j] = 0;
                            } else {
                                pixels[(i * width) + j] = -1;
                            }
                        }
                    }
                    return Bitmap.createBitmap(pixels, 0, width, width, height, Config.RGB_565);
                } catch (WriterException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            private void showMyDialog(final Object[] objs) {
                GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                HashMap<String, Object> map = new HashMap();
                map.put("ISRECOVERY_MANUAL", Boolean.valueOf(false));
                try {
                    final HashMap<String, Object> hsmpResult1 = guiCommonService.execute("GUIMaintenanceCommonService", "queryFullStoreAlarm", map);
                    new Builder(ChannelMainActivity.this).setTitle(ChannelMainActivity.this.getResources().getString(R.string.Hint)).setMessage(ChannelMainActivity.this.getResources().getString(R.string.clear_bottleNumber)).setPositiveButton(ChannelMainActivity.this.getResources().getString(R.string.confirm), new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (ChannelMainActivity.this.cleanNumBtn_Sts == null) {
                                ChannelMainActivity.this.cleanNumBtn_Sts = maintainOptContent.BOTTLE_CLEAR;
                                ChannelMainActivity.this.optList.add(ChannelMainActivity.this.cleanNumBtn_Sts);
                            }
                            ChannelMainActivity.this.executeGUIAction(true, new GUIActionClearCount(), objs);
                            if (hsmpResult1 != null) {
                                HashMap<String, Object> map1 = new HashMap();
                                map1.put("ISRECOVERY_MANUAL", Boolean.valueOf(true));
                                map1.put("ALARM_INST_ID", hsmpResult1.get("ALARM_INST_ID"));
                                try {
                                    Map hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "queryFullStoreAlarm", map1);
                                    if (!(hsmpResult == null || hsmpResult.isEmpty() || Integer.valueOf((String) hsmpResult.get("OPER_STS")).intValue() != 3)) {
                                        String result = new JSONObject(hsmpResult).toString();
                                        ChannelMainActivity.this.bitmap = generateBitmap(result, 233, 233);
                                        MyDialog.Builder builder = new MyDialog.Builder(ChannelMainActivity.this);
                                        builder.setPositiveButton((int) R.string.sure, new OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        builder.setNegativeButton((int) R.string.cancle, new OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        builder.create(ChannelMainActivity.this.bitmap).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            dialog.dismiss();
                        }
                    }).setNegativeButton(ChannelMainActivity.this.getResources().getString(R.string.alarmCancel), null).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ((Button) findViewById(R.id.channel_main_return_btn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ChannelMainActivity.this.optList.size() > 0 && !ChannelMainActivity.this.requestSts) {
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            ChannelMainActivity.this.requestSts = true;
                            HashMap haspParam = new HashMap();
                            haspParam.put("OPT_OPTIONS", ChannelMainActivity.this.optList);
                            try {
                                CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "maintainAddOptCon", haspParam);
                                CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "maintainToRCC", null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(ChannelMainActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        ChannelMainActivity.this.startActivity(intent);
                        ChannelMainActivity.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    public void decideStaffPermission() {
        this.staffPermission = getIntent().getStringExtra("STAFF_PERMISSION");
    }
}
