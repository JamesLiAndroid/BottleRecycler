package com.incomrecycle.prms.rvm.service.comm.entity.trafficcard;

import it.sauronsoftware.ftp4j.FTPCodes;

public class FrameDataFormat {

    public static class SerialFrameType {
        public static final String ACCT_QUERY_REQ = "AA60";
        public static final String ACCT_QUERY_REQ_FEEDBACK = "AA61";
        public static final String FIND_CARD = "AA00";
        public static final String LOCK_CARD = "AA03";
        public static final String LOGIN = "AA51";
        public static final String LOGIN_AUTH = "AA52";
        public static final String LOGOUT_RESET = "AA53";
        public static final String ONLINE_CHARGE_CANCEL_FEEDBACK = "AA19";
        public static final String ONLINE_CHARGE_CANCEL_REQ = "AA18";
        public static final String ONLINE_CHARGE_CONFIRM = "AA06";
        public static final String ONLINE_CHARGE_CONFIRM_FEEDBACK = "AA07";
        public static final String ONLINE_CHARGE_REQ = "AA04";
        public static final String ONLINE_CHARGE_REQ_FEEDBACK = "AA05";
        public static final String ONLINE_CHARGE_WRITEBACK_CONFIRM = "AA0E";
        public static final String ONLINE_CHARGE_WRITEBACK_CONFIRM_FEEDBACK = "AA0F";
        public static final String ONLINE_CHARGE_WRITEBACK_REQ = "AA0C";
        public static final String ONLINE_CHARGE_WRITEBACK_REQ_FEEDBACK = "AA0D";
        public static final String READ_CARD = "AA01";
        public static final String READ_LAST_RECORD = "AA55";
        public static final String READ_VERSION = "AA54";

        public static class TransType {
            public static final String TRANSTYPE_CARD_CHARGE = "30";
            public static final String TRANSTYPE_CARD_CHARGE_QUICKCARD = "5B";
            public static final String TRANSTYPE_CARD_CHARGE_SCRASHCARD = "38";
            public static final String TRANSTYPE_CARD_CHARGE_WRITEBACK = "33";
        }
    }

    public static class ServerFrameType {
        public static final int ACCT_QUERY_REQ = 5007;
        public static final int ACCT_QUERY_RES = 5008;
        public static final int COMM_ERROR = 5041;
        public static final int DATA_TRANSFER_REQ = 1000;
        public static final int DATA_TRANSFER_RES = 1100;
        public static final int DATA_UPLOAD_REQ = 5037;
        public static final int DATA_UPLOAD_RES = 5038;
        public static final int LOGIN_CONFIRM = 5000;
        public static final int LOGIN_REQ = 5001;
        public static final int LOGIN_RES = 5002;
        public static final int LOGOUT_REQ = 6001;
        public static final int LOGOUT_RES = 6002;
        public static final int ONLINE_CARD_CHARGE_CANCEL_REQ = 5091;
        public static final int ONLINE_CARD_CHARGE_CANCEL_RES = 5092;
        public static final int ONLINE_CARD_CHARGE_CONFIRM_REQ = 5005;
        public static final int ONLINE_CARD_CHARGE_CONFIRM_RES = 5006;
        public static final int ONLINE_CARD_CHARGE_REQ = 5003;
        public static final int ONLINE_CARD_CHARGE_RES = 5004;
        public static final int ONLINE_CARD_CHARGE_WRITEBACK_CONFIRM_REQ = 5027;
        public static final int ONLINE_CARD_CHARGE_WRITEBACK_CONFIRM_RES = 5028;
        public static final int ONLINE_CARD_CHARGE_WRITEBACK_REQ = 5025;
        public static final int ONLINE_CARD_CHARGE_WRITEBACK_RES = 5026;
        public static final int PARAM_DOWNLOAD_REQ = 5035;
        public static final int PARAM_DOWNLOAD_RES = 5036;
        public static final int PARAM_QUERY_REQ = 5033;
        public static final int PARAM_QUERY_RES = 5034;
        public static final int QUICKCARD_CHARGE_CONFIRM_REQ = 5145;
        public static final int QUICKCARD_CHARGE_CONFIRM_RES = 5146;
        public static final int QUICKCARD_CHARGE_REQ = 5143;
        public static final int QUICKCARD_CHARGE_RES = 5144;
        public static final int QUICKCARD_QUERY_REQ = 5141;
        public static final int QUICKCARD_QUERY_RES = 5142;
        public static final int TERM_TRANS_BATCHNO_APPLY_CONFIRM = 5042;
        public static final int TERM_TRANS_BATCHNO_APPLY_REQ = 5039;
        public static final int TERM_TRANS_BATCHNO_APPLY_RES = 5040;

