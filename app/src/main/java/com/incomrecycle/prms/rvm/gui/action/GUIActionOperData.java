package com.incomrecycle.prms.rvm.gui.action;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.VendingWay;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelAdvancedActivity;
import com.incomrecycle.prms.rvm.gui.activity.channel.PullDownView;
import com.incomrecycle.prms.rvm.gui.activity.channel.PullDownView.OnPullDownListener;
import com.incomrecycle.prms.rvm.util.ActivitySimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GUIActionOperData extends GUIAction implements OnPullDownListener, OnItemClickListener {
    ChannelAdvancedActivity activity;
    private SimpleAdapter adapter;
    private List<HashMap<String, String>> columnLMap = new ArrayList();
    private ListView listView;
    private Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    GUIActionOperData.this.columnLMap.clear();
                    GUIActionOperData.this.columnLMap.addAll(GUIActionOperData.this.getResultListPage(1));
                    GUIActionOperData.this.adapter.notifyDataSetChanged();
                    GUIActionOperData.this.pullDownView.notifyDidLoad();
                    return;
                case 1:
                    GUIActionOperData.this.adapter.notifyDataSetChanged();
                    GUIActionOperData.this.pullDownView.notifyDidRefresh();
                    return;
                case 2:
                    GUIActionOperData.this.pageNo = GUIActionOperData.this.pageNo + 1;
                    GUIActionOperData.this.columnLMap.addAll(GUIActionOperData.this.getResultListPage(GUIActionOperData.this.pageNo));
                    GUIActionOperData.this.adapter.notifyDataSetChanged();
                    GUIActionOperData.this.pullDownView.notifyDidMore();
                    return;
                default:
                    return;
            }
        }
    };
    private int pageNo = 1;
    private PullDownView pullDownView;

    protected void doAction(Object[] paramObjs) {
        this.activity = (ChannelAdvancedActivity) paramObjs[0];
        LinearLayout lin = (LinearLayout) this.activity.findViewById(R.id.channel_advance_linnearlayout);
        LinearLayout layout1 = (LinearLayout) LayoutInflater.from(this.activity).inflate(R.layout.activity_oper_data, null);
        this.pullDownView = (PullDownView) layout1.findViewById(R.id.pull_down_operdata_list_view);
        this.pullDownView.setOnPullDownListener(this);
        this.listView = this.pullDownView.getListView();
        this.listView.setOnItemClickListener(this);
        this.listView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                TimeoutTask.getTimeoutTask().reset(GUIActionOperData.this.activity.getTimeoutAction());
                return false;
            }
        });
        lin.removeAllViews();
        lin.addView(layout1);
        this.adapter = new ActivitySimpleAdapter(this.activity, this.columnLMap, R.layout.oper_data_listviewlayout,
                new String[]{"OPT_TIME", AllAdvertisement.VENDING_WAY, "PRODUCT_TYPE", "PRODUCT_AMOUNT"},
                new int[]{R.id.operDataDateTime, R.id.operDataInfo, R.id.operDataType, R.id.operDataStaff});
        this.listView.setAdapter(this.adapter);
        this.pullDownView.enableAutoFetchMore(true, 1);
        loadData();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
    }

    private void loadData() {
        SysGlobal.execute(new Runnable() {
            public void run() {
                GUIActionOperData.this.mUIHandler.obtainMessage(0).sendToTarget();
            }
        });
    }

    public void onRefresh() {
        SysGlobal.execute(new Runnable() {
            public void run() {
                Message msg = GUIActionOperData.this.mUIHandler.obtainMessage(1);
                msg.obj = "After refresh " + System.currentTimeMillis();
                msg.sendToTarget();
            }
        });
    }

    public void onMore() {
        SysGlobal.execute(new Runnable() {
            public void run() {
                Message msg = GUIActionOperData.this.mUIHandler.obtainMessage(2);
                msg.obj = "After more " + System.currentTimeMillis();
                msg.sendToTarget();
            }
        });
    }

    private List<HashMap<String, String>> getResultListPage(int pageNo) {
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        HashMap<String, Object> hsmpResult = null;
        List<HashMap<String, String>> newList = new ArrayList();
        HashMap<String, Object> hsmpParam = new HashMap();
        hsmpParam.put("PAGE_ROWS", SysConfig.get("RVM.PAGE.ROWS"));
        hsmpParam.put("PAGE_NO", String.valueOf(pageNo));
        try {
            hsmpResult = guiCommonService.execute("GUIMaintenanceCommonService", "getOptList", hsmpParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (hsmpResult != null) {
            List<HashMap<String, String>> listPage = (List) hsmpResult.get("RET_LIST");
            int listSize = listPage.size();
            for (int i = 0; i < listSize; i++) {
                HashMap<String, String> hashOpt = (HashMap) listPage.get(i);
                String vendingWay = (String) hashOpt.get(AllAdvertisement.VENDING_WAY);
                String recycleType = (String) hashOpt.get("PRODUCT_TYPE");
                if ("CARD".equalsIgnoreCase(vendingWay)) {
                    vendingWay = this.activity.getString(R.string.greengard);
                }
                if ("COUPON".equalsIgnoreCase(vendingWay) || VendingWay.TICKET.equalsIgnoreCase(vendingWay)) {
                    vendingWay = this.activity.getString(R.string.voucher);
                }
                if ("DONATION".equalsIgnoreCase(vendingWay)) {
                    vendingWay = this.activity.getString(R.string.donate);
                }
                if ("QRCODE".equalsIgnoreCase(vendingWay) || "SQRCODE".equalsIgnoreCase(vendingWay)) {
                    vendingWay = this.activity.getString(R.string.QRcode);
                }
                if ("TRANSPORTCARD".equalsIgnoreCase(vendingWay)) {
                    vendingWay = this.activity.getString(R.string.trafficcard);
                }
                if ("PHONE".equalsIgnoreCase(vendingWay)) {
                    vendingWay = this.activity.getString(R.string.phone);
                }
                if ("WECHAT".equalsIgnoreCase(vendingWay)) {
                    vendingWay = this.activity.getString(R.string.wechat);
                }
                if ("ALIPAY".equalsIgnoreCase(vendingWay)) {
                    vendingWay = this.activity.getString(R.string.Alipay);
                }
                if ("BDJ".equalsIgnoreCase(vendingWay)) {
                    vendingWay = this.activity.getString(R.string.help_home_lv);
                }
                if ("BOTTLE".equalsIgnoreCase(recycleType)) {
                    recycleType = this.activity.getString(R.string.recycle_bottle);
                }
                if ("PAPER".equalsIgnoreCase(recycleType)) {
                    recycleType = this.activity.getString(R.string.recycle_paper);
                }
                hashOpt.put(AllAdvertisement.VENDING_WAY, vendingWay);
                hashOpt.put("PRODUCT_TYPE", recycleType);
                newList.add(hashOpt);
            }
        }
        return newList;
    }
}
