package com.qc.printers.common.print.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.constant.MQConstant;
import com.qc.printers.common.common.event.print.PrintErrorEvent;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.common.print.domain.entity.Printer;
import com.qc.printers.common.print.domain.vo.response.data.PrintDataFromPrintResp;
import com.qc.printers.common.print.service.IPrinterService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Description: pdf打印的自监听，回复的消息给tag，只监听处理完成的tag
 * 生产者的tag是动态的，找注册中心的打印机，然后用户选择打印机
 * resp为处理完消费者当生产者放入的
 */
@RocketMQMessageListener(consumerGroup = MQConstant.SEND_PRINT_R_GROUP, topic = MQConstant.SEND_PRINT_R_TOPIC, selectorExpression = "resp")
@Component
public class PDFPrintConsumer implements RocketMQListener<PrintDataFromPrintResp> {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private IPrinterService iPrinterService;

    @Override
    public void onMessage(PrintDataFromPrintResp printDataFromPrintResp) {
        PrinterRedis printerRedis = RedisUtils.get(MyString.print + printDataFromPrintResp.getId(), PrinterRedis.class);

        if (printDataFromPrintResp.getIsSuccess().equals(1)) {
            //打印成功
            //更新数据库状态，并且删除在100s后
            LambdaUpdateWrapper<Printer> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(Printer::getId, Long.valueOf(printDataFromPrintResp.getId()));
            lambdaUpdateWrapper.set(Printer::getIsPrint, 1);
            lambdaUpdateWrapper.set(Printer::getCopies, printerRedis.getCopies());
            lambdaUpdateWrapper.set(Printer::getIsDuplex, printerRedis.getIsDuplex());
            lambdaUpdateWrapper.set(Printer::getOriginFilePages, printerRedis.getPageNums());
            lambdaUpdateWrapper.set(Printer::getNeedPrintPagesIndex, printerRedis.getNeedPrintPagesIndex());
            lambdaUpdateWrapper.set(Printer::getNeedPrintPagesEndIndex, printerRedis.getNeedPrintPagesEndIndex());
            lambdaUpdateWrapper.set(Printer::getSingleDocumentPaperUsage, (printerRedis.getIsDuplex().equals(2) ? (int) Math.ceil((double) (printerRedis.getNeedPrintPagesEndIndex() - printerRedis.getNeedPrintPagesIndex() + 1) / 2.0) : (printerRedis.getNeedPrintPagesEndIndex() - printerRedis.getNeedPrintPagesIndex() + 1)));
            iPrinterService.update(lambdaUpdateWrapper);
            printerRedis.setSTU(5);
            printerRedis.setIsPrint(1);
            RedisUtils.set(MyString.print + printDataFromPrintResp.getId(), printerRedis, 100L, TimeUnit.SECONDS);
        } else {
            //打印失败
            //交给打印异常事件
            applicationEventPublisher.publishEvent(new PrintErrorEvent(this, Long.valueOf(printDataFromPrintResp.getId()), printDataFromPrintResp.getMessage()));
        }
    }
}
