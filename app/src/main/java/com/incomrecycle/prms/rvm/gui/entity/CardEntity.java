package com.incomrecycle.prms.rvm.gui.entity;

public class CardEntity {
    public static int BALANCE_IN_CARD = 0;
    public static String CARD_NO = null;
    public static int CARD_STATUS = 2;
    public static String CREDIT = null;
    public static double INCOM_AMOUNT = 0.0d;
    public static int IS_RECHANGE = 0;
    public static double RECHARGED = 0.0d;
    public static int RECHARGE_STATE = 0;
    public static int VERSION = 0;
    public static boolean isValid = false;

    public static void Reset() {
        CARD_STATUS = 2;
        IS_RECHANGE = 0;
        CARD_NO = null;
        INCOM_AMOUNT = 0.0d;
        RECHARGE_STATE = 0;
        BALANCE_IN_CARD = 0;
        RECHARGED = 0.0d;
        isValid = false;
        CREDIT = null;
        VERSION = 0;
    }
}
