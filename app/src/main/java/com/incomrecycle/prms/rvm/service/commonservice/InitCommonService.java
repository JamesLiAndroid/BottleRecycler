package com.incomrecycle.prms.rvm.service.commonservice;

import android.database.sqlite.SQLiteDatabase;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.commtable.CommTable;
import com.incomrecycle.common.commtable.CommTableRecord;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.sqlite.DBQuery;
import com.incomrecycle.common.sqlite.DBSequence;
import com.incomrecycle.common.sqlite.SQLiteExecutor;
import com.incomrecycle.common.sqlite.SqlBuilder;
import com.incomrecycle.common.sqlite.SqlInsertBuilder;
import com.incomrecycle.common.sqlite.SqlUpdateBuilder;
import com.incomrecycle.common.sqlite.SqlWhereBuilder;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.PropUtils;
import com.incomrecycle.common.utils.ShellUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.common.utils.ZipUtils;
import com.incomrecycle.prms.rvm.common.RVMShell;
import com.incomrecycle.prms.rvm.common.SysDef;
import com.incomrecycle.prms.rvm.common.SysDef.MediaInfo;
import com.incomrecycle.prms.rvm.common.SysDef.MsgType;
import com.incomrecycle.prms.rvm.common.SysDef.TrafficType;
import com.incomrecycle.prms.rvm.common.SysDef.maintainOptContent;
import com.incomrecycle.prms.rvm.common.SysDef.updateDetection;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import com.incomrecycle.prms.rvm.service.AppCommonService;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import com.incomrecycle.prms.rvm.service.traffic.TrafficEntity;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

public class InitCommonService implements AppCommonService {
    private static final Logger logger = LoggerFactory.getLogger("INIT");

    public HashMap execute(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        if ("initRVM".equalsIgnoreCase(subSvnName)) {
            logger.debug("InitCommonService Init...");
            try {
                rvmCodeRestore(svcName, subSvnName, hsmpParam);
            } catch (Exception e) {
                logger.debug("rvmCodeRestore error" + e);
            }
            initSettings(svcName, subSvnName, hsmpParam);
            initDBSql(svcName, subSvnName, hsmpParam);
            initDBSeq(svcName, subSvnName, hsmpParam);
            initBarcodePrice(svcName, subSvnName, hsmpParam);
            initBarcode(svcName, subSvnName, hsmpParam);
            initMedia(svcName, subSvnName, hsmpParam);
            initRVMDaemon(svcName, subSvnName, hsmpParam);
            initAlarmSetting(svcName, subSvnName, hsmpParam);
            initRVMStatus(svcName, subSvnName, hsmpParam);
        }
        if ("initSettings".equalsIgnoreCase(subSvnName)) {
            return initSettings(svcName, subSvnName, hsmpParam);
        }
        if ("initBarcodePrice".equalsIgnoreCase(subSvnName)) {
            return initBarcodePrice(svcName, subSvnName, hsmpParam);
        }
        if ("initBarcode".equalsIgnoreCase(subSvnName)) {
            return initBarcode(svcName, subSvnName, hsmpParam);
        }
        if ("initMedia".equalsIgnoreCase(subSvnName)) {
            return initMedia(svcName, subSvnName, hsmpParam);
        }
        if ("initDB".equalsIgnoreCase(subSvnName)) {
            return initDBSql(svcName, subSvnName, hsmpParam);
        }
        if ("initRVMDaemon".equalsIgnoreCase(subSvnName)) {
            initRVMDaemon(svcName, subSvnName, hsmpParam);
        }
        if ("downloadBarcode".equalsIgnoreCase(subSvnName)) {
            return downloadBarcode(svcName, subSvnName, hsmpParam);
        }
        if ("rvmCodeBackup".equalsIgnoreCase(subSvnName)) {
            HashMap newHashMap = new HashMap();
            newHashMap.put("IS_CLEAR", "TRUE");
            return rvmCodeRestore(svcName, subSvnName, newHashMap);
        } else if ("rvmCodeRestore".equalsIgnoreCase(subSvnName)) {
            return rvmCodeRestore(svcName, subSvnName, hsmpParam);
        } else {
            return null;
        }
    }

