package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class SelectVoucherActivity extends BaseActivity {
    String PRODUCT_TYPE = ((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"));
    private OnItemClickListener clickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long id) {
            if (SelectVoucherActivity.this.pWindow != null && SelectVoucherActivity.this.pWindow.isShowing()) {
                SelectVoucherActivity.this.pWindow.dismiss();
            }
            SelectVoucherActivity.this.isSelect = true;
            TimeoutTask.getTimeoutTask().reset(SelectVoucherActivity.this.timeoutAction);
            HashMap<String, String> hsmpVoucher = (HashMap) SelectVoucherActivity.this.listVoucher.get(position);
            String ACTIVITY_ID = (String) hsmpVoucher.get("ACTIVITY_ID");
            String PRINT_INFO = (String) hsmpVoucher.get("PRINT_INFO");
            String VOUCHER_NAME = (String) hsmpVoucher.get("textItem");
            String PRINT_CONTENT = null;
            if (!StringUtils.isBlank(PRINT_INFO)) {
                GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                try {
                    HashMap hsmpParam = new HashMap();
                    hsmpParam.put("PRINT_INFO", PRINT_INFO);
                    HashMap hsmpResult = guiCommonService.execute("GUIVoucherCommonService", "previewVoucher", hsmpParam);
                    if (hsmpResult != null) {
                        PRINT_CONTENT = (String) hsmpResult.get("PRINT_INFO");
                    }
                } catch (Exception e) {
                    PRINT_CONTENT = PRINT_INFO;
                }
            }
            View inflate = LayoutInflater.from(SelectVoucherActivity.this).inflate(R.layout.popupwindow_select_voucher, null, false);
            ((TextView) inflate.findViewById(R.id.content_SelectVoucher)).setText(PRINT_CONTENT);
            SelectVoucherActivity.this.pWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b0000000")));
            SelectVoucherActivity.this.pWindow.setWidth(290);
            SelectVoucherActivity.this.pWindow.setHeight(341);
            SelectVoucherActivity.this.pWindow.setContentView(inflate);
            SelectVoucherActivity.this.pWindow.showAtLocation(arg1, 3, 98, 10);
            GUIGlobal.setCurrentSession("ACTIVITY_ID", ACTIVITY_ID);
            GUIGlobal.setCurrentSession("PRINT_RULE", hsmpVoucher.get("PRINT_RULE"));
            GUIGlobal.setCurrentSession("VOUCHER_NAME", VOUCHER_NAME);
        }
    };
    private TextView couponInfo = null;
    private boolean isPlaySounds;
    private boolean isSelect = false;
    private List<HashMap<String, String>> listVoucher = new ArrayList();
    private PopupWindow pWindow;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) SelectVoucherActivity.this.findViewById(R.id.showCouponTime)).setText("" + remainedSeconds);
                    } else if ("PAPER".equals(SelectVoucherActivity.this.PRODUCT_TYPE)) {
                        Intent intent = new Intent(SelectVoucherActivity.this, SelectRecycleActivity.class);
                        intent.putExtra("RECYCLE", "RECYCLEPAPER");
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        SelectVoucherActivity.this.startActivity(intent);
                        SelectVoucherActivity.this.finish();
                    } else {
                        Intent intent = new Intent(SelectVoucherActivity.this, SelectRecycleActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        SelectVoucherActivity.this.startActivity(intent);
                        SelectVoucherActivity.this.finish();
                    }
                }
            };
            SelectVoucherActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private int xuanZeYouHuiQuan;

    public void onStart() {
        super.onStart();
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.SELECTVOUCHER")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        this.listVoucher.clear();
        try {
            HashMap<String, Object> hsmpResult = guiCommonService.execute("GUIVoucherCommonService", "queryVoucherList", null);
            if (hsmpResult != null) {
                List<HashMap<String, String>> listVoucher2 = (List) hsmpResult.get("VOUCHER_LIST");
                int num = listVoucher2.size();
                if (listVoucher2 != null) {
                    for (int i = 0; i < num; i++) {
                        HashMap<String, String> hsmpVoucher = (HashMap) listVoucher2.get(i);
                        String PRINT_INFO = (String) hsmpVoucher.get("PRINT_INFO");
                        String PIC_INFO = (String) hsmpVoucher.get("PIC_INFO");
                        String ACTIVITY_NAME = (String) hsmpVoucher.get("ACTIVITY_NAME");
                        String imageItem = null;
                        if (!StringUtils.isBlank(PIC_INFO) && new File(PIC_INFO).isFile()) {
                            imageItem = PIC_INFO;
                        }
                        if (imageItem != null) {
                            hsmpVoucher.put("imageItem", imageItem);
                        }
                        if (!StringUtils.isBlank(ACTIVITY_NAME)) {
                            hsmpVoucher.put("textItem", ACTIVITY_NAME);
                        }
                        this.listVoucher.add(hsmpVoucher);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        GridView gridview = (GridView) findViewById(R.id.gridview_main);
        gridview.setAdapter(new SimpleAdapter(this, this.listVoucher, R.layout.activity_printvoucher, new String[]{"imageItem", "textItem"}, new int[]{R.id.image_item, R.id.text_item}));
        gridview.setOnItemClickListener(this.clickListener);
        if (this.listVoucher.size() <= 0 && this.couponInfo != null) {
            this.couponInfo.setText(getString(R.string.havenot_voucher));
        }
        this.pWindow = new PopupWindow(this);
    }

    protected void onResume() {
        super.onResume();
        if (this.isPlaySounds && this.soundPool != null) {
            this.xuanZeYouHuiQuan = this.soundPool.load(this, R.raw.xuanzeyouhuiquan, 0);
            this.soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    if (sampleId == SelectVoucherActivity.this.xuanZeYouHuiQuan) {
                        soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
            });
        }
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
        if (this.pWindow != null || this.pWindow.isShowing()) {
            this.pWindow.dismiss();
            this.pWindow = null;
        }
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_voucher_gridview);
        backgroundToActivity();
        Button btnRturn = (Button) findViewById(R.id.showCouponReturnBtn);
        Button btnConfirm = (Button) findViewById(R.id.showCouponConfirmBtn);
        this.couponInfo = (TextView) findViewById(R.id.show_coupon_info);
        btnRturn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.REBATE_PRINTPAPER_RETURN);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                if ("PAPER".equals(SelectVoucherActivity.this.PRODUCT_TYPE)) {
                    Intent intent = new Intent(SelectVoucherActivity.this, SelectRecycleActivity.class);
                    intent.putExtra("RECYCLE", "RECYCLEPAPER");
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    SelectVoucherActivity.this.startActivity(intent);
                    SelectVoucherActivity.this.finish();
                    return;
                }
                Intent intent = new Intent(SelectVoucherActivity.this, SelectRecycleActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                SelectVoucherActivity.this.startActivity(intent);
                SelectVoucherActivity.this.finish();
            }
        });
        btnConfirm.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.REBATE_PRINTPAPER_CONFIRM);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                if (SelectVoucherActivity.this.isSelect) {
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
                            HashMap<String, Object> VENDING_FLAG = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
                            Intent intent;
                            if (TRANSMIT_ADV != null) {
                                HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
                                if (HOMEPAGE_LEFT == null || StringUtils.isBlank((String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_PIC))) {
                                    SelectVoucherActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{SelectVoucherActivity.this.getBaseActivity(), Integer.valueOf(2), "COUPON"});
                                    SelectVoucherActivity.this.finish();
                                    return;
                                }
                                intent = new Intent(SelectVoucherActivity.this, ActivityAdActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                SelectVoucherActivity.this.startActivity(intent);
                                SelectVoucherActivity.this.finish();
                            } else if (VENDING_FLAG == null || StringUtils.isBlank((String) VENDING_FLAG.get(AllAdvertisement.VENDING_PIC))) {
                                SelectVoucherActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{SelectVoucherActivity.this.getBaseActivity(), Integer.valueOf(2), "COUPON"});
                                SelectVoucherActivity.this.finish();
                            } else {
                                intent = new Intent(SelectVoucherActivity.this, ActivityAdActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                SelectVoucherActivity.this.startActivity(intent);
                                SelectVoucherActivity.this.finish();
                            }
                        }
                    });
                } else if (SelectVoucherActivity.this.couponInfo != null) {
                    SelectVoucherActivity.this.couponInfo.setText(SelectVoucherActivity.this.getString(R.string.select_coupon_remind));
                }
            }
        });
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }
}
