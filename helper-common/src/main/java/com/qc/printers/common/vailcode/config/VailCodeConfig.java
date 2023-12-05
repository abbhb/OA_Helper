package com.qc.printers.common.vailcode.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vail-code")
public class VailCodeConfig {
    private boolean use;
}
