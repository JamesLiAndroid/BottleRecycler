package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.content.Intent;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class PrintingVoucherActivity extends BaseActivity implements OnLoadCompleteListener {
    private String JSON;
    private String VOUCHER_NAME;
    private boolean isPlaySounds;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        Intent intent = new Intent();
                        if (!StringUtils.isBlank(PrintingVoucherActivity.this.JSON) && "BOTTLE".equalsIgnoreCase(ServiceGlobal.getCurrentSession("PRODUCT_TYPE").toString())) {
                            intent.setClass(PrintingVoucherActivity.this, EnvironmentalPromotionalActivity.class);
                            intent.putExtra("JSON", PrintingVoucherActivity.this.JSON);
                        } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                            try {
                                intent.setClass(PrintingVoucherActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        PrintingVoucherActivity.this.startActivity(intent);
                        PrintingVoucherActivity.this.finish();
                        return;
                    }
                    if (remainedSeconds == 3 && PrintingVoucherActivity.this.isPlaySounds && PrintingVoucherActivity.this.soundPool != null) {
                        PrintingVoucherActivity.this.soundPool.load(PrintingVoucherActivity.this, R.raw.dayinyouhuiquanwancheng, 0);
                    }
                    ((TextView) PrintingVoucherActivity.this.findViewById(R.id.dayin_time)).setText("" + remainedSeconds);
                }
            };
            PrintingVoucherActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onStop() {
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
        findViewById(R.id.dayin_layout).setBackgroundDrawable(null);
    }

    protected void onStart() {
        super.onStart();
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
            this.soundPool.setOnLoadCompleteListener(this);
        }
        this.VOUCHER_NAME = (String) GUIGlobal.getCurrentSession("VOUCHER_NAME");
        ((TextView) findViewById(R.id.voucher_name)).setText(this.VOUCHER_NAME);
        SysGlobal.execute(new Runnable() {
            public void run() {
                String ACTIVITY_ID = (String) GUIGlobal.getCurrentSession("ACTIVITY_ID");
                String PRINT_RULE = (String) GUIGlobal.getCurrentSession("PRINT_RULE");
                HashMap<String, Object> hsmpParam = new HashMap();
                hsmpParam.put("ACTIVITY_ID", ACTIVITY_ID);
                hsmpParam.put("PRINT_RULE", PRINT_RULE);
                hsmpParam.put("LOCALE", GUIGlobal.getLocale(PrintingVoucherActivity.this.getApplication()).toString());
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIVoucherCommonService", "printVoucher", hsmpParam);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        String PRODUCT_TYPE = (String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE");
        if ("BOTTLE".equalsIgnoreCase(PRODUCT_TYPE)) {
            showPutBottleInfo();
        } else if ("PAPER".equalsIgnoreCase(PRODUCT_TYPE)) {
            View putBottleCount = findViewById(R.id.BottleCount);
            View putPaperCount = findViewById(R.id.PaperCount);
            putBottleCount.setVisibility(View.GONE);
            putPaperCount.setVisibility(View.VISIBLE);
            showPutPaperInfo();
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.PRINTING")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onResume() {
        super.onResume();
        if (this.isPlaySounds && this.soundPool != null) {
            this.soundPool.load(this, R.raw.dayinyouhuiquan, 0);
        }
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_printing_voucher);
        backgroundToActivity();
        findViewById(R.id.dayin_end).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                if (!StringUtils.isBlank(PrintingVoucherActivity.this.JSON) && "BOTTLE".equalsIgnoreCase(ServiceGlobal.getCurrentSession("PRODUCT_TYPE").toString())) {
                    intent.setClass(PrintingVoucherActivity.this, EnvironmentalPromotionalActivity.class);
                    intent.putExtra("JSON", PrintingVoucherActivity.this.JSON);
                } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        intent.setClass(PrintingVoucherActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                PrintingVoucherActivity.this.startActivity(intent);
                PrintingVoucherActivity.this.finish();
            }
        });
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
        if ("INFORM".equalsIgnoreCase((String) hsmpEvent.get("EVENT"))) {
            if ("BOTTLE_NUM".equalsIgnoreCase((String) hsmpEvent.get("INFORM"))) {
                this.JSON = JSONUtils.toJSON(hsmpEvent);
            }
        }
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

    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        soundPlay(sampleId);
    }

    private void soundPlay(int soundId) {
        if (this.soundPool != null) {
            this.soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
}
