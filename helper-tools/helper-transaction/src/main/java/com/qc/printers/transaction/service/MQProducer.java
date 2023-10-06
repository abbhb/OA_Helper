package com.qc.printers.transaction.service;

import com.qc.printers.transaction.annotation.SecureInvoke;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Description: 发送mq工具类
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-08-12
 */
public class MQProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendMsg(String topic, Object body) {
        Message<Object> build = MessageBuilder.withPayload(body).build();
        rocketMQTemplate.send(topic, build);
    }

    /**
     * 发送可靠消息，在事务提交后保证发送成功
     *
     * @param topic
     * @param body
     */
    @SecureInvoke
    public void sendSecureMsg(String topic, Object body, Object key) {
        Message<Object> build = MessageBuilder
                .withPayload(body)
                .setHeader("KEYS", key)
                .build();
        rocketMQTemplate.send(topic, build);
    }

    /**
     * 发布且携带Tag
     */
    public void sendMessageWithTags(String topic, Object body, String tags) {
        // 创建消息
        Message<Object> rocketMQMessage = MessageBuilder.withPayload(body)
                .setHeader("rocketmq_TAGS", tags) // 设置标签
                .build();
        // 发送消息
        rocketMQTemplate.send(topic, rocketMQMessage);
    }
}
