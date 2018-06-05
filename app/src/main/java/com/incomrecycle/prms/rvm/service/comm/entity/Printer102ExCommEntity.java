package com.incomrecycle.prms.rvm.service.comm.entity;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MotionEventCompat;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.SyslogMessage;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.queue.FIFOQueue;
import com.incomrecycle.common.task.TaskAction;
import com.incomrecycle.common.task.TickTaskThread;
import com.incomrecycle.common.utils.EncryptUtils;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.HardwareAlarmState;
import com.incomrecycle.prms.rvm.common.SysDef.ServiceName;
import com.incomrecycle.prms.rvm.service.ServiceGlobal;
import com.incomrecycle.prms.rvm.service.comm.CommEntity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android_serialport_api.SerialPort;

public class Printer102ExCommEntity extends BasePrinter implements CommEntity {
    private static final String BLANK_LINE = "                                ";
    private static final byte[] IMAGE_HEAD = new byte[]{(byte) 27, (byte) 64, (byte) 27, (byte) 99, (byte) 0, (byte) 27, (byte) 49, (byte) 0, (byte) 27, (byte) 42, (byte) 33, (byte) 0, (byte) 0};
    private static final int IMAGE_MAX_WIDTH = 380;
    private static final String LINE = "\n";
    private static final int LINE_MAX_SIZE = 32;
    private static final Logger logger = LoggerFactory.getLogger("PrinterCommEntity");
    private byte XYSize = (byte) 0;
    private String align = null;
    private int baud = 9600;
    private String charset = null;
    private boolean checkStateEnable = true;
    private String color = PrinterOptions.COLOR_BLACK;
    private int errorTimes = 0;
    private int id;
    private long lHasNoPaper = 0;
    private long lHasNoPaperDelayCheck = 300000;
    boolean logRecvEnable = false;
    private HardwareAlarmState paperState = HardwareAlarmState.UNKNOWN;
    private FIFOQueue printFIFOQueue = new FIFOQueue();
    private Thread printThread = new Thread() {
        public void run() {
            while (true) {
                PrinterCmdExecutorInfo printerCmdExecutorInfo = (PrinterCmdExecutorInfo) Printer102ExCommEntity.this.printFIFOQueue.pop();
                if (printerCmdExecutorInfo != null) {
                    printerCmdExecutorInfo.execute();
                } else {
                    return;
                }
            }
        }
    };
    private PrinterCmdExecutor printerCmdExecutorPaperState = new PrinterCmdExecutor() {
        public boolean execute(String str, HashMap<String, String> hashMap) {
            Printer102ExCommEntity.this.execPaperState();
            return true;
        }
    };
    private FIFOQueue printerFIFOQueue = new FIFOQueue();
    private SerialPort printerSerialPort = null;
    private HardwareAlarmState printerState = HardwareAlarmState.UNKNOWN;
    private HardwareAlarmState realPaperState = HardwareAlarmState.UNKNOWN;

    private class InitThread extends Thread {
        private InitThread() {
        }

        public void run() {
            Printer102ExCommEntity.this.checkPaperState();
        }
    }

    private static class PrinterCmdExecutorInfo {
        HashMap<String, String> hsmpParam;
        PrinterCmdExecutor printerCmdExecutor;
        String str;

        public PrinterCmdExecutorInfo(PrinterCmdExecutor printerCmdExecutor, String str, HashMap<String, String> hsmpParam) {
            this.printerCmdExecutor = printerCmdExecutor;
            this.str = str;
            this.hsmpParam = hsmpParam;
        }

        public void execute() {
            this.printerCmdExecutor.execute(this.str, this.hsmpParam);
        }
    }

    private class RecvThread extends Thread {
        private byte recvByte;

        private RecvThread() {
            this.recvByte = (byte) -1;
        }

        public void run() {
            InputStream is = Printer102ExCommEntity.this.printerSerialPort.getInputStream();
            byte[] buffer = new byte[1];
            while (is != null) {
                try {
                    if (is.read(buffer) <= 0) {
                        is.close();
                        break;
                    }
                    if (this.recvByte != buffer[0]) {
                        this.recvByte = buffer[0];
                        Printer102ExCommEntity.logger.debug("Printer State Changed:" + EncryptUtils.byte2hex(buffer, 0, 1));
                    }
                    if (Printer102ExCommEntity.this.logRecvEnable) {
                        Printer102ExCommEntity.logger.debug("Printer RECV:" + EncryptUtils.byte2hex(buffer, 0, 1));
                    }
                    Printer102ExCommEntity.this.errorTimes = 0;
                    Printer102ExCommEntity.this.monitorError();
                    Printer102ExCommEntity.this.printerFIFOQueue.push(new Byte(buffer[0]));
                } catch (Exception e) {
                    Printer102ExCommEntity.this.printerSerialPort.close();
                    Printer102ExCommEntity.this.printerSerialPort = null;
                    return;
                } catch (Throwable th) {
                    Printer102ExCommEntity.this.printerSerialPort.close();
                    Printer102ExCommEntity.this.printerSerialPort = null;
                }
            }
            Printer102ExCommEntity.this.printerSerialPort.close();
            Printer102ExCommEntity.this.printerSerialPort = null;
        }
    }

    public Printer102ExCommEntity(int id) {
        this.id = id;
        this.lHasNoPaperDelayCheck = Long.parseLong(SysConfig.get("COM.PRINTER.PAPER.CHECK.MAXDURATION")) * 1000;
        TickTaskThread.getTickTaskThread().register(new TaskAction() {
            public void execute() {
                Printer102ExCommEntity.this.checkPaperState();
            }
        }, (double) Integer.parseInt(SysConfig.get("COM.PRINTER.PAPER.CHECK.INTERVAL")), false);
    }

