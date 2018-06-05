package com.incomrecycle.prms.rvm.service.comm.entity.trafficcard;

import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.MotionEventCompat;
import com.incomrecycle.common.utils.EncryptUtils;

public enum FrameItemType {
    BLOCK,
    HEX,
    BCD,
    BIT,
    ASCII,
    H_ASCII,
    B_ASCII;
    
    public static final String PATCH_STR = "000000000000000000000000000000000000000000000000000000000000";

    public static void setString(byte[] data, int pos, int len, String val) {
        byte[] buff = val.getBytes();
        for (int i = 0; i < len; i++) {
            if (i < buff.length) {
                data[pos + i] = buff[i];
            } else {
                data[pos + i] = (byte) 0;
            }
        }
    }

    public static void setString(byte[] data, int pos, String val) {
        byte[] buff = val.getBytes();
        System.arraycopy(buff, 0, data, pos, buff.length);
    }

    public static String getString(byte[] data, int pos, int len) {
        return new String(data, pos, len);
    }

    public static void setLong2BCD(byte[] data, int pos, int len, long val) {
        String sVal = PATCH_STR + val;
        System.arraycopy(EncryptUtils.hex2byte(sVal.substring(sVal.length() - (len * 2))), 0, data, pos, len);
    }

    public static long getBCD2Long(byte[] data, int pos, int len) {
        return Long.parseLong(EncryptUtils.byte2hex(data, pos, len));
    }

    public static void setCRC2Byte4(byte[] data, int pos, int crc) {
        String val = PATCH_STR + Integer.toHexString(SupportMenu.USER_MASK & crc);
        byte[] bVal = EncryptUtils.hex2byte(val.substring(val.length() - 4));
        System.arraycopy(bVal, 0, data, pos, bVal.length);
        data[bVal.length + pos] = (byte) -1;
        data[(bVal.length + pos) + 1] = (byte) -1;
    }

    public static int getByte4CRC(byte[] data, int pos) {
        return Integer.parseInt(EncryptUtils.byte2hex(new byte[]{data[pos], data[pos + 1]}), 16);
    }

    public static void setByte(byte[] data, int pos, byte b) {
        data[pos] = b;
    }

    public static byte getByte(byte[] data, int pos) {
        return data[pos];
    }

    public static String getBCD2String(byte[] data, int pos, int len) {
        return EncryptUtils.byte2hex(data, pos, len);
    }

    public static void setString2BCD(byte[] data, int pos, int len, String sVal) {
        if (sVal.length() < len * 2) {
            sVal = PATCH_STR + sVal;
        }
        System.arraycopy(EncryptUtils.hex2byte(sVal.substring(sVal.length() - (len * 2))), 0, data, pos, len);
    }

    public static String getHEX2String(byte[] data, int pos, int len) {
        return EncryptUtils.byte2hex(data, pos, len);
    }

    public static void setString2HEX(byte[] data, int pos, int len, String sVal) {
        if (sVal.length() != len * 2) {
            sVal = (sVal + PATCH_STR).substring(0, len * 2);
        }
        System.arraycopy(EncryptUtils.hex2byte(sVal), 0, data, pos, len);
    }

    public static void setLong2HEX(byte[] data, int pos, int len, long val) {
        String sVal = PATCH_STR + Long.toHexString(val);
        byte[] bVal = EncryptUtils.hex2byte(sVal.substring(sVal.length() - (len + len)));
        for (int i = 0; i < len; i++) {
            data[pos + i] = bVal[(bVal.length - i) - 1];
        }
    }

    public static long getHEX2Long(byte[] data, int pos, int len) {
        byte[] buff = new byte[len];
        for (int i = 0; i < len; i++) {
            buff[i] = data[((pos + len) - 1) - i];
        }
        return Long.parseLong(EncryptUtils.byte2hex(buff), 16);
    }

    public static long getHEX2Balance(byte[] data, int pos, int len) {
        byte[] buff = new byte[len];
        for (int i = 0; i < len; i++) {
            buff[i] = data[((pos + len) - 1) - i];
        }
        String sVal = EncryptUtils.byte2hex(buff);
        if (!sVal.startsWith("FF") && !sVal.startsWith("ff")) {
            return Long.parseLong(sVal, 16);
        }
        String sHead = "100";
        while (sHead.length() <= sVal.length()) {
            sHead = sHead + "0";
        }
        return Long.parseLong(sVal, 16) - Long.parseLong(sHead, 16);
    }

    public static long getHString2Long(byte[] data, int pos, int len) {
        byte[] bVal = EncryptUtils.hex2byte(new String(data, pos, len));
        byte[] buff = new byte[bVal.length];
        for (int i = 0; i < bVal.length; i++) {
            buff[i] = bVal[(bVal.length - 1) - i];
        }
        return Long.parseLong(EncryptUtils.byte2hex(buff), 16);
    }

    public static long getHString2Balance(byte[] data, int pos, int len) {
        byte[] bVal = EncryptUtils.hex2byte(new String(data, pos, len));
        byte[] buff = new byte[bVal.length];
        for (int i = 0; i < bVal.length; i++) {
            buff[i] = bVal[(bVal.length - 1) - i];
        }
        String sVal = EncryptUtils.byte2hex(buff);
        if (!sVal.startsWith("FF") && !sVal.startsWith("ff")) {
            return Long.parseLong(sVal, 16);
        }
        String sHead = "100";
        while (sHead.length() <= sVal.length()) {
            sHead = sHead + "0";
        }
        return Long.parseLong(sVal, 16) - Long.parseLong(sHead, 16);
    }

    public static void setLong2HString(byte[] data, int pos, int len, long value) {
        String val = PATCH_STR + Long.toHexString(value);
        byte[] bVal = EncryptUtils.hex2byte(val.substring(val.length() - len));
        EncryptUtils.revert(bVal, 0, bVal.length);
        byte[] v = EncryptUtils.byte2hex(bVal).toUpperCase().getBytes();
        System.arraycopy(v, 0, data, pos, v.length);
    }

    public static void setLong2BString(byte[] data, int pos, int len, long value) {
        String val = PATCH_STR + Long.toString(value);
        byte[] v = val.substring(val.length() - len).getBytes();
        System.arraycopy(v, 0, data, pos, v.length);
    }

    public static long getBString2Long(byte[] data, int pos, int len) {
        return Long.parseLong(new String(data, pos, len));
    }

    public static int getBIT(byte[] data, int pos, int len) {
        int bit = 0;
        for (int i = 0; i < len; i++) {
            bit = ((bit << 8) & -256) + (data[pos + i] & MotionEventCompat.ACTION_MASK);
        }
        return bit;
    }

    public static void setBIT(byte[] data, int pos, int len, int bit) {
        for (int i = 0; i < len; i++) {
            data[((pos + len) - 1) - i] = (byte) (bit & MotionEventCompat.ACTION_MASK);
            bit >>= 8;
        }
    }
}
