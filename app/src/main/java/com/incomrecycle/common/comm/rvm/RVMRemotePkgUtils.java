package com.incomrecycle.common.comm.rvm;

import com.incomrecycle.common.utils.EncryptUtils;
import com.incomrecycle.common.utils.ZipUtils;

public class RVMRemotePkgUtils {
    public static void send(RVMRemoteSession rvmRemoteSession, byte[] des3Key, String s) throws Exception {
        if (s != null && !"".equals(s)) {
            byte[] body = ZipUtils.compress(s.getBytes("utf-8"));
            if (des3Key != null) {
                body = EncryptUtils.byte_base64(EncryptUtils.DES3Encrypt(body, des3Key)).getBytes();
            }
            RVMRemotePkg rvmRemotePkg = new RVMRemotePkg();
            rvmRemotePkg.setBody(body);
            rvmRemoteSession.send(rvmRemotePkg);
        }
    }

    public static String read(RVMRemoteSession rvmRemoteSession, byte[] des3Key) throws Exception {
        RVMRemotePkg retRVMRemotePkg = rvmRemoteSession.read();
        if (retRVMRemotePkg == null) {
            return null;
        }
        byte[] retBody = retRVMRemotePkg.getBody();
        if (des3Key != null) {
            retBody = EncryptUtils.DES3Decrypt(EncryptUtils.base64_byte(new String(retBody)), des3Key);
        }
        return new String(ZipUtils.unCompress(retBody), "utf-8");
    }
}
