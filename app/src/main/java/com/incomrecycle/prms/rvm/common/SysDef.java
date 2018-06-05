package com.incomrecycle.prms.rvm.common;

import com.google.code.microlog4android.format.SimpleFormatter;
import com.incomrecycle.common.utils.NumberUtils;
import com.incomrecycle.common.utils.StringUtils;

public class SysDef {
    public static final int[] AlarmIdStuts = new int[]{11, 12, 13, 14, AlarmId.RVM_ERROR, AlarmId.PRINTER_NO_PAPER,
            AlarmId.PRINTER_ERROR, AlarmId.BARCODE_SCANER_ERROR,
            AlarmId.MAGNETIC_CARD_READER_ERROR, AlarmId.DO_READER_ERROR, AlarmId.TCP_ALARM_YELLOW,
            AlarmId.TCP_ALARM_RED, AlarmId.PLC_DOOR_ERROR, AlarmId.PLC_COMM_ERROR, 5001, 5002, 5003};
    public static final int[] AlarmStuts = new int[]{1, 2, 3, 4};
    public static final String[][] SERVICE_OF_OPT_TYPE;
    public static final int[] handlingAlarmId = new int[]{AlarmId.RVM_ERROR, AlarmId.PRINTER_NO_PAPER,
            AlarmId.PRINTER_ERROR, AlarmId.BARCODE_SCANER_ERROR, AlarmId.MAGNETIC_CARD_READER_ERROR, AlarmId.DO_READER_ERROR};
    private static final int[] optSeq = new int[]{1};

    public static final class AboutUs {
        public static final int ABOUT_US_BEGIN_ID = 2014;
        public static final int ABOUT_US_SAVE_ID_KEY = 96001;
    }

    public static final class AlarmId {
        public static final int ALARM_AUTO = 4;
        public static final int ALARM_GOING = 2;
        public static final int ALARM_HAND = 3;
        public static final int ALARM_NO = 1;
        public static final int BARCODE_SCANER_ERROR = 1031;
        public static final int CARD_READER_NO_REGISTER = 1042;
        public static final int DO_READER_ERROR = 1051;
        public static final int FIRST_LIGHT_UNNORMAL = 5001;
        public static final int MAGNETIC_CARD_READER_ERROR = 1041;
        public static final int PLC_COMM_ERROR = 3002;
        public static final int PLC_DOOR_ERROR = 3001;
        public static final int PRINTER_ERROR = 1022;
        public static final int PRINTER_NO_PAPER = 1021;
        public static final int RVM_ERROR = 1011;
        public static final int SECOND_LIGHT_UNNORMAL = 5002;
        public static final int[] SINGLE_ALARM = new int[]{RVM_ERROR, PLC_COMM_ERROR, PLC_DOOR_ERROR, PRINTER_NO_PAPER, PRINTER_ERROR, BARCODE_SCANER_ERROR, MAGNETIC_CARD_READER_ERROR, DO_READER_ERROR, 5001, 5002, 5003};
        public static final int STORAGE_BOTTLE_MAX = 11;
        public static final int STORAGE_BOTTLE_TOOMUCH = 12;
        public static final int STORAGE_PAPER_MAX = 13;
        public static final int STORAGE_PAPER_TOOMUCH = 14;
        public static final int TCP_ALARM_RED = 2002;
        public static final int TCP_ALARM_YELLOW = 2001;
        public static final int THIRD_LIGHT_UNNORMAL = 5003;

        public static boolean isSingleAlarm(String alarmId) {
            return isSingleAlarm(Integer.parseInt(alarmId));
        }

        public static boolean isSingleAlarm(int alarmId) {
            for (int i : SINGLE_ALARM) {
                if (i == alarmId) {
                    return true;
                }
            }
            return false;
        }
    }

    public static final class AlarmStatus {
        public static final int HANDLING = 2;
        public static final int RAISED = 1;
        public static final int RECOVERY_AUTO = 4;
        public static final int RECOVERY_MANUAL = 3;
    }

