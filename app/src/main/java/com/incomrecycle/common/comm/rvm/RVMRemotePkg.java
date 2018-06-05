package com.incomrecycle.common.comm.rvm;

public class RVMRemotePkg {
    private byte[] body;
    private byte[] pkgData;

    public static boolean isEndFlag(byte b) {
        return b == (byte) 0;
    }

    public static RVMRemotePkg parse(byte[] pkg) {
        for (int i = 0; i < pkg.length; i++) {
            if (pkg[i] == (byte) 0) {
                RVMRemotePkg pkgObj = new RVMRemotePkg();
                pkgObj.pkgData = new byte[(i + 1)];
                System.arraycopy(pkg, 0, pkgObj.pkgData, 0, pkgObj.pkgData.length);
                pkgObj.body = null;
                return pkgObj;
            }
        }
        return null;
    }

    public int getLength() {
        body2pkg();
        if (this.pkgData != null) {
            return this.pkgData.length;
        }
        return 0;
    }

    public byte[] getPkgData() {
        body2pkg();
        return this.pkgData;
    }

    public byte[] getBody() {
        pkg2body();
        return this.body;
    }

    public void setBody(byte[] body) {
        this.body = new byte[body.length];
        System.arraycopy(body, 0, this.body, 0, body.length);
        this.pkgData = null;
    }

    private void body2pkg() {
        if (this.pkgData == null && this.body != null) {
            this.pkgData = new byte[(this.body.length + 1)];
            System.arraycopy(this.body, 0, this.pkgData, 0, this.body.length);
            this.pkgData[this.body.length] = (byte) 0;
        }
    }

    private void pkg2body() {
        if (this.body == null && this.pkgData != null) {
            this.body = new byte[(this.pkgData.length - 1)];
            System.arraycopy(this.pkgData, 0, this.body, 0, this.body.length);
        }
    }
}
