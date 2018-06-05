package com.incomrecycle.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

public class PropUtils {
    private static final String ENCRYPT_FLAG = "ENCRYPT_FLAG";
    private static final byte[] RC4KEY = "incomrecycle".getBytes();
    private static final Object objSync = new Object();

    public static Properties loadFile(String propFile) {
        if (StringUtils.isBlank(propFile)) {
            return loadFile((File) null);
        }
        return loadFile(new File(propFile));
    }

    public static Properties loadFile(File file) {
        return loadFile(file, null);
    }

    public static Properties loadFile(File file, String charSetName){
        Throwable th;
        Properties prop = new Properties();
        if (file != null) {
            synchronized (objSync) {
                InputStream is = null;
                try {
                    if (file.isFile()) {
                        InputStream is2 = new FileInputStream(file);
                        try {
                            prop = loadInputStream(is2, charSetName);
                            try {
                                IOUtils.close(is2);
                            } catch (Throwable th2) {
                                th = th2;
                                is = is2;
                                throw th;
                            }
                        } catch (Exception e) {
                            is = is2;
                            IOUtils.close(is);
                            return prop;
                        } catch (Throwable th3) {
                            th = th3;
                            is = is2;
                            IOUtils.close(is);
                            throw th;
                        }
                    }
                    IOUtils.close(is);
                } catch (Exception e2) {
                    IOUtils.close(is);
                    return prop;
                } catch (Throwable th4) {
                    try {
                        throw th4;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }
        }
        return prop;
    }

    public static Properties loadInputStream(InputStream is, String charSetName) {
        if (StringUtils.isBlank(charSetName)) {
            return loadInputStream(is);
        }
        byte[] data = IOUtils.read(is);
        Properties prop = new Properties();
        if (data == null) {
            return prop;
        }
        try {
            String[] ss = StringUtils.replace(new String(data, charSetName), "\r", "\n").split("\n");
            for (int i = 0; i < ss.length; i++) {
                String sm = ss[i].trim();
                if (!(sm.length() == 0 || sm.startsWith("#"))) {
                    int div = ss[i].indexOf("=");
                    if (div != -1) {
                        String sh = ss[i].substring(0, div).trim();
                        if (sh.length() != 0) {
                            prop.setProperty(sh, StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(ss[i].substring(div + 1),
                                    "\\=", "="), "\\.", "."), "\\:", ":"), "\\'", "'"),
                                    "\\\"", "\""), "\\r", "\r"), "\\n", "\n"), "\\\\", "\\"));
                        }
                    }
                }
            }
            return prop;
        } catch (Exception e) {
            e.printStackTrace();
            return prop;
        }
    }

    public static Properties loadInputStream(InputStream is) {
        Properties prop = new Properties();
        if (is != null) {
            synchronized (objSync) {
                try {
                    prop.load(is);
                } catch (Exception e) {
                }
            }
        }
        return prop;
    }

    public static Properties loadResource(String resource) {
        Properties prop = new Properties();
        if (StringUtils.isBlank(resource)) {
            return prop;
        }
        boolean isLoaded = false;
        if (!resource.endsWith(".properties")) {
            synchronized (objSync) {
                try {
                    ResourceBundle rb = ResourceBundle.getBundle(resource);
                    for (String name : rb.keySet()) {
                        prop.setProperty(name, rb.getString(name));
                    }
                    isLoaded = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (!isLoaded) {
            InputStream inputStream = null;
            try {
                inputStream = IOUtils.getResourceAsInputStream(resource);
                prop = loadInputStream(inputStream);
            } catch (Exception e2) {
            }
            IOUtils.close(inputStream);
        }
        return prop;
    }

    public static boolean update(String propFile, Properties prop) {
        return update(new File(propFile), prop);
    }

    public static boolean update(File file, Properties prop) {
        boolean hasChanged;
        Throwable th;
        boolean z = false;
        Properties externalProp = new Properties();
        synchronized (objSync) {
            FileOutputStream fos;
            FileInputStream is = null;
            try {
                if (file.isFile()) {
                    FileInputStream is2 = new FileInputStream(file);
                    try {
                        externalProp.load(is2);
                        is = is2;
                    } catch (Exception e) {
                        is = is2;
                        if (is != null) {
                            try {
                                is.close();
                            } catch (Exception e2) {
                            }
                        }
                        hasChanged = false;
                        for (String key : prop.stringPropertyNames()) {
                            if (!StringUtils.trimToEmpty(prop.getProperty(key)).equals(StringUtils.trimToEmpty(externalProp.getProperty(key)))) {
                                hasChanged = true;
                                break;
                            }
                        }
                        if (hasChanged) {
                            externalProp.putAll(prop);
                            fos = new FileOutputStream(file, false);
                            externalProp.store(fos, null);
                            fos.flush();
                            fos.close();
                        }
                        z = true;
                        return z;
                    } catch (Throwable th2) {
                        th = th2;
                        is = is2;
                        if (is != null) {
                            try {
                                is.close();
                            } catch (Exception e3) {
                            }
                        }
                        throw th;
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e4) {
                    }
                }
            } catch (Exception e5) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                hasChanged = false;
                for (String key2 : prop.stringPropertyNames()) {
                    if (StringUtils.trimToEmpty(prop.getProperty(key2)).equals(StringUtils.trimToEmpty(externalProp.getProperty(key2)))) {
                        hasChanged = true;
                        break;
                    }
                }
                if (hasChanged) {
                    externalProp.putAll(prop);
                    try {
                        fos = new FileOutputStream(file, false);
                        externalProp.store(fos, null);
                        fos.flush();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                z = true;
                return z;
            } catch (Throwable th3) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    throw th3;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
            hasChanged = false;
            try {
                for (String key22 : prop.stringPropertyNames()) {
                    if (StringUtils.trimToEmpty(prop.getProperty(key22)).equals(StringUtils.trimToEmpty(externalProp.getProperty(key22)))) {
                        hasChanged = true;
                        break;
                    }
                }
                if (hasChanged) {
                    externalProp.putAll(prop);
                    fos = new FileOutputStream(file, false);
                    externalProp.store(fos, null);
                    fos.flush();
                    fos.close();
                }
                z = true;
            } catch (Exception e6) {
            }
        }
        return z;
    }

    public static boolean save(File file, Properties prop) {
        boolean z = false;
        synchronized (objSync) {
            try {
                FileOutputStream fos = new FileOutputStream(file, false);
                prop.store(fos, null);
                fos.flush();
                fos.close();
                z = true;
            } catch (Exception e) {
            }
        }
        return z;
    }

    public static Properties loadEncryptFile(File file) {
        Throwable th;
        Properties prop = new Properties();
        if (file == null) {
            return prop;
        }
        ByteArrayInputStream bais = null;
        synchronized (objSync) {
            try {
                if (file.isFile()) {
                    byte[] data = IOUtils.readFile(file.getAbsolutePath());
                    if (data == null || data.length == 0) {
                        if (bais != null) {
                            try {
                                bais.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        return prop;
                    }
                    ByteArrayInputStream bais2 = new ByteArrayInputStream(EncryptUtils.RC4(data, RC4KEY));
                    try {
                        Properties propEncrypt = loadInputStream(bais2);
                        if (hasEncryptFlag(propEncrypt)) {
                            if (bais2 != null) {
                                try {
                                    bais2.close();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                } catch (Throwable th3) {
                                    th = th3;
                                    bais = bais2;
                                    throw th;
                                }
                            }
                            return propEncrypt;
                        }
                        bais = bais2;
                    } catch (Exception e3) {
                        bais = bais2;
                        if (bais != null) {
                            try {
                                bais.close();
                            } catch (Exception e22) {
                                e22.printStackTrace();
                            }
                        }
                        return prop;
                    } catch (Throwable th4) {
                        th = th4;
                        bais = bais2;
                        if (bais != null) {
                            try {
                                bais.close();
                            } catch (Exception e222) {
                                e222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
                if (bais != null) {
                    try {
                        bais.close();
                    } catch (Exception e2222) {
                        e2222.printStackTrace();
                    }
                }
            } catch (Exception e4) {
                if (bais != null) {
                    try {
                        bais.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return prop;
            } catch (Throwable th5) {
                if (bais != null) {
                    try {
                        bais.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    throw th5;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
            return prop;
        }
    }

    public static void transferEncryptFile(File file) {
        synchronized (objSync) {
            try {
                if (file.isFile()) {
                    saveEncryptFile(file, loadFile(file));
                }
            } catch (Exception e) {
            }
        }
    }

    public static boolean updateEncryptFile(File file, Properties prop) {
        boolean z;
        Properties externalProp = null;
        synchronized (objSync) {
           //  Properties externalProp2;
            try {
                if (file.isFile()) {
                    externalProp = loadEncryptFile(file);
                }
                // = externalProp;
            } catch (Exception e) {
                externalProp = null;
            }
            if (externalProp == null) {
                try {
                    externalProp = new Properties();
                } catch (Exception e3) {
                    z = false;
                    return z;
                } catch (Throwable th) {

                }
            }
            boolean hasChanged = false;
            for (String key : prop.stringPropertyNames()) {
                if (!StringUtils.trimToEmpty(prop.getProperty(key)).equals(StringUtils.trimToEmpty(externalProp.getProperty(key)))) {
                    hasChanged = true;
                    break;
                }
            }
            if (hasChanged) {
                externalProp.putAll(prop);
                saveEncryptFile(file, prop);
            }
            z = true;
        }
        return z;
    }

    public static boolean saveEncryptFile(File file, Properties prop) {
        boolean z = false;
        synchronized (objSync) {
            try {
                prop.setProperty(ENCRYPT_FLAG, "TRUE");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                prop.store(baos, null);
                byte[] data = EncryptUtils.RC4(baos.toByteArray(), RC4KEY);
                FileOutputStream fos = new FileOutputStream(file, false);
                fos.write(data);
                fos.flush();
                fos.close();
                z = true;
            } catch (Exception e) {
            }
        }
        return z;
    }

    public static boolean hasEncryptFlag(Properties prop) {
        if (prop == null) {
            return false;
        }
        return "TRUE".equalsIgnoreCase(prop.getProperty(ENCRYPT_FLAG));
    }
}