        public static class TxnMode {
            public static final int TXN_MODE_ACCT = 4;
            public static final int TXN_MODE_CASH = 1;
            public static final int TXN_MODE_SCRASHCARD = 6;
        }
    }

    public static FrameFormat getServerFrameFormat(int messageType, int subType) {
        FrameFormat frameFormat = new FrameFormat();
        switch (messageType) {
            case ServerFrameType.DATA_TRANSFER_REQ /*1000*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("ResponseCode", FrameItemType.HEX, 1));
                break;
            case ServerFrameType.DATA_TRANSFER_RES /*1100*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("PackFlag", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("DataBlock", FrameItemType.BLOCK, 0));
                break;
            case ServerFrameType.LOGIN_CONFIRM /*5000*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("ResponseCode", FrameItemType.HEX, 1));
                break;
            case 5001:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("Mchntid", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("ProgramName", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("Random", FrameItemType.BCD, 8));
                frameFormat.add(new FrameItem("PosIcSeq", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("PosAccSeq", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("PosCommSeq", FrameItemType.HEX, 4));
                break;
            case 5002:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("ProgramName", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("ResponseCode", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("EncText", FrameItemType.BCD, 8));
                frameFormat.add(new FrameItem("LimitTime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("PosIcSeq", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("PosAccSeq", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("PosCommSeq", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 128));
                break;
            case 5003:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("MchntId", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 48));
                frameFormat.add(new FrameItem("LastRecord", FrameItemType.HEX, 17));
                frameFormat.add(new FrameItem("PosCommSeq", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("TxnMode", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("TxnMsg", FrameItemType.HEX, 0));
                break;
            case ServerFrameType.ONLINE_CARD_CHARGE_RES /*5004*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 64));
                break;
            case ServerFrameType.ONLINE_CARD_CHARGE_CONFIRM_REQ /*5005*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("MchntId", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 32));
                frameFormat.add(new FrameItem("CSN", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("AftBal", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CardCount", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("TxnTAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CardExp", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("TacType", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("PsamTransNo", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("TxnStatus", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("PlivateType", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("PlivateMsg", FrameItemType.HEX, 0));
                break;
            case ServerFrameType.ONLINE_CARD_CHARGE_CONFIRM_RES /*5006*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 16));
                break;
            case ServerFrameType.ACCT_QUERY_REQ /*5007*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("MchntId", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 8));
                frameFormat.add(new FrameItem("EncPasswd", FrameItemType.HEX, 8));
                break;
            case ServerFrameType.ACCT_QUERY_RES /*5008*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 16));
                frameFormat.add(new FrameItem("AcctNum", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("AcctInfo", FrameItemType.HEX, 0));
                break;
            case ServerFrameType.ONLINE_CARD_CHARGE_WRITEBACK_REQ /*5025*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("Mchntid", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 56));
                frameFormat.add(new FrameItem("LastRecord", FrameItemType.HEX, 17));
                frameFormat.add(new FrameItem("TxnMode", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("TxnMsg", FrameItemType.HEX, 0));
                break;
            case ServerFrameType.ONLINE_CARD_CHARGE_WRITEBACK_RES /*5026*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 64));
                break;
            case ServerFrameType.ONLINE_CARD_CHARGE_WRITEBACK_CONFIRM_REQ /*5027*/:
                if (subType == 3) {
                    frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                    frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                    frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                    frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                    frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                    frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                    frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                    frameFormat.add(new FrameItem("Mchntid", FrameItemType.BCD, 6));
                    frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                    frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                    frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 32));
                    frameFormat.add(new FrameItem("CSN", FrameItemType.HEX, 4));
                    frameFormat.add(new FrameItem("CardExp", FrameItemType.BCD, 4));
                    frameFormat.add(new FrameItem("AftBal", FrameItemType.HEX, 4));
                    frameFormat.add(new FrameItem("CardCount", FrameItemType.HEX, 2));
                    frameFormat.add(new FrameItem("TxnTAC", FrameItemType.HEX, 4));
                    frameFormat.add(new FrameItem("TacType", FrameItemType.HEX, 1));
                    frameFormat.add(new FrameItem("PsamTransNo", FrameItemType.HEX, 2));
                    frameFormat.add(new FrameItem("PlivateType", FrameItemType.HEX, 2));
                    frameFormat.add(new FrameItem("PlivateMsg", FrameItemType.HEX, 0));
                }
                if (subType == 4) {
                    frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                    frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                    frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                    frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                    frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                    frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                    frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                    frameFormat.add(new FrameItem("Mchntid", FrameItemType.BCD, 6));
                    frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                    frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                    frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 32));
                    frameFormat.add(new FrameItem("CSN", FrameItemType.HEX, 4));
                    frameFormat.add(new FrameItem("CardExp", FrameItemType.BCD, 4));
                    frameFormat.add(new FrameItem("AftBal", FrameItemType.HEX, 4));
                    frameFormat.add(new FrameItem("CardCount", FrameItemType.HEX, 2));
                    frameFormat.add(new FrameItem("TxnTAC", FrameItemType.HEX, 4));
                    frameFormat.add(new FrameItem("TacType", FrameItemType.HEX, 1));
                    frameFormat.add(new FrameItem("PsamTransNo", FrameItemType.HEX, 2));
                    frameFormat.add(new FrameItem("TxnStatus", FrameItemType.HEX, 1));
                    frameFormat.add(new FrameItem("PlivateType", FrameItemType.HEX, 2));
                    frameFormat.add(new FrameItem("PlivateMsg", FrameItemType.HEX, 0));
                    break;
                }
                break;
            case ServerFrameType.ONLINE_CARD_CHARGE_WRITEBACK_CONFIRM_RES /*5028*/:
                if (subType == 3) {
                    frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                    frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                    frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                    frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                    frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                    frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                    frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                    frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                    frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 16));
                }
                if (subType == 4) {
                    frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                    frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                    frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                    frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                    frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                    frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                    frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                    frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                    frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 16));
                    break;
                }
                break;
            case ServerFrameType.PARAM_QUERY_REQ /*5033*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("Mchntid", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("ChkMode", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("Reserved", FrameItemType.HEX, 4));
                break;
            case ServerFrameType.PARAM_QUERY_RES /*5034*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("ResponseCode", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("ParamFlag", FrameItemType.BIT, 4));
                break;
            case ServerFrameType.PARAM_DOWNLOAD_REQ /*5035*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("ParamFlag", FrameItemType.BIT, 4));
                break;
            case ServerFrameType.PARAM_DOWNLOAD_RES /*5036*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("ParamFlag", FrameItemType.BIT, 4));
                frameFormat.add(new FrameItem("ResponseCode", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("RecordNum", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("ValidDate", FrameItemType.BCD, 4));
                break;
            case ServerFrameType.DATA_UPLOAD_REQ /*5037*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("MessageID", FrameItemType.ASCII, 34));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("MchntId", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("MessageLen", FrameItemType.HEX, 4));
                break;
            case ServerFrameType.DATA_UPLOAD_RES /*5038*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("MessageID", FrameItemType.ASCII, 34));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("MessageLen", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("ResponseCode", FrameItemType.HEX, 1));
                break;
            case ServerFrameType.COMM_ERROR /*5041*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("ResponseCode", FrameItemType.HEX, 1));
                break;
            case ServerFrameType.ONLINE_CARD_CHARGE_CANCEL_REQ /*5091*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("MchntId", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 24));
                break;
            case ServerFrameType.ONLINE_CARD_CHARGE_CANCEL_RES /*5092*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 8));
                break;
            case ServerFrameType.QUICKCARD_QUERY_REQ /*5141*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("MchntId", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CardNo", FrameItemType.BCD, 8));
                frameFormat.add(new FrameItem("CardType", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("CardPhyType", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("BefBal", FrameItemType.HEX, 4));
                break;
            case ServerFrameType.QUICKCARD_QUERY_RES /*5142*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("ResponseCode", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("CardNo", FrameItemType.BCD, 8));
                frameFormat.add(new FrameItem("OrderNum", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("OrderInfo", FrameItemType.HEX, 0));
                break;
            case ServerFrameType.QUICKCARD_CHARGE_REQ /*5143*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("MchntId", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 48));
                frameFormat.add(new FrameItem("OrderNo", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("LastRecord", FrameItemType.HEX, 17));
                frameFormat.add(new FrameItem("PosCommSeq", FrameItemType.HEX, 4));
                break;
            case ServerFrameType.QUICKCARD_CHARGE_RES /*5144*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 64));
                break;
            case ServerFrameType.QUICKCARD_CHARGE_CONFIRM_REQ /*5145*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("MchntId", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 32));
                frameFormat.add(new FrameItem("OrderNo", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CSN", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("AftBal", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CardCount", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("TxnTAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CardExp", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("TacType", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("PsamTransNo", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("TxnStatus", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("PlivateType", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("PlivateMsg", FrameItemType.HEX, 0));
                break;
            case ServerFrameType.QUICKCARD_CHARGE_CONFIRM_RES /*5146*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("CipherDataMAC", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("CipherDataLen", FrameItemType.HEX, 2));
                frameFormat.add(new FrameItem("MacBuf", FrameItemType.HEX, 16));
                frameFormat.add(new FrameItem("OrderNo", FrameItemType.HEX, 4));
                break;
            case ServerFrameType.LOGOUT_REQ /*6001*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("OprId", FrameItemType.BCD, 3));
                frameFormat.add(new FrameItem("POSID", FrameItemType.ASCII, 20));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("UnitId", FrameItemType.BCD, 4));
                frameFormat.add(new FrameItem("Mchntid", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("PosIcSeq", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("PosAccSeq", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("PosCommSeq", FrameItemType.HEX, 4));
                break;
            case ServerFrameType.LOGOUT_RES /*6002*/:
                frameFormat.add(new FrameItem("Messagetype", FrameItemType.BCD, 2));
                frameFormat.add(new FrameItem("Ver", FrameItemType.HEX, 1));
                frameFormat.add(new FrameItem("SysDatetime", FrameItemType.BCD, 7));
                frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
                frameFormat.add(new FrameItem("BatchNO", FrameItemType.HEX, 4));
                frameFormat.add(new FrameItem("ResponseCode", FrameItemType.HEX, 1));
                break;
        }
        return frameFormat;
    }

    public static FrameFormat getSerialRequestFrameFormat(String messageType) {
        FrameFormat frameFormat = new FrameFormat();
        if (SerialFrameType.FIND_CARD.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
        }
        if (SerialFrameType.READ_CARD.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("TransType", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("TransParam", FrameItemType.H_ASCII, 2));
        }
        if (SerialFrameType.ONLINE_CHARGE_REQ.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("TransType", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("CSN", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("TransBalance", FrameItemType.H_ASCII, 8));
        }
        if (SerialFrameType.ONLINE_CHARGE_REQ_FEEDBACK.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("PosIcSeq", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("OprId", FrameItemType.B_ASCII, 6));
            frameFormat.add(new FrameItem("AgentCode", FrameItemType.B_ASCII, 2));
            frameFormat.add(new FrameItem("EndDate", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("MAC", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("ServerEncryptData", FrameItemType.B_ASCII, 128));
        }
        if (SerialFrameType.ONLINE_CHARGE_CONFIRM.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("TransType", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("TransResCode", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("TransDate", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("TransTime", FrameItemType.B_ASCII, 6));
            frameFormat.add(new FrameItem("CardNo", FrameItemType.B_ASCII, 16));
            frameFormat.add(new FrameItem("TransBalance", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("HostTransSeq", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("PosICSeq", FrameItemType.H_ASCII, 8));
        }
        if (SerialFrameType.ONLINE_CHARGE_CONFIRM_FEEDBACK.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("MAC", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("EncryptData", FrameItemType.B_ASCII, 32));
        }
        if (SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("TransType", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("PosCommSeq", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("CSN", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("TransBalance", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("Password", FrameItemType.B_ASCII, 16));
        }
        if (SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ_FEEDBACK.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("PosIcSeq", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("OprId", FrameItemType.B_ASCII, 6));
            frameFormat.add(new FrameItem("AgentCode", FrameItemType.B_ASCII, 2));
            frameFormat.add(new FrameItem("EndDate", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("MAC", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("ServerEncryptData", FrameItemType.B_ASCII, 128));
        }
        if (SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("TransType", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("TransResCode", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("TransDate", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("TransTime", FrameItemType.B_ASCII, 6));
            frameFormat.add(new FrameItem("CardNo", FrameItemType.B_ASCII, 16));
            frameFormat.add(new FrameItem("TransBalance", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("HostTransSeq", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("PosICSeq", FrameItemType.H_ASCII, 8));
        }
        if (SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM_FEEDBACK.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("MAC", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("EncryptData", FrameItemType.B_ASCII, 32));
        }
        if (SerialFrameType.LOGIN.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
        }
        if (SerialFrameType.LOGIN_AUTH.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("EncText", FrameItemType.B_ASCII, 16));
            frameFormat.add(new FrameItem("MacBuf", FrameItemType.H_ASCII, 256));
        }
        if (SerialFrameType.LOGOUT_RESET.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
        }
        if (SerialFrameType.READ_VERSION.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
        }
        if (SerialFrameType.READ_LAST_RECORD.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
        }
        if (SerialFrameType.LOCK_CARD.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("CardNo", FrameItemType.B_ASCII, 16));
            frameFormat.add(new FrameItem("TermTransSeq", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("TransTime", FrameItemType.B_ASCII, 14));
        }
        if (SerialFrameType.ONLINE_CHARGE_CANCEL_REQ.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("TransType", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("TransDate", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("CardNo", FrameItemType.B_ASCII, 16));
            frameFormat.add(new FrameItem("TransBalance", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("PosCommSeq", FrameItemType.H_ASCII, 8));
        }
        if (SerialFrameType.ONLINE_CHARGE_CANCEL_FEEDBACK.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("MAC", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("ServerEncryptData", FrameItemType.B_ASCII, 16));
        }
        if (SerialFrameType.ACCT_QUERY_REQ.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("Password", FrameItemType.B_ASCII, 16));
            frameFormat.add(new FrameItem("TransTime", FrameItemType.B_ASCII, 14));
        }
        if (SerialFrameType.ACCT_QUERY_REQ_FEEDBACK.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("MAC", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("ServerEncryptData", FrameItemType.B_ASCII, 32));
        }
        return frameFormat;
    }

    public static FrameFormat getSerialResponseFrameFormat(String messageType, String subType) {
        FrameFormat frameFormat = new FrameFormat();
        if (SerialFrameType.FIND_CARD.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
        }
        if (SerialFrameType.READ_CARD.equals(messageType)) {
            if ("00".equals(subType) || "02".equals(subType) || "06".equals(subType)) {
                frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
                frameFormat.add(new FrameItem("CSN", FrameItemType.H_ASCII, 8));
                frameFormat.add(new FrameItem("CardNo", FrameItemType.B_ASCII, 16));
                frameFormat.add(new FrameItem("CardType", FrameItemType.H_ASCII, 2));
                frameFormat.add(new FrameItem("PhyCardType", FrameItemType.H_ASCII, 2));
                frameFormat.add(new FrameItem("CardState", FrameItemType.H_ASCII, 2));
                frameFormat.add(new FrameItem("StartDate", FrameItemType.B_ASCII, 8));
                frameFormat.add(new FrameItem("EndDate", FrameItemType.B_ASCII, 8));
                frameFormat.add(new FrameItem("Balance", FrameItemType.H_ASCII, 8));
                frameFormat.add(new FrameItem("Deposit", FrameItemType.H_ASCII, 4));
                frameFormat.add(new FrameItem("CountType", FrameItemType.H_ASCII, 2));
                frameFormat.add(new FrameItem("LastRecord", FrameItemType.H_ASCII, 34));
            }
            if ("CC".equals(subType)) {
                frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
                frameFormat.add(new FrameItem("ConsumeRecordFormat", FrameItemType.H_ASCII, 2));
                frameFormat.add(new FrameItem("CSN", FrameItemType.H_ASCII, 8));
                frameFormat.add(new FrameItem("ConsumeRecord", FrameItemType.B_ASCII, 160));
            }
            if ("CD".equals(subType)) {
                frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
                frameFormat.add(new FrameItem("ChargeRecordFormat", FrameItemType.H_ASCII, 2));
                frameFormat.add(new FrameItem("CSN", FrameItemType.H_ASCII, 8));
                frameFormat.add(new FrameItem("ChargeRecord", FrameItemType.B_ASCII, 0));
            }
        }
        if (SerialFrameType.ONLINE_CHARGE_REQ.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("MAC", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("EncryptData", FrameItemType.B_ASCII, 96));
            frameFormat.add(new FrameItem("WalletTransSeq", FrameItemType.H_ASCII, 4));
        }
        if (SerialFrameType.ONLINE_CHARGE_REQ_FEEDBACK.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("hostResCode", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("hostSeq", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("ChargeDataRecord", FrameItemType.H_ASCII, FTPCodes.RESTART_MARKER));
        }
        if (SerialFrameType.ONLINE_CHARGE_CONFIRM.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("MAC", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("EncryptData", FrameItemType.B_ASCII, 64));
        }
        if (SerialFrameType.ONLINE_CHARGE_CONFIRM_FEEDBACK.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("TransType", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("hostResCode", FrameItemType.H_ASCII, 2));
        }
        if (SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("AcctEncryptData", FrameItemType.B_ASCII, 16));
            frameFormat.add(new FrameItem("MAC", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("EncryptData", FrameItemType.B_ASCII, 112));
            frameFormat.add(new FrameItem("WalletTransSeq", FrameItemType.H_ASCII, 4));
        }
        if (SerialFrameType.ONLINE_CHARGE_WRITEBACK_REQ_FEEDBACK.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("hostResCode", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("hostSeq", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("ChargeDataRecord", FrameItemType.H_ASCII, FTPCodes.RESTART_MARKER));
        }
        if (SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("MAC", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("EncryptData", FrameItemType.B_ASCII, 64));
        }
        if (SerialFrameType.ONLINE_CHARGE_WRITEBACK_CONFIRM_FEEDBACK.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("TransType", FrameItemType.H_ASCII, 2));
        }
        if (SerialFrameType.LOGIN.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("ISAM", FrameItemType.B_ASCII, 12));
            frameFormat.add(new FrameItem("RANDOM", FrameItemType.B_ASCII, 16));
        }
        if (SerialFrameType.LOGIN_AUTH.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("RemainFailCount", FrameItemType.H_ASCII, 2));
        }
        if (SerialFrameType.LOGOUT_RESET.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
        }
        if (SerialFrameType.READ_VERSION.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("Version", FrameItemType.B_ASCII, 34));
            frameFormat.add(new FrameItem("ISAM", FrameItemType.B_ASCII, 12));
        }
        if (SerialFrameType.READ_LAST_RECORD.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("ChargeRecordData", FrameItemType.B_ASCII, FTPCodes.RESTART_MARKER));
            frameFormat.add(new FrameItem("PsamTransNo", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("PsamTermNo", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("hostSeq", FrameItemType.H_ASCII, 8));
        }
        if (SerialFrameType.LOCK_CARD.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("LockTransData", FrameItemType.H_ASCII, 114));
        }
        if (SerialFrameType.ONLINE_CHARGE_CANCEL_REQ.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("MAC", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("EncryptData", FrameItemType.B_ASCII, 48));
        }
        if (SerialFrameType.ONLINE_CHARGE_CANCEL_FEEDBACK.equals(messageType)) {
            frameFormat.add(new FrameItem("responseCode", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("TransType", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("hostResCode", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("PosCommSeq", FrameItemType.H_ASCII, 8));
        }
        if (SerialFrameType.ACCT_QUERY_REQ.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("EncPasswd", FrameItemType.B_ASCII, 16));
            frameFormat.add(new FrameItem("MAC", FrameItemType.B_ASCII, 8));
            frameFormat.add(new FrameItem("EncryptData", FrameItemType.B_ASCII, 16));
            frameFormat.add(new FrameItem("CardNo", FrameItemType.B_ASCII, 16));
        }
        if (SerialFrameType.ACCT_QUERY_REQ_FEEDBACK.equals(messageType)) {
            frameFormat.add(new FrameItem("Messagetype", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("hostResCode", FrameItemType.H_ASCII, 2));
        }
        return frameFormat;
    }

    public static FrameFormat getFrameFormat(String type) {
        FrameFormat frameFormat = new FrameFormat();
        if ("CARD_TYPE".equalsIgnoreCase(type)) {
            frameFormat.add(new FrameItem("PhyCardType", FrameItemType.HEX, 1));
            frameFormat.add(new FrameItem("CardType", FrameItemType.HEX, 1));
            frameFormat.add(new FrameItem("CardTypeName", FrameItemType.ASCII, 16));
            frameFormat.add(new FrameItem("MinChargeAmount", FrameItemType.HEX, 2));
            frameFormat.add(new FrameItem("ChargeAmountUnit", FrameItemType.HEX, 2));
            frameFormat.add(new FrameItem("MaxChargeAmount", FrameItemType.HEX, 4));
            frameFormat.add(new FrameItem("MaxBalance", FrameItemType.HEX, 4));
            frameFormat.add(new FrameItem("EndDays", FrameItemType.HEX, 4));
        } else if ("CHARGE_DATA_RECORD".equals(type)) {
            frameFormat.add(new FrameItem("TransType", FrameItemType.HEX, 1));
            frameFormat.add(new FrameItem("ISAM", FrameItemType.BCD, 6));
            frameFormat.add(new FrameItem("TransAmount", FrameItemType.HEX, 4));
            frameFormat.add(new FrameItem("TransSeq", FrameItemType.HEX, 4));
            frameFormat.add(new FrameItem("CardBalance", FrameItemType.HEX, 4));
            frameFormat.add(new FrameItem("TransDate", FrameItemType.HEX, 4));
            frameFormat.add(new FrameItem("TransTime", FrameItemType.HEX, 3));
            frameFormat.add(new FrameItem("CSN", FrameItemType.HEX, 4));
            frameFormat.add(new FrameItem("CardCount", FrameItemType.HEX, 2));
            frameFormat.add(new FrameItem("CardNo", FrameItemType.BCD, 8));
            frameFormat.add(new FrameItem("TxnTAC", FrameItemType.HEX, 4));
            frameFormat.add(new FrameItem("PrevBalance", FrameItemType.HEX, 4));
            frameFormat.add(new FrameItem("CardType", FrameItemType.HEX, 1));
            frameFormat.add(new FrameItem("PhyCardType", FrameItemType.HEX, 1));
            frameFormat.add(new FrameItem("EndDate", FrameItemType.BCD, 4));
            frameFormat.add(new FrameItem("EncryptType", FrameItemType.HEX, 1));
        } else if ("BUSINESS_PARAM".equals(type)) {
            frameFormat.add(new FrameItem("CORP_ID", FrameItemType.BCD, 4));
            frameFormat.add(new FrameItem("FROM_CORP_ID", FrameItemType.BCD, 4));
            frameFormat.add(new FrameItem("TO_CORP_ID", FrameItemType.BCD, 4));
            frameFormat.add(new FrameItem("AGENT_CODE", FrameItemType.HEX, 1));
            frameFormat.add(new FrameItem("MCHNT_ID", FrameItemType.BCD, 6));
            frameFormat.add(new FrameItem("MCHNT_NAME", FrameItemType.ASCII, 40));
            frameFormat.add(new FrameItem("SITE_NAME", FrameItemType.ASCII, 40));
            frameFormat.add(new FrameItem("ONLINE_CHARGE_LIMIT", FrameItemType.HEX, 4));
            frameFormat.add(new FrameItem("USER1", FrameItemType.ASCII, 10));
            frameFormat.add(new FrameItem("PWD1", FrameItemType.ASCII, 10));
            frameFormat.add(new FrameItem("USER2", FrameItemType.ASCII, 10));
            frameFormat.add(new FrameItem("PWD2", FrameItemType.ASCII, 10));
            frameFormat.add(new FrameItem("USER3", FrameItemType.ASCII, 10));
            frameFormat.add(new FrameItem("PWD3", FrameItemType.ASCII, 10));
            frameFormat.add(new FrameItem("DATAUPDATEMODE", FrameItemType.HEX, 1));
            frameFormat.add(new FrameItem("ROLLBACKTYPE", FrameItemType.HEX, 1));
        } else if ("TxnMsg".equalsIgnoreCase(type)) {
            frameFormat.add(new FrameItem("TxnMsgLen", FrameItemType.HEX, 2));
            frameFormat.add(new FrameItem("DSSign1", FrameItemType.HEX, 1));
            frameFormat.add(new FrameItem("DSSign2", FrameItemType.HEX, 1));
            frameFormat.add(new FrameItem("TxnMsg", FrameItemType.HEX, 0));
            frameFormat.add(new FrameItem("TxnCryMsg", FrameItemType.HEX, 0));
            frameFormat.add(new FrameItem("DigitalSign", FrameItemType.HEX, 0));
        } else if ("QuickCard".equalsIgnoreCase(type)) {
            frameFormat.add(new FrameItem("OrderNo", FrameItemType.HEX, 4));
            frameFormat.add(new FrameItem("OrderSaveMnt", FrameItemType.HEX, 4));
            frameFormat.add(new FrameItem("OrderDate", FrameItemType.BCD, 4));
            frameFormat.add(new FrameItem("OrderTime", FrameItemType.BCD, 3));
        } else if ("ConsumeRecord1".equalsIgnoreCase(type)) {
            frameFormat.add(new FrameItem("TransTime", FrameItemType.B_ASCII, 10));
            frameFormat.add(new FrameItem("PrevBalance", FrameItemType.H_ASCII, 6));
            frameFormat.add(new FrameItem("TransAmount", FrameItemType.H_ASCII, 4));
            frameFormat.add(new FrameItem("TransType", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("SysCode", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("SysInfo", FrameItemType.B_ASCII, 8));
        } else if ("ConsumeRecord2".equalsIgnoreCase(type)) {
            frameFormat.add(new FrameItem("TransTime", FrameItemType.B_ASCII, 10));
            frameFormat.add(new FrameItem("TransAmount", FrameItemType.H_ASCII, 8));
            frameFormat.add(new FrameItem("TransType", FrameItemType.H_ASCII, 2));
            frameFormat.add(new FrameItem("TermCode", FrameItemType.B_ASCII, 12));
        } else if ("ChargeRecord1".equalsIgnoreCase(type)) {
            frameFormat.add(new FrameItem("Reserved", FrameItemType.B_ASCII, 4));
            frameFormat.add(new FrameItem("PrevBalance", FrameItemType.H_ASCII, 6));
            frameFormat.add(new FrameItem("NextBalance", FrameItemType.H_ASCII, 6));
            frameFormat.add(new FrameItem("OprId", FrameItemType.B_ASCII, 4));
            frameFormat.add(new FrameItem("TransDate", FrameItemType.B_ASCII, 6));
            frameFormat.add(new FrameItem("PosCode", FrameItemType.B_ASCII, 6));
            frameFormat.add(new FrameItem("AgentCode", FrameItemType.B_ASCII, 2));
        } else if ("ChargeRecord2".equalsIgnoreCase(type)) {
            frameFormat.add(new FrameItem("PrevBalance", FrameItemType.H_ASCII, 6));
            frameFormat.add(new FrameItem("NextBalance", FrameItemType.H_ASCII, 6));
            frameFormat.add(new FrameItem("OprId", FrameItemType.B_ASCII, 4));
            frameFormat.add(new FrameItem("TransDate", FrameItemType.B_ASCII, 6));
            frameFormat.add(new FrameItem("PosCode", FrameItemType.B_ASCII, 12));
        } else if ("CommSettings".equalsIgnoreCase(type)) {
            frameFormat.add(new FrameItem("DialNumber1", FrameItemType.BCD, 8));
            frameFormat.add(new FrameItem("DialNumber2", FrameItemType.BCD, 8));
            frameFormat.add(new FrameItem("DialNumber3", FrameItemType.BCD, 8));
            frameFormat.add(new FrameItem("ServIp1", FrameItemType.ASCII, 40));
            frameFormat.add(new FrameItem("ServPort1", FrameItemType.BCD, 3));
            frameFormat.add(new FrameItem("ServIp2", FrameItemType.ASCII, 40));
            frameFormat.add(new FrameItem("ServPort2", FrameItemType.BCD, 3));
            frameFormat.add(new FrameItem("ServIp3", FrameItemType.ASCII, 40));
            frameFormat.add(new FrameItem("ServPort3", FrameItemType.BCD, 3));
            frameFormat.add(new FrameItem("ServIp4", FrameItemType.ASCII, 40));
            frameFormat.add(new FrameItem("ServPort4", FrameItemType.BCD, 3));
        }
        return frameFormat;
    }
}