    public static final class AllAdvertisement {
        public static final String AD_VERSION = "[AD_VERSION]";
        public static final String BACKGROUND_LABEL = "[BACKGROUND]";
        public static final String BACKGROUND_PIC = "BACKGROUND_PIC";
        public static final String BEGIN_DATE = "BEGIN_DATE";
        public static final String BUTTON_TYPE_EXTENSION = "BUTTON_TYPE_EXTENSION";
        public static final String BUTTON_TYPE_NATIVE = "BUTTON_TYPE_NATIVE";
        public static final String CLICK_MOVIE_PATH = "MAIN_CLK_MOVIE";
        public static final String CLICK_PICTURE_PATH = "MAIN_CLK_PIC";
        public static final String END_DATE = "END_DATE";
        public static final String HOMEPAGE_CENTER_TOP_LABEL = "[HOMEPAGE_CENTER_TOP]";
        public static final String HOMEPAGE_EXT_VENDING_LABEL = "[EXT_VENDINGWAY]";
        public static final String HOMEPAGE_FULL_AD_LABEL = "[HOMEPAGE_FULL_AD]";
        public static final String HOMEPAGE_LEFT = "HOMEPAGE_LEFT";
        public static final String HOMEPAGE_LEFT_BOTTOM_LABEL = "[HOMEPAGE_LEFT_BOTTOM]";
        public static final String HOMEPAGE_LEFT_TOP_LABEL = "[HOMEPAGE_LEFT_TOP]";
        public static final String HOMEPAGE_PUT_BOTTLE_LABEL = "[PUTBOTTLE]";
        public static final String HOMEPAGE_REBATE_PROCESS_CHANGE_LABEL = "[HOMEPAGE_REBATE_PROCESS_CHANGE]";
        public static final String HOMEPAGE_REBATE_PROCESS_LABEL = "[HOMEPAGE_REBATE_PROCESS]";
        public static final String HOMEPAGE_RIGHT_TOP_LABEL = "[HOMEPAGE_RIGHT_TOP]";
        public static final String HOMEPAGE_VENDING_LABEL = "[VENDING]";
        public static final String ICON = "ICON";
        public static final String MAIN_CLK_DESC = "MAIN_CLK_DESC";
        public static final String MAIN_MOVIE_PATH = "MAIN_MOVIE";
        public static final String MAIN_PICTURE_PATH = "MAIN_PIC";
        public static final String MEDIA_TYPE = "TYPE";
        public static final String MEDIA_TYPE_MOVIE = "MOVIE";
        public static final String MEDIA_TYPE_PICTURE = "PICTURE";
        public static final String PLAY_ORDER = "PLAY_ORDER";
        public static final String PLAY_SECONDS = "PLAY_SECONDS";
        public static final String PUTBOTTLE_PIC = "PUTBOTTLE_PIC";
        public static final String SELECT_FLAG = "SELECT_FLAG";
        public static final String VENDING_DESC = "VENDING_DESC";
        public static final String VENDING_PIC = "VENDING_PIC";
        public static final String VENDING_SELECT_FLAG = "VENDING_SELECT_FLAG";
        public static final String VENDING_WAY = "VENDING_WAY";
        public static final String VENDING_WAY_PIC = "VENDING_WAY_PIC";
        public static final String VENDING_WAY_POSITION = "VENDING_WAY_POSITION";
        public static final String VENDING_WAY_SET = "VENDING_WAY_SET";
        public static final String VENDING_WAY_TITLE = "VENDING_WAY_TITLE";
    }

