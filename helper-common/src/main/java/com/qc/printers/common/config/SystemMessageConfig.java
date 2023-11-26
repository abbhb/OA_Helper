package com.qc.printers.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "system-message")
public class SystemMessageConfig {
    private String userId;

    private String roomId;
}
