package com.qc.printers.common.config.system.signin;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "system-tip-message")
public class SigninTipMessageConfig {
    private String userId; // 推送签到成功消息给哪个用户,该用户需要在系统中存在

    private boolean enable = false; // 是否启动
}
