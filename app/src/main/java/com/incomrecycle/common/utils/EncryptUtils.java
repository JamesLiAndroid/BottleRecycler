package com.incomrecycle.common.utils;

import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.MotionEventCompat;
import android.util.Base64;
import java.security.Key;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtils {
    public static byte[] DESEncryptVi(byte[] src, byte[] key, byte[] iv) throws Exception {
        return DESVi("DES/CBC/PKCS5Padding", src, formatDESKey(key, 8), formatDESKey(iv, 8), 1);
    }

    public static byte[] DESDecryptVi(byte[] encsrc, byte[] key, byte[] iv) throws Exception {
        return DESVi("DES/CBC/PKCS5Padding", encsrc, formatDESKey(key, 8), formatDESKey(iv, 8), 2);
    }

    public static byte[] DESEncrypt(byte[] src, byte[] key) throws Exception {
        return DES("DES", src, formatDESKey(key, 8), 1);
    }

    public static byte[] DESDecrypt(byte[] encsrc, byte[] key) throws Exception {
        return DES("DES", encsrc, formatDESKey(key, 8), 2);
    }

    public static byte[] DES3Encrypt(byte[] src, byte[] key) throws Exception {
        return DES("DESede", src, formatDESKey(key, 24), 1);
    }

    public static byte[] DES3Decrypt(byte[] encsrc, byte[] key) throws Exception {
        return DES("DESede", encsrc, formatDESKey(key, 24), 2);
    }

    public static byte[] DESVi(String algorithm, byte[] encsrc, byte[] key, byte[] iv, int MODE) throws Exception {
        DESKeySpec keySpec = new DESKeySpec(key);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        Key k = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(MODE, k, ivParameterSpec);
        return cipher.doFinal(encsrc);
    }

    public static byte[] RC4(byte[] src, byte[] key) {
        final int[] array3 = new int[256];
        final byte[] array4 = new byte[256];
        for (int i = 0; i < 256; ++i) {
            array3[i] = i;
        }
        for (int j = 0; j < 256; j = (short)(j + 1)) {
            array4[j] = key[j % key.length];
        }
        int n = 0;
        for (int k = 0; k < 256; ++k) {
            n = (array3[k] + n + array4[k]) % 256;
            final int n2 = array3[k];
            array3[k] = array3[n];
            array3[n] = n2;
        }
        int n3 = 0;
        int n4 = 0;
        key = new byte[src.length];
        for (int l = 0; l < src.length; l = (short)(l + 1)) {
            n3 = (n3 + 1) % 256;
            n4 = (array3[n3] + n4) % 256;
            final int n5 = array3[n3];
            array3[n3] = array3[n4];
            array3[n4] = n5;
            key[l] = (byte)(src[l] ^ array3[(array3[n3] + array3[n4] % 256) % 256]);
        }
        return key;
    }

    private static byte[] formatDESKey(byte[] key, int length) {
        if (key.length == length) {
            return key;
        }
        int i;
        byte[] newkey = new byte[length];
        for (i = 0; i < length; i++) {
            newkey[i] = (byte) 0;
        }
        i = 0;
        while (i < key.length && i < length) {
            newkey[i] = key[i];
            i++;
        }
        return newkey;
    }

    private static byte[] DES(String algorithm, byte[] src, byte[] key, int MODE) throws Exception {
        SecretKey securekey = new SecretKeySpec(key, algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(MODE, securekey);
        return cipher.doFinal(src);
    }

    public static byte[] encryptMD5(byte[] src, byte[] key) {
        int b = 0;
        int idx = 0;
        byte[] mod = md5(key);
        byte[] seed = new byte[(key.length + mod.length)];
        System.arraycopy(mod, 0, seed, 0, mod.length);
        System.arraycopy(key, 0, seed, mod.length, key.length);
        mod = md5(seed);
        byte[] res = new byte[src.length];
        while (idx < src.length) {
            res[idx] = (byte) (src[idx] + mod[b]);
            idx++;
            b++;
            if (b == mod.length && idx < src.length) {
                System.arraycopy(src, idx - mod.length, seed, 0, mod.length);
                mod = md5(seed);
                b = 0;
            }
        }
        return res;
    }

    public static byte[] decryptMD5(byte[] src, byte[] key) {
        int b = 0;
        int idx = 0;
        byte[] mod = md5(key);
        byte[] seed = new byte[(key.length + mod.length)];
        System.arraycopy(mod, 0, seed, 0, mod.length);
        System.arraycopy(key, 0, seed, mod.length, key.length);
        mod = md5(seed);
        byte[] res = new byte[src.length];
        while (idx < src.length) {
            res[idx] = (byte) (src[idx] - mod[b]);
            idx++;
            b++;
            if (b == mod.length && idx < src.length) {
                System.arraycopy(res, idx - mod.length, seed, 0, mod.length);
                mod = md5(seed);
                b = 0;
            }
        }
        return res;
    }

    public static final String md5(String[] strSrcs) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strSrcs.length; i++) {
            if (strSrcs[i] != null) {
                sb.append(strSrcs[i]);
            }
        }
        return md5(sb.toString());
    }

    public static final byte[] messageDigest(String algorithm, byte[] src) {
        try {
            return MessageDigest.getInstance(algorithm).digest(src);
        } catch (Exception e) {
            return null;
        }
    }

    public static final byte[] md5(byte[] src) {
        return messageDigest("MD5", src);
    }

    public static final String md5(String src) {
        return byte2hex(messageDigest("MD5", src.getBytes()));
    }

    public static final String sha1(String src) {
        return byte2hex(messageDigest("SHA-1", src.getBytes()));
    }

    public static String byte_base64(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.encodeToString(data, 0);
    }

    public static byte[] base64_byte(String str) {
        byte[] bArr = null;
        if (str != null) {
            try {
                bArr = Base64.decode(str, 0);
            } catch (Exception e) {
            }
        }
        return bArr;
    }

    public static String byte2hex(byte[] data) {
        return byte2hex(data, 0, data.length);
    }

    public static String byte2hex(byte[] data, int offset, int length) {
        if (data == null) {
            return null;
        }
        if (data.length <= offset) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            String strByte = Integer.toHexString(data[i + offset]);
            if (strByte.length() < 2) {
                strByte = "0" + strByte;
            }
            sb.append(strByte.substring(strByte.length() - 2));
        }
        return sb.toString();
    }

    public static byte[] hex2byte(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] data = new byte[(str.length() / 2)];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) Integer.parseInt(str.substring(i * 2, (i * 2) + 2), 16);
        }
        return data;
    }

    public static void htonl(int v, byte[] buff, int offset) {
        buff[offset + 0] = (byte) ((v >> 24) & MotionEventCompat.ACTION_MASK);
        buff[offset + 1] = (byte) ((v >> 16) & MotionEventCompat.ACTION_MASK);
        buff[offset + 2] = (byte) ((v >> 8) & MotionEventCompat.ACTION_MASK);
        buff[offset + 3] = (byte) (v & MotionEventCompat.ACTION_MASK);
    }

    public static int ntohl(byte[] buff, int offset) {
        return (((0 + ((buff[offset + 0] << 24) & -16777216)) + ((buff[offset + 1] << 16) & 16711680)) + ((buff[offset + 2] << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK)) + (buff[offset + 3] & MotionEventCompat.ACTION_MASK);
    }

    public static void htons(int v, byte[] buff, int offset) {
        buff[offset + 0] = (byte) ((v >> 8) & MotionEventCompat.ACTION_MASK);
        buff[offset + 1] = (byte) (v & MotionEventCompat.ACTION_MASK);
    }

    public static int ntohs(byte[] buff, int offset) {
        return (0 + ((buff[offset + 0] << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK)) + (buff[offset + 1] & MotionEventCompat.ACTION_MASK);
    }

    public static void revert(byte[] data, int pos, int len) {
        for (int i = 0; i < len / 2; i++) {
            byte b = data[pos + i];
            data[pos + i] = data[((pos + len) - 1) - i];
            data[((pos + len) - 1) - i] = b;
        }
    }

    public static int CRC16(byte[] data, int pos, int len) {
        int crc = 0;
        int deltaPos = 0;
        int len2 = len;
        while (true) {
            len = len2 - 1;
            if (len2 == 0) {
                return crc;
            }
            for (int i = 128; i != 0; i /= 2) {
                if ((32768 & crc) != 0) {
                    crc = ((crc << 1) & SupportMenu.USER_MASK) ^ 4129;
                } else {
                    crc = (crc << 1) & SupportMenu.USER_MASK;
                }
                if ((data[pos + deltaPos] & i) != 0) {
                    crc ^= 4129;
                }
            }
            deltaPos++;
            len2 = len;
        }
    }
}
