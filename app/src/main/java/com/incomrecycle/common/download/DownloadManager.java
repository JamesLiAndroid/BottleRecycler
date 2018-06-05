package com.incomrecycle.common.download;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.os.StrictMode.VmPolicy;
import android.util.Log;
import com.google.code.microlog4android.format.SimpleFormatter;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.queue.FIFOQueue;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.ShellUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameDataFormat.ServerFrameType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DownloadManager {
    private static final int FILE_STATUS_DONE = 3;
    private static final int FILE_STATUS_DOWNLOADING = 1;
    private static final int FILE_STATUS_ERROR = 4;
    private static final int FILE_STATUS_FINISH = 2;
    private static final int FILE_STATUS_INIT = 0;
    private static final DownloadManager dm = new DownloadManager();
    private DownloadCallback downloadCallback;
    private String downloadPath;
    private int errorInterval = 600;
    private FIFOQueue fifoQueue = new FIFOQueue();
    private List<DownloadFile> listDownloadFile = new ArrayList();
    private String propFile;

    public static class DownloadFile {
        String downloadTime;
        int fileDownloadLen;
        long fileId;
        String filePath;
        int fileSize;
        int fileStatus;
        String fileURL;
        String memo;
        String type;

        boolean isDownloaded() {
            return this.fileStatus == 2 || this.fileStatus == 3 || this.fileStatus == 4;
        }
    }

    public static class DownloadThread extends Thread {
        public void run() {
            StrictMode.setThreadPolicy(new Builder().detectDiskReads().detectDiskWrites().detectNetwork().detectAll().penaltyLog().permitAll().build());
            StrictMode.setVmPolicy(new VmPolicy.Builder().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());
            while (true) {
                DownloadFile downloadFile = (DownloadFile) DownloadManager.dm.fifoQueue.pop();
                if (downloadFile == null) {
                    return;
                }
                if (DownloadManager.dm.isExists(downloadFile)) {
                    download(downloadFile);
                    DownloadManager.dm.checkDone();
                    try {
                        Thread.sleep(5000);
                        if (!downloadFile.isDownloaded()) {
                            SysGlobal.execute(new Thread() {
                                DownloadFile downloadFile;

                                public Thread setDownloadFile(DownloadFile downloadFile) {
                                    this.downloadFile = downloadFile;
                                    return this;
                                }

                                public void run() {
                                    try {
                                        Thread.sleep((long) (DownloadManager.dm.errorInterval * ServerFrameType.DATA_TRANSFER_REQ));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (!this.downloadFile.isDownloaded()) {
                                        DownloadManager.dm.fifoQueue.push(this.downloadFile);
                                    }
                                }
                            }.setDownloadFile(downloadFile));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void download(DownloadFile downloadFile) {
            if (downloadFile.fileStatus != 2 && downloadFile.fileStatus != 3) {
                HttpURLConnection httpConnection = null;
                InputStream input = null;
                try {
                    if (downloadFile.fileStatus == 0) {
                        new File(DownloadManager.getTmpFilename(downloadFile)).getParentFile().mkdirs();
                    }
                    httpConnection = (HttpURLConnection) new URL(downloadFile.fileURL).openConnection();
                    httpConnection.setConnectTimeout(ServerFrameType.LOGIN_CONFIRM);
                    httpConnection.setReadTimeout(10000);
                    httpConnection.setRequestProperty("User-Agent", "AndroidWebKit");
                    if (downloadFile.fileDownloadLen > 0) {
                        httpConnection.setRequestProperty("RANGE", "bytes=" + downloadFile.fileDownloadLen + SimpleFormatter.DEFAULT_DELIMITER);
                    }
                    if (httpConnection.getResponseCode() >= 400) {
                        Date downloadDate = DateUtils.parseDate(downloadFile.downloadTime);
                        if (System.currentTimeMillis() > downloadDate.getTime() && System.currentTimeMillis() - downloadDate.getTime() > 172800000) {
                            synchronized (downloadFile) {
                                downloadFile.fileStatus = 4;
                            }
                            DownloadManager.dm.saveFile();
                        }
                        if (input != null) {
                            try {
                                input.close();
                            } catch (Exception e) {
                            }
                        }
                        if (httpConnection != null) {
                            try {
                                httpConnection.disconnect();
                            } catch (Exception e2) {
                                return;
                            }
                        }
                        return;
                    }
                    downloadFile.downloadTime = DateUtils.formatDatetime(new Date(), "yyyyMMdd");
                    int nFileLength = -1;
                    boolean isRange = false;
                    String ContentLength = httpConnection.getHeaderField("Content-Length");
                    String ContentRange = httpConnection.getHeaderField("Content-Range");
                    String AcceptRange = httpConnection.getHeaderField("Accept-Ranges");
                    if (!StringUtils.isBlank(ContentLength)) {
                        nFileLength = Integer.parseInt(ContentLength);
                    }
                    if (!(StringUtils.isBlank(ContentRange) && StringUtils.isBlank(AcceptRange))) {
                        isRange = true;
                    }
                    input = httpConnection.getInputStream();
                    synchronized (downloadFile) {
                        downloadFile.fileStatus = 1;
                        if (downloadFile.fileDownloadLen == 0) {
                            downloadFile.fileSize = nFileLength;
                        }
                    }
                    byte[] b = new byte[1024];
                    RandomAccessFile randomAccessFile = new RandomAccessFile(DownloadManager.getTmpFilename(downloadFile), "rw");
                    if (!isRange) {
                        downloadFile.fileDownloadLen = 0;
                    }
                    randomAccessFile.seek((long) downloadFile.fileDownloadLen);
                    while (true) {
                        int nRead = input.read(b, 0, 1024);
                        if (nRead <= 0) {
                            break;
                        }
                        if (downloadFile.fileSize != -1 && downloadFile.fileSize < downloadFile.fileDownloadLen + nRead) {
                            nRead = downloadFile.fileSize - downloadFile.fileDownloadLen;
                        }
                        randomAccessFile.write(b, 0, nRead);
                        synchronized (downloadFile) {
                            downloadFile.fileDownloadLen += nRead;
                            if (downloadFile.fileDownloadLen == downloadFile.fileSize) {
                                downloadFile.fileStatus = 2;
                            }
                        }
                        try {
                            DownloadManager.dm.saveFile();
                            if (downloadFile.fileDownloadLen == downloadFile.fileSize) {
                                break;
                            }
                        } catch (Exception e3) {
                            try {
                                Log.d("DOWNLOAD", "Terminate to download file", e3);
                                if (downloadFile.fileSize == -1) {
                                    downloadFile.fileDownloadLen = 0;
                                    DownloadManager.dm.saveFile();
                                }
                            } finally {
                                randomAccessFile.close();
                            }
                        }
                    }
                    if (downloadFile.fileSize == -1 && downloadFile.fileDownloadLen > 0) {
                        downloadFile.fileSize = downloadFile.fileDownloadLen;
                        downloadFile.fileStatus = 2;
                        DownloadManager.dm.saveFile();
                    }
                    randomAccessFile.close();
                    if (input != null) {
                        try {
                            input.close();
                        } catch (Exception e4) {
                        }
                    }
                    if (httpConnection != null) {
                        try {
                            httpConnection.disconnect();
                        } catch (Exception e5) {
                        }
                    }
                } catch (Exception e32) {
                    try {
                        Log.d("DOWNLOAD", "Fail to download file", e32);
                        e32.printStackTrace();
                        DownloadManager.dm.saveFile();
                        if (input != null) {
                            try {
                                input.close();
                            } catch (Exception e6) {
                            }
                        }
                        if (httpConnection != null) {
                            try {
                                httpConnection.disconnect();
                            } catch (Exception e7) {
                            }
                        }
                    } catch (Throwable th) {
                        if (input != null) {
                            try {
                                input.close();
                            } catch (Exception e8) {
                            }
                        }
                        if (httpConnection != null) {
                            try {
                                httpConnection.disconnect();
                            } catch (Exception e9) {
                            }
                        }
                    }
                }
            }
        }
    }

    public static DownloadManager getMgr() {
        return dm;
    }

    private DownloadManager() {
    }

    public void init(String propFile, String downloadPath, DownloadCallback downloadCallback, int threads, int errorInterval) {
        this.propFile = propFile;
        this.downloadPath = downloadPath;
        this.downloadCallback = downloadCallback;
        this.errorInterval = errorInterval;
        new File(propFile).getParentFile().mkdirs();
        new File(downloadPath).mkdirs();
        loadFile();
        for (int i = 0; i < threads; i++) {
            SysGlobal.execute(new DownloadThread());
        }
    }

    public boolean addTask(String type, String url, String file, String memo) {
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.type = type;
        downloadFile.fileDownloadLen = 0;
        downloadFile.fileURL = url;
        downloadFile.fileSize = 0;
        downloadFile.fileStatus = 0;
        downloadFile.memo = memo;
        downloadFile.downloadTime = DateUtils.formatDatetime(new Date(), "yyyyMMdd");
        int findSubId = 0;
        long lDate = Long.parseLong(DateUtils.formatDatetime(new Date(), "yyyyMMdd")) * 10000000;
        synchronized (this.listDownloadFile) {
            HashMap<Integer, Integer> hsmpSubId = new HashMap();
            for (int i = 0; i < this.listDownloadFile.size(); i++) {
                if (url.equals(((DownloadFile) this.listDownloadFile.get(i)).fileURL)) {
                    return true;
                }
                int subId = (int) (((DownloadFile) this.listDownloadFile.get(i)).fileId % 10000000);
                if (findSubId == 0 || subId < findSubId) {
                    findSubId = subId;
                }
                hsmpSubId.put(Integer.valueOf(subId), Integer.valueOf(subId));
            }
            while (hsmpSubId.get(Integer.valueOf(findSubId)) != null) {
                findSubId++;
            }
            downloadFile.fileId = ((long) findSubId) + lDate;
            if (file == null || "".equals(file)) {
                int lastIdx1 = url.lastIndexOf("/");
                int lastIdx2 = url.lastIndexOf("=");
                if (lastIdx1 == -1) {
                    lastIdx1 = lastIdx2;
                }
                if (lastIdx1 < lastIdx2) {
                    lastIdx1 = lastIdx2;
                }
                downloadFile.filePath = this.downloadPath + File.separatorChar + url.substring(lastIdx1 + 1);
            } else {
                downloadFile.filePath = file;
            }
            File download_file = new File(downloadFile.filePath);
            if (download_file.exists()) {
                downloadFile.fileStatus = 2;
            } else {
                try {
                    File pathFile = download_file.getParentFile();
                    if (!pathFile.exists() && !pathFile.mkdirs()) {
                        return false;
                    } else if (!pathFile.isDirectory()) {
                        return false;
                    }
                } catch (Exception e) {
                }
            }
            this.listDownloadFile.add(downloadFile);
            saveFile();
            this.fifoQueue.push(downloadFile);
            return true;
        }
    }

    public boolean isExists(DownloadFile downloadFile) {
        boolean contains;
        synchronized (this.listDownloadFile) {
            contains = this.listDownloadFile.contains(downloadFile);
        }
        return contains;
    }

    public void removeTask(String url) {
        synchronized (this.listDownloadFile) {
            for (int i = 0; i < this.listDownloadFile.size(); i++) {
                if (((DownloadFile) this.listDownloadFile.get(i)).fileURL.equals(url)) {
                    this.listDownloadFile.remove(i);
                    return;
                }
            }
        }
    }

    private static String getTmpFilename(DownloadFile downloadFile) {
        return downloadFile.filePath + ".tmp";
    }

    public void checkDone() {
        boolean isChanged = false;
        List<DownloadFile> listDone = new ArrayList();
        synchronized (this.listDownloadFile) {
            int i;
            for (i = 0; i < this.listDownloadFile.size(); i++) {
                File fileTmp;
                DownloadFile downloadFile = (DownloadFile) this.listDownloadFile.get(i);
                if (downloadFile.fileStatus == 2) {
                    fileTmp = new File(getTmpFilename(downloadFile));
                    File fileDest = new File(downloadFile.filePath);
                    if (!fileDest.isFile() && fileTmp.isFile()) {
                        fileDest.getParentFile().mkdirs();
                        fileTmp.renameTo(fileDest);
                    }
                    if (this.downloadCallback == null || this.downloadCallback.done(downloadFile.type, downloadFile.fileURL, downloadFile.filePath, downloadFile.memo, "success")) {
                        downloadFile.fileStatus = 3;
                        listDone.add(downloadFile);
                        isChanged = true;
                    } else {
                    }
                }
                if (downloadFile.fileStatus == 4) {
                    fileTmp = new File(getTmpFilename(downloadFile));
                    if (fileTmp.isFile()) {
                        fileTmp.delete();
                    }
                    if (this.downloadCallback != null) {
                        this.downloadCallback.done(downloadFile.type, downloadFile.fileURL, downloadFile.filePath, downloadFile.memo, "error");
                    }
                    listDone.add(downloadFile);
                    isChanged = true;
                }
            }
            for (i = 0; i < listDone.size(); i++) {
                this.listDownloadFile.remove(listDone.get(i));
            }
        }
        if (isChanged) {
            saveFile();
        }
    }

    public void loadFile() {
        List<HashMap> listProp = null;
        synchronized (this.propFile) {
            try {
                File file = new File(this.propFile + ".1");
                if (!file.isFile() || file.length() == 0) {
                    file = new File(this.propFile + ".2");
                }
                if (file.isFile()) {
                    FileInputStream fis = new FileInputStream(file);
                    listProp = JSONUtils.toList(new String(IOUtils.read(fis)));
                    fis.close();
                }
            } catch (Exception e) {
            }
            if (listProp != null) {
                int i;
                List<DownloadFile> list = new ArrayList();
                synchronized (this.listDownloadFile) {
                    for (i = 0; i < listProp.size(); i++) {
                        HashMap hsmpFile = (HashMap) listProp.get(i);
                        DownloadFile downloadFile = new DownloadFile();
                        downloadFile.type = (String) hsmpFile.get(AllAdvertisement.MEDIA_TYPE);
                        downloadFile.fileId = Long.parseLong((String) hsmpFile.get("FILE_ID"));
                        downloadFile.fileURL = (String) hsmpFile.get("FILE_URL");
                        downloadFile.memo = (String) hsmpFile.get("MEMO");
                        downloadFile.filePath = (String) hsmpFile.get("FILE_PATH");
                        downloadFile.fileSize = Integer.parseInt((String) hsmpFile.get("FILE_SIZE"));
                        downloadFile.fileDownloadLen = Integer.parseInt((String) hsmpFile.get("FILE_LEN"));
                        downloadFile.fileStatus = Integer.parseInt((String) hsmpFile.get("FILE_STATUS"));
                        downloadFile.downloadTime = (String) hsmpFile.get("DOWNLOAD_TIME");
                        if (StringUtils.isBlank(downloadFile.downloadTime)) {
                            downloadFile.downloadTime = DateUtils.formatDatetime(new Date(), "yyyyMMdd");
                        }
                        this.listDownloadFile.add(downloadFile);
                        list.add(downloadFile);
                    }
                }
                for (i = 0; i < list.size(); i++) {
                    this.fifoQueue.push(list.get(i));
                }
            }
        }
    }

    public void saveFile() {
        List listProp = new ArrayList();
        synchronized (this.listDownloadFile) {
            for (int i = 0; i < this.listDownloadFile.size(); i++) {
                DownloadFile downloadFile = (DownloadFile) this.listDownloadFile.get(i);
                synchronized (downloadFile) {
                    HashMap hsmpFile = new HashMap();
                    hsmpFile.put(AllAdvertisement.MEDIA_TYPE, downloadFile.type);
                    hsmpFile.put("FILE_ID", Long.toString(downloadFile.fileId));
                    hsmpFile.put("FILE_URL", downloadFile.fileURL);
                    hsmpFile.put("FILE_PATH", downloadFile.filePath);
                    hsmpFile.put("MEMO", downloadFile.memo);
                    hsmpFile.put("FILE_SIZE", Long.toString((long) downloadFile.fileSize));
                    hsmpFile.put("FILE_LEN", Long.toString((long) downloadFile.fileDownloadLen));
                    hsmpFile.put("FILE_STATUS", Long.toString((long) downloadFile.fileStatus));
                    hsmpFile.put("DOWNLOAD_TIME", downloadFile.downloadTime);
                    listProp.add(hsmpFile);
                }
            }
        }
        String filePropText = JSONUtils.toJSON(listProp);
        if (filePropText != null) {
            byte[] fileData = filePropText.getBytes();
            synchronized (this.propFile) {
                try {
                    String fileName2 = this.propFile + ".2";
                    FileOutputStream fos = new FileOutputStream(fileName2);
                    fos.write(fileData);
                    fos.flush();
                    fos.close();
                    File file2 = new File(fileName2);
                    File file1 = new File(this.propFile + ".1");
                    if (file1.exists()) {
                        file1.delete();
                    }
                    file2.renameTo(file1);
                    ShellUtils.shell("cat " + file1.getAbsolutePath() + " > " + file2.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
