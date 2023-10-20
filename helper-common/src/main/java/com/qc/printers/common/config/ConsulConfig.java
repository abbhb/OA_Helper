package com.qc.printers.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "my-consul")
public class ConsulConfig {
    private String ip;

    private Integer port;

}

