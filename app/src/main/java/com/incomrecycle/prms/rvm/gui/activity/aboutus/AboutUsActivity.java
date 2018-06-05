package com.incomrecycle.prms.rvm.gui.activity.aboutus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AboutUs;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

@SuppressLint({"SetJavaScriptEnabled", "ResourceAsColor"})
public class AboutUsActivity extends BaseActivity implements OnClickListener {
    private static int position = -1;
    private LinearLayout aboutus_trends_buttons;
    private String[] buttonNames;
    boolean hasIndexHtml = false;
    private int[] imgs = new int[]{R.drawable.lianxiwomen, R.drawable.zhuyishixiang, R.drawable.changjianwenti, R.drawable.erjiyemian};
    private String indexUrl = null;
    private boolean isClick = false;
    private RelativeLayout[] linearLayouts;
    private List<HashMap<Integer, String>> listMap = new ArrayList();
    private Button returnBtn;
    private String selected = "";
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) AboutUsActivity.this.findViewById(R.id.aboutusTimeText)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(AboutUsActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            AboutUsActivity.this.startActivity(intent);
                            AboutUsActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            AboutUsActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_about_us);
        backgroundToActivity();
        this.returnBtn = (Button) findViewById(R.id.aboutus_return_btn);
        this.aboutus_trends_buttons = (LinearLayout) findViewById(R.id.aboutus_trends_button);
        initView();
        setClickListener();
    }

    protected void onStart() {
        super.onStart();
        if (this.hasIndexHtml) {
            TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
            TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
            TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
        } else {
            TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
            TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
            TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
        }
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    private void initView() {
        this.linearLayouts = new RelativeLayout[4];
        String URL_CONFIG = SysConfig.get("RVM.URL." + SysConfig.get("RVM.LANGUAGE"));
        if (!new File(URL_CONFIG).isFile() || StringUtils.isBlank(URL_CONFIG)) {
            URL_CONFIG = SysConfig.get("RVM.URL.CONFIG");
        }
        File newurlcfg = new File(URL_CONFIG);
        if (newurlcfg.isFile()) {
            Properties urlProperties = PropUtils.loadFile(newurlcfg);
            String buttonName = urlProperties.getProperty("BUTTON");
            try {
                this.indexUrl = urlProperties.getProperty("INDEX.URL");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (!StringUtils.isBlank(this.indexUrl) && new File(this.indexUrl).isFile()) {
                    this.hasIndexHtml = true;
                }
                if (!StringUtils.isBlank(buttonName)) {
                    this.buttonNames = buttonName.split(";");
                    for (int i = 0; i < this.buttonNames.length; i++) {
                        this.linearLayouts[i] = (RelativeLayout) this.aboutus_trends_buttons.getChildAt(i);
                        this.linearLayouts[i].setId(i + AboutUs.ABOUT_US_BEGIN_ID);
                        this.linearLayouts[i].setBackgroundColor(Color.parseColor("#0834A0"));
                        ImageView imageView = new ImageView(this);
                        LayoutParams layoutParams_imageView = new LayoutParams(-2, -2);
                        layoutParams_imageView.addRule(14, -1);
                        layoutParams_imageView.addRule(10, -1);
                        layoutParams_imageView.setMargins(0, 59, 0, 0);
                        imageView.setLayoutParams(layoutParams_imageView);
                        imageView.setImageResource(this.imgs[i]);
                        LayoutParams layoutParams_textView = new LayoutParams(-2, -2);
                        layoutParams_textView.addRule(14, -1);
                        layoutParams_textView.addRule(12, -1);
                        layoutParams_textView.setMargins(0, 0, 0, 42);
                        TextView textView = new TextView(this);
                        textView.setId(65537 + i);
                        textView.setLayoutParams(layoutParams_textView);
                        String buttonText = urlProperties.getProperty("HTML." + this.buttonNames[i] + ".TITLE");
                        try {
                            buttonText = new String(buttonText.getBytes("ISO-8859-1"), "GBK");
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                        textView.setText(buttonText);
                        textView.setTextColor(getResources().getColor(R.color.button_textColor));
                        this.linearLayouts[i].addView(imageView);
                        this.linearLayouts[i].addView(textView);
                        this.linearLayouts[i].setEnabled(true);
                        this.linearLayouts[i].setOnClickListener(this);
                        HashMap<Integer, String> urlMap = new HashMap();
                        urlMap.put(Integer.valueOf(AboutUs.ABOUT_US_BEGIN_ID), urlProperties.getProperty("HTML." + this.buttonNames[i] + ".URL"));
                        urlMap.put(Integer.valueOf(AboutUs.ABOUT_US_SAVE_ID_KEY), (i + AboutUs.ABOUT_US_BEGIN_ID) + "");
                        urlMap.put(Integer.valueOf(100), buttonText);
                        this.listMap.add(urlMap);
                    }
                }
            } catch (Exception e22) {
                e22.printStackTrace();
            }
        }
    }

    private void setClickListener() {
        this.returnBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!AboutUsActivity.this.isClick) {
                    AboutUsActivity.this.isClick = true;
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            HashMap map = new HashMap();
                            map.put("KEY", AllClickContent.ABOUTUS_RETURN);
                            try {
                                CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                            } catch (Exception e) {
                            }
                            if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                                try {
                                    Intent intent = new Intent(AboutUsActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    AboutUsActivity.this.startActivity(intent);
                                    AboutUsActivity.this.finish();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }, 350);
                }
            }
        });
    }

    public void onClickItem() {
        HashMap map;
        if (position == 0) {
            map = new HashMap();
            map.put("KEY", AllClickContent.ABOUTUS_CONNECTUS);
            try {
                CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
            } catch (Exception e) {
            }
        } else if (position == 1) {
            map = new HashMap();
            map.put("KEY", AllClickContent.ABOUTUS_ATTENTION);
            try {
                CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
            } catch (Exception e2) {
            }
        } else if (position == 2) {
            map = new HashMap();
            map.put("KEY", AllClickContent.ABOUTUS_FAQ);
            try {
                CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
            } catch (Exception e3) {
            }
        } else if (position == 3) {
            map = new HashMap();
            map.put("KEY", AllClickContent.ABOUTUS_COOPERATION);
            try {
                CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
            } catch (Exception e4) {
            }
        }
        if (position != -1 && !"".equals(this.selected)) {
            Intent intent = new Intent(this, AboutUsSecondActivity.class);
            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("imgId", this.imgs[position]);
            intent.putExtra("position", position);
            intent.putExtra("buttonText", this.selected);
            intent.putExtra("url", ((String) ((HashMap) this.listMap.get(position)).get(Integer.valueOf(AboutUs.ABOUT_US_BEGIN_ID))).toString());
            startActivity(intent);
            finish();
        }
    }

    public void onClick(View v) {
        for (int i = 0; i < this.buttonNames.length; i++) {
            if (this.linearLayouts[i].getId() == v.getId()) {
                v.setEnabled(false);
                this.linearLayouts[i].setBackgroundColor(Color.parseColor("#082050"));
                this.selected = ((TextView) v.findViewById(65537 + i)).getText().toString();
                position = i;
            } else {
                this.linearLayouts[i].setEnabled(true);
                this.linearLayouts[i].setBackgroundColor(Color.parseColor("#0834A0"));
            }
        }
        onClickItem();
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
    }
}
