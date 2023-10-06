package com.qc.printers.common.common.event.listener.chat;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.constant.MQConstant;
import com.qc.printers.common.common.event.print.PrintPDFEvent;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.common.print.domain.enums.PrintReqTypeEnum;
import com.qc.printers.common.print.domain.vo.request.PrintBaseReq;
import com.qc.printers.common.print.domain.vo.request.data.PrintDataPDFToPrintReq;
import com.qc.printers.transaction.service.MQProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * pdf打印监听器
 */
@Slf4j
@Component
public class PrintPDFListener {

    @Autowired
    private MQProducer mqProducer;

    @Async
    @TransactionalEventListener(classes = PrintPDFEvent.class, fallbackExecution = true)
    public void printPDF(PrintPDFEvent event) {
        Long printId = event.getPrintId();
        if (!RedisUtils.hasKey(MyString.print + event.getPrintId())) {
            throw new CustomException("无任务");
        }
        ;
        PrinterRedis printerRedis = RedisUtils.get(MyString.print + event.getPrintId(), PrinterRedis.class);
        PrintDataPDFToPrintReq printDataPDFToImageReq = new PrintDataPDFToPrintReq(String.valueOf(printId), printerRedis.getCopies(), printerRedis.getIsDuplex(), printerRedis.getName(), printerRedis.getNeedPrintPagesIndex(), printerRedis.getNeedPrintPagesEndIndex(), printerRedis.getPdfUrl());
        mqProducer.sendMessageWithTags(MQConstant.SEND_PRINT_TOPIC, new PrintBaseReq<>(PrintReqTypeEnum.PDFTOPRINT.getType(), printDataPDFToImageReq), "req");
    }
}
