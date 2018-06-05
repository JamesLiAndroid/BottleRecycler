package com.incomrecycle.prms.rvm.gui.activity.starput;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.action.GUIActionGotoServiceProcess;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class GreenCardActivity extends BaseActivity {
    String PRODUCT_TYPE = ((String) ServiceGlobal.getCurrentSession("PRODUCT_TYPE"));
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) GreenCardActivity.this.findViewById(R.id.green_card_time)).setText("" + remainedSeconds);
                    } else if ("PAPER".equals(GreenCardActivity.this.PRODUCT_TYPE)) {
                        Intent intent = new Intent(GreenCardActivity.this, SelectRecycleActivity.class);
                        intent.putExtra("RECYCLE", "RECYCLEPAPER");
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        GreenCardActivity.this.startActivity(intent);
                        GreenCardActivity.this.finish();
                    } else {
                        Intent intent = new Intent(GreenCardActivity.this, SelectRecycleActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        GreenCardActivity.this.startActivity(intent);
                        GreenCardActivity.this.finish();
                    }
                }
            };
            GreenCardActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    public void onStart() {
        super.onStart();
        EditText et = (EditText) findViewById(R.id.cardText);
        if (Boolean.parseBoolean(SysConfig.get("IS_HIDE_SOFTKEYBOARD"))) {
            et.setInputType(0);
        }
        et.setText("");
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.INPUTCARDNUM")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public boolean onTouchEvent(MotionEvent event) {
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        return super.onTouchEvent(event);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.activity_green_card);
        backgroundToActivity();
        showOrHideKeybordAndResetTime((EditText) findViewById(R.id.cardText), this.timeoutAction);
        ((Button) findViewById(R.id.green_card_confirm)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.REBATE_GREENCARD_CONFIRM);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                TimeoutTask.getTimeoutTask().reset(GreenCardActivity.this.timeoutAction);
                String cardNumber = ((EditText) GreenCardActivity.this.findViewById(R.id.cardText)).getText().toString().trim();
                if (StringUtils.isBlank(cardNumber)) {
                    ((TextView) GreenCardActivity.this.findViewById(R.id.cardWarn)).setText(R.string.noCardNum);
                    return;
                }
                HashMap<String, Object> hsmpParam = new HashMap();
                hsmpParam.put("CARD_NO", cardNumber);
                hsmpParam.put("CARD_TYPE", "CARD");
                if (GreenCardActivity.this.checkCardNum(hsmpParam)) {
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
                            HashMap<String, Object> VENDING_FLAG = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
                            Intent intent;
                            if (TRANSMIT_ADV != null) {
                                HashMap<String, String> HOMEPAGE_LEFT = (HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV");
                                if (HOMEPAGE_LEFT == null || StringUtils.isBlank((String) HOMEPAGE_LEFT.get(AllAdvertisement.VENDING_PIC))) {
                                    GreenCardActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{GreenCardActivity.this.getBaseActivity(), Integer.valueOf(2), "CARD"});
                                    GreenCardActivity.this.finish();
                                    return;
                                }
                                intent = new Intent(GreenCardActivity.this, ActivityAdActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                GreenCardActivity.this.startActivity(intent);
                                GreenCardActivity.this.finish();
                            } else if (VENDING_FLAG == null || StringUtils.isBlank((String) VENDING_FLAG.get(AllAdvertisement.VENDING_PIC))) {
                                GreenCardActivity.this.executeGUIAction(true, new GUIActionGotoServiceProcess(), new Object[]{GreenCardActivity.this.getBaseActivity(), Integer.valueOf(2), "CARD"});
                                GreenCardActivity.this.finish();
                            } else {
                                intent = new Intent(GreenCardActivity.this, ActivityAdActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                GreenCardActivity.this.startActivity(intent);
                                GreenCardActivity.this.finish();
                            }
                        }
                    });
                }
            }
        });
        ((Button) findViewById(R.id.green_card_return_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.REBATE_GREENCARD_RETURN);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                if ("PAPER".equals(GreenCardActivity.this.PRODUCT_TYPE)) {
                    Intent intent = new Intent(GreenCardActivity.this, SelectRecycleActivity.class);
                    intent.putExtra("RECYCLE", "RECYCLEPAPER");
                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    GreenCardActivity.this.startActivity(intent);
                    GreenCardActivity.this.finish();
                    return;
                }
               Intent intent = new Intent(GreenCardActivity.this, SelectRecycleActivity.class);
                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                GreenCardActivity.this.startActivity(intent);
                GreenCardActivity.this.finish();
            }
        });
    }

    public boolean checkCardNum(HashMap<String, Object> hsmpParam) {
        try {
            HashMap<String, Object> hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIGreenCardCommonService", "verifyCardNo", hsmpParam);
            if (hsmpResult == null) {
                return false;
            }
            String RET_CODE = (String) hsmpResult.get("RET_CODE");
            if ("error".equalsIgnoreCase(RET_CODE)) {
                ((TextView) findViewById(R.id.cardWarn)).setText(R.string.wrongCardNum);
                return false;
            } else if ("success".equalsIgnoreCase(RET_CODE)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
        HashMap<String, Object> hsmpParam;
        String EVENT = (String) hsmpEvent.get("EVENT");
        if ("MAGNETIC_CARD_NUM".equalsIgnoreCase(EVENT)) {
            String MAGNETIC_CARD_NUM = (String) hsmpEvent.get("MAGNETIC_CARD_NUM");
            hsmpParam = new HashMap();
            hsmpParam.put("CARD_NO", MAGNETIC_CARD_NUM);
            hsmpParam.put("CARD_TYPE", "CARD");
            if (checkCardNum(hsmpParam)) {
                ((EditText) findViewById(R.id.cardText)).setText(MAGNETIC_CARD_NUM);
            }
        }
        if ("HUILIFE_CARD_NUM".equalsIgnoreCase(EVENT)) {
            String HUILIFE_CARD_NUM = (String) hsmpEvent.get("HUILIFE_CARD_NUM");
            hsmpParam = new HashMap();
            hsmpParam.put("CARD_NO", HUILIFE_CARD_NUM);
            hsmpParam.put("CARD_TYPE", "CARD");
            if (checkCardNum(hsmpParam)) {
                ((EditText) findViewById(R.id.cardText)).setText(HUILIFE_CARD_NUM);
            }
        }
    }
}
