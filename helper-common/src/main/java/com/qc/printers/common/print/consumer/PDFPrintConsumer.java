package com.qc.printers.common.print.consumer;

import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.constant.MQConstant;
import com.qc.printers.common.common.event.print.PrintErrorEvent;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.common.print.domain.vo.response.data.PrintDataFromPrintResp;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Description: pdf打印的自监听，回复的消息给tag，只监听处理完成的tag
 * 生产者的tag是动态的，找注册中心的打印机，然后用户选择打印机
 * resp为处理完消费者当生产者放入的
 */
@RocketMQMessageListener(consumerGroup = MQConstant.SEND_PDF_IMAGE_GROUP, topic = MQConstant.SEND_PDF_IMAGE_TOPIC, selectorExpression = "resp")
@Component
public class PDFPrintConsumer implements RocketMQListener<PrintDataFromPrintResp> {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onMessage(PrintDataFromPrintResp printDataFromPrintResp) {
        PrinterRedis printerRedis = RedisUtils.get(MyString.print + printDataFromPrintResp.getId(), PrinterRedis.class);

        if (printDataFromPrintResp.getIsSuccess().equals(1)) {
            //打印成功
            //更新数据库状态，并且删除在100s后
            printerRedis.setSTU(5);
            printerRedis.setIsPrint(1);
        } else {
            //打印失败
            //更新数据库状态，并且删除在100s后
            //交给打印异常事件
            applicationEventPublisher.publishEvent(new PrintErrorEvent(this, Long.valueOf(printDataFromPrintResp.getId())));
        }
    }
}
