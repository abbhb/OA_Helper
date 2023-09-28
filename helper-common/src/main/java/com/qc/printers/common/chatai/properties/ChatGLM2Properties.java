package com.qc.printers.common.chatai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ChatGLM2 配置文件
 *
 * @author zhaoyuhang
 * @date 2023/06/30
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "chatai.chatglm2")
public class ChatGLM2Properties {

    /**
     * 使用
     */
    private boolean use = false;

    /**
     * url
     */
    private String url;

    /**
     * 机器人 id
     */
    private Long AIUserId;

    /**
     * 每个用户每?分钟可以请求一次
     */
    private Long minute = 3L;

    /**
     * 超时
     */
    private Integer timeout = 60 * 1000;

}
