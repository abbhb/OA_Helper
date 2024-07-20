package com.qc.printers.common.config.system;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 暂时放在config里，后续分组功能做完再加入数据库完成分组任务功能
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "study")
public class StudyClockConfig {
    private Double minOldTime;

    private Integer maxFirstTime;
}
