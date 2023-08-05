package com.qc.printers;

import com.qc.printers.pojo.Printer;
import com.qc.printers.service.PrinterService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@Slf4j
@SpringBootTest
class PrintersApplicationTests {

    @Autowired
    private PrinterService printerService;

    @Test
    void contextLoads() {

        // 保存打印记录
        Printer printer = new Printer();
        printer.setCreateTime(LocalDateTime.now());
        printer.setCreateUser(1659941852221624321L);
        printer.setUrl("359b4ccdd61640cca7aca17e3d86c0be.pdf");
        printer.setIsDuplex(1);
        printer.setCopies(1);
        printer.setNeedPrintPagesEndIndex(3);
        printer.setNeedPrintPagesIndex(1);
        printer.setName("F题_基于声传播的智能定位系统.pdf");
        printer.setSingleDocumentPaperUsage(2);
        log.info("printer={}", printer);
        boolean save = printerService.save(printer);
    }

}
