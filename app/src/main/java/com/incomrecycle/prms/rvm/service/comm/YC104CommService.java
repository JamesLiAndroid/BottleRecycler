package com.incomrecycle.prms.rvm.service.comm;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.prms.rvm.common.SysDef.TrafficType;
import com.incomrecycle.prms.rvm.service.comm.entity.BarCodeLineCommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.PLC104CommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.PrinterRD_W32CommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.RCCCommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.TrafficCardCommEntity;
import com.incomrecycle.prms.rvm.service.comm.executor.LinkTestingCommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.OneCardReadCommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.PLC104CommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.PrinterPrintCommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.RCCSendCommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.TrafficCardCommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.YC104CheckCommCmdExecutor;
import java.util.ArrayList;
import java.util.List;

public class YC104CommService extends AbstractCommService {
    public YC104CommService() {
        String[] HARDWARE_LIST = SysConfig.get("HARDWARE.LIST." + SysConfig.get("RVM.MODE")).split(";");
        List<String> list = new ArrayList();
        for (String add : HARDWARE_LIST) {
            list.add(add);
        }
        if (list.contains("Printer1")) {
            setCommonEntity("Printer1", new PrinterRD_W32CommEntity(1));
        }
        if (list.contains("BarCode")) {
            setCommonEntity("BarCode", new BarCodeLineCommEntity(1));
        }
        if (list.contains("PLC")) {
            setCommonEntity("PLC", new PLC104CommEntity());
        }
        if (list.contains(TrafficType.RCC)) {
            setCommonEntity(TrafficType.RCC, new RCCCommEntity());
        }
//        if (list.contains("OneCardReader")) {
//            setCommonEntity("OneCardReader", new OneCardReaderCommEntity());
//            setCommonEntity("TrafficCardModel", new TrafficCardCommEntity());
//        }
        addCommonCmdExecutor(new PLC104CommCmdExecutor());
        addCommonCmdExecutor(new PrinterPrintCommCmdExecutor());
        addCommonCmdExecutor(new RCCSendCommCmdExecutor());
        addCommonCmdExecutor(new LinkTestingCommCmdExecutor());
        if (list.contains("OneCardReader")) {
            addCommonCmdExecutor(new OneCardReadCommCmdExecutor());
            addCommonCmdExecutor(new TrafficCardCommCmdExecutor());
        }
        addCommonCmdExecutor(new YC104CheckCommCmdExecutor());
    }
}
