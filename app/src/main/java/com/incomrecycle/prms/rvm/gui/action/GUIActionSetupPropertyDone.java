package com.incomrecycle.prms.rvm.gui.action;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.activity.channel.ChannelAdvancedActivity;
import java.util.HashMap;

public class GUIActionSetupPropertyDone extends GUIAction {
    private CheckBox cbAlipay;
    private CheckBox cbBDJ;
    private CheckBox cbCoupon;
    private CheckBox cbD2Code;
    private CheckBox cbDonation;
    private CheckBox cbGreenCard;
    private CheckBox cbMobilePhone;
    private CheckBox cbTrafficCard;
    private CheckBox cbWechat;
    private HashMap<String, Object> hsmpResult;
    private HashMap<String, Object> hsmpResult1;
    private Logger logger = LoggerFactory.getLogger("VIEW");

    protected void doAction(Object[] paramObjs) {
        ChannelAdvancedActivity activity = (ChannelAdvancedActivity) paramObjs[0];
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        EditText setupCfgTimerNumEdit = (EditText) activity.findViewById(R.id.setupCfgTimerNumEdit);
        EditText setupCfgRccIpEdit = (EditText) activity.findViewById(R.id.setupCfgRccIpEdit);
        EditText setupCfgRccPortEdit = (EditText) activity.findViewById(R.id.setupCfgRccPortEdit);
        EditText importPhoneNumber = (EditText) activity.findViewById(R.id.importPhoneNumber);
        this.cbDonation = (CheckBox) activity.findViewById(R.id.cbDonation);
        this.cbGreenCard = (CheckBox) activity.findViewById(R.id.cbGreenCard);
        this.cbCoupon = (CheckBox) activity.findViewById(R.id.cbCoupon);
        this.cbTrafficCard = (CheckBox) activity.findViewById(R.id.cbTrafficCard);
        this.cbMobilePhone = (CheckBox) activity.findViewById(R.id.cbMobilePhone);
        this.cbD2Code = (CheckBox) activity.findViewById(R.id.cbD2Code);
        this.cbWechat = (CheckBox) activity.findViewById(R.id.cbWechat);
        this.cbAlipay = (CheckBox) activity.findViewById(R.id.cbAlipay);
        this.cbBDJ = (CheckBox) activity.findViewById(R.id.cbBDJ);
        HashMap<String, Object> hsmpParam = new HashMap();
        String timerNo = setupCfgTimerNumEdit.getText().toString();
        if (StringUtils.isBlank(timerNo)) {
            timerNo = SysConfig.get("RVM.CODE");
        }
        hsmpParam.put("TIMER_NO", timerNo);
        String rccIP = setupCfgRccIpEdit.getText().toString();
        if (StringUtils.isBlank(rccIP)) {
            rccIP = SysConfig.get("RCC.IP");
        }
        hsmpParam.put("RCC_IP", rccIP);
        String rccPort = setupCfgRccPortEdit.getText().toString();
        if (StringUtils.isBlank(rccPort)) {
            rccPort = SysConfig.get("RCC.PORT");
        }
        hsmpParam.put("RCC_PORT", rccPort);
        hsmpParam.put(AllAdvertisement.VENDING_WAY, savecbChecked());
        String PhoneNumber = importPhoneNumber.getText().toString();
        if (StringUtils.isBlank(PhoneNumber)) {
            PhoneNumber = "0";
        }
        hsmpParam.put("PHONE_NUMBER", PhoneNumber);
        try {
            this.hsmpResult = guiCommonService.execute("GUIMaintenanceCommonService", "saveConfig", hsmpParam);
            this.logger.debug(this.hsmpResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HashMap<String, Object> hsmp = new HashMap();
        hsmp.put("PHOMENUM", PhoneNumber);
        try {
            this.hsmpResult1 = guiCommonService.execute("GUIMaintenanceCommonService", "verifyPhone", hsmp);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        Boolean reSult = Boolean.valueOf(false);
        if (this.hsmpResult != null) {
            reSult = (Boolean) this.hsmpResult.get("RESULT");
        }
        if (reSult.booleanValue()) {
            Toast.makeText(activity, R.string.setupCfgDoneSuccess, 0).show();
        } else {
            Toast.makeText(activity, R.string.setupCfgDoneFail, 0).show();
        }
    }

    private String savecbChecked() {
        StringBuilder sb = new StringBuilder();
        if (this.cbDonation.isChecked()) {
            sb.append("DONATION;");
        }
        if (this.cbGreenCard.isChecked()) {
            sb.append("CARD;");
        }
        if (this.cbCoupon.isChecked()) {
            sb.append("COUPON;");
        }
        if (this.cbTrafficCard.isChecked()) {
            sb.append("TRANSPORTCARD;");
        }
        if (this.cbMobilePhone.isChecked()) {
            sb.append("PHONE;");
        }
        if (this.cbD2Code.isChecked()) {
            sb.append("QRCODE;");
        }
        if (this.cbWechat.isChecked()) {
            sb.append("WECHAT;");
        }
        if (this.cbAlipay.isChecked()) {
            sb.append("ALIPAY;");
        }
        if (this.cbBDJ.isChecked()) {
            sb.append("BDJ;");
        }
        if (sb.toString().endsWith(";")) {
            if (sb.toString().length() > 1) {
                return sb.substring(0, sb.length() - 1);
            }
        } else if (sb != null) {
            return sb.toString();
        }
        return "";
    }
}
