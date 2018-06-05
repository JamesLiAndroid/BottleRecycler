package com.incomrecycle.prms.rvm.service.comm.entity;

import android.support.v4.view.MotionEventCompat;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.StringUtils;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class BasePrinter {
    private static final String FORMAT_MARK = "[FORMAT]";
    private static final PrinterCmdExecutor PrinterCmdExecutorNULL = new PrinterCmdExecutor() {
        public boolean execute(String str, HashMap<String, String> hashMap) {
            return true;
        }
    };
    private static final String[][] convert;
    private static final Logger logger = LoggerFactory.getLogger("PrinterCommEntity");
    private String charset = null;
    private HashMap<String, PrinterCmdExecutor> hsmpPrinterCmdExecutor = new HashMap();
    private final PrinterCmdExecutor printerFormatExecutor = new PrinterCmdExecutor() {
        public boolean execute(String str, HashMap<String, String> hsmpParam) {
            return BasePrinter.this.formatPrint(str, hsmpParam);
        }
    };
    private final PrinterCmdExecutor printerNormalExecutor = new PrinterCmdExecutor() {
        public boolean execute(String str, HashMap<String, String> hsmpParam) {
            return BasePrinter.this.normalPrint(str, hsmpParam);
        }
    };

    private static class PrinterCmd {
        private static final String BARCODE = "[BARCODE]";
        private static final String CODE = "[CODE]";
        private static final String CUT = "[CUT]";
        private static final String DATA = "[DATA]";
        private static final String IMAGE = "[IMAGE]";
        private static final String LINE = "[LINE]";
        private static final String MODEL = "[MODEL]";
        private static final String QRCODE = "[QRCODE]";
        private static final String RESET = "[RESET]";
        private static final String SETTINGS = "[SETTINGS]";
        private static final String TEXT = "[TEXT]";
        private static final String TEXTIMAGE = "[TEXTIMAGE]";

        private PrinterCmd() {
        }
    }

    public interface PrinterCmdExecutor {
        boolean execute(String str, HashMap<String, String> hashMap);
    }

    protected static class PrinterOptions {
        public static final String ALIGN_CENTER = "CENTER";
        public static final String ALIGN_LEFT = "LEFT";
        public static final String ALIGN_RIGHT = "RIGHT";
        public static final String COLOR_BLACK = "BLACK";
        public static final String COLOR_WHITE = "WHITE";
        public static final String CUT_ALL = "ALL";
        public static final String CUT_HALF = "HALF";
        public static final String SETTINGS_ALIGN = "ALIGN";
        public static final String SETTINGS_CHARSET = "CHARSET";
        public static final String SETTINGS_COLOR = "COLOR";
        public static final String SETTINGS_HIGHSIZE = "HIGHSIZE";
        public static final String SETTINGS_STRONG = "STRONG";
        public static final String SETTINGS_UNDERLINE = "UNDERLINE";
        public static final String SETTINGS_WIDESIZE = "WIDESIZE";

        protected PrinterOptions() {
        }
    }

    protected abstract boolean cutPaper(String str);

    protected abstract String execPrintCode(HashMap<String, String> hashMap);

    protected abstract boolean printBarcode(String str, String str2);

    protected abstract boolean printData(byte[] bArr, int i, int i2);

    protected abstract boolean printImage(String str, String str2, String str3, String str4);

    protected abstract boolean printLine();

    protected abstract boolean printQRCode(String str, String str2);

    protected abstract boolean printText(String str);

    protected abstract boolean printTextImage(String str);

    protected abstract boolean reset();

    protected abstract boolean setSettings(HashMap<String, String> hashMap);

    static {
        convert = new String[4][];
        convert[0] = new String[]{"\\\\", "\\"};
        convert[1] = new String[]{"\\r", "\r"};
        convert[2] = new String[]{"\\n", "\n"};
        convert[3] = new String[]{"\\t", "\t"};
        // convert = r0;
    }

    public BasePrinter() {
        addPrinterCmdExecutor(FORMAT_MARK, PrinterCmdExecutorNULL);
        addPrinterCmdExecutor("[MODEL]", new PrinterCmdExecutor() {
            public boolean execute(String str, HashMap<String, String> hsmpParam) {
                return BasePrinter.this.cmdModel(str, hsmpParam);
            }
        });
        addPrinterCmdExecutor("[RESET]", new PrinterCmdExecutor() {
            public boolean execute(String str, HashMap<String, String> hashMap) {
                return BasePrinter.this.cmdReset(str);
            }
        });
        addPrinterCmdExecutor("[SETTINGS]", new PrinterCmdExecutor() {
            public boolean execute(String str, HashMap<String, String> hashMap) {
                return BasePrinter.this.cmdSetting(str);
            }
        });
        addPrinterCmdExecutor("[BARCODE]", new PrinterCmdExecutor() {
            public boolean execute(String str, HashMap<String, String> hashMap) {
                return BasePrinter.this.cmdBarcode(str);
            }
        });
        addPrinterCmdExecutor("[QRCODE]", new PrinterCmdExecutor() {
            public boolean execute(String str, HashMap<String, String> hashMap) {
                return BasePrinter.this.cmdQRCode(str);
            }
        });
        addPrinterCmdExecutor("[LINE]", new PrinterCmdExecutor() {
            public boolean execute(String str, HashMap<String, String> hashMap) {
                return BasePrinter.this.cmdLine(str);
            }
        });
        addPrinterCmdExecutor("[IMAGE]", new PrinterCmdExecutor() {
            public boolean execute(String str, HashMap<String, String> hashMap) {
                return BasePrinter.this.cmdImage(str);
            }
        });
        addPrinterCmdExecutor("[TEXTIMAGE]", new PrinterCmdExecutor() {
            public boolean execute(String str, HashMap<String, String> hashMap) {
                return BasePrinter.this.cmdTextImage(str);
            }
        });
        addPrinterCmdExecutor("[CODE]", new PrinterCmdExecutor() {
            public boolean execute(String str, HashMap<String, String> hashMap) {
                return BasePrinter.this.cmdCode(str);
            }
        });
        addPrinterCmdExecutor("[TEXT]", new PrinterCmdExecutor() {
            public boolean execute(String str, HashMap<String, String> hashMap) {
                return BasePrinter.this.cmdText(str);
            }
        });
        addPrinterCmdExecutor("[DATA]", new PrinterCmdExecutor() {
            public boolean execute(String str, HashMap<String, String> hashMap) {
                return BasePrinter.this.cmdData(str);
            }
        });
        addPrinterCmdExecutor("[CUT]", new PrinterCmdExecutor() {
            public boolean execute(String str, HashMap<String, String> hashMap) {
                return BasePrinter.this.cmdCut(str);
            }
        });
    }

    protected void addPrinterCmdExecutor(String cmd, PrinterCmdExecutor printerCmdExecutor) {
        this.hsmpPrinterCmdExecutor.put(cmd, printerCmdExecutor);
    }

    private String previewCmd(String cmd, String str, HashMap<String, String> hsmpParam) {
        if (cmd.equals("[TEXT]") || cmd.equals("[BARCODE]")) {
            return str;
        }
        if (cmd.equals("[LINE]")) {
            return "\n";
        }
        if (cmd.equals("[MODEL]") && !StringUtils.isBlank(str)) {
            HashMap<String, String> hsmpValue = StringUtils.toHashMap(str, ";", "=");
            String filename = (String) hsmpValue.get("FILE");
            String resource = (String) hsmpValue.get("RESOURCE");
            if (!(filename == null && resource == null)) {
                byte[] data = readFile(filename, resource);
                if (data == null) {
                    return "";
                }
                return preview(new String(data), hsmpParam);
            }
        }
        return "";
    }

    public String preview(String str, HashMap<String, String> hsmpParam) {
        StringBuffer sb = new StringBuffer();
        if (StringUtils.isBlank(str)) {
            return sb.toString();
        }
        String cmd;
        if (hsmpParam != null && hsmpParam.size() > 0) {
            String[][] convert = new String[hsmpParam.size()][];
            int count = 0;
            for (String key : hsmpParam.keySet()) {
                String value = (String) hsmpParam.get(key);
                convert[count] = new String[]{ key , value};
                count++;
            }
            str = StringUtils.convert(str, convert);
        }
        str = StringUtils.replace(StringUtils.replace(str, "\r\n", "\n"), "\r", "\n");
        int strLen = str.length();
        List<Integer> listPStart = new ArrayList();
        List<String> listPCmd = new ArrayList();
        int pos = 0;
        while (pos < strLen) {
            int idx = str.indexOf(91, pos);
            if (idx != -1) {
                int nextIdx = str.indexOf(91, idx + 1);
                int endIdx = str.indexOf(93, idx + 1);
                if (endIdx == -1) {
                    break;
                } else if (nextIdx == -1 || endIdx < nextIdx) {
                    cmd = str.substring(idx, endIdx + 1);
                    if (((PrinterCmdExecutor) this.hsmpPrinterCmdExecutor.get(cmd)) != null) {
                        listPStart.add(Integer.valueOf(idx));
                        listPCmd.add(cmd);
                    }
                    pos = endIdx + 1;
                } else {
                    pos = nextIdx;
                }
            } else {
                break;
            }
        }
        if (listPStart.size() > 0) {
            for (int i = 0; i < listPStart.size(); i++) {
                String text;
                int start = ((Integer) listPStart.get(i)).intValue();
                cmd = (String) listPCmd.get(i);
                if (i + 1 < listPStart.size()) {
                    text = str.substring(start + cmd.length(), ((Integer) listPStart.get(i + 1)).intValue());
                } else {
                    text = str.substring(cmd.length() + start);
                }
                if (text.endsWith("\n")) {
                    text = text.substring(0, text.length() - 1);
                }
                sb.append(previewCmd(cmd, StringUtils.convert(text, convert), hsmpParam));
            }
        }
        return sb.toString();
    }

    public boolean print(String str, HashMap<String, String> hsmpParam) {
        if (StringUtils.isBlank(str)) {
            return true;
        }
        int idx = str.indexOf(FORMAT_MARK);
        if (idx == -1 || str.substring(0, idx).trim().length() != 0) {
            return execPrinterCmdExecutor(this.printerNormalExecutor, str, hsmpParam);
        }
        return execPrinterCmdExecutor(this.printerFormatExecutor, str.substring(FORMAT_MARK.length() + idx), hsmpParam);
    }

    protected boolean execPrinterCmdExecutor(PrinterCmdExecutor printerCmdExecutor, String str, HashMap<String, String> hsmpParam) {
        return printerCmdExecutor.execute(str, hsmpParam);
    }

    protected boolean normalPrint(String str, HashMap<String, String> hsmpParam) {
        return formatPrint("[FORMAT][RESET][TEXT]" + str, hsmpParam);
    }

    private boolean formatPrint(String str, HashMap<String, String> hsmpParam) {
        if (StringUtils.isBlank(str)) {
            return true;
        }
        String cmd;
        if (hsmpParam != null && hsmpParam.size() > 0) {
            String[][] convert = new String[hsmpParam.size()][];
            int count = 0;
            for (String key : hsmpParam.keySet()) {
                String value = (String) hsmpParam.get(key);
                convert[count] = new String[]{key, value};
                count++;
            }
            str = StringUtils.convert(str, convert);
        }
        str = StringUtils.replace(StringUtils.replace(str, "\r\n", "\n"), "\r", "\n");
        int strLen = str.length();
        List<Integer> listPStart = new ArrayList();
        List<String> listPCmd = new ArrayList();
        int pos = 0;
        while (pos < strLen) {
            int idx = str.indexOf(91, pos);
            if (idx != -1) {
                int nextIdx = str.indexOf(91, idx + 1);
                int endIdx = str.indexOf(93, idx + 1);
                if (endIdx == -1) {
                    break;
                } else if (nextIdx == -1 || endIdx < nextIdx) {
                    cmd = str.substring(idx, endIdx + 1);
                    if (((PrinterCmdExecutor) this.hsmpPrinterCmdExecutor.get(cmd)) != null) {
                        listPStart.add(Integer.valueOf(idx));
                        listPCmd.add(cmd);
                    }
                    pos = endIdx + 1;
                } else {
                    pos = nextIdx;
                }
            } else {
                break;
            }
        }
        if (listPStart.size() <= 0) {
            return execPrinterCmdExecutor((PrinterCmdExecutor) this.hsmpPrinterCmdExecutor.get("[TEXT]"), str, hsmpParam);
        }
        for (int i = 0; i < listPStart.size(); i++) {
            String text;
            int start = ((Integer) listPStart.get(i)).intValue();
            cmd = (String) listPCmd.get(i);
            PrinterCmdExecutor printerCmdExecutor = (PrinterCmdExecutor) this.hsmpPrinterCmdExecutor.get(cmd);
            if (i + 1 < listPStart.size()) {
                text = str.substring(start + cmd.length(), ((Integer) listPStart.get(i + 1)).intValue());
            } else {
                text = str.substring(cmd.length() + start);
            }
            if (text.endsWith("\n")) {
                text = text.substring(0, text.length() - 1);
            }
            text = StringUtils.convert(text, convert);
            logger.debug(printerCmdExecutor.getClass().getName() + ":[" + text + "]");
            if (!execPrinterCmdExecutor(printerCmdExecutor, text, hsmpParam)) {
                return false;
            }
        }
        return true;
    }

    private boolean cmdModel(String str, HashMap<String, String> hsmpParam) {
        if (StringUtils.isBlank(str)) {
            return true;
        }
        HashMap<String, String> hsmpValue = StringUtils.toHashMap(str, ";", "=");
        String filename = (String) hsmpValue.get("FILE");
        String resource = (String) hsmpValue.get("RESOURCE");
        if (filename == null && resource == null) {
            return true;
        }
        byte[] data = readFile(filename, resource);
        if (data == null) {
            return true;
        }
        try {
            return formatPrint(new String(data, "UTF-8"), hsmpParam);
        } catch (UnsupportedEncodingException e) {
            return formatPrint(new String(data), hsmpParam);
        }
    }

    private byte[] readFile(String filename, String resource) {
        byte[] data = null;
        if (!StringUtils.isBlank(filename)) {
            File file = new File(filename);
            if (file.isFile() && file.length() > 0) {
                data = IOUtils.readFile(filename);
                if (data != null && data.length == 0) {
                    data = null;
                }
            }
        }
        if (data != null || StringUtils.isBlank(resource)) {
            return data;
        }
        data = IOUtils.readResource(resource);
        if (data == null || data.length != 0) {
            return data;
        }
        return null;
    }

    private boolean cmdReset(String str) {
        if (!reset()) {
            return false;
        }
        if (StringUtils.isBlank(this.charset)) {
            return true;
        }
        HashMap<String, String> hsmpSettings = new HashMap();
        hsmpSettings.put("SETTINGS_CHARSET", this.charset);
        return setSettings(hsmpSettings);
    }

    private boolean cmdSetting(String str) {
        if (StringUtils.isBlank(str)) {
            return true;
        }
        HashMap<String, String> hsmpSettings = StringUtils.toHashMap(str, ";", "=");
        if (hsmpSettings.size() <= 0) {
            return true;
        }
        String CHARSET = (String) hsmpSettings.get("SETTINGS_CHARSET");
        if (!StringUtils.isBlank(CHARSET)) {
            this.charset = CHARSET;
        }
        return setSettings(hsmpSettings);
    }

    private boolean cmdBarcode(String str) {
        if (StringUtils.isBlank(str)) {
            return true;
        }
        int idx = str.indexOf(58);
        if (idx <= 0) {
            return true;
        }
        String mode = str.substring(0, idx);
        return printBarcode(mode.trim(), str.substring(idx + 1).trim());
    }

    private boolean cmdQRCode(String str) {
        if (StringUtils.isBlank(str)) {
            return true;
        }
        int idx = str.indexOf(58);
        if (idx <= 0) {
            return true;
        }
        String mode = str.substring(0, idx);
        return printQRCode(mode.trim(), str.substring(idx + 1));
    }

    private boolean cmdLine(String str) {
        return printLine();
    }

    private boolean cmdImage(String str) {
        if (StringUtils.isBlank(str)) {
            return true;
        }
        HashMap<String, String> hsmpValue = StringUtils.toHashMap(str, ";", "=");
        String filename = (String) hsmpValue.get("FILE");
        String resource = (String) hsmpValue.get("RESOURCE");
        String width = (String) hsmpValue.get("WIDTH");
        String height = (String) hsmpValue.get("HEIGHT");
        if (filename == null && resource == null) {
            return true;
        }
        return printImage(filename, resource, width, height);
    }

    private boolean cmdTextImage(String str) {
        if (StringUtils.isBlank(str)) {
            return true;
        }
        return printTextImage(str);
    }

    private boolean cmdCode(String str) {
        if (StringUtils.isBlank(str)) {
            return true;
        }
        return formatPrint(execPrintCode(StringUtils.toHashMap(str, ";", "=")), null);
    }

    private boolean cmdText(String str) {
        if (StringUtils.isEmpty(str)) {
            return true;
        }
        return printText(str);
    }

    private boolean cmdData(String str) {
        if (StringUtils.isBlank(str)) {
            return true;
        }
        HashMap<String, String> hsmpValue = StringUtils.toHashMap(str, ";", "=");
        if (hsmpValue.size() == 0) {
            return true;
        }
        byte[] data;
        String RADIX = (String) hsmpValue.get("RADIX");
        String DIV = (String) hsmpValue.get("DIV");
        if (DIV == null) {
            DIV = " ";
        } else if (DIV.toUpperCase().indexOf("SPACE") != -1) {
            DIV = " ";
        } else if (DIV.length() != 1) {
            DIV = " ";
        }
        int radix = 16;
        if (!StringUtils.isBlank(RADIX)) {
            radix = Integer.parseInt(RADIX);
        }
        String filename = (String) hsmpValue.get("FILE");
        String resource = (String) hsmpValue.get("RESOURCE");
        String value = null;
        if (!(filename == null && resource == null)) {
            data = readFile(filename, resource);
            if (data != null) {
                value = StringUtils.replace(StringUtils.replace(StringUtils.replace(new String(data), "\n", DIV), "\r", DIV), "\t", DIV);
            }
        }
        if (value == null) {
            value = (String) hsmpValue.get("DATA");
        }
        if (value != null) {
            String[] items = value.split(DIV);
            data = new byte[items.length];
            int dataLen = 0;
            for (int i = 0; i < items.length; i++) {
                items[i] = items[i].trim();
                if (items[i].length() != 0) {
                    data[dataLen] = (byte) (Integer.valueOf(items[i], radix).intValue() & MotionEventCompat.ACTION_MASK);
                    dataLen++;
                }
            }
            if (dataLen > 0) {
                return printData(data, 0, dataLen);
            }
        }
        return true;
    }

    private boolean cmdCut(String str) {
        if (StringUtils.isBlank(str)) {
            return cutPaper(null);
        }
        return cutPaper(str.trim());
    }
}