    public static final class AllClickContent {
        public static final String ABOUTUS = "10008";
        public static final String ABOUTUSSECOND_RETURN = "16008";
        public static final String ABOUTUS_ATTENTION = "13008";
        public static final String ABOUTUS_CONNECTUS = "12008";
        public static final String ABOUTUS_COOPERATION = "15008";
        public static final String ABOUTUS_FAQ = "14008";
        public static final String ABOUTUS_RETURN = "11008";
        public static final String ACTIVITY = "10002";
        public static final String ACTIVITY_RETURN = "11002";
        public static final String BDJ_CONCERN_BACK = "120059";
        public static final String CHANNAL = "10011";
        public static final String CHINESE = "10009";
        public static final String CONVENIENCESERVICE_ONECARD = "12007";
        public static final String CONVENIENCESERVICE_QRCODE = "13007";
        public static final String CONVENIENCESERVICE_QRCODE_END = "13027";
        public static final String CONVENIENCESERVICE_QRCODE_RETURN = "13017";
        public static final String CONVENIENCESERVICE_RETURN = "11007";
        public static final String CONVENIENCE_SERVICE = "10007";
        public static final String DUTCH = "10016";
        public static final String ENGLISH = "10010";
        public static final String FAULT_RETURN = "13004";
        public static final String HINDI = "10017";
        public static final String MOVIE = "10005";
        public static final String NOTICE = "10006";
        public static final String ONECARD_REBIND_CONFIRM = "12267";
        public static final String ONECARD_REBIND_NEXT = "12467";
        public static final String ONECARD_REBIND_RETURN = "12167";
        public static final String ONECARD_REBIND_SUCCESS_END = "12367";
        public static final String ONECARD_RECHARGE_END = "12147";
        public static final String PHONE_REBATE_CONFIRM = "120142";
        public static final String PHONE_REBATE_CONFIRM_CONFIRM = "120143";
        public static final String PHONE_REBATE_RETURN = "120141";
        public static final String PORTUGUESE = "10015";
        public static final String QUERY_CONSUMPTION_END = "12157";
        public static final String QUERY_ONECARD_CONSUMPTION_RECORD = "12057";
        public static final String QUERY_ONECARD_END = "12027";
        public static final String QUERY_ONECARD_REBIND = "12067";
        public static final String QUERY_ONECARD_RECHARGE = "12047";
        public static final String QUERY_ONECARD_RECHARGERECORD = "12037";
        public static final String QUERY_ONECARD_RETURN = "12017";
        public static final String QUERY_RECHARGE_RECORD_END = "12137";
        public static final String REBATE_ALIPAY = "120057";
        public static final String REBATE_BDJ = "120058";
        public static final String REBATE_DONATE = "120043";
        public static final String REBATE_GREENCARD = "120046";
        public static final String REBATE_GREENCARD_CONFIRM = "120246";
        public static final String REBATE_GREENCARD_RETURN = "120146";
        public static final String REBATE_ONECARD = "120042";
        public static final String REBATE_ONECARD_RECHARGENEXT = "120342";
        public static final String REBATE_ONECARD_RECHARGENOW = "120142";
        public static final String REBATE_ONECARD_RECHARGENOW_END = "120242";
        public static final String REBATE_PHONE = "120041";
        public static final String REBATE_PRINTPAPER = "120045";
        public static final String REBATE_PRINTPAPER_CONFIRM = "120245";
        public static final String REBATE_PRINTPAPER_RETURN = "120145";
        public static final String REBATE_QRCODE = "120047";
        public static final String REBATE_QRCODE_CONFIRM = "120247";
        public static final String REBATE_QRCODE_RETURN = "120147";
        public static final String REBATE_WECHAT = "120226";
        public static final String SPANISH = "10013";
        public static final String THANK_REBATE_END = "120144";
        public static final String THANK_THROWPAPER_END = "10212";
        public static final String THROWBOTLE_DEMO = "10001";
        public static final String THROWBOTTLES = "10004";
        public static final String THROWBOTTLES_NULL_RETURN = "11004";
        public static final String THROWBOTTLES_THROWPAPER_REBATE = "12004";
        public static final String THROWPAPER = "10012";
        public static final String TURKY = "10014";
        public static final String VIDEO_BACK = "11005";
        public static final String WEIXIN = "10003";
        public static final String WEIXIN_RETURN = "11003";
    }

    public static final class BarCodeFlag {
        public static final int ACTUAL_BAR_CODE = 1;
        public static final int BLACKLIST_BAR_CODE = 2;
        public static final int DEFAULT_BAR_CODE = 0;
    }

    public static final class BottleStuff {
        public static final String METAL = "1";
        public static final String PET = "0";
    }

    public static final class COM_PLC_VERSION {
        public static final String K5V1 = "K5V1";
        public static final String MCUV1 = "MCUV1";
    }

    public static final class CardStatus {
        public static final int CARD_BIND_NO = -2;
        public static final int CARD_BIND_YES = 2;
        public static final int CARD_CHARGING = 1;
        public static final int CARD_LN_BY = -3;
        public static final int CARD_UNUSABLE = -1;
        public static final int NEW_CARD = 1;
        public static final int OLD_CARD = 0;
        public static final int RECHARGE_FAIL = 5;
        public static final int RECHARGE_SUCCESS = 4;
    }

    public static final class CardType {
        public static final String ALIPAY = "ALIPAY";
        public static final String BDJ = "BDJ";
        public static final String CARD = "CARD";
        private static final String[][] CARD_TYPE;
        public static final String CONSERVICE = "CONSERVICE";
        public static final String MSG_ALIPAY = "8";
        public static final String MSG_BDJ = "10";
        public static final String MSG_CARD = "1";
        public static final String MSG_CONSERVICE = "6";
        public static final String MSG_NEW_TRANSPORTCARD = "22";
        public static final String MSG_NOCARD = "0";
        public static final String MSG_PHONE = "4";
        public static final String MSG_QRCODE = "3";
        public static final String MSG_SQRCODE = "5";
        public static final String MSG_TRANSPORTCARD = "2";
        public static final String MSG_WECHAT = "7";
        public static final String NEW_TRANSPORTCARD = "NEW_TRANSPORTCARD";
        public static final String NOCARD = "NOCARD";
        public static final String PHONE = "PHONE";
        public static final String QRCODE = "QRCODE";
        public static final String SQRCODE = "SQRCODE";
        public static final String TRANSPORTCARD = "TRANSPORTCARD";
        public static final String WECHAT = "WECHAT";

