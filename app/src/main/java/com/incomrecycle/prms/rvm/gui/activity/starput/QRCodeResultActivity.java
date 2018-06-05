package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.app.Dialog;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.sauronsoftware.ftp4j.FTPCodes;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class QRCodeResultActivity extends BaseActivity {
    private String JSON;
    private int TODAY_BOTTLE = 0;
    private TextView balanceOfINCOM = null;
    private int fanLiWanCheng;
    private boolean isPlaySounds;
    private TextView rebateThis = null;
    private TextView recharge_warningtext = null;
    private View schoolQRcodeInfoLayout = null;
    private TextView schoolQRcodeInfoText = null;
    private View societyQRcodeRebateInfoLayout = null;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        Intent intent = new Intent();
                        if (!StringUtils.isBlank(QRCodeResultActivity.this.JSON) && "BOTTLE".equalsIgnoreCase(ServiceGlobal.getCurrentSession("PRODUCT_TYPE").toString())) {
                            intent.setClass(QRCodeResultActivity.this, EnvironmentalPromotionalActivity.class);
                            intent.putExtra("JSON", QRCodeResultActivity.this.JSON);
                        } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                            try {
                                intent.setClass(QRCodeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        QRCodeResultActivity.this.startActivity(intent);
                        QRCodeResultActivity.this.finish();
                        return;
                    }
                    ((TextView) QRCodeResultActivity.this.findViewById(R.id.QRcode_rebate_time)).setText("" + remainedSeconds);
                }
            };
            QRCodeResultActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private String totalMoney;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_qrcode_result);
        backgroundToActivity();
        initView();
        ((Button) findViewById(R.id.QRcode_rebate_end)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.REBATE_QRCODE_CONFIRM);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                Intent intent = new Intent();
                if (!StringUtils.isBlank(QRCodeResultActivity.this.JSON) && "BOTTLE".equalsIgnoreCase(ServiceGlobal.getCurrentSession("PRODUCT_TYPE").toString())) {
                    intent.setClass(QRCodeResultActivity.this, EnvironmentalPromotionalActivity.class);
                    intent.putExtra("JSON", QRCodeResultActivity.this.JSON);
                } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        intent.setClass(QRCodeResultActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                QRCodeResultActivity.this.startActivity(intent);
                QRCodeResultActivity.this.finish();
            }
        });
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    // TODO: 待修复！
    protected void onStart() {
        /*
        r27 = this;
        super.onStart();
        r23 = "IS_PLAY_SOUNDS";
        r23 = com.incomrecycle.common.SysConfig.get(r23);
        r23 = java.lang.Boolean.parseBoolean(r23);
        r0 = r23;
        r1 = r27;
        r1.isPlaySounds = r0;
        r0 = r27;
        r0 = r0.isPlaySounds;
        r23 = r0;
        if (r23 == 0) goto L_0x0034;
    L_0x001b:
        r0 = r27;
        r0 = r0.soundPool;
        r23 = r0;
        if (r23 != 0) goto L_0x0034;
    L_0x0023:
        r23 = new android.media.SoundPool;
        r24 = 1;
        r25 = 3;
        r26 = 0;
        r23.<init>(r24, r25, r26);
        r0 = r23;
        r1 = r27;
        r1.soundPool = r0;
    L_0x0034:
        r8 = new java.text.DecimalFormat;
        r23 = "0.00";
        r0 = r23;
        r8.<init>(r0);
        r12 = r27.getIntent();
        r10 = 0;
        r23 = "JSON";
        r0 = r23;
        r23 = r12.getStringExtra(r0);	 Catch:{ Exception -> 0x011a }
        r0 = r23;
        r1 = r27;
        r1.JSON = r0;	 Catch:{ Exception -> 0x011a }
        r0 = r27;
        r0 = r0.JSON;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r10 = com.incomrecycle.common.json.JSONUtils.toHashMap(r23);	 Catch:{ Exception -> 0x011a }
        if (r10 == 0) goto L_0x031e;
    L_0x005c:
        r23 = "TODAY_BOTTLE";
        r0 = r23;
        r22 = r10.get(r0);	 Catch:{ Exception -> 0x011a }
        r22 = (java.lang.String) r22;	 Catch:{ Exception -> 0x011a }
        r23 = com.incomrecycle.common.utils.StringUtils.isBlank(r22);	 Catch:{ Exception -> 0x011a }
        if (r23 != 0) goto L_0x0076;
    L_0x006c:
        r23 = java.lang.Integer.parseInt(r22);	 Catch:{ Exception -> 0x011a }
        r0 = r23;
        r1 = r27;
        r1.TODAY_BOTTLE = r0;	 Catch:{ Exception -> 0x011a }
    L_0x0076:
        r4 = -2;
        r23 = "CARD_STATUS";
        r0 = r23;
        r23 = r10.get(r0);	 Catch:{ Exception -> 0x0114 }
        r23 = (java.lang.String) r23;	 Catch:{ Exception -> 0x0114 }
        r4 = java.lang.Integer.parseInt(r23);	 Catch:{ Exception -> 0x0114 }
    L_0x0085:
        r23 = -1;
        r0 = r23;
        if (r4 == r0) goto L_0x0091;
    L_0x008b:
        r23 = -2;
        r0 = r23;
        if (r4 != r0) goto L_0x011f;
    L_0x0091:
        r0 = r27;
        r0 = r0.recharge_warningtext;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = 2131296526; // 0x7f09010e float:1.8210971E38 double:1.0530003946E-314;
        r23.setText(r24);	 Catch:{ Exception -> 0x011a }
        r0 = r27;
        r0 = r0.schoolQRcodeInfoLayout;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = 8;
        r23.setVisibility(r24);	 Catch:{ Exception -> 0x011a }
        r0 = r27;
        r0 = r0.societyQRcodeRebateInfoLayout;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = 8;
        r23.setVisibility(r24);	 Catch:{ Exception -> 0x011a }
    L_0x00b3:
        r23 = "PRODUCT_TYPE";
        r5 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r23);
        r5 = (java.lang.String) r5;
        r23 = "BOTTLE";
        r0 = r23;
        r23 = r0.equalsIgnoreCase(r5);
        if (r23 == 0) goto L_0x0336;
    L_0x00c5:
        r27.showPutBottleInfo();
        r23 = "CARD_TYPE";
        r7 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r23);
        r7 = (java.lang.String) r7;
        r23 = "QRCODE";
        r0 = r23;
        r23 = r7.equalsIgnoreCase(r0);
        if (r23 != 0) goto L_0x00da;
    L_0x00da:
        r23 = com.incomrecycle.common.task.TimeoutTask.getTimeoutTask();
        r0 = r27;
        r0 = r0.timeoutAction;
        r24 = r0;
        r25 = "RVM.TIMEOUT.TRANSPORTCARD";
        r25 = com.incomrecycle.common.SysConfig.get(r25);
        r25 = java.lang.Integer.valueOf(r25);
        r25 = r25.intValue();
        r26 = 0;
        r23.addTimeoutAction(r24, r25, r26);
        r23 = com.incomrecycle.common.task.TimeoutTask.getTimeoutTask();
        r0 = r27;
        r0 = r0.timeoutAction;
        r24 = r0;
        r23.reset(r24);
        r23 = com.incomrecycle.common.task.TimeoutTask.getTimeoutTask();
        r0 = r27;
        r0 = r0.timeoutAction;
        r24 = r0;
        r25 = 1;
        r23.setEnabled(r24, r25);
        return;
    L_0x0114:
        r9 = move-exception;
        r9.printStackTrace();	 Catch:{ Exception -> 0x011a }
        goto L_0x0085;
    L_0x011a:
        r9 = move-exception;
        r9.printStackTrace();
        goto L_0x00b3;
    L_0x011f:
        r0 = r27;
        r0 = r0.isPlaySounds;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        if (r23 == 0) goto L_0x015e;
    L_0x0127:
        r0 = r27;
        r0 = r0.soundPool;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        if (r23 == 0) goto L_0x015e;
    L_0x012f:
        r0 = r27;
        r0 = r0.soundPool;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = 2131034125; // 0x7f05000d float:1.7678759E38 double:1.0528707513E-314;
        r25 = 0;
        r0 = r23;
        r1 = r27;
        r2 = r24;
        r3 = r25;
        r23 = r0.load(r1, r2, r3);	 Catch:{ Exception -> 0x011a }
        r0 = r23;
        r1 = r27;
        r1.fanLiWanCheng = r0;	 Catch:{ Exception -> 0x011a }
        r0 = r27;
        r0 = r0.soundPool;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = new com.incomrecycle.prms.rvm.gui.activity.starput.QRCodeResultActivity$2;	 Catch:{ Exception -> 0x011a }
        r0 = r24;
        r1 = r27;
        r0.<init>();	 Catch:{ Exception -> 0x011a }
        r23.setOnLoadCompleteListener(r24);	 Catch:{ Exception -> 0x011a }
    L_0x015e:
        r23 = "CARD_TYPE";
        r7 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r23);	 Catch:{ Exception -> 0x011a }
        r7 = (java.lang.String) r7;	 Catch:{ Exception -> 0x011a }
        r23 = "QRCODE";
        r0 = r23;
        r23 = r7.equalsIgnoreCase(r0);	 Catch:{ Exception -> 0x011a }
        if (r23 == 0) goto L_0x021e;
    L_0x0170:
        r0 = r27;
        r0 = r0.schoolQRcodeInfoLayout;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = 0;
        r23.setVisibility(r24);	 Catch:{ Exception -> 0x011a }
        r0 = r27;
        r0 = r0.societyQRcodeRebateInfoLayout;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = 8;
        r23.setVisibility(r24);	 Catch:{ Exception -> 0x011a }
        r23 = "CREDIT";
        r0 = r23;
        r13 = r10.get(r0);	 Catch:{ Exception -> 0x011a }
        r13 = (java.lang.String) r13;	 Catch:{ Exception -> 0x011a }
        r23 = com.incomrecycle.common.utils.StringUtils.isBlank(r13);	 Catch:{ Exception -> 0x011a }
        if (r23 != 0) goto L_0x01b6;
    L_0x0196:
        r23 = 2131493323; // 0x7f0c01cb float:1.8610123E38 double:1.053097625E-314;
        r0 = r27;
        r1 = r23;
        r14 = r0.findViewById(r1);	 Catch:{ Exception -> 0x011a }
        r23 = 0;
        r0 = r23;
        r14.setVisibility(r0);	 Catch:{ Exception -> 0x011a }
        r23 = 2131493325; // 0x7f0c01cd float:1.8610127E38 double:1.053097626E-314;
        r0 = r23;
        r11 = r14.findViewById(r0);	 Catch:{ Exception -> 0x011a }
        r11 = (android.widget.TextView) r11;	 Catch:{ Exception -> 0x011a }
        r11.setText(r13);	 Catch:{ Exception -> 0x011a }
    L_0x01b6:
        r23 = "CARD_NAME";
        r0 = r23;
        r6 = r10.get(r0);	 Catch:{ Exception -> 0x011a }
        r6 = (java.lang.String) r6;	 Catch:{ Exception -> 0x011a }
        r23 = com.incomrecycle.common.utils.StringUtils.isBlank(r6);	 Catch:{ Exception -> 0x011a }
        if (r23 != 0) goto L_0x00b3;
    L_0x01c6:
        r23 = 2131493321; // 0x7f0c01c9 float:1.8610119E38 double:1.053097624E-314;
        r0 = r27;
        r1 = r23;
        r23 = r0.findViewById(r1);	 Catch:{ Exception -> 0x011a }
        r23 = (android.widget.TextView) r23;	 Catch:{ Exception -> 0x011a }
        r24 = 0;
        r23.setVisibility(r24);	 Catch:{ Exception -> 0x011a }
        r23 = 2131296761; // 0x7f0901f9 float:1.8211448E38 double:1.0530005107E-314;
        r0 = r27;
        r1 = r23;
        r23 = r0.getString(r1);	 Catch:{ Exception -> 0x011a }
        r24 = "$CARD_NAME$";
        r25 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x011a }
        r25.<init>();	 Catch:{ Exception -> 0x011a }
        r26 = "<big><font>";
        r25 = r25.append(r26);	 Catch:{ Exception -> 0x011a }
        r0 = r25;
        r25 = r0.append(r6);	 Catch:{ Exception -> 0x011a }
        r26 = "</font></big>";
        r25 = r25.append(r26);	 Catch:{ Exception -> 0x011a }
        r25 = r25.toString();	 Catch:{ Exception -> 0x011a }
        r15 = com.incomrecycle.common.utils.StringUtils.replace(r23, r24, r25);	 Catch:{ Exception -> 0x011a }
        r0 = r27;
        r0 = r0.schoolQRcodeInfoText;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = android.text.Html.fromHtml(r15);	 Catch:{ Exception -> 0x011a }
        r23.setText(r24);	 Catch:{ Exception -> 0x011a }
        r0 = r27;
        r0 = r0.schoolQRcodeInfoText;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = 0;
        r23.setVisibility(r24);	 Catch:{ Exception -> 0x011a }
        goto L_0x00b3;
    L_0x021e:
        r0 = r27;
        r0 = r0.schoolQRcodeInfoLayout;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = 8;
        r23.setVisibility(r24);	 Catch:{ Exception -> 0x011a }
        r0 = r27;
        r0 = r0.societyQRcodeRebateInfoLayout;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = 0;
        r23.setVisibility(r24);	 Catch:{ Exception -> 0x011a }
        r23 = "CREDIT";
        r0 = r23;
        r13 = r10.get(r0);	 Catch:{ Exception -> 0x011a }
        r13 = (java.lang.String) r13;	 Catch:{ Exception -> 0x011a }
        r23 = com.incomrecycle.common.utils.StringUtils.isBlank(r13);	 Catch:{ Exception -> 0x011a }
        if (r23 != 0) goto L_0x0264;
    L_0x0244:
        r23 = 2131492916; // 0x7f0c0034 float:1.8609297E38 double:1.053097424E-314;
        r0 = r27;
        r1 = r23;
        r14 = r0.findViewById(r1);	 Catch:{ Exception -> 0x011a }
        r23 = 0;
        r0 = r23;
        r14.setVisibility(r0);	 Catch:{ Exception -> 0x011a }
        r23 = 2131492918; // 0x7f0c0036 float:1.8609301E38 double:1.053097425E-314;
        r0 = r23;
        r11 = r14.findViewById(r0);	 Catch:{ Exception -> 0x011a }
        r11 = (android.widget.TextView) r11;	 Catch:{ Exception -> 0x011a }
        r11.setText(r13);	 Catch:{ Exception -> 0x011a }
    L_0x0264:
        r20 = 0;
        r23 = "INCOM_AMOUNT";
        r0 = r23;
        r23 = r10.get(r0);	 Catch:{ Exception -> 0x0313 }
        r23 = (java.lang.String) r23;	 Catch:{ Exception -> 0x0313 }
        r23 = java.lang.Double.parseDouble(r23);	 Catch:{ Exception -> 0x0313 }
        r25 = 4636737291354636288; // 0x4059000000000000 float:0.0 double:100.0;
        r23 = r23 / r25;
        r25 = "LOCAL_EXCHANGE_RATE";
        r25 = com.incomrecycle.common.SysConfig.get(r25);	 Catch:{ Exception -> 0x0313 }
        r25 = java.lang.Integer.parseInt(r25);	 Catch:{ Exception -> 0x0313 }
        r0 = r25;
        r0 = (double) r0;
        r25 = r0;
        r20 = r23 * r25;
    L_0x0289:
        r0 = r27;
        r0 = r0.balanceOfINCOM;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x011a }
        r24.<init>();	 Catch:{ Exception -> 0x011a }
        r25 = "";
        r24 = r24.append(r25);	 Catch:{ Exception -> 0x011a }
        r0 = r20;
        r25 = r8.format(r0);	 Catch:{ Exception -> 0x011a }
        r24 = r24.append(r25);	 Catch:{ Exception -> 0x011a }
        r24 = r24.toString();	 Catch:{ Exception -> 0x011a }
        r23.setText(r24);	 Catch:{ Exception -> 0x011a }
        r18 = 0;
        r23 = "RECHARGE";
        r0 = r23;
        r23 = r10.get(r0);	 Catch:{ Exception -> 0x0319 }
        r23 = (java.lang.String) r23;	 Catch:{ Exception -> 0x0319 }
        r23 = java.lang.Double.parseDouble(r23);	 Catch:{ Exception -> 0x0319 }
        r25 = 4636737291354636288; // 0x4059000000000000 float:0.0 double:100.0;
        r23 = r23 / r25;
        r25 = "LOCAL_EXCHANGE_RATE";
        r25 = com.incomrecycle.common.SysConfig.get(r25);	 Catch:{ Exception -> 0x0319 }
        r25 = java.lang.Integer.parseInt(r25);	 Catch:{ Exception -> 0x0319 }
        r0 = r25;
        r0 = (double) r0;
        r25 = r0;
        r18 = r23 * r25;
    L_0x02d0:
        r0 = r27;
        r0 = r0.rebateThis;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x011a }
        r24.<init>();	 Catch:{ Exception -> 0x011a }
        r25 = "";
        r24 = r24.append(r25);	 Catch:{ Exception -> 0x011a }
        r0 = r18;
        r25 = r8.format(r0);	 Catch:{ Exception -> 0x011a }
        r24 = r24.append(r25);	 Catch:{ Exception -> 0x011a }
        r24 = r24.toString();	 Catch:{ Exception -> 0x011a }
        r23.setText(r24);	 Catch:{ Exception -> 0x011a }
        r23 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x011a }
        r23.<init>();	 Catch:{ Exception -> 0x011a }
        r24 = "";
        r23 = r23.append(r24);	 Catch:{ Exception -> 0x011a }
        r0 = r18;
        r24 = r8.format(r0);	 Catch:{ Exception -> 0x011a }
        r23 = r23.append(r24);	 Catch:{ Exception -> 0x011a }
        r23 = r23.toString();	 Catch:{ Exception -> 0x011a }
        r0 = r23;
        r1 = r27;
        r1.totalMoney = r0;	 Catch:{ Exception -> 0x011a }
        goto L_0x00b3;
    L_0x0313:
        r9 = move-exception;
        r9.printStackTrace();	 Catch:{ Exception -> 0x011a }
        goto L_0x0289;
    L_0x0319:
        r9 = move-exception;
        r9.printStackTrace();	 Catch:{ Exception -> 0x011a }
        goto L_0x02d0;
    L_0x031e:
        r0 = r27;
        r0 = r0.schoolQRcodeInfoText;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = 8;
        r23.setVisibility(r24);	 Catch:{ Exception -> 0x011a }
        r0 = r27;
        r0 = r0.societyQRcodeRebateInfoLayout;	 Catch:{ Exception -> 0x011a }
        r23 = r0;
        r24 = 8;
        r23.setVisibility(r24);	 Catch:{ Exception -> 0x011a }
        goto L_0x00b3;
    L_0x0336:
        r23 = "PAPER";
        r0 = r23;
        r23 = r0.equalsIgnoreCase(r5);
        if (r23 == 0) goto L_0x00da;
    L_0x0340:
        r23 = 2131492919; // 0x7f0c0037 float:1.8609303E38 double:1.0530974256E-314;
        r0 = r27;
        r1 = r23;
        r16 = r0.findViewById(r1);
        r23 = 2131492937; // 0x7f0c0049 float:1.860934E38 double:1.0530974345E-314;
        r0 = r27;
        r1 = r23;
        r17 = r0.findViewById(r1);
        r23 = 8;
        r0 = r16;
        r1 = r23;
        r0.setVisibility(r1);
        r23 = 0;
        r0 = r17;
        r1 = r23;
        r0.setVisibility(r1);
        r27.showPutPaperInfo();
        goto L_0x00da;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.incomrecycle.prms.rvm.gui.activity.starput.QRCodeResultActivity.onStart():void");
    }

    public void onStop() {
        super.onStop();
        if (this.soundPool != null) {
            try {
                this.soundPool.release();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            this.soundPool = null;
        }
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        try {
            HashMap<String, Object> hsmpParam = new HashMap();
            hsmpParam.put("TEXT", getString(R.string.welcome));
            guiCommonService.execute("GUIRecycleCommonService", "showOnDigitalScreen", hsmpParam);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        findViewById(R.id.QRcode_rebate_layout).setBackgroundDrawable(null);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void finish() {
        super.finish();
    }

    public void initView() {
        this.rebateThis = (TextView) findViewById(R.id.QRcode_rebate_this);
        this.balanceOfINCOM = (TextView) findViewById(R.id.QRcode_INCOM_balance);
        this.recharge_warningtext = (TextView) findViewById(R.id.QRcode_rebate_remind_text);
        this.schoolQRcodeInfoText = (TextView) findViewById(R.id.school_QRcode_info_text);
        this.societyQRcodeRebateInfoLayout = findViewById(R.id.society_QRcode_info);
        this.schoolQRcodeInfoLayout = findViewById(R.id.school_QRcode_info);
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    private void showPutBottleInfo() {
        final CommonServiceHelper.GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        final DecimalFormat decimalFormat = new DecimalFormat("0.00");
        final int n = 0;
        final int n2 = 0;
        double n3 = 0.0;
        final ArrayList<HashMap<String, double[]>> list = new ArrayList<HashMap<String, double[]>>();
        final HashMap<String, double[]> hashMap = new HashMap<String, double[]>();
        hashMap.put("RANGE", new double[] { -1.0, 450.0 });
        hashMap.put("TITLE", (double[])(Object)"0-450");
        hashMap.put("COUNT", (double[])(Object)"0");
        hashMap.put("VENDING_COUNT", (double[])(Object)"0");
        hashMap.put("AMOUNT", (double[])(Object)"0");
        list.add(hashMap);
        final HashMap<String, double[]> hashMap2 = new HashMap<String, double[]>();
        hashMap2.put("RANGE", new double[] { 450.0, 1200.0 });
        hashMap2.put("TITLE", (double[])(Object)"450-1200");
        hashMap2.put("COUNT", (double[])(Object)"0");
        hashMap2.put("VENDING_COUNT", (double[])(Object)"0");
        hashMap2.put("AMOUNT", (double[])(Object)"0");
        list.add(hashMap2);
        final HashMap<String, double[]> hashMap3 = new HashMap<String, double[]>();
        hashMap3.put("RANGE", new double[] { 1200.0, -1.0 });
        hashMap3.put("TITLE", (double[])(Object)"1200");
        hashMap3.put("COUNT", (double[])(Object)"0");
        hashMap3.put("VENDING_COUNT", (double[])(Object)"0");
        hashMap3.put("AMOUNT", (double[])(Object)"0");
        list.add(hashMap3);
        try {
            final HashMap<String, Object> execute = guiCommonService.execute("GUIQueryCommonService", "recycledBottleSummary", null);
            if (execute != null) {
                final List<HashMap> list2 = (List<HashMap>) execute.get("RECYCLED_BOTTLE_SUMMARY");
                if (list2 != null && list2.size() > 0) {
                    for (int i = 0; i < list2.size(); ++i) {
                        final HashMap<String, String> hashMap4 = list2.get(i);
                        final int int1 = Integer.parseInt(hashMap4.get("BOTTLE_COUNT"));
                        int int2;
                        if ((int2 = Integer.parseInt(hashMap4.get("VENDING_BOTTLE_COUNT"))) < 0) {
                            int2 = 0;
                        }
                        final double double1 = Double.parseDouble(hashMap4.get("BOTTLE_VOL"));
                        final double double2 = Double.parseDouble(hashMap4.get("BOTTLE_AMOUNT"));
                        for (int j = 0; j < list.size(); ++j) {
                            final HashMap<String, double[]> hashMap5 = list.get(j);
                            final double[] array = hashMap5.get("RANGE");
                            if (array[0] < double1 && (array[1] == -1.0 || array[1] >= double1)) {
                                final int int3 = Integer.parseInt((String)(Object)hashMap5.get("COUNT"));
                                final int int4 = Integer.parseInt((String)(Object)hashMap5.get("VENDING_COUNT"));
                                final double double3 = Double.parseDouble((String)(Object)hashMap5.get("AMOUNT"));
                                final double n4 = int2;
                                hashMap5.put("COUNT", (double[])(Object)("" + (int3 + int1)));
                                hashMap5.put("VENDING_COUNT", (double[])(Object)("" + (int4 + int2)));
                                hashMap5.put("AMOUNT", (double[])(Object)Double.toString(double3 + n4 * double2));
                                break;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        int k = 0;
        int n5 = n;
        int n6 = n2;
        while (k < list.size()) {
            final HashMap<String, double[]> hashMap6 = list.get(k);
            final int int5 = Integer.parseInt((String)(Object)hashMap6.get("COUNT"));
            final int int6 = Integer.parseInt((String)(Object)hashMap6.get("VENDING_COUNT"));
            final int n7 = int5 - int6;
            final double n8 = Double.parseDouble((String)(Object)hashMap6.get("AMOUNT")) / 100.0 * Integer.parseInt(SysConfig.get("LOCAL_EXCHANGE_RATE"));
            int n9 = n6;
            int n10 = n5;
            double n11 = n3;
            if (k == 0) {
                ((TextView)this.findViewById(R.id.onetotal_number)).setText((CharSequence)("" + int6));
                n10 = n5 + int6;
                ((TextView)this.findViewById(R.id.onetotaldonation_number)).setText((CharSequence)("" + n7));
                n9 = n6 + n7;
                ((TextView)this.findViewById(R.id.onetotal_acount)).setText((CharSequence)("" + decimalFormat.format(n8)));
                n11 = n3 + n8;
            }
            int n12 = n9;
            int n13 = n10;
            double n14 = n11;
            if (k == 1) {
                ((TextView)this.findViewById(R.id.twototal_number)).setText((CharSequence)("" + int6));
                n13 = n10 + int6;
                ((TextView)this.findViewById(R.id.twototaldonation_number)).setText((CharSequence)("" + n7));
                n12 = n9 + n7;
                ((TextView)this.findViewById(R.id.twototal_amount)).setText((CharSequence)("" + decimalFormat.format(n8)));
                n14 = n11 + n8;
            }
            int n15 = n12;
            int n16 = n13;
            double n17 = n14;
            if (k == 2) {
                ((TextView)this.findViewById(R.id.threetotal_number)).setText((CharSequence)("" + int6));
                n16 = n13 + int6;
                ((TextView)this.findViewById(R.id.threetotaldonation_number)).setText((CharSequence)("" + n7));
                n15 = n12 + n7;
                ((TextView)this.findViewById(R.id.threetotal_amount)).setText((CharSequence)("" + decimalFormat.format(n8)));
                n17 = n14 + n8;
            }
            ++k;
            n6 = n15;
            n5 = n16;
            n3 = n17;
        }
        ((TextView)this.findViewById(R.id.total_number)).setText((CharSequence)("" + n5));
        ((TextView)this.findViewById(R.id.totaldonation_number)).setText((CharSequence)("" + n6));
        ((TextView)this.findViewById(R.id.total_amount)).setText((CharSequence)("" + decimalFormat.format(n3)));
    }

    private void showPutPaperInfo() {
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        DecimalFormat df = new DecimalFormat("0.00");
        HashMap<String, Object> HsmpShowInfo = new HashMap();
        HsmpShowInfo.put("TITLE", "paper");
        HsmpShowInfo.put("COUNT", "0");
        HsmpShowInfo.put("AMOUNT", "0");
        try {
            HashMap<String, Object> hsmpResult = guiCommonService.execute("GUIQueryCommonService", "recycledPaperSummary", null);
            if (hsmpResult != null) {
                HashMap<String, String> HsmpRecyclePaper = (HashMap) hsmpResult.get("RECYCLED_PAPER_SUMMARY");
                if (HsmpRecyclePaper != null && HsmpRecyclePaper.size() > 0) {
                    double paperCount = Double.parseDouble((String) HsmpRecyclePaper.get("PAPER_WEIGH"));
                    double amount = Double.parseDouble((String) HsmpShowInfo.get("AMOUNT")) + Double.parseDouble((String) HsmpRecyclePaper.get("PAPER_PRICE"));
                    HsmpShowInfo.put("COUNT", Double.toString(Double.parseDouble((String) HsmpShowInfo.get("COUNT")) + paperCount));
                    HsmpShowInfo.put("AMOUNT", Double.toString(amount));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        double Amount = (Double.parseDouble((String) HsmpShowInfo.get("AMOUNT")) / 100.0d) * ((double) Integer.parseInt(SysConfig.get("LOCAL_EXCHANGE_RATE")));
        ((TextView) findViewById(R.id.paper_count)).setText("" + df.format(Double.parseDouble((String) HsmpShowInfo.get("COUNT"))));
        ((TextView) findViewById(R.id.paper_total_acount)).setText("" + df.format(Amount));
    }

    private void showRemindInfo() {
        if (!"FALSE".equalsIgnoreCase(SysConfig.get("BOTTLES_LIMITED_ENABLE"))) {
            int todayBottleCount;
            String remind;
            Integer DAILY_BOTTLE_COUNT = (Integer) ServiceGlobal.getCurrentSession("DAILY_BOTTLE_COUNT");
            Integer THIS_BOTTLE_COUNT = (Integer) ServiceGlobal.getCurrentSession("THIS_BOTTLE_COUNT");
            Integer DONATION_BOTTLE_COUNT = (Integer) ServiceGlobal.getCurrentSession("DONATION_BOTTLE_COUNT");
            String BOTTLES_DAILY_WARNING_SET = SysConfig.get("BOTTLES.DAILY.WARNING");
            String BOTTLES_DAILY_ALARM_SET = SysConfig.get("BOTTLES.DAILY.ALARM");
            if (DAILY_BOTTLE_COUNT != null) {
                todayBottleCount = DAILY_BOTTLE_COUNT.intValue();
            }
            if (!StringUtils.isBlank(BOTTLES_DAILY_WARNING_SET)) {
                int warnNumSet = Integer.parseInt(BOTTLES_DAILY_WARNING_SET);
            }
            if (!StringUtils.isBlank(BOTTLES_DAILY_ALARM_SET)) {
                int alarmNumSet = Integer.parseInt(BOTTLES_DAILY_ALARM_SET);
            }
            todayBottleCount = this.TODAY_BOTTLE;
            LayoutInflater factory = LayoutInflater.from(this);
            final Dialog dialog = new Dialog(this, R.style.Custom_dialog);
            View contentView = factory.inflate(R.layout.dialog_cmcc_phone_reminder, null);
            dialog.setContentView(contentView);
            backgroundToActivity();
            TextView showTextView = (TextView) contentView.findViewById(R.id.reminderCmcc);
            if (this.TODAY_BOTTLE == 0) {
                remind = StringUtils.replace(getString(R.string.put_bottle_max_warn_notnet), "$MAX_REBATE_BOTTLES_NUM$", BOTTLES_DAILY_ALARM_SET);
            } else {
                remind = StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(getString(R.string.put_bottle_max_warn), "$MAX_REBATE_BOTTLES_NUM$", BOTTLES_DAILY_ALARM_SET), "$TODAY_PUT_BOTTLE_NUM$", todayBottleCount + ""), "$PUT_BOTTLE_NUM$", THIS_BOTTLE_COUNT + ""), "$PUT_BOTTLE_MONEY$", this.totalMoney);
            }
            showTextView.setText(remind);
            dialog.setCancelable(false);
            dialog.show();
            LayoutParams params0 = dialog.getWindow().getAttributes();
            params0.x = 10;
            params0.y = 11;
            params0.width = FTPCodes.FILE_NOT_FOUND;
            params0.height = 360;
            dialog.getWindow().setAttributes(params0);
            ((Button) contentView.findViewById(R.id.btnKnown)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
    }
}
