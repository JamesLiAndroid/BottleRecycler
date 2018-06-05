package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.app.Dialog;
import android.content.Intent;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
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

public class CommitRebateInfoFailActivity extends BaseActivity {
    private int TODAY_BOTTLE = 0;
    private int chongZhiShiBai;
    private boolean isPlaySounds;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) CommitRebateInfoFailActivity.this.findViewById(R.id.onecard_rebate_commit_fail_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(CommitRebateInfoFailActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            CommitRebateInfoFailActivity.this.startActivity(intent);
                            CommitRebateInfoFailActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            CommitRebateInfoFailActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private String totalMoney;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_commit_rebateinfo_fail);
        backgroundToActivity();
        ((Button) findViewById(R.id.onecard_rebate_commit_fail_end)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(CommitRebateInfoFailActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        CommitRebateInfoFailActivity.this.startActivity(intent);
                        CommitRebateInfoFailActivity.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    protected void onStart() {
        super.onStart();
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
        }
        String PRODUCT_TYPE = (String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE");
        if ("BOTTLE".equalsIgnoreCase(PRODUCT_TYPE)) {
            showPutBottleInfo();
        } else if ("PAPER".equalsIgnoreCase(PRODUCT_TYPE)) {
            View putBottleCount = findViewById(R.id.BottleCount);
            View putPaperCount = findViewById(R.id.PaperCount);
            putBottleCount.setVisibility(8);
            putPaperCount.setVisibility(0);
            showPutPaperInfo();
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onResume() {
        super.onResume();
        if (this.isPlaySounds && this.soundPool != null) {
            this.chongZhiShiBai = this.soundPool.load(this, R.raw.chongzhishibai, 0);
            this.soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    if (sampleId == CommitRebateInfoFailActivity.this.chongZhiShiBai) {
                        soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
            });
        }
    }

    public void onStop() {
        super.onStop();
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        try {
            HashMap<String, Object> hsmpParam = new HashMap();
            hsmpParam.put("TEXT", getString(R.string.welcome));
            guiCommonService.execute("GUIRecycleCommonService", "showOnDigitalScreen", hsmpParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        findViewById(R.id.onecard_rebate_commit_fail_layout).setBackgroundDrawable(null);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void finish() {
        super.finish();
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
