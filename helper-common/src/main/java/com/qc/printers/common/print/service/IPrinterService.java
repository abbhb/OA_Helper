package com.qc.printers.common.print.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.print.domain.dto.PrintDeviceDto;
import com.qc.printers.common.print.domain.entity.PrintDocumentTypeStatistic;
import com.qc.printers.common.print.domain.entity.Printer;

import java.util.List;

public interface IPrinterService extends IService<Printer> {
    /**
     * 获取print类别排行榜信息
     *
     * @return
     */
    List<PrintDocumentTypeStatistic> getPrinterTypeStatistics();

    boolean addPrinter(Printer printer, String urlName);

    PrintDeviceDto pollingPrintDevice(String printId);

}