    public HashMap initSettings(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
        sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "SERVICE_DISABLED_SET");
        CommTable commTableSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
        String BOTTLES_LIMITED_ENABLE = "TRUE";
        if (commTableSysCode.getRecordCount() > 0) {
            String SERVICE_DISABLED_SET = commTableSysCode.getRecord(0).get("SYS_CODE_VALUE");
            if (!StringUtils.isBlank(SERVICE_DISABLED_SET) && SERVICE_DISABLED_SET.contains("BOTTLELIMITED")) {
                BOTTLES_LIMITED_ENABLE = "FALSE";
            }
        }
        logger.debug("BOTTLES_LIMITED_ENABLE=" + BOTTLES_LIMITED_ENABLE);
        String BOTTLES_LIMITED = SysConfig.get("BOTTLES.LIMITED");
        String BOTTLES_UNLIMITED = SysConfig.get("BOTTLES.UNLIMITED");
        String BOTTLES_LIMITED_UPDATE = BOTTLES_LIMITED;
        if (!StringUtils.isBlank(BOTTLES_LIMITED_ENABLE)) {
            SysConfig.set("BOTTLES_LIMITED_ENABLE", BOTTLES_LIMITED_ENABLE);
            if ("FALSE".equalsIgnoreCase(BOTTLES_LIMITED_ENABLE) && !StringUtils.isBlank(BOTTLES_UNLIMITED)) {
                BOTTLES_LIMITED_UPDATE = BOTTLES_UNLIMITED;
            }
        }
        if (!StringUtils.isBlank(BOTTLES_LIMITED_UPDATE)) {
            HashMap<String, String> updateValues = StringUtils.toHashMap(BOTTLES_LIMITED_UPDATE, ";", "=");
            for (String KEY : updateValues.keySet()) {
                String VALUE = (String) updateValues.get(KEY);
                SysConfig.set(KEY, VALUE);
                logger.debug(KEY + "=" + VALUE);
            }
        }
        return null;
    }

    public HashMap initBarcodePrice(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String BARCODE_PRICE_INIT_PATH = SysConfig.get("RVM.INIT.PATH.BARCODE.PRICE");
        if (!StringUtils.isBlank(BARCODE_PRICE_INIT_PATH)) {
            File file = new File(BARCODE_PRICE_INIT_PATH);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    int i;
                    CommTableRecord ctr;
                    File file2;
                    String filename;
                    long filetime;
                    SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
                    CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_HANDLED_FILE");
                    HashMap<String, String> hsmpFilename = new HashMap();
                    for (i = 0; i < commTable.getRecordCount(); i++) {
                        ctr = commTable.getRecord(i);
                        hsmpFilename.put(ctr.get("FILE_NAME"), ctr.get("FILE_TIME"));
                    }
                    String[] BARCODE_PRICE_ATTR_FIELDS = SysConfig.get("BARCODE_PRICE_ATTR_FIELDS").split(",");
                    int BARCODE_PRICE_ATTR_FIELD_COUNT = 0;
                    HashMap<String, Integer> hsmpPriceAttrDataFieldIndex = new HashMap();
                    HashMap<String, String> hsmpPriceAttrDataFieldType = new HashMap();
                    int f = 0;
                    while (f < BARCODE_PRICE_ATTR_FIELDS.length) {
                        BARCODE_PRICE_ATTR_FIELDS[f] = BARCODE_PRICE_ATTR_FIELDS[f].trim().toUpperCase();
                        if (BARCODE_PRICE_ATTR_FIELDS[f].length() != 0) {
                            int idx = BARCODE_PRICE_ATTR_FIELDS[f].indexOf(":");
                            if (idx == -1) {
                                break;
                            }
                            String FIELD_NAME = BARCODE_PRICE_ATTR_FIELDS[f].substring(0, idx);
                            String FIELD_TYPE = BARCODE_PRICE_ATTR_FIELDS[f].substring(idx + 1);
                            hsmpPriceAttrDataFieldIndex.put(FIELD_NAME, Integer.valueOf(f));
                            hsmpPriceAttrDataFieldType.put(FIELD_NAME, FIELD_TYPE);
                            BARCODE_PRICE_ATTR_FIELDS[f] = FIELD_NAME;
                            BARCODE_PRICE_ATTR_FIELD_COUNT++;
                            f++;
                        } else {
                            break;
                        }
                    }
                    HashMap<String, String[]> hsmpBarcodePrice = new HashMap();
                    List<File> listFile = new ArrayList();
                    for (File file22 : files) {
                        if (file22.isFile()) {
                            filename = file22.getAbsolutePath();
                            filetime = file22.lastModified();
                            String dbfiletime = (String) hsmpFilename.get(filename);
                            if (dbfiletime == null || Long.parseLong(dbfiletime) != filetime) {
                                String[] FILE_FIELDS = null;
                                InputStream fileInputStream = new FileInputStream(file22);
                                try {
                                    DataInputStream dataInputStream = new DataInputStream(fileInputStream);
                                    while (true) {
                                        String barcodeInfo = dataInputStream.readLine();
                                        if (barcodeInfo == null) {
                                            break;
                                        }
                                        barcodeInfo = barcodeInfo.trim();
                                        if (!(barcodeInfo.length() == 0 || barcodeInfo.startsWith("#"))) {
                                            String[] barcodeItems = barcodeInfo.split(",");
                                            if (FILE_FIELDS == null) {
                                                for (f = 0; f < barcodeItems.length; f++) {
                                                    barcodeItems[f] = barcodeItems[f].trim().toUpperCase();
                                                    if (barcodeItems[f].length() == 0) {
                                                        FILE_FIELDS = new String[f];
                                                        break;
                                                    }
                                                }
                                                if (FILE_FIELDS == null) {
                                                    FILE_FIELDS = new String[barcodeItems.length];
                                                }
                                                for (f = 0; f < FILE_FIELDS.length; f++) {
                                                    FILE_FIELDS[f] = barcodeItems[f];
                                                }
                                            } else {
                                                if (barcodeItems.length >= FILE_FIELDS.length) {
                                                    String[] data = new String[BARCODE_PRICE_ATTR_FIELD_COUNT];
                                                    for (f = 0; f < data.length; f++) {
                                                        data[f] = null;
                                                    }
                                                    for (f = 0; f < FILE_FIELDS.length; f++) {
                                                        data[((Integer) hsmpPriceAttrDataFieldIndex.get(FILE_FIELDS[f])).intValue()] = barcodeItems[f].trim();
                                                    }
                                                    if (StringUtils.isBlank(data[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_PRICE_PRIORITY")).intValue()])) {
                                                        data[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_PRICE_PRIORITY")).intValue()] = "100";
                                                    }
                                                    if (StringUtils.isBlank(data[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_AMOUNT")).intValue()])) {
                                                        data[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_AMOUNT")).intValue()] = "0";
                                                    }
                                                    for (f = 0; f < data.length; f++) {
                                                        if (StringUtils.isBlank(data[f])) {
                                                            data[f] = "-1";
                                                        }
                                                    }
                                                    data[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_VOL")).intValue()] = SysDef.formatBound(data[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_VOL")).intValue()], 2);
                                                    data[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_WEIGH")).intValue()] = SysDef.formatBound(data[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_WEIGH")).intValue()], 2);
                                                    hsmpBarcodePrice.put(RVMUtils.generateBarcodePriceKey(data[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_VOL")).intValue()], data[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_STUFF")).intValue()], data[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_WEIGH")).intValue()], data[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_COLOR")).intValue()]), data);
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                } finally {
                                    IOUtils.close(fileInputStream);
                                }
                                listFile.add(file22);
                            }
                        }
                    }
                    if (!hsmpBarcodePrice.isEmpty()) {
                        List<String> listSql = new ArrayList();
                        CommTable ctBarCodePrice = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_BAR_CODE_PRICE");
                        HashMap<String, CommTableRecord> hsmpBarCodePriceCtr = new HashMap();
                        for (i = 0; i < ctBarCodePrice.getRecordCount(); i++) {
                            ctr = ctBarCodePrice.getRecord(i);
                            hsmpBarCodePriceCtr.put(RVMUtils.generateBarcodePriceKey(ctr.get("BAR_CODE_VOL"), ctr.get("BAR_CODE_STUFF"), ctr.get("BAR_CODE_WEIGH"), ctr.get("BAR_CODE_COLOR")), ctr);
                        }
                        listSql.add("CREATE TABLE IF NOT EXISTS RVM_BAR_CODE_PRICE_IMPORT ( BAR_CODE_PRICE_KEY VARCHAR(128) NOT NULL, BAR_CODE_VOL VARCHAR(60), BAR_CODE_AMOUNT DECIMAL(15,2) NOT NULL, BAR_CODE_STUFF VARCHAR(20) NOT NULL,AREA_ID VARCHAR(20) NOT NULL,BAR_CODE_WEIGH VARCHAR(60) NOT NULL,BAR_CODE_COLOR VARCHAR(20) NOT NULL,VERSION VARCHAR(20) NOT NULL,BAR_CODE_PRICE_PRIORITY DECIMAL(8) NOT NULL,PRIMARY KEY(BAR_CODE_PRICE_KEY) ) ");
                        listSql.add("DELETE FROM RVM_BAR_CODE_PRICE_IMPORT");
                        SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_BAR_CODE_PRICE_IMPORT");
                        String VERSION = null;
                        for (String KEY : hsmpBarcodePrice.keySet()) {
                            String[] data2 = (String[]) hsmpBarcodePrice.get(KEY);
                            sqlInsertBuilder.newInsertRecord().setString("BAR_CODE_PRICE_KEY", KEY).setString("BAR_CODE_VOL", data2[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_VOL")).intValue()]).setNumber("BAR_CODE_STUFF", data2[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_STUFF")).intValue()]).setString("BAR_CODE_WEIGH", data2[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_WEIGH")).intValue()]).setString("BAR_CODE_COLOR", data2[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_COLOR")).intValue()]).setNumber("BAR_CODE_AMOUNT", data2[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_AMOUNT")).intValue()]).setNumber("BAR_CODE_PRICE_PRIORITY", data2[((Integer) hsmpPriceAttrDataFieldIndex.get("BAR_CODE_PRICE_PRIORITY")).intValue()]).setString("AREA_ID", SysConfig.get("RVM.AREA.CODE")).setString(updateDetection.VERSION, data2[((Integer) hsmpPriceAttrDataFieldIndex.get(updateDetection.VERSION)).intValue()]);
                            if (VERSION == null) {
                                VERSION = data2[((Integer) hsmpPriceAttrDataFieldIndex.get(updateDetection.VERSION)).intValue()];
                            }
                        }
                        listSql.add(sqlInsertBuilder.toSql());
                        listSql.add("UPDATE RVM_BAR_CODE_PRICE SET BAR_CODE_AMOUNT=(select a.BAR_CODE_AMOUNT FROM RVM_BAR_CODE_PRICE_IMPORT a where RVM_BAR_CODE_PRICE.BAR_CODE_PRICE_KEY=a.BAR_CODE_PRICE_KEY) ,   BAR_CODE_PRICE_PRIORITY=(select a.BAR_CODE_PRICE_PRIORITY FROM RVM_BAR_CODE_PRICE_IMPORT a where RVM_BAR_CODE_PRICE.BAR_CODE_PRICE_KEY=a.BAR_CODE_PRICE_KEY) ,   VERSION=(select a.VERSION FROM RVM_BAR_CODE_PRICE_IMPORT a where RVM_BAR_CODE_PRICE.BAR_CODE_PRICE_KEY=a.BAR_CODE_PRICE_KEY) ,   AREA_ID=(select a.AREA_ID FROM RVM_BAR_CODE_PRICE_IMPORT a where RVM_BAR_CODE_PRICE.BAR_CODE_PRICE_KEY=a.BAR_CODE_PRICE_KEY) where exists(select 1 FROM RVM_BAR_CODE_PRICE_IMPORT a where RVM_BAR_CODE_PRICE.BAR_CODE_PRICE_KEY=a.BAR_CODE_PRICE_KEY)");
                        listSql.add("DELETE FROM RVM_BAR_CODE_PRICE_IMPORT WHERE EXISTS (SELECT 1 from RVM_BAR_CODE_PRICE a where a.BAR_CODE_PRICE_KEY=RVM_BAR_CODE_PRICE_IMPORT.BAR_CODE_PRICE_KEY)");
                        listSql.add("INSERT INTO RVM_BAR_CODE_PRICE(BAR_CODE_PRICE_KEY,BAR_CODE_VOL,BAR_CODE_STUFF,BAR_CODE_WEIGH,BAR_CODE_COLOR,BAR_CODE_AMOUNT,BAR_CODE_PRICE_PRIORITY,AREA_ID,VERSION)  SELECT BAR_CODE_PRICE_KEY,BAR_CODE_VOL,BAR_CODE_STUFF,BAR_CODE_WEIGH,BAR_CODE_COLOR,BAR_CODE_AMOUNT,BAR_CODE_PRICE_PRIORITY,AREA_ID,VERSION FROM RVM_BAR_CODE_PRICE_IMPORT");
                        SqlInsertBuilder SqlInsertBuilderHandledFile = new SqlInsertBuilder("RVM_HANDLED_FILE");
                        for (i = 0; i < listFile.size(); i++) {
                            File file22 = (File) listFile.get(i);
                            filename = file22.getAbsolutePath();
                            filetime = file22.lastModified();
                            if (((String) hsmpFilename.get(filename)) != null) {
                                listSql.add("UPDATE RVM_HANDLED_FILE SET FILE_TIME='" + filetime + "' WHERE FILE_NAME='" + filename + "'");
                            } else {
                                SqlInsertBuilderHandledFile.newInsertRecord().setString("FILE_NAME", filename).setString("FILE_TIME", Long.valueOf(filetime));
                            }
                        }
                        listSql.add(SqlInsertBuilderHandledFile.toSql());
                        SQLiteExecutor.execSql(sqliteDatabase, (List) listSql);
                        listSql.clear();
                        SQLiteExecutor.execSql(sqliteDatabase, (List) listSql);
                        if (!VERSION.equals(SysConfig.get("RVM_PRICE_VERSION"))) {
                            Properties prop = new Properties();
                            prop.setProperty("RVM_PRICE_VERSION", VERSION);
                            PropUtils.update(new File(SysConfig.get("EXTERNAL.FILE")), prop);
                            SysConfig.set(prop);
                            RVMShell.backupExternalConfig();
                        }
                        RVMBarcodePriceMgr.getMgr().load(0, true);
                    }
                }
            }
        }
        return null;
    }

    public HashMap initBarcode(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String BARCODE_INIT_PATH = SysConfig.get("RVM.INIT.PATH.BARCODE");
        if (!StringUtils.isBlank(BARCODE_INIT_PATH)) {
            File file = new File(BARCODE_INIT_PATH);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    int i;
                    int f;
                    File file2;
                    String filename;
                    long filetime;
                    String[] barcodeItems;
                    SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
                    CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_HANDLED_FILE");
                    HashMap<String, String> hsmpFilename = new HashMap();
                    for (i = 0; i < commTable.getRecordCount(); i++) {
                        CommTableRecord ctr = commTable.getRecord(i);
                        hsmpFilename.put(ctr.get("FILE_NAME"), ctr.get("FILE_TIME"));
                    }
                    String KEY_FIELD = "BAR_CODE";
                    String[] BARCODE_IMPORT_FIELDS = SysConfig.get("BARCODE_IMPORT_FIELDS").split(",");
                    int BARCODE_IMPORT_FIELD_COUNT = 0;
                    HashMap<String, Integer> hsmpDataFieldIndex = new HashMap();
                    for (f = 0; f < BARCODE_IMPORT_FIELDS.length; f++) {
                        BARCODE_IMPORT_FIELDS[f] = BARCODE_IMPORT_FIELDS[f].trim().toUpperCase();
                        if (BARCODE_IMPORT_FIELDS[f].length() == 0) {
                            break;
                        }
                        hsmpDataFieldIndex.put(BARCODE_IMPORT_FIELDS[f], Integer.valueOf(f));
                        BARCODE_IMPORT_FIELD_COUNT++;
                    }
                    String[] BARCODE_ATTR_FIELDS = SysConfig.get("BARCODE_ATTR_FIELDS").split(",");
                    int BARCODE_ATTR_FIELD_COUNT = 0;
                    HashMap<String, Integer> hsmpAttrDataFieldIndex = new HashMap();
                    HashMap<String, String> hsmpAttrDataFieldType = new HashMap();
                    f = 0;
                    while (f < BARCODE_ATTR_FIELDS.length) {
                        BARCODE_ATTR_FIELDS[f] = BARCODE_ATTR_FIELDS[f].trim().toUpperCase();
                        if (BARCODE_ATTR_FIELDS[f].length() != 0) {
                            int idx = BARCODE_ATTR_FIELDS[f].indexOf(":");
                            if (idx == -1) {
                                break;
                            }
                            String FIELD_NAME = BARCODE_ATTR_FIELDS[f].substring(0, idx);
                            String FIELD_TYPE = BARCODE_ATTR_FIELDS[f].substring(idx + 1);
                            hsmpAttrDataFieldIndex.put(FIELD_NAME, Integer.valueOf(f));
                            hsmpAttrDataFieldType.put(FIELD_NAME, FIELD_TYPE);
                            BARCODE_ATTR_FIELDS[f] = FIELD_NAME;
                            BARCODE_ATTR_FIELD_COUNT++;
                            f++;
                        } else {
                            break;
                        }
                    }
                    HashMap<String, String[]> hsmpBarcode = new HashMap();
                    List<File> listFile = new ArrayList();
                    for (File file22 : files) {
                        if (file22.isFile()) {
                            filename = file22.getAbsolutePath();
                            filetime = file22.lastModified();
                            String dbfiletime = (String) hsmpFilename.get(filename);
                            if (dbfiletime == null || Long.parseLong(dbfiletime) != filetime) {
                                String[] FILE_FIELDS = null;
                                InputStream fileInputStream = new FileInputStream(file22);
                                try {
                                    DataInputStream dataInputStream = new DataInputStream(fileInputStream);
                                    while (true) {
                                        String barcodeInfo = dataInputStream.readLine();
                                        if (barcodeInfo == null) {
                                            break;
                                        }
                                        barcodeInfo = barcodeInfo.trim();
                                        if (!(barcodeInfo.length() == 0 || barcodeInfo.startsWith("#"))) {
                                            barcodeItems = barcodeInfo.split(",");
                                            if (FILE_FIELDS == null) {
                                                for (f = 0; f < barcodeItems.length; f++) {
                                                    barcodeItems[f] = barcodeItems[f].trim().toUpperCase();
                                                    if (barcodeItems[f].length() == 0) {
                                                        FILE_FIELDS = new String[f];
                                                        break;
                                                    }
                                                }
                                                if (FILE_FIELDS == null) {
                                                    FILE_FIELDS = new String[barcodeItems.length];
                                                }
                                                for (f = 0; f < FILE_FIELDS.length; f++) {
                                                    FILE_FIELDS[f] = barcodeItems[f];
                                                }
                                            } else {
                                                if (barcodeItems.length >= FILE_FIELDS.length) {
                                                    String[] data = new String[BARCODE_IMPORT_FIELD_COUNT];
                                                    for (f = 0; f < data.length; f++) {
                                                        data[f] = null;
                                                    }
                                                    for (f = 0; f < FILE_FIELDS.length; f++) {
                                                        data[((Integer) hsmpDataFieldIndex.get(FILE_FIELDS[f])).intValue()] = barcodeItems[f].trim();
                                                    }
                                                    hsmpBarcode.put(data[((Integer) hsmpDataFieldIndex.get(KEY_FIELD)).intValue()], data);
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                } finally {
                                    IOUtils.close(fileInputStream);
                                }
                                listFile.add(file22);
                            }
                        }
                    }
                    if (!hsmpBarcode.isEmpty()) {
                        List<String> listSql = new ArrayList();
                        listSql.add("CREATE TABLE IF NOT EXISTS RVM_BAR_CODE_IMPORT ( BAR_CODE VARCHAR(60) NOT NULL, BAR_CODE_VOL DECIMAL(15,2) NOT NULL, BAR_CODE_FLAG INTEGER NOT NULL, BAR_CODE_ATTR_ID INTEGER NOT NULL, BAR_CODE_WEIGH DECIMAL(15,2) NOT NULL, BAR_CODE_COLOR VARCHAR(20) NOT NULL,BAR_CODE_STUFF VARCHAR(20) NOT NULL, PRIMARY KEY(BAR_CODE) ) ");
                        listSql.add("DELETE FROM RVM_BAR_CODE_IMPORT");
                        SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_BAR_CODE_IMPORT");
                        int count = 0;
                        for (String barcode : hsmpBarcode.keySet()) {
                            barcodeItems = (String[]) hsmpBarcode.get(barcode);
                            sqlInsertBuilder.newInsertRecord().setString("BAR_CODE", barcode).setNumber("BAR_CODE_ATTR_ID", Integer.valueOf(0)).setNumber("BAR_CODE_VOL", barcodeItems[((Integer) hsmpDataFieldIndex.get("BAR_CODE_VOL")).intValue()]).setNumber("BAR_CODE_WEIGH", barcodeItems[((Integer) hsmpDataFieldIndex.get("BAR_CODE_WEIGH")).intValue()]).setString("BAR_CODE_COLOR", barcodeItems[((Integer) hsmpDataFieldIndex.get("BAR_CODE_COLOR")).intValue()]).setString("BAR_CODE_STUFF", barcodeItems[((Integer) hsmpDataFieldIndex.get("BAR_CODE_STUFF")).intValue()]).setNumber("BAR_CODE_FLAG", Integer.valueOf(1));
                            count++;
                            if (count > 100) {
                                listSql.add(sqlInsertBuilder.toSql());
                                sqlInsertBuilder = new SqlInsertBuilder("RVM_BAR_CODE_IMPORT");
                                count = 0;
                            }
                        }
                        if (count > 0) {
                            listSql.add(sqlInsertBuilder.toSql());
                        }
                        listSql.add("DELETE FROM RVM_BAR_CODE WHERE EXISTS (SELECT BAR_CODE from RVM_BAR_CODE_IMPORT a where a.BAR_CODE=RVM_BAR_CODE.BAR_CODE)");
                        listSql.add("INSERT INTO RVM_BAR_CODE(BAR_CODE,BAR_CODE_VOL,BAR_CODE_FLAG,BAR_CODE_ATTR_ID,BAR_CODE_WEIGH,BAR_CODE_COLOR,BAR_CODE_STUFF) SELECT BAR_CODE,BAR_CODE_VOL,BAR_CODE_FLAG,BAR_CODE_ATTR_ID,BAR_CODE_WEIGH,BAR_CODE_COLOR,BAR_CODE_STUFF FROM RVM_BAR_CODE_IMPORT");
                        listSql.add("DELETE FROM RVM_BAR_CODE_IMPORT");
                        SqlInsertBuilder SqlInsertBuilderHandledFile = new SqlInsertBuilder("RVM_HANDLED_FILE");
                        for (i = 0; i < listFile.size(); i++) {
                            File file22 = (File) listFile.get(i);
                            filename = file22.getAbsolutePath();
                            filetime = file22.lastModified();
                            if (((String) hsmpFilename.get(filename)) != null) {
                                listSql.add("UPDATE RVM_HANDLED_FILE SET FILE_TIME='" + filetime + "' WHERE FILE_NAME='" + filename + "'");
                            } else {
                                SqlInsertBuilderHandledFile.newInsertRecord().setString("FILE_NAME", filename).setString("FILE_TIME", Long.valueOf(filetime));
                            }
                        }
                        listSql.add(SqlInsertBuilderHandledFile.toSql());
                        SQLiteExecutor.execSql(sqliteDatabase, (List) listSql);
                    }
                }
            }
        }
        return null;
    }

    public HashMap initMedia(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String MEDIA_INIT_PATH = SysConfig.get("RVM.INIT.PATH.MEDIA");
        if (!StringUtils.isBlank(MEDIA_INIT_PATH)) {
            File file = new File(MEDIA_INIT_PATH);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    int i;
                    CommTableRecord ctr;
                    int f;
                    File file2;
                    String filename;
                    long filetime;
                    String[] data;
                    SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
                    CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_HANDLED_FILE");
                    HashMap<String, String> hsmpFilename = new HashMap();
                    for (i = 0; i < commTable.getRecordCount(); i++) {
                        ctr = commTable.getRecord(i);
                        hsmpFilename.put(ctr.get("FILE_NAME"), ctr.get("FILE_TIME"));
                    }
                    HashMap<String, CommTableRecord> hsmpRVMMedia = new HashMap();
                    HashMap<Integer, String> hsmpRVMMediaId = new HashMap();
                    CommTable commTableMedia = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_MEDIA");
                    for (i = 0; i < commTableMedia.getRecordCount(); i++) {
                        ctr = commTableMedia.getRecord(i);
                        hsmpRVMMedia.put(ctr.get("FILE_PATH") + "|" + ctr.get("MEDIA_PLAY_LOCAL"), ctr);
                        hsmpRVMMediaId.put(Integer.valueOf(Integer.parseInt(ctr.get("MEDIA_ID"))), ctr.get("FILE_PATH"));
                    }
                    HashMap<String, String[]> hsmpMedia = new HashMap();
                    String KEY_FIELD = "FILE_PATH";
                    String[] MEDIA_IMPORT_FIELDS = SysConfig.get("MEDIA_IMPORT_FIELDS").split(",");
                    int MEDIA_IMPORT_FIELD_COUNT = 0;
                    HashMap<String, Integer> hsmpDataFieldIndex = new HashMap();
                    for (f = 0; f < MEDIA_IMPORT_FIELDS.length; f++) {
                        MEDIA_IMPORT_FIELDS[f] = MEDIA_IMPORT_FIELDS[f].trim().toUpperCase();
                        if (MEDIA_IMPORT_FIELDS[f].length() == 0) {
                            break;
                        }
                        hsmpDataFieldIndex.put(MEDIA_IMPORT_FIELDS[f], Integer.valueOf(f));
                        MEDIA_IMPORT_FIELD_COUNT++;
                    }
                    List<File> listFile = new ArrayList();
                    for (File file22 : files) {
                        if (file22.isFile()) {
                            filename = file22.getAbsolutePath();
                            filetime = file22.lastModified();
                            String dbfiletime = (String) hsmpFilename.get(filename);
                            if (dbfiletime == null || Long.parseLong(dbfiletime) != filetime) {
                                String[] FILE_FIELDS = null;
                                InputStream fileInputStream = new FileInputStream(file22);
                                try {
                                    DataInputStream dataInputStream = new DataInputStream(fileInputStream);
                                    while (true) {
                                        String mediaInfo = dataInputStream.readLine();
                                        if (mediaInfo == null) {
                                            break;
                                        }
                                        mediaInfo = mediaInfo.trim();
                                        if (!(mediaInfo.length() == 0 || mediaInfo.startsWith("#"))) {
                                            String[] mediaItems = mediaInfo.split(",");
                                            if (FILE_FIELDS == null) {
                                                for (f = 0; f < mediaItems.length; f++) {
                                                    mediaItems[f] = mediaItems[f].trim().toUpperCase();
                                                    if (mediaItems[f].length() == 0) {
                                                        FILE_FIELDS = new String[f];
                                                        break;
                                                    }
                                                }
                                                if (FILE_FIELDS == null) {
                                                    FILE_FIELDS = new String[mediaItems.length];
                                                }
                                                for (f = 0; f < FILE_FIELDS.length; f++) {
                                                    FILE_FIELDS[f] = mediaItems[f];
                                                }
                                            } else {
                                                if (mediaItems.length >= FILE_FIELDS.length) {
                                                    data = new String[MEDIA_IMPORT_FIELD_COUNT];
                                                    for (f = 0; f < data.length; f++) {
                                                        data[f] = null;
                                                    }
                                                    for (f = 0; f < FILE_FIELDS.length; f++) {
                                                        data[((Integer) hsmpDataFieldIndex.get(FILE_FIELDS[f])).intValue()] = mediaItems[f].trim();
                                                    }
                                                    hsmpMedia.put(data[((Integer) hsmpDataFieldIndex.get(KEY_FIELD)).intValue()] + "|" + data[((Integer) hsmpDataFieldIndex.get("MEDIA_PLAY_LOCAL")).intValue()], data);
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                } finally {
                                    IOUtils.close(fileInputStream);
                                }
                                listFile.add(file22);
                            }
                        }
                    }
                    if (!hsmpMedia.isEmpty()) {
                        int mediaId = 900000001;
                        List<String> listSql = new ArrayList();
                        SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder("RVM_MEDIA");
                        for (String key : hsmpMedia.keySet()) {
                            data = (String[]) hsmpMedia.get(key);
                            data[((Integer) hsmpDataFieldIndex.get("MEDIA_PLAY_LOCAL")).intValue()] = MediaInfo.getMediaPlayLocalFromMsgPageNum(data[((Integer) hsmpDataFieldIndex.get("MEDIA_PLAY_LOCAL")).intValue()]);
                            ctr = (CommTableRecord) hsmpRVMMedia.get(data[((Integer) hsmpDataFieldIndex.get(KEY_FIELD)).intValue()] + "|" + data[((Integer) hsmpDataFieldIndex.get("MEDIA_PLAY_LOCAL")).intValue()]);
                            if (ctr == null) {
                                while (hsmpRVMMediaId.get(Integer.valueOf(mediaId)) != null) {
                                    mediaId++;
                                }
                                hsmpRVMMediaId.put(Integer.valueOf(mediaId), data[((Integer) hsmpDataFieldIndex.get("FILE_PATH")).intValue()]);
                                if (StringUtils.isBlank(data[((Integer) hsmpDataFieldIndex.get("MEDIA_PRIORITY")).intValue()])) {
                                    data[((Integer) hsmpDataFieldIndex.get("MEDIA_PRIORITY")).intValue()] = "1";
                                }
                                sqlInsertBuilder.newInsertRecord().setNumber("MEDIA_ID", Integer.valueOf(mediaId)).setString("MEDIA_URL", null).setNumber("MEDIA_TYPE", data[((Integer) hsmpDataFieldIndex.get("MEDIA_TYPE")).intValue()]).setNumber("DOWNLOAD_FLAG", Integer.valueOf(1)).setString("FILE_PATH", data[((Integer) hsmpDataFieldIndex.get("FILE_PATH")).intValue()]).setString("MEDIA_PLAY_LOCAL", data[((Integer) hsmpDataFieldIndex.get("MEDIA_PLAY_LOCAL")).intValue()]).setNumber("MEDIA_PRIORITY", data[((Integer) hsmpDataFieldIndex.get("MEDIA_PRIORITY")).intValue()]);
                            } else {
                                String MEDIA_ID = ctr.get("MEDIA_ID");
                                SqlUpdateBuilder SqlUpdateBuilder = new SqlUpdateBuilder("RVM_MEDIA");
                                SqlUpdateBuilder.setNumber("MEDIA_TYPE", data[((Integer) hsmpDataFieldIndex.get("MEDIA_TYPE")).intValue()]).setNumber("DOWNLOAD_FLAG", Integer.valueOf(1)).setString("FILE_PATH", data[((Integer) hsmpDataFieldIndex.get("FILE_PATH")).intValue()]).setString("MEDIA_PLAY_LOCAL", data[((Integer) hsmpDataFieldIndex.get("MEDIA_PLAY_LOCAL")).intValue()]).setNumber("MEDIA_PRIORITY", data[((Integer) hsmpDataFieldIndex.get("MEDIA_PRIORITY")).intValue()]).setSqlWhere(new SqlWhereBuilder().addNumberEqualsTo("MEDIA_ID", MEDIA_ID));
                                listSql.add(SqlUpdateBuilder.toSql());
                            }
                        }
                        listSql.add(sqlInsertBuilder.toSql());
                        SqlInsertBuilder SqlInsertBuilderHandledFile = new SqlInsertBuilder("RVM_HANDLED_FILE");
                        for (i = 0; i < listFile.size(); i++) {
                           File file22 = (File) listFile.get(i);
                            filename = file22.getAbsolutePath();
                            filetime = file22.lastModified();
                            if (((String) hsmpFilename.get(filename)) != null) {
                                listSql.add("UPDATE RVM_HANDLED_FILE SET FILE_TIME='" + filetime + "' WHERE FILE_NAME='" + filename + "'");
                            } else {
                                SqlInsertBuilderHandledFile.newInsertRecord().setString("FILE_NAME", filename).setString("FILE_TIME", Long.valueOf(filetime));
                            }
                        }
                        listSql.add(SqlInsertBuilderHandledFile.toSql());
                        SQLiteExecutor.execSql(sqliteDatabase, (List) listSql);
                    }
                }
            }
        }
        return null;
    }

    public HashMap initDBSql(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String DB_INIT_PATH = SysConfig.get("RVM.INIT.PATH.DB");
        if (!StringUtils.isBlank(DB_INIT_PATH)) {
            File file = new File(DB_INIT_PATH);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    int i;
                    File file2;
                    String filename;
                    long filetime;
                    SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
                    CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_HANDLED_FILE");
                    HashMap<String, String> hsmpFilename = new HashMap();
                    for (i = 0; i < commTable.getRecordCount(); i++) {
                        CommTableRecord ctr = commTable.getRecord(i);
                        hsmpFilename.put(ctr.get("FILE_NAME"), ctr.get("FILE_TIME"));
                    }
                    List<File> listFile = new ArrayList();
                    for (File file22 : files) {
                        if (file22.isFile()) {
                            filename = file22.getAbsolutePath();
                            filetime = file22.lastModified();
                            String dbfiletime = (String) hsmpFilename.get(filename);
                            if (dbfiletime == null || Long.parseLong(dbfiletime) != filetime) {
                                InputStream fis = new FileInputStream(file22);
                                try {
                                    SQLiteExecutor.execSql(sqliteDatabase, new String(IOUtils.read(fis), "GBK"));
                                } catch (Exception e) {
                                } finally {
                                    IOUtils.close(fis);
                                }
                                listFile.add(file22);
                            }
                        }
                    }
                    List<String> listSql = new ArrayList();
                    SqlInsertBuilder SqlInsertBuilderHandledFile = new SqlInsertBuilder("RVM_HANDLED_FILE");
                    for (i = 0; i < listFile.size(); i++) {
                        File file22 = (File) listFile.get(i);
                        filename = file22.getAbsolutePath();
                        filetime = file22.lastModified();
                        if (((String) hsmpFilename.get(filename)) != null) {
                            listSql.add("UPDATE RVM_HANDLED_FILE SET FILE_TIME='" + filetime + "' WHERE FILE_NAME='" + filename + "'");
                        } else {
                            SqlInsertBuilderHandledFile.newInsertRecord().setString("FILE_NAME", filename).setString("FILE_TIME", Long.valueOf(filetime));
                        }
                    }
                    listSql.add(SqlInsertBuilderHandledFile.toSql());
                    SQLiteExecutor.execSql(sqliteDatabase, (List) listSql);
                }
            }
        }
        return null;
    }

    public HashMap initDBSeq(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        DBQuery dbQuery = DBQuery.getDBQuery(ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase());
        String[][] syncSql = new String[3][];
        syncSql[0] = new String[]{"OPT_ID", "max_opt_id", "select max(opt_id) as max_opt_id from rvm_opt"};
        syncSql[1] = new String[]{"ALARM_INST_ID", "max_alarm_inst_id", "select max(alarm_inst_id) as max_alarm_inst_id from rvm_alarm_inst"};
        syncSql[2] = new String[]{"USER_ID", "max_user_id", "select max(user_id) as max_user_id from rvm_staff where user_id<900000000"};
        List listSql = new ArrayList();
        for (int i = 0; i < syncSql.length; i++) {
            CommTable commTable = dbQuery.getCommTable(syncSql[i][2]);
            if (commTable.getRecordCount() > 0) {
                String max_val = commTable.getRecord(0).get(syncSql[i][1]);
                if (!StringUtils.isBlank(max_val)) {
                    SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder("T_SEQUENCE");
                    sqlUpdateBuilder.setExpression("SEQ_VALUE", "max(SEQ_VALUE," + max_val + ")").setSqlWhere(new SqlWhereBuilder().addStringEqualsTo("SEQ_NAME", syncSql[i][0]));
                    listSql.add(sqlUpdateBuilder.toSql());
                }
            }
        }
        if (listSql.size() > 0) {
            SQLiteExecutor.execSql(ServiceGlobal.getDatabaseHelper("SYS").getWritableDatabase(), listSql);
        }
        return null;
    }

    public HashMap initRVMDaemon(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String RVMDAEMON_PKG_RESOURCE = SysConfig.get("RVMDAEMON.PKG.RESOURCE");
        String RVMDAEMON_MAINACTIVITY = SysConfig.get("RVMDAEMON.MAINACTIVITY");
        String RVMDAEMON_VERSION_ID = SysConfig.get("RVMDAEMON.VERSION.ID");
        String RVMDAEMON_VERSION_ID_INSTALLED = SysConfig.get("RVMDAEMON.VERSION.ID.INSTALLED");
        String RVMDAEMON_VERSION_ID_INSTALLED_OLD = RVMDAEMON_VERSION_ID_INSTALLED;
        if (StringUtils.isBlank(RVMDAEMON_PKG_RESOURCE) || StringUtils.isBlank(RVMDAEMON_MAINACTIVITY) || StringUtils.isBlank(RVMDAEMON_VERSION_ID)) {
            return null;
        }
        int i;
        InputStream inputStream;
        String RVMDaemonPkg = SysConfig.get("APP.DIR") + "/" + RVMDAEMON_PKG_RESOURCE.substring(RVMDAEMON_PKG_RESOURCE.lastIndexOf("/") + 1);
        if (!ServiceGlobal.hasPackage(RVMDAEMON_MAINACTIVITY.substring(0, RVMDAEMON_MAINACTIVITY.indexOf("/")))) {
            RVMDAEMON_VERSION_ID_INSTALLED = "";
        }
        if (!RVMDAEMON_VERSION_ID.equals(RVMDAEMON_VERSION_ID_INSTALLED)) {
            boolean isOK = false;
            i = 0;
            while (i < 5) {
                OutputStream fos = null;
                inputStream = null;
                try {
                    inputStream = IOUtils.getResourceAsInputStream(RVMDAEMON_PKG_RESOURCE);
                    OutputStream fos2 = new FileOutputStream(RVMDaemonPkg);
                    try {
                        long lFileLen = IOUtils.dump(inputStream, fos2);
                        IOUtils.close(inputStream);
                        IOUtils.close(fos2);
                        File file = new File(RVMDaemonPkg);
                        if (file.isFile() && file.length() == lFileLen) {
                            isOK = true;
                            break;
                        }
                        Thread.sleep(30000);
                        i++;
                    } catch (Throwable th2) {
                        fos = fos2;
                    }
                } catch (Throwable th3) {
                    IOUtils.close(fos);
                    if (inputStream!= null) {
                        IOUtils.close(inputStream);
                    }
                }
            }
            if (!isOK) {
                return null;
            }
            ShellUtils.shell("pm install -r " + RVMDaemonPkg);
            if (!ServiceGlobal.hasPackage(RVMDAEMON_MAINACTIVITY.substring(0, RVMDAEMON_MAINACTIVITY.indexOf("/")))) {
                return null;
            }
        }
        if (!ShellUtils.isRunning(RVMDAEMON_MAINACTIVITY)) {
            ShellUtils.shell("am start " + RVMDAEMON_MAINACTIVITY);
        }
        boolean hasRunning = false;
        for (i = 0; i < 10; i++) {
            if (ShellUtils.isRunning(RVMDAEMON_MAINACTIVITY)) {
                hasRunning = true;
                break;
            }
            Thread.sleep(1000);
        }
        if (hasRunning && !RVMDAEMON_VERSION_ID.equals(RVMDAEMON_VERSION_ID_INSTALLED_OLD)) {
            Properties prop = new Properties();
            prop.setProperty("RVMDAEMON.VERSION.ID.INSTALLED", RVMDAEMON_VERSION_ID);
            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), prop);
            RVMShell.backupExternalConfig();
            SysConfig.set(prop);
        }

        return null;
    }

    public HashMap downloadBarcode(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        Properties prop;
        Exception e;
        Throwable th;
        if (!PropUtils.hasEncryptFlag(PropUtils.loadEncryptFile(new File(SysConfig.get("RVM.RESPARAM.FILE"))))) {
            PropUtils.transferEncryptFile(new File(SysConfig.get("RVM.RESPARAM.FILE")));
        }
        File file = new File(SysConfig.get("RVM.RESPARAM.FILE"));
        boolean updateResParamFile = false;
        if (!file.exists() || file.length() == 0) {
            updateResParamFile = true;
        } else {
            prop = PropUtils.loadEncryptFile(new File(SysConfig.get("RVM.RESPARAM.FILE")));
            if (StringUtils.isBlank(prop.getProperty("FTP_IP")) || StringUtils.isBlank(prop.getProperty("FTP_USER"))) {
                updateResParamFile = true;
            }
            String rvmDateParam = prop.getProperty("UPDATE_DATE");
            if (StringUtils.isBlank(rvmDateParam)) {
                updateResParamFile = true;
            } else {
                String resDateParam = PropUtils.loadResource(SysConfig.get("RVM.RESPARAM.RES")).getProperty("UPDATE_DATE");
                try {
                    if (!StringUtils.isBlank(resDateParam) && DateUtils.parseDate(resDateParam).after(DateUtils.parseDate(rvmDateParam))) {
                        updateResParamFile = true;
                    }
                } catch (Exception e2) {
                }
            }
        }
        if (updateResParamFile) {
            prop = PropUtils.loadResource(SysConfig.get("RVM.RESPARAM.RES"));
            PropUtils.saveEncryptFile(file, prop);
        } else {
            prop = PropUtils.loadEncryptFile(new File(SysConfig.get("RVM.RESPARAM.FILE")));
        }
        String FTP_IP = prop.getProperty("FTP_IP");
        String FTP_CMD_PORT = prop.getProperty("FTP_CMD_PORT");
        String FTP_DATA_PORT = prop.getProperty("FTP_DATA_PORT");
        String FTP_USER = prop.getProperty("FTP_USER");
        String FTP_PASSWORD = prop.getProperty("FTP_PASSWORD");
        String FTP_BARCODE_PATH = prop.getProperty("FTP_BARCODE_PATH");
        String FTP_BARCODE_FILE_PREFIX = prop.getProperty("FTP_BARCODE_FILE_PREFIX");
        if (StringUtils.isBlank(FTP_CMD_PORT)) {
            FTP_CMD_PORT = "21";
        }
        if (StringUtils.isBlank(FTP_DATA_PORT)) {
            FTP_DATA_PORT = "20";
        }
        if (StringUtils.isBlank(FTP_IP) || StringUtils.isBlank(FTP_USER) || StringUtils.isBlank(FTP_PASSWORD) || StringUtils.isBlank(FTP_BARCODE_PATH) || StringUtils.isBlank(FTP_BARCODE_FILE_PREFIX)) {
            return null;
        }
        int i;
        int t;
        HashMap hsmp;
        Properties propExternalConfig;
        String LAST_FTP_BARCODEFILE = SysConfig.get("LAST.FTP.BARCODEFILE");
        String NEW_LAST_FTP_BARCODEFILE = null;
        String RVM_TMP_PATH = SysConfig.get("RVM.TMP.PATH");
        String RVM_INIT_PATH_BARCODE = SysConfig.get("RVM.INIT.PATH.BARCODE");
        file = new File(RVM_TMP_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(RVM_INIT_PATH_BARCODE);
        if (!file.exists()) {
            file.mkdirs();
        }
        File[] fileList = file.listFiles();
        if (fileList != null) {
            File newestFile = null;
            int iDate = 0;
            for (i = 0; i < fileList.length; i++) {
                if (fileList[i].getName().startsWith(FTP_BARCODE_FILE_PREFIX)) {
                    t = Integer.parseInt(DateUtils.formatDatetime(new Date(fileList[i].lastModified()), "yyyyMMdd"));
                    if (t > iDate) {
                        iDate = t;
                        if (newestFile != null) {
                            newestFile.delete();
                        }
                        newestFile = fileList[i];
                    } else {
                        fileList[i].delete();
                    }
                }
            }
        }
        boolean barcodeUpdated = false;
        FTPFile lastFTPFile = null;
        int lastFtpFileTime = 0;
        File file2 = null;
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_IP, Integer.parseInt(FTP_CMD_PORT));
            ftpClient.login(FTP_USER, FTP_PASSWORD);
            if (ftpClient.isAuthenticated()) {
                ftpClient.changeDirectory(FTP_BARCODE_PATH);
                FTPFile[] ftpFiles = ftpClient.list();
                if (ftpFiles != null && ftpFiles.length >= 1) {
                    for (i = 0; i < ftpFiles.length; i++) {
                        if (ftpFiles[i].getName().startsWith(FTP_BARCODE_FILE_PREFIX)) {
                            t = Integer.parseInt(DateUtils.formatDatetime(ftpFiles[i].getModifiedDate(), "yyyyMMdd"));
                            if (t > lastFtpFileTime) {
                                lastFtpFileTime = t;
                                lastFTPFile = ftpFiles[i];
                            }
                        }
                    }
                }
                if (lastFTPFile != null) {
                    NEW_LAST_FTP_BARCODEFILE = lastFTPFile.getName() + ";" + lastFtpFileTime;
                    boolean needDownload = true;
                    if (!StringUtils.isBlank(LAST_FTP_BARCODEFILE) && NEW_LAST_FTP_BARCODEFILE.equals(LAST_FTP_BARCODEFILE)) {
                        needDownload = false;
                    }
                    if (needDownload) {
                        file = new File(file, lastFTPFile.getName());
                        try {
                            ftpClient.download(lastFTPFile.getName(), file);
                            barcodeUpdated = true;
                            long FTPFileSize = lastFTPFile.getSize();
                            TrafficEntity.addData(TrafficType.BARCODE, new Long(FTPFileSize).intValue());
                            logger.debug("Download barcode file:" + lastFTPFile.getName() + ", size:" + FTPFileSize);
                            file2 = file;
                        } catch (Exception e3) {
                            e = e3;
                            file2 = file;
                            try {
                                e.printStackTrace();
                                try {
                                    ftpClient.disconnect(true);
                                } catch (Exception e4) {
                                }
                                hsmp = new HashMap();
                                hsmp.put("UPDATE_DETECTION", updateDetection.FTP);
                                CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "updateDetection", hsmp);
                                if (barcodeUpdated) {
                                    return null;
                                }
                                if (lastFTPFile.getName().endsWith(".zip")) {
                                    ZipUtils.upZipFile(file2, RVM_INIT_PATH_BARCODE);
                                } else {
                                    ShellUtils.shell("mv " + file2.getAbsolutePath() + " " + RVM_INIT_PATH_BARCODE);
                                }
                                propExternalConfig = new Properties();
                                propExternalConfig.setProperty("LAST.FTP.BARCODEFILE", NEW_LAST_FTP_BARCODEFILE);
                                PropUtils.update(SysConfig.get("EXTERNAL.FILE"), propExternalConfig);
                                RVMShell.backupExternalConfig();
                                SysConfig.set(propExternalConfig);
                                return initBarcode(null, null, null);
                            } catch (Throwable th2) {
                                try {
                                    ftpClient.disconnect(true);
                                } catch (Exception e5) {
                                }
                            }
                        } catch (Throwable th3) {
                            file2 = file;
                            ftpClient.disconnect(true);
                            throw th3;
                        }
                    }
                }
                ftpClient.logout();
            }
            try {
                ftpClient.disconnect(true);
            } catch (Exception e6) {
            }
        } catch (Exception e7) {
            e = e7;
            e.printStackTrace();
            ftpClient.disconnect(true);
            hsmp = new HashMap();
            hsmp.put("UPDATE_DETECTION", updateDetection.FTP);
            CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "updateDetection", hsmp);
            if (barcodeUpdated) {
                return null;
            }
            if (lastFTPFile.getName().endsWith(".zip")) {
                ShellUtils.shell("mv " + file2.getAbsolutePath() + " " + RVM_INIT_PATH_BARCODE);
            } else {
                ZipUtils.upZipFile(file2, RVM_INIT_PATH_BARCODE);
            }
            propExternalConfig = new Properties();
            propExternalConfig.setProperty("LAST.FTP.BARCODEFILE", NEW_LAST_FTP_BARCODEFILE);
            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), propExternalConfig);
            RVMShell.backupExternalConfig();
            SysConfig.set(propExternalConfig);
            return initBarcode(null, null, null);
        }
        hsmp = new HashMap();
        hsmp.put("UPDATE_DETECTION", updateDetection.FTP);
        CommonServiceHelper.getGUICommonService().execute("GUIMaintenanceCommonService", "updateDetection", hsmp);
        if (barcodeUpdated) {
            return null;
        }
        if (!(file2 == null || lastFTPFile == null || !file2.exists())) {
            if (lastFTPFile.getName().endsWith(".zip")) {
                ZipUtils.upZipFile(file2, RVM_INIT_PATH_BARCODE);
            } else {
                ShellUtils.shell("mv " + file2.getAbsolutePath() + " " + RVM_INIT_PATH_BARCODE);
            }
            propExternalConfig = new Properties();
            propExternalConfig.setProperty("LAST.FTP.BARCODEFILE", NEW_LAST_FTP_BARCODEFILE);
            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), propExternalConfig);
            RVMShell.backupExternalConfig();
            SysConfig.set(propExternalConfig);
        }
        return initBarcode(null, null, null);
    }

    public HashMap rvmCodeRestore(String svcName, String subSvnName, HashMap hsmpParam) throws Exception {
        String IS_CLEAR = null;
        if (hsmpParam != null) {
            IS_CLEAR = (String) hsmpParam.get("IS_CLEAR");
        }
        if ("TRUE".equalsIgnoreCase(IS_CLEAR)) {
            Properties externalProp = new Properties();
            externalProp.put("UPDATE_MAC_ADDR", "");
            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
            SysConfig.set(externalProp);
        }
        if (!"TRUE".equalsIgnoreCase(SysConfig.get("RVM.CODE.BACKUP.REMOTE"))) {
            return null;
        }
        int i;
        HashMap hsmpretPkg;
        int u;
        SqlWhereBuilder sqlWhereBuilder;
        int a;
        List<String> listMacAddr = NetworkUtils.getMacAddr();
        List<String> listUpdateMacAddr = new ArrayList();
        String UPDATE_MAC_ADDR = SysConfig.get("UPDATE_MAC_ADDR");
        if (!StringUtils.isBlank(UPDATE_MAC_ADDR)) {
            String[] UPDATE_MAC_ADDRS = UPDATE_MAC_ADDR.split(";");
            for (String add : UPDATE_MAC_ADDRS) {
                listUpdateMacAddr.add(add);
            }
        }
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        DBQuery dbQuery = DBQuery.getDBQuery(sqliteDatabase);
        DBSequence dbSequence = DBSequence.getInstance(ServiceGlobal.getDatabaseHelper("SYS"));
        boolean updateConfig = false;
        String RVM_CODE = SysConfig.get("RVM.CODE");
        String LOCAL_AREA_ID = SysConfig.get("RVM.AREA.CODE");
        if ("0".equals(RVM_CODE)) {
            for (i = 0; i < listMacAddr.size(); i++) {
                Long time = Long.valueOf(new Date().getTime());
                String opBatchID = SysConfig.get("RVM.CODE") + "_" + time;
                HashMap hsmpMsg = new HashMap();
                hsmpMsg.put("MES_TYPE", MsgType.RVM_CONFIG_ASK);
                hsmpMsg.put("MAC", listMacAddr.get(i));
                hsmpMsg.put("TERM_NO", RVM_CODE);
                hsmpMsg.put("LOCAL_AREA_ID", LOCAL_AREA_ID);
                hsmpMsg.put("QU_TIME", time.toString());
                hsmpMsg.put("OP_BATCH_ID", opBatchID);
                hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpMsg)));
                if (hsmpretPkg != null) {
                    String NEW_RVM_CODE = (String) hsmpretPkg.get("TERM_NO");
                    if (StringUtils.isBlank(NEW_RVM_CODE)) {
                        continue;
                    } else {
                        RVM_CODE = NEW_RVM_CODE;
                        if (!(StringUtils.isBlank(RVM_CODE) || "0".equals(RVM_CODE))) {
                            SqlInsertBuilder sqlInsertBuilder;
                            List<SqlBuilder> listSqlBuilder = new ArrayList();
                            LOCAL_AREA_ID = (String) hsmpretPkg.get("LOCAL_AREA_ID");
                            Properties externalProp = new Properties();
                            String POWER_ON_TIME = (String) hsmpretPkg.get("P_O_T");
                            String POWER_OFF_TIME = (String) hsmpretPkg.get("P_F_T");
                            if (!(StringUtils.isBlank(POWER_ON_TIME) || StringUtils.isBlank(POWER_OFF_TIME))) {
                                externalProp.put("RVM.POWER.ON.TIME", POWER_ON_TIME);
                                externalProp.put("RVM.POWER.OFF.TIME", POWER_OFF_TIME);
                            }
                            String VENDING_WAY = (String) hsmpretPkg.get("V_W");
                            if (!StringUtils.isBlank(VENDING_WAY)) {
                                externalProp.put("VENDING.WAY", VENDING_WAY);
                            }
                            String DOOR_STS = (String) hsmpretPkg.get("DOOR_STS");
                            if (!StringUtils.isBlank(DOOR_STS)) {
                                externalProp.put("COM.PLC.HAS.DOOR", DOOR_STS);
                            }
                            String STAFF_USER_LIST = (String) hsmpretPkg.get("U_S_I");
                            if (!StringUtils.isBlank(STAFF_USER_LIST)) {
                                String[] STAFF_USER_SET = STAFF_USER_LIST.split(";");
                                for (String add2 : STAFF_USER_SET) {
                                    String[] STAFF_USER = add2.split(",");
                                    if (STAFF_USER.length >= 2) {
                                        sqlWhereBuilder = new SqlWhereBuilder();
                                        sqlWhereBuilder.addStringEqualsTo("USER_EXT_CODE", STAFF_USER[1]);
                                        if (dbQuery.getCommTable("select * from RVM_STAFF " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
                                            String userId = dbSequence.getSeq("USER_ID");
                                            sqlInsertBuilder = new SqlInsertBuilder("RVM_STAFF");
                                            sqlInsertBuilder.newInsertRecord().setNumber("USER_ID", userId).setString("USER_EXT_CODE", STAFF_USER[1]).setString("USER_LOGIN_NAME", null).setNumber("USER_STAFF_ID", STAFF_USER[0]).setNumber("USER_STATUS", Integer.valueOf(1)).setNumber("USER_PERMISSION", STAFF_USER[2]);
                                            listSqlBuilder.add(sqlInsertBuilder);
                                        }
                                    }
                                }
                            }
                            List<String[]> listAlarm = new ArrayList();
                            if (!StringUtils.isBlank((String) hsmpretPkg.get("S_B_T"))) {
                                listAlarm.add(new String[]{Integer.toString(12), (String) hsmpretPkg.get("S_B_T")});
                            }
                            if (!StringUtils.isBlank((String) hsmpretPkg.get("S_B_M"))) {
                                listAlarm.add(new String[]{Integer.toString(11), (String) hsmpretPkg.get("S_B_M")});
                            }
                            if (!StringUtils.isBlank((String) hsmpretPkg.get("S_P_T"))) {
                                listAlarm.add(new String[]{Integer.toString(14), (String) hsmpretPkg.get("S_P_T")});
                            }
                            if (!StringUtils.isBlank((String) hsmpretPkg.get("S_P_M"))) {
                                listAlarm.add(new String[]{Integer.toString(13), (String) hsmpretPkg.get("S_P_M")});
                            }
                            if (listAlarm.size() > 0) {
                                for (a = 0; a < listAlarm.size(); a++) {
                                    String[] ALARM_PARAM = (String[]) listAlarm.get(a);
                                    sqlWhereBuilder = new SqlWhereBuilder();
                                    sqlWhereBuilder.addNumberEqualsTo("ALARM_ID", ALARM_PARAM[0]);
                                    if (dbQuery.getCommTable("select * from RVM_ALARM " + sqlWhereBuilder.toSqlWhere("where")).getRecordCount() == 0) {
                                        sqlInsertBuilder = new SqlInsertBuilder(MsgType.RVM_ALARM);
                                        sqlInsertBuilder.newInsertRecord().setNumber("ALARM_ID", ALARM_PARAM[0]).setNumber("TSD_TYPE", "1").setNumber("TSD_VALUE", ALARM_PARAM[1]).setNumber("AM_LEVEL", "1");
                                        listSqlBuilder.add(sqlInsertBuilder);
                                    } else {
                                        SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder(MsgType.RVM_ALARM);
                                        sqlUpdateBuilder.setNumber("TSD_TYPE", "1").setNumber("TSD_VALUE", ALARM_PARAM[1]).setNumber("AM_LEVEL", "1").setSqlWhere(sqlWhereBuilder);
                                        listSqlBuilder.add(sqlUpdateBuilder);
                                    }
                                }
                            }
                            String IS_PLAY_SOUNDS = (String) hsmpretPkg.get("I_P_S");
                            if (!StringUtils.isBlank(IS_PLAY_SOUNDS)) {
                                externalProp.put("IS_PLAY_SOUNDS", IS_PLAY_SOUNDS);
                            }
                            String SERVICE_CFG_DISABLE = (String) hsmpretPkg.get("D_S_S");
                            if (!StringUtils.isBlank(SERVICE_CFG_DISABLE)) {
                                externalProp.put("SERVICE_CFG_DISABLE", SERVICE_CFG_DISABLE);
                            }
                            if (listSqlBuilder.size() > 0) {
                                SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
                            }
                            listUpdateMacAddr.clear();
                            listUpdateMacAddr.add(listMacAddr.get(i));
                            externalProp.put("RVM.CODE", RVM_CODE);
                            externalProp.put("RVM.AREA.CODE", LOCAL_AREA_ID);
                            externalProp.put("UPDATE_MAC_ADDR", listMacAddr.get(i));
                            PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                            SysConfig.set(externalProp);
                            RVMShell.backupExternalConfig();
                            RVMShell.backupKeySettings(SysConfig.get("KEY_SETTINGS_FILE"), SysConfig.get("KEY_SETTINGS_ATTR"), PropUtils.loadFile(SysConfig.get("EXTERNAL.FILE")));
                            updateConfig = true;
                        }
                    }
                }
            }
        }
        if (!"0".equals(RVM_CODE)) {
            StringBuffer sb;
            HashMap hsmpMsg = null;
            List<String> listNewUpdateMacAddr = new ArrayList();
            for (i = 0; i < listMacAddr.size(); i++) {
                listNewUpdateMacAddr.add(listMacAddr.get(i));
                if (!listUpdateMacAddr.contains(listMacAddr.get(i))) {
                    CommTable commTable;
                    CommTableRecord ctr;
                    Long time = Long.valueOf(new Date().getTime());
                    String opBatchID = SysConfig.get("RVM.CODE") + "_" + time;
                    if (hsmpMsg == null) {
                        hsmpMsg = new HashMap();
                        hsmpMsg.put("MES_TYPE", MsgType.RVM_CONFIG_INFO);
                        hsmpMsg.put("TERM_NO", RVM_CODE);
                        hsmpMsg.put("LOCAL_AREA_ID", LOCAL_AREA_ID);
                        hsmpMsg.put("P_O_T", SysConfig.get("RVM.POWER.ON.TIME"));
                        hsmpMsg.put("P_F_T", SysConfig.get("RVM.POWER.OFF.TIME"));
                        hsmpMsg.put("R_V_I", SysConfig.get("RVM.VERSION.ID"));
                        try {
                            String vendingWaySet = SysConfig.get("VENDING.WAY");
                            List vendingWaySetList = null;
                            if (!StringUtils.isBlank(vendingWaySet)) {
                                vendingWaySetList = Arrays.asList(vendingWaySet.split(";"));
                            }
                            List<String> disenableServiceList = new ArrayList();
                            SqlWhereBuilder sqlWhereBuilderRvmSysCode = new SqlWhereBuilder();
                            sqlWhereBuilderRvmSysCode.addStringEqualsTo("SYS_CODE_TYPE", "RVM_INFO").addStringEqualsTo("SYS_CODE_KEY", "SERVICE_DISABLED_SET");
                            CommTable commTableSysCode = dbQuery.getCommTable("select * from RVM_SYS_CODE" + sqlWhereBuilderRvmSysCode.toSqlWhere("where"));
                            String SYS_CODE_VALUE = "";
                            if (commTableSysCode.getRecordCount() > 0) {
                                SYS_CODE_VALUE = commTableSysCode.getRecord(0).get("SYS_CODE_VALUE");
                            }
                            if (!StringUtils.isBlank(SYS_CODE_VALUE)) {
                                String[] service_set = SYS_CODE_VALUE.split(",");
                                for (int j = 0; j < service_set.length; j++) {
                                    if (service_set[j].equalsIgnoreCase("CARD")) {
                                        disenableServiceList.add(maintainOptContent.STOP_CARD);
                                        vendingWaySetList.remove("CARD");
                                    }
                                    if (service_set[j].equalsIgnoreCase("COUPON")) {
                                        disenableServiceList.add(maintainOptContent.STOP_COUPON);
                                        vendingWaySetList.remove("COUPON");
                                    }
                                    if (service_set[j].equalsIgnoreCase("DONATION")) {
                                        disenableServiceList.add(maintainOptContent.STOP_DONATION);
                                        vendingWaySetList.remove("DONATION");
                                    }
                                    if (service_set[j].equalsIgnoreCase("PHONE")) {
                                        disenableServiceList.add(maintainOptContent.STOP_PHONE);
                                        vendingWaySetList.remove("PHONE");
                                    }
                                    if (service_set[j].equalsIgnoreCase("QRCODE")) {
                                        disenableServiceList.add(maintainOptContent.STOP_LNKCARD);
                                        vendingWaySetList.remove("QRCODE");
                                    }
                                    if (service_set[j].equalsIgnoreCase("TRANSPORTCARD")) {
                                        disenableServiceList.add(maintainOptContent.STOP_ONECARD);
                                        vendingWaySetList.remove("TRANSPORTCARD");
                                    }
                                    if (service_set[j].equalsIgnoreCase("BOTTLE")) {
                                        disenableServiceList.add(maintainOptContent.STOP_RECYCLE_BOTTLE);
                                    }
                                    if (service_set[j].equalsIgnoreCase("PAPER")) {
                                        disenableServiceList.add(maintainOptContent.STOP_RECYCLE_PAPER);
                                    }
                                    if (service_set[j].equalsIgnoreCase("CONSERVICE")) {
                                        disenableServiceList.add(maintainOptContent.STOP_CONVENIENCE);
                                    }
                                    if (service_set[j].equalsIgnoreCase("WECHAT")) {
                                        disenableServiceList.add(maintainOptContent.STOP_WECHAT);
                                        vendingWaySetList.remove("WECHAT");
                                    }
                                    if (service_set[j].equalsIgnoreCase("BDJ")) {
                                        disenableServiceList.add(maintainOptContent.STOP_BDJ);
                                        vendingWaySetList.remove("BDJ");
                                    }
                                }
                            }
                            hsmpMsg.put("D_S_S", disenableServiceList);
                            hsmpMsg.put("V_W", vendingWaySetList);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        hsmpMsg.put("I_P_S", SysConfig.get("IS_PLAY_SOUNDS"));
                        hsmpMsg.put("OTHER", SysConfig.get(""));
                        hsmpMsg.put("RVM_TYPE", SysConfig.get("RVM.MODE"));
                        hsmpMsg.put("HARDWARE_ID", SysConfig.get("HARDWARE.VERSION"));
                        hsmpMsg.put("DOOR_STS", SysConfig.get("COM.PLC.HAS.DOOR"));
                        sqlWhereBuilder = new SqlWhereBuilder();
                        sqlWhereBuilder.addNumberEqualsTo("USER_STATUS", Integer.valueOf(1));
                        commTable = dbQuery.getCommTable("select * from RVM_STAFF " + sqlWhereBuilder.toSqlWhere("where"));
                        sb = new StringBuffer();
                        if (commTable.getRecordCount() > 0) {
                            for (u = 0; u < commTable.getRecordCount(); u++) {
                                ctr = commTable.getRecord(u);
                                if (!StringUtils.isBlank(ctr.get("USER_EXT_CODE"))) {
                                    sb.append(ctr.get("USER_STAFF_ID"));
                                    sb.append(",");
                                    sb.append(ctr.get("USER_EXT_CODE"));
                                    sb.append(",");
                                    sb.append(ctr.get("USER_PERMISSION"));
                                    sb.append(";");
                                }
                            }
                        }
                        hsmpMsg.put("U_S_I", sb.toString());
                    }
                    commTable = dbQuery.getCommTable("select * from RVM_ALARM ");
                    for (a = 0; a < commTable.getRecordCount(); a++) {
                        ctr = commTable.getRecord(a);
                        String ALARM_ID = ctr.get("ALARM_ID");
                        String TSD_VALUE = ctr.get("TSD_VALUE");
                        if (Integer.toString(12).equals(ALARM_ID)) {
                            hsmpMsg.put("S_B_T", TSD_VALUE);
                        }
                        if (Integer.toString(11).equals(ALARM_ID)) {
                            hsmpMsg.put("S_B_M", TSD_VALUE);
                        }
                        if (Integer.toString(14).equals(ALARM_ID)) {
                            hsmpMsg.put("S_P_T", TSD_VALUE);
                        }
                        if (Integer.toString(13).equals(ALARM_ID)) {
                            hsmpMsg.put("S_P_M", TSD_VALUE);
                        }
                    }
                    hsmpMsg.put("QU_TIME", time.toString());
                    hsmpMsg.put("OP_BATCH_ID", opBatchID);
                    hsmpMsg.put("MAC", listMacAddr.get(i));
                    hsmpretPkg = JSONUtils.toHashMap(CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpMsg)));
                    if (hsmpretPkg == null) {
                        return null;
                    }
                    if (RVM_CODE.equals((String) hsmpretPkg.get("TERM_NO"))) {
                        updateConfig = true;
                    }
                }
            }
            if (updateConfig) {
                sb = new StringBuffer();
                for (i = 0; i < listNewUpdateMacAddr.size(); i++) {
                    sb.append((String) listNewUpdateMacAddr.get(i));
                    sb.append(";");
                }
                String NEW_UPDATE_MAC_ADDR = sb.toString();
                Properties externalProp = new Properties();
                externalProp.put("UPDATE_MAC_ADDR", NEW_UPDATE_MAC_ADDR);
                PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
                SysConfig.set(externalProp);
                RVMShell.backupExternalConfig();
            }
        }
        return null;
    }

    public void initAlarmSetting(String svcName, String subSvnName, HashMap hsmpParam) {
        SQLiteDatabase sqliteDatabase = ServiceGlobal.getDatabaseHelper("RVM").getWritableDatabase();
        SqlWhereBuilder sqlWhereBuilder = new SqlWhereBuilder();
        sqlWhereBuilder.addNumberIn("ALARM_ID", Integer.valueOf(11), Integer.valueOf(12));
        CommTable commTable = DBQuery.getDBQuery(sqliteDatabase).getCommTable("select * from RVM_ALARM " + sqlWhereBuilder.toSqlWhere("where"));
        String tsdValueMax = "0";
        String tsdValueAlarm = "0";
        boolean hasMaxRecord = false;
        boolean hasAlarmRecord = false;
        if (commTable.getRecordCount() != 0) {
            for (int i = 0; i < commTable.getRecordCount(); i++) {
                if (commTable.getRecord(i).get("ALARM_ID").equals("11")) {
                    tsdValueMax = commTable.getRecord(0).get("TSD_VALUE");
                    hasMaxRecord = true;
                }
                if (commTable.getRecord(i).get("ALARM_ID").equals("12")) {
                    tsdValueAlarm = commTable.getRecord(0).get("TSD_VALUE");
                    hasAlarmRecord = true;
                }
            }
        }
        List<SqlBuilder> listSqlBuilder = new ArrayList();
        if (StringUtils.isBlank(tsdValueMax) || "0".equalsIgnoreCase(tsdValueMax)) {
            tsdValueMax = SysConfig.get("MAX.VALUE");
            if (hasMaxRecord) {
                SqlWhereBuilder sqlWhereBuilder1 = new SqlWhereBuilder();
                sqlWhereBuilder.addNumberEqualsTo("ALARM_ID", Integer.valueOf(11));
                SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder(MsgType.RVM_ALARM);
                sqlUpdateBuilder.setNumber("TSD_VALUE", tsdValueMax).setSqlWhere(sqlWhereBuilder1);
                listSqlBuilder.add(sqlUpdateBuilder);
            } else {
                SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder(MsgType.RVM_ALARM);
                sqlInsertBuilder.newInsertRecord().setNumber("ALARM_ID", Integer.valueOf(11)).setNumber("TSD_TYPE", Integer.valueOf(1)).setNumber("TSD_VALUE", tsdValueMax).setNumber("AM_LEVEL", Integer.valueOf(3));
                listSqlBuilder.add(sqlInsertBuilder);
            }
        }
        if (StringUtils.isBlank(tsdValueAlarm) || "0".equalsIgnoreCase(tsdValueAlarm)) {
            tsdValueAlarm = SysConfig.get("ALARM.VALUE");
            if (hasAlarmRecord) {
                SqlWhereBuilder sqlWhereBuilder1 = new SqlWhereBuilder();
                sqlWhereBuilder1.addNumberEqualsTo("ALARM_ID", Integer.valueOf(12));
                SqlUpdateBuilder sqlUpdateBuilder = new SqlUpdateBuilder(MsgType.RVM_ALARM);
                sqlUpdateBuilder.setNumber("TSD_VALUE", tsdValueAlarm).setSqlWhere(sqlWhereBuilder1);
                listSqlBuilder.add(sqlUpdateBuilder);
            } else {
                SqlInsertBuilder sqlInsertBuilder = new SqlInsertBuilder(MsgType.RVM_ALARM);
                sqlInsertBuilder.newInsertRecord().setNumber("ALARM_ID", Integer.valueOf(12)).setNumber("TSD_TYPE", Integer.valueOf(1)).setNumber("TSD_VALUE", tsdValueAlarm).setNumber("AM_LEVEL", Integer.valueOf(3));
                listSqlBuilder.add(sqlInsertBuilder);
            }
        }
        SQLiteExecutor.execSqlBuilder(sqliteDatabase, listSqlBuilder);
    }

    public void initRVMStatus(String svcName, String subSvnName, HashMap hsmpParam) {
        if ("TRUE".equalsIgnoreCase(SysConfig.get("START_USING"))) {
            String QuTime = SysConfig.get("START_USING_TIME");
            if (StringUtils.isBlank(QuTime)) {
                QuTime = String.valueOf(Long.valueOf(new Date().getTime()));
            }
            HashMap hsmpPkg = new HashMap();
            hsmpPkg.put("MES_TYPE", MsgType.RVM_STS_UPDATE);
            hsmpPkg.put("TERM_NO", SysConfig.get("RVM.CODE"));
            hsmpPkg.put("LOCAL_AREA_ID", SysConfig.get("RVM.AREA.CODE"));
            hsmpPkg.put("QU_TIME", QuTime);
            hsmpPkg.put("OP_BATCH_ID", SysConfig.get("RVM.CODE") + "_" + QuTime);
            hsmpPkg.put("RVM_STATUS", "1");
            try {
                CommService.getCommService().execute("RCC_SEND", JSONUtils.toJSON(hsmpPkg));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