    public void init() {
        synchronized (this) {
            try {
                if (this.printerSerialPort == null) {
                    this.baud = Integer.parseInt(SysConfig.get("COM.PRINTER" + this.id + ".BAND"));
                    this.printerSerialPort = new SerialPort(new File(SysConfig.get("COM.PRINTER" + this.id + "." + SysConfig.get("PLATFORM"))), this.baud, 0);
                    this.paperState = HardwareAlarmState.UNKNOWN;
                    this.realPaperState = HardwareAlarmState.UNKNOWN;
                    this.printerState = HardwareAlarmState.UNKNOWN;
                    this.errorTimes = 0;
                    SysGlobal.execute(new RecvThread());
                    SysGlobal.execute(new InitThread());
                    SysGlobal.execute(this.printThread);
                }
            } catch (Exception e) {
            }
        }
    }

    public String execute(String cmd, String json) throws Exception {
        init();
        String r4 = "";
        try {
            this.checkStateEnable = false;
            if ("CHECK:START".equalsIgnoreCase(cmd)) {
                return null;
            }
            if ("CHECK:END".equalsIgnoreCase(cmd)) {
                this.checkStateEnable = true;
                return null;
            } else if ((ServiceName.PRINTER + this.id + ":CHECK_OPEN").equalsIgnoreCase(cmd)) {
                if (this.printerSerialPort != null) {
                    r4 = "TRUE";
                } else {
                    r4 = "FALSE";
                }
                this.checkStateEnable = true;
                return r4;
            } else if ((ServiceName.PRINTER + this.id + ":state").equalsIgnoreCase(cmd)) {
                r4 = execute("state", null);
                this.checkStateEnable = true;
                return r4;
            } else if ((ServiceName.PRINTER + this.id + ":init").equalsIgnoreCase(cmd)) {
                r4 = execute("init", null);
                this.checkStateEnable = true;
                return r4;
            } else {
                HashMap hsmpPrinter;
                if ((ServiceName.PRINTER + this.id + ":printer").equalsIgnoreCase(cmd)) {
                    hsmpPrinter = JSONUtils.toHashMap(json);
                    String printModel = hsmpPrinter == null ? null : (String) hsmpPrinter.get("MODEL");
                    if ("TRUE".equalsIgnoreCase(SysConfig.get("PRINTER.DEBUG"))) {
                        printModel = initPrintModel(printModel);
                    }
                    print(printModel, hsmpPrinter == null ? null : (HashMap) hsmpPrinter.get("PARAM"));
                }
                if ((ServiceName.PRINTER + this.id + ":cut").equalsIgnoreCase(cmd)) {
                    r4 = execute("cut", null);
                    this.checkStateEnable = true;
                    return r4;
                } else if ("enable".equalsIgnoreCase(cmd)) {
                    if (this.printerSerialPort == null || this.realPaperState == HardwareAlarmState.ALARM || this.printerState == HardwareAlarmState.ALARM) {
                        r4 = "FALSE";
                        this.checkStateEnable = true;
                        return r4;
                    }
                    r4 = "TRUE";
                    this.checkStateEnable = true;
                    return r4;
                } else if ("reset".equals(cmd)) {
                    this.printerFIFOQueue.reset();
                    this.checkStateEnable = true;
                    return null;
                } else if ("init".equals(cmd)) {
                    this.checkStateEnable = true;
                    return null;
                } else {
                    if ("printer".equals(cmd)) {
                        hsmpPrinter = JSONUtils.toHashMap(json);
                        print(initPrintModel(hsmpPrinter == null ? null : (String) hsmpPrinter.get("MODEL")), hsmpPrinter == null ? null : (HashMap) hsmpPrinter.get("PARAM"));
                    }
                    if ("preview".equals(cmd)) {
                        hsmpPrinter = JSONUtils.toHashMap(json);
                        r4 = preview(initPrintModel(hsmpPrinter == null ? null : (String) hsmpPrinter.get("MODEL")), hsmpPrinter == null ? null : (HashMap) hsmpPrinter.get("PARAM"));
                        this.checkStateEnable = true;
                        return r4;
                    } else if ("cut".equals(cmd)) {
                        this.checkStateEnable = true;
                        return null;
                    } else if (!"state".equals(cmd)) {
                        this.checkStateEnable = true;
                        return null;
                    } else if (this.realPaperState == HardwareAlarmState.NORMAL) {
                        r4 = "havePaper";
                        this.checkStateEnable = true;
                        return r4;
                    } else if (this.realPaperState == HardwareAlarmState.ALARM) {
                        r4 = "noPaper";
                        this.checkStateEnable = true;
                        return r4;
                    } else {
                        r4 = "unknown";
                        this.checkStateEnable = true;
                        return r4;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.checkStateEnable = true;
        }
        return r4;
    }

    protected String initPrintModel(String printModel) {
        return printModel;
    }

    private boolean sendData(byte[] data, int pos, int len) {
        try {
            if (this.printerSerialPort == null) {
                return false;
            }
            long lPrinterTime = 1000;
            if (!StringUtils.isBlank(SysConfig.get("PRINTER.SPEEDTIME"))) {
                lPrinterTime = Long.parseLong(SysConfig.get("PRINTER.SPEEDTIME"));
            }
            synchronized (this) {
                OutputStream os = this.printerSerialPort.getOutputStream();
                int count = 0;
                while (count < len) {
                    int n = len - count;
                    if (n > 1166) {
                        n = 1166;
                    }
                    long delayMillisecond = (((long) n) * lPrinterTime) / ((long) (this.baud / 12));
                    long lStartTime = System.currentTimeMillis();
                    os.write(data, pos + count, n);
                    os.flush();
                    count += n;
                    long lEndTime = System.currentTimeMillis();
                    if (lEndTime - lStartTime < delayMillisecond) {
                        try {
                            Thread.sleep(delayMillisecond - (lEndTime - lStartTime));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return true;
        } catch (Exception e2) {
            return false;
        }
    }

    private boolean sendData(byte[] data) {
        return sendData(data, 0, data.length);
    }

    private void checkPaperState() {
        execPrinterCmdExecutor(this.printerCmdExecutorPaperState, null, null);
    }

    private void clearPrinterBuffer() {
        this.printerFIFOQueue.reset();
        if (sendData(new byte[]{(byte) 28, (byte) 118})) {
            this.printerFIFOQueue.pop(2000);
        }
    }

    private void execPaperState() {
        if (this.checkStateEnable) {
            byte[] bufferState = new byte[]{(byte) 28, (byte) 118};
            long lStartTime = System.currentTimeMillis();
            if (sendData(bufferState)) {
                this.logRecvEnable = false;
                Byte obj = (Byte) this.printerFIFOQueue.pop(1000);
                long lEndTime = System.currentTimeMillis();
                if (obj == null) {
                    this.logRecvEnable = true;
                    this.errorTimes++;
                    logger.debug("State CMD:" + EncryptUtils.byte2hex(bufferState) + ";OOT:" + (lEndTime - lStartTime) + "ms");
                    monitorError();
                    return;
                }
                this.printerFIFOQueue.reset();
                HashMap<String, String> hsmpParam;
                if ((((Byte) obj).byteValue() & 1) == 1) {
                    this.lHasNoPaper = 0;
                    this.realPaperState = HardwareAlarmState.NORMAL;
                    if (this.paperState != HardwareAlarmState.NORMAL) {
                        this.paperState = HardwareAlarmState.NORMAL;
                        hsmpParam = new HashMap();
                        hsmpParam.put("type", "PRINTER_NO_PAPER_RECOVERY");
                        hsmpParam.put("data", "" + this.id);
                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                        logger.debug("Alarm recovery;CMD:" + EncryptUtils.byte2hex(bufferState) + ";RECV:" + EncryptUtils.byte2hex(new byte[]{ (Byte)obj.byteValue()}));
                        return;
                    }
                    return;
                }
                this.realPaperState = HardwareAlarmState.ALARM;
                if (this.paperState != HardwareAlarmState.ALARM) {
                    if (this.lHasNoPaper == 0) {
                        this.lHasNoPaper = System.currentTimeMillis();
                    }
                    if (this.lHasNoPaperDelayCheck + this.lHasNoPaper < System.currentTimeMillis()) {
                        this.paperState = HardwareAlarmState.ALARM;
                        hsmpParam = new HashMap();
                        hsmpParam.put("type", "PRINTER_NO_PAPER");
                        hsmpParam.put("data", "" + this.id);
                        ServiceGlobal.getCommEventQueye().push(hsmpParam);
                        logger.debug("Alarm raise;CMD:" + EncryptUtils.byte2hex(bufferState) + ";RECV:" + EncryptUtils.byte2hex(new byte[]{obj.byteValue()}));
                    }
                }
            }
        }
    }

    private void monitorError() {
        if (this.errorTimes == 5 && this.printerState != HardwareAlarmState.ALARM) {
            this.printerState = HardwareAlarmState.ALARM;
            HashMap<String, String> hsmpParam = new HashMap();
            hsmpParam.put("type", "PRINTER_ERROR");
            hsmpParam.put("data", "" + this.id);
            ServiceGlobal.getCommEventQueye().push(hsmpParam);
        }
        if (this.errorTimes == 0 && this.printerState != HardwareAlarmState.NORMAL) {
            this.printerState = HardwareAlarmState.NORMAL;
            HashMap hsmpParam = new HashMap();
            hsmpParam.put("type", "PRINTER_ERROR_RECOVERY");
            hsmpParam.put("data", "" + this.id);
            ServiceGlobal.getCommEventQueye().push(hsmpParam);
        }
    }

    protected boolean execPrinterCmdExecutor(PrinterCmdExecutor printerCmdExecutor, String str, HashMap<String, String> hsmpParam) {
        this.printFIFOQueue.push(new PrinterCmdExecutorInfo(printerCmdExecutor, str, hsmpParam));
        return true;
    }

    protected boolean reset() {
        sendData(new byte[]{(byte) 27, (byte) 64});
        this.XYSize = (byte) 0;
        this.charset = null;
        this.align = null;
        this.color = null;
        return true;
    }

    protected boolean setSettings(HashMap<String, String> hsmpSettings) {
        if (hsmpSettings != null) {
            int size;
            String val = (String) hsmpSettings.get(PrinterOptions.SETTINGS_STRONG);
            if (val != null) {
                if ("Y".equalsIgnoreCase(val)) {
                }
                if ("N".equalsIgnoreCase(val)) {
                }
            }
            val = (String) hsmpSettings.get(PrinterOptions.SETTINGS_HIGHSIZE);
            if (val != null) {
                size = Integer.parseInt(val);
                if (size >= 1 && size <= 8) {
                    this.XYSize = (byte) ((this.XYSize & 240) | ((size - 1) & 15));
                }
            }
            val = (String) hsmpSettings.get(PrinterOptions.SETTINGS_WIDESIZE);
            if (val != null) {
                size = Integer.parseInt(val);
                if (size >= 1 && size <= 8) {
                    this.XYSize = (byte) ((this.XYSize & 15) | (((size - 1) << 4) & 240));
                }
            }
            if (!(StringUtils.isBlank((String) hsmpSettings.get(PrinterOptions.SETTINGS_HIGHSIZE)) && StringUtils.isBlank((String) hsmpSettings.get(PrinterOptions.SETTINGS_WIDESIZE)))) {
                sendData(new byte[]{(byte) 27, (byte) 88, (byte) (((this.XYSize >> 4) & 15) + 1), (byte) ((this.XYSize & 15) + 1)});
            }
            val = (String) hsmpSettings.get(PrinterOptions.SETTINGS_UNDERLINE);
            if (val != null) {
                byte b = (byte) 0;
                if ("Y".equalsIgnoreCase(val)) {
                    b = (byte) 1;
                }
                if ("N".equalsIgnoreCase(val)) {
                    b = (byte) 0;
                }
                sendData(new byte[]{(byte) 27, (byte) 45, b});
            }
            val = (String) hsmpSettings.get(PrinterOptions.SETTINGS_ALIGN);
            if (val != null) {
                setAlign(val.trim());
            }
            val = (String) hsmpSettings.get(PrinterOptions.SETTINGS_COLOR);
            if (val != null) {
                setColor(val.trim());
            }
            val = (String) hsmpSettings.get(PrinterOptions.SETTINGS_CHARSET);
            if (val != null) {
                setCharset(val.trim());
            }
        }
        return true;
    }

    protected boolean setAlign(String align) {
        this.align = align;
        byte b = (byte) -1;
        if (PrinterOptions.ALIGN_LEFT.equalsIgnoreCase(align)) {
            b = (byte) 0;
        }
        if (PrinterOptions.ALIGN_CENTER.equalsIgnoreCase(align)) {
            b = (byte) 1;
        }
        if (PrinterOptions.ALIGN_RIGHT.equalsIgnoreCase(align)) {
            b = (byte) 2;
        }
        if (b != (byte) -1) {
        }
        return true;
    }

    protected boolean setColor(String color) {
        if (this.color == null || !this.color.equalsIgnoreCase(color)) {
            byte b = (byte) 0;
            if (PrinterOptions.COLOR_WHITE.equalsIgnoreCase(color)) {
                b = (byte) 1;
            }
            sendData(new byte[]{(byte) 29, (byte) 66, b});
            this.color = color;
        }
        return true;
    }

    protected boolean setCharset(String charset) {
        this.charset = charset;
        if ("GBK".equalsIgnoreCase(charset) || "GB2312".equalsIgnoreCase(charset)) {
            sendData(new byte[]{(byte) 28, (byte) 38});
        } else {
            sendData(new byte[]{(byte) 28, (byte) 46});
        }
        return true;
    }

    protected boolean printLine() {
        String str;
        String newColor;
        int count = 32 / (((this.XYSize >> 4) & 15) + 1);
        if (count <= 0) {
            str = LINE;
        } else {
            str = BLANK_LINE.substring(0, count) + LINE;
        }
        String oldColor = this.color;
        if (PrinterOptions.COLOR_WHITE.equalsIgnoreCase(this.color)) {
            newColor = PrinterOptions.COLOR_BLACK;
        } else {
            newColor = PrinterOptions.COLOR_WHITE;
        }
        setColor(newColor);
        sendData(str.getBytes());
        setColor(oldColor);
        return true;
    }

    protected List<String> splitText(String text, int lineCharCount) {
        List<String> list = new ArrayList();
        StringBuffer sb = new StringBuffer();
        int strLen = text.length();
        int pos = 0;
        for (int i = 0; i < strLen; i++) {
            char cc = text.charAt(i);
            if (cc == '\n') {
                if (i == pos) {
                    list.add(LINE);
                } else if (i - pos == lineCharCount) {
                    list.add(sb.toString());
                    sb.setLength(0);
                } else {
                    list.add(sb.toString());
                    list.add(LINE);
                    sb.setLength(0);
                }
                pos = i + 1;
            } else {
                sb.append(cc);
            }
        }
        if (sb.length() > 0) {
            list.add(sb.toString());
        }
        return list;
    }

    protected boolean printText(String text) {
        text = formatText(text);
        String charset = this.charset;
        if (StringUtils.isBlank(charset)) {
            charset = null;
        }
        int i = 0;
        while (i < 2) {
            byte[] data;
            if (charset == null) {
                try {
                    data = text.getBytes();
                } catch (Exception e) {
                    charset = null;
                    i++;
                }
            } else {
                try {
                    data = text.getBytes(charset);
                    sendData(data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    protected boolean printQRCode(String mode, String qrCode) {
        reset();
        byte v = (byte) 0;
        byte r = (byte) 0;
        byte m = (byte) 0;
        int n = qrCode.length();
        if ("PDF417".equalsIgnoreCase(mode)) {
            v = (byte) (new Double(Math.sqrt((double) n)).intValue() + 4);
            r = (byte) 4;
            m = (byte) 98;
        } else if ("DATAMATRIX".equalsIgnoreCase(mode)) {
            v = (byte) 0;
            r = (byte) 0;
            m = (byte) 99;
        } else if ("QRCODE".equalsIgnoreCase(mode)) {
            v = (byte) 0;
            r = (byte) 4;
            m = (byte) 97;
        }
        if (m != (byte) 0) {
            byte[] data = qrCode.getBytes();
            byte[] buff = new byte[(data.length + 7)];
            buff[0] = (byte) 29;
            buff[1] = (byte) 107;
            buff[2] = m;
            buff[3] = v;
            buff[4] = r;
            buff[5] = (byte) (n & MotionEventCompat.ACTION_MASK);
            buff[6] = (byte) ((n >> 8) & MotionEventCompat.ACTION_MASK);
            System.arraycopy(data, 0, buff, 7, data.length);
            sendData(buff);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    protected boolean printBarcode(String mode, String barcode) {
        reset();
        int m = 0;
        byte[] data = null;
        byte[] buff;
        if ("UPC-A".equalsIgnoreCase(mode)) {
            barcode = barcode.trim();
            if (barcode.length() >= 11 && barcode.length() <= 12) {
                m = 65;
                data = barcode.getBytes();
                if (data != null) {
                    buff = new byte[(data.length + 4)];
                    buff[0] = (byte) 29;
                    buff[1] = (byte) 107;
                    buff[2] = (byte) m;
                    buff[3] = (byte) data.length;
                    System.arraycopy(data, 0, buff, 4, data.length);
                    sendData(buff);
                }
            }
        } else if ("UPC-E".equalsIgnoreCase(mode)) {
            barcode = barcode.trim();
            if (barcode.length() == 8) {
                m = 66;
                data = barcode.getBytes();
                if (data != null) {
                    buff = new byte[(data.length + 4)];
                    buff[0] = (byte) 29;
                    buff[1] = (byte) 107;
                    buff[2] = (byte) m;
                    buff[3] = (byte) data.length;
                    System.arraycopy(data, 0, buff, 4, data.length);
                    sendData(buff);
                }
            }
        } else if ("EAN13".equalsIgnoreCase(mode) || "JAN13".equalsIgnoreCase(mode)) {
            barcode = barcode.trim();
            if (barcode.length() >= 12 && barcode.length() <= 13) {
                m = 67;
                data = barcode.getBytes();
                if (data != null) {
                    buff = new byte[(data.length + 4)];
                    buff[0] = (byte) 29;
                    buff[1] = (byte) 107;
                    buff[2] = (byte) m;
                    buff[3] = (byte) data.length;
                    System.arraycopy(data, 0, buff, 4, data.length);
                    sendData(buff);
                }
            }
        } else if ("EAN8".equalsIgnoreCase(mode) || "JAN8".equalsIgnoreCase(mode)) {
            barcode = barcode.trim();
            if (barcode.length() >= 7 && barcode.length() <= 8) {
                m = 68;
                data = barcode.getBytes();
                if (data != null) {
                    buff = new byte[(data.length + 4)];
                    buff[0] = (byte) 29;
                    buff[1] = (byte) 107;
                    buff[2] = (byte) m;
                    buff[3] = (byte) data.length;
                    System.arraycopy(data, 0, buff, 4, data.length);
                    sendData(buff);
                }
            }
        } else if ("CODE39".equalsIgnoreCase(mode)) {
            barcode = barcode.trim();
            if (barcode.length() >= 1 && barcode.length() <= 253) {
                m = 69;
                data = barcode.getBytes();
                if (data != null) {
                    buff = new byte[(data.length + 4)];
                    buff[0] = (byte) 29;
                    buff[1] = (byte) 107;
                    buff[2] = (byte) m;
                    buff[3] = (byte) data.length;
                    System.arraycopy(data, 0, buff, 4, data.length);
                    sendData(buff);
                }
            }
        } else if ("ITF".equalsIgnoreCase(mode)) {
            barcode = barcode.trim();
            if (barcode.length() >= 1 && barcode.length() <= MotionEventCompat.ACTION_MASK) {
                m = 70;
                data = barcode.getBytes();
                if (data != null) {
                    buff = new byte[(data.length + 4)];
                    buff[0] = (byte) 29;
                    buff[1] = (byte) 107;
                    buff[2] = (byte) m;
                    buff[3] = (byte) data.length;
                    System.arraycopy(data, 0, buff, 4, data.length);
                    sendData(buff);
                }
            }
        } else if ("CODABAR".equalsIgnoreCase(mode)) {
            barcode = barcode.trim();
            if (barcode.length() >= 1 && barcode.length() <= MotionEventCompat.ACTION_MASK) {
                m = 71;
                data = barcode.getBytes();
                if (data != null) {
                    buff = new byte[(data.length + 4)];
                    buff[0] = (byte) 29;
                    buff[1] = (byte) 107;
                    buff[2] = (byte) m;
                    buff[3] = (byte) data.length;
                    System.arraycopy(data, 0, buff, 4, data.length);
                    sendData(buff);
                }
            }
        } else if ("CODE93".equalsIgnoreCase(mode)) {
            barcode = barcode.trim();
            if (barcode.length() >= 1 && barcode.length() <= MotionEventCompat.ACTION_MASK) {
                m = 72;
                data = barcode.getBytes();
                if (data != null) {
                    buff = new byte[(data.length + 4)];
                    buff[0] = (byte) 29;
                    buff[1] = (byte) 107;
                    buff[2] = (byte) m;
                    buff[3] = (byte) data.length;
                    System.arraycopy(data, 0, buff, 4, data.length);
                    sendData(buff);
                }
            }
        } else {
            if ("CODE128".equalsIgnoreCase(mode)) {
                String newBarcode = StringUtils.replace(barcode.trim(), "{", "{{");
                if (newBarcode.length() >= 2 && newBarcode.length() <= 253) {
                    m = 73;
                    data = ("{B" + newBarcode).getBytes();
                }
            }
            if (data != null) {
                buff = new byte[(data.length + 4)];
                buff[0] = (byte) 29;
                buff[1] = (byte) 107;
                buff[2] = (byte) m;
                buff[3] = (byte) data.length;
                System.arraycopy(data, 0, buff, 4, data.length);
                sendData(buff);
            }
        }
        return true;
    }

    protected boolean printData(byte[] data, int offset, int count) {
        if (count > 2 && data[offset] == (byte) 27 && data[offset + 1] == (byte) 64) {
            clearPrinterBuffer();
        }
        int n = ((count + 1154) - 1) / 1154;
        for (int i = 0; i < n; i++) {
            int finalCount = count - (i * 1154);
            if (finalCount > 1154) {
                finalCount = 1154;
            }
            sendData(data, (i * 1154) + offset, finalCount);
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean printImage(String r18, String r19, String r20, String r21) {
        /*
        r17 = this;
        r9 = 0;
        r8 = 380; // 0x17c float:5.32E-43 double:1.877E-321;
        r6 = 0;
        r12 = 380; // 0x17c float:5.32E-43 double:1.877E-321;
        r11 = 0;
        r13 = com.incomrecycle.common.utils.StringUtils.isBlank(r20);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        if (r13 != 0) goto L_0x0011;
    L_0x000d:
        r12 = java.lang.Integer.parseInt(r20);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
    L_0x0011:
        r13 = com.incomrecycle.common.utils.StringUtils.isBlank(r21);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        if (r13 != 0) goto L_0x001b;
    L_0x0017:
        r11 = java.lang.Integer.parseInt(r21);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
    L_0x001b:
        r13 = 380; // 0x17c float:5.32E-43 double:1.877E-321;
        if (r12 <= r13) goto L_0x0098;
    L_0x001f:
        if (r11 <= 0) goto L_0x0025;
    L_0x0021:
        r13 = r8 * r11;
        r6 = r13 / r12;
    L_0x0025:
        r4 = 0;
        r13 = com.incomrecycle.common.utils.StringUtils.isBlank(r18);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        if (r13 != 0) goto L_0x0047;
    L_0x002c:
        r7 = new java.io.File;	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r0 = r18;
        r7.<init>(r0);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r13 = r7.isFile();	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        if (r13 == 0) goto L_0x0047;
    L_0x0039:
        r13 = r7.length();	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r15 = 0;
        r13 = (r13 > r15 ? 1 : (r13 == r15 ? 0 : -1));
        if (r13 <= 0) goto L_0x0047;
    L_0x0043:
        r4 = android.graphics.drawable.Drawable.createFromPath(r18);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
    L_0x0047:
        if (r4 != 0) goto L_0x0058;
    L_0x0049:
        r13 = com.incomrecycle.common.utils.StringUtils.isBlank(r19);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        if (r13 != 0) goto L_0x0058;
    L_0x004f:
        r9 = com.incomrecycle.common.utils.IOUtils.getResourceAsInputStream(r19);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r13 = 0;
        r4 = android.graphics.drawable.Drawable.createFromStream(r9, r13);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
    L_0x0058:
        if (r4 == 0) goto L_0x0093;
    L_0x005a:
        r5 = r4.getIntrinsicWidth();	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r3 = r4.getIntrinsicHeight();	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        if (r6 != 0) goto L_0x006a;
    L_0x0064:
        if (r5 <= r8) goto L_0x009d;
    L_0x0066:
        r13 = r8 * r3;
        r6 = r13 / r5;
    L_0x006a:
        r13 = android.graphics.Bitmap.Config.ARGB_4444;	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r1 = android.graphics.Bitmap.createBitmap(r8, r6, r13);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r2 = new android.graphics.Canvas;	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r2.<init>(r1);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r13 = 0;
        r14 = 0;
        r15 = r1.getWidth();	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r16 = r1.getHeight();	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r0 = r16;
        r4.setBounds(r13, r14, r15, r0);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r4.draw(r2);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r10 = bitmap2printerdata(r1);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r1.recycle();	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
        r0 = r17;
        r0.printImage(r10);	 Catch:{ Exception -> 0x00a0, all -> 0x00a5 }
    L_0x0093:
        com.incomrecycle.common.utils.IOUtils.close(r9);
    L_0x0096:
        r13 = 1;
        return r13;
    L_0x0098:
        r8 = r12;
        if (r11 <= 0) goto L_0x0025;
    L_0x009b:
        r6 = r11;
        goto L_0x0025;
    L_0x009d:
        r8 = r5;
        r6 = r3;
        goto L_0x006a;
    L_0x00a0:
        r13 = move-exception;
        com.incomrecycle.common.utils.IOUtils.close(r9);
        goto L_0x0096;
    L_0x00a5:
        r13 = move-exception;
        com.incomrecycle.common.utils.IOUtils.close(r9);
        throw r13;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.incomrecycle.prms.rvm.service.comm.entity.Printer102ExCommEntity.printImage(java.lang.String, java.lang.String, java.lang.String, java.lang.String):boolean");
    }

    protected boolean printImage(List<byte[]> listData) {
        clearPrinterBuffer();
        int lineLen = ((byte[]) listData.get(0)).length;
        for (int i = 0; i < listData.size(); i += 3) {
            if (i + 2 < listData.size()) {
                byte[] buff = new byte[(lineLen * 3)];
                byte[] line0 = (byte[]) listData.get(i + 0);
                byte[] line1 = (byte[]) listData.get(i + 1);
                byte[] line2 = (byte[]) listData.get(i + 2);
                for (int c = 0; c < lineLen; c++) {
                    buff[(c * 3) + 0] = line0[c];
                    buff[(c * 3) + 1] = line1[c];
                    buff[(c * 3) + 2] = line2[c];
                }
                printImage(buff, 33, lineLen, 3);
            } else {
                for (int r = i; r < listData.size(); r++) {
                    printImage((byte[]) listData.get(r), 1, lineLen, 1);
                }
            }
        }
        clearPrinterBuffer();
        return true;
    }

    private boolean printImage(byte[] data, int m, int rlen, int r) {
        while (rlen > 1) {
            boolean isPrinted = false;
            for (int i = 0; i < r; i++) {
                if (data[((rlen - 1) * r) + i] != (byte) 0) {
                    isPrinted = true;
                    break;
                }
            }
            if (isPrinted) {
                break;
            }
            rlen--;
        }
        int nl = rlen & MotionEventCompat.ACTION_MASK;
        int nh = (rlen >> 8) & MotionEventCompat.ACTION_MASK;
        byte[] buff = new byte[(((rlen * r) + IMAGE_HEAD.length) + 1)];
        System.arraycopy(IMAGE_HEAD, 0, buff, 0, IMAGE_HEAD.length);
        System.arraycopy(data, 0, buff, IMAGE_HEAD.length, rlen * r);
        buff[IMAGE_HEAD.length - 3] = (byte) m;
        buff[IMAGE_HEAD.length - 2] = (byte) nl;
        buff[IMAGE_HEAD.length - 1] = (byte) nh;
        buff[buff.length - 1] = SyslogMessage.FACILITY_LOG_AUDIT;
        sendData(buff);
        this.XYSize = (byte) 0;
        this.charset = null;
        this.align = null;
        this.color = null;
        return true;
    }

    public static List<byte[]> bitmap2printerdata(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        List<byte[]> listRow = new ArrayList();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int rr = 0; rr < h; rr += 8) {
            for (int x = 0; x < w; x++) {
                byte b = (byte) 0;
                for (int y = 0; y < 8; y++) {
                    int val = 0;
                    if (rr + y < h) {
                        int color = bitmap.getPixel(x, rr + y) & -1;
                        if ((16711680 & color) <= GravityCompat.RELATIVE_LAYOUT_DIRECTION || (MotionEventCompat.ACTION_POINTER_INDEX_MASK & color) <= 32768 || (color & MotionEventCompat.ACTION_MASK) <= 128) {
                            val = 1;
                        } else {
                            val = 0;
                        }
                    }
                    b = (byte) (((byte) (b << 1)) | val);
                }
                baos.write(b);
            }
            listRow.add(baos.toByteArray());
            baos.reset();
        }
        return listRow;
    }

    public static void saveBitmap(String filename, Bitmap bmp) {
        File f = new File(filename);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean cutPaper(String mode) {
        byte b = (byte) 0;
        if (PrinterOptions.CUT_HALF.equalsIgnoreCase(mode)) {
            b = (byte) 1;
        }
        sendData(new byte[]{(byte) 27, (byte) 107, b});
        try {
            if (!StringUtils.isBlank(SysConfig.get("PRINTER.CUT.WAITTIME"))) {
                long lCutWaitTime = (long) Integer.parseInt(SysConfig.get("PRINTER.CUT.WAITTIME"));
                if (lCutWaitTime > 0) {
                    Thread.sleep(lCutWaitTime);
                }
            }
        } catch (Exception e) {
        }
        return true;
    }

    protected boolean printTextImage(String str) {
        Canvas canvas;
        int start = 0;
        String filename = null;
        String resource = null;
        String X = null;
        String Y = null;
        String WIDTH = null;
        String HEIGHT = null;
        String FONTSIZE = null;
        String color = PrinterOptions.COLOR_BLACK;
        String align = PrinterOptions.ALIGN_LEFT;
        int fontSize = 24;
        while (true) {
            int idx = str.indexOf(";", start);
            if (idx == -1) {
                break;
            }
            String s = str.substring(start, idx);
            boolean isKey = false;
            if (s.startsWith("FILE=")) {
                filename = s.substring("FILE=".length());
                isKey = true;
            }
            if (s.startsWith("RESOURCE=")) {
                resource = s.substring("RESOURCE=".length());
                isKey = true;
            }
            if (s.startsWith("X=")) {
                X = s.substring("X=".length());
                isKey = true;
            }
            if (s.startsWith("Y=")) {
                Y = s.substring("Y=".length());
                isKey = true;
            }
            if (s.startsWith("FONTSIZE=")) {
                FONTSIZE = s.substring("FONTSIZE=".length());
                isKey = true;
            }
            if (s.startsWith("COLOR=")) {
                color = s.substring("COLOR=".length());
                isKey = true;
            }
            if (s.startsWith("ALIGN=")) {
                align = s.substring("ALIGN=".length());
                isKey = true;
            }
            if (s.startsWith("WIDTH=")) {
                WIDTH = s.substring("WIDTH=".length());
                isKey = true;
            }
            if (s.startsWith("HEIGHT=")) {
                HEIGHT = s.substring("HEIGHT=".length());
                isKey = true;
            }
            if (!isKey) {
                break;
            }
            start = idx + 1;
        }
        if (start > 0) {
            str = str.substring(start);
        }
        if (!StringUtils.isBlank(FONTSIZE)) {
            fontSize = Integer.parseInt(FONTSIZE);
        }
        int fw = IMAGE_MAX_WIDTH;
        int fh = fontSize + 8;
        int pfw = 0;
        int pfh = 0;
        if (!StringUtils.isBlank(WIDTH)) {
            pfw = Integer.parseInt(WIDTH);
        }
        if (!StringUtils.isBlank(HEIGHT)) {
            pfh = Integer.parseInt(HEIGHT);
        }
        Bitmap bitmap = null;
        if (!(StringUtils.isBlank(filename) && StringUtils.isBlank(resource))) {
            Drawable drawable = null;
            InputStream is = null;
            try {
                if (!StringUtils.isBlank(filename)) {
                    File file = new File(filename);
                    if (file.isFile() && file.length() > 0) {
                        drawable = Drawable.createFromPath(filename);
                    }
                }
                if (drawable == null && !StringUtils.isBlank(resource)) {
                    is = IOUtils.getResourceAsInputStream(resource);
                    drawable = Drawable.createFromStream(is, null);
                }
                if (drawable != null) {
                    fw = drawable.getIntrinsicWidth();
                    fh = drawable.getIntrinsicHeight();
                    if (pfw > 0) {
                        if (pfh == 0) {
                            pfh = (fh * pfw) / fw;
                        }
                        fw = pfw;
                        fh = pfh;
                    }
                    bitmap = Bitmap.createBitmap(fw, fh, Config.ARGB_4444);
                    canvas = new Canvas(bitmap);
                    drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    drawable.draw(canvas);
                }
                IOUtils.close(is);
            } catch (Throwable th) {
                if (is != null) {
                    IOUtils.close(is);
                }
            }
        }
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(fw, fh, Config.ARGB_4444);
            int initColor = -1;
            if (PrinterOptions.COLOR_WHITE.equalsIgnoreCase(color)) {
                initColor = -16777216;
            }
            for (int c = 0; c < fw; c++) {
                for (int r = 0; r < fh; r++) {
                    bitmap.setPixel(c, r, initColor);
                }
            }
        }
        canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setTextSize((float) fontSize);
        if (PrinterOptions.COLOR_WHITE.equalsIgnoreCase(color)) {
            paint.setColor(-1);
        } else {
            paint.setColor(-16777216);
        }
        int x = 0;
        int y = fontSize;
        if (StringUtils.isBlank(X)) {
            int textLen = str.getBytes().length;
            if (PrinterOptions.ALIGN_RIGHT.equalsIgnoreCase(align)) {
                x = fw - ((fontSize * textLen) / 2);
            }
            if (PrinterOptions.ALIGN_CENTER.equalsIgnoreCase(align)) {
                x = (fw - ((fontSize * textLen) / 2)) / 2;
            }
        } else {
            x = Integer.parseInt(X);
        }
        if (!StringUtils.isBlank(Y)) {
            y = Integer.parseInt(Y);
        }
        canvas.drawText(str, (float) x, (float) y, paint);
        if (fw > IMAGE_MAX_WIDTH) {
            int dw = bitmap.getWidth();
            int dh = bitmap.getHeight();
            if (fh == 0) {
                fh = (fw * dh) / dw;
            }
            BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
            Bitmap tBitmap = Bitmap.createBitmap(fw, fh, Config.ARGB_4444);
            Canvas canvas2 = new Canvas(tBitmap);
            bitmapDrawable.setBounds(0, 0, tBitmap.getWidth(), tBitmap.getHeight());
            bitmapDrawable.draw(canvas2);
            bitmap.recycle();
            bitmap = tBitmap;
        }
        List<byte[]> listData = bitmap2printerdata(bitmap);
        bitmap.recycle();
        return printImage(listData);
    }

    protected String execPrintCode(HashMap<String, String> hashMap) {
        return null;
    }

    private String formatText(String text) {
        int lineCharCount = 32 / (((this.XYSize >> 4) & 15) + 1);
        text = StringUtils.replace(text, "\r", "");
        StringBuffer sb;
        List<String> listLine;
        int i;
        String line;
        int len;
        if (PrinterOptions.ALIGN_RIGHT.equalsIgnoreCase(this.align)) {
            sb = new StringBuffer();
            listLine = splitText(text, lineCharCount);
            for (i = 0; i < listLine.size(); i++) {
                line = (String) listLine.get(i);
                if (line.equals(LINE)) {
                    sb.append(LINE);
                } else {
                    len = line.getBytes().length;
                    if (len >= lineCharCount) {
                        sb.append(line);
                    } else {
                        sb.append(BLANK_LINE.substring(0, lineCharCount - len) + line);
                    }
                }
            }
            return sb.toString();
        } else if (!PrinterOptions.ALIGN_CENTER.equalsIgnoreCase(this.align)) {
            return text;
        } else {
            sb = new StringBuffer();
            listLine = splitText(text, lineCharCount);
            for (i = 0; i < listLine.size(); i++) {
                line = (String) listLine.get(i);
                if (line.equals(LINE)) {
                    sb.append(LINE);
                } else {
                    len = line.getBytes().length;
                    if (len >= lineCharCount || len + 2 > lineCharCount) {
                        sb.append(line);
                    } else {
                        sb.append(BLANK_LINE.substring(0, (lineCharCount - len) / 2) + line + BLANK_LINE.substring(0, (lineCharCount - len) / 2));
                    }
                }
            }
            return sb.toString();
        }
    }
}
