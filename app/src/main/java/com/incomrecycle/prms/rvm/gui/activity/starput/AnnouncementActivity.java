package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class AnnouncementActivity extends BaseActivity {
    private int SBAR_ID;
    private String SBAR_TEXT;
    private AnnouncementAdapter announcementAdapter;
    private ListView announcement_remind_list;
    private boolean isClick = false;
    private List<String> list;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int announcementSeconds, int remainedAnnouncements) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedAnnouncements = ((Integer) paramObjs[1]).intValue();
                    if (remainedAnnouncements != 0) {
                        ((TextView) AnnouncementActivity.this.findViewById(R.id.announcement_alarm_list_time)).setText("" + remainedAnnouncements);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(AnnouncementActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            AnnouncementActivity.this.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            AnnouncementActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(announcementSeconds), Integer.valueOf(remainedAnnouncements)});
        }
    };

    public class AnnouncementAdapter extends BaseAdapter {
        public int getCount() {
            return AnnouncementActivity.this.list.size();
        }

        public Object getItem(int position) {
            return AnnouncementActivity.this.list.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View contentView, ViewGroup arg2) {
            if (contentView == null) {
                contentView = LayoutInflater.from(AnnouncementActivity.this).inflate(R.layout.activity_announcement_remind_list_item, null);
            }
            ((TextView) contentView.findViewById(R.id.announcement_text)).setText((CharSequence) AnnouncementActivity.this.list.get(position));
            return contentView;
        }
    }

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        this.list = new ArrayList();
        this.announcement_remind_list = (ListView) findViewById(R.id.announcement_remind_list);
        try {
            HashMap<String, Object> hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryScrollText", null);
            if (hsmpResult != null) {
                List<HashMap<String, String>> listTextAdInfo = new ArrayList();
                listTextAdInfo = (List) hsmpResult.get("RVM_TEXT_AD_LIST");
                int num = listTextAdInfo.size();
                if (listTextAdInfo != null && num > 0) {
                    for (int i = 0; i < num; i++) {
                        this.SBAR_TEXT = (String) ((HashMap) listTextAdInfo.get(i)).get("SBAR_TEXT");
                        this.SBAR_ID = i;
                        this.SBAR_TEXT = (this.SBAR_ID + 1) + ":  " + this.SBAR_TEXT;
                        this.list.add(this.SBAR_TEXT);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.announcementAdapter = new AnnouncementAdapter();
        this.announcement_remind_list.setAdapter(this.announcementAdapter);
        System.out.println("announcementAdapterqidong");
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.SELECTVOUCHER")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onStop() {
        super.onStop();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        finish();
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);
        backgroundToActivity();
        getWindow().getDecorView().setSystemUiVisibility(1);
        Button announcement_return_btn = (Button) findViewById(R.id.announcement_return_btn);
        ImageView announcement_content_pic = (ImageView) findViewById(R.id.announcement_content_pic);
        ListView announcement_remind_list = (ListView) findViewById(R.id.announcement_remind_list);
        ((TextView) findViewById(R.id.announcement_content_title)).setText(R.string.notice);
        announcement_content_pic.setBackgroundResource(R.drawable.gonggao);
        announcement_return_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!AnnouncementActivity.this.isClick) {
                    AnnouncementActivity.this.isClick = true;
                    AnnouncementActivity.this.finish();
                }
            }
        });
        announcement_remind_list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                TimeoutTask.getTimeoutTask().reset(AnnouncementActivity.this.timeoutAction);
            }
        });
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }
}
