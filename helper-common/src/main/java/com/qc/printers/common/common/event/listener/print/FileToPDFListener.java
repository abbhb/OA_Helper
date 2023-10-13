package com.qc.printers.common.common.event.listener.print;

import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.constant.MQConstant;
import com.qc.printers.common.common.event.print.FileToPDFEvent;
import com.qc.printers.common.common.utils.MinIoUtil;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.oss.domain.OssReq;
import com.qc.printers.common.common.utils.oss.domain.OssResp;
import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.common.print.domain.entity.Printer;
import com.qc.printers.common.print.domain.vo.request.data.PrintDataFileToPDFReq;
import com.qc.printers.common.print.service.IPrinterService;
import com.qc.printers.transaction.service.MQProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 文件转pdf监听器
 */
@Slf4j
@Component
public class FileToPDFListener {
    @Autowired
    private IPrinterService iPrinterService;

    @Autowired
    private MinIoUtil minIoUtil;

    @Autowired
    private MQProducer mqProducer;

    @Async
    @TransactionalEventListener(classes = FileToPDFEvent.class, fallbackExecution = true)
    public void fileToPDF(FileToPDFEvent event) {
        Long printId = event.getPrintId();
        Printer printer = iPrinterService.getById(printId);
        OssResp preSignedObjectUrl = minIoUtil.getPreSignedObjectUrl(new OssReq("/", printer.getName(), printId, true));
        PrintDataFileToPDFReq printDataFileToPDFReq = new PrintDataFileToPDFReq(String.valueOf(printer.getId()), printer.getUrl(), preSignedObjectUrl.getDownloadUrl(), preSignedObjectUrl.getUploadUrl());
        PrinterRedis printerRedis = RedisUtils.get(MyString.print + printId, PrinterRedis.class);
        printerRedis.setSTU(2);
        RedisUtils.set(MyString.print + printId, printerRedis);
        mqProducer.sendMessageWithTags(MQConstant.SEND_FILE_TOPDF_TOPIC, printDataFileToPDFReq, "req");
    }
}
