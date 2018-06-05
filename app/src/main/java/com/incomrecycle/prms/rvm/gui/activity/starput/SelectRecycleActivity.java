package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.BroadcastTaskCommonService;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.gui.entity.RebateButtonEntity;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import it.sauronsoftware.ftp4j.FTPCodes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

@SuppressLint({"ResourceAsColor"})
public class SelectRecycleActivity extends BaseActivity {
    public static SelectRecycleActivity selectRecycleActivity;
    private String PRODUCT_TYPE = ((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"));
    private String adconfURL = null;
    private Bitmap bitmap = null;
    private boolean disableServiceIsVisible;
    private List<HashMap<String, String>> ext_vendingway = null;
    private boolean hasClick;
    private boolean isEnableOfCoupon;
    private boolean isPlaySounds;
    private List<String> listDisabledService = new ArrayList();
    private HashMap<String, String> mapExtVendingWay = new HashMap();
    private LinearLayout rebateButtonLayout;
    private boolean showConpon;
    private SoundPool soundPool = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) SelectRecycleActivity.this.findViewById(R.id.selectRecycleTime)).setText("" + remainedSeconds);
                    } else if (!SelectRecycleActivity.this.hasClick) {
                        SelectRecycleActivity.this.hasClick = true;
                        try {
                            HashMap<String, Object> hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "hasRecycled", null);
                            boolean hasPut = false;
                            if (hsmpResult != null && "TRUE".equalsIgnoreCase((String) hsmpResult.get("RESULT"))) {
                                hasPut = true;
                            }
                            if (hasPut) {
                                SelectRecycleActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{SelectRecycleActivity.this.getBaseActivity(), Integer.valueOf(1), "DONATION"});
                                SelectRecycleActivity.this.finish();
                            } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                                try {
                                    Intent intent = new Intent(SelectRecycleActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    SelectRecycleActivity.this.startActivity(intent);
                                    SelectRecycleActivity.this.finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            };
            SelectRecycleActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };
    private List<HashMap<String, String>> vending = null;
    private List vendingWayList = new ArrayList();
    private int xuanZeFanLi;

    protected void onStart() {
        super.onStart();
        this.hasClick = false;
        this.isPlaySounds = Boolean.parseBoolean(SysConfig.get("IS_PLAY_SOUNDS"));
        if (this.isPlaySounds && this.soundPool == null) {
            this.soundPool = new SoundPool(1, 3, 0);
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.SELECTRECYCLE")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void finish() {
        super.finish();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_select_recycle);
        backgroundToActivity();
        selectRecycleActivity = this;
        this.adconfURL = (String) GUIGlobal.getCurrentSession("AD_CONF_URL");
        initView();
    }

    protected void onResume() {
        super.onResume();
        if (this.isPlaySounds && this.soundPool != null) {
            this.xuanZeFanLi = this.soundPool.load(this, R.raw.welcome, 0);
            this.soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    if (sampleId == SelectRecycleActivity.this.xuanZeFanLi) {
                        soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
            });
        }
        GUIGlobal.clearMap();
    }

    private void initView() {
        if ("TRUE".equalsIgnoreCase(SysConfig.get("DISABLE_SERVICE_VISIBLE"))) {
            this.disableServiceIsVisible = true;
        } else {
            this.disableServiceIsVisible = false;
        }
        try {
            HashMap<String, Object> hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryServiceDisable", null);
            if (hsmpResult != null) {
                String SERVICE_DISABLED = (String) hsmpResult.get("SERVICE_DISABLED");
                if (!StringUtils.isBlank(SERVICE_DISABLED)) {
                    String[] SERVICE_DISABLED_ARRAY = SERVICE_DISABLED.split(",");
                    for (String add : SERVICE_DISABLED_ARRAY) {
                        this.listDisabledService.add(add);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.rebateButtonLayout = (LinearLayout) findViewById(R.id.buttons);
        this.rebateButtonLayout.removeAllViews();
        getRebateList();
        setDynamicButtonFullSize();
        if ("PAPER".equals(this.PRODUCT_TYPE)) {
            Button btnPaperReturn = (Button) findViewById(R.id.PaperReturnBtn);
            btnPaperReturn.setVisibility(View.VISIBLE);
            btnPaperReturn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(SelectRecycleActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            SelectRecycleActivity.this.startActivity(intent);
                            SelectRecycleActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
        if ("CMD".equalsIgnoreCase((String) hsmpEvent.get("EVENT"))) {
            String str = (String) hsmpEvent.get("CMD");
        }
    }

    protected void onStop() {
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
    }

    private void getRebateList() {
        int i;
        String vendingWayStr = SysConfig.get("VENDING.WAY");
        String[] vendingWayStrArray = vendingWayStr.split(";");
        String vendingWaySet = SysConfig.get("VENDING.WAY.SET");
        List vendingWaySetList = null;
        if (!StringUtils.isBlank(vendingWaySet)) {
            vendingWaySetList = Arrays.asList(vendingWaySet.split(";"));
        }
        for (i = 0; i < vendingWayStrArray.length; i++) {
            RebateButtonEntity rebateButtonEntity;
            if (vendingWayStrArray[i].equals("TRANSPORTCARD") && vendingWaySetList.contains("TRANSPORTCARD")) {
                rebateButtonEntity = new RebateButtonEntity();
                rebateButtonEntity.setBackgroundFromResource(R.drawable.yikatong_icon_on);
                rebateButtonEntity.setText(getString(R.string.trafficcard));
                rebateButtonEntity.setVisibility(View.VISIBLE);
                rebateButtonEntity.setVendingWay("TRANSPORTCARD");
                rebateButtonEntity.setButtonType(AllAdvertisement.BUTTON_TYPE_NATIVE);
                if (!this.listDisabledService.contains("TRANSPORTCARD")) {
                    rebateButtonEntity.setEnabled(true);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.yikatong_icon_on);
                } else if (this.disableServiceIsVisible) {
                    rebateButtonEntity.setEnabled(false);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.yikatong_icon_unable);
                } else {
                    rebateButtonEntity.setVisibility(8);
                }
                this.vendingWayList.add(rebateButtonEntity);
            }
            if (vendingWayStrArray[i].equals("PHONE") && vendingWaySetList.contains("PHONE")) {
                rebateButtonEntity = new RebateButtonEntity();
                rebateButtonEntity.setBackgroundFromResource(R.drawable.shouji_icon_on);
                rebateButtonEntity.setText(getString(R.string.phone));
                rebateButtonEntity.setVisibility(View.VISIBLE);
                rebateButtonEntity.setVendingWay("PHONE");
                rebateButtonEntity.setButtonType(AllAdvertisement.BUTTON_TYPE_NATIVE);
                if (!this.listDisabledService.contains("PHONE")) {
                    rebateButtonEntity.setEnabled(true);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.shouji_icon_on);
                } else if (this.disableServiceIsVisible) {
                    rebateButtonEntity.setEnabled(false);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.shouji_btn_unable);
                } else {
                    rebateButtonEntity.setVisibility(View.GONE);
                }
                this.vendingWayList.add(rebateButtonEntity);
            }
            if (vendingWayStrArray[i].equalsIgnoreCase("DONATION") && vendingWaySetList.contains("DONATION")) {
                rebateButtonEntity = new RebateButtonEntity();
                rebateButtonEntity.setBackgroundFromResource(R.drawable.juanzeng_icon_on);
                rebateButtonEntity.setText(getString(R.string.donate));
                rebateButtonEntity.setVisibility(View.VISIBLE);
                rebateButtonEntity.setVendingWay("DONATION");
                rebateButtonEntity.setButtonType(AllAdvertisement.BUTTON_TYPE_NATIVE);
                if (!this.listDisabledService.contains("DONATION")) {
                    rebateButtonEntity.setEnabled(true);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.juanzeng_icon_on);
                } else if (this.disableServiceIsVisible) {
                    rebateButtonEntity.setEnabled(false);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.juanzeng_icon_unable);
                } else {
                    rebateButtonEntity.setVisibility(View.GONE);
                }
                this.vendingWayList.add(rebateButtonEntity);
            }
            if (vendingWayStrArray[i].equals("QRCODE") && vendingWaySetList.contains("QRCODE")) {
                rebateButtonEntity = new RebateButtonEntity();
                rebateButtonEntity.setBackgroundFromResource(R.drawable.lvniukou_icon_on);
                rebateButtonEntity.setText(getString(R.string.QRcode));
                rebateButtonEntity.setVisibility(View.VISIBLE);
                rebateButtonEntity.setVendingWay("QRCODE");
                rebateButtonEntity.setButtonType(AllAdvertisement.BUTTON_TYPE_NATIVE);
                if (!this.listDisabledService.contains("QRCODE")) {
                    rebateButtonEntity.setEnabled(true);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.lvniukou_icon_on);
                } else if (this.disableServiceIsVisible) {
                    rebateButtonEntity.setEnabled(false);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.lvniukou_icon_unable);
                } else {
                    rebateButtonEntity.setVisibility(View.GONE);
                }
                this.vendingWayList.add(rebateButtonEntity);
            }
            if (vendingWayStrArray[i].equals("WECHAT") && vendingWaySetList.contains("WECHAT")) {
                rebateButtonEntity = new RebateButtonEntity();
                rebateButtonEntity.setBackgroundFromResource(R.drawable.weixin_icon_on);
                rebateButtonEntity.setText(getString(R.string.wechat));
                rebateButtonEntity.setVisibility(View.VISIBLE);
                rebateButtonEntity.setVendingWay("WECHAT");
                rebateButtonEntity.setButtonType(AllAdvertisement.BUTTON_TYPE_NATIVE);
                if (!this.listDisabledService.contains("WECHAT")) {
                    rebateButtonEntity.setEnabled(true);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.weixin_icon_on);
                } else if (this.disableServiceIsVisible) {
                    rebateButtonEntity.setEnabled(false);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.weixin_icon_unable);
                } else {
                    rebateButtonEntity.setVisibility(View.GONE);
                }
                this.vendingWayList.add(rebateButtonEntity);
            }
            if (vendingWayStrArray[i].equals("BDJ") && vendingWaySetList.contains("BDJ")) {
                rebateButtonEntity = new RebateButtonEntity();
                rebateButtonEntity.setBackgroundFromResource(R.drawable.icon_lvniukou_n);
                rebateButtonEntity.setText(getString(R.string.bdddj));
                rebateButtonEntity.setVisibility(View.VISIBLE);
                rebateButtonEntity.setVendingWay("BDJ");
                rebateButtonEntity.setButtonType(AllAdvertisement.BUTTON_TYPE_NATIVE);
                if (!this.listDisabledService.contains("BDJ")) {
                    rebateButtonEntity.setEnabled(true);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.icon_lvniukou_n);
                } else if (this.disableServiceIsVisible) {
                    rebateButtonEntity.setEnabled(false);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.icon_lvniukou_un);
                } else {
                    rebateButtonEntity.setVisibility(View.GONE);
                }
                this.vendingWayList.add(rebateButtonEntity);
            }
            if (vendingWayStrArray[i].equals("ALIPAY") && vendingWaySetList.contains("ALIPAY")) {
                rebateButtonEntity = new RebateButtonEntity();
                rebateButtonEntity.setBackgroundFromResource(R.drawable.zhifubao_icon_on);
                rebateButtonEntity.setText(getString(R.string.Alipay));
                rebateButtonEntity.setVisibility(View.VISIBLE);
                rebateButtonEntity.setVendingWay("ALIPAY");
                rebateButtonEntity.setButtonType(AllAdvertisement.BUTTON_TYPE_NATIVE);
                if (!this.listDisabledService.contains("ALIPAY")) {
                    rebateButtonEntity.setEnabled(true);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.zhifubao_icon_on);
                } else if (this.disableServiceIsVisible) {
                    rebateButtonEntity.setEnabled(false);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.zhifubao_icon_unable);
                } else {
                    rebateButtonEntity.setVisibility(View.GONE);
                }
                this.vendingWayList.add(rebateButtonEntity);
            }
            if (vendingWayStrArray[i].equals("COUPON") && vendingWaySetList.contains("COUPON")) {
                rebateButtonEntity = new RebateButtonEntity();
                rebateButtonEntity.setBackgroundFromResource(R.drawable.youhuijuan_icon_on);
                rebateButtonEntity.setText(getString(R.string.voucher));
                rebateButtonEntity.setVisibility(View.VISIBLE);
                rebateButtonEntity.setVendingWay("COUPON");
                rebateButtonEntity.setButtonType(AllAdvertisement.BUTTON_TYPE_NATIVE);
                if (this.listDisabledService.contains("COUPON")) {
                    this.showConpon = false;
                    if (!this.disableServiceIsVisible) {
                        rebateButtonEntity.setVisibility(View.GONE);
                    }
                } else {
                    this.showConpon = true;
                }
                boolean hasPrinterPaper = false;
                boolean vendingWayVoucherEnable = false;
                try {
                    HashMap<String, Object> hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIVoucherCommonService", "queryVoucherList", null);
                    if (hsmpResult != null) {
                        List<HashMap<String, String>> listVoucher2 = (List) hsmpResult.get("VOUCHER_LIST");
                        if (listVoucher2 != null && listVoucher2.size() > 0) {
                            vendingWayVoucherEnable = true;
                        }
                    }
                    hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "hasPrinterPaper", null);
                    if (hsmpResult != null) {
                        if ("HAVE_PAPER".equalsIgnoreCase((String) hsmpResult.get("RET_CODE"))) {
                            hasPrinterPaper = true;
                        }
                    }
                    boolean z = vendingWayVoucherEnable && this.showConpon && hasPrinterPaper;
                    this.isEnableOfCoupon = z;
                    if (this.isEnableOfCoupon) {
                        rebateButtonEntity.setEnabled(true);
                        rebateButtonEntity.setBackgroundFromResource(R.drawable.youhuijuan_icon_unable);
                    } else {
                        rebateButtonEntity.setEnabled(false);
                        rebateButtonEntity.setBackgroundFromResource(R.drawable.youhuijuan_icon_on);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.vendingWayList.add(rebateButtonEntity);
            }
            if (vendingWayStrArray[i].equals("CARD") && vendingWaySetList.contains("CARD")) {
                rebateButtonEntity = new RebateButtonEntity();
                rebateButtonEntity.setBackgroundFromResource(R.drawable.card_icon_on);
                rebateButtonEntity.setText(getString(R.string.greengard));
                rebateButtonEntity.setVisibility(View.VISIBLE);
                rebateButtonEntity.setVendingWay("CARD");
                rebateButtonEntity.setButtonType(AllAdvertisement.BUTTON_TYPE_NATIVE);
                if (!this.listDisabledService.contains("CARD")) {
                    rebateButtonEntity.setEnabled(true);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.card_icon_on);
                } else if (this.disableServiceIsVisible) {
                    rebateButtonEntity.setEnabled(false);
                    rebateButtonEntity.setBackgroundFromResource(R.drawable.card_icon_unable);
                } else {
                    rebateButtonEntity.setVisibility(View.GONE);
                }
                this.vendingWayList.add(rebateButtonEntity);
            }
        }
        if (this.vendingWayList != null && this.vendingWayList.size() > 0) {
            int vendingWayListSize = this.vendingWayList.size();
            for (int j = 0; j < vendingWayListSize; j++) {
                RebateButtonEntity rebateEntity = (RebateButtonEntity) this.vendingWayList.get(j);
                RebateButtonEntity originalRebateEntity;
                if ("BDJ".equalsIgnoreCase(rebateEntity.getVendingWay()) && rebateEntity.getVisibility() == 0) {
                    int middlePosition = ((vendingWayListSize / 2) + (vendingWayListSize % 2)) - 1;
                    if (middlePosition != 0) {
                        originalRebateEntity = (RebateButtonEntity) this.vendingWayList.get(middlePosition);
                        this.vendingWayList.set(middlePosition, rebateEntity);
                        this.vendingWayList.set(j, originalRebateEntity);
                    }
                } else if ("DONATION".equalsIgnoreCase(rebateEntity.getVendingWay()) && rebateEntity.getVisibility() == 0) {
                    originalRebateEntity = (RebateButtonEntity) this.vendingWayList.get(0);
                    this.vendingWayList.set(0, rebateEntity);
                    this.vendingWayList.set(j, originalRebateEntity);
                }
            }
        }
        this.ext_vendingway = BroadcastTaskCommonService.getExtVendingWay(this.adconfURL);
        if (this.ext_vendingway != null) {
            for (i = 0; i < this.ext_vendingway.size(); i++) {
                this.mapExtVendingWay = (HashMap) this.ext_vendingway.get(i);
                RebateButtonEntity rebateButtonEntity = new RebateButtonEntity();
                rebateButtonEntity.setBackgroundFromSDCard((String) this.mapExtVendingWay.get(AllAdvertisement.VENDING_WAY_PIC));
                rebateButtonEntity.setText((String) this.mapExtVendingWay.get(AllAdvertisement.VENDING_WAY_TITLE));
                rebateButtonEntity.setVisibility(8);
                rebateButtonEntity.setButtonType(AllAdvertisement.BUTTON_TYPE_EXTENSION);
                rebateButtonEntity.setEnabled(true);
                String vendingWay = (String) this.mapExtVendingWay.get(AllAdvertisement.VENDING_WAY);
                rebateButtonEntity.setVendingWay(vendingWay);
                if (vendingWayStr.contains(vendingWay) && vendingWaySet.contains(vendingWay)) {
                    int vendingWayPosition;
                    rebateButtonEntity.setVisibility(0);
                    if ("DONATION".equals(vendingWay) && this.listDisabledService.contains("DONATION")) {
                        if (this.disableServiceIsVisible) {
                            rebateButtonEntity.setEnabled(false);
                        } else {
                            rebateButtonEntity.setVisibility(8);
                        }
                    }
                    if ("COUPON".equals(vendingWay) && this.listDisabledService.contains("COUPON")) {
                        if (this.disableServiceIsVisible) {
                            rebateButtonEntity.setEnabled(false);
                        } else {
                            rebateButtonEntity.setVisibility(8);
                        }
                    }
                    if ("QRCODE".equals(vendingWay) && this.listDisabledService.contains("QRCODE")) {
                        if (this.disableServiceIsVisible) {
                            rebateButtonEntity.setEnabled(false);
                        } else {
                            rebateButtonEntity.setVisibility(8);
                        }
                    }
                    if ("PHONE".equals(vendingWay) && this.listDisabledService.contains("PHONE")) {
                        if (this.disableServiceIsVisible) {
                            rebateButtonEntity.setEnabled(false);
                        } else {
                            rebateButtonEntity.setVisibility(8);
                        }
                    }
                    if ("WECHAT".equals(vendingWay) && this.listDisabledService.contains("WECHAT")) {
                        if (this.disableServiceIsVisible) {
                            rebateButtonEntity.setEnabled(false);
                        } else {
                            rebateButtonEntity.setVisibility(8);
                        }
                    }
                    if ("ALIPAY".equals(vendingWay) && this.listDisabledService.contains("ALIPAY")) {
                        if (this.disableServiceIsVisible) {
                            rebateButtonEntity.setEnabled(false);
                        } else {
                            rebateButtonEntity.setVisibility(8);
                        }
                    }
                    if ("TRANSPORTCARD".equals(vendingWay) && this.listDisabledService.contains("TRANSPORTCARD")) {
                        if (this.disableServiceIsVisible) {
                            rebateButtonEntity.setEnabled(false);
                        } else {
                            rebateButtonEntity.setVisibility(8);
                        }
                    }
                    if ("BDJ".equals(vendingWay) && this.listDisabledService.contains("BDJ")) {
                        if (this.disableServiceIsVisible) {
                            rebateButtonEntity.setEnabled(false);
                        } else {
                            rebateButtonEntity.setVisibility(8);
                        }
                    }
                    if ("CARD".equals(vendingWay) && this.listDisabledService.contains("CARD")) {
                        if (this.disableServiceIsVisible) {
                            rebateButtonEntity.setEnabled(false);
                        } else {
                            rebateButtonEntity.setVisibility(8);
                        }
                    }
                    try {
                        vendingWayPosition = Integer.parseInt((String) this.mapExtVendingWay.get(AllAdvertisement.VENDING_WAY_POSITION));
                    } catch (Exception e2) {
                        vendingWayPosition = 1;
                    }
                    try {
                        this.vendingWayList.add(vendingWayPosition - 1, rebateButtonEntity);
                    } catch (Exception e3) {
                        this.vendingWayList.add(0, rebateButtonEntity);
                    }
                }
            }
        }
    }

    private void setDynamicButton() {
        if (this.vendingWayList != null) {
            for (int i = 0; i < this.vendingWayList.size(); i++) {
                final RebateButtonEntity myButtonEntity = (RebateButtonEntity) this.vendingWayList.get(i);
                final String buttonType = myButtonEntity.getButtonType();
                Button myRebateButton = new Button(selectRecycleActivity);
                myRebateButton.setPadding(0, FTPCodes.FILE_STATUS_OK, 0, 0);
                LayoutParams params = new LayoutParams(FTPCodes.SERVICE_READY_FOR_NEW_USER, -1);
                params.bottomMargin = 5;
                if (i != 0) {
                    params.leftMargin = 2;
                }
                myRebateButton.setLayoutParams(params);
                myRebateButton.setEnabled(myButtonEntity.isEnabled());
                if (AllAdvertisement.BUTTON_TYPE_NATIVE.equals(buttonType)) {
                    myRebateButton.setBackgroundResource(myButtonEntity.getBackgroundFromResource());
                } else {
                    this.bitmap = BitmapFactory.decodeFile(myButtonEntity.getBackgroundFromSDCard());
                    myRebateButton.setBackgroundDrawable(new BitmapDrawable(this.bitmap));
                }
                myRebateButton.setText(myButtonEntity.getText());
                myRebateButton.setTextColor(-1);
                myRebateButton.setTextSize(17.0f);
                myRebateButton.setTypeface(Typeface.DEFAULT_BOLD, 2);
                myRebateButton.setVisibility(myButtonEntity.getVisibility());
                myRebateButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        HashMap map = new HashMap();
                        String vendingWay = myButtonEntity.getVendingWay();
                        if (AllAdvertisement.BUTTON_TYPE_NATIVE.equals(buttonType)) {
                            SelectRecycleActivity.this.setVendingWayFlag(vendingWay);
                        } else {
                            SelectRecycleActivity.this.setExtVendingWayFlag(vendingWay);
                        }
                        if ("DONATION".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_DONATE);
                        }
                        if ("COUPON".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_PRINTPAPER);
                        }
                        if ("QRCODE".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_QRCODE);
                        }
                        if ("PHONE".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_PHONE);
                        }
                        if ("TRANSPORTCARD".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_ONECARD);
                        }
                        if ("CARD".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_GREENCARD);
                        }
                        if ("WECHAT".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_WECHAT);
                        }
                        if ("ALIPAY".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_ALIPAY);
                        }
                        if ("BDJ".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_BDJ);
                        }
                        try {
                            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                        } catch (Exception e) {
                        }
                        if (!SelectRecycleActivity.this.hasClick) {
                            SelectRecycleActivity.this.hasClick = true;
                            SelectRecycleActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{SelectRecycleActivity.this.getBaseActivity(), Integer.valueOf(1), vendingWay});
                            SelectRecycleActivity.this.finish();
                        }
                    }
                });
                this.rebateButtonLayout.addView(myRebateButton);
            }
        }
    }

    private void setVendingWayFlag(String vendingWay) {
        this.vending = BroadcastTaskCommonService.getVendingWayFlag(this.adconfURL);
        if (this.vending != null) {
            for (int i = 0; i < this.vending.size(); i++) {
                HashMap mapVendingWay = (HashMap) this.vending.get(i);
                if (!StringUtils.isBlank(vendingWay)) {
                    String myVendingSet = (String) mapVendingWay.get(AllAdvertisement.VENDING_WAY_SET);
                    if (!StringUtils.isBlank(myVendingSet) && myVendingSet.contains(vendingWay)) {
                        GUIGlobal.setCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG, mapVendingWay);
                        return;
                    }
                }
            }
        }
    }

    private void setExtVendingWayFlag(String vendingWay) {
        for (int i = 0; i < this.ext_vendingway.size(); i++) {
            HashMap mapExtVendingWay = (HashMap) this.ext_vendingway.get(i);
            if (!StringUtils.isBlank(vendingWay)) {
                String extVendingSet = (String) mapExtVendingWay.get(AllAdvertisement.VENDING_WAY);
                if (!StringUtils.isBlank(extVendingSet) && extVendingSet.equals(vendingWay)) {
                    GUIGlobal.setCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG, mapExtVendingWay);
                    return;
                }
            }
        }
    }

    private void setDynamicButtonFullSize() {
        if (this.vendingWayList != null) {
            int i;
            int screenWidth = selectRecycleActivity.getWindowManager().getDefaultDisplay().getWidth();
            int visibilityVendingWaySize = 0;
            for (i = 0; i < this.vendingWayList.size(); i++) {
                if (((RebateButtonEntity) this.vendingWayList.get(i)).getVisibility() == 0) {
                    visibilityVendingWaySize++;
                }
            }
            int maxVendingWay = Integer.parseInt(SysConfig.get("MAX.VENDING.WAY"));
            if (visibilityVendingWaySize > maxVendingWay) {
                visibilityVendingWaySize = maxVendingWay;
            }
            int fatherViewWidth = (screenWidth - 174) / visibilityVendingWaySize;
            for (i = 0; i < this.vendingWayList.size(); i++) {
                RebateButtonEntity myButtonEntity = (RebateButtonEntity) this.vendingWayList.get(i);
                final String buttonType = myButtonEntity.getButtonType();
                LinearLayout linLayout = new LinearLayout(selectRecycleActivity);
                linLayout.setOrientation(1);
                linLayout.setGravity(1);
                ImageView img = new ImageView(selectRecycleActivity);
                img.setBackgroundResource(myButtonEntity.getBackgroundFromResource());
                LayoutParams imgParams = new LayoutParams(-2, -2);
                imgParams.setMargins(0, 60, 0, 0);
                img.setLayoutParams(imgParams);
                linLayout.addView(img);
                TextView textView = new TextView(selectRecycleActivity);
                textView.setText(myButtonEntity.getText());
                textView.setTextColor(-1);
                textView.setTextSize(17.0f);
                textView.setTypeface(Typeface.DEFAULT_BOLD, 2);
                LayoutParams layoutParams = new LayoutParams(-2, -2);
                layoutParams.setMargins(0, 20, 0, 0);
                textView.setLayoutParams(layoutParams);
                linLayout.addView(textView);
                String vendingWay = myButtonEntity.getVendingWay();
                if (!StringUtils.isBlank(vendingWay) && ("QRCODE".equals(vendingWay) || "BDJ".equals(vendingWay))) {
                    TextView explainTxt = new TextView(selectRecycleActivity);
                    explainTxt.setTextColor(-1);
                    explainTxt.setTextSize(15.0f);
                    explainTxt.setTypeface(Typeface.DEFAULT_BOLD, 2);
                    LayoutParams explainTxtParams = new LayoutParams(-2, -2);
                    explainTxtParams.setMargins(5, 5, 5, 5);
                    if ("BDJ".equals(vendingWay)) {
                        explainTxt.setText(R.string.bdj_xplain);
                    }
                    if ("QRCODE".equals(vendingWay)) {
                        explainTxt.setText(R.string.qrcode_xplain);
                    }
                    explainTxt.setLayoutParams(explainTxtParams);
                    linLayout.addView(explainTxt);
                }
                LayoutParams fatherParams = new LayoutParams(fatherViewWidth, -1);
                fatherParams.bottomMargin = 1;
                if (i != 0) {
                    fatherParams.leftMargin = 1;
                }
                linLayout.setLayoutParams(fatherParams);
                boolean isEnabled = myButtonEntity.isEnabled();
                linLayout.setEnabled(isEnabled);
                if (isEnabled) {
                    linLayout.setBackgroundResource(R.drawable.selectrecycleselector);
                } else {
                    linLayout.setBackgroundResource(R.drawable.select_recycle_btn_off);
                }
                linLayout.setVisibility(myButtonEntity.getVisibility());
                final RebateButtonEntity rebateButtonEntity = myButtonEntity;
                linLayout.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        HashMap map = new HashMap();
                        String vendingWay = rebateButtonEntity.getVendingWay();
                        if (AllAdvertisement.BUTTON_TYPE_NATIVE.equals(buttonType)) {
                            SelectRecycleActivity.this.setVendingWayFlag(vendingWay);
                        } else {
                            SelectRecycleActivity.this.setExtVendingWayFlag(vendingWay);
                        }
                        if ("DONATION".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_DONATE);
                        }
                        if ("COUPON".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_PRINTPAPER);
                        }
                        if ("QRCODE".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_QRCODE);
                        }
                        if ("PHONE".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_PHONE);
                        }
                        if ("TRANSPORTCARD".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_ONECARD);
                        }
                        if ("CARD".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_GREENCARD);
                        }
                        if ("WECHAT".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_WECHAT);
                        }
                        if ("ALIPAY".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_ALIPAY);
                        }
                        if ("BDJ".equals(vendingWay)) {
                            map.put("KEY", AllClickContent.REBATE_BDJ);
                        }
                        try {
                            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                        } catch (Exception e) {
                        }
                        if (!SelectRecycleActivity.this.hasClick) {
                            SelectRecycleActivity.this.hasClick = true;
                            SelectRecycleActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{SelectRecycleActivity.this.getBaseActivity(), Integer.valueOf(1), vendingWay});
                            SelectRecycleActivity.this.finish();
                        }
                    }
                });
                this.rebateButtonLayout.addView(linLayout);
            }
        }
    }
}