        static {
            CARD_TYPE = new String[11][];
            CARD_TYPE[0] = new String[]{"CARD", "1"};
            CARD_TYPE[1] = new String[]{"TRANSPORTCARD", "2"};
            CARD_TYPE[2] = new String[]{"QRCODE", "3"};
            CARD_TYPE[3] = new String[]{"PHONE", MSG_PHONE};
            CARD_TYPE[4] = new String[]{NOCARD, "0"};
            CARD_TYPE[5] = new String[]{"SQRCODE", MSG_SQRCODE};
            CARD_TYPE[6] = new String[]{"CONSERVICE", MSG_CONSERVICE};
            CARD_TYPE[7] = new String[]{"WECHAT", MSG_WECHAT};
            CARD_TYPE[8] = new String[]{"ALIPAY", MSG_ALIPAY};
            CARD_TYPE[9] = new String[]{NEW_TRANSPORTCARD, MSG_NEW_TRANSPORTCARD};
            CARD_TYPE[10] = new String[]{"BDJ", MSG_BDJ};
            // CARD_TYPE = VENDING_REBATE_FLAG;
        }

        public static final String getMsgCardType(String cardType) {
            for (int i = 0; i < CARD_TYPE.length; i++) {
                if (CARD_TYPE[i][0].equalsIgnoreCase(cardType)) {
                    return CARD_TYPE[i][1];
                }
            }
            return null;
        }

        public static final String getCardType(String cardType) {
            for (int i = 0; i < CARD_TYPE.length; i++) {
                if (CARD_TYPE[i][1].equalsIgnoreCase(cardType)) {
                    return CARD_TYPE[i][0];
                }
            }
            return null;
        }
    }

    public static final class ChargeStatus {
        public static final int FAIL = 0;
        public static final int SUCCESS = 1;
        public static final int UNKNOWN = -1;
    }

    public static final class ConvenienceService {
        public static final String CONSERVICE = "CONSERVICE";
        public static final String LNKCARDSTATUS = "LNKCARDSTATUS";
    }

    public static final class CouponActivite {
        public static final int APPLY_COUNT = 10;
        public static final int CANCEL_UNUPLOAD = 2;
        public static final int CANCEL_UPLOAD = 3;
        public static final int MIN_BOTTLE_COUNT = 1;
        public static final int NORMAL = 1;
    }

    public static final class DoorStatus {
        public static final String PAPER_DOOR_CLOSE = "PAPER_DOOR_CLOSE";
        public static final String PAPER_DOOR_OPEN = "PAPER_DOOR_OPEN";
    }

    public enum HardwareAlarmState {
        UNKNOWN,
        ALARM,
        NORMAL
    }

    public static final class MediaInfo {
        public static final String AUDIO = "2";
        public static final String BOTTLE_IN_ACTIVITY = "BOTTLE_IN";
        public static final String CONVENIENCE_ACTIVITY = "CONVENIENCE_SERVICE";
        public static final String DOWNLOAD = "1";
        private static final String[][] MSGPAGENUM_TO_PLAYLOCAL;
        public static final String NONE = "0";
        public static final String PICTURE = "3";
        public static final String RECHARING_ACTIVITY = "RECHARING";
        public static final String RVM_ACTIVITY = "WELCOME";
        public static final String SELECT_ACTIVITY = "SELECT";
        public static final String VIDEO = "1";

        static {
            String[][] strArr = new String[5][];
            strArr[0] = new String[]{"A", RVM_ACTIVITY};
            strArr[1] = new String[]{"B", SELECT_ACTIVITY};
            strArr[2] = new String[]{"C", BOTTLE_IN_ACTIVITY};
            strArr[3] = new String[]{"D", RECHARING_ACTIVITY};
            strArr[4] = new String[]{"E", CONVENIENCE_ACTIVITY};
            MSGPAGENUM_TO_PLAYLOCAL = strArr;
        }

        public static final String getMediaPlayLocalFromMsgPageNum(String pageNum) {
            for (int i = 0; i < MSGPAGENUM_TO_PLAYLOCAL.length; i++) {
                if (MSGPAGENUM_TO_PLAYLOCAL[i][0].equalsIgnoreCase(pageNum)) {
                    return MSGPAGENUM_TO_PLAYLOCAL[i][1];
                }
            }
            return RVM_ACTIVITY;
        }

        public static final String getMediaType(String type) {
            if ("1".equalsIgnoreCase(type)) {
                return "VIDEO";
            }
            if ("2".equalsIgnoreCase(type)) {
                return "AUDIO";
            }
            if ("3".equalsIgnoreCase(type)) {
                return "PICTURE";
            }
            return "1";
        }

        public static final int getMediaStatus(String status) {
            if (!"NONE".equals(status) && "DOWNLOAD".equals(status)) {
                return 1;
            }
            return 0;
        }
    }

