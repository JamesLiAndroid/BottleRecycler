package com.incomrecycle.prms.rvm.service.commonservice.gui;

import android.database.sqlite.SQLiteDatabase;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlBuilder;
import com.incomrecycle.common.sqlite.SqlDeleteBuilder;
import com.incomrecycle.common.sqlite.SqlInsertBuilder;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.NumberUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AlarmId;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.common.SysDef.CardType;
import com.incomrecycle.prms.rvm.common.SysDef.MsgType;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.gui.GUIGlobal;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardCommEntity.ErrorCode;
import com.incomrecycle.prms.rvm.service.commonservice.BaseAppCommonService;
import com.incomrecycle.prms.rvm.service.task.action.RCCInstanceTask;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class GUITrafficCardCommonService extends BaseAppCommonService {
    private static final String SAM_NO_REGIST_LONG = "04";
    private static final String SAM_NO_REGIST_SHORT = "4";

    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if ("recycleEnd".equalsIgnoreCase(subSvnName)) {
            return recycleEnd(svcName, subSvnName, hsmpParam);
        }
        if ("readOneCard".equalsIgnoreCase(subSvnName)) {
            return readCard(svcName, subSvnName, hsmpParam);
        }
        if ("verifyOneCard".equalsIgnoreCase(subSvnName)) {
            return verifyOneCard(svcName, subSvnName, hsmpParam);
        }
        if ("rechargeOneCard".equalsIgnoreCase(subSvnName)) {
            return rechargeOneCard(svcName, subSvnName, hsmpParam);
        }
        if ("transactionRecord".equalsIgnoreCase(subSvnName)) {
            return transactionRecord(svcName, subSvnName, hsmpParam);
        }
        if ("consumeRecord".equalsIgnoreCase(subSvnName)) {
            return consumeRecord(svcName, subSvnName, hsmpParam);
        }
        if ("writeTrans".equalsIgnoreCase(subSvnName)) {
            return writeTrans(svcName, subSvnName, hsmpParam);
        }
        if ("queryOneCardBalance".equalsIgnoreCase(subSvnName)) {
            return queryOneCardBalance(svcName, subSvnName, hsmpParam);
        }
        if ("bindOneCard".equalsIgnoreCase(subSvnName)) {
            return bindOneCard(svcName, subSvnName, hsmpParam);
        }
        if ("verifyBindPhone".equalsIgnoreCase(subSvnName)) {
            return verifyBindPhone(svcName, subSvnName, hsmpParam);
        }
        if ("ChargeWriteBack".equalsIgnoreCase(subSvnName)) {
            return chargeWriteBack(svcName, subSvnName, hsmpParam);
        }
        if ("ChargeScrashCard".equalsIgnoreCase(subSvnName)) {
            return chargeScrashCard(svcName, subSvnName, hsmpParam);
        }
        if ("QueryQuickCard".equalsIgnoreCase(subSvnName)) {
            return queryQuickCard(svcName, subSvnName, hsmpParam);
        }
        if ("ChargeQuickCard".equalsIgnoreCase(subSvnName)) {
            return chargeQuickCard(svcName, subSvnName, hsmpParam);
        }
        if ("QueryGGK".equalsIgnoreCase(subSvnName)) {
            return queryGGK(svcName, subSvnName, hsmpParam);
        }
        if ("ChargeGGK".equalsIgnoreCase(subSvnName)) {
            return chargeGGK(svcName, subSvnName, hsmpParam);
        }
        return null;
    }

    private HashMap chargeGGK(String svcName, String subSvnName, HashMap hsmpParam) {
        if (hsmpParam == null || hsmpParam.size() < 1) {
            return null;
        }
        try {
            Date d = new Date();
            Long Time = Long.valueOf(d.getTime());
            String Datetime = DateUtils.formatDatetime(d);
            String cardNo = (String) ServiceGlobal.getCurrentSession("ONECARD_NUM");
            String beginMoney = (String) ServiceGlobal.getCurrentSession("BALANCE");
            HashMap retPkg = new HashMap();
            if (Integer.parseInt((String) hsmpParam.get("Balance")) + Integer.parseInt(beginMoney) <= 0) {
                retPkg.put("RET_CODE", "NOT_ENOUGH");
                return retPkg;
            }
            HashMap hasp = new HashMap();
            hasp.put("CardNo", cardNo);
            hasp.put("Balance", hsmpParam.get("Balance") + "");
            hasp.put("CardPwd", hsmpParam.get("CardPwd"));
            HashMap hsmpCardInfo = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_CHARGE_SCRASHCARD", JSONUtils.toJSON(hasp)));
            if (hsmpCardInfo != null) {
                if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpCardInfo.get("RESULT"))) {
                    String endMoney = null;
                    HashMap hsmpResult = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_READCARD", null));
                    if (hsmpCardInfo != null) {
                        if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpResult.get("RESULT"))) {
                            endMoney = (String) hsmpResult.get("Balance");
                            if (!StringUtils.isBlank(endMoney)) {
                                ServiceGlobal.setCurrentSession("BALANCE", endMoney);
                            }
                            recoverAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                        }
                    }
                    String POS_COMM_SEQ = (String) ServiceGlobal.getCurrentSession("POS_COMM_SEQ");
                    String ISAM = (String) ServiceGlobal.getCurrentSession("ISAM");
                    String UID_REC = SysConfig.get("RVM.CODE") + "_" + Time;
                    SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
                    SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
                    sqlWhereBuilder.addStringEqualsTo("CARD_NO", cardNo).addStringEqualsTo("POS_COMM_SEQ", POS_COMM_SEQ).addStringEqualsTo("UID_REC", UID_REC);
                    List<SqlBuilder> listSqlBuilder = new ArrayList();
                    SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_BMCHARGE_FEEDBACK");
                    sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
                    listSqlBuilder.add(sqlDeleteBuilder);
                    SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_BMCHARGE_FEEDBACK");
                    sqlInsertBuilder.newInsertRecord().setString("POS_COMM_SEQ", POS_COMM_SEQ).setString("UID_REC", UID_REC).setString("ISAM", ISAM).setString("CARD_NO", cardNo).setNumber("BUSINESS_TYPE", Integer.valueOf(4)).setString("BUSINESS_NO", hsmpParam.get("CardPwd")).setString("CHARGE_TIME", Datetime).setNumber("CHARGE_AMOUNT", Double.valueOf(Double.parseDouble((String) hsmpParam.get("Balance")))).setNumber("BEGIN_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(beginMoney, 0)))).setNumber("END_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(endMoney, 0)))).setString("UPLOAD_FLAG", Integer.valueOf(1));
                    listSqlBuilder.add(sqlInsertBuilder);
                    SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                    HashMap<String, String> hsmpInstanceTask = new HashMap();
                    hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_GGK_FEEDBACK_STS");
                    RCCInstanceTask.addTask(hsmpInstanceTask);
                    retPkg.put("RET_CODE", NetworkUtils.NET_STATE_SUCCESS);
                    return retPkg;
                }
                retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
                String modelErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_RESPONSECODE);
                if (StringUtils.isBlank(modelErrorCode)) {
                    String hostErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_HOSTRESPONSECODE);
                    if (StringUtils.isBlank(hostErrorCode)) {
                        String serverErrorCode = (String) hsmpCardInfo.get(ErrorCode.SERVER_RESPONSE);
                        if (StringUtils.isBlank(serverErrorCode)) {
                            String rvmErrorCode = (String) hsmpCardInfo.get(ErrorCode.ERRCODE);
                            if (StringUtils.isBlank(rvmErrorCode)) {
                                return retPkg;
                            }
                            retPkg.put("RVM_RESPONSECODE", rvmErrorCode);
                            return retPkg;
                        }
                        if ("4".equals(serverErrorCode) || SAM_NO_REGIST_LONG.equals(serverErrorCode)) {
                            raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                        }
                        retPkg.put("SERVER_RESPONSECODE", serverErrorCode);
                        return retPkg;
                    }
                    if ("4".equals(hostErrorCode) || SAM_NO_REGIST_LONG.equals(hostErrorCode)) {
                        raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                    }
                    retPkg.put("HOST_RESPONSECODE", hostErrorCode);
                    return retPkg;
                }
                retPkg.put(ErrorCode.MODEL_RESPONSECODE, modelErrorCode);
                return retPkg;
            }
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
            return retPkg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private HashMap queryGGK(String svcName, String subSvnName, HashMap hsmpParam) {
        try {
            String cardNo = (String) ServiceGlobal.getCurrentSession("ONECARD_NUM");
            if (StringUtils.isBlank(cardNo)) {
                HashMap retMap = new HashMap();
                retMap.put("RET_CODE", "NOT_FOUND_CARD");
                return retMap;
            }
            String MES_TYPE = MsgType.RVM_YKT_GGK;
            Date d = new Date();
            String Datetime = DateUtils.formatDatetime(d);
            Long Time = Long.valueOf(d.getTime());
            String QuTime = Time.toString();
            HashMap hsmpPkg = new HashMap();
            hsmpPkg.put("MES_TYPE", MES_TYPE);
            hsmpPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
            hsmpPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
            hsmpPkg.put("QU_TIME", QuTime);
            hsmpPkg.put("YKT_CARD", cardNo);
            hsmpPkg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + Time);
            HashMap hsmp = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPkg)));
            if (hsmp == null) {
                return null;
            }
            String termNo = (String) hsmp.get("TERM_NO");
            if (MsgType.RCC_YKT_GGK.equalsIgnoreCase((String) hsmp.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo) && cardNo.equals((String) hsmp.get("YKT_CARD"))) {
                return hsmp;
            }
        } catch (Exception e) {
        }
        return null;

    }

    private HashMap chargeQuickCard(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if (hsmpParam == null || hsmpParam.size() < 1) {
            return null;
        }
        try {
            Date d = new Date();
            Long Time = Long.valueOf(d.getTime());
            String Datetime = DateUtils.formatDatetime(d);
            String cardNo = (String) ServiceGlobal.getCurrentSession("ONECARD_NUM");
            String beginMoney = (String) ServiceGlobal.getCurrentSession("BALANCE");
            HashMap retPkg = new HashMap();
            if (Integer.parseInt((String) hsmpParam.get("Balance")) + Integer.parseInt(beginMoney) <= 0) {
                retPkg.put("RET_CODE", "NOT_ENOUGH");
                return retPkg;
            }
            HashMap hasp = new HashMap();
            hasp.put("CardNo", cardNo);
            hasp.put("Balance", hsmpParam.get("Balance"));
            hasp.put("OrderNo", hsmpParam.get("OrderNo"));
            hasp.put("Datetime", Datetime);
            HashMap hsmpCardInfo = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_CHARGE_QUICKCARD", JSONUtils.toJSON(hasp)));
            if (hsmpCardInfo != null) {
                if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpCardInfo.get("RESULT"))) {
                    String endMoney = null;
                    HashMap hsmpResult = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_READCARD", null));
                    if (hsmpCardInfo != null) {
                        retPkg = new HashMap();
                        if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpResult.get("RESULT"))) {
                            endMoney = (String) hsmpResult.get("Balance");
                            if (!StringUtils.isBlank(endMoney)) {
                                ServiceGlobal.setCurrentSession("BALANCE", endMoney);
                            }
                            recoverAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                        }
                    }
                    String POS_COMM_SEQ = (String) ServiceGlobal.getCurrentSession("POS_COMM_SEQ");
                    String ISAM = (String) ServiceGlobal.getCurrentSession("ISAM");
                    String UID_REC = SysConfig.get("RVM.CODE") + "_" + Time;
                    SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
                    SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
                    sqlWhereBuilder.addStringEqualsTo("CARD_NO", cardNo).addStringEqualsTo("POS_COMM_SEQ", POS_COMM_SEQ).addStringEqualsTo("UID_REC", UID_REC);
                    List<SqlBuilder> listSqlBuilder = new ArrayList();
                    SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_BMCHARGE_FEEDBACK");
                    sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
                    listSqlBuilder.add(sqlDeleteBuilder);
                    SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_BMCHARGE_FEEDBACK");
                    sqlInsertBuilder.newInsertRecord().setString("POS_COMM_SEQ", POS_COMM_SEQ).setString("UID_REC", UID_REC).setString("ISAM", ISAM).setString("CARD_NO", cardNo).setNumber("BUSINESS_TYPE", Integer.valueOf(3)).setString("BUSINESS_NO", hsmpParam.get("OrderNo")).setString("CHARGE_TIME", Datetime).setNumber("CHARGE_AMOUNT", Double.valueOf(Double.parseDouble((String) hsmpParam.get("Balance")))).setNumber("BEGIN_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(beginMoney, 0)))).setNumber("END_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(endMoney, 0)))).setString("UPLOAD_FLAG", Integer.valueOf(1));
                    listSqlBuilder.add(sqlInsertBuilder);
                    SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                    HashMap<String, String> hsmpInstanceTask = new HashMap();
                    hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_BMCHARGE_FEEDBACK_STS");
                    RCCInstanceTask.addTask(hsmpInstanceTask);
                    retPkg.put("RET_CODE", NetworkUtils.NET_STATE_SUCCESS);
                    return retPkg;
                }
                retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
                String modelErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_RESPONSECODE);
                if (StringUtils.isBlank(modelErrorCode)) {
                    String hostErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_HOSTRESPONSECODE);
                    if (StringUtils.isBlank(hostErrorCode)) {
                        String serverErrorCode = (String) hsmpCardInfo.get(ErrorCode.SERVER_RESPONSE);
                        if (StringUtils.isBlank(serverErrorCode)) {
                            String rvmErrorCode = (String) hsmpCardInfo.get(ErrorCode.ERRCODE);
                            if (StringUtils.isBlank(rvmErrorCode)) {
                                return retPkg;
                            }
                            retPkg.put("RVM_RESPONSECODE", rvmErrorCode);
                            return retPkg;
                        }
                        if ("4".equals(serverErrorCode) || SAM_NO_REGIST_LONG.equals(serverErrorCode)) {
                            raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                        }
                        retPkg.put("SERVER_RESPONSECODE", serverErrorCode);
                        return retPkg;
                    }
                    if ("4".equals(hostErrorCode) || SAM_NO_REGIST_LONG.equals(hostErrorCode)) {
                        raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                    }
                    retPkg.put("HOST_RESPONSECODE", hostErrorCode);
                    return retPkg;
                }
                retPkg.put(ErrorCode.MODEL_RESPONSECODE, modelErrorCode);
                return retPkg;
            }
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
            return retPkg;
        } catch (Exception e) {
            return null;
        }
    }

    private HashMap queryQuickCard(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String cardNo = (String) ServiceGlobal.getCurrentSession("ONECARD_NUM");
        HashMap hasp = new HashMap();
        hasp.put("CardNo", cardNo);
        HashMap hsmpQuickCardInfo = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_QUERY_QUICKCARD", JSONUtils.toJSON(hasp)));
        HashMap retPkg = new HashMap();
        if (hsmpQuickCardInfo == null) {
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
        } else if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpQuickCardInfo.get("RESULT"))) {
            List listQuickCartInfo = (List) hsmpQuickCardInfo.get("ORDER");
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_SUCCESS);
            retPkg.put("QCINFO", listQuickCartInfo);
            recoverAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
        } else {
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
            String modelErrorCode = (String) hsmpQuickCardInfo.get(ErrorCode.MODEL_RESPONSECODE);
            if (StringUtils.isBlank(modelErrorCode)) {
                String hostErrorCode = (String) hsmpQuickCardInfo.get(ErrorCode.MODEL_HOSTRESPONSECODE);
                if (StringUtils.isBlank(hostErrorCode)) {
                    String serverErrorCode = (String) hsmpQuickCardInfo.get(ErrorCode.SERVER_RESPONSE);
                    if (StringUtils.isBlank(serverErrorCode)) {
                        String rvmErrorCode = (String) hsmpQuickCardInfo.get(ErrorCode.ERRCODE);
                        if (!StringUtils.isBlank(rvmErrorCode)) {
                            retPkg.put("RVM_RESPONSECODE", rvmErrorCode);
                        }
                    } else {
                        if ("4".equals(serverErrorCode) || SAM_NO_REGIST_LONG.equals(serverErrorCode)) {
                            raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                        }
                        retPkg.put("SERVER_RESPONSECODE", serverErrorCode);
                    }
                } else {
                    if ("4".equals(hostErrorCode) || SAM_NO_REGIST_LONG.equals(hostErrorCode)) {
                        raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                    }
                    retPkg.put("HOST_RESPONSECODE", hostErrorCode);
                }
            } else {
                retPkg.put(ErrorCode.MODEL_RESPONSECODE, modelErrorCode);
            }
        }
        return retPkg;
    }

    private HashMap writeTrans(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String Datetime = (String) ServiceGlobal.getCurrentSession("CHARGE_TIME");
        String beginMoney = (String) ServiceGlobal.getCurrentSession("BALANCE");
        HashMap hasp = new HashMap();
        String Amount = (String) hsmpParam.get("AMOUNT");
        if (StringUtils.isBlank(Amount) || 0.0d == Double.parseDouble(Amount)) {
            return null;
        }
        String balance = NumberUtils.toScale((String) hsmpParam.get("AMOUNT"), 0);
        HashMap retPkg = new HashMap();
        if (Integer.parseInt(beginMoney) + Integer.parseInt(balance) <= 0) {
            retPkg.put("RET_CODE", "NOT_ENOUGH");
            return retPkg;
        }
        hasp.put("Balance", balance + "");
        hasp.put("CardNo", hsmpParam.get("ONECARD_NUM"));
        hasp.put("Datetime", Datetime);
        HashMap hsmpCardInfo = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_CHARGE", JSONUtils.toJSON(hasp)));
        String POS_COMM_SEQ = (String) ServiceGlobal.getCurrentSession("POS_COMM_SEQ");
        String ISAM = (String) ServiceGlobal.getCurrentSession("ISAM");
        String UID_REC = (String) ServiceGlobal.getCurrentSession("UID_REC");
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addStringEqualsTo("CARD_NO", (String) hsmpParam.get("ONECARD_NUM")).addStringEqualsTo("POS_COMM_SEQ", POS_COMM_SEQ).addStringEqualsTo("UID_REC", UID_REC);
        List<SqlBuilder> listSqlBuilder;
        SqlDeleteBuilder sqlDeleteBuilder;
        SqlInsertBuilder sqlInsertBuilder;
        HashMap<String, String> hsmpInstanceTask;
        String modelErrorCode;
        String hostErrorCode;
        String serverErrorCode;
        String rvmErrorCode;
        if (hsmpCardInfo != null) {
            if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpCardInfo.get("RESULT"))) {
                String endMoney = null;
                HashMap hsmpResult = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_READCARD", null));
                if (hsmpCardInfo != null) {
                    retPkg = new HashMap();
                    if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpResult.get("RESULT"))) {
                        endMoney = (String) hsmpResult.get("Balance");
                        if (!StringUtils.isBlank(endMoney)) {
                            ServiceGlobal.setCurrentSession("BALANCE", endMoney);
                        }
                        recoverAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                    }
                }
                listSqlBuilder = new ArrayList();
                sqlDeleteBuilder = new SqlDeleteBuilder("RVM_RECHARGE_FEEDBACK");
                sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
                listSqlBuilder.add(sqlDeleteBuilder);
                sqlInsertBuilder = new SqlInsertBuilder("RVM_RECHARGE_FEEDBACK");
                sqlInsertBuilder.newInsertRecord().setString("POS_COMM_SEQ", POS_COMM_SEQ).setString("UID_REC", UID_REC).setString("ISAM", ISAM).setString("CARD_NO", (String) hsmpParam.get("ONECARD_NUM")).setString("CHARGE_TIME", Datetime).setNumber("CHARGE_AMOUNT", Double.valueOf(Double.parseDouble((String) hsmpParam.get("AMOUNT")))).setNumber("BEGIN_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(beginMoney, 0)))).setNumber("END_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(endMoney, 0)))).setString("CHARGE_STATUS", Integer.valueOf(1)).setString("UPLOAD_FLAG", Integer.valueOf(1));
                listSqlBuilder.add(sqlInsertBuilder);
                SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                hsmpInstanceTask = new HashMap();
                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_RECHARGE_FEEDBACK_STS");
                RCCInstanceTask.addTask(hsmpInstanceTask);
                retPkg.put("CARD_NO", hsmpParam.get("ONECARD_NUM"));
                retPkg.put("CHARGE_AMOUNT", Double.valueOf(Double.parseDouble((String) hsmpParam.get("AMOUNT"))));
                retPkg.put("BALANCE", Double.valueOf(Double.parseDouble((String) ServiceGlobal.getCurrentSession("BALANCE"))));
                retPkg.put("RET_CODE", NetworkUtils.NET_STATE_SUCCESS);
                return retPkg;
            }
            listSqlBuilder = new ArrayList();
            sqlDeleteBuilder = new SqlDeleteBuilder("RVM_RECHARGE_FEEDBACK");
            sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
            listSqlBuilder.add(sqlDeleteBuilder);
            sqlInsertBuilder = new SqlInsertBuilder("RVM_RECHARGE_FEEDBACK");
            sqlInsertBuilder.newInsertRecord().setString("POS_COMM_SEQ", POS_COMM_SEQ).setString("UID_REC", UID_REC).setString("ISAM", ISAM).setString("CARD_NO", (String) hsmpParam.get("ONECARD_NUM")).setString("CHARGE_TIME", Datetime).setNumber("CHARGE_AMOUNT", Double.valueOf(Double.parseDouble((String) hsmpParam.get("AMOUNT")))).setNumber("BEGIN_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(beginMoney, 0)))).setNumber("END_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(beginMoney, 0)))).setString("CHARGE_STATUS", Integer.valueOf(0)).setString("UPLOAD_FLAG", Integer.valueOf(1));
            listSqlBuilder.add(sqlInsertBuilder);
            SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
            hsmpInstanceTask = new HashMap();
            hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_RECHARGE_FEEDBACK_STS");
            RCCInstanceTask.addTask(hsmpInstanceTask);
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
            modelErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_RESPONSECODE);
            if (StringUtils.isBlank(modelErrorCode)) {
                hostErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_HOSTRESPONSECODE);
                if (StringUtils.isBlank(hostErrorCode)) {
                    serverErrorCode = (String) hsmpCardInfo.get(ErrorCode.SERVER_RESPONSE);
                    if (StringUtils.isBlank(serverErrorCode)) {
                        rvmErrorCode = (String) hsmpCardInfo.get(ErrorCode.ERRCODE);
                        if (StringUtils.isBlank(rvmErrorCode)) {
                            return retPkg;
                        }
                        retPkg.put("RVM_RESPONSECODE", rvmErrorCode);
                        return retPkg;
                    }
                    if ("4".equals(serverErrorCode) || SAM_NO_REGIST_LONG.equals(serverErrorCode)) {
                        raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                    }
                    retPkg.put("SERVER_RESPONSECODE", serverErrorCode);
                    return retPkg;
                }
                if ("4".equals(hostErrorCode) || SAM_NO_REGIST_LONG.equals(hostErrorCode)) {
                    raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                }
                retPkg.put("HOST_RESPONSECODE", hostErrorCode);
                return retPkg;
            }
            retPkg.put(ErrorCode.MODEL_RESPONSECODE, modelErrorCode);
            return retPkg;
        }
        listSqlBuilder = new ArrayList();
        sqlDeleteBuilder = new SqlDeleteBuilder("RVM_RECHARGE_FEEDBACK");
        sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
        listSqlBuilder.add(sqlDeleteBuilder);
        sqlInsertBuilder = new SqlInsertBuilder("RVM_RECHARGE_FEEDBACK");
        sqlInsertBuilder.newInsertRecord().setString("POS_COMM_SEQ", POS_COMM_SEQ).setString("UID_REC", UID_REC).setString("ISAM", ISAM).setString("CARD_NO", (String) hsmpParam.get("ONECARD_NUM")).setString("CHARGE_TIME", Datetime).setNumber("BEGIN_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(beginMoney, 0)))).setNumber("END_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(beginMoney, 0)))).setNumber("CHARGE_AMOUNT", Double.valueOf(Double.parseDouble((String) hsmpParam.get("AMOUNT")))).setString("CHARGE_STATUS", Integer.valueOf(0)).setString("UPLOAD_FLAG", Integer.valueOf(1));
        listSqlBuilder.add(sqlInsertBuilder);
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
        hsmpInstanceTask = new HashMap();
        hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_RECHARGE_FEEDBACK_STS");
        RCCInstanceTask.addTask(hsmpInstanceTask);
        retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
        modelErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_RESPONSECODE);
        if (StringUtils.isBlank(modelErrorCode)) {
            hostErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_HOSTRESPONSECODE);
            if (StringUtils.isBlank(hostErrorCode)) {
                serverErrorCode = (String) hsmpCardInfo.get(ErrorCode.SERVER_RESPONSE);
                if (StringUtils.isBlank(serverErrorCode)) {
                    rvmErrorCode = hsmpCardInfo.get(ErrorCode.ERRCODE) + "";
                    if (StringUtils.isBlank(rvmErrorCode)) {
                        return retPkg;
                    }
                    retPkg.put("RVM_RESPONSECODE", rvmErrorCode);
                    return retPkg;
                }
                if ("4".equals(serverErrorCode) || SAM_NO_REGIST_LONG.equals(serverErrorCode)) {
                    raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                }
                retPkg.put("SERVER_RESPONSECODE", serverErrorCode);
                return retPkg;
            }
            if ("4".equals(hostErrorCode) || SAM_NO_REGIST_LONG.equals(hostErrorCode)) {
                raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
            }
            retPkg.put("HOST_RESPONSECODE", hostErrorCode);
            return retPkg;
        }
        retPkg.put(ErrorCode.MODEL_RESPONSECODE, modelErrorCode);
        return retPkg;
    }

    private HashMap recycleEnd(String svcName, String subSvnName, HashMap hsmpParam) {
        try {
            String CARD_NO = (String) ServiceGlobal.getCurrentSession("CARD_NO");
            String CARD_TYPE = "TRANSPORTCARD";
            String OPT_ID = (String) ServiceGlobal.getCurrentSession("OPT_ID");
            if (OPT_ID == null) {
                return null;
            }
            String SELECT_FLAG = null;
            HashMap<String, Object> TRANSMIT_ADV = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.HOMEPAGE_LEFT);
            if (TRANSMIT_ADV != null) {
                SELECT_FLAG = (String) ((HashMap) TRANSMIT_ADV.get("TRANSMIT_ADV")).get(AllAdvertisement.SELECT_FLAG);
            } else {
                HashMap mapVendingWayFlag = (HashMap) GUIGlobal.getCurrentSession(AllAdvertisement.VENDING_SELECT_FLAG);
                if (mapVendingWayFlag != null && mapVendingWayFlag.size() > 0) {
                    String VENDING_WAY_SET = (String) mapVendingWayFlag.get(AllAdvertisement.VENDING_WAY_SET);
                    if (VENDING_WAY_SET == null || !VENDING_WAY_SET.contains("TRANSPORTCARD")) {
                        String VENDING_WAY = (String) mapVendingWayFlag.get(AllAdvertisement.VENDING_WAY);
                        if (VENDING_WAY != null && "TRANSPORTCARD".equalsIgnoreCase(VENDING_WAY)) {
                            SELECT_FLAG = (String) mapVendingWayFlag.get(AllAdvertisement.SELECT_FLAG);
                        }
                    } else {
                        SELECT_FLAG = (String) mapVendingWayFlag.get(AllAdvertisement.SELECT_FLAG);
                    }
                }
            }
            SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
            SqlWhereBuilder sqlWhereBuilderRvmOpt = new SqlWhereBuilder();
            sqlWhereBuilderRvmOpt.addNumberEqualsTo("OPT_ID", OPT_ID).addNumberEqualsTo("OPT_STATUS", Integer.valueOf(0));
            SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("RVM_OPT");
            sqlUpdateBuilder.setString(AllAdvertisement.VENDING_WAY, "TRANSPORTCARD").setNumber("OPT_STATUS", Integer.valueOf(1)).setString("CARD_TYPE", CARD_TYPE).setString("CARD_NO", CARD_NO).setString(AllAdvertisement.SELECT_FLAG, SELECT_FLAG).setSqlWhere(sqlWhereBuilderRvmOpt);
            SQLiteExecutor.execSql(sqliteDatabase, sqlUpdateBuilder.toSql());
            HashMap<String, Object> hasmPram = new HashMap();
            hasmPram.put("CARD_NO", CARD_NO);
            hasmPram.put("CARD_TYPE", CARD_TYPE);
            hasmPram.put("OPT_ID", OPT_ID);
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "cutPrice", hasmPram);
            CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "limitBottleCount", hasmPram);
            HashMap hsmpUploadParam = new HashMap();
            hsmpUploadParam.put("OPT_ID", OPT_ID);
            HashMap hsmpUploadResult = CommonServiceHelper.getGUICommonService().execute("GUIRecycleCommonService", "uploadOptDetail", hsmpUploadParam);
            if (hsmpUploadResult != null) {
                if ("repeat".equalsIgnoreCase((String) hsmpUploadResult.get("RESULT"))) {
                    HashMap hsmpStatusParam = new HashMap();
                    hsmpStatusParam.put("CARD_NO", CARD_NO);
                    hsmpStatusParam.put("CARD_TYPE", CARD_TYPE);
                    return new GUIRecycleCommonService().execute("GUIRecycleCommonService", "queryCardStatus", hsmpStatusParam);
                } else if ("success".equalsIgnoreCase((String) hsmpUploadResult.get("RESULT"))) {
                    HashMap hsmpResult = new HashMap();
                    hsmpResult.put("INCOM_AMOUNT", hsmpUploadResult.get("INCOM_AMOUNT"));
                    hsmpResult.put("CARD_STATUS", hsmpUploadResult.get("CARD_STATUS"));
                    hsmpResult.put("RECHARGE", hsmpUploadResult.get("RECHARGE"));
                    hsmpResult.put("IS_RECHANGE", hsmpUploadResult.get("IS_RECHANGE"));
                    hsmpResult.put("BOTTLE_NUM", hsmpUploadResult.get("BOTTLE_NUM"));
                    hsmpResult.put("CREDIT", hsmpUploadResult.get("CREDIT"));
                    hsmpResult.put("TODAY_BOTTLE", hsmpUploadResult.get("TODAY_BOTTLE"));
                    return hsmpResult;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public HashMap readCard(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap hsmpCardInfo = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_READCARDNO", null));
        HashMap retPkg = new HashMap();
        if (hsmpCardInfo == null) {
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
        } else if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpCardInfo.get("RESULT"))) {
            ServiceGlobal.setCurrentSession("ONECARD_NUM", hsmpCardInfo.get("CardNo"));
            ServiceGlobal.setCurrentSession("CARD_NO", hsmpCardInfo.get("CardNo"));
            ServiceGlobal.setCurrentSession("POS_COMM_SEQ", hsmpCardInfo.get("NextPosCommSeq"));
            ServiceGlobal.setCurrentSession("CARD_TYPE", "TRANSPORTCARD");
            ServiceGlobal.setCurrentSession("CARD_CONSUME_RECORD", hsmpCardInfo.get("ConsumeRecordList"));
            ServiceGlobal.setCurrentSession("CARD_CHARGE_RECORD", hsmpCardInfo.get("ChargeRecordList"));
            ServiceGlobal.setCurrentSession("BALANCE", hsmpCardInfo.get("Balance"));
            ServiceGlobal.setCurrentSession("ISAM", hsmpCardInfo.get("ISAM"));
            ServiceGlobal.setCurrentSession("MaxBalance", hsmpCardInfo.get("MaxBalance"));
            retPkg.put("RET_CODE", hsmpCardInfo.get("RESULT"));
            retPkg.put("ONECARD_NUM", hsmpCardInfo.get("CardNo"));
        } else {
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
            String modelErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_RESPONSECODE);
            if (StringUtils.isBlank(modelErrorCode)) {
                String hostErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_HOSTRESPONSECODE);
                if (StringUtils.isBlank(hostErrorCode)) {
                    String serverErrorCode = (String) hsmpCardInfo.get(ErrorCode.SERVER_RESPONSE);
                    if (StringUtils.isBlank(serverErrorCode)) {
                        String rvmErrorCode = (String) hsmpCardInfo.get(ErrorCode.ERRCODE);
                        if (!StringUtils.isBlank(rvmErrorCode)) {
                            retPkg.put("RVM_RESPONSECODE", rvmErrorCode);
                        }
                    } else {
                        retPkg.put("SERVER_RESPONSECODE", serverErrorCode);
                    }
                } else {
                    retPkg.put("HOST_RESPONSECODE", hostErrorCode);
                }
            } else {
                retPkg.put(ErrorCode.MODEL_RESPONSECODE, modelErrorCode);
            }
        }
        return retPkg;
    }

    private HashMap verifyOneCard(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String ONECARD_NUM = (String) hsmpParam.get("ONECARD_NUM");
        if (ONECARD_NUM != null) {
            try {
                HashMap hsmpStatusParam = new HashMap();
                hsmpStatusParam.put("CARD_NO", ONECARD_NUM);
                hsmpStatusParam.put("CARD_TYPE", CardType.NEW_TRANSPORTCARD);
                return new GUIRecycleCommonService().execute("GUIRecycleCommonService", "queryCardStatus", hsmpStatusParam);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private HashMap rechargeOneCard(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String ONECARD_NUM = (String) ServiceGlobal.getCurrentSession("ONECARD_NUM");
        String POS_COMM_SEQ = (String) ServiceGlobal.getCurrentSession("POS_COMM_SEQ");
        String ISAM = (String) ServiceGlobal.getCurrentSession("ISAM");
        String balance = (String) ServiceGlobal.getCurrentSession("BALANCE");
        String MES_TYPE = MsgType.RVM_LTD_RECHANGE;
        Date d = new Date();
        String Datetime = DateUtils.formatDatetime(d);
        Long Time = Long.valueOf(d.getTime());
        String QuTime = Time.toString();
        HashMap hsmpPkg = new HashMap();
        hsmpPkg.put("MES_TYPE", MES_TYPE);
        hsmpPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
        hsmpPkg.put("QU_TIME", QuTime);
        hsmpPkg.put("CHARGE_TIME", Datetime);
        hsmpPkg.put("CARD_NO", ONECARD_NUM);
        hsmpPkg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + Time);
        hsmpPkg.put("UID_REC", SysConfig.get("RVM.CODE") + "_" + Time);
        hsmpPkg.put("POS_COMM_SEQ", POS_COMM_SEQ);
        hsmpPkg.put("ISAM", ISAM);
        hsmpPkg.put("BEGIN_MONEY", balance);
        ServiceGlobal.setCurrentSession("CHARGE_TIME", Datetime);
        ServiceGlobal.setCurrentSession("UID_REC", SysConfig.get("RVM.CODE") + "_" + Time);
        try {
            HashMap hsmp = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPkg)));
            HashMap hsmpRet;
            if (hsmp == null) {
                String UID_REC = (String) ServiceGlobal.getCurrentSession("UID_REC");
                SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
                SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
                sqlWhereBuilder.addStringEqualsTo("CARD_NO", ONECARD_NUM).addStringEqualsTo("POS_COMM_SEQ", POS_COMM_SEQ).addStringEqualsTo("UID_REC", UID_REC);
                List<SqlBuilder> listSqlBuilder = new ArrayList();
                SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_RECHARGE_FEEDBACK");
                sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
                listSqlBuilder.add(sqlDeleteBuilder);
                SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_RECHARGE_FEEDBACK");
                sqlInsertBuilder.newInsertRecord().setString("POS_COMM_SEQ", POS_COMM_SEQ).setString("UID_REC", UID_REC).setString("ISAM", ISAM).setString("CARD_NO", ONECARD_NUM).setString("CHARGE_TIME", Datetime).setNumber("CHARGE_AMOUNT", hsmpParam.get("RECHARGED")).setNumber("BEGIN_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(balance, 0)))).setNumber("END_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(balance, 0)))).setString("CHARGE_STATUS", Integer.valueOf(0)).setString("UPLOAD_FLAG", Integer.valueOf(1));
                listSqlBuilder.add(sqlInsertBuilder);
                SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                HashMap<String, String> hsmpInstanceTask = new HashMap();
                hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_RECHARGE_FEEDBACK_STS");
                RCCInstanceTask.addTask(hsmpInstanceTask);
                hsmpRet = new HashMap();
                hsmpRet.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
                return hsmpRet;
            }
            String termNo = (String) hsmp.get("TERM_NO");
            String areaId = (String) hsmp.get("LOCAL_AREA_ID");
            if (MsgType.RCC_LTD_RECHANGE.equalsIgnoreCase((String) hsmp.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                hsmpRet = new HashMap();
                int card_status = Integer.parseInt((String) hsmp.get("CARD_STATUS"));
                if (2 == card_status) {
                    hsmpRet.put("CARD_NO", (String) hsmp.get("CARD_NO"));
                    hsmpRet.put("AMOUNT", (String) hsmp.get("REAL_AMOUNT"));
                    hsmpRet.put("ONECARD_NUM", (String) hsmp.get("CARD_NO"));
                    hsmpRet.put("RET_CODE", "WRITABLE");
                    return hsmpRet;
                } else if (-2 == card_status) {
                    hsmpRet.put("RET_CODE", "UNBUNDLE");
                    return hsmpRet;
                } else if (-1 == card_status) {
                    hsmpRet.put("RET_CODE", "BLACKLIST");
                    return hsmpRet;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private HashMap transactionRecord(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap retPkg = new HashMap();
        HashMap hsmpCardInfo = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_READCARD", null));
        if (hsmpCardInfo == null) {
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
        } else if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpCardInfo.get("RESULT"))) {
            ServiceGlobal.setCurrentSession("CARD_CONSUME_RECORD", hsmpCardInfo.get("ConsumeRecordList"));
            ServiceGlobal.setCurrentSession("CARD_CHARGE_RECORD", hsmpCardInfo.get("ChargeRecordList"));
            ServiceGlobal.setCurrentSession("BALANCE", hsmpCardInfo.get("Balance"));
            List chargeRecordList = (List) hsmpCardInfo.get("ChargeRecordList");
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_SUCCESS);
            retPkg.put("chargeRecord", chargeRecordList);
        } else {
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
            String modelErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_RESPONSECODE);
            if (StringUtils.isBlank(modelErrorCode)) {
                String hostErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_HOSTRESPONSECODE);
                if (StringUtils.isBlank(hostErrorCode)) {
                    String serverErrorCode = (String) hsmpCardInfo.get(ErrorCode.SERVER_RESPONSE);
                    if (StringUtils.isBlank(serverErrorCode)) {
                        String rvmErrorCode = (String) hsmpCardInfo.get(ErrorCode.ERRCODE);
                        if (!StringUtils.isBlank(rvmErrorCode)) {
                            retPkg.put("RVM_RESPONSECODE", rvmErrorCode);
                        }
                    } else {
                        retPkg.put("SERVER_RESPONSECODE", serverErrorCode);
                    }
                } else {
                    retPkg.put("HOST_RESPONSECODE", hostErrorCode);
                }
            } else {
                retPkg.put(ErrorCode.MODEL_RESPONSECODE, modelErrorCode);
            }
        }
        return retPkg;
    }

    private HashMap consumeRecord(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        HashMap retPkg = new HashMap();
        HashMap hsmpCardInfo = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_READCARD", null));
        if (hsmpCardInfo == null) {
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
        } else if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpCardInfo.get("RESULT"))) {
            ServiceGlobal.setCurrentSession("CARD_CONSUME_RECORD", hsmpCardInfo.get("ConsumeRecordList"));
            ServiceGlobal.setCurrentSession("CARD_CHARGE_RECORD", hsmpCardInfo.get("ChargeRecordList"));
            ServiceGlobal.setCurrentSession("BALANCE", hsmpCardInfo.get("Balance"));
            List consumeRecordList = (List) hsmpCardInfo.get("ConsumeRecordList");
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_SUCCESS);
            retPkg.put("consumeRecord", consumeRecordList);
        } else {
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
            String modelErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_RESPONSECODE);
            if (StringUtils.isBlank(modelErrorCode)) {
                String hostErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_HOSTRESPONSECODE);
                if (StringUtils.isBlank(hostErrorCode)) {
                    String serverErrorCode = (String) hsmpCardInfo.get(ErrorCode.SERVER_RESPONSE);
                    if (StringUtils.isBlank(serverErrorCode)) {
                        String rvmErrorCode = (String) hsmpCardInfo.get(ErrorCode.ERRCODE);
                        if (!StringUtils.isBlank(rvmErrorCode)) {
                            retPkg.put("RVM_RESPONSECODE", rvmErrorCode);
                        }
                    } else {
                        retPkg.put("SERVER_RESPONSECODE", serverErrorCode);
                    }
                } else {
                    retPkg.put("HOST_RESPONSECODE", hostErrorCode);
                }
            } else {
                retPkg.put(ErrorCode.MODEL_RESPONSECODE, modelErrorCode);
            }
        }
        return retPkg;
    }

    private HashMap queryOneCardBalance(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String balance = (String) ServiceGlobal.getCurrentSession("BALANCE");
        if (StringUtils.isBlank(balance)) {
            HashMap hsmpCardInfo = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_READCARD", null));
            if (hsmpCardInfo != null && NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpCardInfo.get("RESULT"))) {
                ServiceGlobal.setCurrentSession("CARD_CONSUME_RECORD", hsmpCardInfo.get("ConsumeRecordList"));
                ServiceGlobal.setCurrentSession("CARD_CHARGE_RECORD", hsmpCardInfo.get("ChargeRecordList"));
                ServiceGlobal.setCurrentSession("BALANCE", hsmpCardInfo.get("Balance"));
                balance = (String) hsmpCardInfo.get("Balance");
            }
        }
        HashMap hsmp = new HashMap();
        hsmp.put("Balance", balance);
        return hsmp;
    }

    private HashMap bindOneCard(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String ONECARD_NUM = (String) hsmpParam.get("ONECARD_NUM");
        String PHONG_NUM = (String) hsmpParam.get("PHONE_NUM");
        String MES_TYPE = MsgType.BIND_MESTYPE;
        Long Time = Long.valueOf(new Date().getTime());
        String QuTime = Time.toString();
        HashMap hsmpPkg = new HashMap();
        hsmpPkg.put("MES_TYPE", MES_TYPE);
        hsmpPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
        hsmpPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
        hsmpPkg.put("QU_TIME", QuTime);
        hsmpPkg.put("CARD_TYPE", CardType.getMsgCardType("TRANSPORTCARD"));
        hsmpPkg.put("CARD_NO", ONECARD_NUM);
        hsmpPkg.put("PHONE", PHONG_NUM);
        hsmpPkg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + Time);
        String retJson = JSONUtils.toJSON(hsmpPkg);
        HashMap hsmpRetPkg = new HashMap();
        try {
            HashMap hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPkg)));
            if (hsmpretPkg == null) {
                HashMap hsmp = new HashMap();
                hsmp.put("RESULT", "error");
                return hsmp;
            }
            String termNo = (String) hsmpretPkg.get("TERM_NO");
            if ("RESPONSE".equalsIgnoreCase((String) hsmpretPkg.get("MES_TYPE")) && SysConfig.get("RVM.CODE").equalsIgnoreCase(termNo)) {
                Integer Confirm = Integer.valueOf(Integer.parseInt((String) hsmpretPkg.get("CONFIRM")));
                if (Confirm.intValue() == 1) {
                    hsmpRetPkg.put("RESULT", "success");
                }
                if (Confirm.intValue() == -1) {
                    hsmpRetPkg.put("RESULT", "error");
                }
                if (Confirm.intValue() != 0) {
                    return hsmpRetPkg;
                }
                hsmpRetPkg.put("RESULT", "failed");
                return hsmpRetPkg;
            }
            hsmpRetPkg.put("RESULT", "error");
            return hsmpRetPkg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private HashMap verifyBindPhone(String svcName, String subSvnName, HashMap hsmpParam) {
        String PHONE_NUM = (String) hsmpParam.get("PHONG_NUM");
        HashMap hsmpResult = new HashMap();
        if (StringUtils.isBlank(PHONE_NUM)) {
            hsmpResult.put("RET_CODE", "none");
        } else {
            if (!StringUtils.isBlank(PHONE_NUM)) {
                String VERIFY_PHONE = SysConfig.get("VERIFY.PHONE.BIND");
                Pattern p = Pattern.compile(VERIFY_PHONE);
                if (PHONE_NUM.matches(VERIFY_PHONE)) {
                    hsmpResult.put("RET_CODE", "success");
                }
            }
            hsmpResult.put("RET_CODE", "error");
        }
        return hsmpResult;
    }

    private HashMap chargeWriteBack(String svcName, String subSvnName, HashMap hsmpParam) {
        if (hsmpParam == null || hsmpParam.size() < 1) {
            return null;
        }
        try {
            Date d = new Date();
            Long Time = Long.valueOf(d.getTime());
            String Datetime = DateUtils.formatDatetime(d);
            String cardNo = (String) ServiceGlobal.getCurrentSession("ONECARD_NUM");
            String beginMoney = (String) ServiceGlobal.getCurrentSession("BALANCE");
            HashMap retPkg = new HashMap();
            if (Integer.parseInt((String) hsmpParam.get("Balance")) + Integer.parseInt(beginMoney) <= 0) {
                retPkg.put("RET_CODE", "NOT_ENOUGH");
                return retPkg;
            }
            HashMap hasp = new HashMap();
            hasp.put("CardNo", cardNo);
            hasp.put("Balance", hsmpParam.get("Balance") + "");
            hasp.put("UserAcctPassword", hsmpParam.get("UserAcctPassword"));
            HashMap hsmpCardInfo = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_CHARGE_WRITEBACK", JSONUtils.toJSON(hasp)));
            if (hsmpCardInfo != null) {
                if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpCardInfo.get("RESULT"))) {
                    String endMoney = null;
                    HashMap hsmpResult = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_READCARD", null));
                    if (hsmpCardInfo != null) {
                        retPkg = new HashMap();
                        if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpResult.get("RESULT"))) {
                            endMoney = (String) hsmpResult.get("Balance");
                            if (!StringUtils.isBlank(endMoney)) {
                                ServiceGlobal.setCurrentSession("BALANCE", endMoney);
                            }
                            recoverAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                        }
                    }
                    String POS_COMM_SEQ = (String) ServiceGlobal.getCurrentSession("POS_COMM_SEQ");
                    String ISAM = (String) ServiceGlobal.getCurrentSession("ISAM");
                    String UID_REC = SysConfig.get("RVM.CODE") + "_" + Time;
                    SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
                    SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
                    sqlWhereBuilder.addStringEqualsTo("CARD_NO", cardNo).addStringEqualsTo("POS_COMM_SEQ", POS_COMM_SEQ).addStringEqualsTo("UID_REC", UID_REC);
                    List<SqlBuilder> listSqlBuilder = new ArrayList();
                    SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_BMCHARGE_FEEDBACK");
                    sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
                    listSqlBuilder.add(sqlDeleteBuilder);
                    SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_BMCHARGE_FEEDBACK");
                    sqlInsertBuilder.newInsertRecord().setString("POS_COMM_SEQ", POS_COMM_SEQ).setString("UID_REC", UID_REC).setString("ISAM", ISAM).setString("CARD_NO", cardNo).setNumber("BUSINESS_TYPE", Integer.valueOf(1)).setString("BUSINESS_NO", hsmpParam.get("UserAcctPassword")).setString("CHARGE_TIME", Datetime).setNumber("CHARGE_AMOUNT", Double.valueOf(Double.parseDouble((String) hsmpParam.get("Balance")))).setNumber("BEGIN_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(beginMoney, 0)))).setNumber("END_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(endMoney, 0)))).setString("UPLOAD_FLAG", Integer.valueOf(1));
                    listSqlBuilder.add(sqlInsertBuilder);
                    SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                    HashMap<String, String> hsmpInstanceTask = new HashMap();
                    hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_BMCHARGE_FEEDBACK_STS");
                    RCCInstanceTask.addTask(hsmpInstanceTask);
                    retPkg.put("RET_CODE", NetworkUtils.NET_STATE_SUCCESS);
                    return retPkg;
                }
                retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
                String modelErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_RESPONSECODE);
                if (StringUtils.isBlank(modelErrorCode)) {
                    String hostErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_HOSTRESPONSECODE);
                    if (StringUtils.isBlank(hostErrorCode)) {
                        String serverErrorCode = (String) hsmpCardInfo.get(ErrorCode.SERVER_RESPONSE);
                        if (StringUtils.isBlank(serverErrorCode)) {
                            String rvmErrorCode = (String) hsmpCardInfo.get(ErrorCode.ERRCODE);
                            if (StringUtils.isBlank(rvmErrorCode)) {
                                return retPkg;
                            }
                            retPkg.put("RVM_RESPONSECODE", rvmErrorCode);
                            return retPkg;
                        }
                        if ("4".equals(serverErrorCode) || SAM_NO_REGIST_LONG.equals(serverErrorCode)) {
                            raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                        }
                        retPkg.put("SERVER_RESPONSECODE", serverErrorCode);
                        return retPkg;
                    }
                    if ("4".equals(hostErrorCode) || SAM_NO_REGIST_LONG.equals(hostErrorCode)) {
                        raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                    }
                    retPkg.put("HOST_RESPONSECODE", hostErrorCode);
                    return retPkg;
                }
                retPkg.put(ErrorCode.MODEL_RESPONSECODE, modelErrorCode);
                return retPkg;
            }
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
            return retPkg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private HashMap chargeScrashCard(String svcName, String subSvnName, HashMap hsmpParam) {
        if (hsmpParam == null || hsmpParam.size() < 1) {
            return null;
        }
        try {
            Date d = new Date();
            Long Time = Long.valueOf(d.getTime());
            String Datetime = DateUtils.formatDatetime(d);
            String cardNo = (String) ServiceGlobal.getCurrentSession("ONECARD_NUM");
            String beginMoney = (String) ServiceGlobal.getCurrentSession("BALANCE");
            HashMap retPkg = new HashMap();
            if (Integer.parseInt((String) hsmpParam.get("Balance")) + Integer.parseInt(beginMoney) <= 0) {
                retPkg.put("RET_CODE", "NOT_ENOUGH");
                return retPkg;
            }
            HashMap hasp = new HashMap();
            hasp.put("CardNo", cardNo);
            hasp.put("Balance", hsmpParam.get("Balance") + "");
            hasp.put("CardPwd", hsmpParam.get("CardPwd"));
            HashMap hsmpCardInfo = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_CHARGE_SCRASHCARD", JSONUtils.toJSON(hasp)));
            if (hsmpCardInfo != null) {
                if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpCardInfo.get("RESULT"))) {
                    String endMoney = null;
                    HashMap hsmpResult = JSONUtils.toHashMap(CommService.getCommService().execute("ONECARDMODEL_READCARD", null));
                    if (hsmpCardInfo != null) {
                        if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase((String) hsmpResult.get("RESULT"))) {
                            endMoney = (String) hsmpResult.get("Balance");
                            if (!StringUtils.isBlank(endMoney)) {
                                ServiceGlobal.setCurrentSession("BALANCE", endMoney);
                            }
                            recoverAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                        }
                    }
                    String POS_COMM_SEQ = (String) ServiceGlobal.getCurrentSession("POS_COMM_SEQ");
                    String ISAM = (String) ServiceGlobal.getCurrentSession("ISAM");
                    String UID_REC = SysConfig.get("RVM.CODE") + "_" + Time;
                    SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
                    SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
                    sqlWhereBuilder.addStringEqualsTo("CARD_NO", cardNo).addStringEqualsTo("POS_COMM_SEQ", POS_COMM_SEQ).addStringEqualsTo("UID_REC", UID_REC);
                    List<SqlBuilder> listSqlBuilder = new ArrayList();
                    SqlDeleteBuilder sqlDeleteBuilder = new SqlDeleteBuilder("RVM_BMCHARGE_FEEDBACK");
                    sqlDeleteBuilder.setSqlWhere(sqlWhereBuilder);
                    listSqlBuilder.add(sqlDeleteBuilder);
                    SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_BMCHARGE_FEEDBACK");
                    sqlInsertBuilder.newInsertRecord().setString("POS_COMM_SEQ", POS_COMM_SEQ).setString("UID_REC", UID_REC).setString("ISAM", ISAM).setString("CARD_NO", cardNo).setNumber("BUSINESS_TYPE", Integer.valueOf(2)).setString("BUSINESS_NO", hsmpParam.get("CardPwd")).setString("CHARGE_TIME", Datetime).setNumber("CHARGE_AMOUNT", Double.valueOf(Double.parseDouble((String) hsmpParam.get("Balance")))).setNumber("BEGIN_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(beginMoney, 0)))).setNumber("END_MONEY", Integer.valueOf(Integer.parseInt(NumberUtils.toScale(endMoney, 0)))).setString("UPLOAD_FLAG", Integer.valueOf(1));
                    listSqlBuilder.add(sqlInsertBuilder);
                    SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                    HashMap<String, String> hsmpInstanceTask = new HashMap();
                    hsmpInstanceTask.put(AllAdvertisement.MEDIA_TYPE, "RVM_BMCHARGE_FEEDBACK_STS");
                    RCCInstanceTask.addTask(hsmpInstanceTask);
                    retPkg.put("RET_CODE", NetworkUtils.NET_STATE_SUCCESS);
                    return retPkg;
                }
                retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
                String modelErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_RESPONSECODE);
                if (StringUtils.isBlank(modelErrorCode)) {
                    String hostErrorCode = (String) hsmpCardInfo.get(ErrorCode.MODEL_HOSTRESPONSECODE);
                    if (StringUtils.isBlank(hostErrorCode)) {
                        String serverErrorCode = (String) hsmpCardInfo.get(ErrorCode.SERVER_RESPONSE);
                        if (StringUtils.isBlank(serverErrorCode)) {
                            String rvmErrorCode = (String) hsmpCardInfo.get(ErrorCode.ERRCODE);
                            if (StringUtils.isBlank(rvmErrorCode)) {
                                return retPkg;
                            }
                            retPkg.put("RVM_RESPONSECODE", rvmErrorCode);
                            return retPkg;
                        }
                        if ("4".equals(serverErrorCode) || SAM_NO_REGIST_LONG.equals(serverErrorCode)) {
                            raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                        }
                        retPkg.put("SERVER_RESPONSECODE", serverErrorCode);
                        return retPkg;
                    }
                    if ("4".equals(hostErrorCode) || SAM_NO_REGIST_LONG.equals(hostErrorCode)) {
                        raiseAlarm(Integer.toString(AlarmId.CARD_READER_NO_REGISTER), null);
                    }
                    retPkg.put("HOST_RESPONSECODE", hostErrorCode);
                    return retPkg;
                }
                retPkg.put(ErrorCode.MODEL_RESPONSECODE, modelErrorCode);
                return retPkg;
            }
            retPkg.put("RET_CODE", NetworkUtils.NET_STATE_FAILED);
            return retPkg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
