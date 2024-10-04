package com.qc.printers.common.common.event.listener.print;

import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.event.print.FileToPDFSuccessEvent;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.common.print.domain.entity.Printer;
import com.qc.printers.common.print.service.IPrinterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class FileToPDFSuccessListener {
    @Autowired
    private IPrinterService iPrinterService;



    @Transactional
    @TransactionalEventListener(classes = FileToPDFSuccessEvent.class, fallbackExecution = true)
    public void pdfToMysql(FileToPDFSuccessEvent event) {
        Long printId = event.getPrintId();
        Printer printer = iPrinterService.getById(printId);
        PrinterRedis printerRedis = RedisUtils.get(MyString.print + printId, PrinterRedis.class);
        printer.setPdfUrl(OssDBUtil.toDBUrl(printerRedis.getPdfUrl()));
        iPrinterService.updateById(printer);
    }
}
