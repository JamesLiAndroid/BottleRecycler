package com.incomrecycle.prms.rvm.common;

import com.incomrecycle.common.utils.EncryptUtils;

public class ResolveCode {
    private static final String charSet = "UTF-8";

    public static String resolveQRcode(String codeString) {
        byte[] src = null;
        try {
            src = EncryptUtils.DESDecryptVi(EncryptUtils.base64_byte(codeString), "recyclE8".getBytes(charSet), "incom013s".getBytes(charSet));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (src == null) {
            return null;
        }
        try {
            return new String(src, charSet);
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }
}
