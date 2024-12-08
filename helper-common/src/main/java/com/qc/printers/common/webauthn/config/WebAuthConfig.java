package com.qc.printers.common.webauthn.config;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "authn")
@RequiredArgsConstructor
@Getter
@Setter
public class WebAuthConfig {

    private String hostName;

    private String display;

    private Set<String> origin;


}
