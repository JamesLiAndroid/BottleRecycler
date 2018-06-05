package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.content.Intent;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class ThankBottlePageActivity extends BaseActivity {
    private String JSON = null;
    private String VENDING_WAY = null;
    private int bottleNumber;
    private boolean isPlaySounds;
    private int juanZengWanCheng;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        ThankBottlePageActivity.this.stopPhase();
                    } else {
                        ((TextView) ThankBottlePageActivity.this.findViewById(R.id.thank_page_time)).setText("" + remainedSeconds);
                    }
                }
            };
            ThankBottlePageActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_thank);
        backgroundToActivity();
        this.JSON = getIntent().getStringExtra("JSON");
        ((Button) findViewById(R.id.thank_page_end)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.THANK_REBATE_END);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                ThankBottlePageActivity.this.stopPhase();
            }
        });
    }

    // TOOD: 待修复
    public void onStart() {
        super.onStart();
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
        }
        final CommonServiceHelper.GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        final DecimalFormat decimalFormat = new DecimalFormat("0.00");
        this.bottleNumber = 0;
        final int n = 0;
        double n2 = 0.0;
        final ArrayList<HashMap<String, double[]>> list = new ArrayList<HashMap<String, double[]>>();
        final HashMap<String, double[]> hashMap = new HashMap<String, double[]>();
        hashMap.put("RANGE", new double[] { -1.0, 500.0 });
        hashMap.put("TITLE", (double[])(Object)"0-500");
        hashMap.put("COUNT", (double[])(Object)"0");
        hashMap.put("VENDING_COUNT", (double[])(Object)"0");
        hashMap.put("AMOUNT", (double[])(Object)"0");
        list.add(hashMap);
        final HashMap<String, double[]> hashMap2 = new HashMap<String, double[]>();
        hashMap2.put("RANGE", new double[] { 500.0, 1200.0 });
        hashMap2.put("TITLE", (double[])(Object)"500-1200");
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
            final HashMap<String, Object> execute = guiCommonService.execute("GUIRecycleCommonService", "queryVendingWay", null);
            if (execute != null) {
                this.VENDING_WAY = (String)execute.get("VENDING_WAY");
            }
            final HashMap<String, Object> execute2 = guiCommonService.execute("GUIQueryCommonService", "recycledBottleSummary", null);
            if (execute2 != null) {
                final List<HashMap> list2 = (List<HashMap>) execute2.get("RECYCLED_BOTTLE_SUMMARY");
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
                                final double n3 = int1;
                                hashMap5.put("COUNT", (double[])(Object)("" + (int3 + int1)));
                                hashMap5.put("VENDING_COUNT", (double[])(Object)("" + (int4 + int2)));
                                hashMap5.put("AMOUNT", (double[])(Object)Double.toString(double3 + n3 * double2));
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
        int n4 = n;
        while (k < list.size()) {
            final HashMap<String, double[]> hashMap6 = list.get(k);
            final int int5 = Integer.parseInt((String)(Object)hashMap6.get("COUNT"));
            Integer.parseInt((String)(Object)hashMap6.get("VENDING_COUNT"));
            double n5 = 0.0;
            if (!"DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
                n5 = Double.parseDouble((String)(Object)hashMap6.get("AMOUNT")) / 100.0 * Integer.parseInt(SysConfig.get("LOCAL_EXCHANGE_RATE"));
            }
            int n6 = n4;
            double n7 = n2;
            if (k == 0) {
                ((TextView)this.findViewById(R.id.onetotal_number)).setText((CharSequence)"0");
                this.bottleNumber += 0;
                ((TextView)this.findViewById(R.id.onetotaldonation_number)).setText((CharSequence)("" + int5));
                n6 = n4 + int5;
                ((TextView)this.findViewById(R.id.onetotal_acount)).setText((CharSequence)("" + decimalFormat.format(n5)));
                n7 = n2 + n5;
            }
            int n8 = n6;
            double n9 = n7;
            if (k == 1) {
                ((TextView)this.findViewById(R.id.twototal_number)).setText((CharSequence)"0");
                this.bottleNumber += 0;
                ((TextView)this.findViewById(R.id.twototaldonation_number)).setText((CharSequence)("" + int5));
                n8 = n6 + int5;
                ((TextView)this.findViewById(R.id.twototal_amount)).setText((CharSequence)("" + decimalFormat.format(n5)));
                n9 = n7 + n5;
            }
            int n10 = n8;
            double n11 = n9;
            if (k == 2) {
                ((TextView)this.findViewById(R.id.threetotal_number)).setText((CharSequence)"0");
                this.bottleNumber += 0;
                ((TextView)this.findViewById(R.id.threetotaldonation_number)).setText((CharSequence)("" + int5));
                n10 = n8 + int5;
                ((TextView)this.findViewById(R.id.threetotal_amount)).setText((CharSequence)("" + decimalFormat.format(n5)));
                n11 = n9 + n5;
            }
            ++k;
            n4 = n10;
            n2 = n11;
        }
        ((TextView)this.findViewById(R.id.total_number)).setText((CharSequence)("" + this.bottleNumber));
        ((TextView)this.findViewById(R.id.totaldonation_number)).setText((CharSequence)("" + n4));
        ((TextView)this.findViewById(R.id.total_amount)).setText((CharSequence)("" + decimalFormat.format(n2)));
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.THANKS")), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onResume() {
        super.onResume();
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        if (this.isPlaySounds && "DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
            this.juanZengWanCheng = this.soundPool.load(this, R.raw.juanzengwancheng, 0);
            this.soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    if (sampleId == ThankBottlePageActivity.this.juanZengWanCheng) {
                        soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
            });
        }
    }

    public void onStop() {
        super.onStop();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
        if (this.soundPool != null) {
            try {
                this.soundPool.release();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            this.soundPool = null;
        }
        findViewById(R.id.thank_page_layout).setBackgroundDrawable(null);
    }

    public void finish() {
        super.finish();
    }

    public void updateLanguage() {
    }

    private void stopPhase() {
        Intent intent = new Intent();
        if (!StringUtils.isBlank(this.JSON)) {
            intent.setClass(this, EnvironmentalPromotionalActivity.class);
            intent.putExtra("JSON", this.JSON);
        } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
            try {
                intent.setClass(getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    public void doEvent(HashMap hsmpEvent) {
        if ("INFORM".equalsIgnoreCase((String) hsmpEvent.get("EVENT"))) {
            if ("BOTTLE_NUM".equalsIgnoreCase((String) hsmpEvent.get("INFORM")) && "DONATION".equalsIgnoreCase(this.VENDING_WAY)) {
                this.JSON = JSONUtils.toJSON(hsmpEvent);
            }
        }
    }
}
