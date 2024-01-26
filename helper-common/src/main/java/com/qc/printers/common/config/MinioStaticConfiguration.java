package com.qc.printers.common.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MinioStaticConfiguration implements InitializingBean {
    public static String MINIO_URL;

    @Value("${minio.url}")
    private String minio_url;
    @Override
    public void afterPropertiesSet() throws Exception {
        MINIO_URL = minio_url;
    }
}
