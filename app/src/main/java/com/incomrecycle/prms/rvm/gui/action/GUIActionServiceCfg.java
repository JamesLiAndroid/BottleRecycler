package com.incomrecycle.prms.rvm.gui.action;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.RVMShell;
import com.incomrecycle.prms.rvm.common.SysDef.CardType;
import com.incomrecycle.prms.rvm.common.SysDef.MediaInfo;
import com.incomrecycle.prms.rvm.common.SysDef.ServiceName;
import com.incomrecycle.prms.rvm.common.SysDef.maintainOptContent;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelAdvancedActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class GUIActionServiceCfg extends GUIAction implements OnClickListener {
    private TextView AcceptBottle_current_status;
    private TextView AcceptCan_current_status;
    private TextView Alipay_current_status;
    private TextView CServicesText_current_statusCServices;
    private TextView D2CodeText_current_status;
    private TextView Print_current_status;
    private String START_ALIPAY_Sts = null;
    private String START_BDJ_Sts = null;
    private String START_BOTTLELIMITED_Sts = null;
    private String START_CARD_Sts = null;
    private String START_CONVENIENCE_Sts = null;
    private String START_COUPON_Sts = null;
    private String START_DONATION_Sts = null;
    private String START_LNKCARD_Sts = null;
    private String START_ONECARD_Sts = null;
    private String START_PHONE_Sts = null;
    private String START_PRINT_Sts = null;
    private String START_RECYCLE_BOTTLE_Sts = null;
    private String START_RECYCLE_PAPER_Sts = null;
    private String START_WECHAT_Sts = null;
    private String STOP_ALIPAY_Sts = null;
    private String STOP_BDJ_Sts = null;
    private String STOP_BOTTLELIMITED_Sts = null;
    private String STOP_CARD_Sts = null;
    private String STOP_CONVENIENCE_Sts = null;
    private String STOP_COUPON_Sts = null;
    private String STOP_DONATION_Sts = null;
    private String STOP_LNKCARD_Sts = null;
    private String STOP_ONECARD_Sts = null;
    private String STOP_PHONE_Sts = null;
    private String STOP_PRINT_Sts = null;
    private String STOP_RECYCLE_BOTTLE_Sts = null;
    private String STOP_RECYCLE_PAPER_Sts = null;
    private String STOP_WECHAT_Sts = null;
    private TextView Wechat_current_status;
    private ChannelAdvancedActivity activity;
    private TextView bdjText;
    private TextView bdj_current_status;
    private TextView bottleText_current_status;
    private TextView bottlelimited_current_status;
    private TextView couponText_currnet_status;
    private TextView donationText_current_status;
    private LinearLayout fenxuanqi_ServiceLayout;
    private TextView fenxuanqi_current_status;
    private TextView greengardText_current_status;
    private GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
    private HashMap<String, Object> hsmpParam = new HashMap();
    private LinearLayout lin;
    List<String> listOpt = new ArrayList();
    private Logger logger = LoggerFactory.getLogger("VIEW");
    private TextView mobilePhoneText_current_status;
    private TextView paperText_current_status;
    private Button startAcceptBottleBtn;
    private Button startAcceptCanBtn;
    private Button startAlipayBtn;
    private Button startBDJBtn;
    private Button startBottleBtn;
    private Button startBottlelimitedBtn;
    private Button startConServiceBtn;
    private Button startCouponBtn;
    private Button startD2CodeBtn;
    private Button startDonationBtn;
    private Button startGreengardBtn;
    private Button startMobilePhoneBtn;
    private Button startPaperBtn;
    private Button startPrintBtn;
    private Button startTrafficCardBtn;
    private Button startWechatBtn;
    private Button startfenxuanqi_Btn;
    private Button stopAcceptBottleBtn;
    private Button stopAcceptCanBtn;
    private Button stopAlipayBtn;
    private Button stopBDJBtn;
    private Button stopBottleBtn;
    private Button stopBottlelimitedBtn;
    private Button stopConServiceBtn;
    private Button stopCouponBtn;
    private Button stopD2CodeBtn;
    private Button stopDonationBtn;
    private Button stopGreengardBtn;
    private Button stopMobilePhoneBtn;
    private Button stopPaperBtn;
    private Button stopPrintBtn;
    private Button stopTrafficCardBtn;
    private Button stopWechatBtn;
    private Button stopfenxuanqi_Btn;
    private TextView trafficCardText_current_status;

    protected void doAction(Object[] paramObjs) {
        this.activity = (ChannelAdvancedActivity) paramObjs[0];
        this.listOpt = (List) paramObjs[1];
        LayoutInflater inflater = LayoutInflater.from(this.activity);
        this.lin = (LinearLayout) this.activity.findViewById(R.id.channel_advance_linnearlayout);
        LinearLayout layout1 = (LinearLayout) inflater.inflate(R.layout.activity_service_set, null).findViewById(R.id.serviceSetlayout);
        this.lin.removeAllViews();
        this.lin.addView(layout1);
        initView();
        setClickListener();
    }

    private void initView() {
        int i;
        this.fenxuanqi_ServiceLayout = (LinearLayout) this.lin.findViewById(R.id.fenxuanqi_ServiceLayout);
        this.startfenxuanqi_Btn = (Button) this.lin.findViewById(R.id.startfenxuanqi_Btn);
        this.stopfenxuanqi_Btn = (Button) this.lin.findViewById(R.id.stopfenxuanqi_Btn);
        this.fenxuanqi_current_status = (TextView) this.lin.findViewById(R.id.fenxuanqi_current_status);
        this.bottleText_current_status = (TextView) this.lin.findViewById(R.id.bottleText_current_status);
        this.startBottleBtn = (Button) this.lin.findViewById(R.id.startBottleBtn);
        this.stopBottleBtn = (Button) this.lin.findViewById(R.id.stopBottleBtn);
        this.paperText_current_status = (TextView) this.lin.findViewById(R.id.paperText_current_status);
        this.startPaperBtn = (Button) this.lin.findViewById(R.id.startPaperBtn);
        this.stopPaperBtn = (Button) this.lin.findViewById(R.id.stopPaperBtn);
        this.donationText_current_status = (TextView) this.lin.findViewById(R.id.donationText_current_status);
        this.startDonationBtn = (Button) this.lin.findViewById(R.id.startDonationBtn);
        this.stopDonationBtn = (Button) this.lin.findViewById(R.id.stopDonationBtn);
        this.greengardText_current_status = (TextView) this.lin.findViewById(R.id.greengardText_current_status);
        this.startGreengardBtn = (Button) this.lin.findViewById(R.id.startGreengardBtn);
        this.stopGreengardBtn = (Button) this.lin.findViewById(R.id.stopGreengardBtn);
        this.couponText_currnet_status = (TextView) this.lin.findViewById(R.id.couponText_currnet_status);
        this.startCouponBtn = (Button) this.lin.findViewById(R.id.startCouponBtn);
        this.stopCouponBtn = (Button) this.lin.findViewById(R.id.stopCouponBtn);
        this.trafficCardText_current_status = (TextView) this.lin.findViewById(R.id.trafficCardText_current_status);
        this.startTrafficCardBtn = (Button) this.lin.findViewById(R.id.startTrafficCardBtn);
        this.stopTrafficCardBtn = (Button) this.lin.findViewById(R.id.stopTrafficCardBtn);
        this.mobilePhoneText_current_status = (TextView) this.lin.findViewById(R.id.mobilePhoneText_current_status);
        this.startMobilePhoneBtn = (Button) this.lin.findViewById(R.id.startMobilePhoneBtn);
        this.stopMobilePhoneBtn = (Button) this.lin.findViewById(R.id.stopMobilePhoneBtn);
        this.D2CodeText_current_status = (TextView) this.lin.findViewById(R.id.D2CodeText_current_status);
        this.startD2CodeBtn = (Button) this.lin.findViewById(R.id.startD2CodeBtn);
        this.stopD2CodeBtn = (Button) this.lin.findViewById(R.id.stopD2CodeBtn);
        this.CServicesText_current_statusCServices = (TextView) this.lin.findViewById(R.id.CServicesText_current_status);
        this.startConServiceBtn = (Button) this.lin.findViewById(R.id.startCServicesBtn);
        this.stopConServiceBtn = (Button) this.lin.findViewById(R.id.stopCServicesBtn);
        this.Wechat_current_status = (TextView) this.lin.findViewById(R.id.Wechat_current_status);
        this.startWechatBtn = (Button) this.lin.findViewById(R.id.startWechatBtn);
        this.stopWechatBtn = (Button) this.lin.findViewById(R.id.stopWechatBtn);
        this.Alipay_current_status = (TextView) this.lin.findViewById(R.id.alipay_current_status);
        this.startAlipayBtn = (Button) this.lin.findViewById(R.id.startAlipayBtn);
        this.stopAlipayBtn = (Button) this.lin.findViewById(R.id.stopAlipayBtn);
        this.Print_current_status = (TextView) this.lin.findViewById(R.id.Print_current_status);
        this.startPrintBtn = (Button) this.lin.findViewById(R.id.startPrintBtn);
        this.stopPrintBtn = (Button) this.lin.findViewById(R.id.stopPrintBtn);
        this.AcceptBottle_current_status = (TextView) this.lin.findViewById(R.id.accept_bottle_text_current_status);
        this.startAcceptBottleBtn = (Button) this.lin.findViewById(R.id.startAcceptBottleBtn);
        this.stopAcceptBottleBtn = (Button) this.lin.findViewById(R.id.stopAcceptBottleBtn);
        this.AcceptCan_current_status = (TextView) this.lin.findViewById(R.id.accept_can_text_current_status);
        this.startAcceptCanBtn = (Button) this.lin.findViewById(R.id.startAcceptCanBtn);
        this.stopAcceptCanBtn = (Button) this.lin.findViewById(R.id.stopAcceptCanBtn);
        this.bottlelimited_current_status = (TextView) this.lin.findViewById(R.id.bottlelimited_current_status);
        this.startBottlelimitedBtn = (Button) this.lin.findViewById(R.id.startBottlelimitedBtn);
        this.stopBottlelimitedBtn = (Button) this.lin.findViewById(R.id.stopBottlelimitedBtn);
        this.bdj_current_status = (TextView) this.lin.findViewById(R.id.bdj_current_status);
        this.startBDJBtn = (Button) this.lin.findViewById(R.id.startBDJBtn);
        this.stopBDJBtn = (Button) this.lin.findViewById(R.id.stopBDJBtn);
        this.bdjText = (TextView) this.lin.findViewById(R.id.bdjText);
        this.bdjText.setText(R.string.help_home_lv);
        String recycle_set = SysConfig.get("RECYCLE.MATERIAL.SET");
        if (recycle_set != null && recycle_set.split(";").length < 2) {
            this.fenxuanqi_ServiceLayout.setVisibility(View.GONE);
        }
        if ("FALSE".equalsIgnoreCase(SysConfig.get("RECYCLE.STATUS.STOP"))) {
            this.fenxuanqi_current_status.setText(this.startfenxuanqi_Btn.getText() + "");
            this.startfenxuanqi_Btn.setEnabled(false);
            this.startfenxuanqi_Btn.setBackgroundColor(-7829368);
        } else {
            this.fenxuanqi_current_status.setText(this.stopfenxuanqi_Btn.getText() + "");
            this.stopfenxuanqi_Btn.setEnabled(false);
            this.stopfenxuanqi_Btn.setBackgroundColor(-7829368);
        }
        String vendingWay = SysConfig.get("VENDING.WAY");
        String servicCfgDisable = SysConfig.get("SERVICE_CFG_DISABLE");
        List<String> listVendingWay = new ArrayList();
        if (!StringUtils.isBlank(vendingWay)) {
            String[] vendingWayArray = vendingWay.split(";");
            i = 0;
            while (i < vendingWayArray.length) {
                if (servicCfgDisable == null || "".equalsIgnoreCase(servicCfgDisable)) {
                    listVendingWay.add(vendingWayArray[i]);
                } else {
                    String[] servicCfgDisableArray = servicCfgDisable.split(";");
                    int j = 0;
                    while (j < servicCfgDisableArray.length && !servicCfgDisableArray[j].equalsIgnoreCase(vendingWayArray[i])) {
                        j++;
                    }
                    if (j == servicCfgDisableArray.length) {
                        listVendingWay.add(vendingWayArray[i]);
                    }
                }
                i++;
            }
        }
        String recycleServiceSet = SysConfig.get("RECYCLE.SERVICE.SET");
        List<String> listRecycleServiceSet = new ArrayList<>();
        if (!StringUtils.isBlank(recycleServiceSet)) {
            String[] recycleServiceSetArray = recycleServiceSet.split(",");
            for (String add : recycleServiceSetArray) {
                listRecycleServiceSet.add(add);
            }
        }
        String vendingWaySet = SysConfig.get("VENDING.WAY.SET");
        List vendingWaySetList = null;
        if (!StringUtils.isBlank(vendingWaySet)) {
            vendingWaySetList = Arrays.asList(vendingWaySet.split(";"));
        }
        if (!listRecycleServiceSet.contains("BOTTLE")) {
            this.lin.findViewById(R.id.bottleServiceLayout).setVisibility(View.GONE);
        }
        if (!listRecycleServiceSet.contains("PAPER")) {
            this.lin.findViewById(R.id.paperServiceLayout).setVisibility(View.GONE);
        }
        if (!(listVendingWay.contains("DONATION") && vendingWaySetList.contains("DONATION"))) {
            this.lin.findViewById(R.id.donationServiceLayout).setVisibility(View.GONE);
        }
        if (!(listVendingWay.contains("CARD") && vendingWaySetList.contains("CARD"))) {
            this.lin.findViewById(R.id.greengardServiceLayout).setVisibility(View.GONE);
        }
        if (!(listVendingWay.contains("COUPON") && vendingWaySetList.contains("COUPON"))) {
            this.lin.findViewById(R.id.couponServiceLayout).setVisibility(View.GONE);
        }
        if (!(listVendingWay.contains("TRANSPORTCARD") && vendingWaySetList.contains("TRANSPORTCARD"))) {
            this.lin.findViewById(R.id.trafficCardServiceLayout).setVisibility(View.GONE);
        }
        if (!(listVendingWay.contains("PHONE") && vendingWaySetList.contains("PHONE"))) {
            this.lin.findViewById(R.id.mobilePthoneServiceLayout).setVisibility(View.GONE);
        }
        if (!(listVendingWay.contains("QRCODE") && vendingWaySetList.contains("QRCODE"))) {
            this.lin.findViewById(R.id.D2CodeServiceLayout).setVisibility(View.GONE);
        }
        if (!(listVendingWay.contains("WECHAT") && vendingWaySetList.contains("WECHAT"))) {
            this.lin.findViewById(R.id.WechatLayout).setVisibility(View.GONE);
        }
        if (!(listVendingWay.contains("ALIPAY") && vendingWaySetList.contains("ALIPAY"))) {
            this.lin.findViewById(R.id.AlipayLayout).setVisibility(View.GONE);
        }
        if (!(listVendingWay.contains("BDJ") && vendingWaySetList.contains("BDJ"))) {
            this.lin.findViewById(R.id.bdjLayout).setVisibility(View.GONE);
        }
        String printEnable = SysConfig.get("SET.PRINT.ENABLE");
        if (StringUtils.isBlank(printEnable)) {
            this.lin.findViewById(R.id.PrintLayout).setVisibility(View.GONE);
        }
        String str1 = SysConfig.get(MediaInfo.CONVENIENCE_ACTIVITY);
        String str = SysConfig.get("CONVENIENCESERVICE.WAY");
        if (!(!StringUtils.isBlank(str) && str.equalsIgnoreCase("CONSERVICE") && str1.equalsIgnoreCase("TRUE"))) {
            this.lin.findViewById(R.id.ConServiceLayout).setVisibility(View.GONE);
        }
        try {
            List<String> listDisabledService = new ArrayList();
            HashMap<String, Object> hsmpResultServiceDisable = this.guiCommonService.execute("GUIQueryCommonService", "queryServiceDisable", null);
            if (hsmpResultServiceDisable != null) {
                String ServiceDisabled = (String) hsmpResultServiceDisable.get("SERVICE_DISABLED");
                if (!StringUtils.isBlank(ServiceDisabled) && ServiceDisabled.length() > 0) {
                    String[] strDisabledService = ServiceDisabled.split(",");
                    for (String add2 : strDisabledService) {
                        listDisabledService.add(add2);
                    }
                }
            }
            if (listDisabledService.contains("BOTTLE")) {
                this.STOP_RECYCLE_BOTTLE_Sts = maintainOptContent.STOP_RECYCLE_BOTTLE;
                this.stopBottleBtn.setEnabled(false);
                this.stopBottleBtn.setBackgroundColor(-7829368);
                this.bottleText_current_status.setText(this.stopBottleBtn.getText() + "");
            } else {
                this.START_RECYCLE_BOTTLE_Sts = maintainOptContent.START_RECYCLE_BOTTLE;
                this.startBottleBtn.setEnabled(false);
                this.startBottleBtn.setBackgroundColor(-7829368);
                this.bottleText_current_status.setText(this.startBottleBtn.getText() + "");
            }
            if (listDisabledService.contains("PAPER")) {
                this.STOP_RECYCLE_PAPER_Sts = maintainOptContent.STOP_RECYCLE_PAPER;
                this.stopPaperBtn.setEnabled(false);
                this.stopPaperBtn.setBackgroundColor(-7829368);
                this.paperText_current_status.setText(this.stopPaperBtn.getText() + "");
            } else {
                this.START_RECYCLE_PAPER_Sts = maintainOptContent.START_RECYCLE_PAPER;
                this.startPaperBtn.setEnabled(false);
                this.startPaperBtn.setBackgroundColor(-7829368);
                this.paperText_current_status.setText(this.startPaperBtn.getText() + "");
            }
            if (listDisabledService.contains("DONATION")) {
                this.STOP_DONATION_Sts = maintainOptContent.STOP_DONATION;
                this.stopDonationBtn.setEnabled(false);
                this.stopDonationBtn.setBackgroundColor(-7829368);
                this.donationText_current_status.setText(this.stopDonationBtn.getText() + "");
            } else {
                this.START_DONATION_Sts = maintainOptContent.START_DONATION;
                this.startDonationBtn.setEnabled(false);
                this.startDonationBtn.setBackgroundColor(-7829368);
                this.donationText_current_status.setText(this.startDonationBtn.getText() + "");
            }
            if (listDisabledService.contains("CARD")) {
                this.STOP_CARD_Sts = maintainOptContent.STOP_CARD;
                this.stopGreengardBtn.setEnabled(false);
                this.stopGreengardBtn.setBackgroundColor(-7829368);
                this.greengardText_current_status.setText(this.stopGreengardBtn.getText() + "");
            } else {
                this.START_CARD_Sts = maintainOptContent.START_CARD;
                this.startGreengardBtn.setEnabled(false);
                this.startGreengardBtn.setBackgroundColor(-7829368);
                this.greengardText_current_status.setText(this.startGreengardBtn.getText() + "");
            }
            if (listDisabledService.contains("COUPON")) {
                this.STOP_COUPON_Sts = maintainOptContent.STOP_COUPON;
                this.stopCouponBtn.setEnabled(false);
                this.stopCouponBtn.setBackgroundColor(-7829368);
                this.couponText_currnet_status.setText(this.stopCouponBtn.getText() + "");
            } else {
                this.START_COUPON_Sts = maintainOptContent.START_COUPON;
                this.startCouponBtn.setEnabled(false);
                this.startCouponBtn.setBackgroundColor(-7829368);
                this.couponText_currnet_status.setText(this.startCouponBtn.getText() + "");
            }
            if (listDisabledService.contains("TRANSPORTCARD")) {
                this.STOP_ONECARD_Sts = maintainOptContent.STOP_ONECARD;
                this.stopTrafficCardBtn.setEnabled(false);
                this.stopTrafficCardBtn.setBackgroundColor(-7829368);
                this.trafficCardText_current_status.setText(this.stopTrafficCardBtn.getText() + "");
            } else {
                this.START_ONECARD_Sts = maintainOptContent.START_ONECARD;
                this.startTrafficCardBtn.setEnabled(false);
                this.startTrafficCardBtn.setBackgroundColor(-7829368);
                this.trafficCardText_current_status.setText(this.startTrafficCardBtn.getText() + "");
            }
            if (listDisabledService.contains("PHONE")) {
                this.STOP_PHONE_Sts = maintainOptContent.STOP_PHONE;
                this.stopMobilePhoneBtn.setEnabled(false);
                this.stopMobilePhoneBtn.setBackgroundColor(-7829368);
                this.mobilePhoneText_current_status.setText(this.stopMobilePhoneBtn.getText() + "");
            } else {
                this.START_PHONE_Sts = maintainOptContent.START_PHONE;
                this.startMobilePhoneBtn.setEnabled(false);
                this.startMobilePhoneBtn.setBackgroundColor(-7829368);
                this.mobilePhoneText_current_status.setText(this.startMobilePhoneBtn.getText() + "");
            }
            if (listDisabledService.contains("QRCODE")) {
                this.STOP_LNKCARD_Sts = maintainOptContent.STOP_LNKCARD;
                this.stopD2CodeBtn.setEnabled(false);
                this.stopD2CodeBtn.setBackgroundColor(-7829368);
                this.D2CodeText_current_status.setText(this.stopD2CodeBtn.getText() + "");
            } else {
                this.START_LNKCARD_Sts = maintainOptContent.START_LNKCARD;
                this.startD2CodeBtn.setEnabled(false);
                this.startD2CodeBtn.setBackgroundColor(-7829368);
                this.D2CodeText_current_status.setText(this.startD2CodeBtn.getText() + "");
            }
            if (listDisabledService.contains("WECHAT")) {
                this.STOP_WECHAT_Sts = maintainOptContent.STOP_WECHAT;
                this.stopWechatBtn.setEnabled(false);
                this.stopWechatBtn.setBackgroundColor(-7829368);
                this.Wechat_current_status.setText(this.stopWechatBtn.getText() + "");
            } else {
                this.START_WECHAT_Sts = maintainOptContent.START_WECHAT;
                this.startWechatBtn.setEnabled(false);
                this.startWechatBtn.setBackgroundColor(-7829368);
                this.Wechat_current_status.setText(this.startWechatBtn.getText() + "");
            }
            if (listDisabledService.contains("ALIPAY")) {
                this.STOP_ALIPAY_Sts = maintainOptContent.STOP_ALIPAY;
                this.stopAlipayBtn.setEnabled(false);
                this.stopAlipayBtn.setBackgroundColor(-7829368);
                this.Alipay_current_status.setText(this.stopAlipayBtn.getText() + "");
            } else {
                this.START_ALIPAY_Sts = maintainOptContent.START_ALIPAY;
                this.startAlipayBtn.setEnabled(false);
                this.startAlipayBtn.setBackgroundColor(-7829368);
                this.Alipay_current_status.setText(this.startAlipayBtn.getText() + "");
            }
            if (listDisabledService.contains("CONSERVICE")) {
                this.STOP_CONVENIENCE_Sts = maintainOptContent.STOP_CONVENIENCE;
                this.stopConServiceBtn.setEnabled(false);
                this.stopConServiceBtn.setBackgroundColor(-7829368);
                this.CServicesText_current_statusCServices.setText(this.stopConServiceBtn.getText() + "");
            } else {
                this.START_CONVENIENCE_Sts = maintainOptContent.START_CONVENIENCE;
                this.startConServiceBtn.setEnabled(false);
                this.startConServiceBtn.setBackgroundColor(-7829368);
                this.CServicesText_current_statusCServices.setText(this.startConServiceBtn.getText() + "");
            }
            if (listDisabledService.contains(ServiceName.PRINTER) || "FALSE".equalsIgnoreCase(printEnable)) {
                this.STOP_PRINT_Sts = maintainOptContent.STOP_PRINT;
                this.stopPrintBtn.setEnabled(false);
                this.stopPrintBtn.setBackgroundColor(-7829368);
                this.Print_current_status.setText(this.stopPrintBtn.getText() + "");
            } else {
                this.START_PRINT_Sts = maintainOptContent.START_PRINT;
                this.startPrintBtn.setEnabled(false);
                this.startPrintBtn.setBackgroundColor(-7829368);
                this.Print_current_status.setText(this.startPrintBtn.getText() + "");
            }
            String RECYCLE_MATERIAL_SET = SysConfig.get("RECYCLE.MATERIAL.SET") + "";
            if (listDisabledService.contains(ServiceName.PET) || !RECYCLE_MATERIAL_SET.contains(ServiceName.PET)) {
                this.stopAcceptBottleBtn.setEnabled(false);
                this.stopAcceptBottleBtn.setBackgroundColor(-7829368);
                this.AcceptBottle_current_status.setText(this.stopAcceptBottleBtn.getText() + "");
            } else {
                this.startAcceptBottleBtn.setEnabled(false);
                this.startAcceptBottleBtn.setBackgroundColor(-7829368);
                this.AcceptBottle_current_status.setText(this.startAcceptBottleBtn.getText() + "");
            }
            if (listDisabledService.contains(ServiceName.METAL) || !RECYCLE_MATERIAL_SET.contains(ServiceName.METAL)) {
                this.stopAcceptCanBtn.setEnabled(false);
                this.stopAcceptCanBtn.setBackgroundColor(-7829368);
                this.AcceptCan_current_status.setText(this.stopAcceptCanBtn.getText() + "");
            } else {
                this.startAcceptCanBtn.setEnabled(false);
                this.startAcceptCanBtn.setBackgroundColor(-7829368);
                this.AcceptCan_current_status.setText(this.startAcceptCanBtn.getText() + "");
            }
            if (listDisabledService.contains("BOTTLELIMITED")) {
                this.STOP_BOTTLELIMITED_Sts = maintainOptContent.STOP_BOTTLELIMITED;
                this.stopBottlelimitedBtn.setEnabled(false);
                this.stopBottlelimitedBtn.setBackgroundColor(-7829368);
                this.bottlelimited_current_status.setText(this.stopBottlelimitedBtn.getText() + "");
            } else {
                this.START_BOTTLELIMITED_Sts = maintainOptContent.START_BOTTLELIMITED;
                this.startBottlelimitedBtn.setEnabled(false);
                this.startBottlelimitedBtn.setBackgroundColor(-7829368);
                this.bottlelimited_current_status.setText(this.startWechatBtn.getText() + "");
            }
            if (listDisabledService.contains("BDJ")) {
                this.STOP_BDJ_Sts = maintainOptContent.STOP_BDJ;
                this.stopBDJBtn.setEnabled(false);
                this.stopBDJBtn.setBackgroundColor(-7829368);
                this.bdj_current_status.setText(this.stopBDJBtn.getText() + "");
                return;
            }
            this.START_BDJ_Sts = maintainOptContent.START_BDJ;
            this.startBDJBtn.setEnabled(false);
            this.startBDJBtn.setBackgroundColor(-7829368);
            this.bdj_current_status.setText(this.startBDJBtn.getText() + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setClickListener() {
        this.startBottleBtn.setOnClickListener(this);
        this.stopBottleBtn.setOnClickListener(this);
        this.startPaperBtn.setOnClickListener(this);
        this.stopPaperBtn.setOnClickListener(this);
        this.startDonationBtn.setOnClickListener(this);
        this.stopDonationBtn.setOnClickListener(this);
        this.startGreengardBtn.setOnClickListener(this);
        this.stopGreengardBtn.setOnClickListener(this);
        this.startCouponBtn.setOnClickListener(this);
        this.stopCouponBtn.setOnClickListener(this);
        this.startTrafficCardBtn.setOnClickListener(this);
        this.stopTrafficCardBtn.setOnClickListener(this);
        this.startMobilePhoneBtn.setOnClickListener(this);
        this.stopMobilePhoneBtn.setOnClickListener(this);
        this.startD2CodeBtn.setOnClickListener(this);
        this.stopD2CodeBtn.setOnClickListener(this);
        this.startConServiceBtn.setOnClickListener(this);
        this.stopConServiceBtn.setOnClickListener(this);
        this.startWechatBtn.setOnClickListener(this);
        this.stopWechatBtn.setOnClickListener(this);
        this.startAlipayBtn.setOnClickListener(this);
        this.stopAlipayBtn.setOnClickListener(this);
        this.startPrintBtn.setOnClickListener(this);
        this.stopPrintBtn.setOnClickListener(this);
        this.startAcceptBottleBtn.setOnClickListener(this);
        this.stopAcceptBottleBtn.setOnClickListener(this);
        this.startAcceptCanBtn.setOnClickListener(this);
        this.stopAcceptCanBtn.setOnClickListener(this);
        this.startBottlelimitedBtn.setOnClickListener(this);
        this.stopBottlelimitedBtn.setOnClickListener(this);
        this.startBDJBtn.setOnClickListener(this);
        this.stopBDJBtn.setOnClickListener(this);
        this.startfenxuanqi_Btn.setOnClickListener(this);
        this.stopfenxuanqi_Btn.setOnClickListener(this);
    }

    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.startBottleBtn:
                    if (this.STOP_RECYCLE_BOTTLE_Sts.equalsIgnoreCase(maintainOptContent.STOP_RECYCLE_BOTTLE)) {
                        if (this.listOpt.contains(this.STOP_RECYCLE_BOTTLE_Sts)) {
                            this.listOpt.remove(this.STOP_RECYCLE_BOTTLE_Sts);
                        }
                        this.START_RECYCLE_BOTTLE_Sts = maintainOptContent.START_RECYCLE_BOTTLE;
                        this.listOpt.add(this.START_RECYCLE_BOTTLE_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startBottleBtn.setEnabled(false);
                    this.startBottleBtn.setBackgroundColor(-7829368);
                    this.bottleText_current_status.setText(this.startBottleBtn.getText() + "");
                    this.stopBottleBtn.setEnabled(true);
                    this.stopBottleBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", "1");
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopBottleBtn:
                    if (this.START_RECYCLE_BOTTLE_Sts.equalsIgnoreCase(maintainOptContent.START_RECYCLE_BOTTLE)) {
                        if (this.listOpt.contains(this.START_RECYCLE_BOTTLE_Sts)) {
                            this.listOpt.remove(this.START_RECYCLE_BOTTLE_Sts);
                        }
                        this.STOP_RECYCLE_BOTTLE_Sts = maintainOptContent.STOP_RECYCLE_BOTTLE;
                        this.listOpt.add(this.STOP_RECYCLE_BOTTLE_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startBottleBtn.setEnabled(true);
                    this.startBottleBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopBottleBtn.setEnabled(false);
                    this.stopBottleBtn.setBackgroundColor(-7829368);
                    this.bottleText_current_status.setText(this.stopBottleBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", "1");
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.startPaperBtn:
                    if (this.STOP_RECYCLE_PAPER_Sts.equalsIgnoreCase(maintainOptContent.STOP_RECYCLE_PAPER)) {
                        if (this.listOpt.contains(this.STOP_RECYCLE_PAPER_Sts)) {
                            this.listOpt.remove(this.STOP_RECYCLE_PAPER_Sts);
                        }
                        this.START_RECYCLE_PAPER_Sts = maintainOptContent.START_RECYCLE_PAPER;
                        this.listOpt.add(this.START_RECYCLE_PAPER_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startPaperBtn.setEnabled(false);
                    this.startPaperBtn.setBackgroundColor(-7829368);
                    this.paperText_current_status.setText(this.startPaperBtn.getText() + "");
                    this.stopPaperBtn.setEnabled(true);
                    this.stopPaperBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", "2");
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopPaperBtn:
                    if (this.START_RECYCLE_PAPER_Sts.equalsIgnoreCase(maintainOptContent.START_RECYCLE_PAPER)) {
                        if (this.listOpt.contains(this.START_RECYCLE_PAPER_Sts)) {
                            this.listOpt.remove(this.START_RECYCLE_PAPER_Sts);
                        }
                        this.STOP_RECYCLE_PAPER_Sts = maintainOptContent.STOP_RECYCLE_PAPER;
                        this.listOpt.add(this.STOP_RECYCLE_PAPER_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startPaperBtn.setEnabled(true);
                    this.startPaperBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopPaperBtn.setEnabled(false);
                    this.stopPaperBtn.setBackgroundColor(-7829368);
                    this.paperText_current_status.setText(this.stopPaperBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", "2");
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.startfenxuanqi_Btn:
                    Properties externalProp3 = new Properties();
                    externalProp3.put("RECYCLE.STATUS.STOP", "FALSE");
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp3);
                    SysConfig.set(externalProp3);
                    RVMShell.backupExternalConfig();
                    this.fenxuanqi_current_status.setText(this.startfenxuanqi_Btn.getText() + "");
                    this.startfenxuanqi_Btn.setEnabled(false);
                    this.startfenxuanqi_Btn.setBackgroundColor(-7829368);
                    this.stopfenxuanqi_Btn.setEnabled(true);
                    this.stopfenxuanqi_Btn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    return;
                case R.id.stopfenxuanqi_Btn:
                    Properties externalProp2 = new Properties();
                    externalProp2.put("RECYCLE.STATUS.STOP", "TRUE");
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp2);
                    SysConfig.set(externalProp2);
                    RVMShell.backupExternalConfig();
                    this.fenxuanqi_current_status.setText(this.stopfenxuanqi_Btn.getText() + "");
                    this.stopfenxuanqi_Btn.setEnabled(false);
                    this.stopfenxuanqi_Btn.setBackgroundColor(-7829368);
                    this.startfenxuanqi_Btn.setEnabled(true);
                    this.startfenxuanqi_Btn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    return;
                case R.id.startDonationBtn:
                    if (this.STOP_DONATION_Sts.equalsIgnoreCase(maintainOptContent.STOP_DONATION)) {
                        if (this.listOpt.contains(this.STOP_DONATION_Sts)) {
                            this.listOpt.remove(this.STOP_DONATION_Sts);
                        }
                        this.START_DONATION_Sts = maintainOptContent.START_DONATION;
                        this.listOpt.add(this.START_DONATION_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startDonationBtn.setEnabled(false);
                    this.startDonationBtn.setBackgroundColor(-7829368);
                    this.donationText_current_status.setText(this.startDonationBtn.getText() + "");
                    this.stopDonationBtn.setEnabled(true);
                    this.stopDonationBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", CardType.MSG_CONSERVICE);
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopDonationBtn:
                    if (this.START_DONATION_Sts.equalsIgnoreCase(maintainOptContent.START_DONATION)) {
                        if (this.listOpt.contains(this.START_DONATION_Sts)) {
                            this.listOpt.remove(this.START_DONATION_Sts);
                        }
                        this.STOP_DONATION_Sts = maintainOptContent.STOP_DONATION;
                        this.listOpt.add(this.STOP_DONATION_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startDonationBtn.setEnabled(true);
                    this.startDonationBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopDonationBtn.setEnabled(false);
                    this.stopDonationBtn.setBackgroundColor(-7829368);
                    this.donationText_current_status.setText(this.stopDonationBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", CardType.MSG_CONSERVICE);
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.startGreengardBtn:
                    if (this.STOP_CARD_Sts.equalsIgnoreCase(maintainOptContent.STOP_CARD)) {
                        if (this.listOpt.contains(this.STOP_CARD_Sts)) {
                            this.listOpt.remove(this.STOP_CARD_Sts);
                        }
                        this.START_CARD_Sts = maintainOptContent.START_CARD;
                        this.listOpt.add(this.START_CARD_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startGreengardBtn.setEnabled(false);
                    this.startGreengardBtn.setBackgroundColor(-7829368);
                    this.greengardText_current_status.setText(this.startGreengardBtn.getText() + "");
                    this.stopGreengardBtn.setEnabled(true);
                    this.stopGreengardBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", "9");
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopGreengardBtn:
                    if (this.START_CARD_Sts.equalsIgnoreCase(maintainOptContent.START_CARD)) {
                        if (this.listOpt.contains(this.START_CARD_Sts)) {
                            this.listOpt.remove(this.START_CARD_Sts);
                        }
                        this.STOP_CARD_Sts = maintainOptContent.STOP_CARD;
                        this.listOpt.add(this.STOP_CARD_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startGreengardBtn.setEnabled(true);
                    this.startGreengardBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopGreengardBtn.setEnabled(false);
                    this.stopGreengardBtn.setBackgroundColor(-7829368);
                    this.greengardText_current_status.setText(this.stopGreengardBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", "9");
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.startCouponBtn:
                    if (this.STOP_COUPON_Sts.equalsIgnoreCase(maintainOptContent.STOP_COUPON)) {
                        if (this.listOpt.contains(this.STOP_COUPON_Sts)) {
                            this.listOpt.remove(this.STOP_COUPON_Sts);
                        }
                        this.START_COUPON_Sts = maintainOptContent.START_COUPON;
                        this.listOpt.add(this.START_COUPON_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startCouponBtn.setEnabled(false);
                    this.startCouponBtn.setBackgroundColor(-7829368);
                    this.couponText_currnet_status.setText(this.startCouponBtn.getText() + "");
                    this.stopCouponBtn.setEnabled(true);
                    this.stopCouponBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", "3");
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopCouponBtn:
                    if (this.START_COUPON_Sts.equalsIgnoreCase(maintainOptContent.START_COUPON)) {
                        if (this.listOpt.contains(this.START_COUPON_Sts)) {
                            this.listOpt.remove(this.START_COUPON_Sts);
                        }
                        this.STOP_COUPON_Sts = maintainOptContent.STOP_COUPON;
                        this.listOpt.add(this.STOP_COUPON_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startCouponBtn.setEnabled(true);
                    this.startCouponBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopCouponBtn.setEnabled(false);
                    this.stopCouponBtn.setBackgroundColor(-7829368);
                    this.couponText_currnet_status.setText(this.stopCouponBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", "3");
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.startTrafficCardBtn:
                    if (this.STOP_ONECARD_Sts.equalsIgnoreCase(maintainOptContent.STOP_ONECARD)) {
                        if (this.listOpt.contains(this.STOP_ONECARD_Sts)) {
                            this.listOpt.remove(this.STOP_ONECARD_Sts);
                        }
                        this.START_ONECARD_Sts = maintainOptContent.START_ONECARD;
                        this.listOpt.add(this.START_ONECARD_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startTrafficCardBtn.setEnabled(false);
                    this.startTrafficCardBtn.setBackgroundColor(-7829368);
                    this.trafficCardText_current_status.setText(this.startTrafficCardBtn.getText() + "");
                    this.stopTrafficCardBtn.setEnabled(true);
                    this.stopTrafficCardBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", CardType.MSG_PHONE);
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopTrafficCardBtn:
                    if (this.START_ONECARD_Sts.equalsIgnoreCase(maintainOptContent.START_ONECARD)) {
                        if (this.listOpt.contains(this.START_ONECARD_Sts)) {
                            this.listOpt.remove(this.START_ONECARD_Sts);
                        }
                        this.STOP_ONECARD_Sts = maintainOptContent.STOP_ONECARD;
                        this.listOpt.add(this.STOP_ONECARD_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startTrafficCardBtn.setEnabled(true);
                    this.startTrafficCardBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopTrafficCardBtn.setEnabled(false);
                    this.stopTrafficCardBtn.setBackgroundColor(-7829368);
                    this.trafficCardText_current_status.setText(this.stopTrafficCardBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", CardType.MSG_PHONE);
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.startMobilePhoneBtn:
                    if (this.STOP_PHONE_Sts.equalsIgnoreCase(maintainOptContent.STOP_PHONE)) {
                        if (this.listOpt.contains(this.STOP_PHONE_Sts)) {
                            this.listOpt.remove(this.STOP_PHONE_Sts);
                        }
                        this.START_PHONE_Sts = maintainOptContent.START_PHONE;
                        this.listOpt.add(this.START_PHONE_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startMobilePhoneBtn.setEnabled(false);
                    this.startMobilePhoneBtn.setBackgroundColor(-7829368);
                    this.mobilePhoneText_current_status.setText(this.startMobilePhoneBtn.getText() + "");
                    this.stopMobilePhoneBtn.setEnabled(true);
                    this.stopMobilePhoneBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", CardType.MSG_WECHAT);
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopMobilePhoneBtn:
                    if (this.START_PHONE_Sts.equalsIgnoreCase(maintainOptContent.START_PHONE)) {
                        if (this.listOpt.contains(this.START_PHONE_Sts)) {
                            this.listOpt.remove(this.START_PHONE_Sts);
                        }
                        this.STOP_PHONE_Sts = maintainOptContent.STOP_PHONE;
                        this.listOpt.add(this.STOP_PHONE_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startMobilePhoneBtn.setEnabled(true);
                    this.startMobilePhoneBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopMobilePhoneBtn.setEnabled(false);
                    this.stopMobilePhoneBtn.setBackgroundColor(-7829368);
                    this.mobilePhoneText_current_status.setText(this.stopMobilePhoneBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", CardType.MSG_WECHAT);
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.startD2CodeBtn:
                    if (this.STOP_LNKCARD_Sts.equalsIgnoreCase(maintainOptContent.STOP_LNKCARD)) {
                        if (this.listOpt.contains(this.STOP_LNKCARD_Sts)) {
                            this.listOpt.remove(this.STOP_LNKCARD_Sts);
                        }
                        this.START_LNKCARD_Sts = maintainOptContent.START_LNKCARD;
                        this.listOpt.add(this.START_LNKCARD_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startD2CodeBtn.setEnabled(false);
                    this.startD2CodeBtn.setBackgroundColor(-7829368);
                    this.D2CodeText_current_status.setText(this.startD2CodeBtn.getText() + "");
                    this.stopD2CodeBtn.setEnabled(true);
                    this.stopD2CodeBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", CardType.MSG_SQRCODE);
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopD2CodeBtn:
                    if (this.START_LNKCARD_Sts.equalsIgnoreCase(maintainOptContent.START_LNKCARD)) {
                        if (this.listOpt.contains(this.START_LNKCARD_Sts)) {
                            this.listOpt.remove(this.START_LNKCARD_Sts);
                        }
                        this.STOP_LNKCARD_Sts = maintainOptContent.STOP_LNKCARD;
                        this.listOpt.add(this.STOP_LNKCARD_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startD2CodeBtn.setEnabled(true);
                    this.startD2CodeBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopD2CodeBtn.setEnabled(false);
                    this.stopD2CodeBtn.setBackgroundColor(-7829368);
                    this.D2CodeText_current_status.setText(this.stopD2CodeBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", CardType.MSG_SQRCODE);
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.startWechatBtn:
                    if (this.STOP_WECHAT_Sts.equalsIgnoreCase(maintainOptContent.STOP_WECHAT)) {
                        if (this.listOpt.contains(this.STOP_WECHAT_Sts)) {
                            this.listOpt.remove(this.STOP_WECHAT_Sts);
                        }
                        this.START_WECHAT_Sts = maintainOptContent.START_WECHAT;
                        this.listOpt.add(this.START_WECHAT_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startWechatBtn.setEnabled(false);
                    this.startWechatBtn.setBackgroundColor(-7829368);
                    this.Wechat_current_status.setText(this.startWechatBtn.getText() + "");
                    this.stopWechatBtn.setEnabled(true);
                    this.stopWechatBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", "14");
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopWechatBtn:
                    if (this.START_WECHAT_Sts.equalsIgnoreCase(maintainOptContent.START_WECHAT)) {
                        if (this.listOpt.contains(this.START_WECHAT_Sts)) {
                            this.listOpt.remove(this.START_WECHAT_Sts);
                        }
                        this.STOP_WECHAT_Sts = maintainOptContent.STOP_WECHAT;
                        this.listOpt.add(this.STOP_WECHAT_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startWechatBtn.setEnabled(true);
                    this.startWechatBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopWechatBtn.setEnabled(false);
                    this.stopWechatBtn.setBackgroundColor(-7829368);
                    this.Wechat_current_status.setText(this.stopWechatBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", "14");
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.startAlipayBtn:
                    if (this.STOP_ALIPAY_Sts.equalsIgnoreCase(maintainOptContent.STOP_ALIPAY)) {
                        if (this.listOpt.contains(this.STOP_ALIPAY_Sts)) {
                            this.listOpt.remove(this.STOP_ALIPAY_Sts);
                        }
                        this.START_ALIPAY_Sts = maintainOptContent.START_ALIPAY;
                        this.listOpt.add(this.START_ALIPAY_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startAlipayBtn.setEnabled(false);
                    this.startAlipayBtn.setBackgroundColor(-7829368);
                    this.Alipay_current_status.setText(this.startAlipayBtn.getText() + "");
                    this.stopAlipayBtn.setEnabled(true);
                    this.stopAlipayBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", "15");
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopAlipayBtn:
                    if (this.START_WECHAT_Sts.equalsIgnoreCase(maintainOptContent.START_WECHAT)) {
                        if (this.listOpt.contains(this.START_ALIPAY_Sts)) {
                            this.listOpt.remove(this.START_ALIPAY_Sts);
                        }
                        this.STOP_ALIPAY_Sts = maintainOptContent.STOP_ALIPAY;
                        this.listOpt.add(this.STOP_ALIPAY_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startAlipayBtn.setEnabled(true);
                    this.startAlipayBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopAlipayBtn.setEnabled(false);
                    this.stopAlipayBtn.setBackgroundColor(-7829368);
                    this.Alipay_current_status.setText(this.stopAlipayBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", "15");
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.startBDJBtn:
                    if (this.STOP_BDJ_Sts.equalsIgnoreCase(maintainOptContent.STOP_BDJ)) {
                        if (this.listOpt.contains(this.STOP_BDJ_Sts)) {
                            this.listOpt.remove(this.STOP_BDJ_Sts);
                        }
                        this.START_BDJ_Sts = maintainOptContent.START_BDJ;
                        this.listOpt.add(this.START_BDJ_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startBDJBtn.setEnabled(false);
                    this.startBDJBtn.setBackgroundColor(-7829368);
                    this.bdj_current_status.setText(this.startBDJBtn.getText() + "");
                    this.stopBDJBtn.setEnabled(true);
                    this.stopBDJBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", CardType.MSG_BDJ);
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopBDJBtn:
                    if (this.START_BDJ_Sts.equalsIgnoreCase(maintainOptContent.START_BDJ)) {
                        if (this.listOpt.contains(this.START_BDJ_Sts)) {
                            this.listOpt.remove(this.START_BDJ_Sts);
                        }
                        this.STOP_BDJ_Sts = maintainOptContent.STOP_BDJ;
                        this.listOpt.add(this.STOP_BDJ_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startBDJBtn.setEnabled(true);
                    this.startBDJBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopBDJBtn.setEnabled(false);
                    this.stopBDJBtn.setBackgroundColor(-7829368);
                    this.bdj_current_status.setText(this.stopBDJBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", CardType.MSG_BDJ);
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.startCServicesBtn:
                    if (this.STOP_CONVENIENCE_Sts.equalsIgnoreCase(maintainOptContent.STOP_CONVENIENCE)) {
                        if (this.listOpt.contains(this.STOP_CONVENIENCE_Sts)) {
                            this.listOpt.remove(this.STOP_CONVENIENCE_Sts);
                        }
                        this.START_CONVENIENCE_Sts = maintainOptContent.START_CONVENIENCE;
                        this.listOpt.add(this.START_CONVENIENCE_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startConServiceBtn.setEnabled(false);
                    this.startConServiceBtn.setBackgroundColor(-7829368);
                    this.CServicesText_current_statusCServices.setText(this.startConServiceBtn.getText() + "");
                    this.stopConServiceBtn.setEnabled(true);
                    this.stopConServiceBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", "12");
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopCServicesBtn:
                    if (this.START_CONVENIENCE_Sts.equalsIgnoreCase(maintainOptContent.START_CONVENIENCE)) {
                        if (this.listOpt.contains(this.START_CONVENIENCE_Sts)) {
                            this.listOpt.remove(this.START_CONVENIENCE_Sts);
                        }
                        this.STOP_CONVENIENCE_Sts = maintainOptContent.STOP_CONVENIENCE;
                        this.listOpt.add(this.STOP_CONVENIENCE_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startConServiceBtn.setEnabled(true);
                    this.startConServiceBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopConServiceBtn.setEnabled(false);
                    this.stopConServiceBtn.setBackgroundColor(-7829368);
                    this.CServicesText_current_statusCServices.setText(this.stopConServiceBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", "12");
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.startPrintBtn:
                    if (this.STOP_PRINT_Sts.equalsIgnoreCase(maintainOptContent.STOP_PRINT)) {
                        if (this.listOpt.contains(this.STOP_PRINT_Sts)) {
                            this.listOpt.remove(this.STOP_PRINT_Sts);
                        }
                        this.START_PRINT_Sts = maintainOptContent.START_PRINT;
                        this.listOpt.add(this.START_PRINT_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startPrintBtn.setEnabled(false);
                    this.startPrintBtn.setBackgroundColor(-7829368);
                    this.Print_current_status.setText(this.startPrintBtn.getText() + "");
                    this.stopPrintBtn.setEnabled(true);
                    this.stopPrintBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", "18");
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    Properties externalProp = new Properties();
                    externalProp.put("SET.PRINT.ENABLE", "TRUE");
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                    SysConfig.set(externalProp);
                    RVMShell.backupExternalConfig();
                    return;
                case R.id.stopPrintBtn:
                    if (this.START_PRINT_Sts.equalsIgnoreCase(maintainOptContent.START_PRINT)) {
                        if (this.listOpt.contains(this.START_PRINT_Sts)) {
                            this.listOpt.remove(this.START_PRINT_Sts);
                        }
                        this.STOP_PRINT_Sts = maintainOptContent.STOP_PRINT;
                        this.listOpt.add(this.STOP_PRINT_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startPrintBtn.setEnabled(true);
                    this.startPrintBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopPrintBtn.setEnabled(false);
                    this.stopPrintBtn.setBackgroundColor(-7829368);
                    this.Print_current_status.setText(this.stopConServiceBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", "18");
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    Properties externalProp1 = new Properties();
                    externalProp1.put("SET.PRINT.ENABLE", "FALSE");
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp1);
                    SysConfig.set(externalProp1);
                    RVMShell.backupExternalConfig();
                    return;
                case R.id.startAcceptBottleBtn:
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startAcceptBottleBtn.setEnabled(false);
                    this.startAcceptBottleBtn.setBackgroundColor(-7829368);
                    this.AcceptBottle_current_status.setText(this.startAcceptBottleBtn.getText() + "");
                    this.stopAcceptBottleBtn.setEnabled(true);
                    this.stopAcceptBottleBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", "19");
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    String RECYCLE_MATERIAL_SET = SysConfig.get("RECYCLE.MATERIAL.SET");
                    if (StringUtils.isBlank(RECYCLE_MATERIAL_SET)) {
                        RECYCLE_MATERIAL_SET = ServiceName.PET;
                    } else if (!RECYCLE_MATERIAL_SET.contains(ServiceName.PET)) {
                        RECYCLE_MATERIAL_SET = RECYCLE_MATERIAL_SET + ";" + ServiceName.PET;
                    }
                    Properties externalPropMaterialSet = new Properties();
                    externalPropMaterialSet.put("RECYCLE.MATERIAL.SET", RECYCLE_MATERIAL_SET);
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalPropMaterialSet);
                    SysConfig.set(externalPropMaterialSet);
                    RVMShell.backupExternalConfig();
                    return;
                case R.id.stopAcceptBottleBtn:
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startAcceptBottleBtn.setEnabled(true);
                    this.startAcceptBottleBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopAcceptBottleBtn.setEnabled(false);
                    this.stopAcceptBottleBtn.setBackgroundColor(-7829368);
                    this.AcceptBottle_current_status.setText(this.stopAcceptBottleBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", "19");
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    String RECYCLE_MATERIAL_SET2 = SysConfig.get("RECYCLE.MATERIAL.SET");
                    if (!StringUtils.isBlank(RECYCLE_MATERIAL_SET2) && RECYCLE_MATERIAL_SET2.contains(ServiceName.PET)) {
                        RECYCLE_MATERIAL_SET2 = StringUtils.replace(RECYCLE_MATERIAL_SET2, ServiceName.PET, "");
                    }
                    Properties externalPropMaterialSet2 = new Properties();
                    externalPropMaterialSet2.put("RECYCLE.MATERIAL.SET", RECYCLE_MATERIAL_SET2);
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalPropMaterialSet2);
                    SysConfig.set(externalPropMaterialSet2);
                    RVMShell.backupExternalConfig();
                    return;
                case R.id.startAcceptCanBtn:
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startAcceptCanBtn.setEnabled(false);
                    this.startAcceptCanBtn.setBackgroundColor(-7829368);
                    this.AcceptCan_current_status.setText(this.startAcceptCanBtn.getText() + "");
                    this.stopAcceptCanBtn.setEnabled(true);
                    this.stopAcceptCanBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", "20");
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    String RECYCLE_MATERIAL_SET3 = SysConfig.get("RECYCLE.MATERIAL.SET");
                    if (StringUtils.isBlank(RECYCLE_MATERIAL_SET3)) {
                        RECYCLE_MATERIAL_SET3 = ServiceName.METAL;
                    } else if (!RECYCLE_MATERIAL_SET3.contains(ServiceName.METAL)) {
                        RECYCLE_MATERIAL_SET3 = RECYCLE_MATERIAL_SET3 + ";" + ServiceName.METAL;
                    }
                    Properties externalPropMaterialSet3 = new Properties();
                    externalPropMaterialSet3.put("RECYCLE.MATERIAL.SET", RECYCLE_MATERIAL_SET3);
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalPropMaterialSet3);
                    SysConfig.set(externalPropMaterialSet3);
                    RVMShell.backupExternalConfig();
                    return;
                case R.id.stopAcceptCanBtn:
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startAcceptCanBtn.setEnabled(true);
                    this.startAcceptCanBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopAcceptCanBtn.setEnabled(false);
                    this.stopAcceptCanBtn.setBackgroundColor(-7829368);
                    this.AcceptCan_current_status.setText(this.stopAcceptCanBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", "20");
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    String RECYCLE_MATERIAL_SET4 = SysConfig.get("RECYCLE.MATERIAL.SET");
                    if (!StringUtils.isBlank(RECYCLE_MATERIAL_SET4) && RECYCLE_MATERIAL_SET4.contains(ServiceName.METAL)) {
                        RECYCLE_MATERIAL_SET4 = StringUtils.replace(RECYCLE_MATERIAL_SET4, ServiceName.METAL, "");
                    }
                    Properties externalPropMaterialSet4 = new Properties();
                    externalPropMaterialSet4.put("RECYCLE.MATERIAL.SET", RECYCLE_MATERIAL_SET4);
                    PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalPropMaterialSet4);
                    SysConfig.set(externalPropMaterialSet4);
                    RVMShell.backupExternalConfig();
                    return;
                case R.id.startBottlelimitedBtn:
                    if (this.STOP_BOTTLELIMITED_Sts.equalsIgnoreCase(maintainOptContent.STOP_BOTTLELIMITED)) {
                        if (this.listOpt.contains(this.STOP_BOTTLELIMITED_Sts)) {
                            this.listOpt.remove(this.STOP_BOTTLELIMITED_Sts);
                        }
                        this.START_BOTTLELIMITED_Sts = maintainOptContent.START_BOTTLELIMITED;
                        this.listOpt.add(this.START_BOTTLELIMITED_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startBottlelimitedBtn.setEnabled(false);
                    this.startBottlelimitedBtn.setBackgroundColor(-7829368);
                    this.bottlelimited_current_status.setText(this.startBottlelimitedBtn.getText() + "");
                    this.stopBottlelimitedBtn.setEnabled(true);
                    this.stopBottlelimitedBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.hsmpParam.put("OPT_TYPE", "21");
                    this.hsmpParam.put("RUN_OPT", "1");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                case R.id.stopBottlelimitedBtn:
                    if (this.START_BOTTLELIMITED_Sts.equalsIgnoreCase(maintainOptContent.START_BOTTLELIMITED)) {
                        if (this.listOpt.contains(this.START_BOTTLELIMITED_Sts)) {
                            this.listOpt.remove(this.START_BOTTLELIMITED_Sts);
                        }
                        this.STOP_BOTTLELIMITED_Sts = maintainOptContent.STOP_BOTTLELIMITED;
                        this.listOpt.add(this.STOP_BOTTLELIMITED_Sts);
                    }
                    TimeoutTask.getTimeoutTask().reset(this.activity.getTimeoutAction());
                    this.startBottlelimitedBtn.setEnabled(true);
                    this.startBottlelimitedBtn.setBackgroundResource(R.drawable.tongdaoweihu_gaoji_btn_bg);
                    this.stopBottlelimitedBtn.setEnabled(false);
                    this.stopBottlelimitedBtn.setBackgroundColor(-7829368);
                    this.bottlelimited_current_status.setText(this.stopBottlelimitedBtn.getText() + "");
                    this.hsmpParam.put("OPT_TYPE", "21");
                    this.hsmpParam.put("RUN_OPT", "0");
                    this.guiCommonService.execute("GUIMaintenanceCommonService", "startOrStopService", this.hsmpParam);
                    return;
                default:
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
