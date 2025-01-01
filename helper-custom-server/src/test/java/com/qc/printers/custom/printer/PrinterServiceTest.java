package com.qc.printers.custom.printer;

import com.qc.printers.common.print.domain.vo.request.PreUploadPrintFileReq;
import com.qc.printers.common.signin.domain.entity.SigninRenewal;
import com.qc.printers.common.signin.mapper.SigninRenewalMapper;
import com.qc.printers.custom.print.service.PrinterService;
import com.qc.printers.custom.user.domain.vo.response.UserSelectListResp;
import com.qc.printers.custom.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)

public class PrinterServiceTest {
    @Autowired
    private PrinterService printerService;


    @Test
    public void preUploadPrintFileTest() throws Exception {
        PreUploadPrintFileReq preUploadPrintFileReq = new PreUploadPrintFileReq();
        preUploadPrintFileReq.setHash("f9f120c6e19f18c1c1e424979d1b6727");
        preUploadPrintFileReq.setOriginFileName("四川银行ibmmq-exporter优化.pdf");
        String s = printerService.preUploadPrintFile(preUploadPrintFileReq);

        log.info("preUploadPrintFileReq{}", s);
    }
}
