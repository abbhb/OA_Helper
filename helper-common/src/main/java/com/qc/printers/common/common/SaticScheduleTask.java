package com.qc.printers.common.common;


import com.qc.printers.common.common.utils.apiCount.ApiCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class SaticScheduleTask {

    @Autowired
    private PrinterService printerService;


    @Scheduled(cron = "0 59 23 * * ?")
    //或直接指定时间间隔，例如：5秒
    //@Scheduled(fixedRate=5000)
    private void cleanApiCount() {
        ApiCount.cleanApiCount();
        log.info("cleanApiCount");
    }

    //注意此部分运行在appRun之前
    @Scheduled(cron = "0 59 23 * * ?")
    @PostConstruct
    //或直接指定时间间隔，例如：5秒
    //@Scheduled(fixedRate=5000)
    private void updataPrintTypeStatistics() {
        List<PrintDocumentTypeStatistic> printerTypeStatistics = printerService.getPrinterTypeStatistics();
        iRedisService.setPrintDocumentTypeStatistics(printerTypeStatistics);
        log.info("setPrintDocumentTypeStatistics");
    }
}