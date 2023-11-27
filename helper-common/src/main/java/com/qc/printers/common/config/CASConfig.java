package com.qc.printers.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 废弃，之前用作接入第三方登录
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cas")
public class CASConfig {
    private String baseUrl;

    private String clientId;

    private String clientSecret;
}
