package com.incomrecycle.prms.rvm.service.commonservice;

import android.database.sqlite.SQLiteDatabase;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.format.SimpleFormatter;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.commtable.CommTableRecord;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.sqlite.DBQuery;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlBuilder;
import com.incomrecycle.common.sqlite.SqlInsertBuilder;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AlarmId;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.ServiceName;
import com.incomrecycle.prms.rvm.service.MainCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CommEventYC103CommonService extends BaseAppCommonService {
    public static final Logger logger = LoggerFactory.getLogger("SVCEVENT");

    public HashMap execute(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        logger.debug(JSONUtils.toJSON(hsmpParam));
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        if ("BARCODE_DATA".equalsIgnoreCase(type)) {
            logger.info("BARCODE:" + data + " at " + ((String) hsmpParam.get("time")));
            CommService.getCommService().execute("SET_SECOND_LIGHT_ON", null);
            return null;
        } else if ("ENTRY_RECYCLE".equalsIgnoreCase(type)) {
            return entryRecycle(svcName, subSvcName, hsmpParam);
        } else {
            if ("SECOND_LIGHT_LOST".equalsIgnoreCase(type)) {
                return lostSecondLight(svcName, subSvcName, hsmpParam);
            }
            if ("RECYCLE_CHECK".equalsIgnoreCase(type)) {
                return plcRecycleCheck(svcName, subSvcName, hsmpParam);
            }
            if ("CHECK".equalsIgnoreCase(type)) {
                return check(svcName, subSvcName, hsmpParam);
            }
            if ("NO_FIRST_LIGHT".equalsIgnoreCase(type)) {
                return plcNoFirstLight(svcName, subSvcName, hsmpParam);
            }
            if ("NO_SECOND_LIGHT".equalsIgnoreCase(type)) {
                return plcNoSecondLight(svcName, subSvcName, hsmpParam);
            }
            if ("NO_THIRD_LIGHT".equalsIgnoreCase(type)) {
                return plcNoThirdLight(svcName, subSvcName, hsmpParam);
            }
            if ("RVM_BUTTON_PUSH".equalsIgnoreCase(type)) {
                return plcButtonPush(svcName, subSvcName, hsmpParam);
            }
            if ("FIRST_LIGHT_ON".equalsIgnoreCase(type)) {
                return plcFirstLightOn(svcName, subSvcName, hsmpParam);
            }
            if ("SCAN_BARCODE".equalsIgnoreCase(type)) {
                return plcScanBarcode(svcName, subSvcName, hsmpParam);
            }
            if ("SECOND_LIGHT_ON".equalsIgnoreCase(type)) {
                return plcSecondLightOn(svcName, subSvcName, hsmpParam);
            }
            if ("THIRD_LIGHT_ON".equalsIgnoreCase(type)) {
                return plcThirdLightOn(svcName, subSvcName, hsmpParam);
            }
            if ("FIRST_LIGHT_ON_BACKWARD".equalsIgnoreCase(type)) {
                return plcFirstLightOnBackward(svcName, subSvcName, hsmpParam);
            }
            if ("SECOND_LIGHT_ON_BACKWARD".equalsIgnoreCase(type)) {
                return plcSecondLightOnBackward(svcName, subSvcName, hsmpParam);
            }
            if ("THIRD_LIGHT_ON_BACKWARD".equalsIgnoreCase(type)) {
                return plcThirdLightOnBackward(svcName, subSvcName, hsmpParam);
            }
            if ("PLC_COMM_ERROR".equalsIgnoreCase(type)) {
                return plcCommError(svcName, subSvcName, hsmpParam);
            }
            if ("PLC_COMM_ERROR_RECOVERY".equalsIgnoreCase(type)) {
                return plcCommErrorRecovery(svcName, subSvcName, hsmpParam);
            }
            if ("PLC_DOOR_ERROR".equalsIgnoreCase(type)) {
                return plcDoorError(svcName, subSvcName, hsmpParam);
            }
            if ("PLC_DOOR_ERROR_RECOVERY".equalsIgnoreCase(type)) {
                return plcDoorErrorRecovery(svcName, subSvcName, hsmpParam);
            }
            if ("PLC_ERROR".equalsIgnoreCase(type)) {
                return plcError(svcName, subSvcName, hsmpParam);
            }
            if ("PLC_ERROR_RECOVERY".equalsIgnoreCase(type)) {
                return plcErrorRecovery(svcName, subSvcName, hsmpParam);
            }
            if ("PRINTER_ERROR".equalsIgnoreCase(type)) {
                return printerError(svcName, subSvcName, hsmpParam);
            }
            if ("PRINTER_ERROR_RECOVERY".equalsIgnoreCase(type)) {
                return printerErrorRecovery(svcName, subSvcName, hsmpParam);
            }
            if ("PRINTER_NO_PAPER".equalsIgnoreCase(type)) {
                return printerNoPaper(svcName, subSvcName, hsmpParam);
            }
            if ("PRINTER_NO_PAPER_RECOVERY".equalsIgnoreCase(type)) {
                return printerNoPaperRecovery(svcName, subSvcName, hsmpParam);
            }
            if ("BARCODE_READER_ERROR".equalsIgnoreCase(type)) {
                return barcodeReaderError(svcName, subSvcName, hsmpParam);
            }
            if ("BARCODE_READER_ERROR_RECOVERY".equalsIgnoreCase(type)) {
                return barcodeReaderErrorRecovery(svcName, subSvcName, hsmpParam);
            }
            if ("MAGNETIC_CARD_NUM".equalsIgnoreCase(type)) {
                return magneticCardNum(svcName, subSvcName, hsmpParam);
            }
            if ("HUILIFE_CARD_NUM".equalsIgnoreCase(type)) {
                return huilifeCardNum(svcName, subSvcName, hsmpParam);
            }
            if ("LIGHT_UNNORMAL_ON".equalsIgnoreCase(type)) {
                return lightUnnormalOn(svcName, subSvcName, hsmpParam);
            }
            if ("LIGHT_NORMAL".equalsIgnoreCase(type)) {
                return lightNormal(svcName, subSvcName, hsmpParam);
            }
            if ("ONECARDDRV_ERROR".equalsIgnoreCase(type)) {
                return oneCardDrvError(svcName, subSvcName, hsmpParam);
            }
            if ("ONECARDDRV_ERROR_RECOVERY".equalsIgnoreCase(type)) {
                return oneCardDrvErrorRecovery(svcName, subSvcName, hsmpParam);
            }
            return null;
        }
    }

    public HashMap lightNormal(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:PLC_LIGHT_ERROR", "FALSE");
        SysConfig.set("STATE:PLC_FIRST_LIGHT_ERROR", "FALSE");
        SysConfig.set("STATE:PLC_SECOND_LIGHT_ERROR", "FALSE");
        SysConfig.set("STATE:PLC_THIRD_LIGHT_ERROR", "FALSE");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "CMD");
        hsmpGUIEvent.put("CMD", "LIGHT_NORMAL");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        recoverAlarm(Integer.toString(5001), null);
        recoverAlarm(Integer.toString(5002), null);
        recoverAlarm(Integer.toString(5003), null);
        return null;
    }

    public HashMap lightUnnormalOn(String svcName, String subSvcName, HashMap hsmpParam) throws Exception {
        String msg = (String) hsmpParam.get("msg");
        SysConfig.set("STATE:PLC_LIGHT_ERROR", "TRUE");
        if (msg != null) {
            HashMap<String, String> hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
            hsmpGUIEvent.put("EVENT", "CMD");
            hsmpGUIEvent.put("CMD", "LIGHT_UNNORMAL_ON");
            if ("FIRST_LIGHT_UNNORMAL_ON".equalsIgnoreCase(msg)) {
                SysConfig.set("STATE:PLC_FIRST_LIGHT_ERROR", "TRUE");
                raiseAlarm(Integer.toString(5001), null);
            }
            if ("SECOND_LIGHT_UNNORMAL_ON".equalsIgnoreCase(msg)) {
                SysConfig.set("STATE:PLC_SECOND_LIGHT_ERROR", "TRUE");
                raiseAlarm(Integer.toString(5002), null);
            }
            if ("THIRD_LIGHT_UNNORMAL_ON".equalsIgnoreCase(msg)) {
                SysConfig.set("STATE:PLC_THIRD_LIGHT_ERROR", "TRUE");
                raiseAlarm(Integer.toString(5003), null);
            }
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        }
        return null;
    }

    public HashMap entryRecycle(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "CMD");
        hsmpGUIEvent.put("CMD", "REQUEST_RECYCLE");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap plcButtonPush(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        return null;
    }

    public HashMap lostSecondLight(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "CMD");
        return null;
    }

    public HashMap plcRecycleCheck(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String STUFF_VAL;
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        String HAS_METAL_LIGHT = (String) hsmpParam.get("HAS_METAL_LIGHT");
        String METAL_LIGHT_ON = (String) hsmpParam.get("METAL_LIGHT_ON");
        String VENDING_WAY = (String) ServiceGlobal.getCurrentSession(AllAdvertisement.VENDING_WAY);
        String barCode = StringUtils.trimToNull(CommService.getCommService().execute("BARCODE_READ", null));
        String previousBarCode = (String) ServiceGlobal.getCurrentSession("CURRENT_BAR_CODE");
        boolean isForceRecycle = false;
        if ("FORCE_RECYCLE".equalsIgnoreCase((String) ServiceGlobal.getCurrentSession("BAR_CODE_FLAG"))) {
            isForceRecycle = true;
            ServiceGlobal.setCurrentSession("BAR_CODE_FLAG", null);
        }
        if (!StringUtils.equals(barCode, previousBarCode)) {
            isForceRecycle = false;
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        boolean isValidBarCode = false;
        String BAR_CODE_STUFF = "0";
        boolean RECYCLE_ENABLE = true;
        if (barCode != null) {
            DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
            SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
            sqlWhereBuilder.addStringEqualsTo("BAR_CODE", barCode);
            CommTable commTable = dbQuery.getCommTable("select * from RVM_BAR_CODE " + sqlWhereBuilder.toSqlWhere("where"));
            if (commTable.getRecordCount() != 0) {
                if (Integer.toString(1).equals(commTable.getRecord(0).get("BAR_CODE_FLAG"))) {
                    isValidBarCode = true;
                }
            } else if (dbQuery.getCommTable("select * from RVM_BAR_CODE_UNKNOWN " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
                SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_BAR_CODE_UNKNOWN");
                sqlInsertBuilder.newInsertRecord().setString("BAR_CODE", barCode).setNumber("UPLOAD_FLAG", "0").setDateTime("CREATE_TIME", new Date());
                try {
                    SQLiteExecutor.execSql(sqliteDatabase, sqlInsertBuilder.toSql());
                } catch (Exception e) {
                }
            } else {
                SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_BAR_CODE_UNKNOWN");
                sqlUpdateBuilder.setNumber("UPLOAD_FLAG", Integer.valueOf(0)).setSqlWhere(sqlWhereBuilder);
                SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
            }
            CommTable commTableBarCode = dbQuery.getCommTable("select * from RVM_BAR_CODE " + sqlWhereBuilder.toSqlWhere("where"));
            if (commTableBarCode.getRecordCount() > 0) {
                BAR_CODE_STUFF = commTableBarCode.getRecord(0).get("BAR_CODE_STUFF");
                if (StringUtils.isBlank(BAR_CODE_STUFF)) {
                    BAR_CODE_STUFF = "0";
                }
            }
            List<String> listDisabledService = new ArrayList();
            HashMap<String, Object> hsmpResultServiceDisable = JSONUtils.toHashMap(new MainCommonService().execute("GUIQueryCommonService", "queryServiceDisable", null));
            if (hsmpResultServiceDisable != null) {
                String ServiceDisabled = (String) hsmpResultServiceDisable.get("SERVICE_DISABLED");
                if (!StringUtils.isBlank(ServiceDisabled) && ServiceDisabled.length() > 0) {
                    String[] strDisabledService = ServiceDisabled.split(",");
                    for (String add : strDisabledService) {
                        listDisabledService.add(add);
                    }
                }
            }
            String RECYCLE_MATERIAL_SET = SysConfig.get("RECYCLE.MATERIAL.SET") + "";
            if ("0".equalsIgnoreCase(BAR_CODE_STUFF) && (listDisabledService.contains(ServiceName.PET) || !RECYCLE_MATERIAL_SET.contains(ServiceName.PET))) {
                RECYCLE_ENABLE = false;
            }
            if ("1".equalsIgnoreCase(BAR_CODE_STUFF) && (listDisabledService.contains(ServiceName.METAL) || !RECYCLE_MATERIAL_SET.contains(ServiceName.METAL))) {
                RECYCLE_ENABLE = false;
            }
            if ("TRUE".equalsIgnoreCase(HAS_METAL_LIGHT)) {
                if ("0".equalsIgnoreCase(BAR_CODE_STUFF) && "TRUE".equalsIgnoreCase(METAL_LIGHT_ON)) {
                    RECYCLE_ENABLE = false;
                }
                if ("1".equalsIgnoreCase(BAR_CODE_STUFF) && "FALSE".equalsIgnoreCase(METAL_LIGHT_ON)) {
                    RECYCLE_ENABLE = false;
                }
            }
        }
        if ("TRUE".equalsIgnoreCase(SysConfig.get("RECYCLE.STATUS.STOP"))) {
            STUFF_VAL = ServiceName.PET;
        } else {
            STUFF_VAL = "0".equalsIgnoreCase(BAR_CODE_STUFF) ? ServiceName.PET : "CAN";
        }
        HashMap hsmpCommParam = new HashMap();
        hsmpCommParam.put("STUFF", STUFF_VAL);
        String jsonCommParam = JSONUtils.toJSON(hsmpCommParam);
        HashMap<String, String> hsmpGUIEvent;
        if (!RECYCLE_ENABLE) {
            CommService.getCommService().execute("BOTTLE_REJECT_READY", jsonCommParam);
            hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
            hsmpGUIEvent.put("EVENT", "INFORM");
            hsmpGUIEvent.put("INFORM", "BAR_CODE_REJECT_ACCEPT");
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        } else if ("DONATION".equalsIgnoreCase(VENDING_WAY)) {
            if (isForceRecycle || isValidBarCode || "false".equalsIgnoreCase(SysConfig.get("DONATION.WITHDRAW.ENABLE"))) {
                hsmpGUIEvent = new HashMap();
                hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                hsmpGUIEvent.put("EVENT", "INFORM");
                hsmpGUIEvent.put("INFORM", "BOTTLE_SCAN_END");
                ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
                isForceRecycle = true;
                CommService.getCommService().execute("BOTTLE_ACCEPT_READY", jsonCommParam);
            } else {
                CommService.getCommService().execute("BOTTLE_REJECT_READY", jsonCommParam);
                hsmpGUIEvent = new HashMap();
                hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
                hsmpGUIEvent.put("EVENT", "INFORM");
                if (barCode == null) {
                    hsmpGUIEvent.put("INFORM", "BAR_CODE_NOT_FOUND");
                } else {
                    hsmpGUIEvent.put("INFORM", "BAR_CODE_REJECT");
                }
                ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
            }
        } else if (isValidBarCode) {
            hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
            hsmpGUIEvent.put("EVENT", "INFORM");
            hsmpGUIEvent.put("INFORM", "BOTTLE_ACCEPT_READY");
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
            CommService.getCommService().execute("BOTTLE_ACCEPT_READY", jsonCommParam);
        } else {
            if (isForceRecycle) {
                CommService.getCommService().execute("BOTTLE_ACCEPT_READY", jsonCommParam);
            } else {
                CommService.getCommService().execute("BOTTLE_REJECT_READY", jsonCommParam);
            }
            hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
            hsmpGUIEvent.put("EVENT", "INFORM");
            if (barCode == null) {
                hsmpGUIEvent.put("INFORM", "BAR_CODE_NOT_FOUND");
            } else {
                hsmpGUIEvent.put("INFORM", "BAR_CODE_REJECT");
            }
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        }
        if (barCode == null && isForceRecycle) {
            barCode = SysConfig.get("DEFAULT_BAR_CODE");
        }
        ServiceGlobal.setCurrentSession("CURRENT_BAR_CODE", barCode);
        return null;
    }

    public HashMap plcFirstLightOn(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        CommService.getCommService().execute("BARCODE_RESET", null);
        return null;
    }

    public HashMap plcScanBarcode(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "INFORM");
        hsmpGUIEvent.put("INFORM", "BOTTLE_READY");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap plcSecondLightOn(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public HashMap plcThirdLightOn(String r87, String r88, HashMap r89) throws Exception {
        /*
        r86 = this;
        r2 = "type";
        r0 = r89;
        r81 = r0.get(r2);
        r81 = (java.lang.String) r81;
        r2 = "data";
        r0 = r89;
        r38 = r0.get(r2);
        r38 = (java.lang.String) r38;
        r55 = new java.util.ArrayList;
        r55.<init>();
        r2 = "CURRENT_BAR_CODE";
        r18 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r2);
        r18 = (java.lang.String) r18;
        r2 = "VENDING_WAY";
        r16 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r2);
        r16 = (java.lang.String) r16;
        r2 = "OPT_ID";
        r12 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r2);
        r12 = (java.lang.String) r12;
        r2 = "SYS";
        r2 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getDatabaseHelper(r2);
        r40 = com.incomrecycle.common.sqlite.DBSequence.getInstance(r2);
        if (r12 != 0) goto L_0x004a;
    L_0x003d:
        r2 = "OPT_ID";
        r0 = r40;
        r12 = r0.getSeq(r2);
        r2 = "OPT_ID";
        com.incomrecycle.prms.rvm.service.ServiceGlobal.setCurrentSession(r2, r12);
    L_0x004a:
        r2 = "RVM";
        r2 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getDatabaseHelper(r2);
        r76 = r2.getWritableDatabase();
        r39 = com.incomrecycle.common.sqlite.DBQuery.getDBQuery(r76);
        r71 = new com.incomrecycle.common.sqlite.SqlWhereBuilder;
        r71.<init>();
        r2 = "OPT_ID";
        r0 = r71;
        r0.addNumberEqualsTo(r2, r12);
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "select * from RVM_OPT";
        r2 = r2.append(r3);
        r3 = "where";
        r0 = r71;
        r3 = r0.toSqlWhere(r3);
        r2 = r2.append(r3);
        r2 = r2.toString();
        r0 = r39;
        r30 = r0.getCommTable(r2);
        r68 = new com.incomrecycle.common.sqlite.SqlWhereBuilder;
        r68.<init>();
        r2 = "BAR_CODE";
        r0 = r68;
        r1 = r18;
        r0.addStringEqualsTo(r2, r1);
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "select * from RVM_BAR_CODE ";
        r2 = r2.append(r3);
        r3 = "where";
        r0 = r68;
        r3 = r0.toSqlWhere(r3);
        r2 = r2.append(r3);
        r2 = r2.toString();
        r0 = r39;
        r29 = r0.getCommTable(r2);
        r4 = "0";
        r9 = "0";
        r5 = "0";
        r2 = "TOTAL_BAR_CODE_AMOUNT";
        r13 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r2);
        r13 = (java.lang.String) r13;
        r2 = com.incomrecycle.common.utils.StringUtils.isBlank(r13);
        if (r2 == 0) goto L_0x00ca;
    L_0x00c8:
        r13 = "0";
    L_0x00ca:
        r2 = "TOTAL_BAR_CODE_COUNT";
        r14 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r2);
        r14 = (java.lang.String) r14;
        r2 = com.incomrecycle.common.utils.StringUtils.isBlank(r14);
        if (r2 == 0) goto L_0x00da;
    L_0x00d8:
        r14 = "0";
    L_0x00da:
        r2 = "TOTAL_BAR_CODE_VOL";
        r15 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r2);
        r15 = (java.lang.String) r15;
        r2 = com.incomrecycle.common.utils.StringUtils.isBlank(r15);
        if (r2 == 0) goto L_0x00ea;
    L_0x00e8:
        r15 = "0";
    L_0x00ea:
        r2 = r29.getRecordCount();
        if (r2 <= 0) goto L_0x0135;
    L_0x00f0:
        r2 = 0;
        r0 = r29;
        r2 = r0.getRecord(r2);
        r3 = "BAR_CODE_VOL";
        r4 = r2.get(r3);
        r2 = 0;
        r0 = r29;
        r2 = r0.getRecord(r2);
        r3 = "BAR_CODE_STUFF";
        r5 = r2.get(r3);
        r2 = 0;
        r0 = r29;
        r2 = r0.getRecord(r2);
        r3 = "BAR_CODE_WEIGH";
        r6 = r2.get(r3);
        r2 = 0;
        r0 = r29;
        r2 = r0.getRecord(r2);
        r3 = "BAR_CODE_COLOR";
        r7 = r2.get(r3);
        if (r5 != 0) goto L_0x0128;
    L_0x0126:
        r5 = "0";
    L_0x0128:
        r2 = com.incomrecycle.prms.rvm.service.commonservice.RVMBarcodePriceMgr.getMgr();
        r3 = 0;
        r9 = r2.getStuffPrice(r3, r4, r5, r6, r7);
        if (r9 != 0) goto L_0x0135;
    L_0x0133:
        r9 = "0";
    L_0x0135:
        r2 = java.lang.Integer.parseInt(r14);
        r2 = r2 + 1;
        r14 = java.lang.Integer.toString(r2);
        r2 = java.lang.Double.parseDouble(r15);
        r83 = java.lang.Double.parseDouble(r4);
        r2 = r2 + r83;
        r15 = java.lang.Double.toString(r2);
        r2 = java.lang.Double.parseDouble(r13);
        r83 = java.lang.Double.parseDouble(r9);
        r2 = r2 + r83;
        r13 = java.lang.Double.toString(r2);
        r58 = new java.util.ArrayList;
        r58.<init>();
        r78 = new java.util.Date;
        r78.<init>();
        r2 = r30.getRecordCount();
        if (r2 != 0) goto L_0x02aa;
    L_0x016b:
        r61 = new com.incomrecycle.common.sqlite.SqlInsertBuilder;
        r2 = "RVM_OPT";
        r0 = r61;
        r0.<init>(r2);
        r2 = r61.newInsertRecord();
        r3 = "OPT_ID";
        r2 = r2.setNumber(r3, r12);
        r3 = "OPT_TIME";
        r0 = r78;
        r2 = r2.setDateTime(r3, r0);
        r3 = "OPT_TYPE";
        r83 = "RECYCLE";
        r0 = r83;
        r2 = r2.setString(r3, r0);
        r3 = "RVM_CODE";
        r83 = "RVM.CODE";
        r83 = com.incomrecycle.common.SysConfig.get(r83);
        r0 = r83;
        r2 = r2.setString(r3, r0);
        r3 = "PRODUCT_TYPE";
        r83 = "BOTTLE";
        r0 = r83;
        r2 = r2.setString(r3, r0);
        r3 = "PRODUCT_AMOUNT";
        r83 = "1";
        r0 = r83;
        r2 = r2.setNumber(r3, r0);
        r3 = "CARD_TYPE";
        r83 = "CARD_TYPE";
        r83 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r83);
        r0 = r83;
        r2 = r2.setString(r3, r0);
        r3 = "CARD_NO";
        r83 = "CARD_NO";
        r83 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r83);
        r0 = r83;
        r2 = r2.setString(r3, r0);
        r3 = "PROFIT_AMOUNT";
        r2 = r2.setNumber(r3, r9);
        r3 = "VENDING_WAY";
        r0 = r16;
        r2 = r2.setString(r3, r0);
        r3 = "OPT_STATUS";
        r83 = 0;
        r83 = java.lang.Integer.valueOf(r83);
        r0 = r83;
        r2 = r2.setNumber(r3, r0);
        r3 = "CHARGE_FLAG";
        r83 = "0";
        r0 = r83;
        r2 = r2.setNumber(r3, r0);
        r3 = "UNIQUE_CODE";
        r83 = new java.lang.StringBuilder;
        r83.<init>();
        r84 = "RVM.CODE";
        r84 = com.incomrecycle.common.SysConfig.get(r84);
        r83 = r83.append(r84);
        r84 = "_";
        r83 = r83.append(r84);
        r84 = r78.getTime();
        r83 = r83.append(r84);
        r84 = "_";
        r83 = r83.append(r84);
        r0 = r83;
        r83 = r0.append(r12);
        r83 = r83.toString();
        r0 = r83;
        r2.setString(r3, r0);
        r62 = new com.incomrecycle.common.sqlite.SqlInsertBuilder;
        r2 = "RVM_OPT_BOTTLE";
        r0 = r62;
        r0.<init>(r2);
        r2 = r62.newInsertRecord();
        r3 = "OPT_ID";
        r2 = r2.setNumber(r3, r12);
        r3 = "BOTTLE_BAR_CODE";
        r0 = r18;
        r2 = r2.setString(r3, r0);
        r3 = "BOTTLE_VOL";
        r2 = r2.setNumber(r3, r4);
        r3 = "BOTTLE_AMOUNT";
        r2 = r2.setNumber(r3, r9);
        r3 = "BOTTLE_COUNT";
        r83 = "1";
        r0 = r83;
        r2 = r2.setNumber(r3, r0);
        r3 = "VENDING_BOTTLE_COUNT";
        r83 = "1";
        r0 = r83;
        r2.setNumber(r3, r0);
        r0 = r58;
        r1 = r61;
        r0.add(r1);
        r0 = r58;
        r1 = r62;
        r0.add(r1);
    L_0x0270:
        r42 = 1;
        r2 = "VOL_CONVERSION";
        r82 = com.incomrecycle.common.SysConfig.get(r2);
        r2 = java.lang.Double.valueOf(r4);
        r10 = r2.doubleValue();
        r2 = com.incomrecycle.common.utils.StringUtils.isBlank(r82);
        if (r2 != 0) goto L_0x03c6;
    L_0x0286:
        r2 = r82.trim();
        r3 = ";";
        r27 = r2.split(r3);
        r21 = 0;
        r22 = 0;
        r53 = 0;
    L_0x0296:
        r0 = r27;
        r2 = r0.length;
        r0 = r53;
        if (r0 >= r2) goto L_0x03c6;
    L_0x029d:
        r2 = r27[r53];
        r3 = ":";
        r43 = r2.indexOf(r3);
        if (r43 > 0) goto L_0x0378;
    L_0x02a7:
        r53 = r53 + 1;
        goto L_0x0296;
    L_0x02aa:
        r72 = new com.incomrecycle.common.sqlite.SqlWhereBuilder;
        r72.<init>();
        r2 = "OPT_ID";
        r0 = r72;
        r2 = r0.addNumberEqualsTo(r2, r12);
        r3 = "BOTTLE_BAR_CODE";
        r0 = r18;
        r2.addStringEqualsTo(r3, r0);
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "select * from RVM_OPT_BOTTLE ";
        r2 = r2.append(r3);
        r3 = "where";
        r0 = r72;
        r3 = r0.toSqlWhere(r3);
        r2 = r2.append(r3);
        r2 = r2.toString();
        r0 = r39;
        r33 = r0.getCommTable(r2);
        r65 = new com.incomrecycle.common.sqlite.SqlUpdateBuilder;
        r2 = "RVM_OPT";
        r0 = r65;
        r0.<init>(r2);
        r2 = "PRODUCT_AMOUNT";
        r3 = "1";
        r0 = r65;
        r2 = r0.addNumber(r2, r3);
        r3 = "PROFIT_AMOUNT";
        r2 = r2.addNumber(r3, r9);
        r0 = r71;
        r2.setSqlWhere(r0);
        r0 = r58;
        r1 = r65;
        r0.add(r1);
        r2 = r33.getRecordCount();
        if (r2 <= 0) goto L_0x0335;
    L_0x030a:
        r66 = new com.incomrecycle.common.sqlite.SqlUpdateBuilder;
        r2 = "RVM_OPT_BOTTLE";
        r0 = r66;
        r0.<init>(r2);
        r2 = "BOTTLE_COUNT";
        r3 = "1";
        r0 = r66;
        r2 = r0.addNumber(r2, r3);
        r3 = "VENDING_BOTTLE_COUNT";
        r83 = "1";
        r0 = r83;
        r2 = r2.addNumber(r3, r0);
        r0 = r72;
        r2.setSqlWhere(r0);
        r0 = r58;
        r1 = r66;
        r0.add(r1);
        goto L_0x0270;
    L_0x0335:
        r60 = new com.incomrecycle.common.sqlite.SqlInsertBuilder;
        r2 = "RVM_OPT_BOTTLE";
        r0 = r60;
        r0.<init>(r2);
        r2 = r60.newInsertRecord();
        r3 = "OPT_ID";
        r2 = r2.setNumber(r3, r12);
        r3 = "BOTTLE_BAR_CODE";
        r0 = r18;
        r2 = r2.setString(r3, r0);
        r3 = "BOTTLE_VOL";
        r2 = r2.setNumber(r3, r4);
        r3 = "BOTTLE_AMOUNT";
        r2 = r2.setNumber(r3, r9);
        r3 = "BOTTLE_COUNT";
        r83 = "1";
        r0 = r83;
        r2 = r2.setNumber(r3, r0);
        r3 = "VENDING_BOTTLE_COUNT";
        r83 = "1";
        r0 = r83;
        r2.setNumber(r3, r0);
        r0 = r58;
        r1 = r60;
        r0.add(r1);
        goto L_0x0270;
    L_0x0378:
        r2 = r27[r53];
        r3 = 0;
        r0 = r43;
        r19 = r2.substring(r3, r0);
        r2 = r27[r53];
        r3 = r43 + 1;
        r20 = r2.substring(r3);
        r2 = com.incomrecycle.common.utils.StringUtils.isBlank(r20);
        if (r2 != 0) goto L_0x02a7;
    L_0x038f:
        r2 = r20.trim();
        r3 = "-";
        r28 = r2.split(r3);
        r0 = r28;
        r2 = r0.length;
        r3 = 1;
        if (r2 != r3) goto L_0x055d;
    L_0x039f:
        r2 = 0;
        r21 = r28[r2];
        r22 = "0";
    L_0x03a4:
        r2 = java.lang.Double.valueOf(r21);	 Catch:{ Exception -> 0x0aca }
        r25 = r2.doubleValue();	 Catch:{ Exception -> 0x0aca }
        r2 = java.lang.Double.valueOf(r22);	 Catch:{ Exception -> 0x0aca }
        r23 = r2.doubleValue();	 Catch:{ Exception -> 0x0aca }
        r2 = (r25 > r10 ? 1 : (r25 == r10 ? 0 : -1));
        if (r2 > 0) goto L_0x02a7;
    L_0x03b8:
        r2 = 0;
        r2 = (r23 > r2 ? 1 : (r23 == r2 ? 0 : -1));
        if (r2 == 0) goto L_0x03c2;
    L_0x03be:
        r2 = (r23 > r10 ? 1 : (r23 == r10 ? 0 : -1));
        if (r2 <= 0) goto L_0x02a7;
    L_0x03c2:
        r42 = java.lang.Integer.parseInt(r19);	 Catch:{ Exception -> 0x0aca }
    L_0x03c6:
        r73 = new com.incomrecycle.common.sqlite.SqlWhereBuilder;
        r73.<init>();
        r2 = "SYS_CODE_TYPE";
        r3 = "RVM_INFO";
        r0 = r73;
        r2 = r0.addStringEqualsTo(r2, r3);
        r3 = "SYS_CODE_KEY";
        r83 = "STORAGE_CURR_COUNT";
        r0 = r83;
        r2.addStringEqualsTo(r3, r0);
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "select * from RVM_SYS_CODE";
        r2 = r2.append(r3);
        r3 = "where";
        r0 = r73;
        r3 = r0.toSqlWhere(r3);
        r2 = r2.append(r3);
        r2 = r2.toString();
        r0 = r39;
        r34 = r0.getCommTable(r2);
        r77 = 0;
        r59 = 0;
        r2 = r34.getRecordCount();
        if (r2 != 0) goto L_0x0565;
    L_0x0409:
        r60 = new com.incomrecycle.common.sqlite.SqlInsertBuilder;
        r2 = "RVM_SYS_CODE";
        r0 = r60;
        r0.<init>(r2);
        r2 = r60.newInsertRecord();
        r3 = "SYS_CODE_TYPE";
        r83 = "RVM_INFO";
        r0 = r83;
        r2 = r2.setString(r3, r0);
        r3 = "SYS_CODE_KEY";
        r83 = "STORAGE_CURR_COUNT";
        r0 = r83;
        r2 = r2.setString(r3, r0);
        r3 = "SYS_CODE_VALUE";
        r83 = new java.lang.StringBuilder;
        r83.<init>();
        r84 = "";
        r83 = r83.append(r84);
        r0 = r83;
        r1 = r42;
        r83 = r0.append(r1);
        r83 = r83.toString();
        r0 = r83;
        r2.setString(r3, r0);
        r0 = r58;
        r1 = r60;
        r0.add(r1);
        r77 = r42;
    L_0x0451:
        r41 = r42 + -1;
        if (r41 <= 0) goto L_0x04da;
    L_0x0455:
        r74 = new com.incomrecycle.common.sqlite.SqlWhereBuilder;
        r74.<init>();
        r2 = "SYS_CODE_TYPE";
        r3 = "RVM_INFO";
        r0 = r74;
        r2 = r0.addStringEqualsTo(r2, r3);
        r3 = "SYS_CODE_KEY";
        r83 = "STORAGE_CURR_COUNT_DELTA";
        r0 = r83;
        r2.addStringEqualsTo(r3, r0);
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "select * from RVM_SYS_CODE";
        r2 = r2.append(r3);
        r3 = "where";
        r0 = r74;
        r3 = r0.toSqlWhere(r3);
        r2 = r2.append(r3);
        r2 = r2.toString();
        r0 = r39;
        r35 = r0.getCommTable(r2);
        r2 = r35.getRecordCount();
        if (r2 != 0) goto L_0x059b;
    L_0x0494:
        r60 = new com.incomrecycle.common.sqlite.SqlInsertBuilder;
        r2 = "RVM_SYS_CODE";
        r0 = r60;
        r0.<init>(r2);
        r2 = r60.newInsertRecord();
        r3 = "SYS_CODE_TYPE";
        r83 = "RVM_INFO";
        r0 = r83;
        r2 = r2.setString(r3, r0);
        r3 = "SYS_CODE_KEY";
        r83 = "STORAGE_CURR_COUNT_DELTA";
        r0 = r83;
        r2 = r2.setString(r3, r0);
        r3 = "SYS_CODE_VALUE";
        r83 = new java.lang.StringBuilder;
        r83.<init>();
        r84 = "";
        r83 = r83.append(r84);
        r0 = r83;
        r1 = r41;
        r83 = r0.append(r1);
        r83 = r83.toString();
        r0 = r83;
        r2.setString(r3, r0);
        r0 = r58;
        r1 = r60;
        r0.add(r1);
    L_0x04da:
        r69 = new com.incomrecycle.common.sqlite.SqlWhereBuilder;
        r69.<init>();
        r2 = "ALARM_ID";
        r3 = 2;
        r3 = new java.lang.Object[r3];
        r83 = 0;
        r84 = 11;
        r84 = java.lang.Integer.valueOf(r84);
        r3[r83] = r84;
        r83 = 1;
        r84 = 12;
        r84 = java.lang.Integer.valueOf(r84);
        r3[r83] = r84;
        r0 = r69;
        r0.addNumberIn(r2, r3);
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "select * from RVM_ALARM";
        r2 = r2.append(r3);
        r3 = "where";
        r0 = r69;
        r3 = r0.toSqlWhere(r3);
        r2 = r2.append(r3);
        r2 = r2.toString();
        r0 = r39;
        r31 = r0.getCommTable(r2);
        r57 = com.incomrecycle.common.json.JSONUtils.toList(r31);
        r49 = 0;
        r52 = 0;
        if (r57 == 0) goto L_0x05d1;
    L_0x0528:
        r53 = 0;
    L_0x052a:
        r2 = r57.size();
        r0 = r53;
        if (r0 >= r2) goto L_0x05d1;
    L_0x0532:
        r0 = r57;
        r1 = r53;
        r51 = r0.get(r1);
        r51 = (java.util.HashMap) r51;
        r2 = "ALARM_ID";
        r0 = r51;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r17 = java.lang.Integer.parseInt(r2);
        r2 = 11;
        r0 = r17;
        if (r0 != r2) goto L_0x0552;
    L_0x0550:
        r49 = r51;
    L_0x0552:
        r2 = 12;
        r0 = r17;
        if (r0 != r2) goto L_0x055a;
    L_0x0558:
        r52 = r51;
    L_0x055a:
        r53 = r53 + 1;
        goto L_0x052a;
    L_0x055d:
        r2 = 0;
        r21 = r28[r2];
        r2 = 1;
        r22 = r28[r2];
        goto L_0x03a4;
    L_0x0565:
        r2 = 0;
        r0 = r34;
        r2 = r0.getRecord(r2);
        r3 = "SYS_CODE_VALUE";
        r2 = r2.get(r3);
        r59 = java.lang.Integer.parseInt(r2);
        r77 = r59 + r42;
        r67 = new com.incomrecycle.common.sqlite.SqlUpdateBuilder;
        r2 = "RVM_SYS_CODE";
        r0 = r67;
        r0.<init>(r2);
        r2 = "SYS_CODE_VALUE";
        r3 = java.lang.Integer.toString(r77);
        r0 = r67;
        r2 = r0.setString(r2, r3);
        r0 = r73;
        r2.setSqlWhere(r0);
        r0 = r58;
        r1 = r67;
        r0.add(r1);
        goto L_0x0451;
    L_0x059b:
        r2 = 0;
        r0 = r35;
        r2 = r0.getRecord(r2);
        r3 = "SYS_CODE_VALUE";
        r2 = r2.get(r3);
        r2 = java.lang.Integer.parseInt(r2);
        r80 = r41 + r2;
        r63 = new com.incomrecycle.common.sqlite.SqlUpdateBuilder;
        r2 = "RVM_SYS_CODE";
        r0 = r63;
        r0.<init>(r2);
        r2 = "SYS_CODE_VALUE";
        r3 = java.lang.Integer.toString(r80);
        r0 = r63;
        r2 = r0.setString(r2, r3);
        r0 = r74;
        r2.setSqlWhere(r0);
        r0 = r58;
        r1 = r63;
        r0.add(r1);
        goto L_0x04da;
    L_0x05d1:
        r44 = 0;
        r45 = 0;
        if (r52 == 0) goto L_0x0605;
    L_0x05d7:
        r2 = "TSD_VALUE";
        r0 = r52;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r2 = java.lang.Double.parseDouble(r2);
        r0 = r59;
        r0 = (double) r0;
        r83 = r0;
        r2 = (r2 > r83 ? 1 : (r2 == r83 ? 0 : -1));
        if (r2 <= 0) goto L_0x0605;
    L_0x05ee:
        r2 = "TSD_VALUE";
        r0 = r52;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r2 = java.lang.Double.parseDouble(r2);
        r0 = r77;
        r0 = (double) r0;
        r83 = r0;
        r2 = (r2 > r83 ? 1 : (r2 == r83 ? 0 : -1));
        if (r2 <= 0) goto L_0x0635;
    L_0x0605:
        if (r49 == 0) goto L_0x0914;
    L_0x0607:
        r2 = "TSD_VALUE";
        r0 = r49;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r2 = java.lang.Double.parseDouble(r2);
        r0 = r59;
        r0 = (double) r0;
        r83 = r0;
        r2 = (r2 > r83 ? 1 : (r2 == r83 ? 0 : -1));
        if (r2 <= 0) goto L_0x0914;
    L_0x061e:
        r2 = "TSD_VALUE";
        r0 = r49;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r2 = java.lang.Double.parseDouble(r2);
        r0 = r77;
        r0 = (double) r0;
        r83 = r0;
        r2 = (r2 > r83 ? 1 : (r2 == r83 ? 0 : -1));
        if (r2 > 0) goto L_0x0914;
    L_0x0635:
        r70 = new com.incomrecycle.common.sqlite.SqlWhereBuilder;
        r70.<init>();
        r2 = "ALARM_STATUS";
        r3 = 2;
        r3 = new java.lang.Object[r3];
        r83 = 0;
        r84 = 1;
        r84 = java.lang.Integer.valueOf(r84);
        r3[r83] = r84;
        r83 = 1;
        r84 = 2;
        r84 = java.lang.Integer.valueOf(r84);
        r3[r83] = r84;
        r0 = r70;
        r2 = r0.addNumberIn(r2, r3);
        r3 = "ALARM_ID";
        r83 = 2;
        r0 = r83;
        r0 = new java.lang.Object[r0];
        r83 = r0;
        r84 = 0;
        r85 = 11;
        r85 = java.lang.Integer.valueOf(r85);
        r83[r84] = r85;
        r84 = 1;
        r85 = 12;
        r85 = java.lang.Integer.valueOf(r85);
        r83[r84] = r85;
        r0 = r83;
        r2.addNumberIn(r3, r0);
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "select * from RVM_ALARM_INST";
        r2 = r2.append(r3);
        r3 = "where";
        r0 = r70;
        r3 = r0.toSqlWhere(r3);
        r2 = r2.append(r3);
        r2 = r2.toString();
        r0 = r39;
        r32 = r0.getCommTable(r2);
        r53 = 0;
    L_0x069f:
        r2 = r32.getRecordCount();
        r0 = r53;
        if (r0 >= r2) goto L_0x06da;
    L_0x06a7:
        r0 = r32;
        r1 = r53;
        r2 = r0.getRecord(r1);
        r3 = "ALARM_ID";
        r2 = r2.get(r3);
        r3 = "11";
        r2 = r2.equals(r3);
        if (r2 == 0) goto L_0x06bf;
    L_0x06bd:
        r44 = 1;
    L_0x06bf:
        r0 = r32;
        r1 = r53;
        r2 = r0.getRecord(r1);
        r3 = "ALARM_ID";
        r2 = r2.get(r3);
        r3 = "12";
        r2 = r2.equals(r3);
        if (r2 == 0) goto L_0x06d7;
    L_0x06d5:
        r45 = 1;
    L_0x06d7:
        r53 = r53 + 1;
        goto L_0x069f;
    L_0x06da:
        if (r45 != 0) goto L_0x078c;
    L_0x06dc:
        if (r52 == 0) goto L_0x078c;
    L_0x06de:
        r2 = "TSD_VALUE";
        r0 = r52;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r2 = java.lang.Double.parseDouble(r2);
        r0 = r59;
        r0 = (double) r0;
        r83 = r0;
        r2 = (r2 > r83 ? 1 : (r2 == r83 ? 0 : -1));
        if (r2 <= 0) goto L_0x078c;
    L_0x06f5:
        r2 = "TSD_VALUE";
        r0 = r52;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r2 = java.lang.Double.parseDouble(r2);
        r0 = r77;
        r0 = (double) r0;
        r83 = r0;
        r2 = (r2 > r83 ? 1 : (r2 == r83 ? 0 : -1));
        if (r2 > 0) goto L_0x078c;
    L_0x070c:
        r2 = "ALARM_INST_ID";
        r0 = r40;
        r8 = r0.getSeq(r2);
        r60 = new com.incomrecycle.common.sqlite.SqlInsertBuilder;
        r2 = "RVM_ALARM_INST";
        r0 = r60;
        r0.<init>(r2);
        r2 = r60.newInsertRecord();
        r3 = "ALARM_INST_ID";
        r2 = r2.setNumber(r3, r8);
        r3 = "ALARM_TYPE";
        r83 = "0";
        r0 = r83;
        r2 = r2.setNumber(r3, r0);
        r3 = "ALARM_TIME";
        r0 = r78;
        r2 = r2.setDateTime(r3, r0);
        r3 = "ALARM_ID";
        r83 = 12;
        r83 = java.lang.Integer.valueOf(r83);
        r0 = r83;
        r2 = r2.setNumber(r3, r0);
        r3 = "UPLOAD_FLAG";
        r83 = "0";
        r0 = r83;
        r2 = r2.setNumber(r3, r0);
        r3 = "ALARM_STATUS";
        r83 = 1;
        r83 = java.lang.Integer.valueOf(r83);
        r0 = r83;
        r2.setNumber(r3, r0);
        r0 = r58;
        r1 = r60;
        r0.add(r1);
        r47 = new java.util.HashMap;
        r47.<init>();
        r2 = "TYPE";
        r3 = "RVM_ALARM_INST";
        r0 = r47;
        r0.put(r2, r3);
        r2 = "ALARM_INST_ID";
        r0 = r47;
        r0.put(r2, r8);
        r2 = "ALARM_TYPE";
        r3 = "0";
        r0 = r47;
        r0.put(r2, r3);
        r0 = r55;
        r1 = r47;
        r0.add(r1);
        r45 = 1;
    L_0x078c:
        if (r44 != 0) goto L_0x0914;
    L_0x078e:
        if (r49 == 0) goto L_0x0914;
    L_0x0790:
        r2 = "TSD_VALUE";
        r0 = r49;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r2 = java.lang.Double.parseDouble(r2);
        r0 = r59;
        r0 = (double) r0;
        r83 = r0;
        r2 = (r2 > r83 ? 1 : (r2 == r83 ? 0 : -1));
        if (r2 <= 0) goto L_0x0914;
    L_0x07a7:
        r2 = "TSD_VALUE";
        r0 = r49;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r2 = java.lang.Double.parseDouble(r2);
        r0 = r77;
        r0 = (double) r0;
        r83 = r0;
        r2 = (r2 > r83 ? 1 : (r2 == r83 ? 0 : -1));
        if (r2 > 0) goto L_0x0914;
    L_0x07be:
        r75 = new com.incomrecycle.common.sqlite.SqlWhereBuilder;
        r75.<init>();
        r2 = "ALARM_STATUS";
        r3 = 2;
        r3 = new java.lang.Object[r3];
        r83 = 0;
        r84 = 1;
        r84 = java.lang.Integer.valueOf(r84);
        r3[r83] = r84;
        r83 = 1;
        r84 = 2;
        r84 = java.lang.Integer.valueOf(r84);
        r3[r83] = r84;
        r0 = r75;
        r2 = r0.addNumberIn(r2, r3);
        r3 = "ALARM_ID";
        r83 = 12;
        r83 = java.lang.Integer.valueOf(r83);
        r0 = r83;
        r2.addNumberEqualsTo(r3, r0);
        r64 = new com.incomrecycle.common.sqlite.SqlUpdateBuilder;
        r2 = "RVM_ALARM_INST";
        r0 = r64;
        r0.<init>(r2);
        r2 = "ALARM_STATUS";
        r3 = 4;
        r3 = java.lang.Integer.valueOf(r3);
        r0 = r64;
        r2 = r0.setNumber(r2, r3);
        r3 = "UPLOAD_FLAG";
        r83 = "0";
        r0 = r83;
        r3 = r2.setNumber(r3, r0);
        r83 = "USER_STAFF_ID";
        r2 = "USER_STAFF_ID";
        r2 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r2);
        r2 = (java.lang.String) r2;
        r0 = r83;
        r2 = r3.setNumber(r0, r2);
        r3 = "START_OPER_DATE";
        r83 = new java.util.Date;
        r83.<init>();
        r0 = r83;
        r2 = r2.setTime(r3, r0);
        r0 = r75;
        r2.setSqlWhere(r0);
        r0 = r58;
        r1 = r64;
        r0.add(r1);
        r47 = 0;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "select * from RVM_ALARM_INST";
        r2 = r2.append(r3);
        r3 = "where";
        r0 = r75;
        r3 = r0.toSqlWhere(r3);
        r2 = r2.append(r3);
        r2 = r2.toString();
        r0 = r39;
        r36 = r0.getCommTable(r2);
        r53 = 0;
    L_0x085d:
        r2 = r36.getRecordCount();
        r0 = r53;
        if (r0 >= r2) goto L_0x0894;
    L_0x0865:
        r0 = r36;
        r1 = r53;
        r37 = r0.getRecord(r1);
        r47 = new java.util.HashMap;
        r47.<init>();
        r2 = "TYPE";
        r3 = "RVM_ALARM_INST";
        r0 = r47;
        r0.put(r2, r3);
        r2 = "ALARM_INST_ID";
        r3 = "ALARM_INST_ID";
        r0 = r37;
        r3 = r0.get(r3);
        r0 = r47;
        r0.put(r2, r3);
        r0 = r55;
        r1 = r47;
        r0.add(r1);
        r53 = r53 + 1;
        goto L_0x085d;
    L_0x0894:
        r2 = "ALARM_INST_ID";
        r0 = r40;
        r8 = r0.getSeq(r2);
        r60 = new com.incomrecycle.common.sqlite.SqlInsertBuilder;
        r2 = "RVM_ALARM_INST";
        r0 = r60;
        r0.<init>(r2);
        r2 = r60.newInsertRecord();
        r3 = "ALARM_INST_ID";
        r2 = r2.setNumber(r3, r8);
        r3 = "ALARM_TYPE";
        r83 = "0";
        r0 = r83;
        r2 = r2.setNumber(r3, r0);
        r3 = "ALARM_TIME";
        r0 = r78;
        r2 = r2.setDateTime(r3, r0);
        r3 = "ALARM_ID";
        r83 = 11;
        r83 = java.lang.Integer.valueOf(r83);
        r0 = r83;
        r2 = r2.setNumber(r3, r0);
        r3 = "UPLOAD_FLAG";
        r83 = "0";
        r0 = r83;
        r2 = r2.setNumber(r3, r0);
        r3 = "ALARM_STATUS";
        r83 = 1;
        r83 = java.lang.Integer.valueOf(r83);
        r0 = r83;
        r2.setNumber(r3, r0);
        r0 = r58;
        r1 = r60;
        r0.add(r1);
        r47 = new java.util.HashMap;
        r47.<init>();
        r2 = "TYPE";
        r3 = "RVM_ALARM_INST";
        r0 = r47;
        r0.put(r2, r3);
        r2 = "ALARM_INST_ID";
        r0 = r47;
        r0.put(r2, r8);
        r2 = "ALARM_TYPE";
        r3 = "0";
        r0 = r47;
        r0.put(r2, r3);
        r0 = r55;
        r1 = r47;
        r0.add(r1);
        r44 = 1;
    L_0x0914:
        r0 = r76;
        r1 = r58;
        com.incomrecycle.common.sqlite.SQLiteExecutor.execSqlBuilder(r0, r1);
        r2 = "TOTAL_BAR_CODE_AMOUNT";
        com.incomrecycle.prms.rvm.service.ServiceGlobal.setCurrentSession(r2, r13);
        r2 = "TOTAL_BAR_CODE_COUNT";
        com.incomrecycle.prms.rvm.service.ServiceGlobal.setCurrentSession(r2, r14);
        r2 = "TOTAL_BAR_CODE_VOL";
        com.incomrecycle.prms.rvm.service.ServiceGlobal.setCurrentSession(r2, r15);
        r53 = 0;
    L_0x092c:
        r2 = r55.size();
        r0 = r53;
        if (r0 >= r2) goto L_0x0944;
    L_0x0934:
        r0 = r55;
        r1 = r53;
        r2 = r0.get(r1);
        r2 = (java.util.HashMap) r2;
        com.incomrecycle.prms.rvm.service.task.action.RCCInstanceTask.addTask(r2);
        r53 = r53 + 1;
        goto L_0x092c;
    L_0x0944:
        r2 = "RECYCLED_BOTTLE_DETAIL";
        r56 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r2);
        r56 = (java.util.List) r56;
        if (r56 != 0) goto L_0x095a;
    L_0x094e:
        r56 = new java.util.ArrayList;
        r56.<init>();
        r2 = "RECYCLED_BOTTLE_DETAIL";
        r0 = r56;
        com.incomrecycle.prms.rvm.service.ServiceGlobal.setCurrentSession(r2, r0);
    L_0x095a:
        r0 = r56;
        r1 = r18;
        r0.add(r1);
        r2 = "RECYCLED_BOTTLE_SUMMARY";
        r54 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getCurrentSession(r2);
        r54 = (java.util.List) r54;
        if (r54 != 0) goto L_0x0977;
    L_0x096b:
        r54 = new java.util.ArrayList;
        r54.<init>();
        r2 = "RECYCLED_BOTTLE_SUMMARY";
        r0 = r54;
        com.incomrecycle.prms.rvm.service.ServiceGlobal.setCurrentSession(r2, r0);
    L_0x0977:
        r79 = 0;
        r50 = 0;
        r53 = 0;
    L_0x097d:
        r2 = r54.size();
        r0 = r53;
        if (r0 >= r2) goto L_0x09b6;
    L_0x0985:
        r0 = r54;
        r1 = r53;
        r48 = r0.get(r1);
        r48 = (java.util.HashMap) r48;
        r2 = "BOTTLE_COUNT";
        r0 = r48;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r2 = java.lang.Integer.parseInt(r2);
        r79 = r79 + r2;
        r2 = "BOTTLE_BAR_CODE";
        r0 = r48;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r0 = r18;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x09b3;
    L_0x09b1:
        r50 = r48;
    L_0x09b3:
        r53 = r53 + 1;
        goto L_0x097d;
    L_0x09b6:
        r79 = r79 + 1;
        if (r50 == 0) goto L_0x0a2a;
    L_0x09ba:
        r3 = "BOTTLE_COUNT";
        r2 = "BOTTLE_COUNT";
        r0 = r50;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r2 = java.lang.Integer.parseInt(r2);
        r2 = r2 + 1;
        r2 = java.lang.Integer.toString(r2);
        r0 = r50;
        r0.put(r3, r2);
        r3 = "VENDING_BOTTLE_COUNT";
        r2 = "VENDING_BOTTLE_COUNT";
        r0 = r50;
        r2 = r0.get(r2);
        r2 = (java.lang.String) r2;
        r2 = java.lang.Integer.parseInt(r2);
        r2 = r2 + 1;
        r2 = java.lang.Integer.toString(r2);
        r0 = r50;
        r0.put(r3, r2);
    L_0x09f0:
        r46 = new java.util.HashMap;
        r46.<init>();
        r2 = "TYPE";
        r3 = "CurrentActivity";
        r0 = r46;
        r0.put(r2, r3);
        r2 = "EVENT";
        r3 = "INFORM";
        r0 = r46;
        r0.put(r2, r3);
        if (r44 == 0) goto L_0x0a67;
    L_0x0a09:
        r2 = com.incomrecycle.prms.rvm.service.comm.CommService.getCommService();
        r3 = "RECYCLE_PAUSE";
        r83 = 0;
        r0 = r83;
        r2.execute(r3, r0);
        r2 = "INFORM";
        r3 = "REACH_MAX_BOTTLES";
        r0 = r46;
        r0.put(r2, r3);
        r2 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getGUIEventMgr();
        r0 = r46;
        r2.addEvent(r0);
    L_0x0a28:
        r2 = 0;
        return r2;
    L_0x0a2a:
        r50 = new java.util.HashMap;
        r50.<init>();
        r2 = "BOTTLE_BAR_CODE";
        r0 = r50;
        r1 = r18;
        r0.put(r2, r1);
        r2 = "BOTTLE_COUNT";
        r3 = "1";
        r0 = r50;
        r0.put(r2, r3);
        r2 = "VENDING_BOTTLE_COUNT";
        r3 = "1";
        r0 = r50;
        r0.put(r2, r3);
        r2 = "BOTTLE_AMOUNT";
        r0 = r50;
        r0.put(r2, r9);
        r2 = "BOTTLE_VOL";
        r0 = r50;
        r0.put(r2, r4);
        r2 = "BOTTLE_STUFF";
        r0 = r50;
        r0.put(r2, r5);
        r0 = r54;
        r1 = r50;
        r0.add(r1);
        goto L_0x09f0;
    L_0x0a67:
        r2 = "MAX.BOTTLES.PER.OPT";
        r2 = com.incomrecycle.common.SysConfig.get(r2);
        r2 = java.lang.Integer.parseInt(r2);
        r0 = r79;
        if (r0 >= r2) goto L_0x0aa9;
    L_0x0a75:
        r2 = "TRUE";
        r3 = "COM.PLC.HAS.ROLLER";
        r3 = com.incomrecycle.common.SysConfig.get(r3);
        r2 = r2.equalsIgnoreCase(r3);
        if (r2 == 0) goto L_0x0a96;
    L_0x0a83:
        r2 = com.incomrecycle.common.task.DelayTask.getDelayTask();
        r3 = new com.incomrecycle.prms.rvm.service.commonservice.CommEventYC103CommonService$1;
        r0 = r86;
        r3.<init>();
        r83 = 3000; // 0xbb8 float:4.204E-42 double:1.482E-320;
        r0 = r83;
        r2.addDelayTask(r3, r0);
        goto L_0x0a28;
    L_0x0a96:
        r2 = "INFORM";
        r3 = "BOTTLE_ACCEPT_FINISH";
        r0 = r46;
        r0.put(r2, r3);
        r2 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getGUIEventMgr();
        r0 = r46;
        r2.addEvent(r0);
        goto L_0x0a28;
    L_0x0aa9:
        r2 = com.incomrecycle.prms.rvm.service.comm.CommService.getCommService();
        r3 = "RECYCLE_PAUSE";
        r83 = 0;
        r0 = r83;
        r2.execute(r3, r0);
        r2 = "INFORM";
        r3 = "REACH_MAX_BOTTLES_PER_OPT";
        r0 = r46;
        r0.put(r2, r3);
        r2 = com.incomrecycle.prms.rvm.service.ServiceGlobal.getGUIEventMgr();
        r0 = r46;
        r2.addEvent(r0);
        goto L_0x0a28;
    L_0x0aca:
        r2 = move-exception;
        goto L_0x02a7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.incomrecycle.prms.rvm.service.commonservice.CommEventYC103CommonService.plcThirdLightOn(java.lang.String, java.lang.String, java.util.HashMap):java.util.HashMap");
    }

    public HashMap plcFirstLightOnBackward(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "INFORM");
        hsmpGUIEvent.put("INFORM", "BOTTLE_REJECT_FINISH");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap plcSecondLightOnBackward(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "INFORM");
        hsmpGUIEvent.put("INFORM", "BOTTLE_STATE_EXCEPTION");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap plcThirdLightOnBackward(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        String barCode = (String) ServiceGlobal.getCurrentSession("CURRENT_BAR_CODE");
        String VENDING_WAY = (String) ServiceGlobal.getCurrentSession(AllAdvertisement.VENDING_WAY);
        String OPT_ID = (String) ServiceGlobal.getCurrentSession("OPT_ID");
        if (OPT_ID == null) {
            HashMap<String, String> hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
            hsmpGUIEvent.put("EVENT", "INFORM");
            hsmpGUIEvent.put("INFORM", "BOTTLE_STATE_EXCEPTION");
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
            return null;
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        SqlWhereBuilder sqlWhereBuilderRvmOpt = new SqlWhereBuilder();
        sqlWhereBuilderRvmOpt.addNumberEqualsTo("OPT_ID", OPT_ID);
        if (dbQuery.getCommTable("select * from RVM_OPT" + sqlWhereBuilderRvmOpt.toSqlWhere("where")).getRecordCount() == 0) {
            HashMap hsmpGUIEvent = new HashMap();
            hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
            hsmpGUIEvent.put("EVENT", "INFORM");
            hsmpGUIEvent.put("INFORM", "BOTTLE_STATE_EXCEPTION");
            ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
            return null;
        }
        int i;
        CommTable commTableRvmOptBottle = dbQuery.getCommTable("select * from RVM_OPT_BOTTLE" + sqlWhereBuilderRvmOpt.toSqlWhere("where"));
        CommTableRecord ctrChoiced = null;
        for (i = 0; i < commTableRvmOptBottle.getRecordCount(); i++) {
            CommTableRecord ctr = commTableRvmOptBottle.getRecord(i);
            if (!"0".equals(ctr.get("BOTTLE_COUNT"))) {
                if (ctrChoiced == null) {
                    ctrChoiced = ctr;
                } else {
                    if (Double.parseDouble(ctrChoiced.get("BOTTLE_AMOUNT")) < Double.parseDouble(ctr.get("BOTTLE_AMOUNT"))) {
                        ctrChoiced = ctr;
                    }
                }
                if (!StringUtils.isBlank(barCode) && barCode.equals(ctr.get("BOTTLE_BAR_CODE"))) {
                    ctrChoiced = ctr;
                    break;
                }
            }
        }
        if (ctrChoiced == null) {
            return null;
        }
        String BAR_CODE_VOL = "0";
        String BAR_CODE_AMOUNT = "0";
        String TOTAL_BAR_CODE_AMOUNT = (String) ServiceGlobal.getCurrentSession("TOTAL_BAR_CODE_AMOUNT");
        String TOTAL_BAR_CODE_COUNT = (String) ServiceGlobal.getCurrentSession("TOTAL_BAR_CODE_COUNT");
        String TOTAL_BAR_CODE_VOL = (String) ServiceGlobal.getCurrentSession("TOTAL_BAR_CODE_VOL");
        if (TOTAL_BAR_CODE_AMOUNT == null) {
            TOTAL_BAR_CODE_AMOUNT = "0";
        }
        BAR_CODE_VOL = ctrChoiced.get("BOTTLE_VOL");
        BAR_CODE_AMOUNT = ctrChoiced.get("BOTTLE_AMOUNT");
        TOTAL_BAR_CODE_COUNT = Integer.toString(Integer.parseInt(TOTAL_BAR_CODE_COUNT) - 1);
        TOTAL_BAR_CODE_VOL = Double.toString(Double.parseDouble(TOTAL_BAR_CODE_VOL) - Double.parseDouble(BAR_CODE_VOL));
        TOTAL_BAR_CODE_AMOUNT = Double.toString(Double.parseDouble(TOTAL_BAR_CODE_AMOUNT) - Double.parseDouble(BAR_CODE_AMOUNT));
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        Date tDate = new Date();
        SqlWhereBuilder sqlWhereBuilderRvmOptBottle = new SqlWhereBuilder();
        sqlWhereBuilderRvmOptBottle.addNumberEqualsTo("OPT_ID", OPT_ID).addStringEqualsTo("BOTTLE_BAR_CODE", ctrChoiced.get("BOTTLE_BAR_CODE"));
        SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
        sqlUpdateBuilder.addNumber("PRODUCT_AMOUNT", "-1").addNumber("PROFIT_AMOUNT", SimpleFormatter.DEFAULT_DELIMITER + BAR_CODE_AMOUNT).setSqlWhere(sqlWhereBuilderRvmOpt);
        listSqlBuilder.add(sqlUpdateBuilder);
        sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT_BOTTLE");
        sqlUpdateBuilder.addNumber("BOTTLE_COUNT", "-1").addNumber("VENDING_BOTTLE_COUNT", "-1").setSqlWhere(sqlWhereBuilderRvmOptBottle);
        listSqlBuilder.add(sqlUpdateBuilder);
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "STORAGE_CURR_COUNT");
        CommTable commTableRvmSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        if (commTableRvmSysCode.getRecordCount() > 0) {
            int storageCurrCount = Integer.parseInt(commTableRvmSysCode.getRecord(0).get("SYS_CODE_VALUE")) - 1;
            sqlUpdateBuilder = new SqlUpdateBuilder("RVM_SYS_CODE");
            sqlUpdateBuilder.setString("SYS_CODE_VALUE", Integer.toString(storageCurrCount)).setSqlWhere(sqlWhereBuilderRvmSysCode);
            listSqlBuilder.add(sqlUpdateBuilder);
        }
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        ServiceGlobal.setCurrentSession("TOTAL_BAR_CODE_AMOUNT", TOTAL_BAR_CODE_AMOUNT);
        ServiceGlobal.setCurrentSession("TOTAL_BAR_CODE_COUNT", TOTAL_BAR_CODE_COUNT);
        ServiceGlobal.setCurrentSession("TOTAL_BAR_CODE_VOL", TOTAL_BAR_CODE_VOL);
        List<String> listRecycledBottleDetail = (List) ServiceGlobal.getCurrentSession("RECYCLED_BOTTLE_DETAIL");
        for (i = listRecycledBottleDetail.size() - 1; i >= 0; i--) {
            if (((String) listRecycledBottleDetail.get(i)).equals(ctrChoiced.get("BOTTLE_BAR_CODE"))) {
                listRecycledBottleDetail.remove(i);
                break;
            }
        }
        List<HashMap<String, String>> listHsmpRecycleBottle = (List) ServiceGlobal.getCurrentSession("RECYCLED_BOTTLE_SUMMARY");
        if (listHsmpRecycleBottle == null) {
            listHsmpRecycleBottle = new ArrayList();
            ServiceGlobal.setCurrentSession("RECYCLED_BOTTLE_SUMMARY", listHsmpRecycleBottle);
        }
        HashMap<String, String> hsmpRecycleBottle = null;
        for (i = 0; i < listHsmpRecycleBottle.size(); i++) {
            hsmpRecycleBottle = (HashMap) listHsmpRecycleBottle.get(i);
            if (((String) hsmpRecycleBottle.get("BOTTLE_BAR_CODE")).equals(ctrChoiced.get("BOTTLE_BAR_CODE"))) {
                break;
            }
            hsmpRecycleBottle = null;
        }
        if (hsmpRecycleBottle != null) {
            hsmpRecycleBottle.put("BOTTLE_COUNT", Integer.toString(Integer.parseInt((String) hsmpRecycleBottle.get("BOTTLE_COUNT")) - 1));
            hsmpRecycleBottle.put("VENDING_BOTTLE_COUNT", Integer.toString(Integer.parseInt((String) hsmpRecycleBottle.get("VENDING_BOTTLE_COUNT")) - 1));
        }
       HashMap hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "INFORM");
        hsmpGUIEvent.put("INFORM", "BOTTLE_STATE_EXCEPTION");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap plcError(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:PLC_ERROR", "TRUE");
        raiseAlarm(Integer.toString(AlarmId.RVM_ERROR), "PLC_ERROR");
        return null;
    }

    public HashMap plcErrorRecovery(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:PLC_ERROR", "FALSE");
        recoverAlarm(Integer.toString(AlarmId.RVM_ERROR), "PLC_ERROR_RECOVERY");
        return null;
    }

    public HashMap plcCommError(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:PLC_COMM_ERROR", "TRUE");
        SysConfig.set("STATE:PLC_ERROR", "TRUE");
        raiseAlarm(Integer.toString(AlarmId.PLC_COMM_ERROR), "PLC_COMM_ERROR");
        return null;
    }

    public HashMap plcCommErrorRecovery(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:PLC_COMM_ERROR", "FALSE");
        if (!"TRUE".equalsIgnoreCase(SysConfig.get("STATE:PLC_DOOR_ERROR"))) {
            SysConfig.set("STATE:PLC_ERROR", "FALSE");
        }
        recoverAlarm(Integer.toString(AlarmId.PLC_COMM_ERROR), "PLC_COMM_ERROR_RECOVERY");
        return null;
    }

    public HashMap plcDoorError(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:PLC_DOOR_ERROR", "TRUE");
        SysConfig.set("STATE:PLC_ERROR", "TRUE");
        raiseAlarm(Integer.toString(AlarmId.PLC_DOOR_ERROR), "PLC_DOOR_ERROR");
        return null;
    }

    public HashMap plcDoorErrorRecovery(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:PLC_DOOR_ERROR", "FALSE");
        if (!"TRUE".equalsIgnoreCase(SysConfig.get("STATE:PLC_COMM_ERROR"))) {
            SysConfig.set("STATE:PLC_ERROR", "FALSE");
        }
        recoverAlarm(Integer.toString(AlarmId.PLC_DOOR_ERROR), "PLC_DOOR_ERROR_RECOVERY");
        return null;
    }

    public HashMap printerError(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:PRINTER_ERROR", "TRUE");
        raiseAlarm(Integer.toString(AlarmId.PRINTER_ERROR), "PRINTER_ERROR");
        return null;
    }

    public HashMap printerErrorRecovery(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:PRINTER_ERROR", "FALSE");
        recoverAlarm(Integer.toString(AlarmId.PRINTER_ERROR), "PRINTER_ERROR_RECOVERY");
        return null;
    }

    public HashMap printerNoPaper(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:PRINTER_NO_PAPER", "TRUE");
        raiseAlarm(Integer.toString(AlarmId.PRINTER_NO_PAPER), "PRINTER_NO_PAPER");
        return null;
    }

    public HashMap printerNoPaperRecovery(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:PRINTER_NO_PAPER", "FALSE");
        recoverAlarm(Integer.toString(AlarmId.PRINTER_NO_PAPER), "PRINTER_NO_PAPER_RECOVERY");
        return null;
    }

    public HashMap barcodeReaderError(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:BARCODE_SCANER_ERROR", "TRUE");
        raiseAlarm(Integer.toString(AlarmId.BARCODE_SCANER_ERROR), "BARCODE_SCANER_ERROR");
        return null;
    }

    public HashMap barcodeReaderErrorRecovery(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        SysConfig.set("STATE:BARCODE_SCANER_ERROR", "FALSE");
        recoverAlarm(Integer.toString(AlarmId.BARCODE_SCANER_ERROR), "BARCODE_SCANER_ERROR_RECOVERY");
        return null;
    }

    public HashMap magneticCardNum(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "MAGNETIC_CARD_NUM");
        hsmpGUIEvent.put("MAGNETIC_CARD_NUM", data);
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap huilifeCardNum(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "HUILIFE_CARD_NUM");
        hsmpGUIEvent.put("HUILIFE_CARD_NUM", data);
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap plcNoFirstLight(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "INFORM");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap plcNoSecondLight(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "INFORM");
        hsmpGUIEvent.put("INFORM", "RESET");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap plcNoThirdLight(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String data = (String) hsmpParam.get("data");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "INFORM");
        hsmpGUIEvent.put("INFORM", "RESET");
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap check(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String type = (String) hsmpParam.get("type");
        String msg = (String) hsmpParam.get("msg");
        String comm = (String) hsmpParam.get("comm");
        HashMap<String, String> hsmpGUIEvent = new HashMap();
        hsmpGUIEvent.put(AllAdvertisement.MEDIA_TYPE, "CurrentActivity");
        hsmpGUIEvent.put("EVENT", "CMD");
        hsmpGUIEvent.put("CMD", comm);
        hsmpGUIEvent.put("MSG", msg);
        ServiceGlobal.getGUIEventMgr().addEvent(hsmpGUIEvent);
        return null;
    }

    public HashMap oneCardDrvError(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        raiseAlarm(Integer.toString(AlarmId.DO_READER_ERROR), null);
        return null;
    }

    public HashMap oneCardDrvErrorRecovery(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        recoverAlarm(Integer.toString(AlarmId.DO_READER_ERROR), null);
        return null;
    }
}