    public static final class MsgType {
        public static final String BARCODE_ISSUED = "TASK_BAR";
        public static final String BIND_MESTYPE = "RVM_CARD_BIND";
        public static final String BIND_RESPONSE = "RESPONSE";
        public static final String CARD = "CARD";
        public static final String CARD_SELECT_STS = "CARD_SELECT_STS";
        public static final String COUPON = "COUPON";
        public static final String COUPONS_ISSUED = "TASK_COUPON";
        public static final String COUPONS_ISSUED_ACTIVITIES = "TASK_ACTIVITY";
        public static final String DONATION = "DONATION";
        public static final String PATROL_BARCODE_ISSUED = "TASK_WORKER_QR";
        public static final String PHONE = "PHONE";
        public static final String PICTURES_AUDIO_ISSUED = "TASK_MEDIA";
        public static final String QRCODE = "QRCODE";
        public static final String RCC_CARD_RECHANGE = "RCC_CARD_RECHANGE";
        public static final String RCC_CARD_STATUS = "RCC_CARD_STATUS";
        public static final String RCC_COUPON = "RCC_COUPON";
        public static final String RCC_LOCALID = "RCC_LOCALID";
        public static final String RCC_LTD_RECHANGE = "RCC_LTD_RECHANGE";
        public static final String RCC_RES_PARAM = "RCC_RES_PARAM";
        public static final String RCC_YKT_GGK = "RCC_YKT_GGK";
        public static final String RESPONSE = "RESPONSE";
        public static final String RVM_ALARM = "RVM_ALARM";
        public static final String RVM_BAR = "RVM_BAR";
        public static final String RVM_BIND_SIM = "RVM_BIND_SIM";
        public static final String RVM_BMCHANGE_RESULT = "RVM_BMCHANGE_RESULT";
        public static final String RVM_CARD_RECHANGE = "RVM_CARD_RECHANGE";
        public static final String RVM_CARD_STATUS = "RVM_CARD_STATUS";
        public static final String RVM_CHANGE_RESULT = "RVM_CHANGE_RESULT";
        public static final String RVM_CONFIG_ASK = "RVM_CONFIG_ASK";
        public static final String RVM_CONFIG_INFO = "RVM_CONFIG_INFO";
        public static final String RVM_COUPON_CAN = "RVM_COUPON_CAN";
        public static final String RVM_COUPON_NE = "RVM_COUPON_NE";
        public static final String RVM_COUPON_PRINT = "RVM_COUPON_PRINT";
        public static final String RVM_DAYSUM = "RVM_RECYCLE_SUM";
        public static final String RVM_INFO_UPDATE = "RVM_INFO_UPDATE";
        public static final String RVM_LTD_RECHANGE = "RVM_LTD_RECHANGE";
        public static final String RVM_MAINTAINER_OPT = "RVM_MAINTAINER_OPT";
        public static final String RVM_PING = "RVM_PING";
        public static final String RVM_RECHANGE_STATUS = "RVM_RECHANGE_STATUS";
        public static final String RVM_RECYCLE_DETAIL = "RVM_RECYCLE_DETAIL";
        public static final String RVM_REQUEST = "RVM_REQUEST";
        public static final String RVM_RESTART = "RVM_RESTART";
        public static final String RVM_STS_UPDATE = "RVM_STS_UPDATE";
        public static final String RVM_UICLICK_SUM = "RVM_UICLICK_SUM";
        public static final String RVM_UPDATE = "RVM_UPDATE";
        public static final String RVM_WORKER_REGISTER = "RVM_WORKER_REGISTER";
        public static final String RVM_YKT_GGK = "RVM_YKT_GGK";
        public static final String RVM_YKT_RESULT = "RVM_YKT_RESULT";
        public static final String RVM_YOUKU = "RVM_YOUKU";
        public static final String SCROLLBAR_ISSUED = "TASK_SBAR";
        public static final String TASK_ALARM_BOTTEL = "TASK_ALARM_BOTTEL";
        public static final String TASK_BLACK_BAR = "TASK_BLACK_BAR";
        public static final String TASK_COUPON = "TASK_COUPON";
        public static final String TASK_RVM_SLEEP = "TASK_RVM_SLEEP";
        public static final String TASK_UD_TIME = "TASK_UD_TIME";
        public static final String TASK_VOLUME = "TASK_PRICE";
        public static final String TERMINAL_SERVICES_COMMITMENT_ISSUED = "TASK_ORDER";
        public static final String TRANSPORTCARD = "TRANSPORTCARD";
        public static final String WARNING_ISSUED = "TASK_ALARM_CFG";
    }

    public static final class NewOneCardBusinessType {
        public static final int ChargeQuickCard = 3;
        public static final int ChargeScrashCard = 2;
        public static final int ChargeWriteBack = 1;
        public static final int GGK = 4;
    }

