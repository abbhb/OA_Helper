package com.qc.printers.common.common.event.listener.print;

import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.event.print.PrintErrorEvent;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.common.print.domain.entity.Printer;
import com.qc.printers.common.print.service.IPrinterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.TimeUnit;

/**
 * 打印失败的处理监听
 */
@Slf4j
@Component
public class PrintErrorListener {
    @Autowired
    private IPrinterService iPrinterService;

    @Async
    @TransactionalEventListener(classes = PrintErrorEvent.class, fallbackExecution = true)
    public void error(PrintErrorEvent event) {
        Long printId = event.getId();
        Printer printer = iPrinterService.getById(printId);
        PrinterRedis printerRedis = RedisUtils.get(MyString.print + printId, PrinterRedis.class);
        printerRedis.setIsPrint(0);
        printerRedis.setSTU(0);
        printer.setIsPrint(0);
        iPrinterService.updateById(printer);
        RedisUtils.set(MyString.print + printId, printerRedis, 100L, TimeUnit.SECONDS);

    }
}
