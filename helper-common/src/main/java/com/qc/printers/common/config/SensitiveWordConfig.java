package com.qc.printers.common.config;

import com.qc.printers.common.common.utils.sensitiveWord.DFAFilter;
import com.qc.printers.common.common.utils.sensitiveWord.SensitiveWordBs;
import com.qc.printers.common.sensitive.MyWordDeny;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SensitiveWordConfig {

    @Autowired
    private MyWordDeny myWordDeny;

    /**
     * 初始化引导类
     *
     * @return 初始化引导类
     * @since 1.0.0
     */
    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        return SensitiveWordBs.newInstance()
                .filterStrategy(DFAFilter.getInstance())
                .sensitiveWord(myWordDeny)
                .init();
    }

}