    public static final class OptStatus {
        public static final int UNDONE = 0;
        public static final int UNLAWFUL = -1;
        public static final int UNUPLOAD = 1;
        public static final int UPLOADED = 2;
    }

    public static final class OrderEnable {
        public static final int ORDER_DISABLE_RECHARGE = 0;
        public static final int ORDER_ENABLE_RECHARGE = 1;
    }

    public static final class OrderSts {
        public static final int ORDER_BLACKLIST = -1;
        public static final int ORDER_CHARGING = 1;
        public static final int ORDER_FAILED = 0;
        public static final int ORDER_LACKMONEY = -3;
        public static final int ORDER_UNBUNDLE = -2;
        public static final int ORDER_WRITABLE = 2;
    }

    public static final class PatrolBarcodeState {
        public static final int EFFECTIVE = 1;
        public static final int UNEFFECTIVE = 0;
    }

    public static final class ProductType {
        private static final String[][] PRODUCT_TYPE;
        public static final String PRODUCT_TYPE_BOTTLE = "BOTTLE";
        public static final String PRODUCT_TYPE_PAPER = "PAPER";

        static {
            PRODUCT_TYPE = new String[2][];
            PRODUCT_TYPE[0] = new String[]{"BOTTLE", "1"};
            PRODUCT_TYPE[1] = new String[]{"PAPER", "2"};
           //  PRODUCT_TYPE = VENDING_REBATE_FLAG;
        }

        public static final String getMsgProductType(String productType) {
            for (int i = 0; i < PRODUCT_TYPE.length; i++) {
                if (PRODUCT_TYPE[i][0].equalsIgnoreCase(productType)) {
                    return PRODUCT_TYPE[i][1];
                }
            }
            return null;
        }

        public static final String getProductType(String productType) {
            for (int i = 0; i < PRODUCT_TYPE.length; i++) {
                if (PRODUCT_TYPE[i][1].equalsIgnoreCase(productType)) {
                    return PRODUCT_TYPE[i][0];
                }
            }
            return null;
        }
    }

    public static final class PullDownList {
        public static final int WHAT_DID_LOAD_DATA = 0;
        public static final int WHAT_DID_MORE = 2;
        public static final int WHAT_DID_REFRESH = 1;
    }

    public static final class Recycle {
        public static final int RECYCLE_ID = 2064;
    }

    public static final class ResponseConfirm {
        public static final int CONFIRM_ERROR = -1;
        public static final int CONFIRM_FAILED = 0;
        public static final int CONFIRM_SUCCESS = 1;
    }

    public static final class RestartInform {
        public static final int TASKTIME = 300;
    }

    public static final class RvmStatus {
        public static final String RvmStart = "1";
    }

    public static final class SERVICEWAY {
        public static final String BOTTLE = "BOTTLE";
        public static final String CONSERVICE = "CONSERVICE";
        public static final String PAPER = "PAPER";
    }

    public static final class ServiceCfg {
        public static final int ALIPAY = 15;
        public static final int BDJ = 10;
        public static final int BOTTLE = 1;
        public static final int BOTTLELIMITED = 21;
        public static final int CARD = 9;
        public static final int CLEAR_NUMBER = 16;
        public static final int CLEAR_WEIGHT = 17;
        public static final int CONSERVICE = 12;
        public static final int COUPON = 3;
        public static final int DONATION = 6;
        public static final int LNKCARDSTATUS = 13;
        public static final int METAL = 20;
        public static final int PAPER = 2;
        public static final int PET = 19;
        public static final int PHONE = 7;
        public static final int PRINTER = 18;
        public static final int QRCODE = 5;
        public static final int TRANSPORTCARD = 4;
        public static final int WECHAT = 14;
    }

    public static final class ServiceName {
        public static final String BOTTLE_NUMBER_CLEAR = "CLEAR_NUMBER";
        public static final String METAL = "METAL";
        public static final String PAPER_WEIGHT_CLEAR = "CLEAR_WEIGHT";
        public static final String PET = "PET";
        public static final String PRINTER = "PRINTER";
        public static final String SCREEN_CAPTURE = "SCREEN_CAPTURE";
        public static final String TAKE_PHOTO = "TAKE_PHOTO";
    }

    public static final class ServiceOpt {
        public static final int START = 1;
        public static final int STOP = 0;
    }

    public static final class TaskPriceType {
        public static final String BOTTLE = "1";
        public static final String PAPER = "2";
    }

    public static final class TrafficType {
        public static final String BARCODE = "BARCODE";
        public static final String PICTURE = "PICTURE";
        public static final String RCC = "RCC";
        public static final String TRAFFICCARD = "TRAFFICCARD";
    }

    public static final class USESTATE {
        public static final int CONVENIENCESERVICES = 2;
        public static final int REBATING = 1;
        public static final int SELECTREBATE = 0;
    }

