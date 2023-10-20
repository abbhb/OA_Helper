package com.qc.printers.common.common.event.listener.print;

import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.constant.MQConstant;
import com.qc.printers.common.common.event.print.PDFToImageEvent;
import com.qc.printers.common.common.utils.MinIoUtil;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.common.print.domain.entity.Printer;
import com.qc.printers.common.print.domain.vo.request.data.PrintDataPDFToImageReq;
import com.qc.printers.common.print.service.IPrinterService;
import com.qc.printers.transaction.service.MQProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * pdf转Image监听器
 */
@Slf4j
@Component
public class PDFToImageListener {
    @Autowired
    private IPrinterService iPrinterService;

    @Autowired
    private MinIoUtil minIoUtil;

    @Autowired
    private MQProducer mqProducer;

    @Async
    @TransactionalEventListener(classes = PDFToImageEvent.class, fallbackExecution = true)
    public void pdfToImage(PDFToImageEvent event) {
        Long printId = event.getPrintId();
        Printer printer = iPrinterService.getById(printId);
        PrinterRedis printerRedis = RedisUtils.get(MyString.print + printId, PrinterRedis.class);
        PrintDataPDFToImageReq printDataPDFToImageReq = new PrintDataPDFToImageReq(String.valueOf(printer.getId()), printerRedis.getPdfUrl(), printerRedis.getImageDownloadUrl(), printerRedis.getImageUploadUrl());
        mqProducer.sendMessageWithTags(MQConstant.SEND_PDF_IMAGE_TOPIC, printDataPDFToImageReq, "req");
    }
}
