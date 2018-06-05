package com.incomrecycle.prms.rvm.gui.activity.aboutus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

@SuppressLint({"SetJavaScriptEnabled", "ResourceAsColor"})
public class AboutUsSecondActivity extends BaseActivity {
    private static final String TAG = "AboutUsSecondActivity";
    private ImageView contentImage;
    private TextView contentTitle;
    private WebView myWebView;
    private Button returnBtn;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds == 0) {
                        Intent intent = new Intent(AboutUsSecondActivity.this, AboutUsActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        AboutUsSecondActivity.this.startActivity(intent);
                        AboutUsSecondActivity.this.finish();
                        return;
                    }
                    ((TextView) AboutUsSecondActivity.this.findViewById(R.id.aboutus_us_second_time)).setText("" + remainedSeconds);
                }
            };
            AboutUsSecondActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_about_us_second);
        backgroundToActivity();
        this.contentTitle = (TextView) findViewById(R.id.aboutus_us_second_content_title);
        this.contentImage = (ImageView) findViewById(R.id.aboutus_us_second_content_pic);
        this.returnBtn = (Button) findViewById(R.id.aboutus_us_second_return_btn);
        this.myWebView = (WebView) findViewById(R.id.aboutus_webview);
        setClickListener();
        String buttonText = getIntent().getStringExtra("buttonText");
        String url = getIntent().getStringExtra("url");
        int imgId = getIntent().getIntExtra("imgId", -1);
        this.contentTitle.setText(buttonText);
        this.contentImage.setImageResource(imgId);
        initWebView("file://" + url);
    }

    protected void onStart() {
        super.onStart();
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.TRANSPORTCARD")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onStop() {
        super.onStop();
        this.myWebView.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.myWebView != null) {
            this.myWebView.destroy();
            this.myWebView = null;
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

    private void initWebView(String url) {
        this.myWebView.getSettings().setJavaScriptEnabled(true);
        this.myWebView.getSettings().setLoadsImagesAutomatically(true);
        this.myWebView.getSettings().setSupportZoom(true);
        this.myWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        this.myWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }
        });
        this.myWebView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                TimeoutTask.getTimeoutTask().reset(AboutUsSecondActivity.this.timeoutAction);
                return false;
            }
        });
        this.myWebView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                TimeoutTask.getTimeoutTask().reset(AboutUsSecondActivity.this.timeoutAction);
                return false;
            }
        });
        this.myWebView.loadUrl(url);
    }

    private void setClickListener() {
        this.returnBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.ABOUTUSSECOND_RETURN);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Intent intent = new Intent();
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.setClass(AboutUsSecondActivity.this, AboutUsActivity.class);
                        AboutUsSecondActivity.this.startActivity(intent);
                        AboutUsSecondActivity.this.finish();
                    }
                }, 350);
            }
        });
    }

    public List<Map<String, Object>> stringToList(String string) {
        int i;
        List<Map<String, Object>> list = new ArrayList();
        String[] split = string.split(":");
        for (String println : split) {
            System.out.println(println);
        }
        for (i = 1; i < split.length; i++) {
            Map<String, Object> map = new HashMap();
            if (i % 2 != 0) {
                map.put("Questions", split[i].substring(0, split[i].lastIndexOf("?") + 1));
            } else {
                map.put("Answers", split[i].substring(0, split[i].lastIndexOf(".") + 1));
            }
            list.add(map);
        }
        return list;
    }

    public String readTextFile(String filePath) {
        Exception e;
        Throwable th;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader bReader = new BufferedReader(new FileReader(filePath));
            try {
                String line = "";
                StringBuilder sb = new StringBuilder();
                while (true) {
                    line = bReader.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                }
                String stringBuilder = sb.toString();
                try {
                    bReader.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                bufferedReader = bReader;
                return stringBuilder;
            } catch (Exception e3) {
                e = e3;
                bufferedReader = bReader;
                try {
                    e.printStackTrace();
                    try {
                        bufferedReader.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                    return null;
                } catch (Throwable th2) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                    throw th2;
                }
            } catch (Throwable th3) {
                bufferedReader = bReader;
                bufferedReader.close();
                throw th3;
            }
        } catch (Exception e4) {
            e = e4;
            e.printStackTrace();
            try {
                bufferedReader.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }
}
