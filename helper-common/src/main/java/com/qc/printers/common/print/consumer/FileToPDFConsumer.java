package com.qc.printers.common.print.consumer;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.constant.MQConstant;
import com.qc.printers.common.common.event.print.PDFToImageEvent;
import com.qc.printers.common.common.event.print.PrintErrorEvent;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.common.print.domain.vo.response.data.PrintDataFromPDFResp;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Description: file转pdf的自监听，回复的消息给tag，只监听处理完成的tag
 * req为生产者发送的tag，需要处理
 * resp为处理完消费者当生产者放入的
 */
@RocketMQMessageListener(consumerGroup = MQConstant.SEND_FILE_TOPDF_GROUP, topic = MQConstant.SEND_FILE_TOPDF_TOPIC, selectorExpression = "resp")
@Component
public class FileToPDFConsumer implements RocketMQListener<PrintDataFromPDFResp> {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onMessage(PrintDataFromPDFResp printDataFromPDFResp) {
        //到这的说明处理完了，成功或者失败给个原因
        if (printDataFromPDFResp.getStatus().equals(1)) {
            //成功
            //更新redis
            PrinterRedis printerRedis = RedisUtils.get(MyString.print + printDataFromPDFResp.getId(), PrinterRedis.class);
            if (printerRedis == null) {
                throw new CustomException("异常");
            }
            printerRedis.setPageNums(printDataFromPDFResp.getPageNums());
            printerRedis.setSTU(3);
            printerRedis.setPdfUrl(printDataFromPDFResp.getFilePDFUrl());
            RedisUtils.set(MyString.print + printDataFromPDFResp.getId(), printerRedis);
            //这里成功了再获取缩略图
            applicationEventPublisher.publishEvent(new PDFToImageEvent(this, Long.valueOf(printDataFromPDFResp.getId())));
        } else {
            // 失败
            PrinterRedis printerRedis = RedisUtils.get(MyString.print + printDataFromPDFResp.getId(), PrinterRedis.class);
            printerRedis.setSTU(0);
            printerRedis.setMessage(printDataFromPDFResp.getMessage());
            //失败在600s删除
            RedisUtils.set(MyString.print + printDataFromPDFResp.getId(), printerRedis, 600, TimeUnit.SECONDS);
            applicationEventPublisher.publishEvent(new PrintErrorEvent(this, Long.valueOf(printDataFromPDFResp.getId())));
        }
    }
}
