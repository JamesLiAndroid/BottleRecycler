package com.incomrecycle.prms.rvm.service.comm.executor;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.prms.rvm.service.comm.CommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.CommService;

public class PrinterPrintCommCmdExecutor implements CommCmdExecutor {
    private static final String[] cmdSet = new String[]{"PRINTER_PRINT", "PRINTER_PREVIEW", "PRINTER_CHECK"};

    public String execute(String cmd, String json) throws Exception {
        boolean hasPrinter1;
        boolean hasPrinter2;
        String Printer1 = "Printer1";
        String Printer2 = "Printer2";
        if ("TRUE".equalsIgnoreCase(SysConfig.get("PRINTER.EXCHANGE"))) {
            Printer1 = "Printer2";
            Printer2 = "Printer1";
        }
        if (CommService.getCommService().getCommEntity(Printer1) != null) {
            hasPrinter1 = true;
        } else {
            hasPrinter1 = false;
        }
        if (CommService.getCommService().getCommEntity(Printer2) != null) {
            hasPrinter2 = true;
        } else {
            hasPrinter2 = false;
        }
        String setPrintEnable = "TRUE";
        setPrintEnable = SysConfig.get("SET.PRINT.ENABLE");
        if ("PRINTER_CHECK".equalsIgnoreCase(cmd) && "TRUE".equalsIgnoreCase(setPrintEnable)) {
            String printer1State = null;
            String printer2State = null;
            if (hasPrinter1) {
                printer1State = CommService.getCommService().getCommEntity(Printer1).execute("state", json);
            }
            if (hasPrinter2) {
                printer2State = CommService.getCommService().getCommEntity(Printer2).execute("state", json);
            }
            if ("havePaper".equalsIgnoreCase(printer1State) || "havePaper".equalsIgnoreCase(printer2State)) {
                return "havePaper";
            }
            if ("noPaper".equalsIgnoreCase(printer1State) || "noPaper".equalsIgnoreCase(printer2State)) {
                return "noPaper";
            }
            return "unknown";
        } else if ("PRINTER_PREVIEW".equalsIgnoreCase(cmd) && "TRUE".equalsIgnoreCase(setPrintEnable)) {
            if (hasPrinter1) {
                return CommService.getCommService().getCommEntity(Printer1).execute("preview", json);
            }
            return hasPrinter2 ? CommService.getCommService().getCommEntity(Printer2).execute("preview", json) : null;
        } else if (!"PRINTER_PRINT".equalsIgnoreCase(cmd) || !"TRUE".equalsIgnoreCase(setPrintEnable)) {
            return null;
        } else {
            String PrinterId = null;
            if (null == null && hasPrinter1 && "TRUE".equalsIgnoreCase(CommService.getCommService().getCommEntity(Printer1).execute("enable", null))) {
                PrinterId = Printer1;
            }
            if (PrinterId == null && hasPrinter2 && "TRUE".equalsIgnoreCase(CommService.getCommService().getCommEntity(Printer2).execute("enable", null))) {
                PrinterId = Printer2;
            }
            if (PrinterId == null) {
                return null;
            }
            CommService.getCommService().getCommEntity(PrinterId).execute("reset", json);
            CommService.getCommService().getCommEntity(PrinterId).execute("init", json);
            CommService.getCommService().getCommEntity(PrinterId).execute("printer", json);
            CommService.getCommService().getCommEntity(PrinterId).execute("cut", json);
            return null;
        }
    }

    public String[] getCmdSet() {
        return cmdSet;
    }
}
