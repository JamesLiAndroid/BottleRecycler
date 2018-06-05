package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
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
import com.incomrecycle.prms.rvm.gui.GUIAction;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class EnvironmentalPromotionalActivity extends BaseActivity {
    private ImageView iv_environment_erweima;
    private Button thank_page_end;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        EnvironmentalPromotionalActivity.this.stopPhase();
                    } else {
                        ((TextView) EnvironmentalPromotionalActivity.this.findViewById(R.id.thank_page_time)).setText("" + remainedSeconds);
                    }
                }
            };
            EnvironmentalPromotionalActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private TextView tv_environ_show;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environmental_promotional);
        backgroundToActivity();
        this.thank_page_end = (Button) findViewById(R.id.thank_page_end);
        this.tv_environ_show = (TextView) findViewById(R.id.tv_environ_show);
        this.iv_environment_erweima = (ImageView) findViewById(R.id.iv_environment_erweima);
        this.tv_environ_show.setText(R.string.EnvironmentalPromotionalActivity_more_lv);
        this.iv_environment_erweima.setBackgroundResource(R.drawable.wechatqrcard_lv);
        this.thank_page_end.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.THANK_REBATE_END);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                EnvironmentalPromotionalActivity.this.stopPhase();
            }
        });
    }

    private void stopPhase() {
        if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
            try {
                Intent intent = new Intent(getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void onStart() {
        super.onStart();
        try {
            String bottleNum = (String) JSONUtils.toHashMap(getIntent().getStringExtra("JSON")).get("BOTTLE_NUM");
            if (!StringUtils.isBlank(bottleNum)) {
                int num = Integer.valueOf(bottleNum).intValue();
                TextView bottleNumText = (TextView) findViewById(R.id.all_bottles);
                bottleNumText.setText(Html.fromHtml(StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(getString(R.string.all_bottles), "$BOTTLE_NUM$", "<big><font color=\"#ff0000\">" + bottleNum + "</font></big>"), "$BOTTLE_SHI$", "<big><font color=\"#ff0000\">" + ((num / 40000) * 6) + "</font></big>"), "$BOTTLE_TREE$", "<big><font color=\"#ff0000\">" + ((num / 40000) * 41) + "</font></big>"), "$BOTTLE_C$", "<big><font color=\"#ff0000\">" + ((num / 40000) * 3) + "</font></big>")));
                bottleNumText.setVisibility(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.THANKS")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onStop() {
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
        super.onStop();
    }

    public void finish() {
        super.finish();
    }

    public void updateLanguage() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @Override
    public void doEvent(HashMap<String,String> hashMap) {
        final String s = (String) hashMap.get("EVENT");
        if (!"CMD".equalsIgnoreCase(s) || "REQUEST_RECYCLE".equalsIgnoreCase((String) hashMap.get("CMD"))) {}
        if ("INFORM".equalsIgnoreCase(s) && "BOTTLE_NUM".equalsIgnoreCase(hashMap.get("INFORM"))) {
            final String s2 = hashMap.get("BOTTLE_NUM");
            if (!StringUtils.isBlank(s2)) {
                final int intValue = Integer.valueOf(s2);
                final TextView textView = (TextView)this.findViewById(R.id.all_bottles);
                textView.setText((CharSequence)Html.fromHtml(StringUtils.replace(StringUtils.replace(
                        StringUtils.replace(StringUtils.replace(this.getString(2131296669),
                                "$BOTTLE_NUM$", "&nbsp<big><font color=\"#ff0000\">" + s2 + "</font>&nbsp</big>"), "$BOTTLE_SHI$", "&nbsp<big><font color=\"#ff0000\">" +
                                intValue / 40000 * 6 + "</font>&nbsp</big>"), "$BOTTLE_TREE$", "&nbsp<big><font color=\"#ff0000\">" + intValue / 40000 * 41 + "</font>&nbsp</big>"),
                        "$BOTTLE_C$", "&nbsp<big><font color=\"#ff0000\">" + intValue / 40000 * 3 + "</font>&nbsp</big>")));
                textView.setVisibility(View.VISIBLE);
            }
        }
    }
}
