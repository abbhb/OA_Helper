package com.qc.printers.common.print.consumer;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.constant.MQConstant;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.common.print.domain.vo.response.data.PrintDataImageFromPDFResp;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;


/**
 * Description: pdf生成缩略图的自监听，回复的消息给tag，只监听处理完成的tag
 * req为生产者发送的tag，需要处理
 * resp为处理完消费者当生产者放入的
 */
@RocketMQMessageListener(consumerGroup = MQConstant.SEND_PDF_IMAGE_GROUP, topic = MQConstant.SEND_PDF_IMAGE_TOPIC, selectorExpression = "resp")
@Component
public class PDFToImageConsumer implements RocketMQListener<PrintDataImageFromPDFResp> {

    @Override
    public void onMessage(PrintDataImageFromPDFResp printDataImageFromPDFResp) {
        PrinterRedis printerRedis = RedisUtils.get(MyString.print + printDataImageFromPDFResp.getId(), PrinterRedis.class);
        //到这的说明处理完了，成功或者失败给个原因
        if (printDataImageFromPDFResp.getStatus().equals(1)) {
            //成功
            //更新redis

            if (printerRedis == null) {
                throw new CustomException("异常");
            }
            printerRedis.setIsCanGetImage(1);
            printerRedis.setImageDownloadUrl(printDataImageFromPDFResp.getFilePDFImageUrl());
            RedisUtils.set(MyString.print + printDataImageFromPDFResp.getId(), printerRedis);
        } else {
            // 失败
            printerRedis.setIsCanGetImage(2);
            printerRedis.setMessage(printDataImageFromPDFResp.getMessage());
            //缩略图失败不认为整个打印失败，所以不删除打印任务
        }
    }
}
