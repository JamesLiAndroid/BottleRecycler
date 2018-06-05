package com.incomrecycle.prms.rvm.gui.activity.convenienceservices;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.task.TimeoutAction;
import com.incomrecycle.common.task.TimeoutTask;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.common.SysDef.AllClickContent;
import com.incomrecycle.prms.rvm.gui.BaseActivity;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper.GUICommonService;
import com.incomrecycle.prms.rvm.gui.GUIAction;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.gui.entity.CardEntity;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardCommEntity.ErrorCode;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import it.sauronsoftware.ftp4j.FTPCodes;

import static android.content.Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class OneCardRechargeHintActivity extends BaseActivity {
    private TextView INCOMBalance = null;
    private View INCOMBalenceLayout = null;
    private String OrderMoney = null;
    private String OrderNo = null;
    private int bdj_ggk_money = 0;
    private Button btnFastVolume = null;
    private String cardNo = null;
    private boolean chargeClick = false;
    private DecimalFormat df = new DecimalFormat("0.00");
    private GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
    private HashMap<String, Object> hashMapQueryOneCardBalance = null;
    private HashMap hsmp = null;
    private LayoutInflater infalter = null;
    private boolean isClick = false;
    private String isblack = "goback";
    private View layout = null;
    private LinearLayout lin = null;
    private TextView moneyInsideCard = null;
    private View moneyInsideCardLayout = null;
    private TextView moneyRecharge = null;
    private View moneyRechargeLayout = null;
    private TimeoutAction timeoutAction = new TimeoutAction() {
        public void apply(int forwardSeconds, int remainedSeconds) {
            GUIAction guiAction = new GUIAction() {
                protected void doAction(Object[] paramObjs) {
                    int remainedSeconds = ((Integer) paramObjs[1]).intValue();
                    if (remainedSeconds != 0) {
                        ((TextView) OneCardRechargeHintActivity.this.findViewById(R.id.onecard_query_main_time)).setText("" + remainedSeconds);
                    } else if (!StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                        try {
                            Intent intent = new Intent(OneCardRechargeHintActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                            intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                            OneCardRechargeHintActivity.this.startActivity(intent);
                            OneCardRechargeHintActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            OneCardRechargeHintActivity.this.executeGUIAction(false, guiAction, new Object[]{Integer.valueOf(forwardSeconds), Integer.valueOf(remainedSeconds)});
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1);
        setContentView(R.layout.onecardchongzhi);
        backgroundToActivity();
        this.moneyRecharge = (TextView) findViewById(R.id.money_recharge);
        this.moneyInsideCard = (TextView) findViewById(R.id.money_insidecard);
        this.moneyRechargeLayout = findViewById(R.id.money_recharge_layout);
        this.moneyInsideCardLayout = findViewById(R.id.money_insidecard_layout);
        this.INCOMBalance = (TextView) findViewById(R.id.INCOM_balence);
        this.INCOMBalenceLayout = findViewById(R.id.INCOM_balence_layout);
        setClickListener();
    }

    protected void onStart() {
        super.onStart();
        this.isClick = false;
        this.chargeClick = false;
        try {
            if (CardEntity.isValid) {
                this.moneyRecharge.setText("" + this.df.format(CardEntity.RECHARGED / 100.0d));
                SysGlobal.execute(new Runnable() {
                    public void run() {
                        try {
                            HashMap<String, Object> GGkmap = (HashMap) OneCardRechargeHintActivity.this.guiCommonService.execute("GUITrafficCardCommonService", "QueryGGK", null).get("GGK_MAP");
                            if (GGkmap == null || GGkmap.size() <= 0) {
                                OneCardRechargeHintActivity.this.bdj_ggk_money = 0;
                            } else {
                                for (Entry<String, Object> entry : GGkmap.entrySet()) {
                                    OneCardRechargeHintActivity.this.bdj_ggk_money = OneCardRechargeHintActivity.this.bdj_ggk_money + Integer.parseInt(String.valueOf(entry.getValue()));
                                }
                            }
                            final double INCOM_BALANCE = (double) (OneCardRechargeHintActivity.this.bdj_ggk_money / 100);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                public void run() {
                                    OneCardRechargeHintActivity.this.INCOMBalance.setText("" + OneCardRechargeHintActivity.this.df.format(INCOM_BALANCE));
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                this.moneyRechargeLayout.setVisibility(View.GONE);
                this.INCOMBalenceLayout.setVisibility(View.GONE);
            }
            SysGlobal.execute(new Runnable() {
                public void run() {
                    try {
                        if (CardEntity.VERSION == 0) {
                            OneCardRechargeHintActivity.this.hashMapQueryOneCardBalance = OneCardRechargeHintActivity.this.guiCommonService.execute("GUIOneCardCommonService", "queryOneCardBalance", null);
                        } else if (CardEntity.VERSION == 1) {
                            HashMap resultMap = OneCardRechargeHintActivity.this.guiCommonService.execute("GUITrafficCardCommonService", "readOneCard", null);
                            if (resultMap == null || !NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) resultMap.get("RET_CODE"))) {
                                OneCardRechargeHintActivity.this.showSwipeCardFaildInfo();
                            } else {
                                OneCardRechargeHintActivity.this.hashMapQueryOneCardBalance = OneCardRechargeHintActivity.this.guiCommonService.execute("GUITrafficCardCommonService", "queryOneCardBalance", null);
                            }
                        }
                        if (OneCardRechargeHintActivity.this.hashMapQueryOneCardBalance != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                public void run() {
                                    if (CardEntity.VERSION == 1) {
                                        String Money = new DecimalFormat("0.00").format((double) (Float.parseFloat((String) OneCardRechargeHintActivity.this.hashMapQueryOneCardBalance.get("Balance")) / 100.0f));
                                        OneCardRechargeHintActivity.this.moneyInsideCard.setText(Money);
                                        GUIGlobal.setCurrentSession("cardBalance", Money);
                                        return;
                                    }
                                    String oneCardBalance = (String) OneCardRechargeHintActivity.this.hashMapQueryOneCardBalance.get("CARD_BALANCE");
                                    if (!StringUtils.isBlank(oneCardBalance)) {
                                        if (Pattern.compile(SysConfig.get("VERIFY.MONEYINSIDECARD.PATTERN")).matcher(oneCardBalance).matches()) {
                                            OneCardRechargeHintActivity.this.moneyInsideCard.setText(oneCardBalance);
                                            GUIGlobal.setCurrentSession("cardBalance", oneCardBalance);
                                            return;
                                        }
                                        OneCardRechargeHintActivity.this.moneyInsideCardLayout.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        TimeoutTask.getTimeoutTask().addTimeoutAction(this.timeoutAction, Integer.valueOf(SysConfig.get("RVM.TIMEOUT.VALIDATE")).intValue(), false);
        TimeoutTask.getTimeoutTask().reset(this.timeoutAction);
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, true);
    }

    protected void onPause() {
        super.onPause();
    }

    public void finish() {
        super.finish();
        TimeoutTask.getTimeoutTask().setEnabled(this.timeoutAction, false);
        TimeoutTask.getTimeoutTask().removeTimeoutAction(this.timeoutAction);
    }

    public void setClickListener() {
        Button btnToLoad = (Button) findViewById(R.id.toload);
        Button btnScrape = (Button) findViewById(R.id.scrape);
        this.btnFastVolume = (Button) findViewById(R.id.fastVolume);
        Button btnEnd = (Button) findViewById(R.id.end);
        Button btnother = (Button) findViewById(R.id.queryandother);
        Button btnincomrechare = (Button) findViewById(R.id.incomrecharge);
        Button btnBDJ = (Button) findViewById(R.id.btn_bdj);
        if (CardEntity.VERSION == 1) {
            btnToLoad.setVisibility(View.VISIBLE);
            btnScrape.setVisibility(View.VISIBLE);
            this.btnFastVolume.setVisibility(View.VISIBLE);
            btnBDJ.setVisibility(View.VISIBLE);
        }
        if (10.0d > CardEntity.RECHARGED) {
            CardEntity.IS_RECHANGE = 0;
        }
        btnToLoad.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (!OneCardRechargeHintActivity.this.isClick) {
                    OneCardRechargeHintActivity.this.isClick = true;
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            if (CardEntity.CARD_NO.equals(OneCardRechargeHintActivity.this.getCardNo())) {
                                Intent intent = new Intent(OneCardRechargeHintActivity.this.getBaseContext(), OneCardToLoadActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                OneCardRechargeHintActivity.this.startActivity(intent);
                                OneCardRechargeHintActivity.this.finish();
                                return;
                            }
                            OneCardRechargeHintActivity.this.showSwipeCardFaildInfo();
                        }
                    });
                }
            }
        });
        btnScrape.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (!OneCardRechargeHintActivity.this.isClick) {
                    OneCardRechargeHintActivity.this.isClick = true;
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            if (CardEntity.CARD_NO.equals(OneCardRechargeHintActivity.this.getCardNo())) {
                                Intent intent = new Intent(OneCardRechargeHintActivity.this.getBaseContext(), OneCardToScrapeActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                OneCardRechargeHintActivity.this.startActivity(intent);
                                OneCardRechargeHintActivity.this.finish();
                                return;
                            }
                            OneCardRechargeHintActivity.this.showSwipeCardFaildInfo();
                        }
                    });
                }
            }
        });
        this.btnFastVolume.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (!OneCardRechargeHintActivity.this.isClick) {
                    OneCardRechargeHintActivity.this.isClick = true;
                    TimeoutTask.getTimeoutTask().reset(OneCardRechargeHintActivity.this.timeoutAction);
                    if (CardEntity.CARD_NO.equals(OneCardRechargeHintActivity.this.getCardNo())) {
                        final Dialog dialog = new Dialog(OneCardRechargeHintActivity.this, R.style.Custom_dialog);
                        View contentView = LayoutInflater.from(OneCardRechargeHintActivity.this).inflate(R.layout.dialog_onecard_jiareminder, null);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setContentView(contentView);
                        if (!OneCardRechargeHintActivity.this.isFinishing()) {
                            dialog.show();
                        }
                        LayoutParams params0 = dialog.getWindow().getAttributes();
                        params0.x = 10;
                        params0.y = 11;
                        params0.width = FTPCodes.FILE_NOT_FOUND;
                        params0.height = 360;
                        dialog.getWindow().setAttributes(params0);
                        SysGlobal.execute(new Runnable() {
                            public void run() {
                                OneCardRechargeHintActivity.this.getQuery(dialog);
                            }
                        });
                        return;
                    }
                    OneCardRechargeHintActivity.this.showSwipeCardFaildInfo();
                }
            }
        });
        btnother.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                OneCardRechargeHintActivity.this.isblack = "isback";
                OneCardRechargeHintActivity.this.lin = (LinearLayout) OneCardRechargeHintActivity.this.findViewById(R.id.conve_select_this);
                OneCardRechargeHintActivity.this.lin.setVisibility(View.GONE);
                OneCardRechargeHintActivity.this.infalter = LayoutInflater.from(OneCardRechargeHintActivity.this);
                LinearLayout linear = (LinearLayout) OneCardRechargeHintActivity.this.findViewById(R.id.conve_select_query);
                OneCardRechargeHintActivity.this.layout = OneCardRechargeHintActivity.this.infalter.inflate(R.layout.onecardqueryother, null);
                linear.addView(OneCardRechargeHintActivity.this.layout);
                Button btnConsumptionRecord = (Button) OneCardRechargeHintActivity.this.findViewById(R.id.consumptionrecord);
                Button btnRechargeRecord = (Button) OneCardRechargeHintActivity.this.findViewById(R.id.rechargerecord);
                Button brnRebind = (Button) OneCardRechargeHintActivity.this.findViewById(R.id.rebind);
                if (CardEntity.CARD_STATUS == -2) {
                    brnRebind.setText(R.string.bind);
                } else {
                    brnRebind.setText(R.string.rebind);
                }
                btnRechargeRecord.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (!OneCardRechargeHintActivity.this.isClick) {
                            OneCardRechargeHintActivity.this.isClick = true;
                            HashMap map = new HashMap();
                            map.put("KEY", AllClickContent.QUERY_ONECARD_RECHARGERECORD);
                            try {
                                CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                            } catch (Exception e) {
                            }
                            SysGlobal.execute(new Runnable() {
                                public void run() {
                                    OneCardRechargeHintActivity.this.getCardNo();
                                    if (CardEntity.CARD_NO.equals(OneCardRechargeHintActivity.this.cardNo)) {
                                        Intent intent = null;
                                        if (CardEntity.VERSION == 0) {
                                            intent = new Intent(OneCardRechargeHintActivity.this, QueryRechargeRecordActivity.class);
                                        } else if (CardEntity.VERSION == 1) {
                                            intent = new Intent(OneCardRechargeHintActivity.this, QueryNewRechargeRecordActivity.class);
                                        }
                                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        OneCardRechargeHintActivity.this.startActivity(intent);
                                        OneCardRechargeHintActivity.this.finish();
                                        return;
                                    }
                                    OneCardRechargeHintActivity.this.showSwipeCardFaildInfo();
                                }
                            });
                        }
                    }
                });
                btnConsumptionRecord.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (!OneCardRechargeHintActivity.this.isClick) {
                            OneCardRechargeHintActivity.this.isClick = true;
                            HashMap map = new HashMap();
                            map.put("KEY", AllClickContent.QUERY_ONECARD_CONSUMPTION_RECORD);
                            try {
                                CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                            } catch (Exception e) {
                            }
                            SysGlobal.execute(new Runnable() {
                                public void run() {
                                    if (CardEntity.CARD_NO.equals(OneCardRechargeHintActivity.this.getCardNo())) {
                                        Intent intent = null;
                                        if (CardEntity.VERSION == 0) {
                                            intent = new Intent(OneCardRechargeHintActivity.this, QueryConsumptionRecordActivity.class);
                                        } else if (CardEntity.VERSION == 1) {
                                            intent = new Intent(OneCardRechargeHintActivity.this, QueryNewConsumptionRecordActivity.class);
                                        }
                                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        OneCardRechargeHintActivity.this.startActivity(intent);
                                        OneCardRechargeHintActivity.this.finish();
                                        return;
                                    }
                                    OneCardRechargeHintActivity.this.showSwipeCardFaildInfo();
                                }
                            });
                        }
                    }
                });
                brnRebind.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (!OneCardRechargeHintActivity.this.isClick) {
                            OneCardRechargeHintActivity.this.isClick = true;
                            HashMap map = new HashMap();
                            map.put("KEY", AllClickContent.QUERY_ONECARD_REBIND);
                            try {
                                CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                            } catch (Exception e) {
                            }
                            String OneCardVerson = SysConfig.get("RVM.ONECARD.DRV.VERSION");
                            Intent intent;
                            if (CardEntity.VERSION == 0) {
                                intent = new Intent(OneCardRechargeHintActivity.this, OneCardBindPhoneActivity.class);
                                intent.putExtra("RESON", "REBIND");
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                OneCardRechargeHintActivity.this.startActivity(intent);
                                OneCardRechargeHintActivity.this.finish();
                            } else if (CardEntity.VERSION == 1) {
                                intent = new Intent(OneCardRechargeHintActivity.this, NewOneCardBindPhoneActivity.class);
                                intent.putExtra("RESON", "REBIND");
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                OneCardRechargeHintActivity.this.startActivity(intent);
                                OneCardRechargeHintActivity.this.finish();
                            }
                        }
                    }
                });
            }
        });
        btnEnd.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("KEY", AllClickContent.QUERY_ONECARD_END);
                try {
                    CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                } catch (Exception e) {
                }
                if (OneCardRechargeHintActivity.this.isblack.equalsIgnoreCase("isback")) {
                    if (OneCardRechargeHintActivity.this.layout != null) {
                        ((ViewGroup) OneCardRechargeHintActivity.this.layout.getParent()).removeView(OneCardRechargeHintActivity.this.layout);
                    }
                    OneCardRechargeHintActivity.this.lin.setVisibility(View.VISIBLE);
                    OneCardRechargeHintActivity.this.setClickListener();
                    OneCardRechargeHintActivity.this.isblack = "goback";
                } else if (OneCardRechargeHintActivity.this.isblack.equalsIgnoreCase("goback") && !StringUtils.isBlank(SysConfig.get("RVMMActivity.class"))) {
                    try {
                        Intent intent = new Intent(OneCardRechargeHintActivity.this.getBaseContext(), Class.forName(SysConfig.get("RVMMActivity.class")));
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        OneCardRechargeHintActivity.this.startActivity(intent);
                        OneCardRechargeHintActivity.this.finish();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });
        btnincomrechare.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!OneCardRechargeHintActivity.this.isClick) {
                    OneCardRechargeHintActivity.this.isClick = true;
                    HashMap map = new HashMap();
                    map.put("KEY", AllClickContent.QUERY_ONECARD_RECHARGE);
                    try {
                        CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "add_click", map);
                    } catch (Exception e) {
                    }
                    if (CardEntity.CARD_STATUS == -2) {
                        Intent intent = null;
                        if (CardEntity.VERSION == 0) {
                            intent = new Intent(OneCardRechargeHintActivity.this, OneCardBindPhoneActivity.class);
                        } else if (CardEntity.VERSION == 1) {
                            intent = new Intent(OneCardRechargeHintActivity.this, NewOneCardBindPhoneActivity.class);
                        }
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        OneCardRechargeHintActivity.this.startActivity(intent);
                        OneCardRechargeHintActivity.this.finish();
                    } else if (CardEntity.IS_RECHANGE == 0) {
                        OneCardRechargeHintActivity.this.isClick = false;
                        LayoutInflater inflate = (LayoutInflater) OneCardRechargeHintActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                        TextView tv = (TextView) OneCardRechargeHintActivity.this.findViewById(R.id.conve_remind_text);
                        tv.setText(R.string.recharge_lack_money);
                        tv.setGravity(17);
                        tv.setTextSize(20.0f);
                        tv.setTextColor(Color.parseColor("#FFFFFF"));
                    } else {
                        SysGlobal.execute(new Runnable() {
                            public void run() {
                                if (CardEntity.CARD_NO.equals(OneCardRechargeHintActivity.this.getCardNo())) {
                                    Intent intent = null;
                                    if (CardEntity.VERSION == 0) {
                                        intent = new Intent(OneCardRechargeHintActivity.this, OneCardRechargingActivity.class);
                                    } else if (CardEntity.VERSION == 1) {
                                        intent = new Intent(OneCardRechargeHintActivity.this, NewOneCardRechargingActivity.class);
                                    }
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    OneCardRechargeHintActivity.this.startActivity(intent);
                                    OneCardRechargeHintActivity.this.finish();
                                    return;
                                }
                                OneCardRechargeHintActivity.this.showSwipeCardFaildInfo();
                            }
                        });
                    }
                }
            }
        });
        btnBDJ.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!OneCardRechargeHintActivity.this.isClick) {
                    OneCardRechargeHintActivity.this.isClick = true;
                    SysGlobal.execute(new Runnable() {
                        public void run() {
                            if (CardEntity.CARD_NO.equals(OneCardRechargeHintActivity.this.getCardNo())) {
                                Intent intent = new Intent();
                                intent.setClass(OneCardRechargeHintActivity.this, BDJRechagActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                OneCardRechargeHintActivity.this.startActivity(intent);
                                OneCardRechargeHintActivity.this.finish();
                                return;
                            }
                            OneCardRechargeHintActivity.this.showSwipeCardFaildInfo();
                        }
                    });
                }
            }
        });
    }

    public void updateLanguage() {
    }

    public void doEvent(HashMap hsmpEvent) {
    }

    protected void onResume() {
        super.onResume();
        setClickListener();
    }

    public void getQuery(Dialog dialog) {
        this.hsmp = new HashMap();
        GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
        String OneCardNum = (String) ServiceGlobal.getCurrentSession("ONECARD_NUM");
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("CardNo", OneCardNum);
        try {
            this.hsmp = guiCommonService.execute("GUITrafficCardCommonService", "QueryQuickCard", hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.dismiss();
        if (this.hsmp != null) {
            List<HashMap<String, String>> write = (List) this.hsmp.get("QCINFO");
            if (write != null && write.size() > 0) {
                HashMap<String, String> hsmpWrite = (HashMap) write.get(0);
                this.OrderNo = (String) hsmpWrite.get("OrderNo");
                this.OrderMoney = (String) hsmpWrite.get("OrderSaveMnt");
                ServiceGlobal.setCurrentSession("OrderNo", this.OrderNo);
                ServiceGlobal.setCurrentSession("OrderSaveMnt", this.OrderMoney);
            }
        }
        runOnUiThread(new Runnable() {
            public void run() {
                OneCardRechargeHintActivity.this.ShowDialog();
            }
        });
    }

    public void ShowDialog() {
        int MyBalance = 0;
        int MyMaxBalance = 0;
        int RechargeMoney = 0;
        if (!(StringUtils.isBlank((String) ServiceGlobal.getCurrentSession("BALANCE")) || StringUtils.isBlank((String) ServiceGlobal.getCurrentSession("MaxBalance")) || StringUtils.isBlank(this.OrderMoney))) {
            MyBalance = Integer.parseInt((String) ServiceGlobal.getCurrentSession("BALANCE")) / 100;
            MyMaxBalance = Integer.parseInt((String) ServiceGlobal.getCurrentSession("MaxBalance")) / 100;
            RechargeMoney = Integer.parseInt(this.OrderMoney) / 100;
        }
        final Dialog dialog;
        if (MyBalance + RechargeMoney > MyMaxBalance) {
            LayoutInflater factory = LayoutInflater.from(this);
            dialog = new Dialog(this, R.style.Custom_dialog);
            View contentView = factory.inflate(R.layout.dialog_onecard_thousand, null);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(contentView);
            if (!isFinishing()) {
                dialog.show();
            }
            LayoutParams params0 = dialog.getWindow().getAttributes();
            params0.x = 10;
            params0.y = 11;
            params0.width = FTPCodes.FILE_NOT_FOUND;
            params0.height = 360;
            dialog.getWindow().setAttributes(params0);
            Button btnKnown = (Button) contentView.findViewById(R.id.btnKnown);
            btnKnown.setVisibility(View.VISIBLE);
            btnKnown.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    OneCardRechargeHintActivity.this.isClick = false;
                    if (!OneCardRechargeHintActivity.this.isFinishing()) {
                        dialog.dismiss();
                    }
                }
            });
            return;
        }
        LayoutInflater factory = LayoutInflater.from(this);
        dialog = new Dialog(this, R.style.Custom_dialog);
        View contentView = factory.inflate(R.layout.dialog_onecard_reminder, null);
        TextView tv = (TextView) contentView.findViewById(R.id.reminderOneCard);
        TextView tv1 = (TextView) contentView.findViewById(R.id.o1);
        TextView tv2 = (TextView) contentView.findViewById(R.id.o2);
        if (StringUtils.isBlank(this.OrderMoney)) {
            if (this.hsmp != null) {
                if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) this.hsmp.get("RET_CODE"))) {
                    ((Button) contentView.findViewById(R.id.btnKnown)).setVisibility(View.GONE);
                    ((Button) contentView.findViewById(R.id.btnNo)).setText(R.string.confirm);
                    tv1.setVisibility(View.GONE);
                    tv2.setVisibility(View.GONE);
                    tv.setText(R.string.no_quick);
                }
            }
            ((Button) contentView.findViewById(R.id.btnKnown)).setVisibility(View.GONE);
            ((Button) contentView.findViewById(R.id.btnNo)).setText(R.string.confirm);
            tv1.setVisibility(View.GONE);
            tv2.setVisibility(View.GONE);
            tv.setText(R.string.no_quick_by_newwork);
        } else {
            TextView textView = tv;
            textView.setText(this.df.format(Double.parseDouble(this.OrderMoney) / 100.0d) + "");
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(contentView);
        if (!isFinishing()) {
            dialog.show();
        }
       LayoutParams params0 = dialog.getWindow().getAttributes();
        params0.x = 10;
        params0.y = 11;
        params0.width = FTPCodes.FILE_NOT_FOUND;
        params0.height = 360;
        dialog.getWindow().setAttributes(params0);
        ((Button) contentView.findViewById(R.id.btnNo)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                OneCardRechargeHintActivity.this.isClick = false;
                if (!OneCardRechargeHintActivity.this.isFinishing()) {
                    dialog.dismiss();
                }
            }
        });
        ((Button) contentView.findViewById(R.id.btnKnown)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    if (!OneCardRechargeHintActivity.this.chargeClick) {
                        OneCardRechargeHintActivity.this.chargeClick = true;
                        OneCardRechargeHintActivity.this.isClick = false;
                        dialog.dismiss();
                        LayoutInflater factory = LayoutInflater.from(OneCardRechargeHintActivity.this);
                        final Dialog dialogCharge = new Dialog(OneCardRechargeHintActivity.this, R.style.Custom_dialog);
                        View contentView = factory.inflate(R.layout.dialog_onecard_toload, null);
                        dialogCharge.setCanceledOnTouchOutside(false);
                        dialogCharge.setContentView(contentView);
                        if (!OneCardRechargeHintActivity.this.isFinishing()) {
                            dialogCharge.show();
                        }
                        LayoutParams params0 = dialogCharge.getWindow().getAttributes();
                        params0.x = 10;
                        params0.y = 11;
                        params0.width = FTPCodes.FILE_NOT_FOUND;
                        params0.height = 360;
                        dialogCharge.getWindow().setAttributes(params0);
                        SysGlobal.execute(new Runnable() {
                            public void run() {
                                GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
                                String OneCardNum = (String) ServiceGlobal.getCurrentSession("ONECARD_NUM");
                                HashMap<String, Object> hashMap = new HashMap();
                                hashMap.put("OrderNo", ServiceGlobal.getCurrentSession("OrderNo"));
                                hashMap.put("Balance", ServiceGlobal.getCurrentSession("OrderSaveMnt"));
                                hashMap.put("CardNo", OneCardNum);
                                ServiceGlobal.setCurrentSession("ONECARD_MONEY", OneCardRechargeHintActivity.this.df.format(Double.parseDouble((String) ServiceGlobal.getCurrentSession("OrderSaveMnt")) / 100.0d) + "");
                                HashMap hsmp = new HashMap();
                                try {
                                    hsmp = guiCommonService.execute("GUITrafficCardCommonService", "ChargeQuickCard", hashMap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (!OneCardRechargeHintActivity.this.isFinishing()) {
                                    dialogCharge.dismiss();
                                }
                                String RET_CODE = null;
                                if (hsmp != null) {
                                    RET_CODE = (String) hsmp.get("RET_CODE");
                                }
                                if (RET_CODE.equalsIgnoreCase(NetworkUtils.NET_STATE_SUCCESS)) {
                                    Intent intent = new Intent(OneCardRechargeHintActivity.this.getApplicationContext(), NewOneCardRechargeResultActivity.class);
                                    intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    OneCardRechargeHintActivity.this.startActivity(intent);
                                    OneCardRechargeHintActivity.this.finish();
                                    return;
                                }
                                String errorType = ErrorCode.MODEL_RESPONSECODE;
                                String str = (String) hsmp.get(ErrorCode.MODEL_RESPONSECODE);
                                if (StringUtils.isBlank(str)) {
                                    errorType = "HOST_RESPONSECODE";
                                    str = (String) hsmp.get("HOST_RESPONSECODE");
                                }
                                if (StringUtils.isBlank(str)) {
                                    errorType = "SERVER_RESPONSECODE";
                                    str = (String) hsmp.get("SERVER_RESPONSECODE");
                                }
                                if (StringUtils.isBlank(str)) {
                                    errorType = "RVM_RESPONSECODE";
                                    str = (String) hsmp.get("RVM_RESPONSECODE");
                                }
                                if (StringUtils.isBlank(str)) {
                                    errorType = "RVM_RESPONSECODE";
                                    str = NetworkUtils.NET_STATE_UNKNOWN;
                                }
                                Intent intent = new Intent(OneCardRechargeHintActivity.this.getApplicationContext(), NewOneCardRechargeResultWarnActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                                intent.putExtra("errorType", errorType);
                                intent.putExtra("errorCode", str);
                                OneCardRechargeHintActivity.this.startActivity(intent);
                                OneCardRechargeHintActivity.this.finish();
                            }
                        });
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    public String getCardNo() {
        try {
            GUICommonService guiCommonService = CommonServiceHelper.getGUICommonService();
            HashMap<String, Object> hashMap = null;
            String OneCardVerson = SysConfig.get("RVM.ONECARD.DRV.VERSION");
            if (OneCardVerson.equals("0")) {
                hashMap = guiCommonService.execute("GUIOneCardCommonService", "readOneCard", null);
            } else if (OneCardVerson.equals("1")) {
                hashMap = guiCommonService.execute("GUITrafficCardCommonService", "readOneCard", null);
            }
            if (hashMap == null) {
                this.cardNo = null;
            } else if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hashMap.get("RET_CODE"))) {
                this.cardNo = hashMap.get("ONECARD_NUM").toString();
            } else {
                this.cardNo = null;
            }
            return this.cardNo;
        } catch (Exception e) {
            return null;
        }
    }

    private void showSwipeCardFaildInfo() {
        runOnUiThread(new Runnable() {
            public void run() {
                LayoutInflater factory = LayoutInflater.from(OneCardRechargeHintActivity.this);
                final Dialog dialog = new Dialog(OneCardRechargeHintActivity.this, R.style.Custom_dialog);
                View contentView = factory.inflate(R.layout.dialog_notsame_onecard, null);
                if (StringUtils.isBlank(OneCardRechargeHintActivity.this.cardNo)) {
                    ((TextView) contentView.findViewById(R.id.reminderCmcc)).setText(R.string.swiping_card_fail);
                }
                dialog.setTitle(Html.fromHtml(StringUtils.replace(OneCardRechargeHintActivity.this.getResources().getString(R.string.cmccRechargeReminder),
                        OneCardRechargeHintActivity.this.getResources().getString(R.string.cmccRechargeReminder), "<H1><font color=\"#ff0000\">"
                                + OneCardRechargeHintActivity.this.getResources().getString(R.string.cmccRechargeReminder) + "</font></H1>")));
                dialog.setContentView(contentView);
                dialog.setCancelable(false);
                if (!OneCardRechargeHintActivity.this.isFinishing()) {
                    dialog.show();
                }
                LayoutParams params0 = dialog.getWindow().getAttributes();
                params0.x = 10;
                params0.y = 11;
                params0.width = FTPCodes.FILE_NOT_FOUND;
                params0.height = 360;
                dialog.getWindow().setAttributes(params0);
                ((Button) contentView.findViewById(R.id.btnKnown)).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        OneCardRechargeHintActivity.this.isClick = false;
                        dialog.dismiss();
                    }
                });
            }
        });
    }
}
