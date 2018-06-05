package com.incomrecycle.prms.rvm.service.printersvr;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.json.JSONUtils;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.ShellUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.service.comm.CommService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AppPrinterService {

    private static class PrinterThread extends Thread {
        private HashMap hsmpPrintParam;
        private Socket s;

        public PrinterThread(Socket s, HashMap hsmpPrintParam) {
            this.s = s;
            this.hsmpPrintParam = hsmpPrintParam;
        }

        public void run() {
            try {
                int i;
                InputStream is = this.s.getInputStream();
                byte[] data = new byte[1024];
                boolean isFindEnd = false;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (true) {
                    int readLen = is.read(data);
                    if (readLen <= 0) {
                        break;
                    }
                    for (i = 0; i < readLen; i++) {
                        if (data[i] == (byte) 0) {
                            baos.write(data, 0, i);
                            isFindEnd = true;
                            break;
                        }
                    }
                    if (!isFindEnd) {
                        baos.write(data, 0, readLen);
                    }
                }
                if ("print".equalsIgnoreCase(new String(baos.toByteArray(), "GBK"))) {
                    String n = "00000" + ((System.currentTimeMillis() / 1000) % 86400);
                    String date = DateUtils.formatDatetime(new Date(), "yyyyMMdd");
                    String TEXT_FILE = (String) this.hsmpPrintParam.get("TEXT_FILE");
                    String NO_FILE = (String) this.hsmpPrintParam.get("NO_FILE");
                    String USED_NO_FILE = NO_FILE + ".used";
                    if (!StringUtils.isBlank(TEXT_FILE)) {
                        String str;
                        String[] sa;
                        String pt = "";
                        if (new File(TEXT_FILE).isFile()) {
                            str = new String(IOUtils.readFile(TEXT_FILE), "GBK");
                        }
                        List<String> listNo = new ArrayList();
                        List<String> listUsedNo = new ArrayList();
                        HashMap<String, String> hsmpUsedNo = new HashMap();
                        String noText = null;
                        String UsedNoText = null;
                        if (new File(NO_FILE).isFile()) {
                            str = new String(IOUtils.readFile(NO_FILE));
                        }
                        if (new File(USED_NO_FILE).isFile()) {
                            UsedNoText = new String(IOUtils.readFile(USED_NO_FILE));
                        }
                        if (!StringUtils.isBlank(noText)) {
                            sa = noText.split("\n");
                            for (i = 0; i < sa.length; i++) {
                                sa[i] = sa[i].trim();
                                if (sa[i].length() != 0) {
                                    listNo.add(sa[i]);
                                }
                            }
                        }
                        if (!StringUtils.isBlank(UsedNoText)) {
                            sa = UsedNoText.split("\n");
                            for (i = 0; i < sa.length; i++) {
                                sa[i] = sa[i].trim();
                                if (sa[i].length() != 0) {
                                    listUsedNo.add(sa[i]);
                                    hsmpUsedNo.put(sa[i], sa[i]);
                                }
                            }
                        }
                        String no = "";
                        for (i = 0; i < listNo.size(); i++) {
                            if (hsmpUsedNo.get(listNo.get(i)) == null) {
                                no = (String) listNo.get(i);
                                break;
                            }
                        }
                        pt = StringUtils.replace(StringUtils.replace(pt, "$DATE$", date), "$NO$", no);
                        if (pt.indexOf("[FORMAT]") == -1) {
                            pt = StringUtils.replace("[FORMAT][RESET][SETTINGS]CHARSET=$CHARSET$;[TEXT]", "$CHARSET$", "GBK") + pt + "[RESET][TEXT]\\n\\n\\n\\n[CUT]HALF[TEXT]\\n";
                        }
                        HashMap<String, String> hsmpReplace = new HashMap();
                        hsmpReplace.put("$TERM_CODE$", SysConfig.get("RVM.CODE"));
                        hsmpReplace.put("$CHARSET$", "GBK");
                        hsmpReplace.put("$n$", "\\n");
                        HashMap hsmpCmdParam = new HashMap();
                        if (pt.indexOf("$LOGO$") != -1) {
                            hsmpReplace.put("$LOGO$", StringUtils.replace(StringUtils.replace(StringUtils.replace("[DATA]RADIX=16;DIV=SPACE;FILE=$LOGO.FILE$;RESOURCE=$LOGO.FILE.RESOURCE$;[SETTINGS]CHARSET=$CHARSET$;[TEXT]", "$LOGO.FILE$", SysConfig.get("LOGO.FILE")), "$LOGO.FILE.RESOURCE$", SysConfig.get("LOGO.FILE.RESOURCE")), "$CHARSET$", "GBK"));
                        }
                        hsmpCmdParam.put("MODEL", pt);
                        hsmpCmdParam.put("PARAM", hsmpReplace);
                        CommService.getCommService().execute("PRINTER_PRINT", JSONUtils.toJSON(hsmpCmdParam));
                        if (!(StringUtils.isBlank(no) || StringUtils.isBlank(USED_NO_FILE))) {
                            listUsedNo.add(no);
                            ShellUtils.shell("echo \"" + no + "\" >> " + USED_NO_FILE);
                        }
                    }
                }
                try {
                    this.s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                try {
                    this.s.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            } catch (Throwable th) {
                try {
                    this.s.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
                throw th;
            }
        }
    }

    public static void startPrinterService(int port, String param) {
        SysGlobal.execute(new Thread() {
            HashMap hsmpPrintParam = null;
            int port;

            public Thread setPort(int port, String param) {
                this.port = port;
                try {
                    this.hsmpPrintParam = JSONUtils.toHashMap(param);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return this;
            }

            public void run() {
                IOException e;
                Throwable th;
                StrictMode.setThreadPolicy(new Builder().permitAll().build());
                ServerSocket serverSocket = null;
                try {
                    ServerSocket ss = new ServerSocket(this.port);
                    while (true) {
                        try {
                            Socket s = ss.accept();
                            if (s == null) {
                                break;
                            }
                            SysGlobal.execute(new PrinterThread(s, this.hsmpPrintParam));
                        } catch (IOException e2) {
                            e = e2;
                            serverSocket = ss;
                        } catch (Throwable th2) {
                            th = th2;
                            serverSocket = ss;
                        }
                    }
                    if (ss != null) {
                        try {
                            ss.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                            serverSocket = ss;
                        }
                    }
                } catch (IOException e4) {
                    try {
                        e4.printStackTrace();
                        if (serverSocket != null) {
                            try {
                                serverSocket.close();
                            } catch (IOException e32) {
                                e32.printStackTrace();
                            }
                        }
                    } catch (Throwable th3) {
                        if (serverSocket != null) {
                            try {
                                serverSocket.close();
                            } catch (IOException e322) {
                                e322.printStackTrace();
                            }
                        }
                        throw th3;
                    }
                }
            }
        }.setPort(port, param));
    }
}
