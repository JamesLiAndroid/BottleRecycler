package com.incomrecycle.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
    public static InputStream close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static OutputStream close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static byte[] read(InputStream is) {
        byte[] buff = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (true) {
            try {
                int readLen = is.read(buff);
                if (readLen <= 0) {
                    break;
                }
                baos.write(buff, 0, readLen);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return baos.toByteArray();
    }

    public static long dump(InputStream is, OutputStream os) {
        byte[] buff = new byte[1024];
        long lLen = 0;
        while (true) {
            try {
                int readLen = is.read(buff);
                if (readLen <= 0) {
                    return lLen;
                }
                os.write(buff, 0, readLen);
                lLen += (long) readLen;
            } catch (IOException e) {
                return 0;
            }
        }
    }

    public static byte[] readFile(String filename) {
        Throwable th;
        byte[] bArr = null;
        File file = new File(filename);
        if (file.isFile()) {
            InputStream is = null;
            try {
                InputStream is2 = new FileInputStream(file);
                if (is2 == null) {
                    close(is2);
                } else {
                    try {
                        bArr = read(is2);
                        close(is2);
                    } catch (Exception e) {
                        is = is2;
                        close(is);
                        return bArr;
                    } catch (Throwable th2) {
                        th = th2;
                        is = is2;
                        close(is);
                        throw th;
                    }
                }
            } catch (Exception e2) {
                close(is);
                return bArr;
            } catch (Throwable th3) {
                close(is);
            }
        }
        return bArr;
    }

    public static boolean writeFile(String filename, byte[] data) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(data);
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static InputStream getResourceAsInputStream(String resource) {
        return IOUtils.class.getClassLoader().getResourceAsStream(resource);
    }

    public static byte[] readResource(String resource) {
        byte[] bArr = null;
        InputStream is = null;
        try {
            is = IOUtils.class.getClassLoader().getResourceAsStream(resource);
            if (is != null) {
                bArr = read(is);
                close(is);
            }
        } catch (Exception e) {
        } finally {
            close(is);
        }
        return bArr;
    }
}