    public static final class VendingWay {
        public static final String ALIPAY = "ALIPAY";
        public static final String BDJ = "BDJ";
        public static final String CARD = "CARD";
        public static final String COUPON = "COUPON";
        public static final String DONATION = "DONATION";
        public static final String PHONE = "PHONE";
        public static final String QRCODE = "QRCODE";
        public static final String SQRCODE = "SQRCODE";
        public static final String TICKET = "TICKET";
        public static final String TRANSPORTCARD = "TRANSPORTCARD";
        private static final String[][] VENDING_REBATE_FLAG;
        public static final String WECHAT = "WECHAT";

        static {
            VENDING_REBATE_FLAG = new String[11][];
            VENDING_REBATE_FLAG[0] = new String[]{"CARD", "1"};
            VENDING_REBATE_FLAG[1] = new String[]{"COUPON", "2"};
            VENDING_REBATE_FLAG[2] = new String[]{TICKET, CardType.MSG_PHONE};
            VENDING_REBATE_FLAG[3] = new String[]{"DONATION", "3"};
            VENDING_REBATE_FLAG[4] = new String[]{"QRCODE", "1"};
            VENDING_REBATE_FLAG[5] = new String[]{"TRANSPORTCARD", "1"};
            VENDING_REBATE_FLAG[6] = new String[]{"PHONE", "1"};
            VENDING_REBATE_FLAG[7] = new String[]{"SQRCODE", "1"};
            VENDING_REBATE_FLAG[8] = new String[]{"WECHAT", "1"};
            VENDING_REBATE_FLAG[9] = new String[]{"ALIPAY", "1"};
            VENDING_REBATE_FLAG[10] = new String[]{"BDJ", "1"};
        }

        public static final String getMsgVendingRebateFlag(String vendingWay) {
            for (int i = 0; i < VENDING_REBATE_FLAG.length; i++) {
                if (VENDING_REBATE_FLAG[i][0].equalsIgnoreCase(vendingWay)) {
                    return VENDING_REBATE_FLAG[i][1];
                }
            }
            return null;
        }
    }

    public static final class WeighMax {
        public static final double CHECKTIME = 0.5d;
        public static final double WEIGHMAX = 8.0d;
    }

    public static final class audioCurrentState {
        public static final String CHECKED = "CHECKED";
        public static final String CHECKING = "CHECKING";
    }

    public static final class loginType {
        public static final String loginByName = "1";
        public static final String loginByQRcode = "2";
    }

    public static final class maintainOptContent {
        public static final String BOTTLE_CLEAR = "02000";
        public static final String CONFIG_SAVE = "02003";
        public static final String OPEN_BOTTLE_DOOR = "02001";
        public static final String OPEN_PAPER_DOOR = "01001";
        public static final String PAPER_CLEAR = "01000";
        public static final String SET_ALARM_VALUE = "02002";
        public static final String SET_HAS_DOOR = "02101";
        public static final String SET_NO_DOOR = "02100";
        public static final String SET_RVM_OFF_TIME = "02005";
        public static final String SET_RVM_ON_TIME = "02004";
        public static final String START_ALIPAY = "22001";
        public static final String START_BDJ = "23001";
        public static final String START_BOTTLELIMITED = "30101";
        public static final String START_CARD = "17001";
        public static final String START_CONVENIENCE = "19001";
        public static final String START_COUPON = "16001";
        public static final String START_DONATION = "13001";
        public static final String START_LNKCARD = "18001";
        public static final String START_ONECARD = "14001";
        public static final String START_PHONE = "15001";
        public static final String START_PRINT = "21001";
        public static final String START_RECYCLE_BOTTLE = "12001";
        public static final String START_RECYCLE_PAPER = "11001";
        public static final String START_WECHAT = "20001";
        public static final String STOP_ALIPAY = "22000";
        public static final String STOP_BDJ = "23000";
        public static final String STOP_BOTTLELIMITED = "30100";
        public static final String STOP_CARD = "17000";
        public static final String STOP_CONVENIENCE = "19000";
        public static final String STOP_COUPON = "16000";
        public static final String STOP_DONATION = "13000";
        public static final String STOP_LNKCARD = "18000";
        public static final String STOP_ONECARD = "14000";
        public static final String STOP_PHONE = "15000";
        public static final String STOP_PRINT = "21000";
        public static final String STOP_RECYCLE_BOTTLE = "12000";
        public static final String STOP_RECYCLE_PAPER = "11000";
        public static final String STOP_WECHAT = "20000";
    }

    public static final class networkSts {
        public static String NETWORK_STS = NetworkStateMgr.NETWORK_FAILED;
        public static long tcpTotal = 0;
    }

