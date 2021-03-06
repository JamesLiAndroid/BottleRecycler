package com.incomrecycle.prms.rvm.service.comm;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.prms.rvm.common.SysDef.TrafficType;
import com.incomrecycle.prms.rvm.service.comm.entity.BarCodeLineCommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.DigitalScreenDWT48270T050_18WTCommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.HuiLifeReaderCommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.MagneticStripeCardCommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.PLC103PCommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.PrinterRD_W32CommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.RCCCommEntity;
import com.incomrecycle.prms.rvm.service.comm.entity.Weigh103PCommEntity;
import com.incomrecycle.prms.rvm.service.comm.executor.LinkTestingCommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.OneCardReadCommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.PLC103PCommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.PrinterPrintCommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.RCCSendCommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.RecyclePaper.PLC103PRecyclePaperCommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.TrafficCardCommCmdExecutor;
import com.incomrecycle.prms.rvm.service.comm.executor.YC103PCheckCommCmdExecutor;

import java.util.ArrayList;
import java.util.List;

public class YC103POLDCommService extends AbstractCommService {
    public YC103POLDCommService() {
        String[] HARDWARE_LIST = SysConfig.get("HARDWARE.LIST." + SysConfig.get("RVM.MODE")).split(";");
        List<String> list = new ArrayList();
        for (String add : HARDWARE_LIST) {
            list.add(add);
        }
        if (list.contains("Printer1")) {
            setCommonEntity("Printer1", new PrinterRD_W32CommEntity(1));
        }
        if (list.contains("Printer2")) {
            setCommonEntity("Printer2", new PrinterRD_W32CommEntity(2));
        }
        if (list.contains("BarCode")) {
            setCommonEntity("BarCode", new BarCodeLineCommEntity(1));
        }
        if (list.contains("PLC")) {
            setCommonEntity("PLC", new PLC103PCommEntity());
        }
        if (list.contains("Weigh")) {
            setCommonEntity("Weigh", new Weigh103PCommEntity());
        }
        if (list.contains("DigitalScreen")) {
            setCommonEntity("DigitalScreen", new DigitalScreenDWT48270T050_18WTCommEntity());
        }
        if (list.contains(TrafficType.RCC)) {
            setCommonEntity(TrafficType.RCC, new RCCCommEntity());
        }
//        if (list.contains("OneCardReader")) {
//            setCommonEntity("OneCardReader", new OneCardReaderCommEntity());
//            setCommonEntity("TrafficCardModel", new TrafficCardCommEntity());
//        }
        if (list.contains("MagneticStripeCard")) {
            setCommonEntity("MagneticStripeCard", new MagneticStripeCardCommEntity());
        }
        if (list.contains("HuiLifeCard")) {
            setCommonEntity("HuiLifeCard", new HuiLifeReaderCommEntity());
        }
        addCommonCmdExecutor(new PLC103PCommCmdExecutor());
        addCommonCmdExecutor(new PrinterPrintCommCmdExecutor());
        addCommonCmdExecutor(new PLC103PRecyclePaperCommCmdExecutor());
        addCommonCmdExecutor(new RCCSendCommCmdExecutor());
        addCommonCmdExecutor(new LinkTestingCommCmdExecutor());
        if (list.contains("OneCardReader")) {
            addCommonCmdExecutor(new OneCardReadCommCmdExecutor());
            addCommonCmdExecutor(new TrafficCardCommCmdExecutor());
        }
        addCommonCmdExecutor(new YC103PCheckCommCmdExecutor());
    }
}