    public static final class staffPermission {
        public static String ADVANCE_STAFF = "1";
        public static String GENERAL_STAFF = "0";
        public static String TimingBoot = "TimingBoot";
        public static String advance = "advance";
        public static String cfgSeting = "cfgSeting";
        public static String checkIn = "checkIn";
        public static String cleanNum = "cleanNum";
        public static String cleanWeight = "cleanWeight";
        public static String dwonOpenDoor = "dwonOpenDoor";
        public static String dwonOpenPaperDoor = "dwonOpenPaperDoor";
        public static String faultDiagnosis = "faultDiagnosis";
        public static String heartToneDetection = "heartToneDetection";
        public static String networkTest = "networkTest";
        public static String openCloseVoice = "openCloseVoice";
        public static String recycleBottleList = "recycleBottleList";
        public static String serSeting = "serSeting";
        public static String setBottleMaxNum = "setBottleMaxNum";
        public static String setOnOffDoor = "setOnOffDoor";
        public static String start_using = "start_using";
        public static String systemSeting = "systemSeting";
        public static String wringList = "wringList";
    }

    public static final class updateDetection {
        public static final String FTP = "FTP";
        public static final String RVM_REQUEST = "RVM_REQUEST";
        public static final String VERSION = "VERSION";
    }

    static {
        SERVICE_OF_OPT_TYPE = new String[22][];
        SERVICE_OF_OPT_TYPE[0] = new String[]{"1", "BOTTLE"};
        SERVICE_OF_OPT_TYPE[1] = new String[]{"2", "PAPER"};
        SERVICE_OF_OPT_TYPE[2] = new String[]{"3", "COUPON"};
        SERVICE_OF_OPT_TYPE[3] = new String[]{CardType.MSG_PHONE, "TRANSPORTCARD"};
        SERVICE_OF_OPT_TYPE[4] = new String[]{CardType.MSG_SQRCODE, "QRCODE"};
        SERVICE_OF_OPT_TYPE[5] = new String[]{CardType.MSG_CONSERVICE, "DONATION"};
        SERVICE_OF_OPT_TYPE[6] = new String[]{CardType.MSG_WECHAT, "PHONE"};
        SERVICE_OF_OPT_TYPE[7] = new String[]{"9", "CARD"};
        SERVICE_OF_OPT_TYPE[8] = new String[]{CardType.MSG_BDJ, "BDJ"};
        SERVICE_OF_OPT_TYPE[9] = new String[]{"12", "CONSERVICE"};
        SERVICE_OF_OPT_TYPE[10] = new String[]{"13", ConvenienceService.LNKCARDSTATUS};
        SERVICE_OF_OPT_TYPE[11] = new String[]{"14", "WECHAT"};
        SERVICE_OF_OPT_TYPE[12] = new String[]{"15", "ALIPAY"};
        SERVICE_OF_OPT_TYPE[13] = new String[]{"16", ServiceName.BOTTLE_NUMBER_CLEAR};
        SERVICE_OF_OPT_TYPE[14] = new String[]{"17", ServiceName.PAPER_WEIGHT_CLEAR};
        SERVICE_OF_OPT_TYPE[15] = new String[]{"18", ServiceName.PRINTER};
        SERVICE_OF_OPT_TYPE[16] = new String[]{"19", ServiceName.PET};
        SERVICE_OF_OPT_TYPE[17] = new String[]{"20", ServiceName.METAL};
        SERVICE_OF_OPT_TYPE[18] = new String[]{"21", "BOTTLELIMITED"};
        SERVICE_OF_OPT_TYPE[19] = new String[]{"24", ServiceName.SCREEN_CAPTURE};
        SERVICE_OF_OPT_TYPE[20] = new String[]{"25", ServiceName.TAKE_PHOTO};
        SERVICE_OF_OPT_TYPE[21] = new String[]{"28", "FENXUANQI"};
    }

    public static final int getOptSeq() {
        int i;
        synchronized (optSeq) {
            optSeq[0] = (optSeq[0] + 1) % 100000000;
            i = optSeq[0];
        }
        return i;
    }

    public static final String formatBound(String val, int scale) {
        if (StringUtils.isBlank(val)) {
            return val;
        }
        if (val.equals("-1--1")) {
            return "-1";
        }
        if (val.equals("-1")) {
            return val;
        }
        int div = val.indexOf(SimpleFormatter.DEFAULT_DELIMITER);
        if (div != -1) {
            return NumberUtils.toScale(val.substring(0, div), scale) + SimpleFormatter.DEFAULT_DELIMITER + NumberUtils.toScale(val.substring(div + 1), scale);
        }
        val = NumberUtils.toScale(val, scale);
        return val + SimpleFormatter.DEFAULT_DELIMITER + val;
    }
}